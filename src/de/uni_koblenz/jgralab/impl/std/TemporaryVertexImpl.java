package de.uni_koblenz.jgralab.impl.std;

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;

import de.uni_koblenz.jgralab.Edge;
import de.uni_koblenz.jgralab.EdgeDirection;
import de.uni_koblenz.jgralab.Graph;
import de.uni_koblenz.jgralab.GraphIO;
import de.uni_koblenz.jgralab.TemporaryEdge;
import de.uni_koblenz.jgralab.TemporaryVertex;
import de.uni_koblenz.jgralab.Vertex;
import de.uni_koblenz.jgralab.exception.GraphIOException;
import de.uni_koblenz.jgralab.exception.NoSuchAttributeException;
import de.uni_koblenz.jgralab.exception.TemporaryGraphElementException;
import de.uni_koblenz.jgralab.impl.InternalGraph;
import de.uni_koblenz.jgralab.impl.InternalVertex;
import de.uni_koblenz.jgralab.schema.VertexClass;

public class TemporaryVertexImpl extends VertexImpl implements TemporaryVertex {

	private HashMap<String, Object> attributes;

	private VertexClass preliminaryType;

	protected TemporaryVertexImpl(int id, Graph graph) {
		super(id, graph);
		this.attributes = new HashMap<String, Object>();
	}

	protected TemporaryVertexImpl(int id, Graph graph,
			VertexClass preliminaryType) {
		super(id, graph);
		this.attributes = new HashMap<String, Object>();
		this.preliminaryType = preliminaryType;
	}

	@Override
	public VertexClass getAttributedElementClass() {
		return this.graph.getGraphClass().getTemporaryVertexClass();
	}

	@Override
	public Class<? extends Vertex> getSchemaClass() {
		throw new UnsupportedOperationException();
	}

	@Override
	public void readAttributeValueFromString(String attributeName, String value)
			throws GraphIOException, NoSuchAttributeException {
		throw new UnsupportedOperationException();
	}

	@Override
	public String writeAttributeValueToString(String attributeName)
			throws IOException, GraphIOException, NoSuchAttributeException {
		throw new UnsupportedOperationException();
	}

	@Override
	public void writeAttributeValues(GraphIO io) throws IOException,
			GraphIOException {
		throw new UnsupportedOperationException();
	}

