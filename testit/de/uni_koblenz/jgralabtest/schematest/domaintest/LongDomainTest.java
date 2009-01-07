package de.uni_koblenz.jgralabtest.schematest.domaintest;

import org.junit.Before;

public class LongDomainTest extends NativeDomainTest {

	@Before
	public void init() {
		super.init();
		// Initializing of DomainTest
		expectedDomainName = "Long";
		expectedJavaAttributeImplementationTypeName = "long";
		expectedJavaClassName = "Long";
		expectedTgTypeName = "Long";
		expectedStringRepresentation = "domain Long";
		expectedQualifiedName1 = "Long";
		expectedQualifiedName2 = "Long";
		expectedDirectoryName1 = "Long";
		expectedDirectoryName2 = "Long";
		expectedSimpleName = "Long";
		expectedUniqueName1 = "Long";
		expectedUniqueName2 = "Long";
		domain1 = schema1.getDomain("Long");
		domain2 = schema2.getDomain("Long");
		otherDomain1 = schema1.getDomain("Boolean");
		otherDomain2 = schema2.getDomain("String");
	}

}
