package de.uni_koblenz.jgralabtest.greql2;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import de.uni_koblenz.jgralab.Graph;
import de.uni_koblenz.jgralab.GraphIO;
import de.uni_koblenz.jgralab.greql2.evaluator.GreqlEvaluator;
import de.uni_koblenz.jgralab.greql2.serialising.HTMLOutputWriter;
import de.uni_koblenz.jgralab.greql2.serialising.XMLOutputWriter;

public class StoreValuesTest {

	 static Graph graph = null;
	 static GreqlEvaluator eval = null;
	
	@BeforeClass
	public static void setUp() {
		try {
			graph = createTestGraph();
			eval = new GreqlEvaluator("", graph, null);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	@AfterClass
	public static void tearDown() {
		
	}
	
	
	public static Graph createTestGraph() throws Exception {
		Graph testGraph = GraphIO.loadGraphFromFileWithStandardSupport(
				"testit/testgraphs/greqltestgraph.tg", null);
		return testGraph;
	}
	
	@Test
	public void testOutputOfInteger(){		
		//integer
		String qu = "count(V{junctions.Airport})";
		
		printQueryResult(qu, "testit/testdata/greqlWOjvalue_integer");		
	}
	
	@Test
	public void testOutputOfListOfVertices(){
		//list of vertices
		String qu = "from airport: V{junctions.Airport}, x: V with x "
			+ "(-->{localities.ContainsLocality} | -->{connections.AirRoute}) airport "
			+ "report x end";
		printQueryResult(qu, "testit/testdata/greqlWOjvalue_list");

	}
	
	@Test
	public void testOutputOfListOfTupleOfVertices(){
		//list of tuple of vertices
		String qu = "from a,b:V with connected report a,b end where connected := a-->b";
		printQueryResult(qu, "testit/testdata/greqlWOjvalue_ListOfTuple");

	}
	
	public void testMapOfIntegerPairs(){
		//map of integer pairs
		String qu = "from i : list (1..10) reportMap i -> i*i end"; 
		printQueryResult(qu, "testit/testdata/greqlWOjvalue_map");

	}
	
	public void printQueryResult(String query, String filename){
	
		eval.setQuery(query);
		eval.startEvaluation();
		Object result = eval.getEvaluationResult();
		System.out.println(result);
		
		@SuppressWarnings("unused")
		HTMLOutputWriter htmlout = new HTMLOutputWriter(result, filename+".html", graph);
		@SuppressWarnings("unused")
		XMLOutputWriter xmlout = new XMLOutputWriter(result, filename+".xml", graph);
	}
}
