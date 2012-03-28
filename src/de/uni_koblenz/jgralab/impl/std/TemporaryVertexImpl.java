package de.uni_koblenz.jgralab.impl.std;

import java.io.IOException;
import java.util.HashMap;

import de.uni_koblenz.jgralab.Edge;
import de.uni_koblenz.jgralab.EdgeDirection;
import de.uni_koblenz.jgralab.Graph;
import de.uni_koblenz.jgralab.GraphIO;
import de.uni_koblenz.jgralab.GraphIOException;
import de.uni_koblenz.jgralab.NoSuchAttributeException;
import de.uni_koblenz.jgralab.TemporaryVertex;
import de.uni_koblenz.jgralab.Vertex;
import de.uni_koblenz.jgralab.impl.InternalVertex;
import de.uni_koblenz.jgralab.impl.VertexBaseImpl;
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
	public <T> T getAttribute(String name) throws NoSuchAttributeException {
		return (T) this.attributes.get(name);
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
	public Vertex transformToRealGraphElement(VertexClass vc) {
		int id = this.id;
		Graph g = this.graph;
		InternalVertex prevVertex = this.getPrevVertexInVSeq();
		InternalVertex nextVertex = this.getNextVertexInVSeq();
		
		Vertex newVertex = g.createVertex(vc);
		for(String attrName : this.attributes.keySet()){
			if(newVertex.getAttributedElementClass().containsAttribute(attrName)){
				newVertex.setAttribute(attrName, this.attributes.get(attrName));
			}
		}
		
		Edge e = this.getFirstIncidence(EdgeDirection.OUT);	
		while(e != null){
			e.setAlpha(newVertex);
			e = this.getFirstIncidence(EdgeDirection.OUT);
		}
		e = this.getFirstIncidence(EdgeDirection.IN);
		while(e != null){
			e.setOmega(newVertex);
			e = this.getFirstIncidence(EdgeDirection.IN);
		}
		
		InternalVertex newLastVertex = ((InternalVertex)newVertex).getPrevVertexInVSeq();
		
		this.delete();

		if(nextVertex != null){
			prevVertex.setNextVertex(newVertex);
			nextVertex.setPrevVertex(newVertex);
			((InternalVertex)newVertex).setNextVertex(nextVertex);
			((InternalVertex)newVertex).setPrevVertex(prevVertex);
			
			newLastVertex.setNextVertex(null);
		}
		
		((VertexBaseImpl)newVertex).setId(id);
		return newVertex;
	}

	@Override
	public void deleteAttribute(String name) {
		this.attributes.remove(name);
	}

}
