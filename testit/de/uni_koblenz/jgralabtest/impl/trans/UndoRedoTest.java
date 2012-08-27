package de.uni_koblenz.jgralabtest.impl.trans;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.util.Iterator;
import java.util.Stack;

import org.junit.Before;
import org.junit.Test;

import de.uni_koblenz.jgralab.GraphIO;
import de.uni_koblenz.jgralab.GraphIOException;
import de.uni_koblenz.jgralab.ImplementationType;
import de.uni_koblenz.jgralab.trans.InvalidSavepointException;
import de.uni_koblenz.jgralab.trans.Savepoint;
import de.uni_koblenz.jgralab.trans.Transaction;
import de.uni_koblenz.jgralabtest.schemas.greqltestschema.RouteMap;
import de.uni_koblenz.jgralabtest.schemas.greqltestschema.RouteSchema;
import de.uni_koblenz.jgralabtest.schemas.greqltestschema.connections.Street;
import de.uni_koblenz.jgralabtest.schemas.greqltestschema.junctions.Crossroad;
import de.uni_koblenz.jgralabtest.schemas.greqltestschema.localities.City;
import de.uni_koblenz.jgralabtest.schemas.greqltestschema.localities.County;
import de.uni_koblenz.jgralabtest.schemas.greqltestschema.localities.Town;
import de.uni_koblenz.jgralabtest.schemas.greqltestschema.localities.Village;

public class UndoRedoTest {

	private RouteMap loadedgraph;
	private RouteMap newGraph;

	
	@Before
	public void setUp() throws GraphIOException {
		loadedgraph = (RouteMap) GraphIO.loadGraphFromFile("testit"
				+ File.separator + "testgraphs" + File.separator
				+ "greqltestgraph.tg", ImplementationType.TRANSACTION, null);
		newGraph = RouteSchema.instance().createRouteMap(ImplementationType.TRANSACTION);

	}

	@Test
	public void testSimpleCreateNode() throws InvalidSavepointException {
		Transaction lgTrans = this.loadedgraph.newTransaction();
		int vcount = this.loadedgraph.getVCount();
		Savepoint sp1 = lgTrans.defineSavepoint();
		Town t1 = this.loadedgraph.createTown();
		assertEquals(vcount +1, this.loadedgraph.getVCount());
		lgTrans.restoreSavepoint(sp1);
		assertEquals(vcount, this.loadedgraph.getVCount());
		assertFalse(t1.isValid());
		
		Transaction ngTrans = this.newGraph.newTransaction();
		vcount = this.newGraph.getVCount();
		sp1 = ngTrans.defineSavepoint();
		t1 = this.newGraph.createTown();
		assertEquals(vcount +1, this.newGraph.getVCount());
		ngTrans.restoreSavepoint(sp1);
		assertEquals(vcount, this.newGraph.getVCount());
		assertFalse(t1.isValid());
		
	}
	
	@Test
	public void testSimpleDeleteNode() throws InvalidSavepointException{
		Transaction lgTrans = this.loadedgraph.newTransaction();
		int vcount = this.loadedgraph.getVCount();
		int ecount = this.loadedgraph.getECount();
		Savepoint sp1 = this.loadedgraph.defineSavepoint();
		Town t1 = this.loadedgraph.getFirstTown();
		t1.delete();
		assertEquals(vcount - 2, this.loadedgraph.getVCount());
		assertEquals(ecount - 4, this.loadedgraph.getECount());
		assertFalse(t1.isValid());
		lgTrans.restoreSavepoint(sp1);
		assertEquals(vcount, this.loadedgraph.getVCount());
		assertEquals(ecount, this.loadedgraph.getECount());
		assertTrue(t1.isValid());

		Transaction ngTrans = this.newGraph.newTransaction();
		this.newGraph.createTown();
		vcount = this.newGraph.getVCount();
		ecount = this.newGraph.getECount();
		sp1 = this.newGraph.defineSavepoint();
		t1 = this.newGraph.getFirstTown();
		t1.delete();
		assertEquals(vcount - 1, this.newGraph.getVCount());
		assertEquals(ecount, this.newGraph.getECount());
		assertFalse(t1.isValid());
		ngTrans.restoreSavepoint(sp1);
		assertEquals(vcount, this.newGraph.getVCount());
		assertEquals(ecount, this.newGraph.getECount());
		assertTrue(t1.isValid());
	}

	
	@Test
	public void minimalFailureTest() throws InvalidSavepointException{
		Transaction trans = this.newGraph.newTransaction();
		
		Savepoint sp1 = this.newGraph.defineSavepoint();
		Town t1 = this.newGraph.createTown();
		
		Savepoint sp2 = this.newGraph.defineSavepoint();
		t1.set_name("Rennerod");
				
		trans.restoreSavepoint(sp2);
		
		Village v1 = this.newGraph.createVillage();
		
	}
	
