/*
 * JGraLab - The Java graph laboratory
 * (c) 2006-2007 Institute for Software Technology
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

package de.uni_koblenz.jgralab.schema;

import java.util.Set;

import de.uni_koblenz.jgralab.EdgeDirection;

public class DirectedEdgeClass /* implements Comparable<DirectedEdgeClass> */{

	private EdgeClass edgeClass;

	private EdgeDirection direction;

	public DirectedEdgeClass(EdgeClass ec, EdgeDirection d) {
		if (d == EdgeDirection.INOUT) {
			throw new IllegalArgumentException("INOUT not allowed here");
		}
		edgeClass = ec;
		direction = d;
	}

	public EdgeDirection getDirection() {
		return direction;
	}

	public void setDirection(EdgeDirection direction) {
		if (direction == EdgeDirection.INOUT) {
			throw new IllegalArgumentException("INOUT not allowed here");
		}
		this.direction = direction;
	}

	public EdgeClass getEdgeClass() {
		return edgeClass;
	}

	public void setEdgeClass(EdgeClass edgeClass) {
		this.edgeClass = edgeClass;
	}

	/**
	 * @return the rolename at the far end of the edge
	 */
	public String getThatRolename() {
		if (direction == EdgeDirection.IN)
			return edgeClass.getFromRolename();
		else
			return edgeClass.getToRolename();
	}

	/**
	 * @return the rolename at the near end of the edge
	 */
	public String getThisRolename() {
		if (direction == EdgeDirection.OUT)
			return edgeClass.getFromRolename();
		else
			return edgeClass.getToRolename();
	}

	/**
	 * @return the set of redefined rolenames at the far end of the edge
	 */
	public Set<String> getRedefinedThatRolenames() {
		if (direction == EdgeDirection.IN)
			return edgeClass.getRedefinedFromRoles();
		else
			return edgeClass.getRedefinedToRoles();
	}

	/**
	 * @return the set of redefined rolenames at the far end of the edge
	 */
	public Set<String> getRedefinedThisRolenames() {
		if (direction == EdgeDirection.OUT)
			return edgeClass.getRedefinedFromRoles();
		else
			return edgeClass.getRedefinedToRoles();
	}

}
