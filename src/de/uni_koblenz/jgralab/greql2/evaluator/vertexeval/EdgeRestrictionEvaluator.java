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

import java.util.HashSet;
import java.util.Set;

import de.uni_koblenz.jgralab.EdgeDirection;
import de.uni_koblenz.jgralab.greql2.evaluator.GreqlEvaluator;
import de.uni_koblenz.jgralab.greql2.evaluator.costmodel.GraphSize;
import de.uni_koblenz.jgralab.greql2.evaluator.costmodel.VertexCosts;
import de.uni_koblenz.jgralab.greql2.exception.EvaluateException;
import de.uni_koblenz.jgralab.greql2.exception.JValueInvalidTypeException;
import de.uni_koblenz.jgralab.greql2.jvalue.JValue;
import de.uni_koblenz.jgralab.greql2.jvalue.JValueImpl;
import de.uni_koblenz.jgralab.greql2.jvalue.JValueTypeCollection;
import de.uni_koblenz.jgralab.greql2.schema.EdgeRestriction;
import de.uni_koblenz.jgralab.greql2.schema.Greql2Vertex;
import de.uni_koblenz.jgralab.greql2.schema.IsBooleanPredicateOfEdgeRestriction;
import de.uni_koblenz.jgralab.greql2.schema.IsRoleIdOf;
import de.uni_koblenz.jgralab.greql2.schema.IsTypeIdOf;
import de.uni_koblenz.jgralab.greql2.schema.RoleId;

/**
 * Evaluates an edge restriction, edges can be restricted with TypeIds and Roles
 * 
 * @author ist@uni-koblenz.de
 * 
 */
public class EdgeRestrictionEvaluator extends VertexEvaluator {

	/**
	 * The EdgeRestriction vertex in the GReQL Syntaxgraph
	 */
	private EdgeRestriction vertex;

	private VertexEvaluator predicateEvaluator = null;

	/**
	 * returns the vertex this VertexEvaluator evaluates
	 */
	@Override
	public Greql2Vertex getVertex() {
		return vertex;
	}

	public VertexEvaluator getPredicateEvaluator() {
		return predicateEvaluator;
	}

	/**
	 * The JValueTypeCollection which holds all the allowed and forbidden types
	 */
	private JValueTypeCollection typeCollection = null;

	/**
	 * Returns the typeCollection
	 */
	public JValueTypeCollection getTypeCollection() throws EvaluateException {
		if (typeCollection == null) {
			evaluate();
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
	public EdgeRestrictionEvaluator(EdgeRestriction vertex, GreqlEvaluator eval) {
		super(eval);
		this.vertex = vertex;
	}

	/**
	 * evaluates the EdgeRestriction, creates the typeList and the validEdgeRole
	 */
	@Override
	public JValue evaluate() throws EvaluateException {
		if (typeCollection == null) {
			typeCollection = new JValueTypeCollection();
			IsTypeIdOf typeInc = vertex
					.getFirstIsTypeIdOfIncidence(EdgeDirection.IN);
			while (typeInc != null) {
				TypeIdEvaluator typeEval = (TypeIdEvaluator) vertexEvalMarker
						.getMark(typeInc.getAlpha());
				try {
					// GreqlEvaluator.println("Adding types: " +
					// typeEval.getResult(subgraph).toJValueTypeCollection());
					typeCollection.addTypes(typeEval.getResult(subgraph)
							.toJValueTypeCollection());
				} catch (JValueInvalidTypeException ex) {
					throw new EvaluateException(
							"Result of TypeId was not a JValueTypeCollection",
							ex);
				}
				typeInc = typeInc.getNextIsTypeIdOf(EdgeDirection.IN);
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
			predicateEvaluator = vertexEvalMarker.getMark(predInc.getAlpha());
		}
		return new JValueImpl();
	}

	@Override
	public VertexCosts calculateSubtreeEvaluationCosts(GraphSize graphSize) {
		return this.greqlEvaluator.getCostModel()
				.calculateCostsEdgeRestriction(this, graphSize);
	}

}
