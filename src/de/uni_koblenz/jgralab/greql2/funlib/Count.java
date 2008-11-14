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

import de.uni_koblenz.jgralab.BooleanGraphMarker;
import de.uni_koblenz.jgralab.Graph;
import de.uni_koblenz.jgralab.greql2.exception.EvaluateException;
import de.uni_koblenz.jgralab.greql2.exception.WrongFunctionParameterException;
import de.uni_koblenz.jgralab.greql2.jvalue.JValue;
import de.uni_koblenz.jgralab.greql2.jvalue.JValueCollection;


/**
 * Returns the number of elements in a given Object. If the object is not a collection, 1 is returned.
 *
 * <dl>
 * <dt><b>GReQL-signature</b></dt>
 * <dd><code>BOOLEAN contains(obj:OBJECT)</code></dd>
 * <dd>&nbsp;</dd>
 * </dl>
 * <dl><dt></dt>
 * <dd>
 * <dl>
 * <dt><b>Parameters:</b></dt>
 * <dd><code>obj</code> - object to count elements in</dd>
 * <dt><b>Returns:</b></dt>
 * <dd>the amount of elements in the given object. If the object is not a collection, 1 is returned.</dd>
 * <dd><code>Null</code> if one of the parameters is <code>Null</code></dd>
 * </dl>
 * </dd>
 * </dl>
 * @author ist@uni-koblenz.de
 * 
 */

public class Count implements Greql2Function {
	
	public JValue evaluate(Graph graph, BooleanGraphMarker subgraph, JValue[] arguments) throws EvaluateException {
		if ( arguments.length < 1) {
			throw new WrongFunctionParameterException(this, null, arguments );
		}	
		try {
			if (arguments[0].isCollection()) {
				JValueCollection col = arguments[0].toCollection();
				return new JValue(col.size());
			} else {
				return new JValue(1);
			}
		}
		catch (Exception ex) { //JValueInvalidTypeException, NoSuchFieldException, IndexOutOfBoundsException
			throw new WrongFunctionParameterException(this, null, arguments);
		} 
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
		return "(JValue)";
	}

	@Override
	public boolean isPredicate() {
		return false;
	}

}
