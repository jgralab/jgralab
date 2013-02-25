package de.uni_koblenz.jgralab.utilities.gui.undo;

import java.util.Map;

import javax.swing.undo.AbstractUndoableEdit;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;
import javax.swing.undo.CompoundEdit;
import javax.swing.undo.UndoManager;
import javax.swing.undo.UndoableEdit;

import de.uni_koblenz.jgralab.AttributedElement;
import de.uni_koblenz.jgralab.Edge;
import de.uni_koblenz.jgralab.Graph;
import de.uni_koblenz.jgralab.GraphElement;
import de.uni_koblenz.jgralab.Vertex;
import de.uni_koblenz.jgralab.eca.ECARuleManagerInterface;
import de.uni_koblenz.jgralab.schema.Attribute;
import de.uni_koblenz.jgralab.schema.AttributedElementClass;
import de.uni_koblenz.jgralab.schema.EdgeClass;
import de.uni_koblenz.jgralab.schema.VertexClass;

public class GraphUndoManager extends UndoManager implements
		ECARuleManagerInterface {
	private static final long serialVersionUID = -1959515066823695788L;
	private boolean working;
	private Graph graph;
	private CompoundEdit deleteVertexCompound;
	private GraphEdit deleteVertexEdit;
	protected Map<Integer, AttributedElement<?, ?>> referencedElements;
	protected Map<AttributedElement<?, ?>, Integer> elementRefs;

	private GraphEdit first;
	private GraphEdit last;

	protected enum Event {
		CREATE_VERTEX, CREATE_EDGE, DELETE_VERTEX, DELETE_EDGE, CHANGE_OMEGA, CHANGE_ALPHA, CHANGE_ATTRIBUTE
	}

	protected abstract class GraphEdit extends AbstractUndoableEdit {
		private static final long serialVersionUID = 1100368833451887175L;
		GraphEdit prev;
		GraphEdit next;

		int elementId;
		AttributedElementClass<?, ?> aec;
		Event event;

		GraphEdit(Event event, AttributedElement<?, ?> el) {
			if (first == null) {
				first = last = this;
			} else {
				prev = last;
				last.next = this;
				last = this;
			}
			this.event = event;
			aec = el.getAttributedElementClass();
			elementId = (el instanceof Vertex) ? ((Vertex) el).getId()
					: (el instanceof Edge) ? -((Edge) el).getId() : 0;
		}

		@Override
		public String toString() {
			String s = ""; // super.toString() + ", ";
			s += event + " " + aec.getSimpleName() + " ";
			s += isVertexEdit() ? "v" + elementId : isEdgeEdit() ? "e"
					+ (-elementId) : "graph";
			return s;
		}

		@Override
		public void die() {
			System.out.println("DIE " + this);
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

		public void changeVertexId(int from, int to) {
			if (elementId > 0 && elementId == from) {
				elementId = to;
			}
		}

		public void changeEdgeId(int from, int to) {
			if (elementId < 0 && elementId == from) {
				elementId = to;
			}
		}

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

		public boolean isVertexEdit() {
			return elementId > 0;
		}

		public boolean isEdgeEdit() {
			return elementId < 0;
		}

		public boolean isGraphEdit() {
			return elementId == 0;
		}
	}

	protected abstract class GraphElementEdit extends GraphEdit {
		GraphElementEdit(Event event, GraphElement<?, ?> el) {
			super(event, el);
		}
	}

	protected class CreateVertexEdit extends GraphElementEdit {
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
			if (v.getId() != elementId) {
				System.out.println("\tWAS v" + elementId + " is v" + v.getId());
				for (GraphEdit g = next; g != null; g = g.next) {
					g.changeVertexId(elementId, v.getId());
				}
				elementId = v.getId();
			}
		}
	}

	protected class CreateEdgeEdit extends GraphElementEdit {
		CreateEdgeEdit(Edge e) {
			super(Event.CREATE_EDGE, e);
		}
	}

	protected abstract class DeleteElementEdit extends GraphElementEdit {
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
		public DeleteVertexEdit(Vertex v) {
			super(Event.DELETE_VERTEX, v);
		}

		@Override
		public void undo() throws CannotUndoException {
			super.undo();
			Vertex v = graph.createVertex((VertexClass) aec);
			restoreAttributes(v);
			if (v.getId() != elementId) {
				System.out.println("\tWAS v" + elementId + " is v" + v.getId());
				for (GraphEdit g = next; g != null; g = g.next) {
					g.changeVertexId(elementId, v.getId());
				}
				elementId = v.getId();
			}
		}

		@Override
		public void redo() throws CannotRedoException {
			super.redo();
			graph.deleteVertex(graph.getVertex(elementId));
		}
	}

	protected class DeleteEdgeEdit extends DeleteElementEdit {
		public DeleteEdgeEdit(Edge e) {
			super(Event.DELETE_EDGE, e);
		}
	}

	protected class AttributeChangeEdit extends GraphEdit {
		String attributeName;
		Object newValue;
		Object oldValue;

		AttributeChangeEdit(AttributedElement<?, ?> el, String attributeName,
				Object oldValue, Object newValue) {
			super(Event.CHANGE_ATTRIBUTE, el);
			this.attributeName = attributeName;
			this.oldValue = oldValue;
			this.newValue = newValue;
		}

		@Override
		public void undo() throws CannotUndoException {
			super.undo();
			if (isVertexEdit()) {
				graph.getVertex(elementId)
						.setAttribute(attributeName, oldValue);
			} else if (isEdgeEdit()) {
				// graph.getEdge(-elementId).setAttribute(attributeName,
				// oldValue);
			} else {
				graph.setAttribute(attributeName, oldValue);
			}
		}

		@Override
		public void redo() throws CannotRedoException {
			super.redo();
			if (isVertexEdit()) {
				graph.getVertex(elementId)
						.setAttribute(attributeName, newValue);
			} else if (isEdgeEdit()) {
				// graph.getEdge(-elementId).setAttribute(attributeName,
				// newValue);
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
		setLimit(1000);
	}

	@Override
	public synchronized boolean addEdit(UndoableEdit anEdit) {
		boolean result = super.addEdit(anEdit);
		if (anEdit instanceof GraphEdit) {
			System.out.println("EDIT " + anEdit);
		}
		return result;
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

	@Override
	public void fireBeforeCreateVertexEvents(VertexClass elementClass) {
	}

	@Override
	public void fireAfterCreateVertexEvents(Vertex element) {
		if (!isWorking()) {
			addEdit(new CreateVertexEdit(element));
		}
	}

	@Override
	public void fireBeforeDeleteVertexEvents(Vertex element) {
		if (!isWorking()) {
			assert deleteVertexCompound == null;
			deleteVertexCompound = new CompoundEdit();
			addEdit(deleteVertexCompound);
			deleteVertexEdit = new DeleteVertexEdit(element);
		}
	}

	@Override
	public void fireAfterDeleteVertexEvents(VertexClass elementClass) {
		if (!isWorking()) {
			assert deleteVertexCompound != null && deleteVertexEdit != null;
			addEdit(deleteVertexEdit);
			deleteVertexCompound.end();
			deleteVertexEdit = null;
			deleteVertexCompound = null;
		}
	}

	@Override
	public void fireBeforeCreateEdgeEvents(EdgeClass elementClass) {
		// TODO Auto-generated method stub

	}

	@Override
	public void fireAfterCreateEdgeEvents(Edge element) {
		if (!isWorking()) {
			addEdit(new CreateEdgeEdit(element));
		}
	}

	@Override
	public void fireBeforeDeleteEdgeEvents(Edge element) {
		if (!isWorking()) {
			addEdit(new DeleteEdgeEdit(element));
		}
	}

	@Override
	public void fireAfterDeleteEdgeEvents(EdgeClass elementClass,
			Vertex oldAlpha, Vertex oldOmega) {
	}

	@Override
	public void fireBeforeChangeAlphaOfEdgeEvents(Edge element,
			Vertex oldVertex, Vertex newVertex) {
	}

	@Override
	public void fireAfterChangeAlphaOfEdgeEvents(Edge element,
			Vertex oldVertex, Vertex newVertex) {
		if (!isWorking()) {
			// TODO
			// addEdit(new GraphEdit(Event.CHANGE_ALPHA, element, null,
			// oldVertex,
			// newVertex));
		}
	}

	@Override
	public void fireBeforeChangeOmegaOfEdgeEvents(Edge element,
			Vertex oldVertex, Vertex newVertex) {
	}

	@Override
	public void fireAfterChangeOmegaOfEdgeEvents(Edge element,
			Vertex oldVertex, Vertex newVertex) {
		if (!isWorking()) {
			// TODO
			// addEdit(new GraphEdit(Event.CHANGE_OMEGA, element, null,
			// oldVertex,
			// newVertex));
		}
	}

	@Override
	public <AEC extends AttributedElementClass<AEC, ?>> void fireBeforeChangeAttributeEvents(
			AttributedElement<AEC, ?> element, String attributeName,
			Object oldValue, Object newValue) {
	}

	@Override
	public <AEC extends AttributedElementClass<AEC, ?>> void fireAfterChangeAttributeEvents(
			AttributedElement<AEC, ?> element, String attributeName,
			Object oldValue, Object newValue) {
		if (!isWorking()) {
			addEdit(new AttributeChangeEdit(element, attributeName, oldValue,
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
