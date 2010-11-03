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

package de.uni_koblenz.jgralab.schema.impl;

import java.io.File;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.Vector;
import java.util.regex.Pattern;

import javax.tools.FileObject;
import javax.tools.ForwardingJavaFileManager;
import javax.tools.JavaCompiler;
import javax.tools.JavaFileManager;
import javax.tools.JavaFileObject;
import javax.tools.ToolProvider;
import javax.tools.JavaFileObject.Kind;

import de.uni_koblenz.jgralab.Graph;
import de.uni_koblenz.jgralab.GraphFactory;
import de.uni_koblenz.jgralab.GraphIOException;
import de.uni_koblenz.jgralab.ImplementationType;
import de.uni_koblenz.jgralab.M1ClassManager;
import de.uni_koblenz.jgralab.ProgressFunction;
import de.uni_koblenz.jgralab.codegenerator.ClassFileAbstraction;
import de.uni_koblenz.jgralab.codegenerator.CodeGenerator;
import de.uni_koblenz.jgralab.codegenerator.CodeGeneratorConfiguration;
import de.uni_koblenz.jgralab.codegenerator.EdgeCodeGenerator;
import de.uni_koblenz.jgralab.codegenerator.EnumCodeGenerator;
import de.uni_koblenz.jgralab.codegenerator.GraphCodeGenerator;
import de.uni_koblenz.jgralab.codegenerator.GraphFactoryGenerator;
import de.uni_koblenz.jgralab.codegenerator.JavaSourceFromString;
import de.uni_koblenz.jgralab.codegenerator.RecordCodeGenerator;
import de.uni_koblenz.jgralab.codegenerator.ReversedEdgeCodeGenerator;
import de.uni_koblenz.jgralab.codegenerator.SchemaCodeGenerator;
import de.uni_koblenz.jgralab.codegenerator.VertexCodeGenerator;
import de.uni_koblenz.jgralab.schema.Attribute;
import de.uni_koblenz.jgralab.schema.AttributedElementClass;
import de.uni_koblenz.jgralab.schema.BooleanDomain;
import de.uni_koblenz.jgralab.schema.CompositeDomain;
import de.uni_koblenz.jgralab.schema.Domain;
import de.uni_koblenz.jgralab.schema.DoubleDomain;
import de.uni_koblenz.jgralab.schema.EdgeClass;
import de.uni_koblenz.jgralab.schema.EnumDomain;
import de.uni_koblenz.jgralab.schema.GraphClass;
import de.uni_koblenz.jgralab.schema.IntegerDomain;
import de.uni_koblenz.jgralab.schema.ListDomain;
import de.uni_koblenz.jgralab.schema.LongDomain;
import de.uni_koblenz.jgralab.schema.MapDomain;
import de.uni_koblenz.jgralab.schema.NamedElement;
import de.uni_koblenz.jgralab.schema.Package;
import de.uni_koblenz.jgralab.schema.RecordDomain;
import de.uni_koblenz.jgralab.schema.Schema;
import de.uni_koblenz.jgralab.schema.SetDomain;
import de.uni_koblenz.jgralab.schema.StringDomain;
import de.uni_koblenz.jgralab.schema.VertexClass;
import de.uni_koblenz.jgralab.schema.RecordDomain.RecordComponent;
import de.uni_koblenz.jgralab.schema.exception.InvalidNameException;
import de.uni_koblenz.jgralab.schema.exception.M1ClassAccessException;
import de.uni_koblenz.jgralab.schema.exception.SchemaException;

/**
 * @author ist@uni-koblenz.de
 */
public class SchemaImpl implements Schema {
	/**
	 * File Manager class overwriting the method {@code getJavaFileForOutput} so
	 * that bytecode is written to a {@code ClassFileAbstraction}.
	 * 
	 */
	private class ClassFileManager extends
			ForwardingJavaFileManager<JavaFileManager> {

		public ClassFileManager(JavaFileManager fm) {
			super(fm);
		}

		@Override
		public JavaFileObject getJavaFileForOutput(Location location,
				String className, Kind kind, FileObject sibling) {
			ClassFileAbstraction cfa = new ClassFileAbstraction(className);
			M1ClassManager.instance(qualifiedName).putM1Class(className, cfa);
			return cfa;
		}
	}

	// we need a hard reference here, cause the M1ClassManager uses only weak
	// references. This way, when the schema gets collected, the class manager
	// is free for collection, too.
	private M1ClassManager m1ClassManager = null;

	public M1ClassManager getM1ClassManager() {
		return m1ClassManager;
	}

	// TODO Remove
	private static final String GRAPH_IMPLEMENTATION_PACKAGE = "array";

	static final Class<?>[] GRAPHCLASS_CREATE_SIGNATURE = { String.class,
			int.class, int.class };

	/**
	 * This is the name of the package into which the implementation classes for
	 * this schema are generated. The impl package is child of the package for
	 * the Schema.
	 */
	public static final String IMPL_PACKAGE_NAME = "impl";

