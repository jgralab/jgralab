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

package de.uni_koblenz.jgralab.utilities.jgralab2owl;

import java.util.List;
import java.util.Set;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import org.w3c.dom.Document;

import de.uni_koblenz.jgralab.AttributedElement;
import de.uni_koblenz.jgralab.Edge;
import de.uni_koblenz.jgralab.Graph;
import de.uni_koblenz.jgralab.ProgressFunction;
import de.uni_koblenz.jgralab.Vertex;
import de.uni_koblenz.jgralab.schema.AggregationKind;
import de.uni_koblenz.jgralab.schema.Attribute;
import de.uni_koblenz.jgralab.schema.AttributedElementClass;
import de.uni_koblenz.jgralab.schema.Domain;
import de.uni_koblenz.jgralab.schema.EdgeClass;
import de.uni_koblenz.jgralab.schema.ListDomain;
import de.uni_koblenz.jgralab.schema.RecordDomain;
import de.uni_koblenz.jgralab.schema.Schema;
import de.uni_koblenz.jgralab.schema.SetDomain;
import de.uni_koblenz.jgralab.schema.RecordDomain.RecordComponent;

class Graph2OWLConcepts {

	/**
	 * the {@link XMLStreamWriter} used to write the OWL document
	 */
	private XMLStreamWriter writer;

	/**
	 * If {@code true}, an EdgeClass is converted to exactly one property,
	 * discarding possible attributes. If {@code false}, an EdgeClass is
	 * converted to an OWL class and two Properties.
	 */
	private boolean edgeClasses2Props;

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
	 * The name of the property linking the Graph Individual to the Vertex
	 * Individuals
	 */
	private String graphContainsVertexPropName;

	/**
	 * The name of the property linking the Vertex Individual to the Graph
	 * Individuals
	 */
	private String vertexIsInGraphPropName;

	/**
	 * The name of the property linking the Graph Individual to the Edge
	 * Individuals
	 */
	private String graphContainsEdgePropName;

	/**
	 * The name of the property linking the Edge Individual to the Graph
	 * Individuals
	 */
	private String edgeIsInGraphPropName;

	/**
	 * A prefix used for Properties linking an Edge Class to its role
	 */
	private String edgeRolePropPrefix;

	/**
	 * An Infix used for Properties linking Edge Individuals to Attributes
	 * (Datatypes or Individuals)
	 */
	private String edgeAttrPropInfix;

	/**
	 * Creates an instance of {@code Graph2OWL}, assigns values to the member
	 * variables.
	 * 
	 * @param writer
	 *            The {@link XMLStreamWriter} to be used
	 * @param edgeClasses2Properties
	 *            If {@code true}, an EdgeClass is converted to exactly one
	 *            property, discarding possible attributes and rolenames. If
	 *            {@code false}, an EdgeClass is converted to an OWL class and
	 *            two Properties.
	 * @param appendSuffix2EdgeClassName
	 *            If {@code true}, the suffix {@code EC} is appended to each OWL
	 *            construct representing an EdgeClass.
	 * @param pf
	 *            An instance of {@code ProgressFunction} to display the
	 *            progress of the conversion in a status bar.
	 */
	Graph2OWLConcepts(XMLStreamWriter writer, boolean edgeClasses2Props,
			boolean appendSuffix2EdgeClassName, ProgressFunction pf) {
		this.writer = writer;
		this.edgeClasses2Props = edgeClasses2Props;
		if (appendSuffix2EdgeClassName) {
			edgeClassNameSuffix = "EC";
		} else {
			edgeClassNameSuffix = "";
		}

		graphContainsVertexPropName = "graphContainsVertex";
		vertexIsInGraphPropName = "vertexIsInGraph";
		graphContainsEdgePropName = "graphContainsEdge" + edgeClassNameSuffix;
		edgeIsInGraphPropName = "edge" + edgeClassNameSuffix + "IsInGraph";
		edgeRolePropPrefix = "edge" + edgeClassNameSuffix;
		edgeAttrPropInfix = edgeClassNameSuffix + "Has";
	}

	/**
	 * Initializes {@code pf} and starts the conversion of the {@code Graph
	 * graph} to OWL.
	 * 
	 * @param g
	 *            The graph which shall be converted to OWL.
	 * @param pf
	 *            An instance of {@code ProgressFunction} to display the
	 *            progress of the conversion in a status bar.
	 * @throws XMLStreamException
	 * 
	 * @see #convertGraph(Graph g, ProgressFunction pf)
	 */
	protected void saveGraph(Graph g, ProgressFunction pf)
			throws XMLStreamException {
		// initialize progress bar for graph
		int graphElements = 0, currentCount = 0, interval = 1;
		if (pf != null) {
			pf.init(g.getVCount() + g.getECount());
			interval = (int) pf.getUpdateInterval();
		}

		// update progress bar
		if (pf != null) {
			graphElements++;
			currentCount++;
			if (currentCount == interval) {
				pf.progress(graphElements);
				currentCount = 0;
			}
		}

		convertGraph(g, pf);

		// finish progress bar
		if (pf != null) {
			pf.finished();
		}
	}

