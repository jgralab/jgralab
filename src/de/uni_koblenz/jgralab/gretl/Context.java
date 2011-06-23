package de.uni_koblenz.jgralab.gretl;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import java.util.Set;
import java.util.TreeSet;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import de.uni_koblenz.jgralab.Graph;
import de.uni_koblenz.jgralab.GraphIOException;
import de.uni_koblenz.jgralab.ImplementationType;
import de.uni_koblenz.jgralab.JGraLab;
import de.uni_koblenz.jgralab.M1ClassManager;
import de.uni_koblenz.jgralab.codegenerator.CodeGeneratorConfiguration;
import de.uni_koblenz.jgralab.greql2.SerializableGreql2;
import de.uni_koblenz.jgralab.greql2.SerializableGreql2Impl;
import de.uni_koblenz.jgralab.greql2.evaluator.GreqlEvaluator;
import de.uni_koblenz.jgralab.greql2.exception.EvaluateException;
import de.uni_koblenz.jgralab.greql2.exception.Greql2Exception;
import de.uni_koblenz.jgralab.greql2.jvalue.JValue;
import de.uni_koblenz.jgralab.greql2.jvalue.JValueImpl;
import de.uni_koblenz.jgralab.greql2.jvalue.JValueMap;
import de.uni_koblenz.jgralab.greql2.jvalue.JValueRecord;
import de.uni_koblenz.jgralab.greql2.jvalue.JValueTuple;
import de.uni_koblenz.jgralab.greql2.jvalue.JValueXMLLoader;
import de.uni_koblenz.jgralab.greql2.jvalue.JValueXMLOutputVisitor;
import de.uni_koblenz.jgralab.greql2.schema.Greql2;
import de.uni_koblenz.jgralab.greql2.schema.Greql2Schema;
import de.uni_koblenz.jgralab.schema.AttributedElementClass;
import de.uni_koblenz.jgralab.schema.EnumDomain;
import de.uni_koblenz.jgralab.schema.GraphClass;
import de.uni_koblenz.jgralab.schema.Schema;
import de.uni_koblenz.jgralab.schema.exception.M1ClassAccessException;
import de.uni_koblenz.jgralab.schema.exception.SchemaException;
import de.uni_koblenz.jgralab.schema.impl.NamedElementImpl;
import de.uni_koblenz.jgralab.schema.impl.SchemaImpl;

/**
 * @author Tassilo Horn &lt;horn@uni-koblenz.de&gt;
 */
public class Context {
	static {
		Greql2Schema
				.instance()
				.getGraphFactory()
				.setGraphImplementationClass(Greql2.class,
						SerializableGreql2Impl.class);
	}

	private static Logger logger = JGraLab.getLogger(Context.class.getPackage()
			.getName());
	public static final String DEFAULT_SOURCE_GRAPH_ALIAS = "default";
	public static final String DEFAULT_TARGET_GRAPH_ALIAS = "target";

	private static final Pattern QUERY_GRAPH_ALIAS_PATTERN = Pattern.compile(
			"\\p{Space}*(#(\\p{Alnum}+)#\\p{Space}*).*", Pattern.DOTALL);

	private final Map<String, Graph> sourceGraphs = new HashMap<String, Graph>(
			1);
	private GreqlEvaluator eval = null;

	Schema targetSchema = null;

	private String targetSchemaCodeDirectory = null;

	/**
	 * This lets you set the directory where to commit the target schema code
	 * to. Normally, the code isn't committed at all but compiled in memory, but
	 * you can use this for debugging purposes.
	 * 
	 * The value <code>null</code> (default) means don't commit.
	 * 
	 * @param targetSchemaCodeDirectory
	 *            the targetSchemaCodeDirectory to set
	 */
	public void setTargetSchemaCodeDirectory(String targetSchemaCodeDirectory) {
		this.targetSchemaCodeDirectory = targetSchemaCodeDirectory;
	}

	/**
	 * @return the targetSchema
	 */
	public final Schema getTargetSchema() {
		return targetSchema;
	}

	Graph targetGraph = null;

	public enum TransformationPhase {
		SCHEMA, GRAPH
	}

	TransformationPhase phase = TransformationPhase.SCHEMA;

	/**
	 * @return the transformation phase
	 */
	public final TransformationPhase getPhase() {
		return phase;
	}

	private String targetSchemaName;
	private String targetGraphClassName;

