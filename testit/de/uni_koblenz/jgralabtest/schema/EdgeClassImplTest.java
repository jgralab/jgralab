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

import de.uni_koblenz.jgralab.schema.AggregationKind;
import de.uni_koblenz.jgralab.schema.AttributedElementClass;
import de.uni_koblenz.jgralab.schema.EdgeClass;
import de.uni_koblenz.jgralab.schema.GraphClass;
import de.uni_koblenz.jgralab.schema.Schema;
import de.uni_koblenz.jgralab.schema.VertexClass;
import de.uni_koblenz.jgralab.schema.impl.SchemaImpl;

public class EdgeClassImplTest extends GraphElementClassImplTest {

	private EdgeClass edgeClass;
	private VertexClass sourceVertexClass, targetVertexClass;

	@Before
	@Override
	public void setUp() {
		super.setUp();

		sourceVertexClass = graphClass
				.createVertexClass("EdgeClassFromVertexClass");
		targetVertexClass = graphClass
				.createVertexClass("EdgeClassToVertexClass");

		attributedElement = edgeClass = graphClass.createEdgeClass(
				"EdgeClass1", sourceVertexClass, 0, random.nextInt(100),
				"EdgeClassFromRoleName", AggregationKind.NONE,
				targetVertexClass, 0, random.nextInt(100),
				"EdgeClassToRoleName", AggregationKind.NONE);
	}

	/**
	 * addAttribute(Attribute)
	 * 
	 * TEST CASE: Adding an attribute, already contained in a superclass of this
	 * element
	 */
	@Test
	public void testAddAttribute4() {
		// Normale Kante
		// TODO possible error? two times targetVertexClass
		EdgeClass superClass = createEdgeClass("EdgeClassSuperClass", edgeClass);

		edgeClass.addSuperClass(superClass);

		testAddAttribute4(superClass);
	}

	private EdgeClass createEdgeClass(String name, EdgeClass addaptedEdgeClass) {
		return createEdgeClass(graphClass, name, addaptedEdgeClass,
				addaptedEdgeClass.getFrom().getVertexClass(), addaptedEdgeClass
						.getTo().getVertexClass());
	}

	private EdgeClass createEdgeClass(String name, EdgeClass addaptedEdgeClass,
			VertexClass from, VertexClass to) {
		return createEdgeClass(graphClass, name, addaptedEdgeClass, from, to);
	}

	private EdgeClass createEdgeClass(GraphClass graph, String name,
			EdgeClass addaptedEdgeClass, VertexClass from, VertexClass to) {
		return graph.createEdgeClass(name, from, edgeClass.getFrom().getMin(),
				addaptedEdgeClass.getFrom().getMax(), "", AggregationKind.NONE,
				to, edgeClass.getTo().getMin(), addaptedEdgeClass.getTo()
						.getMax(), "", AggregationKind.NONE);
	}

	/**
	 * addAttribute(Attribute)
	 * 
	 * TEST CASE: Adding an attribute, already contained in a subclass of this
	 * element
	 */
	@Test
	public void testAddAttribute5() {
		// Beispiel Kante
		EdgeClass subClass = createEdgeClass("EdgeClassSubClass", edgeClass);

		subClass.addSuperClass(edgeClass);

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
		// Normale Kante
		EdgeClass superClass = createEdgeClass("EdgeClassSuperClass", edgeClass);

		edgeClass.addSuperClass(superClass);

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
		// Beispiel Kante
		EdgeClass subClass = createEdgeClass("EdgeClassSubClass", edgeClass);

		subClass.addSuperClass(edgeClass);

		testAddConstraint5(subClass);
	}

	/**
	 * compareTo(AttributedElementClass)
	 * 
	 * TEST CASE: Comparing this element to another, where this element´s
	 * qualified name is lexicographically less than the other´s
	 */
	@Test
	public void testCompareTo() {
		// Normale Kante
		EdgeClass other = createEdgeClass("Z", edgeClass);

		testCompareTo(other);
	}

