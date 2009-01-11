package de.uni_koblenz.jgralabtest.schematest.attributedelementtest;

import java.util.Set;
import java.util.SortedSet;
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
	protected AttributedElementClass attributedElement;

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
	 * 
	 * NOTE: This method is called upon in all of this classes´ subclasses.
	 */
	public final void testAddAttribute4(AttributedElementClass superclass) {
		Attribute attribute = new AttributeImpl("testAttribute", schema
				.getDomain(new QualifiedName("Boolean")));

		int attributeCountBefore;

		superclass.addAttribute(attribute);

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
	 * TEST CASE: Adding an attribute, already contained in a subclass of this
	 * element
	 * 
	 * NOTE: This method is called upon in all of this classes´ subclasses.
	 */
	public final void testAddAttribute5(AttributedElementClass subclass) {
		Attribute attribute = new AttributeImpl("testAttribute", schema
				.getDomain(new QualifiedName("Boolean")));

		int attributeCountBefore;

		subclass.addAttribute(attribute);

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
	 * 
	 * NOTE: This method is called upon in all of this classes´ subclasses.
	 */
	public final void testContainsAttribute3(AttributedElementClass superClass) {
		Attribute attribute = new AttributeImpl("testAttribute", schema
				.getDomain(new QualifiedName("Boolean")));

		superClass.addAttribute(attribute);

		Assert.assertTrue(attributedElement.containsAttribute(attribute
				.getName()));
	}

	/*
	 * Tests for the getAllSubClasses() method.
	 */
	/**
	 * getAllSubClasses()
	 * 
	 * TEST CASE: Getting all subclasses of an element with one direct subclass
	 * 
	 * NOTE: This method is called upon in all of this classes´ subclasses.
	 */
	public final void testGetAllSubClasses(
			Vector<AttributedElementClass> expectedSubClasses) {
		Set<AttributedElementClass> subClasses = attributedElement
				.getAllSubClasses();

		Assert.assertEquals(expectedSubClasses.size(), subClasses.size());

		// Check if this element contains all expected subclasses
		for (AttributedElementClass expectedSubClass : expectedSubClasses) {
			boolean expectedSubClassFound = false;
			for (AttributedElementClass subClass : subClasses) {
				if (subClass.getQualifiedName().equals(
						expectedSubClass.getQualifiedName())) {
					expectedSubClassFound = true;
					break;
				}
			}
			Assert.assertTrue(expectedSubClassFound);
		}
	}

	/**
	 * getAllSubClasses()
	 * 
	 * TEST CASE: Getting all subclasses of an element with multiple direct
	 * subclasses
	 * 
	 * NOTE: This method is called upon in all of this classes´ subclasses.
	 */
	public final void testGetAllSubClasses2(
			Vector<AttributedElementClass> expectedSubClasses) {
		Set<AttributedElementClass> subClasses = attributedElement
				.getAllSubClasses();

		Assert.assertEquals(expectedSubClasses.size(), subClasses.size());

		// Check if this element contains all expected subclasses
		for (AttributedElementClass expectedSubClass : expectedSubClasses) {
			boolean expectedSubClassFound = false;
			for (AttributedElementClass subClass : subClasses) {
				if (subClass.getQualifiedName().equals(
						expectedSubClass.getQualifiedName())) {
					expectedSubClassFound = true;
					break;
				}
			}
			Assert.assertTrue(expectedSubClassFound);
		}
	}

	/**
	 * getAllSubClasses()
	 * 
	 * TEST CASE: Getting all subclasses of an element with multiple direct and
	 * indirect subclasses
	 * 
	 * NOTE: This method is called upon in all of this classes´ subclasses.
	 */
	public final void testGetAllSubClasses3(
			Vector<AttributedElementClass> expectedSubClasses) {
		Set<AttributedElementClass> subClasses = attributedElement
				.getAllSubClasses();

		Assert.assertEquals(expectedSubClasses.size(), subClasses.size());

		// Check if this element contains all expected subclasses
		for (AttributedElementClass expectedSubClass : expectedSubClasses) {
			boolean expectedSubClassFound = false;
			for (AttributedElementClass subClass : subClasses) {
				if (subClass.getQualifiedName().equals(
						expectedSubClass.getQualifiedName())) {
					expectedSubClassFound = true;
					break;
				}
			}
			Assert.assertTrue(expectedSubClassFound);
		}
	}

	/**
	 * getAllSubClasses()
	 * 
	 * TEST CASE: Getting all subclasses of an element that has no subclasses
	 * 
	 * NOTE: This method is called upon in all of this classes´ subclasses.
	 */
	public final void testGetAllSubClasses4(
			Vector<AttributedElementClass> expectedSubClasses) {
		Set<AttributedElementClass> subClasses = attributedElement
				.getAllSubClasses();

		Assert.assertEquals(expectedSubClasses.size(), subClasses.size());

		// Check if this element contains all expected subclasses
		for (AttributedElementClass expectedSubClass : expectedSubClasses) {
			boolean expectedSubClassFound = false;
			for (AttributedElementClass subClass : subClasses) {
				if (subClass.getQualifiedName().equals(
						expectedSubClass.getQualifiedName())) {
					expectedSubClassFound = true;
					break;
				}
			}
			Assert.assertTrue(expectedSubClassFound);
		}
	}

	/*
	 * Tests for the getAllSuperClasses() method.
	 */
	/**
	 * getAllSuperClasses()
	 * 
	 * TEST CASE: Getting all superclasses of an element with one direct
	 * superclass
	 * 
	 * NOTE: This method is called upon in all of this classes´ subclasses.
	 */
	public final void testGetAllSuperClasses(
			Vector<AttributedElementClass> expectedSuperClasses) {
		Set<AttributedElementClass> superClasses = attributedElement
				.getAllSuperClasses();

		Assert.assertEquals(expectedSuperClasses.size(), superClasses.size());

		// Check if this element contains all expected superclasses
		for (AttributedElementClass expectedSuperClass : expectedSuperClasses) {
			boolean expectedSuperClassFound = false;
			for (AttributedElementClass superClass : superClasses) {
				if (superClass.getQualifiedName().equals(
						expectedSuperClass.getQualifiedName())) {
					expectedSuperClassFound = true;
					break;
				}
			}
			Assert.assertTrue(expectedSuperClassFound);
		}
	}

	/**
	 * getAllSuperClasses()
	 * 
	 * TEST CASE: Getting all superclasses of an element with multiple direct
	 * superclasses
	 * 
	 * NOTE: This method is called upon in all of this classes´ subclasses.
	 */
	public final void testGetAllSuperClasses2(
			Vector<AttributedElementClass> expectedSuperClasses) {
		Set<AttributedElementClass> superClasses = attributedElement
				.getAllSuperClasses();

		Assert.assertEquals(expectedSuperClasses.size(), superClasses.size());

		// Check if this element contains all expected superclasses
		for (AttributedElementClass expectedSuperClass : expectedSuperClasses) {
			boolean expectedSuperClassFound = false;
			for (AttributedElementClass superClass : superClasses) {
				if (superClass.getQualifiedName().equals(
						expectedSuperClass.getQualifiedName())) {
					expectedSuperClassFound = true;
					break;
				}
			}
			Assert.assertTrue(expectedSuperClassFound);
		}
	}

	/**
	 * getAllSuperClasses()
	 * 
	 * TEST CASE: Getting all superclasses of an element with multiple direct
	 * and indirect superclasses
	 * 
	 * NOTE: This method is called upon in all of this classes´ subclasses.
	 */
	public final void testGetAllSuperClasses3(
			Vector<AttributedElementClass> expectedSuperClasses) {
		Set<AttributedElementClass> superClasses = attributedElement
				.getAllSuperClasses();

		Assert.assertEquals(expectedSuperClasses.size(), superClasses.size());

		// Check if this element contains all expected superclasses
		for (AttributedElementClass expectedSuperClass : expectedSuperClasses) {
			boolean expectedSuperClassFound = false;
			for (AttributedElementClass superClass : superClasses) {
				if (superClass.getQualifiedName().equals(
						expectedSuperClass.getQualifiedName())) {
					expectedSuperClassFound = true;
					break;
				}
			}
			Assert.assertTrue(expectedSuperClassFound);
		}
	}

	/**
	 * getAllSuperClasses()
	 * 
	 * TEST CASE: Getting all superclasses of an element that has no
	 * superclasses
	 * 
	 * NOTE: This method is called upon in all of this classes´ subclasses.
	 */
	public final void testGetAllSuperClasses4(
			Vector<AttributedElementClass> expectedSuperClasses) {
		Set<AttributedElementClass> superClasses = attributedElement
				.getAllSuperClasses();

		Assert.assertEquals(expectedSuperClasses.size(), superClasses.size());

		// Check if this element contains all expected superclasses
		for (AttributedElementClass expectedSuperClass : expectedSuperClasses) {
			boolean expectedSuperClassFound = false;
			for (AttributedElementClass superClass : superClasses) {
				if (superClass.getQualifiedName().equals(
						expectedSuperClass.getQualifiedName())) {
					expectedSuperClassFound = true;
					break;
				}
			}
			Assert.assertTrue(expectedSuperClassFound);
		}
	}

	/*
	 * Tests for the getAttribute(String) method.
	 */
	/**
	 * getAttribute()
	 * 
	 * TEST CASE: Getting a direct attribute
	 */
	@Test
	public final void testGetAttribute() {
		Attribute attribute = new AttributeImpl("testAttribute", schema
				.getDomain(new QualifiedName("Boolean")));

		attributedElement.addAttribute(attribute);

		Attribute attr = attributedElement.getAttribute(attribute.getName());

		Assert.assertSame(attribute, attr);
	}

	/**
	 * getAttribute()
	 * 
	 * TEST CASE: Getting an inherited attribute
	 * 
	 * NOTE: This method is called upon in all of this classes´ subclasses.
	 */
	public final void testGetAttribute2(AttributedElementClass superClass) {
		Attribute attribute = new AttributeImpl("testAttribute", schema
				.getDomain(new QualifiedName("Boolean")));

		// Adding the attribute to the superclass
		superClass.addAttribute(attribute);

		Attribute attr = attributedElement.getAttribute(attribute.getName());

		Assert.assertSame(attribute, attr);
	}

	/**
	 * getAttribute()
	 * 
	 * TEST CASE: Trying to get a non existent attribute
	 */
	@Test
	public final void testGetAttribute3() {
		Assert.assertNull(attributedElement
				.getAttribute("nonexistentAttribute"));
	}

	/**
	 * getAttribute()
	 * 
	 * TEST CASE: Trying to get an attribute with an empty name
	 */
	@Test
	public final void testGetAttribute4() {
		Assert.assertNull(attributedElement.getAttribute(""));
	}

	/**
	 * getAttribute()
	 * 
	 * TEST CASE: Trying to get an attribute present in a subclass of this
	 * element
	 * 
	 * NOTE: This method is called upon in all of this classes´ subclasses.
	 */
	public final void testGetAttribute5(AttributedElementClass subClass) {
		Attribute attribute = new AttributeImpl("testAttribute", schema
				.getDomain(new QualifiedName("Boolean")));

		subClass.addAttribute(attribute);

		Assert.assertNull(attributedElement.getAttribute(attribute.getName()));
	}

	/*
	 * Tests for the getAttributeCount() method.
	 */
	/**
	 * getAttributeCount()
	 * 
	 * TEST CASE: Getting the number of attributes of an element which has only
	 * one direct attribute and no inherited attributes
	 */
	@Test
	public final void testGetAttributeCount() {
		attributedElement.addAttribute(new AttributeImpl("testAttribute",
				schema.getDomain(new QualifiedName("Boolean"))));

		Assert.assertEquals(1, attributedElement.getAttributeCount());
	}

	/**
	 * getAttributeCount()
	 * 
	 * TEST CASE: Getting the number of attributes of an element which has
	 * exactly only one inherited attribute and no direct attributes
	 * 
	 * NOTE: This method is called upon in all of this classes´ subclasses.
	 */
	public final void testGetAttributeCount2(AttributedElementClass superClass) {
		superClass.addAttribute(new AttributeImpl("testAttribute", schema
				.getDomain(new QualifiedName("Boolean"))));

		Assert.assertEquals(1, attributedElement.getAttributeCount());
	}

	/**
	 * getAttributeCount()
	 * 
	 * TEST CASE: Getting the number of attributes of an element which has
	 * multiple direct and indirect attributes
	 * 
	 * NOTE: This method is called upon in all of this classes´ subclasses.
	 */
	public final void testGetAttributeCount3(AttributedElementClass superClass) {
		attributedElement.addAttribute(new AttributeImpl("testAttribute",
				schema.getDomain(new QualifiedName("Boolean"))));

		superClass.addAttribute(new AttributeImpl("testAttribute2", schema
				.getDomain(new QualifiedName("Boolean"))));

		Assert.assertEquals(2, attributedElement.getAttributeCount());
	}

	/**
	 * getAttributeCount()
	 * 
	 * TEST CASE: Getting the number of attributes of an element which has no
	 * direct nor inherited attributes
	 */
	@Test
	public final void testGetAttributeCount4() {
		Assert.assertEquals(0, attributedElement.getAttributeCount());
	}

	/**
	 * getAttributeCount()
	 * 
	 * TEST CASE: Getting the number of attributes of an element which has no
	 * direct nor inherited attributes but whose subclass has attributes
	 * 
	 * NOTE: This method is called upon in all of this classes´ subclasses.
	 */
	public final void testGetAttributeCount5(AttributedElementClass subClass) {
		subClass.addAttribute(new AttributeImpl("testAttribute", schema
				.getDomain(new QualifiedName("Boolean"))));

		Assert.assertEquals(0, attributedElement.getAttributeCount());
	}

	/*
	 * Tests for the getAttributeList() method.
	 */
	/**
	 * getAttributeList()
	 * 
	 * TEST CASE: Getting an element´s list of attributes, which has only one
	 * direct attribute and no inherited attributes
	 */
	@Test
	public final void testGetAttributeList() {
		Attribute attribute = new AttributeImpl("testAttribute", schema
				.getDomain(new QualifiedName("Boolean")));

		attributedElement.addAttribute(attribute);

		SortedSet<Attribute> attrs = attributedElement.getAttributeList();

		Assert.assertEquals(1, attrs.size());
		Assert.assertTrue(attrs.contains(attribute));
	}

	/**
	 * getAttributeList()
	 * 
	 * TEST CASE: Getting an element´s list of attributes, which has exactly one
	 * inherited attribute and no direct attributes
	 * 
	 * NOTE: This method is called upon in all of this classes´ subclasses.
	 */
	public final void testGetAttributeList2(AttributedElementClass superClass) {
		Attribute attribute = new AttributeImpl("testAttribute", schema
				.getDomain(new QualifiedName("Boolean")));

		superClass.addAttribute(attribute);

		SortedSet<Attribute> attrs = attributedElement.getAttributeList();

		Assert.assertEquals(1, attrs.size());
		Assert.assertTrue(attrs.contains(attribute));
	}

	/**
	 * getAttributeList()
	 * 
	 * TEST CASE: Getting an element´s list of attributes, which has mutliple
	 * direct and inherited attributes
	 * 
	 * NOTE: This method is called upon in all of this classes´ subclasses.
	 */
	public final void testGetAttributeList3(AttributedElementClass superClass) {
		Attribute attribute = new AttributeImpl("testAttribute", schema
				.getDomain(new QualifiedName("Boolean")));
		Attribute attribute2 = new AttributeImpl("testAttribute2", schema
				.getDomain(new QualifiedName("Boolean")));

		attributedElement.addAttribute(attribute);
		superClass.addAttribute(attribute2);

		SortedSet<Attribute> attrs = attributedElement.getAttributeList();

		Assert.assertEquals(2, attrs.size());
		Assert.assertTrue(attrs.contains(attribute));
		Assert.assertTrue(attrs.contains(attribute2));
	}

	/**
	 * getAttributeList()
	 * 
	 * TEST CASE: Getting an element´s list of attributes, which has no direct
	 * nor inherited attributes
	 */
	@Test
	public final void testGetAttributeList4() {
		Assert.assertTrue(attributedElement.getAttributeList().isEmpty());
	}

	/**
	 * getAttributeList()
	 * 
	 * TEST CASE: Getting an element´s list of attributes, which has no direct
	 * nor inherited attributes but whose subclass has attributes
	 * 
	 * NOTE: This method is called upon in all of this classes´ subclasses.
	 */
	public final void testGetAttributeList5(AttributedElementClass subClass) {
		Assert.assertTrue(attributedElement.getAttributeList().isEmpty());
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
