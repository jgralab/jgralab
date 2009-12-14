package de.uni_koblenz.jgralab;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;

import de.uni_koblenz.ist.utilities.option_handler.OptionHandler;
import de.uni_koblenz.jgralab.impl.ProgressFunctionImpl;
import de.uni_koblenz.jgralab.schema.Attribute;

/**
 * Utility class to load multiple graphs conforming to the same schema into one
 * graph. The schema classes have to be in the CLASSPATH.
 * 
 * @author Tassilo Horn &lt;horn@uni-koblenz.de&gt;
 * 
 */
public class GraphMerge {

	private static OptionHandler optionHandler;

	/**
	 * @param args
	 * @throws GraphIOException
	 * @throws NoSuchFieldException
	 */
	public static void main(String[] args) throws GraphIOException,
			NoSuchFieldException {
		CommandLine cmd = processCommandLineOptions(args);

		String[] files = cmd.getArgs();

		if (files.length < 2) {
			System.err
					.println("GraphMerge makes no sence when given fewer than 2 graphs. Aborting.");
			System.exit(1);
		}

		new GraphMerge(cmd.getOptionValue('o'), cmd.getArgs());
	}

	private Graph mergedGraph;

	/**
	 * @return the mergedGraph
	 */
	public Graph getMergedGraph() {
		return mergedGraph;
	}

	private String outfile;
	private String[] graphFiles;
	private List<Graph> graphs;
	private HashMap<Vertex, Vertex> old2newVertices = new HashMap<Vertex, Vertex>();
	private HashMap<Edge, Edge> new2oldEdges = new HashMap<Edge, Edge>();

	private GraphMerge(String outfile, String... graphFiles)
			throws GraphIOException {
		if (graphFiles.length < 2) {
			throw new RuntimeException(
					"GraphMerge makes no sence when given fewer than 2 graphs. Aborting.");
		}
		this.outfile = outfile;
		this.graphFiles = graphFiles;
		graphs = new ArrayList<Graph>(graphFiles.length);

		// Called from command line, so do the full program.
		load();
		merge();
		save();
	}

	public GraphMerge(List<Graph> graphs) {
		this.graphs = graphs;

		// nothing to load.
		merge();
	}

	private void merge() {
		System.out.println("Merging graphs...");
		mergedGraph = graphs.remove(0);
		while (!graphs.isEmpty()) {
			old2newVertices.clear();
			new2oldEdges.clear();

			copyGraph(graphs.remove(0));

			System.out.println("Sorting incidences...");
			for (Vertex v : old2newVertices.values()) {
				v.sortIncidences(new Comparator<Edge>() {

					@Override
					public int compare(Edge e1, Edge e2) {
						Edge old1 = new2oldEdges.get(e1);
						Edge old2 = new2oldEdges.get(e2);
						if (old1.isBefore(old2)) {
							return -1;
						} else if (old2.isBefore(old1)) {
							return 1;
						}
						throw new RuntimeException(
								"Exception while sorting incidences.");
					}
				});
			}
		}
	}

	private void load() throws GraphIOException {
		for (String tgfile : graphFiles) {
			System.out.println("Loading " + tgfile);
			graphs.add(GraphIO.loadGraphFromFile(tgfile,
					new ProgressFunctionImpl()));
		}
	}

	private void save() throws GraphIOException {
		System.out.println("Saving merged graph to " + outfile);
		GraphIO.saveGraphToFile(outfile, mergedGraph,
				new ProgressFunctionImpl());
	}

	private void copyGraph(Graph g) {
		for (Vertex v : g.vertices()) {
			copyVertex(v);
		}
		for (Edge e : g.edges()) {
			copyEdge(e);
		}
	}

	@SuppressWarnings("unchecked")
	private void copyEdge(Edge e) {
		Edge newE = mergedGraph.createEdge((Class<? extends Edge>) e
				.getM1Class(), old2newVertices.get(e.getAlpha()),
				old2newVertices.get(e.getOmega()));
		copyAttributes(e, newE);

		new2oldEdges.put(newE, e);
		new2oldEdges.put(newE.getReversedEdge(), e.getReversedEdge());
	}

	@SuppressWarnings("unchecked")
	private void copyVertex(Vertex v) {
		Vertex newV = mergedGraph.createVertex((Class<? extends Vertex>) v
				.getM1Class());
		copyAttributes(v, newV);

		old2newVertices.put(v, newV);
	}

	private void copyAttributes(AttributedElement oldAE, AttributedElement newAE) {
		for (Attribute attr : oldAE.getAttributedElementClass()
				.getAttributeList()) {
			try {
				newAE.setAttribute(attr.getName(), oldAE.getAttribute(attr
						.getName()));
			} catch (NoSuchFieldException e) {
				throw new RuntimeException(
						"Exception while setting attribute values.", e);
			}
		}
	}

	private static CommandLine processCommandLineOptions(String[] args) {
		String toolString = GraphMerge.class.getSimpleName();
		String versionString = JGraLab.getInfo(false);
		optionHandler = new OptionHandler(toolString, versionString);

		Option outfile = new Option("o", "output-file", true,
				"(required): the output TG file");
		outfile.setArgName("out.tg");
		outfile.setRequired(true);
		optionHandler.addOption(outfile);

		optionHandler.setArgumentCount(Option.UNLIMITED_VALUES);
		optionHandler.setArgumentName("tgfile");
		optionHandler.setOptionalArgument(false);

		return optionHandler.parse(args);
	}
}
