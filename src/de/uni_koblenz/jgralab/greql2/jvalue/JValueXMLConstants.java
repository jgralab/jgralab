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
/**
 * 
 */
package de.uni_koblenz.jgralab.greql2.jvalue;

public interface JValueXMLConstants {
	public static final String JVALUE = "jvalue";

	public static final String BOOLEAN = "boolean";

	public static final String INTEGER = "integer";
	public static final String LONG = "long";
	public static final String DOUBLE = "double";

	public static final String STRING = "string";

	public static final String ENUM = "enum";

	public static final String GRAPH = "graph";
	public static final String VERTEX = "vertex";
	public static final String EDGE = "edge";

	public static final String ATTRIBUTEDELEMENTCLASS = "attributedElementClass";

	public static final String RECORD = "record";
	public static final String RECORD_COMPONENT = "comp";
	public static final String TUPLE = "tup";
	public static final String LIST = "list";
	public static final String SET = "set";
	public static final String BAG = "bag";
	public static final String MAP = "map";
	public static final String MAP_ENTRY = "entry";
	public static final String TABLE = "table";

	public static final String ATTR_ID = "id";
	public static final String ATTR_SCHEMA = "schema";
	public static final String ATTR_NAME = "name";
	public static final String ATTR_VALUE = "value";
	public static final String ATTR_TYPE = "type";
	public static final String ATTR_GRAPH_ID = "graphId";
	public static final String ATTR_GRAPH_LINK = "graphLink";
	public static final String ATTR_VERTEX_LINK = "vertexLink";
	public static final String ATTR_EDGE_LINK = "edgeLink";
}