	/**
	 * saves for nested transformations if the actual is the outermost one
	 */
	boolean outermost = true;

	/**
	 * Maps from {@link AttributedElementClass} to a map, mapping old elements
	 * to their images. (zeta-reverse)
	 * 
	 * Map<AttributedElementClass, Map<JValue archMap, JValue imgMap>>
	 */
	private JValueMap imgMap = new JValueMap();

	/**
	 * Maps from {@link AttributedElementClass} to a map, mapping new elements
	 * to the elements they were created for (their archetypes). (zeta)
	 * 
	 * Map<AttributedElementClass, Map<JValue imgMap, JValue archMap>>
	 */
	private JValueMap archMap = new JValueMap();

	/**
	 * Maps {@link AttributedElementClass} objects to their {@link JValue}
	 * encapsulation.
	 */
	private final Map<AttributedElementClass, JValue> attrElemClassMap = new HashMap<AttributedElementClass, JValue>();

	/**
	 * @param aec
	 * @return the given {@link AttributedElementClass} encapsulated in a
	 *         {@link JValue}
	 */
	private final JValue getAttrElemClassJValue(AttributedElementClass aec) {
		JValue jaec = attrElemClassMap.get(aec);
		if (jaec == null) {
			jaec = new JValueImpl(aec);
			attrElemClassMap.put(aec, jaec);
		}
		return jaec;
	}

	private final Map<String, JValue> greqlExtraVars = new HashMap<String, JValue>();
	private final Set<String> greqlImports = new HashSet<String>();

	final void setGReQLVariable(String name, JValue val) {
		greqlExtraVars.put(name, val);
	}

	final void setGReQLVariable(String name, String greqlExpression) {
		greqlExtraVars.put(name, evaluateGReQLQuery(greqlExpression));
	}

	final void setGReQLHelper(String name, String greqlExpression) {
		ensureGreqlEvaluator();
		eval.setSubQuery(name, greqlExpression);
	}

	final void addGReQLImport(String qualifiedName) {
		greqlImports.add(qualifiedName);
	}

	private final String getGreqlImportString(Graph graph) {
		StringBuilder sb = new StringBuilder();
		for (String s : greqlImports) {
			// don't import, if the element[s] don't exist in the graph to be
			// queried.
			if (s.endsWith(".*")) {
				if (graph.getSchema().getPackage(s.replace(".*", "")) == null) {
					continue;
				}
			} else {
				if (graph.getSchema().getAttributedElementClass(s) == null) {
					continue;
				}
			}

			// ok, we can use it, so do the import!
			sb.append("import ");
			sb.append(s);
			sb.append("; ");
		}
		return sb.toString();
	}

	/**
	 * Creates a new Context object
	 * 
	 * @param targetSchemaName
	 *            The name of the target schema
	 * @param targetGraphClassName
	 *            The name of the target graph class
	 */
	public Context(String targetSchemaName, String targetGraphClassName) {
		this.targetSchemaName = targetSchemaName;
		this.targetGraphClassName = targetGraphClassName;

		// Check if the target schema is already present and we can thus skip
		// the SCHEMA phase.
		try {
			Class<?> schemaClass = M1ClassManager.instance(targetSchemaName)
					.loadClass(targetSchemaName);
			Method schemaInstanceMethod = schemaClass.getMethod("instance");
			targetSchema = (Schema) schemaInstanceMethod.invoke(null);
		} catch (Exception e) {
			// Failing is ok here.
		}
		// Do it here, cause that takes some time. We don't want to have that
		// counted to the transformation time...
		ensureGreqlEvaluator();
	}

	/**
	 * Creates a new Context object with the given target schema. This is only
	 * useful if the target schema already exists and is set with the
	 * {@link #setTargetSchema(Schema)} method afterwards.
	 */
	public Context(Schema targetSchema) {
		this.targetSchema = targetSchema;
		this.targetSchemaName = targetSchema.getQualifiedName();
		// Do it here, cause that takes some time. We don't want to have that
		// counted to the transformation time...
		ensureGreqlEvaluator();

	}

	/**
	 * Creates a new Context object with the given graph set as source and
	 * target. So this is useful only for in-place transforms.
	 */
	public Context(Graph g) {
		setTargetGraph(g);
		setSourceGraph(g);
		// Do it here, cause that takes some time. We don't want to have that
		// counted to the transformation time...
		ensureGreqlEvaluator();
	}

