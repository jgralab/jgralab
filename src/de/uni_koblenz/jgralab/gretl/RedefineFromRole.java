package de.uni_koblenz.jgralab.gretl;

import de.uni_koblenz.jgralab.gretl.Context.TransformationPhase;
import de.uni_koblenz.jgralab.schema.EdgeClass;

public class RedefineFromRole extends Transformation<EdgeClass> {

	private String roleName;
	private EdgeClass edgeClass;

	public RedefineFromRole(final Context c, final EdgeClass ec,
			final String roleName) {
		super(c);
		edgeClass = ec;
		this.roleName = roleName;
	}

	public static RedefineFromRole parseAndCreate(final ExecuteTransformation et) {
		EdgeClass ec = et.matchEdgeClass();
		String role = et.matchQualifiedName();
		return new RedefineFromRole(et.context, ec, role);
	}

	@Override
	protected EdgeClass transform() {
		if (context.phase != TransformationPhase.SCHEMA) {
			return edgeClass;
		}

		edgeClass.getFrom().addRedefinedRole(roleName);

		return edgeClass;
	}

}
