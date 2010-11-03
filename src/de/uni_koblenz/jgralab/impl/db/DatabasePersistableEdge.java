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
package de.uni_koblenz.jgralab.impl.db;

import de.uni_koblenz.jgralab.Edge;

/**
 * An edge which can be persisted to database.
 * 
 * @author ultbreit@uni-koblenz.de
 */
public interface DatabasePersistableEdge extends
		DatabasePersistableGraphElement, DatabasePersistableIncidence, Edge {

	/**
	 * Gets number mapping edge's sequence in ESeq of graph.
	 * 
	 * @return Number mapping edge's sequence in ESeq of graph.
	 */
	long getSequenceNumberInESeq();

	/**
	 * Sets number mapping edge's sequence in ESeq of graph.
	 * 
	 * @param sequenceNumberInEseq
	 *            Number mapping edge's sequence in ESeq of graph.
	 */
	void setSequenceNumberInESeq(long sequenceNumber);
}
