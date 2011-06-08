package de.uni_koblenz.jgralab.gretl;

import de.uni_koblenz.jgralab.gretl.Context.TransformationPhase;
import de.uni_koblenz.jgralab.schema.EdgeClass;

public class CreateAbstractEdgeClass extends CreateEdgeClass {

	public CreateAbstractEdgeClass(Context c, String qualifiedName,
			IncidenceClassSpec from, IncidenceClassSpec to) {
		super(c, qualifiedName, from, to);
	}

	public static CreateAbstractEdgeClass parseAndCreate(
			ExecuteTransformation et) {
		String qname = et.matchQualifiedName();
		IncidenceClassSpec from = et.matchIncidenceClassSpec();
		IncidenceClassSpec to = et.matchIncidenceClassSpec();
		return new CreateAbstractEdgeClass(et.context, qname, from, to);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.uni_koblenz.jgralab.gretl.CreateEdgeClass#transform()
	 */
	@Override
	protected EdgeClass transform() {
		if (context.phase == TransformationPhase.SCHEMA) {
			EdgeClass ec = super.transform();
			ec.setAbstract(true);
		}
		return ec(qualifiedName);
	}

}
