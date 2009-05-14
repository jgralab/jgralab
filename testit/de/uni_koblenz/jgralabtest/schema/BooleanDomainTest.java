package de.uni_koblenz.jgralabtest.schema;

import org.junit.Before;

public class BooleanDomainTest extends NativeDomainTest {

	@Before
	public void init() {
		super.init();
		// Initializing DomainTest
		expectedDomainName = "Boolean";
		expectedJavaAttributeImplementationTypeName = "boolean";
		expectedJavaClassName = "Boolean";
		expectedTgTypeName = "Boolean";
		expectedStringRepresentation = "domain Boolean";
		expectedQualifiedName1 = "Boolean";
		expectedQualifiedName2 = "Boolean";
		expectedDirectoryName1 = "Boolean";
		expectedDirectoryName2 = "Boolean";
		expectedSimpleName = "Boolean";
		expectedUniqueName1 = "Boolean";
		expectedUniqueName2 = "Boolean";
		domain1 = schema1.getDomain("Boolean");
		domain2 = schema2.getDomain("Boolean");
		otherDomain1 = schema1.getDomain("Integer");
		otherDomain2 = schema2.getDomain("String");
	}

}
