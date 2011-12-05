package de.uni_koblenz.jgralab.impl.generic;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import de.uni_koblenz.jgralab.*;
import de.uni_koblenz.jgralab.impl.ReversedEdgeBaseImpl;
import de.uni_koblenz.jgralab.impl.std.EdgeImpl;
import de.uni_koblenz.jgralab.schema.*;

public class GenericEdgeImpl extends EdgeImpl {
	
	private EdgeClass type;
	private Map<String, Object> attributes;

	protected GenericEdgeImpl(EdgeClass type, int anId, Graph graph, Vertex alpha, Vertex omega) {
		super(anId, graph, alpha, omega);
		this.type = type;
		attributes = new HashMap<String, Object>();
		for(Attribute a : type.getAttributeList()) {
			attributes.put(a.getName(), null);
		}
		initializeAttributesWithDefaultValues();
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
		// TODO Auto-generated method stub
		
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
	public void readAttributeValues(GraphIO io) throws GraphIOException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public AggregationKind getAggregationKind() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public AggregationKind getAlphaAggregationKind() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public AggregationKind getOmegaAggregationKind() {
		// TODO Auto-generated method stub
		return null;
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> T getAttribute(String name) throws NoSuchAttributeException {
		if(!attributes.containsKey(name)) {
			throw new NoSuchAttributeException(type.getSimpleName() + " doesn't contain an attribute " + name);
		}
		else {
			return (T) attributes.get(name);
		}
	}

	@Override
	public <T> void setAttribute(String name, T data)
			throws NoSuchAttributeException {
		if(!attributes.containsKey(name)) {
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
	public Class<? extends AttributedElement> getSchemaClass() {
		throw new UnsupportedOperationException("getSchemaClass is not supported by the generic implementation");
	}

}
