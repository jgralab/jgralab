package de.uni_koblenz.jgralabtest.greql2;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.junit.Test;

import de.uni_koblenz.jgralab.Edge;
import de.uni_koblenz.jgralab.EdgeDirection;
import de.uni_koblenz.jgralab.GraphIO;
import de.uni_koblenz.jgralab.Vertex;
import de.uni_koblenz.jgralab.greql2.exception.UndefinedVariableException;
import de.uni_koblenz.jgralab.greql2.parser.ManualGreqlParser;
import de.uni_koblenz.jgralab.greql2.parser.ParsingException;
import de.uni_koblenz.jgralab.greql2.schema.AggregationPathDescription;
import de.uni_koblenz.jgralab.greql2.schema.AlternativePathDescription;
import de.uni_koblenz.jgralab.greql2.schema.BagComprehension;
import de.uni_koblenz.jgralab.greql2.schema.BoolLiteral;
import de.uni_koblenz.jgralab.greql2.schema.ConditionalExpression;
import de.uni_koblenz.jgralab.greql2.schema.Declaration;
import de.uni_koblenz.jgralab.greql2.schema.EdgePathDescription;
import de.uni_koblenz.jgralab.greql2.schema.EdgeRestriction;
import de.uni_koblenz.jgralab.greql2.schema.EdgeSetExpression;
import de.uni_koblenz.jgralab.greql2.schema.EdgeSubgraphExpression;
import de.uni_koblenz.jgralab.greql2.schema.ForwardVertexSet;
import de.uni_koblenz.jgralab.greql2.schema.FunctionApplication;
import de.uni_koblenz.jgralab.greql2.schema.FunctionId;
import de.uni_koblenz.jgralab.greql2.schema.Greql2;
import de.uni_koblenz.jgralab.greql2.schema.Greql2Expression;
import de.uni_koblenz.jgralab.greql2.schema.IntLiteral;
import de.uni_koblenz.jgralab.greql2.schema.IntermediateVertexPathDescription;
import de.uni_koblenz.jgralab.greql2.schema.IsAlternativePathOf;
import de.uni_koblenz.jgralab.greql2.schema.IsArgumentOf;
import de.uni_koblenz.jgralab.greql2.schema.IsBoundVarOf;
import de.uni_koblenz.jgralab.greql2.schema.IsCompDeclOf;
import de.uni_koblenz.jgralab.greql2.schema.IsCompResultDefOf;
import de.uni_koblenz.jgralab.greql2.schema.IsConstraintOf;
import de.uni_koblenz.jgralab.greql2.schema.IsEdgeExprOf;
import de.uni_koblenz.jgralab.greql2.schema.IsEdgeRestrOf;
import de.uni_koblenz.jgralab.greql2.schema.IsFirstValueOf;
import de.uni_koblenz.jgralab.greql2.schema.IsFunctionIdOf;
import de.uni_koblenz.jgralab.greql2.schema.IsGoalRestrOf;
import de.uni_koblenz.jgralab.greql2.schema.IsIntermediateVertexOf;
import de.uni_koblenz.jgralab.greql2.schema.IsLastValueOf;
import de.uni_koblenz.jgralab.greql2.schema.IsPartOf;
import de.uni_koblenz.jgralab.greql2.schema.IsPathOf;
import de.uni_koblenz.jgralab.greql2.schema.IsQueryExprOf;
import de.uni_koblenz.jgralab.greql2.schema.IsRecordElementOf;
import de.uni_koblenz.jgralab.greql2.schema.IsSequenceElementOf;
import de.uni_koblenz.jgralab.greql2.schema.IsSimpleDeclOf;
import de.uni_koblenz.jgralab.greql2.schema.IsStartExprOf;
import de.uni_koblenz.jgralab.greql2.schema.IsStartRestrOf;
import de.uni_koblenz.jgralab.greql2.schema.IsSubPathOf;
import de.uni_koblenz.jgralab.greql2.schema.IsTargetExprOf;
import de.uni_koblenz.jgralab.greql2.schema.IsTypeIdOf;
import de.uni_koblenz.jgralab.greql2.schema.IsTypeRestrOf;
import de.uni_koblenz.jgralab.greql2.schema.IteratedPathDescription;
import de.uni_koblenz.jgralab.greql2.schema.ListConstruction;
import de.uni_koblenz.jgralab.greql2.schema.ListRangeConstruction;
import de.uni_koblenz.jgralab.greql2.schema.PathExistence;
import de.uni_koblenz.jgralab.greql2.schema.QuantifiedExpression;
import de.uni_koblenz.jgralab.greql2.schema.Quantifier;
import de.uni_koblenz.jgralab.greql2.schema.RealLiteral;
import de.uni_koblenz.jgralab.greql2.schema.RecordConstruction;
import de.uni_koblenz.jgralab.greql2.schema.RecordElement;
import de.uni_koblenz.jgralab.greql2.schema.RecordId;
import de.uni_koblenz.jgralab.greql2.schema.RoleId;
import de.uni_koblenz.jgralab.greql2.schema.SequentialPathDescription;
import de.uni_koblenz.jgralab.greql2.schema.SetConstruction;
import de.uni_koblenz.jgralab.greql2.schema.SimpleDeclaration;
import de.uni_koblenz.jgralab.greql2.schema.SimplePathDescription;
import de.uni_koblenz.jgralab.greql2.schema.StringLiteral;
import de.uni_koblenz.jgralab.greql2.schema.ThisVertex;
import de.uni_koblenz.jgralab.greql2.schema.TrivalentBoolean;
import de.uni_koblenz.jgralab.greql2.schema.TypeId;
import de.uni_koblenz.jgralab.greql2.schema.Variable;
import de.uni_koblenz.jgralab.greql2.schema.VertexSetExpression;
import de.uni_koblenz.jgralab.greql2.schema.VertexSubgraphExpression;

/*
 * GReQL Constructs where discussions are needed
 *
 * Start & GoalRestriction:
 * Syntax --> & {AType} und --> & {ABooleanVarialbe} is identical, currently the parser requests to put
 * expressions other than TypeIds in parantheses
 *
 *
 *
 *
 *
 *
 */

public class ParserTest {

	private Greql2 parseQuery(String query) throws ParsingException {
		return parseQuery(query, null);
	}

	private Greql2 parseQuery(String query, String file)
			throws ParsingException {
		Greql2 graph = ManualGreqlParser.parse(query);
		if (file != null) {
			try {
				GraphIO.saveGraphToFile(file, graph, null);
			} catch (Exception ex) {
				throw new RuntimeException(
						"Error saving graph to file " + file, ex);
			}
		}

		return graph;
	}

	@Test
	public void testGreql2TestGraph() throws ParsingException {
		parseQuery("from i:c report i end where d:=\"drölfundfünfzig\", c:=b, b:=a, a:=\"Mensaessen\"");
	}

