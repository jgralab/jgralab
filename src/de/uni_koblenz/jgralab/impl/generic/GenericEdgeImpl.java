package de.uni_koblenz.jgralab.impl.generic;

import java.io.IOException;

import de.uni_koblenz.jgralab.Edge;
import de.uni_koblenz.jgralab.EdgeDirection;
import de.uni_koblenz.jgralab.Graph;
import de.uni_koblenz.jgralab.GraphIO;
import de.uni_koblenz.jgralab.GraphIOException;
import de.uni_koblenz.jgralab.NoSuchAttributeException;
import de.uni_koblenz.jgralab.Vertex;
import de.uni_koblenz.jgralab.impl.InternalGraph;
import de.uni_koblenz.jgralab.impl.ReversedEdgeBaseImpl;
import de.uni_koblenz.jgralab.impl.std.EdgeImpl;
import de.uni_koblenz.jgralab.schema.AggregationKind;
import de.uni_koblenz.jgralab.schema.Attribute;
import de.uni_koblenz.jgralab.schema.EdgeClass;

/**
 * A generic {@link Edge}-Implementation that can represent edges of arbitrary
 * {@link Schema}s.
 */
public class GenericEdgeImpl extends EdgeImpl {

	private EdgeClass type;
	private Object[] attributes;

	public GenericEdgeImpl(EdgeClass type, int anId, Graph graph, Vertex alpha,
			Vertex omega) {
		super(anId, graph, alpha, omega);
		this.type = type;
		if (type.getAttributeCount() > 0) {
			attributes = new Object[type.getAttributeCount()];
			if (!((InternalGraph) graph).isLoading()) {
				GenericGraphImpl.initializeGenericAttributeValues(this);
			}
		}
		((GenericGraphImpl) graph).addEdge(this, alpha, omega);
	}

	@Override
	protected ReversedEdgeBaseImpl createReversedEdge() {
		return new GenericReversedEdgeImpl(this, graph);
	}

	@Override
	public EdgeClass getAttributedElementClass() {
		return type;
	}

	@Override
	public void readAttributeValueFromString(String attributeName, String value)
			throws GraphIOException, NoSuchAttributeException {
		int i = type.getAttributeIndex(attributeName);
		attributes[i] = type
				.getAttribute(attributeName)
				.getDomain()
				.parseGenericAttribute(
						GraphIO.createStringReader(value, getSchema()));
	}

	@Override
	public void readAttributeValues(GraphIO io) throws GraphIOException {
		for (Attribute a : type.getAttributeList()) {
			attributes[type.getAttributeIndex(a.getName())] = a.getDomain()
					.parseGenericAttribute(io);
		}
	}

	@Override
	public String writeAttributeValueToString(String attributeName)
			throws IOException, GraphIOException, NoSuchAttributeException {
		GraphIO io = GraphIO.createStringWriter(getSchema());
		type.getAttribute(attributeName).getDomain()
				.serializeGenericAttribute(io, getAttribute(attributeName));
		return io.getStringWriterResult();
	}

	@Override
	public void writeAttributeValues(GraphIO io) throws IOException,
			GraphIOException {
		for (Attribute a : type.getAttributeList()) {
			a.getDomain().serializeGenericAttribute(io,
					getAttribute(a.getName()));
		}
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
	public AggregationKind getAlphaAggregationKind() {
		return (getAttributedElementClass()).getFrom().getAggregationKind();
	}

	@Override
	public AggregationKind getOmegaAggregationKind() {
		return (getAttributedElementClass()).getTo().getAggregationKind();
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> T getAttribute(String name) throws NoSuchAttributeException {
		int i = type.getAttributeIndex(name);
		return (T) attributes[i];
	}

	@Override
	public <T> void setAttribute(String name, T data)
			throws NoSuchAttributeException {
		int i = type.getAttributeIndex(name);
		if (getAttributedElementClass().getAttribute(name).getDomain()
				.isConformGenericValue(data)) {
			this.ecaAttributeChanging(name, this.attributes[i], data);
			Object old_value = this.attributes[i];
			attributes[i] = data;
			this.ecaAttributeChanged(name, old_value, data);
		} else {
			throw new ClassCastException();
		}
	}

	@Override
	public Edge getNextEdge(EdgeClass anEdgeClass) {
		Edge currentEdge = getNextEdge();
		while (currentEdge != null) {
			if (currentEdge.getAttributedElementClass().equals(anEdgeClass)
					|| currentEdge.getAttributedElementClass()
							.getAllSuperClasses().contains(anEdgeClass)) {
				return currentEdge;
			}
			currentEdge = currentEdge.getNextEdge();
		}
		return currentEdge;
	}

	@Override
	public Edge getNextIncidence(EdgeClass anEdgeClass) {
		return getNextIncidence(anEdgeClass, EdgeDirection.INOUT, false);
	}

	@Override
	public Edge getNextIncidence(EdgeClass anEdgeClass,
			EdgeDirection orientation) {
		return getNextIncidence(anEdgeClass, orientation, false);
	}

	@Override
	public Edge getNextIncidence(EdgeClass anEdgeClass, boolean noSubClasses) {
		return getNextIncidence(anEdgeClass, EdgeDirection.INOUT, noSubClasses);
	}

	@Override
	public Edge getNextIncidence(EdgeClass anEdgeClass,
			EdgeDirection orientation, boolean noSubclasses) {
		Edge currentEdge = getNextIncidence(orientation);
		while (currentEdge != null) {
			if (noSubclasses) {
				if (currentEdge.getAttributedElementClass().equals(anEdgeClass)) {
					return currentEdge;
				}
			} else {
				if (anEdgeClass.equals(currentEdge.getAttributedElementClass())
						|| anEdgeClass.getAllSubClasses().contains(
								currentEdge.getAttributedElementClass())) {
					return currentEdge;
				}
			}
			currentEdge = currentEdge.getNextIncidence(orientation);
		}
		return currentEdge;
	}

	@Override
	public void initializeAttributesWithDefaultValues() {
		GenericGraphImpl.initializeGenericAttributeValues(this);
	}

	// ************** unsupported methods ***************/
	@Override
	public Class<? extends Edge> getSchemaClass() {
		throw new UnsupportedOperationException(
				"This method is not supported by the generic implementation");
	}

	@Override
	public Edge getNextEdge(Class<? extends Edge> anEdgeClass) {
		throw new UnsupportedOperationException(
				"This method is not supported by the generic implementation");
	}

	@Override
	public Edge getNextIncidence(Class<? extends Edge> anEdgeClass) {
		throw new UnsupportedOperationException(
				"This method is not supported by the generic implementation");
	}

	@Override
	public Edge getNextIncidence(Class<? extends Edge> anEdgeClass,
			boolean noSubclasses) {
		throw new UnsupportedOperationException(
				"This method is not supported by the generic implementation");
	}

	@Override
	public Edge getNextIncidence(Class<? extends Edge> anEdgeClass,
			EdgeDirection orientation) {
		throw new UnsupportedOperationException(
				"This method is not supported by the generic implementation");
	}

	@Override
	public Edge getNextIncidence(Class<? extends Edge> anEdgeClass,
			EdgeDirection orientation, boolean noSubclasses) {
		throw new UnsupportedOperationException(
				"This method is not supported by the generic implementation");
	}

	@Override
	public boolean isInstanceOf(EdgeClass cls) {
		// Needs to be overridden from the base variant, because that relies on
		// code generation.
		return type.equals(cls) || type.isSubClassOf(cls);
	}

}
