package de.uni_koblenz.jgralab.gretl;

import java.util.HashMap;
import java.util.Map;

import de.uni_koblenz.jgralab.AttributedElement;
import de.uni_koblenz.jgralab.gretl.Context.GReTLVariableType;
import de.uni_koblenz.jgralab.gretl.Context.TransformationPhase;
import de.uni_koblenz.jgralab.schema.Attribute;

public class SetAttributes extends
		Transformation<Map<AttributedElement, Object>> {

	private Attribute attribute = null;
	private Map<Object, Object> archetype2valueMap = null;
	private String semanticExpression = null;

	public SetAttributes(final Context c, final Attribute attr,
			final Map<Object, Object> archetypeValueMap) {
		super(c);
		attribute = attr;
		archetype2valueMap = archetypeValueMap;
	}

	public SetAttributes(final Context c, final Attribute attr,
			final String semanticExpression) {
		super(c);
		attribute = attr;
		this.semanticExpression = semanticExpression;
	}

	public static SetAttributes parseAndCreate(ExecuteTransformation et) {
		Attribute attr = et.matchAttribute();
		et.matchTransformationArrow();
		String semExp = et.matchSemanticExpression();
		return new SetAttributes(et.context, attr, semExp);
	}

	@Override
	protected Map<AttributedElement, Object> transform() {
		if (context.phase != TransformationPhase.GRAPH) {
			return null;
		}

		if (archetype2valueMap == null) {
			archetype2valueMap = context.evaluateGReQLQuery(semanticExpression);
		}

		HashMap<AttributedElement, Object> resultMap = new HashMap<AttributedElement, Object>(
				archetype2valueMap.size());
		for (Object archetype : archetype2valueMap.keySet()) {
			// System.out.println("sourceElement = " + sourceElement);
			// context.printMappings();
			AttributedElement image = context.getImg(
					attribute.getAttributedElementClass()).get(archetype);
			if (image == null) {
				String qname = attribute.getAttributedElementClass()
						.getQualifiedName();
				throw new GReTLException(context, "The source graph element '"
						+ archetype
						+ "' has no image in "
						+ Context.toGReTLVarNotation(qname,
								GReTLVariableType.IMG)
						+ " yet, so no attribute '" + attribute.getName()
						+ "' can be created!");
			}
			Object val = archetype2valueMap.get(archetype);
			resultMap.put(image, val);
			if (val != null) {
				Object o = convertToAttributeValue(val);
				image.setAttribute(attribute.getName(), o);
			}
		}

		return resultMap;
	}

	private Object convertToAttributeValue(Object val) {
		// TODO: Implement proper conversion from GReQL result to domain
		// (records, enums,...)
		return val;
	}

}
