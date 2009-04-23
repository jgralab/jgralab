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

package de.uni_koblenz.jgralab.utilities.jgralab2owl;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import de.uni_koblenz.jgralab.Aggregation;
import de.uni_koblenz.jgralab.Attribute;
import de.uni_koblenz.jgralab.AttributedElement;
import de.uni_koblenz.jgralab.Edge;
import de.uni_koblenz.jgralab.Graph;
import de.uni_koblenz.jgralab.ProgressFunction;
import de.uni_koblenz.jgralab.Vertex;
import de.uni_koblenz.jgralab.schema.AggregationClass;
import de.uni_koblenz.jgralab.schema.AttributedElementClass;
import de.uni_koblenz.jgralab.schema.Domain;
import de.uni_koblenz.jgralab.schema.EdgeClass;
import de.uni_koblenz.jgralab.schema.ListDomain;
import de.uni_koblenz.jgralab.schema.RecordDomain;
import de.uni_koblenz.jgralab.schema.Schema;
import de.uni_koblenz.jgralab.schema.SetDomain;

class Graph2OWLInstances {

	/**
	 * Represents the root of the DOM-tree.
	 */
	private Document doc;

	/**
	 * The direct child element of the root node
	 */
	private Element rdfElem;

	/**
	 * If {@code true}, an EdgeClass is converted to exactly one property,
	 * discarding possible attributes. If {@code false}, an EdgeClass is
	 * converted to an OWL class and two Properties.
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
	 * Creates an instance of {@code Graph2OWL}, assigns values to the member
	 * variables.
	 * 
	 * @param doc
	 *            The root node of the DOM-tree.
	 * @param g
	 *            The graph which shall be converted to OWL.
	 * @param pf
	 *            An instance of {@code ProgressFunction} to display the
	 *            progress of the conversion in a status bar.
	 */
	Graph2OWLInstances(Document doc, Graph g, boolean edgeClasses2Properties,
			boolean appendSuffix2EdgeClassName, ProgressFunction pf) {
		this.doc = doc;
		this.edgeClasses2Properties = edgeClasses2Properties;
		if (appendSuffix2EdgeClassName) {
			edgeClassNameSuffix = "EC";
		} else {
			edgeClassNameSuffix = "";
		}

		rdfElem = (Element) doc.getElementsByTagName("rdf:RDF").item(0);
		saveGraph(g, pf);
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
	 * 
	 * @see #convertGraph(Graph g, ProgressFunction pf)
	 */
	private void saveGraph(Graph g, ProgressFunction pf) {
		// initialize progress bar for graph
		long graphElements = 0, currentCount = 0, interval = 1;
		if (pf != null) {
			pf.init(g.getVCount() + g.getECount());
			interval = pf.getUpdateInterval();
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
	 * 
	 * @see #convertAttributeValue(AttributedElement ownerAe, Attribute attr)
	 */
	private void convertGraph(Graph g, ProgressFunction pf) {
		String eElemId;
		String vElemId;

		// create Individual for Graph g
		Element gElem = createIndividualElement(g.getAttributedElementClass()
				.getQualifiedName(), g.getId());

		// convert attributes of g
		try {
			for (Attribute attr : g.getAttributedElementClass()
					.getAttributeList()) {
				if (g.getAttribute(attr.getName()) != null) {
					gElem.appendChild(convertAttributeValue(g, attr));
				}
			}
		} catch (NoSuchFieldException nsfe) {
			nsfe.printStackTrace();
		}

		// append Individual for Graph g to element <rdf:RDF>
		rdfElem.appendChild(gElem);

		// convert vertices
		for (Vertex v : g.vertices()) {
			vElemId = HelperMethods.firstToLowerCase(v
					.getAttributedElementClass().getQualifiedName())
					+ String.valueOf(v.getId());
			gElem.appendChild(createIndividualObjectPropertyElement(
					"graphContainsVertex", "#" + vElemId));
			convertVertex(v, vElemId, pf);
		}

		if (!edgeClasses2Properties) {
			// convert edges
			for (Edge e : g.edges()) {
				eElemId = HelperMethods.firstToLowerCase(e
						.getAttributedElementClass().getQualifiedName())
						+ edgeClassNameSuffix + String.valueOf(e.getId());
				gElem.appendChild(createIndividualObjectPropertyElement(
						"graphContainsEdge" + edgeClassNameSuffix, "#"
								+ eElemId));

				convertEdge(e, eElemId, pf);
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
	 * 
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
	 * 
	 * @see #convertAttributeValue(AttributedElement ownerAe, Attribute attr)
	 */
	private void convertVertex(Vertex v, String vElemId, ProgressFunction pf) {
		AttributedElementClass vc = v.getAttributedElementClass();
		Edge e = v.getFirstEdge();

		// create the Individual representing v
		Element vElem = createIndividualElement(vc.getQualifiedName(), vElemId);

		String eElemId;

		// convert Attributes of v
		try {
			for (Attribute attr : v.getAttributedElementClass()
					.getAttributeList()) {
				if (v.getAttribute(attr.getName()) != null) {
					vElem.appendChild(convertAttributeValue(v, attr));
				}
			}
		} catch (NoSuchFieldException nsfe) {
			nsfe.printStackTrace();
		}

		vElem.appendChild(createIndividualObjectPropertyElement(
				"vertexIsInGraph", "#" + v.getGraph().getId()));

		// create individual properties referring to individuals representing
		// incident edges
		while (e != null) {
			eElemId = HelperMethods.firstToLowerCase(e
					.getAttributedElementClass().getQualifiedName())
					+ edgeClassNameSuffix
					+ String.valueOf(e.getNormalEdge().getId());

			if (edgeClasses2Properties) {
				if (e.getAlpha() == v) {
					vElem.appendChild(createIndividualObjectPropertyElement(
							HelperMethods.firstToLowerCase(e
									.getAttributedElementClass()
									.getQualifiedName()
									+ edgeClassNameSuffix), "#"
									+ HelperMethods.firstToLowerCase(e
											.getOmega()
											.getAttributedElementClass()
											.getQualifiedName())
									+ e.getOmega().getId()));
				} else {
					vElem.appendChild(createIndividualObjectPropertyElement(
							HelperMethods.firstToLowerCase(e
									.getAttributedElementClass()
									.getQualifiedName()
									+ edgeClassNameSuffix + "-of"), "#"
									+ HelperMethods.firstToLowerCase(e
											.getAlpha()
											.getAttributedElementClass()
											.getQualifiedName())
									+ e.getAlpha().getId()));
				}
			} else {
				if (e.getAlpha() == v) {
					vElem.appendChild(createIndividualObjectPropertyElement(
							HelperMethods.firstToLowerCase(e
									.getAttributedElementClass()
									.getQualifiedName()
									+ edgeClassNameSuffix + "Out"), "#"
									+ eElemId));
				} else {
					vElem.appendChild(createIndividualObjectPropertyElement(
							HelperMethods.firstToLowerCase(e
									.getAttributedElementClass()
									.getQualifiedName()
									+ edgeClassNameSuffix + "In"), "#"
									+ eElemId));
				}
			}

			e = e.getNextEdge();
		}

		rdfElem.appendChild(vElem);
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
	 * 
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
	 * 
	 * @see #convertAttributeValue(AttributedElement ownerAe, Attribute attr)
	 */
	private void convertEdge(Edge e, String eElemId, ProgressFunction pf) {
		AttributedElementClass ec = e.getAttributedElementClass();
		Vertex fromVertex = e.getAlpha();
		Vertex toVertex = e.getOmega();

		Element eElem = createIndividualElement(ec.getQualifiedName()
				+ edgeClassNameSuffix, eElemId);

		// compute the ids of the individuals representing the out and the in
		// vertices
		String fromElemId = HelperMethods.firstToLowerCase(fromVertex
				.getAttributedElementClass().getQualifiedName())
				+ fromVertex.getId();
		
		String toElemId = HelperMethods.firstToLowerCase(toVertex
				.getAttributedElementClass().getQualifiedName())
				+ toVertex.getId();

		// convert attributes of e
		try {
			for (Attribute attr : e.getAttributedElementClass()
					.getAttributeList()) {
				if (e.getAttribute(attr.getName()) != null) {
					eElem.appendChild(convertAttributeValue(e, attr));
				}
			}
		} catch (NoSuchFieldException nsfe) {
			nsfe.printStackTrace();
		}

		eElem
				.appendChild(createIndividualObjectPropertyElement("edge"
						+ edgeClassNameSuffix + "IsInGraph", "#"
						+ e.getGraph().getId()));

		// create properties for role names
		eElem.appendChild(createIndividualDatatypePropertyElement("edge"
				+ edgeClassNameSuffix + "OutRole",
				"http://www.w3.org/2001/XMLSchema#string", ((EdgeClass) e
						.getAttributedElementClass()).getFromRolename()));
		eElem.appendChild(createIndividualDatatypePropertyElement("edge"
				+ edgeClassNameSuffix + "InRole",
				"http://www.w3.org/2001/XMLSchema#string", ((EdgeClass) e
						.getAttributedElementClass()).getToRolename()));

		// create properties for aggregate if e is an Aggregation or Composition
		if (e instanceof Aggregation) {
			if (((AggregationClass) ec).isAggregateFrom()) {
				eElem.appendChild(createIndividualObjectPropertyElement(
						"aggregate", "#" + fromElemId));
			} else {
				eElem.appendChild(createIndividualObjectPropertyElement(
						"aggregate", "#" + toElemId));
			}
		}

		rdfElem.appendChild(eElem);
	}

	/**
	 * Converts the value of the {@code Attribute attr} to an individual of a
	 * Property. See
	 * {@link #createAttributeIndividualObjectPropertyElement(String name, Object value, Domain dom)}
	 * and
	 * {@link #createAttributeIndividualDatatypePropertyElement(String name, Object value, Domain dom)}
	 * for the created XML code.
	 * 
	 * @param ownerAe
	 *            The {@code AttributedElementClass} containing {@code attr}.
	 * @param attr
	 *            The {@code Attribute} which shall be converted.
	 * @return An individual property representing {@code attr}.
	 * 
	 * @throws NoSuchFieldException
	 * 
	 * @see #createAttributeIndividualObjectPropertyElement(String name, Object
	 *      value, Domain dom)
	 * @see #createAttributeIndividualDatatypePropertyElement(String name,
	 *      Object value, Domain dom)
	 */
	private Element convertAttributeValue(AttributedElement ownerAe,
			Attribute attr) throws NoSuchFieldException {
		String attrPropertyName;
		String attrName = attr.getName();
		Object value = ownerAe.getAttribute(attrName);

		AttributedElementClass ownersAec = ownerAe.getAttributedElementClass();

		// Find AttributedElementClass owning attr. This can be a superclass of
		// the initial ownersAec.
		Queue<AttributedElementClass> q = new LinkedList<AttributedElementClass>();
		q.add(ownersAec);
		while (ownersAec.getOwnAttribute(attrName) == null) {
			q.addAll(ownersAec.getDirectSuperClasses());
			ownersAec = q.remove();
		}

		Domain dom = attr.getDomain();

		// The name of the Property representing the attribute
		if (ownersAec instanceof EdgeClass) {
			attrPropertyName = HelperMethods.firstToLowerCase(ownersAec
					.getQualifiedName())
					+ edgeClassNameSuffix
					+ "Has"
					+ HelperMethods.firstToUpperCase(attrName);
		} else {
			attrPropertyName = HelperMethods.firstToLowerCase(ownersAec
					.getQualifiedName())
					+ "Has" + HelperMethods.firstToUpperCase(attrName);
		}

		// if "attr" has a CompositeDomain as type (no Object)
		if (dom.isComposite()) {
			return createAttributeIndividualObjectPropertyElement(
					attrPropertyName, value, dom);
			// if "attr" has a BasicDomain as type
		} else {
			return createAttributeIndividualDatatypePropertyElement(
					attrPropertyName, value, dom);
		}
	}

	/**
	 * Creates an individual of an ObjectProperty representing the value of a
	 * composite {@code Attribute}. See
	 * {@link #createListIndividualObjectPropertyElement(String name, Object value, Domain dom)}
	 * ,
	 * {@link #createSetIndividualObjectPropertyElement(String name, Object value, Domain dom)}
	 * and
	 * {@link #createRecordIndividualObjectPropertyElement(String name, Object value, Domain dom)}
	 * for the created XML code.
	 * 
	 * @param propertyName
	 *            The name of the ObjectProperty which shall be created.
	 * @param value
	 *            The value of the composite attribute which shall be converted.
	 * @param dom
	 *            The {@code Domain} of the composite attribute which shall be
	 *            converted.
	 * @return An individual ObjectProperty representing a composite attribute.
	 * 
	 * @see #createListIndividualObjectPropertyElement(String name, Object
	 *      value, Domain dom)
	 * @see #createSetIndividualObjectPropertyElement(String name, Object value,
	 *      Domain dom)
	 * @see #createRecordIndividualObjectPropertyElement(String name, Object
	 *      value, Domain dom)
	 */
	private Element createAttributeIndividualObjectPropertyElement(
			String propertyName, Object value, Domain dom) {
		if (dom.getTGTypeName(null).startsWith("List<")) {
			return createListIndividualObjectPropertyElement(propertyName,
					value, dom);
		} else if (dom.getTGTypeName(null).startsWith("Set<")) {
			return createSetIndividualObjectPropertyElement(propertyName,
					value, dom);
		} else {
			return createRecordIndividualObjectPropertyElement(propertyName,
					value, dom);
		}
	}

	/**
	 * Creates an individual of an ObjectProperty representing the value of an
	 * {@code Attribute} of a list type. <br>
	 * XML-code written if the members of the list are of composite type: <br>
	 * 
	 * <pre>
	 *     &lt;&lt;i&gt;propertyName&lt;/i&gt;&gt;
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
	 *          
	 *     &lt;&lt;i&gt;propertyName&lt;/i&gt;&gt;
	 * </pre>
	 * 
	 * XML-code written if the members of the list are of basic type: <br>
	 * 
	 * <pre>
	 *     &lt;&lt;i&gt;propertyName&lt;/i&gt;&gt;
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
	 *     &lt;/&lt;i&gt;propertyName&lt;/i&gt;&gt;
	 * </pre>
	 * 
	 * The child elements and/or attributes of the individuals {@code <Set>} are
	 * determined by yet another call of
	 * {@link #createAttributeIndividualObjectPropertyElement(String propertyName, Object value, Domain dom)}
	 * or
	 * {@link #createAttributeIndividualDatatypePropertyElement(String propertyName, Object value, Domain dom)}
	 * , respectively.
	 * 
	 * @param propertyName
	 *            The name of the ObjectProperty which shall be created.
	 * @param value
	 *            The value of the attribute of type List which shall be
	 *            converted.
	 * @param dom
	 *            The {@code Domain} of the attribute of type List which shall
	 *            be converted.
	 * @return An individual ObjectProperty representing an attribute of type
	 *         List.
	 * 
	 * @see #createAttributeIndividualObjectPropertyElement(String propertyName,
	 *      Object value, Domain dom)
	 * @see #createAttributeIndividualDatatypePropertyElement(String
	 *      propertyName, Object value, Domain dom)
	 */
	@SuppressWarnings("unchecked")
	private Element createListIndividualObjectPropertyElement(
			String propertyName, Object value, Domain dom) {
		Object componentValue;

		// get the base domain of the list
		Domain baseDomain = ((ListDomain) dom).getBaseDomain();

		Element attrIndividualPropertyElem = createIndividualPropertyElement(propertyName);
		Element nextListElementProperty = attrIndividualPropertyElem;
		Element compositeIndividualElem;

		// for each value inside the list
		for (int i = 0; i < ((List) value).size(); i++) {
			componentValue = ((List) value).get(i);

			// create an individual of owl-class "ListElement" and append it as
			// child of
			// the "hasNextListElement" individual property of the last
			// iteration
			compositeIndividualElem = createAnonymousIndividualElement("ListElement");
			nextListElementProperty.appendChild(compositeIndividualElem);

			// create individual properties for the ListElement's value
			if (baseDomain.isComposite()) {
				compositeIndividualElem
						.appendChild(createAttributeIndividualObjectPropertyElement(
								"listElementHasObject", componentValue,
								baseDomain));
			} else {
				compositeIndividualElem
						.appendChild(createAttributeIndividualDatatypePropertyElement(
								"listElementHasDatatype", componentValue,
								baseDomain));
			}

			// if i is not the index of the last ListElement, create property
			// "hasNextListElement"
			if (i < ((List) value).size() - 1) {
				nextListElementProperty = createIndividualPropertyElement("hasNextListElement");
				compositeIndividualElem.appendChild(nextListElementProperty);
			}
		}

		return attrIndividualPropertyElem;
	}

	/**
	 * Creates an individual of an ObjectProperty representing the value of an
	 * {@code Attribute} of a set type. <br>
	 * XML-code written if the members of the set members are of composite type: <br>
	 * 
	 * <pre>
	 *     &lt;&lt;i&gt;propertyName&lt;/i&gt;&gt;
	 *         &lt;Set&gt;
	 *             &lt;setHasObject &lt;i&gt;...&lt;/i&gt;
	 *         &lt;/Set&gt;    
	 *     &lt;/&lt;i&gt;propertyName&lt;/i&gt;&gt;
	 * </pre>
	 * 
	 * XML-code written if the members of the set members are of basic type: <br>
	 * 
	 * <pre>
	 *     &lt;&lt;i&gt;propertyName&lt;/i&gt;&gt;
	 *         &lt;Set&gt;
	 *             &lt;setHasDatatype &lt;i&gt;...&lt;/i&gt;
	 *         &lt;/Set&gt;    
	 *     &lt;/&lt;i&gt;propertyName&lt;/i&gt;&gt;
	 * </pre>
	 * 
	 * The child elements and/or attributes of the individuals {@code <Set>} are
	 * determined by yet another call of
	 * {@link #createAttributeIndividualObjectPropertyElement(String propertyName, Object value, Domain dom)}
	 * or
	 * {@link #createAttributeIndividualDatatypePropertyElement(String propertyName, Object value, Domain dom)}
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
	 * @return An individual ObjectProperty representing an attribute of type
	 *         Set.
	 * 
	 * @see #createAttributeIndividualObjectPropertyElement(String propertyName,
	 *      Object value, Domain dom)
	 * @see #createAttributeIndividualDatatypePropertyElement(String
	 *      propertyName, Object value, Domain dom)
	 */
	@SuppressWarnings("unchecked")
	private Element createSetIndividualObjectPropertyElement(
			String propertyName, Object value, Domain dom) {
		// get the base domain of the Set
		Domain baseDomain = ((SetDomain) dom).getBaseDomain();

		Element compositeIndividualElem = createAnonymousIndividualElement("Set");

		Element attrIndividualPropertyElem = createIndividualPropertyElement(propertyName);
		attrIndividualPropertyElem.appendChild(compositeIndividualElem);

		// if the base domain is a composite domain
		if (baseDomain.isComposite()) {
			// for each value inside the Set
			for (Object componentValue : (Set) value) {
				compositeIndividualElem
						.appendChild(createAttributeIndividualObjectPropertyElement(
								"setHasObject", componentValue, baseDomain));
			}
			// if the base domain is a basic domain
		} else {
			// for each value inside the Set
			for (Object componentValue : (Set) value) {
				compositeIndividualElem
						.appendChild(createAttributeIndividualDatatypePropertyElement(
								"setHasDatatype", componentValue, baseDomain));
			}
		}

		return attrIndividualPropertyElem;
	}

	/**
	 * Creates an individual of an ObjectProperty representing the value of an
	 * {@code Attribute} of a record type. <br>
	 * XML-code written, where <i>componentNameX</i> denotes the names of the
	 * components: <br>
	 * 
	 * <pre>
	 *     &lt;&lt;i&gt;propertyName&lt;/i&gt;&gt;
	 *         &lt;&lt;i&gt;dom.getName()&lt;/i&gt;&gt;
	 *             &lt;&lt;i&gt;dom.getName()&lt;/i&gt;Has&lt;i&gt;componentName1&lt;/i&gt; &lt;i&gt;...&lt;/i&gt;
	 *             &lt;&lt;i&gt;dom.getName()&lt;/i&gt;Has&lt;i&gt;componentName2&lt;/i&gt; &lt;i&gt;...&lt;/i&gt;
	 *         &lt;/&lt;i&gt;dom.getName()&lt;/i&gt;&gt;    
	 *     &lt;/&lt;i&gt;propertyName&lt;/i&gt;&gt;
	 * </pre>
	 * 
	 * The child elements and/or attributes of the individuals {@code <}
	 * <i>dom.getName() </i>{@code Has}<i>componentNameX</i> are determined by a
	 * call of
	 * {@link #createAttributeIndividualObjectPropertyElement(String propertyName, Object value, Domain dom)}
	 * or
	 * {@link #createAttributeIndividualDatatypePropertyElement(String propertyName, Object value, Domain dom)}
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
	 * @param propertyName
	 *            The name of the ObjectProperty which shall be created.
	 * @param value
	 *            The value of the attribute of type Record which shall be
	 *            converted.
	 * @param dom
	 *            The {@code Domain} of the attribute of type Record which shall
	 *            be converted.
	 * @return An individual ObjectProperty representing an attribute of type
	 *         Record.
	 * 
	 * @see #createAttributeIndividualObjectPropertyElement(String propertyName,
	 *      Object value, Domain dom)
	 * @see #createAttributeIndividualDatatypePropertyElement(String
	 *      propertyName, Object value, Domain dom)
	 */
	private Element createRecordIndividualObjectPropertyElement(
			String propertyName, Object value, Domain dom) {
		Element compositeIndividualElem = createAnonymousIndividualElement(dom
				.getQualifiedName());

		Element attrIndividualPropertyElem = createIndividualPropertyElement(propertyName);
		attrIndividualPropertyElem.appendChild(compositeIndividualElem);

		// for every component of the Record
		for (Map.Entry<String, Domain> component : ((RecordDomain) dom)
				.getComponents().entrySet()) {
			Object componentValue = null;

			// get the value of the record component
			try {
				componentValue = value.getClass().getField(component.getKey())
						.get(value);
			} catch (Exception e) {
				e.printStackTrace();
			}

			// if the component is of composite type
			if (component.getValue().isComposite()) {
				compositeIndividualElem
						.appendChild(createAttributeIndividualObjectPropertyElement(
								HelperMethods.firstToLowerCase(dom
										.getQualifiedName())
										+ "Has"
										+ HelperMethods
												.firstToUpperCase(component
														.getKey()),
								componentValue, component.getValue()));
				// if the component is of basic type
			} else {
				compositeIndividualElem
						.appendChild(createAttributeIndividualDatatypePropertyElement(
								HelperMethods.firstToLowerCase(dom
										.getQualifiedName())
										+ "Has"
										+ HelperMethods
												.firstToUpperCase(component
														.getKey()),
								componentValue, component.getValue()));
			}
		}

		return attrIndividualPropertyElem;
	}

	/**
	 * Creates an individual of a DatatypeProperty representing the value of an
	 * {@code Attribute} of a basic type. <br>
	 * XML-code written:<br>
	 * <br>
	 * 
	 * <pre>
	 *     &lt;&lt;i&gt;propertyName&lt;/i&gt; rdf:datatype=&quot;#&lt;i&gt;dom.getTGTypeName()&lt;/i&gt;&gt;&lt;i&gt;value.toString()&lt;/i&gt;&lt;/&lt;i&gt;propertyName&lt;/i&gt;&gt;
	 * </pre>
	 * 
	 * @param propertyName
	 *            The name of the DatatypeProperty which shall be created.
	 * @param value
	 *            The value of the attribute of basic type which shall be
	 *            converted.
	 * @param dom
	 *            The {@code Domain} of the attribute of basic type which shall
	 *            be converted.
	 * @return An individual DatatypeProperty representing an attribute of basic
	 *         type.
	 */
	@SuppressWarnings("unchecked")
	private Element createAttributeIndividualDatatypePropertyElement(
			String propertyName, Object value, Domain dom) {
		Element attrIndividualPropertyElem = createIndividualPropertyElement(propertyName);

		if (dom.getTGTypeName(null).equals("String")) {
			attrIndividualPropertyElem.setAttribute("rdf:datatype",
					"http://www.w3.org/2001/XMLSchema#string");
			attrIndividualPropertyElem.appendChild(doc
					.createTextNode((String) value));
		} else if (dom.toString().contains("Enum")) {
			attrIndividualPropertyElem.setAttribute("rdf:resource", "#"
					+ ((Enum) value).toString());
		} else {
			attrIndividualPropertyElem.setAttribute("rdf:datatype",
					"http://www.w3.org/2001/XMLSchema#"
							+ dom.getJavaAttributeImplementationTypeName(""));
			attrIndividualPropertyElem.appendChild(doc.createTextNode((value
					.toString())));
		}

		return attrIndividualPropertyElem;
	}

	/**
	 * Creates and returns an element {@code <}<i>owlClass</i>{@code />},
	 * representing an individual.
	 * 
	 * @param owlClass
	 *            The element's tag.
	 * @return The created element.
	 */
	private Element createAnonymousIndividualElement(String owlClass) {
		Element elem = doc.createElement(owlClass);

		return elem;
	}

	/**
	 * Creates and returns an element {@code <}<i>owlClass</i> {@code rdf:ID = }
	 * <i>id</i>{@code />}, representing an individual.
	 * 
	 * @param owlClass
	 *            The element's tag.
	 * @param id
	 *            The value of the "rdf:ID" attribute.
	 * @return The created element.
	 */
	private Element createIndividualElement(String owlClass, String id) {
		Element elem = doc.createElement(owlClass);
		elem.setAttribute("rdf:ID", id);

		return elem;
	}

	/**
	 * Creates and returns an element {@code <}<i>owlProperty</i>{@code />},
	 * representing an individual's Property.
	 * 
	 * @param owlProperty
	 *            The element's tag.
	 * @return The created element.
	 */
	private Element createIndividualPropertyElement(String owlProperty) {
		Element elem = doc.createElement(owlProperty);

		return elem;
	}

	/**
	 * Creates and returns an element {@code <}<i>owlProperty</i> {@code
	 * rdf:resource = } <i>id</i>{@code />}, representing an individual's
	 * ObjectProperty.
	 * 
	 * @param owlProperty
	 *            The element's tag.
	 * @param resource
	 *            The value of the "rdf:resource" attribute.
	 * @return The created element.
	 */
	private Element createIndividualObjectPropertyElement(String owlProperty,
			String resource) {
		Element elem = doc.createElement(owlProperty);
		elem.setAttribute("rdf:resource", resource);

		return elem;
	}

	/**
	 * Creates and returns an element {@code <}<i>owlProperty</i> {@code
	 * rdf:datatype = } <i>datatype</i>{@code />}<i>value</i>{@code </}
	 * <i>owlProperty</i>{@code >}, representing an individual's
	 * DatatypeProperty.
	 * 
	 * @param owlProperty
	 *            The element's tag.
	 * @param datatype
	 *            The property's datatype.
	 * @param value
	 *            The text node representing the property's value.
	 * @return The created element.
	 */
	private Element createIndividualDatatypePropertyElement(String owlProperty,
			String datatype, String value) {
		Element elem = doc.createElement(owlProperty);
		elem.setAttribute("rdf:datatype", datatype);
		elem.appendChild(doc.createTextNode(value));

		return elem;
	}
}
