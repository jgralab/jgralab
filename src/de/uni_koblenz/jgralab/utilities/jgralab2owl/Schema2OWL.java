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

package de.uni_koblenz.jgralab.utilities.jgralab2owl;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import de.uni_koblenz.jgralab.schema.Attribute;
import de.uni_koblenz.jgralab.schema.AttributedElementClass;
import de.uni_koblenz.jgralab.schema.CompositeDomain;
import de.uni_koblenz.jgralab.schema.EdgeClass;
import de.uni_koblenz.jgralab.schema.EnumDomain;
import de.uni_koblenz.jgralab.schema.GraphClass;
import de.uni_koblenz.jgralab.schema.GraphElementClass;
import de.uni_koblenz.jgralab.schema.RecordDomain;
import de.uni_koblenz.jgralab.schema.Schema;
import de.uni_koblenz.jgralab.schema.VertexClass;
import de.uni_koblenz.jgralab.schema.RecordDomain.RecordComponent;

class Schema2OWL {

	/**
	 * The names of the default {@code GraphElementClass}es except {@code
	 * Aggregation} and {@code Composition}.
	 */
	private final String[] defaultGECs = { "Vertex", "Edge" };

	/**
	 * the {@link XMLStreamWriter} used to write the OWL document
	 */
	private XMLStreamWriter writer;

	/**
	 * If {@code true}, an EdgeClass is converted to exactly one property,
	 * discarding possible attributes and rolenames. If {@code false}, an
	 * EdgeClass is converted to an OWL class and two Properties.
	 */
	private boolean edgeClasses2Properties;

	/**
	 * The suffix appended to the OWL construct representing an EdgeClass. It is
	 * an empty string if the parameter {@code appendSuffix2EdgeClassName} given
	 * to the constructor is {@code false}.
	 * 
	 * @see #Schema2OWL(Document doc, Schema schema, boolean
	 *      edgeClasses2Properties, boolean appendSuffix2EdgeClassName)
	 */
	private String edgeClassNameSuffix;

	/**
	 * Creates an instance of {@code Schema2OWL} and assigns values to the
	 * member variables.
	 * 
	 * @param writer
	 *            the {@link XMLStreamWriter} used to write the OWL document
	 * @param edgeClasses2Properties
	 *            If {@code true}, an EdgeClass is converted to exactly one
	 *            property, discarding possible attributes and rolenames. If
	 *            {@code false}, an EdgeClass is converted to an OWL class and
	 *            two Properties.
	 * @param appendSuffix2EdgeClassName
	 *            If {@code true}, the suffix {@code EC} is appended to each OWL
	 *            construct representing an EdgeClass.
	 */
	Schema2OWL(XMLStreamWriter writer, boolean edgeClasses2Properties,
			boolean appendSuffix2EdgeClassName) {
		this.writer = writer;
		this.edgeClasses2Properties = edgeClasses2Properties;
		if (appendSuffix2EdgeClassName) {
			edgeClassNameSuffix = "EC";
		} else {
			edgeClassNameSuffix = "";
		}
	}

	/**
	 * Converts a schema ({@code schema}) to a DOM-tree consisting of
	 * OWL-Elements and then writes the tree as XML-output into a file.
	 * 
	 * @param schema
	 *            The schema which shall be converted to OWL.
	 * @throws XMLStreamException
	 * 
	 * @see #convertEnumDomains(Schema schema)
	 * @see #convertCompositeDomains(Schema schema)
	 * @see #convertGraphClasses(Schema schema)
	 */
	protected void saveSchema(Schema schema) throws XMLStreamException {
		convertEnumDomains(schema);
		convertCompositeDomains(schema);
		convertGraphClasses(schema);
	}

	/**
	 * Converts all {@code EnumDomain}s in a schema to an Enumerated Class of
	 * the corresponding ontology.<br>
	 * <br>
	 * XML-code written for one {@code EnumDomain ed}:<br>
	 * 
	 * <pre>
	 *     &lt;owl:Class rdf:ID=&quot;&lt;i&gt;ed.getName()&lt;/i&gt;&quot;&gt;
	 *         &lt;owl:oneOf rdf:parseType=&quot;Collection&quot;&gt;
	 *             &lt;owl:Thing rdf:about=&quot;#&lt;i&gt;ed.getConsts.get(0)&lt;/i&gt;&quot;/&gt;
	 *             &lt;owl:Thing rdf:about=&quot;#&lt;i&gt;ed.getConsts.get(1)&lt;/i&gt;&quot;/&gt;
	 *             &lt;i&gt;...&lt;/i&gt;
	 *         &lt;/owl:oneOf&gt;
	 *     &lt;/owl:Class&gt;
	 * </pre>
	 * 
	 * @param schema
	 *            The schema whose {@code EnumDomain}s shall be converted.
	 * @throws XMLStreamException
	 */
	private void convertEnumDomains(Schema schema) throws XMLStreamException {
		for (EnumDomain enumDomain : schema.getEnumDomains()) {
			writeOwlClassStartElement(enumDomain.getQualifiedName());
			writer.writeStartElement(JGraLab2OWL.owlNS, "oneOf");
			writer.writeAttribute(JGraLab2OWL.rdfNS, "parseType", "Collection");

			// convert Enum constants
			for (String enumConst : enumDomain.getConsts()) {
				writer.writeEmptyElement(JGraLab2OWL.owlNS, "Thing");
				writer.writeAttribute(JGraLab2OWL.rdfNS, "about", "#"
						+ enumConst);
			}

			writer.writeEndElement();
			writer.writeEndElement();
		}
	}

	/**
	 * Converts all {@code CompositeDomain}s in a schema to constructs of the
	 * corresponding ontology.<br>
	 * <br>
	 * The written XML representation depends on the type of the {@code
	 * CompositeDomain}.
	 * 
	 * @param schema
	 *            The schema whose {@code CompositeDomain}s shall be converted.
	 * @throws XMLStreamException
	 * 
	 * @see #convertSetDomain()
	 * @see #convertListDomain()
	 * @see #convertRecordDomain(RecordDomain rd)
	 */
	private void convertCompositeDomains(Schema schema)
			throws XMLStreamException {
		boolean setCreated = false;
		boolean listCreated = false;

		for (CompositeDomain compositeDomain : schema
				.getCompositeDomainsInTopologicalOrder()) {
			if (!setCreated
					&& compositeDomain.getTGTypeName(null).startsWith("Set<")) {
				convertSetDomain();
				setCreated = true;
			}
			if (!listCreated
					&& compositeDomain.getTGTypeName(null).startsWith("List<")) {
				convertListDomain();
				listCreated = true;
			}
			if (compositeDomain.toString().startsWith("Record")) {
				convertRecordDomain((RecordDomain) compositeDomain);
			}
		}
	}

	/**
	 * Creates an OWL Class with the ID {@code Set} and two OWL Properties (
	 * {@code setContainsDatatype} and {@code setContainsObject}) in order to
	 * represent Sets. The Properties relate individuals of {@code Set} to
	 * contained individuals.<br>
	 * <br>
	 * XML-code written:<br>
	 * 
	 * <pre>
	 *     &lt;owl:Class rdf:ID=&quot;Set&quot;/&gt;
	 * 
	 * 
	 *     &lt;owl:DatatypeProperty rdf:ID=&quot;setHasDatatype&quot;&gt;
	 *         &lt;rdfs:domain rdf:resource=&quot;#Set&quot;/&gt;
	 *         &lt;rdfs:range rdf:resource=&quot;http://www.w3.org/2000/01/rdf-schema#datatype&quot;/&gt;
	 *     &lt;/owl:DatatypeProperty&gt;
	 * 
	 *     &lt;owl:ObjectProperty rdf:ID=&quot;setHasObject&quot;&gt;
	 *         &lt;rdfs:domain rdf:resource=&quot;#Set&quot;/&gt;
	 *         &lt;rdfs:range rdf:resource=&quot;http://www.w3.org/2002/07/owl#Class&quot;/&gt;
	 *     &lt;/owl:ObjectProperty&gt;
	 * </pre>
	 * 
	 * @throws XMLStreamException
	 */
	private void convertSetDomain() throws XMLStreamException {
		writeOwlClassStartElement("Set");

		writeOwlDatatypePropertyStartElement("setHasDatatype");
		writeRdfsDomainEmptyElement("#Set");
		writeRdfsRangeEmptyElement(JGraLab2OWL.rdfsNS + "Datatype");
		writer.writeEndElement();

		writeOwlObjectPropertyStartElement("setHasObject");
		writeRdfsDomainEmptyElement("#Set");
		writeRdfsRangeEmptyElement(JGraLab2OWL.owlNS + "Class");
		writer.writeEndElement();

		writer.writeEndElement();
	}

