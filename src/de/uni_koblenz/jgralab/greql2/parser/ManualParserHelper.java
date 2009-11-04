package de.uni_koblenz.jgralab.greql2.parser;

import java.util.ArrayList;
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
import de.uni_koblenz.jgralab.greql2.schema.BackwardVertexSet;
import de.uni_koblenz.jgralab.greql2.schema.BagComprehension;
import de.uni_koblenz.jgralab.greql2.schema.Comprehension;
import de.uni_koblenz.jgralab.greql2.schema.Declaration;
import de.uni_koblenz.jgralab.greql2.schema.Definition;
import de.uni_koblenz.jgralab.greql2.schema.DefinitionExpression;
import de.uni_koblenz.jgralab.greql2.schema.Expression;
import de.uni_koblenz.jgralab.greql2.schema.ForwardVertexSet;
import de.uni_koblenz.jgralab.greql2.schema.FunctionApplication;
import de.uni_koblenz.jgralab.greql2.schema.FunctionId;
import de.uni_koblenz.jgralab.greql2.schema.Greql2;
import de.uni_koblenz.jgralab.greql2.schema.Greql2Aggregation;
import de.uni_koblenz.jgralab.greql2.schema.Greql2Expression;
import de.uni_koblenz.jgralab.greql2.schema.Greql2Schema;
import de.uni_koblenz.jgralab.greql2.schema.Greql2Vertex;
import de.uni_koblenz.jgralab.greql2.schema.IsArgumentOf;
import de.uni_koblenz.jgralab.greql2.schema.IsBoundExprOfQuantifier;
import de.uni_koblenz.jgralab.greql2.schema.IsBoundVarOf;
import de.uni_koblenz.jgralab.greql2.schema.IsColumnHeaderExprOf;
import de.uni_koblenz.jgralab.greql2.schema.IsConstraintOf;
import de.uni_koblenz.jgralab.greql2.schema.IsDeclaredVarOf;
import de.uni_koblenz.jgralab.greql2.schema.IsDefinitionOf;
import de.uni_koblenz.jgralab.greql2.schema.IsFunctionIdOf;
import de.uni_koblenz.jgralab.greql2.schema.IsKeyExprOfComprehension;
import de.uni_koblenz.jgralab.greql2.schema.IsQuantifiedDeclOf;
import de.uni_koblenz.jgralab.greql2.schema.IsQueryExprOf;
import de.uni_koblenz.jgralab.greql2.schema.IsRowHeaderExprOf;
import de.uni_koblenz.jgralab.greql2.schema.IsSimpleDeclOf;
import de.uni_koblenz.jgralab.greql2.schema.IsSubgraphOf;
import de.uni_koblenz.jgralab.greql2.schema.IsTableHeaderOf;
import de.uni_koblenz.jgralab.greql2.schema.IsValueExprOfComprehension;
import de.uni_koblenz.jgralab.greql2.schema.MapComprehension;
import de.uni_koblenz.jgralab.greql2.schema.PathDescription;
import de.uni_koblenz.jgralab.greql2.schema.PathExistence;
import de.uni_koblenz.jgralab.greql2.schema.QuantifiedExpression;
import de.uni_koblenz.jgralab.greql2.schema.SimpleDeclaration;
import de.uni_koblenz.jgralab.greql2.schema.SourcePosition;
import de.uni_koblenz.jgralab.greql2.schema.TableComprehension;
import de.uni_koblenz.jgralab.greql2.schema.ThisEdge;
import de.uni_koblenz.jgralab.greql2.schema.ThisLiteral;
import de.uni_koblenz.jgralab.greql2.schema.ThisVertex;
import de.uni_koblenz.jgralab.greql2.schema.Variable;

