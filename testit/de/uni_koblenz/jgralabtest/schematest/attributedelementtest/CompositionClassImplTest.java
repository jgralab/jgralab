package de.uni_koblenz.jgralabtest.schematest.attributedelementtest;

import org.junit.Before;
import org.junit.Test;

import de.uni_koblenz.jgralab.schema.CompositionClass;
import de.uni_koblenz.jgralab.schema.QualifiedName;
import de.uni_koblenz.jgralab.schema.VertexClass;

public final class CompositionClassImplTest extends AggregationClassImplTest {

	private CompositionClass compositionClass, compositionClass2;
	private VertexClass compositionClassFromVertexClass,
			compositionClassToVertexClass;

	@Before
	@Override
	public void setUp() {
		super.setUp();

		compositionClassFromVertexClass = graphClass
				.createVertexClass(new QualifiedName(
						"CompositionClassFromVertexClass"));
		compositionClassToVertexClass = graphClass
				.createVertexClass(new QualifiedName(
						"CompositionClassToVertexClass"));

		attributedElement = compositionClass = graphClass
				.createCompositionClass(new QualifiedName("CompositionClass1"),
						compositionClassFromVertexClass, 1, (int) (Math
								.random() * 100) + 1,
						"CompositionClassFromRoleName", true,
						compositionClassToVertexClass, 1,
						(int) (Math.random() * 100) + 1,
						"CompositionClassToRoleName");
		attributedElement2 = compositionClass2 = graphClass
				.createCompositionClass(new QualifiedName("CompositionClass2"),
						compositionClassFromVertexClass, true,
						compositionClassToVertexClass);
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
		compositionClass.addSuperClass(compositionClass2);

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
		compositionClass2.addSuperClass(compositionClass);

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
		compositionClass.addSuperClass(compositionClass2);

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
		String subClassName = "CompositionClassSubClass";
		CompositionClass subClass = graphClass.createCompositionClass(
				new QualifiedName(subClassName),
				compositionClassFromVertexClass, true,
				compositionClassToVertexClass);

		subClass.addSuperClass(compositionClass);
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
		String subClassName = "CompositionClassSubClass";
		String subClass2Name = "CompositionClassSubClass2";
		CompositionClass subClass = graphClass.createCompositionClass(
				new QualifiedName(subClassName),
				compositionClassFromVertexClass, true,
				compositionClassToVertexClass);
		CompositionClass subClass2 = graphClass.createCompositionClass(
				new QualifiedName(subClass2Name),
				compositionClassFromVertexClass, true,
				compositionClassToVertexClass);

		subClass.addSuperClass(compositionClass);
		subClass2.addSuperClass(compositionClass);
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
		String subClassName = "CompositionClassSubClass";
		String subClass2Name = "CompositionClassSubClass2";
		CompositionClass subClass = graphClass.createCompositionClass(
				new QualifiedName(subClassName),
				compositionClassFromVertexClass, true,
				compositionClassToVertexClass);
		CompositionClass subClass2 = graphClass.createCompositionClass(
				new QualifiedName(subClass2Name),
				compositionClassFromVertexClass, true,
				compositionClassToVertexClass);

		subClass.addSuperClass(compositionClass);
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
		String superClassName = "CompositionClassSuperClass";
		CompositionClass superClass = graphClass.createCompositionClass(
				new QualifiedName(superClassName),
				compositionClassFromVertexClass, true,
				compositionClassToVertexClass);

		compositionClass.addSuperClass(superClass);
		// expected names of superclasses of this element
		expectedStrings.add(schema.getDefaultEdgeClass().getSimpleName());
		expectedStrings
				.add(schema.getDefaultAggregationClass().getSimpleName());
		expectedStrings
				.add(schema.getDefaultCompositionClass().getSimpleName());
		expectedStrings.add(superClassName);
		// expected superclass count including the default superclass(es)
		expectedValue = 4;

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
		String superClassName = "CompositionClassSuperClass";
		String superClass2Name = "CompositionClassSuperClass2";
		CompositionClass superClass = graphClass.createCompositionClass(
				new QualifiedName(superClassName),
				compositionClassFromVertexClass, true,
				compositionClassToVertexClass);
		CompositionClass superClass2 = graphClass.createCompositionClass(
				new QualifiedName(superClass2Name),
				compositionClassFromVertexClass, true,
				compositionClassToVertexClass);

		compositionClass.addSuperClass(superClass);
		compositionClass.addSuperClass(superClass2);
		// expected names of superclasses of this element
		expectedStrings.add(schema.getDefaultEdgeClass().getSimpleName());
		expectedStrings
				.add(schema.getDefaultAggregationClass().getSimpleName());
		expectedStrings
				.add(schema.getDefaultCompositionClass().getSimpleName());
		expectedStrings.add(superClassName);
		expectedStrings.add(superClass2Name);
		// expected superclass count including the default superclass(es)
		expectedValue = 5;

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
		String superClassName = "CompositionClassSuperClass";
		String superClass2Name = "CompositionClassSuperClass2";
		CompositionClass superClass = graphClass.createCompositionClass(
				new QualifiedName(superClassName),
				compositionClassFromVertexClass, true,
				compositionClassToVertexClass);
		CompositionClass superClass2 = graphClass.createCompositionClass(
				new QualifiedName(superClass2Name),
				compositionClassFromVertexClass, true,
				compositionClassToVertexClass);

		compositionClass.addSuperClass(superClass);
		superClass.addSuperClass(superClass2);
		// expected names of superclasses of this element
		expectedStrings.add(schema.getDefaultEdgeClass().getSimpleName());
		expectedStrings
				.add(schema.getDefaultAggregationClass().getSimpleName());
		expectedStrings
				.add(schema.getDefaultCompositionClass().getSimpleName());
		expectedStrings.add(superClassName);
		expectedStrings.add(superClass2Name);
		// expected superclass count including the default superclass(es)
		expectedValue = 5;

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
		expectedStrings
				.add(schema.getDefaultCompositionClass().getSimpleName());
		// expected superclass count including the default superclass(es)
		expectedValue = 3;

		super.testGetAllSuperClasses_main();
	}
}