	/**
	 * Creates an OWL-Class with the ID {@code ListElement} and four OWL
	 * Properties ({@code listElementIsDatatype}, {@code listElementIsObject},
	 * {@code nextListElement}, and {@code prevListElement}) in order to
	 * represent Lists. The last two Properties serve to interconnect the
	 * {@code ListElements} in order to form a list. The first two Properties
	 * relate individuals of {@code ListElement} to individuals representing the
	 * "real" data.<br>
	 * <br>
	 * XML-code written:<br>
	 * 
	 * <pre>
	 *     &lt;owl:Class rdf:ID=&quot;ListElement&quot;&gt;
	 *         &lt;rdfs:subClassOf&gt;
	 *             &lt;owl:Restriction&gt;
	 *                 &lt;owl:onProperty rdf:resource=&quot;#listElementHasDatatype&quot;/&gt;
	 *                 &lt;owl:cardinality rdf:datatype=&quot;http://www.w3.org/2001/XMLSchema#nonNegativeInteger&quot;&gt;1&lt;/owl:cardinality&gt;
	 *             &lt;/owl:Restriction&gt;
	 *         &lt;/rdfs:subClassOf&gt;
	 *         &lt;rdfs:subClassOf&gt;
	 *             &lt;owl:Restriction&gt;
	 *                 &lt;owl:onProperty rdf:resource=&quot;#listElementHasObject&quot;/&gt;
	 *                 &lt;owl:cardinality rdf:datatype=&quot;http://www.w3.org/2001/XMLSchema#nonNegativeInteger&quot;&gt;1&lt;/owl:cardinality&gt;
	 *             &lt;/owl:Restriction&gt;
	 *         &lt;/rdfs:subClassOf&gt;
	 *     &lt;/owl:Class&gt;
	 * 
	 * 
	 *     &lt;owl:DatatypeProperty rdf:ID=&quot;listElementHasDatatype&quot;&gt;
	 *         &lt;rdfs:domain rdf:resource=&quot;#ListElement&quot;/&gt;
	 *         &lt;rdfs:range rdf:resource=&quot;http://www.w3.org/2000/01/rdf-schema#datatype&quot;/&gt;
	 *     &lt;/owl:DatatypeProperty&gt;
	 * 
	 *     &lt;owl:ObjectProperty rdf:ID=&quot;listElementHasObject&quot;&gt;
	 *       &lt;rdfs:domain rdf:resource=&quot;#ListElement&quot;/&gt;
	 *       &lt;rdfs:range rdf:resource=&quot;http://www.w3.org/2002/07/owl#Class&quot;/&gt;
	 *     &lt;/owl:ObjectProperty&gt;
	 * 
	 *     &lt;owl:ObjectProperty rdf:ID=&quot;hasNextListElement&quot;&gt;
	 *         &lt;rdf:type rdf:resource=&quot;http://www.w3.org/2002/07/owl#FunctionalProperty&quot;/&gt;
	 *         &lt;rdfs:domain rdf:resource=&quot;#ListElement&quot;/&gt;
	 *         &lt;rdfs:domain rdf:resource=&quot;#ListElement&quot;/&gt;
	 *     &lt;/owl:ObjectProperty&gt;
	 * </pre>
	 * 
	 * @throws XMLStreamException
	 */
	private void convertListDomain() throws XMLStreamException {
		// write ListElement element
		writeOwlClassStartElement("ListElement");

		writeRdfsSubClassOfStartElement();
		writeOwlRestrictionStartElement();
		writeOwlOnPropertyEmptyElement("#listElementHasDatatype");
		writeOwlCardinalityStartElement();
		writer.writeCharacters(String.valueOf(1));
		writer.writeEndElement();
		writer.writeEndElement();
		writer.writeEndElement();

		writeRdfsSubClassOfStartElement();
		writeOwlRestrictionStartElement();
		writeOwlOnPropertyEmptyElement("#listElementHasObject");
		writeOwlCardinalityStartElement();
		writer.writeCharacters(String.valueOf(1));
		writer.writeEndElement();
		writer.writeEndElement();
		writer.writeEndElement();

		writer.writeEndElement();

		// write property elements
		writeOwlDatatypePropertyStartElement("listElementHasDatatype");
		writeRdfsDomainEmptyElement("#ListElement");
		writeRdfsRangeEmptyElement(JGraLab2OWL.rdfsNS + "Datatype");
		writer.writeEndElement();

		writeOwlDatatypePropertyStartElement("listElementHasObject");
		writeRdfsDomainEmptyElement("#ListElement");
		writeRdfsRangeEmptyElement(JGraLab2OWL.owlNS + "Class");
		writer.writeEndElement();

		writeOwlDatatypePropertyStartElement("hasNextListElement");
		writeRdfTypeEmptyElement(JGraLab2OWL.owlNS + "FunctionalProperty");
		writeRdfsDomainEmptyElement("#ListElement");
		writeRdfsRangeEmptyElement("#ListElement");
		writer.writeEndElement();
	}

	/**
	 * Converts the {@code RecordDomain rd} to an OWL Class and OWL Properties.
	 * The name of the class corresponds to the name of the {@code RecordDomain}
	 * . For each component, a Property relating the Class representing the
	 * {@code RecordDomain} to the class representing the component is created.<br>
	 * <br>
	 * XML-code written for one {@code RecordDomain rd}:<br>
	 * 
	 * <pre>
	 *     &lt;owl:Class rdf:ID=&quot;&lt;i&gt;rd.getName()&lt;/i&gt;&quot;/&gt;
	 * </pre>
	 * 
	 * For each {@code component}, i.e. a {@code (Name, Domain)} pair, either an
	 * ObjectProperty or a DatatypeProperty is created, depending on the {@code
	 * component}s domain:<br>
	 * 
	 * <pre>
	 *     &lt;owl:DatatypeProperty rdf:ID=&quot;&lt;i&gt;rd.getName()&lt;/i&gt; + Has + &lt;i&gt;Name&lt;/i&gt;&quot;&gt;
	 *         &lt;rdfs:domain rdf:resource=&quot;#&lt;i&gt;rd.getName()&lt;/i&gt;&quot;/&gt;
	 *         &lt;rdfs:range rdf:resource=&quot;#&lt;i&gt;Domain&lt;/i&gt;&quot;/&gt;
	 *     &lt;/owl:DatatypeProperty&gt;
	 * 
	 *     &lt;owl:ObjectProperty rdf:ID=&quot;&lt;i&gt;rd.getName()&lt;/i&gt; + Has + &lt;i&gt;Name&lt;/i&gt;&quot;&gt;
	 *         &lt;rdfs:domain rdf:resource=&quot;#&lt;i&gt;rd.getName()&lt;/i&gt;&quot;/&gt;
	 *         &lt;rdfs:range rdf:resource=&quot;#&lt;i&gt;Domain&lt;/i&gt;&quot;/&gt;
	 *     &lt;/owl:ObjectProperty&gt;
	 * </pre>
	 * 
	 * @throws XMLStreamException
	 */
	private void convertRecordDomain(RecordDomain rd) throws XMLStreamException {
		// create Class element
		writeOwlClassEmptyElement(rd.getQualifiedName());

		// create Properties for "rd"'s components
		for (RecordComponent component : rd.getComponents()) {

			// if "component" has a CompositeDomain or an EnumDomain (no Object)
			if (component.getDomain().isComposite()
					|| component.getDomain().toString().contains("Enum")) {
				writeOwlObjectPropertyStartElement();

				writeRdfTypeEmptyElement(JGraLab2OWL.owlNS
						+ "FunctionalProperty");
				writeRdfsDomainEmptyElement("#" + rd.getQualifiedName());

				writeRdfsRangeEmptyElement();
				if (component.getDomain().getTGTypeName(null).startsWith(
						"List<")) {
					writer.writeAttribute(JGraLab2OWL.rdfNS, "resource",
							"#ListElement");
				} else if (component.getDomain().getTGTypeName(null)
						.startsWith("Set<")) {
					writer
							.writeAttribute(JGraLab2OWL.rdfNS, "resource",
									"#Set");
				} else {
					writer.writeAttribute(JGraLab2OWL.rdfNS, "resource", "#"
							+ component.getDomain().getQualifiedName());
				}
				// if "component" has a BasicDomain
			} else {
				writeOwlDatatypePropertyStartElement(HelperMethods
						.firstToLowerCase(rd.getQualifiedName())
						+ "Has"
						+ HelperMethods.firstToUpperCase(component.getName()));

				writeRdfTypeEmptyElement(JGraLab2OWL.owlNS
						+ "FunctionalProperty");
				writeRdfsDomainEmptyElement("#" + rd.getQualifiedName());

				writeRdfsRangeEmptyElement();
				if (component.getDomain().getTGTypeName(null).equals("String")) {
					writer.writeAttribute(JGraLab2OWL.rdfNS, "resource",
							JGraLab2OWL.xsdNS + "string");
				} else {
					writer
							.writeAttribute(
									JGraLab2OWL.rdfNS,
									"resource",
									JGraLab2OWL.xsdNS
											+ component
													.getDomain()
													.getJavaAttributeImplementationTypeName(
															""));
				}
			}

			writer.writeEndElement();
		}

	}

