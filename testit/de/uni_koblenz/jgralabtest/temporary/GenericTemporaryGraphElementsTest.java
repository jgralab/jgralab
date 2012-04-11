package de.uni_koblenz.jgralabtest.temporary;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;
import java.util.Iterator;

import org.junit.Before;
import org.junit.Test;
import org.pcollections.ArrayPMap;
import org.pcollections.PMap;

import de.uni_koblenz.jgralab.Edge;
import de.uni_koblenz.jgralab.Graph;
import de.uni_koblenz.jgralab.GraphIO;
import de.uni_koblenz.jgralab.GraphIOException;
import de.uni_koblenz.jgralab.ImplementationType;
import de.uni_koblenz.jgralab.Record;
import de.uni_koblenz.jgralab.TemporaryEdge;
import de.uni_koblenz.jgralab.TemporaryGraphElementConversionException;
import de.uni_koblenz.jgralab.TemporaryVertex;
import de.uni_koblenz.jgralab.Vertex;
import de.uni_koblenz.jgralab.schema.EdgeClass;
import de.uni_koblenz.jgralab.schema.RecordDomain;
import de.uni_koblenz.jgralab.schema.Schema;
import de.uni_koblenz.jgralab.schema.VertexClass;

public class GenericTemporaryGraphElementsTest {

	private Schema schema;
	private VertexClass vc_CrossRoad;
	private VertexClass vc_Plaza;
	private VertexClass vc_Airport;
	private VertexClass vc_Town;
	private EdgeClass ec_Street;
	private EdgeClass ec_AirRoute;
	private EdgeClass ec_ContainsCrossroad;
	
	private Graph graph;
	
	@Before
	public void setUp() throws GraphIOException{
		this.schema = GraphIO.loadSchemaFromFile("testit"+ File.separator + 
				"testschemas" + File.separator + "greqltestschema.tg");
		this.vc_CrossRoad = schema.getGraphClass().getVertexClass("junctions.Crossroad");
		this.vc_Plaza = schema.getGraphClass().getVertexClass("junctions.Plaza");
		this.vc_Airport = schema.getGraphClass().getVertexClass("junctions.Airport");
		this.vc_Town = schema.getGraphClass().getVertexClass("localities.Town");
		this.ec_Street = schema.getGraphClass().getEdgeClass("connections.Street");
		this.ec_AirRoute = schema.getGraphClass().getEdgeClass("connections.AirRoute");
		this.ec_ContainsCrossroad = schema.getGraphClass().getEdgeClass("localities.ContainsCrossroad");
		this.graph = schema.createGraph(ImplementationType.GENERIC);
	}

	@Test
	public void testIsTemporary(){
		Vertex v1_town = graph.createVertex(vc_Town);
		Vertex v2_crossroad = graph.createVertex(vc_CrossRoad);
		Vertex v3_crossroad = graph.createVertex(vc_CrossRoad);
		Vertex v4_temp = graph.createTemporaryVertex();
		Edge e1_street = graph.createEdge(ec_Street, v3_crossroad, v2_crossroad);
		Edge e2_street = graph.createEdge(ec_Street, v3_crossroad, v4_temp);
		Edge e3_street = graph.createEdge(ec_ContainsCrossroad,v1_town,v4_temp);
		Edge e4_temp = graph.createTemporaryEdge(v2_crossroad, v3_crossroad);
		Edge e5_temp = graph.createTemporaryEdge(v1_town, v4_temp);
		
		assertFalse(v1_town.isTemporary());
		assertFalse(v2_crossroad.isTemporary());
		assertFalse(v3_crossroad.isTemporary());
		assertTrue(v4_temp.isTemporary());
		assertFalse(e1_street.isTemporary());
		assertFalse(e2_street.isTemporary());
		assertFalse(e3_street.isTemporary());
		assertTrue(e4_temp.isTemporary());
		assertTrue(e5_temp.isTemporary());
	}
	
