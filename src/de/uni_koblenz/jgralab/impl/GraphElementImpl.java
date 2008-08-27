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
 
package de.uni_koblenz.jgralab.impl;

import de.uni_koblenz.jgralab.Graph;
import de.uni_koblenz.jgralab.schema.AttributedElementClass;
import de.uni_koblenz.jgralab.schema.GraphClass;
import de.uni_koblenz.jgralab.schema.Schema;

public abstract class GraphElementImpl extends AttributedElementImpl {

	/**
	 * holds the version of the graphelement, for every modification 
	 * (e.g. adding an edge or changing incidence sequence of a vertex)
	 * this version number is increased by one
	 * It is set to 0 when the graph is loaded.
	 */
	protected long graphVersion;
	
	public GraphElementImpl(Graph graph, AttributedElementClass cls) {
		super(cls);
		myGraph = graph;
		if (graph == null) {
			System.out.println("graph in GraphElementConstructor is null");
			System.exit(1);
		}
	}

	/**
	 * reference to the corresponding graph, all operations
	 * are redirected to it because only myGraph knows the
	 * whole graph structure  
	 */
	protected Graph myGraph;
	
	public Graph getGraph() {
		return myGraph;
	}
	
	public GraphClass getGraphClass() {
		return (GraphClass) myGraph.getAttributedElementClass();
	}
	
	/* (non-Javadoc)
	 * @see jgralab.AttributedElement#getSchema()
	 */
	public Schema getSchema() {
		return myGraph.getSchema();
	}
	


	public void graphModified() {
		myGraph.graphModified();
	}
}
