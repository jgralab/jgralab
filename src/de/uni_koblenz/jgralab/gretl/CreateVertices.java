package de.uni_koblenz.jgralab.gretl;

import org.pcollections.Empty;
import org.pcollections.PSet;

import de.uni_koblenz.jgralab.Vertex;
import de.uni_koblenz.jgralab.gretl.Context.TransformationPhase;
import de.uni_koblenz.jgralab.schema.VertexClass;

public class CreateVertices extends Transformation<PSet<? extends Vertex>> {

	private PSet<Object> archetypes = null;
	private String semanticExpression = null;
	private VertexClass vertexClass = null;

	public CreateVertices(final Context c, final VertexClass vertexClass,
			final PSet<Object> archetypes) {
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
