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

import de.uni_koblenz.jgralab.greql2.exception.EvaluateException;
import de.uni_koblenz.jgralab.greql2.exception.WrongFunctionParameterException;
import de.uni_koblenz.jgralab.greql2.jvalue.JValue;
import de.uni_koblenz.jgralab.schema.AttributedElementClass;
import de.uni_koblenz.jgralab.schema.GraphClass;
import de.uni_koblenz.jgralab.schema.QualifiedName;
import de.uni_koblenz.jgralab.schema.Schema;
import de.uni_koblenz.jgralab.BooleanGraphMarker;
import de.uni_koblenz.jgralab.Graph;

/**
 * Checks if the first given type is a subtype of the second given type.
 * 
 * <dl>
 * <dt><b>GReQL-signature</b></dt>
 * <dd><code>BOOLEAN isA(type:STRING, supertype:STRING)</code></dd>
 * <dd><code>BOOLEAN isA(typeA:ATTRIBUTEDELEMENTCLASS, supertype:STRING)</code></dd>
 * <dd><code>BOOLEAN isA(type:STRING, supertypeA:ATTRIBUTEDELEMENTCLASS)</code></dd>
 * <dd><code>BOOLEAN isA(typeA:ATTRIBUTEDELEMENTCLASS, supertypeA:ATTRIBUTEDELEMENTCLASS)</code></dd>
 * <dd>&nbsp;</dd>
 * </dl>
 * <dl>
 * <dt></dt>
 * <dd>
 * <dl>
 * <dt><b>Parameters:</b></dt>
 * <dd><code>type</code> - string representation of the type to check</dd>
 * <dd><code>supertype</code> - string representation of the potential
 * supertype</dd>
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
 * @author Daniel Bildhauer <dbildh@uni-koblenz.de> Summer 2006, Diploma Thesis
 * 
 */

public class IsA implements Greql2Function {

	public JValue evaluate(Graph graph, BooleanGraphMarker subgraph,
			JValue[] arguments) throws EvaluateException {
		try {
			if (arguments.length >= 1) {
				AttributedElementClass type;
				Schema schema = null;
				GraphClass graphClass;
				AttributedElementClass supertype;
				if (arguments[0].isString()) {
					graphClass = (GraphClass) graph.getAttributedElementClass();
					schema = graphClass.getSchema();
					type = schema.getAttributedElementClass(new QualifiedName(
							arguments[0].toString()));
				} else {
					type = arguments[0].toAttributedElementClass();
				}
				if (arguments[1].isString()) {
					if (schema == null) {
						graphClass = (GraphClass) graph
								.getAttributedElementClass();
						schema = graphClass.getSchema();
					}
					supertype = schema
							.getAttributedElementClass(new QualifiedName(
									arguments[1].toString()));
				} else {
					supertype = arguments[1].toAttributedElementClass();
				}
				return new JValue(type.isSubClassOf(supertype));
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
		return true;
	}

}
