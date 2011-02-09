/*
 * JGraLab - The Java Graph Laboratory
 * 
 * Copyright (C) 2006-2010 Institute for Software Technology
 *                         University of Koblenz-Landau, Germany
 *                         ist@uni-koblenz.de
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
package de.uni_koblenz.jgralab.utilities.tg2dot.dot;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

/**
 * DotWriter creates and writes DOT-files. It includes several methods to create
 * a graph, groups, general attribute lists, subgraphs, clusters, nodes and
 * edges.
 * 
 * @author ist@uni-koblenz.des
 */
public class DotWriter {

	/**
	 * String for enclosing a value.
	 */
	private static final String QUOTATIONMARKS = "\"";

	/**
	 * Char for spacing.
	 */
	private static final char SPACE = ' ';

	/**
	 * A tabluator string.
	 */
	private static final String TABULATOR = "  ";

	public static final Set<String> allowedDotEdgeAttributes;

	public static final Set<String> allowedDotNodeAttributes;

	/**
	 * Output PrintWriter created from a given file or a PrintStream.
	 */
	private PrintWriter stream;

	/**
	 * Status variable for the current nestedDepth at the current position. Is
	 * only implicitly initialized and therefore 0 by default.
	 */
	private int nestedDepth;

	/**
	 * Creates a DotWriter from the given file name and creates / overwrites
	 * this file at the destination.
	 * 
	 * @param filename
	 *            Destination file name of the output.
	 * @throws FileNotFoundException
	 */
	public DotWriter(String filename) throws FileNotFoundException {
		stream = new PrintWriter(filename);
	}

	/**
	 * Creates a DotWriter from the given File and creates / overwrites this
	 * file at the destination
	 * 
	 * @param file
	 *            Destination File of the output.
	 * @throws FileNotFoundException
	 *             Occurs in case of a not writable or uncreateable file.
	 */
	public DotWriter(File file) throws FileNotFoundException {
		stream = new PrintWriter(file);
	}

	/**
	 * Creates a DotWriter from the given stream.
	 * 
	 * @param out
	 *            Provided stream.
	 */
	public DotWriter(PrintStream out) {
		stream = new PrintWriter(out);
	}

	/**
	 * DON'T CREATE UNNAMED GRAPHS! Dot can handle them, but dotty cannot.
	 * Despite of that, giving the graph a name is not a bad idea anyway.
	 * 
	 * Starts a unnamed DOT-graph of the specified GraphType. The nested depth
	 * is increased by one.
	 * 
	 * @param type
	 *            Indicates which type of DOT-graph should be written.
	 */
	@Deprecated
	public void startGraph(GraphType type) {
		startElement();
		stream.write(type.name);
		startAbstractGroup();
	}

	/**
	 * Starts a named DOT-graph of the specified GraphType. The nested depth is
	 * increased by one.
	 * 
	 * @param type
	 *            Indicates which type of Dot-graph should be written.
	 * @param name
	 *            Provides the name for the DOT-graph.
	 */
	public void startGraph(GraphType type, String name) {
		startElement();
		stream.write(type.name);
		stream.write(SPACE);
		stream.write(name);
		stream.write(SPACE);
		startAbstractGroup();
	}

	/**
	 * Starts a group. The nested depth is increased by one.
	 */
	public void startGroup() {
		startElement();
		startAbstractGroup();
	}

	/**
	 * Starts a abstract group, increased the nested depth by one and writes a
	 * new line. An abstract group simple groups elements in braces.
	 */
	private void startAbstractGroup() {
		nestedDepth++;
		stream.write('{');
		newLine();
	}

	/**
	 * Writes a new line.
	 */
	private void newLine() {
		stream.write('\n');
	}

	/**
	 * Closes an existing group, decreases the nested depth by one.
	 */
	public void endGroup() {
		nestedDepth--;
		startElement();
		stream.write('}');
		newLine();
	}

	/**
	 * Ends the all groups including the graph group.
	 */
	public void endGraph() {
		int depth = nestedDepth;
		for (int i = 0; i < depth; i++) {
			endGroup();
		}
	}

	/**
	 * Writes a provided attribute list for a given GraphElementType. The
	 * general type name is written in front of the attribute list. Values are
	 * automatically quoted, except values starting and ending with arrow
	 * brackets.
	 * 
	 * @param type
	 *            The general attribute list type.
	 * @param attributeList
	 *            A Map of String pairs. A key has to correspond to a
	 *            DOT-attribute. A value has to be of the corresponding
	 *            DOT-type.
	 */
	public void writeGeneralAttributeList(GraphElementType type,
			Map<String, String> attributeList) {
		startElement();
		stream.write(type.name);
		writeAttributeList(attributeList);
		endElement();
	}

