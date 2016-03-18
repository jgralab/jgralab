package de.uni_koblenz.jgralabtest.plist;

import java.util.ArrayList;
import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import de.uni_koblenz.ist.utilities.plist.PList;
import de.uni_koblenz.ist.utilities.plist.PListDict;

public class PListTest {

	private static final String TESTFILENAME = "testit/testdata/testIt.plist";

	private PList pl;

	@Before
	public void createPList() throws Exception {
		pl = new PList();

		List<String> s = new ArrayList<>();
		s.add("Alice");
		s.add("Bob");
		s.add("Charlie");
		pl.getDict().putArray("strings", s);

		List<PListDict> dl = new ArrayList<>();
		dl.add(new PListDict());
		dl.add(new PListDict());

		pl.getDict().putArray("dicts", dl);

		pl.storeTo(TESTFILENAME);
	}

	@Test
	public void testCreate() throws Exception {
		List<String> t = pl.getDict().getArray("strings");
		Assert.assertEquals(3, t.size());
		Assert.assertEquals("Alice", t.get(0));
		Assert.assertEquals("Bob", t.get(1));
		Assert.assertEquals("Charlie", t.get(2));

		List<PListDict> u = pl.getDict().getArray("dicts");
		Assert.assertEquals(2, u.size());
	}

	@Test
	public void testLoad() throws Exception {
		PList newPl = new PList(TESTFILENAME);

		List<String> t = newPl.getDict().getArray("strings");
		Assert.assertEquals(3, t.size());
		Assert.assertEquals("Alice", t.get(0));
		Assert.assertEquals("Bob", t.get(1));
		Assert.assertEquals("Charlie", t.get(2));

		List<PListDict> u = newPl.getDict().getArray("dicts");
		Assert.assertEquals(2, u.size());
	}
}
