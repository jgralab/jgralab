package de.uni_koblenz.jgralab.gretl;

import de.uni_koblenz.jgralab.gretl.Context.TransformationPhase;
import de.uni_koblenz.jgralab.schema.VertexClass;

public class CreateAbstractVertexClass extends CreateVertexClass {

	public CreateAbstractVertexClass(final Context c, final String qualifiedName) {
		super(c, qualifiedName);
	}

	public static CreateAbstractVertexClass parseAndCreate(
			ExecuteTransformation et) {
		return new CreateAbstractVertexClass(et.context,
				et.matchQualifiedName());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.uni_koblenz.jgralab.gretl.CreateVertexClass#transform()
	 */
	@Override
	protected VertexClass transform() {
		if (context.phase == TransformationPhase.SCHEMA) {
			VertexClass vc = super.transform();
			vc.setAbstract(true);
		}
		return vc(qualifiedName);
	}

}
