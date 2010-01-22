/**
 * 
 */
package de.uni_koblenz.jgralab.utilities.tgmerge;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;

import de.uni_koblenz.ist.utilities.option_handler.OptionHandler;
import de.uni_koblenz.jgralab.AttributedElement;
import de.uni_koblenz.jgralab.Edge;
import de.uni_koblenz.jgralab.Graph;
import de.uni_koblenz.jgralab.GraphIO;
import de.uni_koblenz.jgralab.GraphIOException;
import de.uni_koblenz.jgralab.JGraLab;
import de.uni_koblenz.jgralab.Vertex;
import de.uni_koblenz.jgralab.impl.ProgressFunctionImpl;
import de.uni_koblenz.jgralab.schema.Attribute;
import de.uni_koblenz.jgralab.schema.Schema;

/**
 * @author Tassilo Horn &lt;horn@uni-koblenz.de&gt;
 * 
 */
public class TGMerge {
	private Set<Graph> graphs = new HashSet<Graph>();
	private Graph targetGraph;
	private Map<Vertex, Vertex> old2NewMap = new HashMap<Vertex, Vertex>();

	public TGMerge(Set<Graph> graphs) {
		if (graphs.size() < 2) {
			throw new RuntimeException(
					"Merging makes no sense with less than 2 graphs.");
		}

		Schema s = graphs.iterator().next().getSchema();
		for (Graph g : graphs) {
			if (g.getSchema() != s) {
				throw new RuntimeException(
						"It's only possible to merge graphs conforming to one schema.");
			}
		}

		this.graphs = graphs;
	}

	/**
	 * @param args
	 * @throws GraphIOException
	 */
	public static void main(String[] args) throws GraphIOException {
		CommandLine cmdl = processCommandLineOptions(args);

		String outputFilename = cmdl.getOptionValue('o').trim();

		HashSet<Graph> graphs = new HashSet<Graph>();
		for (String g : cmdl.getArgs()) {
			graphs
					.add(GraphIO.loadGraphFromFile(g,
							new ProgressFunctionImpl()));
		}

		TGMerge tgmerge = new TGMerge(graphs);
		Graph merged = tgmerge.merge();

		GraphIO.saveGraphToFile(outputFilename, merged,
				new ProgressFunctionImpl());
	}

	private Graph merge() {
		int ecount = 0, vcount = 0;
		for (Graph g : graphs) {
			ecount += g.getECount();
			vcount += g.getVCount();
		}

		Method create = graphs.iterator().next().getSchema()
				.getGraphCreateMethod(false);
		try {
			targetGraph = (Graph) create.invoke(null, null, vcount, ecount);
		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		}

		for (Graph g : graphs) {
			copyVertices(g);
			copyEdges(g);
			old2NewMap.clear();
		}

		return null;
	}

	private void copyEdges(Graph g) {
		for (Edge e : g.edges()) {
			Vertex start = old2NewMap.get(e.getAlpha());
			Vertex end = old2NewMap.get(e.getOmega());

			Edge newEdge = targetGraph.createEdge(e.getClass(), start, end);
			copyAttributes(e, newEdge);
		}
	}

	private void copyVertices(Graph g) {
		for (Vertex v : g.vertices()) {
			Vertex newV = targetGraph.createVertex(v.getClass());
			copyAttributes(v, newV);
		}

	}

	private void copyAttributes(AttributedElement oldAttrElem,
			AttributedElement newAttrElem) {
		for (Attribute attr : oldAttrElem.getAttributedElementClass()
				.getAttributeList()) {
			try {
				newAttrElem.setAttribute(attr.getName(), oldAttrElem
						.getAttribute(attr.getName()));
			} catch (NoSuchFieldException e) {
				e.printStackTrace();
				throw new RuntimeException(e);
			}
		}
	}

	private static CommandLine processCommandLineOptions(String[] args) {
		String toolString = "java " + TGMerge.class.getName();
		String versionString = JGraLab.getInfo(false);
		OptionHandler oh = new OptionHandler(toolString, versionString);

		Option output = new Option("o", "outTG", true,
				"(required): output TG file");
		output.setRequired(true);
		output.setArgName("outputTG");
		oh.addOption(output);

		oh.setArgumentCount(Option.UNLIMITED_VALUES);
		oh.setArgumentName("inputTG");
		oh.setOptionalArgument(false);

		return oh.parse(args);
	}
}
