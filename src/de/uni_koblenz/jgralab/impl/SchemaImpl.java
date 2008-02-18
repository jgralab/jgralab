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
 
package de.uni_koblenz.jgralab.impl;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Vector;
import java.util.Map.Entry;

import javax.tools.FileObject;
import javax.tools.ForwardingJavaFileManager;
import javax.tools.JavaCompiler;
import javax.tools.JavaFileManager;
import javax.tools.JavaFileObject;
import javax.tools.StandardJavaFileManager;
import javax.tools.ToolProvider;
import javax.tools.JavaFileObject.Kind;

import de.uni_koblenz.jgralab.AggregationClass;
import de.uni_koblenz.jgralab.Attribute;
import de.uni_koblenz.jgralab.AttributedElement;
import de.uni_koblenz.jgralab.AttributedElementClass;
import de.uni_koblenz.jgralab.CompositeDomain;
import de.uni_koblenz.jgralab.CompositionClass;
import de.uni_koblenz.jgralab.Domain;
import de.uni_koblenz.jgralab.EdgeClass;
import de.uni_koblenz.jgralab.EnumDomain;
import de.uni_koblenz.jgralab.GraphClass;
import de.uni_koblenz.jgralab.GraphElementClass;
import de.uni_koblenz.jgralab.GraphFactory;
import de.uni_koblenz.jgralab.GraphIOException;
import de.uni_koblenz.jgralab.ListDomain;
import de.uni_koblenz.jgralab.M1ClassManager;
import de.uni_koblenz.jgralab.ProgressFunction;
import de.uni_koblenz.jgralab.RecordDomain;
import de.uni_koblenz.jgralab.Schema;
import de.uni_koblenz.jgralab.SchemaException;
import de.uni_koblenz.jgralab.SetDomain;
import de.uni_koblenz.jgralab.VertexClass;
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

public class SchemaImpl implements Schema {

	private String implPackage = "array";

	/**
	 * the name of the schema
	 */
	private String name;

	private Map<String, Domain> domains;

	/**
	 * saves all graphclasses which are contained in the schema
	 */
	protected Map<String, GraphClass> graphClasses;

	/**
	 * the prefix for the generation of the m2 elements, the prefix is added
	 * prior to the generated package
	 */
	private String packagePrefix;

	/**
	 * the massa of all edsge classes
	 */
	private EdgeClass defaultEdgeClass;

	/**
	 * 
	 */
	private VertexClass defaultVertexClass;

	private AggregationClass defaultAggregationClass;

	private CompositionClass defaultCompositionClass;

	private GraphClass defaultGraphClass;
	
	protected GraphFactory graphFactory;

	/**
	 * builds a new schema
	 * 
	 * @param aName
	 *            the name of the schema
	 */
	public SchemaImpl(String aName, String packagePrefix) {
		this.name = aName;
		this.packagePrefix = packagePrefix;
		domains = new HashMap<String, Domain>();
		graphClasses = new HashMap<String, GraphClass>();
		addDomain(new BooleanDomainImpl());
		addDomain(new IntDomainImpl());
		addDomain(new LongDomainImpl());
		addDomain(new StringDomainImpl());
		addDomain(new DoubleDomainImpl());
		addDomain(new ObjectDomainImpl());
		try {
			//System.out.println("Creating default classes");
			defaultGraphClass = createGraphClass("Graph");
			defaultGraphClass.setInternal(true);
			defaultGraphClass.setAbstract(true);
			defaultVertexClass = defaultGraphClass.createVertexClass("Vertex");
			defaultVertexClass.setInternal(true);
			defaultVertexClass.setAbstract(true);
			defaultEdgeClass = defaultGraphClass.createEdgeClass("Edge",
					defaultVertexClass, defaultVertexClass);
			defaultEdgeClass.setInternal(true);
			defaultEdgeClass.setAbstract(true);
			defaultAggregationClass = defaultGraphClass
					.createAggregationClass("Aggregation", defaultVertexClass,
							true, defaultVertexClass);
			defaultAggregationClass.setInternal(true);
			defaultAggregationClass.setAbstract(true);
			defaultAggregationClass.addSuperClass(defaultEdgeClass);
			defaultCompositionClass = defaultGraphClass
					.createCompositionClass("Composition", defaultVertexClass,
							true, defaultVertexClass);
			defaultCompositionClass.setInternal(true);
			defaultCompositionClass.setAbstract(true);
			defaultCompositionClass.addSuperClass(defaultAggregationClass);
		} catch (SchemaException ex) {
			System.out.println("Exception Terror");
			// this may not happen, because the generated vertex and edge class
			// is the first one, so no other edge oder vertex
			// class with the same name may exist
			throw new RuntimeException("FIXME!");
		}
	}

	
	public void setGraphFactory(GraphFactory factory) {
		graphFactory = factory;
	}
	

