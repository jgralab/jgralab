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

import de.uni_koblenz.jgralab.schema.AttributedElementClass;
import de.uni_koblenz.jgralab.schema.Constraint;

/**
 * @author Tassilo Horn <horn@uni-koblenz.de>
 *
 */
public class BrokenGReQLConstraintViolation extends ConstraintViolation {

	private Constraint constraint;
	private String brokenPart;

	public BrokenGReQLConstraintViolation(AttributedElementClass aec,
			Constraint constraint, String brokenPart) {
		super(aec);
		this.constraint = constraint;
		this.brokenPart = brokenPart;
	}

	@Override
	public int hashCode() {
		int hash = 11;
		hash = hash * 751 + constraint.hashCode();
		hash = hash * 751 + brokenPart.hashCode();
		return hash;
	}

	@Override
	public boolean equals(Object o) {
		if (o instanceof BrokenGReQLConstraintViolation) {
			BrokenGReQLConstraintViolation other = (BrokenGReQLConstraintViolation) o;
			return this.compareTo(other) == 0;
		}
		return false;
	}

	/**
	 * @return the constraint
	 */
	public Constraint getConstraint() {
		return constraint;
	}

	/**
	 * @return the brokenPart
	 */
	public String getBrokenPart() {
		return brokenPart;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("The query \"");
		sb.append(brokenPart);
		sb.append("\" in ");
		sb.append(constraint);
		sb.append(" attached to ");
		sb.append(attributedElementClass.getQualifiedName());
		sb.append(" is broken.");
		return sb.toString();
	}
}
