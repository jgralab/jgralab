package de.uni_koblenz.jgralabtest.schematest.attributedelementtest;

import java.util.Vector;

import org.junit.Before;
import org.junit.Test;

import de.uni_koblenz.jgralab.schema.AttributedElementClass;
import de.uni_koblenz.jgralab.schema.EdgeClass;
import de.uni_koblenz.jgralab.schema.QualifiedName;
import de.uni_koblenz.jgralab.schema.VertexClass;

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

		testGetAllSubClasses2(expectedSubClasses);
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

		testGetAllSubClasses3(expectedSubClasses);
	}

	/**
	 * getAllSubClasses()
	 * 
	 * TEST CASE: Getting all subclasses of an element that has no subclasses
	 */
	@Test
	public void testGetAllSubClasses4() {
		// no subclasses expected
		testGetAllSubClasses4(new Vector<AttributedElementClass>());
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

		testGetAllSuperClasses2(expectedSuperClasses);
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

		testGetAllSuperClasses3(expectedSuperClasses);
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

		testGetAllSuperClasses4(expectedSuperClasses);
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