	/**
	 * @param aec
	 *            the AttributedElementClass for which to get the archMap
	 *            mappings
	 * @return a map from target graph elements (images) to their archetypes
	 */
	public final JValueMap getArch(AttributedElementClass aec) {
		JValue result = archMap.get(getAttrElemClassJValue(aec));
		if (result == null) {
			result = new JValueMap();
			archMap.put(getAttrElemClassJValue(aec), result);
		}
		return result.toJValueMap();
	}

	/**
	 * @param aec
	 *            the AttributedElementClass for which to get the archMap
	 *            mappings
	 * @return a map from archetypes to target graph elements, which are their
	 *         images
	 */
	public final JValueMap getImg(AttributedElementClass aec) {
		JValue result = imgMap.get(getAttrElemClassJValue(aec));
		if (result == null) {
			result = new JValueMap();
			imgMap.put(getAttrElemClassJValue(aec), result);
		}
		return result.toJValueMap();
	}

	/**
	 * Ensures that theres a function for this attributed element class, even
	 * though this function may be empty.
	 * 
	 * @param aec
	 *            the AttributedElementClass for which to ensure the
	 *            archMap/imgMap mappings
	 */
	final void ensureMappings(AttributedElementClass aec) {
		getImg(aec);
		getArch(aec);
		// validateMappings();
	}

	/**
	 * Ensures that theres a function for all attributed elements in the target
	 * schema.
	 */
	public final void ensureAllMappings() {
		ensureMappings(targetSchema.getGraphClass());
		for (AttributedElementClass gec : targetSchema.getGraphClass()
				.getGraphElementClasses()) {
			ensureMappings(gec);
		}
	}

	public final void printImgMappings() {
		System.out.println("Image Mappings:");
		for (Entry<JValue, JValue> e : imgMap.entrySet()) {
			AttributedElementClass aec = e.getKey().toAttributedElementClass();
			JValueMap img = e.getValue().toJValueMap();
			if (aec.isInternal()) {
				continue;
			}
			System.out.println("Mappings for: " + aec.getQualifiedName());
			for (Entry<JValue, JValue> entry : img.entrySet()) {
				System.out.println("    " + entry.getKey() + "("
						+ entry.getKey().getType() + ")" + " ==> "
						+ entry.getValue() + "(" + entry.getValue().getType()
						+ ")");
			}
		}
	}

	final void addMapping(AttributedElementClass attrElemClass,
			JValue archetype, JValue image) {
		addMappingToClass(attrElemClass, archetype, image);
		addMappingsToSuperClasses(attrElemClass, archetype, image);
	}

	private final void addMappingsToSuperClasses(
			final AttributedElementClass subClass, final JValue archetype,
			final JValue image) {
		for (AttributedElementClass superClass : subClass.getAllSuperClasses()) {
			if (superClass.isInternal()) {
				continue;
			}
			addMappingToClass(superClass, archetype, image);
		}
	}

	private final void addMappingToClass(AttributedElementClass attrElemClass,
			JValue archetype, JValue image) {
		addArchMapping(attrElemClass, image, archetype);
		addImgMapping(attrElemClass, archetype, image);
	}

	private void addArchMapping(AttributedElementClass attrElemClass,
			JValue image, JValue archetype) {
		JValueMap map = archMap.get(getAttrElemClassJValue(attrElemClass))
				.toJValueMap();
		if (map.containsKey(image)) {
			throw new GReTLBijectionViolationException(this, "'"
					+ image
					+ "' already maps to '"
					+ map.get(image)
					+ "' in "
					+ toGReTLVarNotation(attrElemClass.getQualifiedName(),
							GReTLVariableType.ARCH)
					+ "! You wanted to create an archMap mapping from '"
					+ image + "' to '" + archetype + "'.");
		}

		// everything is fine
		map.put(image, archetype);
	}

	private void addImgMapping(AttributedElementClass attrElemClass,
			JValue archetype, JValue image) {
		JValueMap map = imgMap.get(getAttrElemClassJValue(attrElemClass))
				.toJValueMap();
		if (map.containsKey(archetype)) {
			throw new GReTLBijectionViolationException(this, "'"
					+ archetype
					+ "' already maps to '"
					+ map.get(archetype)
					+ "' in "
					+ toGReTLVarNotation(attrElemClass.getQualifiedName(),
							GReTLVariableType.IMG)
					+ "! You wanted to create an imgMap mapping from '"
					+ archetype + "' to '" + image + "'.");
		}

		// everything is fine
		map.put(archetype, image);
	}

