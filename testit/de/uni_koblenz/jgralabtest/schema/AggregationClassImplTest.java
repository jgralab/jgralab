package de.uni_koblenz.jgralabtest.schema;

import java.util.Vector;

import org.junit.Before;
import org.junit.Test;

import de.uni_koblenz.jgralab.schema.AggregationClass;
import de.uni_koblenz.jgralab.schema.AttributedElementClass;
import de.uni_koblenz.jgralab.schema.GraphClass;
import de.uni_koblenz.jgralab.schema.Schema;
import de.uni_koblenz.jgralab.schema.VertexClass;
import de.uni_koblenz.jgralab.schema.impl.SchemaImpl;

public class AggregationClassImplTest extends EdgeClassImplTest {

	private AggregationClass aggregationClass;
	private VertexClass aggregationClassFromVertexClass,
			aggregationClassToVertexClass;

	@Before
	@Override
	public void setUp() {
		super.setUp();

		aggregationClassFromVertexClass = graphClass
				.createVertexClass("AggregationClassFromVertexClass");
		aggregationClassToVertexClass = graphClass
				.createVertexClass("AggregationClassToVertexClass");

		attributedElement = aggregationClass = graphClass
				.createAggregationClass("AggregationClass1",
						aggregationClassFromVertexClass, 0, (int) (Math
								.random() * 100) + 1,
						"AggregationClassFromRoleName", true,
						aggregationClassToVertexClass, 0,
						(int) (Math.random() * 100) + 1,
						"AggregationClassToRoleName");
	}

	/**
	 * addAttribute(Attribute)
	 * 
	 * TEST CASE: Adding an attribute, already contained in a superclass of this
	 * element
	 */
	@Test
	@Override
	public void testAddAttribute4() {
		AggregationClass superClass = graphClass.createAggregationClass(
				"AggregationClassSuperClass", aggregationClassFromVertexClass,
				true, aggregationClassToVertexClass);

		aggregationClass.addSuperClass(superClass);

		testAddAttribute4(superClass);
	}

	/**
	 * addAttribute(Attribute)
	 * 
	 * TEST CASE: Adding an attribute, already contained in a subclass of this
	 * element
	 */
	@Test
	@Override
	public void testAddAttribute5() {
		AggregationClass subClass = graphClass.createAggregationClass(
				"AggregationClassSubClass", aggregationClassFromVertexClass, aggregationClass.getFromMin(), aggregationClass.getFromMax(),
				true, aggregationClassToVertexClass, aggregationClass.getToMin(), aggregationClass.getToMax());

		subClass.addSuperClass(aggregationClass);

		testAddAttribute5(subClass);
	}

	/**
	 * addConstraint(Constraint)
	 * 
	 * TEST CASE: Adding a constraint, already contained in a superclass of this
	 * element
	 */
	@Test
	@Override
	public void testAddConstraint4() {
		AggregationClass superClass = graphClass.createAggregationClass(
				"AggregationClassSuperClass", aggregationClassFromVertexClass,
				true, aggregationClassToVertexClass);

		aggregationClass.addSuperClass(superClass);

		testAddConstraint4(superClass);
	}

	/**
	 * addConstraint(Constraint)
	 * 
	 * TEST CASE: Adding a constraint, already contained in a subclass of this
	 * element
	 */
	@Test
	@Override
	public void testAddConstraint5() {
		AggregationClass subClass = graphClass.createAggregationClass(
				"AggregationClassSubClass", aggregationClassFromVertexClass, aggregationClass.getFromMin(), aggregationClass.getFromMax(),
				true, aggregationClassToVertexClass, aggregationClass.getToMin(), aggregationClass.getToMax());

		subClass.addSuperClass(aggregationClass);

		testAddConstraint5(subClass);
	}

	/**
	 * compareTo(AttributedElementClass)
	 * 
	 * TEST CASE: Comparing this element to another, where this element´s
	 * qualified name is lexicographically less than the other´s
	 */
	@Test
	@Override
	public void testCompareTo() {
		AggregationClass other = graphClass.createAggregationClass("Z",
				aggregationClassFromVertexClass, true,
				aggregationClassToVertexClass);

		testCompareTo(other);
	}

	/**
	 * compareTo(AttributedElementClass)
	 * 
	 * TEST CASE: Comparing this element to another, where this element´s
	 * qualified name is lexicographically greater than the other´s
	 */
	@Test
	@Override
	public void testCompareTo2() {
		AggregationClass other = graphClass.createAggregationClass("A",
				aggregationClassFromVertexClass, true,
				aggregationClassToVertexClass);

		testCompareTo2(other);
	}

