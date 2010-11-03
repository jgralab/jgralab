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
package de.uni_koblenz.jgralabtest.greql2.funlib;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import de.uni_koblenz.jgralab.greql2.jvalue.JValue;
import de.uni_koblenz.jgralab.greql2.jvalue.JValueBag;
import de.uni_koblenz.jgralab.greql2.jvalue.JValueSlice;
import de.uni_koblenz.jgralabtest.greql2.GenericTests;

public class SliceTest extends GenericTests {

	@Test
	public void testSliceCreation() throws Exception {
		// TODO: Broken, because the GReQL parser removes all WhereExpressions
		// and LetExpressions!
		String queryString = "from w: V{WhereExpression} report slice(w, <--) end";
		JValue result = evalTestQuery("SliceCreation", queryString);
		JValueBag bag = result.toCollection().toJValueBag();
		assertEquals(1, bag.size());
		for (JValue v : bag) {
			JValueSlice c = (JValueSlice) v;
			System.out.println("Result Slice is: ");
			System.out.println("  Number of nodes: " + c.nodes().size());
			for (Object n : c.nodes()) {
				System.out.println("    Node: " + n);
			}
		}
	}

}
