package de.uni_koblenz.jgralabtest.schema;

import org.junit.Before;

public class DoubleDomainTest extends NativeDomainTest {

	@Before
	public void init() {
		super.init();
		// Initializing DomainTest
		expectedDomainName = "Double";
		expectedJavaAttributeImplementationTypeName = "double";
		expectedJavaClassName = "Double";
		expectedTgTypeName = "Double";
		expectedStringRepresentation = "domain Double";
		expectedQualifiedName1 = "Double";
		expectedQualifiedName2 = "Double";
		expectedDirectoryName1 = "Double";
		expectedDirectoryName2 = "Double";
		expectedSimpleName = "Double";
		expectedUniqueName1 = "Double";
		expectedUniqueName2 = "Double";
		domain1 = schema1.getDomain("Double");
		domain2 = schema2.getDomain("Double");
		otherDomain1 = schema1.getDomain("Integer");
		otherDomain2 = schema2.getDomain("String");
	}

}
