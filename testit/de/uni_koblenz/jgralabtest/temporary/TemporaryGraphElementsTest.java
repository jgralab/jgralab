package de.uni_koblenz.jgralabtest.temporary;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Iterator;

import org.junit.Test;

import de.uni_koblenz.jgralab.Edge;
import de.uni_koblenz.jgralab.Graph;
import de.uni_koblenz.jgralab.GraphIO;
import de.uni_koblenz.jgralab.GraphIOException;
import de.uni_koblenz.jgralab.ImplementationType;
import de.uni_koblenz.jgralab.TemporaryEdge;
import de.uni_koblenz.jgralab.TemporaryGraphElementBlessingException;
import de.uni_koblenz.jgralab.TemporaryVertex;
import de.uni_koblenz.jgralab.Vertex;
import de.uni_koblenz.jgralab.schema.Schema;
import de.uni_koblenz.jgralabtest.schemas.citymap.CityMapSchema;

public class TemporaryGraphElementsTest {

	private static ImplementationType impl = ImplementationType.STANDARD;

	@Test
	public void testCreatingTemporaryGraphElements() throws GraphIOException {
		Schema schema = CityMapSchema.instance();
		Graph g = schema.createGraph(impl);

		Vertex v1 = g.createVertex(schema.getGraphClass().getVertexClass(
				"ParkingGarage"));
		Vertex v2 = g.createVertex(schema.getGraphClass().getVertexClass(
				"Intersection"));
		g.createEdge(schema.getGraphClass().getEdgeClass("Street"), v1, v2);

		TemporaryVertex tempv = g.createTemporaryVertex();

		TemporaryEdge e2 = g.createTemporaryEdge(v1, tempv);
		e2.setPreliminaryType(schema.getGraphClass().getEdgeClass("Street"));

		assertEquals(v1, e2.getAlpha());
		assertEquals(tempv, e2.getOmega());

		Edge tempe = g.createTemporaryEdge(v1, v2);

		assertEquals(v1, tempe.getAlpha());
		assertEquals(v2, tempe.getOmega());

		assertEquals(3, v1.getDegree());

		assertEquals(1, tempv.getDegree());

		tempv.setAttribute("anAttribute", "Hugo Harry");

		assertEquals("Hugo Harry", tempv.getAttribute("anAttribute"));

		for (Vertex vv : g
				.vertices(g.getGraphClass().getTemporaryVertexClass())) {
			assertEquals(tempv, vv);
		}

	}

	@Test
	public void testTransformVertex() throws GraphIOException {
		Schema schema = CityMapSchema.instance();
		Graph g = schema.createGraph(impl);

		Vertex v1 = g.createVertex(schema.getGraphClass().getVertexClass(
				"ParkingGarage"));
		Vertex v2 = g.createVertex(schema.getGraphClass().getVertexClass(
				"Intersection"));
		g.createEdge(schema.getGraphClass().getEdgeClass("Street"), v1, v2);

		TemporaryVertex tempv = g.createTemporaryVertex();

		TemporaryEdge e2 = g.createTemporaryEdge(v1, tempv);
		e2.setPreliminaryType(schema.getGraphClass().getEdgeClass("Street"));
		int e2_id = e2.getId();
		TemporaryEdge e3 = g.createTemporaryEdge(schema.getGraphClass()
				.getEdgeClass("Bridge"), v2, tempv);
		int e3_id = e3.getId();
		TemporaryEdge e4 = g.createTemporaryEdge(schema.getGraphClass()
				.getEdgeClass("Street"), tempv, v1);
		int e4_id = e4.getId();
		Edge tempe = g.createTemporaryEdge(tempv, v2);

		assertEquals(4, tempv.getDegree());

		tempv.setAttribute("hugoAtt", "Talils");
		tempv.setAttribute("name", "HugoJunction");

		Vertex v3 = g.createVertex(schema.getGraphClass().getVertexClass(
				"Intersection"));

		Vertex v = tempv.bless(schema.getGraphClass().getVertexClass(
				"Intersection"));
		assertTrue(v.isValid());
		assertFalse(tempv.isValid());

		assertEquals(g.getEdge(e2_id).getReversedEdge(), v.getFirstIncidence());
		assertEquals(g.getEdge(e3_id).getReversedEdge(), v.getFirstIncidence()
				.getNextIncidence());
		assertEquals(g.getEdge(e4_id), v.getFirstIncidence().getNextIncidence()
				.getNextIncidence());
		assertEquals(tempe, v.getFirstIncidence().getNextIncidence()
				.getNextIncidence().getNextIncidence());
		assertEquals(tempe, v.getLastIncidence());

		assertEquals("HugoJunction", v.getAttribute("name"));

		assertEquals(v1, g.getFirstVertex());
		assertEquals(v2, g.getFirstVertex().getNextVertex());
		assertEquals(v, g.getFirstVertex().getNextVertex().getNextVertex());
		assertEquals(v3, g.getFirstVertex().getNextVertex().getNextVertex()
				.getNextVertex());
		assertEquals(v3, g.getLastVertex());

	}

