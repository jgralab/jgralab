package de.uni_koblenz.jgralab.greql2.optimizer.dissolution;

import java.util.HashMap;
import java.util.HashSet;

import de.uni_koblenz.jgralab.greql2.evaluator.vertexeval.VertexEvaluator;
import de.uni_koblenz.jgralab.greql2.schema.Expression;
import de.uni_koblenz.jgralab.greql2.schema.Greql2;

public class Atom extends Leaf {
	private static HashMap<String, Integer> nameNumberingMap = new HashMap<String, Integer>();
	private static boolean DEBUG = false;
	protected Expression originalExpression;
	protected String name;
	private int number;

	/**
	 * Contains all originalExpressions of Atoms for which the initial costs
	 * were already returned. If an Atom's oE is already contained here, it's
	 * correct to return 1 as costs.
	 */
	protected static HashSet<Expression> originalExpressionsCostSet;

	public static void resetNumbering() {
		nameNumberingMap.clear();
	}

	public Atom(String name, Expression origExp) {
		number = 1;
		if (DEBUG) {
			if (nameNumberingMap.containsKey(name)) {
				number = nameNumberingMap.get(name);
			}
			nameNumberingMap.put(name, number + 1);
		}
		this.name = name;

		originalExpression = origExp;
	}

	@Override
	public String toString() {
		if (DEBUG)
			return name + "_" + number;
		return name;
	}

	public String getName() {
		return name;
	}

	@Override
	public Expression toExpression(Greql2 syntaxgraph) {
		return originalExpression;
	}

	@Override
	public SemanticGraph toNegationNormalForm() {
		return this;
	}

	@Override
	public SemanticGraph deepCopy() {
		return new Atom(name, originalExpression);
	}

	@Override
	public boolean isEqualTo(SemanticGraph sg) {
		return this == sg;
	}

	@Override
	public long getCosts() {
		if (originalExpressionsCostSet.contains(originalExpression)) {
			// the initial costs were already returned.
			return 1;
		}

		VertexEvaluator veval = vertexEvalGraphMarker
				.getMark(originalExpression);
		originalExpressionsCostSet.add(originalExpression);
		return veval.getInitialSubtreeEvaluationCosts(graphSize);
	}
}
