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

package de.uni_koblenz.jgralab.greql2.evaluator.vertexeval;

import java.util.ArrayList;
import java.util.Map;

import de.uni_koblenz.jgralab.EdgeDirection;
import de.uni_koblenz.jgralab.greql2.evaluator.GreqlEvaluator;
import de.uni_koblenz.jgralab.greql2.evaluator.costmodel.GraphSize;
import de.uni_koblenz.jgralab.greql2.evaluator.costmodel.VertexCosts;
import de.uni_koblenz.jgralab.greql2.exception.EvaluateException;
import de.uni_koblenz.jgralab.greql2.exception.UndefinedVariableException;
import de.uni_koblenz.jgralab.greql2.exception.UnknownTypeException;
import de.uni_koblenz.jgralab.greql2.jvalue.JValue;
import de.uni_koblenz.jgralab.greql2.schema.Expression;
import de.uni_koblenz.jgralab.greql2.schema.Greql2Expression;
import de.uni_koblenz.jgralab.greql2.schema.Greql2Vertex;
import de.uni_koblenz.jgralab.greql2.schema.IsBoundVarOf;
import de.uni_koblenz.jgralab.greql2.schema.IsIdOf;
import de.uni_koblenz.jgralab.greql2.schema.SourcePosition;
import de.uni_koblenz.jgralab.greql2.schema.Variable;
import de.uni_koblenz.jgralab.schema.AttributedElementClass;
import de.uni_koblenz.jgralab.schema.EdgeClass;
import de.uni_koblenz.jgralab.schema.Package;
import de.uni_koblenz.jgralab.schema.Schema;
import de.uni_koblenz.jgralab.schema.VertexClass;

/**
 * Evaluates a Greql2Expression vertex in the GReQL-2 Syntaxgraph. A
 * GReQL2-Expression is the rootvertex of the GReQL-2Syntaxgraph. It contains
 * the bound/free variables, that are defined via "using" and binds them to the
 * values in the variableMap of the Greql2Evaluator.
 * 
 * @author ist@uni-koblenz.de
 * 
 */
public class Greql2ExpressionEvaluator extends VertexEvaluator {

	/**
	 * The Greql2Expression-Vertex this evaluator evaluates
	 */
	private Greql2Expression vertex;

	/**
	 * returns the vertex this VertexEvaluator evaluates
	 */
	@Override
	public Greql2Vertex getVertex() {
		return vertex;
	}

	/**
	 * The varibles that are defined via the <code>using</code> clause. They are
	 * called bound or also free variables
	 */
	private Map<String, JValue> boundVariables;

	/**
	 * @param eval
	 *            the GreqlEvaluator this VertexEvaluator belongs to
	 * @param vertex
	 *            the vertex which gets evaluated by this VertexEvaluator
	 */
	public Greql2ExpressionEvaluator(Greql2Expression vertex,
			GreqlEvaluator eval) {
		super(eval);
		this.vertex = vertex;
		this.boundVariables = eval.getVariables();
	}

	/**
	 * sets the values of all bound variables and evaluates the queryexpression
	 */
	@Override
	public JValue evaluate() throws EvaluateException {
		if (vertex.get_importedTypes() != null) {
			Schema graphSchema = graph.getSchema();
			for (String importedType : vertex.get_importedTypes()) {
				if (importedType.endsWith(".*")) {
					String packageName = importedType.substring(0, importedType
							.length() - 2);
					Package p = graphSchema.getPackage(packageName);
					if (p == null) {
						throw new UnknownTypeException(packageName,
								new ArrayList<SourcePosition>());
					}
					// for (Domain elem : p.getDomains().values()) {
					// greqlEvaluator.addKnownType(elem);
					// }
					for (VertexClass elem : p.getVertexClasses().values()) {
						greqlEvaluator.addKnownType(elem);
					}
					for (EdgeClass elem : p.getEdgeClasses().values()) {
						greqlEvaluator.addKnownType(elem);
					}
				} else {
					AttributedElementClass elemClass = graphSchema
							.getAttributedElementClass(importedType);
					if (elemClass == null) {
						throw new UnknownTypeException(importedType,
								new ArrayList<SourcePosition>());
					}
					greqlEvaluator.addKnownType(elemClass);
				}
			}
		}
		IsBoundVarOf inc = vertex.getFirstIsBoundVarOf(EdgeDirection.IN);
		while (inc != null) {
			Variable currentBoundVariable = (Variable) inc.getAlpha();
			JValue variableValue = boundVariables.get(currentBoundVariable
					.get_name());
			if (variableValue == null) {
				throw new UndefinedVariableException(currentBoundVariable,
						createSourcePositions(inc));
			}
			VariableEvaluator variableEval = (VariableEvaluator) greqlEvaluator
					.getVertexEvaluatorGraphMarker().getMark(
							currentBoundVariable);
			variableEval.setValue(variableValue);
			inc = inc.getNextIsBoundVarOf(EdgeDirection.IN);
		}
		Expression boundExpression = (Expression) vertex.getFirstIsQueryExprOf(
				EdgeDirection.IN).getAlpha();
		VertexEvaluator eval = greqlEvaluator.getVertexEvaluatorGraphMarker()
				.getMark(boundExpression);
		JValue result = eval.getResult(subgraph);
		// if the query contains a "store as " - clause, there is a
		// "isIdOfInc"-Incidence connected with the Greql2Expression
		IsIdOf storeInc = vertex.getFirstIsIdOf(EdgeDirection.IN);
		if (storeInc != null) {
			VertexEvaluator storeEval = greqlEvaluator
					.getVertexEvaluatorGraphMarker().getMark(
							storeInc.getAlpha());
			String varName = storeEval.getResult(null).toString();
			boundVariables.put(varName, result);
		}
		return result;
	}

	@Override
	public VertexCosts calculateSubtreeEvaluationCosts(GraphSize graphSize) {
		return this.greqlEvaluator.getCostModel()
				.calculateCostsGreql2Expression(this, graphSize);
	}

}
