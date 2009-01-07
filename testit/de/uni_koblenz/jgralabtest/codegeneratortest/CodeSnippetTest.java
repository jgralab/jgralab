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
		cs1.add("Ananas");
		cl.add(cs1);
		cs3=new CodeSnippet(cl, "Mango", "Pflaume", "Limette");
		assertEquals("Mango\nPflaume\nLimette\n", cs3.getCode());
		cl.clear();
		cl.add(cs1, 2);
		cs3=new CodeSnippet(cl, "Mango", "Avocado");
		assertEquals("Mango\nAvocado\n", cs3.getCode());
		
		//border cases
		
		//tests with an empty CodeList
		cl.clear();
		cs3=new CodeSnippet(cl, "Mango", "Avocado");
		assertEquals("Mango\nAvocado\n", cs3.getCode());
		
		cs3=new CodeSnippet(null, null);
		assertEquals("", cs3.getCode());
		cs3=new CodeSnippet(cl, null);
		assertEquals("", cs3.getCode());
		cs3=new CodeSnippet(cl, "Ginko");
		assertEquals("Ginko\n", cs3.getCode());
		
		//faults

		//error message: constructor CodeSnippet(String[]) is ambiguous
		//cs3=new CodeSnippet(null, "Birke");
		//assertEquals("Birke\n", cs3.getCode());
	}
	
	
	@Test
	public void testCodeSnippet5(){
		//normal cases
		
		
		//border cases
		cs3=new CodeSnippet(cl, true, "Gurke", "Tomate");
		assertEquals("\nGurke\nTomate\n", cs3.getCode());
		cs3=new CodeSnippet(cl, false, "Schnittlauch");
		assertEquals("Schnittlauch\n", cs3.getCode());
		cs3=new CodeSnippet(null, true, "Melisse", "Minze");
		assertEquals("\nMelisse\nMinze\n", cs3.getCode());
		cs3=new CodeSnippet(cl, null, "Zimt", "Paprika", "Curry");

		//assertEquals("Zimt\nPaprika\nCurry\n", cs3.getCode());
	}
	
	@Test
	public void testSetNewLine(){
		
	}
	
	@Test
	public void testAdd(){
		//border cases
		cs3=new CodeSnippet("");
		cs3.add("");
		assertEquals("\n\n", cs3.getCode());
		cs3.add("");
		assertEquals("\n\n\n", cs3.getCode());
		cs3.add(null);
		assertEquals("\n\n\n", cs3.getCode());

	}
	
	@Test
	public void testGetCode(){
		//normal cases
		//tests if calling getCode() with some kind of number results in the same number of tabulators in between the Strings
		cs1.add("gelb", "orange", "rot");
		assertEquals("\t\t\t\t\tgelb\n\t\t\t\t\torange\n\t\t\t\t\trot\n", cs1.getCode(5));
		assertEquals("\t\t\t\tgelb\n\t\t\t\torange\n\t\t\t\trot\n", cs1.getCode(4));
		cs1.add("grün", "blau");
		assertEquals("\t\tgelb\n\t\torange\n\t\trot\n\t\tgrün\n\t\tblau\n", cs1.getCode(2));
		
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
		
		//faults
		//String s = cs2.getCode(Integer.MAX_VALUE);
		try{
			cs3=new CodeSnippet(cl, null, "Zimt", "Paprika", "Curry");
			String s=cs3.getCode(1);
			Assert.fail("NullPointerException expected!");
		}catch(NullPointerException e){
			
		}
		try{
			cs3=new CodeSnippet("Tulpe", null, "Buschwindröschen");
			cs3.getCode();
			Assert.fail("NullPointerException expected.");
		}catch(NullPointerException e){
			
		}
	}
	
	@Test
	public void testClear(){
		//normal cases
		cs1.add("Java", "C#", "C++", "Pascal");
		cs1.clear();
		assertEquals(0, cs1.size());
		
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
}