	/**
	 * Converts the {@code Graph g} to an individual of the OWL class
	 * representing the graph's {@code AttributedElementClass}. The individual
	 * contains properties relating the graph to its attributes and its
	 * contained vertices and edges.<br>
	 * <br>
	 * XML code written: <br>
	 * 
	 * <pre>
	 *     &lt;&lt;i&gt;g.getAttributedElementClass().getName()&lt;/i&gt; rdf:ID=&quot;&lt;i&gt;g.getId()&lt;/i&gt;&quot;&gt;
	 *         &lt;i&gt;convertAttributeValue(g, g.getAttributedElementClass().getAttributeList().toArray(new Attribute[0])[0])&lt;/i&gt;
	 *         &lt;i&gt;convertAttributeValue(g, g.getAttributedElementClass().getAttributeList().toArray(new Attribute[0])[1])&lt;/i&gt;
	 *         &lt;i&gt;...&lt;/i&gt;
	 * </pre>
	 * 
	 * ------------------------------------<br>
	 * This part is written for every {@code Vertex v} contained in {@code g},
	 * where <i>vElemId</i> is the id of the individual representing that
	 * {@code Vertex}.
	 * 
	 * <pre>
	 *         &lt;graphContainsVertex rdf:resource=&quot;#&lt;i&gt;vElemId&lt;/i&gt;&quot;/&gt;
	 * </pre>
	 * 
	 * If {@code edgeClasses2Properties = false}, this part is written for every
	 * {@code Edge e} contained in {@code g}, where <i>eElemId</i> is the id of
	 * the individual representing that {@code Edge}.
	 * 
	 * <pre>
	 *         &lt;graphContainsEdge rdf:resource=&quot;#&lt;i&gt;eElemId&lt;/i&gt;&quot;/&gt;
	 * </pre>
	 * 
	 * ------------------------------------<br>
	 * 
	 * <pre>
	 *    &lt;/&lt;i&gt;g.getAttributedElementClass().getName()&lt;/i&gt;&gt;
	 * </pre>
	 * 
	 * @param g
	 *            The graph which shall be converted to OWL.
	 * @param pf
	 *            An instance of {@code ProgressFunction} to display the
	 *            progress of the conversion in a status bar.
	 * @throws XMLStreamException
	 * 
	 * @see #convertAttributeValue(AttributedElement ownerAe, Attribute attr)
	 */
	private void convertGraph(Graph g, ProgressFunction pf)
			throws XMLStreamException {
		String eElemId;
		String vElemId;
		String gId = g.getId();
		String hashedGId = "#" + gId;
		String attrName;

		// create Individual for Graph g
		writeClassStartElement(
				g.getAttributedElementClass().getQualifiedName(), hashedGId);

		// convert attributes of g

		for (Attribute attr : g.getAttributedElementClass().getAttributeList()) {
			attrName = attr.getName();

			if (g.getAttribute(attrName) != null) {
				convertAttributeValue(g, attr, attrName);
			}
		}

		// append Individual for Graph g to element <rdf:RDF>
		// rdfElem.appendChild(gElem);

		// write "graphContains..." properties
		for (Vertex v : g.vertices()) {
			vElemId = HelperMethods.firstToLowerCase(v
					.getAttributedElementClass().getQualifiedName())
					+ "_" + gId + "_" + v.getId();
			writeIndividualObjectPropEmptyElement(graphContainsVertexPropName,
					"#" + vElemId);
		}

		if (!edgeClasses2Props) {
			for (Edge e : g.edges()) {
				eElemId = HelperMethods.firstToLowerCase(e
						.getAttributedElementClass().getQualifiedName())
						+ edgeClassNameSuffix + "_" + gId + "_" + e.getId();
				writeIndividualObjectPropEmptyElement(
						graphContainsEdgePropName, "#" + eElemId);
			}
		}

		writer.writeEndElement();

		// convert vertices
		for (Vertex v : g.vertices()) {
			vElemId = HelperMethods.firstToLowerCase(v
					.getAttributedElementClass().getQualifiedName())
					+ "_" + gId + "_" + v.getId();

			convertVertex(hashedGId, v, vElemId, pf);
		}

		if (!edgeClasses2Props) {
			// convert edges
			for (Edge e : g.edges()) {
				eElemId = HelperMethods.firstToLowerCase(e
						.getAttributedElementClass().getQualifiedName())
						+ edgeClassNameSuffix + "_" + gId + "_" + e.getId();

				convertEdge(hashedGId, e, eElemId, pf);
			}
		}
	}

