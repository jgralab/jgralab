package de.uni_koblenz.jgralabtest.schematest.attributedelementtest;

import java.util.Vector;

import org.junit.Before;
import org.junit.Test;

import de.uni_koblenz.jgralab.schema.AttributedElementClass;

public final class GraphClassImplTest extends AttributedElementClassImplTest {

	@Before
	@Override
	public void setUp() {
		super.setUp();
		attributedElement = graphClass;
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
