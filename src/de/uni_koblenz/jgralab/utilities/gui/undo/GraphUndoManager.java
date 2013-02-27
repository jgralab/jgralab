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
 * @author riediger
 * 
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
	 * counter is increased.
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
	 * {@link Edge}s.
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
		CREATE_VERTEX, CREATE_EDGE, DELETE_VERTEX, DELETE_EDGE, CHANGE_OMEGA, CHANGE_ALPHA, CHANGE_ATTRIBUTE
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
					: (el instanceof Edge) ? -((Edge) el).getId() : 0;

			elementVersion = elementVersion(elementId);
			if (first == null) {
				first = last = this;
			} else {
				this.prev = last;
				last.next = this;
				last = this;
			}
		}

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

		// remove comments below to print undo/redo operations to System.out

		@Override
		public void undo() throws CannotUndoException {
			System.out.println("UNDO " + this);
			super.undo();
		}

		@Override
		public void redo() throws CannotRedoException {
			System.out.println("REDO " + this);
			super.redo();
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
			Vertex v = graph.createVertex((VertexClass) aec);
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
		}

		@Override
		public void changeVertexId(int from, int fromVersion, int to,
				int toVersion) {
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
	 * Records deletion of a {@link Vertex}. This edit does not represent
	 * automatic deletions of incident edges, those are recorded in a special
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
			Vertex v = graph.createVertex((VertexClass) aec);
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
	 * 
	 * Upon undo, the resurrected edge is put into the correct places in the
	 * incidence sequences. If this {@link DeleteEdgeEdit} is part of a
	 * {@link Vertex} deletion, incidence numbers are corrected only after all
	 * incident edges are resurrected.
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
			alphaInc = incidenceNumber(e.getAlpha(), e);
			omegaInc = incidenceNumber(e.getOmega(), e.getReversedEdge());
			graph.setTraversalContext(tc);
		}

		@Override
		public void undo() throws CannotUndoException {
			super.undo();
			Vertex alpha = graph.getVertex(alphaId);
			Vertex omega = graph.getVertex(omegaId);
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
			restoreAttributes(e);
			if (!undoDeleteVertex) {
				TraversalContext tc = graph.setTraversalContext(null);
				putIncidenceAt(e.getAlpha(), e, alphaInc);
				putIncidenceAt(e.getOmega(), e.getReversedEdge(), omegaInc);
				graph.setTraversalContext(tc);
			} else {
				correctIncidences.add(e);
				correctPositions.add(alphaInc);
				correctPositions.add(omegaInc);
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

	protected class ChangeIncidenceEdit extends GraphEdit {
		private static final long serialVersionUID = 350404556440935178L;
		private int oldVertexId, newVertexId, oldVertexVersion,
				newVertexVersion;

		// TODO record/restore incidence position at old vertex

		ChangeIncidenceEdit(GraphEditEvent event, Edge e, Vertex oldVertex,
				Vertex newVertex) {
			super(event, e);
			oldVertexId = oldVertex.getId();
			oldVertexVersion = elementVersion(oldVertexId);
			newVertexId = newVertex.getId();
			newVertexVersion = elementVersion(newVertexId);
		}

		@Override
		public void undo() throws CannotUndoException {
			super.undo();
			Vertex v = graph.getVertex(oldVertexId);
			Edge e = graph.getEdge(elementId);
			if (event == GraphEditEvent.CHANGE_ALPHA) {
				e.setAlpha(v);
			} else {
				e.setOmega(v);
			}
		}

		@Override
		public void redo() throws CannotRedoException {
			super.redo();
			Vertex v = graph.getVertex(newVertexId);
			Edge e = graph.getEdge(elementId);
			if (event == GraphEditEvent.CHANGE_ALPHA) {
				e.setAlpha(v);
			} else {
				e.setOmega(v);
			}
		}

		@Override
		public void changeVertexId(int from, int fromVersion, int to,
				int toVersion) {
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

	public void beginEdit(String name) {
		CompoundGraphEdit cge = new CompoundGraphEdit(name);
		addEdit(cge);
		compoundEditStack.push(cge);
	}

	public void endEdit() {
		CompoundGraphEdit cge = compoundEditStack.pop();
		cge.end();
	}

	@Override
	public synchronized void discardAllEdits() {
		super.discardAllEdits();
		versions.clear();
	}

	@Override
	public void beforeCreateVertex(VertexClass elementClass) {
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
			deleteVertexEdit = new DeleteVertexEdit(v);
		}
	}

	@Override
	public void afterDeleteVertex(VertexClass vc, boolean finalDelete) {
		if (!isWorking()) {
			assert deleteVertexCompound != null;
			assert deleteVertexEdit != null;
			deleteVertexCompound.addEdit(deleteVertexEdit);
			if (finalDelete) {
				deleteVertexCompound.end();
				deleteVertexCompound = null;
			}
			deleteVertexEdit = null;
		}
	}

	@Override
	public void beforeCreateEdge(EdgeClass elementClass, Vertex alpha,
			Vertex omega) {
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
	}

	@Override
	public void beforeChangeAlpha(Edge element, Vertex oldVertex,
			Vertex newVertex) {
	}

	@Override
	public void afterChangeAlpha(Edge element, Vertex oldVertex,
			Vertex newVertex) {
		if (!isWorking()) {
			addEdit(new ChangeIncidenceEdit(GraphEditEvent.CHANGE_ALPHA,
					element, oldVertex, newVertex));
		}
	}

	@Override
	public void beforeChangeOmega(Edge element, Vertex oldVertex,
			Vertex newVertex) {
	}

	@Override
	public void afterChangeOmega(Edge element, Vertex oldVertex,
			Vertex newVertex) {
		if (!isWorking()) {
			addEdit(new ChangeIncidenceEdit(GraphEditEvent.CHANGE_OMEGA,
					element, oldVertex, newVertex));
		}
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

	private int incidenceNumber(Vertex v, Edge e) {
		assert v != null && v.isValid();
		assert e != null && e.isValid();
		int n = 0;
		for (Edge i : v.incidences()) {
			if (i.equals(e)) {
				return n;
			}
			++n;
		}
		return -1;
	}

	private void restoreIncidencePositions() {
		TraversalContext tc = graph.setTraversalContext(null);
		int i = 0;
		for (Edge e : correctIncidences) {
			putIncidenceAt(e.getAlpha(), e, correctPositions.get(i++));
			putIncidenceAt(e.getOmega(), e.getReversedEdge(),
					correctPositions.get(i++));
		}
		correctIncidences.clear();
		correctPositions.clear();
		graph.setTraversalContext(tc);
	}

	private void putIncidenceAt(Vertex v, Edge e, int p) {
		assert v != null && v.isValid();
		assert e != null && e.isValid();
		assert e.getThis().equals(v);
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

	protected boolean isWorking() {
		return working;
	}

	protected void setWorking(boolean working) {
		this.working = working;
	}

}
