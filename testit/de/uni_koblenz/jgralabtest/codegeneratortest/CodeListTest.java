package de.uni_koblenz.jgralabtest.codegeneratortest;

import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.assertEquals;
import org.junit.Assert;

import de.uni_koblenz.jgralab.codegenerator.CodeBlock;
import de.uni_koblenz.jgralab.codegenerator.CodeList;
import de.uni_koblenz.jgralab.codegenerator.CodeSnippet;

public class CodeListTest extends CodeBlockTest{
	
	private CodeList cl1;
	private CodeList cl2;
	
	@Before
	public void init(){
		super.init();
	}
	
	@Test
	public void testCodeList(){
		//only possible case using this constructor
		cl1=new CodeList();
		assertEquals(0, cl1.size());
		System.out.println(cl1.getCode(0));
	}
	
	@Test
	public void testCodeList2(){
		//border cases
		cl1=new CodeList();
		cl1=new CodeList(cl1);
		cl2=new CodeList(cl2);
		assertEquals(0,cl1.size());
		assertEquals(0,cl2.size());
		
		//normal cases
		CodeSnippet cs=new CodeSnippet("Impressionismus", "Expressionismus");
		cl1.add(cs);
		cl2=new CodeList(cl1);
		assertEquals(0, cl2.size());
		
		//TODO: How to test parent-stuff?
	}
	
	@Test
	public void testRemove(){
		
	}
	
	@Test
	public void testAdd(){
		//border cases
		cl1=new CodeList();
		cl1.add(null);
		assertEquals(0, cl1.size());
		CodeSnippet cs=new CodeSnippet();
		assertEquals(0, cl1.size());
		cs.add("");
		cl1.add(cs);
		assertEquals(1, cl1.size());
		cs.clear();
		cs.add("Impressionismus");
		cl1.add(cs);
		assertEquals(1, cl1.size());
		
		//normal cases
		cs.add("Expressionismus", "Jugendstil");
		cl1.add(cs);
		assertEquals(3, cl1.size());
		cs.add("Klassizismus", "Renaissance", "Pointillismus");
		cl1.add(cs);
		assertEquals(6, cl1.size());
		cs.add("", "Romantik", "Rokoko", "", "Realismus", "");
		cl1.add(cs);
		assertEquals(12, cl1.size());
	}
	
	@Test
	public void testAdd2(){
		//border cases
		cl1=new CodeList();
		cl1.add(null, 0);
		assertEquals(0, cl1.size());
		cl1.add(null, -4);
		assertEquals(0, cl1.size());
		CodeSnippet cs=new CodeSnippet();
		cl1.add(cs, -1);
		assertEquals(0, cl1.size());
		cs.add("");
		cl1.add(cs, 1);
		assertEquals(1, cl1.size());
		
		//normal cases
		cs.add("Gotik");
		cl1.add(cs, 7);
		assertEquals(2, cl1.size());
		cs.add("Kubismus", "", "Moderne", "Postmoderne", "");
		cl1.add(cs);
		assertEquals(7, cl1.size());
	}
	
	@Test
	public void testAddNoIndent(){
		
	}
	
	@Test
	public void testGetCode(){
		
	}
	
	@Test
	public void testClear(){
		//border cases
		cl1=new CodeList();
		cl1.clear();
		assertEquals(0, cl1.size());
		CodeSnippet cs=new CodeSnippet();
		cl1.add(cs);
		cl1.clear();
		assertEquals(0, cl1.size());
		cs.add("Monday");
		cl1.add(cs);
		cl1.clear();
		assertEquals(0, cl1.size());
		
		//normal cases
		cs.add("Tuesday", "Wednesday");
		cl1.add(cs);
		cl1.clear();
		assertEquals(0, cl1.size());
		cs.add("Thursday", "Friday", "Saturday", "Sunday");
		cl1.add(cs);
		cl1.clear();
		assertEquals(0, cl1.size());
	}
	
	@Test
	public void testSize(){
		//border cases
		cl1=new CodeList();
		assertEquals(0, cl1.size());
		cl1=new CodeList(cl1);
		assertEquals(0, cl1.size());
		cl2=new CodeList();
		cl1=new CodeList(cl2);
		assertEquals(0, cl1.size());
		CodeSnippet cs1=new CodeSnippet("January");
		cl1.add(cs1);
		assertEquals(1, cl1.size());
		
		//normal cases
		cs1.add("February", "March", "April");
		cl1.add(cs1);
		assertEquals(4, cl1.size());
		cs1.add("May");
		assertEquals(5, cl1.size());
		CodeSnippet cs2=new CodeSnippet("June", "July", "August");
		CodeSnippet cs3=new CodeSnippet("September", "October", "November", "December");
		cl1.add(cs2);
		assertEquals(8, cl1.size());
		cl1.add(cs3);
		assertEquals(12, cl1.size());
	}

}
