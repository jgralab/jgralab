package de.uni_koblenz.jgralabtest.schematest.domaintest;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.HashMap;
import java.util.HashSet;

import org.junit.Before;
import org.junit.Test;

import de.uni_koblenz.jgralab.schema.CompositeDomain;
import de.uni_koblenz.jgralab.schema.Domain;
import de.uni_koblenz.jgralab.schema.ListDomain;
import de.uni_koblenz.jgralab.schema.RecordDomain;
import de.uni_koblenz.jgralab.schema.Schema;
import de.uni_koblenz.jgralab.schema.exception.InvalidNameException;
import de.uni_koblenz.jgralab.schema.exception.NoSuchRecordComponentException;
import de.uni_koblenz.jgralab.schema.exception.RecordCycleException;
import de.uni_koblenz.jgralab.schema.exception.WrongSchemaException;
import de.uni_koblenz.jgralab.schema.impl.SchemaImpl;

public class RecordDomainTest extends CompositeDomainTest {

	@Override
	@Before
	public void init() {
		super.init();
		// Initializing of DomainTest
		HashMap<String, Domain> elements = new HashMap<String, Domain>();
		elements.put("int1", schema1.getDomain("Integer"));
		elements.put("double1", schema1.getDomain("Double"));
		elements.put("bool1", schema1.getDomain("Boolean"));
		elements.put("string1", schema1.getDomain("String"));
		schema1.createRecordDomain("package1.Record1", elements);
		domain1 = schema1.getDomain("package1.Record1");
		elements = new HashMap<String, Domain>();
		elements.put("int1", schema2.getDomain("Integer"));
		elements.put("double1", schema2.getDomain("Double"));
		elements.put("bool1", schema2.getDomain("Boolean"));
		elements.put("string1", schema2.getDomain("String"));
		schema2.createRecordDomain("package1.Record1", elements);
		domain2 = schema2.getDomain("package1.Record1");
		otherDomain1 = schema1.getDomain("Boolean");
		otherDomain2 = schema1.getDomain("Integer");
		expectedQualifiedName1 = "package1.Record1";
		expectedQualifiedName2 = "package1.Record1";
		expectedJavaClassName = schema1Package + "." + expectedQualifiedName1;
		expectedPackage1 = "package1";
		expectedPackage2 = "package1";
		expectedSimpleName = "Record1";
		expectedTgTypeName = expectedSimpleName;
		expectedStringRepresentation = "Record package1.Record1 (string1=domain String, double1=domain Double, bool1=domain Boolean, int1=domain Integer)";
		expectedPathName1 = "package1";
		expectedPathName2 = "package1";
		expectedSimpleName = "Record1";
		expectedDirectoryName1 = "package1" + sep + "Record1";
		expectedDirectoryName2 = "package1" + sep + "Record1";
		expectedUniqueName1 = "Record1";
		expectedUniqueName2 = "Record1";
		// Initializing of CompositeDomainTest
		domain3 = (CompositeDomain) domain1;
		schema1.createListDomain(schema1.getDomain("Boolean"));
		HashMap<String, Domain> element = new HashMap<String, Domain>();
		element.put("aList", schema1.getDomain("List<Boolean>"));
		element.put("aRecord", domain1);
		schema1.createRecordDomain("Record4", element);
		domain4 = (CompositeDomain) schema1.getDomain("Record4");
		expectedCompositeDomains1 = new HashSet<CompositeDomain>();
		expectedCompositeDomains2 = new HashSet<CompositeDomain>();
		expectedCompositeDomains3 = expectedCompositeDomains1;
		expectedCompositeDomains4 = new HashSet<CompositeDomain>();
		expectedCompositeDomains4.add((ListDomain) schema1
				.getDomain("List<Boolean>"));
		expectedCompositeDomains4.add((RecordDomain) domain1);
		expectedDomains1 = new HashSet<Domain>();
		for (Domain d : elements.values()) {
			expectedDomains1.add(d);
		}
		expectedDomains2 = expectedDomains1;
		expectedDomains3 = expectedDomains1;
		expectedDomains4 = new HashSet<Domain>();
		expectedDomains4.add(schema1.getDomain("List<Boolean>"));
		expectedDomains4.add(domain1);
	}

