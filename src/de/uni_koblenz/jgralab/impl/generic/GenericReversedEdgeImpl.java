package de.uni_koblenz.jgralab.impl.generic;


import java.io.IOException;

import de.uni_koblenz.jgralab.Graph;
import de.uni_koblenz.jgralab.GraphIO;
import de.uni_koblenz.jgralab.GraphIOException;
import de.uni_koblenz.jgralab.NoSuchAttributeException;
import de.uni_koblenz.jgralab.impl.std.ReversedEdgeImpl;
import de.uni_koblenz.jgralab.schema.AttributedElementClass;

public class GenericReversedEdgeImpl extends ReversedEdgeImpl {

	protected GenericReversedEdgeImpl(GenericEdgeImpl e, Graph g) {
		super(e, g);
	}

	@Override
	public AttributedElementClass getAttributedElementClass() {
		return normalEdge.getAttributedElementClass();
	}

	@Override
	public void readAttributeValueFromString(String attributeName, String value)
			throws GraphIOException, NoSuchAttributeException {
		throw new GraphIOException("Can not call readAttributeValuesFromString for reversed Edges.");
		
	}

	@Override
	public String writeAttributeValueToString(String attributeName)
			throws IOException, GraphIOException, NoSuchAttributeException {
		throw new GraphIOException("Can not call readAttributeValuesFromString for reversed Edges.");
	}

	@Override
	public void writeAttributeValues(GraphIO io) throws IOException,
			GraphIOException {
		throw new GraphIOException("Can not call readAttributeValuesFromString for reversed Edges.");
		
	}

	@Override
	public void readAttributeValues(GraphIO io) throws GraphIOException {
		throw new GraphIOException("Can not call readAttributeValuesFromString for reversed Edges.");		
	}

}
