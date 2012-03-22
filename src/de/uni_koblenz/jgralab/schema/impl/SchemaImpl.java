/*
 * JGraLab - The Java Graph Laboratory
 *
 * Copyright (C) 2006-2012 Institute for Software Technology
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

package de.uni_koblenz.jgralab.schema.impl;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.Vector;
import java.util.regex.Pattern;

import javax.tools.JavaCompiler;
import javax.tools.JavaFileObject;
import javax.tools.StandardJavaFileManager;
import javax.tools.ToolProvider;

import de.uni_koblenz.jgralab.Graph;
import de.uni_koblenz.jgralab.GraphFactory;
import de.uni_koblenz.jgralab.GraphIO;
import de.uni_koblenz.jgralab.GraphIOException;
import de.uni_koblenz.jgralab.ImplementationType;
import de.uni_koblenz.jgralab.ProgressFunction;
import de.uni_koblenz.jgralab.Vertex;
import de.uni_koblenz.jgralab.codegenerator.CodeGenerator;
import de.uni_koblenz.jgralab.codegenerator.CodeGeneratorConfiguration;
import de.uni_koblenz.jgralab.codegenerator.EdgeCodeGenerator;
import de.uni_koblenz.jgralab.codegenerator.EnumCodeGenerator;
import de.uni_koblenz.jgralab.codegenerator.GraphCodeGenerator;
import de.uni_koblenz.jgralab.codegenerator.GraphFactoryGenerator;
import de.uni_koblenz.jgralab.codegenerator.RecordCodeGenerator;
import de.uni_koblenz.jgralab.codegenerator.ReversedEdgeCodeGenerator;
import de.uni_koblenz.jgralab.codegenerator.SchemaCodeGenerator;
import de.uni_koblenz.jgralab.codegenerator.VertexCodeGenerator;
import de.uni_koblenz.jgralab.impl.ConsoleProgressFunction;
import de.uni_koblenz.jgralab.impl.generic.GenericGraphFactoryImpl;
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
import de.uni_koblenz.jgralab.schema.RecordDomain.RecordComponent;
import de.uni_koblenz.jgralab.schema.Schema;
import de.uni_koblenz.jgralab.schema.SetDomain;
import de.uni_koblenz.jgralab.schema.StringDomain;
import de.uni_koblenz.jgralab.schema.VertexClass;
import de.uni_koblenz.jgralab.schema.exception.SchemaClassAccessException;
import de.uni_koblenz.jgralab.schema.exception.SchemaException;
import de.uni_koblenz.jgralab.schema.impl.compilation.ClassFileManager;
import de.uni_koblenz.jgralab.schema.impl.compilation.InMemoryJavaSourceFile;
import de.uni_koblenz.jgralab.schema.impl.compilation.ManagableArtifact;
import de.uni_koblenz.jgralab.schema.impl.compilation.SchemaClassManager;

/**
 * @author ist@uni-koblenz.de
 */
public class SchemaImpl implements Schema, ManagableArtifact {
	// we need a hard reference here, cause the SchemaClassManager uses only
	// weak references. This way, when the schema gets collected, the class
	// manager is free for collection, too.
	private SchemaClassManager schemaClassManager = null;

	public SchemaClassManager getSchemaClassManager() {
		return schemaClassManager;
	}

	static final Class<?>[] GRAPHCLASS_CREATE_SIGNATURE = {
			ImplementationType.class, String.class, int.class, int.class };

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

	static final Class<?>[] VERTEX_CLASS_CREATE_SIGNATURE = { int.class };

	/**
	 * Toggles if the schema allows lowercase enumeration constants
	 */
	private boolean allowLowercaseEnumConstants = true;

	private PackageImpl defaultPackage;

	/**
	 * Maps from qualified name to the {@link Domain}.
	 */
	Map<String, Domain> domains = new HashMap<String, Domain>();

	private DirectedAcyclicGraph<Domain> domainsDag = new DirectedAcyclicGraph<Domain>();

	private boolean finished = false;

	/**
	 * Holds a reference to the {@link GraphClass} of this schema (not the
	 * default graph class {@link GraphClass})
	 */
	private GraphClassImpl graphClass;