	/**
	 * Converts all {@code GraphClass}es of the given {@code Schema schema}
	 * together with their attributes, {@code VertexClass}es, and {@code
	 * EdgeClass}es to corresponding OWL constructs. Each {@code GraphClass}
	 * itself with references to its superclasses is transformed to an OWL
	 * Class. Abstract {@code GraphClass}es are represented as unions of their
	 * subclasses.<br>
	 * <br>
	 * See the description of
	 * {@link #convertAttributes(AttributedElementClass aec)} for the
	 * representation of attributes and
	 * {@link #convertVertexClasses(GraphClass gc)} and
	 * {@link #convertEdgeClasses(GraphClass gc)} for the conversion of {@code
	 * VertexClasses} and {@code EdgeClasses}.<br>
	 * <br>
	 * in addition, four Properties are created which relate a {@code Graph} to
	 * its contained {@code Vertices} or {@code Edge}s (or their subclasses) and
	 * vice versa.<br>
	 * <br>
	 * XML-code written for one {@code GraphClass gc}:<br>
	 * 
	 * <pre>
	 *     &lt;owl:Class rdf:ID=&quot;&lt;i&gt;gc.getName()&lt;/i&gt;&quot;&gt;
	 *         &lt;rdfs:subClassOf rdf:resource=&quot;#&lt;i&gt;gc.getDirectSuperClasses.toArray(new GraphClass[0])[0]&lt;/i&gt;&quot;/&gt;
	 *         &lt;rdfs:subClassOf rdf:resource=&quot;#&lt;i&gt;gc.getDirectSuperClasses.toArray(new GraphClass[0])[1]&lt;/i&gt;&quot;/&gt;
	 *         &lt;i&gt;...&lt;/i&gt;
	 * </pre>
	 * 
	 * ------------------------------------<br>
	 * This part is only written if {@code gc} is abstract and has one or more
	 * subclasses:<br>
	 * 
	 * <pre>
	 *         &lt;owl:unionOf rdf:parseType=&quot;Collection&quot;&amp;rt
	 * 	       &lt;owl:Class rdf:about=&quot;#&lt;i&gt;gc.getDirectSubClasses.toArray(new GraphClass[0])[0]&lt;/i&gt;&quot;/&amp;rt
	 *             &lt;owl:Class rdf:about=&quot;#&lt;i&gt;gc.getDirectSubClasses.toArray(new GraphClass[0])[1]&lt;/i&gt;&quot;/&amp;rt
	 * 	       &lt;i&gt;...&lt;/i&gt;
	 *        &lt;/owl:unionOf&amp;rt
	 * </pre>
	 * 
	 * This XML-code is only written once:<br>
	 * 
	 * <pre>
	 *     &lt;/owl:Class&gt;
	 * 
	 *     &lt;owl:ObjectProperty rdf:ID=&quot;vertexIsInGraph&quot;&gt;
	 *         &lt;rdf:type rdf:resource=&quot;http://www.w3.org/2002/07/owl#FunctionalProperty&quot;/&gt;
	 *         &lt;rdfs:domain rdf:resource=&quot;#Vertex&quot;/&gt;
	 *         &lt;rdfs:range rdf:resource=&quot;#Graph&quot;/&gt;
	 *     &lt;/owl:ObjectProperty&gt;
	 *     &lt;owl:ObjectProperty rdf:ID=&quot;graphContainsVertex&quot;&gt;
	 *         &lt;owl:inverseOf rdf:resource=&quot;#vertexIsInGraph&quot;/&gt;
	 *         &lt;rdfs:domain rdf:resource=&quot;#Graph&quot;/&gt;
	 *         &lt;rdfs:range rdf:resource=&quot;#Vertex&quot;/&gt;
	 *     &lt;/owl:ObjectProperty&gt;
	 * </pre>
	 * 
	 * This XML-code is only written if {@code edgeClasses2Properties = false}:<br>
	 * 
	 * <pre>
	 *     &lt;owl:ObjectProperty rdf:ID=&quot;edgeIsInGraph&quot;&gt;
	 *         &lt;rdf:type rdf:resource=&quot;http://www.w3.org/2002/07/owl#FunctionalProperty&quot;/&gt;
	 *         &lt;rdfs:domain rdf:resource=&quot;#Edge&quot;/&gt;
	 *         &lt;rdfs:range rdf:resource=&quot;#Graph&quot;/&gt;
	 *     &lt;/owl:ObjectProperty&gt;
	 *     &lt;owl:ObjectProperty rdf:ID=&quot;graphContainsEdge&quot;&gt;
	 *         &lt;owl:inverseOf rdf:resource=&quot;#edgeIsInGraph&quot;/&gt;
	 *         &lt;rdfs:domain rdf:resource=&quot;#Graph&quot;/&gt;
	 *         &lt;rdfs:range rdf:resource=&quot;#Edge&quot;/&gt;
	 *     &lt;/owl:ObjectProperty&gt;
	 * </pre>
	 * 
	 * Furthermore, this method creates two Properties {@code edgeClassFromRole}
	 * and {@code edgeClassToRole} relating an {@code Edge} to a {@code string}.
	 * They serve to assign roles to the two ends of an {@code EdgeClass}.<br>
	 * <br>
	 * This XML-code is only written once:<br>
	 * 
	 * <pre>
	 *     &lt;owl:DatatypeProperty rdf:ID=&quot;edgeOutRole&quot;&gt;
	 *         &lt;rdf:type rdf:resource=&quot;http://www.w3.org/2002/07/owl#FunctionalProperty&quot;/&gt;
	 *         &lt;rdfs:domain rdf:resource=&quot;#Edge&quot;/&gt;
	 *         &lt;rdfs:range rdf:resource=&quot;http://www.w3.org/2001/XMLSchema#string&quot;/&gt;
	 *     &lt;/owl:DatatypeProperty&gt;
	 *     &lt;owl:DatatypeProperty rdf:ID=&quot;edgeInRole&quot;&gt;
	 *         &lt;rdf:type rdf:resource=&quot;http://www.w3.org/2002/07/owl#FunctionalProperty&quot;/&gt;
	 *         &lt;rdfs:domain rdf:resource=&quot;#Edge&quot;/&gt;
	 *         &lt;rdfs:range rdf:resource=&quot;http://www.w3.org/2001/XMLSchema#string&quot;/&gt;
	 *     &lt;/owl:DatatypeProperty&gt;
	 * </pre>
	 * 
	 * @param schema
	 *            The {@code Schema} whose {@code GraphClass}es shall be
	 *            converted.
	 * @throws XMLStreamException
	 * 
	 * @see #convertAttributes(AttributedElementClass aec)
	 * @see #convertVertexClasses(GraphClass gc)
	 * @see #convertEdgeClasses(GraphClass gc)
	 * @see #writeDefaultGECProperties()
	 * @see #writeRoleElement(boolean from, String gecStringRep)
	 */
	private void convertGraphClasses(Schema schema) throws XMLStreamException {
		// create OWL class for Default GraphClass
		GraphClass gc = schema.getDefaultGraphClass();
		writeOwlClassStartElement(gc.getQualifiedName());

		// if gc is abstract and has subclasses, create union of subclasses
		if (gc.isAbstract() && !gc.getDirectSubClasses().isEmpty()) {
			writeUnionOfSubclasses(gc);
		}

		writer.writeEndElement();

		// create OWL class for "other" GraphClass
		gc = schema.getGraphClass();
		writeOwlClassStartElement(gc.getQualifiedName());

		// create references to superclasses
		for (AttributedElementClass superGC : gc.getDirectSuperClasses()) {
			writeRdfsSubClassOfEmptyElement("#" + superGC.getQualifiedName());
		}

		// if gc is abstract and has subclasses, create union of subclasses
		if (gc.isAbstract() && !gc.getDirectSubClasses().isEmpty()) {
			writeUnionOfSubclasses(gc);
		}

		writer.writeEndElement();

		convertAttributes(gc);
		convertVertexClasses(gc);
		convertEdgeClasses(gc);

		writeDefaultGECProperties();

		if (!edgeClasses2Properties) {
			writeRoleElement(true);
			writeRoleElement(false);
		}
	}

	/**
	 * Converts all {@code VertexClass}es of the given {@code GraphClass gc}
	 * together with their attributes to corresponding OWL constructs. A {@code
	 * VertexClass} itself with references to its superclasses is transformed to
	 * an OWL Class. Abstract {@code VertexClass}es are represented as unions of
	 * their subclasses.<br>
	 * <br>
	 * See the description of
	 * {@link #convertAttributes(AttributedElementClass aec)} for the
	 * representation of attributes.<br>
	 * <br>
	 * XML-code written for one {@code VertexClass vc} (except the default
	 * {@code VertexClass Vertex}:<br>
	 * 
	 * <pre>
	 *     &lt;owl:Class rdf:ID=&quot;&lt;i&gt;vc.getName()&lt;/i&gt;&quot;&gt;
	 *         &lt;rdfs:subClassOf rdf:resource=&quot;#&lt;i&gt;vc.getDirectSuperClasses.toArray(new VertexClass[0])[0]&lt;/i&gt;&quot;/&gt;
	 *         &lt;rdfs:subClassOf rdf:resource=&quot;#&lt;i&gt;vc.getDirectSuperClasses.toArray(new VertexClass[0])[1]&lt;/i&gt;&quot;/&gt;
	 *         &lt;i&gt;...&lt;/i&gt;
	 * </pre>
	 * 
	 * ------------------------------------<br>
	 * This part is only written if {@code vc} is abstract and has one or more
	 * subclasses:<br>
	 * 
	 * <pre>
	 *         &lt;owl:unionOf rdf:parseType=&quot;Collection&quot;&amp;rt
	 * 	       &lt;owl:Class rdf:about=&quot;#&lt;i&gt;vc.getDirectSubClasses.toArray(new VertexClass[0])[0]&lt;/i&gt;&quot;/&amp;rt
	 *             &lt;owl:Class rdf:about=&quot;#&lt;i&gt;vc.getDirectSubClasses.toArray(new VertexClass[0])[1]&lt;/i&gt;&quot;/&amp;rt
	 * 	       &lt;i&gt;...&lt;/i&gt;
	 *        &lt;/owl:unionOf&amp;rt
	 * </pre>
	 * 
	 * ------------------------------------<br>
	 * 
	 * <pre>
	 *     &lt;/owl:Class&gt;
	 * </pre>
	 * 
	 * The Class for the default {@code VertexClass Vertex} also has an
	 * anonymous superclass restricting the cardinality of the Property {@code
	 * vertexClassIsInGraph} to 1. This means that every {@code Vertex}
	 * individual and individuals of its subclasses only belong to one graph.<br>
	 * <br>
	 * XML-code written for the default {@code VertexClass Vertex}:<br>
	 * 
	 * <pre>
	 *     &lt;owl:Class rdf:ID=&quot;Vertex&quot;&gt;
	 *        &lt;rdfs:subClassOf&gt;
	 *            &lt;owl:Restriction&gt;
	 *                &lt;owl:onProperty rdf:resource=&quot;#vertexIsInGraph&quot;/&gt;
	 *                &lt;owl:cardinality rdf:datatype=&quot;http://www.w3.org/2001/XMLSchema#nonNegativeInteger&quot;&gt;1&lt;/owl:cardinality&gt;
	 *            &lt;/owl:Restriction&gt;
	 *        &lt;/rdfs:subClassOf&gt;
	 *     &lt;/owl:Class&gt;
	 * </pre>
	 * 
	 * @param gc
	 *            The {@code GraphClass} whose {@code VertexClass}es shall be
	 *            converted.
	 * @throws XMLStreamException
	 * 
	 * @see #convertAttributes(AttributedElementClass aec)
	 */
	private void convertVertexClasses(GraphClass gc) throws XMLStreamException {
		// for each VertexClass in gc
		for (VertexClass vc : gc.getVertexClasses()) {
			writeOwlClassStartElement(vc.getQualifiedName());

			// create references to superclasses
			for (AttributedElementClass superVC : vc.getDirectSuperClasses()) {
				writeRdfsSubClassOfEmptyElement("#"
						+ superVC.getQualifiedName());
			}

			// if vc is abstract and has subclasses, create union of subclasses
			if (vc.isAbstract() && !vc.getDirectSubClasses().isEmpty()) {
				writeUnionOfSubclasses(vc);
			}

			// create restriction for Property "vertexClassIsIn + gc.getName()"
			if (vc.getQualifiedName().equals("Vertex")) {
				writeDefaultGECCardinality("Vertex");
			}

			// create subclass restrictions for multiplicities
			for (EdgeClass ec : vc.getOwnConnectedEdgeClasses()) {
				if (ec.getFrom().getVertexClass() == vc) {
					writeMultiplicityElement(true, ec);
				}
				if (ec.getTo().getVertexClass() == vc) {
					writeMultiplicityElement(false, ec);
				}
			}

			writer.writeEndElement();
			convertAttributes(vc);
		}
	}

