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

package de.uni_koblenz.jgralab.schema;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.Vector;

import de.uni_koblenz.jgralab.Attribute;
import de.uni_koblenz.jgralab.AttributedElement;
import de.uni_koblenz.jgralab.GraphFactory;
import de.uni_koblenz.jgralab.GraphIOException;
import de.uni_koblenz.jgralab.ProgressFunction;
import de.uni_koblenz.jgralab.codegenerator.JavaSourceFromString;

/**
 * represents a schema, aggregates all graph/vertex/edge/aggregation/composition
 * classes, holds all domains and can generate the m2 classes
 * 
 * @author Steffen Kahle
 * 
 */
public interface Schema extends NamedElement {
	
	/**
	 * These are words that are reserved by Java itself and may not be used for
	 * element names
	 */
	public static final Set<String> reservedJavaWords = new TreeSet<String>(
			Arrays.asList(new String[] { "abstract", "continue", "for", "new",
					"switch", "assert", "default", "goto", "package",
					"synchronized", "boolean", "do", "if", "private", "this",
					"break", "double", "implements", "protected", "throw",
					"byte", "else", "import", "public", "throws", "case",
					"enum", "instanceof", "return", "transient", "catch",
					"extends", "int", "short", "try", "char", "final",
					"interface", "static", "void", "class", "finally", "long",
					"strictfp", "volatile", "const", "float", "native",
					"super", "while" }));

	public static final Set<String> basicDomains = new TreeSet<String>(Arrays
			.asList(new String[] { "Boolean", "Integer", "Long", "String",
					"Double", "Object" }));

	public static final Set<String> reservedTGWords = new TreeSet<String>(Arrays
			.asList(new String[] { "abstract", "aggregate", "AggregationClass",
					"Boolean", "CompositionClass", "Double", "EdgeClass",
					"EnumDomain", "f", "from", "Graph", "GraphClass",
					"Integer", "List", "Long", "Object", "Package",
					"RecordDomain", "redefines", "role", "Schema", "Set",
					"String", "t", "to", "VertexClass", "id" }));

	/**
	 * sets the factory that is used to create graphs, vertices and edges
	 */
	public void setGraphFactory(GraphFactory factory);

	/**
	 * @return the factory that is used to create graphs, vertices and edges
	 */
	public GraphFactory getGraphFactory();

	/**
	 * Creates a new Attribute <code>name</code> with domain <code>dom</code>.
	 * 
	 * @param name
	 *            the attribute name
	 * @param dom
	 *            the domain for the attribute
	 * @return the new Attribute
	 */
	public Attribute createAttribute(String name, Domain dom);

	/**
	 * builds a new graphclass and saves it to the schema object
	 * 
	 * @param id
	 *            the unique identifier of the graphclass in the schema
	 * @return the new graphclass
	 */
	public GraphClass createGraphClass(QualifiedName id);

	/**
	 * builds a new enumeration domain, multiple domains may exist in a schema
	 * 
	 * @param qn
	 *            a unique name which identifies the enum in the schema
	 * @param enumComponents
	 *            a list of strings which state the constants of the enumeration
	 * @return a new enumeration domain
	 */
	public EnumDomain createEnumDomain(QualifiedName qn, List<String> enumComponents);

	/**
	 * builds a new enumeration domain, multiple domains may exist in a schema
	 * 
	 * @param qn
	 *            a unique name which identifies the enum in the schema
	 * @return a new enumeration domain
	 */
	public EnumDomain createEnumDomain(QualifiedName qn);

	/**
	 * builds a new list domain, multiple domains may exist in a schema
	 * 
	 * @param baseDomain
	 *            the domain of which all elements in the list are built of
	 * @return the new list domain
	 */
	public ListDomain createListDomain(Domain baseDomain);

	/**
	 * builds a new set domain, multiple domains may exist in a schema
	 * 
	 * @param baseDomain
	 *            the domain of which all elements in the set are built of
	 * @return the new set domain
	 */
	public SetDomain createSetDomain(Domain baseDomain);

