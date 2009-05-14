package de.uni_koblenz.jgralabtest.schema.domain;

import java.util.HashSet;

import org.junit.Before;

import de.uni_koblenz.jgralab.schema.CompositeDomain;
import de.uni_koblenz.jgralab.schema.Domain;

public class ListDomainTest extends CollectionDomainTest {

	@Before
	public void init() {
		super.init();
		// Initializing of DomainTest
		expectedDomainName = "List<Integer>";
		expectedJavaAttributeImplementationTypeName = "java.util.List<Integer>";
		expectedJavaClassName = "java.util.List<Integer>";
		expectedTgTypeName = "List<Integer>";
		expectedStringRepresentation = "domain List<domain Integer>";
		expectedQualifiedName1 = "List<Integer>";
		expectedQualifiedName2 = "List<Integer>";
		expectedDirectoryName1 = "List<Integer>";
		expectedDirectoryName2 = "List<Integer>";
		expectedSimpleName = "List<Integer>";
		expectedUniqueName1 = "List<Integer>";
		expectedUniqueName2 = "List<Integer>";
		domain1 = schema1.createListDomain(schema1.getDomain("Integer"));
		domain2 = schema2.createListDomain(schema2.getDomain("Integer"));
		otherDomain1 = schema1.getDomain("Integer");
		otherDomain2 = schema2.getDomain("String");
		// Initializing of CollectionDomainTest
		expectedBaseDomain = schema1.getDomain("Integer");
		// Initializing of CompositeDomainTest
		domain3 = schema1.createListDomain(schema1.getDomain("String"));
		domain4 = schema1.createListDomain(domain3);
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
