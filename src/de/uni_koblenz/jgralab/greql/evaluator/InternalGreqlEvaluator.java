/*
 * JGraLab - The Java Graph Laboratory
 * 
 * Copyright (C) 2006-2014 Institute for Software Technology
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
package de.uni_koblenz.jgralab.greql.evaluator;

import de.uni_koblenz.jgralab.Graph;
import de.uni_koblenz.jgralab.greql.evaluator.fa.FiniteAutomaton;
import de.uni_koblenz.jgralab.greql.schema.GreqlVertex;
import de.uni_koblenz.jgralab.schema.GraphElementClass;
import de.uni_koblenz.jgralab.schema.Schema;

public interface InternalGreqlEvaluator {

	public Object setVariable(String varName, Object value);

	public Object getVariable(String varName);

	public Object setLocalEvaluationResult(GreqlVertex vertex, Object value);

	public Object getLocalEvaluationResult(GreqlVertex vertex);

	public Object removeLocalEvaluationResult(GreqlVertex vertex);

	public Graph getGraph();

	public Schema getSchema();

	public GraphElementClass<?, ?> getGraphElementClass(String typeName);

	/**
	 * should be called by every vertex evaluator to indicate a progress. The
	 * given value should be the ownEvaluationCosts of that VertexEvaluator.
	 * Calls the progress()-Method of the progress function this evaluator uses
	 */
	public void progress(long value);

	public void setSchema(Schema schema);

	public FiniteAutomaton setLocalAutomaton(GreqlVertex vertex,
			FiniteAutomaton value);

	public FiniteAutomaton getLocalAutomaton(GreqlVertex vertex);
}
