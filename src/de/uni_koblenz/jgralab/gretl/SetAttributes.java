package de.uni_koblenz.jgralab.gretl;

import java.lang.reflect.Constructor;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.pcollections.ArrayPMap;
import org.pcollections.PMap;

import de.uni_koblenz.jgralab.AttributedElement;
import de.uni_koblenz.jgralab.Record;
import de.uni_koblenz.jgralab.greql2.jvalue.JValue;
import de.uni_koblenz.jgralab.greql2.jvalue.JValueMap;
import de.uni_koblenz.jgralab.greql2.jvalue.JValueRecord;
import de.uni_koblenz.jgralab.gretl.Context.GReTLVariableType;
import de.uni_koblenz.jgralab.gretl.Context.TransformationPhase;
import de.uni_koblenz.jgralab.schema.Attribute;
import de.uni_koblenz.jgralab.schema.CollectionDomain;
import de.uni_koblenz.jgralab.schema.Domain;
import de.uni_koblenz.jgralab.schema.MapDomain;
import de.uni_koblenz.jgralab.schema.RecordDomain;

public class SetAttributes extends
		Transformation<Map<AttributedElement, Object>> {

	private Attribute attribute = null;
	private JValueMap archetype2valueMap = null;
	private String semanticExpression = null;

	public SetAttributes(final Context c, final Attribute attr,
			final JValueMap archetypeValueMap) {
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
			archetype2valueMap = context.evaluateGReQLQuery(semanticExpression)
					.toJValueMap();
		}

		HashMap<AttributedElement, Object> resultMap = new HashMap<AttributedElement, Object>(
				archetype2valueMap.size());
		for (JValue sourceElement : archetype2valueMap.keySet()) {
			// System.out.println("sourceElement = " + sourceElement);
			// context.printMappings();
			JValue targetElemValue = context.getImg(
					attribute.getAttributedElementClass()).get(sourceElement);
			if (targetElemValue == null) {
				String qname = attribute.getAttributedElementClass()
						.getQualifiedName();
				throw new GReTLException(context, "The source graph element '"
						+ sourceElement
						+ "' has no image in "
						+ Context.toGReTLVarNotation(qname,
								GReTLVariableType.IMG)
						+ " yet, so no attribute '" + attribute.getName()
						+ "' can be created!");
			}
			AttributedElement targetElem = targetElemValue
					.toAttributedElement();
			JValue val = archetype2valueMap.get(sourceElement);
			resultMap.put(targetElem, val);
			if (val != null) {
				Object o = null;

				o = convertJValueToAttributeValue(val);
				targetElem.setAttribute(attribute.getName(), o);
			}
		}

		return resultMap;
	}

	private Object convertJValueToAttributeValue(JValue val) {
		Domain attrDom = attribute.getDomain();
		if (attrDom instanceof RecordDomain) {
			return convertJValueRecordToRecord(val.toJValueRecord());
		} else if ((attrDom instanceof CollectionDomain)
				&& (((CollectionDomain) attrDom).getBaseDomain() instanceof RecordDomain)) {
			@SuppressWarnings("unchecked")
			Collection<Object> coll = (Collection<Object>) val.toCollection()
					.toObject();
			coll.clear();
			for (JValue i : val.toCollection()) {
				coll.add(convertJValueRecordToRecord(i.toJValueRecord()));
			}
			return coll;
		} else if (attrDom instanceof MapDomain) {
			MapDomain md = (MapDomain) attrDom;
			Domain kd = md.getKeyDomain();
			Domain vd = md.getValueDomain();
			if ((kd instanceof RecordDomain) || (vd instanceof RecordDomain)) {
				JValueMap jmap = val.toJValueMap();
				PMap<Object, Object> map = ArrayPMap.empty();
				Object k = null, v = null;
				for (Entry<JValue, JValue> e : jmap.entrySet()) {
					if (kd instanceof RecordDomain) {
						k = convertJValueRecordToRecord(e.getKey()
								.toJValueRecord());
					} else {
						k = e.getKey().toObject();
					}
					if (vd instanceof RecordDomain) {
						v = convertJValueRecordToRecord(e.getValue()
								.toJValueRecord());
					} else {
						v = e.getValue().toObject();
					}
					map = map.plus(k, v);
				}
				return map;
			}
		}
		return val.toObject();
	}

	@SuppressWarnings("unchecked")
	private Record convertJValueRecordToRecord(JValueRecord jrec) {
		// TODO (ido) implement construction via reflection
		RecordDomain rd = (RecordDomain) attribute.getDomain();
		Constructor<? extends Record> c;
		try {
			c = (Constructor<? extends Record>) rd.getM1Class().getConstructor(
					Map.class);
			Map<String, Object> compVals = jrec.toObject();
			return c.newInstance(compVals);
		} catch (Exception e) {
			throw new RuntimeException("Can't convert JValue to Record", e);
		}
	}
}
