package de.uni_koblenz.jgralabtest;

import java.io.ByteArrayInputStream;
import java.util.Set;
import java.util.SortedSet;

import org.junit.Before;
import org.junit.Test;

import de.uni_koblenz.jgralab.AttributedElement;
import de.uni_koblenz.jgralab.GraphIO;
import de.uni_koblenz.jgralab.GraphIOException;
import de.uni_koblenz.jgralab.graphvalidator.GraphValidator;
import de.uni_koblenz.jgralab.graphvalidator.MultiplicityConstraintViolation;
import de.uni_koblenz.jgralab.schema.EdgeClass;
import de.uni_koblenz.jgralab.schema.Schema;
import de.uni_koblenz.jgralab.GraphException;
import de.uni_koblenz.jgralabtest.schemas.vertextest.A;
import de.uni_koblenz.jgralabtest.schemas.vertextest.B;
import de.uni_koblenz.jgralabtest.schemas.vertextest.VertexTestGraph;
import de.uni_koblenz.jgralabtest.schemas.vertextest.VertexTestSchema;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class MultiplicityTest {
	private VertexTestGraph graph;
	private GraphValidator validator;

	/**
	 * Creates a new graph for each test.
	 */
	@Before
	public void setUp() {
		graph = VertexTestSchema.instance().createVertexTestGraph();
		validator = new GraphValidator(graph);
	}

	/**
	 * Compiles the schema defined in schemaString.
	 * 
	 * @param schemaString
	 * @return the schema
	 * @throws GraphIOException
	 */
	private Schema compileSchema(String schemaString) throws GraphIOException {
		ByteArrayInputStream input = new ByteArrayInputStream(schemaString
				.getBytes());
		Schema s = null;
		s = GraphIO.loadSchemaFromStream(input);
		try {
			s.compile();
		} catch (Exception e) {
			throw new GraphIOException("", e);
		}
		return s;
	}

	/*
	 * 1. MultiplicityConstraints fulfilled.
	 */

	/*
	 * 1.1. MultiplicityConstraints fulfilled of one EdgeClass.
	 */

	@Test
	public void multiplicityTest0() {
		A v1 = graph.createA();
		B v2 = graph.createB();
		graph.createE(v1, v2);
		SortedSet<MultiplicityConstraintViolation> violations = validator
				.validateMultiplicities((EdgeClass) graph.getSchema()
						.getAttributedElementClass("E"));
		assertTrue(violations.isEmpty());
	}

	@Test
	public void multiplicityTest1() {
		A v1 = graph.createA();
		B v2 = graph.createB();
		graph.createE(v1, v2);
		graph.createE(v1, v2);
		graph.createE(v1, v2);
		graph.createE(v1, v2);
		graph.createE(v1, v2);
		graph.createE(v1, v2);
		graph.createE(v1, v2);
		graph.createE(v1, v2);
		graph.createE(v1, v2);
		graph.createE(v1, v2);
		SortedSet<MultiplicityConstraintViolation> violations = validator
				.validateMultiplicities((EdgeClass) graph.getSchema()
						.getAttributedElementClass("E"));
		assertTrue(violations.isEmpty());
	}

	@Test
	public void multiplicityTest8() {
		graph.createA();
		graph.createB();
		SortedSet<MultiplicityConstraintViolation> violations = validator
				.validateMultiplicities((EdgeClass) graph.getSchema()
						.getAttributedElementClass("E"));
		assertTrue(violations.isEmpty());
	}

	@Test
	public void multiplicityTest2() {
		A v1 = graph.createA();
		B v2 = graph.createB();
		graph.createH(v1, v2);
		SortedSet<MultiplicityConstraintViolation> violations = validator
				.validateMultiplicities((EdgeClass) graph.getSchema()
						.getAttributedElementClass("H"));
		assertTrue(violations.isEmpty());
	}

	@Test
	public void multiplicityTest3() {
		A v1 = graph.createA();
		B v2 = graph.createB();
		graph.createH(v1, v2);
		graph.createH(v1, v2);
		SortedSet<MultiplicityConstraintViolation> violations = validator
				.validateMultiplicities((EdgeClass) graph.getSchema()
						.getAttributedElementClass("H"));
		assertTrue(violations.isEmpty());
	}

	@Test
	public void multiplicityTest4() {
		A v1 = graph.createA();
		B v2 = graph.createB();
		graph.createH(v1, v2);
		graph.createH(v1, v2);
		graph.createH(v1, v2);
		SortedSet<MultiplicityConstraintViolation> violations = validator
				.validateMultiplicities((EdgeClass) graph.getSchema()
						.getAttributedElementClass("H"));
		assertTrue(violations.isEmpty());
	}

	@Test
	public void multiplicityTest5() {
		A v1 = graph.createA();
		B v2 = graph.createB();
		graph.createH(v1, v2);
		graph.createH(v1, v2);
		graph.createH(v1, v2);
		graph.createH(v1, v2);
		SortedSet<MultiplicityConstraintViolation> violations = validator
				.validateMultiplicities((EdgeClass) graph.getSchema()
						.getAttributedElementClass("H"));
		assertTrue(violations.isEmpty());
	}

	@Test
	public void multiplicityTest11() {
		A v1 = graph.createA();
		B v2 = graph.createB();
		graph.createK(v1, v2);
		graph.createK(v1, v2);
		SortedSet<MultiplicityConstraintViolation> violations = validator
				.validateMultiplicities((EdgeClass) graph.getSchema()
						.getAttributedElementClass("K"));
		assertTrue(violations.isEmpty());
	}

	@Test
	public void multiplicityTest12() {
		A v1 = graph.createA();
		B v2 = graph.createB();
		graph.createK(v1, v2);
		graph.createK(v1, v2);
		graph.createK(v1, v2);
		SortedSet<MultiplicityConstraintViolation> violations = validator
				.validateMultiplicities((EdgeClass) graph.getSchema()
						.getAttributedElementClass("K"));
		assertTrue(violations.isEmpty());
	}

	/*
	 * 1.2. MultiplicityConstraints fulfilled of several EdgeClasses.
	 */

	@Test
	public void multiplicityTest14() {
		A v1 = graph.createA();
		B v2 = graph.createB();
		graph.createK(v1, v2);
		graph.createK(v1, v2);
		graph.createK(v1, v2);
		graph.createH(v1, v2);
		SortedSet<MultiplicityConstraintViolation> violations = validator
				.validateMultiplicities((EdgeClass) graph.getSchema()
						.getAttributedElementClass("K"));
		assertTrue(violations.isEmpty());
		violations = validator.validateMultiplicities((EdgeClass) graph
				.getSchema().getAttributedElementClass("H"));
		assertTrue(violations.isEmpty());
	}

	/*
	 * 2. MultiplicityConstraints broken.
	 */

	/*
	 * 2.1. MultiplicityConstraints broken of one EdgeClass.
	 */

	/**
	 * No H-edges.
	 */
	@Test
	public void multiplicityTest6() {
		A v1 = graph.createA();
		graph.createB();
		SortedSet<MultiplicityConstraintViolation> violations = validator
				.validateMultiplicities((EdgeClass) graph.getSchema()
						.getAttributedElementClass("H"));
		assertFalse(violations.isEmpty());
		Set<AttributedElement> offendingElements = violations.first()
				.getOffendingElements();
		assertEquals(1, offendingElements.size());
		assertTrue(offendingElements.contains(v1));
		assertEquals(graph.getSchema().getAttributedElementClass("H"),
				violations.first().getAttributedElementClass());
	}

	/**
	 * Too many H-edges.
	 */
	@Test
	public void multiplicityTest7() {
		A v1 = graph.createA();
		B v2 = graph.createB();
		graph.createH(v1, v2);
		graph.createH(v1, v2);
		graph.createH(v1, v2);
		graph.createH(v1, v2);
		graph.createH(v1, v2);
		SortedSet<MultiplicityConstraintViolation> violations = validator
				.validateMultiplicities((EdgeClass) graph.getSchema()
						.getAttributedElementClass("H"));
		assertFalse(violations.isEmpty());
		Set<AttributedElement> offendingElements = violations.first()
				.getOffendingElements();
		assertEquals(1, offendingElements.size());
		assertTrue(offendingElements.contains(v1));
		assertEquals(graph.getSchema().getAttributedElementClass("H"),
				violations.first().getAttributedElementClass());
	}

	/**
	 * No K-edges.
	 */
	@Test
	public void multiplicityTest9() {
		A v1 = graph.createA();
		graph.createB();
		SortedSet<MultiplicityConstraintViolation> violations = validator
				.validateMultiplicities((EdgeClass) graph.getSchema()
						.getAttributedElementClass("K"));
		assertFalse(violations.isEmpty());
		Set<AttributedElement> offendingElements = violations.first()
				.getOffendingElements();
		assertEquals(1, offendingElements.size());
		assertTrue(offendingElements.contains(v1));
		assertEquals(graph.getSchema().getAttributedElementClass("K"),
				violations.first().getAttributedElementClass());
	}

	/**
	 * Too less K-edges.
	 */
	@Test
	public void multiplicityTest10() {
		A v1 = graph.createA();
		B v2 = graph.createB();
		graph.createK(v1, v2);
		SortedSet<MultiplicityConstraintViolation> violations = validator
				.validateMultiplicities((EdgeClass) graph.getSchema()
						.getAttributedElementClass("K"));
		assertFalse(violations.isEmpty());
		Set<AttributedElement> offendingElements = violations.first()
				.getOffendingElements();
		assertEquals(1, offendingElements.size());
		assertTrue(offendingElements.contains(v1));
		assertEquals(graph.getSchema().getAttributedElementClass("K"),
				violations.first().getAttributedElementClass());
	}

	/**
	 * Too many K-edges.
	 */
	@Test
	public void multiplicityTest13() {
		A v1 = graph.createA();
		B v2 = graph.createB();
		graph.createK(v1, v2);
		graph.createK(v1, v2);
		graph.createK(v1, v2);
		graph.createK(v1, v2);
		SortedSet<MultiplicityConstraintViolation> violations = validator
				.validateMultiplicities((EdgeClass) graph.getSchema()
						.getAttributedElementClass("K"));
		assertFalse(violations.isEmpty());
		Set<AttributedElement> offendingElements = violations.first()
				.getOffendingElements();
		assertEquals(1, offendingElements.size());
		assertTrue(offendingElements.contains(v1));
		assertEquals(graph.getSchema().getAttributedElementClass("K"),
				violations.first().getAttributedElementClass());
		violations = validator.validateMultiplicities((EdgeClass) graph
				.getSchema().getAttributedElementClass("H"));
		assertTrue(violations.isEmpty());
	}

	/*
	 * 2.2. MultiplicityConstraints broken of several EdgeClasses.
	 */

	/**
	 * The number of K-edges is correct, but there are too many H-edges.
	 */
	@Test
	public void multiplicityTest15() {
		A v1 = graph.createA();
		B v2 = graph.createB();
		graph.createH(v1, v2);
		graph.createK(v1, v2);
		graph.createK(v1, v2);
		graph.createK(v1, v2);
		graph.createH(v1, v2);
		SortedSet<MultiplicityConstraintViolation> violations = validator
				.validateMultiplicities((EdgeClass) graph.getSchema()
						.getAttributedElementClass("K"));
		assertTrue(violations.isEmpty());
		violations = validator.validateMultiplicities((EdgeClass) graph
				.getSchema().getAttributedElementClass("H"));
		assertFalse(violations.isEmpty());
		Set<AttributedElement> offendingElements = violations.first()
				.getOffendingElements();
		assertEquals(1, offendingElements.size());
		assertTrue(offendingElements.contains(v1));
		assertEquals(graph.getSchema().getAttributedElementClass("H"),
				violations.first().getAttributedElementClass());
	}

	/*
	 * 3. Defining MultiplicityConstraints.
	 */

	/*
	 * 3.1 Legal MultiplicityConstraints.
	 */

	@Test
	public void multiplicityTest16() throws GraphIOException {
			compileSchema("Schema de.uni_koblenz.jgralabtest.TestSchema;"
					+ "GraphClass TestGraph;" + "VertexClass VC1;"
					+ "VertexClass VC2;"
					+ "EdgeClass EC1 from VC2 (0,*) to VC1 (2,6);"
					+ "EdgeClass EC2:EC1 from VC2 (0,*) to VC1 (3,4);");
	}

	@Test
	public void multiplicityTest18() throws GraphIOException {
			compileSchema("Schema de.uni_koblenz.jgralabtest.TestSchema;"
					+ "GraphClass TestGraph;" + "VertexClass VC1;"
					+ "VertexClass VC2;"
					+ "EdgeClass EC1 from VC2 (0,*) to VC1 (2,6);"
					+ "EdgeClass EC2:EC1 from VC2 (0,*) to VC1 (2,6);");
	}

	@Test
	public void multiplicityTest22() throws GraphIOException {
			compileSchema("Schema de.uni_koblenz.jgralabtest.TestSchema;"
					+ "GraphClass TestGraph;" + "VertexClass VC1;"
					+ "VertexClass VC2;" + "VertexClass VC3:VC1;"
					+ "EdgeClass EC1 from VC2 (0,*) to VC1 (2,6);"
					+ "EdgeClass EC2:EC1 from VC2 (0,*) to VC3 (3,4);");
	}

	/*
	 * 3.2 Illegal MultiplicityConstraints must be rejected during compilation.
	 */

	/**
	 * Multiplicity of the child edge is smaller and greater than the smallest
	 * and greatest possible multiplicity of the parent edge.
	 */
	@Test(expected = GraphException.class)
	// TODO Replace with the expected exception
	public void multiplicityTest17() throws GraphIOException {
		compileSchema("Schema de.uni_koblenz.jgralabtest.TestSchema;"
				+ "GraphClass TestGraph;" + "VertexClass VC1;"
				+ "VertexClass VC2;"
				+ "EdgeClass EC1 from VC2 (0,*) to VC1 (2,6);"
				+ "EdgeClass EC2:EC1 from VC2 (0,*) to VC1 (1,7);");
	}

	/**
	 * Multiplicity of the child edge is greater than the greatest possible
	 * multiplicity of the parent edge.
	 */
	@Test(expected = GraphException.class)
	// TODO Replace with the expected exception
	public void multiplicityTest19() throws GraphIOException {
		compileSchema("Schema de.uni_koblenz.jgralabtest.TestSchema;"
				+ "GraphClass TestGraph;" + "VertexClass VC1;"
				+ "VertexClass VC2;"
				+ "EdgeClass EC1 from VC2 (0,*) to VC1 (2,6);"
				+ "EdgeClass EC2:EC1 from VC2 (0,*) to VC1 (3,7);");
	}

	/**
	 * Multiplicity of the child edge is smaller than the smallest possible
	 * multiplicity of the parent edge.
	 */
	@Test(expected = GraphException.class)
	// TODO Replace with the expected exception
	public void multiplicityTest20() throws GraphIOException {
		compileSchema("Schema de.uni_koblenz.jgralabtest.TestSchema;"
				+ "GraphClass TestGraph;" + "VertexClass VC1;"
				+ "VertexClass VC2;"
				+ "EdgeClass EC1 from VC2 (0,*) to VC1 (2,6);"
				+ "EdgeClass EC2:EC1 from VC2 (0,*) to VC1 (1,4);");
	}

	/**
	 * EC2 has a different toVertexType than its supertype.
	 */
	@Test(expected = GraphIOException.class)
	public void multiplicityTest21() throws GraphIOException {
		compileSchema("Schema de.uni_koblenz.jgralabtest.TestSchema;"
				+ "GraphClass TestGraph;" + "VertexClass VC1;"
				+ "VertexClass VC2;" + "VertexClass VC3;"
				+ "EdgeClass EC1 from VC2 (0,*) to VC1 (2,6);"
				+ "EdgeClass EC2:EC1 from VC2 (0,*) to VC3 (3,4);");
	}
}
