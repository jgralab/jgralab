package de.uni_koblenz.jgralab.gretl;

import java.util.LinkedList;
import java.util.List;

import de.uni_koblenz.jgralab.Edge;
import de.uni_koblenz.jgralab.greql2.jvalue.JValue;
import de.uni_koblenz.jgralab.greql2.jvalue.JValueImpl;
import de.uni_koblenz.jgralab.greql2.jvalue.JValueSet;
import de.uni_koblenz.jgralab.greql2.jvalue.JValueTuple;
import de.uni_koblenz.jgralab.gretl.Context.TransformationPhase;
import de.uni_koblenz.jgralab.schema.EdgeClass;
import de.uni_koblenz.jgralab.schema.VertexClass;

public class CreateEdges extends Transformation<List<? extends Edge>> {

	private JValueSet archetypes = null;
	private String semanticExpression = null;
	private EdgeClass edgeClass = null;

	public CreateEdges(final Context c, final EdgeClass edgeClass,
			final String semanticExpression) {
		super(c);
		this.semanticExpression = semanticExpression;
		this.edgeClass = edgeClass;
	}

	public CreateEdges(final Context c, final EdgeClass edgeClass,
			final JValueSet archetypes) {
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
	protected List<? extends Edge> transform() {
		if (context.phase != TransformationPhase.GRAPH) {
			return null;
		}

		if (archetypes == null) {
			archetypes = context.evaluateGReQLQuery(semanticExpression)
					.toJValueSet();
		}

		List<Edge> result = new LinkedList<Edge>();
		for (JValue trip : archetypes) {
			JValueTuple triple = trip.toJValueTuple();
			JValue edgeArch = triple.get(0);

			JValue startVertexArch = triple.get(1);
			VertexClass fromVC = edgeClass.getFrom().getVertexClass();
			JValue startVertex = context.getImg(fromVC).get(startVertexArch);
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

			JValue endVertexArch = triple.get(2);

			VertexClass toVC = edgeClass.getTo().getVertexClass();
			JValue endVertex = context.getImg(toVC).get(endVertexArch);
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

			Edge newEdge = context.targetGraph.createEdge(
					edgeClass.getM1Class(), startVertex.toVertex(),
					endVertex.toVertex());
			result.add(newEdge);
			JValue image = new JValueImpl(newEdge);
			context.addMapping(edgeClass, edgeArch, image);
		}

		return result;
	}
}
