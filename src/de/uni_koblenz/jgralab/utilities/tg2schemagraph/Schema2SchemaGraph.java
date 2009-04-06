package de.uni_koblenz.jgralab.utilities.tg2schemagraph;

import java.util.HashMap;
import java.util.Map;

import de.uni_koblenz.jgralab.WorkInProgress;
import de.uni_koblenz.jgralab.grumlschema.SchemaGraph;
import de.uni_koblenz.jgralab.grumlschema.domains.Domain;
import de.uni_koblenz.jgralab.grumlschema.impl.SchemaGraphImpl;
import de.uni_koblenz.jgralab.grumlschema.structure.Attribute;
import de.uni_koblenz.jgralab.grumlschema.structure.AttributedElementClass;
import de.uni_koblenz.jgralab.grumlschema.structure.GraphClass;
import de.uni_koblenz.jgralab.grumlschema.structure.Package;
import de.uni_koblenz.jgralab.grumlschema.structure.Schema;
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
	private Map<de.uni_koblenz.jgralab.schema.Domain, Domain> domainMap;
	private Map<de.uni_koblenz.jgralab.Attribute, Attribute> attributeMap;

	private SchemaGraph schemaGraph;

	public Schema2SchemaGraph() {

	}

	private void setUp() {

		packageMap = new HashMap<de.uni_koblenz.jgralab.schema.Package, Package>();
		attributedElementClassMap = new HashMap<de.uni_koblenz.jgralab.schema.AttributedElementClass, AttributedElementClass>();
		domainMap = new HashMap<de.uni_koblenz.jgralab.schema.Domain, Domain>();
		attributeMap = new HashMap<de.uni_koblenz.jgralab.Attribute, Attribute>();

		schemaGraph = new SchemaGraphImpl();

	}

	private void tearDown() {
		packageMap = null;
		attributedElementClassMap = null;
		domainMap = null;
		attributeMap = null;

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

		createDefaultPackage();

		SchemaGraph schemaGraph = this.schemaGraph;

		tearDown();

		return schemaGraph;
	}

	private void createDefaultPackage() {
		defaultPackage = schema.getDefaultPackage();
		gDefaultPackage = schemaGraph.createPackage();

		gDefaultPackage.setQualifiedName(defaultPackage.getQualifiedName());
		packageMap.put(defaultPackage, gDefaultPackage);

		schemaGraph.createContainsDefaultPackage(gSchema, gDefaultPackage);

		createDomains(defaultPackage, gDefaultPackage);

		createVertexClasses(defaultPackage, gDefaultPackage);

		createEdgeClasses(defaultPackage, gDefaultPackage);

		createSubPackages(defaultPackage, gDefaultPackage);
	}

	private void createEdgeClasses(
			de.uni_koblenz.jgralab.schema.Package defaultPackage2,
			Package defaultPackage3) {
		// TODO Auto-generated method stub

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

			schemaGraph.createContainsGraphElementClass(gPackage, gVertexClass);
		}
	}

	private void createSubPackages(
			de.uni_koblenz.jgralab.schema.Package Package, Package gPackage) {

		Package gSubPackage;

		for (de.uni_koblenz.jgralab.schema.Package subPackage : defaultPackage
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
		}
		if (domain instanceof de.uni_koblenz.jgralab.schema.IntDomain) {
			gDomain = schemaGraph.createIntDomain();
		}
		if (domain instanceof de.uni_koblenz.jgralab.schema.LongDomain) {
			gDomain = schemaGraph.createLongDomain();
		}
		if (domain instanceof de.uni_koblenz.jgralab.schema.DoubleDomain) {
			gDomain = schemaGraph.createDoubleDomain();
		}
		if (domain instanceof de.uni_koblenz.jgralab.schema.StringDomain) {
			gDomain = schemaGraph.createStringDomain();
		}
		if (domain instanceof de.uni_koblenz.jgralab.schema.RecordDomain) {
			gDomain = schemaGraph.createRecordDomain();
		}
		if (domain instanceof de.uni_koblenz.jgralab.schema.ListDomain) {
			gDomain = schemaGraph.createListDomain();
		}
		if (domain instanceof de.uni_koblenz.jgralab.schema.SetDomain) {
			gDomain = schemaGraph.createSetDomain();
		}
		if (domain instanceof de.uni_koblenz.jgralab.schema.MapDomain) {
			gDomain = schemaGraph.createMapDomain();
		}
		if (domain instanceof de.uni_koblenz.jgralab.schema.EnumDomain) {
			gDomain = schemaGraph.createEnumDomain();
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
