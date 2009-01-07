package de.uni_koblenz.jgralabtest.schematest.attributedelementtest;

import java.util.Vector;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import de.uni_koblenz.jgralab.Attribute;
import de.uni_koblenz.jgralab.schema.AttributedElementClass;
import de.uni_koblenz.jgralab.schema.GraphClass;
import de.uni_koblenz.jgralab.schema.QualifiedName;
import de.uni_koblenz.jgralab.schema.Schema;
import de.uni_koblenz.jgralab.schema.exception.DuplicateAttributeException;
import de.uni_koblenz.jgralab.schema.exception.ReservedWordException;
import de.uni_koblenz.jgralab.schema.impl.AttributeImpl;
import de.uni_koblenz.jgralab.schema.impl.SchemaImpl;

public abstract class AttributedElementClassImplTest {

	protected Schema schema;
	protected GraphClass graphClass;
	protected AttributedElementClass attributedElement, attributedElement2;

	/*
	 * The following variables are sed in a variety of tests to determine if the
	 * result of "whatever" meets the expectations.
	 */
	protected int expectedValue = -1;
	protected Vector<String> expectedStrings = new Vector<String>();

	@Before
	public void setUp() {
		schema = new SchemaImpl(new QualifiedName(
				"de.uni_koblenz.jgralabtest.schematest.TestSchema"));
		graphClass = schema.createGraphClass(new QualifiedName("GraphClass1"));
	}

	/*
	 * Tests for the addAttribute(Attribute) and addAttribute(QualifiedName,
	 * Domain) methods.
	 *
	 * NOTE: As addAttribute(QualifiedName, Domain) only creates an attribute
	 * with the given parameters then calling the addAttribute(Attribute) method
	 * and in accordance with the specification of addAttribute(QualifiedName,
	 * Schema), the only error-source not covered by the addAttribute(Attribute)
	 * specification, is an attribute´s name containing reserved TG/Java words.
	 * In consequence there will only be tests for addAttribute(QualifiedName,
	 * Domain) addressing these special error-sources, as the rest is already
	 * covered by the tests for addAttribute(anAttribute).
	 */
	/**
	 * addAttribute(Attribute)
	 *
	 * TEST CASE: Adding an attribute, which is not yet present in this element,
	 * nor in this element´s direct and indirect super-/subclasses
	 */
	@Test
	public final void testAddAttribute() {
		Attribute attribute = new AttributeImpl("testAttribute", schema
				.getDomain(new QualifiedName("Boolean")));

		int attributeCountBefore = attributedElement.getAttributeCount();

		attributedElement.addAttribute(attribute);

		Assert.assertEquals(attributeCountBefore + 1, attributedElement
				.getAttributeCount());
		Assert.assertTrue(attributedElement.containsAttribute(attribute
				.getName()));
	}

	/**
	 * addAttribute(Attribute)
	 *
	 * TEST CASE: Adding two distinct attributes which are not yet present in
	 * this element, nor in this element´s direct and indirect superclasses (and
	 * subclasses)
	 */
	@Test
	public final void testAddAttribute2() {
		Attribute attribute = new AttributeImpl("testAttribute", schema
				.getDomain(new QualifiedName("Boolean")));
		Attribute attribute2 = new AttributeImpl("testAttribute2", schema
				.getDomain(new QualifiedName("Boolean")));

		int attributeCountBefore = attributedElement.getAttributeCount();

		attributedElement.addAttribute(attribute);
		attributedElement.addAttribute(attribute2);

		Assert.assertEquals(attributeCountBefore + 2, attributedElement
				.getAttributeCount());
		Assert.assertTrue(attributedElement.containsAttribute(attribute
				.getName()));
		Assert.assertTrue(attributedElement.containsAttribute(attribute2
				.getName()));
	}

	/**
	 * addAttribute(Attribute)
	 *
	 * TEST CASE: Adding an attribute, already contained directly in this
	 * element.
	 */
	@Test
	public final void testAddAttribute3() {
		Attribute attribute = new AttributeImpl("testAttribute", schema
				.getDomain(new QualifiedName("Boolean")));
		int attributeCountBefore;

		attributedElement.addAttribute(attribute);
		attributeCountBefore = attributedElement.getAttributeCount();

		try {
			attributedElement.addAttribute(attribute);
			Assert.fail("DuplicateAttributeException expected!");
		} catch (DuplicateAttributeException e) {
		}

		// The number of attributes does not change
		Assert.assertEquals(attributeCountBefore, attributedElement
				.getAttributeCount());
	}

