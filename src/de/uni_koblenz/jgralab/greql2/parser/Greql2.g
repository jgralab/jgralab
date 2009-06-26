grammar Greql2;
options {backtrack=false;}

tokens {
	FUNCTIONID;
	THISVERTEX;
	THISEDGE;
}

@lexer::members {
//  protected boolean enumIsKeyword = true;
//  protected boolean assertIsKeyword = true;
  
  private boolean isFunctionName(String ident) {
       return Greql2FunctionLibrary.instance().isGreqlFunction(ident);		
  }	
}


@header {
package de.uni_koblenz.jgralab.greql2.parser;

import java.util.Queue;
import java.util.LinkedList;
import java.util.Set;
import java.util.HashSet;

import org.antlr.runtime.RecognitionException;

import antlr.TokenStreamException;
import de.uni_koblenz.jgralab.*;
import de.uni_koblenz.jgralab.greql2.exception.*;
import de.uni_koblenz.jgralab.greql2.schema.*;
import de.uni_koblenz.jgralab.greql2.schema.impl.*;
import de.uni_koblenz.jgralab.schema.*;
}

@lexer::header {
package de.uni_koblenz.jgralab.greql2.parser;
import org.antlr.runtime.RecognitionException;
import antlr.TokenStreamException;
import de.uni_koblenz.jgralab.greql2.funlib.Greql2FunctionLibrary;
}

