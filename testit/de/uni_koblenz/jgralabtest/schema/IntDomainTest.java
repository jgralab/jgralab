package de.uni_koblenz.jgralabtest.schema;

import org.junit.Before;

public class IntDomainTest extends NativeDomainTest {

	@Before
	public void init() {
		super.init();
		// Initializing of DomainTest
		expectedDomainName = "Integer";
		expectedJavaAttributeImplementationTypeName = "int";
		expectedJavaClassName = "Integer";
		expectedTgTypeName = "Integer";
		expectedStringRepresentation = "domain Integer";
		expectedQualifiedName1 = "Integer";
		expectedQualifiedName2 = "Integer";
		expectedDirectoryName1 = "Integer";
		expectedDirectoryName2 = "Integer";
		expectedSimpleName = "Integer";
		expectedUniqueName1 = "Integer";
		expectedUniqueName2 = "Integer";
		domain1 = schema1.getDomain("Integer");
		domain2 = schema2.getDomain("Integer");
		otherDomain1 = schema1.getDomain("Boolean");
		otherDomain2 = schema2.getDomain("String");
	}
}
