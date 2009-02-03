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

package de.uni_koblenz.jgralab;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.net.URL;
import java.nio.CharBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.Map.Entry;
import java.util.logging.Logger;

import de.uni_koblenz.jgralab.GraphIOException.GraphIOExceptionReason;
import de.uni_koblenz.jgralab.impl.GraphImpl;
import de.uni_koblenz.jgralab.schema.AggregationClass;
import de.uni_koblenz.jgralab.schema.AttributedElementClass;
import de.uni_koblenz.jgralab.schema.BasicDomain;
import de.uni_koblenz.jgralab.schema.CompositionClass;
import de.uni_koblenz.jgralab.schema.Domain;
import de.uni_koblenz.jgralab.schema.EdgeClass;
import de.uni_koblenz.jgralab.schema.EnumDomain;
import de.uni_koblenz.jgralab.schema.GraphClass;
import de.uni_koblenz.jgralab.schema.GraphElementClass;
import de.uni_koblenz.jgralab.schema.Package;
import de.uni_koblenz.jgralab.schema.QualifiedName;
import de.uni_koblenz.jgralab.schema.RecordDomain;
import de.uni_koblenz.jgralab.schema.Schema;
import de.uni_koblenz.jgralab.schema.VertexClass;
import de.uni_koblenz.jgralab.schema.exception.InvalidNameException;
import de.uni_koblenz.jgralab.schema.exception.SchemaException;
import de.uni_koblenz.jgralab.schema.impl.AttributeImpl;
import de.uni_koblenz.jgralab.schema.impl.SchemaImpl;

/**
 * class for loading and storing schema and graphs in tg format
 *
 * @author ist@uni-koblenz.de
 */
public class GraphIO {

	private static Logger logger = Logger.getLogger(GraphIO.class.getName());

	private InputStream TGIn;

	private DataOutputStream TGOut;

	private Schema schema;

	/**
	 * Maps the strings that form a qualified name to the correct qualified name
	 * object
	 */
	private Map<String, QualifiedName> qualifiedNameMap;

	/**
	 * Maps domain names to the respective Domains.
	 */
	private Map<QualifiedName, Domain> domains;

	/**
	 * Maps GraphElementClasses to their containing GraphClasses
	 */
	private Map<GraphElementClass, GraphClass> GECsearch;

	private Map<QualifiedName, Method> createMethods;

	private int line; // line number

	private int la; // lookahead character

	private String lookAhead; // lookahead token

	private boolean isUtfString; // lookahead is UTF string

	private boolean writeSpace; // if true, a space is written in the next
	// writeXXX()

	private QualifiedName gcName; // GraphClass name of the currently loaded
	// graph

	private byte buffer[];

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
	private Set<EnumDomainData> enumDomainBuffer;

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
	private Map<QualifiedName, List<GraphElementClassData>> vertexClassBuffer;

	/**
	 * Buffers the parsed data of edge classes prior to their creation in
	 * JGraLab.
	 */
	private Map<QualifiedName, List<GraphElementClassData>> edgeClassBuffer;

	private int putBackChar;

	private String currentPackageName;

	private Object[] vertexDescTempObject = { 0 };

	private Object[] edgeDescTempObject = { 0, 0, 0 };

	private GraphIO() {
		domains = new TreeMap<QualifiedName, Domain>();
		GECsearch = new HashMap<GraphElementClass, GraphClass>();
		createMethods = new HashMap<QualifiedName, Method>();
		buffer = new byte[65536];
		bufferPos = 0;
		enumDomainBuffer = new HashSet<EnumDomainData>();
		recordDomainBuffer = new ArrayList<RecordDomainData>();
		graphClass = null;
		vertexClassBuffer = new TreeMap<QualifiedName, List<GraphElementClassData>>();
		edgeClassBuffer = new TreeMap<QualifiedName, List<GraphElementClassData>>();
		putBackChar = -1;
		qualifiedNameMap = new HashMap<String, QualifiedName>();
	}

	public static void saveSchemaToFile(String filename, Schema schema)
			throws GraphIOException {
		try {
			GraphIO io = new GraphIO();
			DataOutputStream out = new DataOutputStream(
					new BufferedOutputStream(new FileOutputStream(new File(
							filename))));
			io.TGOut = out;
			io.saveSchema(schema);
			out.flush();
			out.close();
		} catch (IOException e) {
			throw new GraphIOException("exception while saving schema to '"
					+ filename + "'", e);
		}
	}

	public static void saveSchemaToStream(DataOutputStream out, Schema schema)
			throws GraphIOException {
		try {
			GraphIO io = new GraphIO();
			io.TGOut = out;
			io.saveSchema(schema);
			out.flush();
		} catch (IOException e) {
			throw new GraphIOException("exception while saving schema", e);
		}
	}

