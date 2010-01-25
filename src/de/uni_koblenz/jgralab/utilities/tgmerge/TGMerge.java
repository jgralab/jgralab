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

import java.lang.reflect.Method;
import java.util.Comparator;
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
	private Map<Vertex, Vertex> old2NewVertices = new HashMap<Vertex, Vertex>();
	private Map<Edge, Edge> new2OldEdges = new HashMap<Edge, Edge>();

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
