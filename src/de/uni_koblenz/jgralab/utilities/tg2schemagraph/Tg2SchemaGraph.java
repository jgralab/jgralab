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

package de.uni_koblenz.jgralab.utilities.tg2schemagraph;

import gnu.getopt.Getopt;
import gnu.getopt.LongOpt;

import java.io.DataOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import de.uni_koblenz.jgralab.Attribute;
import de.uni_koblenz.jgralab.Edge;
import de.uni_koblenz.jgralab.Graph;
import de.uni_koblenz.jgralab.GraphIO;
import de.uni_koblenz.jgralab.GraphIOException;
import de.uni_koblenz.jgralab.ProgressFunction;
import de.uni_koblenz.jgralab.schema.AggregationClass;
import de.uni_koblenz.jgralab.schema.AttributedElementClass;
import de.uni_koblenz.jgralab.schema.BooleanDomain;
import de.uni_koblenz.jgralab.schema.CompositeDomain;
import de.uni_koblenz.jgralab.schema.CompositionClass;
import de.uni_koblenz.jgralab.schema.Domain;
import de.uni_koblenz.jgralab.schema.DoubleDomain;
import de.uni_koblenz.jgralab.schema.EdgeClass;
import de.uni_koblenz.jgralab.schema.EnumDomain;
import de.uni_koblenz.jgralab.schema.IntDomain;
import de.uni_koblenz.jgralab.schema.ListDomain;
import de.uni_koblenz.jgralab.schema.LongDomain;
import de.uni_koblenz.jgralab.schema.ObjectDomain;
import de.uni_koblenz.jgralab.schema.Package;
import de.uni_koblenz.jgralab.schema.QualifiedName;
import de.uni_koblenz.jgralab.schema.RecordDomain;
import de.uni_koblenz.jgralab.schema.Schema;
import de.uni_koblenz.jgralab.schema.SetDomain;
import de.uni_koblenz.jgralab.schema.StringDomain;
import de.uni_koblenz.jgralab.schema.VertexClass;
import de.uni_koblenz.jgralab.utilities.tg2schemagraph.grumlschema.ContainsGraphElementClass;
import de.uni_koblenz.jgralab.utilities.tg2schemagraph.grumlschema.From;
import de.uni_koblenz.jgralab.utilities.tg2schemagraph.grumlschema.GrUMLSchema;
import de.uni_koblenz.jgralab.utilities.tg2schemagraph.grumlschema.GrUMLSchemaGraph;
import de.uni_koblenz.jgralab.utilities.tg2schemagraph.grumlschema.HasRecordDomainComponent;
import de.uni_koblenz.jgralab.utilities.tg2schemagraph.grumlschema.To;

/**
 * This class represents any <code>Schema</code> object as an
 * <code>Graph</code> object. This resulting <code>Graph</code> object is an
 * instance of the M3 schema <code>GrUMLSchema</code>.
 * 
 * @author HiWi
 */
public class Tg2SchemaGraph {

	/**
	 * writes a schema's schemagraph to a file. the schema .tg file and the
	 * outputfile get defined by the command line options
	 */
	public static void main(String[] args) {
		Tg2SchemaGraph tg2sg = new Tg2SchemaGraph();
		tg2sg.getOptions(args);
		tg2sg.saveSchemaGraphToFile();
	}

	private int MAX_VERTICES = 1000;
	private int MAX_EDGES = 1000;

	private String outputFilename;
	// the schema, this class was instantiated with.
	private Schema schema;

	// this object will be returned, when getSchemaGraph() is called.
	private GrUMLSchemaGraph schemagraph;

	// helpful to encapsulate the CompositeDomain hierarchy from the
	// rest of the graph
	private Map<de.uni_koblenz.jgralab.schema.Domain, de.uni_koblenz.jgralab.utilities.tg2schemagraph.grumlschema.Domain> domainMap;

	private Map<de.uni_koblenz.jgralab.schema.VertexClass, de.uni_koblenz.jgralab.utilities.tg2schemagraph.grumlschema.VertexClass> vertexClassMap;
	private Map<de.uni_koblenz.jgralab.schema.EdgeClass, de.uni_koblenz.jgralab.utilities.tg2schemagraph.grumlschema.EdgeClass> edgeClassMap;

