/*
 * JGraLab - The Java graph laboratory
 * (c) 2006-2010 Institute for Software Technology
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

package de.uni_koblenz.jgralab.greql2.funlib;

import java.util.ArrayList;

import de.uni_koblenz.jgralab.AttributedElement;
import de.uni_koblenz.jgralab.Graph;
import de.uni_koblenz.jgralab.graphmarker.AbstractGraphMarker;
import de.uni_koblenz.jgralab.greql2.evaluator.fa.DFA;
import de.uni_koblenz.jgralab.greql2.exception.EvaluateException;
import de.uni_koblenz.jgralab.greql2.exception.WrongFunctionParameterException;
import de.uni_koblenz.jgralab.greql2.jvalue.JValue;
import de.uni_koblenz.jgralab.greql2.jvalue.JValueImpl;
import de.uni_koblenz.jgralab.greql2.jvalue.JValueType;

/**
 * Returns a DFA which recognizes he given ath expression and can be used in several other functions such as pathSystem, isReachable etc., 
 * 
 * <dl>
 * <dt><b>GReQL-signature</b></dt>
 * <dd><code>DFA pathExpr(dfa:AUTOMATON)</code></dd>
 * <dd>&nbsp;</dd>
 * <code>pathExpr(rpe)</code></dd>
 * <dd><code>rpe</code> is a regular path expression.</dd>
 * <dd>&nbsp;</dd>
 * </dl>
 * <dl>
 * <dt></dt>
 * <dd>
 * <dl>
 * <dt><b>Parameters:</b></dt>
 * <dd><code>dfa</code> - a dfa as automaton or regular path expression</dd>
 * <dt><b>Returns:</b></dt>
 * <dd>a dfa which accepts the language of the given rpe</dd>
 * </dl>
 * </dd>
 * </dl>
 * 
 * @author ist@uni-koblenz.de
 * 
 */
public class PathExpr extends Greql2Function {

	{
		JValueType[][] x = { { JValueType.AUTOMATON,
				JValueType.AUTOMATON } };
		signatures = x;

		description = "Returns a  afa which accepts the given path description.";

		Category[] c = { Category.PATHS_AND_PATHSYSTEMS_AND_SLICES };
		categories = c;
	}

	/**
	 * creates the pathsystem
	 */
	@Override
	public JValue evaluate(Graph graph,
			AbstractGraphMarker<AttributedElement> subgraph, JValue[] arguments)
			throws EvaluateException {
		DFA dfa = null;
		switch (checkArguments(arguments)) {
		case 0:
			dfa = arguments[0].toAutomaton().getDFA();
			break;
		default:
			throw new WrongFunctionParameterException(this, arguments);
		}
        return new JValueImpl(dfa);
	}


	@Override
	public long getEstimatedCosts(ArrayList<Long> inElements) {
		return 100;
	}

	@Override
	public double getSelectivity() {
		return 0.001f;
	}

	@Override
	public long getEstimatedCardinality(int inElements) {
		return 1;
	}

}
