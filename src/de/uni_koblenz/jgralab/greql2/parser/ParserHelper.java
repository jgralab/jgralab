/*
 * JGraLab - The Java Graph Laboratory
 * 
 * Copyright (C) 2006-2010 Institute for Software Technology
 *                         University of Koblenz-Landau, Germany
 *                         ist@uni-koblenz.de
 * 
 * This program is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as published by the
 * Free Software Foundation; either version 3 of the License, or (at your
 * option) any later version.
 * 
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General
 * Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, see <http://www.gnu.org/licenses>.
 * 
 * Additional permission under GNU GPL version 3 section 7
 * 
 * If you modify this Program, or any covered work, by linking or combining
 * it with Eclipse (or a modified version of that program or an Eclipse
 * plugin), containing parts covered by the terms of the Eclipse Public
 * License (EPL), the licensors of this Program grant you additional
 * permission to convey the resulting work.  Corresponding Source for a
 * non-source form of such a combination shall include the source code for
 * the parts of JGraLab used as well as that of the covered work.
 */
package de.uni_koblenz.jgralab.greql2.parser;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;

import de.uni_koblenz.jgralab.Edge;
import de.uni_koblenz.jgralab.EdgeDirection;
import de.uni_koblenz.jgralab.Vertex;
import de.uni_koblenz.jgralab.greql2.exception.DuplicateVariableException;
import de.uni_koblenz.jgralab.greql2.exception.ParsingException;
import de.uni_koblenz.jgralab.greql2.exception.UndefinedVariableException;
import de.uni_koblenz.jgralab.greql2.funlib.Greql2FunctionLibrary;
import de.uni_koblenz.jgralab.greql2.schema.BagComprehension;
import de.uni_koblenz.jgralab.greql2.schema.Comprehension;
import de.uni_koblenz.jgralab.greql2.schema.Declaration;
import de.uni_koblenz.jgralab.greql2.schema.Definition;
import de.uni_koblenz.jgralab.greql2.schema.DefinitionExpression;
import de.uni_koblenz.jgralab.greql2.schema.Expression;
import de.uni_koblenz.jgralab.greql2.schema.FunctionApplication;
import de.uni_koblenz.jgralab.greql2.schema.FunctionId;
import de.uni_koblenz.jgralab.greql2.schema.Greql2;
import de.uni_koblenz.jgralab.greql2.schema.Greql2Aggregation;
import de.uni_koblenz.jgralab.greql2.schema.Greql2Expression;
import de.uni_koblenz.jgralab.greql2.schema.Greql2Schema;
import de.uni_koblenz.jgralab.greql2.schema.Greql2Vertex;
import de.uni_koblenz.jgralab.greql2.schema.IsArgumentOf;
import de.uni_koblenz.jgralab.greql2.schema.IsBooleanPredicateOfEdgeRestriction;
import de.uni_koblenz.jgralab.greql2.schema.IsBoundExprOfQuantifier;
import de.uni_koblenz.jgralab.greql2.schema.IsBoundVarOf;
import de.uni_koblenz.jgralab.greql2.schema.IsColumnHeaderExprOf;
import de.uni_koblenz.jgralab.greql2.schema.IsConstraintOf;
import de.uni_koblenz.jgralab.greql2.schema.IsDeclaredVarOf;
import de.uni_koblenz.jgralab.greql2.schema.IsDefinitionOf;
import de.uni_koblenz.jgralab.greql2.schema.IsExprOf;
import de.uni_koblenz.jgralab.greql2.schema.IsFunctionIdOf;
import de.uni_koblenz.jgralab.greql2.schema.IsGoalRestrOf;
import de.uni_koblenz.jgralab.greql2.schema.IsKeyExprOfComprehension;
import de.uni_koblenz.jgralab.greql2.schema.IsQuantifiedDeclOf;
import de.uni_koblenz.jgralab.greql2.schema.IsQueryExprOf;
import de.uni_koblenz.jgralab.greql2.schema.IsRowHeaderExprOf;
import de.uni_koblenz.jgralab.greql2.schema.IsSimpleDeclOf;
import de.uni_koblenz.jgralab.greql2.schema.IsStartRestrOf;
import de.uni_koblenz.jgralab.greql2.schema.IsSubgraphOf;
import de.uni_koblenz.jgralab.greql2.schema.IsTableHeaderOf;
import de.uni_koblenz.jgralab.greql2.schema.IsValueExprOfComprehension;
import de.uni_koblenz.jgralab.greql2.schema.IsVarOf;
import de.uni_koblenz.jgralab.greql2.schema.MapComprehension;
import de.uni_koblenz.jgralab.greql2.schema.PathDescription;
import de.uni_koblenz.jgralab.greql2.schema.QuantifiedExpression;
import de.uni_koblenz.jgralab.greql2.schema.SimpleDeclaration;
import de.uni_koblenz.jgralab.greql2.schema.SourcePosition;
import de.uni_koblenz.jgralab.greql2.schema.TableComprehension;
import de.uni_koblenz.jgralab.greql2.schema.ThisEdge;
import de.uni_koblenz.jgralab.greql2.schema.ThisLiteral;
import de.uni_koblenz.jgralab.greql2.schema.ThisVertex;
import de.uni_koblenz.jgralab.greql2.schema.Variable;
import de.uni_koblenz.jgralab.greql2.schema.WhereExpression;