	/**
	 * compareTo(AttributedElementClass)
	 * 
	 * TEST CASE: Comparing this element to another, where this element´s
	 * qualified name is lexicographically greater than the other´s
	 */
	@Test
	public void testCompareTo2() {
		// Normale Kante
		EdgeClass other = createEdgeClass("A", edgeClass);

		testCompareTo2(other);
	}

	/**
	 * compareTo(AttributedElementClass)
	 * 
	 * TEST CASE: Comparing this element to another, where both element´s
	 * qualified names are equal
	 */
	@Test
	public void testCompareTo3() {
		// SPEZIAL
		Schema schema2 = new SchemaImpl("TestSchema2",
				"de.uni_koblenz.jgralabtest.schematest");
		GraphClass graphClass2 = schema2.createGraphClass(graphClass
				.getSimpleName());
		VertexClass edgeClassFromVertexClass2 = graphClass2
				.createVertexClass("EdgeClassFromVertexClass");
		VertexClass edgeClassToVertexClass2 = graphClass2
				.createVertexClass("EdgeClassToVertexClass");
		EdgeClass other = createEdgeClass(graphClass2, edgeClass
				.getQualifiedName(), edgeClass, edgeClassFromVertexClass2,
				edgeClassToVertexClass2);

		testCompareTo3(other);
	}

	/**
	 * compareTo(AttributedElementClass)
	 * 
	 * TEST CASE: Comparing an element to itself
	 */
	@Test
	public void testCompareTo4() {
		testCompareTo3(edgeClass);
	}

	/*
	 * TODO: Compare edge class without aggregation ends with one with
	 * aggregation ends
	 */

	/**
	 * containsAttribute(String)
	 * 
	 * TEST CASE: looking for an attribute, present in a superclass of this
	 * element
	 */
	@Test
	public void testContainsAttribute3() {
		// Normale Kante
		EdgeClass superClass = createEdgeClass("EdgeClassSuperClass", edgeClass);

		edgeClass.addSuperClass(superClass);

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
		// TODO: This kind of creation of an edge class is not allowed
		// Beispiel Kante
		EdgeClass subClass = createEdgeClass("EdgeClassSubClass", edgeClass);

		subClass.addSuperClass(edgeClass);

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

		// Beispiel kante
		EdgeClass subClass = createEdgeClass("EdgeClassSubClass", edgeClass);

		// Beispiel kante
		EdgeClass subClass2 = createEdgeClass("EdgeClassSubClass2", edgeClass);

		subClass.addSuperClass(edgeClass);
		subClass2.addSuperClass(edgeClass);

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

		// Beispiel kante
		EdgeClass subClass = createEdgeClass("EdgeClassSubClass", edgeClass);
		// Beispiel kante
		EdgeClass subClass2 = createEdgeClass("EdgeClassSubClass2", edgeClass);

		subClass.addSuperClass(edgeClass);
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

		// Normale Kante
		EdgeClass superClass = createEdgeClass("EdgeClassSuperClass",
				edgeClass, sourceVertexClass, targetVertexClass);

		edgeClass.addSuperClass(superClass);

		expectedSuperClasses.add(schema.getDefaultEdgeClass());
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

		// Normale Kanten
		EdgeClass superClass = createEdgeClass("EdgeClassSuperClass", edgeClass);
		EdgeClass superClass2 = createEdgeClass("EdgeClassSuperClass2",
				edgeClass);

		edgeClass.addSuperClass(superClass);
		edgeClass.addSuperClass(superClass2);

		expectedSuperClasses.add(schema.getDefaultEdgeClass());
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

		// Normale Kanten
		EdgeClass superClass = createEdgeClass("VertexClassSuperClass",
				edgeClass);
		EdgeClass superClass2 = createEdgeClass("VertexClassSuperClass2",
				edgeClass);

		edgeClass.addSuperClass(superClass);
		superClass.addSuperClass(superClass2);

		expectedSuperClasses.add(schema.getDefaultEdgeClass());
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

		expectedSuperClasses.add(schema.getDefaultEdgeClass());

		testGetAllSuperClasses(expectedSuperClasses);
	}

