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
import de.uni_koblenz.jgralab.impl.EdgeIterable;
import de.uni_koblenz.jgralab.impl.VertexIterable;
import de.uni_koblenz.jgralab.impl.std.GraphImpl;
import de.uni_koblenz.jgralab.schema.Attribute;
import de.uni_koblenz.jgralab.schema.AttributedElementClass;
import de.uni_koblenz.jgralab.schema.BasicDomain;
import de.uni_koblenz.jgralab.schema.BooleanDomain;
import de.uni_koblenz.jgralab.schema.Domain;
import de.uni_koblenz.jgralab.schema.DoubleDomain;
import de.uni_koblenz.jgralab.schema.EdgeClass;
import de.uni_koblenz.jgralab.schema.GraphClass;
import de.uni_koblenz.jgralab.schema.IntegerDomain;
import de.uni_koblenz.jgralab.schema.LongDomain;
import de.uni_koblenz.jgralab.schema.VertexClass;

/**
 *
 * @author Bernhard
 *
 */
public class GenericGraphImpl extends GraphImpl {

	private GraphClass type;
	private Map<String, Object> attributes;
	private Map<VertexClass, Set<EdgeClass>> vcInEdgeCache;
	private Map<VertexClass, Set<EdgeClass>> vcOutEdgeCache;

	protected GenericGraphImpl(String id, GraphClass type) {
		super(id, type, 100, 100);
	}

	protected GenericGraphImpl(GraphClass type, String id, int vmax, int emax) {
		super(id, type, vmax, emax);
		this.type = type;
		if (type.getAttributeCount() > 0) {
			attributes = new HashMap<String, Object>();
			for (Attribute a : type.getAttributeList()) {
				attributes.put(a.getName(), null);
			}
			initializeAttributesWithDefaultValues();
		}
		vcOutEdgeCache = new HashMap<VertexClass, Set<EdgeClass>>();
		vcInEdgeCache = new HashMap<VertexClass, Set<EdgeClass>>();
	}

	/**
	 * Creates a new instance of a generic Graph. This method isn't supposed to
	 * be called manually. Use
	 * <code>Schema.createGraph(ImplementationType.Generic)</code> instead!
	 */
	public static Graph create(GraphClass type, String id, int vmax, int emax) {
		return new GenericGraphImpl(type, id, vmax, emax);
	}

	/**
	 * Creates a new {@link GenericVertexImpl} in the graph that conforms to a
	 * given {@Link VertexClass} from the Schema.
	 */
	@Override
	public <T extends Vertex> T createVertex(VertexClass vc) {
		return createVertex(vc, 0);
	}

	@SuppressWarnings("unchecked")
	public <T extends Vertex> T createVertex(VertexClass vc, int id) {
		if (type.getVertexClass(vc.getQualifiedName()) == null) {
			throw new GraphException("Error creating vertex of VertexClass "
					+ vc);
		}
		try {
			return (T) new GenericVertexImpl(vc, id, this);
		} catch (Exception e) {
			if (e instanceof GraphException) {
				throw (GraphException) e;
			} else {
				throw new GraphException(
						"Error creating vertex of VertexClass " + vc);
			}
		}
	}

	/**
	 * Creates a new {@Link GenericEdgeImpl} in the Graph that conforms
	 * to a given {@link EdgeClass} from the Schema.
	 */
	@Override
	public <T extends Edge> T createEdge(EdgeClass ec, Vertex alpha,
			Vertex omega) {
		return createEdge(ec, 0, alpha, omega);
	}

	@SuppressWarnings("unchecked")
	public <T extends Edge> T createEdge(EdgeClass ec, int id, Vertex alpha,
			Vertex omega) {
		try {
			return (T) new GenericEdgeImpl(ec, id, this, alpha, omega);
		} catch (Exception e) {
			if (e instanceof GraphException) {
				throw (GraphException) e;
			} else {
				throw new GraphException("Error creating edge of EdgeClass "
						+ ec);
			}
		}
	}