public abstract class ParserHelper {

	protected String query = null;

	protected Greql2 graph;

	protected Greql2Schema schema = null;

	protected SymbolTable afterParsingvariableSymbolTable = null;

	protected EasySymbolTable duringParsingvariableSymbolTable = null;

	protected Map<String, FunctionId> functionSymbolTable = null;

	protected boolean graphCleaned = false;

	protected Greql2FunctionLibrary funlib = null;

	protected Token lookAhead = null;

	protected abstract boolean inPredicateMode();

	protected final int getCurrentOffset() {
		if (lookAhead != null) {
			return lookAhead.getOffset();
		}
		return query.length();
	}

	protected final int getLength(int offset) {
		return getCurrentOffset() - offset;
	}

	public PathDescription addPathElement(Class<? extends PathDescription> vc,
			Class<? extends Edge> ec, PathDescription pathDescr,
			PathDescription part1, int offsetPart1, int lengthPart1,
			PathDescription part2, int offsetPart2, int lengthPart2) {
		Greql2Aggregation edge = null;
		if (pathDescr == null) {
			pathDescr = graph.createVertex(vc);
			edge = (Greql2Aggregation) graph.createEdge(ec, part1, pathDescr);
			edge.set_sourcePositions((createSourcePositionList(lengthPart1,
					offsetPart1)));
		}
		edge = (Greql2Aggregation) graph.createEdge(ec, part2, pathDescr);
		edge.set_sourcePositions((createSourcePositionList(lengthPart2,
				offsetPart2)));
		return pathDescr;
	}

	/**
	 * Returns the abstract syntax graph for the input
	 * 
	 * @return the abstract syntax graph representing a GReQL 2 query
	 */
	public Greql2 getGraph() {
		if (graph == null) {
			return null;
		}
		cleanGraph();
		return graph;
	}

	private void cleanGraph() {
		if (!graphCleaned) {
			Set<Vertex> reachableVertices = new HashSet<Vertex>();
			Queue<Vertex> queue = new LinkedList<Vertex>();
			Greql2Expression root = graph.getFirstGreql2Expression();
			queue.add(root);
			while (!queue.isEmpty()) {
				Vertex current = queue.poll();
				if (current != null)
				for (Edge e : current.incidences()) {
					if (!reachableVertices.contains(e.getThat())) {
						queue.add(e.getThat());
						reachableVertices.add(e.getThat());
					}
				}
			}
			Vertex deleteCandidate = graph.getFirstVertex();
			while ((deleteCandidate != null)
					&& (!reachableVertices.contains(deleteCandidate))) {
				deleteCandidate.delete();
				deleteCandidate = graph.getFirstVertex();
			}
			while (deleteCandidate != null) {
				if (!reachableVertices.contains(deleteCandidate)) {
					Vertex v = deleteCandidate.getNextVertex();
					deleteCandidate.delete();
					deleteCandidate = v;
				} else {
					deleteCandidate = deleteCandidate.getNextVertex();
				}
			}
			replaceDefinitionExpressions();
			eliminateUnusedNodes();
		}
	}

