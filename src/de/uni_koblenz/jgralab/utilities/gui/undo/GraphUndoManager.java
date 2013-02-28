/*
 * JGraLab - The Java Graph Laboratory
 *
 * Copyright (C) 2006-2013 Institute for Software Technology
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
package de.uni_koblenz.jgralab.utilities.gui.undo;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Stack;

import javax.swing.undo.AbstractUndoableEdit;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;
import javax.swing.undo.CompoundEdit;
import javax.swing.undo.UndoManager;

import de.uni_koblenz.jgralab.AttributedElement;
import de.uni_koblenz.jgralab.Edge;
import de.uni_koblenz.jgralab.Graph;
import de.uni_koblenz.jgralab.GraphChangeListener;
import de.uni_koblenz.jgralab.GraphElement;
import de.uni_koblenz.jgralab.TraversalContext;
import de.uni_koblenz.jgralab.Vertex;
import de.uni_koblenz.jgralab.schema.Attribute;
import de.uni_koblenz.jgralab.schema.AttributedElementClass;
import de.uni_koblenz.jgralab.schema.EdgeClass;
import de.uni_koblenz.jgralab.schema.VertexClass;

/**
 * {@link GraphUndoManager} is a {@link GraphChangeListener} that records
 * changes to a {@link Graph}. It can be used in interactive applications to
 * implement undo/redo functionality. Though the undo manager is derived from
 * Swing classes, it's not required that {@link GraphUndoManager} is used in
 * Swing GUI applications.
 * 
 * The constructor does not automatically register the {@link GraphUndoManager}
 * as {@link GraphChangeListener} in its graph. This is up to the user.
 * 
 * @author ist@uni-koblenz.de
 */
