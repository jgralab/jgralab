/*
 * JGraLab - The Java Graph Laboratory
 *
 * Copyright (C) 2006-2014 Institute for Software Technology
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
package de.uni_koblenz.jgralab.gretl;

import java.util.Map.Entry;

import org.pcollections.PMap;

import de.uni_koblenz.jgralab.AttributedElement;
import de.uni_koblenz.jgralab.gretl.Context.TransformationPhase;
import de.uni_koblenz.jgralab.schema.AttributedElementClass;

/**
 * This transformation allows for adding mappings to the img/arch functions,
 * which are usually managed automatically by the transformation framework. The
 * transformation only expects a semantic expression resulting in a map from
 * arbitrary archetype to a graph elements. The mappings are added to the
 * arch/img functions for the respective graph element class.
 * 
 * This is mostly useful in in-place scenarios, where you can use this
 * transformation to add special mappings for elements the transformation does
 * not affect, but which should be referred to.
 * 
 * Example: Consider an in-place transformation, that only creates some new
 * edges. The {@link CreateEdges} transformation expects a set of 3-tuples
 * (newEdgeArchetype, startVertexArchetype, endVertexArchetype). However, the
 * already existing vertices that should be connected by the new edges don't
 * have an archetype, because they already existed in the source graph, which is
 * the target graph as well in this in-place scenario.
 * 
 * @author Tassilo Horn &lt;horn@uni-koblenz.de&gt;
 */
public class AddMappings extends Transformation<Void> {

	private String semanticExpression;
	private PMap<Object, AttributedElement<?, ?>> archetypes;

	public AddMappings(Context c, String semanticExpression) {
		super(c);
		this.semanticExpression = semanticExpression;
	}

	public AddMappings(Context c,
			PMap<Object, AttributedElement<?, ?>> archetypeMap) {
		super(c);
		archetypes = archetypeMap;
	}

	public static AddMappings parseAndCreate(ExecuteTransformation et) {
		et.matchTransformationArrow();
		String semanticExpression = et.matchSemanticExpression();
		return new AddMappings(et.context, semanticExpression);
	}

	@Override
	protected Void transform() {
		if (context.getPhase() != TransformationPhase.GRAPH) {
			return null;
		}

		if (archetypes == null) {
			archetypes = context.evaluateGReQLQuery(semanticExpression);
		}

		for (Entry<Object, AttributedElement<?, ?>> e : archetypes.entrySet()) {
			AttributedElementClass<?, ?> aec = e.getValue()
					.getAttributedElementClass();
			context.addMapping(aec, e.getKey(), e.getValue());
		}

		return null;
	}

}