	/**
	 * The name of this schema without the package prefix.
	 */
	private String name;

	/**
	 * The package prefix of this schema.
	 */
	private String packagePrefix;

	/**
	 * Maps from qualified name to the {@link Package} with that qualified name.
	 */
	Map<String, PackageImpl> packages = new TreeMap<String, PackageImpl>();

	/**
	 * The qualified name of this schema, that is {@link #packagePrefix} DOT
	 * {@link #name}
	 */
	private String qualifiedName;

	/**
	 * A set of all qualified names known to this schema.
	 */
	Map<String, NamedElement> namedElements = new TreeMap<String, NamedElement>();

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
			throw new SchemaException(
					"Invalid schema name '"
							+ name
							+ "'.\n"
							+ "The name must not be empty.\n"
							+ "The name must start with a capital letter.\n"
							+ "Any following character must be alphanumeric and/or a '_' character.\n"
							+ "The name must end with an alphanumeric character.");
		}

		if (!PACKAGE_PREFIX_PATTERN.matcher(packagePrefix).matches()) {
			throw new SchemaException(
					"Invalid schema package prefix '"
							+ packagePrefix
							+ "'.\n"
							+ "The packagePrefix must not be empty.\n"
							+ "The package prefix must start with a small letter.\n"
							+ "The first character after each '.' must be a small letter.\n"
							+ "Following characters may be alphanumeric and/or '_' characters.\n"
							+ "The last character before a '.' and the end of the line must be an alphanumeric character.");
		}

		this.name = name;
		this.packagePrefix = packagePrefix;
		qualifiedName = packagePrefix + "." + name;
		schemaClassManager = SchemaClassManager.instance(qualifiedName);

		// Needs to be created before any NamedElement can be created
		defaultPackage = PackageImpl.createDefaultPackage(this);

		// Creation of the BasicDomains
		createBooleanDomain();
		createDoubleDomain();
		createIntegerDomain();
		createLongDomain();
		createStringDomain();
	}

	void addDomain(Domain dom) {
		if (domains.containsKey(dom.getQualifiedName())) {
			throw new SchemaException("Duplicate Domain '"
					+ dom.getQualifiedName() + "'");
		}
		domains.put(dom.getQualifiedName(), dom);
		domainsDag.createNode(dom);
	}

	void addPackage(PackageImpl pkg) {
		if (packages.containsKey(pkg.getQualifiedName())) {
			throw new SchemaException("Duplicate Package '"
					+ pkg.getQualifiedName() + "'");
		}
		packages.put(pkg.getQualifiedName(), pkg);
	}

	void addNamedElement(NamedElement namedElement) {
		if (namedElements.containsKey(namedElement.getQualifiedName())) {
			throw new SchemaException("Duplicate NamedElement '"
					+ namedElement.getQualifiedName() + "'");
		}
		namedElements.put(namedElement.getQualifiedName(), namedElement);

		if (!(namedElement instanceof AttributedElementClass)) {
			return;
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

	private Vector<InMemoryJavaSourceFile> createClasses(
			CodeGeneratorConfiguration config) {
		Vector<InMemoryJavaSourceFile> javaSources = new Vector<InMemoryJavaSourceFile>();

		/* create code for graph */
		GraphCodeGenerator graphCodeGenerator = new GraphCodeGenerator(
				graphClass, packagePrefix, name, config);
		javaSources.addAll(graphCodeGenerator.createJavaSources());

		for (VertexClass vertexClass : graphClass.getVertexClasses()) {
			VertexCodeGenerator codeGen = new VertexCodeGenerator(vertexClass,
					packagePrefix, config);
			javaSources.addAll(codeGen.createJavaSources());
		}

		for (EdgeClass edgeClass : graphClass.getEdgeClasses()) {
			CodeGenerator codeGen = new EdgeCodeGenerator(edgeClass,
					packagePrefix, config);
			javaSources.addAll(codeGen.createJavaSources());

			if (!edgeClass.isAbstract()) {
				codeGen = new ReversedEdgeCodeGenerator(edgeClass,
						packagePrefix, config);
				javaSources.addAll(codeGen.createJavaSources());
			}
		}

		// build records and enums
		for (Domain domain : getRecordDomains()) {
			// also generate an abstract class for Records
			CodeGenerator rcode = new RecordCodeGenerator(
					(RecordDomain) domain, packagePrefix, config);
			javaSources.addAll(rcode.createJavaSources());
		}
		for (Domain domain : getEnumDomains()) {
			CodeGenerator ecode = new EnumCodeGenerator((EnumDomain) domain,
					packagePrefix);
			javaSources.addAll(ecode.createJavaSources());
		}

		return javaSources;
	}

	@Override
	public void createJAR(CodeGeneratorConfiguration config, String jarFileName)
			throws IOException, GraphIOException {
		assertFinished();
		File tmpFile = File.createTempFile("jar-creation", "tmp");
		tmpFile.deleteOnExit();
		File tmpDir = new File(tmpFile.getParent());
		File schemaDir = new File(tmpDir + File.separator + getName());
		if (!schemaDir.mkdir()) {
			System.err.println("Couldn't create " + schemaDir);
			return;
		}
		System.out.println("Committing schema classes to " + schemaDir);
		commit(schemaDir.getAbsolutePath(), config,
				new ConsoleProgressFunction("Committing"));

		compileClasses(schemaDir);

		// TODO: That should be doable without resorting to the cmd line, but
		// how? JarFile seems to provide only read access...
		Process proc = Runtime.getRuntime().exec(
				"jar cf " + jarFileName + " -C " + schemaDir.getAbsolutePath()
						+ " .");

		try {
			proc.waitFor();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		deleteRecursively(schemaDir);
	}

	private void deleteRecursively(File file) {
		if (file.isDirectory()) {
			for (File f : file.listFiles()) {
				deleteRecursively(f);
			}
			file.delete();
		} else {
			file.delete();
		}
	}

	private void compileClasses(File schemaDir) throws IOException {
		JavaCompiler c = ToolProvider.getSystemJavaCompiler();
		StandardJavaFileManager fileManager = c.getStandardFileManager(null,
				null, null);
		Iterable<? extends JavaFileObject> compilationUnits = fileManager
				.getJavaFileObjectsFromFiles(getJavaFiles(schemaDir));
		c.getTask(null, fileManager, null, null, null, compilationUnits).call();
		fileManager.close();
	}

	private List<File> getJavaFiles(File schemaDir) {
		LinkedList<File> sources = new LinkedList<File>();
		for (File f : schemaDir.listFiles()) {
			if (f.isDirectory()) {
				sources.addAll(getJavaFiles(f));
			} else if (f.getName().endsWith(".java")) {
				sources.add(f);
			}
		}
		return sources;
	}

	@Override
	public Vector<InMemoryJavaSourceFile> commit(
			CodeGeneratorConfiguration config) {
		assertFinished();

		Vector<InMemoryJavaSourceFile> javaSources = new Vector<InMemoryJavaSourceFile>();

		// generate schema class
		CodeGenerator schemaCodeGenerator = new SchemaCodeGenerator(this,
				packagePrefix, config);
		javaSources.addAll(schemaCodeGenerator.createJavaSources());

		// generate factory
		CodeGenerator factoryCodeGenerator = new GraphFactoryGenerator(this,
				packagePrefix, config);
		javaSources.addAll(factoryCodeGenerator.createJavaSources());

		javaSources.addAll(createClasses(config));
		return javaSources;
	}

	private void createFiles(CodeGeneratorConfiguration config,
			String pathPrefix, ProgressFunction progressFunction,
			long schemaElements, long currentCount, long interval)
			throws GraphIOException {

		/* create code for graph */
		GraphCodeGenerator graphCodeGenerator = new GraphCodeGenerator(
				graphClass, packagePrefix, name, config);
		graphCodeGenerator.createFiles(pathPrefix);

		for (VertexClass vertexClass : graphClass.getVertexClasses()) {
			VertexCodeGenerator codeGen = new VertexCodeGenerator(vertexClass,
					packagePrefix, config);
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
					packagePrefix, config);
			codeGen.createFiles(pathPrefix);

			if (!edgeClass.isAbstract()) {
				codeGen = new ReversedEdgeCodeGenerator(edgeClass,
						packagePrefix, config);
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
					(RecordDomain) domain, packagePrefix, config);
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
					packagePrefix);
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
		assertFinished();
		this.commit(pathPrefix, config, null);
	}

	@Override
	public void commit(String pathPrefix, CodeGeneratorConfiguration config,
			ProgressFunction progressFunction) throws GraphIOException {
		assertFinished();
		// progress bar for schema generation
		// ProgressFunctionImpl pf;
		long schemaElements = 0, currentCount = 0, interval = 1;
		if (progressFunction != null) {
			int elements = getNumberOfElements();
			if (config.hasTransactionSupport()) {
				elements *= 2;
			}
			progressFunction.init(elements);
			interval = progressFunction.getUpdateInterval();
		}

		// ********************* build code **********************
		if (!pathPrefix.endsWith(File.separator)) {
			pathPrefix += File.separator;
		}

		// generate schema class
		CodeGenerator schemaCodeGenerator = new SchemaCodeGenerator(this,
				packagePrefix, config);
		schemaCodeGenerator.createFiles(pathPrefix);

		// generate factory
		CodeGenerator factoryCodeGenerator = new GraphFactoryGenerator(this,
				packagePrefix, config);
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
		return qualifiedName.compareTo(other.getQualifiedName());
	}

	@Override
	public void compile(CodeGeneratorConfiguration config) {
		assertFinished();
		JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
		if (compiler == null) {
			throw new SchemaException("Cannot compile schema " + qualifiedName
					+ ". Most probably you use a JRE instead of a JDK. "
					+ "The JRE does not provide a compiler.");

		}
		StandardJavaFileManager jfm = compiler.getStandardFileManager(null,
				null, null);
		ClassFileManager manager = new ClassFileManager(this, jfm);
		Vector<InMemoryJavaSourceFile> javaSources = commit(config);
		compiler.getTask(null, manager, null, null, null, javaSources).call();
	}

	@Override
	public EnumDomain createEnumDomain(String qualifiedName) {
		return createEnumDomain(qualifiedName, new ArrayList<String>());
	}

	@Override
	public EnumDomain createEnumDomain(String qualifiedName,
			List<String> enumComponents) {
		assertNotFinished();
		String[] components = splitQualifiedName(qualifiedName);
		PackageImpl parent = createPackageWithParents(components[0]);
		String simpleName = components[1];
		EnumDomain ed = new EnumDomainImpl(simpleName, parent, enumComponents);
		return ed;
	}

	@Override
	public GraphClass createGraphClass(String simpleName) {
		assertNotFinished();
		if (graphClass != null) {
			throw new SchemaException(
					"Only one GraphClass (except DefaultGraphClass) is allowed in a Schema! '"
							+ graphClass.getQualifiedName()
							+ "' is already there.");
		}

		if (simpleName.contains(".")) {
			throw new SchemaException(
					"A GraphClass must always be in the default package!");
		}
		GraphClassImpl gc = new GraphClassImpl(simpleName, this);
		gc.initializeDefaultVertexClass();
		gc.initializeDefaultEdgeClass();
		return gc;
	}

	private BooleanDomain createBooleanDomain() {
		assertNotFinished();
		if (booleanDomain == null) {
			booleanDomain = new BooleanDomainImpl(this);
		}
		return booleanDomain;
	}

	private DoubleDomain createDoubleDomain() {
		assertNotFinished();
		if (doubleDomain == null) {
			doubleDomain = new DoubleDomainImpl(this);
		}
		return doubleDomain;
	}

	private IntegerDomain createIntegerDomain() {
		assertNotFinished();
		if (integerDomain == null) {
			integerDomain = new IntegerDomainImpl(this);
		}
		return integerDomain;
	}

	private LongDomain createLongDomain() {
		assertNotFinished();
		if (longDomain == null) {
			longDomain = new LongDomainImpl(this);
		}
		return longDomain;
	}

	private StringDomain createStringDomain() {
		assertNotFinished();
		if (stringDomain == null) {
			stringDomain = new StringDomainImpl(this);
		}
		return stringDomain;
	}

	@Override
	public ListDomain createListDomain(Domain baseDomain) {
		assertNotFinished();
		String qn = "List<" + baseDomain.getQualifiedName() + ">";
		if (domains.containsKey(qn)) {
			return (ListDomain) domains.get(qn);
		}
		return new ListDomainImpl(this, baseDomain);
	}

	@Override
	public MapDomain createMapDomain(Domain keyDomain, Domain valueDomain) {
		assertNotFinished();
		String qn = "Map<" + keyDomain.getQualifiedName() + ", "
				+ valueDomain.getQualifiedName() + ">";
		if (domains.containsKey(qn)) {
			return (MapDomain) domains.get(qn);
		}
		return new MapDomainImpl(this, keyDomain, valueDomain);
	}

	PackageImpl createPackage(String sn, PackageImpl parentPkg) {
		assertNotFinished();
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
	PackageImpl createPackageWithParents(String qn) {
		assertNotFinished();
		if (packages.containsKey(qn)) {
			return packages.get(qn);
		}

		String[] components = splitQualifiedName(qn);
		String parent = components[0];
		String pkgSimpleName = components[1];

		assert !pkgSimpleName.contains(".") : "The package simple name '"
				+ pkgSimpleName + "' must not contain a dot!";

		PackageImpl currentParent = defaultPackage;
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
		assertNotFinished();
		String[] components = splitQualifiedName(qualifiedName);
		PackageImpl parent = createPackageWithParents(components[0]);
		String simpleName = components[1];
		RecordDomain rd = new RecordDomainImpl(simpleName, parent,
				recordComponents);
		return rd;
	}

	@Override
	public SetDomain createSetDomain(Domain baseDomain) {
		assertNotFinished();
		String qn = "Set<" + baseDomain.getQualifiedName() + ">";
		if (domains.containsKey(qn)) {
			return (SetDomain) domains.get(qn);
		}
		return new SetDomainImpl(this, baseDomain);
	}

	@Override
	public boolean equals(Object other) {
		if ((other == null) || !(other instanceof Schema)) {
			return false;
		}
		return qualifiedName.equals(((SchemaImpl) other).qualifiedName);
	}

	@Override
	public int hashCode() {
		return qualifiedName.hashCode();
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T extends AttributedElementClass<?, ?>> T getAttributedElementClass(
			String qualifiedName) {
		if (graphClass == null) {
			return null;
		} else if (graphClass.getQualifiedName().equals(qualifiedName)) {
			return (T) graphClass;
		} else {
			return (T) graphClass.getGraphElementClass(qualifiedName);
		}
	}

	@Override
	public List<CompositeDomain> getCompositeDomains() {
		ArrayList<CompositeDomain> topologicalOrderList = new ArrayList<CompositeDomain>();

		for (Domain dom : domainsDag.getNodesInTopologicalOrder()) {
			if (dom instanceof CompositeDomain) {
				topologicalOrderList.add((CompositeDomain) dom);
			}
		}
		return topologicalOrderList;
	}

	private Method getCreateMethod(String className, String graphClassName,
			Class<?>[] signature, ImplementationType implementationType) {
		Class<? extends Graph> schemaClass = null;
		AttributedElementClass<?, ?> aec = null;
		try {
			schemaClass = getGraphClassImpl(implementationType);
			if (className.equals(graphClassName)) {
				if (implementationType != ImplementationType.GENERIC) {
					// Graph create method is in the SchemaImpl specialization
					return getClass().getMethod("create" + graphClassName,
							signature);
				} else {
					// generic case
					return schemaClass.getMethod("createGraph", signature);
				}
			} else {
				// Element create methods are in the GraphImpl specialization
				aec = graphClass.getVertexClass(className);
				if (aec == null) {
					aec = graphClass.getEdgeClass(className);
					if (aec == null) {
						throw new SchemaClassAccessException("class "
								+ className + " does not exist in schema");
					}
				}
				if (implementationType != ImplementationType.GENERIC) {
					return schemaClass.getMethod(
							"create"
									+ CodeGenerator.camelCase(aec
											.getUniqueName()), signature);
				} else {
					if (signature[0].equals(VertexClass.class)) {
						return schemaClass.getMethod("createVertex", signature);
					} else {
						return schemaClass.getMethod("createEdge", signature);
					}
				}

			}
		} catch (SecurityException e) {
			throw new SchemaClassAccessException(
					"can't find create method in '" + schemaClass.getName()
							+ "' for '" + aec.getUniqueName() + "'", e);
		} catch (NoSuchMethodException e) {
			throw new SchemaClassAccessException(
					"can't find create method in '" + schemaClass.getName()
							+ "' for '" + aec.getUniqueName() + "'", e);
		}
	}

	@Override
	public Package getDefaultPackage() {
		return defaultPackage;
	}

	@Override
	public Domain getDomain(String domainName) {
		return domains.get(domainName);
	}

	@Override
	public Map<String, Domain> getDomains() {
		return domains;
	}

	void addDomainDependency(Domain composite, Domain base) {
		domainsDag.createEdge(base, composite);
	}

	@Override
	public Method getEdgeCreateMethod(String edgeClassName,
			ImplementationType implementationType) {
		// Edge class create method cannot be found directly by its signature
		// because the vertex parameters are subclassed to match the to- and
		// from-class. Those subclasses are unknown in this method. Therefore,
		// we look for a method with correct name and 3 parameters
		// (int, vertex, Vertex).
		AttributedElementClass<?, ?> aec = getAttributedElementClass(edgeClassName);
		if ((aec == null) || !(aec instanceof EdgeClass)) {
			throw new SchemaException(
					"There's no EdgeClass with qualified name " + edgeClassName
							+ "!");
		}
		EdgeClass ec = (EdgeClass) aec;
		String methodName = "create"
				+ CodeGenerator.camelCase(ec.getUniqueName());
		Class<?> schemaClass = getGraphClassImpl(implementationType);
		if (implementationType != ImplementationType.GENERIC) {
			for (Method m : schemaClass.getMethods()) {
				if (m.getName().equals(methodName)
						&& (m.getParameterTypes().length == 3)) {
					return m;
				}
			}
		} else {
			try {
				return schemaClass.getMethod("createEdge",
						new Class[] { EdgeClass.class, int.class, Vertex.class,
								Vertex.class });
			} catch (NoSuchMethodException e) {
				e.printStackTrace();
			} catch (SecurityException e) {
				e.printStackTrace();
			}
		}
		throw new SchemaClassAccessException("can't find create method '"
				+ methodName + "' in '" + schemaClass.getName() + "' for '"
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
		case DATABASE:
			implClassName += IMPLDATABASEPACKAGENAME;
		case GENERIC:
			implClassName = "de.uni_koblenz.jgralab.impl.generic";
			break;
		default:
			throw new SchemaException("Implementation type "
					+ implementationType + " not supported yet.");
		}

		Class<? extends Graph> schemaClass;
		if (implementationType != ImplementationType.GENERIC) {
			implClassName = implClassName + "." + graphClass.getSimpleName()
					+ "Impl";

			try {
				schemaClass = (Class<? extends Graph>) Class.forName(
						implClassName, true,
						SchemaClassManager.instance(qualifiedName));
			} catch (ClassNotFoundException e) {
				throw new SchemaClassAccessException(
						"can't load implementation class '" + implClassName
								+ "'", e);
			}
			return schemaClass;
		} else {
			implClassName += "." + "GenericGraphImpl";
			try {
				return (Class<? extends Graph>) Class.forName(implClassName);
			} catch (ClassNotFoundException e) {
				throw new SchemaClassAccessException(
						"can't load implementation class '" + implClassName
								+ "'", e);
			}
		}
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
	public Method getVertexCreateMethod(String vertexClassName,
			ImplementationType implementationType) {
		if (implementationType != ImplementationType.GENERIC) {
			return getCreateMethod(vertexClassName, graphClass.getSimpleName(),
					VERTEX_CLASS_CREATE_SIGNATURE, implementationType);
		} else {
			return getCreateMethod(vertexClassName, graphClass.getSimpleName(),
					new Class[] { VertexClass.class, int.class },
					implementationType);
		}
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
	public void setAllowLowercaseEnumConstants(
			boolean allowLowercaseEnumConstants) {
		this.allowLowercaseEnumConstants = allowLowercaseEnumConstants;
	}

	void setGraphClass(GraphClassImpl gc) {
		if (graphClass != null) {
			throw new SchemaException("A GraphClass named '"
					+ graphClass.getQualifiedName()
					+ "' already exists in this Schema!");
		}
		graphClass = gc;
	}

	@Override
	public String toString() {
		return getQualifiedName();
	}

	@Override
	public String toTGString() {
		String schemaDefinition = null;
		ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
		DataOutputStream out = new DataOutputStream(byteOut);
		try {
			GraphIO.saveSchemaToStream(this, out);
			out.close();
			byteOut.close();
			schemaDefinition = new String(byteOut.toByteArray());
		} catch (GraphIOException e) {
			throw new SchemaException(e);
		} catch (IOException e) {
			throw new SchemaException(e);
		}
		return schemaDefinition;
	}

	@Override
	public String getFileName() {
		return qualifiedName.replace('.', File.separatorChar);
	}

	@Override
	public String getPathName() {
		return packagePrefix.replace('.', File.separatorChar);
	}

	@Override
	public GraphFactory createDefaultGraphFactory(
			ImplementationType implementationType) {
		assertFinished();
		if (implementationType != ImplementationType.GENERIC) {
			throw new SchemaException(
					"Base implementation can't create a GraphFactory for implementation type "
							+ implementationType
							+ ". Only GENERIC is supported.");
		}
		return new GenericGraphFactoryImpl(this);
	}

	@Override
	public Graph createGraph(ImplementationType implementationType) {
		return createGraph(implementationType, null, 100, 100);
	}

	@Override
	public Graph createGraph(ImplementationType implementationType, String id,
			int vMax, int eMax) {
		assertFinished();
		GraphFactory factory = createDefaultGraphFactory(implementationType);
		return factory.createGraph(getGraphClass(), id, vMax, eMax);
	}

	protected void assertFinished() {
		if (!finished) {
			throw new SchemaException("Schema must be finished.");
		}
	}

	protected void assertNotFinished() {
		if (finished) {
			throw new SchemaException(
					"No changes allowed in a finished Schema.");
		}
	}

	/**
	 * @return whether the schema is finished
	 */
	@Override
	public boolean isFinished() {
		return finished;
	}

	/**
	 * Signals that the schema is finished. No more changes are allowed. To open
	 * the change mode call reopen
	 */
	@Override
	public boolean finish() {
		if (finished) {
			return false;
		}
		if (graphClass == null) {
			throw new SchemaException(
					"Can't finish a schema without a GraphClass. Create a GraphClass first!");
		}
		domainsDag.finish();
		graphClass.finish();
		finished = true;
		return true;
	}

	/**
	 * Reopens the schema to allow changes. To finish the schema again, call
	 * finish
	 */
	@Override
	public boolean reopen() {
		if (!finished) {
			return false;
		}

		domainsDag.reopen();
		graphClass.reopen();
		finished = false;

		return true;
	}

	@Override
	public void save(String filename) throws GraphIOException {
		GraphIO.saveSchemaToFile(this, filename);
	}

	@Override
	public void save(DataOutputStream out) throws GraphIOException {
		GraphIO.saveSchemaToStream(this, out);
	}

	/**
	 * This field and the method getNextClassId() is used to allow a unique
	 * mapping of GraphElementClasses to integer values, which is necessary for
	 * the GReQL code generator
	 */
	int nextGraphElementClassId = 0;

	protected int getNextGraphElementClassId() {
		return nextGraphElementClassId++;
	}

	protected int nextIncidenceClassId = 0;

	public int getNextIncidenceClassId() {
		return nextIncidenceClassId++;
	}

	@Override
	public String getManagedName() {
		return qualifiedName;
	}

}
