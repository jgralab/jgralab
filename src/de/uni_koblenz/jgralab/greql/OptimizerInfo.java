/*
 * JGraLab - The Java Graph Laboratory
 *
 * Copyright (C) 2006-2012 Institute for Software Technology
 *                         University of Koblenz-Landau, Germany
 *                         ist@uni-koblenz.de
 *
 * For bug reports, documentation and further information, visit
 *
 *                         https://github.com/jgralab/jgralab
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

package de.uni_koblenz.jgralab.greql;

import de.uni_koblenz.jgralab.greql.types.TypeCollection;

/**
 * This class is needed to propagate the size of the currently used graph along
 * different calculate methods
 * 
 * @author ist@uni-koblenz.de
 */
public interface OptimizerInfo {

	/**
	 * 
	 * @return the number of EdgeTypes this GraphSize object knows
	 */
	public int getKnownEdgeTypes();

	/**
	 * 
	 * @return the number of VertexTypes this GraphSize object knows
	 */
	public int getKnownVertexTypes();

	/**
	 * @return the number of vertices in this graphsize object
	 */
	public long getVertexCount();

	/**
	 * sets the number of vertices in this GraphSize object
	 */
	public void setVertexCount(long count);

	/**
	 * @return the number of edge in this graphsize object
	 */
	public long getEdgeCount();

	/**
	 * sets the number of edges in this GraphSize object
	 */
	public void setEdgeCount(long count);

	/**
	 * @return the average number of subclasses of a vertex class
	 */
	public double getAverageVertexSubclasses();

	/**
	 * @return the average number of subclasses of an edge class
	 */
	public double getAverageEdgeSubclasses();

	/**
	 * Returns the average percentage of vertices of the given
	 * {@link VertexClass} in a {@link Graph} in relation to all
	 * {@link VertexClass}es. The returned value lies in the interval [0,1].
	 * 
	 * @param vcName
	 *            qualified {@link VertexClass} name
	 * 
	 * @return the average percentage of vertices of the given type in a
	 *         {@link Graph}
	 */
	public double getRelativeFrequencyOfVertexClass(String vcName);

	/**
	 * Returns the average percentage of edges of the given {@link EdgeClass} in
	 * a {@link Graph} in relation to all {@link EdgeClass}es. The returned
	 * value lies in the interval [0,1].
	 * 
	 * @param ecName
	 *            qualified {@link EdgeClass} name
	 * 
	 * @return the average percentage of edges of the given type in a
	 *         {@link Graph}
	 */
	public double getRelativeFrequencyOfEdgeClass(String ecName);

	/**
	 * Returns the average percentage of graph elements of the given
	 * {@link GraphElementClass} in a {@link Graph} in relation to all
	 * {@link GraphElementClass}es. The returned value lies in the interval
	 * [0,1]. Allows the comparison of values for {@link VertexClasses} and
	 * {@link EdgeClasses}.
	 * 
	 * @param vcName
	 *            qualified {@link GraphElementClass} name
	 * 
	 * @return the average percentage of graph elements of the given type in a
	 *         {@link Graph}
	 */
	public double getRelativeFrequencyOfGraphElementClass(String geName);

	/**
	 * Returns the average percentage of vertices of the given
	 * {@link VertexClass} in a {@link Graph}. The returned value lies in the
	 * interval [0,1].
	 * 
	 * @param vcName
	 *            qualified {@link VertexClass} name
	 * 
	 * @return the average percentage of vertices of the given type in a
	 *         {@link Graph}
	 */
	public double getRelativeFrequencyOfTypeCollection(TypeCollection tc);

	/**
	 * @return the average ratio between edges and vertices in a graph
	 */
	public double getEdgesPerVertex();

}