	@Test(expected = GraphIOException.class)
	public void testExceptionWhileSaving() throws GraphIOException {
		Schema schema = CityMapSchema.instance();
		Graph g = schema.createGraph(impl);

		Vertex v1 = g.createVertex(schema.getGraphClass().getVertexClass(
				"ParkingGarage"));

		Vertex v2 = g.createVertex(schema.getGraphClass().getVertexClass(
				"Intersection"));

		g.createEdge(schema.getGraphClass().getEdgeClass("Street"), v1, v2);

		TemporaryVertex tempv = g.createTemporaryVertex();

		g.createTemporaryEdge(schema.getGraphClass().getEdgeClass("Street"),
				v1, tempv);

		g.createTemporaryEdge(schema.getGraphClass().getEdgeClass("Street"),
				tempv, v1);

		writeTgToConsole(g);

	}

	@Test
	public void testFirstVertexTemp() throws GraphIOException {
		Schema schema = CityMapSchema.instance();
		Graph g = schema.createGraph(impl);
		TemporaryVertex tempv = g.createTemporaryVertex();
		Edge e = g.createTemporaryEdge(
				schema.getGraphClass().getEdgeClass("Street"), tempv, tempv);
		Vertex v = g.createVertex(schema.getGraphClass().getVertexClass(
				"Intersection"));

		Vertex transformed = tempv.bless(schema.getGraphClass().getVertexClass(
				"Intersection"));
		assertTrue(transformed.isValid());
		assertFalse(tempv.isValid());
		assertEquals(transformed, g.getFirstVertex());
		assertEquals(1, transformed.getId());
		assertEquals(2, v.getId());
		assertEquals(v, transformed.getNextVertex());
		Iterator<Edge> it = transformed.incidences().iterator();
		e = g.getEdge(1);
		assertEquals(e, it.next());
		assertEquals(e.getReversedEdge(), it.next());

	}

	@Test
	public void testTransformEdge() {
		Schema schema = CityMapSchema.instance();
		Graph g = schema.createGraph(impl);

		Vertex v1 = g.createVertex(schema.getGraphClass().getVertexClass(
				"ParkingGarage"));

		Vertex v2 = g.createVertex(schema.getGraphClass().getVertexClass(
				"Intersection"));

		TemporaryEdge tempEdge1 = g.createTemporaryEdge(v1, v2);

		Edge transEdge1 = tempEdge1.bless(schema.getGraphClass().getEdgeClass(
				"Bridge"));
		assertTrue(transEdge1.isValid());
		assertFalse(tempEdge1.isValid());
		assertEquals(v1, transEdge1.getAlpha());
		assertEquals(v2, transEdge1.getOmega());
		assertFalse(tempEdge1.isValid());

	}