	/**
	 * addAttribute(Attribute)
	 *
	 * TEST CASE: Adding an attribute, already contained in a superclass of this
	 * element
	 */
	public abstract void testAddAttribute4();

	/**
	 * addAttribute(Attribute)
	 *
	 * TEST CASE: Adding an attribute, already contained in a subclass of this
	 * element
	 */
	public abstract void testAddAttribute5();

	/**
	 * addAttribute(QualifiedName, Domain)
	 *
	 * TEST CASE: Adding an attribute with a name containing a reserved TG word.
	 */
	@Test
	public final void testAddAttribute6() {
		String invalidAttributeName = "aggregate";

		int attributeCountBefore = attributedElement.getAttributeCount();

		try {
			attributedElement.addAttribute(invalidAttributeName, schema
					.getDomain(new QualifiedName("Boolean")));
			Assert.fail("ReservedWordException expected!");
		} catch (ReservedWordException e) {
		}

		// The number of attributes does not change
		Assert.assertEquals(attributeCountBefore, attributedElement
				.getAttributeCount());
		Assert.assertFalse(attributedElement
				.containsAttribute(invalidAttributeName));
	}

	/**
	 * addAttribute(QualifiedName, Domain)
	 *
	 * TEST CASE: Adding an attribute with a name containing a reserved Java
	 * word
	 */
	@Test
	public final void testAddAttribute7() {
		String invalidAttributeName = "aggregate";

		int attributeCountBefore = attributedElement.getAttributeCount();

		try {
			attributedElement.addAttribute(invalidAttributeName, schema
					.getDomain(new QualifiedName("Boolean")));
			Assert.fail("ReservedWordException expected!");
		} catch (ReservedWordException e) {
		}

		// The number of attributes does not change
		Assert.assertEquals(attributeCountBefore, attributedElement
				.getAttributeCount());
		Assert.assertFalse(attributedElement
				.containsAttribute(invalidAttributeName));
	}

	/*
	 * Tests for the containsAttribute(String) method.
	 */
	/**
	 * containsAttribute(String)
	 *
	 * TEST CASE: looking for a non-present attribute
	 */
	@Test
	public final void testContainsAttribute() {
		Assert.assertFalse(attributedElement
				.containsAttribute("nonPresentAttribute"));
	}

	/**
	 * containsAttribute(String)
	 *
	 * TEST CASE: looking for an attribute, directly present in this element
	 */
	@Test
	public final void testContainsAttribute2() {
		Attribute attribute = new AttributeImpl("testAttribute", schema
				.getDomain(new QualifiedName("Boolean")));

		attributedElement.addAttribute(attribute);

		Assert.assertTrue(attributedElement.containsAttribute(attribute
				.getName()));
	}

	/**
	 * containsAttribute(String)
	 *
	 * TEST CASE: looking for an attribute, present in a superclass of this
	 * element
	 */
	@Test
	public abstract void testContainsAttribute3();

	/*
	 * Tests for the getAllSubClasses() method.
	 */
	/**
	 * getAllSubClasses()
	 *
	 * TEST CASE: Getting all subclasses of an element with one direct subclass
	 */
	public abstract void testGetAllSubClasses();

	/**
	 * getAllSubClasses()
	 *
	 * TEST CASE: Getting all subclasses of an element with multiple direct
	 * subclasses
	 */
	public abstract void testGetAllSubClasses2();

	/**
	 * getAllSubClasses()
	 *
	 * TEST CASE: Getting all subclasses of an element with multiple direct and
	 * indirect subclasses
	 */
	public abstract void testGetAllSubClasses3();

	/**
	 * getAllSubClasses()
	 *
	 * TEST CASE: Getting all subclasses of an element that has no subclasses
	 */
	public abstract void testGetAllSubClasses4();