	private Random uniqueSeed = new Random();

	/**
	 * @return a JValue that is guaranteed to be unique
	 */
	public JValue getUniqueJValue() {
		StringBuilder sb = new StringBuilder();
		sb.append("<--[");
		sb.append(uniqueSeed.nextInt());
		sb.append("]--[");
		sb.append(System.currentTimeMillis());
		sb.append("]-->");
		return new JValueImpl(sb.toString());
	}

	public final void validateMappings() {
		if (imgMap.size() != archMap.size()) {
			JValueMap m = imgMap;
			JValueMap o = archMap;
			if (archMap.size() > imgMap.size()) {
				m = archMap;
				o = imgMap;
			}
			for (JValue jaec : m.keySet()) {
				if (!o.containsKey(jaec)) {
					System.err.println(toGReTLVarNotation(jaec
							.toAttributedElementClass().getQualifiedName(),
							(o == archMap) ? GReTLVariableType.ARCH
									: GReTLVariableType.IMG)
							+ " is missing");
				}
			}
			throw new GReTLBijectionViolationException(this,
					"The imgMap and archMap mappings aren't valid! "
							+ "The sizes of imgMap (" + imgMap.size()
							+ ") and archMap (" + archMap.size()
							+ ") don't match!");
		}
		for (JValue jaec : archMap.keySet()) {
			AttributedElementClass aec = jaec.toAttributedElementClass();
			if (!imgMap.containsKey(jaec)) {
				throw new GReTLBijectionViolationException(this,
						"The imgMap and archMap mappings aren't valid! "
								+ "imgMap contains no mappings for '"
								+ aec.getQualifiedName() + "'!");
			}
			JValueMap arch = archMap.get(jaec).toJValueMap();
			JValueMap img = imgMap.get(jaec).toJValueMap();

			if (arch.size() != img.size()) {
				throw new GReTLBijectionViolationException(this,
						"The imgMap and archMap mappings aren't valid! "
								+ "The sizes of '"
								+ toGReTLVarNotation(aec.getQualifiedName(),
										GReTLVariableType.IMG)
								+ "' and '"
								+ toGReTLVarNotation(aec.getQualifiedName(),
										GReTLVariableType.ARCH)
								+ "' don't match!");
			}

			for (Entry<JValue, JValue> entry : arch.entrySet()) {
				if (!img.containsKey(entry.getValue())) {
					throw new GReTLBijectionViolationException(this,
							"The imgMap and archMap mappings aren't valid! "
									+ "imgMap contains no mapping for '"
									+ entry.getValue() + "'!");
				}
				if (!img.get(entry.getValue()).equals(entry.getKey())) {
					throw new GReTLBijectionViolationException(this,
							"The imgMap and archMap mappings aren't valid! "
									+ "imgMap is not inverse to archMap for '"
									+ toGReTLVarNotation(
											aec.getQualifiedName(),
											GReTLVariableType.ARCH) + "'! "
									+ "archMap: " + entry.getKey() + " --> "
									+ entry.getValue() + ", but imgMap: "
									+ entry.getValue() + " --> "
									+ img.get(entry.getValue()));
				}
			}
		}
	}

	/**
	 * Swap this context object. E.g. make the current target graph the default
	 * source graph and reinitialize all member vars such as archMap/imgMap.
	 * This mainly useful for sequencing transformations like
	 * {@link TransformationChain} does.
	 * 
	 * @return this context object itself
	 */
	public final Context swap() {
		logger.info("Swapping context...");
		logger.info("Old target schema name: "
				+ targetSchema.getQualifiedName());

		// forget the old source graphs
		sourceGraphs.clear();

		// make the target graph the default source graph and increase version
		// number
		Graph oldTargetGraph = targetGraph;
		String newSchemaPackagePrefix = targetSchema.getPackagePrefix();
		Pattern stepSuffix = Pattern.compile(".*(_v(\\d+))$");
		Matcher matcher = stepSuffix.matcher(newSchemaPackagePrefix);
		int step = 1;
		if (matcher.matches()) {
			String matchGroup = matcher.group(2);
			step = Integer.parseInt(matchGroup);
			newSchemaPackagePrefix = newSchemaPackagePrefix
					.substring(0, newSchemaPackagePrefix.length()
							- matcher.group(1).length());
		}
		step++;
		newSchemaPackagePrefix += "_v" + step;
		targetSchemaName = newSchemaPackagePrefix + "."
				+ targetSchema.getName();
		logger.info("New target schema name: " + targetSchemaName);
		setSourceGraph(oldTargetGraph);

		return reset(true);
	}

