package de.uni_koblenz.jgralabtest.schematest.attributedelementtest;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;

public final class GraphClassImplTest extends AttributedElementClassImplTest {

	@Before
	@Override
	public void setUp() {
		super.setUp();
		attributedElement = graphClass;
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
		/*
		 * NOTE: As a GraphClassImpl can´t have superclasses (except the
		 * DefaultGraphClass), this case can´t be covered.
		 */
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
		/*
		 * NOTE: As a GraphClassImpl can´t have subclasses, this case can´t be
		 * covered.
		 */
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
		/*
		 * NOTE: As a GraphClassImpl can´t have superclasses (except the
		 * DefaultGraphClass), this case can´t be covered.
		 */
	}

	/**
	 * getAllSubClasses()
	 *
	 * TEST CASE: Getting all subclasses of an element with one direct subclass
	 */
	@Test
	@Override
	public void testGetAllSubClasses() {
		/*
		 * NOTE: As a GraphClassImpl can´t have subclasses, this case can´t be
		 * covered.
		 */
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
		/*
		 * NOTE: As a GraphClassImpl can´t have subclasses, this case can´t be
		 * covered.
		 */
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
		/*
		 * NOTE: As a GraphClassImpl can´t have subclasses, this case can´t be
		 * covered.
		 */
	}

	/**
	 * getAllSubClasses()
	 *
	 * TEST CASE: Getting all subclasses of an element that has no subclasses
	 */
	@Test
	@Override
	public void testGetAllSubClasses4() {
		/*
		 * NOTE: A virgin GraphClass has no subclasses.
		 */
		Assert.assertEquals(0, attributedElement.getAllSubClasses().size());
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
		/*
		 * NOTE: As a GraphClassImpl can´t have superclasses (except the
		 * DefaultGraphClass), this case can´t be covered.
		 */
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
		/*
		 * NOTE: As a GraphClassImpl can´t have superclasses (except the
		 * DefaultGraphClass), this case can´t be covered.
		 */
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
		/*
		 * NOTE: As a GraphClassImpl can´t have superclasses (except the
		 * DefaultGraphClass), this case can´t be covered.
		 */
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
		/*
		 * This test is actually possible, as a virgin GraphClass has a default
		 * superclass.
		 */
		Assert.assertEquals(1, graphClass.getAllSuperClasses().size());
	}

