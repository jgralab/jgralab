/*
 * JGraLab - The Java Graph Laboratory
 *
 * Copyright (C) 2006-2014 Institute for Software Technology
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
package de.uni_koblenz.jgralabtest.schema;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayInputStream;
import java.util.Set;
import java.util.SortedSet;

import org.junit.Before;
import org.junit.Test;

import de.uni_koblenz.jgralab.AttributedElement;
import de.uni_koblenz.jgralab.GraphIO;
import de.uni_koblenz.jgralab.ImplementationType;
import de.uni_koblenz.jgralab.exception.GraphIOException;
import de.uni_koblenz.jgralab.graphvalidator.GraphValidator;
import de.uni_koblenz.jgralab.graphvalidator.MultiplicityConstraintViolation;
import de.uni_koblenz.jgralab.schema.EdgeClass;
import de.uni_koblenz.jgralab.schema.Schema;
import de.uni_koblenz.jgralab.schema.codegenerator.CodeGeneratorConfiguration;
import de.uni_koblenz.jgralabtest.schemas.vertextest.A;
import de.uni_koblenz.jgralabtest.schemas.vertextest.B;
import de.uni_koblenz.jgralabtest.schemas.vertextest.VertexTestGraph;
import de.uni_koblenz.jgralabtest.schemas.vertextest.VertexTestSchema;

public class MultiplicityTest {
	private VertexTestGraph graph;
	private GraphValidator validator;

	/**
	 * Creates a new graph for each test.
	 */
	@Before
	public void setUp() {
		graph = VertexTestSchema.instance().createVertexTestGraph(
				ImplementationType.STANDARD);
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
		ByteArrayInputStream input = new ByteArrayInputStream(
				schemaString.getBytes());
		Schema s = null;
		s = GraphIO.loadSchemaFromStream(input);
		try {
			s.compile(CodeGeneratorConfiguration.NORMAL);
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
		Set<AttributedElement<?, ?>> offendingElements = violations.first()
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
		Set<AttributedElement<?, ?>> offendingElements = violations.first()
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
		Set<AttributedElement<?, ?>> offendingElements = violations.first()
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
		Set<AttributedElement<?, ?>> offendingElements = violations.first()
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
		Set<AttributedElement<?, ?>> offendingElements = violations.first()
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
		Set<AttributedElement<?, ?>> offendingElements = violations.first()
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
	public void multiplicityTest16() throws Exception {
		compileSchema("TGraph 2;Schema de.uni_koblenz.jgralabtest.TestSchema;"
				+ "GraphClass TestGraph;" + "VertexClass VC1;"
				+ "VertexClass VC2;"
				+ "EdgeClass EC1 from VC2 (0,*) to VC1 (2,6);"
				+ "EdgeClass EC2:EC1 from VC2 (0,*) to VC1 (3,4);");
	}

	@Test
	public void multiplicityTest18() throws Exception {
		compileSchema("TGraph 2;Schema de.uni_koblenz.jgralabtest.TestSchema;"
				+ "GraphClass TestGraph;" + "VertexClass VC1;"
				+ "VertexClass VC2;"
				+ "EdgeClass EC1 from VC2 (0,*) to VC1 (2,6);"
				+ "EdgeClass EC2:EC1 from VC2 (0,*) to VC1 (2,6);");
	}

	@Test
	public void multiplicityTest22() throws Exception {
		compileSchema("TGraph 2;Schema de.uni_koblenz.jgralabtest.TestSchema;"
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
	@Test(expected = GraphIOException.class)
	// TODO Replace with the expected exception
	public void multiplicityTest17() throws Exception {
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
	@Test(expected = GraphIOException.class)
	// TODO Replace with the expected exception
	public void multiplicityTest19() throws Exception {
		compileSchema("Schema de.uni_koblenz.jgralabtest.TestSchema;"
				+ "GraphClass TestGraph;" + "VertexClass VC1;"
				+ "VertexClass VC2;"
				+ "EdgeClass EC1 from VC2 (0,*) to VC1 (2,6);"
				+ "EdgeClass EC2:EC1 from VC2 (0,*) to VC1 (3,7);");
	}

	/**
	 * Multiplicity of the child edge is smaller than the smallest possible
	 * multiplicity of the parent edge, this is allowed
	 */
	public void multiplicityTest20() throws Exception {
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
	public void multiplicityTest21() throws Exception {
		compileSchema("Schema de.uni_koblenz.jgralabtest.TestSchema;"
				+ "GraphClass TestGraph;" + "VertexClass VC1;"
				+ "VertexClass VC2;" + "VertexClass VC3;"
				+ "EdgeClass EC1 from VC2 (0,*) to VC1 (2,6);"
				+ "EdgeClass EC2:EC1 from VC2 (0,*) to VC3 (3,4);");
	}
}
