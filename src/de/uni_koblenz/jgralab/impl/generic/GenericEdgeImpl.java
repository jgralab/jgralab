package de.uni_koblenz.jgralab.impl.generic;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import de.uni_koblenz.jgralab.Edge;
import de.uni_koblenz.jgralab.EdgeDirection;
import de.uni_koblenz.jgralab.Graph;
import de.uni_koblenz.jgralab.GraphIO;
import de.uni_koblenz.jgralab.GraphIOException;
import de.uni_koblenz.jgralab.NoSuchAttributeException;
import de.uni_koblenz.jgralab.Vertex;
import de.uni_koblenz.jgralab.impl.ReversedEdgeBaseImpl;
import de.uni_koblenz.jgralab.impl.std.EdgeImpl;
import de.uni_koblenz.jgralab.impl.std.GraphImpl;
import de.uni_koblenz.jgralab.schema.AggregationKind;
import de.uni_koblenz.jgralab.schema.Attribute;
import de.uni_koblenz.jgralab.schema.AttributedElementClass;
import de.uni_koblenz.jgralab.schema.EdgeClass;

public class GenericEdgeImpl extends EdgeImpl {

	private EdgeClass type;
	private Map<String, Object> attributes;

	public GenericEdgeImpl(EdgeClass type, int anId, Graph graph, Vertex alpha,
			Vertex omega) {
		super(anId, graph, alpha, omega);
		this.type = type;
		((GraphImpl) graph).addEdge(this, alpha, omega);
		if (type.getAttributeCount() > 0) {
			attributes = new HashMap<String, Object>();
			for (Attribute a : type.getAttributeList()) {
				attributes.put(a.getName(), null);
			}
			initializeAttributesWithDefaultValues();
		}
	}

	@Override
	protected void addToGraph(Graph graph, Vertex alpha, Vertex omega) {
		// Do nothing. Edge will be added to the graph later.
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
		if ((attributes != null) && attributes.containsKey(attributeName)) {
			attributes.put(
					attributeName,
					type.getAttribute(attributeName)
							.getDomain()
							.parseGenericAttribute(
									GraphIO.createStringReader(value,
											getSchema())));
		} else {
			throw new NoSuchAttributeException(
					"DefaultValueTestGraph doesn't contain an attribute "
							+ attributeName);
		}

	}

	@Override
	public void readAttributeValues(GraphIO io) throws GraphIOException {
		for (Attribute a : type.getAttributeList()) {
			attributes
					.put(a.getName(), a.getDomain().parseGenericAttribute(io));
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
					attributes.get(a.getName()));
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
		if ((attributes == null) || !attributes.containsKey(name)) {
			throw new NoSuchAttributeException(type.getSimpleName()
					+ " doesn't contain an attribute " + name);
		} else {
			return (T) attributes.get(name);
		}
	}

	@Override
	public <T> void setAttribute(String name, T data)
			throws NoSuchAttributeException {
		if ((attributes == null) || !attributes.containsKey(name)) {
			throw new NoSuchAttributeException(type.getSimpleName()
					+ " doesn't contain an attribute " + name);
		} else {
			if (!type.getAttribute(name).getDomain().genericIsConform(data)) {
				throw new ClassCastException();
			} else {
				attributes.put(name, data);
			}
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
		for (Attribute attr : getAttributedElementClass().getAttributeList()) {
			if (attr.getDefaultValueAsString() != null) {
				try {
					internalSetDefaultValue(attr);
				} catch (GraphIOException e) {
					e.printStackTrace();
				}
			} else {
				setAttribute(attr.getName(),
						GenericGraphImpl.genericAttributeDefaultValue(attr
								.getDomain()));
			}
		}
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
	public boolean isInstanceOf(AttributedElementClass cls) {
		// Needs to be overridden from the base variant, because that relies on
		// code generation.
		return type.equals(cls) || type.isSubClassOf(cls);
	}

}
