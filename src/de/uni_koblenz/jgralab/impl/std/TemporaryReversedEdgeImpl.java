package de.uni_koblenz.jgralab.impl.std;

import java.io.IOException;
import java.util.Map;
import java.util.Map.Entry;

import de.uni_koblenz.jgralab.Edge;
import de.uni_koblenz.jgralab.Graph;
import de.uni_koblenz.jgralab.GraphIO;
import de.uni_koblenz.jgralab.TemporaryEdge;
import de.uni_koblenz.jgralab.exception.GraphIOException;
import de.uni_koblenz.jgralab.exception.NoSuchAttributeException;
import de.uni_koblenz.jgralab.impl.EdgeBaseImpl;
import de.uni_koblenz.jgralab.schema.EdgeClass;

public class TemporaryReversedEdgeImpl extends ReversedEdgeImpl implements
		TemporaryEdge {

	protected TemporaryReversedEdgeImpl(EdgeBaseImpl normalEdge, Graph graph) {
		super(normalEdge, graph);
	}

	@Override
	public EdgeClass getAttributedElementClass() {
		return this.graph.getGraphClass().getTemporaryEdgeClass();
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
	public Edge bless(EdgeClass edgeClass) {
		return ((TemporaryEdge) this.getNormalEdge()).bless(edgeClass);
	}

	@Override
	public Edge bless() {
		return ((TemporaryEdge) this.getNormalEdge()).bless();
	}

	@Override
	public void deleteAttribute(String name) {
		((TemporaryEdge) this.getNormalEdge()).deleteAttribute(name);

	}

	@Override
	public boolean isTemporary() {
		return true;
	}

	@Override
	public EdgeClass getPreliminaryType() {
		return ((TemporaryEdge) this.getNormalEdge()).getPreliminaryType();
	}

	@Override
	public void setPreliminaryType(EdgeClass ec) {
		((TemporaryEdge) this.getNormalEdge()).setPreliminaryType(ec);
	}

	@Override
	public final String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("-te");
		sb.append(Math.abs(getId()));
		sb.append(": ");
		if (getPreliminaryType() != null) {
			sb.append(getPreliminaryType().getQualifiedName());
		} else {
			sb.append("-MissingPreliminaryType-");
		}
		sb.append(" {");
		boolean first = true;
		for (Entry<String, Object> e : getAttributes().entrySet()) {
			if (first) {
				first = false;
			} else {
				sb.append(", ");
			}
			sb.append(e.getKey());
			sb.append(" -> ");
			sb.append(e.getValue());
		}
		sb.append("}");
		return sb.toString();
	}

	@Override
	public Map<String, Object> getAttributes() {
		return ((TemporaryEdge) normalEdge).getAttributes();
	}

}