	/**
	 * Converts all {@code EdgeClass}es of the given {@code GraphClass gc}
	 * together with their attributes to corresponding OWL constructs, depending
	 * on the value of {@code edgeClasses2Properties}.
	 * 
	 * @param gc
	 *            The {@code GraphClass} whose {@code EdgeClass}es shall be
	 *            converted.
	 * @throws XMLStreamException
	 * 
	 * @see #convertEdgeClass2OWLProperty(EdgeClass ec)
	 * @see #convertEdgeClass2OWLClass(EdgeClass ec)
	 */
	private void convertEdgeClasses(GraphClass gc) throws XMLStreamException {
		// for each GraphElementClass gec contained in GraphClass "gc"
		for (GraphElementClass gec : gc.getGraphElementClasses()) {
			// if gec is of type EdgeClass
			if (gec instanceof EdgeClass) {
				if (edgeClasses2Properties) {
					convertEdgeClass2OWLProperty((EdgeClass) gec);
				} else {
					convertEdgeClass2OWLClass((EdgeClass) gec);
				}
			}
		}
	}

	/**
	 * Converts the given {@code EdgeClass ec} together with its attributes to
	 * two corresponding OWL Properties. One of these two Properties bears the
	 * name of {@code ec}, with the OWL Class representing the {@code
	 * VertexClass} on the "from" side as domain and the {@code VertexClass} on
	 * the "to" side as range. The other Property has the name of {@code ec}
	 * with an additional "-of" as suffix. Its domain and range are reversed.
	 * The multiplicities are mapped to subclass restrictions of the OWL Classes
	 * representing the incident {@code VertexClass}es. <br>
	 * <b>The attributes and rolenames of {@code ec} are not converted.</b><br>
	 * <br>
	 * XML-code written for {@code ec}:<br>
	 * 
	 * <pre>
	 *     &lt;owl:ObjectProperty rdf:ID=&quot;&lt;i&gt;ec.getName() + edgeClassNameSuffix&lt;/i&gt;&quot;&gt;
	 *         &lt;rdfs:subPropertyOf rdf:resource=&quot;#&lt;i&gt;ec.getDirectSuperClasses.toArray(new EdgeClass[0])[0] + edgeClassNameSuffix&lt;/i&gt;&quot;/&gt;
	 *         &lt;rdfs:subPropertyOf rdf:resource=&quot;#&lt;i&gt;ec.getDirectSuperClasses.toArray(new EdgeClass[0])[1] + edgeClassNameSuffix&lt;/i&gt;&quot;/&gt;
	 *         &lt;i&gt;...&lt;/i&gt;
	 *         &lt;rdfs:domain rdf:resource=&quot;#&lt;i&gt;ec.getFrom.getName()&lt;/i&gt;&quot;/&gt;
	 *         &lt;rdfs:range rdf:resource=&quot;#&lt;i&gt;ec.getTo.getName()&lt;/i&gt;&quot;/&gt;
	 *     &lt;/owl:ObjectProperty&gt;
	 * 
	 *     &lt;owl:ObjectProperty rdf:ID=&quot;&lt;i&gt;ec.getName() + edgeClassNameSuffix&lt;/i&gt;-of&quot;&gt;
	 * 		   &lt;owl:inverseOf rdf:resource=&quot;#&lt;i&gt;ec.getName() + edgeClassNameSuffix&lt;/i&gt;&quot;/&gt;
	 *         &lt;rdfs:subPropertyOf rdf:resource=&quot;#&lt;i&gt;ec.getDirectSuperClasses.toArray(new EdgeClass[0])[0] + edgeClassNameSuffix&lt;/i&gt;-of&quot;/&gt;
	 *         &lt;rdfs:subPropertyOf rdf:resource=&quot;#&lt;i&gt;ec.getDirectSuperClasses.toArray(new EdgeClass[0])[1] + edgeClassNameSuffix&lt;/i&gt;-of&quot;/&gt;
	 *         &lt;i&gt;...&lt;/i&gt;
	 *         &lt;rdfs:domain rdf:resource=&quot;#&lt;i&gt;ec.getTo.getName()&lt;/i&gt;&quot;/&gt;
	 *         &lt;rdfs:range rdf:resource=&quot;#&lt;i&gt;ec.getFrom.getName()&lt;/i&gt;&quot;/&gt;
	 *     &lt;/owl:ObjectProperty&gt;
	 * </pre>
	 * 
	 * The following subclass restriction is appended as a child to the OWL
	 * class representing the {@code VertexClass} on the "from" side.<br>
	 * 
	 * <pre>
	 *         &lt;rdfs:subClassOf&gt;
	 *             &lt;owl:Restriction&gt;
	 *                 &lt;owl:onProperty rdf:resource=&quot;#&lt;i&gt;ec.getName() + edgeClassNameSuffix&lt;/i&gt;&quot;/&gt;
	 *                 &lt;owl:minCardinality rdf:datatype=&quot;http://www.w3.org/2001/XMLSchema#nonNegativeInteger&quot;&gt;&lt;i&gt;gec.getToMin()&lt;/i&gt;&lt;/owl:minCardinality&gt;
	 *                 &lt;owl:maxCardinality rdf:datatype=&quot;http://www.w3.org/2001/XMLSchema#nonNegativeInteger&quot;&gt;&lt;i&gt;gec.getToMax()&lt;/i&gt;&lt;/owl:maxCardinality&gt;
	 *             &lt;/owl:Restriction&gt;
	 *         &lt;/rdfs:subClassOf&gt;
	 * </pre>
	 * 
	 * The following subclass restriction is appended as a child to the OWL
	 * class representing the {@code VertexClass} on the "to" side.<br>
	 * 
	 * <pre>
	 *         &lt;rdfs:subClassOf&gt;
	 *             &lt;owl:Restriction&gt;
	 *                 &lt;owl:onProperty rdf:resource=&quot;#&lt;i&gt;ec.getName() + edgeClassNameSuffix&lt;/i&gt;-of&quot;/&gt;
	 *                 &lt;owl:minCardinality rdf:datatype=&quot;http://www.w3.org/2001/XMLSchema#nonNegativeInteger&quot;&gt;&lt;i&gt;gec.getFromMin()&lt;/i&gt;&lt;/owl:minCardinality&gt;
	 *                 &lt;owl:maxCardinality rdf:datatype=&quot;http://www.w3.org/2001/XMLSchema#nonNegativeInteger&quot;&gt;&lt;i&gt;gec.getFromMax()&lt;/i&gt;&lt;/owl:maxCardinality&gt;
	 *             &lt;/owl:Restriction&gt;
	 *         &lt;/rdfs:subClassOf&gt;
	 * </pre>
	 * 
	 * @param ec
	 *            The {@code EdgeClass} which shall be converted.
	 * @throws XMLStreamException
	 * 
	 * @see #writeMultiplicityElement(boolean from, EdgeClass ec)
	 */
	private void convertEdgeClass2OWLProperty(EdgeClass ec)
			throws XMLStreamException {
		// write normal property
		writeOwlObjectPropertyStartElement(HelperMethods.firstToLowerCase(ec
				.getQualifiedName())
				+ edgeClassNameSuffix);

		for (AttributedElementClass superEC : ec.getDirectSuperClasses()) {
			writeRdfsSubPropertyOfEmptyElement("#"
					+ HelperMethods
							.firstToLowerCase(superEC.getQualifiedName())
					+ edgeClassNameSuffix);
		}
		writeRdfsDomainEmptyElement("#"
				+ (ec).getFrom().getVertexClass().getQualifiedName());
		writeRdfsRangeEmptyElement("#"
				+ (ec).getTo().getVertexClass().getQualifiedName());

		writer.writeEndElement();

		// write "-of" property
		writeOwlObjectPropertyStartElement(HelperMethods.firstToLowerCase(ec
				.getQualifiedName())
				+ edgeClassNameSuffix + "-of");

		for (AttributedElementClass superEC : ec.getDirectSuperClasses()) {
			writeRdfsSubPropertyOfEmptyElement("#"
					+ HelperMethods
							.firstToLowerCase(superEC.getQualifiedName())
					+ edgeClassNameSuffix + "-of");
		}

		writeOwlInverseOfEmptyElement("#"
				+ HelperMethods.firstToLowerCase(ec.getQualifiedName())
				+ edgeClassNameSuffix);

		writeRdfsDomainEmptyElement("#"
				+ (ec).getTo().getVertexClass().getQualifiedName());
		writeRdfsRangeEmptyElement("#"
				+ (ec).getFrom().getVertexClass().getQualifiedName());

		writer.writeEndElement();
	}