	/**
	 * The unparameterized constructor is only used in the command line mode.
	 * The method <code>private void setSchema()</code> ensures the
	 * initialization of <code>private Schema schema</code>
	 */
	private Tg2SchemaGraph() {
	}

	/**
	 * This class must be instantiated with a schema. So for every schema, you
	 * want to represent with a <Code>Graph</Code> object, you have to create
	 * a new instance of this class.
	 * 
	 * @param schema
	 *            Any desired <code>Schema</code> object.
	 */
	public Tg2SchemaGraph(Schema schema) {
		this.schema = schema;
	}

	/**
	 * This method creates an instance graph of the grUML language's meta
	 * schema. Its result is a <code>Graph</code>, that represents any
	 * desired <code>Schema</code>.
	 * 
	 * @return a <code>Graph</code> object that represents a
	 *         <code>Schema</code>.
	 */
	public Graph getSchemaGraph() {
		if (schema == null)
			return null;
		if (schemagraph == null) {

			// create the schemagraph
			schemagraph = GrUMLSchema.instance().createGrUMLSchemaGraph(
					schema.getQualifiedName(), MAX_VERTICES, MAX_EDGES);

			// create a vertex for the schema
			de.uni_koblenz.jgralab.utilities.tg2schemagraph.grumlschema.Schema schemaVertex = schemagraph
					.createSchema();

			schemaVertex.setName(schema.getSimpleName());
			schemaVertex.setPackagePrefix(schema.getPackageName());
						
			edgeClassMap = new HashMap<de.uni_koblenz.jgralab.schema.EdgeClass, de.uni_koblenz.jgralab.utilities.tg2schemagraph.grumlschema.EdgeClass>();
			vertexClassMap = new HashMap<de.uni_koblenz.jgralab.schema.VertexClass, de.uni_koblenz.jgralab.utilities.tg2schemagraph.grumlschema.VertexClass>();
			// create a HashMap that maps each schema domain to the
			// corresponding schemagraph domainVertex
			domainMap = new HashMap<Domain, de.uni_koblenz.jgralab.utilities.tg2schemagraph.grumlschema.Domain>();
			createDomainToSchemaGraphDomainVertexMap();

			ArrayList<de.uni_koblenz.jgralab.utilities.tg2schemagraph.grumlschema.Domain> domains = new ArrayList<de.uni_koblenz.jgralab.utilities.tg2schemagraph.grumlschema.Domain>();
			for (de.uni_koblenz.jgralab.utilities.tg2schemagraph.grumlschema.Domain domain : domainMap
					.values())
				domains.add(domain);
			for (de.uni_koblenz.jgralab.utilities.tg2schemagraph.grumlschema.Domain domain : domains)
				schemagraph
						.createContainsDomain(
								(de.uni_koblenz.jgralab.utilities.tg2schemagraph.grumlschema.Schema) schemaVertex,
								domain);

			// create the schemagraph vertex for the graphclass and the
			// schemagraph edge definesGraphClass
			de.uni_koblenz.jgralab.utilities.tg2schemagraph.grumlschema.GraphClass graphClassVertex = schemagraph
					.createGraphClass();
			schemagraph.createDefinesGraphClass(schemaVertex, graphClassVertex);
			
			graphClassVertex.setName(schema.getGraphClassesInTopologicalOrder().get(1).getQualifiedName());
			graphClassVertex.setQualifiedName(schema.getGraphClassesInTopologicalOrder().get(1).getQualifiedName());
			graphClassVertex.setFullyQualifiedName(schema.getPackageName()+"."+schema.getGraphClassesInTopologicalOrder().get(1).getQualifiedName());
			// create vertex for the default package and set its attributes
			// create incident edge containsDefaultPackage
			de.uni_koblenz.jgralab.schema.Package defaultPackage = schema
					.getDefaultPackage();
			de.uni_koblenz.jgralab.utilities.tg2schemagraph.grumlschema.Package defaultPackageVertex = schemagraph
					.createPackage();
			defaultPackageVertex.setName(defaultPackage.getQualifiedName());
			defaultPackageVertex.setQualifiedName(defaultPackage
					.getQualifiedName());
			defaultPackageVertex.setFullyQualifiedName(schema
					.getPackageName());
			schemagraph.createContainsDefaultPackage(schemaVertex,
					defaultPackageVertex);

			//
			createPackageVertices(defaultPackage, defaultPackageVertex);

			//
			createVertexHirarchie();
			createEdgeHirarchie();
		}
		return schemagraph;
	}

