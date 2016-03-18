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
package de.uni_koblenz.jgralabtest.greql;

import java.io.File;
import java.io.IOException;

import javax.xml.stream.XMLStreamException;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.pcollections.PVector;

import de.uni_koblenz.jgralab.Graph;
import de.uni_koblenz.jgralab.GraphIO;
import de.uni_koblenz.jgralab.Vertex;
import de.uni_koblenz.jgralab.exception.GraphIOException;
import de.uni_koblenz.jgralab.greql.GreqlQuery;
import de.uni_koblenz.jgralab.greql.evaluator.fa.State;
import de.uni_koblenz.jgralab.greql.exception.SerialisingException;
import de.uni_koblenz.jgralab.greql.serialising.HTMLOutputWriter;
import de.uni_koblenz.jgralab.greql.serialising.XMLOutputWriter;
import de.uni_koblenz.jgralab.greql.types.Path;
import de.uni_koblenz.jgralab.greql.types.Undefined;
import de.uni_koblenz.jgralabtest.schemas.greqltestschema.localities.CountyTags;

public class StoreValuesTest {

	static Graph graph = null;
	static String testdir = "testit/testdata/";

	@BeforeClass
	public static void setUp() {
		try {
			graph = createTestGraph();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@AfterClass
	public static void tearDown() {

	}

	/**
	 * Loads the test graph
	 *
	 * @return the loaded graph
	 * @throws GraphIOException
	 */
	public static Graph createTestGraph() throws GraphIOException {
		Graph testGraph = GraphIO.loadGraphFromFile("testit/testgraphs/greqltestgraph.tg", null);
		return testGraph;
	}

	/**
	 * Integer
	 */
	@Test
	public void testOutputOfInteger() {
		Integer i = 42;
		generateHTMLandXMLoutput(i, "outputInteger");
	}

	/**
	 * Long
	 */
	@Test
	public void testOutputOfLong() {
		Long l = 42l;
		generateHTMLandXMLoutput(l, "outputLong");
	}

	/**
	 * Double
	 */
	@Test
	public void testOutputOfDouble() {
		Double d = 42.0;
		generateHTMLandXMLoutput(d, "outputDouble");
	}

	/**
	 * String
	 */
	@Test
	public void testOutputOfString() {
		String s = "Hallo World!";
		generateHTMLandXMLoutput(s, "outputString");
	}

	/**
	 * Boolean
	 */
	@Test
	public void testOutputOfBoolean() {
		Boolean b = true;
		generateHTMLandXMLoutput(b, "outputBoolean");
	}

	/**
	 * Record
	 */
	@Test
	public void testOutputOfRecord() {
		String qu = "rec(a:5,b:\"Yes\")";
		evaluateQueryAndSaveResult(qu, "outputRecord");
	}

	/**
	 * Undefined
	 */
	@Test
	public void testOutputUndefined() {
		Undefined n = Undefined.UNDEFINED;
		generateHTMLandXMLoutput(n, "outputUndefined");
	}

	/**
	 * Graph
	 */
	@Test
	public void testOutputOfGraph() {
		generateHTMLandXMLoutput(graph, "outputGraph");
	}

	/**
	 * Vertex
	 */
	@Test
	public void testOutputOfVertex() {
		String qu = "from v : V with id(v)  = 1 report v end";
		evaluateQueryAndSaveResult(qu, "outputVertex");
	}

	/**
	 * Edge
	 */
	@Test
	public void testOutputOfEdge() {
		String qu = "from e : E with id(e)  = 1 report e end";
		evaluateQueryAndSaveResult(qu, "outputEdge");
	}

	/**
	 * Enum
	 */
	@Test
	public void testOutputOfEnum() {
		generateHTMLandXMLoutput(CountyTags.AREA, "outputEnum");
	}

	/**
	 * PVector of Vertices
	 */
	@Test
	public void testOutputOfListOfVertices() {
		String qu = "from airport: V{junctions.Airport}, x: V with x "
				+ "(-->{localities.ContainsLocality} | -->{connections.AirRoute}) airport " + "report x end";
		evaluateQueryAndSaveResult(qu, "outputPVectorOfVertices");
	}

	/**
	 * PSet of Vertices
	 */
	@Test
	public void testOutputOfSetOfVertices() {
		String qu = "from airport: V{junctions.Airport}, x: V with x "
				+ "(-->{localities.ContainsLocality} | -->{connections.AirRoute}) airport " + "reportSet x end";
		evaluateQueryAndSaveResult(qu, "outputPSetOfVertices");
	}

	/**
	 * PVector of Tuples of Vertices
	 */
	@Test
	public void testOutputOfListOfTupleOfVertices() {
		String qu = "from a,b:V with connected report a,b end where connected := a-->b";
		evaluateQueryAndSaveResult(qu, "outputPVectorOfTuplesOfVertices");
	}

	/**
	 * PMap of Integer to Integer
	 */
	@Test
	public void testMapOfIntegerPairs() {
		String qu = "from i : list (1..10) reportMap i -> i*i end";
		evaluateQueryAndSaveResult(qu, "outputPMapOfIntegerToInteger");
	}

	/**
	 * Table
	 */
	@Test
	public void testTableOfIntegers() {
		String queryString = "from i : list (1..10), j : list (1..5) report i-1, j, i*j end";
		evaluateQueryAndSaveResult(queryString, "outputTableOfIntegers");
	}

	/**
	 * Tuple of Vertices
	 */
	@Test
	public void testOutputOfTupleOfVertices() {
		String qu = "from a,b:V with connected report a,b end where connected := a-->b";

		@SuppressWarnings("unchecked")
		PVector<Vertex> result = (PVector<Vertex>) GreqlQuery.createQuery(qu).evaluate(graph);

		generateHTMLandXMLoutput(result.get(0), "outputTupleOfVertices", graph, true);
	}

	/**
	 * Path
	 */
	@Test
	public void testOutputOfPath() {
		Path p = Path.start(graph.getVertex(1));
		p = p.append(graph.getEdge(135));
		p = p.append(graph.getEdge(136));
		generateHTMLandXMLoutput(p, "outputPath");
	}

	/**
	 * Slice
	 */
	// HTMLOutputWriter can not handle slices
	@Test(expected = SerialisingException.class)
	public void testOutputOfSlice() {
		String qu = "from w: V{localities.Town} report slice(w, <--) end";
		Object result = GreqlQuery.createQuery(qu).evaluate(graph);

		try {
			HTMLOutputWriter writer = new HTMLOutputWriter(graph);
			writer.setCreateElementLinks(true);
			writer.writeValue(result, new File(testdir + "outputSlice" + ".html"));
		} catch (IOException e) {
			e.printStackTrace();
			assert false;
		}
	}

	/**
	 * Slice
	 */

	// HTMLOutputWriter can not handle slices
	@Test(expected = SerialisingException.class)
	public void testOutputOfSliceException() {
		String qu = "from w: V{localities.Town} report slice(w, <--) end";
		Object result = GreqlQuery.createQuery(qu).evaluate(graph);

		try {
			XMLOutputWriter writer = new XMLOutputWriter(graph);
			writer.writeValue(result, new File(testdir + "outputSlice" + ".xml"));
		} catch (XMLStreamException e) {
			e.printStackTrace();
		}

	}

	/**
	 * AttributedElementClass
	 */
	@Test
	public void testOutputOfAttributedElementClas() {
		generateHTMLandXMLoutput(graph.getFirstVertex().getAttributedElementClass(), "outputAttributedElementClass");
	}

	/**
	 * PMap from String to Integers
	 */
	@Test
	public void testOutputOfMapFromStringToInteger() {
		String qu = "from v : V{localities.Locality} with v.inhabitants > 100 reportMap v.name -> v.inhabitants end";
		evaluateQueryAndSaveResult(qu, "outputMapFromStringToInteger");
	}

	/**
	 * PMap from Vertex to PVector of Vertices
	 */
	@Test
	public void testOutputOfMapFromVertexToListOfVertices() {
		String qu = "from j : V{junctions.Junction}  reportMap j -> (j <--{connections.Connection}) end";
		evaluateQueryAndSaveResult(qu, "outputMapFromVertexToListOfVertices");
	}

	/**
	 * PVector of PSet of Vertices
	 */
	@Test
	public void testOutputOfListOfSetsOfVertices() {
		String qu = "from v : V{junctions.Junction} with count(v-->{connections.AirRoute}) > 0 reportList v-->{connections.AirRoute} end";
		evaluateQueryAndSaveResult(qu, "outputListOfSetsOfVertices");
	}

	/**
	 * PMap from Vertex to (PMap from Enumeration to Double)
	 */
	@Test
	public void testOutputOfMapFromVertexToMapFromEnumToDouble() {
		String qu = "from v : V{localities.County} reportMap v -> v.tags end";
		evaluateQueryAndSaveResult(qu, "outputMapFromVertexToMapFromEnumToDouble");
	}

	/**
	 * State
	 */
	@Test(expected = SerialisingException.class)
	public void testOutputOfState() {
		State state = new State();
		try {
			HTMLOutputWriter writer = new HTMLOutputWriter(graph);
			writer.setCreateElementLinks(true);
			writer.writeValue(state, new File(testdir + "outputException" + ".html"));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * State
	 */
	@Test(expected = SerialisingException.class)
	public void testOutputOfState2() {
		State state = new State();
		try {
			XMLOutputWriter writer = new XMLOutputWriter(graph);
			writer.writeValue(state, new File(testdir + "outputException" + ".xml"));
		} catch (XMLStreamException e) {
			e.printStackTrace();
		}
	}

	// ----------------------------------------------------------------------

	public void evaluateQueryAndSaveResult(String query, String filename) {

		Object result = GreqlQuery.createQuery(query).evaluate(graph);

		generateHTMLandXMLoutput(result, filename, graph, true);
	}

	public void generateHTMLandXMLoutput(Object result, String filename) {
		generateHTMLandXMLoutput(result, filename, null, false);
	}

	public void generateHTMLandXMLoutput(Object result, String filename, Graph graph, boolean elemlinks) {
		try {
			HTMLOutputWriter htmlWriter = new HTMLOutputWriter(graph);
			htmlWriter.setCreateElementLinks(elemlinks);
			htmlWriter.writeValue(result, new File(testdir + filename + ".html"));

			XMLOutputWriter xmlWriter = new XMLOutputWriter(graph);
			xmlWriter.writeValue(result, new File(testdir + filename + ".xml"));
		} catch (XMLStreamException e) {
			e.printStackTrace();
			assert false;
		} catch (IOException e) {
			e.printStackTrace();
			assert false;
		}
	}
}
