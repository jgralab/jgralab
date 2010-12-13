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

package de.uni_koblenz.jgralab.utilities.xml;

public class XMLConstants {

	public static final String XSD_NAMESPACE_PREFIX = "xsd";
	public static final String XSD_INSTANCE_NAMESPACE_PREFIX = "xsi";

	public static final String XSD_DOMAIN_STRING = "string";
	public static final String XSD_DOMAIN_DOUBLE = "double";
	public static final String XSD_DOMAIN_LONG = "long";
	public static final String XSD_DOMAIN_INTEGER = "integer";
	public static final String XML_DOMAIN_IDREF = "IDREF";
	public static final String XML_DOMAIN_ID = "ID";

	public static final String XML_VALUE_TRUE = "true";
	public static final String XML_VALUE_FALSE = "false";

	public static final String XSD_COMPLEXCONTENT = "complexContent";
	public static final String XSD_ENUMERATION = "enumeration";
	public static final String XSD_RESTRICTION = "restriction";
	public static final String XSD_SIMPLETYPE = "simpleType";
	public static final String XSD_REQUIRED = "required";
	public static final String XSD_SCHEMA = "schema";
	public static final String XSD_ATTRIBUTE = "attribute";
	public static final String XSD_CHOICE = "choice";
	public static final String XSD_EXTENSION = "extension";
	public static final String XSD_PATTERN = "pattern";
	public static final String XSD_ELEMENT = "element";
	public static final String XSD_COMPLEXTYPE = "complexType";

	public static final String XSD_ATTRIBUTE_ABSTRACT = "abstract";
	public static final String XSD_ATTRIBUTE_TYPE = "type";
	public static final String XSD_ATTRIBUTE_NAME = "name";
	public static final String XSD_ATTRIBUTE_VALUE = "value";
	public static final String XSD_ATTRIBUTE_USE = "use";
	public static final String XSD_ATTRIBUTE_BASE = "base";

	public static final String XSD_VALUE_MAX_OCCURS = "maxOccurs";
	public static final String XSD_VALUE_MIN_OCCURS = "minOccurs";

	public static final String GRUML_ATTRIBUTE_TSEQ = "TSEQ";
	public static final String GRUML_ATTRIBUTE_TO = "TO";
	public static final String GRUML_ATTRIBUTE_FSEQ = "FSEQ";
	public static final String GRUML_ATTRIBUTE_FROM = "FROM";
	public static final String GRUML_ATTRIBUTE_ID = "ID";

	public static final String GRUML_ID_PREFIX_GRAPH = "g";
	public static final String GRUML_ID_PREFIX_VERTEX = "v";

	public static final String GRUML_VALUE_TRUE = "t";
	public static final String GRUML_VALUE_FALSE = "f";
	public static final String GRUML_VALUE_NULL = "n";
	public static final String[] GRUML_VALUES_OF_DOMAIN_BOOLEAN = {
			GRUML_VALUE_FALSE, GRUML_VALUE_TRUE };

	public static final String GRUML_PREFIX_DOMAINTYPE = "ST_";
	public static final String GRUML_PREFIX_GRAPHTYPE = "GT_";
	public static final String GRUML_PREFIX_VERTEXTYPE = "VT_";
	public static final String GRUML_PREFIX_EDGETYPE = "ET_";
	public static final String GRUML_PREFIX_BASETYPE = "BT_";

	public static final String GRUML_ATTRIBUTEDELEMENTTYPE = GRUML_PREFIX_BASETYPE
			+ "AttributedElement";
	public static final String GRUML_GRAPHTYPE = GRUML_PREFIX_BASETYPE
			+ "Graph";
	public static final String GRUML_VERTEXTYPE = GRUML_PREFIX_BASETYPE
			+ "Vertex";
	public static final String GRUML_EDGETYPE = GRUML_PREFIX_BASETYPE + "Edge";

	public static final String GRUML_DOMAIN_RECORD_PREFIX = GRUML_PREFIX_DOMAINTYPE
			+ "RECORD_";
	public static final String GRUML_DOMAIN_SET = GRUML_PREFIX_DOMAINTYPE
			+ "SET";
	public static final String GRUML_DOMAIN_LIST = GRUML_PREFIX_DOMAINTYPE
			+ "LIST";
	public static final String GRUML_DOMAIN_MAP = GRUML_PREFIX_DOMAINTYPE
			+ "MAP";
	public static final String GRUML_DOMAIN_BOOLEAN = GRUML_PREFIX_DOMAINTYPE
			+ "BOOLEAN";
	public static final String GRUML_DOMAIN_STRING = GRUML_PREFIX_DOMAINTYPE
			+ "STRING";
	public static final String GRUML_DOMAIN_INTEGER = GRUML_PREFIX_DOMAINTYPE
			+ "INTEGER";
	public static final String GRUML_DOMAIN_LONG = GRUML_PREFIX_DOMAINTYPE
			+ "LONG";
	public static final String GRUML_DOMAIN_DOUBLE = GRUML_PREFIX_DOMAINTYPE
			+ "DOUBLE";
	public static final String GRUML_DOMAIN_ENUM_PREFIX = GRUML_PREFIX_DOMAINTYPE
			+ "ENUM_";
}
