package de.uni_koblenz.jgralab.utilities.tg2schemagraph;

import java.util.ArrayList;
import java.util.ConcurrentModificationException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import de.uni_koblenz.jgralab.grumlschema.SchemaGraph;
import de.uni_koblenz.jgralab.grumlschema.domains.CollectionDomain;
import de.uni_koblenz.jgralab.grumlschema.domains.Domain;
import de.uni_koblenz.jgralab.grumlschema.domains.EnumDomain;
import de.uni_koblenz.jgralab.grumlschema.domains.HasBaseDomain;
import de.uni_koblenz.jgralab.grumlschema.domains.HasRecordDomainComponent;
import de.uni_koblenz.jgralab.grumlschema.domains.MapDomain;
import de.uni_koblenz.jgralab.grumlschema.domains.RecordDomain;
import de.uni_koblenz.jgralab.grumlschema.impl.SchemaGraphImpl;
import de.uni_koblenz.jgralab.grumlschema.structure.AggregationClass;
import de.uni_koblenz.jgralab.grumlschema.structure.Attribute;
import de.uni_koblenz.jgralab.grumlschema.structure.AttributedElementClass;
import de.uni_koblenz.jgralab.grumlschema.structure.Constraint;
import de.uni_koblenz.jgralab.grumlschema.structure.ContainsDefaultPackage;
import de.uni_koblenz.jgralab.grumlschema.structure.ContainsDomain;
import de.uni_koblenz.jgralab.grumlschema.structure.ContainsGraphElementClass;
import de.uni_koblenz.jgralab.grumlschema.structure.ContainsSubPackage;
import de.uni_koblenz.jgralab.grumlschema.structure.EdgeClass;
import de.uni_koblenz.jgralab.grumlschema.structure.From;
import de.uni_koblenz.jgralab.grumlschema.structure.GraphClass;
import de.uni_koblenz.jgralab.grumlschema.structure.HasAttribute;
import de.uni_koblenz.jgralab.grumlschema.structure.HasConstraint;
import de.uni_koblenz.jgralab.grumlschema.structure.HasDomain;
import de.uni_koblenz.jgralab.grumlschema.structure.Package;
import de.uni_koblenz.jgralab.grumlschema.structure.Schema;
import de.uni_koblenz.jgralab.grumlschema.structure.SpecializesEdgeClass;
import de.uni_koblenz.jgralab.grumlschema.structure.SpecializesVertexClass;
import de.uni_koblenz.jgralab.grumlschema.structure.To;
import de.uni_koblenz.jgralab.grumlschema.structure.VertexClass;

/**
 * Converts a Schema to a SchemaGraph. This class is mend to be a reusable
 * converter class. This class is not thread safe!
 * 
 * Note for Developers:
 * 
 * All variables are written like their classes from the package
 * "de.uni_koblenz.jgralab.schema" normal with the exception of the variable for
 * packages. "package" is a keyword. In this case the variable is written with a
 * prefix "x". All variables from the package
 * "de.uni_koblenz.jgralab.grumlschema.structure" are written with an prefix
 * "g".
 * 
 * All types from "de.uni_koblenz.jgralab.schema" are fully qualified with their
 * package name.
 * 
 * @author ist@uni-koblenz.de, Eckhard Großmann
 */
public class Schema2SchemaGraph {

	/**
	 * Schema, which will be converted.
	 */
	private de.uni_koblenz.jgralab.schema.Schema schema;

	/**
	 * DefaultPackage of the SchemaGraph.
	 */
	private Package gDefaultPackage;

	/**
	 * DefaultPackage of the given Schema.
	 */
	private de.uni_koblenz.jgralab.schema.Package defaultPackage;

	/**
	 * Semaphore to prevent any additional conversion process.
	 */
	private boolean workInProgress;

	/**
	 * Map to reference a Package of a Schema to a Package of a SchemaGraph.
	 */
	private Map<de.uni_koblenz.jgralab.schema.Package, Package> packageMap;

	/**
	 * Map to reference a AttributedElementClass of a Schema to a
	 * AttributedElementClass of a SchemaGraph.
	 */
	private Map<de.uni_koblenz.jgralab.schema.AttributedElementClass, AttributedElementClass> attributedElementClassMap;

	/**
	 * Map to reference a VertexClass of a Schema to a VertexClass of a
	 * SchemaGraph.
	 */
	private Map<de.uni_koblenz.jgralab.schema.VertexClass, VertexClass> vertexClassMap;

