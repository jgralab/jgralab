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
package de.uni_koblenz.jgralab;

/**
 * Represents a path element in a path description given to
 * {@link Vertex#reachableVertices(Class, PathElement...)}.
 * 
 * @author Tassilo Horn &lt;horn@uni-koblenz.de&gt;
 * 
 */
public class PathElement {
	public Class<? extends Edge> edgeClass;
	public EdgeDirection edgeDirection;
	public boolean strictType = false;

	/**
	 * see {@link #PathElement(Class, EdgeDirection, boolean)}.
	 * <code>stricts</code> defaults to false.
	 */
	public PathElement(Class<? extends Edge> ec, EdgeDirection ed) {
		this.edgeClass = ec;
		this.edgeDirection = ed;
	}

	/**
	 * @param ec
	 *            the class of allowed edges
	 * @param ed
	 *            the direction of allowed edges
	 * @param strict
	 *            allow only the exact type
	 */
	public PathElement(Class<? extends Edge> ec, EdgeDirection ed,
			boolean strict) {
		this(ec, ed);
		this.strictType = strict;
	}
}
