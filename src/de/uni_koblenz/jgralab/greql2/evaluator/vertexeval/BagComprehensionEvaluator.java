/*
 * JGraLab - The Java graph laboratory
 * (c) 2006-2009 Institute for Software Technology
 *               University of Koblenz-Landau, Germany
 *
 *               ist@uni-koblenz.de
 *
 * Please report bugs to http://serres.uni-koblenz.de/bugzilla
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */

package de.uni_koblenz.jgralab.greql2.evaluator.vertexeval;

import de.uni_koblenz.jgralab.EdgeDirection;
import de.uni_koblenz.jgralab.Vertex;
import de.uni_koblenz.jgralab.greql2.evaluator.GreqlEvaluator;
import de.uni_koblenz.jgralab.greql2.evaluator.VariableDeclarationLayer;
import de.uni_koblenz.jgralab.greql2.evaluator.costmodel.GraphSize;
import de.uni_koblenz.jgralab.greql2.evaluator.costmodel.VertexCosts;
import de.uni_koblenz.jgralab.greql2.exception.EvaluateException;
import de.uni_koblenz.jgralab.greql2.exception.JValueInvalidTypeException;
import de.uni_koblenz.jgralab.greql2.jvalue.JValue;
import de.uni_koblenz.jgralab.greql2.jvalue.JValueBag;
import de.uni_koblenz.jgralab.greql2.jvalue.JValueCollection;
import de.uni_koblenz.jgralab.greql2.jvalue.JValueTable;
import de.uni_koblenz.jgralab.greql2.jvalue.JValueTuple;
import de.uni_koblenz.jgralab.greql2.schema.BagComprehension;
import de.uni_koblenz.jgralab.greql2.schema.Declaration;
import de.uni_koblenz.jgralab.greql2.schema.Expression;
import de.uni_koblenz.jgralab.greql2.schema.IsTableHeaderOf;

/**
 * Evaluates a BagComprehensionvertex in the GReQL-2 Syntaxgraph
 * 
 * @author ist@uni-koblenz.de
 * 
 */
public class BagComprehensionEvaluator extends VertexEvaluator {

	/**
	 * The BagComprehension-Vertex this evaluator evaluates
	 */
	private BagComprehension vertex;

	/**
	 * returns the vertex this VertexEvaluator evaluates
	 */
	public Vertex getVertex() {
		return vertex;
	}

	/**
	 * Creates a new BagComprehensionEvaluator for the given vertex
	 * 
	 * @param eval
	 *            the GreqlEvaluator instance this VertexEvaluator belong to
	 * @param vertex
	 *            the vertex this VertexEvaluator evaluates
	 */
	public BagComprehensionEvaluator(BagComprehension vertex,
			GreqlEvaluator eval) {
		super(eval);
		this.vertex = vertex;
	}

	@Override
	public JValue evaluate() throws EvaluateException {
		Declaration d = (Declaration) vertex.getFirstIsCompDeclOf(
				EdgeDirection.IN).getAlpha();
		DeclarationEvaluator declEval = (DeclarationEvaluator) greqlEvaluator
				.getVertexEvaluatorGraphMarker().getMark(d);
		VariableDeclarationLayer declLayer = null;
		try {
			declLayer = declEval.getResult(subgraph).toDeclarationLayer();
		} catch (JValueInvalidTypeException exception) {
			throw new EvaluateException("Error evaluating BagComprehension",
					exception);
		}
		Expression resultDef = (Expression) vertex.getFirstIsCompResultDefOf(
				EdgeDirection.IN).getAlpha();
		VertexEvaluator resultDefEval = greqlEvaluator
				.getVertexEvaluatorGraphMarker().getMark(resultDef);

		// check if there are tableheaders defined
		IsTableHeaderOf tableInc = vertex
				.getFirstIsTableHeaderOf(EdgeDirection.IN);
		JValueCollection resultCol = null;
		if (tableInc != null) {
			JValueTuple headerTuple = new JValueTuple();
			/*
			 * in GReQL 2, the report-clause always has
			 * "isTableHeaderOf"-Incidences, even if there is no "as" in the
			 * query. The user expects, that, if he just uses report x.name, a
			 * bag gets constructed and not a table without an header
			 */
			boolean hasRealHeader = false;
			while (tableInc != null) {
				VertexEvaluator headerEval = greqlEvaluator
						.getVertexEvaluatorGraphMarker().getMark(
								tableInc.getAlpha());
				headerTuple.add(headerEval.getResult(subgraph));
				if (headerEval.getResult(subgraph).toString().length() != 0) {
					hasRealHeader = true;
				}
				tableInc = tableInc.getNextIsTableHeaderOf(EdgeDirection.IN);
			}
			if (hasRealHeader)
				resultCol = new JValueTable(headerTuple);
			else
				resultCol = new JValueBag();
		} else {
			resultCol = new JValueBag();
		}

		int noOfVarCombinations = 0;
		while (declLayer.iterate(subgraph)) {
			noOfVarCombinations++;
			JValue localResult = resultDefEval.getResult(subgraph);
			resultCol.add(localResult);
		}

		return resultCol;
	}

	@Override
	public VertexCosts calculateSubtreeEvaluationCosts(GraphSize graphSize) {
		return this.greqlEvaluator.getCostModel()
				.calculateCostsBagComprehension(this, graphSize);
	}

	@Override
	public long calculateEstimatedCardinality(GraphSize graphSize) {
		return greqlEvaluator.getCostModel()
				.calculateCardinalityBagComprehension(this, graphSize);
	}

}