	/**
	 * This is the name of the package into which the implementation classes for
	 * this schema are generated. The impl package is child of the package for
	 * Schema.
	 */
	public static final String IMPLSTDPACKAGENAME = "impl.std";
	public static final String IMPLTRANSPACKAGENAME = "impl.trans";
	public static final String IMPLDATABASEPACKAGENAME = "impl.db";
	public static final String IMPLSAVEMEMPACKAGENAME = "impl.savemem";

	static final Class<?>[] VERTEX_CLASS_CREATE_SIGNATURE = { int.class };

	/**
	 * Toggles if the schema allows lowercase enumeration constants
	 */
	private boolean allowLowercaseEnumConstants = true;

	private EdgeClass defaultEdgeClass;

	private GraphClass defaultGraphClass;

	private Package defaultPackage;

	private VertexClass defaultVertexClass;

	protected CodeGeneratorConfiguration config;

	/**
	 * Maps from qualified name to the {@link Domain}.
	 */
	private Map<String, Domain> domains = new HashMap<String, Domain>();

	/**
	 * Holds a reference to the {@link GraphClass} of this schema (not the
	 * default graph class {@link GraphClass})
	 */
	private GraphClass graphClass;

	/**
	 * The {@link GraphFactory} for this schemas {@link GraphClass}, see {
	 * {@link #graphClass}.
	 */
	protected GraphFactory graphFactory;

	/**
	 * The name of this schema without the package prefix.
	 */
	private String name;

	/**
	 * The package prefix of this schema.
	 */
	private String packagePrefix;

	/**
	 * Maps from simple names to a set of {@link NamedElement}s which have this
	 * simple name. Used for creation of unique names.
	 */
	private Map<String, Set<NamedElement>> namedElementsBySimpleName = new HashMap<String, Set<NamedElement>>();

	/**
	 * Maps from qualified name to the {@link Package} with that qualified name.
	 */
	private Map<String, Package> packages = new TreeMap<String, Package>();

	/**
	 * The qualified name of this schema, that is {@link #packagePrefix} DOT
	 * {@link #name}
	 */
	private String qualifiedName;

	/**
	 * A set of all qualified names known to this schema.
	 */
	private Map<String, NamedElement> namedElements = new TreeMap<String, NamedElement>();

	private BooleanDomain booleanDomain;

	private DoubleDomain doubleDomain;

	private IntegerDomain integerDomain;

	private LongDomain longDomain;

	private StringDomain stringDomain;

	private static final Pattern SCHEMA_NAME_PATTERN = Pattern
			.compile("^\\p{Upper}(\\p{Alnum}|[_])*\\p{Alnum}$");

	private static final Pattern PACKAGE_PREFIX_PATTERN = Pattern
			.compile("^\\p{Lower}\\w*(\\.\\p{Lower}\\w*)*$");

	/**
	 * Creates a new <code>Schema</code>.
	 * 
	 * @param name
	 *            Name of schema.
	 * @param packagePrefix
	 *            Package prefix of schema.
	 */
	public SchemaImpl(String name, String packagePrefix) {

		if (!SCHEMA_NAME_PATTERN.matcher(name).matches()) {
			this.throwInvalidSchemaNameException();
		}

		if (!PACKAGE_PREFIX_PATTERN.matcher(packagePrefix).matches()) {
			this.throwInvalidPackagePrefixNameException();
		}

		this.name = name;
		this.packagePrefix = packagePrefix;
		qualifiedName = packagePrefix + "." + name;
		m1ClassManager = M1ClassManager.instance(qualifiedName);

		// Needs to be created before any NamedElement can be created
		defaultPackage = PackageImpl.createDefaultPackage(this);

		// Creation of the BasicDomains
		createBooleanDomain();
		createDoubleDomain();
		createIntegerDomain();
		createLongDomain();
		createStringDomain();

		/*
		 * Needs to be created before any GraphElementClass element can be
		 * created.
		 */
		defaultGraphClass = GraphClassImpl.createDefaultGraphClass(this);

		// Creation of default GraphElementClasses
		defaultVertexClass = VertexClassImpl.createDefaultVertexClass(this);
		defaultEdgeClass = EdgeClassImpl.createDefaultEdgeClass(this);
		config = createDefaultConfig();
	}

	private void throwInvalidSchemaNameException() {
		throw new InvalidNameException(
				"Invalid schema name '"
						+ name
						+ "'.\n"
						+ "The name must not be empty.\n"
						+ "The name must start with a capital letter.\n"
						+ "Any following character must be alphanumeric and/or a '_' character.\n"
						+ "The name must end with an alphanumeric character.");
	}

	private void throwInvalidPackagePrefixNameException() {
		throw new InvalidNameException(
				"Invalid schema package prefix '"
						+ packagePrefix
						+ "'.\n"
						+ "The packagePrefix must not be empty.\n"
						+ "The package prefix must start with a small letter.\n"
						+ "The first character after each '.' must be a small letter.\n"
						+ "Following characters may be alphanumeric and/or '_' characters.\n"
						+ "The last character before a '.' and the end of the line must be an alphanumeric character.");
	}

