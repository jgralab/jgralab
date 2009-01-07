package de.uni_koblenz.jgralabtest.schematest.attributedelementtest;

import org.junit.Before;
import org.junit.Test;

import de.uni_koblenz.jgralab.schema.AggregationClass;
import de.uni_koblenz.jgralab.schema.QualifiedName;
import de.uni_koblenz.jgralab.schema.VertexClass;

public class AggregationClassImplTest extends EdgeClassImplTest {

	private AggregationClass aggregationClass, aggregationClass2;
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
		attributedElement2 = aggregationClass2 = graphClass
				.createAggregationClass(new QualifiedName("AggregationClass2"),
						aggregationClassFromVertexClass, true,
						aggregationClassToVertexClass);
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
		aggregationClass.addSuperClass(aggregationClass2);

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
		aggregationClass2.addSuperClass(aggregationClass);

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
		aggregationClass.addSuperClass(aggregationClass2);

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
		String subClassName = "AggregationClassSubClass";
		AggregationClass subClass = graphClass.createAggregationClass(
				new QualifiedName(subClassName),
				aggregationClassFromVertexClass, true,
				aggregationClassToVertexClass);

		subClass.addSuperClass(aggregationClass);
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
		String subClassName = "AggregationClassSubClass";
		String subClass2Name = "AggregationClassSubClass2";
		AggregationClass subClass = graphClass.createAggregationClass(
				new QualifiedName(subClassName),
				aggregationClassFromVertexClass, true,
				aggregationClassToVertexClass);
		AggregationClass subClass2 = graphClass.createAggregationClass(
				new QualifiedName(subClass2Name),
				aggregationClassFromVertexClass, true,
				aggregationClassToVertexClass);

		subClass.addSuperClass(aggregationClass);
		subClass2.addSuperClass(aggregationClass);
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
		String subClassName = "AggregationClassSubClass";
		String subClass2Name = "AggregationClassSubClass2";
		AggregationClass subClass = graphClass.createAggregationClass(
				new QualifiedName(subClassName),
				aggregationClassFromVertexClass, true,
				aggregationClassToVertexClass);
		AggregationClass subClass2 = graphClass.createAggregationClass(
				new QualifiedName(subClass2Name),
				aggregationClassFromVertexClass, true,
				aggregationClassToVertexClass);

		subClass.addSuperClass(aggregationClass);
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
		String superClassName = "AggregationClassSuperClass";
		AggregationClass superClass = graphClass.createAggregationClass(
				new QualifiedName(superClassName),
				aggregationClassFromVertexClass, true,
				aggregationClassToVertexClass);

		aggregationClass.addSuperClass(superClass);
		// expected names of superclasses of this element
		expectedStrings.add(schema.getDefaultEdgeClass().getSimpleName());
		expectedStrings
				.add(schema.getDefaultAggregationClass().getSimpleName());
		expectedStrings.add(superClassName);
		// expected superclass count including the default superclass(es)
		expectedValue = 3;

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
		String superClassName = "AggregationClassSuperClass";
		String superClass2Name = "AggregationClassSuperClass2";
		AggregationClass superClass = graphClass.createAggregationClass(
				new QualifiedName(superClassName),
				aggregationClassFromVertexClass, true,
				aggregationClassToVertexClass);
		AggregationClass superClass2 = graphClass.createAggregationClass(
				new QualifiedName(superClass2Name),
				aggregationClassFromVertexClass, true,
				aggregationClassToVertexClass);

		aggregationClass.addSuperClass(superClass);
		aggregationClass.addSuperClass(superClass2);
		// expected names of superclasses of this element
		expectedStrings.add(schema.getDefaultEdgeClass().getSimpleName());
		expectedStrings
				.add(schema.getDefaultAggregationClass().getSimpleName());
		expectedStrings.add(superClassName);
		expectedStrings.add(superClass2Name);
		// expected superclass count including the default superclass(es)
		expectedValue = 4;

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
		String superClassName = "AggregationClassSuperClass";
		String superClass2Name = "AggregationClassSuperClass2";
		AggregationClass superClass = graphClass.createAggregationClass(
				new QualifiedName(superClassName),
				aggregationClassFromVertexClass, true,
				aggregationClassToVertexClass);
		AggregationClass superClass2 = graphClass.createAggregationClass(
				new QualifiedName(superClass2Name),
				aggregationClassFromVertexClass, true,
				aggregationClassToVertexClass);

		aggregationClass.addSuperClass(superClass);
		superClass.addSuperClass(superClass2);
		// expected names of superclasses of this element
		expectedStrings.add(schema.getDefaultEdgeClass().getSimpleName());
		expectedStrings
				.add(schema.getDefaultAggregationClass().getSimpleName());
		expectedStrings.add(superClassName);
		expectedStrings.add(superClass2Name);
		// expected superclass count including the default superclass(es)
		expectedValue = 4;

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
		expectedStrings
				.add(schema.getDefaultAggregationClass().getSimpleName());
		// expected superclass count including the default superclass(es)
		expectedValue = 2;

		super.testGetAllSuperClasses_main();
	}

	@Test
	public void testIsAggregateFrom() {
		// TODO Auto-generated method stub
	}

}
