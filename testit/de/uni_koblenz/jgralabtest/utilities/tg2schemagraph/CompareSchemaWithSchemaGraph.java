package de.uni_koblenz.jgralabtest.utilities.tg2schemagraph;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertTrue;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.Map.Entry;

import de.uni_koblenz.jgralab.EdgeDirection;
import de.uni_koblenz.jgralab.Vertex;
import de.uni_koblenz.jgralab.WorkInProgress;
import de.uni_koblenz.jgralab.grumlschema.SchemaGraph;
import de.uni_koblenz.jgralab.grumlschema.domains.CollectionDomain;
import de.uni_koblenz.jgralab.grumlschema.domains.Domain;
import de.uni_koblenz.jgralab.grumlschema.domains.EnumDomain;
import de.uni_koblenz.jgralab.grumlschema.domains.HasBaseDomain;
import de.uni_koblenz.jgralab.grumlschema.domains.HasKeyDomain;
import de.uni_koblenz.jgralab.grumlschema.domains.HasRecordDomainComponent;
import de.uni_koblenz.jgralab.grumlschema.domains.HasValueDomain;
import de.uni_koblenz.jgralab.grumlschema.domains.MapDomain;
import de.uni_koblenz.jgralab.grumlschema.domains.RecordDomain;
import de.uni_koblenz.jgralab.grumlschema.structure.AggregationClass;
import de.uni_koblenz.jgralab.grumlschema.structure.Attribute;
import de.uni_koblenz.jgralab.grumlschema.structure.AttributedElementClass;
import de.uni_koblenz.jgralab.grumlschema.structure.CompositionClass;
import de.uni_koblenz.jgralab.grumlschema.structure.Constraint;
import de.uni_koblenz.jgralab.grumlschema.structure.ContainsDefaultPackage;
import de.uni_koblenz.jgralab.grumlschema.structure.ContainsDomain;
import de.uni_koblenz.jgralab.grumlschema.structure.ContainsGraphElementClass;
import de.uni_koblenz.jgralab.grumlschema.structure.ContainsSubPackage;
import de.uni_koblenz.jgralab.grumlschema.structure.DefinesGraphClass;
import de.uni_koblenz.jgralab.grumlschema.structure.EdgeClass;
import de.uni_koblenz.jgralab.grumlschema.structure.From;
import de.uni_koblenz.jgralab.grumlschema.structure.GraphClass;
import de.uni_koblenz.jgralab.grumlschema.structure.GraphElementClass;
import de.uni_koblenz.jgralab.grumlschema.structure.HasAttribute;
import de.uni_koblenz.jgralab.grumlschema.structure.HasConstraint;
import de.uni_koblenz.jgralab.grumlschema.structure.HasDomain;
import de.uni_koblenz.jgralab.grumlschema.structure.Package;
import de.uni_koblenz.jgralab.grumlschema.structure.Schema;
import de.uni_koblenz.jgralab.grumlschema.structure.SpecializesEdgeClass;
import de.uni_koblenz.jgralab.grumlschema.structure.SpecializesVertexClass;
import de.uni_koblenz.jgralab.grumlschema.structure.To;
import de.uni_koblenz.jgralab.grumlschema.structure.VertexClass;
import de.uni_koblenz.jgralab.schema.BasicDomain;

/**
 * Compares a given Schema and SchemaGraph with each other.
 * 
 * Note:
 * 
 * This class exists only for test purposes. Because of the use of a lot of
 * Assert of JUnit, it will crash without catching these exceptions.
 * 
 * @author mmce, Eckhard Großmann
 */
@WorkInProgress(responsibleDevelopers = "mmce, Eckhard Großmann", expectedFinishingDate = "2009-04-29")
public class CompareSchemaWithSchemaGraph {

	/**
	 * Only used EdgeDirection in this comparison.
	 */
	private static final EdgeDirection OUTGOING = EdgeDirection.OUT;

	/**
	 * Schema, which is compared.
	 */
	private de.uni_koblenz.jgralab.schema.Schema schema;

	/**
	 * Empty constructor.
	 */
	public CompareSchemaWithSchemaGraph() {
	}

	/**
	 * Compares the given Schema with the given SchemaGraph.
	 * 
	 * @param schema
	 *            Schema, which should be compared.
	 * @param schemaGraph
	 *            SchemaGraph, which should be compared.
	 */
	final public void compare(de.uni_koblenz.jgralab.schema.Schema schema,
			SchemaGraph schemaGraph) {

		this.schema = schema;

		compareSchema(schema, schemaGraph.getFirstSchema());
	}

