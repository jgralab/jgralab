package de.uni_koblenz.jgralabtest.schematest.domaintest;

import static org.junit.Assert.*;

import java.io.File;

import org.junit.Before;
import org.junit.Test;

import de.uni_koblenz.jgralab.schema.QualifiedName;

public class QualifiedNameTest {

	private QualifiedName qName1, qName2, qName3, qName4, qName5;
	private String qualifiedName1 = "package1.subpackage1.TestName";
	private String qualifiedName2 = "TestName";
	private String qualifiedName3 = "List<Boolean>";
	private String qualifiedName4 = "Set<package1.Enum1>";
	private String qualifiedName5 = "package1.TestName";
	private String packageName1 = "package1.subpackage1";
	private String packageName2 = "";
	private String packageName3 = "";
	private String packageName4 = "";
	private String packageName5 = "package1";
	private String simpleName1 = "TestName";
	private String simpleName2 = "TestName";
	private String simpleName3 = "List<Boolean>";
	private String simpleName4 = "Set<package1.Enum1>";
	private String simpleName5 = "TestName";
	private String uniqueName1 = "TestName";
	private String uniqueName2 = "TestName";
	private String uniqueName3 = "List<Boolean>";
	private String uniqueName4 = "Set<package1.Enum1>";
	private String uniqueName5 = "TestName";
	private char separator = File.separatorChar;

	@Before
	public void init() {
		qName1 = new QualifiedName("package1.subpackage1.TestName");
		qName2 = new QualifiedName("TestName");
		qName3 = new QualifiedName("List<Boolean>");
		qName4 = new QualifiedName("Set<package1.Enum1>");
		qName5 = new QualifiedName("package1.TestName");
	}

	@Test
	public void testHashCode() {
		// tests if the correct hashCode is returned
		assertEquals(qualifiedName1.hashCode(), qName1.hashCode());
		assertEquals(qualifiedName2.hashCode(), qName2.hashCode());
		assertEquals(qualifiedName3.hashCode(), qName3.hashCode());
		assertEquals(qualifiedName4.hashCode(), qName4.hashCode());
		assertEquals(qualifiedName5.hashCode(), qName5.hashCode());
	}

	@Test
	public void testQualifiedNameString() {
		// tests if the constructor creates the correct qualifiedName,
		// packageName, simpleName and uniqueName from the string of the
		// constructor call
		// test with two packages
		assertEquals(qualifiedName1, qName1.getQualifiedName());
		assertEquals(packageName1, qName1.getPackageName());
		assertEquals(simpleName1, qName1.getSimpleName());
		assertEquals(uniqueName1, qName1.getUniqueName());
		// test with no packages
		assertEquals(qualifiedName2, qName2.getQualifiedName());
		assertEquals(packageName2, qName2.getPackageName());
		assertEquals(simpleName2, qName2.getSimpleName());
		assertEquals(uniqueName2, qName2.getUniqueName());
		// test of a simple collection
		assertEquals(qualifiedName3, qName3.getQualifiedName());
		assertEquals(packageName3, qName3.getPackageName());
		assertEquals(simpleName3, qName3.getSimpleName());
		assertEquals(uniqueName3, qName3.getUniqueName());
		// test of a collection with a baseDomain which is in a package
		assertEquals(qualifiedName4, qName4.getQualifiedName());
		assertEquals(packageName4, qName4.getPackageName());
		assertEquals(simpleName4, qName4.getSimpleName());
		assertEquals(uniqueName4, qName4.getUniqueName());
		// test with one package
		assertEquals(qualifiedName5, qName5.getQualifiedName());
		assertEquals(packageName5, qName5.getPackageName());
		assertEquals(simpleName5, qName5.getSimpleName());
		assertEquals(uniqueName5, qName5.getUniqueName());
	}

	@Test
	public void testQualifiedNameStringString() {
		// test if the constructor works correct with no package
		QualifiedName qName = new QualifiedName("", "Test");
		assertEquals("Test", qName.getQualifiedName());
		assertEquals("", qName.getPackageName());
		assertEquals("Test", qName.getSimpleName());
		assertEquals("Test", qName.getUniqueName());
		// test if the constructor works correct with one package
		qName = new QualifiedName("package1", "Test");
		assertEquals("package1.Test", qName.getQualifiedName());
		assertEquals("package1", qName.getPackageName());
		assertEquals("Test", qName.getSimpleName());
		assertEquals("Test", qName.getUniqueName());
	}

	@Test
	public void testToString() {
		// tests if the correct string representation is returned
		assertEquals(qualifiedName1, qName1.toString());
		assertEquals(qualifiedName2, qName2.toString());
		assertEquals(qualifiedName3, qName3.toString());
		assertEquals(qualifiedName4, qName4.toString());
		assertEquals(qualifiedName5, qName5.toString());
	}

