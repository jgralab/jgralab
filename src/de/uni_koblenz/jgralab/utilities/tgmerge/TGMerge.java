/*
 * JGraLab - The Java Graph Laboratory
 * 
 * Copyright (C) 2006-2010 Institute for Software Technology
 *                         University of Koblenz-Landau, Germany
 *                         ist@uni-koblenz.de
 * 
 * This program is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as published by the
 * Free Software Foundation; either version 3 of the License, or (at your
 * option) any later version.
 * 
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General
 * Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, see <http://www.gnu.org/licenses>.
 * 
 * Additional permission under GNU GPL version 3 section 7
 * 
 * If you modify this Program, or any covered work, by linking or combining
 * it with Eclipse (or a modified version of that program or an Eclipse
 * plugin), containing parts covered by the terms of the Eclipse Public
 * License (EPL), the licensors of this Program grant you additional
 * permission to convey the resulting work.  Corresponding Source for a
 * non-source form of such a combination shall include the source code for
 * the parts of JGraLab used as well as that of the covered work.
 */
package de.uni_koblenz.jgralab.utilities.tgmerge;

import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;

import de.uni_koblenz.ist.utilities.option_handler.OptionHandler;
import de.uni_koblenz.jgralab.AttributedElement;
import de.uni_koblenz.jgralab.Edge;
import de.uni_koblenz.jgralab.Graph;
import de.uni_koblenz.jgralab.GraphElement;
import de.uni_koblenz.jgralab.GraphIO;
import de.uni_koblenz.jgralab.GraphIOException;
import de.uni_koblenz.jgralab.JGraLab;
import de.uni_koblenz.jgralab.Vertex;
import de.uni_koblenz.jgralab.graphmarker.AbstractGraphMarker;
import de.uni_koblenz.jgralab.impl.ConsoleProgressFunction;
import de.uni_koblenz.jgralab.schema.Attribute;
import de.uni_koblenz.jgralab.schema.Schema;

/**
 * @author Tassilo Horn &lt;horn@uni-koblenz.de&gt;
 * 
 */
public class TGMerge {
	private List<Graph> additionalGraphs = new LinkedList<Graph>();
	private List<AbstractGraphMarker<?>> additionalGraphMarkers = new LinkedList<AbstractGraphMarker<?>>();

	private Graph targetGraph;
	private Map<Vertex, Vertex> old2NewVertices = new HashMap<Vertex, Vertex>();
	private Map<Vertex, Vertex> new2OldVertices = new HashMap<Vertex, Vertex>();
	private Map<Edge, Edge> new2OldEdges = new HashMap<Edge, Edge>();

	/**
	 * Remembers the positions of all copied graph elements in their original
	 * graph to speed up sorting of vertices and edges.
	 */
	private Map<GraphElement, Integer> copiedGraphPositions = new HashMap<GraphElement, Integer>();

	/**
	 * Remembers the positions of all target graph elements before the elements
	 * of another graph are merged into to speed up sorting of vertices and
	 * edges.
	 */
	private Map<GraphElement, Integer> targetGraphPositions = new HashMap<GraphElement, Integer>();

	private static Logger log = JGraLab.getLogger(TGMerge.class.getPackage()
			.getName());

	/**
	 * @param graphs
	 *            a list of graphs. The second to last graph will be merged into
	 *            the first one.
	 */
	public TGMerge(List<Graph> graphs) {
		this(graphs.toArray(new Graph[graphs.size()]));
	}

	public TGMerge(Graph g, AbstractGraphMarker<?>... markers) {
		if (markers.length == 0) {
			throw new RuntimeException("No marker given!");
		}
		targetGraph = g;
		for (AbstractGraphMarker<?> m : markers) {
			additionalGraphMarkers.add(m);
		}
	}