	/**
	 * Converts the given {@code EdgeClass ec} together with its attributes to
	 * corresponding OWL constructs. {@code ec} itself with references to its
	 * superclasses is transformed to an OWL Class and two Properties relating
	 * the OWL Classes representing the two incident {@code VertexClass}es on
	 * the "from" and "to" sides to the OWL Class representing the {@code
	 * EdgeClass}. The multiplicities are mapped to subclass restrictions of the
	 * OWL Classes representing the incident {@code VertexClass}es. If {@code
	 * ec} is abstract, it is represented as union of its subclasses.<br>
	 * <br>
	 * See the description of
	 * {@link #convertAttributes(AttributedElementClass aec)} for the
	 * representation of attributes.<br>
	 * <br>
	 * XML-code written for {@code ec} (except the default {@code EdgeClass
	 * Edge}):<br>
	 * 
	 * <pre>
	 *     &lt;owl:Class rdf:ID=&quot;&lt;i&gt;ec.getName() + edgeClassNameSuffix&lt;/i&gt;&quot;&gt;
	 *         &lt;rdfs:subClassOf rdf:resource=&quot;#&lt;i&gt;ec.getDirectSuperClasses.toArray(new EdgeClass[0])[0] + edgeClassNameSuffix&lt;/i&gt;&quot;/&gt;
	 *         &lt;rdfs:subClassOf rdf:resource=&quot;#&lt;i&gt;ec.getDirectSuperClasses.toArray(new EdgeClass[0])[1] + edgeClassNameSuffix&lt;/i&gt;&quot;/&gt;
	 *         &lt;i&gt;...&lt;/i&gt;
	 * </pre>
	 * 
	 * ------------------------------------<br>
	 * This part is only written if {@code ec} is abstract and has one or more
	 * subclasses:<br>
	 * 
	 * <pre>
	 *         &lt;owl:unionOf rdf:parseType=&quot;Collection&quot;&amp;rt
	 * 	       &lt;owl:Class rdf:about=&quot;#&lt;i&gt;ec.getDirectSubClasses.toArray(new EdgeClass[0])[0] + edgeClassNameSuffix&lt;/i&gt;&quot;/&amp;rt
	 *             &lt;owl:Class rdf:about=&quot;#&lt;i&gt;ec.getDirectSubClasses.toArray(new EdgeClass[0])[1] + edgeClassNameSuffix&lt;/i&gt;&quot;/&amp;rt
	 * 	       &lt;i&gt;...&lt;/i&gt;
	 *        &lt;/owl:unionOf&amp;rt
	 * </pre>
	 * 
	 * ------------------------------------<br>
	 * 
	 * <pre>
	 *     &lt;/owl:Class&gt;
	 * 
	 *     &lt;owl:ObjectProperty rdf:ID=&quot;&lt;i&gt;ec.getName() + edgeClassNameSuffix&lt;/i&gt;From&quot;&gt;
	 *         &lt;rdf:type rdf:resource=&quot;http://www.w3.org/2002/07/owl#SymmetricProperty&quot;/&gt;
	 *         &lt;rdfs:domain rdf:resource=&quot;#&lt;i&gt;ec.getFrom().getName()&lt;/i&gt;&quot;/&gt;
	 *         &lt;rdfs:range rdf:resource=&quot;#&lt;i&gt;ec.getName()&lt;/i&gt;&quot;/&gt;
	 *     &lt;/owl:ObjectProperty&gt;
	 * 
	 *     &lt;owl:ObjectProperty rdf:ID=&quot;&lt;i&gt;ec.getName() + edgeClassNameSuffix&lt;/i&gt;To&quot;&gt;
	 *         &lt;rdf:type rdf:resource=&quot;http://www.w3.org/2002/07/owl#SymmetricProperty&quot;/&gt;
	 *         &lt;rdfs:domain rdf:resource=&quot;#&lt;i&gt;ec.getTo().getName()&lt;/i&gt;&quot;/&gt;
	 *         &lt;rdfs:range rdf:resource=&quot;#&lt;i&gt;ec.getName()&lt;/i&gt;&quot;/&gt;
	 *     &lt;/owl:ObjectProperty&gt;
	 * </pre>
	 * 
	 * The following subclass restriction is appended as a child to the OWL
	 * class representing the {@code VertexClass} on the "from" side.<br>
	 * 
	 * <pre>
	 *         &lt;rdfs:subClassOf&gt;
	 *             &lt;owl:Restriction&gt;
	 *                 &lt;owl:onProperty rdf:resource=&quot;#&lt;i&gt;ec.getName() + edgeClassNameSuffix&lt;/i&gt;From&quot;/&gt;
	 *                 &lt;owl:minCardinality rdf:datatype=&quot;http://www.w3.org/2001/XMLSchema#nonNegativeInteger&quot;&gt;&lt;i&gt;gec.getToMin()&lt;/i&gt;&lt;/owl:minCardinality&gt;
	 *                 &lt;owl:maxCardinality rdf:datatype=&quot;http://www.w3.org/2001/XMLSchema#nonNegativeInteger&quot;&gt;&lt;i&gt;gec.getToMax()&lt;/i&gt;&lt;/owl:maxCardinality&gt;
	 *             &lt;/owl:Restriction&gt;
	 *         &lt;/rdfs:subClassOf&gt;
	 * </pre>
	 * 
	 * The following subclass restriction is appended as a child to the OWL
	 * class representing the {@code VertexClass} on the "to" side.<br>
	 * 
	 * <pre>
	 *         &lt;rdfs:subClassOf&gt;
	 *             &lt;owl:Restriction&gt;
	 *                 &lt;owl:onProperty rdf:resource=&quot;#&lt;i&gt;ec.getName() + edgeClassNameSuffix&lt;/i&gt;To&quot;/&gt;
	 *                 &lt;owl:minCardinality rdf:datatype=&quot;http://www.w3.org/2001/XMLSchema#nonNegativeInteger&quot;&gt;&lt;i&gt;gec.getFromMin()&lt;/i&gt;&lt;/owl:minCardinality&gt;
	 *                 &lt;owl:maxCardinality rdf:datatype=&quot;http://www.w3.org/2001/XMLSchema#nonNegativeInteger&quot;&gt;&lt;i&gt;gec.getFromMax()&lt;/i&gt;&lt;/owl:maxCardinality&gt;
	 *             &lt;/owl:Restriction&gt;
	 *         &lt;/rdfs:subClassOf&gt;
	 * </pre>
	 * 
	 * The OWL Class for the default {@code EdgeClass Edge} also has an
	 * anonymous superclass restricting the cardinality of the Property {@code
	 * edgeClassIsInGraph} to 1. This means that every {@code Edge} individual
	 * and individuals of its subclasses only belong to one graph.<br>
	 * <br>
	 * XML-code written for the default {@code EdgeClass Edge} (only {@code
	 * <owl:Class rdf:ID="Edge">} element):<br>
	 * 
	 * <pre>
	 *     &lt;owl:Class rdf:ID=&quot;Edge&lt;i&gt; + edgeClassNameSuffix&lt;/i&gt;&quot;&gt;
	 *        &lt;rdfs:subClassOf&gt;
	 *            &lt;owl:Restriction&gt;
	 *                &lt;owl:onProperty rdf:resource=&quot;#edgeIsInGraph&quot;/&gt;
	 *                &lt;owl:cardinality rdf:datatype=&quot;http://www.w3.org/2001/XMLSchema#nonNegativeInteger&quot;&gt;1&lt;/owl:cardinality&gt;
	 *            &lt;/owl:Restriction&gt;
	 *        &lt;/rdfs:subClassOf&gt;
	 *     &lt;/owl:Class&gt;
	 * </pre>
	 * 
	 * If {@code ec} is the default {@code AggregationClass Aggregation},
	 * another Property is created, relating {@code Aggregation} (and all its
	 * subclasses) to the default {@code VertexClass Vertex} constituting the
	 * aggregate:<br>
	 * XML-code written for the default {@code AggregationClass Aggregation}:<br>
	 * 
	 * <pre>
	 *     &lt;owl:ObjectProperty rdf:ID=&quot;aggregate&quot;&gt;
	 *         &lt;rdf:type rdf:resource=&quot;http://www.w3.org/2002/07/owl#FunctionalProperty&quot;/&gt;
	 *         &lt;rdfs:domain rdf:resource=&quot;#Aggregation&lt;i&gt; + edgeClassNameSuffix&lt;/i&gt;&quot;/&gt;
	 *         &lt;rdfs:range rdf:resource=&quot;#Vertex&quot;/&gt;
	 *     &lt;/owl:ObjectProperty&gt;
	 * </pre>
	 * 
	 * The following subclass restriction is appended as child of the OWL Class
	 * representing the {@code Aggregation}:<br>
	 * 
	 * <pre>
	 *         &lt;rdfs:subClassOf&gt;
	 *             &lt;owl:Restriction&gt;
	 *                 &lt;owl:onProperty rdf:resource=&quot;#aggregate&quot;/&gt;
	 *                 &lt;owl:cardinality rdf:datatype=&quot;http://www.w3.org/2001/XMLSchema#nonNegativeInteger&quot;&gt;1&lt;/owl:cardinality&gt;
	 *             &lt;/owl:Restriction&gt;
	 *         &lt;/rdfs:subClassOf&gt;
	 * </pre>
	 * 
	 * @param ec
	 *            The {@code EdgeClass} which shall be converted.
	 * @throws XMLStreamException
	 * 
	 * @see #convertAttributes(AttributedElementClass aec)
	 * @see #writeMultiplicityElement(boolean from, EdgeClass ec)
	 * @see #writeIncidentVertexClassElement(boolean from, EdgeClass ec)
	 * @see #writeAggregateElement()
	 * @see #writeAggregateSubClassElement(Element elem)
	 */
	private void convertEdgeClass2OWLClass(EdgeClass ec)
			throws XMLStreamException {
		writeOwlClassStartElement(ec.getQualifiedName() + edgeClassNameSuffix);

		// create superclass references
		for (AttributedElementClass superEC : ec.getDirectSuperClasses()) {
			writeRdfsSubClassOfEmptyElement("#" + superEC.getQualifiedName()
					+ edgeClassNameSuffix);
		}

		// if gec is abstract and has subclasses, create union of subclasses
		if (ec.isAbstract() && !ec.getDirectSubClasses().isEmpty()) {
			writeUnionOfSubclasses(ec);
		}

		// create ObjectProperty for aggregate
		if (ec.getQualifiedName().equals("Aggregation")) {
			writeAggregateSubClassElement();
		}

		// create subclass restriction for Property
		// "edgeClassIsIn + gc.getName()"
		if (ec.getQualifiedName().equals("Edge")) {
			writeDefaultGECCardinality("Edge");
		}

		writer.writeEndElement();

		if (ec.getQualifiedName().equals("Aggregation")) {
			writeAggregateElement();
		}

		// build subtrees

		// create ObjectProperties for incident VertexClasses
		writeIncidentVertexClassElement(true, ec);
		writeIncidentVertexClassElement(false, ec);

		// convert attributes
		convertAttributes(ec);
	}