	/**
	 * Compares a Schema and a Schema from a SchemaGraph with each other.
	 * 
	 * @param schema
	 *            Schema, which is compared.
	 * @param gSchema
	 *            Schema of a SchemaGraph, which is compared.
	 */
	final public void compareSchema(
			de.uni_koblenz.jgralab.schema.Schema schema, Schema gSchema) {

		// Compares there names and package prefixes
		assertEquals("Both Schema objects have a different name.", schema
				.getName(), gSchema.getName());
		assertEquals("Both Schema objects have a different package prefix.",
				schema.getPackagePrefix(), gSchema.getPackagePrefix());

		// GRAPHCLASS

		// Get the only defined GraphClass in the SchemaGraph
		DefinesGraphClass definesGraphClass = gSchema
				.getFirstDefinesGraphClass(OUTGOING);
		// There should be one GraphClass
		assertTrue("No GraphClass is defined.", definesGraphClass != null);
		Vertex vertex = definesGraphClass.getThat();
		assertTrue("That should be an instance of \"GraphClass\".",
				vertex instanceof GraphClass);
		assertFalse("There is more than one GraphClass defined.",
				definesGraphClass.getNextDefinesGraphClass(OUTGOING) != null);

		// Compares both GraphClass objects
		compareAttributedElementClass(schema.getGraphClass(),
				(GraphClass) vertex);

		// DEFAULTPACKAGE

		// Gets the only defined DefaultPackage
		ContainsDefaultPackage containsDefaultPackage = gSchema
				.getFirstContainsDefaultPackage(OUTGOING);
		// There should be one DefaultPackage
		assertTrue("No DefaultPackage is defined.",
				containsDefaultPackage != null);
		vertex = containsDefaultPackage.getThat();
		assertFalse(
				"There is more than one DefaultPackage defined.",
				containsDefaultPackage.getNextContainsDefaultPackage(OUTGOING) != null);
		assertTrue("That should be an instance of \"Package\".",
				vertex instanceof Package);
		// Compares both Package objects with each other.
		comparePackage(schema.getDefaultPackage(), (Package) vertex);
	}

	/**
	 * Compares a Package of a Schema and a Package of a SchemaGraph with each
	 * other.
	 * 
	 * It also compares all subpackages.
	 * 
	 * @param xPackage
	 *            Package, which should be compared.
	 * @param gPackage
	 *            Package of a SchemaGraph, which should be compared.
	 */
	final private void comparePackage(
			de.uni_koblenz.jgralab.schema.Package xPackage, Package gPackage) {

		// Comparison of the QualifiedName
		assertEquals("Both Package objects have a different name.", xPackage
				.getQualifiedName(), gPackage.getQualifiedName());

		// DOMAINS

		compareAllDomains(xPackage, gPackage);

		// GRAPHELEMENTCLASS

		compareAllGraphElementClasses(xPackage, gPackage);

		// SUBPACKAGES

		compareAllSubPackages(xPackage, gPackage);
	}

	/**
	 * Compares all Domain objects of two Package objects.
	 * 
	 * @param xPackage
	 *            Package from the Schema, of which all Domain objects are
	 *            compared.
	 * @param gPackage
	 *            Package from the SchemaGraph, of which all Domain objects are
	 *            compared.
	 */
	final private void compareAllDomains(
			de.uni_koblenz.jgralab.schema.Package xPackage, Package gPackage) {
		// Gets all Domains (clone of the map) of a Schema
		Map<String, de.uni_koblenz.jgralab.schema.Domain> domains = new HashMap<String, de.uni_koblenz.jgralab.schema.Domain>(
				xPackage.getDomains());

		// Loop over all ContainsDomain edges
		for (ContainsDomain containsDomain : gPackage
				.getContainsDomainIncidences(OUTGOING)) {
			// Checking if the reference is right
			assertTrue("Omega should be an instance of Domain.", containsDomain
					.getOmega() instanceof Domain);
			Domain gDomain = (Domain) containsDomain.getOmega();

			// Gets the simpleName for querying a the right domain
			String qualifiedName = schema.getDomain(gDomain.getQualifiedName())
					.getSimpleName();

			// gQualifiedName = removeWhiteSpaces(gQualifiedName);
			// qualifiedName = removeWhiteSpaces(qualifiedName);

			// Gets, removes and compare at the same time both Domain objects.
			compareDomain(domains.remove(qualifiedName), gDomain);
		}

		// TODO - It seems, that the SchemaGraph is malformed. Not all standard
		// domains are included.
		// THIS IS A FIX ----
		// Loop over all left over Domain objects
		for (Iterator<Entry<String, de.uni_koblenz.jgralab.schema.Domain>> it = domains
				.entrySet().iterator(); it.hasNext();) {

			de.uni_koblenz.jgralab.schema.Domain domain = it.next().getValue();
			// In the case of a Domain in the DefaultPackage and an instance of
			// Boolean-, Int-, Long-, Double- or StringDomain
			if (domain.isInDefaultPackage() && (domain instanceof BasicDomain)) {
				// Remove the Domain
				it.remove();
			}
		}

		// After all this, the Domain map should be empty
		assertTrue(
				"There are more Domains in the Schema then in the SchemaGraph.",
				domains.isEmpty());
	}

