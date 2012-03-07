package de.uni_koblenz.jgralab.greql2.executable;

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
import de.uni_koblenz.jgralab.Vertex;
import de.uni_koblenz.jgralab.codegenerator.CodeBlock;
import de.uni_koblenz.jgralab.codegenerator.CodeGenerator;
import de.uni_koblenz.jgralab.codegenerator.CodeGeneratorConfiguration;
import de.uni_koblenz.jgralab.codegenerator.CodeList;
import de.uni_koblenz.jgralab.codegenerator.CodeSnippet;
import de.uni_koblenz.jgralab.graphmarker.GraphMarker;
import de.uni_koblenz.jgralab.greql2.evaluator.fa.AggregationTransition;
import de.uni_koblenz.jgralab.greql2.evaluator.fa.DFA;
import de.uni_koblenz.jgralab.greql2.evaluator.fa.NFA;
import de.uni_koblenz.jgralab.greql2.evaluator.fa.SimpleTransition;
import de.uni_koblenz.jgralab.greql2.evaluator.fa.State;
import de.uni_koblenz.jgralab.greql2.evaluator.fa.Transition;
import de.uni_koblenz.jgralab.greql2.evaluator.vertexeval.PathDescriptionEvaluator;
import de.uni_koblenz.jgralab.greql2.evaluator.vertexeval.VariableEvaluator;
import de.uni_koblenz.jgralab.greql2.evaluator.vertexeval.VertexEvaluator;
import de.uni_koblenz.jgralab.greql2.funlib.FunLib;
import de.uni_koblenz.jgralab.greql2.funlib.Function;
import de.uni_koblenz.jgralab.greql2.schema.BackwardVertexSet;
import de.uni_koblenz.jgralab.greql2.schema.BoolLiteral;
import de.uni_koblenz.jgralab.greql2.schema.Comprehension;
import de.uni_koblenz.jgralab.greql2.schema.ConditionalExpression;
import de.uni_koblenz.jgralab.greql2.schema.Declaration;
import de.uni_koblenz.jgralab.greql2.schema.DoubleLiteral;
import de.uni_koblenz.jgralab.greql2.schema.EdgeSetExpression;
import de.uni_koblenz.jgralab.greql2.schema.Expression;
import de.uni_koblenz.jgralab.greql2.schema.ForwardVertexSet;
import de.uni_koblenz.jgralab.greql2.schema.FunctionApplication;
import de.uni_koblenz.jgralab.greql2.schema.FunctionId;
import de.uni_koblenz.jgralab.greql2.schema.GReQLDirection;
import de.uni_koblenz.jgralab.greql2.schema.Greql2;
import de.uni_koblenz.jgralab.greql2.schema.Greql2Expression;
import de.uni_koblenz.jgralab.greql2.schema.Greql2Vertex;
import de.uni_koblenz.jgralab.greql2.schema.Identifier;
import de.uni_koblenz.jgralab.greql2.schema.IntLiteral;
import de.uni_koblenz.jgralab.greql2.schema.IsArgumentOf;
import de.uni_koblenz.jgralab.greql2.schema.IsBoundVarOf;
import de.uni_koblenz.jgralab.greql2.schema.IsConstraintOf;
import de.uni_koblenz.jgralab.greql2.schema.IsDeclaredVarOf;
import de.uni_koblenz.jgralab.greql2.schema.IsIdOfStoreClause;
import de.uni_koblenz.jgralab.greql2.schema.IsPartOf;
import de.uni_koblenz.jgralab.greql2.schema.IsQueryExprOf;
import de.uni_koblenz.jgralab.greql2.schema.IsRecordElementOf;
import de.uni_koblenz.jgralab.greql2.schema.IsSimpleDeclOf;
import de.uni_koblenz.jgralab.greql2.schema.IsTypeRestrOfExpression;
import de.uni_koblenz.jgralab.greql2.schema.ListComprehension;
import de.uni_koblenz.jgralab.greql2.schema.ListConstruction;
import de.uni_koblenz.jgralab.greql2.schema.ListRangeConstruction;
import de.uni_koblenz.jgralab.greql2.schema.Literal;
import de.uni_koblenz.jgralab.greql2.schema.LongLiteral;
import de.uni_koblenz.jgralab.greql2.schema.MapComprehension;
import de.uni_koblenz.jgralab.greql2.schema.PathDescription;
import de.uni_koblenz.jgralab.greql2.schema.QuantifiedExpression;
import de.uni_koblenz.jgralab.greql2.schema.Quantifier;
import de.uni_koblenz.jgralab.greql2.schema.RecordConstruction;
import de.uni_koblenz.jgralab.greql2.schema.RecordElement;
import de.uni_koblenz.jgralab.greql2.schema.SetComprehension;
import de.uni_koblenz.jgralab.greql2.schema.SetConstruction;
import de.uni_koblenz.jgralab.greql2.schema.SimpleDeclaration;
import de.uni_koblenz.jgralab.greql2.schema.StringLiteral;
import de.uni_koblenz.jgralab.greql2.schema.ThisEdge;
import de.uni_koblenz.jgralab.greql2.schema.ThisVertex;
import de.uni_koblenz.jgralab.greql2.schema.TupleConstruction;
import de.uni_koblenz.jgralab.greql2.schema.TypeId;
import de.uni_koblenz.jgralab.greql2.schema.Variable;
import de.uni_koblenz.jgralab.greql2.schema.VertexSetExpression;
import de.uni_koblenz.jgralab.greql2.serialising.GreqlSerializer;
import de.uni_koblenz.jgralab.greql2.types.TypeCollection;
import de.uni_koblenz.jgralab.schema.EdgeClass;
import de.uni_koblenz.jgralab.schema.GraphElementClass;
import de.uni_koblenz.jgralab.schema.IncidenceClass;
import de.uni_koblenz.jgralab.schema.Schema;
import de.uni_koblenz.jgralab.schema.exception.SchemaException;
import de.uni_koblenz.jgralab.schema.impl.compilation.InMemoryJavaSourceFile;

public class GreqlCodeGenerator extends CodeGenerator {

