package de.uni_koblenz.jgralabtest.schematest.attributedelementtest;

import java.util.HashSet;
import java.util.Vector;

import org.junit.Before;
import org.junit.Test;

import de.uni_koblenz.jgralab.schema.AttributedElementClass;
import de.uni_koblenz.jgralab.schema.GraphClass;
import de.uni_koblenz.jgralab.schema.QualifiedName;
import de.uni_koblenz.jgralab.schema.Schema;
import de.uni_koblenz.jgralab.schema.VertexClass;
import de.uni_koblenz.jgralab.schema.impl.SchemaImpl;

public final class GraphClassImplTest extends AttributedElementClassImplTest {

	@Before
	@Override
	public void setUp() {
		super.setUp();
		attributedElement = graphClass;
	}

	/**
	 * compareTo(AttributedElementClass)
	 * 
	 * TEST CASE: Comparing this element to another, where this element´s
	 * qualified name is lexicographically less than the other´s
	 */
	@Test
	public void testCompareTo() {
		GraphClass other = schema.createGraphClass(new QualifiedName("Z"));

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
		GraphClass other = schema.createGraphClass(new QualifiedName("A"));

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
		GraphClass other = schema2.createGraphClass(new QualifiedName(
				graphClass.getQualifiedName()));

		testCompareTo3(other);
	}

	/**
	 * compareTo(AttributedElementClass)
	 * 
	 * TEST CASE: Comparing an element to itself
	 */
	@Test
	public void testCompareTo4() {
		testCompareTo3(graphClass);
	}

	/**
	 * getLeastCommonSuperclass(...)
	 * 
	 * TEST CASE: The elements are the same
	 */
	@Test
	public void testGetLeastCommonSuperclass4() {
		AttributedElementClass expectedLCS = graphClass;

		HashSet<AttributedElementClass> others = new HashSet<AttributedElementClass>();
		others.add(graphClass);

		testGetLeastCommonSuperclass(others, expectedLCS);
	}

	/**
	 * getLeastCommonSuperclass(...)
	 * 
	 * TEST CASE: The elements have no common superclasses, except the default
	 * class
	 */
	@Test
	public void testGetLeastCommonSuperclass5() {
		AttributedElementClass expectedLCS = schema.getDefaultGraphClass();

		HashSet<AttributedElementClass> others = new HashSet<AttributedElementClass>();
		GraphClass graphClass2 = schema.createGraphClass(new QualifiedName(
				"GraphClass2"));
		others.add(graphClass2);

		testGetLeastCommonSuperclass(others, expectedLCS);
	}

	/**
	 * getLeastCommonSuperclass(...)
	 * 
	 * TEST CASE: The element array is empty
	 */
	@Test
	public void testGetLeastCommonSuperclass6() {
		AttributedElementClass expectedLCS = graphClass;
		HashSet<AttributedElementClass> others = new HashSet<AttributedElementClass>();

		testGetLeastCommonSuperclass(others, expectedLCS);
	}

	/**
	 * getLeastCommonSuperclass(...)
	 * 
	 * TEST CASE: The elements are from different kinds
	 */
	@Test
	public void testGetLeastCommonSuperclass7() {
		HashSet<AttributedElementClass> others = new HashSet<AttributedElementClass>();
		VertexClass vertexClass = graphClass
				.createVertexClass(new QualifiedName("VertexClass1"));
		others.add(vertexClass);

		testGetLeastCommonSuperclass(others, null);
	}

	/**
	 * getAllSubClasses()
	 * 
	 * TEST CASE: Getting all subclasses of an element that has no subclasses
	 */
	@Test
	public void testGetAllSubClasses4() {
		// A (virgin) GraphClass has no subclasses.
		testGetAllSubClasses(new Vector<AttributedElementClass>());
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

		// Each element has a Default-Class as superclass upon creation.
		// GraphClass does not allow other superclasses.
		expectedSuperClasses.add(schema.getDefaultGraphClass());

		testGetAllSuperClasses(expectedSuperClasses);
	}

	/**
	 * getDirectSubClasses()
	 * 
	 * TEST CASE: Getting all direct subclasses of an element that has no direct
	 * subclasses.
	 */
	@Test
	public void testGetDirectSubClasses4() {
		// A (virgin) GraphClass has no subclasses.
		testGetDirectSubClasses(new Vector<AttributedElementClass>());
	}

	/**
	 * getDirectSubClasses()
	 * 
	 * TEST CASE: Getting all direct superclasses of an element that has no
	 * direct superclasses.
	 */
	@Test
	public void testGetDirectSuperClasses4() {
		Vector<AttributedElementClass> expectedSuperClasses = new Vector<AttributedElementClass>();

		// Each element has a Default-Class as superclass upon creation.
		// GraphClass does not allow other superclasses.
		expectedSuperClasses.add(schema.getDefaultGraphClass());

		testGetDirectSuperClasses(expectedSuperClasses);
	}

	@Test
	public void testCreateAggregationClass() {
		// TODO Auto-generated method stub
	}

	@Test
	public void testCreateCompositionClass() {
		// TODO Auto-generated method stub
	}

	@Test
	public void testCreateEdgeClass() {
		// TODO Auto-generated method stub
	}

	@Test
	public void testCreateVertexClass() {
		// TODO Auto-generated method stub
	}

	@Test
	public void testGetAggregationClass() {
		// TODO Auto-generated method stub
	}

	@Test
	public void testGetAggregationClasses() {
		// TODO Auto-generated method stub
	}

	@Test
	public void testGetCompositionClass() {
		// TODO Auto-generated method stub
	}

	@Test
	public void testGetCompositionClasses() {
		// TODO Auto-generated method stub
	}

	@Test
	public void testGetEdgeClass() {
		// TODO Auto-generated method stub
	}

	@Test
	public void testGetEdgeClasses() {
		// TODO Auto-generated method stub
	}

	@Test
	public void testGetGraphElementClass() {
		// TODO Auto-generated method stub
	}

	@Test
	public void testGetGraphElementClasses() {
		// TODO Auto-generated method stub
	}

	@Test
	public void testGetOwnAggregationClasses() {
		// TODO Auto-generated method stub
	}

	@Test
	public void testGetOwnCompositionClasses() {
		// TODO Auto-generated method stub
	}

	@Test
	public void testGetOwnEdgeClassCount() {
		// TODO Auto-generated method stub
	}

	@Test
	public void testGetOwnEdgeClasses() {
		// TODO Auto-generated method stub
	}

	@Test
	public void testGetOwnGraphElementClasses() {
		// TODO Auto-generated method stub
	}

	@Test
	public void testGetOwnVertexClassCount() {
		// TODO Auto-generated method stub
	}

	@Test
	public void testGetOwnVertexClasses() {
		// TODO Auto-generated method stub
	}

	@Test
	public void testGetSchema() {
		// TODO Auto-generated method stub
	}

	@Test
	public void testGetVariableName() {
		// TODO Auto-generated method stub
	}

	@Test
	public void testGetVertexClass() {
		// TODO Auto-generated method stub
	}

	@Test
	public void testGetVertexClasses() {
		// TODO Auto-generated method stub
	}

	@Test
	public void testKnows() {
		// TODO Auto-generated method stub
	}

	@Test
	public void testKnowsOwn() {
		// TODO Auto-generated method stub
	}

	@Override
	@Test
	public void testToString() {
		// TODO Auto-generated method stub
	}
}
