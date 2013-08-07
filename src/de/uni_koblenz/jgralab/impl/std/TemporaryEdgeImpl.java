package de.uni_koblenz.jgralab.impl.std;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import de.uni_koblenz.jgralab.Edge;
import de.uni_koblenz.jgralab.Graph;
import de.uni_koblenz.jgralab.GraphIO;
import de.uni_koblenz.jgralab.TemporaryEdge;
import de.uni_koblenz.jgralab.Vertex;
import de.uni_koblenz.jgralab.exception.GraphIOException;
import de.uni_koblenz.jgralab.exception.NoSuchAttributeException;
import de.uni_koblenz.jgralab.exception.TemporaryGraphElementException;
import de.uni_koblenz.jgralab.impl.GraphBaseImpl;
import de.uni_koblenz.jgralab.impl.InternalEdge;
import de.uni_koblenz.jgralab.impl.InternalGraph;
import de.uni_koblenz.jgralab.impl.InternalVertex;
import de.uni_koblenz.jgralab.impl.ReversedEdgeBaseImpl;
import de.uni_koblenz.jgralab.schema.AggregationKind;
import de.uni_koblenz.jgralab.schema.EdgeClass;

public class TemporaryEdgeImpl extends EdgeImpl implements TemporaryEdge {

	private HashMap<String, Object> attributes;
	private EdgeClass preliminaryType;

	protected TemporaryEdgeImpl(int anId, Graph graph, Vertex alpha,
			Vertex omega) {
		super(anId, graph, alpha, omega);
		this.attributes = new HashMap<String, Object>();
		((GraphBaseImpl) graph).addEdge(this, alpha, omega);
	}

	protected TemporaryEdgeImpl(int anId, Graph graph,
			EdgeClass preliminaryType, Vertex alpha, Vertex omega) {
		super(anId, graph, alpha, omega);
		this.preliminaryType = preliminaryType;
		this.attributes = new HashMap<String, Object>();
		((GraphBaseImpl) graph).addEdge(this, alpha, omega);
	}