	/**
	 * Writes an {@code ObjectProperty} representing the relation from a {@code
	 * VertexClass} to the incident {@code EdgeClass ec} (if {@code
	 * edgeClasses2Properties = false}). Whether the "from" or "to" relation is
	 * created depends on the value of the {@code from} parameter.<br>
	 * <br>
	 * XML code written if {@code from = true}:<br>
	 * 
	 * <pre>
	 *     &lt;owl:ObjectProperty rdf:ID=&quot;&lt;i&gt;ec.getName() + edgeClassNameSuffix&lt;/i&gt;Out&quot;&gt;
	 *         &lt;rdf:type rdf:resource=&quot;http://www.w3.org/2002/07/owl#SymmetricProperty&quot;/&gt;
	 *         &lt;rdfs:domain rdf:resource=&quot;#&lt;i&gt;ec.getFrom().getName()&lt;/i&gt;&quot;/&gt;
	 *         &lt;rdfs:range rdf:resource=&quot;#&lt;i&gt;ec.getName()&lt;/i&gt;&quot;/&gt;
	 *     &lt;/owl:ObjectProperty&gt;
	 * </pre>
	 * 
	 * @param from
	 *            Indicates whether a Property for the {@code VertexClass} on
	 *            the "from" side or for the {@code VertexClass} on the "to"
	 *            side shall be created.
	 * @param ec
	 *            The {@code EdgeClass} for which the Property shall be created.
	 * @throws XMLStreamException
	 */
	private void writeIncidentVertexClassElement(boolean from, EdgeClass ec)
			throws XMLStreamException {
		String direction;
		String vcName;

		// get name of 'from' or 'to' VertexClass
		if (from) {
			direction = "Out";
			vcName = ec.getFrom().getVertexClass().getQualifiedName();
		} else {
			direction = "In";
			vcName = ec.getTo().getVertexClass().getQualifiedName();
		}

		// create ObjectProperty for incident EdgeClass
		writeOwlObjectPropertyStartElement(HelperMethods.firstToLowerCase(ec
				.getQualifiedName())
				+ edgeClassNameSuffix + direction);

		writeRdfTypeEmptyElement(JGraLab2OWL.owlNS + "SymmetricProperty");
		writeRdfsDomainEmptyElement("#" + vcName);
		writeRdfsRangeEmptyElement("#" + ec.getQualifiedName()
				+ edgeClassNameSuffix);

		writer.writeEndElement();
	}

	/**
	 * Writes a subclass restriction representing the multiplicity of a {@code
	 * VertexClass} incident to the {@code EdgeClass ec}. Whether a restriction
	 * for the "from" or "to" multiplicity is created depends on the value of
	 * the {@code from} parameter.<br>
	 * <br>
	 * XML code written if {@code from = true}:<br>
	 * 
	 * <pre>
	 *         &lt;rdfs:subClassOf&gt;
	 *             &lt;owl:Restriction&gt;
	 *                 &lt;owl:onProperty rdf:resource=&quot;#&lt;i&gt;ec.getName() + edgeClassNameSuffix&lt;/i&gt;Out&quot;/&gt;
	 *                 &lt;owl:minCardinality rdf:datatype=&quot;http://www.w3.org/2001/XMLSchema#nonNegativeInteger&quot;&gt;ec.getToMin()&lt;/owl:minCardinality&gt;
	 *                 &lt;owl:maxCardinality rdf:datatype=&quot;http://www.w3.org/2001/XMLSchema#nonNegativeInteger&quot;&gt;ec.getToMax()&lt;/owl:maxCardinality&gt;
	 *             &lt;/owl:Restriction&gt;
	 *         &lt;/rdfs:subClassOf&gt;
	 * </pre>
	 * 
	 * @param from
	 *            Indicates whether a subclass restriction for the {@code
	 *            VertexClass} on the "from" side or for the {@code VertexClass}
	 *            on the "to" side of the {@code EdgeClass ec} shall be created.
	 * @param ec
	 *            The {@code EdgeClass} for which the subclass restriction shall
	 *            be created.
	 * @throws XMLStreamException
	 */
	private void writeMultiplicityElement(boolean from, EdgeClass ec)
			throws XMLStreamException {
		int lowerBound;
		int upperBound;
		String direction;

		// get multiplicities for "from" or "to" VertexClasses
		if (from) {
			direction = "Out";
			lowerBound = ec.getTo().getMin();
			upperBound = ec.getTo().getMax();
		} else {
			direction = "In";
			lowerBound = ec.getFrom().getMin();
			upperBound = ec.getFrom().getMax();
		}

		writeRdfsSubClassOfStartElement();
		writeOwlRestrictionStartElement();

		// create subclass restriction
		if (edgeClasses2Properties) {
			if (from) {
				writeOwlOnPropertyEmptyElement("#"
						+ HelperMethods.firstToLowerCase(ec.getQualifiedName())
						+ edgeClassNameSuffix);
			} else {
				writeOwlOnPropertyEmptyElement("#"
						+ HelperMethods.firstToLowerCase(ec.getQualifiedName())
						+ edgeClassNameSuffix + "-of");
			}
		} else {
			writeOwlOnPropertyEmptyElement("#"
					+ HelperMethods.firstToLowerCase(ec.getQualifiedName())
					+ edgeClassNameSuffix + direction);
		}
		writeOwlMinCardinalityStartElement();
		writer.writeCharacters(String.valueOf(lowerBound));
		writer.writeEndElement();

		writeOwlMaxCardinalityStartElement();
		writer.writeCharacters(String.valueOf(upperBound));
		writer.writeEndElement();

		writer.writeEndElement();
		writer.writeEndElement();
	}

	/**
	 * Writes an {@code ObjectProperty} relating the OWL Class representing the
	 * {@code AggregationClass} or {@code CompositionClass ac} to the OWL Class
	 * {@code VertexClass} constituting the aggregate.<br>
	 * <br>
	 * XML code written :<br>
	 * 
	 * <pre>
	 *     &lt;owl:ObjectProperty rdf:ID=&quot;aggregate&quot;&gt;
	 *         &lt;rdf:type rdf:resource=&quot;http://www.w3.org/2002/07/owl#FunctionalProperty&quot;/&gt;
	 *         &lt;rdfs:domain rdf:resource=&quot;#Aggregation&lt;i&gt; + edgeClassNameSuffix&lt;/i&gt;&quot;/&gt;
	 *         &lt;rdfs:range rdf:resource=&quot;#Vertex&quot;/&gt;
	 *     &lt;/owl:ObjectProperty&gt;
	 * </pre>
	 * 
	 * @throws XMLStreamException
	 */
	private void writeAggregateElement() throws XMLStreamException {
		writeOwlObjectPropertyStartElement("aggregate");

		writeRdfTypeEmptyElement(JGraLab2OWL.owlNS + "FunctionalProperty");
		writeRdfsDomainEmptyElement("#Aggregation" + edgeClassNameSuffix);
		writeRdfsRangeEmptyElement("#Vertex");

		writer.writeEndElement();
	}

	/**
	 * Writes a subclass restriction representing the cardinality "1" of an OWL
	 * Class representing a {@code Vertex} related to the OWL Class {@code
	 * Aggregation} or {@code AggregationEdgeClass}, respectively. via {@code
	 * aggregate} Property.<br>
	 * 
	 * <pre>
	 *         &lt;rdfs:subClassOf&gt;
	 *             &lt;owl:Restriction&gt;
	 *                 &lt;owl:onProperty rdf:resource=&quot;#aggregate&quot;/&gt;
	 *                 &lt;owl:cardinality rdf:datatype=&quot;http://www.w3.org/2001/XMLSchema#nonNegativeInteger&quot;&gt;1&lt;/owl:cardinality&gt;
	 *             &lt;/owl:Restriction&gt;
	 *         &lt;/rdfs:subClassOf&gt;
	 * </pre>
	 * 
	 * @param aggregateElem
	 *            The Property relating to the aggregated Class for which the
	 *            subclass restriction shall be created.
	 * @throws XMLStreamException
	 */
	private void writeAggregateSubClassElement() throws XMLStreamException {
		writeRdfsSubClassOfStartElement();
		writeOwlRestrictionStartElement();

		writeOwlOnPropertyEmptyElement("#aggregate");
		writeOwlCardinalityStartElement();
		writer.writeCharacters(String.valueOf(1));
		writer.writeEndElement();

		writer.writeEndElement();
		writer.writeEndElement();
	}

	/**
	 * Converts the attributes of an {@code AttributedElementClass aec} to OWL
	 * Properties. The Properties' names correspond to the attributes' names.
	 * For each attribute, a Property relating {@code aec} to the Class or
	 * datatype representing the attribute's type is created.<br>
	 * <br>
	 * XML-code written for one {@code Attribute attr} of basic type:<br>
	 * 
	 * <pre>
	 *     &lt;owl:DatatypeProperty rdf:ID=&quot;&lt;i&gt;aec.getName()&lt;/i&gt;Has&lt;i&gt;attr.getName()&lt;/i&gt;&quot;&gt;
	 *         &lt;rdfs:domain rdf:resource=&quot;#&lt;i&gt;aec.getName() + edgeClassNameSuffix&lt;/i&gt;&quot;/&gt;
	 *         &lt;rdfs:range rdf:resource=&quot;#&lt;i&gt;attr.getDomain().getJavaAttributeImplementationTypeName()&lt;/i&gt;&quot;/&gt;
	 *     &lt;/owl:DatatypeProperty&gt;
	 * </pre>
	 * 
	 * XML-code written for one {@code Attribute attr} of composite type:<br>
	 * 
	 * <pre>
	 *     &lt;owl:ObjectProperty rdf:ID=&quot;&lt;i&gt;aec.getName()&lt;/i&gt;EdgeClassHas&lt;i&gt;attr.getName()&lt;/i&gt;&quot;&gt;
	 *         &lt;rdfs:domain rdf:resource=&quot;#&lt;i&gt;aec.getName() + edgeClassNameSuffix&lt;/i&gt;&quot;/&gt;
	 *         &lt;rdfs:range rdf:resource=&quot;#&lt;i&gt;attr.getDomain().getName()&lt;/i&gt;&quot;/&gt;
	 *     &lt;/owl:ObjectProperty&gt;
	 * </pre>
	 * 
	 * @param aec
	 *            The {@code AttributedElementClass} whose attributes shall be
	 *            converted.
	 * @throws XMLStreamException
	 */
	private void convertAttributes(AttributedElementClass aec)
			throws XMLStreamException {
		String aecElemName;

		if (aec instanceof EdgeClass) {
			aecElemName = aec.getQualifiedName() + edgeClassNameSuffix;
		} else {
			aecElemName = aec.getQualifiedName();
		}

		// for every attribute of "aec"
		for (Attribute attr : aec.getOwnAttributeList()) {
			// if "attr" has a CompositeDomain or an EnumDomain as type
			if (attr.getDomain().isComposite()
					|| attr.getDomain().toString().contains("Enum")) {
				writeOwlObjectPropertyStartElement(HelperMethods
						.firstToLowerCase(aecElemName)
						+ "Has"
						+ HelperMethods.firstToUpperCase(attr.getName()));

				writeRdfTypeEmptyElement(JGraLab2OWL.owlNS
						+ "FunctionalProperty");
				writeRdfsDomainEmptyElement("#" + aecElemName);

				writeRdfsRangeEmptyElement();
				if (attr.getDomain().getTGTypeName(null).contains("List<")) {
					writer.writeAttribute(JGraLab2OWL.rdfNS, "resource",
							"#ListElement");
				} else if (attr.getDomain().getTGTypeName(null)
						.contains("Set<")) {
					writer
							.writeAttribute(JGraLab2OWL.rdfNS, "resource",
									"#Set");
				} else {
					writer.writeAttribute(JGraLab2OWL.rdfNS, "resource", "#"
							+ attr.getDomain().getQualifiedName());
				}
				// if "attr" has a BasicDomain as type
			} else {
				writeOwlDatatypePropertyStartElement(HelperMethods
						.firstToLowerCase(aecElemName)
						+ "Has"
						+ HelperMethods.firstToUpperCase(attr.getName()));

				writeRdfTypeEmptyElement(JGraLab2OWL.owlNS
						+ "FunctionalProperty");
				writeRdfsDomainEmptyElement("#" + aecElemName);

				writeRdfsRangeEmptyElement();
				if (attr.getDomain().getTGTypeName(null).equals("String")) {
					writer.writeAttribute(JGraLab2OWL.rdfNS, "resource",
							JGraLab2OWL.xsdNS + "string");
				} else {
					writer
							.writeAttribute(
									JGraLab2OWL.rdfNS,
									"resource",
									JGraLab2OWL.xsdNS
											+ attr
													.getDomain()
													.getJavaAttributeImplementationTypeName(
															""));
				}
			}

			writer.writeEndElement();
		}
	}

