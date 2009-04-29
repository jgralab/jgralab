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
import de.uni_koblenz.jgralab.grumlschema.structure.Attribute;
import de.uni_koblenz.jgralab.grumlschema.structure.AttributedElementClass;
import de.uni_koblenz.jgralab.grumlschema.structure.Constraint;
import de.uni_koblenz.jgralab.grumlschema.structure.ContainsDefaultPackage;
import de.uni_koblenz.jgralab.grumlschema.structure.ContainsDomain;
import de.uni_koblenz.jgralab.grumlschema.structure.ContainsGraphElementClass;
import de.uni_koblenz.jgralab.grumlschema.structure.ContainsSubPackage;
import de.uni_koblenz.jgralab.grumlschema.structure.DefinesGraphClass;
import de.uni_koblenz.jgralab.grumlschema.structure.EdgeClass;
import de.uni_koblenz.jgralab.grumlschema.structure.GraphClass;
import de.uni_koblenz.jgralab.grumlschema.structure.GraphElementClass;
import de.uni_koblenz.jgralab.grumlschema.structure.HasAttribute;
import de.uni_koblenz.jgralab.grumlschema.structure.HasConstraint;
import de.uni_koblenz.jgralab.grumlschema.structure.HasDomain;
import de.uni_koblenz.jgralab.grumlschema.structure.Package;
import de.uni_koblenz.jgralab.grumlschema.structure.Schema;
import de.uni_koblenz.jgralab.grumlschema.structure.SpecializesEdgeClass;
import de.uni_koblenz.jgralab.grumlschema.structure.SpecializesVertexClass;
import de.uni_koblenz.jgralab.grumlschema.structure.VertexClass;

@WorkInProgress(responsibleDevelopers = "mmce", expectedFinishingDate = "2009-04-29")
public class CompareSchemaWithSchemaGraph {

	private de.uni_koblenz.jgralab.schema.Schema schema;
	private SchemaGraph schemaGraph;

	private static final EdgeDirection OUTGOING = EdgeDirection.OUT;

	public CompareSchemaWithSchemaGraph() {
		// TODO Auto-generated constructor stub
	}

	public boolean compare(de.uni_koblenz.jgralab.schema.Schema schema,
			SchemaGraph schemaGraph) {

		return compareSchema(schema, schemaGraph.getFirstSchema());
	}

	public boolean compareSchema(de.uni_koblenz.jgralab.schema.Schema schema,
			Schema gSchema) {

		assertEquals("Different name.", schema.getName(), gSchema.getName());
		assertEquals("Different package prefix.", schema.getPackagePrefix(),
				gSchema.getPackagePrefix());

		Iterator<DefinesGraphClass> it = schemaGraph
				.getDefinesGraphClassEdges().iterator();

		Vertex vertex = it.next().getOmega();
		assertTrue("Omega should be an instance of \"GraphClass\"",
				vertex instanceof GraphClass);

		compareGraphClass(schema.getGraphClass(), (GraphClass) vertex);

		Iterator<ContainsDefaultPackage> packageIt = gSchema
				.getContainsDefaultPackageIncidences().iterator();
		vertex = packageIt.next().getOmega();
		assertFalse("", packageIt.hasNext());
		assertTrue("", vertex instanceof Package);
		comparePackage(schema.getDefaultPackage(), (Package) vertex);

		return false;
	}

