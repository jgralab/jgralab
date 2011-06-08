package de.uni_koblenz.jgralab.gretl;

import java.util.LinkedList;
import java.util.List;

import de.uni_koblenz.jgralab.Vertex;
import de.uni_koblenz.jgralab.greql2.jvalue.JValue;
import de.uni_koblenz.jgralab.greql2.jvalue.JValueImpl;
import de.uni_koblenz.jgralab.greql2.jvalue.JValueSet;
import de.uni_koblenz.jgralab.gretl.Context.TransformationPhase;
import de.uni_koblenz.jgralab.schema.VertexClass;

public class CreateVertices extends Transformation<List<? extends Vertex>> {

	private JValueSet archetypes = null;
	private String semanticExpression = null;
	private VertexClass vertexClass = null;

	public CreateVertices(final Context c, final VertexClass vertexClass,
			final JValueSet archetypes) {
		super(c);
		this.vertexClass = vertexClass;
		this.archetypes = archetypes;
	}

	public CreateVertices(final Context c, final VertexClass vertexClass,
			final String semExp) {
		super(c);
		this.vertexClass = vertexClass;
		this.semanticExpression = semExp;
	}

	public static CreateVertices parseAndCreate(final ExecuteTransformation et) {
		VertexClass vc = et.matchVertexClass();
		et.matchTransformationArrow();
		String semExp = et.matchSemanticExpression();
		return new CreateVertices(et.context, vc, semExp);
	}

	@Override
	protected List<? extends Vertex> transform() {
		if (context.phase != TransformationPhase.GRAPH) {
			return null;
		}

		if (archetypes == null) {
			archetypes = context.evaluateGReQLQuery(semanticExpression)
					.toJValueSet();
		}

		List<Vertex> result = new LinkedList<Vertex>();
		for (JValue v : archetypes) {
			Vertex newVertex = context.targetGraph.createVertex(vertexClass
					.getM1Class());
			result.add(newVertex);
			// System.out.println(newVertex);
			JValue image = new JValueImpl(newVertex);
			context.addMapping(vertexClass, v, image);
		}
		return result;
	}

}
