/*
 * JGraLab - The Java graph laboratory
 * (c) 2006-2008 Institute for Software Technology
 *               University of Koblenz-Landau, Germany
 *
 *               ist@uni-koblenz.de
 *
 * Please report bugs to http://serres.uni-koblenz.de/bugzilla
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */

package de.uni_koblenz.jgralab.greql2.evaluator.vertexeval;

import de.uni_koblenz.jgralab.greql2.evaluator.GreqlEvaluator;
import de.uni_koblenz.jgralab.greql2.evaluator.fa.DFA;
import de.uni_koblenz.jgralab.greql2.funlib.Greql2Function;

/**
 * Abstract baseclass for all regular pathsearches, that are PathExistence,
 * ForwardVertexSet and BackwardVertexSet
 * 
 * @author ist@uni-koblenz.de
 * 
 */
public abstract class PathSearchEvaluator extends VertexEvaluator {

	/**
	 * The DFA used for PathSearch
	 */
	protected DFA searchAutomaton;

	/**
	 * this is the GReQL-Function which evaluates the pathexistence
	 */
	protected Greql2Function function;

	public PathSearchEvaluator(GreqlEvaluator eval) {
		super(eval);
		searchAutomaton = null;
	}
}