	/**
	 * getAttribute()
	 * 
	 * TEST CASE: Getting an inherited attribute
	 */
	@Test
	public void testGetAttribute2() {

		// Normale Kante
		EdgeClass superClass = createEdgeClass("EdgeClassSuperClass", edgeClass);

		edgeClass.addSuperClass(superClass);

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
		// Beispiel kante
		EdgeClass subClass = createEdgeClass("EdgeClassSubClass", edgeClass);

		subClass.addSuperClass(edgeClass);

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

		// NOrmale Kante
		EdgeClass superClass = createEdgeClass("EdgeClassSuperClass", edgeClass);

		edgeClass.addSuperClass(superClass);

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

		// Normale Kante
		EdgeClass superClass = createEdgeClass("EdgeClassSuperClass", edgeClass);

		edgeClass.addSuperClass(superClass);

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

		// Beispiel kante
		EdgeClass subClass = createEdgeClass("EdgeClassSubClass", edgeClass);

		subClass.addSuperClass(edgeClass);

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

		// Normale Kante
		EdgeClass superClass = createEdgeClass("EdgeClassSuperClass", edgeClass);

		edgeClass.addSuperClass(superClass);

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

		// Normale Kante
		EdgeClass superClass = createEdgeClass("EdgeClassSuperClass", edgeClass);

		edgeClass.addSuperClass(superClass);

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

		// Normale Kante
		EdgeClass subClass = createEdgeClass("EdgeClassSubClass", edgeClass);

		subClass.addSuperClass(edgeClass);

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

		// Normale Kante
		EdgeClass superClass = createEdgeClass("EdgeClassSuperClass", edgeClass);

		edgeClass.addSuperClass(superClass);

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

		// Beispiel kante
		Vector<AttributedElementClass> expectedSubClasses = new Vector<AttributedElementClass>();

		EdgeClass subClass = createEdgeClass("EdgeClassSubClass", edgeClass);

		subClass.addSuperClass(edgeClass);

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

		// Beispiel kante
		EdgeClass subClass2 = createEdgeClass("EdgeClassSubClass2", edgeClass);

		// Beispiel kante
		EdgeClass subClass = createEdgeClass("EdgeClassSubClass", edgeClass);

		subClass.addSuperClass(edgeClass);
		subClass2.addSuperClass(edgeClass);

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

		// Beispiel kante
		EdgeClass subClass = createEdgeClass("EdgeClassSubClass", edgeClass);

		// Beispiel kante
		// subclass
		EdgeClass subClass2 = createEdgeClass("EdgeClassSubClass2", edgeClass);

		// subclass

		subClass.addSuperClass(edgeClass);
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

		// Normale Kante
		EdgeClass superClass = createEdgeClass("EdgeClassSuperClass", edgeClass);

		edgeClass.addSuperClass(superClass);

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

		// Normale Kanten
		EdgeClass superClass = createEdgeClass("EdgeClassSuperClass", edgeClass);
		EdgeClass superClass2 = createEdgeClass("EdgeClassSuperClass2",
				edgeClass);

		edgeClass.addSuperClass(superClass);
		edgeClass.addSuperClass(superClass2);

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

		// Normale Kanten
		EdgeClass superClass = createEdgeClass("EdgeClassSuperClass", edgeClass); // Direct
		// superclass
		EdgeClass superClass2 = createEdgeClass("EdgeClassSuperClass2",
				edgeClass); // Indirect
		// superclass

		edgeClass.addSuperClass(superClass);
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

		expectedSuperClasses.add(schema.getDefaultEdgeClass());

		testGetDirectSuperClasses(expectedSuperClasses);
	}

	@Test
	public void testAddSuperClass() {
		// TODO Auto-generated method stub
	}

	@Test
	public void testCheckConnectionRestrictions() {
		// TODO Auto-generated method stub
	}

	@Test
	public void testGetFrom() {
		// TODO Auto-generated method stub
	}

	@Test
	public void testGetFromMax() {
		// TODO Auto-generated method stub
	}

	@Test
	public void testGetFromMin() {
		// TODO Auto-generated method stub
	}

	@Test
	public void testGetFromRolename() {
		// TODO Auto-generated method stub
	}

