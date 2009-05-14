package de.uni_koblenz.jgralabtest;


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
		SubNode v1 = graph.createSubNode();
		SubNode v2 = graph.createSubNode();
		SuperNode v3 = graph.createSuperNode();
		SuperNode v4 = graph.createSuperNode();
		DoubleSubNode v5 = graph.createDoubleSubNode();
		DoubleSubNode v6 = graph.createDoubleSubNode();
		DoubleSubNode v7 = graph2.createDoubleSubNode();
		
		Edge e1 = graph.createEdge(SubLink.class, v5, v3);
		Edge e2 = graph.createEdge(SubLink.class, v6, v4);
		Edge e3 = graph.createEdge(SubLink.class, v7, v3);
		Edge e4 = graph.createEdge(Link.class, v1, v3);
		Edge e5 = graph.createEdge(Link.class, v2, v4);
		Edge e6 = graph.createEdge(Link.class, v5, v4);
		Edge e7 = graph.createEdge(Link.class, v6, v3);
		Edge e8 = graph.createEdge(Link.class, v7, v4);
		Edge e9 = graph.createEdge(LinkBack.class, v3, v1);
		Edge e10 = graph.createEdge(LinkBack.class, v4, v2);
		Edge e11 = graph.createEdge(LinkBack.class, v3, v5);
		Edge e12 = graph.createEdge(LinkBack.class, v4, v6);
		Edge e13 = graph.createEdge(LinkBack.class, v3, v7);
		
		assertTrue(graph.containsEdge(e1));
		assertTrue(graph.containsEdge(e2));
		assertTrue(graph.containsEdge(e3));
		assertTrue(graph.containsEdge(e4));
		assertTrue(graph.containsEdge(e5));
		assertTrue(graph.containsEdge(e6));
		assertTrue(graph.containsEdge(e7));
		assertTrue(graph.containsEdge(e8));
		assertTrue(graph.containsEdge(e9));
		assertTrue(graph.containsEdge(e10));
		assertFalse(graph2.containsEdge(e1));
		assertFalse(graph2.containsEdge(e2));	
		assertFalse(graph2.containsEdge(e3));	
		assertFalse(graph2.containsEdge(e4));	
		assertFalse(graph2.containsEdge(e5));	
		assertFalse(graph2.containsEdge(e6));	
		assertFalse(graph2.containsEdge(e7));	
		assertFalse(graph2.containsEdge(e8));	
		assertFalse(graph2.containsEdge(e9));	
		assertFalse(graph2.containsEdge(e10));
		
		System.out.println("Done testing createEdge.");
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
		assertEquals(0, graph.getGraphVersion());
		graph.createDoubleSubNode();
		assertEquals(1, graph.getGraphVersion());
		graph.createDoubleSubNode();
		assertEquals(2, graph.getGraphVersion());
		DoubleSubNode v1 = graph.createDoubleSubNode();
		graph.createDoubleSubNode();
		assertEquals(4, graph.getGraphVersion());
		for (int i = 0; i < 20; i++){
			graph.createSubNode();
			assertEquals(i+5, graph.getGraphVersion());
		}
		assertEquals(24, graph.getGraphVersion());
		graph.deleteVertex(v1);
		assertEquals(25, graph.getGraphVersion());
		
		System.out.println("Done testing getGraphVersion.");
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
		SubNode v1 = graph.createSubNode();
		SubNode v2 = graph.createSubNode();
		SubNode v3 = graph.createSubNode();
		SuperNode v4 = graph.createSuperNode();
		SuperNode v5 = graph.createSuperNode();
		SuperNode v6 = graph.createSuperNode();
		DoubleSubNode v7 = graph.createDoubleSubNode();
		DoubleSubNode v8 = graph.createDoubleSubNode();
		DoubleSubNode v9 = graph.createDoubleSubNode();
		
		SubNode v10 = graph2.createSubNode();
		SuperNode v11 = graph2.createSuperNode();
		DoubleSubNode v12 = graph2.createDoubleSubNode();
		
		graph.deleteVertex(v1);
		assertFalse(graph.containsVertex(v1));
		graph.deleteVertex(v2);
		assertFalse(graph.containsVertex(v2));
		graph.deleteVertex(v3);
		assertFalse(graph.containsVertex(v3));
		graph.deleteVertex(v4);
		assertFalse(graph.containsVertex(v4));
		graph.deleteVertex(v5);
		assertFalse(graph.containsVertex(v5));
		graph.deleteVertex(v6);
		assertFalse(graph.containsVertex(v6));
		graph.deleteVertex(v7);
		assertFalse(graph.containsVertex(v7));
		graph.deleteVertex(v8);
		assertFalse(graph.containsVertex(v8));
		graph.deleteVertex(v9);
		assertFalse(graph.containsVertex(v9));
		try{
			graph.deleteVertex(v10);
		}catch(NullPointerException e){
		}
		try{
			graph.deleteVertex(v11);
		}catch(NullPointerException e){
			
		}
		try{
			graph.deleteVertex(v12);
		}catch(NullPointerException e){
			
		}
		System.out.println("Done testing deleteVertex.");
	}
	
	@Test
	public void testVertexDeleted(){
		
	}
	
	@Test
	public void testVertexAdded(){
		
	}
	
	@Test
	public void testDeleteEdge(){
		SubNode v1 = graph.createSubNode();
		SubNode v2 = graph.createSubNode();
		SubNode v3 = graph2.createSubNode();
		SuperNode v4 = graph.createSuperNode();
		SuperNode v5 = graph.createSuperNode();
		SuperNode v6 = graph2.createSuperNode();
		DoubleSubNode v7 = graph.createDoubleSubNode();
		DoubleSubNode v8 = graph.createDoubleSubNode();
		DoubleSubNode v9 = graph2.createDoubleSubNode();
		
		Link e1 = graph.createLink(v1, v4);
		Link e2 = graph.createLink(v7, v5);
		Link e3 = graph.createLink(v2, v6);
		SubLink e4 = graph.createSubLink(v7, v4);
		SubLink e5 = graph.createSubLink(v9, v5);
		LinkBack e6 = graph.createLinkBack(v4, v7);
		LinkBack e7 = graph.createLinkBack(v5, v2);
		LinkBack e8 = graph.createLinkBack(v6, v3);
		
		graph.deleteEdge(e1);
		assertFalse(graph.containsEdge(e1));
		graph.deleteEdge(e2);
		assertFalse(graph.containsEdge(e2));
		graph.deleteEdge(e3);
		assertFalse(graph.containsEdge(e3));
		graph.deleteEdge(e4);
		assertFalse(graph.containsEdge(e4));
		graph.deleteEdge(e5);
		assertFalse(graph.containsEdge(e5));
		graph.deleteEdge(e6);
		assertFalse(graph.containsEdge(e6));
		graph.deleteEdge(e7);
		assertFalse(graph.containsEdge(e7));
		graph.deleteEdge(e8);
		assertFalse(graph.containsEdge(e8));
		
		System.out.println("Done testing deleteEdge");
	}
	
	@Test
	public void testEdgeDeleted(){
		
	}
	
	@Test
	public void testEdgeAdded(){
		
	}
	
	@Test
	public void testGetFirstVertex(){
		assertEquals(null, graph.getFirstVertex());
		assertEquals(null, graph2.getFirstVertex());
		
		SubNode v1 = graph.createSubNode();
		SuperNode v2 = graph.createSuperNode();
		
		assertEquals(v1, graph.getFirstVertex());
		
		DoubleSubNode v3 = graph2.createDoubleSubNode();
		SubNode v4 = graph2.createSubNode();
		assertEquals(v3, graph2.getFirstVertex());
		
		DoubleSubNode v5 = graph.createDoubleSubNode();
		assertEquals(v1, graph.getFirstVertex());
		
		System.out.println("Done testing getFirstVertex.");
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