	@Test
	public void testExistsOne() throws Exception {
		Greql2 graph = parseQuery("exists! x:list(1..5) @ x = 5");
		Quantifier quantifier = graph.getFirstQuantifier();
		assertEquals("exists!", quantifier.getName());
		quantifier = quantifier.getNextQuantifier();
		assertNull(quantifier);
	}

	@Test
	public void testOrExpression() throws Exception {
		Greql2 graph = parseQuery("true or false");
		FunctionApplication funAp = graph.getFirstFunctionApplication();
		assertNotNull(funAp);
		IsFunctionIdOf isIdOf = funAp.getFirstIsFunctionIdOf();
		assertNotNull(isIdOf);
		FunctionId funId = (FunctionId) isIdOf.getAlpha();
		assertEquals("or", funId.getName());
	}

	@Test
	public void testAndExpression() throws Exception {
		Greql2 graph = parseQuery("true and false");
		FunctionApplication funAp = graph.getFirstFunctionApplication();
		assertNotNull(funAp);
		IsFunctionIdOf isIdOf = funAp.getFirstIsFunctionIdOf();
		assertNotNull(isIdOf);
		FunctionId funId = (FunctionId) isIdOf.getAlpha();
		assertEquals("and", funId.getName());
	}

	@Test
	public void testXorExpression() throws Exception {
		Greql2 graph = parseQuery("true xor false");
		FunctionApplication funAp = graph.getFirstFunctionApplication();
		assertNotNull(funAp);
		IsFunctionIdOf isIdOf = funAp.getFirstIsFunctionIdOf();
		assertNotNull(isIdOf);
		FunctionId funId = (FunctionId) isIdOf.getAlpha();
		assertEquals("xor", funId.getName());
	}

	@Test
	public void testEqualityExpression() throws Exception {
		Greql2 graph = parseQuery("true = false");
		FunctionApplication funAp = graph.getFirstFunctionApplication();
		assertNotNull(funAp);
		IsFunctionIdOf isIdOf = funAp.getFirstIsFunctionIdOf();
		assertNotNull(isIdOf);
		FunctionId funId = (FunctionId) isIdOf.getAlpha();
		assertEquals("equals", funId.getName());
	}

	@Test
	public void testMatchExpression() throws Exception {
		Greql2 graph = parseQuery("true =~ false");
		FunctionApplication funAp = graph.getFirstFunctionApplication();
		assertNotNull(funAp);
		IsFunctionIdOf isIdOf = funAp.getFirstIsFunctionIdOf();
		assertNotNull(isIdOf);
		FunctionId funId = (FunctionId) isIdOf.getAlpha();
		assertEquals("reMatch", funId.getName());
	}

	@Test
	public void testIdentifierWithDollar() throws Exception {
		Greql2 graph = parseQuery("from $i : V{} report $i end");
		Variable v = graph.getFirstVariable();
		assertNotNull(v);
		assertEquals("$i", v.getName());
	}

	@Test
	public void testIdentifierWithDollar2() throws Exception {
		Greql2 graph = parseQuery("using $: from i:$ report i end");
		Variable v = graph.getFirstVariable();
		assertNotNull(v);
		assertEquals("$", v.getName());
	}

	@Test
	public void testTypeId() throws Exception {
		Greql2 graph = parseQuery("V{Part!}");
		TypeId t = graph.getFirstTypeId();
		assertNotNull(t);
		assertEquals("Part", t.getName());
		assertTrue(t.isType());
		assertFalse(t.isExcluded());
	}

	@Test
	public void testTypeId2() throws Exception {
		Greql2 graph = parseQuery("V{^Part!}");
		TypeId t = graph.getFirstTypeId();
		assertNotNull(t);
		assertEquals("Part", t.getName());
		assertTrue(t.isType());
		assertTrue(t.isExcluded());
	}

	@Test
	public void testTypeId3() throws Exception {
		Greql2 graph = parseQuery("V{^Part}");
		TypeId t = graph.getFirstTypeId();
		assertNotNull(t);
		assertEquals("Part", t.getName());
		assertFalse(t.isType());
		assertTrue(t.isExcluded());
	}

	@Test
	public void testNotEqualExpression() throws Exception {
		Greql2 graph = parseQuery("true <> false");
		FunctionApplication funAp = graph.getFirstFunctionApplication();
		assertNotNull(funAp);
		IsFunctionIdOf isIdOf = funAp.getFirstIsFunctionIdOf();
		assertNotNull(isIdOf);
		FunctionId funId = (FunctionId) isIdOf.getAlpha();
		assertEquals("nequals", funId.getName());
	}

	@Test
	public void testLessThanExpression() throws Exception {
		Greql2 graph = parseQuery("5 < 6");
		FunctionApplication funAp = graph.getFirstFunctionApplication();
		assertNotNull(funAp);
		IsFunctionIdOf isIdOf = funAp.getFirstIsFunctionIdOf();
		assertNotNull(isIdOf);
		FunctionId funId = (FunctionId) isIdOf.getAlpha();
		assertEquals("leThan", funId.getName());
	}

	@Test
	public void testLessOrEqualExpression() throws Exception {
		Greql2 graph = parseQuery("5 <= 6");
		FunctionApplication funAp = graph.getFirstFunctionApplication();
		assertNotNull(funAp);
		IsFunctionIdOf isIdOf = funAp.getFirstIsFunctionIdOf();
		assertNotNull(isIdOf);
		FunctionId funId = (FunctionId) isIdOf.getAlpha();
		assertEquals("leEqual", funId.getName());
	}

	@Test
	public void testBooleanLiteral() throws Exception {
		System.out.println("------------------");
		System.out.println("Testing BooleanLiteral");
		System.out.println("------------------");
		Greql2 graph = parseQuery("true");
		BoolLiteral lit = graph.getFirstBoolLiteral();
		assertNotNull(lit);
		assertEquals(TrivalentBoolean.TRUE, lit.getBoolValue());
		graph = parseQuery("false");
		lit = graph.getFirstBoolLiteral();
		assertNotNull(lit);
		assertEquals(TrivalentBoolean.FALSE, lit.getBoolValue());
	}

	@Test
	public void testIntegerLiteral() throws Exception {
		System.out.println("------------------");
		System.out.println("Testing IntLiteral");
		System.out.println("------------------");
		Greql2 graph = parseQuery("5");
		IntLiteral lit = graph.getFirstIntLiteral();
		assertNotNull(lit);
		assertEquals(5, lit.getIntValue());
	}

	@Test
	public void testHexLiteral() throws Exception {
		System.out.println("------------------");
		System.out.println("Testing HexLiteral");
		System.out.println("------------------");
		Greql2 graph = parseQuery("0x5");
		IntLiteral lit = graph.getFirstIntLiteral();
		assertNotNull(lit);
		assertEquals(5, lit.getIntValue());
		graph = parseQuery("0xA");
		lit = graph.getFirstIntLiteral();
		assertNotNull(lit);
		assertEquals(10, lit.getIntValue());
	}

