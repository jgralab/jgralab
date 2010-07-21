/*
 * JGraLab - The Java graph laboratory
 * (c) 2006-2010 Institute for Software Technology
 *               University of Koblenz-Landau, Germany
 *
 *               ist@uni-koblenz.de
 *
 * Please report bugs to http://serres.uni-koblenz.de/bugzilla
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

package de.uni_koblenz.jgralab.utilities.rsa;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;

import de.uni_koblenz.jgralab.EdgeDirection;
import de.uni_koblenz.jgralab.GraphIO;
import de.uni_koblenz.jgralab.grumlschema.SchemaGraph;
import de.uni_koblenz.jgralab.grumlschema.domains.Domain;
import de.uni_koblenz.jgralab.grumlschema.domains.EnumDomain;
import de.uni_koblenz.jgralab.grumlschema.domains.HasRecordDomainComponent;
import de.uni_koblenz.jgralab.grumlschema.domains.RecordDomain;
import de.uni_koblenz.jgralab.grumlschema.structure.Annotates;
import de.uni_koblenz.jgralab.grumlschema.structure.Attribute;
import de.uni_koblenz.jgralab.grumlschema.structure.AttributedElementClass;
import de.uni_koblenz.jgralab.grumlschema.structure.Comment;
import de.uni_koblenz.jgralab.grumlschema.structure.Constraint;
import de.uni_koblenz.jgralab.grumlschema.structure.ContainsDomain;
import de.uni_koblenz.jgralab.grumlschema.structure.ContainsGraphElementClass;
import de.uni_koblenz.jgralab.grumlschema.structure.ContainsSubPackage;
import de.uni_koblenz.jgralab.grumlschema.structure.EdgeClass;
import de.uni_koblenz.jgralab.grumlschema.structure.GraphClass;
import de.uni_koblenz.jgralab.grumlschema.structure.GraphElementClass;
import de.uni_koblenz.jgralab.grumlschema.structure.HasAttribute;
import de.uni_koblenz.jgralab.grumlschema.structure.HasConstraint;
import de.uni_koblenz.jgralab.grumlschema.structure.IncidenceClass;
import de.uni_koblenz.jgralab.grumlschema.structure.NamedElement;
import de.uni_koblenz.jgralab.grumlschema.structure.Package;
import de.uni_koblenz.jgralab.grumlschema.structure.Redefines;
import de.uni_koblenz.jgralab.grumlschema.structure.Schema;
import de.uni_koblenz.jgralab.grumlschema.structure.SpecializesEdgeClass;
import de.uni_koblenz.jgralab.grumlschema.structure.SpecializesVertexClass;
import de.uni_koblenz.jgralab.grumlschema.structure.VertexClass;

public class SchemaGraph2Tg {

	private boolean useShortNames = true;

	/**
	 * @return the useShortNames
	 */
	public boolean isUseShortNames() {
		return useShortNames;
	}

	/**
	 * @param useShortNames
	 *            the useShortNames to set
	 */
	public void setUseShortNames(boolean useShortNames) {
		this.useShortNames = useShortNames;
	}

	private final static String SPACE = " ";
	private final static String EMPTY = "";

	private final static String STAR = "*";
	private final static String DOT = ".";
	private final static String COMMA = ",";
	private final static String DELIMITER = ";";
	private final static String COLON = ":";
	private final static String CURLY_BRACKET_OPENED = "{";
	private final static String CURLY_BRACKET_CLOSED = "}";
	private final static String SQUARE_BRACKET_OPENED = "[";
	private final static String SQUARE_BRACKET_CLOSED = "]";
	private final static String ROUND_BRACKET_OPENED = "(";
	private final static String ROUND_BRACKET_CLOSED = ")";

	private final static String FROM = "from";
	private final static String TO = "to";
	private final static String ROLE = "role";
	private final static String REDEFINES = "redefines";

	private final static String SCHEMA = "Schema";
	private final static String PACKAGE = "Package";
	private final static String COMMENT = "Comment";
	private final static String ABSTRACT = "abstract";
	private final static String VERTEX_CLASS = "VertexClass";
	private final static String GRAPH_CLASS = "GraphClass";
	private final static String RECORD_DOMAIN = "RecordDomain";
	private final static String ENUM_DOMAIN = "EnumDomain";
	private final static String EDGE_CLASS = "EdgeClass";
	private final static String AGGREGATION = "aggregation";
	private final static String AGG_SHARED = "shared";
	private final static String AGG_COMPOSITE = "composite";
	private final static String TGRAPH = "TGraph";
	private final static String TGRAPH_VERSION = "2";
	private final static String ASSIGN = "=";
	private static final String NEWLINE = "\n";

	/**
	 * SchemaGraph which should be transformed to a TG file.
	 */
	private final SchemaGraph schemaGraph;

	/**
	 * Name of the output TG file.
	 */
	private final String outputFilename;

	/**
	 * Stores the current used package name.
	 */
	private String currentPackageName;

	/**
	 * PrintWriter object, which is used to write the TG file.
	 */
	private Writer stream;

	/**
	 * Constructs an object, which will print out the specified
	 * {@link SchemaGraph} to a TG file with the given output filename. The TG
	 * output will be hierarchical ordered. This means all qualified names will
	 * be simple names.<br>
	 * <br>
	 * 
	 * <strong>Note:</strong> run() have to be executed to get a TG file.
	 * 
	 * @param sg
	 *            {@link SchemaGraph}, which will be written to a TG file.
	 * @param outputFilename
	 *            {@link String} of the Location of the TG file. Note: The file
	 *            will be overwritten!
	 */
	public SchemaGraph2Tg(SchemaGraph sg, String outputFilename) {
		schemaGraph = sg;
		this.outputFilename = outputFilename;
	}

	/**
	 * Prints the specified {@link SchemaGraph} to a location according to the
	 * given outputFilename via a {@link PrintWriter}.<br>
	 * 
	 * @throws IOException
	 */
	public void process() throws IOException {

		assert outputFilename != null && !outputFilename.equals(EMPTY) : "No output filename specified!";
		assert schemaGraph != null : "No SchemaGraph specified!";
		stream = new PrintWriter(outputFilename);

		// This line is for debugging and developing purposes only.
		// stream = new PrintWriter(System.out);

		printTGSchema(schemaGraph);

		// Write out, close and dispose the Printstream object.
		stream.append(NEWLINE);
		stream.flush();
		stream.close();
		stream = null;
	}

	public void setStream(StringWriter stream) {
		this.stream = stream;
	}

	/**
	 * Transforms a {@link SchemaGraph} to a TG string, which is written to a
	 * {@link PrintWriter} object stored in the member variable
	 * <code>stream</code>. The transformation rules
	 * <code>PackageDeclaration</code>, <code>DomainDefinition</code>,
	 * <code>VertexClassDefinition</code>, <code>EdgeClassDefinition</code>,
	 * <code>AggregationClassDefinition</code> and
	 * <code>CompositionClassDefinition</code> are encapsulated in methods
	 * corresponding to a prefix "print" and the name of the EBNF rule.<br>
	 * <br>
	 * 
	 * @param schemaGraph
	 *            {@link SchemaGraph}, which should be transformed to TG string.
	 */
	private void printTGSchema(SchemaGraph schemaGraph) {
		// The version of the TG format
		println(TGRAPH, SPACE, TGRAPH_VERSION, DELIMITER, NEWLINE);

		// schema
		Schema schema = schemaGraph.getFirstSchema();
		assert schema != null;
		println(SCHEMA, SPACE, schema.get_packagePrefix(), DOT, schema
				.get_name(), DELIMITER, NEWLINE);

		Package defaultPackage = (Package) schema
				.getFirstContainsDefaultPackage(EdgeDirection.OUT).getThat();
		setCurrentPackageName(defaultPackage);

		// graphclass
		GraphClass gc = (GraphClass) schema.getFirstDefinesGraphClass(
				EdgeDirection.OUT).getOmega();
		printGraphClass(gc);
		printComments(gc);

		printPackageWithElements((Package) schema
				.getFirstContainsDefaultPackage(EdgeDirection.OUT).getThat());
	}

	private void printPackageWithElements(Package gPackage) {
		setCurrentPackageName(gPackage);
		printComments(gPackage);
		println(PACKAGE, SPACE, currentPackageName, DELIMITER);

		for (ContainsDomain cd : gPackage
				.getContainsDomainIncidences(EdgeDirection.OUT)) {
			printDomain((Domain) cd.getThat());
		}

		for (ContainsGraphElementClass cgec : gPackage
				.getContainsGraphElementClassIncidences(EdgeDirection.OUT)) {
			GraphElementClass gec = (GraphElementClass) cgec.getThat();
			if (gec instanceof EdgeClass) {
				printEdgeClass((EdgeClass) gec);
			} else {
				printVertexClass((VertexClass) gec);
			}
		}

		for (ContainsSubPackage csp : gPackage
				.getContainsSubPackageIncidences(EdgeDirection.OUT)) {
			printPackageWithElements((Package) csp.getThat());
		}
	}

	private void setCurrentPackageName(Package pkg) {
		currentPackageName = pkg.get_qualifiedName();
	}

	private void printComments(NamedElement ne) {
		for (Annotates ann : ne.getAnnotatesIncidences(EdgeDirection.IN)) {
			Comment com = (Comment) ann.getThat();
			println(COMMENT, SPACE, ne.get_qualifiedName(), SPACE, GraphIO
					.toUtfString(com.get_text()), DELIMITER);
		}
	}

	public void printVertexClass(VertexClass vc) {
		printComments(vc);
		if (vc.is_abstract()) {
			print(ABSTRACT, SPACE);
		}
		print(VERTEX_CLASS, SPACE, shortName(vc.get_qualifiedName()));

		// superclasses
		if (vc.getFirstSpecializesVertexClass(EdgeDirection.OUT) != null) {
			print(COLON, SPACE);
			boolean first = true;
			for (SpecializesVertexClass svc : vc
					.getSpecializesVertexClassIncidences(EdgeDirection.OUT)) {
				if (first) {
					first = false;
				} else {
					print(COMMA, SPACE);
				}
				VertexClass superVC = (VertexClass) svc.getThat();
				print(shortName(superVC.get_qualifiedName()));
			}
		}

		// attributes
		printAttributes(vc);

		// constraints
		printConstraints(vc);
		println(DELIMITER);
	}

	public void printEdgeClass(EdgeClass ec) {
		printComments(ec);
		if (ec.is_abstract()) {
			print(ABSTRACT, SPACE);
		}
		print(EDGE_CLASS, SPACE, shortName(ec.get_qualifiedName()));

		// superclasses
		if (ec.getFirstSpecializesEdgeClass(EdgeDirection.OUT) != null) {
			print(COLON, SPACE);
			boolean first = true;
			for (SpecializesEdgeClass svc : ec
					.getSpecializesEdgeClassIncidences(EdgeDirection.OUT)) {
				if (first) {
					first = false;
				} else {
					print(COMMA, SPACE);
				}
				EdgeClass superEC = (EdgeClass) svc.getThat();
				print(shortName(superEC.get_qualifiedName()));
			}
		}

		// from/to
		IncidenceClass fromIC = (IncidenceClass) ec.getFirstComesFrom(
				EdgeDirection.OUT).getThat();
		IncidenceClass toIC = (IncidenceClass) ec.getFirstGoesTo(
				EdgeDirection.OUT).getThat();
		VertexClass fromVC = (VertexClass) fromIC.getFirstEndsAt(
				EdgeDirection.OUT).getThat();
		VertexClass toVC = (VertexClass) toIC.getFirstEndsAt(EdgeDirection.OUT)
				.getThat();

		print(SPACE, FROM, SPACE, shortName(fromVC.get_qualifiedName()));
		printMultiplicitiesAndRoles(fromIC);
		print(SPACE, TO, SPACE, shortName(toVC.get_qualifiedName()));
		printMultiplicitiesAndRoles(toIC);

		// attrs
		printAttributes(ec);

		// constraints
		printConstraints(ec);
		println(DELIMITER);
	}

	private void printAggregation(IncidenceClass inc) {
		assert inc != null;
		switch (inc.get_aggregation()) {
		case NONE:
			break;
		case SHARED:
			print(SPACE, AGGREGATION, SPACE, AGG_SHARED);
			break;
		case COMPOSITE:
			print(SPACE, AGGREGATION, SPACE, AGG_COMPOSITE);
			break;
		}
	}

	private String shortName(String qname) {
		if (!useShortNames || isPredefinedDomainName(qname)) {
			return qname;
		}

		// To refer to elements in the default package while not being there, we
		// need to add a DOT.
		if (!qname.contains(".") && !currentPackageName.isEmpty()) {
			return '.' + qname;
		}
		return qname.replaceFirst("^" + currentPackageName + "\\" + DOT, "");

	}

	private boolean isPredefinedDomainName(String qname) {
		return qname.equals("Integer") || qname.equals("String")
				|| qname.equals("Long") || qname.equals("Double")
				|| qname.startsWith("List<") || qname.startsWith("Set<")
				|| qname.startsWith("Map<") || qname.equals("Boolean");
	}

	private void printMultiplicitiesAndRoles(IncidenceClass ic) {
		String min = ic.get_min() == Integer.MAX_VALUE ? STAR : String
				.valueOf(ic.get_min());
		String max = ic.get_max() == Integer.MAX_VALUE ? STAR : String
				.valueOf(ic.get_max());
		print(SPACE, ROUND_BRACKET_OPENED, min, COMMA, max,
				ROUND_BRACKET_CLOSED);

		if (ic.get_roleName() != null && !ic.get_roleName().isEmpty()) {
			print(SPACE, ROLE, SPACE, ic.get_roleName());
		}

		printAggregation(ic);

		if (ic.getFirstRedefines(EdgeDirection.OUT) != null) {
			print(SPACE, REDEFINES, SPACE);
			boolean first = true;
			for (Redefines r : ic.getRedefinesIncidences(EdgeDirection.OUT)) {
				if (first) {
					first = false;
				} else {
					print(COMMA, SPACE);
				}
				IncidenceClass redefined = (IncidenceClass) r.getThat();
				print(redefined.get_roleName());
			}
		}
	}

	private void printDomain(Domain dom) {
		if (dom instanceof RecordDomain) {
			printRecordDomain((RecordDomain) dom);
			return;
		} else if (dom instanceof EnumDomain) {
			printEnumDomain((EnumDomain) dom);
			return;
		}
	}

	private void printEnumDomain(EnumDomain dom) {
		printComments(dom);
		print(ENUM_DOMAIN, SPACE, dom.get_qualifiedName(), SPACE,
				ROUND_BRACKET_OPENED);
		boolean first = true;
		for (String constant : dom.get_enumConstants()) {
			if (first) {
				first = false;
			} else {
				print(COMMA, SPACE);
			}
			print(constant);
		}
		println(ROUND_BRACKET_CLOSED, DELIMITER);
	}

	private void printRecordDomain(RecordDomain dom) {
		printComments(dom);
		print(RECORD_DOMAIN, SPACE, dom.get_qualifiedName(), SPACE,
				ROUND_BRACKET_OPENED);
		boolean first = true;
		for (HasRecordDomainComponent hc : dom
				.getHasRecordDomainComponentIncidences(EdgeDirection.OUT)) {
			if (first) {
				first = false;
			} else {
				print(COMMA, SPACE);
			}
			Domain compDom = (Domain) hc.getThat();
			print(hc.get_name(), COLON, SPACE, compDom.get_qualifiedName());
		}
		println(ROUND_BRACKET_CLOSED, DELIMITER);
	}

	private void printGraphClass(GraphClass gc) {
		print(GRAPH_CLASS, SPACE, gc.get_qualifiedName());
		printAttributes(gc);
		printConstraints(gc);
		println(DELIMITER, NEWLINE);
	}

	private void printConstraints(AttributedElementClass aec) {
		for (HasConstraint hc : aec
				.getHasConstraintIncidences(EdgeDirection.OUT)) {
			Constraint constr = (Constraint) hc.getThat();
			print(SPACE, SQUARE_BRACKET_OPENED);

			print(GraphIO.toUtfString(constr.get_message()), SPACE);
			print(GraphIO.toUtfString(constr.get_predicateQuery()));

			String offElemQ = constr.get_offendingElementsQuery();
			if (offElemQ != null) {
				print(SPACE, GraphIO.toUtfString(offElemQ));
			}

			print(SQUARE_BRACKET_CLOSED);
		}
	}

	private void printAttributes(AttributedElementClass aec) {
		if (aec.getFirstHasAttribute(EdgeDirection.OUT) == null) {
			return;
		}

		print(SPACE, CURLY_BRACKET_OPENED);
		boolean first = true;
		for (HasAttribute ha : aec.getHasAttributeIncidences(EdgeDirection.OUT)) {
			if (first) {
				first = false;
			} else {
				print(COMMA, SPACE);
			}
			Attribute attr = (Attribute) ha.getThat();
			Domain dom = (Domain) attr.getFirstHasDomain(EdgeDirection.OUT)
					.getThat();
			print(attr.get_name(), COLON, SPACE, shortName(dom
					.get_qualifiedName()));
			String defaultValue = attr.get_defaultValue();
			if (defaultValue != null) {
				print(SPACE, ASSIGN, SPACE, GraphIO.toUtfString(defaultValue));
			}
		}
		print(CURLY_BRACKET_CLOSED);
	}

	private void println(String... strings) {
		print(strings);
		try {
			stream.write(NEWLINE);
		} catch (IOException e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		}
	}

	private void print(String... strings) {
		try {
			for (int i = 0; i < strings.length - 1; i++) {
				stream.write(strings[i]);
			}
			stream.write(strings[strings.length - 1]);
		} catch (IOException ex) {
			ex.printStackTrace();
			throw new RuntimeException(ex);
		}
	}
}
