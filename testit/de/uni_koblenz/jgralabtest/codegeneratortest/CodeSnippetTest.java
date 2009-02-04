package de.uni_koblenz.jgralabtest.codegeneratortest;

import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.assertEquals;
import org.junit.Assert;

import de.uni_koblenz.jgralab.codegenerator.CodeSnippet;
import de.uni_koblenz.jgralab.codegenerator.CodeList;

public class CodeSnippetTest extends CodeBlockTest{
	
	protected CodeSnippet cs1;
	protected CodeSnippet cs2;
	protected CodeSnippet cs3;
	protected CodeList cl;
	
	@Before
	public void init(){
		super.init();
		cs1=new CodeSnippet();
		cs2=new CodeSnippet();
		cl=new CodeList();
	}
	
	@Test
	public void testCodeSnippet(){
		//tests the only case in which this constructor may be used
		cs3=new CodeSnippet();
		assertEquals("", cs3.getCode());
	}
	
	@Test
	public void testCodeSnippet2(){
		//normal cases
		cs3=new CodeSnippet("Apfel", "Birne", "Kirsche");
		assertEquals("Apfel\nBirne\nKirsche\n", cs3.getCode());
		cs3=new CodeSnippet("Ahorn", "Kastanie", "Birke", "Esche", "Buche");
		assertEquals("Ahorn\nKastanie\nBirke\nEsche\nBuche\n", cs3.getCode());
		cs3=new CodeSnippet("Ginko", "Flieder", "Eiche");
		assertEquals("Ginko\nFlieder\nEiche\n", cs3.getCode());
		
		//border cases
		cs3=new CodeSnippet("");
		assertEquals("\n", cs3.getCode());
		cs3=new CodeSnippet(null);
		assertEquals("", cs3.getCode());
		cs3=new CodeSnippet("Pflaume");
		assertEquals("Pflaume\n", cs3.getCode());
	}

	@Test
	public void testCodeSnippet3(){
		//normal cases
		cs3=new CodeSnippet(true, "Erdbeere", "Blaubeere", "Himbeere");
		assertEquals("\nErdbeere\nBlaubeere\nHimbeere\n", cs3.getCode());
		cs3=new CodeSnippet(false, "Brombeere", "Johannisbeere");
		assertEquals("Brombeere\nJohannisbeere\n", cs3.getCode());
		cs3=new CodeSnippet(false, "Moltebeere", "", "Stachelbeere");
		assertEquals("Moltebeere\n\nStachelbeere\n", cs3.getCode());
		cs3=new CodeSnippet(true, "", "Preiselbeere");
		assertEquals("\n\nPreiselbeere\n", cs3.getCode());
		cs3=new CodeSnippet(true, "Granatapfel", "", "Paprika", "");
		assertEquals("\nGranatapfel\n\nPaprika\n\n", cs3.getCode());
		
		//border cases
		cs3=new CodeSnippet(true, "");
		assertEquals("\n\n", cs3.getCode());
		cs3=new CodeSnippet(false, "");
		assertEquals("\n", cs3.getCode());
		cs3=new CodeSnippet(true, null);
		assertEquals("", cs3.getCode());
		cs3=new CodeSnippet(false, null);
		assertEquals("", cs3.getCode());
	}
	