	private void replaceDefinitionExpressions()
			throws DuplicateVariableException, UndefinedVariableException {
		List<DefinitionExpression> list = new ArrayList<DefinitionExpression>();
		for (DefinitionExpression exp : graph.getDefinitionExpressionVertices()) {
			list.add(exp);
		}

		/* iterate over all definitionsexpressions in the graph */
		for (DefinitionExpression exp : list) {
			List<Definition> defList = new ArrayList<Definition>();
			for (IsDefinitionOf isDefOf : exp
					.getIsDefinitionOfIncidences(EdgeDirection.IN)) {
				Definition definition = (Definition) isDefOf.getAlpha();
				defList.add(definition);
			}
			/*
			 * if the current DefinitionExpression is a whereExpression, revert
			 * the list of definitions
			 */
			if (exp instanceof WhereExpression) {
				Collections.reverse(defList);
			}

			/* iterate over all definitions at the current definition expression */
			for (Definition definition : defList) {
				IsExprOf isExprOf = definition
						.getFirstIsExprOfIncidence(EdgeDirection.IN);
				IsVarOf isVarOf = definition
						.getFirstIsVarOfIncidence(EdgeDirection.IN);
				Expression expr = (Expression) isExprOf.getAlpha();
				Variable variable = (Variable) isVarOf.getAlpha();
				isVarOf.delete();
				isExprOf.delete();
				Edge e = variable.getFirstIncidence(EdgeDirection.OUT);
				while (e != null) {
					e.setAlpha(expr);
					e = variable.getFirstIncidence(EdgeDirection.OUT);
				}
				variable.delete();
			}
			Expression boundExpr = (Expression) exp
					.getFirstIsBoundExprOfIncidence(EdgeDirection.IN)
					.getAlpha();
			Edge e = exp.getFirstIncidence(EdgeDirection.OUT);
			while (e != null) {
				e.setAlpha(boundExpr);
				e = exp.getFirstIncidence(EdgeDirection.OUT);
			}
			exp.delete();
		}
	}

	protected void eliminateUnusedNodes() {
		List<Vertex> deleteList = new ArrayList<Vertex>();
		for (Vertex v : graph.vertices()) {
			if (v.getFirstIncidence() == null) {
				deleteList.add(v);
			}
		}
		for (Vertex v : deleteList) {
			v.delete();
		}
	}

	/**
	 * merges variable-vertices in the subgraph with the root-vertex
	 * <code>v</code>
	 * 
	 * @param v
	 *            root of the subgraph
	 * @param separateScope
	 *            if true, this block may define a separate scope, should be
	 *            true in most cases but false for where and let expression
	 *            calling this method, e.g. in
	 *            "from x:A with p report x end where p := x > 7" the where and
	 *            the from clause have the same scope
	 */
	private void mergeVariables(Vertex v, boolean separateScope)
			throws DuplicateVariableException, UndefinedVariableException {
		if (v instanceof DefinitionExpression) {
			mergeVariablesInDefinitionExpression((DefinitionExpression) v,
					separateScope);
		} else if (v instanceof Comprehension) {
			mergeVariablesInComprehension((Comprehension) v, separateScope);
		} else if (v instanceof QuantifiedExpression) {
			mergeVariablesInQuantifiedExpression((QuantifiedExpression) v,
					separateScope);
		} else if (v instanceof Greql2Expression) {
			mergeVariablesInGreql2Expression((Greql2Expression) v);
		} else if (v instanceof ThisLiteral) {
			return;
		} else if (v instanceof Variable) {
			Vertex var = afterParsingvariableSymbolTable.lookup(((Variable) v)
					.get_name());
			if (var != null) {
				if (var != v) {
					Edge inc = v.getFirstIncidence(EdgeDirection.OUT);
					inc.setAlpha(var);
					if (v.getDegree() <= 0) {
						v.delete();
					}
				}
			} else {
				Greql2Aggregation e = (Greql2Aggregation) v
						.getFirstIncidence(EdgeDirection.OUT);
				throw new UndefinedVariableException((Variable) v,
						e.get_sourcePositions());
			}
		} else {
			ArrayList<Edge> incidenceList = new ArrayList<Edge>();
			for (Edge inc : v.incidences(EdgeDirection.IN)) {
				incidenceList.add(inc);
			}
			for (Edge e : incidenceList) {
				mergeVariables(e.getAlpha(), separateScope);
			}
		}
	}

