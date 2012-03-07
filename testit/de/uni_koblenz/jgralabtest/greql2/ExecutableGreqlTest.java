package de.uni_koblenz.jgralabtest.greql2;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.util.HashMap;
import java.util.Map;

import org.junit.Test;
import org.pcollections.PSet;

import de.uni_koblenz.jgralab.Graph;
import de.uni_koblenz.jgralab.GraphIOException;
import de.uni_koblenz.jgralab.JGraLab;
import de.uni_koblenz.jgralab.Vertex;
import de.uni_koblenz.jgralab.graphmarker.GraphMarker;
import de.uni_koblenz.jgralab.greql2.evaluator.GreqlEvaluator;
import de.uni_koblenz.jgralab.greql2.evaluator.vertexeval.VertexEvaluator;
import de.uni_koblenz.jgralab.greql2.executable.GreqlCodeGenerator;
import de.uni_koblenz.jgralab.greql2.executable.queries.SampleQuery;
import de.uni_koblenz.jgralab.greql2.parser.GreqlParser;
import de.uni_koblenz.jgralab.greql2.schema.Greql2;
import de.uni_koblenz.jgralab.greql2.schema.Greql2Schema;
import de.uni_koblenz.jgralab.greql2.types.Tuple;
import de.uni_koblenz.jgralabtest.schemas.greqltestschema.junctions.Airport;
import de.uni_koblenz.jgralabtest.schemas.greqltestschema.localities.County;
import de.uni_koblenz.jgralabtest.schemas.greqltestschema.localities.Locality;



public class ExecutableGreqlTest extends GenericTest {

	String path = "src/";
	
