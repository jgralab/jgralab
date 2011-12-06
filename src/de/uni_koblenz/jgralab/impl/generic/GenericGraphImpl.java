package de.uni_koblenz.jgralab.impl.generic;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.pcollections.POrderedSet;

import de.uni_koblenz.jgralab.AttributedElement;
import de.uni_koblenz.jgralab.Edge;
import de.uni_koblenz.jgralab.Graph;
import de.uni_koblenz.jgralab.GraphException;
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
	
	// TODO The legal incidenceClasses of vertices must be cached
	// and available before an edge is created! 
	
	private GraphClass type;
	private Map<String, Object> attributes;
	private Map<VertexClass, Set<IncidenceClass>> vcInIcCache;
	private Map<VertexClass, Set<IncidenceClass>> vcOutIcCache;

	protected GenericGraphImpl(String id, GraphClass type) {
		super(id, type, 100, 100);
	}
	
	protected GenericGraphImpl(GraphClass type, String id, int vmax, int emax) {
		super(id, type, vmax, emax);
		this.type = type;
		if(type.getAttributeCount() > 0) {
			attributes = new HashMap<String, Object>();
			for(Attribute a : type.getAttributeList()) {
				attributes.put(a.getName(), null);
			}
			initializeAttributesWithDefaultValues();
		}
		vcOutIcCache = new HashMap<VertexClass, Set<IncidenceClass>>();
		vcInIcCache = new HashMap<VertexClass, Set<IncidenceClass>>();
	}
	
	/**
	 * Creates a new instance if a generic Graph. This method isn't supposed to be called manually.
	 * Use <code>Schema.createGraph(ImplementationType.Generic)</code> instead!
	 */
	public static Graph create(GraphClass type, int vmax, int emax) {
		return new GenericGraphImpl(type, null, vmax, emax);
	}
	
	
	/**
	 * Creates a new {@link GenericVertexImpl} in the graph that conforms to a given {@Link VertexClass}
	 * from the Schema.
	 */
	@SuppressWarnings("unchecked")
	@Override
	public <T extends Vertex> T createVertex(VertexClass vc) {
		try {
			return (T) new GenericVertexImpl(vc, 0, this);
		}
		catch(Exception e) {
			if(e instanceof GraphException) {
				throw (GraphException) e;
			}
			else {
				throw new GraphException("Error creating vertex of VertexClass " + vc);
			}
		}
	}

	
	/**
	 * Creates a new {@Link GenericEdgeImpl} in the Graph that conforms to a given {@link EdgeClass}
	 * from the Schema.
	 */
	@SuppressWarnings("unchecked")
	@Override
	public <T extends Edge> T createEdge(EdgeClass ec, Vertex alpha, Vertex omega) {
		try {
			return (T) new GenericEdgeImpl(ec, 0, this, alpha, omega);
		}
		catch(Exception e) {
			if(e instanceof GraphException) {
				throw (GraphException) e;
			}
			else {
				throw new GraphException("Error creating edge of EdgeClass " + ec);
			}
		}
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
	public Class<? extends AttributedElement> getSchemaClass() {
		throw new UnsupportedOperationException("getSchemaClass is not supported by the generic implementation");
	}
	

	/**
	 * Returns the cache containing the allowed outgoing {@link IncidenceClass}es
	 * of the {@link VertexClass}es in this Graph's Schema.
	 */
	public boolean cachedIsValidAlpha(VertexClass vc, EdgeClass ec) {
		if(!vcOutIcCache.containsKey(vc)) {
			vcOutIcCache.put(vc, vc.getAllOutIncidenceClasses());
		}
		return vcOutIcCache.get(vc).contains(ec.getFrom());
	}
	
	/**
	 * Returns the cache containing the allowed incoming {@link IncidenceClass}es
	 * of the {@link VertexClass}es in this Graph's Schema.
	 */
	public boolean cachedIsValidOmega(VertexClass vc, EdgeClass ec) {
		if(!vcInIcCache.containsKey(vc)) {
			vcInIcCache.put(vc, vc.getAllInIncidenceClasses());
		}
		return vcInIcCache.get(vc).contains(ec.getTo());
	}
	
	// TODO Methoden zur Traversierung!

}
