package de.uni_koblenz.jgralab.gretl;

import java.lang.reflect.Constructor;
import java.util.Map;

import org.pcollections.Empty;
import org.pcollections.PMap;

import de.uni_koblenz.jgralab.AttributedElement;
import de.uni_koblenz.jgralab.greql2.types.Record;
import de.uni_koblenz.jgralab.gretl.Context.GReTLVariableType;
import de.uni_koblenz.jgralab.gretl.Context.TransformationPhase;
import de.uni_koblenz.jgralab.schema.Attribute;
import de.uni_koblenz.jgralab.schema.Domain;
import de.uni_koblenz.jgralab.schema.RecordDomain;

public class SetAttributes extends
		Transformation<PMap<AttributedElement, Object>> {

	private Attribute attribute = null;
	private PMap<Object, Object> archetype2valueMap = null;
	private String semanticExpression = null;

	public SetAttributes(final Context c, final Attribute attr,
			final PMap<Object, Object> archetypeValueMap) {
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
	protected PMap<AttributedElement, Object> transform() {
		if (context.phase != TransformationPhase.GRAPH) {
			return null;
		}

		if (archetype2valueMap == null) {
			archetype2valueMap = context.evaluateGReQLQuery(semanticExpression);
		}

		PMap<AttributedElement, Object> resultMap = Empty.orderedMap();
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
			resultMap = resultMap.plus(image, val);
			if (val != null) {
				Object o = convertToAttributeValue(val);
				image.setAttribute(attribute.getName(), o);
			}
		}

		return resultMap;
	}

	private Object convertToAttributeValue(Object val) {
		// TODO: Implement proper conversion from GReQL result to domain
		// (records, Collections of records/enums,...)
		Object result = val;
		Domain dom = attribute.getDomain();
		if (dom instanceof RecordDomain) {
			Record greqlRec = (Record) val;
			RecordDomain rd = (RecordDomain) dom;
			Class<?> rClass = rd.getM1Class();
			try {
				Constructor<?> recConstr = rClass.getConstructor(Map.class);
				result = recConstr.newInstance(greqlRec.toPMap());
			} catch (Exception e) {
				throw new GReTLException(context, "Conversion of " + val
						+ " from " + val.getClass() + " to " + rd + " failed.",
						e);
			}
		}
		return result;
	}

}
