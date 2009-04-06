package de.uni_koblenz.jgralab.utilities.tg2schemagraph;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import de.uni_koblenz.jgralab.WorkInProgress;
import de.uni_koblenz.jgralab.grumlschema.SchemaGraph;
import de.uni_koblenz.jgralab.grumlschema.domains.Domain;
import de.uni_koblenz.jgralab.grumlschema.impl.SchemaGraphImpl;
import de.uni_koblenz.jgralab.grumlschema.structure.AggregationClass;
import de.uni_koblenz.jgralab.grumlschema.structure.Attribute;
import de.uni_koblenz.jgralab.grumlschema.structure.AttributedElementClass;
import de.uni_koblenz.jgralab.grumlschema.structure.Constraint;
import de.uni_koblenz.jgralab.grumlschema.structure.EdgeClass;
import de.uni_koblenz.jgralab.grumlschema.structure.From;
import de.uni_koblenz.jgralab.grumlschema.structure.GraphClass;
import de.uni_koblenz.jgralab.grumlschema.structure.Package;
import de.uni_koblenz.jgralab.grumlschema.structure.Schema;
import de.uni_koblenz.jgralab.grumlschema.structure.To;
import de.uni_koblenz.jgralab.grumlschema.structure.VertexClass;

/**
 * Allows to convert a Schema to a SchemaGraph.
 * 
 * @author mmce
 */

@WorkInProgress(responsibleDevelopers = "mmce")
public class Schema2SchemaGraph {

	private Schema gSchema;
	private de.uni_koblenz.jgralab.schema.Schema schema;

	private Package gDefaultPackage;
	private de.uni_koblenz.jgralab.schema.Package defaultPackage;

	private GraphClass gGraphClass;
	private de.uni_koblenz.jgralab.schema.GraphClass graphClass;

	private Map<de.uni_koblenz.jgralab.schema.Package, Package> packageMap;
	private Map<de.uni_koblenz.jgralab.schema.AttributedElementClass, AttributedElementClass> attributedElementClassMap;
	private Map<de.uni_koblenz.jgralab.schema.VertexClass, VertexClass> vertexClassMap;
	private Map<de.uni_koblenz.jgralab.schema.EdgeClass, EdgeClass> edgeClassMap;
	private Map<de.uni_koblenz.jgralab.schema.Domain, Domain> domainMap;

	private SchemaGraph schemaGraph;

	public Schema2SchemaGraph() {

	}

	private void setUp() {

		packageMap = new HashMap<de.uni_koblenz.jgralab.schema.Package, Package>();
		attributedElementClassMap = new HashMap<de.uni_koblenz.jgralab.schema.AttributedElementClass, AttributedElementClass>();
		domainMap = new HashMap<de.uni_koblenz.jgralab.schema.Domain, Domain>();
		vertexClassMap = new HashMap<de.uni_koblenz.jgralab.schema.VertexClass, VertexClass>();
		edgeClassMap = new HashMap<de.uni_koblenz.jgralab.schema.EdgeClass, EdgeClass>();

		schemaGraph = new SchemaGraphImpl();

	}

	private void tearDown() {
		packageMap = null;
		attributedElementClassMap = null;
		domainMap = null;
		vertexClassMap = null;
		edgeClassMap = null;

		defaultPackage = null;
		gDefaultPackage = null;
		graphClass = null;
		gGraphClass = null;

		schemaGraph = null;

		System.gc();
		System.runFinalization();
	}

	public SchemaGraph convert2SchemaGraph(
			de.uni_koblenz.jgralab.schema.Schema schema) {

		setUp();

		this.schema = schema;

		createSchema();

		createGraphClass();

		createPackages();

		createDomains();

		createVertexClasses();

		createEdgeClasses();

		createSpecializations();

		createAttributes();

		createConstraints();

		createEdges();

		SchemaGraph schemaGraph = this.schemaGraph;

		tearDown();

		return schemaGraph;
	}

	private void createEdges() {

		for (Entry<de.uni_koblenz.jgralab.schema.EdgeClass, EdgeClass> entry : edgeClassMap
				.entrySet()) {
			createEdges(entry.getKey(), entry.getValue());
		}
	}

	private void createEdges(de.uni_koblenz.jgralab.schema.EdgeClass edgeClass,
			EdgeClass gEdgeClass) {

		VertexClass vertexClass = vertexClassMap.get(edgeClass.getTo());
		To to = schemaGraph.createTo(gEdgeClass, vertexClass);
		to.setMin(edgeClass.getToMin());
		to.setMax(edgeClass.getToMax());
		to.setRoleName(edgeClass.getToRolename());
		to.setRedefinedRoles(edgeClass.getRedefinedToRoles());

		vertexClass = vertexClassMap.get(edgeClass.getFrom());
		From from = schemaGraph.createFrom(gEdgeClass, vertexClass);
		from.setMin(edgeClass.getFromMin());
		from.setMax(edgeClass.getFromMax());
		from.setRoleName(edgeClass.getFromRolename());
		from.setRedefinedRoles(edgeClass.getRedefinedFromRoles());

	}