	@Override
	public EdgeClass getAttributedElementClass() {
		return graph.getGraphClass().getTemporaryEdgeClass();
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
	public void internalInitializeAttributesWithDefaultValues() {
		// do nothing - no attributes
	}

	@Override
	public boolean isInstanceOf(EdgeClass cls) {
		return cls.equals(graph.getGraphClass().getTemporaryEdgeClass());
	}

	@Override
	public Edge bless() {
		if (this.preliminaryType == null) {
			throw new TemporaryGraphElementException(
					"Transformation of temporary edge " + this + " failed. "
							+ "There is no preliminary EdgeClass set.");
		}
		return bless(this.preliminaryType);
	}

	@Override
	public Edge bless(EdgeClass edgeClass) {

		// test if valid
		validateConversion(edgeClass);

		// save properties
		InternalGraph g = graph;
		int tempID = id;
		InternalEdge prevEdge = this.getPrevEdgeInESeq();
		InternalEdge nextEdge = this.getNextEdgeInESeq();

		InternalEdge prevIncidence = this.getPrevIncidenceInISeq();
		InternalEdge nextIncidence = this.getNextIncidenceInISeq();

		InternalEdge prevIncidenceReversed = ((InternalEdge) this
				.getReversedEdge()).getPrevIncidenceInISeq();
		InternalEdge nextIncidenceReversed = ((InternalEdge) this
				.getReversedEdge()).getNextIncidenceInISeq();

		// create new edge
		InternalEdge newEdge = g.createEdge(edgeClass, getAlpha(), getOmega());

		// attributes
		for (String attrName : this.attributes.keySet()) {
			if (newEdge.getAttributedElementClass().containsAttribute(attrName)) {
				newEdge.setAttribute(attrName, this.attributes.get(attrName));
			}
		}

		InternalEdge newLastIncidence = newEdge.getPrevIncidenceInISeq();
		InternalEdge newLastIncidenceReversed = ((InternalEdge) newEdge
				.getReversedEdge()).getPrevIncidenceInISeq();

		this.delete();

		// eSeq
		if (nextEdge != null) {
			newEdge.putBeforeEdge(nextEdge);
		}
		if (prevEdge != null) {
			newEdge.putAfterEdge(prevEdge);
		}

		// iSeq edge
		if (newLastIncidence == this) {
			newLastIncidence = newEdge;
		}
		correctISeq(prevIncidence, nextIncidence, newEdge, newLastIncidence);

		// iSeq reversed
		if (prevIncidenceReversed == this) {
			prevIncidenceReversed = newEdge;
		}
		if (nextIncidenceReversed == this) {
			nextIncidenceReversed = newEdge;
		}
		if (newLastIncidenceReversed == this.getReversedEdge()) {
			newLastIncidenceReversed = (InternalEdge) newEdge.getReversedEdge();
		}
		correctISeq(prevIncidenceReversed, nextIncidenceReversed,
				(InternalEdge) newEdge.getReversedEdge(),
				newLastIncidenceReversed);

		// set id
		int idToFree = newEdge.getId();
		newEdge.setId(tempID);
		g.allocateEdgeIndex(tempID);
		g.freeEdgeIndex(idToFree);
		// fix edge[] & revEdge
		InternalEdge[] edge = g.getEdge();
		edge[tempID] = newEdge;
		edge[idToFree] = null;
		InternalEdge[] revEdge = g.getRevEdge();
		revEdge[tempID] = (InternalEdge) newEdge.getReversedEdge();
		revEdge[idToFree] = null;

		return newEdge;

	}

	private void validateConversion(EdgeClass edgeClass) {
		if (!isValid()) {
			throw new TemporaryGraphElementException(
					"This temporary edge isn't valid anymore! " + this);
		}
		if (!this.getAlpha().getAttributedElementClass()
				.isValidFromFor(edgeClass)) {
			throw new TemporaryGraphElementException(
					"Transformation of temporary edge " + this + " failed. "
							+ this.getAlpha() + " is not a valid source for "
							+ edgeClass);
		}
		if (!this.getOmega().getAttributedElementClass()
				.isValidToFor(edgeClass)) {
			throw new TemporaryGraphElementException(
					"Transformation of temporary edge " + this + " failed. "
							+ this.getOmega() + " is not a valid target for "
							+ edgeClass);
		}

		for (String atname : this.attributes.keySet()) {
			if (edgeClass.containsAttribute(atname)) {
				if (!edgeClass.getAttribute(atname).getDomain()
						.isConformValue(this.attributes.get(atname))) {
					throw new TemporaryGraphElementException(
							"Transformation of temporary vertex " + this
									+ " failed. " + edgeClass
									+ " has an attribute " + atname + " but "
									+ this.attributes.get(atname)
									+ " is not a valid value.");
				}
			}
		}
	}

	private void correctISeq(InternalEdge prevIncidence,
			InternalEdge nextIncidence, InternalEdge newEdge,
			InternalEdge newLastIncidence) {
		if (nextIncidence != null) {
			nextIncidence.setPrevIncidenceInternal(newEdge);
			newEdge.setNextIncidenceInternal(nextIncidence);
			newEdge.setPrevIncidenceInternal(prevIncidence);
		}
		newLastIncidence.setNextIncidenceInternal(null);

		if (prevIncidence != null) {
			prevIncidence.setNextIncidenceInternal(newEdge);
		} else {// Temporary Edge is first incidence
			((InternalVertex) newEdge.getThis()).setFirstIncidence(newEdge);
		}

		((InternalVertex) newEdge.getThis()).setLastIncidence(newLastIncidence);

	}

	@Override
	public void deleteAttribute(String name) {
		((TemporaryEdge) this.getNormalEdge()).deleteAttribute(name);

	}

	@Override
	protected ReversedEdgeBaseImpl createReversedEdge() {
		return new TemporaryReversedEdgeImpl(this, graph);
	}

	@Override
	public AggregationKind getAlphaAggregationKind() {
		return getAttributedElementClass().getFrom().getAggregationKind();
	}

	@Override
	public AggregationKind getOmegaAggregationKind() {
		return getAttributedElementClass().getTo().getAggregationKind();
	}

	// ........................................................

	@Override
	public Class<? extends Edge> getSchemaClass() {
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

	@Override
	public AggregationKind getAggregationKind() {
		AggregationKind fromAK = (getAttributedElementClass()).getFrom()
				.getAggregationKind();
		AggregationKind toAK = (getAttributedElementClass()).getTo()
				.getAggregationKind();
		return fromAK != AggregationKind.NONE ? fromAK
				: (toAK != AggregationKind.NONE ? toAK : AggregationKind.NONE);
	}

	@Override
	public boolean isTemporary() {
		return true;
	}

	@Override
	public EdgeClass getPreliminaryType() {
		return this.preliminaryType;
	}

	@Override
	public void setPreliminaryType(EdgeClass ec) {
		this.preliminaryType = ec;
	}

	@Override
	public final String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("+te");
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