public abstract class ManualParserHelper {

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
		if (!graphCleaned) {
			Set<Vertex> reachableVertices = new HashSet<Vertex>();
			Queue<Vertex> queue = new LinkedList<Vertex>();
			Greql2Expression root = graph.getFirstGreql2Expression();
			if (root == null) {
				return null;
			}
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
		}
		return graph;
	}

	/**
	 * merges variable-vertices in the subgraph with the root-vertex
	 * <code>v</code>
	 * 
	 * @param v
	 *            root of the subgraph
	 */
	private void mergeVariables(Vertex v) throws DuplicateVariableException,
			UndefinedVariableException {
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
			Vertex var = afterParsingvariableSymbolTable.lookup(((Variable) v).get_name());
			if (var != null) {
				if (var != v) {
					Edge inc = v.getFirstEdge(EdgeDirection.OUT);
					inc.setAlpha(var);
					if (v.getDegree() <= 0) {
						v.delete();
					}
				}	
			} else {
				Greql2Aggregation e = (Greql2Aggregation) v
						.getFirstEdge(EdgeDirection.OUT);
				throw new UndefinedVariableException((Variable) v, e
						.get_sourcePositions());
			}
		} else {
			ArrayList<Edge> incidenceList = new ArrayList<Edge>();
			for (Edge inc : v.incidences(EdgeDirection.IN)) {
				incidenceList.add(inc);
			}
			for (Edge e : incidenceList) {
				mergeVariables(e.getAlpha());
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
		for (IsBoundVarOf isBoundVarOf : root.getIsBoundVarOfIncidences(EdgeDirection.IN)) {
			afterParsingvariableSymbolTable.insert(((Variable) isBoundVarOf.getAlpha()).get_name(), isBoundVarOf.getAlpha());
		}
		IsQueryExprOf isQueryExprOf = root.getFirstIsQueryExprOf(EdgeDirection.IN);
		mergeVariables(isQueryExprOf.getAlpha());
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
	private void mergeVariablesInDefinitionExpression(DefinitionExpression v)
			throws DuplicateVariableException, UndefinedVariableException {
		afterParsingvariableSymbolTable.blockBegin();
		for (IsDefinitionOf currentEdge : v.getIsDefinitionOfIncidences(EdgeDirection.IN)) {
			Definition definition = (Definition) currentEdge.getAlpha();
			Variable variable = (Variable) definition.getFirstIsVarOf(EdgeDirection.IN).getAlpha();
			afterParsingvariableSymbolTable.insert(variable.get_name(), variable);
		}
		for (IsDefinitionOf currentEdge : v.getIsDefinitionOfIncidences(EdgeDirection.IN)) {
			Definition definition = (Definition) currentEdge.getAlpha();
			Expression expr = (Expression) definition.getFirstIsExprOf(EdgeDirection.IN).getAlpha();
			mergeVariables(expr);
		}
		Edge isBoundExprOf = v.getFirstIsBoundExprOfDefinition(EdgeDirection.IN);
		mergeVariables(isBoundExprOf.getAlpha());
		afterParsingvariableSymbolTable.blockEnd();
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
		for (IsSimpleDeclOf currentEdge : v.getIsSimpleDeclOfIncidences(EdgeDirection.IN)) {
			SimpleDeclaration simpleDecl = (SimpleDeclaration) currentEdge.getAlpha();
			for (IsDeclaredVarOf isDeclaredVarOf : simpleDecl.getIsDeclaredVarOfIncidences(EdgeDirection.IN)) {
				Variable variable = (Variable) isDeclaredVarOf.getAlpha();
				afterParsingvariableSymbolTable.insert(variable.get_name(), variable);
			}
		}
		
		for (IsSimpleDeclOf currentEdge : v.getIsSimpleDeclOfIncidences(EdgeDirection.IN)) {
			SimpleDeclaration simpleDecl = (SimpleDeclaration) currentEdge.getAlpha();
			Expression expr = (Expression) simpleDecl.getFirstIsTypeExprOf(EdgeDirection.IN).getAlpha();
			mergeVariables(expr);
		}

		IsSubgraphOf isSubgraphOf = v.getFirstIsSubgraphOf(EdgeDirection.IN);
		if (isSubgraphOf != null) {
			mergeVariables(isSubgraphOf.getAlpha());
		}
		for (IsConstraintOf isConstraintOf : v.getIsConstraintOfIncidences(EdgeDirection.IN)) {
			mergeVariables(isConstraintOf.getAlpha());
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
	private void mergeVariablesInQuantifiedExpression(QuantifiedExpression v)
			throws DuplicateVariableException, UndefinedVariableException {
		afterParsingvariableSymbolTable.blockBegin();
		IsQuantifiedDeclOf isQuantifiedDeclOf = v.getFirstIsQuantifiedDeclOf(EdgeDirection.IN);
		mergeVariablesInDeclaration((Declaration) isQuantifiedDeclOf.getAlpha());
		IsBoundExprOfQuantifier isBoundExprOfQuantifier = v.getFirstIsBoundExprOfQuantifier(EdgeDirection.IN);
		mergeVariables(isBoundExprOfQuantifier.getAlpha());
		afterParsingvariableSymbolTable.blockEnd();
	}

	/**
	 * Inserts declared variable-vertices into the variables symbol table and
	 * merges variables within the comprehension result and tableheaders
	 * 
	 * @param v
	 *            contains a set- or a bag-comprehension
	 */
	private void mergeVariablesInComprehension(Comprehension v)
			throws DuplicateVariableException, UndefinedVariableException {
		afterParsingvariableSymbolTable.blockBegin();
		Edge IsCompDeclOf = v.getFirstIsCompDeclOf(EdgeDirection.IN);
		mergeVariablesInDeclaration((Declaration) IsCompDeclOf.getAlpha());
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
				if (th != null) {
					mergeVariables(th.getAlpha());
				}
			}
		}
		if (v instanceof MapComprehension) {
			IsKeyExprOfComprehension keyEdge = ((MapComprehension) v)
					.getFirstIsKeyExprOfComprehension();
			mergeVariables(keyEdge.getAlpha());
			IsValueExprOfComprehension valueEdge = ((MapComprehension) v)
					.getFirstIsValueExprOfComprehension();
			mergeVariables(valueEdge.getAlpha());
		}
		afterParsingvariableSymbolTable.blockEnd();
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
			this.arg2 = arg2;
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

	protected final boolean isFunctionName(String ident) {
		return funlib.isGreqlFunction(ident);
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
		// list.add(new SourcePosition(length, offset));
		list.add(graph.createSourcePosition(length, offset));
		return list;
	}

	/**
	 * Test if all ThisLiterals occur only inside PathDescriptions because they
	 * must not be used outside PathDescriptions If any ThisLiteral that occurs
	 * outside a PathDescription is found, a ParseException is thrown.
	 */
	protected final void testIllegalThisLiterals() {
		Set<Class<? extends Greql2Vertex>> allowedClassesForThisLiterals = new HashSet<Class<? extends Greql2Vertex>>();
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
					for (Edge edge : currentVertex
							.incidences(EdgeDirection.OUT)) {
						Greql2Vertex omega = (Greql2Vertex) edge.getOmega();
						if (allowedClassesForThisLiterals.contains(omega
								.getM1Class())) {
							continue;
						}
						if (omega instanceof FunctionApplication) {
							FunctionApplication fa = (FunctionApplication) omega;
							FunctionId funid = (FunctionId) fa
									.getFirstIsFunctionIdOf(EdgeDirection.IN)
									.getAlpha();
							if (funid.get_name().equals("pathSystem")) {
								continue;
							}
						}
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

}