	@Test
	public void testOctLiteral() throws Exception {
		System.out.println("------------------");
		System.out.println("Testing OctLiteral");
		System.out.println("------------------");
		Greql2 graph = parseQuery("05");
		IntLiteral lit = graph.getFirstIntLiteral();
		assertNotNull(lit);
		assertEquals(5, lit.getIntValue());
		graph = parseQuery("011");
		lit = graph.getFirstIntLiteral();
		assertNotNull(lit);
		assertEquals(9, lit.getIntValue());
	}

	@Test
	public void testRealLiteral() throws Exception {
		System.out.println("------------------");
		System.out.println("Testing RealLiteral");
		System.out.println("------------------");
		Greql2 graph = null;
		RealLiteral lit = null;
		graph = parseQuery("5.0");
		lit = graph.getFirstRealLiteral();
		assertNotNull(lit);
		assertEquals(5, lit.getRealValue(), 0.0001);
		graph = parseQuery("5.0f");
		lit = graph.getFirstRealLiteral();
		assertNotNull(lit);
		assertEquals(5.0, lit.getRealValue(), 0.0001);
		graph = parseQuery("0.5");
		lit = graph.getFirstRealLiteral();
		assertNotNull(lit);
		assertEquals(0.5, lit.getRealValue(), 0.0001);
	}

	@Test
	public void testUnaryExpressionUMinus() throws Exception {
		Greql2 graph = parseQuery("-5");
		FunctionApplication funAp = graph.getFirstFunctionApplication();
		assertNotNull(funAp);
		IsFunctionIdOf isIdOf = funAp.getFirstIsFunctionIdOf();
		assertNotNull(isIdOf);
		FunctionId funId = (FunctionId) isIdOf.getAlpha();
		assertEquals("uminus", funId.getName());
	}

	@Test
	public void testUnaryExpressionNot() throws Exception {
		Greql2 graph = parseQuery("not true");
		FunctionApplication funAp = graph.getFirstFunctionApplication();
		assertNotNull(funAp);
		IsFunctionIdOf isIdOf = funAp.getFirstIsFunctionIdOf();
		assertNotNull(isIdOf);
		FunctionId funId = (FunctionId) isIdOf.getAlpha();
		assertEquals("not", funId.getName());
	}

	@Test
	public void testListConstruction() throws Exception {
		Greql2 graph = parseQuery("list(10,11,12,13)");
		ListConstruction constr = graph.getFirstListConstruction();
		assertNotNull(constr);
		assertEquals(4, constr.getDegree(IsPartOf.class));
	}

	@Test
	public void testGreTLQuery() throws Exception {
		String query = "from t : V{Vertex}    " + "report t --> "
				+ "     & {hasType(thisVertex, \"MyType\")} " + "end";
		Greql2 graph = parseQuery(query);
		assertNotNull(graph);
		ThisVertex tv = graph.getFirstThisVertex();
		assertNotNull(tv);
		IsArgumentOf argOf = tv.getFirstIsArgumentOf(EdgeDirection.OUT);
		assertNotNull(argOf);
		assertEquals(graph.getFirstFunctionApplication(), argOf.getOmega());
		StringLiteral sl = graph.getFirstStringLiteral();
		assertNotNull(sl);
		assertEquals("MyType", sl.getStringValue());
		argOf = sl.getFirstIsArgumentOf(EdgeDirection.OUT);
		assertNotNull(argOf);
		assertEquals(graph.getFirstFunctionApplication(), argOf.getOmega());
		tv = tv.getNextThisVertex();
		assertNull(tv);
	}

	@Test
	public void testRole() throws Exception {
		String queryString = "from var: V{Variable} report <>--{@undefinedRole} end";
		Greql2 graph = parseQuery(queryString);
		assertNotNull(graph);
		RoleId id = graph.getFirstRoleId();
		assertNotNull(id);
		assertEquals("undefinedRole", id.getName());
		AggregationPathDescription agg = graph
				.getFirstAggregationPathDescription();
		assertNotNull(agg);
		Edge e = id.getFirstIsRoleIdOf();
		assertNotNull(e);
		EdgeRestriction er = (EdgeRestriction) e.getOmega();
		IsEdgeRestrOf erof = er.getFirstIsEdgeRestrOf();
		assertNotNull(erof);
		assertEquals(agg, erof.getOmega());
	}

	@Test
	public void testListRangeConstruction() throws Exception {
		Greql2 graph = parseQuery("list(10..13)");
		ListRangeConstruction constr = graph.getFirstListRangeConstruction();
		assertNotNull(constr);
		IsFirstValueOf firstValueEdge = constr
				.getFirstIsFirstValueOf(EdgeDirection.IN);
		assertNotNull(firstValueEdge);
		IntLiteral firstValue = (IntLiteral) firstValueEdge.getAlpha();
		assertEquals(10, firstValue.getIntValue());
		IsLastValueOf lastValueEdge = constr
				.getFirstIsLastValueOf(EdgeDirection.IN);
		assertNotNull(lastValueEdge);
		IntLiteral lastValue = (IntLiteral) lastValueEdge.getAlpha();
		assertEquals(13, lastValue.getIntValue());
	}

	@Test
	public void testSetConstruction() throws Exception {
		Greql2 graph = parseQuery("set(10,11,12,13)");
		SetConstruction constr = graph.getFirstSetConstruction();
		assertNotNull(constr);
		assertEquals(4, constr.getDegree(IsPartOf.class));
	}

	@Test
	public void testRecordConstruction() throws Exception {
		Greql2 graph = parseQuery("rec(a:5,b:\"Yes\")");
		RecordConstruction constr = graph.getFirstRecordConstruction();
		assertNotNull(constr);
		IsRecordElementOf recElemEdge = constr.getFirstIsRecordElementOf();
		RecordElement elem = (RecordElement) recElemEdge.getAlpha();
		RecordId recId = (RecordId) elem.getFirstIsRecordIdOf().getAlpha();
		assertEquals("a", recId.getName());
		Vertex recExpr = elem.getFirstIsRecordExprOf().getAlpha();
		assertEquals(5, ((IntLiteral) recExpr).getIntValue());
	}

	@Test
	public void testForallExpression() throws Exception {
		Greql2 graph = parseQuery("forall v:set(1,2,3) @ v < 7");
		Variable var = graph.getFirstVariable();
		assertNotNull(var);
		assertEquals("v", var.getName());
		QuantifiedExpression expr = graph.getFirstQuantifiedExpression();
		assertNotNull(expr);
		Quantifier quantifier = (Quantifier) expr.getFirstIsQuantifierOf()
				.getAlpha();
		assertNotNull(quantifier);
		assertEquals("forall", quantifier.getName());
	}

