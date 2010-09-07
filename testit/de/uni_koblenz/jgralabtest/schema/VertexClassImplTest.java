/*
 * JGraLab - The Java graph laboratory
 * (c) 2006-2010 Institute for Software Technology
 *               University of Koblenz-Landau, Germany
 * 
 *               ist@uni-koblenz.de
 * 
 * Please report bugs to http://serres.uni-koblenz.de/bugzilla
 * 
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package de.uni_koblenz.jgralabtest.schema;

import java.util.Vector;

import org.junit.Before;
import org.junit.Test;

import de.uni_koblenz.jgralab.schema.AttributedElementClass;
import de.uni_koblenz.jgralab.schema.VertexClass;

public final class VertexClassImplTest extends GraphElementClassImplTest {

	private VertexClass vertexClass;

	@Before
	@Override
	public void setUp() {
		super.setUp();

		attributedElement = vertexClass = graphClass
				.createVertexClass("VertexClass1");
	}

	/**
	 * addAttribute(Attribute)
	 * 
	 * TEST CASE: Adding an attribute, already contained in a superclass of this
	 * element
	 */
	@Test
	public void testAddAttribute4() {
		VertexClass superClass = graphClass
				.createVertexClass("VertexClassSuperClass");
		vertexClass.addSuperClass(superClass);

		testAddAttribute4(superClass);
	}

	/**
	 * addAttribute(Attribute)
	 * 
	 * TEST CASE: Adding an attribute, already contained in a subclass of this
	 * element
	 */
	@Test
	public void testAddAttribute5() {
		VertexClass subClass = graphClass
				.createVertexClass("VertexClassSubClass");
		subClass.addSuperClass(vertexClass);

		testAddAttribute5(subClass);
	}

	/**
	 * addConstraint(Constraint)
	 * 
	 * TEST CASE: Adding a constraint, already contained in a superclass of this
	 * element
	 */
	@Test
	public void testAddConstraint4() {
		VertexClass superClass = graphClass
				.createVertexClass("VertexClassSuperClass");
		vertexClass.addSuperClass(superClass);

		testAddConstraint4(superClass);
	}

	/**
	 * addConstraint(Constraint)
	 * 
	 * TEST CASE: Adding a constraint, already contained in a subclass of this
	 * element
	 */
	@Test
	public void testAddConstraint5() {
		VertexClass subClass = graphClass
				.createVertexClass("VertexClassSubClass");
		subClass.addSuperClass(vertexClass);

		testAddConstraint5(subClass);
	}

	/**
	 * containsAttribute(String)
	 * 
	 * TEST CASE: looking for an attribute, present in a superclass of this
	 * element
	 */
	@Test
	public void testContainsAttribute3() {
		VertexClass superClass = graphClass
				.createVertexClass("VertexClassSuperClass");

		vertexClass.addSuperClass(superClass);

		testContainsAttribute3(superClass);
	}

	/**
	 * getAllSubClasses()
	 * 
	 * TEST CASE: Getting all subclasses of an element with one direct subclass
	 */
	@Test
	public void testGetAllSubClasses() {
		Vector<AttributedElementClass> expectedSubClasses = new Vector<AttributedElementClass>();

		VertexClass subClass = graphClass
				.createVertexClass("VertexClassSubClass");

		subClass.addSuperClass(vertexClass);

		expectedSubClasses.add(subClass);

		testGetAllSubClasses(expectedSubClasses);
	}

	/**
	 * getAllSubClasses()
	 * 
	 * TEST CASE: Getting all subclasses of an element with multiple direct
	 * subclasses
	 */
	@Test
	public void testGetAllSubClasses2() {
		Vector<AttributedElementClass> expectedSubClasses = new Vector<AttributedElementClass>();

		VertexClass subClass = graphClass
				.createVertexClass("VertexClassSubClass");
		VertexClass subClass2 = graphClass
				.createVertexClass("VertexClassSubClass2");

		subClass.addSuperClass(vertexClass);
		subClass2.addSuperClass(vertexClass);

		expectedSubClasses.add(subClass);
		expectedSubClasses.add(subClass2);

		testGetAllSubClasses(expectedSubClasses);
	}

	/**
	 * getAllSubClasses()
	 * 
	 * TEST CASE: Getting all subclasses of an element with multiple direct and
	 * indirect subclasses
	 */
	@Test
	public void testGetAllSubClasses3() {
		Vector<AttributedElementClass> expectedSubClasses = new Vector<AttributedElementClass>();

		VertexClass subClass = graphClass
				.createVertexClass("VertexClassSubClass");
		VertexClass subClass2 = graphClass
				.createVertexClass("VertexClassSubClass2");

		subClass.addSuperClass(vertexClass);
		subClass2.addSuperClass(subClass);

		expectedSubClasses.add(subClass);
		expectedSubClasses.add(subClass2);

		testGetAllSubClasses(expectedSubClasses);
	}

	/**
	 * getAllSubClasses()
	 * 
	 * TEST CASE: Getting all subclasses of an element that has no subclasses
	 */
	@Test
	public void testGetAllSubClasses4() {
		// no subclasses expected
		testGetAllSubClasses(new Vector<AttributedElementClass>());
	}

	/**
	 * getAllSuperClasses()
	 * 
	 * TEST CASE: Getting all superclasses of an element with one direct
	 * superclass
	 */
	@Test
	public void testGetAllSuperClasses() {
		Vector<AttributedElementClass> expectedSuperClasses = new Vector<AttributedElementClass>();

		VertexClass superClass = graphClass
				.createVertexClass("VertexClassSuperClass");

		vertexClass.addSuperClass(superClass);

		expectedSuperClasses.add(schema.getDefaultVertexClass());
		expectedSuperClasses.add(superClass);

		testGetAllSuperClasses(expectedSuperClasses);
	}

	/**
	 * getAllSuperClasses()
	 * 
	 * TEST CASE: Getting all superclasses of an element with multiple direct
	 * superclasses
	 */
	@Test
	public void testGetAllSuperClasses2() {
		Vector<AttributedElementClass> expectedSuperClasses = new Vector<AttributedElementClass>();

		VertexClass superClass = graphClass
				.createVertexClass("VertexClassSuperClass");
		VertexClass superClass2 = graphClass
				.createVertexClass("VertexClassSuperClass2");

		vertexClass.addSuperClass(superClass);
		vertexClass.addSuperClass(superClass2);

		expectedSuperClasses.add(schema.getDefaultVertexClass());
		expectedSuperClasses.add(superClass);
		expectedSuperClasses.add(superClass2);

		testGetAllSuperClasses(expectedSuperClasses);
	}

	/**
	 * getAllSuperClasses()
	 * 
	 * TEST CASE: Getting all superclasses of an element with multiple direct
	 * and indirect superclasses
	 */
	@Test
	public void testGetAllSuperClasses3() {
		Vector<AttributedElementClass> expectedSuperClasses = new Vector<AttributedElementClass>();

		VertexClass superClass = graphClass
				.createVertexClass("VertexClassSuperClass");
		VertexClass superClass2 = graphClass
				.createVertexClass("VertexClassSuperClass2");

		vertexClass.addSuperClass(superClass);
		superClass.addSuperClass(superClass2);

		expectedSuperClasses.add(schema.getDefaultVertexClass());
		expectedSuperClasses.add(superClass);
		expectedSuperClasses.add(superClass2);

		testGetAllSuperClasses(expectedSuperClasses);
	}

	/**
	 * getAllSuperClasses()
	 * 
	 * TEST CASE: Getting all superclasses of an element that has no
	 * superclasses
	 */
	@Test
	public void testGetAllSuperClasses4() {
		Vector<AttributedElementClass> expectedSuperClasses = new Vector<AttributedElementClass>();

		expectedSuperClasses.add(schema.getDefaultVertexClass());

		testGetAllSuperClasses(expectedSuperClasses);
	}

	/**
	 * getAttribute()
	 * 
	 * TEST CASE: Getting an inherited attribute
	 */
	@Test
	public void testGetAttribute2() {
		VertexClass superClass = graphClass
				.createVertexClass("VertexClassSuperClass");

		vertexClass.addSuperClass(superClass);

		testGetAttribute2(superClass);
	}

	/**
	 * getAttribute()
	 * 
	 * TEST CASE: Trying to get an attribute present in a subclass of this
	 * element
	 */
	@Test
	public void testGetAttribute5() {
		VertexClass subClass = graphClass
				.createVertexClass("VertexClassSubClass");

		subClass.addSuperClass(vertexClass);

		testGetAttribute5(subClass);
	}

	/**
	 * getAttributeCount()
	 * 
	 * TEST CASE: Getting the number of attributes of an element which has
	 * exactly only one inherited attribute and no direct attributes
	 */
	@Test
	public void testGetAttributeCount2() {
		VertexClass superClass = graphClass
				.createVertexClass("VertexClassSuperClass");

		vertexClass.addSuperClass(superClass);

		testGetAttributeCount2(superClass);
	}

	/**
	 * getAttributeCount()
	 * 
	 * TEST CASE: Getting the number of attributes of an element which has
	 * multiple direct and indirect attributes
	 */
	@Test
	public void testGetAttributeCount3() {
		VertexClass superClass = graphClass
				.createVertexClass("VertexClassSuperClass");

		vertexClass.addSuperClass(superClass);

		testGetAttributeCount3(superClass);
	}

	/**
	 * getAttributeCount()
	 * 
	 * TEST CASE: Getting the number of attributes of an element which has no
	 * direct nor inherited attributes but whose subclass has attributes
	 */
	@Test
	public void testGetAttributeCount5() {
		VertexClass subClass = graphClass
				.createVertexClass("VertexClassSubClass");

		subClass.addSuperClass(vertexClass);

		testGetAttributeCount5(subClass);
	}

	/**
	 * getAttributeList()
	 * 
	 * TEST CASE: Getting an element´s list of attributes, which has exactly one
	 * inherited attribute and no direct attributes
	 */
	@Test
	public void testGetAttributeList2() {
		VertexClass superClass = graphClass
				.createVertexClass("VertexClassSuperClass");

		vertexClass.addSuperClass(superClass);

		testGetAttributeList2(superClass);
	}

	/**
	 * getAttributeList()
	 * 
	 * TEST CASE: Getting an element´s list of attributes, which has mutliple
	 * direct and inherited attributes
	 */
	@Test
	public void testGetAttributeList3() {
		VertexClass superClass = graphClass
				.createVertexClass("VertexClassSuperClass");

		vertexClass.addSuperClass(superClass);

		testGetAttributeList3(superClass);
	}

	/**
	 * getAttributeList()
	 * 
	 * TEST CASE: Getting an element´s list of attributes, which has no direct
	 * nor inherited attributes but whose subclass has attributes
	 */
	@Test
	public void testGetAttributeList5() {
		VertexClass subClass = graphClass
				.createVertexClass("VertexClassSubClass");

		subClass.addSuperClass(vertexClass);

		testGetAttributeList5(subClass);
	}

	/**
	 * getConstraints()
	 * 
	 * TEST CASE: Getting an element´s list of constraints, that has a
	 * superclass with constraints
	 */
	@Test
	public void testGetConstraints4() {
		VertexClass superClass = graphClass
				.createVertexClass("VertexClassSuperClass");

		vertexClass.addSuperClass(superClass);

		testGetConstraints4(superClass);
	}

	/**
	 * getDirectSubClasses()
	 * 
	 * TEST CASE: Getting all direct subclasses of an element that has one
	 * direct subclass.
	 */
	@Test
	public void testGetDirectSubClasses() {
		Vector<AttributedElementClass> expectedSubClasses = new Vector<AttributedElementClass>();

		VertexClass subClass = graphClass
				.createVertexClass("VertexClassSubClass");

		subClass.addSuperClass(vertexClass);

		expectedSubClasses.add(subClass);

		testGetDirectSubClasses(expectedSubClasses);
	}

	/**
	 * getDirectSubClasses()
	 * 
	 * TEST CASE: Getting all direct subclasses of an element that has multiple
	 * direct subclasses.
	 */
	@Test
	public void testGetDirectSubClasses2() {
		Vector<AttributedElementClass> expectedSubClasses = new Vector<AttributedElementClass>();

		VertexClass subClass = graphClass
				.createVertexClass("VertexClassSubClass");
		VertexClass subClass2 = graphClass
				.createVertexClass("VertexClassSubClass2");

		subClass.addSuperClass(vertexClass);
		subClass2.addSuperClass(vertexClass);

		expectedSubClasses.add(subClass);
		expectedSubClasses.add(subClass2);

		testGetDirectSubClasses(expectedSubClasses);
	}

	/**
	 * getDirectSubClasses()
	 * 
	 * TEST CASE: Getting all direct subclasses of an element that has multiple
	 * direct and indirect subclasses.
	 */
	@Test
	public void testGetDirectSubClasses3() {
		Vector<AttributedElementClass> expectedSubClasses = new Vector<AttributedElementClass>();

		VertexClass subClass = graphClass
				.createVertexClass("VertexClassSubClass"); // Direct subclass
		VertexClass subClass2 = graphClass
				.createVertexClass("VertexClassSubClass2"); // Indirect subclass

		subClass.addSuperClass(vertexClass);
		subClass2.addSuperClass(subClass);

		expectedSubClasses.add(subClass);

		testGetDirectSubClasses(expectedSubClasses);
	}

	/**
	 * getDirectSubClasses()
	 * 
	 * TEST CASE: Getting all direct subclasses of an element that has no direct
	 * subclasses.
	 */
	@Test
	public void testGetDirectSubClasses4() {
		testGetDirectSubClasses(new Vector<AttributedElementClass>());
	}

	/**
	 * getDirectSuperClasses()
	 * 
	 * TEST CASE: Getting all direct superclasses of an element that has one
	 * direct superclass.
	 */
	@Test
	public void testGetDirectSuperClasses() {
		Vector<AttributedElementClass> expectedSuperClasses = new Vector<AttributedElementClass>();

		VertexClass superClass = graphClass
				.createVertexClass("VertexClassSuperClass");

		vertexClass.addSuperClass(superClass);

		expectedSuperClasses.add(superClass);

		testGetDirectSuperClasses(expectedSuperClasses);
	}

	/**
	 * getDirectSuperClasses()
	 * 
	 * TEST CASE: Getting all direct superclasses of an element that has
	 * multiple direct superclasses.
	 */
	@Test
	public void testGetDirectSuperClasses2() {
		Vector<AttributedElementClass> expectedSuperClasses = new Vector<AttributedElementClass>();

		VertexClass superClass = graphClass
				.createVertexClass("VertexClassSuperClass");
		VertexClass superClass2 = graphClass
				.createVertexClass("VertexClassSuperClass2");

		vertexClass.addSuperClass(superClass);
		vertexClass.addSuperClass(superClass2);

		expectedSuperClasses.add(superClass);
		expectedSuperClasses.add(superClass2);

		testGetDirectSuperClasses(expectedSuperClasses);
	}

	/**
	 * getDirectSuperClasses()
	 * 
	 * TEST CASE: Getting all direct superclasses of an element that has
	 * multiple direct and indirect superclasses.
	 */
	@Test
	public void testGetDirectSuperClasses3() {
		Vector<AttributedElementClass> expectedSuperClasses = new Vector<AttributedElementClass>();

		VertexClass superClass = graphClass // Direct superclass
				.createVertexClass("VertexClassSuperClass");
		VertexClass superClass2 = graphClass // Indirect superclass
				.createVertexClass("VertexClassSuperClass2");

		vertexClass.addSuperClass(superClass);
		superClass.addSuperClass(superClass2);

		expectedSuperClasses.add(superClass);

		testGetDirectSuperClasses(expectedSuperClasses);
	}

	/**
	 * getDirectSuperClasses()
	 * 
	 * TEST CASE: Getting all direct superclasses of an element that has no
	 * direct superclasses.
	 */
	@Test
	public void testGetDirectSuperClasses4() {
		Vector<AttributedElementClass> expectedSuperClasses = new Vector<AttributedElementClass>();

		expectedSuperClasses.add(schema.getDefaultVertexClass());

		testGetDirectSuperClasses(expectedSuperClasses);
	}

	@Test
	public void testAddEdgeClass() {
		// TODO Auto-generated method stub
	}

	@Test
	public void testAddSuperClass() {
		// TODO Auto-generated method stub
	}

	@Test
	public void testGetDirectedEdgeClasses() {
		// TODO Auto-generated method stub
	}

	@Test
	public void testGetEdgeClasses() {
		// TODO Auto-generated method stub
	}

	/**
	 * getOwnAttribute()
	 * 
	 * TEST CASE: Trying to get an attribute present in a superclass of this
	 * element
	 */
	@Test
	public void testGetOwnAttribute4() {
		VertexClass superClass = graphClass.createVertexClass("Superclass");
		vertexClass.addSuperClass(superClass);

		testGetOwnAttribute4(superClass);
	}

	/**
	 * getOwnAttribute()
	 * 
	 * TEST CASE: Trying to get an attribute present in a subclass of this
	 * element
	 */
	@Test
	public void testGetOwnAttribute5() {
		VertexClass subClass = graphClass.createVertexClass("Subclass");
		subClass.addSuperClass(vertexClass);

		testGetOwnAttribute4(subClass);
	}

	/**
	 * getOwnAttributeCount()
	 * 
	 * TEST CASE: Getting the number of attributes of an element that only has
	 * inherited attributes and no direct attributes
	 */
	@Test
	public void testGetOwnAttributeCount4() {
		VertexClass superClass = graphClass.createVertexClass("Superclass");
		vertexClass.addSuperClass(superClass);

		testGetOwnAttributeCount4(superClass);
	}

	/**
	 * getOwnAttributeList()
	 * 
	 * TEST CASE: Getting an element´s list of attributes, that only has
	 * inherited attributes and no direct attributes
	 */
	@Test
	public void testGetOwnAttributeList4() {
		VertexClass superClass = graphClass.createVertexClass("Superclass");
		vertexClass.addSuperClass(superClass);

		testGetOwnAttributeList4(superClass);
	}

	@Override
	@Test
	public void testGetM1Class() {
		// TODO Auto-generated method stub
	}

	@Test
	public void testGetOwnDirectedEdgeClasses() {
		// TODO Auto-generated method stub
	}

	@Test
	public void testGetOwnEdgeClasses() {
		// TODO Auto-generated method stub
	}

	@Test
	public void testGetRolenameMap() {
		// TODO Auto-generated method stub
	}

	@Test
	public void testGetValidDirectedEdgeClasses() {
		// TODO Auto-generated method stub
	}

	@Test
	public void testGetValidFromEdgeClasses() {
		// TODO Auto-generated method stub
	}

	@Test
	public void testGetValidToEdgeClasses() {
		// TODO Auto-generated method stub
	}

	@Test
	public void testGetVariableName() {
		// TODO Auto-generated method stub
	}

	/**
	 * hasAttributes()
	 * 
	 * TEST CASE: The element has one inherited attribute
	 */
	@Test
	public void testHasAttributes3() {
		VertexClass superClass = graphClass.createVertexClass("Superclass");
		vertexClass.addSuperClass(superClass);

		testHasAttributes3(superClass);
	}

	/**
	 * hasAttributes()
	 * 
	 * TEST CASE: The element has multiple inherited attributes
	 */
	@Test
	public void testHasAttributes4() {
		VertexClass superClass = graphClass.createVertexClass("Superclass");
		vertexClass.addSuperClass(superClass);

		testHasAttributes4(superClass);
	}

	/**
	 * hasAttributes()
	 * 
	 * TEST CASE: The element has multiple direct and indirect attributes
	 */
	@Test
	public void testHasAttributes5() {
		VertexClass superClass = graphClass.createVertexClass("Superclass");
		vertexClass.addSuperClass(superClass);

		testHasAttributes5(superClass);
	}

	/**
	 * hasOwnAttributes()
	 * 
	 * TEST CASE: The element has direct and inherited attributes
	 */
	@Test
	public void testHasOwnAttributes4() {
		VertexClass superClass = graphClass.createVertexClass("Superclass");
		vertexClass.addSuperClass(superClass);

		testHasOwnAttributes4(superClass);
	}

	/**
	 * hasOwnAttributes()
	 * 
	 * TEST CASE: The element has no direct but indirect attributes
	 */
	@Test
	public void testHasOwnAttributes5() {
		VertexClass superClass = graphClass.createVertexClass("Superclass");
		vertexClass.addSuperClass(superClass);

		testHasOwnAttributes5(superClass);
	}

	/**
	 * isDirectSubClassOf()
	 * 
	 * TEST CASE: The other element is a direct superclass of this element
	 */
	@Test
	public void testIsDirectSubClassOf() {
		VertexClass superClass = graphClass.createVertexClass("Superclass");
		vertexClass.addSuperClass(superClass);

		testIsDirectSubClassOf(superClass);
	}

	/**
	 * isDirectSubClassOf()
	 * 
	 * TEST CASE: The other element is an inherited superclass of this element
	 */
	@Test
	public void testIsDirectSubClassOf2() {
		VertexClass superClass = graphClass.createVertexClass("Superclass");
		VertexClass superClass2 = graphClass.createVertexClass("Superclass2");
		vertexClass.addSuperClass(superClass);
		superClass.addSuperClass(superClass2);

		testIsDirectSubClassOf2(superClass2);
	}

	/**
	 * isDirectSubClassOf()
	 * 
	 * TEST CASE: The other element is a subclass of this element
	 */
	@Test
	public void testIsDirectSubClassOf3() {
		VertexClass subClass = graphClass.createVertexClass("SubClass");
		subClass.addSuperClass(vertexClass);

		testIsDirectSubClassOf2(subClass);
	}

	/**
	 * isDirectSubClassOf()
	 * 
	 * TEST CASE: The other element has no relation with this element
	 */
	@Test
	public void testIsDirectSubClassOf4() {
		VertexClass vertexClass2 = graphClass.createVertexClass("VertexClass2");

		testIsDirectSubClassOf2(vertexClass2);
	}

	/**
	 * isDirectSubClassOf()
	 * 
	 * TEST CASE: The other element and this element are the same
	 */
	@Test
	public void testIsDirectSubClassOf5() {
		testIsDirectSubClassOf2(vertexClass);
	}

	/**
	 * isDirectSuperClassOf()
	 * 
	 * TEST CASE: The other element is a direct subclass of this element
	 */
	@Test
	public void testIsDirectSuperClassOf() {
		VertexClass subClass = graphClass.createVertexClass("Subclass");
		subClass.addSuperClass(vertexClass);

		testIsDirectSuperClassOf(subClass);
	}

	/**
	 * isDirectSuperClassOf()
	 * 
	 * TEST CASE: The other element is an inherited subclass of this element
	 */
	@Test
	public void testIsDirectSuperClassOf2() {
		VertexClass subClass = graphClass.createVertexClass("SubClass");
		VertexClass subClass2 = graphClass.createVertexClass("SubClass2");
		subClass.addSuperClass(vertexClass);
		subClass2.addSuperClass(subClass);

		testIsDirectSuperClassOf2(subClass2);
	}

	/**
	 * isDirectSuperClassOf()
	 * 
	 * TEST CASE: The other element is a superclass of this element
	 */
	@Test
	public void testIsDirectSuperClassOf3() {
		VertexClass superClass = graphClass.createVertexClass("SuperClass");
		vertexClass.addSuperClass(superClass);

		testIsDirectSuperClassOf2(superClass);
	}

	/**
	 * isDirectSuperClassOf()
	 * 
	 * TEST CASE: The other element has no relation with this element
	 */
	@Test
	public void testIsDirectSuperClassOf4() {
		VertexClass vertexClass2 = graphClass.createVertexClass("VertexClass2");

		testIsDirectSuperClassOf2(vertexClass2);
	}

	/**
	 * isDirectSuperClassOf()
	 * 
	 * TEST CASE: The other element and this element are the same
	 */
	@Test
	public void testIsDirectSuperClassOf5() {
		testIsDirectSuperClassOf2(vertexClass);
	}

	/**
	 * isSubClassOf()
	 * 
	 * TEST CASE: The other element is a direct superclass of this element
	 */
	@Test
	public void testIsSubClassOf() {
		VertexClass superClass = graphClass.createVertexClass("Superclass");
		vertexClass.addSuperClass(superClass);

		testIsSubClassOf(superClass);
	}

	/**
	 * isSubClassOf()
	 * 
	 * TEST CASE: The other element is an inherited superclass of this element
	 */
	@Test
	public void testIsSubClassOf2() {
		VertexClass superClass = graphClass.createVertexClass("Superclass");
		VertexClass superClass2 = graphClass.createVertexClass("Superclass2");
		vertexClass.addSuperClass(superClass);
		superClass.addSuperClass(superClass2);

		testIsSubClassOf(superClass2);
	}

	/**
	 * isSubClassOf()
	 * 
	 * TEST CASE: The other element is a subclass of this element
	 */
	@Test
	public void testIsSubClassOf3() {
		VertexClass subClass = graphClass.createVertexClass("SubClass");
		subClass.addSuperClass(vertexClass);

		testIsSubClassOf2(subClass);
	}

	/**
	 * isSubClassOf()
	 * 
	 * TEST CASE: The other element has no relation with this element
	 */
	@Test
	public void testIsSubClassOf4() {
		VertexClass vertexClass2 = graphClass.createVertexClass("VertexClass2");

		testIsSubClassOf2(vertexClass2);
	}

	/**
	 * isSubClassOf()
	 * 
	 * TEST CASE: The other element and this element are the same
	 */
	@Test
	public void testIsSubClassOf5() {
		testIsSubClassOf2(vertexClass);
	}

	/**
	 * isSuperClassOf()
	 * 
	 * TEST CASE: The other element is a direct subclass of this element
	 */
	@Test
	public void testIsSuperClassOf() {
		VertexClass subClass = graphClass.createVertexClass("Subclass");
		subClass.addSuperClass(vertexClass);

		testIsSuperClassOf(subClass);
	}

	/**
	 * isSuperClassOf()
	 * 
	 * TEST CASE: The other element is an inherited subclass of this element
	 */
	@Test
	public void testIsSuperClassOf2() {
		VertexClass subClass = graphClass.createVertexClass("SubClass");
		VertexClass subClass2 = graphClass.createVertexClass("SubClass2");
		subClass.addSuperClass(vertexClass);
		subClass2.addSuperClass(subClass);

		testIsSuperClassOf(subClass2);
	}

	/**
	 * isSuperClassOf()
	 * 
	 * TEST CASE: The other element is a superclass of this element
	 */
	@Test
	public void testIsSuperClassOf3() {
		VertexClass superClass = graphClass.createVertexClass("SuperClass");
		vertexClass.addSuperClass(superClass);

		testIsSuperClassOf2(superClass);
	}

	/**
	 * isSuperClassOf()
	 * 
	 * TEST CASE: The other element has no relation with this element
	 */
	@Test
	public void testIsSuperClassOf4() {
		VertexClass vertexClass2 = graphClass.createVertexClass("VertexClass2");

		testIsSuperClassOf2(vertexClass2);
	}

	/**
	 * isSuperClassOf()
	 * 
	 * TEST CASE: The other element and this element are the same
	 */
	@Test
	public void testIsSuperClassOf5() {
		testIsSuperClassOf2(vertexClass);
	}

	/**
	 * isSuperClassOfOrEquals()
	 * 
	 * TEST CASE: The other element is a direct subclass of this element
	 */
	@Test
	public void testIsSuperClassOfOrEquals() {
		VertexClass subClass = graphClass.createVertexClass("Subclass");
		subClass.addSuperClass(vertexClass);

		testIsSuperClassOfOrEquals(subClass);
	}

	/**
	 * isSuperClassOfOrEquals()
	 * 
	 * TEST CASE: The other element is an indirect subclass of this element
	 */
	@Test
	public void testIsSuperClassOfOrEquals2() {
		VertexClass subClass = graphClass.createVertexClass("SubClass");
		VertexClass subClass2 = graphClass.createVertexClass("SubClass2");
		subClass.addSuperClass(vertexClass);
		subClass2.addSuperClass(subClass);

		testIsSuperClassOfOrEquals(subClass2);
	}

	/**
	 * isSuperClassOfOrEquals()
	 * 
	 * TEST CASE: The other element and this element are the same
	 */
	@Test
	public void testIsSuperClassOfOrEquals3() {
		testIsSuperClassOfOrEquals(vertexClass);
	}

	/**
	 * isSuperClassOfOrEquals()
	 * 
	 * TEST CASE: The other element has no relation with this element
	 */
	@Test
	public void testIsSuperClassOfOrEquals4() {
		VertexClass vertexClass2 = graphClass.createVertexClass("VertexClass2");

		testIsSuperClassOfOrEquals2(vertexClass2);
	}

	/**
	 * isSuperClassOfOrEquals()
	 * 
	 * TEST CASE: The other element is a superclass of this element
	 */
	@Test
	public void testIsSuperClassOfOrEquals5() {
		VertexClass superClass = graphClass.createVertexClass("SuperClass");
		vertexClass.addSuperClass(superClass);

		testIsSuperClassOfOrEquals2(superClass);
	}
}
