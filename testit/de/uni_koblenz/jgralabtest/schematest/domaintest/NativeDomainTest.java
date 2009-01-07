package de.uni_koblenz.jgralabtest.schematest.domaintest;

import org.junit.Test;

public abstract class NativeDomainTest extends BasicDomainTest {

	public void init() {
		super.init();
		expectedPackage1 = "";
		expectedPackage2 = "";
		expectedPathName1 = "";
		expectedPathName2 = "";
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

}
