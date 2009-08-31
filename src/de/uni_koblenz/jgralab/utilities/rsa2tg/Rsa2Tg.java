/*
 * JGraLab - The Java graph laboratory
 * (c) 2006-2009 Institute for Software Technology
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

package de.uni_koblenz.jgralab.utilities.rsa2tg;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;
import java.util.TreeSet;
import java.util.Map.Entry;
import java.util.logging.Level;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;

import de.uni_koblenz.jgralab.AttributedElement;
import de.uni_koblenz.jgralab.Edge;
import de.uni_koblenz.jgralab.EdgeDirection;
import de.uni_koblenz.jgralab.GraphIO;
import de.uni_koblenz.jgralab.GraphIOException;
import de.uni_koblenz.jgralab.GraphMarker;
import de.uni_koblenz.jgralab.JGraLab;
import de.uni_koblenz.jgralab.Vertex;
import de.uni_koblenz.jgralab.WorkInProgress;
import de.uni_koblenz.jgralab.graphvalidator.ConstraintViolation;
import de.uni_koblenz.jgralab.graphvalidator.GraphValidator;
import de.uni_koblenz.jgralab.grumlschema.GrumlSchema;
import de.uni_koblenz.jgralab.grumlschema.SchemaGraph;
import de.uni_koblenz.jgralab.grumlschema.domains.CollectionDomain;
import de.uni_koblenz.jgralab.grumlschema.domains.Domain;
import de.uni_koblenz.jgralab.grumlschema.domains.EnumDomain;
import de.uni_koblenz.jgralab.grumlschema.domains.HasRecordDomainComponent;
import de.uni_koblenz.jgralab.grumlschema.domains.MapDomain;
import de.uni_koblenz.jgralab.grumlschema.domains.RecordDomain;
import de.uni_koblenz.jgralab.grumlschema.domains.StringDomain;
import de.uni_koblenz.jgralab.grumlschema.structure.AggregationClass;
import de.uni_koblenz.jgralab.grumlschema.structure.Attribute;
import de.uni_koblenz.jgralab.grumlschema.structure.AttributedElementClass;
import de.uni_koblenz.jgralab.grumlschema.structure.CompositionClass;
import de.uni_koblenz.jgralab.grumlschema.structure.Constraint;
import de.uni_koblenz.jgralab.grumlschema.structure.ContainsGraphElementClass;
import de.uni_koblenz.jgralab.grumlschema.structure.EdgeClass;
import de.uni_koblenz.jgralab.grumlschema.structure.From;
import de.uni_koblenz.jgralab.grumlschema.structure.GraphClass;
import de.uni_koblenz.jgralab.grumlschema.structure.HasAttribute;
import de.uni_koblenz.jgralab.grumlschema.structure.HasDomain;
import de.uni_koblenz.jgralab.grumlschema.structure.Package;
import de.uni_koblenz.jgralab.grumlschema.structure.Schema;
import de.uni_koblenz.jgralab.grumlschema.structure.To;
import de.uni_koblenz.jgralab.grumlschema.structure.VertexClass;
import de.uni_koblenz.jgralab.utilities.common.OptionHandler;
import de.uni_koblenz.jgralab.utilities.tg2dot.Tg2Dot;
import de.uni_koblenz.jgralab.utilities.xml.SchemaGraph2XSD;

/**
 * Rsa2Tg is a utility that converts XMI files exported from IBM (tm) Rational
 * Software Architect (tm) into a TG schema file. The converter is based on a
 * SAX parser. As intermediate format, a grUML schema graph is created from the
 * XMI elements.
 * 
 * @author ist@uni-koblenz.de
 * 
 *         TODO: Currently Rsa2Tg breaks if multiplicities are given for
 *         attributes. But it shouldn't do so if an attribute has the
 *         multiplicity (1,1).
 */
@WorkInProgress(description = "TODOs: switch from SAX to STAX,"
		+ "record comments," + "implement command line interface,"
		+ "error checking and reporting", responsibleDevelopers = "riediger, mmce")
public class Rsa2Tg {
	/**
	 * Contains XML element names in the format "name>xmiId"
	 */
	private Stack<String> elementNameStack;

	/**
	 * Collects character data per element
	 */
	private Stack<StringBuilder> elementContent;

	/**
	 * Names of XML elements which are completely ignored (including children)
	 */
	private final Set<String> ignoredElements;

	/**
	 * Counter for ignored state. ignore>0 ==> elements are ignored, ignore==0
	 * ==> elements are processed.
	 */
	private int ignore;

	/**
	 * The schema graph.
	 */
	private SchemaGraph sg;

	/**
	 * The Schema vertex of the schema graph.
	 */
	private Schema schema;

	/**
	 * The GraphClass vertex of the schema graph.
	 */
	private GraphClass graphClass;

	/**
	 * A Stack containing the package hierarchy. Packages and their nesting are
	 * represented as tree in XML. The top element is the current package.
	 */
	private Stack<Package> packageStack;

	/**
	 * Maps XMI-Ids to vertices and edges of the schema graph.
	 */
	private Map<String, AttributedElement> idMap;

	/**
	 * Remembers the current class id for processing of nested elements.
	 */
	private String currentClassId;

	/**
	 * Remembers the current VertexClass/EdgeClass vertex for processing of
	 * nested elements.
	 */
	private AttributedElementClass currentClass;

	/**
	 * Remembers the current RecordDomain vertex for processing of nested
	 * elements.
	 */
	private RecordDomain currentRecordDomain;

	/**
	 * Remembers the current domain component edge for processing of nested
	 * elements.
	 */
	private HasRecordDomainComponent currentRecordDomainComponent;

	/**
	 * Remembers the current Attribute vertex for processing of nested elements.
	 */
	private Attribute currentAttribute;

	/**
	 * Marks Vertex/EdgeClass vertices with a set of XMI Ids of superclasses.
	 */
	private GraphMarker<Set<String>> generalizations;

	/**
	 * Keeps track of uml:Realizations (key = client id, value = set of supplier
	 * ids) as workaround for missing generalizations between association and
	 * association class.
	 */
	private Map<String, Set<String>> realizations;

	/**
	 * Marks Attribute vertices with the XMI Id of its type if the type can not
	 * be resolved at the time the Attribute is processed.
	 */
	private GraphMarker<String> attributeType;

	/**
	 * Marks RecordDomainComponent edges with the XMI Id of its type if the type
	 * can not be resolved at the time the component is processed.
	 */
	private GraphMarker<String> recordComponentType;

	/**
	 * Maps qualified names of domains to the corresponding Domain vertex.
	 */
	private Map<String, Domain> domainMap;

	/**
	 * A set of preliminary vertices which are created to have a target vertex
	 * for edges where the real target can only be created later (i.e. forward
	 * references in XMI). After processing is finished, this set must be empty,
	 * since each preliminary vertex has to be replaced by the correct vertex.
	 */
	private Set<Vertex> preliminaryVertices;

	/**
	 * Remembers the current association end edge (To/From edge), which can be
	 * an ownedEnd or an ownedAttribute, for processing of nested elements.
	 */
	private Edge currentAssociationEnd;

	/**
	 * The XMI file that's currently processed.
	 */
	private File currentXmiFile;

	/**
	 * The set of To/From edges which are the aggregate side of an
	 * AggregationClass/CompositionClass (use to determine the aggregateFrom
	 * attribute).
	 */
	private Set<Edge> aggregateEnds;

	/**
	 * The set of To/From edges which are represented by ownedEnd elements (used
	 * to determine the direction of edges).
	 */
	private Set<Edge> ownedEnds;

	/**
	 * True if currently processing a constraint (ownedRule) element.
	 */
	private boolean inConstraint;

	/**
	 * The XMI Id of the constrained element if the constraint has exactly one
	 * constrained element, null otherwise. If set to null, the constraint will
	 * be attached to the GraphClass vertex.
	 */
	private String constrainedElementId;

	/**
	 * Maps the XMI Id of constrained elements to the list of constraints.
	 * Constrains are the character data inside a body element of ownedRule
	 * elements.
	 */
	private Map<String, List<String>> constraints;

