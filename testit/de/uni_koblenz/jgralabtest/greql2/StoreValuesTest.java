package de.uni_koblenz.jgralabtest.greql2;

import java.io.File;
import java.io.IOException;

import javax.xml.stream.XMLStreamException;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.pcollections.PVector;

import de.uni_koblenz.jgralab.Graph;
import de.uni_koblenz.jgralab.GraphIO;
import de.uni_koblenz.jgralab.GraphIOException;
import de.uni_koblenz.jgralab.Vertex;
import de.uni_koblenz.jgralab.greql2.evaluator.GreqlEvaluator;
import de.uni_koblenz.jgralab.greql2.evaluator.fa.State;
import de.uni_koblenz.jgralab.greql2.exception.SerialisingException;
import de.uni_koblenz.jgralab.greql2.serialising.HTMLOutputWriter;
import de.uni_koblenz.jgralab.greql2.serialising.XMLOutputWriter;
import de.uni_koblenz.jgralab.greql2.types.Path;
import de.uni_koblenz.jgralab.greql2.types.Undefined;
import de.uni_koblenz.jgralabtest.schemas.greqltestschema.localities.CountyTags;

public class StoreValuesTest {

	static Graph graph = null;
	static GreqlEvaluator eval = null;
	static String testdir = "testit/testdata/";