	@Test
	public void testTransformEdge2() throws GraphIOException {
		Schema schema = CityMapSchema.instance();
		Graph g = schema.createGraph(impl);

		Vertex v1 = g.createVertex(schema.getGraphClass().getVertexClass(
				"ParkingGarage"));
		Vertex v2 = g.createVertex(schema.getGraphClass().getVertexClass(
				"Intersection"));
		Edge e1 = g.createEdge(schema.getGraphClass().getEdgeClass("Street"),
				v1, v2);
		Vertex v3 = g.createVertex(schema.getGraphClass().getVertexClass(
				"Intersection"));
		Edge e2 = g.createEdge(schema.getGraphClass().getEdgeClass("Street"),
				v3, v1);

		TemporaryEdge tempEdge1 = g.createTemporaryEdge(v1, v2);

		Vertex v4 = g.createVertex(schema.getGraphClass().getVertexClass(
				"Intersection"));
		Edge e4 = g.createEdge(schema.getGraphClass().getEdgeClass("Street"),
				v2, v4);
		Edge e5 = g.createEdge(schema.getGraphClass().getEdgeClass("Street"),
				v1, v4);

		e2.setOmega(v2);

		Edge transEdge1 = tempEdge1.bless(schema.getGraphClass().getEdgeClass(
				"Bridge"));
		assertTrue(transEdge1.isValid());
		assertFalse(tempEdge1.isValid());
		assertEquals(v1, transEdge1.getAlpha());
		assertEquals(v2, transEdge1.getOmega());
		assertFalse(tempEdge1.isValid());

		assertEquals(3, transEdge1.getId());
		assertEquals(e1, transEdge1.getPrevIncidence());
		assertEquals(e5, transEdge1.getNextIncidence());
		assertEquals(e1.getReversedEdge(), transEdge1.getReversedEdge()
				.getPrevIncidence());
		assertEquals(e4, transEdge1.getReversedEdge().getNextIncidence());
		assertEquals(e1, v1.getFirstIncidence());
		assertEquals(e5, v1.getLastIncidence());
		assertEquals(e1.getReversedEdge(), v2.getFirstIncidence());
		assertEquals(e2.getReversedEdge(), v2.getLastIncidence());

		writeTgToConsole(g);
	}

	@Test(expected = TemporaryGraphElementBlessingException.class)
	public void testWrongAttributeValue() {
		Schema schema = CityMapSchema.instance();
		Graph g = schema.createGraph(impl);

		TemporaryVertex v = g.createTemporaryVertex();
		v.setAttribute("name", 4.0);

		v.bless(g.getGraphClass().getVertexClass("Intersection"));
	}

	@Test
	public void testTransformFreeID() {
		Schema schema = CityMapSchema.instance();
		Graph g = schema.createGraph(impl);

		Vertex v1 = g.createVertex(g.getGraphClass().getVertexClass(
				"Intersection"));
		TemporaryVertex v2 = g.createTemporaryVertex();
		Vertex v3 = g.createVertex(g.getGraphClass().getVertexClass(
				"Intersection"));
		Vertex cv2 = v2.bless(g.getGraphClass().getVertexClass("Intersection"));
		assertTrue(cv2.isValid());
		assertFalse(v2.isValid());

		Vertex v4 = g.createVertex(g.getGraphClass().getVertexClass(
				"Intersection"));

		assertEquals(1, v1.getId());
		assertEquals(2, cv2.getId());
		assertEquals(3, v3.getId());
		assertEquals(4, v4.getId());
	}

	@Test
	public void testIsTemporary() {
		Schema schema = CityMapSchema.instance();
		Graph g = schema.createGraph(impl);

		Vertex v1 = g.createVertex(g.getGraphClass().getVertexClass(
				"Intersection"));
		assertFalse(v1.isTemporary());

		Vertex v2 = g.createTemporaryVertex();
		assertTrue(v2.isTemporary());

		Edge e1 = g.createTemporaryEdge(
				schema.getGraphClass().getEdgeClass("Bridge"), v1, v2);
		assertTrue(e1.isTemporary());

		Edge e2 = g.createTemporaryEdge(v2, v1);
		assertTrue(e2.isTemporary());
	}