	/**
	 * Map to reference a EdgeClass of a Schema to a EdgeClass of a SchemaGraph.
	 */
	private Map<de.uni_koblenz.jgralab.schema.EdgeClass, EdgeClass> edgeClassMap;

	/**
	 * Map to reference a Domain of a Schema to a Domain of a SchemaGraph.
	 */
	private Map<de.uni_koblenz.jgralab.schema.Domain, Domain> domainMap;

	private SchemaGraph schemaGraph;

	/**
	 * Constructs a converter for transforming a Schema to a SchemaGraph. This
	 * constructor is empty, because the real work is done by the method
	 * convert2SchemaGraph(Schema).
	 */
	public Schema2SchemaGraph() {
	}

	/**
	 * SetUp method, which instantiates all necessary resources.
	 */
	private void setUp() {

		workInProgress = true;

		packageMap = new HashMap<de.uni_koblenz.jgralab.schema.Package, Package>();
		attributedElementClassMap = new HashMap<de.uni_koblenz.jgralab.schema.AttributedElementClass, AttributedElementClass>();
		domainMap = new HashMap<de.uni_koblenz.jgralab.schema.Domain, Domain>();
		vertexClassMap = new HashMap<de.uni_koblenz.jgralab.schema.VertexClass, VertexClass>();
		edgeClassMap = new HashMap<de.uni_koblenz.jgralab.schema.EdgeClass, EdgeClass>();

		schemaGraph = new SchemaGraphImpl();
	}

	/**
	 * Sets all member variables to null to indirectly free resources and
	 * performs a garbage collection.
	 */
	private void tearDown() {
		// All member variables are set to null
		// This should free resources after a garbage collection
		packageMap = null;
		attributedElementClassMap = null;
		domainMap = null;
		vertexClassMap = null;
		edgeClassMap = null;

		defaultPackage = null;
		gDefaultPackage = null;

		schemaGraph = null;

		// Calls the garbage collector
		System.gc();
		System.runFinalization();

		workInProgress = false;
	}

	/**
	 * Converts a given Schema to a SchemaGraph and returns it.
	 * 
	 * @param schema
	 *            Schema, which should be convert to a SchemaGraph.
	 * @return New SchemaGraph object.
	 */
	public SchemaGraph convert2SchemaGraph(
			de.uni_koblenz.jgralab.schema.Schema schema) {

		// Guards the access of this object
		if (workInProgress) {
			throw new ConcurrentModificationException("WorkInProgress");
		}

		// Sets all resources up.
		setUp();

		if (schema == null) {
			throw new IllegalArgumentException("Schema is a NullReference.");
		}

		this.schema = schema;
		assert (checkSchemaAndSchemaGraph());

		// Creates the Schema
		createSchema();

		// Creates the GraphClass
		createGraphClass();

		// Creates all Packages
		createPackages();

		// Creates all Domains
		createDomains();

		// Creates all VertexClasses
		createVertexClasses();

		// Creates all EdgeClasses
		createEdgeClasses();

		// Creates all SpecializationEdges
		createSpecializations();

		// Creates all Attributes
		createAttributes();

		// Creates all Constraints
		createConstraints();

		// Creates all "From" and "To" Edges
		createEdges();

		// Stores the schemaGraph object, so that it will not be lost after
		// calling the tearDown Method.
		SchemaGraph schemaGraph = this.schemaGraph;

		// Frees all used and no longer needed resources
		tearDown();

		return schemaGraph;
	}

	/**
	 * Creates a corresponding Schema in the SchemaGraph to the existing Schema.
	 */
	private void createSchema() {

		assert (checkSchemaAndSchemaGraph());

		// Creates a Schema in a SchemaGraph
		Schema gSchema = schemaGraph.createSchema();
		assert (gSchema != null) : "FIXME! No Schema has been created";

		assert (schema.getName() != null) : "FIXME! Schema name of a Schema shouldn't be null.";
		assert schema.getPackagePrefix() != null : "FIXME! Package prefix should exist.";

		// Sets all necessary Attributes
		gSchema.setName(schema.getName());
		gSchema.setPackagePrefix(schema.getPackagePrefix());
	}

	private boolean checkSchemaAndSchemaGraph() {

		assert (schemaGraph != null) : "SchemaGraph has not been created.";
		assert (schema != null) : "Given Schema is null";

		return true;
	}

