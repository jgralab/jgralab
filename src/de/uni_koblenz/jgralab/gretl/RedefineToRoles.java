package de.uni_koblenz.jgralab.gretl;

import de.uni_koblenz.jgralab.gretl.Context.TransformationPhase;
import de.uni_koblenz.jgralab.schema.EdgeClass;

public class RedefineToRoles extends Transformation<EdgeClass> {
	private String[] roleNames;
	private EdgeClass edgeClass;

	public RedefineToRoles(final Context c, final EdgeClass ec,
			final String... roleNames) {
		super(c);
		edgeClass = ec;
		this.roleNames = roleNames;

		if (roleNames.length < 1) {
			throw new GReTLException(c, "No role name to redefine given!");
		}
	}

	public static RedefineToRoles parseAndCreate(final ExecuteTransformation et) {
		EdgeClass ec = et.matchEdgeClass();
		String[] roles = et.matchIdentifierArray();
		return new RedefineToRoles(et.context, ec, roles);
	}

	@Override
	protected EdgeClass transform() {
		if (context.phase != TransformationPhase.SCHEMA) {
			return edgeClass;
		}

		for (String roleName : roleNames) {
			new RedefineToRole(context, edgeClass, roleName).execute();
		}

		return edgeClass;
	}

}
