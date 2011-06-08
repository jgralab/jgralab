package de.uni_koblenz.jgralab.gretl;

import java.util.Arrays;

import de.uni_koblenz.jgralab.gretl.Context.TransformationPhase;
import de.uni_koblenz.jgralab.schema.EnumDomain;

public class CreateEnumDomain extends Transformation<EnumDomain> {

	private String qualifiedName;
	private String[] constants;

	public CreateEnumDomain(final Context c, final String qualifiedName,
			final String... constants) {
		super(c);
		this.qualifiedName = qualifiedName;
		this.constants = constants;
	}

	public static CreateEnumDomain parseAndCreate(ExecuteTransformation et) {
		String qname = et.matchQualifiedName();
		String[] consts = et.matchIdentifierArray();
		return new CreateEnumDomain(et.context, qname, consts);
	}

	@Override
	protected EnumDomain transform() {
		if (context.phase != TransformationPhase.SCHEMA) {
			return (EnumDomain) domain(qualifiedName);
		}
		return context.getTargetSchema().createEnumDomain(qualifiedName,
				Arrays.asList(constants));
	}

}
