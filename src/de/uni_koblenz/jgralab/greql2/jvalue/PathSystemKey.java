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

package de.uni_koblenz.jgralab.greql2.jvalue;

import de.uni_koblenz.jgralab.Vertex;

/**
 * This is the key of the hashmap which stores the references to the parent
 * vertices. This class is _not_ a JValue
 */
public class PathSystemKey {

	private Vertex vertex;

	private int stateNumber;

	private int hashValue = 0;

	public int hashCode() {
		System.out.println("Vertex: " + vertex);
		if (hashValue == 0)
			hashValue = this.getClass().hashCode() + vertex.getId() * 2373  + stateNumber;
		return hashValue;
	}

	public boolean equals(Object o) {
		if (o instanceof PathSystemKey) {
			PathSystemKey foreignKey = (PathSystemKey) o;
			if ((foreignKey.vertex == vertex)
					&& (foreignKey.stateNumber == stateNumber))
				return true;
		}
		return false;
	}

	/**
	 * creates a new PathSystemKey
	 * 
	 * @param v
	 * @param s
	 */
	public PathSystemKey(Vertex v, int s) {
		vertex = v;
		stateNumber = s;
	}

	/**
	 * returns the string representation of this key
	 */
	public String toString() {
		return "(V: " + vertex.getId() + ", S: " + stateNumber + ")"
				+ "HashValue: " + hashCode();
	}

	public Vertex getVertex() {
		return vertex;
	}

	public void setVertex(Vertex vertex) {
		hashValue = 0;
		this.vertex = vertex;
	}

	public int getStateNumber() {
		return stateNumber;
	}

	public void setStateNumber(int stateNumber) {
		hashValue = 0;
		this.stateNumber = stateNumber;
	}

}