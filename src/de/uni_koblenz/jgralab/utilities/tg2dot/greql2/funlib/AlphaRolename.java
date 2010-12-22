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

package de.uni_koblenz.jgralab.utilities.tg2dot.greql2.funlib;

import java.util.ArrayList;

import de.uni_koblenz.jgralab.AttributedElement;
import de.uni_koblenz.jgralab.Graph;
import de.uni_koblenz.jgralab.graphmarker.AbstractGraphMarker;
import de.uni_koblenz.jgralab.greql2.exception.EvaluateException;
import de.uni_koblenz.jgralab.greql2.exception.WrongFunctionParameterException;
import de.uni_koblenz.jgralab.greql2.funlib.Greql2Function;
import de.uni_koblenz.jgralab.greql2.jvalue.JValue;
import de.uni_koblenz.jgralab.greql2.jvalue.JValueImpl;
import de.uni_koblenz.jgralab.greql2.jvalue.JValueType;
import de.uni_koblenz.jgralab.schema.AttributedElementClass;
import de.uni_koblenz.jgralab.schema.EdgeClass;

/**
 * Returns the alpha rolename of the given edge or edge class.
 * 
 * <dl>
 * <dt><b>GReQL-signatures</b></dt>
 * <dd><code>STRING alphaRolename(ae:EDGE)</code></dd>
 * <dd>&nbsp;</dd>
 * </dl>
 * <dl>
 * <dt></dt>
 * <dd>
 * <dl>
 * <dt><b>Parameters:</b></dt>
 * <dd><code>ae</code> - edge or edge class to return the alpha role name for</dd>
 * <dt><b>Returns:</b></dt>
 * <dd>The alpha role name of the given edge or edge class as String</dd>
 * <dd><code>Null</code> if one of the parameters is <code>Null</code></dd>
 * </dl>
 * </dd>
 * </dl>
 * 
 * @author ist@uni-koblenz.de
 * 
 */
public class AlphaRolename extends Greql2Function {
	{
		JValueType[][] x = { { JValueType.ATTRELEMCLASS, JValueType.STRING },
				{ JValueType.EDGE, JValueType.STRING } };
		signatures = x;

		description = "Returns the alpha rolename of the given edge or null otherwise.";

		Category[] c = { Category.SCHEMA_ACCESS };
		categories = c;
	}

	@Override
	public JValue evaluate(Graph graph,
			AbstractGraphMarker<AttributedElement> subgraph, JValue[] arguments)
			throws EvaluateException {
		String rolename = null;
		switch (checkArguments(arguments)) {
		case 0:
			AttributedElementClass clazz = arguments[0]
					.toAttributedElementClass();
			if (clazz instanceof EdgeClass) {
				rolename = getAlphaRolename((EdgeClass) clazz);
			}
			break;
		case 1:
			rolename = getAlphaRolename((EdgeClass) arguments[0].toEdge()
					.getAttributedElementClass());
			break;
		default:
			throw new WrongFunctionParameterException(this, arguments);
		}

		return new JValueImpl(rolename);
	}

	/**
	 * Returns the alpha role name of the given EdgeClass.
	 * 
	 * @param edgeClass
	 *            EdgeClass of which the alpha role name is requested.
	 * @return The alpha role name.
	 */
	private String getAlphaRolename(EdgeClass edgeClass) {
		String rolename = edgeClass.getFrom().getRolename();
		rolename = rolename == null ? "" : rolename;
		return rolename;
	}

	@Override
	public long getEstimatedCosts(ArrayList<Long> inElements) {
		return 2;
	}

	@Override
	public double getSelectivity() {
		return 1;
	}

	@Override
	public long getEstimatedCardinality(int inElements) {
		return 1;
	}

}
