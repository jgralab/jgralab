package de.uni_koblenz.jgralabtest;

import org.junit.Before;
import org.junit.Test;

import de.uni_koblenz.jgralab.impl.FreeIndexList;
//import de.uni_koblenz.jgralabtest.schemas.vertextest.VertexTestGraph;
//import de.uni_koblenz.jgralabtest.schemas.vertextest.VertexTestSchema;

public class FreeIndexListTest {
//	private VertexTestGraph graph;

	/**
	 * Creates a new graph for each test.
	 */
	@Before
	public void setUp() {
//		graph = VertexTestSchema.instance().createVertexTestGraph();
	}

	/*
	 * 1. Test of constructor.
	 */
	
	@Test(expected=AssertionError.class)
	public void constructorTest0(){
		new FreeIndexList(-1);
	}
	
	
}