	@Test
	public void testExistsExpression() throws Exception {
		Greql2 graph = parseQuery("exists v:set(1,2,3) @ v < 7");
		Variable var = graph.getFirstVariable();
		assertNotNull(var);
		assertEquals("v", var.getName());
		QuantifiedExpression expr = graph.getFirstQuantifiedExpression();
		assertNotNull(expr);
		Quantifier quantifier = (Quantifier) expr.getFirstIsQuantifierOf()
				.getAlpha();
		assertNotNull(quantifier);
		assertEquals("exists", quantifier.getName());
	}

	@Test
	public void testExistsOneExpression() throws Exception {
		Greql2 graph = parseQuery("exists! v:set(1,2,3) @ v < 7");
		Variable var = graph.getFirstVariable();
		assertNotNull(var);
		assertEquals("v", var.getName());
		QuantifiedExpression expr = graph.getFirstQuantifiedExpression();
		assertNotNull(expr);
		Quantifier quantifier = (Quantifier) expr.getFirstIsQuantifierOf()
				.getAlpha();
		assertNotNull(quantifier);
		assertEquals("exists!", quantifier.getName());
	}

	// @Test
	// public void testExistsOneQuantifier() throws Exception {
	// ManualGreql2Parser parser = getParser("exists!");
	// parser.initialize();
	// try {
	// Quantifier q = parser.quantifier();
	// assertNotNull(q);
	// } catch (RecognitionException e) {
	// // TODO Auto-generated catch block
	// e.printStackTrace();
	// }
	// }

	@Test
	public void testVariableList() throws Exception {
		Greql2 graph = parseQuery("from a,b,c,d:V report a end");
		Variable var = graph.getFirstVariable();
		assertNotNull(var);
		assertEquals("a", var.getName());
		var = var.getNextVariable();
		assertNotNull(var);
		assertEquals("b", var.getName());
		var = var.getNextVariable();
		assertNotNull(var);
		assertEquals("c", var.getName());
		var = var.getNextVariable();
		assertNotNull(var);
		assertEquals("d", var.getName());
	}

	@Test
	public void testSimpleQueryWithConstraint() throws Exception {
		Greql2 graph = parseQuery("from var: V with isPrime(var) report var end");
		Variable var = graph.getFirstVariable();
		assertNotNull(var);
		assertEquals("var", var.getName());
		BagComprehension comp = graph.getFirstBagComprehension();
		assertNotNull(comp);
		IsCompDeclOf declEdge = comp.getFirstIsCompDeclOf();
		assertNotNull(declEdge);
		Declaration decl = (Declaration) declEdge.getAlpha();
		IsConstraintOf constraintEdge = decl.getFirstIsConstraintOf();
		assertNotNull(constraintEdge);
		FunctionApplication funAp = (FunctionApplication) constraintEdge
				.getAlpha();
		FunctionId funId = (FunctionId) funAp.getFirstIsFunctionIdOf()
				.getAlpha();
		assertEquals("isPrime", funId.getName());
	}

	@Test
	public void testSimpleQueryWithMultipleDeclarations() throws Exception {
		Greql2 graph = parseQuery("from var: V{Definition}, def: V{WhereExpression} report var end");
		Variable var = graph.getFirstVariable();
		assertNotNull(var);
		assertEquals("var", var.getName());
		var = var.getNextVariable();
		assertNotNull(var);
		assertEquals("def", var.getName());
		BagComprehension comp = graph.getFirstBagComprehension();
		assertNotNull(comp);
		IsCompDeclOf declEdge = comp.getFirstIsCompDeclOf();
		assertNotNull(declEdge);
		Declaration decl = (Declaration) declEdge.getAlpha();
		/* testing first simple declaration var:V{Definition} */
		IsSimpleDeclOf simpleDeclEdge = decl.getFirstIsSimpleDeclOf();
		assertNotNull(simpleDeclEdge);
		SimpleDeclaration simpleDecl = (SimpleDeclaration) simpleDeclEdge
				.getAlpha();
		var = (Variable) simpleDecl.getFirstIsDeclaredVarOf().getAlpha();
		assertEquals("var", var.getName());
		VertexSetExpression vset = (VertexSetExpression) simpleDecl
				.getFirstIsTypeExprOf().getAlpha();
		IsTypeRestrOf typeRestrEdge = vset.getFirstIsTypeRestrOf();
		assertNotNull(typeRestrEdge);
		TypeId typeId = (TypeId) typeRestrEdge.getAlpha();
		assertEquals("Definition", typeId.getName());
		/* testing second simple declaration def:V{WhereExpression} */
		simpleDeclEdge = simpleDeclEdge.getNextIsSimpleDeclOf();
		assertNotNull(simpleDeclEdge);
		simpleDecl = (SimpleDeclaration) simpleDeclEdge.getAlpha();
		var = (Variable) simpleDecl.getFirstIsDeclaredVarOf().getAlpha();
		assertEquals("def", var.getName());
		vset = (VertexSetExpression) simpleDecl.getFirstIsTypeExprOf()
				.getAlpha();
		typeRestrEdge = vset.getFirstIsTypeRestrOf();
		assertNotNull(typeRestrEdge);
		typeId = (TypeId) typeRestrEdge.getAlpha();
		assertEquals("WhereExpression", typeId.getName());
	}

