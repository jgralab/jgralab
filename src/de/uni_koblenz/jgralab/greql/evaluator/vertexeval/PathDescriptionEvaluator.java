/*
 * JGraLab - The Java Graph Laboratory
 *
 * Copyright (C) 2006-2012 Institute for Software Technology
 *                         University of Koblenz-Landau, Germany
 *                         ist@uni-koblenz.de
 *
 * For bug reports, documentation and further information, visit
 *
 *                         https://github.com/jgralab/jgralab
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

package de.uni_koblenz.jgralab.greql.evaluator.vertexeval;

import de.uni_koblenz.jgralab.EdgeDirection;
import de.uni_koblenz.jgralab.greql.evaluator.GreqlQueryImpl;
import de.uni_koblenz.jgralab.greql.evaluator.InternalGreqlEvaluator;
import de.uni_koblenz.jgralab.greql.evaluator.fa.NFA;
import de.uni_koblenz.jgralab.greql.exception.UnknownTypeException;
import de.uni_koblenz.jgralab.greql.schema.Expression;
import de.uni_koblenz.jgralab.greql.schema.IsGoalRestrOf;
import de.uni_koblenz.jgralab.greql.schema.IsStartRestrOf;
import de.uni_koblenz.jgralab.greql.schema.PathDescription;
import de.uni_koblenz.jgralab.greql.types.TypeCollection;

/**
 * This is the base class for all path descriptions. It provides methods to add
 * start- and goalrestrictions to the pathdescription. The subclasses like
 * AlternativePathDescriptionEvaluator etc. don't need to care about this,
 * because the method PathDescriptionEvaluator.getResult(...) automaticly adds
 * start- and goalrestrictions to the pathdescription, if a start or
 * goalrestriction exists.
 * 
 * @author ist@uni-koblenz.de
 * 
 */
public abstract class PathDescriptionEvaluator<V extends PathDescription>
		extends VertexEvaluator<V> {

	/**
	 * Creates a new PathDescriptionEvaluator
	 * 
	 * @param eval
	 */
	public PathDescriptionEvaluator(V vertex, GreqlQueryImpl query) {
		super(vertex, query);
	}

	/**
	 * returns the nfa
	 */
	public NFA getNFA(InternalGreqlEvaluator evaluator) {
		NFA createdNFA = (NFA) evaluator.getLocalAutomaton(vertex);
		if (createdNFA == null) {
			createdNFA = (NFA) getResult(evaluator);
			evaluator.setLocalAutomaton(vertex, createdNFA);
		}
		return createdNFA;
	}

	/**
	 * Returns the created NFA, encapsulated in a JValue The NFA for the path
	 * description doesn't depend on the subgraph, so the getResult-Methode is
	 * overwritten
	 * 
	 * @return the result as jvalue
	 */
	@Override
	public Object getResult(InternalGreqlEvaluator evaluator) {
		NFA createdNFA = (NFA) evaluator.getLocalAutomaton(vertex);
		if (createdNFA == null) {
			Object result = evaluate(evaluator);
			createdNFA = (NFA) result;
			evaluator.setLocalAutomaton(vertex, createdNFA);
			addGoalRestrictions(evaluator);
			addStartRestrictions(evaluator);
		}
		if (evaluator.getLocalEvaluationResult(vertex) == null) {
			evaluator.setLocalEvaluationResult(vertex, createdNFA);
		}
		return evaluator.getLocalEvaluationResult(vertex);
	}

	/**
	 * creates the lists of goal type restrictions from all TypeId-Vertices that
	 * belong to this path description and adds the transitions that accepts
	 * them to the nfa
	 */
	protected void addGoalRestrictions(InternalGreqlEvaluator evaluator) {
		PathDescription pathDesc = getVertex();
		VertexEvaluator<? extends Expression> goalRestEval = null;
		IsGoalRestrOf inc = pathDesc
				.getFirstIsGoalRestrOfIncidence(EdgeDirection.IN);
		if (inc == null) {
			return;
		}
		TypeCollection typeCollection = TypeCollection.empty();
		while (inc != null) {
			VertexEvaluator<? extends Expression> vertexEval = query
					.getVertexEvaluator(inc.getAlpha());
			if (vertexEval instanceof TypeIdEvaluator) {
				TypeIdEvaluator typeEval = (TypeIdEvaluator) vertexEval;
				typeCollection = typeCollection
						.combine((TypeCollection) typeEval.getResult(evaluator));
			} else {
				goalRestEval = vertexEval;
			}
			inc = inc.getNextIsGoalRestrOfIncidence(EdgeDirection.IN);
		}
		try {
			typeCollection = typeCollection.bindToSchema(evaluator);
		} catch (UnknownTypeException e) {
			throw new UnknownTypeException(e.getTypeName(),
					createPossibleSourcePositions());
		}
		NFA.addGoalTypeRestriction(getNFA(evaluator), typeCollection);
		if (goalRestEval != null) {
			NFA.addGoalBooleanRestriction(getNFA(evaluator), goalRestEval,
					query);
		}
	}

	/**
	 * creates the lists of start and goal type restrictions from all
	 * TypeId-Vertices that belong to this path descritpion
	 * 
	 * @return the generated list of types
	 */
	protected void addStartRestrictions(InternalGreqlEvaluator evaluator) {
		PathDescription pathDesc = getVertex();
		VertexEvaluator<? extends Expression> startRestEval = null;
		IsStartRestrOf inc = pathDesc
				.getFirstIsStartRestrOfIncidence(EdgeDirection.IN);
		if (inc == null) {
			return;
		}
		TypeCollection typeCollection = TypeCollection.empty();
		while (inc != null) {
			VertexEvaluator<? extends Expression> vertexEval = query
					.getVertexEvaluator(inc.getAlpha());
			if (vertexEval instanceof TypeIdEvaluator) {
				TypeIdEvaluator typeEval = (TypeIdEvaluator) vertexEval;
				typeCollection = typeCollection
						.combine((TypeCollection) typeEval.getResult(evaluator));
			} else {
				startRestEval = vertexEval;
			}
			inc = inc.getNextIsStartRestrOfIncidence(EdgeDirection.IN);
		}
		try {
			typeCollection = typeCollection.bindToSchema(evaluator);
		} catch (UnknownTypeException e) {
			throw new UnknownTypeException(e.getTypeName(),
					createPossibleSourcePositions());
		}
		NFA.addStartTypeRestriction(getNFA(evaluator), typeCollection);
		if (startRestEval != null) {
			NFA.addStartBooleanRestriction(getNFA(evaluator), startRestEval,
					query);
		}
	}

}
