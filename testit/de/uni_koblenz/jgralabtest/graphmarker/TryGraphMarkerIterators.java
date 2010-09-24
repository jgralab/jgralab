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
package de.uni_koblenz.jgralabtest.graphmarker;

import java.util.Iterator;

import de.uni_koblenz.jgralab.Edge;
import de.uni_koblenz.jgralab.GraphElement;
import de.uni_koblenz.jgralab.Vertex;
import de.uni_koblenz.jgralab.graphmarker.ArrayEdgeMarker;
import de.uni_koblenz.jgralab.graphmarker.ArrayVertexMarker;
import de.uni_koblenz.jgralab.graphmarker.BitSetEdgeMarker;
import de.uni_koblenz.jgralab.graphmarker.BitSetVertexMarker;
import de.uni_koblenz.jgralab.graphmarker.DoubleEdgeMarker;
import de.uni_koblenz.jgralab.graphmarker.DoubleVertexMarker;
import de.uni_koblenz.jgralab.graphmarker.IntegerEdgeMarker;
import de.uni_koblenz.jgralab.graphmarker.IntegerVertexMarker;
import de.uni_koblenz.jgralab.graphmarker.LongEdgeMarker;
import de.uni_koblenz.jgralab.graphmarker.LongVertexMarker;
import de.uni_koblenz.jgralab.graphmarker.SubGraphMarker;
import de.uni_koblenz.jgralabtest.schemas.minimal.Link;
import de.uni_koblenz.jgralabtest.schemas.minimal.MinimalGraph;
import de.uni_koblenz.jgralabtest.schemas.minimal.MinimalSchema;
import de.uni_koblenz.jgralabtest.schemas.minimal.Node;

public class TryGraphMarkerIterators {
	private static ArrayVertexMarker<String> arrayVertexMarker;
	private static ArrayEdgeMarker<String> arrayEdgeMarker;
	private static IntegerVertexMarker integerVertexMarker;
	private static IntegerEdgeMarker integerEdgeMarker;
	private static DoubleVertexMarker doubleVertexMarker;
	private static DoubleEdgeMarker doubleEdgeMarker;
	private static LongVertexMarker longVertexMarker;
	private static LongEdgeMarker longEdgeMarker;
	private static BitSetVertexMarker bitSetVertexMarker;
	private static BitSetEdgeMarker bitSetEdgeMarker;
	private static SubGraphMarker subGraphMarker;

	public static void main(String[] args) {
		MinimalGraph graph = MinimalSchema.instance().createMinimalGraph();
		Node[] nodes = new Node[10];
		for (int i = 0; i < nodes.length; i++) {
			nodes[i] = graph.createNode();
		}
		Link[] links = new Link[10];
		for (int i = 0; i < links.length; i++) {
			links[i] = graph.createLink(nodes[i], nodes[10 - i - 1]);
		}
		arrayVertexMarker = new ArrayVertexMarker<String>(graph);
		arrayEdgeMarker = new ArrayEdgeMarker<String>(graph);
		integerVertexMarker = new IntegerVertexMarker(graph);
		integerEdgeMarker = new IntegerEdgeMarker(graph);
		doubleVertexMarker = new DoubleVertexMarker(graph);
		doubleEdgeMarker = new DoubleEdgeMarker(graph);
		longVertexMarker = new LongVertexMarker(graph);
		longEdgeMarker = new LongEdgeMarker(graph);
		bitSetVertexMarker = new BitSetVertexMarker(graph);
		bitSetEdgeMarker = new BitSetEdgeMarker(graph);
		subGraphMarker = new SubGraphMarker(graph);

		applyIterators("No marks:");

		for (int i = 0; i < nodes.length; i++) {
			arrayVertexMarker.mark(nodes[i], "StringMark: " + nodes[i]);
			integerVertexMarker.mark(nodes[i], nodes[i].getId());
			doubleVertexMarker.mark(nodes[i], nodes[i].getId() + 0.5);
			longVertexMarker.mark(nodes[i], 1000l + nodes[i].getId());
			bitSetVertexMarker.mark(nodes[i]);
			subGraphMarker.mark(nodes[i]);
		}
		for (int i = 0; i < links.length; i++) {
			arrayEdgeMarker.mark(links[i], "StringMark: " + links[i]);
			integerEdgeMarker.mark(links[i], links[i].getId());
			doubleEdgeMarker.mark(links[i], links[i].getId() + 0.5);
			longEdgeMarker.mark(links[i], 1000l + links[i].getId());
			bitSetEdgeMarker.mark(links[i]);
			subGraphMarker.mark(links[i]);
		}

		applyIterators("All marked");

		unmark(nodes, links, 0);

		applyIterators("first unmarked");

		unmark(nodes, links, 9);

		applyIterators("last unmarked");

		unmark(nodes, links, 3);
		unmark(nodes, links, 4);
		unmark(nodes, links, 5);

		applyIterators("gap from 4 to 6");

	}

