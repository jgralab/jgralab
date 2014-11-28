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

import org.pcollections.PSet;

import de.uni_koblenz.jgralab.schema.VertexClass;

public class CreateVertexClass extends Transformation<VertexClass> {

	protected String qualifiedName = null;
	private PSet<? extends Object> archetypes = null;
	private String semanticExpression = null;

	protected CreateVertexClass(final Context c, final String qualifiedName) {
		super(c);
		this.qualifiedName = qualifiedName;
	}

	public CreateVertexClass(final Context c, final String qualifiedName,
			final PSet<? extends Object> archetypes) {
		this(c, qualifiedName);
		this.archetypes = archetypes;
	}

	public CreateVertexClass(final Context c, final String qualifiedName,
			final String semanticExpression) {
		this(c, qualifiedName);
		this.semanticExpression = semanticExpression;
	}

	public static CreateVertexClass parseAndCreate(ExecuteTransformation et) {
		String qname = et.matchQualifiedName();
		et.matchTransformationArrow();
		String semExp = et.matchSemanticExpression();
		return new CreateVertexClass(et.context, qname, semExp);
	}

	@Override
	protected VertexClass transform() {
		switch (context.phase) {
		case SCHEMA:
			VertexClass vc = context.targetSchema.getGraphClass()
					.createVertexClass(qualifiedName);
			context.ensureMappings(vc);
			return vc;
		case GRAPH:
			VertexClass vertexClass = context.targetGraph.getGraphClass()
					.getVertexClass(qualifiedName);
			assert vertexClass != null : "Couldn't get VertexClass '"
					+ qualifiedName + "'.";
			if (archetypes != null) {
				new CreateVertices(context, vertexClass, archetypes).execute();
			} else {
				new CreateVertices(context, vertexClass, semanticExpression)
						.execute();
			}
			return vertexClass;
		default:
			throw new GReTLException(context, "Unknown TransformationPhase "
					+ context.phase + "!");
		}
	}

}
