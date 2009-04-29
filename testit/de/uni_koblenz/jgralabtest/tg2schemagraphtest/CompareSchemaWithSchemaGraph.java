package de.uni_koblenz.jgralabtest.tg2schemagraphtest;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertTrue;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
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

@WorkInProgress(responsibleDevelopers = "mmce, Eckhard Großmann", expectedFinishingDate = "2009-04-29")
public class CompareSchemaWithSchemaGraph {

	private de.uni_koblenz.jgralab.schema.Schema schema;

	private static final EdgeDirection OUTGOING = EdgeDirection.OUT;

	public CompareSchemaWithSchemaGraph() {
		// TODO Auto-generated constructor stub
	}

	public boolean compare(de.uni_koblenz.jgralab.schema.Schema schema,
			SchemaGraph schemaGraph) {

		this.schema = schema;

		return compareSchema(schema, schemaGraph.getFirstSchema());
	}

	public boolean compareSchema(de.uni_koblenz.jgralab.schema.Schema schema,
			Schema gSchema) {

		assertEquals("Both Schema objects have a different name.", schema
				.getName(), gSchema.getName());
		assertEquals("Both Schema objects have a different package prefix.",
				schema.getPackagePrefix(), gSchema.getPackagePrefix());

		Iterator<DefinesGraphClass> it = gSchema
				.getDefinesGraphClassIncidences().iterator();

		assertTrue("No GraphClass is defined.", it.hasNext());
		Vertex vertex = it.next().getOmega();
		assertTrue("Omega should be an instance of \"GraphClass\".",
				vertex instanceof GraphClass);
		assertFalse("There is more than one GraphClass defined.", it.hasNext());

		compareGraphClass(schema.getGraphClass(), (GraphClass) vertex);

		Iterator<ContainsDefaultPackage> packageIt = gSchema
				.getContainsDefaultPackageIncidences().iterator();
		assertTrue("No DefaultPackage is defined.", packageIt.hasNext());
		vertex = packageIt.next().getOmega();
		assertFalse("There is more than one DefaultPackage defined.", packageIt
				.hasNext());
		assertTrue("Omega should be an instance of \"Package\".",
				vertex instanceof Package);
		comparePackage(schema.getDefaultPackage(), (Package) vertex);

		return true;
	}

