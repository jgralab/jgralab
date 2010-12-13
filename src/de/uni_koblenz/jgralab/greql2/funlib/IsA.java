/*
 * JGraLab - The Java Graph Laboratory
 * 
 * Copyright (C) 2006-2010 Institute for Software Technology
 *                         University of Koblenz-Landau, Germany
 *                         ist@uni-koblenz.de
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
import de.uni_koblenz.jgralab.schema.Schema;

/**
 * Checks if the first given type is a subtype of the second given type.
 * 
 * <dl>
 * <dt><b>GReQL-signature</b></dt>
 * <dd><code>BOOL isA(type:STRING, supertype:STRING)</code></dd>
 * <dd><code>BOOL isA(typeA:ATTRELEMCLASS, supertype:STRING)</code></dd>
 * <dd><code>BOOL isA(type:STRING, supertypeA:ATTRELEMCLASS)</code></dd>
 * <dd>
 * <code>BOOL isA(typeA:ATTRELEMCLASS, supertypeA:ATTRELEMCLASS)</code></dd>
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

public class IsA extends Greql2Function {
	{
		JValueType[][] x = {
				{ JValueType.STRING, JValueType.STRING, JValueType.BOOL },
				{ JValueType.STRING, JValueType.ATTRELEMCLASS, JValueType.BOOL },
				{ JValueType.ATTRELEMCLASS, JValueType.STRING, JValueType.BOOL },
				{ JValueType.ATTRELEMCLASS, JValueType.ATTRELEMCLASS,
						JValueType.BOOL } };
		signatures = x;

		description = "Returns true iff the first type is a subtype of the second type.\n"
				+ "The types may be given as attributed element class or by\n"
				+ "their qualified name given as string.";

		Category[] c = { Category.SCHEMA_ACCESS };
		categories = c;
	}

	@Override
	public JValue evaluate(Graph graph,
			AbstractGraphMarker<AttributedElement> subgraph, JValue[] arguments)
			throws EvaluateException {
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
			s2 = arguments[1].toString();
			break;
		case 3:
			aec1 = arguments[0].toAttributedElementClass();
			aec2 = arguments[1].toAttributedElementClass();
			break;
		default:
			throw new WrongFunctionParameterException(this, arguments);
		}

		Schema schema = graph.getGraphClass().getSchema();
		if (aec1 == null) {
			aec1 = schema.getAttributedElementClass(s1);
		}
		if (aec2 == null) {
			aec2 = schema.getAttributedElementClass(s2);
		}

		return new JValueImpl(aec1.isSubClassOf(aec2));
	}

	@Override
	public long getEstimatedCosts(ArrayList<Long> inElements) {
		return 5;
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