@members {
    private Greql2Schema schema = null;
    private Greql2 graph = null;
    private SymbolTable variableSymbolTable = null;
    private SymbolTable functionSymbolTable = null;
    private boolean graphCleaned = false;

    class FunctionConstruct {
    	String operatorName = null;  
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
        
        public boolean isValidFunction() {
        	return operatorName != null;
        }
    
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
	  		this.operatorName = op;
        }
        
        public FunctionApplication postArg2(Expression arg2) { 
         	lengthArg2 = getLTLength(offsetArg2);
         	this.arg2 = arg2;
			op = getFunctionId(operatorName);
       	  	return createFunctionIdAndArgumentOf(op, offsetOperator,lengthOperator, 
	   			  arg1, offsetArg1, lengthArg1, arg2, offsetArg2, lengthArg2, binary); 	   
        }
    }
    
    /**
     * @return a set of variables names used in this query that are still valid 
     *         at the current position during the parsing process
     * After a successful parsing, this set should be empty 
     */
    public Set<String> getUsedVariableNames() {
   		return variableSymbolTable.getKnownIdentifierSet();
    }
    
    private FunctionId getFunctionId(String name) {
        FunctionId functionId = (FunctionId) functionSymbolTable.lookup(name);
		if (functionId == null) {
			functionId = graph.createFunctionId();
			functionId.setName(name);
			functionSymbolTable.insert(name, functionId);
		}
		return functionId;
    }
    

        /**
         * Test if all ThisLiterals occur only inside PathDescriptions because
         * they must not be used outside PathDescriptions
         * If any ThisLiteral that occurs outside a PathDescription is found,
         * a ParseException is thrown.
         */
    	private void testIllegalThisLiterals() {
    		Set<Class<? extends Greql2Vertex>> allowedClassesForThisLiterals
    		  = new HashSet<Class<? extends Greql2Vertex>> ();
    		  allowedClassesForThisLiterals.add(PathDescription.class);
    		  allowedClassesForThisLiterals.add(PathExistence.class);
    		  allowedClassesForThisLiterals.add(ForwardVertexSet.class);
    		  allowedClassesForThisLiterals.add(BackwardVertexSet.class);
    	
    		for (ThisLiteral vertex : graph.getThisLiteralVertices()) {
    			for (Edge sourcePositionEdge : vertex.incidences(EdgeDirection.OUT)) {
    				Queue<Greql2Vertex> queue = new LinkedList<Greql2Vertex>();
    				queue.add(vertex);
    				while (!queue.isEmpty()) {
    					Greql2Vertex currentVertex = queue.poll();
    					for (Edge edge : currentVertex.incidences(EdgeDirection.OUT)) {
    						Greql2Vertex omega = (Greql2Vertex) edge.getOmega();
    						if (allowedClassesForThisLiterals.contains(omega.getM1Class()))
    							continue;
    						if (omega instanceof FunctionApplication) {
    							FunctionApplication fa = (FunctionApplication) omega;
    							FunctionId funid = (FunctionId) fa.getFirstIsFunctionIdOf(EdgeDirection.IN).getAlpha();
    							if (funid.getName().equals("pathSystem"))
    								continue;
    						}	
    						if (omega instanceof Greql2Expression) 
    							throw new ParseException("This literals must not be used outside pathdescriptions", vertex.getName(), ((Greql2Aggregation) sourcePositionEdge).getSourcePositions().get(0) );
    						queue.add(omega);
    					}
    				}
    			}	
    		}
    		ThisVertex firstThisVertex = null;
    		for (ThisVertex thisVertex : graph.getThisVertexVertices()) {
    			if (firstThisVertex == null) {
    				firstThisVertex = thisVertex;
    			} else {
    				while (thisVertex.getFirstEdge() != null) {
    					Edge e = thisVertex.getFirstEdge();
    					e.setThis(firstThisVertex);
    				}
    			}
    		}
    		ThisEdge firstThisEdge = null;
    		for (ThisEdge thisEdge : graph.getThisEdgeVertices()) {
    			if (firstThisEdge == null) {
    				firstThisEdge = thisEdge;
    			} else {
    				while (thisEdge.getFirstEdge() != null) {
    					Edge e = thisEdge.getFirstEdge();
    					e.setThis(firstThisEdge);
    				}
    			}
    		}
    	}


    private FunctionApplication createFunctionIdAndArgumentOf(FunctionId functionId, int offsetOperator, int lengthOperator, Expression arg1, int offsetArg1, int lengthArg1, Expression arg2, int offsetArg2, int lengthArg2, boolean binary) {
        	FunctionApplication fa = graph.createFunctionApplication();
        	IsFunctionIdOf functionIdOf = graph.createIsFunctionIdOf(functionId, fa);
        	functionIdOf.setSourcePositions((createSourcePositionList(lengthOperator, offsetOperator)));
        	IsArgumentOf arg1Of = null;
        	if (binary) {
        		arg1Of = graph.createIsArgumentOf(arg1, fa);
        		arg1Of.setSourcePositions((createSourcePositionList(lengthArg1, offsetArg1)));
        	}
          	IsArgumentOf arg2Of = graph.createIsArgumentOf(arg2, fa);
        	arg2Of.setSourcePositions((createSourcePositionList(lengthArg2, offsetArg2)));
        	return fa;
    }
    
    private List<SourcePosition> createSourcePositionList(int length, int offset) {
    	List<SourcePosition> list = new ArrayList<SourcePosition>();
    	list.add(new SourcePosition(length, offset));
    	return list;
    }


	private ValueConstruction createPartsOfValueConstruction(List<VertexPosition> expressions, ValueConstruction parent) {
		return (ValueConstruction) createMultipleEdgesToParent(expressions, parent, IsPartOf.class);
	}
	
	
	private Vertex createMultipleEdgesToParent(List<VertexPosition> expressions, Vertex parent, Class<? extends Edge> edgeClass) {
       	if (expressions != null)
       		for (VertexPosition expr : expressions) {
				Greql2Aggregation edge = (Greql2Aggregation) graph.createEdge(edgeClass, (Vertex)expr.node, parent);
				edge.setSourcePositions((createSourcePositionList(expr.length, expr.offset)));
			}
		return parent;
	}


	public PathDescription addPathElement(Class<? extends PathDescription> vc, Class<? extends Edge> ec, PathDescription pathDescr, PathDescription part1, int offsetPart1, int lengthPart1, PathDescription part2, int offsetPart2, int lengthPart2) {
	 	Greql2Aggregation edge = null;
	 	if (pathDescr == null) {
	 		pathDescr = graph.createVertex(vc);
	 		edge =  (Greql2Aggregation) graph.createEdge(ec, part1, pathDescr);
	 		edge.setSourcePositions((createSourcePositionList(lengthPart1, offsetPart1)));
	 	}
		edge = (Greql2Aggregation) graph.createEdge(ec, part2, pathDescr);
		edge.setSourcePositions((createSourcePositionList(lengthPart2, offsetPart2 )));
		return pathDescr;
	}

	/** Returns the abstract syntax graph for the input
     *  @return the abstract syntax graph representing a GReQL 2 query
     */
    public Greql2 getGraph()  {
    	if (graph == null)
    		return null;
    	if (!graphCleaned) {
    	    Set<Vertex> reachableVertices = new HashSet<Vertex>();
    	    Queue<Vertex> queue = new LinkedList<Vertex>();
    		Greql2Expression root = graph.getFirstGreql2Expression();
    		if (root == null)
    			return null;
    		queue.add(root);
    		while (!queue.isEmpty()) {
    			Vertex current = queue.poll();
    			for (Edge e : current.incidences()) {
    				if (!reachableVertices.contains(e.getThat())) {
    					queue.add(e.getThat());
    					reachableVertices.add(e.getThat());
    				}	
    			}
    		}
    		Vertex deleteCandidate = graph.getFirstVertex();
    		while ((deleteCandidate != null) && (!reachableVertices.contains(deleteCandidate))) {
    			deleteCandidate.delete();
    			deleteCandidate = graph.getFirstVertex();
    		}
    		while (deleteCandidate != null)  {
    			if (!reachableVertices.contains(deleteCandidate)) {
    				Vertex v = deleteCandidate.getNextVertex();
    				deleteCandidate.delete();
    				deleteCandidate = v;
    			} else {
    				deleteCandidate = deleteCandidate.getNextVertex();
    			}	
    		}
    	}
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
    public void initialize() {
       	schema = Greql2Schema.instance();
		graph = Greql2Impl.create();
        variableSymbolTable = new SymbolTable();
 		functionSymbolTable = new SymbolTable();
		functionSymbolTable.blockBegin();
		graphCleaned = false;
    }
    
    private int currentLength = 0;
   
    private int getLTLength(int offset) {
       Token token = input.LT(0);
       if (token != null) {
        int charPos =  token.getCharPositionInLine();
        String text = token.getText();
        int length = text.length();
        currentLength = (- offset + charPos-1 + length);
        return currentLength;
       } else {
       		currentLength = -1;
    	    return -1;
       }	    
    }
    
    private int currentOffset = 0;
   
   private int getLTOffset() {
		currentOffset = input.LT(1).getCharPositionInLine()-1;
		return currentOffset; 
   }
   
   public void displayRecognitionError(String[] tokenNames, RecognitionException e) {
        String msg = getErrorMessage(e, tokenNames);
        String elementName = "";
        int length = -1;
        if (tokenNames.length > 0) {
        	length = tokenNames[0].length();
        	elementName = tokenNames[0];	
        }	
        SourcePosition pos = new SourcePosition(currentOffset, length);
        throw new ParseException("Error parsing query at offset: " + currentOffset + ", length: " + length + ". " + msg, elementName, pos, e ); 
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
			ArrayList<Edge> incidenceList = new ArrayList<Edge>();
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
    		Edge isCompResultDefOf = v.getFirstIsCompResultDefOf(EdgeDirection.IN);
    		if (isCompResultDefOf != null) {
    			mergeVariables(isCompResultDefOf.getAlpha());
    			// merge variables in table-headers if it's a bag-comprehension
    			if (v instanceof BagComprehension) {
    				IsTableHeaderOf isTableHeaderOf = v
						.getFirstIsTableHeaderOf(EdgeDirection.IN);
    				while (isTableHeaderOf != null) {
    					mergeVariables(isTableHeaderOf.getAlpha());
    					isTableHeaderOf = isTableHeaderOf
							.getNextIsTableHeaderOf(EdgeDirection.IN);
    				}
    			}
    			if (v instanceof TableComprehension) {
    				TableComprehension tc = (TableComprehension) v;
    				IsColumnHeaderExprOf ch = tc
						.getFirstIsColumnHeaderExprOf(EdgeDirection.IN);
    				mergeVariables(ch.getAlpha());
    				IsRowHeaderExprOf rh = tc
						.getFirstIsRowHeaderExprOf(EdgeDirection.IN);
    				mergeVariables(rh.getAlpha());
    				IsTableHeaderOf th = tc
						.getFirstIsTableHeaderOf(EdgeDirection.IN);
    				if (th != null)
    					mergeVariables(th.getAlpha());
    			}
    		}
    		if (v instanceof MapComprehension) {
    			IsKeyExprOfComprehension keyEdge = ((MapComprehension)v).getFirstIsKeyExprOfComprehension();
    			mergeVariables(keyEdge.getAlpha());
    			IsValueExprOfComprehension valueEdge = ((MapComprehension)v).getFirstIsValueExprOfComprehension();
    			mergeVariables(valueEdge.getAlpha());
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
MAP 	    : 'map';
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
REPORTMAP	: 'reportMap';
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
EDGESTART	: '<-';
EDGEEND		: '->';
EDGE		: '--';
RARROW		: '-->';
LARROW  	: '<--';
ARROW		: '<->';
ASSIGN		: ':=';
GASSIGN 	: '::=';
ENUM_SEPARATOR  : '::';
EQUAL		: '=';
MATCH 		: '=~';
NOT_EQUAL 	: '<>';
LE			: '<=';
GE			: '>=';
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
HASH 		: '#';
OUTAGGREGATION	: '<>--';
INAGGREGATION   : '--<>';
PATHSYSTEMSTART : '-<';	 	
IMPORT     : 'import';
	
//Whitespace
WS  :  (' '|'\r'|'\t'|'\u000C'|'\n')* 
	   {$channel=HIDDEN;}
;

SL_COMMENT
    :   '//' ~('\n'|'\r')*  ('\r\n' | '\r' | '\n') 
            {
                skip();
            }
    |   '//' ~('\n'|'\r')*     // a line comment could appear at the end of the file without CR/LF
            {
                skip();
            }
;            
            	    
// Multi-line comments	    
ML_COMMENT
@init{ 
	int start = input.getCharPositionInLine()-1;
}
    :   '/*' ( options {greedy=false;} : . )* '*/' {skip();}
    ;


		
STRING_LITERAL
@init{
	int start = input.getCharPositionInLine()-1; 
}	
    :  '"' ( ESCAPE_SEQUENCE | ~('\\'|'"') )* '"'
    ;
		

HEXLITERAL : '0' ('x'|'X') HexDigit+ IntegerTypeSuffix? ;

INTLITERAL : ('0'..'9')+ IntegerTypeSuffix;

DIGITSEQUENCE : ('0'..'9')+;

fragment
HexDigit : ('0'..'9'|'a'..'f'|'A'..'F') ;

fragment
IntegerTypeSuffix : ('l'|'L') ;

REALLITERAL
    :   ('0'..'9')+ Exponent FloatTypeSuffix?
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


IDENT
	:	
		(('a'..'z'|'A'..'Z'|'$') ('a'..'z'|'A'..'Z'|'_'|'$'|'0'..'9')*
		{			
			if (getText().equals("thisEdge")) 
				{_type = THISEDGE;}
			else if (getText().equals("thisVertex"))
				{_type = THISVERTEX;}
			else if (isFunctionName(getText()))
				{_type = FUNCTIONID;} 		
				
		})
	;



variableList returns [ArrayList<VertexPosition> variables = null] 
@init{
    int offset = 0;
    int length = 0;
    offset = getLTOffset();
}
: var = variable
  {
        VertexPosition v = new VertexPosition();
   	    length = getLTLength(offset);
        v.node = var;
        v.offset = offset;
        v.length = length;
        variables = new ArrayList<VertexPosition>();
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
		i = IDENT
        {	
        	var = graph.createVariable();
        	var.setName(i.getText());
        }
;





importDeclarationList returns [Set<String> typeNames = null]
:
    IMPORT
    importedType = qualifiedName
    ((DOT) => (DOT STAR {importedType += ".*";}) | )
    {
      typeNames = new HashSet<String>();
      typeNames.add(importedType);
    }
    SEMI 
(
  (IMPORT) => (followingNames = importDeclarationList {if (followingNames != null) typeNames.addAll(followingNames);})
 | /* no further imports */ )
;



greqlExpression 
@init{
	int offset = 0;
	int length = 0;
	initialize();
	Greql2Expression root = graph.createGreql2Expression();
}
: (
    (
    	typeNames = importDeclarationList
        {root.setImportedTypes(typeNames);}
    )?
    (USING varList = variableList COLON)?
    { offset = input.LT(1).getCharPositionInLine()-1; }
    expr = expression
    (STORE AS id = IDENT)?
    {
	length = getLTLength(offset);
	// add using-variables
	if (varList != null)
		for (VertexPosition var : varList) {
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
	     isId.setSourcePositions((createSourcePositionList(id.getText().length(), getLTOffset())));
	   }
   }
   EOF { testIllegalThisLiterals(); mergeVariables();}
  | EOF {graph = null;}/* allow empty input */ )
;



/** matches expressions
    @return  vertex representing the expression
*/
expression returns [Expression result = null]
: quantifiedExpression {$result = $quantifiedExpression.result;}
;


/** matches quantifiedExpressions
    @return vertex representing the quantified expression
*/
quantifiedExpression returns [Expression result = null]
@init{
	int offsetQuantifier = 0;
	int offsetQuantifiedDecl = 0;
	int offsetQuantifiedExpr = 0;
	int lengthQuantifier = 0;
	int lengthQuantifiedDecl = 0;
	int lengthQuantifiedExpr = 0;
}
:
  ((quantifier) => ( 
    {
       	offsetQuantifier = input.LT(1).getCharPositionInLine()-1;
       	lengthQuantifier = input.LT(1).getText().length();
    }
    // starts with quantifier ...
    q = quantifier
    { offsetQuantifiedDecl = input.LT(1).getCharPositionInLine()-1; }
    // ...followed by a declaration...
    decl = quantifiedDeclaration
    { lengthQuantifiedDecl = getLTLength(offsetQuantifiedDecl); }
    AT
    { offsetQuantifiedExpr = input.LT(1).getCharPositionInLine()-1; }
    // ... ends with predicate: a quantifiedExpr or something of lower level
    tempExpression = quantifiedExpression
    {
      	lengthQuantifiedExpr = getLTLength(offsetQuantifiedExpr);
        // create new Quantifies Expression
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
	result = quantifiedExprVertex;
    }
)
| // not a "real" quantified expression
  letExpression {$result = $letExpression.result;}
  )
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
  | EXISTS_ONE {name = "exists!";}  
  | EXISTS {name = "exists";} 
)
{
    for (Quantifier q : graph.getQuantifierVertices()) {
    	if (q.getName().equals(name))
    		return q;
    }
    quantifier = graph.createQuantifier();
    quantifier.setName(name);
}
;


/** matches let-expressions
    @return
*/
letExpression returns [Expression result = null]
@init{
	int offset = 0;
	int length = 0;
}
:
(
  LET
  // definitions
  defList = definitionList
  IN
  { offset = getLTOffset(); }
  // bound expression
  boundExpr = letExpression
  {
    length = getLTLength(offset);
    if (defList.size() != 0) {
 	  // create new letexpression-vertex
	  result = graph.createLetExpression();
	  // set bound expression
	  IsBoundExprOf exprOf = graph.createIsBoundExprOfDefinition(boundExpr, (LetExpression) result);
	  exprOf.setSourcePositions((createSourcePositionList(length, offset)));
	  // add definitions
	  if (defList != null)
	  for (VertexPosition def : defList) {
		  IsDefinitionOf definitionOf = graph.createIsDefinitionOf((Definition)def.node, (LetExpression) result);
		  definitionOf.setSourcePositions((createSourcePositionList(def.length, def.offset)));
	  }
    }
  }
)
| // not a let-Expression
  whereExpression {$result = $whereExpression.result;}
;

/** matches Where-Expressions
	@return
*/
whereExpression returns [Expression result = null]
@init{
	int offset = 0;
	int length = 0;
}
:
{ offset = input.LT(1).getCharPositionInLine()-1; }
// bound expression
conditionalExpression
{ length = getLTLength(offset); }
       	// optional "where"-part:
((WHERE) =>
	(WHERE
	defList = definitionList) |
)
{
if ((defList != null) && (!defList.isEmpty())){ //defList is empty if it's not a where-expression 
    // create new where-expression
    result = graph.createWhereExpression();
    // add boundexpr
	IsBoundExprOf exprOf = graph.createIsBoundExprOfDefinition($conditionalExpression.result, (WhereExpression) result);
	exprOf.setSourcePositions((createSourcePositionList(length, offset)));
	// add definitions
	if (defList != null)
	for (VertexPosition def : defList) {
		IsDefinitionOf isDefOf = graph.createIsDefinitionOf((Definition)def.node,  (WhereExpression) result);
		isDefOf.setSourcePositions((createSourcePositionList(length, offset)));
	}
} else {
	$result = $conditionalExpression.result; 
}	
}
;
	
	
	
/** matches a list of definitions for let- or where expressions
@return
*/
definitionList returns [ArrayList<VertexPosition> definitions = null] 
@init{
    int offset = 0;
    int length = 0;
}
	:
		{ offset = getLTOffset(); }
		v = definition
        {
			length = getLTLength(offset);
			VertexPosition def = new VertexPosition();
        	def.node = v;
            def.offset = offset;
            def.length = length;
            definitions = new ArrayList<VertexPosition>();
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
  	int offsetVar = 0;
  	int offsetExpr = 0;
    int lengthVar = 0;
    int lengthExpr = 0;
}
:
	{offsetVar = getLTOffset(); }
	var = variable
	{
	  lengthVar = getLTLength(offsetVar); 
	}
    ASSIGN
    {
      offsetExpr = getLTOffset(); 
    }
    expression
    {
      lengthExpr = getLTLength(offsetExpr);
      definition = graph.createDefinition();
      IsVarOf varOf = graph.createIsVarOf(var, definition);
      varOf.setSourcePositions((createSourcePositionList(lengthVar, offsetVar)));
      IsExprOf exprOf = graph.createIsExprOf($expression.result, definition);
      exprOf.setSourcePositions((createSourcePositionList(lengthExpr, offsetExpr)));
    }
;
	
	
/** matches conditional expressions
@return
*/
conditionalExpression returns [Expression result = null]
@init{
	int offsetExpr = 0;
	int offsetTrueExpr = 0;
	int offsetFalseExpr = 0;
	int offsetNullExpr = 0;
	int lengthExpr = 0;
	int lengthTrueExpr = 0;
	int lengthFalseExpr = 0;
	int lengthNullExpr = 0;
}
:
    { offsetExpr = getLTOffset(); }
    // condition or expression (if it's not a real conditional expr)
    orExpression
    { 
      $result = $orExpression.result;
      lengthExpr = getLTLength(offsetExpr); 
    }
    /* optional part */
    (
    QUESTION
    { offsetTrueExpr = getLTOffset(); }
    // expression which is evaluated if condition is true
    trueExpr = conditionalExpression
    {lengthTrueExpr = getLTLength(offsetTrueExpr); }
    COLON
    { offsetFalseExpr = getLTOffset(); }
    // expression which is evaluated if condition is true
    falseExpr = conditionalExpression
    { lengthFalseExpr = getLTLength(offsetFalseExpr); }
    COLON
    { offsetNullExpr = getLTOffset(); }
    // expression which is evaluated if condition is true
    nullExpr = conditionalExpression
    {
      lengthNullExpr = getLTLength(offsetNullExpr);
      // create new conditional expression
      ConditionalExpression condExpr = graph.createConditionalExpression();
      // add condition
	  IsConditionOf conditionOf = graph.createIsConditionOf(result, condExpr);
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
	  $result = condExpr;
    }
    )?
;

	
orExpression returns [Expression result]
@init{
    FunctionConstruct construct = new FunctionConstruct();
}
:
  { construct.preArg1(); }
  xorExpression 
  { 
    $result = $xorExpression.result;
    construct.preOp(result); 
  }
(OR
  {
    construct.postOp("or");
  }
  expr = orExpression
  {
    $result = construct.postArg2(expr);
  })?
;	

	
xorExpression returns [Expression result]
@init{
    FunctionConstruct construct = new FunctionConstruct();
}
:
  { construct.preArg1(); }
  andExpression
  { 
    $result = $andExpression.result;
    construct.preOp(result); 
  }
(XOR
  { construct.postOp("xor"); }
  expr = xorExpression
  { result = construct.postArg2(expr); })?
;	

andExpression returns [Expression result]
@init{
    FunctionConstruct construct = new FunctionConstruct();
}
:
  { construct.preArg1(); }
  equalityExpression
  { 
    $result = $equalityExpression.result;
    construct.preOp(result); 
  }
(AND
  { construct.postOp("and"); }
  expr = andExpression
  { result = construct.postArg2(expr); })?
;	

	

equalityExpression returns [Expression result]
@init{
    FunctionConstruct construct = new FunctionConstruct();
}
:
  { construct.preArg1(); }
  expr = relationalExpression
  { 
    $result = $relationalExpression.result;
    construct.preOp($result); 
  }
  {}
( 
  (EQUAL) =>
  (
    {}
    EQUAL { construct.postOp("equals"); }
    expr = equalityExpression
    { $result = construct.postArg2(expr);}
  )
| (NOT_EQUAL) =>
  (
    NOT_EQUAL { construct.postOp("nequals"); }
    expr = equalityExpression
    { $result = construct.postArg2(expr);}
  )
| /* RelationalExpression as fake EqualityExpression */)
;  
  


relationalExpression returns [Expression result]
@init{
    String name = null;
    FunctionConstruct construct = new FunctionConstruct();
}
:
  { construct.preArg1(); }
  additiveExpression
  { 
    $result = $additiveExpression.result;
    construct.preOp($result); 
  }
( ( L_T { name = "leThan"; }
  | LE { name = "leEqual"; }
  | G_T  { name = "grThan"; }
  | GE { name = "grEqual"; }
  | MATCH {name = "reMatch";} ) 
  { construct.postOp(name); }
  expr = relationalExpression
  {$result = construct.postArg2(expr); }
) ?
;


additiveExpression returns [Expression result]
@init{
    FunctionConstruct construct = new FunctionConstruct();
}
:
  { construct.preArg1(); }
  multiplicativeExpression
  {
    $result = $multiplicativeExpression.result;
    construct.preOp($result); 
  }
(
   (PLUS) => (PLUS  {construct.postOp("plus");} expr = additiveExpression {$result = construct.postArg2(expr);} )
  |(MINUS) => (MINUS { construct.postOp("minus");} expr = additiveExpression {$result = construct.postArg2(expr);})
  | /* fake multiplicative expression */
) 
;



multiplicativeExpression returns [Expression result]
@init{
    FunctionConstruct construct = new FunctionConstruct();
}
:
  { construct.preArg1(); }
  unaryExpression
  {
    $result = $unaryExpression.result; 
    construct.preOp($result); 
  }
( 
  (STAR) => (STAR {construct.postOp("times"); } expr = multiplicativeExpression)
 |(MOD)  => (MOD  { construct.postOp("modulo"); } expr = multiplicativeExpression)
 |(DIV)  => (DIV  { construct.postOp("dividedBy");} expr = multiplicativeExpression)
 | /* Only unaryExpression */
) 
{
    if (expr != null) 
        $result = construct.postArg2(expr); 
}
 
;

/** matches unary Expressions (-, not)
	@return
*/
unaryExpression returns [Expression result = null]
@init{
    FunctionConstruct construct = new FunctionConstruct();
}
:
((unaryOperator) =>  
 (
  { construct.preUnaryOp(); }
  unaryOp = unaryOperator
  { construct.postOp(unaryOp.getName()); }
  expr = pathExpression
  { $result = construct.postArg2(expr); }
 ) 
 |
 (
  expr2 = pathExpression
  {$result = expr2; }
 )
) 
;

/** matches a role-id
	@return
*/
roleId returns [RoleId role = null] 
:
i = identifier
{
   role = graph.createRoleId();
   role.setName(i.getName()); 
}
;

/** matches one of the unaryOperators '-' and 'not'
 *  @return functionId-vertex representing the operator
 */
unaryOperator returns [FunctionId result = null]
@init{
   String name = "uminus"; 
   FunctionId unaryOp = null;
}
:
( NOT { name = "not";}  | MINUS )
{
   unaryOp = (FunctionId) functionSymbolTable.lookup(name);
   if (unaryOp == null)  {
	unaryOp = graph.createFunctionId();
	unaryOp.setName(name);
	functionSymbolTable.insert(name, unaryOp);
   }
   $result = unaryOp;
}
;




/** matches restricted vertex expressions
    @return
*/
restrictedExpression returns [Expression result = null] 
@init{
    int offsetExpr = 0;
    int offsetRestr = 0;
    int lengthExpr = 0;
    int lengthRestr = 0;
}
:
{ offsetExpr = getLTOffset(); }
valueAccess {$result = $valueAccess.result;}
{
  lengthExpr = getLTLength(offsetExpr); 
}
((AMP LCURLY) => 
   (AMP LCURLY
   { offsetRestr = getLTOffset(); }
   restr = expression
   { lengthRestr = getLTLength(offsetRestr); }
   RCURLY
   {
      RestrictedExpression restrExpr = graph.createRestrictedExpression();
	  IsRestrictedExprOf restrExprOf = graph.createIsRestrictedExprOf(result, restrExpr);
	  restrExprOf.setSourcePositions((createSourcePositionList(lengthExpr, offsetExpr)));
	  // add restriction
	  IsRestrictionOf restrOf = graph.createIsRestrictionOf(restr, restrExpr);
	  restrOf.setSourcePositions((createSourcePositionList(lengthRestr, offsetRestr)));
	  $result = restrExpr;
   }
  ) | /*value access without restriction */ )
;



identifier returns [Identifier result]
: i=IDENT 
{  
   $result = graph.createIdentifier();
   $result.setName(i.getText());
};



/** matches first argument of value-accesses
	@return vertex representing the value-Access-Expression
*/
valueAccess returns [Expression result = null]
@init{
	int offset = 0;
	int length = 0;
}
	:
	{offset = getLTOffset(); }
        expr = primaryExpression
        {$result = expr; length = getLTLength(offset); }
		(
		    ( LBRACK primaryPathDescription) => /*nothin*/ () |
			(DOT | LBRACK) => ( expr = valueAccess2[expr, offset, length] {$result = expr;})
			| /* nothing */
		)
	;

/** matches operator and 2nd argument of valueAccess
	@param arg1 first argument-expression
	@param offsetArg1 offset of first argument-expression
	@param lengthArg1 length of first argument-expression
	@return vertex representing the value-access-expression
*/
valueAccess2[Expression arg1, int offsetArg1, int lengthArg1] returns [Expression result = null]
@init{
    String name = "nthElement";
    int offsetArg2 = 0;
    int offsetOperator = 0;
    int lengthArg2 = 0;
    int lengthOperator = 0;
}
:	{ offsetOperator = getLTOffset(); }
    (   (	DOT
       		{
       			lengthOperator = 1;
       			offsetArg2 = getLTOffset();
       		}
    		arg2Token = (IDENT | FUNCTIONID)
            {
            	name = "getValue";
            	lengthArg2 = getLTLength(offsetArg2);
            	arg2 = graph.createIdentifier();
  				((Identifier)arg2).setName(arg2Token.getText());
            	//((Identifier) arg2).getName().length();
            }
        )
	| 	(	LBRACK
        	{ offsetArg2 = getLTOffset(); }
			arg2 = expression
			{
           		name = "nthElement";
               	lengthArg2 =  getLTLength(offsetArg2);
            }
            RBRACK
            {
               	lengthOperator = getLTLength(offsetOperator);
            }
		)
    )
	{$result = createFunctionIdAndArgumentOf(getFunctionId(name), offsetOperator, lengthOperator, arg1, offsetArg1, lengthArg1, arg2, offsetArg2, lengthArg2, true);}
	(	(LBRACK primaryPathDescription) => /*nothin*/ |
		(DOT|LBRACK) => (expr = valueAccess2[$result, offsetArg1, getLTLength(offsetArg1)] {$result = expr;})
		|
	)
;

/** matches primary expressions (elementset, literal,
	valueConstruction, functionAppl., subgraph, simpleQuery, cfGrammar, variable)
*/
primaryExpression returns [Expression result = null] 
:
(   
    ((LPAREN) => LPAREN parenthesedExpression {$result = $parenthesedExpression.result;} )
|	(rangeExpression {$result = $rangeExpression.expr;})
|	(altPath = alternativePathDescription {$result = altPath;})
| 	(functionApplication {$result = $functionApplication.expr;})
|	(valueConstruction {$result = $valueConstruction.result;})
|	(variable {$result = $variable.var;})
| 	(graphRangeExpression {$result = $graphRangeExpression.expr;})
|	(literal {$result = $literal.literal;})
|   (simpleQuery {$result = $simpleQuery.comprehension;}))
;


parenthesedExpression returns [Expression result]
:
(alternativePathDescription RPAREN) => (alternativePathDescription RPAREN {$result = $alternativePathDescription.result;} ) 
|   (expression RPAREN {$result = $expression.result;} )
;

// parenthesedExpression returns [Expression result]
// :
// (alternativePathDescription RPAREN {$result = $alternativePathDescription.result;}) 
// |   (expression RPAREN {$result = $expression.result;} )
// ;


/** matches a pathdescription
*/
pathDescription returns [PathDescription result = null]
:
alternativePathDescription
{$result = $alternativePathDescription.result;}
;


/** matches an alternative pathdescription
	@return
*/
alternativePathDescription returns [PathDescription result = null] 
@init {
	PathDescription pathDescr = null;
	int offsetPart1 = 0;
	int lengthPart1 = 0;
	int offsetPart2 = 0;
	int lengthPart2 = 0;
}
:
  {offsetPart1 = getLTOffset(); }
  part1 = intermediateVertexPathDescription
  {
    lengthPart1 = getLTLength(offsetPart1);
    $result = part1;
  }
(BOR
  {offsetPart2 = getLTOffset(); }
  part2 = alternativePathDescription
  {
	$result = addPathElement(AlternativePathDescription.class, IsAlternativePathOf.class, pathDescr, part1, offsetPart1, lengthPart1, part2, offsetPart2, lengthPart2);
})?
;

	   
	   
intermediateVertexPathDescription returns [PathDescription result = null] 
@init {
	PathDescription pathDescr = null;
	int offsetPart1 = 0;
	int lengthPart1 = 0;
	int offsetPart2 = 0;
	int lengthPart2 = 0;
	int offsetExpr = 0;
	int lengthExpr = 0;
}
:
  {offsetPart1 = getLTOffset(); }
  part1 = sequentialPathDescription
  {
    $result = part1;
    lengthPart1 = getLTLength(offsetPart1);
  }
  /* Removing the following syntactiv predicate doesn't work to to nondeterminism */
( (restrictedExpression sequentialPathDescription) => 
  (
    {offsetExpr = getLTOffset(); }
    restrExpr = restrictedExpression
    {
     	lengthExpr = getLTLength(offsetExpr);
  		offsetPart2 = getLTOffset(); 
  	}
  	part2 = intermediateVertexPathDescription
  	{
		$result = (IntermediateVertexPathDescription) addPathElement(IntermediateVertexPathDescription.class, IsSubPathOf.class, pathDescr, part1, offsetPart1, lengthPart1, part2, offsetPart2, lengthPart2);
		IsIntermediateVertexOf intermediateVertexOf = graph.createIsIntermediateVertexOf(restrExpr, (IntermediateVertexPathDescription)$result);
		intermediateVertexOf.setSourcePositions((createSourcePositionList(lengthExpr, offsetExpr )));
  	}
  ) | /* only sequential path description*/ )		
;


sequentialPathDescription returns [PathDescription result = null] 
@init {
	PathDescription pathDescr = null;
	int offsetPart1 = 0;
	int lengthPart1 = 0;
	int lengthPart2 = 0;
	int offsetPart2 = 0;
}
:
  {offsetPart1 = getLTOffset(); }
  part1 = startRestrictedPathDescription
  {
    $result = part1;
    lengthPart1 = getLTLength(offsetPart1);
  }
    /* Removing the following syntactiv predicate doesn't work to to nondeterminism */
( /*{offsetPart2 = getLTOffset(); } TODO */
  (sequentialPathDescription) =>
  (part2 = sequentialPathDescription
  {
	$result = addPathElement(SequentialPathDescription.class, IsSequenceElementOf.class, pathDescr, part1, offsetPart1, lengthPart1, part2, offsetPart2, lengthPart2);
  }) | /* no second part */)  	
;


startRestrictedPathDescription returns [PathDescription result = null] 
@init{
	int offset = 0;
	int length = 0;
}
:
( (LCURLY) => (LCURLY
  (
  	   (typeId) => (typeIds = typeExpressionList)
    |  (expression) => (expr = expression)
       
  )
  RCURLY
  AMP)
| )
pathDescr = goalRestrictedPathDescription
{
  	if (expr != null) {
		IsStartRestrOf startRestrOf = graph.createIsStartRestrOf(expr, pathDescr);
		startRestrOf.setSourcePositions((createSourcePositionList(length, offset)));
	} else {
		if(typeIds != null)
		for (VertexPosition t : typeIds) {
			IsStartRestrOf startRestrOf = graph.createIsStartRestrOf((Expression)t.node, pathDescr);
			startRestrOf.setSourcePositions((createSourcePositionList(t.length, t.offset)));
		}
	}
	$result = pathDescr;
}
;	   
	
goalRestrictedPathDescription returns [PathDescription result = null] 
@init{
	int offset = 0;
	int length = 0;
}
:
iteratedOrTransposedPathDescription
{$result = $iteratedOrTransposedPathDescription.result;}
((AMP) => 
  (AMP
	LCURLY
	( ((typeId) =>typeIds = typeExpressionList
		{
			if (typeIds != null)
           	for (VertexPosition t : typeIds) {
    			IsGoalRestrOf goalRestrOf = graph.createIsGoalRestrOf((Expression)t.node, $result);
    			goalRestrOf.setSourcePositions((createSourcePositionList(t.length, t.offset)));
    		}
   	 	}
   	  ) | (
   	      { offset = getLTOffset(); }
		  expr = expression
		  {
			 length = getLTLength(offset);
   			 IsGoalRestrOf goalRestrOf = graph.createIsGoalRestrOf(expr, $result);
    		 goalRestrOf.setSourcePositions((createSourcePositionList(length, offset)));
    	  }
		) // ende expr
	)
	RCURLY
  )
|)
;


iteratedOrTransposedPathDescription	returns [PathDescription result = null]
@init{
   	PathDescription pathDesc = null;
   	int offsetPath = 0;
 	int lengthPath = 0;
}
:
{offsetPath = getLTOffset();}
primaryPathDescription
{ 
  $result = $primaryPathDescription.result;
  lengthPath = getLTLength(offsetPath);
}
(
  (STAR | PLUS | CARET) 
  => (iteration[$result, offsetPath, lengthPath] {$result = $iteration.result;})
 | /* no iteration */
) 
;


iteration[PathDescription iteratedPath, int offsetPath, int lengthPath] returns [PathDescription result = null] 
@init{
   	String iteration = null;
 	int offsetExpr = 0;
 	int lengthExpr = 0;
}
:
( (STAR| PLUS) =>	
  	(( STAR {iteration = "star"; } | PLUS {iteration ="plus";} )
      {
		IteratedPathDescription ipd = graph.createIteratedPathDescription();
	    ((IteratedPathDescription)ipd).setTimes(iteration);
	    IsIteratedPathOf iteratedPathOf = graph.createIsIteratedPathOf(iteratedPath, ipd);
	    iteratedPathOf.setSourcePositions((createSourcePositionList(lengthPath, offsetPath)));
	    $result = ipd;
      }
      ( 
  		(STAR | PLUS | CARET) 
  		=> (tempPathDescr = iteration[$result, offsetPath, lengthPath+1] {$result = tempPathDescr;})
  		|
	  )  
    )
| ( (CARET) => (CARET
		(	T  // transponatedPath:
           	{
				TransposedPathDescription tpd = graph.createTransposedPathDescription();
                IsTransposedPathOf transposedPathOf = graph.createIsTransposedPathOf(iteratedPath, tpd);
	            transposedPathOf.setSourcePositions((createSourcePositionList(lengthPath, offsetPath)));
	            $result = tpd;
            }
        | (// exponentedPath:
            { offsetExpr = getLTOffset(); }
            ( (i=INTLITERAL) | (i=DIGITSEQUENCE))
            { 
                lengthExpr = getLTOffset();
			 	ExponentiatedPathDescription epd = graph.createExponentiatedPathDescription();
		        IsExponentiatedPathOf exponentiatedPathOf = graph.createIsExponentiatedPathOf(iteratedPath, epd);
	            exponentiatedPathOf.setSourcePositions((createSourcePositionList(lengthPath, offsetPath)));
	            IntLiteral exponent = graph.createIntLiteral();
				exponent.setIntValue(Integer.parseInt(i.getText()));
	            IsExponentOf exponentOf = graph.createIsExponentOf(exponent, epd);
	            exponentOf.setSourcePositions((createSourcePositionList(lengthExpr, offsetExpr)));
	            $result = epd;
            }
          )
		)
		( 
 			 (STAR | PLUS | CARET) 
  			 => (tempPathDescr = iteration[$result, offsetPath, lengthPath+lengthExpr] {$result = tempPathDescr;})
  		|
		)  
	)
	)
|	
)

;


primaryPathDescription returns [PathDescription result = null]
@init{
	int offset = 0;
	int length = 0;
}
:
  ( (LPAREN pathDescription) => (LPAREN pathDescr = pathDescription {$result = pathDescr;} RPAREN )
  | (simplePathDescription) => (pathDescr = simplePathDescription  {$result = pathDescr;})
  | (aggregationPathDescription) => (pathDescr = aggregationPathDescription  {$result = pathDescr;})
  | (edgePathDescription) => (pathDescr = edgePathDescription    {$result = pathDescr;})
      | (LBRACK) => (LBRACK
      { offset = getLTOffset(); }
      pathDescr = pathDescription
      { length = getLTLength(offset); }
      RBRACK
      {
	    OptionalPathDescription optPathDescr = graph.createOptionalPathDescription();
		IsOptionalPathOf optionalPathOf = graph.createIsOptionalPathOf(pathDescr, optPathDescr);
		optionalPathOf.setSourcePositions((createSourcePositionList(length, offset)));
	    $result = optPathDescr;
      }
    ) 
  )
;




/** matches a simle pathdescription consisting of an arrow simple
	and eventually a restriction. 
*/
simplePathDescription returns [PrimaryPathDescription result = null]
@init{
    Direction dir;
    String direction = "any";
    int offsetDir = 0;
}
:
{offsetDir = getLTOffset();}
((RARROW) => (RARROW { direction = "out"; }) 
 |(LARROW) => (LARROW { direction = "in"; }) 
 | ARROW)
/* edge type restriction */
(   (LCURLY (edgeRestrictionList)? RCURLY ) =>
      (LCURLY (typeIds = edgeRestrictionList)?	RCURLY)
| /* empty */ )
{
    $result = graph.createSimplePathDescription();
	dir = (Direction)graph.getFirstVertexOfClass(Direction.class);
	while (dir != null ) {
    	if (!dir.getDirValue().equals(direction)) {
	        dir = dir.getNextDirection();
	    } else { 
	    	break;
	    }	
	}    	
	if (dir == null) {
		dir = graph.createDirection();
	       dir.setDirValue(direction);
	}
	IsDirectionOf directionOf = graph.createIsDirectionOf(dir, (PrimaryPathDescription) $result);
	directionOf.setSourcePositions((createSourcePositionList(0, offsetDir)));
	if (typeIds != null)
	    for (VertexPosition t : typeIds) {
			IsEdgeRestrOf edgeRestrOf = graph.createIsEdgeRestrOf((EdgeRestriction)t.node, (PrimaryPathDescription) $result);
			edgeRestrOf.setSourcePositions((createSourcePositionList(t.length, t.offset)));
		}	
}
;



/** matches a an aggregation pathdescription consisting of an arrow simple
	and eventually a restriction. 
*/
aggregationPathDescription returns [AggregationPathDescription result = null]
@init{
    boolean outAggregation = true;
    int offsetDir = 0;
}
:
{offsetDir = getLTOffset();}
((OUTAGGREGATION) =>(OUTAGGREGATION) | (INAGGREGATION { outAggregation = false; })
)
/* edge type restriction */
(   (LCURLY (edgeRestrictionList)? RCURLY ) =>
      (LCURLY (typeIds = edgeRestrictionList)?	RCURLY)
| /* empty */ )
{
    $result = graph.createAggregationPathDescription();
	$result.setOutAggregation(outAggregation);
	if (typeIds != null)
	    for (VertexPosition t : typeIds) {
			IsEdgeRestrOf edgeRestrOf = graph.createIsEdgeRestrOf((EdgeRestriction)t.node, (PrimaryPathDescription) $result);
			edgeRestrOf.setSourcePositions((createSourcePositionList(t.length, t.offset)));
		}	
}
;


/** matches a edgePathDescription, i.e. am edge as part of a pathdescription
	@return
*/
edgePathDescription returns [EdgePathDescription result = null] 
@init{
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
{offsetDir = getLTOffset();}
/* TODO: insert here for aggregation */
(EDGESTART	{ edgeStart = true; } | EDGE)
{offsetExpr = getLTOffset();}
expr = expression
{lengthExpr = getLTLength(offsetExpr);}
(EDGEEND { edgeEnd = true; }| EDGE)
{
	lengthExpr = getLTLength(offsetExpr);
    $result = graph.createEdgePathDescription();
	if (edgeStart && !edgeEnd) 
		direction = "in";
	else if (!edgeStart  && edgeEnd)
        direction = "out";
	dir = (Direction)graph.getFirstVertexOfClass(Direction.class);
	while (dir != null ) {
   		if (! dir.getDirValue().equals(direction)) {
       			dir = dir.getNextDirection();
	    } else {
	     	break;
	    }	
	}    
	if (dir == null) {
		dir = graph.createDirection();
	       dir.setDirValue(direction);
	}
	IsDirectionOf directionOf = graph.createIsDirectionOf(dir, $result);
	directionOf.setSourcePositions((createSourcePositionList(lengthDir, offsetDir)));
	IsEdgeExprOf edgeExprOf = graph.createIsEdgeExprOf(expr, $result);
	edgeExprOf.setSourcePositions((createSourcePositionList(lengthExpr, offsetExpr)));
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
    FunctionId functionId = null;
    int offset = 0;
    int length = 0;
}
:
(FUNCTIONID (LCURLY | LPAREN)) => (
{
offset = getLTOffset();}
f=FUNCTIONID
{length = getLTLength(offset);}
(LCURLY	(typeIds = typeExpressionList)? RCURLY )?
LPAREN (expressions = expressionList)? RPAREN
{
    expr = graph.createFunctionApplication();
    // retrieve function id or create a new one
    functionId = getFunctionId(f.getText());
	IsFunctionIdOf  functionIdOf = graph.createIsFunctionIdOf(functionId, expr);
    functionIdOf.setSourcePositions((createSourcePositionList(f.getCharPositionInLine()-1, f.getText().length())));
    if (typeIds != null)
    	for (VertexPosition t : typeIds) {
			IsTypeExprOf typeOf = graph.createIsTypeExprOfFunction((Expression)t.node, expr);
			typeOf.setSourcePositions((createSourcePositionList(t.length, t.offset)));
		}
	if (expressions != null)	
		for (VertexPosition ex : expressions) {
			IsArgumentOf argOf = graph.createIsArgumentOf((Expression)ex.node,expr);
			argOf.setSourcePositions((createSourcePositionList(ex.length, ex.offset)));
		}
})
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
valueConstruction returns [Expression result = null]
	:
	(	expr = bagConstruction 
	  | expr = listConstruction
	  |	expr = pathConstruction 
	  | expr = pathsystemConstruction
	  | expr = recordConstruction
	  | expr = setConstruction 
	  | expr = mapConstruction
	  | expr = tupleConstruction)
	  {$result = expr;}
	;


/**	matches a bag construction
*/
bagConstruction returns [ValueConstruction valueConstr = null]
:
	BAG
	LPAREN ( expressions = expressionList )? RPAREN
    {$valueConstr = createPartsOfValueConstruction(expressions, graph.createBagConstruction()); }
;
	
setConstruction returns [ValueConstruction valueConstr = null]
:
	SET
	LPAREN ( expressions = expressionList )? RPAREN
    {$valueConstr = createPartsOfValueConstruction(expressions, graph.createSetConstruction()); }
;

/** matches a tupel construction
*/
tupleConstruction returns [ValueConstruction valueConstr = null]
:
	TUP
	LPAREN
	expressions = expressionList
	RPAREN
    {$valueConstr = createPartsOfValueConstruction(expressions, graph.createTupleConstruction()); }
 ;
 
/** matches a tupel construction
*/
mapConstruction returns [MapConstruction valueConstr = null]
@init {
  int offsetKey = 0;
  int offsetValue = 0;
  int lengthKey = 0;
  int lengthValue = 0;
  IsKeyExprOfConstruction keyEdge = null;
  IsValueExprOfConstruction valueEdge = null;
}
:   (MAP) => (
    MAP
	LPAREN
	{
	   $valueConstr = graph.createMapConstruction();
    }

	  ((
	    {offsetKey = getLTOffset();}
		keyExpr = expression
		{lengthKey = getLTLength(offsetKey);}
		EDGEEND
	    {offsetValue = getLTOffset();}
		valueExpr = expression
		{
			lengthValue = getLTLength(offsetValue);
			keyEdge = graph.createIsKeyExprOfConstruction(keyExpr, $valueConstr);
			keyEdge.setSourcePositions((createSourcePositionList(lengthKey, offsetKey)));	  
			valueEdge = graph.createIsValueExprOfConstruction(valueExpr, $valueConstr);
			valueEdge.setSourcePositions((createSourcePositionList(lengthValue, offsetValue)));	
		}
		((COMMA) => (
			COMMA
	    	{offsetKey = getLTOffset();}
			keyExpr = expression
			{lengthKey = getLTLength(offsetKey);}
			EDGEEND
		    {offsetValue = getLTOffset();}
			valueExpr = expression
			{
				lengthValue = getLTLength(offsetValue);
				keyEdge = graph.createIsKeyExprOfConstruction(keyExpr, $valueConstr);
				keyEdge.setSourcePositions((createSourcePositionList(lengthKey, offsetKey)));	  
				valueEdge = graph.createIsValueExprOfConstruction(valueExpr, $valueConstr);
				valueEdge.setSourcePositions((createSourcePositionList(lengthValue, offsetValue)));	
			})
	  	)*	 
	  ) | )
	RPAREN)
 ; 
 
 


listConstruction returns [ValueConstruction valueConstr = null]
@init{
 	int offsetStart = 0;
 	int offsetEnd = 0;
 	int lengthStart = 0;
 	int lengthEnd = 0;
}
:
LIST
LPAREN
{offsetStart = getLTOffset(); }
startExpr = expression
{
 lengthStart = getLTLength(offsetStart);
}
( 
  (DOTDOT) => 
    (DOTDOT 
   		{offsetEnd = getLTOffset();}
   		endExpr = expression
   		{
     		lengthEnd = getLTLength(offsetEnd);
     		$valueConstr = graph.createListRangeConstruction();
     		IsFirstValueOf firstValueOf = graph.createIsFirstValueOf(startExpr, (ListRangeConstruction) valueConstr);
     		firstValueOf.setSourcePositions((createSourcePositionList(lengthStart, offsetStart)));
     		IsLastValueOf lastValueOf = graph.createIsLastValueOf(endExpr, (ListRangeConstruction) valueConstr);
     		lastValueOf.setSourcePositions((createSourcePositionList(lengthEnd, offsetEnd)));
   		}
  	)
  |
  (
    ((COMMA) => (COMMA exprList = expressionList) | )
    	{
  			ArrayList<VertexPosition> allExpressions = new ArrayList<VertexPosition>();
  			VertexPosition v = new VertexPosition();
		  	v.length = lengthStart;
	    	v.node = startExpr;
		    allExpressions.add(v);
		    if (exprList != null)
	    		allExpressions.addAll(exprList);
	    	$valueConstr = createPartsOfValueConstruction(allExpressions, graph.createListConstruction());
		}
  )  
)  
RPAREN
;



recordConstruction returns [ValueConstruction valueConstr = null]
:
	REC
	LPAREN
	elements = recordElementList
	RPAREN
    {
		$valueConstr = graph.createRecordConstruction();
		if (elements != null)
		for (VertexPosition expr : elements) {
			IsRecordElementOf exprOf = graph.createIsRecordElementOf((RecordElement)expr.node, (RecordConstruction) valueConstr);
			exprOf.setSourcePositions((createSourcePositionList(expr.length, expr.offset)));
		}
    }
;



recordElementList returns [ArrayList<VertexPosition> elements = new ArrayList<VertexPosition>()]
@init{
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
:
	PATH
	LPAREN
	expressions = expressionList
	RPAREN
    {createPartsOfValueConstruction(expressions, graph.createPathConstruction()); }
;
	

pathsystemConstruction returns [PathSystemConstruction pathsystemConstr = null] 
@init{
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
        	{ offsetEVList = input.LT(1).getCharPositionInLine()-1; }
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
    int offsetConstraint = 0;
    int offsetSubgraph = 0;
    int lengthConstraint = 0;
    int lengthSubgraph = 0;
}
:
/* Comment: Refactor rule */
declarations = declarationList
{declaration = (Declaration) createMultipleEdgesToParent(declarations, graph.createDeclaration(), IsSimpleDeclOf.class);}
(COMMA
	{
    	offsetConstraint = getLTOffset(); 
	}
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
@init {
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
((COMMA simpleDeclaration) => (COMMA tail = declarationList
 {	declList.addAll(tail); }
) | /* no further declaration */ )
;



simpleDeclaration returns [SimpleDeclaration simpleDecl = null]
@init{
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
    VertexPosition v = new VertexPosition();
}
:  
{ v.offset = getLTOffset();}
expr = expression
{
  	v.length = getLTLength(v.offset);;
    v.node = expr;
    expressions.add(v);
}
((COMMA) => (COMMA
	exprList = expressionList
	{expressions.addAll(exprList);}
)|)
;



rangeExpression returns [Expression expr = null]
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
:
(	VSUBGRAPH { expr = graph.createVertexSubgraphExpression(); }
|	ESUBGRAPH { expr = graph.createEdgeSubgraphExpression(); }
)
LCURLY typeIds = typeExpressionList	RCURLY
{createMultipleEdgesToParent(typeIds, expr, IsTypeRestrOf.class);}
;


typeExpressionList returns [ArrayList<VertexPosition> typeIdList = new ArrayList<VertexPosition>()] 
@init{
    VertexPosition type = new VertexPosition();
}
:
{ type.offset = getLTOffset(); }
v = typeId
{
    type.node = v;
    type.length = getLTLength(type.offset);
    typeIdList.add(type);
}
(COMMA list = typeExpressionList {typeIdList.addAll(list);} )?
;



qualifiedName returns [String name = null]
: 
i=(IDENT | FUNCTIONID) {name = i.getText();}
( 
  (DOT (IDENT|FUNCTIONID)) 
  => ( 
       DOT
	   newName = qualifiedName
	   {name = name + "." + newName;}
     ) | /* qualified name finished */
)
;


typeId returns [TypeId type = null] 
:
{type = graph.createTypeId();}
(CARET	{ type.setExcluded(true); } )?
( s = qualifiedName )
{ type.setName(s); }
(EXCL	{ type.setType(true);  })?
;


numericLiteral returns [Expression literal = null]
@init {
	String textRepresentation = null;
	int base = 10;
	boolean isRealLiteral = false;
	int offset = -1;
}
:
   {offset = getLTOffset();}
   (
      (REALLITERAL) => (
        /* real literal */
        token = REALLITERAL
        {textRepresentation = token.getText(); isRealLiteral=true;}
      )
      |
      (INTLITERAL) => (
        /* int literal */
        token = INTLITERAL
        {textRepresentation = token.getText();}
      )
      |
      (HEXLITERAL) => (
        /* hex literal */
        token = HEXLITERAL
        {
          textRepresentation = token.getText().substring(2);
          base=16;
        }
      )
      |
   	  (
   	    /* numeric literal that could not be recognized by lexer */
   	    token = DIGITSEQUENCE
   	    {textRepresentation = token.getText();}
   	    (
   	      DOT
   	      ((token = REALLITERAL) | (token = DIGITSEQUENCE))
   	      {textRepresentation += "." + token.getText(); isRealLiteral=true;}   
   	    )?
   	  )
   	)  
	{
		if (isRealLiteral) {
			literal = graph.createRealLiteral();
			((RealLiteral) literal).setRealValue(Double.parseDouble(textRepresentation));
		} else {
	    	literal = graph.createIntLiteral();
	    	/* check for octal literal*/
	    	if ((base==10) && textRepresentation.startsWith("0") && !textRepresentation.startsWith("0x")) {
	    	  base = 8;
	    	}
			((IntLiteral) literal).setIntValue(Integer.parseInt(textRepresentation, base));
		}	
	}
	
;	


enumLiteral returns [Expression result = null]
:
    (enumType=(IDENT | FUNCTIONID)) ENUM_SEPARATOR (enumField=(IDENT | FUNCTIONID))
    {
        Literal typeLiteral = graph.createStringLiteral();
	System.out.println("TypText: " + enumType.getText());
       	((StringLiteral) typeLiteral).setStringValue(decode(enumType.getText()));
	Literal fieldLiteral = graph.createStringLiteral();
       	((StringLiteral) fieldLiteral).setStringValue(decode(enumField.getText()));
        FunctionId id = getFunctionId("enumConstant");
        $result = createFunctionIdAndArgumentOf(id, 0, 0, typeLiteral, 0, 0, fieldLiteral, 0, 0, true);
    }
;




literal returns [Expression literal = null]
:
	(token=STRING_LITERAL
    {
       	literal = graph.createStringLiteral();
       	((StringLiteral) literal).setStringValue(decode(token.getText()));
    })
|	(THISVERTEX
    {
  /*     literal = graph.getFirstThisLiteral();
       if (literal != null)	
	       	return literal;*/
       literal = graph.createThisVertex();
    })
|	(THISEDGE
    {
     	/*literal = graph.getFirstThisEdge();
        if (literal != null)
	      	return literal;*/
        literal = graph.createThisEdge();
    })
|	(TRUE
    {
        literal = (Literal) graph.getFirstVertexOfClass(BoolLiteral.class);
        while ( (literal != null) && ( ((BoolLiteral) literal).getBoolValue() != TrivalentBoolean.TRUE))
        	literal = (BoolLiteral) literal.getNextVertexOfClass(BoolLiteral.class);
        if (literal == null || ( ((BoolLiteral) literal).getBoolValue() != TrivalentBoolean.TRUE )) {
		   	literal = graph.createBoolLiteral();
				((BoolLiteral) literal).setBoolValue(TrivalentBoolean.TRUE);
		}
    })
|	(FALSE
    {
        literal = (Literal) graph.getFirstVertexOfClass(BoolLiteral.class);
        while ((literal != null) && (((BoolLiteral) literal).getBoolValue() != TrivalentBoolean.FALSE))
        	literal = (BoolLiteral) literal.getNextVertexOfClass(BoolLiteral.class);
   	    if (literal == null || ( ((BoolLiteral) literal).getBoolValue() != TrivalentBoolean.FALSE )) {
		   	literal = graph.createBoolLiteral();
		   	((BoolLiteral) literal).setBoolValue(TrivalentBoolean.FALSE);
		}
    })
|	(NULL_VALUE
    {
        literal = (Literal) graph.getFirstVertexOfClass(NullLiteral.class);
	    if (literal == null)
		    literal = graph.createNullLiteral(); 
    })
|	(numericLiteral {$literal = $numericLiteral.literal;})    
|       (enumLiteral {$literal = $enumLiteral.result;})
;
	

edgeVertexList returns [EdgeVertexList eVList = null]
@init{
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
	


expressionOrPathDescription returns [Expression result = null]
:
    (  (pathDescription expression) => (expr = expression {$result = expr;})
      | (expression) => (expr = expression {$result = expr;})
      | (pathDescription {$result = $pathDescription.result;})
    )
;
	


edgeRestrictionList returns [ArrayList<VertexPosition> list = new ArrayList<VertexPosition>()] 
@init{
	VertexPosition v = new VertexPosition();
	EdgeRestriction er = null;
	int offsetType = 0;
	int offsetRole = 0;
	int lengthType = 0;
	int lengthRole = 0;
}
	:
	(	(
			{offsetRole = getLTOffset(); }
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


labeledReportList returns [Comprehension result = null]
@init{
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
            	lengthAsExpr = getLTLength(offsetAsExpr);
            	hasLabel = true;
            }
		)?
        {
            result = graph.createBagComprehension();
			tupConstr = graph.createTupleConstruction();
			e = graph.createIsCompResultDefOf(tupConstr, result);
			IsPartOf partOf = graph.createIsPartOf((Expression)expr, tupConstr);
			partOf.setSourcePositions((createSourcePositionList(lengthExpr, offsetExpr)));
		  	if (hasLabel) {
			    IsTableHeaderOf tableHeaderOf = graph.createIsTableHeaderOf(asExpr, (ComprehensionWithTableHeader)result);
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
				    IsTableHeaderOf tableHeaderOf = graph.createIsTableHeaderOf(asExpr,(ComprehensionWithTableHeader) result);
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
	
	

reportClause returns [Comprehension comprehension = null] 
@init{
	int offset = 0;
	int length = 0;
	boolean vartable = false;
	boolean map = false;
}
	:
	(	REPORT
		labeledReportList {$comprehension = $labeledReportList.result;}
	)
	|
	(	 REPORTBAG   {$comprehension = graph.createBagComprehension();}
	   | REPORTSET   {$comprehension = graph.createSetComprehension(); }  
	   | REPORTTABLE {$comprehension = graph.createTableComprehension(); vartable = true; }
	   | REPORTMAP   {$comprehension = graph.createMapComprehension(); map = true; }
	)
    { offset = getLTOffset(); }
	reportList = expressionList
    {
        length = getLTLength(offset);
		IsCompResultDefOf e = null;
	    if (map) {
	       	if (reportList.size() != 2) {
	       	    throw new ParseException("reportMap keyExpr, valueExpr must be followed by exactly two arguments", "reportMap", new SourcePosition(length, offset));
	       	}
			IsKeyExprOfComprehension keyEdge = graph.createIsKeyExprOfComprehension((Expression) reportList.get(0).node, (MapComprehension) $comprehension);
			IsValueExprOfComprehension valueEdge = graph.createIsValueExprOfComprehension((Expression)reportList.get(1).node, (MapComprehension) $comprehension);  
			keyEdge.setSourcePositions(createSourcePositionList(reportList.get(0).length, reportList.get(0).offset));
			valueEdge.setSourcePositions(createSourcePositionList(reportList.get(1).length, reportList.get(1).offset));
	    } else if (vartable) {
	       if ((reportList.size() != 3) && (reportList.size() != 4))
	            throw new ParseException("reportTable columHeaderExpr, rowHeaderExpr, cellContent [,tableHeader] must be followed by three or for arguments", "reportTable", new SourcePosition(length, offset));
					// size == 3, set columnHeader and rowHeader
	       IsColumnHeaderExprOf cHeaderE = graph.createIsColumnHeaderExprOf((Expression)(reportList.get(0)).node, (TableComprehension)comprehension);
	       cHeaderE.setSourcePositions((createSourcePositionList((reportList.get(0)).length, (reportList.get(0)).offset)));
	       IsRowHeaderExprOf rHeaderE = graph.createIsRowHeaderExprOf((Expression)(reportList.get(1)).node, (TableComprehension)comprehension);
	       rHeaderE.setSourcePositions((createSourcePositionList((reportList.get(1)).length, (reportList.get(1)).offset)));
	       e = graph.createIsCompResultDefOf((Expression)(reportList.get(2)).node, comprehension);
	       e.setSourcePositions(createSourcePositionList((reportList.get(2)).length, (reportList.get(2)).offset));

		   // tableheader
	       if (reportList.size() == 4) {
	           	IsTableHeaderOf tHeaderE = graph.createIsTableHeaderOf((Expression)(reportList.get(3)).node, (ComprehensionWithTableHeader)comprehension);
	           	tHeaderE.setSourcePositions((createSourcePositionList((reportList.get(3)).length, (reportList.get(3)).offset)));
	       }
	 	} else {
	 		if (reportList.size() > 1) {
			  	TupleConstruction tupConstr = (TupleConstruction) createMultipleEdgesToParent(reportList, graph.createTupleConstruction(), IsPartOf.class);
	       	   	e = graph.createIsCompResultDefOf(tupConstr, comprehension);
	       	} else {
		    	e = graph.createIsCompResultDefOf((Expression)(reportList.get(0)).node, comprehension);
			}
			e.setSourcePositions((createSourcePositionList(length, offset)));
	 	}
	}
;	
		

simpleQuery returns [Comprehension comprehension = null]
@init{
    Declaration declaration = null;
    int offsetDecl = 0;
    int lengthDecl = 0;
    int offsetSubgraph = 0;
    int lengthSubgraph = 0;
    int offsetConstraint = 0;
    int lengthConstraint = 0;
    int offsetResult = 0;
    int lengthResult = 0;
}
	:
		// declaration part
		FROM
		declarations = declarationList
        {
        	//TODO dbildh 21.11.08 : check if this can be replaced by call of declaration
        	declaration = graph.createDeclaration();
        	if (declarations.size() > 0) {
        		offsetDecl = declarations.get(0).offset;
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
		reportClause {$comprehension = $reportClause.comprehension;}
        {
		    lengthResult = getLTLength(offsetResult);
	   		IsCompDeclOf comprDeclOf = graph.createIsCompDeclOf(declaration, comprehension);
	   		comprDeclOf.setSourcePositions((createSourcePositionList(lengthDecl, offsetDecl)));
		}
		END
	;		
			

/* Original rule */		
pathExpression returns [Expression result = null] 
@init{
	int offsetArg1 = 0;
	int lengthArg1 = 0;
}		
:
/* AlternativePathDescrition as path of backwardVertexSet or backwardPathSystem */
(alternativePathDescription (SMILEY | restrictedExpression)) => 
  ( regBackwardVertexSetOrPathSystem  {$result = $regBackwardVertexSetOrPathSystem.result; })
|
(restrictedExpression) => (
  {offsetArg1 = getLTOffset();}
  expr = restrictedExpression
  {
    lengthArg1 = getLTLength(offsetArg1);
    $result = expr;
  }
  (
    /* AlternativePathDescription as path of forwardVertexSet, pathExistence */
    (alternativePathDescription) => 
       (regPathExistenceOrForwardVertexSet[expr, offsetArg1, lengthArg1] {$result = $regPathExistenceOrForwardVertexSet.result;} )
    |
    /* AlternativePathDescription as path of forwardPathSystem */
    (SMILEY) => 
       (regPathOrPathSystem[expr, offsetArg1, lengthArg1] {$result = $regPathOrPathSystem.result; })
    |
      /* pure restricted expression */
  )   
)
|
/* AlternativePathDescription as vertexPairs */
  (alternativePathDescription {$result = $alternativePathDescription.result;})
;


regPathExistenceOrForwardVertexSet[Expression expr, int offsetArg1, int lengthArg1] returns [Expression result = null]
@init{
	int offsetPathDescr = 0;
	int offsetExpr = 0;
	int lengthPathDescr = 0;
	int lengthExpr = 0;
}
	:
	{
	  offsetExpr = offsetArg1;
	  offsetPathDescr = getLTOffset();
	}
	pathDescr = pathDescription
    {lengthPathDescr = getLTLength(offsetPathDescr);}
	( 	(primaryExpression) => (
       	{ offsetExpr = getLTOffset(); }
       	restrExpr = restrictedExpression
        { lengthExpr = getLTLength(offsetExpr); })
	| /* forward vertex set */
	)
	{
		if (restrExpr != null) {
			// create new pathexistence
			PathExistence pe = graph.createPathExistence();
			$result = pe;
					
			// add start vertex
			IsStartExprOf startVertexOf = graph.createIsStartExprOf(expr, pe);
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
			$result  = fvs;
			// add start expr
			IsStartExprOf startVertexOf = graph.createIsStartExprOf(expr, fvs);
			startVertexOf.setSourcePositions((createSourcePositionList(lengthArg1, offsetArg1)));
			// add pathdescr
			IsPathOf pathOf = graph.createIsPathOf(pathDescr, fvs);
			pathOf.setSourcePositions((createSourcePositionList(lengthPathDescr, offsetPathDescr)));
		}
	}
;	
	


regPathOrPathSystem[Expression arg1, int offsetArg1, int lengthArg1] returns [Expression result = null]
@init{
    boolean isPath = false;
    int offsetPathDescr = 0;
    int offsetOperator1 = 0;
    int offsetOperator2 = 0;
    int offsetExpr = 0;
    int lengthPathDescr = 0;
    int lengthExpr = 0;
}
:
	{
	   offsetExpr = offsetArg1;
	   offsetOperator1 = getLTOffset(); 
	}
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
		FunctionId funId = getFunctionId("pathSystem"); 
		result = createFunctionIdAndArgumentOf(funId, offsetOperator1, 3, 
		   							  arg1, offsetArg1, lengthArg1, pathDescr, 
		   							  offsetPathDescr, lengthPathDescr, true); 
		if (isPath) {
			arg1 = $result;
			result = createFunctionIdAndArgumentOf(funId, offsetOperator1, 3,
											  arg1, offsetArg1, -offsetArg1 + offsetOperator2 + 3,
											  restrExpr, offsetExpr, lengthExpr, true);
		}
	}
;	
	


regBackwardVertexSetOrPathSystem returns [Expression result = null] 
@init{
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
				FunctionId f = getFunctionId("pathSystem"); 
		        result = createFunctionIdAndArgumentOf(f, offsetOperator, 3, 
				  							  pathDescr, offsetPathDescr, lengthPathDescr, restrExpr, offsetExpr, lengthExpr, true); 	
	        } else {
		    	// create a backwardvertexset
				BackwardVertexSet bs = graph.createBackwardVertexSet();
				result = bs;
	            IsTargetExprOf targetVertexOf = graph.createIsTargetExprOf(restrExpr, bs);
	            targetVertexOf.setSourcePositions((createSourcePositionList(lengthExpr, offsetExpr)));
				IsPathOf pathOf = graph.createIsPathOf(pathDescr, bs);
				pathOf.setSourcePositions((createSourcePositionList(lengthPathDescr, offsetPathDescr)));
	        }
        }
;
