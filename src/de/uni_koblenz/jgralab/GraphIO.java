/*
 * JGraLab - The Java graph laboratory
 * (c) 2006-2007 Institute for Software Technology
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

import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
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
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.Map.Entry;

import de.uni_koblenz.jgralab.impl.AttributeImpl;
import de.uni_koblenz.jgralab.impl.SchemaImpl;

/**
 * class for loading and storing schema and graphs in tg format
 * 
 * @author riediger@uni-koblenz.de
 */
public class GraphIO {
	private static final boolean DEBUG = false;

	private static final boolean PARSE_DEBUG = false;

	private InputStream TGIn;

	private DataOutputStream TGOut;

	private Schema schema;

	/**
	 * Maps domain names to the respective Domains.
	 */
	private Map<String, Domain> domains;

	/**
	 * Maps GraphElementClasses to their containing GraphClasses
	 */
	private Map<GraphElementClass, GraphClass> GECsearch;

	private Map<String, Method> createMethods;

	private Vertex edgeIn[], edgeOut[];

	private int line; // line number

	private int la; // lookahead character

	private String lookAhead; // lookahead token

	private boolean isUtfString; // lookahead is UTF string

	private boolean writeSpace; // if true, a space is written in the next

	// writeXXX()

	private String gcName; // GraphClass name of the currently loaded graph

	private static HashSet<String> reservedWords = new HashSet<String>();

	private byte buffer[];

	private int bufferPos;

	private int bufferSize;

	/**
	 * indexed with incidence-id, holds the next incidence of the current vertex
	 * to represent iSeq
	 */
	private int nextEdgeAtVertex[];

	/**
	 * indexed with vertex-id, holds the first incidence-id of the vertex
	 */
	private int firstEdgeAtVertex[];

	/**
	 * indexed with vertex-id, holds the last incidence-id of the vertex
	 */
	private int lastEdgeAtVertex[];

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
	 * Buffers the parsed data of graph classes prior to their creation in
	 * JGraLab.
	 */
	private List<GraphClassData> graphClassBuffer;

	/**
	 * Buffers the parsed data of vertex classes prior to their creation in
	 * JGraLab.
	 */
	private Map<String, List<GraphElementClassData>> vertexClassBuffer;

	/**
	 * Buffers the parsed data of edge classes prior to their creation in
	 * JGraLab.
	 */
	private Map<String, List<GraphElementClassData>> edgeClassBuffer;

	private int putBackChar;

	static {
		reservedWords.add("Schema");
		reservedWords.add("EnumDomain");
		reservedWords.add("RecordDomain");
		reservedWords.add("Boolean");
		reservedWords.add("Integer");
		reservedWords.add("Long");
		reservedWords.add("Double");
		reservedWords.add("String");
		reservedWords.add("Object");
		reservedWords.add("List");
		reservedWords.add("Set");
		reservedWords.add("GraphClass");
		reservedWords.add("VertexClass");
		reservedWords.add("EdgeClass");
		reservedWords.add("AggregationClass");
		reservedWords.add("CompositionClass");
		reservedWords.add("abstract");
		reservedWords.add("redefines");
		reservedWords.add("role");
		reservedWords.add("from");
		reservedWords.add("to");
		reservedWords.add("aggregate");
		reservedWords.add("Graph");
		reservedWords.add("false");
		reservedWords.add("true");
	}

