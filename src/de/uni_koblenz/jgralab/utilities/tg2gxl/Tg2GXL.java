/*
 * JGraLab - The Java graph laboratory
 * (c) 2006-2008 Institute for Software Technology
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

package de.uni_koblenz.jgralab.utilities.tg2gxl;

import gnu.getopt.Getopt;
import gnu.getopt.LongOpt;

import java.io.PrintStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import de.uni_koblenz.jgralab.Attribute;
import de.uni_koblenz.jgralab.AttributedElement;
import de.uni_koblenz.jgralab.Edge;
import de.uni_koblenz.jgralab.GraphIOException;
import de.uni_koblenz.jgralab.Vertex;
import de.uni_koblenz.jgralab.grumlschema.DefinesGraphClass;
import de.uni_koblenz.jgralab.grumlschema.HasRecordDomainComponent;
import de.uni_koblenz.jgralab.grumlschema.Schema;
import de.uni_koblenz.jgralab.schema.AttributedElementClass;
import de.uni_koblenz.jgralab.schema.BooleanDomain;
import de.uni_koblenz.jgralab.schema.Domain;
import de.uni_koblenz.jgralab.schema.DoubleDomain;
import de.uni_koblenz.jgralab.schema.EnumDomain;
import de.uni_koblenz.jgralab.schema.IntDomain;
import de.uni_koblenz.jgralab.schema.ListDomain;
import de.uni_koblenz.jgralab.schema.LongDomain;
import de.uni_koblenz.jgralab.schema.RecordDomain;
import de.uni_koblenz.jgralab.schema.SetDomain;
import de.uni_koblenz.jgralab.utilities.tg2schemagraph.Tg2SchemaGraph;
import de.uni_koblenz.jgralab.utilities.tg2whatever.Tg2Whatever;

/**
 * This class allows the transformation of any <code>Graph</code> to GXL-like
 * code. This GXL-like code is valid GXL, if and only if:
 * 
 * 1. The <code>Graph</code> has at most and at least ONE
 * <code>GraphClass</code>. (command-line option -c / --combine)
 * 
 * 2. The <code>Schema</code> contains NOT ANY <code>RecordDomain</code>.
 * 
 */
public class Tg2GXL extends Tg2Whatever {

	// it might be suggestive to copy << gxl-1.0.dtd >> to a locale destination.
	private String gxlDtd = "http://www.gupro.de/GXL/gxl-1.0.dtd";
	private String xlink = "http://www.w3.org/1999/xlink";
	private String gxlMetaSchema = "http://www.gupro.de/GXL/gxl-1.0.gxl";

	private String graphOutputName;
	private String schemaGraphOutputName;

	private String uniqueGraphClassName;
	private Boolean printSchema;

	private HashMap<String, String> grUML2GXL;

	/**
	 * Maps the names of the M2-graph classes to the corresponding names in the
	 * GXL-Metaschema
	 */
	private void initgrUML2GXLMap() {
		if (grUML2GXL == null) {
			grUML2GXL = new HashMap<String, String>();

			grUML2GXL.put("GraphClassM2", "GraphClass");
			grUML2GXL.put("ContainsGraphElementClassM2", "contains");
			grUML2GXL.put("IsSubGraphClassOfM2", "isA");

			grUML2GXL.put("VertexClassM2", "NodeClass");
			grUML2GXL.put("IsSubVertexClassOfM2", "isA");

			grUML2GXL.put("EdgeClassM2", "EdgeClass");
			grUML2GXL.put("AggregationClassM2", "AggregationClass");

			grUML2GXL.put("CompositionClassM2", "CompositionClass");
			grUML2GXL.put("IsSubEdgeClassOfM2", "isA");
			grUML2GXL.put("ToM2", "to");
			grUML2GXL.put("FromM2", "from");

			grUML2GXL.put("AttributeM2", "AttributeClass");
			grUML2GXL.put("HasAttributeM2", "hasAttribute");
			grUML2GXL.put("HasDomainM2", "hasDomain");

			grUML2GXL.put("BooleanDomainM2", "domainBoolean");
			grUML2GXL.put("DoubleDomainM2", "domainFloat");
			grUML2GXL.put("IntDomainM2", "domainInt");
			grUML2GXL.put("LongDomainM2", "domainInt");
			grUML2GXL.put("StringDomainM2", "domainString");
			grUML2GXL.put("ObjectDomainM2", "domainString");
			grUML2GXL.put("EnumDomainM2", "domainEnum");

			grUML2GXL.put("ListDomainM2", "domainList");
			grUML2GXL.put("SetDomainM2", "domainSet");
			grUML2GXL.put("HasBaseDomainM2", "hasComponent");
			grUML2GXL.put("RecordDomainM2", "(notSpecified)");
			grUML2GXL.put("HasRecordDomainComponentM2", "(notSpecified)");
		}

	}

