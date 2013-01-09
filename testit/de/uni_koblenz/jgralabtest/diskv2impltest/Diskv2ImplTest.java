package de.uni_koblenz.jgralabtest.diskv2impltest;

import org.junit.Test;

import de.uni_koblenz.jgralab.Edge;
import de.uni_koblenz.jgralab.GraphIO;
import de.uni_koblenz.jgralab.ImplementationType;
import de.uni_koblenz.jgralab.Vertex;
import de.uni_koblenz.jgralab.exception.GraphIOException;
import de.uni_koblenz.jgralab.schema.Schema;
import de.uni_koblenz.jgralab.schema.VertexClass;
import de.uni_koblenz.jgralab.schema.codegenerator.CodeGenerator;
import de.uni_koblenz.jgralab.schema.codegenerator.CodeGeneratorConfiguration;
import de.uni_koblenz.jgralab.schema.impl.AttributedElementClassImpl;
import de.uni_koblenz.jgralabtest.schemas.citymap2.CarPark;
import de.uni_koblenz.jgralabtest.schemas.citymap2.CityMap;
import de.uni_koblenz.jgralabtest.schemas.citymap2.CityMapSchema;
import de.uni_koblenz.jgralabtest.schemas.citymap2.Junction;
import de.uni_koblenz.jgralabtest.schemas.citymap2.Street;
import de.uni_koblenz.jgralabtest.schemas.citymap2.impl.diskv2.IntersectionImpl;

public class Diskv2ImplTest {

	
	//@Test
	public void test() throws GraphIOException{
		Schema s = GraphIO.loadSchemaFromFile("testit/testschemas/citymapschema2.tg");
		s.commit("testit",CodeGeneratorConfiguration.WITH_DISKV2_SUPPORT);
		
	}
	@Test
	public void test2(){
		System.out.println(Integer.MAX_VALUE);
		System.out.println(Long.MAX_VALUE);
		CityMap graph= CityMapSchema.instance().createCityMap(ImplementationType.DISKV2);
		
		CarPark cp1 = graph.createCarPark();
		CarPark cp2 = graph.createCarPark();
		CarPark cp3 = graph.createCarPark();
		
		Street s1 = graph.createStreet(cp1, cp2);
		Street s2 = graph.createStreet(cp2, cp3);
		
		System.out.println(s1.getId());
		System.out.println(s1.getReversedEdge().getId());
		
		for (Vertex v : graph.vertices()){
			System.out.println("vertex: " + v );
		}
		
		for(Edge e : graph.edges()){
			System.out.println("edge: "+ e);
		}
		
		for(Edge e : cp1.incidences()){
			System.out.println("inc of v1: " + e);
		}
		
		for(Edge e : cp2.incidences()){
			System.out.println("inc of v2: " + e);
		}
		
		for(Edge e : cp3.incidences()){
			System.out.println("inc of v3: " + e);
		}
		
		cp3.delete();

		for(Edge e : cp1.incidences()){
			System.out.println("inc of v1: " + e);
		}
		
		for(Edge e : cp2.incidences()){
			System.out.println("inc of v2: " + e);
		}
		
		for(int i = 0 ; i < 100000 ; i++){
			CarPark cp = graph.createCarPark();
			cp.set_capacity((int)(Math.random()*1000.0));
			cp.set_name("Hugo");
		}
		for (int i = 0 ; i < 200000; i++){
			Junction alpha = (Junction) graph.getVertex((int)((Math.random()*99999.0) +1));
			Junction omega = (Junction) graph.getVertex((int)((Math.random()*99999.0) +1));
			graph.createStreet(alpha, omega);
		}
		
		for(Vertex v : graph.vertices()){
			if(((Integer) v.getAttribute("capacity")) > 0 && ((Integer) v.getAttribute("capacity")) <100)
				System.out.println(v + " capacity: " + v.getAttribute("capacity"));
		}
	}
	
}
