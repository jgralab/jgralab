package de.uni_koblenz.jgralabtest.schematest.attributedelementtest;

import org.junit.Before;
import org.junit.Test;

import de.uni_koblenz.jgralab.schema.QualifiedName;
import de.uni_koblenz.jgralab.schema.VertexClass;

public final class VertexClassImplTest extends GraphElementClassImplTest {

	private VertexClass vertexClass, vertexClass2;

	@Before
	@Override
	public void setUp() {
		super.setUp();

		attributedElement = vertexClass = graphClass
				.createVertexClass(new QualifiedName("VertexClass1"));
		attributedElement2 = vertexClass2 = graphClass
				.createVertexClass(new QualifiedName("VertexClass2"));
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
		vertexClass.addSuperClass(vertexClass2);

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
		vertexClass2.addSuperClass(vertexClass);

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
		vertexClass.addSuperClass(vertexClass2);

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
		String subClassName = "VertexClassSubClass";
		VertexClass subClass = graphClass.createVertexClass(new QualifiedName(
				subClassName));

		subClass.addSuperClass(vertexClass);
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
		String subClassName = "VertexClassSubClass";
		String subClass2Name = "VertexClassSubClass2";
		VertexClass subClass = graphClass.createVertexClass(new QualifiedName(
				subClassName));
		VertexClass subClass2 = graphClass.createVertexClass(new QualifiedName(
				subClass2Name));

		subClass.addSuperClass(vertexClass);
		subClass2.addSuperClass(vertexClass);
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
		String subClassName = "VertexClassSubClass";
		String subClass2Name = "VertexClassSubClass2";
		VertexClass subClass = graphClass.createVertexClass(new QualifiedName(
				subClassName));
		VertexClass subClass2 = graphClass.createVertexClass(new QualifiedName(
				subClass2Name));

		subClass.addSuperClass(vertexClass);
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
		String superClassName = "VertexClassSuperClass";
		VertexClass superClass = graphClass
				.createVertexClass(new QualifiedName(superClassName));

		vertexClass.addSuperClass(superClass);
		// expected names of superclasses of this element
		expectedStrings.add(schema.getDefaultVertexClass().getSimpleName());
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
		String superClassName = "VertexClassSuperClass";
		String superClass2Name = "VertexClassSuperClass2";
		VertexClass superClass = graphClass
				.createVertexClass(new QualifiedName(superClassName));
		VertexClass superClass2 = graphClass
				.createVertexClass(new QualifiedName(superClass2Name));

		vertexClass.addSuperClass(superClass);
		vertexClass.addSuperClass(superClass2);
		// expected names of superclasses of this element
		expectedStrings.add(schema.getDefaultVertexClass().getSimpleName());
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
		VertexClass superClass = graphClass
				.createVertexClass(new QualifiedName(superClassName));
		VertexClass superClass2 = graphClass
				.createVertexClass(new QualifiedName(superClass2Name));

		vertexClass.addSuperClass(superClass);
		superClass.addSuperClass(superClass2);
		// expected names of superclasses of this element
		expectedStrings.add(schema.getDefaultVertexClass().getSimpleName());
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
		expectedStrings.add(schema.getDefaultVertexClass().getSimpleName());
		// expected superclass count including the default superclass(es)
		expectedValue = 1;

		super.testGetAllSuperClasses_main();
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

}