	/**
	 * Reset this context, so that the same context can be passed to another
	 * transformation. This means, everything except the source graph is
	 * cleared.
	 * 
	 * @return the context
	 */
	public final Context reset(boolean forgetTargetSchema) {
		// reinitialize outermost/phase
		outermost = true;
		phase = TransformationPhase.SCHEMA;

		// forget target graph and schema
		targetGraph = null;
		if (forgetTargetSchema) {
			targetSchema = null;
		}

		// clear archMap/imgMap
		archMap.clear();
		imgMap.clear();

		// reset the GreqlEvaluator index cache, they prevent garbage
		// collection!
		GreqlEvaluator.resetGraphIndizes();
		GreqlEvaluator.resetOptimizedSyntaxGraphs();

		// clear imports/extra vars
		greqlExtraVars.clear();
		greqlImports.clear();

		return this;
	}

	/**
	 * Sets the (default) source graph for the transformation
	 * 
	 * @param sourceGraph
	 *            the source graph
	 */
	public final void setSourceGraph(Graph sourceGraph) {
		addSourceGraph(DEFAULT_SOURCE_GRAPH_ALIAS, sourceGraph);
	}

	/**
	 * adds a source graph for the transformation
	 * 
	 * @param alias
	 *            the alias to access this source graph (used as prefix #name#
	 *            in semantic expressions)
	 * @param sourceGraph
	 *            the source graph
	 */
	public final void addSourceGraph(String alias, Graph sourceGraph) {
		if (sourceGraphs.containsKey(alias)) {
			throw new GReTLException(this,
					"There's already a source graph with name '" + alias + "'.");
		}

		if (!alias.matches("\\w+")) {
			throw new GReTLException(this, "Invalid source graph name '"
					+ alias + "'. Must be only word characters.");
		}

		if (alias.equals(DEFAULT_TARGET_GRAPH_ALIAS)) {
			throw new GReTLException(this, "Invalid source graph name '"
					+ alias
					+ "'. That's the default alias for the target graph.");
		}

		// If the target schema is already there, then it is of course ok to
		// transform to that existing schema.
		if (sourceGraph.getSchema().getQualifiedName().equals(targetSchemaName)
				&& (targetSchema == null)) {
			throw new SchemaException(
					"The schema names of source and target have to be different. "
							+ "Currently both are named '" + targetSchemaName
							+ "'.");
		}

		sourceGraphs.put(alias, sourceGraph);
	}

	/**
	 * @return a map, mapping aliases to source graphs
	 */
	public final Map<String, Graph> getSourceGraphs() {
		return sourceGraphs;
	}

	public final Graph getSourceGraph() {
		return getSourceGraph(DEFAULT_SOURCE_GRAPH_ALIAS);
	}

	public final Graph getSourceGraph(String alias) {
		return sourceGraphs.get(alias);
	}

	/**
	 * returns the target graph of the transformation if no target graph exists,
	 * it will be created
	 * 
	 * @return the target graph
	 */
	public final Graph getTargetGraph() {
		return targetGraph;
	}

	/**
	 * @param targetGraph
	 *            the targetGraph to set
	 */
	public final void setTargetGraph(Graph targetGraph) {
		this.targetGraph = targetGraph;
		targetSchema = targetGraph.getSchema();
	}

	/**
	 * creates a blank target Schema
	 */
	final void createTargetSchema() {
		String[] qn = SchemaImpl.splitQualifiedName(targetSchemaName);
		targetSchema = new SchemaImpl(qn[1], qn[0]);
		GraphClass gc = targetSchema.createGraphClass(targetGraphClassName);
		ensureMappings(gc);
	}

