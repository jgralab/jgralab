package de.uni_koblenz.jgralab.gretl;

import java.util.LinkedList;
import java.util.List;

import de.uni_koblenz.jgralab.Edge;
import de.uni_koblenz.jgralab.EdgeBase;
import de.uni_koblenz.jgralab.Vertex;
import de.uni_koblenz.jgralab.VertexBase;

/**
 * @author Tassilo Horn &lt;horn@uni-koblenz.de&gt;
 * 
 */
public abstract class InPlaceTransformation extends CountingTransformation {
	protected InPlaceTransformation(Context context) {
		super(context);
		assertInPlace();
	}

	private void assertInPlace() {
		if (context.getSourceGraph() != context.getTargetGraph()) {
			throw new GReTLException(context, "In-place transformation "
					+ getClass().getSimpleName()
					+ " executed in non-in-place context!");
		}
	}

	/**
	 * Relink all incidences of oldVertex to newVertex.
	 * 
	 * @param oldVertex
	 * @param newVertex
	 */
	protected void relinkIncidences(Vertex oldVertex, Vertex newVertex) {
		if (oldVertex == newVertex) {
			return;
		}
		List<EdgeBase> incs = new LinkedList<EdgeBase>();
		for (Edge inc : oldVertex.incidences()) {
			incs.add((EdgeBase) inc);
		}
		for (EdgeBase inc : incs) {
			inc.setThis((VertexBase) newVertex);
		}
	}
}