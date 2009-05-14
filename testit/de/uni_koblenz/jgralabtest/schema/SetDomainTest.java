package de.uni_koblenz.jgralabtest.schema;

import java.util.HashSet;

import org.junit.Before;

import de.uni_koblenz.jgralab.schema.CompositeDomain;
import de.uni_koblenz.jgralab.schema.Domain;

public class SetDomainTest extends CollectionDomainTest {

	@Before
	public void init() {
		super.init();
		// Initializing of DomainTest
		expectedDomainName = "Set<Integer>";
		expectedJavaAttributeImplementationTypeName = "java.util.Set<Integer>";
		expectedJavaClassName = "java.util.Set<Integer>";
		expectedTgTypeName = "Set<Integer>";
		expectedStringRepresentation = "domain Set<domain Integer>";
		expectedQualifiedName1 = "Set<Integer>";
		expectedQualifiedName2 = "Set<Integer>";
		expectedDirectoryName1 = "Set<Integer>";
		expectedDirectoryName2 = "Set<Integer>";
		expectedSimpleName = "Set<Integer>";
		expectedUniqueName1 = "Set<Integer>";
		expectedUniqueName2 = "Set<Integer>";
		domain1 = schema1.createSetDomain(schema1.getDomain("Integer"));
		domain2 = schema2.createSetDomain(schema2.getDomain("Integer"));
		otherDomain1 = schema1.getDomain("Integer");
		otherDomain2 = schema2.getDomain("String");
		// Initializing of CollectionDomainTest
		expectedBaseDomain = schema1.getDomain("Integer");
		// Initializing of CompositeDomainTest
		domain3 = schema1.createSetDomain(schema1.getDomain("String"));
		domain4 = schema1.createSetDomain(domain3);
		expectedCompositeDomains1 = new HashSet<CompositeDomain>();
		expectedCompositeDomains2 = new HashSet<CompositeDomain>();
		expectedCompositeDomains3 = new HashSet<CompositeDomain>();
		expectedCompositeDomains4 = new HashSet<CompositeDomain>();
		expectedCompositeDomains4.add(domain3);
		expectedDomains1 = new HashSet<Domain>();
		expectedDomains1.add(schema1.getDomain("Integer"));
		expectedDomains2 = new HashSet<Domain>();
		expectedDomains2.add(schema2.getDomain("Integer"));
		expectedDomains3 = new HashSet<Domain>();
		expectedDomains3.add(schema1.getDomain("String"));
		expectedDomains4 = new HashSet<Domain>();
		expectedDomains4.add(domain3);
	}

}
