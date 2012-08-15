package de.uni_koblenz.jgralabtest.greql.optimizer;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import org.junit.Test;

import de.uni_koblenz.jgralab.Graph;
import de.uni_koblenz.jgralab.GraphIO;
import de.uni_koblenz.jgralab.GraphIOException;
import de.uni_koblenz.jgralab.ImplementationType;
import de.uni_koblenz.jgralab.greql.optimizer.GraphInfo;
import de.uni_koblenz.jgralab.greql.optimizer.GraphInfoCreator;
import de.uni_koblenz.jgralab.schema.Schema;

public class GraphInfoCreatorTest {

	private static final String filename1 = "testit" + File.separator + "testgraphs" + File.separator + "graphInfoTestGraph1.tg";
	private static final String filename2 = "testit" + File.separator + "testgraphs" + File.separator + "graphInfoTestGraph2.tg";

	@Test
	public void test() throws GraphIOException{
		
		Schema schema = GraphIO.loadSchemaFromFile(filename1);
		Graph g1 = GraphIO.loadGraphFromFile(filename1, schema, ImplementationType.GENERIC, null);
		Graph g2 = GraphIO.loadGraphFromFile(filename2, schema, ImplementationType.GENERIC, null);
		ArrayList<Graph> graphs = new ArrayList<Graph>();
		graphs.add(g1);
		graphs.add(g2);
		GraphInfo gi = GraphInfoCreator.createGraphInfo(graphs);
		System.out.println(gi);
		gi.save("testit"+File.separator + "testdata"+File.separator+"testinfo.properties");
		
		double d = gi.getRelativeFrequencyOfVertexClass("junctions.Crossroad") 
				+ gi.getRelativeFrequencyOfVertexClass("localities.County")
				+ gi.getRelativeFrequencyOfVertexClass("localities.Village")
				+ gi.getRelativeFrequencyOfVertexClass("localities.Town");
	
		System.out.println("Sum vc: "+d);
	}
	
	@Test
	public void testLoad(){
		GraphInfo gi = GraphInfo.load("testit"+File.separator + "testdata"+File.separator+"testinfo.properties");
		System.out.println(gi);
	}

	@Test
	public void testConsole() throws IOException, GraphIOException{
		GraphInfoCreator.main(new String []{"-i",filename1,"-i",filename2,"-o", "testit"+File.separator + "testdata"+File.separator+"testinfo2.properties"});
	}
	
}