	// /*
	// * Tests for the createAggregationClass method Note: Tests are only
	// * performed on the createAggregationClass with the full attribute
	// * magnitude. This is due to the fact that all other incarnations of this
	// * method, only call the maxed-out createAggregationClass method, filling
	// in
	// * the missing arguments with default values
	// */
	//
	// /**
	// * Testing the createAggregationClass method. Test-type: valid
	// * Test-description: creating an AggregationClass providing valid
	// parameters
	// */
	// @Test
	// public void testCreateAggregationClass_Valid() {
	// // Test prerequisites
	// AggregationClass aggregationClass;
	// QualifiedName aggregationClassQName = new QualifiedName(
	// "AnotherAggregationClass");
	// VertexClass from = graphClass.createVertexClass(new QualifiedName(
	// "FromVertexClass"));
	// int fromMin = 0, fromMax = 1;
	// String fromRoleName = "fromRoleName";
	// boolean aggregateFrom = true;
	// VertexClass to = graphClass.createVertexClass(new QualifiedName(
	// "ToVertexClass"));
	// int toMin = 0, toMax = 1;
	// String toRoleName = "toRoleName";
	//
	// // Perform operation to be tested
	// aggregationClass = graphClass.createAggregationClass(
	// aggregationClassQName, from, fromMin, fromMax, fromRoleName,
	// aggregateFrom, to, toMin, toMax, toRoleName);
	//
	// // Test postconditions
	// // 1. Postcondition: graphClass must have one issue of the freshly
	// // created AggregationClass
	// Assert.assertNotNull(aggregationClass);
	// Assert.assertNotNull(graphClass
	// .getAggregationClass(aggregationClassQName));
	// }
	//
	// /**
	// * Testing the createAggregationClass method. Test-type: valid
	// * Test-description: creating an AggregationClass where the aggregation
	// * start and end vertices coincide
	// */
	// @Test
	// public void testCreateAggregationClass_Valid2() {
	// // Test prerequisites
	// AggregationClass aggregationClass;
	// QualifiedName aggregationClassQName = new QualifiedName(
	// "AnotherAggregationClass");
	// VertexClass from = graphClass.createVertexClass(new QualifiedName(
	// "FromVertexClass"));
	// int fromMin = 0, fromMax = 1;
	// String fromRoleName = "fromRoleName";
	// boolean aggregateFrom = true;
	// // Set the end of the aggregation to be the start vertex
	// VertexClass to = from;
	// int toMin = 0, toMax = 1;
	// String toRoleName = "toRoleName";
	//
	// // Perform operation to be tested
	// aggregationClass = graphClass.createAggregationClass(
	// aggregationClassQName, from, fromMin, fromMax, fromRoleName,
	// aggregateFrom, to, toMin, toMax, toRoleName);
	//
	// // Test postconditions
	// // 1. Postcondition: graphClass must have one issue of the freshly
	// // created AggregationClass
	// Assert.assertNotNull(aggregationClass);
	// Assert.assertNotNull(graphClass
	// .getAggregationClass(aggregationClassQName));
	// }
	//
	// /**
	// * Testing the createAggregationClass method. Test-type: invalid
	// * Test-description: providing a name already in use
	// */
	// @Test(expected = DuplicateNamedElementException.class)
	// public void testCreateAggregationClass_Invalid() {
	// // Test prerequisites
	// AggregationClass aggregationClass;
	// QualifiedName aggregationClassQName = new QualifiedName(
	// "AnotherAggregationClass");
	// VertexClass from = graphClass.createVertexClass(new QualifiedName(
	// "FromVertexClass"));
	// int fromMin = 0, fromMax = 1;
	// String fromRoleName = "fromRoleName";
	// boolean aggregateFrom = true;
	// VertexClass to = graphClass.createVertexClass(new QualifiedName(
	// "ToVertexClass"));
	// int toMin = 0, toMax = 1;
	// String toRoleName = "toRoleName";
	//
	// // Create the aggregation class once
	// graphClass.createAggregationClass(aggregationClassQName, from, fromMin,
	// fromMax, fromRoleName, aggregateFrom, to, toMin, toMax,
	// toRoleName);
	//
	// // Perform operation to be tested
	// // Try to create an aggregation class with a name already in use
	//
	// aggregationClass = graphClass.createAggregationClass(
	// aggregationClassQName, from, fromMin, fromMax, fromRoleName,
	// aggregateFrom, to, toMin, toMax, toRoleName);
	// }
	//
	// /**
	// * Testing the createAggregationClass method. Test-type: invalid
	// * Test-description: providing a name that contains a reserved TG and/or
	// * Java word
	// */
	// @Test(expected = ReservedWordException.class)
	// public void testCreateAggregationClass_Invalid2() {
	// // Test prerequisites
	// AggregationClass aggregationClass;
	// // Name containing a reserved Java Word
	// QualifiedName aggregationClassQName = new QualifiedName("abstract");
	// VertexClass from = graphClass.createVertexClass(new QualifiedName(
	// "FromVertexClass"));
	// int fromMin = 0, fromMax = 1;
	// String fromRoleName = "fromRoleName";
	// boolean aggregateFrom = true;
	// VertexClass to = graphClass.createVertexClass(new QualifiedName(
	// "ToVertexClass"));
	// int toMin = 0, toMax = 1;
	// String toRoleName = "toRoleName";
	//
	// // Perform operation to be tested
	// aggregationClass = graphClass.createAggregationClass(
	// aggregationClassQName, from, fromMin, fromMax, fromRoleName,
	// aggregateFrom, to, toMin, toMax, toRoleName);
	// }
	//
	// /**
	// * Testing the createAggregationClass method. Test-type: invalid
	// * Test-description: to and/or from VertexClasses are not known by
	// * graphClass
	// */
	// @Test(expected = InheritanceException.class)
	// public void testCreateAggregationClass_Invalid3() {
	// // Test prerequisites
	// AggregationClass aggregationClass;
	// // Name containing a reserved Java Word
	// QualifiedName aggregationClassQName = new QualifiedName(
	// "AnotherAggregationClass");
	// VertexClassImpl from = new VertexClassImpl(new QualifiedName(
	// "FromVertexClass"), graphClass);
	// int fromMin = 0, fromMax = 1;
	// String fromRoleName = "fromRoleName";
	// boolean aggregateFrom = true;
	// VertexClass to = new VertexClassImpl(
	// new QualifiedName("ToVertexClass"), graphClass);
	// int toMin = 0, toMax = 1;
	// String toRoleName = "toRoleName";
	//
	// // Perform operation to be tested
	// aggregationClass = graphClass.createAggregationClass(
	// aggregationClassQName, from, fromMin, fromMax, fromRoleName,
	// aggregateFrom, to, toMin, toMax, toRoleName);
	// }

	@Test
	public void testCreateAggregationClass4() {

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