	/**
	 * Inserts variable-vertices that are declared in the <code>using</code>
	 * -clause into the variables symbol table and merges variables within the
	 * query-expression.
	 * 
	 * @param root
	 *            root of the graph, represents a <code>Greql2Expression</code>
	 */
	protected final void mergeVariablesInGreql2Expression(Greql2Expression root)
			throws DuplicateVariableException, UndefinedVariableException {
		afterParsingvariableSymbolTable.blockBegin();
		for (IsBoundVarOf isBoundVarOf : root
				.getIsBoundVarOfIncidences(EdgeDirection.IN)) {
			afterParsingvariableSymbolTable.insert(
					((Variable) isBoundVarOf.getAlpha()).get_name(),
					isBoundVarOf.getAlpha());
		}
		IsQueryExprOf isQueryExprOf = root
				.getFirstIsQueryExprOfIncidence(EdgeDirection.IN);
		mergeVariables(isQueryExprOf.getAlpha(), true);
		afterParsingvariableSymbolTable.blockEnd();
	}

	/**
	 * Inserts variables that are defined in the definitions of let- or
	 * where-expressions and merges variables used in these definitions and in
	 * the bound expression
	 * 
	 * @param v
	 *            contains a let- or where-expression.
	 */
	private void mergeVariablesInDefinitionExpression(DefinitionExpression v,
			boolean separateScope) throws DuplicateVariableException,
			UndefinedVariableException {
		if (separateScope) {
			afterParsingvariableSymbolTable.blockBegin();
		}
		for (IsDefinitionOf currentEdge : v
				.getIsDefinitionOfIncidences(EdgeDirection.IN)) {
			Definition definition = (Definition) currentEdge.getAlpha();
			Variable variable = (Variable) definition.getFirstIsVarOfIncidence(
					EdgeDirection.IN).getAlpha();
			afterParsingvariableSymbolTable.insert(variable.get_name(),
					variable);
		}
		Edge isBoundExprOf = v
				.getFirstIsBoundExprOfDefinitionIncidence(EdgeDirection.IN);
		mergeVariables(isBoundExprOf.getAlpha(), false);
		for (IsDefinitionOf currentEdge : v
				.getIsDefinitionOfIncidences(EdgeDirection.IN)) {
			Definition definition = (Definition) currentEdge.getAlpha();
			Expression expr = (Expression) definition
					.getFirstIsExprOfIncidence(EdgeDirection.IN).getAlpha();
			mergeVariables(expr, true);
		}
		if (separateScope) {
			afterParsingvariableSymbolTable.blockEnd();
		}
	}

