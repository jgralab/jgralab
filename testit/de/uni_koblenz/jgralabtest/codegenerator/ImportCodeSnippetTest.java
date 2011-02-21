/*
 * JGraLab - The Java Graph Laboratory
 * 
 * Copyright (C) 2006-2011 Institute for Software Technology
 *                         University of Koblenz-Landau, Germany
 *                         ist@uni-koblenz.de
 * 
 * For bug reports, documentation and further information, visit
 * 
 *                         http://jgralab.uni-koblenz.de
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
package de.uni_koblenz.jgralabtest.codegenerator;

import static org.junit.Assert.assertEquals;

import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

import de.uni_koblenz.jgralab.codegenerator.CodeList;
import de.uni_koblenz.jgralab.codegenerator.CodeSnippet;
import de.uni_koblenz.jgralab.codegenerator.ImportCodeSnippet;

public class ImportCodeSnippetTest extends CodeSnippetTest {

	@Test
	public void testImportCodeSnippet() {
		// only case in which this constructor may be used
		ImportCodeSnippet ics = new ImportCodeSnippet();
		assertEquals(0, ics.size());
		assertEquals("", ics.getCode());
	}

	@Test
	public void testImportCodeSnippet2() {
		CodeSnippet cs = new CodeSnippet("Cabot", "Gaarder", "Sage", "Rowling");
		CodeList cl = new CodeList();
		cl.add(cs);
		ImportCodeSnippet ics = new ImportCodeSnippet(cl);
		assertEquals(0, ics.size());
		// no matter what CodeList is passed to the constructor, the
		// ImportCodeSnippet
		// itself must always be empty
		assertEquals("", ics.getCode());
		// to make sure the parent was set correctly
		assertEquals("\tCabot\n\tGaarder\n\tSage\n\tRowling\n", ics.getParent()
				.getCode());
		cs.add("Pratchett", "", "Pullman");
		cl.add(cs);
		ics = new ImportCodeSnippet(cl);
		assertEquals("", ics.getCode());
		assertEquals(
				"\tCabot\n\tGaarder\n\tSage\n\tRowling\n\tPratchett\n\t\n\tPullman\n",
				ics.getParent().getCode());

		// border cases
		cl.clear();
		ics.clear();
		assertEquals(0, ics.size());
		assertEquals("", ics.getParent().getCode());
		assertEquals("", ics.getCode());
		cs.clear();
		cs.add("");
		cl.add(cs);
		ics = new ImportCodeSnippet(cl);
		assertEquals("", ics.getCode());
		assertEquals("\t\n", ics.getParent().getCode());
		cs.add("");
		cl.add(cs);
		ics = new ImportCodeSnippet(cl);
		assertEquals("", ics.getCode());
		assertEquals("\t\n\t\n", ics.getParent().getCode());
		cs.clear();
		cs.add("Canavan");
		cl.add(cs);
		ics = new ImportCodeSnippet(cl);
		assertEquals("", ics.getCode());
		assertEquals("\tCanavan\n", ics.getParent().getCode());
	}

	@Override
	@Test
	public void testAdd() {
		// normal cases
		ImportCodeSnippet ics = new ImportCodeSnippet();
		ics.add("Der.", "Herr.", "der.", "Ringe.");
		assertEquals(4, ics.size());
		assertEquals(
				"\nimport Der.;\n\nimport Herr.;\n\nimport Ringe.;\n\nimport der.;\n",
				ics.getCode(0));
		ics.add("Sofies.", "Welt.");
		assertEquals(6, ics.size());
		assertEquals(
				"\nimport Der.;\n\nimport Herr.;\n\nimport Ringe.;\n\nimport Sofies.;\n\nimport Welt.;\n\nimport der.;\n",
				ics.getCode());

		// border cases
		ics.clear();
		ics.add("Queste.");
		assertEquals(1, ics.size());
		assertEquals("\nimport Queste.;\n", ics.getCode());
		ics.clear();
		ics.add("");
		assertEquals(1, ics.size());
		ics.add("eclipse.", "");
		assertEquals(2, ics.size());
		ics.add("");
		assertEquals(2, ics.size());
		ics.clear();
		ics.add(".");
		assertEquals(1, ics.size());
		assertEquals("\nimport .;\n", ics.getCode());
	}

	@Override
	@Test
	public void testGetCode() {
		ImportCodeSnippet ics = new ImportCodeSnippet();
		ics.add("Harry.", "Potter.", "und.", "der.", "Stein.", "der.",
				"Weisen.");
		assertEquals(
				"\n\t\timport Harry.;\n\t\t\n\t\timport Potter.;\n\t\t\n\t\t"
						+ "import Stein.;\n\t\t\n\t\timport Weisen.;\n\t\t\n\t\timport der.;\n\t\t\n\t\t"
						+ "import und.;\n", ics.getCode(2));
		ics.clear();
		ics.add("Kammer.des.Schreckens", "Gefangene.von.Askaban");
		assertEquals(
				"\n\t\t\t\t\t\t\t\t\timport Gefangene.von.Askaban;\n\t\t\t\t\t\t\t\t\t"
						+ "\n\t\t\t\t\t\t\t\t\timport Kammer.des.Schreckens;\n",
				ics.getCode(9));
		ics.add("Feuerkelch.", "Orden.des.Phoenix");
		assertEquals(
				"\n\timport Feuerkelch.;\n\t\n\timport "
						+ "Gefangene.von.Askaban;\n\t\n\timport Kammer.des.Schreckens;\n\t\n\timport "
						+ "Orden.des.Phoenix;\n", ics.getCode(1));
		ics.clear();
		ics.add(".", "Halbblutprinz.", ".", "Heiligtuemer.des.Todes", ".", ".");
		assertEquals(
				"\n\t\t\timport .;\n\t\t\t\n\t\t\timport Halbblutprinz.;\n\t\t\t"
						+ "\n\t\t\timport Heiligtuemer.des.Todes;\n", ics
						.getCode(3));
		ics.clear();
		ics.add("Heiligtuemer.Harrys", "Heiligtuemer.des.Todes");
		assertEquals("\n\t\t\timport Heiligtuemer.Harrys;\n\t\t\timport "
				+ "Heiligtuemer.des.Todes;\n", ics.getCode(3));

		// border cases
		ics.clear();
		ics.add("Der.Clan.der.Otori");
		assertEquals("\n\timport Der.Clan.der.Otori;\n", ics.getCode(1));
		ics.clear();
		ics.add(".");
		assertEquals("\n\t\t\t\t\timport .;\n", ics.getCode(5));
		ics.add(".", ".", ".");
		assertEquals("\nimport .;\n", ics.getCode(0));
		assertEquals("\nimport .;\n", ics.getCode(-20));
		ics.clear();
		assertEquals("", ics.getCode(0));
		assertEquals("", ics.getCode(3));
		assertEquals("", ics.getCode(-45));
	}

	@Override
	@Test
	// tests the inherited getCode()-method
	public void testGetCode2() {
		// normal cases
		ImportCodeSnippet ics = new ImportCodeSnippet();
		ics.add("rho.", "ny.", "iota.");
		assertEquals("\nimport iota.;\n\nimport ny.;\n\nimport rho.;\n", ics
				.getCode());
		ics.clear();
		ics.add("sigma.tau", "tau.ypsilon", "sigma.ypsilon");
		assertEquals("\nimport sigma.tau;\nimport sigma.ypsilon;\n\nimport "
				+ "tau.ypsilon;\n", ics.getCode());
		ics.clear();
		ics.add(".", "chi.psi", "xi.omega");
		assertEquals("\nimport .;\n\nimport chi.psi;\n\nimport xi.omega;\n",
				ics.getCode());

		// border cases
		ics.clear();
		assertEquals("", ics.getCode());
		ics.add(".");
		assertEquals("\nimport .;\n", ics.getCode());
		ics.add(".", ".", ".");
		assertEquals("\nimport .;\n", ics.getCode());
		ics.clear();
		ics.add("Omikron.o");
		assertEquals("\nimport Omikron.o;\n", ics.getCode());
		ics.add("Omikron.a", "omikron.b", "Omikron.p");
		assertEquals(
				"\nimport Omikron.a;\nimport Omikron.o;\nimport Omikron.p;\n\n"
						+ "import omikron.b;\n", ics.getCode());
		ics.clear();
		ics.add("zeta.s", "Zeta.z", "zeta.a");
		assertEquals("\nimport Zeta.z;\n\nimport zeta.a;\nimport zeta.s;\n",
				ics.getCode());
	}

	@Override
	@Test
	public void testClear() {
		// normal cases
		ImportCodeSnippet ics = new ImportCodeSnippet();
		ics.add("Alpha", "Beta", "Gamma", "Delta");
		ics.clear();
		assertEquals(0, ics.size());
		try {
			ics.getParent();
		} catch (NullPointerException e) {
			// :)
		}
		ics.add("Epsilon", "Phi", "Xsi");
		ics.clear();
		assertEquals(0, ics.size());
		try {
			ics.getParent();
		} catch (NullPointerException e) {
			// :)
		}

		// border cases
		ics = new ImportCodeSnippet();
		ics.clear();
		assertEquals(0, ics.size());
		try {
			ics.getParent();
		} catch (NullPointerException e) {
			// :)
		}
		ics.clear();
		assertEquals(0, ics.size());
		try {
			ics.getParent();
		} catch (NullPointerException e) {
			// :)
		}
	}

	@Override
	@Test
	public void testSize() {
		// normal cases
		ImportCodeSnippet ics1 = new ImportCodeSnippet();
		ics1.add("Bla", "Blubb", "Blara");
		assertEquals(3, ics1.size());
		ics1.add("Blubbel");
		assertEquals(4, ics1.size());
		ics1.clear();
		ics1.add("Babb", "Babbel", "Babb");
		assertEquals(2, ics1.size());
		ics1.add("Blamuh", "Blubb", "Babubbel");
		assertEquals(5, ics1.size());

		// border cases
		ImportCodeSnippet ics2 = new ImportCodeSnippet();
		ics1.add("Blamuh");
		assertEquals(5, ics1.size());
		assertEquals(0, ics2.size());
		ics2.add("Bla");
		assertEquals(1, ics2.size());
		ics2.add("Bla");
		assertEquals(1, ics2.size());
		ics2.add("");
		assertEquals(2, ics2.size());
		ics2.clear();
		ics2.add("");
		assertEquals(1, ics2.size());
		ics2.add(".");
		assertEquals(2, ics2.size());
		ics2.clear();
		ics2.add(".");
		assertEquals(1, ics2.size());
	}

	@Test
	public void testAddVariablesAndGetVariable() {
		// border cases
		ImportCodeSnippet ics = new ImportCodeSnippet();
		Map<String, String> testMap = new HashMap<String, String>();
		assertEquals("*UNDEFINED:Beta*", ics.getVariable("Beta"));
		assertEquals("*UNDEFINED:*", ics.getVariable(""));

		testMap.put("1", "a");
		ics.addVariables(testMap);
		assertEquals("a", ics.getVariable("1"));
		assertEquals("*UNDEFINED:a*", ics.getVariable("a"));
		assertEquals("*UNDEFINED:Test*", ics.getVariable("Test"));

		testMap.clear();
		testMap.put("", "");
		ics.addVariables(testMap);
		assertEquals("", ics.getVariable(""));

		testMap.put("", "b");
		ics.addVariables(testMap);
		assertEquals("b", ics.getVariable(""));

		testMap.clear();
		testMap.put("a", "");
		ics.addVariables(testMap);
		assertEquals("", ics.getVariable("a"));

		testMap.clear();
		testMap.put(null, null);
		ics.addVariables(testMap);
		assertEquals(null, ics.getVariable(null));

		testMap.clear();
		testMap.put("7", null);
		ics.addVariables(testMap);
		assertEquals(null, ics.getVariable("7"));

		testMap.clear();
		testMap.put(null, "k");
		ics.addVariables(testMap);
		assertEquals("k", ics.getVariable(null));

		// normal cases
		testMap.put("5", "e");
		ics.addVariables(testMap);
		assertEquals("e", ics.getVariable("5"));
		assertEquals("e", ics.getVariable("5"));

		ics.clear();
		testMap.put("8", "h");
		testMap.put("3", "c");
		ics.addVariables(testMap);
		assertEquals("h", ics.getVariable("8"));
		assertEquals("c", ics.getVariable("3"));

		testMap.put("6", "f");
		ics.addVariables(testMap);
		assertEquals("f", ics.getVariable("6"));
	}

	@Override
	@Test
	public void testSetVariable() {
		// border cases
		ImportCodeSnippet ics = new ImportCodeSnippet();
		ics.setVariable(null, null);
		assertEquals(null, ics.getVariable(null));
		ics.setVariable(null, "sylvester");
		assertEquals("sylvester", ics.getVariable(null));
		ics.setVariable("3", null);
		assertEquals(null, ics.getVariable("3"));
		ics.setVariable("", null);
		assertEquals(null, ics.getVariable(""));
		ics.setVariable("", "");
		assertEquals("", ics.getVariable(""));
		ics.setVariable("5", "");
		assertEquals("", ics.getVariable("5"));
		ics.setVariable(null, "");
		assertEquals("", ics.getVariable(null));

		// normal cases
		ics.setVariable("1", "montag");
		assertEquals("montag", ics.getVariable("1"));
		ics.setVariable("1", "neujahr");
		assertEquals("neujahr", ics.getVariable("1"));
		ics.setVariable("2", "dienstag");
		assertEquals("dienstag", ics.getVariable("2"));
		ics.setVariable("3", "mittwoch");
		assertEquals("mittwoch", ics.getVariable("3"));
		ics.setVariable("4", "donnerstag");
		assertEquals("donnerstag", ics.getVariable("4"));
		ics.setVariable("6", "samstag");
		assertEquals("samstag", ics.getVariable("6"));
		ics.setVariable("6", "wochenende");
		assertEquals("wochenende", ics.getVariable("6"));
	}

	@Override
	@Test
	public void testGetParent() {
		ImportCodeSnippet ics = new ImportCodeSnippet();
		assertEquals(null, ics.getParent());

		CodeList cl1 = new CodeList();
		cl1.setVariable("parent1", "0");
		CodeList cl2 = new CodeList(cl1);
		cl2.setVariable("parent2", "1");
		CodeList cl3 = new CodeList(cl2);
		cl3.setVariable("parent3", "2");

		ics = new ImportCodeSnippet(cl1);
		assertEquals(cl1, ics.getParent());
		ics = new ImportCodeSnippet(cl2);
		assertEquals(cl2, ics.getParent());
		assertEquals(cl1, ics.getParent().getParent());
		ics = new ImportCodeSnippet(cl3);
		assertEquals(cl3, ics.getParent());
		assertEquals(cl2, ics.getParent().getParent());
		assertEquals(cl1, ics.getParent().getParent().getParent());

		// tests what happens if a circle is constructed
		cl1 = new CodeList(cl2);
		cl1.setVariable("parent4", "2");
		ics = new ImportCodeSnippet(cl1);
		assertEquals(cl1, ics.getParent());
		assertEquals("2", ics.getParent().getVariable("parent4"));
		assertEquals("0", ics.getParent().getVariable("parent1"));
		assertEquals(cl2, ics.getParent().getParent());
		assertEquals("*UNDEFINED:parent4*", ics.getParent().getParent()
				.getParent().getVariable("parent4"));
		assertEquals("0", ics.getParent().getParent().getParent().getVariable(
				"parent1"));
	}
}
