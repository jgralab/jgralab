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
 
package de.uni_koblenz.jgralab.impl;

import de.uni_koblenz.jgralab.AttributedElement;
import de.uni_koblenz.jgralab.AttributedElementClass;
import de.uni_koblenz.jgralab.Edge;
import de.uni_koblenz.jgralab.EdgeClass;
import de.uni_koblenz.jgralab.Graph;
import de.uni_koblenz.jgralab.Vertex;



/**
 * represents an incidence object, created temporarily by Graph class,
 * delegates nearly all methods to corresponding Graph 
 * @author Daniel Bildhauer
 */
public abstract class EdgeBaseImpl extends GraphElementImpl implements Edge  {
	
	/**
	 * the id of this edge
	 */
	protected int id;
		
	/**
	 * the reversed edge
	 */
	protected ReversedEdgeBaseImpl reversedEdge;
	
	
	public final int getId() {
		return id;
	}
	
	public void setId(int id) {
		if ( (this.id == 0) && (id > 0))
			this.id = id;
	}
	
	
	/**
	 * creates the edge with id -id. 
	 * 
	 */
	public EdgeBaseImpl(int anId, Graph graph, AttributedElementClass cls) {
		super(graph, cls);
		id = anId;
		if (graph == null) {
			System.out.println("aGraph in EdgeBaseConstructor is null");
			System.exit(1);
		}
	}

	/* (non-Javadoc)
	 * @see jgralab.Edge#setThis(jgralab.Vertex)
	 */
	public void setThis(Vertex v) {
		setAlpha(v);
	}

	/* (non-Javadoc)
	 * @see jgralab.Edge#setThat(jgralab.Vertex)
	 */
	public void setThat(Vertex v) {
		setOmega(v);
	}

	/**
	 * returns true if this incidence is in normal order, false otherwise
	 */
	public final boolean isNormal() {
		return true;
	}
	
	/**
	 * returns the incidence which has the same direction like the edge
	 */
	public final Edge getNormalEdge() {
		return this;
	}
	
	
	public String toString() {
		return "e" + id + ": " + getAttributedElementClass().getName();
	}
	
	/**
	 * returns the incidence which has the opposite direction than the edge
	 */
	public final Edge getReversedEdge() {
		return reversedEdge;
	}
	
	public int compareTo(AttributedElement a) {
		if (a instanceof Edge) {
			Edge e = (Edge) a;
			return id - e.getId();
		}
		return -1;
	}
	
	public String getThisRole() {
		return ((EdgeClass)this.getAttributedElementClass()).getFromRolename();
	}
	
	public String getThatRole() {
		return ((EdgeClass)this.getAttributedElementClass()).getToRolename();
	}
	
}
