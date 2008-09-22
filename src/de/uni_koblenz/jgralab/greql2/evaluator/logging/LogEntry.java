/*
 * JGraLab - The Java graph laboratory
 * (c) 2006-2008 Institute for Software Technology
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

package de.uni_koblenz.jgralab.greql2.evaluator.logging;

import org.jdom.Element;

/**
 * This class is the baseclass for all LogEntrys. It stores the number of calls
 * of this logentry
 */
public abstract class LogEntry {

	/**
	 * Stores the number of log-calls for this logentry
	 */
	protected long calls;

	/**
	 * Stores the name of this logentry. This can be either the vertextype or
	 * something similar.
	 */
	protected String name;

	/**
	 * creates a new LogEntry from given JDOM Element
	 */
	public LogEntry(Element elem) {
		Element nameElem = elem.getChild("name");
		name = nameElem.getText();
		Element callsElem = elem.getChild("calls");
		calls = Integer.parseInt(callsElem.getText());
	}

	/**
	 * creates a new empty logentry with the given name
	 */
	public LogEntry(String name) {
		calls = 0;
		this.name = name;
	}

	/**
	 * returns the callcount of this LogEntry object
	 * 
	 * @return the callcount of this LogEntry object
	 */
	public long getCalls() {
		return calls;
	}

	/**
	 * returns the name of this LogEntry
	 * 
	 * @return the name of this LogEntry
	 */
	public String getName() {
		return name;
	}

	/**
	 * returns the data stored in this logentry as JDom Element. The structure
	 * of the element is like this
	 * 
	 * @return the data stored in this logentry as JDom Element
	 */
	public abstract Element toJDOMEntry();

}