	/**
	 * Compares all GraphElementClass objects in two Packages.
	 * 
	 * @param xPackage
	 *            Package from the Schema, of which all GraphElementClass
	 *            objects should be compared.
	 * @param gPackage
	 *            Package from the SchemaGraph, of which all GraphElementClass
	 *            objects should be compared.
	 */
	final private void compareAllGraphElementClasses(
			de.uni_koblenz.jgralab.schema.Package xPackage, Package gPackage) {
		// Gets a cloned map of all VertexClass and EdgeClass objects
		Map<String, de.uni_koblenz.jgralab.schema.VertexClass> vertexClasses = new HashMap<String, de.uni_koblenz.jgralab.schema.VertexClass>(
				xPackage.getVertexClasses());
		Map<String, de.uni_koblenz.jgralab.schema.EdgeClass> edgeClasses = new HashMap<String, de.uni_koblenz.jgralab.schema.EdgeClass>(
				xPackage.getEdgeClasses());

		// This loop prevents the comparison of internal structures
		for (Iterator<Entry<String, de.uni_koblenz.jgralab.schema.VertexClass>> it = vertexClasses
				.entrySet().iterator(); it.hasNext();) {
			if (it.next().getValue().isInternal()) {
				it.remove();
			}
		}

		// This loop prevents the comparison of internal structures
		for (Iterator<Entry<String, de.uni_koblenz.jgralab.schema.EdgeClass>> it = edgeClasses
				.entrySet().iterator(); it.hasNext();) {
			if (it.next().getValue().isInternal()) {
				it.remove();
			}
		}

		// Loop over all ContainsGraphElementClass edges
		for (ContainsGraphElementClass containsGraphElementClass : gPackage
				.getContainsGraphElementClassIncidences(OUTGOING)) {

			// The referenced object should be at least a FraphElementClass
			assertTrue(
					"Omega should be an instance of GraphElementClass.",
					containsGraphElementClass.getOmega() instanceof GraphElementClass);

			// Distinguishing between VertexClass and EdgeClass
			if (containsGraphElementClass.getOmega() instanceof VertexClass) {
				VertexClass gVertexClass = (VertexClass) containsGraphElementClass
						.getOmega();
				// Retrieving the simple name of the corresponding VertexClass
				String simpleName = schema.getAttributedElementClass(
						gVertexClass.getQualifiedName()).getSimpleName();
				// Queries, removes and compares at the same time two
				// VertexClass objects
				compareVertexClass(vertexClasses.remove(simpleName),
						gVertexClass);
			} else {
				// The Same for the EdgeClass comparison
				EdgeClass gEdgeClass = (EdgeClass) containsGraphElementClass
						.getOmega();
				// Retrieving the simple name of the corresponding VertexClass
				String simpleName = schema.getAttributedElementClass(
						gEdgeClass.getQualifiedName()).getSimpleName();
				// Queries, removes and compares at the same time two EdgeClass
				// objects
				compareEdgeClass(edgeClasses.remove(simpleName), gEdgeClass);
			}
		}

		// Both maps should be empty.
		assertTrue(
				"There are more VertexClasses in Schema then in the SchemaGraph.",
				vertexClasses.isEmpty());
		assertTrue(
				"There are more EdgeClasses in Schema then in the SchemaGraph.",
				edgeClasses.isEmpty());
	}

	/**
	 * Compares all SubPackage objects of two Package objects.
	 * 
	 * @param xPackage
	 *            Package from the Schema, of which all Packages should be
	 *            compared.
	 * @param gPackage
	 *            Package from the SchemaGraph, of which all Packages should be
	 *            compared.
	 */
	final private void compareAllSubPackages(
			de.uni_koblenz.jgralab.schema.Package xPackage, Package gPackage) {
		// Map of SubPackages is cloned
		Map<String, de.uni_koblenz.jgralab.schema.Package> subPackages = new HashMap<String, de.uni_koblenz.jgralab.schema.Package>(
				xPackage.getSubPackages());

		// Loop over all ContainsSubPackage edges
		for (ContainsSubPackage containsSubPackage : gPackage
				.getContainsSubPackageIncidences(OUTGOING)) {

			assertTrue("Omega should be an instance of \"Package\".",
					containsSubPackage.getOmega() instanceof Package);
			Package gSubPackage = (Package) containsSubPackage.getOmega();
			de.uni_koblenz.jgralab.schema.Package subpackage = schema
					.getPackage(gSubPackage.getQualifiedName());

			// The references shouldn't be null
			assertTrue("There is no corresponding Package in Schema.",
					subpackage != null);
			assertTrue("There is no corresponding Package in Schema.",
					subPackages.containsKey(subpackage.getSimpleName()));

			// Gets, removes and compares both Package objects with each other
			comparePackage(subPackages.remove(subpackage.getSimpleName()),
					gSubPackage);
		}
		// There shouldn't be any Package left in the map
		assertTrue(
				"There are more Packages in the Schema then in the SchemaGraph.",
				subPackages.isEmpty());
	}

