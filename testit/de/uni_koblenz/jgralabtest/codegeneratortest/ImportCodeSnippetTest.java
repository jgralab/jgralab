package de.uni_koblenz.jgralabtest.codegeneratortest;

import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.assertEquals;

import de.uni_koblenz.jgralab.codegenerator.ImportCodeSnippet;
import de.uni_koblenz.jgralab.codegenerator.CodeList;

public class ImportCodeSnippetTest extends CodeSnippetTest{
	
	protected ImportCodeSnippet ics1;
	protected ImportCodeSnippet ics2;
	protected ImportCodeSnippet ics3;
	protected ImportCodeSnippet ics4;
	protected CodeList cl1;
	
	@Before
	public void init(){
		super.init();
		ics3=new ImportCodeSnippet();
		ics4=new ImportCodeSnippet();
	}
	
	@Test
	public void testImportCodeSnippet(){
		ics1=new ImportCodeSnippet();
		//assertEquals(,ics1); Expected is what???
	}
	
	@Test
	public void testImportCodeSnippet2(){
		cl1=new CodeList(); //fill CodeList
		ics2=new ImportCodeSnippet(cl1);
	}
	
	@Test
	public void testAdd(){
	}
	
	@Test
	public void testGetCode(){
		
	}
	
	@Test
	public void testClear(){
		ics3.add("Alpha", "Beta", "Gamma", "Delta");
		ics3.clear();
		assertEquals(0, ics3.size());
		ics3.add("Epsilon", "Phi", "Xsi");
		ics3.clear();
		assertEquals(0, ics3.size());
	}
	
	@Test 
	public void testSize(){
		//Normalfälle
		ics3.add("Bla", "Blubb", "Blara");
		assertEquals(3, ics3.size());
		ics3.add("Blubbel", "Blamuh");
		assertEquals(5, ics3.size());
		
		//Grenzfälle
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
