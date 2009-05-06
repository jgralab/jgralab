package de.uni_koblenz.jgralabtest.coretest;


import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Iterator;

import org.junit.Before;
import org.junit.Test;

import de.uni_koblenz.jgralab.Edge;
import de.uni_koblenz.jgralab.Vertex;
import de.uni_koblenz.jgralabtest.schemas.vertextest.*;

public class GraphTest {
	private VertexTestGraph graph;
	private VertexTestGraph graph2;
	
	@Before
	public void setUp() {
		graph = VertexTestSchema.instance().createVertexTestGraph();
		graph2 = VertexTestSchema.instance().createVertexTestGraph();
	}
	
	@Test
	public void testCreateVertex(){
		Vertex v1 = graph.createVertex(SubNode.class);
		Vertex v2 = graph2.createVertex(SubNode.class);		
		Vertex v3 = graph.createVertex(SuperNode.class);
		Vertex v4 = graph2.createVertex(SuperNode.class);
		Vertex v5 = graph.createVertex(DoubleSubNode.class);
		Vertex v6 = graph2.createVertex(DoubleSubNode.class);
		
		//tests whether the vertex is an instance of the expected class 
		assertTrue(v1 instanceof SubNode);
		assertTrue(v2 instanceof SubNode);
		assertTrue(v3 instanceof SuperNode);
		assertTrue(v4 instanceof SuperNode);
		assertTrue(v5 instanceof DoubleSubNode);
		assertTrue(v6 instanceof DoubleSubNode);
		
		//tests whether the graphs now contain the right vertices 
		assertTrue(graph.containsVertex(v1));
		assertTrue(graph2.containsVertex(v2));
		assertTrue(graph.containsVertex(v3));
		assertTrue(graph2.containsVertex(v4));
		assertTrue(graph.containsVertex(v5));
		assertTrue(graph2.containsVertex(v6));
		
		assertFalse(graph2.containsVertex(v1));
		assertFalse(graph.containsVertex(v2));
		assertFalse(graph2.containsVertex(v3));
		assertFalse(graph.containsVertex(v4));
		assertFalse(graph2.containsVertex(v5));
		assertFalse(graph.containsVertex(v6));		

/*		for(Iterator<Vertex> i = graph.vertices().iterator();  i.hasNext(); i.next()){
		}*/
		
		System.out.println("Done testing createVertex.");
	}
	
	@Test
	public void testCreateEdge(){
		
	}
	
	@Test
	public void testIsLoading(){
	}
	
	@Test
	public void testLoadingCompleted(){
		
	}
	
	@Test
	public void testIsGraphModified(){
		
	}
	
	@Test
	public void testGetGraphVersion(){
/*		long l = graph.getGraphVersion();
		System.out.println("GraphVersion:" + l);
		long l2 = graph.getGraphVersion();
		System.out.println("GraphVersion:" + l2);*/
	}
	
	@Test
	public void testIsVertexListModified(){
		
	}
	
	@Test
	public void testGetVertexListVersion(){
		
	}
	
	@Test
	public void testIsEdgeListModified(){
		
	}
	
	@Test
	public void testGetEdgeListVersion(){
		
	}
	
	@Test
	public void testContainsVertex(){
		DoubleSubNode v1 = graph.createDoubleSubNode();
		DoubleSubNode v2 = graph2.createDoubleSubNode();
		SubNode v3 = graph.createSubNode();
		SubNode v4 = graph2.createSubNode();
		SuperNode v5 = graph.createSuperNode();
		SuperNode v6 = graph2.createSuperNode();
		
		assertTrue(graph.containsVertex(v1));
		assertTrue(graph.containsVertex(v3));
		assertTrue(graph.containsVertex(v5));
		assertTrue(graph2.containsVertex(v2));
		assertTrue(graph2.containsVertex(v4));
		assertTrue(graph2.containsVertex(v6));
		
		assertFalse(graph.containsVertex(v2));
		assertFalse(graph.containsVertex(v4));
		assertFalse(graph.containsVertex(v6));
		assertFalse(graph2.containsVertex(v1));
		assertFalse(graph2.containsVertex(v3));
		assertFalse(graph2.containsVertex(v5));
		System.out.println("Done testing containsVertex.");
	}
	
