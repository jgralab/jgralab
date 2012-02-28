/*
* JGraLab - The Java Graph Laboratory
*
* Copyright (C) 2006-2012 Institute for Software Technology
*                         University of Koblenz-Landau, Germany
*                         ist@uni-koblenz.de
*
* For bug reports, documentation and further information, visit
*
*                         https://github.com/jgralab/jgralab
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

package de.uni_koblenz.jgralabtest.greql2;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

import java.io.File;

import org.junit.Test;

import de.uni_koblenz.jgralab.greql2.serialising.XMLLoader;
import de.uni_koblenz.jgralab.greql2.serialising.XMLOutputWriter;

public class StoreJValueTest extends GenericTest {

	@Test
	public void testStoreJValue1() throws Exception {
		fail(); // JValueVisitorException
		String queryString = "list(tup(\"Nodes:\", count(from v:V{} report v end)), tup(\"Edges:\", count(from e:E{} report e end)))";
		Object result = evalTestQuery("StoreJValue1", queryString);
		XMLOutputWriter writer = new XMLOutputWriter(
				getTestGraph(TestVersion.GREQL_GRAPH));
		writer.writeValue(result, new File(
				"testit/testdata/storejvaluetest1.xml"));
		XMLLoader loader = new XMLLoader(getTestGraph(TestVersion.GREQL_GRAPH));
		Object loadedValue = loader
				.load("testit/testdata/storejvaluetest1.xml");
		assertNotNull(loadedValue);
		assertEquals(result, loadedValue);
	}

	@Test
	public void testStoreJValue2a() throws Exception {
		fail(); // JValueVisitorException
		String queryString = "V{}";
		Object result = evalTestQuery("StoreJValue2a", queryString);
		XMLOutputWriter writer = new XMLOutputWriter(
				getTestGraph(TestVersion.GREQL_GRAPH));
		writer.writeValue(result, new File(
				"testit/testdata/storejvaluetest2a.xml"));
		XMLLoader loader = new XMLLoader(getTestGraph(TestVersion.GREQL_GRAPH));
		Object loadedValue = loader
				.load("testit/testdata/storejvaluetest2a.xml");
		assertNotNull(loadedValue);
		assertEquals(result, loadedValue);
	}

	@Test
	public void testStoreJValue2() throws Exception {
		fail(); // JValueVisitorException
		String queryString = "from v:V{} report v as \"Nodes\" end";
		Object result = evalTestQuery("StoreJValue2", queryString);
		XMLOutputWriter writer = new XMLOutputWriter(
				getTestGraph(TestVersion.GREQL_GRAPH));
		writer.writeValue(result, new File(
				"testit/testdata/storejvaluetest2.xml"));
		XMLLoader loader = new XMLLoader(getTestGraph(TestVersion.GREQL_GRAPH));
		Object loadedValue = loader
				.load("testit/testdata/storejvaluetest2.xml");
		assertNotNull(loadedValue);
		assertEquals(result, loadedValue);
	}

	@Test
	public void testStoreJValue3() throws Exception {
		fail(); // JValueVisitorException
		String queryString = "from x,y:list(1..100) reportTable x, y, x*y end";
		Object result = evalTestQuery("StoreJValue3", queryString);
		XMLOutputWriter writer = new XMLOutputWriter(
				getTestGraph(TestVersion.GREQL_GRAPH));
		writer.writeValue(result, new File(
				"testit/testdata/storejvaluetest3.xml"));
		XMLLoader loader = new XMLLoader(getTestGraph(TestVersion.GREQL_GRAPH));
		Object loadedValue = loader
				.load("testit/testdata/storejvaluetest3.xml");
		assertNotNull(loadedValue);
		assertEquals(result, loadedValue);
	}
}
