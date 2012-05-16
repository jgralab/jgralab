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

import java.util.ArrayList;
import java.util.List;

import de.uni_koblenz.jgralab.greql2.evaluator.InternalGreqlEvaluator;
import de.uni_koblenz.jgralab.greql2.evaluator.QueryImpl;
import de.uni_koblenz.jgralab.greql2.evaluator.VertexCosts;
import de.uni_koblenz.jgralab.greql2.exception.UnknownTypeException;
import de.uni_koblenz.jgralab.greql2.schema.TypeId;
import de.uni_koblenz.jgralab.greql2.types.TypeCollection;
import de.uni_koblenz.jgralab.schema.GraphElementClass;

/**
 * Creates a List of types out of the TypeId-Vertex.
 * 
 * @author ist@uni-koblenz.de
 * 
 */
public class TypeIdEvaluator extends VertexEvaluator<TypeId> {

	public TypeIdEvaluator(TypeId vertex, QueryImpl query) {
		super(vertex, query);
	}

	/**
	 * Creates a list of types from this TypeId-Vertex
	 * 
	 * @param schema
	 *            the schema of the datagraph
	 * @return the generated list of types
	 */
	protected List<GraphElementClass<?, ?>> createTypeList(
			InternalGreqlEvaluator evaluator) {

		ArrayList<GraphElementClass<?, ?>> returnTypes = new ArrayList<GraphElementClass<?, ?>>();
		GraphElementClass<?, ?> elemClass = (GraphElementClass<?, ?>) evaluator
				.getAttributedElementClass(vertex.get_name());
		if (elemClass == null) {
			elemClass = (GraphElementClass<?, ?>) query.getKnownType(
					evaluator.getSchemaOfDataGraph(), vertex.get_name());
			if (elemClass == null) {
				throw new UnknownTypeException(vertex.get_name(),
						createPossibleSourcePositions());
			} else {
				vertex.set_name(elemClass.getQualifiedName());
			}
		}
		returnTypes.add(elemClass);
		if (!vertex.is_type()) {
			returnTypes.addAll(elemClass.getAllSubClasses());
		}
		return returnTypes;
	}

	@Override
	public TypeCollection evaluate(InternalGreqlEvaluator evaluator) {
		List<GraphElementClass<?, ?>> typeList = createTypeList(evaluator);
		return new TypeCollection(typeList, vertex.is_excluded());
	}

	@Override
	public VertexCosts calculateSubtreeEvaluationCosts() {
		long costs = query.getOptimizerInfo().getKnownEdgeTypes()
				+ query.getOptimizerInfo().getKnownVertexTypes();
		return new VertexCosts(costs, costs, costs);
	}

	@Override
	public double calculateEstimatedSelectivity() {
		int typesInSchema = (int) Math.round((query.getOptimizerInfo()
				.getKnownEdgeTypes() + query.getOptimizerInfo()
				.getKnownVertexTypes()) / 2.0);
		double selectivity = 1.0;
		TypeId id = getVertex();
		if (id.is_type()) {
			selectivity = 1.0 / typesInSchema;
		} else {
			double avgSubclasses = (query.getOptimizerInfo()
					.getAverageEdgeSubclasses() + query.getOptimizerInfo()
					.getAverageVertexSubclasses()) / 2.0;
			selectivity = avgSubclasses / typesInSchema;
		}
		if (id.is_excluded()) {
			selectivity = 1 - selectivity;
		}
		return selectivity;
	}

	@Override
	public String getLoggingName() {
		StringBuilder name = new StringBuilder();
		name.append(vertex.getAttributedElementClass().getQualifiedName());
		if (vertex.is_type()) {
			name.append("-type");
		}
		if (vertex.is_excluded()) {
			name.append("-excluded");
		}
		return name.toString();
	}

}