	@Test
	public void testAddComponent() {
		// testAddComponentWithEmptyNameRejected
		// testOfSelfInclusion
		// testOfCyclicInclusion
		// testOfIncludingNewDomains
		// test of normal cases
		schema1.createRecordDomain("package1.Rec1");
		RecordDomain rec1 = (RecordDomain) schema1.getDomain("package1.Rec1");
		rec1.addComponent("component1", schema1.getDomain("Boolean"));
		assertEquals(1, rec1.getComponents().size());
		assertTrue(rec1.getComponents().containsKey("component1"));
		assertTrue(rec1.getComponents().containsValue(
				schema1.getDomain("Boolean")));
		rec1.addComponent("component2", schema1.getDomain("Integer"));
		assertEquals(2, rec1.getComponents().size());
		assertTrue(rec1.getComponents().containsKey("component1"));
		assertTrue(rec1.getComponents().containsValue(
				schema1.getDomain("Boolean")));
		assertTrue(rec1.getComponents().containsKey("component2"));
		assertTrue(rec1.getComponents().containsValue(
				schema1.getDomain("Integer")));
	}

	@Test(expected = InvalidNameException.class)
	public void testAddComponentWithEmptyNameRejected() {
		// tests if an empty component name is rejected
		Schema schema1 = new SchemaImpl("Schema1", "pkgPrefix1");
		schema1.createRecordDomain("package1.Record1");
		RecordDomain record1 = (RecordDomain) schema1
				.getDomain("package1.Record1");
		record1.addComponent("", schema1.getDomain("Boolean"));
	}

	@Test(expected = RecordCycleException.class)
	public void testOfSelfInclusion() {
		// tests if an exception occurs during creating a RecordDomain which
		// includes itself
		Schema schema1 = new SchemaImpl("Schema1", "pkgPrefix1");
		schema1.createRecordDomain("package1.Record1");
		RecordDomain record1 = (RecordDomain) schema1
				.getDomain("package1.Record1");
		record1.addComponent("this", record1);
	}

	@Test(expected = RecordCycleException.class)
	public void testOfCyclicInclusion() {
		// tests if an exception occurs during creating two RecordDomains which
		// include each other
		Schema schema1 = new SchemaImpl("Schema1", "pkgPrefix1");
		RecordDomain record2 = schema1.createRecordDomain("package1.Record2");
		RecordDomain record3 = schema1.createRecordDomain("package1.Record3");
		record2.addComponent("theOther", record3);
		record3.addComponent("theOther", record2);
	}

	@Test(expected = RecordCycleException.class)
	public void testOfCyclicInclusion2() {
		// tests if an exception occurs during creating two RecordDomains which
		// include each other
		Schema schema1 = new SchemaImpl("Schema1", "pkgPrefix1");
		RecordDomain record2 = schema1.createRecordDomain("package1.Record2");
		RecordDomain record3 = schema1.createRecordDomain("package1.Record3");
		RecordDomain record4 = schema1.createRecordDomain("package1.Record4");
		record2.addComponent("theOther", record3);
		record3.addComponent("theOther", record4);
		record4.addComponent("theOther", record2);
	}

	@Test(expected = RecordCycleException.class)
	public void testOfCyclicInclusion3() {
		Schema s = new SchemaImpl("MySchema", "pkgPrefix1");
		RecordDomain r1 = s.createRecordDomain("test.R1");
		RecordDomain r2 = s.createRecordDomain("test.R2");

		r1.addComponent("myList", s.createListDomain(s.createMapDomain(s
				.getDomain("String"), r2)));

		// now r1 <>-- r2 holds

		HashMap<String, Domain> r3CompMap = new HashMap<String, Domain>();
		r3CompMap.put("myR2", r2);
		RecordDomain r3 = s.createRecordDomain("test.R3", r3CompMap);

		// now r1 <>-- r2 --<> r3 holds

		// try to make r2 include r3
		r2.addComponent("mySet", s.createSetDomain(s.createSetDomain(r3)));
	}

