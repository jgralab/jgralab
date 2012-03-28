package de.uni_koblenz.jgralab.impl.std;

import java.io.IOException;
import java.util.HashMap;

import de.uni_koblenz.jgralab.Edge;
import de.uni_koblenz.jgralab.EdgeDirection;
import de.uni_koblenz.jgralab.Graph;
import de.uni_koblenz.jgralab.GraphException;
import de.uni_koblenz.jgralab.GraphIO;
import de.uni_koblenz.jgralab.GraphIOException;
import de.uni_koblenz.jgralab.NoSuchAttributeException;
import de.uni_koblenz.jgralab.TemporaryVertex;
import de.uni_koblenz.jgralab.Vertex;
import de.uni_koblenz.jgralab.impl.InternalGraph;
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
		
		//Test if vc is valid
		// TODO maybe faster if iterate over all incidences at once and checking direction every time
		for(Edge e : this.incidences(EdgeDirection.OUT)){
			if(!vc.isValidFromFor(e.getAttributedElementClass())){
				throw new GraphException("Transformation of temporary vertex "+this+ " failed. "
						+ vc +" is not a valid source for edge "+ e +".");
			}
		}
		
		for(Edge e : this.incidences(EdgeDirection.IN)){
			if(!vc.isValidToFor(e.getAttributedElementClass())){
				throw new GraphException("Transformation of temporary vertex "+this+ " failed. "
						+ vc +" is not a valid target for edge "+ e +".");
			}
		}
		
		// save properties
		int id = this.id;
		Graph g = this.graph;
		InternalVertex prevVertex = this.getPrevVertexInVSeq();
		InternalVertex nextVertex = this.getNextVertexInVSeq();
		
		// create new vertex 
		Vertex newVertex = g.createVertex(vc);
		
		// attributes
		for(String attrName : this.attributes.keySet()){
			if(newVertex.getAttributedElementClass().containsAttribute(attrName)){
				newVertex.setAttribute(attrName, this.attributes.get(attrName));
			}
		}
		
		// incidences
		Edge e = this.getFirstIncidence();	
		while(e != null){
			e.setThis(newVertex);
			e = this.getFirstIncidence();
		}
		
		InternalVertex newLastVertex = ((InternalVertex)newVertex).getPrevVertexInVSeq();
		
		this.delete();

		if(nextVertex != null){
			nextVertex.setPrevVertex(newVertex);
			((InternalVertex)newVertex).setNextVertex(nextVertex);
			((InternalVertex)newVertex).setPrevVertex(prevVertex);
			
			newLastVertex.setNextVertex(null);
			
			if(prevVertex != null){
				prevVertex.setNextVertex(newVertex);
			}else{// Temporary Vertex is first Vertex in graph
				((InternalGraph)g).setFirstVertex((InternalVertex) newVertex);
			}

			((InternalGraph)g).setLastVertex(newLastVertex);
		}
		
		((VertexBaseImpl)newVertex).setId(id);
		return newVertex;
	}

	@Override
	public void deleteAttribute(String name) {
		this.attributes.remove(name);
	}

}
