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

import de.uni_koblenz.jgralab.greql2.SerializableGreql2;
import de.uni_koblenz.jgralab.greql2.schema.Greql2Vertex;
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
	 * the position in the query where exception occured
	 */
	private List<SourcePosition> positions;

	/**
	 * the element that causes the error
	 */
	private Greql2Vertex element;

	/**
	 * the error message
	 */
	private String errorMessage;

	/**
	 * 
	 * @param element
	 *            the element that caused the error
	 * @param sourcePositions
	 *            a list of sourceposition where the error possible occurs
	 */
	public QuerySourceException(String errorMessage, Greql2Vertex element,
			List<SourcePosition> sourcePositions, Exception cause) {
		super(errorMessage, cause);
		this.element = element;
		this.errorMessage = errorMessage;
		if (sourcePositions != null) {
			positions = sourcePositions;
		} else {
			positions = new ArrayList<SourcePosition>();
		}
	}

	/**
	 * 
	 * @param element
	 *            the element that caused the error
	 * @param sourcePosition
	 *            the sourceposition where the error occurs
	 */
	public QuerySourceException(String errorMessage, Greql2Vertex element,
			SourcePosition sourcePosition, Exception cause) {
		super(errorMessage, cause);
		this.errorMessage = errorMessage;
		this.element = element;
		positions = new ArrayList<SourcePosition>();
		positions.add(sourcePosition);
	}

	/**
	 * 
	 * @param element
	 *            the element that caused the error
	 * @param sourcePositions
	 *            a list of sourceposition where the error possible occurs
	 */
	public QuerySourceException(String errorMessage, Greql2Vertex element,
			List<SourcePosition> sourcePositions) {
		this(errorMessage, element, sourcePositions, null);
	}

	/**
	 * 
	 * @param element
	 *            the element that caused the error
	 * @param sourcePosition
	 *            the sourceposition where the error occurs
	 */
	public QuerySourceException(String errorMessage, Greql2Vertex element,
			SourcePosition sourcePosition) {
		this(errorMessage, element, sourcePosition, null);
	}

	/**
	 * returns the string of the message
	 */
	@Override
	public String getMessage() {
		StringBuilder sb = new StringBuilder();
		if (positions.size() > 0) {
			sb.append(errorMessage);
			sb.append(": query part '");
			sb.append((element != null) ? ((SerializableGreql2) element
					.getGraph()).serialize(element) : "<unknown element>");
			sb.append("' at position (");
			sb.append(positions.get(0).get_offset());
			sb.append(", ");
			sb.append(positions.get(0).get_length());
			sb.append(")");
		} else {
			sb.append(errorMessage);
			sb.append(": query part '");
			sb.append((element != null) ? ((SerializableGreql2) element
					.getGraph()).serialize(element) : "<unknown element>");
			sb.append("' at unknown position in query");
		}
		return sb.toString();
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
	 * @return the broken element
	 */
	public Greql2Vertex getElement() {
		return element;
	}

}