	/**
	 * @param graphs
	 *            an array of graphs. The second to last graph will be merged
	 *            into the first one.
	 */
	public TGMerge(Graph... graphs) {
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
					new ConsoleProgressFunction()));
		}

		TGMerge tgmerge = new TGMerge(graphs);
		Graph merged = tgmerge.merge();

		GraphIO.saveGraphToFile(outputFilename, merged,
				new ConsoleProgressFunction());
	}

	public Graph merge() {
		log.fine("TargetGraph is '" + targetGraph.getId() + "'.");
		for (Graph g : additionalGraphs) {
			log.fine("Merging graph '" + g.getId() + "'...");
			rememberTargetGraphPositions();
			rememberCopiedGraphPositions(g);
			for (Vertex v : g.vertices()) {
				copyVertex(v);
			}
			for (Edge e : g.edges()) {
				copyEdge(e);
			}
			sortVertices();
			sortEdges();
			sortIncidences();
			resetMaps();
		}
		for (AbstractGraphMarker<?> marker : additionalGraphMarkers) {
			log.fine("Merging GraphMarker '" + marker + "'...");
			rememberTargetGraphPositions();
			rememberCopiedGraphPositions(marker.getGraph());
			for (AttributedElement ae : marker.getMarkedElements()) {
				if (ae instanceof Vertex) {
					copyVertex((Vertex) ae);
				}
			}
			for (AttributedElement ae : marker.getMarkedElements()) {
				if (ae instanceof Edge) {
					copyEdge((Edge) ae);
				}
			}
			sortVertices();
			sortEdges();
			sortIncidences();
			resetMaps();
		}

		return targetGraph;
	}

	private void resetMaps() {
		old2NewVertices.clear();
		new2OldVertices.clear();
		new2OldEdges.clear();
		copiedGraphPositions.clear();
		targetGraphPositions.clear();
	}

	private void rememberCopiedGraphPositions(Graph g) {
		int pos = 0;
		for (Vertex v : g.vertices()) {
			copiedGraphPositions.put(v, ++pos);
		}
		pos = 0;
		for (Edge e : g.edges()) {
			copiedGraphPositions.put(e, ++pos);
		}
	}

	private void rememberTargetGraphPositions() {
		int pos = 0;
		for (Vertex v : targetGraph.vertices()) {
			targetGraphPositions.put(v, ++pos);
		}
		pos = 0;
		for (Edge e : targetGraph.edges()) {
			targetGraphPositions.put(e, ++pos);
		}
	}

	private class VertexComparator implements Comparator<Vertex> {
		long compareCount = 0;

		@Override
		public int compare(Vertex v1, Vertex v2) {
			compareCount++;
			if (new2OldVertices.containsKey(v1)
					&& new2OldVertices.containsKey(v2)) {
				// Both vertices were copied
				Vertex ov1 = new2OldVertices.get(v1);
				Vertex ov2 = new2OldVertices.get(v2);
				return copiedGraphPositions.get(ov1)
						- copiedGraphPositions.get(ov2);
			} else if (new2OldVertices.containsKey(v1)
					&& !new2OldVertices.containsKey(v2)) {
				// Only v1 is a copy, so it should come after v2.
				return 1;
			} else if (!new2OldVertices.containsKey(v1)
					&& new2OldVertices.containsKey(v2)) {
				// Only v2 is a copy, so v1 should come before v2.
				return -1;
			} else if (!new2OldVertices.containsKey(v1)
					&& !new2OldVertices.containsKey(v2)) {
				// Neither v1 nor v2 is a copy, so keep stable
				return targetGraphPositions.get(v1)
						- targetGraphPositions.get(v2);
			}
			throw new RuntimeException("Exception while sorting vertices.");
		}
	}

	private void sortVertices() {
		log.fine("Sorting " + targetGraph.getVCount() + " vertices...");
		VertexComparator vc = new VertexComparator();
		targetGraph.sortVertices(vc);
		log.fine(vc.compareCount + " comparisons were needed to sort "
				+ targetGraph.getVCount() + " vertices.");
	}

	private class EdgeComparator implements Comparator<Edge> {
		long compareCount = 0;

		@Override
		public int compare(Edge e1, Edge e2) {
			compareCount++;
			if (new2OldEdges.containsKey(e1) && new2OldEdges.containsKey(e2)) {
				// Both vertices were copied, so keep the order of the
				// original graph.
				Edge oe1 = new2OldEdges.get(e1);
				Edge oe2 = new2OldEdges.get(e2);
				return copiedGraphPositions.get(oe1)
						- copiedGraphPositions.get(oe2);
			} else if (new2OldEdges.containsKey(e1)
					&& !new2OldEdges.containsKey(e2)) {
				// Only e1 is a copy, so it should come after e2.
				return 1;
			} else if (!new2OldEdges.containsKey(e1)
					&& new2OldEdges.containsKey(e2)) {
				// Only e2 is a copy, so e1 should come before e2.
				return -1;
			} else if (!new2OldEdges.containsKey(e1)
					&& !new2OldEdges.containsKey(e2)) {
				// Neither e1 nor e2 is a copy, so keep stable
				return targetGraphPositions.get(e1)
						- targetGraphPositions.get(e2);
			}
			throw new RuntimeException("Exception while sorting edges.");
		}
	}

	private void sortEdges() {
		log.fine("Sorting " + targetGraph.getECount() + " edges...");
		EdgeComparator ec = new EdgeComparator();
		targetGraph.sortEdges(ec);
		log.fine(ec.compareCount + " comparisons were needed to sort "
				+ targetGraph.getECount() + " edges.");
	}

	private void sortIncidences() {
		log.fine("Sorting incidences...");
		for (Vertex v : old2NewVertices.values()) {
			v.sortIncidences(new Comparator<Edge>() {
				@Override
				public int compare(Edge e1, Edge e2) {
					Edge old1 = new2OldEdges.get(e1);
					Edge old2 = new2OldEdges.get(e2);
					if (old1.isBeforeIncidence(old2)) {
						return -1;
					} else if (old2.isBeforeIncidence(old1)) {
						return 1;
					}
					throw new RuntimeException(
							"Exception while sorting incidences.");
				}
			});
		}
	}

	@SuppressWarnings("unchecked")
	private void copyEdge(Edge e) {
		Vertex start = old2NewVertices.get(e.getAlpha());
		Vertex end = old2NewVertices.get(e.getOmega());
		Edge newEdge = targetGraph.createEdge(
				(Class<? extends Edge>) e.getM1Class(), start, end);

		copyAttributes(e, newEdge);

		new2OldEdges.put(newEdge, e);
		new2OldEdges.put(newEdge.getReversedEdge(), e.getReversedEdge());
	}

	@SuppressWarnings("unchecked")
	private void copyVertex(Vertex v) {
		Vertex newVertex = targetGraph.createVertex((Class<? extends Vertex>) v
				.getM1Class());

		copyAttributes(v, newVertex);

		old2NewVertices.put(v, newVertex);
		new2OldVertices.put(newVertex, v);
	}

	private void copyAttributes(AttributedElement oldAttrElem,
			AttributedElement newAttrElem) {
		for (Attribute attr : oldAttrElem.getAttributedElementClass()
				.getAttributeList()) {
			newAttrElem.setAttribute(attr.getName(),
					oldAttrElem.getAttribute(attr.getName()));
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
