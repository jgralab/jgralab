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
package de.uni_koblenz.jgralabtest.codegenerator;

import static org.junit.Assert.assertEquals;

import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

import de.uni_koblenz.jgralab.codegenerator.CodeList;
import de.uni_koblenz.jgralab.codegenerator.CodeSnippet;

public class CodeSnippetTest {

	@Test
	public void testCodeSnippet() {
		// tests the only case in which this constructor may be used
		CodeSnippet cs = new CodeSnippet();
		assertEquals("", cs.getCode());
	}

	@Test
	public void testCodeSnippet2() {
		// normal cases
		CodeSnippet cs = new CodeSnippet("Apfel", "Birne", "Kirsche");
		assertEquals("Apfel\nBirne\nKirsche\n", cs.getCode());
		cs = new CodeSnippet("Ahorn", "Kastanie", "Birke", "Esche", "Buche");
		assertEquals("Ahorn\nKastanie\nBirke\nEsche\nBuche\n", cs.getCode());
		cs = new CodeSnippet("Ginko", "Flieder", "Eiche");
		assertEquals("Ginko\nFlieder\nEiche\n", cs.getCode());

		// border cases
		cs = new CodeSnippet("");
		assertEquals("\n", cs.getCode());
		cs = new CodeSnippet("Pflaume");
		assertEquals("Pflaume\n", cs.getCode());
	}

	@Test
	public void testCodeSnippet3() {
		// normal cases
		CodeSnippet cs = new CodeSnippet(true, "Erdbeere", "Blaubeere",
				"Himbeere");
		assertEquals("\nErdbeere\nBlaubeere\nHimbeere\n", cs.getCode());
		cs = new CodeSnippet(false, "Brombeere", "Johannisbeere");
		assertEquals("Brombeere\nJohannisbeere\n", cs.getCode());
		cs = new CodeSnippet(false, "Moltebeere", "", "Stachelbeere");
		assertEquals("Moltebeere\n\nStachelbeere\n", cs.getCode());
		cs = new CodeSnippet(true, "", "Preiselbeere");
		assertEquals("\n\nPreiselbeere\n", cs.getCode());
		cs = new CodeSnippet(true, "Granatapfel", "", "Paprika", "");
		assertEquals("\nGranatapfel\n\nPaprika\n\n", cs.getCode());

		// border cases
		cs = new CodeSnippet(true, "");
		assertEquals("\n\n", cs.getCode());
		cs = new CodeSnippet(false, "");
		assertEquals("\n", cs.getCode());
	}

	@Test
	public void testCodeSnippet4() {
		// normal cases
		CodeList cl = new CodeList();
		CodeSnippet cs1 = new CodeSnippet(cl, "Mango", "Avocado");
		assertEquals("Mango\nAvocado\n", cs1.getCode());
		assertEquals("\tMango\n\tAvocado\n", cl.getCode());// to make sure the
															// given
		// CodeList was changed accordingly
		CodeSnippet cs2 = new CodeSnippet("Ananas");
		cl.add(cs2);
		cs1 = new CodeSnippet(cl, "Mango", "Pflaume", "Limette");
		assertEquals("Mango\nPflaume\nLimette\n", cs1.getCode());
		assertEquals(
				"\tMango\n\tAvocado\n\tAnanas\n\tMango\n\tPflaume\n\tLimette\n",
				cl.getCode());
		cl.clear();
		cl.add(cs2, 2);
		cs1 = new CodeSnippet(cl, "Mango", "Avocado");
		assertEquals("Mango\nAvocado\n", cs1.getCode());
		assertEquals("\t\t\tAnanas\n\tMango\n\tAvocado\n", cl.getCode());
		cl.clear();
		cs1 = new CodeSnippet(cl, "Mandarine", "Orange", "Grapefruit",
				"Pampelmuse");
		assertEquals("Mandarine\nOrange\nGrapefruit\nPampelmuse\n", cs1
				.getCode());
		assertEquals("\tMandarine\n\tOrange\n\tGrapefruit\n\tPampelmuse\n", cl
				.getCode());
		cs2.add("Drachenfrucht", "Affenbrot", "Guave");
		cl.add(cs2, 1);
		cs1 = new CodeSnippet(cl, "Feige", "Granatapfel");
		assertEquals("Feige\nGranatapfel\n", cs1.getCode());
		assertEquals(
				"\tMandarine\n\tOrange\n\tGrapefruit\n\tPampelmuse\n\t\tAnanas"
						+ "\n\t\tDrachenfrucht\n\t\tAffenbrot\n\t\tGuave\n\tFeige\n\tGranatapfel\n",
				cl.getCode());

		// border cases
		// tests with an empty CodeList
		cl.clear();
		cl.add(cs2, 4);
		cs1 = new CodeSnippet(cl, "Dattel");
		assertEquals("Dattel\n", cs1.getCode());
		assertEquals(
				"\t\t\t\t\tAnanas\n\t\t\t\t\tDrachenfrucht\n\t\t\t\t\tAffenbrot"
						+ "\n\t\t\t\t\tGuave\n\tDattel\n", cl.getCode());
		cl.clear();
		cs1 = new CodeSnippet(cl, "Mango", "Avocado");
		assertEquals("Mango\nAvocado\n", cs1.getCode());
		assertEquals("\tMango\n\tAvocado\n", cl.getCode());
		cs1 = new CodeSnippet(cl, "");
		assertEquals("\n", cs1.getCode());
		assertEquals("\tMango\n\tAvocado\n\t\n", cl.getCode());
		cl.clear();
		cs1 = new CodeSnippet(cl, "");
		assertEquals("\n", cs1.getCode());
		assertEquals("\t\n", cl.getCode());
		cl.clear();
		cs1 = new CodeSnippet(cl, "Ginkgo");
		assertEquals("Ginkgo\n", cs1.getCode());
		assertEquals("\tGinkgo\n", cl.getCode());
	}

