package de.uni_koblenz.jgralabtest.schematest.attributedelementtest;

import org.junit.Before;
import org.junit.Test;

import de.uni_koblenz.jgralab.schema.EdgeClass;
import de.uni_koblenz.jgralab.schema.QualifiedName;
import de.uni_koblenz.jgralab.schema.VertexClass;

public class EdgeClassImplTest extends GraphElementClassImplTest {

	private EdgeClass edgeClass, edgeClass2;
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
		attributedElement2 = edgeClass2 = graphClass.createEdgeClass(
				new QualifiedName("EdgeClass2"), edgeClassFromVertexClass,
				edgeClassToVertexClass);
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
		edgeClass.addSuperClass(edgeClass2);

		super.testAddAttribute4();
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
		edgeClass2.addSuperClass(edgeClass);

		super.testAddAttribute5();
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
		edgeClass.addSuperClass(edgeClass2);

		super.testContainsAttribute3();
	}

	/**
	 * getAllSubClasses()
	 *
	 * TEST CASE: Getting all subclasses of an element with one direct subclass
	 */
	@Test
	@Override
	public void testGetAllSubClasses() {
		String subClassName = "EdgeClassSubClass";
		EdgeClass subClass = graphClass
				.createEdgeClass(new QualifiedName(subClassName),
						edgeClassFromVertexClass, edgeClassToVertexClass);

		subClass.addSuperClass(edgeClass);
		// expected names of subclasses of this element
		expectedStrings.add(subClassName);
		// expected subclass count
		expectedValue = 1;

		super.testGetAllSubClasses_main();
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
		String subClassName = "EdgeClassSubClass";
		String subClass2Name = "EdgeClassSubClass2";
		EdgeClass subClass = graphClass
				.createEdgeClass(new QualifiedName(subClassName),
						edgeClassFromVertexClass, edgeClassToVertexClass);
		EdgeClass subClass2 = graphClass.createEdgeClass(new QualifiedName(
				subClass2Name), edgeClassFromVertexClass,
				edgeClassToVertexClass);

		subClass.addSuperClass(edgeClass);
		subClass2.addSuperClass(edgeClass);
		// expected names of subclasses of this element
		expectedStrings.add(subClassName);
		expectedStrings.add(subClass2Name);
		// expected subclass count
		expectedValue = 2;

		super.testGetAllSubClasses_main();
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
		String subClassName = "EdgeClassSubClass";
		String subClass2Name = "EdgeClassSubClass2";
		EdgeClass subClass = graphClass
				.createEdgeClass(new QualifiedName(subClassName),
						edgeClassFromVertexClass, edgeClassToVertexClass);
		EdgeClass subClass2 = graphClass.createEdgeClass(new QualifiedName(
				subClass2Name), edgeClassFromVertexClass,
				edgeClassToVertexClass);

		subClass.addSuperClass(edgeClass);
		subClass2.addSuperClass(subClass);
		// expected names of subclasses of this element
		expectedStrings.add(subClassName);
		expectedStrings.add(subClass2Name);
		// expected subclass count
		expectedValue = 2;

		super.testGetAllSubClasses_main();
	}

	/**
	 * getAllSubClasses()
	 *
	 * TEST CASE: Getting all subclasses of an element that has no subclasses
	 */
	@Test
	@Override
	public void testGetAllSubClasses4() {
		// expected subclass count
		expectedValue = 0;

		super.testGetAllSubClasses_main();
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
		String superClassName = "EdgeClassSuperClass";
		EdgeClass superClass = graphClass.createEdgeClass(new QualifiedName(
				superClassName), edgeClassFromVertexClass,
				edgeClassToVertexClass);

		edgeClass.addSuperClass(superClass);
		// expected names of superclasses of this element
		expectedStrings.add(schema.getDefaultEdgeClass().getSimpleName());
		expectedStrings.add(superClassName);
		// expected superclass count including the default superclass(es)
		expectedValue = 2;

		super.testGetAllSuperClasses_main();
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
		String superClassName = "EdgeClassSuperClass";
		String superClass2Name = "EdgeClassSuperClass2";
		EdgeClass superClass = graphClass.createEdgeClass(new QualifiedName(
				superClassName), edgeClassFromVertexClass,
				edgeClassToVertexClass);
		EdgeClass superClass2 = graphClass.createEdgeClass(new QualifiedName(
				superClass2Name), edgeClassFromVertexClass,
				edgeClassToVertexClass);

		edgeClass.addSuperClass(superClass);
		edgeClass.addSuperClass(superClass2);
		// expected names of superclasses of this element
		expectedStrings.add(schema.getDefaultEdgeClass().getSimpleName());
		expectedStrings.add(superClassName);
		expectedStrings.add(superClass2Name);
		// expected superclass count including the default superclass(es)
		expectedValue = 3;

		super.testGetAllSuperClasses_main();
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
		String superClassName = "VertexClassSuperClass";
		String superClass2Name = "VertexClassSuperClass2";
		EdgeClass superClass = graphClass.createEdgeClass(new QualifiedName(
				superClassName), edgeClassFromVertexClass,
				edgeClassToVertexClass);
		EdgeClass superClass2 = graphClass.createEdgeClass(new QualifiedName(
				superClass2Name), edgeClassFromVertexClass,
				edgeClassToVertexClass);

		edgeClass.addSuperClass(superClass);
		superClass.addSuperClass(superClass2);
		// expected names of superclasses of this element
		expectedStrings.add(schema.getDefaultEdgeClass().getSimpleName());
		expectedStrings.add(superClassName);
		expectedStrings.add(superClass2Name);
		// expected superclass count including the default superclass(es)
		expectedValue = 3;

		super.testGetAllSuperClasses_main();
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
		// expected names of superclasses of this element
		expectedStrings.add(schema.getDefaultEdgeClass().getSimpleName());
		// expected superclass count including the default superclass(es)
		expectedValue = 1;

		super.testGetAllSuperClasses_main();
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