	private Greql2 graph;
	
	private String classname;
	
	private int functionNumber = 1;
	
	private CodeSnippet classFieldSnippet = new CodeSnippet();
	
	private CodeSnippet staticFieldSnippet = new CodeSnippet();
	
	private CodeSnippet staticInitializerSnippet = new CodeSnippet("static {");
	
	private List<CodeBlock> createdMethods = new LinkedList<CodeBlock>();
	
	private Scope scope;
	
	private Schema schema;
	
	private boolean thisLiteralsCreated = false;
	
	private GraphMarker<VertexEvaluator> vertexEvalGraphMarker;
	
	public GreqlCodeGenerator(Greql2 graph, GraphMarker<VertexEvaluator> vertexEvalGraphMarker, Schema datagraphSchema) {
		super("de.uni_koblenz.jgralab.greql2.executable", "queries", CodeGeneratorConfiguration.WITHOUT_TYPESPECIFIC_METHODS);
		this.graph = graph;
		this.vertexEvalGraphMarker = vertexEvalGraphMarker;
		classname = "SampleQuery";
		this.schema = datagraphSchema;
		scope = new Scope();
		//this.classname = "Query_" + System.currentTimeMillis();
	}

	
	public CodeBlock createBody() {
		CodeList code = new CodeList();
		
		addImports("de.uni_koblenz.jgralab.greql2.executable.*");
		addImports("de.uni_koblenz.jgralab.Graph");
		code.add(staticFieldSnippet);
		code.add(staticInitializerSnippet);
		code.add(classFieldSnippet);		
		Greql2Expression rootExpr = graph.getFirstGreql2Expression();
		CodeSnippet method = new CodeSnippet();
		method.add("");
		method.add("private Graph datagraph;");
		method.add("");
		method.add("public Object execute(de.uni_koblenz.jgralab.Graph graph, java.util.Map<String, Object> boundVariables) {");
		method.add("\tObject result = null;");
		method.add("\tdatagraph = graph;");

		
		//create code for bound variables		
		scope.blockBegin();
		for (IsBoundVarOf inc : rootExpr.getIsBoundVarOfIncidences(EdgeDirection.IN)) {
			Variable var = (Variable) inc.getThat();
			scope.addVariable(var.get_name());
			method.add("\tObject " + var.get_name() + " = boundVariables.get(\"" + var.get_name() + "\");");
		}
		code.add(method);
		
		//create code for main query expression
		IsQueryExprOf inc = rootExpr.getFirstIsQueryExprOfIncidence(EdgeDirection.IN);
		Expression queryExpr = (Expression) inc.getThat();
		code.add( new CodeSnippet("\tresult = " + createCodeForExpression(queryExpr) + ";")  );
				
		
		//create code for store as 
		CodeSnippet endOfMethod = new CodeSnippet();
		for (IsIdOfStoreClause storeInc : rootExpr.getIsIdOfStoreClauseIncidences(EdgeDirection.IN)) {
			Identifier ident = (Identifier) storeInc.getThat();
			endOfMethod.add("\tboundVariables.put(\"" + ident.get_name() + "\","  + ident.get_name() + ");");
		}
		
		
		scope.blockEnd();
		
		//create code for return and method end
		endOfMethod.add("\treturn result;");
		endOfMethod.add("}");
		code.add(endOfMethod);
		
		//add generated methods
		code.add(new CodeSnippet("",""));
		for (CodeBlock methodBlock : createdMethods) {
			code.addNoIndent(methodBlock);
			code.add(new CodeSnippet("",""));
		}
		staticInitializerSnippet.add("}");
		return code;
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
			//Identifiers that are not variables 
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
	

	private String createInitializerForTypeCollection(TypeCollection typeCollection) {
		String fieldName = "acceptedType_" + acceptedTypesNumber++;
		int numberOfTypesInSchema = schema.getVertexClasses().size() + schema.getEdgeClasses().size();
		addStaticField("java.util.BitSet", fieldName, "new java.util.BitSet();");	
		if (typeCollection.getAllowedTypes().isEmpty()) {
			//all types but the forbidden ones are allowed
			addStaticInitializer(fieldName+ ".set(0," + numberOfTypesInSchema + ", true);");
			for (GraphElementClass tc : typeCollection.getForbiddenTypes()) {
				addStaticInitializer(fieldName + ".set(" + tc.getGraphElementClassIdInSchema()  +", false);" );
			}
		} else {
			//only allowed type are allowed, others are forbidden
			addStaticInitializer(fieldName + ".set(0," + numberOfTypesInSchema + ", false);");
			for (GraphElementClass tc : typeCollection.getAllowedTypes()) {
				addStaticInitializer(fieldName + ".set(" + tc.getGraphElementClassIdInSchema()  +",  true);" );
			}
		}
		return fieldName;
	}
	
	private String createInitializerForIncidenceTypeCollection(Set<IncidenceClass> incidenceClasses) {
		String fieldName = "acceptedType_" + acceptedTypesNumber++;
		int numberOfTypesInSchema = schema.getEdgeClasses().size()*2;
		addStaticField("java.util.BitSet", fieldName, "new java.util.BitSet();");	
		addStaticInitializer(fieldName + ".set(0," + numberOfTypesInSchema + ", false);");
		for (IncidenceClass tc : incidenceClasses) {
			addStaticInitializer(fieldName + ".set(" + tc.getIncidenceClassIdInSchema()  +",  true);" );
		}
		return fieldName;
	}

	
	
	//EdgeSetExpression
	private String createCodeForEdgeSetExpression(EdgeSetExpression setExpr) {
		addImports("org.pcollections.PCollection");
		addImports("de.uni_koblenz.jgralab.JGraLab");
		addImports("de.uni_koblenz.jgralab.Edge");
		addImports("de.uni_koblenz.jgralab.greql2.types.TypeCollection");
		addImports("de.uni_koblenz.jgralab.schema.GraphElementClass");
		addImports("java.util.Collection");
		addImports("java.util.LinkedList");
		CodeList list = new CodeList();
		TypeCollection typeCol = new TypeCollection();
		for (IsTypeRestrOfExpression inc : setExpr.getIsTypeRestrOfExpressionIncidences(EdgeDirection.IN)) {
			TypeId typeId = (TypeId) inc.getThat();
			typeCol.addTypes((TypeCollection) vertexEvalGraphMarker.getMark(typeId).getResult());
		}
		String acceptedTypesField = createInitializerForTypeCollection(typeCol);
		CodeSnippet createEdgeSetSnippet = new CodeSnippet();
		createEdgeSetSnippet.add("PCollection<Edge> edgeSet = JGraLab.set();");
		createEdgeSetSnippet.add("for (Edge e : datagraph.edges()) {");
		createEdgeSetSnippet.add("\tif (" + acceptedTypesField + ".get(e.getAttributedElementClass().getGraphElementClassIdInSchema())) {");
		createEdgeSetSnippet.add("\t\tedgeSet = edgeSet.plus(e);");
		createEdgeSetSnippet.add("\t}");
		createEdgeSetSnippet.add("}");
		createEdgeSetSnippet.add("return edgeSet;");
		list.add(createEdgeSetSnippet);
		return createMethod(list, setExpr);
	}
	
	//VertexSetExpression
	private String createCodeForVertexSetExpression(VertexSetExpression setExpr) {
		addImports("org.pcollections.PCollection");
		addImports("de.uni_koblenz.jgralab.JGraLab");
		addImports("de.uni_koblenz.jgralab.Vertex");
		addImports("de.uni_koblenz.jgralab.greql2.types.TypeCollection");
		addImports("de.uni_koblenz.jgralab.schema.GraphElementClass");
		addImports("java.util.Collection");
		addImports("java.util.LinkedList");
		CodeList list = new CodeList();
		TypeCollection typeCol = new TypeCollection();
		for (IsTypeRestrOfExpression inc : setExpr.getIsTypeRestrOfExpressionIncidences(EdgeDirection.IN)) {
			TypeId typeId = (TypeId) inc.getThat();
			typeCol.addTypes((TypeCollection) vertexEvalGraphMarker.getMark(typeId).getResult());
		}
		String acceptedTypesField = createInitializerForTypeCollection(typeCol);
		CodeSnippet createVertexSetSnippet = new CodeSnippet();
		createVertexSetSnippet.add("PCollection<Vertex> vertexSet = JGraLab.set();");
		createVertexSetSnippet.add("for (Vertex e : datagraph.vertices()) {");
		createVertexSetSnippet.add("\tif (" + acceptedTypesField + ".get(e.getAttributedElementClass().getGraphElementClassIdInSchema())) {");
		createVertexSetSnippet.add("\t\tvertexSet = vertexSet.plus(e);");
		createVertexSetSnippet.add("\t}");
		createVertexSetSnippet.add("}");
		createVertexSetSnippet.add("return vertexSet;");
		list.add(createVertexSetSnippet);
		return createMethod(list, setExpr);
	}

	
	
	
	private String createCodeForListRangeConstruction(ListRangeConstruction listConstr) {
		addImports("org.pcollections.PCollection");
		addImports("de.uni_koblenz.jgralab.JGraLab");
		CodeList list = new CodeList();
		CodeSnippet listSnippet = new CodeSnippet();
		listSnippet.add("PCollection list = JGraLab.vector();");
		Expression startExpr = (Expression) listConstr.getFirstIsFirstValueOfIncidence(EdgeDirection.IN).getThat();
		Expression endExpr = (Expression) listConstr.getFirstIsLastValueOfIncidence(EdgeDirection.IN).getThat();
		listSnippet.add("for (int i= " + createCodeForExpression(startExpr) + "; i<" + createCodeForExpression(endExpr) + "; i++) {");
		listSnippet.add("list = list.plus(i);");
		listSnippet.add("}");
		listSnippet.add("return list;");
		list.add(listSnippet);
		return createMethod(list, listConstr);
	}
	
	
	private String createCodeForListConstruction(ListConstruction listConstr) {
		addImports("org.pcollections.PCollection");
		addImports("de.uni_koblenz.jgralab.JGraLab");
		CodeList list = new CodeList();
		CodeSnippet listSnippet = new CodeSnippet();
		listSnippet.add("PCollection list = JGraLab.vector();");
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
		addImports("org.pcollections.PCollection");
		addImports("de.uni_koblenz.jgralab.JGraLab");
		CodeList list = new CodeList();
		CodeSnippet listSnippet = new CodeSnippet();
		listSnippet.add("PCollection list = JGraLab.set();");
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
		addImports("org.pcollections.PCollection");
		addImports("de.uni_koblenz.jgralab.JGraLab");
		addImports("de.uni_koblenz.jgralab.greql2.types.Tuple");
		CodeList list = new CodeList();
		CodeSnippet listSnippet = new CodeSnippet();
		listSnippet.add("PCollection list = Tuple.empty();");
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
		
	private String createCodeForRecordConstruction(RecordConstruction recordConstr) {
		addImports("de.uni_koblenz.jgralab.impl.RecordImpl");
		CodeList list = new CodeList();
		CodeSnippet listSnippet = new CodeSnippet();
		listSnippet.add("RecordImpl resultRecord = RecordImpl.empty();");
		StringBuilder builder = new StringBuilder("resultRecord = resultRecord");
		for (IsRecordElementOf inc : recordConstr.getIsRecordElementOfIncidences(EdgeDirection.IN)) {
			RecordElement currentElement = inc.getAlpha();
			Identifier ident = (Identifier) currentElement.getFirstIsRecordIdOfIncidence(EdgeDirection.IN).getThat();
			Expression expr = (Expression) currentElement.getFirstIsRecordExprOfIncidence(EdgeDirection.IN).getThat();
			builder.append(".plus(\"" + ident.get_name() + "\"," + createCodeForExpression(expr) + ")");
		}
		builder.append(";");
		listSnippet.add(builder.toString());
		listSnippet.add("return resultRecord;");
		list.add(listSnippet);
		return createMethod(list, recordConstr);
	}
	
	private String createCodeForConditionalExpression(ConditionalExpression condExpr) {
		CodeList list = new CodeList();

		Expression condition = (Expression) condExpr.getFirstIsConditionOfIncidence(EdgeDirection.IN).getThat();
		Expression trueExpr = (Expression) condExpr.getFirstIsTrueExprOfIncidence(EdgeDirection.IN).getThat();
		Expression falseExpr = (Expression) condExpr.getFirstIsFalseExprOfIncidence(EdgeDirection.IN).getThat();
		
		CodeSnippet snip = new CodeSnippet();
		list.add(snip);
		snip.add("if ((Boolean) " + createCodeForExpression(condition) + ") {" );
		snip.add("\treturn " + createCodeForExpression(trueExpr) + ";" );
		snip.add("} else {");
		snip.add("\treturn " + createCodeForExpression(falseExpr) + ";" );
		snip.add("}");
		
		String retVal = createMethod(list, condExpr);
		return retVal;
	}
	
	private String createCodeForComprehension(Comprehension compr) {
		addImports("de.uni_koblenz.jgralab.JGraLab");
		addImports("org.pcollections.PCollection");
		CodeList list = new CodeList();
		CodeSnippet initSnippet = new CodeSnippet();
		if (compr instanceof ListComprehension) {
			initSnippet.add("PCollection result = JGraLab.vector();");
		}
		if (compr instanceof SetComprehension) {
			initSnippet.add("PCollection result = JGraLab.set();");
		}
		if (compr instanceof MapComprehension) {
			addImports("org.pcollections.PMap");
			initSnippet.add("PMap result = JGraLab.map();");
		}
		list.add(initSnippet);
		
		Declaration decl = (Declaration) compr.getFirstIsCompDeclOfIncidence(EdgeDirection.IN).getThat();
		
		//Declarations and variable iteration loops
		int declaredVars = 0;
		String tabs = "";
		int simpleDecls = 0;
		scope.blockBegin();
		for (IsSimpleDeclOf simpleDeclInc : decl.getIsSimpleDeclOfIncidences(EdgeDirection.IN)) {
			SimpleDeclaration simpleDecl = (SimpleDeclaration) simpleDeclInc.getThat();
			Expression domain = (Expression) simpleDecl.getFirstIsTypeExprOfDeclarationIncidence(EdgeDirection.IN).getThat();
			CodeSnippet simpleDeclSnippet = new CodeSnippet();
			simpleDeclSnippet.setVariable("simpleDeclNum", Integer.toString(simpleDecls));
			simpleDeclSnippet.add(tabs + "PCollection domain_#simpleDeclNum# = (PCollection) " + createCodeForExpression(domain) + ";");
			list.add(simpleDeclSnippet);
			for (IsDeclaredVarOf declaredVarInc : simpleDecl.getIsDeclaredVarOfIncidences(EdgeDirection.IN)) {
				declaredVars++;
				Variable var = (Variable) declaredVarInc.getThat();
				CodeSnippet varIterationSnippet = new CodeSnippet();
				varIterationSnippet.setVariable("variableName", var.get_name());
				varIterationSnippet.setVariable("simpleDeclNum", Integer.toString(simpleDecls));
				varIterationSnippet.add(tabs + "for (Object #variableName# : domain_#simpleDeclNum#) {");
				VariableEvaluator vertexEval = (VariableEvaluator) vertexEvalGraphMarker.getMark(var);
				List<VertexEvaluator> dependingExpressions = vertexEval.calculateDependingExpressions();
				for (VertexEvaluator ve : dependingExpressions) {
					Vertex currentV = ve.getVertex();
					if (currentV instanceof Variable)
						continue;
					String variableName = getVariableName(Integer.toString(currentV.getId()));
					varIterationSnippet.add("\t" + variableName + " = null;");
				}
				tabs += "\t";
				scope.addVariable(var.get_name());
				list.add(varIterationSnippet);
			}
			simpleDecls++;
		}
		
		//condition
		if (decl.getFirstIsConstraintOfIncidence(EdgeDirection.IN) != null) {
			CodeSnippet constraintSnippet = new CodeSnippet();
			constraintSnippet.add(tabs + "boolean constraint = true;");
			for (IsConstraintOf constraintInc : decl.getIsConstraintOfIncidences(EdgeDirection.IN)) {
				Expression constrExpr = (Expression) constraintInc.getThat();
				constraintSnippet.add(tabs + "constraint = constraint && (Boolean) " + createCodeForExpression(constrExpr) + ";");
			}
			constraintSnippet.add(tabs + "if (constraint)");
			list.add(constraintSnippet);
		}


		//main expression
		CodeSnippet iteratedExprSnip = new CodeSnippet();
		if (compr instanceof MapComprehension) {
			Expression keyExpr = (Expression) ((MapComprehension)compr).getFirstIsKeyExprOfComprehensionIncidence(EdgeDirection.IN).getThat();
			Expression valueExpr = (Expression) ((MapComprehension)compr).getFirstIsValueExprOfComprehensionIncidence(EdgeDirection.IN).getThat();
			iteratedExprSnip.add(tabs + "result.put(" + createCodeForExpression(keyExpr) + "," + createCodeForExpression(valueExpr) + ");");
		} else {
			Expression resultDefinition = (Expression) compr.getFirstIsCompResultDefOfIncidence(EdgeDirection.IN).getThat();
			iteratedExprSnip.add(tabs + "result = result.plus(" + createCodeForExpression(resultDefinition) + ");");
		}
		list.add(iteratedExprSnip);
		//closing parantheses for interation loops
		for (int curLoop=0; curLoop<declaredVars; curLoop++) {
			StringBuilder tabBuff = new StringBuilder();
			for (int i=1; i<(declaredVars-curLoop); i++) {
				tabBuff.append("\t");
			}
			tabs = tabBuff.toString();
			list.add(new CodeSnippet(tabs + "}"));
		}
		list.add(new CodeSnippet("return result;"));
		scope.blockEnd();
		return createMethod(list, compr);
	}
	
	
	private String createCodeForQuantifiedExpression(QuantifiedExpression quantExpr) {
		CodeList list = new CodeList();
		addImports("org.pcollections.PCollection");
		Declaration decl = (Declaration) quantExpr.getFirstIsQuantifiedDeclOfIncidence(EdgeDirection.IN).getThat();
		
		//Declarations and variable iteration loops
		int declaredVars = 0;
		String tabs = "";
		int simpleDecls = 0;
		//quantifier
		Quantifier quantifier = (Quantifier) quantExpr.getFirstIsQuantifierOfIncidence(EdgeDirection.IN).getThat();
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
		for (IsSimpleDeclOf simpleDeclInc : decl.getIsSimpleDeclOfIncidences(EdgeDirection.IN)) {
			SimpleDeclaration simpleDecl = (SimpleDeclaration) simpleDeclInc.getThat();
			Expression domain = (Expression) simpleDecl.getFirstIsTypeExprOfDeclarationIncidence(EdgeDirection.IN).getThat();
			CodeSnippet simpleDeclSnippet = new CodeSnippet();
			simpleDeclSnippet.setVariable("simpleDeclNum", Integer.toString(simpleDecls));
			simpleDeclSnippet.add(tabs + "PCollection domain_#simpleDeclNum# = (PCollection) " + createCodeForExpression(domain) + ";");
			list.add(simpleDeclSnippet);
			for (IsDeclaredVarOf declaredVarInc : simpleDecl.getIsDeclaredVarOfIncidences(EdgeDirection.IN)) {
				declaredVars++;
				Variable var = (Variable) declaredVarInc.getThat();
				CodeSnippet varIterationSnippet = new CodeSnippet();
				varIterationSnippet.setVariable("variableName", var.get_name());
				varIterationSnippet.setVariable("simpleDeclNum", Integer.toString(simpleDecls));
				varIterationSnippet.add(tabs + "for (Object #variableName# : domain_#simpleDeclNum#) {");
				tabs += "\t";
				scope.addVariable(var.get_name());
				list.add(varIterationSnippet);
			}
			simpleDecls++;
		}
		
		//condition
		if (decl.getFirstIsConstraintOfIncidence(EdgeDirection.IN) != null) {
			CodeSnippet constraintSnippet = new CodeSnippet();
			constraintSnippet.add(tabs + "boolean constraint = true;");
			for (IsConstraintOf constraintInc : decl.getIsConstraintOfIncidences(EdgeDirection.IN)) {
				Expression constrExpr = (Expression) constraintInc.getThat();
				constraintSnippet.add(tabs + "constraint = constraint && (Boolean) " + createCodeForExpression(constrExpr) + ";");
			}
			constraintSnippet.add(tabs + "if (constraint)");
			list.add(constraintSnippet);
		}

		//main expression
		Expression resultDefinition = (Expression) quantExpr.getFirstIsBoundExprOfQuantifiedExpressionIncidence(EdgeDirection.IN).getThat();
		CodeSnippet iteratedExprSnip = new CodeSnippet();
		switch (quantifier.get_type()) {
		case FORALL:
			iteratedExprSnip.add(tabs + "if ( ! (Boolean) " + createCodeForExpression(resultDefinition) + ") return false;");
			break;
		case EXISTS:	
			iteratedExprSnip.add(tabs + "if ( (Boolean) " + createCodeForExpression(resultDefinition) + ") return true;");
			break;
		case EXISTSONE:
			iteratedExprSnip.add(tabs + "if ( (Boolean) " + createCodeForExpression(resultDefinition) + ") {");
			iteratedExprSnip.add(tabs + "\tif (result) {");
			iteratedExprSnip.add(tabs + "\t\treturn false; //two elements exists");
			iteratedExprSnip.add(tabs + "\t} else {");
			iteratedExprSnip.add(tabs + "\t\tresult = true; //first element found");
			iteratedExprSnip.add(tabs + "\t}");
			iteratedExprSnip.add(tabs + "}");
		}

		list.add(iteratedExprSnip);
		//closing parantheses for interation loops
		for (int curLoop=0; curLoop<declaredVars; curLoop++) {
			StringBuilder tabBuff = new StringBuilder();
			for (int i=1; i<(declaredVars-curLoop); i++) {
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
			return "\"" + ((StringLiteral)literal).get_stringValue() + "\"";
		}
		if (literal instanceof IntLiteral) {
			return  Integer.toString(((IntLiteral)literal).get_intValue());
		}
		if (literal instanceof LongLiteral) {
			return  Long.toString(((LongLiteral)literal).get_longValue());
		}
		if (literal instanceof DoubleLiteral) {
			return  Double.toString(((DoubleLiteral)literal).get_doubleValue());
		}
		if (literal instanceof BoolLiteral) {
			return  Boolean.toString(((BoolLiteral)literal).is_boolValue());
		}
		if (literal instanceof ThisEdge) {
			return  "thisIncidence";
		}
		if (literal instanceof ThisVertex) {
			return  "thisElement";
		}
		return "UndefinedLiteral";
	}


	private String createCodeForVariable(Variable var) {
		return var.get_name();
	}


	private String createCodeForFunctionApplication(
			FunctionApplication funApp) {
		addImports("de.uni_koblenz.jgralab.greql2.funlib.FunLib");

		//create static field to access function
		FunctionId funId = (FunctionId) funApp.getFirstIsFunctionIdOfIncidence(EdgeDirection.IN).getThat();		
		Function function = FunLib.getFunctionInfo(funId.get_name()).getFunction();

		String functionName = function.getClass().getName();
		String functionSimpleName = function.getClass().getSimpleName();
		if (functionSimpleName.contains("."))
			functionSimpleName = functionSimpleName.substring(functionSimpleName.lastIndexOf("."));
		String functionStaticFieldName = functionSimpleName + "_" + functionNumber++;
		addStaticField(functionName, functionStaticFieldName, "(" + functionName + ") FunLib.getFunctionInfo(\"" + funId.get_name() + "\").getFunction()");		
		//create code list to evaluate function
		CodeList list = new CodeList();

		int argNumber=0;
		for (IsArgumentOf argInc : funApp.getIsArgumentOfIncidences(EdgeDirection.IN)) {
			Expression expr = (Expression) argInc.getThat();
			list.add(new CodeSnippet("Object arg_" + argNumber++  + " = " + createCodeForExpression(expr) + ";"));
		}
		list.add(new CodeSnippet("boolean matches;"));
		Method[] methods = function.getClass().getMethods();
		for (Method m : methods) {
			if (m.getName() == "evaluate") {
				Class<?>[] paramTypes = m.getParameterTypes();
				//TODO subgraph and type parameter
				if (paramTypes.length == argNumber) {
					CodeSnippet checkSnippet = new CodeSnippet();
					checkSnippet.add("matches = true;");
					for (int i=0;i<paramTypes.length; i++) {
						checkSnippet.add("matches &= arg_" + i + " instanceof " + paramTypes[i].getCanonicalName() + ";");
					}
					checkSnippet.add("if (matches)");
					String delim = "";
					StringBuilder argBuilder = new StringBuilder();
					argBuilder.append("\treturn " + functionStaticFieldName + ".evaluate(");
					for (int i=0;i<paramTypes.length; i++) {
						String cast = "(" + paramTypes[i].getCanonicalName() + ")";
						argBuilder.append("\t" + delim + cast + "arg_" + i);
						delim = ",";
					}
					argBuilder.append(");");
					checkSnippet.add(argBuilder.toString());
					list.add(checkSnippet);
				}
			}
		}
		list.add(new CodeSnippet("throw new RuntimeException(\"Given arguments don't match available GReQL function." +
				" If you have added a function, you need to recompile the GReQL query for the function to be available\");"));
		return createMethod(list, funApp);
	}
	
	
	
	private String createCodeForForwardVertexSet(ForwardVertexSet fws) {
		DFA dfa = null;
		PathDescription pathDescr = (PathDescription) fws.getFirstIsPathOfIncidence(EdgeDirection.IN).getThat();
		PathDescriptionEvaluator pathDescrEval = (PathDescriptionEvaluator) vertexEvalGraphMarker.getMark(pathDescr);
		dfa = ((NFA)pathDescrEval.getResult()).getDFA();
		Expression startElementExpr = (Expression) fws.getFirstIsStartExprOfIncidence(EdgeDirection.IN).getThat();
		return createCodeForForwarOrBackwardVertexSet(dfa, startElementExpr, fws);
	}	
	
	private String createCodeForBackwardVertexSet(BackwardVertexSet fws) {
		DFA dfa = null;
		PathDescription pathDescr = (PathDescription) fws.getFirstIsPathOfIncidence(EdgeDirection.IN).getThat();
		PathDescriptionEvaluator pathDescrEval = (PathDescriptionEvaluator) vertexEvalGraphMarker.getMark(pathDescr);
		dfa = NFA.revertNFA((NFA)pathDescrEval.getResult()).getDFA();		
		Expression targetElementExpr = (Expression) fws.getFirstIsTargetExprOfIncidence(EdgeDirection.IN).getThat();
		return createCodeForForwarOrBackwardVertexSet(dfa, targetElementExpr, fws);
	}	
		
	private String createCodeForForwarOrBackwardVertexSet(DFA dfa, Expression startElementExpr, Greql2Vertex syntaxGraphVertex)	{
		CodeList list = new CodeList();
		addImports("org.pcollections.PCollection");
		addImports("org.pcollections.PSet");
		addImports("java.util.HashSet");
		addImports("java.util.BitSet");
		addImports("de.uni_koblenz.jgralab.JGraLab");
		addImports("de.uni_koblenz.jgralab.Vertex");
		addImports("de.uni_koblenz.jgralab.Edge");
		addImports("de.uni_koblenz.jgralab.greql2.executable.VertexStateNumberQueue");
		CodeSnippet initSnippet = new CodeSnippet();
		list.add(initSnippet);
		initSnippet.add("PSet<Vertex> resultSet = JGraLab.set();");
		initSnippet.add("//one BitSet for each state");
		initSnippet.add("HashSet<Vertex>[] markedElements = new HashSet[#stateCount#];");
		initSnippet.setVariable("stateCount", Integer.toString(dfa.stateList.size()));
		initSnippet.add("for (int i=0; i<#stateCount#;i++) {");
		initSnippet.add("\tmarkedElements[i] = new HashSet(100);");
		initSnippet.add("}");
		initSnippet.add("BitSet finalStates = new BitSet();");
		for (State s : dfa.stateList) {
			if (s.isFinal) {
				initSnippet.add("finalStates.set(" + s.number + ");");
			}
		}
		initSnippet.add("Vertex startElement = (Vertex)" + createCodeForExpression(startElementExpr) + ";");
		initSnippet.add("VertexStateNumberQueue queue = new VertexStateNumberQueue();");
		initSnippet.add("markedElements[" + dfa.initialState.number + "].add(startElement);");
		initSnippet.add("int stateNumber;");
		initSnippet.add("Vertex element;");
		initSnippet.add("int nextStateNumber;");
		initSnippet.add("Vertex nextElement;");
		initSnippet.add("queue.put((Vertex) v, " + dfa.initialState.number + ");");
		initSnippet.add("while (queue.hasNext()) {");
		initSnippet.add("\telement = queue.currentVertex;");
		initSnippet.add("\tstateNumber = queue.currentState;");
		initSnippet.add("\tif (finalStates.get(stateNumber)) {");
		initSnippet.add("\t\tresultSet = resultSet.plus(element);");
		initSnippet.add("\t}");
		initSnippet.add("\tfor (Edge inc = element.getFirstIncidence();");
		initSnippet.add("\t\tinc != null; inc = inc.getNextIncidence() ) {");
		initSnippet.add("\t\tswitch (stateNumber) {");
		for (State curState : dfa.stateList) {
			CodeList stateCodeList = new CodeList();
			list.add(stateCodeList);
			stateCodeList.add(new CodeSnippet("\t\tcase " + curState.number + ":"));
			for (Transition curTrans : curState.outTransitions) {
			//	System.out.println("Handling transition " + curTrans.getStartState().number + " --> " + curTrans.endState.number + ":" + curTrans);
				CodeList transitionCodeList = new CodeList();
				stateCodeList.add(transitionCodeList);
				CodeSnippet transBeginSnippet = new CodeSnippet();
				transitionCodeList.addNoIndent(transBeginSnippet);
				//Generate code to get next vertex and state number
				if (curTrans.consumesEdge()) {
					transBeginSnippet.add("\t\t\tnextElement = inc.getThat();"); 
				} else {
					transBeginSnippet.add("\t\t\tnextElement = element;");
				}					
				//Generate code to check if next element is marked
				transBeginSnippet.add("\t\t\tif (!markedElements[" + curTrans.endState.number + "].contains(nextElement)) {");
				transitionCodeList.add(createCodeForTransition(curTrans),2);
				transitionCodeList.add(new CodeSnippet("\t\t\t}"));
			}
			stateCodeList.add(new CodeSnippet("\t\tbreak;//break case block"));
		}
		CodeSnippet finalSnippet = new CodeSnippet();
		finalSnippet.add("\t\t}");
		finalSnippet.add("\t}");
		finalSnippet.add("}");
		finalSnippet.add("return resultSet;");
		list.add(finalSnippet);
		return createMethod(list, syntaxGraphVertex);
	}
	
	
	private CodeSnippet createAddToQueueSnippet(int number) {
		CodeSnippet annToQueueSnippet = new CodeSnippet();
		annToQueueSnippet.add("markedElements[" + number + "].add(nextElement);");
		annToQueueSnippet.add("queue.put(nextElement," + number + ");");
		return annToQueueSnippet;
	}


	private int acceptedTypesNumber = 0;
	
	
	private CodeBlock createCodeForTransition(Transition trans) {
		if (trans instanceof SimpleTransition) {
			return createCodeForSimpleTransition((SimpleTransition) trans);
		}
		if (trans instanceof AggregationTransition) {
			return createCodeForAggregationTransition((AggregationTransition) trans);
		}
		return new CodeSnippet("FAILURE: TRANSITION TYPE IS UNKNOWN TO GREQL CODE GENERATOR " + trans.getClass().getSimpleName()); 
	}
	
	private CodeBlock createCodeForSimpleTransition(SimpleTransition trans) {
		CodeList resultList = new CodeList();
		CodeList curr = resultList;
		addImports("de.uni_koblenz.jgralab.greql2.schema.GReQLDirection");
		if (trans.getAllowedDirection() != GReQLDirection.INOUT) {
			switch (trans.getAllowedDirection()) {
			case IN: 
				curr.add(new CodeSnippet("if (!inc.isNormal()) {" ));
			    break;
			case OUT: 
				curr.add(new CodeSnippet("if (inc.isNormal()) {" ));
				break;
			}
		    CodeList body = new CodeList();
		    curr.add(body);
		    curr.add(new CodeSnippet("}"));
		    curr = body;
		}
		TypeCollection typeCollection = trans.getTypeCollection();
		if (typeCollection != null) {
			String fieldName = createInitializerForTypeCollection(typeCollection);
			curr.add(new CodeSnippet("if (" + fieldName + ".get(((GraphElementClass)inc.getAttributedElementClass()).getGraphElementClassIdInSchema())) {" ));	
		    CodeList body = new CodeList();
		    curr.add(body);
		    curr.add(new CodeSnippet("}"));
		    curr = body;
		}
		
		//roles
		Set<String> roles = trans.getValidToRoles();
		boolean checkTo = true;
		if (roles == null) {
			roles = trans.getValidFromRoles();
			checkTo = false;
		}	
		if (roles != null) {
			addImports("de.uni_koblenz.jgralab.schema.IncidenceClass");
			HashSet<IncidenceClass> incidenceClasses = new HashSet<IncidenceClass>();
			for (EdgeClass ec : schema.getEdgeClasses()) {
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
				curr.add(new CodeSnippet("IncidenceClass ic = inc.isNormal() ? inc.getAttributedElementClass().getTo() : inc.getAttributedElementClass().getFrom();"));
			} else {
				curr.add(new CodeSnippet("IncidenceClass ic = inc.isNormal() ? inc.getAttributedElementClass().getFrom() : inc.getAttributedElementClass().getTo();"));
			}
			curr.add(new CodeSnippet("if (" + roleAcceptanceField + ".get(ic.getIncidenceClassIdInSchema())) {" ));	
		    CodeList body = new CodeList();
		    curr.add(body);
		    curr.add(new CodeSnippet("}"));
		    curr = body;
		}
		
		VertexEvaluator predicateEval = trans.getPredicateEvaluator();
		if (predicateEval != null ) {
			//create code for the boolean expression restricting this transition
			//add class fields for this literals, TODO: only if thisIncidence/thisElement literals are used in this predicate
			createThisLiterals();
			CodeSnippet predicateSnippet = new CodeSnippet();
			predicateSnippet.add("thisEdge = inc;");
			predicateSnippet.add("thisVertex = element;");
			predicateSnippet.add("if (" + createCodeForExpression((Expression)predicateEval.getVertex()) + ") {");
		    CodeList body = new CodeList();
		    curr.add(body);
		    curr.addNoIndent(new CodeSnippet("}"));
		    curr = body;
		}
		//add element to queue
		curr.add(createAddToQueueSnippet(trans.endState.number));
		return resultList;
	}
	
	private CodeBlock createCodeForAggregationTransition(AggregationTransition trans) {
		CodeList resultList = new CodeList();
		CodeList curr = resultList;
		addImports("de.uni_koblenz.jgralab.schema.AggregationKind");
		{
			if (trans.isAggregateFrom()) {
				curr.add(new CodeSnippet("AggregationKind aggrKind = inc.getThisAggregationKind();"));
			} else {
				curr.add(new CodeSnippet("AggregationKind aggrKind = inc.getThatAggregationKind();"));
			}
			curr.add(new CodeSnippet("if ((aggrKind == AggregationKind.SHARED) || (aggrKind == AggregationKind.COMPOSITE)) {" ));
		    CodeList body = new CodeList();
		    curr.add(body);
		    curr.add(new CodeSnippet("}"));
		    curr = body;
		}
		TypeCollection typeCollection = trans.getTypeCollection();
		if (typeCollection != null) {
			String fieldName = createInitializerForTypeCollection(typeCollection);
			curr.add(new CodeSnippet("if (" + fieldName + ".get(((GraphElementClass)inc.getAttributedElementClass()).getGraphElementClassIdInSchema())) {" ));	
		    CodeList body = new CodeList();
		    curr.add(body);
		    curr.add(new CodeSnippet("}"));
		    curr = body;
		}
		//TODO: Add role acceptance
		
		VertexEvaluator predicateEval = trans.getPredicateEvaluator();
		if (predicateEval != null ) {
			//create code for the boolean expression restricting this transition
			//add class fields for this literals, TODO: only if thisIncidence/thisElement literals are used in this predicate
			createThisLiterals();
			CodeSnippet predicateSnippet = new CodeSnippet();
			predicateSnippet.add("thisEdge = inc;");
			predicateSnippet.add("thisVertex = element;");
			predicateSnippet.add("if (" + createCodeForExpression((Expression)predicateEval.getVertex()) + ") {");
		    CodeList body = new CodeList();
		    curr.add(body);
		    curr.add(new CodeSnippet("}"));
		    curr = body;
		}
		//add element to queue
		curr.add(createAddToQueueSnippet(trans.endState.number));
		return resultList;
	}
	
	//Helper methods
	
	/*
	 * returns the name of the variable holding the partial result for the expression
	 * with the given id
	 */
	private String getVariableName(String uniqueId) {
		return "result_" + uniqueId;
	}
	
	/**
	 * Creates a method encapsulating the codelist given and returns the call of that method as a String
	 * @param list
	 * @param uniqueId a unique Id used to create a global variable that stores the value of this method
	 *        to allow a re-calculation only if the variables this expression depends on have changed
	 * @return
	 */
	private String createMethod(CodeList methodBody, Greql2Vertex vertex) {
		String comment = "//" + GreqlSerializer.serializeVertex(vertex);
		String methodName = "evaluationMethod_" + vertex.getId();
		String uniqueId = Integer.toString(vertex.getId());
		StringBuilder formalParams = new StringBuilder();
		StringBuilder actualParams = new StringBuilder();
		String delim = "";
		for (String s : scope.getDefinedVariables()) {
			formalParams.append(delim +  "Object " + s);
			actualParams.append(delim +  s);
			delim = ",";
		}
			
		CodeList preventReevaluationBlock = new CodeList();
		preventReevaluationBlock.setVariable("actualParams", actualParams.toString());
		preventReevaluationBlock.setVariable("formalParams", formalParams.toString());
		CodeSnippet variableSnippet = new CodeSnippet();
		variableSnippet.add("private Object " + getVariableName(uniqueId) + " = null;");
		preventReevaluationBlock.add(variableSnippet);
		CodeSnippet checkVariableMethod = new CodeSnippet();
		checkVariableMethod.add("\n");
		checkVariableMethod.add("private Object " + methodName + "(#formalParams#) {");
		checkVariableMethod.add("\tif (result_" + uniqueId + " == null) {");
		checkVariableMethod.add("\t\tresult_" + uniqueId + " = internal_" + methodName + "(#actualParams#);");
		checkVariableMethod.add("\t}");
		checkVariableMethod.add("\treturn result_" + uniqueId + ";");
		checkVariableMethod.add("}");
		checkVariableMethod.add("\n");
		preventReevaluationBlock.add(checkVariableMethod);
		
		CodeSnippet internalEvaluationMethodHead = new CodeSnippet();
		internalEvaluationMethodHead.add(comment);
		internalEvaluationMethodHead.add("private Object internal_" + methodName + "(#formalParams#) {");
		preventReevaluationBlock.add(internalEvaluationMethodHead);
		preventReevaluationBlock.add(methodBody);
		preventReevaluationBlock.add(new CodeSnippet("}"));
		
		createdMethods.add(preventReevaluationBlock);

		return methodName + "(" + actualParams.toString() + ")";
	}

	
	private void createThisLiterals() {
		if (!thisLiteralsCreated) {
			thisLiteralsCreated = true;
			addClassField("Edge", "thisEdge", "null");
			addClassField("Vertex", "thisVertex", "null");
		}
	}

	private void addStaticField(String type, String var, String def) {
		staticFieldSnippet.add("static " + type + " " + var + " = " + def + ";", "");
	}
	
	private void addClassField(String type, String var, String def) {
		classFieldSnippet.add(type + " " + var + " = " + def + ";", "");
	}

	private void addStaticInitializer(String statement) {
		staticInitializerSnippet.add(statement);		
	}


	public void createFiles(String pathPrefix) throws GraphIOException {
		String schemaPackage = rootBlock.getVariable("schemaPackage");
		createCode();
		writeCodeToFile(pathPrefix, this.classname + ".java", schemaPackage);
	}
	
	public InMemoryJavaSourceFile createInMemoryJavaSource() {
		return new InMemoryJavaSourceFile(this.classname, rootBlock.getCode());
	}

	public void compile() {
		JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
		if (compiler == null) {
			throw new SchemaException("Cannot compile greql query. "
					+ "Most probably you use a JRE instead of a JDK. "
					+ "The JRE does not provide a compiler.");

		}
		StandardJavaFileManager jfm = compiler.getStandardFileManager(null,
				null, null);
		Vector<InMemoryJavaSourceFile> javaSources = new Vector<InMemoryJavaSourceFile>();
		javaSources.add(createInMemoryJavaSource());
		compiler.getTask(null, jfm, null, null, null, javaSources).call();
	}


	@Override
	protected CodeBlock createHeader() {
		CodeSnippet s = new CodeSnippet();
		s.add("public class " + classname + " extends AbstractExecutableQuery implements ExecutableQuery {");
		return s;
	}
	
	protected CodeBlock createPackageDeclaration() {
		CodeSnippet code = new CodeSnippet(true);
		code.add("package de.uni_koblenz.jgralab.greql2.executable.queries;");
		return code;
	}
	
}
