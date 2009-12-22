/**
 *
 */
package de.uni_koblenz.jgralab.greql2.funlib;

import java.util.ArrayList;

import de.uni_koblenz.jgralab.Edge;
import de.uni_koblenz.jgralab.EdgeDirection;
import de.uni_koblenz.jgralab.Vertex;
import de.uni_koblenz.jgralab.graphmarker.BooleanGraphMarker;
import de.uni_koblenz.jgralab.greql2.exception.EvaluateException;
import de.uni_koblenz.jgralab.greql2.exception.WrongFunctionParameterException;
import de.uni_koblenz.jgralab.greql2.jvalue.JValue;
import de.uni_koblenz.jgralab.greql2.jvalue.JValuePath;
import de.uni_koblenz.jgralab.greql2.jvalue.JValuePathSystem;
import de.uni_koblenz.jgralab.greql2.jvalue.JValueSet;
import de.uni_koblenz.jgralab.greql2.jvalue.JValueType;
import de.uni_koblenz.jgralab.greql2.jvalue.JValueTypeCollection;

/**
 * Superclass of {@link EdgesConnected}, {@link EdgesFrom}, and {@link EdgesTo}.
 * 
 * @author Tassilo Horn <horn@uni-koblenz.de>
 * 
 */
public abstract class Incidences extends AbstractGreql2Function {

	{
		JValueType[][] x = { { JValueType.VERTEX },
				{ JValueType.VERTEX, JValueType.PATH },
				{ JValueType.VERTEX, JValueType.PATHSYSTEM },
				{ JValueType.VERTEX, JValueType.TYPECOLLECTION }, };
		signatures = x;
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
		return 2;
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
		return 10;
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

	protected JValue evaluate(BooleanGraphMarker subgraph, JValue[] arguments,
			EdgeDirection direction) throws EvaluateException {
		JValuePath path = null;
		JValuePathSystem pathSystem = null;
		JValueTypeCollection typeCol = null;
		switch (checkArguments(arguments)) {
		case 0:
			break;
		case 1:
			path = arguments[1].toPath();
			break;
		case 2:
			pathSystem = arguments[1].toPathSystem();
			break;
		case 3:
			typeCol = arguments[1].toJValueTypeCollection();
			break;
		default:
			throw new WrongFunctionParameterException(this, arguments);
		}
		Vertex vertex = arguments[0].toVertex();

		if (path != null) {
			if (direction == EdgeDirection.INOUT) {
				if (typeCol == null) {
					return path.edgesConnected(vertex);
				}
			} else {
				return path.edgesConnected(vertex,
						direction == EdgeDirection.IN);
			}
		}
		if (pathSystem != null) {
			if (direction == EdgeDirection.INOUT) {
				return pathSystem.edgesConnected(vertex);
			} else {
				return pathSystem.edgesConnected(vertex,
						direction == EdgeDirection.IN);
			}
		}
		JValueSet resultSet = new JValueSet();
		Edge inc = vertex.getFirstEdge(direction);
		if (typeCol == null) {
			while (inc != null) {
				if ((subgraph == null) || (subgraph.isMarked(inc))) {
					resultSet.add(new JValue(inc));
				}
				inc = inc.getNextEdge(direction);
			}
		} else {
			while (inc != null) {
				if ((subgraph == null) || (subgraph.isMarked(inc))) {
					if (typeCol.acceptsType(inc.getAttributedElementClass())) {
						resultSet.add(new JValue(inc));
					}
				}
				inc = inc.getNextEdge(direction);
			}
		}
		return resultSet;
	}

}
