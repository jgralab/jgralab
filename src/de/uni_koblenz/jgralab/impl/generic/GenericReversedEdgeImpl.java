package de.uni_koblenz.jgralab.impl.generic;

import java.io.IOException;

import de.uni_koblenz.jgralab.Edge;
import de.uni_koblenz.jgralab.EdgeDirection;
import de.uni_koblenz.jgralab.Graph;
import de.uni_koblenz.jgralab.GraphIO;
import de.uni_koblenz.jgralab.GraphIOException;
import de.uni_koblenz.jgralab.NoSuchAttributeException;
import de.uni_koblenz.jgralab.impl.std.ReversedEdgeImpl;
import de.uni_koblenz.jgralab.schema.AttributedElementClass;
import de.uni_koblenz.jgralab.schema.EdgeClass;

public class GenericReversedEdgeImpl extends ReversedEdgeImpl {

	protected GenericReversedEdgeImpl(GenericEdgeImpl e, Graph g) {
		super(e, g);
	}

	@Override
	public EdgeClass getAttributedElementClass() {
		return normalEdge.getAttributedElementClass();
	}

	@Override
	public void readAttributeValueFromString(String attributeName, String value)
			throws GraphIOException, NoSuchAttributeException {
		throw new GraphIOException(
				"Can not call readAttributeValuesFromString for reversed Edges.");
	}

	@Override
	public Edge getNextIncidence(EdgeClass anEdgeClass) {
		return getNextIncidence(anEdgeClass, EdgeDirection.INOUT, false);
	}

	@Override
	public Edge getNextIncidence(EdgeClass anEdgeClass,
			EdgeDirection orientation) {
		return getNextIncidence(anEdgeClass, orientation, false);
	}

	@Override
	public Edge getNextIncidence(EdgeClass anEdgeClass,
			boolean noSubClasses) {
		return getNextIncidence(anEdgeClass, EdgeDirection.INOUT, noSubClasses);
	}

	@Override
	public Edge getNextIncidence(EdgeClass anEdgeClass,
			EdgeDirection orientation, boolean noSubclasses) {
		Edge currentEdge = getNextIncidence(orientation);
		while (currentEdge != null) {
			if (noSubclasses) {
				if (anEdgeClass.equals(currentEdge.getAttributedElementClass())) {
					return currentEdge;
				}
			} else {
				if (anEdgeClass.equals(currentEdge.getAttributedElementClass())
						|| anEdgeClass.getAllSubClasses().contains(
								currentEdge.getAttributedElementClass())) {
					return currentEdge;
				}
			}
			currentEdge = currentEdge.getNextIncidence(orientation);
		}
		return null;
	}

	@Override
	public String writeAttributeValueToString(String attributeName)
			throws IOException, GraphIOException, NoSuchAttributeException {
		throw new GraphIOException(
				"Can not call readAttributeValuesFromString for reversed Edges.");
	}

	@Override
	public void writeAttributeValues(GraphIO io) throws IOException,
			GraphIOException {
		throw new GraphIOException(
				"Can not call readAttributeValuesFromString for reversed Edges.");

	}

	@Override
	public void readAttributeValues(GraphIO io) throws GraphIOException {
		throw new GraphIOException(
				"Can not call readAttributeValuesFromString for reversed Edges.");
	}

	@Override
	public Edge getNextIncidence(Class<? extends Edge> anEdgeClass) {
		throw new UnsupportedOperationException(
				"Can not call readAttributeValuesFromString for reversed Edges.");
	}

	@Override
	public Edge getNextIncidence(Class<? extends Edge> anEdgeClass,
			EdgeDirection orientation) {
		throw new UnsupportedOperationException(
				"Can not call readAttributeValuesFromString for reversed Edges.");
	}

	@Override
	public Edge getNextIncidence(Class<? extends Edge> anEdgeClass,
			EdgeDirection orientation, boolean noSubclasses) {
		throw new UnsupportedOperationException(
				"Can not call readAttributeValuesFromString for reversed Edges.");
	}

	@Override
	public boolean isInstanceOf(AttributedElementClass cls) {
		// Needs to be overridden from the base variant, because that relies on
		// code generation.
		return getNormalEdge().isInstanceOf(cls);
	}
}
