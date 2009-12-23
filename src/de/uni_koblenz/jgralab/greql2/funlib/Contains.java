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

import de.uni_koblenz.jgralab.Graph;
import de.uni_koblenz.jgralab.GraphElement;
import de.uni_koblenz.jgralab.graphmarker.BooleanGraphMarker;
import de.uni_koblenz.jgralab.greql2.exception.EvaluateException;
import de.uni_koblenz.jgralab.greql2.exception.WrongFunctionParameterException;
import de.uni_koblenz.jgralab.greql2.jvalue.JValue;
import de.uni_koblenz.jgralab.greql2.jvalue.JValueCollection;
import de.uni_koblenz.jgralab.greql2.jvalue.JValuePath;
import de.uni_koblenz.jgralab.greql2.jvalue.JValuePathSystem;
import de.uni_koblenz.jgralab.greql2.jvalue.JValueType;

/**
 * Checks if a given object is included in a given collection. The object can be
 * anything (for example an integer or a string).
 * 
 * <dl>
 * <dt><b>GReQL-signature</b></dt>
 * <dd><code>BOOLEAN contains(c:COLLECTION, obj:OBJECT)</code></dd>
 * <dd><code>BOOLEAN contains(c:PATH, obj:ATTRIBUTEDELEMENT)</code></dd>
 * <dd><code>BOOLEAN contains(c:PATHSYSTEM, obj:ATTRIBUTEDELEMENT)</code></dd>
 * <dd>&nbsp;</dd>
 * </dl>
 * <dl>
 * <dt></dt>
 * <dd>
 * <dl>
 * <dt><b>Parameters:</b></dt>
 * <dd><code>c</code> - collection or path or pathsystem to check</dd>
 * <dd><code>obj</code> - object or attributed element to check</dd>
 * <dt><b>Returns:</b></dt>
 * <dd><code>true</code> if the given object is included in the given collection
 * </dd>
 * <dd><code>Null</code> if one of the given parameters is <code>Null</code></dd>
 * <dd><code>false</code> otherwise</dd>
 * </dl>
 * </dd>
 * </dl>
 * 
 * @author ist@uni-koblenz.de
 * 
 */

public class Contains extends Greql2Function {

	{
		JValueType[][] x = { { JValueType.COLLECTION, JValueType.OBJECT },
				{ JValueType.PATH, JValueType.ATTRIBUTEDELEMENT },
				{ JValueType.PATHSYSTEM, JValueType.ATTRIBUTEDELEMENT } };
		signatures = x;

		description = "Return true, iff the given collection/path/pathsystem contains the given element.";
	}

	public JValue evaluate(Graph graph, BooleanGraphMarker subgraph,
			JValue[] arguments) throws EvaluateException {

		switch (checkArguments(arguments)) {
		case 0:
			JValueCollection col = arguments[0].toCollection();
			return new JValue(col.contains(arguments[1]));
		case 1:
			JValuePath path = arguments[0].toPath();
			return new JValue(path.contains((GraphElement) arguments[1]
					.toAttributedElement()));
		case 2:
			JValuePathSystem pathsys = arguments[0].toPathSystem();
			return new JValue(pathsys.contains((GraphElement) arguments[1]
					.toAttributedElement()));
		default:
			throw new WrongFunctionParameterException(this, arguments);
		}
	}

	public long getEstimatedCosts(ArrayList<Long> inElements) {
		return 2;
	}

	public double getSelectivity() {
		return 0.2;
	}

	public long getEstimatedCardinality(int inElements) {
		return 1;
	}

}
