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
		//to make sure I don´t think something wrong about what is inside the CodeList
		System.out.println(cl1.getCode());
		ics1=new ImportCodeSnippet(cl1);
		assertEquals(0, ics1.size());
		assertEquals("", ics1.getCode());
		//TODO what else has to be controlled...
		//no matter what CodeList is given the size of the ImportCodeSnippet always
		//has to be zero
		//how can I test what else is happening? perhaps with getCode as it returns
		//the super-class-String!!!!!!!!!!!
		
		/*assertEquals("\nCabot\nGaarder\nSage\nRowling\n" ,ics1.getCode(0));
		 * should not work! getCode overwrites what is in the CodeSnippet with
		 * what is in ImportCodeSnipper
		 */
		
		//border cases
		cl1.clear();
		ics1.clear();
		assertEquals(0,ics1.size());
		assertEquals("", ics1.getCode());
	}
	
	@Test
	public void testAdd(){
		ics1=new ImportCodeSnippet();
		ics1.add("Der.", "Herr.", "der.", "Ringe.");
		assertEquals(4,ics1.size());
		assertEquals("\nimport Der.;\n\nimport Herr.;\n\nimport Ringe.;\n\nimport der.;\n", ics1.getCode(0));
	}
	
	@Test
	public void testGetCode(){
		
	}
	
	@Test
	public void testClear(){
		//normal cases
		ics3.add("Alpha", "Beta", "Gamma", "Delta");
		ics3.clear();
		assertEquals(0, ics3.size());
		ics3.add("Epsilon", "Phi", "Xsi");
		ics3.clear();
		assertEquals(0, ics3.size());
		
		//border cases
		ics3=new ImportCodeSnippet();
		ics3.clear();
		assertEquals(0, ics3.size());
		ics3.clear();
		assertEquals(0, ics3.size());
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
	}

}
