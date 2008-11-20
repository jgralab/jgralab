grammar Greql2;
options {backtrack=true; memoize=true;}

@lexer::members {
  protected boolean enumIsKeyword = true;
  protected boolean assertIsKeyword = true;
}


@header {
package de.uni_koblenz.jgralab.greql2.parser;
 
import de.uni_koblenz.jgralab.greql2.funlib.Greql2FunctionLibrary;
}

@lexer::header {
package de.uni_koblenz.jgralab.greql2.parser;

import java.util.Vector;
import java.util.*;
import java.util.logging.Logger;
import de.uni_koblenz.jgralab.*;
import de.uni_koblenz.jgralab.schema.*;
import de.uni_koblenz.jgralab.greql2.schema.*;
import de.uni_koblenz.jgralab.greql2.*;
import de.uni_koblenz.jgralab.greql2.schema.impl.*;
import de.uni_koblenz.jgralab.greql2.exception.*;
import de.uni_koblenz.jgralab.greql2.funlib.Greql2FunctionLibrary;
}

@members {
    private static Logger logger = Logger.getLogger(Greql2Parser.class.getName());
    private final int VMAX = 100;
    private final int EMAX = 100;
    private Greql2Schema schema = null;
    private Greql2 graph = null;
    private SymbolTable variableSymbolTable = null;
    private SymbolTable functionSymbolTable = null;
    private SymbolTable nonterminalSymbolTable = null;
    private GraphClass graphClass = null;
    private boolean isAdditiveExpression = true;

    class FunctionConstruct {
    	String operatorName;  
    	Operator op;
    	Expression arg1 = null;
        Expression arg2 = null;
        FunctionId op = null;
        int offsetArg1 = 0;
        int lengthArg1 = 0;
        int offsetOperator = 0;
        int offsetArg2 = 0;
        int lengthOperator = 0;
        int lengthArg2 = 0;
        boolean binary = true;
    
        public void preUnaryOp() {
          binary = false;
          offsetOperator = getLTOffset();
        }
    
        public void preArg1() {
          offsetArg1 = getLTOffset();
        }
        
        public void preOp(Expression arg1) { 
          binary = true;
          this.arg1 = arg1;
          lengthArg1 = getLTLength();
          offsetOperator = getLTOffset();
        }
        
        public void postOp(String op) {
          lengthOperator = getLTLength();
	  offsetArg2 = getLTOffset();
	  operatorName = op;
        }
        
        public FunctionApplication postArg2(Expression arg2) { 
          lengthArg2 = getLTLength();
          this.arg2 = arg2;
          // retrieve operator...
  	  op = (FunctionId) functionSymbolTable.lookup(operatorName);
    	  //... or create a new one and add it to the symboltable
          if (op == null) {
	      op = graph.createFunctionId();
              op.setName(operatorName);
              functionSymbolTable.insert(operatorName, op);
          }
          // add operator
       	  return createFunctionIdAndArgumentOf(op, offsetOperator,lengthOperator, 
	   			  arg1, offsetArg1, lengthArg1, arg2, offsetArg2, lengthArg2, binary); 	   
        }
    }

    private void createFunctionIdAndArgumentOf(FunctionId functionId, int offsetOperator, int lengthOperator, Expression arg1, int offsetArg1, int lengthArg1, Expression arg2, int offsetArg2, int lengthArg2, boolean binary) {
    	FunctionApplication fa = graph.createFunctionApplication();
    	IsFunctionIdOf functionIdOf = graph.createIsFunctionIdOf(functionId, fa);
    	functionIdOf.setSourcePositions((createSourcePositionList(lengthOperator, offsetOperator)));
    	IsArgumentOf arg1Of = graph.createIsArgumentOf(arg1, fa);
    	arg1Of.setSourcePositions((createSourcePositionList(lengthArg1, offsetArg1)));
    	if (binary) {
      	   IsArgumentOf arg2Of = graph.createIsArgumentOf(arg2, fa);
    	   arg2Of.setSourcePositions((createSourcePositionList(lengthArg2, offsetArg2)));
    	}  
    }	
    
    private List<SourcePosition> createSourcePositionList(int length, int offset) {
    	List<SourcePosition> list = new ArrayList<SourcePosition>();
    	list.add(new SourcePosition(length, offset));
    	return list;
    }


	public void addPathElement(Class<? extends PathDescription> vc, Class<? extends Edge> ec, PathDescription pathDescr, PathDescription part1, PathDescription part2) {
	 	lengthPart2 = getLTLength();
	 	Edge edge = null;
	 	if (pathDescr == null) {
	 		pathDescr = graph.createVertex(vc);
	 		edge = graph.createEdge(ec, part1, pathDescr);
	 		edge.setSourcePositions((createSourcePositionList(lengthPart1, offsetPart1)));
	 	}
		edge = graph.createIsEdge(ec, alt2, pathDescr);
		edge.setSourcePositions((createSourcePositionList(lengthPart2, offsetPart2 )));
	}

    private boolean isFunctionName(String ident) {
        return Greql2FunctionLibrary.instance().isGreqlFunction(ident);		
    }	
	
	/** Returns the abstract syntax graph for the input
     *  @return the abstract syntax graph representing a GReQL 2 query
     */
    public Greql2 getGraph()  {
    	return graph;
    }

    /** Returns the schema of the abstract syntax graph
     *  @return the schema
     */
    public Schema getSchema()  {
    	return schema;
    }

    /** Loads the schema from the file "greql2Schema.tg",
     *  creates a new graph and symbol tables for variables and
     *  function-ids and retrieves the Greql2-graphclass (graphClass)
     *
     */
    private void initialize() {
       	schema = Greql2Schema.instance();
	graph = Greql2Impl.create(VMAX,EMAX);
        variableSymbolTable = new SymbolTable();
 	functionSymbolTable = new SymbolTable();
	nonterminalSymbolTable = new SymbolTable();
	functionSymbolTable.blockBegin();
        graphClass = schema.getGraphClass(new QualifiedName("Greql2"));
   }
   
   private int getLTLength() {
	return (- offset + LT(0).getColumn()-1 + LT(0).getText().length());
   }
   
   private int getLTOffset() {
	return LT(1).getColumn()-1; 
   }
   
    /**
     *	@see antlr.Parser#reportError(RecognitionException)
     */
  	public void reportError(RecognitionException e)
 	{
		int offset = -1;
		try
		{
			offset = LT(1).getColumn()-1;
		}
		catch (Exception ex) { ex.printStackTrace(); }
		if (offset != -1)
				logger.severe("error: " + offset +": " + e.getMessage());
			else logger.severe("error (offset = -1): " + e.getMessage());
	}

  	public void reportError(TokenStreamException e)
 	{
		int offset = -1;
		try
		{
			offset = LT(1).getColumn()-1;
		}
		catch (Exception ex) { ex.printStackTrace(); }
		if (offset != -1)
				logger.severe("error: " + offset +": " + e.getMessage());
			else logger.severe("error (offset = -1): " + e.getMessage());
	}

    /**
    *	merges variable-vertices of the graph which represent identical variables
    */
    private void mergeVariables() throws DuplicateVariableException, UndefinedVariableException {
	    // retrieve root-vertex
		Greql2Expression root = graph.getFirstGreql2Expression();
		mergeVariablesInGreql2Expression(root);
	}

	/**
	*	merges variable-vertices in the subgraph with the root-vertex <code>v</code>
	*   @param v root of the subgraph
	*/
    private void mergeVariables(Vertex v) throws DuplicateVariableException, UndefinedVariableException {
		if (v instanceof DefinitionExpression) {
			mergeVariablesInDefinitionExpression((DefinitionExpression) v);
		} else if (v instanceof Comprehension) {
			mergeVariablesInComprehension((Comprehension) v);
		} else if (v instanceof QuantifiedExpression) {
			mergeVariablesInQuantifiedExpression((QuantifiedExpression) v);
		} else if (v instanceof Greql2Expression) {
			mergeVariablesInGreql2Expression((Greql2Expression) v);
		} else if (v instanceof ThisLiteral) {
		    return;	 
		} else if (v instanceof Variable) {
			Vertex var = variableSymbolTable.lookup( ( (Variable) v).getName());
			if (var != null) {
				Edge inc = v.getFirstEdge(EdgeDirection.OUT);
				inc.setAlpha(var);
				if (v.getDegree() <= 0) {
					v.delete();
				}
			} else {
				Greql2Aggregation e = (Greql2Aggregation) v.getFirstEdge(EdgeDirection.OUT);
				throw new UndefinedVariableException(((Variable)v).getName(), e.getSourcePositions() );
			}
		} else {
			Edge inc = v.getFirstEdge(EdgeDirection.IN);
			LinkedList<Edge> incidenceList = new LinkedList<Edge>();
			while (inc != null) {
				incidenceList.add(inc);
				inc = inc.getNextEdge(EdgeDirection.IN);
			}
			for (Edge e : incidenceList) 
				mergeVariables(e.getAlpha());
		}
	}

	/**
	*	Inserts variable-vertices that are declared in the <code>using</code>-clause
	*   into the variables symbol table and
	*	merges variables within the query-expression.
	*   @param root root of the graph, represents a <code>Greql2Expression</code>
	*/
	private void mergeVariablesInGreql2Expression(Greql2Expression root) throws DuplicateVariableException, UndefinedVariableException {
		variableSymbolTable.blockBegin();
		IsBoundVarOf isBoundVarOf = root.getFirstIsBoundVarOf(EdgeDirection.IN);
		while (isBoundVarOf != null) {
		 	 variableSymbolTable.insert( ((Variable) isBoundVarOf.getAlpha()).getName(), isBoundVarOf.getAlpha());
			 isBoundVarOf = isBoundVarOf.getNextIsBoundVarOf(EdgeDirection.IN);
		}
		IsQueryExprOf isQueryExprOf = root.getFirstIsQueryExprOf(EdgeDirection.IN);
		mergeVariables(isQueryExprOf.getAlpha());
		variableSymbolTable.blockEnd();
	}

	/**
	*	Inserts variables that are defined in the definitions of let- or
	*   where-expressions and merges variables used in these definitions and in the bound expression
	*   @param v contains a let- or where-expression.
	*/
	private void mergeVariablesInDefinitionExpression(DefinitionExpression v) throws DuplicateVariableException, UndefinedVariableException	{
		variableSymbolTable.blockBegin();
		IsDefinitionOf isDefinitionOf = v.getFirstIsDefinitionOf(EdgeDirection.IN);
		while (isDefinitionOf != null) {
			Definition definition = (Definition) isDefinitionOf.getAlpha();
			Variable variable = (Variable) definition.getFirstIsVarOf(EdgeDirection.IN).getAlpha();
			variableSymbolTable.insert(variable.getName(), variable);
			isDefinitionOf = isDefinitionOf.getNextIsDefinitionOf(EdgeDirection.IN);
		}
		isDefinitionOf = v.getFirstIsDefinitionOf(EdgeDirection.IN);
		while (isDefinitionOf != null) {
			Definition definition = (Definition) isDefinitionOf.getAlpha();
			Expression expr = (Expression) definition.getFirstIsExprOf(EdgeDirection.IN).getAlpha();
			mergeVariables(expr);
			isDefinitionOf = isDefinitionOf.getNextIsDefinitionOf(EdgeDirection.IN);
		}
		Edge isBoundExprOf = v.getFirstIsBoundExprOfDefinition(EdgeDirection.IN);
		mergeVariables(isBoundExprOf.getAlpha());
		variableSymbolTable.blockEnd();
	}

	/**
	*	Inserts variables that are declared in a declaration of a simple query
	*   or a quantified expression into the symbol-table and merges variables
	*   that are used in these declaration (in typeexpressions, constraints, or subgraphs)
	*   @param v contains a declaration
	*/
	private void mergeVariablesInDeclaration(Declaration v) throws DuplicateVariableException, UndefinedVariableException	{
		IsSimpleDeclOf isSimpleDeclOf = v.getFirstIsSimpleDeclOf(EdgeDirection.IN);
		while (isSimpleDeclOf != null) {
			SimpleDeclaration simpleDecl = (SimpleDeclaration) isSimpleDeclOf.getAlpha();
			IsDeclaredVarOf isDeclaredVarOf = simpleDecl.getFirstIsDeclaredVarOf(EdgeDirection.IN);
			while (isDeclaredVarOf != null)	{
				Variable variable = (Variable) isDeclaredVarOf.getAlpha();
				variableSymbolTable.insert(variable.getName(), variable);
				isDeclaredVarOf = isDeclaredVarOf.getNextIsDeclaredVarOf(EdgeDirection.IN);
			}
			isSimpleDeclOf = isSimpleDeclOf.getNextIsSimpleDeclOf(EdgeDirection.IN);
		}
		isSimpleDeclOf = v.getFirstIsSimpleDeclOf(EdgeDirection.IN);
		while (isSimpleDeclOf != null) {
			SimpleDeclaration simpleDecl = (SimpleDeclaration) isSimpleDeclOf.getAlpha();
			Expression expr = (Expression) simpleDecl.getFirstIsTypeExprOf(EdgeDirection.IN).getAlpha();
			mergeVariables(expr); 
			isSimpleDeclOf = isSimpleDeclOf.getNextIsSimpleDeclOf(EdgeDirection.IN);
		}
		IsSubgraphOf isSubgraphOf = v.getFirstIsSubgraphOf(EdgeDirection.IN);
		if (isSubgraphOf != null) {
			mergeVariables(isSubgraphOf.getAlpha());
		}
		IsConstraintOf isConstraintOf = v.getFirstIsConstraintOf(EdgeDirection.IN);
		while (isConstraintOf != null) {
			mergeVariables(isConstraintOf.getAlpha());
			isConstraintOf = isConstraintOf.getNextIsConstraintOf(EdgeDirection.IN);
		}
	}

	/**
	*	Inserts variable-vertices that are declared in the quantified expression
	*   represented by <code>v</code> into the variables symbol table and
	*	merges variables within the bound expression.
	*   @param v contains a quantified expression
	*/
	private void mergeVariablesInQuantifiedExpression(QuantifiedExpression v) throws DuplicateVariableException, UndefinedVariableException {
		variableSymbolTable.blockBegin();
		IsQuantifiedDeclOf isQuantifiedDeclOf = v.getFirstIsQuantifiedDeclOf(EdgeDirection.IN);
		mergeVariablesInDeclaration((Declaration) isQuantifiedDeclOf.getAlpha());
		IsBoundExprOfQuantifier isBoundExprOfQuantifier = v.getFirstIsBoundExprOfQuantifier(EdgeDirection.IN);
		mergeVariables(isBoundExprOfQuantifier.getAlpha()); 
		variableSymbolTable.blockEnd();
	}

	/**
	*	Inserts declared variable-vertices
	*   into the variables symbol table and
	*	merges variables within the comprehension result and tableheaders
	*   @param v contains a set- or a bag-comprehension
	*/
	private void mergeVariablesInComprehension(Comprehension v) throws DuplicateVariableException, UndefinedVariableException {
		variableSymbolTable.blockBegin();
		Edge IsCompDeclOf = v.getFirstIsCompDeclOf(EdgeDirection.IN);
		mergeVariablesInDeclaration((Declaration)IsCompDeclOf.getAlpha());
		Edge IsCompResultDefOf = v.getFirstIsCompResultDefOf(EdgeDirection.IN);
		mergeVariables(IsCompResultDefOf.getAlpha());
		// merge variables in table-headers if it's a bag-comprehension
		if (v instanceof BagComprehension) {
			IsTableHeaderOf isTableHeaderOf = v.getFirstIsTableHeaderOf(EdgeDirection.IN);
			while (isTableHeaderOf != null)	{
				mergeVariables(isTableHeaderOf.getAlpha());
				isTableHeaderOf = isTableHeaderOf.getNextIsTableHeaderOf(EdgeDirection.IN);
			}
		}
		if (v instanceof TableComprehension) {
			TableComprehension tc = (TableComprehension) v;
			IsColumnHeaderExprOf ch = tc.getFirstIsColumnHeaderExprOf(EdgeDirection.IN);
			mergeVariables(ch.getAlpha());
			IsRowHeaderExprOf rh = tc.getFirstIsRowHeaderExprOf(EdgeDirection.IN);
			mergeVariables(rh.getAlpha());
			IsTableHeaderOf th = tc.getFirstIsTableHeaderOf(EdgeDirection.IN);
			if (th != null)
				mergeVariables(th.getAlpha());
		}
		variableSymbolTable.blockEnd();
	}


	private String decode(String s) {
		s = s.substring(1,s.length()-1);
		int i = s.indexOf('\\');
		while (i>= 0) {
			char c = s.charAt(i+1);
			switch(c) {
				case 'u' :
					char unicodeChar = (char) Integer.parseInt(s.substring(i+2, i+6), 16);
					s = s.substring(0,i) +  unicodeChar + s.substring(i+6);
					break;
				case '\\':
					s = s.substring(0,i) +  '\\' + s.substring(i+2);
					break;
				case '\"':
					s = s.substring(0,i) +  '\"' + s.substring(i+2);
					break;
				case 'n':
					s = s.substring(0,i) + '\n' + s.substring(i+2);
					break;
				case 'r':
					s = s.substring(0,i) + '\r' + s.substring(i+2);
					break;
				case 't':
					s = s.substring(0,i) + '\t' + s.substring(i+2);
					break;
				case 'f':
					s = s.substring(0,i) + '\r' + s.substring(i+2);
					break;
				case '\'':
					s = s.substring(0,i) + '\r' + s.substring(i+2);
					break;
				default:
					if (Character.isDigit(c)) {
						if (c < 4) {
							char octChar = (char) Integer.parseInt(s.substring(i+1, i+4), 8);
							s = s.substring(0,i) + octChar + s.substring(i+4);
						} else {
							char octChar = (char) Integer.parseInt(s.substring(i+1, i+3), 8);
							s = s.substring(0,i) + octChar + s.substring(i+3);
						}
					}
			}
			i = s.indexOf('\\', i+1);
		}
		return s;
	}

	/**
	 * saves graph to file.
	 * @param filename name of the file.
	 */
	public void saveGraph(String filename) throws GraphIOException {
		GraphIO.saveGraphToFile(filename, graph, null);
	}

}

