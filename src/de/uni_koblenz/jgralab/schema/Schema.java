/*
 * JGraLab - The Java Graph Laboratory
 *
 * Copyright (C) 2006-2014 Institute for Software Technology
 *                         University of Koblenz-Landau, Germany
 *                         ist@uni-koblenz.de
 *
 * For bug reports, documentation and further information, visit
 *
 *                         https://github.com/jgralab/jgralab
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

package de.uni_koblenz.jgralab.schema;

import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.pcollections.PSet;

import de.uni_koblenz.jgralab.Graph;
import de.uni_koblenz.jgralab.GraphFactory;
import de.uni_koblenz.jgralab.ImplementationType;
import de.uni_koblenz.jgralab.ProgressFunction;
import de.uni_koblenz.jgralab.exception.GraphIOException;
import de.uni_koblenz.jgralab.schema.RecordDomain.RecordComponent;
import de.uni_koblenz.jgralab.schema.codegenerator.CodeGeneratorConfiguration;
import de.uni_koblenz.jgralab.schema.impl.compilation.InMemoryJavaSourceFile;

/**
 * The class Schema represents a grUML Schema.
 * 
 * @author ist@uni-koblenz.de
 * 
 */
public interface Schema extends Comparable<Schema> {

	/**
	 * Reserved Java words that are not allowed as a name for any NamedElement
	 * and/or Schema.
	 */
	public static final Set<String> RESERVED_JAVA_WORDS = new TreeSet<>(
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
	 * schema classes, contained in {@code JavaSourceFromString} objects.
	 * 
	 * @param config
	 *            a {@link CodeGeneratorConfiguration} specifying the requested
	 *            implementation variant
	 */
	public ArrayList<InMemoryJavaSourceFile> commit(
			CodeGeneratorConfiguration config);

	/**
	 * after creating the schema, this command serves to make it permanent,
	 * schema classes are generated to represent the object oriented access
	 * layer
	 * 
	 * @param path
	 *            the path to the schema classes which are to be generated
	 * @param config
	 *            a {@link CodeGeneratorConfiguration} specifying the requested
	 *            implementation variant
	 * 
	 * @throws GraphIOException
	 *             if an error occured during optional compilation
	 */
	public void commit(String path, CodeGeneratorConfiguration config)
			throws GraphIOException;

	/**
	 * after creating the schema, this command serves to make it permanent,
	 * schema classes are generated to represent the object oriented access
	 * layer
	 * 
	 * @param path
	 *            the path to the schema classes which are to be generated
	 * @param config
	 *            a {@link CodeGeneratorConfiguration} specifying the requested
	 *            implementation variant
	 * @param progressFunction
	 *            an optional progressfunction
	 * @throws GraphIOException
	 *             if an error occured during optional compilation
	 */
	public void commit(String path, CodeGeneratorConfiguration config,
			ProgressFunction progressFunction) throws GraphIOException;

	/**
	 * After creating the schema, this command serves to generate and compile
	 * code for the schema classes. The class files are not written to disk, but
	 * only held in memory.
	 * 
	 * @param config
	 *            configures the CodeGenerator and which classes and methods to
	 *            be created
	 */
	public void compile(CodeGeneratorConfiguration config);

	/**
	 * Generates Java classes in a temp directory, compiles them, and packs them
	 * in a JAR file.
	 * 
	 * @param config
	 *            the {@link CodeGeneratorConfiguration} to be used for class
	 *            generation
	 * @param jarFileName
	 *            the name of the JAR file to be created
	 * @throws IOException
	 * @throws GraphIOException
	 */
	public void createJAR(CodeGeneratorConfiguration config, String jarFileName)
			throws IOException, GraphIOException;

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
	 * Builds a new enumeration domain, multiple domains may exist in a schema
	 * 
	 * @param qualifiedName
	 *            the qualified name of the {@link EnumDomain}
	 * @param enumComponents
	 *            an array of strings which state the constants of the
	 *            enumeration
	 * @return a new enumeration domain
	 */
	public EnumDomain createEnumDomain(String qualifiedName,
			String... enumComponents);

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

	@Override
	public boolean equals(Object other);

	/**
	 * @param qn
	 * @return the attributed element class with the specified qualified name
	 */
	public <T extends AttributedElementClass<?, ?>> T getAttributedElementClass(
			String qn);

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
	public List<CompositeDomain> getCompositeDomains();

	/**
	 * Returns the default package of this Schema.
	 * 
	 * @return the default package, guaranteed to be != null
	 */
	public Package getDefaultPackage();

	/**
	 * @param domainName
	 *            the unique name of the enum/record domain
	 * @return the enum or record domain with the name domainName
	 */
	public Domain getDomain(String domainName);

	/**
	 * @return all the domains in the schema
	 */
	public PSet<Domain> getDomains();

	public DoubleDomain getDoubleDomain();

	/**
	 * Gets the method to create a new edge with the given name
	 * 
	 * @param edgeClassName
	 *            the name of the edge to create
	 * @return the Method-Object that represents the method to create such edges
	 */
	public Method getEdgeCreateMethod(String edgeClassName,
			ImplementationType implementationType);

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
	 * Gets the method to create a new vertex with the given name
	 * 
	 * @param vertexClassQName
	 *            the qualified name of the vertex to create
	 * @return the Method-Object that represents the method to create such
	 *         vertices
	 */
	public Method getVertexCreateMethod(String vertexClassQName,
			ImplementationType implementationType);

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
	 * Creates a string representation of this schema in the TG language. Do not
	 * use in GraphIO.
	 * 
	 * @return the TG String of this schema
	 */
	public String toTGString();

	public GraphFactory createDefaultGraphFactory(
			ImplementationType implementationType);

	public Graph createGraph(ImplementationType implementationType);

	public Graph createGraph(ImplementationType implementationType, String id,
			int vMax, int eMax);

	/**
	 * @return whether the schema is finished
	 */
	public boolean isFinished();

	/**
	 * Signals that the schema is finished. No more changes are allowed. To open
	 * the change mode call reopen(). The schema has to be finished before
	 * commit or compile.
	 * 
	 * @return true, if the schema was finished by this call, false if it
	 *         already was finished before
	 */
	public boolean finish();

	/**
	 * Returns an internal version number of this schema. This version number is
	 * equal to the number of calls to {@link #finish()} and can be used to
	 * detect whether a schema was modified.
	 * 
	 * @return an internal version number of this Schema
	 */
	public int getVersion();

	/**
	 * Reopens the schema to allow changes. To finish the schema again, call
	 * finish
	 * 
	 * @return true, if the schema was reopened by this call, false if the
	 *         schema was already open (i.e., not yet finished or reopened by a
	 *         previous call).
	 */
	public boolean reopen();

	/**
	 * Returns the number of GraphElementClasses in this Schema, including
	 * default and temporary classes. A Schema implementation assigns numeric
	 * identities to its {@link GraphElementClass}es, starting with 0. This
	 * method determines the total count of {@link GraphElementClass}es in this
	 * schema such that
	 * {@link GraphElementClass#getGraphElementClassIdInSchema()} is guaranteed
	 * to be less than the total number.
	 * 
	 * @return the number of GraphElementClasses in this Schema, including
	 *         default and temporary classes
	 */
	public int getGraphElementClassCount();

	/**
	 * @return the number of {@link IncidenceClass}es in this schema, including
	 *         default and temporary classes
	 */
	public int getIncidenceClassCount();

	public void save(String filename) throws GraphIOException;

	public void save(OutputStream out) throws GraphIOException;

}