	/**
	 * Creates to the existing GraphClass a corresponding GraphClass in the
	 * SchemaGraph. Also the GraphClass is linked to the new Schema.
	 */
	private void createGraphClass() {

		assert (checkSchemaAndSchemaGraph());

		Schema gSchema = schemaGraph.getFirstSchema();
		assert (gSchema != null) : "FIXME! No Schema has been created!";

		// Creates the new GraphClass in the SchemaGraph.
		GraphClass gGraphClass = schemaGraph.createGraphClass();
		assert (gGraphClass != null) : "FIXME! No GraphClass has been created!";
		de.uni_koblenz.jgralab.schema.GraphClass graphClass = schema
				.getGraphClass();

		assert ((graphClass != null) && !graphClass.isInternal()) : "There have to be a GraphClass, which isn't internal!";

		assert (attributedElementClassMap != null);
		// Is needed to reference to the new AttributedElementClass-objects.
		attributedElementClassMap.put(graphClass, gGraphClass);

		// Sets all general attributes of the GraphClass.
		gGraphClass.setIsAbstract(graphClass.isAbstract());
		assert (graphClass.getQualifiedName() != null) : "FIXME! GraphClass is broken. No QualifiedName is set.";
		gGraphClass.setQualifiedName(graphClass.getQualifiedName());

		// Links the new GraphClass to the new Schema.
		schemaGraph.createDefinesGraphClass(gSchema, gGraphClass);
	}

	/**
	 * Traverses all Packages and creates corresponding Packages in the
	 * SchemaGraph.
	 */
	private void createPackages() {
		// Creates first the DefaultPackage
		createDefaultPackage();

		// Creates all SubPackages of the defaultPackage
		// This method creates recursively
		createSubPackages(defaultPackage, gDefaultPackage);
	}

	/**
	 * Creates a corresponding DefaultPackage to the DefaultPackage in the given
	 * Schema.
	 */
	private void createDefaultPackage() {

		assert (checkSchemaAndSchemaGraph());

		// Creates a new DefaultPackage in the SchemaGraph
		gDefaultPackage = schemaGraph.createPackage();
		defaultPackage = schema.getDefaultPackage();

		assert (gDefaultPackage != null) : "FIXEME! No DefaultPackage has been created!";
		assert (defaultPackage != null) : "FIXME! No DefaultPackage is defined in the given Schema.";

		// Sets all general attributes
		assert (defaultPackage.getQualifiedName() != null) : "FIXME! Package has not QualifiedName defined.";
		gDefaultPackage.setQualifiedName(defaultPackage.getQualifiedName());
		packageMap.put(defaultPackage, gDefaultPackage);

		// Links the new DefaultPackage to the new Schema
		ContainsDefaultPackage link = schemaGraph.createContainsDefaultPackage(
				schemaGraph.getFirstSchema(), gDefaultPackage);
		assert (link != null) : "FIXME! No ContainsDefaultPackage link has been created.";
	}

	/**
	 * Creates all subpackages of the given Package and links them to the
	 * corresponding given Package of the SchemaGraph.
	 * 
	 * @param xPackage
	 *            Package of the given Schema, of which all subpackages should
	 *            be created.
	 * @param gPackage
	 *            Package of the SchemaGraph, to which all new created
	 *            subpackages should be linked to.
	 */
	private void createSubPackages(
			de.uni_koblenz.jgralab.schema.Package xPackage, Package gPackage) {

		Package gSubPackage;

		assert (checkSchemaAndSchemaGraph());

		// Loop over all subpackages of the given Package.
		for (de.uni_koblenz.jgralab.schema.Package subPackage : xPackage
				.getSubPackages().values()) {

			assert ((subPackage != null) && (subPackage.getQualifiedName() != null)) : "FIXME! This Package has no QualifiedName defined!";

			// Creates the subpackage and sets the QualifiedName.
			gSubPackage = schemaGraph.createPackage();
			assert (gSubPackage != null) : "FIXME! A Package wasn't created.";
			gSubPackage.setQualifiedName(subPackage.getQualifiedName());

			// Stores the Package for further linking
			packageMap.put(subPackage, gSubPackage);

			// Links the new Package to its parent Package.
			ContainsSubPackage link = schemaGraph.createContainsSubPackage(
					gPackage, gSubPackage);
			assert (link != null) : "FIXME! Link ContainsSubPackage has not been created!";

			// All subpackages of the new Package are created.
			createSubPackages(subPackage, gSubPackage);
		}
	}

	/**
	 * Creates all Domains of all known Package objects.
	 */
	private void createDomains() {

		// Loop over all Packages
		for (Entry<de.uni_koblenz.jgralab.schema.Package, Package> entry : packageMap
				.entrySet()) {

			// Loop over all Domains in the current Package
			for (de.uni_koblenz.jgralab.schema.Domain domain : entry.getKey()
					.getDomains().values()) {
				createDomain(domain);
			}
		}
	}

