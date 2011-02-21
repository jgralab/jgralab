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

package de.uni_koblenz.jgralab.greql2.evaluator.vertexeval;

import de.uni_koblenz.jgralab.greql2.evaluator.GreqlEvaluator;
import de.uni_koblenz.jgralab.greql2.evaluator.costmodel.GraphSize;
import de.uni_koblenz.jgralab.greql2.evaluator.costmodel.VertexCosts;
import de.uni_koblenz.jgralab.greql2.exception.EvaluateException;
import de.uni_koblenz.jgralab.greql2.jvalue.JValue;
import de.uni_koblenz.jgralab.greql2.jvalue.JValueBoolean;
import de.uni_koblenz.jgralab.greql2.schema.BoolLiteral;
import de.uni_koblenz.jgralab.greql2.schema.Greql2Vertex;

/**
 * Evaluates a boolean literal, that means, provides access to the literal value
 * using the getResult(...)-Method. This is needed, because is should make no
 * difference for the other VertexEvaluators, if a value is the result of a
 * maybe complex evaluation or if it is a literal.
 * 
 * @author ist@uni-koblenz.de
 * 
 */
public class BoolLiteralEvaluator extends VertexEvaluator {

	/**
	 * The BoolLiteral this evluator provides acces to
	 */
	private BoolLiteral vertex;

	/**
	 * returns the vertex this VertexEvaluator evaluates
	 */
	@Override
	public Greql2Vertex getVertex() {
		return vertex;
	}

	public BoolLiteralEvaluator(BoolLiteral vertex, GreqlEvaluator eval) {
		super(eval);
		this.vertex = vertex;
	}

	@Override
	public JValue evaluate() throws EvaluateException {
		if (vertex.is_boolValue()) {
			return JValueBoolean.trueJValue;
		} else {
			return JValueBoolean.falseJValue;
		}
	}

	@Override
	public VertexCosts calculateSubtreeEvaluationCosts(GraphSize graphSize) {
		return new VertexCosts(1, 1, 1);
	}

	@Override
	public double calculateEstimatedSelectivity(GraphSize graphSize) {
		// true has selectivity 1, but false and null can never be true, so
		// their selectivity is 0.
		if (vertex.is_boolValue()) {
			return 1;
		}
		return 0;
	}
}