	private CodeGeneratorConfiguration createDefaultConfig() {
		CodeGeneratorConfiguration out = new CodeGeneratorConfiguration();
		// TODO Add SAVEMEM.
		if (java.lang.Package.getPackage(packagePrefix + ". "
				+ IMPLSTDPACKAGENAME) == null) {
			out.setStandardSupport(false);
		}
		if (java.lang.Package.getPackage(packagePrefix + ". "
				+ IMPLTRANSPACKAGENAME) != null) {
			out.setTransactionSupport(true);
		}
		if (java.lang.Package.getPackage(packagePrefix + ". "
				+ IMPLDATABASEPACKAGENAME) != null) {
			out.setDatabaseSupport(true);
		}

		return out.withMethodsForSubclassesSupport();
		// TODO: Monte, check for the other values :-)
	}

	void addDomain(Domain dom) {
		assert !domains.containsKey(dom.getQualifiedName()) : "There already is a Domain with the qualified name: "
				+ dom.getQualifiedName() + " in the Schema!";
		domains.put(dom.getQualifiedName(), dom);
	}

	void addPackage(PackageImpl pkg) {
		assert !packages.containsKey(pkg.getQualifiedName()) : "There already is a Package with the qualified name '"
				+ pkg.getQualifiedName() + "' in the Schema!";
		packages.put(pkg.getQualifiedName(), pkg);
	}

	void addNamedElement(NamedElement namedElement) {
		assert !namedElements.containsKey(namedElement.getQualifiedName()) : "You are trying to add the NamedElement '"
				+ namedElement.getQualifiedName()
				+ "' to this Schema, but that does already exist!";

		namedElements.put(namedElement.getQualifiedName(), namedElement);

		// Check if any element's unique name needs adaptation after addition of
		// the new named element.
		Set<NamedElement> elementsWithSameSimpleName = namedElementsBySimpleName
				.get(namedElement.getSimpleName());
		// add element to map
		if ((elementsWithSameSimpleName != null)
				&& !elementsWithSameSimpleName.isEmpty()) {
			elementsWithSameSimpleName.add(namedElement);
		} else {
			elementsWithSameSimpleName = new TreeSet<NamedElement>();
			elementsWithSameSimpleName.add(namedElement);
			namedElementsBySimpleName.put(namedElement.getSimpleName(),
					elementsWithSameSimpleName);
		}
		// uniquify if needed
		if (elementsWithSameSimpleName.size() >= 2) {
			for (NamedElement other : elementsWithSameSimpleName) {
				((NamedElementImpl) other).changeUniqueName();
			}
		}
	}

	@Override
	public NamedElement getNamedElement(String qualifiedName) {
		return namedElements.get(qualifiedName);
	}

	@Override
	public boolean allowsLowercaseEnumConstants() {
		return allowLowercaseEnumConstants;
	}

	private Vector<JavaSourceFromString> createClasses(
			CodeGeneratorConfiguration config) {
		Vector<JavaSourceFromString> javaSources = new Vector<JavaSourceFromString>();

		/* create code for graph */
		GraphCodeGenerator graphCodeGenerator = new GraphCodeGenerator(
				graphClass, packagePrefix, GRAPH_IMPLEMENTATION_PACKAGE, name,
				config);
		javaSources.addAll(graphCodeGenerator.createJavaSources());

		for (VertexClass vertexClass : graphClass.getVertexClasses()) {
			VertexCodeGenerator codeGen = new VertexCodeGenerator(vertexClass,
					packagePrefix, GRAPH_IMPLEMENTATION_PACKAGE, config);
			javaSources.addAll(codeGen.createJavaSources());
		}

		for (EdgeClass edgeClass : graphClass.getEdgeClasses()) {
			CodeGenerator codeGen = new EdgeCodeGenerator(edgeClass,
					packagePrefix, GRAPH_IMPLEMENTATION_PACKAGE, config);
			javaSources.addAll(codeGen.createJavaSources());

			if (!edgeClass.isAbstract()) {
				codeGen = new ReversedEdgeCodeGenerator(edgeClass,
						packagePrefix, GRAPH_IMPLEMENTATION_PACKAGE, config);
				javaSources.addAll(codeGen.createJavaSources());
			}
		}

		// build records and enums
		for (Domain domain : getRecordDomains()) {
			// also generate an abstract class for Records
			CodeGenerator rcode = new RecordCodeGenerator(
					(RecordDomain) domain, packagePrefix,
					GRAPH_IMPLEMENTATION_PACKAGE, config);
			javaSources.addAll(rcode.createJavaSources());
		}
		for (Domain domain : getEnumDomains()) {
			CodeGenerator ecode = new EnumCodeGenerator((EnumDomain) domain,
					packagePrefix, GRAPH_IMPLEMENTATION_PACKAGE);
			javaSources.addAll(ecode.createJavaSources());
		}

		return javaSources;
	}

