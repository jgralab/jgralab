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

import java.util.ArrayList;

import de.uni_koblenz.jgralab.greql2.exception.EvaluateException;
import de.uni_koblenz.jgralab.greql2.exception.WrongFunctionParameterException;
import de.uni_koblenz.jgralab.greql2.jvalue.JValue;
import de.uni_koblenz.jgralab.greql2.jvalue.JValuePathSystem;
import de.uni_koblenz.jgralab.BooleanGraphMarker;
import de.uni_koblenz.jgralab.Graph;
import de.uni_koblenz.jgralab.Vertex;

/**
 * Returns the parent-vertex of the given vertex in the given pathsystem. The
 * parent-vertex of a vertex, is the vertex that connects the vertex with an
 * incoming edge.
 * 
 * <dl>
 * <dt><b>GReQL-signature</b></dt>
 * <dd><code>VERTEX parent(ps:PATHSYSTEM, v:VERTEX)</code></dd>
 * <dd>&nbsp;</dd>
 * </dl>
 * <dl>
 * <dt></dt>
 * <dd>
 * <dl>
 * <dt><b>Parameters:</b></dt>
 * <dd><code>ps</code> - pathsystem to work with</dd>
 * <dd><code>v</code> - vertex to calculate the parent for</dd>
 * <dt><b>Returns:</b></dt>
 * <dd>the vertex that is the parent of the given vertex.</dd>
 * <dd><code>Null</code> if the given vertex is the root of the pathsystem or
 * one of the parameters is <code>Null</code></dd>
 * </dl>
 * </dd>
 * </dl>
 * 
 * @author Daniel Bildhauer <dbildh@uni-koblenz.de> Summer 2006, Diploma Thesis
 * 
 */

public class Parent implements Greql2Function {

	public JValue evaluate(Graph graph, BooleanGraphMarker subgraph,
			JValue[] arguments) throws EvaluateException {
		Vertex vertex;
		JValuePathSystem pathSystem;
		try {
			pathSystem = arguments[0].toPathSystem();
			vertex = arguments[1].toVertex();
		} catch (Exception ex) {
			throw new WrongFunctionParameterException(this, null, arguments);
		}
		return pathSystem.parent(vertex);
	}

	public int getEstimatedCosts(ArrayList<Integer> inElements) {
		return 5;
	}

	public double getSelectivity() {
		return 1;
	}

	public int getEstimatedCardinality(int inElements) {
		return 1;
	}

	public String getExpectedParameters() {
		return "(PathSystem, Vertex)";
	}

	@Override
	public boolean isPredicate() {
		return false;
	}
}