	@Test
	public void testCodeSnippet5() {
		// normal cases
		CodeSnippet cs1 = new CodeSnippet("Bärlauch", "Dill", "Estragon");
		CodeList cl = new CodeList();
		cl.add(cs1);
		CodeSnippet cs2 = new CodeSnippet(cl, true, "Fenchel", "Liebstöckel");
		assertEquals("\nFenchel\nLiebstöckel\n", cs2.getCode());
		assertEquals(
				"\tBärlauch\n\tDill\n\tEstragon\n\n\tFenchel\n\tLiebstöckel\n",
				cl.getCode());
		cl.clear();
		cs1.add("Lorbeer");
		cl.add(cs1, 3);
		cs2 = new CodeSnippet(cl, false, "Kresse", "Bohnenkraut", "Knöterich");
		assertEquals("Kresse\nBohnenkraut\nKnöterich\n", cs2.getCode());
		assertEquals(
				"\t\t\t\tBärlauch\n\t\t\t\tDill\n\t\t\t\tEstragon\n\t\t\t\t"
						+ "Lorbeer\n\tKresse\n\tBohnenkraut\n\tKnöterich\n", cl
						.getCode());

		// border cases
		cs2 = new CodeSnippet(cl, true, "Kapuzinerkresse", "", "Löwenzahn", "");
		assertEquals("\nKapuzinerkresse\n\nLöwenzahn\n\n", cs2.getCode());
		assertEquals(
				"\t\t\t\tBärlauch\n\t\t\t\tDill\n\t\t\t\tEstragon\n\t\t\t\t"
						+ "Lorbeer\n\tKresse\n\tBohnenkraut\n\tKnöterich\n\n\tKapuzinerkresse"
						+ "\n\t\n\tLöwenzahn\n\t\n", cl.getCode());
		cl.clear();
		cs2 = new CodeSnippet(cl, true, "Gurke", "Tomate");
		assertEquals("\nGurke\nTomate\n", cs2.getCode());
		assertEquals("\n\tGurke\n\tTomate\n", cl.getCode());
		cs2 = new CodeSnippet(cl, false, "Schnittlauch");
		assertEquals("Schnittlauch\n", cs2.getCode());
		assertEquals("\n\tGurke\n\tTomate\n\tSchnittlauch\n", cl.getCode());
		cs2 = new CodeSnippet(null, true, "Melisse", "Minze");
		assertEquals("\nMelisse\nMinze\n", cs2.getCode());
		cl.clear();
		cs2 = new CodeSnippet(cl, true, "", "");
		assertEquals("\n\n\n", cs2.getCode());
		assertEquals("\n\t\n\t\n", cl.getCode());
	}

