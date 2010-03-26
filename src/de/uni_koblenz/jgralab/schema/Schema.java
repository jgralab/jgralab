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

package de.uni_koblenz.jgralab.schema;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.Vector;

import de.uni_koblenz.jgralab.GraphFactory;
import de.uni_koblenz.jgralab.GraphIOException;
import de.uni_koblenz.jgralab.ProgressFunction;
import de.uni_koblenz.jgralab.codegenerator.CodeGeneratorConfiguration;
import de.uni_koblenz.jgralab.codegenerator.JavaSourceFromString;
import de.uni_koblenz.jgralab.schema.RecordDomain.RecordComponent;

/**
 * The class Schema represents a grUML Schema (M2).
 * 
 * @author ist@uni-koblenz.de
 * 
 */
public interface Schema extends Comparable<Schema> {

	/**
	 * Reserved Java words that are not allowed as a name for any NamedElement
	 * and/or Schema.
	 */
	public static final Set<String> RESERVED_JAVA_WORDS = new TreeSet<String>(
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

	/**
	 * Checks if this schema supports enumeration constants with lowercase
	 * letters.
	 * 
	 * @return true iff the schema allows lowercase enum constants
	 */
	public boolean allowsLowercaseEnumConstants();

	/**
	 * After creating the schema, this command serves to generate code for the
	 * m1 classes, contained in {@code JavaSourceFromString} objects.
	 * 
	 * @param transactionSupport
	 *            create code for transaction support
	 */
	public Vector<JavaSourceFromString> commit(CodeGeneratorConfiguration config);

	/**
	 * after creating the schema, this command serves to make it permanent, m2
	 * classes are generated to represent the object oriented access layer
	 * 
	 * @param path
	 *            the path to the m1 classes which are to be generated
	 * @param transactionSupport
	 *            create code for transaction support
	 * 
	 * @throws GraphIOException
	 *             if an error occured during optional compilation
	 */
	public void commit(String path, CodeGeneratorConfiguration config)
			throws GraphIOException;

	/**
	 * after creating the schema, this command serves to make it permanent, m2
	 * classes are generated to represent the object oriented access layer
	 * 
	 * @param path
	 *            the path to the m1 classes which are to be generated
	 * @param transactionSupport
	 *            create code for transaction support
	 * @param progressFunction
	 *            an optional progressfunction
	 * @throws GraphIOException
	 *             if an error occured during optional compilation
	 */
	public void commit(String path, CodeGeneratorConfiguration config,
			ProgressFunction progressFunction) throws GraphIOException;

	/**
	 * After creating the schema, this command serves to generate and compile
	 * code for the m1 classes. The class files are not written to disk, but
	 * only held in memory.
	 * 
	 * @param config
	 *            configures the CodeGenerator and which classes and methods to
	 *            be created
	 */
	public void compile(CodeGeneratorConfiguration config);

	/**
	 * After creating the schema, this command serves to generate and compile
	 * code for the m1 classes. The class files are not written to disk, but
	 * only held in memory.
	 * 
	 * @param jgralabClassPath
	 *            the classpath to JGraLab
	 * @param config
	 *            configures the CodeGenerator and which classes and methods to
	 *            be created
	 */
	public void compile(String jgralabClassPath,
			CodeGeneratorConfiguration config);

	/**
	 * After creating the schema, this command serves to generate and compile
	 * code for the m1 classes. The class files are not written to disk, but
	 * only held in memory.
	 * 
	 * @param jgralabClassPath
	 *            the classpath to JGraLab
	 */
	public void compile(String jgralabClassPath);

	/**
	 * Creates a new Attribute <code>name</code> with domain <code>dom</code>.
	 * 
	 * @param name
	 *            the attribute name
	 * @param dom
	 *            the domain for the attribute
	 * @param aec
	 *            the {@link AttributedElementClass} owning the
	 *            {@link Attribute}
	 * @param defaultValueAsString
	 *            a String for the default value in TG value syntax, or null if
	 *            no default value is to be set
	 * @return the new Attribute
	 */
	public Attribute createAttribute(String name, Domain dom,
			AttributedElementClass aec, String defaultValueAsString);

	/**
	 * Builds a new enumeration domain, multiple domains may exist in a schema.
	 * 
	 * @param qualifiedName
	 *            the qualified name of the {@link EnumDomain}
	 * @return a new enumeration domain
	 */
	public EnumDomain createEnumDomain(String qualifiedName);

	/**
	 * Builds a new enumeration domain, multiple domains may exist in a schema
	 * 
	 * @param qualifiedName
	 *            the qualified name of the {@link EnumDomain}
	 * @param enumComponents
	 *            a list of strings which state the constants of the enumeration
	 * @return a new enumeration domain
	 */
	public EnumDomain createEnumDomain(String qualifiedName,
			List<String> enumComponents);

	/**
	 * Creates a new {@link GraphClass} and saves it to the schema object
	 * 
	 * @param simpleName
	 *            the simple name of the graphclass in the schema
	 * @return the new graphclass
	 */
	public GraphClass createGraphClass(String simpleName);

	/**
	 * builds a new list domain, multiple domains may exist in a schema
	 * 
	 * @param baseDomain
	 *            the domain of which all elements in the list are built of
	 * @return the new list domain
	 */
	public ListDomain createListDomain(Domain baseDomain);

	/**
	 * builds a new map domain, multiple domains may exist in a schema
	 * 
	 * @param keyDomain
	 *            the domain of which all keys in the set are built of
	 * @param valueDomain
	 *            the domain of which all values in the set are built of
	 * @return the new map domain
	 */
	public MapDomain createMapDomain(Domain keyDomain, Domain valueDomain);

	/**
	 * builds a new record domain, multiple domains may exist in a schema
	 * 
	 * @param qualifiedName
	 *            the qualified name of this RecordDomain
	 * @return the new record domain
	 */
	public RecordDomain createRecordDomain(String qualifiedName);

	/**
	 * builds a new record domain, multiple domains may exist in a schema
	 * 
	 * @param qualifiedName
	 *            the qualified name of this RecordDomain
	 * @param recordComponents
	 *            a list of record domain components which state the individual
	 *            components of the record, each consisting of a name and a
	 *            domain, and possibly a default value
	 * @return the new record domain
	 */
	public RecordDomain createRecordDomain(String qualifiedName,
			Collection<RecordComponent> recordComponents);

	/**
	 * builds a new set domain, multiple domains may exist in a schema
	 * 
	 * @param baseDomain
	 *            the domain of which all elements in the set are built of
	 * @return the new set domain
	 */
	public SetDomain createSetDomain(Domain baseDomain);

	public boolean equals(Object other);

	/**
	 * @param qn
	 * @return the attributed element class with the specified qualified name
	 */
	public AttributedElementClass getAttributedElementClass(String qn);

	public BooleanDomain getBooleanDomain();

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
	 * Returns the default package of this Schema.
	 * 
	 * @return the default package, guaranteed to be != null
	 */
	public Package getDefaultPackage();

	/**
	 * @return the default VertexClass of the schema, that is the VertexClass
	 *         with the name "Vertex"
	 */
	public VertexClass getDefaultVertexClass();

	/**
	 * @param domainName
	 *            the unique name of the enum/record domain
	 * @return the enum or record domain with the name domainName
	 */
	public Domain getDomain(String domainName);

	/**
	 * @return all the domains in the schema
	 */
	public Map<String, Domain> getDomains();

	public DoubleDomain getDoubleDomain();

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
	 * Gets the method to create a new edge with the given name
	 * 
	 * @param edgeClassName
	 *            the name of the edge to create
	 * @return the Method-Object that represents the method to create such edges
	 */
	public Method getEdgeCreateMethod(String edgeClassName,
			boolean transactionSupport);

	/**
	 * Returns a list of all enum domains
	 * 
	 * @return a list of all enum domains
	 */
	public List<EnumDomain> getEnumDomains();

	public String getFileName();

	/**
	 * @return the {@link GraphClass} defined by this schema
	 */
	public GraphClass getGraphClass();

	/**
	 * Gets the method to create a new graph of this schema
	 * 
	 * @return the Method-Object that represents the method to create graphs of
	 *         this schema
	 */
	public Method getGraphCreateMethod(boolean transactionSupport);

	/**
	 * @return the factory that is used to create graphs, vertices and edges
	 */
	public GraphFactory getGraphFactory();

	public IntegerDomain getIntegerDomain();

	public String getName();

	public LongDomain getLongDomain();

	/**
	 * @param qn
	 *            the qualified name of the package
	 * @return the package name packageName
	 */
	public Package getPackage(String qn);

	public String getPackagePrefix();

	/**
	 * @return all packages in the schema
	 */
	public Map<String, Package> getPackages();

	public String getPathName();

	public String getQualifiedName();

	/**
	 * Returns a list of all record domains
	 * 
	 * @return a list of all record domains
	 */
	public List<RecordDomain> getRecordDomains();

	public StringDomain getStringDomain();

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
	 * Gets the method to create a new vertex with the given name
	 * 
	 * @param vertexClassQName
	 *            the qualified name of the vertex to create
	 * @return the Method-Object that represents the method to create such
	 *         vertices
	 */
	public Method getVertexCreateMethod(String vertexClassQName,
			boolean transactionSupport);

	public boolean isSimpleNameUnique(String sn);

	/**
	 * Checks if the given name is a valid enumeration constant in this Schema.
	 * 
	 * @param name
	 *            the constant name to check
	 * @return true if <code>name</code> is a valid enum constant
	 */
	public boolean isValidEnumConstant(String name);

	/**
	 * Checks if the given <code>qualifiedName</code> is already known in this
	 * Schema. If this is the case, it's not allowed to use it for any other
	 * element in this schema. Even it'S not allowed to use a domain name also
	 * as name of a VertexClass.
	 * 
	 * @param qualifiedName
	 *            the qualified name to check for
	 * @return true if the name is already known, false otherwise
	 */
	public boolean knows(String qualifiedName);

	/**
	 * Returns the NamedElement with the given <code>qualifiedName</code>.
	 * 
	 * @param qualifiedName
	 *            the qualified name of the desired element
	 * @return the corresponding NamedElement, or null if no such element exists
	 */
	public NamedElement getNamedElement(String qualifiedName);

	/**
	 * Sets the schema to allow lowercase enum constants
	 * 
	 * @param allowLowercaseEnumConstants
	 *            set to true to make the schema to allow lowercase enum
	 *            constants
	 */
	public void setAllowLowercaseEnumConstants(
			boolean allowLowercaseEnumConstants);

	/**
	 * sets the factory that is used to create graphs, vertices and edges
	 */
	public void setGraphFactory(GraphFactory factory);

}
