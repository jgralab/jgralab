/*
 * JGraLab - The Java Graph Laboratory
 *
 * Copyright (C) 2006-2013 Institute for Software Technology
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

import org.pcollections.PMap;
import org.pcollections.PSequence;

import de.uni_koblenz.jgralab.gretl.CreateAttribute.AttributeSpec;
import de.uni_koblenz.jgralab.schema.Attribute;

public class CreateAttributes extends Transformation<Attribute[]> {
	private AttributeSpec[] attrSpecs;
	private String semanticExpression;
	private PMap<Object, PSequence<Object>> archetype2ValueListMap;

	protected CreateAttributes(final Context c,
			final AttributeSpec... attrSpecs) {
		super(c);
		this.attrSpecs = attrSpecs;
	}

	public CreateAttributes(final Context c,
			final PMap<Object, PSequence<Object>> archetype2ValListMap,
			final AttributeSpec... attrSpecs) {
		this(c, attrSpecs);
		this.archetype2ValueListMap = archetype2ValListMap;
	}

	public CreateAttributes(final Context c, final String semanticExpression,
			final AttributeSpec... attrSpecs) {
		this(c, attrSpecs);
		this.semanticExpression = semanticExpression;
	}

	public static CreateAttributes parseAndCreate(ExecuteTransformation et) {
		AttributeSpec[] attrSpec = et.matchAttributeSpecArray();
		et.matchTransformationArrow();
		String semanticExpression = et.matchSemanticExpression();
		return new CreateAttributes(et.context, semanticExpression, attrSpec);
	}

	@Override
	protected Attribute[] transform() {
		switch (context.phase) {
		case SCHEMA:
			int i = 0;
			Attribute[] retVal = new Attribute[attrSpecs.length];
			for (AttributeSpec as : attrSpecs) {
				retVal[i++] = new CreateAttribute(context, as, (String) null)
						.execute();
			}
			return retVal;
		case GRAPH:
			retVal = new Attribute[attrSpecs.length];
			i = 0;
			for (AttributeSpec as : attrSpecs) {
				retVal[i++] = as.aec.getAttribute(as.name);
			}
			if (archetype2ValueListMap != null) {
				new SetMultipleAttributes(context, archetype2ValueListMap,
						retVal).execute();
			} else {
				new SetMultipleAttributes(context, semanticExpression, retVal)
						.execute();
			}
			return retVal;
		default:
			throw new GReTLException(context, "Unknown TransformationPhase "
					+ context.phase + "!");
		}
	}
}
