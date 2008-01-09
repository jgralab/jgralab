// $ANTLR 2.7.6 (2005-12-22): "greql2parser.g" -> "Greql2Parser.java"$

package de.uni_koblenz.jgralab.greql2.parser;

import java.util.Vector;
import java.util.*;

import de.uni_koblenz.jgralab.*;
import de.uni_koblenz.jgralab.greql2.schema.*;
import de.uni_koblenz.jgralab.greql2.*;
import de.uni_koblenz.jgralab.greql2.schema.impl.*;
import de.uni_koblenz.jgralab.greql2.exception.*;

import antlr.TokenBuffer;
import antlr.TokenStreamException;
import antlr.TokenStreamIOException;
import antlr.ANTLRException;
import antlr.LLkParser;
import antlr.Token;
import antlr.TokenStream;
import antlr.RecognitionException;
import antlr.NoViableAltException;
import antlr.MismatchedTokenException;
import antlr.SemanticException;
import antlr.ParserSharedInputState;
import antlr.collections.impl.BitSet;

public class Greql2Parser extends antlr.LLkParser       implements Greql2ParserTokenTypes
 {

    private final int VMAX = 100;
    private final int EMAX = 100;
    private Greql2Schema schema = null;
    private Greql2 graph = null;
    private SymbolTable variableSymbolTable = null;
    private SymbolTable functionSymbolTable = null;
    private SymbolTable nonterminalSymbolTable = null;
	private GraphClass graphClass = null;
    private boolean isAdditiveExpression = true;

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
    private void initialize() throws ParseException {
       	schema = Greql2Schema.instance();
		graph = Greql2Impl.create(VMAX,EMAX);
	    variableSymbolTable = new SymbolTable();
		functionSymbolTable = new SymbolTable();
		nonterminalSymbolTable = new SymbolTable();
		functionSymbolTable.blockBegin();
	    graphClass = schema.getGraphClass("Greql2");
  	}

    private void createFunctionIdAndArgumentOf(FunctionApplication fa, FunctionId functionId, int offsetOperator, int lengthOperator, Expression arg1, int offsetArg1, int lengthArg1, Expression arg2, int offsetArg2, int lengthArg2) {
    	IsFunctionIdOf functionIdOf = graph.createIsFunctionIdOf(functionId, fa);
    	functionIdOf.setSourcePositions((createSourcePositionList(lengthOperator, offsetOperator)));
    	IsArgumentOf arg1Of = graph.createIsArgumentOf(arg1, fa);
    	arg1Of.setSourcePositions((createSourcePositionList(lengthArg1, offsetArg1)));
    	IsArgumentOf arg2Of = graph.createIsArgumentOf(arg2, fa);
    	arg2Of.setSourcePositions((createSourcePositionList(lengthArg2, offsetArg2)));
    }	


    private List<SourcePosition> createSourcePositionList(int length, int offset) {
    	List<SourcePosition> list = new ArrayList<SourcePosition>();
    	list.add(new SourcePosition(length, offset));
    	return list;
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
				System.err.println("error: " + offset +": " + e.getMessage());
			else System.err.println("error (offset = -1): " + e.getMessage());
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
				System.err.println("error: " + offset +": " + e.getMessage());
			else System.err.println("error (offset = -1): " + e.getMessage());
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
		IsBoundVarOf isBoundVarOf = root.getFirstIsBoundVarOf(EdgeDirection.IN, true);
		while (isBoundVarOf != null) {
		 	 variableSymbolTable.insert( ((Variable) isBoundVarOf.getAlpha()).getName(), isBoundVarOf.getAlpha());
			 isBoundVarOf = isBoundVarOf.getNextIsBoundVarOf(EdgeDirection.IN, true);
		}
		IsQueryExprOf isQueryExprOf = root.getFirstIsQueryExprOf(EdgeDirection.IN, true);
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
		IsDefinitionOf isDefinitionOf = v.getFirstIsDefinitionOf( EdgeDirection.IN,true);
		while (isDefinitionOf != null) {
			Definition definition = (Definition) isDefinitionOf.getAlpha();
			Variable variable = (Variable) definition.getFirstIsVarOf(EdgeDirection.IN, true).getAlpha();
			variableSymbolTable.insert(variable.getName(), variable);
			isDefinitionOf = isDefinitionOf.getNextIsDefinitionOf(EdgeDirection.IN, true);
		}
		isDefinitionOf = v.getFirstIsDefinitionOf(EdgeDirection.IN, true);
		while (isDefinitionOf != null) {
			Definition definition = (Definition) isDefinitionOf.getAlpha();
			Expression expr = (Expression) definition.getFirstIsExprOf(EdgeDirection.IN, true).getAlpha();
			mergeVariables(expr);
			isDefinitionOf = isDefinitionOf.getNextIsDefinitionOf(EdgeDirection.IN,true);
		}
		Edge isBoundExprOf = v.getFirstIsBoundExprOfDefinition(EdgeDirection.IN, true);
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
		IsSimpleDeclOf isSimpleDeclOf = v.getFirstIsSimpleDeclOf(EdgeDirection.IN, true);
		while (isSimpleDeclOf != null) {
			SimpleDeclaration simpleDecl = (SimpleDeclaration) isSimpleDeclOf.getAlpha();
			IsDeclaredVarOf isDeclaredVarOf = simpleDecl.getFirstIsDeclaredVarOf(EdgeDirection.IN,true);
			while (isDeclaredVarOf != null)	{
				Variable variable = (Variable) isDeclaredVarOf.getAlpha();
				variableSymbolTable.insert(variable.getName(), variable);
				isDeclaredVarOf = isDeclaredVarOf.getNextIsDeclaredVarOf(EdgeDirection.IN, true);
			}
			isSimpleDeclOf = isSimpleDeclOf.getNextIsSimpleDeclOf(EdgeDirection.IN,	true);
		}
		isSimpleDeclOf = v.getFirstIsSimpleDeclOf(EdgeDirection.IN,true);
		while (isSimpleDeclOf != null) {
			SimpleDeclaration simpleDecl = (SimpleDeclaration) isSimpleDeclOf.getAlpha();
			Expression expr = (Expression) simpleDecl.getFirstIsTypeExprOf(EdgeDirection.IN).getAlpha();
			mergeVariables(expr); 
			isSimpleDeclOf = isSimpleDeclOf.getNextIsSimpleDeclOf(EdgeDirection.IN, true);
		}
		IsSubgraphOf isSubgraphOf = v.getFirstIsSubgraphOf(EdgeDirection.IN,true);
		if (isSubgraphOf != null) {
			mergeVariables(isSubgraphOf.getAlpha());
		}
		IsConstraintOf isConstraintOf = v.getFirstIsConstraintOf(EdgeDirection.IN, true);
		while (isConstraintOf != null) {
			mergeVariables(isConstraintOf.getAlpha());
			isConstraintOf = isConstraintOf.getNextIsConstraintOf(EdgeDirection.IN, true);
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
		IsQuantifiedDeclOf isQuantifiedDeclOf = v.getFirstIsQuantifiedDeclOf(EdgeDirection.IN, true);
		mergeVariablesInDeclaration((Declaration) isQuantifiedDeclOf.getAlpha());
		IsBoundExprOfQuantifier isBoundExprOfQuantifier = v.getFirstIsBoundExprOfQuantifier(EdgeDirection.IN,	true);
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
		Edge IsCompDeclOf = v.getFirstIsCompDeclOf(EdgeDirection.IN,true);
		mergeVariablesInDeclaration((Declaration)IsCompDeclOf.getAlpha());
		Edge IsCompResultDefOf = v.getFirstIsCompResultDefOf(EdgeDirection.IN,	true);
		mergeVariables(IsCompResultDefOf.getAlpha());
		// merge variables in table-headers if it's a bag-comprehension
		if (v instanceof BagComprehension) {
			IsTableHeaderOf isTableHeaderOf = v.getFirstIsTableHeaderOf(EdgeDirection.IN, true);
			while (isTableHeaderOf != null)	{
				mergeVariables(isTableHeaderOf.getAlpha());
				isTableHeaderOf = isTableHeaderOf.getNextIsTableHeaderOf(EdgeDirection.IN,true);
			}
		}
		if (v instanceof TableComprehension) {
			TableComprehension tc = (TableComprehension) v;
			IsColumnHeaderExprOf ch = tc.getFirstIsColumnHeaderExprOf(EdgeDirection.IN, true);
			mergeVariables(ch.getAlpha());
			IsRowHeaderExprOf rh = tc.getFirstIsRowHeaderExprOf(EdgeDirection.IN, true);
			mergeVariables(rh.getAlpha());
			IsTableHeaderOf th = tc.getFirstIsTableHeaderOf(EdgeDirection.IN, true);
			if (th != null)
				mergeVariables(th.getAlpha());
		}
		variableSymbolTable.blockEnd();
	}

	/** Replaces instances of thisVertex-Literals in restrictions by the
	    vertex-Expression that is restricted.
		@param restriction  the vertex representing the restriction
		@param restrictedExpr the expression that is restricted
	*/
	private void mergeRestrictedExpr(Vertex restriction, Expression expr) throws DuplicateVariableException, UndefinedVariableException {
		Edge inc = restriction.getFirstEdge(EdgeDirection.IN);
		while (inc != null)	{
		    Vertex thisLit = inc.getAlpha();
			if (thisLit instanceof ThisLiteral)	{
				String name = ((ThisLiteral) thisLit).getThisValue();
				if ( (name.equals("thisVertex") && !(expr instanceof PathDescription))
				   ||(name.equals("thisEdge") && (expr instanceof PathDescription)) ) {
					inc.setAlpha(expr);
					if (thisLit.getDegree() <= 0) 
						thisLit.delete();
				}
				mergeRestrictedExpr(inc.getAlpha(), expr);
			}
			inc = inc.getNextEdge(EdgeDirection.IN);
		}
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

protected Greql2Parser(TokenBuffer tokenBuf, int k) {
  super(tokenBuf,k);
  tokenNames = _tokenNames;
}

public Greql2Parser(TokenBuffer tokenBuf) {
  this(tokenBuf,1);
}

protected Greql2Parser(TokenStream lexer, int k) {
  super(lexer,k);
  tokenNames = _tokenNames;
}

public Greql2Parser(TokenStream lexer) {
  this(lexer,1);
}

public Greql2Parser(ParserSharedInputState state) {
  super(state,1);
  tokenNames = _tokenNames;
}

/** matches a GReQL 2-Query */
	public final void greqlExpression() throws RecognitionException, TokenStreamException, ParseException,DuplicateVariableException,UndefinedVariableException {
		
		Token  id = null;
		
			Expression expr = null;
			Vector<VertexPosition> varList = new Vector<VertexPosition>();
			int offset = 0;
			int length = 0;
		
		
		try {      // for error handling
			if ( inputState.guessing==0 ) {
				// set inital values
							initialize();
						
			}
			{
			switch ( LA(1)) {
			case NUM_REAL:
			case FUNCTIONID:
			case THIS:
			case FALSE:
			case NOT:
			case NULL_VALUE:
			case TRUE:
			case BAG:
			case E:
			case ESUBGRAPH:
			case EXISTS:
			case FORALL:
			case FROM:
			case LET:
			case LIST:
			case PATH:
			case PATHSYSTEM:
			case REC:
			case SET:
			case TUP:
			case USING:
			case V:
			case VSUBGRAPH:
			case LPAREN:
			case LBRACK:
			case LCURLY:
			case L_T:
			case MINUS:
			case EDGESTART:
			case EDGE:
			case RARROW:
			case LARROW:
			case ARROW:
			case HASH:
			case STRING_LITERAL:
			case IDENT:
			case NUM_INT:
			{
				{
				switch ( LA(1)) {
				case USING:
				{
					match(USING);
					varList=variableList();
					match(COLON);
					break;
				}
				case NUM_REAL:
				case FUNCTIONID:
				case THIS:
				case FALSE:
				case NOT:
				case NULL_VALUE:
				case TRUE:
				case BAG:
				case E:
				case ESUBGRAPH:
				case EXISTS:
				case FORALL:
				case FROM:
				case LET:
				case LIST:
				case PATH:
				case PATHSYSTEM:
				case REC:
				case SET:
				case TUP:
				case V:
				case VSUBGRAPH:
				case LPAREN:
				case LBRACK:
				case LCURLY:
				case L_T:
				case MINUS:
				case EDGESTART:
				case EDGE:
				case RARROW:
				case LARROW:
				case ARROW:
				case HASH:
				case STRING_LITERAL:
				case IDENT:
				case NUM_INT:
				{
					break;
				}
				default:
				{
					throw new NoViableAltException(LT(1), getFilename());
				}
				}
				}
				if ( inputState.guessing==0 ) {
					offset = LT(1).getColumn()-1;
				}
				expr=expression();
				{
				switch ( LA(1)) {
				case STORE:
				{
					match(STORE);
					match(AS);
					id = LT(1);
					match(IDENT);
					break;
				}
				case EOF:
				{
					break;
				}
				default:
				{
					throw new NoViableAltException(LT(1), getFilename());
				}
				}
				}
				if ( inputState.guessing==0 ) {
					
						    	length = - offset + LT(0).getColumn()-1 + LT(0).getText().length();
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
				match(Token.EOF_TYPE);
				if ( inputState.guessing==0 ) {
					
						   		mergeVariables();
						
				}
				break;
			}
			case EOF:
			{
				match(Token.EOF_TYPE);
				if ( inputState.guessing==0 ) {
					graph = null;
				}
				break;
			}
			default:
			{
				throw new NoViableAltException(LT(1), getFilename());
			}
			}
			}
		}
		catch (RecognitionException ex) {
			if (inputState.guessing==0) {
				
				offset = LT(1).getColumn()-1;
				length = LT(1).getText().length();
				throw new ParseException(ex.getMessage(), LT(1).getText(), new SourcePosition (length, offset), ex);
				
			} else {
				throw ex;
			}
		}
		catch (TokenStreamException ex) {
			if (inputState.guessing==0) {
				
					  offset = LT(1).getColumn()-1;
				length = LT(1).getText().length();
				throw new ParseException(ex.getMessage(), LT(1).getText(), new SourcePosition (length, offset), ex);
				
			} else {
				throw ex;
			}
		}
	}
	
/** matches a list of variables: variable {, variable}
	@return vector containing variable-vertices
*/
	public final Vector<VertexPosition>  variableList() throws RecognitionException, TokenStreamException, ParseException,DuplicateVariableException {
		Vector<VertexPosition> variables = new Vector<VertexPosition>();;
		
		
			Variable var = null;
			Vector<VertexPosition> list = null;
		VertexPosition v = new VertexPosition();
		int offset = 0;
		int length = 0;
		
		
		if ( inputState.guessing==0 ) {
			offset = LT(1).getColumn()-1;
		}
		var=variable();
		if ( inputState.guessing==0 ) {
			
						length = -offset + LT(0).getColumn()-1 + LT(0).getText().length();
				v.node = var;
				v.offset = offset;
				v.length = length;
			variables.add(v );
			
		}
		{
		switch ( LA(1)) {
		case COMMA:
		{
			match(COMMA);
			list=variableList();
			if ( inputState.guessing==0 ) {
				
					    variables.addAll(list);
					
			}
			break;
		}
		case COLON:
		{
			break;
		}
		default:
		{
			throw new NoViableAltException(LT(1), getFilename());
		}
		}
		}
		return variables;
	}
	
/** matches expressions
    @return  vertex representing the expression
*/
	public final Expression  expression() throws RecognitionException, TokenStreamException, ParseException,DuplicateVariableException {
		Expression expr = null;
		
		
		expr=quantifiedExpression();
		return expr;
	}
	
/** matches quantifiedExpressions
    @return vertex representing the quantified expression
*/
	public final Expression  quantifiedExpression() throws RecognitionException, TokenStreamException, ParseException,DuplicateVariableException {
		Expression expr = null;
		
		
			Quantifier q;
			Declaration decl;
			int offsetQuantifier = 0;
			int offsetQuantifiedDecl = 0;
			int offsetQuantifiedExpr = 0;
			int lengthQuantifier = 0;
			int lengthQuantifiedDecl = 0;
			int lengthQuantifiedExpr = 0;
		
		
		switch ( LA(1)) {
		case EXISTS:
		case FORALL:
		{
			{
			if ( inputState.guessing==0 ) {
				
					offsetQuantifier = LT(1).getColumn()-1;
					lengthQuantifier = LT(1).getText().length();
				
			}
			q=quantifier();
			if ( inputState.guessing==0 ) {
				offsetQuantifiedDecl = LT(1).getColumn()-1;
			}
			decl=quantifiedDeclaration();
			if ( inputState.guessing==0 ) {
				lengthQuantifiedDecl = - offsetQuantifiedDecl + LT(0).getColumn()-1 + LT(0).getText().length();
			}
			match(AT);
			if ( inputState.guessing==0 ) {
				offsetQuantifiedExpr = LT(1).getColumn()-1;
			}
			expr=quantifiedExpression();
			if ( inputState.guessing==0 ) {
				
					lengthQuantifiedExpr = - offsetQuantifiedExpr + LT(0).getColumn()-1 + LT(0).getText().length();
					            // create new Quantifies Expr
					            QuantifiedExpression quantifiedExpr = graph.createQuantifiedExpression();
					            // add quantifier
					            IsQuantifierOf quantifierOf = graph.createIsQuantifierOf(q, quantifiedExpr);
					            quantifierOf.setSourcePositions((createSourcePositionList(lengthQuantifier, offsetQuantifier)));
								// add declaration
					            IsQuantifiedDeclOf quantifiedDeclOf = graph.createIsQuantifiedDeclOf(decl, quantifiedExpr);
					            quantifiedDeclOf.setSourcePositions((createSourcePositionList(lengthQuantifiedDecl, offsetQuantifiedDecl)));
					            // add predicate
								IsBoundExprOf boundExprOf = graph.createIsBoundExprOfQuantifier(expr, quantifiedExpr);
								boundExprOf.setSourcePositions((createSourcePositionList(lengthQuantifiedExpr, offsetQuantifiedExpr)));
								// return the right vertex...
								expr = quantifiedExpr;
				
			}
			}
			break;
		}
		case NUM_REAL:
		case FUNCTIONID:
		case THIS:
		case FALSE:
		case NOT:
		case NULL_VALUE:
		case TRUE:
		case BAG:
		case E:
		case ESUBGRAPH:
		case FROM:
		case LET:
		case LIST:
		case PATH:
		case PATHSYSTEM:
		case REC:
		case SET:
		case TUP:
		case V:
		case VSUBGRAPH:
		case LPAREN:
		case LBRACK:
		case LCURLY:
		case L_T:
		case MINUS:
		case EDGESTART:
		case EDGE:
		case RARROW:
		case LARROW:
		case ARROW:
		case HASH:
		case STRING_LITERAL:
		case IDENT:
		case NUM_INT:
		{
			expr=letExpression();
			break;
		}
		default:
		{
			throw new NoViableAltException(LT(1), getFilename());
		}
		}
		return expr;
	}
	
/** matches a quantifier
*/
	public final Quantifier  quantifier() throws RecognitionException, TokenStreamException, ParseException,DuplicateVariableException {
		Quantifier expr = null;
		
		
			String name = "";
		
		
		{
		switch ( LA(1)) {
		case FORALL:
		{
			match(FORALL);
			if ( inputState.guessing==0 ) {
				name = "forall";
			}
			break;
		}
		case EXISTS:
		{
			{
			boolean synPredMatched257 = false;
			if (((LA(1)==EXISTS))) {
				int _m257 = mark();
				synPredMatched257 = true;
				inputState.guessing++;
				try {
					{
					match(EXISTS);
					match(EXCL);
					}
				}
				catch (RecognitionException pe) {
					synPredMatched257 = false;
				}
				rewind(_m257);
inputState.guessing--;
			}
			if ( synPredMatched257 ) {
				{
				match(EXISTS);
				match(EXCL);
				if ( inputState.guessing==0 ) {
					name = "exists!";
				}
				}
			}
			else if ((LA(1)==EXISTS)) {
				match(EXISTS);
				if ( inputState.guessing==0 ) {
					name = "exists";
				}
			}
			else {
				throw new NoViableAltException(LT(1), getFilename());
			}
			
			}
			break;
		}
		default:
		{
			throw new NoViableAltException(LT(1), getFilename());
		}
		}
		}
		if ( inputState.guessing==0 ) {
			
			expr = graph.getFirstQuantifier();
			while (expr != null) {
				if (expr.getName().equals(name)) return expr;
				expr = expr.getNextQuantifier();
			}
				        expr = graph.createQuantifier();
				        expr.setName(name);
			
		}
		return expr;
	}
	
/** matches a quantified declaration which contains
	simple declarations and boolean expressions,
	each of them separated by  ','.
*/
	public final Declaration  quantifiedDeclaration() throws RecognitionException, TokenStreamException, ParseException,DuplicateVariableException {
		Declaration declaration = null;
		
		
			Expression subgraphExpr = null;
			Expression constraintExpr = null;
		Vector<VertexPosition> declarations = new Vector<VertexPosition>();
		int offsetConstraint = 0;
		int offsetSubgraph = 0;
		int lengthConstraint = 0;
		int lengthSubgraph = 0;
		
		
		declarations=declarationList();
		if ( inputState.guessing==0 ) {
			
					    declaration = graph.createDeclaration();
					    for (int i = 0; i < declarations.size(); i++) {
							VertexPosition decl = declarations.get(i);
							IsSimpleDeclOf simpleDeclOf = graph.createIsSimpleDeclOf((SimpleDeclaration)decl.node, declaration);
							simpleDeclOf.setSourcePositions((createSourcePositionList(decl.length, decl.offset)));
						}
			
		}
		{
		_loop241:
		do {
			if ((LA(1)==COMMA)) {
				match(COMMA);
				if ( inputState.guessing==0 ) {
					offsetConstraint = LT(1).getColumn()-1;
				}
				constraintExpr=expression();
				if ( inputState.guessing==0 ) {
					
							   		lengthConstraint = -offsetConstraint + LT(0).getColumn()-1 +LT(0).getText().length();
						   			IsConstraintOf constraintOf = graph.createIsConstraintOf(constraintExpr,declaration);
							   		constraintOf.setSourcePositions((createSourcePositionList(lengthConstraint, offsetConstraint)));
								
				}
				{
				boolean synPredMatched239 = false;
				if (((LA(1)==COMMA))) {
					int _m239 = mark();
					synPredMatched239 = true;
					inputState.guessing++;
					try {
						{
						match(COMMA);
						simpleDeclaration();
						}
					}
					catch (RecognitionException pe) {
						synPredMatched239 = false;
					}
					rewind(_m239);
inputState.guessing--;
				}
				if ( synPredMatched239 ) {
					{
					match(COMMA);
					declarations=declarationList();
					if ( inputState.guessing==0 ) {
						
								    	   for (int i = 0; i < declarations.size(); i++) {
												VertexPosition decl = declarations.get(i);
												IsSimpleDeclOf simpleDeclOf = graph.createIsSimpleDeclOf((SimpleDeclaration)decl.node, declaration);
												simpleDeclOf.setSourcePositions((createSourcePositionList(decl.length, decl.offset)));
										   }
									
					}
					}
				}
				else if ((LA(1)==IN||LA(1)==COMMA||LA(1)==AT)) {
				}
				else {
					throw new NoViableAltException(LT(1), getFilename());
				}
				
				}
			}
			else {
				break _loop241;
			}
			
		} while (true);
		}
		{
		switch ( LA(1)) {
		case IN:
		{
			match(IN);
			if ( inputState.guessing==0 ) {
				offsetSubgraph = LT(1).getColumn()-1;
			}
			subgraphExpr=expression();
			if ( inputState.guessing==0 ) {
				
					lengthSubgraph = -offsetSubgraph + LT(0).getColumn()-1 +LT(0).getText().length();
					 	IsSubgraphOf subgraphOf = graph.createIsSubgraphOf(subgraphExpr, declaration);
					subgraphOf.setSourcePositions((createSourcePositionList(lengthSubgraph, offsetSubgraph)));
				
			}
			break;
		}
		case AT:
		{
			break;
		}
		default:
		{
			throw new NoViableAltException(LT(1), getFilename());
		}
		}
		}
		return declaration;
	}
	
/** matches let-expressions
    @return
*/
	public final Expression  letExpression() throws RecognitionException, TokenStreamException, ParseException,DuplicateVariableException {
		Expression expr = null;
		
		
			Vector<VertexPosition> defList = new Vector<VertexPosition>();
			int offset = 0;
			int length = 0;
		
		
		switch ( LA(1)) {
		case LET:
		{
			{
			match(LET);
			defList=definitionList();
			match(IN);
			if ( inputState.guessing==0 ) {
				offset = LT(1).getColumn()-1;
			}
			expr=letExpression();
			if ( inputState.guessing==0 ) {
				
								length = -offset + LT(0).getColumn()-1 + LT(0).getText().length();
								if (defList.size() != 0) {
									// create new letexpression-vertex
									LetExpression letExpr = graph.createLetExpression();
									// set bound expression
									IsBoundExprOf exprOf = graph.createIsBoundExprOfDefinition(expr, letExpr);
									exprOf.setSourcePositions((createSourcePositionList(length, offset)));
									// add definitions
									for (int i = 0; i < defList.size(); i++) {
										VertexPosition def = defList.get(i);
										IsDefinitionOf definitionOf = graph.createIsDefinitionOf((Definition)def.node, letExpr);
										definitionOf.setSourcePositions((createSourcePositionList(def.length, def.offset)));
							    	}
							    	// return letExpr
							    	expr = letExpr;
					}
							
			}
			}
			break;
		}
		case NUM_REAL:
		case FUNCTIONID:
		case THIS:
		case FALSE:
		case NOT:
		case NULL_VALUE:
		case TRUE:
		case BAG:
		case E:
		case ESUBGRAPH:
		case FROM:
		case LIST:
		case PATH:
		case PATHSYSTEM:
		case REC:
		case SET:
		case TUP:
		case V:
		case VSUBGRAPH:
		case LPAREN:
		case LBRACK:
		case LCURLY:
		case L_T:
		case MINUS:
		case EDGESTART:
		case EDGE:
		case RARROW:
		case LARROW:
		case ARROW:
		case HASH:
		case STRING_LITERAL:
		case IDENT:
		case NUM_INT:
		{
			expr=whereExpression();
			break;
		}
		default:
		{
			throw new NoViableAltException(LT(1), getFilename());
		}
		}
		return expr;
	}
	
/** matches a list of definitions for let- or where expressions
	@return
*/
	public final Vector<VertexPosition>  definitionList() throws RecognitionException, TokenStreamException, ParseException,DuplicateVariableException {
		Vector<VertexPosition> definitions = new Vector<VertexPosition>();;
		
		
			Definition v = null;
		VertexPosition def = new VertexPosition();
		Vector<VertexPosition> defList = new Vector<VertexPosition>();
		int offset = 0;
		int length = 0;
		
		
		if ( inputState.guessing==0 ) {
			offset = LT(1).getColumn()-1;
		}
		v=definition();
		if ( inputState.guessing==0 ) {
			
						length = -offset + LT(0).getColumn()-1 +LT(0).getText().length();
				def.node = v;
			def.offset = offset;
			def.length = length;
			definitions.add(def);
			
		}
		{
		boolean synPredMatched206 = false;
		if (((LA(1)==COMMA))) {
			int _m206 = mark();
			synPredMatched206 = true;
			inputState.guessing++;
			try {
				{
				match(COMMA);
				}
			}
			catch (RecognitionException pe) {
				synPredMatched206 = false;
			}
			rewind(_m206);
inputState.guessing--;
		}
		if ( synPredMatched206 ) {
			{
			match(COMMA);
			defList=definitionList();
			}
			if ( inputState.guessing==0 ) {
				
					definitions.addAll(defList);
				
			}
		}
		else if ((_tokenSet_0.member(LA(1)))) {
		}
		else {
			throw new NoViableAltException(LT(1), getFilename());
		}
		
		}
		return definitions;
	}
	
/** matches Where-Expressions
	@return
*/
	public final Expression  whereExpression() throws RecognitionException, TokenStreamException, ParseException,DuplicateVariableException {
		Expression expr = null;
		
		
			Vector<VertexPosition> defList = new Vector<VertexPosition>();
			int offset = 0;
			int length = 0;
		
		
		if ( inputState.guessing==0 ) {
			offset = LT(1).getColumn()-1;
		}
		expr=conditionalExpression();
		if ( inputState.guessing==0 ) {
			length = -offset + LT(0).getColumn()-1 + LT(0).getText().length();
		}
		{
		switch ( LA(1)) {
		case WHERE:
		{
			match(WHERE);
			defList=definitionList();
			break;
		}
		case EOF:
		case DOTDOT:
		case AS:
		case END:
		case IN:
		case REPORT:
		case REPORTSET:
		case REPORTBAG:
		case REPORTTABLE:
		case STORE:
		case WITH:
		case COMMA:
		case AT:
		case RPAREN:
		case RBRACK:
		case RCURLY:
		case EDGEEND:
		case EDGE:
		{
			break;
		}
		default:
		{
			throw new NoViableAltException(LT(1), getFilename());
		}
		}
		}
		if ( inputState.guessing==0 ) {
			
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
		return expr;
	}
	
/** matches conditional expressions
    @return
*/
	public final Expression  conditionalExpression() throws RecognitionException, TokenStreamException, ParseException,DuplicateVariableException {
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
		
		
		if ( inputState.guessing==0 ) {
			offsetExpr = LT(1).getColumn()-1;
		}
		expr=orExpression();
		if ( inputState.guessing==0 ) {
			lengthExpr = -offsetExpr + LT(0).getColumn()-1 + LT(0).getText().length();
		}
		{
		switch ( LA(1)) {
		case QUESTION:
		{
			match(QUESTION);
			if ( inputState.guessing==0 ) {
				offsetTrueExpr = LT(1).getColumn()-1;
			}
			trueExpr=conditionalExpression();
			if ( inputState.guessing==0 ) {
				lengthTrueExpr = -offsetTrueExpr + LT(0).getColumn()-1 + LT(0).getText().length();
			}
			match(COLON);
			if ( inputState.guessing==0 ) {
				offsetFalseExpr = LT(1).getColumn()-1;
			}
			falseExpr=conditionalExpression();
			if ( inputState.guessing==0 ) {
				lengthFalseExpr = -offsetFalseExpr + LT(0).getColumn()-1 + LT(0).getText().length();
			}
			match(COLON);
			if ( inputState.guessing==0 ) {
				offsetNullExpr = LT(1).getColumn()-1;
			}
			nullExpr=conditionalExpression();
			if ( inputState.guessing==0 ) {
				
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
			break;
		}
		case EOF:
		case DOTDOT:
		case AS:
		case END:
		case IN:
		case REPORT:
		case REPORTSET:
		case REPORTBAG:
		case REPORTTABLE:
		case STORE:
		case WHERE:
		case WITH:
		case COLON:
		case COMMA:
		case AT:
		case RPAREN:
		case RBRACK:
		case RCURLY:
		case EDGEEND:
		case EDGE:
		{
			break;
		}
		default:
		{
			throw new NoViableAltException(LT(1), getFilename());
		}
		}
		}
		return expr;
	}
	
/** matches first argument of or- and orElse-Expressions
	@return
*/
	public final Expression  orExpression() throws RecognitionException, TokenStreamException, ParseException,DuplicateVariableException {
		Expression expr = null;
		
		
			int offset = 0;
			int length = 0;
		
		
		if ( inputState.guessing==0 ) {
			offset = LT(1).getColumn()-1;
		}
		expr=xorExpression();
		if ( inputState.guessing==0 ) {
			length = -offset + LT(0).getColumn()-1 + LT(0).getText().length();
		}
		{
		switch ( LA(1)) {
		case OR:
		case ORELSE:
		{
			expr=orExpression2(expr, offset, length);
			break;
		}
		case EOF:
		case DOTDOT:
		case AS:
		case END:
		case IN:
		case REPORT:
		case REPORTSET:
		case REPORTBAG:
		case REPORTTABLE:
		case STORE:
		case WHERE:
		case WITH:
		case QUESTION:
		case COLON:
		case COMMA:
		case AT:
		case RPAREN:
		case RBRACK:
		case RCURLY:
		case EDGEEND:
		case EDGE:
		{
			break;
		}
		default:
		{
			throw new NoViableAltException(LT(1), getFilename());
		}
		}
		}
		return expr;
	}
	
/** matches first argument of xor-Expression
	@return
*/
	public final Expression  xorExpression() throws RecognitionException, TokenStreamException, ParseException,DuplicateVariableException {
		Expression expr = null;
		
		
			int offset = 0;
			int length = 0;
		
		
		if ( inputState.guessing==0 ) {
			offset = LT(1).getColumn()-1;
		}
		expr=andExpression();
		if ( inputState.guessing==0 ) {
			length = -offset + LT(0).getColumn()-1 + LT(0).getText().length();
		}
		{
		switch ( LA(1)) {
		case XOR:
		{
			expr=xorExpression2(expr, offset, length);
			break;
		}
		case EOF:
		case DOTDOT:
		case OR:
		case ORELSE:
		case AS:
		case END:
		case IN:
		case REPORT:
		case REPORTSET:
		case REPORTBAG:
		case REPORTTABLE:
		case STORE:
		case WHERE:
		case WITH:
		case QUESTION:
		case COLON:
		case COMMA:
		case AT:
		case RPAREN:
		case RBRACK:
		case RCURLY:
		case EDGEEND:
		case EDGE:
		{
			break;
		}
		default:
		{
			throw new NoViableAltException(LT(1), getFilename());
		}
		}
		}
		return expr;
	}
	
/** matches operator and 2nd argument of or- and orElse-Expressions
	@param arg1 first argument
	@param offsetArg1 offset of first argument
	@param lengthArg1 length of first argument
	@return or-expression (FunctionApplication)
*/
	public final FunctionApplication  orExpression2(
		Expression arg1, int offsetArg1, int lengthArg1
	) throws RecognitionException, TokenStreamException, ParseException,DuplicateVariableException {
		FunctionApplication functionAppl = null;
		
		
			Expression arg2 = null;
			FunctionId op = null;
		String name = "orElse";
		int offsetOperator = 0;
		int offsetArg2 = 0;
		int lengthOperator = 0;
		int lengthArg2 = 0;
		
		
		if ( inputState.guessing==0 ) {
			offsetOperator = LT(1).getColumn()-1;
		}
		{
		switch ( LA(1)) {
		case OR:
		{
			match(OR);
			if ( inputState.guessing==0 ) {
				name = "or";
			}
			break;
		}
		case ORELSE:
		{
			match(ORELSE);
			break;
		}
		default:
		{
			throw new NoViableAltException(LT(1), getFilename());
		}
		}
		}
		if ( inputState.guessing==0 ) {
			lengthOperator = -offsetOperator + LT(0).getColumn()-1 + LT(0).getText().length();
					  offsetArg2 = LT(1).getColumn()-1;
					
		}
		arg2=xorExpression();
		if ( inputState.guessing==0 ) {
			
			lengthArg2 = -offsetArg2 + LT(0).getColumn()-1 + LT(0).getText().length();
					    // create new F.A.
				        functionAppl = graph.createFunctionApplication();
				    	// retrieve operator...
				    	op = (FunctionId) functionSymbolTable.lookup(name);
				    	//... or create a new one and add it to the symboltable
				        if (op == null) {
				    		op = graph.createFunctionId();
				   			op.setName(name);
				   			functionSymbolTable.insert(name, op);
				   		}
				   		// add operator
				   		createFunctionIdAndArgumentOf(functionAppl, op, offsetOperator,lengthOperator, 
				   									  arg1, offsetArg1, lengthArg1, arg2, offsetArg2, lengthArg2); 
					
		}
		{
		switch ( LA(1)) {
		case OR:
		case ORELSE:
		{
			functionAppl=orExpression2(functionAppl, offsetArg1,
            			   -offsetArg1 + LT(0).getColumn()-1 + LT(0).getText().length());
			break;
		}
		case EOF:
		case DOTDOT:
		case AS:
		case END:
		case IN:
		case REPORT:
		case REPORTSET:
		case REPORTBAG:
		case REPORTTABLE:
		case STORE:
		case WHERE:
		case WITH:
		case QUESTION:
		case COLON:
		case COMMA:
		case AT:
		case RPAREN:
		case RBRACK:
		case RCURLY:
		case EDGEEND:
		case EDGE:
		{
			break;
		}
		default:
		{
			throw new NoViableAltException(LT(1), getFilename());
		}
		}
		}
		return functionAppl;
	}
	
/** matches first argument of an and-or andThen-Expression
	@return
*/
	public final Expression  andExpression() throws RecognitionException, TokenStreamException, ParseException,DuplicateVariableException {
		Expression expr = null;
		
		
			int offset = 0;
			int length = 0;
		
		
		if ( inputState.guessing==0 ) {
			offset = LT(1).getColumn()-1;
		}
		expr=equalityExpression();
		if ( inputState.guessing==0 ) {
			length = -offset + LT(0).getColumn()-1 + LT(0).getText().length();
		}
		{
		switch ( LA(1)) {
		case AND:
		case ANDTHEN:
		{
			expr=andExpression2(expr, offset, length);
			break;
		}
		case EOF:
		case DOTDOT:
		case OR:
		case ORELSE:
		case XOR:
		case AS:
		case END:
		case IN:
		case REPORT:
		case REPORTSET:
		case REPORTBAG:
		case REPORTTABLE:
		case STORE:
		case WHERE:
		case WITH:
		case QUESTION:
		case COLON:
		case COMMA:
		case AT:
		case RPAREN:
		case RBRACK:
		case RCURLY:
		case EDGEEND:
		case EDGE:
		{
			break;
		}
		default:
		{
			throw new NoViableAltException(LT(1), getFilename());
		}
		}
		}
		return expr;
	}
	
/** matches operator and 2nd argument of xor-Expressions
	@param arg1 first argument
	@param offsetArg1 offset of first argument
	@param lengthArg1 length of first argument
	@return or-expression (FunctionApplication)
*/
	public final FunctionApplication  xorExpression2(
		Expression arg1, int offsetArg1, int lengthArg1
	) throws RecognitionException, TokenStreamException, ParseException,DuplicateVariableException {
		FunctionApplication functionAppl = null;
		
		
			Expression arg2 = null;
			FunctionId op = null;
		String name = "xor";
		int offsetOperator = 0;
		int offsetArg2 = 0;
		int lengthOperator = 0;
		int lengthArg2 = 0;
		
		
		if ( inputState.guessing==0 ) {
			offsetOperator = LT(1).getColumn()-1;
		}
		match(XOR);
		if ( inputState.guessing==0 ) {
			lengthOperator = -offsetOperator + LT(0).getColumn()-1 + LT(0).getText().length();
					  offsetArg2 = LT(1).getColumn()-1;
					
		}
		arg2=andExpression();
		if ( inputState.guessing==0 ) {
			
				        lengthArg2 = -offsetArg2 + LT(0).getColumn()-1 + LT(0).getText().length();
			functionAppl = graph.createFunctionApplication();
					op = (FunctionId) functionSymbolTable.lookup(name);
			if (op == null)	{
				    		op = graph.createFunctionId();
						op.setName(name);
						functionSymbolTable.insert(name, op);
					}
					createFunctionIdAndArgumentOf(functionAppl, op, offsetOperator, lengthOperator, 
							   						  arg1, offsetArg1, lengthArg1, arg2, offsetArg2, lengthArg2); 
					
		}
		{
		switch ( LA(1)) {
		case XOR:
		{
			functionAppl=xorExpression2(functionAppl, offsetArg1,
            			   -offsetArg1 + LT(0).getColumn()-1 + LT(0).getText().length());
			break;
		}
		case EOF:
		case DOTDOT:
		case OR:
		case ORELSE:
		case AS:
		case END:
		case IN:
		case REPORT:
		case REPORTSET:
		case REPORTBAG:
		case REPORTTABLE:
		case STORE:
		case WHERE:
		case WITH:
		case QUESTION:
		case COLON:
		case COMMA:
		case AT:
		case RPAREN:
		case RBRACK:
		case RCURLY:
		case EDGEEND:
		case EDGE:
		{
			break;
		}
		default:
		{
			throw new NoViableAltException(LT(1), getFilename());
		}
		}
		}
		return functionAppl;
	}
	
/** matches first argument of equality-Expression
	@return
*/
	public final Expression  equalityExpression() throws RecognitionException, TokenStreamException, ParseException,DuplicateVariableException {
		Expression expr = null;
		
		
			int offset = 0;
			int length = 0;
		
		
		if ( inputState.guessing==0 ) {
			offset = LT(1).getColumn()-1;
		}
		expr=relationalExpression();
		if ( inputState.guessing==0 ) {
			length = -offset + LT(0).getColumn()-1 + LT(0).getText().length();
		}
		{
		switch ( LA(1)) {
		case EQUAL:
		case NOT_EQUAL:
		{
			expr=equalityExpression2(expr, offset, length);
			break;
		}
		case EOF:
		case DOTDOT:
		case AND:
		case ANDTHEN:
		case OR:
		case ORELSE:
		case XOR:
		case AS:
		case END:
		case IN:
		case REPORT:
		case REPORTSET:
		case REPORTBAG:
		case REPORTTABLE:
		case STORE:
		case WHERE:
		case WITH:
		case QUESTION:
		case COLON:
		case COMMA:
		case AT:
		case RPAREN:
		case RBRACK:
		case RCURLY:
		case EDGEEND:
		case EDGE:
		{
			break;
		}
		default:
		{
			throw new NoViableAltException(LT(1), getFilename());
		}
		}
		}
		return expr;
	}
	
/** matches operator and 2nd argument of and- or andthen-Expressions
	@param arg1 first argument
	@param offsetArg1 offset of first argument
	@param lengthArg1 length of first argument
	@return or-expression (FunctionApplication)
*/
	public final FunctionApplication  andExpression2(
		Expression arg1, int offsetArg1, int lengthArg1
	) throws RecognitionException, TokenStreamException, ParseException,DuplicateVariableException {
		FunctionApplication functionAppl = null;
		
		
			Expression arg2 = null;
			FunctionId  op = null;
		String name = "andThen";
		int offsetOperator = 0;
		int offsetArg2 = 0;
		int lengthOperator = 0;
		int lengthArg2 = 0;
		
		
		if ( inputState.guessing==0 ) {
			offsetOperator = LT(1).getColumn()-1;
		}
		{
		switch ( LA(1)) {
		case AND:
		{
			match(AND);
			if ( inputState.guessing==0 ) {
				name = "and";
			}
			break;
		}
		case ANDTHEN:
		{
			match(ANDTHEN);
			break;
		}
		default:
		{
			throw new NoViableAltException(LT(1), getFilename());
		}
		}
		}
		if ( inputState.guessing==0 ) {
			lengthOperator = -offsetOperator + LT(0).getColumn()-1 + LT(0).getText().length();
					  offsetArg2 = LT(1).getColumn()-1;
					
		}
		arg2=equalityExpression();
		if ( inputState.guessing==0 ) {
			
			lengthArg2 = -offsetArg2 + LT(0).getColumn()-1 + LT(0).getText().length();
			functionAppl = graph.createFunctionApplication();
					op = (FunctionId) functionSymbolTable.lookup(name);
			if (op == null) {
					   		op = graph.createFunctionId();
					   		op.setName(name);
					   		functionSymbolTable.insert(name, op);
					   	}
					createFunctionIdAndArgumentOf(functionAppl, op, offsetOperator, lengthOperator, 
							   						  arg1, offsetArg1, lengthArg1, arg2, offsetArg2, lengthArg2); 
					
		}
		{
		switch ( LA(1)) {
		case AND:
		case ANDTHEN:
		{
			functionAppl=andExpression2(functionAppl, offsetArg1,
            			   -offsetArg1 + LT(0).getColumn()-1 + LT(0).getText().length());
			break;
		}
		case EOF:
		case DOTDOT:
		case OR:
		case ORELSE:
		case XOR:
		case AS:
		case END:
		case IN:
		case REPORT:
		case REPORTSET:
		case REPORTBAG:
		case REPORTTABLE:
		case STORE:
		case WHERE:
		case WITH:
		case QUESTION:
		case COLON:
		case COMMA:
		case AT:
		case RPAREN:
		case RBRACK:
		case RCURLY:
		case EDGEEND:
		case EDGE:
		{
			break;
		}
		default:
		{
			throw new NoViableAltException(LT(1), getFilename());
		}
		}
		}
		return functionAppl;
	}
	
/** matches first argument of relational-Expression
	@return
*/
	public final Expression  relationalExpression() throws RecognitionException, TokenStreamException, ParseException,DuplicateVariableException {
		Expression expr = null;
		
		
			int offset = 0;
			int length = 0;
		
		
		if ( inputState.guessing==0 ) {
			offset = LT(1).getColumn()-1;
		}
		expr=additiveExpression();
		if ( inputState.guessing==0 ) {
			length = -offset + LT(0).getColumn()-1 + LT(0).getText().length();
		}
		{
		if (((_tokenSet_1.member(LA(1))))&&( isAdditiveExpression )) {
			{
			expr=relationalExpression2(expr, offset, length);
			}
		}
		else if ((_tokenSet_2.member(LA(1)))) {
		}
		else {
			throw new NoViableAltException(LT(1), getFilename());
		}
		
		}
		return expr;
	}
	
/** matches operator and 2nd argument of equality-Expression ( = , <>)
	@param arg1 first argument
	@param offsetArg1 offset of first argument
	@param lengthArg1 length of first argument
	@return or-expression (FunctionApplication)
*/
	public final FunctionApplication  equalityExpression2(
		Expression arg1, int offsetArg1, int lengthArg1
	) throws RecognitionException, TokenStreamException, ParseException,DuplicateVariableException {
		FunctionApplication functionAppl = null;
		
		
			Expression arg2 = null;
			FunctionId  op = null;
		String name = "nequals";
		int offsetOperator = 0;
		int offsetArg2 = 0;
		int lengthOperator = 0;
		int lengthArg2 = 0;
		
		
		if ( inputState.guessing==0 ) {
			offsetOperator = LT(1).getColumn()-1;
		}
		{
		switch ( LA(1)) {
		case EQUAL:
		{
			match(EQUAL);
			if ( inputState.guessing==0 ) {
				name = "equals";
			}
			break;
		}
		case NOT_EQUAL:
		{
			match(NOT_EQUAL);
			break;
		}
		default:
		{
			throw new NoViableAltException(LT(1), getFilename());
		}
		}
		}
		if ( inputState.guessing==0 ) {
			lengthOperator = -offsetOperator + LT(0).getColumn()-1 + LT(0).getText().length();
					  offsetArg2 = LT(1).getColumn()-1;
					
		}
		arg2=relationalExpression();
		if ( inputState.guessing==0 ) {
			
			lengthArg2 = -offsetArg2 + LT(0).getColumn()-1 + LT(0).getText().length();
			functionAppl = graph.createFunctionApplication();
				    	op = (FunctionId) functionSymbolTable.lookup(name);
				        if (op == null) {
					   		op = graph.createFunctionId();
					   		op.setName(name);
					   		functionSymbolTable.insert(name, op);
					   	}
					createFunctionIdAndArgumentOf(functionAppl, op, offsetOperator, lengthOperator, 
							   						  arg1, offsetArg1, lengthArg1, arg2, offsetArg2, lengthArg2); 		   	
					
		}
		{
		switch ( LA(1)) {
		case EQUAL:
		case NOT_EQUAL:
		{
			functionAppl=equalityExpression2(functionAppl, offsetArg1,
            			   -offsetArg1 + LT(0).getColumn()-1 + LT(0).getText().length());
			break;
		}
		case EOF:
		case DOTDOT:
		case AND:
		case ANDTHEN:
		case OR:
		case ORELSE:
		case XOR:
		case AS:
		case END:
		case IN:
		case REPORT:
		case REPORTSET:
		case REPORTBAG:
		case REPORTTABLE:
		case STORE:
		case WHERE:
		case WITH:
		case QUESTION:
		case COLON:
		case COMMA:
		case AT:
		case RPAREN:
		case RBRACK:
		case RCURLY:
		case EDGEEND:
		case EDGE:
		{
			break;
		}
		default:
		{
			throw new NoViableAltException(LT(1), getFilename());
		}
		}
		}
		return functionAppl;
	}
	
/** matches first argument of additive Expression (+, -)
	@return
*/
	public final Expression  additiveExpression() throws RecognitionException, TokenStreamException, ParseException,DuplicateVariableException {
		Expression expr = null;
		
		
			int offset = 0;
			int length = 0;
		
		
		if ( inputState.guessing==0 ) {
			offset = LT(1).getColumn()-1;
		}
		expr=multiplicativeExpression();
		if ( inputState.guessing==0 ) {
			length = -offset + LT(0).getColumn()-1 + LT(0).getText().length();
		}
		{
		switch ( LA(1)) {
		case PLUS:
		case MINUS:
		{
			expr=additiveExpression2(expr, offset, length);
			break;
		}
		case EOF:
		case DOTDOT:
		case AND:
		case ANDTHEN:
		case OR:
		case ORELSE:
		case XOR:
		case AS:
		case END:
		case IN:
		case REPORT:
		case REPORTSET:
		case REPORTBAG:
		case REPORTTABLE:
		case STORE:
		case WHERE:
		case WITH:
		case QUESTION:
		case COLON:
		case COMMA:
		case AT:
		case RPAREN:
		case RBRACK:
		case RCURLY:
		case EQUAL:
		case MATCH:
		case NOT_EQUAL:
		case LE:
		case GE:
		case L_T:
		case G_T:
		case EDGEEND:
		case EDGE:
		{
			break;
		}
		default:
		{
			throw new NoViableAltException(LT(1), getFilename());
		}
		}
		}
		return expr;
	}
	
/** matches operator and 2nd argument of relational-Expressions (<, <= , >, >= , =~)
	@param arg1 first argument
	@param offsetArg1 offset of first argument
	@param lengthArg1 length of first argument
	@return or-expression (FunctionApplication)
*/
	public final FunctionApplication  relationalExpression2(
		Expression arg1, int offsetArg1, int lengthArg1
	) throws RecognitionException, TokenStreamException, ParseException,DuplicateVariableException {
		FunctionApplication functionAppl = null;
		
		
			Expression arg2 = null;
			FunctionId op = null;
		String name = "reMatch";
		int offsetOperator = 0;
		int offsetArg2 = 0;
		int lengthOperator = 0;
		int lengthArg2 = 0;
		
		
		if ( inputState.guessing==0 ) {
			offsetOperator = LT(1).getColumn()-1;
		}
		{
		switch ( LA(1)) {
		case L_T:
		{
			match(L_T);
			if ( inputState.guessing==0 ) {
				name = "leThan";
			}
			break;
		}
		case LE:
		{
			match(LE);
			if ( inputState.guessing==0 ) {
				name = "leEqual";
			}
			break;
		}
		case G_T:
		{
			match(G_T);
			if ( inputState.guessing==0 ) {
				name = "grThan";
			}
			break;
		}
		case GE:
		{
			match(GE);
			if ( inputState.guessing==0 ) {
				name = "grEqual";
			}
			break;
		}
		case MATCH:
		{
			match(MATCH);
			break;
		}
		default:
		{
			throw new NoViableAltException(LT(1), getFilename());
		}
		}
		}
		if ( inputState.guessing==0 ) {
			lengthOperator = -offsetOperator + LT(0).getColumn()-1 + LT(0).getText().length();
					  offsetArg2 = LT(1).getColumn()-1;
					
		}
		arg2=additiveExpression();
		if ( inputState.guessing==0 ) {
			
			lengthArg2 = -offsetArg2 + LT(0).getColumn()-1 + LT(0).getText().length();
			functionAppl = graph.createFunctionApplication();
					op = (FunctionId) functionSymbolTable.lookup(name);
			if (op == null) {
				    		op = graph.createFunctionId();
				    		op.setName(name);
				    		functionSymbolTable.insert(name, op);
				    	}
				    	createFunctionIdAndArgumentOf(functionAppl, op, offsetOperator, lengthOperator, 
							   						  arg1, offsetArg1, lengthArg1, arg2, offsetArg2, lengthArg2); 
					
		}
		{
		if (((_tokenSet_1.member(LA(1))))&&(isAdditiveExpression )) {
			{
			functionAppl=relationalExpression2(functionAppl, offsetArg1,
            			   -offsetArg1 + LT(0).getColumn()-1 + LT(0).getText().length());
			}
		}
		else if ((_tokenSet_2.member(LA(1)))) {
		}
		else {
			throw new NoViableAltException(LT(1), getFilename());
		}
		
		}
		return functionAppl;
	}
	
/** matches first argument of multiplicative-Expression
	@return
*/
	public final Expression  multiplicativeExpression() throws RecognitionException, TokenStreamException, ParseException,DuplicateVariableException {
		Expression expr = null;
		
		
			int offset = 0;
			int length = 0;
		
		
		if ( inputState.guessing==0 ) {
			offset = LT(1).getColumn()-1;
		}
		expr=unaryExpression();
		if ( inputState.guessing==0 ) {
			length = -offset + LT(0).getColumn()-1 + LT(0).getText().length();
		}
		{
		switch ( LA(1)) {
		case DIV:
		case STAR:
		case MOD:
		{
			expr=multiplicativeExpression2(expr, offset, length);
			break;
		}
		case EOF:
		case DOTDOT:
		case AND:
		case ANDTHEN:
		case OR:
		case ORELSE:
		case XOR:
		case AS:
		case END:
		case IN:
		case REPORT:
		case REPORTSET:
		case REPORTBAG:
		case REPORTTABLE:
		case STORE:
		case WHERE:
		case WITH:
		case QUESTION:
		case COLON:
		case COMMA:
		case AT:
		case RPAREN:
		case RBRACK:
		case RCURLY:
		case EQUAL:
		case MATCH:
		case NOT_EQUAL:
		case LE:
		case GE:
		case L_T:
		case G_T:
		case PLUS:
		case MINUS:
		case EDGEEND:
		case EDGE:
		{
			break;
		}
		default:
		{
			throw new NoViableAltException(LT(1), getFilename());
		}
		}
		}
		return expr;
	}
	
/** matches operator and 2nd argument of or- and orElse-Expressions
	@param arg1 first argument
	@param offsetArg1 offset of first argument
	@param lengthArg1 length of first argument
	@return or-expression (FunctionApplication)
*/
	public final FunctionApplication  additiveExpression2(
		Expression arg1, int offsetArg1, int lengthArg1
	) throws RecognitionException, TokenStreamException, ParseException,DuplicateVariableException {
		FunctionApplication functionAppl = null;
		
		
			Expression arg2 = null;
			FunctionId op = null;
		String name = "minus";
		int offsetOperator = 0;
		int offsetArg2 = 0;
		int lengthOperator = 0;
		int lengthArg2 = 0;
		
		
		if ( inputState.guessing==0 ) {
			offsetOperator = LT(1).getColumn()-1;
		}
		{
		switch ( LA(1)) {
		case PLUS:
		{
			match(PLUS);
			if ( inputState.guessing==0 ) {
				name = "plus";
			}
			break;
		}
		case MINUS:
		{
			match(MINUS);
			break;
		}
		default:
		{
			throw new NoViableAltException(LT(1), getFilename());
		}
		}
		}
		if ( inputState.guessing==0 ) {
			lengthOperator = -offsetOperator + LT(0).getColumn()-1 + LT(0).getText().length();
					  offsetArg2 = LT(1).getColumn()-1;
					
		}
		arg2=multiplicativeExpression();
		if ( inputState.guessing==0 ) {
			
			lengthArg2 = -offsetArg2 + LT(0).getColumn()-1 + LT(0).getText().length();
				        functionAppl = graph.createFunctionApplication();
				    	op = (FunctionId) functionSymbolTable.lookup(name);
				        if (op == null) {
					    	op = graph.createFunctionId();
					    	op.setName(name);
					    	functionSymbolTable.insert(name, op);
					    }
					    createFunctionIdAndArgumentOf(functionAppl, op, offsetOperator, lengthOperator, 
							   						  arg1, offsetArg1, lengthArg1, arg2, offsetArg2, lengthArg2); 
					
		}
		{
		switch ( LA(1)) {
		case PLUS:
		case MINUS:
		{
			functionAppl=additiveExpression2(functionAppl, offsetArg1,
            			   -offsetArg1 + LT(0).getColumn()-1 + LT(0).getText().length());
			break;
		}
		case EOF:
		case DOTDOT:
		case AND:
		case ANDTHEN:
		case OR:
		case ORELSE:
		case XOR:
		case AS:
		case END:
		case IN:
		case REPORT:
		case REPORTSET:
		case REPORTBAG:
		case REPORTTABLE:
		case STORE:
		case WHERE:
		case WITH:
		case QUESTION:
		case COLON:
		case COMMA:
		case AT:
		case RPAREN:
		case RBRACK:
		case RCURLY:
		case EQUAL:
		case MATCH:
		case NOT_EQUAL:
		case LE:
		case GE:
		case L_T:
		case G_T:
		case EDGEEND:
		case EDGE:
		{
			break;
		}
		default:
		{
			throw new NoViableAltException(LT(1), getFilename());
		}
		}
		}
		return functionAppl;
	}
	
/** matches unary Expressions (-, not)
	@return
*/
	public final Expression  unaryExpression() throws RecognitionException, TokenStreamException, ParseException,DuplicateVariableException {
		Expression expr = null;
		
		
			Expression arg = null;
			FunctionId unaryOp = null;
		boolean isUnaryExpr = false;
		int offsetOperator = 0;
		int offsetExpr = 0;
		int lengthOperator = 0;
		int lengthExpr = 0;
		
		
		{
		switch ( LA(1)) {
		case NOT:
		case MINUS:
		{
			if ( inputState.guessing==0 ) {
				offsetOperator = LT(1).getColumn()-1;
			}
			unaryOp=unaryOperator();
			if ( inputState.guessing==0 ) {
				
					lengthOperator = -offsetOperator + LT(0).getColumn()-1 + LT(0).getText().length();
					isUnaryExpr = true;
				
			}
			break;
		}
		case NUM_REAL:
		case FUNCTIONID:
		case THIS:
		case FALSE:
		case NULL_VALUE:
		case TRUE:
		case BAG:
		case E:
		case ESUBGRAPH:
		case FROM:
		case LIST:
		case PATH:
		case PATHSYSTEM:
		case REC:
		case SET:
		case TUP:
		case V:
		case VSUBGRAPH:
		case LPAREN:
		case LBRACK:
		case LCURLY:
		case L_T:
		case EDGESTART:
		case EDGE:
		case RARROW:
		case LARROW:
		case ARROW:
		case HASH:
		case STRING_LITERAL:
		case IDENT:
		case NUM_INT:
		{
			break;
		}
		default:
		{
			throw new NoViableAltException(LT(1), getFilename());
		}
		}
		}
		if ( inputState.guessing==0 ) {
			offsetExpr = LT(1).getColumn()-1;
		}
		expr=pathOrGrammarExpression();
		if ( inputState.guessing==0 ) {
			
			lengthExpr = -offsetExpr + LT(0).getColumn()-1 + LT(0).getText().length();
			if (isUnaryExpr)
			{
			arg = expr;
			try {
				   				// create new F.A.
								FunctionApplication fa = graph.createFunctionApplication();
				                expr = fa;
				                // add operator
				                IsFunctionIdOf functionIdOf = graph.createIsFunctionIdOf(unaryOp, fa);
								functionIdOf.setSourcePositions((createSourcePositionList(lengthOperator, offsetOperator)));
								// add argument
				                IsArgumentOf argOf = graph.createIsArgumentOf(arg, fa);
				                argOf.setSourcePositions((createSourcePositionList(lengthExpr, offsetExpr)));
			}
			catch (Exception ex) {
								ex.printStackTrace();
							}
			}
			
		}
		return expr;
	}
	
/** matches operator and 2nd argument of multiplicative-Expressions (*, /, %)
	@param arg1 first argument
	@param offsetArg1 offset of first argument
	@param lengthArg1 length of first argument
	@return or-expression (FunctionApplication)
*/
	public final FunctionApplication  multiplicativeExpression2(
		Expression arg1, int offsetArg1, int lengthArg1
	) throws RecognitionException, TokenStreamException, ParseException,DuplicateVariableException {
		FunctionApplication functionAppl = null;
		
		
			Expression arg2 = null;
			FunctionId op = null;
		String name = "dividedBy";
		int offsetOperator = 0;
		int offsetArg2 = 0;
		int lengthOperator = 0;
		int lengthArg2 = 0;
		
		
		if ( inputState.guessing==0 ) {
			offsetOperator = LT(1).getColumn()-1;
		}
		{
		switch ( LA(1)) {
		case STAR:
		{
			match(STAR);
			if ( inputState.guessing==0 ) {
				name = "times";
			}
			break;
		}
		case MOD:
		{
			match(MOD);
			if ( inputState.guessing==0 ) {
				name = "modulo";
			}
			break;
		}
		case DIV:
		{
			match(DIV);
			break;
		}
		default:
		{
			throw new NoViableAltException(LT(1), getFilename());
		}
		}
		}
		if ( inputState.guessing==0 ) {
			lengthOperator = -offsetOperator + LT(0).getColumn()-1 + LT(0).getText().length();
					  offsetArg2 = LT(1).getColumn()-1;
					
		}
		arg2=unaryExpression();
		if ( inputState.guessing==0 ) {
			
			lengthArg2 = -offsetArg2 + LT(0).getColumn()-1 + LT(0).getText().length();
			try
			{
				            functionAppl = graph.createFunctionApplication();
				    		op = (FunctionId) functionSymbolTable.lookup(name);
				            if (op == null)
				            {
					    		op = graph.createFunctionId();
					    		op.setName(name);
					    		functionSymbolTable.insert(name, op);
				    		}
					createFunctionIdAndArgumentOf(functionAppl, op, offsetOperator, lengthOperator, 
							   						  arg1, offsetArg1, lengthArg1, arg2, offsetArg2, lengthArg2); 	
						} catch (Exception ex) { ex.printStackTrace(); }
					
		}
		{
		switch ( LA(1)) {
		case DIV:
		case STAR:
		case MOD:
		{
			functionAppl=multiplicativeExpression2(functionAppl, offsetArg1,
            			   -offsetArg1 + LT(0).getColumn()-1 + LT(0).getText().length());
			break;
		}
		case EOF:
		case DOTDOT:
		case AND:
		case ANDTHEN:
		case OR:
		case ORELSE:
		case XOR:
		case AS:
		case END:
		case IN:
		case REPORT:
		case REPORTSET:
		case REPORTBAG:
		case REPORTTABLE:
		case STORE:
		case WHERE:
		case WITH:
		case QUESTION:
		case COLON:
		case COMMA:
		case AT:
		case RPAREN:
		case RBRACK:
		case RCURLY:
		case EQUAL:
		case MATCH:
		case NOT_EQUAL:
		case LE:
		case GE:
		case L_T:
		case G_T:
		case PLUS:
		case MINUS:
		case EDGEEND:
		case EDGE:
		{
			break;
		}
		default:
		{
			throw new NoViableAltException(LT(1), getFilename());
		}
		}
		}
		return functionAppl;
	}
	
/** matches one of the unaryOperators '-' and 'not'
	@return functionId-vertex representing the operator
*/
	public final FunctionId  unaryOperator() throws RecognitionException, TokenStreamException, ParseException,DuplicateVariableException {
		FunctionId unaryOp = null;
		
		String name = "uminus";
		
		{
		switch ( LA(1)) {
		case NOT:
		{
			match(NOT);
			if ( inputState.guessing==0 ) {
				name = "not";	
			}
			break;
		}
		case MINUS:
		{
			match(MINUS);
			break;
		}
		default:
		{
			throw new NoViableAltException(LT(1), getFilename());
		}
		}
		}
		if ( inputState.guessing==0 ) {
			
				  unaryOp = (FunctionId) functionSymbolTable.lookup(name);
					  if (unaryOp == null)  {
					  		unaryOp = graph.createFunctionId();
						 	unaryOp.setName(name);
							functionSymbolTable.insert(name, unaryOp);
					  }
					
		}
		return unaryOp;
	}
	
/** matches regular and context free forward- and backvertex sets or
    pathexistences
    @return
*/
	public final Expression  pathOrGrammarExpression() throws RecognitionException, TokenStreamException, ParseException,DuplicateVariableException {
		Expression expr = null;
		
		
			Expression arg1 = null;
			Expression arg2 = null;
			Expression  p = null;
			int offsetArg1 = 0;
			int offsetArg2 = 0;
			int offsetPath = 0;
			int lengthArg1 = 0;
			int lengthArg2 = 0;
			int lengthPath = 0;
		
		
		switch ( LA(1)) {
		case L_T:
		{
			{
			match(L_T);
			if ( inputState.guessing==0 ) {
				offsetArg1 = LT(1).getColumn()-1;
			}
			arg1=primaryExpression();
			if ( inputState.guessing==0 ) {
				lengthArg1 = -offsetArg1 + LT(0).getColumn()-1 + LT(0).getText().length();
			}
			match(HASH);
			if ( inputState.guessing==0 ) {
				offsetPath = LT(1).getColumn()-1;
			}
			{
			if ((_tokenSet_3.member(LA(1)))) {
				p=pathDescription();
			}
			else {
				boolean synPredMatched76 = false;
				if (((LA(1)==IDENT))) {
					int _m76 = mark();
					synPredMatched76 = true;
					inputState.guessing++;
					try {
						{
						match(IDENT);
						match(GASSIGN);
						}
					}
					catch (RecognitionException pe) {
						synPredMatched76 = false;
					}
					rewind(_m76);
inputState.guessing--;
				}
				if ( synPredMatched76 ) {
					p=cfGrammar();
				}
				else if ((LA(1)==IDENT)) {
					p=variable();
				}
				else {
					throw new NoViableAltException(LT(1), getFilename());
				}
				}
				}
				match(HASH);
				if ( inputState.guessing==0 ) {
					lengthPath = -offsetPath + LT(0).getColumn()-1 + LT(0).getText().length();
				}
				{
				switch ( LA(1)) {
				case NUM_REAL:
				case FUNCTIONID:
				case THIS:
				case FALSE:
				case NULL_VALUE:
				case TRUE:
				case BAG:
				case E:
				case ESUBGRAPH:
				case FROM:
				case LIST:
				case PATH:
				case PATHSYSTEM:
				case REC:
				case SET:
				case TUP:
				case V:
				case VSUBGRAPH:
				case LPAREN:
				case STRING_LITERAL:
				case IDENT:
				case NUM_INT:
				{
					{
					if ( inputState.guessing==0 ) {
						
								    		isAdditiveExpression = false;
								    		offsetArg2 = LT(1).getColumn()-1;
								    	
					}
					arg2=primaryExpression();
					match(G_T);
					if ( inputState.guessing==0 ) {
						lengthArg2 = -offsetArg2 + LT(0).getColumn()-1 + LT(0).getText().length();
					}
					}
					break;
				}
				case EOF:
				case DOTDOT:
				case AND:
				case ANDTHEN:
				case OR:
				case ORELSE:
				case XOR:
				case AS:
				case END:
				case IN:
				case REPORT:
				case REPORTSET:
				case REPORTBAG:
				case REPORTTABLE:
				case STORE:
				case WHERE:
				case WITH:
				case QUESTION:
				case COLON:
				case COMMA:
				case AT:
				case RPAREN:
				case RBRACK:
				case RCURLY:
				case EQUAL:
				case MATCH:
				case NOT_EQUAL:
				case LE:
				case GE:
				case L_T:
				case G_T:
				case DIV:
				case PLUS:
				case MINUS:
				case STAR:
				case MOD:
				case EDGEEND:
				case EDGE:
				{
					break;
				}
				default:
				{
					throw new NoViableAltException(LT(1), getFilename());
				}
				}
				}
				if ( inputState.guessing==0 ) {
					
							    try
							    {
							    	if (!isAdditiveExpression) {
								  		// wenn AdditiveExpression false ist, handelt es sich um eine Pfadexistenz.
										PathExistence pe = graph.createPathExistence();
										expr = pe;
							    		IsStartExprOf startVertexOf = graph.createIsStartExprOf(arg1, pe);
							    		startVertexOf.setSourcePositions((createSourcePositionList(lengthArg1, offsetArg1)));
							    		IsTargetExprOf targetVertexOf = graph.createIsTargetExprOf(arg2, pe);
							    		targetVertexOf.setSourcePositions((createSourcePositionList(lengthArg2, offsetArg2)));
							    		IsPathOf pathOf = graph.createIsPathOf(p, pe);
							    		pathOf.setSourcePositions((createSourcePositionList(lengthPath, offsetPath)));
							    	} else {
										ForwardVertexSet fvs = graph.createForwardVertexSet();
										expr = fvs;
							    		IsStartExprOf startVertexOf = graph.createIsStartExprOf(arg1, fvs);
							    		startVertexOf.setSourcePositions((createSourcePositionList(lengthArg1, offsetArg1)));
							    		IsPathOf pathOf = graph.createIsPathOf(p, fvs);
							    		pathOf.setSourcePositions((createSourcePositionList(lengthPath, offsetPath)));
							    	}
							    }
								catch (Exception ex) {
							    	ex.printStackTrace();
							    }
							    // globale Var "isAdditiveExpression" wieder auf true setzen,
							    // da sonst keine Vergleiche mehr gematcht werden koennen
							    isAdditiveExpression = true;
							
				}
				}
				break;
			}
			case HASH:
			{
				match(HASH);
				if ( inputState.guessing==0 ) {
					offsetPath = LT(1).getColumn()-1;
				}
				{
				if ((_tokenSet_3.member(LA(1)))) {
					p=pathDescription();
				}
				else {
					boolean synPredMatched83 = false;
					if (((LA(1)==IDENT))) {
						int _m83 = mark();
						synPredMatched83 = true;
						inputState.guessing++;
						try {
							{
							match(IDENT);
							match(GASSIGN);
							}
						}
						catch (RecognitionException pe) {
							synPredMatched83 = false;
						}
						rewind(_m83);
inputState.guessing--;
					}
					if ( synPredMatched83 ) {
						p=cfGrammar();
					}
					else if ((LA(1)==IDENT)) {
						p=variable();
					}
					else {
						throw new NoViableAltException(LT(1), getFilename());
					}
					}
					}
					if ( inputState.guessing==0 ) {
						lengthPath = -offsetPath + LT(0).getColumn()-1 + LT(0).getText().length();
					}
					match(HASH);
					if ( inputState.guessing==0 ) {
						isAdditiveExpression = false;
							      offsetArg1 = LT(1).getColumn()-1;
							
					}
					{
					switch ( LA(1)) {
					case NUM_REAL:
					case FUNCTIONID:
					case THIS:
					case FALSE:
					case NULL_VALUE:
					case TRUE:
					case BAG:
					case E:
					case ESUBGRAPH:
					case FROM:
					case LIST:
					case PATH:
					case PATHSYSTEM:
					case REC:
					case SET:
					case TUP:
					case V:
					case VSUBGRAPH:
					case LPAREN:
					case STRING_LITERAL:
					case IDENT:
					case NUM_INT:
					{
						arg1=primaryExpression();
						match(G_T);
						if ( inputState.guessing==0 ) {
							
											lengthArg1 = -offsetArg1 + LT(0).getColumn()-1 + LT(0).getText().length();
								    		try {
												BackwardVertexSet bvs = graph.createBackwardVertexSet();
												expr = bvs;
								    			IsTargetExprOf targetVertexOf = graph.createIsTargetExprOf(arg1, bvs);
								    			targetVertexOf.setSourcePositions((createSourcePositionList(lengthArg1, offsetArg1)));
								    			IsPathOf pathOf = graph.createIsPathOf(p, bvs);
								    			pathOf.setSourcePositions((createSourcePositionList(lengthPath, offsetPath)));
								    		}
								    		catch (Exception ex) {
												ex.printStackTrace();
											}
										
						}
						break;
					}
					case EOF:
					case DOTDOT:
					case AND:
					case ANDTHEN:
					case OR:
					case ORELSE:
					case XOR:
					case AS:
					case END:
					case IN:
					case REPORT:
					case REPORTSET:
					case REPORTBAG:
					case REPORTTABLE:
					case STORE:
					case WHERE:
					case WITH:
					case QUESTION:
					case COLON:
					case COMMA:
					case AT:
					case RPAREN:
					case RBRACK:
					case RCURLY:
					case EQUAL:
					case MATCH:
					case NOT_EQUAL:
					case LE:
					case GE:
					case L_T:
					case G_T:
					case DIV:
					case PLUS:
					case MINUS:
					case STAR:
					case MOD:
					case EDGEEND:
					case EDGE:
					{
						break;
					}
					default:
					{
						throw new NoViableAltException(LT(1), getFilename());
					}
					}
					}
					if ( inputState.guessing==0 ) {
						
								  // reset isAdditiveExpression to true...
							    		isAdditiveExpression = true;
										if (arg1 == null) {	expr = p; }
								
					}
					break;
				}
				default:
					boolean synPredMatched68 = false;
					if (((_tokenSet_3.member(LA(1))))) {
						int _m68 = mark();
						synPredMatched68 = true;
						inputState.guessing++;
						try {
							{
							alternativePathDescription();
							{
							switch ( LA(1)) {
							case SMILEY:
							{
								match(SMILEY);
								break;
							}
							case NUM_REAL:
							case FUNCTIONID:
							case THIS:
							case FALSE:
							case NULL_VALUE:
							case TRUE:
							case BAG:
							case E:
							case ESUBGRAPH:
							case FROM:
							case LIST:
							case PATH:
							case PATHSYSTEM:
							case REC:
							case SET:
							case TUP:
							case V:
							case VSUBGRAPH:
							case LPAREN:
							case STRING_LITERAL:
							case IDENT:
							case NUM_INT:
							{
								restrictedExpression();
								break;
							}
							default:
							{
								throw new NoViableAltException(LT(1), getFilename());
							}
							}
							}
							}
						}
						catch (RecognitionException pe) {
							synPredMatched68 = false;
						}
						rewind(_m68);
inputState.guessing--;
					}
					if ( synPredMatched68 ) {
						expr=regBackwardVertexSetOrPathSystem();
					}
					else {
						boolean synPredMatched70 = false;
						if (((_tokenSet_4.member(LA(1))))) {
							int _m70 = mark();
							synPredMatched70 = true;
							inputState.guessing++;
							try {
								{
								alternativePathDescription();
								}
							}
							catch (RecognitionException pe) {
								synPredMatched70 = false;
							}
							rewind(_m70);
inputState.guessing--;
						}
						if ( synPredMatched70 ) {
							expr=primaryExpression();
						}
						else if ((_tokenSet_4.member(LA(1)))) {
							if ( inputState.guessing==0 ) {
								offsetArg1 = LT(1).getColumn()-1;
							}
							expr=restrictedExpression();
							if ( inputState.guessing==0 ) {
								lengthArg1 = -offsetArg1 + LT(0).getColumn()-1 + LT(0).getText().length();
							}
							{
							switch ( LA(1)) {
							case IDENT:
							{
								expr=cfPathExistenceOrForwardVertexSet(expr,offsetArg1,lengthArg1);
								break;
							}
							case SMILEY:
							{
								expr=regPathOrPathSystem(expr, offsetArg1, lengthArg1);
								break;
							}
							default:
								boolean synPredMatched87 = false;
								if (((_tokenSet_3.member(LA(1))))) {
									int _m87 = mark();
									synPredMatched87 = true;
									inputState.guessing++;
									try {
										{
										alternativePathDescription();
										}
									}
									catch (RecognitionException pe) {
										synPredMatched87 = false;
									}
									rewind(_m87);
inputState.guessing--;
								}
								if ( synPredMatched87 ) {
									expr=regPathExistenceOrForwardVertexSet(expr, offsetArg1, lengthArg1);
								}
								else if ((_tokenSet_5.member(LA(1)))) {
								}
							else {
								throw new NoViableAltException(LT(1), getFilename());
							}
							}
							}
						}
					else {
						throw new NoViableAltException(LT(1), getFilename());
					}
					}}
					return expr;
				}
				
/** matches regular backwardvertex sets and pathsystem expressions beginning with
     a pathdescription
    @return
*/
	public final Expression  regBackwardVertexSetOrPathSystem() throws RecognitionException, TokenStreamException, ParseException,DuplicateVariableException {
		Expression expr = null;
		
		
			PathDescription pathDescr = null;
		Expression restrExpr = null;
		boolean isPathSystem = false;
		int offsetPathDescr = 0;
		int offsetExpr = 0;
		int offsetOperator = 0;
		int lengthPathDescr = 0;
		int lengthExpr = 0;
		
		
		if ( inputState.guessing==0 ) {
			offsetPathDescr = LT(1).getColumn()-1;
		}
		pathDescr=pathDescription();
		if ( inputState.guessing==0 ) {
			lengthPathDescr = -offsetPathDescr + LT(0).getColumn()-1 + LT(0).getText().length();
		}
		{
		switch ( LA(1)) {
		case SMILEY:
		{
			if ( inputState.guessing==0 ) {
				offsetOperator = LT(1).getColumn()-1;
			}
			match(SMILEY);
			if ( inputState.guessing==0 ) {
					isPathSystem = true;
				
			}
			break;
		}
		case NUM_REAL:
		case FUNCTIONID:
		case THIS:
		case FALSE:
		case NULL_VALUE:
		case TRUE:
		case BAG:
		case E:
		case ESUBGRAPH:
		case FROM:
		case LIST:
		case PATH:
		case PATHSYSTEM:
		case REC:
		case SET:
		case TUP:
		case V:
		case VSUBGRAPH:
		case LPAREN:
		case STRING_LITERAL:
		case IDENT:
		case NUM_INT:
		{
			break;
		}
		default:
		{
			throw new NoViableAltException(LT(1), getFilename());
		}
		}
		}
		if ( inputState.guessing==0 ) {
			offsetExpr = LT(1).getColumn()-1;
		}
		restrExpr=restrictedExpression();
		if ( inputState.guessing==0 ) {
			
					    lengthExpr = -offsetExpr + LT(0).getColumn()-1 + LT(0).getText().length();
				    try {
				            if (isPathSystem) {
					   			// create a path-system-functionapplication
								FunctionApplication fa = graph.createFunctionApplication();
								expr = fa;
								FunctionId f = (FunctionId )functionSymbolTable.lookup("pathSystem");
								if (f == null)
								{
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
						catch (Exception ex) {
							ex.printStackTrace();
						}
			
		}
		return expr;
	}
	
/** matches a pathdescription
*/
	public final PathDescription  pathDescription() throws RecognitionException, TokenStreamException, ParseException,DuplicateVariableException {
		PathDescription pathDescr = null;
		
		
		pathDescr=alternativePathDescription();
		return pathDescr;
	}
	
/** matches restricted vertex expressions
    (and merges this-literals with the expression that is restricted)
    @return
*/
	public final Expression  restrictedExpression() throws RecognitionException, TokenStreamException, ParseException,DuplicateVariableException {
		Expression expr = null;
		
		
			Expression restr = null;
		RestrictedExpression restrExpr = null;
		int offsetExpr = 0;
		int offsetRestr = 0;
		int lengthExpr = 0;
		int lengthRestr = 0;
		
		
		if ( inputState.guessing==0 ) {
			offsetExpr = LT(1).getColumn()-1;
		}
		expr=valueAccess();
		if ( inputState.guessing==0 ) {
			lengthExpr = -offsetExpr + LT(0).getColumn()-1 + LT(0).getText().length();
		}
		{
		switch ( LA(1)) {
		case AMP:
		{
			{
			match(AMP);
			match(LCURLY);
			if ( inputState.guessing==0 ) {
				offsetRestr = LT(1).getColumn()-1;
			}
			restr=expression();
			if ( inputState.guessing==0 ) {
				lengthRestr = -offsetRestr + LT(0).getColumn()-1 + LT(0).getText().length();
			}
			match(RCURLY);
			if ( inputState.guessing==0 ) {
				
					try
					{   // create new restrictedExpr
						restrExpr = graph.createRestrictedExpression();
					                    // add expression
					                    IsRestrictedExprOf restrExprOf = graph.createIsRestrictedExprOf(expr, restrExpr);
					                    restrExprOf.setSourcePositions((createSourcePositionList(lengthExpr, offsetExpr)));
										// add restriction
					                    IsRestrictionOf restrOf = graph.createIsRestrictionOf(restr, restrExpr);
					                    restrOf.setSourcePositions((createSourcePositionList(lengthRestr, offsetRestr)));
					                	// merge this-literals in restriction with vertex
					                	mergeRestrictedExpr(restr, expr);
										expr = restrExpr;
									} catch (Exception ex) { ex.printStackTrace(); }
				
			}
			}
			break;
		}
		case EOF:
		case DOTDOT:
		case AND:
		case ANDTHEN:
		case OR:
		case ORELSE:
		case XOR:
		case AS:
		case END:
		case IN:
		case REPORT:
		case REPORTSET:
		case REPORTBAG:
		case REPORTTABLE:
		case STORE:
		case WHERE:
		case WITH:
		case QUESTION:
		case COLON:
		case COMMA:
		case AT:
		case LPAREN:
		case RPAREN:
		case LBRACK:
		case RBRACK:
		case LCURLY:
		case RCURLY:
		case EQUAL:
		case MATCH:
		case NOT_EQUAL:
		case LE:
		case GE:
		case L_T:
		case G_T:
		case DIV:
		case PLUS:
		case MINUS:
		case STAR:
		case MOD:
		case SMILEY:
		case EDGESTART:
		case EDGEEND:
		case EDGE:
		case RARROW:
		case LARROW:
		case ARROW:
		case IDENT:
		{
			break;
		}
		default:
		{
			throw new NoViableAltException(LT(1), getFilename());
		}
		}
		}
		return expr;
	}
	
/** matches expressions that compute regular paths or pathsystems starting with a
    vertex expression
    @param arg1 startvertex
    @param offsetArg1 offset of the startvertex-expression
    @param lengthArg1 length of the startvertex-expression
*/
	public final Expression  regPathOrPathSystem(
		Expression arg1, int offsetArg1, int lengthArg1
	) throws RecognitionException, TokenStreamException, ParseException,DuplicateVariableException {
		Expression expr = null;
		
		
			PathDescription pathDescr = null;
			Expression restrExpr = null;
		boolean isPath = false;
		int offsetPathDescr = 0;
		int offsetOperator1 = 0;
		int offsetOperator2 = 0;
		int offsetExpr = 0;
		int lengthPathDescr = 0;
		int lengthExpr = 0;
		
		
		if ( inputState.guessing==0 ) {
			offsetOperator1 = LT(1).getColumn()-1;
		}
		match(SMILEY);
		if ( inputState.guessing==0 ) {
			
				offsetPathDescr = LT(1).getColumn()-1;
			
		}
		pathDescr=pathDescription();
		if ( inputState.guessing==0 ) {
			lengthPathDescr = -offsetPathDescr + LT(0).getColumn()-1 + LT(0).getText().length();
		}
		{
		switch ( LA(1)) {
		case SMILEY:
		{
			if ( inputState.guessing==0 ) {
				offsetOperator2 = LT(1).getColumn()-1;
			}
			match(SMILEY);
			if ( inputState.guessing==0 ) {
				
						offsetExpr = LT(1).getColumn()-1;
					
			}
			restrExpr=restrictedExpression();
			if ( inputState.guessing==0 ) {
				
					lengthExpr = -offsetPathDescr + LT(0).getColumn()-1 + LT(0).getText().length();
					isPath = true;
				
			}
			break;
		}
		case EOF:
		case DOTDOT:
		case AND:
		case ANDTHEN:
		case OR:
		case ORELSE:
		case XOR:
		case AS:
		case END:
		case IN:
		case REPORT:
		case REPORTSET:
		case REPORTBAG:
		case REPORTTABLE:
		case STORE:
		case WHERE:
		case WITH:
		case QUESTION:
		case COLON:
		case COMMA:
		case AT:
		case RPAREN:
		case RBRACK:
		case RCURLY:
		case EQUAL:
		case MATCH:
		case NOT_EQUAL:
		case LE:
		case GE:
		case L_T:
		case G_T:
		case DIV:
		case PLUS:
		case MINUS:
		case STAR:
		case MOD:
		case EDGEEND:
		case EDGE:
		{
			break;
		}
		default:
		{
			throw new NoViableAltException(LT(1), getFilename());
		}
		}
		}
		if ( inputState.guessing==0 ) {
			
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
		return expr;
	}
	
/** matches regular path-existences or regular forward-vertex-sets
	@param arg1 startvertex-expression
	@param offsetArg1 offset of the start-expression
	@param lengthArg1 length of the start-expression
	@return
*/
	public final Expression  regPathExistenceOrForwardVertexSet(
		Expression arg1, int offsetArg1, int lengthArg1
	) throws RecognitionException, TokenStreamException, ParseException,DuplicateVariableException {
		Expression expr = null;
		
		
			PathDescription pathDescr = null;
			Expression restrExpr = null;
			int offsetPathDescr = 0;
			int offsetExpr = 0;
			int lengthPathDescr = 0;
			int lengthExpr = 0;
		
		
		if ( inputState.guessing==0 ) {
			offsetPathDescr = LT(1).getColumn()-1;
		}
		pathDescr=pathDescription();
		if ( inputState.guessing==0 ) {
			lengthPathDescr = -offsetPathDescr + LT(0).getColumn()-1 + LT(0).getText().length();
		}
		{
		switch ( LA(1)) {
		case NUM_REAL:
		case FUNCTIONID:
		case THIS:
		case FALSE:
		case NULL_VALUE:
		case TRUE:
		case BAG:
		case E:
		case ESUBGRAPH:
		case FROM:
		case LIST:
		case PATH:
		case PATHSYSTEM:
		case REC:
		case SET:
		case TUP:
		case V:
		case VSUBGRAPH:
		case LPAREN:
		case STRING_LITERAL:
		case IDENT:
		case NUM_INT:
		{
			if ( inputState.guessing==0 ) {
				offsetExpr = LT(1).getColumn()-1;
			}
			restrExpr=restrictedExpression();
			if ( inputState.guessing==0 ) {
				
				lengthExpr = -offsetExpr + LT(0).getColumn()-1 + LT(0).getText().length();
				
			}
			break;
		}
		case EOF:
		case DOTDOT:
		case AND:
		case ANDTHEN:
		case OR:
		case ORELSE:
		case XOR:
		case AS:
		case END:
		case IN:
		case REPORT:
		case REPORTSET:
		case REPORTBAG:
		case REPORTTABLE:
		case STORE:
		case WHERE:
		case WITH:
		case QUESTION:
		case COLON:
		case COMMA:
		case AT:
		case RPAREN:
		case RBRACK:
		case RCURLY:
		case EQUAL:
		case MATCH:
		case NOT_EQUAL:
		case LE:
		case GE:
		case L_T:
		case G_T:
		case DIV:
		case PLUS:
		case MINUS:
		case STAR:
		case MOD:
		case EDGEEND:
		case EDGE:
		{
			break;
		}
		default:
		{
			throw new NoViableAltException(LT(1), getFilename());
		}
		}
		}
		if ( inputState.guessing==0 ) {
			
					    try {
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
						catch (Exception ex) {
							ex.printStackTrace();
						}
					
		}
		return expr;
	}
	
/** matches primary expressions (elementset, literal,
	valueConstruction, functionAppl., subgraph, simpleQuery, cfGrammar, variable)
*/
	public final Expression  primaryExpression2() throws RecognitionException, TokenStreamException, ParseException,DuplicateVariableException {
		Expression expr = null;
		
		
		switch ( LA(1)) {
		case LPAREN:
		{
			{
			match(LPAREN);
			expr=expression();
			match(RPAREN);
			}
			break;
		}
		case E:
		case V:
		{
			expr=rangeExpression();
			break;
		}
		case NUM_REAL:
		case THIS:
		case FALSE:
		case NULL_VALUE:
		case TRUE:
		case STRING_LITERAL:
		case NUM_INT:
		{
			expr=literal();
			break;
		}
		case BAG:
		case LIST:
		case PATH:
		case PATHSYSTEM:
		case REC:
		case SET:
		case TUP:
		{
			expr=valueConstruction();
			break;
		}
		case FUNCTIONID:
		{
			expr=functionApplication();
			break;
		}
		case ESUBGRAPH:
		case VSUBGRAPH:
		{
			expr=graphRangeExpression();
			break;
		}
		case FROM:
		{
			expr=simpleQuery();
			break;
		}
		case IDENT:
		{
			{
			boolean synPredMatched117 = false;
			if (((LA(1)==IDENT))) {
				int _m117 = mark();
				synPredMatched117 = true;
				inputState.guessing++;
				try {
					{
					match(IDENT);
					match(GASSIGN);
					}
				}
				catch (RecognitionException pe) {
					synPredMatched117 = false;
				}
				rewind(_m117);
inputState.guessing--;
			}
			if ( synPredMatched117 ) {
				expr=cfGrammar();
			}
			else if ((LA(1)==IDENT)) {
				expr=variable();
			}
			else {
				throw new NoViableAltException(LT(1), getFilename());
			}
			
			}
			break;
		}
		default:
		{
			throw new NoViableAltException(LT(1), getFilename());
		}
		}
		return expr;
	}
	
/** matches contextfree path existences and contextfree forward-vertex-sets
	@param arg1 start-vertex-expression
	@param offsetArg1 offset of the start-expression
	@param lengthArg1 length of the start-expression
	@return
*/
	public final Expression  cfPathExistenceOrForwardVertexSet(
		Expression arg1, int offsetArg1, int lengthArg1
	) throws RecognitionException, TokenStreamException, ParseException,DuplicateVariableException {
		Expression expr = null;
		
		
			Expression grammar = null;
			Expression arg2 = null;
			int offsetGrammar = 0;
			int lengthGrammar = 0;
			int offsetArg2 = 0;
			int lengthArg2 = 0;
		
		
		if ( inputState.guessing==0 ) {
			offsetGrammar = LT(1).getColumn()-1;
		}
		grammar=cfGrammar();
		if ( inputState.guessing==0 ) {
			lengthGrammar = -offsetGrammar + LT(0).getColumn()-1 + LT(0).getText().length();
		}
		{
		switch ( LA(1)) {
		case NUM_REAL:
		case FUNCTIONID:
		case THIS:
		case FALSE:
		case NULL_VALUE:
		case TRUE:
		case BAG:
		case E:
		case ESUBGRAPH:
		case FROM:
		case LIST:
		case PATH:
		case PATHSYSTEM:
		case REC:
		case SET:
		case TUP:
		case V:
		case VSUBGRAPH:
		case LPAREN:
		case STRING_LITERAL:
		case IDENT:
		case NUM_INT:
		{
			if ( inputState.guessing==0 ) {
				offsetArg2 = LT(1).getColumn()-1;
			}
			arg2=restrictedExpression();
			if ( inputState.guessing==0 ) {
				lengthArg2 = -offsetArg2 + LT(0).getColumn()-1 + LT(0).getText().length();
			}
			break;
		}
		case EOF:
		case DOTDOT:
		case AND:
		case ANDTHEN:
		case OR:
		case ORELSE:
		case XOR:
		case AS:
		case END:
		case IN:
		case REPORT:
		case REPORTSET:
		case REPORTBAG:
		case REPORTTABLE:
		case STORE:
		case WHERE:
		case WITH:
		case QUESTION:
		case COLON:
		case COMMA:
		case AT:
		case RPAREN:
		case RBRACK:
		case RCURLY:
		case EQUAL:
		case MATCH:
		case NOT_EQUAL:
		case LE:
		case GE:
		case L_T:
		case G_T:
		case DIV:
		case PLUS:
		case MINUS:
		case STAR:
		case MOD:
		case EDGEEND:
		case EDGE:
		{
			break;
		}
		default:
		{
			throw new NoViableAltException(LT(1), getFilename());
		}
		}
		}
		if ( inputState.guessing==0 ) {
			
						try	{
							PathExpression pe;
				 			if (arg2 == null) {
								// no target-vertex => it's a forward-vertex-set
				 				pe = graph.createForwardVertexSet();
				 			} else {
				 				pe = graph.createPathExistence();
				 			}
							expr = pe;
				 			// add start vertex
				 			IsStartExprOf isStartExpr = graph.createIsStartExprOf(arg1, pe);
				 			isStartExpr.setSourcePositions((createSourcePositionList(lengthArg1, offsetArg1)));
				 			// add contextfree grammar
				 			IsPathOf isPath = graph.createIsPathOf(grammar, pe);
				 			isPath.setSourcePositions((createSourcePositionList(lengthGrammar, offsetGrammar)));
				 			// add target vertex, if it's a pathexistence
			
				 			if (arg2 != null) {
				 				IsTargetExprOf isTargetExpr = graph.createIsTargetExprOf(arg2, pe);
				 				isTargetExpr.setSourcePositions((createSourcePositionList(lengthArg2, offsetArg2)));
				 			}
						}
						catch (Exception ex) {
							ex.printStackTrace();
						}
					
		}
		return expr;
	}
	
/** matches context free grammars
	@return
*/
	public final CfGrammar  cfGrammar() throws RecognitionException, TokenStreamException, ParseException,DuplicateVariableException {
		CfGrammar expr = null;
		
		
			Vector<VertexPosition> prodList = new Vector<VertexPosition>();
		
		
		if ( inputState.guessing==0 ) {
			nonterminalSymbolTable.blockBegin();
		}
		prodList=cfProductionList();
		if ( inputState.guessing==0 ) {
			
						try {
							expr = graph.createCfGrammar();
							for (int i = 0; i < prodList.size(); i++)
							{
								VertexPosition prod = prodList.get(i);
								IsProductionOf productionOf = graph.createIsProductionOf((Production)prod.node, expr);
								productionOf.setSourcePositions((createSourcePositionList(prod.length, prod.offset)));
							}
						} catch (Exception ex){ex.printStackTrace();}
					
		}
		if ( inputState.guessing==0 ) {
			nonterminalSymbolTable.blockEnd();
		}
		return expr;
	}
	
/** matches a contextfree path (':-)'-Operator)
	@param arg1 start-vertex expression
	@param offsetArg1 offset of start-expression
	@param lengthArg1 length of start-expression
	@return
*/
	public final Expression  cfPath(
		Expression arg1, int offsetArg1, int lengthArg1
	) throws RecognitionException, TokenStreamException, ParseException,DuplicateVariableException {
		Expression expr = null;
		
		
			Expression grammar = null;
			Expression arg2 = null;
			int offsetGrammar = 0;
			int offsetArg2 = 0;
			int lengthGrammar = 0;
			int lengthArg2 = 0;
			int offsetOp1 = 0;
			// int offsetOp2 = 0;
		
		
		if ( inputState.guessing==0 ) {
			offsetOp1 = LT(1).getColumn()-1;
		}
		match(SMILEY);
		if ( inputState.guessing==0 ) {
			offsetGrammar = LT(1).getColumn()-1;
		}
		grammar=cfGrammar();
		if ( inputState.guessing==0 ) {
			lengthGrammar = -offsetGrammar + LT(0).getColumn()-1 + LT(0).getText().length();
					  // offsetOp2 = LT(1).getColumn()-1;
					
		}
		match(SMILEY);
		if ( inputState.guessing==0 ) {
			offsetArg2 = LT(1).getColumn()-1;
		}
		arg2=restrictedExpression();
		if ( inputState.guessing==0 ) {
			
						lengthArg2 = -offsetArg2 + LT(0).getColumn()-1 + LT(0).getText().length();
						try
						{
							FunctionApplication fa = graph.createFunctionApplication();
							expr = fa;
			
							FunctionId f = (FunctionId) functionSymbolTable.lookup("pathSystem");
							if (f == null)
							{
								graph.createFunctionId();
								f.setName("pathSystem");
								functionSymbolTable.insert("pathSystem", f);
							}
							IsFunctionIdOf functionIdOf = graph.createIsFunctionIdOf(f, fa);
							functionIdOf.setSourcePositions((createSourcePositionList(3, offsetOp1)));
							IsArgumentOf arg1Of = graph.createIsArgumentOf(arg1,fa);
							arg1Of.setSourcePositions((createSourcePositionList(lengthArg1, offsetArg1)));
							IsArgumentOf arg2Of = graph.createIsArgumentOf(arg2, fa);
							arg2Of.setSourcePositions((createSourcePositionList(lengthArg2, offsetArg2)));
							IsArgumentOf arg3Of = graph.createIsArgumentOf(grammar, fa);
							arg3Of.setSourcePositions((createSourcePositionList(lengthGrammar, offsetGrammar)));
						} catch (Exception ex) { ex.printStackTrace(); }
					
		}
		return expr;
	}
	
/** matches an alternative pathdescription
	@return
*/
	public final PathDescription  alternativePathDescription() throws RecognitionException, TokenStreamException, ParseException,DuplicateVariableException {
		PathDescription pathDescr = null;
		
		
			int offsetPathDescr = 0;
			int lengthPathDescr = 0;
		
		
		if ( inputState.guessing==0 ) {
			offsetPathDescr = LT(1).getColumn()-1;
		}
		pathDescr=intermediateVertexPathDescription();
		{
		switch ( LA(1)) {
		case BOR:
		{
			if ( inputState.guessing==0 ) {
				
								lengthPathDescr = -offsetPathDescr + LT(0).getColumn()-1 + LT(0).getText().length();
								try
								{
									AlternativePathDescription altPathDescr = graph.createAlternativePathDescription();
					    	        IsAlternativePathOf alt1Of = graph.createIsAlternativePathOf(pathDescr, altPathDescr);
					    	        alt1Of.setSourcePositions((createSourcePositionList(lengthPathDescr, offsetPathDescr )));
									pathDescr = altPathDescr;
								}
								catch (Exception ex) { ex.printStackTrace(); }
							
			}
			pathDescr=alternativePathDescription2((AlternativePathDescription)pathDescr, offsetPathDescr, lengthPathDescr);
			break;
		}
		case EOF:
		case NUM_REAL:
		case DOTDOT:
		case FUNCTIONID:
		case THIS:
		case AND:
		case ANDTHEN:
		case FALSE:
		case NULL_VALUE:
		case OR:
		case ORELSE:
		case TRUE:
		case XOR:
		case AS:
		case BAG:
		case E:
		case ESUBGRAPH:
		case END:
		case FROM:
		case IN:
		case LIST:
		case PATH:
		case PATHSYSTEM:
		case REC:
		case REPORT:
		case REPORTSET:
		case REPORTBAG:
		case REPORTTABLE:
		case STORE:
		case SET:
		case TUP:
		case V:
		case VSUBGRAPH:
		case WHERE:
		case WITH:
		case QUESTION:
		case COLON:
		case COMMA:
		case AT:
		case LPAREN:
		case RPAREN:
		case RBRACK:
		case RCURLY:
		case EQUAL:
		case MATCH:
		case NOT_EQUAL:
		case LE:
		case GE:
		case L_T:
		case G_T:
		case DIV:
		case PLUS:
		case MINUS:
		case STAR:
		case MOD:
		case SMILEY:
		case EDGEEND:
		case EDGE:
		case HASH:
		case STRING_LITERAL:
		case IDENT:
		case NUM_INT:
		{
			break;
		}
		default:
		{
			throw new NoViableAltException(LT(1), getFilename());
		}
		}
		}
		return pathDescr;
	}
	
/**	@see primaryExpression2
*/
	public final Expression  primaryExpression() throws RecognitionException, TokenStreamException, ParseException,DuplicateVariableException {
		Expression expr = null;
		
		
		expr=primaryExpression2();
		return expr;
	}
	
/** matches a variable
	@return the variable-vertex
*/
	public final Variable  variable() throws RecognitionException, TokenStreamException, ParseException,DuplicateVariableException {
		Variable var = null;
		
		Token  i = null;
		
		i = LT(1);
		match(IDENT);
		if ( inputState.guessing==0 ) {
			
				var = graph.createVariable();
				        var.setName(i.getText());
			
		}
		return var;
	}
	
/** matches first argument of value-accesses
	@return vertex representing the value-Access-Expression
*/
	public final Expression  valueAccess() throws RecognitionException, TokenStreamException, ParseException,DuplicateVariableException {
		Expression expr = null;
		
		
			int offset = 0;
			int length = 0;
		
		
		if ( inputState.guessing==0 ) {
			offset = LT(1).getColumn()-1;
		}
		expr=primaryExpression();
		if ( inputState.guessing==0 ) {
			length = -offset + LT(0).getColumn()-1 + LT(0).getText().length();
		}
		{
		boolean synPredMatched100 = false;
		if (((_tokenSet_6.member(LA(1))))) {
			int _m100 = mark();
			synPredMatched100 = true;
			inputState.guessing++;
			try {
				{
				match(LBRACK);
				primaryPathDescription();
				}
			}
			catch (RecognitionException pe) {
				synPredMatched100 = false;
			}
			rewind(_m100);
inputState.guessing--;
		}
		if ( synPredMatched100 ) {
		}
		else {
			boolean synPredMatched102 = false;
			if (((LA(1)==DOT||LA(1)==LBRACK))) {
				int _m102 = mark();
				synPredMatched102 = true;
				inputState.guessing++;
				try {
					{
					switch ( LA(1)) {
					case DOT:
					{
						match(DOT);
						break;
					}
					case LBRACK:
					{
						match(LBRACK);
						break;
					}
					default:
					{
						throw new NoViableAltException(LT(1), getFilename());
					}
					}
					}
				}
				catch (RecognitionException pe) {
					synPredMatched102 = false;
				}
				rewind(_m102);
inputState.guessing--;
			}
			if ( synPredMatched102 ) {
				expr=valueAccess2(expr, offset, length);
			}
			else if ((_tokenSet_6.member(LA(1)))) {
			}
			else {
				throw new NoViableAltException(LT(1), getFilename());
			}
			}
			}
			return expr;
		}
		
/** matches a primary pathdescription, i.e. one of<br>
	- simple pathdescription
	- edge-pathdescription
	- pathdescription in parenthesis
	- optional pathdescription
	@return
*/
	public final PathDescription  primaryPathDescription() throws RecognitionException, TokenStreamException, ParseException,DuplicateVariableException {
		PathDescription pathDescr = null;
		
		
			int offset = 0;
			int length = 0;
		
		
		switch ( LA(1)) {
		case EDGESTART:
		case EDGE:
		case RARROW:
		case LARROW:
		case ARROW:
		{
			{
			{
			switch ( LA(1)) {
			case RARROW:
			case LARROW:
			case ARROW:
			{
				pathDescr=simplePathDescription();
				break;
			}
			case EDGESTART:
			case EDGE:
			{
				pathDescr=edgePathDescription();
				break;
			}
			default:
			{
				throw new NoViableAltException(LT(1), getFilename());
			}
			}
			}
			}
			break;
		}
		case LPAREN:
		{
			{
			match(LPAREN);
			pathDescr=pathDescription();
			match(RPAREN);
			}
			break;
		}
		case LBRACK:
		{
			{
			match(LBRACK);
			if ( inputState.guessing==0 ) {
				offset = LT(1).getColumn()-1;
			}
			pathDescr=pathDescription();
			if ( inputState.guessing==0 ) {
				length = -offset + LT(0).getColumn()-1 + LT(0).getText().length();
			}
			match(RBRACK);
			if ( inputState.guessing==0 ) {
				
					try
					{
					              	OptionalPathDescription optPathDescr = graph.createOptionalPathDescription();
									IsOptionalPathOf optionalPathOf = graph.createIsOptionalPathOf(pathDescr, optPathDescr);
									optionalPathOf.setSourcePositions((createSourcePositionList(length, offset)));
					                pathDescr = optPathDescr;
				}
				catch (Exception ex) { ex.printStackTrace(); }
				
			}
			}
			break;
		}
		default:
		{
			throw new NoViableAltException(LT(1), getFilename());
		}
		}
		return pathDescr;
	}
	
/** matches operator and 2nd argument of valueAccess
	@param arg1 first argument-expression
	@param offsetArg1 offset of first argument-expression
	@param lengthArg1 length of first argument-expression
	@return vertex representing the value-access-expression
*/
	public final Expression  valueAccess2(
		Expression arg1, int offsetArg1, int lengthArg1
	) throws RecognitionException, TokenStreamException, ParseException,DuplicateVariableException {
		Expression expr = null;
		
		Token  i = null;
		
			Expression arg2 = null;
			FunctionId  functionId = null;
		String name = "nthElement";
		int offsetArg2 = 0;
		int offsetOperator = 0;
		int lengthArg2 = 0;
		int lengthOperator = 0;
		
		
		if ( inputState.guessing==0 ) {
			offsetOperator = LT(1).getColumn()-1;
		}
		{
		switch ( LA(1)) {
		case DOT:
		{
			{
			match(DOT);
			if ( inputState.guessing==0 ) {
				
							lengthOperator = 1;
							offsetArg2 = LT(1).getColumn()-1;
						
			}
			i = LT(1);
			match(IDENT);
			if ( inputState.guessing==0 ) {
				
					name = "getValue";
					try
					{
					            	arg2 = graph.createIdentifier();
					            	((Identifier)arg2).setName(i.getText());
					            	lengthArg2 = i.getText().length();
					} catch(Exception ex){ex.printStackTrace();}
				
			}
			}
			break;
		}
		case LBRACK:
		{
			{
			match(LBRACK);
			if ( inputState.guessing==0 ) {
				offsetArg2 = LT(1).getColumn()-1;
			}
			arg2=expression();
			if ( inputState.guessing==0 ) {
				
						name = "nthElement";
					lengthArg2 = -offsetArg2 + LT(0).getColumn()-1 +LT(0).getText().length();
				
			}
			match(RBRACK);
			if ( inputState.guessing==0 ) {
				
					lengthOperator = -offsetOperator + LT(0).getColumn()-1 + LT(0).getText().length();
				
			}
			}
			break;
		}
		default:
		{
			throw new NoViableAltException(LT(1), getFilename());
		}
		}
		}
		if ( inputState.guessing==0 ) {
			
						try {
							FunctionApplication fa = graph.createFunctionApplication();
							expr = fa;
							functionId = (FunctionId) functionSymbolTable.lookup(name);
							if (functionId == null) {
								functionId = graph.createFunctionId();
								functionId.setName(name);
								functionSymbolTable.insert(name, functionId);
							}
							IsFunctionIdOf functionIdOf = graph.createIsFunctionIdOf(functionId, fa);
							functionIdOf.setSourcePositions((createSourcePositionList(lengthOperator, offsetOperator)));
							IsArgumentOf arg1Of = graph.createIsArgumentOf(arg1, fa);
							arg1Of.setSourcePositions((createSourcePositionList(lengthArg1, offsetArg1)));
							IsArgumentOf arg2Of = graph.createIsArgumentOf(arg2, fa);
							arg2Of.setSourcePositions((createSourcePositionList(lengthArg2, offsetArg2)));
						}
						catch (Exception ex) {
							ex.printStackTrace();
						}
					
		}
		{
		boolean synPredMatched109 = false;
		if (((_tokenSet_6.member(LA(1))))) {
			int _m109 = mark();
			synPredMatched109 = true;
			inputState.guessing++;
			try {
				{
				match(LBRACK);
				primaryPathDescription();
				}
			}
			catch (RecognitionException pe) {
				synPredMatched109 = false;
			}
			rewind(_m109);
inputState.guessing--;
		}
		if ( synPredMatched109 ) {
		}
		else {
			boolean synPredMatched111 = false;
			if (((LA(1)==DOT||LA(1)==LBRACK))) {
				int _m111 = mark();
				synPredMatched111 = true;
				inputState.guessing++;
				try {
					{
					switch ( LA(1)) {
					case DOT:
					{
						match(DOT);
						break;
					}
					case LBRACK:
					{
						match(LBRACK);
						break;
					}
					default:
					{
						throw new NoViableAltException(LT(1), getFilename());
					}
					}
					}
				}
				catch (RecognitionException pe) {
					synPredMatched111 = false;
				}
				rewind(_m111);
inputState.guessing--;
			}
			if ( synPredMatched111 ) {
				expr=valueAccess2(expr, offsetArg1,
			 -offsetArg1 + LT(0).getColumn()-1 +LT(0).getText().length());
			}
			else if ((_tokenSet_6.member(LA(1)))) {
			}
			else {
				throw new NoViableAltException(LT(1), getFilename());
			}
			}
			}
			return expr;
		}
		
/**	matches an element-set-expression: (E|V) [{typeExpressionList}]
*/
	public final Expression  rangeExpression() throws RecognitionException, TokenStreamException, ParseException,DuplicateVariableException {
		Expression expr = null;
		
		
			Vector<VertexPosition> typeIds = new Vector<VertexPosition>();
		
		
		{
		switch ( LA(1)) {
		case V:
		{
			match(V);
			if ( inputState.guessing==0 ) {
				expr = graph.createVertexSetExpression();
			}
			break;
		}
		case E:
		{
			match(E);
			if ( inputState.guessing==0 ) {
				expr = graph.createEdgeSetExpression();
			}
			break;
		}
		default:
		{
			throw new NoViableAltException(LT(1), getFilename());
		}
		}
		}
		{
		boolean synPredMatched264 = false;
		if (((LA(1)==LCURLY))) {
			int _m264 = mark();
			synPredMatched264 = true;
			inputState.guessing++;
			try {
				{
				match(LCURLY);
				{
				switch ( LA(1)) {
				case CARET:
				case IDENT:
				{
					typeExpressionList();
					break;
				}
				case RCURLY:
				{
					break;
				}
				default:
				{
					throw new NoViableAltException(LT(1), getFilename());
				}
				}
				}
				match(RCURLY);
				}
			}
			catch (RecognitionException pe) {
				synPredMatched264 = false;
			}
			rewind(_m264);
inputState.guessing--;
		}
		if ( synPredMatched264 ) {
			{
			match(LCURLY);
			{
			switch ( LA(1)) {
			case CARET:
			case IDENT:
			{
				typeIds=typeExpressionList();
				break;
			}
			case RCURLY:
			{
				break;
			}
			default:
			{
				throw new NoViableAltException(LT(1), getFilename());
			}
			}
			}
			match(RCURLY);
			}
		}
		else if ((_tokenSet_7.member(LA(1)))) {
		}
		else {
			throw new NoViableAltException(LT(1), getFilename());
		}
		
		}
		if ( inputState.guessing==0 ) {
			
				    for (int i = 0; i < typeIds.size(); i++) {
								VertexPosition t = typeIds.get(i);
								IsTypeRestrOf typeRestrOf = graph.createIsTypeRestrOf((TypeId)t.node, expr);
								typeRestrOf.setSourcePositions((createSourcePositionList(t.length, t.offset)));
							}
			
		}
		return expr;
	}
	
/** matches string-, this-, int-, real-, boolean- and null-literals
	@return
*/
	public final Literal  literal() throws RecognitionException, TokenStreamException, ParseException,DuplicateVariableException {
		Literal literal = null;
		
		Token  s = null;
		Token  t = null;
		Token  i = null;
		Token  r = null;
		
		switch ( LA(1)) {
		case STRING_LITERAL:
		{
			s = LT(1);
			match(STRING_LITERAL);
			if ( inputState.guessing==0 ) {
				
					literal = graph.createStringLiteral();
					((StringLiteral) literal).setStringValue(decode(s.getText()));
				
			}
			break;
		}
		case THIS:
		{
			t = LT(1);
			match(THIS);
			if ( inputState.guessing==0 ) {
				
					VertexClass thisLiteralVertexClass = (VertexClass) graphClass.getGraphElementClass("ThisLiteral");
					literal = (ThisLiteral) graph.getFirstVertexOfClass(thisLiteralVertexClass, true);
					while (literal != null)	{
					               if ( literal != null && ((ThisLiteral) literal).getThisValue().equals(t.getText() ))
					               	return literal;
					              literal = (ThisLiteral) graph.getNextVertexOfClass(literal, thisLiteralVertexClass, true);
					           	}
					literal = graph.createThisLiteral();
					           	((ThisLiteral) literal).setThisValue(t.getText());
				
			}
			break;
		}
		case NUM_INT:
		{
			i = LT(1);
			match(NUM_INT);
			if ( inputState.guessing==0 ) {
				
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
			break;
		}
		case NUM_REAL:
		{
			r = LT(1);
			match(NUM_REAL);
			if ( inputState.guessing==0 ) {
				
					literal = graph.createRealLiteral();
								((RealLiteral) literal).setRealValue(Double.parseDouble(r.getText()));
				
			}
			break;
		}
		case TRUE:
		{
			match(TRUE);
			if ( inputState.guessing==0 ) {
				
					VertexClass boolLiteralVertexClass = (VertexClass) graphClass.getGraphElementClass("BoolLiteral");
					literal = (Literal) graph.getFirstVertexOfClass(boolLiteralVertexClass, true);
					if (literal == null || !( (BoolLiteral) literal).isBoolValue() ) {
					            	literal = graph.createBoolLiteral();
					            	((BoolLiteral) literal).setBoolValue(true);
					}
				
			}
			break;
		}
		case FALSE:
		{
			match(FALSE);
			if ( inputState.guessing==0 ) {
				
					    VertexClass boolLiteralVertexClass = (VertexClass) graphClass.getGraphElementClass("BoolLiteral");
					literal = (Literal) graph.getFirstVertexOfClass(boolLiteralVertexClass, true);
					if (literal == null || ( (BoolLiteral) literal).isBoolValue() ) {
					            	literal = graph.createBoolLiteral();
					   ((BoolLiteral) literal).setBoolValue(false);
					            }
				
			}
			break;
		}
		case NULL_VALUE:
		{
			match(NULL_VALUE);
			if ( inputState.guessing==0 ) {
				
								VertexClass nullLiteralVertexClass = (VertexClass) graphClass.getGraphElementClass("NullLiteral");
					            literal = (Literal) graph.getFirstVertexOfClass(nullLiteralVertexClass, true);
					            if (literal == null)
						            literal = graph.createNullLiteral(); 
				
			}
			break;
		}
		default:
		{
			throw new NoViableAltException(LT(1), getFilename());
		}
		}
		return literal;
	}
	
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
	public final Expression  valueConstruction() throws RecognitionException, TokenStreamException, ParseException,DuplicateVariableException {
		Expression expr = null;
		
		
		switch ( LA(1)) {
		case BAG:
		{
			expr=bagConstruction();
			break;
		}
		case LIST:
		{
			expr=listConstruction();
			break;
		}
		case PATH:
		{
			expr=pathConstruction();
			break;
		}
		case PATHSYSTEM:
		{
			expr=pathsystemConstruction();
			break;
		}
		case REC:
		{
			expr=recordConstruction();
			break;
		}
		case SET:
		{
			expr=setConstruction();
			break;
		}
		case TUP:
		{
			expr=tupleConstruction();
			break;
		}
		default:
		{
			throw new NoViableAltException(LT(1), getFilename());
		}
		}
		return expr;
	}
	
/**	matches a function application
*/
	public final FunctionApplication  functionApplication() throws RecognitionException, TokenStreamException, ParseException,DuplicateVariableException {
		FunctionApplication expr = null;
		
		Token  f = null;
		
			Vector<VertexPosition> typeIds = new Vector<VertexPosition>();
			Vector<VertexPosition> expressions = new Vector<VertexPosition>();
		FunctionId functionId = null;
		
		
		f = LT(1);
		match(FUNCTIONID);
		{
		switch ( LA(1)) {
		case LCURLY:
		{
			match(LCURLY);
			{
			switch ( LA(1)) {
			case CARET:
			case IDENT:
			{
				typeIds=typeExpressionList();
				break;
			}
			case RCURLY:
			{
				break;
			}
			default:
			{
				throw new NoViableAltException(LT(1), getFilename());
			}
			}
			}
			match(RCURLY);
			break;
		}
		case LPAREN:
		{
			break;
		}
		default:
		{
			throw new NoViableAltException(LT(1), getFilename());
		}
		}
		}
		match(LPAREN);
		{
		switch ( LA(1)) {
		case NUM_REAL:
		case FUNCTIONID:
		case THIS:
		case FALSE:
		case NOT:
		case NULL_VALUE:
		case TRUE:
		case BAG:
		case E:
		case ESUBGRAPH:
		case EXISTS:
		case FORALL:
		case FROM:
		case LET:
		case LIST:
		case PATH:
		case PATHSYSTEM:
		case REC:
		case SET:
		case TUP:
		case V:
		case VSUBGRAPH:
		case LPAREN:
		case LBRACK:
		case LCURLY:
		case L_T:
		case MINUS:
		case EDGESTART:
		case EDGE:
		case RARROW:
		case LARROW:
		case ARROW:
		case HASH:
		case STRING_LITERAL:
		case IDENT:
		case NUM_INT:
		{
			expressions=expressionList();
			break;
		}
		case RPAREN:
		{
			break;
		}
		default:
		{
			throw new NoViableAltException(LT(1), getFilename());
		}
		}
		}
		match(RPAREN);
		if ( inputState.guessing==0 ) {
			
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
		return expr;
	}
	
/** matches a subgraph expression
	@return
*/
	public final Expression  graphRangeExpression() throws RecognitionException, TokenStreamException, ParseException,DuplicateVariableException {
		Expression expr = null;
		
		
			Vector<VertexPosition> typeIds = new Vector<VertexPosition>();
		
		
		{
		switch ( LA(1)) {
		case VSUBGRAPH:
		{
			match(VSUBGRAPH);
			if ( inputState.guessing==0 ) {
				
							  expr = graph.createVertexSubgraphExpression(); 
							
			}
			break;
		}
		case ESUBGRAPH:
		{
			match(ESUBGRAPH);
			if ( inputState.guessing==0 ) {
				
								  expr = graph.createEdgeSubgraphExpression();
								
			}
			break;
		}
		default:
		{
			throw new NoViableAltException(LT(1), getFilename());
		}
		}
		}
		match(LCURLY);
		typeIds=typeExpressionList();
		match(RCURLY);
		if ( inputState.guessing==0 ) {
			
				for (int i = 0; i < typeIds.size(); i++) {
							VertexPosition t = typeIds.get(i);
							IsTypeRestrOf typeRestrOf = graph.createIsTypeRestrOf((TypeId)t.node, expr);
							typeRestrOf.setSourcePositions((createSourcePositionList(t.length, t.offset)));
						}
			
		}
		return expr;
	}
	
/** matches a  fwr-expression
*/
	public final Comprehension  simpleQuery() throws RecognitionException, TokenStreamException, ParseException,DuplicateVariableException,ParseException {
		Comprehension comprehension = null;
		
		
			Vector<VertexPosition> declarations = new Vector<VertexPosition>();
		Declaration declaration = null;
		Expression subgraphExpr = null;
		Expression constraintExpr = null;
		int offsetDecl = 0;
		int lengthDecl = 0;
		int offsetSubgraph = 0;
		int lengthSubgraph = 0;
		int offsetConstraint = 0;
		int lengthConstraint = 0;
			// TODO check coordinates (see lengthResult commented out below)
			// int lengthResult = 0;
		// int offsetResult = 0;
		
		
		match(FROM);
		declarations=declarationList();
		if ( inputState.guessing==0 ) {
			
				declaration = graph.createDeclaration();
				for (int i = 0; i < declarations.size(); i++) {
							VertexPosition d = declarations.get(i);
							if (i == 0)
					        	offsetDecl = d.offset;
					        lengthDecl += d.length;
							IsSimpleDeclOf simpleDeclOf = graph.createIsSimpleDeclOf((SimpleDeclaration)d.node ,declaration);
							simpleDeclOf.setSourcePositions((createSourcePositionList(d.length, d.offset)));
						}
			
		}
		{
		switch ( LA(1)) {
		case IN:
		{
			match(IN);
			if ( inputState.guessing==0 ) {
				offsetSubgraph = LT(1).getColumn()-1;
			}
			subgraphExpr=expression();
			if ( inputState.guessing==0 ) {
				
					            lengthSubgraph = -offsetSubgraph + LT(0).getColumn()-1 +LT(0).getText().length();
					           	lengthDecl += lengthSubgraph;
					           	IsSubgraphOf subgraphOf = graph.createIsSubgraphOf(subgraphExpr, declaration);
					           	subgraphOf.setSourcePositions((createSourcePositionList(lengthSubgraph, offsetSubgraph)));
							
			}
			break;
		}
		case REPORT:
		case REPORTSET:
		case REPORTBAG:
		case REPORTTABLE:
		case WITH:
		{
			break;
		}
		default:
		{
			throw new NoViableAltException(LT(1), getFilename());
		}
		}
		}
		{
		switch ( LA(1)) {
		case WITH:
		{
			match(WITH);
			if ( inputState.guessing==0 ) {
				offsetConstraint = LT(1).getColumn()-1;
			}
			constraintExpr=expression();
			if ( inputState.guessing==0 ) {
				
					lengthConstraint = -offsetConstraint + LT(0).getColumn()-1 +LT(0).getText().length();
					           	lengthDecl += lengthConstraint;
					           	IsConstraintOf  constraintOf = graph.createIsConstraintOf(constraintExpr, declaration);
					          	constraintOf.setSourcePositions((createSourcePositionList(lengthConstraint, offsetConstraint)));
							
			}
			break;
		}
		case REPORT:
		case REPORTSET:
		case REPORTBAG:
		case REPORTTABLE:
		{
			break;
		}
		default:
		{
			throw new NoViableAltException(LT(1), getFilename());
		}
		}
		}
		if ( inputState.guessing==0 ) {
			
						// offsetResult = LT(1).getColumn()-1;
					
		}
		comprehension=reportClause();
		if ( inputState.guessing==0 ) {
			
					   // lengthResult = -offsetResult + LT(0).getColumn()-1 +LT(0).getText().length();
				   		IsCompDeclOf comprDeclOf = graph.createIsCompDeclOf(declaration, comprehension);
				   		comprDeclOf.setSourcePositions((createSourcePositionList(lengthDecl, offsetDecl)));
					
		}
		match(END);
		return comprehension;
	}
	
/**	matches list of productions for contextfree grammars
*/
	public final Vector<VertexPosition>  cfProductionList() throws RecognitionException, TokenStreamException, ParseException,DuplicateVariableException {
		Vector<VertexPosition> rules = new Vector<VertexPosition>();;
		
		
			Production production = null;
			Vector<VertexPosition> list = new Vector<VertexPosition>();
			VertexPosition p = new VertexPosition();
			int offset = 0;
			int length = 0;
		
		
		if ( inputState.guessing==0 ) {
			offset = LT(1).getColumn()-1;
		}
		production=cfProduction();
		if ( inputState.guessing==0 ) {
			
						length = -offset + LT(0).getColumn()-1 + LT(0).getText().length();
						p.node = production;
						p.offset = offset;
						p.length = length;
						rules.add(p);
					
		}
		{
		boolean synPredMatched122 = false;
		if (((LA(1)==IDENT))) {
			int _m122 = mark();
			synPredMatched122 = true;
			inputState.guessing++;
			try {
				{
				match(IDENT);
				match(GASSIGN);
				}
			}
			catch (RecognitionException pe) {
				synPredMatched122 = false;
			}
			rewind(_m122);
inputState.guessing--;
		}
		if ( synPredMatched122 ) {
			list=cfProductionList();
			if ( inputState.guessing==0 ) {
				
								rules.addAll(list);
							
			}
		}
		else if ((_tokenSet_8.member(LA(1)))) {
		}
		else {
			throw new NoViableAltException(LT(1), getFilename());
		}
		
		}
		return rules;
	}
	
/** matches a contextfree rule
*/
	public final Production  cfProduction() throws RecognitionException, TokenStreamException, ParseException,DuplicateVariableException {
		Production production = null;
		
		Token  i = null;
		Token  j = null;
		Token  k = null;
		
			RightSide rightSide = null;
			IsRightSideOf rightSideOf = null;
			int offsetRightSide = 0;
			boolean isExcluded = false;
			boolean isType = false;
		
		
		i = LT(1);
		match(IDENT);
		if ( inputState.guessing==0 ) {
			
				try
				{
				        	production = graph.createProduction();
				        	Nonterminal nt = (Nonterminal) nonterminalSymbolTable.lookup(i.getText());
				        	if (nt == null)
				        	{
				        		nt = graph.createNonterminal();
								nt.setName(i.getText());
								nonterminalSymbolTable.insert(i.getText(), nt);
					        }
				            IsLeftSideOf leftSideOf = graph.createIsLeftSideOf(nt, production);
				            leftSideOf.setSourcePositions((createSourcePositionList( i.getText().length(), i.getColumn()-1)));
			}
			catch (Exception ex) { ex.printStackTrace(); }
			
		}
		match(GASSIGN);
		if ( inputState.guessing==0 ) {
			
					  try
					  {
					  	  rightSide = graph.createRightSide();
						  rightSideOf = graph.createIsRightSideOf(rightSide, production);
						  offsetRightSide = LT(1).getColumn()-1;
						  rightSideOf.setSourcePositions((createSourcePositionList(1, offsetRightSide)));
					  }
					  catch (Exception ex) { ex.printStackTrace(); }
					
		}
		{
		_loop127:
		do {
			if ((LA(1)==CARET||LA(1)==IDENT)) {
				{
				switch ( LA(1)) {
				case CARET:
				{
					match(CARET);
					if ( inputState.guessing==0 ) {
						isExcluded = true;
					}
					break;
				}
				case IDENT:
				{
					break;
				}
				default:
				{
					throw new NoViableAltException(LT(1), getFilename());
				}
				}
				}
				j = LT(1);
				match(IDENT);
				{
				switch ( LA(1)) {
				case EXCL:
				{
					match(EXCL);
					if ( inputState.guessing==0 ) {
						isType = true;
					}
					break;
				}
				case SEMI:
				case CARET:
				case BOR:
				case IDENT:
				{
					break;
				}
				default:
				{
					throw new NoViableAltException(LT(1), getFilename());
				}
				}
				}
				if ( inputState.guessing==0 ) {
					
								    try
								    {
									    if (isExcluded || isType)
								   		{   // it's a typeId (terminal)
									   		TypeId t = graph.createTypeId();
									   		t.setName(j.getText());
									   		t.setExcluded(isExcluded);
									   		t.setType(isType);
									   		IsSymbolOf symbolOf = graph.createIsSymbolOf(t, rightSide);
									   		symbolOf.setSourcePositions((createSourcePositionList(j.getText().length(), j.getColumn()-1)));
									    }
								   		else
								   		{   // donno if it's a terminal or nonterminal
									  	 	Symbol s = graph.createSymbol();
										    s.setName(j.getText());
							                IsSymbolOf symbolOf = graph.createIsSymbolOf(s, rightSide);
							                symbolOf.setSourcePositions((createSourcePositionList(j.getText().length(), j.getColumn()-1)));
						                }
						           }
						           catch (Exception ex) { ex.printStackTrace(); }
					
				}
			}
			else {
				break _loop127;
			}
			
		} while (true);
		}
		if ( inputState.guessing==0 ) {
			
						rightSideOf.setSourcePositions((createSourcePositionList(-offsetRightSide + LT(0).getColumn()-1 + LT(0).getText().length(), offsetRightSide )));
					
		}
		{
		_loop133:
		do {
			if ((LA(1)==BOR)) {
				match(BOR);
				if ( inputState.guessing==0 ) {
					
					try
					{
							rightSide = graph.createRightSide();
							rightSideOf = graph.createIsRightSideOf(rightSide, production);
							offsetRightSide = LT(1).getColumn()-1;
							rightSideOf.setSourcePositions((createSourcePositionList(0, offsetRightSide )));
					}
					catch (Exception ex) { ex.printStackTrace(); }
							
				}
				{
				_loop132:
				do {
					if ((LA(1)==CARET||LA(1)==IDENT)) {
						{
						switch ( LA(1)) {
						case CARET:
						{
							match(CARET);
							if ( inputState.guessing==0 ) {
								isExcluded = true;
							}
							break;
						}
						case IDENT:
						{
							break;
						}
						default:
						{
							throw new NoViableAltException(LT(1), getFilename());
						}
						}
						}
						k = LT(1);
						match(IDENT);
						{
						switch ( LA(1)) {
						case EXCL:
						{
							match(EXCL);
							if ( inputState.guessing==0 ) {
								isType = true;
							}
							break;
						}
						case SEMI:
						case CARET:
						case BOR:
						case IDENT:
						{
							break;
						}
						default:
						{
							throw new NoViableAltException(LT(1), getFilename());
						}
						}
						}
						if ( inputState.guessing==0 ) {
							
											    try
								 			    {
												    if (isExcluded || isType)
											   		{// it's a typeId (terminal)
												   		TypeId t = graph.createTypeId();
												   		t.setName(k.getText());
												   		t.setExcluded(isExcluded);
												   		t.setType(isType);
												   		IsSymbolOf symbolOf = graph.createIsSymbolOf(t, rightSide);
												   		symbolOf.setSourcePositions((createSourcePositionList(k.getText().length(), k.getColumn()-1 )));
												    }
											   		else
											   		{// don't know if it's a terminal or nonterminal
												  	 	Symbol s = graph.createSymbol();
													    s.setName(k.getText());
										                IsSymbolOf symbolOf = graph.createIsSymbolOf(s, rightSide);
														symbolOf.setSourcePositions((createSourcePositionList(k.getText().length(), k.getColumn()-1 )));
									                }
									           }
									           catch (Exception ex) { ex.printStackTrace(); }
								
						}
					}
					else {
						break _loop132;
					}
					
				} while (true);
				}
				if ( inputState.guessing==0 ) {
					rightSideOf.setSourcePositions((createSourcePositionList(-offsetRightSide + LT(0).getColumn()-1 + LT(0).getText().length(), offsetRightSide )));
									
				}
			}
			else {
				break _loop133;
			}
			
		} while (true);
		}
		match(SEMI);
		return production;
	}
	
/** matches a pathdescription with intermediate vertex
*/
	public final PathDescription  intermediateVertexPathDescription() throws RecognitionException, TokenStreamException, ParseException,DuplicateVariableException {
		PathDescription pathDescr = null;
		
		
			int offset = 0;
			int length = 0;
		
		
		if ( inputState.guessing==0 ) {
			offset = LT(1).getColumn()-1;
		}
		pathDescr=sequentialPathDescription();
		if ( inputState.guessing==0 ) {
			length = -offset  + LT(0).getColumn()-1 +LT(0).getText().length();
		}
		{
		boolean synPredMatched146 = false;
		if (((_tokenSet_4.member(LA(1))))) {
			int _m146 = mark();
			synPredMatched146 = true;
			inputState.guessing++;
			try {
				{
				restrictedExpression();
				sequentialPathDescription();
				}
			}
			catch (RecognitionException pe) {
				synPredMatched146 = false;
			}
			rewind(_m146);
inputState.guessing--;
		}
		if ( synPredMatched146 ) {
			pathDescr=intermediateVertexPathDescription2(pathDescr, offset, length);
		}
		else if ((_tokenSet_9.member(LA(1)))) {
		}
		else {
			throw new NoViableAltException(LT(1), getFilename());
		}
		
		}
		return pathDescr;
	}
	
/** matches a sequential pathdescription
	@return
*/
	public final PathDescription  sequentialPathDescription() throws RecognitionException, TokenStreamException, ParseException,DuplicateVariableException {
		PathDescription pathDescr = null;
		
		
			int offset = 0;
			int length = 0;
			SequentialPathDescription seqPathDescr = null;
		
		
		if ( inputState.guessing==0 ) {
			offset = LT(1).getColumn()-1;
		}
		pathDescr=startRestrictedPathDescription();
		if ( inputState.guessing==0 ) {
			length = -offset + LT(0).getColumn()-1 + LT(0).getText().length();
		}
		{
		boolean synPredMatched154 = false;
		if (((_tokenSet_3.member(LA(1))))) {
			int _m154 = mark();
			synPredMatched154 = true;
			inputState.guessing++;
			try {
				{
				startRestrictedPathDescription();
				}
			}
			catch (RecognitionException pe) {
				synPredMatched154 = false;
			}
			rewind(_m154);
inputState.guessing--;
		}
		if ( synPredMatched154 ) {
			if ( inputState.guessing==0 ) {
				
								try
								{
									seqPathDescr = graph.createSequentialPathDescription();
								    IsSequenceElementOf sequenceElementOf = graph.createIsSequenceElementOf(pathDescr, seqPathDescr);
								    sequenceElementOf.setSourcePositions((createSourcePositionList(length, offset )));
								}
								catch (Exception ex) { ex.printStackTrace(); }
							
			}
			pathDescr=sequentialPathDescription2(seqPathDescr, offset, length);
		}
		else if ((_tokenSet_9.member(LA(1)))) {
		}
		else {
			throw new NoViableAltException(LT(1), getFilename());
		}
		
		}
		return pathDescr;
	}
	
/** matches 2nd part of an alternative pathdescription
	@param alt first alternative
	@param offsetAlt offset of the first alternative
	@param lenghtAlt lenght of the first alternative
	@return
*/
	public final AlternativePathDescription  alternativePathDescription2(
		AlternativePathDescription alt, int offsetAlt1, int lengthAlt1
	) throws RecognitionException, TokenStreamException, ParseException,DuplicateVariableException {
		AlternativePathDescription pathDescr = null;
		
		
			PathDescription alt2 = null;
			int offsetAlt2 = 0;
			int lengthAlt2 = 0;
		
		
		match(BOR);
		if ( inputState.guessing==0 ) {
			offsetAlt2 = LT(1).getColumn()-1;
		}
		alt2=intermediateVertexPathDescription();
		if ( inputState.guessing==0 ) {
			
						lengthAlt2 = -offsetAlt2 + LT(0).getColumn()-1 + LT(0).getText().length();
						try
						{
				            IsAlternativePathOf alt2Of = graph.createIsAlternativePathOf(alt2, alt);
				            alt2Of.setSourcePositions((createSourcePositionList(lengthAlt2, offsetAlt2 )));
							pathDescr = alt;
						}
						catch (Exception ex) { ex.printStackTrace(); }
			
		}
		{
		switch ( LA(1)) {
		case BOR:
		{
			pathDescr=alternativePathDescription2((AlternativePathDescription)pathDescr,
				offsetAlt1, -offsetAlt1 + LT(0).getColumn()-1 + LT(0).getText().length());
			break;
		}
		case EOF:
		case NUM_REAL:
		case DOTDOT:
		case FUNCTIONID:
		case THIS:
		case AND:
		case ANDTHEN:
		case FALSE:
		case NULL_VALUE:
		case OR:
		case ORELSE:
		case TRUE:
		case XOR:
		case AS:
		case BAG:
		case E:
		case ESUBGRAPH:
		case END:
		case FROM:
		case IN:
		case LIST:
		case PATH:
		case PATHSYSTEM:
		case REC:
		case REPORT:
		case REPORTSET:
		case REPORTBAG:
		case REPORTTABLE:
		case STORE:
		case SET:
		case TUP:
		case V:
		case VSUBGRAPH:
		case WHERE:
		case WITH:
		case QUESTION:
		case COLON:
		case COMMA:
		case AT:
		case LPAREN:
		case RPAREN:
		case RBRACK:
		case RCURLY:
		case EQUAL:
		case MATCH:
		case NOT_EQUAL:
		case LE:
		case GE:
		case L_T:
		case G_T:
		case DIV:
		case PLUS:
		case MINUS:
		case STAR:
		case MOD:
		case SMILEY:
		case EDGEEND:
		case EDGE:
		case HASH:
		case STRING_LITERAL:
		case IDENT:
		case NUM_INT:
		{
			break;
		}
		default:
		{
			throw new NoViableAltException(LT(1), getFilename());
		}
		}
		}
		return pathDescr;
	}
	
/** matches 2nd part of a  pathdescription with an intermediate vertex
	@param subPath1 pathdescription preceding the intermediate vertex
	@param offsetSub1 offset of subPath1
	@param lengthSub1 length of subPath1
	@return
*/
	public final PathDescription  intermediateVertexPathDescription2(
		PathDescription subPath1, int offsetSub1, int lengthSub1
	) throws RecognitionException, TokenStreamException, ParseException,DuplicateVariableException {
		PathDescription pathDescr = null;
		
		
			Expression restrExpr = null;
			PathDescription subPath2 = null;
			int offsetExpr = 0;
			int offsetSub2 = 0;
			int lengthExpr = 0;
			int lengthSub2 = 0;
		
		
		if ( inputState.guessing==0 ) {
			offsetExpr = LT(1).getColumn()-1;
		}
		restrExpr=restrictedExpression();
		if ( inputState.guessing==0 ) {
			
				lengthExpr = -offsetExpr + LT(0).getColumn()-1 + LT(0).getText().length();
				offsetSub2 = LT(1).getColumn()-1;
			
		}
		subPath2=sequentialPathDescription();
		if ( inputState.guessing==0 ) {
			
			lengthSub2 = -offsetSub2 + LT(0).getColumn()-1 + LT(0).getText().length();
			try
			{
							IntermediateVertexPathDescription vpd = graph.createIntermediateVertexPathDescription();
							pathDescr = vpd;
				            IsSubPathOf subpath1Of = graph.createIsSubPathOf(subPath1, vpd);
				            subpath1Of.setSourcePositions((createSourcePositionList(lengthSub1, offsetSub1 )));
				            IsSubPathOf subpath2Of = graph.createIsSubPathOf(subPath2, vpd);
				            subpath2Of.setSourcePositions((createSourcePositionList(lengthSub2, offsetSub2 )));
				            IsIntermediateVertexOf intermediateVertexOf = graph.createIsIntermediateVertexOf(restrExpr, vpd);
				            intermediateVertexOf.setSourcePositions((createSourcePositionList(lengthExpr, offsetExpr )));
						}
						catch (Exception ex) { ex.printStackTrace(); }
			
		}
		{
		boolean synPredMatched150 = false;
		if (((_tokenSet_4.member(LA(1))))) {
			int _m150 = mark();
			synPredMatched150 = true;
			inputState.guessing++;
			try {
				{
				restrictedExpression();
				sequentialPathDescription();
				}
			}
			catch (RecognitionException pe) {
				synPredMatched150 = false;
			}
			rewind(_m150);
inputState.guessing--;
		}
		if ( synPredMatched150 ) {
			pathDescr=intermediateVertexPathDescription2(pathDescr, offsetSub1,
            					-offsetSub1 + LT(0).getColumn()-1 +LT(0).getText().length());
		}
		else if ((_tokenSet_9.member(LA(1)))) {
		}
		else {
			throw new NoViableAltException(LT(1), getFilename());
		}
		
		}
		return pathDescr;
	}
	
/** matches a pathdescription with startrestriction
	@return
*/
	public final PathDescription  startRestrictedPathDescription() throws RecognitionException, TokenStreamException, ParseException,DuplicateVariableException {
		PathDescription pathDescr = null;
		
		
			Vector<VertexPosition> typeIds = new Vector<VertexPosition>();
			Expression expr = null;
			int offset = 0;
			int length = 0;
		
		
		{
		switch ( LA(1)) {
		case LCURLY:
		{
			match(LCURLY);
			{
			boolean synPredMatched163 = false;
			if (((LA(1)==CARET||LA(1)==IDENT))) {
				int _m163 = mark();
				synPredMatched163 = true;
				inputState.guessing++;
				try {
					{
					typeId();
					}
				}
				catch (RecognitionException pe) {
					synPredMatched163 = false;
				}
				rewind(_m163);
inputState.guessing--;
			}
			if ( synPredMatched163 ) {
				typeIds=typeExpressionList();
			}
			else if ((_tokenSet_10.member(LA(1)))) {
				if ( inputState.guessing==0 ) {
					offset = LT(1).getColumn()-1;
				}
				expr=expression();
				if ( inputState.guessing==0 ) {
					length = -offset + LT(0).getColumn()-1 +LT(0).getText().length();
				}
			}
			else {
				throw new NoViableAltException(LT(1), getFilename());
			}
			
			}
			match(RCURLY);
			match(AMP);
			break;
		}
		case LPAREN:
		case LBRACK:
		case EDGESTART:
		case EDGE:
		case RARROW:
		case LARROW:
		case ARROW:
		{
			break;
		}
		default:
		{
			throw new NoViableAltException(LT(1), getFilename());
		}
		}
		}
		pathDescr=goalRestrictedPathDescription();
		if ( inputState.guessing==0 ) {
			
			try
			{
				            if (expr != null)
							{
							  		IsStartRestrOf startRestrOf = graph.createIsStartRestrOf(expr, pathDescr);
									startRestrOf.setSourcePositions((createSourcePositionList(length, offset)));
									mergeRestrictedExpr(expr, pathDescr);
							}
							else
							{
								for (int i = 0; i < typeIds.size(); i++)
								{
									VertexPosition t = typeIds.get(i);
									IsStartRestrOf startRestrOf = graph.createIsStartRestrOf((Expression)t.node, pathDescr);
									startRestrOf.setSourcePositions((createSourcePositionList(t.length, t.offset)));
								}
							}
						}
						catch (Exception ex) { ex.printStackTrace(); }
			
		}
		return pathDescr;
	}
	
/** matches 2nd part of a sequential pathdescription
	@return
*/
	public final SequentialPathDescription  sequentialPathDescription2(
		SequentialPathDescription seqPathDescr, int offsetSeq1, int lengthSeq1
	) throws RecognitionException, TokenStreamException, ParseException,DuplicateVariableException {
		SequentialPathDescription pathDescr = null;
		
		
			PathDescription seq2;
			int offsetSeq2 = 0;
			int lengthSeq2 = 0;
		
		
		if ( inputState.guessing==0 ) {
			offsetSeq2 = LT(1).getColumn()-1;
		}
		seq2=startRestrictedPathDescription();
		if ( inputState.guessing==0 ) {
			
				lengthSeq2 = -offsetSeq2 + LT(0).getColumn()-1 + LT(0).getText().length();
				try
				{
				        	IsSequenceElementOf sequenceElementOf = graph.createIsSequenceElementOf(seq2, seqPathDescr);
				        	sequenceElementOf.setSourcePositions((createSourcePositionList(lengthSeq2, offsetSeq2 )));
							pathDescr = seqPathDescr;
						}
						catch (Exception ex) { ex.printStackTrace(); }
			
		}
		{
		boolean synPredMatched158 = false;
		if (((_tokenSet_3.member(LA(1))))) {
			int _m158 = mark();
			synPredMatched158 = true;
			inputState.guessing++;
			try {
				{
				iteratedOrTransposedPathDescription();
				}
			}
			catch (RecognitionException pe) {
				synPredMatched158 = false;
			}
			rewind(_m158);
inputState.guessing--;
		}
		if ( synPredMatched158 ) {
			pathDescr=sequentialPathDescription2(seqPathDescr, offsetSeq1,
					-offsetSeq1 +LT(0).getColumn()-1 + LT(0).getText().length());
		}
		else if ((_tokenSet_9.member(LA(1)))) {
		}
		else {
			throw new NoViableAltException(LT(1), getFilename());
		}
		
		}
		return pathDescr;
	}
	
/** matches an iterated (+/*), exponentiated (^Integer)
	or transposed (^T) pathdescription
	@return
*/
	public final PathDescription  iteratedOrTransposedPathDescription() throws RecognitionException, TokenStreamException, ParseException,DuplicateVariableException {
		PathDescription pathDescr = null;
		
		
			int offset = 0;
			int length = 0;
		
		
		if ( inputState.guessing==0 ) {
			offset = LT(1).getColumn()-1;
		}
		pathDescr=primaryPathDescription();
		if ( inputState.guessing==0 ) {
			length = -offset + LT(0).getColumn()-1 +LT(0).getText().length();
		}
		{
		boolean synPredMatched176 = false;
		if (((LA(1)==PLUS||LA(1)==STAR||LA(1)==CARET))) {
			int _m176 = mark();
			synPredMatched176 = true;
			inputState.guessing++;
			try {
				{
				switch ( LA(1)) {
				case STAR:
				{
					match(STAR);
					break;
				}
				case PLUS:
				{
					match(PLUS);
					break;
				}
				case CARET:
				{
					match(CARET);
					break;
				}
				default:
				{
					throw new NoViableAltException(LT(1), getFilename());
				}
				}
				}
			}
			catch (RecognitionException pe) {
				synPredMatched176 = false;
			}
			rewind(_m176);
inputState.guessing--;
		}
		if ( synPredMatched176 ) {
			pathDescr=iteratedOrTransposedPathDescription2(pathDescr, offset, length);
		}
		else if ((_tokenSet_11.member(LA(1)))) {
		}
		else {
			throw new NoViableAltException(LT(1), getFilename());
		}
		
		}
		return pathDescr;
	}
	
/** matches a typeId
	@return
*/
	public final TypeId  typeId() throws RecognitionException, TokenStreamException, ParseException,DuplicateVariableException {
		TypeId type = null;
		
		Token  i = null;
		
		if ( inputState.guessing==0 ) {
			
						type = graph.createTypeId();
					
		}
		{
		switch ( LA(1)) {
		case CARET:
		{
			match(CARET);
			if ( inputState.guessing==0 ) {
				type.setExcluded(true);
			}
			break;
		}
		case IDENT:
		{
			break;
		}
		default:
		{
			throw new NoViableAltException(LT(1), getFilename());
		}
		}
		}
		i = LT(1);
		match(IDENT);
		if ( inputState.guessing==0 ) {
			type.setName(i.getText());
		}
		{
		switch ( LA(1)) {
		case EXCL:
		{
			match(EXCL);
			if ( inputState.guessing==0 ) {
				type.setType(true);
			}
			break;
		}
		case COMMA:
		case AT:
		case RCURLY:
		{
			break;
		}
		default:
		{
			throw new NoViableAltException(LT(1), getFilename());
		}
		}
		}
		return type;
	}
	
/** matches a list of type-descriptions: [^] typeId [!]
	@return
*/
	public final Vector<VertexPosition>  typeExpressionList() throws RecognitionException, TokenStreamException, ParseException,DuplicateVariableException {
		Vector<VertexPosition> typeIdList = new Vector<VertexPosition>();;
		
		
			Expression v = null;
			Vector<VertexPosition> list = null;
		VertexPosition type = new VertexPosition();
		int offset = 0;
		int length = 0;
		
		
		if ( inputState.guessing==0 ) {
			offset = LT(1).getColumn()-1;
		}
		v=typeId();
		if ( inputState.guessing==0 ) {
			
				length = -offset + LT(0).getColumn()-1 + LT(0).getText().length();
				type.node = v;
				type.offset = offset;
				type.length = length;
				typeIdList.add(type);
			
		}
		{
		switch ( LA(1)) {
		case COMMA:
		{
			match(COMMA);
			list=typeExpressionList();
			if ( inputState.guessing==0 ) {
				typeIdList.addAll(list);
			}
			break;
		}
		case RCURLY:
		{
			break;
		}
		default:
		{
			throw new NoViableAltException(LT(1), getFilename());
		}
		}
		}
		return typeIdList;
	}
	
/** matches a pathdescription with goalrestriction
	@return
*/
	public final PathDescription  goalRestrictedPathDescription() throws RecognitionException, TokenStreamException, ParseException,DuplicateVariableException {
		PathDescription pathDescr = null;
		
		
			Vector<VertexPosition> typeIds = new Vector<VertexPosition>();
			Expression expr = null;
			int offset = 0;
			int length = 0;
		
		
		pathDescr=iteratedOrTransposedPathDescription();
		{
		switch ( LA(1)) {
		case AMP:
		{
			{
			match(AMP);
			match(LCURLY);
			{
			boolean synPredMatched171 = false;
			if (((LA(1)==CARET||LA(1)==IDENT))) {
				int _m171 = mark();
				synPredMatched171 = true;
				inputState.guessing++;
				try {
					{
					typeId();
					}
				}
				catch (RecognitionException pe) {
					synPredMatched171 = false;
				}
				rewind(_m171);
inputState.guessing--;
			}
			if ( synPredMatched171 ) {
				typeIds=typeExpressionList();
				if ( inputState.guessing==0 ) {
					
							try
						{
						            	for (int i = 0; i < typeIds.size(); i++)
										{
											VertexPosition t = typeIds.get(i);
											IsGoalRestrOf goalRestrOf = graph.createIsGoalRestrOf((Expression)t.node, pathDescr);
											goalRestrOf.setSourcePositions((createSourcePositionList(t.length, t.offset)));
										}
									}
									catch (Exception ex) { ex.printStackTrace(); }
						 	
				}
			}
			else if ((_tokenSet_10.member(LA(1)))) {
				{
				if ( inputState.guessing==0 ) {
					offset = LT(1).getColumn()-1;
				}
				expr=expression();
				if ( inputState.guessing==0 ) {
					
								  	 		length = -offset + LT(0).getColumn()-1 +LT(0).getText().length();
								  		try
								  		{
								  			IsGoalRestrOf goalRestrOf = graph.createIsGoalRestrOf(expr, pathDescr);
								  			goalRestrOf.setSourcePositions((createSourcePositionList(length, offset)));
								  			mergeRestrictedExpr(expr, pathDescr);
								  		}
								  		catch (Exception ex) { ex.printStackTrace(); }
								  		
				}
				}
			}
			else {
				throw new NoViableAltException(LT(1), getFilename());
			}
			
			}
			match(RCURLY);
			}
			break;
		}
		case EOF:
		case NUM_REAL:
		case DOTDOT:
		case FUNCTIONID:
		case THIS:
		case AND:
		case ANDTHEN:
		case FALSE:
		case NULL_VALUE:
		case OR:
		case ORELSE:
		case TRUE:
		case XOR:
		case AS:
		case BAG:
		case E:
		case ESUBGRAPH:
		case END:
		case FROM:
		case IN:
		case LIST:
		case PATH:
		case PATHSYSTEM:
		case REC:
		case REPORT:
		case REPORTSET:
		case REPORTBAG:
		case REPORTTABLE:
		case STORE:
		case SET:
		case TUP:
		case V:
		case VSUBGRAPH:
		case WHERE:
		case WITH:
		case QUESTION:
		case COLON:
		case COMMA:
		case AT:
		case LPAREN:
		case RPAREN:
		case LBRACK:
		case RBRACK:
		case LCURLY:
		case RCURLY:
		case EQUAL:
		case MATCH:
		case NOT_EQUAL:
		case LE:
		case GE:
		case L_T:
		case G_T:
		case DIV:
		case PLUS:
		case MINUS:
		case STAR:
		case MOD:
		case BOR:
		case SMILEY:
		case EDGESTART:
		case EDGEEND:
		case EDGE:
		case RARROW:
		case LARROW:
		case ARROW:
		case HASH:
		case STRING_LITERAL:
		case IDENT:
		case NUM_INT:
		{
			break;
		}
		default:
		{
			throw new NoViableAltException(LT(1), getFilename());
		}
		}
		}
		return pathDescr;
	}
	
/** matches 2nd part of an iterated (+/*), exponentiated (^Integer)
	or transposed (^T) pathdescription
	@return
*/
	public final PathDescription  iteratedOrTransposedPathDescription2(
		PathDescription path, int offsetPath, int lengthPath
	) throws RecognitionException, TokenStreamException, ParseException,DuplicateVariableException {
		PathDescription pathDescr = null;
		
		Token  i = null;
		
			String times = "plus";
			int offsetExpr = 0;
			int lengthExpr = 0;
		
		
		{
		switch ( LA(1)) {
		case PLUS:
		case STAR:
		{
			{
			switch ( LA(1)) {
			case STAR:
			{
				match(STAR);
				if ( inputState.guessing==0 ) {
					times = "star";
				}
				break;
			}
			case PLUS:
			{
				match(PLUS);
				break;
			}
			default:
			{
				throw new NoViableAltException(LT(1), getFilename());
			}
			}
			}
			if ( inputState.guessing==0 ) {
				
					try
					{
									IteratedPathDescription ipd = graph.createIteratedPathDescription();
									pathDescr = ipd;
					            	((IteratedPathDescription)pathDescr).setTimes(times);
					                IsIteratedPathOf iteratedPathOf = graph.createIsIteratedPathOf(path, ipd);
					                iteratedPathOf.setSourcePositions((createSourcePositionList(lengthPath, offsetPath)));
								}
								catch (Exception ex) { ex.printStackTrace(); }
				
			}
			break;
		}
		case CARET:
		{
			{
			match(CARET);
			{
			switch ( LA(1)) {
			case T:
			{
				match(T);
				if ( inputState.guessing==0 ) {
					
							try
							{
												TransposedPathDescription tpd = graph.createTransposedPathDescription();
												pathDescr = tpd;
						                    	IsTransposedPathOf transposedPathOf = graph.createIsTransposedPathOf(path, tpd);
						                    	transposedPathOf.setSourcePositions((createSourcePositionList(lengthPath, offsetPath)));
											}
											catch (Exception ex) { ex.printStackTrace(); }
					
				}
				break;
			}
			case NUM_INT:
			{
				{
				if ( inputState.guessing==0 ) {
					offsetExpr = LT(1).getColumn()-1;
				}
				i = LT(1);
				match(NUM_INT);
				if ( inputState.guessing==0 ) {
					lengthExpr = -offsetExpr + LT(0).getColumn()-1 +LT(0).getText().length();
				}
				if ( inputState.guessing==0 ) {
					
						try
						{
												ExponentiatedPathDescription epd = graph.createExponentiatedPathDescription();
												pathDescr = epd;
						                        IsExponentiatedPathOf exponentiatedPathOf = graph.createIsExponentiatedPathOf(path, epd);
						                        exponentiatedPathOf.setSourcePositions((createSourcePositionList(lengthPath, offsetPath)));
						            			IntLiteral exponent = graph.createIntLiteral();
												exponent.setIntValue(Integer.parseInt(i.getText()));
						                      	IsExponentOf exponentOf = graph.createIsExponentOf(exponent, epd);
						                      	exponentOf.setSourcePositions((createSourcePositionList(lengthExpr, offsetExpr)));
											} catch (Exception ex) { ex.printStackTrace(); }
					
				}
				}
				break;
			}
			default:
			{
				throw new NoViableAltException(LT(1), getFilename());
			}
			}
			}
			}
			break;
		}
		default:
		{
			throw new NoViableAltException(LT(1), getFilename());
		}
		}
		}
		{
		boolean synPredMatched185 = false;
		if (((LA(1)==PLUS||LA(1)==STAR||LA(1)==CARET))) {
			int _m185 = mark();
			synPredMatched185 = true;
			inputState.guessing++;
			try {
				{
				switch ( LA(1)) {
				case STAR:
				{
					match(STAR);
					break;
				}
				case PLUS:
				{
					match(PLUS);
					break;
				}
				case CARET:
				{
					match(CARET);
					break;
				}
				default:
				{
					throw new NoViableAltException(LT(1), getFilename());
				}
				}
				}
			}
			catch (RecognitionException pe) {
				synPredMatched185 = false;
			}
			rewind(_m185);
inputState.guessing--;
		}
		if ( synPredMatched185 ) {
			pathDescr=iteratedOrTransposedPathDescription2(pathDescr,
        			offsetPath, -offsetPath + LT(0).getColumn()-1 + LT(0).getText().length());
		}
		else if ((_tokenSet_11.member(LA(1)))) {
		}
		else {
			throw new NoViableAltException(LT(1), getFilename());
		}
		
		}
		return pathDescr;
	}
	
/** matches a simle pathdescription consisting of an arrow simple
	and eventually a restriction. "thisEdge"s are replaced by
	the corresponding simple pathdescription
	@return
*/
	public final PrimaryPathDescription  simplePathDescription() throws RecognitionException, TokenStreamException, ParseException,DuplicateVariableException {
		PrimaryPathDescription pathDescr = null;
		
		
			Vector<VertexPosition> typeIds = new Vector<VertexPosition>();
		Direction dir;
		String direction = "any";
		int offsetDir = 0;
		
		
		if ( inputState.guessing==0 ) {
			
			offsetDir = LT(1).getColumn()-1;
			
		}
		{
		switch ( LA(1)) {
		case RARROW:
		{
			match(RARROW);
			if ( inputState.guessing==0 ) {
				direction = "out";
			}
			break;
		}
		case LARROW:
		{
			match(LARROW);
			if ( inputState.guessing==0 ) {
				direction = "in";
			}
			break;
		}
		case ARROW:
		{
			match(ARROW);
			break;
		}
		default:
		{
			throw new NoViableAltException(LT(1), getFilename());
		}
		}
		}
		{
		boolean synPredMatched196 = false;
		if (((LA(1)==LCURLY))) {
			int _m196 = mark();
			synPredMatched196 = true;
			inputState.guessing++;
			try {
				{
				match(LCURLY);
				{
				switch ( LA(1)) {
				case AT:
				case CARET:
				case IDENT:
				{
					edgeRestrictionList();
					break;
				}
				case RCURLY:
				{
					break;
				}
				default:
				{
					throw new NoViableAltException(LT(1), getFilename());
				}
				}
				}
				match(RCURLY);
				}
			}
			catch (RecognitionException pe) {
				synPredMatched196 = false;
			}
			rewind(_m196);
inputState.guessing--;
		}
		if ( synPredMatched196 ) {
			{
			match(LCURLY);
			{
			switch ( LA(1)) {
			case AT:
			case CARET:
			case IDENT:
			{
				typeIds=edgeRestrictionList();
				break;
			}
			case RCURLY:
			{
				break;
			}
			default:
			{
				throw new NoViableAltException(LT(1), getFilename());
			}
			}
			}
			match(RCURLY);
			}
		}
		else if ((_tokenSet_12.member(LA(1)))) {
		}
		else {
			throw new NoViableAltException(LT(1), getFilename());
		}
		
		}
		if ( inputState.guessing==0 ) {
			
				try
				{
				        	pathDescr = graph.createSimplePathDescription();
				        	VertexClass directionVertexClass = (VertexClass) graphClass.getGraphElementClass("Direction");
				        	dir = (Direction)graph.getFirstVertexOfClass(directionVertexClass, true);
				        	while (dir != null ) //
				        	{
				        		if (! dir.getDirValue().equals(direction))
				        		{
				        			dir = (Direction)graph.getNextVertexOfClass(dir, directionVertexClass, true);
				        		}
				        		else
				        		{
				        			break;
				        		}
							}
							if (dir == null)
							{
								dir = graph.createDirection();
				        		dir.setDirValue(direction);
				        	}
				            IsDirectionOf directionOf = graph.createIsDirectionOf(dir, pathDescr);
				            directionOf.setSourcePositions((createSourcePositionList(0, offsetDir)));
							for (int i = 0; i < typeIds.size(); i++)
							{
								VertexPosition t = typeIds.get(i);
								IsEdgeRestrOf edgeRestrOf = graph.createIsEdgeRestrOf((EdgeRestriction)t.node, pathDescr);
								edgeRestrOf.setSourcePositions((createSourcePositionList(t.length, t.offset)));
							}
						}
						catch (Exception ex) { ex.printStackTrace(); }
			
		}
		return pathDescr;
	}
	
/** matches a edgePathDescription, i.e. am edge as part of a pathdescription
	@return
*/
	public final EdgePathDescription  edgePathDescription() throws RecognitionException, TokenStreamException, ParseException,DuplicateVariableException {
		EdgePathDescription pathDescr = null;
		
		
			Expression expr = null;
			Direction dir = null;
		boolean edgeStart = false;
		boolean edgeEnd = false;
		String direction = "any";
		int offsetDir = 0;
		int offsetExpr = 0;
		int lengthDir = 0;
		int lengthExpr = 0;
		
		
		if ( inputState.guessing==0 ) {
			
					offsetDir = LT(1).getColumn()-1;
				
		}
		{
		switch ( LA(1)) {
		case EDGESTART:
		{
			match(EDGESTART);
			if ( inputState.guessing==0 ) {
				edgeStart = true;
			}
			break;
		}
		case EDGE:
		{
			match(EDGE);
			break;
		}
		default:
		{
			throw new NoViableAltException(LT(1), getFilename());
		}
		}
		}
		if ( inputState.guessing==0 ) {
			
					offsetExpr = LT(1).getColumn()-1;
				
		}
		expr=expression();
		if ( inputState.guessing==0 ) {
			
					lengthExpr = -offsetExpr + LT(0).getColumn()-1 + LT(0).getText().length();
				
		}
		{
		switch ( LA(1)) {
		case EDGEEND:
		{
			match(EDGEEND);
			if ( inputState.guessing==0 ) {
				edgeEnd = true;
			}
			break;
		}
		case EDGE:
		{
			match(EDGE);
			break;
		}
		default:
		{
			throw new NoViableAltException(LT(1), getFilename());
		}
		}
		}
		if ( inputState.guessing==0 ) {
			
				lengthExpr = -offsetDir + LT(0).getColumn()-1 + LT(0).getText().length();
				try
				{
				        	pathDescr = graph.createEdgePathDescription();
				            if ((edgeStart && !edgeEnd) || (!edgeStart  && edgeEnd))
				            	if (edgeStart) direction = "in";
				            	else direction = "out";
				            VertexClass directionVertexClass = (VertexClass) graphClass.getGraphElementClass("Direction");
				        	dir = (Direction)graph.getFirstVertexOfClass(directionVertexClass, true);
				        	while (dir != null )
				        	{
				        		if (! dir.getDirValue().equals(direction))
				        		{
				        			dir = (Direction)graph.getNextVertexOfClass(dir, directionVertexClass, true);
				        		}
				        		else
				        		{
				        			break;
				        		}
							}
							if (dir == null)
							{
								dir = graph.createDirection();
				        		dir.setDirValue(direction);
				        	}
				    		IsDirectionOf directionOf = graph.createIsDirectionOf(dir, pathDescr);
				    		directionOf.setSourcePositions((createSourcePositionList(lengthDir, offsetDir)));
				            IsEdgeExprOf edgeExprOf = graph.createIsEdgeExprOf(expr, pathDescr);
				            edgeExprOf.setSourcePositions((createSourcePositionList(lengthExpr, offsetExpr)));
			}
			catch (Exception ex) { ex.printStackTrace();}
					
		}
		return pathDescr;
	}
	
/** matches a list of edge restrictions: each of them containing a
    typeId and/or a roleId
    @return vector containing the elements  of the list
*/
	public final Vector<VertexPosition>  edgeRestrictionList() throws RecognitionException, TokenStreamException, ParseException,DuplicateVariableException {
		Vector<VertexPosition> list = new Vector<VertexPosition>();;
		
		
			TypeId type = null;
			RoleId role = null;
			Vector<VertexPosition> eRList = null;
			VertexPosition v = new VertexPosition();
			EdgeRestriction er = null;
			int offsetType = 0;
			int offsetRole = 0;
			int lengthType = 0;
			int lengthRole = 0;
		
		
		{
		switch ( LA(1)) {
		case AT:
		{
			{
			if ( inputState.guessing==0 ) {
					offsetRole = LT(1).getColumn()-1;
			}
			match(AT);
			role=roleId();
			if ( inputState.guessing==0 ) {
				
								lengthRole = -offsetRole + LT(0).getColumn()-1 + LT(0).getText().length();
							
			}
			}
			break;
		}
		case CARET:
		case IDENT:
		{
			{
			if ( inputState.guessing==0 ) {
					offsetType = LT(1).getColumn()-1;
			}
			type=typeId();
			if ( inputState.guessing==0 ) {
				
								lengthType = -offsetType + LT(0).getColumn()-1 + LT(0).getText().length();
							
			}
			{
			switch ( LA(1)) {
			case AT:
			{
				match(AT);
				if ( inputState.guessing==0 ) {
					offsetRole = LT(1).getColumn()-1;
				}
				role=roleId();
				break;
			}
			case COMMA:
			case RCURLY:
			{
				break;
			}
			default:
			{
				throw new NoViableAltException(LT(1), getFilename());
			}
			}
			}
			}
			break;
		}
		default:
		{
			throw new NoViableAltException(LT(1), getFilename());
		}
		}
		}
		if ( inputState.guessing==0 ) {
			
				lengthRole = -offsetRole + LT(0).getColumn()-1 + LT(0).getText().length();
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
				offsetRole = 0;
				offsetType = 0;
				lengthType = 0;
				lengthRole = 0;
			
		}
		{
		switch ( LA(1)) {
		case COMMA:
		{
			match(COMMA);
			eRList=edgeRestrictionList();
			if ( inputState.guessing==0 ) {
				list.addAll(eRList);
			}
			break;
		}
		case RCURLY:
		{
			break;
		}
		default:
		{
			throw new NoViableAltException(LT(1), getFilename());
		}
		}
		}
		return list;
	}
	
/** matches a attributeId
	@return
*/
	public final AttributeId  attributeId() throws RecognitionException, TokenStreamException, ParseException,DuplicateVariableException {
		AttributeId expr = null;
		
		Token  i = null;
		
		i = LT(1);
		match(IDENT);
		if ( inputState.guessing==0 ) {
			
				       	expr = graph.createAttributeId();
				       	expr.setName(i.getText());
			
		}
		return expr;
	}
	
/** matches a definition for let- or where expressions
	@return
*/
	public final Definition  definition() throws RecognitionException, TokenStreamException, ParseException,DuplicateVariableException {
		Definition definition = null;
		
		
			Variable var = null;
			Expression expr = null;
			int offsetVar = 0;
			int offsetExpr = 0;
		int lengthVar = 0;
		int lengthExpr = 0;
		
		
		if ( inputState.guessing==0 ) {
			offsetVar = LT(1).getColumn()-1;
		}
		var=variable();
		if ( inputState.guessing==0 ) {
			lengthVar = -offsetVar + LT(0).getColumn()-1 +LT(0).getText().length();
		}
		match(ASSIGN);
		if ( inputState.guessing==0 ) {
			offsetExpr = LT(1).getColumn()-1;
		}
		{
		expr=expressionOrPathDescription();
		}
		if ( inputState.guessing==0 ) {
			
				lengthExpr = -offsetExpr + LT(0).getColumn()-1 +LT(0).getText().length();
			definition = graph.createDefinition();
			IsVarOf varOf = graph.createIsVarOf(var, definition);
			varOf.setSourcePositions((createSourcePositionList(lengthVar, offsetVar)));
			IsExprOf exprOf = graph.createIsExprOf(expr, definition);
			exprOf.setSourcePositions((createSourcePositionList(lengthExpr, offsetExpr)));
			
		}
		return definition;
	}
	
/** matches an expression or a pathDescription
	@return
*/
	public final Expression  expressionOrPathDescription() throws RecognitionException, TokenStreamException, ParseException,DuplicateVariableException {
		Expression expr = null;
		
		
		{
		boolean synPredMatched307 = false;
		if (((_tokenSet_10.member(LA(1))))) {
			int _m307 = mark();
			synPredMatched307 = true;
			inputState.guessing++;
			try {
				{
				pathDescription();
				expression();
				}
			}
			catch (RecognitionException pe) {
				synPredMatched307 = false;
			}
			rewind(_m307);
inputState.guessing--;
		}
		if ( synPredMatched307 ) {
			expr=expression();
		}
		else {
			boolean synPredMatched309 = false;
			if (((_tokenSet_10.member(LA(1))))) {
				int _m309 = mark();
				synPredMatched309 = true;
				inputState.guessing++;
				try {
					{
					expression();
					}
				}
				catch (RecognitionException pe) {
					synPredMatched309 = false;
				}
				rewind(_m309);
inputState.guessing--;
			}
			if ( synPredMatched309 ) {
				expr=expression();
			}
			else if ((_tokenSet_3.member(LA(1)))) {
				expr=pathDescription();
			}
			else {
				throw new NoViableAltException(LT(1), getFilename());
			}
			}
			}
			return expr;
		}
		
/** matches a list of expressions: expression {, expression}
	@return contains the expression-vertices
*/
	public final Vector<VertexPosition>  expressionList() throws RecognitionException, TokenStreamException, ParseException,DuplicateVariableException {
		Vector<VertexPosition> expressions;
		
		
			expressions = new Vector<VertexPosition>();
			Expression expr = null;
		VertexPosition v = new VertexPosition();
		Vector<VertexPosition> exprList = new Vector<VertexPosition>();
		int offset = 0;
		int length = 0;
		
		
		if ( inputState.guessing==0 ) {
			offset = LT(1).getColumn()-1;
		}
		expr=expression();
		if ( inputState.guessing==0 ) {
			
				length = -offset + LT(0).getColumn()-1 + LT(0).getText().length();
				v.node = expr;
				v.offset = offset;
				v.length = length;
				expressions.add(v);
			
		}
		{
		switch ( LA(1)) {
		case COMMA:
		{
			match(COMMA);
			exprList=expressionList();
			if ( inputState.guessing==0 ) {
				
					expressions.addAll(exprList);
				
			}
			break;
		}
		case END:
		case RPAREN:
		{
			break;
		}
		default:
		{
			throw new NoViableAltException(LT(1), getFilename());
		}
		}
		}
		return expressions;
	}
	
/**	matches a bag construction
*/
	public final BagConstruction  bagConstruction() throws RecognitionException, TokenStreamException, ParseException,DuplicateVariableException {
		BagConstruction bagConstr = null;
		
		
			Vector<VertexPosition> expressions = new Vector<VertexPosition>();
		
		
		match(BAG);
		match(LPAREN);
		{
		switch ( LA(1)) {
		case NUM_REAL:
		case FUNCTIONID:
		case THIS:
		case FALSE:
		case NOT:
		case NULL_VALUE:
		case TRUE:
		case BAG:
		case E:
		case ESUBGRAPH:
		case EXISTS:
		case FORALL:
		case FROM:
		case LET:
		case LIST:
		case PATH:
		case PATHSYSTEM:
		case REC:
		case SET:
		case TUP:
		case V:
		case VSUBGRAPH:
		case LPAREN:
		case LBRACK:
		case LCURLY:
		case L_T:
		case MINUS:
		case EDGESTART:
		case EDGE:
		case RARROW:
		case LARROW:
		case ARROW:
		case HASH:
		case STRING_LITERAL:
		case IDENT:
		case NUM_INT:
		{
			expressions=expressionList();
			break;
		}
		case RPAREN:
		{
			break;
		}
		default:
		{
			throw new NoViableAltException(LT(1), getFilename());
		}
		}
		}
		match(RPAREN);
		if ( inputState.guessing==0 ) {
			
				bagConstr = graph.createBagConstruction();
				for (int i = 0; i < expressions.size(); i++) {
							VertexPosition expr = expressions.get(i);
							IsPartOf exprOf = graph.createIsPartOf((Expression)expr.node, bagConstr);
							exprOf.setSourcePositions((createSourcePositionList(expr.length, expr.offset)));
						}
			
		}
		return bagConstr;
	}
	
/** matches a list construction
*/
	public final ListConstruction  listConstruction() throws RecognitionException, TokenStreamException, ParseException,DuplicateVariableException {
		ListConstruction listConstr = null;
		
		
			Vector<VertexPosition> expressions = new Vector<VertexPosition>();
		// int offset = 0;
		
		
		match(LIST);
		match(LPAREN);
		{
		boolean synPredMatched223 = false;
		if (((_tokenSet_10.member(LA(1))))) {
			int _m223 = mark();
			synPredMatched223 = true;
			inputState.guessing++;
			try {
				{
				expression();
				match(DOTDOT);
				}
			}
			catch (RecognitionException pe) {
				synPredMatched223 = false;
			}
			rewind(_m223);
inputState.guessing--;
		}
		if ( synPredMatched223 ) {
			if ( inputState.guessing==0 ) {
				
								// offset = LT(1).getColumn()-1;
							
			}
			listConstr=listRangeExpression();
		}
		else if ((_tokenSet_13.member(LA(1)))) {
			{
			switch ( LA(1)) {
			case NUM_REAL:
			case FUNCTIONID:
			case THIS:
			case FALSE:
			case NOT:
			case NULL_VALUE:
			case TRUE:
			case BAG:
			case E:
			case ESUBGRAPH:
			case EXISTS:
			case FORALL:
			case FROM:
			case LET:
			case LIST:
			case PATH:
			case PATHSYSTEM:
			case REC:
			case SET:
			case TUP:
			case V:
			case VSUBGRAPH:
			case LPAREN:
			case LBRACK:
			case LCURLY:
			case L_T:
			case MINUS:
			case EDGESTART:
			case EDGE:
			case RARROW:
			case LARROW:
			case ARROW:
			case HASH:
			case STRING_LITERAL:
			case IDENT:
			case NUM_INT:
			{
				expressions=expressionList();
				if ( inputState.guessing==0 ) {
					
						       		listConstr = graph.createListConstruction();
						        for (int i = 0; i < expressions.size(); i++) {
											VertexPosition expr = expressions.get(i);
											IsPartOf exprOf = graph.createIsPartOf((Expression)expr.node, listConstr);
											exprOf.setSourcePositions((createSourcePositionList(expr.length, expr.offset)));
										}
						     	
				}
				break;
			}
			case RPAREN:
			{
				break;
			}
			default:
			{
				throw new NoViableAltException(LT(1), getFilename());
			}
			}
			}
		}
		else {
			throw new NoViableAltException(LT(1), getFilename());
		}
		
		}
		match(RPAREN);
		return listConstr;
	}
	
/** matches a path construction
*/
	public final PathConstruction  pathConstruction() throws RecognitionException, TokenStreamException, ParseException,DuplicateVariableException {
		PathConstruction pathConstr = null;
		
		
			Vector<VertexPosition> expressions = new Vector<VertexPosition>();
		
		
		match(PATH);
		match(LPAREN);
		expressions=expressionList();
		match(RPAREN);
		if ( inputState.guessing==0 ) {
			
					    pathConstr = graph.createPathConstruction();
					    for (int i = 0; i < expressions.size(); i++) {
							VertexPosition expr = expressions.get(i);
							IsPartOf exprOf = graph.createIsPartOf((Expression)expr.node, pathConstr);
							exprOf.setSourcePositions((createSourcePositionList(expr.length, expr.offset)));
						}
			
		}
		return pathConstr;
	}
	
/** matches a pathsystem construction
*/
	public final PathSystemConstruction  pathsystemConstruction() throws RecognitionException, TokenStreamException, ParseException,DuplicateVariableException {
		PathSystemConstruction pathsystemConstr = null;
		
		
			Expression expr = null;
		EdgeVertexList eVList = null;
		int offsetExpr = 0;
		int offsetEVList = 0;
		int lengthExpr = 0;
		int lengthEVList = 0;
		
		
		match(PATHSYSTEM);
		match(LPAREN);
		if ( inputState.guessing==0 ) {
			offsetExpr = LT(1).getColumn()-1;
		}
		expr=expression();
		if ( inputState.guessing==0 ) {
			
				lengthExpr = -offsetExpr + LT(0).getColumn()-1 +LT(0).getText().length();
					pathsystemConstr = graph.createPathSystemConstruction();
				       	IsRootOf rootOf = graph.createIsRootOf(expr, pathsystemConstr);
				       	rootOf.setSourcePositions((createSourcePositionList(lengthExpr, offsetExpr)));
			
		}
		{
		_loop234:
		do {
			if ((LA(1)==COMMA)) {
				match(COMMA);
				if ( inputState.guessing==0 ) {
					offsetEVList = LT(1).getColumn()-1;
				}
				eVList=edgeVertexList();
				if ( inputState.guessing==0 ) {
					
						lengthEVList = -offsetEVList + LT(0).getColumn()-1 +LT(0).getText().length();
						IsEdgeVertexListOf exprOf = graph.createIsEdgeVertexListOf(eVList, pathsystemConstr);
						exprOf.setSourcePositions((createSourcePositionList(lengthEVList, offsetEVList)));
					
				}
			}
			else {
				break _loop234;
			}
			
		} while (true);
		}
		match(RPAREN);
		return pathsystemConstr;
	}
	
/** matches a record construction
*/
	public final RecordConstruction  recordConstruction() throws RecognitionException, TokenStreamException, ParseException,DuplicateVariableException {
		RecordConstruction recConstr = null;
		
		
			Vector<VertexPosition> elements = new Vector<VertexPosition>();
		
		
		match(REC);
		match(LPAREN);
		elements=recordElementList();
		match(RPAREN);
		if ( inputState.guessing==0 ) {
			
					    recConstr = graph.createRecordConstruction();
						for (int i = 0; i < elements.size(); i++) {
							VertexPosition expr = elements.get(i);
							IsRecordElementOf exprOf = graph.createIsRecordElementOf((RecordElement)expr.node, recConstr);
							exprOf.setSourcePositions((createSourcePositionList(expr.length, expr.offset)));
						}
			
		}
		return recConstr;
	}
	
/**	matches a set construction
*/
	public final SetConstruction  setConstruction() throws RecognitionException, TokenStreamException, ParseException,DuplicateVariableException {
		SetConstruction setConstr = null;
		
		
			Vector<VertexPosition> expressions = new Vector<VertexPosition>();
		
		
		match(SET);
		match(LPAREN);
		{
		switch ( LA(1)) {
		case NUM_REAL:
		case FUNCTIONID:
		case THIS:
		case FALSE:
		case NOT:
		case NULL_VALUE:
		case TRUE:
		case BAG:
		case E:
		case ESUBGRAPH:
		case EXISTS:
		case FORALL:
		case FROM:
		case LET:
		case LIST:
		case PATH:
		case PATHSYSTEM:
		case REC:
		case SET:
		case TUP:
		case V:
		case VSUBGRAPH:
		case LPAREN:
		case LBRACK:
		case LCURLY:
		case L_T:
		case MINUS:
		case EDGESTART:
		case EDGE:
		case RARROW:
		case LARROW:
		case ARROW:
		case HASH:
		case STRING_LITERAL:
		case IDENT:
		case NUM_INT:
		{
			expressions=expressionList();
			break;
		}
		case RPAREN:
		{
			break;
		}
		default:
		{
			throw new NoViableAltException(LT(1), getFilename());
		}
		}
		}
		match(RPAREN);
		if ( inputState.guessing==0 ) {
			
				setConstr = graph.createSetConstruction();
			for (int i = 0; i < expressions.size(); i++) {
							VertexPosition expr = expressions.get(i);
							IsPartOf exprOf = graph.createIsPartOf((Expression)expr.node, setConstr);
							exprOf.setSourcePositions((createSourcePositionList(expr.length, expr.offset)));
						}
			
		}
		return setConstr;
	}
	
/** matches a tupel construction
*/
	public final TupleConstruction  tupleConstruction() throws RecognitionException, TokenStreamException, ParseException,DuplicateVariableException {
		TupleConstruction tupConstr = null;
		
		
			Vector<VertexPosition> expressions = new Vector<VertexPosition>();
		
		
		match(TUP);
		match(LPAREN);
		expressions=expressionList();
		match(RPAREN);
		if ( inputState.guessing==0 ) {
			
				tupConstr = graph.createTupleConstruction();
				for (int i = 0; i < expressions.size(); i++) {
							VertexPosition expr = expressions.get(i);
							IsPartOf exprOf = graph.createIsPartOf((Expression)expr.node, tupConstr);
							exprOf.setSourcePositions((createSourcePositionList(expr.length, expr.offset)));
						}
			
		}
		return tupConstr;
	}
	
/** matches a listrange expression: integer-expression .. integer-expression
	@return
*/
	public final ListRangeConstruction  listRangeExpression() throws RecognitionException, TokenStreamException, ParseException,DuplicateVariableException {
		ListRangeConstruction expr = null;
		
		
			Expression startExpr = null;
			Expression endExpr = null;
			int offsetStart = 0;
			int offsetEnd = 0;
			int lengthStart = 0;
			int lengthEnd = 0;
		
		
		if ( inputState.guessing==0 ) {
			offsetStart = LT(1).getColumn()-1;
		}
		startExpr=expression();
		if ( inputState.guessing==0 ) {
			lengthStart = -offsetStart + LT(0).getColumn()-1 +LT(0).getText().length();
		}
		match(DOTDOT);
		if ( inputState.guessing==0 ) {
			offsetEnd = LT(1).getColumn()-1;
		}
		endExpr=expression();
		if ( inputState.guessing==0 ) {
			
				lengthEnd = -offsetEnd + LT(0).getColumn()-1 +LT(0).getText().length();
				// expr = graph.createListRangeExpression();
				        expr = graph.createListRangeConstruction();
			IsFirstValueOf firstValueOf = graph.createIsFirstValueOf(startExpr, expr);
			firstValueOf.setSourcePositions((createSourcePositionList(lengthStart, offsetStart)));
				IsLastValueOf lastValueOf = graph.createIsLastValueOf(endExpr, expr);
				lastValueOf.setSourcePositions((createSourcePositionList(lengthEnd, offsetEnd)));
			
		}
		return expr;
	}
	
/** matches a list of record-elements
*/
	public final Vector<VertexPosition>  recordElementList() throws RecognitionException, TokenStreamException, ParseException,DuplicateVariableException {
		Vector<VertexPosition> elements = new Vector<VertexPosition>();;
		
		
			RecordElement v = null;
			Vector<VertexPosition> list = null;
		VertexPosition recElement = new VertexPosition();
		int offset = 0;
		int length = 0;
		
		
		if ( inputState.guessing==0 ) {
			offset = LT(1).getColumn()-1;
		}
		v=recordElement();
		if ( inputState.guessing==0 ) {
			
				length = -offset + LT(0).getColumn()-1 +LT(0).getText().length();
			recElement.node = v;
			recElement.offset = offset;
			recElement.length = length;
				elements.add(recElement);
			
		}
		{
		switch ( LA(1)) {
		case COMMA:
		{
			match(COMMA);
			list=recordElementList();
			if ( inputState.guessing==0 ) {
				elements.addAll(list);
			}
			break;
		}
		case RPAREN:
		{
			break;
		}
		default:
		{
			throw new NoViableAltException(LT(1), getFilename());
		}
		}
		}
		return elements;
	}
	
/** matches a record element consisting of an id, a colon and an expression
*/
	public final RecordElement  recordElement() throws RecognitionException, TokenStreamException, ParseException,DuplicateVariableException {
		RecordElement recElement = null;
		
		
			RecordId recId = null;
			Expression expr = null;
		int offsetRecId = 0;
		int offsetExpr = 0;
		int lengthRecId = 0;
		int lengthExpr = 0;
		
		
		if ( inputState.guessing==0 ) {
			offsetRecId = LT(1).getColumn()-1;
		}
		recId=recordId();
		if ( inputState.guessing==0 ) {
			lengthRecId = -offsetRecId + LT(0).getColumn()-1 +LT(0).getText().length();
		}
		match(COLON);
		if ( inputState.guessing==0 ) {
			offsetExpr = LT(1).getColumn()-1;
		}
		expr=expression();
		if ( inputState.guessing==0 ) {
			
				lengthExpr = -offsetExpr + LT(0).getColumn()-1 +LT(0).getText().length();
			recElement = graph.createRecordElement();
				IsRecordIdOf recIdOf = graph.createIsRecordIdOf(recId, recElement);
				recIdOf.setSourcePositions((createSourcePositionList(lengthRecId, offsetRecId)));
			IsRecordExprOf  exprOf = graph.createIsRecordExprOf(expr, recElement);
			exprOf.setSourcePositions((createSourcePositionList(lengthExpr, offsetExpr)));
			
		}
		return recElement;
	}
	
/** matches a record-id
*/
	public final RecordId  recordId() throws RecognitionException, TokenStreamException, ParseException,DuplicateVariableException {
		RecordId expr = null;
		
		Token  i = null;
		
		i = LT(1);
		match(IDENT);
		if ( inputState.guessing==0 ) {
			
				expr = graph.createRecordId();
				expr.setName(i.getText());
			
		}
		return expr;
	}
	
/** matches a list of edges and vertices
	@return
*/
	public final EdgeVertexList  edgeVertexList() throws RecognitionException, TokenStreamException, ParseException,DuplicateVariableException {
		EdgeVertexList eVList = null;
		
		
			Expression edgeExpr = null;
			Expression  vertexExpr = null;
			EdgeVertexList eVList2 = null;
		int offsetV = 0;
		int offsetE = 0;
		int offsetEVList = 0;
		int lengthV = 0;
		int lengthE = 0;
		int lengthEVList = 0;
		
		
		match(LPAREN);
		if ( inputState.guessing==0 ) {
			offsetE = LT(1).getColumn()-1;
		}
		edgeExpr=expression();
		if ( inputState.guessing==0 ) {
			lengthE = -offsetE + LT(0).getColumn()-1 + LT(0).getText().length();
		}
		match(COMMA);
		if ( inputState.guessing==0 ) {
			offsetV = LT(1).getColumn()-1;
		}
		vertexExpr=expression();
		if ( inputState.guessing==0 ) {
			
				lengthV = -offsetV + LT(0).getColumn()-1 + LT(0).getText().length();
			eVList = graph.createEdgeVertexList();
			IsEdgeOrVertexExprOf eExprOf = graph.createIsEdgeOrVertexExprOf(edgeExpr, eVList);
			eExprOf.setSourcePositions((createSourcePositionList(lengthE, offsetE)));
			IsEdgeOrVertexExprOf vExprOf = graph.createIsEdgeOrVertexExprOf(vertexExpr, eVList);
			vExprOf.setSourcePositions((createSourcePositionList(lengthV, offsetV)));
			
		}
		{
		_loop303:
		do {
			if ((LA(1)==COMMA)) {
				match(COMMA);
				if ( inputState.guessing==0 ) {
					offsetEVList = LT(1).getColumn()-1;
				}
				eVList2=edgeVertexList();
				if ( inputState.guessing==0 ) {
					
						lengthEVList = -offsetEVList + LT(0).getColumn()-1 + LT(0).getText().length();
							IsElementOf exprOf = graph.createIsElementOf(eVList2, eVList);
						exprOf.setSourcePositions((createSourcePositionList(lengthEVList, offsetEVList)));
					
				}
			}
			else {
				break _loop303;
			}
			
		} while (true);
		}
		match(RPAREN);
		return eVList;
	}
	
/** matches a comma-seperated list of simple declarations
*/
	public final Vector<VertexPosition>  declarationList() throws RecognitionException, TokenStreamException, ParseException,DuplicateVariableException {
		Vector<VertexPosition> declList = new Vector<VertexPosition>();;
		
		
			SimpleDeclaration v = null;
		VertexPosition simpleDecl = new VertexPosition();
		int offset = 0;
		int length = 0;
		
		
		if ( inputState.guessing==0 ) {
			offset = LT(1).getColumn()-1;
		}
		v=simpleDeclaration();
		if ( inputState.guessing==0 ) {
			
				length = -offset + LT(0).getColumn()-1 +LT(0).getText().length();
				simpleDecl.node = v;
			simpleDecl.offset = offset;
			simpleDecl.length = length;
				declList.add(simpleDecl);
			
		}
		{
		boolean synPredMatched246 = false;
		if (((LA(1)==COMMA))) {
			int _m246 = mark();
			synPredMatched246 = true;
			inputState.guessing++;
			try {
				{
				match(COMMA);
				simpleDeclaration();
				}
			}
			catch (RecognitionException pe) {
				synPredMatched246 = false;
			}
			rewind(_m246);
inputState.guessing--;
		}
		if ( synPredMatched246 ) {
			declList=declarationList2(declList);
		}
		else if ((_tokenSet_14.member(LA(1)))) {
		}
		else {
			throw new NoViableAltException(LT(1), getFilename());
		}
		
		}
		return declList;
	}
	
/** matches a simple declaration: variablelist ':' set-expression
*/
	public final SimpleDeclaration  simpleDeclaration() throws RecognitionException, TokenStreamException, ParseException,DuplicateVariableException {
		SimpleDeclaration simpleDecl = null;
		
		
			Expression expr = null;
		Vector<VertexPosition> variables = new Vector<VertexPosition>();
		int offset = 0;
		int length = 0;
		
		
		variables=variableList();
		match(COLON);
		if ( inputState.guessing==0 ) {
			offset = LT(1).getColumn()-1;
		}
		expr=expression();
		if ( inputState.guessing==0 ) {
			
				length = -offset + LT(0).getColumn()-1 +LT(0).getText().length();
			simpleDecl = graph.createSimpleDeclaration();
				        for (int i = 0; i < variables.size(); i++) {
							VertexPosition var = variables.get(i);
							IsDeclaredVarOf varOf = graph.createIsDeclaredVarOf((Variable)var.node, simpleDecl);
							varOf.setSourcePositions((createSourcePositionList(var.length, var.offset)));
						}
				        IsTypeExprOf typeExprOf = graph.createIsTypeExprOfDeclaration(expr, simpleDecl);
				        typeExprOf.setSourcePositions((createSourcePositionList(length, offset)));
			
			
		}
		return simpleDecl;
	}
	
/**
*/
	public final Vector<VertexPosition>  declarationList2(
		Vector<VertexPosition> list
	) throws RecognitionException, TokenStreamException, ParseException,DuplicateVariableException {
		Vector<VertexPosition> declList = new Vector<VertexPosition>();;
		
		
			SimpleDeclaration v = null;
			VertexPosition simpleDecl = new VertexPosition();
			 int offset = 0;
		int length = 0;
		
		
		if ( inputState.guessing==0 ) {
			
					   declList.addAll(list);
				
		}
		match(COMMA);
		if ( inputState.guessing==0 ) {
			offset = LT(1).getColumn()-1;
		}
		v=simpleDeclaration();
		if ( inputState.guessing==0 ) {
			
				length = -offset + LT(0).getColumn()-1 +LT(0).getText().length();
			simpleDecl.node = v;
			simpleDecl.offset = offset;
			simpleDecl.length = length;
					declList.add(simpleDecl);
				
		}
		{
		boolean synPredMatched250 = false;
		if (((LA(1)==COMMA))) {
			int _m250 = mark();
			synPredMatched250 = true;
			inputState.guessing++;
			try {
				{
				match(COMMA);
				simpleDeclaration();
				}
			}
			catch (RecognitionException pe) {
				synPredMatched250 = false;
			}
			rewind(_m250);
inputState.guessing--;
		}
		if ( synPredMatched250 ) {
			{
			declList=declarationList2(declList);
			}
		}
		else if ((_tokenSet_14.member(LA(1)))) {
		}
		else {
			throw new NoViableAltException(LT(1), getFilename());
		}
		
		}
		return declList;
	}
	
/**	returns a comprehension including the comprehension result
*/
	public final Comprehension  reportClause() throws RecognitionException, TokenStreamException, ParseException,DuplicateVariableException,ParseException {
		Comprehension comprehension = null;
		
		
			Vector<VertexPosition> reportList = new Vector<VertexPosition>();
			int offset = 0;
			int length = 0;
			boolean vartable = false;
		
		
		switch ( LA(1)) {
		case REPORT:
		{
			{
			match(REPORT);
			comprehension=labeledReportList();
			}
			break;
		}
		case REPORTSET:
		case REPORTBAG:
		case REPORTTABLE:
		{
			{
			{
			switch ( LA(1)) {
			case REPORTBAG:
			{
				match(REPORTBAG);
				if ( inputState.guessing==0 ) {
					try {comprehension = graph.createBagComprehension(); } catch (Exception ex) { ex.printStackTrace(); }
				}
				break;
			}
			case REPORTSET:
			{
				match(REPORTSET);
				if ( inputState.guessing==0 ) {
					try { comprehension = graph.createSetComprehension(); } catch (Exception ex) { ex.printStackTrace(); }
				}
				break;
			}
			case REPORTTABLE:
			{
				match(REPORTTABLE);
				if ( inputState.guessing==0 ) {
					try { comprehension = graph.createTableComprehension(); vartable = true; }	catch (Exception ex) { ex.printStackTrace(); }
				}
				break;
			}
			default:
			{
				throw new NoViableAltException(LT(1), getFilename());
			}
			}
			}
			if ( inputState.guessing==0 ) {
				offset = LT(1).getColumn()-1;
			}
			reportList=expressionList();
			if ( inputState.guessing==0 ) {
				
				length = -offset + LT(0).getColumn()-1 + LT(0).getText().length();
							   	IsCompResultDefOf e = null;
					            if (!vartable) {
						    	   	if (reportList.size() > 1) {
									   	TupleConstruction tupConstr = graph.createTupleConstruction();
					           		   	e = graph.createIsCompResultDefOf(tupConstr, comprehension);
									   	for (int i = 0; i < reportList.size(); i++) {
											VertexPosition expr = reportList.get(i);
											IsPartOf partOf = graph.createIsPartOf((Expression)expr.node, tupConstr);
											partOf.setSourcePositions((createSourcePositionList(expr.length, expr.offset)));
										}
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
			}
			break;
		}
		default:
		{
			throw new NoViableAltException(LT(1), getFilename());
		}
		}
		return comprehension;
	}
	
/** matches a report-list with labels
	@return Bag-Comprehension-Vertex with <br>
	a) a TupelConstruction as result or <br>
	b) the expression as result (if the reportlist has only one element)
*/
	public final BagComprehension  labeledReportList() throws RecognitionException, TokenStreamException, ParseException,DuplicateVariableException {
		BagComprehension bagCompr = null;
		
		
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
		
		
		if ( inputState.guessing==0 ) {
			offsetExpr = LT(1).getColumn()-1;
				  offset = offsetExpr;
				
		}
		expr=expression();
		if ( inputState.guessing==0 ) {
			lengthExpr = -offsetExpr + LT(0).getColumn()-1 + LT(0).getText().length();
		}
		{
		switch ( LA(1)) {
		case AS:
		{
			match(AS);
			if ( inputState.guessing==0 ) {
				offsetAsExpr = LT(1).getColumn()-1;
			}
			asExpr=expression();
			if ( inputState.guessing==0 ) {
				
					lengthAsExpr = -offsetAsExpr + LT(0).getColumn()-1 + LT(0).getText().length();
					hasLabel = true;
				
			}
			break;
		}
		case END:
		case COMMA:
		{
			break;
		}
		default:
		{
			throw new NoViableAltException(LT(1), getFilename());
		}
		}
		}
		if ( inputState.guessing==0 ) {
			
			bagCompr = graph.createBagComprehension();
						tupConstr = graph.createTupleConstruction();
						e = graph.createIsCompResultDefOf(tupConstr, bagCompr);
						IsPartOf partOf = graph.createIsPartOf((Expression)expr, tupConstr);
						partOf.setSourcePositions((createSourcePositionList(lengthExpr, offsetExpr)));
					  	if (hasLabel) {
						    IsTableHeaderOf tableHeaderOf = graph.createIsTableHeaderOf(asExpr, bagCompr);
						    tableHeaderOf.setSourcePositions((createSourcePositionList(lengthAsExpr, offsetAsExpr)));
						} else {
				StringLiteral emptyString = graph.createStringLiteral();
				emptyString.setStringValue("");
				IsTableHeaderOf tableHeaderOf = graph.createIsTableHeaderOf(emptyString, bagCompr);
				tableHeaderOf.setSourcePositions((createSourcePositionList(-1, -1)));
				    	}
					
		}
		{
		_loop280:
		do {
			if ((LA(1)==COMMA)) {
				if ( inputState.guessing==0 ) {
					hasLabel = false;
				}
				match(COMMA);
				if ( inputState.guessing==0 ) {
					offsetExpr = LT(1).getColumn()-1;
				}
				expr=expression();
				if ( inputState.guessing==0 ) {
					lengthExpr = -offsetExpr + LT(0).getColumn()-1 + LT(0).getText().length();
				}
				{
				switch ( LA(1)) {
				case AS:
				{
					match(AS);
					if ( inputState.guessing==0 ) {
						offsetAsExpr = LT(1).getColumn()-1;
					}
					asExpr=expression();
					if ( inputState.guessing==0 ) {
						
							lengthAsExpr = -offsetAsExpr + LT(0).getColumn()-1 + LT(0).getText().length();
							hasLabel = true;
						
					}
					break;
				}
				case END:
				case COMMA:
				{
					break;
				}
				default:
				{
					throw new NoViableAltException(LT(1), getFilename());
				}
				}
				}
				if ( inputState.guessing==0 ) {
					
								    IsPartOf partOf = graph.createIsPartOf(expr, tupConstr);
								    partOf.setSourcePositions((createSourcePositionList(lengthExpr, offsetExpr)));
							  	   	if (hasLabel) {
									    IsTableHeaderOf tableHeaderOf = graph.createIsTableHeaderOf(asExpr, bagCompr);
										tableHeaderOf.setSourcePositions((createSourcePositionList(lengthAsExpr, offsetAsExpr)));
								    } else {
						            	StringLiteral emptyString = graph.createStringLiteral();
						            	emptyString.setStringValue("");
						            	IsTableHeaderOf tableHeaderOf = graph.createIsTableHeaderOf(emptyString, bagCompr);
										tableHeaderOf.setSourcePositions((createSourcePositionList(-1, -1)));
							    	}
							
				}
			}
			else {
				break _loop280;
			}
			
		} while (true);
		}
		if ( inputState.guessing==0 ) {
			
						e.setSourcePositions((createSourcePositionList(-offset + LT(0).getColumn()-1 + LT(0).getText().length(), offset)));
					
		}
		if ( inputState.guessing==0 ) {
			
					  	if (tupConstr.getDegree(EdgeDirection.IN) == 1)	{
							Vertex v = tupConstr.getFirstEdge(EdgeDirection.IN).getAlpha();
							Edge e2 = tupConstr.getFirstEdge(EdgeDirection.OUT);
							e2.setAlpha(v);
							tupConstr.delete();
						}
					
		}
		return bagCompr;
	}
	
/** matches a role-id
	@return
*/
	public final RoleId  roleId() throws RecognitionException, TokenStreamException, ParseException,DuplicateVariableException {
		RoleId role = null;
		
		Token  i = null;
		
		if ( inputState.guessing==0 ) {
			
						role = graph.createRoleId();
					
		}
		i = LT(1);
		match(IDENT);
		if ( inputState.guessing==0 ) {
			role.setName(i.getText());
		}
		return role;
	}
	
	
	public static final String[] _tokenNames = {
		"<0>",
		"EOF",
		"<2>",
		"NULL_TREE_LOOKAHEAD",
		"NUM_REAL",
		"DOTDOT",
		"DOT",
		"FUNCTIONID",
		"THIS",
		"\"and\"",
		"\"andThen\"",
		"\"false\"",
		"\"not\"",
		"\"null\"",
		"\"or\"",
		"\"orElse\"",
		"\"true\"",
		"\"xor\"",
		"\"as\"",
		"\"bag\"",
		"\"E\"",
		"\"eSubgraph\"",
		"\"exists!\"",
		"\"exists\"",
		"\"end\"",
		"\"forall\"",
		"\"from\"",
		"\"in\"",
		"\"let\"",
		"\"list\"",
		"\"path\"",
		"\"pathsystem\"",
		"\"rec\"",
		"\"report\"",
		"\"reportSet\"",
		"\"reportBag\"",
		"\"reportTable\"",
		"\"store\"",
		"\"set\"",
		"\"T\"",
		"\"tup\"",
		"\"using\"",
		"\"V\"",
		"\"vSubgraph\"",
		"\"where\"",
		"\"with\"",
		"'?'",
		"'!'",
		"':'",
		"','",
		"'@'",
		"'('",
		"')'",
		"'['",
		"']'",
		"'{'",
		"'}'",
		"':='",
		"'::='",
		"'='",
		"'=~'",
		"'<>'",
		"'<='",
		"'>='",
		"'<'",
		"'>'",
		"'/'",
		"'+'",
		"'-'",
		"'*'",
		"'%'",
		"';'",
		"'^'",
		"'|'",
		"'&'",
		"':-)'",
		"'<-'",
		"'->'",
		"'--'",
		"'-->'",
		"'<--'",
		"'<->'",
		"'#'",
		"WS",
		"SL_COMMENT",
		"ML_COMMENT",
		"a string literal",
		"ESC",
		"an identifier",
		"HEX_DIGIT",
		"DIGIT",
		"NONZERO_DIGIT",
		"OCT_DIGIT",
		"NUM_INT",
		"EXPONENT"
	};
	
	private static final long[] mk_tokenSet_0() {
		long[] data = { 96299892846362658L, 24576L, 0L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_0 = new BitSet(mk_tokenSet_0());
	private static final long[] mk_tokenSet_1() {
		long[] data = { -3458764513820540928L, 3L, 0L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_1 = new BitSet(mk_tokenSet_1());
	private static final long[] mk_tokenSet_2() {
		long[] data = { 2978973090270594594L, 24576L, 0L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_2 = new BitSet(mk_tokenSet_2());
	private static final long[] mk_tokenSet_3() {
		long[] data = { 47287796087390208L, 249856L, 0L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_3 = new BitSet(mk_tokenSet_3());
	private static final long[] mk_tokenSet_4() {
		long[] data = { 2266376466672016L, 557842432L, 0L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_4 = new BitSet(mk_tokenSet_4());
	private static final long[] mk_tokenSet_5() {
		long[] data = { -479791423549946334L, 24703L, 0L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_5 = new BitSet(mk_tokenSet_5());
	private static final long[] mk_tokenSet_6() {
		long[] data = { -432503627462556126L, 17038463L, 0L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_6 = new BitSet(mk_tokenSet_6());
	private static final long[] mk_tokenSet_7() {
		long[] data = { -432503627462556062L, 17300607L, 0L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_7 = new BitSet(mk_tokenSet_7());
	private static final long[] mk_tokenSet_8() {
		long[] data = { -432489050809569294L, 558365823L, 0L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_8 = new BitSet(mk_tokenSet_8());
	private static final long[] mk_tokenSet_9() {
		long[] data = { -477525047083274318L, 558131839L, 0L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_9 = new BitSet(mk_tokenSet_9());
	private static final long[] mk_tokenSet_10() {
		long[] data = { 47302373050759568L, 558354449L, 0L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_10 = new BitSet(mk_tokenSet_10());
	private static final long[] mk_tokenSet_11() {
		long[] data = { -432489050809569358L, 558366335L, 0L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_11 = new BitSet(mk_tokenSet_11());
	private static final long[] mk_tokenSet_12() {
		long[] data = { -432489050809569358L, 558366591L, 0L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_12 = new BitSet(mk_tokenSet_12());
	private static final long[] mk_tokenSet_13() {
		long[] data = { 51805972678130064L, 558354449L, 0L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_13 = new BitSet(mk_tokenSet_13());
	private static final long[] mk_tokenSet_14() {
		long[] data = { 1724163215589376L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_14 = new BitSet(mk_tokenSet_14());
	
	}