	@Override
	public Vector<JavaSourceFromString> commit(CodeGeneratorConfiguration config) {
		Vector<JavaSourceFromString> javaSources = new Vector<JavaSourceFromString>();

		// generate schema class
		CodeGenerator schemaCodeGenerator = new SchemaCodeGenerator(this,
				packagePrefix, GRAPH_IMPLEMENTATION_PACKAGE, config);
		javaSources.addAll(schemaCodeGenerator.createJavaSources());

		// generate factory
		CodeGenerator factoryCodeGenerator = new GraphFactoryGenerator(this,
				packagePrefix, GRAPH_IMPLEMENTATION_PACKAGE, config);
		javaSources.addAll(factoryCodeGenerator.createJavaSources());

		// generate graph classes

		if (graphClass.getQualifiedName().equals("Graph")) {
			throw new SchemaException(
					"The defined GraphClass must not be named Graph!");
		}

		javaSources.addAll(createClasses(config));
		return javaSources;
	}

	private void createFiles(CodeGeneratorConfiguration config,
			String pathPrefix, ProgressFunction progressFunction,
			long schemaElements, long currentCount, long interval)
			throws GraphIOException {

		/* create code for graph */
		GraphCodeGenerator graphCodeGenerator = new GraphCodeGenerator(
				graphClass, packagePrefix, GRAPH_IMPLEMENTATION_PACKAGE, name,
				config);
		graphCodeGenerator.createFiles(pathPrefix);

		for (VertexClass vertexClass : graphClass.getVertexClasses()) {
			VertexCodeGenerator codeGen = new VertexCodeGenerator(vertexClass,
					packagePrefix, GRAPH_IMPLEMENTATION_PACKAGE, config);
			codeGen.createFiles(pathPrefix);
			if (progressFunction != null) {
				schemaElements++;
				currentCount++;
				if (currentCount == interval) {
					progressFunction.progress(schemaElements);
					currentCount = 0;
				}
			}
		}

		for (EdgeClass edgeClass : graphClass.getEdgeClasses()) {
			CodeGenerator codeGen = new EdgeCodeGenerator(edgeClass,
					packagePrefix, GRAPH_IMPLEMENTATION_PACKAGE, config);
			codeGen.createFiles(pathPrefix);

			if (!edgeClass.isAbstract()) {
				codeGen = new ReversedEdgeCodeGenerator(edgeClass,
						packagePrefix, GRAPH_IMPLEMENTATION_PACKAGE, config);
				codeGen.createFiles(pathPrefix);
			}
			if (progressFunction != null) {
				schemaElements++;
				currentCount++;
				if (currentCount == interval) {
					progressFunction.progress(schemaElements);
					currentCount = 0;
				}
			}
		}

		// build records and enums
		for (Domain domain : getRecordDomains()) {
			// also generate an abstract class for Records
			CodeGenerator rcode = new RecordCodeGenerator(
					(RecordDomain) domain, packagePrefix,
					GRAPH_IMPLEMENTATION_PACKAGE, config);
			rcode.createFiles(pathPrefix);
			if (progressFunction != null) {
				schemaElements++;
				currentCount++;
				if (currentCount == interval) {
					progressFunction.progress(schemaElements);
					currentCount = 0;
				}
			}
		}
		for (Domain domain : getEnumDomains()) {
			CodeGenerator ecode = new EnumCodeGenerator((EnumDomain) domain,
					packagePrefix, GRAPH_IMPLEMENTATION_PACKAGE);
			ecode.createFiles(pathPrefix);
		}
		if (progressFunction != null) {
			schemaElements++;
			currentCount++;
			if (currentCount == interval) {
				progressFunction.progress(schemaElements);
				currentCount = 0;
			}
		}
	}

	@Override
	public void commit(String pathPrefix, CodeGeneratorConfiguration config)
			throws GraphIOException {
		this.commit(pathPrefix, config, null);
	}

	@Override
	public void commit(String pathPrefix, CodeGeneratorConfiguration config,
			ProgressFunction progressFunction) throws GraphIOException {
		// progress bar for schema generation
		// ProgressFunctionImpl pf;
		long schemaElements = 0, currentCount = 0, interval = 1;
		if (progressFunction != null) {
			int elements = getNumberOfElements();
			if (config.hasTransactionSupport()) {
				elements *= 2;
			}
			progressFunction.init(getNumberOfElements());
			interval = progressFunction.getUpdateInterval();
		}

		// ********************* build code **********************
		if (!pathPrefix.endsWith(File.separator)) {
			pathPrefix += File.separator;
		}

		// generate schema class
		CodeGenerator schemaCodeGenerator = new SchemaCodeGenerator(this,
				packagePrefix, GRAPH_IMPLEMENTATION_PACKAGE, config);
		schemaCodeGenerator.createFiles(pathPrefix);

		// generate factory
		CodeGenerator factoryCodeGenerator = new GraphFactoryGenerator(this,
				packagePrefix, GRAPH_IMPLEMENTATION_PACKAGE, config);
		factoryCodeGenerator.createFiles(pathPrefix);

		// generate graph class
		if (graphClass.getQualifiedName().equals("Graph")) {
			throw new SchemaException(
					"The defined GraphClass must not be named Graph!");
		}

		createFiles(config, pathPrefix, progressFunction, schemaElements,
				currentCount, interval);

		// finish progress bar
		if (progressFunction != null) {
			progressFunction.finished();
		}
	}

	@Override
	public int compareTo(Schema other) {
		return this.qualifiedName.compareTo(other.getQualifiedName());
	}

