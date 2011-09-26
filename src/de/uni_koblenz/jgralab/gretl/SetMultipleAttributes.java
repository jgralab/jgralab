package de.uni_koblenz.jgralab.gretl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import de.uni_koblenz.jgralab.AttributedElement;
import de.uni_koblenz.jgralab.gretl.Context.TransformationPhase;
import de.uni_koblenz.jgralab.schema.Attribute;

public class SetMultipleAttributes extends
		Transformation<List<Map<AttributedElement, Object>>> {

	private Attribute[] attributes = null;
	private Map<Object, List<Object>> archetype2valuesMap = null;
	private String semanticExpression = null;

	public SetMultipleAttributes(Context c, String semanticExpression,
			Attribute... attrs) {
		super(c);
		attributes = attrs;
		this.semanticExpression = semanticExpression;
	}

	public SetMultipleAttributes(Context c,
			Map<Object, List<Object>> arch2ValuesMap, Attribute... attrs) {
		super(c);
		attributes = attrs;
		archetype2valuesMap = arch2ValuesMap;
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

		if (archetype2valuesMap == null) {
			archetype2valuesMap = context
					.evaluateGReQLQuery(semanticExpression);
		}
		List<Map<AttributedElement, Object>> retLst = new LinkedList<Map<AttributedElement, Object>>();
		List<Map<Object, Object>> lst = splice(archetype2valuesMap);
		for (int i = 0; i < attributes.length; i++) {
			retLst.add(new SetAttributes(context, attributes[i], lst.get(i))
					.execute());
		}
		return retLst;
	}

	private List<Map<Object, Object>> splice(
			Map<Object, List<Object>> arch2listOfAttrVals) {
		ArrayList<Map<Object, Object>> out = new ArrayList<Map<Object, Object>>();
		for (int i = 0; i < attributes.length; i++) {
			out.add(new HashMap<Object, Object>());
		}
		for (Entry<Object, List<Object>> e : arch2listOfAttrVals.entrySet()) {
			for (int i = 0; i < attributes.length; i++) {
				out.get(i).put(e.getKey(), e.getValue().get(i));
			}
		}
		return out;
	}
}
