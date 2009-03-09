package de.uni_koblenz.jgralab.utilities.tg2schemagraph;

/*
 * JGraLab - The Java graph laboratory
 * (c) 2006-2007 Institute for Software Technology
 *               University of Koblenz-Landau, Germany
 *
 *               ist@uni-koblenz.de
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

import gnu.getopt.Getopt;
import gnu.getopt.LongOpt;

import java.io.DataOutputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import de.uni_koblenz.jgralab.Attribute;
import de.uni_koblenz.jgralab.Graph;
import de.uni_koblenz.jgralab.GraphIO;
import de.uni_koblenz.jgralab.GraphIOException;
import de.uni_koblenz.jgralab.ProgressFunction;
import de.uni_koblenz.jgralab.WorkInProgress;
import de.uni_koblenz.jgralab.grumlschema.GrumlSchema;
import de.uni_koblenz.jgralab.grumlschema.SchemaGraph;
import de.uni_koblenz.jgralab.grumlschema.domains.HasRecordDomainComponent;
import de.uni_koblenz.jgralab.grumlschema.structure.From;
import de.uni_koblenz.jgralab.grumlschema.structure.To;
import de.uni_koblenz.jgralab.schema.AggregationClass;
import de.uni_koblenz.jgralab.schema.AttributedElementClass;
import de.uni_koblenz.jgralab.schema.BooleanDomain;
import de.uni_koblenz.jgralab.schema.CollectionDomain;
import de.uni_koblenz.jgralab.schema.CompositeDomain;
import de.uni_koblenz.jgralab.schema.CompositionClass;
import de.uni_koblenz.jgralab.schema.Domain;
import de.uni_koblenz.jgralab.schema.DoubleDomain;
import de.uni_koblenz.jgralab.schema.EdgeClass;
import de.uni_koblenz.jgralab.schema.EnumDomain;
import de.uni_koblenz.jgralab.schema.GraphClass;
import de.uni_koblenz.jgralab.schema.IntDomain;
import de.uni_koblenz.jgralab.schema.ListDomain;
import de.uni_koblenz.jgralab.schema.LongDomain;
import de.uni_koblenz.jgralab.schema.Package;
import de.uni_koblenz.jgralab.schema.QualifiedName;
import de.uni_koblenz.jgralab.schema.RecordDomain;
import de.uni_koblenz.jgralab.schema.Schema;
import de.uni_koblenz.jgralab.schema.SetDomain;
import de.uni_koblenz.jgralab.schema.StringDomain;
import de.uni_koblenz.jgralab.schema.VertexClass;

/**
 * This class represents any <code>Schema</code> object as an <code>Graph</code>
 * object.
 * 
 * @author ist@uni-koblenz.de
 */
@WorkInProgress(description = "Problems with forward links to domains, constraints missing", responsibleDevelopers = "riediger")
public class Tg2SchemaGraph {

	/**
	 * writes a schema's to a file. the schema .tg file and the outputfile get
	 * defined by the command line options
	 */
	public static void main(String[] args) {
		Tg2SchemaGraph tg2sg = new Tg2SchemaGraph();
		tg2sg.getOptions(args);
		tg2sg.saveSchemaGraphToFile();
	}

	private String outputFilename;
	// the schema this class was instantiated with.
	private Schema schema;

	// this object will be returned, when getSchemaGraph() is called.
	private SchemaGraph schemagraph;

	// helpful to encapsulate the CompositeDomain hierarchy from the
	// rest of the graph
	private Map<de.uni_koblenz.jgralab.schema.Domain, de.uni_koblenz.jgralab.grumlschema.domains.Domain> jGraLab2SchemagraphDomainMap;
	private Map<de.uni_koblenz.jgralab.schema.Package, de.uni_koblenz.jgralab.grumlschema.structure.Package> jGraLab2SchemagraphPackageMap;
	private Map<de.uni_koblenz.jgralab.schema.VertexClass, de.uni_koblenz.jgralab.grumlschema.structure.VertexClass> jGraLab2SchemagraphVertexClassMap;
	private Map<de.uni_koblenz.jgralab.schema.EdgeClass, de.uni_koblenz.jgralab.grumlschema.structure.EdgeClass> jGraLab2SchemagraphEdgeClassMap;

	/**
	 * This class must be instantiated with a schema. You cannot change the
	 * <code>Schema</code> afterwards hence for every schemagraph you want to
	 * create a new instance of this class is needed.
	 * 
	 * @param schema
	 *            Any desired <code>Schema</code> object.
	 */
	public Tg2SchemaGraph(de.uni_koblenz.jgralab.schema.Schema schema) {
		this.schema = schema;
	}

