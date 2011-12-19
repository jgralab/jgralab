package de.uni_koblenz.jgralab.impl.generic;


import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import de.uni_koblenz.jgralab.AttributedElement;
import de.uni_koblenz.jgralab.Edge;
import de.uni_koblenz.jgralab.EdgeDirection;
import de.uni_koblenz.jgralab.Graph;
import de.uni_koblenz.jgralab.GraphIO;
import de.uni_koblenz.jgralab.GraphIOException;
import de.uni_koblenz.jgralab.NoSuchAttributeException;
import de.uni_koblenz.jgralab.Vertex;
import de.uni_koblenz.jgralab.impl.InternalVertex;
import de.uni_koblenz.jgralab.impl.std.VertexImpl;
import de.uni_koblenz.jgralab.schema.*;
import de.uni_koblenz.jgralab.schema.impl.DirectedSchemaEdgeClass;

public class GenericVertexImpl extends VertexImpl {

	private final VertexClass type;
	private Map<String, Object> attributes;
	
	protected GenericVertexImpl(VertexClass type, int id, Graph graph) {
		super(id, graph);
		this.type = type;
		if(type.getAttributeCount() > 0) {
			if(type.getAttributeList().size() > 0) {
				attributes = new HashMap<String, Object>();
				for(Attribute a : type.getAttributeList()) {
					attributes.put(a.getName(), null);
				}
				initializeAttributesWithDefaultValues();
			}
		}
	}
	
	@Override
	public Edge addAdjacence(String role, Vertex other) {
		EdgeClass newEdgeClass = null;
		// TODO optimize!
		for(EdgeClass ec : getSchema().getEdgeClassesInTopologicalOrder()) {
			if(ec.getFrom().getRolename().equals(role) || ec.getTo().getRolename().equals(role)) {
				newEdgeClass = ec;
			}
		}
		if(newEdgeClass.getFrom().getRolename().equals(role)) {
			return getGraph().createEdge(newEdgeClass, this, other);
		}
		else {
			return getGraph().createEdge(newEdgeClass, other, this);
		}
		
	}

	@Override
	public boolean isValidAlpha(Edge edge) {
		return ((GenericGraphImpl) getGraph()).cachedIsValidAlpha(type, ((EdgeClass) edge.getAttributedElementClass()));
	}

	@Override
	public boolean isValidOmega(Edge edge) {
		return ((GenericGraphImpl) getGraph()).cachedIsValidOmega(type, ((EdgeClass) edge.getAttributedElementClass()));
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
				if(!GenericUtil.conformsToDomain(data, type.getAttribute(name).getDomain())) {
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
	public DirectedSchemaEdgeClass getEdgeForRolename(String rolename) {
		// TODO DirectedSchemaEdgeClass is specific for generated Code. Unsupported operation?
		return null;
	}
	
	@Override
	public Vertex getNextVertex(VertexClass vertexClass) {
		InternalVertex currentVertex = (InternalVertex) getNextVertex();
		while(currentVertex != null) {
			if(currentVertex.getAttributedElementClass().equals(vertexClass)) {
				return currentVertex;
			}
			currentVertex = (InternalVertex) currentVertex.getNextVertex();
		}
		return currentVertex;
	}
	
	@Override
	public Edge getFirstIncidence(EdgeClass anEdgeClass) {
		Edge currentEdge = getFirstIncidence();
		while(currentEdge != null) {
			if(currentEdge.getNormalEdge().getAttributedElementClass().equals(anEdgeClass)) {
				return currentEdge;
			}
			currentEdge = currentEdge.getNextIncidence();
		}
		return currentEdge;
	}
	
	@Override
	public Edge getFirstIncidence(EdgeClass anEdgeClass, EdgeDirection orientation) {
		Edge currentEdge = getFirstIncidence(orientation);
		while(currentEdge != null) {
			if(currentEdge.getNormalEdge().getAttributedElementClass().equals(anEdgeClass)) {
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
		for(Edge e = getFirstIncidence(ec, direction); e != null; e = e.getNextIncidence(ec, direction)) {
			++degree;
		}
		return degree;
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
			}
			else {
				setAttribute(attr.getName(), GenericUtil.genericAttributeDefaultValue(attr.getDomain()));
			}
		}
	}
	
	//************** unsupported methods ***************/
	@Override
	public Class<? extends AttributedElement> getSchemaClass() {
		throw new UnsupportedOperationException("This method is not supported by the generic implementation");
	}
	
	public Vertex getNextVertex(Class<? extends Vertex> vertexClass) {
		throw new UnsupportedOperationException("This method is not supported by the generic implementation");
	}
	
	@Override
	public Edge getFirstIncidence(Class<? extends Edge> anEdgeClass) {
		throw new UnsupportedOperationException("This method is not supported by the generic implementation");
	}

	@Override
	public Edge getFirstIncidence(Class<? extends Edge> anEdgeClass,
			EdgeDirection orientation) {
		throw new UnsupportedOperationException("This method is not supported by the generic implementation");
	}
	
	@Override
	public int getDegree(Class<? extends Edge> ec) {
		throw new UnsupportedOperationException("This method is not supported by the generic implementation");
	}
	
	@Override
	public int getDegree(Class<? extends Edge> ec, EdgeDirection direction) {
		throw new UnsupportedOperationException("This method is not supported by the generic implementation");
	}
}
