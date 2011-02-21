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

import de.uni_koblenz.jgralab.EdgeDirection;
import de.uni_koblenz.jgralab.greql2.evaluator.GreqlEvaluator;
import de.uni_koblenz.jgralab.greql2.evaluator.VariableDeclarationLayer;
import de.uni_koblenz.jgralab.greql2.exception.EvaluateException;
import de.uni_koblenz.jgralab.greql2.jvalue.JValue;
import de.uni_koblenz.jgralab.greql2.jvalue.JValueCollection;
import de.uni_koblenz.jgralab.greql2.schema.Comprehension;
import de.uni_koblenz.jgralab.greql2.schema.Declaration;
import de.uni_koblenz.jgralab.greql2.schema.Expression;

public abstract class ComprehensionEvaluator extends VertexEvaluator {

	private VariableDeclarationLayer varDeclLayer = null;
	private VertexEvaluator resultDefinitionEvaluator = null;

	@Override
	public abstract Comprehension getVertex();

	public ComprehensionEvaluator(GreqlEvaluator eval) {
		super(eval);
	}

	protected abstract JValueCollection getResultDatastructure();

	protected final VertexEvaluator getResultDefinitionEvaluator() {
		if (resultDefinitionEvaluator == null) {
			Expression resultDefinition = (Expression) getVertex()
					.getFirstIsCompResultDefOfIncidence(EdgeDirection.IN)
					.getAlpha();
			resultDefinitionEvaluator = vertexEvalMarker
					.getMark(resultDefinition);
		}
		return resultDefinitionEvaluator;
	}

	protected final VariableDeclarationLayer getVariableDeclationLayer() {
		if (varDeclLayer == null) {
			Declaration d = (Declaration) getVertex()
					.getFirstIsCompDeclOfIncidence(EdgeDirection.IN).getAlpha();
			DeclarationEvaluator declEval = (DeclarationEvaluator) vertexEvalMarker
					.getMark(d);
			varDeclLayer = (VariableDeclarationLayer) declEval.getResult(
					subgraph).toObject();
		}
		return varDeclLayer;
	}

	@Override
	public JValue evaluate() throws EvaluateException {
		VariableDeclarationLayer declLayer = getVariableDeclationLayer();
		VertexEvaluator resultDefEval = getResultDefinitionEvaluator();
		JValueCollection resultCollection = getResultDatastructure();
		declLayer.reset();
		int noOfVarCombinations = 0;
		while (declLayer.iterate(subgraph)) {
			noOfVarCombinations++;
			JValue localResult = resultDefEval.getResult(subgraph);
			resultCollection.add(localResult);
		}
		return resultCollection;
	}

}