	@Test
	public void testGetInEdgeClass() {
		// TODO Auto-generated method stub
	}

	@Override
	@Test
	public void testGetM1Class() {
		// TODO Auto-generated method stub
	}

	@Test
	public void testGetOutEdgeClass() {
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
		// Normale Kante
		EdgeClass superClass = createEdgeClass("EdgeClassSuperClass", edgeClass);
		edgeClass.addSuperClass(superClass);

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

		// Beispiel kante
		EdgeClass subClass = createEdgeClass("EdgeClassSubClass", edgeClass);

		subClass.addSuperClass(edgeClass);

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

		// Normale Kante
		EdgeClass superClass = createEdgeClass("EdgeClassSuperClass", edgeClass);
		edgeClass.addSuperClass(superClass);

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

		// Normale Kante
		EdgeClass superClass = createEdgeClass("EdgeClassSuperClass", edgeClass);
		edgeClass.addSuperClass(superClass);

		testGetOwnAttributeList4(superClass);
	}

	@Test
	public void testGetRedefinedFromRoles() {
		// TODO Auto-generated method stub
	}

	@Test
	public void testGetRedefinedToRoles() {
		// TODO Auto-generated method stub
	}

	@Test
	public void testGetTo() {
		// TODO Auto-generated method stub
	}

	@Test
	public void testGetToMax() {
		// TODO Auto-generated method stub
	}

	@Test
	public void testGetToMin() {
		// TODO Auto-generated method stub
	}

	@Test
	public void testGetToRolename() {
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

		// Normale Kante
		EdgeClass superClass = createEdgeClass("EdgeClassSuperClass", edgeClass);
		edgeClass.addSuperClass(superClass);

		testHasAttributes3(superClass);
	}

	/**
	 * hasAttributes()
	 * 
	 * TEST CASE: The element has multiple inherited attributes
	 */
	@Test
	public void testHasAttributes4() {

		// Normale Kante
		EdgeClass superClass = createEdgeClass("EdgeClassSuperClass", edgeClass);
		edgeClass.addSuperClass(superClass);

		testHasAttributes4(superClass);
	}

	/**
	 * hasAttributes()
	 * 
	 * TEST CASE: The element has multiple direct and indirect attributes
	 */
	@Test
	public void testHasAttributes5() {

		// Normale Kante
		EdgeClass superClass = createEdgeClass("EdgeClassSuperClass", edgeClass);
		edgeClass.addSuperClass(superClass);

		testHasAttributes5(superClass);
	}

	/**
	 * hasOwnAttributes()
	 * 
	 * TEST CASE: The element has direct and inherited attributes
	 */
	@Test
	public void testHasOwnAttributes4() {

		// Normale Kante
		EdgeClass superClass = createEdgeClass("EdgeClassSuperClass", edgeClass);
		edgeClass.addSuperClass(superClass);

		testHasOwnAttributes4(superClass);
	}

	/**
	 * hasOwnAttributes()
	 * 
	 * TEST CASE: The element has no direct but indirect attributes
	 */
	@Test
	public void testHasOwnAttributes5() {

		// Normale Kante
		EdgeClass superClass = createEdgeClass("EdgeClassSuperClass", edgeClass);
		edgeClass.addSuperClass(superClass);

		testHasOwnAttributes5(superClass);
	}

	/**
	 * isDirectSubClassOf()
	 * 
	 * TEST CASE: The other element is a direct superclass of this element
	 */
	@Test
	public void testIsDirectSubClassOf() {

		// Normale Kante
		EdgeClass superClass = createEdgeClass("EdgeClassSuperClass", edgeClass);
		edgeClass.addSuperClass(superClass);

		testIsDirectSubClassOf(superClass);
	}

	/**
	 * isDirectSubClassOf()
	 * 
	 * TEST CASE: The other element is an inherited superclass of this element
	 */
	@Test
	public void testIsDirectSubClassOf2() {

		// Normale Kanten
		EdgeClass superClass = createEdgeClass("EdgeClassSuperClass", edgeClass);
		EdgeClass superClass2 = createEdgeClass("EdgeClassSuperClass2",
				edgeClass);
		edgeClass.addSuperClass(superClass);
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

		// Beispiel Kante
		EdgeClass subClass = createEdgeClass("EdgeClassSubClass", edgeClass);

		subClass.addSuperClass(edgeClass);

		testIsDirectSubClassOf2(subClass);
	}

