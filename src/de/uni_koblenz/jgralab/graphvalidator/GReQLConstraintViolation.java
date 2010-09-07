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
package de.uni_koblenz.jgralab.graphvalidator;

import java.util.Set;

import de.uni_koblenz.jgralab.AttributedElement;
import de.uni_koblenz.jgralab.schema.AttributedElementClass;
import de.uni_koblenz.jgralab.schema.Constraint;

/**
 * 
 * @author Tassilo Horn <horn@uni-koblenz.de>
 * 
 */
public class GReQLConstraintViolation extends ConstraintViolation {

	private Constraint constraint;

	/**
	 * @return the constraint
	 */
	public Constraint getConstraint() {
		return constraint;
	}

	/**
	 * @return the offendingElements
	 */
	@Override
	public Set<AttributedElement> getOffendingElements() {
		return offendingElements;
	}

	public GReQLConstraintViolation(AttributedElementClass aec,
			Constraint constraint, Set<AttributedElement> offendingElems) {
		super(aec);
		this.constraint = constraint;
		offendingElements = offendingElems;
	}

	@Override
	public boolean equals(Object o) {
		if (o instanceof GReQLConstraintViolation) {
			GReQLConstraintViolation other = (GReQLConstraintViolation) o;
			return this.compareTo(other) == 0;
		}
		return false;
	}

	@Override
	public int hashCode() {
		int hash = 29;
		hash = hash * 97 + constraint.hashCode();
		hash = hash * 97 + offendingElements.hashCode();
		return hash;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("The predicate attached to ");
		sb.append(attributedElementClass.getQualifiedName());
		sb.append(" is not satisfied! ");
		sb.append(constraint.getMessage());
		if (offendingElements != null) {
			sb.append(" Offending elements: ");
			boolean first = true;
			for (AttributedElement ae : offendingElements) {
				if (first) {
					first = false;
				} else {
					sb.append(", ");
				}
				sb.append(ae);
			}
		}
		return sb.toString();
	}

	@Override
	public String getMessage() {
		return constraint.getMessage();
	}
}
