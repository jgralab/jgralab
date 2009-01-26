package de.uni_koblenz.jgralabtest.codegeneratortest;

import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.assertEquals;

import de.uni_koblenz.jgralab.codegenerator.ImportCodeSnippet;
import de.uni_koblenz.jgralab.codegenerator.CodeList;
import de.uni_koblenz.jgralab.codegenerator.CodeSnippet;

public class ImportCodeSnippetTest extends CodeSnippetTest{
	
	protected ImportCodeSnippet ics1;
	protected ImportCodeSnippet ics3;
	protected ImportCodeSnippet ics4;
	protected CodeList cl1;
	protected CodeSnippet cs1;
	
	@Before
	public void init(){
		super.init();
		ics3=new ImportCodeSnippet();
		ics4=new ImportCodeSnippet();
	}
	
	@Test
	public void testImportCodeSnippet(){
		//only case in which this constructor may be used
		ics1=new ImportCodeSnippet();
		assertEquals(0,ics1.size());
		assertEquals("", ics1.getCode());
	}
	
	@Test
	public void testImportCodeSnippet2(){
		cs1=new CodeSnippet("Cabot", "Gaarder", "Sage", "Rowling");
		cl1=new CodeList();
		cl1.add(cs1);
		ics1=new ImportCodeSnippet(cl1);
		assertEquals(0, ics1.size());
		//no matter what CodeList is passed to the constructor, the ImportCodeSnippet
		//itself must always be empty
		assertEquals("", ics1.getCode());		
		//to make sure the parent was set correctly
		assertEquals("\tCabot\n\tGaarder\n\tSage\n\tRowling\n", ics1.getParent().getCode());
		cs1.add("Pratchett", "", "Pullman");
		cl1.add(cs1);
		ics1=new ImportCodeSnippet(cl1);
		assertEquals("", ics1.getCode());
		assertEquals("\tCabot\n\tGaarder\n\tSage\n\tRowling\n\tPratchett\n\t\n\tPullman\n", ics1.getParent().getCode());
		
		
		//border cases
		cl1.clear();
		ics1.clear();
		assertEquals(0,ics1.size());
		assertEquals("", ics1.getParent().getCode());
		assertEquals("", ics1.getCode());
		cs1.clear();
		cs1.add("");
		cl1.add(cs1);
		ics1=new ImportCodeSnippet(cl1);
		assertEquals("", ics1.getCode());
		assertEquals("\t\n", ics1.getParent().getCode());
		cs1.add("");
		cl1.add(cs1);
		ics1=new ImportCodeSnippet(cl1);
		assertEquals("", ics1.getCode());
		assertEquals("\t\n\t\n", ics1.getParent().getCode());
		cs1.clear();
		cs1.add("Canavan");
		cl1.add(cs1);
		ics1=new ImportCodeSnippet(cl1);
		assertEquals("", ics1.getCode());
		assertEquals("\tCanavan\n", ics1.getParent().getCode());
	}
	
	@Test
	public void testAdd(){
		//normal cases
		ics1=new ImportCodeSnippet();
		ics1.add("Der.", "Herr.", "der.", "Ringe.");
		assertEquals(4,ics1.size());
		assertEquals("\nimport Der.;\n\nimport Herr.;\n\nimport Ringe.;\n\nimport der.;\n", ics1.getCode(0));
		ics1.add("Sofies.", "Welt.");
		assertEquals(6, ics1.size());
		assertEquals("\nimport Der.;\n\nimport Herr.;\n\nimport Ringe.;\n\nimport Sofies.;\n\nimport Welt.;\n\nimport der.;\n", ics1.getCode());
		
		//border cases
		ics1.clear();
		ics1.add("Queste.");
		assertEquals(1, ics1.size());
		assertEquals("\nimport Queste.;\n", ics1.getCode());
		ics1.clear();
		ics1.add(null);
		assertEquals(0, ics1.size());
		assertEquals("", ics1.getCode());
		ics1.add("");
		assertEquals(1, ics1.size());
		ics1.add("eclipse.", "");
		assertEquals(2, ics1.size());
		ics1.add("");
		assertEquals(2, ics1.size());
		ics1.add(null);
		assertEquals(2, ics1.size());
		ics1.clear();
		ics1.add(".");
		assertEquals(1, ics1.size());
		assertEquals("\nimport .;\n", ics1.getCode());
	}
	
