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
import java.util.Iterator;

import de.uni_koblenz.jgralab.BooleanGraphMarker;
import de.uni_koblenz.jgralab.Graph;
import de.uni_koblenz.jgralab.greql2.exception.EvaluateException;
import de.uni_koblenz.jgralab.greql2.exception.WrongFunctionParameterException;
import de.uni_koblenz.jgralab.greql2.jvalue.JValue;
import de.uni_koblenz.jgralab.greql2.jvalue.JValueCollection;
import de.uni_koblenz.jgralab.greql2.jvalue.JValueSet;
import de.uni_koblenz.jgralab.greql2.jvalue.JValueTable;

/**
 * Extracts the set of elements that are part of the given structure
 * 
 * <dl>
 * <dt><b>GReQL-signature</b></dt>
 * <dd><code>SET elementsIn(structure:COLLECTION)</code></dd>
 * <dd>&nbsp;</dd>
 * </dl>
 * <dl>
 * <dt></dt>
 * <dd>
 * <dl>
 * <dt><b>Parameters:</b></dt>
 * <dd><code>structure</code> - the structure which should be flattened</dd>
 * <dt><b>Returns:</b></dt>
 * <dd>The set of elements that are part of <code>structure</code></dd>
 * </dl>
 * </dd>
 * </dl>
 * 
 * @author Daniel Bildhauer <dbildh@uni-koblenz.de> Feb 2008
 * 
 */


public class ElementsIn implements Greql2Function {

	public JValue evaluate(Graph graph, BooleanGraphMarker subgraph,
			JValue[] arguments) throws EvaluateException {
		try {
			JValueSet set = new JValueSet();
			JValue structure = arguments[0];
			if (structure.isCollection()) {
				JValueCollection col = structure.toCollection();
				if (col.isJValueTable()) {
					JValueTable tab = col.toJValueTable();
					Iterator<JValue> iter = tab.iterator();
					while (iter.hasNext()) {
						JValue next = iter.next();
						if (next.isCollection()) {
							JValue[] params = {next};
							set.addAll((JValueSet) evaluate(graph, subgraph, params));
						} else {
							set.add(next);
						}
					}
				} else {
					set = col.toJValueSet();
				}
			} else {
				set.add(structure);
			}	
			return set;
		} catch (Exception ex) {
			throw new WrongFunctionParameterException(this, null, arguments);
		}
	}

	public long getEstimatedCosts(ArrayList<Long> inElements) {
		return inElements.get(0) * 2;
	}

	public double getSelectivity() {
		return 0.1;
	}

	public long getEstimatedCardinality(int inElements) {
		return inElements / 10;
	}

	public String getExpectedParameters() {
		return "(Collection)";
	}

	@Override
	public boolean isPredicate() {
		return false;
	}
}
