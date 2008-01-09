/*
 * JGraLab - The Java graph laboratory
 * (c) 2006-2007 Institute for Software Technology
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

import de.uni_koblenz.jgralab.greql2.evaluator.GreqlEvaluator;
import de.uni_koblenz.jgralab.greql2.evaluator.costmodel.GraphSize;
import de.uni_koblenz.jgralab.greql2.evaluator.costmodel.VertexCosts;
import de.uni_koblenz.jgralab.greql2.exception.EvaluateException;
import de.uni_koblenz.jgralab.greql2.exception.UnknownTypeException;
import de.uni_koblenz.jgralab.greql2.jvalue.JValue;
import de.uni_koblenz.jgralab.greql2.jvalue.JValueTypeCollection;
import de.uni_koblenz.jgralab.greql2.schema.TypeId;

import java.util.ArrayList;
import java.util.List;

import de.uni_koblenz.jgralab.AttributedElementClass;
import de.uni_koblenz.jgralab.Graph;
import de.uni_koblenz.jgralab.GraphClass;
import de.uni_koblenz.jgralab.GraphElementClass;
import de.uni_koblenz.jgralab.Schema;
import de.uni_koblenz.jgralab.Vertex;

/**
 * Creates a List of types out of the TypeId-Vertex.
 * 
 * @author Daniel Bildhauer <dbildh@uni-koblenz.de> Summer 2006, Diploma Thesis
 * 
 */
public class TypeIdEvaluator extends VertexEvaluator {

	/**
	 * returns the vertex this VertexEvaluator evaluates
	 */
	@Override
	public Vertex getVertex() {
		return vertex;
	}

	private TypeId vertex;

	public TypeIdEvaluator(TypeId vertex, GreqlEvaluator eval) {
		super(eval);
		this.vertex = vertex;
	}

	/**
	 * Creates a list of types from this TypeId-Vertex
	 * 
	 * @param schema
	 *            the schema of the datagraph
	 * @return the generated list of types
	 */
	protected List<AttributedElementClass> createTypeList(Schema schema)
			throws EvaluateException {
		ArrayList<AttributedElementClass> returnTypes = new ArrayList<AttributedElementClass>();
		if (vertex.isType()) {
			returnTypes.add((GraphElementClass) schema
					.getAttributedElementClass(vertex.getName()));
		} else {
			GraphElementClass graphElemClass = (GraphElementClass) schema
					.getAttributedElementClass(vertex.getName());
			if (graphElemClass == null) {
				throw new UnknownTypeException(vertex.getName(),
						createPossibleSourcePositions());
			}
			returnTypes.add(graphElemClass);
			returnTypes.addAll(graphElemClass.getAllSubClasses());
		}
		return returnTypes;
	}

	@Override
	public JValue evaluate() throws EvaluateException {
		Graph datagraph = getDatagraph();
		GraphClass graphClass = (GraphClass) datagraph
				.getAttributedElementClass();
		Schema schema = graphClass.getSchema();
		return new JValueTypeCollection(createTypeList(schema), vertex.isExcluded());
	}

	@Override
	public VertexCosts calculateSubtreeEvaluationCosts(GraphSize graphSize) {
		return this.greqlEvaluator.getCostModel().calculateCostsTypeId(this,
				graphSize);
	}
	
	@Override
	public double calculateEstimatedSelectivity(GraphSize graphSize) {
		return greqlEvaluator.getCostModel().calculateSelectivityTypeId(this, graphSize);
	}
	
	/* (non-Javadoc)
	 * @see de.uni_koblenz.jgralab.greql2.evaluator.vertexeval.VertexEvaluator#getLoggingName()
	 */
	@Override
	public String getLoggingName() {
		StringBuilder name = new StringBuilder();
		name.append(vertex.getAttributedElementClass().getName());
		if (vertex.isType()) {
			name.append("-type");
		}
		if (vertex.isExcluded()) {
			name.append("-excluded");
		}
		return name.toString();
	}

}