	@Test
	public void testContainsEdge(){
		DoubleSubNode v1 = graph.createDoubleSubNode();
		DoubleSubNode v2 = graph.createDoubleSubNode();
		DoubleSubNode v3 = graph2.createDoubleSubNode();
		SubNode v4 = graph.createSubNode();
		SubNode v5 = graph2.createSubNode();
		SuperNode v6 = graph.createSuperNode();
		SuperNode v7 = graph.createSuperNode();
		SuperNode v8 = graph2.createSuperNode();
		
		SubLink e1 = graph.createSubLink(v1, v6);
		SubLink e2 = graph2.createSubLink(v3, v8);
		Link e3 = graph.createLink(v2, v7);
		Link e4 = graph2.createLink(v5, v8);
		LinkBack e5 = graph.createLinkBack(v6, v4);
		LinkBack e6 = graph2.createLinkBack(v8, v3);
		
		assertTrue(graph.containsEdge(e1));
		assertTrue(graph2.containsEdge(e2));
		assertTrue(graph.containsEdge(e3));
		assertTrue(graph2.containsEdge(e4));
		assertTrue(graph.containsEdge(e5));
		assertTrue(graph2.containsEdge(e6));
		
		assertFalse(graph2.containsEdge(e1));
		assertFalse(graph.containsEdge(e2));
		assertFalse(graph2.containsEdge(e3));
		assertFalse(graph.containsEdge(e4));
		assertFalse(graph2.containsEdge(e5));
		assertFalse(graph.containsEdge(e6));
		
		System.out.println("Done testing containsEdge.");
	}
	
	@Test
	public void testDeleteVertex(){
		
	}
	
	@Test
	public void testVertexDeleted(){
		
	}
	
	@Test
	public void testVertexAdded(){
		
	}
	
	@Test
	public void testDeleteEdge(){
		
	}
	
	@Test
	public void testEdgeDeleted(){
		
	}
	
	@Test
	public void testEdgeAdded(){
		
	}
	
	@Test
	public void testGetFirstVertex(){
		
	}
	
	@Test
	public void testGetLastVertex(){
		
	}
	
	@Test
	public void testGetFirstVertexOfClass(){
		
	}
	
	@Test
	public void testGetFirstVertexOfClass2(){
		
	}
	
	@Test
	public void testGetFirstVertexOfClass3(){
		
	}
	
	@Test
	public void testGetFirstVertexOfClass4(){
		
	}
	
	@Test
	public void testGetFirstEdgeInGraph(){
		
	}
	
	@Test
	public void testGetLastEdgeInGraph(){
		
	}
	
	@Test
	public void testGetFirstEdgeOfClassInGraph(){
		
	}
	
	@Test
	public void testGetFirstEdgeOfClassInGraph2(){
		
	}
	
	@Test
	public void testGetFirstEdgeOfClassInGraph3(){
		
	}
	
	@Test
	public void testGetFirstEdgeOfClassInGraph4(){
		
	}
	
	@Test
	public void testGetVertex(){
		
	}
	
	@Test
	public void testGetEdge(){
		
	}
	
	@Test
	public void testGetMaxVCount(){
		
	}
	
	@Test
	public void testGetExpandedVertexCount(){
		
	}
	
	@Test
	public void testGetExpandedEdgeCount(){
		
	}
	
	@Test
	public void testGetMaxECount(){
		
	}
	
	@Test
	public void testGetVCount(){
		
	}
	
	@Test
	public void testGetECount(){
		
	}
	
	@Test
	public void testGetId(){
		
	}
	
	@Test
	public void testSetId(){
		
	}
	
	@Test
	public void testEdges(){
		
	}
	
	@Test
	public void testEdges2(){
		
	}
	
	@Test
	public void testEdges3(){
		
	}
	
	@Test
	public void testVertices(){
		
	}
	
	@Test
	public void testVertices2(){
		
	}
	
	@Test
	public void testVertices3(){
		
	}

	@Test
	public void testDefragment(){
		
	}
	
}