	/**
	 * builds a new record domain, multiple domains may exist in a schema
	 * 
	 * @param qn
	 *            a unique name which identifies the record in the schema
	 * @param recordComponents
	 *            a list of record domain components which state the individual
	 *            components of the record, each consisting of a name and a
	 *            domain
	 * @return the new record domain
	 */
	public RecordDomain createRecordDomain(QualifiedName qn,
			Map<String, Domain> recordComponents);

	/**
	 * builds a new record domain, multiple domains may exist in a schema
	 * 
	 * @param qn
	 *            a unique name which identifies the record in the schema
	 * @return the new record domain
	 */
	public RecordDomain createRecordDomain(QualifiedName qn);

	/**
	 * after creating the schema, this command serves to make it permanent, m2
	 * classes are generated to represent the object oriented access layer
	 * 
	 * @param path
	 *            the path to the m1 classes which are to be generated
	 * 
	 * 
	 * @throws GraphIOException
	 *             if an error occured during optional compilation
	 */
	public void commit(String path) throws GraphIOException;

	/**
	 * after creating the schema, this command serves to make it permanent, m2
	 * classes are generated to represent the object oriented access layer
	 * 
	 * @param path
	 *            the path to the m1 classes which are to be generated
	 * @param progressFunction
	 *            an optional progressfunction
	 * @throws GraphIOException
	 *             if an error occured during optional compilation
	 */
	public void commit(String path, ProgressFunction progressFunction)
			throws GraphIOException;

	/**
	 * After creating the schema, this command serves to generate code for the
	 * m1 classes, contained in {@code JavaSourceFromString} objects.
	 */
	public Vector<JavaSourceFromString> commit();

	/**
	 * After creating the schema, this command serves to generate and compile
	 * code for the m1 classes. The class files are not written to disk, but
	 * only held in memory.
	 */
	public void compile();

	/**
	 * After creating the schema, this command serves to generate and compile code
	 * for the m1 classes. The class files are not written to disk, but only held in
	 * memory.
	 * 
	 * @param jgralabClassPath the classpath to JGraLab
	 */
	public void compile(String jgralabClassPath);

	/**
	 * @param aGraphClass
	 *            the graph class which is being searched for
	 * @return true, if the graph class is part of the schema
	 */
	public boolean containsGraphClass(GraphClass aGraphClass);

	/**
	 * @param id
	 *            the unique identifier of the graph class in the schema
	 * @return the graph class associated with the id
	 */
	public GraphClass getGraphClass(QualifiedName id);

	/**
	 * @return the textual representation of the schema with all graph classes,
	 *         their edge and vertex classes, all attributes and the whole
	 *         hierarchy of those classes
	 */
	public String toString();

	/**
	 * sets the prefix for the generation of the m2 elements, the prefix is
	 * added prior to the generated package, example: prefix.schemaname
	 * 
	 * @param prefix
	 *            the prefix to set
	 */
	// public void setPrefix(String prefix);
	/**
	 * @param anAttributedElement
	 *            the element which class should be returned
	 * @return the m2 element of anAttributedElement (instances of
	 *         AttributedElementClass)
	 */
	public AttributedElementClass getClass(AttributedElement anAttributedElement);

	/**
	 * @return all the graph classes in the schema
	 */
	public Map<QualifiedName, GraphClass> getGraphClasses();

	/**
	 * @return all the domains in the schema
	 */
	public Map<QualifiedName, Domain> getDomains();
	
	/**
	 * @return all packages in the schema
	 */
	public Map<QualifiedName, Package> getPackages();

	/**
	 * @param id
	 * @return the attributed element class with the specified id
	 */
	public AttributedElementClass getAttributedElementClass(QualifiedName id);

	/**
	 * @param domainName
	 *            the unique name of the enum/record domain
	 * @return the enum or record domain with the name domainName
	 */
	public Domain getDomain(QualifiedName domainName);
	
