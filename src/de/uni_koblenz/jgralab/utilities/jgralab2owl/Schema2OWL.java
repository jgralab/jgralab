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

import java.util.Map;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import de.uni_koblenz.jgralab.Attribute;
import de.uni_koblenz.jgralab.schema.AttributedElementClass;
import de.uni_koblenz.jgralab.schema.CompositeDomain;
import de.uni_koblenz.jgralab.schema.Domain;
import de.uni_koblenz.jgralab.schema.EdgeClass;
import de.uni_koblenz.jgralab.schema.EnumDomain;
import de.uni_koblenz.jgralab.schema.GraphClass;
import de.uni_koblenz.jgralab.schema.GraphElementClass;
import de.uni_koblenz.jgralab.schema.RecordDomain;
import de.uni_koblenz.jgralab.schema.Schema;
import de.uni_koblenz.jgralab.schema.VertexClass;

class Schema2OWL {

	/**
	 * The names of the default {@code GraphElementClass}es except {@code
	 * Aggregation} and {@code Composition}.
	 */
	private final String[] defaultGECs = { "Vertex", "Edge" };

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
	 * @param doc
	 *            The root node of the DOM-tree.
	 * @param schema
	 *            The schema which shall be converted to OWL.
	 * @param edgeClasses2Properties
	 *            If {@code true}, an EdgeClass is converted to exactly one
	 *            property, discarding possible attributes and rolenames. If
	 *            {@code false}, an EdgeClass is converted to an OWL class and
	 *            two Properties.
	 * @param appendSuffix2EdgeClassName
	 *            If {@code true}, the suffix {@code EC} is appended to each OWL
	 *            construct representing an EdgeClass.
	 */
	Schema2OWL(Document doc, Schema schema, boolean edgeClasses2Properties,
			boolean appendSuffix2EdgeClassName) {
		this.doc = doc;
		this.edgeClasses2Properties = edgeClasses2Properties;
		if (appendSuffix2EdgeClassName) {
			edgeClassNameSuffix = "EC";
		} else {
			edgeClassNameSuffix = "";
		}

		rdfElem = (Element) doc.getElementsByTagName("rdf:RDF").item(0);
		saveSchema(schema);
	}

