package de.uni_koblenz.jgralab.utilities.tg2schemagraph;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import de.uni_koblenz.jgralab.EdgeDirection;
import de.uni_koblenz.jgralab.GraphException;
import de.uni_koblenz.jgralab.WorkInProgress;
import de.uni_koblenz.jgralab.grumlschema.SchemaGraph;
import de.uni_koblenz.jgralab.grumlschema.domains.Domain;
import de.uni_koblenz.jgralab.grumlschema.domains.EnumDomain;
import de.uni_koblenz.jgralab.grumlschema.domains.HasBaseDomain;
import de.uni_koblenz.jgralab.grumlschema.domains.HasKeyDomain;
import de.uni_koblenz.jgralab.grumlschema.domains.HasRecordDomainComponent;
import de.uni_koblenz.jgralab.grumlschema.domains.HasValueDomain;
import de.uni_koblenz.jgralab.grumlschema.domains.ListDomain;
import de.uni_koblenz.jgralab.grumlschema.domains.MapDomain;
import de.uni_koblenz.jgralab.grumlschema.domains.RecordDomain;
import de.uni_koblenz.jgralab.grumlschema.domains.SetDomain;
import de.uni_koblenz.jgralab.grumlschema.structure.ContainsDefaultPackage;
import de.uni_koblenz.jgralab.grumlschema.structure.ContainsDomain;
import de.uni_koblenz.jgralab.grumlschema.structure.ContainsGraphElementClass;
import de.uni_koblenz.jgralab.grumlschema.structure.ContainsSubPackage;
import de.uni_koblenz.jgralab.grumlschema.structure.DefinesGraphClass;
import de.uni_koblenz.jgralab.grumlschema.structure.GraphClass;
import de.uni_koblenz.jgralab.grumlschema.structure.GraphElementClass;
import de.uni_koblenz.jgralab.grumlschema.structure.Package;
import de.uni_koblenz.jgralab.grumlschema.structure.Schema;
import de.uni_koblenz.jgralab.schema.impl.SchemaImpl;

/**
 * TODO: - Code - Comments - Test
 * 
 * @author mmce, Eckhard Großmann
 */
@WorkInProgress(responsibleDevelopers = "mmce, Eckhard Großmann")
public class SchemaGraph2Schema {

	private de.uni_koblenz.jgralab.schema.Schema schema;
	private Schema gSchema;

	private Map<String, GraphElementClass> gGraphElementClasses;
	private Map<String, Domain> gDomains;

	private Map<String, de.uni_koblenz.jgralab.schema.GraphElementClass> graphElementClasses;
	private Map<String, de.uni_koblenz.jgralab.schema.Domain> domains;

	private de.uni_koblenz.jgralab.schema.GraphClass graphClass;

	public SchemaGraph2Schema() {
	}

	public de.uni_koblenz.jgralab.schema.Schema convert(SchemaGraph schemaGraph) {

		createSchema(schemaGraph);

		createGraphClass();

		getAllGraphElementClassesAndDomains();

		createAllDomains();

		return null;
	}

	private void createAllDomains() {

		domains = new HashMap<String, de.uni_koblenz.jgralab.schema.Domain>();
		domains.putAll(schema.getDomains());

		for (Entry<String, Domain> entry : gDomains.entrySet()) {
			Domain gDomain = entry.getValue();

			createDomain(gDomain);
		}
	}

	private de.uni_koblenz.jgralab.schema.Domain createDomain(Domain gDomain) {

		de.uni_koblenz.jgralab.schema.Domain domain = null;

		if (domains.containsKey(gDomain.getQualifiedName())) {
			domain = domains.get(gDomain.getQualifiedName());
		} else if (gDomain instanceof EnumDomain) {

			EnumDomain enumDomain = (EnumDomain) gDomain;
			schema.createEnumDomain(enumDomain.getQualifiedName(), enumDomain
					.getEnumConstants());

		} else if (gDomain instanceof MapDomain) {

			Domain key, value;

			Iterator<HasKeyDomain> keyIt = gDomain.getHasKeyDomainIncidences(
					EdgeDirection.OUT).iterator();
			if (!keyIt.hasNext()) {
				throw new GraphException(
						"No \"HasKeyDomain\" has been defined.");
			}

			assert (keyIt.next() != null && keyIt.next().getOmega() instanceof Domain);
			key = (Domain) keyIt.next().getOmega();

			Iterator<HasValueDomain> valueIt = gDomain
					.getHasValueDomainIncidences(EdgeDirection.OUT).iterator();
			if (!valueIt.hasNext()) {
				throw new GraphException(
						"No \"HasValueDomain\" has been defined.");
			}

			assert (valueIt.next() != null && valueIt.next().getOmega() instanceof Domain);
			value = (Domain) valueIt.next().getOmega();

			domain = schema.createMapDomain(queryDomain(key),
					queryDomain(value));

		} else if (gDomain instanceof ListDomain) {

			Iterator<HasBaseDomain> baseIt = gDomain
					.getHasBaseDomainIncidences(EdgeDirection.OUT).iterator();
			if (!baseIt.hasNext()) {
				throw new GraphException(
						"No \"HasBaseDomain\" has been defined.");
			}

			assert (baseIt.next() != null && baseIt.next().getOmega() instanceof Domain);
			Domain base = (Domain) baseIt.next().getOmega();

			domain = schema.createListDomain(queryDomain(base));

		} else if (gDomain instanceof SetDomain) {

			Iterator<HasBaseDomain> baseIt = gDomain
					.getHasBaseDomainIncidences(EdgeDirection.OUT).iterator();
			if (!baseIt.hasNext()) {
				throw new GraphException(
						"No \"HasBaseDomain\" has been defined.");
			}

			assert (baseIt.next() != null && baseIt.next().getOmega() instanceof Domain);
			Domain base = (Domain) baseIt.next().getOmega();

			domain = schema.createSetDomain(queryDomain(base));

		} else if (gDomain instanceof RecordDomain) {

			Map<String, de.uni_koblenz.jgralab.schema.Domain> recordComponents = new HashMap<String, de.uni_koblenz.jgralab.schema.Domain>();

			for (HasRecordDomainComponent hasRecordComponent : gDomain
					.getHasRecordDomainComponentIncidences(EdgeDirection.OUT)) {
				assert (hasRecordComponent.getOmega() instanceof Domain);

				recordComponents.put(hasRecordComponent.getName(),
						queryDomain((Domain) hasRecordComponent.getOmega()));
			}

			domain = schema.createRecordDomain(gDomain.getQualifiedName(),
					recordComponents);
		}

		if (domain == null) {
			throw new GraphException("No \"Domain\" has been created.");
		}

		domains.put(domain.getQualifiedName(), domain);

		return domain;
	}