	private void createConstraints() {
		for (Entry<de.uni_koblenz.jgralab.schema.AttributedElementClass, AttributedElementClass> entry : attributedElementClassMap
				.entrySet()) {
			createConstraints(entry.getKey(), entry.getValue());
		}
	}

	private void createConstraints(
			de.uni_koblenz.jgralab.schema.AttributedElementClass element,
			AttributedElementClass gElement) {

		Constraint gConstraint;

		for (de.uni_koblenz.jgralab.schema.Constraint constraint : element
				.getConstraints()) {
			gConstraint = schemaGraph.createConstraint();
			gConstraint.setMessage(constraint.getMessage());
			gConstraint.setPredicateQuery(constraint.getPredicate());
			gConstraint.setOffendingElementsQuery(constraint
					.getOffendingElementsQuery());

			schemaGraph.createHasConstraint(gElement, gConstraint);
		}

	}

	private void createAttributes() {
		for (Entry<de.uni_koblenz.jgralab.schema.AttributedElementClass, AttributedElementClass> entry : attributedElementClassMap
				.entrySet()) {
			createAttributes(entry.getKey(), entry.getValue());
		}
	}

	private void createAttributes(
			de.uni_koblenz.jgralab.schema.AttributedElementClass element,
			AttributedElementClass gElement) {

		Attribute gAttribute;
		Domain gDomain;

		for (de.uni_koblenz.jgralab.Attribute attribute : element
				.getAttributeList()) {

			gAttribute = schemaGraph.createAttribute();
			gAttribute.setName(attribute.getName());

			gDomain = domainMap.get(attribute.getDomain());
			assert (gDomain != null) : "";

			schemaGraph.createHasAttribute(gElement, gAttribute);
			schemaGraph.createHasDomain(gAttribute, gDomain);
		}
	}

	private void createSpecializations() {

		for (Entry<de.uni_koblenz.jgralab.schema.VertexClass, VertexClass> entry : vertexClassMap
				.entrySet()) {
			for (de.uni_koblenz.jgralab.schema.AttributedElementClass superClass : entry
					.getKey().getDirectSuperClasses()) {
				schemaGraph.createSpecializesVertexClass(entry.getValue(),
						vertexClassMap.get(superClass));
			}
		}

		for (Entry<de.uni_koblenz.jgralab.schema.EdgeClass, EdgeClass> entry : edgeClassMap
				.entrySet()) {
			for (de.uni_koblenz.jgralab.schema.AttributedElementClass superClass : entry
					.getKey().getDirectSuperClasses()) {
				schemaGraph.createSpecializesEdgeClass(entry.getValue(),
						edgeClassMap.get(superClass));
			}
		}
	}

	private void createEdgeClasses() {

		for (Entry<de.uni_koblenz.jgralab.schema.Package, Package> entry : packageMap
				.entrySet()) {
			createEdgeClasses(entry.getKey(), entry.getValue());
		}
	}

	private void createVertexClasses() {

		for (Entry<de.uni_koblenz.jgralab.schema.Package, Package> entry : packageMap
				.entrySet()) {
			createVertexClasses(entry.getKey(), entry.getValue());
		}
	}

	private void createDomains() {

		for (Entry<de.uni_koblenz.jgralab.schema.Package, Package> entry : packageMap
				.entrySet()) {
			createDomains(entry.getKey(), entry.getValue());
		}
	}

	private void createPackages() {
		createDefaultPackage();

		createSubPackages(defaultPackage, gDefaultPackage);
	}

	private void createDefaultPackage() {
		defaultPackage = schema.getDefaultPackage();
		gDefaultPackage = schemaGraph.createPackage();

		gDefaultPackage.setQualifiedName(defaultPackage.getQualifiedName());
		packageMap.put(defaultPackage, gDefaultPackage);

		schemaGraph.createContainsDefaultPackage(gSchema, gDefaultPackage);
	}

	private void createEdgeClasses(
			de.uni_koblenz.jgralab.schema.Package Package, Package gPackage) {

		EdgeClass gEdgeClass;

		for (de.uni_koblenz.jgralab.schema.EdgeClass edgeClass : Package
				.getEdgeClasses().values()) {

			gEdgeClass = createEdgeClass(edgeClass);

			attributedElementClassMap.put(edgeClass, gEdgeClass);
			edgeClassMap.put(edgeClass, gEdgeClass);
			schemaGraph.createContainsGraphElementClass(gPackage, gEdgeClass);
		}
	}

	private EdgeClass createEdgeClass(
			de.uni_koblenz.jgralab.schema.EdgeClass edgeClass) {

		EdgeClass gEdgeClass = null;

		if (edgeClass instanceof de.uni_koblenz.jgralab.schema.AggregationClass) {
			AggregationClass gAggregationClass;
			if (edgeClass instanceof de.uni_koblenz.jgralab.schema.CompositionClass) {
				gAggregationClass = schemaGraph.createCompositionClass();
			} else {
				gAggregationClass = schemaGraph.createAggregationClass();
			}

			de.uni_koblenz.jgralab.schema.AggregationClass aggregationClass = (de.uni_koblenz.jgralab.schema.AggregationClass) edgeClass;
			gAggregationClass.setAggregateFrom(aggregationClass
					.isAggregateFrom());
			gEdgeClass = gAggregationClass;

		} else {
			gEdgeClass = schemaGraph.createEdgeClass();
		}

		gEdgeClass.setIsAbstract(edgeClass.isAbstract());
		gEdgeClass.setQualifiedName(edgeClass.getQualifiedName());

		assert (gEdgeClass != null);
		return gEdgeClass;
	}

