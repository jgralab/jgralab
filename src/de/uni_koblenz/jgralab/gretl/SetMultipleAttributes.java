package de.uni_koblenz.jgralab.gretl;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import de.uni_koblenz.jgralab.AttributedElement;
import de.uni_koblenz.jgralab.greql2.jvalue.JValue;
import de.uni_koblenz.jgralab.greql2.jvalue.JValueMap;
import de.uni_koblenz.jgralab.gretl.Context.TransformationPhase;
import de.uni_koblenz.jgralab.schema.Attribute;

public class SetMultipleAttributes extends
		Transformation<List<Map<AttributedElement, Object>>> {

	private Attribute[] attributes = null;
	private JValueMap archetype2valueMap = null;
	private String semanticExpression = null;

	public SetMultipleAttributes(Context c, String semanticExpression,
			Attribute... attrs) {
		super(c);
		attributes = attrs;
		this.semanticExpression = semanticExpression;
	}

	public SetMultipleAttributes(Context c, JValueMap arch2ValueMap,
			Attribute... attrs) {
		super(c);
		attributes = attrs;
		archetype2valueMap = arch2ValueMap;
	}

	public static SetMultipleAttributes parseAndCreate(ExecuteTransformation et) {
		Attribute[] attrs = et.matchAttributeArray();
		et.matchTransformationArrow();
		String semExp = et.matchSemanticExpression();
		return new SetMultipleAttributes(et.context, semExp, attrs);
	}

	@Override
	protected List<Map<AttributedElement, Object>> transform() {
		if (context.phase != TransformationPhase.GRAPH) {
			return null;
		}

		if (archetype2valueMap == null) {
			archetype2valueMap = context.evaluateGReQLQuery(semanticExpression)
					.toJValueMap();
		}
		List<Map<AttributedElement, Object>> retLst = new LinkedList<Map<AttributedElement, Object>>();
		ArrayList<JValueMap> lst = splice(archetype2valueMap);
		for (int i = 0; i < attributes.length; i++) {
			retLst.add(new SetAttributes(context, attributes[i], lst.get(i))
					.execute());
		}
		return retLst;
	}

	private ArrayList<JValueMap> splice(JValueMap archetype2valueMap2) {
		ArrayList<JValueMap> funs = new ArrayList<JValueMap>();
		for (@SuppressWarnings("unused")
		Attribute attribute : attributes) {
			funs.add(new JValueMap(archetype2valueMap2.size()));
		}
		for (Entry<JValue, JValue> e : archetype2valueMap2.entrySet()) {
			for (int i = 0; i < attributes.length; i++) {
				funs.get(i)
						.put(e.getKey(), e.getValue().toJValueTuple().get(i));
			}
		}
		return funs;
	}
}
