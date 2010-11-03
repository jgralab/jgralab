/*
 * JGraLab - The Java Graph Laboratory
 * 
 * Copyright (C) 2006-2010 Institute for Software Technology
 *                         University of Koblenz-Landau, Germany
 *                         ist@uni-koblenz.de
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
import de.uni_koblenz.jgralab.greql2.jvalue.JValueSet;
import de.uni_koblenz.jgralab.greql2.jvalue.JValueType;
import de.uni_koblenz.jgralab.greql2.jvalue.JValueTypeCollection;

/**
 * Superclass of {@link EdgesConnected}, {@link EdgesFrom}, and {@link EdgesTo}.
 * 
 * @author Tassilo Horn <horn@uni-koblenz.de>
 * 
 */
public abstract class Incidences extends Greql2Function {

	{
		JValueType[][] x = {
				{ JValueType.VERTEX, JValueType.COLLECTION },
				{ JValueType.VERTEX, JValueType.PATH, JValueType.COLLECTION },
				{ JValueType.VERTEX, JValueType.PATHSYSTEM,
						JValueType.COLLECTION },
				{ JValueType.VERTEX, JValueType.TYPECOLLECTION,
						JValueType.COLLECTION }, };
		signatures = x;

		Category[] c = { Category.GRAPH,
				Category.PATHS_AND_PATHSYSTEMS_AND_SLICES };
		categories = c;
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

	protected JValueImpl evaluate(
			AbstractGraphMarker<AttributedElement> subgraph,
			JValue[] arguments, EdgeDirection direction)
			throws EvaluateException {
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
			return path.edgesConnected(vertex, direction);
		}
		if (pathSystem != null) {
			return pathSystem.edgesConnected(vertex, direction);
		}
		JValueSet resultSet = new JValueSet();
		Edge inc = vertex.getFirstEdge(direction);
		if (typeCol == null) {
			while (inc != null) {
				if ((subgraph == null) || (subgraph.isMarked(inc))) {
					resultSet.add(new JValueImpl(inc));
				}
				inc = inc.getNextEdge(direction);
			}
		} else {
			while (inc != null) {
				if ((subgraph == null) || (subgraph.isMarked(inc))) {
					if (typeCol.acceptsType(inc.getAttributedElementClass())) {
						resultSet.add(new JValueImpl(inc));
					}
				}
				inc = inc.getNextEdge(direction);
			}
		}
		return resultSet;
	}

}
