/*
 * JGraLab - The Java graph laboratory
 * (c) 2006-2009 Institute for Software Technology
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

package de.uni_koblenz.jgralab.greql2.exception;

import java.util.ArrayList;
import java.util.List;

import de.uni_koblenz.jgralab.greql2.schema.SourcePosition;

/**
 * This is the base class for all exceptions that refeer to the querysource with
 * offset/length pairs
 * 
 * @author ist@uni-koblenz.de
 * 
 */
public class QuerySourceException extends EvaluateException {

	static final long serialVersionUID = -1234561;

	/**
	 * the position in the query where the undefined variable is used
	 */
	private List<SourcePosition> positions;

	/**
	 * the name of the elements that causes the error
	 */
	private String elementName;

	/**
	 * the error message
	 */
	private String errorMessage;

	/**
	 * 
	 * @param elementName
	 *            the name of the element that caused the error
	 * @param sourcePositions
	 *            a list of sourceposition where the error possible occurs
	 */
	public QuerySourceException(String errorMessage, String elementName,
			List<SourcePosition> sourcePositions, Exception cause) {
		super(errorMessage + elementName, cause);
		this.elementName = elementName;
		this.errorMessage = errorMessage;
		if (sourcePositions != null) {
			positions = sourcePositions;
		} else {
			positions = new ArrayList<SourcePosition>();
		}
	}

	/**
	 * 
	 * @param elementName
	 *            the name of the element that caused the error
	 * @param sourcePosition
	 *            the sourceposition where the error occurs
	 */
	public QuerySourceException(String errorMessage, String elementName,
			SourcePosition sourcePosition, Exception cause) {
		super(errorMessage + elementName, cause);
		this.errorMessage = errorMessage;
		this.elementName = elementName;
		positions = new ArrayList<SourcePosition>();
		positions.add(sourcePosition);
	}

	/**
	 * 
	 * @param elementName
	 *            the name of the element that caused the error
	 * @param sourcePositions
	 *            a list of sourceposition where the error possible occurs
	 */
	public QuerySourceException(String errorMessage, String elementName,
			List<SourcePosition> sourcePositions) {
		this(errorMessage, elementName, sourcePositions, null);
	}

	/**
	 * 
	 * @param elementName
	 *            the name of the element that caused the error
	 * @param sourcePosition
	 *            the sourceposition where the error occurs
	 */
	public QuerySourceException(String errorMessage, String elementName,
			SourcePosition sourcePosition) {
		this(errorMessage, elementName, sourcePosition, null);
	}

	/**
	 * returns the string of the message
	 */
	@Override
	public String getMessage() {
		if (positions.size() > 0) {
			return errorMessage + " " + elementName + " at position ("
					+ positions.get(0).get_offset() + ", "
					+ positions.get(0).get_length() + ")";
		} else {
			return errorMessage + elementName + " at unknown position in query";
		}
	}

	/**
	 * returns the list of sourcepositions
	 */
	public List<SourcePosition> getSourcePositions() {
		return positions;
	}

	/**
	 * @return the position where the undefined varialbe is used
	 */
	public int getOffset() {
		if (positions.size() < 0) {
			return 0;
		}
		return positions.get(0).get_offset();
	}

	/**
	 * @return the length of the usage of the undefined variable
	 */
	public int getLength() {
		if (positions.size() < 0) {
			return 0;
		}
		return positions.get(0).get_length();
	}

	/**
	 * @return the name of the undefinedvariable
	 */
	public String getElementName() {
		return elementName;
	}

}