	/**
	 * Creates to a given Domain a corresponding Domain in the SchemaGraph and
	 * links it to its Package.
	 * 
	 * @param domain
	 *            Domain of which a a corresponding Domain is created.
	 * @return New Domain.
	 */
	private Domain createDomain(de.uni_koblenz.jgralab.schema.Domain domain) {

		assert (checkSchemaAndSchemaGraph());

		assert (domain != null) : "FIXME! Domain is not set.";

		Domain gDomain = null;

		// In the case of an existing Domain, no new Domain is created.
		if (domainMap.containsKey(domain)) {
			gDomain = domainMap.get(domain);
		} else { // No Domain exists. Create a new Domain

			if (domain instanceof de.uni_koblenz.jgralab.schema.BooleanDomain) {

				gDomain = schemaGraph.createBooleanDomain();

			} else if (domain instanceof de.uni_koblenz.jgralab.schema.IntegerDomain) {

				gDomain = schemaGraph.createIntegerDomain();

			} else if (domain instanceof de.uni_koblenz.jgralab.schema.LongDomain) {

				gDomain = schemaGraph.createLongDomain();

			} else if (domain instanceof de.uni_koblenz.jgralab.schema.DoubleDomain) {

				gDomain = schemaGraph.createDoubleDomain();

			} else if (domain instanceof de.uni_koblenz.jgralab.schema.StringDomain) {

				gDomain = schemaGraph.createStringDomain();

			} else if (domain instanceof de.uni_koblenz.jgralab.schema.CollectionDomain) {

				gDomain = createCollectionDomain((de.uni_koblenz.jgralab.schema.CollectionDomain) domain);

			} else if (domain instanceof de.uni_koblenz.jgralab.schema.MapDomain) {

				gDomain = createMapDomain((de.uni_koblenz.jgralab.schema.MapDomain) domain);

			} else if (domain instanceof de.uni_koblenz.jgralab.schema.RecordDomain) {

				gDomain = createRecordDomain((de.uni_koblenz.jgralab.schema.RecordDomain) domain);

			} else if (domain instanceof de.uni_koblenz.jgralab.schema.EnumDomain) {

				gDomain = createEnumDomain((de.uni_koblenz.jgralab.schema.EnumDomain) domain);

			} else {
				throw new RuntimeException("FIXME: Unforseen domain occured! "
						+ domain);
			}

			// General attributes are set.
			assert (domain.getQualifiedName() != null) : "FIXME! QualifiedName is not defined";
			gDomain.setQualifiedName(domain.getQualifiedName());
			Package gPackage = packageMap.get(domain.getPackage());
			ContainsDomain link = schemaGraph.createContainsDomain(gPackage,
					gDomain);
			assert (link != null) : "FIXME! No link ContainsDomain has been created!";

			// Domain is registered in the domain Map.
			domainMap.put(domain, gDomain);
		}

		assert (gDomain != null);

		return gDomain;
	}

	/**
	 * Creates a new MapDomain, which corresponds to the given Domain.
	 * 
	 * @param domain
	 *            Given Domain.
	 * @return New MapDomain.
	 */
	private MapDomain createMapDomain(
			de.uni_koblenz.jgralab.schema.MapDomain domain) {

		assert (checkSchemaAndSchemaGraph());

		// MapDomain is created
		MapDomain gDomain = schemaGraph.createMapDomain();

		// Registers this Domain in the Domain map. This is must be done, before
		// an non-existing Domain is created with the method
		// "queryGDomain(Domain)".
		domainMap.put(domain, gDomain);

		// Links this Domain with its key- and value domains
		schemaGraph.createHasKeyDomain(gDomain, queryGDomain(domain
				.getKeyDomain()));
		schemaGraph.createHasValueDomain(gDomain, queryGDomain(domain
				.getValueDomain()));
		return gDomain;
	}

	/**
	 * Creates a new EnumDomain, which corresponds to the given Domain.
	 * 
	 * @param domain
	 *            Given Domain.
	 * @return New EnumDomain.
	 */
	private EnumDomain createEnumDomain(
			de.uni_koblenz.jgralab.schema.EnumDomain domain) {

		assert (checkSchemaAndSchemaGraph());

		// EnumDomain is created
		EnumDomain gDomain = schemaGraph.createEnumDomain();
		assert (gDomain != null) : "FIXME! No EnumDomain has been created!";

		// The existing ArrayList is copied and set as EnumConstants
		assert (domain.getConsts() != null) : "FIXME! No consts defined.";
		gDomain.setEnumConstants(new ArrayList<String>(domain.getConsts()));
		return gDomain;
	}

