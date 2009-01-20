package de.uni_koblenz.jgralabtest.schematest.attributedelementtest;

import java.util.Vector;

import org.junit.Before;
import org.junit.Test;

import de.uni_koblenz.jgralab.schema.AttributedElementClass;
import de.uni_koblenz.jgralab.schema.CompositionClass;
import de.uni_koblenz.jgralab.schema.QualifiedName;
import de.uni_koblenz.jgralab.schema.VertexClass;

public final class CompositionClassImplTest extends AggregationClassImplTest {

	private CompositionClass compositionClass;
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
						compositionClassFromVertexClass, 0, 1,
						"CompositionClassFromRoleName", true,
						compositionClassToVertexClass, 1,
						(int) (Math.random() * 100) + 1,
						"CompositionClassToRoleName");
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
		CompositionClass superClass = graphClass.createCompositionClass(
				new QualifiedName("CompositionClassSuperClass"),
				compositionClassFromVertexClass, true,
				compositionClassToVertexClass);

		compositionClass.addSuperClass(superClass);

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
		CompositionClass subClass = graphClass.createCompositionClass(
				new QualifiedName("CompositionClassSubClass"),
				compositionClassFromVertexClass, true,
				compositionClassToVertexClass);

		subClass.addSuperClass(compositionClass);

		testAddAttribute5(subClass);
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
		CompositionClass superClass = graphClass.createCompositionClass(
				new QualifiedName("CompositionClassSuperClass"),
				compositionClassFromVertexClass, true,
				compositionClassToVertexClass);

		compositionClass.addSuperClass(superClass);

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

		CompositionClass subClass = graphClass.createCompositionClass(
				new QualifiedName("CompositionClassSubClass"),
				compositionClassFromVertexClass, true,
				compositionClassToVertexClass);

		subClass.addSuperClass(compositionClass);
		// expected names of subclasses of this element
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

		CompositionClass subClass = graphClass.createCompositionClass(
				new QualifiedName("CompositionClassSubClass"),
				compositionClassFromVertexClass, true,
				compositionClassToVertexClass);
		CompositionClass subClass2 = graphClass.createCompositionClass(
				new QualifiedName("CompositionClassSubClass2"),
				compositionClassFromVertexClass, true,
				compositionClassToVertexClass);

		subClass.addSuperClass(compositionClass);
		subClass2.addSuperClass(compositionClass);

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

		CompositionClass subClass = graphClass.createCompositionClass(
				new QualifiedName("CompositionClassSubClass"),
				compositionClassFromVertexClass, true,
				compositionClassToVertexClass);
		CompositionClass subClass2 = graphClass.createCompositionClass(
				new QualifiedName("CompositionClassSubClass2"),
				compositionClassFromVertexClass, true,
				compositionClassToVertexClass);

		subClass.addSuperClass(compositionClass);
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

		CompositionClass superClass = graphClass.createCompositionClass(
				new QualifiedName("CompositionClassSuperClass"),
				compositionClassFromVertexClass, true,
				compositionClassToVertexClass);

		compositionClass.addSuperClass(superClass);

		expectedSuperClasses.add(schema.getDefaultEdgeClass());
		expectedSuperClasses.add(schema.getDefaultAggregationClass());
		expectedSuperClasses.add(schema.getDefaultCompositionClass());
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

		CompositionClass superClass = graphClass.createCompositionClass(
				new QualifiedName("CompositionClassSuperClass"),
				compositionClassFromVertexClass, true,
				compositionClassToVertexClass);
		CompositionClass superClass2 = graphClass.createCompositionClass(
				new QualifiedName("CompositionClassSuperClass2"),
				compositionClassFromVertexClass, true,
				compositionClassToVertexClass);

		compositionClass.addSuperClass(superClass);
		compositionClass.addSuperClass(superClass2);

		expectedSuperClasses.add(schema.getDefaultEdgeClass());
		expectedSuperClasses.add(schema.getDefaultAggregationClass());
		expectedSuperClasses.add(schema.getDefaultCompositionClass());
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

		CompositionClass superClass = graphClass.createCompositionClass(
				new QualifiedName("CompositionClassSuperClass"),
				compositionClassFromVertexClass, true,
				compositionClassToVertexClass);
		CompositionClass superClass2 = graphClass.createCompositionClass(
				new QualifiedName("CompositionClassSuperClass2"),
				compositionClassFromVertexClass, true,
				compositionClassToVertexClass);

		compositionClass.addSuperClass(superClass);
		superClass.addSuperClass(superClass2);

		expectedSuperClasses.add(schema.getDefaultEdgeClass());
		expectedSuperClasses.add(schema.getDefaultAggregationClass());
		expectedSuperClasses.add(schema.getDefaultCompositionClass());
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
		expectedSuperClasses.add(schema.getDefaultCompositionClass());

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
		CompositionClass superClass = graphClass.createCompositionClass(
				new QualifiedName("CompositionClassSuperClass"),
				compositionClassFromVertexClass, true,
				compositionClassToVertexClass);

		compositionClass.addSuperClass(superClass);

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
		CompositionClass subClass = graphClass.createCompositionClass(
				new QualifiedName("CompositionClassSubClass"),
				compositionClassFromVertexClass, true,
				compositionClassToVertexClass);

		subClass.addSuperClass(compositionClass);

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
		CompositionClass superClass = graphClass.createCompositionClass(
				new QualifiedName("CompositionClassSuperClass"),
				compositionClassFromVertexClass, true,
				compositionClassToVertexClass);

		compositionClass.addSuperClass(superClass);

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
		CompositionClass superClass = graphClass.createCompositionClass(
				new QualifiedName("CompositionClassSuperClass"),
				compositionClassFromVertexClass, true,
				compositionClassToVertexClass);

		compositionClass.addSuperClass(superClass);

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
		CompositionClass subClass = graphClass.createCompositionClass(
				new QualifiedName("CompositionClassSubClass"),
				compositionClassFromVertexClass, true,
				compositionClassToVertexClass);

		subClass.addSuperClass(compositionClass);

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
		CompositionClass superClass = graphClass.createCompositionClass(
				new QualifiedName("CompositionClassSuperClass"),
				compositionClassFromVertexClass, true,
				compositionClassToVertexClass);

		compositionClass.addSuperClass(superClass);

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
		CompositionClass superClass = graphClass.createCompositionClass(
				new QualifiedName("CompositionClassSuperClass"),
				compositionClassFromVertexClass, true,
				compositionClassToVertexClass);

		compositionClass.addSuperClass(superClass);

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
		CompositionClass subClass = graphClass.createCompositionClass(
				new QualifiedName("CompositionClassSubClass"),
				compositionClassFromVertexClass, true,
				compositionClassToVertexClass);

		subClass.addSuperClass(compositionClass);

		testGetAttributeList5(subClass);
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

		CompositionClass subClass = graphClass.createCompositionClass(
				new QualifiedName("CompositionClassSubClass"),
				compositionClassFromVertexClass, true,
				compositionClassToVertexClass);

		subClass.addSuperClass(compositionClass);

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

		CompositionClass subClass = graphClass.createCompositionClass(
				new QualifiedName("CompositionClassSubClass"),
				compositionClassFromVertexClass, true,
				compositionClassToVertexClass);
		CompositionClass subClass2 = graphClass.createCompositionClass(
				new QualifiedName("CompositionClassSubClass2"),
				compositionClassFromVertexClass, true,
				compositionClassToVertexClass);

		subClass.addSuperClass(compositionClass);
		subClass2.addSuperClass(compositionClass);

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

		CompositionClass subClass = graphClass.createCompositionClass(
				new QualifiedName("CompositionClassSubClass"),
				compositionClassFromVertexClass, true,
				compositionClassToVertexClass); // Direct subclass
		CompositionClass subClass2 = graphClass.createCompositionClass(
				new QualifiedName("CompositionClassSubClass2"),
				compositionClassFromVertexClass, true,
				compositionClassToVertexClass); // Indirect subclass

		subClass.addSuperClass(compositionClass);
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

		CompositionClass superClass = graphClass.createCompositionClass(
				new QualifiedName("CompositionClassSuperClass"),
				compositionClassFromVertexClass, true,
				compositionClassToVertexClass);

		compositionClass.addSuperClass(superClass);

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
	@Override
	public void testGetDirectSuperClasses2() {
		Vector<AttributedElementClass> expectedSuperClasses = new Vector<AttributedElementClass>();

		CompositionClass superClass = graphClass.createCompositionClass(
				new QualifiedName("CompositionClassSuperClass"),
				compositionClassFromVertexClass, true,
				compositionClassToVertexClass);
		CompositionClass superClass2 = graphClass.createCompositionClass(
				new QualifiedName("CompositionClassSuperClass2"),
				compositionClassFromVertexClass, true,
				compositionClassToVertexClass);

		compositionClass.addSuperClass(superClass);
		compositionClass.addSuperClass(superClass2);

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

		CompositionClass superClass = graphClass.createCompositionClass(
				new QualifiedName("CompositionClassSuperClass"),
				compositionClassFromVertexClass, true,
				compositionClassToVertexClass); // Direct superclass
		CompositionClass superClass2 = graphClass.createCompositionClass(
				new QualifiedName("CompositionClassSuperClass2"),
				compositionClassFromVertexClass, true,
				compositionClassToVertexClass); // Indirect superclass

		compositionClass.addSuperClass(superClass);
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
	@Override
	public void testGetDirectSuperClasses4() {
		Vector<AttributedElementClass> expectedSuperClasses = new Vector<AttributedElementClass>();

		expectedSuperClasses.add(schema.getDefaultEdgeClass());
		expectedSuperClasses.add(schema.getDefaultAggregationClass());
		expectedSuperClasses.add(schema.getDefaultCompositionClass());

		testGetDirectSuperClasses(expectedSuperClasses);
	}
}
