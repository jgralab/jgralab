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

import java.util.ArrayList;

import de.uni_koblenz.jgralab.BooleanGraphMarker;
import de.uni_koblenz.jgralab.Graph;
import de.uni_koblenz.jgralab.greql2.exception.EvaluateException;
import de.uni_koblenz.jgralab.greql2.exception.WrongFunctionParameterException;
import de.uni_koblenz.jgralab.greql2.jvalue.JValue;
import de.uni_koblenz.jgralab.greql2.jvalue.JValueType;
import de.uni_koblenz.jgralab.schema.AttributedElementClass;
import de.uni_koblenz.jgralab.schema.Schema;

/**
 * Checks if the first given type is a subtype of the second given type.
 *
 * <dl>
 * <dt><b>GReQL-signature</b></dt>
 * <dd><code>BOOLEAN isA(type:STRING, supertype:STRING)</code></dd>
 * <dd><code>BOOLEAN isA(typeA:ATTRIBUTEDELEMENTCLASS, supertype:STRING)</code></dd>
 * <dd><code>BOOLEAN isA(type:STRING, supertypeA:ATTRIBUTEDELEMENTCLASS)</code></dd>
 * <dd>
 * <code>BOOLEAN isA(typeA:ATTRIBUTEDELEMENTCLASS, supertypeA:ATTRIBUTEDELEMENTCLASS)</code>
 * </dd>
 * <dd>&nbsp;</dd>
 * </dl>
 * <dl>
 * <dt></dt>
 * <dd>
 * <dl>
 * <dt><b>Parameters:</b></dt>
 * <dd><code>type</code> - string representation of the type to check</dd>
 * <dd><code>supertype</code> - string representation of the potential supertype
 * </dd>
 * <dd><code>typeA</code> - type to check</dd>
 * <dd><code>supertypeA</code> - potential supertype</dd>
 * <dt><b>Returns:</b></dt>
 * <dd><code>true</code> if the first given type is a subtype of the second
 * given type</dd>
 * <dd><code>Null</code> if one of the given parameters is <code>Null</code></dd>
 * <dd><code>false</code> otherwise</dd>
 * </dl>
 * </dd>
 * </dl>
 *
 * @author ist@uni-koblenz.de
 *
 */

public class IsA extends AbstractGreql2Function {
	{
		JValueType[][] x = { { JValueType.STRING, JValueType.STRING },
				{ JValueType.STRING, JValueType.ATTRIBUTEDELEMENTCLASS },
				{ JValueType.ATTRIBUTEDELEMENTCLASS, JValueType.STRING } };
		signatures = x;
	}

	public JValue evaluate(Graph graph, BooleanGraphMarker subgraph,
			JValue[] arguments) throws EvaluateException {
		String s1 = null, s2 = null;
		AttributedElementClass aec1 = null, aec2 = null;
		switch (checkArguments(arguments)) {
		case 0:
			s1 = arguments[0].toString();
			s2 = arguments[1].toString();
			break;
		case 1:
			s1 = arguments[0].toString();
			aec2 = arguments[1].toAttributedElementClass();
			break;
		case 2:
			aec1 = arguments[0].toAttributedElementClass();
			aec2 = arguments[1].toAttributedElementClass();
			break;
		default:
			throw new WrongFunctionParameterException(this, null, arguments);
		}
		if (s1 != null || s2 != null) {
			Schema schema = graph.getGraphClass().getSchema();
			if (s1 != null) {
				aec1 = schema.getAttributedElementClass(s1);
			}
			if (s2 != null) {
				aec2 = schema.getAttributedElementClass(s2);
			}
		}
		return new JValue(aec1.isSubClassOf(aec2));
	}

	public long getEstimatedCosts(ArrayList<Long> inElements) {
		return 5;
	}

	public double getSelectivity() {
		return 1;
	}

	public long getEstimatedCardinality(int inElements) {
		return 1;
	}

}