	private void createEdgeHirarchie() {
		for (VertexClass vc : schema.getVertexClassesInTopologicalOrder())
			for (AttributedElementClass vcSub : vc.getDirectSubClasses()) {
				schemagraph.createSpecializesVertexClass(
						vertexClassMap.get(vc), vertexClassMap.get(vcSub));
			}
	}

	private void createVertexHirarchie() {
		for (EdgeClass ec : schema.getEdgeClassesInTopologicalOrder())
			for (AttributedElementClass ecSub : ec.getDirectSubClasses()) {
				schemagraph.createSpecializesEdgeClass(edgeClassMap.get(ec),
						edgeClassMap.get(ecSub));
			}
	}

	/**
	 * 
	 * @param superPackage
	 * @param superPackageVertex
	 */
	private void createPackageVertices(
			de.uni_koblenz.jgralab.schema.Package superPackage,
			de.uni_koblenz.jgralab.utilities.tg2schemagraph.grumlschema.Package superPackageVertex) {
		createVertexClassVerticesForPackage(superPackage, superPackageVertex);
		createEdgeClassVerticesForPackage(superPackage, superPackageVertex);

		Map<String, de.uni_koblenz.jgralab.schema.Package> subPackages = superPackage
				.getSubPackages();
		if (subPackages != null)
			for (de.uni_koblenz.jgralab.schema.Package subPackage : subPackages
					.values()) {
				de.uni_koblenz.jgralab.utilities.tg2schemagraph.grumlschema.Package subPackageVertex = schemagraph
						.createPackage();
				subPackageVertex.setName(subPackage.getQualifiedName());
				subPackageVertex.setQualifiedName(subPackage.getPackageName());
				subPackageVertex.setFullyQualifiedName(schema.getPackageName()+"."+subPackage
						.getQualifiedName());
				schemagraph.createContainsSubPackage(superPackageVertex,
						subPackageVertex);
				createPackageVertices(subPackage, subPackageVertex);
			}
	}

	/**
	 * This method creates all <code>VertexClassM2</code> objects, the
	 * <code>isSubVertexClassOfM2</code> edges and the
	 * <code>containsGraphElementClassM2</code> edge.
	 * 
	 * @param pakkageVertex
	 * @param pakkage
	 */
	private void createVertexClassVerticesForPackage(
			Package pakkage,
			de.uni_koblenz.jgralab.utilities.tg2schemagraph.grumlschema.Package pakkageVertex) {

		// for each vertexClass of package pakkage...
		for (VertexClass vc : pakkage.getVertexClasses().values()) {
			// ...create a vertex
			de.uni_koblenz.jgralab.utilities.tg2schemagraph.grumlschema.VertexClass vcM2 = schemagraph
					.createVertexClass();
			schemagraph.createContainsGraphElementClass(pakkageVertex, vcM2);

			vcM2.setName(vc.getQualifiedName());
			vcM2.setFullyQualifiedName(schema.getPackageName() + "."
					+ vc.getQualifiedName());
			vcM2.setQualifiedName(vc.getQualifiedName());
			vcM2.setIsAbstract(vc.isAbstract());

			vertexClassMap.put(vc, vcM2);

			// ..each attribute gets created.
			for (Attribute attr : vc.getOwnAttributeList())
				createAttributeM2(attr, vcM2);
		}

	}

