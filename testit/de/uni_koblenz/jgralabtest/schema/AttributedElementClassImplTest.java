/*
 * JGraLab - The Java Graph Laboratory
 *
 * Copyright (C) 2006-2013 Institute for Software Technology
 *                         University of Koblenz-Landau, Germany
 *                         ist@uni-koblenz.de
 *
 * For bug reports, documentation and further information, visit
 *
 *                         https://github.com/jgralab/jgralab
 *
 * This program is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as published by the
 * Free Software Foundation; either version 3 of the License, or (at your
 * option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General
 * Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, see <http://www.gnu.org/licenses>.
 *
 * Additional permission under GNU GPL version 3 section 7
 *
 * If you modify this Program, or any covered work, by linking or combining
 * it with Eclipse (or a modified version of that program or an Eclipse
 * plugin), containing parts covered by the terms of the Eclipse Public
 * License (EPL), the licensors of this Program grant you additional
 * permission to convey the resulting work.  Corresponding Source for a
 * non-source form of such a combination shall include the source code for
 * the parts of JGraLab used as well as that of the covered work.
 */
package de.uni_koblenz.jgralabtest.schema;

import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import de.uni_koblenz.jgralab.schema.Attribute;
import de.uni_koblenz.jgralab.schema.AttributedElementClass;
import de.uni_koblenz.jgralab.schema.Constraint;
import de.uni_koblenz.jgralab.schema.GraphClass;
import de.uni_koblenz.jgralab.schema.Schema;
import de.uni_koblenz.jgralab.schema.exception.SchemaException;
import de.uni_koblenz.jgralab.schema.impl.ConstraintImpl;
import de.uni_koblenz.jgralab.schema.impl.SchemaImpl;

public abstract class AttributedElementClassImplTest<AEC extends AttributedElementClass<?, ?>> {

	protected Schema schema;
	protected GraphClass graphClass;
	protected AEC attributedElement;