	/**
	 * starts the graph. The M1 graph refers to its corresponding M2-graph
	 * graphclass object. The M2 graph refers to the GXL-Metaschema. The name of
	 * the M2-graph id does not matter anything, the name schemaName+"Graph" was
	 * chosen for comprehensibility only.
	 * 
	 * @param out
	 *            the output stream
	 */
	protected void graphStart(PrintStream out) {

		out.println("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
		out.println("<!DOCTYPE gxl SYSTEM \"" + gxlDtd + "\">");
		out.println("<gxl xmlns:xlink=\"" + xlink + "\">");

		if (printSchema) {

			out
					.println("<graph id=\""
							+ uniqueGraphClassName
							+ "Graph\" edgeids=\" true\" edgemode=\" directed\" hypergraph=\" false\">");
			out.println("<type xlink:href=\"" + gxlMetaSchema
					+ "#gxl-1.0\" xlink:type=\" simple\"/>");
		} else {
			out
					.println("<graph id=\""
							+ graph.getId()
							+ "\" edgeids=\" true\" edgemode=\" directed\" hypergraph=\" false\">");
			out.println("<type xlink:href=\"" + schemaGraphOutputName + "#"
					+ graph.getGraphClass().getQualifiedName()
					+ "\" xlink:type=\" simple\"/>");
		}
	}

	/**
	 * prints a vertex. All M1 vertices are identified by there Id and refer to
	 * the <code>AttributedElementClassM2</code> object, identified by its
	 * (unique) name. That is why M2 vertices are divided in two groups: 1.
	 * <code>v instanceof AttributedElementClassM2</code> 2.
	 * <code>!(v instanceof AttributedElementClassM2)</code> For 1. the vertex
	 * name identifies id and for 2. the vertex Id identifies it. Anyway, the
	 * <code>SchemaM2</code> vertex is ignored, as it has no correspondence in
	 * the GXL-Metaschema.
	 * 
	 * 
	 * @param out
	 *            the output stream
	 * @param v
	 *            the processed vertex
	 */
	protected void printVertex(PrintStream out, Vertex v) {
		AttributedElementClass elemClass = v.getAttributedElementClass();

		try {
			if (printSchema && !(v instanceof Schema)) {

				if (v instanceof de.uni_koblenz.jgralab.grumlschema.AttributedElementClass)
					out.println("<node id=\"" + v.getAttribute("name") + "\">");
				else
					out.println("<node id=\"v:" + v.getId() + "\">");
				out.println("<type xlink:href=\"" + gxlMetaSchema + "#"
						+ grUML2GXL.get(elemClass.getQualifiedName())
						+ "\" xlink:type=\" simple\"/>");
				// print attributes
				if (elemClass.getAttributeCount() > 0) {
					printAttributes(out, v);
				}
				out.println("</node>");
			}

			else if (!printSchema) {
				out.println("<node id=\"v:" + v.getId() + "\">");
				out.println("<type xlink:href=\"" + schemaGraphOutputName
						+ "#" + elemClass.getQualifiedName()
						+ "\" xlink:type=\" simple\"/>");

				// print attributes
				if (elemClass.getAttributeCount() > 0) {
					printAttributes(out, v);
				}
				out.println("</node>");
			}
		} catch (NoSuchFieldException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * @param e
	 *            an edge
	 * @param v
	 *            a vertex
	 * @return the incidence number of an <code>Edge e</code> at
	 *         <code>Vertex v</code>
	 */
	protected int getEdgeIncidence(Edge e, Vertex v) {
		int i = 0;
		for (Edge e0 : v.incidences()) {
			if ((e0 == e) && (e0.isNormal() == e.isNormal()))
				return i;
			i++;
		}
		return -1;
	}

	/**
	 * prints an edge object. For the M1 graph, the incidence numbers of the
	 * edge get printed, too. For the M2 graph, the (in GXL) undefined edges
	 * <code>ContainsGraphClassM2</code> and
	 * <code>HasRecordDomainComponentM2</code> are ignored anyway.
	 * 
	 * @param out
	 *            the output stream
	 * @param e
	 *            the processed edge
	 */
	protected void printEdge(PrintStream out, Edge e) {
		AttributedElementClass elemClass = e.getAttributedElementClass();

		if (printSchema && !(e instanceof DefinesGraphClass)
				&& !(e instanceof HasRecordDomainComponent)) {
			String thisVertex = "v:" + e.getThis().getId();
			String thatVertex = "v:" + e.getThat().getId();

			try {
				if (e.getThis() instanceof de.uni_koblenz.jgralab.grumlschema.AttributedElementClass)
					thisVertex = "" + e.getThis().getAttribute("name");
				if (e.getThat() instanceof de.uni_koblenz.jgralab.grumlschema.AttributedElementClass)
					thatVertex = "" + e.getThat().getAttribute("name");
			} catch (NoSuchFieldException ex) {
				ex.printStackTrace();
			}

			out.println("<edge id=\"e:" + e.getId() + "\" to=\"" + thatVertex
					+ "\" from=\"" + thisVertex + "\">");
			out.println("<type xlink:href=\"" + gxlMetaSchema + "#"
					+ grUML2GXL.get(elemClass.getQualifiedName())
					+ "\" xlink:type=\" simple\"/>");

			// printAttributes
			if (elemClass.getAttributeCount() > 0) {
				printAttributes(out, e);
			}
			out.println("</edge>");
		} else if (!printSchema) {
			int toOrder = getEdgeIncidence(e.getReversedEdge(), e.getThat());
			int fromOrder = getEdgeIncidence(e, e.getThis());
			out.println("<edge id=\"e:" + e.getId() + "\" to=\"v:"
					+ e.getThat().getId() + "\" from=\"v:"
					+ e.getThis().getId() + "\" toorder=\" " + toOrder
					+ "\" fromorder=\" " + fromOrder + "\">");
			out.println("<type xlink:href=\"" + schemaGraphOutputName + "#"
					+ elemClass.getQualifiedName() + "\" xlink:type=\" simple\"/>");
			// printAttributes
			if (elemClass.getAttributeCount() > 0) {
				printAttributes(out, e);
			}
			out.println("</edge>");
		}
	}

	/**
	 * closes the tags opened in <code>graphStart(PrintStream out)</code>.
	 * 
	 * @param out
	 *            the output stream
	 */
	protected void graphEnd(PrintStream out) {
		out.println("</graph>");
		out.println("</gxl>");
	}

	/**
	 * prints the \<attr\> ... \</attr\> tag for each attribute of
	 * <code>elem</code>. The methods <code>printComposite(...)</code> and
	 * <code>printValue(...)</code> are responsible for the attributes
	 * interior.
	 * 
	 * @param out
	 *            the output stream
	 * @param elem
	 *            the <code>AttributedElement</code> owning at least one
	 *            <code>Attribute</code>
	 */

	private void printAttributes(PrintStream out, AttributedElement elem) {

		for (Attribute attr : elem.getAttributedElementClass()
				.getAttributeList()) {

			out.println("<attr name=\"" + attr.getName() + "\">");

			Object val = null;
			try {
				val = elem.getAttribute(attr.getName());
			} catch (NoSuchFieldException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			Domain dom = attr.getDomain();
			printComposite(out, dom, val);
			out.println("</attr>");

		}
	}

	/**
	 * each attributes domain is submitted to this method with the attributes
	 * value. While the domain is a <code>CompositeDomain</code>, this method
	 * is called recursively. The break condition is the appearance of a
	 * <Code>BasicDomain</Code>.
	 * 
	 * @param out
	 *            the output stream
	 * @param dom
	 *            a JGralab domain
	 * @param val
	 *            the value of the attribute having this domain
	 */
	@SuppressWarnings("unchecked")
	private void printComposite(PrintStream out, Domain dom, Object val) {
		if (!dom.isComposite())
			printValue(out, dom, val);
		else {
			if (dom instanceof SetDomain) {
				out.println("<Set>");

				for (Object o : (Set<?>) val) {
					printComposite(out, ((SetDomain) dom).getBaseDomain(), o);
				}
				out.println("</Set>");
			}
			if (dom instanceof ListDomain) {
				out.println("<List>");
				for (Object o : (List<?>) val) {
					printComposite(out, ((ListDomain) dom).getBaseDomain(), o);
				}
				out.println("</List>");
			}
			if (dom instanceof RecordDomain) {
				out.println("<Tup>");
				out.println("<String>");
				out.println("" + ((RecordDomain) dom).getQualifiedName());
				out.println("</String>");
				Map<String, Domain> components = ((RecordDomain) dom)
						.getComponents();
				for (Map.Entry<String, Domain> component : components
						.entrySet()) {
					out.println("<Tup>");
					out.println("<String>");
					out.println(""
							+ stringQuote((String) component.getKey()));
					out.println("</String>");
					try {
						printComposite(out, component.getValue(), val
								.getClass().getField(component.getKey()).get(
										val));
					} catch (Exception e) {

					}
					out.println("</Tup>");
				}
				out.println("</Tup>");
			}
		}
	}

	/**
	 * prints the value <code>val</code> of a basic domain <code>dom</code>
	 * to the print stream <code>out</code>. this method acts as the break
	 * condition for the recursive function
	 * <code>printComposite(PrintStram out, Domain dom, Object val)</code>
	 * 
	 * @param out
	 *            the output stream
	 * @param dom
	 *            a JGralab domain
	 * @param val
	 *            the value of the attribute having this Domain
	 */
	private void printValue(PrintStream out, Domain dom, Object val) {
		String attrValue = "null";

		if (val != null) {
			if (val instanceof Double)
				val = Float.parseFloat(val.toString());
			if (val instanceof Long)
				val = Integer.parseInt(val.toString());
			attrValue = stringQuote(val.toString());
		}

		if (dom instanceof BooleanDomain) {
			out.println("<Bool>");
			out.println("" + attrValue);
			out.println("</Bool>");
		}
		if (dom instanceof DoubleDomain) {
			out.println("<Float>");
			out.println("" + attrValue);
			out.println("</Float>");
		}
		if (dom instanceof EnumDomain) {
			out.println("<String>");
			out.println("" + stringQuote(val.toString()));
			out.println("</String>");
		}
		if (dom instanceof IntDomain || dom instanceof LongDomain) {
			out.println("<Int>");
			out.println("" + attrValue);
			out.println("</Int>");
		}
	}

	/**
	 * You can launch this tool from the command-line.
	 * 
	 * i.e. java Tg2GXL -g /myTg/myGraph.tg -o /myGxl/myGraph.gxl -c
	 * 
	 * @param args
	 *            the command-line option set processed by
	 *            <code>getOptions(String[] args)</code>
	 */
	public static void main(String[] args) {
		Tg2GXL converter = new Tg2GXL();
		converter.getOptions(args);
		converter.initgrUML2GXLMap();
		converter.printGraph();
	}

	/**
	 * overrides the <code>printGraph()</code> method in Tg2Whatever. The
	 * <code>boolean printSchema</code> variable is used in every method,
	 * invoked by <code>super.printGraph()</code> to decide, if either the M1
	 * or the M2 graph is printed.
	 */
	public void printGraph() {
		printSchema = false;
		setOutputFile(graphOutputName);
		uniqueGraphClassName = graph.getSchema().getQualifiedName();
		super.printGraph();
		setOutputFile(schemaGraphOutputName);
		setGraph(new Tg2SchemaGraph(graph.getSchema()).getSchemaGraph());
		printSchema = true;
		super.printGraph();

	}

	/**
	 * processes the command-line parameter set valid parameters are: -g --graph
	 * points at the .tg file in which the graph is located, that should be
	 * processed. -o --output point at the .gxl output file, where the converted
	 * graph will be stored. -h --help print usage information on System.out
	 */
	protected void getOptions(String[] args) {
		LongOpt[] longOptions = new LongOpt[3];

		int c = 0;
		longOptions[c++] = new LongOpt("graph", LongOpt.REQUIRED_ARGUMENT,
				null, 'g');
		longOptions[c++] = new LongOpt("output", LongOpt.REQUIRED_ARGUMENT,
				null, 'o');

		longOptions[c++] = new LongOpt("help", LongOpt.NO_ARGUMENT, null, 'h');

		Getopt g = new Getopt("Tg2Dot", args, "g:o:h", longOptions);
		c = g.getopt();
		String graphName = null;
		while (c >= 0) {
			switch (c) {
			case 'g':
				try {
					graphName = g.getOptarg();

					setGraph(graphName);

				} catch (GraphIOException e) {
					System.err.println("Couldn't load graph in file '"
							+ graphName + "': " + e.getMessage());
					if (e.getCause() != null) {
						e.getCause().printStackTrace();
					}
					System.exit(1);
				}
				break;
			case 'o':
				graphOutputName = g.getOptarg();
				schemaGraphOutputName = graphOutputName.substring(0,
						graphOutputName.length() - 4)
						+ "Schema.gxl";
				if (graphOutputName == null) {
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
			System.out.println("Missing option");
			// usage(1);
		}
		if (outputName == null) {
			outputName = "";
		}
	}

	/**
	 * prints usage information on System.out if requested by command-line
	 * parameter -h or if the command-line parameter set is malformed.
	 */
	protected void usage(int exitCode) {
		System.err.println("Usage: Tg2GXL -g graphFileName [options]");
		System.err
				.println("The schema classes of the graph must be reachable via CLASSPATH.");
		System.err.println("Options are:");
		System.err
				.println("-g graphFileName   (--graph)     the graph to be converted");
		System.err
				.println("-o outputFileName  (--output)    the output file name, or empty for stdout");
		System.err
				.println("-c                 (--combine)     if set, only one graphclass will be printed.");
		System.err
				.println("                                 This graphclass is an unification of all graphclasses in the schema.");

		System.err
				.println("-h                 (--help)      prints usage information");

		System.exit(exitCode);
	}

	/**
	 * adds an escape sequence to special characters in a string
	 * 
	 */
	protected String stringQuote(String s) {
		StringBuffer sb = new StringBuffer();
		for (char ch : s.toCharArray()) {
			switch (ch) {
			case '\\':
				sb.append("\\\\");
				break;
			case '<':
				sb.append("\\<");
				break;
			case '>':
				sb.append("\\>");
				break;
			case '{':
				sb.append("\\{");
				break;
			case '}':
				sb.append("\\}");
				break;
			case '"':
				sb.append("&quot;");
				break;
			case '|':
				sb.append("\\|");
				break;
			case '\n':
				sb.append("\\\\n");
				break;
			case '\r':
				sb.append("\\\\r");
				break;
			case '\t':
				sb.append("\\\\t");
				break;
			default:
				if (ch < ' ' || ch > '\u007F') {
					sb.append("\\\\u");
					String code = ("000" + Integer.toHexString(ch));
					sb.append(code.substring(code.length() - 4, code.length()));
				} else {
					sb.append(ch);
				}
				break;
			}
		}
		return sb.toString();
	}
}
