/*
 * JGraLab - The Java graph laboratory
 * (c) 2006-2010 Institute for Software Technology
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
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;

import de.uni_koblenz.ist.utilities.option_handler.OptionHandler;
import de.uni_koblenz.jgralab.Graph;
import de.uni_koblenz.jgralab.GraphIO;
import de.uni_koblenz.jgralab.JGraLab;
import de.uni_koblenz.jgralab.ProgressFunction;
import de.uni_koblenz.jgralab.impl.ConsoleProgressFunction;
import de.uni_koblenz.jgralab.schema.Schema;

public class JGraLab2OWL {

	protected final static String rdfNS = "http://www.w3.org/1999/02/22-rdf-syntax-ns#";

	protected final static String rdfsNS = "http://www.w3.org/2000/01/rdf-schema#";

	protected final static String owlNS = "http://www.w3.org/2002/07/owl#";

	protected final static String xsdNS = "http://www.w3.org/2001/XMLSchema#";

	protected static String defaultNS;

	private OutputStream outputStream;

	/**
	 * The XMLStreamWriter used to output the OWL-file.
	 */
	private XMLStreamWriter writer;

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
	 * @see #createOntologyHeader(Schema schema)
	 */
	public static void saveSchemaToOWL(String filename, Schema schema,
			boolean edgeClasses2Properties, boolean appendSuffix2EdgeClassName) {
		JGraLab2OWL j2o = null;
		try {
			j2o = new JGraLab2OWL(filename, schema);

			j2o.createOntologyHeader(schema);

			Schema2OWL s2o = new Schema2OWL(j2o.writer, edgeClasses2Properties,
					appendSuffix2EdgeClassName);
			s2o.saveSchema(schema);
		} catch (XMLStreamException e) {
			e.printStackTrace();
		} finally {
			if (j2o != null) {
				j2o.finalizeDocument();
			}
		}
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
	 * @see #createOntologyHeader(Schema schema)
	 */
	public static void saveGraphToOWLInstances(String filename, Graph graph,
			boolean edgeClasses2Properties, boolean appendSuffix2EdgeClassName,
			boolean convertSchema, ProgressFunction pf) throws IOException {
		JGraLab2OWL j2o = null;
		try {
			Schema schema = graph.getSchema();
			j2o = new JGraLab2OWL(filename, schema);

			j2o.createOntologyHeader(schema);

			if (convertSchema) {
				Schema2OWL s2o = new Schema2OWL(j2o.writer,
						edgeClasses2Properties, appendSuffix2EdgeClassName);

				s2o.saveSchema(schema);
			}
			Graph2OWLInstances g2oi = new Graph2OWLInstances(j2o.writer,
					edgeClasses2Properties, appendSuffix2EdgeClassName, pf);
			g2oi.saveGraph(graph, pf);
		} catch (XMLStreamException e) {
			e.printStackTrace();
		} finally {
			if (j2o != null) {
				j2o.finalizeDocument();
			}
		}
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
	 * @see #createOntologyHeader(Schema schema)
	 */
	public static void saveGraphsToOWLConcepts(String filename, Schema schema,
			Graph[] graphs, boolean edgeClasses2Properties,
			boolean appendSuffix2EdgeClassName, boolean convertSchema,
			ProgressFunction pf) throws IOException {
		JGraLab2OWL j2o = null;
		try {
			j2o = new JGraLab2OWL(filename, schema);

			// We assume all graphs have the same schema!
			j2o.createOntologyHeader(schema);

			if (convertSchema) {
				Schema2OWL s2o = new Schema2OWL(j2o.writer,
						edgeClasses2Properties, appendSuffix2EdgeClassName);
				s2o.saveSchema(schema);
			}

			Graph2OWLConcepts g2oc = new Graph2OWLConcepts(j2o.writer,
					edgeClasses2Properties, appendSuffix2EdgeClassName, pf);

			for (int i = 0; i < graphs.length; i++) {
				Graph graph = graphs[i];
				g2oc.saveGraph(graph, pf);
			}

		} catch (XMLStreamException e) {
			e.printStackTrace();
		} finally {
			if (j2o != null) {
				j2o.finalizeDocument();
			}
		}
	}

	/**
	 * Initiates the conversion of one or more graphs ({@code graph}) to OWL
	 * concepts and exactly one graph to OWL instances. The output is saved in a
	 * file with name {@code filename}. It is assumed that the graphs' schemas
	 * are identical. The schema can also be converted and written to the file.
	 * 
	 * @param filename
	 *            The name of the OWL-file to be created.
	 * @param schema
	 *            the common schema for all concept graphs
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
	 * @param pf
	 *            a progress function (may be null)
	 * @throws IOException
	 * 
	 * @see #createOntologyHeader(Schema schema)
	 */
	public static void saveGraphsToOWLConceptsAndGraphToOWLInstances(
			String filename, Schema schema, Graph[] conceptGraphs,
			Graph instanceGraph, boolean edgeClasses2Properties,
			boolean appendSuffix2EdgeClassName, boolean convertSchema,
			ProgressFunction pf) throws IOException {
		JGraLab2OWL j2o = null;
		try {
			j2o = new JGraLab2OWL(filename, schema);

			// We assume all graphs have the same schema!
			j2o.createOntologyHeader(conceptGraphs[0].getSchema());

			Schema2OWL s2o = new Schema2OWL(j2o.writer, edgeClasses2Properties,
					appendSuffix2EdgeClassName);
			s2o.saveSchema(schema);

			// CONCEPTS
			Graph2OWLConcepts g2oc = new Graph2OWLConcepts(j2o.writer,
					edgeClasses2Properties, appendSuffix2EdgeClassName, pf);
			for (int i = 0; i < conceptGraphs.length; i++) {
				Graph conceptGraph = conceptGraphs[i];
				g2oc.saveGraph(conceptGraph, pf);
			}

			// INSTANCES
			Graph2OWLInstances g2oi = new Graph2OWLInstances(j2o.writer,
					edgeClasses2Properties, appendSuffix2EdgeClassName, pf);
			g2oi.saveGraph(instanceGraph, pf);

		} catch (XMLStreamException e) {
			e.printStackTrace();
		} finally {
			if (j2o != null) {
				j2o.finalizeDocument();
			}
		}
	}

	public static void main(String[] args) {
		CommandLine comLine = processCommandLineOptions(args);
		assert comLine != null;

		// if (args.length == 0)
		// System.out.println("Usage: JGraLab2OWL tgFile");
		// else {
		// String filename = args[0];
		String filename = comLine.getOptionValue("g");
		try {
			Graph graph = GraphIO.loadGraphFromFileWithStandardSupport(
					comLine.getOptionValue("g")/* args[0] */, null);

			saveGraphToOWLInstances(filename + ".owl", graph, false, true,
					true, new ConsoleProgressFunction());
		} catch (Exception ex) {
			System.out.println("Sorry, something went wrong");
			ex.printStackTrace();
		}
		// }
	}

	private static CommandLine processCommandLineOptions(String[] args) {
		String toolString = "java " + JGraLab2OWL.class.getName();
		String versionString = JGraLab.getInfo(false);
		OptionHandler oh = new OptionHandler(toolString, versionString);

		Option graph = new Option("g", "graph", true,
				"(required): TG-file of the graph");
		graph.setRequired(true);
		graph.setArgName("file");
		oh.addOption(graph);

		return oh.parse(args);
	}

	/**
	 * Creates an instance of {@code JGraLab2OWL} and opens the file specified
	 * by {@code filename}.
	 * 
	 * @param filename
	 *            The name of the OWL-file to be created.
	 */
	public JGraLab2OWL(String filename, Schema schema)
			throws XMLStreamException {
		defaultNS = "http://" + schema.getQualifiedName() + "#";

		try {
			outputStream = new BufferedOutputStream(new FileOutputStream(
					filename));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}

		XMLOutputFactory factory = XMLOutputFactory.newInstance();
		writer = new de.uni_koblenz.ist.utilities.xml.IndentingXMLStreamWriter(
				factory.createXMLStreamWriter(outputStream, "UTF-8"), "\t");

		writer.writeStartDocument("UTF-8", "1.0");
	}

	/**
	 * Creates the header of the OWL-file, i.e. the element {@code <rdf:RDF>},
	 * together with namespace definitions as its attributes, and its first
	 * child element {@code <owl:Ontology>} with a subelement
	 * {@code <rdfs:label>}. {@code <rdfs:label>} contains a text node
	 * representing the name of the ontology, which is equal to the schema's
	 * name.<br>
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
	private void createOntologyHeader(Schema schema) throws XMLStreamException {
		writer.setPrefix("rdf", rdfNS);

		writer.writeStartElement(rdfNS, "RDF");
		writer.writeDefaultNamespace(defaultNS);
		writer.writeNamespace("owl", owlNS);
		writer.writeNamespace("rdf", rdfNS);
		writer.writeNamespace("rdfs", rdfsNS);
		writer.writeNamespace("xsd", xsdNS);
		writer.writeAttribute("xml:base", defaultNS);

		writer.writeStartElement(owlNS, "Ontology");
		writer.writeAttribute(rdfNS, "about", "");
		writer.writeStartElement(rdfsNS, "label");
		writer.writeCharacters(schema.getQualifiedName());

		writer.writeEndElement();
		writer.writeEndElement();
	}

	/**
	 * Writes &lt;rdf:RDF&rt;, flushes and closes Writer and underlying Stream.
	 * 
	 * @throws XMLStreamException
	 */
	private void finalizeDocument() {
		try {
			writer.writeEndDocument();
			writer.flush();
		} catch (XMLStreamException ex) {

		} finally {
			try {
				writer.close();
			} catch (XMLStreamException ex) {
				ex.printStackTrace();
			} finally {
				try {
					outputStream.close();
				} catch (IOException ex) {
					ex.printStackTrace();
				}
			}
		}
	}
}