	final private void comparePackage(
			de.uni_koblenz.jgralab.schema.Package xPackage, Package gPackage) {

		assertEquals("", xPackage.getQualifiedName(), gPackage
				.getQualifiedName());

		Map<String, de.uni_koblenz.jgralab.schema.Domain> domains = new HashMap<String, de.uni_koblenz.jgralab.schema.Domain>(
				xPackage.getDomains());

		for (ContainsDomain containsDomain : gPackage
				.getContainsDomainIncidences(OUTGOING)) {
			assertTrue("", containsDomain.getOmega() instanceof Domain);
			Domain domain = (Domain) containsDomain.getOmega();
			assertTrue("", domains.containsKey(domain.getQualifiedName()));

			compareDomain(domains.remove(domain.getQualifiedName()), domain);
		}
		assertTrue("", domains.isEmpty());

		Map<String, de.uni_koblenz.jgralab.schema.Package> subPackages = new HashMap<String, de.uni_koblenz.jgralab.schema.Package>(
				xPackage.getSubPackages());

		for (ContainsSubPackage containsSubPackage : gPackage
				.getContainsSubPackageIncidences(OUTGOING)) {
			assertTrue("", containsSubPackage.getOmega() instanceof Package);
			Package subPackage = (Package) containsSubPackage.getOmega();
			assertTrue("", subPackages.containsKey(subPackage
					.getQualifiedName()));

			comparePackage(subPackages.remove(subPackage.getQualifiedName()),
					subPackage);
		}

		assertTrue("", subPackages.isEmpty());

		Map<String, de.uni_koblenz.jgralab.schema.VertexClass> vertexClasses = new HashMap<String, de.uni_koblenz.jgralab.schema.VertexClass>(
				xPackage.getVertexClasses());
		Map<String, de.uni_koblenz.jgralab.schema.EdgeClass> edgeClasses = new HashMap<String, de.uni_koblenz.jgralab.schema.EdgeClass>(
				xPackage.getEdgeClasses());

		for (de.uni_koblenz.jgralab.schema.VertexClass element : vertexClasses
				.values()) {
			if (element.isInternal()) {
				vertexClasses.remove(element.getQualifiedName());
			}
		}

		for (de.uni_koblenz.jgralab.schema.EdgeClass element : edgeClasses
				.values()) {
			if (element.isInternal()) {
				edgeClasses.remove(element.getQualifiedName());
			}
		}

		for (ContainsGraphElementClass containsGraphElementClass : gPackage
				.getContainsGraphElementClassIncidences(OUTGOING)) {

			assertTrue(
					"",
					containsGraphElementClass.getOmega() instanceof GraphElementClass);

			if (containsGraphElementClass.getOmega() instanceof VertexClass) {
				VertexClass gVertexClass = (VertexClass) containsGraphElementClass
						.getOmega();
				compareVertexClass(vertexClasses.remove(gVertexClass
						.getQualifiedName()), gVertexClass);
			} else {
				EdgeClass gEdgeClass = (EdgeClass) containsGraphElementClass
						.getOmega();
				compareEdgeClass(edgeClasses.remove(gEdgeClass
						.getQualifiedName()), gEdgeClass);
			}
		}

		assertTrue("", vertexClasses.isEmpty());
		assertTrue("", vertexClasses.isEmpty());
	}

	private void compareAttributedElementClass(
			de.uni_koblenz.jgralab.schema.AttributedElementClass element,
			AttributedElementClass gElement) {

		assertEquals("Attribute \"isAbstract\" is different.", element
				.isAbstract(), gElement.isIsAbstract());
		assertEquals("Attribute \"qualifiedName\" is different.", element
				.getQualifiedName(), gElement.getQualifiedName());

		compareAttributes(element, gElement);
		compareConstraints(element, gElement);

	}

	private void compareVertexClass(
			de.uni_koblenz.jgralab.schema.VertexClass vertexClass,
			VertexClass gVertexClass) {

		compareAttributedElementClass(vertexClass, gVertexClass);

		Map<String, de.uni_koblenz.jgralab.schema.AttributedElementClass> superClasses = getAttributedElementClassMap(vertexClass
				.getDirectSuperClasses());

		for (SpecializesVertexClass specializesVertexClass : gVertexClass
				.getSpecializesVertexClassIncidences(OUTGOING)) {
			AttributedElementClass element = (AttributedElementClass) specializesVertexClass
					.getOmega();

			assertEquals("", superClasses.remove(element.getQualifiedName()),
					element.getQualifiedName());
		}

		assertTrue("", superClasses.isEmpty());
	}

	private Map<String, de.uni_koblenz.jgralab.schema.AttributedElementClass> getAttributedElementClassMap(
			Set<de.uni_koblenz.jgralab.schema.AttributedElementClass> elementSet) {

		Map<String, de.uni_koblenz.jgralab.schema.AttributedElementClass> map = new HashMap<String, de.uni_koblenz.jgralab.schema.AttributedElementClass>();

		for (de.uni_koblenz.jgralab.schema.AttributedElementClass element : elementSet) {
			map.put(element.getQualifiedName(), element);
		}

		return map;
	}

	private void compareEdgeClass(
			de.uni_koblenz.jgralab.schema.EdgeClass edgeClass,
			EdgeClass gEdgeClass) {

		compareAttributedElementClass(edgeClass, gEdgeClass);

		Map<String, de.uni_koblenz.jgralab.schema.AttributedElementClass> superClasses = getAttributedElementClassMap(edgeClass
				.getDirectSuperClasses());

		for (SpecializesEdgeClass specializesEdgeClass : gEdgeClass
				.getSpecializesEdgeClassIncidences(OUTGOING)) {
			AttributedElementClass element = (AttributedElementClass) specializesEdgeClass
					.getOmega();

			assertEquals("", superClasses.remove(element.getQualifiedName()),
					element.getQualifiedName());
		}

		assertTrue("", superClasses.isEmpty());

	}

