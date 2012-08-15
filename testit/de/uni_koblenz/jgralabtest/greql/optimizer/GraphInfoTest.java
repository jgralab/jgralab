package de.uni_koblenz.jgralabtest.greql.optimizer;

import java.io.File;
import java.util.ArrayList;

import org.junit.Test;

import de.uni_koblenz.jgralab.Graph;
import de.uni_koblenz.jgralab.GraphIO;
import de.uni_koblenz.jgralab.GraphIOException;
import de.uni_koblenz.jgralab.ImplementationType;
import de.uni_koblenz.jgralab.greql.GreqlQuery;
import de.uni_koblenz.jgralab.greql.optimizer.GraphInfo;
import de.uni_koblenz.jgralab.greql.optimizer.GraphInfoCreator;
import de.uni_koblenz.jgralab.greql.optimizer.VariableDeclarationOrderOptimizer;
import de.uni_koblenz.jgralab.schema.Schema;


public class GraphInfoTest {

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
		
		
		VariableDeclarationOrderOptimizer optimizer = new VariableDeclarationOrderOptimizer();
		//optimizer.setGraphInfo(gi);
		//GreqlQuery query = GreqlQuery.createQuery("from a : V{localities.County}, b : V{junctions.Crossroad}"
		//											+" with contains(a-->{localities.HasCapital}-->{localities.ContainsCrossroad},b)" +
		//											"report a as \"county\", b as \"crossroad\" end", 
		//											optimizer);
		GreqlQuery query = GreqlQuery.createQuery("from a : V{localities.Village,localities.Town}, b : V{junctions.Crossroad}"							
				+" with contains(a-->{localities.ContainsCrossroad},b)" +
				"report a as \"local\", b as \"cr\" end", 
				optimizer);
		System.out.println("res: " +query.evaluate(g1));
		
	}
	
}