	final private void comparePackage(
			de.uni_koblenz.jgralab.schema.Package xPackage, Package gPackage) {

		assertEquals("Both Package objects have a different name.", xPackage
				.getQualifiedName(), gPackage.getQualifiedName());

		Map<String, de.uni_koblenz.jgralab.schema.Domain> domains = new HashMap<String, de.uni_koblenz.jgralab.schema.Domain>(
				xPackage.getDomains());

		for (ContainsDomain containsDomain : gPackage
				.getContainsDomainIncidences(OUTGOING)) {
			assertTrue("Omega should be an instance of Domain.", containsDomain
					.getOmega() instanceof Domain);
			Domain domain = (Domain) containsDomain.getOmega();

			String simpleName = schema.getDomain(domain.getQualifiedName())
					.getSimpleName();

			compareDomain(domains.remove(simpleName), domain);
		}
		assertTrue(
				"There are more Domains in the Schema then in the SchemaGraph.",
				domains.isEmpty());

		Map<String, de.uni_koblenz.jgralab.schema.Package> subPackages = new HashMap<String, de.uni_koblenz.jgralab.schema.Package>(
				xPackage.getSubPackages());

		for (ContainsSubPackage containsSubPackage : gPackage
				.getContainsSubPackageIncidences(OUTGOING)) {
			assertTrue("Omega should be an instance of \"Package\".",
					containsSubPackage.getOmega() instanceof Package);
			Package subPackage = (Package) containsSubPackage.getOmega();
			assertTrue("There is no corresponding Package in Schema.",
					subPackages.containsKey(subPackage.getQualifiedName()));

			comparePackage(subPackages.remove(subPackage.getQualifiedName()),
					subPackage);
		}

		assertTrue(
				"There are more Packages in the Schema then in the SchemaGraph.",
				subPackages.isEmpty());

		Map<String, de.uni_koblenz.jgralab.schema.VertexClass> vertexClasses = new HashMap<String, de.uni_koblenz.jgralab.schema.VertexClass>(
				xPackage.getVertexClasses());
		Map<String, de.uni_koblenz.jgralab.schema.EdgeClass> edgeClasses = new HashMap<String, de.uni_koblenz.jgralab.schema.EdgeClass>(
				xPackage.getEdgeClasses());

		for (Iterator<Entry<String, de.uni_koblenz.jgralab.schema.VertexClass>> it = vertexClasses
				.entrySet().iterator(); it.hasNext();) {
			if (it.next().getValue().isInternal()) {
				it.remove();
			}
		}

		for (Iterator<Entry<String, de.uni_koblenz.jgralab.schema.EdgeClass>> it = edgeClasses
				.entrySet().iterator(); it.hasNext();) {
			if (it.next().getValue().isInternal()) {
				it.remove();
			}
		}

		for (ContainsGraphElementClass containsGraphElementClass : gPackage
				.getContainsGraphElementClassIncidences(OUTGOING)) {

			assertTrue(
					"Omega should be an instance of GraphElementClass.",
					containsGraphElementClass.getOmega() instanceof GraphElementClass);

			if (containsGraphElementClass.getOmega() instanceof VertexClass) {
				VertexClass gVertexClass = (VertexClass) containsGraphElementClass
						.getOmega();

				String simpleName = schema.getAttributedElementClass(
						gVertexClass.getQualifiedName()).getSimpleName();

				compareVertexClass(vertexClasses.remove(simpleName),
						gVertexClass);
			} else {
				EdgeClass gEdgeClass = (EdgeClass) containsGraphElementClass
						.getOmega();

				String simpleName = schema.getAttributedElementClass(
						gEdgeClass.getQualifiedName()).getSimpleName();

				compareEdgeClass(edgeClasses.remove(simpleName), gEdgeClass);
			}
		}

		assertTrue(
				"There are more VertexClasses in Schema then in the SchemaGraph.",
				vertexClasses.isEmpty());
		assertTrue(
				"There are more EdgeClasses in Schema then in the SchemaGraph.",
				edgeClasses.isEmpty());
	}

	final private void compareAttributedElementClass(
			de.uni_koblenz.jgralab.schema.AttributedElementClass element,
			AttributedElementClass gElement) {

		assertEquals("Attribute \"isAbstract\" is different.", element
				.isAbstract(), gElement.isIsAbstract());
		assertEquals("Attribute \"qualifiedName\" is different.", element
				.getQualifiedName(), gElement.getQualifiedName());

		compareAttributes(element, gElement);
		compareConstraints(element, gElement);

	}

	final private void compareVertexClass(
			de.uni_koblenz.jgralab.schema.VertexClass vertexClass,
			VertexClass gVertexClass) {

		compareAttributedElementClass(vertexClass, gVertexClass);

		Map<String, de.uni_koblenz.jgralab.schema.AttributedElementClass> superClasses = getAttributedElementClassMap(vertexClass
				.getDirectSuperClasses());

		for (Iterator<Entry<String, de.uni_koblenz.jgralab.schema.AttributedElementClass>> it = superClasses
				.entrySet().iterator(); it.hasNext();) {
			if (it.next().getValue().isInternal()) {
				it.remove();
			}
		}

		for (SpecializesVertexClass specializesVertexClass : gVertexClass
				.getSpecializesVertexClassIncidences(OUTGOING)) {
			AttributedElementClass element = (AttributedElementClass) specializesVertexClass
					.getOmega();
			assertEquals(
					"SuperClasses of these AttributeElementClass objects are different.",
					superClasses.remove(element.getQualifiedName())
							.getQualifiedName(), element.getQualifiedName());
		}

		assertTrue(
				"There are more SuperClasses in the AttibuteElement of the Schema then in the SchemaGraph.",
				superClasses.isEmpty());
	}

