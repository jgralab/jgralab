package de.uni_koblenz.jgralabtest.schematest.attributedelementtest;

import java.util.Vector;

import org.junit.Before;
import org.junit.Test;

import de.uni_koblenz.jgralab.schema.AggregationClass;
import de.uni_koblenz.jgralab.schema.AttributedElementClass;
import de.uni_koblenz.jgralab.schema.GraphClass;
import de.uni_koblenz.jgralab.schema.QualifiedName;
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
				.createVertexClass(new QualifiedName(
						"AggregationClassFromVertexClass"));
		aggregationClassToVertexClass = graphClass
				.createVertexClass(new QualifiedName(
						"AggregationClassToVertexClass"));

		attributedElement = aggregationClass = graphClass
				.createAggregationClass(new QualifiedName("AggregationClass1"),
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
				new QualifiedName("AggregationClassSuperClass"),
				aggregationClassFromVertexClass, true,
				aggregationClassToVertexClass);

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
				new QualifiedName("AggregationClassSubClass"),
				aggregationClassFromVertexClass, true,
				aggregationClassToVertexClass);

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
				new QualifiedName("AggregationClassSuperClass"),
				aggregationClassFromVertexClass, true,
				aggregationClassToVertexClass);

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
				new QualifiedName("AggregationClassSubClass"),
				aggregationClassFromVertexClass, true,
				aggregationClassToVertexClass);

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
		AggregationClass other = graphClass.createAggregationClass(
				new QualifiedName("Z"), aggregationClassFromVertexClass, true,
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
		AggregationClass other = graphClass.createAggregationClass(
				new QualifiedName("A"), aggregationClassFromVertexClass, true,
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
		Schema schema2 = new SchemaImpl(new QualifiedName(
				"de.uni_koblenz.jgralabtest.schematest.TestSchema2"));
		GraphClass graphClass2 = schema2.createGraphClass(new QualifiedName(
				graphClass.getQualifiedName()));
		VertexClass aggregationClassFromVertexClass2 = graphClass2
				.createVertexClass(new QualifiedName(
						"AggregationClassFromVertexClass"));
		VertexClass aggregationClassToVertexClass2 = graphClass2
				.createVertexClass(new QualifiedName(
						"AggregationClassToVertexClass"));
		AggregationClass other = graphClass2.createAggregationClass(
				new QualifiedName(aggregationClass.getQualifiedName()),
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
				new QualifiedName("AggregationClassSuperClass"),
				aggregationClassFromVertexClass, true,
				aggregationClassToVertexClass);

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
				new QualifiedName("AggregationClassSubClass"),
				aggregationClassFromVertexClass, true,
				aggregationClassToVertexClass);

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
				new QualifiedName("AggregationClassSubClass"),
				aggregationClassFromVertexClass, true,
				aggregationClassToVertexClass);
		AggregationClass subClass2 = graphClass.createAggregationClass(
				new QualifiedName("AggregationClassSubClass2"),
				aggregationClassFromVertexClass, true,
				aggregationClassToVertexClass);

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
				new QualifiedName("AggregationClassSubClass"),
				aggregationClassFromVertexClass, true,
				aggregationClassToVertexClass);
		AggregationClass subClass2 = graphClass.createAggregationClass(
				new QualifiedName("AggregationClassSubClass2"),
				aggregationClassFromVertexClass, true,
				aggregationClassToVertexClass);

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
				new QualifiedName("AggregationClassSuperClass"),
				aggregationClassFromVertexClass, true,
				aggregationClassToVertexClass);

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
				new QualifiedName("AggregationClassSuperClass"),
				aggregationClassFromVertexClass, true,
				aggregationClassToVertexClass);
		AggregationClass superClass2 = graphClass.createAggregationClass(
				new QualifiedName("AggregationClassSuperClass2"),
				aggregationClassFromVertexClass, true,
				aggregationClassToVertexClass);

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
				new QualifiedName("AggregationClassSuperClass"),
				aggregationClassFromVertexClass, true,
				aggregationClassToVertexClass);
		AggregationClass superClass2 = graphClass.createAggregationClass(
				new QualifiedName("AggregationClassSuperClass2"),
				aggregationClassFromVertexClass, true,
				aggregationClassToVertexClass);

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
				new QualifiedName("AggregationClassSuperClass"),
				aggregationClassFromVertexClass, true,
				aggregationClassToVertexClass);

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
				new QualifiedName("AggregationClassSubClass"),
				aggregationClassFromVertexClass, true,
				aggregationClassToVertexClass);

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
				new QualifiedName("AggregationClassSuperClass"),
				aggregationClassFromVertexClass, true,
				aggregationClassToVertexClass);

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
				new QualifiedName("AggregationClassSuperClass"),
				aggregationClassFromVertexClass, true,
				aggregationClassToVertexClass);

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
				new QualifiedName("AggregationClassSubClass"),
				aggregationClassFromVertexClass, true,
				aggregationClassToVertexClass);

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
				new QualifiedName("AggregationClassSuperClass"),
				aggregationClassFromVertexClass, true,
				aggregationClassToVertexClass);

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
				new QualifiedName("AggregationClassSuperClass"),
				aggregationClassFromVertexClass, true,
				aggregationClassToVertexClass);

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
				new QualifiedName("AggregationClassSubClass"),
				aggregationClassFromVertexClass, true,
				aggregationClassToVertexClass);

		subClass.addSuperClass(aggregationClass);

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
		AggregationClass superClass = graphClass.createAggregationClass(
				new QualifiedName("AggregationClassSuperClass"),
				aggregationClassFromVertexClass, true,
				aggregationClassToVertexClass);

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
				new QualifiedName("AggregationClassSubClass"),
				aggregationClassFromVertexClass, true,
				aggregationClassToVertexClass);

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
				new QualifiedName("AggregationClassSubClass"),
				aggregationClassFromVertexClass, true,
				aggregationClassToVertexClass);
		AggregationClass subClass2 = graphClass.createAggregationClass(
				new QualifiedName("AggregationClassSubClass2"),
				aggregationClassFromVertexClass, true,
				aggregationClassToVertexClass);

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
				new QualifiedName("AggregationClassSubClass"),
				aggregationClassFromVertexClass, true,
				aggregationClassToVertexClass); // Direct subclass
		AggregationClass subClass2 = graphClass.createAggregationClass(
				new QualifiedName("AggregationClassSubClass2"),
				aggregationClassFromVertexClass, true,
				aggregationClassToVertexClass); // Indirect subclass

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
				new QualifiedName("AggregationClassSuperClass"),
				aggregationClassFromVertexClass, true,
				aggregationClassToVertexClass);

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
				new QualifiedName("AggregationClassSuperClass"),
				aggregationClassFromVertexClass, true,
				aggregationClassToVertexClass);
		AggregationClass superClass2 = graphClass.createAggregationClass(
				new QualifiedName("AggregationClassSuperClass2"),
				aggregationClassFromVertexClass, true,
				aggregationClassToVertexClass);

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
				new QualifiedName("AggregationClassSuperClass"),
				aggregationClassFromVertexClass, true,
				aggregationClassToVertexClass); // Direct superclass
		AggregationClass superClass2 = graphClass.createAggregationClass(
				new QualifiedName("AggregationClassSuperClass2"),
				aggregationClassFromVertexClass, true,
				aggregationClassToVertexClass); // Indirect superclass

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
				new QualifiedName("AggregationClassSuperClass"),
				aggregationClassFromVertexClass, true,
				aggregationClassToVertexClass);
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
				new QualifiedName("AggregationClassSuperClass"),
				aggregationClassFromVertexClass, true,
				aggregationClassToVertexClass);
		subClass.addSuperClass(aggregationClass);

		testGetOwnAttribute4(subClass);
	}

	@Test
	public void testIsAggregateFrom() {
		// TODO Auto-generated method stub
	}

}
