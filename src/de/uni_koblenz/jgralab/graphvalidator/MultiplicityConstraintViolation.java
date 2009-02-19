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

import de.uni_koblenz.jgralab.Vertex;
import de.uni_koblenz.jgralab.schema.EdgeClass;

/**
 * @author Tassilo Horn <horn@uni-koblenz.de>
 *
 */
public class MultiplicityConstraintViolation extends ConstraintViolation {
	private String message;
	private Set<Vertex> offendingVertices;

	public MultiplicityConstraintViolation(EdgeClass ec, String message,
			Set<Vertex> offendingElems) {
		super(ec);
		this.message = message;
		offendingVertices = offendingElems;
	}

	@Override
	public int hashCode() {
		int hash = 23;
		hash = hash * 571 + message.hashCode();
		hash = hash * 571 + offendingVertices.hashCode();
		return hash;
	}

	@Override
	public boolean equals(Object o) {
		if (o instanceof MultiplicityConstraintViolation) {
			MultiplicityConstraintViolation other = (MultiplicityConstraintViolation) o;
			return this.compareTo(other) == 0;
		}
		return false;
	}

	/**
	 * @return the message
	 */
	public String getMessage() {
		return message;
	}

	/**
	 * @return the offendingElements
	 */
	public Set<Vertex> getOffendingVertices() {
		return offendingVertices;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("Broken constraint attached to ");
		sb.append(attributedElementClass.getQualifiedName());
		sb.append("! ");
		sb.append(message);
		sb.append(" Offending vertices: ");
		boolean first = true;
		for (Vertex v : offendingVertices) {
			if (first) {
				first = false;
			} else {
				sb.append(", ");
			}
			sb.append(v);
		}
		return sb.toString();
	}
}
