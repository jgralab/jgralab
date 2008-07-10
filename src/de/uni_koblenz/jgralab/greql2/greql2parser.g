//
// JGraLab - The Java graph laboratory
// (c) 2006-2007 Institute for Software Technology
//               University of Koblenz-Landau, Germany
//
//               ist@uni-koblenz.de
//
// This program is free software; you can redistribute it and/or
// modify it under the terms of the GNU General Public License
// as published by the Free Software Foundation; either version 2
// of the License, or (at your option) any later version.
//
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU General Public License for more details.
//
// You should have received a copy of the GNU General Public License
// along with this program; if not, write to the Free Software
// Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
//
 
header
{
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
}

class Greql2Parser extends Parser;
options
{
	k = 1;
	importVocab = Greql2Lexer;
	defaultErrorHandler = false;
}
{
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
	    graphClass = schema.getGraphClass(new QualifiedName("Greql2"));
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

	/** Replaces instances of thisVertex-Literals in restrictions by the
	    vertex-Expression that is restricted.
		@param restriction  the vertex representing the restriction
		@param restrictedExpr the expression that is restricted
	*/
	private void mergeRestrictedExpr(Vertex restriction, Expression expr) throws DuplicateVariableException, UndefinedVariableException {
	//	Edge inc = restriction.getFirstEdge(EdgeDirection.IN);
	//	while (inc != null)	{
	//	    Vertex thisLit = inc.getAlpha();
	//		if ((thisLit instanceof ThisVertex) || (thisList instanceof ThisEdge))	{
			//	String name = ((ThisLiteral) thisLit).getThisValue();
			//	if ( (name.equals("thisVertex") && !(expr instanceof PathDescription))
			//	   ||(name.equals("thisEdge") && (expr instanceof PathDescription)) ) {
	//				inc.setAlpha(expr);
	//				if (thisLit.getDegree() <= 0) 
	//					thisLit.delete();
			//	}
	//			mergeRestrictedExpr(inc.getAlpha(), expr);
	//		}
	//		inc = inc.getNextEdge(EdgeDirection.IN);
	//	}
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

// ----------------------------------------------------------------------------------------------------
//    grammer rules begin
// ----------------------------------------------------------------------------------------------------
/** matches a GReQL 2-Query */
greqlExpression throws ParseException, DuplicateVariableException, UndefinedVariableException 
{
	Expression expr = null;
	Vector<VertexPosition> varList = new Vector<VertexPosition>();
	int offset = 0;
	int length = 0;
}
	:	{   // set inital values
			initialize();
		}
		( (USING varList = variableList COLON)?
	    { offset = LT(1).getColumn()-1; }
		expr = expression
		( STORE AS id:IDENT  )?
	    {
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
	    EOF
        {
	   		mergeVariables();
	    }
	    | EOF {graph = null;}/* allow empty input */ )
	;
	exception
    catch [RecognitionException ex] {
        offset = LT(1).getColumn()-1;
        length = LT(1).getText().length();
        throw new ParseException(ex.getMessage(), LT(1).getText(), new SourcePosition (length, offset), ex);
    }
    catch [TokenStreamException ex] {
	  offset = LT(1).getColumn()-1;
      length = LT(1).getText().length();
      throw new ParseException(ex.getMessage(), LT(1).getText(), new SourcePosition (length, offset), ex);
    }

/** matches expressions
    @return  vertex representing the expression
*/
expression returns [Expression expr = null] throws ParseException, DuplicateVariableException
	:
		expr = quantifiedExpression
	;

/** matches quantifiedExpressions
    @return vertex representing the quantified expression
*/
quantifiedExpression returns [Expression expr = null]
	throws ParseException, DuplicateVariableException
{
	Quantifier q;
	Declaration decl;
	int offsetQuantifier = 0;
	int offsetQuantifiedDecl = 0;
	int offsetQuantifiedExpr = 0;
	int lengthQuantifier = 0;
	int lengthQuantifiedDecl = 0;
	int lengthQuantifiedExpr = 0;
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
          	expr = quantifiedExpression
            {
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
		)
        | // not a "real" quantified expression
		  expr = letExpression
	;

/** matches let-expressions
    @return
*/
letExpression returns [Expression expr = null]
	throws ParseException, DuplicateVariableException
{
	Vector<VertexPosition> defList = new Vector<VertexPosition>();
	int offset = 0;
	int length = 0;
}
	:
		(
			LET
			// definitions
			defList = definitionList
			IN
			{ offset = LT(1).getColumn()-1; }
			// bound expression
            expr = letExpression
			{
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
		)
		| // not a let-Expression
		  expr = whereExpression
	;

/** matches Where-Expressions
	@return
*/
whereExpression returns [Expression expr = null]
	throws ParseException, DuplicateVariableException
{
	Vector<VertexPosition> defList = new Vector<VertexPosition>();
	int offset = 0;
	int length = 0;
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

/** matches conditional expressions
    @return
*/
conditionalExpression returns [Expression expr = null]
	throws ParseException, DuplicateVariableException
{
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
            { lengthTrueExpr = -offsetTrueExpr + LT(0).getColumn()-1 + LT(0).getText().length(); }
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

/** matches first argument of or- and orElse-Expressions
	@return
*/
orExpression returns [Expression expr = null] throws ParseException, DuplicateVariableException
{
	int offset = 0;
	int length = 0;
}	:
        { offset = LT(1).getColumn()-1; }
        // first argument
        expr = xorExpression
        { length = -offset + LT(0).getColumn()-1 + LT(0).getText().length(); }
		(	// rest of the or-expression, if it is one
            expr = orExpression2[expr, offset, length]
		)?
	;
/** matches operator and 2nd argument of or- and orElse-Expressions
	@param arg1 first argument
	@param offsetArg1 offset of first argument
	@param lengthArg1 length of first argument
	@return or-expression (FunctionApplication)
*/
orExpression2[Expression arg1, int offsetArg1, int lengthArg1]
	returns [FunctionApplication functionAppl = null]
	throws ParseException, DuplicateVariableException
{
	Expression arg2 = null;
	FunctionId op = null;
    String name = "orElse";
    int offsetOperator = 0;
    int offsetArg2 = 0;
    int lengthOperator = 0;
    int lengthArg2 = 0;
}
	:
		{ offsetOperator = LT(1).getColumn()-1; }
		// which operator?
		( OR { name = "or"; } | ORELSE)
		{ lengthOperator = -offsetOperator + LT(0).getColumn()-1 + LT(0).getText().length();
		  offsetArg2 = LT(1).getColumn()-1;
		}
		// 2nd argument
        arg2 = xorExpression
        {
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
		(   /* may be arg1 of another (following) or/Orelse  */
            functionAppl = orExpression2[functionAppl, offsetArg1,
            			   -offsetArg1 + LT(0).getColumn()-1 + LT(0).getText().length()]
		)?
	;

/** matches first argument of xor-Expression
	@return
*/
xorExpression returns [Expression expr = null] throws ParseException, DuplicateVariableException
{
	int offset = 0;
	int length = 0;
}	:
		{ offset = LT(1).getColumn()-1; }
        expr = andExpression
        { length = -offset + LT(0).getColumn()-1 + LT(0).getText().length(); }
		( 	// match rest
            expr = xorExpression2[expr, offset, length]
		)?
	;

/** matches operator and 2nd argument of xor-Expressions
	@param arg1 first argument
	@param offsetArg1 offset of first argument
	@param lengthArg1 length of first argument
	@return or-expression (FunctionApplication)
*/
xorExpression2[Expression arg1, int offsetArg1, int lengthArg1]
   	returns [FunctionApplication functionAppl = null]
	throws ParseException, DuplicateVariableException   
{
	Expression arg2 = null;
	FunctionId op = null;
    String name = "xor";
    int offsetOperator = 0;
    int offsetArg2 = 0;
    int lengthOperator = 0;
    int lengthArg2 = 0;
}
	:
		{ offsetOperator = LT(1).getColumn()-1; }
        XOR
		{ lengthOperator = -offsetOperator + LT(0).getColumn()-1 + LT(0).getText().length();
		  offsetArg2 = LT(1).getColumn()-1;
		}
    	arg2 = andExpression
		{
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
		(
            functionAppl = xorExpression2[functionAppl, offsetArg1,
            			   -offsetArg1 + LT(0).getColumn()-1 + LT(0).getText().length()]
		)?
	;

/** matches first argument of an and-or andThen-Expression
	@return
*/
andExpression returns [Expression expr = null] throws ParseException, DuplicateVariableException
{
	int offset = 0;
	int length = 0;
}	:
 		{ offset = LT(1).getColumn()-1; }
        expr = equalityExpression
        { length = -offset + LT(0).getColumn()-1 + LT(0).getText().length(); }
        (
            expr = andExpression2[expr, offset, length]
		)?
	;

/** matches operator and 2nd argument of and- or andthen-Expressions
	@param arg1 first argument
	@param offsetArg1 offset of first argument
	@param lengthArg1 length of first argument
	@return or-expression (FunctionApplication)
*/
andExpression2[Expression arg1, int offsetArg1, int lengthArg1]
	returns [FunctionApplication functionAppl = null]
	throws ParseException, DuplicateVariableException
{
	Expression arg2 = null;
	FunctionId  op = null;
    String name = "andThen";
    int offsetOperator = 0;
    int offsetArg2 = 0;
    int lengthOperator = 0;
    int lengthArg2 = 0;
}
	:
		{ offsetOperator = LT(1).getColumn()-1; }
        (AND { name = "and"; }| ANDTHEN)
		{ lengthOperator = -offsetOperator + LT(0).getColumn()-1 + LT(0).getText().length();
		  offsetArg2 = LT(1).getColumn()-1;
		}
        arg2 = equalityExpression
		{
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
		(
            functionAppl = andExpression2[functionAppl, offsetArg1,
            			   -offsetArg1 + LT(0).getColumn()-1 + LT(0).getText().length()]
		)?
	;

/** matches first argument of equality-Expression
	@return
*/
equalityExpression returns [Expression expr = null] throws ParseException, DuplicateVariableException
{
	int offset = 0;
	int length = 0;
}	:
		{ offset = LT(1).getColumn()-1; }
		expr = relationalExpression
        { length = -offset + LT(0).getColumn()-1 + LT(0).getText().length(); }
        (
            expr = equalityExpression2[expr, offset, length]
		)?
	;
/** matches operator and 2nd argument of equality-Expression ( = , <>)
	@param arg1 first argument
	@param offsetArg1 offset of first argument
	@param lengthArg1 length of first argument
	@return or-expression (FunctionApplication)
*/
equalityExpression2[Expression arg1, int offsetArg1, int lengthArg1]
	returns [FunctionApplication functionAppl = null]
	throws ParseException, DuplicateVariableException
{
	Expression arg2 = null;
	FunctionId  op = null;
    String name = "nequals";
    int offsetOperator = 0;
    int offsetArg2 = 0;
    int lengthOperator = 0;
    int lengthArg2 = 0;
}
	:
		{ offsetOperator = LT(1).getColumn()-1; }
		(EQUAL { name = "equals"; }| NOT_EQUAL)
		{ lengthOperator = -offsetOperator + LT(0).getColumn()-1 + LT(0).getText().length();
		  offsetArg2 = LT(1).getColumn()-1;
		}
		arg2 = relationalExpression
		{
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
		(
            functionAppl = equalityExpression2[functionAppl, offsetArg1,
            			   -offsetArg1 + LT(0).getColumn()-1 + LT(0).getText().length()]
		)?
	;

/** matches first argument of relational-Expression
	@return
*/ 
relationalExpression returns [Expression expr = null] throws ParseException, DuplicateVariableException
{
	int offset = 0;
	int length = 0;
}	:
		{ offset = LT(1).getColumn()-1; }
		expr = additiveExpression
        { length = -offset + LT(0).getColumn()-1 + LT(0).getText().length(); }
        (   { isAdditiveExpression }?
		    (
                expr = relationalExpression2[expr, offset, length]
		    )
		    | /* empty */
		)
	;

/** matches operator and 2nd argument of relational-Expressions (<, <= , >, >= , =~)
	@param arg1 first argument
	@param offsetArg1 offset of first argument
	@param lengthArg1 length of first argument
	@return or-expression (FunctionApplication)
*/
relationalExpression2[Expression arg1, int offsetArg1, int lengthArg1]
	returns [FunctionApplication functionAppl = null]
	throws ParseException, DuplicateVariableException
{
	Expression arg2 = null;
	FunctionId op = null;
    String name = "reMatch";
        int offsetOperator = 0;
    int offsetArg2 = 0;
    int lengthOperator = 0;
    int lengthArg2 = 0;
}
	:
		{ offsetOperator = LT(1).getColumn()-1; }
		( 	L_T { name = "leThan"; }
        	| LE { name = "leEqual"; }
            | G_T  { name = "grThan"; }
            | GE { name = "grEqual"; }
            | MATCH )
		{ lengthOperator = -offsetOperator + LT(0).getColumn()-1 + LT(0).getText().length();
		  offsetArg2 = LT(1).getColumn()-1;
		}
		arg2 = additiveExpression
		{
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
		(   {isAdditiveExpression }?
		    (
			    functionAppl = relationalExpression2[functionAppl, offsetArg1,
            			   -offsetArg1 + LT(0).getColumn()-1 + LT(0).getText().length()]
		    )
		    |
		)
	;

/** matches first argument of additive Expression (+, -)
	@return
*/
additiveExpression returns [Expression expr = null] throws ParseException, DuplicateVariableException
{
	int offset = 0;
	int length = 0;
}	:
 		{ offset = LT(1).getColumn()-1; }
        expr = multiplicativeExpression
        { length = -offset + LT(0).getColumn()-1 + LT(0).getText().length(); }
        (
            expr = additiveExpression2[expr, offset, length]
		)?
	;

/** matches operator and 2nd argument of or- and orElse-Expressions
	@param arg1 first argument
	@param offsetArg1 offset of first argument
	@param lengthArg1 length of first argument
	@return or-expression (FunctionApplication)
*/
additiveExpression2[Expression arg1, int offsetArg1, int lengthArg1]
	returns [FunctionApplication functionAppl = null]
	throws ParseException, DuplicateVariableException
{
	Expression arg2 = null;
	FunctionId op = null;
    String name = "minus";
        int offsetOperator = 0;
    int offsetArg2 = 0;
    int lengthOperator = 0;
    int lengthArg2 = 0;
}
	:
		{ offsetOperator = LT(1).getColumn()-1; }
		(PLUS { name = "plus"; } | MINUS)
		{ lengthOperator = -offsetOperator + LT(0).getColumn()-1 + LT(0).getText().length();
		  offsetArg2 = LT(1).getColumn()-1;
		}
		arg2 = multiplicativeExpression
		{
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
		(
            functionAppl = additiveExpression2[functionAppl, offsetArg1,
            			   -offsetArg1 + LT(0).getColumn()-1 + LT(0).getText().length()]
		)?
	;

/** matches first argument of multiplicative-Expression
	@return
*/
multiplicativeExpression returns [Expression expr = null] throws ParseException, DuplicateVariableException
{
	int offset = 0;
	int length = 0;
}	:
		{ offset = LT(1).getColumn()-1; }
		expr = unaryExpression
        { length = -offset + LT(0).getColumn()-1 + LT(0).getText().length(); }
        (
            expr = multiplicativeExpression2[expr, offset, length]
		)?
	;

/** matches operator and 2nd argument of multiplicative-Expressions (*, /, %)
	@param arg1 first argument
	@param offsetArg1 offset of first argument
	@param lengthArg1 length of first argument
	@return or-expression (FunctionApplication)
*/
multiplicativeExpression2[Expression arg1, int offsetArg1, int lengthArg1]
	returns [FunctionApplication functionAppl = null] 
	throws ParseException, DuplicateVariableException
{
	Expression arg2 = null;
	FunctionId op = null;
    String name = "dividedBy";
    int offsetOperator = 0;
    int offsetArg2 = 0;
    int lengthOperator = 0;
    int lengthArg2 = 0;
}
	:
		{ offsetOperator = LT(1).getColumn()-1; }
		(STAR { name = "times"; }| MOD { name = "modulo"; }| DIV)
		{ lengthOperator = -offsetOperator + LT(0).getColumn()-1 + LT(0).getText().length();
		  offsetArg2 = LT(1).getColumn()-1;
		}
		arg2 = unaryExpression
        {
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
		(
            functionAppl = multiplicativeExpression2[functionAppl, offsetArg1,
            			   -offsetArg1 + LT(0).getColumn()-1 + LT(0).getText().length()]
		)?
	;

/** matches unary Expressions (-, not)
	@return
*/
unaryExpression returns [Expression expr = null] throws ParseException, DuplicateVariableException
{
	Expression arg = null;
	FunctionId unaryOp = null;
    boolean isUnaryExpr = false;
    int offsetOperator = 0;
    int offsetExpr = 0;
    int lengthOperator = 0;
    int lengthExpr = 0;
}	:
        (
			{ offsetOperator = LT(1).getColumn()-1; }
			unaryOp = unaryOperator
            {
            	lengthOperator = -offsetOperator + LT(0).getColumn()-1 + LT(0).getText().length();
            	isUnaryExpr = true;
            }
		)?
        { offsetExpr = LT(1).getColumn()-1; }
		expr = pathOrGrammarExpression
        {
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
	;

/** matches regular backwardvertex sets and pathsystem expressions beginning with
     a pathdescription
    @return
*/
regBackwardVertexSetOrPathSystem returns [Expression expr = null] throws ParseException, DuplicateVariableException
{
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
		{ offsetPathDescr = LT(1).getColumn()-1; }
		// the path description
        pathDescr = pathDescription
        { lengthPathDescr = -offsetPathDescr + LT(0).getColumn()-1 + LT(0).getText().length(); }
        /* is it a pathsystem ?*/
        (
        	{ offsetOperator = LT(1).getColumn()-1; }
            SMILEY
            {	isPathSystem = true;
            }
		)?
        { offsetExpr = LT(1).getColumn()-1; }
        // the target vertex
		restrExpr = restrictedExpression
		{
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
	;

/** matches expressions that compute regular paths or pathsystems starting with a
    vertex expression
    @param arg1 startvertex
    @param offsetArg1 offset of the startvertex-expression
    @param lengthArg1 length of the startvertex-expression
*/
regPathOrPathSystem[Expression arg1, int offsetArg1, int lengthArg1]
    returns [Expression expr = null]
    throws ParseException, DuplicateVariableException
{
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
			{ offsetOperator1 = LT(1).getColumn()-1; }
			SMILEY
            {
            	offsetPathDescr = LT(1).getColumn()-1;
            }
			pathDescr = pathDescription
			{ lengthPathDescr = -offsetPathDescr + LT(0).getColumn()-1 + LT(0).getText().length();}
            (
    			{ offsetOperator2 = LT(1).getColumn()-1; }
                SMILEY
      			{
            		offsetExpr = LT(1).getColumn()-1;
            	}
                restrExpr = restrictedExpression
                {
                	lengthExpr = -offsetPathDescr + LT(0).getColumn()-1 + LT(0).getText().length();
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

/** matches regular path-existences or regular forward-vertex-sets
	@param arg1 startvertex-expression
	@param offsetArg1 offset of the start-expression
	@param lengthArg1 length of the start-expression
	@return
*/
regPathExistenceOrForwardVertexSet[Expression arg1, int offsetArg1, int lengthArg1]
	returns [Expression expr = null]
	throws ParseException, DuplicateVariableException
{
	PathDescription pathDescr = null;
	Expression restrExpr = null;
	int offsetPathDescr = 0;
	int offsetExpr = 0;
	int lengthPathDescr = 0;
	int lengthExpr = 0;
}
	:
		{ offsetPathDescr = LT(1).getColumn()-1; }
		pathDescr = pathDescription
        { lengthPathDescr = -offsetPathDescr + LT(0).getColumn()-1 + LT(0).getText().length();}
		( 	(primaryExpression2) =>
        	{ offsetExpr = LT(1).getColumn()-1; }
        	restrExpr = restrictedExpression
            {
                lengthExpr = -offsetExpr + LT(0).getColumn()-1 + LT(0).getText().length();
            }
			| /* forward vertex set */
		)
		{
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
	;

/** matches contextfree path existences and contextfree forward-vertex-sets
	@param arg1 start-vertex-expression
	@param offsetArg1 offset of the start-expression
	@param lengthArg1 length of the start-expression
	@return
*/
cfPathExistenceOrForwardVertexSet[Expression arg1, int offsetArg1, int lengthArg1]
	returns [Expression expr = null]
	throws ParseException, DuplicateVariableException
{
	Expression grammar = null;
	Expression arg2 = null;
	int offsetGrammar = 0;
	int lengthGrammar = 0;
	int offsetArg2 = 0;
	int lengthArg2 = 0;
}
	:
 		{ offsetGrammar = LT(1).getColumn()-1; }
 		grammar = cfGrammar
 		{ lengthGrammar = -offsetGrammar + LT(0).getColumn()-1 + LT(0).getText().length(); }
 		( (restrictedExpression) =>
 		  { offsetArg2 = LT(1).getColumn()-1; }
 		  arg2 = restrictedExpression
 		  { lengthArg2 = -offsetArg2 + LT(0).getColumn()-1 + LT(0).getText().length(); }
 		|
 		)
 		{
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
	;

/** matches a contextfree path (':-)'-Operator)
	@param arg1 start-vertex expression
	@param offsetArg1 offset of start-expression
	@param lengthArg1 length of start-expression
	@return
*/
cfPath[Expression arg1, int offsetArg1, int lengthArg1]
	 returns [Expression expr = null]
	 throws ParseException, DuplicateVariableException
{
	Expression grammar = null;
	Expression arg2 = null;
	int offsetGrammar = 0;
	int offsetArg2 = 0;
	int lengthGrammar = 0;
	int lengthArg2 = 0;
	int offsetOp1 = 0;
	// int offsetOp2 = 0;
}
	:
		{ offsetOp1 = LT(1).getColumn()-1; }
		SMILEY
		{ offsetGrammar = LT(1).getColumn()-1; }
		grammar = cfGrammar
		{ lengthGrammar = -offsetGrammar + LT(0).getColumn()-1 + LT(0).getText().length();
		  // offsetOp2 = LT(1).getColumn()-1;
		}
		SMILEY
		{ offsetArg2 = LT(1).getColumn()-1; }
		arg2 = restrictedExpression
		{
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
	;

/** matches regular and context free forward- and backvertex sets or
    pathexistences
    @return
*/
pathOrGrammarExpression returns [Expression expr = null] throws ParseException, DuplicateVariableException
{
	Expression arg1 = null;
	Expression arg2 = null;
	Expression  p = null;
	int offsetArg1 = 0;
	int offsetArg2 = 0;
	int offsetPath = 0;
	int lengthArg1 = 0;
	int lengthArg2 = 0;
	int lengthPath = 0;
}
:
    (alternativePathDescription (SMILEY | restrictedExpression)) =>
	   /* pathDescr kann mit LPAREN beginnen, wie andere Expressions auch */
   	   /* matcht regBackwardVertexSetOrPathSystem, wenn der
	      Ausdruck mit altPathDescr beginnt und ein SMILEY oder eine
	      restrExpr folgt */
	expr = regBackwardVertexSetOrPathSystem
	| /* Ausdruck beginnt zwar mit altPathDescr, danach kommt
	     aber weder Smiley noch restrExpr --> matche also
	     pfadausdruck als primaryExpr (Knotenpaare)
	  */
	  (alternativePathDescription) =>expr = primaryExpression
    |  /* beginnt mit " < primary | " */
      (L_T primaryExpression HASH) =>
      /*  matche "< primary | primary |" */
	  (  L_T
	     { offsetArg1 = LT(1).getColumn()-1; }
	     arg1 = primaryExpression
	     { lengthArg1 = -offsetArg1 + LT(0).getColumn()-1 + LT(0).getText().length(); }
	     HASH
	     { offsetPath = LT(1).getColumn()-1; }
	     /* nur Pfadbeschreibungen, Grammatiken oder Variablen erlaubt */
	     ( p = pathDescription
	       |  (IDENT GASSIGN) => p = cfGrammar
	       | p = variable
	     )
		 HASH
	     { lengthPath = -offsetPath + LT(0).getColumn()-1 + LT(0).getText().length();  }
	     /* wenn "primary >" folgt, matche dies : */
		 (  (primaryExpression G_T) =>
		    (
		    	/* setze additiveExpression auf false, damit
		    	   das folgende "primary >" nicht als Vergleich
		    	   gematcht wird
		    	*/
		    	{
		    		isAdditiveExpression = false;
		    		offsetArg2 = LT(1).getColumn()-1;
		    	}
			    arg2 = primaryExpression G_T
			    { lengthArg2 = -offsetArg2 + LT(0).getColumn()-1 + LT(0).getText().length(); }
		     )
		     | /* empty */
		  )
		  {
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
		) /* beginnt mit " < primary | " */
	 |	HASH
	 	{ offsetPath = LT(1).getColumn()-1; }
	 	 /* nur Pfadbeschreibungen, Grammatiken oder Variablen erlaubt */
	 	( p = pathDescription
	 	  |  (IDENT GASSIGN) => p = cfGrammar
	 	  |   p = variable
	 	)
	 	{ lengthPath = -offsetPath + LT(0).getColumn()-1 + LT(0).getText().length(); }
	 	HASH
	    /* setze additiveExpression auf false, damit das folgende "primary >"
	       nicht als Vergleich gematcht wird
		*/
	    { isAdditiveExpression = false;
	      offsetArg1 = LT(1).getColumn()-1;
	    }
		  (
		  	arg1 = primaryExpression G_T
			{
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
		  )?
		  {
		  // reset isAdditiveExpression to true...
	    		isAdditiveExpression = true;
				if (arg1 == null) {	expr = p; }
		  }
	|
       { offsetArg1 = LT(1).getColumn()-1; }
	   expr = restrictedExpression
	   { lengthArg1 = -offsetArg1 + LT(0).getColumn()-1 + LT(0).getText().length(); }
	   (     (alternativePathDescription) =>
             expr = regPathExistenceOrForwardVertexSet[expr, offsetArg1, lengthArg1]
			 |
			   (cfGrammar) =>expr = cfPathExistenceOrForwardVertexSet[expr,offsetArg1,lengthArg1]
	         | (SMILEY) => expr = regPathOrPathSystem[expr, offsetArg1, lengthArg1]
             | // nix
      )
	;

/** matches restricted vertex expressions
    (and merges this-literals with the expression that is restricted)
    @return
*/
restrictedExpression returns [Expression expr = null] throws ParseException, DuplicateVariableException
{
	Expression restr = null;
    RestrictedExpression restrExpr = null;
    int offsetExpr = 0;
    int offsetRestr = 0;
    int lengthExpr = 0;
    int lengthRestr = 0;
}
	:
		{ offsetExpr = LT(1).getColumn()-1; }
		expr = valueAccess
		{ lengthExpr = -offsetExpr + LT(0).getColumn()-1 + LT(0).getText().length(); }
        (	// if followed by '&{' match this as part of this expr
        	(AMP LCURLY) =>
			(	AMP LCURLY
            	{ offsetRestr = LT(1).getColumn()-1; }
                restr = expression
 				{ lengthRestr = -offsetRestr + LT(0).getColumn()-1 + LT(0).getText().length(); }
                RCURLY
            	{
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
	                //	mergeRestrictedExpr(restr, expr);
						expr = restrExpr;
					} catch (Exception ex) { ex.printStackTrace(); }
                }
            )
			| /*empty*/
		)
	;

/** matches first argument of value-accesses
	@return vertex representing the value-Access-Expression
*/
valueAccess returns [Expression expr = null] throws ParseException, DuplicateVariableException
{
	int offset = 0;
	int length = 0;
}
	:
		{ offset = LT(1).getColumn()-1; }
        expr = primaryExpression
        { length = -offset + LT(0).getColumn()-1 + LT(0).getText().length(); }
		(
		    (LBRACK primaryPathDescription) => /*nothin*/ |
			(DOT | LBRACK) => expr = valueAccess2[expr, offset, length]
			| /* nothing */
		)
	;

/** matches operator and 2nd argument of valueAccess
	@param arg1 first argument-expression
	@param offsetArg1 offset of first argument-expression
	@param lengthArg1 length of first argument-expression
	@return vertex representing the value-access-expression
*/
valueAccess2[Expression arg1, int offsetArg1, int lengthArg1]
	returns [Expression expr = null]
	throws ParseException, DuplicateVariableException
{
	Expression arg2 = null;
	FunctionId  functionId = null;
    String name = "nthElement";
    int offsetArg2 = 0;
    int offsetOperator = 0;
    int lengthArg2 = 0;
    int lengthOperator = 0;
}
	:	{ offsetOperator = LT(1).getColumn()-1; }
    	((	DOT
       		{
       			lengthOperator = 1;
       			offsetArg2 = LT(1).getColumn()-1;
       		}
    		i:IDENT
            {
            	name = "getValue";
            	try
            	{
	            	arg2 = graph.createIdentifier();
	            	((Identifier)arg2).setName(i.getText());
	            	lengthArg2 = i.getText().length();
            	} catch(Exception ex){ex.printStackTrace();}
            }
        )
		| 	(	LBRACK
        		{ offsetArg2 = LT(1).getColumn()-1; }
				arg2 = expression
				{
            		name = "nthElement";
                	lengthArg2 = -offsetArg2 + LT(0).getColumn()-1 +LT(0).getText().length();
                }
                RBRACK
                {
                	lengthOperator = -offsetOperator + LT(0).getColumn()-1 + LT(0).getText().length();
                }
			)
            )
		{
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
		(	(LBRACK primaryPathDescription) => /*nothin*/ |
			(DOT|LBRACK) => expr = valueAccess2[expr, offsetArg1,
			 -offsetArg1 + LT(0).getColumn()-1 +LT(0).getText().length()]
			|
		)
	;

/**	@see primaryExpression2
*/
primaryExpression returns [Expression expr = null] throws ParseException, DuplicateVariableException
	:
    //	(primaryExpression2) =>
        expr = primaryExpression2
    //    | expr = pathDescription
 	;

/** matches primary expressions (elementset, literal,
	valueConstruction, functionAppl., subgraph, simpleQuery, cfGrammar, variable)
*/
primaryExpression2 returns [Expression expr = null] throws ParseException, DuplicateVariableException
	:
		( LPAREN expr = expression RPAREN )
		|	expr = rangeExpression
		|	expr = alternativePathDescription
		|	expr = literal
		|	expr = valueConstruction
		| 	expr = functionApplication
		| 	expr = graphRangeExpression
		|	expr = simpleQuery
		|(	(IDENT GASSIGN) =>
			expr = cfGrammar
		| 	expr = variable			)
	;

/** matches context free grammars
	@return
*/
cfGrammar returns [CfGrammar expr = null] throws ParseException, DuplicateVariableException
{
	Vector<VertexPosition> prodList = new Vector<VertexPosition>();
}
	:
		{ nonterminalSymbolTable.blockBegin(); }
		prodList = cfProductionList
		{
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
		{ nonterminalSymbolTable.blockEnd(); }
	;

/**	matches list of productions for contextfree grammars
*/
cfProductionList returns[Vector<VertexPosition> rules = new Vector<VertexPosition>();] throws ParseException, DuplicateVariableException
{
	Production production = null;
	Vector<VertexPosition> list = new Vector<VertexPosition>();
	VertexPosition p = new VertexPosition();
	int offset = 0;
	int length = 0;
}
	:
		{ offset = LT(1).getColumn()-1; }
		production = cfProduction
		{
			length = -offset + LT(0).getColumn()-1 + LT(0).getText().length();
			p.node = production;
			p.offset = offset;
			p.length = length;
			rules.add(p);
		}
		(	(IDENT GASSIGN) =>
			list = cfProductionList
			{
				rules.addAll(list);
			}
			|
		)
	;

/** matches a contextfree rule
*/
cfProduction returns [Production production = null] throws ParseException, DuplicateVariableException
{
	RightSide rightSide = null;
	IsRightSideOf rightSideOf = null;
	int offsetRightSide = 0;
	boolean isExcluded = false;
	boolean isType = false;
}
	:	// left side: Nonterminal
		i:IDENT
        {
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
        // ':: = '
		GASSIGN
		{
		  try
		  {
		  	  rightSide = graph.createRightSide();
			  rightSideOf = graph.createIsRightSideOf(rightSide, production);
			  offsetRightSide = LT(1).getColumn()-1;
			  rightSideOf.setSourcePositions((createSourcePositionList(1, offsetRightSide)));
		  }
		  catch (Exception ex) { ex.printStackTrace(); }
		}
		/*  recognise right side: */
		(  (CARET { isExcluded = true; })? j:IDENT (EXCL {isType = true;})?
		   {
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
		)*
		{
			rightSideOf.setSourcePositions((createSourcePositionList(-offsetRightSide + LT(0).getColumn()-1 + LT(0).getText().length(), offsetRightSide )));
		 }
		// alternative right sides:
		( 	BOR
    		{
               try
               {
              		rightSide = graph.createRightSide();
               		rightSideOf = graph.createIsRightSideOf(rightSide, production);
              		offsetRightSide = LT(1).getColumn()-1;
              		rightSideOf.setSourcePositions((createSourcePositionList(0, offsetRightSide )));
               }
               catch (Exception ex) { ex.printStackTrace(); }
    		}
    		(  (CARET { isExcluded = true; })? k:IDENT (EXCL {isType = true;})?
			    {
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
			)*
			{  rightSideOf.setSourcePositions((createSourcePositionList(-offsetRightSide + LT(0).getColumn()-1 + LT(0).getText().length(), offsetRightSide )));
				 }
		)*
		SEMI
	;

/** matches a pathdescription
*/
pathDescription returns [PathDescription pathDescr = null] throws ParseException, DuplicateVariableException
	:
        pathDescr = alternativePathDescription
	;

/** matches an alternative pathdescription
	@return
*/
alternativePathDescription returns [PathDescription pathDescr = null] throws ParseException, DuplicateVariableException
{
	int offsetPathDescr = 0;
	int lengthPathDescr = 0;
}	:
		{ offsetPathDescr = LT(1).getColumn()-1; }
        pathDescr = intermediateVertexPathDescription
        (	(BOR sequentialPathDescription) =>
			{
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
		    pathDescr = alternativePathDescription2[(AlternativePathDescription)pathDescr, offsetPathDescr, lengthPathDescr]
			| /*empty*/
		)
	;

/** matches 2nd part of an alternative pathdescription
	@param alt first alternative
	@param offsetAlt offset of the first alternative
	@param lenghtAlt lenght of the first alternative
	@return
*/
alternativePathDescription2[AlternativePathDescription alt, int offsetAlt1, int lengthAlt1]
	returns [AlternativePathDescription pathDescr = null] throws ParseException, DuplicateVariableException
{
	PathDescription alt2 = null;
	int offsetAlt2 = 0;
	int lengthAlt2 = 0;
}
	:
	    BOR
	    { offsetAlt2 = LT(1).getColumn()-1; }
        alt2 = intermediateVertexPathDescription
        {
 			lengthAlt2 = -offsetAlt2 + LT(0).getColumn()-1 + LT(0).getText().length();
			try
 			{
	            IsAlternativePathOf alt2Of = graph.createIsAlternativePathOf(alt2, alt);
	            alt2Of.setSourcePositions((createSourcePositionList(lengthAlt2, offsetAlt2 )));
				pathDescr = alt;
			}
			catch (Exception ex) { ex.printStackTrace(); }
        }
		(
			(BOR sequentialPathDescription) =>
			pathDescr = alternativePathDescription2[(AlternativePathDescription)pathDescr,
				offsetAlt1, -offsetAlt1 + LT(0).getColumn()-1 + LT(0).getText().length()]
			| /*empty*/
		)
	;

/** matches a pathdescription with intermediate vertex
*/
intermediateVertexPathDescription returns [PathDescription pathDescr = null] throws ParseException, DuplicateVariableException
{
	int offset = 0;
	int length = 0;
}	:
        { offset = LT(1).getColumn()-1; }
        pathDescr = sequentialPathDescription
        { length = -offset  + LT(0).getColumn()-1 +LT(0).getText().length(); }
		(	(restrictedExpression sequentialPathDescription) =>
			pathDescr = intermediateVertexPathDescription2[pathDescr, offset, length]
			| /*empty*/
		)
	;

/** matches 2nd part of a  pathdescription with an intermediate vertex
	@param subPath1 pathdescription preceding the intermediate vertex
	@param offsetSub1 offset of subPath1
	@param lengthSub1 length of subPath1
	@return
*/
intermediateVertexPathDescription2[PathDescription subPath1, int offsetSub1, int lengthSub1]
	returns [PathDescription pathDescr = null]
	throws ParseException, DuplicateVariableException
{
	Expression restrExpr = null;
	PathDescription subPath2 = null;
	int offsetExpr = 0;
	int offsetSub2 = 0;
	int lengthExpr = 0;
	int lengthSub2 = 0;
}
	:
		{ offsetExpr = LT(1).getColumn()-1; }
		restrExpr = restrictedExpression
        {
        	lengthExpr = -offsetExpr + LT(0).getColumn()-1 + LT(0).getText().length();
        	offsetSub2 = LT(1).getColumn()-1;
        }
		subPath2 = sequentialPathDescription
        {
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
		(	(restrictedExpression sequentialPathDescription) =>
            	pathDescr = intermediateVertexPathDescription2[pathDescr, offsetSub1,
            					-offsetSub1 + LT(0).getColumn()-1 +LT(0).getText().length()]
			| /*empty*/
		)
	;

/** matches a sequential pathdescription
	@return
*/
sequentialPathDescription returns [PathDescription pathDescr = null] throws ParseException, DuplicateVariableException
{
	int offset = 0;
	int length = 0;
	SequentialPathDescription seqPathDescr = null;
}	:
        { offset = LT(1).getColumn()-1; }
        pathDescr = startRestrictedPathDescription
        { length = -offset + LT(0).getColumn()-1 + LT(0).getText().length(); }
        (
			(startRestrictedPathDescription) =>
			{
				try
				{
					seqPathDescr = graph.createSequentialPathDescription();
				    IsSequenceElementOf sequenceElementOf = graph.createIsSequenceElementOf(pathDescr, seqPathDescr);
				    sequenceElementOf.setSourcePositions((createSourcePositionList(length, offset )));
				}
				catch (Exception ex) { ex.printStackTrace(); }
			}
			pathDescr = sequentialPathDescription2[seqPathDescr, offset, length]
			| /*empty*/
		)
	;

/** matches 2nd part of a sequential pathdescription
	@return
*/
sequentialPathDescription2[SequentialPathDescription seqPathDescr, int offsetSeq1, int lengthSeq1]
	returns [SequentialPathDescription pathDescr = null]
	throws ParseException, DuplicateVariableException
{
	PathDescription seq2;
	int offsetSeq2 = 0;
	int lengthSeq2 = 0;
}
	:
		{ offsetSeq2 = LT(1).getColumn()-1; }
		seq2 = startRestrictedPathDescription
        {
        	lengthSeq2 = -offsetSeq2 + LT(0).getColumn()-1 + LT(0).getText().length();
        	try
        	{
	        	IsSequenceElementOf sequenceElementOf = graph.createIsSequenceElementOf(seq2, seqPathDescr);
	        	sequenceElementOf.setSourcePositions((createSourcePositionList(lengthSeq2, offsetSeq2 )));
				pathDescr = seqPathDescr;
			}
			catch (Exception ex) { ex.printStackTrace(); }
        }
		(
			(iteratedOrTransposedPathDescription) =>
			pathDescr = sequentialPathDescription2[seqPathDescr, offsetSeq1,
					-offsetSeq1 +LT(0).getColumn()-1 + LT(0).getText().length()]
			| /*empty*/
		)
	;

/** matches a pathdescription with startrestriction
	@return
*/
startRestrictedPathDescription returns [PathDescription pathDescr = null] throws ParseException, DuplicateVariableException
{
	Vector<VertexPosition> typeIds = new Vector<VertexPosition>();
	Expression expr = null;
	int offset = 0;
	int length = 0;
}	:
    	( LCURLY
		  ( (typeId) =>typeIds = typeExpressionList
		    | 	{ offset = LT(1).getColumn()-1; }
		    	expr = expression
		    	{ length = -offset + LT(0).getColumn()-1 +LT(0).getText().length(); }
		  )
		  RCURLY
		  AMP
		)?
        pathDescr = goalRestrictedPathDescription
		{
            try
            {
	            if (expr != null)
				{
				  		IsStartRestrOf startRestrOf = graph.createIsStartRestrOf(expr, pathDescr);
						startRestrOf.setSourcePositions((createSourcePositionList(length, offset)));
				//		mergeRestrictedExpr(expr, pathDescr);
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
	;

/** matches a pathdescription with goalrestriction
	@return
*/
goalRestrictedPathDescription returns [PathDescription pathDescr = null] throws ParseException, DuplicateVariableException
{
	Vector<VertexPosition> typeIds = new Vector<VertexPosition>();
	Expression expr = null;
	int offset = 0;
	int length = 0;
}	:
        pathDescr = iteratedOrTransposedPathDescription
		( 	(AMP) =>
			(	AMP
				LCURLY
				( (typeId) =>typeIds = typeExpressionList
				{
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
				| (	{ offset = LT(1).getColumn()-1; }
					expr = expression
			  		{
			  	 		length = -offset + LT(0).getColumn()-1 +LT(0).getText().length();
    			  		try
    			  		{
    			  			IsGoalRestrOf goalRestrOf = graph.createIsGoalRestrOf(expr, pathDescr);
    			  			goalRestrOf.setSourcePositions((createSourcePositionList(length, offset)));
    			  //			mergeRestrictedExpr(expr, pathDescr);
    			  		}
    			  		catch (Exception ex) { ex.printStackTrace(); }
			  		}
			  	   ) // ende expr
				)// ende nachLcurly
			RCURLY
			)|  /* empty (no AMP)*/
		)
	;

/** matches an iterated (+/*), exponentiated (^Integer)
	or transposed (^T) pathdescription
	@return
*/
iteratedOrTransposedPathDescription returns [PathDescription pathDescr = null] throws ParseException, DuplicateVariableException
{
	int offset = 0;
	int length = 0;
}	:
		{ offset = LT(1).getColumn()-1;}
		pathDescr = primaryPathDescription
		{ length = -offset + LT(0).getColumn()-1 +LT(0).getText().length(); }
        (
			(	STAR | PLUS | CARET ) =>
            	pathDescr = iteratedOrTransposedPathDescription2[pathDescr, offset, length]
			| /*empty*/
		)
	;

/** matches 2nd part of an iterated (+/*), exponentiated (^Integer)
	or transposed (^T) pathdescription
	@return
*/
iteratedOrTransposedPathDescription2[PathDescription path, int offsetPath, int lengthPath]
	returns [PathDescription pathDescr = null]
	throws ParseException, DuplicateVariableException
{
   	String times = "plus";
 	int offsetExpr = 0;
 	int lengthExpr = 0;
}
	:
		(	// iteratedPathDescr:
        	( STAR { times = "star"; } | PLUS )
        	{
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
			|	( CARET
            	  // transponatedPath:
				  (	T
                  	{
                  		try
                  		{
							TransposedPathDescription tpd = graph.createTransposedPathDescription();
							pathDescr = tpd;
	                    	IsTransposedPathOf transposedPathOf = graph.createIsTransposedPathOf(path, tpd);
	                    	transposedPathOf.setSourcePositions((createSourcePositionList(lengthPath, offsetPath)));
						}
						catch (Exception ex) { ex.printStackTrace(); }
                    }
                  | // exponentedPath:
                    ( //LCURLY
                      { offsetExpr = LT(1).getColumn()-1; }
                      //expr = expression
                      i:NUM_INT
                      { lengthExpr = -offsetExpr + LT(0).getColumn()-1 +LT(0).getText().length(); }
                      //RCURLY
                      {
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
                    )
				  )
				 )
		)
		(	(STAR | PLUS | CARET ) =>
        	pathDescr = iteratedOrTransposedPathDescription2[pathDescr,
        			offsetPath, -offsetPath + LT(0).getColumn()-1 + LT(0).getText().length()]
			| /*empty*/
		)
	;

/** matches a primary pathdescription, i.e. one of<br>
	- simple pathdescription
	- edge-pathdescription
	- pathdescription in parenthesis
	- optional pathdescription
	@return
*/
primaryPathDescription returns [PathDescription pathDescr = null] throws ParseException, DuplicateVariableException
{
	int offset = 0;
	int length = 0;
}	:
		(
          (		pathDescr = simplePathDescription
            	|	pathDescr = edgePathDescription
          )
        )
		|	( LPAREN  pathDescr = pathDescription RPAREN )
		|	( LBRACK
        	  { offset = LT(1).getColumn()-1; }
              pathDescr = pathDescription
              { length = -offset + LT(0).getColumn()-1 + LT(0).getText().length(); }
              RBRACK
              {
              	try
              	{
	              	OptionalPathDescription optPathDescr = graph.createOptionalPathDescription();
					IsOptionalPathOf optionalPathOf = graph.createIsOptionalPathOf(pathDescr, optPathDescr);
					optionalPathOf.setSourcePositions((createSourcePositionList(length, offset)));
	                pathDescr = optPathDescr;
                }
                catch (Exception ex) { ex.printStackTrace(); }
              }
            )
	;

/** matches a simle pathdescription consisting of an arrow simple
	and eventually a restriction. "thisEdge"s are replaced by
	the corresponding simple pathdescription
	@return
*/
simplePathDescription returns [PrimaryPathDescription pathDescr = null] throws ParseException, DuplicateVariableException
{
	Vector<VertexPosition> typeIds = new Vector<VertexPosition>();
    Direction dir;
    String direction = "any";
    int offsetDir = 0;
}
	:
    	{
          offsetDir = LT(1).getColumn()-1;
        }
		(	RARROW { direction = "out"; }
			| LARROW { direction = "in"; }
			| ARROW
		)
		(   (LCURLY (edgeRestrictionList)? RCURLY ) =>
        	(	LCURLY
				(typeIds = edgeRestrictionList)?
				RCURLY
			)
			| /* empty */
        )
        {
        	try
        	{
	        	pathDescr = graph.createSimplePathDescription();
	        	VertexClass directionVertexClass = (VertexClass) graphClass.getGraphElementClass(new QualifiedName("Direction"));
	        	dir = (Direction)graph.getFirstVertexOfClass(directionVertexClass);
	        	while (dir != null ) //
	        	{
	        		if (! dir.getDirValue().equals(direction))
	        		{
	        			dir = (Direction)graph.getNextVertexOfClass(dir, directionVertexClass);
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
	;

/** matches a edgePathDescription, i.e. am edge as part of a pathdescription
	@return
*/
edgePathDescription returns [EdgePathDescription pathDescr = null] throws ParseException, DuplicateVariableException
{
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
	:	{
    		offsetDir = LT(1).getColumn()-1;
    	}
		(	EDGESTART	{ edgeStart = true; }
			| EDGE
		)
        {
    		offsetExpr = LT(1).getColumn()-1;
    	}
		expr = expression
        {
    		lengthExpr = -offsetExpr + LT(0).getColumn()-1 + LT(0).getText().length();
    	}
		(	EDGEEND { edgeEnd = true; }
			| EDGE
		)
        {
        	lengthExpr = -offsetDir + LT(0).getColumn()-1 + LT(0).getText().length();
        	try
        	{
	        	pathDescr = graph.createEdgePathDescription();
	            if ((edgeStart && !edgeEnd) || (!edgeStart  && edgeEnd))
	            	if (edgeStart) direction = "in";
	            	else direction = "out";
	            VertexClass directionVertexClass = (VertexClass) graphClass.getGraphElementClass(new QualifiedName("Direction"));
	        	dir = (Direction)graph.getFirstVertexOfClass(directionVertexClass);
	        	while (dir != null )
	        	{
	        		if (! dir.getDirValue().equals(direction))
	        		{
	        			dir = (Direction)graph.getNextVertexOfClass(dir, directionVertexClass);
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
	;

/** matches a attributeId
	@return
*/
attributeId returns [AttributeId expr = null] throws ParseException, DuplicateVariableException
	:
		i:IDENT
        {
	       	expr = graph.createAttributeId();
	       	expr.setName(i.getText());
        }
	;

/** matches a list of definitions for let- or where expressions
	@return
*/
definitionList returns [Vector<VertexPosition> definitions = new Vector<VertexPosition>();] throws ParseException, DuplicateVariableException
{
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
definition returns [Definition definition = null] throws ParseException, DuplicateVariableException
{
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
        (
            expr = expressionOrPathDescription)
        {
        	lengthExpr = -offsetExpr + LT(0).getColumn()-1 +LT(0).getText().length();
            definition = graph.createDefinition();
            IsVarOf varOf = graph.createIsVarOf(var, definition);
            varOf.setSourcePositions((createSourcePositionList(lengthVar, offsetVar)));
            IsExprOf exprOf = graph.createIsExprOf(expr, definition);
            exprOf.setSourcePositions((createSourcePositionList(lengthExpr, offsetExpr)));
        }
	;

/**	matches a function application
*/
functionApplication returns [FunctionApplication expr = null] throws ParseException, DuplicateVariableException
{
	Vector<VertexPosition> typeIds = new Vector<VertexPosition>();
	Vector<VertexPosition> expressions = new Vector<VertexPosition>();
    FunctionId functionId = null;
}
	:
		f:FUNCTIONID
        (
			LCURLY
			(typeIds = typeExpressionList)?
			RCURLY
		)?
		LPAREN
		(expressions = expressionList)?
		RPAREN
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
valueConstruction returns [Expression expr = null] throws ParseException, DuplicateVariableException
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
bagConstruction returns [BagConstruction bagConstr = null] throws ParseException, DuplicateVariableException
{
	Vector<VertexPosition> expressions = new Vector<VertexPosition>();
}
	:
		BAG
		LPAREN
		(
			expressions = expressionList
		)?
		RPAREN
        {
        	bagConstr = graph.createBagConstruction();
        	for (int i = 0; i < expressions.size(); i++) {
				VertexPosition expr = expressions.get(i);
				IsPartOf exprOf = graph.createIsPartOf((Expression)expr.node, bagConstr);
				exprOf.setSourcePositions((createSourcePositionList(expr.length, expr.offset)));
			}
        }
	;

/**	matches a set construction
*/
setConstruction returns [SetConstruction setConstr = null] throws ParseException, DuplicateVariableException
{
	Vector<VertexPosition> expressions = new Vector<VertexPosition>();
}
	:
		SET
		LPAREN
		(
			expressions = expressionList
		)?
		RPAREN
        {
        	setConstr = graph.createSetConstruction();
            for (int i = 0; i < expressions.size(); i++) {
				VertexPosition expr = expressions.get(i);
				IsPartOf exprOf = graph.createIsPartOf((Expression)expr.node, setConstr);
				exprOf.setSourcePositions((createSourcePositionList(expr.length, expr.offset)));
			}
        }
	;

/** matches a listrange expression: integer-expression .. integer-expression
	@return
*/
listRangeExpression returns [ListRangeConstruction expr = null] throws ParseException, DuplicateVariableException
{
	Expression startExpr = null;
	Expression endExpr = null;
 	int offsetStart = 0;
 	int offsetEnd = 0;
 	int lengthStart = 0;
 	int lengthEnd = 0;
}
	:
		{ offsetStart = LT(1).getColumn()-1; }
    	startExpr = expression
    	{ lengthStart = -offsetStart + LT(0).getColumn()-1 +LT(0).getText().length(); }
        DOTDOT
        { offsetEnd = LT(1).getColumn()-1; }
        endExpr = expression
        {
        	lengthEnd = -offsetEnd + LT(0).getColumn()-1 +LT(0).getText().length();
           	// expr = graph.createListRangeExpression();
	        expr = graph.createListRangeConstruction();
            IsFirstValueOf firstValueOf = graph.createIsFirstValueOf(startExpr, expr);
            firstValueOf.setSourcePositions((createSourcePositionList(lengthStart, offsetStart)));
        	IsLastValueOf lastValueOf = graph.createIsLastValueOf(endExpr, expr);
        	lastValueOf.setSourcePositions((createSourcePositionList(lengthEnd, offsetEnd)));
        }
	;

/** matches a list construction
*/
listConstruction returns [ListConstruction listConstr = null] throws ParseException, DuplicateVariableException
{
	Vector<VertexPosition> expressions = new Vector<VertexPosition>();
    // int offset = 0;
}
	:
		LIST
		LPAREN
        (
        	(expression DOTDOT) =>
        	{
				// offset = LT(1).getColumn()-1;
			}
        	listConstr = listRangeExpression
        	|
			(
				expressions = expressionList
        	    {
       	       		listConstr = graph.createListConstruction();
        	        for (int i = 0; i < expressions.size(); i++) {
						VertexPosition expr = expressions.get(i);
						IsPartOf exprOf = graph.createIsPartOf((Expression)expr.node, listConstr);
						exprOf.setSourcePositions((createSourcePositionList(expr.length, expr.offset)));
					}
	     	   }
			)?
        )
		RPAREN
	;

/** matches a tupel construction
*/
tupleConstruction returns [TupleConstruction tupConstr = null] throws ParseException, DuplicateVariableException
{
	Vector<VertexPosition> expressions = new Vector<VertexPosition>();
}	:
		TUP
		LPAREN
		expressions = expressionList
		RPAREN
        {
        	tupConstr = graph.createTupleConstruction();
           	for (int i = 0; i < expressions.size(); i++) {
				VertexPosition expr = expressions.get(i);
				IsPartOf exprOf = graph.createIsPartOf((Expression)expr.node, tupConstr);
				exprOf.setSourcePositions((createSourcePositionList(expr.length, expr.offset)));
			}
        }
 	;

/** matches a record construction
*/
recordConstruction returns [RecordConstruction recConstr = null] throws ParseException, DuplicateVariableException
{
	Vector<VertexPosition> elements = new Vector<VertexPosition>();
}
	:
		REC
		LPAREN
		elements = recordElementList
		RPAREN
        {
		    recConstr = graph.createRecordConstruction();
			for (int i = 0; i < elements.size(); i++) {
				VertexPosition expr = elements.get(i);
				IsRecordElementOf exprOf = graph.createIsRecordElementOf((RecordElement)expr.node, recConstr);
				exprOf.setSourcePositions((createSourcePositionList(expr.length, expr.offset)));
			}
        }
	;

/** matches a list of record-elements
*/
recordElementList returns [Vector<VertexPosition> elements = new Vector<VertexPosition>();] throws ParseException, DuplicateVariableException
{
	RecordElement v = null;
	Vector<VertexPosition> list = null;
    VertexPosition recElement = new VertexPosition();
    int offset = 0;
    int length = 0;
}
	:   { offset = LT(1).getColumn()-1; }
		v = recordElement
        {
        	length = -offset + LT(0).getColumn()-1 +LT(0).getText().length();
            recElement.node = v;
            recElement.offset = offset;
            recElement.length = length;
        	elements.add(recElement);
        }
		(	COMMA list = recordElementList {elements.addAll(list);} )?
	;

/** matches a record element consisting of an id, a colon and an expression
*/
recordElement returns [RecordElement recElement = null] throws ParseException, DuplicateVariableException
{
	RecordId recId = null;
	Expression expr = null;
    int offsetRecId = 0;
    int offsetExpr = 0;
    int lengthRecId = 0;
    int lengthExpr = 0;
}	:
		{ offsetRecId = LT(1).getColumn()-1; }
		recId = recordId
        { lengthRecId = -offsetRecId + LT(0).getColumn()-1 +LT(0).getText().length(); }
		COLON
        { offsetExpr = LT(1).getColumn()-1; }
		expr = expression
        {
        	lengthExpr = -offsetExpr + LT(0).getColumn()-1 +LT(0).getText().length();
            recElement = graph.createRecordElement();
        	IsRecordIdOf recIdOf = graph.createIsRecordIdOf(recId, recElement);
        	recIdOf.setSourcePositions((createSourcePositionList(lengthRecId, offsetRecId)));
            IsRecordExprOf  exprOf = graph.createIsRecordExprOf(expr, recElement);
            exprOf.setSourcePositions((createSourcePositionList(lengthExpr, offsetExpr)));
        }
	;

/** matches a record-id
*/
recordId returns [RecordId expr = null] throws ParseException, DuplicateVariableException
	:
		i:IDENT
        {
        	expr = graph.createRecordId();
        	expr.setName(i.getText());
        }
	;

/** matches a path construction
*/
pathConstruction returns [PathConstruction pathConstr = null] throws ParseException, DuplicateVariableException
{
	Vector<VertexPosition> expressions = new Vector<VertexPosition>();
}
	:
		PATH
		LPAREN
		expressions = expressionList
		RPAREN
        {
		    pathConstr = graph.createPathConstruction();
		    for (int i = 0; i < expressions.size(); i++) {
				VertexPosition expr = expressions.get(i);
				IsPartOf exprOf = graph.createIsPartOf((Expression)expr.node, pathConstr);
				exprOf.setSourcePositions((createSourcePositionList(expr.length, expr.offset)));
			}
        }
	;

/** matches a pathsystem construction
*/
pathsystemConstruction returns [PathSystemConstruction pathsystemConstr = null] 
throws ParseException, DuplicateVariableException
{
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
        { offsetExpr = LT(1).getColumn()-1; }
		expr = expression
        {
        	lengthExpr = -offsetExpr + LT(0).getColumn()-1 +LT(0).getText().length();
    		pathsystemConstr = graph.createPathSystemConstruction();
	       	IsRootOf rootOf = graph.createIsRootOf(expr, pathsystemConstr);
	       	rootOf.setSourcePositions((createSourcePositionList(lengthExpr, offsetExpr)));
        }
		(	COMMA
        	{ offsetEVList = LT(1).getColumn()-1; }
			eVList = edgeVertexList
            {
            	lengthEVList = -offsetEVList + LT(0).getColumn()-1 +LT(0).getText().length();
               	IsEdgeVertexListOf exprOf = graph.createIsEdgeVertexListOf(eVList, pathsystemConstr);
               	exprOf.setSourcePositions((createSourcePositionList(lengthEVList, offsetEVList)));
            }
		)*
		RPAREN
	;

/** matches a quantified declaration which contains
	simple declarations and boolean expressions,
	each of them separated by  ','.
*/
quantifiedDeclaration returns [Declaration declaration = null] throws ParseException, DuplicateVariableException
{
	Expression subgraphExpr = null;
	Expression constraintExpr = null;
    Vector<VertexPosition> declarations = new Vector<VertexPosition>();
    int offsetConstraint = 0;
    int offsetSubgraph = 0;
    int lengthConstraint = 0;
    int lengthSubgraph = 0;
}
	:
		declarations = declarationList
        {
		    declaration = graph.createDeclaration();
		    for (int i = 0; i < declarations.size(); i++) {
				VertexPosition decl = declarations.get(i);
				IsSimpleDeclOf simpleDeclOf = graph.createIsSimpleDeclOf((SimpleDeclaration)decl.node, declaration);
				simpleDeclOf.setSourcePositions((createSourcePositionList(decl.length, decl.offset)));
			}
        }
		(  	COMMA
			{ offsetConstraint = LT(1).getColumn()-1; }
			constraintExpr = expression
		   	{
		   		lengthConstraint = -offsetConstraint + LT(0).getColumn()-1 +LT(0).getText().length();
	   			IsConstraintOf constraintOf = graph.createIsConstraintOf(constraintExpr,declaration);
		   		constraintOf.setSourcePositions((createSourcePositionList(lengthConstraint, offsetConstraint)));
			}
		   (
			 (COMMA simpleDeclaration) =>
			 (COMMA declarations = declarationList
			 {
		    	   for (int i = 0; i < declarations.size(); i++) {
						VertexPosition decl = declarations.get(i);
						IsSimpleDeclOf simpleDeclOf = graph.createIsSimpleDeclOf((SimpleDeclaration)decl.node, declaration);
						simpleDeclOf.setSourcePositions((createSourcePositionList(decl.length, decl.offset)));
				   }
			 }
			 )
		   | /* empty */
		   )
		)*
		(	IN
			{ offsetSubgraph = LT(1).getColumn()-1; }
			subgraphExpr = expression
            {
            	lengthSubgraph = -offsetSubgraph + LT(0).getColumn()-1 +LT(0).getText().length();
           	 	IsSubgraphOf subgraphOf = graph.createIsSubgraphOf(subgraphExpr, declaration);
             	subgraphOf.setSourcePositions((createSourcePositionList(lengthSubgraph, offsetSubgraph)));
            }
		)?
	;

/** matches a comma-seperated list of simple declarations
*/
declarationList returns [Vector<VertexPosition> declList = new Vector<VertexPosition>();] throws ParseException, DuplicateVariableException
{
	SimpleDeclaration v = null;
    VertexPosition simpleDecl = new VertexPosition();
    int offset = 0;
    int length = 0;
}
	:
		{ offset = LT(1).getColumn()-1; }
		v = simpleDeclaration
        {
        	length = -offset + LT(0).getColumn()-1 +LT(0).getText().length();
        	simpleDecl.node = v;
            simpleDecl.offset = offset;
            simpleDecl.length = length;
        	declList.add(simpleDecl);
        }
		( (COMMA simpleDeclaration) => declList = declarationList2[declList]
		  | /* empty */
		)
	;

/**
*/
declarationList2[Vector<VertexPosition> list] returns [Vector<VertexPosition> declList = new Vector<VertexPosition>();] throws ParseException, DuplicateVariableException
{
	SimpleDeclaration v = null;
	VertexPosition simpleDecl = new VertexPosition();
	 int offset = 0;
    int length = 0;
}	:
	{
		   declList.addAll(list);
	}
	COMMA
	{ offset = LT(1).getColumn()-1; }
	v = simpleDeclaration
    {
    	length = -offset + LT(0).getColumn()-1 +LT(0).getText().length();
        simpleDecl.node = v;
        simpleDecl.offset = offset;
        simpleDecl.length = length;
		declList.add(simpleDecl);
	}
	( (COMMA simpleDeclaration) =>(declList = declarationList2[declList])
	 | /* empty */
	)
	;

/** matches a simple declaration: variablelist ':' set-expression
*/
simpleDeclaration returns [SimpleDeclaration simpleDecl = null] throws ParseException, DuplicateVariableException
{
	Expression expr = null;
    Vector<VertexPosition> variables = new Vector<VertexPosition>();
    int offset = 0;
    int length = 0;
}
	:
		variables = variableList
		COLON
        { offset = LT(1).getColumn()-1; }
		expr = expression
        {
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
	;

/** matches a quantifier
*/
quantifier returns [Quantifier expr = null] throws ParseException, DuplicateVariableException
{
	String name = "";
}
	:
        (
			FORALL {name = "forall";}
 			|(	(EXISTS EXCL) => (EXISTS EXCL {name = "exists!";})
 			|	EXISTS {name = "exists";})
		)
        {
            expr = graph.getFirstQuantifier();
            while (expr != null) {
            	if (expr.getName().equals(name)) return expr;
            	expr = expr.getNextQuantifier();
            }
	        expr = graph.createQuantifier();
	        expr.setName(name);
        }
    ;

/**	matches an element-set-expression: (E|V) [{typeExpressionList}]
*/
rangeExpression returns [Expression expr = null] throws ParseException, DuplicateVariableException
{
	Vector<VertexPosition> typeIds = new Vector<VertexPosition>();
}
	:
		(
		  V {expr = graph.createVertexSetExpression();}
        | E { expr = graph.createEdgeSetExpression(); }
        )
        	(  (LCURLY (typeExpressionList)? RCURLY ) =>
        		(LCURLY
				(typeIds = typeExpressionList)?
				RCURLY)
            |
            )
            {
        	    for (int i = 0; i < typeIds.size(); i++) {
					VertexPosition t = typeIds.get(i);
					IsTypeRestrOf typeRestrOf = graph.createIsTypeRestrOf((TypeId)t.node, expr);
					typeRestrOf.setSourcePositions((createSourcePositionList(t.length, t.offset)));
				}
            }
	;

/** matches a subgraph expression
	@return
*/
graphRangeExpression returns [Expression expr = null] throws ParseException, DuplicateVariableException
{
	Vector<VertexPosition> typeIds = new Vector<VertexPosition>();
}
	:
		(	VSUBGRAPH
			{ 
			  expr = graph.createVertexSubgraphExpression(); 
			}
			|	ESUBGRAPH
				{ 
				  expr = graph.createEdgeSubgraphExpression();
				}
		)
		LCURLY
		typeIds = typeExpressionList
		RCURLY
        {
        	for (int i = 0; i < typeIds.size(); i++) {
				VertexPosition t = typeIds.get(i);
				IsTypeRestrOf typeRestrOf = graph.createIsTypeRestrOf((TypeId)t.node, expr);
				typeRestrOf.setSourcePositions((createSourcePositionList(t.length, t.offset)));
			}
        }
	;

/** matches a  fwr-expression
*/
simpleQuery returns [Comprehension comprehension = null] throws ParseException, DuplicateVariableException, ParseException
{
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
}
	:
		// declaration part
		FROM
		declarations = declarationList
        {
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
        // optional subgraph-clause
		(	IN
			{ offsetSubgraph = LT(1).getColumn()-1; }
			subgraphExpr = expression
            {
	            lengthSubgraph = -offsetSubgraph + LT(0).getColumn()-1 +LT(0).getText().length();
	           	lengthDecl += lengthSubgraph;
	           	IsSubgraphOf subgraphOf = graph.createIsSubgraphOf(subgraphExpr, declaration);
	           	subgraphOf.setSourcePositions((createSourcePositionList(lengthSubgraph, offsetSubgraph)));
			}
		)?
		// optional predicate
		(	WITH
			{ offsetConstraint = LT(1).getColumn()-1; }
			constraintExpr = expression
            {
            	lengthConstraint = -offsetConstraint + LT(0).getColumn()-1 +LT(0).getText().length();
	           	lengthDecl += lengthConstraint;
	           	IsConstraintOf  constraintOf = graph.createIsConstraintOf(constraintExpr, declaration);
	          	constraintOf.setSourcePositions((createSourcePositionList(lengthConstraint, offsetConstraint)));
			}
		)?
		// report-clause
		{
			// offsetResult = LT(1).getColumn()-1;
		}
		comprehension = reportClause
        {
		   // lengthResult = -offsetResult + LT(0).getColumn()-1 +LT(0).getText().length();
	   		IsCompDeclOf comprDeclOf = graph.createIsCompDeclOf(declaration, comprehension);
	   		comprDeclOf.setSourcePositions((createSourcePositionList(lengthDecl, offsetDecl)));
		}
		END
	;

/**	returns a comprehension including the comprehension result
*/
reportClause returns [Comprehension comprehension = null] throws ParseException, DuplicateVariableException, ParseException
{
	Vector<VertexPosition> reportList = new Vector<VertexPosition>();
	int offset = 0;
	int length = 0;
	boolean vartable = false;
}
	:
		(	REPORT
			comprehension = labeledReportList
		)
		|
		(	(REPORTBAG { try {comprehension = graph.createBagComprehension(); } catch (Exception ex) { ex.printStackTrace(); } }
			| REPORTSET { try { comprehension = graph.createSetComprehension(); } catch (Exception ex) { ex.printStackTrace(); } }
			|  REPORTTABLE { try { comprehension = graph.createTableComprehension(); vartable = true; }	catch (Exception ex) { ex.printStackTrace(); } }
			)
            { offset = LT(1).getColumn()-1; }
			reportList = expressionList
            {
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
		)
	;

/** matches a report-list with labels
	@return Bag-Comprehension-Vertex with <br>
	a) a TupelConstruction as result or <br>
	b) the expression as result (if the reportlist has only one element)
*/
labeledReportList returns [BagComprehension bagCompr = null] throws ParseException, DuplicateVariableException
{
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
}
	:
    	{ offsetExpr = LT(1).getColumn()-1;
    	  offset = offsetExpr;
    	}
		expr = expression
 		{ lengthExpr = -offsetExpr + LT(0).getColumn()-1 + LT(0).getText().length(); }
        (	AS
       		{ offsetAsExpr = LT(1).getColumn()-1; }
			asExpr = expression
            {
            	lengthAsExpr = -offsetAsExpr + LT(0).getColumn()-1 + LT(0).getText().length();
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
			} else {
            	StringLiteral emptyString = graph.createStringLiteral();
            	emptyString.setStringValue("");
            	IsTableHeaderOf tableHeaderOf = graph.createIsTableHeaderOf(emptyString, bagCompr);
            	tableHeaderOf.setSourcePositions((createSourcePositionList(-1, -1)));
	    	}
		}
		(	{ hasLabel = false; }
        	COMMA
            { offsetExpr = LT(1).getColumn()-1; }
			expr = expression
            { lengthExpr = -offsetExpr + LT(0).getColumn()-1 + LT(0).getText().length(); }
			(	AS
            	{ offsetAsExpr = LT(1).getColumn()-1; }
				asExpr = expression
                {
                	lengthAsExpr = -offsetAsExpr + LT(0).getColumn()-1 + LT(0).getText().length();
                	hasLabel = true;
                }
			)?
            {
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
		)*
		{
			e.setSourcePositions((createSourcePositionList(-offset + LT(0).getColumn()-1 + LT(0).getText().length(), offset)));
		}
        {
		  	if (tupConstr.getDegree(EdgeDirection.IN) == 1)	{
				Vertex v = tupConstr.getFirstEdge(EdgeDirection.IN).getAlpha();
				Edge e2 = tupConstr.getFirstEdge(EdgeDirection.OUT);
				e2.setAlpha(v);
				tupConstr.delete();
			}
		}
	;

/** matches a list of type-descriptions: [^] typeId [!]
	@return
*/
typeExpressionList returns [Vector<VertexPosition> typeIdList = new Vector<VertexPosition>();] throws ParseException, DuplicateVariableException
{
	Expression v = null;
	Vector<VertexPosition> list = null;
    VertexPosition type = new VertexPosition();
    int offset = 0;
    int length = 0;
}
	:
	 	{ offset = LT(1).getColumn()-1; }
		v = typeId
        {
        	length = -offset + LT(0).getColumn()-1 + LT(0).getText().length();
        	type.node = v;
        	type.offset = offset;
        	type.length = length;
        	typeIdList.add(type);
        }
		(	COMMA list = typeExpressionList {typeIdList.addAll(list);} )?
	;

/** matches a list of edge restrictions: each of them containing a
    typeId and/or a roleId
    @return vector containing the elements  of the list
*/
edgeRestrictionList returns [Vector<VertexPosition> list = new Vector<VertexPosition>();] throws ParseException, DuplicateVariableException
{
	TypeId type = null;
	RoleId role = null;
	Vector<VertexPosition> eRList = null;
	VertexPosition v = new VertexPosition();
	EdgeRestriction er = null;
	int offsetType = 0;
	int offsetRole = 0;
	int lengthType = 0;
	int lengthRole = 0;
}
	:
		((
			{	offsetRole = LT(1).getColumn()-1; }
			AT role = roleId
			{
				lengthRole = -offsetRole + LT(0).getColumn()-1 + LT(0).getText().length();
			}
		)
		|
		(
			{	offsetType = LT(1).getColumn()-1; }
			type = typeId
			{
				lengthType = -offsetType + LT(0).getColumn()-1 + LT(0).getText().length();
			}
			(	AT
				{ offsetRole = LT(1).getColumn()-1; }
				role = roleId
			)?
		))
        {
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
		(	COMMA eRList = edgeRestrictionList {list.addAll(eRList);}  )?
	;


/** mathes a qualifiedName
*/	
qualifiedName returns [String name = null] throws ParseException
{
      	String newName = null;
}
    : 
      i:IDENT {name = i.getText();}
      ( DOT
  		newName = qualifiedName
  		{
         	name = name + "." + newName;
        } 
      )? 
    ;

/** matches a typeId
	@return
*/
typeId returns [TypeId type = null] throws ParseException, DuplicateVariableException
{
     String s;
}
	:
    	{
			type = graph.createTypeId();
		}
		(	CARET	{ type.setExcluded(true); } )?
		(  s = qualifiedName )
		  { 
		     type.setName(s);
		  }
		(	CARET	{ type.setExcluded(true); }
		)?
		(	EXCL	{ type.setType(true);  }
		)?
	;
	


/** matches a role-id
	@return
*/
roleId returns [RoleId role = null] throws ParseException, DuplicateVariableException
	:
    	{
 			role = graph.createRoleId();
		}
		i:IDENT
		{ role.setName(i.getText()); }
	;

/** matches one of the unaryOperators '-' and 'not'
	@return functionId-vertex representing the operator
*/
unaryOperator returns [FunctionId unaryOp = null] throws ParseException, DuplicateVariableException
{   String name = "uminus"; }
	:
		( NOT { name = "not";	}
    	  |	MINUS )
    	{
    	  unaryOp = (FunctionId) functionSymbolTable.lookup(name);
		  if (unaryOp == null)  {
		  		unaryOp = graph.createFunctionId();
			 	unaryOp.setName(name);
				functionSymbolTable.insert(name, unaryOp);
		  }
		}
	;

/** matches a list of variables: variable {, variable}
	@return vector containing variable-vertices
*/
variableList returns [Vector<VertexPosition> variables = new Vector<VertexPosition>();] throws ParseException, DuplicateVariableException
{
	Variable var = null;
	Vector<VertexPosition> list = null;
    VertexPosition v = new VertexPosition();
    int offset = 0;
    int length = 0;
}
	:   { offset = LT(1).getColumn()-1; }
		var = variable
        {
			length = -offset + LT(0).getColumn()-1 + LT(0).getText().length();
        	v.node = var;
        	v.offset = offset;
        	v.length = length;
            variables.add(v );
        }
        ( 	COMMA list = variableList
        	{
        	    variables.addAll(list);
        	}
        )?
	;

/** matches a variable
	@return the variable-vertex
*/
variable returns [Variable var = null] throws ParseException, DuplicateVariableException
	:
		i:IDENT
        {
           	var = graph.createVariable();
	        var.setName(i.getText());
        }
	;

/** matches string-, this-, int-, real-, boolean- and null-literals
	@return
*/
literal returns [Literal literal = null] throws ParseException, DuplicateVariableException
	:
		s:STRING_LITERAL
        {
           	literal = graph.createStringLiteral();
        	((StringLiteral) literal).setStringValue(decode(s.getText()));
        }
		|	tv:THISVERTEX
        	{
                literal = graph.getFirstThisLiteral();
            	if (literal != null)	
	               	return literal;
               	literal = graph.createThisVertex();
            }
       	|	te:THISEDGE
        	{
            	VertexClass thisLiteralVertexClass = (VertexClass) graphClass.getGraphElementClass(new QualifiedName("ThisEdge"));
            	literal = graph.getFirstThisEdge();
            	if (literal != null)
	               	return literal;
               	literal = graph.createThisEdge();
            }
		|	i:NUM_INT
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
		|	r:NUM_REAL
        	{
            	literal = graph.createRealLiteral();
				((RealLiteral) literal).setRealValue(Double.parseDouble(r.getText()));
            }
		|	TRUE
        	{
            	VertexClass boolLiteralVertexClass = (VertexClass) graphClass.getGraphElementClass(new QualifiedName("BoolLiteral"));
            	literal = (Literal) graph.getFirstVertexOfClass(boolLiteralVertexClass);
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
           	    VertexClass boolLiteralVertexClass = (VertexClass) graphClass.getGraphElementClass(new QualifiedName("BoolLiteral"));
            	literal = (Literal) graph.getFirstVertexOfClass(boolLiteralVertexClass);
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
				VertexClass nullLiteralVertexClass = (VertexClass) graphClass.getGraphElementClass(new QualifiedName("NullLiteral"));
	            literal = (Literal) graph.getFirstVertexOfClass(nullLiteralVertexClass);
	            if (literal == null)
		            literal = graph.createNullLiteral(); 
            }
	;

/** matches a list of expressions: expression {, expression}
	@return contains the expression-vertices
*/
expressionList returns [Vector<VertexPosition> expressions] throws ParseException, DuplicateVariableException
{
	expressions = new Vector<VertexPosition>();
	Expression expr = null;
    VertexPosition v = new VertexPosition();
    Vector<VertexPosition> exprList = new Vector<VertexPosition>();
    int offset = 0;
    int length = 0;
}
	:   { offset = LT(1).getColumn()-1; }
		expr = expression
        {
        	length = -offset + LT(0).getColumn()-1 + LT(0).getText().length();
        	v.node = expr;
        	v.offset = offset;
        	v.length = length;
        	expressions.add(v);
        }
		(	COMMA
			exprList = expressionList
            {
            	expressions.addAll(exprList);
            }
		)?
	;

/** matches a list of edges and vertices
	@return
*/
edgeVertexList returns [EdgeVertexList eVList = null] throws ParseException, DuplicateVariableException
{
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
        {offsetE = LT(1).getColumn()-1; }
		edgeExpr = expression
        { lengthE = -offsetE + LT(0).getColumn()-1 + LT(0).getText().length(); }
		COMMA
        {offsetV = LT(1).getColumn()-1; }
		vertexExpr = expression
        {
        	lengthV = -offsetV + LT(0).getColumn()-1 + LT(0).getText().length();
            eVList = graph.createEdgeVertexList();
            IsEdgeOrVertexExprOf eExprOf = graph.createIsEdgeOrVertexExprOf(edgeExpr, eVList);
            eExprOf.setSourcePositions((createSourcePositionList(lengthE, offsetE)));
            IsEdgeOrVertexExprOf vExprOf = graph.createIsEdgeOrVertexExprOf(vertexExpr, eVList);
            vExprOf.setSourcePositions((createSourcePositionList(lengthV, offsetV)));
        }
		( 	COMMA
        	{offsetEVList = LT(1).getColumn()-1; }
			eVList2 = edgeVertexList
            {
            	lengthEVList = -offsetEVList + LT(0).getColumn()-1 + LT(0).getText().length();
           		IsElementOf exprOf = graph.createIsElementOf(eVList2, eVList);
            	exprOf.setSourcePositions((createSourcePositionList(lengthEVList, offsetEVList)));
            }
		)*
		RPAREN
	;

/** matches an expression or a pathDescription
	@return
*/
expressionOrPathDescription returns [Expression expr = null] throws ParseException, DuplicateVariableException
	:
	        (  (pathDescription expression) => expr = expression
            | (expression) => expr = expression
            | expr = pathDescription)
	;
