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
import java.util.logging.Logger;

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
	 * Initiates the conversion of a graph ({@code graph}) to OWL and saves the
	 * output in a file with name {@code filename}. The graph's schema is also
	 * converted and the output written to the {@code filename}.
	 * 
	 * @param filename
	 *            The name of the OWL-file to be created.
	 * @param graph
	 *            The graph which shall be converted to OWL.
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
	 */
	public static void saveGraphToOWL(String filename, Graph graph,
			boolean edgeClasses2Properties, boolean appendSuffix2EdgeClassName,
			ProgressFunction pf) throws IOException {
		JGraLab2OWL j2o = new JGraLab2OWL(filename);

		j2o.initializeDocument();
		j2o.createOntologyHeader(graph.getSchema());

		new Schema2OWL(j2o.doc, graph.getSchema(), edgeClasses2Properties,
				appendSuffix2EdgeClassName);
		new Graph2OWL(j2o.doc, graph, edgeClasses2Properties,
				appendSuffix2EdgeClassName, pf);

		j2o.createOwlFile(j2o.doc);
		j2o.out.flush();
		j2o.out.close();
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
				saveGraphToOWL(filename + ".owl", graph, false, true,
						new ProgressFunctionImpl());
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