	/**
	 * Converts the {@code Vertex v} to an individual of the OWL class
	 * representing the vertex' {@code AttributedElementClass}. {@code vElemId}
	 * specifies the individual's id. The individual contains properties
	 * relating it to its attributes, its containing graph, and its incident
	 * edges.<br>
	 * <br>
	 * XML code written if {@code appendSuffix2EdgeClassName = false}: <br>
	 * 
	 * <pre>
	 *     &lt;&lt;i&gt;v.getAttributedElementClass().getName()&lt;/i&gt; rdf:ID=&quot;&lt;i&gt;vElemId&lt;/i&gt;&quot;&gt;
	 *         &lt;i&gt;convertAttributeValue(v, v.getAttributedElementClass().getAttributeList().toArray(new Attribute[0])[0])&lt;/i&gt;
	 *         &lt;i&gt;convertAttributeValue(v, v.getAttributedElementClass().getAttributeList().toArray(new Attribute[0])[1])&lt;/i&gt;
	 *         &lt;i&gt;...&lt;/i&gt;
	 *         &lt;vertexIsInGraph rdf:resource=&quot;#&lt;i&gt;v.getGraph().getId()&lt;/i&gt;&quot;/&gt;
	 * </pre>
	 * 
	 * ------------------------------------<br>
	 * This part is only written if {@code edgeClasses2Properties = false} and
	 * {@code e} is an {@code Edge} incident to {@code v} with {@code v} on its
	 * "from" side. <i>eElemId</i> is the id of the individual representing that
	 * {@code Edge}:
	 * 
	 * <pre>
	 *         &lt;&lt;i&gt;e.getAttributedElementClass().getName() + edgeClassNameSuffix&lt;/i&gt;Out rdf:resource=&quot;#&lt;i&gt;eElemId&lt;/i&gt;&quot;/&gt;
	 * </pre>
	 * 
	 * If {@code v} is on the "to" side, replace
	 * <i>e.getAttributedElementClass().getName() + edgeClassNameSuffix</i>
	 * {@code Out} with <i>e.getAttributedElementClass().getName() +
	 * edgeClassNameSuffix</i>{@code In}.<br>
	 * ------------------------------------<br>
	 * This part is only written if {@code edgeClasses2Properties = true} and
	 * {@code e} is an {@code Edge} incident to {@code v} with {@code v} on its
	 * "from" side. <i>eElemId</i> is the id of the individual representing that
	 * {@code Edge}:
	 * 
	 * <pre>
	 *         &lt;&lt;i&gt;e.getAttributedElementClass().getName() + edgeClassNameSuffix&lt;/i&gt; rdf:resource=&quot;#&lt;i&gt;eElemId&lt;/i&gt;&quot;/&gt;
	 * </pre>
	 * 
	 * If {@code v} is on the "to" side, replace
	 * <i>e.getAttributedElementClass().getName() + edgeClassNameSuffix</i> with
	 * <i>e.getAttributedElementClass().getName() + edgeClassNameSuffix</i>
	 * {@code -of}.<br>
	 * ------------------------------------<br>
	 * 
	 * <pre>
	 *     &lt;/&lt;i&gt;v.getAttributedElementClass().getName()&lt;/i&gt;&gt;
	 * </pre>
	 * 
	 * @param v
	 *            The vertex which shall be converted.
	 * @param vElemId
	 *            The id of the individual representing the vertex in OWL.
	 * @param pf
	 *            An instance of {@code ProgressFunction} to display the
	 *            progress of the conversion in a status bar.
	 * @throws XMLStreamException
	 * 
	 * @see #convertAttributeValue(AttributedElement ownerAe, Attribute attr)
	 */
	private void convertVertex(String hashedGId, Vertex v, String vElemId,
			ProgressFunction pf) throws XMLStreamException {
		AttributedElementClass vc = v.getAttributedElementClass();
		Vertex incidentVertex;
		String attrName, eElemId, eSuffixedLowerCaseQName;

		// create the Individual representing v
		writeClassStartElement(vc.getQualifiedName(), "#" + vElemId);

		// convert Attributes of v

		for (Attribute attr : vc.getAttributeList()) {
			attrName = attr.getName();

			if (v.getAttribute(attrName) != null) {
				convertAttributeValue(v, attr, attrName);
			}
		}

		writeIndividualObjectPropEmptyElement(vertexIsInGraphPropName,
				hashedGId);

		// create individual properties referring to individuals representing
		// incident edges
		for (Edge e : v.incidences()) {
			eSuffixedLowerCaseQName = HelperMethods.firstToLowerCase(e
					.getAttributedElementClass().getQualifiedName())
					+ edgeClassNameSuffix;

			eElemId = eSuffixedLowerCaseQName
					+ String.valueOf(e.getNormalEdge().getId());

			if (edgeClasses2Props) {
				if (e.getAlpha() == v) {
					incidentVertex = e.getOmega();

					writeIndividualObjectPropEmptyElement(
							eSuffixedLowerCaseQName,
							"#"
									+ HelperMethods
											.firstToLowerCase(incidentVertex
													.getAttributedElementClass()
													.getQualifiedName())
									+ incidentVertex.getId());
				} else {
					incidentVertex = e.getAlpha();

					writeIndividualObjectPropEmptyElement(
							eSuffixedLowerCaseQName + "-of",
							"#"
									+ HelperMethods
											.firstToLowerCase(incidentVertex
													.getAttributedElementClass()
													.getQualifiedName())
									+ incidentVertex.getId());
				}
			} else {
				if (e.getAlpha() == v) {
					writeIndividualObjectPropEmptyElement(
							eSuffixedLowerCaseQName + "Out", "#" + eElemId);
				} else {
					writeIndividualObjectPropEmptyElement(
							eSuffixedLowerCaseQName + "In", "#" + eElemId);
				}
			}
		}

		writer.writeEndElement();
	}