	@Test
	public void testSimpleQuery1() throws Exception {
		Greql2 graph = parseQuery("from var: V{Definition}, def: V{WhereExpression} with var -->{IsDefinitionOf} | -->{IsVarOf}  def report var end");
		BagComprehension comp = graph.getFirstBagComprehension();
		assertNotNull(comp);
		IsCompDeclOf declEdge = comp.getFirstIsCompDeclOf();
		assertNotNull(declEdge);
		Declaration decl = (Declaration) declEdge.getAlpha();
		/* testing first simple declaration var:V{Definition} */
		IsSimpleDeclOf simpleDeclEdge = decl.getFirstIsSimpleDeclOf();
		assertNotNull(simpleDeclEdge);
		SimpleDeclaration simpleDecl = (SimpleDeclaration) simpleDeclEdge
				.getAlpha();
		Variable var = (Variable) simpleDecl.getFirstIsDeclaredVarOf()
				.getAlpha();
		assertEquals("var", var.getName());
		VertexSetExpression vset = (VertexSetExpression) simpleDecl
				.getFirstIsTypeExprOf().getAlpha();
		IsTypeRestrOf typeRestrEdge = vset.getFirstIsTypeRestrOf();
		assertNotNull(typeRestrEdge);
		TypeId typeId = (TypeId) typeRestrEdge.getAlpha();
		assertEquals("Definition", typeId.getName());
		/* testing second simple declaration def:V{WhereExpression} */
		simpleDeclEdge = simpleDeclEdge.getNextIsSimpleDeclOf();
		assertNotNull(simpleDeclEdge);
		simpleDecl = (SimpleDeclaration) simpleDeclEdge.getAlpha();
		var = (Variable) simpleDecl.getFirstIsDeclaredVarOf().getAlpha();
		assertEquals("def", var.getName());
		vset = (VertexSetExpression) simpleDecl.getFirstIsTypeExprOf()
				.getAlpha();
		typeRestrEdge = vset.getFirstIsTypeRestrOf();
		assertNotNull(typeRestrEdge);
		typeId = (TypeId) typeRestrEdge.getAlpha();
		assertEquals("WhereExpression", typeId.getName());
		IsConstraintOf constraintEdge = decl.getFirstIsConstraintOf();
		assertNotNull(constraintEdge);
		PathExistence constraint = (PathExistence) constraintEdge.getAlpha();
		IsStartExprOf startEdge = constraint.getFirstIsStartExprOf();
		assertNotNull(startEdge);
		var = (Variable) startEdge.getAlpha();
		assertEquals("var", var.getName());
		IsTargetExprOf targetEdge = constraint.getFirstIsTargetExprOf();
		assertNotNull(targetEdge);
		var = (Variable) targetEdge.getAlpha();
		IsPathOf pathOfEdge = constraint.getFirstIsPathOf();
		assertNotNull(pathOfEdge);
		AlternativePathDescription pathDescr = (AlternativePathDescription) pathOfEdge
				.getAlpha();
		IsAlternativePathOf altEdge = pathDescr.getFirstIsAlternativePathOf();
		assertNotNull(altEdge);
		SimplePathDescription simplePath = (SimplePathDescription) altEdge
				.getAlpha();
		EdgeRestriction edgeRestr = (EdgeRestriction) simplePath
				.getFirstIsEdgeRestrOf().getAlpha();
		typeId = (TypeId) edgeRestr.getFirstIsTypeIdOf().getAlpha();
		assertEquals("IsDefinitionOf", typeId.getName());
		altEdge = altEdge.getNextIsAlternativePathOf();
		assertNotNull(altEdge);
		simplePath = (SimplePathDescription) altEdge.getAlpha();
		edgeRestr = (EdgeRestriction) simplePath.getFirstIsEdgeRestrOf()
				.getAlpha();
		typeId = (TypeId) edgeRestr.getFirstIsTypeIdOf().getAlpha();
		assertEquals("IsVarOf", typeId.getName());
		IsCompResultDefOf resultEdge = comp
				.getFirstIsCompResultDefOf(EdgeDirection.IN);
		var = (Variable) resultEdge.getAlpha();
		assertEquals("var", var.getName());

	}

	@Test
	public void testSimpleQuery2() throws Exception {
		Greql2 graph = parseQuery("using FOO: from i: toSet(FOO) report i end");
		Greql2Expression root = graph.getFirstGreql2Expression();
		assertNotNull(root);
		IsBoundVarOf boundVarEdge = root.getFirstIsBoundVarOf();
		assertNotNull(boundVarEdge);
		Variable boundVar = (Variable) boundVarEdge.getAlpha();
		assertEquals("FOO", boundVar.getName());

		BagComprehension comp = (BagComprehension) graph
				.getFirstIsQueryExprOfInGraph().getAlpha();
		assertNotNull(comp);
		IsCompDeclOf declEdge = comp.getFirstIsCompDeclOf();
		assertNotNull(declEdge);
		Declaration decl = (Declaration) declEdge.getAlpha();
		/* testing first simple declaration var:V{Definition} */
		IsSimpleDeclOf simpleDeclEdge = decl.getFirstIsSimpleDeclOf();
		assertNotNull(simpleDeclEdge);
		SimpleDeclaration simpleDecl = (SimpleDeclaration) simpleDeclEdge
				.getAlpha();
		Variable var = (Variable) simpleDecl.getFirstIsDeclaredVarOf()
				.getAlpha();
		assertEquals("i", var.getName());
		FunctionApplication funAp = (FunctionApplication) simpleDecl
				.getFirstIsTypeExprOf().getAlpha();
		FunctionId funId = (FunctionId) funAp.getFirstIsFunctionIdOf()
				.getAlpha();
		assertEquals("toSet", funId.getName());
		var = (Variable) funAp.getFirstIsArgumentOf().getAlpha();
		assertEquals("FOO", var.getName());
		assertEquals(boundVar, var);
	}

	@Test
	public void testUsing() throws Exception {
		Greql2 graph = parseQuery("using A: from b:A report b end");
		Greql2Expression root = graph.getFirstGreql2Expression();
		assertNotNull(root);
		IsBoundVarOf boundVarEdge = root.getFirstIsBoundVarOf();
		assertNotNull(boundVarEdge);
		Variable var = (Variable) boundVarEdge.getAlpha();
		assertEquals("A", var.getName());
	}

	@Test
	public void testDefinitionList() throws Exception {
		parseQuery("let a:=7, b:=5 in a+b");
	}

	@Test
	public void testEvaluateListAccess() throws Exception {
		String queryString = "let x := list ( \"bratwurst\", \"currywurst\") in from i:V{Identifier} report x[3] end";
		parseQuery(queryString);

	}

	@Test
	public void testIsCycle() throws Exception {
		String queryString = "from v : V reportSet isCycle(extractPath(pathSystem(v, -->), v)) end";
		parseQuery(queryString);
	}

	@Test
	public void testLetExpression() throws Exception {
		Greql2 graph = parseQuery("let a:=7 in from b:list(1..a) report b end");
		Variable var = graph.getFirstVariable();
		assertNotNull(var);
		assertEquals("a", var.getName());
		var = var.getNextVariable();
		assertNotNull(var);
		assertEquals("b", var.getName());
	}

	@Test
	public void testFRQuery() throws Exception {
		Greql2 graph = parseQuery("from v:V report v end");
		Variable var = graph.getFirstVariable();
		assertNotNull(var);
		assertEquals("v", var.getName());
		VertexSetExpression expr = graph.getFirstVertexSetExpression();
		assertNotNull(expr);
	}

	@Test
	public void testFunctionApplication() throws Exception {
		Greql2 graph = parseQuery("from v:V report degree(v) end");
		Variable var = graph.getFirstVariable();
		assertNotNull(var);
		assertEquals("v", var.getName());
		VertexSetExpression expr = graph.getFirstVertexSetExpression();
		assertNotNull(expr);
		FunctionApplication funAp = graph.getFirstFunctionApplication();
		assertNotNull(funAp);
		FunctionId funId = (FunctionId) funAp.getFirstIsFunctionIdOf()
				.getAlpha();
		assertEquals("degree", funId.getName());
	}

	@Test
	public void testEdgeSubgraphExpression2() throws Exception {
		parseQuery("from i: V in eSubgraph{^IsDefinitionOf} report i end");
		// GraphIO.saveGraphToFile("/home/dbildh/greqlgraph.tg", graph, null);
	}

