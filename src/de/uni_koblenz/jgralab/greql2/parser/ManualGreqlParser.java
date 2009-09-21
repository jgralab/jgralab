package de.uni_koblenz.jgralab.greql2.parser;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;

import de.uni_koblenz.jgralab.Edge;
import de.uni_koblenz.jgralab.EdgeDirection;
import de.uni_koblenz.jgralab.Vertex;
import de.uni_koblenz.jgralab.greql2.funlib.Greql2FunctionLibrary;
import de.uni_koblenz.jgralab.greql2.schema.*;

public class ManualGreqlParser extends ManualParserHelper {

	private Map<RuleEnum, int[]> testedRules = new HashMap<RuleEnum, int[]>();

	private List<Token> tokens = null;

	private int current = 0;

	private int farestOffset = 0;

	private ParsingException farestException = null;

	private Stack<Integer> parsingStack;

	private Stack<Boolean> predicateStack;

	private boolean predicateFulfilled = true;

	private Greql2Schema schema = null;

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

	public ManualGreqlParser(String source) {
		query = source;
		parsingStack = new Stack<Integer>();
		predicateStack = new Stack<Boolean>();
		funlib = Greql2FunctionLibrary.instance();
		schema = Greql2Schema.instance();
		graph = schema.createGreql2();
		tokens = ManualGreqlLexer.scan(source);
		afterParsingvariableSymbolTable = new SymbolTable();
		duringParsingvariableSymbolTable = new EasySymbolTable();
		duringParsingvariableSymbolTable.blockBegin();
		functionSymbolTable = new HashMap<String, FunctionId>();
		graphCleaned = false;
		lookAhead = tokens.get(0);
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

	private final TokenTypes lookAhead(int i) {
		if (current + i < tokens.size()) {
			return tokens.get(current + i).type;
		} else {
			return null;
		}
	}

	protected final List<SourcePosition> createSourcePositionList(
			int startOffset) {
		List<SourcePosition> list = new ArrayList<SourcePosition>();
		list.add(new SourcePosition(getCurrentOffset() - startOffset,
				startOffset));
		return list;
	}

	public static Greql2 parse(String query) {
		ManualGreqlParser parser = new ManualGreqlParser(query);
		parser.parse();
		return parser.graph;
	}

	private final ValueConstruction createPartsOfValueConstruction(
			List<VertexPosition<Expression>> expressions,
			ValueConstruction parent) {
		return (ValueConstruction) createMultipleEdgesToParent(expressions,
				parent, IsPartOf.class);
	}

	private final Vertex createMultipleEdgesToParent(
			List<VertexPosition<Expression>> expressions, Vertex parent,
			Class<? extends Edge> edgeClass) {
		if (expressions != null) {
			for (VertexPosition<? extends Vertex> expr : expressions) {
				Greql2Aggregation edge = (Greql2Aggregation) graph.createEdge(
						edgeClass, expr.node, parent);
				edge.set_sourcePositions((createSourcePositionList(expr.length,
						expr.offset)));
			}
		}
		return parent;
	}

	private final Vertex createMultipleEdgesToParent(
			List<VertexPosition<TypeId>> expressions, Vertex parent,
			Class<? extends Edge> edgeClass, int i) {
		if (expressions != null) {
			for (VertexPosition<? extends Vertex> expr : expressions) {
				Greql2Aggregation edge = (Greql2Aggregation) graph.createEdge(
						edgeClass, expr.node, parent);
				edge.set_sourcePositions((createSourcePositionList(expr.length,
						expr.offset)));
			}
		}
		return parent;
	}

	private final Vertex createMultipleEdgesToParent(
			List<VertexPosition<SimpleDeclaration>> expressions, Vertex parent,
			Class<? extends Edge> edgeClass, boolean b) {
		if (expressions != null) {
			for (VertexPosition<? extends Vertex> expr : expressions) {
				Greql2Aggregation edge = (Greql2Aggregation) graph.createEdge(
						edgeClass, expr.node, parent);
				edge.set_sourcePositions((createSourcePositionList(expr.length,
						expr.offset)));
			}
		}
		return parent;
	}

	private final Vertex createMultipleEdgesToParent(
			List<VertexPosition<Variable>> expressions, Vertex parent,
			Class<? extends Edge> edgeClass, String s) {
		if (expressions != null) {
			for (VertexPosition<? extends Vertex> expr : expressions) {
				Greql2Aggregation edge = (Greql2Aggregation) graph.createEdge(
						edgeClass, expr.node, parent);
				edge.set_sourcePositions((createSourcePositionList(expr.length,
						expr.offset)));
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

	private final void matchEOF() {
		if (current < tokens.size() - 1) {
			fail("Expected end of file");
		}
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
		}
		ParsingException ex = new ParsingException(msg, tokenText, offset,
				length);
		predicateFulfilled = false;
		if (getCurrentOffset() > farestOffset) {
			farestException = ex;
			farestOffset = getCurrentOffset();
		}
		throw ex;
	}

	private final String matchIdentifier() {
		if (lookAhead(0) == TokenTypes.IDENTIFIER) {
			String name = lookAhead.getValue();
			if (isValidIdentifier(name)) {
				match();
				return name;
			}
		}
		fail("expected identifier");
		return null;
	}

	private final String matchSimpleName() {
		if (lookAhead(0) == TokenTypes.IDENTIFIER) {
			String name = lookAhead.getValue();
			if (isValidSimpleName(name)) {
				match();
				return name;
			}
		}
		fail("expected simple name");
		return null;
	}

	private final void match(TokenTypes type) {
		if (lookAhead(0) == type) {
			match();
		} else {
			fail("Expected " + type);
		}
	}

	private final String matchPackageName() {
		if ((lookAhead(0) == TokenTypes.IDENTIFIER)
				&& (isValidPackageName(getLookAheadValue(0)))) {
			StringBuilder name = new StringBuilder();
			name.append(lookAhead.getValue());
			match();
			boolean ph = true;
			do {
				if (lookAhead(0) == TokenTypes.DOT) {
					if ((lookAhead(1) == TokenTypes.IDENTIFIER)
							&& (isValidPackageName(getLookAheadValue(1)))) {
						ph = true;
						match(TokenTypes.DOT);
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
		fail("Unrecognized package name or TypeName expected");
		return null;
	}

	private String getLookAheadValue(int i) {
		if (current + i < tokens.size()) {
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
			match(TokenTypes.DOT);
		} catch (ParsingException ex) {
		}
		if (predicateEnd()) {
			name.append(matchPackageName());
			name.append(".");
			match(TokenTypes.DOT);
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
		Greql2Expression rootExpr = graph.createGreql2Expression();
		rootExpr.set_importedTypes(parseImports());
		if (lookAhead(0) == TokenTypes.USING) {
			match();
			List<VertexPosition<Variable>> varList = parseVariableList();
			for (VertexPosition<Variable> var : varList) {
				IsBoundVarOf isVarOf = graph.createIsBoundVarOf(var.node,
						rootExpr);
				isVarOf.set_sourcePositions((createSourcePositionList(
						var.length, var.offset)));
			}
			match(TokenTypes.COLON);
		}
		int offset = getCurrentOffset();
		Expression expr = parseExpression();
		IsQueryExprOf e = graph.createIsQueryExprOf(expr, rootExpr);
		e.set_sourcePositions((createSourcePositionList(offset)));
		if (lookAhead(0) == TokenTypes.STORE) {
			match();
			match(TokenTypes.AS);
			Identifier ident = graph.createIdentifier();
			offset = getCurrentOffset();
			ident.set_name(matchIdentifier());
			IsIdOf isId = graph.createIsIdOf(ident, rootExpr);
			isId.set_sourcePositions(createSourcePositionList(offset));
		}
		matchEOF();
		testIllegalThisLiterals();
		mergeVariablesInGreql2Expression(rootExpr);
	}

	private final Set<String> parseImports() {
		Set<String> importedTypes = new HashSet<String>();
		while (lookAhead(0) == TokenTypes.IMPORT) {
			match(TokenTypes.IMPORT);
			StringBuilder importedType = new StringBuilder();
			importedType.append(matchPackageName());
			match(TokenTypes.DOT);
			if (lookAhead.type == TokenTypes.STAR) {
				match(TokenTypes.STAR);
				importedType.append(".*");
			} else {
				importedType.append(".");
				importedType.append(matchSimpleName());
			}
			importedTypes.add(importedType.toString());
			match(TokenTypes.SEMI);
		}
		return importedTypes;
	}

	private final List<VertexPosition<Variable>> parseVariableList() {
		List<VertexPosition<Variable>> vlist = new ArrayList<VertexPosition<Variable>>();
		int offset = getCurrentOffset();
		vlist.add(new VertexPosition<Variable>(parseVariable(true),
				getLength(offset), offset));
		while (lookAhead(0) == TokenTypes.COMMA) {
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
		if (inDeclaration)
			duringParsingvariableSymbolTable.insert(varName, var);
		return var;
	}

	private final Expression parseExpression() {
		int pos = alreadySucceeded(RuleEnum.EXPRESSION);
		if (skipRule(pos)) {
			return null;
		}
		Expression expr = parseQuantifiedExpression();
		ruleSucceeds(RuleEnum.EXPRESSION, pos);
		return expr;
	}

	private final boolean tryMatch(TokenTypes type) {
		if (lookAhead(0) == type) {
			match();
			return true;
		}
		return false;
	}

	private final Quantifier parseQuantifier() {
		String name = null;
		if (tryMatch(TokenTypes.FORALL)) {
			name = "forall";
		} else if (tryMatch(TokenTypes.EXISTS_ONE)) {
			name = "exists!";
		} else if (tryMatch(TokenTypes.EXISTS)) {
			name = "exists";
		}
		if (name != null) {
			if (!inPredicateMode()) {
				for (Quantifier quantifier : graph.getQuantifierVertices()) {
					if (quantifier.get_name().equals(name)) {
						return quantifier;
					}
				}
				Quantifier quantifier = graph.createQuantifier();
				quantifier.set_name(name);
				return quantifier;
			}
			return null;
		} else {
			fail("Expected a quantifier");
			return null;
		}
	}

	private final Expression parseQuantifiedExpression() {
		if ((lookAhead(0) == TokenTypes.EXISTS)
				|| (lookAhead(0) == TokenTypes.EXISTS_ONE)
				|| (lookAhead(0) == TokenTypes.FORALL)) {
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
			match(TokenTypes.AT);
			offsetQuantifiedExpr = getCurrentOffset();
			Expression boundExpr = parseQuantifiedExpression();
			lengthQuantifiedExpr = getLength(offsetQuantifiedExpr);
			QuantifiedExpression quantifiedExpr = null;
			if (!inPredicateMode()) {
				quantifiedExpr = graph.createQuantifiedExpression();
				IsQuantifierOf quantifierOf = graph.createIsQuantifierOf(
						quantifier, quantifiedExpr);
				quantifierOf.set_sourcePositions((createSourcePositionList(
						lengthQuantifier, offsetQuantifier)));
				// add declaration
				IsQuantifiedDeclOf quantifiedDeclOf = graph
						.createIsQuantifiedDeclOf(decl, quantifiedExpr);
				quantifiedDeclOf.set_sourcePositions((createSourcePositionList(
						lengthQuantifiedDecl, offsetQuantifiedDecl)));
				// add predicate
				IsBoundExprOf boundExprOf = graph
						.createIsBoundExprOfQuantifier(boundExpr,
								quantifiedExpr);
				boundExprOf.set_sourcePositions((createSourcePositionList(
						lengthQuantifiedExpr, offsetQuantifiedExpr)));
			}
			duringParsingvariableSymbolTable.blockEnd();
			return quantifiedExpr;
		} else {
			return parseLetExpression();
		}
	}

	private final Expression parseLetExpression() {
		if (lookAhead.type == TokenTypes.LET) {
			match();
			duringParsingvariableSymbolTable.blockBegin();
			List<VertexPosition<Definition>> defList = parseDefinitionList();
			match(TokenTypes.IN);
			int offset = getCurrentOffset();
			Expression boundExpr = parseLetExpression();
			LetExpression result = null;
			if (!inPredicateMode() && !defList.isEmpty()) {
				int length = getLength(offset);
				result = graph.createLetExpression();
				IsBoundExprOf exprOf = graph.createIsBoundExprOfDefinition(
						boundExpr, result);
				exprOf.set_sourcePositions((createSourcePositionList(length,
						offset)));
				for (VertexPosition<Definition> def : defList) {
					IsDefinitionOf definitionOf = graph.createIsDefinitionOf(
							def.node, result);
					definitionOf.set_sourcePositions((createSourcePositionList(
							def.length, def.offset)));
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
		Expression expr = parseConditionalExpression();
		if (tryMatch(TokenTypes.WHERE)) {
			int length = getLength(offset);
			List<VertexPosition<Definition>> defList = parseDefinitionList();
			WhereExpression result = null;
			if (!inPredicateMode()) {
				result = graph.createWhereExpression();
				IsBoundExprOf exprOf = graph.createIsBoundExprOfDefinition(
						expr, result);
				exprOf.set_sourcePositions((createSourcePositionList(length,
						offset)));
				for (VertexPosition<Definition> def : defList) {
					IsDefinitionOf isDefOf = graph.createIsDefinitionOf(
							def.node, result);
					isDefOf.set_sourcePositions((createSourcePositionList(
							length, offset)));
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
		} while (tryMatch(TokenTypes.COMMA));
		return definitions;
	}

	private final Definition parseDefinition() {
		int offsetVar = getCurrentOffset();
		Variable var = parseVariable(true);
		int lengthVar = getLength(offsetVar);
		match(TokenTypes.ASSIGN);
		int offsetExpr = getCurrentOffset();
		Expression expr = parseExpression();
		int lengthExpr = getLength(offsetExpr);
		if (!inPredicateMode()) {
			Definition definition = graph.createDefinition();
			IsVarOf varOf = graph.createIsVarOf(var, definition);
			varOf.set_sourcePositions((createSourcePositionList(lengthVar,
					offsetVar)));
			IsExprOf exprOf = graph.createIsExprOf(expr, definition);
			exprOf.set_sourcePositions((createSourcePositionList(lengthExpr,
					offsetExpr)));
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
		if (tryMatch(TokenTypes.QUESTION)) {
			int offsetTrueExpr = getCurrentOffset();
			Expression trueExpr = parseConditionalExpression();
			int lengthTrueExpr = getLength(offsetTrueExpr);
			match(TokenTypes.COLON);
			int offsetFalseExpr = getCurrentOffset();
			Expression falseExpr = parseConditionalExpression();
			int lengthFalseExpr = getLength(offsetFalseExpr);
			match(TokenTypes.COLON);
			int offsetNullExpr = getCurrentOffset();
			Expression nullExpr = parseConditionalExpression();
			int lengthNullExpr = getLength(offsetNullExpr);
			if (!inPredicateMode()) {
				ConditionalExpression condExpr = graph
						.createConditionalExpression();
				// add condition
				IsConditionOf conditionOf = graph.createIsConditionOf(result,
						condExpr);
				conditionOf.set_sourcePositions((createSourcePositionList(
						lengthExpr, offsetExpr)));
				// add true-expression
				IsTrueExprOf trueExprOf = graph.createIsTrueExprOf(trueExpr,
						condExpr);
				trueExprOf.set_sourcePositions((createSourcePositionList(
						lengthTrueExpr, offsetTrueExpr)));
				// add false-expression
				IsFalseExprOf falseExprOf = graph.createIsFalseExprOf(
						falseExpr, condExpr);
				falseExprOf.set_sourcePositions((createSourcePositionList(
						lengthFalseExpr, offsetFalseExpr)));
				// add null-expression
				IsNullExprOf nullExprOf = graph.createIsNullExprOf(nullExpr,
						condExpr);
				nullExprOf.set_sourcePositions((createSourcePositionList(
						lengthNullExpr, offsetNullExpr)));
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
		if (tryMatch(TokenTypes.OR)) {
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
		if (tryMatch(TokenTypes.XOR)) {
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
		if (tryMatch(TokenTypes.AND)) {
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
		if (tryMatch(TokenTypes.EQUAL)) {
			construct.postOp("equals");
			return construct.postArg2(parseEqualityExpression());
		} else if (tryMatch(TokenTypes.NOT_EQUAL)) {
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
		if (tryMatch(TokenTypes.L_T)) {
			name = "leThan";
		} else if (tryMatch(TokenTypes.LE)) {
			name = "leEqual";
		} else if (tryMatch(TokenTypes.GE)) {
			name = "grEqual";
		} else if (tryMatch(TokenTypes.G_T)) {
			name = "grThan";
		} else if (tryMatch(TokenTypes.MATCH)) {
			name = "reMatch";
		}
		if (name != null) {
			construct.postOp(name);
			return construct.postArg2(parseRelationalExpression());
		}
		return expr;
	}

	private final Expression parseAdditiveExpression() {
		FunctionConstruct construct = new FunctionConstruct();
		construct.preArg1();
		Expression expr = parseMultiplicativeExpression();
		construct.preOp(expr);
		if (tryMatch(TokenTypes.PLUS)) {
			construct.postOp("plus");
			return construct.postArg2(parseAdditiveExpression());
		} else if (tryMatch(TokenTypes.MINUS)) {
			construct.postOp("minus");
			return construct.postArg2(parseAdditiveExpression());
		}
		return expr;
	}

	private final Expression parseMultiplicativeExpression() {
		FunctionConstruct construct = new FunctionConstruct();
		construct.preArg1();
		Expression expr = parseUnaryExpression();
		construct.preOp(expr);
		String name = null;
		if (tryMatch(TokenTypes.STAR)) {
			name = "times";
		} else if (tryMatch(TokenTypes.MOD)) {
			name = "modulo";
		} else if (tryMatch(TokenTypes.DIV)) {
			name = "dividedBy";
		}
		if (name != null) {
			construct.postOp(name);
			return construct.postArg2(parseMultiplicativeExpression());
		}
		return expr;
	}

	private final Expression parseUnaryExpression() {
		FunctionConstruct construct = null;
		if ((lookAhead(0) == TokenTypes.NOT)
				|| (lookAhead(0) == TokenTypes.MINUS)) {
			construct = new FunctionConstruct();
			construct.preUnaryOp();
			String opName = null;
			if (tryMatch(TokenTypes.NOT)) {
				opName = "not";
			} else if (tryMatch(TokenTypes.MINUS)) {
				opName = "uminus";
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

	/**
	 * matches restricted vertex expressions
	 * 
	 * @return
	 */
	private final Expression parseRestrictedExpression() {
		int pos = alreadySucceeded(RuleEnum.RESTRICTED_EXPRESSION);
		if (skipRule(pos)) {
			return null;
		}
		int offsetExpr = getCurrentOffset();
		Expression valAccess = parseValueAccess();
		int lengthExpr = getLength(offsetExpr);
		if ((lookAhead(0) == TokenTypes.AMP)
				&& (lookAhead(1) == TokenTypes.LCURLY)) {
			match(TokenTypes.AMP);
			match(TokenTypes.LCURLY);
			int offsetRestr = getCurrentOffset();
			Expression restriction = parseExpression();
			int lengthRestr = getLength(offsetRestr);
			match(TokenTypes.RCURLY);
			if (!inPredicateMode()) {
				RestrictedExpression restrExpr = graph
						.createRestrictedExpression();
				IsRestrictedExprOf restrExprOf = graph
						.createIsRestrictedExprOf(valAccess, restrExpr);
				restrExprOf.set_sourcePositions((createSourcePositionList(
						lengthExpr, offsetExpr)));
				// add restriction
				IsRestrictionOf restrOf = graph.createIsRestrictionOf(
						restriction, restrExpr);
				restrOf.set_sourcePositions((createSourcePositionList(
						lengthRestr, offsetRestr)));
				ruleSucceeds(RuleEnum.RESTRICTED_EXPRESSION, pos);
				return restrExpr;
			}
		}
		ruleSucceeds(RuleEnum.RESTRICTED_EXPRESSION, pos);
		return valAccess;
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
		if (lookAhead(0) == TokenTypes.DOT) {
			secondPart = true;
		}
		if (lookAhead(0) == TokenTypes.LBRACK) {
			predicateStart();
			try {
				match(TokenTypes.LBRACK);
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
		String name = "nthElement";
		int offsetOperator = getCurrentOffset();
		int lengthOperator = 0;
		int lengthArg2 = 0;
		int offsetArg2 = 0;
		Expression arg2 = null;
		if (tryMatch(TokenTypes.DOT)) {
			name = "getValue";
			lengthOperator = 1;
			offsetArg2 = getCurrentOffset();
			arg2 = parseIdentifier();
		} else if (tryMatch(TokenTypes.LBRACK)) {
			offsetArg2 = getCurrentOffset();
			arg2 = parseExpression();
			lengthArg2 = getLength(offsetArg2);
			match(TokenTypes.RBRACK);
			lengthOperator = getLength(offsetOperator);
		}
		Expression result = null;
		if (!inPredicateMode()) {
			result = createFunctionIdAndArgumentOf(getFunctionId(name),
					offsetOperator, lengthOperator, arg1, offsetArg1,
					lengthArg1, arg2, offsetArg2, lengthArg2, true);
		}
		boolean secondPart = false;
		if (lookAhead(0) == TokenTypes.DOT) {
			secondPart = true;
		}
		if (lookAhead(0) == TokenTypes.LBRACK) {
			predicateStart();
			try {
				match(TokenTypes.LBRACK);
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
		if (tryMatch(TokenTypes.LPAREN)) {
			return parseParenthesedExpression();
		}

		if ((lookAhead(0) == TokenTypes.V) || (lookAhead(0) == TokenTypes.E)) {
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

		if ((lookAhead(0) == TokenTypes.IDENTIFIER)
				&& ((lookAhead(1) == TokenTypes.LCURLY) || (lookAhead(1) == TokenTypes.LPAREN))) {
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

		predicateStart();
		try {
			parseVariable(false);
		} catch (ParsingException ex) {
		}
		if (predicateEnd()) {
			return parseVariable(false);
		}

		predicateStart();
		try {
			parseGraphRangeExpression();
		} catch (ParsingException ex) {
		}
		if (predicateEnd()) {
			return parseGraphRangeExpression();
		}

		predicateStart();
		try {
			parseLiteral();
		} catch (ParsingException ex) {
		}
		if (predicateEnd()) {
			return parseLiteral();
		}
		if (lookAhead(0) == TokenTypes.FROM) {
			return parseSimpleQuery();
		}

		fail("Unrecognized token ");
		return null;
	}

	private final Expression parseParenthesedExpression() {
		predicateStart();
		try {
			parseAltPathDescription();
			match(TokenTypes.RPAREN);
		} catch (ParsingException ex) {
		}
		if (predicateEnd()) {
			Expression expr = parseAltPathDescription();
			match(TokenTypes.RPAREN);
			return expr;
		}

		Expression expr = parseExpression();
		match(TokenTypes.RPAREN);
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
		if (tryMatch(TokenTypes.BOR)) {
			int offsetPart2 = getCurrentOffset();
			PathDescription part2 = parseAltPathDescription();
			int lengthPart2 = getLength(offsetPart2);
			if (!inPredicateMode()) {
				part1 = addPathElement(AlternativePathDescription.class,
						IsAlternativePathOf.class, null, part1, offsetPart1,
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
			parseRestrictedExpression();
			if (predicateHolds()) {
				parseSequentialPathDescription();
			}
		} catch (ParsingException ex) {
		}
		if (predicateEnd()) {
			int offsetExpr = getCurrentOffset();
			Expression restrExpr = parseRestrictedExpression();
			int lengthExpr = getLength(offsetExpr);
			int offsetPart2 = getCurrentOffset();
			PathDescription part2 = parseIntermediateVertexPathDescription();
			int lengthPart2 = getLength(offsetPart2);
			IntermediateVertexPathDescription result = null;
			if (!inPredicateMode()) {
				result = (IntermediateVertexPathDescription) addPathElement(
						IntermediateVertexPathDescription.class,
						IsSubPathOf.class, null, part1, offsetPart1,
						lengthPart1, part2, offsetPart2, lengthPart2);
				IsIntermediateVertexOf intermediateVertexOf = graph
						.createIsIntermediateVertexOf(restrExpr, result);
				intermediateVertexOf
						.set_sourcePositions((createSourcePositionList(
								lengthExpr, offsetExpr)));
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
			parseSequentialPathDescription();
		} catch (ParsingException ex) {
		}
		if (predicateEnd()) {
			int offsetPart2 = getCurrentOffset();
			PathDescription part2 = parseSequentialPathDescription();
			int lengthPart2 = getLength(offsetPart2);
			if (!inPredicateMode()) {
				return addPathElement(SequentialPathDescription.class,
						IsSequenceElementOf.class, null, part1, offsetPart1,
						lengthPart1, part2, offsetPart2, lengthPart2);
			} else {
				return null;
			}
		}
		return part1;
	}

	private final PathDescription parseStartRestrictedPathDescription() {
		int offsetRest = getCurrentOffset();
		List<VertexPosition<TypeId>> typeIds = null;
		Expression expr = null;
		if (tryMatch(TokenTypes.LCURLY)) {
			predicateStart();
			try {
				parseTypeId();
			} catch (ParsingException ex) {
			}
			if (predicateEnd()) {
				typeIds = parseTypeExpressionList();
			} else {
				expr = parseExpression();
			}
			match(TokenTypes.RCURLY);
			match(TokenTypes.AMP);
		}
		int lengthRestr = getLength(offsetRest);
		PathDescription pathDescr = parseGoalRestrictedPathDescription();
		if (!inPredicateMode()) {
			if (expr != null) {
				IsStartRestrOf startRestrOf = graph.createIsStartRestrOf(expr,
						pathDescr);
				startRestrOf.set_sourcePositions((createSourcePositionList(
						lengthRestr, offsetRest)));
			} else {
				if (typeIds != null) {
					for (VertexPosition<TypeId> t : typeIds) {
						IsStartRestrOf startRestrOf = graph
								.createIsStartRestrOf(t.node, pathDescr);
						startRestrOf
								.set_sourcePositions((createSourcePositionList(
										t.length, t.offset)));
					}
				}
			}
		}
		return pathDescr;
	}

	private final PathDescription parseGoalRestrictedPathDescription() {
		PathDescription pathDescr = parseIteratedOrTransposedPathDescription();
		if (tryMatch(TokenTypes.AMP)) {
			match(TokenTypes.LCURLY);
			predicateStart();
			try {
				parseTypeId();
			} catch (ParsingException ex) {
			}
			if (predicateEnd()) {
				List<VertexPosition<TypeId>> typeIds = parseTypeExpressionList();
				if (!inPredicateMode()) {
					for (VertexPosition<TypeId> t : typeIds) {
						IsGoalRestrOf goalRestrOf = graph.createIsGoalRestrOf(
								t.node, pathDescr);
						goalRestrOf
								.set_sourcePositions((createSourcePositionList(
										t.length, t.offset)));
					}
				}
			} else {
				int offset = getCurrentOffset();
				Expression expr = parseExpression();
				int length = getLength(offset);
				if (!inPredicateMode()) {
					IsGoalRestrOf goalRestrOf = graph.createIsGoalRestrOf(expr,
							pathDescr);
					goalRestrOf.set_sourcePositions((createSourcePositionList(
							length, offset)));
				}
			}
			match(TokenTypes.RCURLY);
		}
		return pathDescr;
	}

	private final PathDescription parseIteratedOrTransposedPathDescription() {
		int offsetPath = getCurrentOffset();
		PathDescription pathDescr = parsePrimaryPathDescription();
		int lengthPath = getLength(offsetPath);
		if ((lookAhead(0) == TokenTypes.STAR)
				|| (lookAhead(0) == TokenTypes.PLUS)
				|| (lookAhead(0) == TokenTypes.CARET)) {
			return parseIteration(pathDescr, offsetPath, lengthPath);
		}
		return pathDescr;
	}

	private final PathDescription parseIteration(PathDescription iteratedPath,
			int offsetPath, int lengthPath) {
		String iteration = null;
		PathDescription result = null;
		if (tryMatch(TokenTypes.STAR)) {
			iteration = "star";
		} else if (tryMatch(TokenTypes.PLUS)) {
			iteration = "plus";
		}
		if (iteration != null) {
			if (!inPredicateMode()) {
				IteratedPathDescription ipd = graph
						.createIteratedPathDescription();
				ipd.set_times(iteration);
				IsIteratedPathOf iteratedPathOf = graph.createIsIteratedPathOf(
						iteratedPath, ipd);
				iteratedPathOf.set_sourcePositions((createSourcePositionList(
						lengthPath, offsetPath)));
				result = ipd;
			}
		} else if (tryMatch(TokenTypes.CARET)) {
			if (tryMatch(TokenTypes.T)) {
				if (!inPredicateMode()) {
					TransposedPathDescription tpd = graph
							.createTransposedPathDescription();
					IsTransposedPathOf transposedPathOf = graph
							.createIsTransposedPathOf(iteratedPath, tpd);
					transposedPathOf
							.set_sourcePositions((createSourcePositionList(
									lengthPath, offsetPath)));
					result = tpd;
				}
			} else {
				int offsetExpr = getCurrentOffset();
				Expression ie = parseNumericLiteral();
				if (!inPredicateMode()) {
					if (!(ie instanceof IntLiteral)) {
						fail("Expected integer constant as iteration quantifier or T");
					}
					int lengthExpr = getLength(offsetExpr);
					ExponentiatedPathDescription epd = graph
							.createExponentiatedPathDescription();
					IsExponentiatedPathOf exponentiatedPathOf = graph
							.createIsExponentiatedPathOf(iteratedPath, epd);
					exponentiatedPathOf
							.set_sourcePositions((createSourcePositionList(
									lengthPath, offsetPath)));
					IsExponentOf exponentOf = graph.createIsExponentOf(
							(IntLiteral) ie, epd);
					exponentOf.set_sourcePositions((createSourcePositionList(
							lengthExpr, offsetExpr)));
					result = epd;
				}
			}
		} else {
			fail("No iteration at iterated path description");
		}
		if ((lookAhead(0) == TokenTypes.STAR)
				|| (lookAhead(0) == TokenTypes.PLUS)
				|| (lookAhead(0) == TokenTypes.CARET)) {
			return parseIteration(result, offsetPath, getLength(offsetPath));
		}
		return result;
	}

	private final PathDescription parsePrimaryPathDescription() {
		if (lookAhead(0) == TokenTypes.LPAREN) {
			predicateStart();
			try {
				match(TokenTypes.LPAREN);
				parseAltPathDescription();
			} catch (ParsingException ex) {
			}
			if (predicateEnd()) {
				match(TokenTypes.LPAREN);
				PathDescription pathDescr = parseAltPathDescription();
				match(TokenTypes.RPAREN);
				return pathDescr;
			}
		}
		if ((lookAhead(0) == TokenTypes.OUTAGGREGATION)
				|| (lookAhead(0) == TokenTypes.INAGGREGATION)) {
			return parseAggregationPathDescription();
		}
		if ((lookAhead(0) == TokenTypes.RARROW)
				|| (lookAhead(0) == TokenTypes.LARROW)
				|| (lookAhead(0) == TokenTypes.ARROW)) {
			return parseSimplePathDescription();
		}
		if ((lookAhead(0) == TokenTypes.EDGESTART)
				|| (lookAhead(0) == TokenTypes.EDGEEND)
				|| (lookAhead(0) == TokenTypes.EDGE)) {
			return parseEdgePathDescription();
		}
		if (tryMatch(TokenTypes.LBRACK)) {
			int offset = getCurrentOffset();
			PathDescription pathDescr = parseAltPathDescription();
			int length = getLength(offset);
			match(TokenTypes.RBRACK);
			if (!inPredicateMode()) {
				OptionalPathDescription optPathDescr = graph
						.createOptionalPathDescription();
				IsOptionalPathOf optionalPathOf = graph.createIsOptionalPathOf(
						pathDescr, optPathDescr);
				optionalPathOf.set_sourcePositions((createSourcePositionList(
						length, offset)));
				return optPathDescr;
			}
			return null;
		}
		fail("Unrecognized token");
		return null;
	}

	private final PrimaryPathDescription parseSimplePathDescription() {
		Direction dir = null;
		List<VertexPosition<EdgeRestriction>> typeIds = null;
		String direction = "any";
		int offsetDir = getCurrentOffset();
		if (tryMatch(TokenTypes.RARROW)) {
			direction = "out";
		} else if (tryMatch(TokenTypes.LARROW)) {
			direction = "in";
		} else {
			match(TokenTypes.ARROW);
		}
		if (tryMatch(TokenTypes.LCURLY)) {
			typeIds = parseEdgeRestrictionList();
			match(TokenTypes.RCURLY);
		}
		if (!inPredicateMode()) {
			PrimaryPathDescription result = graph.createSimplePathDescription();
			dir = (Direction) graph.getFirstVertexOfClass(Direction.class);
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
			directionOf.set_sourcePositions((createSourcePositionList(0,
					offsetDir)));
			if (typeIds != null) {
				for (VertexPosition<EdgeRestriction> t : typeIds) {
					IsEdgeRestrOf edgeRestrOf = graph.createIsEdgeRestrOf(
							t.node, result);
					edgeRestrOf.set_sourcePositions((createSourcePositionList(
							t.length, t.offset)));
				}
			}
			return result;
		}
		return null;
	}

	private final PrimaryPathDescription parseAggregationPathDescription() {
		boolean outAggregation = true;
		List<VertexPosition<EdgeRestriction>> typeIds = null;
		if (tryMatch(TokenTypes.INAGGREGATION)) {
			outAggregation = false;
		} else {
			match(TokenTypes.OUTAGGREGATION);
		}
		if (tryMatch(TokenTypes.LCURLY)) {
			typeIds = parseEdgeRestrictionList();
			match(TokenTypes.RCURLY);
		}
		if (!inPredicateMode()) {
			AggregationPathDescription result = graph
					.createAggregationPathDescription();
			result.set_outAggregation(outAggregation);
			if (typeIds != null) {
				for (VertexPosition<EdgeRestriction> t : typeIds) {
					IsEdgeRestrOf edgeRestrOf = graph.createIsEdgeRestrOf(
							t.node, result);
					edgeRestrOf.set_sourcePositions((createSourcePositionList(
							t.length, t.offset)));
				}
			}
			return result;
		}
		return null;
	}

	private final EdgePathDescription parseEdgePathDescription() {
		Direction dir = null;
		boolean edgeStart = false;
		boolean edgeEnd = false;
		String direction = "any";
		int offsetDir = getCurrentOffset();
		if (tryMatch(TokenTypes.EDGESTART)) {
			edgeStart = true;
		} else {
			match(TokenTypes.EDGE);
		}
		int offsetExpr = getCurrentOffset();
		Expression expr = parseExpression();
		int lengthExpr = getLength(offsetExpr);

		if (edgeStart) {
			if (!tryMatch(TokenTypes.EDGEEND)) {
				match(TokenTypes.EDGE);
			}
		} else {
			match(TokenTypes.EDGEEND);
			edgeEnd = true;
		}

		if (!inPredicateMode()) {
			int lengthDir = getLength(offsetDir);
			EdgePathDescription result = graph.createEdgePathDescription();
			if (edgeStart && !edgeEnd) {
				direction = "in";
			} else if (!edgeStart && edgeEnd) {
				direction = "out";
			}
			dir = (Direction) graph.getFirstVertexOfClass(Direction.class);
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
			directionOf.set_sourcePositions((createSourcePositionList(
					lengthDir, offsetDir)));
			IsEdgeExprOf edgeExprOf = graph.createIsEdgeExprOf(expr, result);
			edgeExprOf.set_sourcePositions((createSourcePositionList(
					lengthExpr, offsetExpr)));
			return result;
		}
		return null;
	}

	private final FunctionApplication parseFunctionApplication() {
		List<VertexPosition<TypeId>> typeIds = null;
		if ((lookAhead(0) == TokenTypes.IDENTIFIER)
				&& (isFunctionName(lookAhead.getValue()))
				&& ((lookAhead(1) == TokenTypes.LCURLY) || (lookAhead(1) == TokenTypes.LPAREN))) {
			int offset = getCurrentOffset();
			String name = matchIdentifier();
			int length = getLength(offset);
			if (tryMatch(TokenTypes.LCURLY)) {
				typeIds = parseTypeExpressionList();
				match(TokenTypes.RCURLY);
			}
			match(TokenTypes.LPAREN);
			List<VertexPosition<Expression>> expressions = null;
			if (lookAhead(0) != TokenTypes.RPAREN) {
				expressions = parseExpressionList();
			}
			match(TokenTypes.RPAREN);
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
						typeOf.set_sourcePositions((createSourcePositionList(
								t.length, t.offset)));
					}
				}
				if (expressions != null) {
					for (VertexPosition<Expression> ex : expressions) {
						IsArgumentOf argOf = graph.createIsArgumentOf(ex.node,
								funApp);
						argOf.set_sourcePositions((createSourcePositionList(
								ex.length, ex.offset)));
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
				match(TokenTypes.LPAREN);
				List<VertexPosition<Expression>> expressions = parseExpressionList();
				match(TokenTypes.RPAREN);
				if (!inPredicateMode()) {
					return createPartsOfValueConstruction(expressions, graph
							.createSetConstruction());
				} else {
					return null;
				}
			case BAG:
				match();
				match(TokenTypes.LPAREN);
				expressions = parseExpressionList();
				match(TokenTypes.RPAREN);
				if (!inPredicateMode()) {
					return createPartsOfValueConstruction(expressions, graph
							.createBagConstruction());
				} else {
					return null;
				}
			case PATH:
				return parsePathConstruction();
			case PATHSYSTEM:
				return parsePathsystemConstruction();
			case TUP:
				match();
				match(TokenTypes.LPAREN);
				expressions = parseExpressionList();
				match(TokenTypes.RPAREN);
				if (!inPredicateMode()) {
					return createPartsOfValueConstruction(expressions, graph
							.createTupleConstruction());
				} else {
					return null;
				}
			}
		}
		fail("Expected value construction");
		return null;
	}

	private final MapConstruction parseMapConstruction() {
		match(TokenTypes.MAP);
		match(TokenTypes.LPAREN);
		MapConstruction mapConstr = null;
		if (!inPredicateMode()) {
			mapConstr = graph.createMapConstruction();
		}
		int offsetKey = getCurrentOffset();
		Expression keyExpr = parseExpression();
		int lengthKey = getLength(offsetKey);
		match(TokenTypes.EDGEEND);
		int offsetValue = getCurrentOffset();
		Expression valueExpr = parseExpression();
		int lengthValue = getLength(offsetValue);
		if (!inPredicateMode()) {
			IsKeyExprOfConstruction keyEdge = graph
					.createIsKeyExprOfConstruction(keyExpr, mapConstr);
			keyEdge.set_sourcePositions((createSourcePositionList(lengthKey,
					offsetKey)));
			IsValueExprOfConstruction valueEdge = graph
					.createIsValueExprOfConstruction(valueExpr, mapConstr);
			valueEdge.set_sourcePositions((createSourcePositionList(
					lengthValue, offsetValue)));
		}
		while (tryMatch(TokenTypes.COMMA)) {
			offsetKey = getCurrentOffset();
			keyExpr = parseExpression();
			lengthKey = getLength(offsetKey);
			match(TokenTypes.EDGEEND);
			offsetValue = getCurrentOffset();
			valueExpr = parseExpression();
			lengthValue = getLength(offsetValue);
			if (!inPredicateMode()) {
				IsKeyExprOfConstruction keyEdge = graph
						.createIsKeyExprOfConstruction(keyExpr, mapConstr);
				keyEdge.set_sourcePositions((createSourcePositionList(
						lengthKey, offsetKey)));
				IsValueExprOfConstruction valueEdge = graph
						.createIsValueExprOfConstruction(valueExpr, mapConstr);
				valueEdge.set_sourcePositions((createSourcePositionList(
						lengthValue, offsetValue)));
			}
		}

		match(TokenTypes.RPAREN);
		return mapConstr;
	}

	private final ValueConstruction parseListConstruction() {
		match(TokenTypes.LIST);
		match(TokenTypes.LPAREN);
		ValueConstruction result = null;
		int offsetStart = getCurrentOffset();
		Expression startExpr = parseExpression();
		int lengthStart = getLength(offsetStart);
		if (tryMatch(TokenTypes.DOTDOT)) {
			int offsetEnd = getCurrentOffset();
			Expression endExpr = parseExpression();
			int lengthEnd = getLength(offsetEnd);
			if (!inPredicateMode()) {
				result = graph.createListRangeConstruction();
				IsFirstValueOf firstValueOf = graph.createIsFirstValueOf(
						startExpr, (ListRangeConstruction) result);
				firstValueOf.set_sourcePositions((createSourcePositionList(
						lengthStart, offsetStart)));
				IsLastValueOf lastValueOf = graph.createIsLastValueOf(endExpr,
						(ListRangeConstruction) result);
				lastValueOf.set_sourcePositions((createSourcePositionList(
						lengthEnd, offsetEnd)));
			}
		} else {
			match(TokenTypes.COMMA);
			List<VertexPosition<Expression>> allExpressions = parseExpressionList();
			if (!inPredicateMode()) {
				VertexPosition<Expression> v = new VertexPosition<Expression>(
						startExpr, lengthStart, offsetStart);
				allExpressions.add(0, v);
				result = createPartsOfValueConstruction(allExpressions, graph
						.createListConstruction());
			}
		}
		match(TokenTypes.RPAREN);
		return result;
	}

	private final ValueConstruction parseRecordConstruction() {
		match(TokenTypes.REC);
		match(TokenTypes.LPAREN);
		List<VertexPosition<RecordElement>> elements = new ArrayList<VertexPosition<RecordElement>>();
		do {
			int offset = getCurrentOffset();
			RecordElement recElem = parseRecordElement();
			int length = getLength(offset);
			elements.add(new VertexPosition<RecordElement>(recElem, length,
					offset));
		} while (tryMatch(TokenTypes.COMMA));
		match(TokenTypes.RPAREN);
		if (!inPredicateMode()) {
			RecordConstruction valueConstr = graph.createRecordConstruction();
			if (elements != null) {
				for (VertexPosition<RecordElement> expr : elements) {
					IsRecordElementOf exprOf = graph.createIsRecordElementOf(
							expr.node, valueConstr);
					exprOf.set_sourcePositions((createSourcePositionList(
							expr.length, expr.offset)));
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
		match(TokenTypes.COLON);
		int offsetExpr = getCurrentOffset();
		Expression expr = parseExpression();
		int lengthExpr = getLength(offsetExpr);
		if (!inPredicateMode()) {
			RecordId recId = graph.createRecordId();
			recId.set_name(recIdName);
			RecordElement recElement = graph.createRecordElement();
			IsRecordIdOf recIdOf = graph.createIsRecordIdOf(recId, recElement);
			recIdOf.set_sourcePositions((createSourcePositionList(lengthRecId,
					offsetRecId)));
			IsRecordExprOf exprOf = graph
					.createIsRecordExprOf(expr, recElement);
			exprOf.set_sourcePositions((createSourcePositionList(lengthExpr,
					offsetExpr)));
			return recElement;
		}
		return null;
	}

	private final PathConstruction parsePathConstruction() {
		match(TokenTypes.PATH);
		match(TokenTypes.LPAREN);
		List<VertexPosition<Expression>> expressions = parseExpressionList();
		match(TokenTypes.RPAREN);
		if (!inPredicateMode()) {
			return (PathConstruction) createPartsOfValueConstruction(
					expressions, graph.createPathConstruction());
		} else {
			return null;
		}
	}

	private final PathSystemConstruction parsePathsystemConstruction() {
		match(TokenTypes.PATHSYSTEM);
		match(TokenTypes.LPAREN);
		PathSystemConstruction pathsystemConstr = null;
		int offsetExpr = getCurrentOffset();
		Expression expr = parseExpression();
		int lengthExpr = getLength(offsetExpr);
		if (!inPredicateMode()) {
			pathsystemConstr = graph.createPathSystemConstruction();
			IsRootOf rootOf = graph.createIsRootOf(expr, pathsystemConstr);
			rootOf.set_sourcePositions((createSourcePositionList(lengthExpr,
					offsetExpr)));
		}
		while (tryMatch(TokenTypes.COMMA)) {
			int offsetEVList = getCurrentOffset();
			EdgeVertexList evList = parseEdgeVertexList();
			int lengthEVList = getLength(offsetEVList);
			if (!inPredicateMode()) {
				IsEdgeVertexListOf exprOf = graph.createIsEdgeVertexListOf(
						evList, pathsystemConstr);
				exprOf.set_sourcePositions((createSourcePositionList(
						lengthEVList, offsetEVList)));
			}
		}
		match(TokenTypes.RPAREN);
		return pathsystemConstr;
	}

	private final Declaration parseQuantifiedDeclaration() {
		List<VertexPosition<SimpleDeclaration>> declarations = parseDeclarationList();
		Declaration declaration = null;
		if (!inPredicateMode()) {
			declaration = (Declaration) createMultipleEdgesToParent(
					declarations, graph.createDeclaration(),
					IsSimpleDeclOf.class, false);
		}
		while (tryMatch(TokenTypes.COMMA)) {
			int offsetConstraint = getCurrentOffset();
			Expression constraintExpr = parseExpression();
			int lengthConstraint = getLength(offsetConstraint);
			if (!inPredicateMode()) {
				IsConstraintOf constraintOf = graph.createIsConstraintOf(
						constraintExpr, declaration);
				constraintOf.set_sourcePositions((createSourcePositionList(
						lengthConstraint, offsetConstraint)));
			}
			predicateStart();
			try {
				match(TokenTypes.COMMA);
				parseSimpleDeclaration();
			} catch (ParsingException ex) {
			}
			if (predicateEnd()) {
				match(TokenTypes.COMMA);
				declarations = parseDeclarationList();
				if (!inPredicateMode()) {
					createMultipleEdgesToParent(declarations, declaration,
							IsSimpleDeclOf.class, false);
				}
			}
		}
		if (tryMatch(TokenTypes.IN)) {
			int offsetSubgraph = getCurrentOffset();
			Expression subgraphExpr = parseExpression();
			int lengthSubgraph = getLength(offsetSubgraph);
			if (!inPredicateMode()) {
				IsSubgraphOf subgraphOf = graph.createIsSubgraphOf(
						subgraphExpr, declaration);
				subgraphOf.set_sourcePositions((createSourcePositionList(
						lengthSubgraph, offsetSubgraph)));
			}
		}
		return declaration;
	}

	private final List<VertexPosition<SimpleDeclaration>> parseDeclarationList() {
		List<VertexPosition<SimpleDeclaration>> declList = new ArrayList<VertexPosition<SimpleDeclaration>>();
		int offset = getCurrentOffset();
		SimpleDeclaration decl = parseSimpleDeclaration();
		int length = getLength(offset);
		declList
				.add(new VertexPosition<SimpleDeclaration>(decl, length, offset));
		if (lookAhead(0) == TokenTypes.COMMA) {
			predicateStart();
			try {
				match(TokenTypes.COMMA);
				parseSimpleDeclaration();
			} catch (ParsingException ex) {
			}
			if (predicateEnd()) {
				match(TokenTypes.COMMA);
				declList.addAll(parseDeclarationList());
			}
		}
		return declList;
	}

	private final SimpleDeclaration parseSimpleDeclaration() {
		List<VertexPosition<Variable>> variables = parseVariableList();
		match(TokenTypes.COLON);
		int offset = getCurrentOffset();
		Expression expr = parseExpression();
		int length = getLength(offset);
		if (!inPredicateMode()) {
			SimpleDeclaration simpleDecl = (SimpleDeclaration) createMultipleEdgesToParent(
					variables, graph.createSimpleDeclaration(),
					IsDeclaredVarOf.class, "");
			IsTypeExprOf typeExprOf = graph.createIsTypeExprOfDeclaration(expr,
					simpleDecl);
			typeExprOf.set_sourcePositions((createSourcePositionList(length,
					offset)));
			return simpleDecl;
		}
		return null;
	}

	private final List<VertexPosition<Expression>> parseExpressionList() {
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
		} while (tryMatch(TokenTypes.COMMA));
		ruleSucceeds(RuleEnum.EXPRESSION_LIST, pos);
		return list;
	}

	private final Expression parseRangeExpression() {
		Expression expr = null;
		if (tryMatch(TokenTypes.V)) {
			if (!inPredicateMode()) {
				expr = graph.createVertexSetExpression();
			}
		} else {
			match(TokenTypes.E);
			if (!inPredicateMode()) {
				expr = graph.createEdgeSetExpression();
			}
		}
		if (tryMatch(TokenTypes.LCURLY)) {
			if (!tryMatch(TokenTypes.RCURLY)) {
				List<VertexPosition<TypeId>> typeIds = parseTypeExpressionList();
				match(TokenTypes.RCURLY);
				if (!inPredicateMode()) {
					createMultipleEdgesToParent(typeIds, expr,
							IsTypeRestrOf.class, 0);
				}
			}
		}
		return expr;
	}

	private final Expression parseGraphRangeExpression() {
		Expression expr = null;
		if (tryMatch(TokenTypes.VSUBGRAPH)) {
			if (!inPredicateMode()) {
				expr = graph.createVertexSubgraphExpression();
			}
		} else {
			match(TokenTypes.ESUBGRAPH);
			if (!inPredicateMode()) {
				expr = graph.createEdgeSubgraphExpression();
			}
		}
		if (tryMatch(TokenTypes.LCURLY)) {
			List<VertexPosition<TypeId>> typeIds = parseTypeExpressionList();
			match(TokenTypes.RCURLY);
			if (!inPredicateMode()) {
				createMultipleEdgesToParent(typeIds, expr, IsTypeRestrOf.class,
						0);
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
		} while (tryMatch(TokenTypes.COMMA));
		return list;
	}

	private final TypeId parseTypeId() {
		TypeId type = null;
		if (!inPredicateMode()) {
			type = graph.createTypeId();
		}
		if (tryMatch(TokenTypes.CARET)) {
			if (!inPredicateMode()) {
				type.set_excluded(true);
			}
		}
		String s = matchQualifiedName();
		if (!inPredicateMode()) {
			type.set_name(s);
		}
		if (tryMatch(TokenTypes.EXCL)) {
			if (!inPredicateMode()) {
				type.set_type(true);
			}
		}
		return type;
	}

	private final EdgeVertexList parseEdgeVertexList() {
		match(TokenTypes.LPAREN);
		int offsetE = getCurrentOffset();
		Expression edgeExpr = parseExpression();
		int lengthE = getLength(offsetE);
		match(TokenTypes.COMMA);
		int offsetV = getCurrentOffset();
		Expression vertexExpr = parseExpression();
		int lengthV = getLength(offsetV);
		EdgeVertexList eVList = null;
		if (!inPredicateMode()) {
			eVList = graph.createEdgeVertexList();
			IsEdgeOrVertexExprOf eExprOf = graph.createIsEdgeOrVertexExprOf(
					edgeExpr, eVList);
			eExprOf.set_sourcePositions((createSourcePositionList(lengthE,
					offsetE)));
			IsEdgeOrVertexExprOf vExprOf = graph.createIsEdgeOrVertexExprOf(
					vertexExpr, eVList);
			vExprOf.set_sourcePositions((createSourcePositionList(lengthV,
					offsetV)));
		}
		while (tryMatch(TokenTypes.COMMA)) {
			int offsetEVList = getCurrentOffset();
			EdgeVertexList eVList2 = parseEdgeVertexList();
			int lengthEVList = getLength(offsetEVList);
			if (!inPredicateMode()) {
				IsElementOf exprOf = graph.createIsElementOf(eVList2, eVList);
				exprOf.set_sourcePositions((createSourcePositionList(
						lengthEVList, offsetEVList)));
			}
		}
		match(TokenTypes.RPAREN);
		return eVList;
	}

	private final List<VertexPosition<EdgeRestriction>> parseEdgeRestrictionList() {
		List<VertexPosition<EdgeRestriction>> list = new ArrayList<VertexPosition<EdgeRestriction>>();
		int offsetRole = 0;
		int lengthRole = 0;
		int offsetType = getCurrentOffset();
		int lengthType = 0;
		RoleId role = null;
		TypeId type = null;
		if (tryMatch(TokenTypes.AT)) {
			offsetRole = getCurrentOffset();
			role = parseRoleId();
		} else {
			type = parseTypeId();
			lengthType = getLength(offsetType);
			if (tryMatch(TokenTypes.AT)) {
				offsetRole = getCurrentOffset();
				role = parseRoleId();
			}
		}
		lengthRole = getLength(offsetRole);
		EdgeRestriction er = null;
		if (!inPredicateMode()) {
			er = graph.createEdgeRestriction();
			if (type != null) {
				IsTypeIdOf typeIdOf = graph.createIsTypeIdOf(type, er);
				typeIdOf.set_sourcePositions((createSourcePositionList(
						lengthType, offsetType)));
			}
			if (role != null) {
				IsRoleIdOf roleIdOf = graph.createIsRoleIdOf(role, er);
				roleIdOf.set_sourcePositions((createSourcePositionList(
						lengthRole, offsetRole)));
			}
		}
		VertexPosition<EdgeRestriction> v = new VertexPosition<EdgeRestriction>(
				er, getLength(offsetType), offsetType);
		list.add(v);
		if (tryMatch(TokenTypes.COMMA)) {
			list.addAll(parseEdgeRestrictionList());
		}
		return list;
	}

	private final Comprehension parseLabeledReportList() {
		TupleConstruction tupConstr = null;
		boolean hasLabel = false;
		int offsetExpr = 0;
		int offset = 0;
		int offsetAsExpr = 0;
		int lengthAsExpr = 0;
		BagComprehension bagCompr = null;
		Expression expr = null;
		int lengthExpr = 0;
		Expression asExpr = null;
		match(TokenTypes.REPORT);
		do {
			hasLabel = false;
			offsetExpr = getCurrentOffset();
			offset = offsetExpr;
			expr = parseExpression();
			lengthExpr = getLength(offsetExpr);
			if (tryMatch(TokenTypes.AS)) {
				offsetAsExpr = getCurrentOffset();
				asExpr = parseExpression();
				lengthAsExpr = getLength(offsetAsExpr);
				hasLabel = true;
			}
			if (!inPredicateMode()) {
				if (bagCompr == null) {
					bagCompr = graph.createBagComprehension();
					tupConstr = graph.createTupleConstruction();
					IsCompResultDefOf e = graph.createIsCompResultDefOf(
							tupConstr, bagCompr);
					e.set_sourcePositions((createSourcePositionList(
							getLength(offset), offset)));
				}
				IsPartOf partOf = graph.createIsPartOf(expr, tupConstr);
				partOf.set_sourcePositions((createSourcePositionList(
						lengthExpr, offsetExpr)));
				if (hasLabel) {
					IsTableHeaderOf tableHeaderOf = graph
							.createIsTableHeaderOf(asExpr, bagCompr);
					tableHeaderOf
							.set_sourcePositions((createSourcePositionList(
									lengthAsExpr, offsetAsExpr)));
				}
			}
		} while (tryMatch(TokenTypes.COMMA));
		if (!inPredicateMode() && (tupConstr.getDegree(EdgeDirection.IN) == 1)) {
			Vertex v = tupConstr.getFirstEdge(EdgeDirection.IN).getAlpha();
			Edge e2 = tupConstr.getFirstEdge(EdgeDirection.OUT);
			e2.setAlpha(v);
			tupConstr.delete();
		}
		return bagCompr;
	}

	private final Comprehension parseReportClause() {
		Comprehension comprehension = null;
		boolean vartable = false;
		boolean map = false;
		switch (lookAhead(0)) {
		case REPORT:
			return parseLabeledReportList();
		case REPORTBAG:
			if (!inPredicateMode()) {
				comprehension = graph.createBagComprehension();
			}
			match();
			break;
		case REPORTSET:
			if (!inPredicateMode()) {
				comprehension = graph.createSetComprehension();
			}
			match();
			break;
		case REPORTTABLE:
			if (!inPredicateMode()) {
				comprehension = graph.createTableComprehension();
			}
			vartable = true;
			match();
			break;
		case REPORTMAP:
			if (!inPredicateMode()) {
				comprehension = graph.createMapComprehension();
			}
			map = true;
			match();
			break;
		default:
			fail("Unrecognized token");
		}
		int offset = getCurrentOffset();
		List<VertexPosition<Expression>> reportList = parseExpressionList();
		int length = getLength(offset);
		IsCompResultDefOf e = null;
		if (map) {
			if (reportList.size() != 2) {
				fail("reportMap keyExpr, valueExpr must be followed by exactly two arguments");
			}
			if (!inPredicateMode()) {
				IsKeyExprOfComprehension keyEdge = graph
						.createIsKeyExprOfComprehension(reportList.get(0).node,
								(MapComprehension) comprehension);
				IsValueExprOfComprehension valueEdge = graph
						.createIsValueExprOfComprehension(
								reportList.get(1).node,
								(MapComprehension) comprehension);
				keyEdge.set_sourcePositions(createSourcePositionList(reportList
						.get(0).length, reportList.get(0).offset));
				valueEdge.set_sourcePositions(createSourcePositionList(
						reportList.get(1).length, reportList.get(1).offset));
			}
		} else if (vartable) {
			if ((reportList.size() != 3) && (reportList.size() != 4)) {
				fail("reportTable columHeaderExpr, rowHeaderExpr, cellContent [,tableHeader] must be followed by three or for arguments");
			}
			if (!inPredicateMode()) {
				IsColumnHeaderExprOf cHeaderE = graph
						.createIsColumnHeaderExprOf((reportList.get(0)).node,
								(TableComprehension) comprehension);
				cHeaderE
						.set_sourcePositions((createSourcePositionList(
								(reportList.get(0)).length,
								(reportList.get(0)).offset)));
				IsRowHeaderExprOf rHeaderE = graph.createIsRowHeaderExprOf(
						(reportList.get(1)).node,
						(TableComprehension) comprehension);
				rHeaderE
						.set_sourcePositions((createSourcePositionList(
								(reportList.get(1)).length,
								(reportList.get(1)).offset)));
				e = graph.createIsCompResultDefOf((reportList.get(2)).node,
						comprehension);
				e.set_sourcePositions(createSourcePositionList((reportList
						.get(2)).length, (reportList.get(2)).offset));
				if (reportList.size() == 4) {
					IsTableHeaderOf tHeaderE = graph.createIsTableHeaderOf(
							(reportList.get(3)).node,
							(ComprehensionWithTableHeader) comprehension);
					tHeaderE.set_sourcePositions((createSourcePositionList(
							(reportList.get(3)).length,
							(reportList.get(3)).offset)));
				}
			}
		} else {
			if (!inPredicateMode()) {
				if (reportList.size() > 1) {
					TupleConstruction tupConstr = (TupleConstruction) createMultipleEdgesToParent(
							reportList, graph.createTupleConstruction(),
							IsPartOf.class);
					e = graph.createIsCompResultDefOf(tupConstr, comprehension);
				} else {
					e = graph.createIsCompResultDefOf((reportList.get(0)).node,
							comprehension);
				}
				e
						.set_sourcePositions((createSourcePositionList(length,
								offset)));
			}
		}
		return comprehension;
	}

	private final Comprehension parseSimpleQuery() {
		match(TokenTypes.FROM);
		int offsetDecl = getCurrentOffset();
		List<VertexPosition<SimpleDeclaration>> declarations = parseDeclarationList();
		int lengthDecl = getLength(offsetDecl);
		duringParsingvariableSymbolTable.blockBegin();
		Declaration declaration = null;
		if (!inPredicateMode()) {
			declaration = graph.createDeclaration();
			createMultipleEdgesToParent(declarations, declaration,
					IsSimpleDeclOf.class, false);
		}
		if (tryMatch(TokenTypes.IN)) {
			int offsetSubgraph = getCurrentOffset();
			Expression subgraphExpr = parseExpression();
			int lengthSubgraph = getLength(offsetSubgraph);
			lengthDecl += lengthSubgraph;
			if (!inPredicateMode()) {
				IsSubgraphOf subgraphOf = graph.createIsSubgraphOf(
						subgraphExpr, declaration);
				subgraphOf.set_sourcePositions((createSourcePositionList(
						lengthSubgraph, offsetSubgraph)));
			}
		}
		if (tryMatch(TokenTypes.WITH)) {
			int offsetConstraint = getCurrentOffset();
			Expression constraintExpr = parseExpression();
			int lengthConstraint = getLength(offsetConstraint);
			lengthDecl += lengthConstraint;
			if (!inPredicateMode()) {
				IsConstraintOf constraintOf = graph.createIsConstraintOf(
						constraintExpr, declaration);
				constraintOf.set_sourcePositions((createSourcePositionList(
						lengthConstraint, offsetConstraint)));
			}
		}
		Comprehension comprehension = parseReportClause();
		if (!inPredicateMode()) {
			IsCompDeclOf comprDeclOf = graph.createIsCompDeclOf(declaration,
					comprehension);
			comprDeclOf.set_sourcePositions((createSourcePositionList(
					lengthDecl, offsetDecl)));
		}
		match(TokenTypes.END);
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
			if (!tryMatch(TokenTypes.SMILEY)) {
				parseRestrictedExpression();
			}
		} catch (ParsingException ex) {
		}
		if (predicateEnd()) {
			expr = parseRegBackwardVertexSetOrPathSystem();
		} else {
			predicateStart();
			try {
				parseRestrictedExpression();
			} catch (ParsingException ex) {
			}
			if (predicateEnd()) {
				int offsetArg1 = getCurrentOffset();
				expr = parseRestrictedExpression();
				int lengthArg1 = getLength(offsetArg1);
				if (lookAhead(0) == TokenTypes.SMILEY) {
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
			restrExpr = parseRestrictedExpression();
			if (!inPredicateMode()) {
				int lengthExpr = getLength(offsetExpr);
				PathExistence pe = graph.createPathExistence();
				// add start vertex
				IsStartExprOf startVertexOf = graph.createIsStartExprOf(expr,
						pe);
				startVertexOf.set_sourcePositions((createSourcePositionList(
						lengthArg1, offsetArg1)));
				// add target vertex
				IsTargetExprOf targetVertexOf = graph.createIsTargetExprOf(
						restrExpr, pe);
				targetVertexOf.set_sourcePositions((createSourcePositionList(
						lengthExpr, offsetExpr)));
				// add pathdescription
				IsPathOf pathOf = graph.createIsPathOf(pathDescr, pe);
				pathOf.set_sourcePositions((createSourcePositionList(
						lengthPathDescr, offsetPathDescr)));
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
				startVertexOf.set_sourcePositions((createSourcePositionList(
						lengthArg1, offsetArg1)));
				// add pathdescr
				IsPathOf pathOf = graph.createIsPathOf(pathDescr, fvs);
				pathOf.set_sourcePositions((createSourcePositionList(
						lengthPathDescr, offsetPathDescr)));
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
		match(TokenTypes.SMILEY);
		int offsetPathDescr = getCurrentOffset();
		PathDescription pathDescr = parseAltPathDescription();
		int lengthPathDescr = getLength(offsetPathDescr);
		int offsetOperator2 = getCurrentOffset();
		if (tryMatch(TokenTypes.SMILEY)) {
			offsetExpr = getCurrentOffset();
			restrExpr = parseRestrictedExpression();
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
		if (tryMatch(TokenTypes.SMILEY)) {
			isPathSystem = true;
		}
		int offsetExpr = getCurrentOffset();
		Expression restrExpr = parseRestrictedExpression();
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
				targetVertexOf.set_sourcePositions((createSourcePositionList(
						lengthExpr, offsetExpr)));
				IsPathOf pathOf = graph.createIsPathOf(pathDescr, bs);
				pathOf.set_sourcePositions((createSourcePositionList(
						lengthPathDescr, offsetPathDescr)));
				return bs;
			}
		}
		return null;
	}

	private final Expression parseNumericLiteral() {
		if (lookAhead(0) == TokenTypes.REALLITERAL) {
			RealLiteral literal = null;
			if (!inPredicateMode()) {
				literal = graph.createRealLiteral();
				literal.set_realValue(((RealToken) lookAhead).getNumber());
			}
			match();
			return literal;
		}
		if ((lookAhead(0) == TokenTypes.HEXLITERAL)
				|| (lookAhead(0) == TokenTypes.OCTLITERAL)) {
			IntLiteral literal = null;
			if (!inPredicateMode()) {
				literal = graph.createIntLiteral();
				literal.set_intValue(((IntegerToken) lookAhead).getNumber());
			}
			match();
			return literal;
		}
		if (lookAhead(0) == TokenTypes.INTLITERAL) {
			int value = ((IntegerToken) lookAhead).getNumber();
			double decValue = 0;
			match();
			if (lookAhead(0) == TokenTypes.DOT) {
				match();
				if ((lookAhead(0) == TokenTypes.INTLITERAL)
						|| (lookAhead(0) == TokenTypes.OCTLITERAL)) {
					decValue = ((IntegerToken) lookAhead).getDecValue();
					match();
				} else if (lookAhead(0) == TokenTypes.REALLITERAL) {
					decValue = ((RealToken) lookAhead).getNumber();
					match();
				} else {
					fail("Unrecognized token as part of decimal value");
				}
				if (!inPredicateMode()) {
					while (decValue > 1) {
						decValue /= 10;
					}
					RealLiteral literal = graph.createRealLiteral();
					literal.set_realValue(value + decValue);
					return literal;
				}
				return null;
			} else {
				if (!inPredicateMode()) {
					IntLiteral literal = graph.createIntLiteral();
					literal.set_intValue(value);
					return literal;
				}
				return null;
			}
		}
		fail("No numeric literal");
		return null;
	}

	private final Expression parseLiteral() {
		if (lookAhead(0) != null) {
			switch (lookAhead(0)) {
			case REALLITERAL:
			case HEXLITERAL:
			case INTLITERAL:
			case OCTLITERAL:
				return parseNumericLiteral();
			case STRING:
				StringLiteral sl = null;
				if (!inPredicateMode()) {
					sl = graph.createStringLiteral();
					sl.set_stringValue(lookAhead.getValue());
				}
				match();
				return sl;
			case THISEDGE:
				match();
				ThisEdge te = null;
				if (!inPredicateMode()) {
					te = graph.getFirstThisEdge();
					if (te == null) {
						te = graph.createThisEdge();
					}
				}
				return te;
			case THISVERTEX:
				match();
				ThisVertex tv = null;
				if (!inPredicateMode()) {
					tv = graph.getFirstThisVertex();
					if (tv == null) {
						tv = graph.createThisVertex();
					}
				}
				return tv;
			case TRUE:
				match();
				BoolLiteral tl = null;
				if (!inPredicateMode()) {
					graph.getFirstBoolLiteral();
					while ((tl != null)
							&& (tl.get_boolValue() != TrivalentBoolean.TRUE)) {
						tl = tl.getNextBoolLiteral();
					}
					if ((tl == null)
							|| (tl.get_boolValue() != TrivalentBoolean.TRUE)) {
						tl = graph.createBoolLiteral();
						tl.set_boolValue(TrivalentBoolean.TRUE);
					}
				}
				return tl;
			case FALSE:
				match();
				BoolLiteral fl = null;
				if (!inPredicateMode()) {
					tl = graph.getFirstBoolLiteral();
					while ((fl != null)
							&& (fl.get_boolValue() != TrivalentBoolean.FALSE)) {
						fl = fl.getNextBoolLiteral();
					}
					if ((fl == null)
							|| (fl.get_boolValue() != TrivalentBoolean.FALSE)) {
						fl = graph.createBoolLiteral();
						fl.set_boolValue(TrivalentBoolean.FALSE);
					}
				}
				return fl;
			case NULL_VALUE:
				match();
				BoolLiteral nl = null;
				if (!inPredicateMode()) {
					nl = graph.getFirstBoolLiteral();
					while ((nl != null)
							&& (nl.get_boolValue() != TrivalentBoolean.NULL)) {
						nl = nl.getNextBoolLiteral();
					}
					if ((nl == null)
							|| (nl.get_boolValue() != TrivalentBoolean.NULL)) {
						nl = graph.createBoolLiteral();
						nl.set_boolValue(TrivalentBoolean.NULL);
					}
				}
				return nl;
			}
		}
		fail("Unrecognized literal");
		return null;
	}

	public Greql2Schema getSchema() {
		return schema;
	}

}