	/**
	 * When creating EdgeClass names, also use the rolename of the "from" end.
	 */
	private boolean useFromRole;

	/**
	 * After processing is complete, remove Domain vertices which are not used
	 * by an attribute or by a record domain component.
	 */
	private boolean removeUnusedDomains;

	/**
	 * When determining the edge direction, also take navigability of
	 * associations into account (rather than the drawing direction only).
	 */
	private boolean useNavigability;

	/**
	 * Suppresses the direct output into a dot- and tg-file.
	 */
	private boolean suppressOutput;

	/**
	 * Marks whether or not the xmi-file has been processed.
	 */
	private boolean processed;

	/**
	 * Flag for writing the output as SchemaGraph.
	 */
	private boolean writeSchemaGraph;

	/**
	 * Flag for writing debug information of the conversion process.
	 */
	private boolean writeDebug;

	/**
	 * Filename for the Schema.
	 */
	private String filenameSchema;

	/**
	 * Filename for the SchemaGraph;
	 */
	private String filenameSchemaGraph;

	/**
	 * Filename for debug informations.
	 */
	private String filenameDebug;

	/**
	 * Creates a Rsa2Tg converter.
	 */
	public Rsa2Tg() {
		ignoredElements = new TreeSet<String>();
		ignoredElements.add("profileApplication");
		ignoredElements.add("packageImport");
		ignoredElements.add("ownedComment");
	}

	public static void main(String[] args) {
		System.out.println("RSA to TG");
		System.out.println("=========");
		JGraLab.setLogLevel(Level.OFF);
		Rsa2Tg r = new Rsa2Tg();
		r.setUseFromRole(true);
		r.setRemoveUnusedDomains(true);
		r.setUseNavigability(true);

		// Retrieving all command line options
		CommandLine cli = processCommandLineOptions(args);
		String[] input = cli.getOptionValues('i');

		int numFiles = input.length;

		String[] nulls = new String[input.length];
		String[] debug = cli.hasOption('d') ? cli.getOptionValues('d') : nulls;
		String[] output = cli.hasOption('o') ? cli.getOptionValues('o') : nulls;
		String[] schemaGraph = cli.hasOption('s') ? cli.getOptionValues('s')
				: nulls;

		if (debug == null) {
			debug = nulls;
		}
		if (output == null) {
			output = nulls;
		}
		if (schemaGraph == null) {
			schemaGraph = nulls;
		}

		if (debug.length != numFiles || output.length != numFiles
				|| schemaGraph.length != numFiles) {
			throw new IllegalArgumentException(
					"There should be the same amount filenames for debug information, "
							+ "output as Schema or as SchemaGraph given as for input filenames.");
		}

		for (int i = 0; i < input.length; i++) {
			System.out.println("processing: " + input[i]);
			try {
				r.setWriteDebug(cli.hasOption('d'));
				r.setWriteSchemaGraph(cli.hasOption('s'));
				r.process(input[i], output[i], schemaGraph[i], debug[i]);
			} catch (Exception e) {
				System.err.println("An Exception occured while processing "
						+ input[i] + ".");
				System.err.println(e.getMessage());
				e.printStackTrace();
			}
		}
		System.out.println("Fini.");
	}

	/**
	 * Processes all command line parameters and returns a {@link CommandLine}
	 * object, which holds all values included in the given String array.
	 * 
	 * <pre>
	 * RSA to TG
	 * =========
	 * usage: java de.uni_koblenz.jgralab.utilities.xml.SchemaGraph2XSD [-h ] [-v
	 *             ] [-d [&lt;filename&gt;]] [-s [&lt;filename&gt;]] -i &lt;filename&gt; [-o
	 *             &lt;filename&gt;]
	 *  -d,--debug &lt;filename&gt;         (optional): write a validation report
	 *                                '&lt;filename&gt;.gruml.validationreport.html'
	 *                                and a dotty-graph '&lt;filename&gt;.gruml.dot'.
	 *                                The &lt;filename&gt; is optional. In case of no
	 *                                given filename, &lt;filename&gt; :=
	 *                                '&lt;NAME_OF_SCHEMA&gt;'.
	 *  -h,--help                     (optional): print this help message.
	 *  -i,--input &lt;filename&gt;         (required): UML 2.1-XMI exchange modell
	 *                                file of the Schema.
	 *  -o,--output &lt;filename&gt;        (optional): write a TG-file of the Schema
	 *                                in '&lt;filename&gt;.rsa.tg.'
	 *  -s,--schemaGraph &lt;filename&gt;   (optional): write a TG-file of the Schema
	 *                                as graph instance in '&lt;filename&gt;.tg'.
	 *                                &lt;filename&gt; is optional. In case of no given
	 *                                filename, &lt;filename&gt; :=
	 *                                '&lt;NAME_OF_SCHEMA&gt;.gruml'.
	 *  -v,--version                  (optional): print version information
	 * </pre>
	 * 
	 * @param args
	 *            {@link CommandLine} parameters.
	 * @return {@link CommandLine} object, which holds all necessary values.
	 */
	private static CommandLine processCommandLineOptions(String[] args) {

		// Creates a OptionHandler.
		String toolString = "java " + SchemaGraph2XSD.class.getName();
		String versionString = JGraLab.getInfo(false);
		OptionHandler oh = new OptionHandler(toolString, versionString);

		// Several Options are declared.
		Option debug = new Option(
				"d",
				"debug",
				true,
				"(optional): write a validation report '<filename>.gruml.validationreport.html' and a dotty-graph "
						+ "'<filename>.gruml.dot'. The <filename> is optional. In case of no given filename, <filename> := "
						+ "'<NAME_OF_SCHEMA>'.");
		debug.setRequired(false);
		debug.setArgName("filename");
		debug.setOptionalArg(true);
		oh.addOption(debug);

		Option schemaGraph = new Option(
				"s",
				"schemaGraph",
				true,
				"(optional): write a TG-file of the Schema as graph instance in '<filename>.tg'"
						+ ". <filename> is optional. In case of no given filename, <filename> := '<NAME_OF_"
						+ "SCHEMA>.gruml'.");
		schemaGraph.setRequired(false);
		schemaGraph.setArgName("filename");
		schemaGraph.setOptionalArg(true);
		oh.addOption(schemaGraph);

		Option input = new Option("i", "input", true,
				"(required): UML 2.1-XMI exchange modell file of the Schema.");
		input.setRequired(true);
		input.setArgName("filename");
		oh.addOption(input);

		Option output = new Option("o", "output", true,
				"(optional): write a TG-file of the Schema in '<filename>.rsa.tg.'");
		output.setRequired(false);
		output.setArgName("filename");
		oh.addOption(output);

		// Parses the given command line parameters with all created Option.
		return oh.parse(args);
	}