	@Override
	public AttributedElementClass getAttributedElementClass() {
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

	/**
	 * Checks the allowed outgoing {@link EdgeClass}es of a {@link VertexClass}.
	 * If this method is called for a <code>VertexClass</code> for the first
	 * time, the allowed outgoing <code>EdgeClass</code>es will be cached.
	 */
	public boolean cachedIsValidAlpha(VertexClass vc, EdgeClass ec) {
		if (!vcOutEdgeCache.containsKey(vc)) {
			vcOutEdgeCache.put(vc, vc.getValidFromEdgeClasses());
		}
		return vcOutEdgeCache.get(vc).contains(ec);
	}

	/**
	 * Checks the allowed incoming {@link EdgeClass}es of a {@link VertexClass}.
	 * If this method is called for a <code>VertexClass</code> for the first
	 * time, the allowed incoming <code>EdgeClass</code>es will be cached.
	 */
	public boolean cachedIsValidOmega(VertexClass vc, EdgeClass ec) {
		if (!vcInEdgeCache.containsKey(vc)) {
			vcInEdgeCache.put(vc, vc.getValidToEdgeClasses());
		}
		return vcInEdgeCache.get(vc).contains(ec);
	}

	@Override
	public Vertex getFirstVertex(VertexClass vertexClass) {
		Vertex v = getFirstVertex();
		if (v == null) {
			return null;
		}
		if (v.getAttributedElementClass().equals(vertexClass)
				|| v.getAttributedElementClass().getAllSuperClasses()
						.contains(vertexClass)) {
			return v;
		}
		return v.getNextVertex(vertexClass);
	}

	@Override
	public Edge getFirstEdge(EdgeClass edgeClass) {
		Edge e = getFirstEdge();
		if (e == null) {
			return null;
		}
		if (e.getAttributedElementClass().equals(edgeClass)
				|| e.getAttributedElementClass().getAllSuperClasses()
						.contains(edgeClass)) {
			return e;
		}
		return e.getNextEdge(edgeClass);
	}

	@Override
	public Iterable<Vertex> vertices(VertexClass vc) {
		return new VertexIterable<Vertex>(this, vc);
	}

	@Override
	public Iterable<Edge> edges(EdgeClass ec) {
		return new EdgeIterable<Edge>(this, ec);
	}

	@Override
	public void initializeAttributesWithDefaultValues() {
		for (Attribute attr : getAttributedElementClass().getAttributeList()) {
			if ((attr.getDefaultValueAsString() != null)
					&& !attr.getDefaultValueAsString().isEmpty()) {
				try {
					internalSetDefaultValue(attr);
				} catch (GraphIOException e) {
					e.printStackTrace();
				}
			} else {
				setAttribute(attr.getName(),
						genericAttributeDefaultValue(attr.getDomain()));
			}
		}
	}

	/**
	 * Returns the default value for attributes in the generic implementation if
	 * there is no explicitly defined default value, according to the
	 * attribute's domain.
	 *
	 * @param domain
	 *            The attribute's domain.
	 * @return The default value for attributes of the domain.
	 */
	public static Object genericAttributeDefaultValue(Domain domain) {
		if (domain instanceof BasicDomain) {
			if (domain instanceof BooleanDomain) {
				return new Boolean(false);
			} else if (domain instanceof IntegerDomain) {
				return new Integer(0);
			} else if (domain instanceof LongDomain) {
				return new Long(0);
			} else if (domain instanceof DoubleDomain) {
				return new Double(0.0);
			} else { // StringDomain
				return null;
			}
		} else {
			return null;
		}
	}

	// ************** unsupported methods ***************/
	@Override
	public Class<? extends AttributedElement> getSchemaClass() {
		throw new UnsupportedOperationException(
				"This method is not supported by the generic implementation");
	}

	@Override
	public Vertex getFirstVertex(Class<? extends Vertex> vertexClass) {
		throw new UnsupportedOperationException(
				"This method is not supported by the generic implementation");
	}

	@Override
	public Iterable<Vertex> vertices(Class<? extends Vertex> vertexClass) {
		throw new UnsupportedOperationException(
				"This method is not supported by the generic implementation");
	}

	@Override
	public Edge getFirstEdge(Class<? extends Edge> edgeClass) {
		throw new UnsupportedOperationException(
				"This method is not supported by the generic implementation");
	}

	@Override
	public Iterable<Edge> edges(Class<? extends Edge> edgeClass) {
		throw new UnsupportedOperationException(
				"This method is not supported by the generic implementation");
	}

	@Override
	public <T extends Vertex> POrderedSet<T> reachableVertices(
			Vertex startVertex, String pathDescription, Class<T> vertexType) {
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