	/**
	 * Compares two Domains with each other.
	 * 
	 * @param domain
	 *            Domain from the Schema, which should be compared.
	 * @param gDomain
	 *            Domain from the SchemaGraph, which should be compared.
	 */
	final private void compareDomain(
			de.uni_koblenz.jgralab.schema.Domain domain, Domain gDomain) {

		// Compares the QualifiedName
		assertEquals("Both Domain objects should have a equal QualifiedName",
				domain.getQualifiedName(), gDomain.getQualifiedName());

		// Differentiated comparison of different Domain types
		if (domain instanceof de.uni_koblenz.jgralab.schema.MapDomain
				&& gDomain instanceof MapDomain) {

			compareDomain((de.uni_koblenz.jgralab.schema.MapDomain) domain,
					(MapDomain) gDomain);

		} else if (domain instanceof de.uni_koblenz.jgralab.schema.RecordDomain
				&& gDomain instanceof RecordDomain) {

			compareDomain((de.uni_koblenz.jgralab.schema.RecordDomain) domain,
					(RecordDomain) gDomain);

		} else if (domain instanceof de.uni_koblenz.jgralab.schema.CollectionDomain
				&& gDomain instanceof CollectionDomain) {

			compareDomain(
					(de.uni_koblenz.jgralab.schema.CollectionDomain) domain,
					(CollectionDomain) gDomain);

		} else if (domain instanceof de.uni_koblenz.jgralab.schema.EnumDomain
				&& gDomain instanceof EnumDomain) {

			compareDomain((de.uni_koblenz.jgralab.schema.EnumDomain) domain,
					(EnumDomain) gDomain);
		}
	}

	/**
	 * Compares two RecordDomain objects with each other.
	 * 
	 * @param domain
	 *            RecordDomain from Schema, which should be compared.
	 * @param gDomain
	 *            RecordDomain from SchemaGraph, which should be compared.
	 */
	final private void compareDomain(
			de.uni_koblenz.jgralab.schema.RecordDomain domain,
			RecordDomain gDomain) {

		// Clones a map of Components
		Map<String, de.uni_koblenz.jgralab.schema.Domain> components = new HashMap<String, de.uni_koblenz.jgralab.schema.Domain>(
				domain.getComponents());

		// Loop over all HasRecordDomainComponent edges
		for (HasRecordDomainComponent hasRecordDomainComponent : gDomain
				.getHasRecordDomainComponentIncidences(OUTGOING)) {

			assertTrue("Omega should be an instance of Domain.",
					hasRecordDomainComponent.getOmega() instanceof Domain);
			// Gets the Domain
			Domain domainComponent = (Domain) hasRecordDomainComponent
					.getOmega();

			// Get and removes the Domain and compares.
			// The comparison of the Component name is missed out, because it is
			// implicitly done.
			assertEquals("Both DomainComponents don't have an equal name.",
					components.remove(hasRecordDomainComponent.getName())
							.getQualifiedName(), domainComponent
							.getQualifiedName());
		}
		// The map should be empty or there are some components left over
		assertTrue("There are more Components in Schema then in SchemaGraph",
				components.isEmpty());
	}

	/**
	 * Compares tow MapDomain objects with each other.
	 * 
	 * @param domain
	 *            MapDomain from the Schema, which should be compared.
	 * @param gDomain
	 *            MapDomain form the SchemaGraph, which should be compared.
	 */
	final private void compareDomain(
			de.uni_koblenz.jgralab.schema.MapDomain domain, MapDomain gDomain) {

		// KEY DOMAIN
		HasKeyDomain hasKeyDomain = gDomain.getFirstHasKeyDomain(OUTGOING);
		assertTrue("There is no key Domain defined.", hasKeyDomain != null);
		Vertex vertex = hasKeyDomain.getThat();
		assertTrue("That should be an instance of Domain.",
				vertex instanceof Domain);
		assertFalse("There is more than one key Domain.", hasKeyDomain
				.getNextHasKeyDomain(OUTGOING) != null);
		Domain gKeyDomain = (Domain) vertex;

		// Compares the QualifiedName of the key domain
		assertEquals(
				"Both key Domain objects should have the same QualifiedName.",
				domain.getKeyDomain().getQualifiedName(), gKeyDomain
						.getQualifiedName());

		// VALUE DOMAIN
		HasValueDomain hasValueDomain = gDomain
				.getFirstHasValueDomain(OUTGOING);
		assertTrue("There is no value Domain defined.", hasValueDomain != null);
		vertex = hasValueDomain.getThat();
		assertTrue("That should be an instance of Domain.",
				vertex instanceof Domain);
		assertFalse("There is more than one value Domain.", hasValueDomain
				.getNextHasValueDomain(OUTGOING) != null);
		Domain gValueDomain = (Domain) vertex;

		// Compares the QualifiedName
		assertEquals(
				"Both value Domain objects should have an equal QualifiedName.",
				domain.getValueDomain().getQualifiedName(), gValueDomain
						.getQualifiedName());
	}

