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

import java.util.LinkedList;
import java.util.List;

import org.pcollections.PSet;

import de.uni_koblenz.jgralab.gretl.Context.TransformationPhase;
import de.uni_koblenz.jgralab.gretl.parser.TokenTypes;
import de.uni_koblenz.jgralab.schema.VertexClass;

public class CreateVertexClassDisjoint extends Transformation<VertexClass> {

	private String qualifiedName;
	private String[] semanticExpressions;

	public CreateVertexClassDisjoint(Context c, final String qualifiedName,
			String... semanticExpressions) {
		super(c);
		this.qualifiedName = qualifiedName;
		this.semanticExpressions = semanticExpressions;
	}

	public static CreateVertexClassDisjoint parseAndCreate(
			ExecuteTransformation et) {
		String qname = et.matchQualifiedName();
		List<String> semanticExps = new LinkedList<String>();
		while (et.tryMatch(TokenTypes.TRANSFORM_ARROW)) {
			et.matchTransformationArrow();
			semanticExps.add(et.matchSemanticExpression());
		}
		return new CreateVertexClassDisjoint(et.context, qname,
				semanticExps.toArray(new String[semanticExps.size()]));
	}

	@Override
	protected VertexClass transform() {
		VertexClass newVC = new CreateVertexClass(context, qualifiedName,
				"set()").execute();
		if (context.phase == TransformationPhase.SCHEMA) {
			return newVC;
		}
		for (String semExp : semanticExpressions) {
			PSet<Object> archetypes = context.evaluateGReQLQuery(semExp);
			// Remove already existing archetypes
			archetypes = archetypes.minusAll(context.getImg(newVC).keySet());
			new CreateVertices(context, newVC, archetypes).execute();
		}
		return newVC;
	}
}
