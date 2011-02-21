/*
 * JGraLab - The Java Graph Laboratory
 * 
 * Copyright (C) 2006-2011 Institute for Software Technology
 *                         University of Koblenz-Landau, Germany
 *                         ist@uni-koblenz.de
 * 
 * For bug reports, documentation and further information, visit
 * 
 *                         http://jgralab.uni-koblenz.de
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

package de.uni_koblenz.jgralab.greql2.evaluator.costmodel;

import de.uni_koblenz.jgralab.Graph;
import de.uni_koblenz.jgralab.schema.GraphClass;

/**
 * This class is needed to propagate the size of the currently used graph along
 * different calculate methods
 * 
 * @author ist@uni-koblenz.de
 */
public class GraphSize {

	private long vertexCount;

	private long edgeCount;

	private int knownVertexTypes;

	private int knownEdgeTypes;

	private double averageEdgeSubclasses = 2;

	private double averageVertexSubclasses = 2;

	/**
	 * constructs a new GraphSize object for the given graph
	 * 
	 * @param dataGraph
	 *            the datagaph which is base of this GraphSize object
	 */
	public GraphSize(Graph dataGraph) {
		vertexCount = dataGraph.getVCount();
		edgeCount = dataGraph.getECount();
		GraphClass graphClass = (GraphClass) dataGraph
				.getAttributedElementClass();
		knownVertexTypes = graphClass.getVertexClassCount();
		knownEdgeTypes = graphClass.getEdgeClassCount();
	}

	/**
	 * constructs a new GraphSize object with the given number of vertices and
	 * edges
	 * 
	 * @param vCount
	 *            the number of vertices in this GraphSize object
	 * @param eCount
	 *            the number of edge in this GraphSize object
	 * @param knownVertexTypes
	 *            the number of vertextypes this GraphSize object should know
	 * @param knownEdgeTypes
	 *            the number edgetypes this GraphSize object should know
	 */
	public GraphSize(long vCount, long eCount, int knownVertexTypes,
			int knownEdgeTypes) {
		vertexCount = vCount;
		edgeCount = eCount;
		this.knownEdgeTypes = knownEdgeTypes;
		this.knownVertexTypes = knownVertexTypes;
	}

	/**
	 * constructs a copy of the given GraphSize object
	 */
	public GraphSize(GraphSize copiedGraphSize) {
		vertexCount = copiedGraphSize.getVertexCount();
		edgeCount = copiedGraphSize.getEdgeCount();
		knownVertexTypes = copiedGraphSize.getKnownVertexTypes();
		knownEdgeTypes = copiedGraphSize.getKnownEdgeTypes();
	}

	/**
	 * constructs a new GraphSize object with the given number of vertices and
	 * edges and the given GraphClass Usefull for offline-optimization if the
	 * GraphClass is known but the Graph not
	 * 
	 * @param vCount
	 *            the number of vertices in this GraphSize object
	 * @param eCount
	 *            the number of edge in this GraphSize object
	 * @param graphClass
	 *            the GraphClass of the Datagraph to evaluate
	 */
	public GraphSize(GraphClass graphClass, int vCount, int eCount) {
		vertexCount = vCount;
		edgeCount = eCount;
		this.knownEdgeTypes = graphClass.getEdgeClassCount();
		this.knownVertexTypes = graphClass.getVertexClassCount();
	}

	/**
	 * 
	 * @return the number of EdgeTypes this GraphSize object knows
	 */
	public int getKnownEdgeTypes() {
		return knownEdgeTypes;
	}

	/**
	 * 
	 * @return the number of VertexTypes this GraphSize object knows
	 */
	public int getKnownVertexTypes() {
		return knownVertexTypes;
	}

	/**
	 * @return the number of vertices in this graphsize object
	 */
	public long getVertexCount() {
		return vertexCount;
	}

	/**
	 * sets the number of vertices in this GraphSize object
	 */
	public void setVertexCount(long count) {
		vertexCount = count;
	}

	/**
	 * @return the number of edge in this graphsize object
	 */
	public long getEdgeCount() {
		return edgeCount;
	}

	/**
	 * sets the number of edges in this GraphSize object
	 */
	public void setEdgeCount(long count) {
		edgeCount = count;
	}

	/**
	 * @return the average number of subclasses of a vertex class
	 */
	public double getAverageVertexSubclasses() {
		return averageVertexSubclasses;
	}

	/**
	 * @return the average number of subclasses of an edge class
	 */
	public double getAverageEdgeSubclasses() {
		return averageEdgeSubclasses;
	}

}