	/**
	 * The unparameterized constructor is only used in the command line mode.
	 * The method <code>private void setSchema()</code> ensures the
	 * initialization of <code>private Schema schema</code>
	 */
	private Tg2SchemaGraph() {
	}

	/**
	 * creates an instance graph of the grUML language's meta schema. Its result
	 * is a <code>Graph</code>, that represents any desired <code>Schema</code>.
	 * 
	 * @return a <code>Graph</code> object that represents a <code>Schema</code>
	 *         .
	 */
	public Graph getSchemaGraph() {
		if (schema == null) {
			return null;
		}
		if (schemagraph == null) {

			// create the schemagraph
			schemagraph = GrumlSchema.instance().createSchemaGraph(
					schema.getQualifiedName());

			// create a vertex for the schema
			de.uni_koblenz.jgralab.grumlschema.structure.Schema schemaVertex = schemagraph
					.createSchema();

			schemaVertex.setName(schema.getSimpleName());
			schemaVertex.setPackagePrefix(schema.getPackageName());

			// create a HashMap that maps each schema domain to the
			// corresponding schemagraph domainVertex
			jGraLab2SchemagraphDomainMap = new HashMap<de.uni_koblenz.jgralab.schema.Domain, de.uni_koblenz.jgralab.grumlschema.domains.Domain>();
			jGraLab2SchemagraphPackageMap = new HashMap<de.uni_koblenz.jgralab.schema.Package, de.uni_koblenz.jgralab.grumlschema.structure.Package>();
			jGraLab2SchemagraphVertexClassMap = new HashMap<de.uni_koblenz.jgralab.schema.VertexClass, de.uni_koblenz.jgralab.grumlschema.structure.VertexClass>();
			jGraLab2SchemagraphEdgeClassMap = new HashMap<de.uni_koblenz.jgralab.schema.EdgeClass, de.uni_koblenz.jgralab.grumlschema.structure.EdgeClass>();

			createJGraLabDomainToSchemagraphDomainMap();

			// create the schemagraph vertex for the graphclass and the
			// schemagraph edge definesGraphClass
			de.uni_koblenz.jgralab.grumlschema.structure.GraphClass graphClassVertex = schemagraph
					.createGraphClass();
			schemagraph.createDefinesGraphClass(schemaVertex, graphClassVertex);

			GraphClass gc = schema.getGraphClassesInTopologicalOrder().get(1);
			graphClassVertex.setQualifiedName(gc.getQualifiedName());

			// create vertex for the default package and set its attributes
			// create incident edge containsDefaultPackage
			de.uni_koblenz.jgralab.schema.Package defaultPackage = schema
					.getDefaultPackage();
			de.uni_koblenz.jgralab.grumlschema.structure.Package defaultPackageVertex = schemagraph
					.createPackage();
			defaultPackageVertex.setQualifiedName(defaultPackage
					.getQualifiedName());
			schemagraph.createContainsDefaultPackage(schemaVertex,
					defaultPackageVertex);

			for (Attribute attr : gc.getOwnAttributeList()) {
				createSchemagraphAttribute(attr, graphClassVertex);
			}

			//
			createSchemagraphPackageAndContents(defaultPackage,
					defaultPackageVertex);

			//
			createSpecializesEdgesForSchemagraphVertexClasses();
			createSpecializesEdgesForSchemagraphEdgeClasses();

			//
			for (Entry<de.uni_koblenz.jgralab.schema.Domain, de.uni_koblenz.jgralab.grumlschema.domains.Domain> entry : jGraLab2SchemagraphDomainMap
					.entrySet()) {
				schemagraph.createContainsDomain(jGraLab2SchemagraphPackageMap
						.get(entry.getKey().getPackage()), entry.getValue());
			}
		}
		return schemagraph;
	}

