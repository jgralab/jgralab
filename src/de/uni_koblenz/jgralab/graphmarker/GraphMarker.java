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

package de.uni_koblenz.jgralab.graphmarker;

import de.uni_koblenz.jgralab.AttributedElement;
import de.uni_koblenz.jgralab.Edge;
import de.uni_koblenz.jgralab.Graph;
import de.uni_koblenz.jgralab.Vertex;

/**
 * This class can be used to "colorize" graphs, edges and vertices. If a
 * algorithm only needs to distinguish between "marked" and "not marked", a look
 * at the class <code>BooleanGraphMarker</code> may be reasonable. If a specific
 * kind of marking is used, it may be reasonalbe to extends this GraphMarker. A
 * example how that could be done is located in the tutorial in the class
 * <code>DijkstraVertexMarker</code>.
 * 
 * This Marker only exists for compatibility reasons to older versions of
 * JGraLab. The new marker class <code>GenericGraphMarker</code> allows a
 * stricter limitation to specific <code>AttributedElement</code>s.
 * 
 * @author ist@uni-koblenz.de
 * 
 */
public class GraphMarker<O> extends MapGraphMarker<AttributedElement, O> {

	public GraphMarker(Graph g) {
		super(g);
	}

	@Override
	public void edgeDeleted(Edge e) {
		tempAttributeMap.remove(e);
	}

	@Override
	public void vertexDeleted(Vertex v) {
		tempAttributeMap.remove(v);
	}

}
