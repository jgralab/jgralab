/*
 * JGraLab - The Java graph laboratory
 * (c) 2006-2010 Institute for Software Technology
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
					.getFirstIsCompResultDefOf(EdgeDirection.IN).getAlpha();
			resultDefinitionEvaluator = greqlEvaluator
					.getVertexEvaluatorGraphMarker().getMark(resultDefinition);
		}
		return resultDefinitionEvaluator;
	}

	protected final VariableDeclarationLayer getVariableDeclationLayer() {
		if (varDeclLayer == null) {
			Declaration d = (Declaration) getVertex().getFirstIsCompDeclOf(
					EdgeDirection.IN).getAlpha();
			DeclarationEvaluator declEval = (DeclarationEvaluator) greqlEvaluator
					.getVertexEvaluatorGraphMarker().getMark(d);
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