	@Test
	public void testConditionalExpression() throws Exception {
		Greql2 graph = parseQuery("1=2 ? true : false : 3=4");
		ConditionalExpression condExpr = graph.getFirstConditionalExpression();
		assertNotNull(condExpr);
		FunctionApplication condition = (FunctionApplication) condExpr
				.getFirstIsConditionOf().getAlpha();
		assertNotNull(condition);
		FunctionId conditionId = (FunctionId) condition
				.getFirstIsFunctionIdOf().getAlpha();
		assertNotNull(conditionId);
		assertEquals("equals", conditionId.getName());
		IntLiteral arg1 = (IntLiteral) condition.getFirstIsArgumentOf()
				.getAlpha();
		IntLiteral arg2 = (IntLiteral) condition.getFirstIsArgumentOf()
				.getNextIsArgumentOf().getAlpha();
		assertEquals(1, arg1.getIntValue());
		assertEquals(2, arg2.getIntValue());
		BoolLiteral trueExpression = (BoolLiteral) condExpr
				.getFirstIsTrueExprOf().getAlpha();
		assertNotNull(trueExpression);
		assertEquals(TrivalentBoolean.TRUE, trueExpression.getBoolValue());
		BoolLiteral falseExpression = (BoolLiteral) condExpr
				.getFirstIsFalseExprOf().getAlpha();
		assertNotNull(falseExpression);
		assertEquals(TrivalentBoolean.FALSE, falseExpression.getBoolValue());
		FunctionApplication nullExpression = (FunctionApplication) condExpr
				.getFirstIsNullExprOf().getAlpha();
		FunctionId nullId = (FunctionId) nullExpression
				.getFirstIsFunctionIdOf().getAlpha();
		assertNotNull(nullId);
		assertEquals("equals", nullId.getName());
	}

	@Test
	public void testConditionalExpression2() throws Exception {
		Greql2 graph = parseQuery("1=1?1:2:3");
		Greql2Expression root = graph.getFirstGreql2Expression();
		assertNotNull(root);
		IsQueryExprOf queryEdge = root.getFirstIsQueryExprOf(EdgeDirection.IN);
		assertNotNull(queryEdge);
		ConditionalExpression condExpr = (ConditionalExpression) queryEdge
				.getAlpha();
		assertNotNull(condExpr);
		FunctionApplication condition = (FunctionApplication) condExpr
				.getFirstIsConditionOf().getAlpha();
		assertNotNull(condition);
		FunctionId conditionId = (FunctionId) condition
				.getFirstIsFunctionIdOf().getAlpha();
		assertNotNull(conditionId);
		assertEquals("equals", conditionId.getName());
		IntLiteral arg1 = (IntLiteral) condition.getFirstIsArgumentOf()
				.getAlpha();
		IntLiteral arg2 = (IntLiteral) condition.getFirstIsArgumentOf()
				.getNextIsArgumentOf().getAlpha();
		assertEquals(1, arg1.getIntValue());
		assertEquals(1, arg2.getIntValue());
		IntLiteral trueExpression = (IntLiteral) condExpr
				.getFirstIsTrueExprOf().getAlpha();
		assertEquals(1, trueExpression.getIntValue());
		IntLiteral falseExpression = (IntLiteral) condExpr
				.getFirstIsFalseExprOf().getAlpha();
		assertEquals(2, falseExpression.getIntValue());
		IntLiteral nullExpression = (IntLiteral) condExpr
				.getFirstIsNullExprOf().getAlpha();
		assertEquals(3, nullExpression.getIntValue());
	}

	@Test
	public void testVertexSetExpression() throws Exception {
		Greql2 graph = parseQuery("V{FirstType, SecondType, ^ThirdType}");
		VertexSetExpression vset = graph.getFirstVertexSetExpression();
		assertNotNull(vset);
		assertEquals(3, vset.getDegree(IsTypeRestrOf.class));
		IsTypeRestrOf typeEdge = vset.getFirstIsTypeRestrOf();
		TypeId typeId = (TypeId) typeEdge.getAlpha();
		assertEquals("FirstType", typeId.getName());
		assertEquals(false, typeId.isExcluded());
		typeEdge = typeEdge.getNextIsTypeRestrOf();
		typeId = (TypeId) typeEdge.getAlpha();
		assertEquals("SecondType", typeId.getName());
		assertEquals(false, typeId.isExcluded());
		typeEdge = typeEdge.getNextIsTypeRestrOf();
		typeId = (TypeId) typeEdge.getAlpha();
		assertEquals("ThirdType", typeId.getName());
		assertEquals(true, typeId.isExcluded());
	}

	@Test
	public void testEdgeSetExpression() throws Exception {
		Greql2 graph = parseQuery("E{^FirstType, ^SecondType, ThirdType}");
		EdgeSetExpression vset = graph.getFirstEdgeSetExpression();
		assertNotNull(vset);
		assertEquals(3, vset.getDegree(IsTypeRestrOf.class));
		IsTypeRestrOf typeEdge = vset.getFirstIsTypeRestrOf();
		TypeId typeId = (TypeId) typeEdge.getAlpha();
		assertEquals("FirstType", typeId.getName());
		assertEquals(true, typeId.isExcluded());
		typeEdge = typeEdge.getNextIsTypeRestrOf();
		typeId = (TypeId) typeEdge.getAlpha();
		assertEquals("SecondType", typeId.getName());
		assertEquals(true, typeId.isExcluded());
		typeEdge = typeEdge.getNextIsTypeRestrOf();
		typeId = (TypeId) typeEdge.getAlpha();
		assertEquals("ThirdType", typeId.getName());
		assertEquals(false, typeId.isExcluded());
	}

	@Test
	public void testEdgeSubgraphExpression() throws Exception {
		Greql2 graph = parseQuery("eSubgraph{^FirstType, ^SecondType, ThirdType}");
		EdgeSubgraphExpression vset = graph.getFirstEdgeSubgraphExpression();
		assertNotNull(vset);
		assertEquals(3, vset.getDegree(IsTypeRestrOf.class));
		IsTypeRestrOf typeEdge = vset.getFirstIsTypeRestrOf();
		TypeId typeId = (TypeId) typeEdge.getAlpha();
		assertEquals("FirstType", typeId.getName());
		assertEquals(true, typeId.isExcluded());
		typeEdge = typeEdge.getNextIsTypeRestrOf();
		typeId = (TypeId) typeEdge.getAlpha();
		assertEquals("SecondType", typeId.getName());
		assertEquals(true, typeId.isExcluded());
		typeEdge = typeEdge.getNextIsTypeRestrOf();
		typeId = (TypeId) typeEdge.getAlpha();
		assertEquals("ThirdType", typeId.getName());
		assertEquals(false, typeId.isExcluded());
	}

