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

package de.uni_koblenz.jgralab.greql.optimizer;

import java.util.HashMap;

import de.uni_koblenz.jgralab.Graph;
import de.uni_koblenz.jgralab.greql.OptimizerInfo;
import de.uni_koblenz.jgralab.greql.evaluator.GreqlQueryImpl;
import de.uni_koblenz.jgralab.greql.evaluator.InternalGreqlEvaluator;
import de.uni_koblenz.jgralab.greql.evaluator.fa.FiniteAutomaton;
import de.uni_koblenz.jgralab.greql.evaluator.vertexeval.ElementSetExpressionEvaluator;
import de.uni_koblenz.jgralab.greql.evaluator.vertexeval.GreqlExpressionEvaluator;
import de.uni_koblenz.jgralab.greql.evaluator.vertexeval.TypeIdEvaluator;
import de.uni_koblenz.jgralab.greql.schema.ElementSetExpression;
import de.uni_koblenz.jgralab.greql.schema.GreqlExpression;
import de.uni_koblenz.jgralab.greql.schema.GreqlGraph;
import de.uni_koblenz.jgralab.greql.schema.GreqlVertex;
import de.uni_koblenz.jgralab.greql.schema.TypeId;
import de.uni_koblenz.jgralab.schema.AttributedElementClass;
import de.uni_koblenz.jgralab.schema.Schema;

public class TypeCollectionEvaluator implements InternalGreqlEvaluator {

	private Schema schema;

	private HashMap<GreqlVertex, Object> results;

	private GreqlQueryImpl query;

	public TypeCollectionEvaluator(GreqlQueryImpl query) {
		this.query = query;
		results = new HashMap<GreqlVertex, Object>();
	}

	public void execute() {
		// System.out.println("TypeCollectionEvaluator.execute()");
		OptimizerInfo info = query.getOptimizerInfo();
		schema = info.getSchema();
		GreqlGraph graph = query.getQueryGraph();

		// resolve imports
		GreqlExpression root = graph.getFirstGreqlExpression();
		if (root != null) {
			GreqlExpressionEvaluator e = (GreqlExpressionEvaluator) query
					.getVertexEvaluator(root);
			e.handleImportedTypes(schema);
		}

		// create TypeCollections for TypeId
		// System.out.println("evaluate TypeId");
		for (TypeId tid : graph.getTypeIdVertices()) {
			TypeIdEvaluator e = (TypeIdEvaluator) query.getVertexEvaluator(tid);
			e.evaluate(this);
			// System.out.println("\t" + e.evaluate(this));
		}

		// create TypeCollections for EdgeSetExpression and VertexSetExpression
		// System.out.println("evaluate ElementSetExpression");
		for (ElementSetExpression ese : graph.getElementSetExpressionVertices()) {
			ElementSetExpressionEvaluator<?> e = (ElementSetExpressionEvaluator<?>) query
					.getVertexEvaluator(ese);
			e.getTypeCollection(this);
			// System.out.println("\t" + e.getTypeCollection(this));
		}

		// System.out.println("TypeCollectionEvaluator finished.");

	}

	@Override
	public Object setVariable(String varName, Object value) {
		return null;
	}

	@Override
	public Object getVariable(String varName) {
		return null;
	}

	@Override
	public Object setLocalEvaluationResult(GreqlVertex vertex, Object value) {
		return results.put(vertex, value);
	}

	@Override
	public Object getLocalEvaluationResult(GreqlVertex vertex) {
		return results.get(vertex);
	}

	@Override
	public Object removeLocalEvaluationResult(GreqlVertex vertex) {
		return results.remove(vertex);
	}

	@Override
	public Graph getDataGraph() {
		return null;
	}

	@Override
	public Schema getSchemaOfDataGraph() {
		return schema;
	}

	@Override
	public AttributedElementClass<?, ?> getAttributedElementClass(
			String qualifiedName) {
		return schema != null ? schema.getAttributedElementClass(qualifiedName)
				: null;
	}

	@Override
	public void progress(long value) {
	}

	@Override
	public void setDatagraphSchema(Schema datagraphSchema) {
		schema = datagraphSchema;
	}

	@Override
	public FiniteAutomaton setLocalAutomaton(GreqlVertex vertex,
			FiniteAutomaton value) {
		return null;
	}

	@Override
	public FiniteAutomaton getLocalAutomaton(GreqlVertex vertex) {
		return null;
	}
}