	/**
	 * Converts the {@code Edge e} to an individual of the OWL class
	 * representing the edge's {@code AttributedElementClass}. {@code eElemId}
	 * specifies the individual's id. The individual contains properties
	 * relating it to its attributes, its containing graph, the role names on
	 * its "from" and "to" sides and, if {@code e} constitutes an {@code
	 * Aggregation} or {@code Composition}, to the {@code Vertex} forming the
	 * aggregate.<br>
	 * <br>
	 * XML code written if: <br>
	 * 
	 * <pre>
	 *     &lt;&lt;i&gt;e.getAttributedElementClass().getName() + edgeClassNameSuffix&lt;/i&gt; rdf:ID=&quot;&lt;i&gt;eElemId&lt;/i&gt;&quot;&gt;
	 *         &lt;i&gt;convertAttributeValue(e, e.getAttributedElementClass().getAttributeList().toArray(new Attribute[0])[0])&lt;/i&gt;
	 *         &lt;i&gt;convertAttributeValue(e, e.getAttributedElementClass().getAttributeList().toArray(new Attribute[0])[1])&lt;/i&gt;
	 *         &lt;i&gt;...&lt;/i&gt;
	 *         &lt;edgeIsInGraph rdf:resource=&quot;#&lt;i&gt;e.getGraph().getId()&lt;/i&gt;&quot;/&gt;
	 *         &lt;edgeFromRole rdf:datatype=&quot;http://www.w3.org/2001/XMLSchema#string&quot;&gt;&lt;i&gt;e.getAttributedElementClass().getFromRolename()&lt;/i&gt;&lt;/edgeFromRole&gt;
	 *         &lt;edgeToRole rdf:datatype=&quot;http://www.w3.org/2001/XMLSchema#string&quot;&gt;&lt;i&gt;e.getAttributedElementClass()).getToRolename()&lt;/i&gt;&lt;/edgeToRole&gt;
	 * </pre>
	 * 
	 * ------------------------------------<br>
	 * This part is only written if {@code e} is an {@code Aggregation} or
	 * {@code Composition} and the {@code Vertex} on the "from" side is the
	 * aggregate, where <i>fromElemId</i> is the id is the id of the individual
	 * representing that {@code Vertex}:
	 * 
	 * <pre>
	 *         &lt;aggregate rdf:resource=&quot;#&lt;i&gt;fromElemId&lt;/i&gt;&quot;/&gt;
	 * </pre>
	 * 
	 * If the aggregate is on the "to" side, replace <i>fromElemId</i> with
	 * <i>toElemId</i>. ------------------------------------<br>
	 * 
	 * <pre>
	 *     &lt;/&lt;i&gt;e.getAttributedElementClass().getName() + edgeClassNameSuffix&lt;/i&gt;&gt;
	 * </pre>
	 * 
	 * @param e
	 *            The edge which shall be converted.
	 * @param eElemId
	 *            The id of the individual representing the edge in OWL.
	 * @param pf
	 *            An instance of {@code ProgressFunction} to display the
	 *            progress of the conversion in a status bar.
	 * @throws XMLStreamException
	 * 
	 * @see #convertAttributeValue(AttributedElement ownerAe, Attribute attr)
	 */
	private void convertEdge(String hashedGId, Edge e, String eElemId,
			ProgressFunction pf) throws XMLStreamException {
		AttributedElementClass ec = e.getAttributedElementClass();
		Vertex fromVertex = e.getAlpha();
		Vertex toVertex = e.getOmega();
		String attrName;

		writeClassStartElement(ec.getQualifiedName(), "#" + eElemId);

		// compute the ids of the individuals representing the out and the in
		// vertices
		String fromElemId = HelperMethods.firstToLowerCase(fromVertex
				.getAttributedElementClass().getQualifiedName())
				+ "_"
				+ fromVertex.getGraph().getId()
				+ "_"
				+ +fromVertex.getId();

		String toElemId = HelperMethods.firstToLowerCase(toVertex
				.getAttributedElementClass().getQualifiedName())
				+ "_" + toVertex.getGraph().getId() + "_" + toVertex.getId();

		// convert attributes of e

		for (Attribute attr : ec.getAttributeList()) {
			attrName = attr.getName();

			if (e.getAttribute(attrName) != null) {
				convertAttributeValue(e, attr, attrName);
			}
		}

		writeIndividualObjectPropEmptyElement(edgeIsInGraphPropName, hashedGId);

		// create properties for role names
		writeIndividualDatatypePropElement(edgeRolePropPrefix + "OutRole",
				JGraLab2OWL.xsdNS + "string", ((EdgeClass) ec).getFrom()
						.getRolename());
		writeIndividualDatatypePropElement(edgeRolePropPrefix + "InRole",
				JGraLab2OWL.xsdNS + "string", ((EdgeClass) ec).getTo()
						.getRolename());

		// create properties for aggregate if e is an Aggregation or Composition
		if (e.getOmegaSemantics() != AggregationKind.NONE) {
			writeIndividualObjectPropEmptyElement("aggregate", "#" + fromElemId);
		}
		if (e.getAlphaSemantics() != AggregationKind.NONE) {
			writeIndividualObjectPropEmptyElement("aggregate", "#" + toElemId);
		}

		writer.writeEndElement();
	}