	@Test
	public void testVertexSubgraphExpression() throws Exception {
		Greql2 graph = parseQuery("vSubgraph{^FirstType, ^SecondType, ThirdType}");
		VertexSubgraphExpression vset = graph
				.getFirstVertexSubgraphExpression();
		assertNotNull(vset);
		assertEquals(3, vset.getDegree(IsTypeRestrOf.class));
		IsTypeRestrOf typeEdge = vset.getFirstIsTypeRestrOf();
		TypeId typeId = (TypeId) typeEdge.getAlpha();
		assertEquals("FirstType", typeId.getName());
		assertEquals(true, typeId.isExcluded());
		typeEdge = typeEdge.getNextIsTypeRestrOf();
		typeId = (TypeId) typeEdge.getAlpha();
		assertEquals("SecondType", typeId.getName());
		assertEquals(true, typeId.isExcluded());
		typeEdge = typeEdge.getNextIsTypeRestrOf();
		typeId = (TypeId) typeEdge.getAlpha();
		assertEquals("ThirdType", typeId.getName());
		assertEquals(false, typeId.isExcluded());
	}

	@Test
	public void testReportAsQuery() throws Exception {
		parseQuery("from v:V report v as \"Vertex\" end");
	}

	@Test
	public void testSimplePathDescription() throws Exception {
		Greql2 graph = parseQuery("using v: v --> ");
		SimplePathDescription pathDescr = graph.getFirstSimplePathDescription();
		for (Vertex v : graph.vertices()) {
			System.out.println("VErtex: " + v);
		}
		assertNotNull(pathDescr);
	}

	@Test
	public void testAlternativePathDescriptionWithTypes() throws Exception {
		Greql2 graph = parseQuery("using v: v -->{AType} | <--{AnotherType}");
		Variable var = graph.getFirstVariable();
		assertNotNull(var);
		assertEquals("v", var.getName());
		ForwardVertexSet vset = graph.getFirstForwardVertexSet();
		assertNotNull(vset);

		AlternativePathDescription apd = graph
				.getFirstAlternativePathDescription();
		assertNotNull(apd);
		IsAlternativePathOf edge = apd
				.getFirstIsAlternativePathOf(EdgeDirection.IN);
		assertNotNull(edge);
		SimplePathDescription spd = (SimplePathDescription) edge.getAlpha();

		IsEdgeRestrOf restrEdge = spd.getFirstIsEdgeRestrOf();
		assertNotNull(restrEdge);
		EdgeRestriction edgeRestriction = (EdgeRestriction) restrEdge
				.getAlpha();
		IsTypeIdOf typeEdge = edgeRestriction.getFirstIsTypeIdOf();
		assertNotNull(typeEdge);
		TypeId type = (TypeId) typeEdge.getAlpha();
		assertEquals("AType", type.getName());
		assertFalse(type.isType());
		assertFalse(type.isExcluded());

		edge = edge.getNextIsAlternativePathOf();
		assertNotNull(edge);
		spd = (SimplePathDescription) edge.getAlpha();
		restrEdge = spd.getFirstIsEdgeRestrOf();
		assertNotNull(restrEdge);
		edgeRestriction = (EdgeRestriction) restrEdge.getAlpha();
		typeEdge = edgeRestriction.getFirstIsTypeIdOf();
		assertNotNull(typeEdge);
		type = (TypeId) typeEdge.getAlpha();
		assertEquals("AnotherType", type.getName());
		assertFalse(type.isType());
		assertFalse(type.isExcluded());
	}

	@Test
	public void testPathDescriptionWithParantheses1() throws Exception {
		Greql2 graph = parseQuery("(--> | <--)");
		AlternativePathDescription apd = (AlternativePathDescription) graph
				.getFirstAlternativePathDescription();
		assertNotNull(apd);
		IsAlternativePathOf edge = apd
				.getFirstIsAlternativePathOf(EdgeDirection.IN);
		assertNotNull(edge);
		assertTrue(edge.getAlpha() instanceof SimplePathDescription);
		edge = edge.getNextIsAlternativePathOf();
		assertNotNull(edge);
		edge.getAlpha();
		assertTrue(edge.getAlpha() instanceof SimplePathDescription);
	}

	@Test
	public void testTypedPathDescription() throws Exception {
		Greql2 graph = parseQuery("using v: v -->{AType} ");
		Variable var = graph.getFirstVariable();
		assertNotNull(var);
		assertEquals("v", var.getName());
		ForwardVertexSet vset = graph.getFirstForwardVertexSet();
		assertNotNull(vset);

		SimplePathDescription simplepd = graph.getFirstSimplePathDescription();
		assertNotNull(simplepd);
		IsEdgeRestrOf restrEdge = simplepd.getFirstIsEdgeRestrOf();
		assertNotNull(restrEdge);
		EdgeRestriction edgeRestriction = (EdgeRestriction) restrEdge
				.getAlpha();
		IsTypeIdOf typeEdge = edgeRestriction.getFirstIsTypeIdOf();
		assertNotNull(typeEdge);
		TypeId type = (TypeId) typeEdge.getAlpha();
		assertEquals("AType", type.getName());
		assertFalse(type.isType());
		assertFalse(type.isExcluded());
	}

	@Test
	public void testAlternativePathDescription() throws Exception {
		Greql2 graph = parseQuery("using v: v --> | <-- ");
		Variable var = graph.getFirstVariable();
		assertNotNull(var);
		assertEquals("v", var.getName());
		ForwardVertexSet vset = graph.getFirstForwardVertexSet();
		assertNotNull(vset);

		AlternativePathDescription apd = graph
				.getFirstAlternativePathDescription();
		assertNotNull(apd);
		IsAlternativePathOf edge = apd
				.getFirstIsAlternativePathOf(EdgeDirection.IN);
		assertNotNull(edge);
		assertTrue(edge.getAlpha() instanceof SimplePathDescription);
		edge = edge.getNextIsAlternativePathOf();
		assertNotNull(edge);
		assertTrue(edge.getAlpha() instanceof SimplePathDescription);
	}

	@Test
	public void testIntermediateVertexPathDescription() throws Exception {
		Greql2 graph = parseQuery("using v: v --> v <-- ");
		ForwardVertexSet vset = graph.getFirstForwardVertexSet();
		assertNotNull(vset);

		IntermediateVertexPathDescription ipd = graph
				.getFirstIntermediateVertexPathDescription();
		assertNotNull(ipd);
		IsSubPathOf edge = ipd.getFirstIsSubPathOf(EdgeDirection.IN);
		assertNotNull(edge);
		assertTrue(edge.getAlpha() instanceof SimplePathDescription);
		edge = edge.getNextIsSubPathOf();
		assertNotNull(edge);
		assertTrue(edge.getAlpha() instanceof SimplePathDescription);
		IsIntermediateVertexOf intEdge = ipd.getFirstIsIntermediateVertexOf();
		assertNotNull(intEdge);
		assertEquals(graph.getFirstVariable(), intEdge.getAlpha());
	}