	/**
	 * Creates a new ListDomain or a new SetDomain, which corresponds to the
	 * given Domain.
	 * 
	 * @param domain
	 * @return
	 */
	private CollectionDomain createCollectionDomain(
			de.uni_koblenz.jgralab.schema.CollectionDomain domain) {

		assert (checkSchemaAndSchemaGraph());

		// A ListDomain or SetDomain is created.
		CollectionDomain gDomain = (domain instanceof de.uni_koblenz.jgralab.schema.ListDomain) ? schemaGraph
				.createListDomain()
				: schemaGraph.createSetDomain();
		assert (gDomain != null) : "FIXME! No Domain has been created!";

		// Registers this Domain in the Domain map. This is must be done, before
		// an non-existing Domain is created with the method
		// "queryGDomain(Domain)".
		domainMap.put(domain, gDomain);

		// Links a base domain to this CollectionDomain
		HasBaseDomain link = schemaGraph.createHasBaseDomain(gDomain,
				queryGDomain(domain.getBaseDomain()));
		assert (link != null) : "FIXME! No link has been created!";

		return gDomain;
	}

	/**
	 * Creates a new RecordDomain, which corresponds to the given Domain.
	 * 
	 * @param domain
	 *            Given Domain of which a new Domain in the SchemaGraph is
	 *            created.
	 * @return New Domain.
	 */
	private RecordDomain createRecordDomain(
			de.uni_koblenz.jgralab.schema.RecordDomain domain) {

		assert (checkSchemaAndSchemaGraph());

		// RecordDomain is created
		RecordDomain gDomain = schemaGraph.createRecordDomain();
		assert (gDomain != null) : "FIXME! No RecordDomain has been created!";

		// Registers this Domain in the Domain map. This is must be done, before
		// an non-existing Domain is created with the method
		// "queryGDomain(Domain)".
		domainMap.put(domain, gDomain);

		// Loop over all Domain entries
		for (Entry<String, de.uni_koblenz.jgralab.schema.Domain> entry : domain
				.getComponents().entrySet()) {

			// Creates a new hasRecordDomainComponent-edge and sets its name
			// afterwards.
			HasRecordDomainComponent edge = schemaGraph
					.createHasRecordDomainComponent(gDomain, queryGDomain(entry
							.getValue()));
			assert (edge != null) : "FIXME! No link HasRecordDomainComponent has been created!";
			assert (entry.getKey() != null) : "FIXME! No name defined!";
			edge.setName(entry.getKey());
		}

		return gDomain;
	}

	/**
	 * Creates all VertexClass object.
	 */
	private void createVertexClasses() {

		// Loop over all packages
		for (Entry<de.uni_koblenz.jgralab.schema.Package, Package> entry : packageMap
				.entrySet()) {
			// Creates a new VertexClass with the given old and new Package
			createVertexClasses(entry.getKey(), entry.getValue());
		}
	}

	/**
	 * Creates for all VertexClass objects in the given Package new VertexClass
	 * objects and links them to the given new Package.
	 * 
	 * @param xPackage
	 *            Package of which all VertexClass objects are created.
	 * @param gPackage
	 *            Package to which all VertexClass objects are linked.
	 */
	private void createVertexClasses(
			de.uni_koblenz.jgralab.schema.Package xPackage, Package gPackage) {

		assert (checkSchemaAndSchemaGraph());

		VertexClass gVertexClass;
		// Loop over all existing VertexClass objects
		for (de.uni_koblenz.jgralab.schema.VertexClass vertexClass : xPackage
				.getVertexClasses().values()) {

			assert ((vertexClass != null) && (vertexClass.getQualifiedName() != null)) : "FIXME! No QualifiedName for this VertexClass defined!";
			// Skips object, which already exists internal
			if (vertexClass.isInternal()) {
				continue;
			}

			// Creates an VertexClass
			gVertexClass = schemaGraph.createVertexClass();
			assert (gVertexClass != null) : "FIXME! No VertexClass has been created!";

			// Sets all general attributes
			gVertexClass.setIsAbstract(vertexClass.isAbstract());
			gVertexClass.setQualifiedName(vertexClass.getQualifiedName());

			// Registers the new object with the old object as key
			attributedElementClassMap.put(vertexClass, gVertexClass);
			// The same
			vertexClassMap.put(vertexClass, gVertexClass);

			// Links the new VertexClass with the given Package
			ContainsGraphElementClass link = schemaGraph
					.createContainsGraphElementClass(gPackage, gVertexClass);
			assert (link != null) : "FIXME! No link ContainsGraphElementClass has been created!";
		}
	}

