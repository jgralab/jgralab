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
package de.uni_koblenz.jgralab.eca.events;

import de.uni_koblenz.jgralab.Graph;
import de.uni_koblenz.jgralab.Vertex;
import de.uni_koblenz.jgralab.eca.events.EventDescription.EventTime;
import de.uni_koblenz.jgralab.schema.VertexClass;

public class CreateVertexEvent extends Event<VertexClass> {

	/**
	 * The created Vertex or null if the EventTime is before
	 */
	private Vertex vertex;

	/**
	 * Creates an CreateVertexEvent with the given parameters
	 * 
	 * @param nestedCalls
	 *            depth of nested trigger calls
	 * @param graph
	 *            Graph where the Event happened
	 * @param element
	 *            the created Vertex or null if the EventTime is before
	 */
	public CreateVertexEvent(int nestedCalls, Graph graph, Vertex element) {
		super(nestedCalls, EventTime.AFTER, graph, element
				.getAttributedElementClass());
		vertex = element;
	}

	/**
	 * Creates an CreateVertexEvent with the given parameters
	 * 
	 * @param nestedCalls
	 *            depth of nested trigger calls
	 * @param graph
	 *            Graph where the Event happened
	 * @param type
	 *            the type of the to be created Vertex
	 */
	public CreateVertexEvent(int nestedCalls, Graph graph, VertexClass type) {
		super(nestedCalls, EventTime.BEFORE, graph, type);
		vertex = null;
	}

	/**
	 * @return the AttributedElement that causes the Event or null if the
	 *         EventTime is before
	 */
	@Override
	public Vertex getElement() {
		return vertex;
	}

}
