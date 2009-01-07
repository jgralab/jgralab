package de.uni_koblenz.jgralabtest.schematest.attributedelementtest;

import java.util.Set;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import de.uni_koblenz.jgralab.Attribute;
import de.uni_koblenz.jgralab.schema.AttributedElementClass;
import de.uni_koblenz.jgralab.schema.QualifiedName;
import de.uni_koblenz.jgralab.schema.exception.DuplicateAttributeException;
import de.uni_koblenz.jgralab.schema.impl.AttributeImpl;

public abstract class GraphElementClassImplTest extends
		AttributedElementClassImplTest {

	@Before
	@Override
	public void setUp() {
		super.setUp();
	}

	/**
	 * addAttribute(Attribute)
	 *
	 * TEST CASE: Adding an attribute, already contained in a superclass of this
	 * element
	 *
	 * NOTE: This method is overridden in all of this classes´ subclasses.
	 */
	@Override
	public void testAddAttribute4() {
		Attribute attribute = new AttributeImpl("testAttribute", schema
				.getDomain(new QualifiedName("Boolean")));

		int attributeCountBefore;

		// Adding the attribute to the superclass
		attributedElement2.addAttribute(attribute);

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
	 * NOTE: This method is overridden in all of this classes´ subclasses.
	 */
	@Override
	public void testAddAttribute5() {
		Attribute attribute = new AttributeImpl("testAttribute", schema
				.getDomain(new QualifiedName("Boolean")));

		int attributeCountBefore;

		// Adding the attribute to the subclass
		attributedElement2.addAttribute(attribute);

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
	 * containsAttribute(String)
	 *
	 * TEST CASE: looking for an attribute, present in a superclass of this
	 * element
	 *
	 * NOTE: This method is overridden in all of this classes´ subclasses.
	 */
	@Test
	@Override
	public void testContainsAttribute3() {
		Attribute attribute = new AttributeImpl("testAttribute", schema
				.getDomain(new QualifiedName("Boolean")));

		// Adding the attribute to the superclass
		attributedElement2.addAttribute(attribute);

		// Perform operation to test
		Assert.assertTrue(attributedElement.containsAttribute(attribute
				.getName()));
	}

	/**
	 * getAllSubClasses()
	 *
	 * SPECIAL NOTE: This method is required due to the nature of JGraLab. The
	 * test prerequisites are set in all of this classes´ subclasses.
	 */
	public final void testGetAllSubClasses_main() {
		Set<AttributedElementClass> subClasses = attributedElement
				.getAllSubClasses();

		Assert.assertEquals(expectedValue, subClasses.size());
		// Check if this element contains all expected subclasses
		for (String expectedSubClassName : expectedStrings) {
			boolean expectedSubClassFound = false;
			for (AttributedElementClass subClass : subClasses) {
				if (subClass.getSimpleName().equals(expectedSubClassName)) {
					expectedSubClassFound = true;
					break;
				}
			}
			Assert.assertTrue(expectedSubClassFound);
		}
	}

	/**
	 * getAllSuperClasses()
	 *
	 * SPECIAL NOTE: This method is required due to the nature of JGraLab. The
	 * test prerequisites are set in all of this classes´ subclasses.
	 */
	public final void testGetAllSuperClasses_main() {
		Set<AttributedElementClass> superClasses = attributedElement
				.getAllSuperClasses();

		Assert.assertEquals(expectedValue, superClasses.size());
		// Check if this element contains all expected superclasses
		for (String expectedSuperClassName : expectedStrings) {
			boolean expectedSuperClassFound = false;
			for (AttributedElementClass superClass : superClasses) {
				if (superClass.getSimpleName().equals(expectedSuperClassName)) {
					expectedSuperClassFound = true;
					break;
				}
			}
			Assert.assertTrue(expectedSuperClassFound);
		}
	}

	@Test
	public void testGetGraphClass() {
		// TODO Auto-generated method stub
	}

	@Test
	public void testGetSchema() {
		// TODO Auto-generated method stub
	}

	@Override
	@Test
	public void testToString() {
		// TODO Auto-generated method stub
	}
}