	/**
	 * Sets up a <code>schemagraph</code> <code>Package</code> vertex. For each
	 * subpackge of <code>jGraLabSuperPackage</code> this method gets called
	 * recursively.
	 * 
	 * @param jGraLabSuperPackage
	 *            a JGraLab package
	 * @param schemagraphSuperPackage
	 *            a vertex of the representing package jGraLabSuperPackage
	 */
	private void createSchemagraphPackageAndContents(
			de.uni_koblenz.jgralab.schema.Package jGraLabSuperPackage,
			de.uni_koblenz.jgralab.grumlschema.structure.Package schemagraphSuperPackage) {
		createSchemagraphVertexClassesForPackage(jGraLabSuperPackage,
				schemagraphSuperPackage);
		createSchemagraphEdgeClassesForPackage(jGraLabSuperPackage,
				schemagraphSuperPackage);
		jGraLab2SchemagraphPackageMap.put(jGraLabSuperPackage,
				schemagraphSuperPackage);

		Map<String, de.uni_koblenz.jgralab.schema.Package> jGraLabSubPackages = jGraLabSuperPackage
				.getSubPackages();
		if (jGraLabSubPackages != null) {
			for (de.uni_koblenz.jgralab.schema.Package jGraLabSubPackage : jGraLabSubPackages
					.values()) {
				de.uni_koblenz.jgralab.grumlschema.structure.Package schemagraphSubPackage = schemagraph
						.createPackage();
				schemagraphSubPackage.setQualifiedName(jGraLabSubPackage
						.getQualifiedName());
				schemagraph.createContainsSubPackage(schemagraphSuperPackage,
						schemagraphSubPackage);
				createSchemagraphPackageAndContents(jGraLabSubPackage,
						schemagraphSubPackage);
			}
		}
	}

	/**
	 * creates all <code>schemagraph</code> <code>VertexClass</code> vertices
	 * for a given package and the required incident edges of it. For each
	 * attribute of the <code>VertexClass</code>es
	 * <code>createSchemagraphAttribute</code> gets called.
	 * 
	 * @param schemagraphPackage
	 *            a vertex representing package <code>jGraLabPackage</code>
	 * @param jGraLabPackage
	 *            a JGraLab package
	 */
	private void createSchemagraphVertexClassesForPackage(
			Package jGraLabPackage,
			de.uni_koblenz.jgralab.grumlschema.structure.Package schemagraphPackage) {

		// for each vertexClass of package pakkage...
		for (VertexClass jGraLabVertexClass : jGraLabPackage.getVertexClasses()
				.values()) {
			if (!jGraLabVertexClass.isInternal()
					&& jGraLabVertexClass.getPackage().equals(jGraLabPackage)) {
				// ...create a vertex
				de.uni_koblenz.jgralab.grumlschema.structure.VertexClass schemagraphVertexClass = schemagraph
						.createVertexClass();
				schemagraph.createContainsGraphElementClass(schemagraphPackage,
						schemagraphVertexClass);

				schemagraphVertexClass.setQualifiedName(jGraLabVertexClass
						.getQualifiedName());
				schemagraphVertexClass.setIsAbstract(jGraLabVertexClass
						.isAbstract());

				jGraLab2SchemagraphVertexClassMap.put(jGraLabVertexClass,
						schemagraphVertexClass);

				// ..each attribute gets created.
				for (Attribute attr : jGraLabVertexClass.getOwnAttributeList()) {
					createSchemagraphAttribute(attr, schemagraphVertexClass);
				}
			}
		}
	}

