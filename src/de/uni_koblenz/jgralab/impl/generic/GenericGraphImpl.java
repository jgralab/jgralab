package de.uni_koblenz.jgralab.impl.generic;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.pcollections.POrderedSet;

import de.uni_koblenz.jgralab.AttributedElement;
import de.uni_koblenz.jgralab.Edge;
import de.uni_koblenz.jgralab.GraphIO;
import de.uni_koblenz.jgralab.GraphIOException;
import de.uni_koblenz.jgralab.NoSuchAttributeException;
import de.uni_koblenz.jgralab.Vertex;
import de.uni_koblenz.jgralab.impl.std.GraphImpl;
import de.uni_koblenz.jgralab.schema.*;

/**
 * 
 * @author Bernhard
 *
 */
public class GenericGraphImpl extends GraphImpl {
	
	private GraphClass type;
	private Map<String, Object> attributes;

	protected GenericGraphImpl(String id, GraphClass type) {
		super(id, type);
		this.type = type;
		attributes = new HashMap<String, Object>();
		for(Attribute a : type.getAttributeList()) {
			attributes.put(a.getName(), null);
		}
		initializeAttributesWithDefaultValues();
	}
	
	
	/**
	 * Creates a new {@link GenericVertexImpl} in the graph that conforms to a given {@Link VertexClass}
	 * from the Schema.
	 */
	@SuppressWarnings("unchecked")
	@Override
	public <T extends Vertex> T createVertex(VertexClass vc) {
		return (T) new GenericVertexImpl(vc, 0, this);
	}

	
	/**
	 * Creates a new {@Link GenericEdgeImpl} in the Graph that conforms to a given {@link EdgeClass}
	 * from the Schema.
	 */
	@SuppressWarnings("unchecked")
	@Override
	public <T extends Edge> T createEdge(EdgeClass ec, Vertex alpha, Vertex omega) {
		return (T) new GenericEdgeImpl(ec, 0, this, alpha, omega);
	}

	@Override
	public <T extends Vertex> POrderedSet<T> reachableVertices(Vertex startVertex,
			String pathDescription, Class<T> vertexType) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public AttributedElementClass getAttributedElementClass() {
		return type;
	}

	@Override
	public void readAttributeValueFromString(String attributeName, String value)
			throws GraphIOException, NoSuchAttributeException {
		
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
