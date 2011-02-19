/*
 * JGraLab - The Java Graph Laboratory
 * 
 * Copyright (C) 2006-2011 Institute for Software Technology
 *                         University of Koblenz-Landau, Germany
 *                         ist@uni-koblenz.de
 * 
 * For bug reports, documentation and further information, visit
 * 
 *                         http://jgralab.uni-koblenz.de
 * 
 * This program is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as published by the
 * Free Software Foundation; either version 3 of the License, or (at your
 * option) any later version.
 * 
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General
 * Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, see <http://www.gnu.org/licenses>.
 * 
 * Additional permission under GNU GPL version 3 section 7
 * 
 * If you modify this Program, or any covered work, by linking or combining
 * it with Eclipse (or a modified version of that program or an Eclipse
 * plugin), containing parts covered by the terms of the Eclipse Public
 * License (EPL), the licensors of this Program grant you additional
 * permission to convey the resulting work.  Corresponding Source for a
 * non-source form of such a combination shall include the source code for
 * the parts of JGraLab used as well as that of the covered work.
 */
/**
 *
 */
package de.uni_koblenz.jgralab.greql2.funlib;

import java.util.ArrayList;

import de.uni_koblenz.jgralab.AttributedElement;
import de.uni_koblenz.jgralab.Edge;
import de.uni_koblenz.jgralab.EdgeDirection;
import de.uni_koblenz.jgralab.Vertex;
import de.uni_koblenz.jgralab.graphmarker.AbstractGraphMarker;
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
				{ JValueType.VERTEX, JValueType.TYPECOLLECTION, JValueType.INT },
				{ JValueType.VERTEX, JValueType.PATH, JValueType.INT },
				{ JValueType.VERTEX, JValueType.PATHSYSTEM, JValueType.INT },
				{ JValueType.VERTEX, JValueType.PATHSYSTEM,
						JValueType.TYPECOLLECTION, JValueType.INT } };
		signatures = x;

		Category[] c = { Category.GRAPH };
		categories = c;
	}

	public JValueImpl evaluate(AbstractGraphMarker<AttributedElement> subgraph,
			JValue[] arguments, EdgeDirection direction)
			throws EvaluateException {
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
				Edge inc = vertex.getFirstIncidence(direction);
				int count = 0;
				while (inc != null) {
					if (((subgraph == null) || subgraph.isMarked(inc))
							&& typeCol.acceptsType(inc
									.getAttributedElementClass())) {
						count++;
					}
					inc = inc.getNextIncidence(direction);
				}
				return new JValueImpl(count);
			}
		}
		if (pathSystem != null) {
			return new JValueImpl(pathSystem.degree(vertex, direction, typeCol));
		}
		// path
		return new JValueImpl(path.degree(vertex, direction));
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
