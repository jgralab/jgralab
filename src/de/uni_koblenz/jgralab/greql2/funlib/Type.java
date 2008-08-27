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
import de.uni_koblenz.jgralab.schema.QualifiedName;
import de.uni_koblenz.jgralab.BooleanGraphMarker;
import de.uni_koblenz.jgralab.Graph;
import de.uni_koblenz.jgralab.GraphElement;

/**
 * Returns the type of the given attributed element.
 * 
 * <dl>
 * <dt><b>GReQL-signature</b></dt>
 * <dd><code>ATTRIBUTEDELEMENTCLASS Type(ae:ATTRIBUTEDELEMENT)</code></dd>
 * <dd>&nbsp;</dd>
 * </dl>
 * <dl>
 * <dt></dt>
 * <dd>
 * <dl>
 * <dt><b>Parameters:</b></dt>
 * <dd><code>ae</code> - attributed element to return type for</dd>
 * <dt><b>Returns:</b></dt>
 * <dd>the type of the given attributed element as attributed element class</dd>
 * <dd><code>Null</code> if one of the parameters is <code>Null</code></dd>
 * </dl>
 * </dd>
 * </dl>
 * 
 * @author Daniel Bildhauer <dbildh@uni-koblenz.de> Summer 2006, Diploma Thesis
 * 
 */

/*
 * Calculates the type of the given vertex or edge
 * 
 * @param graphelem the Graphelement (edge or vertex) to calculate the typename
 * of @return the type of the given vertex or edge @author Daniel Bildhauer
 * <dbildh@uni-koblenz.de> Summer 2006, Diploma Thesis
 * 
 */

public class Type implements Greql2Function {

	public JValue evaluate(Graph graph, BooleanGraphMarker subgraph,
			JValue[] arguments) throws EvaluateException {
		GraphElement elem = null;
		try {
			if (arguments[0].isVertex())
				elem = arguments[0].toVertex();
			else if (arguments[0].isEdge())
				elem = arguments[0].toEdge();
			else if (arguments[0].isString()) {
				return new JValue(graph.getSchema().getAttributedElementClass(
						new QualifiedName(arguments[0].toString())));
			}

		} catch (Exception ex) {
			throw new WrongFunctionParameterException(this, null, arguments);
		}
		return new JValue(elem.getAttributedElementClass(), elem);
	}

	public long getEstimatedCosts(ArrayList<Long> inElements) {
		return 2;
	}

	public double getSelectivity() {
		return 1;
	}

	public long getEstimatedCardinality(int inElements) {
		return 1;
	}

	public String getExpectedParameters() {
		return "(Vertex or Edge)";
	}

	@Override
	public boolean isPredicate() {
		return false;
	}
}