	/**
	 * Converts a schema ({@code schema}) to a DOM-tree consisting of
	 * OWL-Elements and then writes the tree as XML-output into a file.
	 *
	 * @param schema
	 *            The schema which shall be converted to OWL.
	 *
	 * @see #convertEnumDomains(Schema schema)
	 * @see #convertCompositeDomains(Schema schema)
	 * @see #convertGraphClasses(Schema schema)
	 */
	private void saveSchema(Schema schema) {
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
	 */
	private void convertEnumDomains(Schema schema) {
		for (EnumDomain enumDomain : schema.getEnumDomains()) {
			Element enumDomainElem = createOwlClassElement(enumDomain
					.getQualifiedName());
			rdfElem.appendChild(enumDomainElem);

			Element oneOfElem = doc.createElement("owl:oneOf");
			oneOfElem.setAttribute("rdf:parseType", "Collection");
			enumDomainElem.appendChild(oneOfElem);

			// convert Enum constants
			Element enumConstElem;
			for (String enumConst : enumDomain.getConsts()) {
				enumConstElem = doc.createElement("owl:Thing");
				enumConstElem.setAttribute("rdf:about", "#" + enumConst);
				oneOfElem.appendChild(enumConstElem);
			}
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
	 *
	 * @see #convertSetDomain()
	 * @see #convertListDomain()
	 * @see #convertRecordDomain(RecordDomain rd)
	 */
	private void convertCompositeDomains(Schema schema) {
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
	 */
	private void convertSetDomain() {
		Element setDomainElem;
		Element datatypeComponentElem;
		Element objectComponentElem;
		Element rdfsDomainElem;
		Element datatypeRdfsRangeElem;
		Element objectRdfsRangeElem;

		// create elements
		setDomainElem = createOwlClassElement("Set");
		datatypeComponentElem = createOwlDatatypePropertyElement("setHasDatatype");
		objectComponentElem = createOwlObjectPropertyElement("setHasObject");
		rdfsDomainElem = createRdfsDomainElement("#Set");
		datatypeRdfsRangeElem = createRdfsRangeElement("http://www.w3.org/2000/01/rdf-schema#Datatype");
		objectRdfsRangeElem = createRdfsRangeElement("http://www.w3.org/2002/07/owl#Class");

		// build subtree
		rdfElem.appendChild(setDomainElem);
		rdfElem.appendChild(datatypeComponentElem);
		datatypeComponentElem.appendChild(rdfsDomainElem);
		datatypeComponentElem.appendChild(datatypeRdfsRangeElem);
		rdfElem.appendChild(objectComponentElem);
		objectComponentElem.appendChild(rdfsDomainElem.cloneNode(false));
		objectComponentElem.appendChild(objectRdfsRangeElem);

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
	 */
	private void convertListDomain() {
		Element listDomainElem;
		Element datatypeComponentElem;
		Element objectComponentElem;
		Element rdfsDomainElem;
		Element datatypeRdfsRangeElem;
		Element objectRdfsRangeElem;
		Element nextElem;
		Element nextTypeElem;
		Element nextDomainElem;
		Element nextRangeElem;
		Element datatypeSubClassOfElem;
		Element datatypeRestrictionElem;
		Element datatypeOnPropertyElem;
		Element datatypeCardinalityElem;
		Element objectSubClassOfElem;
		Element objectRestrictionElem;
		Element objectOnPropertyElem;
		Element objectCardinalityElem;

		// create element "ListElement"
		listDomainElem = createOwlClassElement("ListElement");

		datatypeSubClassOfElem = createRdfsSubClassOfElement();
		datatypeRestrictionElem = createOwlRestrictionElement();
		datatypeOnPropertyElem = createOwlOnPropertyElement("#listElementHasDatatype");
		datatypeCardinalityElem = createOwlCardinalityElement(1);

		objectSubClassOfElem = createRdfsSubClassOfElement();
		objectRestrictionElem = createOwlRestrictionElement();
		objectOnPropertyElem = createOwlOnPropertyElement("#listElementHasObject");
		objectCardinalityElem = createOwlCardinalityElement(1);

		// create Property elements
		datatypeComponentElem = createOwlDatatypePropertyElement("listElementHasDatatype");
		objectComponentElem = createOwlObjectPropertyElement("listElementHasObject");
		rdfsDomainElem = createRdfsDomainElement("#ListElement");
		datatypeRdfsRangeElem = createRdfsRangeElement("http://www.w3.org/2000/01/rdf-schema#Datatype");
		objectRdfsRangeElem = createRdfsRangeElement("http://www.w3.org/2002/07/owl#Class");

		nextElem = createOwlObjectPropertyElement();
		nextElem.setAttribute("rdf:ID", "hasNextListElement");
		nextTypeElem = createRdfTypeElement("http://www.w3.org/2002/07/owl#FunctionalProperty");
		nextDomainElem = createRdfsDomainElement("#ListElement");
		nextRangeElem = createRdfsRangeElement("#ListElement");

		// build "ListElement" subtree
		rdfElem.appendChild(listDomainElem);

		datatypeRestrictionElem.appendChild(datatypeOnPropertyElem);
		datatypeRestrictionElem.appendChild(datatypeCardinalityElem);
		datatypeSubClassOfElem.appendChild(datatypeRestrictionElem);
		listDomainElem.appendChild(datatypeSubClassOfElem);

		objectRestrictionElem.appendChild(objectOnPropertyElem);
		objectRestrictionElem.appendChild(objectCardinalityElem);
		objectSubClassOfElem.appendChild(objectRestrictionElem);
		listDomainElem.appendChild(objectSubClassOfElem);

		// build Property subtrees
		rdfElem.appendChild(datatypeComponentElem);
		datatypeComponentElem.appendChild(rdfsDomainElem);
		datatypeComponentElem.appendChild(datatypeRdfsRangeElem);
		rdfElem.appendChild(objectComponentElem);
		objectComponentElem.appendChild(rdfsDomainElem.cloneNode(false));
		objectComponentElem.appendChild(objectRdfsRangeElem);

		rdfElem.appendChild(nextElem);
		nextElem.appendChild(nextTypeElem);
		nextElem.appendChild(nextDomainElem);
		nextElem.appendChild(nextRangeElem);
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
	 */
	private void convertRecordDomain(RecordDomain rd) {
		Element rdElem;
		Element componentElem;
		Element rdfsDomainElem;
		Element rdfsRangeElem;
		Element typeElem;

		rdElem = createOwlClassElement();
		rdElem.setAttribute("rdf:ID", rd.getQualifiedName());
		rdfsDomainElem = createRdfsDomainElement();
		rdfsDomainElem
				.setAttribute("rdf:resource", "#" + rd.getQualifiedName());

		// append Class representing "rd" to root
		rdfElem.appendChild(rdElem);

		// create Properties for "rd"'s components
		for (Map.Entry<String, Domain> component : rd.getComponents()
				.entrySet()) {
			rdfsRangeElem = createRdfsRangeElement();
			typeElem = createRdfTypeElement("http://www.w3.org/2002/07/owl#FunctionalProperty");

			// if "component" has a CompositeDomain or an EnumDomain (no Object)
			if (component.getValue().isComposite()
					|| component.getValue().toString().contains("Enum")) {
				componentElem = createOwlObjectPropertyElement();
				
				if (component.getValue().getTGTypeName(null).startsWith("List<")) {
					rdfsRangeElem.setAttribute("rdf:resource", "#ListElement");
				} else if (component.getValue().getTGTypeName(null).startsWith("Set<")) {
					rdfsRangeElem.setAttribute("rdf:resource", "#Set");
				} else {
					rdfsRangeElem.setAttribute("rdf:resource", 
							"#" + component.getValue().getQualifiedName());
				}
			// if "component" has a BasicDomain
			} else {
				componentElem = createOwlDatatypePropertyElement();
								
				if (component.getValue().getTGTypeName(null).equals("String")) {
					rdfsRangeElem.setAttribute("rdf:resource", 
							"http://www.w3.org/2001/XMLSchema#string");
				} else if (component.getValue().getTGTypeName(null).equals("Object")) {
					rdfsRangeElem.setAttribute("rdf:resource", 
							"http://www.w3.org/2001/XMLSchema#base64binary");
				} else {
					rdfsRangeElem.setAttribute("rdf:resource", 
							"http://www.w3.org/2001/XMLSchema#" 
							+ component.getValue().getJavaAttributeImplementationTypeName(""));
				} 
			}
			
			// set Property's ID
			componentElem.setAttribute("rdf:ID", HelperMethods
					.firstToLowerCase(rd.getQualifiedName())
					+ "Has"
					+ HelperMethods.firstToUpperCase(component.getKey()));

			// build subtree
			rdfElem.appendChild(componentElem);
			componentElem.appendChild(typeElem);
			componentElem.appendChild(rdfsDomainElem.cloneNode(false));
			componentElem.appendChild(rdfsRangeElem);
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
	 *
	 * @see #convertAttributes(AttributedElementClass aec)
	 * @see #convertVertexClasses(GraphClass gc)
	 * @see #convertEdgeClasses(GraphClass gc)
	 * @see #createDefaultGECProperties()
	 * @see #createRoleElement(boolean from, String gecStringRep)
	 */
	private void convertGraphClasses(Schema schema) {
		Element gcElem;
		Element subClassOfElem;
		Element fromRoleElem;
		Element toRoleElem;
		Element unionOfElem;
		
		// create OWL class for Default GraphClass
		GraphClass gc = schema.getDefaultGraphClass();
		gcElem = createOwlClassElement(gc.getQualifiedName());
		rdfElem.appendChild(gcElem);

		// create OWL class for "other" GraphClass
		gc = schema.getGraphClass();
		gcElem = createOwlClassElement(gc.getQualifiedName());
		rdfElem.appendChild(gcElem);

		// create references to superclasses
		for (AttributedElementClass superGC : gc.getDirectSuperClasses()) {
			subClassOfElem = createRdfsSubClassOfElement("#"
					+ superGC.getQualifiedName());
			gcElem.appendChild(subClassOfElem);
		}

		// if gc is abstract and has subclasses, create union of subclasses
		if (gc.isAbstract() && !gc.getDirectSubClasses().isEmpty()) {
			unionOfElem = createUnionOfSubclasses(gc);

			gcElem.appendChild(unionOfElem);
		}

		convertAttributes(gc);
		convertVertexClasses(gc);
		convertEdgeClasses(gc);

		createDefaultGECProperties();

		if (!edgeClasses2Properties) {
			fromRoleElem = createRoleElement(true);
			toRoleElem = createRoleElement(false);

			rdfElem.appendChild(fromRoleElem);
			rdfElem.appendChild(toRoleElem);
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
	 *
	 * @see #convertAttributes(AttributedElementClass aec)
	 */
	private void convertVertexClasses(GraphClass gc) {
		Element subClassOfElem;
		Element vcElem;
		Element unionOfElem;

		// for each VertexClass in gc
		for (VertexClass vc : gc.getVertexClasses()) {
			vcElem = createOwlClassElement(vc.getQualifiedName());
			rdfElem.appendChild(vcElem);

			// create references to superclasses
			for (AttributedElementClass superVC : vc.getDirectSuperClasses()) {
				subClassOfElem = createRdfsSubClassOfElement("#"
						+ superVC.getQualifiedName());
				vcElem.appendChild(subClassOfElem);
			}

			// if vc is abstract and has subclasses, create union of subclasses
			if (vc.isAbstract() && !vc.getDirectSubClasses().isEmpty()) {
				unionOfElem = createUnionOfSubclasses(vc);

				vcElem.appendChild(unionOfElem);
			}

			// create restriction for Property "vertexClassIsIn + gc.getName()"
			if (vc.getQualifiedName().equals("Vertex")) {
				vcElem.appendChild(createDefaultGECCardinality("Vertex"));
			}

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
	 *
	 * @see #convertEdgeClass2OWLProperty(EdgeClass ec)
	 * @see #convertEdgeClass2OWLClass(EdgeClass ec)
	 */
	private void convertEdgeClasses(GraphClass gc) {
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
	 *
	 * @see #createMultiplicityElement(boolean from, EdgeClass ec)
	 */
	private void convertEdgeClass2OWLProperty(EdgeClass ec) {
		Element ecElem, ecReElem;
		Element subPropertyOfElem;
		Element inverseOfElem;
		Element domainElem;
		Element rangeElem;
		Element outMultiplicityElem;
		Element inMultiplicityElem;

		ecElem = createOwlObjectPropertyElement(HelperMethods
				.firstToLowerCase(ec.getQualifiedName())
				+ edgeClassNameSuffix);
		ecReElem = createOwlObjectPropertyElement(HelperMethods
				.firstToLowerCase(ec.getQualifiedName())
				+ edgeClassNameSuffix + "-of");

		rdfElem.appendChild(ecElem);
		rdfElem.appendChild(ecReElem);

		// create superclass references
		for (AttributedElementClass superEC : ec.getDirectSuperClasses()) {
			subPropertyOfElem = createRdfsSubPropertyOfElement("#"
					+ HelperMethods
							.firstToLowerCase(superEC.getQualifiedName())
					+ edgeClassNameSuffix);
			ecElem.appendChild(subPropertyOfElem);

			subPropertyOfElem = createRdfsSubPropertyOfElement("#"
					+ HelperMethods
							.firstToLowerCase(superEC.getQualifiedName())
					+ edgeClassNameSuffix + "-of");
			ecReElem.appendChild(subPropertyOfElem);
		}

		inverseOfElem = createOwlInverseOfElement("#"
				+ HelperMethods.firstToLowerCase(ec.getQualifiedName())
				+ edgeClassNameSuffix);
		ecReElem.appendChild(inverseOfElem);

		domainElem = createRdfsDomainElement("#"
				+ (ec).getFrom().getQualifiedName());
		rangeElem = createRdfsRangeElement("#"
				+ (ec).getTo().getQualifiedName());
		ecElem.appendChild(domainElem);
		ecElem.appendChild(rangeElem);

		domainElem = createRdfsDomainElement("#"
				+ (ec).getTo().getQualifiedName());
		rangeElem = createRdfsRangeElement("#"
				+ (ec).getFrom().getQualifiedName());
		ecReElem.appendChild(domainElem);
		ecReElem.appendChild(rangeElem);

		// create subclass restrictions for mutiplicities
		outMultiplicityElem = createMultiplicityElement(true, ec);
		inMultiplicityElem = createMultiplicityElement(false, ec);

		// build subtrees
		HelperMethods.getOwlElement(doc,
				(ec).getFrom().getQualifiedName()).appendChild(
				outMultiplicityElem);
		HelperMethods.getOwlElement(doc,
				(ec).getTo().getQualifiedName()).appendChild(
				inMultiplicityElem);
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
	 *
	 * @see #convertAttributes(AttributedElementClass aec)
	 * @see #createMultiplicityElement(boolean from, EdgeClass ec)
	 * @see #createIncidentVertexClassElement(boolean from, EdgeClass ec)
	 * @see #createAggregateElement()
	 * @see #createAggregateSubClassElement(Element elem)
	 */
	private void convertEdgeClass2OWLClass(EdgeClass ec) {
		Element ecElem;
		Element aggregateElem;
		Element aggregateSubClassElem;
		Element subClassOfElem;
		Element fromVCElem;
		Element toVCElem;
		Element outMultiplicityElem;
		Element inMultiplicityElem;
		Element unionOfElem;

		ecElem = createOwlClassElement(ec.getQualifiedName()
				+ edgeClassNameSuffix);
		rdfElem.appendChild(ecElem);

		// create superclass references
		for (AttributedElementClass superEC : ec.getDirectSuperClasses()) {
			subClassOfElem = createRdfsSubClassOfElement("#"
					+ superEC.getQualifiedName() + edgeClassNameSuffix);
			ecElem.appendChild(subClassOfElem);
		}

		// if gec is abstract and has subclasses, create union of subclasses
		if (ec.isAbstract() && !ec.getDirectSubClasses().isEmpty()) {
			unionOfElem = createUnionOfSubclasses(ec);

			ecElem.appendChild(unionOfElem);
		}

		// create subclass restrictions for mutiplicities
		outMultiplicityElem = createMultiplicityElement(true, ec);
		inMultiplicityElem = createMultiplicityElement(false, ec);

		// create ObjectProperties for incident VertexClasses
		fromVCElem = createIncidentVertexClassElement(true, ec);
		toVCElem = createIncidentVertexClassElement(false, ec);

		// create ObjectProperty for aggregate
		if (ec.getQualifiedName().equals("Aggregation")) {
			aggregateElem = createAggregateElement();
			aggregateSubClassElem = createAggregateSubClassElement(aggregateElem);

			ecElem.appendChild(aggregateSubClassElem);
			rdfElem.appendChild(aggregateElem);
		}

		// create subclass restriction for Property
		// "edgeClassIsIn + gc.getName()"
		if (ec.getQualifiedName().equals("Edge")) {
			ecElem.appendChild(createDefaultGECCardinality("Edge"));
		}

		// build subtrees
		HelperMethods.getOwlElement(doc, (ec).getFrom().getQualifiedName())
				.appendChild(outMultiplicityElem);
		HelperMethods.getOwlElement(doc, (ec).getTo().getQualifiedName())
				.appendChild(inMultiplicityElem);

		rdfElem.appendChild(fromVCElem);
		rdfElem.appendChild(toVCElem);

		// convert attributes
		convertAttributes(ec);
	}

	/**
	 * Creates an {@code ObjectProperty} representing the relation from a
	 * {@code VertexClass} to the incident {@code EdgeClass ec} (if {@code
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
	 * @return The created {@code ObjectProperty}.
	 */
	private Element createIncidentVertexClassElement(boolean from, EdgeClass ec) {
		Element incidentVertexClassElem;
		Element domainElem;
		Element rangeElem;
		Element typeElem;

		String direction;
		String vcName;

		// get name of 'from' or 'to' VertexClass
		if (from) {
			direction = "Out";
			vcName = ec.getFrom().getQualifiedName();
		} else {
			direction = "In";
			vcName = ec.getTo().getQualifiedName();
		}

		// create ObjectProperty for incident EdgeClass
		incidentVertexClassElem = createOwlObjectPropertyElement();
		incidentVertexClassElem.setAttribute("rdf:ID", HelperMethods
				.firstToLowerCase(ec.getQualifiedName())
				+ edgeClassNameSuffix + direction);
		typeElem = createRdfTypeElement("http://www.w3.org/2002/07/owl#SymmetricProperty");
		domainElem = createRdfsDomainElement("#" + vcName);
		rangeElem = createRdfsRangeElement("#" + ec.getQualifiedName()
				+ edgeClassNameSuffix);

		// build subtree
		incidentVertexClassElem.appendChild(typeElem);
		incidentVertexClassElem.appendChild(domainElem);
		incidentVertexClassElem.appendChild(rangeElem);

		return incidentVertexClassElem;
	}

	/**
	 * Creates a subclass restriction representing the multiplicity of a {@code
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
	 * @return The created subclass restriction.
	 */
	private Element createMultiplicityElement(boolean from, EdgeClass ec) {
		Element maxCardinalityElem;
		Element minCardinalityElem;
		Element onPropertyElem;
		Element restrictionElem;
		Element subClassOfElem;

		int lowerBound;
		int upperBound;
		String direction;

		// get multiplicities for "from" or "to" VertexClasses
		if (from) {
			direction = "Out";
			lowerBound = ec.getToMin();
			upperBound = ec.getToMax();
		} else {
			direction = "In";
			lowerBound = ec.getFromMin();
			upperBound = ec.getFromMax();
		}

		// create subclass restriction
		subClassOfElem = createRdfsSubClassOfElement();
		restrictionElem = createOwlRestrictionElement();
		if (edgeClasses2Properties) {
			if (from) {
				onPropertyElem = createOwlOnPropertyElement("#"
						+ HelperMethods.firstToLowerCase(ec.getQualifiedName())
						+ edgeClassNameSuffix);
			} else {
				onPropertyElem = createOwlOnPropertyElement("#"
						+ HelperMethods.firstToLowerCase(ec.getQualifiedName())
						+ edgeClassNameSuffix + "-of");
			}
		} else {
			onPropertyElem = createOwlOnPropertyElement("#"
					+ HelperMethods.firstToLowerCase(ec.getQualifiedName())
					+ edgeClassNameSuffix + direction);
		}
		minCardinalityElem = createOwlMinCardinalityElement(lowerBound);
		maxCardinalityElem = createOwlMaxCardinalityElement(upperBound);

		// build subtree
		restrictionElem.appendChild(onPropertyElem);
		restrictionElem.appendChild(minCardinalityElem);
		restrictionElem.appendChild(maxCardinalityElem);
		subClassOfElem.appendChild(restrictionElem);

		return subClassOfElem;
	}

	/**
	 * Creates an {@code ObjectProperty} relating the OWL Class representing the
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
	 * @return The created {@code ObjectProperty}.
	 */
	private Element createAggregateElement() {
		Element aggregateElem;
		Element typeElem;
		Element domainElem;
		Element rangeElem;

		// create ObjectProperty
		aggregateElem = createOwlObjectPropertyElement("aggregate");
		typeElem = createRdfTypeElement("http://www.w3.org/2002/07/owl#FunctionalProperty");
		domainElem = createRdfsDomainElement("#Aggregation"
				+ edgeClassNameSuffix);
		rangeElem = createRdfsRangeElement("#Vertex");

		// build subtree
		aggregateElem.appendChild(typeElem);
		aggregateElem.appendChild(domainElem);
		aggregateElem.appendChild(rangeElem);

		return aggregateElem;
	}

	/**
	 * Creates a subclass restriction representing the cardinality "1" of an OWL
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
	 * @return The created subclass restriction.
	 */
	private Element createAggregateSubClassElement(Element aggregateElem) {
		Element aggregateSubClassElem;
		Element restrictionElem;
		Element onPropertyElem;
		Element cardinalityElem;

		// create subclass restriction
		aggregateSubClassElem = createRdfsSubClassOfElement();
		restrictionElem = createOwlRestrictionElement();
		onPropertyElem = createOwlOnPropertyElement("#"
				+ HelperMethods.firstToLowerCase(aggregateElem
						.getAttribute("rdf:ID")));
		cardinalityElem = createOwlCardinalityElement(1);

		// build subtree
		restrictionElem.appendChild(onPropertyElem);
		restrictionElem.appendChild(cardinalityElem);
		aggregateSubClassElem.appendChild(restrictionElem);

		return aggregateSubClassElem;
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
	 */
	private void convertAttributes(AttributedElementClass aec) {
		Element attrElem;
		Element domainElem;
		Element rangeElem;
		Element typeElem;

		String aecElemName;
		if (aec instanceof EdgeClass) {
			aecElemName = aec.getQualifiedName() + edgeClassNameSuffix;
		} else {
			aecElemName = aec.getQualifiedName();
		}

		// for every attribute of "aec"
		for (Attribute attr : aec.getOwnAttributeList()) {	
			typeElem = createRdfTypeElement(
					"http://www.w3.org/2002/07/owl#FunctionalProperty");
			domainElem = createRdfsDomainElement("#" + aecElemName);
			rangeElem = createRdfsRangeElement();
			
			// if "attr" has a CompositeDomain or an EnumDomain as type
			if (attr.getDomain().isComposite() 		
					|| attr.getDomain().toString().contains("Enum")) {
				attrElem = createOwlObjectPropertyElement(
						HelperMethods.firstToLowerCase(aecElemName)
						+ "Has" 
						+ HelperMethods.firstToUpperCase(attr.getName()));
				
				if (attr.getDomain().getTGTypeName(null).contains("List<")) {
					rangeElem.setAttribute("rdf:resource", "#ListElement");
				} else if (attr.getDomain().getTGTypeName(null).contains("Set<")) {
					rangeElem.setAttribute("rdf:resource", "#Set");
				} else {
					rangeElem.setAttribute("rdf:resource", 
							"#" + attr.getDomain().getQualifiedName());
				}
			// if "attr" has a BasicDomain as type
			} else {
				attrElem = createOwlDatatypePropertyElement(
						HelperMethods.firstToLowerCase(aecElemName)
						+ "Has" 
						+ HelperMethods.firstToUpperCase(attr.getName()));
								
				if (attr.getDomain().getTGTypeName(null).equals("String")) {
					rangeElem.setAttribute("rdf:resource", 
							"http://www.w3.org/2001/XMLSchema#string");
				} else {
					rangeElem.setAttribute("rdf:resource", 
							"http://www.w3.org/2001/XMLSchema#"	+ attr.getDomain()
									.getJavaAttributeImplementationTypeName(""));
				}
			}
			
			// build subtree
			rdfElem.appendChild(attrElem);
			attrElem.appendChild(typeElem);
			attrElem.appendChild(domainElem);
			attrElem.appendChild(rangeElem);
		}
	}
	
	/**
	 * Creates a Property relating the OWL Class with the ID {@code Edge} to a
	 * {@code string} representing a role name. Whether the role name on the "from" or
	 * "to" side is related depends on the value of the parameter {@code from}.
	 * <br>
	 * XML-code written if {@code appendSuffix2EdgeClassName = false}:<br>
	 * <pre>  
	 *     &lt;owl:DatatypeProperty rdf:ID="EdgeOutRole"&gt;
     *         &lt;rdf:type rdf:resource="http://www.w3.org/2002/07/owl#FunctionalProperty"/&gt;
     *         &lt;rdfs:domain rdf:resource="#Edge"/&gt;
     *         &lt;rdfs:range rdf:resource="http://www.w3.org/2001/XMLSchema#string"/&gt;
     *     &lt;/owl:DatatypeProperty&gt;
	 * </pre> 
	 * 
	 * @param from Indicates whether the Property for the role name on the "from" side or
	 * for the role name on the "to" side shall be created.
	 * @return The created {@code DatatypeProperty}.
	 */
	private Element createRoleElement(boolean from) {
		Element roleElem;
		Element domainElem;
		Element rangeElem;
		Element typeElem;
		
		String direction;
		
		if (from) {
			direction = "Out";
		} else {
			direction = "In";
		}
		

		// create DatatypeProperty
		roleElem = createOwlDatatypePropertyElement(HelperMethods
				.firstToLowerCase("Edge" + edgeClassNameSuffix)
				+ direction + "Role");
		typeElem = createRdfTypeElement("http://www.w3.org/2002/07/owl#FunctionalProperty");
		domainElem = createRdfsDomainElement("#Edge" + edgeClassNameSuffix);
		rangeElem = createRdfsRangeElement("http://www.w3.org/2001/XMLSchema#string");

		// build subtree
		roleElem.appendChild(typeElem);
		roleElem.appendChild(domainElem);
		roleElem.appendChild(rangeElem);

		return roleElem;
	}

	/**
	 * Creates two Properties for each element of array {@code defaultGECs}
	 * which relate them to their containing {@code GraphClass gc} and vice
	 * versa.<br>
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
	 */
	private void createDefaultGECProperties() {
		Element domainElem;
		Element inverseOfElem;
		Element rangeElem;
		Element typeElem;
		Element gecIsInElem;
		Element gcContainsElem;

		for (String gecName : defaultGECs) {
			if (!(gecName.equals("Edge") && edgeClasses2Properties)) {
				// create ObjectProperty relating GraphElementClass to
				// GraphClass
				if (gecName.equals("Edge")) {
					gecIsInElem = createOwlObjectPropertyElement(HelperMethods
							.firstToLowerCase(gecName)
							+ edgeClassNameSuffix + "IsInGraph");
					domainElem = createRdfsDomainElement("#" + gecName
							+ edgeClassNameSuffix);
				} else {
					gecIsInElem = createOwlObjectPropertyElement(HelperMethods
							.firstToLowerCase(gecName)
							+ "IsInGraph");
					domainElem = createRdfsDomainElement("#" + gecName);
				}
				typeElem = createRdfTypeElement("http://www.w3.org/2002/07/owl#FunctionalProperty");
				rangeElem = createRdfsRangeElement("#Graph");

				// build subtree
				rdfElem.appendChild(gecIsInElem);
				gecIsInElem.appendChild(typeElem);
				gecIsInElem.appendChild(domainElem);
				gecIsInElem.appendChild(rangeElem);

				// create ObjectProperty relating GraphClass to
				// GraphElementClass
				if (gecName.equals("Edge")) {
					gcContainsElem = createOwlObjectPropertyElement("graphContains"
							+ gecName + edgeClassNameSuffix);
					inverseOfElem = createOwlInverseOfElement("#"
							+ HelperMethods.firstToLowerCase(gecName)
							+ edgeClassNameSuffix + "IsInGraph");
					rangeElem = createRdfsRangeElement("#" + gecName
							+ edgeClassNameSuffix);
				} else {
					gcContainsElem = createOwlObjectPropertyElement("graphContains"
							+ gecName);
					inverseOfElem = createOwlInverseOfElement("#"
							+ HelperMethods.firstToLowerCase(gecName)
							+ "IsInGraph");
					rangeElem = createRdfsRangeElement("#" + gecName);
				}

				domainElem = createRdfsDomainElement("#Graph");

				// build subtree
				rdfElem.appendChild(gcContainsElem);
				gcContainsElem.appendChild(inverseOfElem);
				gcContainsElem.appendChild(domainElem);
				gcContainsElem.appendChild(rangeElem);
			}
		}
	}

	/**
	 * Creates a subclass restriction restricting the cardinality of the
	 * Property <i>gecName</i>{@code ClassIsIn}<i>gcName</i> to 1. This means
	 * that every {@code GraphElementClass} individual and individuals of its
	 * subclasses only belong to one graph.<br>
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
	 */
	private Element createDefaultGECCardinality(String gecName) {
		Element cardinalityElem;
		Element onPropertyElem;
		Element restrictionElem;
		Element subClassOfElem;

		// create subclass restriction
		subClassOfElem = createRdfsSubClassOfElement();
		restrictionElem = createOwlRestrictionElement();
		if (gecName.equals("Edge")) {
			onPropertyElem = createOwlOnPropertyElement("#"
					+ HelperMethods.firstToLowerCase(gecName)
					+ edgeClassNameSuffix + "IsInGraph");
		} else {
			onPropertyElem = createOwlOnPropertyElement("#"
					+ HelperMethods.firstToLowerCase(gecName) + "IsInGraph");
		}
		cardinalityElem = createOwlCardinalityElement(1);

		// build subtree
		restrictionElem.appendChild(onPropertyElem);
		restrictionElem.appendChild(cardinalityElem);
		subClassOfElem.appendChild(restrictionElem);

		return subClassOfElem;
	}

	/**
	 * Creates an owl:unionOf element which contains a child owl:Class element
	 * for every direct subclass of the {@code AttributedElementClass aec}. This
	 * is a representation for abstract {@code AttributedElementClasses} in OWL.
	 *
	 * @param aec
	 *            The {@code AttributedElementClass} for which the owl:unionOf
	 *            element shall be created.
	 * @return The created owl:unionOf element.
	 */
	private Element createUnionOfSubclasses(AttributedElementClass aec) {
		Element unionOfElem;
		Element classElem;

		// create unionOf element
		unionOfElem = doc.createElement("owl:unionOf");
		unionOfElem.setAttribute("rdf:parseType", "Collection");

		// create owl:Class element for every direct subclass of aec and build
		// the subtree
		for (AttributedElementClass subclass : aec.getDirectSubClasses()) {
			classElem = doc.createElement("owl:Class");

			if (subclass instanceof EdgeClass) {
				classElem.setAttribute("rdf:about", subclass.getQualifiedName()
						+ edgeClassNameSuffix);
			} else {
				classElem
						.setAttribute("rdf:about", subclass.getQualifiedName());
			}

			unionOfElem.appendChild(classElem);
		}

		return unionOfElem;
	}

	/**
	 * Creates and returns an element {@code <owl:Class/>}.
	 *
	 * @return The created element.
	 */
	private Element createOwlClassElement() {
		Element elem = doc.createElement("owl:Class");

		return elem;
	}

	/**
	 * Creates and returns an element {@code <owl:Class rdf:ID = }<i>id</i>
	 * {@code />}
	 *
	 * @param id
	 *            The value for the {@code rdf:ID"} attribute.
	 * @return The created element.
	 */
	private Element createOwlClassElement(String id) {
		Element elem = createOwlClassElement();
		elem.setAttribute("rdf:ID", id);

		return elem;
	}

	/**
	 * Creates and returns an element {@code <owl:ObjectProperty/>}.
	 *
	 * @return The created element.
	 */
	private Element createOwlObjectPropertyElement() {
		Element elem = doc.createElement("owl:ObjectProperty");

		return elem;
	}

	/**
	 * Creates and returns an element {@code <owl:ObjectProperty rdf:ID = }
	 * <i>id</i>{@code />}
	 *
	 * @param id
	 *            The value for the {@code rdf:ID} attribute.
	 * @return The created element.
	 */
	private Element createOwlObjectPropertyElement(String id) {
		Element elem = createOwlObjectPropertyElement();
		elem.setAttribute("rdf:ID", id);

		return elem;
	}

	/**
	 * Creates and returns an element {@code <owl:DatatypeProperty/>}.
	 *
	 * @return The created element.
	 */
	private Element createOwlDatatypePropertyElement() {
		Element elem = doc.createElement("owl:DatatypeProperty");

		return elem;
	}

	/**
	 * Creates and returns an element {@code <owl:DatatypeProperty rdf:ID = }
	 * <i>id</i> {@code />}
	 *
	 * @param id
	 *            The value for the {@code rdf:ID} attribute.
	 * @return The created element.
	 */
	private Element createOwlDatatypePropertyElement(String id) {
		Element elem = createOwlDatatypePropertyElement();
		elem.setAttribute("rdf:ID", id);

		return elem;
	}

	/**
	 * Creates and returns an element {@code <rdfs:domain/>}.
	 *
	 * @return The created element.
	 */
	private Element createRdfsDomainElement() {
		Element elem = doc.createElement("rdfs:domain");

		return elem;
	}

	/**
	 * Creates and returns an element {@code <rdfs:domain rdf:resource = }
	 * <i>resource</i> {@code />}
	 *
	 * @param resource
	 *            The value for the {@code rdf:resource} attribute.
	 * @return The created element.
	 */
	private Element createRdfsDomainElement(String resource) {
		Element elem = createRdfsDomainElement();
		elem.setAttribute("rdf:resource", resource);

		return elem;
	}

	/**
	 * Creates and returns an element {@code <rdfs:range/>}.
	 *
	 * @return The created element.
	 */
	private Element createRdfsRangeElement() {
		Element elem = doc.createElement("rdfs:range");

		return elem;
	}

	/**
	 * Creates and returns an element {@code <rdfs:range rdf:resource = }
	 * <i>resource</i> {@code />}
	 *
	 * @param resource
	 *            The value for the {@code rdf:resource} attribute.
	 * @return The created element.
	 */
	private Element createRdfsRangeElement(String resource) {
		Element elem = createRdfsRangeElement();
		elem.setAttribute("rdf:resource", resource);

		return elem;
	}

	/**
	 * Creates and returns an element {@code <rdf:type/>}.
	 *
	 * @return The created element.
	 */
	private Element createRdfTypeElement() {
		Element elem = doc.createElement("rdf:type");

		return elem;
	}

	/**
	 * Creates and returns an element {@code <rdf:type rdf:resource = }
	 * <i>resource</i> {@code />}
	 *
	 * @param resource
	 *            The value for the {@code rdf:resource} attribute.
	 * @return The created element.
	 */
	private Element createRdfTypeElement(String resource) {
		Element elem = createRdfTypeElement();
		elem.setAttribute("rdf:resource", resource);

		return elem;
	}

	/**
	 * Creates and returns an element {@code <rdfs:subClassOf/>}.
	 *
	 * @return The created element.
	 */
	private Element createRdfsSubClassOfElement() {
		Element elem = doc.createElement("rdfs:subClassOf");

		return elem;
	}

	/**
	 * Creates and returns an element {@code <rdfs:subClassOf rdf:resource = }
	 * <i>resource</i>{@code />}
	 *
	 * @param resource
	 *            The value for the {@code rdf:resource} attribute.
	 * @return The created element.
	 */
	private Element createRdfsSubClassOfElement(String resource) {
		Element elem = createRdfsSubClassOfElement();
		elem.setAttribute("rdf:resource", resource);

		return elem;
	}

	/**
	 * Creates and returns an element {@code <rdfs:subPropertyOf/>}.
	 *
	 * @return The created element.
	 */
	private Element createRdfsSubPropertyOfElement() {
		Element elem = doc.createElement("rdfs:subPropertyOf");

		return elem;
	}

	/**
	 * Creates and returns an element {@code <rdfs:subPropertyOf rdf:resource =
	 * * } <i>resource</i>{@code />}
	 *
	 * @param resource
	 *            The value for the {@code rdf:resource} attribute.
	 * @return The created element.
	 */
	private Element createRdfsSubPropertyOfElement(String resource) {
		Element elem = createRdfsSubPropertyOfElement();
		elem.setAttribute("rdf:resource", resource);

		return elem;
	}

	/**
	 * Creates and returns an element {@code <owl:Restriction/>}.
	 *
	 * @return The created element.
	 */
	private Element createOwlRestrictionElement() {
		Element elem = doc.createElement("owl:Restriction");

		return elem;
	}

	/**
	 * Creates and returns an element {@code <owl:onProperty/>}.
	 *
	 * @return The created element.
	 */
	private Element createOwlOnPropertyElement() {
		Element elem = doc.createElement("owl:onProperty");

		return elem;
	}

	/**
	 * Creates and returns an element {@code <owl:onProperty rdf:resource = }
	 * <i>resource</i>{@code />}
	 *
	 * @param resource
	 *            The value for the {@code rdf:resource} attribute.
	 * @return The created element.
	 */
	private Element createOwlOnPropertyElement(String resource) {
		Element elem = createOwlOnPropertyElement();
		elem.setAttribute("rdf:resource", resource);

		return elem;
	}

	/**
	 * Creates an element {@code <owl:inverseOf/>}.
	 *
	 * @return The created element.
	 */
	private Element createOwlInverseOfElement() {
		Element elem = doc.createElement("owl:inverseOf");

		return elem;
	}

	/**
	 * Creates and returns an element {@code <owl:inverseOf rdf:resource = }
	 * <i>resource</i>{@code />}
	 *
	 * @param resource
	 *            The value for the {@code rdf:resource} attribute.
	 * @return The created element.
	 */
	private Element createOwlInverseOfElement(String resource) {
		Element elem = createOwlInverseOfElement();
		elem.setAttribute("rdf:resource", resource);

		return elem;
	}

	/**
	 * Creates and returns an element {@code <owl:cardinality rdf:datatype =
	 * "http://www.w3.org/2001/XMLSchema#nonNegativeInteger"/>}.
	 *
	 * @return The created element.
	 */
	private Element createOwlCardinalityElement() {
		Element elem = doc.createElement("owl:cardinality");
		elem.setAttribute("rdf:datatype",
				"http://www.w3.org/2001/XMLSchema#nonNegativeInteger");

		return elem;
	}

	/**
	 * Creates and returns an element {@code <owl:cardinality rdf:datatype =
	 * "http://www.w3.org/2001/XMLSchema#nonNegativeInteger">}<i>bound</i>
	 * {@code </owl:minCardinality>}
	 *
	 * @param bound
	 *            The value for the minimum and maximum cardinality.
	 * @return The created element.
	 */
	private Element createOwlCardinalityElement(int bound) {
		Element elem = createOwlCardinalityElement();
		elem.appendChild(doc.createTextNode(String.valueOf(bound)));

		return elem;
	}

	/**
	 * Creates and returns an element {@code <owl:maxCardinality rdf:datatype =
	 * "http://www.w3.org/2001/XMLSchema#nonNegativeInteger"/>}.
	 *
	 * @return The created element.
	 */
	private Element createOwlMaxCardinalityElement() {
		Element elem = doc.createElement("owl:maxCardinality");
		elem.setAttribute("rdf:datatype",
				"http://www.w3.org/2001/XMLSchema#nonNegativeInteger");

		return elem;
	}

	/**
	 * Creates and returns an element {@code <owl:maxCardinality rdf:datatype =
	 * "http://www.w3.org/2001/XMLSchema#nonNegativeInteger">}<i>upperBound</i>
	 * {@code </owl:minCardinality>}
	 *
	 * @param upperBound
	 *            The value for the maximum cardinality.
	 * @return The created element.
	 */
	private Element createOwlMaxCardinalityElement(int upperBound) {
		Element elem = createOwlMaxCardinalityElement();
		elem.appendChild(doc.createTextNode(String.valueOf(upperBound)));

		return elem;
	}

	/**
	 * Creates and returns an element {@code <owl:minCardinality rdf:datatype =
	 * "http://www.w3.org/2001/XMLSchema#nonNegativeInteger"/>}.
	 *
	 * @return The created element.
	 */
	private Element createOwlMinCardinalityElement() {
		Element elem = doc.createElement("owl:minCardinality");
		elem.setAttribute("rdf:datatype",
				"http://www.w3.org/2001/XMLSchema#nonNegativeInteger");

		return elem;
	}

	/**
	 * Creates and returns an element {@code <owl:minCardinality rdf:datatype =
	 * "http://www.w3.org/2001/XMLSchema#nonNegativeInteger">}<i>lowerBound</i>
	 * {@code </owl:minCardinality>}
	 *
	 * @param lowerBound
	 *            The value for the minimum cardinality.
	 * @return The created element.
	 */
	private Element createOwlMinCardinalityElement(int lowerBound) {
		Element elem = createOwlMinCardinalityElement();
		elem.appendChild(doc.createTextNode(String.valueOf(lowerBound)));

		return elem;
	}

}
