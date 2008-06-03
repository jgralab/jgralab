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
import de.uni_koblenz.jgralab.greql2.jvalue.JValueList;
import de.uni_koblenz.jgralab.greql2.jvalue.JValuePath;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import de.uni_koblenz.jgralab.BooleanGraphMarker;
import de.uni_koblenz.jgralab.Graph;
import de.uni_koblenz.jgralab.Vertex;

/**
 * Calculates the nodetrace of the given path. A nodetrace is a List of all
 * vertices of this path in correct order.
 * 
 * <dl>
 * <dt><b>GReQL-signature</b></dt>
 * <dd><code>LIST&lt;VERTEX&gt; nodetrace(p:PATH)</code></dd>
 * <dd>&nbsp;</dd>
 * </dl>
 * <dl>
 * <dt></dt>
 * <dd>
 * <dl>
 * <dt><b>Parameters:</b></dt>
 * <dd><code>p</code> - path to calculate the nodetrace for</dd>
 * <dt><b>Returns:</b></dt>
 * <dd>the nodetrace of the given path</dd>
 * <dd><code>Null</code> if one of the parameters is <code>Null</code></dd>
 * </dl>
 * </dd>
 * </dl>
 * 
 * @see EdgeTrace
 * @author Daniel Bildhauer <dbildh@uni-koblenz.de> Summer 2006, Diploma Thesis
 * 
 */

public class NodeTrace implements Greql2Function {

	public JValue evaluate(Graph graph, BooleanGraphMarker subgraph,
			JValue[] arguments) throws EvaluateException {
		JValuePath p1;
		try {
			p1 = arguments[0].toPath();
		} catch (Exception ex) {
			throw new WrongFunctionParameterException(this, null, arguments);
		}
		List<Vertex> list = p1.nodeTrace();
		Iterator<Vertex> iter = list.iterator();
		JValueList resultList = new JValueList();
		while (iter.hasNext()) {
			Vertex v = iter.next();
			resultList.add(new JValue(v));
		}
		return resultList;
	}

	public long getEstimatedCosts(ArrayList<Long> inElements) {
		return 10;
	}

	public double getSelectivity() {
		return 1;
	}

	public long getEstimatedCardinality(int inElements) {
		return 1;
	}

	public String getExpectedParameters() {
		return "(Path)";
	}

	@Override
	public boolean isPredicate() {
		return false;
	}
}
