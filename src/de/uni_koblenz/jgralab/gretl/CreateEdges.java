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
import org.pcollections.PSet;

import de.uni_koblenz.jgralab.Edge;
import de.uni_koblenz.jgralab.Vertex;
import de.uni_koblenz.jgralab.greql2.types.Tuple;
import de.uni_koblenz.jgralab.gretl.Context.TransformationPhase;
import de.uni_koblenz.jgralab.schema.EdgeClass;
import de.uni_koblenz.jgralab.schema.VertexClass;

public class CreateEdges extends Transformation<PSet<? extends Edge>> {

	private PSet<Tuple> archetypes = null;
	private String semanticExpression = null;
	private EdgeClass edgeClass = null;

	public CreateEdges(final Context c, final EdgeClass edgeClass,
			final String semanticExpression) {
		super(c);
		this.semanticExpression = semanticExpression;
		this.edgeClass = edgeClass;
	}

	public CreateEdges(final Context c, final EdgeClass edgeClass,
			final PSet<Tuple> archetypes) {
		super(c);
		this.archetypes = archetypes;
		this.edgeClass = edgeClass;
	}

	public static CreateEdges parseAndCreate(ExecuteTransformation et) {
		EdgeClass ec = et.matchEdgeClass();
		et.matchTransformationArrow();
		String semanticExpression = et.matchSemanticExpression();
		return new CreateEdges(et.context, ec, semanticExpression);
	}

	@Override
	protected PSet<? extends Edge> transform() {
		if (context.phase != TransformationPhase.GRAPH) {
			return null;
		}

		if (archetypes == null) {
			archetypes = context.evaluateGReQLQuery(semanticExpression);
		}

		PSet<Edge> result = Empty.set();
		for (Tuple trip : archetypes) {
			Object arch = trip.get(0);

			Object startVertexArch = trip.get(1);
			VertexClass fromVC = edgeClass.getFrom().getVertexClass();
			Vertex startVertex = (Vertex) context.getImg(fromVC).get(
					startVertexArch);
			if (startVertex == null) {
				context.printImgMappings();
				throw new GReTLException(context, "No startVertex for a new '"
						+ edgeClass.getQualifiedName()
						+ "' instance! Couldn't fetch image of '"
						+ startVertexArch
						+ "' in "
						+ Context.toGReTLVarNotation(fromVC.getQualifiedName(),
								Context.GReTLVariableType.IMG) + ".");
			}

			Object endVertexArch = trip.get(2);
			VertexClass toVC = edgeClass.getTo().getVertexClass();
			Vertex endVertex = (Vertex) context.getImg(toVC).get(endVertexArch);
			if (endVertex == null) {
				context.printImgMappings();
				throw new GReTLException(context, "No endVertex for a new '"
						+ edgeClass.getQualifiedName()
						+ "' instance! Couldn't fetch image of '"
						+ endVertexArch
						+ "' in "
						+ Context.toGReTLVarNotation(toVC.getQualifiedName(),
								Context.GReTLVariableType.IMG) + ".");
			}

			Edge img = context.targetGraph.createEdge(edgeClass, startVertex,
					endVertex);
			result = result.plus(img);
			context.addMapping(edgeClass, arch, img);
		}

		return result;
	}
}
