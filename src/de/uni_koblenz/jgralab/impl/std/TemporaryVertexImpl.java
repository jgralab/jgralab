package de.uni_koblenz.jgralab.impl.std;

import java.io.IOException;
import java.util.HashMap;

import de.uni_koblenz.jgralab.Graph;
import de.uni_koblenz.jgralab.GraphIO;
import de.uni_koblenz.jgralab.GraphIOException;
import de.uni_koblenz.jgralab.NoSuchAttributeException;
import de.uni_koblenz.jgralab.TemporaryVertex;
import de.uni_koblenz.jgralab.Vertex;
import de.uni_koblenz.jgralab.schema.VertexClass;

public class TemporaryVertexImpl extends VertexImpl implements TemporaryVertex {

	private HashMap<String,Object> attributes;
	
	protected TemporaryVertexImpl(int id, Graph graph) {
		super(id, graph);
		this.attributes = new HashMap<String, Object>();
	}

	@Override
	public VertexClass getAttributedElementClass() {
		return this.graph.getGraphClass().getTemporaryVertexClass();
	}

	@Override
	public Class<? extends Vertex> getSchemaClass() {
		throw new UnsupportedOperationException();
	}


	@Override
	public void readAttributeValueFromString(String attributeName, String value)
			throws GraphIOException, NoSuchAttributeException {
		throw new UnsupportedOperationException();
	}

	@Override
	public String writeAttributeValueToString(String attributeName)
			throws IOException, GraphIOException, NoSuchAttributeException {
		throw new UnsupportedOperationException();
	}

	@Override
	public void writeAttributeValues(GraphIO io) throws IOException,
			GraphIOException {
		throw new UnsupportedOperationException();
	}

	@Override
	public void readAttributeValues(GraphIO io) throws GraphIOException {
		throw new UnsupportedOperationException();
	}

	@SuppressWarnings("unchecked")
	@Override
	public Object getAttribute(String name) throws NoSuchAttributeException {
		return this.attributes.get(name);
	}

	@Override
	public <T> void setAttribute(String name, T data)
			throws NoSuchAttributeException {
		this.attributes.put(name, data);
		
	}

	@Override
	public void initializeAttributesWithDefaultValues() {
		// TODO Auto-generated method stub
		// do nothing
	}

	@Override
	public boolean isInstanceOf(VertexClass cls) {
		return cls.equals(this.graph.getGraphClass().getTemporaryVertexClass());
	}

	@Override
	public VertexClass transformToRealGraphElement() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void deleteAttribute(String name) {
		this.attributes.remove(name);
	}

}