	@Override
	public void readAttributeValues(GraphIO io) throws GraphIOException {
		throw new UnsupportedOperationException();
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> T getAttribute(String name) throws NoSuchAttributeException {
		return (T) this.attributes.get(name);
	}

	@Override
	public <T> void setAttribute(String name, T data)
			throws NoSuchAttributeException {
		this.attributes.put(name, data);
	}

	@Override
	public boolean isUnsetAttribute(String name)
			throws NoSuchAttributeException {
		throw new UnsupportedOperationException(
				"The isUnsetAttribute() method is not available for temporary elements.");
	}

	@Override
	public void internalInitializeSetAttributesBitSet() {
		throw new UnsupportedOperationException(
				"The internalInitializeSetAttributesBitSet() method is not available for temporary elements.");
	}

	@Override
	public void initializeAttributesWithDefaultValues() {
		// do nothing
	}

	@Override
	public boolean isInstanceOf(VertexClass cls) {
		return cls.equals(this.graph.getGraphClass().getTemporaryVertexClass());
	}

	@Override
	public Vertex bless() {
		if (this.preliminaryType == null) {
			throw new TemporaryGraphElementException(
					"Transformation of temporary vertex " + this + " failed. "
							+ "There is no preliminary VertexClass set.");
		}
		return this.bless(this.preliminaryType);
	}

	@Override
	public Vertex bless(VertexClass vc) {

		// Test if vc is valid
		validateConversion(vc);

		// save properties
		int id = this.id;
		InternalGraph g = this.graph;
		InternalVertex prevVertex = this.getPrevVertexInVSeq();
		InternalVertex nextVertex = this.getNextVertexInVSeq();

		// create new vertex
		InternalVertex newVertex = g.createVertex(vc);

		// attributes
		for (String attrName : this.attributes.keySet()) {
			if (newVertex.getAttributedElementClass().containsAttribute(
					attrName)) {
				newVertex.setAttribute(attrName, this.attributes.get(attrName));
			}
		}

		// incidences
		Edge e = this.getFirstIncidence();
		while (e != null) {
			e.setThis(newVertex);
			e = this.getFirstIncidence();
		}

		this.delete();

		if (nextVertex != null) {
			newVertex.putBefore(nextVertex);
		}
		if (prevVertex != null) {
			newVertex.putAfter(prevVertex);
		}

		// Set id
		int idToFree = newVertex.getId();
		newVertex.setId(id);
		g.allocateVertexIndex(id);
		g.freeVertexIndex(idToFree);
		// fixup vertex[]
		InternalVertex[] vertex = g.getVertex();
		vertex[id] = newVertex;
		vertex[idToFree] = null;

		// Transform TemporaryEdges with type
		HashSet<TemporaryEdge> tempEdgeList = new HashSet<TemporaryEdge>();
		for (Edge te : newVertex.incidences(this.getGraphClass()
				.getTemporaryEdgeClass())) {
			if ((((TemporaryEdge) te).getPreliminaryType() != null)
					&& te.isValid() && !te.getThat().isTemporary()) {
				tempEdgeList.add((TemporaryEdge) te.getNormalEdge());
			}
		}
		for (TemporaryEdge tempEdge : tempEdgeList) {
			tempEdge.bless();
		}
		return newVertex;
	}

	private void validateConversion(VertexClass vc) {
		if (!isValid()) {
			throw new TemporaryGraphElementException(
					"This temporary vertex isn't valid anymore! " + this);
		}
		for (Edge e : this.incidences(EdgeDirection.OUT)) {
			if (e.isNormal()) {
				if (!vc.isValidFromFor(e.getAttributedElementClass())) {
					throw new TemporaryGraphElementException(
							"Transformation of temporary vertex " + this
									+ " failed. " + vc
									+ " is not a valid source for edge " + e
									+ ".");
				}
			} else {
				if (!vc.isValidToFor(e.getAttributedElementClass())) {
					throw new TemporaryGraphElementException(
							"Transformation of temporary vertex " + this
									+ " failed. " + vc
									+ " is not a valid target for edge " + e
									+ ".");
				}
			}
		}

		for (String atname : this.attributes.keySet()) {
			if (vc.containsAttribute(atname)) {
				boolean valid = true;
				try {
					valid = vc.getAttribute(atname).getDomain()
							.isConformValue(this.attributes.get(atname));
				} catch (NoSuchAttributeException ex) {
					valid = false;
				}
				if (!valid) {
					throw new TemporaryGraphElementException(
							"Transformation of temporary vertex " + this
									+ " failed. " + vc + " has an attribute "
									+ atname + " but "
									+ this.attributes.get(atname)
									+ " is not a valid value.");
				}
			}
		}
	}

	@Override
	public void deleteAttribute(String name) {
		this.attributes.remove(name);
	}

	@Override
	public boolean isTemporary() {
		return true;
	}

	@Override
	public VertexClass getPreliminaryType() {
		return this.preliminaryType;
	}

	@Override
	public void setPreliminaryType(VertexClass ec) {
		this.preliminaryType = ec;
	}

	@Override
	public final String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("tv");
		sb.append(id);
		sb.append(": ");
		if (preliminaryType != null) {
			sb.append(preliminaryType.getQualifiedName());
		} else {
			sb.append("-MissingPreliminaryType-");
		}
		sb.append(" {");
		boolean first = true;
		for (Entry<String, Object> e : attributes.entrySet()) {
			if (first) {
				first = false;
			} else {
				sb.append(", ");
			}
			sb.append(e.getKey());
			sb.append(" -> ");
			sb.append(e.getValue());
		}
		sb.append("}");
		return sb.toString();
	}

	@Override
	public Map<String, Object> getAttributes() {
		return attributes;
	}

}
