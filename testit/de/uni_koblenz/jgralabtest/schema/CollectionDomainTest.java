package de.uni_koblenz.jgralabtest.schema;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import de.uni_koblenz.jgralab.schema.CollectionDomain;
import de.uni_koblenz.jgralab.schema.Domain;
import de.uni_koblenz.jgralab.schema.exception.WrongSchemaException;

public class CollectionDomainTest extends CompositeDomainTest {

	protected Domain expectedBaseDomain;

	@Override
	public void init() {
		super.init();
		expectedPackage1 = "";
		expectedPackage2 = "";
		expectedPathName1 = "";
		expectedPathName2 = "";
	}

	@Test
	public void testGetBaseDomain() {
		// tests if the correct baseDomain is returned
		assertEquals(expectedBaseDomain, ((CollectionDomain) domain1)
				.getBaseDomain());
		assertEquals(expectedBaseDomain, ((CollectionDomain) domain2)
				.getBaseDomain());
	}


	@Test(expected = WrongSchemaException.class)
	public void testRejectionOfForeignSchemas() {
		// test if baseDomains of a foreign schema are rejected
		schema2.createEnumDomain("Enum1");
		schema1.createListDomain(schema2.getDomain("Enum1"));
	}
}