	final private void compareDomain(
			de.uni_koblenz.jgralab.schema.Domain domain, Domain gDomain) {

		assertEquals("", domain.getQualifiedName(), gDomain.getQualifiedName());

		assertEquals("", domain.getClass().getSimpleName(), gDomain.getClass()
				.getSimpleName());

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

			assertTrue("",
					hasRecordDomainComponent.getOmega() instanceof Domain);
			Domain domainComponent = (Domain) hasRecordDomainComponent
					.getOmega();

			assertEquals("", components.remove(
					hasRecordDomainComponent.getName()).getQualifiedName(),
					domainComponent.getQualifiedName());
		}
		assertTrue("", components.isEmpty());
	}

	final private void compareDomain(
			de.uni_koblenz.jgralab.schema.MapDomain domain, MapDomain gDomain) {

		Iterator<HasKeyDomain> keyIt = gDomain.getHasKeyDomainIncidences(
				OUTGOING).iterator();
		Vertex vertex = keyIt.next().getOmega();
		assertTrue("", vertex instanceof Domain);
		assertFalse("", keyIt.hasNext());
		Domain gKeyDomain = (Domain) vertex;

		assertEquals("", domain.getKeyDomain().getQualifiedName(), gKeyDomain
				.getQualifiedName());

		Iterator<HasValueDomain> valueIt = gDomain.getHasValueDomainIncidences(
				OUTGOING).iterator();
		vertex = valueIt.next().getOmega();
		assertTrue("", vertex instanceof Domain);
		assertFalse("", valueIt.hasNext());
		Domain gValueDomain = (Domain) vertex;

		assertEquals("", domain.getValueDomain().getQualifiedName(),
				gValueDomain.getQualifiedName());
	}

	final private void compareDomain(
			de.uni_koblenz.jgralab.schema.CollectionDomain domain,
			CollectionDomain gDomain) {

		Iterator<HasBaseDomain> it = gDomain.getHasBaseDomainIncidences(
				OUTGOING).iterator();
		Vertex vertex = it.next().getOmega();
		assertTrue("", vertex instanceof Domain);
		assertFalse("", it.hasNext());
		Domain gBaseDomain = (Domain) vertex;

		assertEquals("", domain.getBaseDomain().getQualifiedName(), gBaseDomain
				.getQualifiedName());
	}

	final private void compareDomain(
			de.uni_koblenz.jgralab.schema.EnumDomain domain, EnumDomain gDomain) {

		List<String> enumConstants = domain.getConsts();
		List<String> gEnumConstants = gDomain.getEnumConstants();

		assertTrue("", enumConstants.size() == gEnumConstants.size());

		assertTrue("", gEnumConstants.containsAll(enumConstants));
		assertTrue("", enumConstants.containsAll(gEnumConstants));
	}

	final private void compareGraphClass(
			de.uni_koblenz.jgralab.schema.GraphClass graphClass,
			GraphClass gGraphClass) {

		compareAttributedElementClass(graphClass, gGraphClass);
	}

	final private void compareAttributes(
			de.uni_koblenz.jgralab.schema.AttributedElementClass element,
			AttributedElementClass gElement) {

		Map<String, de.uni_koblenz.jgralab.Attribute> attributes = getAttributeMap(element
				.getAttributeList());

		for (HasAttribute hasAttribute : gElement
				.getHasAttributeIncidences(OUTGOING)) {
			assertTrue("", hasAttribute.getOmega() instanceof Attribute);
			Attribute gAttribute = (Attribute) hasAttribute.getOmega();
			assertTrue("", attributes.containsKey(gAttribute.getName()));

			Iterator<HasDomain> it = gAttribute
					.getHasDomainIncidences(OUTGOING).iterator();
			Vertex vertex = it.next().getOmega();
			assertTrue("", vertex instanceof Domain);
			assertFalse("", it.hasNext());

			compareDomain(attributes.remove(gAttribute.getName()).getDomain(),
					(Domain) vertex);
		}
		assertTrue("", attributes.isEmpty());
	}

	private Map<String, de.uni_koblenz.jgralab.Attribute> getAttributeMap(
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
				assertFalse("", element.getConstraints().isEmpty());
				firstTime = false;
			}

			assertTrue("", hasConstraint.getOmega() instanceof Constraint);
			Constraint gConstraint = (Constraint) hasConstraint.getOmega();

			boolean foundMatch = false;

			for (de.uni_koblenz.jgralab.schema.Constraint constraint : element
					.getConstraints()) {
				foundMatch |= constraint.getMessage().equals(
						gConstraint.getMessage())
						&& constraint.getPredicate().equals(
								gConstraint.getPredicateQuery())
						&& constraint.getOffendingElementsQuery().equals(
								gConstraint.getOffendingElementsQuery());
			}

			assertTrue("", foundMatch);
		}
	}
}