	/**
	 * Writes a provided attribute list. Values are automatically quoted, except
	 * values starting and ending with arrow brackets.
	 * 
	 * @param attributeList
	 *            A Map of String pairs. A key has to correspond to a
	 *            DOT-attribute, whereas a value has to be of the corresponding
	 *            DOT-type.
	 */
	private void writeAttributeList(Map<String, String> attributeList) {

		if (attributeList.isEmpty()) {
			return;
		}

		startAttributeList();
		String delimiter = "";
		for (Entry<String, String> entry : attributeList.entrySet()) {
			stream.write(delimiter);
			delimiter = ", ";
			writeAttribute(entry.getKey(), entry.getValue());
		}

		endAttributeList();
	}

	/**
	 * Starts a AttributeList with a opening bracket.
	 */
	private void startAttributeList() {
		stream.write('[');
	}

	/**
	 * Writes a given Attribute for a given name and value. Values are
	 * automatically quoted, except values starting and ending with arrow
	 * brackets.
	 * 
	 * @param name
	 *            Name of the DOT-attribute.
	 * @param value
	 *            Value of a corresponding DOT-type.
	 */
	private void writeAttribute(String name, String value) {
		stream.write(name);

		stream.write(" = ");

		boolean isHtml = value.startsWith("<<") && value.endsWith(">>");
		String quotationMarks = isHtml ? "" : QUOTATIONMARKS;

		stream.write(quotationMarks);
		stream.write(value);
		stream.write(quotationMarks);
	}

	/**
	 * Ends a attribute list with a closing bracket.
	 */
	private void endAttributeList() {
		stream.write(']');
	}

	/**
	 * Starts an Element with its correct indentation.
	 */
	private void startElement() {
		writeIndent();
	}

	/**
	 * Writes the indentation correlation to the current nested depth.
	 */
	private void writeIndent() {
		for (int i = 0; i < nestedDepth; i++) {
			stream.write(TABULATOR);
		}
	}

	/**
	 * Ends an Element with a semicolon and a new line.
	 */
	private void endElement() {
		stream.write(';');
		newLine();
	}

	/**
	 * Writes an edge with a given start and end node.
	 * 
	 * @param startNode
	 *            Name of the start node.
	 * @param endNode
	 *            Name of the end node.
	 */
	public void writeEdge(String startNode, String endNode) {
		writeEdge(startNode, endNode, null);
	}

	/**
	 * Writes an edge with a given start, end node and attribute list.
	 * 
	 * @param startNode
	 *            Name of the start node.
	 * @param endNode
	 *            Name of the end node.
	 * @param attributeList
	 *            An attribute list provided as a Map of String-pairs.
	 */
	public void writeEdge(String startNode, String endNode,
			Map<String, String> attributeList) {
		String[] endNodeList = { endNode };
		writeEdge(startNode, endNodeList, attributeList);

	}

	/**
	 * Writes an edge with a given start node and an array of end nodes.
	 * 
	 * @param startNode
	 *            Name of the start node.
	 * @param endNodes
	 *            Names of end nodes as array.
	 */
	public void writeEdge(String startNode, String[] endNodes) {
		writeEdge(startNode, endNodes, null);
	}

	/**
	 * Writes an edge with a given start node, an array of end nodes and an
	 * attribute list.
	 * 
	 * @param startNode
	 *            Name of the start node.
	 * @param endNodes
	 *            Names of end nodes as array.
	 * @param attributeList
	 *            An attributed list provided as Map of Strig-pairs.
	 */
	public void writeEdge(String startNode, String[] endNodes,
			Map<String, String> attributeList) {

		startElement();
		stream.write(processName(startNode));
		stream.write(" -> ");
		writeNodeList(endNodes);

		if (attributeList != null) {
			writeAttributeList(attributeList);
		}
		endElement();
	}

	/**
	 * Writes a node with the given name.
	 * 
	 * @param name
	 *            Name of the node.
	 */
	public void writeNode(String name) {
		writeNode(name, null);
	}

	/**
	 * Writes a node with the given name and attribute list.
	 * 
	 * @param name
	 *            Name of the node. A name in
	 * @param attributeList
	 *            An attribute list provided as Map of String-pairs.
	 */
	public void writeNode(String name, Map<String, String> attributeList) {

		startElement();
		name = processName(name);
		stream.write(name);

		if (attributeList != null) {
			writeAttributeList(attributeList);
		}
		endElement();
	}