	@Test
	public void testSimpleFunction() {
		String query = "2 + 3";
		GreqlEvaluator eval = new GreqlEvaluator(query, null, null);
		GraphMarker<VertexEvaluator> graphMarker = eval.getVertexEvaluatorGraphMarker();
		Greql2 queryGraph = eval.getSyntaxGraph();
		GreqlCodeGenerator greqlcodeGen = new GreqlCodeGenerator(queryGraph, graphMarker, Greql2Schema.instance());
		try {
			greqlcodeGen.createFiles(path);
		} catch (GraphIOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	
	@Test
	public void testGenerateComprehension() {
		String query = "using X,Y: from x:X, y:Y with (y % 2) reportList x*y end";
		//String query = "using X,Y: from x:X, y:Y with (true) and true reportList x*y end";
		GreqlEvaluator eval = new GreqlEvaluator(query, null, null);
		eval.createOptimizedSyntaxGraph();
		GraphMarker<VertexEvaluator> graphMarker = eval.getVertexEvaluatorGraphMarker();
		Greql2 queryGraph = eval.getSyntaxGraph();
		GreqlCodeGenerator greqlcodeGen = new GreqlCodeGenerator(queryGraph, graphMarker, Greql2Schema.instance());
		try {
			greqlcodeGen.createFiles(path);
		} catch (GraphIOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	@Test
	public void testGenerateForallExpression() {
		//String query = "using X,Y: from x:X, y:Y with (y % 2 <> 1) and (x % 3 = 0) reportList x*y end";
		String query = "using X,Y: forall x:X, y:Y @ x*y > 0";
		GreqlEvaluator eval = new GreqlEvaluator(query, null, null);
		GraphMarker<VertexEvaluator> graphMarker = eval.getVertexEvaluatorGraphMarker();
		Greql2 queryGraph = eval.getSyntaxGraph();
		GreqlCodeGenerator greqlcodeGen = new GreqlCodeGenerator(queryGraph, graphMarker, Greql2Schema.instance());
		try {
			greqlcodeGen.createFiles(path);
		} catch (GraphIOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	@Test
	public void testGenerateListConstruction() {
		String query = "list(1,2,3)";
		GreqlEvaluator eval = new GreqlEvaluator(query, null, null);
		GraphMarker<VertexEvaluator> graphMarker = eval.getVertexEvaluatorGraphMarker();
		Greql2 queryGraph = eval.getSyntaxGraph();
		GreqlCodeGenerator greqlcodeGen = new GreqlCodeGenerator(queryGraph, graphMarker, Greql2Schema.instance());
		try {
			greqlcodeGen.createFiles(path);
		} catch (GraphIOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	@Test
	public void testGenerateListRangeConstruction() {
		String query = "list(1..1000)";
		GreqlEvaluator eval = new GreqlEvaluator(query, null, null);
		GraphMarker<VertexEvaluator> graphMarker = eval.getVertexEvaluatorGraphMarker();
		Greql2 queryGraph = eval.getSyntaxGraph();
		GreqlCodeGenerator greqlcodeGen = new GreqlCodeGenerator(queryGraph, graphMarker, Greql2Schema.instance());
		try {
			greqlcodeGen.createFiles(path);
		} catch (GraphIOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	@Test
	public void testGenerateMapComprehension() {
		String query = "using X,Y: from x:X, y:Y reportMap y->x end";
		GreqlEvaluator eval = new GreqlEvaluator(query, null, null);
		eval.createOptimizedSyntaxGraph();
		GraphMarker<VertexEvaluator> graphMarker = eval.getVertexEvaluatorGraphMarker();
		Greql2 queryGraph = eval.getSyntaxGraph();
		GreqlCodeGenerator greqlcodeGen = new GreqlCodeGenerator(queryGraph, graphMarker, Greql2Schema.instance());
		try {
			greqlcodeGen.createFiles(path);
		} catch (GraphIOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	@Test
	public void testGenerateVertexSetExpression() {
		//String query = "using X,Y: from x:X, y:Y with (y % 2 <> 1) and (x % 3 = 0) reportList x*y end";
		String query = "from v:V{MyVertex} report v end";
		GreqlEvaluator eval = new GreqlEvaluator(query, null, null);
		GraphMarker<VertexEvaluator> graphMarker = eval.getVertexEvaluatorGraphMarker();
		Greql2 queryGraph = eval.getSyntaxGraph();
		GreqlCodeGenerator greqlcodeGen = new GreqlCodeGenerator(queryGraph, graphMarker, Greql2Schema.instance());
		try {
			greqlcodeGen.createFiles(path);
		} catch (GraphIOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	
	@Test
	public void testEvaluateAlternativePathDescription2() throws Exception {
		String queryString = "from v:V{NamedElement} reportSet v, v.name, v (-->{^connections.Way, ^connections.AirRoute} | (-->{localities.ContainsLocality} -->{connections.AirRoute}))* end";
		PSet<Tuple> result = (PSet<Tuple>) evalTestQuery(queryString);

		for (Tuple tuple : result) {
			Vertex vertex = (Vertex) tuple.get(0);
			if (!(vertex instanceof Airport || vertex instanceof County || vertex instanceof Locality)) {
				fail();
			}
		}

		SampleQuery generatedQuery = new SampleQuery();
		Object resultOfGeneratedQuery = generatedQuery.execute(getTestGraph(TestVersion.ROUTE_MAP_GRAPH));
		assertEquals(result, (PSet<Tuple>)resultOfGeneratedQuery);
	}
	
	@Test
	public void testGenerateForwardVertexSet() {
		String query = "from v:V{NamedElement} reportSet v, v.name, v (-->{^connections.Way, ^connections.AirRoute} | (-->{localities.ContainsLocality} v -->{connections.AirRoute @ (thisEdge) }))* end";
		Graph testGraph = null;
		try {
			testGraph = getTestGraph(TestVersion.ROUTE_MAP_GRAPH);
		} catch (Exception e1) {
			e1.printStackTrace();
		}
		GreqlEvaluator eval = new GreqlEvaluator(query,  testGraph, new HashMap<String, Object>());
		eval.createOptimizedSyntaxGraph();
		GraphMarker<VertexEvaluator> graphMarker = eval.getVertexEvaluatorGraphMarker();
		Greql2 queryGraph = eval.getSyntaxGraph();
		if (queryGraph == null) {
			System.out.println("Query graph is null");
			queryGraph = GreqlParser.parse(query);
			if (queryGraph == null)
				System.out.println("Parsing exception");
		}
		GreqlCodeGenerator greqlcodeGen = new GreqlCodeGenerator(queryGraph, graphMarker, testGraph.getSchema());
		try {
			greqlcodeGen.createFiles(path);
		} catch (GraphIOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	

	@Test
	public void testGeneratedComprehension() {
		System.out.println("Testing generated comprehensin");
		Map<String, Object> boundVars = new HashMap<String, Object>();
		PSet x = JGraLab.set();
		for (int i=1; i<2000; i++) {
			x = x.plus(i);
		}
		PSet y = JGraLab.set();
		for (int i=1; i<3000; i++) {
			y = y.plus(i);
		}
		boundVars.put("X", x);
		boundVars.put("Y", y);
		long startTime = System.currentTimeMillis();
	//	new SampleQuery().execute(null, boundVars);
		long usedTime = System.currentTimeMillis() - startTime;
		System.out.println("Evaluation of generated query took " + usedTime + "msec");
		
		
		
	}
	
}
