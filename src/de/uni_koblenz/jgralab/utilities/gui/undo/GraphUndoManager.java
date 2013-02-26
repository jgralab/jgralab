package de.uni_koblenz.jgralab.utilities.gui.undo;

import java.util.HashMap;
import java.util.Map;
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
import de.uni_koblenz.jgralab.Vertex;
import de.uni_koblenz.jgralab.schema.Attribute;
import de.uni_koblenz.jgralab.schema.AttributedElementClass;
import de.uni_koblenz.jgralab.schema.EdgeClass;
import de.uni_koblenz.jgralab.schema.VertexClass;

public class GraphUndoManager extends UndoManager implements
		GraphChangeListener {
	private static final long serialVersionUID = -1959515066823695788L;
	private boolean working;
	private Graph graph;
	private CompoundEdit deleteVertexCompound;
	private GraphEdit deleteVertexEdit;
	protected Map<Integer, AttributedElement<?, ?>> referencedElements;
	protected Map<AttributedElement<?, ?>, Integer> elementRefs;

	private GraphEdit first;
	private GraphEdit last;
	private int version;
	private Stack<CompoundGraphEdit> compoundEditStack = new Stack<>();
	private HashMap<AttributedElement<?, ?>, Integer> versions = new HashMap<>();

	protected enum Event {
		CREATE_VERTEX, CREATE_EDGE, DELETE_VERTEX, DELETE_EDGE, CHANGE_OMEGA, CHANGE_ALPHA, CHANGE_ATTRIBUTE
	}

	protected abstract class GraphEdit extends AbstractUndoableEdit {
		private static final long serialVersionUID = 1100368833451887175L;
		GraphEdit prev;
		GraphEdit next;

		int elementId;
		int elementVersion;

		AttributedElementClass<?, ?> aec;
		Event event;

		GraphEdit(Event event, AttributedElement<?, ?> el) {
			this.event = event;
			aec = el.getAttributedElementClass();
			elementId = (el instanceof Vertex) ? ((Vertex) el).getId()
					: (el instanceof Edge) ? -((Edge) el).getId() : 0;

			elementVersion = ev(el);
			if (first == null) {
				first = last = this;
			} else {
				this.prev = last;
				last.next = this;
				last = this;
			}
		}

		int ev(AttributedElement<?, ?> el) {
			Integer v = versions.get(el);
			if (v == null) {
				versions.put(el, ++version);
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

		public void changeVertexId(int from, int fromVersion, int to,
				int toVersion) {
			if (elementId > 0 && elementId == from
					&& elementVersion == fromVersion) {
				elementId = to;
				elementVersion = toVersion;
			}
		}

		public void changeEdgeId(int from, int fromVersion, int to,
				int toVersion) {
			if (elementId < 0 && elementId == from
					&& elementVersion == fromVersion) {
				elementId = to;
				elementVersion = fromVersion;
			}
		}
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
	}

	protected class CreateVertexEdit extends GraphEdit {
		private static final long serialVersionUID = 7455868109487075050L;

		CreateVertexEdit(Vertex v) {
			super(Event.CREATE_VERTEX, v);
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
			versions.put(v, elementVersion);
			// System.out.println("\tWAS v" + oldId + "-" + oldVersion +
			// ", is v"
			// + elementId + "-" + elementVersion);
			for (GraphEdit g = first; g != null; g = g.next) {
				g.changeVertexId(oldId, oldVersion, elementId, elementVersion);
			}
		}
	}

	protected class CreateEdgeEdit extends GraphEdit {
		private static final long serialVersionUID = 8840782477572584494L;
		private int alphaId, omegaId, alphaVersion, omegaVersion;

		CreateEdgeEdit(Edge e) {
			super(Event.CREATE_EDGE, e);
			alphaId = e.getAlpha().getId();
			alphaVersion = ev(e.getAlpha());
			omegaId = e.getOmega().getId();
			omegaVersion = ev(e.getOmega());
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
			versions.put(e, elementVersion);
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

	protected abstract class DeleteElementEdit extends GraphEdit {
		private static final long serialVersionUID = -6211867897158716199L;
		private Object[] attrValues;

		public DeleteElementEdit(Event event, GraphElement<?, ?> el) {
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

	protected class DeleteVertexEdit extends DeleteElementEdit {
		private static final long serialVersionUID = -4787860787912669387L;

		public DeleteVertexEdit(Vertex v) {
			super(Event.DELETE_VERTEX, v);
		}

		@Override
		public void undo() throws CannotUndoException {
			super.undo();
			Vertex v = graph.createVertex((VertexClass) aec);
			int oldId = elementId;
			int oldVersion = elementVersion;
			elementId = v.getId();
			elementVersion = ++version;
			versions.put(v, elementVersion);
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

	protected class DeleteEdgeEdit extends DeleteElementEdit {
		private static final long serialVersionUID = 6430961207052596036L;
		private int alphaId, omegaId, alphaVersion, omegaVersion;

		public DeleteEdgeEdit(Edge e) {
			super(Event.DELETE_EDGE, e);
			alphaId = e.getAlpha().getId();
			alphaVersion = ev(e.getAlpha());
			omegaId = e.getOmega().getId();
			omegaVersion = ev(e.getOmega());
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
			versions.put(e, elementVersion);
			// System.out.println("\tWAS e" + (-oldId) + "-" + oldVersion
			// + ", is e" + (-elementId) + "-" + elementVersion);
			for (GraphEdit g = first; g != null; g = g.next) {
				g.changeEdgeId(oldId, oldVersion, elementId, elementVersion);
			}
			restoreAttributes(e);
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

		ChangeIncidenceEdit(Event event, Edge e, Vertex oldVertex,
				Vertex newVertex) {
			super(event, e);
			oldVertexId = oldVertex.getId();
			oldVertexVersion = ev(oldVertex);
			newVertexId = newVertex.getId();
			newVertexVersion = ev(newVertex);
		}

		@Override
		public void undo() throws CannotUndoException {
			super.undo();
			Vertex v = graph.getVertex(oldVertexId);
			Edge e = graph.getEdge(elementId);
			if (event == Event.CHANGE_ALPHA) {
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
			if (event == Event.CHANGE_ALPHA) {
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
			super(Event.CHANGE_ATTRIBUTE, el);
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
	public void beforeDeleteVertex(Vertex element) {
		if (!isWorking()) {
			assert deleteVertexCompound == null;
			deleteVertexCompound = new CompoundEdit();
			addEdit(deleteVertexCompound);
			deleteVertexEdit = new DeleteVertexEdit(element);
		}
	}

	@Override
	public void afterDeleteVertex(VertexClass elementClass) {
		if (!isWorking()) {
			assert deleteVertexCompound != null && deleteVertexEdit != null;
			deleteVertexCompound.addEdit(deleteVertexEdit);
			deleteVertexCompound.end();
			deleteVertexEdit = null;
			deleteVertexCompound = null;
		}
	}

	@Override
	public void beforeCreateEdge(EdgeClass elementClass,
			Vertex alpha, Vertex omega) {
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
	public void afterDeleteEdge(EdgeClass elementClass,
			Vertex oldAlpha, Vertex oldOmega) {
	}

	@Override
	public void beforeChangeAlpha(Edge element,
			Vertex oldVertex, Vertex newVertex) {
	}

	@Override
	public void afterChangeAlpha(Edge element,
			Vertex oldVertex, Vertex newVertex) {
		if (!isWorking()) {
			addEdit(new ChangeIncidenceEdit(Event.CHANGE_ALPHA, element,
					oldVertex, newVertex));
		}
	}

	@Override
	public void beforeChangeOmega(Edge element,
			Vertex oldVertex, Vertex newVertex) {
	}

	@Override
	public void afterChangeOmega(Edge element,
			Vertex oldVertex, Vertex newVertex) {
		if (!isWorking()) {
			addEdit(new ChangeIncidenceEdit(Event.CHANGE_OMEGA, element,
					oldVertex, newVertex));
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

	@Override
	public Graph getGraph() {
		return graph;
	}

	@Override
	public int getMaxNestedTriggerCalls() {
		return 0;
	}

	@Override
	public void setMaxNestedTriggerCalls(int maxNestedTriggerCalls) {
	}

	@Override
	public int getNestedTriggerCalls() {
		return 0;
	}

	protected boolean isWorking() {
		return working;
	}

	protected void setWorking(boolean working) {
		this.working = working;
	}

}
