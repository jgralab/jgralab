/**
 *
 */
package de.uni_koblenz.jgralab.greql2.funlib;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Queue;

import de.uni_koblenz.jgralab.BooleanGraphMarker;
import de.uni_koblenz.jgralab.Edge;
import de.uni_koblenz.jgralab.EdgeDirection;
import de.uni_koblenz.jgralab.Graph;
import de.uni_koblenz.jgralab.GraphMarker;
import de.uni_koblenz.jgralab.Vertex;
import de.uni_koblenz.jgralab.greql2.exception.EvaluateException;
import de.uni_koblenz.jgralab.greql2.exception.WrongFunctionParameterException;
import de.uni_koblenz.jgralab.greql2.jvalue.JValue;
import de.uni_koblenz.jgralab.greql2.jvalue.JValueList;
import de.uni_koblenz.jgralab.greql2.jvalue.JValueType;

/**
 * Returns a list of vertices in topological ordering.
 * 
 * <dl>
 * <dt><b>GReQL-signature</b></dt>
 * <dd><code>LIST topologicalSort()</code></dd>
 * <dd><code>LIST topologicalSort(subgraph : SubgraphTempAttribute)</code></dd>
 * <dd>&nbsp;</dd>
 * </dl>
 * <dl>
 * <dt></dt>
 * <dd>
 * <dl>
 * <dt><b>Parameters:</b></dt>
 * <dd><code>subgraph</code> - the subgraph to be sorted (optional)</dd>
 * <dt><b>Returns:</b></dt>
 * <dd>a list of vertices in topological ordering or null, if there's no
 * topological order meaning the graph has cycles</dd>
 * </dl>
 * </dd>
 * </dl>
 * 
 * @author ist@uni-koblenz.de
 * 
 */
public class TopologicalSort extends AbstractGreql2Function {

	{
		JValueType[][] x = { {}, { JValueType.SUBGRAPHTEMPATTRIBUTE } };
		signatures = x;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * de.uni_koblenz.jgralab.greql2.funlib.Greql2Function#evaluate(de.uni_koblenz
	 * .jgralab.Graph, de.uni_koblenz.jgralab.BooleanGraphMarker,
	 * de.uni_koblenz.jgralab.greql2.jvalue.JValue[])
	 */
	@Override
	public JValue evaluate(Graph graph, BooleanGraphMarker subgraph,
			JValue[] arguments) throws EvaluateException {
		switch (checkArguments(arguments)) {
		case 0:
			break;
		case 1:
			subgraph = arguments[0].toSubgraphTempAttribute();
			break;
		default:
			throw new WrongFunctionParameterException(this, null, arguments);
		}

		JValueList result = new JValueList();

		Queue<Vertex> queue = new ArrayDeque<Vertex>();
		GraphMarker<Integer> marker = new GraphMarker<Integer>(graph);
		int vCount = 0;
		for (Vertex v : graph.vertices()) {
			if (subgraph == null || subgraph.isMarked(v)) {
				int inDegree = 0;
				for (Edge inc : v.incidences(EdgeDirection.IN)) {
					if (subgraph == null || subgraph.isMarked(inc)) {
						inDegree++;
					}
				}
				marker.mark(v, inDegree);
				if (inDegree == 0) {
					queue.offer(v);
					result.add(new JValue(v));
				}
				vCount++;
			}
		}

		while (!queue.isEmpty()) {
			Vertex v = queue.poll();
			vCount--;
			for (Edge inc : v.incidences(EdgeDirection.OUT)) {
				if (subgraph == null || subgraph.isMarked(inc)) {
					Vertex omega = inc.getOmega();
					assert subgraph == null || subgraph.isMarked(omega);
					int decVal = marker.getMark(omega) - 1;
					marker.mark(omega, decVal);
					if (decVal == 0) {
						queue.offer(omega);
						result.add(new JValue(omega));
					}
				}
			}
		}

		if (vCount == 0) {
			return result;
		}
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * de.uni_koblenz.jgralab.greql2.funlib.Greql2Function#getEstimatedCardinality
	 * (int)
	 */
	@Override
	public long getEstimatedCardinality(int inElements) {
		return 1000;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * de.uni_koblenz.jgralab.greql2.funlib.Greql2Function#getEstimatedCosts
	 * (java.util.ArrayList)
	 */
	@Override
	public long getEstimatedCosts(ArrayList<Long> inElements) {
		return 200;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.uni_koblenz.jgralab.greql2.funlib.Greql2Function#getSelectivity()
	 */
	@Override
	public double getSelectivity() {
		return 1;
	}

}
