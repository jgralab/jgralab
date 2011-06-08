package de.uni_koblenz.jgralab.gretl;

import de.uni_koblenz.jgralab.Edge;
import de.uni_koblenz.jgralab.Vertex;
import de.uni_koblenz.jgralab.greql2.jvalue.JValue;
import de.uni_koblenz.jgralab.greql2.jvalue.JValueMap;
import de.uni_koblenz.jgralab.gretl.Context.TransformationPhase;

public class Delete extends InPlaceTransformation {

	private String greqlExp;
	private JValue elementsToBeDeleted;

	protected Delete(Context context, String semExp) {
		super(context);
		this.greqlExp = semExp;
	}

	protected Delete(Context context, JValue deletableElements) {
		super(context);
		this.elementsToBeDeleted = deletableElements;
	}

	@Override
	protected Integer transform() {
		if (context.getPhase() == TransformationPhase.SCHEMA) {
			throw new GReTLException(
					"Huzza! SCHEMA phase in InPlaceTransformatio?!?");
		}

		if (elementsToBeDeleted == null) {
			elementsToBeDeleted = context.evaluateGReQLQuery(greqlExp);
		}

		int deleteCount = deleteElements(elementsToBeDeleted);

		// No side-effects with multiple evaluations!
		elementsToBeDeleted = null;

		return deleteCount;
	}

	private int deleteElements(JValue j) {
		if (j.isVertex()) {
			Vertex v = j.toVertex();
			if (v.isValid()) {
				v.delete();
				return 1;
			} else {
				return 0;
			}
		}
		if (j.isEdge()) {
			Edge e = j.toEdge();
			if (e.isValid()) {
				e.delete();
				return 1;
			} else {
				return 0;
			}
		}
		if (j.isCollection()) {
			int deleteCount = 0;
			for (JValue member : j.toCollection()) {
				deleteCount += deleteElements(member);
			}
			return deleteCount;
		}
		if (j.isMap()) {
			JValueMap m = j.toJValueMap();
			return deleteElements(m.keySet()) + deleteElements(m.values());
		}
		throw new GReTLException(context, "Don't know how to delete " + j);
	}

	public static Delete parseAndCreate(ExecuteTransformation et) {
		et.matchTransformationArrow();
		String semExp = et.matchSemanticExpression();
		return new Delete(et.context, semExp);
	}
}
