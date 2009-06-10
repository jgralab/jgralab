package de.uni_koblenz.jgralabtest;


import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

//import java.util.Iterator;

import org.junit.Before;
import org.junit.Test;

import de.uni_koblenz.jgralab.Edge;
import de.uni_koblenz.jgralab.Vertex;
import de.uni_koblenz.jgralabtest.schemas.vertextest.*;
import de.uni_koblenz.jgralabtest.schemas.minimal.MinimalGraph;
import de.uni_koblenz.jgralabtest.schemas.minimal.MinimalSchema;

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
		Vertex[] graphVertices = {v1, v3, v5};
		Vertex[] graph2Vertices = {v2, v4, v6};
		
		//tests whether the vertex is an instance of the expected class 
		assertTrue(v1 instanceof SubNode);
		assertTrue(v2 instanceof SubNode);
		assertTrue(v3 instanceof SuperNode);
		assertTrue(v4 instanceof SuperNode);
		assertTrue(v5 instanceof DoubleSubNode);
		assertTrue(v6 instanceof DoubleSubNode);	

		//tests whether the graphs contain the right vertices in the right order
		int i = 0;//the position of the vertex corresponding to the one currently returned by the iterator
		for (Vertex v : graph.vertices()){
			assertEquals(graphVertices[i], v);
			i++;
		}
		i = 0;
		for (Vertex v : graph2.vertices()){
			assertEquals(graph2Vertices[i], v);
			i++;
		}
		
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
		Edge e14 = graph.createEdge(LinkBack.class, v4, v6); //the same as e12
		
		//tests whether the edge is an instance of the expected class
		assertTrue(e1 instanceof SubLink);
		assertTrue(e2 instanceof SubLink);
		assertTrue(e3 instanceof SubLink);
		assertTrue(e4 instanceof Link);
		assertTrue(e5 instanceof Link);
		assertTrue(e6 instanceof Link);
		assertTrue(e7 instanceof Link);
		assertTrue(e8 instanceof Link);
		assertTrue(e9 instanceof LinkBack);
		assertTrue(e10 instanceof LinkBack);
		assertTrue(e11 instanceof LinkBack);
		assertTrue(e12 instanceof LinkBack);
		assertTrue(e13 instanceof LinkBack);
		assertTrue(e14 instanceof LinkBack);
			
		/*tests whether the edges are part of the right graph and have been inserted 
		 * in the right order
		 */
		Edge[] graphEdges = {e1, e2, e3, e4, e5, e6, e7, e8, e9, e10, e11, e12, e13, e14};
		int i = 0;//refers to the position of the edge which the iterator currently returns
		for (Edge e : graph.edges()){
			assertEquals(graphEdges[i], e);
			i++;
		}
		
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
		//border cases
		long l1 = graph.getVertexListVersion();
		long l2 = graph2.getVertexListVersion();
		assertFalse(graph.isVertexListModified(l1));
		assertFalse(graph2.isVertexListModified(l2));
		
		Vertex v1 = graph.createVertex(DoubleSubNode.class);
		assertTrue(graph.isVertexListModified(l1));
		l1 = graph.getVertexListVersion();
		assertFalse(graph.isVertexListModified(l1));
		
		Vertex v2 = graph.createVertex(SuperNode.class);
		assertTrue(graph.isVertexListModified(l1));
		l1 = graph.getVertexListVersion();
		assertFalse(graph.isVertexListModified(l1));
		
		//makes sure that changing edges does not affect the vertexList
		graph.createEdge(SubLink.class, v1, v2);
		assertFalse(graph.isVertexListModified(l1));
		graph.createEdge(Link.class, v1, v2);
		assertFalse(graph.isVertexListModified(l1));
		l1 = graph.getVertexListVersion();
		assertFalse(graph.isVertexListModified(l1));
		
		graph.deleteVertex(v2);
		assertTrue(graph.isVertexListModified(l1));
		l1 = graph.getVertexListVersion();
		assertFalse(graph.isVertexListModified(l1));
		
		//normal cases
		for (int i = 0; i< 21; i++){
			graph.createVertex(SubNode.class);
			assertTrue(graph.isVertexListModified(l1));
			l1 = graph.getVertexListVersion();
			assertFalse(graph.isVertexListModified(l1));
		}
		
		graph.deleteVertex(v1);
		assertTrue(graph.isVertexListModified(l1));
		l1 = graph.getVertexListVersion();
		assertFalse(graph.isVertexListModified(l1));
		
		for (int i=0; i<12; i++){
			graph.createVertex(SuperNode.class);
			assertTrue(graph.isVertexListModified(l1));
			l1 = graph.getVertexListVersion();
			assertFalse(graph.isVertexListModified(l1));
		}
		
		System.out.println("Done testing isVertexListModified.");
	}
	
	@Test
	public void testGetVertexListVersion(){
		//border cases
		assertEquals(0, graph.getVertexListVersion());
		assertEquals(0, graph2.getVertexListVersion());
		graph.createVertex(SubNode.class);
		assertEquals(1, graph.getVertexListVersion());
		Vertex v1 = graph2.createVertex(SuperNode.class);
		assertEquals(1, graph2.getVertexListVersion());		
		
		//normal cases
		graph.createVertex(SuperNode.class);
		assertEquals(2, graph.getVertexListVersion());
		graph.createVertex(DoubleSubNode.class);
		assertEquals(3, graph.getVertexListVersion());
		for(int i = 4; i < 100; i++){
			graph.createVertex(SuperNode.class);
			assertEquals(i, graph.getVertexListVersion());
		}
		graph.createVertex(DoubleSubNode.class);
		assertEquals(100, graph.getVertexListVersion());
		
		//tests whether the version changes correctly if vertices are deleted 
		graph2.deleteVertex(v1);
		assertEquals(2, graph2.getVertexListVersion());
		for(int i = 3; i<21; i+=3){
			graph2.createVertex(DoubleSubNode.class);
			assertEquals(i, graph2.getVertexListVersion());
			graph2.createVertex(SubNode.class);
			assertEquals(i+1, graph2.getVertexListVersion());
			graph2.createVertex(SuperNode.class);
			assertEquals(i+2, graph2.getVertexListVersion());
		}
		Vertex v2 = graph2.createVertex(SuperNode.class);
		assertEquals(21, graph2.getVertexListVersion());
		Vertex v3 = graph2.createVertex(DoubleSubNode.class);
		assertEquals(22, graph2.getVertexListVersion());
		graph2.deleteVertex(v3);
		assertEquals(23, graph2.getVertexListVersion());
		graph2.deleteVertex(v2);
		assertEquals(24, graph2.getVertexListVersion());
		
		//makes sure that editing edges does not change the vertexList
		graph2.createEdge(SubLink.class, v3, v2);
		assertEquals(24, graph2.getVertexListVersion());
		graph2.createEdge(LinkBack.class, v2, v3);
		assertEquals(24, graph2.getVertexListVersion());
		
		System.out.println("Done testing getVertexListVersion.");
	}
	
	@Test
	public void testIsEdgeListModified(){
		//preparations...
		Vertex v1 = graph.createVertex(SubNode.class);
		Vertex v2 = graph.createVertex(SubNode.class);
		Vertex v3 = graph.createVertex(SubNode.class);
		Vertex v4 = graph.createVertex(SubNode.class);
		Vertex v5 = graph.createVertex(SuperNode.class);
		Vertex v6 = graph.createVertex(SuperNode.class);
		Vertex v7 = graph.createVertex(SuperNode.class);
		Vertex v8 = graph.createVertex(SuperNode.class);
		Vertex v9 = graph.createVertex(DoubleSubNode.class);
		Vertex v10 = graph.createVertex(DoubleSubNode.class);
		Vertex v11 = graph.createVertex(DoubleSubNode.class);
		Vertex v12 = graph.createVertex(DoubleSubNode.class);
		
		//border cases
		long elv1 = graph.getEdgeListVersion();
		long elv2 = graph2.getEdgeListVersion();
		assertFalse(graph.isEdgeListModified(elv1));
		assertFalse(graph2.isEdgeListModified(elv2));
		
		graph.createEdge(SubLink.class, v11, v7);
		Edge e1 = graph2.createEdge(Link.class, v3, v7);
		assertTrue(graph.isEdgeListModified(elv1));
		assertTrue(graph2.isEdgeListModified(elv2));
		elv1 = graph.getEdgeListVersion();
		elv2 = graph2.getEdgeListVersion();
		assertFalse(graph.isEdgeListModified(elv1));
		assertFalse(graph2.isEdgeListModified(elv2));
		
		graph2.deleteEdge(e1);
		assertTrue(graph2.isEdgeListModified(elv2));
		elv2 = graph2.getEdgeListVersion();
		assertFalse(graph2.isEdgeListModified(elv2));
		
		//normal cases
		graph2.createEdge(LinkBack.class, v7, v3);
		assertTrue(graph2.isEdgeListModified(elv2));
		elv2 = graph2.getEdgeListVersion();
		assertFalse(graph2.isEdgeListModified(elv2));

		graph2.createEdge(Link.class, v3, v7);
		assertTrue(graph2.isEdgeListModified(elv2));
		elv2 = graph2.getEdgeListVersion();
		assertFalse(graph2.isEdgeListModified(elv2));

		Edge e2 = graph2.createEdge(SubLink.class, v11, v7);
		assertTrue(graph2.isEdgeListModified(elv2));
		elv2 = graph2.getEdgeListVersion();
		assertFalse(graph2.isEdgeListModified(elv2));

		graph2.createEdge(Link.class, v4, v8);
		assertTrue(graph2.isEdgeListModified(elv2));
		elv2 = graph2.getEdgeListVersion();
		assertFalse(graph2.isEdgeListModified(elv2));

		Edge e3 = graph2.createEdge(Link.class, v11, v8);
		assertTrue(graph2.isEdgeListModified(elv2));
		elv2 = graph2.getEdgeListVersion();
		assertFalse(graph2.isEdgeListModified(elv2));

		graph2.createEdge(Link.class, v12, v7);
		assertTrue(graph2.isEdgeListModified(elv2));
		elv2 = graph2.getEdgeListVersion();
		assertFalse(graph2.isEdgeListModified(elv2));

		graph2.createEdge(LinkBack.class, v8, v4);
		assertTrue(graph2.isEdgeListModified(elv2));
		elv2 = graph2.getEdgeListVersion();
		assertFalse(graph2.isEdgeListModified(elv2));

		Edge e4 = graph2.createEdge(SubLink.class, v12, v8);
		assertTrue(graph2.isEdgeListModified(elv2));
		elv2 = graph2.getEdgeListVersion();
		assertFalse(graph2.isEdgeListModified(elv2));
		
		graph2.deleteEdge(e2);
		assertTrue(graph2.isEdgeListModified(elv2));
		elv2 = graph2.getEdgeListVersion();
		assertFalse(graph2.isEdgeListModified(elv2));

		graph2.createEdge(LinkBack.class, v7, v11);
		assertTrue(graph2.isEdgeListModified(elv2));
		elv2 = graph2.getEdgeListVersion();
		assertFalse(graph2.isEdgeListModified(elv2));

		graph2.createEdge(LinkBack.class, v8, v12);
		assertTrue(graph2.isEdgeListModified(elv2));
		elv2 = graph2.getEdgeListVersion();
		assertFalse(graph2.isEdgeListModified(elv2));

		graph2.deleteEdge(e4);
		assertTrue(graph2.isEdgeListModified(elv2));
		elv2 = graph2.getEdgeListVersion();
		assertFalse(graph2.isEdgeListModified(elv2));
		
		graph2.deleteEdge(e3);
		assertTrue(graph2.isEdgeListModified(elv2));
		elv2 = graph2.getEdgeListVersion();
		assertFalse(graph2.isEdgeListModified(elv2));
		
		graph2.createEdge(SubLink.class, v9, v5);
		assertTrue(graph2.isEdgeListModified(elv2));
		elv2 = graph2.getEdgeListVersion();
		assertFalse(graph2.isEdgeListModified(elv2));
		
		graph2.createEdge(Link.class, v1, v6);
		assertTrue(graph2.isEdgeListModified(elv2));
		elv2 = graph2.getEdgeListVersion();
		assertFalse(graph2.isEdgeListModified(elv2));
		
		graph2.createEdge(LinkBack.class, v5, v2);
		assertTrue(graph2.isEdgeListModified(elv2));
		elv2 = graph2.getEdgeListVersion();
		assertFalse(graph2.isEdgeListModified(elv2));
		
		graph2.createEdge(Link.class, v10, v6);
		assertTrue(graph2.isEdgeListModified(elv2));
		elv2 = graph2.getEdgeListVersion();
		assertFalse(graph2.isEdgeListModified(elv2));
		
		System.out.println("Done testing isEdgeListModified.");
	}
	
	@Test
	public void testGetEdgeListVersion(){
		//preparations...
		Vertex v1 = graph.createVertex(SubNode.class);
		Vertex v2 = graph.createVertex(SubNode.class);
		Vertex v3 = graph.createVertex(SubNode.class);
		Vertex v4 = graph.createVertex(SubNode.class);
		Vertex v5 = graph.createVertex(SuperNode.class);
		Vertex v6 = graph.createVertex(SuperNode.class);
		Vertex v7 = graph.createVertex(SuperNode.class);
		Vertex v8 = graph.createVertex(SuperNode.class);
		Vertex v9 = graph.createVertex(DoubleSubNode.class);
		Vertex v10 = graph.createVertex(DoubleSubNode.class);
		Vertex v11 = graph.createVertex(DoubleSubNode.class);
		Vertex v12 = graph.createVertex(DoubleSubNode.class);
		
		//border cases
		assertEquals(0, graph.getEdgeListVersion());
		assertEquals(0, graph2.getEdgeListVersion());
		
		Edge e1 = graph.createEdge(SubLink.class, v9, v7);
		assertEquals(1, graph.getEdgeListVersion());
		
		graph.deleteEdge(e1);
		assertEquals(2, graph.getEdgeListVersion());
		
		//normal cases
		graph.createEdge(SubLink.class, v10, v5);
		assertEquals(3, graph.getEdgeListVersion());
		graph.createEdge(SubLink.class, v10, v6);
		assertEquals(4, graph.getEdgeListVersion());
		graph.createEdge(SubLink.class, v10, v7);
		assertEquals(5, graph.getEdgeListVersion());
		graph.createEdge(SubLink.class, v10, v8);
		assertEquals(6, graph.getEdgeListVersion());
		graph.createEdge(SubLink.class, v11, v5);
		assertEquals(7, graph.getEdgeListVersion());
		graph.createEdge(SubLink.class, v11, v6);
		assertEquals(8, graph.getEdgeListVersion());
		graph.createEdge(SubLink.class, v11, v7);
		assertEquals(9, graph.getEdgeListVersion());
		//Edge e2 = 
		graph.createEdge(SubLink.class, v11, v8);
		assertEquals(10, graph.getEdgeListVersion());
		graph.createEdge(SubLink.class, v12, v5);
		assertEquals(11, graph.getEdgeListVersion());
		graph.createEdge(SubLink.class, v12, v6);
		assertEquals(12, graph.getEdgeListVersion());
		graph.createEdge(SubLink.class, v12, v7);
		assertEquals(13, graph.getEdgeListVersion());
		Edge e3 = graph.createEdge(SubLink.class, v12, v8);
		assertEquals(14, graph.getEdgeListVersion());
		graph.createEdge(SubLink.class, v9, v6);
		assertEquals(15, graph.getEdgeListVersion());
		graph.createEdge(SubLink.class, v9, v7);
		assertEquals(16, graph.getEdgeListVersion());
		graph.createEdge(SubLink.class, v9, v8);
		assertEquals(17, graph.getEdgeListVersion());

		graph.deleteEdge(e3);
		assertEquals(18, graph.getEdgeListVersion());
		//making sure that changing a vertex does not affect the edges
		graph.deleteVertex(v9);
		assertEquals(18, graph.getEdgeListVersion());
		
//		graph.deleteEdge(e2);
//		assertEquals(19, graph.getEdgeListVersion());
/*
 * TODO apparently one cannot delete ALL edges of the same type (here: SubLink) after deleting a vertex?
 */		
		graph.createEdge(Link.class, v1, v5);
		assertEquals(19, graph.getEdgeListVersion());
		graph.createEdge(Link.class, v2, v5);
		assertEquals(20, graph.getEdgeListVersion());
		graph.createEdge(Link.class, v3, v5);
		assertEquals(21, graph.getEdgeListVersion());
		graph.createEdge(Link.class, v4, v5);
		assertEquals(22, graph.getEdgeListVersion());
		//how can this work if I have already deleted v9?
		graph.createEdge(Link.class, v9, v5);
		assertEquals(23, graph.getEdgeListVersion());
		graph.createEdge(Link.class, v10, v5);
		assertEquals(24, graph.getEdgeListVersion());
		graph.createEdge(Link.class, v11, v5);
		assertEquals(25, graph.getEdgeListVersion());
		graph.createEdge(Link.class, v12, v5);
		assertEquals(26, graph.getEdgeListVersion());
		graph.createEdge(Link.class, v1, v6);
		assertEquals(27, graph.getEdgeListVersion());
		graph.createEdge(Link.class, v1, v7);
		assertEquals(28, graph.getEdgeListVersion());
		graph.createEdge(Link.class, v1, v8);
		assertEquals(29, graph.getEdgeListVersion());
		Edge e4 = graph.createEdge(Link.class, v3, v7);
		assertEquals(30, graph.getEdgeListVersion());
		graph.createEdge(Link.class, v11, v8);
		assertEquals(31, graph.getEdgeListVersion());
		
		graph.createEdge(LinkBack.class, v5, v1);
		assertEquals(32, graph.getEdgeListVersion());
		graph.createEdge(LinkBack.class, v6, v2);
		assertEquals(33, graph.getEdgeListVersion());
		Edge e5 = graph.createEdge(LinkBack.class, v7, v3);
		assertEquals(34, graph.getEdgeListVersion());
		graph.createEdge(LinkBack.class, v8, v4);
		assertEquals(35, graph.getEdgeListVersion());
		graph.createEdge(LinkBack.class, v8, v9);
		assertEquals(36, graph.getEdgeListVersion());
		graph.createEdge(LinkBack.class, v7, v10);
		assertEquals(37, graph.getEdgeListVersion());
		graph.createEdge(LinkBack.class, v6, v11);
		assertEquals(38, graph.getEdgeListVersion());
		Edge e6 = graph.createEdge(LinkBack.class, v5, v12);
		assertEquals(39, graph.getEdgeListVersion());

		graph.deleteEdge(e4);
		assertEquals(40, graph.getEdgeListVersion());
		graph.deleteEdge(e5);
		assertEquals(41, graph.getEdgeListVersion());
		graph.deleteEdge(e6);
		assertEquals(42, graph.getEdgeListVersion());
		
		System.out.println("Done testing getEdgeListVersion.");
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
		
		//TODO test what happens when vertex is deleted with the edges to which it belongs!
		/*
		e1 = graph.createLink(v2, v4);
		graph.deleteVertex(v2);
		assertFalse(graph.containsEdge(e1));
		*/
		
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
		//TODO continue

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
		LinkBack e9 = graph.createLinkBack(v5, v8);
//		SubLink e10 = graph2.createSubLink(v7, v6);
		
		graph.deleteEdge(e1);
		assertFalse(graph.containsEdge(e1));
		graph.deleteEdge(e2);
		assertFalse(graph.containsEdge(e2));
		graph.deleteEdge(e3);
		assertFalse(graph.containsEdge(e3));
		graph.deleteEdge(e9);
		assertFalse(graph.containsEdge(e9));
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
		
		//border cases
		
		//errors
		//cannot try to delete an edge which has never been created?
		//graph.deleteEdge(e10);
		
		System.out.println("Done testing deleteEdge.");
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
		graph.createSuperNode();
		
		assertEquals(v1, graph.getFirstVertex());
		
		DoubleSubNode v3 = graph2.createDoubleSubNode();
		graph2.createSubNode();
		assertEquals(v3, graph2.getFirstVertex());
		
		graph.createDoubleSubNode();
		assertEquals(v1, graph.getFirstVertex());
		
		System.out.println("Done testing getFirstVertex.");
	}
	
	@Test
	public void testGetLastVertex(){
		//border cases
		assertEquals(null, graph.getLastVertex());
		assertEquals(null, graph2.getLastVertex());
		
		Vertex v1 = graph.createVertex(SubNode.class);
		assertEquals(v1, graph.getLastVertex());
		
		//normal cases
		Vertex v2 = graph.createVertex(SubNode.class);
		assertEquals(v2, graph.getLastVertex());
		
		Vertex v3 = graph.createVertex(SubNode.class);
		assertEquals(v3, graph.getLastVertex());
		
		Vertex v4 = graph.createVertex(SubNode.class);
		assertEquals(v4, graph.getLastVertex());
		
		Vertex v5 = graph.createVertex(SuperNode.class);
		assertEquals(v5, graph.getLastVertex());
		
		Vertex v6 = graph.createVertex(SuperNode.class);
		assertEquals(v6, graph.getLastVertex());
		
		Vertex v7 = graph.createVertex(SuperNode.class);
		assertEquals(v7, graph.getLastVertex());
		
		Vertex v8 = graph.createVertex(SuperNode.class);
		assertEquals(v8, graph.getLastVertex());
		
		Vertex v9 = graph.createVertex(DoubleSubNode.class);
		assertEquals(v9, graph.getLastVertex());
		
		Vertex v10 = graph.createVertex(DoubleSubNode.class);
		assertEquals(v10, graph.getLastVertex());
		
		Vertex v11 = graph.createVertex(DoubleSubNode.class);
		assertEquals(v11, graph.getLastVertex());
		
		Vertex v12 = graph.createVertex(DoubleSubNode.class);
		assertEquals(v12, graph.getLastVertex());
	
		System.out.println("Done testing getLastVertex.");
	}
	
	@Test
	public void testGetFirstVertexOfClass(){
		assertEquals(null, graph.getFirstVertexOfClass(SubNode.class));
		assertEquals(null, graph.getFirstVertexOfClass(SuperNode.class));
		assertEquals(null, graph.getFirstVertexOfClass(DoubleSubNode.class));

		Vertex v1 = graph.createVertex(SubNode.class);
		assertEquals(v1, graph.getFirstVertexOfClass(SubNode.class));
		assertEquals(null, graph.getFirstVertexOfClass(SuperNode.class));
		assertEquals(null, graph.getFirstVertexOfClass(DoubleSubNode.class));

		Vertex v2 = graph.createVertex(SuperNode.class);
		assertEquals(v1, graph.getFirstVertexOfClass(SubNode.class));
		assertEquals(v2, graph.getFirstVertexOfClass(SuperNode.class));
		assertEquals(null, graph.getFirstVertexOfClass(DoubleSubNode.class));

		Vertex v3 = graph.createVertex(SubNode.class);
		assertEquals(v1, graph.getFirstVertexOfClass(SubNode.class));
		assertEquals(v2, graph.getFirstVertexOfClass(SuperNode.class));
		assertEquals(null, graph.getFirstVertexOfClass(DoubleSubNode.class));

		Vertex v4 = graph.createVertex(SubNode.class);
		assertEquals(v1, graph.getFirstVertexOfClass(SubNode.class));
		assertEquals(v2, graph.getFirstVertexOfClass(SuperNode.class));
		assertEquals(null, graph.getFirstVertexOfClass(DoubleSubNode.class));

		Vertex v5 = graph.createVertex(DoubleSubNode.class);
		assertEquals(v1, graph.getFirstVertexOfClass(SubNode.class));
		assertEquals(v2, graph.getFirstVertexOfClass(SuperNode.class));
		assertEquals(v5, graph.getFirstVertexOfClass(DoubleSubNode.class));

		Vertex v6 = graph.createVertex(SuperNode.class);
		assertEquals(v1, graph.getFirstVertexOfClass(SubNode.class));
		assertEquals(v2, graph.getFirstVertexOfClass(SuperNode.class));
		assertEquals(v5, graph.getFirstVertexOfClass(DoubleSubNode.class));

		Vertex v7 = graph.createVertex(SubNode.class);
		assertEquals(v1, graph.getFirstVertexOfClass(SubNode.class));
		assertEquals(v2, graph.getFirstVertexOfClass(SuperNode.class));
		assertEquals(v5, graph.getFirstVertexOfClass(DoubleSubNode.class));

		Vertex v8 = graph.createVertex(DoubleSubNode.class);
		assertEquals(v1, graph.getFirstVertexOfClass(SubNode.class));
		assertEquals(v2, graph.getFirstVertexOfClass(SuperNode.class));
		assertEquals(v5, graph.getFirstVertexOfClass(DoubleSubNode.class));

		graph.deleteVertex(v2);
		assertEquals(v1, graph.getFirstVertexOfClass(SubNode.class));
		assertEquals(v5, graph.getFirstVertexOfClass(SuperNode.class));
		assertEquals(v5, graph.getFirstVertexOfClass(DoubleSubNode.class));
		
		Vertex v9 = graph.createVertex(DoubleSubNode.class);
		assertEquals(v1, graph.getFirstVertexOfClass(SubNode.class));
		assertEquals(v5, graph.getFirstVertexOfClass(SuperNode.class));
		assertEquals(v5, graph.getFirstVertexOfClass(DoubleSubNode.class));

		graph.deleteVertex(v4);
		assertEquals(v1, graph.getFirstVertexOfClass(SubNode.class));
		assertEquals(v5, graph.getFirstVertexOfClass(SuperNode.class));
		assertEquals(v5, graph.getFirstVertexOfClass(DoubleSubNode.class));
		
		graph.createVertex(SuperNode.class);
		assertEquals(v1, graph.getFirstVertexOfClass(SubNode.class));
		assertEquals(v5, graph.getFirstVertexOfClass(SuperNode.class));
		assertEquals(v5, graph.getFirstVertexOfClass(DoubleSubNode.class));

		graph.createVertex(DoubleSubNode.class);
		assertEquals(v1, graph.getFirstVertexOfClass(SubNode.class));
		assertEquals(v5, graph.getFirstVertexOfClass(SuperNode.class));
		assertEquals(v5, graph.getFirstVertexOfClass(DoubleSubNode.class));
		
		graph.deleteVertex(v1);
		assertEquals(v3, graph.getFirstVertexOfClass(SubNode.class));
		assertEquals(v5, graph.getFirstVertexOfClass(SuperNode.class));
		assertEquals(v5, graph.getFirstVertexOfClass(DoubleSubNode.class));
		
		graph.deleteVertex(v5);
		assertEquals(v3, graph.getFirstVertexOfClass(SubNode.class));
		assertEquals(v6, graph.getFirstVertexOfClass(SuperNode.class));
		assertEquals(v8, graph.getFirstVertexOfClass(DoubleSubNode.class));
		
		graph.deleteVertex(v8);
		assertEquals(v3, graph.getFirstVertexOfClass(SubNode.class));
		assertEquals(v6, graph.getFirstVertexOfClass(SuperNode.class));
		assertEquals(v9, graph.getFirstVertexOfClass(DoubleSubNode.class));
		
		graph.createVertex(SuperNode.class);
		assertEquals(v3, graph.getFirstVertexOfClass(SubNode.class));
		assertEquals(v6, graph.getFirstVertexOfClass(SuperNode.class));
		assertEquals(v9, graph.getFirstVertexOfClass(DoubleSubNode.class));
		
		graph.deleteVertex(v3);
		assertEquals(v7, graph.getFirstVertexOfClass(SubNode.class));
		assertEquals(v6, graph.getFirstVertexOfClass(SuperNode.class));
		assertEquals(v9, graph.getFirstVertexOfClass(DoubleSubNode.class));
		
		System.out.println("Done testing getFirstVertexOfClass.");
	}
	
	@Test
	public void testGetFirstVertexOfClass2(){
		assertEquals(null, graph.getFirstVertexOfClass(SubNode.class, true));
		assertEquals(null, graph.getFirstVertexOfClass(SubNode.class, false));
		assertEquals(null, graph.getFirstVertexOfClass(SuperNode.class, true));
		assertEquals(null, graph.getFirstVertexOfClass(SuperNode.class, false));
		assertEquals(null, graph.getFirstVertexOfClass(DoubleSubNode.class, true));
		assertEquals(null, graph.getFirstVertexOfClass(DoubleSubNode.class, false));
		
		Vertex v1 = graph.createVertex(SubNode.class);
		assertEquals(v1, graph.getFirstVertexOfClass(SubNode.class, true));
		assertEquals(v1, graph.getFirstVertexOfClass(SubNode.class, false));
		assertEquals(null, graph.getFirstVertexOfClass(SuperNode.class, true));
		assertEquals(null, graph.getFirstVertexOfClass(SuperNode.class, false));
		assertEquals(null, graph.getFirstVertexOfClass(DoubleSubNode.class, true));
		assertEquals(null, graph.getFirstVertexOfClass(DoubleSubNode.class, false));
		
		Vertex v2 = graph.createVertex(SuperNode.class);
		assertEquals(v1, graph.getFirstVertexOfClass(SubNode.class, true));
		assertEquals(v1, graph.getFirstVertexOfClass(SubNode.class, false));
		assertEquals(v2, graph.getFirstVertexOfClass(SuperNode.class, true));
		assertEquals(v2, graph.getFirstVertexOfClass(SuperNode.class, false));
		assertEquals(null, graph.getFirstVertexOfClass(DoubleSubNode.class, true));
		assertEquals(null, graph.getFirstVertexOfClass(DoubleSubNode.class, false));
		
		Vertex v3 = graph.createVertex(DoubleSubNode.class);
		assertEquals(v1, graph.getFirstVertexOfClass(SubNode.class, true));
		assertEquals(v1, graph.getFirstVertexOfClass(SubNode.class, false));
		assertEquals(v2, graph.getFirstVertexOfClass(SuperNode.class, true));
		assertEquals(v2, graph.getFirstVertexOfClass(SuperNode.class, false));
		assertEquals(v3, graph.getFirstVertexOfClass(DoubleSubNode.class, true));
		assertEquals(v3, graph.getFirstVertexOfClass(DoubleSubNode.class, false));
		
		graph.deleteVertex(v1);
		assertEquals(null, graph.getFirstVertexOfClass(SubNode.class, true));
		assertEquals(v3, graph.getFirstVertexOfClass(SubNode.class, false));
		assertEquals(v2, graph.getFirstVertexOfClass(SuperNode.class, true));
		assertEquals(v2, graph.getFirstVertexOfClass(SuperNode.class, false));
		assertEquals(v3, graph.getFirstVertexOfClass(DoubleSubNode.class, true));
		assertEquals(v3, graph.getFirstVertexOfClass(DoubleSubNode.class, false));
		
		Vertex v4 = graph.createVertex(SubNode.class);
		assertEquals(v4, graph.getFirstVertexOfClass(SubNode.class, true));
		assertEquals(v3, graph.getFirstVertexOfClass(SubNode.class, false));
		assertEquals(v2, graph.getFirstVertexOfClass(SuperNode.class, true));
		assertEquals(v2, graph.getFirstVertexOfClass(SuperNode.class, false));
		assertEquals(v3, graph.getFirstVertexOfClass(DoubleSubNode.class, true));
		assertEquals(v3, graph.getFirstVertexOfClass(DoubleSubNode.class, false));
		
		Vertex v5 = graph.createVertex(SubNode.class);
		assertEquals(v4, graph.getFirstVertexOfClass(SubNode.class, true));
		assertEquals(v3, graph.getFirstVertexOfClass(SubNode.class, false));
		assertEquals(v2, graph.getFirstVertexOfClass(SuperNode.class, true));
		assertEquals(v2, graph.getFirstVertexOfClass(SuperNode.class, false));
		assertEquals(v3, graph.getFirstVertexOfClass(DoubleSubNode.class, true));
		assertEquals(v3, graph.getFirstVertexOfClass(DoubleSubNode.class, false));
		
		graph.deleteVertex(v4);
		assertEquals(v5, graph.getFirstVertexOfClass(SubNode.class, true));
		assertEquals(v3, graph.getFirstVertexOfClass(SubNode.class, false));
		assertEquals(v2, graph.getFirstVertexOfClass(SuperNode.class, true));
		assertEquals(v2, graph.getFirstVertexOfClass(SuperNode.class, false));
		assertEquals(v3, graph.getFirstVertexOfClass(DoubleSubNode.class, true));
		assertEquals(v3, graph.getFirstVertexOfClass(DoubleSubNode.class, false));
		
		Vertex v6 = graph.createVertex(DoubleSubNode.class);
		assertEquals(v5, graph.getFirstVertexOfClass(SubNode.class, true));
		assertEquals(v3, graph.getFirstVertexOfClass(SubNode.class, false));
		assertEquals(v2, graph.getFirstVertexOfClass(SuperNode.class, true));
		assertEquals(v2, graph.getFirstVertexOfClass(SuperNode.class, false));
		assertEquals(v3, graph.getFirstVertexOfClass(DoubleSubNode.class, true));
		assertEquals(v3, graph.getFirstVertexOfClass(DoubleSubNode.class, false));
		
		graph.deleteVertex(v3);
		assertEquals(v5, graph.getFirstVertexOfClass(SubNode.class, true));
		assertEquals(v5, graph.getFirstVertexOfClass(SubNode.class, false));
		assertEquals(v2, graph.getFirstVertexOfClass(SuperNode.class, true));
		assertEquals(v2, graph.getFirstVertexOfClass(SuperNode.class, false));
		assertEquals(v6, graph.getFirstVertexOfClass(DoubleSubNode.class, true));
		assertEquals(v6, graph.getFirstVertexOfClass(DoubleSubNode.class, false));
		
		Vertex v7 = graph.createVertex(SubNode.class);
		assertEquals(v5, graph.getFirstVertexOfClass(SubNode.class, true));
		assertEquals(v5, graph.getFirstVertexOfClass(SubNode.class, false));
		assertEquals(v2, graph.getFirstVertexOfClass(SuperNode.class, true));
		assertEquals(v2, graph.getFirstVertexOfClass(SuperNode.class, false));
		assertEquals(v6, graph.getFirstVertexOfClass(DoubleSubNode.class, true));
		assertEquals(v6, graph.getFirstVertexOfClass(DoubleSubNode.class, false));
		
		graph.deleteVertex(v5);
		assertEquals(v7, graph.getFirstVertexOfClass(SubNode.class, true));
		assertEquals(v6, graph.getFirstVertexOfClass(SubNode.class, false));
		assertEquals(v2, graph.getFirstVertexOfClass(SuperNode.class, true));
		assertEquals(v2, graph.getFirstVertexOfClass(SuperNode.class, false));
		assertEquals(v6, graph.getFirstVertexOfClass(DoubleSubNode.class, true));
		assertEquals(v6, graph.getFirstVertexOfClass(DoubleSubNode.class, false));
		
		Vertex v8 = graph.createVertex(DoubleSubNode.class);
		assertEquals(v7, graph.getFirstVertexOfClass(SubNode.class, true));
		assertEquals(v6, graph.getFirstVertexOfClass(SubNode.class, false));
		assertEquals(v2, graph.getFirstVertexOfClass(SuperNode.class, true));
		assertEquals(v2, graph.getFirstVertexOfClass(SuperNode.class, false));
		assertEquals(v6, graph.getFirstVertexOfClass(DoubleSubNode.class, true));
		assertEquals(v6, graph.getFirstVertexOfClass(DoubleSubNode.class, false));
		
		graph.deleteVertex(v6);
		assertEquals(v7, graph.getFirstVertexOfClass(SubNode.class, true));
		assertEquals(v7, graph.getFirstVertexOfClass(SubNode.class, false));
		assertEquals(v2, graph.getFirstVertexOfClass(SuperNode.class, true));
		assertEquals(v2, graph.getFirstVertexOfClass(SuperNode.class, false));
		assertEquals(v8, graph.getFirstVertexOfClass(DoubleSubNode.class, true));
		assertEquals(v8, graph.getFirstVertexOfClass(DoubleSubNode.class, false));
		
		Vertex v9 = graph.createVertex(SuperNode.class);
		assertEquals(v7, graph.getFirstVertexOfClass(SubNode.class, true));
		assertEquals(v7, graph.getFirstVertexOfClass(SubNode.class, false));
		assertEquals(v2, graph.getFirstVertexOfClass(SuperNode.class, true));
		assertEquals(v2, graph.getFirstVertexOfClass(SuperNode.class, false));
		assertEquals(v8, graph.getFirstVertexOfClass(DoubleSubNode.class, true));
		assertEquals(v8, graph.getFirstVertexOfClass(DoubleSubNode.class, false));
		
		graph.deleteVertex(v2);
		assertEquals(v7, graph.getFirstVertexOfClass(SubNode.class, true));
		assertEquals(v7, graph.getFirstVertexOfClass(SubNode.class, false));
		assertEquals(v9, graph.getFirstVertexOfClass(SuperNode.class, true));
		assertEquals(v8, graph.getFirstVertexOfClass(SuperNode.class, false));//???????
		assertEquals(v8, graph.getFirstVertexOfClass(DoubleSubNode.class, true));
		assertEquals(v8, graph.getFirstVertexOfClass(DoubleSubNode.class, false));
		
		graph.deleteVertex(v7);
		assertEquals(null, graph.getFirstVertexOfClass(SubNode.class, true));
		assertEquals(v8, graph.getFirstVertexOfClass(SubNode.class, false));
		assertEquals(v9, graph.getFirstVertexOfClass(SuperNode.class, true));
		assertEquals(v8, graph.getFirstVertexOfClass(SuperNode.class, false));
		assertEquals(v8, graph.getFirstVertexOfClass(DoubleSubNode.class, true));
		assertEquals(v8, graph.getFirstVertexOfClass(DoubleSubNode.class, false));
		
		Vertex v10 = graph.createVertex(SubNode.class);
		assertEquals(v10, graph.getFirstVertexOfClass(SubNode.class, true));
		assertEquals(v8, graph.getFirstVertexOfClass(SubNode.class, false));
		assertEquals(v9, graph.getFirstVertexOfClass(SuperNode.class, true));
		assertEquals(v8, graph.getFirstVertexOfClass(SuperNode.class, false));
		assertEquals(v8, graph.getFirstVertexOfClass(DoubleSubNode.class, true));
		assertEquals(v8, graph.getFirstVertexOfClass(DoubleSubNode.class, false));
		
		graph.deleteVertex(v8);
		assertEquals(v10, graph.getFirstVertexOfClass(SubNode.class, true));
		assertEquals(v10, graph.getFirstVertexOfClass(SubNode.class, false));
		assertEquals(v9, graph.getFirstVertexOfClass(SuperNode.class, true));
		assertEquals(v9, graph.getFirstVertexOfClass(SuperNode.class, false));
		assertEquals(null, graph.getFirstVertexOfClass(DoubleSubNode.class, true));
		assertEquals(null, graph.getFirstVertexOfClass(DoubleSubNode.class, false));
		
		Vertex v11 = graph.createVertex(DoubleSubNode.class);
		assertEquals(v10, graph.getFirstVertexOfClass(SubNode.class, true));
		assertEquals(v10, graph.getFirstVertexOfClass(SubNode.class, false));
		assertEquals(v9, graph.getFirstVertexOfClass(SuperNode.class, true));
		assertEquals(v9, graph.getFirstVertexOfClass(SuperNode.class, false));
		assertEquals(v11, graph.getFirstVertexOfClass(DoubleSubNode.class, true));
		assertEquals(v11, graph.getFirstVertexOfClass(DoubleSubNode.class, false));
		
		System.out.println("Done testing getFirstVertexOfClass2.");
	}
	
	@Test
	public void testGetFirstVertexOfClass3(){
//		assertEquals(null, graph.getFirstVertexOfClass());
		SuperNode v1 = graph.createSuperNode();
		SubNode v2 = graph.createSubNode();
		SuperNode v3 = graph.createSuperNode();
		DoubleSubNode v4 = graph.createDoubleSubNode();
		DoubleSubNode v5 = graph.createDoubleSubNode();
		SubNode v6 = graph.createSubNode();
		DoubleSubNode v7 = graph.createDoubleSubNode();
		SubNode v8 = graph.createSubNode();
		SuperNode v9 = graph.createSuperNode();
		SubNode v10 = graph.createSubNode();
	}
	
	@Test
	public void testGetFirstVertexOfClass4(){
		Vertex v1 = graph.createVertex(SubNode.class);
		Vertex v2 = graph.createVertex(SuperNode.class);
		Vertex v3 = graph.createVertex(DoubleSubNode.class);
		Vertex v4 = graph.createVertex(SubNode.class);
		Vertex v5 = graph.createVertex(SubNode.class);
		Vertex v6 = graph.createVertex(DoubleSubNode.class);
		Vertex v7 = graph.createVertex(SubNode.class);
		Vertex v8 = graph.createVertex(DoubleSubNode.class);
		Vertex v9 = graph.createVertex(SuperNode.class);
		Vertex v10 = graph.createVertex(SubNode.class);
		Vertex v11 = graph.createVertex(DoubleSubNode.class);
	}
	
	@Test
	public void testGetFirstEdgeInGraph(){
		Vertex v1 = graph.createVertex(SubNode.class);
		Vertex v2 = graph.createVertex(SubNode.class);
		Vertex v3 = graph.createVertex(SubNode.class);
		Vertex v4 = graph.createVertex(SuperNode.class);
		Vertex v5 = graph.createVertex(SuperNode.class);
		Vertex v6 = graph.createVertex(SuperNode.class);
		Vertex v7 = graph.createVertex(SuperNode.class);
		Vertex v8 = graph.createVertex(SuperNode.class);
		Vertex v9 = graph.createVertex(DoubleSubNode.class);
		Vertex v10 = graph.createVertex(DoubleSubNode.class);
		Vertex v11 = graph.createVertex(DoubleSubNode.class);
		
		assertEquals(null, graph.getFirstEdgeInGraph());

		Edge e1 = graph.createEdge(Link.class, v3, v6);
		assertEquals(e1, graph.getFirstEdgeInGraph());
		
		Edge e2 = graph.createEdge(Link.class, v3, v4);
		assertEquals(e1, graph.getFirstEdgeInGraph());
		
		Edge e3 = graph.createEdge(LinkBack.class, v7, v11);
		assertEquals(e1, graph.getFirstEdgeInGraph());
		
		graph.deleteEdge(e1);
		assertEquals(e2, graph.getFirstEdgeInGraph());
		
		graph.deleteEdge(e2);
		assertEquals(e3, graph.getFirstEdgeInGraph());
		
		Edge e4 = graph.createEdge(SubLink.class, v10, v8);
		assertEquals(e3, graph.getFirstEdgeInGraph());
		
		graph.deleteEdge(e3);
		assertEquals(e4, graph.getFirstEdgeInGraph());
		
		graph.createEdge(LinkBack.class, v8, v3);
		assertEquals(e4, graph.getFirstEdgeInGraph());
		
		System.out.println("Done testing getFirstEdgeInGraph.");
	}
	
	@Test
	public void testGetLastEdgeInGraph(){
		Vertex v1 = graph.createVertex(SubNode.class);
		Vertex v2 = graph.createVertex(SubNode.class);
		Vertex v3 = graph.createVertex(SubNode.class);
		Vertex v4 = graph.createVertex(SuperNode.class);
		Vertex v5 = graph.createVertex(SuperNode.class);
		Vertex v6 = graph.createVertex(SuperNode.class);
		Vertex v7 = graph.createVertex(SuperNode.class);
		Vertex v8 = graph.createVertex(SuperNode.class);
		Vertex v9 = graph.createVertex(DoubleSubNode.class);
		Vertex v10 = graph.createVertex(DoubleSubNode.class);
		Vertex v11 = graph.createVertex(DoubleSubNode.class);

		assertEquals(null, graph.getLastEdgeInGraph());
		
		Edge e1 = graph.createEdge(Link.class, v3, v6);
		assertEquals(e1, graph.getLastEdgeInGraph());
		
		Edge e2 = graph.createEdge(Link.class, v3, v4);
		assertEquals(e2, graph.getLastEdgeInGraph());
		
		Edge e3 = graph.createEdge(LinkBack.class, v7, v11);
		assertEquals(e3, graph.getLastEdgeInGraph());
		
		graph.deleteEdge(e3);
		assertEquals(e2, graph.getLastEdgeInGraph());
		
		Edge e4 = graph.createEdge(SubLink.class, v10, v8);
		assertEquals(e4, graph.getLastEdgeInGraph());
		
		Edge e5 = graph.createEdge(LinkBack.class, v8, v3);
		assertEquals(e5, graph.getLastEdgeInGraph());
		
		Edge e6 = graph.createEdge(Link.class, v9, v5);
		assertEquals(e6, graph.getLastEdgeInGraph());
		
		Edge e7 = graph.createEdge(SubLink.class, v11, v7);
		assertEquals(e7, graph.getLastEdgeInGraph());
		
		graph.deleteEdge(e7);
		assertEquals(e6, graph.getLastEdgeInGraph());
		
		graph.deleteEdge(e6);
		assertEquals(e5, graph.getLastEdgeInGraph());
		
		System.out.println("Done testing getLastEdgeInGraph.");
	}
	
	@Test
	public void testGetFirstEdgeOfClassInGraph(){
		Vertex v1 = graph.createVertex(SubNode.class);
		Vertex v2 = graph.createVertex(SubNode.class);
		Vertex v3 = graph.createVertex(SubNode.class);
		Vertex v4 = graph.createVertex(SuperNode.class);
		Vertex v5 = graph.createVertex(SuperNode.class);
		Vertex v6 = graph.createVertex(SuperNode.class);
		Vertex v7 = graph.createVertex(SuperNode.class);
		Vertex v8 = graph.createVertex(SuperNode.class);
		Vertex v9 = graph.createVertex(DoubleSubNode.class);
		Vertex v10 = graph.createVertex(DoubleSubNode.class);
		Vertex v11 = graph.createVertex(DoubleSubNode.class);

		assertEquals(null, graph.getFirstEdgeOfClassInGraph(Link.class));
		assertEquals(null, graph.getFirstEdgeOfClassInGraph(SubLink.class));
		assertEquals(null, graph.getFirstEdgeOfClassInGraph(LinkBack.class));
		
		Edge e1 = graph.createEdge(Link.class, v3, v6);
		assertEquals(e1, graph.getFirstEdgeOfClassInGraph(Link.class));
		assertEquals(null, graph.getFirstEdgeOfClassInGraph(SubLink.class));
		assertEquals(null, graph.getFirstEdgeOfClassInGraph(LinkBack.class));
		
		Edge e2 = graph.createEdge(Link.class, v3, v4);
		assertEquals(e1, graph.getFirstEdgeOfClassInGraph(Link.class));
		assertEquals(null, graph.getFirstEdgeOfClassInGraph(SubLink.class));
		assertEquals(null, graph.getFirstEdgeOfClassInGraph(LinkBack.class));
		
		Edge e3 = graph.createEdge(LinkBack.class, v7, v11);
		assertEquals(e1, graph.getFirstEdgeOfClassInGraph(Link.class));
		assertEquals(null, graph.getFirstEdgeOfClassInGraph(SubLink.class));
		assertEquals(e3, graph.getFirstEdgeOfClassInGraph(LinkBack.class));
		
		graph.deleteEdge(e1);
		assertEquals(e2, graph.getFirstEdgeOfClassInGraph(Link.class));
		assertEquals(null, graph.getFirstEdgeOfClassInGraph(SubLink.class));
		assertEquals(e3, graph.getFirstEdgeOfClassInGraph(LinkBack.class));
		
		Edge e4 = graph.createEdge(SubLink.class, v10, v8);
		assertEquals(e2, graph.getFirstEdgeOfClassInGraph(Link.class));
		assertEquals(e4, graph.getFirstEdgeOfClassInGraph(SubLink.class));
		assertEquals(e3, graph.getFirstEdgeOfClassInGraph(LinkBack.class));
		
		Edge e5 = graph.createEdge(LinkBack.class, v8, v3);
		assertEquals(e2, graph.getFirstEdgeOfClassInGraph(Link.class));
		assertEquals(e4, graph.getFirstEdgeOfClassInGraph(SubLink.class));
		assertEquals(e3, graph.getFirstEdgeOfClassInGraph(LinkBack.class));
		
		graph.deleteEdge(e3);
		assertEquals(e2, graph.getFirstEdgeOfClassInGraph(Link.class));
		assertEquals(e4, graph.getFirstEdgeOfClassInGraph(SubLink.class));
		assertEquals(e5, graph.getFirstEdgeOfClassInGraph(LinkBack.class));
		
		Edge e6 = graph.createEdge(Link.class, v9, v5);
		assertEquals(e2, graph.getFirstEdgeOfClassInGraph(Link.class));
		assertEquals(e4, graph.getFirstEdgeOfClassInGraph(SubLink.class));
		assertEquals(e5, graph.getFirstEdgeOfClassInGraph(LinkBack.class));
		
		graph.deleteEdge(e2);
		assertEquals(e4, graph.getFirstEdgeOfClassInGraph(Link.class));
		assertEquals(e4, graph.getFirstEdgeOfClassInGraph(SubLink.class));
		assertEquals(e5, graph.getFirstEdgeOfClassInGraph(LinkBack.class));
		
		Edge e7 = graph.createEdge(SubLink.class, v11, v7);
		assertEquals(e4, graph.getFirstEdgeOfClassInGraph(Link.class));
		assertEquals(e4, graph.getFirstEdgeOfClassInGraph(SubLink.class));
		assertEquals(e5, graph.getFirstEdgeOfClassInGraph(LinkBack.class));
		
		graph.deleteEdge(e4);
		assertEquals(e6, graph.getFirstEdgeOfClassInGraph(Link.class));
		assertEquals(e7, graph.getFirstEdgeOfClassInGraph(SubLink.class));
		assertEquals(e5, graph.getFirstEdgeOfClassInGraph(LinkBack.class));
		
		Edge e8 = graph.createEdge(SubLink.class, v10, v4);
		assertEquals(e6, graph.getFirstEdgeOfClassInGraph(Link.class));
		assertEquals(e7, graph.getFirstEdgeOfClassInGraph(SubLink.class));
		assertEquals(e5, graph.getFirstEdgeOfClassInGraph(LinkBack.class));
		
		Edge e9 = graph.createEdge(LinkBack.class, v6, v1);
		assertEquals(e6, graph.getFirstEdgeOfClassInGraph(Link.class));
		assertEquals(e7, graph.getFirstEdgeOfClassInGraph(SubLink.class));
		assertEquals(e5, graph.getFirstEdgeOfClassInGraph(LinkBack.class));
		
		graph.deleteEdge(e5);
		assertEquals(e6, graph.getFirstEdgeOfClassInGraph(Link.class));
		assertEquals(e7, graph.getFirstEdgeOfClassInGraph(SubLink.class));
		assertEquals(e9, graph.getFirstEdgeOfClassInGraph(LinkBack.class));
		
		graph.deleteEdge(e7);
		assertEquals(e6, graph.getFirstEdgeOfClassInGraph(Link.class));
		assertEquals(e8, graph.getFirstEdgeOfClassInGraph(SubLink.class));
		assertEquals(e9, graph.getFirstEdgeOfClassInGraph(LinkBack.class));
		
		System.out.println("Done testing getFirstEdgeOfClassInGraph.");
	}
	
	@Test
	public void testGetFirstEdgeOfClassInGraph2(){
		Vertex v1 = graph.createVertex(SubNode.class);
		Vertex v2 = graph.createVertex(SubNode.class);
		Vertex v3 = graph.createVertex(SubNode.class);
		Vertex v4 = graph.createVertex(SuperNode.class);
		Vertex v5 = graph.createVertex(SuperNode.class);
		Vertex v6 = graph.createVertex(SuperNode.class);
		Vertex v7 = graph.createVertex(SuperNode.class);
		Vertex v8 = graph.createVertex(SuperNode.class);
		Vertex v9 = graph.createVertex(DoubleSubNode.class);
		Vertex v10 = graph.createVertex(DoubleSubNode.class);
		Vertex v11 = graph.createVertex(DoubleSubNode.class);

		Edge e1 = graph.createEdge(Link.class, v3, v6);
		Edge e2 = graph.createEdge(Link.class, v3, v4);
		Edge e3 = graph.createEdge(LinkBack.class, v7, v11);
		Edge e4 = graph.createEdge(SubLink.class, v10, v8);
		Edge e5 = graph.createEdge(LinkBack.class, v8, v3);
		Edge e6 = graph.createEdge(Link.class, v9, v5);
		Edge e7 = graph.createEdge(SubLink.class, v11, v7);
		Edge e8 = graph.createEdge(SubLink.class, v10, v4);
		Edge e9 = graph.createEdge(LinkBack.class, v6, v1);
		Edge e10 = graph.createEdge(LinkBack.class, v7, v2);
	}
	
	@Test
	public void testGetFirstEdgeOfClassInGraph3(){
		
	}
	
	@Test
	public void testGetFirstEdgeOfClassInGraph4(){
		
	}
	
	@Test
	public void testGetVertex(){
		Vertex v1 = graph.createVertex(SubNode.class);
		Vertex v2 = graph.createVertex(SubNode.class);
		Vertex v3 = graph.createVertex(SubNode.class);
		Vertex v4 = graph.createVertex(SubNode.class);
		Vertex v5 = graph.createVertex(SuperNode.class);
		Vertex v6 = graph.createVertex(SuperNode.class);
		Vertex v7 = graph.createVertex(SuperNode.class);
		Vertex v8 = graph.createVertex(SuperNode.class);
		Vertex v9 = graph.createVertex(DoubleSubNode.class);
		Vertex v10 = graph.createVertex(DoubleSubNode.class);
		Vertex v11 = graph.createVertex(DoubleSubNode.class);
		Vertex v12 = graph.createVertex(DoubleSubNode.class);
		Vertex v13 = graph.createVertex(SubNode.class);
		Vertex v14 = graph.createVertex(DoubleSubNode.class);
		Vertex v15 = graph.createVertex(SuperNode.class);
		Vertex v16 = graph2.createVertex(SubNode.class);
		Vertex v17 = graph2.createVertex(SuperNode.class);
		Vertex v18 = graph2.createVertex(DoubleSubNode.class);
		
		//faults
		//TODO these cases are not caught yet
//		assertEquals(null, graph.getVertex(-5));
		//values higher than 1000
		
		//border cases
		assertEquals(v1, graph.getVertex(1));
		assertEquals(v16, graph2.getVertex(1));
		assertEquals(null, graph.getVertex(0));
		assertEquals(null, graph.getVertex(42));
		assertEquals(null, graph.getVertex(33));
		assertEquals(null, graph2.getVertex(4));
		//1000 is the highest possible value
		assertEquals(null, graph.getVertex(1000));

		//normal cases
		assertEquals(v2, graph.getVertex(2));
		assertEquals(v3, graph.getVertex(3));
		assertEquals(v4, graph.getVertex(4));
		assertEquals(v5, graph.getVertex(5));
		assertEquals(v6, graph.getVertex(6));
		assertEquals(v7, graph.getVertex(7));
		assertEquals(v8, graph.getVertex(8));
		assertEquals(v9, graph.getVertex(9));
		assertEquals(v10, graph.getVertex(10));
		assertEquals(v11, graph.getVertex(11));
		assertEquals(v12, graph.getVertex(12));
		assertEquals(v13, graph.getVertex(13));
		assertEquals(v14, graph.getVertex(14));
		assertEquals(v15, graph.getVertex(15));
		assertEquals(v17, graph2.getVertex(2));
		assertEquals(v18, graph2.getVertex(3));
		
		System.out.println("Done testing getVertex.");		
	}
	
	@Test
	public void testGetEdge(){
		//TODO not caught yet: values higher than 1000 and less than 1
		Vertex v1 = graph.createVertex(SubNode.class);
		Vertex v2 = graph.createVertex(SubNode.class);
		Vertex v3 = graph.createVertex(SubNode.class);
		Vertex v4 = graph.createVertex(SubNode.class);
		Vertex v5 = graph.createVertex(SuperNode.class);
		Vertex v6 = graph.createVertex(SuperNode.class);
		Vertex v7 = graph.createVertex(SuperNode.class);
		Vertex v8 = graph.createVertex(SuperNode.class);
		Vertex v9 = graph.createVertex(DoubleSubNode.class);
		Vertex v10 = graph.createVertex(DoubleSubNode.class);
		Vertex v11 = graph.createVertex(DoubleSubNode.class);
		Vertex v12 = graph.createVertex(DoubleSubNode.class);
		
		Edge e1 = graph.createEdge(LinkBack.class, v5, v1);
		Edge e2 = graph.createEdge(Link.class, v2, v7);
		Edge e3 = graph.createEdge(LinkBack.class, v8, v4);
		Edge e4 = graph.createEdge(SubLink.class, v11, v6);
		Edge e5 = graph.createEdge(Link.class, v2, v5);
		Edge e6 = graph.createEdge(LinkBack.class, v7, v12);
		Edge e7 = graph.createEdge(SubLink.class, v9, v8);
		Edge e8 = graph.createEdge(SubLink.class, v10, v6);
		Edge e9 = graph.createEdge(Link.class, v3, v7);
		Edge e10 = graph.createEdge(Link.class, v3, v7);
		
		//border cases
		assertEquals(null, graph.getEdge(0));
		assertEquals(null, graph.getEdge(42));
		assertEquals(null, graph.getEdge(-42));
		assertEquals(e1, graph.getEdge(1));
		assertEquals(null, graph.getEdge(1000));
		assertEquals(null, graph.getEdge(-1000));
		
		//normal cases
		assertEquals(e2, graph.getEdge(2));
		assertEquals(e3, graph.getEdge(3));
		assertEquals(e4, graph.getEdge(4));
		assertEquals(e5, graph.getEdge(5));
		assertEquals(e6, graph.getEdge(6));
		assertEquals(e7, graph.getEdge(7));
		assertEquals(e8, graph.getEdge(8));
		assertEquals(e9, graph.getEdge(9));
		assertEquals(e10, graph.getEdge(10));
		
		System.out.println("Done testing getEdge.");
	}
	
	@Test
	public void testGetMaxVCount(){
		assertEquals(1000, graph.getMaxVCount());
		assertEquals(1000, graph2.getMaxVCount());
		MinimalGraph graph3 = MinimalSchema.instance().createMinimalGraph();
		assertEquals(1000, graph3.getMaxVCount());
		
		System.out.println("Done testing getMaxVCount.");
	}
	
	@Test
	public void testGetExpandedVertexCount(){
		//border case
		assertEquals(2000, graph.getExpandedVertexCount());
		
		//normal cases
		for(int i=0; i<1000; i++){
			graph.createVertex(SubNode.class);
		}
		assertEquals(2000, graph.getExpandedVertexCount());
		for(int i=0; i<1000; i++){
			graph.createVertex(SuperNode.class);
		}
		assertEquals(4000, graph.getExpandedVertexCount());
		for(int i=0; i<1000; i++){
			graph.createVertex(DoubleSubNode.class);
		}
		assertEquals(8000, graph.getExpandedVertexCount());
		System.out.println("Done testing getExpandedVertexCount.");
	}
	
	@Test
	public void testGetExpandedEdgeCount(){
		Vertex v1 = graph.createVertex(SubNode.class);
		Vertex v2 = graph.createVertex(SuperNode.class);
		Vertex v3 = graph.createVertex(DoubleSubNode.class);

		//border case
		assertEquals(2000, graph.getExpandedEdgeCount());
		
		//normal cases
		for(int i=0; i<1000; i++){
			graph.createEdge(SubLink.class, v3, v2);
		}
		assertEquals(2000, graph.getExpandedEdgeCount());
		

		for(int i=0; i<1000; i++){
			graph.createEdge(Link.class, v1, v2);
		}
		assertEquals(4000, graph.getExpandedEdgeCount());
		

		for(int i=0; i<1000; i++){
			graph.createEdge(LinkBack.class, v2, v3);
		}
		assertEquals(8000, graph.getExpandedEdgeCount());
		
		System.out.println("Done testing getExpandedEdgeCount.");
	}
	
	@Test
	public void testGetMaxECount(){
		assertEquals(1000, graph.getMaxECount());
		assertEquals(1000, graph2.getMaxECount());
		MinimalGraph graph3 = MinimalSchema.instance().createMinimalGraph();
		assertEquals(1000, graph3.getMaxECount());
		
		System.out.println("Done testing getMaxECount.");
	}
	
	@Test
	public void testGetVCount(){
		//border cases
		assertEquals(0, graph.getVCount());
		assertEquals(0, graph2.getVCount());
		
		Vertex v1 = graph.createVertex(SubNode.class);
		assertEquals(1, graph.getVCount());
		
		graph.deleteVertex(v1);
		assertEquals(0, graph.getVCount());
		
		graph.createVertex(SubNode.class);
		assertEquals(1, graph.getVCount());
		
		//normal cases
		Vertex v2 = graph.createVertex(SubNode.class);
		assertEquals(2, graph.getVCount());
		
		graph.createVertex(SubNode.class);
		assertEquals(3, graph.getVCount());
		
		graph.deleteVertex(v2);
		assertEquals(2, graph.getVCount());
		
		graph.createVertex(SuperNode.class);
		assertEquals(3, graph.getVCount());
		
		Vertex v3 = graph.createVertex(SuperNode.class);
		assertEquals(4, graph.getVCount());
		
		graph.deleteVertex(v3);
		assertEquals(3, graph.getVCount());
		
		Vertex v4 = graph.createVertex(SuperNode.class);
		assertEquals(4, graph.getVCount());
		
		graph.createVertex(SuperNode.class);
		assertEquals(5, graph.getVCount());
		
		graph.createVertex(DoubleSubNode.class);
		assertEquals(6, graph.getVCount());
		
		graph.createVertex(DoubleSubNode.class);
		assertEquals(7, graph.getVCount());
		
		graph.deleteVertex(v4);
		assertEquals(6, graph.getVCount());
		
		graph.createVertex(DoubleSubNode.class);
		assertEquals(7, graph.getVCount());
		
		graph.createVertex(DoubleSubNode.class);
		assertEquals(8, graph.getVCount());
		
		for(int i=9; i<20; i++){
			graph.createVertex(SuperNode.class);
			assertEquals(i, graph.getVCount());
		}
		
		for(int i=20; i<32; i++){
			graph.createVertex(DoubleSubNode.class);
			assertEquals(i, graph.getVCount());
		}
		
		for(int i=32; i<42; i++){
			graph.createVertex(SubNode.class);
			assertEquals(i, graph.getVCount());
		}
		
		System.out.println("Done testing getVCount.");
	}
	
	@Test
	public void testGetECount(){

		Vertex v1 = graph.createVertex(SubNode.class);
		Vertex v2 = graph.createVertex(SubNode.class);
		Vertex v3 = graph.createVertex(SubNode.class);
		Vertex v4 = graph.createVertex(SubNode.class);
		Vertex v5 = graph.createVertex(SuperNode.class);
		Vertex v6 = graph.createVertex(SuperNode.class);
		Vertex v7 = graph.createVertex(SuperNode.class);
		Vertex v8 = graph.createVertex(SuperNode.class);
		Vertex v9 = graph.createVertex(DoubleSubNode.class);
		Vertex v10 = graph.createVertex(DoubleSubNode.class);
		Vertex v11 = graph.createVertex(DoubleSubNode.class);
		Vertex v12 = graph.createVertex(DoubleSubNode.class);
		
		//border cases
		assertEquals(0, graph.getECount());
		Edge e1 = graph.createEdge(LinkBack.class, v5, v1);
		assertEquals(1, graph.getECount());
		
		//creating a vertex does not change the value
		graph.createVertex(DoubleSubNode.class);
		assertEquals(1, graph.getECount());
		
		//when an edge is deleted, the count is decreased by 1
		graph.deleteEdge(e1);
		assertEquals(0, graph.getECount());
		
		//normal cases
		//creating an edge increases the value by 1
		Edge e2 = graph.createEdge(Link.class, v2, v7);
		assertEquals(1, graph.getECount());
		Edge e3 = graph.createEdge(LinkBack.class, v8, v4);
		assertEquals(2, graph.getECount());
		Edge e4 = graph.createEdge(SubLink.class, v11, v6);
		assertEquals(3, graph.getECount());
		Edge e5 = graph.createEdge(Link.class, v2, v5);
		assertEquals(4, graph.getECount());
		Edge e6 = graph.createEdge(LinkBack.class, v7, v12);
		assertEquals(5, graph.getECount());
		Edge e7 = graph.createEdge(SubLink.class, v9, v8);
		assertEquals(6, graph.getECount());
		Edge e8 = graph.createEdge(SubLink.class, v10, v6);
		assertEquals(7, graph.getECount());
		Edge e9 = graph.createEdge(Link.class, v3, v7);
		assertEquals(8, graph.getECount());
		Edge e10 = graph.createEdge(Link.class, v3, v7);
		assertEquals(9, graph.getECount());
		
		//deleting edges...
		graph.deleteEdge(e2);
		assertEquals(8, graph.getECount());
		graph.deleteEdge(e3);
		assertEquals(7, graph.getECount());
		graph.deleteEdge(e4);
		assertEquals(6, graph.getECount());
		graph.deleteEdge(e5);
		assertEquals(5, graph.getECount());
		graph.deleteEdge(e6);
		assertEquals(4, graph.getECount());
		graph.deleteEdge(e7);
		assertEquals(3, graph.getECount());
		graph.deleteEdge(e8);
		assertEquals(2, graph.getECount());
		graph.deleteEdge(e9);
		assertEquals(1, graph.getECount());
		graph.deleteEdge(e10);
		assertEquals(0, graph.getECount());
		
		System.out.println("Done testing getECount.");
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
		/* Testen der defragment()-Methode: Ein Vorher-Nachher Abbild von Vertex-
		* Referenzen sammeln und vergleichen, genauso mit Kantenseq. Inzidenzen
		* sind nicht betroffen (von defragment() zumindest das, was einfach zu testen
		* ist); Dafür bedarf es einen Graph, indem gelöscht wurde und dadurch
		* Lücken entstanden sind, sodass defragment() zum Einsatz kommen kann
		*/
	}
	
}