grammar Greql2;
options {backtrack=true; memoize=true;}

tokens {
	FUNCTIONID;
	THISVERTEX;
	THISEDGE;
}

@lexer::members {
  protected boolean enumIsKeyword = true;
  protected boolean assertIsKeyword = true;
}


@header {
package de.uni_koblenz.jgralab.greql2.parser;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Logger;

import org.antlr.runtime.RecognitionException;

import antlr.TokenStreamException;
import de.uni_koblenz.jgralab.*;
import de.uni_koblenz.jgralab.greql2.exception.*;
import de.uni_koblenz.jgralab.greql2.funlib.Greql2FunctionLibrary;
import de.uni_koblenz.jgralab.greql2.schema.*;
import de.uni_koblenz.jgralab.greql2.schema.impl.*;
import de.uni_koblenz.jgralab.schema.*;
}

@lexer::header {
package de.uni_koblenz.jgralab.greql2.parser;
import java.util.LinkedList;
import java.util.logging.Logger;
import org.antlr.runtime.RecognitionException;
import antlr.TokenStreamException;
import de.uni_koblenz.jgralab.*;
import de.uni_koblenz.jgralab.greql2.exception.*;
import de.uni_koblenz.jgralab.greql2.funlib.Greql2FunctionLibrary;
import de.uni_koblenz.jgralab.greql2.schema.*;
import de.uni_koblenz.jgralab.greql2.schema.impl.*;
import de.uni_koblenz.jgralab.schema.*;
}