	@Test
	public void testGetCode(){
		ics1=new ImportCodeSnippet();
		ics1.add("Harry.", "Potter.", "und.", "der.", "Stein.", "der.", "Weisen.");
		assertEquals("\n\t\timport Harry.;\n\t\t\n\t\timport Potter.;\n\t\t\n\t\t" +
				"import Stein.;\n\t\t\n\t\timport Weisen.;\n\t\t\n\t\timport der.;\n\t\t\n\t\t" +
				"import und.;\n", ics1.getCode(2));
		ics1.clear();
		ics1.add("Kammer.des.Schreckens", "Gefangene.von.Askaban");
		assertEquals("\n\t\t\t\t\t\t\t\t\timport Gefangene.von.Askaban;\n\t\t\t\t\t\t\t\t\t" +
				"\n\t\t\t\t\t\t\t\t\timport Kammer.des.Schreckens;\n", ics1.getCode(9));
		ics1.add("Feuerkelch.", "Orden.des.Phoenix");
		assertEquals("\n\timport Feuerkelch.;\n\t\n\timport " +
				"Gefangene.von.Askaban;\n\t\n\timport Kammer.des.Schreckens;\n\t\n\timport " +
				"Orden.des.Phoenix;\n", ics1.getCode(1));
		ics1.clear();
		ics1.add(".", "Halbblutprinz.", ".", "Heiligtuemer.des.Todes", ".", ".");
		System.out.println(ics1.getCode(3));
		assertEquals("\n\t\t\timport .;\n\t\t\t\n\t\t\timport Halbblutprinz.;\n\t\t\t" +
				"\n\t\t\timport Heiligtuemer.des.Todes;\n", ics1.getCode(3));
		
		//border cases
		ics1.clear();
		ics1.add("Der.Clan.der.Otori");
		assertEquals("\n\timport Der.Clan.der.Otori;\n", ics1.getCode(1));
		ics1.clear();
		ics1.add(".");
		assertEquals("\n\t\t\t\t\timport .;\n", ics1.getCode(5));
		ics1.add(".", ".", ".");
		assertEquals("\nimport .;\n", ics1.getCode(0));
		assertEquals("\nimport .;\n", ics1.getCode(-20));
	}
	
	@Test
	public void testClear(){
		//normal cases
		ics3.add("Alpha", "Beta", "Gamma", "Delta");
		ics3.clear();
		assertEquals(0, ics3.size());
		try{
			ics3.getParent();
		}catch(NullPointerException e){
			// :)
		}
		ics3.add("Epsilon", "Phi", "Xsi");
		ics3.clear();
		assertEquals(0, ics3.size());
		try{
			ics3.getParent();
		}catch(NullPointerException e){
			// :)
		}
		
		//border cases
		ics3=new ImportCodeSnippet();
		ics3.clear();
		assertEquals(0, ics3.size());
		try{
			ics3.getParent();
		}catch(NullPointerException e){
			// :)
		}
		ics3.clear();
		assertEquals(0, ics3.size());		
		try{
			ics3.getParent();
		}catch(NullPointerException e){
			// :)
		}
	}
	
	@Test 
	public void testSize(){
		//normal cases
		ics3.add("Bla", "Blubb", "Blara");
		assertEquals(3, ics3.size());
		ics3.add("Blubbel", "Blamuh");
		assertEquals(5, ics3.size());
		
		//border cases
		ics3.add("Blamuh");
		assertEquals(5,ics3.size());
		assertEquals(0, ics4.size());
		ics4.add("Bla");
		assertEquals(1, ics4.size());
		ics4.add("Bla");
		assertEquals(1,ics4.size());
		ics4.add("");
		assertEquals(2,ics4.size());
		ics4.add(null);
		assertEquals(2, ics4.size());
		ics4.clear();
		ics4.add("");
		assertEquals(1, ics4.size());
		ics4.add(".");
		assertEquals(2, ics4.size());
		ics4.clear();
		ics4.add(null);
		assertEquals(0, ics4.size());
		ics4.add(".");
		assertEquals(1, ics4.size());
	}

}
