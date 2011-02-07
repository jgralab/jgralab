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
import static org.junit.Assert.assertTrue;


import org.junit.Test;

import de.uni_koblenz.jgralab.greql2.jvalue.JValue;
import de.uni_koblenz.jgralab.greql2.jvalue.JValueImpl;
import de.uni_koblenz.jgralabtest.greql2.GenericTests;

/**
 * Tests all functions that are provided by the Greql2FunctionLibrary
 * 
 * @author Daniel Bildhauer <dbildh@uni-koblenz.de> Summer 2006, Diploma Thesis
 * 
 */
public class ListAccessTest extends GenericTests {

	/*
	 * Test method for
	 * 'greql2.evaluator.GreqlEvaluator.evaluateRecordConstruction(RecordConstruction,
	 * Graph)'
	 */
	@Test
	public void testEvaluateListAccess() throws Exception {
		// TODO: Broken, because the GReQL parser removes all WhereExpressions
		// and LetExpressions!
		String queryString = "let x := list ( \"bratwurst\", \"currywurst\", \"steak\", \"kaenguruhfleisch\", \"spiessbraten\") in from i:V{Identifier} report x[3] end";
		JValue result = evalTestQuery("ListAccess", queryString);
		assertEquals(5, result.toCollection().size());
		assertTrue(result.toCollection().contains(
				new JValueImpl("kaenguruhfleisch")));
	}

	/*
	 * Test method for
	 * 'greql2.evaluator.GreqlEvaluator.evaluateRecordConstruction(RecordConstruction,
	 * Graph)'
	 */
	@Test
	public void testEvaluateRecordAccess() throws Exception {
		String queryString = "let x := rec ( menue1:\"bratwurst\", menue2:\"currywurst\", menue3:\"steak\", menue4:\"kaenguruhfleisch\", menue5:\"spiessbraten\") in from i:V{Identifier} report x.menue4 end";
		JValue result = evalTestQuery("RecordAccess", queryString);
		assertEquals(1, result.toCollection().toJValueSet().size());
		assertTrue(result.toCollection().toJValueSet()
				.contains(new JValueImpl("kaenguruhfleisch")));
	}
}
