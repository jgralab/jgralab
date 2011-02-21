/*
 * JGraLab - The Java Graph Laboratory
 * 
 * Copyright (C) 2006-2011 Institute for Software Technology
 *                         University of Koblenz-Landau, Germany
 *                         ist@uni-koblenz.de
 * 
 * For bug reports, documentation and further information, visit
 * 
 *                         http://jgralab.uni-koblenz.de
 * 
 * This program is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as published by the
 * Free Software Foundation; either version 3 of the License, or (at your
 * option) any later version.
 * 
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General
 * Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, see <http://www.gnu.org/licenses>.
 * 
 * Additional permission under GNU GPL version 3 section 7
 * 
 * If you modify this Program, or any covered work, by linking or combining
 * it with Eclipse (or a modified version of that program or an Eclipse
 * plugin), containing parts covered by the terms of the Eclipse Public
 * License (EPL), the licensors of this Program grant you additional
 * permission to convey the resulting work.  Corresponding Source for a
 * non-source form of such a combination shall include the source code for
 * the parts of JGraLab used as well as that of the covered work.
 */

package de.uni_koblenz.jgralab.utilities.tg2gxl;

//import gnu.getopt.Getopt;
//import gnu.getopt.LongOpt;

import java.io.PrintStream;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;

import de.uni_koblenz.ist.utilities.option_handler.OptionHandler;
import de.uni_koblenz.jgralab.AttributedElement;
import de.uni_koblenz.jgralab.Edge;
import de.uni_koblenz.jgralab.GraphIOException;
import de.uni_koblenz.jgralab.JGraLab;
import de.uni_koblenz.jgralab.Vertex;
import de.uni_koblenz.jgralab.WorkInProgress;
import de.uni_koblenz.jgralab.grumlschema.domains.HasRecordDomainComponent;
import de.uni_koblenz.jgralab.grumlschema.structure.Schema;
import de.uni_koblenz.jgralab.schema.Attribute;
import de.uni_koblenz.jgralab.schema.AttributedElementClass;
import de.uni_koblenz.jgralab.schema.BooleanDomain;
import de.uni_koblenz.jgralab.schema.Domain;
import de.uni_koblenz.jgralab.schema.DoubleDomain;
import de.uni_koblenz.jgralab.schema.EnumDomain;
import de.uni_koblenz.jgralab.schema.IntegerDomain;
import de.uni_koblenz.jgralab.schema.ListDomain;
import de.uni_koblenz.jgralab.schema.LongDomain;
import de.uni_koblenz.jgralab.schema.RecordDomain;
import de.uni_koblenz.jgralab.schema.RecordDomain.RecordComponent;
import de.uni_koblenz.jgralab.schema.SetDomain;
import de.uni_koblenz.jgralab.utilities.tg2schemagraph.Schema2SchemaGraph;
import de.uni_koblenz.jgralab.utilities.tg2whatever.Tg2Whatever;

/**
 * This class allows the transformation of any <code>Graph</code> to GXL-like
 * code. This GXL-like code is valid GXL, if and only if:
 * 
 * 1. The <code>Graph</code> has at most and at least ONE
 * <code>GraphClass</code>.
 * 
 * 2. The <code>Schema</code> contains NOT ANY <code>RecordDomain</code>.
 * 
 */
@WorkInProgress(description = "untested, suspected problems with record/enum domains and schema extraction")
public class Tg2GXL extends Tg2Whatever {

