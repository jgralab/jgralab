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
package de.uni_koblenz.jgralab.graphvalidator;

/**
 * @author Tassilo Horn <horn@uni-koblenz.de>
 *
 */
public class ConstraintInvalidation {
	public enum ConstraintType {
		MULTIPLICITY, GREQL
	}

	private String message;
	private ConstraintType constraintType;

	public ConstraintInvalidation(ConstraintType type, String message) {
		constraintType = type;
		this.message = message;
	}

	/**
	 * @return the message
	 */
	public String getMessage() {
		return message;
	}

	/**
	 * @return the constraintType
	 */
	public ConstraintType getConstraintType() {
		return constraintType;
	}

	@Override
	public String toString() {
		return "Broken " + constraintType + " constraint: " + message;
	}

	@Override
	public boolean equals(Object o) {
		if (o instanceof ConstraintInvalidation) {
			ConstraintInvalidation other = (ConstraintInvalidation) o;
			return constraintType == other.constraintType
					&& message.equals(other.message);
		}
		return false;
	}

	@Override
	public int hashCode() {
		return constraintType.hashCode() + message.hashCode();
	}
}