	@Test
	public void testConvertTemporaryVertex(){
		Vertex v1_plaza = graph.createVertex(vc_Plaza);
		Vertex v2_crossroad = graph.createVertex(vc_CrossRoad);
		Vertex v3_crossroad = graph.createVertex(vc_CrossRoad);
		
		TemporaryVertex v4_temp = graph.createTemporaryVertex();

		Vertex v5_crossroad = graph.createVertex(vc_CrossRoad);
		
		Edge e1_street = graph.createEdge(ec_Street, v5_crossroad, v4_temp);
		Edge e2_street = graph.createEdge(ec_Street, v4_temp, v2_crossroad);
		Edge e3_street = graph.createEdge(ec_Street, v3_crossroad, v4_temp);
		
		Vertex v4_plaza = v4_temp.convertToRealGraphElement(vc_Plaza);
		
		Vertex v6_plaza = graph.createVertex(vc_Plaza);
		
		assertEquals(4, v4_plaza.getId());
		assertEquals(vc_Plaza, v4_plaza.getAttributedElementClass());
		
		assertEquals(v1_plaza, graph.getFirstVertex());
		assertEquals(v6_plaza, graph.getLastVertex());
		assertEquals(v2_crossroad, v1_plaza.getNextVertex());
		assertEquals(v3_crossroad, v2_crossroad.getNextVertex());
		assertEquals(v4_plaza, v3_crossroad.getNextVertex());
		assertEquals(v5_crossroad, v4_plaza.getNextVertex());
		assertEquals(v5_crossroad, v4_plaza.getNextVertex());
		assertEquals(v6_plaza, v5_crossroad.getNextVertex());
		assertEquals(null, v6_plaza.getNextVertex());
		assertEquals(v4_plaza, v5_crossroad.getPrevVertex());
		assertEquals(v3_crossroad, v4_plaza.getPrevVertex());
		
		Iterator<Edge> it = v4_plaza.incidences().iterator();
		assertEquals(e1_street.getReversedEdge(), it.next());
		assertEquals(e2_street, it.next());
		assertEquals(e3_street.getReversedEdge(), it.next());
		
	}
	
	@Test
	public void testFailConvertTemporaryVertex(){
		Vertex v1_plaza = graph.createVertex(vc_Plaza);
		TemporaryVertex v2_temp = graph.createTemporaryVertex();
		Vertex v3_airport = graph.createVertex(vc_Airport);
		
		graph.createEdge(ec_Street, v1_plaza, v2_temp);
		graph.createEdge(ec_AirRoute, v2_temp, v3_airport);
		
		try{
			v2_temp.convertToRealGraphElement(vc_Airport);
			fail();
		}catch(TemporaryGraphElementConversionException ex){
			assertTrue(v2_temp.isValid());
		}
	}
	
	@Test
	public void testConvertTemporaryEdge(){
		Vertex v1_plaza = graph.createVertex(vc_Plaza);
		Vertex v2_crossroad = graph.createVertex(vc_CrossRoad);
		Vertex v3_crossroad = graph.createVertex(vc_CrossRoad);
		Vertex v4_plaza = graph.createVertex(vc_Plaza);
		
		Edge e1_street = graph.createEdge(ec_Street, v1_plaza, v2_crossroad);
		Edge e2_street = graph.createEdge(ec_Street, v2_crossroad, v4_plaza);
		
		TemporaryEdge e3_temp = graph.createTemporaryEdge(v2_crossroad, v3_crossroad);
		
		Edge e4_street = graph.createEdge(ec_Street, v3_crossroad, v1_plaza);
		Edge e5_street = graph.createEdge(ec_Street, v4_plaza, v2_crossroad);
		Edge e6_street = graph.createEdge(ec_Street, v4_plaza, v3_crossroad);
		
		Edge e3_street = e3_temp.convertToRealGraphElement(ec_Street);
		
		assertEquals(3, e3_street.getId());
		assertEquals(ec_Street, e3_street.getAttributedElementClass());
		assertEquals(v2_crossroad, e3_street.getAlpha());
		assertEquals(v3_crossroad, e3_street.getOmega());
		
		assertEquals(e1_street, graph.getFirstEdge());
		assertEquals(e6_street, graph.getLastEdge());
		assertEquals(e2_street, e1_street.getNextEdge());
		assertEquals(e3_street, e2_street.getNextEdge());
		assertEquals(e4_street, e3_street.getNextEdge());
		assertEquals(e5_street, e4_street.getNextEdge());
		assertEquals(e6_street, e5_street.getNextEdge());
		assertEquals(null, e6_street.getNextEdge());
		assertEquals(e3_street, e4_street.getPrevEdge());
		assertEquals(e2_street, e3_street.getPrevEdge());

		Iterator<Edge> incV2 = v2_crossroad.incidences().iterator();
		assertEquals(e1_street.getReversedEdge(), incV2.next());
		assertEquals(e2_street, incV2.next());
		assertEquals(e3_street, incV2.next());
		assertEquals(e5_street.getReversedEdge(), incV2.next());
		assertFalse(incV2.hasNext());
		
		Iterator<Edge> incV3 = v3_crossroad.incidences().iterator();
		assertEquals(e3_street.getReversedEdge(), incV3.next());
		assertEquals(e4_street, incV3.next());
		assertEquals(e6_street.getReversedEdge(), incV3.next());
		assertFalse(incV3.hasNext());
		
	}
	