@members {

    private static Logger logger = Logger.getLogger(Greql2Parser.class.getName());
    private final int VMAX = 100;
    private final int EMAX = 100;
    private Greql2Schema schema = null;
    private Greql2 graph = null;
    private SymbolTable variableSymbolTable = null;
    private SymbolTable functionSymbolTable = null;
    // private GraphClass graphClass = null;
    // private boolean isAdditiveExpression = true;

    class FunctionConstruct {
    	String operatorName;  
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
        	lengthArg1 = getLTLength(offsetArg1);
        	offsetOperator = getLTOffset();
        }
        
        public void postOp(String op) {
          	lengthOperator = getLTLength(offsetOperator);
	  		offsetArg2 = getLTOffset();
	  		operatorName = op;
        }
        
        public FunctionApplication postArg2(Expression arg2) { 
         	lengthArg2 = getLTLength(offsetArg2);
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

    private FunctionApplication createFunctionIdAndArgumentOf(FunctionId functionId, int offsetOperator, int lengthOperator, Expression arg1, int offsetArg1, int lengthArg1, Expression arg2, int offsetArg2, int lengthArg2, boolean binary) {
    	FunctionApplication fa = graph.createFunctionApplication();
    	IsFunctionIdOf functionIdOf = graph.createIsFunctionIdOf(functionId, fa);
    	functionIdOf.setSourcePositions((createSourcePositionList(lengthOperator, offsetOperator)));
    	IsArgumentOf arg1Of = graph.createIsArgumentOf(arg1, fa);
    	arg1Of.setSourcePositions((createSourcePositionList(lengthArg1, offsetArg1)));
    	if (binary) {
      	   IsArgumentOf arg2Of = graph.createIsArgumentOf(arg2, fa);
    	   arg2Of.setSourcePositions((createSourcePositionList(lengthArg2, offsetArg2)));
    	}  
    	return fa;
    }	
    
    private List<SourcePosition> createSourcePositionList(int length, int offset) {
    	List<SourcePosition> list = new ArrayList<SourcePosition>();
    	list.add(new SourcePosition(length, offset));
    	return list;
    }


	private Vertex createPartsOfValueConstruction(List<VertexPosition> expressions, ValueConstruction parent) {
		return createMultipleEdgesToParent(expressions, parent, IsPartOf.class);
	}
	
	private Vertex createMultipleEdgesToParent(List<VertexPosition> expressions, Vertex parent, Class<? extends Edge> edgeClass) {
       	for (VertexPosition expr : expressions) {
			Greql2Aggregation edge = (Greql2Aggregation) graph.createEdge(edgeClass, (Expression)expr.node, parent);
			edge.setSourcePositions((createSourcePositionList(expr.length, expr.offset)));
		}
		return parent;
	}


	public void addPathElement(Class<? extends PathDescription> vc, Class<? extends Edge> ec, PathDescription pathDescr, PathDescription part1, int offsetPart1, int lengthPart1, PathDescription part2, int offsetPart2, int lengthPart2) {
	 	Greql2Aggregation edge = null;
	 	if (pathDescr == null) {
	 		pathDescr = graph.createVertex(vc);
	 		edge =  (Greql2Aggregation) graph.createEdge(ec, part1, pathDescr);
	 		edge.setSourcePositions((createSourcePositionList(lengthPart1, offsetPart1)));
	 	}
		edge = (Greql2Aggregation) graph.createEdge(ec, part2, pathDescr);
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
		//nonterminalSymbolTable = new SymbolTable();
		functionSymbolTable.blockBegin();
        //graphClass = schema.getGraphClass(new QualifiedName("Greql2"));
   }
   
   private int getLTLength(int offset) {
		return (- offset + LT(0).getColumn()-1 + LT(0).getText().length());
   }
   
   private int getLTOffset() {
		return LT(1).getColumn()-1; 
   }
   
    /**
     *	@see antlr.Parser#reportError(RecognitionException)
     */
  	public void reportError(RecognitionException e) {
		int offset = -1;
		offset = getLTOffset();
		if (offset != -1)
				logger.severe("error: " + offset +": " + e.getMessage());
			else logger.severe("error (offset = -1): " + e.getMessage());
	}

  	public void reportError(TokenStreamException e)
 	{
		int offset = -1;
		offset = getLTOffset();
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
AS 			: 'as';
BAG 		: 'bag';
E 			: 'E';
ESUBGRAPH	: 'eSubgraph';
EXISTS_ONE	: 'exists!';
EXISTS		: 'exists';
END 		: 'end';
FORALL		: 'forall';
FROM  		: 'from';
IN 			: 'in';
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
V 			: 'V';
VSUBGRAPH	: 'vSubgraph';
WHERE 		: 'where';
WITH 		: 'with';
QUESTION 	: '?';
EXCL 		: '!';
COLON 		: ':';
COMMA 		: ',';
DOT			: '.';
DOTDOT		: '..';
AT			: '@';
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
LE			: '<=';
GE			: '=>';
L_T			: '<';
G_T			: '>';
DIV			: '/';
PLUS		: '+';
MINUS		: '-';
STAR		: '*';
MOD			: '%';
SEMI		: ';';
CARET		: '^';
BOR			: '|';
AMP			: '&';	
SMILEY		: ':-)';
EDGESTART	: '<-';
EDGEEND		: '->';
EDGE		: '--';
RARROW		: '-->';
LARROW  	: '<--';
ARROW		: '<->';
HASH 		: '#';
OUTAGGREGATION	: '<>--';
INAGGREGATION   : '--<>';
PATHSYSTEMSTART : '-<';	 	
	
//WHitespace
WS  :  (' '|'\r'|'\t'|'\u000C'|'\n')* 
	{ setType(Token.SKIP_TOKEN); }
;

// Single-line comments
SL_COMMENT
	:	'//'
		(~('\n'|'\r'))* ('\n'|'\r'('\n')?)?
		{setType(Token.SKIP_TOKEN);}
	;  
	    

// multiple-line comments
ML_COMMENT	
@init{ 
	int start = getColumn()-1;
}
:	'/*'	     
( 		
	//options { generateAmbigWarnings=false; 	}
	{ LA(1) != EOF_CHAR && LA(2)!='/' }? '*'			
			| { LA(1) != EOF_CHAR }? ('/' '*') => ML_COMMENT				    
			| { LA(1) != EOF_CHAR }? ~('*')
			| { LA(1) == EOF_CHAR }? { throw new TokenStreamException("Unterminated /*-comment starting at offset " + start); }
		
)*
		
'*/'
	{setType(Token.SKIP_TOKEN);}
;

		
// string literals
STRING_LITERAL //options {  paraphrase = "a string literal";}
@init{
	int start = getColumn()-1; 
}	
	:	
		'"' 
		( //	options { generateAmbigWarnings=false; 	}
			:	
			  ESCAPE_SEQUENCE	          
	      |  {LA(1) != EOF_CHAR}? ~( '"' | '\\' | '\n' | '\r' )	   
	      |  { LA(1) == EOF_CHAR }? { throw new TokenStreamException("Unterminated string-literal starting at offset "+start); }     
	    )* 
	    '"'
	;


HEXLITERAL : '0' ('x'|'X') HEXDIGIT+ IntegerTypeSuffix? ;

DECLITERAL : ('0' | '1'..'9' '0'..'9'*) IntegerTypeSuffix? ;

OCTLITERAL : '0' ('0'..'7')+ IntegerTypeSuffix? ;

fragment
HEXDIGIT : ('0'..'9'|'a'..'f'|'A'..'F') ;

	
fragment
IntegerTypeSuffix : ('l'|'L') ;

	
FLOAT_LITERAL
    :   ('0'..'9')+ '.' ('0'..'9')* Exponent? FloatTypeSuffix?
    |   '.' ('0'..'9')+ Exponent? FloatTypeSuffix?
    |   ('0'..'9')+ Exponent FloatTypeSuffix?
    |   ('0'..'9')+ FloatTypeSuffix
    ;

fragment
Exponent : ('e'|'E') ('+'|'-')? ('0'..'9')+ ;

fragment
FloatTypeSuffix : ('f'|'F'|'d'|'D') ;


fragment
ESCAPE_SEQUENCE
    :   '\\' ('b'|'t'|'n'|'f'|'r'|'\"'|'\''|'\\')
    |   OctalEscape
    ;

fragment
OctalEscape
    :   '\\' ('0'..'3') ('0'..'7') ('0'..'7')
    |   '\\' ('0'..'7') ('0'..'7')
    |   '\\' ('0'..'7')
    ;



// a couple protected methods to assist in matching floating point numbers
protected
EXPONENT
	:	
		('e'|'E') ('+'|'-')? ('0'..'9')+
	;	

// an identifier.  Note that testLiterals is set to true!  This means
// that after we match the rule, we look in the literals table to see
// if it's a literal or really an identifer
IDENT
/*	options {  
		testLiterals=true;
	}	*/
	:	
		(('a'..'z'|'A'..'Z') ('a'..'z'|'A'..'Z'|'_'|'0'..'9')*
		{			
			if (getText().equals("thisEdge")) 
				{_ttype = THISEDGE;}
			else if (getText().equals("thisVertex"))
				{_ttype = THISVERTEX;}
			else if (isFunctionName(getText()))
				{_ttype = FUNCTIONID;} 		
				
		})
	;



variableList returns [ArrayList<VertexPosition> variables = new ArrayList<VertexPosition>()] 
@init{
    Variable var = null;
    ArrayList<VertexPosition> list = null;
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
  { variables.addAll(list); }
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
        	var.setName(getText());
        }
;

greqlExpression 
@init{
	Expression expr = null;
	Token id = null;
	ArrayList<VertexPosition> varList = new ArrayList<VertexPosition>();
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
	length = getLTLength(offset);
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
  tempExpression = letExpression
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
	ArrayList<VertexPosition> defList = new ArrayList<VertexPosition>();
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
	ArrayList<VertexPosition> defList = new ArrayList<VertexPosition>();
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
definitionList returns [ArrayList<VertexPosition> definitions = new ArrayList<VertexPosition>()] 
@init{
    Definition v = null;
    VertexPosition def = new VertexPosition();
    ArrayList<VertexPosition> defList = new ArrayList<VertexPosition>();
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
  { construct.preArg1(); }
  expr = xorExpression
  { construct.preOp(expr); }
((OR) =>  (
  OR
  { construct.postOp("or"); }
  expr = orExpression
  { expr = construct.postArg2(expr); }
) | /* only xorExpression as fake orExpression */) 
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
((XOR) =>  (
  XOR
  { construct.postOp("xor"); }
  expr = xorExpression
  { expr = construct.postArg2(expr); }
) | /* only andExpression as fake xorExpression */)
) 
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
((AND) =>  (
  AND
  { construct.postOp("and"); }
  expr = andExpression
  { expr = construct.postArg2(expr); }
) | /* only equalityExpression as fake andExpression */) )
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
((EQUAL) =>  (
  EQUAL
  { construct.postOp("="); }
  expr = equalityExpression
  { expr = construct.postArg2(expr); }
) | /* only xoRExpression as fake orExpression */) )
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
  { construct.preArg1(); }
  expr = additiveExpression
  { construct.preOp(expr); }
 ((L_T | LE | G_T | GE | MATCH) => 
  ( (  L_T { name = "leThan"; }
    | LE { name = "leEqual"; }
    | G_T  { name = "grThan"; }
    | GE { name = "grEqual"; }
    | MATCH {name = "match"} ) 
    { construct.postOp(name); }
    expr = relationalExpression
    { expr = construct.postArg2(expr); }
  )    | )
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
 { construct.preArg1(); }
  expr = multiplicativeExpression
  { construct.preOp(expr); }
  ((PLUS | MINUS) => 
  ( (  PLUS { construct.postOp("plus"); }
     | MINUS { construct.postOp("minus"); })
    expr = additiveExpression
    { expr = construct.postArg2(expr); }
  ) | ) 	 
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
  
  { construct.preArg1(); }
  expr = unaryExpression
  { construct.preOp(expr); }
  ( (STAR | MOD | DIV) => 
    (( STAR { construct.postOp("times"); }
     | MOD  { construct.postOp("modulo"); }
     | DIV  { construct.postOp("dividedBy"); })
     expr = multiplicativeExpression
     { expr = construct.postArg2(expr); }
    )
| )	 
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
           expr = regPathExistenceOrForwardVertexSet[expr, offsetArg1, lengthArg1]
	| (SMILEY) => expr = regPathOrPathSystem[expr, offsetArg1, lengthArg1]
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
  {lengthPart1 = getLTLength(offsetPart1);}
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
  {lengthPart1 = getLTLength(offsetPart1);}
( {offsetExpr = getLTOffset(); }
  restrExpr = restrictedExpression
  {
   	lengthExpr = getLTLength(offsetExpr);
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
  {lengthPart1 = getLTLength(offsetPart1);}
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


startRestrictedPathDescription returns [PathDescription retVal = null] 
@init{
	ArrayList<VertexPosition> typeIds = new ArrayList<VertexPosition>();
	Expression expr = null;
	int offset = 0;
	int length = 0;
	PathDescription pathDescr = null;
}
@after {
	retVal = pathDescr;
}
:
( LCURLY
  ( 
      (typeId) =>typeIds = typeExpressionList
      | { offset = getLTOffset(); }
	    expr = expression
	    { length = getLTLength(offset); }
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
		for (VertexPosition t : typeIds) {
			IsStartRestrOf startRestrOf = graph.createIsStartRestrOf((Expression)t.node, pathDescr);
			startRestrOf.setSourcePositions((createSourcePositionList(t.length, t.offset)));
		}
	}
}
;	   
	
goalRestrictedPathDescription returns [PathDescription retVal = null] 
@init{
	ArrayList<VertexPosition> typeIds = new ArrayList<VertexPosition>();
	Expression expr = null;
	int offset = 0;
	int length = 0;
	PathDescription pathDescr = null;
}
@after {
	retVal = pathDescr;
}
:
pathDescr = iteratedOrTransposedPathDescription
((AMP) => (	AMP
	LCURLY
	( ((typeId) =>typeIds = typeExpressionList
		{
           	for (int i = 0; i < typeIds.size(); i++) {
				VertexPosition t = typeIds.get(i);
    			IsGoalRestrOf goalRestrOf = graph.createIsGoalRestrOf((Expression)t.node, pathDescr);
    			goalRestrOf.setSourcePositions((createSourcePositionList(t.length, t.offset)));
    		}
   	 	}
   	  ) | (
   	      { offset = getLTOffset(); }
		  expr = expression
		  {
			 length = getLTLength(offset);
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
{ lengthPath = getLTLength(offsetPath);}
(	
  	( STAR { iteration = "star"; } | PLUS {iteration ="plus";} )
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
            i=DECLITERAL
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






primaryPathDescription returns [PathDescription retVal = null]
@init{
	int offset = 0;
	int length = 0;
	PathDescription pathDescr = null;
}
@after {
	retVal = pathDescr;
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
    { length = getLTLength(offset); }
    RBRACK
    {
	    OptionalPathDescription optPathDescr = graph.createOptionalPathDescription();
		IsOptionalPathOf optionalPathOf = graph.createIsOptionalPathOf(pathDescr, optPathDescr);
		optionalPathOf.setSourcePositions((createSourcePositionList(length, offset)));
	    pathDescr = optPathDescr;
    }
)
;


/** matches a simle pathdescription consisting of an arrow simple
	and eventually a restriction. "thisEdge"s are replaced by
	the corresponding simple pathdescription
	@return
*/
simplePathDescription returns [PrimaryPathDescription retVal = null]
@init{
	ArrayList<VertexPosition> typeIds = new ArrayList<VertexPosition>();
    Direction dir;
    String direction = "any";
    int offsetDir = 0;
	PathDescription pathDescr = null;
}
@after {
	retVal = pathDescr;
}
:
{offsetDir = getLTOffset();}
/* edge symbol */
/* TODO: insert here for aggregation */
( RARROW { direction = "out"; }
| LARROW { direction = "in"; }
| ARROW
)
/* edge type restriction */
(   (LCURLY (edgeRestrictionList)? RCURLY ) =>
      (LCURLY (typeIds = edgeRestrictionList)?	RCURLY)
| /* empty */    )

{
    pathDescr = graph.createSimplePathDescription();
	dir = (Direction)graph.getFirstVertexOfClass(Direction.class);
	while (dir != null ) {
    	if (!dir.getDirValue().equals(direction)) {
	        dir = (Direction)dir.getNextVertexOfClass(directionVertexClass);
	    } else { 
	    	break;
	    }		
		if (dir == null) {
			dir = graph.createDirection();
	        dir.setDirValue(direction);
	    }
	    IsDirectionOf directionOf = graph.createIsDirectionOf(dir, pathDescr);
	    directionOf.setSourcePositions((createSourcePositionList(0, offsetDir)));
	    for (VertexPosition t : typeIds) {
			IsEdgeRestrOf edgeRestrOf = graph.createIsEdgeRestrOf((EdgeRestriction)t.node, pathDescr);
			edgeRestrOf.setSourcePositions((createSourcePositionList(t.length, t.offset)));
		}
	}	
}
;



/** matches a edgePathDescription, i.e. am edge as part of a pathdescription
	@return
*/
edgePathDescription returns [EdgePathDescription pathDescr = null] 
@init{
	Expression expr = null;
	Direction dir = null;
    boolean edgeStart = false;
    boolean edgeEnd = false;
    String direction = "any";
    int offsetDir = 0;
    int offsetExpr = 0;
    int lengthDir = 0;
    int lengthExpr = 0;
}
:	
{offsetDir = LT(1).getColumn()-1;}
/* TODO: insert here for aggregation */
(EDGESTART	{ edgeStart = true; } | EDGE)
{offsetExpr = LT(1).getColumn()-1;}
expr = expression
{lengthExpr = getLTLength(offsetExpr);}
(EDGEEND { edgeEnd = true; }| EDGE)
{
	lengthExpr = getLTLength(offsetExpr);
    pathDescr = graph.createEdgePathDescription();
	if (edgeStart && !edgeEnd) 
		direction = "in";
	else if  (!edgeStart  && edgeEnd))
        direction = "out";
	dir = (Direction)graph.getFirstVertexOfClass(Direction.class);
	while (dir != null ) {
   		if (! dir.getDirValue().equals(direction)) {
       			dir = (Direction)dir.getNextVertexOfClass(directionVertexClass);
	    } else {
	     	break;
	    }	
		if (dir == null) {
			dir = graph.createDirection();
	        dir.setDirValue(direction);
	    }
	    IsDirectionOf directionOf = graph.createIsDirectionOf(dir, pathDescr);
	    directionOf.setSourcePositions((createSourcePositionList(lengthDir, offsetDir)));
	    IsEdgeExprOf edgeExprOf = graph.createIsEdgeExprOf(expr, pathDescr);
	    edgeExprOf.setSourcePositions((createSourcePositionList(lengthExpr, offsetExpr)));
	}
}	
;


attributeId returns [AttributeId expr = null]
:
i=IDENT
{
	expr = graph.createAttributeId();
	expr.setName(i.getText());
}
;


/**	matches a function application
*/
functionApplication returns [FunctionApplication expr = null]
@init{
	ArrayList<VertexPosition> typeIds = new ArrayList<VertexPosition>();
	ArrayList<VertexPosition> expressions = new ArrayList<VertexPosition>();
    FunctionId functionId = null;
}
:
f=FUNCTIONID
(LCURLY	(typeIds = typeExpressionList)? RCURLY )?
LPAREN (expressions = expressionList)? RPAREN
{
    expr = graph.createFunctionApplication();
    // retrieve function id or create a new one
    functionId = (FunctionId) functionSymbolTable.lookup(f.getText());
    if (functionId == null) {
    	functionId = graph.createFunctionId();
		functionId.setName(f.getText());
    	functionSymbolTable.insert(f.getText(), functionId);
    }
	IsFunctionIdOf  functionIdOf = graph.createIsFunctionIdOf(functionId, expr);
    functionIdOf.setSourcePositions((createSourcePositionList(f.getColumn()-1, f.getText().length())));
    for (int i = 0; i < typeIds.size(); i++) {
		VertexPosition t = typeIds.get(i);
		IsTypeExprOf typeOf = graph.createIsTypeExprOfFunction((Expression)t.node, expr);
		typeOf.setSourcePositions((createSourcePositionList(t.length, t.offset)));
	}
	for (int i = 0; i < expressions.size(); i++) {
		VertexPosition ex = expressions.get(i);
		IsArgumentOf argOf = graph.createIsArgumentOf((Expression)ex.node,expr);
		argOf.setSourcePositions((createSourcePositionList(ex.length, ex.offset)));
	}
}
;



/** matches a construction of a complex value, i.e. one of<br>
	- bag<br>
	- path<br>
	- pathsystem<br>
	- record<br>
	- set<br>
	- list<br>
	- tuple<br>
	@return
*/
valueConstruction returns [Expression retVal = null] 
@init {
	Expression expr = null;
}
@after {
	retVal = expr;
}
	:
		expr = bagConstruction
		|	expr = listConstruction
		|	expr = pathConstruction
		|	expr = pathsystemConstruction
		|	expr = recordConstruction
		|	expr = setConstruction
		|	expr = tupleConstruction
	;


/**	matches a bag construction
*/
bagConstruction returns [BagConstruction bagConstr = null]
@init{
	ArrayList<VertexPosition> expressions = new ArrayList<VertexPosition>();
}
:
	BAG
	LPAREN ( expressions = expressionList )? RPAREN
    {createPartsOfValueConstruction(expressions, graph.createBagConstruction()); }
;
	
setConstruction returns [BagConstruction bagConstr = null]
@init{
	ArrayList<VertexPosition> expressions = new ArrayList<VertexPosition>();
}
:
	SET
	LPAREN ( expressions = expressionList )? RPAREN
    {createPartsOfValueConstruction(expressions, graph.createSetConstruction()); }
;

/** matches a tupel construction
*/
tupleConstruction returns [TupleConstruction tupConstr = null]
@init{
	ArrayList<VertexPosition> expressions = new ArrayList<VertexPosition>();
}	:
	TUP
	LPAREN
	expressions = expressionList
	RPAREN
    {createPartsOfValueConstruction(expressions, graph.createTupleConstruction()); }
 ;


listConstruction returns [ListConstruction retVal = null]
@init{
	ArrayList<VertexPosition> expressions = new ArrayList<VertexPosition>();
	ListConstruction listConstr = null;
}
@after {
	retVal = listConstr;
}
:
LIST
LPAREN
(
   	(expression DOTDOT) => listConstr = listRangeExpression
    | (	expressions = expressionList
        {createPartsOfValueConstruction(expressions, graph.createListConstruction()); }
      )?
)
RPAREN
;


listRangeExpression returns [ListRangeConstruction expr = null] 
@init{
	Expression startExpr = null;
	Expression endExpr = null;
 	int offsetStart = 0;
 	int offsetEnd = 0;
 	int lengthStart = 0;
 	int lengthEnd = 0;
}
:
{ offsetStart = getLTOffset(); }
startExpr = expression
{ lengthStart = getLTLength(offsetStart);}
DOTDOT
{ offsetEnd = getLTOffset(); }
endExpr = expression
{
   lengthEnd = getLTLength(offsetEnd);
   expr = graph.createListRangeConstruction();
   IsFirstValueOf firstValueOf = graph.createIsFirstValueOf(startExpr, expr);
   firstValueOf.setSourcePositions((createSourcePositionList(lengthStart, offsetStart)));
   IsLastValueOf lastValueOf = graph.createIsLastValueOf(endExpr, expr);
   lastValueOf.setSourcePositions((createSourcePositionList(lengthEnd, offsetEnd)));
}
;


recordConstruction returns [RecordConstruction recConstr = null]
@init{
	ArrayList<VertexPosition> elements = new ArrayList<VertexPosition>();
}
	:
	REC
	LPAREN
	elements = recordElementList
	RPAREN
    {
		recConstr = graph.createRecordConstruction();
		for (VertexPosition expr : elements) {
			IsRecordElementOf exprOf = graph.createIsRecordElementOf((RecordElement)expr.node, recConstr);
			exprOf.setSourcePositions((createSourcePositionList(expr.length, expr.offset)));
		}
    }
;



recordElementList returns [ArrayList<VertexPosition> elements = new ArrayList<VertexPosition>()]
@init{
	RecordElement v = null;
	ArrayList<VertexPosition> list = null;
    VertexPosition recElement = new VertexPosition();
}
:   { recElement.offset = getLTOffset(); }
	v = recordElement
    {
        recElement.length = getLTLength(recElement.offset);
        recElement.node = v;
        elements.add(recElement);
    }
	(COMMA list = recordElementList {elements.addAll(list);} )?
;



recordElement returns [RecordElement recElement = null]
@init{
	RecordId recId = null;
	Expression expr = null;
    int offsetRecId = 0;
    int offsetExpr = 0;
    int lengthRecId = 0;
    int lengthExpr = 0;
}	
:
{ offsetRecId = getLTOffset(); }
recId = recordId
{ lengthRecId = getLTLength(offsetRecId); }
COLON
{ offsetExpr =getLTOffset(); }
expr = expression
{
  	lengthExpr = getLTLength(offsetExpr);
    recElement = graph.createRecordElement();
   	IsRecordIdOf recIdOf = graph.createIsRecordIdOf(recId, recElement);
    recIdOf.setSourcePositions((createSourcePositionList(lengthRecId, offsetRecId)));
    IsRecordExprOf  exprOf = graph.createIsRecordExprOf(expr, recElement);
    exprOf.setSourcePositions((createSourcePositionList(lengthExpr, offsetExpr)));
}
;

/** matches a record-id
*/
recordId returns [RecordId expr = null]
:
i=IDENT
{
 	expr = graph.createRecordId();
    expr.setName(i.getText());
}
;

pathConstruction returns [PathConstruction pathConstr = null]
@init{
	ArrayList<VertexPosition> expressions = new ArrayList<VertexPosition>();
}
:
	PATH
	LPAREN
	expressions = expressionList
	RPAREN
    {createPartsOfValueConstruction(expressions, graph.createPathConstruction()); }
;
	

pathsystemConstruction returns [PathSystemConstruction pathsystemConstr = null] 
@init{
	Expression expr = null;
    EdgeVertexList eVList = null;
    int offsetExpr = 0;
    int offsetEVList = 0;
    int lengthExpr = 0;
    int lengthEVList = 0;
}
	:
		PATHSYSTEM
		LPAREN
        { offsetExpr = getLTOffset(); }
		expr = expression
        {
        	lengthExpr = getLTLength(offsetExpr);
    		pathsystemConstr = graph.createPathSystemConstruction();
	       	IsRootOf rootOf = graph.createIsRootOf(expr, pathsystemConstr);
	       	rootOf.setSourcePositions((createSourcePositionList(lengthExpr, offsetExpr)));
        }
		(	COMMA
        	{ offsetEVList = LT(1).getColumn()-1; }
			eVList = edgeVertexList
            {
            	lengthEVList = getLTLength(offsetEVList);
               	IsEdgeVertexListOf exprOf = graph.createIsEdgeVertexListOf(eVList, pathsystemConstr);
               	exprOf.setSourcePositions((createSourcePositionList(lengthEVList, offsetEVList)));
            }
		)*
		RPAREN
	;
	
	
	
quantifiedDeclaration returns [Declaration declaration = null]
@init{
	Expression subgraphExpr = null;
	Expression constraintExpr = null;
    ArrayList<VertexPosition> declarations = new ArrayList<VertexPosition>();
    int offsetConstraint = 0;
    int offsetSubgraph = 0;
    int lengthConstraint = 0;
    int lengthSubgraph = 0;
}
:
declarations = declarationList
{declaration = (Declaration) createMultipleEdgesToParent(declarations, graph.createDeclaration(), IsSimpleDeclOf.class);}
(COMMA
{ offsetConstraint = getLTOffset(); }
constraintExpr = expression
{
	lengthConstraint = getLTLength(offsetConstraint);
	IsConstraintOf constraintOf = graph.createIsConstraintOf(constraintExpr,declaration);
	constraintOf.setSourcePositions((createSourcePositionList(lengthConstraint, offsetConstraint)));
}
(
	(COMMA simpleDeclaration) => 
	(
	  COMMA declarations = declarationList
	  {createMultipleEdgesToParent(declarations, declaration, IsSimpleDeclOf.class);}
	)
| /* empty */)
)*
/* subgraphs */
(	IN
	{ offsetSubgraph = getLTOffset(); }
	subgraphExpr = expression
    {
       	lengthSubgraph = getLTLength(offsetSubgraph); 
    	IsSubgraphOf subgraphOf = graph.createIsSubgraphOf(subgraphExpr, declaration);
      	subgraphOf.setSourcePositions((createSourcePositionList(lengthSubgraph, offsetSubgraph)));
    }
)?
;	



declarationList returns [ArrayList<VertexPosition> declList = new ArrayList<VertexPosition>()] 
@init{
	SimpleDeclaration v = null;
    VertexPosition simpleDecl = new VertexPosition();
}
:
{ simpleDecl.offset = getLTOffset(); }
v = simpleDeclaration
{
    simpleDecl.length = getLTLength(simpleDecl.offset);
    simpleDecl.node = v;
    declList.add(simpleDecl);
}
(
COMMA
{ simpleDecl.offset = getLTOffset(); }
v = simpleDeclaration
{
    simpleDecl.length = getLTLength(simpleDecl.offset);
    simpleDecl.node = v;
    declList.add(simpleDecl);
}
)*
;



simpleDeclaration returns [SimpleDeclaration simpleDecl = null]
@init{
	Expression expr = null;
    ArrayList<VertexPosition> variables = new ArrayList<VertexPosition>();
    int offset = 0;
    int length = 0;
}
:
	variables = variableList
	COLON
    { offset = getLTOffset(); }
 	expr = expression
    {
       	length = getLTLength(offset);
       	simpleDecl = (SimpleDeclaration) createMultipleEdgesToParent(variables, graph.createSimpleDeclaration(), IsDeclaredVarOf.class);
        IsTypeExprOf typeExprOf = graph.createIsTypeExprOfDeclaration(expr, simpleDecl);
        typeExprOf.setSourcePositions((createSourcePositionList(length, offset)));
    }
;
	
expressionList returns [ArrayList<VertexPosition> expressions]
@init{
	expressions = new ArrayList<VertexPosition>();
	Expression expr = null;
    VertexPosition v = new VertexPosition();
    ArrayList<VertexPosition> exprList = new ArrayList<VertexPosition>();
}
:  
{ v.offset = getLTOffset();}
expr = expression
{
  	v.length = -offset + LT(0).getColumn()-1 + LT(0).getText().length();
    v.node = expr;
    expressions.add(v);
}
(	COMMA
	exprList = expressionList
	{expressions.addAll(exprList);}
)?
;





rangeExpression returns [Expression expr = null]
@init{
	ArrayList<VertexPosition> typeIds = new ArrayList<VertexPosition>();
}
:
(  V {expr = graph.createVertexSetExpression();}
 | E { expr = graph.createEdgeSetExpression(); }
)
( (LCURLY (typeExpressionList)? RCURLY ) =>
  (LCURLY (typeIds = typeExpressionList)? RCURLY)
|
)
{createMultipleEdgesToParent(typeIds, expr, IsTypeRestrOf.class);}
;

/** matches a subgraph expression
	@return
*/
graphRangeExpression returns [Expression expr = null]
@init{
	ArrayList<VertexPosition> typeIds = new ArrayList<VertexPosition>();
}
:
(	VSUBGRAPH { expr = graph.createVertexSubgraphExpression(); }
|	ESUBGRAPH { expr = graph.createEdgeSubgraphExpression(); }
)
LCURLY typeIds = typeExpressionList	RCURLY
{createMultipleEdgesToParent(typeIds, expr, IsTypeRestrOf.class);}
;


typeExpressionList returns [ArrayList<VertexPosition> typeIdList = new ArrayList<VertexPosition>()] 
@init{
	Expression v = null;
	ArrayList<VertexPosition> list = null;
    VertexPosition type = new VertexPosition();
}
:
{ type.offset = geTLTOffset(); }
v = typeId
{
    type.node = v;
    type.length = getLTLength(type.offset);
    typeIdList.add(type);
}
(COMMA list = typeExpressionList {typeIdList.addAll(list);} )?
;



qualifiedName returns [String name = null]
@init{
    String newName = null;
}
: 
i=IDENT {name = i.getText();}
( DOT
	newName = qualifiedName
	{name = name + "." + newName;}
)? 
;


typeId returns [TypeId type = null] 
@init{
     String s;
}
:
{type = graph.createTypeId();}
(CARET	{ type.setExcluded(true); } )?
( s = qualifiedName )
{ type.setName(s); }
/*(CARET	{ type.setExcluded(true); }	)?*/
(EXCL	{ type.setType(true);  })?
;



literal returns [Literal literal = null]
:
	s=STRING_LITERAL
    {
       	literal = graph.createStringLiteral();
       	((StringLiteral) literal).setStringValue(decode(s.getText()));
    }
|	tv=THISVERTEX
    {
  /*     literal = graph.getFirstThisLiteral();
       if (literal != null)	
	       	return literal;*/
       literal = graph.createThisVertex();
    }
|	te=THISEDGE
    {
     	/*literal = graph.getFirstThisEdge();
        if (literal != null)
	      	return literal;*/
        literal = graph.createThisEdge();
    }
|	i=DECLITERAL | i=HEXLITERAL | i = OCTLITERAL
	{
        int value = 0;
        if (i.getText().startsWith("0x") || i.getText().startsWith("0X") ) {
           	value = Integer.parseInt(i.getText().substring(2),16);
        } else if (i.getText().startsWith("0") && i.getText().length()>1) {
         	value = Integer.parseInt(i.getText().substring(1),8);
        } else {
          	value = Integer.parseInt(i.getText());
        }
	    literal = graph.createIntLiteral();
		((IntLiteral) literal).setIntValue(value);
	}
|	r=FLOAT_LITERAL
    {
       	literal = graph.createRealLiteral();
		((RealLiteral) literal).setRealValue(Double.parseDouble(r.getText()));
    }
|	TRUE
    {
        literal = (Literal) graph.getFirstVertexOfClass(BoolLiteral.class);
        /*	if (literal == null || !( (BoolLiteral) literal).isBoolValue() ) {
	           	literal = graph.createBoolLiteral();
   	           	((BoolLiteral) literal).setBoolValue(true);
        }*/
        if (literal == null || ( (BoolLiteral) literal).getBoolValue() != TrivalentBoolean.TRUE ) {
		   	literal = graph.createBoolLiteral();
				((BoolLiteral) literal).setBoolValue(TrivalentBoolean.TRUE);
		}
    }
|	FALSE
    {
        literal = (Literal) graph.getFirstVertexOfClass(BoolLiteral.class);
         /*   	if (literal == null || ( (BoolLiteral) literal).isBoolValue() ) {
	            	literal = graph.createBoolLiteral();
             	   ((BoolLiteral) literal).setBoolValue(false);
   	            }*/
   	    if (literal == null || ( (BoolLiteral) literal).getBoolValue() == TrivalentBoolean.TRUE ) {
		   	literal = graph.createBoolLiteral();
		   	((BoolLiteral) literal).setBoolValue(TrivalentBoolean.FALSE);
		}
    }
|	NULL_VALUE
    {
        literal = (Literal) graph.getFirstVertexOfClass(NullLiteral.class);
	    if (literal == null)
		    literal = graph.createNullLiteral(); 
    }
;
	

edgeVertexList returns [EdgeVertexList eVList = null]
@init{
	Expression edgeExpr = null;
	Expression  vertexExpr = null;
	EdgeVertexList eVList2 = null;
    int offsetV = 0;
    int offsetE = 0;
    int offsetEVList = 0;
    int lengthV = 0;
    int lengthE = 0;
    int lengthEVList = 0;
}
	:
		LPAREN
        {offsetE = getLTOffset(); }
		edgeExpr = expression
        {lengthE = getLTLength(offsetE); }
		COMMA
        {offsetV = getLTOffset(); }
		vertexExpr = expression
        {
        	lengthV = getLTLength(offsetV);
            eVList = graph.createEdgeVertexList();
            IsEdgeOrVertexExprOf eExprOf = graph.createIsEdgeOrVertexExprOf(edgeExpr, eVList);
            eExprOf.setSourcePositions((createSourcePositionList(lengthE, offsetE)));
            IsEdgeOrVertexExprOf vExprOf = graph.createIsEdgeOrVertexExprOf(vertexExpr, eVList);
            vExprOf.setSourcePositions((createSourcePositionList(lengthV, offsetV)));
        }
		(COMMA
        	{offsetEVList = getLTOffset(); }
			eVList2 = edgeVertexList
            {
            	lengthEVList = getLTLength(offsetEVList);
           		IsElementOf exprOf = graph.createIsElementOf(eVList2, eVList);
            	exprOf.setSourcePositions((createSourcePositionList(lengthEVList, offsetEVList)));
            }
		)*
		RPAREN
	;	
	


expressionOrPathDescription returns [Expression retVal = null]
@init{
	Expression expr = null;
} 
@after {
	retVal = expr;
}
:
    (  (pathDescription expression) => expr = expression
    | (expression) => expr = expression
    | expr = pathDescription)
;	
	
	

edgeRestrictionList returns [ArrayList<VertexPosition> list = new ArrayList<VertexPosition>()] 
@init{
	TypeId type = null;
	RoleId role = null;
	ArrayList<VertexPosition> eRList = null;
	VertexPosition v = new VertexPosition();
	EdgeRestriction er = null;
	int offsetType = 0;
	int offsetRole = 0;
	int lengthType = 0;
	int lengthRole = 0;
}
	:
	(	(
			{offsetRole = LgetLTOffset(); }
			AT role = roleId
			{lengthRole = getLTLength(offsetRole);}
		)
		|
		(
			{offsetType = getLTOffset(); }
			type = typeId
			{lengthType = getLTLength(offsetType);}
			(	AT
				{ offsetRole = getLTOffset(); }
				role = roleId
			)?
		)
	)
    {
      	lengthRole = getLTLength(offsetRole);
       	er = graph.createEdgeRestriction();
       	if (type != null) {
	       	IsTypeIdOf typeIdOf = graph.createIsTypeIdOf(type, er);
	       	typeIdOf.setSourcePositions((createSourcePositionList(lengthType, offsetType)));
       	}
       	if (role != null) {
       		IsRoleIdOf roleIdOf = graph.createIsRoleIdOf(role,er);
       		roleIdOf.setSourcePositions((createSourcePositionList(lengthRole, offsetRole)));
       	}
        v.node = er;
        v.offset = offsetType;
        v.length = -offsetType + offsetRole + lengthRole;
       	list.add(v);
    }
	(	COMMA eRList = edgeRestrictionList {list.addAll(eRList);}  )?
;	


labeledReportList returns [BagComprehension retVal = null]
@init{
	Expression expr = null;
	Expression asExpr = null;
	TupleConstruction tupConstr = null;
    boolean hasLabel = false;
    IsCompResultDefOf e = null;
    int offset = 0;
    int offsetExpr = 0;
    int offsetAsExpr = 0;
    int lengthExpr = 0;
    int lengthAsExpr = 0;
    BagComprehension bagCompr = null;
}
@after {
	retVal = bagCompr;
}
	:
    	{ offsetExpr = getLTOffset();
    	  offset = offsetExpr;
    	}
		expr = expression
 		{ lengthExpr = getLTLength(offsetExpr); }
        (	AS
       		{ offsetAsExpr = getLTOffset(); }
			asExpr = expression
            {
            	lengthAsExpr = getLTLength(offfsetAsExpr);
            	hasLabel = true;
            }
		)?
        {
            bagCompr = graph.createBagComprehension();
			tupConstr = graph.createTupleConstruction();
			e = graph.createIsCompResultDefOf(tupConstr, bagCompr);
			IsPartOf partOf = graph.createIsPartOf((Expression)expr, tupConstr);
			partOf.setSourcePositions((createSourcePositionList(lengthExpr, offsetExpr)));
		  	if (hasLabel) {
			    IsTableHeaderOf tableHeaderOf = graph.createIsTableHeaderOf(asExpr, bagCompr);
			    tableHeaderOf.setSourcePositions((createSourcePositionList(lengthAsExpr, offsetAsExpr)));
			} 
		}
		(	{ hasLabel = false; }
        	COMMA
            { offsetExpr = getLTOffset(); }
			expr = expression
            { lengthExpr = getLTLength(offsetExpr); }
			(	AS
            	{ offsetAsExpr = getLTOffset(); }
				asExpr = expression
                {
                	lengthAsExpr = getLTLength(offsetAsExpr);
                	hasLabel = true;
                }
			)?
            {
			    IsPartOf partOf = graph.createIsPartOf(expr, tupConstr);
			    partOf.setSourcePositions((createSourcePositionList(lengthExpr, offsetExpr)));
		  	   	if (hasLabel) {
				    IsTableHeaderOf tableHeaderOf = graph.createIsTableHeaderOf(asExpr, bagCompr);
					tableHeaderOf.setSourcePositions((createSourcePositionList(lengthAsExpr, offsetAsExpr)));
			    }
		    }
		)*
		{
			e.setSourcePositions((createSourcePositionList(getLTLength(offset), offset)));
		  	if (tupConstr.getDegree(EdgeDirection.IN) == 1)	{
				Vertex v = tupConstr.getFirstEdge(EdgeDirection.IN).getAlpha();
				Edge e2 = tupConstr.getFirstEdge(EdgeDirection.OUT);
				e2.setAlpha(v);
				tupConstr.delete();
			}
		}
	;
	
	

reportClause returns [Comprehension retVal = null] 
@init{
	ArrayList<VertexPosition> reportList = new ArrayList<VertexPosition>();
	int offset = 0;
	int length = 0;
	boolean vartable = false;
	Comprehension comprehension = null
}
@after {
	retVal = comprehension;
}
	:
	(	REPORT
		comprehension = labeledReportList
	)
	|
	(	 REPORTBAG   {comprehension = graph.createBagComprehension();}
	   | REPORTSET   {comprehension = graph.createSetComprehension(); }  
	   | REPORTTABLE {comprehension = graph.createTableComprehension(); vartable = true; }
	)
    { offset = getLTOffset(); }
	reportList = expressionList
    {
        length = getLTLength(offset);
		IsCompResultDefOf e = null;
	    if (!vartable) {
	       	if (reportList.size() > 1) {
			  	TupleConstruction tupConstr = (TupleConstruction) createMultipleEdgesToParent(reportList, graph.createTupleConstruction(), IsPartOf.class);
	       	   	e = graph.createIsCompResultDefOf(tupConstr, comprehension);
	       	} else {
		    	e = graph.createIsCompResultDefOf((Expression)(reportList.get(0)).node, comprehension);
			}
			e.setSourcePositions((createSourcePositionList(length, offset)));
	    } else {
	         if ((reportList.size() != 3) && (reportList.size() != 4))
	            throw new ParseException("reportTable columHeaderExpr, rowHeaderExpr, cellContent [,tableHeader] must be followed by three or for arguments", "reportTable", new SourcePosition(length, offset));
					// size == 3, set columnHeader and rowHeader
	       IsColumnHeaderExprOf cHeaderE = graph.createIsColumnHeaderExprOf((Expression)(reportList.get(0)).node, (TableComprehension)comprehension);
	       cHeaderE.setSourcePositions((createSourcePositionList((reportList.get(0)).length, (reportList.get(0)).offset)));
	       IsRowHeaderExprOf rHeaderE = graph.createIsRowHeaderExprOf((Expression)(reportList.get(1)).node, (TableComprehension)comprehension);
	       rHeaderE.setSourcePositions((createSourcePositionList((reportList.get(1)).length, (reportList.get(1)).offset)));
	       e = graph.createIsCompResultDefOf((Expression)(reportList.get(2)).node, comprehension);
	       e.setSourcePositions((createSourcePositionList((reportList.get(2)).length, (reportList.get(2)).offset)));

		   // tableheader
	       if (reportList.size() == 4) {
	           	IsTableHeaderOf tHeaderE = graph.createIsTableHeaderOf((Expression)(reportList.get(3)).node, (ComprehensionWithTableHeader)comprehension);
	           	tHeaderE.setSourcePositions((createSourcePositionList((reportList.get(3)).length, (reportList.get(3)).offset)));
	       }
	 	}
	}
;	
		

simpleQuery returns [Comprehension retVal = null]
@init{
	ArrayList<VertexPosition> declarations = new ArrayList<VertexPosition>();
    Declaration declaration = null;
    Expression subgraphExpr = null;
    Expression constraintExpr = null;
    int offsetDecl = 0;
    int lengthDecl = 0;
    int offsetSubgraph = 0;
    int lengthSubgraph = 0;
    int offsetConstraint = 0;
    int lengthConstraint = 0;
	Comprehension comprehension = null
}
@after {
	retVal = comprehension;
}
	:
		// declaration part
		FROM
		declarations = declarationList
        {
        	//TODO dbildh 21.11.08 : check if this can be replaced by call of declaration
        	declaration = graph.createDeclaration();
        	if (declaration.size() > 0) {
        		offsetDecl = declaration.get(0).offset;
        	}
        	createMultipleEdgesToParent(declarations, declaration, IsSimpleDeclOf.class);
        	lengthDecl = getLTLength(offsetDecl);
        }
        // optional subgraph-clause
		(	IN
			{ offsetSubgraph = getLTOffset(); }
			subgraphExpr = expression
            {
	            lengthSubgraph = getLTLength(offsetSubgraph);
	           	lengthDecl += lengthSubgraph;
	           	IsSubgraphOf subgraphOf = graph.createIsSubgraphOf(subgraphExpr, declaration);
	           	subgraphOf.setSourcePositions((createSourcePositionList(lengthSubgraph, offsetSubgraph)));
			}
		)?
		// optional predicate
		(	WITH
			{ offsetConstraint = getLTOffset(); }
			constraintExpr = expression
            {
            	lengthConstraint = getLTLength(offsetConstraint);
	           	lengthDecl += lengthConstraint;
	           	IsConstraintOf  constraintOf = graph.createIsConstraintOf(constraintExpr, declaration);
	          	constraintOf.setSourcePositions((createSourcePositionList(lengthConstraint, offsetConstraint)));
			}
		)?
		// report-clause
		{ offsetResult = getLTOffset();}
		comprehension = reportClause
        {
		    lengthResult = getLTLength(offsetResult);
	   		IsCompDeclOf comprDeclOf = graph.createIsCompDeclOf(declaration, comprehension);
	   		comprDeclOf.setSourcePositions((createSourcePositionList(lengthDecl, offsetDecl)));
		}
		END
	;		
		
	

regPathExistenceOrForwardVertexSet[Expression arg1, int offsetArg1, int lengthArg1]	returns [Expression expr = null]
@init{
	PathDescription pathDescr = null;
	Expression restrExpr = null;
	int offsetPathDescr = 0;
	int offsetExpr = 0;
	int lengthPathDescr = 0;
	int lengthExpr = 0;
}
	:
	{ offsetPathDescr = getLTOffset(); }
	pathDescr = pathDescription
    { lengthPathDescr = getLTLenth(offsetPathDescr);}
	( 	(primaryExpression) =>
       	{ offsetExpr = getLTOffset(); }
       	restrExpr = restrictedExpression
        { lengthExpr = getLTLength(offsetExpr); }
	| /* forward vertex set */
	)
	{
		if (restrExpr != null) {
			// create new pathexistence
			PathExistence pe = graph.createPathExistence();
			expr = pe;
					
			// add start vertex
			IsStartExprOf startVertexOf = graph.createIsStartExprOf(arg1, pe);
			startVertexOf.setSourcePositions((createSourcePositionList(lengthArg1, offsetArg1)));

			// add target vertex
			IsTargetExprOf targetVertexOf = graph.createIsTargetExprOf(restrExpr, pe);
			targetVertexOf.setSourcePositions((createSourcePositionList(lengthExpr, offsetExpr)));

			// add pathdescription
			IsPathOf pathOf = graph.createIsPathOf(pathDescr, pe);
			pathOf.setSourcePositions((createSourcePositionList(lengthPathDescr, offsetPathDescr)));
		} else {
			// create new forward-vertex-set
			ForwardVertexSet fvs = graph.createForwardVertexSet();
			expr = fvs;
			// add start expr
			IsStartExprOf startVertexOf = graph.createIsStartExprOf(arg1, fvs);
			startVertexOf.setSourcePositions((createSourcePositionList(lengthArg1, offsetArg1)));
			// add pathdescr
			IsPathOf pathOf = graph.createIsPathOf(pathDescr, fvs);
			pathOf.setSourcePositions((createSourcePositionList(lengthPathDescr, offsetPathDescr)));
		}
	}
;	
	

//TODO: check if this is really needed
regPathOrPathSystem[Expression arg1, int offsetArg1, int lengthArg1] returns [Expression expr = null]
@init{
	PathDescription pathDescr = null;
	Expression restrExpr = null;
    boolean isPath = false;
    int offsetPathDescr = 0;
    int offsetOperator1 = 0;
    int offsetOperator2 = 0;
    int offsetExpr = 0;
    int lengthPathDescr = 0;
    int lengthExpr = 0;
}
:
	{ offsetOperator1 = getLTOffset(); }
	SMILEY
    { offsetPathDescr = getLTOffset(); }
	pathDescr = pathDescription
	{ lengthPathDescr = getLTLength(offsetPathDescr);}
    (
    	{ offsetOperator2 = getLTOffset(); }
        SMILEY
      	{ offsetExpr = getLTOffset();}
        restrExpr = restrictedExpression
        { 
          lengthExpr = getLTLength(offsetPathDescr);
          isPath = true;
        }
	)?
    {
        FunctionApplication funAp = graph.createFunctionApplication();
		expr = funAp;
		FunctionId funId = graph.createFunctionId();
		funId.setName("pathSystem");
		createFunctionIdAndArgumentOf(funAp, funId, offsetOperator1, 3, 
		   							  arg1, offsetArg1, lengthArg1, pathDescr, 
		   							  offsetPathDescr, lengthPathDescr); 
		if (isPath) {
			arg1 = expr;
			funAp = graph.createFunctionApplication();
			expr = funAp;
			createFunctionIdAndArgumentOf(funAp, funId, offsetOperator1, 3,
											  arg1, offsetArg1, -offsetArg1 + offsetOperator2 + 3,
											  restrExpr, offsetExpr, lengthExpr);
		}
	}
;	
	


regBackwardVertexSetOrPathSystem returns [Expression expr = null] 
@init{
	PathDescription pathDescr = null;
    Expression restrExpr = null;
    boolean isPathSystem = false;
    int offsetPathDescr = 0;
    int offsetExpr = 0;
    int offsetOperator = 0;
    int lengthPathDescr = 0;
    int lengthExpr = 0;
}
	:
		{ offsetPathDescr = getLTOffset(); }
		// the path description
        pathDescr = pathDescription
        { lengthPathDescr = getLTLength(offsetPathDescr); }
        /* is it a pathsystem ?*/
        (
        	{ offsetOperator = getLTOffset(); }
            SMILEY
            { isPathSystem = true; }
		)?
        { offsetExpr = getLTOffset(); }
        // the target vertex
		restrExpr = restrictedExpression
		{
		    lengthExpr = getLTLength(offsetExpr);
	        if (isPathSystem) {
		   		// create a path-system-functionapplication
				FunctionApplication fa = graph.createFunctionApplication();
				expr = fa;
				FunctionId f = (FunctionId )functionSymbolTable.lookup("pathSystem");
				if (f == null)	{
		            f = graph.createFunctionId();
		            f.setName("pathSystem");
		            functionSymbolTable.insert("pathSystem", f);
		        }
		        createFunctionIdAndArgumentOf(fa, f, offsetOperator, 3, 
				  							  pathDescr, offsetPathDescr, lengthPathDescr, restrExpr, offsetExpr, lengthExpr); 	
	        } else {
		    	// create a backwardvertexset
				BackwardVertexSet bs = graph.createBackwardVertexSet();
				expr = bs;
	            IsTargetExprOf targetVertexOf = graph.createIsTargetExprOf(restrExpr, bs);
	            targetVertexOf.setSourcePositions((createSourcePositionList(lengthExpr, offsetExpr)));
				IsPathOf pathOf = graph.createIsPathOf(pathDescr, bs);
				pathOf.setSourcePositions((createSourcePositionList(lengthPathDescr, offsetPathDescr)));
	        }
        }
;