	/**
	 * Creates corresponding EdgeClass, AggregationClass and CompositionClass
	 * objects of objects present in all packages.
	 */
	private void createEdgeClasses() {

		// Loop over all packages entries
		for (Entry<de.uni_koblenz.jgralab.schema.Package, Package> entry : packageMap
				.entrySet()) {
			// Creates all EdgeClass objects and links them to the new Package
			createEdgeClasses(entry.getKey(), entry.getValue());
		}
	}

	/**
	 * Creates corresponding EdgeClass, AggregationClass and CompositionClass
	 * objects of objects present in given Package and links them to the new
	 * Package.
	 * 
	 * @param xPackage
	 *            Package, of which all new objects are created.
	 * @param gPackage
	 *            Package, to which all new objects are linked.
	 */
	private void createEdgeClasses(
			de.uni_koblenz.jgralab.schema.Package xPackage, Package gPackage) {

		assert (checkSchemaAndSchemaGraph());

		EdgeClass gEdgeClass;
		// Loop over all old Packages
		for (de.uni_koblenz.jgralab.schema.EdgeClass edgeClass : xPackage
				.getEdgeClasses().values()) {

			assert ((edgeClass != null) && (edgeClass.getQualifiedName() != null)) : "FIXME! NO QualifiedName for this EdgeClass defined!";

			// Skips all internal present objects.
			if (edgeClass.isInternal()) {
				continue;
			}

			// Creates an EdgeClass, an AggregationClass or a CompositionClass.
			gEdgeClass = createEdgeClass(edgeClass);

			// Registers the new and old objects in the appropriate Map
			attributedElementClassMap.put(edgeClass, gEdgeClass);
			edgeClassMap.put(edgeClass, gEdgeClass);
			// Links the new object with its Package
			ContainsGraphElementClass link = schemaGraph
					.createContainsGraphElementClass(gPackage, gEdgeClass);
			assert (link != null) : "FIXME! No link ContainsGraphElementClass has been created!";
		}
	}

	/**
	 * Creates an EdgeClass, AggregationClass or and CompositionClass of an
	 * existing object.
	 * 
	 * @param edgeClass
	 *            EdgeClass, of which a new corresponding object should be
	 *            created.
	 * @return New EdgeClass, AggregationClass or CompositionClass object.
	 */
	private EdgeClass createEdgeClass(
			de.uni_koblenz.jgralab.schema.EdgeClass edgeClass) {

		assert (checkSchemaAndSchemaGraph());
		assert ((edgeClass != null) && (edgeClass.getQualifiedName() != null)) : "FIXME! No QualifiedName for this EdgeClass defined!";

		EdgeClass gEdgeClass = null;

		// Checks whether the given object is an instance of an AggregationClass
		// (CompositionClass) or not
		if (edgeClass instanceof de.uni_koblenz.jgralab.schema.AggregationClass) {
			AggregationClass gAggregationClass;
			// Checking if the given object is an instance of an
			// CompositionClass
			// Checking of an CompositionClass instance instead wouldn't be
			// wise!
			if (edgeClass instanceof de.uni_koblenz.jgralab.schema.CompositionClass) {
				gAggregationClass = schemaGraph.createCompositionClass();
			} else {
				gAggregationClass = schemaGraph.createAggregationClass();
			}
			assert (gAggregationClass != null) : "FIXME! No AggregationClass / CompositionClass has been created!";

			// AggregationClass is the common parent / type and there are no
			// additional attributes to set for a CompositionClass
			de.uni_koblenz.jgralab.schema.AggregationClass aggregationClass = (de.uni_koblenz.jgralab.schema.AggregationClass) edgeClass;

			gAggregationClass.setAggregateFrom(aggregationClass
					.isAggregateFrom());
			gEdgeClass = gAggregationClass;

		} else {

			// An EdgeClass is created.
			gEdgeClass = schemaGraph.createEdgeClass();

		}
		assert (gEdgeClass != null) : "FIXME! No EdgeClass has been created!";

		// Sets all general attributes of an EdgeClass
		gEdgeClass.setIsAbstract(edgeClass.isAbstract());
		gEdgeClass.setQualifiedName(edgeClass.getQualifiedName());

		return gEdgeClass;
	}

