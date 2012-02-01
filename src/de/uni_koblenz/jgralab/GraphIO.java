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
import de.uni_koblenz.jgralab.impl.InternalGraph;
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
import de.uni_koblenz.jgralab.schema.impl.compilation.SchemaClassManager;

/**
 * class for loading and storing schema and graphs in tg format
 * 
 * @author ist@uni-koblenz.de
 */
public class GraphIO {
	/**
	 * TG File Version this GraphIO recognizes.
	 */
	public static final int TGFILE_VERSION = 2;
	public static final String NULL_LITERAL = "n";
	public static final String TRUE_LITERAL = "t";
	public static final String FALSE_LITERAL = "f";
	public static final String TGRAPH_FILE_EXTENSION = ".tg";
	public static final String TGRAPH_COMPRESSED_FILE_EXTENSION = ".tg.gz";

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
			return f.isDirectory() || this.accept(f, f.getName());
		}

		@Override
		public String getDescription() {
			return "TG Files";
		}
	}

	protected static final int BUFFER_SIZE = 65536;

	private static Logger logger = Logger.getLogger(GraphIO.class.getName());

	protected InputStream TGIn;

	private DataOutputStream TGOut;

	protected Schema schema;

	/**
	 * Maps domain names to the respective Domains.
	 */
	private final Map<String, Domain> domains;

	/**
	 * Maps GraphElementClasses to their containing GraphClasses
	 */
	protected final Map<GraphElementClass<?, ?>, GraphClass> GECsearch;

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
	protected final Map<String, List<GraphElementClassData>> edgeClassBuffer;

	private final Map<String, List<String>> commentData;

	private int putBackChar;

	private String currentPackageName;

	private ByteArrayOutputStream BAOut;

	// stringPool allows re-use string values, saves memory if
	// multiple identical strings are used as attribute values
	private final HashMap<String, String> stringPool;
	private GraphFactory graphFactory;


	protected GraphIO() {
		this.domains = new TreeMap<String, Domain>();
		this.GECsearch = new HashMap<GraphElementClass<?, ?>, GraphClass>();
		this.buffer = new byte[BUFFER_SIZE];
		this.bufferPos = 0;
		this.enumDomainBuffer = new HashSet<EnumDomainData>();
		this.recordDomainBuffer = new ArrayList<RecordDomainData>();
		this.graphClass = null;
		this.vertexClassBuffer = new TreeMap<String, List<GraphElementClassData>>();
		this.edgeClassBuffer = new TreeMap<String, List<GraphElementClassData>>();
		this.commentData = new HashMap<String, List<String>>();
		this.stringPool = new HashMap<String, String>();
		this.putBackChar = -1;
	}

	public static Schema loadSchemaFromFile(String filename)
			throws GraphIOException {
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
			throw new GraphIOException("Exception while loading schema from "
					+ filename, ex);
		} finally {
			close(in);
		}
	}

	public static Schema loadSchemaFromStream(InputStream in)
			throws GraphIOException {
		try {
			GraphIO io = new GraphIO();
			io.TGIn = in;
			io.tgfile();
			io.schema.finish();
			return io.schema;
		} catch (Exception e) {
			throw new GraphIOException("Exception while loading schema.", e);
		}
	}

	public static Schema loadSchemaFromDatabase(GraphDatabase graphDatabase,
			String packagePrefix, String schemaName) throws GraphIOException {
		String definition = graphDatabase.getSchemaDefinition(packagePrefix,
				schemaName);
		InputStream input = new ByteArrayInputStream(definition.getBytes());
		return loadSchemaFromStream(input);
	}

	public static void loadSchemaIntoGraphDatabase(String filePath,
			GraphDatabase graphDatabase) throws IOException, GraphIOException,
			SQLException {
		Schema schema = loadSchemaFromFile(filePath);
		graphDatabase.insertSchema(schema);
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
		DataOutputStream out = null;
		try {
			out = new DataOutputStream(new BufferedOutputStream(
					new FileOutputStream(new File(filename))));
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
	 *            a DataOutputStream
	 * @throws GraphIOException
	 *             if an IOException occurs
	 */
	public static void saveSchemaToStream(Schema schema, DataOutputStream out)
			throws GraphIOException {
		GraphIO io = new GraphIO();
		io.TGOut = out;
		try {
			io.saveHeader();
			io.saveSchema(schema);
			out.flush();
		} catch (IOException e) {
			throw new GraphException("Exception while saving schema", e);
		}
	}

	private void saveSchema(Schema s) throws IOException {
		this.schema = s;
		this.write("Schema");
		this.space();
		this.writeIdentifier(this.schema.getQualifiedName());
		this.write(";\n");

		// write graphclass
		GraphClass gc = this.schema.getGraphClass();
		this.write("GraphClass");
		this.space();
		this.writeIdentifier(gc.getSimpleName());
		this.writeAttributes(null, gc);
		this.writeConstraints(gc);
		this.write(";\n");
		this.writeComments(gc, gc.getSimpleName());

		Queue<de.uni_koblenz.jgralab.schema.Package> worklist = new LinkedList<de.uni_koblenz.jgralab.schema.Package>();
		worklist.offer(s.getDefaultPackage());
		while (!worklist.isEmpty()) {
			Package pkg = worklist.poll();
			worklist.addAll(pkg.getSubPackages().values());

			// write package declaration
			if (!pkg.isDefaultPackage()) {
				this.write("Package");
				this.space();
				this.writeIdentifier(pkg.getQualifiedName());
				this.write(";\n");
			}

			// write domains
			for (Domain dom : pkg.getDomains().values()) {
				if (dom instanceof EnumDomain) {
					EnumDomain ed = (EnumDomain) dom;
					this.write("EnumDomain");
					this.space();
					this.writeIdentifier(ed.getSimpleName());
					this.write(" (");
					for (Iterator<String> eit = ed.getConsts().iterator(); eit
							.hasNext();) {
						this.space();
						this.writeIdentifier(eit.next());
						if (eit.hasNext()) {
							this.write(",");
						}
					}
					this.write(" );\n");
					this.writeComments(ed, ed.getSimpleName());
				} else if (dom instanceof RecordDomain) {
					RecordDomain rd = (RecordDomain) dom;
					this.write("RecordDomain");
					this.space();
					this.writeIdentifier(rd.getSimpleName());
					String delim = " ( ";
					for (RecordComponent rdc : rd.getComponents()) {
						this.write(delim);
						this.noSpace();
						this.writeIdentifier(rdc.getName());
						this.write(": ");
						this.write(rdc.getDomain().getTGTypeName(pkg));
						delim = ", ";
					}
					this.write(" );\n");
					this.writeComments(rd, rd.getSimpleName());
				}
			}

			// write vertex classes
			for (VertexClass vc : pkg.getVertexClasses().values()) {
				if (vc.isInternal()) {
					continue;
				}
				if (vc.isAbstract()) {
					this.write("abstract ");
				}
				this.write("VertexClass");
				this.space();
				this.writeIdentifier(vc.getSimpleName());
				this.writeHierarchy(pkg, vc);
				this.writeAttributes(pkg, vc);
				this.writeConstraints(vc);
				this.write(";\n");
				this.writeComments(vc, vc.getSimpleName());
			}

			// write edge classes
			for (EdgeClass ec : pkg.getEdgeClasses().values()) {
				if (ec.isInternal()) {
					continue;
				}
				if (ec.isAbstract()) {
					this.write("abstract ");
				}
				this.write("EdgeClass");
				this.space();
				this.writeIdentifier(ec.getSimpleName());
				this.writeHierarchy(pkg, ec);

				// from (min,max) rolename
				this.write(" from");
				this.space();
				this.writeIdentifier(ec.getFrom().getVertexClass()
						.getQualifiedName(pkg));
				this.write(" (");
				this.write(ec.getFrom().getMin() + ",");
				if (ec.getFrom().getMax() == Integer.MAX_VALUE) {
					this.write("*)");
				} else {
					this.write(ec.getFrom().getMax() + ")");
				}

				if (!ec.getFrom().getRolename().equals("")) {
					this.write(" role");
					this.space();
					this.writeIdentifier(ec.getFrom().getRolename());
					String delim = " redefines";
					for (String redefinedRolename : ec.getFrom()
							.getRedefinedRoles()) {
						this.write(delim);
						delim = ",";
						this.space();
						this.writeIdentifier(redefinedRolename);
					}
				}

				switch (ec.getFrom().getAggregationKind()) {
				case NONE:
					// do nothing
					break;
				case SHARED:
					this.write(" aggregation shared");
					break;
				case COMPOSITE:
					this.write(" aggregation composite");
					break;
				}

				// to (min,max) rolename
				this.write(" to");
				this.space();
				this.writeIdentifier(ec.getTo().getVertexClass()
						.getQualifiedName(pkg));
				this.write(" (");
				this.write(ec.getTo().getMin() + ",");
				if (ec.getTo().getMax() == Integer.MAX_VALUE) {
					this.write("*)");
				} else {
					this.write(ec.getTo().getMax() + ")");
				}

				if (!ec.getTo().getRolename().equals("")) {
					this.write(" role");
					this.space();
					this.writeIdentifier(ec.getTo().getRolename());
					String delim = " redefines";
					for (String redefinedRolename : ec.getTo()
							.getRedefinedRoles()) {
						this.write(delim);
						delim = ",";
						this.space();
						this.writeIdentifier(redefinedRolename);
					}
				}

				switch (ec.getTo().getAggregationKind()) {
				case NONE:
					// do nothing
					break;
				case SHARED:
					this.write(" aggregation shared");
					break;
				case COMPOSITE:
					this.write(" aggregation composite");
					break;
				}

				this.writeAttributes(pkg, ec);
				this.writeConstraints(ec);
				this.write(";\n");
				this.writeComments(ec, ec.getSimpleName());
			}

			// write package comments
			this.writeComments(pkg, "." + pkg.getQualifiedName());
		}
	}

	private void writeComments(NamedElement elem, String name)
			throws IOException {
		if (!elem.getComments().isEmpty()) {
			this.write("Comment");
			this.space();
			this.writeIdentifier(name);
			this.space();
			for (String c : elem.getComments()) {
				this.writeUtfString(c);
			}
			this.write(";\n");
		}
	}


	private void writeConstraints(AttributedElementClass<?, ?> aec)
			throws IOException {
		for (Constraint c : aec.getConstraints()) {
			this.writeSpace();
			this.write("[");
			this.noSpace();
			this.writeUtfString(c.getMessage());
			this.writeUtfString(c.getPredicate());
			if (c.getOffendingElementsQuery() != null) {
				this.writeUtfString(c.getOffendingElementsQuery());
			}
			this.noSpace();
			this.write("]");
			this.space();
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
		DataOutputStream out = null;
		try {
			if (filename.toLowerCase().endsWith(".gz")) {
				out = new DataOutputStream(new GZIPOutputStream(
						new FileOutputStream(filename), BUFFER_SIZE));
			} else {
				out = new DataOutputStream(new BufferedOutputStream(
						new FileOutputStream(filename), BUFFER_SIZE));
			}
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
	public static void saveGraphToFile(BooleanGraphMarker subGraph,
			String filename, ProgressFunction pf) throws GraphIOException {
		DataOutputStream out = null;
		try {
			if (filename.toLowerCase().endsWith(".gz")) {
				out = new DataOutputStream(new GZIPOutputStream(
						new FileOutputStream(filename), BUFFER_SIZE));
			} else {
				out = new DataOutputStream(new BufferedOutputStream(
						new FileOutputStream(filename), BUFFER_SIZE));
			}
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
	 *            a DataOutputStream
	 * @param pf
	 *            a {@link ProgressFunction}, may be <code>null</code>
	 * @throws GraphIOException
	 *             if an IOException occurs
	 */
	public static void saveGraphToStream(Graph graph, DataOutputStream out,
			ProgressFunction pf) throws GraphIOException {
		try {
			GraphIO io = new GraphIO();
			io.TGOut = out;
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
	 *            a DataOutputStream
	 * @param subGraph
	 *            a BooleanGraphMarker denoting the subgraph to be saved
	 * @param pf
	 *            a {@link ProgressFunction}, may be <code>null</code>
	 * @throws GraphIOException
	 *             if an IOException occurs
	 */
	public static void saveGraphToStream(BooleanGraphMarker subGraph,
			DataOutputStream out, ProgressFunction pf) throws GraphIOException {
		try {
			GraphIO io = new GraphIO();
			io.TGOut = out;
			io.saveGraph((InternalGraph) subGraph.getGraph(), pf, subGraph);
			out.flush();
		} catch (IOException e) {
			throw new GraphIOException("Exception while saving graph", e);
		}
	}

	private void saveGraph(InternalGraph graph, ProgressFunction pf,
			BooleanGraphMarker subGraph) throws IOException, GraphIOException {
		TraversalContext tc = graph.setTraversalContext(null);
		try {
			// Write the jgralab version and license in a comment
			this.saveHeader();

			this.schema = graph.getSchema();
			this.saveSchema(this.schema);

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

			this.space();
			this.write("Graph " + toUtfString(graph.getId()) + " "
					+ graph.getGraphVersion());
			this.writeIdentifier(graph.getAttributedElementClass()
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
			this.write(" (" + graph.getMaxVCount() + " " + graph.getMaxECount()
					+ " " + vCount + " " + eCount + ")");
			this.space();
			graph.writeAttributeValues(this);
			this.write(";\n");

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
					this.write("Package");
					this.space();
					this.writeIdentifier(currentPackage.getQualifiedName());
					this.write(";\n");
					oldPackage = currentPackage;
				}
				this.write(Long.toString(vId));
				this.space();
				this.writeIdentifier(aec.getSimpleName());
				// write incident edges
				Edge nextI = nextV.getFirstIncidence();
				this.write(" <");
				this.noSpace();
				// System.out.print("  Writing incidences of vertex.");
				while (nextI != null) {
					if ((subGraph != null) && !subGraph.isMarked(nextI)) {
						nextI = nextI.getNextIncidence();
						continue;
					}
					this.writeLong(nextI.getId());
					nextI = nextI.getNextIncidence();
				}
				this.write(">");
				this.space();
				nextV.writeAttributeValues(this);
				this.write(";\n");
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
					this.write("Package");
					this.space();
					this.writeIdentifier(currentPackage.getQualifiedName());
					this.write(";\n");
					oldPackage = currentPackage;
				}
				this.write(Long.toString(eId));
				this.space();
				this.writeIdentifier(aec.getSimpleName());
				this.space();
				nextE.writeAttributeValues(this);
				this.write(";\n");
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
			this.TGOut.flush();
			// finish progress bar
			if (pf != null) {
				pf.finished();
			}
		} finally {
			graph.setTraversalContext(tc);
		}
	}

	private void saveHeader() throws IOException {
		this.write(JGraLab.getVersionInfo(true));
		this.write("TGraph " + TGFILE_VERSION + ";\n");
	}

	private void writeHierarchy(Package pkg, AttributedElementClass<?, ?> aec)
			throws IOException {
		String delim = ":";
		for (AttributedElementClass<?, ?> superClass : aec
				.getDirectSuperClasses()) {
			if (!superClass.isInternal()) {
				this.write(delim);
				this.space();
				this.writeIdentifier(superClass.getQualifiedName(pkg));
				delim = ",";
			}
		}
	}


	private void writeAttributes(Package pkg, AttributedElementClass<?, ?> aec)
			throws IOException {
		if (aec.hasOwnAttributes()) {
			this.write(" {");
		}
		for (Iterator<Attribute> ait = aec.getOwnAttributeList().iterator(); ait
				.hasNext();) {
			Attribute a = ait.next();
			this.space();
			this.writeIdentifier(a.getName());
			this.write(": ");
			String domain = a.getDomain().getTGTypeName(pkg);
			this.write(domain);
			if ((a.getDefaultValueAsString() != null)
					&& !a.getDefaultValueAsString().equals("n")) {
				this.write(" = ");
				this.writeUtfString(a.getDefaultValueAsString());
			}
			if (ait.hasNext()) {
				this.write(", ");
			} else {
				this.write(" }");
			}
		}
	}

	public final void write(String s) throws IOException {
		this.TGOut.writeBytes(s);
	}

	public final void noSpace() {
		this.writeSpace = false;
	}

	public final void space() {
		this.writeSpace = true;
	}

	public final void writeSpace() throws IOException {
		if (this.writeSpace) {
			this.TGOut.writeBytes(" ");
		}
		this.writeSpace = true;
	}

	public final void writeBoolean(boolean b) throws IOException {
		this.writeSpace();
		this.TGOut.writeBytes(b ? TRUE_LITERAL : FALSE_LITERAL);
	}

	public final void writeInteger(int i) throws IOException {
		this.writeSpace();
		this.TGOut.writeBytes(Integer.toString(i));
	}

	public final void writeLong(long l) throws IOException {
		this.writeSpace();
		this.TGOut.writeBytes(Long.toString(l));
	}

	public final void writeDouble(double d) throws IOException {
		this.writeSpace();
		this.TGOut.writeBytes(Double.toString(d));
	}

	public final void writeUtfString(String s) throws IOException {
		this.writeSpace();
		this.TGOut.writeBytes(s == null ? NULL_LITERAL : toUtfString(s));
	}

	public final void writeIdentifier(String s) throws IOException {
		this.writeSpace();
		this.TGOut.writeBytes(s);
	}

	public static GraphIO createStringReader(String input, Schema schema)
			throws GraphIOException {
		GraphIO io = new GraphIO();
		io.TGIn = new ByteArrayInputStream(input.getBytes(Charset
				.forName("US-ASCII")));
		io.line = 1;
		io.schema = schema;
		io.la = io.read();
		io.match();
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
		if (this.BAOut == null) {
			throw new GraphIOException("GraphIO did not write to a String.");
		}
		try {
			try {
				this.TGOut.flush();
				this.BAOut.flush();
				String result = this.BAOut.toString("US-ASCII");
				return result;
			} finally {
				close(this.TGOut);
			}
		} finally {
			close(this.BAOut);
		}
	}


	public static Graph loadGraphFromFile(String filename, ProgressFunction pf)
			throws GraphIOException {
		return loadGraphFromFile(filename, ImplementationType.STANDARD, pf);
	}


	public static Graph loadGraphFromFile(String filename,
			ImplementationType implementationType, ProgressFunction pf)
					throws GraphIOException {
		if ((implementationType == null)
				|| (implementationType == ImplementationType.DATABASE)) {
			throw new IllegalArgumentException(
					"ImplementationType must be != null and != DATABASE");
		}

		FileInputStream fileStream = null;
		try {
			logger.finer("Loading graph " + filename);
			fileStream = new FileInputStream(filename);
			InputStream inputStream = null;
			try {
				if (filename.toLowerCase().endsWith(".gz")) {
					inputStream = new GZIPInputStream(fileStream, BUFFER_SIZE);
				} else {
					inputStream = new BufferedInputStream(fileStream,
							BUFFER_SIZE);
				}
				return loadGraphFromStream(inputStream, null, null,
						implementationType, pf);
			} catch (IOException ex) {
				throw new GraphIOException(
						"Exception while loading graph from file " + filename,
						ex);
			} finally {
				close(inputStream);
			}
		} catch (IOException ex) {
			throw new GraphIOException(
					"Exception while loading graph from file " + filename, ex);
		} finally {
			close(fileStream);
		}
	}

	public static <G extends Graph> G loadGraphFromFile(String filename,
			Schema schema, ImplementationType implementationType,
			ProgressFunction pf) throws GraphIOException {
		if (schema == null) {
			throw new IllegalArgumentException("Schema must be != null");
		}
		if ((implementationType == null)
				|| (implementationType == ImplementationType.DATABASE)) {
			throw new IllegalArgumentException(
					"ImplementationType must be != null and != DATABASE");
		}
		GraphFactory factory = schema
				.createDefaultGraphFactory(implementationType);
		return loadGraphFromFile(filename, factory, pf);
	}

	public static <G extends Graph> G loadGraphFromFile(String filename,
			GraphFactory factory, ProgressFunction pf) throws GraphIOException {
		if (factory == null) {
			throw new IllegalArgumentException("GraphFactory must be != null");
		}
		FileInputStream fileStream = null;
		try {
			logger.finer("Loading graph " + filename);
			fileStream = new FileInputStream(filename);
			InputStream inputStream = null;
			try {
				if (filename.toLowerCase().endsWith(".gz")) {
					inputStream = new GZIPInputStream(fileStream, BUFFER_SIZE);
				} else {
					inputStream = new BufferedInputStream(fileStream,
							BUFFER_SIZE);
				}
				return loadGraphFromStream(inputStream, factory.getSchema(),
						factory, factory.getImplementationType(), pf);
			} catch (IOException ex) {
				throw new GraphIOException(
						"Exception while loading graph from file " + filename,
						ex);
			} finally {
				close(inputStream);
			}
		} catch (IOException ex) {
			throw new GraphIOException(
					"Exception while loading graph from file " + filename, ex);
		} finally {
			close(fileStream);
		}
	}

	public static <G extends Graph> G loadGraphFromDatabase(String id,
			GraphDatabase graphDatabase) throws GraphDatabaseException {
		if (graphDatabase != null) {
			return graphDatabase.getGraph(id);
		} else {
			throw new GraphDatabaseException("No graph database given.");
		}
	}

	protected static void close(Closeable stream) throws GraphIOException {
		try {
			if (stream != null) {
				stream.close();
			}
		} catch (IOException ex) {
			throw new GraphIOException("Exception while closing stream.", ex);
		}
	}


	public static <G extends Graph> G loadGraphFromStream(InputStream in,
			Schema schema, GraphFactory graphFactory,
			ImplementationType implementationType, ProgressFunction pf)
					throws GraphIOException {
		try {
			GraphIO io = new GraphIO();
			io.TGIn = in;
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


	protected void tgfile() throws GraphIOException, SchemaException, IOException {
		this.line = 1;
		this.la = this.read();
		this.match();
		this.header();
		this.schema();
		if (this.lookAhead.equals("") || this.lookAhead.equals("Graph")) {
			return;
		}
		throw new GraphIOException("Symbol '" + this.lookAhead
				+ "' not recognized in line " + this.line, null);
	}

	/**
	 * Reads TG File header and checks if the file version can be processed.
	 * 
	 * @throws GraphIOException
	 *             if version number in file can not be processed
	 */
	private void header() throws GraphIOException {
		this.match("TGraph");
		int version = this.matchInteger();
		if (version != TGFILE_VERSION) {
			throw new GraphIOException("Can't read TGFile version " + version
					+ ". Expected version " + TGFILE_VERSION);
		}
		this.match(";");
	}

	/**
	 * Reads a Schema together with its Domains, GraphClasses and
	 * GraphElementClasses from a TG-file. Subsequently, the Schema is created.
	 * 
	 * @throws GraphIOException
	 */
	protected void schema() throws GraphIOException, SchemaException {
		this.currentPackageName = "";
		this.match("Schema");
		String[] qn = this.matchQualifiedName(true);
		if (qn[0].equals("")) {
			throw new GraphIOException("Invalid schema name '" + this.lookAhead
					+ "', package prefix must not be empty in line " + this.line);
		}
		this.match(";");

		if (this.schema != null) {
			// We already have a schema, so we don't want to load the schema
			// from the file

			// but wait, check if the names match...
			if (this.schema.getQualifiedName().equals(qn[0] + "." + qn[1])) {
				// yes, everything is fine :-)
				// skip schema part
				//
				// Beware: it's totally ok to have a VertexClass Graph, so
				// lookAhead = Graph is a too weak check. So we test that before
				// the Graph, the last token is a ;, too.
				String prev = "";
				while ((this.lookAhead.length() > 0)
						&& !(prev.equals(";") && this.lookAhead.equals("Graph"))) {
					prev = this.lookAhead;
					this.match();
				}
				return;
			} else {
				throw new GraphIOException(
						"Trying to load a graph with wrong schema. Expected: "
								+ this.schema.getQualifiedName() + ", but found "
								+ qn[0] + "." + qn[1]);
			}
		}

		this.schema = this.createSchema(qn[1],qn[0]);

		// read Domains and GraphClasses with contained GraphElementClasses
		this.parseSchema();

		// test for correct syntax, because otherwise, the following
		// sorting/creation methods probably can't work.
		if (!(this.lookAhead.equals("") || this.lookAhead.equals("Graph"))) {
			throw new GraphIOException("Symbol '" + this.lookAhead
					+ "' not recognized in line " + this.line, null);
		}

		// sort data of RecordDomains, GraphClasses and GraphElementClasses in
		// topological order

		this.checkFromToVertexClasses();

		this.sortRecordDomains();
		this.sortVertexClasses();
		this.sortEdgeClasses();

		this.domDef(); // create Domains
		this.completeGraphClass(); // create GraphClasses with contained elements
		this.buildHierarchy(); // build inheritance relationships
		this.processComments();
	}




	protected Schema createSchema(String name, String prefix){
		return new SchemaImpl(name,prefix);
	}

	/**
	 * Adds comments collected during schema parsing to the annotated elements.
	 * 
	 * @throws GraphIOException
	 */
	private void processComments() throws GraphIOException {
		for (Entry<String, List<String>> e : this.commentData.entrySet()) {
			if (!this.schema.knows(e.getKey())) {
				throw new GraphIOException("Annotated element '" + e.getKey()
						+ "' not found in schema " + this.schema.getQualifiedName());
			}
			NamedElement el = this.schema.getNamedElement(e.getKey());
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
		this.enumDomains(); // create EnumDomains
		this.recordDomains(); // create RecordDomains
		return this.domains;
	}

	/**
	 * Reads an EnumDomain, i.e. its name along with the enum constants.
	 * 
	 * @throws GraphIOException
	 */
	private void parseEnumDomain() throws GraphIOException {
		this.match("EnumDomain");
		String[] qn = this.matchQualifiedName(true);
		this.enumDomainBuffer.add(new EnumDomainData(qn[0], qn[1],
				this.parseEnumConstants()));
		this.match(";");
	}

	/**
	 * Creates all EnumDomains whose data is stored in {@link enumDomainBuffer}
	 */
	private void enumDomains() {
		Domain domain;

		for (EnumDomainData enumDomainData : this.enumDomainBuffer) {
			String qName = this.toQNameString(enumDomainData.packageName,
					enumDomainData.simpleName);
			domain = this.schema.createEnumDomain(qName,
					enumDomainData.enumConstants);
			this.domains.put(qName, domain);
		}
	}

	/**
	 * Read a RecordDomain, i.e. its name along with the components.
	 * 
	 * @throws GraphIOException
	 */
	private void parseRecordDomain() throws GraphIOException {
		this.match("RecordDomain");
		String[] qn = this.matchQualifiedName(true);
		this.recordDomainBuffer.add(new RecordDomainData(qn[0], qn[1],
				this.parseRecordComponents()));
		this.match(";");
	}

	/**
	 * Creates all RecordDomains whose data is stored in
	 * {@link recordDomainBuffer} @
	 */
	private void recordDomains() throws GraphIOException, SchemaException {
		Domain domain;

		for (RecordDomainData recordDomainData : this.recordDomainBuffer) {
			String qName = this.toQNameString(recordDomainData.packageName,
					recordDomainData.simpleName);
			domain = this.schema.createRecordDomain(qName,
					this.getComponents(recordDomainData.components));
			this.domains.put(qName, domain);
		}
	}

	private List<RecordComponent> getComponents(
			List<ComponentData> componentsData) throws GraphIOException {
		List<RecordComponent> result = new ArrayList<RecordComponent>(
				componentsData.size());

		for (ComponentData ad : componentsData) {
			RecordComponent c = new RecordComponent(ad.name,
					this.attrDomain(ad.domainDescription));
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
		while (this.lookAhead.equals("Comment")) {
			this.parseComment();
		}
		String currentGraphClassName = this.parseGraphClass();

		while (this.lookAhead.equals("Package") || this.lookAhead.equals("RecordDomain")
				|| this.lookAhead.equals("EnumDomain")
				|| this.lookAhead.equals("abstract")
				|| this.lookAhead.equals("VertexClass")
				|| this.lookAhead.equals("EdgeClass") || this.lookAhead.equals("Comment")) {
			if (this.lookAhead.equals("Package")) {
				this.parsePackage();
			} else if (this.lookAhead.equals("RecordDomain")) {
				this.parseRecordDomain();
			} else if (this.lookAhead.equals("EnumDomain")) {
				this.parseEnumDomain();
			} else if (this.lookAhead.equals("Comment")) {
				this.parseComment();
			} else {
				this.parseGraphElementClass(currentGraphClassName);
			}
		}
	}

	private void parseComment() throws GraphIOException {
		this.match("Comment");
		String qName = this.toQNameString(this.matchQualifiedName());
		List<String> comments = new ArrayList<String>();
		comments.add(this.matchUtfString());
		while (!this.lookAhead.equals(";")) {
			comments.add(this.matchUtfString());
		}
		this.match(";");
		if (this.commentData.containsKey(qName)) {
			this.commentData.get(qName).addAll(comments);
		} else {
			this.commentData.put(qName, comments);
		}
	}

	private void parsePackage() throws GraphIOException {
		this.match("Package");
		this.currentPackageName = "";
		if (this.lookAhead.equals(";")) {
			this.currentPackageName = "";
		} else {
			String[] qn = this.matchQualifiedName(false);
			String qualifiedName = this.toQNameString(qn);
			if (!isValidPackageName(qn[1])) {
				throw new GraphIOException("Invalid package name '"
						+ qualifiedName + "' in line " + this.line);
			}
			this.currentPackageName = qualifiedName;
		}
		this.match(";");
	}

	/**
	 * Creates the GraphClass contained in the Schema along with its
	 * GraphElementClasses.
	 * 
	 * @throws GraphIOException
	 * @throws SchemaException
	 */
	private void completeGraphClass() throws GraphIOException, SchemaException {
		GraphClass currentGraphClass = this.createGraphClass(this.graphClass);
		for (GraphElementClassData currentGraphElementClassData : this.vertexClassBuffer
				.get(this.graphClass.name)) {
			this.createVertexClass(currentGraphElementClassData, currentGraphClass);
		}
		for (GraphElementClassData currentGraphElementClassData : this.edgeClassBuffer
				.get(this.graphClass.name)) {
			this.createEdgeClass(currentGraphElementClassData, currentGraphClass);
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
		this.match("GraphClass");
		this.graphClass = new GraphClassData();

		this.graphClass.name = this.matchSimpleName(true);
		if (this.lookAhead.equals("{")) {
			this.graphClass.attributes = this.parseAttributes();
		}

		if (this.lookAhead.equals("[")) {
			// There are constraints
			this.graphClass.constraints = this.parseConstraints();
		}

		this.match(";");

		this.vertexClassBuffer.put(this.graphClass.name,
				new ArrayList<GraphElementClassData>());
		this.edgeClassBuffer.put(this.graphClass.name,
				new ArrayList<GraphElementClassData>());

		return this.graphClass.name;
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
		GraphClass gc = this.schema.createGraphClass(gcData.name);

		gc.setAbstract(gcData.isAbstract);

		this.addAttributes(gcData.attributes, gc);

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
		this.match(":");
		String[] qn = this.matchQualifiedName(true);
		hierarchy.add(this.toQNameString(qn));
		while (this.lookAhead.equals(",")) {
			this.match();
			qn = this.matchQualifiedName(true);
			hierarchy.add(this.toQNameString(qn));
		}
		return hierarchy;
	}

	private List<AttributeData> parseAttributes() throws GraphIOException {
		List<AttributeData> attributesData = new ArrayList<AttributeData>();
		Set<String> names = new TreeSet<String>();

		this.match("{");
		AttributeData ad = new AttributeData();
		ad.name = this.matchSimpleName(false);
		this.match(":");
		ad.domainDescription = this.parseAttrDomain();
		if (this.lookAhead.equals("=")) {
			this.match();
			ad.defaultValue = this.matchUtfString();
		}
		attributesData.add(ad);
		names.add(ad.name);

		while (this.lookAhead.equals(",")) {
			this.match(",");
			ad = new AttributeData();
			ad.name = this.matchSimpleName(false);
			this.match(":");
			ad.domainDescription = this.parseAttrDomain();
			if (this.lookAhead.equals("=")) {
				this.match();
				ad.defaultValue = this.matchUtfString();
			}
			if (names.contains(ad.name)) {
				throw new GraphIOException("Duplicate attribute name '"
						+ ad.name + "' in line " + this.line);
			}
			attributesData.add(ad);
			names.add(ad.name);
		}
		this.match("}");
		return attributesData;
	}

	protected void addAttributes(List<AttributeData> attributesData,
			AttributedElementClass<?, ?> aec) throws GraphIOException {
		for (AttributeData ad : attributesData) {
			aec.addAttribute(this.schema.createAttribute(ad.name, this.attrDomain(ad.domainDescription), aec, ad.defaultValue));
			//aec.addAttribute(ad.name, attrDomain(ad.domainDescription),
			//	ad.defaultValue);
		}
	}

	private List<String> parseAttrDomain() throws GraphIOException {
		List<String> result = new ArrayList<String>();
		this.parseAttrDomain(result);
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
		if (this.lookAhead.matches("[.]?List")) {
			this.match();
			this.match("<");
			attrDomain.add("List<");
			this.parseAttrDomain(attrDomain);
			this.match(">");
		} else if (this.lookAhead.matches("[.]?Set")) {
			this.match();
			this.match("<");
			attrDomain.add("Set<");
			this.parseAttrDomain(attrDomain);
			this.match(">");
		} else if (this.lookAhead.matches("[.]?Map")) {
			this.match();
			this.match("<");
			attrDomain.add("Map<");
			this.parseAttrDomain(attrDomain);
			this.match(",");
			this.parseAttrDomain(attrDomain);
			this.match(">");
		} else {
			if (this.isBasicDomainName(this.lookAhead)) {
				attrDomain.add(this.lookAhead);
				this.match();
			} else {
				String[] qn = this.matchQualifiedName(true);
				attrDomain.add(this.toQNameString(qn));
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
					return this.schema.createListDomain(this.attrDomain(domainNames));
				} catch (SchemaException e) {
					throw new GraphIOException(
							"Can't create list domain in line " + this.line, e);
				}
			} else if (domainName.equals("Set<")) {
				try {
					return this.schema.createSetDomain(this.attrDomain(domainNames));
				} catch (SchemaException e) {
					throw new GraphIOException(
							"Can't create set domain in line " + this.line, e);
				}
			} else if (domainName.equals("Map<")) {
				try {
					Domain keyDomain = this.attrDomain(domainNames);
					Domain valueDomain = this.attrDomain(domainNames);
					if (keyDomain == null) {
						throw new GraphIOException(
								"Can't create map domain, because no key domain was given in line "
										+ this.line);
					}
					MapDomain result = this.schema.createMapDomain(keyDomain,
							valueDomain);
					// System.out.println("result = Map<"
					// + keyDomain.getQualifiedName() + ", "
					// + valueDomain.getQualifiedName() + ">");
					return result;
				} catch (SchemaException e) {
					throw new GraphIOException(
							"Can't create map domain in line " + this.line, e);
				}
			} else {
				Domain result = this.schema.getDomain(domainName);
				if (result == null) {
					throw new GraphIOException("Undefined domain '"
							+ domainName + "' in line " + this.line);
				}
				return result;
			}
		}
		throw new GraphIOException("Couldn't create domain for '" + domainNames
				+ "' in line " + this.line);
	}

	public final String matchEnumConstant() throws GraphIOException {
		if (this.schema.isValidEnumConstant(this.lookAhead)
				|| this.lookAhead.equals(NULL_LITERAL)) {
			return this.matchAndNext();
		}
		throw new GraphIOException("Invalid enumeration constant '" + this.lookAhead
				+ "' in line " + this.line);
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

		if (this.lookAhead.equals("abstract")) {
			this.match();
			graphElementClassData.isAbstract = true;
		}

		if (this.lookAhead.equals("VertexClass")) {
			this.match("VertexClass");
			String[] qn = this.matchQualifiedName(true);
			graphElementClassData.packageName = qn[0];
			graphElementClassData.simpleName = qn[1];
			if (this.lookAhead.equals(":")) {
				graphElementClassData.directSuperClasses = this.parseHierarchy();
			}
			this.vertexClassBuffer.get(gcName).add(graphElementClassData);
		} else if (this.lookAhead.equals("EdgeClass")) {
			this.match();
			String[] qn = this.matchQualifiedName(true);
			graphElementClassData.packageName = qn[0];
			graphElementClassData.simpleName = qn[1];
			if (this.lookAhead.equals(":")) {
				graphElementClassData.directSuperClasses = this.parseHierarchy();
			}
			this.match("from");
			String[] fqn = this.matchQualifiedName(true);
			graphElementClassData.fromVertexClassName = this.toQNameString(fqn);
			graphElementClassData.fromMultiplicity = this.parseMultiplicity();
			graphElementClassData.fromRoleName = this.parseRoleName();
			graphElementClassData.redefinedFromRoles = this.parseRolenameRedefinitions();
			graphElementClassData.fromAggregation = this.parseAggregation();

			this.match("to");
			String[] tqn = this.matchQualifiedName(true);
			graphElementClassData.toVertexClassName = this.toQNameString(tqn);
			graphElementClassData.toMultiplicity = this.parseMultiplicity();
			graphElementClassData.toRoleName = this.parseRoleName();
			graphElementClassData.redefinedToRoles = this.parseRolenameRedefinitions();
			graphElementClassData.toAggregation = this.parseAggregation();
			this.edgeClassBuffer.get(gcName).add(graphElementClassData);
		} else {
			throw new SchemaException("Undefined keyword: " + this.lookAhead
					+ " at position ");
		}

		if (this.lookAhead.equals("{")) {
			graphElementClassData.attributes = this.parseAttributes();
		}

		if (this.lookAhead.equals("[")) {
			// There are constraints
			graphElementClassData.constraints = this.parseConstraints();
		}
		this.match(";");
	}

	private Set<Constraint> parseConstraints() throws GraphIOException {
		// constraints have the form: ["msg" "pred" "optGreql"] or ["msg"
		// "pred"] and there may be as many as one wants...
		HashSet<Constraint> constraints = new HashSet<Constraint>(1);
		do {
			this.match("[");
			String msg = this.matchUtfString();
			String pred = this.matchUtfString();
			String greql = null;
			if (!this.lookAhead.equals("]")) {
				greql = this.matchUtfString();
			}
			constraints.add(new ConstraintImpl(msg, pred, greql));
			this.match("]");
		} while (this.lookAhead.equals("["));
		return constraints;
	}

	private VertexClass createVertexClass(GraphElementClassData vcd,
			GraphClass gc) throws GraphIOException, SchemaException {
		VertexClass vc = gc.createVertexClass(vcd.getQualifiedName());
		vc.setAbstract(vcd.isAbstract);

		this.addAttributes(vcd.attributes, vc);

		for (Constraint constraint : vcd.constraints) {
			vc.addConstraint(constraint);
		}

		this.GECsearch.put(vc, gc);
		return vc;
	}


	protected EdgeClass createEdgeClass(GraphElementClassData ecd, GraphClass gc)
			throws GraphIOException, SchemaException {
		EdgeClass ec = gc.createEdgeClass(ecd.getQualifiedName(),
				gc.getVertexClass(ecd.fromVertexClassName),
				ecd.fromMultiplicity[0], ecd.fromMultiplicity[1],
				ecd.fromRoleName, ecd.fromAggregation,
				gc.getVertexClass(ecd.toVertexClassName),
				ecd.toMultiplicity[0], ecd.toMultiplicity[1], ecd.toRoleName,
				ecd.toAggregation);

		this.addAttributes(ecd.attributes, ec);

		for (Constraint constraint : ecd.constraints) {
			ec.addConstraint(constraint);
		}

		ec.setAbstract(ecd.isAbstract);

		this.GECsearch.put(ec, gc);
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

		this.match("(");
		int min = this.matchInteger();
		if (min < 0) {
			throw new GraphIOException("Minimum multiplicity '" + min
					+ "' must be >=0 in line " + this.line);
		}
		this.match(",");
		int max;
		if (this.lookAhead.equals("*")) {
			max = Integer.MAX_VALUE;
			this.match();
		} else {
			max = this.matchInteger();
			if (max < min) {
				throw new GraphIOException("Maximum multiplicity '" + max
						+ "' must be * or >=" + min + " in line " + this.line);
			}
		}
		this.match(")");
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
		if (this.lookAhead.equals("role")) {
			this.match();
			String result = this.matchSimpleName(false);
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
		if (!this.lookAhead.equals("redefines")) {
			return null;
		}
		this.match();
		Set<String> result = new HashSet<String>();
		String redefinedName = this.matchSimpleName(false);
		result.add(redefinedName);
		while (this.lookAhead.equals(",")) {
			this.match();
			redefinedName = this.matchSimpleName(false);
			result.add(redefinedName);
		}
		return result;
	}

	private AggregationKind parseAggregation() throws GraphIOException {
		if (!this.lookAhead.equals("aggregation")) {
			return AggregationKind.NONE;
		}
		this.match();
		if (this.lookAhead.equals("none")) {
			this.match();
			return AggregationKind.NONE;
		} else if (this.lookAhead.equals("shared")) {
			this.match();
			return AggregationKind.SHARED;
		} else if (this.lookAhead.equals("composite")) {
			this.match();
			return AggregationKind.COMPOSITE;
		} else {
			throw new GraphIOException(
					"Invalid aggregation: expected 'none', 'shared', or 'composite', but found '"
							+ this.lookAhead + "' in line " + this.line);
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

		this.match("(");
		ComponentData cd = new ComponentData();
		cd.name = this.matchSimpleName(false);
		this.match(":");
		cd.domainDescription = this.parseAttrDomain();
		componentsData.add(cd);
		names.add(cd.name);

		while (this.lookAhead.equals(",")) {
			this.match(",");
			cd = new ComponentData();
			cd.name = this.matchSimpleName(false);
			this.match(":");
			cd.domainDescription = this.parseAttrDomain();
			if (names.contains(cd.name)) {
				throw new GraphIOException("Duplicate record component name '"
						+ cd.name + "' in line " + this.line);
			}
			componentsData.add(cd);
			names.add(cd.name);
		}
		this.match(")");
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
		this.match("(");
		List<String> enums = new ArrayList<String>();
		enums.add(this.matchEnumConstant());
		while (this.lookAhead.equals(",")) {
			this.match();
			String s = this.matchEnumConstant();
			if (enums.contains(s)) {
				throw new GraphIOException(
						"Duplicate enumeration constant name '" + this.lookAhead
						+ "' in line " + this.line);
			}
			enums.add(s);
		}
		this.match(")");
		return enums;
	}

	private void buildVertexClassHierarchy() throws GraphIOException,
	SchemaException {
		AttributedElementClass<?, ?> aec;
		VertexClass superClass;

		for (Entry<String, List<GraphElementClassData>> gcElements : this.vertexClassBuffer
				.entrySet()) {
			for (GraphElementClassData vData : gcElements.getValue()) {
				aec = this.schema
						.getAttributedElementClass(vData.getQualifiedName());
				if (aec == null) {
					throw new GraphIOException(
							"Undefined AttributedElementClass '"
									+ vData.getQualifiedName() + "'");
				}
				if (aec instanceof VertexClass) {
					for (String superClassName : vData.directSuperClasses) {
						superClass = (VertexClass) this.GECsearch.get(aec)
								.getGraphElementClass(superClassName);
						if (superClass == null) {
							throw new GraphIOException(
									"Undefined VertexClass '" + superClassName
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
		AttributedElementClass<?, ?> aec;
		EdgeClass superClass;

		for (Entry<String, List<GraphElementClassData>> gcElements : this.edgeClassBuffer
				.entrySet()) {
			for (GraphElementClassData eData : gcElements.getValue()) {
				aec = this.schema
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
					superClass = (EdgeClass) this.GECsearch.get(aec)
							.getGraphElementClass(superClassName);
					if (superClass == null) {
						throw new GraphIOException("Undefined EdgeClass '"
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
		this.buildVertexClassHierarchy();
		this.buildEdgeClassHierarchy();
	}

	private final String nextToken() throws GraphIOException {
		StringBuilder out = new StringBuilder();
		this.isUtfString = false;
		this.skipWs();
		if (this.la == '"') {
			this.readUtfString(out);
			this.isUtfString = true;
		} else if (isSeparator(this.la)) {
			out.append((char) this.la);
			this.la = this.read();
		} else {
			if (this.la != -1) {
				do {
					out.append((char) this.la);
					this.la = this.read();
				} while (!isWs(this.la) && !isSeparator(this.la) && (this.la != -1));
			}
		}
		return out.toString();
	}

	private final int read() throws GraphIOException {
		try {
			if (this.putBackChar >= 0) {
				int result = this.putBackChar;
				this.putBackChar = -1;
				return result;
			}
			if (this.bufferPos < this.bufferSize) {
				return this.buffer[this.bufferPos++];
			} else {
				this.bufferSize = this.TGIn.read(this.buffer);
				if (this.bufferSize != -1) {
					this.bufferPos = 0;
					return this.buffer[this.bufferPos++];
				} else {
					return -1;
				}
			}
		} catch (IOException e) {
			throw new GraphIOException(
					"Error on reading bytes from file, line " + this.line
					+ ", last char read was "
					+ (this.la >= 0 ? "'" + (char) this.la + "'" : "end of file"),
					e);
		}
	}

	private final void readUtfString(StringBuilder out) throws GraphIOException {
		int startLine = this.line;
		this.la = this.read();
		LOOP: while ((this.la != -1) && (this.la != '"')) {
			if ((this.la < 32) || (this.la > 127)) {
				throw new GraphIOException("Invalid character '" + (char) this.la
						+ "' in string in line " + this.line);
			}
			if (this.la == '\\') {
				this.la = this.read();
				if (this.la == -1) {
					break LOOP;
				}
				switch (this.la) {
				case '\\':
					this.la = '\\';
					break;
				case '"':
					this.la = '"';
					break;
				case 'n':
					this.la = '\n';
					break;
				case 'r':
					this.la = '\r';
					break;
				case 't':
					this.la = '\t';
					break;
				case 'u':
					this.la = this.read();
					if (this.la == -1) {
						break LOOP;
					}
					String unicode = "" + (char) this.la;
					this.la = this.read();
					if (this.la == -1) {
						break LOOP;
					}
					unicode += (char) this.la;
					this.la = this.read();
					if (this.la == -1) {
						break LOOP;
					}
					unicode += (char) this.la;
					this.la = this.read();
					if (this.la == -1) {
						break LOOP;
					}
					unicode += (char) this.la;
					try {
						this.la = Integer.parseInt(unicode, 16);
					} catch (NumberFormatException e) {
						throw new GraphIOException(
								"Invalid unicode escape sequence '\\u"
										+ unicode + "' in line " + this.line);
					}
					break;
				default:
					throw new GraphIOException(
							"Invalid escape sequence in string in line " + this.line);
				}
			}
			out.append((char) this.la);
			this.la = this.read();
		}
		if (this.la == -1) {
			throw new GraphIOException("Unterminated string starting in line "
					+ startLine + ".  lookAhead = '" + this.lookAhead + "'");
		}
		this.la = this.read();
	}

	private final static boolean isWs(int c) {
		return (c == ' ') || (c == '\n') || (c == '\t') || (c == '\r');
	}

	private final static boolean isSeparator(int c) {
		return (c == ';') || (c == '<') || (c == '>') || (c == '(')
				|| (c == ')') || (c == '{') || (c == '}') || (c == ':')
				|| (c == '[') || (c == ']') || (c == ',') || (c == '=');
	}

	private final void skipWs() throws GraphIOException {
		// skip whitespace and consecutive single line comments
		do {
			// skip whitespace
			while (isWs(this.la)) {
				if (this.la == '\n') {
					++this.line;
				}
				this.la = this.read();
			}
			// skip single line comments
			if (this.la == '/') {
				this.la = this.read();
				if ((this.la >= 0) && (this.la == '/')) {
					// single line comment, skip to the end of the current line
					while ((this.la >= 0) && (this.la != '\n')) {
						this.la = this.read();
					}
				} else {
					this.putback(this.la);
				}
			}
		} while (isWs(this.la));
	}

	private final void putback(int ch) {
		this.putBackChar = ch;
	}

	private final String matchAndNext() throws GraphIOException {
		String result = this.lookAhead;
		this.match();
		return result;
	}

	public final boolean isNextToken(String token) {
		return this.lookAhead.equals(token);
	}

	public final void match() throws GraphIOException {
		this.lookAhead = this.nextToken();
	}

	public final void match(String s) throws GraphIOException {
		if (this.lookAhead.equals(s)) {
			this.lookAhead = this.nextToken();
		} else {
			throw new GraphIOException("Expected '"
					+ s
					+ "' but found "
					+ (this.lookAhead.equals("") ? "end of file" : "'" + this.lookAhead
							+ "'") + " in line " + this.line, null);
		}
	}

	public final int matchInteger() throws GraphIOException {
		try {
			int result = Integer.parseInt(this.lookAhead);
			this.match();
			return result;
		} catch (NumberFormatException e) {
			throw new GraphIOException("Expected int number but found "
					+ (this.lookAhead.equals("") ? "end of file" : "'" + this.lookAhead
							+ "'") + " in line " + this.line, e);
		}
	}

	public final long matchLong() throws GraphIOException {
		try {
			long result = Long.parseLong(this.lookAhead);
			this.match();
			return result;
		} catch (NumberFormatException e) {
			throw new GraphIOException("Expected long number but found "
					+ (this.lookAhead.equals("") ? "end of file" : "'" + this.lookAhead
							+ "'") + " in line " + this.line, e);
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
		String s = this.lookAhead;
		boolean ok = isValidIdentifier(s)
				&& ((isUpperCase && Character.isUpperCase(s.charAt(0))) || (!isUpperCase && Character
						.isLowerCase(s.charAt(0))));

		if (!ok) {
			throw new GraphIOException("Invalid simple name '" + this.lookAhead
					+ "' in line " + this.line);
		}
		this.match();
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

		String c = this.lookAhead.indexOf('.') >= 0 ? this.lookAhead : this.toQNameString(
				this.currentPackageName, this.lookAhead);
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
				&& ((isUpperCase && Character.isUpperCase(result[1].charAt(0))) || (!isUpperCase && Character
						.isLowerCase(result[1].charAt(0))));

		if (!ok) {
			throw new GraphIOException("Invalid qualified name '" + this.lookAhead
					+ "' in line " + this.line);
		}
		this.match();
		return result;
	}

	public final String[] matchQualifiedName() throws GraphIOException {
		String c = this.lookAhead.indexOf('.') >= 0 ? this.lookAhead : this.toQNameString(
				this.currentPackageName, this.lookAhead);
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
			throw new GraphIOException("Invalid qualified name '" + this.lookAhead
					+ "' in line " + this.line);
		}
		this.match();
		return result;
	}

	/**
	 * @param qn
	 * @return a string representation of a qualified name specified as array
	 *         (like returned by @{#matchQualifiedName}).
	 */
	private final String toQNameString(String[] qn) {
		return this.toQNameString(qn[0], qn[1]);
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
		if (!this.isUtfString && this.lookAhead.equals(NULL_LITERAL)) {
			this.match();
			return null;
		}
		if (this.isUtfString) {
			String result = this.lookAhead;
			this.match();
			String s = this.stringPool.get(result);
			if (s == null) {
				this.stringPool.put(result, result);
			} else {
				result = s;
			}
			return result;
		}
		throw new GraphIOException(
				"Expected a string constant but found "
						+ (this.lookAhead.equals("") ? "end of file" : "'"
								+ this.lookAhead + "'") + " in line " + this.line);
	}

	public final boolean matchBoolean() throws GraphIOException {
		if (!this.lookAhead.equals("t") && !this.lookAhead.equals("f")) {
			throw new GraphIOException(
					"Expected a boolean constant ('f' or 't') but found "
							+ (this.lookAhead.equals("") ? "end of file" : "'"
									+ this.lookAhead + "'") + " in line " + this.line);
		}
		boolean result = this.lookAhead.equals("t");
		this.match();
		return result;
	}


	private GraphBaseImpl graph(ProgressFunction pf) throws GraphIOException {
		this.currentPackageName = "";
		this.match("Graph");
		String graphId = this.matchUtfString();
		long graphVersion = this.matchLong();

		this.gcName = this.matchAndNext();
		assert !this.gcName.contains(".") && isValidIdentifier(this.gcName) : "illegal characters in graph class '"
		+ this.gcName + "'";
		// check if classname is known in the schema
		if (!this.schema.getGraphClass().getQualifiedName().equals(this.gcName)) {
			throw new GraphIOException("Graph Class " + this.gcName
					+ "does not exist in " + this.schema.getQualifiedName());
		}
		this.match("(");
		int maxV = this.matchInteger();
		int maxE = this.matchInteger();
		int vCount = this.matchInteger();
		int eCount = this.matchInteger();
		this.match(")");

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
		this.edgeIn = new Vertex[maxE + 1];
		this.edgeOut = new Vertex[maxE + 1];
		this.firstIncidence = new int[maxV + 1];
		this.nextIncidence = new int[(2 * maxE) + 1];
		this.edgeOffset = maxE;

		long graphElements = 0, currentCount = 0, interval = 1;
		if (pf != null) {
			pf.init(vCount + eCount);
			interval = pf.getUpdateInterval();
		}
		GraphBaseImpl graph = this.graphFactory.createGraph(this.schema.getGraphClass(),
				graphId, maxV, maxE);
		graph.setLoading(true);
		graph.readAttributeValues(this);
		this.match(";");

		int vNo = 1;
		while (vNo <= vCount) {
			if (this.lookAhead.equals("Package")) {
				this.parsePackage();
			} else {
				this.vertexDesc(graph);
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
			if (this.lookAhead.equals("Package")) {
				this.parsePackage();
			} else {
				this.edgeDesc(graph);
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
		graph.internalLoadingCompleted(this.firstIncidence, this.nextIncidence);
		this.firstIncidence = null;
		this.nextIncidence = null;
		graph.setLoading(false);
		graph.loadingCompleted();
		return graph;
	}

	public final double matchDouble() throws GraphIOException {
		try {
			double result = Double.parseDouble(this.lookAhead);
			this.match();
			return result;
		} catch (NumberFormatException e) {
			throw new GraphIOException("expected a double value but found '"
					+ this.lookAhead + "' in line " + this.line, e);
		}
	}


	private void vertexDesc(Graph graph) throws GraphIOException {
		int vId = this.vId();
		String vcName = this.className();
		VertexClass vc = (VertexClass) this.schema.getAttributedElementClass(vcName);
		Vertex vertex = this.graphFactory.createVertex(vc, vId, graph);
		this.parseIncidentEdges(vertex);
		vertex.readAttributeValues(this);
		this.match(";");
	}


	private void edgeDesc(Graph graph) throws GraphIOException {
		int eId = this.eId();
		String ecName = this.className();
		EdgeClass ec = (EdgeClass) this.schema.getAttributedElementClass(ecName);
		Edge edge = this.graphFactory.createEdge(ec, eId, graph, this.edgeOut[eId],
				this.edgeIn[eId]);
		edge.readAttributeValues(this);
		this.match(";");
	}

	private int eId() throws GraphIOException {
		int eId = this.matchInteger();
		if (eId == 0) {
			throw new GraphIOException("Invalid edge id " + eId + ".");
		}
		return eId;
	}

	private String className() throws GraphIOException {
		String[] qn = this.matchQualifiedName(true);
		return this.toQNameString(qn);
	}

	private int vId() throws GraphIOException {
		int vId = this.matchInteger();
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
		this.match("<");
		if (!this.lookAhead.equals(">")) {
			eId = this.eId();
			this.firstIncidence[vId] = eId;
			if (eId < 0) {
				this.edgeIn[-eId] = v;
			} else {
				this.edgeOut[eId] = v;
			}
		}
		while (!this.lookAhead.equals(">")) {
			prevId = eId;
			eId = this.eId();
			this.nextIncidence[this.edgeOffset + prevId] = eId;
			if (eId < 0) {
				this.edgeIn[-eId] = v;
			} else {
				this.edgeOut[eId] = v;
			}
		}
		this.match();
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
		while (!this.recordDomainBuffer.isEmpty()) {
			for (Iterator<RecordDomainData> rdit = this.recordDomainBuffer
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
							String qName = this.toQNameString(orderedRd.packageName,
									orderedRd.simpleName);
							if (componentDomain.equals(qName)) {
								componentDomsInOrderedList = true;
								break;
							}
						}
						for (EnumDomainData ed : this.enumDomainBuffer) {
							String qName = this.toQNameString(ed.packageName,
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

							for (RecordDomainData rd2 : this.recordDomainBuffer) {
								String qName = this.toQNameString(rd2.packageName,
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
		this.recordDomainBuffer = orderedRdList;
	}

	private void sortVertexClasses() throws GraphIOException {
		List<GraphElementClassData> orderedVcList, unorderedVcList;
		Set<String> orderedVcNames = new TreeSet<String>();
		GraphElementClassData vc;
		boolean definedVcName;

		unorderedVcList = this.vertexClassBuffer.get(this.graphClass.name);
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
		this.vertexClassBuffer.put(this.graphClass.name, orderedVcList);
	}

	private void sortEdgeClasses() throws GraphIOException {
		List<GraphElementClassData> orderedEcList, unorderedEcList;
		Set<String> orderedEcNames = new TreeSet<String>();
		GraphElementClassData ec;
		boolean definedEcName;

		unorderedEcList = this.edgeClassBuffer.get(this.graphClass.name);
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
		this.edgeClassBuffer.put(this.graphClass.name, orderedEcList);
	}

	/**
	 * checks if from- and to-VertexClasses given in EdgeClass definitions exist
	 */
	private void checkFromToVertexClasses() throws GraphIOException {
		boolean existingFromVertexClass;
		boolean existingToVertexClass;

		for (Entry<String, List<GraphElementClassData>> graphClassEdge : this.edgeClassBuffer
				.entrySet()) {
			for (GraphElementClassData ec : graphClassEdge.getValue()) {
				existingFromVertexClass = false;
				existingToVertexClass = false;

				for (Entry<String, List<GraphElementClassData>> graphClassVertex : this.vertexClassBuffer
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
	protected class GraphElementClassData {
		protected String simpleName;
		protected String packageName;

		public String getQualifiedName() {
			return GraphIO.this.toQNameString(this.packageName, this.simpleName);
		}

		public boolean isAbstract = false;

		public List<String> directSuperClasses = new LinkedList<String>();

		public String fromVertexClassName;

		public int[] fromMultiplicity = { 1, Integer.MAX_VALUE };

		public String fromRoleName = "";

		protected Set<String> redefinedFromRoles = null;

		public AggregationKind fromAggregation;

		public String toVertexClassName;

		public int[] toMultiplicity = { 1, Integer.MAX_VALUE };

		public String toRoleName = "";

		protected Set<String> redefinedToRoles = null;

		public AggregationKind toAggregation;

		public List<AttributeData> attributes = new ArrayList<AttributeData>();

		public Set<Constraint> constraints = new HashSet<Constraint>(1);
	}

}
