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
package de.uni_koblenz.jgralab.greql2.evaluator.vertexeval;

import de.uni_koblenz.jgralab.EdgeDirection;
import de.uni_koblenz.jgralab.Vertex;
import de.uni_koblenz.jgralab.greql2.evaluator.GreqlEvaluator;
import de.uni_koblenz.jgralab.greql2.evaluator.VariableDeclarationLayer;
import de.uni_koblenz.jgralab.greql2.evaluator.costmodel.GraphSize;
import de.uni_koblenz.jgralab.greql2.evaluator.costmodel.VertexCosts;
import de.uni_koblenz.jgralab.greql2.exception.EvaluateException;
import de.uni_koblenz.jgralab.greql2.jvalue.JValue;
import de.uni_koblenz.jgralab.greql2.jvalue.JValueCollection;
import de.uni_koblenz.jgralab.greql2.jvalue.JValueMap;
import de.uni_koblenz.jgralab.greql2.schema.Comprehension;
import de.uni_koblenz.jgralab.greql2.schema.MapComprehension;

/**
 * @author Tassilo Horn <horn@uni-koblenz.de>
 * 
 */
public class MapComprehensionEvaluator extends ComprehensionEvaluator {
	private MapComprehension vertex;

	public MapComprehensionEvaluator(MapComprehension vertex,
			GreqlEvaluator eval) {
		super(eval);
		this.vertex = vertex;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @seede.uni_koblenz.jgralab.greql2.evaluator.vertexeval.VertexEvaluator#
	 * calculateSubtreeEvaluationCosts
	 * (de.uni_koblenz.jgralab.greql2.evaluator.costmodel.GraphSize)
	 */
	@Override
	protected VertexCosts calculateSubtreeEvaluationCosts(GraphSize graphSize) {
		return this.greqlEvaluator.getCostModel()
				.calculateCostsMapComprehension(this, graphSize);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * de.uni_koblenz.jgralab.greql2.evaluator.vertexeval.VertexEvaluator#evaluate
	 * ()
	 */
	@Override
	public JValue evaluate() throws EvaluateException {
		VariableDeclarationLayer declLayer = getVariableDeclationLayer();

		JValueMap resultMap = new JValueMap();

		Vertex key = vertex.getFirstIsKeyExprOfComprehension(EdgeDirection.IN)
				.getAlpha();
		VertexEvaluator keyEval = greqlEvaluator
				.getVertexEvaluatorGraphMarker().getMark(key);
		Vertex val = vertex
				.getFirstIsValueExprOfComprehension(EdgeDirection.IN)
				.getAlpha();
		VertexEvaluator valEval = greqlEvaluator
				.getVertexEvaluatorGraphMarker().getMark(val);
		declLayer.reset();
		while (declLayer.iterate(subgraph)) {
			JValue jkey = keyEval.getResult(subgraph);
			JValue jval = valEval.getResult(subgraph);
			resultMap.put(jkey, jval);
		}
		return resultMap;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * de.uni_koblenz.jgralab.greql2.evaluator.vertexeval.VertexEvaluator#getVertex
	 * ()
	 */
	@Override
	public Comprehension getVertex() {
		return vertex;
	}

	@Override
	public long calculateEstimatedCardinality(GraphSize graphSize) {
		return greqlEvaluator.getCostModel()
				.calculateCardinalityMapComprehension(this, graphSize);
	}

	@Override
	protected JValueCollection getResultDatastructure() {
		return null;
	}

}
