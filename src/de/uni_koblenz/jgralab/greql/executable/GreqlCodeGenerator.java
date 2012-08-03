package de.uni_koblenz.jgralab.greql.executable;

import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.Vector;

import javax.tools.JavaCompiler;
import javax.tools.StandardJavaFileManager;
import javax.tools.ToolProvider;

import de.uni_koblenz.jgralab.EdgeDirection;
import de.uni_koblenz.jgralab.GraphIOException;
import de.uni_koblenz.jgralab.ImplementationType;
import de.uni_koblenz.jgralab.Vertex;
import de.uni_koblenz.jgralab.codegenerator.CodeBlock;
import de.uni_koblenz.jgralab.codegenerator.CodeGenerator;
import de.uni_koblenz.jgralab.codegenerator.CodeGeneratorConfiguration;
import de.uni_koblenz.jgralab.codegenerator.CodeList;
import de.uni_koblenz.jgralab.codegenerator.CodeSnippet;
import de.uni_koblenz.jgralab.greql.GreqlQuery;
import de.uni_koblenz.jgralab.greql.evaluator.GraphSize;
import de.uni_koblenz.jgralab.greql.evaluator.GreqlEnvironmentAdapter;
import de.uni_koblenz.jgralab.greql.evaluator.GreqlEvaluatorImpl;
import de.uni_koblenz.jgralab.greql.evaluator.GreqlQueryImpl;
import de.uni_koblenz.jgralab.greql.evaluator.InternalGreqlEvaluator;
import de.uni_koblenz.jgralab.greql.evaluator.fa.AggregationTransition;
import de.uni_koblenz.jgralab.greql.evaluator.fa.BoolExpressionTransition;
import de.uni_koblenz.jgralab.greql.evaluator.fa.DFA;
import de.uni_koblenz.jgralab.greql.evaluator.fa.EdgeTransition;
import de.uni_koblenz.jgralab.greql.evaluator.fa.IntermediateVertexTransition;
import de.uni_koblenz.jgralab.greql.evaluator.fa.NFA;
import de.uni_koblenz.jgralab.greql.evaluator.fa.SimpleTransition;
import de.uni_koblenz.jgralab.greql.evaluator.fa.State;
import de.uni_koblenz.jgralab.greql.evaluator.fa.Transition;
import de.uni_koblenz.jgralab.greql.evaluator.fa.VertexTypeRestrictionTransition;
import de.uni_koblenz.jgralab.greql.evaluator.vertexeval.PathDescriptionEvaluator;
import de.uni_koblenz.jgralab.greql.evaluator.vertexeval.VariableEvaluator;
import de.uni_koblenz.jgralab.greql.evaluator.vertexeval.VertexEvaluator;
import de.uni_koblenz.jgralab.greql.funlib.FunLib;
import de.uni_koblenz.jgralab.greql.funlib.Function;
import de.uni_koblenz.jgralab.greql.schema.BackwardVertexSet;
import de.uni_koblenz.jgralab.greql.schema.BoolLiteral;
import de.uni_koblenz.jgralab.greql.schema.Comprehension;
import de.uni_koblenz.jgralab.greql.schema.ConditionalExpression;
import de.uni_koblenz.jgralab.greql.schema.Declaration;
import de.uni_koblenz.jgralab.greql.schema.DoubleLiteral;
import de.uni_koblenz.jgralab.greql.schema.EdgeRestriction;
import de.uni_koblenz.jgralab.greql.schema.EdgeSetExpression;
import de.uni_koblenz.jgralab.greql.schema.Expression;
import de.uni_koblenz.jgralab.greql.schema.ForwardVertexSet;
import de.uni_koblenz.jgralab.greql.schema.FunctionApplication;
import de.uni_koblenz.jgralab.greql.schema.FunctionId;
import de.uni_koblenz.jgralab.greql.schema.GReQLDirection;
import de.uni_koblenz.jgralab.greql.schema.GreqlExpression;
import de.uni_koblenz.jgralab.greql.schema.GreqlGraph;
import de.uni_koblenz.jgralab.greql.schema.GreqlVertex;
import de.uni_koblenz.jgralab.greql.schema.Identifier;
import de.uni_koblenz.jgralab.greql.schema.IntLiteral;
import de.uni_koblenz.jgralab.greql.schema.IsArgumentOf;
import de.uni_koblenz.jgralab.greql.schema.IsBoundVarOf;
import de.uni_koblenz.jgralab.greql.schema.IsConstraintOf;
import de.uni_koblenz.jgralab.greql.schema.IsDeclaredVarOf;
import de.uni_koblenz.jgralab.greql.schema.IsIdOfStoreClause;
import de.uni_koblenz.jgralab.greql.schema.IsPartOf;
import de.uni_koblenz.jgralab.greql.schema.IsQueryExprOf;
import de.uni_koblenz.jgralab.greql.schema.IsRecordElementOf;
import de.uni_koblenz.jgralab.greql.schema.IsSimpleDeclOf;
import de.uni_koblenz.jgralab.greql.schema.IsTypeRestrOfExpression;
import de.uni_koblenz.jgralab.greql.schema.ListComprehension;
import de.uni_koblenz.jgralab.greql.schema.ListConstruction;
import de.uni_koblenz.jgralab.greql.schema.ListRangeConstruction;
import de.uni_koblenz.jgralab.greql.schema.Literal;
import de.uni_koblenz.jgralab.greql.schema.LongLiteral;
import de.uni_koblenz.jgralab.greql.schema.MapComprehension;
import de.uni_koblenz.jgralab.greql.schema.PathDescription;
import de.uni_koblenz.jgralab.greql.schema.QuantifiedExpression;
import de.uni_koblenz.jgralab.greql.schema.Quantifier;
import de.uni_koblenz.jgralab.greql.schema.RecordConstruction;
import de.uni_koblenz.jgralab.greql.schema.RecordElement;
import de.uni_koblenz.jgralab.greql.schema.SetComprehension;
import de.uni_koblenz.jgralab.greql.schema.SetConstruction;
import de.uni_koblenz.jgralab.greql.schema.SimpleDeclaration;
import de.uni_koblenz.jgralab.greql.schema.StringLiteral;
import de.uni_koblenz.jgralab.greql.schema.ThisEdge;
import de.uni_koblenz.jgralab.greql.schema.ThisLiteral;
import de.uni_koblenz.jgralab.greql.schema.ThisVertex;
import de.uni_koblenz.jgralab.greql.schema.TupleConstruction;
import de.uni_koblenz.jgralab.greql.schema.TypeId;
import de.uni_koblenz.jgralab.greql.schema.Variable;
import de.uni_koblenz.jgralab.greql.schema.VertexSetExpression;
import de.uni_koblenz.jgralab.greql.serialising.GreqlSerializer;
import de.uni_koblenz.jgralab.greql.types.TypeCollection;
import de.uni_koblenz.jgralab.schema.EdgeClass;
import de.uni_koblenz.jgralab.schema.GraphElementClass;
import de.uni_koblenz.jgralab.schema.IncidenceClass;
import de.uni_koblenz.jgralab.schema.Schema;
import de.uni_koblenz.jgralab.schema.exception.SchemaException;
import de.uni_koblenz.jgralab.schema.impl.compilation.ClassFileManager;
import de.uni_koblenz.jgralab.schema.impl.compilation.InMemoryJavaSourceFile;
import de.uni_koblenz.jgralab.schema.impl.compilation.ManagableArtifact;
import de.uni_koblenz.jgralab.schema.impl.compilation.SchemaClassManager;

