package de.uni_koblenz.jgralabtest.tg2schemagraphtest;

import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertSame;
import static junit.framework.Assert.assertTrue;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;

import de.uni_koblenz.jgralab.EdgeDirection;
import de.uni_koblenz.jgralab.Vertex;
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
import de.uni_koblenz.jgralab.grumlschema.structure.ContainsSubPackage;
import de.uni_koblenz.jgralab.grumlschema.structure.DefinesGraphClass;
import de.uni_koblenz.jgralab.grumlschema.structure.GraphClass;
import de.uni_koblenz.jgralab.grumlschema.structure.HasAttribute;
import de.uni_koblenz.jgralab.grumlschema.structure.HasConstraint;
import de.uni_koblenz.jgralab.grumlschema.structure.HasDomain;
import de.uni_koblenz.jgralab.grumlschema.structure.Package;
import de.uni_koblenz.jgralab.grumlschema.structure.Schema;

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

		assertSame("Different name.", schema.getName(), gSchema.getName());
		assertSame("Different package prefix.", schema.getPackagePrefix(),
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

		assertSame("", xPackage.getQualifiedName(), gPackage.getQualifiedName());

		Map<String, de.uni_koblenz.jgralab.schema.Domain> domains = xPackage
				.getDomains();

		for (ContainsDomain containsDomain : gPackage
				.getContainsDomainIncidences(OUTGOING)) {
			assertTrue("", containsDomain.getOmega() instanceof Domain);
			Domain domain = (Domain) containsDomain.getOmega();
			assertTrue("", domains.containsKey(domain.getQualifiedName()));

			compareDomain(domains.get(domain.getQualifiedName()), domain);
		}

		Map<String, de.uni_koblenz.jgralab.schema.Package> subPackages = xPackage
				.getSubPackages();

		for (ContainsSubPackage containsSubPackage : gPackage
				.getContainsSubPackageIncidences(OUTGOING)) {
			assertTrue("", containsSubPackage.getOmega() instanceof Package);
			Package subPackage = (Package) containsSubPackage.getOmega();
			assertTrue("", subPackages.containsKey(subPackage
					.getQualifiedName()));

			comparePackage(subPackages.get(subPackage.getQualifiedName()),
					subPackage);
		}

	}

	final private void compareDomain(
			de.uni_koblenz.jgralab.schema.Domain domain, Domain gDomain) {

		assertSame("", domain.getQualifiedName(), gDomain.getQualifiedName());

		assertSame("", domain.getClass().getSimpleName(), gDomain.getClass()
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

		Map<String, de.uni_koblenz.jgralab.schema.Domain> components = domain
				.getComponents();

		for (HasRecordDomainComponent hasRecordDomainComponent : gDomain
				.getHasRecordDomainComponentIncidences(OUTGOING)) {

			assertTrue("",
					hasRecordDomainComponent.getOmega() instanceof Domain);
			Domain domainComponent = (Domain) hasRecordDomainComponent
					.getOmega();

			assertTrue("", components.containsKey(hasRecordDomainComponent
					.getName()));
			assertSame("", components.get(hasRecordDomainComponent.getName())
					.getQualifiedName(), domainComponent.getQualifiedName());
		}
	}

	final private void compareDomain(
			de.uni_koblenz.jgralab.schema.MapDomain domain, MapDomain gDomain) {

		Iterator<HasKeyDomain> keyIt = gDomain.getHasKeyDomainIncidences(
				OUTGOING).iterator();
		Vertex vertex = keyIt.next().getOmega();
		assertTrue("", vertex instanceof Domain);
		assertFalse("", keyIt.hasNext());
		Domain gKeyDomain = (Domain) vertex;

		assertSame("", domain.getKeyDomain().getQualifiedName(), gKeyDomain
				.getQualifiedName());

		Iterator<HasValueDomain> valueIt = gDomain.getHasValueDomainIncidences(
				OUTGOING).iterator();
		vertex = valueIt.next().getOmega();
		assertTrue("", vertex instanceof Domain);
		assertFalse("", valueIt.hasNext());
		Domain gValueDomain = (Domain) vertex;

		assertSame("", domain.getValueDomain().getQualifiedName(), gValueDomain
				.getQualifiedName());
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

		assertSame("", domain.getBaseDomain().getQualifiedName(), gBaseDomain
				.getQualifiedName());
	}

	final private void compareDomain(
			de.uni_koblenz.jgralab.schema.EnumDomain domain, EnumDomain gDomain) {

		List<String> enumConstants = domain.getConsts();
		List<String> gEnumConstants = gDomain.getEnumConstants();

		assertTrue("", enumConstants.size() == gEnumConstants.size());

		assertTrue("", gEnumConstants.containsAll(enumConstants));
	}

	final private void compareGraphClass(
			de.uni_koblenz.jgralab.schema.GraphClass graphClass,
			GraphClass gGraphClass) {

		assertSame("Attribute \"isAbstract\" is different.", graphClass
				.isAbstract(), gGraphClass.isIsAbstract());
		assertSame("Attribute \"qualifiedName\" is different.", graphClass
				.getQualifiedName(), gGraphClass.getQualifiedName());

		compareAttributes(graphClass, gGraphClass);
		compareConstraints(graphClass, gGraphClass);
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

			compareDomain(attributes.get(gAttribute.getName()).getDomain(),
					(Domain) vertex);

		}
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