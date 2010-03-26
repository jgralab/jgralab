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
import de.uni_koblenz.jgralab.greql2.jvalue.JValueImpl;
import de.uni_koblenz.jgralab.greql2.jvalue.JValuePath;
import de.uni_koblenz.jgralab.greql2.jvalue.JValuePathSystem;
import de.uni_koblenz.jgralab.greql2.jvalue.JValueType;
import de.uni_koblenz.jgralab.greql2.jvalue.JValueTypeCollection;

/**
 * @author Tassilo Horn <horn@uni-koblenz.de>
 * 
 */
public abstract class DegreeFunction extends Greql2Function {
	{
		JValueType[][] x = {
				{ JValueType.VERTEX, JValueType.INT },
				{ JValueType.VERTEX, JValueType.TYPECOLLECTION,
						JValueType.INT },
				{ JValueType.VERTEX, JValueType.PATH, JValueType.INT },
				{ JValueType.VERTEX, JValueType.PATHSYSTEM, JValueType.INT },
				{ JValueType.VERTEX, JValueType.PATHSYSTEM,
						JValueType.TYPECOLLECTION, JValueType.INT } };
		signatures = x;

		Category[] c = { Category.GRAPH };
		categories = c;
	}

	public JValueImpl evaluate(BooleanGraphMarker subgraph, JValue[] arguments,
			EdgeDirection direction) throws EvaluateException {
		JValueTypeCollection typeCol = null;
		JValuePathSystem pathSystem = null;
		JValuePath path = null;
		Vertex vertex = null;
		switch (checkArguments(arguments)) {
		case 0:
			break;
		case 1:
			typeCol = (JValueTypeCollection) arguments[1];
			break;
		case 2:
			path = arguments[1].toPath();
			break;
		case 4:
			typeCol = (JValueTypeCollection) arguments[2];
		case 3:
			pathSystem = arguments[1].toPathSystem();
			break;
		default:
			throw new WrongFunctionParameterException(this, arguments);
		}
		vertex = arguments[0].toVertex();

		if ((path == null) && (pathSystem == null)) {
			if (typeCol == null) {
				return new JValueImpl(vertex.getDegree(direction));
			} else {
				Edge inc = vertex.getFirstEdge(direction);
				int count = 0;
				while (inc != null) {
					if (((subgraph == null) || subgraph.isMarked(inc))
							&& typeCol.acceptsType(inc
									.getAttributedElementClass())) {
						count++;
					}
					inc = inc.getNextEdge(direction);
				}
				return new JValueImpl(count);
			}
		}
		if (pathSystem != null) {
			switch (direction) {
			case IN:
				return new JValueImpl(pathSystem.degree(vertex, true, typeCol));
			case OUT:
				return new JValueImpl(pathSystem.degree(vertex, false, typeCol));
			default:
				return new JValueImpl(pathSystem.degree(vertex, typeCol));
			}
		}
		switch (direction) {
		case IN:
			return new JValueImpl(path.degree(vertex, true));
		case OUT:
			return new JValueImpl(path.degree(vertex, false));
		default:
			return new JValueImpl(path.degree(vertex));
		}
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
		return 1;
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

}