	/**
	 * Encapsulates the name in braces, in case an existing blank in the given
	 * name.
	 * 
	 * @param name
	 *            Provided name.
	 * @return Correct encapsulated name.
	 */
	private String processName(String name) {
		if (name.contains(" ")) {
			name = "\"" + name + "\"";
		}
		return name;
	}

	/**
	 * Writes a list of node names separated by a comma.
	 * 
	 * @param nodeNames
	 *            Node names provided as String array.
	 */
	private void writeNodeList(String[] nodeNames) {
		String delimiter = "";
		for (String name : nodeNames) {
			stream.write(delimiter);
			delimiter = ", ";
			stream.write(processName(name));
		}
	}

	/**
	 * Starts a Subgraph.
	 */
	public void startSubgraph() {
		startElement();
		stream.write("subgraph");
		startAbstractGroup();
	}

	/**
	 * Starts a named cluster.
	 * 
	 * @param name
	 *            Name of the cluster.
	 */
	public void startCluster(String name) {
		startElement();
		stream.write("subgraph ");
		stream.write(name);
		startAbstractGroup();
	}

	/**
	 * Closes all unclosed groups, flushes the stream and closes it.
	 */
	public void close() {
		endGraph();
		stream.flush();
		stream.close();
	}

	/**
	 * List of reversible dot edge attribute pairs.
	 */
	public static final Map<String, String> reversableEdgeAttributePairs;

	static {
		reversableEdgeAttributePairs = createReversableEdgeAttributePairMap();
		allowedDotEdgeAttributes = createAllowedDotEdgeAttributes();
		allowedDotNodeAttributes = createAllowedDotNodeAttributes();
	}

	private static Map<String, String> createReversableEdgeAttributePairMap() {
		// reversableEdgeAttributePairs
		Map<String, String> edgeAttributePairs = new HashMap<String, String>();
		edgeAttributePairs.put("headURL", "tailURL");
		edgeAttributePairs.put("headclip", "tailclip");
		edgeAttributePairs.put("headhref", "tailhref");
		edgeAttributePairs.put("headlabel", "taillabel");
		edgeAttributePairs.put("headport", "tailport");
		edgeAttributePairs.put("headtarget", "tailtarget");
		edgeAttributePairs.put("headtooltip", "tailtooltip");
		edgeAttributePairs.put("arrowhead", "arrowtail");
		edgeAttributePairs.put("lhead", "ltail");
		edgeAttributePairs.put("samehead", "sametail");
		return Collections.unmodifiableMap(edgeAttributePairs);
	}

	private static Set<String> createAllowedDotEdgeAttributes() {

		String[] allowedDotAttributes = { "URL", "arrowhead", "arrowsize",
				"arrowtail", "color", "colorscheme", "comment", "constraint",
				"decorate", "dir", "edgeURL", "edgehref", "edgetarget",
				"edgetooltip", "fontcolor", "fontname", "fontsize", "headURL",
				"headclip", "headhref", "headlabel", "headport", "headtarget",
				"headtooltip", "href", "id", "label", "labelURL", "labelangle",
				"labeldistance", "labelfloat", "labelfontcolor",
				"labelfontname", "labelfontsize", "labelhref", "labeltarget",
				"labeltooltip", "layer", "len", "lhead", "lp", "ltail",
				"minlen", "nojustify", "penwidth", "pos", "samehead",
				"sametail", "showboxes", "style", "tailURL", "tailclip",
				"tailhref", "taillabel", "tailport", "tailtarget",
				"tailtooltip", "target", "tooltip", "weight" };

		List<String> allowedDotEdgeAttributes = Arrays
				.asList(allowedDotAttributes);
		return Collections.unmodifiableSet(new HashSet<String>(
				allowedDotEdgeAttributes));
	}

	public static Set<String> createAllowedDotNodeAttributes() {
		String[] allowedNodeAttribute = { "URL", "color", "colorscheme",
				"comment", "distortion", "fillcolor", "fixedsize", "fontcolor",
				"fontname", "fontsize", "group", "height", "id", "image",
				"imagescale", "label", "labelloc", "layer", "margin",
				"nojustify", "orientation", "penwidth", "peripheries", "pin",
				"pos", "rects", "regular", "root", "samplepoints", "shape",
				"shapefile", "showboxes", "sides", "skew", "sortv", "style",
				"target", "tooltip", "vertices", "width" };

		List<String> allowedDotNodeAttributes = Arrays
				.asList(allowedNodeAttribute);
		return Collections.unmodifiableSet(new HashSet<String>(
				allowedDotNodeAttributes));
	}
}
