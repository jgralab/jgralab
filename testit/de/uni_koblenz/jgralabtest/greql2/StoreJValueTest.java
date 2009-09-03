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

package de.uni_koblenz.jgralabtest.greql2;

import static org.junit.Assert.*;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

import org.junit.Test;

import de.uni_koblenz.jgralab.greql2.jvalue.JValue;
import de.uni_koblenz.jgralab.greql2.jvalue.JValueXMLOutputVisitor;
import de.uni_koblenz.jgralab.greql2.jvalue.ValueXMLLoader;

public class StoreJValueTest extends GenericTests {

	@Test
	public void testStoreJValue1() throws Exception {
		 String queryString = "bag(tup(\"Nodes:\", count(from v:V{} report v end)), tup(\"Edges:\", count(from e:E{} report e end)))";
		 JValue result = evalTestQuery("StoreJValue1", queryString);
		 JValueXMLOutputVisitor outputVisitor = new JValueXMLOutputVisitor(result, "storejvaluetest1.xml", getTestGraph());
		 outputVisitor.toString();
		 ValueXMLLoader loader = new ValueXMLLoader(getTestGraph());
		 JValue loadedValue = loader.load("storejvaluetest1.xml");
		 assertNotNull(loadedValue);
		 assertEquals(result, loadedValue);
	}

	@Test
	public void testStoreJValue2() throws Exception {
		 String queryString = "from v:V{} report v as \"Nodes\" end";
		 JValue result = evalTestQuery("StoreJValue2", queryString);
		 JValueXMLOutputVisitor outputVisitor = new JValueXMLOutputVisitor(
		 result, "storejvaluetest2.xml", getTestGraph());
		 outputVisitor.toString();
		 ValueXMLLoader loader = new ValueXMLLoader(getTestGraph());
		 JValue loadedValue = loader.load("storejvaluetest2.xml");
		 assertNotNull(loadedValue);
		 assertEquals(result, loadedValue);
	}
	
	@Test
	public void testStoreJValue3() throws Exception {
		 String queryString = "from x,y:list(1..100) reportTable \"X\", \"Y\", x*y end";
		 JValue result = evalTestQuery("StoreJValue2", queryString);
		 JValueXMLOutputVisitor outputVisitor = new JValueXMLOutputVisitor( result, "storejvaluetest3.xml", getTestGraph());
		 outputVisitor.toString();
		 ValueXMLLoader loader = new ValueXMLLoader(getTestGraph());
		 JValue loadedValue = loader.load("storejvaluetest3.xml");
		 assertNotNull(loadedValue);
	}

	@Test
	public void testQTAPCQuery() throws Exception {
		fail("NYI");
		// String queryString = "from e:E{IsSuperClassOfClass} report e as
		// \"Generalization\"," +
		// " startVertex(e) as \"Superclass\"," +
		// "endVertex(e) as \"Subclass\" end";
		// JValue result = evalTestQuery("StoreJValue2", queryString);
		// JValueHTMLOutputVisitor outputVisitor = new JValueHTMLOutputVisitor(
		// result, "/home/dbildh/Desktop/storejvaluetest.html", getTestGraph());
		// outputVisitor.toString();
	}

}