	private static void unmark(Node[] nodes, Link[] links, int i) {
		arrayVertexMarker.removeMark(nodes[i]);
		integerVertexMarker.removeMark(nodes[i]);
		doubleVertexMarker.removeMark(nodes[i]);
		longVertexMarker.removeMark(nodes[i]);
		bitSetVertexMarker.removeMark(nodes[i]);
		subGraphMarker.removeMark(nodes[i]);

		arrayEdgeMarker.removeMark(links[i]);
		integerEdgeMarker.removeMark(links[i]);
		doubleEdgeMarker.removeMark(links[i]);
		longEdgeMarker.removeMark(links[i]);
		bitSetEdgeMarker.removeMark(links[i]);
		subGraphMarker.removeMark(links[i]);
	}

	private static void applyIterators(String message) {
		System.out.println(message);
		// launch all iterators
		Iterator<Vertex> arrayVertexIterator = arrayVertexMarker
				.getMarkedElements().iterator();
		Iterator<Vertex> integerVertexIterator = integerVertexMarker
				.getMarkedElements().iterator();
		Iterator<Vertex> doubleVertexIterator = doubleVertexMarker
				.getMarkedElements().iterator();
		Iterator<Vertex> longVertexIterator = longVertexMarker
				.getMarkedElements().iterator();
		Iterator<Vertex> bitSetVertexIterator = bitSetVertexMarker
				.getMarkedElements().iterator();
		Iterator<Edge> arrayEdgeIterator = arrayEdgeMarker.getMarkedElements()
				.iterator();
		Iterator<Edge> integerEdgeIterator = integerEdgeMarker
				.getMarkedElements().iterator();
		Iterator<Edge> doubleEdgeIterator = doubleEdgeMarker
				.getMarkedElements().iterator();
		Iterator<Edge> longEdgeIterator = longEdgeMarker.getMarkedElements()
				.iterator();
		Iterator<Edge> bitSetEdgeIterator = bitSetEdgeMarker
				.getMarkedElements().iterator();
		Iterator<GraphElement> subGraphIterator = subGraphMarker
				.getMarkedElements().iterator();

		while (arrayVertexIterator.hasNext() || integerVertexIterator.hasNext()
				|| doubleVertexIterator.hasNext()
				|| longVertexIterator.hasNext()
				|| bitSetVertexIterator.hasNext()
				|| arrayEdgeIterator.hasNext() || integerEdgeIterator.hasNext()
				|| doubleEdgeIterator.hasNext() || longEdgeIterator.hasNext()
				|| bitSetEdgeIterator.hasNext()) {
			// System.out.println("ArrayVertex: " +
			// arrayVertexIterator.hasNext());
			// System.out.println("IntegerVertex: "
			// + integerVertexIterator.hasNext());
			// System.out.println("DoubleVertex: "
			// + doubleVertexIterator.hasNext());
			// System.out.println("LongVertex: " +
			// longVertexIterator.hasNext());
			// System.out.println("BitSetVertex: "
			// + bitSetVertexIterator.hasNext());
			// System.out.println("ArrayEdge: " + arrayEdgeIterator.hasNext());
			// System.out.println("IntegerEdge: " +
			// integerEdgeIterator.hasNext());
			// System.out.println("DoubleEdge: " +
			// doubleEdgeIterator.hasNext());
			// System.out.println("LongEdge: " + longEdgeIterator.hasNext());
			// System.out.println("BitSetEdge: " +
			// bitSetEdgeIterator.hasNext());

			System.out.println();
			System.out.println(arrayVertexIterator.next());
			System.out.println(integerVertexIterator.next());
			System.out.println(doubleVertexIterator.next());
			System.out.println(longVertexIterator.next());
			System.out.println(bitSetVertexIterator.next());
			System.out.println();
			System.out.println(arrayEdgeIterator.next());
			System.out.println(integerEdgeIterator.next());
			System.out.println(doubleEdgeIterator.next());
			System.out.println(longEdgeIterator.next());
			System.out.println(bitSetEdgeIterator.next());
		}

		System.out.println();
		while (subGraphIterator.hasNext()) {
			System.out.println(subGraphIterator.next());
		}

		System.out.println("Done");
	}
}
