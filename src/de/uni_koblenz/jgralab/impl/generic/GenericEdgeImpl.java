package de.uni_koblenz.jgralab.impl.generic;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import de.uni_koblenz.jgralab.*;
import de.uni_koblenz.jgralab.impl.ReversedEdgeBaseImpl;
import de.uni_koblenz.jgralab.impl.std.EdgeImpl;
import de.uni_koblenz.jgralab.impl.std.GraphImpl;
import de.uni_koblenz.jgralab.schema.*;

public class GenericEdgeImpl extends EdgeImpl {
	
	private EdgeClass type;
	private Map<String, Object> attributes;

	public GenericEdgeImpl(EdgeClass type, int anId, Graph graph, Vertex alpha, Vertex omega) {
		super(anId, graph, alpha, omega);
		this.type = type;
		((GraphImpl) graph).addEdge(this, alpha, omega);
		if(type.getAttributeCount() > 0) {
			attributes = new HashMap<String, Object>();
			for(Attribute a : type.getAttributeList()) {
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
	public AttributedElementClass getAttributedElementClass() {
		return type;
	}

	@Override
	public void readAttributeValueFromString(String attributeName, String value)
			throws GraphIOException, NoSuchAttributeException {
		if(attributes != null && attributes.containsKey(attributeName)) {
			attributes.put(attributeName, GenericUtil.parseGenericAttribute(type.getAttribute(attributeName).getDomain(), GraphIO.createStringReader(value, getSchema())));
		}
		else {
			throw new NoSuchAttributeException("DefaultValueTestGraph doesn't contain an attribute " + attributeName);
		}
		
	}

	@Override
	public void readAttributeValues(GraphIO io) throws GraphIOException {
		for(Attribute a : type.getAttributeList()) {
			attributes.put(a.getName(), GenericUtil.parseGenericAttribute(a.getDomain(), io));
		}
	}

	@Override
	public String writeAttributeValueToString(String attributeName)
			throws IOException, GraphIOException, NoSuchAttributeException {
		GraphIO io = GraphIO.createStringWriter(getSchema());
		GenericUtil.serializeGenericAttribute(io, type.getAttribute(attributeName).getDomain(), getAttribute(attributeName));
		return io.getStringWriterResult();
	}

	@Override
	public void writeAttributeValues(GraphIO io) throws IOException,
			GraphIOException {
		for(Attribute a : type.getAttributeList()) {
			GenericUtil.serializeGenericAttribute(io, a.getDomain(), attributes.get(a.getName()));
		}
		
	}

	@Override
	public AggregationKind getAggregationKind() {
		AggregationKind fromAK = ((EdgeClass) getAttributedElementClass()).getFrom().getAggregationKind();
		AggregationKind toAK = ((EdgeClass) getAttributedElementClass()).getTo().getAggregationKind();
		return fromAK != AggregationKind.NONE ? fromAK : (toAK != AggregationKind.NONE ? toAK : AggregationKind.NONE);
	}

	@Override
	public AggregationKind getAlphaAggregationKind() {
		return ((EdgeClass) getAttributedElementClass()).getFrom().getAggregationKind();
	}

	@Override
	public AggregationKind getOmegaAggregationKind() {
		return ((EdgeClass) getAttributedElementClass()).getTo().getAggregationKind();
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> T getAttribute(String name) throws NoSuchAttributeException {
		if(attributes == null || !attributes.containsKey(name)) {
			throw new NoSuchAttributeException(type.getSimpleName() + " doesn't contain an attribute " + name);
		}
		else {
			return (T) attributes.get(name);
		}
	}

	@Override
	public <T> void setAttribute(String name, T data)
			throws NoSuchAttributeException {
		if(attributes == null || !attributes.containsKey(name)) {
			throw new NoSuchAttributeException(type.getSimpleName() + " doesn't contain an attribute " + name);
		} else {
			try {
				if(!GenericUtil.testDomainConformity(data, type.getAttribute(name).getDomain())) {
					throw new ClassCastException();
				}
				else {
					attributes.put(name, data);
				}
			} catch (ClassNotFoundException e) {
				System.err.println(type.getAttribute(name).getDomain() + ".getJavaClassName(String schemaRootPackagePrefix) returned an unknown class name.");
				e.printStackTrace();
			}
		}
	}
	
	@Override
	public Edge getNextEdge(EdgeClass anEdgeClass) {
		Edge currentEdge = getNextEdge();
		while(currentEdge != null) {
			if(currentEdge.getAttributedElementClass().equals(anEdgeClass)) {
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
	public Edge getNextIncidence(EdgeClass anEdgeClass, EdgeDirection orientation) {
		return getNextIncidence(anEdgeClass, orientation, false);
	}
	
	@Override
	public Edge getNextIncidence(EdgeClass anEdgeClass, EdgeDirection orientation, boolean noSubclasses) {
		Edge currentEdge = getNextIncidence(orientation);
		while(currentEdge != null) {
			if(noSubclasses) {
				if(currentEdge.getAttributedElementClass().equals(anEdgeClass)) {
					return currentEdge;
				}
			}
			else {
				if(anEdgeClass.equals(currentEdge.getAttributedElementClass()) || anEdgeClass.getAllSubClasses().contains(currentEdge.getAttributedElementClass())) {
					return currentEdge;
				}
			}
			currentEdge = currentEdge.getNextIncidence(orientation);
		}
		return currentEdge;
	}
	

	//************** unsupported methods ***************/
	@Override
	public Class<? extends AttributedElement> getSchemaClass() {
		throw new UnsupportedOperationException("This method is not supported by the generic implementation");
	}
	
	@Override
	public Edge getNextEdge(Class<? extends Edge> anEdgeClass) {
		throw new UnsupportedOperationException("This method is not supported by the generic implementation");
	}
	
	@Override
	public Edge getNextIncidence(Class<? extends Edge> anEdgeClass) {
		throw new UnsupportedOperationException("This method is not supported by the generic implementation");
	}
	
	@Override
	public Edge getNextIncidence(Class<? extends Edge> anEdgeClass, boolean noSubclasses) {
		throw new UnsupportedOperationException("This method is not supported by the generic implementation");
	}
	
	@Override
	public Edge getNextIncidence(Class<? extends Edge> anEdgeClass, EdgeDirection orientation) {
		throw new UnsupportedOperationException("This method is not supported by the generic implementation");
	}
	
	@Override
	public Edge getNextIncidence(Class<? extends Edge> anEdgeClass, EdgeDirection orientation, boolean noSubclasses) {
		throw new UnsupportedOperationException("This method is not supported by the generic implementation");
	}

}
