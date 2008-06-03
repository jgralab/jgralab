/*
 * JGraLab - The Java graph laboratory
 * (c) 2006-2007 Institute for Software Technology
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

import de.uni_koblenz.jgralab.greql2.exception.EvaluateException;
import de.uni_koblenz.jgralab.greql2.exception.WrongFunctionParameterException;
import de.uni_koblenz.jgralab.greql2.jvalue.JValue;
import de.uni_koblenz.jgralab.greql2.jvalue.JValueSet;
import de.uni_koblenz.jgralab.schema.AttributedElementClass;
import de.uni_koblenz.jgralab.schema.GraphClass;
import de.uni_koblenz.jgralab.schema.QualifiedName;
import de.uni_koblenz.jgralab.schema.Schema;

import java.util.ArrayList;
import java.util.Iterator;

import de.uni_koblenz.jgralab.BooleanGraphMarker;
import de.uni_koblenz.jgralab.Graph;

/**
 * Returns all subtypes of the given type. The type can be given as
 * AttributedElementClass or as String which holds the typename.
 * 
 * <dl>
 * <dt><b>GReQL-signature</b></dt>
 * <dd><code>SET&lt;ATTRIBUTEDELEMENTCLASS&gt; subTypes(type:STRING)</code></dd>
 * <dd><code>SET&lt;ATTRIBUTEDELEMENTCLASS&gt; subTypes(typeA:ATTRIBUTEDELEMENTCLASS)</code></dd>
 * <dd>&nbsp;</dd>
 * </dl>
 * <dl>
 * <dt></dt>
 * <dd>
 * <dl>
 * <dt><b>Parameters:</b></dt>
 * <dd><code>type</code> - name of the type to return the subtypes for</dd>
 * <dd><code>typeA</code> - attributed element class, which is the type to
 * return the subtypes for</dd>
 * <dt><b>Returns:</b></dt>
 * <dd>a set containing all subtypes of the given type</dd>
 * <dd><code>Null</code> if one of the parameters is <code>Null</code></dd>
 * </dl>
 * </dd>
 * </dl>
 * 
 * @author Daniel Bildhauer <dbildh@uni-koblenz.de> Summer 2006, Diploma Thesis
 * 
 */

/*
 * returns a set which contains all subtypes of the given type
 * 
 * @param type a type (AttributedElementClass or String) @author Daniel
 * Bildhauer <dbildh@uni-koblenz.de> Summer 2006, Diploma Thesis
 * 
 */

public class Subtypes implements Greql2Function {

	public JValue evaluate(Graph graph, BooleanGraphMarker subgraph,
			JValue[] arguments) throws EvaluateException {
		try {
			if (arguments.length >= 1) {
				JValueSet typeSet = new JValueSet();
				AttributedElementClass type;
				if (arguments[0].isString()) {
					GraphClass graphClass = (GraphClass) graph
							.getAttributedElementClass();
					Schema schema = graphClass.getSchema();
					type = schema.getAttributedElementClass(new QualifiedName(
							arguments[0].toString()));
				} else {
					type = arguments[0].toAttributedElementClass();
				}
				Iterator<AttributedElementClass> iter = type
						.getDirectSubClasses().iterator();
				while (iter.hasNext()) {
					typeSet.add(new JValue(iter.next()));
				}
				return typeSet;
			}
			throw new WrongFunctionParameterException(this, null, arguments);
		} catch (Exception ex) {
			throw new WrongFunctionParameterException(this, null, arguments);
		}
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

	public String getExpectedParameters() {
		return "(Integer)";
	}

	@Override
	public boolean isPredicate() {
		return false;
	}
}
