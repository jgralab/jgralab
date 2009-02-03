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
 
package de.uni_koblenz.jgralab.greql2.funlib.pathsearch;

import de.uni_koblenz.jgralab.Vertex;
import de.uni_koblenz.jgralab.greql2.evaluator.fa.State;

/**
 * This class modells a entry in the Queue which is used for regular pathsearch.
 * It contains a vertex which must be visited and a state in which the dfa is
 * when the vertex is visited
 * 
 * @author ist@uni-koblenz.de
 * 
 */
public class PathSearchQueueEntry {

	/**
	 * The vertex of the datagraph which should be visited
	 */
	public Vertex vertex;

	/**
	 * the state of the dfa
	 */
	public State state;

	public PathSearchQueueEntry(Vertex v, State s) {
		vertex = v;
		state = s;
	}

}
