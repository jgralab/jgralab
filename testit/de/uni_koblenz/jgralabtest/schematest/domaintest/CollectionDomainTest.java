package de.uni_koblenz.jgralabtest.schematest.domaintest;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import de.uni_koblenz.jgralab.schema.CollectionDomain;
import de.uni_koblenz.jgralab.schema.Domain;
import de.uni_koblenz.jgralab.schema.QualifiedName;
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

	@Test(expected = UnsupportedOperationException.class)
	@Override
	public void testSetPackage() {
		// tests if changes of the package are rejected
		schema1.createPackageWithParents(schema1Package + ".subpackage");
		domain1.setPackage(schema1.getPackage(schema1Package + ".subpackage"));
	}

	@Test(expected = UnsupportedOperationException.class)
	@Override
	public void testSetUniqueName1() {
		// tests if changes of the uniqueName are rejected
		domain1.setUniqueName("Hugo");
	}

	@Test
	@Override
	public void testSetUniqueName2() {
	}

	@Test
	@Override
	public void testSetUniqueName3() {
	}

	@Test(expected = WrongSchemaException.class)
	public void testRejectionOfForeignSchemas() {
		// test if baseDomains of a foreign schema are rejected
		schema2.createEnumDomain(new QualifiedName("enum1"));
		schema1.createListDomain(schema2.getDomain("enum1"));
	}
}
