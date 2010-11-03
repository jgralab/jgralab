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

package de.uni_koblenz.jgralab.greql2.evaluator.logging;

import org.jdom.Element;

/**
 * This class stores the number of logging-calls for selectivity and resultsize
 * and the summed up value. The average or estimated size can be calculated by
 * dividing the result size
 */
public class SimpleLogEntry extends LogEntry {

	/**
	 * stores the summed-up value of logvalues
	 */
	private long sum;

	/**
	 * Creates a new SimpleLogEntry from given JDOM-Element
	 */
	public SimpleLogEntry(Element elem) {
		super(elem);
		Element sumElem = elem.getChild("sum");
		sum = Long.parseLong(sumElem.getText());
	}

	/**
	 * Creates a new SimpleLogEntry with the given name
	 */
	public SimpleLogEntry(String name) {
		super(name);
		sum = 0;
	}

	/**
	 * logs the given integer value, adds it to the sum and increases the
	 * callcount
	 * 
	 * @param n
	 */
	public void logSum(long n) {
		sum += n;
		calls++;
	}

	/**
	 * returns the sumed-up value of logvalues
	 * 
	 * @return the sumed-up value of logvalues
	 */
	public long getSum() {
		return sum;
	}

	/**
	 * returns the average or estimated value
	 * 
	 * @return the average or estimated value
	 */
	public double getAverageValue() {
		return sum * 1d / calls;
	}

	/**
	 * returns the data stored in this logentry as JDom Element. The structure
	 * of the element is like this <SimpleLogEntry> <sum> X </sum> <calls> Y
	 * </sum> </SimpleLogEntry>
	 * 
	 * @return the data stored in this logentry as JDom Element
	 */
	@Override
	public Element toJDOMEntry() {
		Element logEntryElem = new Element("SimpleLogEntry");
		Element nameElem = new Element("name");
		nameElem.addContent(name);
		logEntryElem.addContent(nameElem);
		Element sumElem = new Element("sum");
		sumElem.addContent(String.valueOf(sum));
		Element callsElem = new Element("calls");
		callsElem.addContent(String.valueOf(calls));
		logEntryElem.addContent(sumElem);
		logEntryElem.addContent(callsElem);
		return logEntryElem;
	}

}
