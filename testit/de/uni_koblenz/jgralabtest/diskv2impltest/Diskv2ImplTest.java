package de.uni_koblenz.jgralabtest.diskv2impltest;

import org.junit.Test;

import de.uni_koblenz.jgralab.GraphIO;
import de.uni_koblenz.jgralab.exception.GraphIOException;
import de.uni_koblenz.jgralab.schema.Schema;
import de.uni_koblenz.jgralab.schema.codegenerator.CodeGenerator;
import de.uni_koblenz.jgralab.schema.codegenerator.CodeGeneratorConfiguration;

public class Diskv2ImplTest {

	
	@Test
	public void test() throws GraphIOException{
		Schema s = GraphIO.loadSchemaFromFile("testit/testschemas/citymapschema2.tg");
		s.commit("testit",CodeGeneratorConfiguration.WITH_DISKV2_SUPPORT);
		
	}
	
}
