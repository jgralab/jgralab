package de.uni_koblenz.jgralab.gretl;

import org.pcollections.PSet;

import de.uni_koblenz.jgralab.GraphElement;
import de.uni_koblenz.jgralab.gretl.Context.TransformationPhase;

public class Delete extends InPlaceTransformation {

	private String greqlExp;
	private PSet<GraphElement> elementsToBeDeleted;

	protected Delete(Context context, String semExp) {
		super(context);
		this.greqlExp = semExp;
	}

	protected Delete(Context context, PSet<GraphElement> deletableElements) {
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

	private int deleteElements(PSet<GraphElement> j) {
		int count = 0;
		for (GraphElement ge : j) {
			if (ge.isValid()) {
				ge.delete();
				count++;
			}
		}
		return count;
	}

	public static Delete parseAndCreate(ExecuteTransformation et) {
		et.matchTransformationArrow();
		String semExp = et.matchSemanticExpression();
		return new Delete(et.context, semExp);
	}
}