	/**
	 * creates the target graph from the target schema
	 */
	final void createTargetGraph() {
		boolean targetSchemaIsCompiled = true;
		try {
			targetSchema.getGraphClass().getM1Class();
		} catch (M1ClassAccessException e) {
			targetSchemaIsCompiled = false;
		}

		if (!targetSchemaIsCompiled) {
			if (targetSchemaCodeDirectory != null) {
				try {
					targetSchema.commit(targetSchemaCodeDirectory,
							CodeGeneratorConfiguration.MINIMAL);
				} catch (GraphIOException e1) {
					e1.printStackTrace();
				}
			}
			logger.info("Compiling schema '" + targetSchema.getQualifiedName()
					+ "'...");
			targetSchema.compile(CodeGeneratorConfiguration.MINIMAL);
		} else {
			logger.info("Schema '" + targetSchema.getQualifiedName()
					+ "' is already compiled or in the CLASSPATH...");
		}

		Method graphCreateMethod = targetSchema
				.getGraphCreateMethod(ImplementationType.STANDARD);

		try {
			targetGraph = (Graph) graphCreateMethod
					.invoke(null, null, 500, 500);
			targetSchema = targetGraph.getSchema();
		} catch (Exception e) {
			e.printStackTrace();
			throw new GReTLException(this,
					"Something failed when creating the target graph!", e);
		}

		for (Entry<String, Graph> e : sourceGraphs.entrySet()) {
			String sourceGraphName = e.getKey();
			JValue jsourceGraphName = new JValueImpl(sourceGraphName);
			Graph sourceGraph = e.getValue();
			JValue jsourceGraph = new JValueImpl(sourceGraph);
			JValue jtargetGraph = new JValueImpl(targetGraph);

			if (sourceGraphName.equals(DEFAULT_SOURCE_GRAPH_ALIAS)) {
				// The default source graph is the archetype of the new target
				// graph.
				addMapping(targetGraph.getGraphClass(), jsourceGraph,
						jtargetGraph);
			} else {
				// Secondary source graphs are related to the target graph as
				// tuples with the mapping: (sourceGraphName, sourceGraph) -->
				// (sourceGraphName, targetGraph).
				JValueTuple sourceTup = new JValueTuple(2);
				sourceTup.add(jsourceGraphName);
				sourceTup.add(jsourceGraph);
				JValueTuple targetTup = new JValueTuple(2);
				targetTup.add(jsourceGraphName);
				targetTup.add(jtargetGraph);
				addMapping(targetGraph.getGraphClass(), sourceTup, targetTup);
			}
		}
	}

