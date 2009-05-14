package de.uni_koblenz.jgralabtest.schema;

import org.junit.Before;

public class StringDomainTest extends NativeDomainTest {

	@Before
	public void init() {
		super.init();
		// Initializing of DomainTest
		expectedDomainName = "String";
		expectedJavaAttributeImplementationTypeName = "java.lang.String";
		expectedJavaClassName = "java.lang.String";
		expectedTgTypeName = "String";
		expectedStringRepresentation = "domain String";
		expectedQualifiedName1 = "String";
		expectedQualifiedName2 = "String";
		expectedDirectoryName1 = "String";
		expectedDirectoryName2 = "String";
		expectedSimpleName = "String";
		expectedUniqueName1 = "String";
		expectedUniqueName2 = "String";
		domain1 = schema1.getDomain("String");
		domain2 = schema2.getDomain("String");
		otherDomain1 = schema1.getDomain("Boolean");
		otherDomain2 = schema2.getDomain("Double");
	}

}
