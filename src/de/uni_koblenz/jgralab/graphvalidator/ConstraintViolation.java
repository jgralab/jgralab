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

import de.uni_koblenz.jgralab.greql2.jvalue.JValue;
import de.uni_koblenz.jgralab.greql2.jvalue.JValueRecord;

/**
 * TODO: Describe the format of the jvalue member.
 *
 * @author Tassilo Horn <horn@uni-koblenz.de>
 *
 */
public class ConstraintViolation implements
		Comparable<ConstraintViolation> {
	public enum ConstraintType {
		MULTIPLICITY, GREQL, INVALID_GREQL_EXPRESSION
	}

	private JValue jvalue;
	private ConstraintType constraintType;

	public ConstraintViolation(ConstraintType type, JValue jvalue) {
		constraintType = type;
		this.jvalue = jvalue;
	}

	/**
	 * @return the constraintType
	 */
	public ConstraintType getConstraintType() {
		return constraintType;
	}

	public String getInvalidationDescription() {
		StringBuilder sb = new StringBuilder();
		switch (constraintType) {
		case MULTIPLICITY:
			JValueRecord multRec = jvalue.toJValueRecord();
			sb.append(multRec.get("vertex"));
			sb.append(" has ");
			sb.append(multRec.get("degree"));
			sb.append(" ");
			sb.append(multRec.get("direction"));
			sb.append(" ");
			sb.append(multRec.get("edgeClass").toAttributedElementClass()
					.getQualifiedName());
			sb.append(" edges, but only ");
			sb.append(multRec.get("min"));
			sb.append(" to ");
			sb.append(multRec.get("max"));
			sb.append(" are allowed.");
			break;
		case INVALID_GREQL_EXPRESSION:
			sb.append("\"");
			sb.append(jvalue);
			sb.append("\"");
			sb.append(" is no valid GReQL expression.");
			break;
		case GREQL:
			JValueRecord rec = jvalue.toJValueRecord();
			JValue result = rec.get("result");
			JValue greqlExp = rec.get("greqlExpression");
			JValue aec = rec.get("attributedElementClass");
			sb.append("Query result = ");
			sb.append(result);
			sb.append(". (Constraint attached to ");
			sb.append(aec.toAttributedElementClass().getQualifiedName());
			sb.append(", query = ");
			sb.append("\"");
			sb.append(greqlExp);
			sb.append("\"");
			sb.append(")");
			break;
		}
		return sb.toString();
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("Broken " + constraintType + " constraint: ");
		sb.append(getInvalidationDescription());
		return sb.toString();
	}

	@Override
	public boolean equals(Object o) {
		if (o instanceof ConstraintViolation) {
			ConstraintViolation other = (ConstraintViolation) o;
			return constraintType == other.constraintType
					&& jvalue.equals(other.jvalue);
		}
		return false;
	}

	@Override
	public int hashCode() {
		return constraintType.hashCode() + jvalue.hashCode();
	}

	@Override
	public int compareTo(ConstraintViolation ci) {
		int typeComp = constraintType.compareTo(ci.constraintType);
		if (typeComp == 0) {
			return jvalue.compareTo(ci.jvalue);
		}
		return typeComp;
	}
}
