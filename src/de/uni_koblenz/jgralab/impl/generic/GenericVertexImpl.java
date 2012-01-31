package de.uni_koblenz.jgralab.impl.generic;

import java.io.IOException;
import java.util.Map;

import de.uni_koblenz.jgralab.Edge;
import de.uni_koblenz.jgralab.EdgeDirection;
import de.uni_koblenz.jgralab.Graph;
import de.uni_koblenz.jgralab.GraphIO;
import de.uni_koblenz.jgralab.GraphIOException;
import de.uni_koblenz.jgralab.NoSuchAttributeException;
import de.uni_koblenz.jgralab.Vertex;
import de.uni_koblenz.jgralab.impl.IncidenceIterable;
import de.uni_koblenz.jgralab.impl.InternalVertex;
import de.uni_koblenz.jgralab.impl.std.VertexImpl;
import de.uni_koblenz.jgralab.schema.Attribute;
import de.uni_koblenz.jgralab.schema.EdgeClass;
import de.uni_koblenz.jgralab.schema.IncidenceClass;
import de.uni_koblenz.jgralab.schema.VertexClass;
import de.uni_koblenz.jgralab.schema.impl.DirectedSchemaEdgeClass;

public class GenericVertexImpl extends VertexImpl {

	private final VertexClass type;
	private Map<String, Object> attributes;

	protected GenericVertexImpl(VertexClass type, int id, Graph graph) {
		super(id, graph);
		this.type = type;
		attributes = GenericGraphImpl.initializeAttributes(type);
		GenericGraphImpl.initializeGenericAttributeValues(this);
	}

	@Override
	public VertexClass getAttributedElementClass() {
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
			return;
		}
		throw new NoSuchAttributeException(this + " doesn't have an attribute "
				+ attributeName);
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

	@Override
	public Vertex getNextVertex(VertexClass vertexClass) {
		InternalVertex currentVertex = (InternalVertex) getNextVertex();
		while (currentVertex != null) {
			if (currentVertex.getAttributedElementClass().equals(vertexClass)
					|| currentVertex.getAttributedElementClass()
							.getAllSuperClasses().contains(vertexClass)) {
				return currentVertex;
			}
			currentVertex = (InternalVertex) currentVertex.getNextVertex();
		}
		return currentVertex;
	}

	@Override
	public Edge getFirstIncidence(EdgeClass anEdgeClass) {
		return getFirstIncidence(anEdgeClass, EdgeDirection.INOUT);
	}

	@Override
	public Edge getFirstIncidence(EdgeClass anEdgeClass,
			EdgeDirection orientation) {
		Edge currentEdge = getFirstIncidence(orientation);
		while (currentEdge != null) {
			if (currentEdge.getNormalEdge().getAttributedElementClass()
					.equals(anEdgeClass)
					|| anEdgeClass.getAllSubClasses().contains(
							currentEdge.getAttributedElementClass())) {
				break;
			}
			currentEdge = currentEdge.getNextIncidence(orientation);
		}
		return currentEdge;
	}

	@Override
	public int getDegree(EdgeClass ec) {
		return getDegree(ec, EdgeDirection.INOUT);
	}

	@Override
	public int getDegree(EdgeClass ec, EdgeDirection direction) {
		int degree = 0;
		for (Edge e = getFirstIncidence(ec, direction); e != null; e = e
				.getNextIncidence(ec, direction)) {
			++degree;
		}
		return degree;
	}

	@Override
	public void initializeAttributesWithDefaultValues() {
		GenericGraphImpl.initializeGenericAttributeValues(this);
	}

	@Override
	public DirectedSchemaEdgeClass getEdgeForRolename(String rolename) {
		// TODO Optimize!
		for(IncidenceClass ic : getAttributedElementClass().getAllInIncidenceClasses()) {
			if(ic.getRolename().equals(rolename)) {
				return new DirectedSchemaEdgeClass(ic.getEdgeClass(), EdgeDirection.IN);
			}
		}
		for(IncidenceClass ic : getAttributedElementClass().getAllOutIncidenceClasses()) {
			if(ic.getRolename().equals(rolename)) {
				return new DirectedSchemaEdgeClass(ic.getEdgeClass(), EdgeDirection.OUT);
			}
		}
		return null;
	}

	@Override
	public Iterable<Edge> incidences(EdgeClass eClass) {
		return new IncidenceIterable<Edge>(this, eClass);
	}

	@Override
	public Iterable<Edge> incidences(EdgeClass eClass, EdgeDirection dir) {
		return new IncidenceIterable<Edge>(this, eClass, dir);
	}

	// ************** unsupported methods ***************/
	@Override
	public Class<? extends Vertex> getSchemaClass() {
		throw new UnsupportedOperationException(
				"This method is not supported by the generic implementation");
	}

	@Override
	public Vertex getNextVertex(Class<? extends Vertex> vertexClass) {
		throw new UnsupportedOperationException(
				"This method is not supported by the generic implementation");
	}

	@Override
	public Edge getFirstIncidence(Class<? extends Edge> anEdgeClass) {
		throw new UnsupportedOperationException(
				"This method is not supported by the generic implementation");
	}

	@Override
	public Edge getFirstIncidence(Class<? extends Edge> anEdgeClass,
			EdgeDirection orientation) {
		throw new UnsupportedOperationException(
				"This method is not supported by the generic implementation");
	}

	@Override
	public int getDegree(Class<? extends Edge> ec) {
		throw new UnsupportedOperationException(
				"This method is not supported by the generic implementation");
	}

	@Override
	public int getDegree(Class<? extends Edge> ec, EdgeDirection direction) {
		throw new UnsupportedOperationException(
				"This method is not supported by the generic implementation");
	}

	@Override
	public boolean isInstanceOf(VertexClass cls) {
		// Needs to be overridden from the base variant, because that relies on
		// code generation.
		return type.equals(cls) || type.isSubClassOf(cls);
	}
}