	/*
	 * Tests for the getAllSuperClasses() method.
	 */
	/**
	 * getAllSuperClasses()
	 *
	 * TEST CASE: Getting all superclasses of an element with one direct
	 * superclass
	 */
	public abstract void testGetAllSuperClasses();

	/**
	 * getAllSuperClasses()
	 *
	 * TEST CASE: Getting all superclasses of an element with multiple direct
	 * superclasses
	 */
	public abstract void testGetAllSuperClasses2();

	/**
	 * getAllSuperClasses()
	 *
	 * TEST CASE: Getting all superclasses of an element with multiple direct
	 * and indirect superclasses
	 */
	public abstract void testGetAllSuperClasses3();

	/**
	 * getAllSuperClasses()
	 *
	 * TEST CASE: Getting all superclasses of an element that has no
	 * superclasses
	 */
	public abstract void testGetAllSuperClasses4();

	@Test
	public void testGetAttribute() {
		// TODO Auto-generated method stub
	}

	@Test
	public void testGetAttributeCount() {
		// TODO Auto-generated method stub
	}

	@Test
	public void testGetAttributeList() {
		// TODO Auto-generated method stub
	}

	@Test
	public void testGetDirectoryName() {
		// TODO Auto-generated method stub
	}

	@Test
	public void testGetDirectSubClasses() {
		// TODO Auto-generated method stub
	}

	@Test
	public void testGetDirectSuperClasses() {
		// TODO Auto-generated method stub
	}

	@Test
	public void testGetLeastCommonSuperclass() {
		// TODO Auto-generated method stub
	}

	@Test
	public void testGetM1Class() {
		// TODO Auto-generated method stub
	}

	@Test
	public void testGetM1ImplementationClass() {
		// TODO Auto-generated method stub
	}

	@Test
	public void testGetName() {
		// TODO Auto-generated method stub
	}

	@Test
	public void testGetOwnAttribute() {
		// TODO Auto-generated method stub
	}

	@Test
	public void testGetOwnAttributeCount() {
		// TODO Auto-generated method stub
	}

	@Test
	public void testGetOwnAttributeList() {
		// TODO Auto-generated method stub
	}

	@Test
	public void testGetPackage() {
		// TODO Auto-generated method stub
	}

	@Test
	public void testGetPackageName() {
		// TODO Auto-generated method stub
	}

	@Test
	public void testGetPathName() {
		// TODO Auto-generated method stub
	}

	@Test
	public void testGetQName() {
		// TODO Auto-generated method stub
	}

	@Test
	public void testGetQualifiedName() {
		// TODO Auto-generated method stub
	}

	@Test
	public void testGetSimpleName() {
		// TODO Auto-generated method stub
	}

	@Test
	public void testGetUniqueName() {
		// TODO Auto-generated method stub
	}

	@Test
	public void testHasAttributes() {
		// TODO Auto-generated method stub
	}

	@Test
	public void testHasOwnAttributes() {
		// TODO Auto-generated method stub
	}

	@Test
	public void testIsAbstract() {
		// TODO Auto-generated method stub
	}

	@Test
	public void testIsDirectSubClassOf() {
		// TODO Auto-generated method stub
	}

	@Test
	public void testIsDirectSuperClassOf() {
		// TODO Auto-generated method stub
	}

	@Test
	public void testIsInternal() {
		// TODO Auto-generated method stub
	}

	@Test
	public void testIsSubClassOf() {
		// TODO Auto-generated method stub
	}

	@Test
	public void testIsSuperClassOf() {
		// TODO Auto-generated method stub
	}

	@Test
	public void testIsSuperClassOfOrEquals() {
		// TODO Auto-generated method stub
	}

	@Test
	public void testSetAbstract() {
		// TODO Auto-generated method stub
	}

	@Test
	public void testSetInternal() {
		// TODO Auto-generated method stub
	}

	@Test
	public void testSetPackage() {
		// TODO Auto-generated method stub
	}

	@Test
	public void testSetUniqueName() {
		// TODO Auto-generated method stub
	}

	@Test
	public void testSubclassContainsAttribute() {
		// TODO Auto-generated method stub
	}

	public abstract void testToString();

}
