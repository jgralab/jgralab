/*
 * JGraLab - The Java Graph Laboratory
 *
 * Copyright (C) 2006-2013 Institute for Software Technology
 *                         University of Koblenz-Landau, Germany
 *                         ist@uni-koblenz.de
 *
 * For bug reports, documentation and further information, visit
 *
 *                         https://github.com/jgralab/jgralab
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

package de.uni_koblenz.jgralab;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.nio.CharBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Queue;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import de.uni_koblenz.jgralab.exception.GraphException;
import de.uni_koblenz.jgralab.exception.GraphIOException;
import de.uni_koblenz.jgralab.graphmarker.AbstractBooleanGraphMarker;
import de.uni_koblenz.jgralab.impl.GraphBaseImpl;
import de.uni_koblenz.jgralab.impl.InternalAttributedElement;
import de.uni_koblenz.jgralab.impl.InternalGraph;
import de.uni_koblenz.jgralab.impl.TgLexer;
import de.uni_koblenz.jgralab.impl.TgLexer.Token;
import de.uni_koblenz.jgralab.schema.AggregationKind;
import de.uni_koblenz.jgralab.schema.Attribute;
import de.uni_koblenz.jgralab.schema.AttributedElementClass;
import de.uni_koblenz.jgralab.schema.Constraint;
import de.uni_koblenz.jgralab.schema.Domain;
import de.uni_koblenz.jgralab.schema.EdgeClass;
import de.uni_koblenz.jgralab.schema.EnumDomain;
import de.uni_koblenz.jgralab.schema.GraphClass;
import de.uni_koblenz.jgralab.schema.GraphElementClass;
import de.uni_koblenz.jgralab.schema.MapDomain;
import de.uni_koblenz.jgralab.schema.NamedElement;
import de.uni_koblenz.jgralab.schema.Package;
import de.uni_koblenz.jgralab.schema.RecordDomain;
import de.uni_koblenz.jgralab.schema.RecordDomain.RecordComponent;
import de.uni_koblenz.jgralab.schema.Schema;
import de.uni_koblenz.jgralab.schema.VertexClass;
import de.uni_koblenz.jgralab.schema.codegenerator.CodeGeneratorConfiguration;
import de.uni_koblenz.jgralab.schema.exception.SchemaException;
import de.uni_koblenz.jgralab.schema.impl.BasicDomainImpl;
import de.uni_koblenz.jgralab.schema.impl.ConstraintImpl;
import de.uni_koblenz.jgralab.schema.impl.SchemaImpl;
import de.uni_koblenz.jgralab.schema.impl.compilation.SchemaClassManager;

/**
 * class for loading and storing schema and graphs in tg format
 * 
 * @author ist@uni-koblenz.de
 */
public final class GraphIO {
	/**
	 * TG File Version this GraphIO recognizes.
	 */
	public static final int TGFILE_VERSION = 2;
	public static final String NULL_LITERAL = TgLexer.Token.NULL_LITERAL
			.toString();
	public static final String TRUE_LITERAL = TgLexer.Token.TRUE_LITERAL
			.toString();
	public static final String FALSE_LITERAL = TgLexer.Token.FALSE_LITERAL
			.toString();
	public static final String TGRAPH_FILE_EXTENSION = ".tg";
	public static final String TGRAPH_COMPRESSED_FILE_EXTENSION = ".tg.gz";
	private static final int WRITE_BUFFER_SIZE = 65536;