public class GraphUndoManager extends UndoManager implements
		GraphChangeListener {
	private static final long serialVersionUID = -1959515066823695788L;

	/**
	 * The {@link Graph} for which this {@link GraphUndoManager} is registered.
	 */
	private Graph graph;

	/**
	 * true when this {@link GraphUndoManager} is doing an undo or redo
	 * (prevents recording of new edits when undo/redo change the graph)
	 */
	private boolean working;

	/**
	 * Version number keeps track of versions of {@link Vertex} and {@link Edge}
	 * objects. Versioning is needed since the IDs of resurrected elements (upon
	 * redo) can change, and an element with the same ID can be deleted/created
	 * many times in complex {@link GraphEdit} sequences. Each time an element
	 * is referenced for the first time, or when it's recreated, the version
	 * counter is increased. That way, all edits referring a specific element
	 * can be updated with the new ID.
	 */
	private int version;

	/**
	 * Keeps track of most recent version of elements. Key is negative for
	 * edges, positive for vertices.
	 */
	private HashMap<Integer, Integer> versions = new HashMap<Integer, Integer>();

	/**
	 * first and last {@link GraphEdit} in a doubly linked list of all live
	 * edits. This list is needed to update element references when the versions
	 * of resurrected elements change.
	 */
	private GraphEdit first;
	private GraphEdit last;

	/**
	 * For convenience, {@link #beginEdit(String)} and {@link #endEdit()}
	 * methods can be used to group multiple graph changes into single
	 * {@link CompoundGraphEdit}s. This stack keeps track of the active
	 * {@link CompoundGraphEdit}s.
	 */
	private Stack<CompoundGraphEdit> compoundEditStack = new Stack<CompoundGraphEdit>();

	/**
	 * Data needed to delete/resurrect a {@link Vertex} and all incident
	 * {@link Edge}s. See {@link DeleteVertexEdit} and {@link DeleteEdgeEdit}
	 * for more information.
	 */
	private CompoundEdit deleteVertexCompound;
	private DeleteVertexEdit deleteVertexEdit;
	private boolean undoDeleteVertex;
	private ArrayList<Edge> correctIncidences = new ArrayList<Edge>();
	private ArrayList<Integer> correctPositions = new ArrayList<Integer>();

	/**
	 * GraphEdit events.
	 */
	protected enum GraphEditEvent {
		CREATE_VERTEX, CREATE_EDGE, DELETE_VERTEX, DELETE_EDGE, CHANGE_OMEGA, CHANGE_ALPHA, PUT_INCIDENCE_BEFORE, PUT_INCIDENCE_AFTER, CHANGE_ATTRIBUTE
	}

	/**
	 * Base class of all {@link GraphEdit} operations.
	 */
	protected abstract class GraphEdit extends AbstractUndoableEdit {
		private static final long serialVersionUID = 1100368833451887175L;

		/**
		 * Previous/next edit in the doubly linked list of all live
		 * {@link GraphEdit}s.
		 */
		GraphEdit prev;
		GraphEdit next;

		/**
		 * Element reference and version of this {@link GraphEdit}. elementId is
		 * positive for vertices, negative for edges and 0 for the graph itself.
		 */
		int elementId;
		int elementVersion;

		/**
		 * Element class and event type.
		 */
		AttributedElementClass<?, ?> aec;
		GraphEditEvent event;

		GraphEdit(GraphEditEvent event, AttributedElement<?, ?> el) {
			this.event = event;
			aec = el.getAttributedElementClass();
			elementId = (el instanceof Vertex) ? ((Vertex) el).getId()
					: (el instanceof Edge) ? -Math.abs(((Edge) el).getId()) : 0;
			elementVersion = elementVersion(elementId);

			// append this GraphEdit to the doubly linked list of live edits
			if (first == null) {
				first = last = this;
			} else {
				this.prev = last;
				last.next = this;
				last = this;
			}
		}

		// remove comments below to print undo/redo operations to System.out

		// @Override
		// public void undo() throws CannotUndoException {
		// System.out.println("UNDO " + this);
		// super.undo();
		// }
		//
		// @Override
		// public void redo() throws CannotRedoException {
		// System.out.println("REDO " + this);
		// super.redo();
		// }

		/**
		 * Returns the most recent version for the specified
		 * <code>elementId</code> or creates a new version when the element was
		 * never referenced.
		 * 
		 * @param elementId
		 *            an element id
		 * @return version number for <code>elementId</code>
		 */
		int elementVersion(Integer elementId) {
			Integer v = versions.get(elementId);
			if (v == null) {
				versions.put(elementId, ++version);
				return version;
			}
			return v;
		}

		/**
		 * Recreate a {@link Vertex} of class <code>vc</code> and correct the
		 * vertex references of all live {@link GraphEdit}s to the new id and
		 * version values.
		 * 
		 * @param vc
		 *            {@link VertexClass} of the Vertex to be created.
		 * @return
		 */
		Vertex resurrectVertex(VertexClass vc) {
			Vertex v = graph.createVertex(vc);
			int oldId = elementId;
			int oldVersion = elementVersion;
			elementId = v.getId();
			elementVersion = ++version;
			versions.put(elementId, elementVersion);
			// System.out.println("\tWAS v" + oldId + "-" + oldVersion +
			// ", is v"
			// + elementId + "-" + elementVersion);
			for (GraphEdit g = first; g != null; g = g.next) {
				g.changeVertexId(oldId, oldVersion, elementId, elementVersion);
			}
			return v;
		}

		/**
		 * Recreate an {@link Edge} of class <code>ec</code> from
		 * <code>alpha</code> to <code>omega</code> and correct the edge
		 * references of all live {@link GraphEdit}s to the new id and version.
		 * 
		 * @param ec
		 *            {@link EdgeClass} of the Edge to be created.
		 * @param alpha
		 *            start Vertex
		 * @param omega
		 *            end Vertex
		 * @return
		 */
		Edge resurrectEdge(EdgeClass ec, Vertex alpha, Vertex omega) {
			Edge e = graph.createEdge((EdgeClass) aec, alpha, omega);
			int oldId = elementId;
			int oldVersion = elementVersion;
			elementId = -e.getId();
			elementVersion = ++version;
			versions.put(elementId, elementVersion);
			// System.out.println("\tWAS e" + (-oldId) + "-" + oldVersion
			// + ", is e" + (-elementId) + "-" + elementVersion);
			for (GraphEdit g = first; g != null; g = g.next) {
				g.changeEdgeId(oldId, oldVersion, elementId, elementVersion);
			}
			return e;
		}

		@Override
		public String toString() {
			String s = super.toString() + ", ";
			s += event + " " + aec.getSimpleName() + " ";
			s += (elementId > 0 ? "v" + elementId : elementId < 0 ? "e"
					+ (-elementId) : "graph")
					+ "-" + elementVersion;
			return s;
		}

		@Override
		public void die() {
			super.die();
			// remove this edit from the live edit list
			if (first == this && first == last) {
				first = last = null;
			} else {
				if (prev != null) {
					prev.next = next;
				} else {
					first = next;
				}
				if (next != null) {
					next.prev = prev;
				} else {
					last = prev;
				}
			}
		}

		/**
		 * Changes vertex references of this {@link GraphEdit} to new version.
		 */
		void changeVertexId(int from, int fromVersion, int to, int toVersion) {
			if (elementId > 0 && elementId == from
					&& elementVersion == fromVersion) {
				elementId = to;
				elementVersion = toVersion;
			}
		}

		/**
		 * Changes edge references of this {@link GraphEdit} to new version.
		 */
		void changeEdgeId(int from, int fromVersion, int to, int toVersion) {
			if (elementId < 0 && elementId == from
					&& elementVersion == fromVersion) {
				elementId = to;
				elementVersion = fromVersion;
			}
		}
	}

	/**
	 * Records creation of a {@link Vertex}.
	 */
	protected class CreateVertexEdit extends GraphEdit {
		private static final long serialVersionUID = 7455868109487075050L;

		CreateVertexEdit(Vertex v) {
			super(GraphEditEvent.CREATE_VERTEX, v);
		}

		@Override
		public void undo() throws CannotUndoException {
			super.undo();
			graph.deleteVertex(graph.getVertex(elementId));
		}

		@Override
		public void redo() throws CannotRedoException {
			super.redo();
			resurrectVertex((VertexClass) aec);
		}
	}

	/**
	 * Records creation of an {@link Edge}.
	 */
	protected class CreateEdgeEdit extends GraphEdit {
		private static final long serialVersionUID = 8840782477572584494L;
		private int alphaId, omegaId, alphaVersion, omegaVersion;

		CreateEdgeEdit(Edge e) {
			super(GraphEditEvent.CREATE_EDGE, e);
			assert e.isNormal();
			alphaId = e.getAlpha().getId();
			alphaVersion = elementVersion(alphaId);
			omegaId = e.getOmega().getId();
			omegaVersion = elementVersion(omegaId);
		}

		@Override
		public void undo() throws CannotUndoException {
			super.undo();
			graph.deleteEdge(graph.getEdge(-elementId));
		}

		@Override
		public void redo() throws CannotRedoException {
			super.redo();
			Vertex alpha = graph.getVertex(alphaId);
			Vertex omega = graph.getVertex(omegaId);
			resurrectEdge((EdgeClass) aec, alpha, omega);
		}

		@Override
		public void changeVertexId(int from, int fromVersion, int to,
				int toVersion) {
			// no super call since this edit is an edge edit

			// update references of alpha and omega vertices
			if (alphaId == from && alphaVersion == fromVersion) {
				alphaId = to;
				alphaVersion = toVersion;
			}
			if (omegaId == from && omegaVersion == toVersion) {
				omegaId = to;
				omegaVersion = toVersion;
			}
		}

		@Override
		public String toString() {
			return super.toString() + " from v" + alphaId + "-" + alphaVersion
					+ " to v" + omegaId + "-" + omegaVersion;
		}
	}

	/**
	 * Base class of Delete edits, records attribute values before deletion.
	 */
	protected abstract class DeleteElementEdit extends GraphEdit {
		private static final long serialVersionUID = -6211867897158716199L;
		private Object[] attrValues;

		public DeleteElementEdit(GraphEditEvent event, GraphElement<?, ?> el) {
			super(event, el);
			attrValues = new Object[aec.getAttributeCount()];
			int i = 0;
			for (Attribute attr : aec.getAttributeList()) {
				attrValues[i++] = el.getAttribute(attr.getName());
			}
		}

		protected void restoreAttributes(GraphElement<?, ?> el) {
			assert el != null;
			assert attrValues != null;
			int i = 0;
			for (Attribute attr : aec.getAttributeList()) {
				el.setAttribute(attr.getName(), attrValues[i++]);
			}
		}
	}

	/**
	 * Records deletion of a {@link Vertex}.
	 * 
	 * This edit does not represent automatic deletions of incident edges, those
	 * are recorded in a special {@link CompoundEdit}. When cascading deletes of
	 * composite children happen, those are also recorded in that
	 * {@link CompoundEdit}.
	 */
	protected class DeleteVertexEdit extends DeleteElementEdit {
		private static final long serialVersionUID = -4787860787912669387L;

		public DeleteVertexEdit(Vertex v) {
			super(GraphEditEvent.DELETE_VERTEX, v);
		}

		@Override
		public void undo() throws CannotUndoException {
			super.undo();
			Vertex v = resurrectVertex((VertexClass) aec);
			restoreAttributes(v);
		}

		@Override
		public void redo() throws CannotRedoException {
			super.redo();
			graph.deleteVertex(graph.getVertex(elementId));
		}
	}

	/**
	 * Records deletion of an {@link Edge}.
	 */
	protected class DeleteEdgeEdit extends DeleteElementEdit {
		private static final long serialVersionUID = 6430961207052596036L;
		private int alphaId, omegaId, alphaVersion, omegaVersion;
		private int alphaInc, omegaInc;

		public DeleteEdgeEdit(Edge e) {
			super(GraphEditEvent.DELETE_EDGE, e);
			assert e.isNormal();
			alphaId = e.getAlpha().getId();
			alphaVersion = elementVersion(alphaId);
			omegaId = e.getOmega().getId();
			omegaVersion = elementVersion(omegaId);
			TraversalContext tc = graph.setTraversalContext(null);
			alphaInc = incidencePosition(e);
			omegaInc = incidencePosition(e.getReversedEdge());
			graph.setTraversalContext(tc);
		}

		@Override
		public void undo() throws CannotUndoException {
			// Upon undo, the resurrected edge is put into the correct places in
			// the incidence sequences. If this {@link DeleteEdgeEdit} is part
			// of a {@link Vertex} deletion, incidence numbers are corrected
			// only after all incident edges are resurrected.
			super.undo();
			Vertex alpha = graph.getVertex(alphaId);
			Vertex omega = graph.getVertex(omegaId);
			Edge e = resurrectEdge((EdgeClass) aec, alpha, omega);
			restoreAttributes(e);
			if (undoDeleteVertex) {
				// undo is part of undoDeleteVertex
				// record required changes
				correctIncidences.add(e);
				correctPositions.add(alphaInc);
				correctPositions.add(omegaInc);
			} else {
				// undo is not part of undoDeleteVertex
				// correct incidence positions
				TraversalContext tc = graph.setTraversalContext(null);
				putIncidenceAt(e, alphaInc);
				putIncidenceAt(e.getReversedEdge(), omegaInc);
				graph.setTraversalContext(tc);
			}
		}

		@Override
		public void redo() throws CannotRedoException {
			super.redo();
			graph.deleteEdge(graph.getEdge(-elementId));
		}

		@Override
		public void changeVertexId(int from, int fromVersion, int to,
				int toVersion) {
			// no super call since this is an edge edit

			// adjust alpha and omega references
			if (alphaId == from && alphaVersion == fromVersion) {
				alphaId = to;
				alphaVersion = toVersion;
			}
			if (omegaId == from && omegaVersion == fromVersion) {
				omegaId = to;
				omegaVersion = toVersion;
			}
		}

		@Override
		public String toString() {
			return super.toString() + " from v" + alphaId + "-" + alphaVersion
					+ " to v" + omegaId + "-" + omegaVersion;
		}
	}

	/**
	 * Records intentional changes in incidence order (putIncidenceBefor/After).
	 */
	protected class ChangeIncidenceOrderEdit extends GraphEdit {
		private static final long serialVersionUID = 1553029429775858722L;
		private int otherId, otherVersion, oldIncidencePos;
		private boolean thisOutgoing, otherOutgoing;

		public ChangeIncidenceOrderEdit(GraphEditEvent event, Edge inc,
				Edge other) {
			super(event, inc);
			otherId = -Math.abs(other.getId());
			otherVersion = elementVersion(otherId);
			thisOutgoing = inc.isNormal();
			otherOutgoing = other.isNormal();
			TraversalContext tc = graph.setTraversalContext(null);
			oldIncidencePos = incidencePosition(inc);
			graph.setTraversalContext(tc);
		}

		@Override
		public void undo() throws CannotUndoException {
			super.undo();
			Edge inc = graph.getEdge(thisOutgoing ? -elementId : elementId);
			TraversalContext tc = graph.setTraversalContext(null);
			putIncidenceAt(inc, oldIncidencePos);
			graph.setTraversalContext(tc);
		}

		@Override
		public void redo() throws CannotRedoException {
			super.redo();
			Edge inc = graph.getEdge(thisOutgoing ? -elementId : elementId);
			Edge other = graph.getEdge(otherOutgoing ? -otherId : otherId);
			if (event == GraphEditEvent.PUT_INCIDENCE_AFTER) {
				inc.putIncidenceAfter(other);
			} else {
				inc.putIncidenceBefore(other);
			}
		}

		@Override
		void changeEdgeId(int from, int fromVersion, int to, int toVersion) {
			super.changeEdgeId(from, fromVersion, to, toVersion);
			if (otherId == from && otherVersion == fromVersion) {
				otherId = to;
				otherVersion = toVersion;
			}
		}

		@Override
		public String toString() {
			return super.toString() + ", old position=" + oldIncidencePos;
		}
	}

	/**
	 * Records changes of alpha/omega {@link Vertex} of an {@link Edge}.
	 */
	protected class ChangeIncidenceEdit extends GraphEdit {
		private static final long serialVersionUID = 350404556440935178L;
		private int oldVertexId, newVertexId, oldVertexVersion,
				newVertexVersion;
		private int oldIncidencePosition;

		ChangeIncidenceEdit(GraphEditEvent event, Edge e, Vertex oldVertex,
				Vertex newVertex) {
			super(event, e);
			oldVertexId = oldVertex.getId();
			oldVertexVersion = elementVersion(oldVertexId);
			newVertexId = newVertex.getId();
			newVertexVersion = elementVersion(newVertexId);
			TraversalContext tc = graph.setTraversalContext(null);
			if (event == GraphEditEvent.CHANGE_ALPHA) {
				oldIncidencePosition = incidencePosition(e);
			} else {
				oldIncidencePosition = incidencePosition(e.getReversedEdge());
			}
			graph.setTraversalContext(tc);
		}

		@Override
		public void undo() throws CannotUndoException {
			super.undo();
			Vertex v = graph.getVertex(oldVertexId);
			Edge e = graph.getEdge(-elementId);
			TraversalContext tc = graph.setTraversalContext(null);
			if (event == GraphEditEvent.CHANGE_ALPHA) {
				e.setAlpha(v);
				putIncidenceAt(e, oldIncidencePosition);
			} else {
				e.setOmega(v);
				putIncidenceAt(e.getReversedEdge(), oldIncidencePosition);
			}
			graph.setTraversalContext(tc);
		}

		@Override
		public void redo() throws CannotRedoException {
			super.redo();
			Vertex v = graph.getVertex(newVertexId);
			Edge e = graph.getEdge(-elementId);
			if (event == GraphEditEvent.CHANGE_ALPHA) {
				e.setAlpha(v);
			} else {
				e.setOmega(v);
			}
		}

		@Override
		public void changeVertexId(int from, int fromVersion, int to,
				int toVersion) {
			// no super call since this is an edge edit

			if (oldVertexId == from && oldVertexVersion == fromVersion) {
				oldVertexId = to;
				oldVertexVersion = toVersion;
			}
			if (newVertexId == from && newVertexVersion == fromVersion) {
				newVertexId = to;
				newVertexVersion = toVersion;
			}
		}
	}

	/**
	 * Record change of an attribute value.
	 */
	protected class ChangeAttributeEdit extends GraphEdit {
		private static final long serialVersionUID = 9147856388307821832L;
		private String attributeName;
		private Object newValue;
		private Object oldValue;

		ChangeAttributeEdit(AttributedElement<?, ?> el, String attributeName,
				Object oldValue, Object newValue) {
			super(GraphEditEvent.CHANGE_ATTRIBUTE, el);
			this.attributeName = attributeName;
			this.oldValue = oldValue;
			this.newValue = newValue;
		}

		@Override
		public void undo() throws CannotUndoException {
			super.undo();
			if (elementId > 0) {
				graph.getVertex(elementId)
						.setAttribute(attributeName, oldValue);
			} else if (elementId < 0) {
				graph.getEdge(-elementId).setAttribute(attributeName, oldValue);
			} else {
				graph.setAttribute(attributeName, oldValue);
			}
		}

		@Override
		public void redo() throws CannotRedoException {
			super.redo();
			if (elementId > 0) {
				graph.getVertex(elementId)
						.setAttribute(attributeName, newValue);
			} else if (elementId < 0) {
				graph.getEdge(-elementId).setAttribute(attributeName, newValue);
			} else {
				graph.setAttribute(attributeName, newValue);
			}
		}

		@Override
		public String toString() {
			return super.toString() + " set " + attributeName + " from "
					+ oldValue + " to " + newValue;
		}
	}

	/**
	 * Creates a {@link GraphUndoManager} for {@link Graph} <code>g</code>. This
	 * does not automatically add the UndoManager to the
	 * {@link GraphChangeListener}s of <code>g</code>.
	 * 
	 * @param g
	 *            a {@link Graph}
	 */
	public GraphUndoManager(Graph g) {
		this.graph = g;
	}

	@Override
	public synchronized void undo() throws CannotUndoException {
		setWorking(true);
		try {
			super.undo();
		} finally {
			setWorking(false);
		}
	}

	@Override
	public synchronized void redo() throws CannotRedoException {
		setWorking(true);
		try {
			super.redo();
		} finally {
			setWorking(false);
		}
	}

	/**
	 * Creates a new {@link CompoundGraphEdit} with named <code>name</code>.
	 * Subsequent graph changes are combined into that {@link CompoundGraphEdit}
	 * . Edits may be nested, {@link GraphUndoManager} keeps an internal stack
	 * of {@link CompoundGraphEdit}s.
	 * 
	 * @param name
	 *            human readable name for the edit
	 */
	public void beginEdit(String name) {
		CompoundGraphEdit cge = new CompoundGraphEdit(name);
		addEdit(cge);
		compoundEditStack.push(cge);
	}

	/**
	 * Ends the most recent {@link CompoundGraphEdit} and removes it from the
	 * stack.
	 */
	public void endEdit() {
		CompoundGraphEdit cge = compoundEditStack.pop();
		cge.end();
	}

	@Override
	public synchronized void discardAllEdits() {
		super.discardAllEdits();
		versions.clear();
	}

	// GraphChangeListener implementation. Basically, the operations add a
	// specific GraphEdit to this GraphUndoManager. The deletion of Vertex
	// elements is a more complex matter since incident edges and composition
	// children are deleted automatically.

	@Override
	public void beforeCreateVertex(VertexClass elementClass) {
		// do nothing
	}

	@Override
	public void afterCreateVertex(Vertex element) {
		if (!isWorking()) {
			addEdit(new CreateVertexEdit(element));
		}
	}

	@Override
	public void beforeDeleteVertex(Vertex v) {
		if (!isWorking()) {
			assert deleteVertexEdit == null;
			// create a CompoundEdit to store deletion of incident edges and
			// cascading deletes of composition children
			if (deleteVertexCompound == null) {
				deleteVertexCompound = new CompoundEdit() {
					private static final long serialVersionUID = -2775885260931823100L;

					@Override
					public void undo() throws CannotUndoException {
						undoDeleteVertex = true;
						try {
							super.undo();
							restoreIncidencePositions();
						} finally {
							undoDeleteVertex = false;
						}
					}
				};
				addEdit(deleteVertexCompound);
			}
			// this DeleteVertexEdit is added after deletion of the vertex to
			// guarantee that the vertex is created first upon undo
			deleteVertexEdit = new DeleteVertexEdit(v);
		}
	}

	@Override
	public void afterDeleteVertex(VertexClass vc, boolean finalDelete) {
		if (!isWorking()) {
			assert deleteVertexCompound != null;
			assert deleteVertexEdit != null;
			// add the DeleteVertexEdit
			deleteVertexCompound.addEdit(deleteVertexEdit);
			deleteVertexEdit = null;
			if (finalDelete) {
				// and end the CompoundEdit if this was the last delete
				// operation of a cascading deletion
				deleteVertexCompound.end();
				deleteVertexCompound = null;
			}
		}
	}

	@Override
	public void beforeCreateEdge(EdgeClass elementClass, Vertex alpha,
			Vertex omega) {
		// do nothing
	}

	@Override
	public void afterCreateEdge(Edge element) {
		if (!isWorking()) {
			addEdit(new CreateEdgeEdit(element));
		}
	}

	@Override
	public void beforeDeleteEdge(Edge element) {
		if (!isWorking()) {
			addEdit(new DeleteEdgeEdit(element));
		}
	}

	@Override
	public void afterDeleteEdge(EdgeClass elementClass, Vertex oldAlpha,
			Vertex oldOmega) {
		// do nothing
	}

	@Override
	public void beforeChangeAlpha(Edge element, Vertex oldVertex,
			Vertex newVertex) {
		if (!isWorking()) {
			addEdit(new ChangeIncidenceEdit(GraphEditEvent.CHANGE_ALPHA,
					element, oldVertex, newVertex));
		}
	}

	@Override
	public void afterChangeAlpha(Edge element, Vertex oldVertex,
			Vertex newVertex) {
		// do nothing
	}

	@Override
	public void beforeChangeOmega(Edge element, Vertex oldVertex,
			Vertex newVertex) {
		if (!isWorking()) {
			addEdit(new ChangeIncidenceEdit(GraphEditEvent.CHANGE_OMEGA,
					element, oldVertex, newVertex));
		}
	}

	@Override
	public void afterChangeOmega(Edge element, Vertex oldVertex,
			Vertex newVertex) {
		// do nothing
	}

	@Override
	public <AEC extends AttributedElementClass<AEC, ?>> void beforeChangeAttribute(
			AttributedElement<AEC, ?> element, String attributeName,
			Object oldValue, Object newValue) {
	}

	@Override
	public <AEC extends AttributedElementClass<AEC, ?>> void afterChangeAttribute(
			AttributedElement<AEC, ?> element, String attributeName,
			Object oldValue, Object newValue) {
		if (!isWorking()) {
			addEdit(new ChangeAttributeEdit(element, attributeName, oldValue,
					newValue));
		}
	}

	@Override
	public void beforePutIncidenceAfter(Edge inc, Edge other) {
		if (!isWorking()) {
			addEdit(new ChangeIncidenceOrderEdit(
					GraphEditEvent.PUT_INCIDENCE_AFTER, inc, other));
		}
	}

	@Override
	public void afterPutIncidenceAfter(Edge inc, Edge other) {
		// do nothing
	}

	@Override
	public void beforePutIncidenceBefore(Edge inc, Edge other) {
		if (!isWorking()) {
			addEdit(new ChangeIncidenceOrderEdit(
					GraphEditEvent.PUT_INCIDENCE_BEFORE, inc, other));
		}
	}

	@Override
	public void afterPutIncidenceBefore(Edge inc, Edge other) {
	}

	// internal stuff

	/**
	 * Determines the position of {@link Edge} <code>e</code> in the incidence
	 * list of its 'this' {@link Vertex}. To determine the correct position, the
	 * {@link TraversalContext} of the graph must be <code>null</code>.
	 * 
	 * @param e
	 *            an {@link Edge}
	 * @return the position of <code>e</code>, starting at 0.
	 */
	private int incidencePosition(Edge e) {
		assert e != null && e.isValid();
		assert graph.getTraversalContext() == null;
		Vertex v = e.getThis();
		int n = 0;
		for (Edge inc : v.incidences()) {
			if (inc.equals(e)) {
				return n;
			}
			++n;
		}
		throw new RuntimeException("Something is wrong, should never get here!");
	}

	/**
	 * Restores all incidence positions after all edges caused by undo of a
	 * vertex deletion are resurrected. This assumes that for each {@link Edge}
	 * in the <code>correctIncidences</code> list, there are two entries in the
	 * <code>correctPositions</code> list. The first number ist the position at
	 * the start vertex, the second number the position at the end vertex of the
	 * edge.
	 */
	protected void restoreIncidencePositions() {
		TraversalContext tc = graph.setTraversalContext(null);
		int i = 0;
		for (Edge e : correctIncidences) {
			assert e.isValid();
			putIncidenceAt(e, correctPositions.get(i++));
			putIncidenceAt(e.getReversedEdge(), correctPositions.get(i++));
		}
		correctIncidences.clear();
		correctPositions.clear();
		graph.setTraversalContext(tc);
	}

	/**
	 * Put the Edge <code>e</code> at position <code>p</code> in the incidence
	 * list of its 'this' {@link Vertex}. To do this correctly, the
	 * {@link TraversalContext} of the graph must be <code>null</code>.
	 * 
	 * @param e
	 *            an Edge
	 * @param p
	 *            a position, starting at 0
	 */
	protected void putIncidenceAt(Edge e, int p) {
		assert e != null && e.isValid();
		assert graph.getTraversalContext() == null;
		assert p >= 0 && p < e.getThis().getDegree();
		Vertex v = e.getThis();
		Edge i = v.getFirstIncidence();
		int n = 0;
		while (i != null && n != p) {
			if (!i.equals(e)) {
				++n;
			}
			i = i.getNextIncidence();
		}
		if (i != null) {
			if (!i.equals(e)) {
				e.putIncidenceBefore(i);
			}
		} else {
			e.putIncidenceAfter(v.getLastIncidence());
		}
	}

	@Override
	public Graph getGraph() {
		return graph;
	}

	/**
	 * Returns wheter this {@link GraphUndoManager} is performing an undo/redo
	 * operation.
	 * 
	 * @return true when an undo/redo operation is in progress
	 */
	protected boolean isWorking() {
		return working;
	}

	/**
	 * Set working state of this {@link GraphUndoManager}.
	 * 
	 * @param working
	 *            <code>true</code>: {@link GraphUndoManager} is performing an
	 *            undo/redo, <code>false</code>: finished undo/redo
	 */
	protected void setWorking(boolean working) {
		this.working = working;
	}
}