T		    : 'T';
AND 		: 'and';
FALSE 		: 'false';
NOT 		: 'not';
NULL_VALUE 	: 'null';
OR  		: 'or';
TRUE 		: 'true';
XOR 		: 'xor';
AS 		: 'as';
BAG 		: 'bag';
E 		: 'E';
ESUBGRAPH	: 'eSubgraph';
EXISTS_ONE	: 'exists!';
EXISTS		: 'exists';
END 		: 'end';
FORALL		: 'forall';
FROM  		: 'from';
IN 		: 'in';
LET 		: 'let';
LIST 		: 'list';
PATH 		: 'path';
PATHSYSTEM 	: 'pathsystem';
REC 		: 'rec';
REPORT 		: 'report';
REPORTSET	: 'reportSet';
REPORTBAG	: 'reportBag';
REPORTTABLE	: 'reportTable';
STORE		: 'store';
SET 		: 'set';
TUP 		: 'tup';
USING		: 'using';
V 		: 'V';
VSUBGRAPH	: 'vSubgraph';
WHERE 		: 'where';
WITH 		: 'with';
QUESTION 	: '?';
EXCL 		: '!';
COLON 		: ':';
COMMA 		: ',';
DOT		: '.';
AT		: '@';
LPAREN		: '(';
RPAREN		: ')';
LBRACK		: '[';
RBRACK 		: ']';
LCURLY 		: '{';
RCURLY 		: '}';
ASSIGN		: ':=';
GASSIGN 	: '::=';
EQUAL		: '=';
MATCH 		: '=~';
NOT_EQUAL 	: '<>';
LE		: '<=';
GE		: '=>';
L_T		: '<';
G_T		: '>';
DIV		: '/';
PLUS		: '+';
MINUS		: '-';
STAR		: '*';
MOD		: '%';
SEMI		: ';';
CARET		: '^';
BOR		: '|';
AMP		: '&';	
SMILEY		: ':-)';
//EDGESTART	: '<-';
//EDGEEND		: '->';
//EDGE		: '--';
RARROW		: '-->';
LARROW  	: '<--';
ARROW		: '<->';
HASH 		: '#';
OUTAGGREGATION	: '<>--';
INAGGREGATION   : '--<>';
PATHSYSTEMSTART : '-<';	 	
	