	/**
	 * Links all direct super classes with its subclasses.
	 */
	private void createSpecializations() {

		assert (checkSchemaAndSchemaGraph());

		// Loop over all VertexClass objects
		for (Entry<de.uni_koblenz.jgralab.schema.VertexClass, VertexClass> entry : vertexClassMap
				.entrySet()) {
			// Loop over all superclass's of the current entry
			for (de.uni_koblenz.jgralab.schema.AttributedElementClass superClass : entry
					.getKey().getDirectSuperClasses()) {

				// Skips predefined classes
				if (superClass.isInternal()) {
					continue;
				}

				// Links the superclass with its subclass.
				SpecializesVertexClass link = schemaGraph
						.createSpecializesVertexClass(entry.getValue(),
								vertexClassMap.get(superClass));
				assert (link != null) : "FIXME! No link SpecializesVertexClass has been created!";
			}
		}

		// Loop over all EdgeClass objects
		for (Entry<de.uni_koblenz.jgralab.schema.EdgeClass, EdgeClass> entry : edgeClassMap
				.entrySet()) {
			// Loop over all superclass's of the current entry
			for (de.uni_koblenz.jgralab.schema.AttributedElementClass superClass : entry
					.getKey().getDirectSuperClasses()) {

				// Skips predefined classes
				if (superClass.isInternal()) {
					continue;
				}

				// Links the superclass with its subclass
				SpecializesEdgeClass link = schemaGraph
						.createSpecializesEdgeClass(entry.getValue(),
								edgeClassMap.get(superClass));
				assert (link != null) : "FIXME! No link SpecializesEdgeClass has been created!";
			}
		}
	}

	/**
	 * Creates all Attribute objects and links them with an
	 * AttributesElementClass.
	 */
	private void createAttributes() {
		// Loop over all AttributeElementClass entries.
		for (Entry<de.uni_koblenz.jgralab.schema.AttributedElementClass, AttributedElementClass> entry : attributedElementClassMap
				.entrySet()) {
			// Creates all Attribute objects for this entry
			createAttributes(entry.getKey(), entry.getValue());
		}
	}

	/**
	 * Creates all corresponding Attribute of all Attribute object of the given
	 * AttributedElementClass object.
	 * 
	 * @param element
	 *            AttributedElementClass, of which all attributes are created.
	 * @param gElement
	 *            AttributeElementClass, to which all attributes are linked.
	 */
	private void createAttributes(
			de.uni_koblenz.jgralab.schema.AttributedElementClass element,
			AttributedElementClass gElement) {

		assert (checkSchemaAndSchemaGraph());

		Attribute gAttribute;
		Domain gDomain;

		// Loop over all Attribute objects in the given element.
		for (de.uni_koblenz.jgralab.Attribute attribute : element
				.getOwnAttributeList()) {

			assert ((attribute != null) && (attribute.getName() != null)) : "FIXME! No name for this Attribute is defined!";

			// An new Attribute is created and its name is set.
			gAttribute = schemaGraph.createAttribute();
			assert (gAttribute != null) : "FIXME! No Attribute has been created!";
			gAttribute.setName(attribute.getName());

			// Corresponding new Domain for the new Attribute is queried.
			assert (attribute.getDomain() != null) : "FIXME! No Domain has been defined!";
			gDomain = domainMap.get(attribute.getDomain());
			assert (gDomain != null) : "FIXME! Given Schema malformed, "
					+ "because the requested Domain is not registered in its Package";

			// Attribute is linked with its AttributedElementClass object and
			// the Domain is linked with its Attribute.
			HasAttribute link1 = schemaGraph.createHasAttribute(gElement,
					gAttribute);
			HasDomain link2 = schemaGraph.createHasDomain(gAttribute, gDomain);
			assert ((link1 != null) && (link2 != null)) : "FIXME! No link HasAttribute or HasDomain have been created!";
		}
	}

	/**
	 * All Constraints are created and linked with their corresponding
	 * AttributedElementClass.
	 */
	private void createConstraints() {
		// Loop over all AttributeElementClass entries.
		for (Entry<de.uni_koblenz.jgralab.schema.AttributedElementClass, AttributedElementClass> entry : attributedElementClassMap
				.entrySet()) {
			// Creates all Constraint objects for the current
			// AttributeElementClass entry
			createConstraints(entry.getKey(), entry.getValue());
		}
	}

