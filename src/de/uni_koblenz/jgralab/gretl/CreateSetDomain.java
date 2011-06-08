package de.uni_koblenz.jgralab.gretl;

import de.uni_koblenz.jgralab.schema.Domain;
import de.uni_koblenz.jgralab.schema.SetDomain;

public class CreateSetDomain extends Transformation<SetDomain> {

	private Domain baseDomain;

	public CreateSetDomain(final Context c, final Domain baseDomain) {
		super(c);
		this.baseDomain = baseDomain;
	}

	public static CreateSetDomain parseAndCreate(ExecuteTransformation et) {
		Domain d = et.matchDomain();
		return new CreateSetDomain(et.context, d);
	}

	@Override
	protected SetDomain transform() {
		switch (context.phase) {
		case SCHEMA:
			SetDomain d = context.targetSchema.createSetDomain(baseDomain);
			return d;
		case GRAPH:
			return (SetDomain) context.targetSchema.getDomain("Set<"
					+ baseDomain.getQualifiedName() + ">");
		default:
			throw new GReTLException(context, "Unknown TransformationPhase "
					+ context.phase + "!");
		}
	}

}