	/**
	 * @param packageName
	 *            the unique name of the package
	 * @return the package name packageName
	 */
	public Package getPackage(String packageName);

	/**
	 * @param domainName
	 *            the unique name of the enum/record domain
	 * @return the enum or record domain with the name domainName
	 */
	public Domain getDomain(String domainName);

	/**
	 * Gets the method to create a new graphwith the given name
	 * 
	 * @param graphClassName
	 *            the name of the graph class
	 * @return the Method-Object that represents the method to create such
	 *         graphs
	 */
	public Method getGraphCreateMethod(QualifiedName graphClassName);

	/**
	 * Gets the method to create a new vertex with the given name
	 * 
	 * @param vertexClassName
	 *            the name of the vertex to create
	 * @param graphClassName
	 *            the name of the graph class the VertexClass belongs to
	 * @return the Method-Object that represents the method to create such
	 *         vertices
	 */
	public Method getVertexCreateMethod(QualifiedName vertexClassName,
			QualifiedName graphClassName);

	/**
	 * Gets the method to create a new edge with the given name
	 * 
	 * @param edgeClassName
	 *            the name of the edge to create
	 * @param graphClassName
	 *            the name of the graph class the EdgeClass belongs to
	 * @return the Method-Object that represents the method to create such edges
	 */
	public Method getEdgeCreateMethod(QualifiedName edgeClassName,
			QualifiedName graphClassName);

	/**
	 * @return the default AggregationClass of the schema, that is the
	 *         AggregationClass with the name "Aggregation"
	 */
	public AggregationClass getDefaultAggregationClass();

	/**
	 * @return the default CompositionClass of the schema, that is the
	 *         CompositionClass with the name "Composition"
	 */
	public CompositionClass getDefaultCompositionClass();

	/**
	 * @return the default EdgeClass of the schema, that is the EdgeClass with
	 *         the name "Edge"
	 */
	public EdgeClass getDefaultEdgeClass();

	/**
	 * @return the default GraphClass of the schema, that is the GraphClass with
	 *         the name "Graph"
	 */
	public GraphClass getDefaultGraphClass();

	/**
	 * @return the default VertexClass of the schema, that is the VertexClass
	 *         with the name "Vertex"
	 */
	public VertexClass getDefaultVertexClass();

	/**
	 * Returns an topologically ordered list of all graph classes, i.e. the
	 * graph classes are ordered according to their inheritance hierarchy.
	 * First, the list contains the classes without a superclass (except the
	 * default graph class). The next entries in the list represent those graph
	 * classes which only inherit from the classes without a superclass, etc.
	 * 
	 * @return an topologically ordered list of all graph classes
	 */
	public List<GraphClass> getGraphClassesInTopologicalOrder();

	/**
	 * Returns a list of all enum domains
	 * 
	 * @return a list of all enum domains
	 */
	public List<EnumDomain> getEnumDomains();

	
	/**
	 * Returns a list of all record domains
	 * 
	 * @return a list of all record domains
	 */
	public List<RecordDomain> getRecordDomains();

	/**
	 * Returns an topologically ordered list of all composite domains, i.e. the
	 * domains are ordered according to the hierarchy of their components.
	 * First, the list contains the domains which only contain basic domains.
	 * The next entries in the list represent those domains which exclusively
	 * contain domains with only basic classes as components, etc.
	 * 
	 * @return an topologically ordered list of all composite domains
	 */
	public List<CompositeDomain> getCompositeDomainsInTopologicalOrder();

	/**
	 * Returns an topologically ordered list of all vertex classes in the
	 * schema, i.e. the vertex classes are ordered according to their
	 * inheritance hierarchy. First, the list contains the classes without a
	 * superclass (except the default vertex class). The next entries in the
	 * list represent those vertex classes which only inherit from the classes
	 * without a superclass, etc.
	 * 
	 * @return an topologically ordered list of all vertex classes
	 */
	public List<VertexClass> getVertexClassesInTopologicalOrder();