	@BeforeClass
	public static void setUp() {
		try {
			graph = createTestGraph();
			eval = new GreqlEvaluator("", graph, null);
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
		Graph testGraph = GraphIO.loadGraphFromFileWithStandardSupport(
				"testit/testgraphs/greqltestgraph.tg", null);
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
	public void testOutputOfRecord(){
		String qu = "rec(a:5,b:\"Yes\")";
		evaluateQueryAndSaveResult(qu, "outputRecord");
	}
	
	/**
	 * Undefined
	 */
	@Test
	public void testOutputUndefined(){
		Undefined n = Undefined.UNDEFINED;
		generateHTMLandXMLoutput(n, "outputUndefined");
	}
	
	/**
	 * Graph
	 */
	@Test
	public void testOutputOfGraph(){
		generateHTMLandXMLoutput(graph, "outputGraph");	
	}
	
	/**
	 * Vertex
	 */
	@Test
	public void testOutputOfVertex(){
		String qu = "from v : V with id(v)  = 1 report v end";
		evaluateQueryAndSaveResult(qu, "outputVertex");
	}
	
	/**
	 * Edge
	 */
	@Test
	public void testOutputOfEdge(){
		String qu = "from e : E with id(e)  = 1 report e end";
		evaluateQueryAndSaveResult(qu, "outputEdge");
	}
	
	/**
	 * Enum
	 */
	@Test
	public void testOutputOfEnum(){
		generateHTMLandXMLoutput(CountyTags.AREA, "outputEnum");
	}
	
	/**
	 * PVector of Vertices
	 */
	@Test
	public void testOutputOfListOfVertices() {
		String qu = "from airport: V{junctions.Airport}, x: V with x "
				+ "(-->{localities.ContainsLocality} | -->{connections.AirRoute}) airport "
				+ "report x end";
		evaluateQueryAndSaveResult(qu, "outputPVectorOfVertices");
	}

	/**
	 * PSet of Vertices
	 */
	@Test
	public void testOutputOfSetOfVertices() {
		String qu = "from airport: V{junctions.Airport}, x: V with x "
				+ "(-->{localities.ContainsLocality} | -->{connections.AirRoute}) airport "
				+ "reportSet x end";
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
	public void testTableOfIntegers(){
		String queryString = "from i : list (1..10), j : list (1..5) reportTable i-1, j, i*j end";
		evaluateQueryAndSaveResult(queryString, "outputTableOfIntegers");
	}
	
	/**
	 * Tuple of Vertices
	 */
	@Test
	public void testOutputOfTupleOfVertices() {
		String qu = "from a,b:V with connected report a,b end where connected := a-->b";
		
		eval.setQuery(qu);
		eval.startEvaluation();

		PVector<Vertex> result = eval.getResultList(Vertex.class);

		generateHTMLandXMLoutput(result.get(0), "outputTupleOfVertices", graph, true);	
	}
	
	/**
	 * Path
	 */
	@Test
	public void testOutputOfPath(){
		Path p = Path.start(graph.getVertex(1));
		p = p.append(graph.getEdge(135));
		p = p.append(graph.getEdge(136));
		generateHTMLandXMLoutput(p, "outputPath");
	}
	
	/**
	 * Slice
	 */
	@Test
	public void testOutputOfSlice(){
		String qu = "from w: V{localities.Town} report slice(w, <--) end";
		eval.setQuery(qu);
		eval.startEvaluation();
		Object result = eval.getResult();

		try {
			@SuppressWarnings("unused")
			HTMLOutputWriter htmlout = new HTMLOutputWriter(result, new File(
					testdir + "outputSlice" + ".html"), graph, true);
		} catch (IOException e) {
			e.printStackTrace();
			assert false;
		}
	}
	
	/**
	 * Slice
	 */

	@Test(expected = SerialisingException.class)
	public void testOutputOfSliceException(){
		String qu = "from w: V{localities.Town} report slice(w, <--) end";
		eval.setQuery(qu);
		eval.startEvaluation();
		Object result = eval.getResult();

		try {
			@SuppressWarnings("unused")
			XMLOutputWriter htmlout = new XMLOutputWriter(result, new File(
					testdir + "outputSlice" + ".xml"), graph);
		} catch (XMLStreamException e) {
			e.printStackTrace();
		}
	
	}
	
	/**
	 * AttributedElementClass
	 */
	@Test
	public void testOutputOfAttributedElementClas(){
		generateHTMLandXMLoutput(graph.getFirstVertex().getAttributedElementClass(), "outputAttributedElementClass");
	}
	
	/**
	 * PMap from String to Integers
	 */
	@Test
	public void testOutputOfMapFromStringToInteger(){
		String qu = "from v : V{localities.Locality} with v.inhabitants > 100 reportMap v.name -> v.inhabitants end";
		evaluateQueryAndSaveResult(qu, "outputMapFromStringToInteger");
	}
	
	/**
	 * PMap from Vertex to PVector of Vertices
	 */
	@Test 
	public void testOutputOfMapFromVertexToListOfVertices(){
		String qu = "from j : V{junctions.Junction}  reportMap j -> (j <--{connections.Connection}) end";
		evaluateQueryAndSaveResult(qu, "outputMapFromVertexToListOfVertices");
	}
	
	/**
	 * PVector of PSet of Vertices
	 */
	@Test
	public void testOutputOfListOfSetsOfVertices(){
		String qu ="from v : V{junctions.Junction} with count(v-->{connections.AirRoute}) > 0 reportList v-->{connections.AirRoute} end";
		evaluateQueryAndSaveResult(qu, "outputListOfSetsOfVertices");
	}
	
	/**
	 * PMap from Vertex to (PMap from Enumeration to Double)
	 */
	@Test
	public void testOutputOfMapFromVertexToMapFromEnumToDouble(){
		String qu = "from v : V{localities.County} reportMap v -> v.tags end";
		evaluateQueryAndSaveResult(qu, "outputMapFromVertexToMapFromEnumToDouble");
	}
	
	/**
	 * State
	 */
	@Test(expected = SerialisingException.class)
	public void testOutputOfState(){
		State state = new State();
		try {
			@SuppressWarnings("unused")
			HTMLOutputWriter htmlout = new HTMLOutputWriter(state, new File(
					testdir + "outputException" + ".html"), graph, true);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * State
	 */
	@Test(expected = SerialisingException.class)
	public void testOutputOfState2(){
		State state = new State();
		try {
			@SuppressWarnings("unused")
			XMLOutputWriter htmlout = new XMLOutputWriter(state, new File(
					testdir + "outputException" + ".html"), graph);
		} catch (XMLStreamException e) {
			e.printStackTrace();
		}
	}
	//----------------------------------------------------------------------
	
	
	public void evaluateQueryAndSaveResult(String query, String filename) {

		eval.setQuery(query);
		eval.startEvaluation();

		Object result = eval.getResult();

		generateHTMLandXMLoutput(result, filename, graph, true);
	}
	
	
	
	public void generateHTMLandXMLoutput(Object result, String filename){
		generateHTMLandXMLoutput(result, filename, null, false);
	}
	
	public void generateHTMLandXMLoutput(Object result, String filename, Graph graph, boolean elemlinks){
		try {

			@SuppressWarnings("unused")
			HTMLOutputWriter htmlout = new HTMLOutputWriter(result, new File(
					testdir + filename + ".html"), graph, elemlinks);

			@SuppressWarnings("unused")
			XMLOutputWriter xmlout = new XMLOutputWriter(result, new File(
					testdir + filename + ".xml"), graph);

		} catch (XMLStreamException e) {
			e.printStackTrace();
			assert false;
		} catch (IOException e) {
			e.printStackTrace();
			assert false;
		}
	}
}
