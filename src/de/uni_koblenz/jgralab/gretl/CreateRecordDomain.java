package de.uni_koblenz.jgralab.gretl;

import java.util.Arrays;

import de.uni_koblenz.jgralab.schema.RecordDomain;
import de.uni_koblenz.jgralab.schema.RecordDomain.RecordComponent;

public class CreateRecordDomain extends Transformation<RecordDomain> {

	private String qualifiedName;
	private RecordComponent[] recordComponents;

	public CreateRecordDomain(final Context c, final String qualifiedName,
			final RecordComponent... recordComponents) {
		super(c);
		this.qualifiedName = qualifiedName;
		this.recordComponents = recordComponents;
	}

	public static CreateRecordDomain parseAndCreate(ExecuteTransformation et) {
		String qname = et.matchQualifiedName();
		RecordComponent[] comps = et.matchRecordComponentArray();
		return new CreateRecordDomain(et.context, qname, comps);
	}

	@Override
	protected RecordDomain transform() {
		switch (context.phase) {
		case SCHEMA:
			return context.targetSchema.createRecordDomain(qualifiedName,
					Arrays.asList(recordComponents));
		case GRAPH:
			return (RecordDomain) context.targetSchema.getDomain(qualifiedName);
		default:
			throw new GReTLException(context, "Unknown TransformationPhase "
					+ context.phase + "!");
		}
	}

}