	/**
	 * A {@link FilenameFilter} that accepts TG files.
	 * 
	 * @author ist@uni-koblenz.de
	 */
	public static class TGFilenameFilter extends
			javax.swing.filechooser.FileFilter implements FilenameFilter {

		private static TGFilenameFilter instance;

		private TGFilenameFilter() {
		}

		public static TGFilenameFilter instance() {
			if (instance == null) {
				instance = new TGFilenameFilter();
			}
			return instance;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see java.io.FilenameFilter#accept(java.io.File, java.lang.String)
		 */
		@Override
		public boolean accept(File dir, String name) {
			if (name.matches(".+\\.[Tt][Gg](\\.[Gg][Zz])?$")) {
				return true;
			}
			return false;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see java.io.FileFilter#accept(java.io.File)
		 */
		@Override
		public boolean accept(File f) {
			return f.isDirectory() || accept(f, f.getName());
		}

		@Override
		public String getDescription() {
			return "TG Files";
		}
	}

	private OutputStream TGOut;

	private Schema schema;

	/**
	 * Maps domain names to the respective Domains.
	 */
	private final Map<String, Domain> domains;

	private String gcName; // GraphClass name of the currently loaded graph

	private TgLexer lexer;
	private Token lookAhead; // parser lookAhead token

	// arrays to keep incidence information
	private Vertex[] edgeIn; // omega vertices, index = edge id
	private Vertex[] edgeOut; // alpha vertices, index = edge id
	private int[] firstIncidence; // first incidence, index = vertex id
	// next incidence, index = edgeOffset+incidence id
	private int[] nextIncidence;
	// middle of nextIncidence array to care or negative incidence ids
	private int edgeOffset;

	public static enum Unset {
		UNSET
	};

	/**
	 * Buffers the parsed data of enum domains prior to their creation in
	 * JGraLab.
	 */
	private final Set<EnumDomainData> enumDomainBuffer;

	/**
	 * Buffers the parsed data of record domains prior to their creation in
	 * JGraLab.
	 */
	private List<RecordDomainData> recordDomainBuffer;

	/**
	 * Buffers the parsed data of the graph class prior to its creation in
	 * JGraLab.
	 */
	private GraphClassData graphClass;

	/**
	 * Buffers the parsed data of vertex classes prior to their creation in
	 * JGraLab.
	 */
	private final Map<String, List<GraphElementClassData>> vertexClassBuffer;

	/**
	 * Buffers the parsed data of edge classes prior to their creation in
	 * JGraLab.
	 */
	private final Map<String, List<GraphElementClassData>> edgeClassBuffer;

	private final Map<String, List<String>> commentData;

	private String currentPackageName;

	private ByteArrayOutputStream BAOut;

	// stringPool allows re-use string values, saves memory if
	// multiple identical strings are used as attribute values
	private final HashMap<String, String> stringPool;

	private GraphFactory graphFactory;

	private GraphIO() {
		domains = new TreeMap<String, Domain>();
		enumDomainBuffer = new HashSet<EnumDomainData>();
		recordDomainBuffer = new ArrayList<RecordDomainData>();
		graphClass = null;
		vertexClassBuffer = new TreeMap<String, List<GraphElementClassData>>();
		edgeClassBuffer = new TreeMap<String, List<GraphElementClassData>>();
		commentData = new HashMap<String, List<String>>();
		stringPool = new HashMap<String, String>();
	}

	public static Schema loadSchemaFromFile(String filename)
			throws GraphIOException {
		InputStream in = null;
		try {
			in = inputStreamForFilename(filename);
			return loadSchemaFromStream(in, filename);
		} catch (IOException ex) {
			throw new GraphIOException("Exception while loading schema from "
					+ filename, ex);
		} finally {
			close(in);
		}
	}

	public static Schema loadSchemaFromStream(InputStream in)
			throws GraphIOException {
		return loadSchemaFromStream(in, null);
	}

	public static Schema loadSchemaFromStream(InputStream in, String filename)
			throws GraphIOException {
		try {
			GraphIO io = new GraphIO();
			io.lexer = new TgLexer(in, filename);
			io.tgfile();
			io.schema.finish();
			return io.schema;
		} catch (GraphIOException e1) {
			throw e1;
		} catch (Exception e2) {
			throw new GraphIOException("Exception while loading schema.", e2);
		}
	}

	/**
	 * Saves the specified <code>schema</code> to the file named
	 * <code>filename</code>. When the <code>filename</code> ends with
	 * <code>.gz</code>, output will be GZIP compressed, otherwise uncompressed
	 * plain text.
	 * 
	 * @param schema
	 *            a schema
	 * @param filename
	 *            the name of the file
	 * @throws GraphIOException
	 *             if an IOException occurs
	 */
	public static void saveSchemaToFile(Schema schema, String filename)
			throws GraphIOException {
		OutputStream out = null;
		try {
			out = outputStreamForFilename(filename);
			saveSchemaToStream(schema, out);
		} catch (IOException ex) {
			throw new GraphIOException("Exception while saving schema to "
					+ filename, ex);
		} finally {
			close(out);
		}
	}

	/**
	 * Saves the specified <code>schema</code> to the stream <code>out</code>.
	 * The stream is <em>not</em> closed.
	 * 
	 * @param schema
	 *            a schema
	 * @param out
	 *            an OutputStream
	 * @throws GraphIOException
	 *             if an IOException occurs
	 */
	public static void saveSchemaToStream(Schema schema, OutputStream out)
			throws GraphIOException {
		try {
			GraphIO io = new GraphIO();
			io.TGOut = out;
			// don't save spaces in schema files since they're likely to be read
			// by humans
			io.compact = false;
			io.saveHeader();
			io.saveSchema(schema);
		} catch (IOException e) {
			throw new GraphException("Exception while saving schema", e);
		}
	}

	private void saveSchema(Schema s) throws IOException {
		schema = s;
		write("Schema");
		writeIdentifier(schema.getQualifiedName());
		write(";\n");

		// write graphclass
		GraphClass gc = schema.getGraphClass();
		write("GraphClass");
		writeIdentifier(gc.getSimpleName());
		writeAttributes(null, gc);
		writeConstraints(gc);
		write(";\n");
		writeComments(gc, gc.getSimpleName());

		Queue<de.uni_koblenz.jgralab.schema.Package> worklist = new LinkedList<de.uni_koblenz.jgralab.schema.Package>();
		worklist.offer(s.getDefaultPackage());
		while (!worklist.isEmpty()) {
			Package pkg = worklist.poll();
			worklist.addAll(pkg.getSubPackages());

			// write package declaration
			if (!pkg.isDefaultPackage()) {
				write("Package");
				writeIdentifier(pkg.getQualifiedName());
				write(";\n");
			}

			// write domains
			for (Domain dom : pkg.getDomains()) {
				if (dom instanceof EnumDomain) {
					EnumDomain ed = (EnumDomain) dom;
					write("EnumDomain");
					writeIdentifier(ed.getSimpleName());
					write("(");
					for (Iterator<String> eit = ed.getConsts().iterator(); eit
							.hasNext();) {
						writeIdentifier(eit.next());
						if (eit.hasNext()) {
							write(",");
						}
					}
					write(");\n");
					writeComments(ed, ed.getSimpleName());
				} else if (dom instanceof RecordDomain) {
					RecordDomain rd = (RecordDomain) dom;
					write("RecordDomain");
					writeIdentifier(rd.getSimpleName());
					write("(");
					String delim = "";
					for (RecordComponent rdc : rd.getComponents()) {
						write(delim);
						writeIdentifier(rdc.getName());
						write(":");
						write(rdc.getDomain().getTGTypeName(pkg));
						delim = ",";
					}
					write(");");
					writeComments(rd, rd.getSimpleName());
				}
			}

			// write vertex classes
			for (VertexClass vc : pkg.getVertexClasses()) {
				if (vc.isAbstract()) {
					write("abstract");
				}
				write("VertexClass");
				writeIdentifier(vc.getSimpleName());
				writeHierarchy(pkg, vc);
				writeAttributes(pkg, vc);
				writeConstraints(vc);
				write(";\n");
				writeComments(vc, vc.getSimpleName());
			}

			// write edge classes
			for (EdgeClass ec : pkg.getEdgeClasses()) {
				if (ec.isDefaultGraphElementClass()) {
					continue;
				}
				if (ec.isAbstract()) {
					write("abstract");
				}
				write("EdgeClass");
				writeIdentifier(ec.getSimpleName());
				writeHierarchy(pkg, ec);

				// from (min,max) rolename
				write("from");
				writeIdentifier(ec.getFrom().getVertexClass()
						.getQualifiedName(pkg));
				write("(");
				write(ec.getFrom().getMin() + ",");
				if (ec.getFrom().getMax() == Integer.MAX_VALUE) {
					write("*)");
				} else {
					write(ec.getFrom().getMax() + ")");
				}

				if (!ec.getFrom().getRolename().equals("")) {
					write("role");
					writeIdentifier(ec.getFrom().getRolename());
				}

				switch (ec.getFrom().getAggregationKind()) {
				case NONE:
					// do nothing
					break;
				case SHARED:
					write("aggregation shared");
					break;
				case COMPOSITE:
					write("aggregation composite");
					break;
				}

				// to (min,max) rolename
				write("to");
				writeIdentifier(ec.getTo().getVertexClass()
						.getQualifiedName(pkg));
				write("(");
				write(ec.getTo().getMin() + ",");
				if (ec.getTo().getMax() == Integer.MAX_VALUE) {
					write("*)");
				} else {
					write(ec.getTo().getMax() + ")");
				}

				if (!ec.getTo().getRolename().equals("")) {
					write("role");
					writeIdentifier(ec.getTo().getRolename());
				}

				switch (ec.getTo().getAggregationKind()) {
				case NONE:
					// do nothing
					break;
				case SHARED:
					write("aggregation shared");
					break;
				case COMPOSITE:
					write("aggregation composite");
					break;
				}

				writeAttributes(pkg, ec);
				writeConstraints(ec);
				write(";\n");
				writeComments(ec, ec.getSimpleName());
			}

			// write package comments
			writeComments(pkg, "." + pkg.getQualifiedName());
		}
	}

	private void writeComments(NamedElement elem, String name)
			throws IOException {
		if (!elem.getComments().isEmpty()) {
			write("Comment");
			writeIdentifier(name);
			for (String c : elem.getComments()) {
				writeUtfString(c);
			}
			write(";\n");
		}
	}

	private void writeConstraints(AttributedElementClass<?, ?> aec)
			throws IOException {
		for (Constraint c : aec.getConstraints()) {
			write("[");
			writeUtfString(c.getMessage());
			writeUtfString(c.getPredicate());
			if (c.getOffendingElementsQuery() != null) {
				writeUtfString(c.getOffendingElementsQuery());
			}
			write("]");
		}
	}

	/**
	 * Saves the specified <code>graph</code> to the file named
	 * <code>filename</code>. When the <code>filename</code> ends with
	 * <code>.gz</code>, output will be GZIP compressed, otherwise uncompressed
	 * plain text. A {@link ProgressFunction} <code>pf</code> can be used to
	 * monitor progress.
	 * 
	 * @param graph
	 *            a graph
	 * @param filename
	 *            the name of the TG file to be written
	 * @param pf
	 *            a {@link ProgressFunction}, may be <code>null</code>
	 * @throws GraphIOException
	 *             if an IOException occurs
	 */
	public static void saveGraphToFile(Graph graph, String filename,
			ProgressFunction pf) throws GraphIOException {
		OutputStream out = null;
		try {
			out = outputStreamForFilename(filename);
			saveGraphToStream(graph, out, pf);
		} catch (IOException ex) {
			throw new GraphIOException("Exception while saving graph to "
					+ filename, ex);
		} finally {
			close(out);
		}
	}

	/**
	 * Saves the marked <code>subGraph</code> to the file named
	 * <code>filename</code>. A {@link ProgressFunction} <code>pf</code> can be
	 * used to monitor progress. The stream is <em>not</em> closed. This method
	 * does <i>not</i> check if the subgraph marker is complete.
	 * 
	 * @param subGraph
	 *            a BooleanGraphMarker denoting the subgraph to be saved
	 * @param filename
	 *            a filename
	 * @param pf
	 *            a {@link ProgressFunction}, may be <code>null</code>
	 * @throws GraphIOException
	 *             if an IOException occurs
	 */
	public static void saveGraphToFile(AbstractBooleanGraphMarker subGraph,
			String filename, ProgressFunction pf) throws GraphIOException {
		OutputStream out = null;
		try {
			out = outputStreamForFilename(filename);
			saveGraphToStream(subGraph, out, pf);
		} catch (IOException e) {
			throw new GraphIOException("Exception while saving graph to "
					+ filename, e);
		} finally {
			close(out);
		}
	}

	/**
	 * Saves the specified <code>graph</code> to the stream <code>out</code>. A
	 * {@link ProgressFunction} <code>pf</code> can be used to monitor progress.
	 * The stream is <em>not</em> closed.
	 * 
	 * @param graph
	 *            a graph
	 * @param out
	 *            an OutputStream
	 * @param pf
	 *            a {@link ProgressFunction}, may be <code>null</code>
	 * @throws GraphIOException
	 *             if an IOException occurs
	 */
	public static void saveGraphToStream(Graph graph, OutputStream out,
			ProgressFunction pf) throws GraphIOException {
		try {
			if (hasTemporaryElements(graph)) {
				throw new GraphIOException("Saving graph " + graph
						+ " is not possible. "
						+ "It contains temporary graph elements.");
			}
			GraphIO io = new GraphIO();
			io.TGOut = out;
			io.compact = true; // save spaces in graph files
			io.saveGraph((InternalGraph) graph, pf, null);
			out.flush();
		} catch (IOException e) {
			throw new GraphIOException("Exception while saving graph", e);
		}
	}

	/**
	 * Saves the marked <code>subGraph</code> to the stream <code>out</code>. A
	 * {@link ProgressFunction} <code>pf</code> can be used to monitor progress.
	 * The stream is <em>not</em> closed. This method does <i>not</i> check if
	 * the subgraph marker is complete.
	 * 
	 * @param out
	 *            an OutputStream
	 * @param subGraph
	 *            a BooleanGraphMarker denoting the subgraph to be saved
	 * @param pf
	 *            a {@link ProgressFunction}, may be <code>null</code>
	 * @throws GraphIOException
	 *             if an IOException occurs
	 */
	public static void saveGraphToStream(AbstractBooleanGraphMarker subGraph,
			OutputStream out, ProgressFunction pf) throws GraphIOException {
		try {
			if (hasTemporaryElements(subGraph, subGraph.getGraph())) {
				throw new GraphIOException("Saving subgraph " + subGraph
						+ " of " + subGraph.getGraph() + " is not possible. "
						+ "It contains temporary graph elements.");
			}

			GraphIO io = new GraphIO();
			io.TGOut = out;
			io.compact = true; // save spaces in graph files
			io.saveGraph((InternalGraph) subGraph.getGraph(), pf, subGraph);
			out.flush();
		} catch (IOException e) {
			throw new GraphIOException("Exception while saving graph", e);
		}
	}

	private static boolean hasTemporaryElements(Graph g) {
		if (g.vertices(g.getGraphClass().getTemporaryVertexClass()).iterator()
				.hasNext()) {
			return true;
		}
		if (g.edges(g.getGraphClass().getTemporaryEdgeClass()).iterator()
				.hasNext()) {
			return true;
		}
		return false;
	}

	private static boolean hasTemporaryElements(
			AbstractBooleanGraphMarker marker, Graph g) {
		for (Vertex v : g.vertices(g.getGraphClass().getTemporaryVertexClass())) {
			if (marker.isMarked(v)) {
				return true;
			}
		}
		for (Edge e : g.edges(g.getGraphClass().getTemporaryEdgeClass())) {
			if (marker.isMarked(e)) {
				return true;
			}
		}
		return false;
	}

	private void saveGraph(InternalGraph graph, ProgressFunction pf,
			AbstractBooleanGraphMarker subGraph) throws IOException,
			GraphIOException {
		TraversalContext tc = graph.setTraversalContext(null);
		try {
			// Write the jgralab version and license in a comment
			saveHeader();

			schema = graph.getSchema();
			saveSchema(schema);

			long eId;
			long vId;

			// progress bar for graph
			long graphElements = 0, currentCount = 0, interval = 1;
			if (pf != null) {
				if (subGraph != null) {
					pf.init(subGraph.size());
				} else {
					pf.init(graph.getVCount() + graph.getECount());
				}
				interval = pf.getUpdateInterval();
			}

			write("Graph");
			write(toUtfString(graph.getId()));
			writeLong(graph.getGraphVersion());
			writeIdentifier(graph.getAttributedElementClass()
					.getQualifiedName());
			int vCount = graph.getVCount();
			int eCount = graph.getECount();
			// with a GraphMarker, v/eCount have to be restricted to the marked
			// elements.
			if (subGraph != null) {
				vCount = 0;
				eCount = 0;
				for (AttributedElement<?, ?> ae : subGraph.getMarkedElements()) {
					if (ae instanceof Vertex) {
						vCount++;
					} else if (ae instanceof Edge) {
						eCount++;
					}
				}
			}
			write("(");
			writeInteger(graph.getMaxVCount());
			writeInteger(graph.getMaxECount());
			writeInteger(vCount);
			writeInteger(eCount);
			write(")");
			graph.writeAttributeValues(this);
			write(";\n");

			Package oldPackage = null;
			// write vertices
			// System.out.println("Writing vertices");
			Vertex nextV = graph.getFirstVertex();
			while (nextV != null) {
				if ((subGraph != null) && !subGraph.isMarked(nextV)) {
					nextV = nextV.getNextVertex();
					continue;
				}
				vId = nextV.getId();
				AttributedElementClass<?, ?> aec = nextV
						.getAttributedElementClass();
				Package currentPackage = aec.getPackage();
				if (currentPackage != oldPackage) {
					write("Package");
					writeIdentifier(currentPackage.getQualifiedName());
					write(";\n");
					oldPackage = currentPackage;
				}
				write(Long.toString(vId));
				writeIdentifier(aec.getSimpleName());
				// write incident edges
				Edge nextI = nextV.getFirstIncidence();
				write("<");
				// System.out.print("  Writing incidences of vertex.");
				while (nextI != null) {
					if ((subGraph != null) && !subGraph.isMarked(nextI)) {
						nextI = nextI.getNextIncidence();
						continue;
					}
					writeLong(nextI.getId());
					nextI = nextI.getNextIncidence();
				}
				write(">");
				((InternalAttributedElement) nextV).writeAttributeValues(this);
				write(";\n");
				nextV = nextV.getNextVertex();

				// update progress bar
				if (pf != null) {
					graphElements++;
					currentCount++;
					if (currentCount == interval) {
						pf.progress(graphElements);
						currentCount = 0;
					}
				}
			}

			// System.out.println("Writing edges");
			// write edges
			Edge nextE = graph.getFirstEdge();
			while (nextE != null) {
				if ((subGraph != null) && !subGraph.isMarked(nextE)) {
					nextE = nextE.getNextEdge();
					continue;
				}
				eId = nextE.getId();
				AttributedElementClass<?, ?> aec = nextE
						.getAttributedElementClass();
				Package currentPackage = aec.getPackage();
				if (currentPackage != oldPackage) {
					write("Package");
					writeIdentifier(currentPackage.getQualifiedName());
					write(";\n");
					oldPackage = currentPackage;
				}
				write(Long.toString(eId));
				writeIdentifier(aec.getSimpleName());
				((InternalAttributedElement) nextE).writeAttributeValues(this);
				write(";\n");
				nextE = nextE.getNextEdge();

				// update progress bar
				if (pf != null) {
					graphElements++;
					currentCount++;
					if (currentCount == interval) {
						pf.progress(graphElements);
						currentCount = 0;
					}
				}

			}
			TGOut.flush();
			// finish progress bar
			if (pf != null) {
				pf.finished();
			}
		} finally {
			graph.setTraversalContext(tc);
		}
	}

	private void saveHeader() throws IOException {
		write(JGraLab.getVersionInfo(true));
		write("TGraph");
		writeInteger(TGFILE_VERSION);
		write(";\n");
	}

	private void writeHierarchy(Package pkg, GraphElementClass<?, ?> aec)
			throws IOException {
		String delim = ":";
		for (GraphElementClass<?, ?> superClass : aec.getDirectSuperClasses()) {
			write(delim);
			writeIdentifier(superClass.getQualifiedName(pkg));
			delim = ",";
		}
	}

	private void writeAttributes(Package pkg, AttributedElementClass<?, ?> aec)
			throws IOException {
		List<Attribute> attributes = aec.getOwnAttributeList();
		if (attributes.isEmpty()) {
			return;
		}
		String delim = "{";
		for (Attribute a : attributes) {
			write(delim);
			delim = ",";
			writeIdentifier(a.getName());
			write(":");
			String domain = a.getDomain().getTGTypeName(pkg);
			write(domain);
			if ((a.getDefaultValueAsString() != null)
					&& !a.getDefaultValueAsString().equals("n")) {
				write("=");
				writeUtfString(a.getDefaultValueAsString());
			}
		}
		write("}");
	}

	private int lastCh = 32; // last character written
	// compact controls output behaviour:
	// true => smaller file size and better performance (save spaces)
	// false => improve readability by adding more spaces
	private boolean compact = true;

	public final void write(String s) throws IOException {
		int len = s.length();
		if (len > 0) {
			int ch = s.charAt(0);
			if (compact) {
				if (!TgLexer.isDelimiter(lastCh) && !TgLexer.isDelimiter(ch)) {
					TGOut.write(32);
				}
			} else {
				if (!TgLexer.isWs(lastCh)) {
					if (!((lastCh == '(') || (lastCh == '[') || (lastCh == '{')
							|| (lastCh == '<') || (ch == ')') || (ch == ']')
							|| (ch == '}') || (ch == '>') || (ch == ':')
							|| (ch == ',') || (ch == ';'))) {
						TGOut.write(32);
					}
				}
			}
			TGOut.write(ch);
			lastCh = ch;
			for (int i = 1; i < len; ++i) {
				lastCh = s.charAt(i);
				TGOut.write((byte) lastCh);
			}
		}
	}

	public final void writeBoolean(boolean b) throws IOException {
		write(b ? TRUE_LITERAL : FALSE_LITERAL);
	}

	public final void writeInteger(int i) throws IOException {
		write(Integer.toString(i));
	}

	public final void writeLong(long l) throws IOException {
		write(Long.toString(l));
	}

	public final void writeDouble(double d) throws IOException {
		write(Double.toString(d));
	}

	public final void writeUtfString(String s) throws IOException {
		write(s == null ? NULL_LITERAL : toUtfString(s));
	}

	public final void writeIdentifier(String s) throws IOException {
		write(s);
	}

	public static GraphIO createStringReader(String input, Schema schema)
			throws GraphIOException {
		GraphIO io = new GraphIO();
		io.lexer = new TgLexer(input);
		io.schema = schema;
		io.match();
		return io;
	}

	public static GraphIO createStringWriter(Schema schema) {
		GraphIO io = new GraphIO();
		io.BAOut = new ByteArrayOutputStream();
		io.TGOut = io.BAOut;
		io.schema = schema;
		return io;
	}

	public String getStringWriterResult() throws GraphIOException, IOException {
		if (BAOut == null) {
			throw new GraphIOException("GraphIO did not write to a String.");
		}
		try {
			BAOut.flush();
			String result = BAOut.toString("US-ASCII");
			return result;
		} finally {
			close(BAOut);
		}
	}

	public static Graph loadGraphFromFile(String filename, ProgressFunction pf)
			throws GraphIOException {
		return loadGraphFromFile(filename, ImplementationType.STANDARD, pf);
	}

	public static Graph loadGraphFromFile(String filename,
			ImplementationType implementationType, ProgressFunction pf)
			throws GraphIOException {
		if (implementationType == null) {
			throw new IllegalArgumentException(
					"ImplementationType must be != null");
		}
		InputStream in = null;
		try {
			in = inputStreamForFilename(filename);
			return loadGraphFromStream(in, filename, null, null,
					implementationType, pf);
		} catch (IOException ex) {
			throw new GraphIOException(
					"Exception while loading graph from file " + filename, ex);
		} finally {
			close(in);
		}
	}

	public static <G extends Graph> G loadGraphFromFile(String filename,
			Schema schema, ImplementationType implementationType,
			ProgressFunction pf) throws GraphIOException {
		if (schema == null) {
			throw new IllegalArgumentException("Schema must be != null");
		}
		if (implementationType == null) {
			throw new IllegalArgumentException(
					"ImplementationType must be != null");
		}
		GraphFactory factory = schema
				.createDefaultGraphFactory(implementationType);
		return GraphIO.<G> loadGraphFromFile(filename, factory, pf);
	}

	public static <G extends Graph> G loadGraphFromFile(String filename,
			GraphFactory factory, ProgressFunction pf) throws GraphIOException {
		if (factory == null) {
			throw new IllegalArgumentException("GraphFactory must be != null");
		}
		InputStream in = null;
		try {
			in = inputStreamForFilename(filename);
			return GraphIO.<G> loadGraphFromStream(in, filename,
					factory.getSchema(), factory,
					factory.getImplementationType(), pf);
		} catch (IOException ex) {
			throw new GraphIOException(
					"Exception while loading graph from file " + filename, ex);
		} finally {
			close(in);
		}
	}

	private static InputStream inputStreamForFilename(String filename)
			throws IOException {
		InputStream in = new FileInputStream(filename);
		if (filename.toLowerCase().endsWith(".gz")) {
			in = new GZIPInputStream(in);
		}
		return in;
	}

	private static OutputStream outputStreamForFilename(String filename)
			throws IOException {
		OutputStream out = new FileOutputStream(filename);
		if (filename.toLowerCase().endsWith(".gz")) {
			out = new BufferedOutputStream(new GZIPOutputStream(out),
					WRITE_BUFFER_SIZE);
		} else {
			out = new BufferedOutputStream(out, WRITE_BUFFER_SIZE);
		}
		return out;
	}

	private static void close(Closeable stream) throws GraphIOException {
		try {
			if (stream != null) {
				stream.close();
			}
		} catch (IOException ex) {
			throw new GraphIOException("Exception while closing stream.", ex);
		}
	}

	public static <G extends Graph> G loadGraphFromStream(InputStream in,
			String filename, Schema schema, GraphFactory graphFactory,
			ImplementationType implementationType, ProgressFunction pf)
			throws GraphIOException {
		try {
			GraphIO io = new GraphIO();
			io.lexer = new TgLexer(in, filename);
			io.schema = schema;
			io.tgfile();
			if (implementationType != ImplementationType.GENERIC) {
				// we have replace the schema by an instance of the compiled
				// schema, try to load the schema class
				String schemaQName = io.schema.getQualifiedName();
				Class<?> schemaClass = null;
				try {
					schemaClass = Class.forName(schemaQName, true,
							SchemaClassManager.instance(schemaQName));
				} catch (ClassNotFoundException e) {
					// schema class not found, try compile schema in-memory
					io.schema.finish();
					io.schema.compile(CodeGeneratorConfiguration.MINIMAL);
					try {
						schemaClass = Class.forName(schemaQName, true,
								SchemaClassManager.instance(schemaQName));
					} catch (ClassNotFoundException e1) {
						throw new GraphIOException(
								"Unable to load a graph which belongs to the schema because the Java-classes for this schema can not be created.",
								e1);
					}
				}
				// create an instance of the compiled schema class
				Method instanceMethod = schemaClass.getMethod("instance",
						(Class<?>[]) null);
				io.schema = (Schema) instanceMethod.invoke(null, new Object[0]);
			}
			io.schema.finish();
			if (graphFactory == null) {
				graphFactory = io.schema
						.createDefaultGraphFactory(implementationType);
			}
			if (graphFactory.getSchema() != io.schema) {
				throw new GraphIOException(
						"Incompatible in graph factory: Expected '"
								+ io.schema.getQualifiedName() + "', found '"
								+ graphFactory.getSchema().getQualifiedName()
								+ "'.");
			}
			if ((implementationType != null)
					&& (graphFactory.getImplementationType() != implementationType)) {
				throw new GraphIOException(
						"Graph factory has wrong implementation type: Expected '"
								+ implementationType + "', found '"
								+ graphFactory.getImplementationType() + "'.");
			}
			io.graphFactory = graphFactory;

			@SuppressWarnings("unchecked")
			G loadedGraph = (G) io.graph(pf);
			return loadedGraph;
		} catch (GraphIOException e1) {
			throw e1;
		} catch (Exception e2) {
			throw new GraphIOException("Exception while loading graph.", e2);
		}
	}

	private void tgfile() throws GraphIOException {
		match();
		header();
		schema();
		if ((lookAhead == Token.EOF) || (lookAhead == Token.GRAPH)) {
			return;
		}
		throw new GraphIOException(lexer.getLocation() + "Unexpected symbol '"
				+ lexer.getText() + "'");
	}

	/**
	 * Reads TG File header and checks if the file version can be processed.
	 * 
	 * @throws GraphIOException
	 *             if version number in file can not be processed
	 */
	private void header() throws GraphIOException {
		match(Token.TGRAPH);
		int version = matchInteger();
		if (version != TGFILE_VERSION) {
			throw new GraphIOException("Can't read TGFile version " + version
					+ ". Expected version " + TGFILE_VERSION);
		}
		match(Token.SEMICOLON);
	}

	/**
	 * Reads a Schema together with its Domains, GraphClasses and
	 * GraphElementClasses from a TG-file. Subsequently, the Schema is created.
	 * 
	 * @throws GraphIOException
	 */
	private void schema() throws GraphIOException {
		currentPackageName = "";
		match(Token.SCHEMA);
		String[] qn = matchAndSplitQualifiedName();
		if (qn[0].isEmpty()) {
			throw new GraphIOException(lexer.getLocation()
					+ "Invalid schema name '" + lookAhead
					+ "', package prefix must not be empty.");
		}
		match(Token.SEMICOLON);

		if (schema != null) {
			// We already have a schema, so we don't want to load the schema
			// from the file

			// but wait, check if the names match...
			if (schema.getQualifiedName().equals(qn[0] + "." + qn[1])) {
				// yes, everything is fine :-)
				// skip schema part
				//
				// Beware: it's totally ok to have a VertexClass Graph, so
				// lookAhead = Graph is a too weak check. So we test that before
				// the Graph, the last token is a ;, too.
				Token prev = null;
				while ((lookAhead != Token.EOF)
						&& !((prev == Token.SEMICOLON) && (lookAhead == Token.GRAPH))) {
					prev = lookAhead;
					match();
				}
				return;
			} else {
				throw new GraphIOException(
						"Trying to load a graph with wrong schema. Expected: "
								+ schema.getQualifiedName() + ", but found "
								+ qn[0] + "." + qn[1]);
			}
		}

		schema = new SchemaImpl(qn[1], qn[0]);

		// read Domains and GraphClasses with contained GraphElementClasses
		parseSchema();

		// test for correct syntax, because otherwise, the following
		// sorting/creation methods probably can't work.
		if (!((lookAhead == Token.EOF) || (lookAhead == Token.GRAPH))) {
			throw new GraphIOException(lexer.getLocation()
					+ "Unexpected symbol '" + lexer.getText() + "'");
		}

		// sort data of RecordDomains, GraphClasses and GraphElementClasses in
		// topological order

		checkFromToVertexClasses();

		sortRecordDomains();
		sortVertexClasses();
		sortEdgeClasses();

		createDomains(); // create Domains
		completeGraphClass(); // create GraphClasses with contained elements
		buildHierarchy(); // build inheritance relationships
		processComments();
	}

	/**
	 * Adds comments collected during schema parsing to the annotated elements.
	 * 
	 * @throws GraphIOException
	 */
	private void processComments() throws GraphIOException {
		for (Entry<String, List<String>> e : commentData.entrySet()) {
			if (!schema.knows(e.getKey())) {
				throw new GraphIOException("Annotated element '" + e.getKey()
						+ "' not found in schema " + schema.getQualifiedName());
			}
			NamedElement el = schema.getNamedElement(e.getKey());
			if ((el instanceof Domain)
					&& !((el instanceof EnumDomain) || (el instanceof RecordDomain))) {
				throw new GraphIOException(
						"Default domains can not have comments. Offending domain is '"
								+ e.getKey() + "'");
			}
			for (String comment : e.getValue()) {
				el.addComment(comment);
			}
		}
	}

	/**
	 * Creates the Domains contained in a Schema.
	 * 
	 * @return A Map of the Domain names to the concrete Domain objects.
	 * @throws GraphIOException
	 */
	private Map<String, Domain> createDomains() throws GraphIOException {
		// no need to create basic domains, they're created automatically
		createEnumDomains();
		createRecordDomains();
		return domains;
	}

	/**
	 * Reads an EnumDomain, i.e. its name along with the enum constants.
	 * 
	 * @throws GraphIOException
	 */
	private void parseEnumDomain() throws GraphIOException {
		match(Token.ENUMDOMAIN);
		String[] qn = matchAndSplitQualifiedName();
		enumDomainBuffer.add(new EnumDomainData(qn[0], qn[1],
				parseEnumConstants()));
		match(Token.SEMICOLON);
	}

	/**
	 * Creates all EnumDomains whose data is stored in {@link enumDomainBuffer}
	 */
	private void createEnumDomains() {
		for (EnumDomainData enumDomainData : enumDomainBuffer) {
			String qName = toQNameString(enumDomainData.packageName,
					enumDomainData.simpleName);
			Domain domain = schema.createEnumDomain(qName,
					enumDomainData.enumConstants);
			domains.put(qName, domain);
		}
	}

	/**
	 * Read a RecordDomain, i.e. its name along with the components.
	 * 
	 * @throws GraphIOException
	 */
	private void parseRecordDomain() throws GraphIOException {
		match(Token.RECORDDOMAIN);
		String[] qn = matchAndSplitQualifiedName();
		recordDomainBuffer.add(new RecordDomainData(qn[0], qn[1],
				parseRecordComponents()));
		match(Token.SEMICOLON);
	}

	/**
	 * Creates all RecordDomains whose data is stored in
	 * {@link recordDomainBuffer} @
	 */
	private void createRecordDomains() throws GraphIOException {
		for (RecordDomainData recordDomainData : recordDomainBuffer) {
			String qName = toQNameString(recordDomainData.packageName,
					recordDomainData.simpleName);
			Domain domain = schema.createRecordDomain(qName,
					getComponents(recordDomainData.components));
			domains.put(qName, domain);
		}
	}

	private List<RecordComponent> getComponents(
			List<ComponentData> componentsData) throws GraphIOException {
		List<RecordComponent> result = new ArrayList<RecordComponent>(
				componentsData.size());

		for (ComponentData ad : componentsData) {
			RecordComponent c = new RecordComponent(ad.name,
					attrDomain(ad.domainDescription));
			result.add(c);
		}
		return result;
	}

	/**
	 * Reads Schema's Domains and GraphClasses with contained
	 * GraphElementClasses from TG-file.
	 * 
	 * @throws GraphIOException
	 */
	private void parseSchema() throws GraphIOException {
		while (lookAhead == Token.COMMENT) {
			parseComment();
		}
		String currentGraphClassName = parseGraphClass();

		while ((lookAhead == Token.PACKAGE)
				|| (lookAhead == Token.RECORDDOMAIN)
				|| (lookAhead == Token.ENUMDOMAIN)
				|| (lookAhead == Token.ABSTRACT)
				|| (lookAhead == Token.VERTEXCLASS)
				|| (lookAhead == Token.EDGECLASS)
				|| (lookAhead == Token.COMMENT)) {
			if (lookAhead == Token.PACKAGE) {
				parsePackage();
			} else if (lookAhead == Token.RECORDDOMAIN) {
				parseRecordDomain();
			} else if (lookAhead == Token.ENUMDOMAIN) {
				parseEnumDomain();
			} else if (lookAhead == Token.COMMENT) {
				parseComment();
			} else {
				parseGraphElementClass(currentGraphClassName);
			}
		}
	}

	private void parseComment() throws GraphIOException {
		match(Token.COMMENT);
		String qName = matchQualifiedName(true);
		List<String> comments = new ArrayList<String>();
		comments.add(matchUtfString());
		while (lookAhead != Token.SEMICOLON) {
			comments.add(matchUtfString());
		}
		match(Token.SEMICOLON);
		if (commentData.containsKey(qName)) {
			commentData.get(qName).addAll(comments);
		} else {
			commentData.put(qName, comments);
		}
	}

	private void parsePackage() throws GraphIOException {
		match(Token.PACKAGE);
		currentPackageName = "";
		if (lookAhead == Token.SEMICOLON) {
			currentPackageName = "";
		} else {
			currentPackageName = matchPackageName() + ".";
		}
		match(Token.SEMICOLON);
	}

	/**
	 * Creates the GraphClass contained in the Schema along with its
	 * GraphElementClasses.
	 * 
	 * @throws GraphIOException
	 */
	private void completeGraphClass() throws GraphIOException {
		GraphClass currentGraphClass = createGraphClass(graphClass);
		for (GraphElementClassData currentGraphElementClassData : vertexClassBuffer
				.get(graphClass.name)) {
			createVertexClass(currentGraphElementClassData, currentGraphClass);
		}
		for (GraphElementClassData currentGraphElementClassData : edgeClassBuffer
				.get(graphClass.name)) {
			createEdgeClass(currentGraphElementClassData, currentGraphClass);
		}
	}

	/**
	 * Reads a GraphClass from a TG-file.
	 * 
	 * @return The name of the read GraphClass.
	 * @throws GraphIOException
	 */
	private String parseGraphClass() throws GraphIOException {
		match(Token.GRAPHCLASS);
		graphClass = new GraphClassData();

		graphClass.name = matchSimpleName(true);
		if (lookAhead == Token.LCRL) {
			graphClass.attributes = parseAttributes();
		}

		if (lookAhead == Token.LSQ) {
			// There are constraints
			graphClass.constraints = parseConstraints();
		}

		match(Token.SEMICOLON);

		vertexClassBuffer.put(graphClass.name,
				new ArrayList<GraphElementClassData>());
		edgeClassBuffer.put(graphClass.name,
				new ArrayList<GraphElementClassData>());

		return graphClass.name;
	}

	/**
	 * Creates a GraphClass based on the given GraphClassData.
	 * 
	 * @param gcData
	 *            The GraphClassData used to create the GraphClass.
	 * @return The created GraphClass.
	 * @throws GraphIOException
	 */
	private GraphClass createGraphClass(GraphClassData gcData)
			throws GraphIOException {
		GraphClass gc = schema.createGraphClass(gcData.name);

		gc.setAbstract(gcData.isAbstract);

		addAttributes(gcData.attributes, gc);

		for (Constraint constraint : gcData.constraints) {
			gc.addConstraint(constraint);
		}

		return gc;
	}

	/**
	 * Reads the direct superclasses of a GraphClass or a GraphElementClass from
	 * the TG-file.
	 * 
	 * @return A list of the direct super classes.
	 * @throws GraphIOException
	 */
	private List<String> parseHierarchy() throws GraphIOException {
		List<String> hierarchy = new LinkedList<String>();
		match(Token.COLON);
		String qn = matchQualifiedName();
		hierarchy.add(qn);
		while (lookAhead == Token.COMMA) {
			match();
			qn = matchQualifiedName();
			hierarchy.add(qn);
		}
		return hierarchy;
	}

	private List<AttributeData> parseAttributes() throws GraphIOException {
		List<AttributeData> attributesData = new ArrayList<AttributeData>();
		Set<String> names = new TreeSet<String>();

		match(Token.LCRL);
		AttributeData ad = new AttributeData();
		ad.name = matchSimpleName(false);
		match(Token.COLON);
		ad.domainDescription = parseAttrDomain();
		if (lookAhead == Token.EQ) {
			match();
			ad.defaultValue = matchUtfString();
		}
		attributesData.add(ad);
		names.add(ad.name);

		while (lookAhead == Token.COMMA) {
			match();
			ad = new AttributeData();
			ad.name = matchSimpleName(false);
			match(Token.COLON);
			ad.domainDescription = parseAttrDomain();
			if (lookAhead == Token.EQ) {
				match();
				ad.defaultValue = matchUtfString();
			}
			if (names.contains(ad.name)) {
				throw new GraphIOException(lexer.getLocation()
						+ "Duplicate attribute name '" + ad.name + "'");
			}
			attributesData.add(ad);
			names.add(ad.name);
		}
		match(Token.RCRL);
		return attributesData;
	}

	private void addAttributes(List<AttributeData> attributesData,
			AttributedElementClass<?, ?> aec) throws GraphIOException {
		for (AttributeData ad : attributesData) {
			aec.createAttribute(ad.name, attrDomain(ad.domainDescription),
					ad.defaultValue);
		}
	}

	private List<String> parseAttrDomain() throws GraphIOException {
		List<String> result = new ArrayList<String>();
		parseAttrDomain(result);
		return result;
	}

	/**
	 * Reads an Attribute's domain from the TG-file and stores it in the list
	 * given as argument.
	 * 
	 * @param attrDomain
	 *            The list to which an attribute's domain shall be added.
	 * @throws GraphIOException
	 */
	private void parseAttrDomain(List<String> attrDomain)
			throws GraphIOException {
		if ((lookAhead == Token.LIST) || (lookAhead == Token.LIST2)) {
			match();
			match(Token.LT);
			attrDomain.add("List<");
			parseAttrDomain(attrDomain);
			match(Token.GT);
		} else if ((lookAhead == Token.SET) || (lookAhead == Token.SET2)) {
			match();
			match(Token.LT);
			attrDomain.add("Set<");
			parseAttrDomain(attrDomain);
			match(Token.GT);
		} else if ((lookAhead == Token.MAP) || (lookAhead == Token.MAP2)) {
			match();
			match(Token.LT);
			attrDomain.add("Map<");
			parseAttrDomain(attrDomain);
			match(Token.COMMA);
			parseAttrDomain(attrDomain);
			match(Token.GT);
		} else {
			String dom = lexer.getText();
			if (isBasicDomainName(dom)) {
				attrDomain.add(dom);
				match();
			} else {
				String qn = matchQualifiedName();
				attrDomain.add(qn);
			}
		}
	}

	private boolean isBasicDomainName(String s) {
		// Basic domains may have a leading "." to indicate their membership in
		// the default package.
		return BasicDomainImpl.isBasicDomain(s.startsWith(".") ? s.substring(1)
				: s);
	}

	/**
	 * Creates a Domain corresponding to a list of domain names representing a,
	 * probably composite, domain.
	 * 
	 * @param domainNames
	 *            The list containing the names of, probably composite, domains.
	 * @return The created Domain.
	 * @throws GraphIOException
	 */
	private Domain attrDomain(List<String> domainNames) throws GraphIOException {
		Iterator<String> it = domainNames.iterator();
		String domainName;
		while (it.hasNext()) {
			domainName = it.next();
			it.remove();
			if (domainName.equals("List<")) {
				try {
					return schema.createListDomain(attrDomain(domainNames));
				} catch (SchemaException e) {
					throw new GraphIOException(lexer.getLocation()
							+ "Can't create list domain.", e);
				}
			} else if (domainName.equals("Set<")) {
				try {
					return schema.createSetDomain(attrDomain(domainNames));
				} catch (SchemaException e) {
					throw new GraphIOException(lexer.getLocation()
							+ "Can't create set domain.", e);
				}
			} else if (domainName.equals("Map<")) {
				try {
					Domain keyDomain = attrDomain(domainNames);
					Domain valueDomain = attrDomain(domainNames);
					if (keyDomain == null) {
						throw new GraphIOException(
								lexer.getLocation()
										+ "Can't create map domain, because no key domain was specified.");
					}
					MapDomain result = schema.createMapDomain(keyDomain,
							valueDomain);
					// System.out.println("result = Map<"
					// + keyDomain.getQualifiedName() + ", "
					// + valueDomain.getQualifiedName() + ">");
					return result;
				} catch (SchemaException e) {
					throw new GraphIOException(lexer.getLocation()
							+ "Can't create map domain.", e);
				}
			} else {
				Domain result = schema.getDomain(domainName);
				if (result == null) {
					throw new GraphIOException(lexer.getLocation()
							+ "Undefined domain '" + domainName + "'");
				}
				return result;
			}
		}
		throw new GraphIOException(lexer.getLocation()
				+ "Couldn't create domain for '" + domainNames + "'");
	}

	public final String matchEnumConstant() throws GraphIOException {
		if (lookAhead == Token.NULL_LITERAL) {
			match();
			return null;
		}
		String c = lexer.getText();
		if (schema.isValidEnumConstant(c)) {
			match();
			return c;
		}
		throw new GraphIOException(lexer.getLocation()
				+ "Invalid enumeration constant '" + lexer.getText() + "'");
	}

	/**
	 * Reads the a GraphElementClass of the GraphClass indicated by the given
	 * name.
	 * 
	 * @throws GraphIOException
	 */
	private void parseGraphElementClass(String gcName) throws GraphIOException {
		GraphElementClassData graphElementClassData = new GraphElementClassData();

		if (lookAhead == Token.ABSTRACT) {
			match();
			graphElementClassData.isAbstract = true;
		}

		if (lookAhead == Token.VERTEXCLASS) {
			match();
			String[] qn = matchAndSplitQualifiedName();
			graphElementClassData.packageName = qn[0];
			graphElementClassData.simpleName = qn[1];
			if (lookAhead == Token.COLON) {
				graphElementClassData.directSuperClasses = parseHierarchy();
			}
			vertexClassBuffer.get(gcName).add(graphElementClassData);
		} else if (lookAhead == Token.EDGECLASS) {
			match();
			String[] qn = matchAndSplitQualifiedName();
			graphElementClassData.packageName = qn[0];
			graphElementClassData.simpleName = qn[1];
			if (lookAhead == Token.COLON) {
				graphElementClassData.directSuperClasses = parseHierarchy();
			}
			match(Token.FROM);
			graphElementClassData.fromVertexClassName = matchQualifiedName();
			graphElementClassData.fromMultiplicity = parseMultiplicity();
			graphElementClassData.fromRoleName = parseRoleName();
			graphElementClassData.fromAggregation = parseAggregation();

			match(Token.TO);
			graphElementClassData.toVertexClassName = matchQualifiedName();
			graphElementClassData.toMultiplicity = parseMultiplicity();
			graphElementClassData.toRoleName = parseRoleName();
			graphElementClassData.toAggregation = parseAggregation();
			edgeClassBuffer.get(gcName).add(graphElementClassData);
		} else {
			throw new GraphIOException(lexer.getLocation()
					+ "Unexpected symbol '" + lexer.getText() + "'");
		}

		if (lookAhead == Token.LCRL) {
			graphElementClassData.attributes = parseAttributes();
		}

		if (lookAhead == Token.LSQ) {
			// There are constraints
			graphElementClassData.constraints = parseConstraints();
		}
		match(Token.SEMICOLON);
	}

	private Set<Constraint> parseConstraints() throws GraphIOException {
		// constraints have the form: ["msg" "pred" "optGreql"] or ["msg"
		// "pred"] and there may be as many as one wants...
		Set<Constraint> constraints = new TreeSet<Constraint>();
		do {
			match(Token.LSQ);
			String msg = matchUtfString();
			String pred = matchUtfString();
			String greql = null;
			if (lookAhead != Token.RSQ) {
				greql = matchUtfString();
			}
			constraints.add(new ConstraintImpl(msg, pred, greql));
			match(Token.RSQ);
		} while (lookAhead == Token.LSQ);
		return constraints;
	}

	private VertexClass createVertexClass(GraphElementClassData vcd,
			GraphClass gc) throws GraphIOException {
		VertexClass vc = gc.createVertexClass(vcd.getQualifiedName());
		vc.setAbstract(vcd.isAbstract);

		addAttributes(vcd.attributes, vc);

		for (Constraint constraint : vcd.constraints) {
			vc.addConstraint(constraint);
		}
		return vc;
	}

	private EdgeClass createEdgeClass(GraphElementClassData ecd, GraphClass gc)
			throws GraphIOException {
		EdgeClass ec = gc.createEdgeClass(ecd.getQualifiedName(),
				gc.getVertexClass(ecd.fromVertexClassName),
				ecd.fromMultiplicity[0], ecd.fromMultiplicity[1],
				ecd.fromRoleName, ecd.fromAggregation,
				gc.getVertexClass(ecd.toVertexClassName),
				ecd.toMultiplicity[0], ecd.toMultiplicity[1], ecd.toRoleName,
				ecd.toAggregation);

		addAttributes(ecd.attributes, ec);

		for (Constraint constraint : ecd.constraints) {
			ec.addConstraint(constraint);
		}

		ec.setAbstract(ecd.isAbstract);
		return ec;
	}

	/**
	 * Reads a multiplicity of an EdgeClass.
	 * 
	 * @return An array with two elements. The first element represents the
	 *         multiplicity's lower bound. The second element represents the
	 *         upper bound.
	 * @throws GraphIOException
	 */
	private int[] parseMultiplicity() throws GraphIOException {
		int[] multis = new int[2];

		match(Token.LBR);
		int min = matchInteger();
		if (min < 0) {
			throw new GraphIOException(lexer.getLocation()
					+ "Minimum multiplicity '" + min + "' must be >=0");
		}
		match(Token.COMMA);
		int max;
		if (lookAhead == Token.ASTERISK) {
			max = Integer.MAX_VALUE;
			match();
		} else {
			max = matchInteger();
			if (max < min) {
				throw new GraphIOException(lexer.getLocation()
						+ "Maximum multiplicity '" + max + "' must be * or >="
						+ min);
			}
		}
		match(Token.RBR);
		multis[0] = min;
		multis[1] = max;
		return multis;
	}

	/**
	 * Reads a role name of an EdgeClass.
	 * 
	 * @return A role name.
	 * @throws GraphIOException
	 */
	private String parseRoleName() throws GraphIOException {
		if (lookAhead == Token.ROLE) {
			match();
			String result = matchSimpleName(false);
			return result;
		}
		return "";
	}

	private AggregationKind parseAggregation() throws GraphIOException {
		if (lookAhead != Token.AGGREGATION) {
			return AggregationKind.NONE;
		}
		match();
		if (lookAhead == Token.NONE) {
			match();
			return AggregationKind.NONE;
		} else if (lookAhead == Token.SHARED) {
			match();
			return AggregationKind.SHARED;
		} else if (lookAhead == Token.COMPOSITE) {
			match();
			return AggregationKind.COMPOSITE;
		} else {
			throw new GraphIOException(
					lexer.getLocation()
							+ "Invalid aggregation: expected 'none', 'shared', or 'composite', but found '"
							+ lexer.getText() + "'");
		}
	}

	private static boolean isValidIdentifier(String s,
			boolean startsWithUppercase) {
		if (s == null) {
			return false;
		}
		int l = s.length();
		if (l == 0) {
			return false;
		}
		char c = s.charAt(0);
		if (startsWithUppercase) {
			if ((c < 'A') || (c > 'Z')) {
				return false;
			}
		} else {
			if ((c < 'a') || (c > 'z')) {
				return false;
			}
		}
		for (int i = 1; i < l; ++i) {
			c = s.charAt(i);
			if (!(((c >= 'A') && (c <= 'Z')) || ((c >= 'a') && (c <= 'z'))
					|| ((c >= '0') && (c <= '9')) || (c == '_'))) {
				return false;
			}
		}
		return true;
	}

	private List<ComponentData> parseRecordComponents() throws GraphIOException {
		List<ComponentData> componentsData = new ArrayList<ComponentData>();
		Set<String> names = new TreeSet<String>();

		match(Token.LBR);
		ComponentData cd = new ComponentData();
		cd.name = matchSimpleName(false);
		match(Token.COLON);
		cd.domainDescription = parseAttrDomain();
		componentsData.add(cd);
		names.add(cd.name);

		while (lookAhead == Token.COMMA) {
			match();
			cd = new ComponentData();
			cd.name = matchSimpleName(false);
			match(Token.COLON);
			cd.domainDescription = parseAttrDomain();
			if (names.contains(cd.name)) {
				throw new GraphIOException(lexer.getLocation()
						+ "Duplicate record component name '" + cd.name + "'");
			}
			componentsData.add(cd);
			names.add(cd.name);
		}
		match(Token.RBR);
		return componentsData;
	}

	/**
	 * Reads the constants of an EnumDomain. Duplicate constant names are
	 * rejected.
	 * 
	 * @return A list of String containing the constants.
	 * @throws GraphIOException
	 *             if duplicate constant names are read.
	 */
	private List<String> parseEnumConstants() throws GraphIOException {
		match(Token.LBR);
		List<String> enums = new ArrayList<String>();
		String c = matchEnumConstant();
		if (c == null) {
			throw new GraphIOException(lexer.getLocation() + "'" + NULL_LITERAL
					+ "' can not be used as enumeration constant");
		}
		enums.add(c);
		while (lookAhead == Token.COMMA) {
			match();
			String s = matchEnumConstant();
			if (enums.contains(s)) {
				throw new GraphIOException(lexer.getLocation()
						+ "Duplicate enumeration constant name '" + lookAhead
						+ "'");
			}
			enums.add(s);
		}
		match(Token.RBR);
		return enums;
	}

	private void buildVertexClassHierarchy() throws GraphIOException {
		for (Entry<String, List<GraphElementClassData>> gcElements : vertexClassBuffer
				.entrySet()) {
			for (GraphElementClassData vData : gcElements.getValue()) {
				AttributedElementClass<?, ?> aec = schema
						.getAttributedElementClass(vData.getQualifiedName());
				if (aec == null) {
					throw new GraphIOException(
							"Undefined AttributedElementClass '"
									+ vData.getQualifiedName() + "'");
				}
				if (aec instanceof VertexClass) {
					VertexClass vc = (VertexClass) aec;
					for (String superClassName : vData.directSuperClasses) {
						VertexClass superClass = vc.getGraphClass()
								.getVertexClass(superClassName);
						if (superClass == null) {
							throw new GraphIOException(
									"Undefined VertexClass '" + superClassName
											+ "'");
						}
						vc.addSuperClass(superClass);
					}
				}
			}
		}
	}

	private void buildEdgeClassHierarchy() throws GraphIOException {
		for (Entry<String, List<GraphElementClassData>> gcElements : edgeClassBuffer
				.entrySet()) {
			for (GraphElementClassData eData : gcElements.getValue()) {
				AttributedElementClass<?, ?> aec = schema
						.getAttributedElementClass(eData.getQualifiedName());
				if (aec == null) {
					throw new GraphIOException(
							"Undefined AttributedElementClass '"
									+ eData.getQualifiedName() + "'");
				}
				if (!(aec instanceof EdgeClass)) {
					throw new GraphIOException("Expected EdgeClass '"
							+ eData.getQualifiedName() + "', but it's a "
							+ aec.getSchemaClass().getSimpleName());
				}
				EdgeClass ec = (EdgeClass) aec;
				for (String superClassName : eData.directSuperClasses) {
					EdgeClass superClass = ec.getGraphClass().getEdgeClass(
							superClassName);
					if (superClass == null) {
						throw new GraphIOException("Undefined EdgeClass '"
								+ superClassName + "'");
					}
					ec.addSuperClass(superClass);
				}
			}
		}
	}

	private void buildHierarchy() throws GraphIOException {
		buildVertexClassHierarchy();
		buildEdgeClassHierarchy();
	}

	public final boolean isNextToken(Token token) {
		return lookAhead == token;
	}

	public final void match() throws GraphIOException {
		lookAhead = lexer.nextToken();
	}

	public final void match(Token t) throws GraphIOException {
		if (lookAhead == t) {
			lookAhead = lexer.nextToken();
		} else {
			throw new GraphIOException(lexer.getLocation() + "Expected " + t
					+ " but found '" + lexer.getText() + "'");
		}
	}

	public final String matchGetText(Token t) throws GraphIOException {
		if (lookAhead == t) {
			String text = lexer.getText();
			lookAhead = lexer.nextToken();
			return text;
		} else {
			throw new GraphIOException(lexer.getLocation() + "Expected " + t
					+ " but found " + lexer.getText() + "'");
		}
	}

	public final int matchInteger() throws GraphIOException {
		int result = lookAhead == Token.INT ? lexer.getInt() : 0;
		match(Token.INT);
		return result;
	}

	public final long matchLong() throws GraphIOException {
		long result = lookAhead == Token.INT ? lexer.getLong() : 0;
		match(Token.INT);
		return result;
	}

	/**
	 * Parses an identifier, checks it for validity and returns it.
	 * 
	 * @param startsWithUppercase
	 *            If true, the identifier must begin with an uppercase character
	 * @return the parsed identifier
	 * @throws GraphIOException
	 */
	public final String matchSimpleName(boolean startsWithUppercase)
			throws GraphIOException {
		String s = lexer.getText();
		if (!isValidIdentifier(s, startsWithUppercase)) {
			throw new GraphIOException(lexer.getLocation()
					+ "Invalid simple name '" + lexer.getText() + "'");
		}
		match();
		return s;
	}

	/**
	 * Parses an identifier, checks it for validity and returns it.
	 * 
	 * @return An array of the form {parentPackage, simpleName}
	 * @throws GraphIOException
	 */
	public final String matchQualifiedName() throws GraphIOException {
		return matchQualifiedName(false);
	}

	public final String matchQualifiedName(boolean packageNameAllowed)
			throws GraphIOException {
		String qn = lexer.getText();
		int l = qn.length();
		String result = null;
		if ((l > 0) && (qn.charAt(l - 1) != '.')) {
			int e = qn.indexOf('.');
			if (e < 0) {
				// unqualified simple name, prepend current package name
				if (isValidIdentifier(qn, true)
						|| (packageNameAllowed && isValidIdentifier(qn, false))) {
					result = currentPackageName + qn;
				}
			} else if (e == 0) {
				// simple name in default package (.SimpleName)
				String sn = qn.substring(1);
				if (isValidIdentifier(sn, true)
						|| (packageNameAllowed && isValidIdentifier(sn, false))) {
					result = sn;
				}
			} else {
				// qualified, not default package (packagename.SimpleName)
				int s = 0;
				boolean ok = true;
				// check package name parts
				while (ok && (e >= 0)) {
					String pn = qn.substring(s, e);
					ok = ok && isValidIdentifier(pn, false);
					if (ok) {
						s = e + 1;
						e = qn.indexOf('.', s);
					}
				}
				// simple name starts at position s
				ok = ok
						&& (isValidIdentifier(qn.substring(s), true) || (packageNameAllowed && isValidIdentifier(
								qn.substring(s), false)));
				if (ok) {
					result = qn;
				}
			}
		}
		if (result == null) {
			throw new GraphIOException(lexer.getLocation()
					+ "Invalid qualified name '" + qn + "'");
		}
		match();
		return result;
	}

	public final String[] matchAndSplitQualifiedName() throws GraphIOException {
		String qn = matchQualifiedName();
		int p = qn.lastIndexOf('.');
		return new String[] { p <= 0 ? "" : qn.substring(0, p),
				qn.substring(p + 1) };
	}

	public final String matchPackageName() throws GraphIOException {
		String pn = lexer.getText();
		int l = pn.length();
		boolean ok = (l > 0) && (pn.charAt(0) != '.')
				&& (pn.charAt(l - 1) != '.');
		if (ok) {
			int s = 0;
			int e = pn.indexOf('.');
			while (ok && (e >= 0)) {
				ok = ok && isValidIdentifier(pn.substring(s, e), false);
				s = e + 1;
				e = pn.indexOf('.', s);
			}
			ok = ok && isValidIdentifier(pn.substring(s), false);
		}
		if (!ok) {
			throw new GraphIOException(lexer.getLocation()
					+ "Invalid package name '" + pn + "'");
		}
		match();
		return pn;
	}

	/**
	 * @param pn
	 *            package name
	 * @param sn
	 *            simple name
	 * @return a string representation of a qualified name specified as package
	 *         name and simple name.
	 */
	private final String toQNameString(String pn, String sn) {
		if ((pn == null) || pn.isEmpty()) {
			return sn;
		}
		return pn + "." + sn;
	}

	public final String matchUtfString() throws GraphIOException {
		if (lookAhead == Token.NULL_LITERAL) {
			match();
			return null;
		}
		String result = (lookAhead == Token.STRING) ? lexer.getText() : null;
		match(Token.STRING);
		String s = stringPool.get(result);
		if (s == null) {
			stringPool.put(result, result);
		} else {
			result = s;
		}
		return result;
	}

	public final boolean matchBoolean() throws GraphIOException {
		if ((lookAhead != Token.TRUE_LITERAL)
				&& (lookAhead != Token.FALSE_LITERAL)) {
			throw new GraphIOException(lexer.getLocation()
					+ "Expected a boolean constant ('f' or 't') but found '"
					+ lexer.getText() + "'");
		}
		boolean result = lookAhead == Token.TRUE_LITERAL;
		match();
		return result;
	}

	private GraphBaseImpl graph(ProgressFunction pf) throws GraphIOException {
		currentPackageName = "";
		match(Token.GRAPH);
		String graphId = matchUtfString();
		long graphVersion = matchLong();

		gcName = matchSimpleName(true);
		// check if classname is known in the schema
		if (!schema.getGraphClass().getQualifiedName().equals(gcName)) {
			throw new GraphIOException(lexer.getLocation() + "Graph Class "
					+ gcName + "does not exist in " + schema.getQualifiedName());
		}
		match(Token.LBR);
		int maxV = matchInteger();
		int maxE = matchInteger();
		int vCount = matchInteger();
		int eCount = matchInteger();
		match(Token.RBR);

		// verify vCount <= maxV && eCount <= maxE
		if (vCount > maxV) {
			throw new GraphIOException(lexer.getLocation()
					+ "Number of vertices in graph (" + vCount
					+ ") exceeds maximum number of vertices (" + maxV + ")");
		}
		if (eCount > maxE) {
			throw new GraphIOException(lexer.getLocation()
					+ "Number of edges in graph (" + eCount
					+ ") exceeds maximum number of edges (" + maxE + ")");
		}

		// adjust fields for incidences
		edgeIn = new Vertex[maxE + 1];
		edgeOut = new Vertex[maxE + 1];
		firstIncidence = new int[maxV + 1];
		nextIncidence = new int[(2 * maxE) + 1];
		edgeOffset = maxE;

		long graphElements = 0, currentCount = 0, interval = 1;
		if (pf != null) {
			pf.init(vCount + eCount);
			interval = pf.getUpdateInterval();
		}
		GraphBaseImpl graph = graphFactory.createGraph(schema.getGraphClass(),
				graphId, maxV, maxE);
		graph.setLoading(true);
		graph.readAttributeValues(this);
		match(Token.SEMICOLON);

		int vNo = 1;
		while (vNo <= vCount) {
			if (lookAhead == Token.PACKAGE) {
				parsePackage();
			} else {
				vertexDesc(graph);
				// update progress bar
				if (pf != null) {
					graphElements++;
					currentCount++;
					if (currentCount == interval) {
						pf.progress(graphElements);
						currentCount = 0;
					}
				}
				++vNo;
			}
		}

		int eNo = 1;
		while (eNo <= eCount) {
			if (lookAhead == Token.PACKAGE) {
				parsePackage();
			} else {
				edgeDesc(graph);
				// update progress bar
				if (pf != null) {
					graphElements++;
					currentCount++;
					if (currentCount == interval) {
						pf.progress(graphElements);
						currentCount = 0;
					}
				}
				++eNo;
			}
		}

		graph.setGraphVersion(graphVersion);
		graph.internalLoadingCompleted(firstIncidence, nextIncidence);
		firstIncidence = null;
		nextIncidence = null;
		graph.setLoading(false);
		if (pf != null) {
			pf.finished();
		}
		graph.loadingCompleted();
		return graph;
	}

	public final double matchDouble() throws GraphIOException {
		try {
			double result = Double.parseDouble(lexer.getText());
			match();
			return result;
		} catch (NumberFormatException e) {
			throw new GraphIOException(lexer.getLocation()
					+ "Expected double value but found '" + lexer.getText()
					+ "'", e);
		}
	}

	private void vertexDesc(Graph graph) throws GraphIOException {
		int vId = vId();
		String vcName = matchQualifiedName();
		VertexClass vc = (VertexClass) schema.getAttributedElementClass(vcName);
		Vertex vertex = graphFactory.createVertex(vc, vId, graph);
		parseIncidentEdges(vertex);
		((InternalAttributedElement) vertex).readAttributeValues(this);
		match(Token.SEMICOLON);
	}

	private void edgeDesc(Graph graph) throws GraphIOException {
		int eId = eId();
		String ecName = matchQualifiedName();
		EdgeClass ec = (EdgeClass) schema.getAttributedElementClass(ecName);
		Edge edge = graphFactory.createEdge(ec, eId, graph, edgeOut[eId],
				edgeIn[eId]);
		((InternalAttributedElement) edge).readAttributeValues(this);
		match(Token.SEMICOLON);
	}

	private int eId() throws GraphIOException {
		int eId = matchInteger();
		if (eId == 0) {
			throw new GraphIOException(lexer.getLocation() + "Invalid edge id "
					+ eId + ".");
		}
		return eId;
	}

	private int vId() throws GraphIOException {
		int vId = matchInteger();
		if (vId <= 0) {
			throw new GraphIOException(lexer.getLocation()
					+ "Invalid vertex id " + vId + ".");
		} else {
			return vId;
		}
	}

	private void parseIncidentEdges(Vertex v) throws GraphIOException {
		int eId = 0;
		int prevId = 0;
		int vId = v.getId();
		match(Token.LT);
		if (lookAhead != Token.GT) {
			eId = eId();
			firstIncidence[vId] = eId;
			if (eId < 0) {
				edgeIn[-eId] = v;
			} else {
				edgeOut[eId] = v;
			}
		}
		while (lookAhead != Token.GT) {
			prevId = eId;
			eId = eId();
			nextIncidence[edgeOffset + prevId] = eId;
			if (eId < 0) {
				edgeIn[-eId] = v;
			} else {
				edgeOut[eId] = v;
			}
		}
		match();
	}

	/**
	 * Converts a String value with arbitrary characters to a quoted string
	 * value containing only ASCII characters and escaped unicode sequences as
	 * required by the TG file format.
	 * 
	 * @param value
	 *            a string
	 * @return a quoted string suitable for storage in TG files.
	 */
	public static String toUtfString(String value) {
		if (value == null) {
			return "";
		}
		StringBuilder out = new StringBuilder("\""); // "
		CharBuffer cb = CharBuffer.wrap(value);
		char c;
		while (cb.hasRemaining()) {
			c = cb.get();
			switch (c) {
			case '"':
				out.append("\\\"");
				break;
			case '\n':
				out.append("\\n");
				break;
			case '\r':
				out.append("\\r");
				break;
			case '\\':
				out.append("\\\\");
				break;
			case '\t':
				out.append("\\t");
				break;
			default:
				if ((c >= 32) && (c <= 127)) {
					out.append(c);
				} else {
					out.append("\\u");
					String s = Integer.toHexString(c);
					switch (s.length()) {
					case 1:
						out.append("000");
						break;
					case 2:
						out.append("00");
						break;
					case 3:
						out.append("0");
						break;
					}
					out.append(s);
				}
			}
		}
		out.append("\"");
		return out.toString();
	}

	private void sortRecordDomains() throws GraphIOException {
		List<RecordDomainData> orderedRdList = new ArrayList<RecordDomainData>();
		boolean componentDomsInOrderedList = true;
		RecordDomainData rd;
		boolean definedRdName;

		// iteratively add domains from recordDomainBuffer,
		// whose component domains already are in topologicalOrderList,
		// to topologicalOrderList
		// the added domains are removed from recordDomainBuffer
		while (!recordDomainBuffer.isEmpty()) {
			for (Iterator<RecordDomainData> rdit = recordDomainBuffer
					.iterator(); rdit.hasNext();) {
				rd = rdit.next();
				componentDomsInOrderedList = true;
				for (ComponentData comp : rd.components) {
					for (String componentDomain : comp.domainDescription) {
						if (componentDomain.equals("String")
								|| componentDomain.equals("Integer")
								|| componentDomain.equals("Boolean")
								|| componentDomain.equals("Long")
								|| componentDomain.equals("Double")
								|| componentDomain.equals("Set<")
								|| componentDomain.equals("List<")
								|| componentDomain.equals("Map<")) {
							continue;
						}
						componentDomsInOrderedList = false;
						for (RecordDomainData orderedRd : orderedRdList) {
							String qName = toQNameString(orderedRd.packageName,
									orderedRd.simpleName);
							if (componentDomain.equals(qName)) {
								componentDomsInOrderedList = true;
								break;
							}
						}
						for (EnumDomainData ed : enumDomainBuffer) {
							String qName = toQNameString(ed.packageName,
									ed.simpleName);
							if (componentDomain.equals(qName)) {
								componentDomsInOrderedList = true;
								break;
							}
						}

						/*
						 * check if component domain exists among yet unsorted
						 * domains
						 */
						if (!componentDomsInOrderedList) {
							definedRdName = false;

							for (RecordDomainData rd2 : recordDomainBuffer) {
								String qName = toQNameString(rd2.packageName,
										rd2.simpleName);
								if (qName.equals(componentDomain)) {
									definedRdName = true;
									break;
								}
							}
							if (!definedRdName) {
								throw new GraphIOException("Domain "
										+ componentDomain + " does not exist");
							}
							break;
						}
					}
					if (!componentDomsInOrderedList) {
						break;
					}
				}
				if (componentDomsInOrderedList) {
					orderedRdList.add(rd);
					rdit.remove();
				}
			}
		}
		recordDomainBuffer = orderedRdList;
	}

	private void sortVertexClasses() throws GraphIOException {
		List<GraphElementClassData> orderedVcList, unorderedVcList;
		Set<String> orderedVcNames = new TreeSet<String>();
		GraphElementClassData vc;
		boolean definedVcName;

		unorderedVcList = vertexClassBuffer.get(graphClass.name);
		orderedVcList = new ArrayList<GraphElementClassData>();

		// iteratively add VertexClasses from vertexClassBuffer,
		// whose superclasses already are in orderedVcList,
		// to orderedVcList
		// the added VertexClasses are removed from vertexClassBuffer
		while (!unorderedVcList.isEmpty()) {
			for (Iterator<GraphElementClassData> vcit = unorderedVcList
					.iterator(); vcit.hasNext();) {
				vc = vcit.next();
				// check if all superclasses exist among already sorted
				// VertexClasses
				if (orderedVcNames.containsAll(vc.directSuperClasses)) {
					orderedVcNames.add(vc.getQualifiedName());
					orderedVcList.add(vc);
					vcit.remove();
				} else {
					/*
					 * check if some superclasses exist among yet unsorted
					 * VertexClasses
					 */
					for (String superClass : vc.directSuperClasses) {
						if (orderedVcNames.contains(superClass)) {
							continue;
						}
						definedVcName = false;
						for (GraphElementClassData vc2 : unorderedVcList) {
							if (vc2.getQualifiedName().equals(superClass)) {
								definedVcName = true;
								break;
							}
						}
						if (!definedVcName) {
							throw new GraphIOException("VertexClass "
									+ superClass + " does not exist");
						}
					}
				}
			}
		}
		vertexClassBuffer.put(graphClass.name, orderedVcList);
	}

	private void sortEdgeClasses() throws GraphIOException {
		List<GraphElementClassData> orderedEcList, unorderedEcList;
		Set<String> orderedEcNames = new TreeSet<String>();
		GraphElementClassData ec;
		boolean definedEcName;

		unorderedEcList = edgeClassBuffer.get(graphClass.name);
		orderedEcList = new ArrayList<GraphElementClassData>();

		// iteratively add EdgeClasses from edgeClassBuffer,
		// whose superclasses already are in orderedEcList,
		// to orderedEcList
		// the added EdgeClasses are removed from edgeClassBuffer
		while (!unorderedEcList.isEmpty()) {
			for (Iterator<GraphElementClassData> ecit = unorderedEcList
					.iterator(); ecit.hasNext();) {
				ec = ecit.next();
				// check if all superclasses exist among already sorted
				// EdgeClasses
				if (orderedEcNames.containsAll(ec.directSuperClasses)) {
					orderedEcNames.add(ec.getQualifiedName());
					orderedEcList.add(ec);
					ecit.remove();
				} else {
					/*
					 * check if superclasses exist among yet unsorted
					 * EdgeClasses
					 */
					for (String superClass : ec.directSuperClasses) {
						if (orderedEcNames.contains(superClass)) {
							continue;
						}
						definedEcName = false;
						for (GraphElementClassData ec2 : unorderedEcList) {
							if (ec2.getQualifiedName().equals(superClass)) {
								definedEcName = true;
								break;
							}
						}
						if (!definedEcName) {
							throw new GraphIOException("EdgeClass "
									+ superClass + " does not exist");
						}
					}
				}
			}
		}
		edgeClassBuffer.put(graphClass.name, orderedEcList);
	}

	/**
	 * checks if from- and to-VertexClasses given in EdgeClass definitions exist
	 */
	private void checkFromToVertexClasses() throws GraphIOException {
		boolean existingFromVertexClass;
		boolean existingToVertexClass;

		for (Entry<String, List<GraphElementClassData>> graphClassEdge : edgeClassBuffer
				.entrySet()) {
			for (GraphElementClassData ec : graphClassEdge.getValue()) {
				existingFromVertexClass = false;
				existingToVertexClass = false;

				for (Entry<String, List<GraphElementClassData>> graphClassVertex : vertexClassBuffer
						.entrySet()) {
					for (GraphElementClassData vc : graphClassVertex.getValue()) {
						if (ec.fromVertexClassName
								.equals(vc.getQualifiedName())) {
							existingFromVertexClass = true;
						}
						if (ec.toVertexClassName.equals(vc.getQualifiedName())) {
							existingToVertexClass = true;
						}
						if (existingFromVertexClass && existingToVertexClass) {
							break;
						}
					}
					if (existingFromVertexClass && existingToVertexClass) {
						break;
					}
				}
				if (!existingFromVertexClass) {
					throw new GraphIOException("From-VertexClass "
							+ ec.fromVertexClassName + " at EdgeClass "
							+ ec.getQualifiedName() + " does not exist.");
				}
				if (!existingToVertexClass) {
					throw new GraphIOException("To-VertexClass "
							+ ec.toVertexClassName + " at EdgeClass "
							+ ec.getQualifiedName() + " does not exist.");
				}
			}
		}
	}

	/**
	 * EnumDomainData contains the parsed data of an EnumDomain. This data is
	 * used to create an EnumDomain.
	 */
	private static class EnumDomainData {
		String simpleName;
		String packageName;

		List<String> enumConstants;

		EnumDomainData(String packageName, String simpleName,
				List<String> enumConstants) {
			this.packageName = packageName;
			this.simpleName = simpleName;
			this.enumConstants = enumConstants;
		}
	}

	/**
	 * RecordDomainData contains the parsed data of a RecordDomain. This data is
	 * used to create a RecordDomain.
	 */
	private static class RecordDomainData {
		String simpleName;
		String packageName;
		List<ComponentData> components;

		RecordDomainData(String packageName, String simpleName,
				List<ComponentData> components) {
			this.packageName = packageName;
			this.simpleName = simpleName;
			this.components = components;
		}
	}

	private static class ComponentData {
		String name;
		List<String> domainDescription;
	}

	private static class AttributeData {
		String name;
		List<String> domainDescription;
		String defaultValue;
	}

	/**
	 * GraphClassData contains the parsed data of a GraphClass. This data is
	 * used to create a GraphClass.
	 */
	private static class GraphClassData {
		Set<Constraint> constraints = new HashSet<Constraint>(1);
		String name;
		boolean isAbstract = false;
		List<AttributeData> attributes = new ArrayList<AttributeData>();
	}

	/**
	 * GraphElementClassData contains the parsed data of a GraphElementClass.
	 * This data is used to create a GraphElementClass.
	 */
	private class GraphElementClassData {
		String simpleName;
		String packageName;

		String getQualifiedName() {
			return toQNameString(packageName, simpleName);
		}

		boolean isAbstract = false;

		List<String> directSuperClasses = new LinkedList<String>();

		String fromVertexClassName;

		int[] fromMultiplicity = { 1, Integer.MAX_VALUE };

		String fromRoleName = "";

		AggregationKind fromAggregation;

		String toVertexClassName;

		int[] toMultiplicity = { 1, Integer.MAX_VALUE };

		String toRoleName = "";

		AggregationKind toAggregation;

		List<AttributeData> attributes = new ArrayList<AttributeData>();

		Set<Constraint> constraints = new HashSet<Constraint>(1);
	}
}