	/**
	 * Compares two CollectionDomain objects with each other.
	 * 
	 * @param domain
	 *            CollectionDomain from the Schema, which should be compared.
	 * @param gDomain
	 *            CollectionDomain from the SchemaGraph, which should be
	 *            compared.
	 */
	final private void compareDomain(
			de.uni_koblenz.jgralab.schema.CollectionDomain domain,
			CollectionDomain gDomain) {

		// BASE DOMAIN
		HasBaseDomain hasBaseDomain = gDomain.getFirstHasBaseDomain(OUTGOING);
		assertTrue("There should be a base Domain.", hasBaseDomain != null);
		Vertex vertex = hasBaseDomain.getThat();
		assertTrue("That should be an instance of Domain.",
				vertex instanceof Domain);
		assertFalse("There is more than one base Domain.", hasBaseDomain
				.getNextHasBaseDomain(OUTGOING) != null);
		Domain gBaseDomain = (Domain) vertex;

		// Compares the QualifiedName
		assertEquals(
				"Both base Domain objects should have an equal QualifiedName.",
				domain.getBaseDomain().getQualifiedName(), gBaseDomain
						.getQualifiedName());
	}

	/**
	 * Compares two EnumDomain objects with each other.
	 * 
	 * @param domain
	 *            EnumDomain from the Schema, which should be compared.
	 * @param gDomain
	 *            EnumDomain from the SchemaGraph, which should be compared.
	 */
	final private void compareDomain(
			de.uni_koblenz.jgralab.schema.EnumDomain domain, EnumDomain gDomain) {

		List<String> enumConstants = domain.getConsts();
		List<String> gEnumConstants = gDomain.getEnumConstants();

		assertTrue("The size of enum constants are not equal.", enumConstants
				.size() == gEnumConstants.size());

		assertTrue("Not all Constants are included.", gEnumConstants
				.containsAll(enumConstants));
		assertTrue("Not all Constants are included.", enumConstants
				.containsAll(gEnumConstants));
	}

	/**
	 * Compares two AttributedElementClass with each other.
	 * 
	 * @param element
	 *            An AttributedElementClass from the Schema, which should be
	 *            compared.
	 * @param gElement
	 *            An AttributedElementClass from the SchemaGraph, which should
	 *            be compared.
	 */
	final private void compareAttributedElementClass(
			de.uni_koblenz.jgralab.schema.AttributedElementClass element,
			AttributedElementClass gElement) {

		// Comparing the attribute \"isAbstract\"
		assertEquals("Attribute \"isAbstract\" is different.", element
				.isAbstract(), gElement.isIsAbstract());
		// Comparing the QualifiedName
		assertEquals("Attribute \"qualifiedName\" is different.", element
				.getQualifiedName(), gElement.getQualifiedName());

		// Comparing all other Attributes and Constraints
		compareAttributes(element, gElement);
		compareConstraints(element, gElement);

	}

	/**
	 * Compares two VertexClass objects with each other.
	 * 
	 * @param vertexClass
	 *            VertexClass from the Schema, which should be compared.
	 * @param gVertexClass
	 *            VertexClass from the SchemaGraph, which should be compared.
	 */
	final private void compareVertexClass(
			de.uni_koblenz.jgralab.schema.VertexClass vertexClass,
			VertexClass gVertexClass) {

		// A VertexClass is a AttributedElementClass, so reuse the
		// AttributeElemenClass method.
		compareAttributedElementClass(vertexClass, gVertexClass);
		// Creates a clone Map of AttributedElementClass objects.
		Map<String, de.uni_koblenz.jgralab.schema.AttributedElementClass> superClasses = getAttributedElementClassMap(vertexClass
				.getDirectSuperClasses());

		// This loop drops all already internal used objects.
		for (Iterator<Entry<String, de.uni_koblenz.jgralab.schema.AttributedElementClass>> it = superClasses
				.entrySet().iterator(); it.hasNext();) {
			if (it.next().getValue().isInternal()) {
				it.remove();
			}
		}

		// Loop over all SpecializesVertexClass edges
		for (SpecializesVertexClass specializesVertexClass : gVertexClass
				.getSpecializesVertexClassIncidences(OUTGOING)) {
			AttributedElementClass element = (AttributedElementClass) specializesVertexClass
					.getOmega();
			// It gets, removes and compare the QualifiedNames
			assertEquals(
					"SuperClasses of these AttributeElementClass objects are different.",
					superClasses.remove(element.getQualifiedName())
							.getQualifiedName(), element.getQualifiedName());
		}

		// The map should be empty after the comparison.
		assertTrue(
				"There are more SuperClasses in the AttibuteElement of the Schema then in the SchemaGraph.",
				superClasses.isEmpty());
	}