	/**
	 * isDirectSubClassOf()
	 * 
	 * TEST CASE: The other element has no relation with this element
	 */
	@Test
	public void testIsDirectSubClassOf4() {

		// Normale Kante
		EdgeClass edgeClass2 = createEdgeClass("EdgeClass2", edgeClass);

		testIsDirectSubClassOf2(edgeClass2);
	}

	/**
	 * isDirectSubClassOf()
	 * 
	 * TEST CASE: The other element and this element are the same
	 */
	@Test
	public void testIsDirectSubClassOf5() {
		testIsDirectSubClassOf2(edgeClass);
	}

	/**
	 * isDirectSuperClassOf()
	 * 
	 * TEST CASE: The other element is a direct subclass of this element
	 */
	@Test
	public void testIsDirectSuperClassOf() {

		// Beispiel Kante
		EdgeClass subClass = createEdgeClass("EdgeClassSubClass", edgeClass);

		subClass.addSuperClass(edgeClass);

		testIsDirectSuperClassOf(subClass);
	}

	/**
	 * isDirectSuperClassOf()
	 * 
	 * TEST CASE: The other element is an inherited subclass of this element
	 */
	@Test
	public void testIsDirectSuperClassOf2() {

		// Beispiel Kante
		EdgeClass subClass = createEdgeClass("EdgeClassSubClass", edgeClass);

		EdgeClass subClass2 = createEdgeClass("EdgeClassSubClass2", edgeClass);

		subClass.addSuperClass(edgeClass);
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

		// Normale Kante
		EdgeClass superClass = createEdgeClass("EdgeClassSuperClass", edgeClass);
		edgeClass.addSuperClass(superClass);

		testIsDirectSuperClassOf2(superClass);
	}

	/**
	 * isDirectSuperClassOf()
	 * 
	 * TEST CASE: The other element has no relation with this element
	 */
	@Test
	public void testIsDirectSuperClassOf4() {

		// Normale Kante
		EdgeClass edgeClass2 = createEdgeClass("EdgeClass2", edgeClass);

		testIsDirectSuperClassOf2(edgeClass2);
	}

	/**
	 * isDirectSuperClassOf()
	 * 
	 * TEST CASE: The other element and this element are the same
	 */
	@Test
	public void testIsDirectSuperClassOf5() {
		testIsDirectSuperClassOf2(edgeClass);
	}

	/**
	 * isSubClassOf()
	 * 
	 * TEST CASE: The other element is a direct superclass of this element
	 */
	@Test
	public void testIsSubClassOf() {

		// Normale Kante
		EdgeClass superClass = createEdgeClass("EdgeClassSuperClass", edgeClass);
		edgeClass.addSuperClass(superClass);

		testIsSubClassOf(superClass);
	}

	/**
	 * isSubClassOf()
	 * 
	 * TEST CASE: The other element is an inherited superclass of this element
	 */
	@Test
	public void testIsSubClassOf2() {

		// Normale Kanten
		EdgeClass superClass = createEdgeClass("EdgeClassSuperClass", edgeClass);
		EdgeClass superClass2 = createEdgeClass("EdgeClassSuperClass2",
				edgeClass);
		edgeClass.addSuperClass(superClass);
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

		// Beispiel Kante
		EdgeClass subClass = createEdgeClass("EdgeClassSubClass", edgeClass);

		subClass.addSuperClass(edgeClass);

		testIsSubClassOf2(subClass);
	}

	/**
	 * isSubClassOf()
	 * 
	 * TEST CASE: The other element has no relation with this element
	 */
	@Test
	public void testIsSubClassOf4() {

		// Normale Kante
		EdgeClass edgeClass2 = createEdgeClass("EdgeClass2", edgeClass);

		testIsSubClassOf2(edgeClass2);
	}

