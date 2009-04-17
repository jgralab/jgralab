package de.uni_koblenz.jgralabtest.schematest.domaintest;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.File;

import org.junit.Test;

import de.uni_koblenz.jgralab.schema.Domain;
import de.uni_koblenz.jgralab.schema.Schema;
import de.uni_koblenz.jgralab.schema.impl.DomainImpl;
import de.uni_koblenz.jgralab.schema.impl.SchemaImpl;

public abstract class DomainTest {

	protected String sep = File.separator;

	/**
	 * The first schema for the test.
	 */
	protected Schema schema1;

	/**
	 * The package name for the first schema.
	 */
	protected String schema1Package = "de.uni_koblenz.jgralabtest.schematest.testpackage1";

	/**
	 * The name of the first schema.
	 */
	protected String schema1Name = "Schema1";

	/**
	 * The second schema for the test.
	 */
	protected Schema schema2;

	/**
	 * The package name for the second schema.
	 */
	protected String schema2Package = "de.uni_koblenz.jgralabtest.schematest.testpackage2";

	/**
	 * The name of the second schema.
	 */
	protected String schema2Name = "Schema2";

	/**
	 * A domain to test based on the first schema. Must be of same type as
	 * <code>domain2</code>.
	 */
	protected Domain domain1;

	/**
	 * A domain to test based on the second schema. Must be of same type as
	 * <code>domain1</code>
	 */
	protected Domain domain2;

	/**
	 * A domain different to <code>domain1</code> and <code>domain2</code> based
	 * on the first schema.
	 */
	protected Domain otherDomain1;

	/**
	 * A domain different to <code>domain1</code> and <code>domain2</code> based
	 * on the second schema.
	 */
	protected Domain otherDomain2;

	// fields for expected values
	protected String expectedDomainName;
	protected String expectedJavaAttributeImplementationTypeName;
	protected String expectedJavaClassName;
	protected String expectedPackage1;
	protected String expectedPackage2;
	protected String expectedTgTypeName;
	protected boolean isComposite;
	protected String expectedStringRepresentation;
	protected String expectedDirectoryName1;
	protected String expectedDirectoryName2;
	protected String expectedQualifiedName1;
	protected String expectedQualifiedName2;
	protected String expectedPathName1;
	protected String expectedPathName2;
	protected String expectedSimpleName;
	protected String expectedUniqueName1;
	protected String expectedUniqueName2;

	public void init() {
		schema1 = new SchemaImpl(schema1Name, schema1Package);
		schema2 = new SchemaImpl(schema2Name, schema2Package);
	}

	// interface Domain

	@Test
	public void testEquals() {
		// test if same types of Domains based on different schemas are equal
		// They shouldn't. A domain belongs to one schema.
		assertFalse(domain1.equals(domain2));

		// test if a domain is equal to itself
		assertEquals(domain1, domain1);
		assertEquals(domain2, domain2);

		// test against different domains
		assertFalse(domain1.equals(otherDomain1));
		assertFalse(domain2.equals(otherDomain2));
		assertFalse(domain1.equals(otherDomain2));
		assertFalse(domain2.equals(otherDomain1));

		// test against other types of objects, if equals is implemented
		// properly (must return false in all cases)
		assertEquals(false, domain1.equals(new Object()));
		assertEquals(false, domain2.equals(new Object()));
		assertEquals(false, domain1.equals("AString"));
		assertEquals(false, domain2.equals("AString"));
	}

	@Test
	public void testGetJavaAttributeImplementationTypeName() {
		// tests if the correct javaAttributeImplementationTypeName is returned
		assertEquals(expectedJavaAttributeImplementationTypeName, domain1
				.getJavaAttributeImplementationTypeName(schema1Package));
		assertEquals(expectedJavaAttributeImplementationTypeName, domain1
				.getJavaAttributeImplementationTypeName("Hugo"));
	}

	@Test
	public void testGetJavaClassName() {
		// tests if the correct javaClassName is returned
		assertEquals(expectedJavaClassName, domain1
				.getJavaClassName(schema1Package));
	}

	@Test
	public void testGetPackage() {
		// tests if the correct package is returned
		assertEquals(schema1.getPackage(expectedPackage1), domain1.getPackage());
		assertEquals(schema2.getPackage(expectedPackage2), domain2.getPackage());
	}

	@Test
	public void testTGTypeName() {
		// tests if the correct tgTypeName is returned
		assertEquals(expectedTgTypeName, domain1.getTGTypeName(domain1
				.getPackage()));
	}

	@Test
	public void testIsComposite() {
		// tests if isComposite returns true for CompositeDomains, false
		// otherwise
		assertEquals(isComposite, domain1.isComposite());
		assertEquals(isComposite, domain2.isComposite());
	}

	@Test
	public void testToString() {
		// tests if the correct StringRepresentation is returned
		assertEquals(expectedStringRepresentation, domain1.toString());
	}

	// Test interface NamedElement

	@Test
	public void testGetPackageName() {
		// tests if the correct packageName is returned
		assertEquals(expectedPackage1, domain1.getPackageName());
		assertEquals(expectedPackage2, domain2.getPackageName());
	}

	@Test
	public void testGetQualifiedName() {
		// tests if the correct qualifiedName is returned
		assertEquals(expectedQualifiedName1, domain1.getQualifiedName());
		assertEquals(expectedQualifiedName2, domain2.getQualifiedName());
	}

	@Test
	public void testGetSchema() {
		// tests if the correct schema is returned
		assertTrue(schema1 == domain1.getSchema());
		assertTrue(schema2 == domain2.getSchema());
	}

	@Test
	public void testGetSimpleName() {
		// tests if the correct simpleName is returned
		assertEquals(expectedSimpleName, domain1.getSimpleName());
		assertEquals(expectedSimpleName, domain2.getSimpleName());
	}

	@Test
	public void testGetUniqueName() {
		// tests if the correct uniqueName is returned
		assertEquals(expectedUniqueName1, domain1.getUniqueName());
		assertEquals(expectedUniqueName2, domain2.getUniqueName());
	}

	@Test
	public void testCompareTo() {
		// tests the case of two domains which are equal
		assertEquals(0, ((DomainImpl) domain1).compareTo(domain2));
		// tests the cases of two unequal domains
		assertTrue(0 != ((DomainImpl) domain1).compareTo(otherDomain1));
		assertTrue(0 != ((DomainImpl) domain2).compareTo(otherDomain2));
	}

}
