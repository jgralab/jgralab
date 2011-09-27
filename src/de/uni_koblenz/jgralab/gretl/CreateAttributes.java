package de.uni_koblenz.jgralab.gretl;

import org.pcollections.PMap;
import org.pcollections.PSequence;

import de.uni_koblenz.jgralab.gretl.CreateAttribute.AttributeSpec;
import de.uni_koblenz.jgralab.schema.Attribute;

public class CreateAttributes extends Transformation<Attribute[]> {
	private AttributeSpec[] attrSpecs;
	private String semanticExpression;
	private PMap<Object, PSequence<Object>> archetype2ValueListMap;

	protected CreateAttributes(final Context c,
			final AttributeSpec... attrSpecs) {
		super(c);
		this.attrSpecs = attrSpecs;
	}

	public CreateAttributes(final Context c,
			final PMap<Object, PSequence<Object>> archetype2ValListMap,
			final AttributeSpec... attrSpecs) {
		this(c, attrSpecs);
		this.archetype2ValueListMap = archetype2ValListMap;
	}

	public CreateAttributes(final Context c, final String semanticExpression,
			final AttributeSpec... attrSpecs) {
		this(c, attrSpecs);
		this.semanticExpression = semanticExpression;
	}

	public static CreateAttributes parseAndCreate(ExecuteTransformation et) {
		AttributeSpec[] attrSpec = et.matchAttributeSpecArray();
		et.matchTransformationArrow();
		String semanticExpression = et.matchSemanticExpression();
		return new CreateAttributes(et.context, semanticExpression, attrSpec);
	}

	@Override
	protected Attribute[] transform() {
		switch (context.phase) {
		case SCHEMA:
			int i = 0;
			Attribute[] retVal = new Attribute[attrSpecs.length];
			for (AttributeSpec as : attrSpecs) {
				retVal[i++] = new CreateAttribute(context, as, (String) null)
						.execute();
			}
			return retVal;
		case GRAPH:
			retVal = new Attribute[attrSpecs.length];
			i = 0;
			for (AttributeSpec as : attrSpecs) {
				retVal[i++] = as.aec.getAttribute(as.name);
			}
			if (archetype2ValueListMap != null) {
				new SetMultipleAttributes(context, archetype2ValueListMap,
						retVal).execute();
			} else {
				new SetMultipleAttributes(context, semanticExpression, retVal)
						.execute();
			}
			return retVal;
		default:
			throw new GReTLException(context, "Unknown TransformationPhase "
					+ context.phase + "!");
		}
	}
}
