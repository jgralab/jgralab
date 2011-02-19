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

package de.uni_koblenz.jgralab;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.sql.SQLException;
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
import java.util.logging.Logger;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import de.uni_koblenz.jgralab.codegenerator.CodeGeneratorConfiguration;
import de.uni_koblenz.jgralab.graphmarker.BooleanGraphMarker;
import de.uni_koblenz.jgralab.impl.GraphBaseImpl;
import de.uni_koblenz.jgralab.impl.db.GraphDatabase;
import de.uni_koblenz.jgralab.impl.db.GraphDatabaseException;
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
import de.uni_koblenz.jgralab.schema.exception.SchemaException;
import de.uni_koblenz.jgralab.schema.impl.BasicDomainImpl;
import de.uni_koblenz.jgralab.schema.impl.ConstraintImpl;
import de.uni_koblenz.jgralab.schema.impl.SchemaImpl;

/**
 * class for loading and storing schema and graphs in tg format
 * 
 * @author ist@uni-koblenz.de
 */
public class GraphIO {
	private static final int BUFFER_SIZE = 65536;
	/**
	 * TG File Version this GraphIO recognizes.
	 */
	public static int TGFILE_VERSION = 2;
	public static String NULL_LITERAL = "n";
	public static String TRUE_LITERAL = "t";
	public static String FALSE_LITERAL = "f";
	public static String TGRAPH_FILE_EXTENSION = ".tg";
	public static String TGRAPH_COMPRESSED_FILE_EXTENSION = ".tg.gz";

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

	private static Logger logger = Logger.getLogger(GraphIO.class.getName());

	private InputStream TGIn;

	private DataOutputStream TGOut;

	private Schema schema;

	/**
	 * Maps domain names to the respective Domains.
	 */
	private final Map<String, Domain> domains;

	/**
	 * Maps GraphElementClasses to their containing GraphClasses
	 */
	private final Map<GraphElementClass, GraphClass> GECsearch;

	private final Map<String, Method> createMethods;

	private int line; // line number

	private int la; // lookahead character

	private String lookAhead; // lookahead token

	private boolean isUtfString; // lookahead is UTF string

	private boolean writeSpace; // if true, a space is written in the next
	// writeXXX()

	private String gcName; // GraphClass name of the currently loaded graph

	private final byte buffer[];

	private int bufferPos;

	private int bufferSize;

	private Vertex edgeIn[], edgeOut[];
	private int[] firstIncidence;
	private int[] nextIncidence;

	private int edgeOffset;

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

	private int putBackChar;

	private String currentPackageName;

	private final Object[] vertexDescTempObject = { 0 };

	private final Object[] edgeDescTempObject = { 0, 0, 0 };
	private ByteArrayOutputStream BAOut;

	// stringPool allows re-use string values, saves memory if
	// multiple identical strings are used as attribute values
	private final HashMap<String, String> stringPool;

	private GraphIO() {
		domains = new TreeMap<String, Domain>();
		GECsearch = new HashMap<GraphElementClass, GraphClass>();
		createMethods = new HashMap<String, Method>();
		buffer = new byte[BUFFER_SIZE];
		bufferPos = 0;
		enumDomainBuffer = new HashSet<EnumDomainData>();
		recordDomainBuffer = new ArrayList<RecordDomainData>();
		graphClass = null;
		vertexClassBuffer = new TreeMap<String, List<GraphElementClassData>>();
		edgeClassBuffer = new TreeMap<String, List<GraphElementClassData>>();
		commentData = new HashMap<String, List<String>>();
		stringPool = new HashMap<String, String>();
		putBackChar = -1;
	}