	// it might be suggestive to copy << gxl-1.0.dtd >> to a locale destination.
	private final String gxlDtd = "http://www.gupro.de/GXL/gxl-1.0.dtd";
	private final String xlink = "http://www.w3.org/1999/xlink";
	private final String gxlMetaSchema = "http://www.gupro.de/GXL/gxl-1.0.gxl";

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
	@Override
	protected void graphStart(PrintStream out) {

		out.println("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
		out.println("<!DOCTYPE gxl SYSTEM \"" + gxlDtd + "\">");
		out.println("<gxl xmlns:xlink=\"" + xlink + "\">");

		if (printSchema) {

			out.println("<graph id=\""
					+ uniqueGraphClassName
					+ "Graph\" edgeids=\" true\" edgemode=\" directed\" hypergraph=\" false\">");
			out.println("<type xlink:href=\"" + gxlMetaSchema
					+ "#gxl-1.0\" xlink:type=\" simple\"/>");
		} else {
			out.println("<graph id=\""
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
	@Override
	protected void printVertex(PrintStream out, Vertex v) {
		AttributedElementClass elemClass = v.getAttributedElementClass();
		if (printSchema && !(v instanceof Schema)) {

			if (v instanceof de.uni_koblenz.jgralab.grumlschema.structure.AttributedElementClass) {
				out.println("<node id=\"" + v.getAttribute("qualifiedName")
						+ "\">");
			} else {
				out.println("<node id=\"v:" + v.getId() + "\">");
			}
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
			out.println("<type xlink:href=\"" + schemaGraphOutputName + "#"
					+ elemClass.getQualifiedName()
					+ "\" xlink:type=\" simple\"/>");

			// print attributes
			if (elemClass.getAttributeCount() > 0) {
				printAttributes(out, v);
			}
			out.println("</node>");
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
			if ((e0 == e) && (e0.isNormal() == e.isNormal())) {
				return i;
			}
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
	@Override
	protected void printEdge(PrintStream out, Edge e) {
		AttributedElementClass elemClass = e.getAttributedElementClass();

		if (printSchema
				&& !(e instanceof de.uni_koblenz.jgralab.grumlschema.structure.DefinesGraphClass)
				&& !(e instanceof HasRecordDomainComponent)) {
			String thisVertex = "v:" + e.getThis().getId();
			String thatVertex = "v:" + e.getThat().getId();

			if (e.getThis() instanceof de.uni_koblenz.jgralab.grumlschema.structure.AttributedElementClass) {
				thisVertex = ((de.uni_koblenz.jgralab.grumlschema.structure.AttributedElementClass) e
						.getThis()).get_qualifiedName();
			}
			if (e.getThat() instanceof de.uni_koblenz.jgralab.grumlschema.structure.AttributedElementClass) {
				thatVertex = ((de.uni_koblenz.jgralab.grumlschema.structure.AttributedElementClass) e
						.getThat()).get_qualifiedName();
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
					+ elemClass.getQualifiedName()
					+ "\" xlink:type=\" simple\"/>");
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
	@Override
	protected void graphEnd(PrintStream out) {
		out.println("</graph>");
		out.println("</gxl>");
	}

	/**
	 * prints the \<attr\> ... \</attr\> tag for each attribute of
	 * <code>elem</code>. The methods <code>printComposite(...)</code> and
	 * <code>printValue(...)</code> are responsible for the attributes interior.
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
			val = elem.getAttribute(attr.getName());
			Domain dom = attr.getDomain();
			printComposite(out, dom, val);
			out.println("</attr>");
		}
	}

	/**
	 * each attributes domain is submitted to this method with the attributes
	 * value. While the domain is a <code>CompositeDomain</code>, this method is
	 * called recursively. The break condition is the appearance of a
	 * <Code>BasicDomain</Code>.
	 * 
	 * @param out
	 *            the output stream
	 * @param dom
	 *            a JGralab domain
	 * @param val
	 *            the value of the attribute having this domain
	 */
	private void printComposite(PrintStream out, Domain dom, Object val) {
		if (!dom.isComposite()) {
			printValue(out, dom, val);
		} else {
			if (dom instanceof SetDomain) {
				out.println("<Set>");
				if (val != null) {
					for (Object o : (Set<?>) val) {
						printComposite(out, ((SetDomain) dom).getBaseDomain(),
								o);
					}
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
				Collection<RecordComponent> components = ((RecordDomain) dom)
						.getComponents();
				for (RecordComponent component : components) {
					out.println("<Tup>");
					out.println("<String>");
					out.println("" + stringQuote(component.getName()));
					out.println("</String>");
					try {
						printComposite(out, component.getDomain(),
								val.getClass().getField(component.getName())
										.get(val));
					} catch (Exception e) {

					}
					out.println("</Tup>");
				}
				out.println("</Tup>");
			}
		}
	}

	/**
	 * prints the value <code>val</code> of a basic domain <code>dom</code> to
	 * the print stream <code>out</code>. this method acts as the break
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
			if (val instanceof Float) {
				val = ((Float) val).doubleValue();
			}
			if (val instanceof Integer) {
				val = ((Integer) val).longValue();
			}
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
			out.println("" + attrValue);
			out.println("</String>");
		}
		if ((dom instanceof IntegerDomain) || (dom instanceof LongDomain)) {
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
		converter.convert();
	}

	/**
	 * overrides the <code>printGraph()</code> method in Tg2Whatever. The
	 * <code>boolean printSchema</code> variable is used in every method,
	 * invoked by <code>super.printGraph()</code> to decide, if either the M1 or
	 * the M2 graph is printed.
	 */
	@Override
	public void convert() {
		printSchema = false;
		setOutputFile(graphOutputName);
		uniqueGraphClassName = graph.getSchema().getGraphClass()
				.getQualifiedName();
		super.convert();
		setOutputFile(schemaGraphOutputName);
		setGraph(new Schema2SchemaGraph()
				.convert2SchemaGraph(graph.getSchema()));
		printSchema = true;
		super.convert();

	}

	/**
	 * processes the command-line parameter set valid parameters are: -g --graph
	 * points at the .tg file in which the graph is located, that should be
	 * processed. -o --output point at the .gxl output file, where the converted
	 * graph will be stored. -h --help print usage information on System.out
	 */
	@Override
	protected void getOptions(String[] args) {
		CommandLine comLine = processCommandLineOptions(args);
		assert comLine != null;
		String graphName = null;
		if (comLine.hasOption("g")) {
			try {
				graphName = comLine.getOptionValue("g");

				setGraph(graphName);

			} catch (GraphIOException e) {
				System.err.println("Couldn't load graph in file '" + graphName
						+ "': " + e.getMessage());
				if (e.getCause() != null) {
					e.getCause().printStackTrace();
				}
				System.exit(1);
			}
		}
		if (comLine.hasOption("o")) {
			graphOutputName = comLine.getOptionValue("o");
			schemaGraphOutputName = graphOutputName.substring(0,
					graphOutputName.length() - 4) + "Schema.gxl";
		}
		if (outputName == null) {
			outputName = "";
		}
	}

	protected static CommandLine processCommandlineOptions(String[] args) {
		String toolString = "java " + Tg2GXL.class.getName();
		String versionString = JGraLab.getInfo(false);
		OptionHandler oh = new OptionHandler(toolString, versionString);

		Option graph = new Option("g", "graph", true,
				"(required): the graph to be converted");
		graph.setRequired(true);
		graph.setArgName("file");
		oh.addOption(graph);

		Option output = new Option("o", "output", true,
				"(required): the output file name, or empty for stdout");
		output.setRequired(true);
		output.setArgName("file");
		oh.addOption(output);

		return oh.parse(args);
	}

	/**
	 * adds an escape sequence to special characters in a string
	 * 
	 */
	@Override
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
				if ((ch < ' ') || (ch > '\u007F')) {
					sb.append("\\\\u");
					String code = "000" + Integer.toHexString(ch);
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
