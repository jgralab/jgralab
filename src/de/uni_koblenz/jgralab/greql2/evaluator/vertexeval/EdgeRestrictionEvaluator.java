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

package de.uni_koblenz.jgralab.greql2.evaluator.vertexeval;

import java.util.HashSet;
import java.util.Set;

import de.uni_koblenz.jgralab.EdgeDirection;
import de.uni_koblenz.jgralab.greql2.evaluator.InternalGreqlEvaluator;
import de.uni_koblenz.jgralab.greql2.evaluator.QueryImpl;
import de.uni_koblenz.jgralab.greql2.schema.EdgeRestriction;
import de.uni_koblenz.jgralab.greql2.schema.Expression;
import de.uni_koblenz.jgralab.greql2.schema.IsBooleanPredicateOfEdgeRestriction;
import de.uni_koblenz.jgralab.greql2.schema.IsRoleIdOf;
import de.uni_koblenz.jgralab.greql2.schema.IsTypeIdOf;
import de.uni_koblenz.jgralab.greql2.schema.RoleId;
import de.uni_koblenz.jgralab.greql2.schema.TypeId;
import de.uni_koblenz.jgralab.greql2.types.TypeCollection;

/**
 * Evaluates an edge restriction, edges can be restricted with TypeIds and Roles
 * 
 * @author ist@uni-koblenz.de
 * 
 */
public class EdgeRestrictionEvaluator extends VertexEvaluator<EdgeRestriction> {

	private VertexEvaluator<? extends Expression> predicateEvaluator = null;

	public VertexEvaluator<? extends Expression> getPredicateEvaluator() {
		return predicateEvaluator;
	}

	/**
	 * The JValueTypeCollection which holds all the allowed and forbidden types
	 */
	private TypeCollection typeCollection = null;

	/**
	 * Returns the typeCollection
	 */
	public TypeCollection getTypeCollection(InternalGreqlEvaluator evaluator) {
		if (typeCollection == null) {
			evaluate(evaluator);
		}
		return typeCollection;
	}

	/**
	 * the valid role of an edge
	 */
	private Set<String> validRoles;

	/**
	 * @return the valid edge role
	 */
	public Set<String> getEdgeRoles() {
		return validRoles;
	}

	/**
	 * creates a new EdgeRestriction evaluator
	 * 
	 * @param vertex
	 * @param eval
	 */
	public EdgeRestrictionEvaluator(EdgeRestriction vertex, QueryImpl query) {
		super(vertex, query);
	}

	/**
	 * evaluates the EdgeRestriction, creates the typeList and the validEdgeRole
	 */
	@Override
	public Object evaluate(InternalGreqlEvaluator evaluator) {
		if (typeCollection == null) {
			typeCollection = new TypeCollection();
			IsTypeIdOf typeInc = vertex
					.getFirstIsTypeIdOfIncidence(EdgeDirection.IN);
			while (typeInc != null) {
				TypeIdEvaluator typeEval = (TypeIdEvaluator) query
						.getVertexEvaluator((TypeId) typeInc.getAlpha());
				typeCollection.addTypes((TypeCollection) typeEval
						.getResult(evaluator));
				typeInc = typeInc.getNextIsTypeIdOfIncidence(EdgeDirection.IN);
			}
		}

		if (vertex.getFirstIsRoleIdOfIncidence() != null) {
			validRoles = new HashSet<String>();
			for (IsRoleIdOf e : vertex.getIsRoleIdOfIncidences()) {
				RoleId role = (RoleId) e.getAlpha();
				validRoles.add(role.get_name());
			}
		}
		IsBooleanPredicateOfEdgeRestriction predInc = vertex
				.getFirstIsBooleanPredicateOfEdgeRestrictionIncidence(EdgeDirection.IN);
		if (predInc != null) {
			// System.out.println("Found a BooleanPredicateOfEdge");
			predicateEvaluator = query.getVertexEvaluator((Expression) predInc
					.getAlpha());
		}
		return null;
	}

	// @Override
	// public VertexCosts calculateSubtreeEvaluationCosts() {
	// return greqlEvaluator.getCostModel()
	// .calculateCostsEdgeRestriction(this);
	// }

}
