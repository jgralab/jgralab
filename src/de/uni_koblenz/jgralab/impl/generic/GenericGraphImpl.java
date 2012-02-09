package de.uni_koblenz.jgralab.impl.generic;

import java.io.IOException;
import java.util.Map;

import org.pcollections.POrderedSet;

import de.uni_koblenz.jgralab.AttributedElement;
import de.uni_koblenz.jgralab.Edge;
import de.uni_koblenz.jgralab.Graph;
import de.uni_koblenz.jgralab.GraphException;
import de.uni_koblenz.jgralab.GraphIO;
import de.uni_koblenz.jgralab.GraphIOException;
import de.uni_koblenz.jgralab.NoSuchAttributeException;
import de.uni_koblenz.jgralab.Record;
import de.uni_koblenz.jgralab.Vertex;
import de.uni_koblenz.jgralab.impl.EdgeIterable;
import de.uni_koblenz.jgralab.impl.RecordImpl;
import de.uni_koblenz.jgralab.impl.VertexIterable;
import de.uni_koblenz.jgralab.impl.std.GraphImpl;
import de.uni_koblenz.jgralab.schema.Attribute;
import de.uni_koblenz.jgralab.schema.BasicDomain;
import de.uni_koblenz.jgralab.schema.BooleanDomain;
import de.uni_koblenz.jgralab.schema.Domain;
import de.uni_koblenz.jgralab.schema.DoubleDomain;
import de.uni_koblenz.jgralab.schema.EdgeClass;
import de.uni_koblenz.jgralab.schema.EnumDomain;
import de.uni_koblenz.jgralab.schema.GraphClass;
import de.uni_koblenz.jgralab.schema.IntegerDomain;
import de.uni_koblenz.jgralab.schema.LongDomain;
import de.uni_koblenz.jgralab.schema.RecordDomain;
import de.uni_koblenz.jgralab.schema.RecordDomain.RecordComponent;
import de.uni_koblenz.jgralab.schema.VertexClass;

/**
 * A generic {@link Graph}-Implementation that can represent TGraphs of
 * arbitrary {@link Schema}s.
 */
public class GenericGraphImpl extends GraphImpl {

	private GraphClass type;
	private Object[] attributes;

	protected GenericGraphImpl(GraphClass type, String id) {
		super(id, type, 100, 100);
	}

	protected GenericGraphImpl(GraphClass type, String id, int vmax, int emax) {
		super(id, type, vmax, emax);
		this.type = type;
		attributes = new Object[type.getAttributeCount()];
		if (!isLoading()) {
			GenericGraphImpl.initializeGenericAttributeValues(this);
		}
	}

	/**
	 * Creates a new instance of a generic Graph. This method isn't supposed to
	 * be called manually. Use
	 * <code>Schema.createGraph(ImplementationType.Generic)</code> instead!
	 */
	public static Graph createGraph(GraphClass type, String id, int vmax,
			int emax) {
		return new GenericGraphImpl(type, id, vmax, emax);
	}

	/**
	 * Creates a new {@link GenericVertexImpl} in the graph that conforms to a
	 * given {@Link VertexClass} from the Schema.
	 */
	@Override
	public <T extends Vertex> T createVertex(VertexClass vc) {
		return graphFactory.createVertex(vc, 0, this);
	}

	/**
	 * Creates a new {@Link GenericEdgeImpl} in the Graph that conforms
	 * to a given {@link EdgeClass} from the Schema.
	 */
	@Override
	public <T extends Edge> T createEdge(EdgeClass ec, Vertex alpha,
			Vertex omega) {
		return graphFactory.createEdge(ec, 0, this, alpha, omega);
	}

	@Override
	public GraphClass getAttributedElementClass() {
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

	@SuppressWarnings("unchecked")
	@Override
	public <T> T getAttribute(String name) throws NoSuchAttributeException {
		int i = getAttributedElementClass().getAttributeIndex(name);
		return (T) attributes[i];
	}

	@Override
	public <T> void setAttribute(String name, T data)
			throws NoSuchAttributeException {
		int i = getAttributedElementClass().getAttributeIndex(name);
		if (type.getAttribute(name).getDomain().isConformGenericValue(data)) {
			attributes[i] = data;
		} else {
			throw new ClassCastException();
		}
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
		initializeGenericAttributeValues(this);
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
				return Boolean.valueOf(false);
			} else if (domain instanceof IntegerDomain) {
				return Integer.valueOf(0);
			} else if (domain instanceof LongDomain) {
				return Long.valueOf(0);
			} else if (domain instanceof DoubleDomain) {
				return Double.valueOf(0.0);
			} else { // StringDomain
				return null;
			}
		} else {
			return null;
		}
	}

	// protected int getAttributeIndex(AttributedElementClass<?, ?> aec,
	// String name) {
	// assert (aec.getAttributeCount() > 0);
	// Map<String, Integer> indexMap = getIndexMap(aec);
	// Integer i = indexMap.get(name);
	// return i == null ? Integer.MAX_VALUE : i.intValue();
	// }

	// protected Map<String, Integer> getIndexMap(AttributedElementClass<?, ?>
	// aec) {
	// if (attributeIndexMaps.containsKey(aec)) {
	// return attributeIndexMaps.get(aec);
	// } else {
	// HashMap<String, Integer> valueIndex = new HashMap<String, Integer>();
	// int i = 0;
	// for (Attribute a : aec.getAttributeList()) {
	// valueIndex.put(a.getName(), i);
	// ++i;
	// }
	// attributeIndexMaps.put(aec, valueIndex);
	// return valueIndex;
	// }
	// }

	/**
	 * Initializes attributes of an (generic) AttributedElement with their
	 * default values.
	 */
	static void initializeGenericAttributeValues(AttributedElement<?, ?> ae) {
		for (Attribute attr : ae.getAttributedElementClass().getAttributeList()) {
			if ((attr.getDefaultValueAsString() != null)
					&& !attr.getDefaultValueAsString().isEmpty()) {
				try {
					attr.setDefaultValue(ae);
				} catch (GraphIOException e) {
					e.printStackTrace();
				}
			} else {
				ae.setAttribute(attr.getName(),
						genericAttributeDefaultValue(attr.getDomain()));
			}
		}
	}

	@Override
	public boolean isInstanceOf(GraphClass cls) {
		// Needs to be overridden from the base variant, because that relies on
		// code generation.
		return type.equals(cls) || type.isSubClassOf(cls);
	}

	@Override
	public Object getEnumConstant(EnumDomain enumDomain, String constantName) {
		for (String cn : enumDomain.getConsts()) {
			if (cn.equals(constantName)) {
				return cn;
			}
		}
		throw new GraphException("No such enum constant '" + constantName
				+ "' in EnumDomain " + enumDomain);
	}

	@Override
	public Record createRecord(RecordDomain recordDomain,
			Map<String, Object> values) {
		RecordImpl record = RecordImpl.empty();
		for (RecordComponent c : recordDomain.getComponents()) {
			assert (values.containsKey(c.getName()));
			record = record.plus(c.getName(), values.get(c.getName()));
		}
		return record;
	}

	// ************** unsupported methods ***************/
	@Override
	public Class<? extends Graph> getSchemaClass() {
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
}