	@Test
	public void testOfNoCyclicInclusion() {
		// record2 contains record3
		// record2 contains record4 contains record3
		Schema schema1 = new SchemaImpl("Schema1", "pkgPrefix1");
		RecordDomain record2 = schema1.createRecordDomain("package1.Record2");
		RecordDomain record3 = schema1.createRecordDomain("package1.Record3");
		RecordDomain record4 = schema1.createRecordDomain("package1.Record4");
		record2.addComponent("theR3", record3);
		record2.addComponent("theR4", record4);
		record4.addComponent("theR3", record3);
	}

	@Test(expected = WrongSchemaException.class)
	public void testOfIncludingDomainsFromOtherSchema() {
		// test if the creation of an component with a domain from another
		// schema is rejected
		Schema schema1 = new SchemaImpl("Schema1", "pkgPrefix1");
		schema1.createRecordDomain("package1.Record1");
		RecordDomain record1 = (RecordDomain) schema1
				.getDomain("package1.Record1");
		schema2.createEnumDomain("Enum1");
		record1.addComponent("newDomain", schema2.getDomain("Enum1"));
	}

	@Override
	@Test
	public void testGetJavaAttributeImplementationTypeName() {
		// tests if the correct javaAttributeImplementationTypeName is returned
		assertEquals(schema1Package + ".package1.Record1", domain1
				.getJavaAttributeImplementationTypeName(schema1Package));
	}

	@Test
	public void testGetUniqueNameOfElementsWithSameSimpleName() {
		// Test if uniqueName is changed if two elements have the same
		// simpleName
		schema1.createRecordDomain("package1.rec1");
		schema1.createRecordDomain("package2.rec1");
		Domain domain1 = schema1.getDomain("package1.rec1");
		Domain domain2 = schema1.getDomain("package2.rec1");
		assertEquals("package1_rec1", domain1.getUniqueName());
		assertEquals("package2_rec1", domain2.getUniqueName());
		// Test if uniqueName of a third element with the same simpleName is
		// changed
		schema1.createRecordDomain("package3.rec1");
		Domain domain3 = schema1.getDomain("package3.rec1");
		assertEquals("package3_rec1", domain3.getUniqueName());
	}

	@Test
	public void testGetComponents() {
		// tests if a map with all components is returned
		schema1.createRecordDomain("package1.Rec1");
		RecordDomain rec1 = (RecordDomain) schema1.getDomain("package1.Rec1");
		assertEquals(new HashMap<String, Domain>(), rec1.getComponents());
		HashMap<String, Domain> elements = new HashMap<String, Domain>();
		elements.put("int1", schema1.getDomain("Integer"));
		elements.put("double1", schema1.getDomain("Double"));
		elements.put("bool1", schema1.getDomain("Boolean"));
		elements.put("string1", schema1.getDomain("String"));
		assertEquals(elements, ((RecordDomain) domain1).getComponents());
	}

	@Test
	public void testGetDomainOfComponent() {
		// tests if the correct domain is returned
		RecordDomain rec1 = (RecordDomain) domain1;
		assertEquals(schema1.getDomain("Boolean"), rec1
				.getDomainOfComponent("bool1"));
		rec1 = (RecordDomain) domain4;
		assertEquals(domain1, rec1.getDomainOfComponent("aRecord"));
		assertEquals(schema1.getDomain("List<Boolean>"), rec1
				.getDomainOfComponent("aList"));
	}

	@Test(expected = NoSuchRecordComponentException.class)
	public void testGetDomainOfComponentWithNotExistingComponent() {
		// tests if getting the domain of a not existing component fails
		RecordDomain rec1 = (RecordDomain) domain1;
		rec1.getDomainOfComponent("nonsense");
	}

	@Override
	@Test
	public void testToString() {
		// tests if the correct string representation is returned
		assertEquals(
				"Record package1.Record1 (string1=domain String, double1=domain Double, bool1=domain Boolean, int1=domain Integer)",
				domain1.toString());
		assertEquals(
				"Record package1.Record1 (string1=domain String, double1=domain Double, bool1=domain Boolean, int1=domain Integer)",
				domain2.toString());
		assertEquals(
				"Record package1.Record1 (string1=domain String, double1=domain Double, bool1=domain Boolean, int1=domain Integer)",
				domain3.toString());
		assertEquals(
				"Record Record4 (aList=domain List<domain Boolean>, aRecord=Record package1.Record1 (string1=domain String, double1=domain Double, bool1=domain Boolean, int1=domain Integer))",
				domain4.toString());
	}

}