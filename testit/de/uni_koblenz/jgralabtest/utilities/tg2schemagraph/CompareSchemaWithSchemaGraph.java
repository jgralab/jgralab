/*
 * JGraLab - The Java graph laboratory
 * (c) 2006-2010 Institute for Software Technology
 *               University of Koblenz-Landau, Germany
 * 
 *               ist@uni-koblenz.de
 * 
 * Please report bugs to http://serres.uni-koblenz.de/bugzilla
 * 
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package de.uni_koblenz.jgralabtest.utilities.tg2schemagraph;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.Map.Entry;

import de.uni_koblenz.jgralab.Edge;
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
import de.uni_koblenz.jgralab.grumlschema.structure.Annotates;
import de.uni_koblenz.jgralab.grumlschema.structure.Attribute;
import de.uni_koblenz.jgralab.grumlschema.structure.AttributedElementClass;
import de.uni_koblenz.jgralab.grumlschema.structure.ComesFrom;
import de.uni_koblenz.jgralab.grumlschema.structure.Comment;
import de.uni_koblenz.jgralab.grumlschema.structure.Constraint;
import de.uni_koblenz.jgralab.grumlschema.structure.ContainsDefaultPackage;
import de.uni_koblenz.jgralab.grumlschema.structure.ContainsDomain;
import de.uni_koblenz.jgralab.grumlschema.structure.ContainsGraphElementClass;
import de.uni_koblenz.jgralab.grumlschema.structure.ContainsSubPackage;
import de.uni_koblenz.jgralab.grumlschema.structure.DefinesGraphClass;
import de.uni_koblenz.jgralab.grumlschema.structure.EdgeClass;
import de.uni_koblenz.jgralab.grumlschema.structure.EndsAt;
import de.uni_koblenz.jgralab.grumlschema.structure.GoesTo;
import de.uni_koblenz.jgralab.grumlschema.structure.GraphClass;
import de.uni_koblenz.jgralab.grumlschema.structure.GraphElementClass;
import de.uni_koblenz.jgralab.grumlschema.structure.HasAttribute;
import de.uni_koblenz.jgralab.grumlschema.structure.HasConstraint;
import de.uni_koblenz.jgralab.grumlschema.structure.HasDomain;
import de.uni_koblenz.jgralab.grumlschema.structure.IncidenceClass;
import de.uni_koblenz.jgralab.grumlschema.structure.IncidenceDirection;
import de.uni_koblenz.jgralab.grumlschema.structure.NamedElement;
import de.uni_koblenz.jgralab.grumlschema.structure.Package;
import de.uni_koblenz.jgralab.grumlschema.structure.Schema;
import de.uni_koblenz.jgralab.grumlschema.structure.SpecializesEdgeClass;
import de.uni_koblenz.jgralab.grumlschema.structure.SpecializesVertexClass;
import de.uni_koblenz.jgralab.grumlschema.structure.Subsets;
import de.uni_koblenz.jgralab.grumlschema.structure.VertexClass;
import de.uni_koblenz.jgralab.schema.BooleanDomain;
import de.uni_koblenz.jgralab.schema.DoubleDomain;
import de.uni_koblenz.jgralab.schema.IntegerDomain;
import de.uni_koblenz.jgralab.schema.LongDomain;
import de.uni_koblenz.jgralab.schema.StringDomain;
import de.uni_koblenz.jgralab.schema.RecordDomain.RecordComponent;

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

	private String currentName;

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

		compareAllElements(schema, schemaGraph.getFirstSchema());
	}

	/**
	 * Compares a Schema and a Schema from a SchemaGraph with each other.
	 * 
	 * @param schema
	 *            Schema, which is compared.
	 * @param gSchema
	 *            Schema of a SchemaGraph, which is compared.
	 */
	final public void compareAllElements(
			de.uni_koblenz.jgralab.schema.Schema schema, Schema gSchema) {

		compareSchema(schema, gSchema);

		compareGraphClass(schema, gSchema);

		compareDefaultPackageAndAllSubelements(schema, gSchema);
	}

	private void compareSchema(de.uni_koblenz.jgralab.schema.Schema schema,
			Schema gSchema) {

		assertFalse("There is no Schema defined!", schema == null);

		assertFalse("There is no SchemaGraph defined!", gSchema == null);

		// Compares there names and package prefixes
		assertEquals("Both Schema objects have a different name.", schema
				.getName(), gSchema.get_name());
		assertEquals("Both Schema objects have a different package prefix.",
				schema.getPackagePrefix(), gSchema.get_packagePrefix());
	}

	private void compareGraphClass(de.uni_koblenz.jgralab.schema.Schema schema,
			Schema gSchema) {

		GraphClass gGraphClass = retrieveAndCheckGraphClass(gSchema);
		de.uni_koblenz.jgralab.schema.GraphClass graphClass = retrieveAndCheckGraphClass(schema);

		// Compares both GraphClass objects
		compareAttributedElementClass(graphClass, gGraphClass);
	}

	private de.uni_koblenz.jgralab.schema.GraphClass retrieveAndCheckGraphClass(
			de.uni_koblenz.jgralab.schema.Schema schema) {
		de.uni_koblenz.jgralab.schema.GraphClass graphClass = schema
				.getGraphClass();
		assertFalse("There is no GraphClass defined in the Schema",
				graphClass == null);
		return graphClass;
	}

	private GraphClass retrieveAndCheckGraphClass(Schema gSchema) {
		// Get the 'only' defined GraphClass in the SchemaGraph
		DefinesGraphClass definesGraphClass = gSchema
				.getFirstDefinesGraphClass(OUTGOING);

		// There should be one GraphClass
		assertTrue("There is no GraphClass or DefinesGraphClass edge defined.",
				definesGraphClass != null);

		Vertex vertex = definesGraphClass.getThat();
		assertTrue(
				"DefinesGraphClass is not referencing to an instance of GraphClass.",
				vertex instanceof GraphClass);
		assertFalse(
				"There is more than one GraphClass defined in the SchemaGraph.",
				definesGraphClass.getNextDefinesGraphClass(OUTGOING) != null);

		GraphClass gGraphClass = (GraphClass) vertex;
		return gGraphClass;
	}

	private void compareDefaultPackageAndAllSubelements(
			de.uni_koblenz.jgralab.schema.Schema schema, Schema gSchema) {

		Package gPackage = retrieveAndCheckDefaultPackage(gSchema);
		de.uni_koblenz.jgralab.schema.Package xPackage = retrieveAndCheckDefaultPackage(schema);

		// Compares both Package objects with each other.
		comparePackage(xPackage, gPackage);
	}

	private de.uni_koblenz.jgralab.schema.Package retrieveAndCheckDefaultPackage(
			de.uni_koblenz.jgralab.schema.Schema schema) {
		de.uni_koblenz.jgralab.schema.Package xPackage = schema
				.getDefaultPackage();
		assertFalse("There is no default Package defined in the Schema.",
				xPackage == null);
		return xPackage;
	}

	private Package retrieveAndCheckDefaultPackage(Schema gSchema) {
		Vertex vertex;
		// Gets the only defined DefaultPackage in the SchemaGraph
		ContainsDefaultPackage containsDefaultPackage = gSchema
				.getFirstContainsDefaultPackage(OUTGOING);
		// There should be one DefaultPackage
		assertFalse("There is no DefaultPackage defined.",
				containsDefaultPackage == null);
		vertex = containsDefaultPackage.getThat();
		assertFalse(
				"There is more than one DefaultPackage defined.",
				containsDefaultPackage.getNextContainsDefaultPackage(OUTGOING) != null);
		assertTrue(
				"ContainsDefaultPackage si not referencing to an instance of Package.",
				vertex instanceof Package);
		Package gPackage = (Package) vertex;
		return gPackage;
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

		// Compare this Package instances with each other, but without its
		// containing elements.
		compareNamedElement(xPackage, gPackage);

		// DOMAINS
		compareAllDomains(xPackage, gPackage);

		// GRAPHELEMENTCLASS
		compareAllGraphElementClasses(xPackage, gPackage);

		// SUBPACKAGES
		compareAllSubPackages(xPackage, gPackage);
	}

	private void compareNamedElement(
			de.uni_koblenz.jgralab.schema.NamedElement element,
			NamedElement gElement) {

		compareQualifiedName(element, gElement);

		compareComments(element, gElement);
	}

	private void compareQualifiedName(
			de.uni_koblenz.jgralab.schema.NamedElement element,
			NamedElement gElement) {
		// Comparison of the QualifiedName
		assertEquals("Both Package objects have a different name.", element
				.getQualifiedName(), gElement.get_qualifiedName());
	}

	private void compareComments(
			de.uni_koblenz.jgralab.schema.NamedElement element,
			NamedElement gElement) {

		List<String> comments = element.getComments();
		List<String> gComments = retrieveComments(gElement);

		comments.containsAll(gComments);
		gComments.containsAll(comments);
	}

	private List<String> retrieveComments(NamedElement gElement) {
		List<String> gComments = new ArrayList<String>();

		Annotates annotates = gElement.getFirstAnnotates();

		while (annotates != null) {

			Comment comment = (Comment) annotates.getThat();

			gComments.add(comment.get_text());

			annotates = annotates.getNextAnnotates();
		}
		return gComments;
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
			Domain gDomain = retrieveDomain(containsDomain);

			// Gets the simpleName for querying a the right domain
			String simpleName = schema.getDomain(gDomain.get_qualifiedName())
					.getSimpleName();

			// Gets, removes and compares at the same time both Domain objects.
			de.uni_koblenz.jgralab.schema.Domain domain = domains
					.remove(simpleName);
			assertFalse("There is corresponding Domain of name \"" + simpleName
					+ "\" in the Schema.", domain == null);

			compareDomain(domain, gDomain);
		}

		clearBasicDomains(domains);

		// After all this, the Domain map should be empty
		assertTrue(
				"There are more Domains in the Schema then in the SchemaGraph.",
				domains.isEmpty());
	}

	private void clearBasicDomains(
			Map<String, de.uni_koblenz.jgralab.schema.Domain> domains) {
		domains.remove(BooleanDomain.BOOLEANDOMAIN_NAME);
		domains.remove(IntegerDomain.INTDOMAIN_NAME);
		domains.remove(LongDomain.LONGDOMAIN_NAME);
		domains.remove(DoubleDomain.DOUBLEDOMAIN_NAME);
		domains.remove(StringDomain.STRINGDOMAIN_NAME);
	}

	private Domain retrieveDomain(ContainsDomain containsDomain) {
		assertTrue("ContainsDomain is not an instance of type Domain.",
				containsDomain.getOmega() instanceof Domain);
		Domain gDomain = (Domain) containsDomain.getOmega();
		return gDomain;
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

			Vertex omega = containsGraphElementClass.getOmega();
			assert omega != null;

			// Distinguishing between VertexClass and EdgeClass
			if (omega instanceof VertexClass) {
				VertexClass gVertexClass = (VertexClass) omega;
				// Retrieving the simple name of the corresponding VertexClass
				String simpleName = schema.getAttributedElementClass(
						gVertexClass.get_qualifiedName()).getSimpleName();
				// Queries, removes and compares at the same time two
				// VertexClass objects
				compareVertexClass(vertexClasses.remove(simpleName),
						gVertexClass);
			} else if (omega instanceof EdgeClass) {
				// The Same for the EdgeClass comparison
				EdgeClass gEdgeClass = (EdgeClass) omega;
				// Retrieving the simple name of the corresponding VertexClass
				String simpleName = schema.getAttributedElementClass(
						gEdgeClass.get_qualifiedName()).getSimpleName();
				// Queries, removes and compares at the same time two EdgeClass
				// objects
				compareEdgeClass(edgeClasses.remove(simpleName), gEdgeClass);
			} else {
				throw new RuntimeException("Unexpected type " + omega);
			}
		}

		// Both maps should be empty.

		assertTrue(
				"There are more VertexClasses in Schema then in the SchemaGraph.",
				vertexClasses.isEmpty());
		for (de.uni_koblenz.jgralab.schema.EdgeClass e : edgeClasses.values()) {
			System.out.println(e.getQualifiedName());
		}
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
					.getPackage(gSubPackage.get_qualifiedName());

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

		compareNamedElement(domain, gDomain);

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

		Collection<RecordComponent> tempComponents = domain.getComponents();
		Map<String, de.uni_koblenz.jgralab.schema.Domain> components = new HashMap<String, de.uni_koblenz.jgralab.schema.Domain>(
				tempComponents.size());
		for (RecordComponent component : tempComponents) {
			components.put(component.getName(), component.getDomain());
		}
		for (HasRecordDomainComponent hasRecordDomainComponent : gDomain
				.getHasRecordDomainComponentIncidences(OUTGOING)) {

			assertTrue("Omega should be an instance of Domain.",
					hasRecordDomainComponent.getOmega() instanceof Domain);
			// Gets the Domain
			Domain domainComponent = (Domain) hasRecordDomainComponent
					.getOmega();

			// Get and removes the Domain and compares.
			// The comparison of the Component name is missed out, because
			// it is
			// implicitly done.

			de.uni_koblenz.jgralab.schema.Domain currentDomain = components
					.remove(hasRecordDomainComponent.get_name());
			assertFalse("In the Schema there no Domain called: \""
					+ hasRecordDomainComponent.get_name() + "\"",
					currentDomain == null);

			assertEquals("Both DomainComponents don't have an equal name.",
					currentDomain.getQualifiedName(), domainComponent
							.get_qualifiedName());
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
						.get_qualifiedName());

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
						.get_qualifiedName());
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
						.get_qualifiedName());
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
		List<String> gEnumConstants = gDomain.get_enumConstants();

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

		compareNamedElement(element, gElement);

		if (element instanceof de.uni_koblenz.jgralab.schema.GraphElementClass) {
			compareIsAbstract(
					(de.uni_koblenz.jgralab.schema.GraphElementClass) element,
					(GraphElementClass) gElement);
		}

		// Comparing all other Attributes, Constraints and Comments
		compareAttributes(element, gElement);

		compareConstraints(element, gElement);
	}

	private void compareIsAbstract(
			de.uni_koblenz.jgralab.schema.GraphElementClass element,
			GraphElementClass gElement) {
		// Comparing the attribute \"isAbstract\"
		assertEquals("Attribute \"isAbstract\" is different.", element
				.isAbstract(), gElement.is_abstract());
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

		dropInternalClasses(superClasses);

		// Loop over all SpecializesVertexClass edges
		for (SpecializesVertexClass specializesVertexClass : gVertexClass
				.getSpecializesVertexClassIncidences(OUTGOING)) {
			AttributedElementClass element = (AttributedElementClass) specializesVertexClass
					.getOmega();
			// It gets, removes and compare the QualifiedNames
			assertEquals(
					"SuperClasses of these AttributeElementClass objects are different.",
					superClasses.remove(element.get_qualifiedName())
							.getQualifiedName(), element.get_qualifiedName());
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
		dropInternalClasses(superClasses);

		// Loop over all SpecializesEdgeClass edges
		for (SpecializesEdgeClass specializesEdgeClass : gEdgeClass
				.getSpecializesEdgeClassIncidences(OUTGOING)) {
			AttributedElementClass gElement = (AttributedElementClass) specializesEdgeClass
					.getOmega();

			// Gets, removes and compares the QualifiedNames
			de.uni_koblenz.jgralab.schema.AttributedElementClass element = superClasses
					.remove(gElement.get_qualifiedName());

			assertEquals(
					"SuperClasses of these AttributeElementClass objects are different.",
					element.getQualifiedName(), gElement.get_qualifiedName());
		}

		// After the comparison the map should be empty
		assertTrue(
				"There are more SuperClasses in the AttibuteElement of the Schema then in the SchemaGraph.",
				superClasses.isEmpty());

		// "To" and "From" edges are compared
		ComesFrom comesFrom = gEdgeClass.getFirstComesFrom();
		compareIncidenceClass(edgeClass.getFrom(), (IncidenceClass) comesFrom
				.getThat(), IncidenceDirection.OUT);
		// TODO TEST ob es weitere Kanten gibt, die es nicht geben sollte!

		GoesTo goesTo = gEdgeClass.getFirstGoesTo();
		compareIncidenceClass(edgeClass.getTo(), (IncidenceClass) goesTo
				.getThat(), IncidenceDirection.IN);
		// TODO TEST ob es weitere Kanten gibt, die es nicht geben sollte!
	}

	private void dropInternalClasses(
			Map<String, de.uni_koblenz.jgralab.schema.AttributedElementClass> superClasses) {
		for (Iterator<Entry<String, de.uni_koblenz.jgralab.schema.AttributedElementClass>> it = superClasses
				.entrySet().iterator(); it.hasNext();) {
			if (it.next().getValue().isInternal()) {
				it.remove();
			}
		}
	}

	/**
	 * Compares two IncidenceClasses objects.
	 * 
	 * @param incidence
	 *            IncidenceClasses from the Schema.
	 * @param gIncidence
	 *            IncidenceClasses from the SchemaGraph.
	 */
	final private void compareIncidenceClass(
			de.uni_koblenz.jgralab.schema.IncidenceClass incidence,
			IncidenceClass gIncidence, IncidenceDirection gDirection) {

		currentName = "IncidenceClass";

		checkExistingsOfSchemaElement(incidence);
		checkExistingsOfSchemaGraphElement(gIncidence);

		compareVertexClassesOfIncidenceClasses(incidence, gIncidence);

		currentName = incidence.getVertexClass().getQualifiedName() + " -- "
				+ incidence.getEdgeClass().getQualifiedName();

		// Qualified Name, min, max and rolename are compared.
		compareAggregationKind(incidence, gIncidence);
		compareMaxValue(incidence, gIncidence);
		compareMinValue(incidence, gIncidence);
		compareRoleNames(incidence, gIncidence);
		compareDirection(incidence, gDirection);
		compareSubsettedIncidenceClasses(incidence, gIncidence);
	}

	private void compareDirection(
			de.uni_koblenz.jgralab.schema.IncidenceClass incidence,
			IncidenceDirection gDirection) {
		assertEquals("The directions are not equal: "
				+ incidence.getDirection() + " != " + gDirection, incidence
				.getDirection().toString(), gDirection.toString());
	}

	private void checkExistingsOfSchemaElement(Object object) {
		assertFalse("There is no \"" + currentName
				+ "\" defined in the schema.", object == null);
	}

	private void checkExistingsOfSchemaGraphElement(Object incidence) {
		assertFalse("There is no \"" + currentName
				+ "\" defined in the schema.", incidence == null);
	}

	private void compareVertexClassesOfIncidenceClasses(
			de.uni_koblenz.jgralab.schema.IncidenceClass incidence,
			IncidenceClass gIncidence) {
		EndsAt edgeToVertexClass = gIncidence.getFirstEndsAt();
		assertTrue("That should be an instance of \"VertexClass\".",
				edgeToVertexClass.getThat() instanceof VertexClass);

		VertexClass gVertexClass = (VertexClass) edgeToVertexClass.getThat();
		assertTrue(edgeToVertexClass.getNextEndsAt(OUTGOING) == null);
		de.uni_koblenz.jgralab.schema.VertexClass vertexClass = incidence
				.getVertexClass();

		assertFalse("There is no \"VertexClass\" defined in the schema.",
				vertexClass == null);
		assertFalse("There is no \"VertexClass\" defined in the schema graph.",
				gVertexClass == null);

		assertEquals(
				"The qualifed names of both \"VertexClasses\" do not match: "
						+ vertexClass.getQualifiedName() + " != "
						+ gVertexClass.get_qualifiedName(), vertexClass
						.getQualifiedName(), gVertexClass.get_qualifiedName());
	}

	private void compareRoleNames(
			de.uni_koblenz.jgralab.schema.IncidenceClass incidence,
			IncidenceClass gIncidence) {
		assertEquals(currentName
				+ ": Both \"IncidenceClasses\" should have the same Rolename.",
				incidence.getRolename(), gIncidence.get_roleName());

		// Gets the redefined roles
	}

	private void compareMaxValue(
			de.uni_koblenz.jgralab.schema.IncidenceClass incidence,
			IncidenceClass gIncidence) {
		assertEquals(
				currentName
						+ ": Both \"IncidenceClasses\" should have the same max value.",
				incidence.getMax(), gIncidence.get_max());

	}

	private void compareMinValue(
			de.uni_koblenz.jgralab.schema.IncidenceClass incidence,
			IncidenceClass gIncidence) {
		assertEquals(
				currentName
						+ ": Both \"IncidenceClasses\" should have the same min value.",
				incidence.getMin(), gIncidence.get_min());

	}

	private void compareAggregationKind(
			de.uni_koblenz.jgralab.schema.IncidenceClass type,
			IncidenceClass gType) {

		assertEquals(
				currentName + ": These objects should have the same Type.",
				type.getAggregationKind().toString(), gType.get_aggregation()
						.toString());
	}

	/**
	 * Compares two sets of RedefinedRoles with each other.
	 * 
	 * @param redefinedRoles
	 *            Set of RedefinedRoles of an edge from the Schema.
	 * @param gRedefinedRoles
	 *            Set of RedefinedRoles of an edge from the SchemaGraph.
	 */
	final private void compareSubsettedIncidenceClasses(
			de.uni_koblenz.jgralab.schema.IncidenceClass incidence,
			IncidenceClass gIncidence) {

		Set<String> subsettedIncidenceClasses = getherAllSubsettedEdgeClassQualifiedNames(incidence);

		Set<String> gSubsettedIncidenceClasses = getherAllSubsettedEdgeClassQualifiedNames(gIncidence);

		String contents = subsettedIncidenceClasses + " != "
				+ gSubsettedIncidenceClasses + ".";

		assertTrue("Not all subsets-links are included on the Schema side: "
				+ contents, subsettedIncidenceClasses
				.containsAll(gSubsettedIncidenceClasses));

		assertTrue(
				"Not all subsets-links are included on the Schema graph side: "
						+ contents, gSubsettedIncidenceClasses
						.containsAll(subsettedIncidenceClasses));

	}

	private Set<String> getherAllSubsettedEdgeClassQualifiedNames(
			de.uni_koblenz.jgralab.schema.IncidenceClass incidence) {
		Set<String> subsettedIncidenceClasses = new HashSet<String>(incidence
				.getSubsettedIncidenceClasses().size());

		for (de.uni_koblenz.jgralab.schema.IncidenceClass subsettedIncidenceClass : incidence
				.getSubsettedIncidenceClasses()) {
			de.uni_koblenz.jgralab.schema.EdgeClass edgeClass = subsettedIncidenceClass
					.getEdgeClass();
			if (edgeClass.isInternal()) {
				continue;
			}
			subsettedIncidenceClasses.add(edgeClass.getQualifiedName());
		}
		return subsettedIncidenceClasses;
	}

	private Set<String> getherAllSubsettedEdgeClassQualifiedNames(
			IncidenceClass gIncidence) {
		Set<String> gSubsettedIncidenceClasses = new HashSet<String>();

		for (Subsets subsets : gIncidence.getSubsetsIncidences(OUTGOING)) {
			getherAllSubsettedEdgeClassQaulifiedNames(
					gSubsettedIncidenceClasses, subsets);
		}
		return gSubsettedIncidenceClasses;
	}

	private void getherAllSubsettedEdgeClassQaulifiedNames(
			Set<String> gSubsettedIncidenceClasses, Subsets subsets) {
		IncidenceClass subsettedIncidenceClass = (IncidenceClass) subsets
				.getThat();
		Edge edgeToEdgeClass = subsettedIncidenceClass.getFirstGoesTo();
		if (edgeToEdgeClass == null) {
			edgeToEdgeClass = subsettedIncidenceClass.getFirstComesFrom();
		}
		assertFalse(
				"There is no edge defined to connected this IncidenceClass to its EdgeClass.",
				edgeToEdgeClass == null);
		EdgeClass edgeClass = (EdgeClass) edgeToEdgeClass.getThat();
		gSubsettedIncidenceClasses.add(edgeClass.get_qualifiedName());

		for (Subsets subSubsets : subsettedIncidenceClass
				.getSubsetsIncidences(OUTGOING)) {
			getherAllSubsettedEdgeClassQaulifiedNames(
					gSubsettedIncidenceClasses, subSubsets);
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
		Map<String, de.uni_koblenz.jgralab.schema.Attribute> attributes = new HashMap<String, de.uni_koblenz.jgralab.schema.Attribute>(
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
					attributes.containsKey(gAttribute.get_name()));

			// Get the Domain
			HasDomain hasDomain = gAttribute.getFirstHasDomain(OUTGOING);
			assertTrue("There is no Domain defined.", hasDomain != null);
			Vertex vertex = hasDomain.getThat();
			assertTrue("Omega should be an instance of Domain.",
					vertex instanceof Domain);
			assertFalse("There is more than one Domain defined.", hasDomain
					.getNextHasDomain(OUTGOING) != null);

			// Compares both Domain object with their QualifiedName
			compareDomain(attributes.remove(gAttribute.get_name()).getDomain(),
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

				equal = constraint.getMessage().equals(
						gConstraint.get_message())
						&& constraint.getPredicate().equals(
								gConstraint.get_predicateQuery());
				// If all String objects are present
				foundMatch |= equal
						&& constraint.getOffendingElementsQuery() != null
						&& gConstraint.get_offendingElementsQuery() != null
						&& constraint.getOffendingElementsQuery().equals(
								gConstraint.get_offendingElementsQuery());
				// If all String objects except for "OffendingElementQuery" are
				// present
				foundMatch |= equal
						&& constraint.getOffendingElementsQuery() == null
						&& gConstraint.get_offendingElementsQuery() == null;
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
	final private Map<String, de.uni_koblenz.jgralab.schema.Attribute> getAttributeMap(
			SortedSet<de.uni_koblenz.jgralab.schema.Attribute> attributeList) {

		Map<String, de.uni_koblenz.jgralab.schema.Attribute> attributes = new HashMap<String, de.uni_koblenz.jgralab.schema.Attribute>();

		for (de.uni_koblenz.jgralab.schema.Attribute attribute : attributeList) {
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
