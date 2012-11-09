/*
 * JGraLab - The Java Graph Laboratory
 *
 * Copyright (C) 2006-2012 Institute for Software Technology
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

import java.util.Vector;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import de.uni_koblenz.jgralab.schema.AggregationKind;
import de.uni_koblenz.jgralab.schema.EdgeClass;
import de.uni_koblenz.jgralab.schema.GraphClass;
import de.uni_koblenz.jgralab.schema.Schema;
import de.uni_koblenz.jgralab.schema.VertexClass;
import de.uni_koblenz.jgralab.schema.exception.SchemaException;
import de.uni_koblenz.jgralab.schema.impl.SchemaImpl;

public class EdgeClassImplTest extends GraphElementClassImplTest<EdgeClass> {

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
		EdgeClass other = createEdgeClass(graphClass2,
				edgeClass.getQualifiedName(), edgeClass,
				edgeClassFromVertexClass2, edgeClassToVertexClass2);

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
	 * getAllSubClasses()
	 * 
	 * TEST CASE: Getting all subclasses of an element with one direct subclass
	 */
	@Test
	public void testGetAllSubClasses() {
		Vector<EdgeClass> expectedSubClasses = new Vector<EdgeClass>();
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
		Vector<EdgeClass> expectedSubClasses = new Vector<EdgeClass>();

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
		Vector<EdgeClass> expectedSubClasses = new Vector<EdgeClass>();

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
		testGetAllSubClasses(new Vector<EdgeClass>());
	}

	/**
	 * getAllSuperClasses()
	 * 
	 * TEST CASE: Getting all superclasses of an element with one direct
	 * superclass
	 */
	@Test
	public void testGetAllSuperClasses() {
		Vector<EdgeClass> expectedSuperClasses = new Vector<EdgeClass>();

		// Normale Kante
		EdgeClass superClass = createEdgeClass("EdgeClassSuperClass",
				edgeClass, sourceVertexClass, targetVertexClass);

		edgeClass.addSuperClass(superClass);

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
		Vector<EdgeClass> expectedSuperClasses = new Vector<EdgeClass>();

		// Normale Kanten
		EdgeClass superClass = createEdgeClass("EdgeClassSuperClass", edgeClass);
		EdgeClass superClass2 = createEdgeClass("EdgeClassSuperClass2",
				edgeClass);

		edgeClass.addSuperClass(superClass);
		edgeClass.addSuperClass(superClass2);

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
		Vector<EdgeClass> expectedSuperClasses = new Vector<EdgeClass>();

		// Normale Kanten
		EdgeClass superClass = createEdgeClass("VertexClassSuperClass",
				edgeClass);
		EdgeClass superClass2 = createEdgeClass("VertexClassSuperClass2",
				edgeClass);

		edgeClass.addSuperClass(superClass);
		superClass.addSuperClass(superClass2);

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
		Vector<EdgeClass> expectedSuperClasses = new Vector<EdgeClass>();

		testGetAllSuperClasses(expectedSuperClasses);
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
		Vector<EdgeClass> expectedSubClasses = new Vector<EdgeClass>();

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
		Vector<EdgeClass> expectedSubClasses = new Vector<EdgeClass>();

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
		Vector<EdgeClass> expectedSubClasses = new Vector<EdgeClass>();

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
		testGetDirectSubClasses(new Vector<EdgeClass>());
	}

	/**
	 * getDirectSuperClasses()
	 * 
	 * TEST CASE: Getting all direct superclasses of an element that has one
	 * direct superclass.
	 */
	@Test
	public void testGetDirectSuperClasses() {
		Vector<EdgeClass> expectedSuperClasses = new Vector<EdgeClass>();

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
		Vector<EdgeClass> expectedSuperClasses = new Vector<EdgeClass>();

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
		Vector<EdgeClass> expectedSuperClasses = new Vector<EdgeClass>();

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
		Vector<EdgeClass> expectedSuperClasses = new Vector<EdgeClass>();

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
	public void testGetSchemaClass() {
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
	 * isSubClassOf()
	 * 
	 * TEST CASE: The other element is a direct superclass of this element
	 * 
	 * TEST CASE: The other element is an inherited superclass of this element
	 */
	public final void testIsSubClassOf(EdgeClass other) {
		Assert.assertTrue(attributedElement.isSubClassOf(other));
	}

	/**
	 * isSubClassOf()
	 * 
	 * TEST CASE: The other element is a subclass of this element
	 * 
	 * TEST CASE: The other element has no relation with this element
	 * 
	 * TEST CASE: The other element and this element are the same
	 */
	public final void testIsSubClassOf2(EdgeClass other) {
		Assert.assertFalse(attributedElement.isSubClassOf(other));
	}

	/*
	 * Tests for the isSuperClassOf() method.
	 */
	/**
	 * isSuperClassOf()
	 * 
	 * TEST CASE: The other element is a direct subclass of this element
	 * 
	 * TEST CASE: The other element is an inherited subclass of this element
	 */
	public final void testIsSuperClassOf(EdgeClass other) {
		Assert.assertTrue(attributedElement.isSuperClassOf(other));
	}

	/**
	 * isDirectSuperClassOf()
	 * 
	 * TEST CASE: The other element is a superclass of this element
	 * 
	 * TEST CASE: The other element has no relation with this element
	 * 
	 * TEST CASE: The other element and this element are the same
	 */
	public final void testIsSuperClassOf2(EdgeClass other) {
		Assert.assertFalse(attributedElement.isSuperClassOf(other));
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

	@Test
	public void testMergeConnectionCardinalities() {
		// TODO Auto-generated method stub
	}

	@Test
	public void testMergeConnectionVertexClasses() {
		// TODO Auto-generated method stub
	}

	@Test(expected = SchemaException.class)
	public void testECsFromOrToVertexAreForbiddenSrc() {
		graphClass.createEdgeClass("HasToDoWith",
				graphClass.getDefaultVertexClass(), 0, 1, "",
				AggregationKind.NONE, targetVertexClass, 0, 1, "",
				AggregationKind.NONE);
	}

	@Test(expected = SchemaException.class)
	public void testECsFromOrToVertexAreForbiddenTrg() {
		graphClass.createEdgeClass("HasToDoWith", sourceVertexClass, 0, 1, "",
				AggregationKind.NONE, graphClass.getDefaultVertexClass(), 0, 1,
				"", AggregationKind.NONE);
	}

	@Test(expected = SchemaException.class)
	public void testECsFromOrToVertexAreForbiddenSrcAndTrg() {
		graphClass.createEdgeClass("HasToDoWith",
				graphClass.getDefaultVertexClass(), 0, 1, "",
				AggregationKind.NONE, graphClass.getDefaultVertexClass(), 0, 1,
				"", AggregationKind.NONE);
	}
}
