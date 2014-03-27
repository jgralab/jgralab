/*
 * JGraLab - The Java Graph Laboratory
 *
 * Copyright (C) 2006-2013 Institute for Software Technology
 *                         University of Koblenz-Landau, Germany
 *                         ist@uni-koblenz.de
 *
 * For bug reports, documentation and further information, visit
 *
 *                         https://github.com/jgralab/jgralab
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
package de.uni_koblenz.jgralab.greql.parser;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;

import org.pcollections.PSet;
import org.pcollections.PVector;

import de.uni_koblenz.jgralab.Edge;
import de.uni_koblenz.jgralab.EdgeDirection;
import de.uni_koblenz.jgralab.ImplementationType;
import de.uni_koblenz.jgralab.JGraLab;
import de.uni_koblenz.jgralab.Vertex;
import de.uni_koblenz.jgralab.greql.exception.DuplicateVariableException;
import de.uni_koblenz.jgralab.greql.exception.ParsingException;
import de.uni_koblenz.jgralab.greql.funlib.FunLib;
import de.uni_koblenz.jgralab.greql.schema.*;
import de.uni_koblenz.jgralab.schema.EdgeClass;

public class GreqlParser extends ParserHelper {
	private static final GreqlSchema SCHEMA = GreqlSchema.instance();
	private final Map<RuleEnum, int[]> testedRules = new HashMap<RuleEnum, int[]>();

	private List<Token> tokens = null;

	private int current = 0;

	private int farestOffset = 0;

	private ParsingException farestException = null;

	private final Stack<Integer> parsingStack;

	private final Stack<Boolean> predicateStack;

	private boolean predicateFulfilled = true;

	private Set<String> subQueryNames = null;

	/**
	 * @return the set of variables which are valid at the current position in
	 *         the query
	 */
	public final Set<String> getValidVariables() {
		return duringParsingvariableSymbolTable.getKnownIdentifierSet();
	}

	private final void ruleSucceeds(RuleEnum rule, int pos) {
		int[] maySucceedArray = testedRules.get(rule);
		maySucceedArray[pos] = current;
	}

	/**
	 * Checks if the rule specified by <code>rule</code> was already tested at
	 * the current token position. If it was already tested, this method skips
	 * the number of tokens which were consumed by the rule in its last
	 * application at the current token
	 * 
	 * @param rule
	 *            the rule to test
	 * @return the current token position if the rule was not applied before or
	 *         -1, if the rule has already been applied successfully at this
	 *         current position in the token stream and thus no second test of
	 *         that rule is needed
	 * @throws ParsingException
	 *             if the rule has already failed at this position
	 */
	private final int alreadySucceeded(RuleEnum rule) {
		int[] maySucceedArray = testedRules.get(rule);
		if (maySucceedArray == null) {
			maySucceedArray = new int[tokens.size() + 1];
			for (int i = 0; i < maySucceedArray.length; i++) {
				maySucceedArray[i] = -1;
			}
			testedRules.put(rule, maySucceedArray);
		}
		int positionOfTokenAfterRule = maySucceedArray[current];
		if (inPredicateMode()) {
			if (positionOfTokenAfterRule == -1) { // not yet tested
				maySucceedArray[current] = -2;
			} else if (positionOfTokenAfterRule == -2) {// rule has not
				// succeeded, fail
				fail("Rule " + rule.toString() + " already tested at position "
						+ current + " Current Token " + lookAhead(0));
				return -2;
			} else {
				current = positionOfTokenAfterRule; // skip tokens consumed by
				// rule in last application
				return -1;
			}
		}
		return current;
	}

	/**
	 * Tests, if the application of the current rule can be skipped, should be
	 * used _only_ and _exclusively_ with the result of alreadySucceeded as
	 * parameter, example: int pos = alreadySucceeded(RuleEnum.EXPRESSION); if
	 * (skipRule(pos)) return null; Expression expr =
	 * parseQuantifiedExpression(); ruleSucceeded(RuleEnum.EXPRESSION, pos);
	 * return expr;
	 * 
	 * @return true if the rule application has already been tested and the
	 *         parser is still in predicate mode, so the rule and the tokens it
	 *         matched last time can be skipped, false otherwise
	 */
	private final boolean skipRule(int pos) {
		return (pos < 0) && inPredicateMode();
	}

	public GreqlParser(String source) {
		this(source, null);
	}

	public GreqlParser(String source, Set<String> subQueryNames) {
		query = source;
		parsingStack = new Stack<Integer>();
		predicateStack = new Stack<Boolean>();
		graph = SCHEMA.createGreqlGraph(ImplementationType.STANDARD);
		tokens = GreqlLexer.scan(source);
		afterParsingvariableSymbolTable = new SymbolTable();
		duringParsingvariableSymbolTable = new SimpleSymbolTable();
		duringParsingvariableSymbolTable.blockBegin();
		functionSymbolTable = new HashMap<String, FunctionId>();
		graphCleaned = false;
		lookAhead = tokens.get(0);
		this.subQueryNames = subQueryNames;
	}

	protected final boolean isFunctionName(String ident) {
		return ((subQueryNames != null) && subQueryNames.contains(ident))
				|| FunLib.contains(ident);
	}

	public void parse() {
		try {
			parseQuery();
		} catch (ParsingException ex) {
			if (farestException != null) {
				throw farestException;
			} else {
				throw ex;
			}
		}
	}

	@Override
	protected void debug(String s) {
		for (int i = 0; i < parsingStack.size(); i++) {
			System.out.print("    ");
		}
		System.out.println(s);
	}

	private final GreqlTokenType lookAhead(int i) {
		if ((current + i) < tokens.size()) {
			return tokens.get(current + i).type;
		} else {
			return GreqlTokenType.EOF;
		}
	}

	protected final PVector<SourcePosition> createSourcePositionList(
			int startOffset) {
		PVector<SourcePosition> list = JGraLab.vector();
		return list.plus(new SourcePosition(getCurrentOffset() - startOffset,
				startOffset));
	}

	public static GreqlGraph parse(String query) {
		return parse(query, null);
	}

	public static GreqlGraph parse(String query, Set<String> subQueryNames) {
		GreqlParser parser = new GreqlParser(query, subQueryNames);
		parser.parse();
		return parser.getGraph();
	}

	private final ValueConstruction createPartsOfValueConstruction(
			List<VertexPosition<Expression>> expressions,
			ValueConstruction parent) {
		return (ValueConstruction) createMultipleEdgesToParent(expressions,
				parent, IsPartOf.EC);
	}

	private final Vertex createMultipleEdgesToParent(
			List<VertexPosition<Expression>> expressions, Vertex parent,
			EdgeClass edgeClass) {
		if (expressions != null) {
			for (VertexPosition<? extends Vertex> expr : expressions) {
				GreqlAggregation edge = (GreqlAggregation) graph.createEdge(
						edgeClass, expr.node, parent);
				edge.set_sourcePositions(createSourcePositionList(expr.length,
						expr.offset));
			}
		}
		return parent;
	}

	private final Vertex createMultipleEdgesToParent(
			List<VertexPosition<TypeId>> expressions, Vertex parent,
			EdgeClass edgeClass, int i) {
		if (expressions != null) {
			for (VertexPosition<? extends Vertex> expr : expressions) {
				GreqlAggregation edge = (GreqlAggregation) graph.createEdge(
						edgeClass, expr.node, parent);
				edge.set_sourcePositions(createSourcePositionList(expr.length,
						expr.offset));
			}
		}
		return parent;
	}

	private final Vertex createMultipleEdgesToParent(
			List<VertexPosition<SimpleDeclaration>> expressions, Vertex parent,
			EdgeClass edgeClass, boolean b) {
		if (expressions != null) {
			for (VertexPosition<? extends Vertex> expr : expressions) {
				GreqlAggregation edge = (GreqlAggregation) graph.createEdge(
						edgeClass, expr.node, parent);
				edge.set_sourcePositions(createSourcePositionList(expr.length,
						expr.offset));
			}
		}
		return parent;
	}

	private final Vertex createMultipleEdgesToParent(
			List<VertexPosition<Variable>> expressions, Vertex parent,
			EdgeClass edgeClass, String s) {
		if (expressions != null) {
			for (VertexPosition<? extends Vertex> expr : expressions) {
				GreqlAggregation edge = (GreqlAggregation) graph.createEdge(
						edgeClass, expr.node, parent);
				edge.set_sourcePositions(createSourcePositionList(expr.length,
						expr.offset));
			}
		}
		return parent;
	}

	private final void predicateStart() {
		parsingStack.push(current);
		predicateStack.push(predicateFulfilled);
		predicateFulfilled = true;
	}

	private final void match() {
		current++;
		if (current < tokens.size()) {
			lookAhead = tokens.get(current);
		} else {
			lookAhead = null;
		}
	}

	@Override
	protected boolean inPredicateMode() {
		return !predicateStack.isEmpty();
	}

	private final boolean predicateHolds() {
		return predicateFulfilled;
	}

	private final boolean predicateEnd() {
		current = parsingStack.pop();
		if (current < tokens.size()) {
			lookAhead = tokens.get(current);
		} else {
			lookAhead = null;
		}
		boolean success = predicateFulfilled;
		predicateFulfilled = predicateStack.pop();
		return success;
	}

	private final void fail(String msg) {
		int offset = query.length();
		int length = -1;
		String tokenText = "";
		if (lookAhead != null) {
			offset = lookAhead.getOffset();
			length = lookAhead.getLength();
			tokenText = lookAhead.getValue();
		} else {
			tokenText = lookAhead(0).name();
		}
		ParsingException ex = new ParsingException(msg + " " + lookAhead(0),
				tokenText, offset, length, query);
		predicateFulfilled = false;
		if (getCurrentOffset() > farestOffset) {
			farestException = ex;
			farestOffset = getCurrentOffset();
		}
		throw ex;
	}

	private final String matchIdentifier() {
		if (lookAhead(0) == GreqlTokenType.IDENTIFIER) {
			String name = lookAhead.getValue();
			if (isValidIdentifier(name)) {
				match();
				return name;
			}
		}
		fail("expected identifier, but found");
		return null;
	}

	private final String matchSimpleName() {
		if (lookAhead(0) == GreqlTokenType.IDENTIFIER) {
			String name = lookAhead.getValue();
			if (isValidSimpleName(name)) {
				match();
				return name;
			}
		}
		fail("expected simple name, but found");
		return null;
	}

	private final void match(GreqlTokenType type) {
		if (lookAhead(0) == type) {
			match();
		} else {
			fail("expected " + type + ", but found");
		}
	}

	private static final boolean isValidName(GreqlTokenType token) {
		switch (token) {
		case MAP:
		case AS:
		case IMPORT:
		case IN:
		case SET:
		case LIST:
		case REC:
		case FROM:
		case WITH:
		case REPORT:
		case WHERE:
		case LET:
			return true;
		default:
			return false;
		}
	}

	private final String matchPackageName() {
		if (((lookAhead(0) == GreqlTokenType.IDENTIFIER) || isValidName(lookAhead(0)))
				&& isValidPackageName(getLookAheadValue(0))) {
			StringBuilder name = new StringBuilder();
			name.append(lookAhead.getValue());
			match();
			boolean ph = true;
			do {
				if (lookAhead(0) == GreqlTokenType.DOT) {
					if (((lookAhead(1) == GreqlTokenType.IDENTIFIER) || isValidName(lookAhead(1)))
							&& isValidPackageName(getLookAheadValue(1))) {
						ph = true;
						match(GreqlTokenType.DOT);
						name.append(".");
						name.append(lookAhead.getValue());
						match();
					} else {
						ph = false;
					}
				} else {
					ph = false;
				}
			} while (ph);
			return name.toString();
		}
		fail("Package or type name expected, but found");
		return null;
	}

	private String getLookAheadValue(int i) {
		if ((current + i) < tokens.size()) {
			Token t = tokens.get(current + i);
			return t.getValue();
		} else {
			return null;
		}
	}

	private final String matchQualifiedName() {
		StringBuilder name = new StringBuilder();
		predicateStart();
		try {
			matchPackageName();
			match(GreqlTokenType.DOT);
		} catch (ParsingException ex) {
		}
		if (predicateEnd()) {
			name.append(matchPackageName());
			name.append(".");
			match(GreqlTokenType.DOT);
		}
		name.append(matchSimpleName());
		return name.toString();
	}

	private final static boolean isValidPackageName(String s) {
		if ((s == null) || (s.length() == 0)) {
			return false;
		}
		char[] chars = s.toCharArray();
		if (!Character.isLetter(chars[0]) || !Character.isLowerCase(chars[0])
				|| (chars[0] > 127)) {
			return false;
		}
		for (int i = 1; i < chars.length; i++) {
			if (!(Character.isLowerCase(chars[i])
					|| Character.isDigit(chars[i]) || (chars[i] == '_'))
					|| (chars[i] > 127)) {
				return false;
			}
		}
		return true;
	}

	private final static boolean isValidSimpleName(String s) {
		if ((s == null) || (s.length() == 0)) {
			return false;
		}
		char[] chars = s.toCharArray();
		if (!Character.isLetter(chars[0]) || !Character.isUpperCase(chars[0])
				|| (chars[0] > 127)) {
			return false;
		}
		for (int i = 1; i < chars.length; i++) {
			if (chars[i] > 127) {
				return false;
			}
		}
		return true;
	}

	private final static boolean isValidIdentifier(String s) {
		if ((s == null) || (s.length() == 0)) {
			return false;
		}
		char[] chars = s.toCharArray();
		if (!Character.isJavaIdentifierStart(chars[0])) {
			return false;
		}
		for (int i = 1; i < chars.length; i++) {
			if (!Character.isJavaIdentifierPart(chars[i])) {
				return false;
			}
		}
		return true;
	}

	private final void parseQuery() {
		if (lookAhead(0) == GreqlTokenType.EOF) {
			return;
		}
		GreqlExpression rootExpr = graph.createGreqlExpression();
		rootExpr.set_importedTypes(parseImports());
		if (lookAhead(0) == GreqlTokenType.USING) {
			match();
			List<VertexPosition<Variable>> varList = parseVariableList();
			for (VertexPosition<Variable> var : varList) {
				IsBoundVarOf isVarOf = graph.createIsBoundVarOf(var.node,
						rootExpr);
				isVarOf.set_sourcePositions(createSourcePositionList(
						var.length, var.offset));
			}
			match(GreqlTokenType.COLON);
		}
		int offset = getCurrentOffset();
		Expression expr = parseExpression();
		if (expr == null) {
			return;
		}
		IsQueryExprOf e = graph.createIsQueryExprOf(expr, rootExpr);
		e.set_sourcePositions(createSourcePositionList(offset));
		if (lookAhead(0) == GreqlTokenType.STORE) {
			match();
			match(GreqlTokenType.AS);
			Identifier ident = graph.createIdentifier();
			offset = getCurrentOffset();
			ident.set_name(matchIdentifier());
			IsIdOfStoreClause isId = graph.createIsIdOfStoreClause(ident,
					rootExpr);
			isId.set_sourcePositions(createSourcePositionList(offset));
		}
		match(GreqlTokenType.EOF);
		testIllegalThisLiterals();
		removeUndefinedTableheaders();
		mergeVariablesInGreqlExpression(rootExpr);
	}

	private void removeUndefinedTableheaders() {
		// remove table headers of list comprehensions where
		// all headers are undefined
		COMPR: for (ComprehensionWithTableHeader compr : graph
				.getComprehensionWithTableHeaderVertices()) {
			for (IsTableHeaderOf th : compr.getIsTableHeaderOfIncidences()) {
				if (th.getThat().getAttributedElementClass() != UndefinedLiteral.VC) {
					continue COMPR;
				}
			}
			IsTableHeaderOf th = compr.getFirstIsTableHeaderOfIncidence();
			while (th != null) {
				IsTableHeaderOf next = th.getNextIsTableHeaderOfIncidence();
				th.delete();
				th = next;
			}
		}
		// remove orphaned UndefinedLiterals
		UndefinedLiteral ul = graph.getFirstUndefinedLiteral();
		while (ul != null) {
			UndefinedLiteral next = ul.getNextUndefinedLiteral();
			if (ul.getFirstIncidence() == null) {
				ul.delete();
			}
			ul = next;
		}
	}

	private final PSet<String> parseImports() {
		PSet<String> importedTypes = JGraLab.set();
		while (lookAhead(0) == GreqlTokenType.IMPORT) {
			match(GreqlTokenType.IMPORT);
			StringBuilder importedType = new StringBuilder();
			importedType.append(matchPackageName());
			match(GreqlTokenType.DOT);
			if (lookAhead(0) == GreqlTokenType.STAR) {
				match(GreqlTokenType.STAR);
				importedType.append(".*");
			} else {
				importedType.append(".");
				importedType.append(matchSimpleName());
			}
			importedTypes = importedTypes.plus(importedType.toString());
			match(GreqlTokenType.SEMI);
		}
		return importedTypes;
	}

	private final List<VertexPosition<Variable>> parseVariableList() {
		List<VertexPosition<Variable>> vlist = new ArrayList<VertexPosition<Variable>>();
		int offset = getCurrentOffset();
		vlist.add(new VertexPosition<Variable>(parseVariable(true),
				getLength(offset), offset));
		while (lookAhead(0) == GreqlTokenType.COMMA) {
			match();
			vlist.add(new VertexPosition<Variable>(parseVariable(true),
					getLength(offset), offset));
		}
		return vlist;
	}

	private final Variable parseVariable(boolean inDeclaration) {
		String varName = matchIdentifier();
		Variable var = null;
		if (!inPredicateMode()) {
			var = graph.createVariable();
			var.set_name(varName);
		}
		if (inDeclaration) {
			try {
				duringParsingvariableSymbolTable.insert(varName, var);
			} catch (DuplicateVariableException e) {
				if (!inPredicateMode()) {
					throw e;
				}
			}
		}
		return var;
	}

	private final Expression parseExpression() {
		int pos = alreadySucceeded(RuleEnum.EXPRESSION);
		if (skipRule(pos)) {
			return null;
		}
		Expression expr = parseSubgraphRestrictedExpression();
		ruleSucceeds(RuleEnum.EXPRESSION, pos);
		return expr;
	}

	private final SubgraphDefinition parseSubgraphDefinition() {
		SubgraphDefinition definition = null;
		int exprOffset = getCurrentOffset();
		Expression traversalContextExpr = parseExpression();
		if (!inPredicateMode()) {
			int exprLength = getLength(exprOffset);
			definition = graph.createExpressionDefinedSubgraph();
			IsSubgraphDefiningExpression isSubgraphDefExpr = graph
					.createIsSubgraphDefiningExpression(traversalContextExpr,
							(ExpressionDefinedSubgraph) definition);
			isSubgraphDefExpr.set_sourcePositions(createSourcePositionList(
					exprLength, exprOffset));
		}
		return definition;
	}

	private final Expression parseSubgraphRestrictedExpression() {
		int pos = alreadySucceeded(RuleEnum.SUBGRAPHRESTRICTEDEXPRESSION);
		if (skipRule(pos)) {
			return null;
		}
		Expression result = null;
		if (lookAhead(0) == GreqlTokenType.ON) {
			match();
			int offsetDef = getCurrentOffset();
			SubgraphDefinition subgraphDef = parseSubgraphDefinition();
			match(GreqlTokenType.COLON);
			int lengthDef = getLength(offsetDef);
			int offsetRestrExpr = getCurrentOffset();
			Expression restrictedExpr = parseWhereExpression();
			if (!inPredicateMode()) {
				int lengthRestrExpr = getLength(offsetRestrExpr);
				SubgraphRestrictedExpression subgraphRestrExpr = graph
						.createSubgraphRestrictedExpression();
				IsSubgraphDefinitionOf subgraphDefOf = graph
						.createIsSubgraphDefinitionOf(subgraphDef,
								subgraphRestrExpr);
				subgraphDefOf.set_sourcePositions(createSourcePositionList(
						lengthDef, offsetDef));
				IsExpressionOnSubgraph exprOnSubgraph = graph
						.createIsExpressionOnSubgraph(restrictedExpr,
								subgraphRestrExpr);
				exprOnSubgraph.set_sourcePositions(createSourcePositionList(
						lengthRestrExpr, offsetRestrExpr));
				result = subgraphRestrExpr;
			}
		} else {
			result = parseLetExpression();
		}
		ruleSucceeds(RuleEnum.SUBGRAPHRESTRICTEDEXPRESSION, pos);
		return result;
	}

	private final boolean tryMatch(GreqlTokenType type) {
		if (lookAhead(0) == type) {
			match();
			return true;
		}
		return false;
	}

	private final Quantifier parseQuantifier() {
		QuantificationType type = null;
		if (tryMatch(GreqlTokenType.FORALL)) {
			type = QuantificationType.FORALL;
		} else if (tryMatch(GreqlTokenType.EXISTS_ONE)) {
			type = QuantificationType.EXISTSONE;
		} else if (tryMatch(GreqlTokenType.EXISTS)) {
			type = QuantificationType.EXISTS;
		}
		if (type != null) {
			if (!inPredicateMode()) {
				for (Quantifier quantifier : graph.getQuantifierVertices()) {
					if (quantifier.get_type() == type) {
						return quantifier;
					}
				}
				Quantifier quantifier = graph.createQuantifier();
				quantifier.set_type(type);
				return quantifier;
			}
			return null;
		} else {
			fail("Expected a quantifier");
			return null;
		}
	}

	private final Expression parseQuantifiedExpression() {
		if ((lookAhead(0) == GreqlTokenType.EXISTS)
				|| (lookAhead(0) == GreqlTokenType.EXISTS_ONE)
				|| (lookAhead(0) == GreqlTokenType.FORALL)) {
			int offsetQuantifier = getCurrentOffset();
			int offsetQuantifiedDecl = 0;
			int offsetQuantifiedExpr = 0;
			int lengthQuantifier = 0;
			int lengthQuantifiedDecl = 0;
			int lengthQuantifiedExpr = 0;
			Quantifier quantifier = parseQuantifier();
			lengthQuantifier = getLength(offsetQuantifier);
			offsetQuantifiedDecl = getCurrentOffset();
			duringParsingvariableSymbolTable.blockBegin();
			Declaration decl = parseQuantifiedDeclaration();
			lengthQuantifiedDecl = getLength(offsetQuantifiedDecl);
			match(GreqlTokenType.AT);
			offsetQuantifiedExpr = getCurrentOffset();
			Expression boundExpr = parseSubgraphRestrictedExpression();
			lengthQuantifiedExpr = getLength(offsetQuantifiedExpr);
			QuantifiedExpression quantifiedExpr = null;
			if (!inPredicateMode()) {
				quantifiedExpr = graph.createQuantifiedExpression();
				IsQuantifierOf quantifierOf = graph.createIsQuantifierOf(
						quantifier, quantifiedExpr);
				quantifierOf.set_sourcePositions(createSourcePositionList(
						lengthQuantifier, offsetQuantifier));
				// add declaration
				IsQuantifiedDeclOf quantifiedDeclOf = graph
						.createIsQuantifiedDeclOf(decl, quantifiedExpr);
				quantifiedDeclOf.set_sourcePositions(createSourcePositionList(
						lengthQuantifiedDecl, offsetQuantifiedDecl));
				// add predicate
				IsBoundExprOf boundExprOf = graph
						.createIsBoundExprOfQuantifiedExpression(boundExpr,
								quantifiedExpr);
				boundExprOf.set_sourcePositions(createSourcePositionList(
						lengthQuantifiedExpr, offsetQuantifiedExpr));
			}
			duringParsingvariableSymbolTable.blockEnd();
			return quantifiedExpr;
		} else {
			return parseConditionalExpression();
		}
	}

	private final Expression parseLetExpression() {
		if (lookAhead(0) == GreqlTokenType.LET) {
			match();
			duringParsingvariableSymbolTable.blockBegin();
			List<VertexPosition<Definition>> defList = parseDefinitionList();
			match(GreqlTokenType.IN);
			int offset = getCurrentOffset();
			Expression boundExpr = parseLetExpression();
			LetExpression result = null;
			if (!inPredicateMode() && !defList.isEmpty()) {
				int length = getLength(offset);
				result = graph.createLetExpression();
				IsBoundExprOf exprOf = graph.createIsBoundExprOfDefinition(
						boundExpr, result);
				exprOf.set_sourcePositions(createSourcePositionList(length,
						offset));
				for (VertexPosition<Definition> def : defList) {
					IsDefinitionOf definitionOf = graph.createIsDefinitionOf(
							def.node, result);
					definitionOf.set_sourcePositions(createSourcePositionList(
							def.length, def.offset));
				}
			}
			duringParsingvariableSymbolTable.blockEnd();
			return result;
		} else {
			return parseWhereExpression();
		}
	}

	private final Expression parseWhereExpression() {
		int offset = getCurrentOffset();
		Expression expr = parseQuantifiedExpression();
		if (tryMatch(GreqlTokenType.WHERE)) {
			int length = getLength(offset);
			List<VertexPosition<Definition>> defList = parseDefinitionList();
			WhereExpression result = null;
			if (!inPredicateMode()) {
				result = graph.createWhereExpression();
				IsBoundExprOf exprOf = graph.createIsBoundExprOfDefinition(
						expr, result);
				exprOf.set_sourcePositions(createSourcePositionList(length,
						offset));
				for (VertexPosition<Definition> def : defList) {
					IsDefinitionOf isDefOf = graph.createIsDefinitionOf(
							def.node, result);
					isDefOf.set_sourcePositions(createSourcePositionList(
							length, offset));
				}
			}
			return result;
		} else {
			return expr;
		}
	}

	private final List<VertexPosition<Definition>> parseDefinitionList() {
		List<VertexPosition<Definition>> definitions = null;
		if (!inPredicateMode()) {
			definitions = new ArrayList<VertexPosition<Definition>>();
		}
		do {
			int offset = getCurrentOffset();
			Definition v = parseDefinition();
			int length = getLength(offset);
			if (!inPredicateMode()) {
				definitions.add(new VertexPosition<Definition>(v, offset,
						length));
			}
		} while (tryMatch(GreqlTokenType.COMMA));
		return definitions;
	}

	private final Definition parseDefinition() {
		int offsetVar = getCurrentOffset();
		Variable var = parseVariable(true);
		int lengthVar = getLength(offsetVar);
		match(GreqlTokenType.ASSIGN);
		int offsetExpr = getCurrentOffset();
		Expression expr = parseExpression();
		int lengthExpr = getLength(offsetExpr);
		if (!inPredicateMode()) {
			Definition definition = graph.createDefinition();
			IsVarOf varOf = graph.createIsVarOf(var, definition);
			varOf.set_sourcePositions(createSourcePositionList(lengthVar,
					offsetVar));
			IsExprOf exprOf = graph.createIsExprOf(expr, definition);
			exprOf.set_sourcePositions(createSourcePositionList(lengthExpr,
					offsetExpr));
			return definition;
		}
		return null;
	}

	/**
	 * matches conditional expressions
	 * 
	 * @return
	 */
	private final Expression parseConditionalExpression() {
		int offsetExpr = getCurrentOffset();
		Expression result = parseOrExpression();
		int lengthExpr = getLength(offsetExpr);
		if (tryMatch(GreqlTokenType.QUESTION)) {
			int offsetTrueExpr = getCurrentOffset();
			Expression trueExpr = parseConditionalExpression();
			int lengthTrueExpr = getLength(offsetTrueExpr);
			match(GreqlTokenType.COLON);
			int offsetFalseExpr = getCurrentOffset();
			Expression falseExpr = parseConditionalExpression();
			int lengthFalseExpr = getLength(offsetFalseExpr);
			if (!inPredicateMode()) {
				ConditionalExpression condExpr = graph
						.createConditionalExpression();
				// add condition
				IsConditionOf conditionOf = graph.createIsConditionOf(result,
						condExpr);
				conditionOf.set_sourcePositions(createSourcePositionList(
						lengthExpr, offsetExpr));
				// add true-expression
				IsTrueExprOf trueExprOf = graph.createIsTrueExprOf(trueExpr,
						condExpr);
				trueExprOf.set_sourcePositions(createSourcePositionList(
						lengthTrueExpr, offsetTrueExpr));
				// add false-expression
				IsFalseExprOf falseExprOf = graph.createIsFalseExprOf(
						falseExpr, condExpr);
				falseExprOf.set_sourcePositions(createSourcePositionList(
						lengthFalseExpr, offsetFalseExpr));
				result = condExpr;
			}
		}
		return result;
	}

	private final Expression parseOrExpression() {
		FunctionConstruct construct = new FunctionConstruct();
		construct.preArg1();
		Expression expr = parseXorExpression();
		construct.preOp(expr);
		if (tryMatch(GreqlTokenType.OR)) {
			construct.postOp("or");
			return construct.postArg2(parseOrExpression());
		}
		return expr;
	}

	private final Expression parseXorExpression() {
		FunctionConstruct construct = new FunctionConstruct();
		construct.preArg1();
		Expression expr = parseAndExpression();
		construct.preOp(expr);
		if (tryMatch(GreqlTokenType.XOR)) {
			construct.postOp("xor");
			return construct.postArg2(parseXorExpression());
		}
		return expr;
	}

	private final Expression parseAndExpression() {
		FunctionConstruct construct = new FunctionConstruct();
		construct.preArg1();
		Expression expr = parseEqualityExpression();
		construct.preOp(expr);
		if (tryMatch(GreqlTokenType.AND)) {
			construct.postOp("and");
			return construct.postArg2(parseAndExpression());
		}
		return expr;
	}

	private final Expression parseEqualityExpression() {
		FunctionConstruct construct = new FunctionConstruct();
		construct.preArg1();
		Expression expr = parseRelationalExpression();
		construct.preOp(expr);
		if (tryMatch(GreqlTokenType.EQUAL)) {
			construct.postOp("equals");
			return construct.postArg2(parseEqualityExpression());
		} else if (tryMatch(GreqlTokenType.NOT_EQUAL)) {
			construct.postOp("nequals");
			return construct.postArg2(parseEqualityExpression());
		}
		return expr;
	}

	private final Expression parseRelationalExpression() {
		FunctionConstruct construct = new FunctionConstruct();
		construct.preArg1();
		Expression expr = parseAdditiveExpression();
		construct.preOp(expr);
		String name = null;
		if (tryMatch(GreqlTokenType.L_T)) {
			name = "leThan";
		} else if (tryMatch(GreqlTokenType.LE)) {
			name = "leEqual";
		} else if (tryMatch(GreqlTokenType.GE)) {
			name = "grEqual";
		} else if (tryMatch(GreqlTokenType.G_T)) {
			name = "grThan";
		} else if (tryMatch(GreqlTokenType.MATCH)) {
			name = "reMatch";
		}
		if (name != null) {
			construct.postOp(name);
			return construct.postArg2(parseRelationalExpression());
		}
		return expr;
	}

	private final Expression parseAdditiveExpression() {
		FunctionConstruct construct = null;
		String name = null;
		Expression expr = null;
		do {
			if (construct == null) {
				construct = new FunctionConstruct();
				construct.preArg1();
				expr = parseMultiplicativeExpression();
			} else {
				construct = new FunctionConstruct(construct);
			}
			name = null;
			construct.preOp(expr);
			if (tryMatch(GreqlTokenType.PLUS)) {
				name = "add";
			} else if (tryMatch(GreqlTokenType.MINUS)) {
				name = "sub";
			} else if (tryMatch(GreqlTokenType.PLUSPLUS)) {
				name = "concat";
			}
			if (name != null) {
				construct.postOp(name);
				expr = construct.postArg2(parseMultiplicativeExpression());
			}
		} while (name != null);
		return expr;
	}

	private final Expression parseMultiplicativeExpression() {
		FunctionConstruct construct = null;
		String name = null;
		Expression expr = null;
		do {
			if (construct == null) {
				construct = new FunctionConstruct();
				construct.preArg1();
				expr = parseUnaryExpression();
			} else {
				construct = new FunctionConstruct(construct);
			}
			name = null;
			construct.preOp(expr);
			if (tryMatch(GreqlTokenType.STAR)) {
				name = "mul";
			} else if (tryMatch(GreqlTokenType.MOD)) {
				name = "mod";
			} else if (tryMatch(GreqlTokenType.DIV)) {
				name = "div";
			}
			if (name != null) {
				construct.postOp(name);
				expr = construct.postArg2(parseUnaryExpression());
			}
		} while (name != null);
		return expr;
	}

	private final Expression parseUnaryExpression() {
		FunctionConstruct construct = null;
		if ((lookAhead(0) == GreqlTokenType.NOT)
				|| (lookAhead(0) == GreqlTokenType.MINUS)) {
			construct = new FunctionConstruct();
			construct.preUnaryOp();
			String opName = null;
			if (tryMatch(GreqlTokenType.NOT)) {
				opName = "not";
			} else if (tryMatch(GreqlTokenType.MINUS)) {
				opName = "neg";
			}
			if (!inPredicateMode()) {
				getFunctionId(opName);
			}
			construct.postOp(opName);
		}
		Expression expr = parsePathExpression();
		if (construct != null) {
			return construct.postArg2(expr);
		}
		return expr;
	}

	private final RoleId parseRoleId() {
		String ident = matchIdentifier();
		if (!inPredicateMode()) {
			RoleId roleId = graph.createRoleId();
			roleId.set_name(ident);
			return roleId;
		}
		return null;
	}

	private final Identifier parseIdentifier() {
		String name = matchIdentifier();
		if (!inPredicateMode()) {
			Identifier ident = graph.createIdentifier();
			ident.set_name(name);
			return ident;
		}
		return null;
	}

	private final Expression parseValueAccess() {
		int offset = getCurrentOffset();
		Expression expr = parsePrimaryExpression();
		int length = getLength(offset);
		boolean secondPart = false;
		if (lookAhead(0) == GreqlTokenType.DOT) {
			secondPart = true;
		}
		if (lookAhead(0) == GreqlTokenType.LBRACK) {
			predicateStart();
			try {
				match(GreqlTokenType.LBRACK);
				parsePrimaryPathDescription(); // TODO: pathDescription statt
				// PrimaryPathDescription?
			} catch (ParsingException ex) {
			}
			if (!predicateEnd()) {
				secondPart = true;
			}
		}
		if (secondPart) {
			return parseValueAccess2(expr, offset, length);
		}
		return expr;
	}

	private final Expression parseValueAccess2(Expression arg1, int offsetArg1,
			int lengthArg1) {
		String name = "get";
		int offsetOperator = getCurrentOffset();
		int lengthOperator = 0;
		int lengthArg2 = 0;
		int offsetArg2 = 0;
		Expression arg2 = null;
		if (tryMatch(GreqlTokenType.DOT)) {
			name = "getValue";
			lengthOperator = 1;
			offsetArg2 = getCurrentOffset();
			arg2 = parseIdentifier();
		} else if (tryMatch(GreqlTokenType.LBRACK)) {
			offsetArg2 = getCurrentOffset();
			arg2 = parseExpression();
			lengthArg2 = getLength(offsetArg2);
			match(GreqlTokenType.RBRACK);
			lengthOperator = getLength(offsetOperator);
		}
		Expression result = null;
		if (!inPredicateMode()) {
			result = createFunctionIdAndArgumentOf(getFunctionId(name),
					offsetOperator, lengthOperator, arg1, offsetArg1,
					lengthArg1, arg2, offsetArg2, lengthArg2, true);
		}
		boolean secondPart = false;
		if (lookAhead(0) == GreqlTokenType.DOT) {
			secondPart = true;
		}
		if (lookAhead(0) == GreqlTokenType.LBRACK) {
			predicateStart();
			try {
				match(GreqlTokenType.LBRACK);
				parsePrimaryPathDescription(); // TODO: pathDescription statt
				// PrimaryPathDescription?
			} catch (ParsingException ex) {
			}
			if (!predicateEnd()) {
				secondPart = true;
			}
		}
		if (secondPart) {
			return parseValueAccess2(result, offsetArg1, getLength(offsetArg2));
		}
		return result;
	}

	private final Expression parsePrimaryExpression() {
		if (lookAhead(0) == GreqlTokenType.LPAREN) {
			return parseParenthesedExpression();
		}

		if ((lookAhead(0) == GreqlTokenType.V) || (lookAhead(0) == GreqlTokenType.E)) {
			return parseRangeExpression();
		}

		predicateStart();
		try {
			parseAltPathDescription();
		} catch (ParsingException ex) {
		}
		if (predicateEnd()) {
			return parseAltPathDescription();
		}

		if (((lookAhead(0) == GreqlTokenType.IDENTIFIER)
				|| (lookAhead(0) == GreqlTokenType.AND)
				|| (lookAhead(0) == GreqlTokenType.NOT)
				|| (lookAhead(0) == GreqlTokenType.XOR) || (lookAhead(0) == GreqlTokenType.OR))
				&& ((lookAhead(1) == GreqlTokenType.LCURLY) || (lookAhead(1) == GreqlTokenType.LPAREN))) {
			predicateStart();
			try {
				parseFunctionApplication();
			} catch (ParsingException ex) {
			}
			if (predicateEnd()) {
				return parseFunctionApplication();
			}
		}

		predicateStart();
		try {
			parseValueConstruction();
		} catch (ParsingException ex) {
		}
		if (predicateEnd()) {
			return parseValueConstruction();
		}
		// System.out.println("LA1: " + lookAhead.getValue());
		predicateStart();
		try {
			parseVariable(false);
		} catch (ParsingException ex) {
		}
		if (predicateEnd()) {
			// System.out.println("LA2: " + lookAhead.getValue());
			return parseVariable(false);
		}
		predicateStart();
		try {
			parseLiteral();
		} catch (ParsingException ex) {
		}
		if (predicateEnd()) {
			return parseLiteral();
		}
		if (lookAhead(0) == GreqlTokenType.FROM) {
			return parseFWRExpression();
		}
		fail("Unrecognized token");
		return null;
	}

	private final Expression parseParenthesedExpression() {
		predicateStart();
		try {
			parseAltPathDescription();
		} catch (ParsingException ex) {
		}
		if (predicateEnd()) {
			Expression expr = parseAltPathDescription();
			return expr;
		}
		match(GreqlTokenType.LPAREN);
		Expression expr = parseExpression();
		match(GreqlTokenType.RPAREN);
		return expr;
	}

	private final PathDescription parseAltPathDescription() {
		int pos = alreadySucceeded(RuleEnum.ALTERNATIVE_PATH_DESCRIPTION);
		if (skipRule(pos)) {
			return null;
		}
		int offsetPart1 = getCurrentOffset();
		PathDescription part1 = parseIntermediateVertexPathDescription();
		int lengthPart1 = getLength(offsetPart1);
		if (tryMatch(GreqlTokenType.BOR)) {
			int offsetPart2 = getCurrentOffset();
			PathDescription part2 = parseAltPathDescription();
			int lengthPart2 = getLength(offsetPart2);
			if (!inPredicateMode()) {
				part1 = addPathElement(AlternativePathDescription.VC,
						IsAlternativePathOf.EC, null, part1, offsetPart1,
						lengthPart1, part2, offsetPart2, lengthPart2);
			}
		}
		ruleSucceeds(RuleEnum.ALTERNATIVE_PATH_DESCRIPTION, pos);
		return part1;
	}

	private final PathDescription parseIntermediateVertexPathDescription() {
		int offsetPart1 = getCurrentOffset();
		PathDescription part1 = parseSequentialPathDescription();
		int lengthPart1 = getLength(offsetPart1);
		predicateStart();
		try {
			parseValueAccess();
			if (predicateHolds()) {
				parseSequentialPathDescription();
			}
		} catch (ParsingException ex) {
		}
		if (predicateEnd()) {
			int offsetExpr = getCurrentOffset();
			Expression restrExpr = parseValueAccess();
			int lengthExpr = getLength(offsetExpr);
			int offsetPart2 = getCurrentOffset();
			PathDescription part2 = parseIntermediateVertexPathDescription();
			int lengthPart2 = getLength(offsetPart2);
			IntermediateVertexPathDescription result = null;
			if (!inPredicateMode()) {
				result = (IntermediateVertexPathDescription) addPathElement(
						IntermediateVertexPathDescription.VC, IsSubPathOf.EC,
						null, part1, offsetPart1, lengthPart1, part2,
						offsetPart2, lengthPart2);
				IsIntermediateVertexOf intermediateVertexOf = graph
						.createIsIntermediateVertexOf(restrExpr, result);
				intermediateVertexOf
						.set_sourcePositions(createSourcePositionList(
								lengthExpr, offsetExpr));
			}
			return result;
		}
		return part1;
	}

	private final PathDescription parseSequentialPathDescription() {
		int offsetPart1 = getCurrentOffset();
		PathDescription part1 = parseStartRestrictedPathDescription();
		int lengthPart1 = getLength(offsetPart1);
		predicateStart();
		try {
			if (lookAhead(0) != GreqlTokenType.EOF) {
				parseSequentialPathDescription();
			} else {
				fail("Found EOF");
			}
		} catch (ParsingException ex) {
		}
		if (predicateEnd()) {
			int offsetPart2 = getCurrentOffset();
			PathDescription part2 = parseSequentialPathDescription();
			int lengthPart2 = getLength(offsetPart2);
			if (!inPredicateMode()) {
				return addPathElement(SequentialPathDescription.VC,
						IsSequenceElementOf.EC, null, part1, offsetPart1,
						lengthPart1, part2, offsetPart2, lengthPart2);
			} else {
				return null;
			}
		}
		return part1;
	}

	private final PathDescription parseStartRestrictedPathDescription() {
		int offsetRest = getCurrentOffset();
		List<VertexPosition<? extends TypeOrRoleId>> typeIds = null;
		Expression expr = null;
		int lengthRestr = 0;
		int offsetRestr = 0;
		if (tryMatch(GreqlTokenType.LCURLY)) {
			predicateStart();
			try {
				parseTypeAndRoleExpressionList();
			} catch (ParsingException ex) {
			}
			if (predicateEnd()) {
				typeIds = parseTypeAndRoleExpressionList();
			}
			if (tryMatch(GreqlTokenType.AT)) {
				offsetRestr = getCurrentOffset();
				expr = parseExpression();
				lengthRestr = getLength(offsetRestr);
			}
			match(GreqlTokenType.RCURLY);
			match(GreqlTokenType.AMP);
		}
		PathDescription pathDescr = parseGoalRestrictedPathDescription();
		if (!inPredicateMode()) {
			if (expr != null) {
				IsStartRestrOf startRestrOf = graph.createIsStartRestrOf(expr,
						pathDescr);
				startRestrOf.set_sourcePositions(createSourcePositionList(
						lengthRestr, offsetRest));
			}
			if (typeIds != null) {
				for (VertexPosition<? extends TypeOrRoleId> t : typeIds) {
					IsStartRestrOf startRestrOf = graph.createIsStartRestrOf(
							t.node, pathDescr);
					startRestrOf.set_sourcePositions(createSourcePositionList(
							t.length, t.offset));
				}
			}
		}
		return pathDescr;
	}

	private final PathDescription parseGoalRestrictedPathDescription() {
		PathDescription pathDescr = parseIteratedOrTransposedPathDescription();
		if (tryMatch(GreqlTokenType.AMP)) {
			match(GreqlTokenType.LCURLY);
			predicateStart();
			try {
				parseTypeAndRoleExpressionList();
			} catch (ParsingException ex) {
			}
			if (predicateEnd()) {
				List<VertexPosition<? extends TypeOrRoleId>> typeIds = parseTypeAndRoleExpressionList();
				if (!inPredicateMode()) {
					for (VertexPosition<? extends TypeOrRoleId> t : typeIds) {
						IsGoalRestrOf goalRestrOf = graph.createIsGoalRestrOf(
								t.node, pathDescr);
						goalRestrOf
								.set_sourcePositions(createSourcePositionList(
										t.length, t.offset));
					}
				}
			}
			if (tryMatch(GreqlTokenType.AT)) {
				int offset = getCurrentOffset();
				Expression expr = parseExpression();
				int length = getLength(offset);
				if (!inPredicateMode()) {
					IsGoalRestrOf goalRestrOf = graph.createIsGoalRestrOf(expr,
							pathDescr);
					goalRestrOf.set_sourcePositions(createSourcePositionList(
							length, offset));
				}
			}
			match(GreqlTokenType.RCURLY);
		}
		return pathDescr;
	}

	private final PathDescription parseIteratedOrTransposedPathDescription() {
		int offsetPath = getCurrentOffset();
		PathDescription pathDescr = parsePrimaryPathDescription();
		int lengthPath = getLength(offsetPath);
		if ((lookAhead(0) == GreqlTokenType.STAR)
				|| (lookAhead(0) == GreqlTokenType.PLUS)
				|| (lookAhead(0) == GreqlTokenType.TRANSPOSED)
				|| (lookAhead(0) == GreqlTokenType.CARET)) {
			return parseIteration(pathDescr, offsetPath, lengthPath);
		}
		return pathDescr;
	}

	private final PathDescription parseIteration(PathDescription iteratedPath,
			int offsetPath, int lengthPath) {
		IterationType iteration = null;
		PathDescription result = null;
		if (tryMatch(GreqlTokenType.STAR)) {
			iteration = IterationType.STAR;
		} else if (tryMatch(GreqlTokenType.PLUS)) {
			iteration = IterationType.PLUS;
		}
		if (iteration != null) {
			if (!inPredicateMode()) {
				IteratedPathDescription ipd = graph
						.createIteratedPathDescription();
				ipd.set_times(iteration);
				IsIteratedPathOf iteratedPathOf = graph.createIsIteratedPathOf(
						iteratedPath, ipd);
				iteratedPathOf.set_sourcePositions(createSourcePositionList(
						lengthPath, offsetPath));
				result = ipd;
			}
		} else if (tryMatch(GreqlTokenType.TRANSPOSED)) {
			if (!inPredicateMode()) {
				TransposedPathDescription tpd = graph
						.createTransposedPathDescription();
				IsTransposedPathOf transposedPathOf = graph
						.createIsTransposedPathOf(iteratedPath, tpd);
				transposedPathOf.set_sourcePositions(createSourcePositionList(
						lengthPath, offsetPath));
				result = tpd;
			}
		} else if (tryMatch(GreqlTokenType.CARET)) {
			int offsetExpr = getCurrentOffset();
			Expression ie = parseNumericLiteral();
			if (!inPredicateMode()) {
				if (!(ie instanceof IntLiteral)) {
					fail("Expected integer constant as iteration quantifier or T, but found");
				}
				int lengthExpr = getLength(offsetExpr);
				ExponentiatedPathDescription epd = graph
						.createExponentiatedPathDescription();
				IsExponentiatedPathOf exponentiatedPathOf = graph
						.createIsExponentiatedPathOf(iteratedPath, epd);
				exponentiatedPathOf
						.set_sourcePositions(createSourcePositionList(
								lengthPath, offsetPath));
				IsExponentOf exponentOf = graph.createIsExponentOf(
						(IntLiteral) ie, epd);
				exponentOf.set_sourcePositions(createSourcePositionList(
						lengthExpr, offsetExpr));
				result = epd;
			}
		} else {
			fail("No iteration or transposition at iterated path description");
		}
		if ((lookAhead(0) == GreqlTokenType.STAR)
				|| (lookAhead(0) == GreqlTokenType.PLUS)
				|| (lookAhead(0) == GreqlTokenType.CARET)) {
			return parseIteration(result, offsetPath, getLength(offsetPath));
		}
		return result;
	}

	private final PathDescription parsePrimaryPathDescription() {
		if (lookAhead(0) == GreqlTokenType.LPAREN) {
			predicateStart();
			try {
				match(GreqlTokenType.LPAREN);
				parseAltPathDescription();
			} catch (ParsingException ex) {
			}
			if (predicateEnd()) {
				match(GreqlTokenType.LPAREN);
				PathDescription pathDescr = parseAltPathDescription();
				match(GreqlTokenType.RPAREN);
				return pathDescr;
			}
		}
		if ((lookAhead(0) == GreqlTokenType.OUTAGGREGATION)
				|| (lookAhead(0) == GreqlTokenType.INAGGREGATION)) {
			return parseAggregationPathDescription();
		}
		if ((lookAhead(0) == GreqlTokenType.RARROW)
				|| (lookAhead(0) == GreqlTokenType.LARROW)
				|| (lookAhead(0) == GreqlTokenType.ARROW)) {
			return parseSimplePathDescription();
		}
		if ((lookAhead(0) == GreqlTokenType.EDGESTART)
				|| (lookAhead(0) == GreqlTokenType.EDGEEND)
				|| (lookAhead(0) == GreqlTokenType.EDGE)) {
			return parseEdgePathDescription();
		}
		if (tryMatch(GreqlTokenType.LBRACK)) {
			int offset = getCurrentOffset();
			PathDescription pathDescr = parseAltPathDescription();
			int length = getLength(offset);
			match(GreqlTokenType.RBRACK);
			if (!inPredicateMode()) {
				OptionalPathDescription optPathDescr = graph
						.createOptionalPathDescription();
				IsOptionalPathOf optionalPathOf = graph.createIsOptionalPathOf(
						pathDescr, optPathDescr);
				optionalPathOf.set_sourcePositions(createSourcePositionList(
						length, offset));
				return optPathDescr;
			}
			return null;
		}
		fail("Unrecognized token");
		return null;
	}

	private final PrimaryPathDescription parseSimplePathDescription() {
		Direction dir = null;
		EdgeRestriction edgeRestr = null;
		GReQLDirection direction = GReQLDirection.INOUT;
		int offsetDir = getCurrentOffset();
		int offsetEdgeRestr = 0;
		int lengthEdgeRestr = 0;
		if (tryMatch(GreqlTokenType.RARROW)) {
			direction = GReQLDirection.OUT;
		} else if (tryMatch(GreqlTokenType.LARROW)) {
			direction = GReQLDirection.IN;
		} else {
			match(GreqlTokenType.ARROW);
		}
		if (tryMatch(GreqlTokenType.LCURLY)) {
			offsetEdgeRestr = getCurrentOffset();
			edgeRestr = parseEdgeRestriction();
			lengthEdgeRestr = getLength(offsetEdgeRestr);
			match(GreqlTokenType.RCURLY);
		}
		if (!inPredicateMode()) {
			PrimaryPathDescription result = graph.createSimplePathDescription();
			dir = (Direction) graph.getFirstVertex(Direction.VC);
			while (dir != null) {
				if (!dir.get_dirValue().equals(direction)) {
					dir = dir.getNextDirection();
				} else {
					break;
				}
			}
			if (dir == null) {
				dir = graph.createDirection();
				dir.set_dirValue(direction);
			}
			IsDirectionOf directionOf = graph.createIsDirectionOf(dir, result);
			directionOf.set_sourcePositions(createSourcePositionList(0,
					offsetDir));
			if (edgeRestr != null) {
				IsEdgeRestrOf edgeRestrOf = graph.createIsEdgeRestrOf(
						edgeRestr, result);
				edgeRestrOf.set_sourcePositions(createSourcePositionList(
						lengthEdgeRestr, offsetEdgeRestr));
			}
			return result;
		}
		return null;
	}

	private final PrimaryPathDescription parseAggregationPathDescription() {
		boolean outAggregation = true;
		EdgeRestriction edgeRestr = null;
		int restrOffset = 0;
		int restrLength = 0;
		if (tryMatch(GreqlTokenType.INAGGREGATION)) {
			outAggregation = false;
		} else {
			match(GreqlTokenType.OUTAGGREGATION);
		}
		if (tryMatch(GreqlTokenType.LCURLY)) {
			restrOffset = getCurrentOffset();
			edgeRestr = parseEdgeRestriction();
			restrLength = getLength(restrOffset);
			match(GreqlTokenType.RCURLY);
		}
		if (!inPredicateMode()) {
			AggregationPathDescription result = graph
					.createAggregationPathDescription();
			result.set_outAggregation(outAggregation);
			if (edgeRestr != null) {
				IsEdgeRestrOf edgeRestrOf = graph.createIsEdgeRestrOf(
						edgeRestr, result);
				edgeRestrOf.set_sourcePositions(createSourcePositionList(
						restrLength, restrOffset));
			}
			return result;
		}
		return null;
	}

	private final EdgePathDescription parseEdgePathDescription() {
		Direction dir = null;
		boolean edgeStart = false;
		boolean edgeEnd = false;
		GReQLDirection direction = GReQLDirection.INOUT;
		int offsetDir = getCurrentOffset();
		if (tryMatch(GreqlTokenType.EDGESTART)) {
			edgeStart = true;
		} else {
			match(GreqlTokenType.EDGE);
		}
		int offsetExpr = getCurrentOffset();
		Expression expr = parseExpression();
		int lengthExpr = getLength(offsetExpr);

		if (tryMatch(GreqlTokenType.EDGEEND)) {
			edgeEnd = true;
		} else {
			match(GreqlTokenType.EDGE);
		}

		if (!inPredicateMode()) {
			int lengthDir = getLength(offsetDir);
			EdgePathDescription result = graph.createEdgePathDescription();
			if (edgeStart && !edgeEnd) {
				direction = GReQLDirection.IN;
			} else if (!edgeStart && edgeEnd) {
				direction = GReQLDirection.OUT;
			}
			dir = (Direction) graph.getFirstVertex(Direction.VC);
			while (dir != null) {
				if (!dir.get_dirValue().equals(direction)) {
					dir = dir.getNextDirection();
				} else {
					break;
				}
			}
			if (dir == null) {
				dir = graph.createDirection();
				dir.set_dirValue(direction);
			}
			IsDirectionOf directionOf = graph.createIsDirectionOf(dir, result);
			directionOf.set_sourcePositions(createSourcePositionList(lengthDir,
					offsetDir));
			IsEdgeExprOf edgeExprOf = graph.createIsEdgeExprOf(expr, result);
			edgeExprOf.set_sourcePositions(createSourcePositionList(lengthExpr,
					offsetExpr));
			return result;
		}
		return null;
	}

	private final FunctionApplication parseFunctionApplication() {
		List<VertexPosition<TypeId>> typeIds = null;
		if (((lookAhead(0) == GreqlTokenType.IDENTIFIER)
				|| (lookAhead(0) == GreqlTokenType.AND)
				|| (lookAhead(0) == GreqlTokenType.NOT)
				|| (lookAhead(0) == GreqlTokenType.XOR) || (lookAhead(0) == GreqlTokenType.OR))
				&& isFunctionName(lookAhead.getValue())
				&& ((lookAhead(1) == GreqlTokenType.LCURLY) || (lookAhead(1) == GreqlTokenType.LPAREN))) {
			int offset = getCurrentOffset();
			String name = lookAhead.getValue();
			match();
			int length = getLength(offset);
			if (tryMatch(GreqlTokenType.LCURLY)) {
				typeIds = parseTypeExpressionList();
				match(GreqlTokenType.RCURLY);
			}
			match(GreqlTokenType.LPAREN);
			List<VertexPosition<Expression>> expressions = null;
			if (lookAhead(0) != GreqlTokenType.RPAREN) {
				expressions = parseExpressionList(GreqlTokenType.COMMA);
			}
			match(GreqlTokenType.RPAREN);
			if (!inPredicateMode()) {
				FunctionApplication funApp = graph.createFunctionApplication();
				// retrieve function id or create a new one
				FunctionId functionId = getFunctionId(name);
				IsFunctionIdOf functionIdOf = graph.createIsFunctionIdOf(
						functionId, funApp);
				functionIdOf.set_sourcePositions(createSourcePositionList(
						length, offset));
				if (typeIds != null) {
					for (VertexPosition<TypeId> t : typeIds) {
						IsTypeExprOf typeOf = graph.createIsTypeExprOfFunction(
								t.node, funApp);
						typeOf.set_sourcePositions(createSourcePositionList(
								t.length, t.offset));
					}
				}
				if (expressions != null) {
					for (VertexPosition<Expression> ex : expressions) {
						IsArgumentOf argOf = graph.createIsArgumentOf(ex.node,
								funApp);
						argOf.set_sourcePositions(createSourcePositionList(
								ex.length, ex.offset));
					}
				}
				return funApp;
			}
			return null;
		}
		fail("No function application");
		return null;
	}

	private final Expression parseValueConstruction() {
		if (lookAhead(0) != null) {
			switch (lookAhead(0)) {
			case REC:
				return parseRecordConstruction();
			case MAP:
				return parseMapConstruction();
			case LIST:
				return parseListConstruction();
			case SET:
				match();
				match(GreqlTokenType.LPAREN);
				if (tryMatch(GreqlTokenType.RPAREN)) {
					return graph.createSetConstruction();
				}
				List<VertexPosition<Expression>> expressions = parseExpressionList(GreqlTokenType.COMMA);
				match(GreqlTokenType.RPAREN);
				if (!inPredicateMode()) {
					return createPartsOfValueConstruction(expressions,
							graph.createSetConstruction());
				} else {
					return null;
				}
			case TUP:
				match();
				match(GreqlTokenType.LPAREN);
				if (tryMatch(GreqlTokenType.RPAREN)) {
					return graph.createTupleConstruction();
				}
				expressions = parseExpressionList(GreqlTokenType.COMMA);
				match(GreqlTokenType.RPAREN);
				if (!inPredicateMode()) {
					return createPartsOfValueConstruction(expressions,
							graph.createTupleConstruction());
				} else {
					return null;
				}
			default:
				break;
			}
		}
		fail("Expected value construction, but found");
		return null;
	}

	private final MapConstruction parseMapConstruction() {
		match(GreqlTokenType.MAP);
		match(GreqlTokenType.LPAREN);
		if (tryMatch(GreqlTokenType.RPAREN)) {
			return graph.createMapConstruction();
		}
		MapConstruction mapConstr = null;
		if (!inPredicateMode()) {
			mapConstr = graph.createMapConstruction();
		}
		int offsetKey = getCurrentOffset();
		Expression keyExpr = parseExpression();
		int lengthKey = getLength(offsetKey);
		match(GreqlTokenType.EDGEEND);
		int offsetValue = getCurrentOffset();
		Expression valueExpr = parseExpression();
		int lengthValue = getLength(offsetValue);
		if (!inPredicateMode()) {
			IsKeyExprOfConstruction keyEdge = graph
					.createIsKeyExprOfConstruction(keyExpr, mapConstr);
			keyEdge.set_sourcePositions(createSourcePositionList(lengthKey,
					offsetKey));
			IsValueExprOfConstruction valueEdge = graph
					.createIsValueExprOfConstruction(valueExpr, mapConstr);
			valueEdge.set_sourcePositions(createSourcePositionList(lengthValue,
					offsetValue));
		}
		while (tryMatch(GreqlTokenType.COMMA)) {
			offsetKey = getCurrentOffset();
			keyExpr = parseExpression();
			lengthKey = getLength(offsetKey);
			match(GreqlTokenType.EDGEEND);
			offsetValue = getCurrentOffset();
			valueExpr = parseExpression();
			lengthValue = getLength(offsetValue);
			if (!inPredicateMode()) {
				IsKeyExprOfConstruction keyEdge = graph
						.createIsKeyExprOfConstruction(keyExpr, mapConstr);
				keyEdge.set_sourcePositions(createSourcePositionList(lengthKey,
						offsetKey));
				IsValueExprOfConstruction valueEdge = graph
						.createIsValueExprOfConstruction(valueExpr, mapConstr);
				valueEdge.set_sourcePositions(createSourcePositionList(
						lengthValue, offsetValue));
			}
		}

		match(GreqlTokenType.RPAREN);
		return mapConstr;
	}

	private final ValueConstruction parseListConstruction() {
		match(GreqlTokenType.LIST);
		match(GreqlTokenType.LPAREN);
		if (tryMatch(GreqlTokenType.RPAREN)) {
			return graph.createListConstruction();
		}
		ValueConstruction result = null;
		int offsetStart = getCurrentOffset();
		Expression startExpr = parseExpression();
		int lengthStart = getLength(offsetStart);
		if (tryMatch(GreqlTokenType.DOTDOT)) {
			int offsetEnd = getCurrentOffset();
			Expression endExpr = parseExpression();
			int lengthEnd = getLength(offsetEnd);
			if (!inPredicateMode()) {
				result = graph.createListRangeConstruction();
				IsFirstValueOf firstValueOf = graph.createIsFirstValueOf(
						startExpr, (ListRangeConstruction) result);
				firstValueOf.set_sourcePositions(createSourcePositionList(
						lengthStart, offsetStart));
				IsLastValueOf lastValueOf = graph.createIsLastValueOf(endExpr,
						(ListRangeConstruction) result);
				lastValueOf.set_sourcePositions(createSourcePositionList(
						lengthEnd, offsetEnd));
			}
		} else {
			List<VertexPosition<Expression>> allExpressions = null;
			if (tryMatch(GreqlTokenType.COMMA)) {
				allExpressions = parseExpressionList(GreqlTokenType.COMMA);
			}
			if (!inPredicateMode()) {
				VertexPosition<Expression> v = new VertexPosition<Expression>(
						startExpr, lengthStart, offsetStart);
				if (allExpressions == null) {
					allExpressions = new ArrayList<VertexPosition<Expression>>(
							1);
				}
				allExpressions.add(0, v);
				result = createPartsOfValueConstruction(allExpressions,
						graph.createListConstruction());
			}
		}
		match(GreqlTokenType.RPAREN);
		return result;
	}

	private final ValueConstruction parseRecordConstruction() {
		match(GreqlTokenType.REC);
		match(GreqlTokenType.LPAREN);
		List<VertexPosition<RecordElement>> elements = new ArrayList<VertexPosition<RecordElement>>();
		do {
			int offset = getCurrentOffset();
			RecordElement recElem = parseRecordElement();
			int length = getLength(offset);
			elements.add(new VertexPosition<RecordElement>(recElem, length,
					offset));
		} while (tryMatch(GreqlTokenType.COMMA));
		match(GreqlTokenType.RPAREN);
		if (!inPredicateMode()) {
			RecordConstruction valueConstr = graph.createRecordConstruction();
			if (elements != null) {
				for (VertexPosition<RecordElement> expr : elements) {
					IsRecordElementOf exprOf = graph.createIsRecordElementOf(
							expr.node, valueConstr);
					exprOf.set_sourcePositions(createSourcePositionList(
							expr.length, expr.offset));
				}
			}
			return valueConstr;
		}
		return null;

	}

	private final RecordElement parseRecordElement() {
		int offsetRecId = getCurrentOffset();
		String recIdName = matchIdentifier();
		int lengthRecId = getLength(offsetRecId);
		match(GreqlTokenType.COLON);
		int offsetExpr = getCurrentOffset();
		Expression expr = parseExpression();
		int lengthExpr = getLength(offsetExpr);
		if (!inPredicateMode()) {
			RecordId recId = graph.createRecordId();
			recId.set_name(recIdName);
			RecordElement recElement = graph.createRecordElement();
			IsRecordIdOf recIdOf = graph.createIsRecordIdOf(recId, recElement);
			recIdOf.set_sourcePositions(createSourcePositionList(lengthRecId,
					offsetRecId));
			IsRecordExprOf exprOf = graph
					.createIsRecordExprOf(expr, recElement);
			exprOf.set_sourcePositions(createSourcePositionList(lengthExpr,
					offsetExpr));
			return recElement;
		}
		return null;
	}

	private final Declaration parseQuantifiedDeclaration() {
		List<VertexPosition<SimpleDeclaration>> declarations = parseDeclarationList();
		Declaration declaration = null;
		if (!inPredicateMode()) {
			declaration = (Declaration) createMultipleEdgesToParent(
					declarations, graph.createDeclaration(), IsSimpleDeclOf.EC,
					false);
		}
		while (tryMatch(GreqlTokenType.COMMA)) {
			int offsetConstraint = getCurrentOffset();
			Expression constraintExpr = parseExpression();
			int lengthConstraint = getLength(offsetConstraint);
			if (!inPredicateMode()) {
				IsConstraintOf constraintOf = graph.createIsConstraintOf(
						constraintExpr, declaration);
				constraintOf.set_sourcePositions(createSourcePositionList(
						lengthConstraint, offsetConstraint));
			}
			predicateStart();
			try {
				match(GreqlTokenType.COMMA);
				parseSimpleDeclaration();
			} catch (ParsingException ex) {
			}
			if (predicateEnd()) {
				match(GreqlTokenType.COMMA);
				declarations = parseDeclarationList();
				if (!inPredicateMode()) {
					createMultipleEdgesToParent(declarations, declaration,
							IsSimpleDeclOf.EC, false);
				}
			}
		}
		return declaration;
	}

	private final List<VertexPosition<SimpleDeclaration>> parseDeclarationList() {
		List<VertexPosition<SimpleDeclaration>> declList = new ArrayList<VertexPosition<SimpleDeclaration>>();
		int offset = getCurrentOffset();
		SimpleDeclaration decl = parseSimpleDeclaration();
		int length = getLength(offset);
		declList.add(new VertexPosition<SimpleDeclaration>(decl, length, offset));
		if (lookAhead(0) == GreqlTokenType.COMMA) {
			predicateStart();
			try {
				match(GreqlTokenType.COMMA);
				parseSimpleDeclaration();
			} catch (ParsingException ex) {
			}
			if (predicateEnd()) {
				match(GreqlTokenType.COMMA);
				declList.addAll(parseDeclarationList());
			}
		}
		return declList;
	}

	private final SimpleDeclaration parseSimpleDeclaration() {
		List<VertexPosition<Variable>> variables = parseVariableList();
		match(GreqlTokenType.COLON);
		int offset = getCurrentOffset();
		Expression expr = parseExpression();
		int length = getLength(offset);
		if (!inPredicateMode()) {
			SimpleDeclaration simpleDecl = (SimpleDeclaration) createMultipleEdgesToParent(
					variables, graph.createSimpleDeclaration(),
					IsDeclaredVarOf.EC, "");
			IsTypeExprOf typeExprOf = graph.createIsTypeExprOfDeclaration(expr,
					simpleDecl);
			typeExprOf.set_sourcePositions(createSourcePositionList(length,
					offset));
			return simpleDecl;
		}
		return null;
	}

	private final List<VertexPosition<Expression>> parseExpressionList(
			GreqlTokenType separator) {
		int pos = alreadySucceeded(RuleEnum.EXPRESSION_LIST);
		if (skipRule(pos)) {
			return null;
		}
		List<VertexPosition<Expression>> list = new ArrayList<VertexPosition<Expression>>();
		do {
			int offset = getCurrentOffset();
			Expression expr = parseExpression();
			int length = getLength(offset);
			list.add(new VertexPosition<Expression>(expr, length, offset));
		} while (tryMatch(separator));
		ruleSucceeds(RuleEnum.EXPRESSION_LIST, pos);
		return list;
	}

	private final Expression parseRangeExpression() {
		Expression expr = null;
		if (tryMatch(GreqlTokenType.V)) {
			if (!inPredicateMode()) {
				expr = graph.createVertexSetExpression();
			}
		} else {
			match(GreqlTokenType.E);
			if (!inPredicateMode()) {
				expr = graph.createEdgeSetExpression();
			}
		}
		if (tryMatch(GreqlTokenType.LCURLY)) {
			if (!tryMatch(GreqlTokenType.RCURLY)) {
				List<VertexPosition<TypeId>> typeIds = parseTypeExpressionList();
				match(GreqlTokenType.RCURLY);
				if (!inPredicateMode()) {
					createMultipleEdgesToParent(typeIds, expr,
							IsTypeRestrOfExpression.EC, 0);
				}
			}
		}
		return expr;
	}

	private final List<VertexPosition<TypeId>> parseTypeExpressionList() {
		List<VertexPosition<TypeId>> list = new ArrayList<VertexPosition<TypeId>>();
		do {
			int offset = getCurrentOffset();
			TypeId t = parseTypeId();
			int length = getLength(offset);
			list.add(new VertexPosition<TypeId>(t, length, offset));
		} while (tryMatch(GreqlTokenType.COMMA));
		return list;
	}

	private final TypeId parseTypeId() {
		TypeId type = null;
		if (!inPredicateMode()) {
			type = graph.createTypeId();
		}
		if (tryMatch(GreqlTokenType.CARET)) {
			if (!inPredicateMode()) {
				type.set_excluded(true);
			}
		}
		String s = matchQualifiedName();
		if (!inPredicateMode()) {
			type.set_name(s);
		}
		if (tryMatch(GreqlTokenType.EXCL)) {
			if (!inPredicateMode()) {
				type.set_type(true);
			}
		}
		return type;
	}

	private TypeOrRoleId parseTypeOrRoleId() {
		TypeOrRoleId id = null;
		predicateStart();
		try {
			parseTypeId();
		} catch (ParsingException ex) {
			// no type id but a role id
		}
		if (predicateEnd()) {
			id = parseTypeId();
		} else {
			id = parseRoleId();
		}
		return id;
	}

	private final List<VertexPosition<? extends TypeOrRoleId>> parseTypeAndRoleExpressionList() {
		List<VertexPosition<? extends TypeOrRoleId>> list = new ArrayList<VertexPosition<? extends TypeOrRoleId>>();
		do {
			int offset = getCurrentOffset();
			TypeOrRoleId id = parseTypeOrRoleId();
			int length = getLength(offset);
			list.add(new VertexPosition<TypeOrRoleId>(id, length, offset));
		} while (tryMatch(GreqlTokenType.COMMA));
		return list;
	}

	@SuppressWarnings("unchecked")
	private final EdgeRestriction parseEdgeRestriction() {
		List<VertexPosition<TypeId>> typeIds = null;
		List<VertexPosition<RoleId>> roleIds = null;
		Expression predicate = null;
		int predicateOffset = 0;
		int predicateLength = 0;

		predicateStart();
		try {
			parseTypeOrRoleId();
		} catch (ParsingException ex) {
			// failed predicate
		}
		if (predicateEnd()) {
			List<VertexPosition<? extends TypeOrRoleId>> typeOrRoleIds = parseTypeAndRoleExpressionList();
			if (typeOrRoleIds != null) {
				typeIds = new ArrayList<VertexPosition<TypeId>>();
				roleIds = new ArrayList<VertexPosition<RoleId>>();
				for (VertexPosition<? extends TypeOrRoleId> id : typeOrRoleIds) {
					if (id.node instanceof TypeId) {
						typeIds.add((VertexPosition<TypeId>) id);
					} else {
						roleIds.add((VertexPosition<RoleId>) id);
					}
				}
			}
		}
		if (tryMatch(GreqlTokenType.AT)) {
			predicateOffset = getCurrentOffset();
			predicate = parseExpression();
			predicateLength = getLength(predicateOffset);
		}
		EdgeRestriction er = null;
		if (!inPredicateMode()) {
			er = graph.createEdgeRestriction();
			if (typeIds != null) {
				for (VertexPosition<TypeId> type : typeIds) {
					IsTypeIdOf typeIdOf = graph.createIsTypeIdOf(type.node, er);
					typeIdOf.set_sourcePositions(createSourcePositionList(
							type.length, type.offset));
				}
			}
			if (roleIds != null) {
				for (VertexPosition<RoleId> role : roleIds) {
					IsRoleIdOf roleIdOf = graph.createIsRoleIdOf(role.node, er);
					roleIdOf.set_sourcePositions(createSourcePositionList(
							role.length, role.offset));
				}
			}
			if (predicate != null) {
				IsBooleanPredicateOfEdgeRestriction edge = graph
						.createIsBooleanPredicateOfEdgeRestriction(predicate,
								er);
				edge.set_sourcePositions(createSourcePositionList(
						predicateLength, predicateOffset));
			}
		}
		return er;
	}

	private final Comprehension parseLabeledReportList() {
		TupleConstruction tupConstr = null;
		boolean hasLabel = false;
		int offsetExpr = 0;
		int offset = 0;
		int offsetAsExpr = 0;
		int lengthAsExpr = 0;
		ListComprehension listCompr = null;
		Expression expr = null;
		int lengthExpr = 0;
		Expression asExpr = null;
		match(GreqlTokenType.REPORT);
		do {
			hasLabel = false;
			offsetExpr = getCurrentOffset();
			offset = offsetExpr;
			expr = parseExpression();
			lengthExpr = getLength(offsetExpr);
			if (tryMatch(GreqlTokenType.AS)) {
				offsetAsExpr = getCurrentOffset();
				asExpr = parseExpression();
				lengthAsExpr = getLength(offsetAsExpr);
				hasLabel = true;
			}
			if (!inPredicateMode()) {
				if (listCompr == null) {
					listCompr = graph.createListComprehension();
					tupConstr = graph.createTupleConstruction();
					IsCompResultDefOf e = graph.createIsCompResultDefOf(
							tupConstr, listCompr);
					e.set_sourcePositions(createSourcePositionList(
							getLength(offset), offset));
				}
				IsPartOf partOf = graph.createIsPartOf(expr, tupConstr);
				partOf.set_sourcePositions(createSourcePositionList(lengthExpr,
						offsetExpr));
				if (hasLabel) {
					IsTableHeaderOf tableHeaderOf = graph
							.createIsTableHeaderOf(asExpr, listCompr);
					tableHeaderOf.set_sourcePositions(createSourcePositionList(
							lengthAsExpr, offsetAsExpr));
				} else {
					UndefinedLiteral ul = null;
					if (!inPredicateMode()) {
						ul = graph.getFirstUndefinedLiteral();
						if (ul == null) {
							ul = graph.createUndefinedLiteral();
						}
					}
					graph.createIsTableHeaderOf(ul, listCompr);
				}
			}
		} while (tryMatch(GreqlTokenType.COMMA));
		if (!inPredicateMode() && (tupConstr.getDegree(EdgeDirection.IN) == 1)) {
			Vertex v = tupConstr.getFirstIncidence(EdgeDirection.IN).getAlpha();
			Edge e2 = tupConstr.getFirstIncidence(EdgeDirection.OUT);
			e2.setAlpha(v);
			tupConstr.delete();
		}
		return listCompr;
	}

	private final Comprehension parseReportClause() {
		Comprehension comprehension = null;
		boolean map = false;
		GreqlTokenType separator = GreqlTokenType.COMMA;
		GreqlTokenType comprehensionType = lookAhead(0);
		switch (comprehensionType) {
		case REPORT:
			return parseLabeledReportList();
		case REPORTLIST:
		case REPORTLISTN:
			if (!inPredicateMode()) {
				comprehension = graph.createListComprehension();
			}
			match();
			if (comprehensionType == GreqlTokenType.REPORTLISTN) {
				Expression limit = parseExpression();
				if (!inPredicateMode()) {
					comprehension.add_maxCount(limit);
				}
				match(GreqlTokenType.COLON);
			}
			break;
		case REPORTSET:
		case REPORTSETN:
			if (!inPredicateMode()) {
				comprehension = graph.createSetComprehension();
			}
			match();
			if (comprehensionType == GreqlTokenType.REPORTSETN) {
				Expression limit = parseExpression();
				if (!inPredicateMode()) {
					comprehension.add_maxCount(limit);
				}
				match(GreqlTokenType.COLON);
			}
			break;
		case REPORTMAP:
		case REPORTMAPN:
			if (!inPredicateMode()) {
				comprehension = graph.createMapComprehension();
			}
			map = true;
			separator = GreqlTokenType.EDGEEND;
			match();
			if (comprehensionType == GreqlTokenType.REPORTMAPN) {
				Expression limit = parseExpression();
				if (!inPredicateMode()) {
					comprehension.add_maxCount(limit);
				}
				match(GreqlTokenType.COLON);
			}
			break;
		default:
			fail("Unrecognized token");
		}
		int offset = getCurrentOffset();
		List<VertexPosition<Expression>> reportList = parseExpressionList(separator);
		int length = getLength(offset);
		IsCompResultDefOf e = null;
		if (map) {
			if (!inPredicateMode()) {
				if (reportList.size() != 2) {
					fail("reportMap keyExpr -> valueExpr must be followed by exactly two arguments");
				}

				IsKeyExprOfComprehension keyEdge = graph
						.createIsKeyExprOfComprehension(reportList.get(0).node,
								(MapComprehension) comprehension);
				IsValueExprOfComprehension valueEdge = graph
						.createIsValueExprOfComprehension(
								reportList.get(1).node,
								(MapComprehension) comprehension);
				keyEdge.set_sourcePositions(createSourcePositionList(
						reportList.get(0).length, reportList.get(0).offset));
				valueEdge.set_sourcePositions(createSourcePositionList(
						reportList.get(1).length, reportList.get(1).offset));
			}
		} else {
			if (!inPredicateMode()) {
				if (reportList.size() > 1) {
					TupleConstruction tupConstr = (TupleConstruction) createMultipleEdgesToParent(
							reportList, graph.createTupleConstruction(),
							IsPartOf.EC);
					e = graph.createIsCompResultDefOf(tupConstr, comprehension);
				} else {
					e = graph.createIsCompResultDefOf(reportList.get(0).node,
							comprehension);
				}
				e.set_sourcePositions(createSourcePositionList(length, offset));
			}
		}
		return comprehension;
	}

	private final Comprehension parseFWRExpression() {
		match(GreqlTokenType.FROM);
		int offsetDecl = getCurrentOffset();
		duringParsingvariableSymbolTable.blockBegin();
		List<VertexPosition<SimpleDeclaration>> declarations = parseDeclarationList();
		int lengthDecl = getLength(offsetDecl);
		Declaration declaration = null;
		if (!inPredicateMode()) {
			declaration = graph.createDeclaration();
			createMultipleEdgesToParent(declarations, declaration,
					IsSimpleDeclOf.EC, false);
		}
		if (tryMatch(GreqlTokenType.WITH)) {
			int offsetConstraint = getCurrentOffset();
			Expression constraintExpr = parseExpression();
			int lengthConstraint = getLength(offsetConstraint);
			lengthDecl += lengthConstraint;
			if (!inPredicateMode()) {
				IsConstraintOf constraintOf = graph.createIsConstraintOf(
						constraintExpr, declaration);
				constraintOf.set_sourcePositions(createSourcePositionList(
						lengthConstraint, offsetConstraint));
			}
		}
		Comprehension comprehension = parseReportClause();
		if (!inPredicateMode()) {
			IsCompDeclOf comprDeclOf = graph.createIsCompDeclOf(declaration,
					comprehension);
			comprDeclOf.set_sourcePositions(createSourcePositionList(
					lengthDecl, offsetDecl));
		}
		match(GreqlTokenType.END);
		duringParsingvariableSymbolTable.blockEnd();
		return comprehension;
	}

	private final Expression parsePathExpression() {
		int pos = alreadySucceeded(RuleEnum.PATH_EXPRESSION);
		if (skipRule(pos)) {
			return null;
		}
		Expression expr = null;
		/*
		 * AlternativePathDescrition as path of backwardVertexSet or
		 * backwardPathSystem
		 */
		/* (alternativePathDescription (SMILEY | restrictedExpression)) => */
		predicateStart();
		try {
			parseAltPathDescription();
			if (!tryMatch(GreqlTokenType.SMILEY)) {
				parseValueAccess(); // parseRestrictedExpression();
			}
		} catch (ParsingException ex) {
		}
		if (predicateEnd()) {
			expr = parseRegBackwardVertexSetOrPathSystem();
		} else {
			predicateStart();
			try {
				parseValueAccess(); // parseRestrictedExpression();
			} catch (ParsingException ex) {
			}
			if (predicateEnd()) {
				int offsetArg1 = getCurrentOffset();
				expr = parseValueAccess();// parseRestrictedExpression();
				int lengthArg1 = getLength(offsetArg1);
				if (lookAhead(0) == GreqlTokenType.SMILEY) {
					expr = parseRegPathOrPathSystem(expr, offsetArg1,
							lengthArg1);
				} else {
					predicateStart();
					try {
						parseAltPathDescription();
					} catch (ParsingException ex) {
					}
					if (predicateEnd()) {
						expr = parseRegPathExistenceOrForwardVertexSet(expr,
								offsetArg1, lengthArg1);
					}
				}
			} else {
				expr = parseAltPathDescription();
			}
		}
		ruleSucceeds(RuleEnum.PATH_EXPRESSION, pos);
		return expr;
	}

	private final Expression parseRegPathExistenceOrForwardVertexSet(
			Expression expr, int offsetArg1, int lengthArg1) {
		int offsetExpr = getCurrentOffset();
		int offsetPathDescr = getCurrentOffset();
		PathDescription pathDescr = parseAltPathDescription();
		int lengthPathDescr = getLength(offsetPathDescr);
		Expression restrExpr = null;
		predicateStart();
		try {
			parsePrimaryExpression();
		} catch (ParsingException ex) {
		}
		if (predicateEnd()) {
			restrExpr = parseValueAccess(); // parseRestrictedExpression();
			if (!inPredicateMode()) {
				int lengthExpr = getLength(offsetExpr);
				PathExistence pe = graph.createPathExistence();
				// add start vertex
				IsStartExprOf startVertexOf = graph.createIsStartExprOf(expr,
						pe);
				startVertexOf.set_sourcePositions(createSourcePositionList(
						lengthArg1, offsetArg1));
				// add target vertex
				IsTargetExprOf targetVertexOf = graph.createIsTargetExprOf(
						restrExpr, pe);
				targetVertexOf.set_sourcePositions(createSourcePositionList(
						lengthExpr, offsetExpr));
				// add pathdescription
				IsPathOf pathOf = graph.createIsPathOf(pathDescr, pe);
				pathOf.set_sourcePositions(createSourcePositionList(
						lengthPathDescr, offsetPathDescr));
				return pe;
			}
			return null;
		} else {
			if (!inPredicateMode()) {
				// create new forward-vertex-set
				ForwardVertexSet fvs = graph.createForwardVertexSet();
				// add start expr
				IsStartExprOf startVertexOf = graph.createIsStartExprOf(expr,
						fvs);
				startVertexOf.set_sourcePositions(createSourcePositionList(
						lengthArg1, offsetArg1));
				// add pathdescr
				IsPathOf pathOf = graph.createIsPathOf(pathDescr, fvs);
				pathOf.set_sourcePositions(createSourcePositionList(
						lengthPathDescr, offsetPathDescr));
				return fvs;
			}
			return null;
		}
	}

	private final Expression parseRegPathOrPathSystem(Expression arg1,
			int offsetArg1, int lengthArg1) {
		boolean isPath = false;
		int offsetOperator1 = getCurrentOffset();
		int offsetExpr = offsetArg1;
		int lengthExpr = 0;
		Expression restrExpr = null;
		match(GreqlTokenType.SMILEY);
		int offsetPathDescr = getCurrentOffset();
		PathDescription pathDescr = parseAltPathDescription();
		int lengthPathDescr = getLength(offsetPathDescr);
		int offsetOperator2 = getCurrentOffset();
		if (tryMatch(GreqlTokenType.SMILEY)) {
			offsetExpr = getCurrentOffset();
			restrExpr = parseValueAccess(); // parseRestrictedExpression();
			lengthExpr = getLength(offsetExpr);
		}
		if (!inPredicateMode()) {
			FunctionId funId = getFunctionId("pathSystem");
			Expression result = createFunctionIdAndArgumentOf(funId,
					offsetOperator1, 3, arg1, offsetArg1, lengthArg1,
					pathDescr, offsetPathDescr, lengthPathDescr, true);
			if (isPath) {
				result = createFunctionIdAndArgumentOf(funId, offsetOperator1,
						3, result, offsetArg1, -offsetArg1 + offsetOperator2
								+ 3, restrExpr, offsetExpr, lengthExpr, true);
			}
			return result;
		}
		return null;
	}

	private final Expression parseRegBackwardVertexSetOrPathSystem() {
		boolean isPathSystem = false;
		int offsetPathDescr = getCurrentOffset();
		PathDescription pathDescr = parseAltPathDescription();
		int lengthPathDescr = getLength(offsetPathDescr);
		int offsetOperator = getCurrentOffset();
		if (tryMatch(GreqlTokenType.SMILEY)) {
			isPathSystem = true;
		}
		int offsetExpr = getCurrentOffset();
		Expression restrExpr = parseValueAccess();// parseRestrictedExpression();
		int lengthExpr = getLength(offsetExpr);
		if (!inPredicateMode()) {
			if (isPathSystem) {
				// create a path-system-functionapplication
				FunctionId f = getFunctionId("pathSystem");
				return createFunctionIdAndArgumentOf(f, offsetOperator, 3,
						pathDescr, offsetPathDescr, lengthPathDescr, restrExpr,
						offsetExpr, lengthExpr, true);
			} else {
				BackwardVertexSet bs = graph.createBackwardVertexSet();
				IsTargetExprOf targetVertexOf = graph.createIsTargetExprOf(
						restrExpr, bs);
				targetVertexOf.set_sourcePositions(createSourcePositionList(
						lengthExpr, offsetExpr));
				IsPathOf pathOf = graph.createIsPathOf(pathDescr, bs);
				pathOf.set_sourcePositions(createSourcePositionList(
						lengthPathDescr, offsetPathDescr));
				return bs;
			}
		}
		return null;
	}

	// private final Expression parseNumericLiteral() {
	// if (lookAhead(0) == TokenTypes.DOUBLELITERAL) {
	// DoubleLiteral literal = null;
	// if (!inPredicateMode()) {
	// literal = graph.createDoubleLiteral();
	// literal.set_doubleValue(((DoubleToken) lookAhead).getNumber());
	// }
	// match();
	// return literal;
	// }
	// if ((lookAhead(0) == TokenTypes.HEXLITERAL)
	// || (lookAhead(0) == TokenTypes.OCTLITERAL)) {
	// if (((LongToken) lookAhead).getNumber().intValue() == ((LongToken)
	// lookAhead)
	// .getNumber().longValue()) {
	// IntLiteral literal = null;
	// if (!inPredicateMode()) {
	// literal = graph.createIntLiteral();
	// literal.set_intValue(((LongToken) lookAhead).getNumber()
	// .intValue());
	// }
	// match();
	// return literal;
	// } else {
	// LongLiteral literal = null;
	// if (!inPredicateMode()) {
	// literal = graph.createLongLiteral();
	// literal.set_longValue(((LongToken) lookAhead)
	// .getNumber());
	// }
	// match();
	// return literal;
	// }
	// }
	// if ((lookAhead(0) == TokenTypes.INTLITERAL)) {
	// long value = ((LongToken) lookAhead).getNumber().longValue();
	// String integerPart = lookAhead.getValue();
	// match();
	// if (lookAhead(0) == TokenTypes.DOT) {
	// String decimalPart = "0";
	// match();
	// if ((lookAhead(0) == TokenTypes.INTLITERAL)
	// || (lookAhead(0) == TokenTypes.OCTLITERAL)) {
	// decimalPart = ((LongToken) lookAhead).getValue();
	// match();
	// } else {
	// fail("Unrecognized token as part of decimal value");
	// }
	// if (!inPredicateMode()) {
	// String doubleValue = integerPart + "." + decimalPart;
	// DoubleLiteral literal = graph.createDoubleLiteral();
	// // System.out.println("Real Value: '" + realValue + "'");
	// literal.set_doubleValue(Double.parseDouble(doubleValue));
	// return literal;
	// }
	// return null;
	// } else {
	// if (!inPredicateMode()) {
	// if ((value < Integer.MAX_VALUE)
	// && (value > Integer.MIN_VALUE)) {
	// IntLiteral literal = graph.createIntLiteral();
	// literal.set_intValue((int) value);
	// return literal;
	// } else {
	// LongLiteral literal = graph.createLongLiteral();
	// literal.set_longValue(value);
	// return literal;
	// }
	// }
	// return null;
	// }
	// }
	// fail("No numeric literal");
	// return null;
	// }

	private final Expression parseNumericLiteral() {
		if (lookAhead(0) == GreqlTokenType.DOUBLELITERAL) {
			double value = ((DoubleToken) lookAhead).getNumber().doubleValue();
			match();
			if (!inPredicateMode()) {
				DoubleLiteral literal = graph.createDoubleLiteral();
				literal.set_doubleValue(value);
				return literal;
			} else {
				return null;
			}
		}
		if (lookAhead(0) == GreqlTokenType.LONGLITERAL) {
			long value = ((LongToken) lookAhead).getNumber().longValue();
			match();
			if (!inPredicateMode()) {
				if ((value <= Integer.MAX_VALUE)
						&& (value >= Integer.MIN_VALUE)) {
					IntLiteral literal = graph.createIntLiteral();
					literal.set_intValue((int) value);
					return literal;
				} else {
					LongLiteral literal = graph.createLongLiteral();
					literal.set_longValue(value);
					return literal;
				}
			} else {
				return null;
			}
		}
		fail("Unrecognized literal");
		return null;
	}

	private final Expression parseLiteral() {
		if (lookAhead(0) != null) {
			switch (lookAhead(0)) {
			case UNDEFINED: {
				UndefinedLiteral ul = null;
				if (!inPredicateMode()) {
					ul = graph.getFirstUndefinedLiteral();
					if (ul == null) {
						ul = graph.createUndefinedLiteral();
					}
				}
				match();
				return ul;
			}
			case DOUBLELITERAL:
			case LONGLITERAL:
				return parseNumericLiteral();
			case STRING: {
				StringLiteral sl = null;
				if (!inPredicateMode()) {
					sl = graph.createStringLiteral();
					sl.set_stringValue(lookAhead.getValue());
				}
				match();
				return sl;
			}
			case THISEDGE: {
				match();
				ThisEdge te = null;
				if (!inPredicateMode()) {
					te = graph.getFirstThisEdge();
					if (te == null) {
						te = graph.createThisEdge();
					}
				}
				return te;
			}
			case THISVERTEX: {
				match();
				ThisVertex tv = null;
				if (!inPredicateMode()) {
					tv = graph.getFirstThisVertex();
					if (tv == null) {
						tv = graph.createThisVertex();
					}
				}
				return tv;
			}
			case TRUE: {
				match();
				BoolLiteral tl = null;
				if (!inPredicateMode()) {
					tl = graph.getFirstBoolLiteral();
					while (tl != null) {
						if (tl.is_boolValue() == true) {
							break;
						}
						tl = tl.getNextBoolLiteral();
					}
					if (tl == null) {
						tl = graph.createBoolLiteral();
						tl.set_boolValue(true);
					}
				}
				return tl;
			}
			case FALSE: {
				match();
				BoolLiteral fl = null;
				if (!inPredicateMode()) {
					fl = graph.getFirstBoolLiteral();
					while (fl != null) {
						if (fl.is_boolValue() == false) {
							break;
						}
						fl = fl.getNextBoolLiteral();
					}
					if (fl == null) {
						fl = graph.createBoolLiteral();
						fl.set_boolValue(false);
					}
				}
				return fl;
			}
			default:
				break;
			}
		}
		fail("Unrecognized literal");
		return null;
	}

	public GreqlSchema getSchema() {
		return SCHEMA;
	}

}