	@Test
	public void testSetNewLine() {
		// normal cases
		CodeSnippet cs = new CodeSnippet();
		cs.setNewLine(true);
		assertEquals("", cs.getCode());
		cs.add("Ahornsirup");
		assertEquals("\nAhornsirup\n", cs.getCode());
		cs.add("Erdbeermarmelade");
		assertEquals("\nAhornsirup\nErdbeermarmelade\n", cs.getCode());
		cs.add("Honig");
		assertEquals("\nAhornsirup\nErdbeermarmelade\nHonig\n", cs.getCode());
		cs.clear();
		cs.add("Nougatcreme", "Erdnussbutter", "Blaubeermarmelade");
		assertEquals("\nNougatcreme\nErdnussbutter\nBlaubeermarmelade\n", cs
				.getCode());

		cs.clear();
		cs.setNewLine(false);
		cs.add("Blaubeermarmelade", "Johannisbeergelee");
		assertEquals("Blaubeermarmelade\nJohannisbeergelee\n", cs.getCode());
		cs.add("Apfelmus");
		assertEquals("Blaubeermarmelade\nJohannisbeergelee\nApfelmus\n", cs
				.getCode());
		cs.add("Kirschgelee", "Pflaumenmus", "Himbeermarmelade");
		assertEquals(
				"Blaubeermarmelade\nJohannisbeergelee\nApfelmus\nKirschgelee"
						+ "\nPflaumenmus\nHimbeermarmelade\n", cs.getCode());
	}

	@Test
	public void testAdd() {
		// normal cases
		CodeSnippet cs = new CodeSnippet();
		cs.add("Schokolade", "Pfefferminzbonbon", "Brausepulver",
				"Schokoriegel");
		assertEquals(
				"Schokolade\nPfefferminzbonbon\nBrausepulver\nSchokoriegel\n",
				cs.getCode());
		cs.add("");
		assertEquals(
				"Schokolade\nPfefferminzbonbon\nBrausepulver\nSchokoriegel\n\n",
				cs.getCode());
		cs.clear();
		cs.add("", "Lakritz", "", "Karamell", "");
		assertEquals("\nLakritz\n\nKaramell\n\n", cs.getCode());
		cs.add("Schokofrosch", "", "");
		assertEquals("\nLakritz\n\nKaramell\n\nSchokofrosch\n\n\n", cs
				.getCode());

		// border cases
		CodeSnippet cs2 = new CodeSnippet("");
		cs2.add("");
		assertEquals("\n\n", cs2.getCode());
		cs2.add("");
		assertEquals("\n\n\n", cs2.getCode());
		cs.clear();
		cs.add("Praline");
		assertEquals("Praline\n", cs.getCode());
	}

	@Test
	public void testGetCode() {
		// normal cases
		// tests if calling getCode() with some kind of number results in the
		// same
		// number of tabulators in between the Strings
		CodeSnippet cs = new CodeSnippet("gelb", "orange", "rot");
		assertEquals("\t\t\t\t\tgelb\n\t\t\t\t\torange\n\t\t\t\t\trot\n", cs
				.getCode(5));
		assertEquals("\t\t\t\tgelb\n\t\t\t\torange\n\t\t\t\trot\n", cs
				.getCode(4));
		cs.add("grün", "blau");
		assertEquals("\t\tgelb\n\t\torange\n\t\trot\n\t\tgrün\n\t\tblau\n", cs
				.getCode(2));
		cs.add("Tulpe", "", "Buschwindroeschen");
		assertEquals(
				"\t\t\t\t\t\t\tgelb\n\t\t\t\t\t\t\torange\n\t\t\t\t\t\t\trot\n"
						+ "\t\t\t\t\t\t\tgrün\n\t\t\t\t\t\t\tblau\n\t\t\t\t\t\t\tTulpe\n"
						+ "\t\t\t\t\t\t\t\n\t\t\t\t\t\t\tBuschwindroeschen\n",
				cs.getCode(7));
		cs.clear();
		cs.add("", "Curry", "", "", "Paprika", "");
		assertEquals(
				"\t\t\t\n\t\t\tCurry\n\t\t\t\n\t\t\t\n\t\t\tPaprika\n\t\t\t\n",
				cs.getCode(3));

		// border cases
		// tests if an empty CodeSnippet results in an empty String
		cs.clear();
		assertEquals("", cs.getCode(12));

		// tests if adding an empty String adds an \n
		cs.add("");
		System.out.println(cs.getCode(0));
		assertEquals("\n", cs.getCode(0));

		cs.clear();
		cs.add("violett");
		assertEquals("\tviolett\n", cs.getCode(1));

		// tests if calling getCode() with negative values is handled correctly
		assertEquals("violett\n", cs.getCode(-1));
		cs.add("schwarz");
		assertEquals("violett\nschwarz\n", cs.getCode(-10));
	}