IDENT :	(('a'..'z'|'A'..'Z') ('a'..'z'|'A'..'Z'|'_'|'0'..'9')*
	{			

	})
;

NUM_INT :	('1'..'9')('0'..'9')+
;
/**
//		if ($getText.equals("thisEdge")) 
//			{_ttype = THISEDGE;}
//		else if ($getText.equals("thisVertex"))
//			{_ttype = THISVERTEX;}
//		else if (isFunctionName($getText))
//			{_ttype = FUNCTIONID;} 		
*/
variableList returns [Vector<VertexPosition> variables = new Vector<VertexPosition>();] 
@init{
    Variable var = null;
    Vector<VertexPosition> list = null;
    VertexPosition v = new VertexPosition();
    int offset = 0;
    int length = 0;
    offset = LT(1).getColumn()-1;
}
: var = variable
  {
   	length = getLTLength();
        v.node = var;
        v.offset = offset;
        v.length = length;
        variables.add(v);
  }
  (COMMA list = variableList
        {
            variables.addAll(list);
        }
  )?
;

/** matches a variable
	@return the variable-vertex
*/
variable returns [Variable var = null] 
	:
		IDENT
        {
           	var = graph.createVariable();
        }
;
/**	        var.setName($getText());*/

greqlExpression 
@init{
	Expression expr = null;
	Token id = null;
	Vector<VertexPosition> varList = new Vector<VertexPosition>();
	int offset = 0;
	int length = 0;
	initialize();
	Identifier id = null;
}
: (
    (USING varList = variableList COLON)?
    { offset = LT(1).getColumn()-1; }
    expr = expression
    (STORE AS id = IDENT)?
    {
	length = getLTLength();
	Greql2Expression root = graph.createGreql2Expression();
	// add using-variables
	for (int i = 0; i < varList.size(); i++) {
	    VertexPosition var = varList.get(i);
	    IsBoundVarOf isVarOf = graph.createIsBoundVarOf((Variable)var.node, root);
	    isVarOf.setSourcePositions((createSourcePositionList(var.length, var.offset)));
    }
    IsQueryExprOf e = graph.createIsQueryExprOf(expr, root);
    e.setSourcePositions((createSourcePositionList(length, offset)));
	// query result stored as...
	if (id != null)	{
	     Identifier ident = graph.createIdentifier();
	     ident.setName(id.getText());
	     IsIdOf isId = graph.createIsIdOf(ident, root);
	     isId.setSourcePositions((createSourcePositionList(id.getText().length(), id.getColumn()-1)));
	   }
   }
   EOF { mergeVariables();}
  | EOF {graph = null;}/* allow empty input */ )
