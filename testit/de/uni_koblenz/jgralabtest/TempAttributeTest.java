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

package de.uni_koblenz.jgralabtest;

import java.util.ArrayList;
import java.util.List;

import junit.framework.TestCase;

import org.junit.Test;

import de.uni_koblenz.jgralab.Edge;
import de.uni_koblenz.jgralab.EdgeDirection;
import de.uni_koblenz.jgralab.Vertex;
import de.uni_koblenz.jgralab.graphmarker.GraphMarker;
import de.uni_koblenz.jgralabtest.schemas.citymap.CityMap;
import de.uni_koblenz.jgralabtest.schemas.citymap.CityMapSchema;
import de.uni_koblenz.jgralabtest.schemas.citymap.Intersection;
import de.uni_koblenz.jgralabtest.schemas.citymap.Street;

public class TempAttributeTest extends TestCase {

	CityMap graph = null;

	@Override
	public void setUp() throws Exception {
		graph = CityMapSchema.instance().loadCityMap(
				"testit/testgraphs/citymapgraph.tg");
	}

	@Test
	public void testTempAttribute() {
		GraphMarker<String> marker = new GraphMarker<String>(graph);
		System.out
				.println("Setting and getting temporary attribute belonging to the graph itself");
		marker.mark(graph, "successful");
		assertEquals("successful", marker.getMark(graph));

		System.out
				.println("Setting and getting temporary attribute belonging to a vertex");
		marker.mark(graph.getFirstVertex(), "successful");
		assertEquals("successful", marker.getMark(graph.getFirstVertex()));

		System.out
				.println("Setting and getting temporary attribute belonging to an edge");
		marker.mark(graph.getFirstEdgeInGraph(), "successful");
		assertEquals("successful", marker.getMark(graph.getFirstEdgeInGraph()));
	}

	public void testGenericForEachIncidence() {
		Vertex v = graph.getFirstVertex();
		List<Edge> edgeList = new ArrayList<Edge>();
		for (Edge p : v.incidences()) {
			edgeList.add(p);
		}
		assertEquals(2, edgeList.size());
		Street s = (Street) edgeList.get(0);
		assertEquals("e1", s.get_name());
		s = (Street) edgeList.get(1);
		assertEquals("e3", s.get_name());
	}

	public void testForEachStreet() {
		Intersection v = (Intersection) graph.getFirstVertex();
		List<Edge> edgeList = new ArrayList<Edge>();

		for (Edge p : v.getStreetIncidences()) {
			edgeList.add(p);
		}
		assertEquals(2, edgeList.size());
		Street s = (Street) edgeList.get(0);
		assertEquals("e1", s.get_name());
		s = (Street) edgeList.get(1);
		assertEquals("e3", s.get_name());
	}

	public void testForEachBridge() {
		Intersection v = (Intersection) graph.getFirstVertex();
		List<Edge> edgeList = new ArrayList<Edge>();

		for (Edge p : v.getBridgeIncidences()) {
			edgeList.add(p);
		}
		assertEquals(0, edgeList.size());
	}

	public void testForEachStreetDirection() {
		Intersection v = (Intersection) graph.getFirstVertex();
		List<Edge> edgeList = new ArrayList<Edge>();

		for (Edge p : v.getStreetIncidences(EdgeDirection.IN)) {
			edgeList.add(p);
		}
		assertEquals(0, edgeList.size());
	}

	public void testForEachStreetDirection2() {
		Intersection v = (Intersection) graph.getFirstVertex();
		List<Edge> edgeList = new ArrayList<Edge>();

		for (Edge p : v.getStreetIncidences(EdgeDirection.OUT)) {
			edgeList.add(p);
		}
		assertEquals(2, edgeList.size());
		Street s = (Street) edgeList.get(0);
		assertEquals("e1", s.get_name());
		s = (Street) edgeList.get(1);
		assertEquals("e3", s.get_name());
	}
}
