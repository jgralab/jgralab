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

import org.pcollections.Empty;
import org.pcollections.PMap;

import de.uni_koblenz.jgralab.AttributedElement;
import de.uni_koblenz.jgralab.Record;
import de.uni_koblenz.jgralab.greql2.types.Undefined;
import de.uni_koblenz.jgralab.gretl.Context.GReTLVariableType;
import de.uni_koblenz.jgralab.gretl.Context.TransformationPhase;
import de.uni_koblenz.jgralab.schema.Attribute;
import de.uni_koblenz.jgralab.schema.Domain;
import de.uni_koblenz.jgralab.schema.RecordDomain;

public class SetAttributes extends
		Transformation<PMap<AttributedElement<?, ?>, Object>> {

	private Attribute attribute = null;
	private PMap<? extends Object, ? extends Object> archetype2valueMap = null;
	private String semanticExpression = null;

	public SetAttributes(final Context c, final Attribute attr,
			final PMap<? extends Object, ? extends Object> archetypeValueMap) {
		super(c);
		attribute = attr;
		archetype2valueMap = archetypeValueMap;
	}

	public SetAttributes(final Context c, final Attribute attr,
			final String semanticExpression) {
		super(c);
		attribute = attr;
		this.semanticExpression = semanticExpression;
	}

	public static SetAttributes parseAndCreate(ExecuteTransformation et) {
		Attribute attr = et.matchAttribute();
		et.matchTransformationArrow();
		String semExp = et.matchSemanticExpression();
		return new SetAttributes(et.context, attr, semExp);
	}

	@Override
	protected PMap<AttributedElement<?, ?>, Object> transform() {
		if (context.phase != TransformationPhase.GRAPH) {
			return null;
		}

		if (archetype2valueMap == null) {
			archetype2valueMap = context.evaluateGReQLQuery(semanticExpression);
		}

		PMap<AttributedElement<?, ?>, Object> resultMap = Empty.orderedMap();
		for (Object archetype : archetype2valueMap.keySet()) {
			// System.out.println("sourceElement = " + sourceElement);
			// context.printMappings();
			AttributedElement<?, ?> image = context.getImg(
					attribute.getAttributedElementClass()).get(archetype);
			if (image == null) {
				String qname = attribute.getAttributedElementClass()
						.getQualifiedName();
				throw new GReTLException(context, "The source graph element '"
						+ archetype
						+ "' has no image in "
						+ Context.toGReTLVarNotation(qname,
								GReTLVariableType.IMG)
						+ " yet, so no attribute '" + attribute.getName()
						+ "' can be created!");
			}
			Object val = archetype2valueMap.get(archetype);
			resultMap = resultMap.plus(image, val);
			if (val != Undefined.UNDEFINED) {
				Object o = convertToAttributeValue(val);
				image.setAttribute(attribute.getName(), o);
			}
		}

		return resultMap;
	}

	private Object convertToAttributeValue(Object val) {
		// TODO: Implement proper conversion from GReQL result to domain
		// (Collections of records,...)
		Object result = val;
		Domain dom = attribute.getDomain();
		if (dom instanceof RecordDomain) {
			return context.getTargetGraph().createRecord((RecordDomain) dom,
					((Record) val).toPMap());
		}
		return result;
	}
}