	@Test
	public void testGetTemporaryClasses() {
		Schema schema = CityMapSchema.instance();
		Graph g = schema.createGraph(impl);

		Vertex v1 = g.createVertex(g.getGraphClass().getVertexClass(
				"Intersection"));
		Vertex v2 = g.createVertex(g.getGraphClass().getVertexClass(
				"Intersection"));
		Vertex v3_t = g.createTemporaryVertex();
		Vertex v4 = g.createVertex(g.getGraphClass().getVertexClass(
				"Intersection"));
		Vertex v5_t = g.createTemporaryVertex();

		g.createEdge(schema.getGraphClass().getEdgeClass("Bridge"), v1, v2);
		Edge e2_t = g.createTemporaryEdge(
				schema.getGraphClass().getEdgeClass("Bridge"), v1, v3_t);
		Edge e3_t = g.createTemporaryEdge(v3_t, v4);
		Edge e4_t = g.createTemporaryEdge(v2, v4);
		g.createEdge(schema.getGraphClass().getEdgeClass("Street"), v4, v1);

		Iterator<Edge> it = g.edges(
				schema.getGraphClass().getTemporaryEdgeClass()).iterator();
		assertEquals(e2_t, it.next());
		assertEquals(e3_t, it.next());
		assertEquals(e4_t, it.next());
		assertFalse(it.hasNext());

		Iterator<Vertex> itv = g.vertices(
				schema.getGraphClass().getTemporaryVertexClass()).iterator();
		assertEquals(v3_t, itv.next());
		assertEquals(v5_t, itv.next());
		assertFalse(itv.hasNext());

	}

	@Test
	public void testConvertTemporaryEdge() {
		Schema schema = CityMapSchema.instance();
		Graph g = schema.createGraph(impl);

		Vertex v1 = g.createVertex(g.getGraphClass().getVertexClass(
				"Intersection"));
		Vertex v2 = g.createVertex(g.getGraphClass().getVertexClass(
				"Intersection"));
		Vertex v3 = g.createVertex(g.getGraphClass().getVertexClass(
				"Intersection"));
		Vertex v4 = g.createVertex(g.getGraphClass().getVertexClass(
				"Intersection"));

		Edge e1_1_2 = g.createEdge(schema.getGraphClass()
				.getEdgeClass("Street"), v1, v2);
		Edge e2_2_3 = g.createEdge(schema.getGraphClass()
				.getEdgeClass("Street"), v2, v3);
		Edge e3_3_4 = g.createEdge(schema.getGraphClass()
				.getEdgeClass("Street"), v3, v4);
		Edge e4_4_1 = g.createEdge(schema.getGraphClass()
				.getEdgeClass("Street"), v4, v1);
		Edge e5_1_3 = g.createEdge(schema.getGraphClass()
				.getEdgeClass("Street"), v1, v3);

		TemporaryEdge e6_3_2_t = g.createTemporaryEdge(v3, v2);

		Edge e7_4_2 = g.createEdge(schema.getGraphClass()
				.getEdgeClass("Street"), v4, v2);
		Edge e8_2_2 = g.createEdge(schema.getGraphClass()
				.getEdgeClass("Street"), v2, v2);

		Edge e6_3_2 = e6_3_2_t.bless(schema.getGraphClass().getEdgeClass(
				"Bridge"));

		assertTrue(e6_3_2.isValid());
		assertFalse(e6_3_2_t.isValid());

		assertEquals(v3, e6_3_2.getAlpha());
		assertEquals(v2, e6_3_2.getOmega());

		assertEquals(6, e6_3_2.getId());
		assertEquals(7, e7_4_2.getId());
		assertEquals(8, e8_2_2.getId());

		assertEquals(e1_1_2, g.getFirstEdge());
		assertEquals(e2_2_3, e1_1_2.getNextEdge());
		assertEquals(e3_3_4, e2_2_3.getNextEdge());
		assertEquals(e4_4_1, e3_3_4.getNextEdge());
		assertEquals(e5_1_3, e4_4_1.getNextEdge());
		assertEquals(e6_3_2, e5_1_3.getNextEdge());
		assertEquals(e7_4_2, e6_3_2.getNextEdge());
		assertEquals(e8_2_2, e7_4_2.getNextEdge());
		assertEquals(null, e8_2_2.getNextEdge());
		assertEquals(e8_2_2, g.getLastEdge());
		assertEquals(e6_3_2, e7_4_2.getPrevEdge());
		assertEquals(e5_1_3, e6_3_2.getPrevEdge());

		Iterator<Edge> it = v3.incidences().iterator();

		assertEquals(e2_2_3.getReversedEdge(), it.next());
		assertEquals(e3_3_4, it.next());
		assertEquals(e5_1_3.getReversedEdge(), it.next());
		assertEquals(e6_3_2, it.next());
		assertFalse(it.hasNext());

		Iterator<Edge> it2 = v2.incidences().iterator();
		assertEquals(e1_1_2.getReversedEdge(), it2.next());
		assertEquals(e2_2_3, it2.next());
		assertEquals(e6_3_2.getReversedEdge(), it2.next());
		assertEquals(e7_4_2.getReversedEdge(), it2.next());
		assertEquals(e8_2_2, it2.next());
		assertEquals(e8_2_2.getReversedEdge(), it2.next());
		assertFalse(it2.hasNext());
	}