	/**
	 * This method creates all <code>EdgeClassM2</code> objects, the
	 * <code>FromM2</code> and <code>ToM2</code> edges, the
	 * <code>isSubEdgeClassOfM2</code> edges and the
	 * <code>containsGraphElementClassM2</code> edge.
	 * 
	 * @param superPackageVertex
	 * @param superPackage
	 */
	private void createEdgeClassVerticesForPackage(
			Package superPackage,
			de.uni_koblenz.jgralab.utilities.tg2schemagraph.grumlschema.Package superPackageVertex) {
		// for each edge class..
		for (EdgeClass ec : schema.getEdgeClassesInTopologicalOrder()) {
			de.uni_koblenz.jgralab.utilities.tg2schemagraph.grumlschema.EdgeClass ecM2 = null;

			// ..either an EdgeClassM2 or EdgeClassM2 subclass objects gets
			// created.

			if (ec instanceof CompositionClass) {
				ecM2 = schemagraph.createCompositionClass();
			} else if (ec instanceof AggregationClass) {
				ecM2 = schemagraph.createAggregationClass();
			} else {
				ecM2 = schemagraph.createEdgeClass();
			}
			schemagraph.createContainsGraphElementClass(superPackageVertex,
					ecM2);

			ecM2.setName(ec.getQualifiedName());
			ecM2.setQualifiedName(ec.getQualifiedName());
			ecM2.setFullyQualifiedName(schema.getPackageName() + "."
					+ ec.getQualifiedName());
			ecM2.setIsAbstract(ec.isAbstract());

			// ..the FromM2 aggregation gets created.
			// ..the FromM2 attributes gets initialized.
			for (de.uni_koblenz.jgralab.utilities.tg2schemagraph.grumlschema.VertexClass vcFrom : schemagraph
					.getVertexClassVertices()) {
				if (vcFrom.getName().equals(ec.getFrom().getQualifiedName())) {
					From fromM2 = schemagraph.createFrom(ecM2, vcFrom);
					fromM2.setRoleName(ec.getFromRolename());
					fromM2.setMin(ec.getFromMin());
					fromM2.setMax(ec.getFromMax());
					break;
				}
			}

			// ..the ToM2 aggregation gets created.
			// ..the ToM2 attributes gets initialized.
			for (de.uni_koblenz.jgralab.utilities.tg2schemagraph.grumlschema.VertexClass vcTo : schemagraph
					.getVertexClassVertices()) {
				if (vcTo.getName().equals(ec.getTo().getQualifiedName())) {
					To toM2 = schemagraph.createTo(ecM2, vcTo);
					toM2.setRoleName(ec.getToRolename());
					toM2.setMin(ec.getToMin());
					toM2.setMax(ec.getToMax());
					break;
				}
			}
			edgeClassMap.put(ec, ecM2);
			// ..each attribute gets created.
			for (Attribute attr : ec.getOwnAttributeList()) {
				createAttributeM2(attr, ecM2);
			}
		}
	}

	/**
	 * This method creates <code>AttributeM2</code> objects. It gets called by
	 * <code>createGraphClassesM2</code>, <code>createVertexClassesM2</code>
	 * or <code>createEdgeClassesM2</code>. The association to the caller:
	 * <code>hasAttributeM2</code>, and the association to the
	 * <code>AttributeM2</code>'s <code>DomainM2</code>:
	 * <code>hasDomainM2</code> also get created here.
	 */
	private void createAttributeM2(
			Attribute attr,
			de.uni_koblenz.jgralab.utilities.tg2schemagraph.grumlschema.AttributedElementClass elemM2) {
		de.uni_koblenz.jgralab.utilities.tg2schemagraph.grumlschema.Attribute attrM2 = schemagraph
				.createAttribute();
		attrM2.setName(attr.getName());

		// the link HasAttributeM2 from AttributedElementClassM2 to AttributeM2
		// gets created.
		schemagraph.createHasAttribute(elemM2, attrM2);

		schemagraph.createHasDomain(attrM2, domainMap.get(attr.getDomain()));
	}

