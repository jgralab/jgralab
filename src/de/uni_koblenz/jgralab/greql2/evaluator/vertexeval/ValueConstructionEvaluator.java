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


import de.uni_koblenz.jgralab.greql2.evaluator.GreqlEvaluator;
import de.uni_koblenz.jgralab.greql2.exception.EvaluateException;
import de.uni_koblenz.jgralab.greql2.jvalue.JValue;
import de.uni_koblenz.jgralab.greql2.jvalue.JValueCollection;
import de.uni_koblenz.jgralab.greql2.schema.Expression;
import de.uni_koblenz.jgralab.greql2.schema.IsPartOf;
import de.uni_koblenz.jgralab.greql2.schema.ValueConstruction;

import java.util.ArrayList;

import de.uni_koblenz.jgralab.EdgeDirection;
import de.uni_koblenz.jgralab.Vertex;
/**
 * This is the abstract base class for all ValueConstructions
 * 
 * @author Daniel Bildhauer <dbildh@uni-koblenz.de> Summer 2006, Diploma Thesis
 * 
 */
abstract public class ValueConstructionEvaluator extends VertexEvaluator {

	protected ValueConstruction vertex;
	
	private ArrayList<VertexEvaluator> partEvaluators = null;
	
	
	/**
	 * returns the vertex this VertexEvaluator evaluates
	 */
	@Override
	public Vertex getVertex() {
		return vertex;
	}

	public ValueConstructionEvaluator(ValueConstruction vertex, GreqlEvaluator eval) {
		super(eval);
		this.vertex = vertex;
	}

	public final JValue createValue(JValueCollection collection)
			throws EvaluateException {
		if (partEvaluators == null) {
			int partCount = 0;
			IsPartOf inc = vertex.getFirstIsPartOf(EdgeDirection.IN);
			while (inc != null) {
				partCount++;
				inc = inc.getNextIsPartOf(EdgeDirection.IN);
			}
			inc = vertex.getFirstIsPartOf(EdgeDirection.IN);
			partEvaluators = new ArrayList<VertexEvaluator>(partCount);
			while (inc != null) {
				Expression currentExpression = (Expression) inc.getAlpha();
				VertexEvaluator vertexEval = greqlEvaluator.getVertexEvaluatorGraphMarker().getMark(currentExpression);
				partEvaluators.add(vertexEval);
				inc = inc.getNextIsPartOf(EdgeDirection.IN);
			}
		}
		for (int i=0; i<partEvaluators.size(); i++) {
			collection.add(partEvaluators.get(i).getResult(subgraph));
		}
		return collection;
	}

}
