package de.uni_koblenz.jgralab.gretl;

import de.uni_koblenz.jgralab.schema.Domain;
import de.uni_koblenz.jgralab.schema.MapDomain;

public class CreateMapDomain extends Transformation<MapDomain> {

	private Domain keyDomain;
	private Domain valueDomain;

	public CreateMapDomain(final Context c, final Domain keyDomain,
			final Domain valueDomain) {
		super(c);
		this.keyDomain = keyDomain;
		this.valueDomain = valueDomain;
	}

	public static CreateMapDomain parseAndCreate(ExecuteTransformation et) {
		Domain kd = et.matchDomain();
		Domain vd = et.matchDomain();
		return new CreateMapDomain(et.context, kd, vd);
	}

	@Override
	protected MapDomain transform() {
		switch (context.phase) {
		case SCHEMA:
			return context.targetSchema.createMapDomain(keyDomain, valueDomain);
		case GRAPH:
			return (MapDomain) context.targetSchema.getDomain("Map<"
					+ keyDomain.getQualifiedName() + ","
					+ valueDomain.getQualifiedName() + ">");
		default:
			throw new GReTLException(context, "Unknown TransformationPhase "
					+ context.phase + "!");
		}
	}

}
