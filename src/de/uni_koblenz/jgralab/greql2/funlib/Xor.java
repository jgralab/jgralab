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

package de.uni_koblenz.jgralab.greql2.funlib;

import java.util.ArrayList;

import de.uni_koblenz.jgralab.BooleanGraphMarker;
import de.uni_koblenz.jgralab.Graph;
import de.uni_koblenz.jgralab.greql2.exception.EvaluateException;
import de.uni_koblenz.jgralab.greql2.exception.JValueInvalidTypeException;
import de.uni_koblenz.jgralab.greql2.exception.WrongFunctionParameterException;
import de.uni_koblenz.jgralab.greql2.jvalue.JValue;
import de.uni_koblenz.jgralab.greql2.jvalue.JValueBoolean;

/**
 * Returns the result of the logical operation <code>a xor b</code>.
 * <dl>
 * <dt><b>GReQL-signature</b></dt>
 * <dd><code>BOOLEAN xor(a: BOOLEAN, b: BOOLEAN)</code></dd>
 * <dd></dd>
 * <dd>This function can be used in infix notation: <code>a xor b</code></dd>
 * </dl>
 * <dl>
 * <dt></dt>
 * <dd>
 * <dl>
 * <dt><b>Parameters:</b></dt>
 * <dd><code>a: BOOLEAN</code> - first operand</dd>
 * <dd><code>b: BOOLEAN</code> - second operand</dd>
 * <dt><b>Returns:</b></dt>
 * <dd>the result of the logical operation <code>a xor b</code> as defined
 * below:</dd>
 * <dd> <table border = "1">
 * <tr>
 * <th>a\b</th>
 * <th bgcolor="#cc996f">false</th>
 * <th bgcolor="#bababa">Null</th>
 * <th bgcolor="#92cc90">true</th>
 * </tr>
 * <tr>
 * <th bgcolor="#cc996f">false</th>
 * <td bgcolor="#ffbf8b">false</td>
 * <td bgcolor="#eeeeee">Null</td>
 * <td bgcolor="#b7ffb4">true</td>
 * </tr>
 * <tr>
 * <th bgcolor="#bababa">Null</th>
 * <td bgcolor="#eeeeee">Null</td>
 * <td bgcolor="#eeeeee">Null</td>
 * <td bgcolor="#eeeeee">Null</td>
 * </tr>
 * <tr>
 * <th bgcolor="#92cc90">true</th>
 * <td bgcolor="#b7ffb4">true</td>
 * <td bgcolor="#eeeeee">Null</td>
 * <td bgcolor="#ffbf8b">false</td>
 * </tr>
 * </table> </dd>
 * </dl>
 * </dd>
 * </dl>
 * 
 * @author Daniel Bildhauer <dbildh@uni-koblenz.de> Summer 2006, Diploma Thesis
 * 
 */

public class Xor implements Greql2Function {

	public JValue evaluate(Graph graph, BooleanGraphMarker subgraph,
			JValue[] arguments) throws EvaluateException {
		if (arguments.length != 2) {
			throw new WrongFunctionParameterException(this, null, arguments);
		}
		try {
			return JValueBoolean.xor(arguments[0], arguments[1]);
		} catch (JValueInvalidTypeException ex) {
			throw new WrongFunctionParameterException(this, null, arguments);
		}
	}

	public long getEstimatedCosts(ArrayList<Long> inElements) {
		return 2;
	}

	public double getSelectivity() {
		return 2d / 9;
	}

	public long getEstimatedCardinality(int inElements) {
		return 1;
	}

	public String getExpectedParameters() {
		return "(TrivalentBoolean)";
	}

	@Override
	public boolean isPredicate() {
		return true;
	}
}
