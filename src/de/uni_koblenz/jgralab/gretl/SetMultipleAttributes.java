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
package de.uni_koblenz.jgralab.gretl;

import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;

import org.pcollections.Empty;
import org.pcollections.PMap;
import org.pcollections.PSequence;
import org.pcollections.PVector;

import de.uni_koblenz.jgralab.AttributedElement;
import de.uni_koblenz.jgralab.gretl.Context.TransformationPhase;
import de.uni_koblenz.jgralab.schema.Attribute;

public class SetMultipleAttributes extends
		Transformation<PVector<PMap<AttributedElement, Object>>> {

	private Attribute[] attributes = null;
	private PMap<Object, PSequence<Object>> archetype2valuesMap = null;
	private String semanticExpression = null;

	public SetMultipleAttributes(Context c, String semanticExpression,
			Attribute... attrs) {
		super(c);
		attributes = attrs;
		this.semanticExpression = semanticExpression;
	}

	public SetMultipleAttributes(Context c,
			PMap<Object, PSequence<Object>> arch2ValuesMap, Attribute... attrs) {
		super(c);
		attributes = attrs;
		archetype2valuesMap = arch2ValuesMap;
	}

	public static SetMultipleAttributes parseAndCreate(ExecuteTransformation et) {
		Attribute[] attrs = et.matchAttributeArray();
		et.matchTransformationArrow();
		String semExp = et.matchSemanticExpression();
		return new SetMultipleAttributes(et.context, semExp, attrs);
	}

	@Override
	protected PVector<PMap<AttributedElement, Object>> transform() {
		if (context.phase != TransformationPhase.GRAPH) {
			return null;
		}

		if (archetype2valuesMap == null) {
			archetype2valuesMap = context
					.evaluateGReQLQuery(semanticExpression);
		}
		PVector<PMap<AttributedElement, Object>> retLst = Empty.vector();
		List<PMap<Object, Object>> lst = splice(archetype2valuesMap);
		for (int i = 0; i < attributes.length; i++) {
			retLst = retLst.plus(new SetAttributes(context, attributes[i], lst
					.get(i)).execute());
		}
		return retLst;
	}

	private List<PMap<Object, Object>> splice(
			PMap<Object, PSequence<Object>> arch2listOfAttrVals) {
		List<PMap<Object, Object>> out = new ArrayList<PMap<Object, Object>>(
				attributes.length);

		for (int i = 0; i < attributes.length; i++) {
			out.add(Empty.orderedMap());
		}

		for (Entry<Object, PSequence<Object>> e : arch2listOfAttrVals
				.entrySet()) {
			for (int i = 0; i < attributes.length; i++) {
				PMap<Object, Object> nm = out.get(i).plus(e.getKey(),
						e.getValue().get(i));
				out.set(i, nm);
			}
		}
		return out;
	}
}
