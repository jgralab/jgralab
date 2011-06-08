package de.uni_koblenz.jgralab.gretl;

import de.uni_koblenz.jgralab.schema.Domain;
import de.uni_koblenz.jgralab.schema.ListDomain;

public class CreateListDomain extends Transformation<ListDomain> {

	private Domain baseDomain;

	public CreateListDomain(final Context c, final Domain baseDomain) {
		super(c);
		this.baseDomain = baseDomain;
	}

	public static CreateListDomain parseAndCreate(ExecuteTransformation et) {
		Domain d = et.matchDomain();
		return new CreateListDomain(et.context, d);
	}

	@Override
	protected ListDomain transform() {
		switch (context.phase) {
		case SCHEMA:
			ListDomain d = context.targetSchema.createListDomain(baseDomain);
			return d;
		case GRAPH:
			return (ListDomain) context.targetSchema.getDomain("List<"
					+ baseDomain.getQualifiedName() + ">");
		default:
			throw new GReTLException(context, "Unknown TransformationPhase "
					+ context.phase + "!");
		}
	}

}
