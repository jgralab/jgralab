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
package de.uni_koblenz.jgralab.eca.events;

import de.uni_koblenz.jgralab.Edge;
import de.uni_koblenz.jgralab.Graph;
import de.uni_koblenz.jgralab.Vertex;
import de.uni_koblenz.jgralab.eca.ECARule;
import de.uni_koblenz.jgralab.schema.EdgeClass;

public class ChangeEdgeEventDescription extends EventDescription<EdgeClass> {

	public enum EdgeEnd {
		ALPHA, OMEGA, ANY
	}

	/**
	 * Whether this EventDescription monitors changes on ALPHA, OMEGA or BOTH
	 */
	private EdgeEnd edgeEnd;

	/**
	 * Creates an ChangeEdgeEventDescription with the given parameters
	 * 
	 * @param time
	 *            the EventTime, BEFORE or AFTER
	 * @param type
	 *            the Class of elements, this EventDescription monitors
	 */
	public ChangeEdgeEventDescription(EventTime time, EdgeClass type,
			EdgeEnd end) {
		super(time, type);
		edgeEnd = end;
	}

	/**
	 * Creates an ChangeEdgeEventDescription with the given parameters
	 * 
	 * @param time
	 *            the EventTime, BEFORE or AFTER
	 * @param contextExpr
	 *            the contextExpression to get the context
	 */
	public ChangeEdgeEventDescription(EventTime time, String contextExpr) {
		super(time, contextExpr);
	}

	/**
	 * Checks whether this EventDescription matches the Event and triggers the
	 * rules if it is so
	 * 
	 * @param element
	 *            the Edge that causes the Event
	 * @param oldVertex
	 *            the old Vertex of the Edge
	 * @param newVertex
	 *            the new Vertex of the Edge
	 */
	public void fire(Edge element, Vertex oldVertex, Vertex newVertex,
			EdgeEnd endOfEdge) {
		if (super.checkContext(element)) {
			int nested = getActiveECARules().get(0).getECARuleManager()
					.getNestedTriggerCalls();
			Graph graph = getActiveECARules().get(0).getECARuleManager()
					.getGraph();
			for (ECARule<EdgeClass> rule : activeRules) {
				rule.trigger(new ChangeEdgeEvent(nested, getTime(), graph,
						element, oldVertex, newVertex, endOfEdge));

			}
		}
	}

	/**
	 * @return if the ChangeEdgeEventDescription monitors the change of the
	 *         alpha end, the omega end or both
	 */
	public EdgeEnd getEdgeEnd() {
		return edgeEnd;
	}

}