	@Test
	public void testConvertTemporaryEdgeFail(){
		Vertex v1_crossroad = graph.createVertex(vc_CrossRoad);
		Vertex v2_airport = graph.createVertex(vc_Airport);
		
		TemporaryEdge e1_temp = graph.createTemporaryEdge(v1_crossroad, v2_airport);
		
		try{
			e1_temp.convertToRealGraphElement(ec_Street);
			fail();
		}catch(TemporaryGraphElementConversionException e){
			assertTrue(e1_temp.isValid());
		}
	}
	
	@Test
	public void testConvertTemporaryVertexWithStringAttribute(){
		TemporaryVertex v_temp = graph.createTemporaryVertex();
		v_temp.setAttribute("name", "Plaza of Cats");
		
		Vertex v_plaza = v_temp.convertToRealGraphElement(vc_Plaza);
		
		assertEquals(1, v_plaza.getId());
		assertEquals(vc_Plaza, v_plaza.getAttributedElementClass());
		assertEquals("Plaza of Cats", v_plaza.getAttribute("name"));
	}

	@Test
	public void testConvertTemporaryVertexWithStringAttributeFail(){
		TemporaryVertex v_temp = graph.createTemporaryVertex();
		v_temp.setAttribute("name", 1234);
		
		try{
			v_temp.convertToRealGraphElement(vc_Plaza);
			fail();
		}catch(TemporaryGraphElementConversionException ex){
			assertTrue(v_temp.isValid());
		}

	}
	
	@Test
	public void testConvertTemporaryEdgeWithPrimitiveAttributes(){
		Vertex v1_crossroad = graph.createVertex(vc_CrossRoad);
		Vertex v2_crossroad = graph.createVertex(vc_CrossRoad);
		TemporaryEdge e_temp = graph.createTemporaryEdge(v1_crossroad, v2_crossroad);
		e_temp.setAttribute("name", "Gandhi-Street");
		e_temp.setAttribute("oneway", true);
		e_temp.setAttribute("length", 111.11);
		e_temp.setAttribute("attributeThatDoesNotExist", "Hugo");
		
		Edge e_street = e_temp.convertToRealGraphElement(ec_Street);
		
		assertEquals(1, e_street.getId());
		assertEquals(ec_Street, e_street.getAttributedElementClass());
		assertEquals(v1_crossroad, e_street.getAlpha());
		assertEquals(v2_crossroad, e_street.getOmega());
		assertEquals("Gandhi-Street", e_street.getAttribute("name"));
		assertEquals(true, e_street.getAttribute("oneway"));
		assertEquals(111.11, e_street.getAttribute("length"));
	}
	
	@Test
	public void testConvertVertexWithRecordAttribute(){
		TemporaryVertex v1_temp = graph.createTemporaryVertex();
		PMap<String, Object> values = ArrayPMap.empty();
		values = values.plus("day", 8);
		values = values.plus("month", "AUG");
		values = values.plus("year", 2008);
		Record record = graph.createRecord((RecordDomain)schema.getDomain("Date"), values);
		v1_temp.setAttribute("foundingDate", record);
		
		Vertex v1_town = v1_temp.convertToRealGraphElement(vc_Town);
		
		assertEquals(1, v1_town.getId());
		assertEquals(vc_Town, v1_town.getAttributedElementClass());
		assertEquals(record, v1_town.getAttribute("foundingDate"));
	}
	
	@Test
	public void testConvertVertexWithRecordAttributeFail(){
		TemporaryVertex v1_temp = graph.createTemporaryVertex();
		PMap<String, Object> values = ArrayPMap.empty();
		values = values.plus("days", 8);
		values = values.plus("month", "AUG");
		values = values.plus("year", 2008);
		Record record = graph.createRecord((RecordDomain)schema.getDomain("Date"), values);
		v1_temp.setAttribute("foundingDate", record);
		
		try{
			v1_temp.convertToRealGraphElement(vc_Town);
			fail();
		}catch(TemporaryGraphElementConversionException ex ){
			assertTrue(v1_temp.isValid());
		}
	}
	
}