	/**
	 * Inserts variables that are declared in a declaration of a simple query or
	 * a quantified expression into the symbol-table and merges variables that
	 * are used in these declaration (in typeexpressions, constraints, or
	 * subgraphs)
	 * 
	 * @param v
	 *            contains a declaration
	 */
	private void mergeVariablesInDeclaration(Declaration v)
			throws DuplicateVariableException, UndefinedVariableException {
		for (IsSimpleDeclOf currentEdge : v
				.getIsSimpleDeclOfIncidences(EdgeDirection.IN)) {
			SimpleDeclaration simpleDecl = (SimpleDeclaration) currentEdge
					.getAlpha();
			for (IsDeclaredVarOf isDeclaredVarOf : simpleDecl
					.getIsDeclaredVarOfIncidences(EdgeDirection.IN)) {
				Variable variable = (Variable) isDeclaredVarOf.getAlpha();
				afterParsingvariableSymbolTable.insert(variable.get_name(),
						variable);
			}
		}

		for (IsSimpleDeclOf currentEdge : v
				.getIsSimpleDeclOfIncidences(EdgeDirection.IN)) {
			SimpleDeclaration simpleDecl = (SimpleDeclaration) currentEdge
					.getAlpha();
			Expression expr = (Expression) simpleDecl
					.getFirstIsTypeExprOfIncidence(EdgeDirection.IN).getAlpha();
			mergeVariables(expr, true);
		}

		IsSubgraphOf isSubgraphOf = v
				.getFirstIsSubgraphOfIncidence(EdgeDirection.IN);
		if (isSubgraphOf != null) {
			mergeVariables(isSubgraphOf.getAlpha(), true);
		}
		for (IsConstraintOf isConstraintOf : v
				.getIsConstraintOfIncidences(EdgeDirection.IN)) {
			mergeVariables(isConstraintOf.getAlpha(), true);
		}
	}

	/**
	 * Inserts variable-vertices that are declared in the quantified expression
	 * represented by <code>v</code> into the variables symbol table and merges
	 * variables within the bound expression.
	 * 
	 * @param v
	 *            contains a quantified expression
	 */
	private void mergeVariablesInQuantifiedExpression(QuantifiedExpression v,
			boolean separateScope) throws DuplicateVariableException,
			UndefinedVariableException {
		if (separateScope) {
			afterParsingvariableSymbolTable.blockBegin();
		}
		IsQuantifiedDeclOf isQuantifiedDeclOf = v
				.getFirstIsQuantifiedDeclOfIncidence(EdgeDirection.IN);
		mergeVariablesInDeclaration((Declaration) isQuantifiedDeclOf.getAlpha());
		IsBoundExprOfQuantifier isBoundExprOfQuantifier = v
				.getFirstIsBoundExprOfQuantifierIncidence(EdgeDirection.IN);
		mergeVariables(isBoundExprOfQuantifier.getAlpha(), true);
		if (separateScope) {
			afterParsingvariableSymbolTable.blockEnd();
		}
	}

	/**
	 * Inserts declared variable-vertices into the variables symbol table and
	 * merges variables within the comprehension result and tableheaders
	 * 
	 * @param v
	 *            contains a set- or a bag-comprehension
	 */
	private void mergeVariablesInComprehension(Comprehension v,
			boolean separateScope) throws DuplicateVariableException,
			UndefinedVariableException {
		if (separateScope) {
			afterParsingvariableSymbolTable.blockBegin();
		}
		Edge IsCompDeclOf = v.getFirstIsCompDeclOfIncidence(EdgeDirection.IN);
		mergeVariablesInDeclaration((Declaration) IsCompDeclOf.getAlpha());
		Edge isCompResultDefOf = v
				.getFirstIsCompResultDefOfIncidence(EdgeDirection.IN);
		if (isCompResultDefOf != null) {
			mergeVariables(isCompResultDefOf.getAlpha(), true);
			// merge variables in table-headers if it's a bag-comprehension
			if (v instanceof BagComprehension) {
				IsTableHeaderOf isTableHeaderOf = v
						.getFirstIsTableHeaderOfIncidence(EdgeDirection.IN);
				while (isTableHeaderOf != null) {
					mergeVariables(isTableHeaderOf.getAlpha(), true);
					isTableHeaderOf = isTableHeaderOf
							.getNextIsTableHeaderOf(EdgeDirection.IN);
				}
			}
			if (v instanceof TableComprehension) {
				TableComprehension tc = (TableComprehension) v;
				IsColumnHeaderExprOf ch = tc
						.getFirstIsColumnHeaderExprOfIncidence(EdgeDirection.IN);
				mergeVariables(ch.getAlpha(), true);
				IsRowHeaderExprOf rh = tc
						.getFirstIsRowHeaderExprOfIncidence(EdgeDirection.IN);
				mergeVariables(rh.getAlpha(), true);
				IsTableHeaderOf th = tc
						.getFirstIsTableHeaderOfIncidence(EdgeDirection.IN);
				if (th != null) {
					mergeVariables(th.getAlpha(), true);
				}
			}
		}
		if (v instanceof MapComprehension) {
			IsKeyExprOfComprehension keyEdge = ((MapComprehension) v)
					.getFirstIsKeyExprOfComprehensionIncidence();
			mergeVariables(keyEdge.getAlpha(), true);
			IsValueExprOfComprehension valueEdge = ((MapComprehension) v)
					.getFirstIsValueExprOfComprehensionIncidence();
			mergeVariables(valueEdge.getAlpha(), true);
		}
		if (separateScope) {
			afterParsingvariableSymbolTable.blockEnd();
		}
	}

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