	@Before
	public void setUp() {
		schema = new SchemaImpl("TestSchema",
				"de.uni_koblenz.jgralabtest.schematest");
		graphClass = schema.createGraphClass("GraphClass1");
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

	/*
	 * Tests for the addConstraint(String) method.
	 */

	/**
	 * addConstraint(Constraint)
	 *
	 * TEST CASE: Adding a constraint, which is not yet present in this element,
	 * nor in this element´s direct and indirect super-/subclasses
	 */
	@Test
	public final void testAddConstraint() {
		Constraint constr = new ConstraintImpl("", "SomeConstraint", "");

		int constraintCountBefore = attributedElement.getConstraints().size();

		attributedElement.addConstraint(constr);

		Assert.assertEquals(constraintCountBefore + 1, attributedElement
				.getConstraints().size());
		Assert.assertTrue(attributedElement.getConstraints().contains(constr));
	}

	/**
	 * addConstraint(Constraint)
	 *
	 * TEST CASE: Adding two distinct constraints which are not yet present in
	 * this element, nor in this element´s direct and indirect super-/subclasses
	 */
	@Test
	public final void testAddConstraint2() {
		Constraint constr = new ConstraintImpl("", "SomeConstraint", "");
		Constraint constr2 = new ConstraintImpl("", "SomeOtherConstraint", "");

		int constraintCountBefore = attributedElement.getConstraints().size();

		attributedElement.addConstraint(constr);
		attributedElement.addConstraint(constr2);

		Assert.assertEquals(constraintCountBefore + 2, attributedElement
				.getConstraints().size());
		Assert.assertTrue(attributedElement.getConstraints().contains(constr));
		Assert.assertTrue(attributedElement.getConstraints().contains(constr2));
	}

	/**
	 * addConstraint(Constraint)
	 *
	 * TEST CASE: Adding a constraint, already contained directly in this
	 * element
	 */
	@Test
	public final void testAddConstraint3() {
		Constraint constr = new ConstraintImpl("", "SomeConstraint", "");

		attributedElement.addConstraint(constr);

		int constraintCountBefore = attributedElement.getConstraints().size();

		attributedElement.addConstraint(constr);

		Assert.assertEquals(constraintCountBefore, attributedElement
				.getConstraints().size());
		Assert.assertTrue(attributedElement.getConstraints().contains(constr));
	}

	/**
	 * addConstraint(Constraint)
	 *
	 * TEST CASE: Adding a constraint, already contained in a superclass of this
	 * element
	 *
	 * NOTE: This method is called upon in all of this classes´ subclasses.
	 */
	public final void testAddConstraint4(AEC superClass) {
		Constraint constr = new ConstraintImpl("", "SomeConstraint", "");

		int constraintCountBefore = attributedElement.getConstraints().size();

		superClass.addConstraint(constr);
		attributedElement.addConstraint(constr);

		Assert.assertEquals(constraintCountBefore + 1, attributedElement
				.getConstraints().size());
		Assert.assertTrue(attributedElement.getConstraints().contains(constr));
	}

	/**
	 * addConstraint(Constraint)
	 *
	 * TEST CASE: Adding a constraint, already contained in a subclass of this
	 * element
	 *
	 * NOTE: This method is called upon in all of this classes´ subclasses.
	 */
	public final void testAddConstraint5(AEC subClass) {
		Constraint constr = new ConstraintImpl("", "SomeConstraint", "");

		int constraintCountBefore = attributedElement.getConstraints().size();

		subClass.addConstraint(constr);
		attributedElement.addConstraint(constr);

		Assert.assertEquals(constraintCountBefore + 1, attributedElement
				.getConstraints().size());
		Assert.assertTrue(attributedElement.getConstraints().contains(constr));
	}

	/*
	 * Tests for the compareTo(AttributedElementClass) method
	 */

	/**
	 * compareTo(AttributedElementClass)
	 *
	 * TEST CASE: Comparing this element to another, where this element´s
	 * qualified name is lexicographically less than the other´s
	 *
	 * Note: This method is called upon in all of this classes´ subclasses.
	 */
	public final void testCompareTo(AEC other) {
		int comp = attributedElement.compareTo(other);

		Assert.assertTrue(comp < 0);
	}

	/**
	 * compareTo(AttributedElementClass)
	 *
	 * TEST CASE: Comparing this element to another, where this element´s
	 * qualified name is lexicographically greater than the other´s
	 *
	 * Note: This method is called upon in all of this classes´ subclasses.
	 */
	public final void testCompareTo2(AEC other) {
		int comp = attributedElement.compareTo(other);

		Assert.assertTrue(comp > 0);
	}

	/**
	 * compareTo(AttributedElementClass)
	 *
	 * TEST CASE: Comparing this element to another, where both element´s
	 * qualified names are equal
	 *
	 * TEST CASE: Comparing an element to itself
	 *
	 * Note: This method is called upon in all of this classes´ subclasses.
	 */
	public final void testCompareTo3(AEC other) {
		int comp = attributedElement.compareTo(other);

		Assert.assertTrue(comp == 0);
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
	public final void testGetAttributeList5(AEC subClass) {
		Assert.assertTrue(attributedElement.getAttributeList().isEmpty());
	}

	/*
	 * Tests for the getConstraints() method.
	 */
	/**
	 * getConstraints()
	 *
	 * TEST CASE: Getting an element´s list of constraints, that has only one
	 * constraint
	 */
	@Test
	public final void testGetConstraints() {
		Constraint constr = new ConstraintImpl("", "SomeConstraint", "");

		attributedElement.addConstraint(constr);

		Assert.assertEquals(1, attributedElement.getConstraints().size());
		Assert.assertTrue(attributedElement.getConstraints().contains(constr));
	}

	/**
	 * getConstraints()
	 *
	 * TEST CASE: Getting an element´s list of constraints, that has only one
	 * constraint
	 */
	@Test
	public final void testGetConstraints2() {
		Constraint constr = new ConstraintImpl("", "SomeConstraint", "");
		Constraint constr2 = new ConstraintImpl("", "SomeOtherConstraint", "");

		attributedElement.addConstraint(constr);
		attributedElement.addConstraint(constr2);

		Assert.assertEquals(2, attributedElement.getConstraints().size());
		Assert.assertTrue(attributedElement.getConstraints().contains(constr));
		Assert.assertTrue(attributedElement.getConstraints().contains(constr2));
	}

	/**
	 * getConstraints()
	 *
	 * TEST CASE: Getting an element´s list of constraints, that has no
	 * constraints at all
	 */
	@Test
	public final void testGetConstraints3() {
		Assert.assertTrue(attributedElement.getConstraints().isEmpty());
	}

	/**
	 * getConstraints()
	 *
	 * TEST CASE: Getting an element´s list of constraints, that has a
	 * superclass with constraints
	 *
	 * Note: This method is called upon in all of this classes´ subclasses.
	 */
	public final void testGetConstraints4(AEC superClass) {
		Constraint constr = new ConstraintImpl("", "SomeConstraint", "");
		Constraint constr2 = new ConstraintImpl("", "SomeOtherConstraint", "");

		superClass.addConstraint(constr);
		superClass.addConstraint(constr2);

		Assert.assertTrue(attributedElement.getConstraints().isEmpty());
	}

	@Test
	public void testGetDirectoryName() {
		// TODO Auto-generated method stub
	}

	@Test
	public void testGetSchemaClass() {
		// TODO Auto-generated method stub
	}

	@Test
	public void testGetSchemaImplementationClass() {
		// TODO Auto-generated method stub
	}

	@Test
	public void testGetName() {
		// TODO Auto-generated method stub
	}

	/*
	 * Tests for the getOwnAttribute() method.
	 */

	/**
	 * getOwnAttribute()
	 *
	 * TEST CASE: Getting a direct attribute
	 */
	@Test
	public final void testGetOwnAttribute() {
		Attribute attribute = attributedElement.createAttribute(
				"testAttribute", schema.getBooleanDomain(), "null");

		Attribute attr = attributedElement.getOwnAttribute(attribute.getName());

		Assert.assertSame(attribute, attr);
	}

	/**
	 * getOwnAttribute()
	 *
	 * TEST CASE: Trying to get a non existent attribute
	 */
	@Test
	public final void testGetOwnAttribute2() {
		Assert.assertNull(attributedElement
				.getOwnAttribute("nonexistentAttribute"));
	}

	/**
	 * getOwnAttribute()
	 *
	 * TEST CASE: Trying to get an attribute with an empty name
	 */
	@Test
	public final void testGetOwnAttribute3() {
		Assert.assertNull(attributedElement.getOwnAttribute(""));
	}

	/**
	 * getOwnAttribute()
	 *
	 * TEST CASE: Trying to get an attribute present in a superclass of this
	 * element
	 *
	 * TEST CASE: Trying to get an attribute present in a subclass of this
	 * element
	 *
	 * NOTE: This method is called upon in all of this classes´ subclasses.
	 */
	public final void testGetOwnAttribute4(AEC otherClass) {
		Attribute attribute = otherClass.createAttribute("testAttribute",
				schema.getBooleanDomain(), "null");

		Assert.assertNull(attributedElement.getOwnAttribute(attribute.getName()));
	}

	/*
	 * Tests for the getOwnAttributeCount() method.
	 */
	/**
	 * getOwnAttributeCount()
	 *
	 * TEST CASE: Getting the number of attributes of an element that only has
	 * one direct attribute
	 */
	@Test
	public final void testGetOwnAttributeCount() {
		attributedElement.createAttribute("testAttribute",
				schema.getBooleanDomain(), "null");

		Assert.assertEquals(1, attributedElement.getOwnAttributeCount());
	}

	/**
	 * getOwnAttributeCount()
	 *
	 * TEST CASE: Getting the number of attributes of an element that has
	 * multiple direct attributes
	 */
	@Test
	public final void testGetOwnAttributeCount2() {
		attributedElement.createAttribute("testAttribute",
				schema.getBooleanDomain(), "null");
		attributedElement.createAttribute("testAttribute2",
				schema.getBooleanDomain(), "null");

		Assert.assertEquals(2, attributedElement.getOwnAttributeCount());
	}

	/**
	 * getOwnAttributeCount()
	 *
	 * TEST CASE: Getting the number of attributes of an element that has no
	 * direct attributes
	 */
	@Test
	public final void testGetOwnAttributeCount3() {
		Assert.assertEquals(0, attributedElement.getOwnAttributeCount());
	}

	/**
	 * getOwnAttributeCount()
	 *
	 * TEST CASE: Getting the number of attributes of an element that only has
	 * inherited attributes and no direct attributes
	 *
	 * NOTE: This method is called upon in all of this classes´ subclasses.
	 */
	public final void testGetOwnAttributeCount4(AEC superClass) {
		superClass.createAttribute("testAttribute", schema.getBooleanDomain(),
				"null");

		Assert.assertEquals(0, attributedElement.getOwnAttributeCount());
	}

	/*
	 * Tests for the getOwnAttributeList() method.
	 */
	/**
	 * getOwnAttributeList()
	 *
	 * TEST CASE: Getting an element´s list of attributes, that has one direct
	 * attribute
	 */
	@Test
	public final void testGetOwnAttributeList() {

		Attribute attribute = attributedElement.createAttribute(
				"testAttribute", schema.getBooleanDomain(), "null");

		List<Attribute> attrs = attributedElement.getOwnAttributeList();

		Assert.assertEquals(1, attrs.size());
		Assert.assertTrue(attrs.contains(attribute));
	}

	/**
	 * getOwnAttributeList()
	 *
	 * TEST CASE: Getting an element´s list of attributes, that has mutliple
	 * direct attributes
	 */
	@Test
	public final void testGetOwnAttributeList2() {
		Attribute attribute = attributedElement.createAttribute(
				"testAttribute", schema.getBooleanDomain(), "null");
		Attribute attribute2 = attributedElement.createAttribute(
				"testAttribute2", schema.getBooleanDomain(), "null");

		List<Attribute> attrs = attributedElement.getOwnAttributeList();

		Assert.assertEquals(2, attrs.size());
		Assert.assertTrue(attrs.contains(attribute));
		Assert.assertTrue(attrs.contains(attribute2));
	}

	/**
	 * getOwnAttributeList()
	 *
	 * TEST CASE: Getting an element´s list of attributes, that has no direct
	 * attributes
	 */
	@Test
	public final void testGetOwnAttributeList3() {
		Assert.assertTrue(attributedElement.getOwnAttributeList().isEmpty());
	}

	/**
	 * getOwnAttributeList()
	 *
	 * TEST CASE: Getting an element´s list of attributes, that only has
	 * inherited attributes and no direct attributes
	 *
	 * NOTE: This method is called upon in all of this classes´ subclasses.
	 */
	public final void testGetOwnAttributeList4(AEC superClass) {
		superClass.createAttribute("testAttribute", schema.getBooleanDomain(),
				"null");

		Assert.assertTrue(attributedElement.getOwnAttributeList().isEmpty());
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

	/*
	 * Tests for the hasAttributes() method.
	 */
	/**
	 * hasAttributes()
	 *
	 * TEST CASE: The element has one direct attribute
	 */
	@Test
	public final void testHasAttributes() {
		attributedElement.createAttribute("testAttribute",
				schema.getBooleanDomain(), "null");

		Assert.assertTrue(attributedElement.hasAttributes());
	}

	/**
	 * hasAttributes()
	 *
	 * TEST CASE: The element has multiple direct attributes
	 */
	@Test
	public final void testHasAttributes2() {

		attributedElement.createAttribute("testAttribute",
				schema.getBooleanDomain(), "null");
		attributedElement.createAttribute("testAttribute2",
				schema.getBooleanDomain(), "null");

		Assert.assertTrue(attributedElement.hasAttributes());
	}

	/**
	 * hasAttributes()
	 *
	 * TEST CASE: The element has one inherited attribute
	 *
	 * NOTE: This method is called upon in all of this classes´ subclasses.
	 */
	public final void testHasAttributes3(AEC superClass) {

		superClass.createAttribute("testAttribute", schema.getBooleanDomain(),
				"null");

		Assert.assertTrue(attributedElement.hasAttributes());
	}

	/**
	 * hasAttributes()
	 *
	 * TEST CASE: The element has multiple inherited attributes
	 *
	 * NOTE: This method is called upon in all of this classes´ subclasses.
	 */
	public final void testHasAttributes4(AEC superClass) {
		superClass.createAttribute("testAttribute", schema.getBooleanDomain(),
				"null");
		superClass.createAttribute("testAttribute2", schema.getBooleanDomain(),
				"null");

		Assert.assertTrue(attributedElement.hasAttributes());
	}

	/**
	 * hasAttributes()
	 *
	 * TEST CASE: The element has multiple direct and indirect attributes
	 *
	 * NOTE: This method is called upon in all of this classes´ subclasses.
	 */
	public final void testHasAttributes5(AEC superClass) {
		attributedElement.createAttribute("testAttribute",
				schema.getBooleanDomain(), "null");
		attributedElement.createAttribute("testAttribute2",
				schema.getBooleanDomain(), "null");
		superClass.createAttribute("testAttribute3", schema.getBooleanDomain(),
				"null");
		superClass.createAttribute("testAttribute4", schema.getBooleanDomain(),
				"null");

		Assert.assertTrue(attributedElement.hasAttributes());
	}

	/**
	 * hasAttributes()
	 *
	 * TEST CASE: The element has no direct and no indirect attributes
	 */
	@Test
	public final void testHasAttributes6() {
		Assert.assertFalse(attributedElement.hasAttributes());
	}

	/*
	 * Tests for the hasOwnAttributes() method.
	 */
	/**
	 * hasOwnAttributes()
	 *
	 * TEST CASE: The element has one direct attribute
	 */
	@Test
	public final void testHasOwnAttributes() {
		attributedElement.createAttribute("testAttribute",
				schema.getBooleanDomain(), "null");

		Assert.assertTrue(attributedElement.hasOwnAttributes());
	}

	/**
	 * hasOwnAttributes()
	 *
	 * TEST CASE: The element has multiple direct attributes
	 */
	@Test
	public final void testHasOwnAttributes2() {
		attributedElement.createAttribute("testAttribute",
				schema.getBooleanDomain(), "null");
		attributedElement.createAttribute("testAttribute2",
				schema.getBooleanDomain(), "null");

		Assert.assertTrue(attributedElement.hasOwnAttributes());
	}

	/**
	 * hasOwnAttributes()
	 *
	 * TEST CASE: The element has no direct and no indirect attributes
	 */
	@Test
	public final void testHasOwnAttributes3() {
		Assert.assertFalse(attributedElement.hasOwnAttributes());
	}

	/**
	 * hasOwnAttributes()
	 *
	 * TEST CASE: The element has direct and inherited attributes
	 *
	 * NOTE: This method is called upon in all of this classes´ subclasses.
	 */
	public final void testHasOwnAttributes4(AEC superClass) {
		attributedElement.createAttribute("testAttribute",
				schema.getBooleanDomain(), "null");
		superClass.createAttribute("testAttribute2", schema.getBooleanDomain(),
				"null");

		Assert.assertTrue(attributedElement.hasOwnAttributes());
	}

	/*
	 * Tests for the isAbstract() method.
	 */
	/**
	 * isAbstract()
	 *
	 * TEST CASE: The element is abstract
	 */
	@Test
	public final void testIsAbstract() {
		attributedElement.setAbstract(true);

		Assert.assertTrue(attributedElement.isAbstract());

	}

	/**
	 * isAbstract()
	 *
	 * TEST CASE: The element is not abstract
	 */
	@Test
	public final void testIsAbstract2() {
		attributedElement.setAbstract(false);

		Assert.assertFalse(attributedElement.isAbstract());
	}

	/*
	 * Tests for the setAbstract() method.
	 */
	/**
	 * setAbstract()
	 *
	 * TEST CASE: The element is set to an abstract state
	 */
	@Test
	public void testSetAbstract() {
		attributedElement.setAbstract(true);

		Assert.assertTrue(attributedElement.isAbstract());
	}

	/**
	 * setAbstract()
	 *
	 * TEST CASE: The element is set to a non-abstract state
	 */
	@Test
	public void testSetAbstract2() {
		attributedElement.setAbstract(false);

		Assert.assertFalse(attributedElement.isAbstract());
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

	@Test(expected = SchemaException.class)
	public void testCreateAttributeInvalid() {
		// spaces in attr name
		attributedElement.createAttribute("bla bla", schema.getStringDomain());
	}

	@Test(expected = SchemaException.class)
	public void testCreateAttributeInvalid2() {
		// leading number
		attributedElement.createAttribute("3bla", schema.getStringDomain());
	}

	@Test(expected = SchemaException.class)
	public void testCreateAttributeInvalid3() {
		// against convention
		attributedElement.createAttribute("Bla", schema.getStringDomain());
	}

	public abstract void testToString();

}
