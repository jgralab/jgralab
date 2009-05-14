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
		String queryString = "from w: V{WhereExpression} report slice(w, <--) end";
		JValue result = evalTestQuery("SliceCreation", queryString);
		JValueBag bag = result.toCollection().toJValueBag();
		assertEquals(1, bag.size());
		for (JValue v : bag) {
			JValueSlice c = (JValueSlice) v;
			System.out.println("Result Slice is: " );
			System.out.println("  Number of nodes: " + c.nodes().size());
			for (Object n : c.nodes())
				System.out.println("    Node: " + n);
		}
	}

}