	@Override
	public void compile(CodeGeneratorConfiguration config) {
		compile(null, config);
	}

	@Override
	public void compile(String jgralabClassPath) {
		compile(jgralabClassPath, new CodeGeneratorConfiguration());
	}

	@Override
	public void compile(String jgralabClassPath,
			CodeGeneratorConfiguration config) {
		JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
		JavaFileManager jfm = null;

		// commit
		Vector<JavaSourceFromString> javaSources = commit(config);
		// compile
		try {
			jfm = compiler.getStandardFileManager(null, null, null);
		} catch (NullPointerException e) {
			System.out.println("Cannot compile schema " + qualifiedName + ".");
			System.out.println("Most probably you use a JRE instead of a JDK. "
					+ "The JRE does not provide a compiler.");
			e.printStackTrace();
			throw new SchemaException(e);
		}

		ClassFileManager manager = new ClassFileManager(jfm);

		Vector<String> options = new Vector<String>();
		if (jgralabClassPath != null) {
			options.add("-cp");
			options.add(jgralabClassPath);
		}

		compiler.getTask(null, manager, null, options, null, javaSources)
				.call();
	}

	@Override
	public Attribute createAttribute(String name, Domain dom,
			AttributedElementClass aec, String defaultValueAsString) {
		return new AttributeImpl(name, dom, aec, defaultValueAsString);
	}

	@Override
	public EnumDomain createEnumDomain(String qualifiedName) {
		return createEnumDomain(qualifiedName, new ArrayList<String>());
	}

	@Override
	public EnumDomain createEnumDomain(String qualifiedName,
			List<String> enumComponents) {
		String[] components = splitQualifiedName(qualifiedName);
		PackageImpl parent = (PackageImpl) createPackageWithParents(components[0]);
		String simpleName = components[1];
		return new EnumDomainImpl(simpleName, parent, enumComponents);
	}

	@Override
	public GraphClass createGraphClass(String simpleName) {
		if (graphClass != null) {
			throw new SchemaException(
					"Only one GraphClass (except DefaultGraphClass) is allowed in a Schema! '"
							+ graphClass.getQualifiedName()
							+ "' is already there.");
		}

		if (simpleName.equals(GraphClass.DEFAULTGRAPHCLASS_NAME)) {
			throw new InvalidNameException(
					"A GraphClass must not be named like the default GraphClass ("
							+ GraphClass.DEFAULTGRAPHCLASS_NAME + ")");
		}

		if (simpleName.contains(".")) {
			throw new InvalidNameException(
					"A GraphClass must always be in the default package!");
		}
		GraphClassImpl gc = new GraphClassImpl(simpleName, this);
		gc.addSuperClass(defaultGraphClass);
		return gc;
	}

	private BooleanDomain createBooleanDomain() {
		if (booleanDomain != null) {
			throw new SchemaException(
					"The BooleanDomain for this Schema was already created!");
		}
		booleanDomain = new BooleanDomainImpl(this);
		return booleanDomain;
	}

	private DoubleDomain createDoubleDomain() {
		if (doubleDomain != null) {
			throw new SchemaException(
					"The DoubleDomain for this Schema was already created!");
		}

		doubleDomain = new DoubleDomainImpl(this);
		return doubleDomain;
	}

	private IntegerDomain createIntegerDomain() {
		if (integerDomain != null) {
			throw new SchemaException(
					"The IntegerDomain for this Schema was already created!");
		}

		integerDomain = new IntegerDomainImpl(this);
		return integerDomain;
	}

	private LongDomain createLongDomain() {
		if (longDomain != null) {
			throw new SchemaException(
					"The LongDomain for this Schema was already created!");
		}

		longDomain = new LongDomainImpl(this);
		return longDomain;
	}

	private StringDomain createStringDomain() {
		if (stringDomain != null) {
			throw new SchemaException(
					"The StringDomain for this Schema was already created!");
		}

		stringDomain = new StringDomainImpl(this);
		return stringDomain;
	}

	@Override
	public ListDomain createListDomain(Domain baseDomain) {
		String qn = "List<" + baseDomain.getQualifiedName() + ">";
		if (domains.containsKey(qn)) {
			return (ListDomain) domains.get(qn);
		}
		return new ListDomainImpl(this, baseDomain);
	}

	@Override
	public MapDomain createMapDomain(Domain keyDomain, Domain valueDomain) {
		String qn = "Map<" + keyDomain.getQualifiedName() + ", "
				+ valueDomain.getQualifiedName() + ">";
		if (domains.containsKey(qn)) {
			return (MapDomain) domains.get(qn);
		}
		return new MapDomainImpl(this, keyDomain, valueDomain);
	}

	Package createPackage(String sn, Package parentPkg) {
		return new PackageImpl(sn, parentPkg, this);
	}

