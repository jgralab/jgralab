/*
 * JGraLab - The Java Graph Laboratory
 * 
 * Copyright (C) 2006-2012 Institute for Software Technology
 *                         University of Koblenz-Landau, Germany
 *                         ist@uni-koblenz.de
 * 
 * For bug reports, documentation and further information, visit
 * 
 *                         https://github.com/jgralab/jgralab
 * 
 * This program is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as published by the
 * Free Software Foundation; either version 3 of the License, or (at your
 * option) any later version.
 * 
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General
 * Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, see <http://www.gnu.org/licenses>.
 * 
 * Additional permission under GNU GPL version 3 section 7
 * 
 * If you modify this Program, or any covered work, by linking or combining
 * it with Eclipse (or a modified version of that program or an Eclipse
 * plugin), containing parts covered by the terms of the Eclipse Public
 * License (EPL), the licensors of this Program grant you additional
 * permission to convey the resulting work.  Corresponding Source for a
 * non-source form of such a combination shall include the source code for
 * the parts of JGraLab used as well as that of the covered work.
 */
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
