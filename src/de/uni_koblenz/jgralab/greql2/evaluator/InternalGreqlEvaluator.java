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
package de.uni_koblenz.jgralab.greql2.evaluator;

import de.uni_koblenz.jgralab.Graph;
import de.uni_koblenz.jgralab.greql2.evaluator.vertexeval.FunctionApplicationEvaluator;
import de.uni_koblenz.jgralab.greql2.schema.Greql2Vertex;
import de.uni_koblenz.jgralab.greql2.types.TypeCollection;
import de.uni_koblenz.jgralab.schema.AttributedElementClass;
import de.uni_koblenz.jgralab.schema.Schema;

public interface InternalGreqlEvaluator {
	// TODO [greqlevaluator] create internal interface

	public Object setBoundVariable(String varName, Object value);

	public Object getBoundVariableValue(String varName);

	public Object setLocalEvaluationResult(Greql2Vertex vertex, Object value);

	public Object getLocalEvaluationResult(Greql2Vertex vertex);

	public Object removeLocalEvaluationResult(Greql2Vertex vertex);

	public Schema getSchemaOfDataGraph();

	public AttributedElementClass<?, ?> getAttributedElementClass(String name);

	public boolean haveBoundVariablesChanged();

	public void setBoundVariablesHaveChanged(boolean boundVariablesHaveChanged);

	public Graph getDataGraph();

	/**
	 * @param eval
	 *            {@link FunctionApplicationEvaluator}
	 * @return {@link TypeCollection} which is associated with <code>eval</code>
	 *         . If none is stored, <code>null</code> is returned.
	 */
	public TypeCollection getTypeCollectionForFunctionApplicationEvaluator(
			FunctionApplicationEvaluator eval);

	public TypeCollection setTypeCollectionForFunctionApplicationEvaluator(
			FunctionApplicationEvaluator faeval, TypeCollection typeArgument);
}