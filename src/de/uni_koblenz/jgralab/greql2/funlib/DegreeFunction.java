/**
 *
 */
package de.uni_koblenz.jgralab.greql2.funlib;

import java.util.ArrayList;

import de.uni_koblenz.jgralab.BooleanGraphMarker;
import de.uni_koblenz.jgralab.Edge;
import de.uni_koblenz.jgralab.EdgeDirection;
import de.uni_koblenz.jgralab.Vertex;
import de.uni_koblenz.jgralab.greql2.exception.EvaluateException;
import de.uni_koblenz.jgralab.greql2.exception.WrongFunctionParameterException;
import de.uni_koblenz.jgralab.greql2.jvalue.JValue;
import de.uni_koblenz.jgralab.greql2.jvalue.JValuePath;
import de.uni_koblenz.jgralab.greql2.jvalue.JValuePathSystem;
import de.uni_koblenz.jgralab.greql2.jvalue.JValueType;
import de.uni_koblenz.jgralab.greql2.jvalue.JValueTypeCollection;

/**
 * @author Tassilo Horn <horn@uni-koblenz.de>
 * 
 */
public abstract class DegreeFunction extends AbstractGreql2Function {
	{
		JValueType[][] x = {
				{ JValueType.VERTEX },
				{ JValueType.VERTEX, JValueType.TYPECOLLECTION },
				{ JValueType.VERTEX, JValueType.PATH },
				{ JValueType.VERTEX, JValueType.PATHSYSTEM },
				{ JValueType.VERTEX, JValueType.PATHSYSTEM,
						JValueType.TYPECOLLECTION } };
		signatures = x;
	}

	public JValue evaluate(BooleanGraphMarker subgraph, JValue[] arguments,
			EdgeDirection direction) throws EvaluateException {
		JValueTypeCollection typeCol = null;
		JValuePathSystem pathSystem = null;
		JValuePath path = null;
		Vertex vertex = null;
		switch (checkArguments(arguments)) {
		case 0:
			break;
		case 1:
			typeCol = arguments[1].toJValueTypeCollection();
			break;
		case 2:
			path = arguments[1].toPath();
			break;
		case 4:
			typeCol = arguments[2].toJValueTypeCollection();
		case 3:
			pathSystem = arguments[1].toPathSystem();
			break;
		default:
			throw new WrongFunctionParameterException(this, null, arguments);
		}
		vertex = arguments[0].toVertex();

		if (path == null && pathSystem == null) {
			if (typeCol == null) {
				return new JValue(vertex.getDegree(direction));
			} else {
				Edge inc = vertex.getFirstEdge(direction);
				int count = 0;
				while (inc != null) {
					if ((subgraph == null || subgraph.isMarked(inc))
							&& typeCol.acceptsType(inc
									.getAttributedElementClass())) {
						count++;
					}
					inc = inc.getNextEdge(direction);
				}
				return new JValue(count);
			}
		}
		if (pathSystem != null) {
			switch (direction) {
			case IN:
				return new JValue(pathSystem.degree(vertex, true, typeCol));
			case OUT:
				return new JValue(pathSystem.degree(vertex, false, typeCol));
			default:
				return new JValue(pathSystem.degree(vertex, typeCol));
			}
		}
		switch (direction) {
		case IN:
			return new JValue(path.degree(vertex, true));
		case OUT:
			return new JValue(path.degree(vertex, false));
		default:
			return new JValue(path.degree(vertex));
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