	/**
	 * Compares two EdgeClass objects with each other.
	 * 
	 * @param edgeClass
	 *            EdgeClass from the Schema, which should be compared.
	 * @param gEdgeClass
	 *            EdgeClass from the SchemaGraph, which should be compared.
	 */
	final private void compareEdgeClass(
			de.uni_koblenz.jgralab.schema.EdgeClass edgeClass,
			EdgeClass gEdgeClass) {

		// Reuse of the compareAttributedElementClass method
		compareAttributedElementClass(edgeClass, gEdgeClass);

		// Creates a map out of an set of AttributedElementClass objects.
		Map<String, de.uni_koblenz.jgralab.schema.AttributedElementClass> superClasses = getAttributedElementClassMap(edgeClass
				.getDirectSuperClasses());

		// Drops all internally used objects
		for (Iterator<Entry<String, de.uni_koblenz.jgralab.schema.AttributedElementClass>> it = superClasses
				.entrySet().iterator(); it.hasNext();) {
			if (it.next().getValue().isInternal()) {
				it.remove();
			}
		}

		// Loop over all SpecializesEdgeClass edges
		for (SpecializesEdgeClass specializesEdgeClass : gEdgeClass
				.getSpecializesEdgeClassIncidences(OUTGOING)) {
			AttributedElementClass element = (AttributedElementClass) specializesEdgeClass
					.getOmega();

			// Gets, removes and compares the QualifiedNames
			assertEquals(
					"SuperClasses of these AttributeElementClass objects are different.",
					superClasses.remove(element.getQualifiedName())
							.getQualifiedName(), element.getQualifiedName());
		}

		// After the comparison the map should be empty
		assertTrue(
				"There are more SuperClasses in the AttibuteElement of the Schema then in the SchemaGraph.",
				superClasses.isEmpty());

		// "To" and "From" edges are compared
		compareToEdge(edgeClass, gEdgeClass);
		compareFromEdge(edgeClass, gEdgeClass);

		// Both objects should be instnaces from the same class
		assertEquals(
				"These objects should have the same Type.",
				edgeClass instanceof de.uni_koblenz.jgralab.schema.AggregationClass,
				gEdgeClass instanceof AggregationClass);

		assertEquals(
				"These objects should have the same Type.",
				edgeClass instanceof de.uni_koblenz.jgralab.schema.CompositionClass,
				gEdgeClass instanceof CompositionClass);

		// For the case of an instance of the AggregationClass or
		// CompositionClass the aggregateFrom attribute is compared
		if (gEdgeClass instanceof AggregationClass) {
			de.uni_koblenz.jgralab.schema.AggregationClass aggregationClass = (de.uni_koblenz.jgralab.schema.AggregationClass) edgeClass;
			AggregationClass gAggregationClass = (AggregationClass) gEdgeClass;
			assertEquals("These to edges are aggregated different.",
					aggregationClass.isAggregateFrom(), gAggregationClass
							.isAggregateFrom());
		}
	}

	/**
	 * Compares two To edges of two EdgeClass objects.
	 * 
	 * @param edgeClass
	 *            EdgeClass from the Schema, of which the To edge should be
	 *            compared.
	 * @param gEdgeClass
	 *            EdgeClass from the SchemaGraph, of which the To edge should be
	 *            compared.
	 */
	final private void compareToEdge(
			de.uni_koblenz.jgralab.schema.EdgeClass edgeClass,
			EdgeClass gEdgeClass) {
		VertexClass vertexClass;
		// TO
		To to = gEdgeClass.getFirstTo(OUTGOING);
		// There should be one To edge
		assertTrue("There is no \"To\" edge defined.", to != null);
		// Checking if there are more than one To edge
		assertFalse("There are more than one To edge defined.", to
				.getNextTo(OUTGOING) != null);
		assertTrue("That should be an instance of \"VertexClass\".", to
				.getThat() instanceof VertexClass);
		vertexClass = (VertexClass) to.getThat();

		// QualifiedName, min, max and rolename are compared.
		assertEquals("Both \"To\" edges should have the same QualifiedName.",
				edgeClass.getTo().getQualifiedName(), vertexClass
						.getQualifiedName());
		assertEquals("Both \"To\" edges should have the same min value.",
				edgeClass.getToMin(), to.getMin());
		assertEquals("Both \"To\" edges should have the same max value.",
				edgeClass.getToMax(), to.getMax());
		assertEquals("Both \"To\" edges should have the same RoleName.",
				edgeClass.getToRolename(), to.getRoleName());

		// Comparing the redefined Roles
		Set<String> redefinedRoles, gRedefinedRoles;

		// Gets the redefined roles
		redefinedRoles = edgeClass.getRedefinedToRoles();
		gRedefinedRoles = to.getRedefinedRoles();

		compareRedefinedRoles(redefinedRoles, gRedefinedRoles);
	}