	/**
	 * creates all <code>schemagraph</code> <code>EdgeClass</code> vertices for
	 * a given package and the required incident edges of it(like
	 * <code>To</code>, <code>From</code>,
	 * <code>ContainsGraphElementClass</code>). For each attribute of the
	 * <code>EdgeClass</code>es <code>createSchemagraphAttribute</code> gets
	 * called.
	 * 
	 * @param schemagraphPackage
	 * @param jGraLabPackage
	 */
	private void createSchemagraphEdgeClassesForPackage(
			Package jGraLabPackage,
			de.uni_koblenz.jgralab.grumlschema.structure.Package schemagraphPackage) {
		// for each edge class..
		for (EdgeClass jGraLabEdgeClass : schema
				.getEdgeClassesInTopologicalOrder()) {
			if (!jGraLabEdgeClass.isInternal()
					&& jGraLabEdgeClass.getPackage().equals(jGraLabPackage)) {
				de.uni_koblenz.jgralab.grumlschema.structure.EdgeClass schemagraphEdgeClass = null;

				// ..either an EdgeClassM2 or EdgeClassM2 subclass objects gets
				// created.

				if (jGraLabEdgeClass instanceof CompositionClass) {
					schemagraphEdgeClass = schemagraph.createCompositionClass();
				} else if (jGraLabEdgeClass instanceof AggregationClass) {
					schemagraphEdgeClass = schemagraph.createAggregationClass();
				} else {
					schemagraphEdgeClass = schemagraph.createEdgeClass();
				}
				schemagraph.createContainsGraphElementClass(schemagraphPackage,
						schemagraphEdgeClass);

				schemagraphEdgeClass.setQualifiedName(jGraLabEdgeClass
						.getQualifiedName());
				schemagraphEdgeClass.setIsAbstract(jGraLabEdgeClass
						.isAbstract());

				for (de.uni_koblenz.jgralab.grumlschema.structure.VertexClass vcFrom : schemagraph
						.getVertexClassVertices()) {
					if (vcFrom.getQualifiedName().equals(
							jGraLabEdgeClass.getFrom().getQualifiedName())) {
						// ..the From aggregation gets created.
						From fromM2 = schemagraph.createFrom(
								schemagraphEdgeClass, vcFrom);
						fromM2.setRoleName(jGraLabEdgeClass.getFromRolename());
						fromM2.setMin(jGraLabEdgeClass.getFromMin());
						fromM2.setMax(jGraLabEdgeClass.getFromMax());
						break;
					}
				}

				for (de.uni_koblenz.jgralab.grumlschema.structure.VertexClass vcTo : schemagraph
						.getVertexClassVertices()) {
					if (vcTo.getQualifiedName().equals(
							jGraLabEdgeClass.getTo().getQualifiedName())) {
						// ..the To aggregation gets created.
						To toM2 = schemagraph.createTo(schemagraphEdgeClass,
								vcTo);
						toM2.setRoleName(jGraLabEdgeClass.getToRolename());
						toM2.setMin(jGraLabEdgeClass.getToMin());
						toM2.setMax(jGraLabEdgeClass.getToMax());
						break;
					}
				}
				jGraLab2SchemagraphEdgeClassMap.put(jGraLabEdgeClass,
						schemagraphEdgeClass);
				// ..each attribute gets created.
				for (Attribute attr : jGraLabEdgeClass.getOwnAttributeList()) {
					createSchemagraphAttribute(attr, schemagraphEdgeClass);
				}
			}
		}
	}

	/**
	 * creates all <code>SpecializesEdgeClass</code> edges in
	 * <code>schemagraph</code>
	 */
	private void createSpecializesEdgesForSchemagraphVertexClasses() {
		for (EdgeClass schemagraphSuperEdgeClass : schema
				.getEdgeClassesInTopologicalOrder()) {
			if (!schemagraphSuperEdgeClass.isInternal()) {
				for (AttributedElementClass schemagraphSubEdgeClass : schemagraphSuperEdgeClass
						.getDirectSubClasses()) {
					schemagraph.createSpecializesEdgeClass(
							jGraLab2SchemagraphEdgeClassMap
									.get(schemagraphSuperEdgeClass),
							jGraLab2SchemagraphEdgeClassMap
									.get(schemagraphSubEdgeClass));
				}
			}
		}
	}

	/**
	 * creates all <code>SpecializesVertexClass</code> edges in
	 * <code>schemagraph</code>
	 */
	private void createSpecializesEdgesForSchemagraphEdgeClasses() {
		for (VertexClass schemagraphSuperVertexClass : schema
				.getVertexClassesInTopologicalOrder()) {
			if (!schemagraphSuperVertexClass.isInternal()) {
				for (AttributedElementClass schemagraphSubVertexClass : schemagraphSuperVertexClass
						.getDirectSubClasses()) {
					schemagraph.createSpecializesVertexClass(
							jGraLab2SchemagraphVertexClassMap
									.get(schemagraphSuperVertexClass),
							jGraLab2SchemagraphVertexClassMap
									.get(schemagraphSubVertexClass));
				}
			}
		}
	}

	/**
	 * creates a <code>schemagraph</code> <code>Attribute</code> vertex and the
	 * <code>HasAttribute</code> and <code>HasDomain</code> edges.
	 * 
	 * @param jGraLabAttribute
	 *            a JGraLab attribute
	 * @param schemagraphAttributedElementClass
	 *            a <code>schemagraph</code> vertex representing
	 *            <code>jGraLabAttribute</code>
	 */
	private void createSchemagraphAttribute(
			Attribute jGraLabAttribute,
			de.uni_koblenz.jgralab.grumlschema.structure.AttributedElementClass schemagraphAttributedElementClass) {
		de.uni_koblenz.jgralab.grumlschema.structure.Attribute schemagraphAttribute = schemagraph
				.createAttribute();
		schemagraphAttribute.setName(jGraLabAttribute.getName());

		// the HasAttribute link from AttributedElementClass to Attribute
		// gets created.
		schemagraph.createHasAttribute(schemagraphAttributedElementClass,
				schemagraphAttribute);

		schemagraph.createHasDomain(schemagraphAttribute,
				jGraLab2SchemagraphDomainMap.get(jGraLabAttribute.getDomain()));
	}

