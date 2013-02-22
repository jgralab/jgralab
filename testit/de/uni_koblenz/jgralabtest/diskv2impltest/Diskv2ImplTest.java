package de.uni_koblenz.jgralabtest.diskv2impltest;

import java.io.FileNotFoundException;
import java.util.Random;

import org.junit.Test;

import de.uni_koblenz.jgralab.Edge;
import de.uni_koblenz.jgralab.GraphIO;
import de.uni_koblenz.jgralab.ImplementationType;
import de.uni_koblenz.jgralab.Vertex;
import de.uni_koblenz.jgralab.exception.GraphIOException;
import de.uni_koblenz.jgralab.schema.Schema;
import de.uni_koblenz.jgralab.schema.codegenerator.CodeGeneratorConfiguration;
import de.uni_koblenz.jgralabtest.schemas.citymap2.CarPark;
import de.uni_koblenz.jgralabtest.schemas.citymap2.CityMap;
import de.uni_koblenz.jgralabtest.schemas.citymap2.CityMapSchema;
import de.uni_koblenz.jgralabtest.schemas.citymap2.Junction;
import de.uni_koblenz.jgralabtest.schemas.citymap2.Street;

public class Diskv2ImplTest {

	// @Test
	public void test() throws GraphIOException {
		Schema s = GraphIO
				.loadSchemaFromFile("testit/testschemas/citymapschema2.tg");
		s.commit("testit", CodeGeneratorConfiguration.WITH_DISKV2_SUPPORT);
	}

	@Test
	public void test2() throws FileNotFoundException {
		// System.setOut(new PrintStream(new FileOutputStream("out.log")));
		for (int i = 1; i <= 50; i++) {
			long t0 = System.currentTimeMillis();
			t(i);
			long t1 = System.currentTimeMillis();
			System.err.println("Time: " + (t1 - t0) + "ms");
		}
	}

	void t(int n) {
		Random rnd = new Random(n);
		System.out.println("New Turn " + n);
		CityMap graph = CityMapSchema.instance().createCityMap(
				ImplementationType.DISKV2);

		CarPark cp1 = graph.createCarPark();
		CarPark cp2 = graph.createCarPark();
		CarPark cp3 = graph.createCarPark();

		Street s1 = graph.createStreet(cp1, cp2);
		Street s2 = graph.createStreet(cp2, cp3);

		System.out.println(s1.getId() + " a : " + s1.getAlpha() + " o "
				+ s1.getOmega());
		System.out.println(s1.getReversedEdge().getId());

		for (Vertex v : graph.vertices()) {
			System.out.println("vertex: " + v);
		}

		for (Edge e : graph.edges()) {
			System.out.println("edge: " + e);
		}

		for (Edge e : cp1.incidences()) {
			System.out.println("inc of v1: " + e);
		}

		for (Edge e : cp2.incidences()) {
			System.out.println("inc of v2: " + e);
		}

		for (Edge e : cp3.incidences()) {
			System.out.println("inc of v3: " + e);
		}

		cp3.delete();

		for (Edge e : cp1.incidences()) {
			System.out.println("inc of v1: " + e);
		}

		for (Edge e : cp2.incidences()) {
			System.out.println("inc of v2: " + e);
		}

		for (int i = 1; i <= 100000; i++) {
			if (i % 1000 == 0) {
				System.out.print("v");
				System.out.flush();
			}
			CarPark cp = graph.createCarPark();
			cp.set_capacity(rnd.nextInt(1000));
			cp.set_name("Hugo");
		}
		System.out.println();
		for (int i = 1; i <= 400000; i++) {
			if (i % 1000 == 0) {
				System.out.print("e");
				System.out.flush();
				if (i % 100000 == 0) {
					System.out.println();
				}
			}
			Junction alpha = (Junction) graph.getVertex(rnd.nextInt(10000) + 1);
			Junction omega = (Junction) graph.getVertex(rnd.nextInt(10000) + 1);
			graph.createStreet(alpha, omega);
		}

		for (Vertex v : graph.vertices()) {
			if (((Integer) v.getAttribute("capacity")) > 0
					&& ((Integer) v.getAttribute("capacity")) < 100) {
				System.out.println(v + " capacity="
						+ v.getAttribute("capacity") + ", name="
						+ v.getAttribute("name"));
			}
		}
	}
}