	/**
	 * Creates a {@link Package} with given qualified name, or returns an
	 * existing package with this qualified name.
	 * 
	 * @param qn
	 *            the qualified name of the package
	 * @return a new {@link Package} with the given qualified name, or an
	 *         existing package with this qualified name.
	 */
	Package createPackageWithParents(String qn) {
		if (packages.containsKey(qn)) {
			return packages.get(qn);
		}

		String[] components = splitQualifiedName(qn);
		String parent = components[0];
		String pkgSimpleName = components[1];

		assert !pkgSimpleName.contains(".") : "The package simple name '"
				+ pkgSimpleName + "' must not contain a dot!";

		Package currentParent = defaultPackage;
		String currentPkgQName = "";

		if (!packages.containsKey(parent)) {
			// the parent doesn't exist!

			for (String component : parent.split("\\.")) {
				if (currentParent != defaultPackage) {
					currentPkgQName = currentParent.getQualifiedName() + "."
							+ component;
				} else {
					currentPkgQName = component;
				}
				if (packages.containsKey(currentPkgQName)) {
					currentParent = packages.get(currentPkgQName);
					continue;
				}
				currentParent = createPackage(component, currentParent);
			}
		} else {
			currentParent = packages.get(parent);
		}

		// ok, parent existed or is created;
		assert currentParent.getQualifiedName().equals(parent) : "Something went wrong when creating a package with parents: "
				+ "parent should be \""
				+ parent
				+ "\" but created was \""
				+ currentParent.getQualifiedName() + "\".";
		assert (currentParent.getQualifiedName().isEmpty() ? currentParent == defaultPackage
				: true) : "The parent package of package '" + pkgSimpleName
				+ "' is empty, but not the default package.";
		return createPackage(pkgSimpleName, currentParent);
	}

	/**
	 * Given a qualified name like foo.bar.baz returns a string array with two
	 * components: the package prefix (foo.bar) and the simple name (baz).
	 * 
	 * @param qualifiedName
	 *            a qualified name
	 * @return a string array with two components: the package prefix and the
	 *         simple name
	 */
	public static String[] splitQualifiedName(String qualifiedName) {
		int lastIndex = qualifiedName.lastIndexOf('.');
		String[] components = new String[2];
		if (lastIndex == -1) {
			components[0] = "";
			components[1] = qualifiedName;
		} else {
			components[0] = qualifiedName.substring(0, lastIndex);
			if ((components[0].length() >= 1)
					&& (components[0].charAt(0) == '.')) {
				components[0] = components[0].substring(1);
			}
			components[1] = qualifiedName.substring(lastIndex + 1);
		}
		return components;
	}

	@Override
	public RecordDomain createRecordDomain(String qualifiedName) {
		return createRecordDomain(qualifiedName, null);
	}

	@Override
	public RecordDomain createRecordDomain(String qualifiedName,
			Collection<RecordComponent> recordComponents) {
		String[] components = splitQualifiedName(qualifiedName);
		PackageImpl parent = (PackageImpl) createPackageWithParents(components[0]);
		String simpleName = components[1];
		return new RecordDomainImpl(simpleName, parent, recordComponents);
	}

	@Override
	public SetDomain createSetDomain(Domain baseDomain) {
		String qn = "Set<" + baseDomain.getQualifiedName() + ">";
		if (domains.containsKey(qn)) {
			return (SetDomain) domains.get(qn);
		}
		return new SetDomainImpl(this, baseDomain);
	}

	@Override
	public boolean equals(Object other) {
		return (this == other)
				|| ((other instanceof Schema) && this.qualifiedName
						.equals(((Schema) other).getQualifiedName()));
	}

	@Override
	public int hashCode() {
		return qualifiedName.hashCode();
	}

	@Override
	public AttributedElementClass getAttributedElementClass(String qualifiedName) {
		if (graphClass == null) {
			return null;
		} else if (graphClass.getQualifiedName().equals(qualifiedName)) {
			return graphClass;
		} else {
			return graphClass.getGraphElementClass(qualifiedName);
		}
	}

	@Override
	public List<CompositeDomain> getCompositeDomainsInTopologicalOrder() {
		ArrayList<CompositeDomain> topologicalOrderList = new ArrayList<CompositeDomain>();
		CompositeDomain cd;
		HashSet<CompositeDomain> compositeDomainSet = new HashSet<CompositeDomain>();

		// store composite domains in compositeDomainSet
		for (Domain dl : domains.values()) {
			if (dl instanceof CompositeDomain) {
				compositeDomainSet.add((CompositeDomain) dl);
			}
		}

		// iteratively add domains from compositeDomainSet,
		// whose component domains already are in topologicalOrderList,
		// to topologicalOrderList
		// the added domains are removed from compositeDomainSet
		while (!compositeDomainSet.isEmpty()) {
			for (Iterator<CompositeDomain> cdit = compositeDomainSet.iterator(); cdit
					.hasNext();) {
				cd = cdit.next();
				if (topologicalOrderList.containsAll(cd
						.getAllComponentCompositeDomains())) {
					topologicalOrderList.add(cd);
					cdit.remove();
				}
			}
		}

		return topologicalOrderList;
	}

