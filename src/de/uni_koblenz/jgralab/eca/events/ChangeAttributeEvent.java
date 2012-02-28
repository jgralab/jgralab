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
import de.uni_koblenz.jgralab.AttributedElement;
import de.uni_koblenz.jgralab.schema.AttributedElementClass;

public class ChangeAttributeEvent<AEC extends AttributedElementClass<AEC, ?>>
		extends Event<AEC> {

	/**
	 * AttributedElement who causes this Event
	 */
	private AttributedElement<AEC, ?> element;

	/**
	 * Name of the Attribute that changes
	 */
	private String attributeName;

	/**
	 * Old value of the Attribute
	 */
	private Object oldValue;

	/**
	 * New value of the Attribute
	 */
	private Object newValue;

	/**
	 * Creates a new ChangeAttributeEvent with the given parameters
	 * 
	 * @param nestedCalls
	 *            depth of nested calls
	 * @param time
	 *            before or after
	 * @param graph
	 *            Graph where the Event occurs
	 * @param element
	 *            AttributedElement that causes the Event
	 * @param attributeName
	 *            name of the changing Attribute
	 * @param oldValue
	 *            old value of the Attribute
	 * @param newValue
	 *            new value of the Attribute
	 */
	public ChangeAttributeEvent(int nestedCalls,
			EventDescription.EventTime time, Graph graph,
			AttributedElement<AEC, ?> element, String attributeName,
			Object oldValue, Object newValue) {
		super(nestedCalls, time, graph, element.getAttributedElementClass());
		this.element = element;
		this.attributeName = attributeName;
		this.oldValue = oldValue;
		this.newValue = newValue;
	}

	/**
	 * @return the AttributedElement that causes this Event
	 */
	@Override
	public AttributedElement<AEC, ?> getElement() {
		return element;
	}

	/**
	 * @return the name of the changing Attribute
	 */
	public String getAttributeName() {
		return attributeName;
	}

	/**
	 * @return the old value of the Attribute
	 */
	public Object getOldValue() {
		return oldValue;
	}

	/**
	 * @return the new value of the Attribute
	 */
	public Object getNewValue() {
		return newValue;
	}

}