	private GraphIO() {
		domains = new TreeMap<String, Domain>();
		GECsearch = new HashMap<GraphElementClass, GraphClass>();
		createMethods = new HashMap<String, Method>();
		buffer = new byte[10140];
		bufferPos = 0;
		enumDomainBuffer = new HashSet<EnumDomainData>();
		recordDomainBuffer = new ArrayList<RecordDomainData>();
		graphClassBuffer = new ArrayList<GraphClassData>();
		vertexClassBuffer = new TreeMap<String, List<GraphElementClassData>>();
		edgeClassBuffer = new TreeMap<String, List<GraphElementClassData>>();
		putBackChar = -1;
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
		TGOut.writeBytes("Schema ");
		TGOut.writeBytes(schema.getPrefix() + "." + schema.getName() + ";\n");

		// write domains
		for (EnumDomain ed : schema.getEnumDomains()) {
			TGOut.writeBytes("EnumDomain " + ed.getName());
			TGOut.writeBytes(" (");
			for (Iterator<String> eit = ed.getConsts().iterator(); eit
					.hasNext();) {
				TGOut.writeBytes(eit.next());
				if (eit.hasNext())
					TGOut.writeBytes(", ");
			}
			TGOut.writeBytes(");\n");
		}

		for (CompositeDomain cd : schema
				.getCompositeDomainsInTopologicalOrder()) {
			if (cd instanceof RecordDomain) {
				TGOut.writeBytes("RecordDomain "
						+ ((RecordDomain) cd).getName());
				String delim = " (";
				for (Map.Entry<String, Domain> rdc : ((RecordDomain) cd)
						.getComponents().entrySet()) {
					TGOut.writeBytes(delim);
					TGOut.writeBytes(rdc.getKey() + ":");
					TGOut.writeBytes(rdc.getValue().getTGTypeName());
					delim = ", ";
				}
				TGOut.writeBytes(");\n");
			}
		}

		// write graphclasses
		for (GraphClass gc : schema.getGraphClassesInTopologicalOrder()) {
			if (gc.getName().equals("Graph"))
				continue;
			TGOut.writeBytes("GraphClass " + gc.getName());
			writeHierarchy(gc);
			writeAttributes(gc);
			TGOut.writeBytes(";\n");

			// write vertex classes
			for (VertexClass vc : schema.getVertexClassesInTopologicalOrder()) {
				if (gc.knowsOwn(vc) && vc.getName() != "Vertex") {
					if (vc.isAbstract())
						TGOut.writeBytes("abstract ");
					TGOut.writeBytes("VertexClass ");
					TGOut.writeBytes(vc.getName());
					writeHierarchy(vc);
					writeAttributes(vc);
					TGOut.writeBytes(";\n");
				}
			}

			// write edge classes
			for (EdgeClass ec : schema.getEdgeClassesInTopologicalOrder()) {
				if (gc.knowsOwn(ec) && ec.getName() != "Vertex"
						&& ec.getName() != "Aggregation"
						&& ec.getName() != "Composition") {
					if (ec.isAbstract())
						TGOut.writeBytes("abstract ");
					if (ec instanceof AggregationClass) {
						if (ec instanceof CompositionClass)
							TGOut.writeBytes("CompositionClass ");
						else
							TGOut.writeBytes("AggregationClass ");
					} else {
						TGOut.writeBytes("EdgeClass ");
					}
					TGOut.writeBytes(ec.getName());
					writeHierarchy(ec);

					// from (min,max) rolename
					TGOut.writeBytes(" from ");
					TGOut.writeBytes(ec.getFrom().getName() + " (");
					TGOut.writeBytes(ec.getFromMin() + ",");
					if (ec.getFromMax() == Integer.MAX_VALUE)
						TGOut.writeBytes("*)");
					else
						TGOut.writeBytes(ec.getFromMax() + ")");

					if (!ec.getFromRolename().equals("")) {
						TGOut.writeBytes(" role '");
						TGOut.writeBytes(ec.getFromRolename());
						String delim = " redefines ";
						for (String redefinedRolename : ec
								.getRedefinedFromRoles()) {
							TGOut.writeBytes(delim);
							delim = ",";
							TGOut.writeBytes(redefinedRolename);
						}
					}

					// to (min,max) rolename
					TGOut.writeBytes(" to ");
					TGOut.writeBytes(ec.getTo().getName() + " (");
					TGOut.writeBytes(ec.getToMin() + ",");
					if (ec.getToMax() == Integer.MAX_VALUE)
						TGOut.writeBytes("*)");
					else
						TGOut.writeBytes(ec.getToMax() + ")");
					if (!ec.getToRolename().equals("")) {
						TGOut.writeBytes(" role '");
						TGOut.writeBytes(ec.getToRolename());
						String delim = " redefines ";
						for (String redefinedRolename : ec
								.getRedefinedToRoles()) {
							TGOut.writeBytes(delim);
							delim = ",";
							TGOut.writeBytes(redefinedRolename);
						}
					}

					if (ec instanceof AggregationClass) {
						TGOut.writeBytes(" aggregate ");
						if (((AggregationClass) ec).isAggregateFrom())
							TGOut.writeBytes("from");
						else
							TGOut.writeBytes("to");
					}

					writeAttributes(ec);
					TGOut.writeBytes(";\n");
				}
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

		schema = graph.getSchema();
		saveSchema(schema);

		int eId;
		int vId;

		// progress bar for graph
		int graphElements = 0, currentCount = 0, interval = 1;
		if (pf != null) {
			pf.init(graph.getVCount() + graph.getECount());
			interval = pf.getInterval();
		}

		TGOut.writeBytes("\nGraph "
				+ toUTF(graph.getId() + "_" + graph.getGraphVersion()) + " "
				+ graph.getAttributedElementClass().getName() + " ("
				+ graph.getMaxVCount() + " " + graph.getMaxECount() + " "
				+ graph.getVCount() + " " + graph.getECount() + ")");
		space();
		graph.writeAttributeValues(this);
		TGOut.writeBytes(";\n");

		// write vertices
		Vertex nextV = graph.getFirstVertex();
		while (nextV != null) {
			vId = nextV.getId();
			// System.out.println("Writing vertex: " + nextV.getId());
			TGOut.writeBytes(vId + " "
					+ nextV.getAttributedElementClass().getName());
			TGOut.writeBytes(" ");
			// write incident edges
			Edge nextI = graph.getFirstEdge(vId);
			TGOut.writeBytes("<");
			noSpace();
			while (nextI != null) {
				// System.out.println("Writing incidence: " +
				// nextI.getId());
				eId = nextI.getId();
				writeInteger(eId);
				nextI = graph.getNextEdge(nextI);
			}
			TGOut.writeBytes(">");
			space();
			nextV.writeAttributeValues(this);
			TGOut.writeBytes(";\n");
			nextV = graph.getNextVertex(vId);

			// update progress bar
			if (pf != null) {
				if (!DEBUG) {
					graphElements++;
					currentCount++;
					if (currentCount == interval) {
						pf.progress(graphElements);
						currentCount = 0;
					}
				}
			}
		}

		// write edges
		Edge nextE = graph.getFirstEdgeInGraph();
		while (nextE != null) {
			eId = nextE.getId();
			// System.out.println("Writing edge: " + nextE.getId());
			TGOut.writeBytes(eId + " "
					+ nextE.getAttributedElementClass().getName());
			space();
			nextE.writeAttributeValues(this);
			TGOut.writeBytes(";\n");
			nextE = graph.getNextEdgeInGraph(eId);

			// update progress bar
			if (pf != null) {
				if (!DEBUG) {
					graphElements++;
					currentCount++;
					if (currentCount == interval) {
						pf.progress(graphElements);
						currentCount = 0;
					}
				}
			}

		}
		TGOut.flush();
		// finish progress bar
		if (pf != null)
			pf.finished();
	}

	private void writeHierarchy(AttributedElementClass aec) throws IOException {
		String delim = ": ";
		for (AttributedElementClass baseClass : aec.getDirectSuperClasses()) {
			if ((baseClass != this) && (!baseClass.isInternal())) {
				TGOut.writeBytes(delim);
				TGOut.writeBytes(baseClass.getName());
				delim = ", ";
			}
		}
	}

	private void writeAttributes(AttributedElementClass aec) throws IOException {
		if (aec.hasOwnAttributes())
			TGOut.writeBytes(" { ");
		for (Iterator<Attribute> ait = aec.getOwnAttributeList().iterator(); ait
				.hasNext();) {
			Attribute a = ait.next();
			TGOut.writeBytes(a.getName());
			TGOut.writeBytes(": ");
			String domain = a.getDomain().getTGTypeName();
			TGOut.writeBytes(domain);
			if (ait.hasNext())
				TGOut.writeBytes(", ");
			else
				TGOut.writeBytes(" }");
		}
	}

	public void write(String s) throws IOException {
		TGOut.writeBytes(s);
	}

	public void noSpace() {
		writeSpace = false;
	}

	public void space() {
		writeSpace = true;
	}

	public void writeSpace() throws IOException {
		if (writeSpace) {
			TGOut.writeBytes(" ");
		}
		writeSpace = true;
	}

	public void writeBoolean(boolean b) throws IOException {
		writeSpace();
		TGOut.writeBytes(b ? "t" : "f");
	}

	public void writeInteger(int i) throws IOException {
		writeSpace();
		TGOut.writeBytes(Integer.toString(i));
	}

	public void writeLong(long l) throws IOException {
		writeSpace();
		TGOut.writeBytes(Long.toString(l));
	}

	public void writeDouble(double d) throws IOException {
		writeSpace();
		TGOut.writeBytes(Double.toString(d));
	}

	public void writeUtfString(String s) throws IOException {
		writeSpace();
		if (s == null) {
			TGOut.writeBytes("\\null");
		} else {
			TGOut.writeBytes(toUTF(s));
		}
	}

	public void writeIdentifier(String s) throws IOException {
		writeSpace();
		if (reservedWords.contains(s))
			TGOut.writeBytes("'");
		TGOut.writeBytes(s);
	}

	public void writeObject(Object o) throws IOException {
		writeSpace();
		if (o == null) {
			TGOut.writeBytes("\\null");
			return;
		}
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		ObjectOutputStream oos = new ObjectOutputStream(bos);
		oos.writeObject(o);
		oos.close();
		StringBuffer b64 = new StringBuffer(Base64.encodeBytes(bos
				.toByteArray()));
		int p = b64.indexOf("\n");
		while (p >= 0) {
			b64.deleteCharAt(p);
			p = b64.indexOf("\n", p);
		}
		TGOut.writeBytes("$");
		TGOut.writeBytes(b64.toString());
	}

	public static Schema loadSchemaFromFile(String filename)
			throws GraphIOException {
		try {
			return loadSchemaFromStream(new FileInputStream(filename));
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

	public static Graph loadGraphFromFile(String filename, ProgressFunction pf)
			throws GraphIOException {
		try {
			return loadGraphFromStream(new FileInputStream(filename), pf);
		} catch (IOException ex) {
			throw new GraphIOException("Unable to load graph from file "
					+ filename + ", the file cannot be found", ex);
		}
	}

	public static Graph loadGraphFromURL(String url, ProgressFunction pf)
			throws GraphIOException {
		try {
			return loadGraphFromStream(new URL(url).openStream(), pf);
		} catch (IOException ex) {
			throw new GraphIOException("Unable to load graph from url " + url
					+ ", the resource cannot be found", ex);
		}
	}

	public static Graph loadGraphFromStream(InputStream in, ProgressFunction pf)
			throws GraphIOException {
		try {
			GraphIO io = new GraphIO();
			io.TGIn = in;
			io.tgfile();
			/*
			 * since there is a class CityMapSchema for the Schema
			 * CityMapSchema, the instance of this singleton class is created so
			 * the special methods of this class can be used later
			 */
			String schemaName = io.schema.getFullName();
			Class<?> schemaClass = Class.forName(schemaName, true,
					M1ClassManager.instance());
			Method instanceMethod = schemaClass.getMethod("instance",
					(Class<?>[]) null);
			io.schema = (Schema) instanceMethod.invoke(null, new Object[0]);
			return io.graph(pf);
		} catch (GraphIOException e) {
			throw e;
		} catch (ClassNotFoundException e) {
			// the class was not found, so the schema.commit-method was not
			// called yet, an exception will be thrown
			throw new GraphIOException(
					"Unable to load a graph which belongs to the schema because the Java-classes for this schema have not yet been created. Use Schema.commit(..) to create them!",
					e);
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
	 * @throws GraphIOException @
	 */
	private void schema() throws GraphIOException, SchemaException {
		match("Schema");
		String schemaName = parseSchemaName(); // read Schema name
		String prefixName = "";
		if (schemaName.indexOf(".") != -1) {
			prefixName = schemaName.substring(0, schemaName.lastIndexOf("."));
			schemaName = schemaName.substring(schemaName.lastIndexOf(".") + 1);
		}
		match(";");

		assert !schemaName.equals("") : "empty schema name";
		schema = new SchemaImpl(schemaName, prefixName); // create Schema
		if (DEBUG)
			System.out.println("found schema " + schemaName + " with prefix "
					+ prefixName);

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
		sortGraphClasses();
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
	 * @throws GraphIOException @
	 */
	private Map<String, Domain> domDef() throws GraphIOException,
			SchemaException {
		domains.put("Boolean", schema.getDomain("Boolean"));
		domains.put("Integer", schema.getDomain("Integer"));
		domains.put("Long", schema.getDomain("Long"));
		domains.put("Double", schema.getDomain("Double"));
		domains.put("String", schema.getDomain("String"));
		domains.put("Object", schema.getDomain("Object"));

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
		enumDomainBuffer.add(new EnumDomainData(matchIdentifier(true),
				parseEnumConstants()));
		match(";");
	}

	/**
	 * Creates all EnumDomains whose data is stored in {@link enumDomainBuffer}
	 *  @
	 */
	private void enumDomains() {
		Domain domain;

		for (EnumDomainData enumDomainData : enumDomainBuffer) {
			domain = schema.createEnumDomain(enumDomainData.name,
					enumDomainData.enumConstants);
			domains.put(enumDomainData.name, domain);
			if (DEBUG)
				System.out.println("(" + domain.toString() + ")");
		}
	}

	/**
	 * Read a RecordDomain, i.e. its name along with the components.
	 * 
	 * @throws GraphIOException
	 */
	private void parseRecordDomain() throws GraphIOException {
		match("RecordDomain");
		recordDomainBuffer.add(new RecordDomainData(matchIdentifier(true),
				parseRecordComponents()));
		match(";");
	}

	/**
	 * Creates all RecordDomains whose data is stored in
	 * {@link recordDomainBuffer}
	 *  @
	 */
	private void recordDomains() throws GraphIOException, SchemaException {
		Domain domain;

		for (RecordDomainData recordDomainData : recordDomainBuffer) {
			domain = schema.createRecordDomain(recordDomainData.name,
					getComponents(recordDomainData.components));
			domains.put(recordDomainData.name, domain);
			if (DEBUG)
				System.out.println("(" + domain.toString() + ")");
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
			Map<String, List<String>> componentsData) throws GraphIOException {
		Map<String, Domain> componentDomains = new TreeMap<String, Domain>();
		Domain domain;

		for (Entry<String, List<String>> componentData : componentsData
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
	 * @throws GraphIOException @
	 */
	private void parseSchema() throws GraphIOException, SchemaException {
		String currentGraphClassName = null;

		while (lookAhead.equals("RecordDomain")
				|| lookAhead.equals("EnumDomain")
				|| lookAhead.equals("GraphClass")
				|| lookAhead.equals("abstract")
				|| lookAhead.equals("VertexClass")
				|| lookAhead.equals("EdgeClass")
				|| lookAhead.equals("AggregationClass")
				|| lookAhead.equals("CompositionClass")) {
			if (lookAhead.equals("RecordDomain")) {
				if (DEBUG)
					System.out.print("found RecordDomain");
				parseRecordDomain();
			} else if (lookAhead.equals("EnumDomain")) {
				if (DEBUG)
					System.out.print("found EnumDomain");
				parseEnumDomain();
			} else if (lookAhead.equals("GraphClass")) {
				currentGraphClassName = parseGraphClass();
			} else {
				if (currentGraphClassName == null) {
					throw new GraphIOException(
							"Definition of GraphElementClass before"
									+ "definition of GraphClass");
				}
				parseGraphElementClass(currentGraphClassName);
			}
		}
		if (DEBUG) {
			System.err.println("END OF parseSchema, LA=" + lookAhead);
		}
	}

	/**
	 * Creates the GraphClasses contained in the Schema along with their
	 * GraphElementClasses.
	 * 
	 * @throws GraphIOException
	 * @throws SchemaException
	 */
	private void completeGraphClass() throws GraphIOException, SchemaException {
		for (GraphClassData currentGraphClassData : graphClassBuffer) {
			GraphClass currentGraphClass = graphClass(currentGraphClassData);
			for (GraphElementClassData currentGraphElementClassData : vertexClassBuffer
					.get(currentGraphClassData.name))
				vertexClass(currentGraphElementClassData, currentGraphClass);
			for (GraphElementClassData currentGraphElementClassData : edgeClassBuffer
					.get(currentGraphClassData.name))
				edgeClass(currentGraphElementClassData, currentGraphClass);
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
		GraphClassData graphClassData = new GraphClassData();

		if (lookAhead.equals("abstract")) {
			match();
			graphClassData.isAbstract = true;
		}
		match("GraphClass");
		if (DEBUG) {
			System.out.print("found ");
			if (graphClassData.isAbstract)
				System.out.print("abstract ");
			System.out.print("GraphClass ");
		}
		graphClassData.name = matchIdentifier(true);
		if (DEBUG)
			System.out.print("(" + graphClassData.name + ")");
		if (lookAhead.equals(":"))
			graphClassData.directSuperClasses = parseHierarchy();
		if (lookAhead.equals("{"))
			graphClassData.attributes = parseAttributes();
		match(";");
		if (DEBUG)
			System.out.println();

		graphClassBuffer.add(graphClassData);
		vertexClassBuffer.put(graphClassData.name,
				new ArrayList<GraphElementClassData>());
		edgeClassBuffer.put(graphClassData.name,
				new ArrayList<GraphElementClassData>());

		return graphClassData.name;
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
	private GraphClass graphClass(GraphClassData gcData)
			throws GraphIOException, SchemaException {
		GraphClass gc = schema.createGraphClass(gcData.name);

		gc.setAbstract(gcData.isAbstract);
		gc.addAttributes(attributes(gcData.attributes).values());

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
		if (DEBUG)
			System.out.print(", superclasses: ");
		match(":");
		hierarchy.add(matchIdentifier(true));
		while (lookAhead.equals(",")) {
			match();
			hierarchy.add(matchIdentifier(true));
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
	private Map<String, List<String>> parseAttributes() throws GraphIOException {
		Map<String, List<String>> attributesData = new TreeMap<String, List<String>>();
		LinkedList<String> attributeDomain = new LinkedList<String>();
		String currentAttributeName;

		match("{");
		currentAttributeName = matchIdentifier(false);
		match(":");
		parseAttrDomain(attributeDomain);
		attributesData.put(currentAttributeName, attributeDomain);
		while (lookAhead.equals(",")) {
			attributeDomain = new LinkedList<String>();
			match();
			currentAttributeName = matchIdentifier(false);
			if (attributesData.containsKey(currentAttributeName))
				throw new GraphIOException("duplicate attribute name '"
						+ currentAttributeName + "' in line " + line);
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
			Map<String, List<String>> attributesData) throws GraphIOException {
		Map<String, Attribute> attributes = new TreeMap<String, Attribute>();
		Attribute attribute;

		for (Entry<String, List<String>> attributeData : attributesData
				.entrySet()) {
			Domain domain;

			domain = attrDomain(attributeData.getValue());
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
	private void parseAttrDomain(List<String> attrDomain)
			throws GraphIOException {
		if (DEBUG)
			System.out.print(lookAhead);
		if (lookAhead.equals("List")) {
			match();
			match("<");
			attrDomain.add("List");
			parseAttrDomain(attrDomain);
			match(">");
		} else if (lookAhead.equals("Set")) {
			match();
			match("<");
			attrDomain.add("Set");
			parseAttrDomain(attrDomain);
			match(">");
		} else {
			attrDomain.add(lookAhead);
			match();
		}
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
		if (DEBUG)
			System.out.print(lookAhead);
		Domain result = null;
		for (String domainName : domainNames) {
			if (domainName.equals("List")) {
				try {
					domainNames.remove(0);
					result = schema.createListDomain(attrDomain(domainNames));
				} catch (SchemaException e) {
					throw new GraphIOException(
							"can't create list domain in line " + line, e);
				}
			} else if (domainName.equals("Set")) {
				try {
					domainNames.remove(0);
					result = schema.createSetDomain(attrDomain(domainNames));
				} catch (SchemaException e) {
					throw new GraphIOException(
							"can't create set domain in line " + line, e);
				}
			} else
				result = schema.getDomain(domainName);
			if (result == null) {
				throw new GraphIOException("undefined domain '" + domainName
						+ "' in line " + line);
			}
		}

		return result;
	}

	/**
	 * Reads the name of a GraphClass.
	 * 
	 * @return The name of the GraphClass.
	 * @throws GraphIOException
	 */
	private String parseEnumConstant() throws GraphIOException {
		if (isValidEnumConstant(lookAhead))
			return matchAndNext();
		throw new GraphIOException("invalid enumeration constant '" + lookAhead
				+ "' in line " + line);
	}

	private boolean isValidEnumConstant(String name) {
		for (int i = 0; i < name.length(); i++) {
			if (Character.isLowerCase(name.charAt(i))) {
				return false;
			}
		}
		return true;
	}

	/**
	 * Reads the a GraphElementClass of the GraphClass indicated by the given
	 * name.
	 * 
	 * @throws GraphIOException
	 */
	private void parseGraphElementClass(String gcName) throws GraphIOException,
			SchemaException {
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

			graphElementClassData.name = matchIdentifier(true);
			if (DEBUG)
				System.out.print(" (" + graphElementClassData.name + ")");
			if (lookAhead.equals(":")) {
				graphElementClassData.directSuperClasses = parseHierarchy();
				if (DEBUG)
					System.out.println("Added hierarchy for "
							+ graphElementClassData.name + " "
							+ graphElementClassData.directSuperClasses);
			}
			if (lookAhead.equals("{")) {
				graphElementClassData.attributes = parseAttributes();
			}
			match(";");

			vertexClassBuffer.get(gcName).add(graphElementClassData);
		} else if (lookAhead.equals("EdgeClass")
				|| lookAhead.equals("AggregationClass")
				|| lookAhead.equals("CompositionClass")) {
			type = lookAhead;
			match(type);
			graphElementClassData.type = type;

			graphElementClassData.name = matchIdentifier(true);
			if (lookAhead.equals(":"))
				graphElementClassData.directSuperClasses = parseHierarchy();
			match("from");
			graphElementClassData.fromVertexClassName = matchIdentifier(true);
			if (DEBUG)
				System.out.print(" from "
						+ graphElementClassData.fromVertexClassName);

			graphElementClassData.fromMultiplicity = parseMultiplicity();
			graphElementClassData.fromRoleName = parseRoleName();
			graphElementClassData.redefinedFromRoles = parseRolenameRedefinitions();
			match("to");

			graphElementClassData.toVertexClassName = matchIdentifier(true);
			if (DEBUG)
				System.out.print(" to "
						+ graphElementClassData.toVertexClassName);

			graphElementClassData.toMultiplicity = parseMultiplicity();
			graphElementClassData.toRoleName = parseRoleName();
			graphElementClassData.redefinedToRoles = parseRolenameRedefinitions();
			if (graphElementClassData.type.equals("AggregationClass")
					|| graphElementClassData.type.equals("CompositionClass"))
				graphElementClassData.aggregateFrom = parseAggregate();
			if (lookAhead.equals("{"))
				graphElementClassData.attributes = parseAttributes();
			match(";");

			edgeClassBuffer.get(gcName).add(graphElementClassData);
		}

		if (DEBUG)
			System.out.println();
		if (DEBUG)
			System.err.println("END OF parseGraphElementClasses, LA="
					+ lookAhead);
	}

	private VertexClass vertexClass(GraphElementClassData vcd, GraphClass gc)
			throws GraphIOException, SchemaException {
		if (DEBUG)
			System.out.print(" (" + vcd.name + ")");
		VertexClass vc = gc.createVertexClass(vcd.name);
		vc.addAttributes(attributes(vcd.attributes).values());
		vc.setAbstract(vcd.isAbstract);
		GECsearch.put(vc, gc);

		return vc;
	}

	private EdgeClass edgeClass(GraphElementClassData ecd, GraphClass gc)
			throws GraphIOException, SchemaException {
		EdgeClass ec;

		if (DEBUG)
			System.out.print(" (" + ecd.name + ")");

		if (ecd.type.equals("EdgeClass"))
			ec = gc.createEdgeClass(ecd.name, gc
					.getVertexClass(ecd.fromVertexClassName),
					ecd.fromMultiplicity[0], ecd.fromMultiplicity[1],
					ecd.fromRoleName, gc.getVertexClass(ecd.toVertexClassName),
					ecd.toMultiplicity[0], ecd.toMultiplicity[1],
					ecd.toRoleName);
		else if (ecd.type.equals("AggregationClass"))
			ec = gc.createAggregationClass(ecd.name, gc
					.getVertexClass(ecd.fromVertexClassName),
					ecd.fromMultiplicity[0], ecd.fromMultiplicity[1],
					ecd.fromRoleName, ecd.aggregateFrom, gc
							.getVertexClass(ecd.toVertexClassName),
					ecd.toMultiplicity[0], ecd.toMultiplicity[1],
					ecd.toRoleName);
		else if (ecd.type.equals("CompositionClass"))
			ec = gc.createCompositionClass(ecd.name, gc
					.getVertexClass(ecd.fromVertexClassName),
					ecd.fromMultiplicity[0], ecd.fromMultiplicity[1],
					ecd.fromRoleName, ecd.aggregateFrom, gc
							.getVertexClass(ecd.toVertexClassName),
					ecd.toMultiplicity[0], ecd.toMultiplicity[1],
					ecd.toRoleName);
		else
			throw new SchemaException("Unknown type " + ecd.type);

		ec.addAttributes(attributes(ecd.attributes).values());
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

		if (DEBUG)
			System.out.print("(" + multis[0] + "," + multis[1] + ")");
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
			String result = matchIdentifier(false);
			return result;
		}
		return "";
	}

	/**
	 * Reads the redefinition of a rolename of an EdgeClass
	 * 
	 * @return A Set<String> of redefined rolenames or <code>null</code> if
	 *         no rolenames were redefined
	 * @throw GraphIOException
	 */
	private List<String> parseRolenameRedefinitions() throws GraphIOException {
		if (!lookAhead.equals("redefines")) {
			return null;
		}
		match();
		List<String> result = new ArrayList<String>();
		String redefinedName = matchIdentifier(false);
		result.add(redefinedName);
		while (lookAhead.equals(",")) {
			match();
			redefinedName = matchIdentifier(false);
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

	/**
	 * Reads the name of a Schema.
	 * 
	 * @return The name of the schema.
	 * @throws GraphIOException
	 */
	private String parseSchemaName() throws GraphIOException {
		if (isValidSchemaName(lookAhead)) {
			return matchAndNext();
		}
		throw new GraphIOException("invalid schema name '" + lookAhead
				+ "' in line " + line);
	}

	private boolean isValidPackageName(String s) {
		if (!isValidIdentifier(s))
			return false;
		for (char c : s.toCharArray()) {
			if (!(Character.isLowerCase(c) || Character.isDigit(c) || c == '_'))
				return false;
		}
		return true;
	}

	private boolean isValidSchemaName(String s) {
		String[] parts = s.split("\\.");
		if (parts.length < 1)
			return false;
		for (int i = 0; i < parts.length - 1; i++) {
			if (!isValidPackageName(parts[i])) {
				if (DEBUG)
					System.out.println(parts[i]
							+ " is not a valid package name ");
				return false;
			}
		}
		if (parts[parts.length - 1].charAt(0) == '\'') {
			parts[parts.length - 1] = parts[parts.length - 1].substring(1);
		}

		return isValidIdentifier(parts[parts.length - 1])
				&& Character.isUpperCase(parts[parts.length - 1].charAt(0));
	}

	/**
	 * Reads the components of a RecordDomain from the TG-file.
	 * 
	 * @return A map of component names to lists of Strings representing the
	 *         component's domain.
	 * @throws GraphIOException
	 */
	private Map<String, List<String>> parseRecordComponents()
			throws GraphIOException {
		Map<String, List<String>> componentsData = new TreeMap<String, List<String>>();
		List<String> recordComponentDomain = new LinkedList<String>();
		String recordComponentName;

		match("(");
		recordComponentName = matchIdentifier(false);
		match(":");
		parseAttrDomain(recordComponentDomain);
		componentsData.put(recordComponentName, recordComponentDomain);
		while (lookAhead.equals(",")) {
			recordComponentDomain = new LinkedList<String>();
			match();
			recordComponentName = matchIdentifier(false);
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
		enums.add(parseEnumConstant());
		while (lookAhead.equals(",")) {
			match();
			String s = parseEnumConstant();
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

	private void buildGraphClassHierarchy() throws GraphIOException,
			SchemaException {
		AttributedElementClass aec;
		GraphClass superClass;

		for (GraphClassData gcData : graphClassBuffer) {
			aec = schema.getAttributedElementClass(gcData.name);
			if (aec == null)
				throw new GraphIOException("undefined AttributedElementClass '"
						+ gcData.name + "'");
			if (aec instanceof GraphClass)
				for (String superClassName : gcData.directSuperClasses) {
					superClass = schema.getGraphClass(superClassName);
					if (superClass == null)
						throw new GraphIOException("undefined GraphClass '"
								+ superClassName + "'");
					((GraphClass) aec).addSuperClass(superClass);
				}
		}
	}

	private void buildVertexClassHierarchy() throws GraphIOException,
			SchemaException {
		AttributedElementClass aec;
		VertexClass superClass;

		for (Entry<String, List<GraphElementClassData>> gcElements : vertexClassBuffer
				.entrySet())
			for (GraphElementClassData vData : gcElements.getValue()) {
				aec = schema.getAttributedElementClass(vData.name);
				if (aec == null)
					throw new GraphIOException(
							"undefined AttributedElementClass '" + vData.name
									+ "'");
				if (aec instanceof VertexClass)
					for (String superClassName : vData.directSuperClasses) {
						superClass = (VertexClass) (GECsearch.get(aec)
								.getGraphElementClass(superClassName));
						if (superClass == null)
							throw new GraphIOException(
									"undefined VertexClass '" + superClassName
											+ "'");
						((VertexClass) aec).addSuperClass(superClass);
					}
			}
	}

	private void buildEdgeClassHierarchy() throws GraphIOException,
			SchemaException {
		AttributedElementClass aec;
		EdgeClass superClass;

		for (Entry<String, List<GraphElementClassData>> gcElements : edgeClassBuffer
				.entrySet())
			for (GraphElementClassData eData : gcElements.getValue()) {
				aec = schema.getAttributedElementClass(eData.name);
				if (aec == null)
					throw new GraphIOException(
							"undefined AttributedElementClass '" + eData.name
									+ "'");
				if (aec instanceof EdgeClass)
					for (String superClassName : eData.directSuperClasses) {
						superClass = (EdgeClass) (GECsearch.get(aec)
								.getGraphElementClass(superClassName));
						if (superClass == null)
							throw new GraphIOException("undefined EdgeClass '"
									+ superClassName + "'");
						((EdgeClass) aec).addSuperClass(superClass);
					}
			}
	}

	private void buildHierarchy() throws GraphIOException, SchemaException {
		buildGraphClassHierarchy();
		buildVertexClassHierarchy();
		buildEdgeClassHierarchy();
	}

	private String nextToken() throws GraphIOException {
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
			// if (out.equals("")) System.exit(0);
			if (PARSE_DEBUG) {
				System.out.print("[read:" + out + "]");
			}
		} catch (IOException e) {
			throw new GraphIOException(
					"error on reading bytes from file, line " + line
							+ ", last char read was [" + (char) la + "]", e);
		}
		return out.toString();
	}

	private int read() throws GraphIOException {
		if (putBackChar >= 0) {
			int result = putBackChar;
			putBackChar = -1;
			return result;
		}
		if (bufferPos < bufferSize)
			return buffer[bufferPos++];
		else {
			try {
				bufferSize = TGIn.read(buffer);
			} catch (IOException e) {
				throw new GraphIOException("Error while loading Graph");
			}
			if (bufferSize != -1) {
				bufferPos = 0;
				return buffer[bufferPos++];
			} else
				return -1;
		}
	}

	private void readUtfString(StringBuilder out) throws IOException,
			GraphIOException {
		int startLine = line;
		la = read();
		while (la != -1 && la != '"') {
			if (la < 32 || la > 127) {
				throw new GraphIOException("invalid character '" + (char) la
						+ "' in string in line " + line);
			}
			if (la == '\\') {
				la = read();
				if (la == -1)
					continue;
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
					if (la == -1)
						continue;
					String unicode = "" + (char) la;
					la = read();
					if (la == -1)
						continue;
					unicode += (char) la;
					la = read();
					if (la == -1)
						continue;
					unicode += (char) la;
					la = read();
					if (la == -1)
						continue;
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

	private static boolean isWs(int c) {
		return c == ' ' || c == '\n' || c == '\t' || c == '\r';
	}

	private static boolean isSeparator(int c) {
		return c == ';' || c == '<' || c == '>' || c == '(' || c == ')'
				|| c == '{' || c == '}' || c == ':' || c == '[' || c == ']'
				|| c == ',';
	}

	private void skipWs() throws GraphIOException {
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
					// System.err.println("Comment detected in line " + line);
					while (la >= 0 && la != '\n') {
						la = read();
					}
				} else {
					putback(la);
				}
			}
		} while (isWs(la));
	}

	private void putback(int ch) {
		putBackChar = ch;
	}

	private String matchAndNext() throws GraphIOException {
		String result = lookAhead;
		match();
		return result;
	}

	public boolean isNextToken(String token) {
		return lookAhead.equals(token);
	}

	private void match() throws GraphIOException {
		lookAhead = nextToken();
	}

	public void match(String s) throws GraphIOException {
		if (lookAhead.equals(s)) {
			lookAhead = nextToken();
		} else {
			throw new GraphIOException("expected [" + s + "] but found ["
					+ lookAhead + "] in line " + line, null);
		}
	}

	public int matchInteger() throws GraphIOException {
		try {
			int result = Integer.parseInt(lookAhead);
			match();
			return result;
		} catch (NumberFormatException e) {
			throw new GraphIOException("expected int number but found '"
					+ lookAhead + "' in line " + line, e);
		}
	}

	public long matchLong() throws GraphIOException {
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
	public String matchIdentifier(boolean isUpperCase) throws GraphIOException {
		String result = (lookAhead.charAt(0) == '\'') ? lookAhead.substring(1)
				: lookAhead;
		if (!isValidIdentifier(result)
				&& (isUpperCase && Character.isUpperCase(result.charAt(0)))
				&& (!isUpperCase && Character.isLowerCase(result.charAt(0)))) {
			throw new GraphIOException("invalid identifier '" + lookAhead
					+ "' in line " + line);
		}
		match();
		return result;
	}

	public Object matchObject() throws GraphIOException {
		if (lookAhead.equals("\\null")) {
			match();
			return null;
		}
		try {
			if (!(lookAhead.charAt(0) == '$')) {
				throw new GraphIOException(
						"can't read object, base64 code must start with a '$'");
			}
			byte[] array = Base64.decode(lookAhead.substring(1));
			ByteArrayInputStream bais = new ByteArrayInputStream(array);
			ObjectInputStream ois = new ObjectInputStream(bais);
			Object result = ois.readObject();
			if (DEBUG)
				System.out.print("[serialized base64 coded java object]");
			ois.close();
			match();
			return result;
		} catch (IOException e) {
			throw new GraphIOException("can't decode object from base64", e);
		} catch (ClassNotFoundException e) {
			throw new GraphIOException("can't decode object from base64", e);
		}
	}

	public String matchUtfString() throws GraphIOException {
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

	public boolean matchBoolean() throws GraphIOException {
		if (!lookAhead.equals("t") && !lookAhead.equals("f")) {
			throw new GraphIOException(
					"expected a boolean constant but found '" + lookAhead
							+ "' in line " + line);
		}
		boolean result = lookAhead.equals("t");
		match();
		return result;
	}

	private Graph graph(ProgressFunction pf) throws GraphIOException {
		match("Graph");
		if (DEBUG)
			System.out.print("found Graph ");

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

		if (DEBUG) {
			System.out.print("with ID '" + graphId + "', ");
			System.out.print("with version '" + graphVersion + "'");
		}

		gcName = matchAndNext();
		assert isValidIdentifier(gcName) : "illegal characters in graph class '"
				+ gcName + "'";
		// check if classname is known in the schema
		if (schema.getGraphClass(gcName) == null)
			throw new GraphIOException("Graph Class " + gcName
					+ "does not exist");
		if (DEBUG)
			System.out.print(" and GraphClass '" + gcName + "'");
		match("(");
		int maxV = matchInteger();
		int maxE = matchInteger();
		int vCount = matchInteger();
		int eCount = matchInteger();
		match(")");
		if (DEBUG)
			System.out.print(" (" + maxV + "," + maxE + "," + vCount + ","
					+ eCount + ")");

		// verify vCount <= maxV && eCount <= maxE
		if (vCount > maxV)
			throw new GraphIOException("Number of vertices in graph (" + vCount
					+ ") exceeds maximum number of vertices (" + maxV + ")");
		if (eCount > maxE)
			throw new GraphIOException("Number of edges in graph (" + eCount
					+ ") exceeds maximum number of edges (" + maxE + ")");

		// adjust fields for incidences
		edgeIn = new Vertex[maxE + 1];
		edgeOut = new Vertex[maxE + 1];
		lastEdgeAtVertex = new int[maxV + 1];
		firstEdgeAtVertex = new int[maxV + 1];
		for (int i = 0; i < maxV + 1; i++) {
			lastEdgeAtVertex[i] = 0;
			firstEdgeAtVertex[i] = 0;
		}
		nextEdgeAtVertex = new int[(maxE + 1) * 2];
		for (int i = 0; i < (maxE + 1) * 2; i++) {
			nextEdgeAtVertex[i] = 0;
		}
		edgeOffset = maxE + 1;
		// progress bar for graph
		// ProgressFunction pf;
		int graphElements = 0, currentCount = 0, interval = 1;
		if (pf != null) {
			pf.init(vCount + eCount);
			interval = pf.getInterval();
		}
		if (DEBUG)
			System.out.println(("Get Graph Create method"));
		Graph graph = null;
		try {
			graph = (Graph) schema.getGraphCreateMethod(gcName).invoke(null,
					new Object[] { graphId, maxV, maxE });
		} catch (Exception e) {
			throw new GraphIOException("can't create graph for class '"
					+ gcName + "'", e);
		}
		if (DEBUG)
			System.out.println(("Reading attribute values"));
		graph.readAttributeValues(this);
		match(";");
		if (DEBUG)
			System.out.println("Start reading vertices");
		// long time = System.currentTimeMillis();
		for (int vNo = 1; vNo <= vCount; vNo++) {
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
		}
		// System.out.println((System.currentTimeMillis() - time) / 1000.0);

		// time = System.currentTimeMillis();
		for (int eNo = 1; eNo <= eCount; eNo++) {
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
		}
		((de.uni_koblenz.jgralab.impl.array.GraphImpl) graph)
				.overwriteEdgeAtVertexArrays(firstEdgeAtVertex,
						nextEdgeAtVertex, lastEdgeAtVertex);

		graph.setGraphVersion(graphVersion);
		// System.out.println((System.currentTimeMillis() - time) / 1000.0);
		if (pf != null) {
			pf.finished();
		}
		return graph;
	}

	public double matchDouble() throws GraphIOException {
		try {
			double result = Double.parseDouble(lookAhead);
			match();
			return result;
		} catch (NumberFormatException e) {
			throw new GraphIOException("expected a double value but found '"
					+ lookAhead + "'", e);
		}
	}

	private void vertexDesc(Graph graph) throws GraphIOException {
		int vId = vId();
		String vcName = className();
		Vertex vertex;
		Method createMethod;
		if (DEBUG)
			System.out.println("Graph is : " + graph);
		createMethod = createMethods.get(vcName);
		try {
			if (createMethod == null) {
				createMethod = schema.getVertexCreateMethod(vcName, gcName);
				createMethods.put(vcName, createMethod);
				if (DEBUG)
					System.out.println("Create method is: " + createMethod);
			}
			vertex = (Vertex) createMethod.invoke(graph, new Object[] { vId });
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
		String ecName = className();
		Edge edge;
		Method createMethod;

		createMethod = createMethods.get(ecName);
		try {
			if (createMethod == null) {
				createMethod = schema.getEdgeCreateMethod(ecName, gcName);
				createMethods.put(ecName, createMethod);
			}
			edge = (Edge) createMethod.invoke(graph, new Object[] { eId,
					edgeOut[eId], edgeIn[eId] });
		} catch (Exception e) {
			throw new GraphIOException("cant't create edge '" + eId + "'", e);
		}
		edge.readAttributeValues(this);
		match(";");
		if (DEBUG)
			System.out.println("ok");

	}

	private int eId() throws GraphIOException {
		int eId = matchInteger();
		if (eId == 0)
			throw new GraphIOException("Invalid edge id " + eId + ".");
		return eId;
	}

	private String className() throws GraphIOException {
		String className = matchAndNext();
		if (!schema.knows(className))
			throw new GraphIOException("Class " + className
					+ " of read element does not exist.");
		return className;
	}

	private int vId() throws GraphIOException {
		int vId = matchInteger();
		if (vId <= 0)
			throw new GraphIOException("Invalid vertex id " + vId + ".");
		return vId;
	}

	private void parseIncidentEdges(Vertex v) throws GraphIOException {
		int eId = 0;
		int prevId = 0;
		int vId = v.getId();

		if (DEBUG)
			System.out.print(", incidences: <");
		match("<");
		while (!lookAhead.equals(">")) {
			prevId = eId;
			eId = eId();
			if (firstEdgeAtVertex[vId] == 0) {
				firstEdgeAtVertex[vId] = eId;
			} else {
				nextEdgeAtVertex[edgeOffset + prevId] = eId;
			}
			if (eId < 0) {
				edgeIn[-eId] = v;
			} else
				edgeOut[eId] = v;
		}
		match();
		lastEdgeAtVertex[vId] = eId;
		if (DEBUG)
			System.out.print(">");
	}

	private static String toUTF(String value) {
		if (value == null)
			return "";
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
					out += (char) c;
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
				for (List<String> componentDomains : rd.components.values()) {
					for (String componentDomain : componentDomains) {
						if (componentDomain.equals("String")
								|| componentDomain.equals("Integer")
								|| componentDomain.equals("Boolean")
								|| componentDomain.equals("Object")
								|| componentDomain.equals("Long")
								|| componentDomain.equals("Double")
								|| componentDomain.equals("Set")
								|| componentDomain.equals("List")) {
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

	private void sortGraphClasses() throws GraphIOException {
		List<GraphClassData> orderedGcList = new ArrayList<GraphClassData>();
		Set<String> orderedGcNames = new TreeSet<String>();
		GraphClassData gc;
		boolean definedGcName;

		// iteratively add GraphClasses from graphClassBuffer,
		// whose superclasses already are in orderedGcList,
		// to orderedGcList
		// the added GraphClasses are removed from graphClassBuffer
		while (!graphClassBuffer.isEmpty()) {
			for (Iterator<GraphClassData> gcit = graphClassBuffer.iterator(); gcit
					.hasNext();) {
				gc = gcit.next();
				// check if all superclasses exist among already sorted
				// GraphClasses
				if (orderedGcNames.containsAll(gc.directSuperClasses)) {
					orderedGcList.add(gc);
					orderedGcNames.add(gc.name);
					gcit.remove();
				} else {
					/*
					 * check if some superclasses exist among yet unsorted
					 * GraphClasses
					 */
					for (String superClass : gc.directSuperClasses) {
						if (orderedGcNames.contains(superClass)) {
							continue;
						}
						definedGcName = false;
						for (GraphClassData gc2 : graphClassBuffer) {
							if (gc2.name.equals(superClass)) {
								definedGcName = true;
								break;
							}
						}
						if (!definedGcName) {
							throw new GraphIOException("GraphClass "
									+ superClass + " does not exist");
						}
					}
				}
			}
		}
		graphClassBuffer = orderedGcList;
	}

	private void sortVertexClasses() throws GraphIOException {
		List<GraphElementClassData> orderedVcList, unorderedVcList;
		Set<String> orderedVcNames = new TreeSet<String>();
		GraphElementClassData vc;
		boolean definedVcName;

		for (GraphClassData graphClass : graphClassBuffer) {
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
						for (String superClass : vc.directSuperClasses) {
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
	}

	private void sortEdgeClasses() throws GraphIOException {
		List<GraphElementClassData> orderedEcList, unorderedEcList;
		Set<String> orderedEcNames = new TreeSet<String>();
		GraphElementClassData ec;
		boolean definedEcName;

		for (GraphClassData graphClass : graphClassBuffer) {
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
						for (String superClass : ec.directSuperClasses) {
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
						if (ec.fromVertexClassName.equals(vc.name)
								|| ec.fromVertexClassName.equals("Vertex")) {
							existingFromVertexClass = true;
						}
						if (ec.toVertexClassName.equals(vc.name)
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
					throw new GraphIOException("VertexClass "
							+ ec.fromVertexClassName + " does not exist");
				}
				if (!existingToVertexClass) {
					throw new GraphIOException("VertexClass "
							+ ec.toVertexClassName + " does not exist");
				}
			}
		}
	}

	/**
	 * EnumDomainData contains the parsed data of an EnumDomain. This data is
	 * used to create an EnumDomain.
	 */
	private class EnumDomainData {
		String name;

		List<String> enumConstants;

		EnumDomainData(String name, List<String> enumConstants) {
			this.name = name;
			this.enumConstants = enumConstants;
		}
	}

	/**
	 * RecordDomainData contains the parsed data of a RecordDomain. This data is
	 * used to create a RecordDomain.
	 */
	private class RecordDomainData {
		String name;

		Map<String, List<String>> components;

		RecordDomainData(String name, Map<String, List<String>> components) {
			this.name = name;
			this.components = components;
		}
	}

	/**
	 * GraphClassData contains the parsed data of a GraphClass. This data is
	 * used to create a GraphClass.
	 */
	private class GraphClassData {
		String name;

		boolean isAbstract = false;

		List<String> directSuperClasses = new LinkedList<String>();

		Map<String, List<String>> attributes = new TreeMap<String, List<String>>();
	}

	/**
	 * GraphElementClassData contains the parsed data of a GraphElementClass.
	 * This data is used to create a GraphElementClass.
	 */
	private class GraphElementClassData {
		String name;

		String type;

		boolean isAbstract = false;

		List<String> directSuperClasses = new LinkedList<String>();;

		String fromVertexClassName;

		int[] fromMultiplicity = { 1, Integer.MAX_VALUE };

		String fromRoleName = "";

		List<String> redefinedFromRoles = null;

		String toVertexClassName;

		int[] toMultiplicity = { 1, Integer.MAX_VALUE };

		String toRoleName = "";

		List<String> redefinedToRoles = null;

		boolean aggregateFrom;

		Map<String, List<String>> attributes = new TreeMap<String, List<String>>();
	}
}