	/**
	 * Creates all Constraints corresponding to the Constraints contained in the
	 * given AttributedElementClass and links the new objects with the new
	 * AttributedElementClass object.
	 * 
	 * @param element
	 *            AttributedElementClass, of which all Constraints are created.
	 * @param gElement
	 *            AttributedElementClass, to which all Constraints are linked.
	 */
	private void createConstraints(
			de.uni_koblenz.jgralab.schema.AttributedElementClass element,
			AttributedElementClass gElement) {

		assert (checkSchemaAndSchemaGraph());
		assert (element != null) : "FIXME! AttributedElement is null!";
		assert (gElement != null) : "FIXME! AttributedElement is null!";
		assert (element.getConstraints() != null) : "FIXME! AttributedElement has no Constraints defined!";

		Constraint gConstraint;

		// Loop over all Constraints contained by the given old
		// AttributedElementClass object.
		for (de.uni_koblenz.jgralab.schema.Constraint constraint : element
				.getConstraints()) {

			assert ((constraint != null) && (constraint.getMessage() != null) && (constraint
					.getPredicate() != null)) : "FIXME! Constraint isn't wellformed.";
			// A new Constraint is created.
			gConstraint = schemaGraph.createConstraint();

			// Sets all general attributes.
			gConstraint.setMessage(constraint.getMessage());
			gConstraint.setPredicateQuery(constraint.getPredicate());
			gConstraint.setOffendingElementsQuery(constraint
					.getOffendingElementsQuery());

			// Links the new Constraint with its AttributedElementClass.
			HasConstraint link = schemaGraph.createHasConstraint(gElement,
					gConstraint);
			assert (link != null) : "FIXME! No link HasConstraint has been created!";
		}
	}

	/**
	 * Creates all From and To edges of all EdgeClasses
	 */
	private void createEdges() {

		// Loop over all EdgeClass objects
		for (Entry<de.uni_koblenz.jgralab.schema.EdgeClass, EdgeClass> entry : edgeClassMap
				.entrySet()) {
			// Creates From and To edge
			createEdges(entry.getKey(), entry.getValue());
		}
	}

	/**
	 * Creates the From and To edge of an given EdgeClass.
	 * 
	 * @param edgeClass
	 *            EdgeClass, of which all edges are created.
	 * @param gEdgeClass
	 *            EdgeClass, to which all edges are linked.
	 */
	private void createEdges(de.uni_koblenz.jgralab.schema.EdgeClass edgeClass,
			EdgeClass gEdgeClass) {

		assert (checkSchemaAndSchemaGraph());
		assert (edgeClass != null) : "FIXME! EdgeClass is null!";

		Set<String> redefinedRoles;

		// First the To edge
		// Queries the VertexClass to which the To edge points.
		VertexClass vertexClass = vertexClassMap.get(edgeClass.getTo());
		// Creates the To edge
		To to = schemaGraph.createTo(gEdgeClass, vertexClass);
		assert (to != null) : "FIXME! No To edge has been created.";

		// Sets all general attributes
		to.setMin(edgeClass.getToMin());
		to.setMax(edgeClass.getToMax());
		to.setRoleName(edgeClass.getToRolename());

		redefinedRoles = edgeClass.getRedefinedToRoles();
		if ((redefinedRoles != null) && (redefinedRoles.size() != 0)) {
			// Clones the existing HashSet
			to.setRedefinedRoles(new HashSet<String>(redefinedRoles));
		}

		// First the To edge
		// Queries the VertexClass to which the To edge points.
		vertexClass = vertexClassMap.get(edgeClass.getFrom());
		// Creates the To edge
		From from = schemaGraph.createFrom(gEdgeClass, vertexClass);
		assert (from != null) : "FIXME! No From edge has been created.";
		// Sets all general attributes
		from.setMin(edgeClass.getFromMin());
		from.setMax(edgeClass.getFromMax());
		from.setRoleName(edgeClass.getFromRolename());

		redefinedRoles = edgeClass.getRedefinedFromRoles();
		if ((redefinedRoles != null) && (redefinedRoles.size() != 0)) {
			// Clones the existing HashSet.
			from.setRedefinedRoles(new HashSet<String>(redefinedRoles));
		}
	}

	/**
	 * Query a new Domain or creates a new Domain in case of a failed query.
	 * 
	 * @param domain
	 *            Domain, to which a corresponding Domain should be found.
	 * @return Found or created Domain.
	 */
	private Domain queryGDomain(de.uni_koblenz.jgralab.schema.Domain domain) {

		// Query
		assert (domainMap != null) : "Relation map from original Domain to SchemaGraph Domain hasn't been created.";
		Domain gDomain = domainMap.get(domain);

		if (gDomain == null) {
			// In case of a failed query a new Domain is created!
			gDomain = createDomain(domain);
		}

		assert (gDomain != null) : "FIXME! Something is wrong!";

		return gDomain;
	}
}
