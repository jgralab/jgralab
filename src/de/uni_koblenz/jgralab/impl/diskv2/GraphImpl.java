package de.uni_koblenz.jgralab.impl.diskv2;

import java.util.List;

import de.uni_koblenz.jgralab.Edge;
import de.uni_koblenz.jgralab.TemporaryEdge;
import de.uni_koblenz.jgralab.TemporaryVertex;
import de.uni_koblenz.jgralab.Vertex;
import de.uni_koblenz.jgralab.exception.GraphException;
import de.uni_koblenz.jgralab.impl.EdgeBaseImpl;
import de.uni_koblenz.jgralab.impl.FreeIndexList;
import de.uni_koblenz.jgralab.impl.InternalEdge;
import de.uni_koblenz.jgralab.impl.InternalVertex;
import de.uni_koblenz.jgralab.impl.ReversedEdgeBaseImpl;
import de.uni_koblenz.jgralab.impl.VertexBaseImpl;
import de.uni_koblenz.jgralab.schema.EdgeClass;
import de.uni_koblenz.jgralab.schema.GraphClass;
import de.uni_koblenz.jgralab.schema.VertexClass;

	/**
	 * The implementation of a <code>Graph</code> accessing attributes without
	 * versioning.
	 * 
	 * @author Jose Monte(monte@uni-koblenz.de)
	 */
	public abstract class GraphImpl extends
			de.uni_koblenz.jgralab.impl.GraphBaseImpl {
		private MemStorageManager storage;
		
		private int vCount;

		private int eCount;
		private int firstVertexId;
		private int lastVertexId;
		private int firstEdgeId;
		private int lastEdgeId;

		/**
		 * Holds the version of the vertex sequence. For every modification (e.g.
		 * adding/deleting a vertex or changing the vertex sequence) this version
		 * number is increased by 1. It is set to 0 when the graph is loaded.
		 */
		private long vertexListVersion;

		/**
		 * Holds the version of the edge sequence. For every modification (e.g.
		 * adding/deleting an edge or changing the edge sequence) this version
		 * number is increased by 1. It is set to 0 when the graph is loaded.
		 */
		private long edgeListVersion;

		/**
		 * List of vertices to be deleted by a cascading delete caused by deletion
		 * of a composition "parent".
		 */
		private List<InternalVertex> deleteVertexList;

		
		public MemStorageManager getStorage(){
			return storage;
		}
		
		@Override
		public InternalVertex[] getVertex() {
			throw new UnsupportedOperationException();
		}

		@Override
		public int getVCountInVSeq() {
			return vCount;
		}

		@Override
		public InternalEdge[] getEdge() {
			throw new UnsupportedOperationException();
		}

		@Override
		public InternalEdge[] getRevEdge() {
			throw new UnsupportedOperationException();
		}

		@Override
		public int getECountInESeq() {
			return eCount;
		}

		@Override
		public InternalVertex getFirstVertexInVSeq() {
			return (InternalVertex) this.storage.getVertexObject(firstVertexId);
		}

		@Override
		public InternalVertex getLastVertexInVSeq() {
			return (InternalVertex) this.storage.getVertexObject(lastVertexId);
		}

		@Override
		public InternalEdge getFirstEdgeInESeq() {
			return (InternalEdge) this.storage.getEdgeObject(firstEdgeId);
		}

		@Override
		public InternalEdge getLastEdgeInESeq() {
			return (InternalEdge) this.storage.getEdgeObject(lastEdgeId);
		}

		@Override
		public FreeIndexList getFreeVertexList() {
			return freeVertexList;
		}

		@Override
		public FreeIndexList getFreeEdgeList() {
			return freeEdgeList;
		}

		@Override
		public void setVertex(InternalVertex[] vertex) {
			throw new UnsupportedOperationException();
		}

		@Override
		public void setVCount(int count) {
			vCount = count;
		}

		@Override
		public void setEdge(InternalEdge[] edge) {
			throw new UnsupportedOperationException();
		}

		@Override
		public void setRevEdge(InternalEdge[] revEdge) {
			throw new UnsupportedOperationException();
		}

		@Override
		public void setECount(int count) {
			eCount = count;
		}

		@Override
		public void setFirstVertex(InternalVertex firstVertex) {
			this.firstVertexId = firstVertex.getId();
		}

		@Override
		public void setLastVertex(InternalVertex lastVertex) {
			this.lastVertexId = lastVertex.getId();
		}

		@Override
		public void setFirstEdgeInGraph(InternalEdge firstEdge) {
			this.firstEdgeId = firstEdge.getId();
		}

		@Override
		public void setLastEdgeInGraph(InternalEdge lastEdge) {
			this.lastEdgeId = lastEdge.getId();
		}

		@Override
		public List<InternalVertex> getDeleteVertexList() {
			return deleteVertexList;
		}

		@Override
		public void setDeleteVertexList(List<InternalVertex> deleteVertexList) {
			this.deleteVertexList = deleteVertexList;
		}

		@Override
		public void setVertexListVersion(long vertexListVersion) {
			this.vertexListVersion = vertexListVersion;
		}

		@Override
		public long getVertexListVersion() {
			return vertexListVersion;
		}

		@Override
		public void setEdgeListVersion(long edgeListVersion) {
			this.edgeListVersion = edgeListVersion;
		}

		@Override
		public long getEdgeListVersion() {
			return edgeListVersion;
		}

		/**
		 * 
		 * @param id
		 * @param cls
		 * @param max
		 * @param max2
		 */
		protected GraphImpl(String id, GraphClass cls, int max, int max2) {
			super(id, cls, max, max2);
			this.storage = new MemStorageManager(this);
		}

		protected GraphImpl(String id, GraphClass cls) {
			super(id, cls);
			this.storage = new MemStorageManager(this);
		}

		@Override
		public int allocateVertexIndex(int currentId) {
			int vId = freeVertexList.allocateIndex();
			if (vId == 0) {
				expandVertexArray(getExpandedVertexCount());
				vId = freeVertexList.allocateIndex();
			}
			return vId;
		}

		@Override
		public int allocateEdgeIndex(int currentId) {
			int eId = freeEdgeList.allocateIndex();
			if (eId == 0) {
				expandEdgeArray(getExpandedEdgeCount());
				eId = freeEdgeList.allocateIndex();
			}
			return eId;
		}

		/*
		 * @Override protected void freeIndex(FreeIndexList freeIndexList, int
		 * index) { freeIndexList.freeIndex(index); }
		 */

		@Override
		public void freeEdgeIndex(int index) {
			freeEdgeList.freeIndex(index);
		}

		@Override
		public void freeVertexIndex(int index) {
			freeVertexList.freeIndex(index);
		}

		@Override
		public TemporaryVertex createTemporaryVertex() {
			throw new UnsupportedOperationException("No support for TemporaryElements.");
		}

		@Override
		public TemporaryVertex createTemporaryVertex(VertexClass preliminaryType) {
			throw new UnsupportedOperationException("No support for TemporaryElements.");
		}

		@Override
		public TemporaryEdge createTemporaryEdge(Vertex alpha, Vertex omega) {
			throw new UnsupportedOperationException("No support for TemporaryElements.");
		}

		@Override
		public TemporaryEdge createTemporaryEdge(EdgeClass preliminaryType,
				Vertex alpha, Vertex omega) {
			throw new UnsupportedOperationException("No support for TemporaryElements.");
		}

		@Override
		public boolean hasTemporaryElements() {
			return false;
		}
		
		// ---- Overrides
		
		@Override
		public void appendVertexToVSeq(InternalVertex v) {
			this.storage.putVertex((VertexImpl)v);
			setVCount(getVCountInVSeq() + 1);
			if (getFirstVertexInVSeq() == null) {
				setFirstVertex(v);
			}
			if (getLastVertexInVSeq() != null) {
				(getLastVertexInVSeq()).setNextVertex(v);
				v.setPrevVertex(getLastVertexInVSeq());
			}
			setLastVertex(v);
		}
		
		@Override
		public final void removeVertexFromVSeq(InternalVertex v) {
			assert v != null;
			if (v == getFirstVertexInVSeq()) {
				// delete at head of vertex list
				setFirstVertex(v.getNextVertexInVSeq());
				if (getFirstVertexInVSeq() != null) {
					(getFirstVertexInVSeq()).setPrevVertex(null);
				}
				if (v == getLastVertexInVSeq()) {
					// this vertex was the only one...
					setLastVertex(null);
				}
			} else if (v == getLastVertexInVSeq()) {
				// delete at tail of vertex list
				setLastVertex(v.getPrevVertexInVSeq());
				if (getLastVertexInVSeq() != null) {
					(getLastVertexInVSeq()).setNextVertex(null);
				}
			} else {
				// delete somewhere in the middle
				(v.getPrevVertexInVSeq()).setNextVertex(v.getNextVertexInVSeq());
				(v.getNextVertexInVSeq()).setPrevVertex(v.getPrevVertexInVSeq());
			}
			// freeIndex(getFreeVertexList(), v.getId());
			freeVertexIndex(v.getId());
			//getVertex()[v.getId()] = null;
			this.storage.removeVertex(v.getId());
			v.setPrevVertex(null);
			v.setNextVertex(null);
			v.setId(0);
			setVCount(getVCountInVSeq() - 1);
		}
		
		@Override
		public void appendEdgeToESeq(InternalEdge e) {
			//getEdge()[((EdgeBaseImpl) e).id] = e;
			this.storage.putEdge((EdgeImpl) e);
			//getRevEdge()[((EdgeBaseImpl) e).id] = ((EdgeBaseImpl) e).reversedEdge;
			setECount(getECountInESeq() + 1);
			if (getFirstEdgeInESeq() == null) {
				setFirstEdgeInGraph(e);
			}
			if (getLastEdgeInESeq() != null) {
				(getLastEdgeInESeq()).setNextEdgeInGraph(e);

				e.setPrevEdgeInGraph(getLastEdgeInESeq());

			}
			setLastEdgeInGraph(e);
		}
		
		@Override
		public void removeEdgeFromESeq(InternalEdge e) {
			assert e != null;
			removeEdgeFromESeqWithoutDeletingIt(e);

			// freeIndex(getFreeEdgeList(), e.getId());
			freeEdgeIndex(e.getId());
			//getEdge()[e.getId()] = null;
			//getRevEdge()[e.getId()] = null;
			this.storage.removeEdge(e.getId());
			e.setPrevEdgeInGraph(null);
			e.setNextEdgeInGraph(null);
			e.setId(0);
			setECount(getECountInESeq() - 1);
		}
		
		@Override
		public Vertex getVertex(int vId) {
			assert (vId > 0) : "The vertex id must be > 0, given was " + vId;
			try {
				return this.storage.getVertexObject(vId);
			} catch (ArrayIndexOutOfBoundsException e) {
				return null;
			}
		}
		
		@Override
		public Edge getEdge(int eId) {
			assert eId != 0 : "The edge id must be != 0, given was " + eId;
			try {
				return this.storage.getEdgeObject(eId);// eId < 0 ? getRevEdge()[-eId] : getEdge()[eId];
			} catch (ArrayIndexOutOfBoundsException e) {
				return null;
			}
		}
		
		@Override
		public  void defragment(){
			throw new UnsupportedOperationException();
		}
		
		/**
		 * Changes the size of the edge array of this graph to newSize.
		 * 
		 * @param newSize
		 *            the new size of the edge array
		 */
		@Override
		public void expandEdgeArray(int newSize) {
			if (newSize <= eMax) {
				throw new GraphException("newSize must be > eSize: eSize=" + eMax
						+ ", newSize=" + newSize);
			}
			if (getFreeEdgeList() == null) {
				this.freeEdgeList = new FreeIndexList(newSize);
			} else {
				getFreeEdgeList().expandBy(newSize - eMax);
			}

			eMax = newSize;
			notifyMaxEdgeCountIncreased(newSize);
		}

		/**
		 * Changes the size of the vertex array of this graph to newSize.
		 * 
		 * @param newSize
		 *            the new size of the vertex array
		 */
		@Override
		public void expandVertexArray(int newSize) {
			if (newSize <= vMax) {
				throw new GraphException("newSize must > vSize: vSize=" + vMax
						+ ", newSize=" + newSize);
			}		
			if (getFreeVertexList() == null) {
				this.freeVertexList = new FreeIndexList(newSize);
			} else {
				getFreeVertexList().expandBy(newSize - vMax);
			}
			vMax = newSize;
			notifyMaxVertexCountIncreased(newSize);
		}
		
		//TODO load more efficient 
		@Override
		public void internalLoadingCompleted(int[] firstIncidence,
				int[] nextIncidence) {
			//getFreeVertexList().reinitialize(getVertex());
			//getFreeEdgeList().reinitialize(getEdge());
			//for (int vId = 1; vId < getVertex().length; ++vId) {
			for(int vId = 1; vId < this.vCount; ++vId){	
				InternalVertex v = (InternalVertex) getVertex(vId);
				if (v != null) {
					int eId = firstIncidence[vId];
					while (eId != 0) {
						v.appendIncidenceToISeq((InternalEdge) this.getEdge(eId));
								//eId < 0 ? getRevEdge()[-eId]
								//: getEdge()[eId]);
						eId = nextIncidence[eMax + eId];
					}
				}
			}
		}
}