	/**
	 * Compares two From edges of two EdgeClass objects.
	 * 
	 * @param edgeClass
	 *            EdgeClass from the Schema, of which the From edge should be
	 *            compared.
	 * @param gEdgeClass
	 *            EdgeClass from the SchemaGraph, of which the From edge should
	 *            be compared.
	 */
	final private void compareFromEdge(
			de.uni_koblenz.jgralab.schema.EdgeClass edgeClass,
			EdgeClass gEdgeClass) {
		VertexClass vertexClass;
		Set<String> redefinedRoles;
		Set<String> gRedefinedRoles;
		// FROM
		From from = gEdgeClass.getFirstFrom(OUTGOING);
		// There should be one From edge
		assertTrue("There is no \"From\" edge defined.", from != null);
		// Checking if there are more than one From edge
		assertFalse("There are more than one From edge defined.", from
				.getNextFrom(OUTGOING) != null);
		assertTrue("Omega should be an instance of \"VertexClass\".", from
				.getThat() instanceof VertexClass);
		vertexClass = (VertexClass) from.getThat();

		// QualifiedName, min, max and rolename are compared.
		assertEquals("Both \"From\" edges should have the same QualifiedName.",
				edgeClass.getFrom().getQualifiedName(), vertexClass
						.getQualifiedName());
		assertEquals("Both \"From\" edges should have the same min value.",
				edgeClass.getFromMin(), from.getMin());
		assertEquals("Both \"From\" edges should have the same max value.",
				edgeClass.getFromMax(), from.getMax());
		assertEquals("Both \"From\" edges should have the same RoleName.",
				edgeClass.getFromRolename(), from.getRoleName());

		// Gets the redefined roles
		redefinedRoles = edgeClass.getRedefinedFromRoles();
		gRedefinedRoles = from.getRedefinedRoles();

		compareRedefinedRoles(redefinedRoles, gRedefinedRoles);
	}

	/**
	 * Compares two sets of RedefinedRoles with each other.
	 * 
	 * @param redefinedRoles
	 *            Set of RedefinedRoles of an edge from the Schema.
	 * @param gRedefinedRoles
	 *            Set of RedefinedRoles of an edge from the SchemaGraph.
	 */
	final private void compareRedefinedRoles(Set<String> redefinedRoles,
			Set<String> gRedefinedRoles) {
		// If the set of redefined Roles are not empty
		if (redefinedRoles != null && redefinedRoles.size() > 0
				&& gRedefinedRoles != null) {

			// Comparing all both sets
			assertTrue("", redefinedRoles.containsAll(gRedefinedRoles));
			assertTrue("", gRedefinedRoles.containsAll(redefinedRoles));
		} else {

			assertTrue("Wrong conversion!", redefinedRoles != null
					&& redefinedRoles.size() == 0 && gRedefinedRoles == null);
		}
	}