	/**
	 * Saves the specified <code>schema</code> to the file named
	 * <code>filename</code>. When the <code>filename</code> ends with
	 * <code>.gz</code>, output will be GZIP compressed, otherwise uncompressed
	 * plain text.
	 * 
	 * @param filename
	 *            the name of the file
	 * @param schema
	 *            a schema
	 * @throws GraphIOException
	 *             if an IOException occurs
	 */
	public static void saveSchemaToFile(String filename, Schema schema)
			throws GraphIOException {
		DataOutputStream out = null;
		try {
			out = new DataOutputStream(new BufferedOutputStream(
					new FileOutputStream(new File(filename))));
			saveSchemaToStream(out, schema);
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
	 * @param out
	 *            a DataOutputStream
	 * @param schema
	 *            a schema
	 * @throws GraphIOException
	 *             if an IOException occurs
	 */
	public static void saveSchemaToStream(DataOutputStream out, Schema schema)
			throws GraphIOException {
		GraphIO io = new GraphIO();
		io.TGOut = out;
		try {
			io.saveHeader();
			io.saveSchema(schema);
			out.flush();
		} catch (IOException e) {
			throw new GraphException("exception while saving schema", e);
		}
	}

	private void saveSchema(Schema s) throws IOException {
		// TODO [rie] decide what to do if default schema is used
		schema = s;
		write("Schema");
		space();
		writeIdentifier(schema.getQualifiedName());
		write(";\n");

		// write graphclass
		GraphClass gc = schema.getGraphClass();
		write("GraphClass");
		space();
		writeIdentifier(gc.getSimpleName());
		writeAttributes(null, gc);
		writeConstraints(gc);
		write(";\n");
		writeComments(gc, gc.getSimpleName());

		Queue<de.uni_koblenz.jgralab.schema.Package> worklist = new LinkedList<de.uni_koblenz.jgralab.schema.Package>();
		worklist.offer(s.getDefaultPackage());
		while (!worklist.isEmpty()) {
			Package pkg = worklist.poll();
			worklist.addAll(pkg.getSubPackages().values());

			// write package declaration
			if (!pkg.isDefaultPackage()) {
				write("Package");
				space();
				writeIdentifier(pkg.getQualifiedName());
				write(";\n");
			}

			// write domains
			for (Domain dom : pkg.getDomains().values()) {
				if (dom instanceof EnumDomain) {
					EnumDomain ed = (EnumDomain) dom;
					write("EnumDomain");
					space();
					writeIdentifier(ed.getSimpleName());
					write(" (");
					for (Iterator<String> eit = ed.getConsts().iterator(); eit
							.hasNext();) {
						space();
						writeIdentifier(eit.next());
						if (eit.hasNext()) {
							write(",");
						}
					}
					write(" );\n");
					writeComments(ed, ed.getSimpleName());
				} else if (dom instanceof RecordDomain) {
					RecordDomain rd = (RecordDomain) dom;
					write("RecordDomain");
					space();
					writeIdentifier(rd.getSimpleName());
					String delim = " ( ";
					for (RecordComponent rdc : rd.getComponents()) {
						write(delim);
						noSpace();
						writeIdentifier(rdc.getName());
						write(": ");
						write(rdc.getDomain().getTGTypeName(pkg));
						delim = ", ";
					}
					write(" );\n");
					writeComments(rd, rd.getSimpleName());
				}
			}

			// write vertex classes
			for (VertexClass vc : pkg.getVertexClasses().values()) {
				if (vc.isInternal()) {
					continue;
				}
				if (vc.isAbstract()) {
					write("abstract ");
				}
				write("VertexClass");
				space();
				writeIdentifier(vc.getSimpleName());
				writeHierarchy(pkg, vc);
				writeAttributes(pkg, vc);
				writeConstraints(vc);
				write(";\n");
				writeComments(vc, vc.getSimpleName());
			}

			// write edge classes
			for (EdgeClass ec : pkg.getEdgeClasses().values()) {
				if (ec.isInternal()) {
					continue;
				}
				if (ec.isAbstract()) {
					write("abstract ");
				}
				write("EdgeClass");
				space();
				writeIdentifier(ec.getSimpleName());
				writeHierarchy(pkg, ec);

				// from (min,max) rolename
				write(" from");
				space();
				writeIdentifier(ec.getFrom().getVertexClass()
						.getQualifiedName(pkg));
				write(" (");
				write(ec.getFrom().getMin() + ",");
				if (ec.getFrom().getMax() == Integer.MAX_VALUE) {
					write("*)");
				} else {
					write(ec.getFrom().getMax() + ")");
				}

				if (!ec.getFrom().getRolename().equals("")) {
					write(" role");
					space();
					writeIdentifier(ec.getFrom().getRolename());
					String delim = " redefines";
					for (String redefinedRolename : ec.getFrom()
							.getRedefinedRoles()) {
						write(delim);
						delim = ",";
						space();
						writeIdentifier(redefinedRolename);
					}
				}

				switch (ec.getFrom().getAggregationKind()) {
				case NONE:
					// do nothing
					break;
				case SHARED:
					write(" aggregation shared");
					break;
				case COMPOSITE:
					write(" aggregation composite");
					break;
				}

				// to (min,max) rolename
				write(" to");
				space();
				writeIdentifier(ec.getTo().getVertexClass()
						.getQualifiedName(pkg));
				write(" (");
				write(ec.getTo().getMin() + ",");
				if (ec.getTo().getMax() == Integer.MAX_VALUE) {
					write("*)");
				} else {
					write(ec.getTo().getMax() + ")");
				}

				if (!ec.getTo().getRolename().equals("")) {
					write(" role");
					space();
					writeIdentifier(ec.getTo().getRolename());
					String delim = " redefines";
					for (String redefinedRolename : ec.getTo()
							.getRedefinedRoles()) {
						write(delim);
						delim = ",";
						space();
						writeIdentifier(redefinedRolename);
					}
				}

				switch (ec.getTo().getAggregationKind()) {
				case NONE:
					// do nothing
					break;
				case SHARED:
					write(" aggregation shared");
					break;
				case COMPOSITE:
					write(" aggregation composite");
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
			space();
			writeIdentifier(name);
			space();
			for (String c : elem.getComments()) {
				writeUtfString(c);
			}
			write(";\n");
		}
	}

	private void writeConstraints(AttributedElementClass aec)
			throws IOException {
		for (Constraint c : aec.getConstraints()) {
			writeSpace();
			write("[");
			noSpace();
			writeUtfString(c.getMessage());
			writeUtfString(c.getPredicate());
			if (c.getOffendingElementsQuery() != null) {
				writeUtfString(c.getOffendingElementsQuery());
			}
			noSpace();
			write("]");
			space();
		}
	}

	/**
	 * Saves the specified <code>graph</code> to the file named
	 * <code>filename</code>. When the <code>filename</code> ends with
	 * <code>.gz</code>, output will be GZIP compressed, otherwise uncompressed
	 * plain text. A {@link ProgressFunction} <code>pf</code> can be used to
	 * monitor progress.
	 * 
	 * @param filename
	 *            the name of the TG file to be written
	 * @param graph
	 *            a graph
	 * @param pf
	 *            a {@link ProgressFunction}, may be <code>null</code>
	 * @throws GraphIOException
	 *             if an IOException occurs
	 */
	public static void saveGraphToFile(String filename, Graph graph,
			ProgressFunction pf) throws GraphIOException {
		DataOutputStream out = null;
		try {
			if (filename.toLowerCase().endsWith(".gz")) {
				out = new DataOutputStream(new GZIPOutputStream(
						new FileOutputStream(filename), BUFFER_SIZE));
			} else {
				out = new DataOutputStream(new BufferedOutputStream(
						new FileOutputStream(filename), BUFFER_SIZE));
			}
			saveGraphToStream(out, graph, pf);
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
	 * @param out
	 *            a DataOutputStream
	 * @param subGraph
	 *            a BooleanGraphMarker denoting the subgraph to be saved
	 * @param pf
	 *            a {@link ProgressFunction}, may be <code>null</code>
	 * @throws GraphIOException
	 *             if an IOException occurs
	 */
	public static void saveGraphToFile(String filename,
			BooleanGraphMarker subGraph, ProgressFunction pf)
			throws GraphIOException {
		DataOutputStream out = null;
		try {
			if (filename.toLowerCase().endsWith(".gz")) {
				out = new DataOutputStream(new GZIPOutputStream(
						new FileOutputStream(filename), BUFFER_SIZE));
			} else {
				out = new DataOutputStream(new BufferedOutputStream(
						new FileOutputStream(filename), BUFFER_SIZE));
			}
			saveGraphToStream(out, subGraph, pf);
		} catch (IOException e) {
			throw new GraphIOException("exception while saving graph to "
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
	 * @param out
	 *            a DataOutputStream
	 * @param graph
	 *            a graph
	 * @param pf
	 *            a {@link ProgressFunction}, may be <code>null</code>
	 * @throws GraphIOException
	 *             if an IOException occurs
	 */
	public static void saveGraphToStream(DataOutputStream out, Graph graph,
			ProgressFunction pf) throws GraphIOException {
		try {
			GraphIO io = new GraphIO();
			io.TGOut = out;
			io.saveGraph(graph, pf, null);
			out.flush();
		} catch (IOException e) {
			throw new GraphIOException("exception while saving graph", e);
		}
	}

	/**
	 * Saves the marked <code>subGraph</code> to the stream <code>out</code>. A
	 * {@link ProgressFunction} <code>pf</code> can be used to monitor progress.
	 * The stream is <em>not</em> closed. This method does <i>not</i> check if
	 * the subgraph marker is complete.
	 * 
	 * @param out
	 *            a DataOutputStream
	 * @param subGraph
	 *            a BooleanGraphMarker denoting the subgraph to be saved
	 * @param pf
	 *            a {@link ProgressFunction}, may be <code>null</code>
	 * @throws GraphIOException
	 *             if an IOException occurs
	 */
	public static void saveGraphToStream(DataOutputStream out,
			BooleanGraphMarker subGraph, ProgressFunction pf)
			throws GraphIOException {
		try {
			GraphIO io = new GraphIO();
			io.TGOut = out;
			io.saveGraph(subGraph.getGraph(), pf, subGraph);
			out.flush();
		} catch (IOException e) {
			throw new GraphIOException("exception while saving graph", e);
		}
	}

	private void saveGraph(Graph graph, ProgressFunction pf,
			BooleanGraphMarker subGraph) throws IOException, GraphIOException {
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

		space();
		write("Graph " + toUtfString(graph.getId()) + " "
				+ graph.getGraphVersion());
		writeIdentifier(graph.getAttributedElementClass().getQualifiedName());
		int vCount = graph.getVCount();
		int eCount = graph.getECount();
		// with a GraphMarker, v/eCount have to be restricted to the marked
		// elements.
		if (subGraph != null) {
			vCount = 0;
			eCount = 0;
			for (AttributedElement ae : subGraph.getMarkedElements()) {
				if (ae instanceof Vertex) {
					vCount++;
				} else if (ae instanceof Edge) {
					eCount++;
				}
			}
		}
		write(" (" + graph.getMaxVCount() + " " + graph.getMaxECount() + " "
				+ vCount + " " + eCount + ")");
		space();
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
			AttributedElementClass aec = nextV.getAttributedElementClass();
			Package currentPackage = aec.getPackage();
			if (currentPackage != oldPackage) {
				write("Package");
				space();
				writeIdentifier(currentPackage.getQualifiedName());
				write(";\n");
				oldPackage = currentPackage;
			}
			write(Long.toString(vId));
			space();
			writeIdentifier(aec.getSimpleName());
			// write incident edges
			Edge nextI = nextV.getFirstIncidence();
			write(" <");
			noSpace();
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
			space();
			nextV.writeAttributeValues(this);
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
			AttributedElementClass aec = nextE.getAttributedElementClass();
			Package currentPackage = aec.getPackage();
			if (currentPackage != oldPackage) {
				write("Package");
				space();
				writeIdentifier(currentPackage.getQualifiedName());
				write(";\n");
				oldPackage = currentPackage;
			}
			write(Long.toString(eId));
			space();
			writeIdentifier(aec.getSimpleName());
			space();
			nextE.writeAttributeValues(this);
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
	}

	private void saveHeader() throws IOException {
		write(JGraLab.getVersionInfo(true));
		write("TGraph " + TGFILE_VERSION + ";\n");
	}

	private void writeHierarchy(Package pkg, AttributedElementClass aec)
			throws IOException {
		String delim = ":";
		for (AttributedElementClass superClass : aec.getDirectSuperClasses()) {
			if (!superClass.isInternal()) {
				write(delim);
				space();
				writeIdentifier(superClass.getQualifiedName(pkg));
				delim = ",";
			}
		}
	}

	private void writeAttributes(Package pkg, AttributedElementClass aec)
			throws IOException {
		if (aec.hasOwnAttributes()) {
			write(" {");
		}
		for (Iterator<Attribute> ait = aec.getOwnAttributeList().iterator(); ait
				.hasNext();) {
			Attribute a = ait.next();
			space();
			writeIdentifier(a.getName());
			write(": ");
			String domain = a.getDomain().getTGTypeName(pkg);
			write(domain);
			if ((a.getDefaultValueAsString() != null)
					&& !a.getDefaultValueAsString().equals("n")) {
				write(" = ");
				writeUtfString(a.getDefaultValueAsString());
			}
			if (ait.hasNext()) {
				write(", ");
			} else {
				write(" }");
			}
		}
	}

	public final void write(String s) throws IOException {
		TGOut.writeBytes(s);
	}

	public final void noSpace() {
		writeSpace = false;
	}

	public final void space() {
		writeSpace = true;
	}

	public final void writeSpace() throws IOException {
		if (writeSpace) {
			TGOut.writeBytes(" ");
		}
		writeSpace = true;
	}

	public final void writeBoolean(boolean b) throws IOException {
		writeSpace();
		TGOut.writeBytes(b ? TRUE_LITERAL : FALSE_LITERAL);
	}

	public final void writeInteger(int i) throws IOException {
		writeSpace();
		TGOut.writeBytes(Integer.toString(i));
	}

	public final void writeLong(long l) throws IOException {
		writeSpace();
		TGOut.writeBytes(Long.toString(l));
	}

	public final void writeDouble(double d) throws IOException {
		writeSpace();
		TGOut.writeBytes(Double.toString(d));
	}

	public final void writeUtfString(String s) throws IOException {
		writeSpace();
		TGOut.writeBytes(s == null ? NULL_LITERAL : toUtfString(s));
	}

	public final void writeIdentifier(String s) throws IOException {
		writeSpace();
		TGOut.writeBytes(s);
	}

	public static GraphIO createStringReader(String input, Schema schema) {
		GraphIO io = new GraphIO();
		io.TGIn = new ByteArrayInputStream(input.getBytes(Charset
				.forName("US-ASCII")));
		io.line = 1;
		io.schema = schema;
		try {
			io.la = io.read();
			io.match();
		} catch (GraphIOException e) {
			e.printStackTrace();
		}

		return io;
	}

	public static GraphIO createStringWriter(Schema schema) {
		GraphIO io = new GraphIO();
		io.BAOut = new ByteArrayOutputStream();
		io.TGOut = new DataOutputStream(io.BAOut);
		io.schema = schema;
		return io;
	}

	public String getStringWriterResult() throws GraphIOException, IOException {
		if (BAOut == null) {
			throw new GraphIOException("GraphIO did not write to a String");
		}

		// FIXME There should be a try-catch for every close operation
		TGOut.flush();
		BAOut.flush();
		String result = BAOut.toString("US-ASCII");
		try {
			close(TGOut);
		} finally {
			close(BAOut);
		}
		return result;
	}

	public static Schema loadSchemaFromFile(String filename)
			throws GraphIOException {
		// TODO should throw a file not found exception (or at least a runtime
		// exception)
		InputStream in = null;
		try {
			if (filename.toLowerCase().endsWith(".gz")) {
				in = new GZIPInputStream(new FileInputStream(filename),
						BUFFER_SIZE);
			} else {
				in = new BufferedInputStream(new FileInputStream(filename),
						BUFFER_SIZE);
			}
			return loadSchemaFromStream(in);

		} catch (IOException ex) {
			throw new GraphIOException("exception while loading schema from "
					+ filename, ex);
		} finally {
			if (in != null) {
				close(in);
			}
		}
	}

	public static Schema loadSchemaFromDatabase(GraphDatabase graphDatabase,
			String packagePrefix, String schemaName) throws GraphIOException {
		String definition = graphDatabase.getSchemaDefinition(packagePrefix,
				schemaName);
		InputStream input = new ByteArrayInputStream(definition.getBytes());
		return loadSchemaFromStream(input);
	}

	public static Schema loadAndCommitSchemaFromDatabase(
			GraphDatabase graphDatabase, String packagePrefix, String schemaName)
			throws GraphIOException {
		Schema schema = loadSchemaFromDatabase(graphDatabase, packagePrefix,
				schemaName);
		schema.commit("test",
				new CodeGeneratorConfiguration().withDatabaseSupport());
		return schema;
	}

	public static Schema loadSchemaFromStream(InputStream in)
			throws GraphIOException {
		try {
			GraphIO io = new GraphIO();
			io.TGIn = in;
			io.tgfile();
			return io.schema;
		} catch (Exception e) {
			throw new GraphIOException("exception while loading schema", e);
		}
	}

	/**
	 * Loads a graph with standard support from the file <code>filename</code>.
	 * When schema classes can not be found on the class path, the schema is
	 * first loaded and compiled in memory using the code generator
	 * configuration <code>config</code>. When the <code>filename</code> ends
	 * with <code>.gz</code>, it is assumed that the input is GZIP compressed,
	 * otherwise uncompressed plain text. A {@link ProgressFunction}
	 * <code>pf</code> can be used to monitor progress.
	 * 
	 * @param filename
	 *            the name of the TG file to be read
	 * @param config
	 *            the {@link CodeGeneratorConfiguration} to be used to generate
	 *            and compile the schema classes
	 * @param pf
	 *            a {@link ProgressFunction}, may be <code>null</code>
	 * @return the loaded graph
	 * @throws GraphIOException
	 *             if an IOException occurs or the compiled schema classes can
	 *             not be loaded
	 */
	public static Graph loadSchemaAndGraphFromFile(String filename,
			CodeGeneratorConfiguration config, ProgressFunction pf)
			throws GraphIOException {
		try {
			logger.finer("Loading graph " + filename);
			return loadGraphFromFileWithStandardSupport(filename, null, pf);
		} catch (GraphIOException ex) {
			if (ex.getCause() instanceof ClassNotFoundException) {
				logger.fine("Compiled schema classes were not found, so load and compile the schema first.");
				Schema s = loadSchemaFromFile(filename);
				s.compile(config);
				return loadGraphFromFileWithStandardSupport(filename, s, pf);
			} else {
				throw ex;
			}
		}
	}

	/**
	 * Loads a graph with standard support from the file <code>filename</code>.
	 * When the <code>filename</code> ends with <code>.gz</code>, it is assumed
	 * that the input is GZIP compressed, otherwise uncompressed plain text. A
	 * {@link ProgressFunction} <code>pf</code> can be used to monitor progress.
	 * 
	 * @param filename
	 *            the name of the TG file to be read
	 * @param pf
	 *            a {@link ProgressFunction}, may be <code>null</code>
	 * @return the loaded graph
	 * @throws GraphIOException
	 *             if an IOException occurs or the compiled schema classes can
	 *             not be loaded
	 */
	public static Graph loadGraphFromFileWithStandardSupport(String filename,
			ProgressFunction pf) throws GraphIOException {
		return loadGraphFromFile(filename, null, pf,
				ImplementationType.STANDARD);
	}

	/**
	 * Loads a graph with standard support from the file <code>filename</code>.
	 * When the <code>filename</code> ends with <code>.gz</code>, it is assumed
	 * that the input is GZIP compressed, otherwise uncompressed plain text. A
	 * {@link ProgressFunction} <code>pf</code> can be used to monitor progress.
	 * 
	 * @param filename
	 *            the name of the TG file to be read
	 * @param schema
	 *            the schema (must be the same schema as in the TG file read by
	 *            the InputStream), may be <code>null</code>
	 * @param pf
	 *            a {@link ProgressFunction}, may be <code>null</code>
	 * @return the loaded graph
	 * @throws GraphIOException
	 *             if an IOException occurs or the compiled schema classes can
	 *             not be loaded
	 */
	public static Graph loadGraphFromFileWithStandardSupport(String filename,
			Schema schema, ProgressFunction pf) throws GraphIOException {
		return loadGraphFromFile(filename, schema, pf,
				ImplementationType.STANDARD);
	}

	/**
	 * Loads a graph with transaction support from the file
	 * <code>filename</code>. When the <code>filename</code> ends with
	 * <code>.gz</code>, it is assumed that the input is GZIP compressed,
	 * otherwise uncompressed plain text. A {@link ProgressFunction}
	 * <code>pf</code> can be used to monitor progress.
	 * 
	 * @param filename
	 *            the name of the TG file to be read
	 * @param pf
	 *            a {@link ProgressFunction}, may be <code>null</code>
	 * @return the loaded graph
	 * @throws GraphIOException
	 *             if an IOException occurs or the compiled schema classes can
	 *             not be loaded
	 */
	public static Graph loadGraphFromFileWithTransactionSupport(
			String filename, ProgressFunction pf) throws GraphIOException {
		return loadGraphFromFile(filename, null, pf,
				ImplementationType.TRANSACTION);
	}

	/**
	 * Loads a graph with transaction support from the file
	 * <code>filename</code>. When the <code>filename</code> ends with
	 * <code>.gz</code>, it is assumed that the input is GZIP compressed,
	 * otherwise uncompressed plain text. A {@link ProgressFunction}
	 * <code>pf</code> can be used to monitor progress.
	 * 
	 * @param filename
	 *            the name of the TG file to be read
	 * @param schema
	 *            the schema (must be the same schema as in the TG file read by
	 *            the InputStream), may be <code>null</code>
	 * @param pf
	 *            a {@link ProgressFunction}, may be <code>null</code>
	 * @return the loaded graph
	 * @throws GraphIOException
	 *             if an IOException occurs or the compiled schema classes can
	 *             not be loaded
	 */
	public static Graph loadGraphFromFileWithTransactionSupport(
			String filename, Schema schema, ProgressFunction pf)
			throws GraphIOException {
		return loadGraphFromFile(filename, schema, pf,
				ImplementationType.TRANSACTION);
	}

	/**
	 * Loads a graph with savemem support from the file <code>filename</code>.
	 * When the <code>filename</code> ends with <code>.gz</code>, it is assumed
	 * that the input is GZIP compressed, otherwise uncompressed plain text. A
	 * {@link ProgressFunction} <code>pf</code> can be used to monitor progress.
	 * 
	 * @param filename
	 *            the name of the TG file to be read
	 * @param pf
	 *            a {@link ProgressFunction}, may be <code>null</code>
	 * @return the loaded graph
	 * @throws GraphIOException
	 *             if an IOException occurs or the compiled schema classes can
	 *             not be loaded
	 */
	public static Graph loadGraphFromFileWithSavememSupport(String filename,
			ProgressFunction pf) throws GraphIOException {
		return loadGraphFromFile(filename, null, pf, ImplementationType.SAVEMEM);
	}

	/**
	 * Loads a graph with savemem support from the file <code>filename</code>.
	 * When the <code>filename</code> ends with <code>.gz</code>, it is assumed
	 * that the input is GZIP compressed, otherwise uncompressed plain text. A
	 * {@link ProgressFunction} <code>pf</code> can be used to monitor progress.
	 * 
	 * @param filename
	 *            the name of the TG file to be read
	 * @param schema
	 *            the schema (must be the same schema as in the TG file read by
	 *            the InputStream), may be <code>null</code>
	 * @param pf
	 *            a {@link ProgressFunction}, may be <code>null</code>
	 * @return the loaded graph
	 * @throws GraphIOException
	 *             if an IOException occurs or the compiled schema classes can
	 *             not be loaded
	 */
	public static Graph loadGraphFromFileWithSavememSupport(String filename,
			Schema schema, ProgressFunction pf) throws GraphIOException {
		return loadGraphFromFile(filename, schema, pf,
				ImplementationType.SAVEMEM);
	}

	/**
	 * Use {@link loadGraphFromFileWithStandardSupport} instead.
	 * 
	 * @param filename
	 * @param pf
	 * @return
	 * @throws GraphIOException
	 */
	@Deprecated
	public static Graph loadGraphFromFile(String filename, ProgressFunction pf)
			throws GraphIOException {
		return loadGraphFromFileWithStandardSupport(filename, pf);
	}

	/**
	 * Use {@link loadGraphFromFileWithStandardSupport} instead.
	 * 
	 * @param filename
	 * @param schema
	 * @param pf
	 * @return
	 * @throws GraphIOException
	 */
	@Deprecated
	public static Graph loadGraphFromFile(String filename, Schema schema,
			ProgressFunction pf) throws GraphIOException {
		return loadGraphFromFileWithStandardSupport(filename, schema, pf);
	}

	/**
	 * Loads a graph from the file <code>filename</code>. When the
	 * <code>filename</code> ends with <code>.gz</code>, it is assumed that the
	 * input is GZIP compressed, otherwise uncompressed plain text. A
	 * {@link ProgressFunction} <code>pf</code> can be used to monitor progress.
	 * 
	 * @param filename
	 *            the name of the TG file to be read
	 * @param schema
	 *            the schema (must be the same schema as in the TG file read by
	 *            the InputStream), may be <code>null</code>
	 * @param pf
	 *            a {@link ProgressFunction}, may be <code>null</code>
	 * @param implementationType
	 *            when <code>true</code>, a graph instance with transaction
	 *            support is created
	 * @return the loaded graph
	 * @throws GraphIOException
	 *             if an IOException occurs or the compiled schema classes can
	 *             not be loaded
	 */
	public static Graph loadGraphFromFile(String filename, Schema schema,
			ProgressFunction pf, ImplementationType implementationType)
			throws GraphIOException {

		InputStream inputStream = null;
		FileInputStream fileStream = null;
		try {
			logger.finer("Loading graph " + filename);

			fileStream = new FileInputStream(filename);

			if (filename.toLowerCase().endsWith(".gz")) {
				inputStream = new GZIPInputStream(fileStream, BUFFER_SIZE);
			} else {
				inputStream = new BufferedInputStream(fileStream, BUFFER_SIZE);
			}

			return loadGraphFromStream(inputStream, schema, pf,
					implementationType);

		} catch (IOException ex) {
			throw new GraphIOException(
					"Exception while loading graph from file " + filename, ex);
		} finally {
			if (inputStream != null) {
				close(inputStream);
			}
			if (fileStream != null) {
				close(fileStream);
			}
		}
	}

	private static void close(Closeable stream) throws GraphIOException {
		try {
			if (stream != null) {
				stream.close();
			}
		} catch (IOException ex) {
			throw new GraphIOException("Exception while closing the stream.",
					ex);
		}
	}

	/**
	 * Loads a graph from the stream <code>in</code>. A {@link ProgressFunction}
	 * <code>pf</code> can be used to monitor progress. The stream is
	 * <em>not</em> closed.
	 * 
	 * @param in
	 *            an InputStream
	 * @param schema
	 *            the schema (must be the same schema as in the TG file read by
	 *            the InputStream), may be <code>null</code>
	 * @param pf
	 *            a {@link ProgressFunction}, may be <code>null</code>
	 * @param implementationType
	 *            when <code>true</code>, a graph instance with transaction
	 *            support is created
	 * @return the loaded graph
	 * @throws GraphIOException
	 *             if an IOException occurs or the compiled schema classes can
	 *             not be loaded
	 */
	public static Graph loadGraphFromStream(InputStream in, Schema schema,
			ProgressFunction pf, ImplementationType implementationType)
			throws GraphIOException {
		try {
			GraphIO io = new GraphIO();
			io.schema = schema;
			io.TGIn = in;
			io.tgfile();
			String schemaQName = io.schema.getQualifiedName();
			Class<?> schemaClass = Class.forName(schemaQName, true,
					M1ClassManager.instance(schemaQName));
			Method instanceMethod = schemaClass.getMethod("instance",
					(Class<?>[]) null);
			io.schema = (Schema) instanceMethod.invoke(null, new Object[0]);
			GraphBaseImpl loadedGraph = io.graph(pf, implementationType);
			loadedGraph.internalLoadingCompleted(io.firstIncidence,
					io.nextIncidence);
			io.firstIncidence = null;
			io.nextIncidence = null;
			loadedGraph.loadingCompleted();
			return loadedGraph;
		} catch (ClassNotFoundException e) {
			// the schema class was not found, probably schema.commit-method was
			// not called, or schema package was not included into classpath
			throw new GraphIOException(
					"Unable to load a graph which belongs to the schema because the Java-classes for this schema have not yet been created."
							+ " Use Schema.commit(..) to create them!", e);
		} catch (Exception e) {
			throw new GraphIOException("Exception while loading graph.", e);
		}
	}

	private void tgfile() throws GraphIOException, SchemaException, IOException {
		line = 1;
		la = read();
		match();
		header();
		if (lookAhead.equals("Schema")) {
			schema();
		}
		if (lookAhead.equals("") || lookAhead.equals("Graph")) {
			return;
		}
		throw new GraphIOException("symbol '" + lookAhead
				+ "' not recognized in line " + line, null);
	}

	/**
	 * Reads TG File header and checks if the file version can be processed.
	 * 
	 * @throws GraphIOException
	 *             if version number in file can not be processed
	 */
	private void header() throws GraphIOException {
		match("TGraph");
		int version = matchInteger();
		if (version != TGFILE_VERSION) {
			throw new GraphIOException("Can't read TGFile version " + version
					+ ". Expected version " + TGFILE_VERSION);
		}
		match(";");
	}

	/**
	 * Reads a Schema together with its Domains, GraphClasses and
	 * GraphElementClasses from a TG-file. Subsequently, the Schema is created.
	 * 
	 * @throws GraphIOException
	 */
	private void schema() throws GraphIOException, SchemaException {
		currentPackageName = "";
		match("Schema");
		String[] qn = matchQualifiedName(true);
		if (qn[0].equals("")) {
			throw new GraphIOException("invalid schema name '" + lookAhead
					+ "', package prefix must not be empty in line " + line);
		}
		match(";");

		if (schema != null) {
			// We already have a schema, so we don't want to load the schema
			// from the file

			// but wait, check if the names match...
			if (schema.getQualifiedName().equals(qn[0] + "." + qn[1])) {
				// yes, everything is fine :-)
				// skip schema part
				while ((lookAhead.length() > 0) && !lookAhead.equals("Graph")) {
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
		if (!(lookAhead.equals("") || lookAhead.equals("Graph"))) {
			throw new GraphIOException("symbol '" + lookAhead
					+ "' not recognized in line " + line, null);
		}

		// sort data of RecordDomains, GraphClasses and GraphElementClasses in
		// topological order

		checkFromToVertexClasses();

		sortRecordDomains();
		sortVertexClasses();
		sortEdgeClasses();

		domDef(); // create Domains
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
	private Map<String, Domain> domDef() throws GraphIOException,
			SchemaException {
		// basic domains are created automatically
		enumDomains(); // create EnumDomains
		recordDomains(); // create RecordDomains
		return domains;
	}

	/**
	 * Reads an EnumDomain, i.e. its name along with the enum constants.
	 * 
	 * @throws GraphIOException
	 */
	private void parseEnumDomain() throws GraphIOException {
		match("EnumDomain");
		String[] qn = matchQualifiedName(true);
		enumDomainBuffer.add(new EnumDomainData(qn[0], qn[1],
				parseEnumConstants()));
		match(";");
	}

	/**
	 * Creates all EnumDomains whose data is stored in {@link enumDomainBuffer}
	 */
	private void enumDomains() {
		Domain domain;

		for (EnumDomainData enumDomainData : enumDomainBuffer) {
			String qName = toQNameString(enumDomainData.packageName,
					enumDomainData.simpleName);
			domain = schema.createEnumDomain(qName,
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
		match("RecordDomain");
		String[] qn = matchQualifiedName(true);
		recordDomainBuffer.add(new RecordDomainData(qn[0], qn[1],
				parseRecordComponents()));
		match(";");
	}

	/**
	 * Creates all RecordDomains whose data is stored in
	 * {@link recordDomainBuffer} @
	 */
	private void recordDomains() throws GraphIOException, SchemaException {
		Domain domain;

		for (RecordDomainData recordDomainData : recordDomainBuffer) {
			String qName = toQNameString(recordDomainData.packageName,
					recordDomainData.simpleName);
			domain = schema.createRecordDomain(qName,
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
	private void parseSchema() throws GraphIOException, SchemaException {
		while (lookAhead.equals("Comment")) {
			parseComment();
		}
		String currentGraphClassName = parseGraphClass();

		while (lookAhead.equals("Package") || lookAhead.equals("RecordDomain")
				|| lookAhead.equals("EnumDomain")
				|| lookAhead.equals("abstract")
				|| lookAhead.equals("VertexClass")
				|| lookAhead.equals("EdgeClass") || lookAhead.equals("Comment")) {
			if (lookAhead.equals("Package")) {
				parsePackage();
			} else if (lookAhead.equals("RecordDomain")) {
				parseRecordDomain();
			} else if (lookAhead.equals("EnumDomain")) {
				parseEnumDomain();
			} else if (lookAhead.equals("Comment")) {
				parseComment();
			} else {
				parseGraphElementClass(currentGraphClassName);
			}
		}
	}

	private void parseComment() throws GraphIOException {
		match("Comment");
		String qName = toQNameString(matchQualifiedName());
		List<String> comments = new ArrayList<String>();
		comments.add(matchUtfString());
		while (!lookAhead.equals(";")) {
			comments.add(matchUtfString());
		}
		match(";");
		if (commentData.containsKey(qName)) {
			commentData.get(qName).addAll(comments);
		} else {
			commentData.put(qName, comments);
		}
	}

	private void parsePackage() throws GraphIOException {
		match("Package");
		currentPackageName = "";
		if (lookAhead.equals(";")) {
			currentPackageName = "";
		} else {
			String[] qn = matchQualifiedName(false);
			String qualifiedName = toQNameString(qn);
			if (!isValidPackageName(qn[1])) {
				throw new GraphIOException("invalid package name '"
						+ qualifiedName + "' in line " + line);
			}
			currentPackageName = qualifiedName;
		}
		match(";");
	}

	/**
	 * Creates the GraphClass contained in the Schema along with its
	 * GraphElementClasses.
	 * 
	 * @throws GraphIOException
	 * @throws SchemaException
	 */
	private void completeGraphClass() throws GraphIOException, SchemaException {
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
	 * @throws SchemaException
	 */
	private String parseGraphClass() throws GraphIOException, SchemaException {
		match("GraphClass");
		graphClass = new GraphClassData();

		graphClass.name = matchSimpleName(true);
		if (lookAhead.equals("{")) {
			graphClass.attributes = parseAttributes();
		}

		if (lookAhead.equals("[")) {
			// There are constraints
			graphClass.constraints = parseConstraints();
		}

		match(";");

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
	 * @throws SchemaException
	 */
	private GraphClass createGraphClass(GraphClassData gcData)
			throws GraphIOException, SchemaException {
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
		match(":");
		String[] qn = matchQualifiedName(true);
		hierarchy.add(toQNameString(qn));
		while (lookAhead.equals(",")) {
			match();
			qn = matchQualifiedName(true);
			hierarchy.add(toQNameString(qn));
		}
		return hierarchy;
	}

	private List<AttributeData> parseAttributes() throws GraphIOException {
		List<AttributeData> attributesData = new ArrayList<AttributeData>();
		Set<String> names = new TreeSet<String>();

		match("{");
		AttributeData ad = new AttributeData();
		ad.name = matchSimpleName(false);
		match(":");
		ad.domainDescription = parseAttrDomain();
		if (lookAhead.equals("=")) {
			match();
			ad.defaultValue = matchUtfString();
		}
		attributesData.add(ad);
		names.add(ad.name);

		while (lookAhead.equals(",")) {
			match(",");
			ad = new AttributeData();
			ad.name = matchSimpleName(false);
			match(":");
			ad.domainDescription = parseAttrDomain();
			if (lookAhead.equals("=")) {
				match();
				ad.defaultValue = matchUtfString();
			}
			if (names.contains(ad.name)) {
				throw new GraphIOException("duplicate attribute name '"
						+ ad.name + "' in line " + line);
			}
			attributesData.add(ad);
			names.add(ad.name);
		}
		match("}");
		return attributesData;
	}

	private void addAttributes(List<AttributeData> attributesData,
			AttributedElementClass aec) throws GraphIOException {
		for (AttributeData ad : attributesData) {
			aec.addAttribute(ad.name, attrDomain(ad.domainDescription),
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
		if (lookAhead.matches("[.]?List")) {
			match();
			match("<");
			attrDomain.add("List<");
			parseAttrDomain(attrDomain);
			match(">");
		} else if (lookAhead.matches("[.]?Set")) {
			match();
			match("<");
			attrDomain.add("Set<");
			parseAttrDomain(attrDomain);
			match(">");
		} else if (lookAhead.matches("[.]?Map")) {
			match();
			match("<");
			attrDomain.add("Map<");
			parseAttrDomain(attrDomain);
			match(",");
			parseAttrDomain(attrDomain);
			match(">");
		} else {
			if (isBasicDomainName(lookAhead)) {
				attrDomain.add(lookAhead);
				match();
			} else {
				String[] qn = matchQualifiedName(true);
				attrDomain.add(toQNameString(qn));
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
					throw new GraphIOException(
							"can't create list domain in line " + line, e);
				}
			} else if (domainName.equals("Set<")) {
				try {
					return schema.createSetDomain(attrDomain(domainNames));
				} catch (SchemaException e) {
					throw new GraphIOException(
							"can't create set domain in line " + line, e);
				}
			} else if (domainName.equals("Map<")) {
				try {
					Domain keyDomain = attrDomain(domainNames);
					Domain valueDomain = attrDomain(domainNames);
					if (keyDomain == null) {
						throw new GraphIOException(
								"can't create map domain, because no key domain was given in line "
										+ line);
					}
					MapDomain result = schema.createMapDomain(keyDomain,
							valueDomain);
					// System.out.println("result = Map<"
					// + keyDomain.getQualifiedName() + ", "
					// + valueDomain.getQualifiedName() + ">");
					return result;
				} catch (SchemaException e) {
					throw new GraphIOException(
							"can't create map domain in line " + line, e);
				}
			} else {
				Domain result = schema.getDomain(domainName);
				if (result == null) {
					throw new GraphIOException("undefined domain '"
							+ domainName + "' in line " + line);
				}
				return result;
			}
		}
		throw new GraphIOException("Couldn't create domain for '" + domainNames
				+ "' in line " + line);
	}

	public final String matchEnumConstant() throws GraphIOException {
		if (schema.isValidEnumConstant(lookAhead)
				|| lookAhead.equals(NULL_LITERAL)) {
			return matchAndNext();
		}
		throw new GraphIOException("invalid enumeration constant '" + lookAhead
				+ "' in line " + line);
	}

	/**
	 * Reads the a GraphElementClass of the GraphClass indicated by the given
	 * name.
	 * 
	 * @throws GraphIOException
	 */
	private void parseGraphElementClass(String gcName) throws GraphIOException,
			SchemaException {
		GraphElementClassData graphElementClassData = new GraphElementClassData();

		if (lookAhead.equals("abstract")) {
			match();
			graphElementClassData.isAbstract = true;
		}

		if (lookAhead.equals("VertexClass")) {
			match("VertexClass");
			String[] qn = matchQualifiedName(true);
			graphElementClassData.packageName = qn[0];
			graphElementClassData.simpleName = qn[1];
			if (lookAhead.equals(":")) {
				graphElementClassData.directSuperClasses = parseHierarchy();
			}
			vertexClassBuffer.get(gcName).add(graphElementClassData);
		} else if (lookAhead.equals("EdgeClass")) {
			match();
			String[] qn = matchQualifiedName(true);
			graphElementClassData.packageName = qn[0];
			graphElementClassData.simpleName = qn[1];
			if (lookAhead.equals(":")) {
				graphElementClassData.directSuperClasses = parseHierarchy();
			}
			match("from");
			String[] fqn = matchQualifiedName(true);
			graphElementClassData.fromVertexClassName = toQNameString(fqn);
			graphElementClassData.fromMultiplicity = parseMultiplicity();
			graphElementClassData.fromRoleName = parseRoleName();
			graphElementClassData.redefinedFromRoles = parseRolenameRedefinitions();
			graphElementClassData.fromAggregation = parseAggregation();

			match("to");
			String[] tqn = matchQualifiedName(true);
			graphElementClassData.toVertexClassName = toQNameString(tqn);
			graphElementClassData.toMultiplicity = parseMultiplicity();
			graphElementClassData.toRoleName = parseRoleName();
			graphElementClassData.redefinedToRoles = parseRolenameRedefinitions();
			graphElementClassData.toAggregation = parseAggregation();
			edgeClassBuffer.get(gcName).add(graphElementClassData);
		} else {
			throw new SchemaException("Undefined keyword: " + lookAhead
					+ " at position ");
		}

		if (lookAhead.equals("{")) {
			graphElementClassData.attributes = parseAttributes();
		}

		if (lookAhead.equals("[")) {
			// There are constraints
			graphElementClassData.constraints = parseConstraints();
		}
		match(";");
	}

	private Set<Constraint> parseConstraints() throws GraphIOException {
		// constraints have the form: ["msg" "pred" "optGreql"] or ["msg"
		// "pred"] and there may be as many as one wants...
		HashSet<Constraint> constraints = new HashSet<Constraint>(1);
		do {
			match("[");
			String msg = matchUtfString();
			String pred = matchUtfString();
			String greql = null;
			if (!lookAhead.equals("]")) {
				greql = matchUtfString();
			}
			constraints.add(new ConstraintImpl(msg, pred, greql));
			match("]");
		} while (lookAhead.equals("["));
		return constraints;
	}

	private VertexClass createVertexClass(GraphElementClassData vcd,
			GraphClass gc) throws GraphIOException, SchemaException {
		VertexClass vc = gc.createVertexClass(vcd.getQualifiedName());
		vc.setAbstract(vcd.isAbstract);

		addAttributes(vcd.attributes, vc);

		for (Constraint constraint : vcd.constraints) {
			vc.addConstraint(constraint);
		}

		GECsearch.put(vc, gc);
		return vc;
	}

	private EdgeClass createEdgeClass(GraphElementClassData ecd, GraphClass gc)
			throws GraphIOException, SchemaException {
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

		GECsearch.put(ec, gc);
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

		match("(");
		int min = matchInteger();
		if (min < 0) {
			throw new GraphIOException("minimum multiplicity '" + min
					+ "' must be >=0 in line " + line);
		}
		match(",");
		int max;
		if (lookAhead.equals("*")) {
			max = Integer.MAX_VALUE;
			match();
		} else {
			max = matchInteger();
			if (max < min) {
				throw new GraphIOException("maximum multiplicity '" + max
						+ "' must be * or >=" + min + " in line " + line);
			}
		}
		match(")");
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
		if (lookAhead.equals("role")) {
			match();
			String result = matchSimpleName(false);
			return result;
		}
		return "";
	}

	/**
	 * Reads the redefinition of a rolename of an EdgeClass
	 * 
	 * @return A Set<String> of redefined rolenames or <code>null</code> if no
	 *         rolenames were redefined
	 * @throw GraphIOException
	 */
	private Set<String> parseRolenameRedefinitions() throws GraphIOException {
		if (!lookAhead.equals("redefines")) {
			return null;
		}
		match();
		Set<String> result = new HashSet<String>();
		String redefinedName = matchSimpleName(false);
		result.add(redefinedName);
		while (lookAhead.equals(",")) {
			match();
			redefinedName = matchSimpleName(false);
			result.add(redefinedName);
		}
		return result;
	}

	private AggregationKind parseAggregation() throws GraphIOException {
		if (!lookAhead.equals("aggregation")) {
			return AggregationKind.NONE;
		}
		match();
		if (lookAhead.equals("none")) {
			match();
			return AggregationKind.NONE;
		} else if (lookAhead.equals("shared")) {
			match();
			return AggregationKind.SHARED;
		} else if (lookAhead.equals("composite")) {
			match();
			return AggregationKind.COMPOSITE;
		} else {
			throw new GraphIOException(
					"invalid aggregation: expected none, shared, or composite, but found '"
							+ lookAhead + "' in line " + line);
		}
	}

	private static boolean isValidPackageName(String s) {
		if ((s == null) || (s.length() == 0)) {
			return false;
		}
		char[] chars = s.toCharArray();
		if (!Character.isLetter(chars[0]) || !Character.isLowerCase(chars[0])
				|| (chars[0] > 127)) {
			return false;
		}
		for (int i = 1; i < chars.length; i++) {
			if (!(Character.isLowerCase(chars[i])
					|| Character.isDigit(chars[i]) || (chars[i] == '_'))
					|| (chars[i] > 127)) {
				return false;
			}
		}
		return true;
	}

	private List<ComponentData> parseRecordComponents() throws GraphIOException {
		List<ComponentData> componentsData = new ArrayList<ComponentData>();
		Set<String> names = new TreeSet<String>();

		match("(");
		ComponentData cd = new ComponentData();
		cd.name = matchSimpleName(false);
		match(":");
		cd.domainDescription = parseAttrDomain();
		componentsData.add(cd);
		names.add(cd.name);

		while (lookAhead.equals(",")) {
			match(",");
			cd = new ComponentData();
			cd.name = matchSimpleName(false);
			match(":");
			cd.domainDescription = parseAttrDomain();
			if (names.contains(cd.name)) {
				throw new GraphIOException("duplicate record component  name '"
						+ cd.name + "' in line " + line);
			}
			componentsData.add(cd);
			names.add(cd.name);
		}
		match(")");
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
		match("(");
		List<String> enums = new ArrayList<String>();
		enums.add(matchEnumConstant());
		while (lookAhead.equals(",")) {
			match();
			String s = matchEnumConstant();
			if (enums.contains(s)) {
				throw new GraphIOException(
						"duplicate enumeration constant component name '"
								+ lookAhead + "' in line " + line);
			}
			enums.add(s);
		}
		match(")");
		return enums;
	}

	private void buildVertexClassHierarchy() throws GraphIOException,
			SchemaException {
		AttributedElementClass aec;
		VertexClass superClass;

		for (Entry<String, List<GraphElementClassData>> gcElements : vertexClassBuffer
				.entrySet()) {
			for (GraphElementClassData vData : gcElements.getValue()) {
				aec = schema
						.getAttributedElementClass(vData.getQualifiedName());
				if (aec == null) {
					throw new GraphIOException(
							"undefined AttributedElementClass '"
									+ vData.getQualifiedName() + "'");
				}
				if (aec instanceof VertexClass) {
					for (String superClassName : vData.directSuperClasses) {
						superClass = (VertexClass) GECsearch.get(aec)
								.getGraphElementClass(superClassName);
						if (superClass == null) {
							throw new GraphIOException(
									"undefined VertexClass '" + superClassName
											+ "'");
						}
						((VertexClass) aec).addSuperClass(superClass);
					}
				}
			}
		}
	}

	private void buildEdgeClassHierarchy() throws GraphIOException,
			SchemaException {
		AttributedElementClass aec;
		EdgeClass superClass;

		for (Entry<String, List<GraphElementClassData>> gcElements : edgeClassBuffer
				.entrySet()) {
			for (GraphElementClassData eData : gcElements.getValue()) {
				aec = schema
						.getAttributedElementClass(eData.getQualifiedName());
				if (aec == null) {
					throw new GraphIOException(
							"undefined AttributedElementClass '"
									+ eData.getQualifiedName() + "'");
				}
				if (!(aec instanceof EdgeClass)) {
					throw new GraphIOException("Expected EdgeClass '"
							+ eData.getQualifiedName() + "', but it's a "
							+ aec.getM1Class().getSimpleName());
				}
				EdgeClass ec = (EdgeClass) aec;
				for (String superClassName : eData.directSuperClasses) {
					superClass = (EdgeClass) GECsearch.get(aec)
							.getGraphElementClass(superClassName);
					if (superClass == null) {
						throw new GraphIOException("undefined EdgeClass '"
								+ superClassName + "'");
					}
					ec.addSuperClass(superClass);
				}
				ec.getFrom().addRedefinedRoles(eData.redefinedFromRoles);
				ec.getTo().addRedefinedRoles(eData.redefinedToRoles);
			}
		}
	}

	private void buildHierarchy() throws GraphIOException, SchemaException {
		buildVertexClassHierarchy();
		buildEdgeClassHierarchy();
	}

	private final String nextToken() throws GraphIOException {
		StringBuilder out = new StringBuilder();
		isUtfString = false;
		try {
			skipWs();
			if (la == '"') {
				readUtfString(out);
				isUtfString = true;
			} else if (isSeparator(la)) {
				out.append((char) la);
				la = read();
			} else {
				if (la != -1) {
					do {
						out.append((char) la);
						la = read();
					} while (!isWs(la) && !isSeparator(la) && (la != -1));
				}
			}
		} catch (IOException e) {
			throw new GraphIOException(
					"error on reading bytes from file, line " + line
							+ ", last char read was [" + (char) la + "]", e);
		}
		return out.toString();
	}

	private final int read() throws GraphIOException {
		if (putBackChar >= 0) {
			int result = putBackChar;
			putBackChar = -1;
			return result;
		}
		if (bufferPos < bufferSize) {
			return buffer[bufferPos++];
		} else {
			try {
				bufferSize = TGIn.read(buffer);
			} catch (IOException e) {
				throw new GraphIOException("Error while loading Graph");
			}
			if (bufferSize != -1) {
				bufferPos = 0;
				return buffer[bufferPos++];
			} else {
				return -1;
			}
		}
	}

	private final void readUtfString(StringBuilder out) throws IOException,
			GraphIOException {
		int startLine = line;
		la = read();
		LOOP: while ((la != -1) && (la != '"')) {
			if ((la < 32) || (la > 127)) {
				throw new GraphIOException("invalid character '" + (char) la
						+ "' in string in line " + line);
			}
			if (la == '\\') {
				la = read();
				if (la == -1) {
					break LOOP;
				}
				switch (la) {
				case '\\':
					la = '\\';
					break;
				case '"':
					la = '"';
					break;
				case 'n':
					la = '\n';
					break;
				case 'r':
					la = '\r';
					break;
				case 't':
					la = '\t';
					break;
				case 'u':
					la = read();
					if (la == -1) {
						break LOOP;
					}
					String unicode = "" + (char) la;
					la = read();
					if (la == -1) {
						break LOOP;
					}
					unicode += (char) la;
					la = read();
					if (la == -1) {
						break LOOP;
					}
					unicode += (char) la;
					la = read();
					if (la == -1) {
						break LOOP;
					}
					unicode += (char) la;
					try {
						la = Integer.parseInt(unicode, 16);
					} catch (NumberFormatException e) {
						throw new GraphIOException(
								"invalid unicode escape sequence '\\u"
										+ unicode + "' in line " + line);
					}
					break;
				default:
					throw new GraphIOException(
							"invalid escape sequence in string in line " + line);
				}
			}
			out.append((char) la);
			la = read();
		}
		if (la == -1) {
			throw new GraphIOException("unterminated string starting in line "
					+ startLine + ".  lookAhead = '" + lookAhead + "'");
		}
		la = read();
	}

	private final static boolean isWs(int c) {
		return (c == ' ') || (c == '\n') || (c == '\t') || (c == '\r');
	}

	private final static boolean isSeparator(int c) {
		return (c == ';') || (c == '<') || (c == '>') || (c == '(') || (c == ')')
				|| (c == '{') || (c == '}') || (c == ':') || (c == '[') || (c == ']')
				|| (c == ',') || (c == '=');
	}

	private final void skipWs() throws GraphIOException {
		// skip whitespace and consecutive single line comments
		do {
			// skip whitespace
			while (isWs(la)) {
				if (la == '\n') {
					++line;
				}
				la = read();
			}
			// skip single line comments
			if (la == '/') {
				la = read();
				if ((la >= 0) && (la == '/')) {
					// single line comment, skip to the end of the current line
					while ((la >= 0) && (la != '\n')) {
						la = read();
					}
				} else {
					putback(la);
				}
			}
		} while (isWs(la));
	}

	private final void putback(int ch) {
		putBackChar = ch;
	}

	private final String matchAndNext() throws GraphIOException {
		String result = lookAhead;
		match();
		return result;
	}

	public final boolean isNextToken(String token) {
		return lookAhead.equals(token);
	}

	public final void match() throws GraphIOException {
		lookAhead = nextToken();
	}

	public final void match(String s) throws GraphIOException {
		if (lookAhead.equals(s)) {
			lookAhead = nextToken();
		} else {
			throw new GraphIOException("expected [" + s + "] but found ["
					+ lookAhead + "] in line " + line, null);
		}
	}

	public final int matchInteger() throws GraphIOException {
		try {
			int result = Integer.parseInt(lookAhead);
			match();
			return result;
		} catch (NumberFormatException e) {
			throw new GraphIOException("expected int number but found '"
					+ lookAhead + "' in line " + line, e);
		}
	}

	public final long matchLong() throws GraphIOException {
		try {
			long result = Long.parseLong(lookAhead);
			match();
			return result;
		} catch (NumberFormatException e) {
			throw new GraphIOException("expected long number but found '"
					+ lookAhead + "' in line " + line, e);
		}
	}

	/**
	 * Parses an identifier, checks it for validity and returns it.
	 * 
	 * @param isUpperCase
	 *            If true, the identifier must begin with an uppercase character
	 * @return the parsed identifier
	 * @throws GraphIOException
	 */
	public final String matchSimpleName(boolean isUpperCase)
			throws GraphIOException {
		String s = lookAhead;
		boolean ok = isValidIdentifier(s)
				&& ((isUpperCase && Character.isUpperCase(s.charAt(0))) || (!isUpperCase
						&& Character.isLowerCase(s.charAt(0))));

		if (!ok) {
			throw new GraphIOException("invalid simple name '" + lookAhead
					+ "' in line " + line);
		}
		match();
		return s;
	}

	/**
	 * Parses an identifier, checks it for validity and returns it.
	 * 
	 * @param isUpperCase
	 *            If true, the identifier must begin with an uppercase character
	 * @return An array of the form {parentPackage, simpleName}
	 * @throws GraphIOException
	 */
	public final String[] matchQualifiedName(boolean isUpperCase)
			throws GraphIOException {

		String c = lookAhead.indexOf('.') >= 0 ? lookAhead : toQNameString(
				currentPackageName, lookAhead);
		String[] result = SchemaImpl.splitQualifiedName(c);

		boolean ok = true;
		if (result[0].length() > 0) {
			String[] parts = result[0].split("\\.");
			ok = ((parts.length == 1) && (parts[0].length() == 0))
					|| isValidPackageName(parts[0]);
			for (int i = 1; (i < parts.length) && ok; i++) {
				ok = ok && isValidPackageName(parts[i]);
			}
		}

		ok = ok
				&& isValidIdentifier(result[1])
				&& ((isUpperCase && Character.isUpperCase(result[1].charAt(0))) || (!isUpperCase
						&& Character.isLowerCase(result[1].charAt(0))));

		if (!ok) {
			throw new GraphIOException("invalid qualified name '" + lookAhead
					+ "' in line " + line);
		}
		match();
		return result;
	}

	public final String[] matchQualifiedName() throws GraphIOException {
		String c = lookAhead.indexOf('.') >= 0 ? lookAhead : toQNameString(
				currentPackageName, lookAhead);
		String[] result = SchemaImpl.splitQualifiedName(c);

		boolean ok = true;
		if (result[0].length() > 0) {
			String[] parts = result[0].split("\\.");
			ok = ((parts.length == 1) && (parts[0].length() == 0))
					|| isValidPackageName(parts[0]);
			for (int i = 1; (i < parts.length) && ok; i++) {
				ok = ok && isValidPackageName(parts[i]);
			}
		}

		ok = ok && isValidIdentifier(result[1]);

		if (!ok) {
			throw new GraphIOException("invalid qualified name '" + lookAhead
					+ "' in line " + line);
		}
		match();
		return result;
	}

	/**
	 * @param qn
	 * @return a string representation of a qualified name specified as array
	 *         (like returned by @{#matchQualifiedName}).
	 */
	private final String toQNameString(String[] qn) {
		return toQNameString(qn[0], qn[1]);
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
		if (!isUtfString && lookAhead.equals(NULL_LITERAL)) {
			match();
			return null;
		}
		if (isUtfString) {
			String result = lookAhead;
			match();
			String s = stringPool.get(result);
			if (s == null) {
				stringPool.put(result, result);
			} else {
				result = s;
			}
			return result;
		}
		throw new GraphIOException("expected a string constant but found '"
				+ lookAhead + "' in line " + line);
	}

	public final boolean matchBoolean() throws GraphIOException {
		if (!lookAhead.equals("t") && !lookAhead.equals("f")) {
			throw new GraphIOException(
					"expected a boolean constant ('f' or 't') but found '"
							+ lookAhead + "' in line " + line);
		}
		boolean result = lookAhead.equals("t");
		match();
		return result;
	}

	private GraphBaseImpl graph(ProgressFunction pf,
			ImplementationType implementationType) throws GraphIOException {
		currentPackageName = "";
		match("Graph");
		String graphId = matchUtfString();
		long graphVersion = matchLong();

		gcName = matchAndNext();
		assert !gcName.contains(".") && isValidIdentifier(gcName) : "illegal characters in graph class '"
				+ gcName + "'";
		// check if classname is known in the schema
		if (!schema.getGraphClass().getQualifiedName().equals(gcName)) {
			throw new GraphIOException("Graph Class " + gcName
					+ "does not exist in " + schema.getQualifiedName());
		}
		match("(");
		int maxV = matchInteger();
		int maxE = matchInteger();
		int vCount = matchInteger();
		int eCount = matchInteger();
		match(")");

		// verify vCount <= maxV && eCount <= maxE
		if (vCount > maxV) {
			throw new GraphIOException("Number of vertices in graph (" + vCount
					+ ") exceeds maximum number of vertices (" + maxV + ")");
		}
		if (eCount > maxE) {
			throw new GraphIOException("Number of edges in graph (" + eCount
					+ ") exceeds maximum number of edges (" + maxE + ")");
		}

		// adjust fields for incidences
		edgeIn = new Vertex[maxE + 1];
		edgeOut = new Vertex[maxE + 1];
		firstIncidence = new int[maxV + 1];
		nextIncidence = new int[2 * maxE + 1];
		edgeOffset = maxE;

		long graphElements = 0, currentCount = 0, interval = 1;
		if (pf != null) {
			pf.init(vCount + eCount);
			interval = pf.getUpdateInterval();
		}
		GraphBaseImpl graph = null;
		try {
			graph = (GraphBaseImpl) schema.getGraphCreateMethod(
					implementationType).invoke(null,
					new Object[] { graphId, maxV, maxE });
		} catch (Exception e) {
			throw new GraphIOException("can't create graph for class '"
					+ gcName + "'", e);
		}
		graph.setLoading(true);
		graph.readAttributeValues(this);
		match(";");

		int vNo = 1;
		while (vNo <= vCount) {
			if (lookAhead.equals("Package")) {
				parsePackage();
			} else {
				vertexDesc(graph, implementationType);
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
			if (lookAhead.equals("Package")) {
				parsePackage();
			} else {
				edgeDesc(graph, implementationType);
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
		if (pf != null) {
			pf.finished();
		}
		graph.setLoading(false);
		return graph;
	}

	public final double matchDouble() throws GraphIOException {
		try {
			double result = Double.parseDouble(lookAhead);
			match();
			return result;
		} catch (NumberFormatException e) {
			throw new GraphIOException("expected a double value but found '"
					+ lookAhead + "' in line " + line, e);
		}
	}

	private void vertexDesc(Graph graph, ImplementationType implementationType)
			throws GraphIOException {
		int vId = vId();
		String vcName = className();
		Vertex vertex;
		Method createMethod;
		createMethod = createMethods.get(vcName);
		try {
			if (createMethod == null) {
				createMethod = schema.getVertexCreateMethod(vcName,
						implementationType);
				createMethods.put(vcName, createMethod);
			}
			vertexDescTempObject[0] = vId;
			vertex = (Vertex) createMethod.invoke(graph, vertexDescTempObject);
		} catch (Exception e) {
			e.printStackTrace();
			throw new GraphIOException("can't create vertex " + vId, e);
		}
		parseIncidentEdges(vertex);
		vertex.readAttributeValues(this);
		match(";");
	}

	private void edgeDesc(Graph graph, ImplementationType implementationType)
			throws GraphIOException {
		int eId = eId();
		String ecName = className();
		Edge edge;
		Method createMethod;
		createMethod = createMethods.get(ecName);
		try {
			if (createMethod == null) {
				createMethod = schema.getEdgeCreateMethod(ecName,
						implementationType);
				createMethods.put(ecName, createMethod);
			}
			edgeDescTempObject[0] = eId;
			edgeDescTempObject[1] = edgeOut[eId];
			edgeDescTempObject[2] = edgeIn[eId];
			edge = (Edge) createMethod.invoke(graph, edgeDescTempObject);
		} catch (Exception e) {
			throw new GraphIOException("can't create edge " + eId + " from "
					+ edgeOut[eId] + " to " + edgeIn[eId], e);
		}
		edge.readAttributeValues(this);
		match(";");
	}

	private int eId() throws GraphIOException {
		int eId = matchInteger();
		if (eId == 0) {
			throw new GraphIOException("Invalid edge id " + eId + ".");
		}
		return eId;
	}

	private String className() throws GraphIOException {
		String[] qn = matchQualifiedName(true);
		// The following time-consuming test is performed in the invocation and
		// thus not longer needed here
		// if (!schema.knows(className))
		// throw new GraphIOException("Class " + className
		// + " of read element does not exist.");
		return toQNameString(qn);
	}

	private int vId() throws GraphIOException {
		int vId = matchInteger();
		if (vId <= 0) {
			throw new GraphIOException("Invalid vertex id " + vId + ".");
		} else {
			return vId;
		}
	}

	private void parseIncidentEdges(Vertex v) throws GraphIOException {
		int eId = 0;
		int prevId = 0;
		int vId = v.getId();
		boolean first = true;

		match("<");
		while (!lookAhead.equals(">")) {
			prevId = eId;
			eId = eId();
			// if (firstEdgeAtVertex[vId] == 0) {
			if (first) {
				firstIncidence[vId] = eId;
				first = false;
			} else {
				nextIncidence[edgeOffset + prevId] = eId;
			}
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

	private static boolean isValidIdentifier(String s) {
		if ((s == null) || (s.length() == 0)) {
			return false;
		}
		char[] chars = s.toCharArray();
		if (!Character.isLetter(chars[0]) || (chars[0] > 127)) {
			return false;
		}
		for (int i = 1; i < chars.length; i++) {
			if (!(Character.isLetter(chars[i]) || Character.isDigit(chars[i]) || (chars[i] == '_'))
					|| (chars[i] > 127)) {
				return false;
			}
		}
		return true;
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
								.equals(vc.getQualifiedName())
								|| ec.fromVertexClassName.equals("Vertex")) {
							existingFromVertexClass = true;
						}
						if (ec.toVertexClassName.equals(vc.getQualifiedName())
								|| ec.toVertexClassName.equals("Vertex")) {
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
					throw new GraphIOException("FromVertexClass "
							+ ec.fromVertexClassName + " at EdgeClass "
							+ ec.getQualifiedName() + " + does not exist");
				}
				if (!existingToVertexClass) {
					throw new GraphIOException("ToVertexClass "
							+ ec.toVertexClassName + " at EdgeClass "
							+ ec.getQualifiedName() + " does not exist");
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

		Set<String> redefinedFromRoles = null;

		AggregationKind fromAggregation;

		String toVertexClassName;

		int[] toMultiplicity = { 1, Integer.MAX_VALUE };

		String toRoleName = "";

		Set<String> redefinedToRoles = null;

		AggregationKind toAggregation;

		List<AttributeData> attributes = new ArrayList<AttributeData>();

		Set<Constraint> constraints = new HashSet<Constraint>(1);
	}

	public static Graph loadGraphFromDatabase(String id,
			GraphDatabase graphDatabase) throws GraphDatabaseException {
		if (graphDatabase != null) {
			return graphDatabase.getGraph(id);
		} else {
			throw new GraphDatabaseException("No graph database given.");
		}
	}

	public static void loadSchemaIntoGraphDatabase(String filePath,
			GraphDatabase graphDatabase) throws IOException, GraphIOException,
			SQLException {
		Schema schema = loadSchemaFromFile(filePath);
		graphDatabase.insertSchema(schema);
	}
}
