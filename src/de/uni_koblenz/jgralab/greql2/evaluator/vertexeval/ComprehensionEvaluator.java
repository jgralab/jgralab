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
			varDeclLayer = (VariableDeclarationLayer) declEval.getResult(subgraph).toObject();
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