	/**
	 * Converts the value of the {@code Attribute attr} to an individual of a
	 * Property. See
	 * {@link #writeAttributeIndividualObjectPropertyElement(String name, Object value, Domain dom)}
	 * and
	 * {@link #writeAttributeIndividualDatatypePropertyElement(String name, Object value, Domain dom)}
	 * for the created XML code.
	 * 
	 * @param ownerAe
	 *            The {@code AttributedElementClass} containing {@code attr}.
	 * @param attr
	 *            The {@code Attribute} which shall be converted.
	 * 
	 * @throws XMLStreamException
	 * 
	 * @see #writeAttributeIndividualObjectPropertyElement(String name, Object
	 *      value, Domain dom)
	 * @see #writeAttributeIndividualDatatypePropertyElement(String name, Object
	 *      value, Domain dom)
	 */
	private void convertAttributeValue(AttributedElement ownerAe,
			Attribute attr, String attrName) throws XMLStreamException {
		String attrPropertyName;
		Object value = ownerAe.getAttribute(attrName);

		AttributedElementClass owningAec = attr.getAttributedElementClass();

		// The name of the Property representing the attribute
		if (owningAec instanceof EdgeClass) {
			attrPropertyName = HelperMethods.firstToLowerCase(owningAec
					.getQualifiedName())
					+ edgeAttrPropInfix
					+ HelperMethods.firstToUpperCase(attrName);
		} else {
			attrPropertyName = HelperMethods.firstToLowerCase(owningAec
					.getQualifiedName())
					+ "Has" + HelperMethods.firstToUpperCase(attrName);
		}

		Domain dom = attr.getDomain();

		// if "attr" has a CompositeDomain as type (no Object)
		if (dom.isComposite()) {
			writeAttributeIndividualObjectPropElement(attrPropertyName, value,
					dom);
			// if "attr" has a BasicDomain as type
		} else {
			writeAttributeIndividualDatatypePropElement(attrPropertyName,
					value, dom);
		}
	}

	/**
	 * Writes an individual of an ObjectProperty representing the value of a
	 * composite {@code Attribute}. See
	 * {@link #writeListIndividualObjectPropertyElement(String name, Object value, Domain dom)}
	 * ,
	 * {@link #writeSetIndividualObjectPropertyElement(String name, Object value, Domain dom)}
	 * and
	 * {@link #writeRecordIndividualObjectPropertyElement(String name, Object value, Domain dom)}
	 * for the created XML code.
	 * 
	 * @param propName
	 *            The name of the ObjectProperty which shall be created.
	 * @param value
	 *            The value of the composite attribute which shall be converted.
	 * @param dom
	 *            The {@code Domain} of the composite attribute which shall be
	 *            converted.
	 * @throws XMLStreamException
	 * 
	 * @see #writeListIndividualObjectPropElement(String name, Object value,
	 *      Domain dom)
	 * @see #writeSetIndividualObjectPropElement(String name, Object value,
	 *      Domain dom)
	 * @see #writeRecordIndividualObjectPropElement(String name, Object value,
	 *      Domain dom)
	 */
	private void writeAttributeIndividualObjectPropElement(String propName,
			Object value, Domain dom) throws XMLStreamException {
		if (dom.getTGTypeName(null).startsWith("List<")) {
			writeListIndividualObjectPropElement(propName, value, dom);
		} else if (dom.getTGTypeName(null).startsWith("Set<")) {
			writeSetIndividualObjectPropElement(propName, value, dom);
		} else {
			writeRecordIndividualObjectPropElement(propName, value, dom);
		}
	}