	/**
	 * compareTo(AttributedElementClass)
	 * 
	 * TEST CASE: Comparing this element to another, where both element´s
	 * qualified names are equal
	 */
	@Test
	@Override
	public void testCompareTo3() {
		Schema schema2 = new SchemaImpl("TestSchema2",
				"de.uni_koblenz.jgralabtest.schematest");
		GraphClass graphClass2 = schema2.createGraphClass(graphClass
				.getSimpleName());
		VertexClass aggregationClassFromVertexClass2 = graphClass2
				.createVertexClass("AggregationClassFromVertexClass");
		VertexClass aggregationClassToVertexClass2 = graphClass2
				.createVertexClass("AggregationClassToVertexClass");
		AggregationClass other = graphClass2.createAggregationClass(
				aggregationClass.getQualifiedName(),
				aggregationClassFromVertexClass2, true,
				aggregationClassToVertexClass2);

		testCompareTo3(other);
	}

	/**
	 * compareTo(AttributedElementClass)
	 * 
	 * TEST CASE: Comparing an element to itself
	 */
	@Test
	@Override
	public void testCompareTo4() {
		testCompareTo3(aggregationClass);
	}

	/**
	 * containsAttribute(String)
	 * 
	 * TEST CASE: looking for an attribute, present in a superclass of this
	 * element
	 */
	@Test
	@Override
	public void testContainsAttribute3() {
		AggregationClass superClass = graphClass.createAggregationClass(
				"AggregationClassSuperClass", aggregationClassFromVertexClass,
				true, aggregationClassToVertexClass);

		aggregationClass.addSuperClass(superClass);

		testContainsAttribute3(superClass);
	}