	@Test
	public void testSequentialPathDescription() throws Exception {
		Greql2 graph = parseQuery("using v: v --> <-- ");
		ForwardVertexSet vset = graph.getFirstForwardVertexSet();
		assertNotNull(vset);

		SequentialPathDescription sepd = graph
				.getFirstSequentialPathDescription();
		assertNotNull(sepd);
		IsSequenceElementOf edge = sepd
				.getFirstIsSequenceElementOf(EdgeDirection.IN);
		assertNotNull(edge);
		assertTrue(edge.getAlpha() instanceof SimplePathDescription);
		edge = edge.getNextIsSequenceElementOf();
		assertNotNull(edge);
		assertTrue(edge.getAlpha() instanceof SimplePathDescription);
	}

	@Test
	public void testStartRestrictedPathDescriptionWithExpression()
			throws Exception {
		Greql2 graph = parseQuery("using v: v {v.a=3} & --> ");

		SimplePathDescription srpd = graph.getFirstSimplePathDescription();
		assertNotNull(srpd);
		IsStartRestrOf restrEdge = srpd
				.getFirstIsStartRestrOf(EdgeDirection.IN);
		assertNotNull(restrEdge);
		FunctionApplication restr = (FunctionApplication) restrEdge.getAlpha();
		FunctionId funId = (FunctionId) restr.getFirstIsFunctionIdOf()
				.getAlpha();
		assertEquals("equals", funId.getName());

		ForwardVertexSet vset = graph.getFirstForwardVertexSet();
		assertNotNull(vset);
	}

	@Test
	public void testStartRestrictedPathDescriptionWithType() throws Exception {
		Greql2 graph = parseQuery("using v: v {MyType} & --> ");

		SimplePathDescription srpd = graph.getFirstSimplePathDescription();
		assertNotNull(srpd);
		IsStartRestrOf restrEdge = srpd
				.getFirstIsStartRestrOf(EdgeDirection.IN);
		assertNotNull(restrEdge);
		TypeId typeId = (TypeId) restrEdge.getAlpha();
		assertEquals("MyType", typeId.getName());
		assertFalse(typeId.isExcluded());
		assertFalse(typeId.isType());

		ForwardVertexSet vset = graph.getFirstForwardVertexSet();
		assertNotNull(vset);
	}

	@Test
	public void testGoalRestrictedPathDescription() throws Exception {
		Greql2 graph = parseQuery("using v: v --> & {false} ");
		ForwardVertexSet vset = graph.getFirstForwardVertexSet();
		assertNotNull(vset);

		SimplePathDescription gpd = graph.getFirstSimplePathDescription();
		assertNotNull(gpd);
		IsGoalRestrOf restrEdge = gpd.getFirstIsGoalRestrOf(EdgeDirection.IN);
		assertNotNull(restrEdge);
	}

	@Test
	public void testIteratedPathDescription() throws Exception {
		Greql2 graph = parseQuery("using v: v -->* ");
		ForwardVertexSet vset = graph.getFirstForwardVertexSet();
		assertNotNull(vset);

		IteratedPathDescription ipd = graph.getFirstIteratedPathDescription();
		assertNotNull(ipd);

	}

	@Test
	public void testPathExistence() throws Exception {
		Greql2 graph = parseQuery("using v,w: v --> w ");
		PathExistence pathExistence = graph.getFirstPathExistence();
		assertNotNull(pathExistence);
		IsStartExprOf startEdge = pathExistence.getFirstIsStartExprOf();
		assertNotNull(startEdge);
		Variable startVar = (Variable) startEdge.getAlpha();
		assertEquals("v", startVar.getName());

		IsTargetExprOf endEdge = pathExistence.getFirstIsTargetExprOf();
		assertNotNull(endEdge);
		Variable endVar = (Variable) endEdge.getAlpha();
		assertEquals("w", endVar.getName());

		IsPathOf pathEdge = pathExistence.getFirstIsPathOf();
		assertNotNull(pathEdge);
		assertTrue(pathEdge.getAlpha() instanceof SimplePathDescription);
	}

	@Test
	public void testEdgePathDescription() throws Exception {
		Greql2 graph = parseQuery("using e,v : v --e-> ");
		ForwardVertexSet vset = graph.getFirstForwardVertexSet();
		assertNotNull(vset);

		EdgePathDescription ipd = graph.getFirstEdgePathDescription();
		assertNotNull(ipd);
		IsEdgeExprOf edge = ipd.getFirstIsEdgeExprOf();
		assertNotNull(edge);
	}

	@Test
	public void testErrorInTypeExpression() throws Exception {
		String query = "from v:V{Greql2Expression, ^TypeExpression, ^Quantifier} report v end";
		parseQuery(query);
	}

	@Test(timeout = 5000)
	public void testParsingSpeed2() throws Exception {
		parseQuery("3 + (((3) + (((3)) - 3) * (((((((((9 - 6))) + 3 - 6)) + 3))))) - 3)");
	}

	@Test(timeout = 5000)
	public void testParsingSpeed3() throws Exception {
		parseQuery("((((((((((((9))))))))))))");
	}

	@Test(timeout = 5000)
	public void testParsingSpeed4() throws Exception {
		parseQuery("(((((((((((((((((((((((((((((((((((((((9))))))))))))))))))))))))))))))))))))))))");
	}

	@Test
	public void testStringWithoutEscapes() {
		Greql2 graph = parseQuery("\"my simple string\"");
		assertNotNull(graph);
		StringLiteral lit = graph.getFirstStringLiteral();
		assertNotNull(lit);
		assertEquals("my simple string", lit.getStringValue());
	}

	@Test
	public void testStringWithEscape1() {
		String queryString = "\"my simple \\\"string\"";
		System.out.println("QueryString: " + queryString);
		Greql2 graph = parseQuery(queryString);
		assertNotNull(graph);
		StringLiteral lit = graph.getFirstStringLiteral();
		assertNotNull(lit);
		assertEquals("my simple \"string", lit.getStringValue());
	}

	@Test
	public void testStringWithEscape2() {
		String queryString = "\"my simple \nstring\"";
		System.out.println("QueryString: " + queryString);
		Greql2 graph = parseQuery(queryString);
		assertNotNull(graph);
		StringLiteral lit = graph.getFirstStringLiteral();
		assertNotNull(lit);
		assertEquals("my simple \nstring", lit.getStringValue());
	}

	@Test
	public void testParsingError() {
		try {
			parseQuery("from v:V report v --> --< end");
			fail("Expected ParsingException at offset 24");
		} catch (ParsingException ex) {
			assertEquals(24, ex.getOffset());
		}
	}

	@Test
	public void testParsingError2() {
		try {
			parseQuery("from v:X report v --> --> end");
			fail("Expected ParsingException at offset 7");
		} catch (UndefinedVariableException ex) {
			System.out.println("Exception offset: " + ex.getOffset());
			assertEquals(7, ex.getOffset());
		}
	}
}