	/**
	 * Returns an topologically ordered list of all edge classes in the schema
	 * (including aggregation and composition classes), i.e. the edge classes
	 * are ordered according to their inheritance hierarchy. First, the list
	 * contains the classes without a superclass (except the default edge
	 * class). The next entries in the list represent those edge classes which
	 * only inherit from the classes without a superclass, etc.
	 * 
	 * @return an topologically ordered list of all edge classes
	 */
	public List<EdgeClass> getEdgeClassesInTopologicalOrder();

	/**
	 * Checks if the given name is already known in thsi schema. If this is the
	 * case, it's not allowed to use it for any other element in this schema.
	 * Even it'S not allowed to use a domain name also as name of a VertexClass.
	 * 
	 * @param name
	 *            the name to check
	 * @return true if the name is already known, false otherwise
	 */
	public boolean knows(QualifiedName name);

	/**
	 * Checks if the given name is a valid name for a new Element in this
	 * schema. This includes that it's not the name of a Java-Keyword and not
	 * already in use by any other element in this schema
	 * 
	 * @param name
	 *            the name to check
	 * @return true, if the given name can be used for a new element in the
	 *         schema, false otherwise
	 * @see #isValidSchemaElementName(QualifiedName)
	 * @see #knows
	 */
	public boolean isFreeSchemaElementName(QualifiedName name);

	/**
	 * Checks if the given name is a valid name for a new domain in this schema.
	 * This includes that it's not the name of a Java-Keyword and not already in
	 * use by any other element in this schema. This is not the same as
	 * isFreeSchemaElementName, because some words, like "String" or "Integer"
	 * can be used for the domains but not for any other element
	 * 
	 * @param name
	 *            the name to check
	 * @return true, if the given name can be used for a new domain in the
	 *         schema, false otherwise
	 * @see #isValidSchemaElementName(QualifiedName)
	 * @see #knows(QualifiedName)
	 */
	public boolean isFreeDomainName(QualifiedName name);

	/**
	 * Checks if the given name is a allowed name for a element in this schema.
	 * Words that are reserved by the .tg-format or Java itself are not allowed
	 * as element names. If you want to know if you may create a new element
	 * with the name in the schema, use isFreeSchemaElementName instead, it
	 * checks if the name is allowed and if there already exists a element with
	 * this name.
	 * 
	 * @param name
	 *            the name to check
	 * @return true if the given name is an allowed element name, false
	 *         otherwise
	 * @see #isFreeDomainName(QualifiedName)
	 * @see #knows(QualifiedName)
	 */
	public boolean isValidSchemaElementName(QualifiedName name);

	/**
	 * Returns the default package of this Schema.
	 * 
	 * @return the default package, guaranteed to be != null
	 */
	public Package getDefaultPackage();

	public Package createPackageWithParents(String qualifiedName);

	public void addPackage(Package p);
	
	/**
	 * This method is for internally use of JGraLab only.
	 * It registers the given element <code>elem</code>
	 * under the unique name <code>name</code>
	 * @param name
	 * @param elem
	 */
	public void addToKnownElements(String name, NamedElement elem);
	
	/**
	 * Checks if this schema supports enumeration constants 
	 * with lowercase letters is
	 * @return true iff the schema allows lowercase enum constants
	 */
	public boolean allowsLowercaseEnumConstants();

	
	/**
	 * Sets the schema to allow lowercase enum constants
	 * @param allowLowercaseEnumConstants set to true to make the schema
	 *  to allow lowercase enum constants 
	 */
	public void setAllowLowercaseEnumConstants(boolean allowLowercaseEnumConstants);

	/**
	 * Checks if the given name is a valid enumeration constant in this 
	 * shcema
	 * @param name the constant name to check
	 * @return true if <code>name</code> is a valid enum constant
	 */
	public boolean isValidEnumConstant(String name);
}
