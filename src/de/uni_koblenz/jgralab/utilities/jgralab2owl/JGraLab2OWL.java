/*
 * JGraLab - The Java graph laboratory
 * (c) 2006-2008 Institute for Software Technology
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

package de.uni_koblenz.jgralab.utilities.jgralab2owl;

import java.io.BufferedOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.DocumentType;
import org.w3c.dom.Element;

import de.uni_koblenz.jgralab.Graph;
import de.uni_koblenz.jgralab.GraphIO;
import de.uni_koblenz.jgralab.GraphIOException;
import de.uni_koblenz.jgralab.ProgressFunction;
import de.uni_koblenz.jgralab.impl.ProgressFunctionImpl;
import de.uni_koblenz.jgralab.schema.Schema;

public class JGraLab2OWL {

	/**
	 * The Stream used to output the OWL-file.
	 */
	private DataOutputStream out;

	/**
	 * Represents the root of the DOM-tree.
	 */
	private Document doc;

	/**
	 * The direct child element of the root node
	 */
	private Element rdfElem;

	/**
	 * Initiates the conversion of a schema ({@code schema}) to OWL and saves
	 * the output in a file with name {@code filename}.
	 * 
	 * @param filename
	 *            The name of the OWL-file to be created.
	 * @param schema
	 *            The schema which shall be converted to OWL.
	 * @param edgeClasses2Properties
	 *            If {@code true}, an EdgeClass is converted to exactly one
	 *            property, discarding possible attributes. If {@code false}, an
	 *            EdgeClass is converted to an OWL class and two Properties.
	 * @param appendSuffix2EdgeClassName
	 *            If {@code true}, the suffix {@code EdgeClass} is appended to
	 *            each OWL construct representing an EdgeClass.
	 * @throws IOException
	 * 
	 * @see #initializeDocument()
	 * @see #createOntologyHeader(Schema schema)
	 * @see #createOwlFile(Document doc)
	 */
	public static void saveSchemaToOWL(String filename, Schema schema,
			boolean edgeClasses2Properties, boolean appendSuffix2EdgeClassName)
			throws IOException {
		JGraLab2OWL j2o = new JGraLab2OWL(filename);

		j2o.initializeDocument();
		j2o.createOntologyHeader(schema);

		new Schema2OWL(j2o.doc, schema, edgeClasses2Properties,
				appendSuffix2EdgeClassName);

		j2o.createOwlFile(j2o.doc);
		j2o.out.flush();
		j2o.out.close();
	}

	/**
	 * Initiates the conversion of a graph ({@code graph}) to OWL instances and
	 * saves the output in a file with name {@code filename}. The graph's schema
	 * can also be converted and written to the file.
	 * 
	 * @param filename
	 *            The name of the OWL-file to be created.
	 * @param graph
	 *            The graph which shall be converted to OWL instances.
	 * @param edgeClasses2Properties
	 *            If {@code true}, an EdgeClass is converted to exactly one
	 *            property, discarding possible attributes and rolenames. If
	 *            {@code false}, an EdgeClass is converted to an OWL class and
	 *            two Properties.
	 * @param appendSuffix2EdgeClassName
	 *            If {@code true}, the suffix {@code EdgeClass} is appended to
	 *            each OWL construct representing an EdgeClass.
	 * @param convertSchema
	 *            If {@code true}, the graph's schema is also converted and
	 *            written to the file. If {@code false}, the schema is not
	 *            converted.
	 * @throws IOException
	 * 
	 * @see #initializeDocument()
	 * @see #createOntologyHeader(Schema schema)
	 * @see #createOwlFile(Document doc)
	 */
	public static void saveGraphToOWLInstances(String filename, Graph graph,
			boolean edgeClasses2Properties, boolean appendSuffix2EdgeClassName,
			boolean convertSchema, ProgressFunction pf) throws IOException {
		JGraLab2OWL j2o = new JGraLab2OWL(filename);

		j2o.initializeDocument();
		j2o.createOntologyHeader(graph.getSchema());

		if (convertSchema) {
			new Schema2OWL(j2o.doc, graph.getSchema(), edgeClasses2Properties,
					appendSuffix2EdgeClassName);
		}
		new Graph2OWLInstances(j2o.doc, graph, edgeClasses2Properties,
				appendSuffix2EdgeClassName, pf);

		j2o.createOwlFile(j2o.doc);
		j2o.out.flush();
		j2o.out.close();
	}

	/**
	 * Initiates the conversion of one or more graphs ({@code graph}) to OWL
	 * concepts and saves the output in a file with name {@code filename}. It is
	 * assumed that the graphs' schemas are identical. The schema can also be
	 * converted and written to the file.
	 * 
	 * @param filename
	 *            The name of the OWL-file to be created.
	 * @param graphs
	 *            The graphs which shall be converted to OWL concepts.
	 * @param schema
	 *            The graphs' common schema.
	 * @param edgeClasses2Properties
	 *            If {@code true}, an EdgeClass is converted to exactly one
	 *            property, discarding possible attributes and rolenames. If
	 *            {@code false}, an EdgeClass is converted to an OWL class and
	 *            two Properties.
	 * @param appendSuffix2EdgeClassName
	 *            If {@code true}, the suffix {@code EdgeClass} is appended to
	 *            each OWL construct representing an EdgeClass.
	 * @param convertSchema
	 *            If {@code true}, the graphs' schema is also converted and
	 *            written to the file. If {@code false}, the schema is not
	 *            converted.
	 * @throws IOException
	 * 
	 * @see #initializeDocument()
	 * @see #createOntologyHeader(Schema schema)
	 * @see #createOwlFile(Document doc)
	 */
	public static void saveGraphsToOWLConcepts(String filename, Schema schema,
			Graph[] graphs, boolean edgeClasses2Properties,
			boolean appendSuffix2EdgeClassName, boolean convertSchema,
			ProgressFunction pf) throws IOException {
		JGraLab2OWL j2o = new JGraLab2OWL(filename);

		j2o.initializeDocument();
		// We assume all graphs have the same schema!
		j2o.createOntologyHeader(schema);

		if (convertSchema) {
			new Schema2OWL(j2o.doc, schema, edgeClasses2Properties,
					appendSuffix2EdgeClassName);
		}

		for (int i = 0; i < graphs.length; i++) {
			Graph graph = graphs[i];
			new Graph2OWLConcepts(j2o.doc, graph, edgeClasses2Properties,
					appendSuffix2EdgeClassName, pf);
		}

		j2o.createOwlFile(j2o.doc);
		j2o.out.flush();
		j2o.out.close();
	}

	/**
	 * Initiates the conversion of one or more graphs ({@code graph}) to OWL
	 * concepts and exactly one graph to OWL instances. The output is saved in a
	 * file with name {@code filename}. It is assumed that the graphs' schemas
	 * are identical. The schema can also be converted and written to the file.
	 * 
	 * @param filename
	 *            The name of the OWL-file to be created.
	 * @param schemaFilename
	 *            The name of the TG file containing the graphs' common schema.
	 * @param conceptGraphs
	 *            The graphs which shall be converted to OWL concepts.
	 * @param instanceGraph
	 *            The graph which shall be converted to OWL instances.
	 * @param edgeClasses2Properties
	 *            If {@code true}, an EdgeClass is converted to exactly one
	 *            property, discarding possible attributes and rolenames. If
	 *            {@code false}, an EdgeClass is converted to an OWL class and
	 *            two Properties.
	 * @param appendSuffix2EdgeClassName
	 *            If {@code true}, the suffix {@code EdgeClass} is appended to
	 *            each OWL construct representing an EdgeClass.
	 * @param convertSchema
	 *            If {@code true}, the graphs' schema is also converted and
	 *            written to the file. If {@code false}, the schema is not
	 *            converted.
	 * @throws IOException
	 * 
	 * @see #initializeDocument()
	 * @see #createOntologyHeader(Schema schema)
	 * @see #createOwlFile(Document doc)
	 */
	public static void saveGraphsToOWLConceptsAndGraphToOWLInstances(
			String filename, Schema schema, Graph[] conceptGraphs,
			Graph instanceGraph, boolean edgeClasses2Properties,
			boolean appendSuffix2EdgeClassName, boolean convertSchema,
			ProgressFunction pf) throws IOException {
		JGraLab2OWL j2o = new JGraLab2OWL(filename);

		j2o.initializeDocument();
		// We assume all graphs have the same schema!
		j2o.createOntologyHeader(conceptGraphs[0].getSchema());
		new Schema2OWL(j2o.doc, schema, edgeClasses2Properties,
				appendSuffix2EdgeClassName);

		// CONCEPTS
		for (int i = 0; i < conceptGraphs.length; i++) {
			Graph conceptGraph = conceptGraphs[i];
			new Graph2OWLConcepts(j2o.doc, conceptGraph, edgeClasses2Properties,
					appendSuffix2EdgeClassName, pf);
		}

		// INSTANCES
		new Graph2OWLInstances(j2o.doc, instanceGraph, edgeClasses2Properties,
				appendSuffix2EdgeClassName, pf);

		j2o.createOwlFile(j2o.doc);
		j2o.out.flush();
		j2o.out.close();
	}

	/**
	 * Initiates the conversion of a graph ({@code graph}) to OWL instances and
	 * saves the output in a file with name {@code filename}. The graph's schema
	 * is not converted.
	 * 
	 * @param filename
	 *            The name of the OWL-file to be created.
	 * @param graph
	 *            The graph which shall be converted to OWL instances.
	 * @param edgeClasses2Properties
	 *            If {@code true}, an EdgeClass is converted to exactly one
	 *            property, discarding possible attributes and rolenames. If
	 *            {@code false}, an EdgeClass is converted to an OWL class and
	 *            two Properties.
	 * @param appendSuffix2EdgeClassName
	 *            If {@code true}, the suffix {@code EdgeClass} is appended to
	 *            each OWL construct representing an EdgeClass.
	 * @throws IOException
	 * 
	 * @see #initializeDocument()
	 * @see #createOntologyHeader(Schema schema)
	 * @see #createOwlFile(Document doc)
	 * 
	 * @deprecated Use {@link saveGraphToOWLInstances} with {@code convertSchema
	 *             = false} instead.
	 */
	@Deprecated
	public static void saveGraphToOWLInstancesWithoutSchema(String filename,
			Graph graph, boolean edgeClasses2Properties,
			boolean appendSuffix2EdgeClassName, ProgressFunction pf)
			throws IOException {
		JGraLab2OWL j2o = new JGraLab2OWL(filename);

		j2o.initializeDocument();
		j2o.createOntologyHeader(graph.getSchema());

		new Graph2OWLInstances(j2o.doc, graph, edgeClasses2Properties,
				appendSuffix2EdgeClassName, pf);

		j2o.createOwlFile(j2o.doc);
		j2o.out.flush();
		j2o.out.close();
	}

	/**
	 * Initiates the conversion of a graph ({@code graph}) to OWL concepts and
	 * saves the output in a file with name {@code filename}. The graph's schema
	 * is also converted and written to the file.
	 * 
	 * @param filename
	 *            The name of the OWL-file to be created.
	 * @param graph
	 *            The graph which shall be converted to OWL concepts.
	 * @param edgeClasses2Properties
	 *            If {@code true}, an EdgeClass is converted to exactly one
	 *            property, discarding possible attributes and rolenames. If
	 *            {@code false}, an EdgeClass is converted to an OWL class and
	 *            two Properties.
	 * @param appendSuffix2EdgeClassName
	 *            If {@code true}, the suffix {@code EdgeClass} is appended to
	 *            each OWL construct representing an EdgeClass.
	 * @throws IOException
	 * 
	 * @see #initializeDocument()
	 * @see #createOntologyHeader(Schema schema)
	 * @see #createOwlFile(Document doc)
	 * 
	 * @deprecated Use {@link saveGraphsToOWLConcepts(String, Graph[], Schema,
	 *             boolean, boolean, boolean, ProgressFunction)} with {@code
	 *             convertSchema = true} instead.
	 */
	@Deprecated
	public static void saveGraphToOWLConcepts(String filename, Graph graph,
			boolean edgeClasses2Properties, boolean appendSuffix2EdgeClassName,
			ProgressFunction pf) throws IOException {
		saveGraphsToOWLConcepts(filename, graph.getSchema(),
				new Graph[] { graph }, edgeClasses2Properties,
				appendSuffix2EdgeClassName, true, pf);
	}

	/**
	 * Initiates the conversion of one or more graphs ({@code graph}) to OWL
	 * concepts and saves the output in a file with name {@code filename}. It is
	 * assumed that the graphs' schemas are identical. The schema can also be
	 * converted and written to the file.
	 * 
	 * @param filename
	 *            The name of the OWL-file to be created.
	 * @param schemaFilename
	 *            The name of the TG file containing the graphs' common schema.
	 * @param graphs
	 *            The graphs which shall be converted to OWL concepts.
	 * @param edgeClasses2Properties
	 *            If {@code true}, an EdgeClass is converted to exactly one
	 *            property, discarding possible attributes and rolenames. If
	 *            {@code false}, an EdgeClass is converted to an OWL class and
	 *            two Properties.
	 * @param appendSuffix2EdgeClassName
	 *            If {@code true}, the suffix {@code EdgeClass} is appended to
	 *            each OWL construct representing an EdgeClass.
	 * @throws IOException
	 * 
	 * @see #initializeDocument()
	 * @see #createOntologyHeader(Schema schema)
	 * @see #createOwlFile(Document doc)
	 * 
	 * @deprecated Use {@link saveGraphsToOWLConcepts(String, Graph[], Schema,
	 *             boolean, boolean, boolean, ProgressFunction)} with {@code
	 *             convertSchema = true} instead.
	 */
	@Deprecated
	public static void saveGraphsToOWLConcepts(String filename,
			String schemaFilename, Graph[] graphs,
			boolean edgeClasses2Properties, boolean appendSuffix2EdgeClassName,
			ProgressFunction pf) throws IOException {
		Schema schema = null;

		try {
			schema = GraphIO.loadSchemaFromFile(schemaFilename);
		} catch (GraphIOException e) {
			e.printStackTrace();
		}

		saveGraphsToOWLConcepts(filename, schema, graphs,
				edgeClasses2Properties, appendSuffix2EdgeClassName, true, pf);
	}

	/**
	 * Initiates the conversion of one or more graphs ({@code graph}) to OWL
	 * concepts and exactly one graph to OWL instances. The output is saved in a
	 * file with name {@code filename}. It is assumed that the graphs' schemas
	 * are identical. The schema can also be converted and written to the file.
	 * 
	 * @param filename
	 *            The name of the OWL-file to be created.
	 * @param schemaFilename
	 *            The name of the TG file containing the graphs' common schema.
	 * @param conceptGraphs
	 *            The graphs which shall be converted to OWL concepts.
	 * @param instanceGraph
	 *            The graph which shall be converted to OWL instances.
	 * @param edgeClasses2Properties
	 *            If {@code true}, an EdgeClass is converted to exactly one
	 *            property, discarding possible attributes and rolenames. If
	 *            {@code false}, an EdgeClass is converted to an OWL class and
	 *            two Properties.
	 * @param appendSuffix2EdgeClassName
	 *            If {@code true}, the suffix {@code EdgeClass} is appended to
	 *            each OWL construct representing an EdgeClass.
	 * @throws IOException
	 * 
	 * @see #initializeDocument()
	 * @see #createOntologyHeader(Schema schema)
	 * @see #createOwlFile(Document doc)
	 * 
	 * @deprecated Use {@link saveGraphsToOWLConceptsAndGraphToOWLInstances(
	 *             String, Schema, Graph[], Graph, boolean, boolean, boolean,
	 *             ProgressFunction)} with {@code convertSchema = true} instead.
	 */
	@Deprecated
	public static void saveGraphsToOWLConceptsAndGraphToOWLInstances(
			String filename, String schemaFilename, Graph[] conceptGraphs,
			Graph instanceGraph, boolean edgeClasses2Properties,
			boolean appendSuffix2EdgeClassName, ProgressFunction pf)
			throws IOException {
		Schema schema = null;

		try {
			schema = GraphIO.loadSchemaFromFile(schemaFilename);
		} catch (GraphIOException e) {
			e.printStackTrace();
		}

		saveGraphsToOWLConceptsAndGraphToOWLInstances(filename, schema,
				conceptGraphs, instanceGraph, edgeClasses2Properties,
				appendSuffix2EdgeClassName, true, pf);
	}

	public static void main(String[] args) {
		if (args.length == 0)
			System.out.println("Usage: JGraLab2OWL tgFile");
		else {
			String filename = args[0];
			try {
				Graph graph = GraphIO.loadGraphFromFile(args[0], null);
				// Schema schema = graph.getSchema();
				// saveSchemaToOWL(filename + "Schema.owl", schema, true,
				// false);
				saveGraphToOWLInstances(filename + ".owl", graph, false, true,
						true, new ProgressFunctionImpl());
			} catch (Exception ex) {
				System.out.println("Sorry, something went wrong");
				ex.printStackTrace();
			}
		}
	}

	/**
	 * Creates an instance of {@code JGraLab2OWL} and opens the file specified
	 * by {@code filename}.
	 * 
	 * @param filename
	 *            The name of the OWL-file to be created.
	 */
	public JGraLab2OWL(String filename) {
		try {
			out = new DataOutputStream(new BufferedOutputStream(
					new FileOutputStream(new File(filename))));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Creates the root of the DOM-tree and stores it in member variable {@code
	 * doc}.
	 * 
	 */
	private void initializeDocument() {
		try {
			DocumentBuilderFactory factory = DocumentBuilderFactory
					.newInstance();
			DocumentBuilder builder = factory.newDocumentBuilder();

			doc = builder.newDocument();
		} catch (ParserConfigurationException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Creates the header of the OWL-file, i.e. the element {@code <rdf:RDF>},
	 * together with namespace definitions as its attributes, and its first
	 * child element {@code <owl:Ontology>} with a subelement {@code
	 * <rdfs:label>}. {@code <rdfs:label>} contains a text node representing the
	 * name of the ontology, which is equal to the schema's name.<br>
	 * <br> {@code <rdf:RDF>} is the only child of the root node. Its children are
	 * the {@code <owl:Ontology>} element and further elements which build the
	 * ontology.<br>
	 * <br>
	 * XML-code written:<br>
	 * 
	 * <pre>
	 * &lt;rdf:RDF
	 *         xmlns      = #,
	 *         xmlns:owl  = http://www.w3.org/2002/07/owl#,
	 *         xmlns:rdf  = http://www.w3.org/1999/02/22-rdf-syntax-ns#,
	 *         xmlns:rdfs = http://www.w3.org/2000/01/rdf-schema#,
	 *         xmlns:xsd  = http://www.w3.org/2001/XMLSchema#&gt;
	 *         
	 *     &lt;owl:Ontology rdf:about = &quot;&quot;&gt;
	 *         &lt;rdfs:label&gt;&lt;i&gt;schema.getName()&lt;/i&gt;&lt;/rdfs:label&gt;
	 *     &lt;/owl:Ontology&gt;
	 *     &lt;i&gt;...
	 *     Ontology definition
	 *     ...&lt;/i&gt;
	 * &lt;/rdf:RDF&gt;
	 * </pre>
	 * 
	 * @param schema
	 *            The schema whose name shall be used for naming the ontology.
	 */
	private void createOntologyHeader(Schema schema) {
		Element ontologyElem;
		Element labelElem;
		// create Elements and set Attributes
		rdfElem = doc.createElement("rdf:RDF");
		rdfElem.setAttribute("xmlns", "http://" + schema.getQualifiedName()
				+ "#");
		rdfElem.setAttribute("xml:base", "http://" + schema.getQualifiedName()
				+ "#");
		rdfElem.setAttribute("xmlns:owl", "http://www.w3.org/2002/07/owl#");
		rdfElem.setAttribute("xmlns:rdf",
				"http://www.w3.org/1999/02/22-rdf-syntax-ns#");
		rdfElem.setAttribute("xmlns:rdfs",
				"http://www.w3.org/2000/01/rdf-schema#");
		rdfElem.setAttribute("xmlns:xsd", "http://www.w3.org/2001/XMLSchema#");
		doc.appendChild(rdfElem);

		ontologyElem = doc.createElement("owl:Ontology");
		ontologyElem.setAttribute("rdf:about", "");
		labelElem = doc.createElement("rdfs:label");

		// build subtree
		labelElem.appendChild(doc.createTextNode(schema.getQualifiedName()));
		rdfElem.appendChild(ontologyElem);
		ontologyElem.appendChild(labelElem);
	}

	/**
	 * Transforms the DOM-tree represented by its root ({@code document}) to an
	 * OWL-file.
	 * 
	 * @param doc
	 *            The root of the DOM-tree which shall be transformed to an
	 *            OWL-file.
	 */
	private void createOwlFile(Document doc) {
		try {
			Transformer transformer = TransformerFactory.newInstance()
					.newTransformer();

			DocumentType docType = this.doc.getDoctype();

			if (docType != null) {
				transformer.setOutputProperty(OutputKeys.DOCTYPE_PUBLIC,
						docType.getPublicId());
				transformer.setOutputProperty(OutputKeys.DOCTYPE_SYSTEM,
						docType.getSystemId());
			}

			DOMSource source = new DOMSource(doc);
			StreamResult result = new StreamResult(out);

			transformer.setOutputProperty(OutputKeys.INDENT, "yes");
			transformer.setOutputProperty(
					"{http://xml.apache.org/xslt}indent-amount", "4");
			transformer.transform(source, result);
		} catch (TransformerConfigurationException tce) {
			tce.printStackTrace();
		} catch (TransformerException te) {
			te.printStackTrace();
		}
	}
}