	/**
	 * Processes one RSA XMI file by creating a SAX parser and submitting this
	 * file to the parse() method. All actions take place in overridden methods
	 * of the SAX DefaultHandler.
	 * 
	 * @param xmiFileName
	 *            the name of the XMI file to convert
	 */
	public void process(String xmiFileName, String schemaFileName,
			String schemaGraphFileName, String debugFileName)
			throws FileNotFoundException, XMLStreamException {

		filenameSchema = schemaFileName;
		filenameSchemaGraph = schemaGraphFileName;
		filenameDebug = debugFileName;

		InputStream in = new FileInputStream(xmiFileName);
		currentXmiFile = new File(xmiFileName);
		XMLInputFactory factory = XMLInputFactory.newInstance();
		XMLStreamReader parser = factory.createXMLStreamReader(in);

		System.out.println("start processing...");
		for (int event = parser.getEventType(); event != XMLStreamConstants.END_DOCUMENT; event = parser
				.next()) {
			switch (event) {
			case XMLStreamConstants.START_DOCUMENT:
				startDocument();
				break;
			case XMLStreamConstants.START_ELEMENT:
				startElement(parser);
				break;
			case XMLStreamConstants.END_ELEMENT:
				String name;
				if (parser.getPrefix() == null) {
					name = parser.getLocalName();
				} else {
					name = parser.getPrefix() + ":" + parser.getLocalName();
				}
				endElement(name, parser.getLocation().getLineNumber());
				break;
			case XMLStreamConstants.CHARACTERS:
				elementContent.peek().append(parser.getText());
				break;
			}
		}
		endDocument();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.xml.sax.helpers.DefaultHandler#endDocument()
	 */
	public void endDocument() throws XMLStreamException {
		// finalizes processing by creating missing links
		assert elementNameStack.size() == 0;
		assert ignore == 0;
		if (graphClass.getQualifiedName() == null) {
			throw new XMLStreamException(
					"no <<graphclass>> defined in schema '"
							+ schema.getPackagePrefix() + "."
							+ schema.getName() + "'");
		}
		linkGeneralizations();
		linkRecordDomainComponents();
		linkAttributeDomains();
		setAggregateFromAttributes();
		if (isUseNavigability()) {
			correctEdgeDirection();
		}
		attachConstraints();
		createEdgeClassNames();
		if (isRemoveUnusedDomains()) {
			removeUnusedDomains();
		}
		removeEmptyPackages();
		// preliminaryVertices must be empty at this time of processing,
		// otherwise there is an error...
		if (!preliminaryVertices.isEmpty()) {
			System.err.println("Remaining preliminary vertices ("
					+ preliminaryVertices.size() + "):");
			for (Vertex v : preliminaryVertices) {
				try {
					System.err.println(attributedElement2String(v));
				} catch (NoSuchFieldException e) {
					e.printStackTrace();
				}
			}
		}
		// assert preliminaryVertices.isEmpty();
		if (!preliminaryVertices.isEmpty()) {
			throw new XMLStreamException("There are still vertices left over. ");
		}

		processed = true;
		if (!suppressOutput) {
			writeOutput();
		}
	}

	private String attributedElement2String(AttributedElement a)
			throws NoSuchFieldException {
		StringBuilder sb = new StringBuilder();

		de.uni_koblenz.jgralab.schema.AttributedElementClass aec = a
				.getAttributedElementClass();
		sb.append(a);
		sb.append(" { ");

		for (de.uni_koblenz.jgralab.Attribute attr : aec.getAttributeList()) {
			sb.append(attr.getName());
			sb.append(" = ");
			sb.append(a.getAttribute(attr.getName()));
			sb.append("; ");
		}
		sb.append("}\n");

		return sb.toString();
	}

	/**
	 * Write a dot-file and a tg-file out.
	 * 
	 * @throws XMLStreamException
	 */
	public void writeOutput() throws XMLStreamException {
		if (processed) {
			assert schema != null;
			String schemaName = schema.getName();
			assert schemaName != null;

			String debug, schema, schemaGraph;
			debug = (filenameDebug != null) ? filenameDebug : schemaName;
			schema = (filenameSchema != null) ? filenameSchema : schemaName;
			schemaGraph = (filenameSchemaGraph != null) ? filenameSchemaGraph
					: schemaName;

			if (writeDebug) {

				createDotFile(debug);
			}

			if (writeSchemaGraph) {
				saveGraph(schemaGraph);

				if (writeDebug) {
					validateGraph(debug);
				}
			}
			saveSchemagraphAsTg(schema, false);
		}
	}

	private void saveSchemagraphAsTg(String schemaName, boolean formatTg) {
		try {
			SchemaGraph2Tg sg2tg = new SchemaGraph2Tg(sg, currentXmiFile
					.getParent()
					+ File.separator + schemaName + ".rsa.tg");
			sg2tg.setIsFormatted(formatTg);
			sg2tg.run();
		} catch (IOException e) {
			throw new RuntimeException(
					"SchemaGraph2Tg faild with an IOException!", e);
		}
	}

	private void correctEdgeDirection() {
		if (!isUseNavigability()) {
			return;
		}

		for (EdgeClass e : sg.getEdgeClassVertices()) {
			From from = e.getFirstFrom();
			To to = e.getFirstTo();
			assert (from != null) && (to != null);
			boolean fromIsNavigable = !ownedEnds.contains(from);
			boolean toIsNavigable = !ownedEnds.contains(to);
			if (fromIsNavigable == toIsNavigable) {
				// no navigability specified or both ends navigable:
				// do nothing, edge direction is determined by order of memerEnd
				// in association
				continue;
			}
			if (toIsNavigable) {
				// "to" end is marked navigable, nothing to change
				continue;
			}

			// "from" end is marked navigable, swap edge direction
			VertexClass vc = (VertexClass) to.getThat();
			to.setThat(from.getThat());
			from.setThat(vc);

			int h = to.getMin();
			to.setMin(from.getMin());
			from.setMin(h);

			h = to.getMax();
			to.setMax(from.getMax());
			from.setMax(h);

			String r = to.getRoleName();
			to.setRoleName(from.getRoleName());
			from.setRoleName(r);

			Set<String> rd = to.getRedefinedRoles();
			to.setRedefinedRoles(from.getRedefinedRoles());
			from.setRedefinedRoles(rd);

			if (e instanceof AggregationClass) {
				AggregationClass ac = (AggregationClass) e;
				ac.setAggregateFrom(!ac.isAggregateFrom());
			}
		}

	}

	private void attachConstraints() throws XMLStreamException {
		for (String constrainedElementId : constraints.keySet()) {
			List<String> l = constraints.get(constrainedElementId);
			AttributedElement ae = idMap.get(constrainedElementId);
			if (ae == null) {
				ae = graphClass;
			}
			assert (ae instanceof AttributedElementClass)
					|| (ae instanceof From) || (ae instanceof To);
			if (ae instanceof AttributedElementClass) {
				for (String text : l) {
					if (ae instanceof AttributedElementClass) {
						addGreqlConstraint((AttributedElementClass) ae, text);
					}
				}
			} else {
				assert l.size() == 1 : "Only one redefines allowed";
				addRedefinesConstraint((Edge) ae, l.get(0));
			}
		}
	}

	private void setAggregateFromAttributes() {
		for (Edge e : aggregateEnds) {
			((AggregationClass) e.getAlpha()).setAggregateFrom(e instanceof To);
		}
		aggregateEnds.clear();
	}

	private void createEdgeClassNames() {
		for (EdgeClass ec : sg.getEdgeClassVertices()) {
			String name = ec.getQualifiedName().trim();
			if (!name.equals("") && !name.endsWith(".")) {
				continue;
			}

			// System.err.print("createEdgeClassName for '" + name + "'");
			String ecName = null;
			// invent edgeclass name
			String toRole = ec.getFirstTo().getRoleName();
			if ((toRole == null) || toRole.equals("")) {
				toRole = ((VertexClass) ec.getFirstTo().getOmega())
						.getQualifiedName();
				int p = toRole.lastIndexOf('.');
				if (p >= 0) {
					toRole = toRole.substring(p + 1);
				}
			} else {
				toRole = Character.toUpperCase(toRole.charAt(0))
						+ toRole.substring(1);
			}
			assert (toRole != null) && (toRole.length() > 0);
			if (ec instanceof AggregationClass) {
				if (((AggregationClass) ec).isAggregateFrom()) {
					ecName = "Contains" + toRole;
				} else {
					ecName = "IsPartOf" + toRole;
				}
			} else {
				ecName = "LinksTo" + toRole;
			}
			if (isUseFromRole()) {
				String fromRole = ec.getFirstFrom().getRoleName();
				if ((fromRole == null) || fromRole.equals("")) {
					fromRole = ((VertexClass) ec.getFirstFrom().getOmega())
							.getQualifiedName();
					int p = fromRole.lastIndexOf('.');
					if (p >= 0) {
						fromRole = fromRole.substring(p + 1);
					}
				} else {
					fromRole = Character.toUpperCase(fromRole.charAt(0))
							+ fromRole.substring(1);
				}
				assert (fromRole != null) && (fromRole.length() > 0);
				name += fromRole;
			}
			// System.err.println(" ==> '" + name + "' + '" + ecName + "'");
			assert (ecName != null) && (ecName.length() > 0);
			ec.setQualifiedName(name + ecName);
		}
	}

	private void removeUnusedDomains() {
		if (!isRemoveUnusedDomains()) {
			return;
		}
		Domain d = sg.getFirstDomain();
		while (d != null) {
			Domain n = d.getNextDomain();
			// unused if degree <=1 (one incoming edge is the ContainsDomain
			// edge from a Package)
			if (d.getDegree(EdgeDirection.IN) <= 1) {
				// System.out.println("...remove unused domain '"
				// + d.getQualifiedName() + "'");
				d.delete();
				d = sg.getFirstDomain();
			} else {
				d = n;
			}
		}
	}

	private void linkRecordDomainComponents() {
		for (HasRecordDomainComponent comp : sg
				.getHasRecordDomainComponentEdges()) {
			String domainId = recordComponentType.getMark(comp);
			if (domainId == null) {
				continue;
			}
			Domain dom = (Domain) idMap.get(domainId);
			if (dom != null) {
				Domain d = (Domain) comp.getOmega();
				assert (d instanceof StringDomain)
						&& d.getQualifiedName().equals(domainId)
						&& preliminaryVertices.contains(d);
				comp.setOmega(dom);
				d.delete();
				preliminaryVertices.remove(d);
				recordComponentType.removeMark(comp);
			} else {
				System.err.println("Undefined Domain " + domainId);
			}
		}
		assert recordComponentType.isEmpty();
	}

	private void validateGraph(String schemaName) {
		GraphValidator validator = new GraphValidator(sg);
		try {
			String validationReportFile = currentXmiFile.getParent()
					+ File.separator + schemaName + ".validationreport.html";
			Set<ConstraintViolation> s = validator
					.createValidationReport(validationReportFile);
			if (!s.isEmpty()) {
				System.err.println("The schema graph is not valid :-(\nSee "
						+ validationReportFile + " for details.");
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void linkAttributeDomains() {
		for (Attribute att : sg.getAttributeVertices()) {
			String domainId = attributeType.getMark(att);
			if (domainId == null) {
				assert att.getDegree(HasDomain.class, EdgeDirection.OUT) == 1;
				continue;
			}
			Domain dom = (Domain) idMap.get(domainId);
			if (dom != null) {
				sg.createHasDomain(att, dom);
				attributeType.removeMark(att);
			} else {
				System.err.println("Undefined Domain " + domainId);
			}
			assert att.getDegree(HasDomain.class, EdgeDirection.OUT) == 1;
		}
		assert attributeType.isEmpty();
	}

	private void createDotFile(String schemaName) {
		Tg2Dot tg2Dot = new Tg2Dot();
		tg2Dot.setGraph(sg);
		tg2Dot.setPrintEdgeAttributes(true);
		tg2Dot.setOutputFile(currentXmiFile.getParent() + File.separator
				+ schemaName + ".gruml.dot");
		tg2Dot.printGraph();
	}

	private void saveGraph(String schemaName) throws XMLStreamException {
		try {
			GraphIO.saveGraphToFile(currentXmiFile.getParent() + File.separator
					+ schemaName + ".gruml.tg", sg, null);
		} catch (GraphIOException e) {
			throw new XMLStreamException(e);
		}
	}

	private void linkGeneralizations() {
		for (String clientId : realizations.keySet()) {
			Set<String> suppliers = realizations.get(clientId);
			AttributedElementClass client = (AttributedElementClass) idMap
					.get(clientId);
			if (suppliers.size() > 0) {
				Set<String> superClasses = generalizations.getMark(client);
				if (superClasses == null) {
					superClasses = new TreeSet<String>();
					generalizations.mark(client, superClasses);
				}
				superClasses.addAll(suppliers);
			}
		}
		for (AttributedElement ae : generalizations.getMarkedElements()) {
			Set<String> superclasses = generalizations.getMark(ae);
			for (String id : superclasses) {
				AttributedElementClass sup = (AttributedElementClass) idMap
						.get(id);
				assert (sup != null);
				if (sup instanceof VertexClass) {
					assert ae instanceof VertexClass;
					sg.createSpecializesVertexClass((VertexClass) ae,
							(VertexClass) sup);
				} else {
					assert ae instanceof EdgeClass;
					sg.createSpecializesEdgeClass((EdgeClass) ae,
							(EdgeClass) sup);
				}
			}
		}
		generalizations.clear();
	}

	private void removeEmptyPackages() {
		// remove all empty packages except the default package
		de.uni_koblenz.jgralab.grumlschema.structure.Package p = sg
				.getFirstPackage();
		while (p != null) {
			de.uni_koblenz.jgralab.grumlschema.structure.Package n = p
					.getNextPackage();
			if ((p.getDegree() == 1) && (p.getQualifiedName().length() > 0)) {
				// System.out.println("...remove empty package '"
				// + p.getQualifiedName() + "'");
				p.delete();
				// start over to capture packages that become empty after
				// deletion of p
				p = sg.getFirstPackage();
			} else {
				p = n;
			}
		}
	}

	/**
	 * 
	 * @param name
	 *            the name of the end element
	 * @param line
	 *            the line at which the end element is in the xmi-file
	 * @throws XMLStreamException
	 */
	public void endElement(String name, int line) throws XMLStreamException {
		assert elementNameStack.size() > 0;
		String s = elementNameStack.peek();
		int p = s.indexOf('>');
		assert p >= 0;
		String topName = s.substring(0, p);
		String xmiId = s.substring(p + 1);
		assert topName.equals(name);
		if (ignoredElements.contains(name)) {
			assert ignore > 0;
			--ignore;
		} else if (ignore == 0) {
			if (inConstraint && name.equals("body")) {
				s = elementContent.peek().toString().trim().replace("\\s", " ");
				handleConstraint(s, line);
			}
			AttributedElement elem = idMap.get(xmiId);
			if (elem != null) {
				if (elem instanceof Package) {
					assert packageStack.size() > 1;
					packageStack.pop();
				} else if (elem instanceof AttributedElementClass) {
					currentClassId = null;
					currentClass = null;
				} else if (elem instanceof RecordDomain) {
					currentRecordDomain = null;
				} else if (elem instanceof Attribute) {
					currentAttribute = null;
				}
			}
			if (name.equals("uml:Package")) {
				packageStack.pop();
				assert (packageStack.size() == 0);
			} else if (name.equals("ownedAttribute")) {
				currentRecordDomainComponent = null;
				currentAssociationEnd = null;
			} else if (name.equals("ownedEnd")) {
				currentAssociationEnd = null;
			} else if (name.equals("ownedRule")) {
				inConstraint = false;
				constrainedElementId = null;
			}
		}
		elementNameStack.pop();
		elementContent.pop();
	}

	private void handleConstraint(String text, int line)
			throws XMLStreamException {
		if (text.startsWith("redefines") || text.startsWith("\"")) {
			List<String> l = constraints.get(constrainedElementId);
			if (l == null) {
				l = new LinkedList<String>();
				constraints.put(constrainedElementId, l);
			}
			l.add(text);
		} else {
			throw new XMLStreamException("Illegal constraint format: " + text
					+ " at line " + line);
		}
	}

	private void addRedefinesConstraint(Edge constrainedEnd, String text)
			throws XMLStreamException {
		assert (constrainedEnd instanceof From)
				|| (constrainedEnd instanceof To);

		text = text.trim().replaceAll("\\s+", " ");
		if (!text.startsWith("redefines ")) {
			throw new XMLStreamException("Wrong redefines constraint format.");
		}
		String[] roles = text.substring(10).split("\\s*,\\s*");
		assert roles.length >= 1;
		Set<String> redefinedRoles = new TreeSet<String>();
		for (String role : roles) {
			assert role.length() >= 1;
			redefinedRoles.add(role);
		}
		assert redefinedRoles.size() >= 1;
		if (constrainedEnd instanceof From) {
			((From) constrainedEnd).setRedefinedRoles(redefinedRoles);
		} else {
			((To) constrainedEnd).setRedefinedRoles(redefinedRoles);
		}
	}

	private void addGreqlConstraint(AttributedElementClass constrainedClass,
			String text) throws XMLStreamException {

		assert constrainedClass != null;
		Constraint constraint = sg.createConstraint();
		sg.createHasConstraint(constrainedClass, constraint);

		// the "text" must contain 2 or 3 space-separated quoted ("...") strings
		int stringCount = 0;
		char[] ch = text.toCharArray();
		boolean inString = false;
		boolean escape = false;
		int beginIndex = 0;
		for (int i = 0; i < ch.length; ++i) {
			char c = ch[i];
			if (inString) {
				if (c == '\\') {
					escape = true;
				} else if (!escape && (c == '"')) {
					++stringCount;
					switch (stringCount) {
					case 1:
						constraint.setMessage(text.substring(beginIndex + 1, i)
								.trim());
						break;
					case 2:
						constraint.setPredicateQuery(text.substring(
								beginIndex + 1, i).trim());
						break;
					case 3:
						constraint.setOffendingElementsQuery(text.substring(
								beginIndex + 1, i).trim());
						break;
					default:
						throw new XMLStreamException(
								"Illegal constraint format. The constraint text was '"
										+ text + "'.");
					}
					inString = false;
				} else if (escape && (c == '"')) {
					escape = false;
				}
			} else {
				if (Character.isWhitespace(c)) {
					// ignore
				} else {
					if (c == '"') {
						inString = true;
						beginIndex = i;
					} else {
						throw new XMLStreamException(
								"Illegal constraint format. The constraint text was '"
										+ text + "'.  Expected '\"' but got '"
										+ c + "'.  (position = " + i + ").");
					}
				}
			}
		}
		if (inString || escape || (stringCount < 2) || (stringCount > 3)) {
			throw new XMLStreamException(
					"Illegal constraint format.  The constraint text was '"
							+ text + "'.");
		}
	}

	public void startDocument() {
		elementNameStack = new Stack<String>();
		elementContent = new Stack<StringBuilder>();
		idMap = new HashMap<String, AttributedElement>();
		packageStack = new Stack<de.uni_koblenz.jgralab.grumlschema.structure.Package>();
		sg = GrumlSchema.instance().createSchemaGraph();
		generalizations = new GraphMarker<Set<String>>(sg);
		realizations = new HashMap<String, Set<String>>();
		attributeType = new GraphMarker<String>(sg);
		recordComponentType = new GraphMarker<String>(sg);
		domainMap = new HashMap<String, Domain>();
		preliminaryVertices = new HashSet<Vertex>();
		aggregateEnds = new HashSet<Edge>();
		ownedEnds = new HashSet<Edge>();
		ignore = 0;
		constraints = new HashMap<String, List<String>>();
		// constraintsLines = new HashMap<String, Location>();
	}

	public void startElement(XMLStreamReader parser) throws XMLStreamException {
		String name;
		if (parser.getPrefix() == null) {
			name = parser.getLocalName();
		} else {
			name = parser.getPrefix() + ":" + parser.getLocalName();
		}
		String xmiId = parser.getAttributeValue(parser.getNamespaceURI("xmi"),
				"id");

		elementNameStack.push(name + ">" + (xmiId != null ? xmiId : ""));
		elementContent.push(new StringBuilder());

		if (ignoredElements.contains(name)) {
			++ignore;
		}
		// System.out.println("<" + name + "> (id=" + xmiId + ") "
		// + (ignore > 0 ? "ignored" : "processed"));
		if (ignore > 0) {
			return;
		}
		Vertex idVertex = null;
		if (elementNameStack.size() == 1) {
			if (name.equals("uml:Model") || name.equals("uml:Package")) {
				String nm = parser.getAttributeValue(null, "name");

				int p = nm.lastIndexOf('.');
				schema = sg.createSchema();
				idVertex = schema;
				schema.setPackagePrefix(nm.substring(0, p));
				schema.setName(nm.substring(p + 1));

				graphClass = sg.createGraphClass();
				sg.createDefinesGraphClass(schema, graphClass);

				de.uni_koblenz.jgralab.grumlschema.structure.Package defaultPackage = sg
						.createPackage();
				defaultPackage.setQualifiedName("");
				sg.createContainsDefaultPackage(schema, defaultPackage);
				packageStack.push(defaultPackage);
			} else {
				throw new XMLStreamException("root element at line "
						+ parser.getLocation().getLineNumber()
						+ " must be uml:Model or uml:Package");
			}
		} else {
			// inside toplevel element
			String type = parser.getAttributeValue(parser
					.getNamespaceURI("xmi"), "type");
			if (name.equals("packagedElement")) {
				if (type.equals("uml:Package")) {
					idVertex = handlePackage(parser);
				} else if (type.equals("uml:Class")) {
					idVertex = handleClass(parser, xmiId);
				} else if (type.equals("uml:Association")
						|| type.equals("uml:AssociationClass")) {
					idVertex = handleAssociation(parser, xmiId);
				} else if (type.equals("uml:Enumeration")) {
					idVertex = handleEnumeration(parser);
				} else if (type.equals("uml:PrimitiveType")) {
					idVertex = handlePrimitiveType(parser);
				} else if (type.equals("uml:Realization")) {
					handleRealization(parser);
				} else {
					throw new XMLStreamException("unexpected element " + name
							+ " of type " + type + " at line "
							+ parser.getLocation().getLineNumber());
				}
			} else if (name.equals("ownedRule")) {
				inConstraint = true;
				constrainedElementId = parser.getAttributeValue(null,
						"constrainedElement");
				// If the ID is null, the constraint is attached to the
				// GraphClass

				if (constrainedElementId != null) {
					// There can be more than one ID, separated by spaces ==>
					// the constraint is attached to the GraphClass.
					int p = constrainedElementId.indexOf(' ');
					if (p >= 0) {
						constrainedElementId = null;
					}
				}

			} else if (name.equals("specification") || name.equals("language")
					|| name.equals("body")) {
				if (!inConstraint) {
					throw new XMLStreamException("unecpected element <" + name
							+ "> at line: "
							+ parser.getLocation().getLineNumber());
				}

			} else if (name.equals("ownedEnd")) {
				if (type.equals("uml:Property")
						&& (currentClass instanceof EdgeClass)) {
					handleAssociatioEnd(parser, xmiId);
				} else {
					throw new XMLStreamException("unexpected element <" + name
							+ "> of type " + type + " at line"
							+ parser.getLocation().getLineNumber());
				}
			} else if (name.equals("ownedAttribute")) {
				if (type.equals("uml:Property")) {
					handleOwnedAttribute(parser, xmiId);
				} else {
					throw new XMLStreamException("unexpected element <" + name
							+ "> of type " + type + " at line"
							+ parser.getLocation().getLineNumber());
				}
			} else if (name.equals("type")) {
				if (type.equals("uml:PrimitiveType")) {
					handleNestedTypeElement(parser, type);
				} else {
					throw new XMLStreamException("unexpected element <" + name
							+ "> of type " + type + " at line "
							+ parser.getLocation().getLineNumber());
				}
			} else if (name.equals("ownedLiteral")) {
				if (type.equals("uml:EnumerationLiteral")) {
					handleEnumerationLiteral(parser);
				} else {
					throw new XMLStreamException("unexpected element <" + name
							+ "> of type " + type + " at line "
							+ parser.getLocation().getLineNumber());
				}
			} else if (name.equals("xmi:Extension")) {
				// ignore
			} else if (name.equals("eAnnotations")) {
				// ignore
			} else if (name.equals("generalization")) {
				handleGeneralization(parser);
			} else if (name.equals("details")) {
				handleStereotype(parser);
			} else if (name.equals("lowerValue")) {
				handleLowerValue(parser);
			} else if (name.equals("upperValue")) {
				handleUpperValue(parser);
			} else {
				throw new XMLStreamException("unexpected element <" + name
						+ "> of type " + type + " at line "
						+ parser.getLocation().getLineNumber());
			}
		}
		if ((xmiId != null) && (idVertex != null)) {
			idMap.put(xmiId, idVertex);
		}
	}

	private void handleRealization(XMLStreamReader parser) {
		String supplier = parser.getAttributeValue(null, "supplier");
		String client = parser.getAttributeValue(null, "client");
		Set<String> reals = realizations.get(client);
		if (reals == null) {
			reals = new TreeSet<String>();
			realizations.put(client, reals);
		}
		reals.add(supplier);
	}

	private void handleUpperValue(XMLStreamReader parser) {
		assert currentAssociationEnd != null;
		String val = parser.getAttributeValue(null, "value");
		int n = (val == null) ? 0 : val.equals("*") ? Integer.MAX_VALUE
				: Integer.parseInt(val);
		if (currentAssociationEnd instanceof From) {
			((From) currentAssociationEnd).setMax(n);
		} else {
			((To) currentAssociationEnd).setMax(n);
		}
	}

	private void handleLowerValue(XMLStreamReader parser) {
		assert currentAssociationEnd != null;
		String val = parser.getAttributeValue(null, "value");
		int n = (val == null) ? 0 : val.equals("*") ? Integer.MAX_VALUE
				: Integer.parseInt(val);
		if (currentAssociationEnd instanceof From) {
			((From) currentAssociationEnd).setMin(n);
		} else {
			((To) currentAssociationEnd).setMin(n);
		}
	}

	private void handleStereotype(XMLStreamReader parser)
			throws XMLStreamException {
		String key = parser.getAttributeValue(null, "key");
		if (key.equals("graphclass")) {
			// convert currentClass to graphClass
			assert currentClass != null;
			assert currentClass instanceof VertexClass;
			AttributedElementClass aec = (AttributedElementClass) idMap
					.get(currentClassId);
			graphClass.setQualifiedName(aec.getQualifiedName());
			Edge e = aec.getFirstEdge();
			while (e != null) {
				Edge n = e.getNextEdge();
				if (e instanceof ContainsGraphElementClass) {
					e.delete();
				} else {
					e.setThis(graphClass);
				}
				e = n;
			}
			aec.delete();
			currentClass = graphClass;

			// System.out.println("currentClass = " + currentClass + " "
			// + currentClass.getQualifiedName());

		} else if (key.equals("record")) {
			// convert current class to RecordDomain
			assert currentClass != null;
			assert currentClass instanceof VertexClass;
			RecordDomain rd = sg.createRecordDomain();
			rd.setQualifiedName(currentClass.getQualifiedName());
			Edge e = currentClass.getFirstEdge();
			while (e != null) {
				Edge n = e.getNextEdge();
				if (e instanceof ContainsGraphElementClass) {
					sg
							.createContainsDomain(
									(de.uni_koblenz.jgralab.grumlschema.structure.Package) e
											.getThat(), rd);
					e.delete();
				} else if (e instanceof HasAttribute) {
					Attribute att = (Attribute) e.getThat();
					Edge d = att.getFirstHasDomain();
					if (d != null) {
						Domain dom = (Domain) e.getThat();
						HasRecordDomainComponent comp = sg
								.createHasRecordDomainComponent(rd, dom);
						comp.setName(att.getName());
					} else {
						String typeId = attributeType.getMark(att);
						assert typeId != null;
						Domain dom = sg.createStringDomain();
						dom.setQualifiedName(typeId);
						preliminaryVertices.add(dom);
						HasRecordDomainComponent comp = sg
								.createHasRecordDomainComponent(rd, dom);
						recordComponentType.mark(comp, typeId);
						attributeType.removeMark(att);
					}
					att.delete();
				} else {
					System.err.println("Can't handle " + e);
				}
				e = n;
			}
			assert currentClass.getDegree() == 0;
			domainMap.put(rd.getQualifiedName(), rd);
			idMap.put(currentClassId, rd);
			currentRecordDomain = rd;
			currentClass.delete();
			currentClass = null;
			currentClassId = null;

			// System.out
			// .println("currentClass = null, currentRecordDomain = "
			// + rd + " " + rd.getQualifiedName());
		} else if (key.equals("abstract")) {
			assert currentClass != null;
			currentClass.setIsAbstract(true);
		} else {
			throw new XMLStreamException("unexpected stereotype " + key
					+ " at line " + parser.getLocation().getLineNumber());
		}
	}

	private void handleGeneralization(XMLStreamReader parser) {
		String general = parser.getAttributeValue(null, "general");
		Set<String> gens = generalizations.getMark(currentClass);
		if (gens == null) {
			gens = new TreeSet<String>();
			generalizations.mark(currentClass, gens);
		}
		gens.add(general);
	}

	private void handleNestedTypeElement(XMLStreamReader parser, String type)
			throws XMLStreamException {
		assert (currentAttribute != null) || (currentRecordDomain != null);
		String href = parser.getAttributeValue(null, "href");
		assert href != null;
		Domain dom = null;
		if (href.endsWith("#String")) {
			dom = createDomain("String");
		} else if (href.endsWith("#Integer")) {
			dom = createDomain("Integer");
		} else if (href.endsWith("#Boolean")) {
			dom = createDomain("Boolean");
		} else {
			throw new XMLStreamException("unexpected " + type + " with href "
					+ href + " at line " + parser.getLocation().getLineNumber());
		}
		if (currentRecordDomain != null) {
			assert currentRecordDomainComponent != null;
			if (dom != null) {
				Domain d = (Domain) currentRecordDomainComponent.getOmega();
				assert (d instanceof StringDomain)
						&& (d.getQualifiedName() == null)
						&& preliminaryVertices.contains(d);
				currentRecordDomainComponent.setOmega(dom);
				d.delete();
				preliminaryVertices.remove(d);
				recordComponentType.removeMark(currentRecordDomainComponent);
			}
		} else {
			assert currentAttribute != null;
			if (dom != null) {
				sg.createHasDomain(currentAttribute, dom);
				attributeType.removeMark(currentAttribute);
			}
		}
	}

	private void handleEnumerationLiteral(XMLStreamReader parser) {
		String classifier = parser.getAttributeValue(null, "classifier");
		assert classifier != null;
		EnumDomain ed = (EnumDomain) idMap.get(classifier);
		String s = parser.getAttributeValue(null, "name");
		assert s != null;
		s = s.trim();
		assert s.length() > 0;
		ed.getEnumConstants().add(s);
	}

	private void handleOwnedAttribute(XMLStreamReader parser, String xmiId) {
		assert (currentClass != null) || (currentRecordDomain != null);
		String association = parser.getAttributeValue(null, "association");
		if (association == null) {
			String attrName = parser.getAttributeValue(null, "name");
			assert attrName != null;
			attrName = attrName.trim();
			assert attrName.length() > 0;

			if (currentClass != null) {
				// property is an "ordinary" attribute
				Attribute att = sg.createAttribute();
				currentAttribute = att;
				att.setName(attrName);
				sg.createHasAttribute(currentClass, att);

				String typeId = parser.getAttributeValue(null, "type");
				if (typeId != null) {
					attributeType.mark(att, typeId);
				}
			} else {
				// property is a record component
				assert currentRecordDomain != null;
				currentAttribute = null;
				String typeId = parser.getAttributeValue(null, "type");
				currentRecordDomainComponent = null;
				if (typeId != null) {
					Vertex v = (Vertex) idMap.get(typeId);
					if (v != null) {
						assert v instanceof Domain;
						currentRecordDomainComponent = sg
								.createHasRecordDomainComponent(
										currentRecordDomain, (Domain) v);
					} else {
						Domain dom = sg.createStringDomain();
						dom.setQualifiedName(typeId);
						preliminaryVertices.add(dom);
						currentRecordDomainComponent = sg
								.createHasRecordDomainComponent(
										currentRecordDomain, dom);
						recordComponentType.mark(currentRecordDomainComponent,
								typeId);
					}
				} else {
					Domain dom = sg.createStringDomain();
					preliminaryVertices.add(dom);
					currentRecordDomainComponent = sg
							.createHasRecordDomainComponent(
									currentRecordDomain, dom);
				}
				currentRecordDomainComponent.setName(attrName);
			}
		} else {
			assert (currentClass != null) && (currentRecordDomain == null);
			handleAssociatioEnd(parser, xmiId);
		}
	}

	private Vertex handlePrimitiveType(XMLStreamReader parser) {
		String typeName = parser.getAttributeValue(null, "name");
		assert typeName != null;
		typeName = typeName.replaceAll("\\s", "");
		assert typeName.length() > 0;
		Domain dom = createDomain(typeName);
		assert dom != null;
		return dom;
	}

	private Vertex handleEnumeration(XMLStreamReader parser) {
		EnumDomain ed = sg.createEnumDomain();
		de.uni_koblenz.jgralab.grumlschema.structure.Package p = packageStack
				.peek();
		ed.setQualifiedName(getQualifiedName(parser.getAttributeValue(null,
				"name")));
		sg.createContainsDomain(p, ed);
		ed.setEnumConstants(new ArrayList<String>());
		Domain dom = domainMap.get(ed.getQualifiedName());
		if (dom != null) {
			// there was a preliminary vertex for this domain
			// link the edges to the correct one
			assert preliminaryVertices.contains(dom);
			reconnectEdges(dom, ed);
			// delete preliminary vertex
			dom.delete();
			preliminaryVertices.remove(dom);
		}
		domainMap.put(ed.getQualifiedName(), ed);
		return ed;
	}

	private Vertex handleAssociation(XMLStreamReader parser, String xmiId) {
		// create an EdgeClass at first, probably, this has to
		// become an Aggregation or Composition later...
		AttributedElement ae = idMap.get(xmiId);
		EdgeClass ec = null;
		if (ae != null) {
			assert ae instanceof EdgeClass;
			assert preliminaryVertices.contains(ae);
			preliminaryVertices.remove(ae);
			ec = (EdgeClass) ae;
		} else {
			ec = sg.createEdgeClass();
		}
		currentClassId = xmiId;
		currentClass = ec;
		String abs = parser.getAttributeValue(null, "isAbstract");
		ec.setIsAbstract((abs != null) && abs.equals("true"));
		String n = parser.getAttributeValue(null, "name");
		n = (n == null) ? "" : n.trim();
		if (n.length() > 0) {
			n = Character.toUpperCase(n.charAt(0)) + n.substring(1);
		}
		ec.setQualifiedName(getQualifiedName(n));
		sg.createContainsGraphElementClass(packageStack.peek(), ec);

		String memberEnd = parser.getAttributeValue(null, "memberEnd");
		assert memberEnd != null;
		memberEnd = memberEnd.trim().replaceAll("\\s+", " ");
		int p = memberEnd.indexOf(' ');
		String targetEnd = memberEnd.substring(0, p);
		String sourceEnd = memberEnd.substring(p + 1);

		Edge e = (Edge) idMap.get(sourceEnd);
		if (e == null) {
			VertexClass vc = sg.createVertexClass();
			preliminaryVertices.add(vc);
			vc.setQualifiedName("preliminary for source end " + sourceEnd);
			e = sg.createFrom(ec, vc);
			idMap.put(sourceEnd, e);
		}

		e = (Edge) idMap.get(targetEnd);
		if (e != null) {
			assert e.isValid();
			assert e instanceof From;
			From from = (From) e;
			To to = sg.createTo(ec, (VertexClass) from.getOmega());
			to.setMax(from.getMax());
			to.setMin(from.getMin());
			to.setRoleName(from.getRoleName());
			to.setRedefinedRoles(from.getRedefinedRoles());
			if (ownedEnds.contains(from)) {
				ownedEnds.remove(from);
				ownedEnds.add(to);
			}
			if (aggregateEnds.contains(from)) {
				aggregateEnds.remove(from);
				aggregateEnds.add(to);
			}
			e.delete();
			idMap.put(targetEnd, to);
		} else {
			VertexClass vc = sg.createVertexClass();
			preliminaryVertices.add(vc);
			vc.setQualifiedName("preliminary for target end " + targetEnd);
			e = sg.createTo(ec, vc);
			idMap.put(targetEnd, e);
		}

		// System.out.println("currentClass = " + currentClass + " "
		// + currentClass.getQualifiedName());
		// System.out.println("\tsource " + sourceEnd + " -> "
		// + idMap.get(sourceEnd));
		// System.out.println("\ttarget " + targetEnd + " -> "
		// + idMap.get(targetEnd));
		return ec;
	}

	private Vertex handleClass(XMLStreamReader parser, String xmiId) {
		AttributedElement ae = idMap.get(xmiId);
		VertexClass vc = null;
		if (ae != null) {
			assert ae instanceof VertexClass;
			assert preliminaryVertices.contains(ae);
			preliminaryVertices.remove(ae);
			vc = (VertexClass) ae;
		} else {
			vc = sg.createVertexClass();
		}
		currentClassId = xmiId;
		currentClass = vc;
		String abs = parser.getAttributeValue(null, "isAbstract");
		vc.setIsAbstract((abs != null) && abs.equals("true"));
		vc.setQualifiedName(getQualifiedName(parser.getAttributeValue(null,
				"name")));
		sg.createContainsGraphElementClass(packageStack.peek(), vc);

		// System.out.println("currentClass = " + currentClass + " "
		// + currentClass.getQualifiedName());
		return vc;
	}

	private Vertex handlePackage(XMLStreamReader parser) {
		de.uni_koblenz.jgralab.grumlschema.structure.Package pkg = sg
				.createPackage();
		pkg.setQualifiedName(getQualifiedName(parser.getAttributeValue(null,
				"name")));
		sg.createContainsSubPackage(packageStack.peek(), pkg);
		packageStack.push(pkg);
		return pkg;
	}

	private void handleAssociatioEnd(XMLStreamReader parser, String xmiId) {
		String endName = parser.getAttributeValue(null, "name");
		String agg = parser.getAttributeValue(null, "aggregation");
		boolean aggregation = (agg != null) && agg.equals("shared");
		boolean composition = (agg != null) && agg.equals("composite");

		Edge e = (Edge) idMap.get(xmiId);
		if (e == null) {
			// try to find the end's VertexClass
			// if not found, create a preliminary VertexClass
			VertexClass vc = null;
			// we have an "ownedEnd", vertex class id is in "type" attribute
			String typeId = parser.getAttributeValue(null, "type");
			assert typeId != null;
			AttributedElement ae = idMap.get(typeId);
			if (ae != null) {
				// VertexClass found
				assert ae instanceof VertexClass;
				vc = (VertexClass) ae;
			} else {
				// create a preliminary vertex class
				vc = sg.createVertexClass();
				preliminaryVertices.add(vc);
				vc.setQualifiedName(typeId);
				idMap.put(typeId, vc);
			}

			// try to find the end's EdgeClass
			EdgeClass ec = null;
			if (currentClass instanceof EdgeClass) {
				// we have an "ownedEnd", so the end's Edge is the
				// currentClass
				ec = correctAggregationAndComposition((EdgeClass) currentClass,
						aggregation, composition);
				currentClass = ec;
				idMap.put(currentClassId, currentClass);
			} else {
				// we have an ownedAttribute
				// edge class id is in "association"
				String association = parser.getAttributeValue(null,
						"association");
				assert association != null;
				ae = idMap.get(association);
				if (ae != null) {
					// EdgeClass found
					assert ae instanceof EdgeClass;
					ec = correctAggregationAndComposition((EdgeClass) ae,
							aggregation, composition);
				} else {
					// create a preliminary edge class
					ec = composition ? sg.createCompositionClass()
							: aggregation ? sg.createAggregationClass() : sg
									.createEdgeClass();
				}
				preliminaryVertices.add(ec);
				idMap.put(association, ec);
			}

			assert (vc != null) && (ec != null);
			e = sg.createFrom(ec, vc);
		} else {
			EdgeClass ec = (EdgeClass) e.getAlpha();
			String id = null;
			for (Entry<String, AttributedElement> idEntry : idMap.entrySet()) {
				if (idEntry.getValue() == ec) {
					id = idEntry.getKey();
					break;
				}
			}
			assert id != null;
			ec = correctAggregationAndComposition(ec, aggregation, composition);
			idMap.put(id, ec);

			// an ownedEnd of an association or an ownedAttribute of a class
			// with a possibly preliminary vertex class
			VertexClass vc = (VertexClass) e.getOmega();
			if (preliminaryVertices.contains(vc)) {
				String typeId = parser.getAttributeValue(null, "type");
				assert typeId != null;
				AttributedElement ae = idMap.get(typeId);
				if ((ae != null) && !vc.equals(ae)) {
					assert ae instanceof VertexClass;
					e.setOmega((VertexClass) ae);
					vc.delete();
					preliminaryVertices.remove(vc);
				} else if (ae == null) {
					idMap.put(typeId, vc);
				} else {
					throw new RuntimeException(
							"FIXME: You should not get here!");
				}
			}
		}

		assert e != null;
		assert (e instanceof From) || (e instanceof To);
		currentAssociationEnd = e;
		if (aggregation || composition) {
			aggregateEnds.add(e);
		}
		if (currentClass instanceof EdgeClass) {
			ownedEnds.add(e);
		}
		idMap.put(xmiId, e);
		if (e instanceof To) {
			((To) e).setRoleName(endName);
		} else if (e instanceof From) {
			((From) e).setRoleName(endName);
		} else {
			throw new RuntimeException("FIXME! Should never get here.");
		}
		// System.out.println("\t"
		// + (currentClass instanceof EdgeClass ? "ownedEnd"
		// : "ownedAttribute") + " " + endName + " " + xmiId
		// + " (" + e + " " + e.getOmega() + " "
		// + ((VertexClass) e.getOmega()).getQualifiedName() + ")");

	}

	private EdgeClass correctAggregationAndComposition(EdgeClass ec,
			boolean aggregation, boolean composition) {
		if (composition && (ec.getM1Class() != CompositionClass.class)) {
			EdgeClass cls = sg.createCompositionClass();
			cls.setQualifiedName(ec.getQualifiedName());
			cls.setIsAbstract(ec.isIsAbstract());
			reconnectEdges(ec, cls);
			ec.delete();
			if (preliminaryVertices.contains(ec)) {
				preliminaryVertices.remove(ec);
				preliminaryVertices.add(cls);
			}
			return cls;
		}
		if (aggregation && (ec.getM1Class() != AggregationClass.class)) {
			EdgeClass cls = sg.createAggregationClass();
			cls.setQualifiedName(ec.getQualifiedName());
			cls.setIsAbstract(ec.isIsAbstract());
			reconnectEdges(ec, cls);
			ec.delete();
			if (preliminaryVertices.contains(ec)) {
				preliminaryVertices.remove(ec);
				preliminaryVertices.add(cls);
			}
			return cls;
		}
		return ec;
	}

	private void reconnectEdges(Vertex oldVertex, Vertex newVertex) {
		Edge curr = oldVertex.getFirstEdge();
		while (curr != null) {
			Edge next = curr.getNextEdge();
			curr.setThis(newVertex);
			curr = next;
		}
	}

	/**
	 * Creates a Domain vertex corresponding to the specified
	 * <code>typeName</code>.
	 * 
	 * This vertex can also be a preliminary vertex which has to be replaced by
	 * the correct Domain later. In this case, there is no "ContainsDomain"
	 * edge, and the type is "StringDomain".
	 * 
	 * @param typeName
	 * @return
	 */
	private Domain createDomain(String typeName) {
		Domain dom = domainMap.get(typeName);
		if (dom != null) {
			return dom;
		}

		if (typeName.equals("String")) {
			dom = sg.createStringDomain();
		} else if (typeName.equals("Integer")) {
			dom = sg.createIntegerDomain();
		} else if (typeName.equals("Double")) {
			dom = sg.createDoubleDomain();
		} else if (typeName.equals("Long")) {
			dom = sg.createLongDomain();
		} else if (typeName.equals("Boolean")) {
			dom = sg.createBooleanDomain();
		} else if (typeName.startsWith("Map<") && typeName.endsWith(">")) {
			dom = sg.createMapDomain();
			String keyValueDomains = typeName.substring(4,
					typeName.length() - 1);
			char[] c = keyValueDomains.toCharArray();
			// find the delimiting ',' and take into account nested domains
			int p = 0;
			for (int i = 0; i < c.length; ++i) {
				if ((c[i] == ',') && (p == 0)) {
					p = i;
					break;
				}
				if (c[i] == '<') {
					++p;
				} else if (c[i] == '>') {
					--p;
				}
				assert p >= 0;
			}
			assert (p > 0) && (p < c.length - 1);
			String keyDomainName = keyValueDomains.substring(0, p);
			Domain keyDomain = createDomain(keyDomainName);
			assert keyDomain != null;
			String valueDomainName = keyValueDomains.substring(p + 1);
			Domain valueDomain = createDomain(valueDomainName);
			assert valueDomain != null;
			sg.createHasKeyDomain((MapDomain) dom, keyDomain);
			sg.createHasValueDomain((MapDomain) dom, valueDomain);

			// Adds a space between
			typeName = "Map<" + keyDomainName + ", " + valueDomainName + '>';

		} else if (typeName.startsWith("List<") && typeName.endsWith(">")) {
			dom = sg.createListDomain();
			String compTypeName = typeName.substring(5, typeName.length() - 1);
			Domain compDomain = createDomain(compTypeName);
			assert compDomain != null;
			sg.createHasBaseDomain((CollectionDomain) dom, compDomain);
		} else if (typeName.startsWith("Set<") && typeName.endsWith(">")) {
			dom = sg.createSetDomain();
			String compTypeName = typeName.substring(4, typeName.length() - 1);
			Domain compDomain = createDomain(compTypeName);
			assert compDomain != null;
			sg.createHasBaseDomain((CollectionDomain) dom, compDomain);
		}
		if (dom != null) {
			sg.createContainsDomain(packageStack.get(0), dom);
		} else {
			// there must exist a named domain (Enum or Record)
			// but this was not yet created in the graph
			// create preliminary domain vertex which will
			// later be re-linked and deleted
			dom = sg.createStringDomain();
			preliminaryVertices.add(dom);
		}

		assert dom != null;
		dom.setQualifiedName(typeName);
		domainMap.put(typeName, dom);
		return dom;
	}

	/**
	 * Returns the qualified name for the simple name <code>simpleName</code>.
	 * The qualified name consists the (already qualified) name of the package
	 * on top of the package stack and the name <code>simpleName</code>,
	 * separated by a dot. If the top package is the default package, the name
	 * <code>simpleName</code> is already the qualified name. If the package
	 * stack is empty
	 * 
	 * @param simpleName
	 *            a simple name of a class or package
	 * @return the qualified name for the simple name
	 */
	private String getQualifiedName(String simpleName) {
		assert simpleName != null;
		simpleName = simpleName.trim();
		Package p = packageStack.peek();
		assert p != null;
		if (p.getQualifiedName().equals("")) {
			return simpleName;
		} else {
			return p.getQualifiedName() + "." + simpleName;
		}
	}

	public void setUseFromRole(boolean useFromRole) {
		this.useFromRole = useFromRole;
	}

	private boolean isUseFromRole() {
		return useFromRole;
	}

	public void setRemoveUnusedDomains(boolean removeUnusedDomains) {
		this.removeUnusedDomains = removeUnusedDomains;
	}

	private boolean isRemoveUnusedDomains() {
		return removeUnusedDomains;
	}

	public void setUseNavigability(boolean useNavigability) {
		this.useNavigability = useNavigability;
	}

	private boolean isUseNavigability() {
		return useNavigability;
	}

	public SchemaGraph getSchemaGraph() {
		return sg;
	}

	public void setSuppressOutput(boolean suppressOutput) {
		this.suppressOutput = suppressOutput;
	}

	public boolean isWriteSchemaGraph() {
		return writeSchemaGraph;
	}

	public void setWriteSchemaGraph(boolean writeSchemaGraph) {
		this.writeSchemaGraph = writeSchemaGraph;
	}

	public boolean isWriteDebug() {
		return writeDebug;
	}

	public void setWriteDebug(boolean writeDebug) {
		this.writeDebug = writeDebug;
	}

	public String getFilenameSchema() {
		return filenameSchema;
	}

	public void setFilenameSchema(String filenameSchema) {
		this.filenameSchema = filenameSchema;
	}

	public String getFilenameSchemaGraph() {
		return filenameSchemaGraph;
	}

	public void setFilenameSchemaGraph(String filenameSchemaGraph) {
		this.filenameSchemaGraph = filenameSchemaGraph;
	}

	public String getFilenameDebug() {
		return filenameDebug;
	}

	public void setFilenameDebug(String filenameDebug) {
		this.filenameDebug = filenameDebug;
	}
}
