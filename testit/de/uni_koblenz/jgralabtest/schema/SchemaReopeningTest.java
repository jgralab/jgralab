package de.uni_koblenz.jgralabtest.schema;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.lang.reflect.InvocationTargetException;

import org.junit.Test;

import de.uni_koblenz.jgralab.GraphIO;
import de.uni_koblenz.jgralab.GraphIOException;
import de.uni_koblenz.jgralab.codegenerator.CodeGeneratorConfiguration;
import de.uni_koblenz.jgralab.greql.schema.Greql2Schema;
import de.uni_koblenz.jgralab.schema.Schema;
import de.uni_koblenz.jgralab.schema.impl.SchemaImpl;
import de.uni_koblenz.jgralab.schema.impl.compilation.SchemaClassManager;
import de.uni_koblenz.jgralabtest.schemas.greqltestschema.RouteSchema;

public class SchemaReopeningTest {

	@Test
	public void testSchemaReopening() throws GraphIOException {
		Schema greqlTestSchema = GraphIO
				.loadSchemaFromFile("testit/testschemas/greqltestschema.tg");
		int hash = greqlTestSchema.hashCode();
		// finished after loading
		assertTrue(greqlTestSchema.isFinished());
		// finishing again has no effect
		assertFalse(greqlTestSchema.finish());
		// reopening has an effect
		assertTrue(greqlTestSchema.reopen());
		// and now it's really open
		assertFalse(greqlTestSchema.isFinished());
		// after being open, reopening again has no effecz
		assertFalse(greqlTestSchema.reopen());
		// hashCode() should not depend on openness
		assertEquals(hash, greqlTestSchema.hashCode());
		// finishing again has an effect
		assertTrue(greqlTestSchema.finish());
		// and now its finished
		assertTrue(greqlTestSchema.isFinished());
		// and still the hashCode is as it has ever been
		assertEquals(hash, greqlTestSchema.hashCode());
	}

	@Test(expected = UnsupportedOperationException.class)
	public void testSchemaReopeningWithCompiledSchema1() {
		RouteSchema.instance().reopen();
	}

	@Test(expected = UnsupportedOperationException.class)
	public void testSchemaReopeningWithCompiledSchema2() {
		Greql2Schema.instance().reopen();
	}

	@Test(expected = UnsupportedOperationException.class)
	public void testSchemaReopeningWithCompiledSchema3()
			throws ClassNotFoundException, IllegalAccessException,
			IllegalArgumentException, InvocationTargetException,
			NoSuchMethodException, SecurityException {
		Schema s = new SchemaImpl("ReopenTestSchema", "foo.reopen.test");
		s.createGraphClass("ReopenTestGraph");
		s.finish();
		s.compile(CodeGeneratorConfiguration.MINIMAL);
		Class<?> csc = Class
				.forName("foo.reopen.test.ReopenTestSchema", true,
						SchemaClassManager
								.instance("foo.reopen.test.ReopenTestSchema"));
		Schema cs = (Schema) csc.getMethod("instance").invoke(null);
		cs.reopen();
	}

}