	private void createVertexClasses(
			de.uni_koblenz.jgralab.schema.Package Package, Package gPackage) {

		VertexClass gVertexClass;
		for (de.uni_koblenz.jgralab.schema.VertexClass vertexClass : Package
				.getVertexClasses().values()) {

			gVertexClass = schemaGraph.createVertexClass();
			gVertexClass.setIsAbstract(vertexClass.isAbstract());
			gVertexClass.setQualifiedName(vertexClass.getQualifiedName());

			attributedElementClassMap.put(vertexClass, gVertexClass);
			vertexClassMap.put(vertexClass, gVertexClass);

			schemaGraph.createContainsGraphElementClass(gPackage, gVertexClass);
		}
	}

	private void createSubPackages(
			de.uni_koblenz.jgralab.schema.Package Package, Package gPackage) {

		Package gSubPackage;

		for (de.uni_koblenz.jgralab.schema.Package subPackage : Package
				.getSubPackages().values()) {
			gSubPackage = schemaGraph.createPackage();
			gSubPackage.setQualifiedName(subPackage.getQualifiedName());

			createDomains(subPackage, gSubPackage);

			schemaGraph.createContainsSubPackage(gPackage, gSubPackage);
			createSubPackages(subPackage, gSubPackage);
		}
	}

	private void createDomains(de.uni_koblenz.jgralab.schema.Package Package,
			Package gPackage) {

		Domain gDomain;
		for (de.uni_koblenz.jgralab.schema.Domain domain : Package.getDomains()
				.values()) {
			gDomain = createDomain(domain);
			domainMap.put(domain, gDomain);

			schemaGraph.createContainsDomain(gPackage, gDomain);
		}
	}

	private Domain createDomain(de.uni_koblenz.jgralab.schema.Domain domain) {

		assert (schemaGraph != null);
		assert (domain != null);

		Domain gDomain = null;

		if (domain instanceof de.uni_koblenz.jgralab.schema.BooleanDomain) {
			gDomain = schemaGraph.createBooleanDomain();
		} else if (domain instanceof de.uni_koblenz.jgralab.schema.IntDomain) {
			gDomain = schemaGraph.createIntDomain();
		} else if (domain instanceof de.uni_koblenz.jgralab.schema.LongDomain) {
			gDomain = schemaGraph.createLongDomain();
		} else if (domain instanceof de.uni_koblenz.jgralab.schema.DoubleDomain) {
			gDomain = schemaGraph.createDoubleDomain();
		} else if (domain instanceof de.uni_koblenz.jgralab.schema.StringDomain) {
			gDomain = schemaGraph.createStringDomain();
		} else if (domain instanceof de.uni_koblenz.jgralab.schema.RecordDomain) {
			gDomain = schemaGraph.createRecordDomain();
		} else if (domain instanceof de.uni_koblenz.jgralab.schema.ListDomain) {
			gDomain = schemaGraph.createListDomain();
		} else if (domain instanceof de.uni_koblenz.jgralab.schema.SetDomain) {
			gDomain = schemaGraph.createSetDomain();
		} else if (domain instanceof de.uni_koblenz.jgralab.schema.MapDomain) {
			gDomain = schemaGraph.createMapDomain();
		} else if (domain instanceof de.uni_koblenz.jgralab.schema.EnumDomain) {
			gDomain = schemaGraph.createEnumDomain();
		} else {
			throw new RuntimeException("FIXME: Unforseen domain occured! "
					+ domain);
		}

		assert (gDomain != null);
		gDomain.setQualifiedName(domain.getQualifiedName());
		return gDomain;
	}

	/**
	 * Creates a Schema in the SchemaGraph.
	 */
	private void createSchema() {

		assert (schemaGraph != null) : "No SchemaGraph created! (setUp()-Methode may not been executed.)";
		assert (schema != null) : "No schema defined!";

		gSchema = schemaGraph.createSchema();

		gSchema.setName(schema.getQualifiedName());
		gSchema.setPackagePrefix(schema.getQualifiedName());
	}

	private void createGraphClass() {

		assert (schemaGraph != null);
		assert (schema != null);
		assert (gSchema != null);

		this.graphClass = schema.getDefaultGraphClass();
		this.gGraphClass = schemaGraph.createGraphClass();

		// Is needed to reference to the new AttributedElementClass-objects.
		attributedElementClassMap.put(graphClass, gGraphClass);

		gGraphClass.setIsAbstract(graphClass.isAbstract());
		gGraphClass.setQualifiedName(graphClass.getQualifiedName());

		schemaGraph.createDefinesGraphClass(gSchema, gGraphClass);
	}
}