	/**
	 * getAllSubClasses()
	 * 
	 * TEST CASE: Getting all subclasses of an element with one direct subclass
	 */
	@Test
	@Override
	public void testGetAllSubClasses() {
		Vector<AttributedElementClass> expectedSubClasses = new Vector<AttributedElementClass>();

		AggregationClass subClass = graphClass.createAggregationClass(
				"AggregationClassSubClass", aggregationClassFromVertexClass, aggregationClass.getFromMin(), aggregationClass.getFromMax(),
				true, aggregationClassToVertexClass, aggregationClass.getToMin(), aggregationClass.getToMax());

		subClass.addSuperClass(aggregationClass);

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
	@Override
	public void testGetAllSubClasses2() {
		Vector<AttributedElementClass> expectedSubClasses = new Vector<AttributedElementClass>();

		AggregationClass subClass = graphClass.createAggregationClass(
				"AggregationClassSubClass", aggregationClassFromVertexClass, aggregationClass.getFromMin(), aggregationClass.getFromMax(),
				true, aggregationClassToVertexClass, aggregationClass.getToMin(), aggregationClass.getToMax());
		AggregationClass subClass2 = graphClass.createAggregationClass(
				"AggregationClassSubClass2", aggregationClassFromVertexClass, aggregationClass.getFromMin(), aggregationClass.getFromMax(),
				true, aggregationClassToVertexClass, aggregationClass.getToMin(), aggregationClass.getToMax());

		subClass.addSuperClass(aggregationClass);
		subClass2.addSuperClass(aggregationClass);

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
	@Override
	public void testGetAllSubClasses3() {
		Vector<AttributedElementClass> expectedSubClasses = new Vector<AttributedElementClass>();

		AggregationClass subClass = graphClass.createAggregationClass(
				"AggregationClassSubClass", aggregationClassFromVertexClass, aggregationClass.getFromMin(), aggregationClass.getFromMax(),
				true, aggregationClassToVertexClass, aggregationClass.getToMin(), aggregationClass.getToMax());
		AggregationClass subClass2 = graphClass.createAggregationClass(
				"AggregationClassSubClass2", aggregationClassFromVertexClass, aggregationClass.getFromMin(), aggregationClass.getFromMax(),
				true, aggregationClassToVertexClass, aggregationClass.getToMin(), aggregationClass.getToMax());

		subClass.addSuperClass(aggregationClass);
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
	@Override
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
	@Override
	public void testGetAllSuperClasses() {
		Vector<AttributedElementClass> expectedSuperClasses = new Vector<AttributedElementClass>();

		AggregationClass superClass = graphClass.createAggregationClass(
				"AggregationClassSuperClass", aggregationClassFromVertexClass,
				true, aggregationClassToVertexClass);

		aggregationClass.addSuperClass(superClass);

		expectedSuperClasses.add(schema.getDefaultEdgeClass());
		expectedSuperClasses.add(schema.getDefaultAggregationClass());
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
	@Override
	public void testGetAllSuperClasses2() {
		Vector<AttributedElementClass> expectedSuperClasses = new Vector<AttributedElementClass>();

		AggregationClass superClass = graphClass.createAggregationClass(
				"AggregationClassSuperClass", aggregationClassFromVertexClass,
				true, aggregationClassToVertexClass);
		AggregationClass superClass2 = graphClass.createAggregationClass(
				"AggregationClassSuperClass2", aggregationClassFromVertexClass,
				true, aggregationClassToVertexClass);

		aggregationClass.addSuperClass(superClass);
		aggregationClass.addSuperClass(superClass2);

		expectedSuperClasses.add(schema.getDefaultEdgeClass());
		expectedSuperClasses.add(schema.getDefaultAggregationClass());
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
	@Override
	public void testGetAllSuperClasses3() {
		Vector<AttributedElementClass> expectedSuperClasses = new Vector<AttributedElementClass>();

		AggregationClass superClass = graphClass.createAggregationClass(
				"AggregationClassSuperClass", aggregationClassFromVertexClass,
				true, aggregationClassToVertexClass);
		AggregationClass superClass2 = graphClass.createAggregationClass(
				"AggregationClassSuperClass2", aggregationClassFromVertexClass,
				true, aggregationClassToVertexClass);

		aggregationClass.addSuperClass(superClass);
		superClass.addSuperClass(superClass2);

		expectedSuperClasses.add(schema.getDefaultEdgeClass());
		expectedSuperClasses.add(schema.getDefaultAggregationClass());
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
	@Override
	public void testGetAllSuperClasses4() {
		Vector<AttributedElementClass> expectedSuperClasses = new Vector<AttributedElementClass>();

		expectedSuperClasses.add(schema.getDefaultEdgeClass());
		expectedSuperClasses.add(schema.getDefaultAggregationClass());

		testGetAllSuperClasses(expectedSuperClasses);
	}

	/**
	 * getAttribute()
	 * 
	 * TEST CASE: Getting an inherited attribute
	 */
	@Test
	@Override
	public void testGetAttribute2() {
		AggregationClass superClass = graphClass.createAggregationClass(
				"AggregationClassSuperClass", aggregationClassFromVertexClass,
				true, aggregationClassToVertexClass);

		aggregationClass.addSuperClass(superClass);

		testGetAttribute2(superClass);
	}

	/**
	 * getAttribute()
	 * 
	 * TEST CASE: Trying to get an attribute present in a subclass of this
	 * element
	 */
	@Test
	@Override
	public void testGetAttribute5() {
		AggregationClass subClass = graphClass.createAggregationClass(
				"AggregationClassSubClass", aggregationClassFromVertexClass, aggregationClass.getFromMin(), aggregationClass.getFromMax(),
				true, aggregationClassToVertexClass, aggregationClass.getToMin(), aggregationClass.getToMax());

		subClass.addSuperClass(aggregationClass);

		testGetAttribute5(subClass);
	}

	/**
	 * getAttributeCount()
	 * 
	 * TEST CASE: Getting the number of attributes of an element which has
	 * exactly only one inherited attribute and no direct attributes
	 */
	@Test
	@Override
	public void testGetAttributeCount2() {
		AggregationClass superClass = graphClass.createAggregationClass(
				"AggregationClassSuperClass", aggregationClassFromVertexClass,
				true, aggregationClassToVertexClass);

		aggregationClass.addSuperClass(superClass);

		testGetAttributeCount2(superClass);
	}

	/**
	 * getAttributeCount()
	 * 
	 * TEST CASE: Getting the number of attributes of an element which has
	 * multiple direct and indirect attributes
	 */
	@Test
	@Override
	public void testGetAttributeCount3() {
		AggregationClass superClass = graphClass.createAggregationClass(
				"AggregationClassSuperClass", aggregationClassFromVertexClass,
				true, aggregationClassToVertexClass);

		aggregationClass.addSuperClass(superClass);

		testGetAttributeCount3(superClass);
	}

	/**
	 * getAttributeCount()
	 * 
	 * TEST CASE: Getting the number of attributes of an element which has no
	 * direct nor inherited attributes but whose subclass has attributes
	 */
	@Test
	@Override
	public void testGetAttributeCount5() {
		AggregationClass subClass = graphClass.createAggregationClass(
				"AggregationClassSubClass", aggregationClassFromVertexClass, aggregationClass.getFromMin(), aggregationClass.getFromMax(),
				true, aggregationClassToVertexClass, aggregationClass.getToMin(), aggregationClass.getToMax());

		subClass.addSuperClass(aggregationClass);

		testGetAttributeCount5(subClass);
	}

	/**
	 * getAttributeList()
	 * 
	 * TEST CASE: Getting an element´s list of attributes, which has exactly one
	 * inherited attribute and no direct attributes
	 */
	@Test
	@Override
	public void testGetAttributeList2() {
		AggregationClass superClass = graphClass.createAggregationClass(
				"AggregationClassSuperClass", aggregationClassFromVertexClass,
				true, aggregationClassToVertexClass);

		aggregationClass.addSuperClass(superClass);

		testGetAttributeList2(superClass);
	}

	/**
	 * getAttributeList()
	 * 
	 * TEST CASE: Getting an element´s list of attributes, which has mutliple
	 * direct and inherited attributes
	 */
	@Test
	@Override
	public void testGetAttributeList3() {
		AggregationClass superClass = graphClass.createAggregationClass(
				"AggregationClassSuperClass", aggregationClassFromVertexClass,
				true, aggregationClassToVertexClass);

		aggregationClass.addSuperClass(superClass);

		testGetAttributeList3(superClass);
	}

	/**
	 * getAttributeList()
	 * 
	 * TEST CASE: Getting an element´s list of attributes, which has no direct
	 * nor inherited attributes but whose subclass has attributes
	 */
	@Test
	@Override
	public void testGetAttributeList5() {
		AggregationClass subClass = graphClass.createAggregationClass(
				"AggregationClassSubClass", aggregationClassFromVertexClass, aggregationClass.getFromMin(), aggregationClass.getFromMax(),
				true, aggregationClassToVertexClass, aggregationClass.getToMin(), aggregationClass.getToMax());

		subClass.addSuperClass(aggregationClass);

		testGetAttributeList5(subClass);
	}

	/**
	 * getConstraints()
	 * 
	 * TEST CASE: Getting an element´s list of constraints, that has a
	 * superclass with constraints
	 */
	@Override
	@Test
	public void testGetConstraints4() {
		AggregationClass superClass = graphClass.createAggregationClass(
				"AggregationClassSuperClass", aggregationClassFromVertexClass,
				true, aggregationClassToVertexClass);

		aggregationClass.addSuperClass(superClass);

		testGetConstraints4(superClass);
	}

	/**
	 * getDirectSubClasses()
	 * 
	 * TEST CASE: Getting all direct subclasses of an element that has one
	 * direct subclass.
	 */
	@Test
	@Override
	public void testGetDirectSubClasses() {
		Vector<AttributedElementClass> expectedSubClasses = new Vector<AttributedElementClass>();

		AggregationClass subClass = graphClass.createAggregationClass(
				"AggregationClassSubClass", aggregationClassFromVertexClass, aggregationClass.getFromMin(), aggregationClass.getFromMax(),
				true, aggregationClassToVertexClass, aggregationClass.getToMin(), aggregationClass.getToMax());

		subClass.addSuperClass(aggregationClass);

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
	@Override
	public void testGetDirectSubClasses2() {
		Vector<AttributedElementClass> expectedSubClasses = new Vector<AttributedElementClass>();

		AggregationClass subClass = graphClass.createAggregationClass(
				"AggregationClassSubClass", aggregationClassFromVertexClass, aggregationClass.getFromMin(), aggregationClass.getFromMax(),
				true, aggregationClassToVertexClass, aggregationClass.getToMin(), aggregationClass.getToMax());
		AggregationClass subClass2 = graphClass.createAggregationClass(
				"AggregationClassSubClass2", aggregationClassFromVertexClass, aggregationClass.getFromMin(), aggregationClass.getFromMax(),
				true, aggregationClassToVertexClass, aggregationClass.getToMin(), aggregationClass.getToMax());

		subClass.addSuperClass(aggregationClass);
		subClass2.addSuperClass(aggregationClass);

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
	@Override
	public void testGetDirectSubClasses3() {
		Vector<AttributedElementClass> expectedSubClasses = new Vector<AttributedElementClass>();

		AggregationClass subClass = graphClass.createAggregationClass(
				"AggregationClassSubClass", aggregationClassFromVertexClass, aggregationClass.getFromMin(), aggregationClass.getFromMax(),
				true, aggregationClassToVertexClass, aggregationClass.getToMin(), aggregationClass.getToMax());
		AggregationClass subClass2 = graphClass.createAggregationClass(
				"AggregationClassSubClass2", aggregationClassFromVertexClass, aggregationClass.getFromMin(), aggregationClass.getFromMax(),
				true, aggregationClassToVertexClass, aggregationClass.getToMin(), aggregationClass.getToMax());

		subClass.addSuperClass(aggregationClass);
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
	@Override
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
	@Override
	public void testGetDirectSuperClasses() {
		Vector<AttributedElementClass> expectedSuperClasses = new Vector<AttributedElementClass>();

		AggregationClass superClass = graphClass.createAggregationClass(
				"AggregationClassSuperClass", aggregationClassFromVertexClass,
				true, aggregationClassToVertexClass);

		aggregationClass.addSuperClass(superClass);

		expectedSuperClasses.add(superClass);
		expectedSuperClasses.add(schema.getDefaultAggregationClass());

		testGetDirectSuperClasses(expectedSuperClasses);
	}

	/**
	 * getDirectSuperClasses()
	 * 
	 * TEST CASE: Getting all direct superclasses of an element that has
	 * multiple direct superclasses.
	 */
	@Test
	@Override
	public void testGetDirectSuperClasses2() {
		Vector<AttributedElementClass> expectedSuperClasses = new Vector<AttributedElementClass>();

		AggregationClass superClass = graphClass.createAggregationClass(
				"AggregationClassSuperClass", aggregationClassFromVertexClass,
				true, aggregationClassToVertexClass);
		AggregationClass superClass2 = graphClass.createAggregationClass(
				"AggregationClassSuperClass2", aggregationClassFromVertexClass,
				true, aggregationClassToVertexClass);

		aggregationClass.addSuperClass(superClass);
		aggregationClass.addSuperClass(superClass2);

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
	@Override
	public void testGetDirectSuperClasses3() {
		Vector<AttributedElementClass> expectedSuperClasses = new Vector<AttributedElementClass>();

		AggregationClass superClass = graphClass.createAggregationClass(
				"AggregationClassSuperClass", aggregationClassFromVertexClass,
				true, aggregationClassToVertexClass); // Direct superclass
		AggregationClass superClass2 = graphClass.createAggregationClass(
				"AggregationClassSuperClass2", aggregationClassFromVertexClass,
				true, aggregationClassToVertexClass); // Indirect superclass

		aggregationClass.addSuperClass(superClass);
		superClass.addSuperClass(superClass2);

		expectedSuperClasses.add(superClass);
		expectedSuperClasses.add(schema.getDefaultAggregationClass());

		testGetDirectSuperClasses(expectedSuperClasses);
	}

	/**
	 * getDirectSuperClasses()
	 * 
	 * TEST CASE: Getting all direct superclasses of an element that has no
	 * direct superclasses.
	 */
	@Test
	@Override
	public void testGetDirectSuperClasses4() {
		Vector<AttributedElementClass> expectedSuperClasses = new Vector<AttributedElementClass>();

		expectedSuperClasses.add(schema.getDefaultAggregationClass());

		testGetDirectSuperClasses(expectedSuperClasses);
	}

	/**
	 * getOwnAttribute()
	 * 
	 * TEST CASE: Trying to get an attribute present in a superclass of this
	 * element
	 */
	@Test
	@Override
	public void testGetOwnAttribute4() {
		AggregationClass superClass = graphClass.createAggregationClass(
				"AggregationClassSuperClass", aggregationClassFromVertexClass,
				true, aggregationClassToVertexClass);
		aggregationClass.addSuperClass(superClass);

		testGetOwnAttribute4(superClass);
	}

	/**
	 * getOwnAttribute()
	 * 
	 * TEST CASE: Trying to get an attribute present in a subclass of this
	 * element
	 */
	@Test
	@Override
	public void testGetOwnAttribute5() {
		AggregationClass subClass = graphClass.createAggregationClass(
				"AggregationClassSubClass", aggregationClassFromVertexClass, aggregationClass.getFromMin(), aggregationClass.getFromMax(),
				true, aggregationClassToVertexClass, aggregationClass.getToMin(), aggregationClass.getToMax());
		subClass.addSuperClass(aggregationClass);

		testGetOwnAttribute4(subClass);
	}

	/**
	 * getOwnAttributeCount()
	 * 
	 * TEST CASE: Getting the number of attributes of an element that only has
	 * inherited attributes and no direct attributes
	 */
	@Test
	@Override
	public void testGetOwnAttributeCount4() {
		AggregationClass superClass = graphClass.createAggregationClass(
				"AggregationClassSuperClass", aggregationClassFromVertexClass,
				true, aggregationClassToVertexClass);
		aggregationClass.addSuperClass(superClass);

		testGetOwnAttributeCount4(superClass);
	}

	/**
	 * getOwnAttributeList()
	 * 
	 * TEST CASE: Getting an element´s list of attributes, that only has
	 * inherited attributes and no direct attributes
	 */
	@Test
	@Override
	public void testGetOwnAttributeList4() {
		AggregationClass superClass = graphClass.createAggregationClass(
				"AggregationClassSuperClass", aggregationClassFromVertexClass,
				true, aggregationClassToVertexClass);
		aggregationClass.addSuperClass(superClass);

		testGetOwnAttributeList4(superClass);
	}

	/**
	 * hasAttributes()
	 * 
	 * TEST CASE: The element has one inherited attribute
	 */
	@Test
	@Override
	public void testHasAttributes3() {
		AggregationClass superClass = graphClass.createAggregationClass(
				"AggregationClassSuperClass", aggregationClassFromVertexClass,
				true, aggregationClassToVertexClass);
		aggregationClass.addSuperClass(superClass);

		testHasAttributes3(superClass);
	}

	/**
	 * hasAttributes()
	 * 
	 * TEST CASE: The element has multiple inherited attributes
	 */
	@Test
	@Override
	public void testHasAttributes4() {
		AggregationClass superClass = graphClass.createAggregationClass(
				"AggregationClassSuperClass", aggregationClassFromVertexClass,
				true, aggregationClassToVertexClass);
		aggregationClass.addSuperClass(superClass);

		testHasAttributes4(superClass);
	}

	/**
	 * hasAttributes()
	 * 
	 * TEST CASE: The element has multiple direct and indirect attributes
	 */
	@Test
	@Override
	public void testHasAttributes5() {
		AggregationClass superClass = graphClass.createAggregationClass(
				"AggregationClassSuperClass", aggregationClassFromVertexClass,
				true, aggregationClassToVertexClass);
		aggregationClass.addSuperClass(superClass);

		testHasAttributes5(superClass);
	}

	/**
	 * hasOwnAttributes()
	 * 
	 * TEST CASE: The element has direct and inherited attributes
	 */
	@Test
	@Override
	public void testHasOwnAttributes4() {
		AggregationClass superClass = graphClass.createAggregationClass(
				"AggregationClassSuperClass", aggregationClassFromVertexClass,
				true, aggregationClassToVertexClass);
		aggregationClass.addSuperClass(superClass);

		testHasOwnAttributes4(superClass);
	}

	/**
	 * hasOwnAttributes()
	 * 
	 * TEST CASE: The element has no direct but indirect attributes
	 */
	@Test
	@Override
	public void testHasOwnAttributes5() {
		AggregationClass superClass = graphClass.createAggregationClass(
				"AggregationClassSuperClass", aggregationClassFromVertexClass,
				true, aggregationClassToVertexClass);
		aggregationClass.addSuperClass(superClass);

		testHasOwnAttributes5(superClass);
	}

	@Test
	public void testIsAggregateFrom() {
		// TODO Auto-generated method stub
	}

	/**
	 * isDirectSubClassOf()
	 * 
	 * TEST CASE: The other element is a direct superclass of this element
	 */
	@Test
	@Override
	public void testIsDirectSubClassOf() {
		AggregationClass superClass = graphClass.createAggregationClass(
				"AggregationClassSuperClass", aggregationClassFromVertexClass,
				true, aggregationClassToVertexClass);
		aggregationClass.addSuperClass(superClass);

		testIsDirectSubClassOf(superClass);
	}

	/**
	 * isDirectSubClassOf()
	 * 
	 * TEST CASE: The other element is an inherited superclass of this element
	 */
	@Test
	@Override
	public void testIsDirectSubClassOf2() {
		AggregationClass superClass = graphClass.createAggregationClass(
				"AggregationClassSuperClass", aggregationClassFromVertexClass,
				true, aggregationClassToVertexClass);
		AggregationClass superClass2 = graphClass.createAggregationClass(
				"AggregationClassSuperClass2", aggregationClassFromVertexClass,
				true, aggregationClassToVertexClass);
		aggregationClass.addSuperClass(superClass);
		superClass.addSuperClass(superClass2);

		testIsDirectSubClassOf2(superClass2);
	}

	/**
	 * isDirectSubClassOf()
	 * 
	 * TEST CASE: The other element is a subclass of this element
	 */
	@Test
	@Override
	public void testIsDirectSubClassOf3() {
		AggregationClass subClass = graphClass.createAggregationClass(
				"AggregationClassSubClass", aggregationClassFromVertexClass, aggregationClass.getFromMin(), aggregationClass.getFromMax(),
				true, aggregationClassToVertexClass, aggregationClass.getToMin(), aggregationClass.getToMax());
		subClass.addSuperClass(aggregationClass);

		testIsDirectSubClassOf2(subClass);
	}

	/**
	 * isDirectSubClassOf()
	 * 
	 * TEST CASE: The other element has no relation with this element
	 */
	@Test
	@Override
	public void testIsDirectSubClassOf4() {
		AggregationClass aggregationClass2 = graphClass.createAggregationClass(
				"AggregationClass2", aggregationClassFromVertexClass, true,
				aggregationClassToVertexClass);

		testIsDirectSubClassOf2(aggregationClass2);
	}

	/**
	 * isDirectSubClassOf()
	 * 
	 * TEST CASE: The other element and this element are the same
	 */
	@Test
	@Override
	public void testIsDirectSubClassOf5() {
		testIsDirectSubClassOf2(aggregationClass);
	}

	/**
	 * isDirectSuperClassOf()
	 * 
	 * TEST CASE: The other element is a direct subclass of this element
	 */
	@Test
	@Override
	public void testIsDirectSuperClassOf() {
		AggregationClass subClass = graphClass.createAggregationClass(
				"AggregationClassSubClass", aggregationClassFromVertexClass, aggregationClass.getFromMin(), aggregationClass.getFromMax(),
				true, aggregationClassToVertexClass, aggregationClass.getToMin(), aggregationClass.getToMax());
		subClass.addSuperClass(aggregationClass);

		testIsDirectSuperClassOf(subClass);
	}

	/**
	 * isDirectSuperClassOf()
	 * 
	 * TEST CASE: The other element is an inherited subclass of this element
	 */
	@Test
	@Override
	public void testIsDirectSuperClassOf2() {
		AggregationClass subClass = graphClass.createAggregationClass(
				"AggregationClassSubClass", aggregationClassFromVertexClass, aggregationClass.getFromMin(), aggregationClass.getFromMax(),
				true, aggregationClassToVertexClass, aggregationClass.getToMin(), aggregationClass.getToMax());		
		AggregationClass subClass2 = graphClass.createAggregationClass(
					"AggregationClassSubClass2", aggregationClassFromVertexClass, aggregationClass.getFromMin(), aggregationClass.getFromMax(),
					true, aggregationClassToVertexClass, aggregationClass.getToMin(), aggregationClass.getToMax());
		subClass.addSuperClass(aggregationClass);
		subClass2.addSuperClass(subClass);

		testIsDirectSuperClassOf2(subClass2);
	}

	/**
	 * isDirectSuperClassOf()
	 * 
	 * TEST CASE: The other element is a superclass of this element
	 */
	@Test
	@Override
	public void testIsDirectSuperClassOf3() {
		AggregationClass superClass = graphClass.createAggregationClass(
				"AggregationClassSuperClass", aggregationClassFromVertexClass,
				true, aggregationClassToVertexClass);
		aggregationClass.addSuperClass(superClass);

		testIsDirectSuperClassOf2(superClass);
	}

	/**
	 * isDirectSuperClassOf()
	 * 
	 * TEST CASE: The other element has no relation with this element
	 */
	@Test
	@Override
	public void testIsDirectSuperClassOf4() {
		AggregationClass aggregationClass2 = graphClass.createAggregationClass(
				"AggregationClass2", aggregationClassFromVertexClass, true,
				aggregationClassToVertexClass);

		testIsDirectSuperClassOf2(aggregationClass2);
	}

	/**
	 * isDirectSuperClassOf()
	 * 
	 * TEST CASE: The other element and this element are the same
	 */
	@Test
	@Override
	public void testIsDirectSuperClassOf5() {
		testIsDirectSuperClassOf2(aggregationClass);
	}

	/**
	 * isSubClassOf()
	 * 
	 * TEST CASE: The other element is a direct superclass of this element
	 */
	@Test
	@Override
	public void testIsSubClassOf() {
		AggregationClass superClass = graphClass.createAggregationClass(
				"AggregationClassSuperClass", aggregationClassFromVertexClass,
				true, aggregationClassToVertexClass);
		aggregationClass.addSuperClass(superClass);

		testIsSubClassOf(superClass);
	}

	/**
	 * isSubClassOf()
	 * 
	 * TEST CASE: The other element is an inherited superclass of this element
	 */
	@Test
	@Override
	public void testIsSubClassOf2() {
		AggregationClass superClass = graphClass.createAggregationClass(
				"AggregationClassSuperClass", aggregationClassFromVertexClass,
				true, aggregationClassToVertexClass);
		AggregationClass superClass2 = graphClass.createAggregationClass(
				"AggregationClassSuperClass2", aggregationClassFromVertexClass,
				true, aggregationClassToVertexClass);
		aggregationClass.addSuperClass(superClass);
		superClass.addSuperClass(superClass2);

		testIsSubClassOf(superClass2);
	}

	/**
	 * isSubClassOf()
	 * 
	 * TEST CASE: The other element is a subclass of this element
	 */
	@Test
	@Override
	public void testIsSubClassOf3() {
		AggregationClass subClass = graphClass.createAggregationClass(
				"AggregationClassSubClass", aggregationClassFromVertexClass, aggregationClass.getFromMin(), aggregationClass.getFromMax(),
				true, aggregationClassToVertexClass, aggregationClass.getToMin(), aggregationClass.getToMax());
		subClass.addSuperClass(aggregationClass);

		testIsSubClassOf2(subClass);
	}

	/**
	 * isSubClassOf()
	 * 
	 * TEST CASE: The other element has no relation with this element
	 */
	@Test
	@Override
	public void testIsSubClassOf4() {
		AggregationClass aggregationClass2 = graphClass.createAggregationClass(
				"AggregationClass2", aggregationClassFromVertexClass, true,
				aggregationClassToVertexClass);

		testIsSubClassOf2(aggregationClass2);
	}

	/**
	 * isSubClassOf()
	 * 
	 * TEST CASE: The other element and this element are the same
	 */
	@Test
	@Override
	public void testIsSubClassOf5() {
		testIsSubClassOf2(aggregationClass);
	}

	/**
	 * isSuperClassOf()
	 * 
	 * TEST CASE: The other element is a direct subclass of this element
	 */
	@Test
	@Override
	public void testIsSuperClassOf() {
		AggregationClass subClass = graphClass.createAggregationClass(
				"AggregationClassSubClass", aggregationClassFromVertexClass, aggregationClass.getFromMin(), aggregationClass.getFromMax(),
				true, aggregationClassToVertexClass, aggregationClass.getToMin(), aggregationClass.getToMax());
		subClass.addSuperClass(aggregationClass);

		testIsSuperClassOf(subClass);
	}

	/**
	 * isSuperClassOf()
	 * 
	 * TEST CASE: The other element is an inherited subclass of this element
	 */
	@Test
	@Override
	public void testIsSuperClassOf2() {
		AggregationClass subClass = graphClass.createAggregationClass(
				"AggregationClassSubClass", aggregationClassFromVertexClass, aggregationClass.getFromMin(), aggregationClass.getFromMax(),
				true, aggregationClassToVertexClass, aggregationClass.getToMin(), aggregationClass.getToMax());
		AggregationClass subClass2 = graphClass.createAggregationClass(
				"AggregationClassSubClass2", aggregationClassFromVertexClass, aggregationClass.getFromMin(), aggregationClass.getFromMax(),
				true, aggregationClassToVertexClass, aggregationClass.getToMin(), aggregationClass.getToMax());
		subClass.addSuperClass(aggregationClass);
		subClass2.addSuperClass(subClass);

		testIsSuperClassOf(subClass2);
	}

	/**
	 * isSuperClassOf()
	 * 
	 * TEST CASE: The other element is a superclass of this element
	 */
	@Test
	@Override
	public void testIsSuperClassOf3() {
		AggregationClass superClass = graphClass.createAggregationClass(
				"AggregationClassSuperClass", aggregationClassFromVertexClass,
				true, aggregationClassToVertexClass);
		aggregationClass.addSuperClass(superClass);

		testIsSuperClassOf2(superClass);
	}

	/**
	 * isSuperClassOf()
	 * 
	 * TEST CASE: The other element has no relation with this element
	 */
	@Test
	@Override
	public void testIsSuperClassOf4() {
		AggregationClass aggregationClass2 = graphClass.createAggregationClass(
				"AggregationClass2", aggregationClassFromVertexClass, true,
				aggregationClassToVertexClass);

		testIsSuperClassOf2(aggregationClass2);
	}

	/**
	 * isSuperClassOf()
	 * 
	 * TEST CASE: The other element and this element are the same
	 */
	@Test
	@Override
	public void testIsSuperClassOf5() {
		testIsSuperClassOf2(aggregationClass);
	}

	/**
	 * isSuperClassOfOrEquals()
	 * 
	 * TEST CASE: The other element is a direct subclass of this element
	 */
	@Test
	@Override
	public void testIsSuperClassOfOrEquals() {
		AggregationClass subClass = graphClass.createAggregationClass(
				"AggregationClassSubClass", aggregationClassFromVertexClass, aggregationClass.getFromMin(), aggregationClass.getFromMax(),
				true, aggregationClassToVertexClass, aggregationClass.getToMin(), aggregationClass.getToMax());
		subClass.addSuperClass(aggregationClass);

		testIsSuperClassOfOrEquals(subClass);
	}

	/**
	 * isSuperClassOfOrEquals()
	 * 
	 * TEST CASE: The other element is an inherited subclass of this element
	 */
	@Test
	@Override
	public void testIsSuperClassOfOrEquals2() {
		AggregationClass subClass = graphClass.createAggregationClass(
				"AggregationClassSubClass", aggregationClassFromVertexClass, aggregationClass.getFromMin(), aggregationClass.getFromMax(),
				true, aggregationClassToVertexClass, aggregationClass.getToMin(), aggregationClass.getToMax());
		AggregationClass subClass2 = graphClass.createAggregationClass(
				"AggregationClassSubClass2", aggregationClassFromVertexClass, aggregationClass.getFromMin(), aggregationClass.getFromMax(),
				true, aggregationClassToVertexClass, aggregationClass.getToMin(), aggregationClass.getToMax());
		subClass.addSuperClass(aggregationClass);
		subClass2.addSuperClass(subClass);

		testIsSuperClassOfOrEquals(subClass2);
	}

	/**
	 * isSuperClassOfOrEquals()
	 * 
	 * TEST CASE: The other element and this element are the same
	 */
	@Test
	@Override
	public void testIsSuperClassOfOrEquals3() {
		testIsSuperClassOfOrEquals(aggregationClass);
	}

	/**
	 * isSuperClassOfOrEquals()
	 * 
	 * TEST CASE: The other element has no relation with this element
	 */
	@Test
	@Override
	public void testIsSuperClassOfOrEquals4() {
		AggregationClass aggregationClass2 = graphClass.createAggregationClass(
				"AggregationClass2", aggregationClassFromVertexClass, true,
				aggregationClassToVertexClass);

		testIsSuperClassOfOrEquals2(aggregationClass2);
	}

	/**
	 * isSuperClassOfOrEquals()
	 * 
	 * TEST CASE: The other element is a superclass of this element
	 */
	@Test
	@Override
	public void testIsSuperClassOfOrEquals5() {
		AggregationClass superClass = graphClass.createAggregationClass(
				"AggregationClassSuperClass", aggregationClassFromVertexClass,
				true, aggregationClassToVertexClass);
		aggregationClass.addSuperClass(superClass);

		testIsSuperClassOfOrEquals2(superClass);
	}
}