	final private Map<String, de.uni_koblenz.jgralab.schema.AttributedElementClass> getAttributedElementClassMap(
			Set<de.uni_koblenz.jgralab.schema.AttributedElementClass> elementSet) {

		Map<String, de.uni_koblenz.jgralab.schema.AttributedElementClass> map = new HashMap<String, de.uni_koblenz.jgralab.schema.AttributedElementClass>();

		for (de.uni_koblenz.jgralab.schema.AttributedElementClass element : elementSet) {
			map.put(element.getQualifiedName(), element);
		}

		return map;
	}

	final private void compareEdgeClass(
			de.uni_koblenz.jgralab.schema.EdgeClass edgeClass,
			EdgeClass gEdgeClass) {

		compareAttributedElementClass(edgeClass, gEdgeClass);

		Map<String, de.uni_koblenz.jgralab.schema.AttributedElementClass> superClasses = getAttributedElementClassMap(edgeClass
				.getDirectSuperClasses());

		for (Iterator<Entry<String, de.uni_koblenz.jgralab.schema.AttributedElementClass>> it = superClasses
				.entrySet().iterator(); it.hasNext();) {
			if (it.next().getValue().isInternal()) {
				it.remove();
			}
		}

		for (SpecializesEdgeClass specializesEdgeClass : gEdgeClass
				.getSpecializesEdgeClassIncidences(OUTGOING)) {
			AttributedElementClass element = (AttributedElementClass) specializesEdgeClass
					.getOmega();

			assertEquals(
					"SuperClasses of these AttributeElementClass objects are different.",
					superClasses.remove(element.getQualifiedName())
							.getQualifiedName(), element.getQualifiedName());
		}

		assertTrue(
				"There are more SuperClasses in the AttibuteElement of the Schema then in the SchemaGraph.",
				superClasses.isEmpty());

		// "To" and "From" edges are compared
		VertexClass vertexClass;

		// TO
		Iterator<To> toIt = gEdgeClass.getToIncidences(OUTGOING).iterator();
		assertTrue("There is no \"To\" edge defined.", toIt.hasNext());
		To to = toIt.next();
		assertTrue("Omega should be an instance of \"VertexClass\".", to
				.getOmega() instanceof VertexClass);
		vertexClass = (VertexClass) to.getOmega();
		assertEquals("Both \"To\" edges should have the same QualifiedName.",
				edgeClass.getTo().getQualifiedName(), vertexClass
						.getQualifiedName());
		assertEquals("Both \"To\" edges should have the same min value.",
				edgeClass.getToMin(), to.getMin());
		assertEquals("Both \"To\" edges should have the same max value.",
				edgeClass.getToMax(), to.getMax());
		assertEquals("Both \"To\" edges should have the same RoleName.",
				edgeClass.getToRolename(), to.getRoleName());

		Set<String> redefinedRoles;
		if (edgeClass.getRedefinedToRoles().size() > 0
				&& to.getRedefinedRoles() != null) {
			redefinedRoles = new TreeSet<String>(edgeClass
					.getRedefinedToRoles());

			for (String redefinedRole : to.getRedefinedRoles()) {
				assertTrue(
						"No corresponding redefined Role could be found and removed.",
						redefinedRoles.remove(redefinedRole));
			}
			assertTrue("There are still redefined Roles left.", redefinedRoles
					.isEmpty());
		} else {
			assertTrue("Wrong conversion!", edgeClass.getRedefinedToRoles()
					.size() == 0
					&& to.getRedefinedRoles() == null);
		}

		// FROM
		Iterator<From> fromIt = gEdgeClass.getFromIncidences(OUTGOING)
				.iterator();
		assertTrue("There is no \"From\" edge defined.", fromIt.hasNext());
		From from = fromIt.next();
		assertTrue("Omega should be an instance of \"VertexClass\".", from
				.getOmega() instanceof VertexClass);
		vertexClass = (VertexClass) from.getOmega();
		assertEquals("Both \"From\" edges should have the same QualifiedName.",
				edgeClass.getFrom().getQualifiedName(), vertexClass
						.getQualifiedName());
		assertEquals("Both \"From\" edges should have the same min value.",
				edgeClass.getFromMin(), from.getMin());
		assertEquals("Both \"From\" edges should have the same max value.",
				edgeClass.getFromMax(), from.getMax());
		assertEquals("Both \"From\" edges should have the same RoleName.",
				edgeClass.getFromRolename(), from.getRoleName());

		if (edgeClass.getRedefinedFromRoles().size() > 0
				&& from.getRedefinedRoles() != null) {
			redefinedRoles = new TreeSet<String>(edgeClass
					.getRedefinedFromRoles());
			for (String redefinedRole : from.getRedefinedRoles()) {
				assertTrue(
						"No corresponding redefined Role could be found and removed.",
						redefinedRoles.remove(redefinedRole));
			}
			assertTrue("There are still redefined Roles left.", redefinedRoles
					.isEmpty());
		} else {
			assertTrue("Wrong conversion!", edgeClass.getRedefinedFromRoles()
					.size() == 0
					&& from.getRedefinedRoles() == null);
		}

		assertEquals(
				"These objects should have the same Type.",
				edgeClass instanceof de.uni_koblenz.jgralab.schema.AggregationClass,
				gEdgeClass instanceof AggregationClass);

		assertEquals(
				"These objects should have the same Type.",
				edgeClass instanceof de.uni_koblenz.jgralab.schema.CompositionClass,
				gEdgeClass instanceof CompositionClass);

		if (gEdgeClass instanceof AggregationClass) {
			de.uni_koblenz.jgralab.schema.AggregationClass aggregationClass = (de.uni_koblenz.jgralab.schema.AggregationClass) edgeClass;
			AggregationClass gAggregationClass = (AggregationClass) gEdgeClass;
			assertEquals("These to edges are aggregated different.",
					aggregationClass.isAggregateFrom(), gAggregationClass
							.isAggregateFrom());
		}
	}