	/**
	 * Writes an individual of an ObjectProperty representing the value of an
	 * {@code Attribute} of a list type. <br>
	 * XML-code written if the members of the list are of composite type: <br>
	 * 
	 * <pre>
	 *     &lt;&lt;i&gt;propName&lt;/i&gt;&gt;
	 *         &lt;ListElement&gt;
	 *             &lt;listElementHasObject &lt;i&gt;...&lt;/i&gt;
	 *             &lt;hasNextListElement&gt;
	 *                 &lt;ListElement&gt;
	 *                     &lt;listElementHasObject &lt;i&gt;...&lt;/i&gt;
	 *                     &lt;hasNextListElement&gt;
	 *                         &lt;i&gt;...&lt;/i&gt;
	 *                     &lt;/hasNextListElement&gt;
	 *             &lt;/hasNextListElement&gt;
	 *         &lt;ListElement&gt;
	 *     &lt;&lt;i&gt;propName&lt;/i&gt;&gt;
	 * </pre>
	 * 
	 * XML-code written if the members of the list are of basic type: <br>
	 * 
	 * <pre>
	 *     &lt;&lt;i&gt;propName&lt;/i&gt;&gt;
	 *         &lt;ListElement&gt;
	 *             &lt;listElementHasDatatype &lt;i&gt;...&lt;/i&gt;
	 *             &lt;hasNextListElement&gt;
	 *                 &lt;ListElement&gt;
	 *                     &lt;listElementHasDatatype &lt;i&gt;...&lt;/i&gt;
	 *                     &lt;hasNextListElement&gt;
	 *                         &lt;i&gt;...&lt;/i&gt;
	 *                     &lt;/hasNextListElement&gt;
	 *             &lt;/hasNextListElement&gt;
	 *         &lt;/ListElement&gt;
	 *     &lt;/&lt;i&gt;propName&lt;/i&gt;&gt;
	 * </pre>
	 * 
	 * The child elements and/or attributes of the individuals {@code <Set>} are
	 * determined by yet another call of
	 * {@link #writeAttributeIndividualObjectPropertyElement(String propertyName, Object value, Domain dom)}
	 * or
	 * {@link #writeAttributeIndividualDatatypePropertyElement(String propertyName, Object value, Domain dom)}
	 * , respectively.
	 * 
	 * @param propName
	 *            The name of the ObjectProperty which shall be created.
	 * @param value
	 *            The value of the attribute of type List which shall be
	 *            converted.
	 * @param dom
	 *            The {@code Domain} of the attribute of type List which shall
	 *            be converted.
	 * @throws XMLStreamException
	 * 
	 * @see #writeAttributeIndividualObjectPropElement(String propertyName,
	 *      Object value, Domain dom)
	 * @see #writeAttributeIndividualDatatypePropElement(String propertyName,
	 *      Object value, Domain dom)
	 */
	private void writeListIndividualObjectPropElement(String propName,
			Object value, Domain dom) throws XMLStreamException {
		Object componentValue;

		// get the base domain of the list
		Domain baseDomain = ((ListDomain) dom).getBaseDomain();

		writer.writeStartElement(propName);

		// for each value inside the list
		for (int i = 0; i < ((List<?>) value).size(); i++) {
			componentValue = ((List<?>) value).get(i);

			// create an individual of owl-class "ListElement" and append it as
			// child of
			// the "hasNextListElement" individual property of the last
			// iteration
			writer.writeStartElement("ListElement");

			// create individual properties for the ListElement's value
			if (baseDomain.isComposite()) {
				writeAttributeIndividualObjectPropElement(
						"listElementHasObject", componentValue, baseDomain);
			} else {
				writeAttributeIndividualDatatypePropElement(
						"listElementHasDatatype", componentValue, baseDomain);
			}

			// if i is not the index of the last ListElement, create property
			// "hasNextListElement"
			if (i < ((List<?>) value).size() - 1) {
				writer.writeStartElement("hasNextListElement");
			}
		}

		for (int i = 0; i < ((List<?>) value).size() - 1; i++) {
			writer.writeEndElement();
		}

		writer.writeEndElement();
	}

	/**
	 * Writes an individual of an ObjectProperty representing the value of an
	 * {@code Attribute} of a set type. <br>
	 * XML-code written if the members of the set members are of composite type: <br>
	 * 
	 * <pre>
	 *     &lt;&lt;i&gt;propName&lt;/i&gt;&gt;
	 *         &lt;Set&gt;
	 *             &lt;setHasObject &lt;i&gt;...&lt;/i&gt;
	 *         &lt;/Set&gt;
	 *     &lt;/&lt;i&gt;propName&lt;/i&gt;&gt;
	 * </pre>
	 * 
	 * XML-code written if the members of the set members are of basic type: <br>
	 * 
	 * <pre>
	 *     &lt;&lt;i&gt;propName&lt;/i&gt;&gt;
	 *         &lt;Set&gt;
	 *             &lt;setHasDatatype &lt;i&gt;...&lt;/i&gt;
	 *         &lt;/Set&gt;
	 *     &lt;/&lt;i&gt;propName&lt;/i&gt;&gt;
	 * </pre>
	 * 
	 * The child elements and/or attributes of the individuals {@code <Set>} are
	 * determined by yet another call of
	 * {@link #writeAttributeIndividualObjectPropertyElement(String propertyName, Object value, Domain dom)}
	 * or
	 * {@link #writeAttributeIndividualDatatypePropertyElement(String propertyName, Object value, Domain dom)}
	 * , respectively.
	 * 
	 * @param propertyName
	 *            The name of the ObjectProperty which shall be created.
	 * @param value
	 *            The value of the attribute of type Set which shall be
	 *            converted.
	 * @param dom
	 *            The {@code Domain} of the attribute of type Set which shall be
	 *            converted.
	 * @throws XMLStreamException
	 * 
	 * @see #writeAttributeIndividualObjectPropertyElement(String propName,
	 *      Object value, Domain dom)
	 * @see #writeAttributeIndividualDatatypePropertyElement(String
	 *      propertyName, Object value, Domain dom)
	 */
	private void writeSetIndividualObjectPropElement(String propName,
			Object value, Domain dom) throws XMLStreamException {
		// get the base domain of the Set
		Domain baseDomain = ((SetDomain) dom).getBaseDomain();

		writer.writeStartElement(propName);
		writer.writeStartElement("Set");

		// if the base domain is a composite domain
		if (baseDomain.isComposite()) {
			// for each value inside the Set
			for (Object componentValue : (Set<?>) value) {
				writeAttributeIndividualObjectPropElement("setHasObject",
						componentValue, baseDomain);
			}
			// if the base domain is a basic domain
		} else {
			// for each value inside the Set
			for (Object componentValue : (Set<?>) value) {
				writeAttributeIndividualDatatypePropElement("setHasDatatype",
						componentValue, baseDomain);
			}
		}

		writer.writeEndElement();
		writer.writeEndElement();
	}

