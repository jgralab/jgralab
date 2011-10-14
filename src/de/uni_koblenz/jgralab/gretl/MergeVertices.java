package de.uni_koblenz.jgralab.gretl;

import java.util.Map.Entry;

import org.pcollections.PMap;
import org.pcollections.PSet;

import de.uni_koblenz.jgralab.Vertex;
import de.uni_koblenz.jgralab.gretl.Context.TransformationPhase;

public class MergeVertices extends InPlaceTransformation {

	private final String semanticExpression;
	private PMap<Vertex, PSet<Vertex>> matches;

	/**
	 * The semantic expression has to result in a map of the form (keep ->
	 * {deletes...}). keep is the canonical vertex that will be kept, and
	 * deletes are duplicates that will be merged with keep, i.e., all
	 * incidences of and vertex d in deletes will be relinked to keep.
	 * 
	 * @param context
	 * @param semExp
	 */
	public MergeVertices(Context context, String semExp) {
		super(context);
		this.semanticExpression = semExp;
	}

	public MergeVertices(Context context, PMap<Vertex, PSet<Vertex>> matches) {
		this(context, (String) null);
		this.matches = matches;
	}

	public static MergeVertices parseAndCreate(ExecuteTransformation et) {
		et.matchTransformationArrow();
		String semExp = et.matchSemanticExpression();
		return new MergeVertices(et.context, semExp);
	}

	@Override
	protected Integer transform() {
		if (context.getPhase() == TransformationPhase.SCHEMA) {
			throw new GReTLException("SCHEMA phase in InPlaceTransformatio?!?");
		}

		if (matches == null) {
			matches = context.evaluateGReQLQuery(semanticExpression);
		}
		int count = 0;
		for (Entry<Vertex, PSet<Vertex>> e : matches.entrySet()) {
			Vertex keep = e.getKey();
			if (!keep.isValid()) {
				// This is ok, because excluding this situation is damn hard in
				// GReQL.
				continue;
			}
			PSet<Vertex> deletes = e.getValue();
			if (deletes.size() > 0) {
				count++;
			}
			for (Vertex del : deletes) {
				if (del == keep) {
					throw new GReTLException(context, keep
							+ " should be both kept and deleted!");
				}
				relinkIncidences(del, keep);
				del.delete();
			}
		}

		// Be side effect free
		matches = null;

		return count;
	}
}
