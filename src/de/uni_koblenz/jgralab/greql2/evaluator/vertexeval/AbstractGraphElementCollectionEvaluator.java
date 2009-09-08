/*
 * JGraLab - The Java graph laboratory
 * (c) 2006-2009 Institute for Software Technology
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

import de.uni_koblenz.jgralab.EdgeDirection;
import de.uni_koblenz.jgralab.greql2.evaluator.GreqlEvaluator;
import de.uni_koblenz.jgralab.greql2.exception.EvaluateException;
import de.uni_koblenz.jgralab.greql2.exception.JValueInvalidTypeException;
import de.uni_koblenz.jgralab.greql2.jvalue.JValueTypeCollection;
import de.uni_koblenz.jgralab.greql2.schema.Expression;
import de.uni_koblenz.jgralab.greql2.schema.IsTypeRestrOf;
import de.uni_koblenz.jgralab.greql2.schema.TypeId;

/**
 * This class is the base class for all VertexEvaluators, that construct an
 * element collection, for instance EdgeSetExpressionEvaluator. But it is not
 * the base for Forward- or BackwardVertexSetEvaluator, because these are
 * PathSearchEvaluators.
 * 
 * @author ist@uni-koblenz.de Summer 2006, Diploma Thesis
 * 
 */
public abstract class AbstractGraphElementCollectionEvaluator extends
		VertexEvaluator {

	public AbstractGraphElementCollectionEvaluator(GreqlEvaluator eval) {
		super(eval);
	}

	private JValueTypeCollection typeCollection = null;

	protected JValueTypeCollection getTypeCollection() throws EvaluateException {
		if (typeCollection == null) {
			typeCollection = new JValueTypeCollection();
			IsTypeRestrOf inc = ((Expression) getVertex())
					.getFirstIsTypeRestrOf(EdgeDirection.IN);
			while (inc != null) {
				if (inc.getAlpha() instanceof TypeId) {
					TypeIdEvaluator typeEval = (TypeIdEvaluator) greqlEvaluator
							.getVertexEvaluatorGraphMarker().getMark(
									inc.getAlpha());
					try {
						typeCollection.addTypes(typeEval.getResult(subgraph)
								.toJValueTypeCollection());
					} catch (JValueInvalidTypeException ex) {
						throw new EvaluateException(
								"Result of TypeId was not a JValueTypeCollection",
								ex);
					}
				}
				inc = inc.getNextIsTypeRestrOf(EdgeDirection.IN);
			}
		}
		return typeCollection;
	}

}
