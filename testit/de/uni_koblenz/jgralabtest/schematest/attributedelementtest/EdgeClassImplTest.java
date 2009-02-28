package de.uni_koblenz.jgralabtest.schematest.attributedelementtest;

import java.util.Vector;

import org.junit.Before;
import org.junit.Test;

import de.uni_koblenz.jgralab.schema.AttributedElementClass;
import de.uni_koblenz.jgralab.schema.EdgeClass;
import de.uni_koblenz.jgralab.schema.GraphClass;
import de.uni_koblenz.jgralab.schema.QualifiedName;
import de.uni_koblenz.jgralab.schema.Schema;
import de.uni_koblenz.jgralab.schema.VertexClass;
import de.uni_koblenz.jgralab.schema.impl.SchemaImpl;

public class EdgeClassImplTest extends GraphElementClassImplTest {

	private EdgeClass edgeClass;
	private VertexClass edgeClassFromVertexClass, edgeClassToVertexClass;

	@Before
	@Override
	public void setUp() {
		super.setUp();

		edgeClassFromVertexClass = graphClass
				.createVertexClass(new QualifiedName("EdgeClassFromVertexClass"));
		edgeClassToVertexClass = graphClass
				.createVertexClass(new QualifiedName("EdgeClassToVertexClass"));

		attributedElement = edgeClass = graphClass.createEdgeClass(
				new QualifiedName("EdgeClass1"), edgeClassFromVertexClass, 0,
				(int) (Math.random() * 100) + 1, "EdgeClassFromRoleName",
				edgeClassToVertexClass, 0, (int) (Math.random() * 100) + 1,
				"EdgeClassToRoleName");
	}