	/**
	 * isSubClassOf()
	 * 
	 * TEST CASE: The other element and this element are the same
	 */
	@Test
	public void testIsSubClassOf5() {
		testIsSubClassOf2(edgeClass);
	}

	/**
	 * isSuperClassOf()
	 * 
	 * TEST CASE: The other element is a direct subclass of this element
	 */
	@Test
	public void testIsSuperClassOf() {

		// Beispiel Kante
		EdgeClass subClass = createEdgeClass("EdgeClassSubClass", edgeClass);

		subClass.addSuperClass(edgeClass);

		testIsSuperClassOf(subClass);
	}

	/**
	 * isSuperClassOf()
	 * 
	 * TEST CASE: The other element is an inherited subclass of this element
	 */
	@Test
	public void testIsSuperClassOf2() {

		// Beispiel Kanten
		EdgeClass subClass = createEdgeClass("EdgeClassSubClass", edgeClass);

		EdgeClass subClass2 = createEdgeClass("EdgeClassSubClass2", edgeClass);

		subClass.addSuperClass(edgeClass);
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

		// Normale Kante
		EdgeClass superClass = createEdgeClass("EdgeClassSuperClass", edgeClass);
		edgeClass.addSuperClass(superClass);

		testIsSuperClassOf2(superClass);
	}

	/**
	 * isSuperClassOf()
	 * 
	 * TEST CASE: The other element has no relation with this element
	 */
	@Test
	public void testIsSuperClassOf4() {

		// Normale Kante
		EdgeClass edgeClass2 = createEdgeClass("EdgeClass2", edgeClass);

		testIsSuperClassOf2(edgeClass2);
	}

	/**
	 * isSuperClassOf()
	 * 
	 * TEST CASE: The other element and this element are the same
	 */
	@Test
	public void testIsSuperClassOf5() {
		testIsSuperClassOf2(edgeClass);
	}

	/**
	 * isSuperClassOfOrEquals()
	 * 
	 * TEST CASE: The other element is a direct subclass of this element
	 */
	@Test
	public void testIsSuperClassOfOrEquals() {

		// Beispiel Kante
		EdgeClass subClass = createEdgeClass("EdgeClassSubClass", edgeClass);

		subClass.addSuperClass(edgeClass);

		testIsSuperClassOfOrEquals(subClass);
	}

	/**
	 * isSuperClassOfOrEquals()
	 * 
	 * TEST CASE: The other element is an inherited subclass of this element
	 */
	@Test
	public void testIsSuperClassOfOrEquals2() {

		// Beispiel Kanten
		EdgeClass subClass = createEdgeClass("EdgeClassSubClass", edgeClass);

		EdgeClass subClass2 = createEdgeClass("EdgeClassSubClass2", edgeClass);

		subClass.addSuperClass(edgeClass);
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
		testIsSuperClassOfOrEquals(edgeClass);
	}

	/**
	 * isSuperClassOfOrEquals()
	 * 
	 * TEST CASE: The other element has no relation with this element
	 */
	@Test
	public void testIsSuperClassOfOrEquals4() {

		// Normale Kante
		EdgeClass edgeClass2 = createEdgeClass("EdgeClass2", edgeClass);

		testIsSuperClassOfOrEquals2(edgeClass2);
	}

	/**
	 * isSuperClassOfOrEquals()
	 * 
	 * TEST CASE: The other element is a superclass of this element
	 */
	@Test
	public void testIsSuperClassOfOrEquals5() {
		EdgeClass superClass = createEdgeClass("EdgeClassSuperClass", edgeClass);
		edgeClass.addSuperClass(superClass);

		testIsSuperClassOfOrEquals2(superClass);
	}

	@Test
	public void testMergeConnectionCardinalities() {
		// TODO Auto-generated method stub
	}

	@Test
	public void testMergeConnectionVertexClasses() {
		// TODO Auto-generated method stub
	}

	@Test
	public void testRedefineFromRole() {
		// TODO Auto-generated method stub
	}

	@Test
	public void testRedefineToRole() {
		// TODO Auto-generated method stub
	}

}