		public FunctionConstruct(FunctionConstruct leftPart) {
			offsetArg1 = leftPart.offsetArg1;
		}

		public FunctionConstruct() {
		}

		public boolean isValidFunction() {
			return operatorName != null;
		}

		public void preUnaryOp() {
			binary = false;
			offsetOperator = getCurrentOffset();
		}

		public void preArg1() {
			offsetArg1 = getCurrentOffset();
		}

		public void preOp(Expression arg1) {
			binary = true;
			this.arg1 = arg1;
			lengthArg1 = getLength(offsetArg1);
			offsetOperator = getCurrentOffset();
		}

		public void postOp(String op) {
			lengthOperator = getLength(offsetOperator);
			offsetArg2 = getCurrentOffset();
			this.operatorName = op;
		}

		public FunctionApplication postArg2(Expression arg2) {
			if (inPredicateMode()) {
				return null;
			}
			lengthArg2 = getLength(offsetArg2);
			op = getFunctionId(operatorName);
			return createFunctionIdAndArgumentOf(op, offsetOperator,
					lengthOperator, arg1, offsetArg1, lengthArg1, arg2,
					offsetArg2, lengthArg2, binary);
		}
	}

	protected abstract void debug(String s);

	protected final FunctionId getFunctionId(String name) {
		FunctionId functionId = functionSymbolTable.get(name);
		if (functionId == null) {
			functionId = graph.createFunctionId();
			functionId.set_name(name);
			functionSymbolTable.put(name, functionId);
		}
		return functionId;
	}

	protected FunctionApplication createFunctionIdAndArgumentOf(
			FunctionId functionId, int offsetOperator, int lengthOperator,
			Expression arg1, int offsetArg1, int lengthArg1, Expression arg2,
			int offsetArg2, int lengthArg2, boolean binary) {
		FunctionApplication fa = graph.createFunctionApplication();
		IsFunctionIdOf functionIdOf = graph
				.createIsFunctionIdOf(functionId, fa);
		functionIdOf.set_sourcePositions((createSourcePositionList(
				lengthOperator, offsetOperator)));
		IsArgumentOf arg1Of = null;
		if (binary) {
			arg1Of = graph.createIsArgumentOf(arg1, fa);
			arg1Of.set_sourcePositions((createSourcePositionList(lengthArg1,
					offsetArg1)));
		}
		IsArgumentOf arg2Of = graph.createIsArgumentOf(arg2, fa);
		arg2Of.set_sourcePositions((createSourcePositionList(lengthArg2,
				offsetArg2)));
		return fa;
	}

	protected final List<SourcePosition> createSourcePositionList(int length,
			int offset) {
		List<SourcePosition> list = new ArrayList<SourcePosition>();
		list.add(graph.createSourcePosition(length, offset));
		return list;
	}

	/**
	 * Test if all ThisLiterals occur only inside PathDescriptions because they
	 * must not be used outside PathDescriptions If any ThisLiteral that occurs
	 * outside a PathDescription is found, a ParseException is thrown.
	 */
	protected final void testIllegalThisLiterals() {
		Set<Class<? extends Greql2Aggregation>> allowedEdgesForThisVertex = new HashSet<Class<? extends Greql2Aggregation>>();
		Set<Class<? extends Greql2Aggregation>> allowedEdgesForThisEdge = new HashSet<Class<? extends Greql2Aggregation>>();
		allowedEdgesForThisVertex.add(IsGoalRestrOf.class);
		allowedEdgesForThisVertex.add(IsStartRestrOf.class);
		allowedEdgesForThisEdge.add(IsBooleanPredicateOfEdgeRestriction.class);

		for (ThisLiteral vertex : graph.getThisVertexVertices()) {
			for (Edge sourcePositionEdge : vertex.incidences(EdgeDirection.OUT)) {
				Queue<Greql2Vertex> queue = new LinkedList<Greql2Vertex>();
				queue.add(vertex);
				while (!queue.isEmpty()) {
					Greql2Vertex currentVertex = queue.poll();
					for (Edge edge : currentVertex
							.incidences(EdgeDirection.OUT)) {
						if (allowedEdgesForThisVertex.contains(edge
								.getM1Class())) {
							continue;
						}
						Greql2Vertex omega = (Greql2Vertex) edge.getOmega();
						if (omega instanceof Greql2Expression) {
							throw new ParsingException(
									"This literals must not be used outside pathdescriptions",
									vertex.get_name(),
									((Greql2Aggregation) sourcePositionEdge)
											.get_sourcePositions().get(0)
											.get_offset(),
									((Greql2Aggregation) sourcePositionEdge)
											.get_sourcePositions().get(0)
											.get_length(), query);
						}
						queue.add(omega);
					}
				}
			}
		}

		for (ThisLiteral vertex : graph.getThisEdgeVertices()) {
			for (Edge sourcePositionEdge : vertex.incidences(EdgeDirection.OUT)) {
				Queue<Greql2Vertex> queue = new LinkedList<Greql2Vertex>();
				queue.add(vertex);
				while (!queue.isEmpty()) {
					Greql2Vertex currentVertex = queue.poll();
					for (Edge edge : currentVertex
							.incidences(EdgeDirection.OUT)) {
						if (allowedEdgesForThisEdge.contains(edge.getM1Class())) {
							continue;
						}
						Greql2Vertex omega = (Greql2Vertex) edge.getOmega();
						if (omega instanceof Greql2Expression) {
							throw new ParsingException(
									"This literals must not be used outside pathdescriptions",
									vertex.get_name(),
									((Greql2Aggregation) sourcePositionEdge)
											.get_sourcePositions().get(0)
											.get_offset(),
									((Greql2Aggregation) sourcePositionEdge)
											.get_sourcePositions().get(0)
											.get_length(), query);
						}
						queue.add(omega);
					}
				}
			}
		}
		LinkedList<Vertex> literalsToDelete = new LinkedList<Vertex>();
		ThisVertex firstThisVertex = null;
		for (ThisVertex thisVertex : graph.getThisVertexVertices()) {
			if (firstThisVertex == null) {
				firstThisVertex = thisVertex;
			} else {
				while (thisVertex.getFirstIncidence() != null) {
					Edge e = thisVertex.getFirstIncidence();
					e.setThis(firstThisVertex);
				}
				literalsToDelete.add(thisVertex);
			}
		}
		ThisEdge firstThisEdge = null;
		for (ThisEdge thisEdge : graph.getThisEdgeVertices()) {
			if (firstThisEdge == null) {
				firstThisEdge = thisEdge;
			} else {
				while (thisEdge.getFirstIncidence() != null) {
					Edge e = thisEdge.getFirstIncidence();
					e.setThis(firstThisEdge);
				}
				literalsToDelete.add(thisEdge);
			}
		}
		while (!literalsToDelete.isEmpty()) {
			literalsToDelete.getFirst().delete();
		}
	}

}
