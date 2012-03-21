package de.uni_koblenz.jgralab.impl.std;

import java.io.IOException;
import java.util.HashMap;

import de.uni_koblenz.jgralab.Edge;
import de.uni_koblenz.jgralab.Graph;
import de.uni_koblenz.jgralab.GraphIO;
import de.uni_koblenz.jgralab.GraphIOException;
import de.uni_koblenz.jgralab.NoSuchAttributeException;
import de.uni_koblenz.jgralab.TemporaryEdge;
import de.uni_koblenz.jgralab.Vertex;
import de.uni_koblenz.jgralab.impl.ReversedEdgeBaseImpl;
import de.uni_koblenz.jgralab.schema.AggregationKind;
import de.uni_koblenz.jgralab.schema.EdgeClass;

public class TemporaryEdgeImpl extends EdgeImpl implements TemporaryEdge{

	private HashMap<String,Object> attributes;
	
	protected TemporaryEdgeImpl(int anId, Graph graph, Vertex alpha,
			Vertex omega) {
		super(anId, graph, alpha, omega);
		this.attributes = new HashMap<String, Object>();
	}

	@Override
	public EdgeClass getAttributedElementClass() {
		return graph.getGraphClass().getTemporaryEdgeClass();
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
		// do nothing - no attributes
	}

	@Override
	public boolean isInstanceOf(EdgeClass cls) {
		return cls.equals(graph.getGraphClass().getTemporaryEdgeClass());
	}

	@Override
	public EdgeClass transformToRealGraphElement() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void deleteAttribute(String name) {
		((TemporaryEdge)this.getNormalEdge()).deleteAttribute(name);
		
	}

	@Override
	protected ReversedEdgeBaseImpl createReversedEdge() {
		return new TemporaryReversedEdgeImpl(this, graph);
	}
	
	@Override
	public AggregationKind getAlphaAggregationKind() {
		return getAttributedElementClass().getFrom().getAggregationKind();
	}

	@Override
	public AggregationKind getOmegaAggregationKind() {
		return getAttributedElementClass().getTo().getAggregationKind();
	}

	
	//........................................................
	
	

	@Override
	public Class<? extends Edge> getSchemaClass() {
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

	@Override
	public AggregationKind getAggregationKind() {
		AggregationKind fromAK = (getAttributedElementClass()).getFrom()
				.getAggregationKind();
		AggregationKind toAK = (getAttributedElementClass()).getTo()
				.getAggregationKind();
		return fromAK != AggregationKind.NONE ? fromAK
				: (toAK != AggregationKind.NONE ? toAK : AggregationKind.NONE);
	}
}