	private Method getCreateMethod(String className, String graphClassName,
			Class<?>[] signature, ImplementationType implementationType) {
		Class<? extends Graph> m1Class = null;
		AttributedElementClass aec = null;
		try {
			m1Class = getGraphClassImpl(implementationType);
			if (className.equals(graphClassName)) {
				return m1Class.getMethod("create", signature);
			} else {
				aec = graphClass.getVertexClass(className);
				if (aec == null) {
					aec = graphClass.getEdgeClass(className);
					if (aec == null) {
						throw new M1ClassAccessException("class " + className
								+ " does not exist in schema");
					}
				}
				return m1Class.getMethod("create"
						+ CodeGenerator.camelCase(aec.getUniqueName()),
						signature);
			}
		} catch (SecurityException e) {
			throw new M1ClassAccessException(
					"can't find create method in '" + m1Class.getName()
							+ "' for '" + aec.getUniqueName() + "'", e);
		} catch (NoSuchMethodException e) {
			throw new M1ClassAccessException(
					"can't find create method in '" + m1Class.getName()
							+ "' for '" + aec.getUniqueName() + "'", e);
		}
	}

	@Override
	public EdgeClass getDefaultEdgeClass() {
		return defaultEdgeClass;
	}

	@Override
	public GraphClass getDefaultGraphClass() {
		return defaultGraphClass;
	}

	@Override
	public Package getDefaultPackage() {
		return defaultPackage;
	}

	@Override
	public VertexClass getDefaultVertexClass() {
		return defaultVertexClass;
	}

	@Override
	public Domain getDomain(String domainName) {
		return domains.get(domainName);
	}

	@Override
	public Map<String, Domain> getDomains() {
		return domains;
	}

	@Override
	public List<EdgeClass> getEdgeClassesInTopologicalOrder() {
		ArrayList<EdgeClass> topologicalOrderList = new ArrayList<EdgeClass>();
		HashSet<EdgeClass> edgeClassSet = new HashSet<EdgeClass>();

		// store edge classes in edgeClassSet
		edgeClassSet.addAll(graphClass.getEdgeClasses());

		topologicalOrderList.add(defaultEdgeClass);
		// iteratively add classes from edgeClassSet,
		// whose superclasses already are in topologicalOrderList,
		// to topologicalOrderList
		// the added classes are removed from edgeClassSet
		while (!edgeClassSet.isEmpty()) {
			for (EdgeClass ec : edgeClassSet) {
				if (topologicalOrderList.containsAll(ec.getAllSuperClasses())) {
					topologicalOrderList.add(ec);
				}
			}
			edgeClassSet.removeAll(topologicalOrderList);
		}

		return topologicalOrderList;
	}

	@Override
	public Method getEdgeCreateMethod(String edgeClassName,
			ImplementationType implementationType) {
		// Edge class create method cannot be found directly by its signature
		// because the vertex parameters are subclassed to match the to- and
		// from-class. Those subclasses are unknown in this method. Therefore,
		// we look for a method with correct name and 3 parameters
		// (int, vertex, Vertex).
		AttributedElementClass aec = getAttributedElementClass(edgeClassName);
		if ((aec == null) || !(aec instanceof EdgeClass)) {
			throw new SchemaException(
					"There's no EdgeClass with qualified name " + edgeClassName
							+ "!");
		}
		EdgeClass ec = (EdgeClass) aec;
		String methodName = "create"
				+ CodeGenerator.camelCase(ec.getUniqueName());
		Class<?> m1Class = getGraphClassImpl(implementationType);
		for (Method m : m1Class.getMethods()) {
			if (m.getName().equals(methodName)
					&& (m.getParameterTypes().length == 3)) {
				return m;
			}
		}
		throw new M1ClassAccessException("can't find create method '"
				+ methodName + "' in '" + m1Class.getName() + "' for '"
				+ ec.getUniqueName() + "'");
	}

	@Override
	public List<EnumDomain> getEnumDomains() {
		ArrayList<EnumDomain> enumList = new ArrayList<EnumDomain>();

		for (Domain dl : domains.values()) {
			if (dl instanceof EnumDomain) {
				enumList.add((EnumDomain) dl);
			}
		}

		return enumList;
	}

	@Override
	public BooleanDomain getBooleanDomain() {
		return booleanDomain;
	}

	@Override
	public DoubleDomain getDoubleDomain() {
		return doubleDomain;
	}

	@Override
	public IntegerDomain getIntegerDomain() {
		return integerDomain;
	}

	@Override
	public LongDomain getLongDomain() {
		return longDomain;
	}

	@Override
	public StringDomain getStringDomain() {
		return stringDomain;
	}

	@Override
	public GraphClass getGraphClass() {
		return graphClass;
	}

	/**
	 * 
	 * @param implementationType
	 * @return
	 */
	@SuppressWarnings("unchecked")
	private Class<? extends Graph> getGraphClassImpl(
			ImplementationType implementationType) {
		String implClassName = packagePrefix + ".";
		// determine package
		switch (implementationType) {
		case STANDARD:
			implClassName += IMPLSTDPACKAGENAME;
			break;
		case TRANSACTION:
			implClassName += IMPLTRANSPACKAGENAME;
			break;
		case SAVEMEM:
			implClassName += IMPLSAVEMEMPACKAGENAME;
			break;
		case DATABASE:
			implClassName += IMPLDATABASEPACKAGENAME;
		default:
			throw new SchemaException("Implementation type "
					+ implementationType + " not supported yet.");
		}
		implClassName = implClassName + "." + graphClass.getSimpleName()
				+ "Impl";

		Class<? extends Graph> m1Class;
		try {
			m1Class = (Class<? extends Graph>) Class.forName(implClassName,
					true, M1ClassManager.instance(qualifiedName));
		} catch (ClassNotFoundException e) {
			throw new M1ClassAccessException(
					"can't load implementation class '" + implClassName + "'",
					e);
		}
		return m1Class;
	}