	private void saveSchema(Schema s) throws IOException {
		schema = s;
		TGOut.writeBytes("Schema");
		space();
		writeIdentifier(schema.getQualifiedName());
		TGOut.writeBytes(";\n");

		// write graphclass
		for (GraphClass gc : schema.getGraphClassesInTopologicalOrder()) {
			if (gc.isInternal()) {
				continue;
			}
			TGOut.writeBytes("GraphClass");
			space();
			writeIdentifier(gc.getSimpleName());
			writeAttributes(null, gc);
			TGOut.writeBytes(";\n");
		}

		Queue<de.uni_koblenz.jgralab.schema.Package> worklist = new LinkedList<de.uni_koblenz.jgralab.schema.Package>();
		worklist.offer(s.getDefaultPackage());
		while (!worklist.isEmpty()) {
			Package pkg = worklist.poll();
			worklist.addAll(pkg.getSubPackages().values());

			// write package declaration
			if (!pkg.isDefaultPackage()) {
				TGOut.writeBytes("Package");
				space();
				writeIdentifier(pkg.getQualifiedName());
				TGOut.writeBytes(";\n");
			}
			// write domains
			for (Domain dom : pkg.getDomains().values()) {
				if (dom instanceof EnumDomain) {
					EnumDomain ed = (EnumDomain) dom;
					TGOut.writeBytes("EnumDomain");
					space();
					writeIdentifier(ed.getSimpleName());
					TGOut.writeBytes(" (");
					for (Iterator<String> eit = ed.getConsts().iterator(); eit
							.hasNext();) {
						space();
						writeIdentifier(eit.next());
						if (eit.hasNext()) {
							TGOut.writeBytes(",");
						}
					}
					TGOut.writeBytes(" );\n");
				} else if (dom instanceof RecordDomain) {
					RecordDomain rd = (RecordDomain) dom;
					TGOut.writeBytes("RecordDomain");
					space();
					writeIdentifier((rd).getSimpleName());
					String delim = " ( ";
					for (Map.Entry<String, Domain> rdc : (rd).getComponents()
							.entrySet()) {
						TGOut.writeBytes(delim);
						noSpace();
						writeIdentifier(rdc.getKey());
						TGOut.writeBytes(": ");
						TGOut.writeBytes(rdc.getValue().getTGTypeName(pkg));
						delim = ", ";
					}
					TGOut.writeBytes(" );\n");
				}
			}

			// write vertex classes
			for (VertexClass vc : pkg.getVertexClasses().values()) {
				if (vc.isInternal()) {
					continue;
				}
				if (vc.isAbstract()) {
					TGOut.writeBytes("abstract ");
				}
				TGOut.writeBytes("VertexClass");
				space();
				writeIdentifier(vc.getSimpleName());
				writeHierarchy(pkg, vc);
				writeAttributes(pkg, vc);
				TGOut.writeBytes(";\n");
			}

			// write edge classes
			for (EdgeClass ec : pkg.getEdgeClasses().values()) {
				if (ec.isInternal()) {
					continue;
				}
				if (ec.isAbstract()) {
					TGOut.writeBytes("abstract ");
				}
				if (ec instanceof CompositionClass) {
					TGOut.writeBytes("CompositionClass");
				} else if (ec instanceof AggregationClass) {
					TGOut.writeBytes("AggregationClass");
				} else {
					TGOut.writeBytes("EdgeClass");
				}
				space();
				writeIdentifier(ec.getSimpleName());
				writeHierarchy(pkg, ec);

				// from (min,max) rolename
				TGOut.writeBytes(" from");
				space();
				writeIdentifier(ec.getFrom().getQualifiedName(pkg));
				TGOut.writeBytes(" (");
				TGOut.writeBytes(ec.getFromMin() + ",");
				if (ec.getFromMax() == Integer.MAX_VALUE) {
					TGOut.writeBytes("*)");
				} else {
					TGOut.writeBytes(ec.getFromMax() + ")");
				}

				if (!ec.getFromRolename().equals("")) {
					TGOut.writeBytes(" role");
					space();
					writeIdentifier(ec.getFromRolename());
					String delim = " redefines";
					for (String redefinedRolename : ec.getRedefinedFromRoles()) {
						TGOut.writeBytes(delim);
						delim = ",";
						space();
						writeIdentifier(redefinedRolename);
					}
				}

				// to (min,max) rolename
				TGOut.writeBytes(" to");
				space();
				writeIdentifier(ec.getTo().getQualifiedName(pkg));
				TGOut.writeBytes(" (");
				TGOut.writeBytes(ec.getToMin() + ",");
				if (ec.getToMax() == Integer.MAX_VALUE) {
					TGOut.writeBytes("*)");
				} else {
					TGOut.writeBytes(ec.getToMax() + ")");
				}
				if (!ec.getToRolename().equals("")) {
					TGOut.writeBytes(" role");
					space();
					writeIdentifier(ec.getToRolename());
					String delim = " redefines";
					for (String redefinedRolename : ec.getRedefinedToRoles()) {
						TGOut.writeBytes(delim);
						delim = ",";
						space();
						writeIdentifier(redefinedRolename);
					}
				}

				if (ec instanceof AggregationClass) {
					TGOut.writeBytes(" aggregate ");
					if (((AggregationClass) ec).isAggregateFrom()) {
						TGOut.writeBytes("from");
					} else {
						TGOut.writeBytes("to");
					}
				}

				writeAttributes(pkg, ec);
				TGOut.writeBytes(";\n");
			}
		}
	}

	public static void saveGraphToFile(String filename, Graph graph,
			ProgressFunction pf) throws GraphIOException {
		try {
			GraphIO io = new GraphIO();
			DataOutputStream out = new DataOutputStream(
					new BufferedOutputStream(new FileOutputStream(filename)));
			io.TGOut = out;
			io.saveGraph(graph, pf);
			out.flush();
			out.close();
		} catch (Exception e) {
			throw new GraphIOException("exception while saving graph to '"
					+ filename + "'", e);
		}
	}

	public static void saveGraphToStream(DataOutputStream out, Graph graph,
			ProgressFunction pf) throws GraphIOException {
		try {
			GraphIO io = new GraphIO();
			io.TGOut = out;
			io.saveGraph(graph, pf);
			out.flush();
		} catch (Exception e) {
			throw new GraphIOException("exception while saving graph", e);
		}
	}