	/**
	 * values the
	 * <code>Map<de.uni_koblenz.jgralab.schema.Domain, de.uni_koblenz.jgralab.grumlschema.Domain> domainMap</code>
	 * . i.e. <code>domainMap.get(de.uni_koblenz.jgralab.schema.Domain d)</code>
	 * return the corresponding
	 * <code>de.uni_koblenz.jgralab.grumlschema.Domain</code> object.
	 * 
	 * At first only the <code>BasicDomain</code>s get mapped. The
	 * <code>CompositeDomain</code>s get mapped in the order of the
	 * "domain-depth" of their base domains or RecordDomainComponents. First,
	 * composites of basic types get mapped. Then composites of composites of
	 * basic types...and so on. The leafs of the compositum get created lastly.
	 */
	private void createJGraLabDomainToSchemagraphDomainMap() {
		Map<QualifiedName, Domain> domains = schema.getDomains();
		while (jGraLab2SchemagraphDomainMap.size() != domains.size()) {
			for (Domain d : domains.values()) {
				de.uni_koblenz.jgralab.grumlschema.domains.Domain schemaGraphDomain = null;
				if (jGraLab2SchemagraphDomainMap.get(d) != null) {
					continue;
				}

				if (d instanceof BooleanDomain) {
					schemaGraphDomain = schemagraph.createBooleanDomain();
				} else if (d instanceof DoubleDomain) {
					schemaGraphDomain = schemagraph.createDoubleDomain();
				} else if (d instanceof EnumDomain) {
					schemaGraphDomain = schemagraph.createEnumDomain();
					((de.uni_koblenz.jgralab.grumlschema.domains.EnumDomain) schemaGraphDomain)
							.setEnumConstants(((EnumDomain) d).getConsts());
				} else if (d instanceof LongDomain) {
					schemaGraphDomain = schemagraph.createLongDomain();
				} else if (d instanceof IntDomain) {
					schemaGraphDomain = schemagraph.createIntDomain();
				} else if (d instanceof StringDomain) {
					schemaGraphDomain = schemagraph.createStringDomain();

				} else if (d instanceof CompositeDomain) {
					schemaGraphDomain = createSchemagraphCompositeDomain(d,
							schemaGraphDomain);
				}
				if (schemaGraphDomain != null) {
					schemaGraphDomain.setQualifiedName(d.getQualifiedName());
					jGraLab2SchemagraphDomainMap.put(d, schemaGraphDomain);
				}
			}
		}
	}

	/**
	 * creates a <code>schemagraph</code> <code>CompositeDomain</code> vertex if
	 * and only if its base domain (<code>ListDomain</code> and
	 * <code>SetDomain</code>) or all of its <code>RecordDomainComponent</code>
	 * have been created. If creation proceeds successfully the
	 * <code>jGraLab2SchemagraphDomainMap</code> map gets updated directly.
	 */
	private de.uni_koblenz.jgralab.grumlschema.domains.Domain createSchemagraphCompositeDomain(
			Domain jGraLabDomain,
			de.uni_koblenz.jgralab.grumlschema.domains.Domain schemagraphDomain) {
		if (jGraLabDomain instanceof CollectionDomain) {
			if (jGraLab2SchemagraphDomainMap
					.get(((CollectionDomain) jGraLabDomain).getBaseDomain()) != null) {
				if (jGraLabDomain instanceof ListDomain) {
					schemagraphDomain = schemagraph.createListDomain();
				} else if (jGraLabDomain instanceof SetDomain) {
					schemagraphDomain = schemagraph.createSetDomain();
				}
				if (schemagraphDomain != null) {
					schemagraph
							.createHasBaseDomain(
									(de.uni_koblenz.jgralab.grumlschema.domains.CollectionDomain) schemagraphDomain,
									jGraLab2SchemagraphDomainMap
											.get(((CollectionDomain) jGraLabDomain)
													.getBaseDomain()));
					return schemagraphDomain;
				}
			}
		} else if (jGraLabDomain instanceof RecordDomain) {
			boolean allBaseDomainsMapped = true;
			for (Domain dom : ((RecordDomain) jGraLabDomain).getComponents()
					.values()) {
				if (jGraLab2SchemagraphDomainMap.get(dom) == null) {
					allBaseDomainsMapped = false;
					break;
				}
			}
			if (allBaseDomainsMapped) {
				schemagraphDomain = schemagraph.createRecordDomain();
				Map<String, Domain> recordMap = ((RecordDomain) jGraLabDomain)
						.getComponents();
				for (String key : recordMap.keySet()) {
					HasRecordDomainComponent hrc = schemagraph
							.createHasRecordDomainComponent(
									((de.uni_koblenz.jgralab.grumlschema.domains.RecordDomain) schemagraphDomain),
									jGraLab2SchemagraphDomainMap.get(recordMap
											.get(key)));
					hrc.setName(key);
				}
				return schemagraphDomain;
			}
		}
		return null;
	}

