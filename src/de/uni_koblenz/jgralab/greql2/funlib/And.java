/*
 * JGraLab - The Java graph laboratory
 * (c) 2006-2009 Institute for Software Technology
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

import de.uni_koblenz.jgralab.Graph;
import de.uni_koblenz.jgralab.graphmarker.BooleanGraphMarker;
import de.uni_koblenz.jgralab.greql2.exception.EvaluateException;
import de.uni_koblenz.jgralab.greql2.jvalue.JValue;

/**
 * Returns the result of the logical operation <code>a and b</code>.
 * <dl>
 * <dt><b>GReQL-signature</b></dt>
 * <dd><code>BOOL and(a:BOOL, b:BOOL)</code></dd>
 * <dd>&nbsp;</dd>
 * <dd>This function can be used in infix notation: <code>a and b</code></dd>
 * <dd>&nbsp;</dd>
 * </dl>
 * <dl>
 * <dt></dt>
 * <dd>
 * <dl>
 * <dt><b>Parameters:</b></dt>
 * <dd><code>a: BOOL</code> - first operand</dd>
 * <dd><code>b: BOOL</code> - second operand</dd>
 * <dt><b>Returns:</b></dt>
 * <dd>the result of the logical operation <code>a and b</code> as defined
 * below:</dd>
 * <dd>&nbsp;</dd>
 * <dd>
 * <table border = "1">
 * <tr>
 * <th>a\b</th>
 * <th bgcolor="#cc996f">false</th>
 * <th bgcolor="#bababa">Null</th>
 * <th * bgcolor="#92cc90">true</th>
 * </tr>
 * <tr>
 * <th bgcolor="#cc996f">false</th>
 * <td bgcolor="#ffbf8b">false</td>
 * <td bgcolor="#ffbf8b">false</td>
 * <td bgcolor="#ffbf8b">false</td>
 * </tr>
 * <tr>
 * <th bgcolor="#bababa">Null</th>
 * <td bgcolor="#ffbf8b">false</td>
 * <td bgcolor="#eeeeee">Null</td>
 * <td bgcolor="#eeeeee">Null</td>
 * </tr>
 * <tr>
 * <th bgcolor="#92cc90">true</th>
 * <td bgcolor="#ffbf8b">false</td>
 * <td bgcolor="#eeeeee">Null</td>
 * <td bgcolor="#b7ffb4">true</td>
 * </tr>
 * </table>
 * </dd>
 * </dl>
 * </dd>
 * </dl>
 * 
 * @author ist@uni-koblenz.de
 * 
 */
public class And extends BooleanFunction {

	{
		description = "Logical operation $a\\wedge b$. \n"
				+ "Alternative usage: a and b.";
	}

	@Override
	public JValue evaluate(Graph graph, BooleanGraphMarker subgraph,
			JValue[] arguments) throws EvaluateException {
		return evaluate(arguments, BooleanOperator.AND);
	}

	@Override
	public double getSelectivity() {
		return 1d / 9;
	}

}