	@Test
	public void testSequence1() throws InvalidSavepointException{
		Transaction trans = this.newGraph.newTransaction();
		Stack<Savepoint> savepoints = new Stack<Savepoint>();
		savepoints.push(this.newGraph.defineSavepoint());
		
		Town t1 = this.newGraph.createTown();
		savepoints.push(this.newGraph.defineSavepoint());

		t1.set_name("Rennerod");
		
		savepoints.push(this.newGraph.defineSavepoint());
		t1.set_inhabitants(2234);
		
		savepoints.push(this.newGraph.defineSavepoint());
		Village v1 = this.newGraph.createVillage();
		
		savepoints.push(this.newGraph.defineSavepoint());
		v1.set_name("Seck");
		
		savepoints.push(this.newGraph.defineSavepoint());
		Crossroad v1_cr1 = this.newGraph.createCrossroad();
		
		savepoints.push(this.newGraph.defineSavepoint());
		Crossroad v1_cr2 = this.newGraph.createCrossroad();

		savepoints.push(this.newGraph.defineSavepoint());
		Street v1_s1_cr1_cr2 = this.newGraph.createStreet(v1_cr1, v1_cr2);
		
		savepoints.push(this.newGraph.defineSavepoint());
		v1_s1_cr1_cr2.set_name("Neustra§e");
		
		savepoints.push(this.newGraph.defineSavepoint());
		v1_s1_cr1_cr2.set_length(0.67);
		
		savepoints.push(this.newGraph.defineSavepoint());
		County c1 = this.newGraph.createCounty();
		
		savepoints.push(this.newGraph.defineSavepoint());
		c1.set_name("Hesssen");
		
		Savepoint sp = savepoints.peek();
		trans.restoreSavepoint(sp);
		assertEquals(null, c1.get_name());
		//trans.removeSavepoint(sp);

		//savepoints.push(this.newGraph.defineSavepoint());		
		c1.set_name("Rheinland-Pfalz");
		
		savepoints.push(this.newGraph.defineSavepoint());
		City cit1 = this.newGraph.createCity();
		
		savepoints.push(this.newGraph.defineSavepoint());
		cit1.set_name("Mainz");
		
		savepoints.push(this.newGraph.defineSavepoint());
		cit1.set_inhabitants(12345);
		
		savepoints.push(this.newGraph.defineSavepoint());
		Crossroad t1_cr1 = this.newGraph.createCrossroad();
		
		savepoints.push(this.newGraph.defineSavepoint());
		this.newGraph.deleteVertex(this.newGraph.getFirstTown());
		
		savepoints.push(this.newGraph.defineSavepoint());
		Town t3 = this.newGraph.createTown();
		
		savepoints.push(this.newGraph.defineSavepoint());
		t3.set_name("Montabaur");
		
		Iterator<Town> townIt = this.newGraph.getTownVertices().iterator();		
		assertTrue(townIt.hasNext());
		townIt.next();
		assertTrue(townIt.hasNext());
		townIt.next();
		assertTrue(townIt.hasNext());
		townIt.next();
		assertFalse(townIt.hasNext());
		
		
		
		
	}
	
}