	/**
	 * Writes a Property relating the OWL Class with the ID {@code Edge} to a
	 * {@code string} representing a role name. Whether the role name on the
	 * "from" or "to" side is related depends on the value of the parameter
	 * {@code from}. <br>
	 * XML-code written if {@code appendSuffix2EdgeClassName = false}:<br>
	 * 
	 * <pre>
	 *     &lt;owl:DatatypeProperty rdf:ID=&quot;EdgeOutRole&quot;&gt;
	 *         &lt;rdf:type rdf:resource=&quot;http://www.w3.org/2002/07/owl#FunctionalProperty&quot;/&gt;
	 *         &lt;rdfs:domain rdf:resource=&quot;#Edge&quot;/&gt;
	 *         &lt;rdfs:range rdf:resource=&quot;http://www.w3.org/2001/XMLSchema#string&quot;/&gt;
	 *     &lt;/owl:DatatypeProperty&gt;
	 * </pre>
	 * 
	 * @param from
	 *            Indicates whether the Property for the role name on the "from"
	 *            side or for the role name on the "to" side shall be created.
	 * @throws XMLStreamException
	 */
	private void writeRoleElement(boolean from) throws XMLStreamException {
		String direction;

		if (from) {
			direction = "Out";
		} else {
			direction = "In";
		}

		// create DatatypeProperty
		writeOwlDatatypePropertyStartElement(HelperMethods
				.firstToLowerCase("Edge" + edgeClassNameSuffix)
				+ direction + "Role");

		writeRdfTypeEmptyElement(JGraLab2OWL.owlNS + "FunctionalProperty");
		writeRdfsDomainEmptyElement("#Edge" + edgeClassNameSuffix);
		writeRdfsRangeEmptyElement(JGraLab2OWL.xsdNS + "string");

		writer.writeEndElement();
	}

	/**
	 * Writes two Properties for each element of array {@code defaultGECs} which
	 * relate them to their containing {@code GraphClass gc} and vice versa.<br>
	 * <br>
	 * XML-code written:<br>
	 * 
	 * <pre>
	 *     &lt;owl:ObjectProperty rdf:ID=&quot;vertexIsInGraph&quot;&gt;
	 *         &lt;rdf:type rdf:resource=&quot;http://www.w3.org/2002/07/owl#FunctionalProperty&quot;/&gt;
	 *         &lt;rdfs:domain rdf:resource=&quot;#Vertex&quot;/&gt;
	 *         &lt;rdfs:range rdf:resource=&quot;#Graph&quot;/&gt;
	 *     &lt;/owl:ObjectProperty&gt;
	 *     &lt;owl:ObjectProperty rdf:ID=&quot;graphContainsVertex&quot;&gt;
	 *         &lt;owl:inverseOf rdf:resource=&quot;#vertexClassIsIn&lt;i&gt;gc.getName()&lt;/i&gt;&quot;/&gt;
	 *         &lt;rdfs:domain rdf:resource=&quot;#Graph&quot;/&gt;
	 *         &lt;rdfs:range rdf:resource=&quot;#Vertex&quot;/&gt;
	 *     &lt;/owl:ObjectProperty&gt;
	 * </pre>
	 * 
	 * XML-code written if {@code edgeClasses2Properties = false}:<br>
	 * 
	 * <pre>
	 *     &lt;owl:ObjectProperty rdf:ID=&quot;edgeIsInGraph&quot;&gt;
	 *         &lt;rdf:type rdf:resource=&quot;http://www.w3.org/2002/07/owl#FunctionalProperty&quot;/&gt;
	 *         &lt;rdfs:domain rdf:resource=&quot;#Edge&lt;i&gt; + edgeClassNameSuffix&lt;i&gt;&quot;/&gt;
	 *         &lt;rdfs:range rdf:resource=&quot;#Graph&quot;/&gt;
	 *     &lt;/owl:ObjectProperty&gt;
	 *     &lt;owl:ObjectProperty rdf:ID=&quot;graphContainsEdge&quot;&gt;
	 *         &lt;owl:inverseOf rdf:resource=&quot;#edgeClassIsInGraph&quot;/&gt;
	 *         &lt;rdfs:domain rdf:resource=&quot;#Graph&quot;/&gt;
	 *         &lt;rdfs:range rdf:resource=&quot;#Edge&lt;i&gt; + edgeClassNameSuffix&lt;i&gt;&quot;/&gt;
	 *     &lt;/owl:ObjectProperty&gt;
	 * </pre>
	 * 
	 * @throws XMLStreamException
	 */
	private void writeDefaultGECProperties() throws XMLStreamException {
		for (String gecName : defaultGECs) {
			if (!(gecName.equals("Edge") && edgeClasses2Properties)) {
				// create ObjectProperty relating GraphElementClass to
				// GraphClass
				if (gecName.equals("Edge")) {
					writeOwlObjectPropertyStartElement(HelperMethods
							.firstToLowerCase(gecName)
							+ edgeClassNameSuffix + "IsInGraph");
					writeRdfTypeEmptyElement(JGraLab2OWL.owlNS
							+ "FunctionalProperty");
					writeRdfsDomainEmptyElement("#" + gecName
							+ edgeClassNameSuffix);
				} else {
					writeOwlObjectPropertyStartElement(HelperMethods
							.firstToLowerCase(gecName)
							+ "IsInGraph");
					writeRdfTypeEmptyElement(JGraLab2OWL.owlNS
							+ "FunctionalProperty");
					writeRdfsDomainEmptyElement("#" + gecName);
				}

				writeRdfsRangeEmptyElement("#Graph");

				writer.writeEndElement();

				// create ObjectProperty relating GraphClass to
				// GraphElementClass
				if (gecName.equals("Edge")) {
					writeOwlObjectPropertyStartElement("graphContains"
							+ gecName + edgeClassNameSuffix);

					writeOwlInverseOfEmptyElement("#"
							+ HelperMethods.firstToLowerCase(gecName)
							+ edgeClassNameSuffix + "IsInGraph");
					writeRdfsDomainEmptyElement("#Graph");
					writeRdfsRangeEmptyElement("#" + gecName
							+ edgeClassNameSuffix);
				} else {
					writeOwlObjectPropertyStartElement("graphContains"
							+ gecName);

					writeOwlInverseOfEmptyElement("#"
							+ HelperMethods.firstToLowerCase(gecName)
							+ "IsInGraph");
					writeRdfsDomainEmptyElement("#Graph");
					writeRdfsRangeEmptyElement("#" + gecName);
				}

				writer.writeEndElement();
			}
		}
	}

	/**
	 * Writes a subclass restriction restricting the cardinality of the Property
	 * <i>gecName</i>{@code ClassIsIn}<i>gcName</i> to 1. This means that every
	 * {@code GraphElementClass} individual and individuals of its subclasses
	 * only belong to one graph.<br>
	 * <br>
	 * XML-code written:<br>
	 * 
	 * <pre>
	 *        &lt;rdfs:subClassOf&gt;
	 *            &lt;owl:Restriction&gt;
	 *                &lt;owl:onProperty rdf:resource=&quot;#&lt;i&gt;gecName&lt;/i&gt;ClassIsIn&lt;i&gt;gcName&lt;/i&gt;&quot;/&gt;
	 *                &lt;owl:cardinality rdf:datatype=&quot;http://www.w3.org/2001/XMLSchema#nonNegativeInteger&quot;&gt;1&lt;/owl:cardinality&gt;
	 *            &lt;/owl:Restriction&gt;
	 *        &lt;/rdfs:subClassOf&gt;
	 * </pre>
	 * 
	 * @param gecName
	 *            The name of the {@code GraphElementClass}.
	 * @throws XMLStreamException
	 */
	private void writeDefaultGECCardinality(String gecName)
			throws XMLStreamException {
		writeRdfsSubClassOfStartElement();
		writeOwlRestrictionStartElement();

		if (gecName.equals("Edge")) {
			writeOwlOnPropertyEmptyElement("#"
					+ HelperMethods.firstToLowerCase(gecName)
					+ edgeClassNameSuffix + "IsInGraph");
		} else {
			writeOwlOnPropertyEmptyElement("#"
					+ HelperMethods.firstToLowerCase(gecName) + "IsInGraph");
		}
		writeOwlCardinalityStartElement();
		writer.writeCharacters(String.valueOf(1));
		writer.writeEndElement();

		writer.writeEndElement();
		writer.writeEndElement();
	}

