package de.uni_koblenz.jgralabtest.codegeneratortest;

import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.assertEquals;
import org.junit.Assert;

import de.uni_koblenz.jgralab.codegenerator.CodeBlock;

public abstract class CodeBlockTest {

	@Before
	public void init(){
		
	}
	
	@Test
	public abstract void testGetCode();
	
	@Test
	public abstract void testClear();
	
	@Test
	public abstract void testSize();
	
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
	public void testGetCode2(){
		
	}
	
	@Test
	public void testGetParent(){
		
	}
}