	private void saveGraph(Graph graph, ProgressFunction pf)
			throws IOException, GraphIOException {
		// Write the jgralab version and license in a comment
		TGOut.writeBytes("/*");
		TGOut.writeBytes(JGraLab.getInfo());
		TGOut.writeBytes("*/\n");

		schema = graph.getSchema();
		saveSchema(schema);

		long eId;
		long vId;

		// progress bar for graph
		long graphElements = 0, currentCount = 0, interval = 1;
		if (pf != null) {
			pf.init(graph.getVCount() + graph.getECount());
			interval = pf.getUpdateInterval();
		}

		space();
		TGOut.writeBytes("\nGraph "
				+ toUTF(graph.getId() + "_" + graph.getGraphVersion()));
		writeIdentifier(graph.getAttributedElementClass().getQualifiedName());
		TGOut.writeBytes(" (" + graph.getMaxVCount() + " "
				+ graph.getMaxECount() + " " + graph.getVCount() + " "
				+ graph.getECount() + ")");
		space();
		graph.writeAttributeValues(this);
		TGOut.writeBytes(";\n");

		Package oldPackage = null;
		// write vertices
		Vertex nextV = graph.getFirstVertex();
		while (nextV != null) {
			vId = nextV.getId();
			AttributedElementClass aec = nextV.getAttributedElementClass();
			Package currentPackage = aec.getPackage();
			if (currentPackage != oldPackage) {
				TGOut.writeBytes("Package");
				space();
				writeIdentifier(currentPackage.getQualifiedName());
				TGOut.writeBytes(";\n");
				oldPackage = currentPackage;
			}
			TGOut.writeBytes(Long.toString(vId));
			space();
			writeIdentifier(aec.getSimpleName());
			// write incident edges
			Edge nextI = nextV.getFirstEdge();
			TGOut.writeBytes(" <");
			noSpace();
			while (nextI != null) {
				writeLong(nextI.getId());
				nextI = nextI.getNextEdge();
			}
			TGOut.writeBytes(">");
			space();
			nextV.writeAttributeValues(this);
			TGOut.writeBytes(";\n");
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

		// write edges
		Edge nextE = graph.getFirstEdgeInGraph();
		while (nextE != null) {
			eId = nextE.getId();
			logger.finer("Writing edge: " + nextE.getId());
			AttributedElementClass aec = nextE.getAttributedElementClass();
			Package currentPackage = aec.getPackage();
			if (currentPackage != oldPackage) {
				TGOut.writeBytes("Package");
				space();
				writeIdentifier(currentPackage.getQualifiedName());
				TGOut.writeBytes(";\n");
				oldPackage = currentPackage;
			}
			TGOut.writeBytes(Long.toString(eId));
			space();
			writeIdentifier(aec.getSimpleName());
			space();
			nextE.writeAttributeValues(this);
			TGOut.writeBytes(";\n");
			nextE = nextE.getNextEdgeInGraph();

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

	private void writeHierarchy(Package pkg, AttributedElementClass aec)
			throws IOException {
		String delim = ":";
		for (AttributedElementClass baseClass : aec.getDirectSuperClasses()) {
			if ((baseClass != this) && (!baseClass.isInternal())) {
				TGOut.writeBytes(delim);
				space();
				writeIdentifier(baseClass.getQualifiedName(pkg));
				delim = ",";
			}
		}
	}

	private void writeAttributes(Package pkg, AttributedElementClass aec)
			throws IOException {
		if (aec.hasOwnAttributes()) {
			TGOut.writeBytes(" {");
		}
		for (Iterator<Attribute> ait = aec.getOwnAttributeList().iterator(); ait
				.hasNext();) {
			Attribute a = ait.next();
			space();
			writeIdentifier(a.getName());
			TGOut.writeBytes(": ");
			String domain = a.getDomain().getTGTypeName(pkg);
			TGOut.writeBytes(domain);
			if (ait.hasNext()) {
				TGOut.writeBytes(", ");
			} else {
				TGOut.writeBytes(" }");
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
		TGOut.writeBytes(b ? "t" : "f");
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
		if (s == null) {
			TGOut.writeBytes("\\null");
		} else {
			TGOut.writeBytes(toUTF(s));
		}
	}

	public final void writeIdentifier(String s) throws IOException {
		writeSpace();
		if (Schema.reservedTGWords.contains(s)) {
			TGOut.writeBytes("'");
		}
		TGOut.writeBytes(s);
	}

	public static Schema loadSchemaFromFile(String filename)
			throws GraphIOException {
		try {
			return loadSchemaFromStream(new BufferedInputStream(
					new FileInputStream(filename), 10000));
		} catch (FileNotFoundException ex) {
			throw new GraphIOException("Unable to load schema from file "
					+ filename + ", the file cannot be found", ex);
		}
	}

	public static Schema loadSchemaFromURL(String url) throws GraphIOException {
		try {
			return loadSchemaFromStream(new URL(url).openStream());
		} catch (IOException ex) {
			throw new GraphIOException("Unable to load graph from url " + url
					+ ", the resource cannot be found", ex);
		}
	}

	public static Schema loadSchemaFromStream(InputStream in)
			throws GraphIOException {
		try {
			GraphIO io = new GraphIO();
			io.TGIn = in;
			io.tgfile();
			return io.schema;
		} catch (GraphIOException e) {
			throw e;
		} catch (Exception e) {
			throw new GraphIOException("exception while loading schema", e);
		}
	}

	public static Graph loadSchemaAndGraphFromFile(String filename,
			ProgressFunction pf) throws GraphIOException {
		try {
			logger.finer("Loading graph " + filename);
			return loadGraphFromFile(filename, null, pf);
		} catch (GraphIOException ex) {
			if (ex.getReason() == GraphIOExceptionReason.UNKNOWN_SCHEMA) {
				logger.fine("Schema was unknown, so loading that first.");
				Schema s = loadSchemaFromFile(filename);
				s.compile();
				return loadGraphFromFile(filename, s, pf);
			}
			throw ex;
		}
	}

	public static Graph loadGraphFromFile(String filename, ProgressFunction pf)
			throws GraphIOException {
		return loadGraphFromFile(filename, null, pf);
	}

	public static Graph loadGraphFromFile(String filename, Schema schema,
			ProgressFunction pf) throws GraphIOException {
		try {
			logger.finer("Loading graph " + filename);
			return loadGraphFromStream(new BufferedInputStream(
					new FileInputStream(filename), 10000), schema, pf);
		} catch (IOException ex) {
			throw new GraphIOException("Unable to load graph from file "
					+ filename + ", the file cannot be found", ex);
		}
	}

	public static Graph loadGraphFromURL(String url, ProgressFunction pf)
			throws GraphIOException {
		return loadGraphFromURL(url, null, pf);
	}

	public static Graph loadGraphFromURL(String url, Schema schema,
			ProgressFunction pf) throws GraphIOException {
		try {
			return loadGraphFromStream(new URL(url).openStream(), schema, pf);
		} catch (IOException ex) {
			throw new GraphIOException("Unable to load graph from url " + url
					+ ", the resource cannot be found", ex);
		}
	}

	public static Graph loadGraphFromStream(InputStream in, ProgressFunction pf)
			throws GraphIOException {
		return loadGraphFromStream(in, null, pf);
	}

	public static Graph loadGraphFromStream(InputStream in, Schema schema,
			ProgressFunction pf) throws GraphIOException {
		try {
			GraphIO io = new GraphIO();
			io.schema = schema;
			io.TGIn = in;
			io.tgfile();
			String schemaName = io.schema.getQualifiedName();
			Class<?> schemaClass = Class.forName(schemaName, true,
					M1ClassManager.instance());
			Method instanceMethod = schemaClass.getMethod("instance",
					(Class<?>[]) null);
			io.schema = (Schema) instanceMethod.invoke(null, new Object[0]);
			GraphImpl g = io.graph(pf);
			g.internalLoadingCompleted(io.firstIncidence, io.nextIncidence);
			io.firstIncidence = null;
			io.nextIncidence = null;
			g.loadingCompleted();
			return g;
		} catch (GraphIOException e) {
			throw e;
		} catch (ClassNotFoundException e) {
			// the class was not found, so the schema.commit-method was not
			// called yet, an exception will be thrown
			throw new GraphIOException(
					"Unable to load a graph which belongs to the schema because the Java-classes for this schema have not yet been created. Use Schema.commit(..) to create them!",
					e, GraphIOExceptionReason.UNKNOWN_SCHEMA);
		} catch (Exception e) {
			throw new GraphIOException("exception while loading graph", e);
		}
	}

	private void tgfile() throws GraphIOException, SchemaException, IOException {
		line = 1;
		la = read();
		match();
		schema();
		if (lookAhead.equals("") || lookAhead.equals("Graph")) {
			return;
		}
		throw new GraphIOException("symbol '" + lookAhead
				+ "' not recognized in line " + line, null);
	}

	/**
	 * Reads a Schema together with its Domains, GraphClasses and
	 * GraphElementClasses from a TG-file. Subsequently, the Schema is created.
	 *
	 * @throws GraphIOException
	 */
	private void schema() throws GraphIOException, SchemaException {
		if (schema != null) {
			// We already have a schema, so we don't want to load the schema
			// from the file
			while (!lookAhead.equals("Graph")) {
				match();
			}
			return;
		}

		currentPackageName = "";
		match("Schema");
		QualifiedName qn = matchQualifiedName(true);
		if (qn.getPackageName().equals("")) {
			throw new GraphIOException("invalid schema name '" + lookAhead
					+ "', package prefix must not be empty in line " + line);
		}
		match(";");

		schema = new SchemaImpl(qn);

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

	}

	/**
	 * Creates the Domains contained in a Schema.
	 *
	 * @return A Map of the Domain names to the concrete Domain objects.
	 * @throws GraphIOException
	 */
	private Map<QualifiedName, Domain> domDef() throws GraphIOException,
			SchemaException {
		domains.put(schema.getDomain("Boolean").getQName(), schema
				.getDomain("Boolean"));
		domains.put(schema.getDomain("Integer").getQName(), schema
				.getDomain("Integer"));
		domains.put(schema.getDomain("Long").getQName(), schema
				.getDomain("Long"));
		domains.put(schema.getDomain("Double").getQName(), schema
				.getDomain("Double"));
		domains.put(schema.getDomain("String").getQName(), schema
				.getDomain("String"));
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
		enumDomainBuffer.add(new EnumDomainData(matchQualifiedName(true),
				parseEnumConstants()));
		match(";");
	}

	/**
	 * Creates all EnumDomains whose data is stored in {@link enumDomainBuffer}
	 */
	private void enumDomains() {
		Domain domain;

		for (EnumDomainData enumDomainData : enumDomainBuffer) {
			domain = schema.createEnumDomain(enumDomainData.name,
					enumDomainData.enumConstants);
			domains.put(enumDomainData.name, domain);
		}
	}

	/**
	 * Read a RecordDomain, i.e. its name along with the components.
	 *
	 * @throws GraphIOException
	 */
	private void parseRecordDomain() throws GraphIOException {
		match("RecordDomain");
		recordDomainBuffer.add(new RecordDomainData(matchQualifiedName(true),
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
			domain = schema.createRecordDomain(recordDomainData.name,
					getComponents(recordDomainData.components));
			domains.put(recordDomainData.name, domain);
		}
	}

	/**
	 * Takes a Map of record component names to lists of Strings as parameter.
	 * Each list represents a component's domain. This Map is converted to a Map
	 * of the component names to Domain objects corresponding to the domains
	 * represented in the lists.
	 *
	 * @param componentsData
	 *            A Map of record component names to lists of Strings. Each list
	 *            represents a component's domain.
	 * @return A Map of record component names to corresponding Domain objects.
	 * @throws GraphIOException
	 */
	private Map<String, Domain> getComponents(
			Map<String, List<QualifiedName>> componentsData)
			throws GraphIOException {
		Map<String, Domain> componentDomains = new TreeMap<String, Domain>();
		Domain domain;

		for (Entry<String, List<QualifiedName>> componentData : componentsData
				.entrySet()) {
			domain = attrDomain(componentData.getValue());
			componentDomains.put(componentData.getKey(), domain);
		}

		return componentDomains;
	}

	/**
	 * Reads Schema's Domains and GraphClasses with contained
	 * GraphElementClasses from TG-file.
	 *
	 * @throws GraphIOException
	 */
	private void parseSchema() throws GraphIOException, SchemaException {
		QualifiedName currentGraphClassName = parseGraphClass();

		while (lookAhead.equals("Package") || lookAhead.equals("RecordDomain")
				|| lookAhead.equals("EnumDomain")
				|| lookAhead.equals("abstract")
				|| lookAhead.equals("VertexClass")
				|| lookAhead.equals("EdgeClass")
				|| lookAhead.equals("AggregationClass")
				|| lookAhead.equals("CompositionClass")) {
			if (lookAhead.equals("Package")) {
				parsePackage();
			} else if (lookAhead.equals("RecordDomain")) {
				parseRecordDomain();
			} else if (lookAhead.equals("EnumDomain")) {
				parseEnumDomain();
			} else {
				parseGraphElementClass(currentGraphClassName);
			}
		}
	}

	private void parsePackage() throws GraphIOException {
		match("Package");
		currentPackageName = "";
		if (lookAhead.equals(";")) {
			currentPackageName = "";
		} else {
			QualifiedName qn = matchQualifiedName(false);
			if (!isValidPackageName(qn.getSimpleName())) {
				throw new GraphIOException("invalid package name '"
						+ qn.getQualifiedName() + "' in line " + line);
			}
			currentPackageName = qn.getQualifiedName();
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
	private QualifiedName parseGraphClass() throws GraphIOException,
			SchemaException {
		match("GraphClass");
		graphClass = new GraphClassData();

		graphClass.name = new QualifiedName(matchSimpleName(true));
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

		for (Attribute attr : attributes(gcData.attributes).values()) {
			gc.addAttribute(attr);
		}

		for (String constraint : gcData.constraints) {
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
	private List<QualifiedName> parseHierarchy() throws GraphIOException {
		List<QualifiedName> hierarchy = new LinkedList<QualifiedName>();
		match(":");
		hierarchy.add(matchQualifiedName(true));
		while (lookAhead.equals(",")) {
			match();
			hierarchy.add(matchQualifiedName(true));
		}
		return hierarchy;
	}

	/**
	 * Reads the attributes (names and domains) of a GraphClass or a
	 * GraphElementClass from the TG-file.
	 *
	 * @return A Map of attribute names to lists of Strings. Each list
	 *         represents an attribute's domain.
	 * @throws GraphIOException
	 */
	private Map<String, List<QualifiedName>> parseAttributes()
			throws GraphIOException {
		Map<String, List<QualifiedName>> attributesData = new TreeMap<String, List<QualifiedName>>();
		LinkedList<QualifiedName> attributeDomain = new LinkedList<QualifiedName>();
		String currentAttributeName;

		match("{");
		currentAttributeName = matchSimpleName(false);
		match(":");
		parseAttrDomain(attributeDomain);
		attributesData.put(currentAttributeName, attributeDomain);
		while (lookAhead.equals(",")) {
			attributeDomain = new LinkedList<QualifiedName>();
			match();
			currentAttributeName = matchSimpleName(false);
			if (attributesData.containsKey(currentAttributeName)) {
				throw new GraphIOException("duplicate attribute name '"
						+ currentAttributeName + "' in line " + line);
			}
			match(":");
			parseAttrDomain(attributeDomain);
			attributesData.put(currentAttributeName, attributeDomain);
		}
		match("}");
		return attributesData;
	}

	/**
	 * Takes a Map of attribute names to lists of Strings as parameter. Each
	 * list represents an attribute's domain. This Map is converted to a Map of
	 * the attribute names to Domain objects corresponding to the domains
	 * represented in the lists.
	 *
	 * @param componentsData
	 *            A Map of attribute names to lists of Strings. Each list
	 *            represents an attribute's domain.
	 * @return A Map of attribute names to corresponding Domain objects.
	 * @throws GraphIOException
	 */
	private Map<String, Attribute> attributes(
			Map<String, List<QualifiedName>> attributesData)
			throws GraphIOException {
		Map<String, Attribute> attributes = new TreeMap<String, Attribute>();
		Attribute attribute;

		for (Entry<String, List<QualifiedName>> attributeData : attributesData
				.entrySet()) {
			Domain domain = attrDomain(attributeData.getValue());
			attribute = new AttributeImpl(attributeData.getKey(), domain);
			attributes.put(attribute.getName(), attribute);
		}

		return attributes;
	}

	/**
	 * Reads an Attribute's domain from the TG-file and stores it in the list
	 * given as argument.
	 *
	 * @param attrDomain
	 *            The list to which an attribute's domain shall be added.
	 * @throws GraphIOException
	 */
	private void parseAttrDomain(List<QualifiedName> attrDomain)
			throws GraphIOException {
		if (lookAhead.equals("List")) {
			match();
			match("<");
			attrDomain.add(new QualifiedName("List<"));
			parseAttrDomain(attrDomain);
			match(">");
		} else if (lookAhead.equals("Set")) {
			match();
			match("<");
			attrDomain.add(new QualifiedName("Set<"));
			parseAttrDomain(attrDomain);
			match(">");
		} else if (lookAhead.equals("Map")) {
			match();
			match("<");
			attrDomain.add(new QualifiedName("Map<"));
			parseAttrDomain(attrDomain);
			match(",");
			parseAttrDomain(attrDomain);
			match(">");
		} else {
			if (isBasicDomainName(lookAhead)) {
				attrDomain.add(new QualifiedName(lookAhead));
				match();
			} else {
				QualifiedName qn = matchQualifiedName(true);
				attrDomain.add(qn);
			}
		}
	}

	private boolean isBasicDomainName(String s) {
		return Schema.basicDomains.contains(s);
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
	private Domain attrDomain(List<QualifiedName> domainNames)
			throws GraphIOException {
		Iterator<QualifiedName> it = domainNames.iterator();
		QualifiedName domainName;
		while (it.hasNext()) {
			domainName = it.next();
			if (domainName.getQualifiedName().equals("List<")) {
				try {
					it.remove();
					return schema.createListDomain(attrDomain(domainNames));
				} catch (SchemaException e) {
					throw new GraphIOException(
							"can't create list domain in line " + line, e);
				}
			} else if (domainName.getQualifiedName().equals("Set<")) {
				try {
					it.remove();
					return schema.createSetDomain(attrDomain(domainNames));
				} catch (SchemaException e) {
					throw new GraphIOException(
							"can't create set domain in line " + line, e);
				}
			} else if (domainName.getQualifiedName().equals("Map<")) {
				try {
					it.remove();
					Domain keyDomain = schema.getDomain(it.next());
					it.remove();
					if (keyDomain == null) {
						throw new GraphIOException(
								"can't create map domain, because no key domain was given in line "
										+ line);
					}
					if (!(keyDomain instanceof BasicDomain)
							&& !(keyDomain instanceof EnumDomain)) {
						throw new GraphIOException(
								"can't create map domain. The key domain must be a basic or enum domain. Line "
										+ line);
					}
					Domain result = schema.createMapDomain(keyDomain,
							attrDomain(domainNames));
					// System.out.println("result = " + result);
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
		if (schema.isValidEnumConstant(lookAhead) || lookAhead.equals("\\null")) {
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
	private void parseGraphElementClass(QualifiedName gcName)
			throws GraphIOException, SchemaException {
		GraphElementClassData graphElementClassData;
		String type;

		graphElementClassData = new GraphElementClassData();

		if (lookAhead.equals("abstract")) {
			match();
			graphElementClassData.isAbstract = true;
		}

		if (lookAhead.equals("VertexClass")) {
			match("VertexClass");
			graphElementClassData.type = "VertexClass";

			graphElementClassData.name = matchQualifiedName(true);
			if (lookAhead.equals(":")) {
				graphElementClassData.directSuperClasses = parseHierarchy();
			}
			vertexClassBuffer.get(gcName).add(graphElementClassData);
		} else if (lookAhead.equals("EdgeClass")
				|| lookAhead.equals("AggregationClass")
				|| lookAhead.equals("CompositionClass")) {
			type = lookAhead;
			match(type);
			graphElementClassData.type = type;

			graphElementClassData.name = matchQualifiedName(true);
			if (lookAhead.equals(":")) {
				graphElementClassData.directSuperClasses = parseHierarchy();
			}
			match("from");
			graphElementClassData.fromVertexClassName = matchQualifiedName(true);
			graphElementClassData.fromMultiplicity = parseMultiplicity();
			graphElementClassData.fromRoleName = parseRoleName();
			graphElementClassData.redefinedFromRoles = parseRolenameRedefinitions();
			match("to");

			graphElementClassData.toVertexClassName = matchQualifiedName(true);
			graphElementClassData.toMultiplicity = parseMultiplicity();
			graphElementClassData.toRoleName = parseRoleName();
			graphElementClassData.redefinedToRoles = parseRolenameRedefinitions();
			if (graphElementClassData.type.equals("AggregationClass")
					|| graphElementClassData.type.equals("CompositionClass")) {
				graphElementClassData.aggregateFrom = parseAggregate();
			}
			edgeClassBuffer.get(gcName).add(graphElementClassData);
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

	private Set<String> parseConstraints() throws GraphIOException {
		// constraints have the form: ["greql exp", "greql exp", ...]
		match("[");
		HashSet<String> constraints = new HashSet<String>(1);
		String constraint = matchUtfString();
		constraints.add(constraint);
		while (lookAhead.equals(",")) {
			match();
			constraint = matchUtfString();
			constraints.add(constraint);
		}
		match("]");
		return constraints;
	}

	private VertexClass createVertexClass(GraphElementClassData vcd,
			GraphClass gc) throws GraphIOException, SchemaException {
		VertexClass vc = gc.createVertexClass(vcd.name);
		vc.setAbstract(vcd.isAbstract);

		for (Attribute attr : attributes(vcd.attributes).values()) {
			vc.addAttribute(attr);
		}

		for (String constraint : vcd.constraints) {
			vc.addConstraint(constraint);
		}

		GECsearch.put(vc, gc);
		return vc;
	}

	private EdgeClass createEdgeClass(GraphElementClassData ecd, GraphClass gc)
			throws GraphIOException, SchemaException {
		EdgeClass ec;
		if (ecd.type.equals("EdgeClass")) {
			ec = gc.createEdgeClass(ecd.name, gc
					.getVertexClass(ecd.fromVertexClassName),
					ecd.fromMultiplicity[0], ecd.fromMultiplicity[1],
					ecd.fromRoleName, gc.getVertexClass(ecd.toVertexClassName),
					ecd.toMultiplicity[0], ecd.toMultiplicity[1],
					ecd.toRoleName);
		} else if (ecd.type.equals("AggregationClass")) {
			ec = gc.createAggregationClass(ecd.name, gc
					.getVertexClass(ecd.fromVertexClassName),
					ecd.fromMultiplicity[0], ecd.fromMultiplicity[1],
					ecd.fromRoleName, ecd.aggregateFrom, gc
							.getVertexClass(ecd.toVertexClassName),
					ecd.toMultiplicity[0], ecd.toMultiplicity[1],
					ecd.toRoleName);
		} else if (ecd.type.equals("CompositionClass")) {
			ec = gc.createCompositionClass(ecd.name, gc
					.getVertexClass(ecd.fromVertexClassName),
					ecd.fromMultiplicity[0], ecd.fromMultiplicity[1],
					ecd.fromRoleName, ecd.aggregateFrom, gc
							.getVertexClass(ecd.toVertexClassName),
					ecd.toMultiplicity[0], ecd.toMultiplicity[1],
					ecd.toRoleName);
		} else {
			throw new InvalidNameException("Unknown type " + ecd.type);
		}

		for (Attribute attr : attributes(ecd.attributes).values()) {
			ec.addAttribute(attr);
		}

		for (String constraint : ecd.constraints) {
			ec.addConstraint(constraint);
		}

		ec.setAbstract(ecd.isAbstract);
		ec.redefineFromRole(ecd.redefinedFromRoles);
		ec.redefineToRole(ecd.redefinedToRoles);

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

	/**
	 * Reads whether an AggregationClass or a CompositionClass has its aggregate
	 * at its "from" VertexClass or at its "to" VertexClass.
	 *
	 * @return True, if the aggregate is at the "from" end. False, if the
	 *         aggregate is at the "to" end.
	 * @throws GraphIOException
	 */
	private boolean parseAggregate() throws GraphIOException {
		match("aggregate");
		if (lookAhead.equals("from")) {
			match();
			return true;
		} else if (lookAhead.equals("to")) {
			match();
			return false;
		} else {
			throw new GraphIOException(
					"invalid aggregate (from/to allowed), but found '"
							+ lookAhead + "' in line " + line);
		}
	}

	private boolean isValidPackageName(String s) {
		if (s == null || s.length() == 0) {
			return false;
		}
		char[] chars = s.toCharArray();
		if (!Character.isLetter(chars[0]) || !Character.isLowerCase(chars[0])
				|| chars[0] > 127) {
			return false;
		}
		for (int i = 1; i < chars.length; i++) {
			if (!(Character.isLowerCase(chars[i])
					|| Character.isDigit(chars[i]) || chars[i] == '_')
					|| chars[i] > 127) {
				return false;
			}
		}
		return true;
	}

	/**
	 * Reads the components of a RecordDomain from the TG-file.
	 *
	 * @return A map of component names to lists of Strings representing the
	 *         component's domain.
	 * @throws GraphIOException
	 */
	private Map<String, List<QualifiedName>> parseRecordComponents()
			throws GraphIOException {
		Map<String, List<QualifiedName>> componentsData = new TreeMap<String, List<QualifiedName>>();
		List<QualifiedName> recordComponentDomain = new LinkedList<QualifiedName>();
		String recordComponentName;

		match("(");
		recordComponentName = matchSimpleName(false);
		match(":");
		parseAttrDomain(recordComponentDomain);
		componentsData.put(recordComponentName, recordComponentDomain);
		while (lookAhead.equals(",")) {
			recordComponentDomain = new LinkedList<QualifiedName>();
			match();
			recordComponentName = matchSimpleName(false);
			match(":");
			parseAttrDomain(recordComponentDomain);
			componentsData.put(recordComponentName, recordComponentDomain);
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

		for (Entry<QualifiedName, List<GraphElementClassData>> gcElements : vertexClassBuffer
				.entrySet()) {
			for (GraphElementClassData vData : gcElements.getValue()) {
				aec = schema.getAttributedElementClass(vData.name);
				if (aec == null) {
					throw new GraphIOException(
							"undefined AttributedElementClass '"
									+ vData.name.getQualifiedName() + "'");
				}
				if (aec instanceof VertexClass) {
					for (QualifiedName superClassName : vData.directSuperClasses) {
						superClass = (VertexClass) (GECsearch.get(aec)
								.getGraphElementClass(superClassName));
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

		for (Entry<QualifiedName, List<GraphElementClassData>> gcElements : edgeClassBuffer
				.entrySet()) {
			for (GraphElementClassData eData : gcElements.getValue()) {
				aec = schema.getAttributedElementClass(eData.name);
				if (aec == null) {
					throw new GraphIOException(
							"undefined AttributedElementClass '"
									+ eData.name.getQualifiedName() + "'");
				}
				if (aec instanceof EdgeClass) {
					for (QualifiedName superClassName : eData.directSuperClasses) {
						superClass = (EdgeClass) (GECsearch.get(aec)
								.getGraphElementClass(superClassName));
						if (superClass == null) {
							throw new GraphIOException("undefined EdgeClass '"
									+ superClassName + "'");
						}
						((EdgeClass) aec).addSuperClass(superClass);
					}
				}
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
		LOOP: while (la != -1 && la != '"') {
			if (la < 32 || la > 127) {
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
					+ startLine);
		}
		la = read();
	}

	private final static boolean isWs(int c) {
		return c == ' ' || c == '\n' || c == '\t' || c == '\r';
	}

	private final static boolean isSeparator(int c) {
		return c == ';' || c == '<' || c == '>' || c == '(' || c == ')'
				|| c == '{' || c == '}' || c == ':' || c == '[' || c == ']'
				|| c == ',';
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
				if (la >= 0 && la == '/') {
					// single line comment, skip to the end of the current line
					logger.finer("Comment detected in line " + line);
					while (la >= 0 && la != '\n') {
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

	private final void match() throws GraphIOException {
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
		String s = (lookAhead.charAt(0) == '\'') ? lookAhead.substring(1)
				: lookAhead;
		boolean ok = isValidIdentifier(s)
				&& ((isUpperCase && Character.isUpperCase(s.charAt(0))) || (!isUpperCase && Character
						.isLowerCase(s.charAt(0))));

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
	 * @return the parsed identifier
	 * @throws GraphIOException
	 */
	public final QualifiedName matchQualifiedName(boolean isUpperCase)
			throws GraphIOException {

		String s = (lookAhead.charAt(0) == '\'') ? lookAhead.substring(1)
				: lookAhead;
		String c = (s.indexOf('.') >= 0) ? s : currentPackageName + "." + s;
		QualifiedName result = qualifiedNameMap.get(c);
		if (result != null) {
			match();
			return result;
		}
		boolean ok = true;
		result = new QualifiedName(s);
		if (result.getPackageName().length() == 0 && s.charAt(0) != '.') {
			// no need to check, because currentPackageName is already checked
			// by parsePackage();
			result.setPackageName(currentPackageName);
		} else {
			if (result.getPackageName().length() > 0) {
				String[] parts = result.getPackageName().split("\\.");
				ok = (parts.length == 1 && parts[0].length() == 0)
						|| isValidPackageName(parts[0]);
				for (int i = 1; i < parts.length && ok; i++) {
					ok = ok && isValidPackageName(parts[i]);
				}
			}
		}

		ok = ok
				&& isValidIdentifier(result.getSimpleName())
				&& ((isUpperCase && Character.isUpperCase(result
						.getSimpleName().charAt(0))) || (!isUpperCase && Character
						.isLowerCase(result.getSimpleName().charAt(0))));

		if (!ok) {
			throw new GraphIOException("invalid qualified name '" + lookAhead
					+ "' in line " + line);
		}
		match();
		qualifiedNameMap.put(result.getQualifiedName(), result);
		return result;
	}

	public final String matchUtfString() throws GraphIOException {
		if (lookAhead.equals("\\null")) {
			match();
			return null;
		}
		if (isUtfString) {
			String result = lookAhead;
			match();
			return result;
		}
		throw new GraphIOException("expected a string constant but found '"
				+ lookAhead + "' in line " + line);
	}

	public final boolean matchBoolean() throws GraphIOException {
		if (!lookAhead.equals("t") && !lookAhead.equals("f")) {
			throw new GraphIOException(
					"expected a boolean constant but found '" + lookAhead
							+ "' in line " + line);
		}
		boolean result = lookAhead.equals("t");
		match();
		return result;
	}

	private GraphImpl graph(ProgressFunction pf) throws GraphIOException {
		currentPackageName = "";
		match("Graph");
		String graphIdVersion = matchUtfString();
		String graphId;
		long graphVersion;

		try {
			graphId = graphIdVersion.substring(0, graphIdVersion
					.lastIndexOf('_'));
		} catch (IndexOutOfBoundsException e) {
			graphId = graphIdVersion;
		}

		try {
			graphVersion = Long.parseLong(graphIdVersion
					.substring(graphIdVersion.lastIndexOf('_') + 1));
		} catch (IndexOutOfBoundsException e1) {
			graphVersion = 0;
		} catch (NumberFormatException e2) {
			graphVersion = 0;
		}

		gcName = new QualifiedName(matchAndNext());
		assert gcName.isSimple() && isValidIdentifier(gcName.getSimpleName()) : "illegal characters in graph class '"
				+ gcName + "'";
		// check if classname is known in the schema
		if (schema.getGraphClass(gcName) == null) {
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
		GraphImpl graph = null;
		try {
			graph = (GraphImpl) schema.getGraphCreateMethod(gcName).invoke(
					null, new Object[] { graphId, maxV, maxE });
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
			if (lookAhead.equals("Package")) {
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

	private void vertexDesc(Graph graph) throws GraphIOException {
		int vId = vId();
		QualifiedName vcName = className();
		Vertex vertex;
		Method createMethod;
		createMethod = createMethods.get(vcName);
		try {
			if (createMethod == null) {
				createMethod = schema.getVertexCreateMethod(vcName, gcName);
				createMethods.put(vcName, createMethod);
			}
			vertexDescTempObject[0] = vId;
			vertex = (Vertex) createMethod.invoke(graph, vertexDescTempObject);
			// vertex = (Vertex) createMethod.invoke(graph, new Object[] { vId
			// });
		} catch (Exception e) {
			e.printStackTrace();
			throw new GraphIOException("cant't create vertex '" + vId + "'", e);
		}
		parseIncidentEdges(vertex);
		vertex.readAttributeValues(this);
		match(";");
	}

	private void edgeDesc(Graph graph) throws GraphIOException {
		int eId = eId();
		QualifiedName ecName = graph.getGraphClass().getEdgeClass(className())
				.getQName();
		Edge edge;
		Method createMethod;
		createMethod = createMethods.get(ecName);
		try {
			if (createMethod == null) {
				logger.finer("Searching create method for edge " + ecName);
				createMethod = schema.getEdgeCreateMethod(ecName, gcName);
				createMethods.put(ecName, createMethod);
			}
			edgeDescTempObject[0] = eId;
			edgeDescTempObject[1] = edgeOut[eId];
			edgeDescTempObject[2] = edgeIn[eId];
			edge = (Edge) createMethod.invoke(graph, edgeDescTempObject);
		} catch (Exception e) {
			throw new GraphIOException("can't create edge '" + eId + "' from "
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

	private QualifiedName className() throws GraphIOException {
		QualifiedName className = matchQualifiedName(true);
		// The following time-consuming test is performed in the invocation and
		// thus not longer needed here
		// if (!schema.knows(className))
		// throw new GraphIOException("Class " + className
		// + " of read element does not exist.");
		return className;
	}

	private int vId() throws GraphIOException {
		int vId = matchInteger();
		if (vId <= 0) {
			throw new GraphIOException("Invalid vertex id " + vId + ".");
		}
		return vId;
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

	private static String toUTF(String value) {
		if (value == null) {
			return "";
		}
		String out = "\""; // "
		CharBuffer cb = CharBuffer.wrap(value);
		char c;
		while (cb.hasRemaining()) {
			c = cb.get();
			switch (c) {
			case '"':
				out += "\\\"";
				break;
			case '\n':
				out += "\\n";
				break;
			case '\r':
				out += "\\r";
				break;
			case '\\':
				out += "\\\\";
				break;
			case '\t':
				out += "\\t";
				break;
			default:
				if (c < 127) {
					out += c;
				} else {
					out += "\\u";
					String s = Integer.toHexString(c);
					switch (s.length()) {
					case 1:
						out += "000";
						break;
					case 2:
						out += "00";
						break;
					case 3:
						out += "0";
						break;
					}
					out += s;
				}
			}
		}
		out += "\"";
		return out;
	}

	private static boolean isValidIdentifier(String s) {
		if (s == null || s.length() == 0) {
			return false;
		}
		char[] chars = s.toCharArray();
		if (!Character.isLetter(chars[0]) || chars[0] > 127) {
			return false;
		}
		for (int i = 1; i < chars.length; i++) {
			if (!(Character.isLetter(chars[i]) || Character.isDigit(chars[i]) || chars[i] == '_')
					|| chars[i] > 127) {
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
				for (List<QualifiedName> componentDomains : rd.components
						.values()) {
					for (QualifiedName componentDomain : componentDomains) {
						if (componentDomain.getQualifiedName().equals("String")
								|| componentDomain.getQualifiedName().equals(
										"Integer")
								|| componentDomain.getQualifiedName().equals(
										"Boolean")
								// || componentDomain.getQualifiedName().equals(
								// "Object")
								|| componentDomain.getQualifiedName().equals(
										"Long")
								|| componentDomain.getQualifiedName().equals(
										"Double")
								|| componentDomain.getQualifiedName().equals(
										"Set<")
								|| componentDomain.getQualifiedName().equals(
										"List<")) {
							continue;
						}
						componentDomsInOrderedList = false;
						for (RecordDomainData orderedRd : orderedRdList) {
							if (componentDomain.equals(orderedRd.name)) {
								componentDomsInOrderedList = true;
								break;
							}
						}
						for (EnumDomainData ed : enumDomainBuffer) {
							if (componentDomain.equals(ed.name)) {
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
								if (rd2.name.equals(componentDomain)) {
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
		Set<QualifiedName> orderedVcNames = new TreeSet<QualifiedName>();
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
					orderedVcNames.add(vc.name);
					orderedVcList.add(vc);
					vcit.remove();
				} else {
					/*
					 * check if some superclasses exist among yet unsorted
					 * VertexClasses
					 */
					for (QualifiedName superClass : vc.directSuperClasses) {
						if (orderedVcNames.contains(superClass)) {
							continue;
						}
						definedVcName = false;
						for (GraphElementClassData vc2 : unorderedVcList) {
							if (vc2.name.equals(superClass)) {
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
		Set<QualifiedName> orderedEcNames = new TreeSet<QualifiedName>();
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
					orderedEcNames.add(ec.name);
					orderedEcList.add(ec);
					ecit.remove();
				} else {
					/*
					 * check if superclasses exist among yet unsorted
					 * EdgeClasses
					 */
					for (QualifiedName superClass : ec.directSuperClasses) {
						if (orderedEcNames.contains(superClass)) {
							continue;
						}
						definedEcName = false;
						for (GraphElementClassData ec2 : unorderedEcList) {
							if (ec2.name.equals(superClass)) {
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

		for (Entry<QualifiedName, List<GraphElementClassData>> graphClassEdge : edgeClassBuffer
				.entrySet()) {
			for (GraphElementClassData ec : graphClassEdge.getValue()) {
				existingFromVertexClass = false;
				existingToVertexClass = false;

				for (Entry<QualifiedName, List<GraphElementClassData>> graphClassVertex : vertexClassBuffer
						.entrySet()) {
					for (GraphElementClassData vc : graphClassVertex.getValue()) {
						if (ec.toVertexClassName.getQualifiedName().contains(
								"MappingInformation")
								&& vc.name.getQualifiedName().contains(
										"MappingInformation")) {
						}
						if (ec.fromVertexClassName.equals(vc.name)
								|| ec.fromVertexClassName.getQualifiedName()
										.equals("Vertex")) {
							existingFromVertexClass = true;
						}
						if (ec.toVertexClassName.equals(vc.name)
								|| ec.toVertexClassName.getQualifiedName()
										.equals("Vertex")) {
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
							+ ec.name + " + does not exist");
				}
				if (!existingToVertexClass) {
					throw new GraphIOException("ToVertexClass "
							+ ec.toVertexClassName + " at EdgeClass " + ec.name
							+ " does not exist");
				}
			}
		}
	}

	/**
	 * EnumDomainData contains the parsed data of an EnumDomain. This data is
	 * used to create an EnumDomain.
	 */
	private class EnumDomainData {
		QualifiedName name;

		List<String> enumConstants;

		EnumDomainData(QualifiedName name, List<String> enumConstants) {
			this.name = name;
			this.enumConstants = enumConstants;
		}
	}

	/**
	 * RecordDomainData contains the parsed data of a RecordDomain. This data is
	 * used to create a RecordDomain.
	 */
	private class RecordDomainData {
		QualifiedName name;

		Map<String, List<QualifiedName>> components;

		RecordDomainData(QualifiedName name,
				Map<String, List<QualifiedName>> components) {
			this.name = name;
			this.components = components;
		}
	}

	/**
	 * GraphClassData contains the parsed data of a GraphClass. This data is
	 * used to create a GraphClass.
	 */
	private class GraphClassData {
		Set<String> constraints = new HashSet<String>(1);

		QualifiedName name;

		boolean isAbstract = false;

		Map<String, List<QualifiedName>> attributes = new TreeMap<String, List<QualifiedName>>();
	}

	/**
	 * GraphElementClassData contains the parsed data of a GraphElementClass.
	 * This data is used to create a GraphElementClass.
	 */
	private class GraphElementClassData {
		QualifiedName name;

		String type;

		boolean isAbstract = false;

		List<QualifiedName> directSuperClasses = new LinkedList<QualifiedName>();

		QualifiedName fromVertexClassName;

		int[] fromMultiplicity = { 1, Integer.MAX_VALUE };

		String fromRoleName = "";

		Set<String> redefinedFromRoles = null;

		QualifiedName toVertexClassName;

		int[] toMultiplicity = { 1, Integer.MAX_VALUE };

		String toRoleName = "";

		Set<String> redefinedToRoles = null;

		boolean aggregateFrom;

		Map<String, List<QualifiedName>> attributes = new TreeMap<String, List<QualifiedName>>();

		Set<String> constraints = new HashSet<String>(1);
	}
}