	final private void compareDomain(
			de.uni_koblenz.jgralab.schema.Domain domain, Domain gDomain) {

		assertEquals("Both Domain objects should have a equal QualifiedName",
				domain.getQualifiedName(), gDomain.getQualifiedName());

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

	final private void compareDomain(
			de.uni_koblenz.jgralab.schema.RecordDomain domain,
			RecordDomain gDomain) {

		Map<String, de.uni_koblenz.jgralab.schema.Domain> components = new HashMap<String, de.uni_koblenz.jgralab.schema.Domain>(
				domain.getComponents());

		for (HasRecordDomainComponent hasRecordDomainComponent : gDomain
				.getHasRecordDomainComponentIncidences(OUTGOING)) {

			assertTrue("Omega should be an instance of Domain.",
					hasRecordDomainComponent.getOmega() instanceof Domain);
			Domain domainComponent = (Domain) hasRecordDomainComponent
					.getOmega();

			assertEquals(
					"Both DomainComponents don't have an equal QualifiedName.",
					components.remove(domainComponent.getQualifiedName())
							.getQualifiedName(), hasRecordDomainComponent
							.getName());
		}
		assertTrue("There are more Components in Schema then in SchemaGraph",
				components.isEmpty());
	}

	final private void compareDomain(
			de.uni_koblenz.jgralab.schema.MapDomain domain, MapDomain gDomain) {

		Iterator<HasKeyDomain> keyIt = gDomain.getHasKeyDomainIncidences(
				OUTGOING).iterator();
		assertTrue("There is no key Domain defined.", keyIt.hasNext());
		Vertex vertex = keyIt.next().getOmega();
		assertTrue("Omega should be an instance of Domain.",
				vertex instanceof Domain);
		assertFalse("There is more than one key Domain.", keyIt.hasNext());
		Domain gKeyDomain = (Domain) vertex;

		assertEquals(
				"Both key Domain objects should have the same QualifiedName.",
				domain.getKeyDomain().getQualifiedName(), gKeyDomain
						.getQualifiedName());

		Iterator<HasValueDomain> valueIt = gDomain.getHasValueDomainIncidences(
				OUTGOING).iterator();
		assertTrue("There is no value Domain defined.", valueIt.hasNext());
		vertex = valueIt.next().getOmega();
		assertTrue("Omega should be an instance of Domain.",
				vertex instanceof Domain);
		assertFalse("There is more than one value Domain.", valueIt.hasNext());
		Domain gValueDomain = (Domain) vertex;

		assertEquals(
				"Both value Domain objects should have an equal QualifiedName.",
				domain.getValueDomain().getQualifiedName(), gValueDomain
						.getQualifiedName());
	}

	final private void compareDomain(
			de.uni_koblenz.jgralab.schema.CollectionDomain domain,
			CollectionDomain gDomain) {

		Iterator<HasBaseDomain> it = gDomain.getHasBaseDomainIncidences(
				OUTGOING).iterator();
		assertTrue("There should be a base Domain.", it.hasNext());
		Vertex vertex = it.next().getOmega();
		assertTrue("Omega should be an instance of Domain.",
				vertex instanceof Domain);
		assertFalse("There is more than one base Domain.", it.hasNext());
		Domain gBaseDomain = (Domain) vertex;

		assertEquals(
				"Both base Domain objects should have an equal QualifiedName.",
				domain.getBaseDomain().getQualifiedName(), gBaseDomain
						.getQualifiedName());
	}

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

	final private void compareGraphClass(
			de.uni_koblenz.jgralab.schema.GraphClass graphClass,
			GraphClass gGraphClass) {

		compareAttributedElementClass(graphClass, gGraphClass);
	}

	final private void compareAttributes(
			de.uni_koblenz.jgralab.schema.AttributedElementClass element,
			AttributedElementClass gElement) {

		Map<String, de.uni_koblenz.jgralab.Attribute> attributes = new HashMap<String, de.uni_koblenz.jgralab.Attribute>(
				getAttributeMap(element.getOwnAttributeList()));

		for (HasAttribute hasAttribute : gElement
				.getHasAttributeIncidences(OUTGOING)) {
			assertTrue("Omega should be an instance of Attribute.",
					hasAttribute.getOmega() instanceof Attribute);
			Attribute gAttribute = (Attribute) hasAttribute.getOmega();
			assertTrue("Attribute is not include in the AttributeMap.",
					attributes.containsKey(gAttribute.getName()));

			Iterator<HasDomain> it = gAttribute
					.getHasDomainIncidences(OUTGOING).iterator();
			assertTrue("There is no Domain defined.", it.hasNext());
			Vertex vertex = it.next().getOmega();
			assertTrue("Omega should be an instance of Domain.",
					vertex instanceof Domain);
			assertFalse("There is more than one Domain defined.", it.hasNext());

			compareDomain(attributes.remove(gAttribute.getName()).getDomain(),
					(Domain) vertex);
		}
		assertTrue("There are more Domain in Schema then in SchemaGraph.",
				attributes.isEmpty());
	}

	final private Map<String, de.uni_koblenz.jgralab.Attribute> getAttributeMap(
			SortedSet<de.uni_koblenz.jgralab.Attribute> attributeList) {

		Map<String, de.uni_koblenz.jgralab.Attribute> attributes = new HashMap<String, de.uni_koblenz.jgralab.Attribute>();

		for (de.uni_koblenz.jgralab.Attribute attribute : attributeList) {
			attributes.put(attribute.getName(), attribute);
		}

		return attributes;
	}

	final private void compareConstraints(
			de.uni_koblenz.jgralab.schema.AttributedElementClass element,
			AttributedElementClass gElement) {

		boolean firstTime = true;

		for (HasConstraint hasConstraint : gElement
				.getHasConstraintIncidences(OUTGOING)) {

			if (firstTime) {
				assertFalse(
						"There are no Constraints in the element from Schema, but some in the element from SchemaGraph.",
						element.getConstraints().isEmpty());
				firstTime = false;
			}

			assertTrue("Omega should be an instance of \"Constraint\".",
					hasConstraint.getOmega() instanceof Constraint);
			Constraint gConstraint = (Constraint) hasConstraint.getOmega();

			boolean foundMatch = false;
			boolean equal = false;

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
			assertTrue("No Match have been found for all Constraints.",
					foundMatch);
		}
	}
}