;


/** matches expressions
    @return  vertex representing the expression
*/
expression returns [Expression retVal = null]
@init{
	Expression expr = null;
}
@after{
  expr = expr;
}  
:
	expr = quantifiedExpression
;



/** matches quantifiedExpressions
    @return vertex representing the quantified expression
*/
quantifiedExpression returns [Expression expr]
@init{
	Quantifier q;
	Declaration decl;
	int offsetQuantifier = 0;
	int offsetQuantifiedDecl = 0;
	int offsetQuantifiedExpr = 0;
	int lengthQuantifier = 0;
	int lengthQuantifiedDecl = 0;
	int lengthQuantifiedExpr = 0;
	Expression tempExpression = null;
}
@after{
  expr = tempExpression;
}  
:
  (
    {
       	offsetQuantifier = LT(1).getColumn()-1;
       	lengthQuantifier = LT(1).getText().length();
    }
    // starts with quantifier ...
    q = quantifier
    { offsetQuantifiedDecl = LT(1).getColumn()-1; }
    // ...followed by a declaration...
    decl = quantifiedDeclaration
    { lengthQuantifiedDecl = - offsetQuantifiedDecl + LT(0).getColumn()-1 + LT(0).getText().length(); }
    AT
    { offsetQuantifiedExpr = LT(1).getColumn()-1; }
    // ... ends with predicate: a quantifiedExpr or something of lower level
    tempExpression = quantifiedExpression
    {
      	lengthQuantifiedExpr = - offsetQuantifiedExpr + LT(0).getColumn()-1 + LT(0).getText().length();
        // create new Quantifies Expr
	QuantifiedExpression quantifiedExprVertex = graph.createQuantifiedExpression();
	// add quantifier
	IsQuantifierOf quantifierOf = graph.createIsQuantifierOf(q, quantifiedExprVertex);
	quantifierOf.setSourcePositions((createSourcePositionList(lengthQuantifier, offsetQuantifier)));
	// add declaration
	IsQuantifiedDeclOf quantifiedDeclOf = graph.createIsQuantifiedDeclOf(decl, quantifiedExprVertex);
	quantifiedDeclOf.setSourcePositions((createSourcePositionList(lengthQuantifiedDecl, offsetQuantifiedDecl)));
	// add predicate
	IsBoundExprOf boundExprOf = graph.createIsBoundExprOfQuantifier(tempExpression, quantifiedExprVertex);
	boundExprOf.setSourcePositions((createSourcePositionList(lengthQuantifiedExpr, offsetQuantifiedExpr)));
	// return the right vertex...
	tempExpression = quantifiedExprVertex;
    }
)
| // not a "real" quantified expression
  rempExpression = letExpression
;

/** matches a quantified declaration which contains
	simple declarations and boolean expressions,
	each of them separated by  ','.
*/
quantifiedDeclaration returns [Declaration declaration = null]
@init{
    Expression subgraphExpr = null;
    Expression constraintExpr = null;
    Vector<VertexPosition> declarations = new Vector<VertexPosition>();
    int offsetConstraint = 0;
    int offsetSubgraph = 0;
    int lengthConstraint = 0;
    int lengthSubgraph = 0;
}
:
;