	private de.uni_koblenz.jgralab.schema.Domain queryDomain(Domain gDomain) {

		de.uni_koblenz.jgralab.schema.Domain domain = domains.get(gDomain
				.getQualifiedName());

		if (domain == null) {
			domain = createDomain(gDomain);
		}
		return domain;
	}

	private void getAllGraphElementClassesAndDomains() {

		Iterator<ContainsDefaultPackage> it = gSchema
				.getContainsDefaultPackageIncidences(EdgeDirection.OUT)
				.iterator();

		if (!it.hasNext()) {
			throw new GraphException(
					"No \"ContainsDefaultPackage\" edge defined.");
		}

		assert (it.next() != null && it.next().getOmega() instanceof Package);

		Package defaultPackage = (Package) it.next().getOmega();

		gDomains = new HashMap<String, Domain>();
		gGraphElementClasses = new HashMap<String, GraphElementClass>();

		getAllGraphElementClassesAndDomains(defaultPackage);
	}

	private void getAllGraphElementClassesAndDomains(Package gPackage) {

		// DOMAINS

		getAllDomains(gPackage);

		// GRAPHELEMENTCLASSES

		getAllGraphElementClasses(gPackage);

		// SUBPACKAGES

		for (ContainsSubPackage containsSubPackage : gPackage
				.getContainsSubPackageIncidences(EdgeDirection.OUT)) {
			assert (containsSubPackage.getOmega() instanceof Package);

			getAllGraphElementClassesAndDomains((Package) containsSubPackage
					.getOmega());
		}
	}

	private void getAllGraphElementClasses(Package gPackage) {

		for (ContainsGraphElementClass containsGraphElementClass : gPackage
				.getContainsGraphElementClassIncidences(EdgeDirection.OUT)) {

			assert (containsGraphElementClass.getOmega() instanceof GraphElementClass);

			GraphElementClass graphElementClass = (GraphElementClass) containsGraphElementClass
					.getOmega();

			gGraphElementClasses.put(graphElementClass.getQualifiedName(),
					graphElementClass);
		}
	}

	private void getAllDomains(Package gPackage) {

		for (ContainsDomain containsDomain : gPackage
				.getContainsDomainIncidences(EdgeDirection.OUT)) {

			assert (containsDomain.getOmega() instanceof Domain);

			Domain domain = (Domain) containsDomain.getOmega();

			gDomains.put(domain.getQualifiedName(), domain);
		}
	}

	private void createSchema(SchemaGraph schemaGraph) {

		this.gSchema = schemaGraph.getFirstSchema();

		String name = gSchema.getName();
		String packagePrefix = gSchema.getPackagePrefix();

		schema = new SchemaImpl(name, packagePrefix);
	}

	private void createGraphClass() {

		assert (gSchema != null && gSchema
				.getDefinesGraphClassIncidences(EdgeDirection.OUT) != null);

		Iterator<DefinesGraphClass> it = gSchema
				.getDefinesGraphClassIncidences(EdgeDirection.OUT).iterator();

		if (!it.hasNext()) {
			throw new GraphException("No \"DefinesGraphClass\" edge defined.");
		}

		assert (it.next().getOmega() instanceof GraphClass);
		GraphClass gGraphClass = (GraphClass) it.next().getOmega();

		this.graphClass = schema.createGraphClass(gGraphClass
				.getQualifiedName());
	}
}