	@Test
	public void testSetPackageName() {
		// Tests if the method works correct on a QualifiedName-object itself
		packageName1 = "package1.s1";
		qualifiedName1 = packageName1 + "." + simpleName1;
		qName1.setPackageName(packageName1);
		assertEquals(packageName1, qName1.getPackageName());
		assertEquals(qualifiedName1, qName1.getQualifiedName());
		packageName2 = "p1";
		qualifiedName2 = packageName2 + "." + simpleName2;
		qName2.setPackageName(packageName2);
		assertEquals(packageName2, qName2.getPackageName());
		assertEquals(qualifiedName3, qName3.getQualifiedName());
		packageName5 = "package2";
		qualifiedName5 = packageName5 + "." + simpleName5;
		qName5.setPackageName(packageName5);
		assertEquals(packageName5, qName5.getPackageName());
		assertEquals(qualifiedName5, qName5.getQualifiedName());
	}

	@Test
	public void testGetSimpleName() {
		// tests if the correct simpleName is returned
		assertEquals(simpleName1, qName1.getSimpleName());
		assertEquals(simpleName2, qName2.getSimpleName());
		assertEquals(simpleName3, qName3.getSimpleName());
		assertEquals(simpleName4, qName4.getSimpleName());
		assertEquals(simpleName5, qName5.getSimpleName());
	}

	@Test
	public void testGetPackageName() {
		// tests if the correct packageName is returned
		assertEquals(packageName1, qName1.getPackageName());
		assertEquals(packageName2, qName2.getPackageName());
		assertEquals(packageName3, qName3.getPackageName());
		assertEquals(packageName4, qName4.getPackageName());
		assertEquals(packageName5, qName5.getPackageName());
	}

	@Test
	public void testGetQualifiedName() {
		// tests if the correct qualifiedName is returned
		assertEquals(qualifiedName1, qName1.getQualifiedName());
		assertEquals(qualifiedName2, qName2.getQualifiedName());
		assertEquals(qualifiedName3, qName3.getQualifiedName());
		assertEquals(qualifiedName4, qName4.getQualifiedName());
		assertEquals(qualifiedName5, qName5.getQualifiedName());
	}

	@Test
	public void testGetDirectoryName() {
		// tests if the correct directoryName is returned
		assertEquals("package1" + separator + "subpackage1" + separator
				+ "TestName", qName1.getDirectoryName());
		assertEquals("TestName", qName2.getDirectoryName());
		assertEquals("List<Boolean>", qName3.getDirectoryName());
		assertEquals("package1" + separator + "TestName", qName5
				.getDirectoryName());
	}

	@Test
	public void testGetName() {
		// tests if the correct name is returned
		assertEquals(qualifiedName1, qName1.getName());
		assertEquals(qualifiedName2, qName2.getName());
		assertEquals(qualifiedName3, qName3.getName());
		assertEquals(qualifiedName4, qName4.getName());
		assertEquals(qualifiedName5, qName5.getName());
	}

	@Test
	public void testGetPathName() {
		// tests if the correct pathName is returned
		assertEquals("package1" + separator + "subpackage1", qName1
				.getPathName());
		assertEquals("", qName2.getPathName());
		assertEquals("", qName3.getPathName());
		assertEquals("", qName4.getPathName());
		assertEquals("package1", qName5.getPathName());
	}

	@Test
	public void testGetUniqueName() {
		// tests if the correct uniqueName is returned
		assertEquals(uniqueName1, qName1.getUniqueName());
		assertEquals(uniqueName2, qName2.getUniqueName());
		assertEquals(uniqueName3, qName3.getUniqueName());
		assertEquals(uniqueName4, qName4.getUniqueName());
		assertEquals(uniqueName5, qName5.getUniqueName());
	}

	@Test
	public void testEqualsObject() {
		// tests if a QualifiedName is equal to itself
		assertTrue(qName1.equals(qName1));
		// tests if a QualifiedName is equal to another with the same name
		assertTrue(qName2.equals(new QualifiedName("TestName")));
		// tests if a QualifiedName is unequal to different one
		assertFalse(qName3.equals(qName4));
	}

	@Test
	public void testCompareTo() {
		// tests if two qualifiedNames are equal
		assertEquals(qualifiedName1.compareTo(qualifiedName1), qName1
				.compareTo(qName1));
		// tests if the first qualifiedName is less then the second one
		assertEquals(qualifiedName3.compareTo(qualifiedName4), qName3
				.compareTo(qName4));
		// tests the comparison of two qualifiedName with different packages
		assertEquals(qualifiedName5.compareTo(qualifiedName1), qName5
				.compareTo(qName1));
	}

	@Test
	public void testIsSimple() {
		assertTrue(qName2.isSimple());
		assertFalse(qName1.isSimple());
	}

	@Test
	public void testIsQualified() {
		assertTrue(qName1.isQualified());
		assertFalse(qName2.isQualified());
	}

	@Test
	public void testToUniqueName() {
		assertEquals("package1_TestName", QualifiedName.toUniqueName(qName5
				.getQualifiedName()));
	}

}
