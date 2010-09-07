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
package de.uni_koblenz.jgralab.impl.trans;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import de.uni_koblenz.jgralab.AttributedElement;
import de.uni_koblenz.jgralab.impl.IncidenceImpl;
import de.uni_koblenz.jgralab.trans.ListPosition;
import de.uni_koblenz.jgralab.trans.Savepoint;
import de.uni_koblenz.jgralab.trans.Transaction;
import de.uni_koblenz.jgralab.trans.VersionedDataObject;
import de.uni_koblenz.jgralab.trans.VertexPosition;

/**
 * Implementation of a save-point.
 * 
 * @author Jose Monte(monte@uni-koblenz.de)
 */
public class SavepointImpl implements Savepoint {
	private short id;
	private TransactionImpl transaction;
	protected long versionAtSavepoint;

	// stored change sets of transaction for this save-point
	protected List<VertexImpl> addedVertices;
	protected List<EdgeImpl> addedEdges;
	protected List<VertexImpl> deletedVertices;
	protected List<EdgeImpl> deletedEdges;
	protected Map<VertexImpl, Map<ListPosition, Boolean>> changedVseqVertices;
	protected Map<EdgeImpl, Map<ListPosition, Boolean>> changedEseqEdges;
	protected Map<VertexImpl, Map<IncidenceImpl, Map<ListPosition, Boolean>>> changedIncidences;
	protected Map<EdgeImpl, VertexPosition> changedEdges;
	protected Map<AttributedElement, Set<VersionedDataObject<?>>> changedAttributes;

	/**
	 * 
	 * @param transaction
	 *            the <code>Transaction</code> to which the
	 *            <code>Savepoint</code> belongs to
	 */
	protected SavepointImpl(TransactionImpl transaction, short id) {
		assert (transaction != null);
		this.transaction = transaction;
		this.id = id;

		// store temporary version counter!!!
		versionAtSavepoint = transaction.temporaryVersionCounter;
		if (transaction.addedVertices != null) {
			addedVertices = new ArrayList<VertexImpl>(1);
			addedVertices.addAll(transaction.addedVertices);
		}
		if (transaction.addedEdges != null) {
			addedEdges = new ArrayList<EdgeImpl>(1);
			addedEdges.addAll(transaction.addedEdges);
		}
		if (transaction.deletedVertices != null) {
			deletedVertices = new ArrayList<VertexImpl>(1);
			deletedVertices.addAll(transaction.deletedVertices);
		}
		if (transaction.deletedEdges != null) {
			deletedEdges = new ArrayList<EdgeImpl>(1);
			deletedEdges.addAll(transaction.deletedEdges);
		}
		if (transaction.changedVseqVertices != null) {
			changedVseqVertices = new HashMap<VertexImpl, Map<ListPosition, Boolean>>(
					1, TransactionManagerImpl.LOAD_FACTOR);
			changedVseqVertices.putAll(transaction.changedVseqVertices);
		}
		if (transaction.changedEseqEdges != null) {
			changedEseqEdges = new HashMap<EdgeImpl, Map<ListPosition, Boolean>>(
					1, TransactionManagerImpl.LOAD_FACTOR);
			changedEseqEdges.putAll(transaction.changedEseqEdges);
		}
		if (transaction.changedIncidences != null) {
			changedIncidences = new HashMap<VertexImpl, Map<IncidenceImpl, Map<ListPosition, Boolean>>>(
					1, TransactionManagerImpl.LOAD_FACTOR);
			changedIncidences.putAll(transaction.changedIncidences);
		}
		if (transaction.changedEdges != null) {
			changedEdges = new HashMap<EdgeImpl, VertexPosition>(1,
					TransactionManagerImpl.LOAD_FACTOR);
			changedEdges.putAll(transaction.changedEdges);
		}
		if (transaction.changedAttributes != null) {
			changedAttributes = new HashMap<AttributedElement, Set<VersionedDataObject<?>>>(
					1, TransactionManagerImpl.LOAD_FACTOR);
			changedAttributes.putAll(transaction.changedAttributes);
		}
	}

	@Override
	public int getID() {
		return id;
	}

	@Override
	public Transaction getTransaction() {
		return transaction;
	}

	@Override
	public boolean isValid() {
		return transaction.getSavepoints().contains(this);
	}

	@Override
	public String toString() {
		return "SP-" + id + "_" + transaction;
	}
}
