package de.uni_koblenz.jgralab.gretl;

import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;

import org.pcollections.Empty;
import org.pcollections.PMap;
import org.pcollections.PQueue;

import de.uni_koblenz.jgralab.AttributedElement;
import de.uni_koblenz.jgralab.greql2.types.Tuple;
import de.uni_koblenz.jgralab.gretl.Context.TransformationPhase;
import de.uni_koblenz.jgralab.schema.Attribute;

public class SetMultipleAttributes extends
		Transformation<PQueue<PMap<AttributedElement, Object>>> {

	private Attribute[] attributes = null;
	private PMap<Object, Tuple> archetype2valuesMap = null;
	private String semanticExpression = null;

	public SetMultipleAttributes(Context c, String semanticExpression,
			Attribute... attrs) {
		super(c);
		attributes = attrs;
		this.semanticExpression = semanticExpression;
	}

	public SetMultipleAttributes(Context c, PMap<Object, Tuple> arch2ValuesMap,
			Attribute... attrs) {
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
	protected PQueue<PMap<AttributedElement, Object>> transform() {
		if (context.phase != TransformationPhase.GRAPH) {
			return null;
		}

		if (archetype2valuesMap == null) {
			archetype2valuesMap = context
					.evaluateGReQLQuery(semanticExpression);
		}
		PQueue<PMap<AttributedElement, Object>> retLst = Empty.queue();
		List<PMap<Object, Object>> lst = splice(archetype2valuesMap);
		for (int i = 0; i < attributes.length; i++) {
			retLst = retLst.plus(new SetAttributes(context, attributes[i], lst
					.get(i)).execute());
		}
		return retLst;
	}

	private List<PMap<Object, Object>> splice(
			PMap<Object, Tuple> arch2listOfAttrVals) {
		List<PMap<Object, Object>> out = new ArrayList<PMap<Object, Object>>(
				attributes.length);

		for (int i = 0; i < attributes.length; i++) {
			out.add(Empty.map());
		}

		for (Entry<Object, Tuple> e : arch2listOfAttrVals.entrySet()) {
			for (int i = 0; i < attributes.length; i++) {
				PMap<Object, Object> nm = out.get(i).plus(e.getKey(),
						e.getValue().get(i));
				out.set(i, nm);
			}
		}
		return out;
	}
}
