package de.uni_koblenz.jgralab.greql.optimizer;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.logging.Level;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;

import de.uni_koblenz.ist.utilities.option_handler.OptionHandler;
import de.uni_koblenz.jgralab.Edge;
import de.uni_koblenz.jgralab.Graph;
import de.uni_koblenz.jgralab.GraphIO;
import de.uni_koblenz.jgralab.GraphIOException;
import de.uni_koblenz.jgralab.ImplementationType;
import de.uni_koblenz.jgralab.JGraLab;
import de.uni_koblenz.jgralab.Vertex;
import de.uni_koblenz.jgralab.schema.EdgeClass;
import de.uni_koblenz.jgralab.schema.Schema;
import de.uni_koblenz.jgralab.schema.VertexClass;

public class GraphInfoCreator {

	private static String OPTION_FILENAME_GRAPH = "i";
	private static String OPTION_FILENAME_INFO = "o";

	public static CommandLine processCommandLineOptions(String[] args) {
		String toolString = "java " + GraphInfoCreator.class.getName();
		String versionString = JGraLab.getInfo(false);

		OptionHandler oh = new OptionHandler(toolString, versionString);

		// OPTION_FILENAME_GRAPH = "i";
		Option input = new Option(OPTION_FILENAME_GRAPH, "input", true,
				"(required): Graph to create GraphInfo for.");
		input.setRequired(true);
		input.setArgName("filename");
		oh.addOption(input);

		// OPTION_FILENAME_INFO = "o";
		Option output = new Option(OPTION_FILENAME_INFO, "output", true,
				"(required): filename for saving the GraphInfo");
		output.setRequired(true);
		output.setArgName("filename");
		oh.addOption(output);

		return oh.parse(args);
	}

	public static void main(String[] args) throws IOException, GraphIOException {

		System.out.println("GraphInfo Creator");
		System.out.println("=========");
		JGraLab.setLogLevel(Level.OFF);

		// Retrieving all command line options
		CommandLine cli = processCommandLineOptions(args);

		assert cli != null : "No CommandLine object has been generated!";

		String[] graphFilenames = cli.getOptionValues(OPTION_FILENAME_GRAPH);
		ArrayList<Graph> graphs = new ArrayList<Graph>();
		Schema schema = GraphIO.loadSchemaFromFile(graphFilenames[0]);
		for (String fn : graphFilenames) {

			System.out.println("load graph " + fn);
			graphs.add(GraphIO.loadGraphFromFile(fn, schema,
					ImplementationType.GENERIC, null));
		}
		System.out.println("Graphs: " + graphs);
		GraphInfo gi = GraphInfoCreator.createGraphInfo(graphs);
		System.out.println(gi);
		gi.save(cli.getOptionValue(OPTION_FILENAME_INFO));
		
	}
	
	/**
	 * Analyzes the given {@link Graph}s to retrieve the ratio between vertices
	 * of different {@link VertexClasses} and edges of different
	 * {@link EdgeClasses}
	 * 
	 * @param graphs
	 *            the {@link Graph}s to analyze
	 * @return a {@link GraphInfo} object containing the result
	 */
	public static GraphInfo createGraphInfo(ArrayList<Graph> graphs) {
		double vertexCount = 0.0d;
		double edgeCount = 0.0d;
		HashMap<String, Double> relativeFrequencyOfVCSum = new HashMap<String, Double>();
		HashMap<String, Double> relativeFrequencyOfECSum = new HashMap<String, Double>();

		// Initialize sums in maps
		Schema schema = graphs.get(0).getSchema();
		for (VertexClass vc : schema.getGraphClass().getVertexClasses()) {
			relativeFrequencyOfVCSum.put(vc.getQualifiedName(), 0.0d);
		}
		for (EdgeClass ec : schema.getGraphClass().getEdgeClasses()) {
			relativeFrequencyOfECSum.put(ec.getQualifiedName(), 0.0d);
		}

		// Sum up over all graphs
		for (Graph g : graphs) {

			// vCount and eCount
			vertexCount += ((double)g.getVCount()) / graphs.size();
			edgeCount += ((double)g.getECount()) / graphs.size();
			
			// Ratio of VertexClasses
			for (VertexClass vc : schema.getGraphClass().getVertexClasses()) {
				Iterator<Vertex> vertexIt = g.vertices(vc).iterator();
				int count = 0;
				g.getVCount();
				while (vertexIt.hasNext()) {
					vertexIt.next();
					count++;
				}
				relativeFrequencyOfVCSum.put(
						vc.getQualifiedName(),
						relativeFrequencyOfVCSum.get(vc.getQualifiedName())
								+ ((count / (double) g.getVCount()) / graphs
										.size()));
			}

			// Ratio of EdgeClasses
			for (EdgeClass ec : schema.getGraphClass().getEdgeClasses()) {
				Iterator<Edge> edgeIt = g.edges(ec).iterator();
				int count = 0;
				while (edgeIt.hasNext()) {
					edgeIt.next();
					count++;
				}
				relativeFrequencyOfECSum.put(
						ec.getQualifiedName(),
						relativeFrequencyOfECSum.get(ec.getQualifiedName())
								+ ((count / (double) g.getECount()) / graphs
										.size()));
			}
		}

		GraphInfo gi = new GraphInfo((int)vertexCount, (int)edgeCount, schema.getQualifiedName(), schema
				.getGraphClass().getVertexClassCount(), schema.getGraphClass()
				.getEdgeClassCount(), relativeFrequencyOfVCSum,
				relativeFrequencyOfECSum);

		return gi;

	}

}
