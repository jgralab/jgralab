/*
 * JGraLab - The Java Graph Laboratory
 *
 * Copyright (C) 2006-2014 Institute for Software Technology
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
package de.uni_koblenz.jgralab.schema;

import java.util.Set;

public interface IncidenceClass {

	/**
	 * @return the upper multiplicity, i.e. the maximal number of edges
	 *         connected to the vertex at the opposite end
	 */
	public int getMax();

	/**
	 * Set the upper multiplicity to <code>max</code>.
	 * 
	 * @param max
	 *            an number between 0 and Integer.MAX_VALUE
	 */
	public void setMax(int max);

	/**
	 * @return the lower multiplicity, i.e. the minimal number of edges
	 *         connected to the vertex at the opposite end
	 */
	public int getMin();

	/**
	 * Set the lower multiplicity to <code>min</code>.
	 * 
	 * @param min
	 *            an number between 0 and Integer.MAX_VALUE
	 */
	public void setMin(int min);

	/**
	 * @return {@link IncidenceDirection#OUT} if this {@link IncidenceClass} is
	 *         the alpha incidence of the {@link EdgeClass}. Otherwise
	 *         {@link IncidenceDirection#IN} is returned.
	 */
	public IncidenceDirection getDirection();

	/**
	 * @return the name of this incidence class, i.e. the rolename of the edge
	 *         end
	 */
	public String getRolename();

	/**
	 * Sets the name of this incidence class to <code>name</code>.
	 * 
	 * @param name
	 *            the new rolename
	 */
	public void setRolename(String name);

	/**
	 * @return the type of this IncidenceClass, NONE for a normal edge end,
	 *         AGGREGATION for an aggregation end and COMPOSITION for a
	 *         composition end
	 */
	public AggregationKind getAggregationKind();

	/**
	 * sets the type of this IncidenceClass, NONE for a normal edge end,
	 * AGGREGATION for an aggregation end and COMPOSITION for a composition end
	 */
	public void setAggregationKind(AggregationKind kind);

	/**
	 * @return the set of IncidenceClasses which are subsetted (i.e.
	 *         specialized) by this IncidenceClass
	 */
	public Set<IncidenceClass> getSubsettedIncidenceClasses();

	/**
	 * @return the set of IncidenceClasses which are directly subsetted (i.e.
	 *         specialized) by this IncidenceClass
	 */
	public Set<IncidenceClass> getOwnSubsettedIncidenceClasses();

	/**
	 * @return the VertexClass this IncidenceClass is connected to
	 */
	public VertexClass getVertexClass();

	/**
	 * @return the EdgeClass this IncidenceClass is connected to
	 */
	public EdgeClass getEdgeClass();

	/**
	 * @return the set of all role names valid for this IncidenceClass
	 */
	public Set<String> getAllRoles();

	/**
	 * @return the IncidenceClass at the other end of the EdgeClass this
	 *         IncidenceClass belongs to
	 */
	public IncidenceClass getOpposite();

	/**
	 * 
	 * @return the numeric id of this incidence class in the schema
	 */
	public int getIncidenceClassIdInSchema();

}