	void initializeEnumValue2LiteralMaps() {
		// Make enum constants accessible via maps of the form enum_QName:
		// String -> Object
		for (EnumDomain d : targetGraph.getSchema().getEnumDomains()) {
			try {
				Class<?> myEnum = Class.forName(targetSchema.getPackagePrefix()
						+ "." + d.getQualifiedName(), false, targetSchema
						.getClass().getClassLoader());

				Method valueOf = myEnum.getMethod("valueOf",
						new Class<?>[] { String.class });
				JValueMap map = new JValueMap(d.getConsts().size());
				for (String c : d.getConsts()) {
					Object constant = valueOf.invoke(null, new Object[] { c });
					map.put(new JValueImpl(c), new JValueImpl(constant));
				}
				setGReQLVariable(
						toGReTLVarNotation(d.getQualifiedName(),
								GReTLVariableType.ENUM), map);
			} catch (ClassNotFoundException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			} catch (SecurityException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (NoSuchMethodException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IllegalArgumentException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (InvocationTargetException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	public enum GReTLVariableType {
		ARCH, IMG, ENUM
	}

	public static String toGReTLVarNotation(String qualifiedName,
			GReTLVariableType type) {
		String qName = NamedElementImpl.toUniqueNameNotation(qualifiedName);
		return type.toString().toLowerCase() + "_" + qName;
	}

	public final JValue evaluateGReQLQuery(String greqlExpression) {
		if (phase == TransformationPhase.SCHEMA) {
			return null;
		}

		String name = DEFAULT_SOURCE_GRAPH_ALIAS;
		// System.out.println("greqlExp = '" + greqlExpression + "'.");
		Matcher m = QUERY_GRAPH_ALIAS_PATTERN.matcher(greqlExpression);
		if (m.matches()) {
			greqlExpression = greqlExpression.replace(m.group(1), "");
			name = m.group(2);

			if (name.equals(DEFAULT_TARGET_GRAPH_ALIAS)) {
				return evalGReQLQuery(greqlExpression, targetGraph);
			}

			if (!sourceGraphs.containsKey(name)) {
				throw new GReTLException(this, "No source graph with name '"
						+ name + "'.");
			}
		}

		return evalGReQLQuery(greqlExpression, sourceGraphs.get(name));
	}

	private final JValue evalGReQLQuery(String semanticExpression, Graph graph) {
		if (phase == TransformationPhase.SCHEMA) {
			return null;
		}

		if (semanticExpression.isEmpty()) {
			logger.severe("The given semantic expression is empty!  Fix that!");
			return null;
		}

		Map<String, JValue> greqlMapping = getGreqlVariablesNeededByQuery(semanticExpression);
		StringBuilder sb = new StringBuilder();

		if (sourceGraphs.values().contains(graph)) {
			sb.append(getGreqlImportString(graph));
		}

		sb.append(getGreqlUsingString(greqlMapping));
		sb.append(semanticExpression);
		String query = sb.toString();
		logger.finest("GReQL: " + semanticExpression);

		ensureGreqlEvaluator();
		eval.setDatagraph(graph);
		eval.setQuery(query);
		eval.setVariables(greqlMapping);

		// eval.setOptimize(false);
		JValue result = null;
		try {
			eval.startEvaluation();
			result = eval.getEvaluationResult();
		} catch (EvaluateException e) {
			if (eval.getSyntaxGraph() != null) {
				logger.severe("Evaluated query was:\n"
						+ ((SerializableGreql2) eval.getSyntaxGraph())
								.serialize());
			} else {
				logger.severe("No syntax graph...");
			}
			throw new GReTLException(this, "GReQL evaluation failed.  Query: "
					+ query, e);
		}

		// log.fine("GReQL result: " + result);
		return result;
	}

	/**
	 * Ensure that the {@link GreqlEvaluator} <code>eval</code> exists.
	 */
	private void ensureGreqlEvaluator() {
		if (eval == null) {
			eval = new GreqlEvaluator((String) null, null, null);
		}
	}

	private final Map<String, JValue> getGreqlVariablesNeededByQuery(
			String query) {
		HashMap<String, JValue> result = new HashMap<String, JValue>();

		for (String extraVar : greqlExtraVars.keySet()) {
			if (query.contains(extraVar)) {
				result.put(extraVar, greqlExtraVars.get(extraVar));
			}
		}

		for (Entry<JValue, JValue> e : archMap.entrySet()) {
			String varName = toGReTLVarNotation(e.getKey()
					.toAttributedElementClass().getQualifiedName(),
					GReTLVariableType.ARCH);
			if (query.contains(varName)) {
				result.put(varName, e.getValue());
			}
		}

		for (Entry<JValue, JValue> e : imgMap.entrySet()) {
			String varName = toGReTLVarNotation(e.getKey()
					.toAttributedElementClass().getQualifiedName(),
					GReTLVariableType.IMG);
			if (query.contains(varName)) {
				result.put(varName, e.getValue());
			}
		}

		return result;
	}

	private final String getGreqlUsingString(Map<String, JValue> greqlMapping) {
		if (greqlMapping.size() == 0) {
			return "";
		}
		StringBuilder sb = new StringBuilder();
		sb.append("using ");
		boolean first = true;
		for (String name : new TreeSet<String>(greqlMapping.keySet())) {
			if (first) {
				first = false;
			} else {
				sb.append(", ");
			}
			sb.append(name);
		}
		sb.append(": ");
		return sb.toString();
	}

	public final void storeTrace(String fileName) {
		try {
			JValueRecord traceRec = new JValueRecord();
			traceRec.add("archMap", archMap);
			traceRec.add("imgMap", imgMap);
			new JValueXMLOutputVisitor(traceRec, fileName);
		} catch (Exception e) {
			e.printStackTrace();
			throw new Greql2Exception("Exception while storing trace file '"
					+ fileName + "'.", e);
		}
	}

	public final void restoreTrace(String fileName) {
		Graph[] graphs = new Graph[getSourceGraphs().values().size() + 1];
		int i = 0;
		for (Graph g : getSourceGraphs().values()) {
			graphs[i] = g;
			i++;
		}
		graphs[i] = targetGraph;
		JValueXMLLoader l = new JValueXMLLoader(graphs);

		try {
			JValueRecord rec = l.load(fileName).toJValueRecord();
			archMap = rec.get("archMap").toJValueMap();
			imgMap = rec.get("imgMap").toJValueMap();
		} catch (Exception e) {
			e.printStackTrace();
			throw new GReTLException("Couldn't load GReTLTrace", e);
		}
	}

}