	@Test
	// tests the inherited getCode()-method
	public void testGetCode2() {
		// border cases
		CodeSnippet cs = new CodeSnippet("");
		assertEquals("\n", cs.getCode());

		cs = new CodeSnippet("", "");
		assertEquals("\n\n", cs.getCode());

		cs = new CodeSnippet("gelb");
		assertEquals("gelb\n", cs.getCode());

		// normal cases
		cs = new CodeSnippet("orange", "rot", "braun");
		assertEquals("orange\nrot\nbraun\n", cs.getCode());

		cs = new CodeSnippet("rosa", "pink", "rosarot", "violett", "lila");
		assertEquals("rosa\npink\nrosarot\nviolett\nlila\n", cs.getCode());

		cs = new CodeSnippet("blau", "grün", "", "türkis", "");
		assertEquals("blau\ngrün\n\ntürkis\n\n", cs.getCode());

		cs = new CodeSnippet("", "azurblau", "meergrün", "preußischblau", "",
				"", "ultramarin");
		assertEquals("\nazurblau\nmeergrün\npreußischblau\n\n\nultramarin\n",
				cs.getCode());

		cs = new CodeSnippet("onyx", "rauchschwarz", "", "grau", "weiß");
		assertEquals("onyx\nrauchschwarz\n\ngrau\nweiß\n", cs.getCode());
	}

	@Test
	public void testClear() {
		// normal cases
		CodeSnippet cs = new CodeSnippet("Java", "C#", "C++", "Pascal");
		cs.clear();
		assertEquals(0, cs.size());
		assertEquals("", cs.getCode());
		cs.add("Perl", "Phyton", "", "Ada");
		cs.clear();
		assertEquals(0, cs.size());
		assertEquals("", cs.getCode());
		cs.add("", "Ruby", "Smalltalk", "", "", "Haskell", "Prolog", "D", "");
		cs.clear();
		assertEquals(0, cs.size());
		assertEquals("", cs.getCode());

		// border cases
		assertEquals(0, cs.size());
		cs.clear();
		assertEquals(0, cs.size());
	}

	@Test
	public void testSize() {
		// normal cases
		CodeSnippet cs = new CodeSnippet("Orangensaft", "Apfelschorle");
		assertEquals(2, cs.size());
		cs.add("Kakao", "Kaffee", "Mangosaft", "Limonade");
		assertEquals(6, cs.size());
		cs.add("Kakao");
		assertEquals(7, cs.size());
		cs.add("Orangensaft", "Limonade", "Kaffee");
		assertEquals(10, cs.size());
		cs.add("Kaffee");
		assertEquals(11, cs.size());

		// border cases
		cs.clear();
		assertEquals(0, cs.size());
		cs.add("Füller");
		assertEquals(1, cs.size());
		cs.add("");
		assertEquals(2, cs.size());

	}

	@Test
	public void testAddAndGetVariables() {
		// border cases
		CodeSnippet cs = new CodeSnippet();
		Map<String, String> testMap = new HashMap<String, String>();
		cs.addVariables(testMap);
		assertEquals("*UNDEFINED:a*", cs.getVariable("a"));

		testMap.put(null, null);
		cs.addVariables(testMap);
		assertEquals(null, cs.getVariable(null));

		testMap.put(null, "");
		cs.addVariables(testMap);
		assertEquals("", cs.getVariable(null));

		testMap.put(null, "schwarz");
		cs.addVariables(testMap);
		assertEquals("schwarz", cs.getVariable(null));

		testMap.put("", null);
		cs.addVariables(testMap);
		assertEquals(null, cs.getVariable(""));

		testMap.put("", "");
		cs.addVariables(testMap);
		assertEquals("", cs.getVariable(""));

		testMap.put("", "weiß");
		cs.addVariables(testMap);
		assertEquals("weiß", cs.getVariable(""));

		testMap.put("schwarz", null);
		cs.addVariables(testMap);
		assertEquals(null, cs.getVariable("schwarz"));

		testMap.put("weiß", "");
		cs.addVariables(testMap);
		assertEquals("", cs.getVariable("weiß"));

		// some undefined variables
		assertEquals("*UNDEFINED:k*", cs.getVariable("k"));
		assertEquals("*UNDEFINED:rosa*", cs.getVariable("rosa"));
		assertEquals("*UNDEFINED:+*", cs.getVariable("+"));

		// normal cases
		testMap.put("gelb", "zitron");
		cs.addVariables(testMap);
		assertEquals("zitron", cs.getVariable("gelb"));

		testMap.put("gelb", "sonnen");
		cs.addVariables(testMap);
		assertEquals("sonnen", cs.getVariable("gelb"));

		testMap.put("grün", "tannen");
		cs.addVariables(testMap);
		assertEquals("tannen", cs.getVariable("grün"));

		testMap.put("violett", "flieder");
		cs.addVariables(testMap);
		assertEquals("flieder", cs.getVariable("violett"));

		testMap.put("rot", "wein");
		cs.addVariables(testMap);
		assertEquals("wein", cs.getVariable("rot"));

		testMap.put("rot", "blut");
		cs.addVariables(testMap);
		assertEquals("blut", cs.getVariable("rot"));
	}

