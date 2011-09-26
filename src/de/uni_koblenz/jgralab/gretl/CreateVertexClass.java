package de.uni_koblenz.jgralab.gretl;

import org.pcollections.PSet;

import de.uni_koblenz.jgralab.schema.VertexClass;

public class CreateVertexClass extends Transformation<VertexClass> {

	protected String qualifiedName = null;
	private PSet<Object> archetypes = null;
	private String semanticExpression = null;

	protected CreateVertexClass(final Context c, final String qualifiedName) {
		super(c);
		this.qualifiedName = qualifiedName;
	}

	public CreateVertexClass(final Context c, final String qualifiedName,
			final PSet<Object> archetypes) {
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
