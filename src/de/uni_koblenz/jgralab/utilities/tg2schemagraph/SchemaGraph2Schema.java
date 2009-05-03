package de.uni_koblenz.jgralab.utilities.tg2schemagraph;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
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
import de.uni_koblenz.jgralab.grumlschema.structure.AggregationClass;
import de.uni_koblenz.jgralab.grumlschema.structure.Attribute;
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
import de.uni_koblenz.jgralab.schema.AttributedElementClass;
import de.uni_koblenz.jgralab.schema.impl.ConstraintImpl;
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
	private Map<String, de.uni_koblenz.jgralab.schema.Domain> domains;

	private de.uni_koblenz.jgralab.schema.GraphClass graphClass;

	public SchemaGraph2Schema() {
	}

	public de.uni_koblenz.jgralab.schema.Schema convert(SchemaGraph schemaGraph) {

		createSchema(schemaGraph);

		createGraphClass();

		getAllGraphElementClassesAndDomains();

		createAllDomains();

		createAllGraphElementClasses();

		createAllGraphElementClasses();

		linkSuperClasses();

		return null;
	}

	private void linkSuperClasses() {

		for (Entry<String, GraphElementClass> entry : gGraphElementClasses
				.entrySet()) {

			AttributedElementClass element = schema
					.getAttributedElementClass(entry.getValue()
							.getQualifiedName());

			if (entry.getValue() instanceof VertexClass) {

				VertexClass gVertexClass = (VertexClass) entry.getValue();
				assert (element instanceof de.uni_koblenz.jgralab.schema.VertexClass);

				linkSuperClasses(
						(de.uni_koblenz.jgralab.schema.VertexClass) element,
						gVertexClass);

			} else {

				assert (entry.getValue() instanceof EdgeClass);
				EdgeClass gEdgeClass = (EdgeClass) entry.getValue();
				assert (element instanceof de.uni_koblenz.jgralab.schema.EdgeClass);

				linkSuperClasses(
						(de.uni_koblenz.jgralab.schema.EdgeClass) element,
						gEdgeClass);
			}
		}
	}

	private void linkSuperClasses(
			de.uni_koblenz.jgralab.schema.EdgeClass edgeClass,
			EdgeClass gEdgeClass) {

		ArrayList<de.uni_koblenz.jgralab.schema.EdgeClass> superClasses = new ArrayList<de.uni_koblenz.jgralab.schema.EdgeClass>();

		for (SpecializesEdgeClass specializesEdgeClass : gEdgeClass
				.getSpecializesEdgeClassIncidences()) {

			assert (specializesEdgeClass.getOmega() instanceof EdgeClass);

			EdgeClass gSuperClass = (EdgeClass) specializesEdgeClass.getOmega();
			AttributedElementClass superClass = schema
					.getAttributedElementClass(gSuperClass.getQualifiedName());
			assert (superClass instanceof de.uni_koblenz.jgralab.schema.EdgeClass);
			superClasses
					.add((de.uni_koblenz.jgralab.schema.EdgeClass) superClass);
		}

		for (de.uni_koblenz.jgralab.schema.EdgeClass superClass : superClasses) {
			edgeClass.addSuperClass(superClass);
		}
	}

	private void linkSuperClasses(
			de.uni_koblenz.jgralab.schema.VertexClass vertexClass,
			VertexClass gVertexClass) {

		ArrayList<de.uni_koblenz.jgralab.schema.VertexClass> superClasses = new ArrayList<de.uni_koblenz.jgralab.schema.VertexClass>();

		for (SpecializesVertexClass specializesVertexClass : gVertexClass
				.getSpecializesVertexClassIncidences()) {

			assert (specializesVertexClass.getOmega() instanceof VertexClass);

			VertexClass gSuperClass = (VertexClass) specializesVertexClass
					.getOmega();

			AttributedElementClass superClass = schema
					.getAttributedElementClass(gSuperClass.getQualifiedName());
			assert (superClass instanceof de.uni_koblenz.jgralab.schema.VertexClass);
			superClasses
					.add((de.uni_koblenz.jgralab.schema.VertexClass) superClass);
		}

		for (de.uni_koblenz.jgralab.schema.VertexClass superClass : superClasses) {
			vertexClass.addSuperClass(superClass);
		}
	}

	private void createAllGraphElementClasses() {

		de.uni_koblenz.jgralab.schema.Package defaultPackage = schema
				.getDefaultPackage();

		for (Entry<String, GraphElementClass> entry : gGraphElementClasses
				.entrySet()) {

			if (entry.getValue() instanceof EdgeClass) {
				continue;
			}

			createGraphElementClass(entry.getValue());
		}

		for (Entry<String, GraphElementClass> entry : gGraphElementClasses
				.entrySet()) {

			if (entry.getValue() instanceof VertexClass) {
				continue;
			}

			createGraphElementClass(entry.getValue());
		}
	}

	private de.uni_koblenz.jgralab.schema.GraphElementClass createGraphElementClass(
			GraphElementClass gElement) {

		de.uni_koblenz.jgralab.schema.GraphElementClass element = null;

		if (gElement instanceof VertexClass) {
			element = graphClass.createVertexClass(gElement.getQualifiedName());

		} else if (gElement instanceof EdgeClass) {

			EdgeClass gEdgeClass = (EdgeClass) gElement;

			Iterator<To> toIt = gEdgeClass.getToIncidences(EdgeDirection.OUT)
					.iterator();
			Iterator<From> fromIt = gEdgeClass.getFromIncidences(
					EdgeDirection.OUT).iterator();

			if (!toIt.hasNext() || !fromIt.hasNext()) {
				throw new GraphException(
						"No \"To\" or \"From\" edge has been defined.");
			}

			To gTo = toIt.next();
			From gFrom = fromIt.next();
			de.uni_koblenz.jgralab.schema.VertexClass to, from;
			int fromMin, fromMax, toMin, toMax;
			String fromRoleName, toRoleName;
			Set<String> fromRedefinedRoles, toRedefinedRoles;

			assert (gTo != null && gTo.getOmega() instanceof VertexClass
					&& gFrom != null && gFrom.getOmega() instanceof VertexClass);

			to = queryVertexClass((VertexClass) gTo.getOmega());
			toMin = gTo.getMin();
			toMax = gTo.getMax();
			toRoleName = gTo.getRoleName();
			toRedefinedRoles = gTo.getRedefinedRoles();

			from = queryVertexClass((VertexClass) gFrom.getOmega());
			fromMin = gFrom.getMin();
			fromMax = gFrom.getMax();
			fromRoleName = gFrom.getRoleName();
			fromRedefinedRoles = gFrom.getRedefinedRoles();

			String qualifiedName = gElement.getQualifiedName();
			boolean isAggegatedFrom = (gElement instanceof AggregationClass) ? ((AggregationClass) gElement)
					.isAggregateFrom()
					: false;

			de.uni_koblenz.jgralab.schema.EdgeClass edgeClass;

			if (gElement instanceof CompositionClass) {

				edgeClass = graphClass.createCompositionClass(qualifiedName,
						from, fromMin, fromMax, fromRoleName, isAggegatedFrom,
						to, toMin, toMax, toRoleName);

			} else if (gElement instanceof AggregationClass) {

				edgeClass = graphClass.createAggregationClass(qualifiedName,
						from, fromMin, fromMax, fromRoleName, isAggegatedFrom,
						to, toMin, toMax, toRoleName);

			} else {

				edgeClass = graphClass.createEdgeClass(qualifiedName, from,
						fromMin, fromMax, fromRoleName, to, toMin, toMax,
						toRoleName);
			}

			edgeClass.getRedefinedFromRoles().addAll(fromRedefinedRoles);
			edgeClass.getRedefinedFromRoles().addAll(toRedefinedRoles);
			edgeClass.setAbstract(gEdgeClass.isIsAbstract());
		}

		if (element == null) {
			throw new GraphException("FIXME!");
		}

		createAllAttributes(element, gElement);
		createAllConstraints(element, gElement);

		return element;
	}

	private de.uni_koblenz.jgralab.schema.VertexClass queryVertexClass(
			GraphElementClass gElement) {
		de.uni_koblenz.jgralab.schema.AttributedElementClass element = schema
				.getAttributedElementClass(gElement.getQualifiedName());
		if (element == null) {
			element = createGraphElementClass(gElement);
		}

		return (element instanceof de.uni_koblenz.jgralab.schema.VertexClass) ? (de.uni_koblenz.jgralab.schema.VertexClass) element
				: null;
	}

	private void createAllConstraints(
			de.uni_koblenz.jgralab.schema.GraphElementClass element,
			GraphElementClass gElement) {

		for (HasConstraint hasConstraint : gElement
				.getHasConstraintIncidences(EdgeDirection.OUT)) {
			assert (hasConstraint != null && hasConstraint.getOmega() instanceof Constraint);

			Constraint constraint = (Constraint) hasConstraint.getOmega();

			element.addConstraint(new ConstraintImpl(constraint.getMessage(),
					constraint.getPredicateQuery(), constraint
							.getOffendingElementsQuery()));
		}
	}

	private void createAllAttributes(
			de.uni_koblenz.jgralab.schema.GraphElementClass element,
			GraphElementClass gElement) {

		for (HasAttribute hasAttribute : gElement
				.getHasAttributeIncidences(EdgeDirection.OUT)) {
			assert (hasAttribute != null && hasAttribute.getOmega() instanceof Attribute);

			Attribute attribute = (Attribute) hasAttribute.getOmega();

			Iterator<HasDomain> it = attribute.getHasDomainIncidences(
					EdgeDirection.OUT).iterator();
			if (!it.hasNext()) {
				throw new GraphException(
						"No \"HasDomain\" edge has been defined.");
			}
			assert (it.next() != null && it.next().getOmega() instanceof Domain);

			element.addAttribute(attribute.getName(), queryDomain((Domain) it
					.next().getOmega()));
		}
	}

	private void createAllDomains() {

		domains = new HashMap<String, de.uni_koblenz.jgralab.schema.Domain>();
		domains.putAll(schema.getDomains());

		for (Entry<String, Domain> entry : gDomains.entrySet()) {

			createDomain(entry.getValue());
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