	/**
	 * This method values the
	 * <code>Map<de.uni_koblenz.jgralab.schema.Domain, de.uni_koblenz.jgralab.utilities.tg2schemagraph.grumlschema.Domain> domainMap</code>.
	 * i.e. <code>domainMap.get(de.uni_koblenz.jgralab.schema.Domain d)</code>
	 * return the corresponding
	 * <code>de.uni_koblenz.jgralab.utilities.tg2schemagraph.grumlschema.Domain</code>
	 * object.
	 * 
	 * At first only the <code>BasicDomain</code>s get mapped. The
	 * <code>CompositeDomain</code>s get mapped according to their
	 * "structural depth". First, composites of basic types get mapped. Then
	 * composites of composites of basic types get mapped...
	 */
	private void createDomainToSchemaGraphDomainVertexMap() {
		Map<QualifiedName, Domain> domains = schema.getDomains();
		while (domainMap.size() != domains.size()) {
			for (Domain d : domains.values()) {
				if (domainMap.get(d) != null)
					continue;

				if (d instanceof BooleanDomain) {
					domainMap.put(d, schemagraph.createBooleanDomain());
				} else if (d instanceof DoubleDomain) {
					domainMap.put(d, schemagraph.createDoubleDomain());
				} else if (d instanceof EnumDomain) {
					de.uni_koblenz.jgralab.utilities.tg2schemagraph.grumlschema.EnumDomain enumM2 = schemagraph
							.createEnumDomain();
					enumM2.setEnumConstants(((EnumDomain) d).getConsts());
					domainMap.put(d, enumM2);
				} else if (d instanceof LongDomain) {
					domainMap.put(d, schemagraph.createLongDomain());
				} else if (d instanceof IntDomain) {
					domainMap.put(d, schemagraph.createIntDomain());
				} else if (d instanceof ObjectDomain) {
					domainMap.put(d, schemagraph.createObjectDomain());
				} else if (d instanceof StringDomain) {
					domainMap.put(d, schemagraph.createStringDomain());
				} else if (d instanceof CompositeDomain) {
					createCompositeDomainM2(d);
				}
			}
		}
	}

	/**
	 * This method checks, if a <code>CompositeDomainM2</code> can be created.
	 * The condition asks every underlying <code>DomainM2</code> to be created
	 * first. If so, it creates a <code>CompositeDomainM2</code> object and
	 * maps its corresponding <code>CompositeDomain</code> object to it.
	 */
	private void createCompositeDomainM2(Domain d) {
		if (d instanceof ListDomain
				&& !(domainMap.get(((ListDomain) d).getBaseDomain()) == null)) {

			de.uni_koblenz.jgralab.utilities.tg2schemagraph.grumlschema.ListDomain dM2 = schemagraph
					.createListDomain();
			schemagraph.createHasListElementDomain(dM2, domainMap
					.get(((ListDomain) d).getBaseDomain()));
			domainMap.put(d, dM2);
		}
		if (d instanceof SetDomain
				&& !(domainMap.get(((SetDomain) d).getBaseDomain()) == null)) {

			de.uni_koblenz.jgralab.utilities.tg2schemagraph.grumlschema.SetDomain dM2 = schemagraph
					.createSetDomain();
			schemagraph.createHasSetElementDomain(dM2, domainMap
					.get(((SetDomain) d).getBaseDomain()));
			domainMap.put(d, dM2);
		}
		if (d instanceof RecordDomain) {
			boolean allBaseDomainsMapped = true;
			for (Domain dom : ((RecordDomain) d).getComponents().values())
				if (domainMap.get(dom) == null) {
					allBaseDomainsMapped = false;
					break;
				}
			if (allBaseDomainsMapped) {
				de.uni_koblenz.jgralab.utilities.tg2schemagraph.grumlschema.RecordDomain dM2 = schemagraph
						.createRecordDomain();
				dM2.setName(((RecordDomain) d).getQualifiedName());
				Map<String, Domain> recordMap = ((RecordDomain) d)
						.getComponents();
				for (String key : recordMap.keySet()) {
					HasRecordDomainComponent hrc = schemagraph
							.createHasRecordDomainComponent(dM2, domainMap
									.get(recordMap.get(key)));
					hrc.setName(key);
				}
				domainMap.put(d, dM2);
			}
		}
	}

	/**
	 * This methods writes the schemagrpah to a file (see GraphIO.java)
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
	 * This method writes the schemagraph to an DataOutputStream (see
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