	@Test
	public void testConvertTemporaryVertex() {
		Schema schema = CityMapSchema.instance();
		Graph g = schema.createGraph(impl);

		Vertex v1 = g.createVertex(g.getGraphClass().getVertexClass(
				"Intersection"));
		Vertex v2 = g.createVertex(g.getGraphClass().getVertexClass(
				"Intersection"));
		Vertex v3 = g.createVertex(g.getGraphClass().getVertexClass(
				"Intersection"));

		Edge e2_2_3 = g.createTemporaryEdge(schema.getGraphClass()
				.getEdgeClass("Street"), v2, v3);
		int id_e2_2_3 = e2_2_3.getId();

		TemporaryVertex v4_t = g.createTemporaryVertex();

		Edge e4_4_2 = g.createTemporaryEdge(schema.getGraphClass()
				.getEdgeClass("Street"), v4_t, v2);
		int id_e4_4_2 = e4_4_2.getId();
		e2_2_3.setOmega(v4_t);
		Edge e5_1_4 = g.createTemporaryEdge(schema.getGraphClass()
				.getEdgeClass("Street"), v1, v4_t);
		int id_e5_1_4 = e5_1_4.getId();

		Vertex v5 = g.createVertex(g.getGraphClass().getVertexClass(
				"Intersection"));
		Vertex v6 = g.createVertex(g.getGraphClass().getVertexClass(
				"Intersection"));

		Vertex v4 = v4_t.bless(schema.getGraphClass().getVertexClass(
				"Intersection"));
		assertTrue(v4.isValid());
		assertFalse(v4_t.isValid());
		assertFalse(e2_2_3.isValid());
		assertFalse(e4_4_2.isValid());
		assertFalse(e5_1_4.isValid());

		Vertex v7 = g.createVertex(g.getGraphClass().getVertexClass(
				"Intersection"));

		assertEquals(4, v4.getId());

		assertEquals(v1, g.getFirstVertex());
		assertEquals(v2, v1.getNextVertex());
		assertEquals(v3, v2.getNextVertex());
		assertEquals(v4, v3.getNextVertex());
		assertEquals(v5, v4.getNextVertex());
		assertEquals(v6, v5.getNextVertex());
		assertEquals(v7, v6.getNextVertex());
		assertEquals(null, v7.getNextVertex());
		assertEquals(v7, g.getLastVertex());
		assertEquals(v4, v5.getPrevVertex());
		assertEquals(v3, v4.getPrevVertex());

		Iterator<Edge> it = v4.incidences().iterator();
		assertEquals(g.getEdge(id_e4_4_2), it.next());
		assertEquals(g.getEdge(id_e2_2_3).getReversedEdge(), it.next());
		assertEquals(g.getEdge(id_e5_1_4).getReversedEdge(), it.next());
		assertFalse(it.hasNext());

		assertEquals(v4, g.getEdge(id_e4_4_2).getAlpha());
		assertEquals(v4, g.getEdge(id_e2_2_3).getOmega());
		assertEquals(v4, g.getEdge(id_e5_1_4).getOmega());

	}

