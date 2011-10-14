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

			Edge img = context.targetGraph.createEdge(edgeClass.getM1Class(),
					startVertex, endVertex);
			result = result.plus(img);
			context.addMapping(edgeClass, arch, img);
		}

		return result;
	}
}