	/**
	 * addAttribute(Attribute)
	 * 
	 * TEST CASE: Adding an attribute, already contained in a superclass of this
	 * element
	 */
	@Test
	public void testAddAttribute4() {
		EdgeClass superClass = graphClass.createEdgeClass(new QualifiedName(
				"EdgeClassSuperClass"), edgeClassFromVertexClass,
				edgeClassToVertexClass);

		edgeClass.addSuperClass(superClass);

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
		EdgeClass subClass = graphClass.createEdgeClass(new QualifiedName(
				"EdgeClassSubClass"), edgeClassFromVertexClass,
				edgeClassToVertexClass);

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
		EdgeClass superClass = graphClass.createEdgeClass(new QualifiedName(
				"EdgeClassSuperClass"), edgeClassFromVertexClass,
				edgeClassToVertexClass);

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
		EdgeClass subClass = graphClass.createEdgeClass(new QualifiedName(
				"EdgeClassSubClass"), edgeClassFromVertexClass,
				edgeClassToVertexClass);

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
		EdgeClass other = graphClass.createEdgeClass(new QualifiedName("Z"),
				edgeClassFromVertexClass, edgeClassToVertexClass);

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
		EdgeClass other = graphClass.createEdgeClass(new QualifiedName("A"),
				edgeClassFromVertexClass, edgeClassToVertexClass);

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
		Schema schema2 = new SchemaImpl(new QualifiedName(
				"de.uni_koblenz.jgralabtest.schematest.TestSchema2"));
		GraphClass graphClass2 = schema2.createGraphClass(new QualifiedName(
				graphClass.getQualifiedName()));
		VertexClass edgeClassFromVertexClass2 = graphClass2
				.createVertexClass(new QualifiedName("EdgeClassFromVertexClass"));
		VertexClass edgeClassToVertexClass2 = graphClass2
				.createVertexClass(new QualifiedName("EdgeClassToVertexClass"));
		EdgeClass other = graphClass2.createEdgeClass(new QualifiedName(
				edgeClass.getQualifiedName()), edgeClassFromVertexClass2,
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

	/**
	 * containsAttribute(String)
	 * 
	 * TEST CASE: looking for an attribute, present in a superclass of this
	 * element
	 */
	@Test
	public void testContainsAttribute3() {
		EdgeClass superClass = graphClass.createEdgeClass(new QualifiedName(
				"EdgeClassSuperClass"), edgeClassFromVertexClass,
				edgeClassToVertexClass);

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

		EdgeClass subClass = graphClass.createEdgeClass(new QualifiedName(
				"EdgeClassSubClass"), edgeClassFromVertexClass,
				edgeClassToVertexClass);

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

		EdgeClass subClass = graphClass.createEdgeClass(new QualifiedName(
				"EdgeClassSubClass"), edgeClassFromVertexClass,
				edgeClassToVertexClass);
		EdgeClass subClass2 = graphClass.createEdgeClass(new QualifiedName(
				"EdgeClassSubClass2"), edgeClassFromVertexClass,
				edgeClassToVertexClass);

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

		EdgeClass subClass = graphClass.createEdgeClass(new QualifiedName(
				"EdgeClassSubClass"), edgeClassFromVertexClass,
				edgeClassToVertexClass);
		EdgeClass subClass2 = graphClass.createEdgeClass(new QualifiedName(
				"EdgeClassSubClass2"), edgeClassFromVertexClass,
				edgeClassToVertexClass);

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

		EdgeClass superClass = graphClass.createEdgeClass(new QualifiedName(
				"EdgeClassSuperClass"), edgeClassFromVertexClass,
				edgeClassToVertexClass);

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

		EdgeClass superClass = graphClass.createEdgeClass(new QualifiedName(
				"EdgeClassSuperClass"), edgeClassFromVertexClass,
				edgeClassToVertexClass);
		EdgeClass superClass2 = graphClass.createEdgeClass(new QualifiedName(
				"EdgeClassSuperClass2"), edgeClassFromVertexClass,
				edgeClassToVertexClass);

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

		EdgeClass superClass = graphClass.createEdgeClass(new QualifiedName(
				"VertexClassSuperClass"), edgeClassFromVertexClass,
				edgeClassToVertexClass);
		EdgeClass superClass2 = graphClass.createEdgeClass(new QualifiedName(
				"VertexClassSuperClass2"), edgeClassFromVertexClass,
				edgeClassToVertexClass);

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
		EdgeClass superClass = graphClass.createEdgeClass(new QualifiedName(
				"EdgeClassSuperClass"), edgeClassFromVertexClass,
				edgeClassToVertexClass);

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
		EdgeClass subClass = graphClass.createEdgeClass(new QualifiedName(
				"EdgeClassSubClass"), edgeClassFromVertexClass,
				edgeClassToVertexClass);

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
		EdgeClass superClass = graphClass.createEdgeClass(new QualifiedName(
				"EdgeClassSuperClass"), edgeClassFromVertexClass,
				edgeClassToVertexClass);

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
		EdgeClass superClass = graphClass.createEdgeClass(new QualifiedName(
				"EdgeClassSuperClass"), edgeClassFromVertexClass,
				edgeClassToVertexClass);

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
		EdgeClass subClass = graphClass.createEdgeClass(new QualifiedName(
				"EdgeClassSubClass"), edgeClassFromVertexClass,
				edgeClassToVertexClass);

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
		EdgeClass superClass = graphClass.createEdgeClass(new QualifiedName(
				"EdgeClassSuperClass"), edgeClassFromVertexClass,
				edgeClassToVertexClass);

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
		EdgeClass superClass = graphClass.createEdgeClass(new QualifiedName(
				"EdgeClassSuperClass"), edgeClassFromVertexClass,
				edgeClassToVertexClass);

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
		EdgeClass subClass = graphClass.createEdgeClass(new QualifiedName(
				"EdgeClassSubClass"), edgeClassFromVertexClass,
				edgeClassToVertexClass);

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
		EdgeClass superClass = graphClass.createEdgeClass(new QualifiedName(
				"EdgeClassSuperClass"), edgeClassFromVertexClass,
				edgeClassToVertexClass);

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
		Vector<AttributedElementClass> expectedSubClasses = new Vector<AttributedElementClass>();

		EdgeClass subClass = graphClass.createEdgeClass(new QualifiedName(
				"EdgeClassSubClass"), edgeClassFromVertexClass,
				edgeClassToVertexClass);

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

		EdgeClass subClass = graphClass.createEdgeClass(new QualifiedName(
				"EdgeClassSubClass"), edgeClassFromVertexClass,
				edgeClassToVertexClass);
		EdgeClass subClass2 = graphClass.createEdgeClass(new QualifiedName(
				"EdgeClassSubClass2"), edgeClassFromVertexClass,
				edgeClassToVertexClass);

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

		EdgeClass subClass = graphClass.createEdgeClass(new QualifiedName(
				"EdgeClassSubClass"), edgeClassFromVertexClass,
				edgeClassToVertexClass); // Direct subclass
		EdgeClass subClass2 = graphClass.createEdgeClass(new QualifiedName(
				"EdgeClassSubClass2"), edgeClassFromVertexClass,
				edgeClassToVertexClass); // Indirect subclass

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

		EdgeClass superClass = graphClass.createEdgeClass(new QualifiedName(
				"EdgeClassSuperClass"), edgeClassFromVertexClass,
				edgeClassToVertexClass);

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

		EdgeClass superClass = graphClass.createEdgeClass(new QualifiedName(
				"EdgeClassSuperClass"), edgeClassFromVertexClass,
				edgeClassToVertexClass);
		EdgeClass superClass2 = graphClass.createEdgeClass(new QualifiedName(
				"EdgeClassSuperClass2"), edgeClassFromVertexClass,
				edgeClassToVertexClass);

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

		EdgeClass superClass = graphClass.createEdgeClass(new QualifiedName(
				"EdgeClassSuperClass"), edgeClassFromVertexClass,
				edgeClassToVertexClass); // Direct superclass
		EdgeClass superClass2 = graphClass.createEdgeClass(new QualifiedName(
				"EdgeClassSuperClass2"), edgeClassFromVertexClass,
				edgeClassToVertexClass); // Indirect superclass

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