	@Test
	public void testHasTemporaryGraphElements() {
		Schema schema = CityMapSchema.instance();
		Graph g = schema.createGraph(impl);

		assertFalse(g.hasTemporaryElements());

		Vertex v1 = g.createVertex(g.getGraphClass().getVertexClass(
				"Intersection"));

		assertFalse(g.hasTemporaryElements());

		TemporaryVertex tempv = g.createTemporaryVertex();

		assertTrue(g.hasTemporaryElements());

		TemporaryEdge tempe = g.createTemporaryEdge(v1, tempv);

		assertTrue(g.hasTemporaryElements());

		tempv.bless(schema.getGraphClass().getVertexClass("Intersection"));

		assertTrue(g.hasTemporaryElements());

		tempe.bless(schema.getGraphClass().getEdgeClass("Street"));

		assertFalse(g.hasTemporaryElements());

		TemporaryVertex tempv2 = g.createTemporaryVertex();

		assertTrue(g.hasTemporaryElements());

		tempv2.delete();

		assertFalse(g.hasTemporaryElements());

	}

	@Test
	public void testCreateTempEdgeAtTempVertexAuto() {
		Schema schema = CityMapSchema.instance();
		Graph g = schema.createGraph(impl);

		Vertex v1 = g.createVertex(schema.getGraphClass().getVertexClass(
				"Intersection"));
		TemporaryVertex tempv2 = g.createTemporaryVertex();

		Edge e = g.createEdge(schema.getGraphClass().getEdgeClass("Street"),
				v1, tempv2);
		assertTrue(e.isTemporary());
	}

	@Test
	public void testFailBlessEdge() {
		Schema schema = CityMapSchema.instance();
		Graph g = schema.createGraph(impl);

		Vertex v1 = g.createVertex(schema.getGraphClass().getVertexClass(
				"Intersection"));
		TemporaryVertex tempv2 = g.createTemporaryVertex();

		TemporaryEdge tempe = g.createTemporaryEdge(schema.getGraphClass()
				.getEdgeClass("Street"), v1, tempv2);

		try {
			tempe.bless(tempe.getPreliminaryType());
			fail();
		} catch (TemporaryGraphElementBlessingException ex) {
			assertTrue(tempe.isValid());
		}

	}

	@Test
	public void testPreliminaryTypeForTemporaryVertex() {
		Schema schema = CityMapSchema.instance();
		Graph g = schema.createGraph(impl);

		TemporaryVertex tempV1 = g.createTemporaryVertex(schema.getGraphClass()
				.getVertexClass("Intersection"));

		assertTrue(tempV1.isTemporary());

		Vertex v1 = tempV1.bless();

		assertFalse(v1.isTemporary());
		assertFalse(tempV1.isValid());
		assertEquals(1, v1.getId());
	}

	@Test
	public void testBlessNonTemporaryElements() {
		Schema schema = CityMapSchema.instance();
		Graph g = schema.createGraph(impl);

		Vertex v1 = g.createVertex(schema.getGraphClass().getVertexClass(
				"Intersection"));

		assertEquals(v1, v1.bless());

		Edge e1 = g.createEdge(schema.getGraphClass().getEdgeClass("Street"),
				v1, v1);
		assertEquals(e1, e1.bless());

		try {
			v1.bless(schema.getGraphClass().getVertexClass("ParkingGarage"));
			fail();
		} catch (TemporaryGraphElementBlessingException ex) {
			// No blessing to wrong type allowed
		}

		try {
			e1.bless(schema.getGraphClass().getEdgeClass("Bridge"));
			fail();
		} catch (TemporaryGraphElementBlessingException ex) {
			// No blessing to wrong type allowed
		}
	}

	private void writeTgToConsole(Graph g) throws GraphIOException {
		try {
			ByteArrayOutputStream out = new ByteArrayOutputStream();
			GraphIO.saveGraphToStream(g, new DataOutputStream(out), null);
			out.flush();
			// System.out.println(out.toString());
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