/** matches a quantifier
*/
quantifier returns [Quantifier quantifier = null]
@init{
    String name = "";
}
:
(
    FORALL {name = "forall";}
|   ((EXISTS EXCL) => (EXISTS EXCL {name = "exists!";})
    EXISTS {name = "exists";})
)
{
    for (Quantifier q : graph.getQuantifierVertices()) {
    	if (q.getName().equals(name)
    		return q;
    }
    quantifier = graph.createQuantifier();
    quantifier.setName(name);
}
;


/** matches let-expressions
    @return
*/
letExpression returns [Expression expr = null]
@init{
	Vector<VertexPosition> defList = new Vector<VertexPosition>();
	int offset = 0;
	int length = 0;
	Expression tempExpression = null;
}
@after{
  expr = tempExpression;
}  
:
(
  LET
  // definitions
  defList = definitionList
  IN
  { offset = LT(1).getColumn()-1; }
  // bound expression
  tempExpression = letExpression
  {
      length = -offset + LT(0).getColumn()-1 + LT(0).getText().length();
      if (defList.size() != 0) {
 	// create new letexpression-vertex
	LetExpression letExpr = graph.createLetExpression();
	// set bound expression
	IsBoundExprOf exprOf = graph.createIsBoundExprOfDefinition(tempExpression, letExpr);
	exprOf.setSourcePositions((createSourcePositionList(length, offset)));
	// add definitions
	for (int i = 0; i < defList.size(); i++) {
		VertexPosition def = defList.get(i);
		IsDefinitionOf definitionOf = graph.createIsDefinitionOf((Definition)def.node, letExpr);
		definitionOf.setSourcePositions((createSourcePositionList(def.length, def.offset)));
	}
	// return letExpr
	tempExpression = letExpr;
        }
  }
)
| // not a let-Expression
  tempExpression = whereExpression
;

/** matches Where-Expressions
	@return
*/
whereExpression returns [Expression retVal = null]
@init{
	Vector<VertexPosition> defList = new Vector<VertexPosition>();
	int offset = 0;
	int length = 0;
	Expression expr = null;
}
@after {
	retVal = expr;
}
:
{ offset = LT(1).getColumn()-1; }
// bound expression
expr = conditionalExpression
{ length = -offset + LT(0).getColumn()-1 + LT(0).getText().length(); }
       	// optional "where"-part:
(
	WHERE
	defList = definitionList
)?
{
if (defList.size() != 0) { //defList is empty if it's not a where-expression 
    // create new where-expression
    WhereExpression whereExpr = graph.createWhereExpression();
    // add boundexpr
	IsBoundExprOf exprOf = graph.createIsBoundExprOfDefinition( expr, whereExpr);
	exprOf.setSourcePositions((createSourcePositionList(length, offset)));
	// add definitions
	for (int i = 0; i < defList.size(); i++) {
		VertexPosition def = defList.get(i);
		IsDefinitionOf isDefOf = graph.createIsDefinitionOf((Definition)def.node, whereExpr);
		isDefOf.setSourcePositions((createSourcePositionList(length, offset)));
	}
	expr = whereExpr;
}
}
;
	
	
	
	/** matches a list of definitions for let- or where expressions
	@return
*/
definitionList returns [Vector<VertexPosition> definitions = new Vector<VertexPosition>();] 
@init{
    Definition v = null;
    VertexPosition def = new VertexPosition();
    Vector<VertexPosition> defList = new Vector<VertexPosition>();
    int offset = 0;
    int length = 0;
}
	:
		{ offset = LT(1).getColumn()-1; }
		v = definition
        {
			length = -offset + LT(0).getColumn()-1 +LT(0).getText().length();
        	def.node = v;
            def.offset = offset;
            def.length = length;
            definitions.add(def);
        }
		(	(COMMA) =>
			( COMMA defList = definitionList )
            {
            	definitions.addAll(defList);
            }
			| /*empty*/
		)
	;

/** matches a definition for let- or where expressions
	@return
*/
definition returns [Definition definition = null]
@init{
	Variable var = null;
	Expression expr = null;
  	int offsetVar = 0;
  	int offsetExpr = 0;
    	int lengthVar = 0;
    	int lengthExpr = 0;
}
:
	{ offsetVar = LT(1).getColumn()-1; }
	var = variable
	{ lengthVar = -offsetVar + LT(0).getColumn()-1 +LT(0).getText().length(); }
        ASSIGN
        { offsetExpr = LT(1).getColumn()-1; }
        //  (expr = expressionOrPathDescription)
        {
            lengthExpr = -offsetExpr + LT(0).getColumn()-1 +LT(0).getText().length();
            definition = graph.createDefinition();
            IsVarOf varOf = graph.createIsVarOf(var, definition);
            varOf.setSourcePositions((createSourcePositionList(lengthVar, offsetVar)));
            IsExprOf exprOf = graph.createIsExprOf(expr, definition);
            exprOf.setSourcePositions((createSourcePositionList(lengthExpr, offsetExpr)));
        }
;
	
	
/** matches conditional expressions
@return
*/
conditionalExpression returns [Expression retVal = null]
@init{
        Expression expr = null;
	Expression trueExpr = null;
	Expression falseExpr = null;
	Expression nullExpr = null;
	int offsetExpr = 0;
	int offsetTrueExpr = 0;
	int offsetFalseExpr = 0;
	int offsetNullExpr = 0;
	int lengthExpr = 0;
	int lengthTrueExpr = 0;
	int lengthFalseExpr = 0;
	int lengthNullExpr = 0;
}
@after {
  	retVal = expr;
}
:
    { offsetExpr = LT(1).getColumn()-1; }
    // condition or expression (if it's not a real conditional expr)
    expr = orExpression
    { lengthExpr = -offsetExpr + LT(0).getColumn()-1 + LT(0).getText().length(); }
    /* optional part */
    (
    QUESTION
    { offsetTrueExpr = LT(1).getColumn()-1; }
    // expression which is evaluated if condition is true
    trueExpr = conditionalExpression
    {lengthTrueExpr = -offsetTrueExpr + LT(0).getColumn()-1 + LT(0).getText().length(); }
    COLON
    { offsetFalseExpr = LT(1).getColumn()-1; }
    // expression which is evaluated if condition is true
    falseExpr = conditionalExpression
    { lengthFalseExpr = -offsetFalseExpr + LT(0).getColumn()-1 + LT(0).getText().length(); }
    COLON
    { offsetNullExpr = LT(1).getColumn()-1; }
    // expression which is evaluated if condition is true
    nullExpr = conditionalExpression
    {
      lengthNullExpr = -offsetNullExpr + LT(0).getColumn()-1 + LT(0).getText().length();
      // create new conditional expression
      ConditionalExpression condExpr = graph.createConditionalExpression();
      // add condition
	IsConditionOf conditionOf = graph.createIsConditionOf(expr, condExpr);
	conditionOf.setSourcePositions((createSourcePositionList(lengthExpr, offsetExpr)));
	// add true-expression
	IsTrueExprOf trueExprOf = graph.createIsTrueExprOf( trueExpr, condExpr);
	trueExprOf.setSourcePositions((createSourcePositionList(lengthTrueExpr, offsetTrueExpr)));
	// add false-expression
	IsFalseExprOf falseExprOf = graph.createIsFalseExprOf( falseExpr, condExpr);
	falseExprOf.setSourcePositions((createSourcePositionList(lengthFalseExpr, offsetFalseExpr)));
	// add null-expression
	IsNullExprOf  nullExprOf = graph.createIsNullExprOf(nullExpr, condExpr);
	nullExprOf.setSourcePositions((createSourcePositionList(lengthNullExpr, offsetNullExpr)));
	expr = condExpr;
    }
  )?
;

	
orExpression returns [Expression retVal]
@init{
    Expression expr = null;
    FunctionConstruct construct = new FunctionConstruct();
}
@after {
    retVal = expr;
}
:
(  
  { construct.preArg1(); }
  expr = xorExpression
  { construct.preOp(expr); }
  OR
  { construct.postOp("or"); }
  expr = orExpression
  { expr = construct.postArg2(expr); }
)
|  (expr = xorExpression )	 
;	

	
xorExpression returns [Expression retVal]
@init{
    Expression expr = null;
    FunctionConstruct construct = new FunctionConstruct();
}
@after {
    retVal = expr;
}
:
(  
  { construct.preArg1(); }
  expr = andExpression
  { construct.preOp(expr); }
  XOR
  { construct.postOp("xor"); }
  expr = xorExpression
  { expr = construct.postArg2(expr); }
)
|  (expr = andExpression )	 
;

andExpression returns [Expression retVal]
@init{
    Expression expr = null;
    FunctionConstruct construct = new FunctionConstruct();
}
@after {
    retVal = expr;
}
:
(  
  { construct.preArg1(); }
  expr = equalityExpression
  { construct.preOp(expr); }
  AND
  { construct.postOp("and"); }
  expr = andExpression
  { expr = construct.postArg2(expr); }
)
|  (expr = equalityExpression )	 
;

equalityExpression returns [Expression retVal]
@init{
    Expression expr = null;
    FunctionConstruct construct = new FunctionConstruct();
}
@after {
    retVal = expr;
}
:
(  
  { construct.preArg1(); }
  expr = relationalExpression
  { construct.preOp(expr); }
  EQUAL
  { construct.postOp("="); }
  expr = equalityExpression
  { expr = construct.postArg2(expr); }
)
|  (expr = relationalExpression )	 
;

relationalExpression returns [Expression retVal]
@init{
    String name = null;
    Expression expr = null;
    FunctionConstruct construct = new FunctionConstruct();
}
@after {
    retVal = expr;
}
:
(  
  { construct.preArg1(); }
  expr = additiveExpression
  { construct.preOp(expr); }
  (  L_T { name = "leThan"; }
   | LE { name = "leEqual"; }
   | G_T  { name = "grThan"; }
   | GE { name = "grEqual"; }
   | MATCH {name = "match"} )
  { construct.postOp(name); }
  expr = relationalExpression
  { expr = construct.postArg2(expr); }
)
|  (expr = additiveExpression )	 
;

additiveExpression returns [Expression retVal]
@init{
    Expression expr = null;
    FunctionConstruct construct = new FunctionConstruct();
}
@after {
    retVal = expr;
}
:
(  
  { construct.preArg1(); }
  expr = multiplicativeExpression
  { construct.preOp(expr); }
  (  PLUS { construct.postOp("plus"); }
   | MINUS { construct.postOp("minus"); })
  expr = additiveExpression
  { expr = construct.postArg2(expr); }
)
|  (expr = multiplicativeExpression )	 
;


multiplicativeExpression returns [Expression retVal]
@init{
    Expression expr = null;
    FunctionConstruct construct = new FunctionConstruct();
}
@after {
    retVal = expr;
}
:
(  
  { construct.preArg1(); }
  expr = unaryExpression
  { construct.preOp(expr); }
  (  STAR { construct.postOp("times"); }
   | MOD  { construct.postOp("modulo"); }
   | DIV  { construct.postOp("dividedBy"); })
  expr = multiplicativeExpression
  { expr = construct.postArg2(expr); }
)
|  (expr = unaryExpression )	 
;

/** matches unary Expressions (-, not)
	@return
*/
unaryExpression returns [Expression retVal = null]
@init{
    Expression expr = null;
    FunctionConstruct construct = new FunctionConstruct();
}
@after {
    retVal = expr;
}
:
(  
  { construct.preUnaryOp(); }
  unaryOp = unaryOperator
  { construct.postOp(unaryOp); }
  expr = pathExpression
  { expr = construct.postArg2(expr); }
)           
| (expr = pathExpression)
;

/** matches a role-id
	@return
*/
roleId returns [RoleId role = null] 
@init{
   Identifier i = null;
}
:
i = identifier
{
   role = graph.createRoleId();
   role.setName(i.getText()); 
}
;

/** matches one of the unaryOperators '-' and 'not'
 *  @return functionId-vertex representing the operator
 */
unaryOperator returns [FunctionId retVal = null]
@init{
   String name = "uminus"; 
   FunctionId unaryOp = null;
}
@after {
   retVal = unaryOp;
}
:
( NOT { name = "not";	}  | MINUS )
{
   unaryOp = (FunctionId) functionSymbolTable.lookup(name);
   if (unaryOp == null)  {
	unaryOp = graph.createFunctionId();
	unaryOp.setName(name);
	functionSymbolTable.insert(name, unaryOp);
   }
}
;

/** matches regular and context free forward- and backvertex sets or
    pathexistences
    @return
*/
pathExpression returns [Expression retVal = null] 
@init{
        Expression expr = null;
	Expression arg1 = null;
	Expression arg2 = null;
	Expression  p = null;
	int offsetArg1 = 0;
	int lengthArg1 = 0;
}
@after {
	retVal = expr;
}
:
    /* matcht regBackwardVertexSetOrPathSystem, wenn der
     * Ausdruck mit altPathDescr beginnt und ein SMILEY oder eine
     * restrExpr folgt */
    (alternativePathDescription (SMILEY | restrictedExpression)) =>
    expr = regBackwardVertexSetOrPathSystem

    /* Ausdruck beginnt zwar mit altPathDescr, danach kommt
     * aber weder Smiley noch restrExpr --> matche also
     * pfadausdruck als primaryExpr (Knotenpaare) */
    | (alternativePathDescription) =>expr = primaryExpression

    | ( { offsetArg1 = LT(1).getColumn()-1; }
        expr = restrictedExpression
	{ lengthArg1 = -offsetArg1 + LT(0).getColumn()-1 + LT(0).getText().length(); }
	( (alternativePathDescription) =>
           expr = regPathExistenceOrForwardVertexSet//[expr, offsetArg1, lengthArg1]
	| (SMILEY) => expr = regPathOrPathSystem//[expr, offsetArg1, lengthArg1]
        )
      )  
;



/** matches restricted vertex expressions
    @return
*/
restrictedExpression returns [Expression retVal = null] 
@init{
    Expression expr = null;
    Expression restr = null;
    RestrictedExpression restrExpr = null;
    int offsetExpr = 0;
    int offsetRestr = 0;
    int lengthExpr = 0;
    int lengthRestr = 0;
}
@after {
   retVal = expr;
}
:
	{ offsetExpr = LT(1).getColumn()-1; }
	expr = valueAccess
	{ lengthExpr = -offsetExpr + LT(0).getColumn()-1 + LT(0).getText().length(); }
        (  // if followed by '&{' match this as part of this expr
           (AMP LCURLY) =>
	   (  AMP LCURLY
              { offsetRestr = LT(1).getColumn()-1; }
              restr = expression
 	      { lengthRestr = -offsetRestr + LT(0).getColumn()-1 + LT(0).getText().length(); }
              RCURLY
              {
                 restrExpr = graph.createRestrictedExpression();
	         // add expression
	         IsRestrictedExprOf restrExprOf = graph.createIsRestrictedExprOf(expr, restrExpr);
	         restrExprOf.setSourcePositions((createSourcePositionList(lengthExpr, offsetExpr)));
		 // add restriction
	         IsRestrictionOf restrOf = graph.createIsRestrictionOf(restr, restrExpr);
	         restrOf.setSourcePositions((createSourcePositionList(lengthRestr, offsetRestr)));
		 expr = restrExpr;
              }
            )
	    | /*empty*/
	)
;

identifier returns [Identifier retVal]
@init{
    Identifier expr = null;
}
@after {
	retVal = expr;
}
: i=IDENT 
{  expr = graph.createIdentifier();
   expr.setName(i.getText());
};


valueAccess returns [Expression retVal]
@init{
    Expression expr = null;
    FunctionConstruct construct = new FunctionConstruct();
}
@after {
    retVal = expr;
}
:
(  
  { construct.preArg1(); }
  expr = primaryExpression
  { construct.preOp(expr); }
  (  (DOT { construct.postOp("getValue"); }
     expr = identifier)
  | (LBRACK 
     { construct.postOp("nthElement");}
     expr = expression 	//TODO: dbildh, 20.11.08 primaryExpression?
     RBRACK)
  )
  { expr = construct.postArg2(expr); }
)
// |  (expr = primaryExpression )  //TODO: dbildh, 20.11.08 primaryExpression?
;

/** matches primary expressions (elementset, literal,
	valueConstruction, functionAppl., subgraph, simpleQuery, cfGrammar, variable)
*/
primaryExpression returns [Expression retVal = null] 
@init {
	Expression expr = null;
}
@after {
	retVal = expr;
}
:
(( LPAREN expr = expression RPAREN )
/*|	expr = rangeExpression  TODO: dbildh, 20.11.08: uncomment
|	expr = alternativePathDescription
|	expr = literal
|	expr = valueConstruction
| 	expr = functionApplication
| 	expr = graphRangeExpression
|	expr = simpleQuery*/
| 	expr = variable	)
;

/** matches a pathdescription
*/
pathDescription returns [PathDescription retVal = null]
@init {
	Expression pathDescr = null;
}
@after {
	retVal = pathDescr;
}
:
pathDescr = alternativePathDescription
;


/** matches an alternative pathdescription
	@return
*/
alternativePathDescription returns [PathDescription retVal = null] 
@init {
	PathDescription pathDescr = null;
	int offsetPathDescr = 0;
	int lengthPathDescr = 0;
	PathDescription part1 = null;
	int offsetPart1 = 0;
	int lengthPart1 = 0;
	PathDescription part2= null;
	int offsetPart2 = 0;
	int lengthPart2 = 0;
}
@after {
	if (pathDescr != null)
		retVal = pathDescr;
	else 
		retVal = part1;	
}
:
  {offsetPart1 = getLTOffset(); }
  part1 = intermediateVertexPathDescription
  {lengthPart1 = getLTLength();}
( BOR
  {offsetPart2 = getLTOffset(); }
  part2 = intermediateVertexPathDescription
  {
	addPathElement(AlternativePathDescription.class, IsAlternativePathOf.class, pathDescr, part1, part2);
  })*		
;

	   
	   
intermediateVertexPathDescription returns [PathDescription retVal = null] 
@init {
	PathDescription pathDescr = null;
	int offsetPathDescr = 0;
	int lengthPathDescr = 0;
	PathDescription part1 = null;
	int offsetPart1 = 0;
	int lengthPart1 = 0;
	PathDescription part2= null;
	int offsetPart2 = 0;
	int lengthPart2 = 0;
	Expression restrExpr = null;
	int offsetExpr = 0;
	int lengthExpr = 0;
}
@after {
	if (pathDescr != null)
		retVal = pathDescr;
	else 
		retVal = part1;	
}
:
  {offsetPart1 = getLTOffset(); }
  part1 = sequentialPathDescription
  {lengthPart1 = getLTLength();}
( {offsetExpr = getLTOffset(); }
  restrExpr = restrictedExpression
  {
   	lengthExpr = getLTLength();
  	offsetPart2 = getLTOffset(); 
  }
  part2 = intermediateVertexPathDescription
  {
	addPathElement(IntermediateVertexPathDescription.class, IsSubPathOf.class, pathDescr, part1, part2);
	IsIntermediateVertexOf intermediateVertexOf = graph.createIsIntermediateVertexOf(restrExpr, vpd);
	intermediateVertexOf.setSourcePositions((createSourcePositionList(lengthExpr, offsetExpr )));
  })		
;


sequentialPathDescription returns [PathDescription retVal = null] 
@init {
	PathDescription pathDescr = null;
	int offsetPathDescr = 0;
	int lengthPathDescr = 0;
	PathDescription part1 = null;
	int offsetPart1 = 0;
	int lengthPart1 = 0;
	PathDescription part2= null;
	int offsetPart2 = 0;
	int lengthPart2 = 0;
}
@after {
	if (pathDescr != null)
		retVal = pathDescr;
	else 
		retVal = part1;	
}
:
  {offsetPart1 = getLTOffset(); }
  part1 = startRestrictedPathDescription
  {lengthPart1 = getLTLength();}
( {offsetPart2 = getLTOffset(); }
  part2 = startRestrictedPathDescription
  {
	addPathElement(AlternativePathDescription.class, IsAlternativePathOf.class, pathDescr, part1, part2);
/*				(iteratedOrTransposedPathDescription) =>
			pathDescr = sequentialPathDescription2[seqPathDescr, offsetSeq1,
					-offsetSeq1 +LT(0).getColumn()-1 + LT(0).getText().length()] 
					TODO dbildh 20.11.08 : Check for what this should be good
					*/
  })*		
;


startRestrictedPathDescription returns [PathDescription pathDescr = null] 
@init{
	Vector<VertexPosition> typeIds = new Vector<VertexPosition>();
	Expression expr = null;
	int offset = 0;
	int length = 0;
}	:
( LCURLY
  ( 
    ( typeId) =>typeIds = typeExpressionList
      | { offset = getLTOffset(); }
	    expr = expression
	    { length = getLTLength(); }
	)
  RCURLY
  AMP
)?
pathDescr = goalRestrictedPathDescription
{
  	if (expr != null) {
		IsStartRestrOf startRestrOf = graph.createIsStartRestrOf(expr, pathDescr);
		startRestrOf.setSourcePositions((createSourcePositionList(length, offset)));
	} else {
		for (int i = 0; i < typeIds.size(); i++) {
			VertexPosition t = typeIds.get(i);
			IsStartRestrOf startRestrOf = graph.createIsStartRestrOf((Expression)t.node, pathDescr);
			startRestrOf.setSourcePositions((createSourcePositionList(t.length, t.offset)));
		}
	}
}
;	   
	
goalRestrictedPathDescription returns [PathDescription pathDescr = null] 
@init{
	Vector<VertexPosition> typeIds = new Vector<VertexPosition>();
	Expression expr = null;
	int offset = 0;
	int length = 0;
}	
:
pathDescr = iteratedOrTransposedPathDescription
((AMP) => (	AMP
	LCURLY
	( (typeId) =>typeIds = typeExpressionList
		{
           	for (int i = 0; i < typeIds.size(); i++) {
				VertexPosition t = typeIds.get(i);
    			IsGoalRestrOf goalRestrOf = graph.createIsGoalRestrOf((Expression)t.node, pathDescr);
    			goalRestrOf.setSourcePositions((createSourcePositionList(t.length, t.offset)));
    		}
   	 	}
   	  | (
   	      { offset = getLTOffset(); }
		  expr = expression
		  {
			 length = getLTLength();
   			 IsGoalRestrOf goalRestrOf = graph.createIsGoalRestrOf(expr, pathDescr);
    		 goalRestrOf.setSourcePositions((createSourcePositionList(length, offset)));
    	  }
		) // ende expr
	)// ende nachLcurly
	RCURLY
)|  /* empty (no AMP)*/
)
;


iteratedOrTransposedPathDescription	returns [PathDescription retVal = null]
@init{
   	String iteration = null;
 	int offsetPath = 0;
 	int lengthPath = 0;
 	PathDescription pathDescr = null;
}
@after {
	retVal = pathDescr;
}
:
{ offsetPath = getLTOffset();}
pathDescr = primaryPathDescription
{ lengthPath = getLTLength();}
(	
  	( STAR { iteration = "star"; } | PLUS {iteration ="plus"; )
      {
		IteratedPathDescription ipd = graph.createIteratedPathDescription();
	    ((IteratedPathDescription)ipd).setTimes(times);
	    IsIteratedPathOf iteratedPathOf = graph.createIsIteratedPathOf(pathDesc, ipd);
	    iteratedPathOf.setSourcePositions((createSourcePositionList(lengthPath, offsetPath)));
	    pathDescr = ipd;
      }
|	( CARET
		(	T  // transponatedPath:
           	{
				TransposedPathDescription tpd = graph.createTransposedPathDescription();
                IsTransposedPathOf transposedPathOf = graph.createIsTransposedPathOf(pathDescr, tpd);
	            transposedPathOf.setSourcePositions((createSourcePositionList(lengthPath, offsetPath)));
	            pathDescr = tpd;
            }
        | (// exponentedPath:
            { offsetExpr = getLTOffset(); }
            i=NUM_INT
            { lengthExpr = getLTOffset();
			 	ExponentiatedPathDescription epd = graph.createExponentiatedPathDescription();
		        IsExponentiatedPathOf exponentiatedPathOf = graph.createIsExponentiatedPathOf(path, epd);
	            exponentiatedPathOf.setSourcePositions((createSourcePositionList(lengthPath, offsetPath)));
	            IntLiteral exponent = graph.createIntLiteral();
				exponent.setIntValue(Integer.parseInt(i.getText()));
	            IsExponentOf exponentOf = graph.createIsExponentOf(exponent, epd);
	            exponentOf.setSourcePositions((createSourcePositionList(lengthExpr, offsetExpr)));
	            pathDescr = epd;
            }
          )
		)
	)
)*
;



primaryPathDescription returns [PathDescription pathDescr = null]
@init{
	int offset = 0;
	int length = 0;
}	
:
(
    ( pathDescr = simplePathDescription
    | pathDescr = edgePathDescription
    )
)
| ( LPAREN pathDescr = pathDescription RPAREN )
| ( LBRACK
    { offset = getLTOffset(); }
    pathDescr = pathDescription
    { length = getLTLength(); }
    RBRACK
    {
	    OptionalPathDescription optPathDescr = graph.createOptionalPathDescription();
		IsOptionalPathOf optionalPathOf = graph.createIsOptionalPathOf(pathDescr, optPathDescr);
		optionalPathOf.setSourcePositions((createSourcePositionList(length, offset)));
	    pathDescr = optPathDescr;
    }
)
;





regBackwardVertexSetOrPathSystem returns [PathDescription retVal = null]
@init {
	Expression pathDescr = null;
}
@after {
	retVal = pathDescr;
}
:
pathDescr = 'BACKWARDVERTEXSET'// alternativePathDescription
;

regPathExistenceOrForwardVertexSet returns [PathDescription retVal = null]
@init {
	Expression pathDescr = null;
}
@after {
	retVal = pathDescr;
}
:
pathDescr = 'FORWARDVERTEXSET'// alternativePathDescription
;

regPathOrPathSystem returns [PathDescription retVal = null]
@init {
	Expression pathDescr = null;
}
@after {
	retVal = pathDescr;
}
:
pathDescr = 'REGPATH'// alternativePathDescription
;