	/**
	 * This methods writes the <code>schemagraph</code> to a file (see
	 * GraphIO.java)
	 */
	public void saveSchemaGraphToFile(String filename, ProgressFunction pf) {
		try {
			GraphIO.saveGraphToFile(filename, getSchemaGraph(), pf);
		} catch (GraphIOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * writes the <code>schemagraph</code> to an DataOutputStream (see
	 * GraphIO.java)
	 */
	public void saveSchemaGraphToStream(DataOutputStream stream,
			ProgressFunction pf) {
		try {
			GraphIO.saveGraphToStream(stream, getSchemaGraph(), pf);

		} catch (GraphIOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * sets the local variable Schema schema. Only used in command line mode.
	 */
	private void setSchema(String filename) throws GraphIOException {
		schema = GraphIO.loadSchemaFromFile(filename);
	}

	/**
	 * this method is used, if Tg2SchemaGraph was called from the command line
	 */
	private void saveSchemaGraphToFile() {
		try {
			GraphIO.saveGraphToFile(outputFilename, getSchemaGraph(), null);
		} catch (GraphIOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * This methods processes the command-line arguments. uses gnu.getopt.GetOpt
	 * and gnu.getopt.LongOpt
	 */
	private void getOptions(String[] args) {
		LongOpt[] longOptions = new LongOpt[3];

		int c = 0;
		longOptions[c++] = new LongOpt("schema", LongOpt.REQUIRED_ARGUMENT,
				null, 's');
		longOptions[c++] = new LongOpt("output", LongOpt.REQUIRED_ARGUMENT,
				null, 'o');
		longOptions[c++] = new LongOpt("help", LongOpt.NO_ARGUMENT, null, 'h');

		Getopt g = new Getopt("Tg2SchemaGraph", args, "s:o:h", longOptions);
		c = g.getopt();
		String schemaName = null;
		while (c >= 0) {
			switch (c) {
			case 's':
				try {
					schemaName = g.getOptarg();
					setSchema(schemaName);
				} catch (GraphIOException e) {
					System.err.println("Coundn't load schema in file '"
							+ schemaName + "': " + e.getMessage());
					if (e.getCause() != null) {
						e.getCause().printStackTrace();
					}
					System.exit(1);
				}
				break;
			case 'o':
				outputFilename = g.getOptarg();
				if (outputFilename == null) {
					usage(1);
				}
				break;
			case '?':
			case 'h':
				usage(0);
				break;
			default:
				throw new RuntimeException("FixMe (c='" + (char) c + "')");
			}
			c = g.getopt();
		}
		if (g.getOptind() < args.length) {
			System.err.println("Extra arguments!");
			usage(1);
		}
		if (g.getOptarg() == null) {
			// ??????????
			// System.out.println("Missing option");
			// usage(1);
		}
		if (outputFilename == null) {
			outputFilename = schema.getQualifiedName() + "_schemagraph.tg";
		}
	}

	/**
	 * A help message. Printed, when invalid command-line options or command
	 * line option -h was typed
	 */
	private void usage(int exitCode) {
		System.err.println("Usage: Tg2SchemaGraph -s schemaFileName [options]");

		System.err.println("Options are:");
		System.err
				.println("-s schemaFileName  (--schema)    the schema to be converted");
		System.err
				.println("-o outputFileName  (--output)    the output file name. If it is empty");
		System.err
				.println("                                 schema.getName()+\"schemagraph\" is used.");
		System.err
				.println("-h                 (--help)      prints usage information");
		System.exit(exitCode);
	}

}