	@Test
	public void testCodeSnippet4(){		
		//normal cases
		cs3=new CodeSnippet(cl, "Mango", "Avocado");
		assertEquals("Mango\nAvocado\n", cs3.getCode());
		assertEquals("\tMango\n\tAvocado\n", cl.getCode());//to make sure the given
		//CodeList was changed accordingly
		cs1.add("Ananas");
		cl.add(cs1);
		cs3=new CodeSnippet(cl, "Mango", "Pflaume", "Limette");
		assertEquals("Mango\nPflaume\nLimette\n", cs3.getCode());
		assertEquals("\tMango\n\tAvocado\n\tAnanas\n\tMango\n\tPflaume\n\tLimette\n", cl.getCode());
		cl.clear();
		cl.add(cs1, 2);
		cs3=new CodeSnippet(cl, "Mango", "Avocado");
		assertEquals("Mango\nAvocado\n", cs3.getCode());
		assertEquals("\t\t\tAnanas\n\tMango\n\tAvocado\n", cl.getCode());
		cl.clear();
		cs3=new CodeSnippet(cl, "Mandarine", "Orange", "Grapefruit", "Pampelmuse");
		assertEquals("Mandarine\nOrange\nGrapefruit\nPampelmuse\n", cs3.getCode());
		assertEquals("\tMandarine\n\tOrange\n\tGrapefruit\n\tPampelmuse\n", cl.getCode());
		cs1.add("Drachenfrucht", "Affenbrot", "Guave");
		cl.add(cs1, 1);
		cs3=new CodeSnippet(cl, "Feige", "Granatapfel");
		assertEquals("Feige\nGranatapfel\n", cs3.getCode());
		assertEquals("\tMandarine\n\tOrange\n\tGrapefruit\n\tPampelmuse\n\t\tAnanas" +
				"\n\t\tDrachenfrucht\n\t\tAffenbrot\n\t\tGuave\n\tFeige\n\tGranatapfel\n", 
				cl.getCode());
		
		//border cases
		//tests with an empty CodeList
		cl.clear();
		cl.add(cs1, 4);
		cs3=new CodeSnippet(cl, "Dattel");
		assertEquals("Dattel\n", cs3.getCode());
		assertEquals("\t\t\t\t\tAnanas\n\t\t\t\t\tDrachenfrucht\n\t\t\t\t\tAffenbrot" +
				"\n\t\t\t\t\tGuave\n\tDattel\n", cl.getCode());
		cl.clear();
		cs3=new CodeSnippet(cl, "Mango", "Avocado");
		assertEquals("Mango\nAvocado\n", cs3.getCode());
		assertEquals("\tMango\n\tAvocado\n", cl.getCode());
		cs3=new CodeSnippet(cl, "");
		assertEquals("\n", cs3.getCode());
		assertEquals("\tMango\n\tAvocado\n\t\n", cl.getCode());
		cs3=new CodeSnippet(null, null);
		assertEquals("", cs3.getCode());
		cl.clear();
		cs3=new CodeSnippet(cl, null);
		assertEquals("", cs3.getCode());
		assertEquals("", cl.getCode());
		cs3=new CodeSnippet(cl, "Ginkgo");
		assertEquals("Ginkgo\n", cs3.getCode());
		assertEquals("\tGinkgo\n", cl.getCode());
	}
	
	
	@Test
	public void testCodeSnippet5(){
		//normal cases
		cs1.add("Bärlauch", "Dill", "Estragon");
		cl.add(cs1);
		cs3=new CodeSnippet(cl, true, "Fenchel", "Liebstöckel");
		assertEquals("\nFenchel\nLiebstöckel\n", cs3.getCode());
		assertEquals("\tBärlauch\n\tDill\n\tEstragon\n\n\tFenchel\n\tLiebstöckel\n", cl.getCode());
		cl.clear();
		cs1.add("Lorbeer");
		cl.add(cs1, 3);
		cs3=new CodeSnippet(cl, false, "Kresse", "Bohnenkraut", "Knöterich");
		assertEquals("Kresse\nBohnenkraut\nKnöterich\n", cs3.getCode());
		assertEquals("\t\t\t\tBärlauch\n\t\t\t\tDill\n\t\t\t\tEstragon\n\t\t\t\t" +
				"Lorbeer\n\tKresse\n\tBohnenkraut\n\tKnöterich\n", cl.getCode());

		
		//border cases
		cs3=new CodeSnippet(cl, true, "Kapuzinerkresse", "", "Löwenzahn", "");
		assertEquals("\nKapuzinerkresse\n\nLöwenzahn\n\n", cs3.getCode());
		assertEquals("\t\t\t\tBärlauch\n\t\t\t\tDill\n\t\t\t\tEstragon\n\t\t\t\t" +
				"Lorbeer\n\tKresse\n\tBohnenkraut\n\tKnöterich\n\n\tKapuzinerkresse" +
				"\n\t\n\tLöwenzahn\n\t\n", cl.getCode());
		cl.clear();
		cs3=new CodeSnippet(cl, true, "Gurke", "Tomate");
		assertEquals("\nGurke\nTomate\n", cs3.getCode());
		assertEquals("\n\tGurke\n\tTomate\n", cl.getCode());
		cs3=new CodeSnippet(cl, false, "Schnittlauch");
		assertEquals("Schnittlauch\n", cs3.getCode());
		assertEquals("\n\tGurke\n\tTomate\n\tSchnittlauch\n", cl.getCode());
		cs3=new CodeSnippet(null, true, "Melisse", "Minze");
		assertEquals("\nMelisse\nMinze\n", cs3.getCode());
		cl.clear();
		cs3=new CodeSnippet(cl, true, "", "");
		assertEquals("\n\n\n", cs3.getCode());
		assertEquals("\n\t\n\t\n", cl.getCode());
	}
	