	@Test
	public void testSetVariable() {
		// border cases
		CodeSnippet cs = new CodeSnippet();
		cs.setVariable(null, null);
		assertEquals(null, cs.getVariable(null));

		cs.setVariable(null, "");
		assertEquals("", cs.getVariable(null));

		cs.setVariable(null, "weiß");
		assertEquals("weiß", cs.getVariable(null));

		cs.setVariable("", null);
		assertEquals(null, cs.getVariable(""));

		cs.setVariable("", "");
		assertEquals("", cs.getVariable(""));

		cs.setVariable("", "schwarz");
		assertEquals("schwarz", cs.getVariable(""));

		cs.setVariable("schneeweiß", null);
		assertEquals(null, cs.getVariable("schneeweiß"));

		cs.setVariable("nachtschwarz", "");
		assertEquals("", cs.getVariable("nachtschwarz"));

		// some undefined variables
		assertEquals("*UNDEFINED:orange*", cs.getVariable("orange"));
		assertEquals("*UNDEFINED:gelb*", cs.getVariable("gelb"));
		assertEquals("*UNDEFINED:violett*", cs.getVariable("violett"));

		// normal cases
		cs.setVariable("blau", "ultramarin");
		assertEquals("ultramarin", cs.getVariable("blau"));

		cs.setVariable("blau", "azur");
		assertEquals("azur", cs.getVariable("blau"));

		cs.setVariable("rot", "feuer");
		assertEquals("feuer", cs.getVariable("rot"));

		cs.setVariable("rot", "bordeaux");
		assertEquals("bordeaux", cs.getVariable("rot"));

		cs.setVariable("grün", "gras");
		assertEquals("gras", cs.getVariable("grün"));
	}

	@Test
	public void testGetParent() {
		CodeSnippet cs = new CodeSnippet();
		assertEquals(null, cs.getParent());

		CodeList cl1 = new CodeList();
		cl1.setVariable("parent1", "0");
		CodeList cl2 = new CodeList(cl1);
		cl2.setVariable("parent2", "1");
		CodeList cl3 = new CodeList(cl2);
		cl3.setVariable("parent3", "2");
		cs = new CodeSnippet(cl1);
		assertEquals(cl1, cs.getParent());
		assertEquals(null, cs.getParent().getParent());

		cs = new CodeSnippet(cl2);
		assertEquals(cl2, cs.getParent());
		assertEquals(cl1, cs.getParent().getParent());
		assertEquals(null, cs.getParent().getParent().getParent());

		cs = new CodeSnippet(cl3);
		assertEquals(cl3, cs.getParent());
		assertEquals(cl2, cs.getParent().getParent());
		assertEquals(cl1, cs.getParent().getParent().getParent());
		assertEquals(null, cs.getParent().getParent().getParent().getParent());

		// tests dealings with circles
		cl1 = new CodeList(cl2);
		cl1.setVariable("parent4", "2");
		cs = new CodeSnippet(cl1);
		assertEquals(cl1, cs.getParent());
		assertEquals(cl2, cs.getParent().getParent());
		assertEquals("2", cs.getParent().getVariable("parent4"));
		assertEquals("0", cs.getParent().getVariable("parent1"));
		assertEquals("0", cs.getParent().getParent().getParent().getVariable(
				"parent1"));
		assertEquals("*UNDEFINED:parent4*", cs.getParent().getParent()
				.getParent().getVariable("parent4"));
		assertEquals(null, cs.getParent().getParent().getParent().getParent());
	}
}
