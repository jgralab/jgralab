/*
 * JGraLab - The Java graph laboratory
 * (c) 2006-2010 Institute for Software Technology
 *               University of Koblenz-Landau, Germany
 *
 *               ist@uni-koblenz.de
 *
 * Please report bugs to http://serres.uni-koblenz.de/bugzilla
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package de.uni_koblenz.jgralab.utilities.tgmerge;

import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

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
	private List<Graph> additionalGraphs = new LinkedList<Graph>();
	private Graph targetGraph;
	private Map<Vertex, Vertex> old2NewVertices = new HashMap<Vertex, Vertex>();
	private Map<Edge, Edge> new2OldEdges = new HashMap<Edge, Edge>();

	/**
	 * @param graphs
	 *            a list of graphs. The second to last graph will be merged into
	 *            the first one.
	 */
	public TGMerge(List<Graph> graphs) {
		this(graphs.toArray(new Graph[graphs.size()]));
	}

	/**
	 * @param graphs
	 *            an array of graphs. The second to last graph will be merged
	 *            into the first one.
	 */
	public TGMerge(Graph[] graphs) {
		if (graphs.length < 2) {
			throw new RuntimeException(
					"Merging makes no sense with less than 2 Graphs.");
		}

		Schema s = graphs[0].getSchema();
		for (Graph g : graphs) {
			if (g.getSchema() != s) {
				throw new RuntimeException(
						"It's only possible to merge additionalGraphs conforming to one schema.");
			}
		}
		targetGraph = graphs[0];
		additionalGraphs = new LinkedList<Graph>();
		for (int i = 1; i < graphs.length; i++) {
			additionalGraphs.add(graphs[i]);
		}
	}

	/**
	 * @param args
	 * @throws GraphIOException
	 */
	public static void main(String[] args) throws GraphIOException {
		CommandLine cmdl = processCommandLineOptions(args);

		String outputFilename = cmdl.getOptionValue('o').trim();

		List<Graph> graphs = new LinkedList<Graph>();
		for (String g : cmdl.getArgs()) {
			graphs.add(GraphIO.loadGraphFromFileWithStandardSupport(g,
					new ProgressFunctionImpl()));
		}

		TGMerge tgmerge = new TGMerge(graphs);
		Graph merged = tgmerge.merge();

		GraphIO.saveGraphToFile(outputFilename, merged,
				new ProgressFunctionImpl());
	}

	public Graph merge() {
		System.out.println("TargetGraph is '" + targetGraph.getId() + "'.");
		for (Graph g : additionalGraphs) {
			System.out.println("Merging graph '" + g.getId() + "'...");
			copyVertices(g);
			copyEdges(g);
			sortIncidences();
			old2NewVertices.clear();
			new2OldEdges.clear();
		}

		return targetGraph;
	}

	private void sortIncidences() {
		System.out.println("Sorting incidences...");
		for (Vertex v : old2NewVertices.values()) {
			v.sortIncidences(new Comparator<Edge>() {
				@Override
				public int compare(Edge e1, Edge e2) {
					Edge old1 = new2OldEdges.get(e1);
					Edge old2 = new2OldEdges.get(e2);
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

	@SuppressWarnings("unchecked")
	private void copyEdges(Graph g) {
		System.out.println("Copying Edges...");
		for (Edge e : g.edges()) {
			Vertex start = old2NewVertices.get(e.getAlpha());
			Vertex end = old2NewVertices.get(e.getOmega());
			Edge newEdge = targetGraph.createEdge((Class<? extends Edge>) e
					.getM1Class(), start, end);

			copyAttributes(e, newEdge);

			new2OldEdges.put(newEdge, e);
			new2OldEdges.put(newEdge.getReversedEdge(), e.getReversedEdge());
		}
	}

	@SuppressWarnings("unchecked")
	private void copyVertices(Graph g) {
		System.out.println("Copying Vertices...");
		for (Vertex v : g.vertices()) {
			Vertex newVertex = targetGraph
					.createVertex((Class<? extends Vertex>) v.getM1Class());

			copyAttributes(v, newVertex);

			old2NewVertices.put(v, newVertex);
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