	@Test
	public void testSetNewLine(){
		//normal cases
		cs1.setNewLine(true);
		assertEquals("", cs1.getCode());
		cs1.add("Ahornsirup");
		assertEquals("\nAhornsirup\n", cs1.getCode());
		cs1.add("Erdbeermarmelade");
		assertEquals("\nAhornsirup\nErdbeermarmelade\n", cs1.getCode());
		cs1.add("Honig");
		assertEquals("\nAhornsirup\nErdbeermarmelade\nHonig\n", cs1.getCode());
		cs1.clear();
		cs1.add("Nougatcreme", "Erdnussbutter", "Blaubeermarmelade");
		assertEquals("\nNougatcreme\nErdnussbutter\nBlaubeermarmelade\n", cs1.getCode());
		
		cs1.clear();
		cs1.setNewLine(false);
		cs1.add("Blaubeermarmelade", "Johannisbeergelee");
		assertEquals("Blaubeermarmelade\nJohannisbeergelee\n", cs1.getCode());
		cs1.add("Apfelmus");
		assertEquals("Blaubeermarmelade\nJohannisbeergelee\nApfelmus\n", cs1.getCode());
		cs1.add("Kirschgelee", "Pflaumenmus", "Himbeermarmelade");
		assertEquals("Blaubeermarmelade\nJohannisbeergelee\nApfelmus\nKirschgelee" +
				"\nPflaumenmus\nHimbeermarmelade\n", cs1.getCode());
	}
	
	@Test
	public void testAdd(){
		//normal cases
		cs2.add("Schokolade", "Pfefferminzbonbon", "Brausepulver", "Schokoriegel");
		assertEquals("Schokolade\nPfefferminzbonbon\nBrausepulver\nSchokoriegel\n", cs2.getCode());
		cs2.add("");
		assertEquals("Schokolade\nPfefferminzbonbon\nBrausepulver\nSchokoriegel\n\n", cs2.getCode());
		cs2.clear();
		cs2.add("", "Lakritz", "", "Karamell", "");
		assertEquals("\nLakritz\n\nKaramell\n\n", cs2.getCode());
		cs2.add("Schokofrosch", "", "");
		assertEquals("\nLakritz\n\nKaramell\n\nSchokofrosch\n\n\n", cs2.getCode());
		
		//border cases
		cs3=new CodeSnippet("");
		cs3.add("");
		assertEquals("\n\n", cs3.getCode());
		cs3.add("");
		assertEquals("\n\n\n", cs3.getCode());
		cs3.add(null);
		assertEquals("\n\n\n", cs3.getCode());
		cs2.clear();
		cs2.add("Praline");
		assertEquals("Praline\n", cs2.getCode());
	}
	