public class GreqlCodeGenerator extends CodeGenerator implements
		ManagableArtifact {

	public static final String codeGeneratorFileManagerName = "GeneratedQueries";

	private final GreqlGraph graph;

	private final String classname;

	private final String packageName;

	private final CodeSnippet classFieldSnippet = new CodeSnippet();

	private final CodeSnippet staticFieldSnippet = new CodeSnippet();

	private final CodeSnippet staticInitializerSnippet = new CodeSnippet(
			"static {");

	private final List<CodeBlock> createdMethods = new LinkedList<CodeBlock>();

	private final Scope scope;

	private final Schema schema;

	private boolean thisLiteralsCreated = false;

	private final GreqlQuery query;

	private final InternalGreqlEvaluator evaluator;

	private final HashSet<String> resultVariables = new HashSet<String>();

	public GreqlCodeGenerator(GreqlQuery query, Schema datagraphSchema,
			String packageName, String classname) {
		super(packageName, "",
				CodeGeneratorConfiguration.WITHOUT_TYPESPECIFIC_METHODS);
		graph = query.getQueryGraph();
		this.query = query;
		this.classname = classname;
		this.packageName = packageName;
		this.schema = datagraphSchema;
		scope = new Scope();
		evaluator = new GreqlEvaluatorImpl(query, null,
				new GreqlEnvironmentAdapter());
		evaluator.setDatagraphSchema(datagraphSchema);
	}

	/**
	 * Generates a Java class implementing a GReQL query. The generated class
	 * will implement the interface {@link ExecutableQuery}, thus it may be
	 * evaluated simply by calling its execute-Method. For proper execution, the
	 * GReQL function library needs to be available.
	 * 
	 * @param queryString
	 *            the query in its String representation
	 * @param datagraphSchema
	 *            the graph schema of the graph the query should be executed on
	 * @param classname
	 *            the fully qualified name of the class to be generated
	 * @param path
	 *            the path where the generated file should be stored. Beware,
	 *            the query will be stored inside a subdirectory of the path
	 *            defined by its qualified name <code>classname</code>
	 */
	public static void generateCode(String queryString, Schema datagraphSchema,
			String classname, String path) {
		GreqlQuery query = GreqlQuery.createQuery(
				queryString,
				true,
				new GraphSize(datagraphSchema
						.createGraph(ImplementationType.GENERIC)));

		String simpleName = classname;
		String packageName = "";
		if (classname.contains(".")) {
			simpleName = classname.substring(classname.lastIndexOf(".") + 1);
			packageName = classname.substring(0, classname.lastIndexOf("."));
		}
		GreqlCodeGenerator greqlcodeGen = new GreqlCodeGenerator(query,
				datagraphSchema, packageName, simpleName);
		try {
			greqlcodeGen.createFiles(path);
		} catch (GraphIOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Generates and compiles an in-memory Java class implementing a GReQL
	 * query. The generated class will implement the interface
	 * {@link ExecutableQuery}, thus it may be evaluated simply by calling its
	 * execute-method. For proper execution, the GReQL function library needs to
	 * be available. After calling this method, the compiled class
	 * <code>classname</code> is available to your environment and may be
	 * instantiated using reflection, e.g., by
	 * Class.forName(className).newInstance().
	 * 
	 * @param queryString
	 *            the query in its String representation
	 * @param datagraphSchema
	 *            the graph schema of the graph the query should be executed on
	 * @param classname
	 *            the fully qualified name of the class to be generated
	 */
	public static Class<ExecutableQuery> generateCode(String queryString,
			Schema datagraphSchema, String classname) {
		GreqlQuery query = GreqlQuery.createQuery(
				queryString,
				true,
				new GraphSize(datagraphSchema
						.createGraph(ImplementationType.GENERIC)));

		String simpleName = classname;
		String packageName = "";
		if (classname.contains(".")) {
			simpleName = classname.substring(classname.lastIndexOf(".") + 1);
			packageName = classname.substring(0, classname.lastIndexOf("."));
		}
		GreqlCodeGenerator greqlcodeGen = new GreqlCodeGenerator(query,
				datagraphSchema, packageName, simpleName);
		return greqlcodeGen.compile();
	}

	@Override
	public CodeBlock createBody() {
		CodeList code = new CodeList();

		addImports("de.uni_koblenz.jgralab.greql.executable.*");
		addImports("de.uni_koblenz.jgralab.Graph");
		addImports("de.uni_koblenz.jgralab.greql.GreqlEnvironment");
		addImports("java.util.Set");
		code.add(staticFieldSnippet);
		code.add(staticInitializerSnippet);
		code.add(classFieldSnippet);
		GreqlExpression rootExpr = graph.getFirstGreqlExpression();
		addClassField("Graph", "datagraph", "null");
		addClassField("GreqlEnvironment", "boundVariables", "null");

		CodeList mExec = new CodeList();
		CodeSnippet methodExecute = new CodeSnippet();
		methodExecute
				.add("public synchronized Object execute(de.uni_koblenz.jgralab.Graph graph, GreqlEnvironment boundVariables) {");
		methodExecute.add("\tthis.datagraph = graph;");
		methodExecute.add("\tthis.boundVariables = boundVariables;");
		mExec.addNoIndent(methodExecute);
		CodeSnippet clearVars = new CodeSnippet();
		mExec.add(clearVars);
		methodExecute = new CodeSnippet();
		methodExecute.add("\treturn " + createCodeForGreqlExpression(rootExpr)
				+ ";");
		for (String var : resultVariables) {
			clearVars.add(var + " = null;");
		}
		methodExecute.add("}");
		mExec.addNoIndent(methodExecute);
		code.add(mExec);

		CodeSnippet methodGetQueryText = new CodeSnippet();
		methodGetQueryText.add("\n\tpublic String getQueryText() {");
		methodGetQueryText.add("\treturn \"" + quote(query.getQueryText())
				+ "\";");
		methodGetQueryText.add("}");
		code.add(methodGetQueryText);

		CodeSnippet methodGetUsedVariables = new CodeSnippet();
		methodGetUsedVariables
				.add("\n\tpublic Set<String> getUsedVariables() {");
		methodGetUsedVariables
				.add("\tjava.util.Set<String> usedVariables = new java.util.HashSet<String>();");
		for (String usedVariable : query.getUsedVariables()) {
			methodGetUsedVariables.add("\tusedVariables.add(\"" + usedVariable
					+ "\");");
		}
		methodGetUsedVariables.add("\treturn usedVariables;");
		methodGetUsedVariables.add("}");
		code.add(methodGetUsedVariables);

		CodeSnippet methodGetStoredVariables = new CodeSnippet();
		methodGetStoredVariables
				.add("\n\tpublic Set<String> getStoredVariables() {");
		methodGetStoredVariables
				.add("\tjava.util.Set<String> storedVariables = new java.util.HashSet<String>();");
		for (String usedVariable : query.getStoredVariables()) {
			methodGetStoredVariables.add("\tstoredVariables.add(\""
					+ usedVariable + "\");");
		}
		methodGetStoredVariables.add("\treturn storedVariables;");
		methodGetStoredVariables.add("}");
		code.add(methodGetStoredVariables);

		// add generated methods
		code.add(new CodeSnippet("", ""));
		for (CodeBlock methodBlock : createdMethods) {
			code.addNoIndent(methodBlock);
			code.add(new CodeSnippet("", ""));
		}
		staticInitializerSnippet.add("}");
		return code;
	}

	private String quote(String queryText) {
		StringBuilder appendable = new StringBuilder();
		for (char c : queryText.toCharArray()) {
			switch (c) {
			case '\n':
				appendable.append("\\\\n");
				break;
			case '\r':
				appendable.append("\\\\r");
				break;
			case '\t':
				appendable.append("\\\\t");
				break;
			case '\\':
				appendable.append("\\\\");
				break;
			case '"':
				appendable.append("\\\"");
				break;
			default:
				if (String.valueOf(c).matches("^[\\p{Cntrl}]$")) {
					appendable.append("\\\\" + (int) c);
				} else {
					appendable.append(c);
				}
			}
		}
		return appendable.toString();
	}

	private String createCodeForGreqlExpression(GreqlExpression rootExpr) {
		CodeList list = new CodeList();
		scope.blockBegin();
		// create code for bound variables
		for (IsBoundVarOf inc : rootExpr
				.getIsBoundVarOfIncidences(EdgeDirection.IN)) {
			Variable var = (Variable) inc.getThat();
			scope.addVariable(var.get_name());
			list.add(new CodeSnippet("Object " + var.get_name()
					+ " = boundVariables.getVariable(\"" + var.get_name()
					+ "\");"));
		}

		// create code for main query expression
		IsQueryExprOf inc = rootExpr
				.getFirstIsQueryExprOfIncidence(EdgeDirection.IN);
		Expression queryExpr = (Expression) inc.getThat();
		String resultVariable = "result";
		list.add(new CodeSnippet("Object " + resultVariable + " = "
				+ createCodeForExpression(queryExpr) + ";"));

		// create code for store as
		for (IsIdOfStoreClause storeInc : rootExpr
				.getIsIdOfStoreClauseIncidences(EdgeDirection.IN)) {
			Identifier ident = (Identifier) storeInc.getThat();
			list.add(new CodeSnippet("boundVariables.setVariable(\""
					+ ident.get_name() + "\"," + resultVariable + ");"));
		}

		list.add(new CodeSnippet("return result;"));
		scope.blockEnd();
		return createMethod(list, rootExpr);
	}

	private String createCodeForExpression(Expression queryExpr) {
		if (queryExpr instanceof FunctionApplication) {
			return createCodeForFunctionApplication((FunctionApplication) queryExpr);
		}
		if (queryExpr instanceof Comprehension) {
			return createCodeForComprehension((Comprehension) queryExpr);
		}
		if (queryExpr instanceof QuantifiedExpression) {
			return createCodeForQuantifiedExpression((QuantifiedExpression) queryExpr);
		}
		if (queryExpr instanceof ConditionalExpression) {
			return createCodeForConditionalExpression((ConditionalExpression) queryExpr);
		}
		if (queryExpr instanceof EdgeSetExpression) {
			return createCodeForEdgeSetExpression((EdgeSetExpression) queryExpr);
		}
		if (queryExpr instanceof VertexSetExpression) {
			return createCodeForVertexSetExpression((VertexSetExpression) queryExpr);
		}
		if (queryExpr instanceof ListRangeConstruction) {
			return createCodeForListRangeConstruction((ListRangeConstruction) queryExpr);
		}
		if (queryExpr instanceof ListConstruction) {
			return createCodeForListConstruction((ListConstruction) queryExpr);
		}
		if (queryExpr instanceof SetConstruction) {
			return createCodeForSetConstruction((SetConstruction) queryExpr);
		}
		if (queryExpr instanceof TupleConstruction) {
			return createCodeForTupleConstruction((TupleConstruction) queryExpr);
		}
		if (queryExpr instanceof RecordConstruction) {
			return createCodeForRecordConstruction((RecordConstruction) queryExpr);
		}
		if (queryExpr instanceof Literal) {
			return createCodeForLiteral((Literal) queryExpr);
		}
		if (queryExpr instanceof Variable) {
			return createCodeForVariable((Variable) queryExpr);
		}
		if (queryExpr instanceof Identifier) {
			// Identifiers that are not variables
			return createCodeForIdentifier((Identifier) queryExpr);
		}
		if (queryExpr instanceof ForwardVertexSet) {
			return createCodeForForwardVertexSet((ForwardVertexSet) queryExpr);
		}
		if (queryExpr instanceof BackwardVertexSet) {
			return createCodeForBackwardVertexSet((BackwardVertexSet) queryExpr);
		}
		return "UnsupportedElement: " + queryExpr.getClass().getSimpleName();
	}

	private String createCodeForIdentifier(Identifier ident) {
		return "\"" + ident.get_name() + "\"";
	}

	private String createInitializerForTypeCollection(
			TypeCollection typeCollection) {
		String fieldName = "acceptedType_" + acceptedTypesNumber++;
		int numberOfTypesInSchema = schema.getGraphClass().getVertexClasses()
				.size()
				+ schema.getGraphClass().getEdgeClasses().size();
		addStaticField("java.util.BitSet", fieldName, "new java.util.BitSet()");
		if (typeCollection.getAllowedTypes().isEmpty()) {
			// all types but the forbidden ones are allowed
			addStaticInitializer(fieldName + ".set(0, " + numberOfTypesInSchema
					+ ", true);");
			for (GraphElementClass<?, ?> tc : typeCollection
					.getForbiddenTypes()) {
				addStaticInitializer(fieldName + ".set("
						+ tc.getGraphElementClassIdInSchema() + ", false);");
			}
		} else {
			// only allowed type are allowed, others are forbidden
			addStaticInitializer(fieldName + ".set(0, " + numberOfTypesInSchema
					+ ", false);");
			for (GraphElementClass<?, ?> tc : typeCollection.getAllowedTypes()) {
				addStaticInitializer(fieldName + ".set("
						+ tc.getGraphElementClassIdInSchema() + ",  true);");
			}
		}
		return fieldName;
	}

	private String createInitializerForIncidenceTypeCollection(
			Set<IncidenceClass> incidenceClasses) {
		String fieldName = "acceptedType_" + acceptedTypesNumber++;
		int numberOfTypesInSchema = schema.getGraphClass().getEdgeClasses()
				.size() * 2;
		addStaticField("java.util.BitSet", fieldName, "new java.util.BitSet();");
		addStaticInitializer(fieldName + ".set(0," + numberOfTypesInSchema
				+ ", false);");
		for (IncidenceClass tc : incidenceClasses) {
			addStaticInitializer(fieldName + ".set("
					+ tc.getIncidenceClassIdInSchema() + ",  true);");
		}
		return fieldName;
	}

	// EdgeSetExpression
	private String createCodeForEdgeSetExpression(EdgeSetExpression setExpr) {
		addImports("de.uni_koblenz.jgralab.JGraLab");
		addImports("de.uni_koblenz.jgralab.Edge");
		addImports("de.uni_koblenz.jgralab.greql.types.TypeCollection");
		CodeList list = new CodeList();
		TypeCollection typeCol = new TypeCollection();
		for (IsTypeRestrOfExpression inc : setExpr
				.getIsTypeRestrOfExpressionIncidences(EdgeDirection.IN)) {
			TypeId typeId = (TypeId) inc.getThat();
			typeCol.addTypes((TypeCollection) ((GreqlQueryImpl) query)
					.getVertexEvaluator(typeId).getResult(evaluator));
		}
		String acceptedTypesField = createInitializerForTypeCollection(typeCol);
		CodeSnippet createEdgeSetSnippet = new CodeSnippet();
		createEdgeSetSnippet
				.add("org.pcollections.PCollection<Edge> edgeSet = JGraLab.set();");
		createEdgeSetSnippet.add("for (Edge e : datagraph.edges()) {");
		createEdgeSetSnippet
				.add("\tif ("
						+ acceptedTypesField
						+ ".get(e.getAttributedElementClass().getGraphElementClassIdInSchema())) {");
		createEdgeSetSnippet.add("\t\tedgeSet = edgeSet.plus(e);");
		createEdgeSetSnippet.add("\t}");
		createEdgeSetSnippet.add("}");
		createEdgeSetSnippet.add("return edgeSet;");
		list.add(createEdgeSetSnippet);
		return createMethod(list, setExpr);
	}

	// VertexSetExpression
	private String createCodeForVertexSetExpression(VertexSetExpression setExpr) {
		CodeList list = new CodeList();
		TypeCollection typeCol = new TypeCollection();
		for (IsTypeRestrOfExpression inc : setExpr
				.getIsTypeRestrOfExpressionIncidences(EdgeDirection.IN)) {
			TypeId typeId = (TypeId) inc.getThat();
			typeCol.addTypes((TypeCollection) ((GreqlQueryImpl) query)
					.getVertexEvaluator(typeId).getResult(evaluator));
		}
		String acceptedTypesField = createInitializerForTypeCollection(typeCol);
		CodeSnippet createVertexSetSnippet = new CodeSnippet();
		createVertexSetSnippet
				.add("org.pcollections.PCollection<de.uni_koblenz.jgralab.Vertex> vertexSet = de.uni_koblenz.jgralab.JGraLab.set();");
		createVertexSetSnippet
				.add("for (de.uni_koblenz.jgralab.Vertex e : datagraph.vertices()) {");
		createVertexSetSnippet
				.add("\tif ("
						+ acceptedTypesField
						+ ".get(e.getAttributedElementClass().getGraphElementClassIdInSchema())) {");
		createVertexSetSnippet.add("\t\tvertexSet = vertexSet.plus(e);");
		createVertexSetSnippet.add("\t}");
		createVertexSetSnippet.add("}");
		createVertexSetSnippet.add("return vertexSet;");
		list.add(createVertexSetSnippet);
		return createMethod(list, setExpr);
	}

	private String createCodeForListRangeConstruction(
			ListRangeConstruction listConstr) {
		addImports("de.uni_koblenz.jgralab.JGraLab");
		CodeList list = new CodeList();
		CodeSnippet listSnippet = new CodeSnippet();
		listSnippet
				.add("org.pcollections.PCollection<Object> list = JGraLab.vector();");
		Expression startExpr = (Expression) listConstr
				.getFirstIsFirstValueOfIncidence(EdgeDirection.IN).getThat();
		Expression endExpr = (Expression) listConstr
				.getFirstIsLastValueOfIncidence(EdgeDirection.IN).getThat();
		listSnippet.add("for (int i= " + createCodeForExpression(startExpr)
				+ "; i<" + createCodeForExpression(endExpr) + "; i++) {");
		listSnippet.add("list = list.plus(i);");
		listSnippet.add("}");
		listSnippet.add("return list;");
		list.add(listSnippet);
		return createMethod(list, listConstr);
	}

	private String createCodeForListConstruction(ListConstruction listConstr) {
		addImports("de.uni_koblenz.jgralab.JGraLab");
		CodeList list = new CodeList();
		CodeSnippet listSnippet = new CodeSnippet();
		listSnippet
				.add("org.pcollections.PCollection<Object> list = JGraLab.vector();");
		StringBuilder builder = new StringBuilder("list = list");
		for (IsPartOf inc : listConstr.getIsPartOfIncidences(EdgeDirection.IN)) {
			Expression expr = (Expression) inc.getThat();
			builder.append(".plus(" + createCodeForExpression(expr) + ")");
		}
		builder.append(";");
		listSnippet.add(builder.toString());
		listSnippet.add("return list;");
		list.add(listSnippet);
		return createMethod(list, listConstr);
	}

	private String createCodeForSetConstruction(SetConstruction setConstr) {
		addImports("de.uni_koblenz.jgralab.JGraLab");
		CodeList list = new CodeList();
		CodeSnippet listSnippet = new CodeSnippet();
		listSnippet
				.add("org.pcollections.PCollection<Object> list = JGraLab.set();");
		StringBuilder builder = new StringBuilder("list = list");
		for (IsPartOf inc : setConstr.getIsPartOfIncidences(EdgeDirection.IN)) {
			Expression expr = (Expression) inc.getThat();
			builder.append(".plus(" + createCodeForExpression(expr) + ")");
		}
		builder.append(";");
		listSnippet.add(builder.toString());
		listSnippet.add("return list;");
		list.add(listSnippet);
		return createMethod(list, setConstr);
	}

	private String createCodeForTupleConstruction(TupleConstruction tupleConstr) {
		addImports("de.uni_koblenz.jgralab.JGraLab");
		addImports("de.uni_koblenz.jgralab.greql.types.Tuple");
		CodeList list = new CodeList();
		CodeSnippet listSnippet = new CodeSnippet();
		listSnippet
				.add("org.pcollections.PCollection<Object> list = Tuple.empty();");
		StringBuilder builder = new StringBuilder("list = list");
		for (IsPartOf inc : tupleConstr.getIsPartOfIncidences(EdgeDirection.IN)) {
			Expression expr = (Expression) inc.getThat();
			builder.append(".plus(" + createCodeForExpression(expr) + ")");
		}
		builder.append(";");
		listSnippet.add(builder.toString());
		listSnippet.add("return list;");
		list.add(listSnippet);
		return createMethod(list, tupleConstr);
	}

	private String createCodeForRecordConstruction(
			RecordConstruction recordConstr) {
		addImports("de.uni_koblenz.jgralab.impl.RecordImpl");
		CodeList list = new CodeList();
		CodeSnippet listSnippet = new CodeSnippet();
		listSnippet.add("RecordImpl resultRecord = RecordImpl.empty();");
		StringBuilder builder = new StringBuilder("resultRecord = resultRecord");
		for (IsRecordElementOf inc : recordConstr
				.getIsRecordElementOfIncidences(EdgeDirection.IN)) {
			RecordElement currentElement = inc.getAlpha();
			Identifier ident = (Identifier) currentElement
					.getFirstIsRecordIdOfIncidence(EdgeDirection.IN).getThat();
			Expression expr = (Expression) currentElement
					.getFirstIsRecordExprOfIncidence(EdgeDirection.IN)
					.getThat();
			builder.append(".plus(\"" + ident.get_name() + "\","
					+ createCodeForExpression(expr) + ")");
		}
		builder.append(";");
		listSnippet.add(builder.toString());
		listSnippet.add("return resultRecord;");
		list.add(listSnippet);
		return createMethod(list, recordConstr);
	}

	private String createCodeForConditionalExpression(
			ConditionalExpression condExpr) {
		CodeList list = new CodeList();

		Expression condition = (Expression) condExpr
				.getFirstIsConditionOfIncidence(EdgeDirection.IN).getThat();
		Expression trueExpr = (Expression) condExpr
				.getFirstIsTrueExprOfIncidence(EdgeDirection.IN).getThat();
		Expression falseExpr = (Expression) condExpr
				.getFirstIsFalseExprOfIncidence(EdgeDirection.IN).getThat();

		CodeSnippet snip = new CodeSnippet();
		list.add(snip);
		snip.add("if ((Boolean) " + createCodeForExpression(condition) + ") {");
		snip.add("\treturn " + createCodeForExpression(trueExpr) + ";");
		snip.add("} else {");
		snip.add("\treturn " + createCodeForExpression(falseExpr) + ";");
		snip.add("}");

		String retVal = createMethod(list, condExpr);
		return retVal;
	}

	private String createCodeForComprehension(Comprehension compr) {
		addImports("de.uni_koblenz.jgralab.JGraLab");
		CodeList methodBody = new CodeList();
		CodeSnippet initSnippet = new CodeSnippet();
		if (compr instanceof ListComprehension) {
			initSnippet
					.add("org.pcollections.PCollection<Object> result = JGraLab.vector();");
		}
		if (compr instanceof SetComprehension) {
			initSnippet
					.add("org.pcollections.PCollection<Object> result = JGraLab.set();");
		}
		if (compr instanceof MapComprehension) {
			addImports("org.pcollections.PMap");
			initSnippet.add("PMap result = JGraLab.map();");
		}
		methodBody.add(initSnippet);

		Declaration decl = (Declaration) compr.getFirstIsCompDeclOfIncidence(
				EdgeDirection.IN).getThat();

		// Declarations and variable iteration loops

		CodeList varIterationList = new CodeList();
		methodBody.add(varIterationList);
		scope.blockBegin();
		for (IsSimpleDeclOf simpleDeclInc : decl
				.getIsSimpleDeclOfIncidences(EdgeDirection.IN)) {
			SimpleDeclaration simpleDecl = (SimpleDeclaration) simpleDeclInc
					.getThat();
			Expression domain = (Expression) simpleDecl
					.getFirstIsTypeExprOfDeclarationIncidence(EdgeDirection.IN)
					.getThat();
			CodeSnippet simpleDeclSnippet = new CodeSnippet();
			varIterationList.setVariable(
					"simpleDeclDomainName",
					"domainOfSimpleDecl_"
							+ Integer.toString(simpleDecl.getId()));
			simpleDeclSnippet
					.add("@SuppressWarnings(\"unchecked\")",
							"org.pcollections.PCollection<Object> #simpleDeclDomainName# = (org.pcollections.PCollection<Object>) "
									+ createCodeForExpression(domain) + ";");
			varIterationList.add(simpleDeclSnippet);
			for (IsDeclaredVarOf declaredVarInc : simpleDecl
					.getIsDeclaredVarOfIncidences(EdgeDirection.IN)) {
				Variable var = (Variable) declaredVarInc.getThat();
				CodeSnippet varIterationSnippet = new CodeSnippet();
				varIterationSnippet.setVariable("variableName", var.get_name());
				varIterationSnippet
						.add("for (Object #variableName# : #simpleDeclDomainName#) {");
				VariableEvaluator<? extends Variable> vertexEval = (VariableEvaluator<? extends Variable>) ((GreqlQueryImpl) query)
						.getVertexEvaluator(var);
				List<VertexEvaluator<? extends Expression>> dependingExpressions = vertexEval
						.calculateDependingExpressions();
				for (VertexEvaluator<? extends Expression> ve : dependingExpressions) {
					Vertex currentV = ve.getVertex();
					if (currentV instanceof Variable) {
						continue;
					}
					// reset stored evaluation values for expressions depending
					// on variable var
					String variableName = getVariableName(Integer
							.toString(currentV.getId()));
					varIterationSnippet.add("\t" + variableName + " = null;");
				}
				scope.addVariable(var.get_name());
				varIterationList.add(varIterationSnippet);
				CodeList body = new CodeList();
				varIterationList.add(body);
				varIterationList.add(new CodeSnippet("}"));
				varIterationList = body;
			}
		}

		// condition
		if (decl.getFirstIsConstraintOfIncidence(EdgeDirection.IN) != null) {
			CodeSnippet constraintSnippet = new CodeSnippet();
			constraintSnippet.add("boolean constraint = true;");
			for (IsConstraintOf constraintInc : decl
					.getIsConstraintOfIncidences(EdgeDirection.IN)) {
				Expression constrExpr = (Expression) constraintInc.getThat();
				constraintSnippet.add("constraint = constraint && (Boolean) "
						+ createCodeForExpression(constrExpr) + ";");
			}
			constraintSnippet.add("if (constraint)");
			varIterationList.add(constraintSnippet);
		}

		// main expression
		CodeSnippet iteratedExprSnip = new CodeSnippet();
		if (compr instanceof MapComprehension) {
			Expression keyExpr = (Expression) ((MapComprehension) compr)
					.getFirstIsKeyExprOfComprehensionIncidence(EdgeDirection.IN)
					.getThat();
			Expression valueExpr = (Expression) ((MapComprehension) compr)
					.getFirstIsValueExprOfComprehensionIncidence(
							EdgeDirection.IN).getThat();
			iteratedExprSnip.add("result.put("
					+ createCodeForExpression(keyExpr) + ","
					+ createCodeForExpression(valueExpr) + ");");
		} else {
			Expression resultDefinition = (Expression) compr
					.getFirstIsCompResultDefOfIncidence(EdgeDirection.IN)
					.getThat();
			iteratedExprSnip.add("result = result.plus("
					+ createCodeForExpression(resultDefinition) + ");");
		}
		varIterationList.add(iteratedExprSnip);

		methodBody.add(new CodeSnippet("return result;"));
		scope.blockEnd();
		return createMethod(methodBody, compr);
	}

	private String createCodeForQuantifiedExpression(
			QuantifiedExpression quantExpr) {
		CodeList list = new CodeList();
		Declaration decl = (Declaration) quantExpr
				.getFirstIsQuantifiedDeclOfIncidence(EdgeDirection.IN)
				.getThat();

		// Declarations and variable iteration loops
		int declaredVars = 0;
		String tabs = "";
		int simpleDecls = 0;
		// quantifier
		Quantifier quantifier = (Quantifier) quantExpr
				.getFirstIsQuantifierOfIncidence(EdgeDirection.IN).getThat();
		switch (quantifier.get_type()) {
		case FORALL:
			break;
		case EXISTS:
			break;
		case EXISTSONE:
			list.add(new CodeSnippet("boolean result = false;"));
			break;
		}
		scope.blockBegin();
		for (IsSimpleDeclOf simpleDeclInc : decl
				.getIsSimpleDeclOfIncidences(EdgeDirection.IN)) {
			SimpleDeclaration simpleDecl = (SimpleDeclaration) simpleDeclInc
					.getThat();
			Expression domain = (Expression) simpleDecl
					.getFirstIsTypeExprOfDeclarationIncidence(EdgeDirection.IN)
					.getThat();
			CodeSnippet simpleDeclSnippet = new CodeSnippet();
			simpleDeclSnippet.setVariable("simpleDeclNum",
					Integer.toString(simpleDecls));
			simpleDeclSnippet
					.add(tabs
							+ "org.pcollections.PCollection<Object> domain_#simpleDeclNum# = (org.pcollections.PCollection<Object>) "
							+ createCodeForExpression(domain) + ";");
			list.add(simpleDeclSnippet);
			for (IsDeclaredVarOf declaredVarInc : simpleDecl
					.getIsDeclaredVarOfIncidences(EdgeDirection.IN)) {
				declaredVars++;
				Variable var = (Variable) declaredVarInc.getThat();
				CodeSnippet varIterationSnippet = new CodeSnippet();
				varIterationSnippet.setVariable("variableName", var.get_name());
				varIterationSnippet.setVariable("simpleDeclNum",
						Integer.toString(simpleDecls));
				varIterationSnippet
						.add(tabs
								+ "for (Object #variableName# : domain_#simpleDeclNum#) {");
				tabs += "\t";
				scope.addVariable(var.get_name());
				list.add(varIterationSnippet);
			}
			simpleDecls++;
		}

		// condition
		if (decl.getFirstIsConstraintOfIncidence(EdgeDirection.IN) != null) {
			CodeSnippet constraintSnippet = new CodeSnippet();
			constraintSnippet.add(tabs + "boolean constraint = true;");
			for (IsConstraintOf constraintInc : decl
					.getIsConstraintOfIncidences(EdgeDirection.IN)) {
				Expression constrExpr = (Expression) constraintInc.getThat();
				constraintSnippet.add(tabs
						+ "constraint = constraint && (Boolean) "
						+ createCodeForExpression(constrExpr) + ";");
			}
			constraintSnippet.add(tabs + "if (constraint)");
			list.add(constraintSnippet);
		}

		// main expression
		Expression resultDefinition = (Expression) quantExpr
				.getFirstIsBoundExprOfQuantifiedExpressionIncidence(
						EdgeDirection.IN).getThat();
		CodeSnippet iteratedExprSnip = new CodeSnippet();
		switch (quantifier.get_type()) {
		case FORALL:
			iteratedExprSnip.add(tabs + "if ( ! (Boolean) "
					+ createCodeForExpression(resultDefinition)
					+ ") return false;");
			break;
		case EXISTS:
			iteratedExprSnip.add(tabs + "if ( (Boolean) "
					+ createCodeForExpression(resultDefinition)
					+ ") return true;");
			break;
		case EXISTSONE:
			iteratedExprSnip.add(tabs + "if ( (Boolean) "
					+ createCodeForExpression(resultDefinition) + ") {");
			iteratedExprSnip.add(tabs + "\tif (result) {");
			iteratedExprSnip.add(tabs
					+ "\t\treturn false; //two elements exists");
			iteratedExprSnip.add(tabs + "\t} else {");
			iteratedExprSnip.add(tabs
					+ "\t\tresult = true; //first element found");
			iteratedExprSnip.add(tabs + "\t}");
			iteratedExprSnip.add(tabs + "}");
		}

		list.add(iteratedExprSnip);
		// closing parantheses for interation loops
		for (int curLoop = 0; curLoop < declaredVars; curLoop++) {
			StringBuilder tabBuff = new StringBuilder();
			for (int i = 1; i < (declaredVars - curLoop); i++) {
				tabBuff.append("\t");
			}
			tabs = tabBuff.toString();
			list.add(new CodeSnippet(tabs + "}"));
		}
		switch (quantifier.get_type()) {
		case FORALL:
			list.add(new CodeSnippet("return true;"));
			break;
		case EXISTSONE:
			list.add(new CodeSnippet("return result;"));
			break;
		case EXISTS:
			list.add(new CodeSnippet("return false;"));
		}
		scope.blockEnd();
		return createMethod(list, quantExpr);
	}

	private String createCodeForLiteral(Literal literal) {
		if (literal instanceof StringLiteral) {
			return "\"" + ((StringLiteral) literal).get_stringValue() + "\"";
		}
		if (literal instanceof IntLiteral) {
			return Integer.toString(((IntLiteral) literal).get_intValue());
		}
		if (literal instanceof LongLiteral) {
			return Long.toString(((LongLiteral) literal).get_longValue());
		}
		if (literal instanceof DoubleLiteral) {
			return Double.toString(((DoubleLiteral) literal).get_doubleValue());
		}
		if (literal instanceof BoolLiteral) {
			return Boolean.toString(((BoolLiteral) literal).is_boolValue());
		}
		if (literal instanceof ThisEdge) {
			createThisLiterals();
			return "thisEdge";
		}
		if (literal instanceof ThisVertex) {
			createThisLiterals();
			return "thisVertex";
		}
		return "UndefinedLiteral";
	}

	private String createCodeForVariable(Variable var) {
		return var.get_name();
	}

	/**
	 * Creates code for function application. While for most functions efficient
	 * access routines to the FunLib-function will be created, some of the
	 * functions that rely on path descriptions (e.g., pathSystem) are realized
	 * by code generation for the path search.
	 * 
	 * @param funApp
	 * @return
	 */
	private String createCodeForFunctionApplication(FunctionApplication funApp) {

		// create static field to access function
		FunctionId funId = (FunctionId) funApp.getFirstIsFunctionIdOfIncidence(
				EdgeDirection.IN).getThat();
		if (funId.get_name().equals("pathSystem")) {
			return createCodeForPathSystemFunction(funApp);
		}
		if (funId.get_name().equals("forwardVertexSet")) {
			throw new RuntimeException(
					"Code generation for function forwardVertexSet is not yet implemented. Use the path expression notation v --> instead of forwardVertexSet(v,-->)");
		}
		if (funId.get_name().equals("backwardVertexSet")) {
			throw new RuntimeException(
					"Code generation for function backwardVertexSet is not yet implemented. Use the path expression notation v --> instead of backwardVertexSet(v,-->)");
		}
		if (funId.get_name().equals("isReachable")) {
			throw new RuntimeException(
					"Code generation for function isReachable is not yet implemented. Use the path expression notation v --> w instead of isReachable(v,w,-->)");
		}
		addImports("de.uni_koblenz.jgralab.greql.funlib.FunLib");
		Function function = FunLib.getFunctionInfo(funId.get_name())
				.getFunction();

		String functionName = function.getClass().getName();
		String functionSimpleName = function.getClass().getSimpleName();
		if (functionSimpleName.contains(".")) {
			functionSimpleName = functionSimpleName
					.substring(functionSimpleName.lastIndexOf("."));
		}

		String functionStaticFieldName = functionSimpleName + "_"
				+ funApp.getId();
		addStaticField(
				functionName,
				functionStaticFieldName,
				"(" + functionName + ") FunLib.getFunctionInfo(\""
						+ funId.get_name() + "\").getFunction()");
		// create code list to evaluate function
		CodeList list = new CodeList();

		int argNumber = 0;
		for (IsArgumentOf argInc : funApp
				.getIsArgumentOfIncidences(EdgeDirection.IN)) {
			Expression expr = (Expression) argInc.getThat();
			list.add(new CodeSnippet("Object arg_" + argNumber++ + " = "
					+ createCodeForExpression(expr) + ";"));
		}
		if (funApp.getFirstIsTypeExprOfFunctionIncidence(EdgeDirection.IN) != null) {
			Expression typeExpr = (Expression) funApp
					.getFirstIsTypeExprOfFunctionIncidence(EdgeDirection.IN)
					.getThat();
			list.add(new CodeSnippet("Object arg_" + argNumber++ + " = "
					+ createCodeForExpression(typeExpr) + ";"));
		}
		list.add(new CodeSnippet("boolean matches;"));
		Method[] methods = function.getClass().getMethods();
		for (Method m : methods) {
			if (m.getName() == "evaluate") {
				Class<?>[] paramTypes = m.getParameterTypes();
				// TODO subgraph parameter
				if (paramTypes.length == argNumber) {
					CodeSnippet checkSnippet = new CodeSnippet();
					checkSnippet.add("matches = true;");
					for (int i = 0; i < paramTypes.length; i++) {
						checkSnippet.add("matches &= arg_" + i + " instanceof "
								+ paramTypes[i].getCanonicalName() + ";");
					}
					checkSnippet.add("if (matches)");
					String delim = "";
					StringBuilder argBuilder = new StringBuilder();
					argBuilder.append("\treturn " + functionStaticFieldName
							+ ".evaluate(");
					for (int i = 0; i < paramTypes.length; i++) {
						String cast = "("
								+ getCanonicalNameWithTypeParams(paramTypes[i])
								+ ")";
						argBuilder.append("\t" + delim + cast + "arg_" + i);
						delim = ",";
					}
					argBuilder.append(");");
					checkSnippet.add(argBuilder.toString());
					list.add(checkSnippet);
				}
			}
		}
		list.add(new CodeSnippet(
				"throw new RuntimeException(\"Given arguments don't match an available GReQL function."
						+ " If you have added a function, you need to recompile the GReQL query for the function to be available.\");"));
		return createMethod(list, funApp);
	}

	private String getCanonicalNameWithTypeParams(Class<?> cls) {
		String cn = cls.getCanonicalName();
		int n = cls.getTypeParameters().length;
		if (n > 0) {
			String delim = "<?";
			for (int i = 0; i < n; ++i) {
				cn = cn + delim;
				delim = ",?";
			}
			cn = cn + ">";
		}
		return cn;
	}

	private String createCodeForForwardVertexSet(ForwardVertexSet fws) {
		PathDescription pathDescr = (PathDescription) fws
				.getFirstIsPathOfIncidence(EdgeDirection.IN).getThat();
		PathDescriptionEvaluator<? extends PathDescription> pathDescrEval = (PathDescriptionEvaluator<? extends PathDescription>) ((GreqlQueryImpl) query)
				.getVertexEvaluator(pathDescr);
		DFA dfa = ((NFA) pathDescrEval.getResult(evaluator)).getDFA();
		Expression startElementExpr = (Expression) fws
				.getFirstIsStartExprOfIncidence(EdgeDirection.IN).getThat();
		return createCodeForForwarOrBackwardVertexSet(dfa, startElementExpr,
				fws);
	}

	private String createCodeForBackwardVertexSet(BackwardVertexSet fws) {
		PathDescription pathDescr = (PathDescription) fws
				.getFirstIsPathOfIncidence(EdgeDirection.IN).getThat();
		PathDescriptionEvaluator<? extends PathDescription> pathDescrEval = (PathDescriptionEvaluator<? extends PathDescription>) ((GreqlQueryImpl) query)
				.getVertexEvaluator(pathDescr);
		DFA dfa = NFA.revertNFA((NFA) pathDescrEval.getResult(evaluator))
				.getDFA();
		Expression targetElementExpr = (Expression) fws
				.getFirstIsTargetExprOfIncidence(EdgeDirection.IN).getThat();
		return createCodeForForwarOrBackwardVertexSet(dfa, targetElementExpr,
				fws);
	}

	private String createCodeForForwarOrBackwardVertexSet(DFA dfa,
			Expression startElementExpr, GreqlVertex syntaxGraphVertex) {
		CodeList list = new CodeList();
		addImports("org.pcollections.PSet");
		addImports("de.uni_koblenz.jgralab.*");
		addImports("de.uni_koblenz.jgralab.greql.executable.VertexStateNumberQueue");
		CodeSnippet initSnippet = new CodeSnippet();
		list.add(initSnippet);
		initSnippet.add("PSet<Vertex> resultSet = JGraLab.set();");
		initSnippet.add("//one BitSet for each state");
		initSnippet
				.add("@SuppressWarnings(\"unchecked\")",
						"java.util.HashSet<Vertex>[] markedElements = new java.util.HashSet[#stateCount#];");
		initSnippet.setVariable("stateCount",
				Integer.toString(dfa.stateList.size()));
		initSnippet.add("for (int i=0; i<#stateCount#;i++) {");
		initSnippet
				.add("\tmarkedElements[i] = new java.util.HashSet<Vertex>(100);");
		initSnippet.add("}");
		initSnippet
				.add("java.util.BitSet finalStates = new java.util.BitSet();");
		for (State s : dfa.stateList) {
			if (s.isFinal) {
				initSnippet.add("finalStates.set(" + s.number + ");");
			}
		}
		initSnippet.add("int stateNumber;");
		initSnippet.add("Vertex element = (Vertex)"
				+ createCodeForExpression(startElementExpr) + ";");
		initSnippet.add("Vertex nextElement;");
		initSnippet
				.add("VertexStateNumberQueue queue = new VertexStateNumberQueue();");
		initSnippet.add("markedElements[" + dfa.initialState.number
				+ "].add(element);");
		initSnippet.add("queue.put((Vertex) element, "
				+ dfa.initialState.number + ");");
		initSnippet.add("while (queue.hasNext()) {");
		initSnippet.add("\telement = queue.currentVertex;");
		initSnippet.add("\tstateNumber = queue.currentState;");
		initSnippet.add("\tif (finalStates.get(stateNumber)) {");
		initSnippet.add("\t\tresultSet = resultSet.plus(element);");
		initSnippet.add("\t}");
		initSnippet.add("\tfor (Edge inc = element.getFirstIncidence();");
		initSnippet
				.add("\t\tinc != null; inc = inc.getNextIncidence() ) { //iterating incident edges");
		initSnippet.add("\t\tswitch (stateNumber) {");
		for (State curState : dfa.stateList) {
			CodeList stateCodeList = new CodeList();
			list.add(stateCodeList);
			stateCodeList.add(new CodeSnippet("\t\tcase " + curState.number
					+ ":"));
			for (Transition curTrans : curState.outTransitions) {
				CodeList transitionCodeList = new CodeList();
				stateCodeList.add(transitionCodeList);
				// Generate code to get next vertex and state number
				if (curTrans.consumesEdge()) {
					transitionCodeList.add(new CodeSnippet(
							"\t\t\tnextElement = inc.getThat();"));
				} else {
					transitionCodeList.add(new CodeSnippet(
							"\t\t\tnextElement = element;"));
				}
				// Generate code to check if next element is marked
				transitionCodeList
						.add(new CodeSnippet(
								"\t\t\tif (!markedElements["
										+ curTrans.endState.number
										+ "].contains(nextElement)) {//checking all transitions of state "
										+ curTrans.endState.number));
				transitionCodeList.add(
						createCodeForTransition(curTrans, false), 2);
				transitionCodeList.add(new CodeSnippet(
						"\t\t} //finished checking transitions of state "
								+ curTrans.endState.number));
			}
			stateCodeList.add(new CodeSnippet("\t\tbreak;//break case block"));
		}
		CodeSnippet finalSnippet = new CodeSnippet();
		finalSnippet.add("\t\t} //end of switch");
		finalSnippet.add("\t} //end of iterating incident edges ");
		finalSnippet.add("} //end of processing queue");
		finalSnippet.add("return resultSet;");
		list.add(finalSnippet);
		return createMethod(list, syntaxGraphVertex);
	}

	private CodeSnippet createAddToPathSearchQueueSnippet(int number) {
		CodeSnippet annToQueueSnippet = new CodeSnippet();
		annToQueueSnippet.add("markedElements[" + number
				+ "].add(nextElement);");
		annToQueueSnippet.add("queue.put(nextElement," + number + ");");
		return annToQueueSnippet;
	}

	private CodeList createAddToPathSystemQueueSnippet(Transition t) {
		CodeList list = new CodeList();
		if (t.consumesEdge()) {
			list.setVariable("traversedEdge", "inc");
		} else {
			list.setVariable("traversedEdge", "null");
		}
		list.setVariable("endStateNumber", Integer.toString(t.endState.number));
		list.setVariable("endStateFinal", Boolean.toString(t.endState.isFinal));
		list.setVariable("endStateNumber", Integer.toString(t.endState.number));
		CodeSnippet addSnippet = new CodeSnippet();
		addSnippet.add("PathSystemMarkerEntry newEntry = markVertex(marker,");
		addSnippet.add("	nextElement, #endStateNumber#, #endStateFinal#,");
		addSnippet
				.add("    element, #traversedEdge#, currentEntry.stateNumber,");
		addSnippet.add("    currentEntry.distanceToRoot + 1);");
		if (t.endState.isFinal) {
			addSnippet.add("finalEntries.add(newEntry);");
		}
		addSnippet.add("queue.add(newEntry);");
		return list;
	}

	private String createCodeForPathSystemFunction(FunctionApplication funApp) {
		IsArgumentOf inc = funApp
				.getFirstIsArgumentOfIncidence(EdgeDirection.IN);
		Expression startExpr = (Expression) inc.getThat();
		inc = inc.getNextIsArgumentOfIncidence(EdgeDirection.IN);
		PathDescription pathDescr = (PathDescription) inc.getThat();
		PathDescriptionEvaluator<? extends PathDescription> pathDescrEval = (PathDescriptionEvaluator<? extends PathDescription>) ((GreqlQueryImpl) query)
				.getVertexEvaluator(pathDescr);
		DFA dfa = ((NFA) pathDescrEval.getResult(evaluator)).getDFA();
		return createCodeForPathSystem(dfa, startExpr, funApp);
	}

	private String createCodeForPathSystem(DFA dfa,
			Expression startElementExpr, GreqlVertex syntaxGraphVertex) {
		CodeList list = new CodeList();
		addImports("de.uni_koblenz.jgralab.*");
		addImports("de.uni_koblenz.jgralab.greql.executable.ExecutablePathSystemHelper");
		addImports("de.uni_koblenz.jgralab.greql.executable.PathSystemMarkerEntry");
		addImports("de.uni_koblenz.jgralab.graphmarker.GraphMarker");
		list.setVariable("stateCount", Integer.toString(dfa.stateList.size()));
		list.setVariable("initialStateNumber",
				Integer.toString(dfa.initialState.number));
		list.setVariable("initialStateFinal",
				Boolean.toString(dfa.initialState.isFinal));
		CodeSnippet initSnippet = new CodeSnippet();
		list.add(initSnippet);
		initSnippet.add("Vertex element = (Vertex)"
				+ createCodeForExpression(startElementExpr) + ";");
		initSnippet.add("Vertex nextElement;");
		initSnippet
				.add("java.util.Queue<PathSystemMarkerEntry> queue = new java.util.LinkedList<PathSystemMarkerEntry>();");
		initSnippet.add("@SuppressWarnings(\"unchecked\")");
		initSnippet
				.add("GraphMarker<PathSystemMarkerEntry>[] marker = new GraphMarker[#stateCount#];");
		initSnippet.add("for (int i = 0; i < #stateCount#; i++) {");
		initSnippet
				.add("\tmarker[i] = new GraphMarker<PathSystemMarkerEntry>(element.getGraph());");
		initSnippet.add("}");
		initSnippet
				.add("HashSet<PathSystemMarkerEntry> finalEntries = new HashSet<PathSystemMarkerEntry>();");
		initSnippet
				.add("PathSystemMarkerEntry currentEntry = ",
						"\tExecutablePathSystemHelper.markVertex(marker, element, #initialStateNumber#, #initialStateFinal#, null, null, 0, 0);");
		if (dfa.initialState.isFinal) {
			initSnippet.add("finalEntries.add(currentEntry);");
		}
		initSnippet.add("queue.add(currentEntry);");
		initSnippet.add("while (!queue.isEmpty()) {");
		initSnippet.add("\tcurrentEntry = queue.poll();");
		initSnippet.add("\telement = currentEntry.vertex;");
		initSnippet.add("\tfor (Edge inc = element.getFirstIncidence();");
		initSnippet
				.add("\t\tinc != null; inc = inc.getNextIncidence() ) { //iterating incident edges");
		initSnippet.add("\t\tswitch (currentEntry.stateNumber) {");

		for (State curState : dfa.stateList) {
			CodeList stateCodeList = new CodeList();
			list.add(stateCodeList);
			stateCodeList.add(new CodeSnippet("\t\tcase " + curState.number
					+ ":"));
			for (Transition curTrans : curState.outTransitions) {
				CodeList transitionCodeList = new CodeList();
				stateCodeList.add(transitionCodeList);
				// Generate code to get next vertex and state number
				if (curTrans.consumesEdge()) {
					transitionCodeList.add(new CodeSnippet(
							"\t\t\tnextElement = inc.getThat();"));
				} else {
					transitionCodeList.add(new CodeSnippet(
							"\t\t\tnextElement = element;"));
				}
				// Generate code to check if next element is marked
				transitionCodeList.add(new CodeSnippet(
						"\t\t\tif (!ExecutablePathSystemHelper.isMarked(marker, nextElement, "
								+ curTrans.endState.number
								+ ")) {//checking all transitions of state "
								+ curTrans.endState.number));
				transitionCodeList.add(createCodeForTransition(curTrans, true),
						2);
				transitionCodeList.add(new CodeSnippet(
						"\t\t} //finished checking transitions of state "
								+ curTrans.endState.number));
			}
			stateCodeList.add(new CodeSnippet("\t\tbreak;//break case block"));
		}

		CodeSnippet finalSnippet = new CodeSnippet();
		finalSnippet.add("\t\t} //end of switch");
		finalSnippet.add("\t} //end of iterating incident edges ");
		finalSnippet.add("} //end of processing queue");
		finalSnippet
				.add("return ExecutablePathSystemHelper.createPathSystemFromMarkings(marker, (Vertex)v, finalEntries);");
		list.add(finalSnippet);
		return createMethod(list, syntaxGraphVertex);
	}

	private int acceptedTypesNumber = 0;

	/**
	 * Creates code that implements the transition <code>trans</code>. Depending
	 * on the value of pathSystem, a successfull "firing" of the transition will
	 * add the checked element to the queue of a forward or backward vertex set,
	 * or a new PathSystemQueueEntry will be created. In particular, these
	 * functionality is realized by the methods
	 * createAddToPathSearchQueueSnippet and createAddToPathSystemQueueSnippet,
	 * which will be called depending on the pathSystem parameter
	 * 
	 * @return a CodeBlock implementing the transition with all its checks
	 */
	private CodeBlock createCodeForTransition(Transition trans,
			boolean pathSystem) {
		if (trans instanceof EdgeTransition) {
			return createCodeForEdgeTransition((EdgeTransition) trans,
					pathSystem);
		}
		if (trans instanceof SimpleTransition) {
			return createCodeForSimpleTransition((SimpleTransition) trans,
					pathSystem);
		}
		if (trans instanceof AggregationTransition) {
			return createCodeForAggregationTransition(
					(AggregationTransition) trans, pathSystem);
		}
		if (trans instanceof BoolExpressionTransition) {
			return createCodeForBooleanExpressionTransition(
					(BoolExpressionTransition) trans, pathSystem);
		}
		if (trans instanceof IntermediateVertexTransition) {
			return createCodeForIntermediateVertexTransition(
					(IntermediateVertexTransition) trans, pathSystem);
		}
		if (trans instanceof VertexTypeRestrictionTransition) {
			return createCodeForVertexTypeRestrictionTransition(
					(VertexTypeRestrictionTransition) trans, pathSystem);
		}
		return new CodeSnippet(
				"FAILURE: TRANSITION TYPE IS UNKNOWN TO GREQL CODE GENERATOR "
						+ trans.getClass().getSimpleName());
	}

	private CodeBlock createCodeForSimpleTransition(SimpleTransition trans,
			boolean pathSystem) {
		return createCodeForSimpleOrEdgeTransition(trans, pathSystem, null);
	}

	private CodeBlock createCodeForEdgeTransition(EdgeTransition trans,
			boolean pathSystem) {
		CodeList curr = new CodeList();
		VertexEvaluator<?> allowedEdgeEvaluator = trans
				.getAllowedEdgeEvaluator();
		if (allowedEdgeEvaluator != null) {
			curr.add(new CodeSnippet("Object allowedEdge = "
					+ createCodeForExpression((Expression) allowedEdgeEvaluator
							.getVertex()) + ";"));
			curr.add(new CodeSnippet(
					"if (edge.getNormalEdge() == allowedEdge.getNormalEdge())"));
		}
		return createCodeForSimpleOrEdgeTransition(trans, pathSystem, curr);
	}

	private CodeBlock createCodeForSimpleOrEdgeTransition(
			SimpleTransition trans, boolean pathSystem, CodeBlock edgeTest) {
		CodeList resultList = new CodeList();
		CodeList curr = resultList;
		if (trans.getAllowedDirection() != GReQLDirection.INOUT) {
			switch (trans.getAllowedDirection()) {
			case IN:
				curr.add(new CodeSnippet(
						"if (!inc.isNormal()) { //begin of simple transition"));
				break;
			case OUT:
				curr.add(new CodeSnippet(
						"if (inc.isNormal()) { //begin of simple transition"));
				break;
			}
			CodeList body = new CodeList();
			curr.add(body);
			curr.add(new CodeSnippet("} //end of simple transition"));
			curr = body;
		}
		curr = createTypeCollectionCheck(curr, trans.getTypeCollection());
		curr = createRolenameCheck(curr, trans.getValidToRoles(),
				trans.getValidFromRoles());
		curr = createPredicateCheck(curr, trans.getPredicateEvaluator());
		curr.add(edgeTest);
		// add element to queue
		if (pathSystem) {
			curr.add(createAddToPathSystemQueueSnippet(trans));
		} else {
			curr.add(createAddToPathSearchQueueSnippet(trans.endState.number));
		}
		return resultList;
	}

	private CodeBlock createCodeForAggregationTransition(
			AggregationTransition trans, boolean pathSystem) {
		CodeList resultList = new CodeList();
		CodeList curr = resultList;
		addImports("de.uni_koblenz.jgralab.schema.AggregationKind");
		if (trans.isAggregateFrom()) {
			curr.add(new CodeSnippet(
					"AggregationKind aggrKind = inc.getThisAggregationKind();"));
		} else {
			curr.add(new CodeSnippet(
					"AggregationKind aggrKind = inc.getThatAggregationKind();"));
		}
		curr.add(new CodeSnippet(
				"if ((aggrKind == AggregationKind.SHARED) || (aggrKind == AggregationKind.COMPOSITE)) {"));
		CodeList body = new CodeList();
		curr.add(body);
		curr.add(new CodeSnippet("} //of of check aggregation kind"));
		curr = body;
		curr = createTypeCollectionCheck(curr, trans.getTypeCollection());
		curr = createRolenameCheck(curr, trans.getValidToRoles(),
				trans.getValidFromRoles());
		curr = createPredicateCheck(curr, trans.getPredicateEvaluator());
		// add element to queue
		if (pathSystem) {
			curr.add(createAddToPathSearchQueueSnippet(trans.endState.number));
		} else {
			curr.add(createAddToPathSystemQueueSnippet(trans));
		}
		return resultList;
	}

	private CodeList createTypeCollectionCheck(CodeList curr,
			TypeCollection typeCollection) {
		if (typeCollection != null) {
			String fieldName = createInitializerForTypeCollection(typeCollection);
			curr.add(new CodeSnippet(
					"if ("
							+ fieldName
							+ ".get(((de.uni_koblenz.jgralab.schema.GraphElementClass<?,?>)inc.getAttributedElementClass()).getGraphElementClassIdInSchema())) { //check type collection"));
			CodeList body = new CodeList();
			curr.add(body);
			curr.add(new CodeSnippet("} //end of check type collection"));
			curr = body;
		}
		return curr;
	}

	private CodeList createRolenameCheck(CodeList curr,
			Set<String> validToRoles, Set<String> validFromRoles) {
		Set<String> roles = validToRoles;
		boolean checkTo = true;
		if (roles == null) {
			roles = validFromRoles;
			checkTo = false;
		}
		if (roles != null) {
			addImports("de.uni_koblenz.jgralab.schema.IncidenceClass");
			HashSet<IncidenceClass> incidenceClasses = new HashSet<IncidenceClass>();
			for (EdgeClass ec : schema.getGraphClass().getEdgeClasses()) {
				IncidenceClass to = ec.getTo();
				if (roles.contains(to.getRolename())) {
					incidenceClasses.add(to);
					for (EdgeClass subclass : ec.getAllSubClasses()) {
						incidenceClasses.add(subclass.getTo());
					}
				}
				IncidenceClass from = ec.getFrom();
				if (roles.contains(from.getRolename())) {
					incidenceClasses.add(from);
					for (EdgeClass subclass : ec.getAllSubClasses()) {
						incidenceClasses.add(subclass.getFrom());
					}
				}
			}
			String roleAcceptanceField = createInitializerForIncidenceTypeCollection(incidenceClasses);
			if (checkTo) {
				curr.add(new CodeSnippet(
						"IncidenceClass ic = inc.isNormal() ? inc.getAttributedElementClass().getTo() : inc.getAttributedElementClass().getFrom();"));
			} else {
				curr.add(new CodeSnippet(
						"IncidenceClass ic = inc.isNormal() ? inc.getAttributedElementClass().getFrom() : inc.getAttributedElementClass().getTo();"));
			}
			curr.add(new CodeSnippet(
					"if ("
							+ roleAcceptanceField
							+ ".get(ic.getIncidenceClassIdInSchema())) { // begin of role test"));
			CodeList body = new CodeList();
			curr.add(body);
			curr.add(new CodeSnippet("} //end of role test"));
			curr = body;
		}
		return curr;
	}

	private CodeList createPredicateCheck(CodeList curr,
			VertexEvaluator<? extends Expression> predicateEval) {
		if (predicateEval != null) {
			createThisLiterals();
			curr.add(new CodeSnippet("setThisEdge(inc);"));
			curr.add(new CodeSnippet("setThisVertex(element);"));
			curr.add(new CodeSnippet("if ("
					+ createCodeForExpression(predicateEval.getVertex())
					+ ") { //begin check predicate"));
			CodeList body = new CodeList();
			curr.add(body);
			curr.add(new CodeSnippet("} //end of predicate check"));
			curr = body;
		}
		return curr;
	}

	private CodeBlock createCodeForVertexTypeRestrictionTransition(
			VertexTypeRestrictionTransition trans, boolean pathSystem) {
		CodeList resultList = new CodeList();
		CodeList curr = resultList;
		TypeCollection typeCollection = trans.getAcceptedVertexTypes();
		if (typeCollection != null) {
			String fieldName = createInitializerForTypeCollection(typeCollection);
			curr.add(new CodeSnippet(
					"if ("
							+ fieldName
							+ ".get(((de.uni_koblenz.jgralab.schema.GraphElementClass<?,?>)nextElement.getAttributedElementClass()).getGraphElementClassIdInSchema())) {//test for VertexTypeRestriction"));
			CodeList body = new CodeList();
			curr.add(body);
			curr.add(new CodeSnippet("} //end of vertex type restriction"));
			curr = body;
		}

		// add element to queue
		if (pathSystem) {
			curr.add(createAddToPathSystemQueueSnippet(trans));
		} else {
			curr.add(createAddToPathSearchQueueSnippet(trans.endState.number));
		}
		return resultList;
	}

	private CodeBlock createCodeForIntermediateVertexTransition(
			IntermediateVertexTransition trans, boolean pathSystem) {
		CodeList curr = new CodeList();

		VertexEvaluator<?> intermediateVertexEval = trans
				.getIntermediateVertexEvaluator();
		if (intermediateVertexEval != null) {
			createThisLiterals();
			CodeSnippet predicateSnippet = new CodeSnippet();
			predicateSnippet.add("setThisVertex(element);");
			predicateSnippet
					.add("Object tempRes = "
							+ createCodeForExpression((Expression) intermediateVertexEval
									.getVertex()) + ";");
			predicateSnippet
					.add("if ((tempRes == element) || (((org.pcollections.PCollection<Object>) tempRes).contains(element))) { //test of intermediate vertex transition");
			curr.add(predicateSnippet);
			CodeList body = new CodeList();
			curr.add(body);
			curr.add(new CodeSnippet(
					"} //end of intermediate vertex transition"));
			curr = body;
		}
		// add element to queue
		if (pathSystem) {
			curr.add(createAddToPathSearchQueueSnippet(trans.endState.number));
		} else {
			curr.add(createAddToPathSystemQueueSnippet(trans));
		}
		return curr;
	}

	private CodeBlock createCodeForBooleanExpressionTransition(
			BoolExpressionTransition trans, boolean pathSystem) {
		CodeList curr = new CodeList();

		VertexEvaluator<? extends Expression> predicateEval = trans
				.getBooleanExpressionEvaluator();
		if (predicateEval != null) {
			createThisLiterals();
			curr.add(new CodeSnippet("setThisVertex(element);"));
			curr.add(new CodeSnippet("if ("
					+ createCodeForExpression(predicateEval.getVertex())
					+ ") {"));
			CodeList body = new CodeList();
			curr.add(body);
			curr.add(new CodeSnippet("}"));
			curr = body;
		}
		// add element to queue
		// add element to queue
		if (pathSystem) {
			curr.add(createAddToPathSearchQueueSnippet(trans.endState.number));
		} else {
			curr.add(createAddToPathSystemQueueSnippet(trans));
		}
		return curr;
	}

	// Helper methods

	/*
	 * returns the name of the variable holding the partial result for the
	 * expression with the given id
	 */
	private String getVariableName(String uniqueId) {
		return "result_" + uniqueId;
	}

	Set<Integer> alreadyCreatedEvaluateFunctions = new HashSet<Integer>();

	/**
	 * Creates a method encapsulating the codelist given and returns the call of
	 * that method as a String
	 * 
	 * @param list
	 * @param uniqueId
	 *            a unique Id used to create a global variable that stores the
	 *            value of this method to allow a re-calculation only if the
	 *            variables this expression depends on have changed
	 * @return
	 */
	private String createMethod(CodeList methodBody, GreqlVertex vertex) {
		String comment = "//" + GreqlSerializer.serializeVertex(vertex);
		String methodName = "evaluationMethod_" + vertex.getId();
		String uniqueId = Integer.toString(vertex.getId());
		StringBuilder formalParams = new StringBuilder();
		StringBuilder actualParams = new StringBuilder();
		String delim = "";
		for (String s : scope.getDefinedVariables()) {
			formalParams.append(delim + "Object " + s);
			actualParams.append(delim + s);
			delim = ",";
		}
		if (!alreadyCreatedEvaluateFunctions.contains(vertex.getId())) {
			alreadyCreatedEvaluateFunctions.add(vertex.getId());
			CodeList evaluateMethodBlock = new CodeList();
			evaluateMethodBlock.setVariable("actualParams",
					actualParams.toString());
			evaluateMethodBlock.setVariable("formalParams",
					formalParams.toString());
			evaluateMethodBlock.add(new CodeSnippet("private Object "
					+ getVariableName(uniqueId) + " = null;"));
			resultVariables.add(getVariableName(uniqueId));
			CodeSnippet checkVariableMethod = new CodeSnippet();
			checkVariableMethod.add("private Object " + methodName
					+ "(#formalParams#) {");
			checkVariableMethod.add("\tif (result_" + uniqueId + " == null) {");
			checkVariableMethod.add("\t\tresult_" + uniqueId + " = internal_"
					+ methodName + "(#actualParams#);");
			checkVariableMethod.add("\t}");
			checkVariableMethod.add("\treturn result_" + uniqueId + ";");
			checkVariableMethod.add("}\n");
			evaluateMethodBlock.add(checkVariableMethod);

			evaluateMethodBlock.add(new CodeSnippet(comment));
			evaluateMethodBlock.add(new CodeSnippet("private Object internal_"
					+ methodName + "(#formalParams#) {"));
			evaluateMethodBlock.add(methodBody);
			evaluateMethodBlock.add(new CodeSnippet("}\n"));

			createdMethods.add(evaluateMethodBlock);
		}

		return methodName + "(" + actualParams.toString() + ")";
	}

	private void createThisLiterals() {
		if (!thisLiteralsCreated) {
			thisLiteralsCreated = true;
			addClassField("Edge", "thisEdge", "null");
			createSetterForThisLiteral(graph.getFirstThisEdge(), "Edge");
			addClassField("Vertex", "thisVertex", "null");
			createSetterForThisLiteral(graph.getFirstThisVertex(), "Vertex");
		}
	}

	protected void createSetterForThisLiteral(ThisLiteral lit,
			String edgeOrVertex) {
		CodeList list = new CodeList();
		CodeSnippet snip = new CodeSnippet();
		list.add(snip);
		snip.setVariable("edgeOrVertex", edgeOrVertex);
		snip.add("private final void setThis#edgeOrVertex#(#edgeOrVertex# value) {");
		if (lit != null) {
			VariableEvaluator<? extends Variable> vertexEval = (VariableEvaluator<? extends Variable>) ((GreqlQueryImpl) query)
					.getVertexEvaluator(lit);
			List<VertexEvaluator<? extends Expression>> dependingExpressions = vertexEval
					.calculateDependingExpressions();
			for (VertexEvaluator<? extends Expression> ve : dependingExpressions) {
				Vertex currentV = ve.getVertex();
				if ((currentV instanceof Variable)
						|| (currentV instanceof PathDescription)
						|| (currentV instanceof EdgeRestriction)) {
					continue;
				}
				String variableName = getVariableName(Integer.toString(currentV
						.getId()));
				snip.add("\t" + variableName + " = null; //result of vertex "
						+ currentV.getAttributedElementClass().getSimpleName());
			}
		}
		snip.add("\tthis#edgeOrVertex# = value;");
		snip.add("}");
		createdMethods.add(list);
	}

	private HashSet<String> staticFieldNames = new HashSet<String>();

	private void addStaticField(String type, String var, String def) {
		if (!staticFieldNames.contains(var)) {
			staticFieldNames.add(var);
			staticFieldSnippet.add("static " + type + " " + var + " = " + def
					+ ";", "");
		}
	}

	private void addClassField(String type, String var, String def) {
		classFieldSnippet.add(type + " " + var + " = " + def + ";", "");
	}

	private void addStaticInitializer(String statement) {
		staticInitializerSnippet.add("\t" + statement);
	}

	@Override
	public void createFiles(String pathPrefix) throws GraphIOException {
		String schemaPackage = rootBlock.getVariable("schemaPackage");
		createCode();
		writeCodeToFile(pathPrefix, this.classname + ".java", schemaPackage);
	}

	public InMemoryJavaSourceFile createInMemoryJavaSource() {
		createCode();
		return new InMemoryJavaSourceFile(this.classname, rootBlock.getCode());
	}

	@SuppressWarnings("unchecked")
	public Class<ExecutableQuery> compile() {
		JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
		if (compiler == null) {
			throw new SchemaException("Cannot compile greql query. "
					+ "Most probably, you use a JRE instead of a JDK. "
					+ "The JRE does not provide a compiler.");
		}
		Vector<InMemoryJavaSourceFile> javaSources = new Vector<InMemoryJavaSourceFile>();
		javaSources.add(createInMemoryJavaSource());
		StandardJavaFileManager jfm = compiler.getStandardFileManager(null,
				null, null);
		ClassFileManager manager = new ClassFileManager(this, jfm);
		compiler.getTask(null, manager, null, null, null, javaSources).call();
		try {
			SchemaClassManager schemaClassManager = SchemaClassManager
					.instance(codeGeneratorFileManagerName);
			return (Class<ExecutableQuery>) Class.forName(packageName + "."
					+ this.classname, true, schemaClassManager);
		} catch (ClassNotFoundException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	protected CodeBlock createHeader() {
		return new CodeSnippet(
		// "@SuppressWarnings({ \"rawtypes\", \"unchecked\" })",
				"public class "
						+ classname
						+ " extends AbstractExecutableQuery implements ExecutableQuery {");
	}

	@Override
	protected CodeBlock createPackageDeclaration() {
		return new CodeSnippet("package " + packageName + ";");
	}

	@Override
	public String getManagedName() {
		return codeGeneratorFileManagerName;
	}

}
