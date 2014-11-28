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

import org.pcollections.Empty;
import org.pcollections.PSet;

import de.uni_koblenz.jgralab.Vertex;
import de.uni_koblenz.jgralab.gretl.Context.TransformationPhase;
import de.uni_koblenz.jgralab.schema.VertexClass;

public class CreateVertices extends Transformation<PSet<? extends Vertex>> {

	private PSet<? extends Object> archetypes = null;
	private String semanticExpression = null;
	private VertexClass vertexClass = null;

	public CreateVertices(final Context c, final VertexClass vertexClass,
			final PSet<? extends Object> archetypes) {
		super(c);
		this.vertexClass = vertexClass;
		this.archetypes = archetypes;
	}

	public CreateVertices(final Context c, final VertexClass vertexClass,
			final String semExp) {
		super(c);
		this.vertexClass = vertexClass;
		semanticExpression = semExp;
	}

	public static CreateVertices parseAndCreate(final ExecuteTransformation et) {
		VertexClass vc = et.matchVertexClass();
		et.matchTransformationArrow();
		String semExp = et.matchSemanticExpression();
		return new CreateVertices(et.context, vc, semExp);
	}

	@Override
	protected PSet<? extends Vertex> transform() {
		if (context.phase != TransformationPhase.GRAPH) {
			return null;
		}

		if (archetypes == null) {
			archetypes = context.evaluateGReQLQuery(semanticExpression);
		}

		PSet<Vertex> result = Empty.set();
		for (Object arch : archetypes) {
			Vertex img = context.targetGraph.createVertex(vertexClass);
			result = result.plus(img);
			// System.out.println(newVertex);
			context.addMapping(vertexClass, arch, img);
		}
		return result;
	}

}