	@Test
	public void testGetCode(){
		//normal cases
		//tests if calling getCode() with some kind of number results in the same 
		//number of tabulators in between the Strings
		cs1.add("gelb", "orange", "rot");
		assertEquals("\t\t\t\t\tgelb\n\t\t\t\t\torange\n\t\t\t\t\trot\n", cs1.getCode(5));
		assertEquals("\t\t\t\tgelb\n\t\t\t\torange\n\t\t\t\trot\n", cs1.getCode(4));
		cs1.add("grün", "blau");
		assertEquals("\t\tgelb\n\t\torange\n\t\trot\n\t\tgrün\n\t\tblau\n", cs1.getCode(2));
		cs1.add("Tulpe", "", "Buschwindroeschen");
		assertEquals("\t\t\t\t\t\t\tgelb\n\t\t\t\t\t\t\torange\n\t\t\t\t\t\t\trot\n" +
				"\t\t\t\t\t\t\tgrün\n\t\t\t\t\t\t\tblau\n\t\t\t\t\t\t\tTulpe\n" +
				"\t\t\t\t\t\t\t\n\t\t\t\t\t\t\tBuschwindroeschen\n", cs1.getCode(7));
		cs1.clear();
		cs1.add("", "Curry", "", "", "Paprika", "");
		assertEquals("\t\t\t\n\t\t\tCurry\n\t\t\t\n\t\t\t\n\t\t\tPaprika\n\t\t\t\n", cs1.getCode(3));
		
		//border cases
		//tests if an empty CodeSnippet results in an empty String
		assertEquals("", cs2.getCode(12));
		
		//tests if adding null does not change the CodeSnippet
		cs2.add(null);
		assertEquals("", cs2.getCode(0));
		
		cs2.add("violett");
		assertEquals("\tviolett\n", cs2.getCode(1));
		
		//tests if calling getCode() with negative values is handled correctly
		assertEquals("violett\n", cs2.getCode(-1));
		cs2.add("schwarz");
		assertEquals("violett\nschwarz\n", cs2.getCode(-10));
	}
	
	@Test
	//tests the inherited getCode()-method
	public void testGetCode2(){
		
	}
	
	@Test
	public void testClear(){
		//normal cases
		cs1.add("Java", "C#", "C++", "Pascal");
		cs1.clear();
		assertEquals(0, cs1.size());
		assertEquals("", cs1.getCode());
		cs1.add("Perl", "Phyton", "", "Ada");
		cs1.clear();
		assertEquals(0, cs1.size());
		assertEquals("", cs1.getCode());
		cs1.add("", "Ruby", "Smalltalk", "", "", "Haskell", "Prolog", "D", "");
		cs1.clear();
		assertEquals(0, cs1.size());
		assertEquals("", cs1.getCode());
		
		//border cases
		assertEquals(0, cs1.size());
		cs1.add(null);
		cs1.clear();
		assertEquals(0, cs1.size());
		cs1.clear();
		assertEquals(0, cs1.size());		
	}
	
	@Test
	public void testSize(){
		//normal cases
		cs1.add("Orangensaft", "Apfelschorle");
		assertEquals(2, cs1.size());
		cs1.add("Kakao", "Kaffee", "Mangosaft", "Limonade");
		assertEquals(6, cs1.size());
		cs1.add("Kakao");
		assertEquals(7, cs1.size());
		cs1.add("Orangensaft", "Limonade", "Kaffee");
		assertEquals(10, cs1.size());
		cs1.add("Kaffee");
		assertEquals(11, cs1.size());
		
		//border cases
		assertEquals(0,cs2.size());
		cs2.add("Füller");
		assertEquals(1,cs2.size());
		cs2.add("");
		assertEquals(2,cs2.size());
		cs2.add(null);
		assertEquals(2,cs2.size());

	}
	
	@Test
	public void testAddVariables(){
		
	}
	
	@Test
	public void testSetVariable(){
		
	}
	
	@Test
	public void testGetVariable(){
		
	}
	
	@Test
	public void testReplaceVariables(){
		
	}
	
	@Test
	public void testGetParent(){
		
	}
	
	@Test
	public void testSetParent(){
		
	}
}
