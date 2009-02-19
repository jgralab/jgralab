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
package de.uni_koblenz.jgralab.graphvalidator;

import java.util.Set;

import de.uni_koblenz.jgralab.AttributedElement;
import de.uni_koblenz.jgralab.schema.AttributedElementClass;
import de.uni_koblenz.jgralab.schema.Constraint;

/**
 * Describes a violation of some constraint, either an explicit
 * {@link Constraint} or a multiplicity constraint.
 *
 * @see MultiplicityConstraintViolation
 * @see GReQLConstraintViolation
 * @see BrokenGReQLConstraintViolation
 *
 * @author Tassilo Horn <horn@uni-koblenz.de>
 */
public abstract class ConstraintViolation implements
		Comparable<ConstraintViolation> {

	protected AttributedElementClass attributedElementClass;
	protected Set<AttributedElement> offendingElements;

	/**
	 * @return the {@link AttributedElementClass} the violated constraint is
	 *         attached to
	 */
	public AttributedElementClass getAttributedElementClass() {
		return attributedElementClass;
	}

	public ConstraintViolation(AttributedElementClass aec) {
		attributedElementClass = aec;
	}

	@Override
	public int compareTo(ConstraintViolation ci) {
		if (this instanceof MultiplicityConstraintViolation
				&& ci instanceof MultiplicityConstraintViolation) {
			MultiplicityConstraintViolation me = (MultiplicityConstraintViolation) this;
			MultiplicityConstraintViolation other = (MultiplicityConstraintViolation) ci;
			int result = me.getAttributedElementClass().getQualifiedName()
					.compareTo(
							other.getAttributedElementClass()
									.getQualifiedName());
			if (result != 0) {
				return result;
			}
			result = me.getMessage().compareTo(other.getMessage());
			if (result != 0) {
				return result;
			}
			if (me.getOffendingElements() != null) {
				if (me.getOffendingElements().equals(
						other.getOffendingElements())) {
					return 0;
				}
				return new Integer(me.getOffendingElements().hashCode())
						.compareTo(other.getOffendingElements().hashCode());
			}
			if (other.getOffendingElements() == null) {
				return 0;
			}
			return -1;
		}

		if (this instanceof BrokenGReQLConstraintViolation
				&& ci instanceof BrokenGReQLConstraintViolation) {
			BrokenGReQLConstraintViolation me = (BrokenGReQLConstraintViolation) this;
			BrokenGReQLConstraintViolation other = (BrokenGReQLConstraintViolation) ci;
			int result = me.getAttributedElementClass().getQualifiedName()
					.compareTo(
							other.getAttributedElementClass()
									.getQualifiedName());
			if (result != 0) {
				return result;
			}
			result = me.getConstraint().compareTo(other.getConstraint());
			if (result != 0) {
				return result;
			}
			return me.getBrokenPart().compareTo(other.getBrokenPart());
		}

		if (this instanceof GReQLConstraintViolation
				&& ci instanceof GReQLConstraintViolation) {
			GReQLConstraintViolation me = (GReQLConstraintViolation) this;
			GReQLConstraintViolation other = (GReQLConstraintViolation) ci;
			int result = me.getAttributedElementClass().getQualifiedName()
					.compareTo(
							other.getAttributedElementClass()
									.getQualifiedName());
			if (result != 0) {
				return result;
			}
			result = me.getConstraint().compareTo(other.getConstraint());
			if (result != 0) {
				return result;
			}
			if (me.getOffendingElements() != null) {
				if (me.getOffendingElements().equals(
						other.getOffendingElements())) {
					return 0;
				}
				return new Integer(me.getOffendingElements().hashCode())
						.compareTo(other.getOffendingElements().hashCode());
			}
			if (other.getOffendingElements() == null) {
				return 0;
			}
			return -1;
		}

		// Ok, the types differ, so we'll use the order: GReQL,
		// Multi, BrokenGReQL.
		if (this instanceof GReQLConstraintViolation) {
			return -1;
		}
		if (this instanceof BrokenGReQLConstraintViolation) {
			return 1;
		}
		// Ok, this is a Multi, so what's ci?
		if (ci instanceof GReQLConstraintViolation) {
			return 1;
		}
		return -1;
	}

	/**
	 * @return a message which describes the violated constraint
	 */
	public abstract String getMessage();

	/**
	 * @return a set of {@link AttributedElement}s which violate the constraint
	 */
	public abstract Set<AttributedElement> getOffendingElements();
}
