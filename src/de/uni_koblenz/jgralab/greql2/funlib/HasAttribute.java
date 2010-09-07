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
import de.uni_koblenz.jgralab.greql2.exception.EvaluateException;
import de.uni_koblenz.jgralab.greql2.exception.WrongFunctionParameterException;
import de.uni_koblenz.jgralab.greql2.jvalue.JValue;
import de.uni_koblenz.jgralab.greql2.jvalue.JValueImpl;
import de.uni_koblenz.jgralab.greql2.jvalue.JValueType;
import de.uni_koblenz.jgralab.schema.AttributedElementClass;

/**
 * Checks if the given attributed element has an attribute with the given name.
 * 
 * <dl>
 * <dt><b>GReQL-signature</b></dt>
 * <dd><code>BOOL hasAttribute(ae:ATTRELEM, name:STRING)</code></dd>
 * <dd><code>BOOL hasAttribute(ae:ATTRELEMCLASS, name:STRING)</code></dd>
 * <dd>&nbsp;</dd>
 * </dl>
 * <dl>
 * <dt></dt>
 * <dd>
 * <dl>
 * <dt><b>Parameters:</b></dt>
 * <dd><code>ae</code> - attributed element or attributed element clazz to check
 * </dd>
 * <dd><code>name</code> - name of the element to check for</dd>
 * <dt><b>Returns:</b></dt>
 * <dd><code>true</code> if the given attributed element has an attribute with
 * the given name</dd>
 * <dd><code>Null</code> if one of the parameters is <code>Null</code></dd>
 * <dd><code>false</code> otherwise</dd>
 * </dl>
 * </dd>
 * </dl>
 * 
 * @author ist@uni-koblenz.de
 * 
 */

public class HasAttribute extends Greql2Function {

	{
		JValueType[][] x = {
				{ JValueType.ATTRELEM, JValueType.STRING, JValueType.BOOL },
				{ JValueType.ATTRELEMCLASS, JValueType.STRING, JValueType.BOOL } };
		signatures = x;

		description = "Returns true iff the given element or class has an attribute with the given name.";

		Category[] c = { Category.GRAPH, Category.SCHEMA_ACCESS };
		categories = c;
	}

	@Override
	public JValue evaluate(Graph graph, AbstractGraphMarker<AttributedElement> subgraph,
			JValue[] arguments) throws EvaluateException {
		AttributedElementClass clazz = null;
		switch (checkArguments(arguments)) {
		case 0:
			clazz = arguments[0].toAttributedElement()
					.getAttributedElementClass();
			break;
		case 1:
			clazz = arguments[0].toAttributedElementClass();
			break;
		default:
			throw new WrongFunctionParameterException(this, arguments);
		}
		return new JValueImpl(clazz.containsAttribute(arguments[1].toString()));
	}

	@Override
	public long getEstimatedCosts(ArrayList<Long> inElements) {
		return 2;
	}

	@Override
	public double getSelectivity() {
		return 0.1;
	}

	@Override
	public long getEstimatedCardinality(int inElements) {
		return 1;
	}
}
