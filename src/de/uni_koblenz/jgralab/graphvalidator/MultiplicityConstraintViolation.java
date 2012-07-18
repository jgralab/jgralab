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
package de.uni_koblenz.jgralab.graphvalidator;

import java.util.Map;
import java.util.Set;

import de.uni_koblenz.jgralab.AttributedElement;
import de.uni_koblenz.jgralab.schema.EdgeClass;

/**
 * @author Tassilo Horn <horn@uni-koblenz.de>
 * 
 */
public class MultiplicityConstraintViolation extends ConstraintViolation {
	private String message;

	Map<AttributedElement<?, ?>, Integer> degreeMap;

	public MultiplicityConstraintViolation(EdgeClass ec, String message,
			Map<AttributedElement<?, ?>, Integer> degreeMap) {
		super(ec);
		this.message = message;
		this.degreeMap = degreeMap;
		offendingElements = degreeMap.keySet();
	}

	@Override
	public int hashCode() {
		return message.hashCode() + offendingElements.hashCode()
				+ degreeMap.hashCode();
	}

	@Override
	public boolean equals(Object o) {
		if (o instanceof MultiplicityConstraintViolation) {
			MultiplicityConstraintViolation other = (MultiplicityConstraintViolation) o;
			return compareTo(other) == 0;
		}
		return false;
	}

	/**
	 * @return the message
	 */
	@Override
	public String getMessage() {
		return message;
	}

	/**
	 * @return the offendingElements
	 */
	@Override
	public Set<AttributedElement<?, ?>> getOffendingElements() {
		return offendingElements;
	}

	public int getDegree(AttributedElement<?, ?> el) {
		return degreeMap.get(el);
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("Broken multiplicity constraint at EdgeClass ");
		sb.append(attributedElementClass.getQualifiedName());
		sb.append("! ");
		sb.append(message);
		sb.append(" Offending vertices: ");
		boolean first = true;
		for (AttributedElement<?, ?> v : offendingElements) {
			if (first) {
				first = false;
			} else {
				sb.append(", ");
			}
			sb.append(v).append(": ").append(degreeMap.get(v));
		}
		return sb.toString();
	}
}