	/**
	 * Writes an individual of an ObjectProperty representing the value of an
	 * {@code Attribute} of a record type. <br>
	 * XML-code written, where <i>componentNameX</i> denotes the names of the
	 * components: <br>
	 * 
	 * <pre>
	 *     &lt;&lt;i&gt;propName&lt;/i&gt;&gt;
	 *         &lt;&lt;i&gt;dom.getName()&lt;/i&gt;&gt;
	 *             &lt;&lt;i&gt;dom.getName()&lt;/i&gt;Has&lt;i&gt;componentName1&lt;/i&gt; &lt;i&gt;...&lt;/i&gt;
	 *             &lt;&lt;i&gt;dom.getName()&lt;/i&gt;Has&lt;i&gt;componentName2&lt;/i&gt; &lt;i&gt;...&lt;/i&gt;
	 *         &lt;/&lt;i&gt;dom.getName()&lt;/i&gt;&gt;
	 *     &lt;/&lt;i&gt;propName&lt;/i&gt;&gt;
	 * </pre>
	 * 
	 * The child elements and/or attributes of the individuals {@code <}
	 * <i>dom.getName() </i>{@code Has}<i>componentNameX</i> are determined by a
	 * call of
	 * {@link #writeAttributeIndividualObjectPropertyElement(String propertyName, Object value, Domain dom)}
	 * or
	 * {@link #writeAttributeIndividualDatatypePropertyElement(String propertyName, Object value, Domain dom)}
	 * , respectively<br>
	 * <br>
	 * An example:<br>
	 * 
	 * <pre>
	 *     &lt;carParkHasParking&gt;
	 *         &lt;Parking&gt;
	 *             &lt;parkingHasCost rdf:datatype=&quot;http://www.w3.org/2001/XMLSchema#double&quot;&gt;3.5&lt;/parkingHasCost&gt;
	 *             &lt;parkingHasDate rdf:datatype=&quot;http://www.w3.org/2001/XMLSchema#string&quot;&gt;08.03.2006&lt;/parkingHasDate&gt;
	 *             &lt;parkingHasDuration rdf:datatype=&quot;http://www.w3.org/2001/XMLSchema#int&quot;&gt;90&lt;/parkingHasDuration&gt;
	 *         &lt;/Parking&gt;
	 *     &lt;/carParkHasParking&gt;
	 * </pre>
	 * 
	 * @param propName
	 *            The name of the ObjectProperty which shall be created.
	 * @param value
	 *            The value of the attribute of type Record which shall be
	 *            converted.
	 * @param dom
	 *            The {@code Domain} of the attribute of type Record which shall
	 *            be converted.
	 * @throws XMLStreamException
	 * 
	 * @see #writesAttributeIndividualObjectPropertyElement(String propertyName,
	 *      Object value, Domain dom)
	 * @see #writesAttributeIndividualDatatypePropertyElement(String
	 *      propertyName, Object value, Domain dom)
	 */
	private void writeRecordIndividualObjectPropElement(String propName,
			Object value, Domain dom) throws XMLStreamException {
		writer.writeStartElement(propName);
		writer.writeStartElement(dom.getQualifiedName());

		// for every component of the Record
		for (RecordComponent component : ((RecordDomain) dom).getComponents()) {
			Object componentValue = null;

			// get the value of the record component
			try {
				componentValue = value.getClass().getField(component.getName())
						.get(value);
			} catch (Exception e) {
				e.printStackTrace();
			}

			// if the component is of composite type
			if (component.getDomain().isComposite()
					&& !component.getDomain().getTGTypeName(null).equals(
							"Object")) {
				writeAttributeIndividualObjectPropElement(HelperMethods
						.firstToLowerCase(dom.getQualifiedName())
						+ "Has"
						+ HelperMethods.firstToUpperCase(component.getName()),
						componentValue, component.getDomain());
				// if the component is of basic type
			} else {
				writeAttributeIndividualDatatypePropElement(HelperMethods
						.firstToLowerCase(dom.getQualifiedName())
						+ "Has"
						+ HelperMethods.firstToUpperCase(component.getName()),
						componentValue, component.getDomain());
			}
		}

		writer.writeEndElement();
		writer.writeEndElement();

		// return attrIndividualPropertyElem;
	}

