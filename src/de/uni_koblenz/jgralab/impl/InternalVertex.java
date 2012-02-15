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
package de.uni_koblenz.jgralab.impl;

import de.uni_koblenz.jgralab.Edge;
import de.uni_koblenz.jgralab.Vertex;
import de.uni_koblenz.jgralab.schema.impl.DirectedSchemaEdgeClass;

public interface InternalVertex extends Vertex, InternalGraphElement {

	/**
	 * @return the internal vertex structure version
	 * @see #isIncidenceListModified(long)
	 */
	public long getIncidenceListVersion();

	/**
	 * Must be called by all methods which manipulate the incidence list of this
	 * Vertex.
	 */
	public abstract void incidenceListModified();

	/**
	 * Checks if the list of incident edges has changed with respect to the
	 * given <code>incidenceListVersion</code>.
	 */
	public boolean isIncidenceListModified(long incidenceListVersion);

	/**
	 * tests if the Edge <code>edge</code> may start at this vertex
	 * 
	 * @return <code>true</code> iff <code>edge</code> may start at this vertex
	 */
	public boolean isValidAlpha(Edge edge);

	/**
	 * tests if the Edge <code>edge</code> may end at this vertex
	 * 
	 * @return <code>true</code> iff <code>edge</code> may end at this vertex
	 */
	public boolean isValidOmega(Edge edge);

	public DirectedSchemaEdgeClass getEdgeForRolename(String rolename);

	/**
	 * @return the next vertex in vSeq
	 */
	public InternalVertex getNextVertexInVSeq();

	/**
	 * @return the previous vertex in vSeq
	 */
	public InternalVertex getPrevVertexInVSeq();

	public void setNextVertex(Vertex nextVertex);

	public void setPrevVertex(Vertex prevVertex);

	/**
	 * @return first incident edge of this vertex
	 */
	public InternalEdge getFirstIncidenceInISeq();

	/**
	 * @return last incident edge of this vertex
	 */
	public InternalEdge getLastIncidenceInISeq();

	public void setFirstIncidence(InternalEdge firstIncidence);

	public void setLastIncidence(InternalEdge lastIncidence);

	public void appendIncidenceToISeq(InternalEdge i);

	public void removeIncidenceFromISeq(InternalEdge i);

	public void putIncidenceBefore(InternalEdge target, InternalEdge moved);

	public void putIncidenceAfter(InternalEdge target, InternalEdge moved);

	public void setIncidenceListVersion(long incidenceListVersion);

}
