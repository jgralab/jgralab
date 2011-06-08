package de.uni_koblenz.jgralab.gretl;

import de.uni_koblenz.jgralab.greql2.jvalue.JValueMap;
import de.uni_koblenz.jgralab.schema.Attribute;
import de.uni_koblenz.jgralab.schema.AttributedElementClass;
import de.uni_koblenz.jgralab.schema.Domain;

public class CreateAttribute extends Transformation<Attribute> {

	private AttributeSpec attrSpec;
	private String semanticExpression;
	private JValueMap archetypes;

	protected CreateAttribute(final Context c, final AttributeSpec attrSpec) {
		super(c);
		this.attrSpec = attrSpec;
	}

	public CreateAttribute(final Context c, final AttributeSpec attrSpec,
			final JValueMap archetypes) {
		this(c, attrSpec);
		this.archetypes = archetypes;
	}

	public CreateAttribute(final Context c, final AttributeSpec attrSpec,
			final String semanticExpression) {
		this(c, attrSpec);
		this.semanticExpression = semanticExpression;
	}

	public static CreateAttribute parseAndCreate(ExecuteTransformation et) {
		AttributeSpec attrSpec = et.matchAttributeSpec();
		et.matchTransformationArrow();
		String semanticExpression = et.matchSemanticExpression();
		return new CreateAttribute(et.context, attrSpec, semanticExpression);
	}

	@Override
	protected Attribute transform() {
		switch (context.phase) {
		case SCHEMA:
			attrSpec.aec.addAttribute(attrSpec.name, attrSpec.domain,
					attrSpec.defaultValue);
			Attribute attr = attrSpec.aec.getAttribute(attrSpec.name);
			// System.out.println("attr = " + attr);
			return attr;
		case GRAPH:
			Attribute attribute = attrSpec.aec.getAttribute(attrSpec.name);
			if (archetypes != null) {
				new SetAttributes(context, attribute, archetypes).execute();
			} else {
				new SetAttributes(context, attribute, semanticExpression)
						.execute();
			}
			return attribute;
		default:
			throw new GReTLException(context, "Unknown TransformationPhase "
					+ context.phase + "!");
		}
	}

	public static final class AttributeSpec {
		protected String name;
		protected AttributedElementClass aec;
		protected Domain domain;
		protected String defaultValue;

		public AttributeSpec(final AttributedElementClass attrElemClass,
				String attrName, final Domain domain, String defaultValue) {
			name = attrName;
			aec = attrElemClass;
			this.domain = domain;
			this.defaultValue = defaultValue;
		}

		public AttributeSpec(final AttributedElementClass attrElemClass,
				String attrName, final Domain domain) {
			this(attrElemClass, attrName, domain, null);
		}
	}
}
