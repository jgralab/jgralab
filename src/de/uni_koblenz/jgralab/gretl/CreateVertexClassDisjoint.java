package de.uni_koblenz.jgralab.gretl;

import java.util.LinkedList;
import java.util.List;

import de.uni_koblenz.jgralab.greql2.jvalue.JValueSet;
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
			JValueSet archetypes = context.evaluateGReQLQuery(semExp)
					.toCollection().toJValueSet();
			// Remove already existing archetypes
			archetypes.removeAll(context.getImg(newVC).keySet());
			new CreateVertices(context, newVC, archetypes).execute();
		}
		return newVC;
	}
}