	@Override
	public Method getGraphCreateMethod(ImplementationType implementationType) {
		return getCreateMethod(graphClass.getSimpleName(), graphClass
				.getSimpleName(), GRAPHCLASS_CREATE_SIGNATURE,
				implementationType);
	}

	@Override
	public GraphFactory getGraphFactory() {
		return graphFactory;
	}

	@Override
	public String getName() {
		return name;
	}

	/**
	 * only used internally
	 * 
	 * @return number of graphelementclasses contained in graphclass
	 */
	private int getNumberOfElements() {
		return graphClass.getGraphElementClasses().size() + 1;
	}

	@Override
	public Package getPackage(String packageName) {
		return packages.get(packageName);
	}

	@Override
	public String getPackagePrefix() {
		return packagePrefix;
	}

	@Override
	public Map<String, Package> getPackages() {
		return packages;
	}

	@Override
	public String getQualifiedName() {
		return qualifiedName;
	}

	@Override
	public List<RecordDomain> getRecordDomains() {
		ArrayList<RecordDomain> recordList = new ArrayList<RecordDomain>();

		for (Domain dl : domains.values()) {
			if (dl instanceof RecordDomain) {
				recordList.add((RecordDomain) dl);
			}
		}

		return recordList;
	}

	@Override
	public List<VertexClass> getVertexClassesInTopologicalOrder() {
		ArrayList<VertexClass> topologicalOrderList = new ArrayList<VertexClass>();
		HashSet<VertexClass> vertexClassSet = new HashSet<VertexClass>();

		// store vertex classes in vertexClassSet
		vertexClassSet.addAll(graphClass.getVertexClasses());
		// first only the default vertex class is in the topo list
		topologicalOrderList.add(defaultVertexClass);

		// iteratively add classes from vertexClassSet,
		// whose superclasses already are in topologicalOrderList,
		// to topologicalOrderList
		// the added classes are removed from vertexClassSet
		while (!vertexClassSet.isEmpty()) {
			for (VertexClass vc : vertexClassSet) {
				if (topologicalOrderList.containsAll(vc.getAllSuperClasses())) {
					topologicalOrderList.add(vc);
				}
			}
			vertexClassSet.removeAll(topologicalOrderList);
		}
		return topologicalOrderList;
	}

	@Override
	public Method getVertexCreateMethod(String vertexClassName,
			ImplementationType implementationType) {
		return getCreateMethod(vertexClassName, graphClass.getSimpleName(),
				VERTEX_CLASS_CREATE_SIGNATURE, implementationType);
	}

	@Override
	public boolean isValidEnumConstant(String name) {
		if (name.isEmpty()) {
			return false;
		}
		if (!allowLowercaseEnumConstants && !name.equals(name.toUpperCase())) {
			return false;
		}
		if (RESERVED_JAVA_WORDS.contains(name)) {
			return false;
		}
		if (!Character.isJavaIdentifierStart(name.charAt(0))) {
			return false;
		}
		for (char c : name.toCharArray()) {
			if (!Character.isJavaIdentifierPart(c)) {
				return false;
			}
		}

		return true;
	}

	@Override
	public boolean knows(String qn) {
		return (namedElements.containsKey(qn) || getQualifiedName().equals(qn));
	}

	@Override
	public boolean isSimpleNameUnique(String sn) {
		return namedElementsBySimpleName.containsKey(sn);
	}

	@Override
	public void setAllowLowercaseEnumConstants(
			boolean allowLowercaseEnumConstants) {
		this.allowLowercaseEnumConstants = allowLowercaseEnumConstants;
	}

	@Override
	public void setGraphFactory(GraphFactory factory) {
		graphFactory = factory;
	}

	void setGraphClass(GraphClass gc) {
		if (graphClass != null) {
			throw new SchemaException("There already is a GraphClass named: "
					+ graphClass.getQualifiedName() + "in the Schema!");
		}
		graphClass = gc;
	}

	/**
	 * @return the textual representation of the schema with all graph classes,
	 *         their edge and vertex classes, all attributes and the whole
	 *         hierarchy of those classes
	 */
	@Override
	public String toString() {
		return "GraphClass of schema '" + qualifiedName + "':\n\n\n"
				+ graphClass.toString();
	}

	@Override
	public String getFileName() {
		return qualifiedName.replace('.', File.separatorChar);
	}

	@Override
	public String getPathName() {
		return packagePrefix.replace('.', File.separatorChar);
	}

	/**
	 * Set flag that transaction support should be used or not.
	 * 
	 * @param config
	 */
	public void setConfiguration(CodeGeneratorConfiguration config) {
		this.config = config;
	}

}