	/**
	 * Writes an owl:unionOf element which contains a child owl:Class element
	 * for every direct subclass of the {@code AttributedElementClass aec}. This
	 * is a representation for abstract {@code AttributedElementClasses} in OWL.
	 * 
	 * @param aec
	 *            The {@code AttributedElementClass} for which the owl:unionOf
	 *            element shall be created.
	 * @throws XMLStreamException
	 */
	private void writeUnionOfSubclasses(AttributedElementClass aec)
			throws XMLStreamException {
		// create unionOf element
		writer.writeStartElement(JGraLab2OWL.owlNS, "unionOf");
		writer.writeAttribute(JGraLab2OWL.rdfNS, "parseType", "Collection");

		// create owl:Class element for every direct subclass of aec and build
		// the subtree
		for (AttributedElementClass subclass : aec.getDirectSubClasses()) {
			writeOwlClassEmptyElement();

			if (subclass instanceof EdgeClass) {
				writer.writeAttribute(JGraLab2OWL.rdfNS, "about", subclass
						.getQualifiedName()
						+ edgeClassNameSuffix);
			} else {
				writer.writeAttribute(JGraLab2OWL.rdfNS, "about", subclass
						.getQualifiedName());
			}
		}

		writer.writeEndElement();
	}

	/**
	 * Writes an element {@code <owl:Class/>}.
	 * 
	 * @throws XMLStreamException
	 */
	private void writeOwlClassEmptyElement() throws XMLStreamException {
		writer.writeEmptyElement(JGraLab2OWL.owlNS, "Class");
	}

	/**
	 * Writes an element {@code <owl:Class rdf:ID = }<i>id</i> {@code />}
	 * 
	 * @param id
	 *            The value for the {@code rdf:ID"} attribute.
	 * @throws XMLStreamException
	 */
	private void writeOwlClassEmptyElement(String id) throws XMLStreamException {
		writer.writeEmptyElement(JGraLab2OWL.owlNS, "Class");
		writer.writeAttribute(JGraLab2OWL.rdfNS, "ID", id);
	}

	/**
	 * Writes an element {@code <owl:Class rdf:ID = }<i>id</i> {@code >}
	 * 
	 * @param id
	 *            The value for the {@code rdf:ID"} attribute.
	 * @throws XMLStreamException
	 */
	private void writeOwlClassStartElement(String id) throws XMLStreamException {
		writer.writeStartElement(JGraLab2OWL.owlNS, "Class");
		writer.writeAttribute(JGraLab2OWL.rdfNS, "ID", id);
	}

	/**
	 * Writes an element {@code <owl:ObjectProperty>}.
	 * 
	 * @throws XMLStreamException
	 */
	private void writeOwlObjectPropertyStartElement() throws XMLStreamException {
		writer.writeStartElement(JGraLab2OWL.owlNS, "ObjectProperty");
	}

	/**
	 * Writes an element {@code <owl:ObjectProperty rdf:ID = } <i>id</i>{@code
	 * >}
	 * 
	 * @param id
	 *            The value for the {@code rdf:ID} attribute.
	 * @throws XMLStreamException
	 */
	private void writeOwlObjectPropertyStartElement(String id)
			throws XMLStreamException {
		writer.writeStartElement(JGraLab2OWL.owlNS, "ObjectProperty");
		writer.writeAttribute(JGraLab2OWL.rdfNS, "ID", id);
	}

	/**
	 * Writes an element {@code <owl:DatatypeProperty rdf:ID = } <i>id</i>
	 * {@code >}
	 * 
	 * @param id
	 *            The value for the {@code rdf:ID} attribute.
	 * @throws XMLStreamException
	 */
	private void writeOwlDatatypePropertyStartElement(String id)
			throws XMLStreamException {
		writer.writeStartElement(JGraLab2OWL.owlNS, "DatatypeProperty");
		writer.writeAttribute(JGraLab2OWL.rdfNS, "ID", id);
	}

	/**
	 * Writes an element {@code <rdfs:domain rdf:resource = } <i>resource</i>
	 * {@code />}
	 * 
	 * @param resource
	 *            The value for the {@code rdf:resource} attribute.
	 * @throws XMLStreamException
	 */
	private void writeRdfsDomainEmptyElement(String resource)
			throws XMLStreamException {
		writer.writeEmptyElement(JGraLab2OWL.rdfsNS, "domain");
		writer.writeAttribute(JGraLab2OWL.rdfNS, "resource", resource);
	}

	/**
	 * Writes an element {@code <rdfs:range/>}.
	 * 
	 * @throws XMLStreamException
	 */
	private void writeRdfsRangeEmptyElement() throws XMLStreamException {
		writer.writeEmptyElement(JGraLab2OWL.rdfsNS, "range");
	}

	/**
	 * Writes an element {@code <rdfs:range rdf:resource = } <i>resource</i>
	 * {@code />}
	 * 
	 * @param resource
	 *            The value for the {@code rdf:resource} attribute.
	 * @throws XMLStreamException
	 */
	private void writeRdfsRangeEmptyElement(String resource)
			throws XMLStreamException {
		writer.writeEmptyElement(JGraLab2OWL.rdfsNS, "range");
		writer.writeAttribute(JGraLab2OWL.rdfNS, "resource", resource);
	}

	/**
	 * Writes an element {@code <rdf:type rdf:resource = } <i>resource</i>
	 * {@code />}
	 * 
	 * @param resource
	 *            The value for the {@code rdf:resource} attribute.
	 * @throws XMLStreamException
	 */
	private void writeRdfTypeEmptyElement(String resource)
			throws XMLStreamException {
		writer.writeEmptyElement(JGraLab2OWL.rdfNS, "type");
		writer.writeAttribute(JGraLab2OWL.rdfNS, "resource", resource);
	}

	/**
	 * Writes an Element {@code <rdfs:subClassOf>}.
	 * 
	 * @throws XMLStreamException
	 */
	private void writeRdfsSubClassOfStartElement() throws XMLStreamException {
		writer.writeStartElement(JGraLab2OWL.rdfsNS, "subClassOf");
	}

	/**
	 * Writes an Element {@code <rdfs:subClassOf rdf:resource = }
	 * <i>resource</i>{@code />}
	 * 
	 * @param resource
	 *            The value for the {@code rdf:resource} attribute.
	 * @throws XMLStreamException
	 */
	private void writeRdfsSubClassOfEmptyElement(String resource)
			throws XMLStreamException {
		writer.writeEmptyElement(JGraLab2OWL.rdfsNS, "subClassOf");
		writer.writeAttribute(JGraLab2OWL.rdfNS, "resource", resource);
	}

	/**
	 * Writes an Element {@code <rdfs:subPropertyOf rdf:resource = * }
	 * <i>resource</i>{@code />}
	 * 
	 * @param resource
	 *            The value for the {@code rdf:resource} attribute.
	 * @throws XMLStreamException
	 */
	private void writeRdfsSubPropertyOfEmptyElement(String resource)
			throws XMLStreamException {
		writer.writeEmptyElement(JGraLab2OWL.rdfsNS, "subPropertyOf");
		writer.writeAttribute(JGraLab2OWL.rdfNS, "resource", resource);
	}

	/**
	 * Writes an Element {@code <owl:Restriction>}.
	 * 
	 * @throws XMLStreamException
	 */
	private void writeOwlRestrictionStartElement() throws XMLStreamException {
		writer.writeStartElement(JGraLab2OWL.owlNS, "Restriction");
	}

	/**
	 * Writes an Element {@code <owl:onProperty rdf:resource = } <i>resource</i>
	 * {@code />}
	 * 
	 * @param resource
	 *            The value for the {@code rdf:resource} attribute.
	 * @throws XMLStreamException
	 */
	private void writeOwlOnPropertyEmptyElement(String resource)
			throws XMLStreamException {
		writer.writeEmptyElement(JGraLab2OWL.owlNS, "onProperty");
		writer.writeAttribute(JGraLab2OWL.rdfNS, "resource", resource);
	}

	/**
	 * Writes an Element {@code <owl:inverseOf rdf:resource = } <i>resource</i>
	 * {@code />}
	 * 
	 * @param resource
	 *            The value for the {@code rdf:resource} attribute.
	 * @throws XMLStreamException
	 */
	private void writeOwlInverseOfEmptyElement(String resource)
			throws XMLStreamException {
		writer.writeEmptyElement(JGraLab2OWL.owlNS, "inverseOf");
		writer.writeAttribute(JGraLab2OWL.rdfNS, "resource", resource);
	}

	/**
	 * Writes an Element {@code <owl:cardinality rdf:datatype =
	 * "http://www.w3.org/2001/XMLSchema#nonNegativeInteger">}.
	 * 
	 * @throws XMLStreamException
	 */
	private void writeOwlCardinalityStartElement() throws XMLStreamException {
		writer.writeStartElement(JGraLab2OWL.owlNS, "cardinality");
		writer.writeAttribute(JGraLab2OWL.rdfNS, "datatype", JGraLab2OWL.xsdNS
				+ "nonNegativeInteger");
	}

	/**
	 * Writes an Element {@code <owl:maxCardinality rdf:datatype =
	 * "http://www.w3.org/2001/XMLSchema#nonNegativeInteger">}.
	 * 
	 * @throws XMLStreamException
	 */
	private void writeOwlMaxCardinalityStartElement() throws XMLStreamException {
		writer.writeStartElement(JGraLab2OWL.owlNS, "maxCardinality");
		writer.writeAttribute(JGraLab2OWL.rdfNS, "datatype", JGraLab2OWL.xsdNS
				+ "nonNegativeInteger");
	}

	/**
	 * Writes an Element {@code <owl:minCardinality rdf:datatype =
	 * "http://www.w3.org/2001/XMLSchema#nonNegativeInteger">}.
	 * 
	 * @throws XMLStreamException
	 */
	private void writeOwlMinCardinalityStartElement() throws XMLStreamException {
		writer.writeStartElement(JGraLab2OWL.owlNS, "minCardinality");
		writer.writeAttribute(JGraLab2OWL.rdfNS, "datatype", JGraLab2OWL.xsdNS
				+ "nonNegativeInteger");
	}
}
