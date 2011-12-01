package de.uni_koblenz.jgralab.impl.generic;


import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import de.uni_koblenz.jgralab.AttributedElement;
import de.uni_koblenz.jgralab.Edge;
import de.uni_koblenz.jgralab.Graph;
import de.uni_koblenz.jgralab.GraphIO;
import de.uni_koblenz.jgralab.GraphIOException;
import de.uni_koblenz.jgralab.NoSuchAttributeException;
import de.uni_koblenz.jgralab.impl.std.VertexImpl;
import de.uni_koblenz.jgralab.schema.*;
import de.uni_koblenz.jgralab.schema.impl.DirectedSchemaEdgeClass;

public class GenericVertexImpl extends VertexImpl {

	private final VertexClass type;
	private Map<String, Object> attributes;
	
	protected GenericVertexImpl(VertexClass type, int id, Graph graph) {
		super(id, graph);
		this.type = type;
		attributes = new HashMap<String, Object>();
		for(Attribute a : type.getAttributeList()) {
			attributes.put(a.getName(), null);
		}
		initializeAttributesWithDefaultValues();
	}

	@Override
	public boolean isValidAlpha(Edge edge) {
		// TODO optimieren!
		if(type.getAllOutIncidenceClasses().contains(((EdgeClass) edge.getAttributedElementClass()).getFrom())) {
			return true;
		}
		else {
			return false;
		}
	}

	@Override
	public boolean isValidOmega(Edge edge) {
		// TODO optimieren!
		if(type.getAllInIncidenceClasses().contains(((EdgeClass) edge.getAttributedElementClass()).getTo())) {
			return true;
		}
		else {
			return false;
		}
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
	public void readAttributeValues(GraphIO io) throws GraphIOException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public String writeAttributeValueToString(String attributeName)
			throws IOException, GraphIOException, NoSuchAttributeException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void writeAttributeValues(GraphIO io) throws IOException,
			GraphIOException {
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
	public DirectedSchemaEdgeClass getEdgeForRolename(String rolename) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Class<? extends AttributedElement> getSchemaClass() {
		throw new UnsupportedOperationException("getSchemaClass is not supported by the generic implementation");
	}

}