	/**
	 * Writes an individual of a DatatypeProperty representing the value of an
	 * {@code Attribute} of a basic type. <br>
	 * XML-code written:<br>
	 * <br>
	 * 
	 * <pre>
	 *     &lt;&lt;i&gt;propName&lt;/i&gt; rdf:datatype=&quot;#&lt;i&gt;dom.getTGTypeName()&lt;/i&gt;&gt;&lt;i&gt;value.toString()&lt;/i&gt;&lt;/&lt;i&gt;propName&lt;/i&gt;&gt;
	 * </pre>
	 * 
	 * @param propName
	 *            The name of the DatatypeProperty which shall be created.
	 * @param value
	 *            The value of the attribute of basic type which shall be
	 *            converted.
	 * @param dom
	 *            The {@code Domain} of the attribute of basic type which shall
	 *            be converted.
	 * @throws XMLStreamException
	 */
	@SuppressWarnings("rawtypes")
	private void writeAttributeIndividualDatatypePropElement(String propName,
			Object value, Domain dom) throws XMLStreamException {
		if (dom.toString().contains("Enum")) {
			writer.writeEmptyElement(propName);
			writer.writeAttribute(JGraLab2OWL.rdfNS, "resource", "#"
					+ ((Enum) value).toString());
		} else {
			writer.writeStartElement(propName);

			if (dom.getTGTypeName(null).equals("String")) {
				writer.writeAttribute(JGraLab2OWL.rdfNS, "datatype",
						JGraLab2OWL.xsdNS + "string");
				writer.writeCharacters((String) value);
			} else {
				writer
						.writeAttribute(
								JGraLab2OWL.rdfNS,
								"datatype",
								JGraLab2OWL.xsdNS
										+ dom
												.getJavaAttributeImplementationTypeName(""));
				writer.writeCharacters(value.toString());
			}

			writer.writeEndElement();
		}
	}

	/**
	 * Writes an element {@code <}<i>owlClass</i> {@code rdf:ID = } <i>id</i>
	 * {@code />}, representing an individual.
	 * 
	 * @param owlClass
	 *            The element's tag.
	 * @param id
	 *            The value of the "rdf:ID" attribute.
	 * @throws XMLStreamException
	 */
	private void writeClassStartElement(String owlClass, String id)
			throws XMLStreamException {
		// create a CLASS
		writer.writeStartElement(JGraLab2OWL.owlNS, "Class");
		writer.writeAttribute(JGraLab2OWL.rdfNS, "about", id);

		// set the subclass PROPERTY
		writer.writeStartElement(JGraLab2OWL.rdfsNS, "subClassOf");

		writer.writeEmptyElement(JGraLab2OWL.owlNS, "Class");
		writer.writeAttribute(JGraLab2OWL.rdfNS, "about", "#" + owlClass);

		writer.writeEndElement();
	}

	/**
	 * Writes an element {@code <}<i>owlProperty</i> {@code rdf:resource = }
	 * <i>id</i>{@code />}, representing an individual's ObjectProperty.
	 * 
	 * @param owlProp
	 *            The element's tag.
	 * @param resource
	 *            The value of the "rdf:resource" attribute.
	 * @throws XMLStreamException
	 */
	private void writeIndividualObjectPropEmptyElement(String owlProp,
			String resource) throws XMLStreamException {
		writer.writeEmptyElement(owlProp);
		writer.writeAttribute(JGraLab2OWL.rdfNS, "resource", resource);
	}

	/**
	 * Writes an element {@code <}<i>owlProp</i> {@code rdf:datatype = }
	 * <i>datatype</i>{@code />}<i>value</i>{@code </} <i>owlProp</i>{@code >},
	 * representing an individual's DatatypeProperty.
	 * 
	 * @param owlProp
	 *            The element's tag.
	 * @param datatype
	 *            The property's datatype.
	 * @param value
	 *            The text node representing the property's value.
	 * @throws XMLStreamException
	 */
	private void writeIndividualDatatypePropElement(String owlProp,
			String datatype, String value) throws XMLStreamException {
		if ((value == null) || value.equals("")) {
			writer.writeEmptyElement(owlProp);
			writer.writeAttribute(JGraLab2OWL.rdfNS, "datatype", datatype);
		} else {
			writer.writeStartElement(owlProp);
			writer.writeAttribute(JGraLab2OWL.rdfNS, "datatype", datatype);
			writer.writeCharacters(value);
			writer.writeEndElement();
		}
	}
}
