/*
 * JGraLab - The Java graph laboratory
 * (c) 2006-2008 Institute for Software Technology
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

import java.util.ArrayList;
import java.util.List;

import de.uni_koblenz.jgralab.Graph;
import de.uni_koblenz.jgralab.Vertex;
import de.uni_koblenz.jgralab.greql2.evaluator.GreqlEvaluator;
import de.uni_koblenz.jgralab.greql2.evaluator.costmodel.GraphSize;
import de.uni_koblenz.jgralab.greql2.evaluator.costmodel.VertexCosts;
import de.uni_koblenz.jgralab.greql2.exception.EvaluateException;
import de.uni_koblenz.jgralab.greql2.exception.UnknownTypeException;
import de.uni_koblenz.jgralab.greql2.jvalue.JValue;
import de.uni_koblenz.jgralab.greql2.jvalue.JValueTypeCollection;
import de.uni_koblenz.jgralab.greql2.schema.TypeId;
import de.uni_koblenz.jgralab.schema.AttributedElementClass;
import de.uni_koblenz.jgralab.schema.QualifiedName;
import de.uni_koblenz.jgralab.schema.Schema;

/**
 * Creates a List of types out of the TypeId-Vertex.
 *
 * @author ist@uni-koblenz.de
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
		AttributedElementClass elemClass = (AttributedElementClass) schema
		.getAttributedElementClass(new QualifiedName(vertex.getName()));
		if (elemClass == null) {
			elemClass = greqlEvaluator.getKnownType(vertex.getName());
			if (elemClass == null)
				throw new UnknownTypeException(vertex.getName(),
					createPossibleSourcePositions());
			else
				vertex.setName(elemClass.getQualifiedName());
		}
		returnTypes.add(elemClass);
		if (!vertex.isType()) {
			returnTypes.addAll(elemClass.getAllSubClasses());
		} 
		return returnTypes;
	}

	@Override
	public JValue evaluate() throws EvaluateException {
		Graph datagraph = getDatagraph();
		Schema schema = datagraph.getSchema();
		return new JValueTypeCollection(createTypeList(schema), vertex
				.isExcluded());
	}

	@Override
	public VertexCosts calculateSubtreeEvaluationCosts(GraphSize graphSize) {
		return this.greqlEvaluator.getCostModel().calculateCostsTypeId(this,
				graphSize);
	}

	@Override
	public double calculateEstimatedSelectivity(GraphSize graphSize) {
		return greqlEvaluator.getCostModel().calculateSelectivityTypeId(this,
				graphSize);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @seede.uni_koblenz.jgralab.greql2.evaluator.vertexeval.VertexEvaluator#
	 * getLoggingName()
	 */
	@Override
	public String getLoggingName() {
		StringBuilder name = new StringBuilder();
		name.append(vertex.getAttributedElementClass().getQualifiedName());
		if (vertex.isType()) {
			name.append("-type");
		}
		if (vertex.isExcluded()) {
			name.append("-excluded");
		}
		return name.toString();
	}

}
