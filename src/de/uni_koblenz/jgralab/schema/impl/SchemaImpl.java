/*
 * JGraLab - The Java graph laboratory
 * (c) 2006-2008 Institute for Software Technology
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
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.Vector;
import java.util.Map.Entry;

import javax.tools.FileObject;
import javax.tools.ForwardingJavaFileManager;
import javax.tools.JavaCompiler;
import javax.tools.JavaFileManager;
import javax.tools.JavaFileObject;
import javax.tools.ToolProvider;
import javax.tools.JavaFileObject.Kind;

import de.uni_koblenz.jgralab.Attribute;
import de.uni_koblenz.jgralab.AttributedElement;
import de.uni_koblenz.jgralab.Graph;
import de.uni_koblenz.jgralab.GraphFactory;
import de.uni_koblenz.jgralab.GraphIOException;
import de.uni_koblenz.jgralab.M1ClassManager;
import de.uni_koblenz.jgralab.ProgressFunction;
import de.uni_koblenz.jgralab.codegenerator.AttributedElementCodeGenerator;
import de.uni_koblenz.jgralab.codegenerator.ClassFileAbstraction;
import de.uni_koblenz.jgralab.codegenerator.CodeGenerator;
import de.uni_koblenz.jgralab.codegenerator.EdgeCodeGenerator;
import de.uni_koblenz.jgralab.codegenerator.EnumCodeGenerator;
import de.uni_koblenz.jgralab.codegenerator.GraphCodeGenerator;
import de.uni_koblenz.jgralab.codegenerator.GraphFactoryGenerator;
import de.uni_koblenz.jgralab.codegenerator.JavaSourceFromString;
import de.uni_koblenz.jgralab.codegenerator.RecordCodeGenerator;
import de.uni_koblenz.jgralab.codegenerator.ReversedEdgeCodeGenerator;
import de.uni_koblenz.jgralab.codegenerator.SchemaCodeGenerator;
import de.uni_koblenz.jgralab.codegenerator.VertexCodeGenerator;
import de.uni_koblenz.jgralab.impl.AttributeImpl;
import de.uni_koblenz.jgralab.schema.AggregationClass;
import de.uni_koblenz.jgralab.schema.AttributedElementClass;
import de.uni_koblenz.jgralab.schema.CompositeDomain;
import de.uni_koblenz.jgralab.schema.CompositionClass;
import de.uni_koblenz.jgralab.schema.Domain;
import de.uni_koblenz.jgralab.schema.EdgeClass;
import de.uni_koblenz.jgralab.schema.EnumDomain;
import de.uni_koblenz.jgralab.schema.GraphClass;
import de.uni_koblenz.jgralab.schema.GraphElementClass;
import de.uni_koblenz.jgralab.schema.ListDomain;
import de.uni_koblenz.jgralab.schema.NamedElement;
import de.uni_koblenz.jgralab.schema.Package;
import de.uni_koblenz.jgralab.schema.QualifiedName;
import de.uni_koblenz.jgralab.schema.RecordDomain;
import de.uni_koblenz.jgralab.schema.Schema;
import de.uni_koblenz.jgralab.schema.SchemaException;
import de.uni_koblenz.jgralab.schema.SetDomain;
import de.uni_koblenz.jgralab.schema.VertexClass;

public class SchemaImpl implements Schema {

	private static final String GRAPHIMPLEMENTATIONPACKAGE = "array";

	/**
	 * This is the name of the package into which the implementation classes for
	 * this schema are generated. The impl package is child of the package for
	 * the Schema.
	 */
	public static final String IMPLPACKAGENAME = "impl";

	/**
	 * Toggles if the schema allows lowercase enumeration constants
	 */
	private boolean allowLowercaseEnumConstants = true;

	private QualifiedName qName;

	private Map<QualifiedName, Package> packages;

	private Map<QualifiedName, Domain> domains;

	private Map<QualifiedName, GraphClass> graphClasses;

	private Map<String, NamedElement> namedElements;

	private Set<String> reservedUniqueNames;

	private Package defaultPackage;

	private EdgeClass defaultEdgeClass;

	private VertexClass defaultVertexClass;

	private AggregationClass defaultAggregationClass;

	private CompositionClass defaultCompositionClass;

	private GraphClass defaultGraphClass;

	protected GraphFactory graphFactory;

	@Override
	public Schema getSchema() {
		return this;
	}

	@Override
	public void setUniqueName(String uniqueName) {
		throw new SchemaException(
				"It is not allowed to explicitly set the unique name of a schema");
	}

	@Override
	public String getUniqueName() {
		return qName.getUniqueName();
	}

	/**
	 * builds a new schema
	 *
	 * @param qn
	 *            the qualified name of the schema
	 */
	public SchemaImpl(QualifiedName qn) {
		qName = qn;
		if (qName.getPackageName().length() == 0) {
			throw new SchemaException(
					"package prefix of Schema must not be empty");
		}
		try {
			packages = new TreeMap<QualifiedName, Package>();
			defaultPackage = PackageImpl.createDefaultPackage(this);
			addPackage(defaultPackage);
			namedElements = new HashMap<String, NamedElement>();
			reservedUniqueNames = new HashSet<String>();
			domains = new HashMap<QualifiedName, Domain>();
			graphClasses = new HashMap<QualifiedName, GraphClass>();
			// addDomain(BooleanDomainImpl.instance());
			// addDomain(IntDomainImpl.instance());
			// addDomain(LongDomainImpl.instance());
			// addDomain(StringDomainImpl.instance());
			// addDomain(DoubleDomainImpl.instance());
			// addDomain(ObjectDomainImpl.instance());
			addDomain(new BooleanDomainImpl(this));
			addDomain(new IntDomainImpl(this));
			addDomain(new LongDomainImpl(this));
			addDomain(new StringDomainImpl(this));
			addDomain(new DoubleDomainImpl(this));
			defaultGraphClass = createGraphClass(new QualifiedName("Graph"));
			addToKnownElements(defaultGraphClass.getUniqueName(),
					defaultGraphClass);
			defaultGraphClass.setInternal(true);
			defaultGraphClass.setAbstract(true);

			defaultVertexClass = defaultGraphClass
					.createVertexClass(new QualifiedName("Vertex"));
			addToKnownElements(defaultVertexClass.getUniqueName(),
					defaultVertexClass);
			defaultVertexClass.setInternal(true);
			defaultVertexClass.setAbstract(true);

			defaultEdgeClass = defaultGraphClass.createEdgeClass(
					new QualifiedName("Edge"), defaultVertexClass,
					defaultVertexClass);
			addToKnownElements(defaultEdgeClass.getUniqueName(),
					defaultEdgeClass);
			defaultEdgeClass.setInternal(true);
			defaultEdgeClass.setAbstract(true);

			defaultAggregationClass = defaultGraphClass.createAggregationClass(
					new QualifiedName("Aggregation"), defaultVertexClass, true,
					defaultVertexClass);
			addToKnownElements(defaultAggregationClass.getUniqueName(),
					defaultAggregationClass);
			defaultAggregationClass.setInternal(true);
			defaultAggregationClass.setAbstract(true);
			defaultAggregationClass.addSuperClass(defaultEdgeClass);

			defaultCompositionClass = defaultGraphClass.createCompositionClass(
					new QualifiedName("Composition"), defaultVertexClass, true,
					defaultVertexClass);
			addToKnownElements(defaultCompositionClass.getUniqueName(),
					defaultCompositionClass);
			defaultCompositionClass.setInternal(true);
			defaultCompositionClass.setAbstract(true);
			defaultCompositionClass.addSuperClass(defaultAggregationClass);
		} catch (SchemaException e) {
			// this may not happen, because the generated vertex and edge class
			// is the first one, so no other edge oder vertex
			// class with the same name may exist
			throw new RuntimeException("FIXME! This exception must not happen",
					e);
		}
	}

	@Override
	public void setGraphFactory(GraphFactory factory) {
		graphFactory = factory;
	}

	@Override
	public GraphFactory getGraphFactory() {
		return graphFactory;
	}

	/**
	 * Adds the given element with the given uniquename to the list of known
	 * elements and reserves the given unique name so it can not be used as
	 * unique name for other elements. If the unique name is already in use, the
	 * unique names of both elements (the known one and the new one) are changed
	 *
	 * @param name
	 * @param elem
	 */
	public void addToKnownElements(String name, NamedElement elem) {
		if (reservedUniqueNames.contains(name)) {
			NamedElement known = namedElements.get(name);
			if (known != null) {
				String uniqueName = QualifiedName.toUniqueName(known
						.getQualifiedName());
				known.setUniqueName(uniqueName);
				namedElements.remove(name);
				namedElements.put(uniqueName, known);
				reservedUniqueNames.add(uniqueName);
			}
			String uniqueName = QualifiedName.toUniqueName(elem
					.getQualifiedName());
			elem.setUniqueName(uniqueName);
			namedElements.put(uniqueName, elem);
			reservedUniqueNames.add(uniqueName);
		} else {
			namedElements.put(name, elem);
			reservedUniqueNames.add(name);
		}
	}

	/**
	 * adds the given domains to the domainlist
	 *
	 * @return true on success, false if a domain with the same name as the
	 *         given one already exists in the schema
	 */
	protected boolean addDomain(Domain d) {
		if (!isFreeDomainName(d.getQName())) {
			return false;
		}
		domains.put(d.getQName(), d);
		addToKnownElements(d.getUniqueName(), d);

		return true;
	}

	@Override
	public GraphClassImpl createGraphClass(QualifiedName name) {
		if (!isFreeSchemaElementName(name)) {
			throw new SchemaException(
					"there is already an element with the name " + name
							+ " in the schema");
		}

		if (name.isQualified()) {
			throw new SchemaException("GraphClass must have simple name, but "
					+ name + " is a qualified name");

		}
		GraphClassImpl graphClass;
		graphClass = new GraphClassImpl(name, this);
		graphClasses.put(name, graphClass);
		addToKnownElements(graphClass.getUniqueName(), graphClass);
		if (!name.getQualifiedName().equals("Graph")) {
			graphClass.addSuperClass(getDefaultGraphClass());
		}
		return graphClass;
	}

	@Override
	public EnumDomain createEnumDomain(QualifiedName qn,
			List<String> enumComponents) {
		EnumDomain ed = new EnumDomainImpl(this, qn, enumComponents);
		if (addDomain(ed)) {
			Package p = createPackageWithParents(qn.getPackageName());
			ed.setPackage(p);
			p.addDomain(ed);
			return ed;
		}
		throw new SchemaException("there is already an element with the name "
				+ qn + " in the schema", null);
	}

	@Override
	public EnumDomain createEnumDomain(QualifiedName qn) {
		return createEnumDomain(qn, new ArrayList<String>());
	}

	@Override
	public ListDomain createListDomain(Domain baseDomain) {
		QualifiedName domainName = new QualifiedName("", "List<"
				+ baseDomain.getTGTypeName(null) + ">");
		ListDomain d = (ListDomain) getDomain(domainName);
		if (d == null) {
			d = new ListDomainImpl(this, domainName, baseDomain);
			addDomain(d);
		}
		return d;
	}

	@Override
	public SetDomain createSetDomain(Domain baseDomain) {
		// TODO check if there should be an exception
		QualifiedName domainName = new QualifiedName("", "Set<"
				+ baseDomain.getTGTypeName(null) + ">");
		SetDomain d = (SetDomain) getDomain(domainName);
		if (d == null) {
			d = new SetDomainImpl(this, domainName, baseDomain);
			addDomain(d);
		}
		return d;
	}

	@Override
	public RecordDomain createRecordDomain(QualifiedName qn,
			Map<String, Domain> recordComponents) {
		RecordDomain rd = new RecordDomainImpl(this, qn, recordComponents);
		if (addDomain(rd)) {
			Package p = createPackageWithParents(qn.getPackageName());
			rd.setPackage(p);
			p.addDomain(rd);
			return rd;
		}
		throw new SchemaException("there is already an element with the name "
				+ qn + " in the schema", null);
	}

	@Override
	public RecordDomain createRecordDomain(QualifiedName qn) {
		return createRecordDomain(qn, new TreeMap<String, Domain>());
	}

	@Override
	public Vector<JavaSourceFromString> commit() {
		Vector<JavaSourceFromString> javaSources = new Vector<JavaSourceFromString>(
				0);

		// generate schema class
		CodeGenerator schemaCodeGenerator = new SchemaCodeGenerator(this,
				getPackageName(), GRAPHIMPLEMENTATIONPACKAGE);
		javaSources.addAll(schemaCodeGenerator.createJavaSources());

		// generate factory
		CodeGenerator factoryCodeGenerator = new GraphFactoryGenerator(this,
				getPackageName(), GRAPHIMPLEMENTATIONPACKAGE);
		javaSources.addAll(factoryCodeGenerator.createJavaSources());

		// generate graph classes
		for (GraphClass graphClass : graphClasses.values()) {
			if (graphClass.getQualifiedName().equals("Graph")) {
				continue;
			}

			GraphCodeGenerator graphCodeGenerator = new GraphCodeGenerator(
					graphClass, getPackageName(), GRAPHIMPLEMENTATIONPACKAGE,
					getSimpleName());
			javaSources.addAll(graphCodeGenerator.createJavaSources());

			// build graphelementclasses
			AttributedElementCodeGenerator codeGenerator = null;
			for (GraphElementClass graphElementClass : graphClass
					.getOwnGraphElementClasses()) {
				if (graphElementClass instanceof VertexClass) {
					codeGenerator = new VertexCodeGenerator(
							(VertexClass) graphElementClass, getPackageName(),
							GRAPHIMPLEMENTATIONPACKAGE);
					javaSources.addAll(codeGenerator.createJavaSources());
				}
				if (graphElementClass instanceof EdgeClass) {
					codeGenerator = new EdgeCodeGenerator(
							(EdgeClass) graphElementClass, getPackageName(),
							GRAPHIMPLEMENTATIONPACKAGE);
					javaSources.addAll(codeGenerator.createJavaSources());

					if (!graphElementClass.isAbstract()) {
						codeGenerator = new ReversedEdgeCodeGenerator(
								(EdgeClass) graphElementClass, qName
										.getPackageName(),
								GRAPHIMPLEMENTATIONPACKAGE);
						javaSources.addAll(codeGenerator.createJavaSources());
					}
				}
			}
		}
		// build records and enums
		for (Domain domain : domains.values()) {
			if (domain instanceof RecordDomain) {
				CodeGenerator rcode = new RecordCodeGenerator(
						(RecordDomain) domain, getPackageName(),
						GRAPHIMPLEMENTATIONPACKAGE);
				javaSources.addAll(rcode.createJavaSources());
			} else if (domain instanceof EnumDomain) {
				CodeGenerator ecode = new EnumCodeGenerator(
						(EnumDomain) domain, getPackageName(),
						GRAPHIMPLEMENTATIONPACKAGE);
				javaSources.addAll(ecode.createJavaSources());
			}
		}

		return javaSources;
	}

	@Override
	public void compile() {
		compile(null);
	}

	@Override
	public void compile(String jgralabClassPath) {
		JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
		JavaFileManager jfm = null;

		// commit
		Vector<JavaSourceFromString> javaSources = commit();
		// compile
		try {
			jfm = compiler.getStandardFileManager(null, null, null);
		} catch (NullPointerException e) {
			System.out
					.println("Cannot compile schema " + getSimpleName() + ".");
			System.out.println("Most probably you use a JRE instead of a JDK. "
					+ "The JRE does not provide a compiler.");
			e.printStackTrace();
		}

		ClassFileManager manager = new ClassFileManager(jfm);

		manager.setSources(javaSources);

		Vector<String> options = new Vector<String>();
		if (jgralabClassPath != null) {
			options.add("-cp");
			options.add(jgralabClassPath);
		}

		compiler.getTask(null, manager, null, options, null, javaSources)
				.call();
	}

	@Override
	public void commit(String pathPrefix) throws GraphIOException {
		commit(pathPrefix, null);
	}

	@Override
	public void commit(String pathPrefix, ProgressFunction progressFunction)
			throws GraphIOException {

		// progress bar for schema generation
		// ProgressFunctionImpl pf;
		long schemaElements = 0, currentCount = 0, interval = 1;
		if (progressFunction != null) {
			progressFunction.init(getNumberOfElements());
			interval = progressFunction.getInterval();
		}

		// ********************* build code **********************
		if (!pathPrefix.endsWith(File.separator)) {
			pathPrefix += File.separator;
		}

		// generate schema class
		CodeGenerator schemaCodeGenerator = new SchemaCodeGenerator(this,
				getPackageName(), GRAPHIMPLEMENTATIONPACKAGE);
		schemaCodeGenerator.createFiles(pathPrefix);

		// generate factory
		CodeGenerator factoryCodeGenerator = new GraphFactoryGenerator(this,
				getPackageName(), GRAPHIMPLEMENTATIONPACKAGE);
		factoryCodeGenerator.createFiles(pathPrefix);

		// generate graph classes
		Iterator<GraphClass> gcit = graphClasses.values().iterator();
		while (gcit.hasNext()) {
			GraphClass graphClass = gcit.next();
			if (graphClass.getQualifiedName().equals("Graph")) {
				continue;
			}

			GraphCodeGenerator graphCodeGenerator = new GraphCodeGenerator(
					graphClass, getPackageName(), GRAPHIMPLEMENTATIONPACKAGE,
					qName.getSimpleName());
			graphCodeGenerator.createFiles(pathPrefix);

			// build graphelementclasses
			Iterator<GraphElementClass> gecit = graphClass
					.getOwnGraphElementClasses().iterator();
			AttributedElementCodeGenerator codeGenerator = null;
			while (gecit.hasNext()) {
				GraphElementClass graphElementClass = gecit.next();
				if (graphElementClass instanceof VertexClass) {
					codeGenerator = new VertexCodeGenerator(
							(VertexClass) graphElementClass, getPackageName(),
							GRAPHIMPLEMENTATIONPACKAGE);
					codeGenerator.createFiles(pathPrefix);
				}
				if (graphElementClass instanceof EdgeClass) {
					codeGenerator = new EdgeCodeGenerator(
							(EdgeClass) graphElementClass, getPackageName(),
							GRAPHIMPLEMENTATIONPACKAGE);
					codeGenerator.createFiles(pathPrefix);

					if (!graphElementClass.isAbstract()) {
						codeGenerator = new ReversedEdgeCodeGenerator(
								(EdgeClass) graphElementClass,
								getPackageName(), GRAPHIMPLEMENTATIONPACKAGE);
						codeGenerator.createFiles(pathPrefix);
					}
				}

				// updateprogress bar
				if (progressFunction != null) {
					schemaElements++;
					currentCount++;
					if (currentCount == interval) {
						progressFunction.progress(schemaElements);
						currentCount = 0;
					}
				}
			}
		}
		Iterator<Domain> iter = domains.values().iterator();
		while (iter.hasNext()) {
			Domain domain = iter.next();
			if (domain instanceof RecordDomain) {
				CodeGenerator rcode = new RecordCodeGenerator(
						(RecordDomain) domain, getPackageName(),
						GRAPHIMPLEMENTATIONPACKAGE);
				rcode.createFiles(pathPrefix);
			} else if (domain instanceof EnumDomain) {
				CodeGenerator ecode = new EnumCodeGenerator(
						(EnumDomain) domain, getPackageName(),
						GRAPHIMPLEMENTATIONPACKAGE);
				ecode.createFiles(pathPrefix);
			}
			// update progress bar
			if (progressFunction != null) {
				schemaElements++;
				currentCount++;
				if (currentCount == interval) {
					progressFunction.progress(schemaElements);
					currentCount = 0;
				}
			}
		}

		// finish progress bar
		if (progressFunction != null) {
			progressFunction.finished();
		}
	}

	@Override
	public boolean containsGraphClass(GraphClass aGraphClass) {
		if (graphClasses.containsValue(aGraphClass)) {
			return true;
		}
		return false;
	}

	@Override
	public GraphClass getGraphClass(QualifiedName name) {
		if (!graphClasses.containsKey(name)) {
			return null;
		}
		return graphClasses.get(name);
	}

	@Override
	public String toString() {
		String output = "GraphClasses of schema '" + qName.getQualifiedName()
				+ "':\n\n\n";
		for (GraphClass gc : graphClasses.values()) {
			output += gc.toString();
		}

		return output;
	}

	/**
	 * only used internally
	 *
	 * @return number of graphelementclasses contained in graphclass
	 */
	private int getNumberOfElements() {
		int count = 0;
		for (GraphClass gc : graphClasses.values()) {
			count += gc.getOwnGraphElementClasses().size() + 1;
		}
		return count;
	}

	@Override
	public AttributedElementClass getClass(AttributedElement anAttributedElement) {
		// String aeClassName = anAttributedElement.getClass().getSimpleName();
		String aeClassName = anAttributedElement.getAttributedElementClass()
				.getQualifiedName()
				+ "Impl";
		aeClassName = aeClassName.substring(0, aeClassName.length() - 4); // cut
		// "Impl"
		// off
		// at
		// the
		// back

		// search for class in graphclasses and each of their
		// graphelementclasses
		Iterator<Entry<QualifiedName, GraphClass>> it = graphClasses.entrySet()
				.iterator();
		while (it.hasNext()) {
			Entry<QualifiedName, GraphClass> gc = it.next();
			if (gc.getKey().equals(aeClassName)) {
				return gc.getValue();
			}
			Iterator<GraphElementClass> it2 = gc.getValue()
					.getOwnGraphElementClasses().iterator();
			while (it2.hasNext()) {
				GraphElementClass gec = it2.next();
				if (gec.getQualifiedName().equals(aeClassName)) {
					return gec;
				}
			}
		}
		return null;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see jgralab.Schema#getDomains()
	 */
	public Map<QualifiedName, Domain> getDomains() {
		return domains;
	}

	public Map<QualifiedName, GraphClass> getGraphClasses() {
		return graphClasses;
	}

	public Map<QualifiedName, Package> getPackages() {
		return packages;
	}

	public Package getPackage(String packageName) {
		return packages.get(new QualifiedName(packageName));
	}

	@Override
	public AttributedElementClass getAttributedElementClass(QualifiedName name) {
		AttributedElementClass search;
		if (graphClasses.containsKey(name)) {
			return graphClasses.get(name);
		}
		for (GraphClass graphClass : graphClasses.values()) {
			search = graphClass.getGraphElementClass(name);
			if (search != null) {
				return search;
			}
		}
		return null;
	}

	public List<GraphClass> getGraphClassesInTopologicalOrder() {
		ArrayList<GraphClass> topologicalOrderList = new ArrayList<GraphClass>();
		GraphClass gc;
		HashSet<GraphClass> graphClassSet = new HashSet<GraphClass>();

		// store graph classes in graphClassSet
		for (GraphClass gcl : graphClasses.values()) {
			graphClassSet.add(gcl);
		}

		// topologicalOrderList.add(getDefaultGraphClass());

		// iteratively add classes from graphClassSet,
		// whose superclasses already are in topologicalOrderList,
		// to topologicalOrderList
		// the added classes are removed from graphClassSet
		while (!graphClassSet.isEmpty()) {
			for (Iterator<GraphClass> gcit = graphClassSet.iterator(); gcit
					.hasNext();) {
				gc = gcit.next();
				if (topologicalOrderList.containsAll(gc.getAllSuperClasses())) {
					topologicalOrderList.add(gc);
					gcit.remove();
				}
			}
		}

		return topologicalOrderList;
	}

	public List<VertexClass> getVertexClassesInTopologicalOrder() {
		ArrayList<VertexClass> topologicalOrderList = new ArrayList<VertexClass>();
		VertexClass vc;
		HashSet<VertexClass> vertexClassSet = new HashSet<VertexClass>();
		List<GraphClass> graphClassList = getGraphClassesInTopologicalOrder();

		for (GraphClass gc : graphClassList) {
			// store vertex classes in vertexClassSet
			for (VertexClass vcl : gc.getOwnVertexClasses()) {
				vertexClassSet.add(vcl);
			}

			// topologicalOrderList.add(getDefaultVertexClass());

			// iteratively add classes from vertexClassSet,
			// whose superclasses already are in topologicalOrderList,
			// to topologicalOrderList
			// the added classes are removed from vertexClassSet
			while (!vertexClassSet.isEmpty()) {
				for (Iterator<VertexClass> vcit = vertexClassSet.iterator(); vcit
						.hasNext();) {
					vc = vcit.next();
					if (topologicalOrderList.containsAll(vc
							.getAllSuperClasses())) {
						topologicalOrderList.add(vc);
						vcit.remove();
					}
				}
			}
		}

		return topologicalOrderList;
	}

	public List<EdgeClass> getEdgeClassesInTopologicalOrder() {
		ArrayList<EdgeClass> topologicalOrderList = new ArrayList<EdgeClass>();
		EdgeClass ec;
		HashSet<EdgeClass> edgeClassSet = new HashSet<EdgeClass>();
		List<GraphClass> graphClassList = getGraphClassesInTopologicalOrder();

		for (GraphClass gc : graphClassList) {
			// store edge classes in edgeClassSet
			for (EdgeClass ecl : gc.getOwnEdgeClasses()) {
				edgeClassSet.add(ecl);
			}
			for (EdgeClass ecl : gc.getOwnAggregationClasses()) {
				edgeClassSet.add(ecl);
			}
			for (EdgeClass ecl : gc.getOwnCompositionClasses()) {
				edgeClassSet.add(ecl);
			}

			// iteratively add classes from edgeClassSet,
			// whose superclasses already are in topologicalOrderList,
			// to topologicalOrderList
			// the added classes are removed from edgeClassSet
			while (!edgeClassSet.isEmpty()) {
				for (Iterator<EdgeClass> ecit = edgeClassSet.iterator(); ecit
						.hasNext();) {
					ec = ecit.next();
					if (topologicalOrderList.containsAll(ec
							.getAllSuperClasses())) {
						topologicalOrderList.add(ec);
						ecit.remove();
					}
				}
			}
		}

		return topologicalOrderList;
	}

	public List<EnumDomain> getEnumDomains() {
		ArrayList<EnumDomain> enumList = new ArrayList<EnumDomain>();

		for (Domain dl : domains.values()) {
			if (dl instanceof EnumDomain) {
				enumList.add((EnumDomain) dl);
			}
		}

		return enumList;
	}

	public List<RecordDomain> getRecordDomains() {
		ArrayList<RecordDomain> recordList = new ArrayList<RecordDomain>();

		for (Domain dl : domains.values()) {
			if (dl instanceof RecordDomain) {
				recordList.add((RecordDomain) dl);
			}
		}

		return recordList;
	}

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

	@Override
	public String getName() {
		return qName.toString();
	}

	@Override
	public String getQualifiedName() {
		return qName.getQualifiedName();
	}

	@Override
	public Domain getDomain(QualifiedName domainName) {
		return domains.get(domainName);
	}

	@Override
	public Domain getDomain(String domainName) {
		return domains.get(new QualifiedName(domainName));
	}

	@SuppressWarnings("unchecked")
	private Class<? extends Graph> getGraphClassImpl(
			QualifiedName graphClassName) {
		String implClassName = getPackageName() + "." + IMPLPACKAGENAME + "."
				+ graphClassName.getSimpleName() + "Impl";
		Class<? extends Graph> m1Class;
		try {
			m1Class = (Class<? extends Graph>) Class.forName(implClassName,
					true, M1ClassManager.instance());
		} catch (ClassNotFoundException e) {
			throw new SchemaException("can't load implementation class '"
					+ implClassName + "'", e);
		}
		return m1Class;
	}

	private Method getCreateMethod(QualifiedName className,
			QualifiedName graphClassName, Class<?>[] signature) {
		Class<? extends Graph> m1Class = null;
		try {
			m1Class = getGraphClassImpl(graphClassName);
			if (className.equals(graphClassName)) {
				return m1Class.getMethod("create", signature);
			} else {
				GraphClass gc = getGraphClasses().get(graphClassName);
				VertexClass vc = gc.getVertexClass(className);
				if (vc != null) {
					className = vc.getQName();
				} else {
					EdgeClass ec = gc.getEdgeClass(className);
					if (ec != null) {
						className = ec.getQName();
					} else {
						throw new SchemaException("class "
								+ className.getQualifiedName()
								+ " does not exist in schema");
					}
				}
				return m1Class.getMethod("create"
						+ CodeGenerator.camelCase(className.getUniqueName()),
						signature);
			}
		} catch (SecurityException e) {
			throw new SchemaException("can't find create method in '"
					+ m1Class.getName() + "' for '" + className.getUniqueName()
					+ "'", e);
		} catch (NoSuchMethodException e) {
			throw new SchemaException("can't find create method in '"
					+ m1Class.getName() + "' for '" + className.getUniqueName()
					+ "'", e);
		}
	}

	static final Class<?>[] graphClassCreateSignature = { String.class,
			int.class, int.class };

	public Method getGraphCreateMethod(QualifiedName graphClassName) {
		return getCreateMethod(graphClassName, graphClassName,
				graphClassCreateSignature);
	}

	static final Class<?>[] vertexClassCreateSignature = { int.class };

	public Method getVertexCreateMethod(QualifiedName vertexClassName,
			QualifiedName graphClassName) {
		return getCreateMethod(vertexClassName, graphClassName,
				vertexClassCreateSignature);
	}

	public Method getEdgeCreateMethod(QualifiedName edgeClassName,
			QualifiedName graphClassName) {

		// Edge class create method cannot be found directly by its signature
		// because the vertex parameters are subclassed to match the to- and
		// from-class. Those subclasses are unknown in this method. Therefore,
		// we look for a method with correct name and 3 parameters
		// (int, vertex, Vertex).
		String methodName = "create"
				+ CodeGenerator.camelCase(edgeClassName.getUniqueName());
		Class<?> m1Class = getGraphClassImpl(graphClassName);
		for (Method m : m1Class.getMethods()) {
			if (m.getName().equals(methodName)
					&& m.getParameterTypes().length == 3) {
				return m;
			}
		}
		throw new SchemaException("can't find create method in '"
				+ m1Class.getName() + "' for '" + edgeClassName.getUniqueName()
				+ "'");
	}

	public AggregationClass getDefaultAggregationClass() {
		return defaultAggregationClass;
	}

	public CompositionClass getDefaultCompositionClass() {
		return defaultCompositionClass;
	}

	public EdgeClass getDefaultEdgeClass() {
		return defaultEdgeClass;
	}

	public GraphClass getDefaultGraphClass() {
		return defaultGraphClass;
	}

	public VertexClass getDefaultVertexClass() {
		return defaultVertexClass;
	}

	@Override
	public boolean knows(QualifiedName name) {
		return getAttributedElementClass(name) != null
				|| domains.get(name) != null || getGraphClass(name) != null
				|| getSimpleName().equals(name);
	}

	@Override
	public boolean isFreeDomainName(QualifiedName name) {
		return isValidSchemaElementName(name) && !knows(name);
	}

	@Override
	public boolean isFreeSchemaElementName(QualifiedName name) {
		return isValidSchemaElementName(name) && !knows(name);
	}

	@Override
	public boolean isValidSchemaElementName(QualifiedName name) {
		return !reservedJavaWords.contains(name.getQualifiedName());
	}

	@Override
	public Attribute createAttribute(String name, Domain dom) {
		return new AttributeImpl(name, dom);
	}

	/**
	 * File Manager class overwriting the method {@code getJavaFileForOutput} so
	 * that bytecode is written to a {@code ClassFileAbstraction}.
	 *
	 */
	private class ClassFileManager extends
			ForwardingJavaFileManager<JavaFileManager> {
		Vector<JavaSourceFromString> sources;

		public ClassFileManager(JavaFileManager fm) {
			super(fm);
		}

		@Override
		public JavaFileObject getJavaFileForOutput(Location location,
				String className, Kind kind, FileObject sibling) {
			ClassFileAbstraction cfa = new ClassFileAbstraction(className);

			M1ClassManager.instance().putM1Class(className, cfa);
			return cfa;
		}

		public void setSources(Vector<JavaSourceFromString> sources) {
			this.sources = sources;
		}
	}

	@Override
	public Package getDefaultPackage() {
		return defaultPackage;
	}

	@Override
	public String getDirectoryName() {
		return qName.getDirectoryName();
	}

	@Override
	public String getPackageName() {
		return qName.getPackageName();
	}

	@Override
	public String getPathName() {
		return qName.getPathName();
	}

	@Override
	public String getSimpleName() {
		return qName.getSimpleName();
	}

	@Override
	public QualifiedName getQName() {
		return qName;
	}

	@Override
	public String getQualifiedName(Package pkg) {
		throw new UnsupportedOperationException();
	}

	@Override
	public String getVariableName() {
		throw new UnsupportedOperationException();
	}

	@Override
	public Package createPackageWithParents(String qualifiedName) {
		if (qualifiedName.length() == 0) {
			return defaultPackage;
		}
		String[] simpleNames = qualifiedName.split("\\.");
		Package p = defaultPackage;
		for (String simpleName : simpleNames) {
			if (p.containsSubPackage(simpleName)) {
				p = p.getSubPackage(simpleName);
			} else {
				p = p.createSubPackage(simpleName);
			}
		}
		return p;
	}

	@Override
	public void addPackage(Package p) {
		packages.put(p.getQName(), p);
		// TODO check if packages should have unique names
		// addToKnownElements(p.getUniqueName(), p);
	}

	public boolean allowsLowercaseEnumConstants() {
		return allowLowercaseEnumConstants;
	}

	public void setAllowLowercaseEnumConstants(
			boolean allowLowercaseEnumConstants) {
		this.allowLowercaseEnumConstants = allowLowercaseEnumConstants;
	}

	public boolean isValidEnumConstant(String name) {
		if (!allowsLowercaseEnumConstants()) {
			for (int i = 0; i < name.length(); i++) {
				if (Character.isLowerCase(name.charAt(i))) {
					return false;
				}
			}
		}
		if (reservedJavaWords.contains(name)) {
			return false;
		}
		return true;
	}
}