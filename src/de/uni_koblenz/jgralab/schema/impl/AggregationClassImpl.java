/*
 * JGraLab - The Java graph laboratory
 * (c) 2006-2007 Institute for Software Technology
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

package de.uni_koblenz.jgralab.schema.impl;

import de.uni_koblenz.jgralab.schema.AggregationClass;
import de.uni_koblenz.jgralab.schema.GraphClass;
import de.uni_koblenz.jgralab.schema.QualifiedName;
import de.uni_koblenz.jgralab.schema.VertexClass;

public class AggregationClassImpl extends EdgeClassImpl implements
		AggregationClass {

	/**
	 * toggles wether the aggregation end is at the "from" or "to" vertex
	 */
	private boolean aggregateFrom;

	/**
	 * builds a new aggregation class
	 * 
	 * @param name
	 *            the unique identifier of the aggregation class in the schema
	 * @param from
	 *            the vertex class from which the aggregation class may connect
	 *            from
	 * @param fromMin
	 *            the minimum multiplicity of the 'from' vertex class,
	 *            represents the minimum allowed number of connections from the
	 *            aggregation class to the 'from' vertex class
	 * @param fromMax
	 *            the maximum multiplicity of the 'from' vertex class,
	 *            represents the maximum allowed number of connections from the
	 *            aggregation class to the 'from' vertex class
	 * @param fromRoleName
	 *            a name which identifies the 'from' side of the aggregation
	 *            class in a unique way
	 * @param aggregateFrom
	 *            true if 'from'-end is an aggregation, false if 'to'-end is an
	 *            aggregation
	 * @param to
	 *            the vertex class to which the aggregation class may connect to
	 * @param toMin
	 *            the minimum multiplicity of the 'to' vertex class, represents
	 *            the minimum allowed number of connections from the aggregation
	 *            class to the 'to' vertex class
	 * @param toMax
	 *            the minimum multiplicity of the 'to' vertex class, represents
	 *            the maximum allowed number of connections from the aggregation
	 *            class to the 'to' vertex class
	 * @param toRoleName
	 *            a name which identifies the 'to' side of the aggregation class
	 *            in a unique way
	 */
	public AggregationClassImpl(QualifiedName qn, GraphClass aGraphClass,
			VertexClass from, int fromMin, int fromMax, String fromRoleName,
			boolean aggregateFrom, VertexClass to, int toMin, int toMax,
			String toRoleName) {
		super(qn, aGraphClass, from, fromMin, fromMax, fromRoleName, to,
				toMin, toMax, toRoleName);
		this.aggregateFrom = aggregateFrom;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see jgralab.AggregationClass#isAggregateFrom()
	 */
	public boolean isAggregateFrom() {
		return aggregateFrom;
	}

}