	/**
	 * Compares all Attribute objects of two AttributedElementClass objects with
	 * each other.
	 * 
	 * @param element
	 *            AttributedElementClass from the Schema, of which all Attribute
	 *            objects should be compared.
	 * @param gElement
	 *            AttributedElementClass from the SchemaGraph, of which all
	 *            Attribute objects should be compared.
	 */
	final private void compareAttributes(
			de.uni_koblenz.jgralab.schema.AttributedElementClass element,
			AttributedElementClass gElement) {

		// Clone the map of Attribute objects.
		Map<String, de.uni_koblenz.jgralab.Attribute> attributes = new HashMap<String, de.uni_koblenz.jgralab.Attribute>(
				getAttributeMap(element.getOwnAttributeList()));

		// Loop over all HasAttribute edges
		for (HasAttribute hasAttribute : gElement
				.getHasAttributeIncidences(OUTGOING)) {
			// Gets the Attribute
			assertTrue("Omega should be an instance of Attribute.",
					hasAttribute.getOmega() instanceof Attribute);
			Attribute gAttribute = (Attribute) hasAttribute.getThat();
			// Checks if the Attribute object has an corresponding object in the
			// map

			assertTrue("Attribute is not include in the AttributeMap.",
					attributes.containsKey(gAttribute.getName()));

			// Get the Domain
			HasDomain hasDomain = gAttribute.getFirstHasDomain(OUTGOING);
			assertTrue("There is no Domain defined.", hasDomain != null);
			Vertex vertex = hasDomain.getThat();
			assertTrue("Omega should be an instance of Domain.",
					vertex instanceof Domain);
			assertFalse("There is more than one Domain defined.", hasDomain
					.getNextHasDomain(OUTGOING) != null);

			// Compares both Domain object with their QualifiedName
			compareDomain(attributes.remove(gAttribute.getName()).getDomain(),
					(Domain) vertex);
		}

		// The map should be empty.
		assertTrue(
				"There are more Domain objects in Schema then in SchemaGraph.",
				attributes.isEmpty());
	}

	/**
	 * Compares all Constraint objects of two AttributedElementClass objects
	 * with each other.
	 * 
	 * @param element
	 *            AttributedElementClass from the Schema, of which all
	 *            Constraint objects should be compared.
	 * @param gElement
	 *            AttributedElementClass from the SchemaGraph, of which all
	 *            Constraint objects should be compared.
	 */
	final private void compareConstraints(
			de.uni_koblenz.jgralab.schema.AttributedElementClass element,
			AttributedElementClass gElement) {

		int gConstraintCount = 0;

		// Loop over all HasConstraint edges
		for (HasConstraint hasConstraint : gElement
				.getHasConstraintIncidences(OUTGOING)) {
			// Count the Constraint objects.
			gConstraintCount++;

			// Gets the Constraint
			assertTrue("Omega should be an instance of \"Constraint\".",
					hasConstraint.getOmega() instanceof Constraint);
			Constraint gConstraint = (Constraint) hasConstraint.getOmega();

			boolean foundMatch = false;
			boolean equal = false;

			// Compares all Constraints with each other.
			for (de.uni_koblenz.jgralab.schema.Constraint constraint : element
					.getConstraints()) {

				equal = constraint.getMessage()
						.equals(gConstraint.getMessage())
						&& constraint.getPredicate().equals(
								gConstraint.getPredicateQuery());
				// If all String objects are present
				foundMatch |= equal
						&& (constraint.getOffendingElementsQuery() != null)
						&& (gConstraint.getOffendingElementsQuery() != null)
						&& constraint.getOffendingElementsQuery().equals(
								gConstraint.getOffendingElementsQuery());
				// If all String objects except for "OffendingElementQuery" are
				// present
				foundMatch |= equal
						&& (constraint.getOffendingElementsQuery() == null)
						&& (gConstraint.getOffendingElementsQuery() == null);
			}
			// One match should be found!
			assertTrue("No Match have been found for all Constraints.",
					foundMatch);
		}
		// The count of Constraint objects are different
		assertTrue("The count of Constraint objects differ.",
				gConstraintCount == element.getConstraints().size());
	}

	/**
	 * Creates an Attribute map out of an Attribute set.
	 * 
	 * @param attributeList
	 *            Set of Attribute object, of which a map of Attributes should
	 *            be created.
	 * @return Map of Attribute objects with their QualifiedName as key.
	 */
	final private Map<String, de.uni_koblenz.jgralab.Attribute> getAttributeMap(
			SortedSet<de.uni_koblenz.jgralab.Attribute> attributeList) {

		Map<String, de.uni_koblenz.jgralab.Attribute> attributes = new HashMap<String, de.uni_koblenz.jgralab.Attribute>();

		for (de.uni_koblenz.jgralab.Attribute attribute : attributeList) {
			attributes.put(attribute.getName(), attribute);
		}

		return attributes;
	}

	/**
	 * Makes out of an element set an element map.
	 * 
	 * @param elementSet
	 *            Set of AttributedElementClass objects.
	 * @return The new map of AttributedElementClass objects with their
	 *         QualifiedName as key.
	 */
	final private Map<String, de.uni_koblenz.jgralab.schema.AttributedElementClass> getAttributedElementClassMap(
			Set<de.uni_koblenz.jgralab.schema.AttributedElementClass> elementSet) {

		// Creates the AttributedElementClass map.
		Map<String, de.uni_koblenz.jgralab.schema.AttributedElementClass> map = new HashMap<String, de.uni_koblenz.jgralab.schema.AttributedElementClass>();

		// Fills the map
		for (de.uni_koblenz.jgralab.schema.AttributedElementClass element : elementSet) {
			map.put(element.getQualifiedName(), element);
		}

		return map;
	}
}