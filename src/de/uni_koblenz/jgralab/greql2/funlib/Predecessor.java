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
import de.uni_koblenz.jgralab.greql2.jvalue.JValueCollection;
import de.uni_koblenz.jgralab.greql2.jvalue.JValueImpl;
import de.uni_koblenz.jgralab.greql2.jvalue.JValueType;

public class Predecessor extends Greql2Function {
	{
		JValueType[][] x = { { JValueType.OBJECT, JValueType.COLLECTION,
				JValueType.OBJECT } };
		signatures = x;

		description = "Returns the predecessor of the given object in the given collection.";

		Category[] c = { Category.COLLECTIONS_AND_MAPS };
		categories = c;
	}

	@Override
	public JValue evaluate(Graph graph, AbstractGraphMarker<AttributedElement> subgraph,
			JValue[] arguments) throws EvaluateException {
		if (checkArguments(arguments) < 0) {
			throw new WrongFunctionParameterException(this, arguments);
		}
		JValue obj = arguments[0];
		JValueCollection coll = arguments[1].toCollection();
		JValue p = new JValueImpl();
		for (JValue c : coll) {
			if (c.equals(obj)) {
				return p;
			}
			p = c;
		}
		return new JValueImpl();
	}

	@Override
	public long getEstimatedCardinality(int inElements) {
		return 1;
	}

	@Override
	public long getEstimatedCosts(ArrayList<Long> inElements) {
		return inElements.get(1) / 2;
	}

	@Override
	public double getSelectivity() {
		return 1;
	}

}