	public GraphFactory getGraphFactory() {
		return graphFactory;
	}
	
	/**
	 * adds the given domains to the domainlist
	 * 
	 * @return true on success, false if a domain with the same name as the
	 *         given one already exists in the schema
	 */
	protected boolean addDomain(Domain d) {
		if (!isFreeDomainName(d.getName()))
			return false;
		domains.put(d.getName(), d);
		return true;
	}

	/**
	 * builds a new schema
	 * 
	 * @param aName
	 *            the name of the schema
	 */
	public static Schema create(String aName, String packagePrefix) {
		return new SchemaImpl(aName, packagePrefix);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see jgralab.Schema#createGraphClass(java.lang.String)
	 */
	public GraphClassImpl createGraphClass(String name)  {
		if (!isFreeSchemaElementName(name))
			throw new SchemaException(
					"there is already an element with the name " + name
							+ " in the schema", null);
		GraphClassImpl graphClass;
		graphClass = new GraphClassImpl(name, this);
		graphClasses.put(name, graphClass);
		graphClass.addSuperClass(this.getGraphClass("Graph"));
		return graphClass;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see jgralab.Schema#createEnumDomain(java.lang.String,
	 *      java.util.SortedSet)
	 */
	public EnumDomain createEnumDomain(String name, List<String> enumComponents)
			 {
		EnumDomain ed = new EnumDomainImpl(name, enumComponents);
		if (addDomain(ed))
			return ed;
		else
			throw new SchemaException(
					"there is already an element with the name " + name
							+ " in the schema", null);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see jgralab.Schema#createEnumDomain(java.lang.String)
	 */
	public EnumDomain createEnumDomain(String name)  {
		EnumDomain ed = new EnumDomainImpl(name);
		if (addDomain(ed))
			return ed;
		else
			throw new SchemaException(
					"there is already an element with the name " + name
							+ " in the schema", null);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see jgralab.Schema#createListDomain(jgralab.Domain)
	 */
	public ListDomain createListDomain(Domain baseDomain)
			 {
		String domainName = "List<" + baseDomain.getTGTypeName() + ">";
		ListDomain d = (ListDomain) getDomain(domainName);
		if (d == null) {
			d = new ListDomainImpl(domainName, baseDomain);
			addDomain(d);
		}
		return d;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see jgralab.Schema#createSetDomain(jgralab.Domain)
	 */
	public SetDomain createSetDomain(Domain baseDomain)  {
		// TODO check if there should be an exception
		String domainName = "Set<" + baseDomain.getTGTypeName() + ">";
		SetDomain d = (SetDomain) getDomain(domainName);
		if (d == null) {
			d = new SetDomainImpl(domainName, baseDomain);
			addDomain(d);
		}
		return d;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see jgralab.Schema#createRecordDomain(java.lang.String, java.util.List)
	 */
	public RecordDomain createRecordDomain(String name,
			Map<String, Domain> recordComponents)  {
		RecordDomain rd = new RecordDomainImpl(name, recordComponents);
		if (addDomain(rd))
			return rd;
		else
			throw new SchemaException(
					"there is already an element with the name " + name
							+ " in the schema", null);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see jgralab.Schema#createRecordDomain(java.lang.String)
	 */
	public RecordDomain createRecordDomain(String name)  {
		RecordDomain rd = new RecordDomainImpl(name);
		if (addDomain(rd))
			return rd;
		else
			throw new SchemaException(
					"there is already an element with the name " + name
							+ " in the schema", null);
	}
	
	public Vector<JavaSourceFromString> commit() {
		Vector<JavaSourceFromString> javaSources 
				= new Vector<JavaSourceFromString>(0);
		
		String schemaPackageName = packagePrefix;

		// generate schema class
		CodeGenerator schemaCodeGenerator = new SchemaCodeGenerator(this,
				schemaPackageName, implPackage);
		javaSources.addAll(schemaCodeGenerator.createJavaSources());
		
		
		//generate factory
		CodeGenerator factoryCodeGenerator = new GraphFactoryGenerator(this, schemaPackageName, implPackage);
		javaSources.addAll(factoryCodeGenerator.createJavaSources());
		
		// generate graph classes
		for (GraphClass graphClass : graphClasses.values()) {
			if (graphClass.getName().equals("Graph"))
				continue;

			GraphCodeGenerator graphCodeGenerator = new GraphCodeGenerator(
					graphClass, schemaPackageName, implPackage, name);
			javaSources.addAll(graphCodeGenerator.createJavaSources());

			// build graphelementclasses
			AttributedElementCodeGenerator codeGenerator = null;
			for (GraphElementClass graphElementClass :
						graphClass.getOwnGraphElementClasses()) {
				if (graphElementClass instanceof VertexClass) {
					codeGenerator = new VertexCodeGenerator(
							(VertexClass) graphElementClass, schemaPackageName,
							implPackage);
					javaSources.addAll(codeGenerator.createJavaSources());
				}
				if (graphElementClass instanceof EdgeClass) {
					codeGenerator = new EdgeCodeGenerator(
							(EdgeClass) graphElementClass, schemaPackageName,
							implPackage);
					javaSources.addAll(codeGenerator.createJavaSources());

					if (!graphElementClass.isAbstract()) {
						codeGenerator = new ReversedEdgeCodeGenerator(
								(EdgeClass) graphElementClass,
								schemaPackageName, implPackage);
						javaSources.addAll(codeGenerator.createJavaSources());
					}
				}
			}
		}

		// build records and enums
		for (Domain domain : domains.values()) {
			if (domain instanceof RecordDomain) {
				CodeGenerator rcode = new RecordCodeGenerator(
						(RecordDomain) domain, schemaPackageName, implPackage);
				javaSources.addAll(rcode.createJavaSources());
			} else if (domain instanceof EnumDomain) {
				CodeGenerator ecode = new EnumCodeGenerator(
						(EnumDomain) domain, schemaPackageName, implPackage);
				javaSources.addAll(ecode.createJavaSources());
			}
		}
		
		return javaSources;
	}
	
	public void compile() {
		compile(null);
	}
	
	public void compile(String jgralabClassPath) {
		JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
		
		//commit
		Vector<JavaSourceFromString> javaSources = commit();				
		//compile
		JavaFileManager jfm = compiler.getStandardFileManager(null, null, null);
		ClassFileManager manager = new ClassFileManager(jfm);
		
		manager.setSources(javaSources);
		
		Vector<String> options = new Vector<String>();
		options.add("-cp");
		options.add(jgralabClassPath);
		
		if (jgralabClassPath == null) {
			compiler.getTask(null, manager, null, null, null, javaSources).call();
		} else {
			compiler.getTask(null, manager, null, options, null, javaSources).call();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see jgralab.Schema#commit()
	 */
	public void commit(String pathPrefix) throws GraphIOException {
		commit(pathPrefix, null);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see jgralab.Schema#commit(java.lang.String, jgralab.ProgressFunction)
	 */
	public void commit(String pathPrefix, ProgressFunction progressFunction)
			throws GraphIOException {

		// progress bar for schema generation
		// ProgressFunctionImpl pf;
		int schemaElements = 0, currentCount = 0, interval = 1;
		if (progressFunction != null) {
			progressFunction.init(getNumberOfElements());
			interval = progressFunction.getInterval();
		}

		// ********************* build code **********************
		if (!pathPrefix.endsWith(File.separator)) {
			pathPrefix += File.separator;
		}
		String schemaPackageName = packagePrefix;

		// generate schema class
		CodeGenerator schemaCodeGenerator = new SchemaCodeGenerator(this,
				schemaPackageName, implPackage);
		schemaCodeGenerator.createFiles(pathPrefix);
		
		//generate factory
		CodeGenerator factoryCodeGenerator = new GraphFactoryGenerator(this, schemaPackageName, implPackage);
		factoryCodeGenerator.createFiles(pathPrefix);
		
		// generate graph classes
		Iterator<GraphClass> gcit = graphClasses.values().iterator();
		while (gcit.hasNext()) {
			GraphClass graphClass = (GraphClass) gcit.next();
			if (graphClass.getName().equals("Graph"))
				continue;

			GraphCodeGenerator graphCodeGenerator = new GraphCodeGenerator(
					graphClass, schemaPackageName, implPackage, name);
			graphCodeGenerator.createFiles(pathPrefix);

			// build graphelementclasses
			Iterator<GraphElementClass> gecit = graphClass.getOwnGraphElementClasses().iterator();
			AttributedElementCodeGenerator codeGenerator = null;
			while (gecit.hasNext()) {
				GraphElementClass graphElementClass = (GraphElementClass) gecit
						.next();
				if (graphElementClass instanceof VertexClass) {
					codeGenerator = new VertexCodeGenerator(
							(VertexClass) graphElementClass, schemaPackageName,
							implPackage);
					codeGenerator.createFiles(pathPrefix);
				}
				if (graphElementClass instanceof EdgeClass) {
					codeGenerator = new EdgeCodeGenerator(
							(EdgeClass) graphElementClass, schemaPackageName,
							implPackage);
					codeGenerator.createFiles(pathPrefix);

					if (!graphElementClass.isAbstract()) {
						codeGenerator = new ReversedEdgeCodeGenerator(
								(EdgeClass) graphElementClass,
								schemaPackageName, implPackage);
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
						(RecordDomain) domain, schemaPackageName, implPackage);
				rcode.createFiles(pathPrefix);
			} else if (domain instanceof EnumDomain) {
				CodeGenerator ecode = new EnumCodeGenerator(
						(EnumDomain) domain, schemaPackageName, implPackage);
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

	/*
	 * (non-Javadoc)
	 * 
	 * @see jgralab.Schema#containsGraphClass(jgralab.GraphClass)
	 */
	public boolean containsGraphClass(GraphClass aGraphClass) {
		if (graphClasses.containsValue(aGraphClass))
			return true;
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see jgralab.Schema#getGraphClass(java.lang.String)
	 */
	public GraphClass getGraphClass(String name) {
		if (!graphClasses.containsKey(name))
			return null;
		return (GraphClass) graphClasses.get(name);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see jgralab.Schema#getName()
	 */
	public String getName() {
		return name;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		String output = "GraphClasses of schema '" + name + "':\n\n\n";
		Iterator<Map.Entry<String, GraphClass>> it = graphClasses.entrySet().iterator();
		while (it.hasNext()) {
			output += it.next().toString();
		}

		return output;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see jgralab.Schema#setPrefix(java.lang.String)
	 */
	public void setPrefix(String prefix) {
		this.packagePrefix = prefix;
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

	/*
	 * (non-Javadoc)
	 * 
	 * @see jgralab.Schema#getClass(jgralab.AttributedElement)
	 */
	public AttributedElementClass getClass(AttributedElement anAttributedElement) {
		// String aeClassName = anAttributedElement.getClass().getSimpleName();
		String aeClassName = anAttributedElement.getAttributedElementClass()
				.getName()
				+ "Impl";
		aeClassName = aeClassName.substring(0, aeClassName.length() - 4); // cut
		// "Impl"
		// off
		// at
		// the
		// back
		// search for class in graphclasses and each of their
		// graphelementclasses
		Iterator<Entry<String, GraphClass>> it = graphClasses.entrySet()
				.iterator();
		while (it.hasNext()) {
			Entry<String, GraphClass> gc = it.next();
			if (gc.getKey().equals(aeClassName))
				return (AttributedElementClass) gc.getValue();
			Iterator<GraphElementClass> it2 = gc.getValue()
					.getOwnGraphElementClasses().iterator();
			while (it2.hasNext()) {
				GraphElementClass gec = it2.next();
				if (gec.getName().equals(aeClassName))
					return gec;
			}
		}
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see jgralab.Schema#getDomains()
	 */
	public Map<String, Domain> getDomains() {
		return domains;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see jgralab.Schema#getGraphClasses()
	 */
	public Map<String, GraphClass> getGraphClasses() {
		return graphClasses;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see jgralab.Schema#getAttributedElementClass(java.lang.String)
	 */
	public AttributedElementClass getAttributedElementClass(String name) {
		AttributedElementClass search;
		if (graphClasses.containsKey(name))
			return graphClasses.get(name);
		for (Iterator<GraphClass> gcit = graphClasses.values().iterator(); gcit
				.hasNext();) {
			search = gcit.next().getGraphElementClass(name);
			if (search != null)
				return search;
		}
		return null;
	}

	public List<GraphClass> getGraphClassesInTopologicalOrder() {
		ArrayList<GraphClass> topologicalOrderList = new ArrayList<GraphClass>();
		GraphClass gc;
		HashSet<GraphClass> graphClassSet = new HashSet<GraphClass>();

		// store graph classes in graphClassSet
		for (GraphClass gcl : graphClasses.values())
			graphClassSet.add(gcl);

		// topologicalOrderList.add(getDefaultGraphClass());

		// iteratively add classes from graphClassSet,
		// whose superclasses already are in topologicalOrderList,
		// to topologicalOrderList
		// the added classes are removed from graphClassSet
		while (!graphClassSet.isEmpty())
			for (Iterator<GraphClass> gcit = graphClassSet.iterator(); gcit
					.hasNext();) {
				gc = gcit.next();
				if (topologicalOrderList.containsAll(gc.getAllSuperClasses())) {
					topologicalOrderList.add(gc);
					gcit.remove();
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
			for (VertexClass vcl : gc.getOwnVertexClasses())
				vertexClassSet.add(vcl);

			// topologicalOrderList.add(getDefaultVertexClass());

			// iteratively add classes from vertexClassSet,
			// whose superclasses already are in topologicalOrderList,
			// to topologicalOrderList
			// the added classes are removed from vertexClassSet
			while (!vertexClassSet.isEmpty())
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

		return topologicalOrderList;
	}

	public List<EdgeClass> getEdgeClassesInTopologicalOrder() {
		ArrayList<EdgeClass> topologicalOrderList = new ArrayList<EdgeClass>();
		EdgeClass ec;
		HashSet<EdgeClass> edgeClassSet = new HashSet<EdgeClass>();
		List<GraphClass> graphClassList = getGraphClassesInTopologicalOrder();

		for (GraphClass gc : graphClassList) {
			// store edge classes in edgeClassSet
			for (EdgeClass ecl : gc.getOwnEdgeClasses())
				edgeClassSet.add(ecl);
			for (EdgeClass ecl : gc.getOwnAggregationClasses())
				edgeClassSet.add(ecl);
			for (EdgeClass ecl : gc.getOwnCompositionClasses())
				edgeClassSet.add(ecl);

			// iteratively add classes from edgeClassSet,
			// whose superclasses already are in topologicalOrderList,
			// to topologicalOrderList
			// the added classes are removed from edgeClassSet
			while (!edgeClassSet.isEmpty())
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

		return topologicalOrderList;
	}

	public List<EnumDomain> getEnumDomains() {
		ArrayList<EnumDomain> enumList = new ArrayList<EnumDomain>();

		for (Domain dl : domains.values())
			if (dl instanceof EnumDomain)
				enumList.add((EnumDomain) dl);

		return enumList;
	}

	public List<CompositeDomain> getCompositeDomainsInTopologicalOrder() {
		ArrayList<CompositeDomain> topologicalOrderList = new ArrayList<CompositeDomain>();
		CompositeDomain cd;
		HashSet<CompositeDomain> compositeDomainSet = new HashSet<CompositeDomain>();

		// store composite domains in compositeDomainSet
		for (Domain dl : domains.values())
			if (dl instanceof CompositeDomain)
				compositeDomainSet.add((CompositeDomain) dl);

		// iteratively add domains from compositeDomainSet,
		// whose component domains already are in topologicalOrderList,
		// to topologicalOrderList
		// the added domains are removed from compositeDomainSet
		while (!compositeDomainSet.isEmpty())
			for (Iterator<CompositeDomain> cdit = compositeDomainSet.iterator(); cdit
					.hasNext();) {
				cd = cdit.next();
				if (topologicalOrderList.containsAll(cd
						.getAllComponentCompositeDomains())) {
					topologicalOrderList.add(cd);
					cdit.remove();
				}
			}

		return topologicalOrderList;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see jgralab.Schema#getPrefix()
	 */
	public String getPrefix() {
		return packagePrefix;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see jgralab.Schema#getFullName()
	 */
	public String getFullName() {
		if (packagePrefix.equals(""))
			return name;
		else {
			if (packagePrefix.endsWith("."))
				return packagePrefix + name;
			else
				return packagePrefix + "." + name;
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see jgralab.Schema#getDomain(java.lang.String)
	 */
	public Domain getDomain(String domainName) {
		return domains.get(domainName);
	}

	public void setListImplementation()  {
		this.implPackage = "list";
		// this.commit(".");
	}

	public void setArrayImplementation()  {
		this.implPackage = "array";
		// this.commit(".");
	}

	private Class<?> getGraphClassImpl(String graphClassName)
			 {
		String implClassName = getPrefix() + ".impl." + graphClassName + "Impl";
		Class<?> m1Class;
		try {
			m1Class = Class.forName(implClassName, true, M1ClassManager.instance());
		} catch (ClassNotFoundException e) {
			throw new SchemaException("can't load implementation class '"
					+ implClassName + "'", e);
		}
		return m1Class;
	}

	private Method getCreateMethod(String className, String graphClassName,
			Class<?>[] signature)  {
		Class<?> m1Class = null;
		try {
			m1Class = getGraphClassImpl(graphClassName);
			if (className.equals(graphClassName)) {
				return m1Class.getMethod("create", signature);
			} else {
				return m1Class.getMethod("create" + className, signature);
			}
		} catch (SecurityException e) {
			throw new SchemaException("can't find create method in '"
					+ m1Class.getName() + "' for '" + className + "'", e);
		} catch (NoSuchMethodException e) {
			throw new SchemaException("can't find create method in '"
					+ m1Class.getName() + "' for '" + className + "'", e);
		}
	}

	static final Class<?>[] graphClassCreateSignature = { String.class, int.class, int.class };

	public Method getGraphCreateMethod(String graphClassName)
	{
		return getCreateMethod(graphClassName, graphClassName,
				graphClassCreateSignature);
	}

	static final Class<?>[] vertexClassCreateSignature = { int.class };

	public Method getVertexCreateMethod(String vertexClassName,
			String graphClassName)  {
		return getCreateMethod(vertexClassName, graphClassName,
				vertexClassCreateSignature);
	}

	public Method getEdgeCreateMethod(String edgeClassName,
			String graphClassName)  {

		// Edge class create method cannot be found directly by its signature
		// because the vertex parameters are subclassed to match the to- and
		// from-class. Those subclasses are unknown in this method. Therefore,
		// we look for a method with correct name and 3 parameters
		// (int, vertex, Vertex).
		String methodName = "create" + edgeClassName;
		Class<?> m1Class = getGraphClassImpl(graphClassName);
		for (Method m : m1Class.getMethods()) {
			if (m.getName().equals(methodName)
					&& m.getParameterTypes().length == 3) {
				return m;
			}
		}
		throw new SchemaException("can't find create method in '"
				+ m1Class.getName() + "' for '" + edgeClassName + "'");
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

	public boolean knows(String name) {
		if (getAttributedElementClass(name) != null)
			return true;
		if (domains.get(name) != null)
			return true;
		if (getGraphClass(name) != null)
			return true;
		return false;
	}

	public boolean isFreeDomainName(String name) {
		if (reservedJavaWords.contains(name))
			return false;
		if (knows(name))
			return false;
		return true;
	}

	public boolean isFreeSchemaElementName(String name) {
		if (!isAllowedSchemaElementName(name))
			return false;
		if (knows(name))
			return false;
		return true;
	}

	public boolean isAllowedSchemaElementName(String name) {
		if (reservedJavaWords.contains(name))
			return false;
		if (reservedDomainWords.contains(name))
			return false;
		return true;
	}

	public Attribute createAttribute(String name, Domain dom) {
		return new AttributeImpl(name, dom);
	}
	
	/**
	 * File Manager class overwriting the method {@code getJavaFileForOutput} so
	 * that bytecode is written to a {@code ClassFileAbstraction}.
	 *
	 */
	private class ClassFileManager extends ForwardingJavaFileManager<JavaFileManager> {
		Vector<JavaSourceFromString> sources;
		
		public ClassFileManager(JavaFileManager fm) {
			super(fm);
		}
		
		public JavaFileObject getJavaFileForOutput (Location location, String className,
				Kind kind, FileObject sibling) {
			ClassFileAbstraction cfa = new ClassFileAbstraction(className);
			
			M1ClassManager.instance().putM1Class(className, cfa);
		    return cfa;
		}
		
		public void setSources(Vector<JavaSourceFromString> sources) {
			this.sources = sources;
		}
	}
}