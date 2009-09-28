/**
 * 
 */
package de.uni_koblenz.jgralab.greql2;

import java.util.List;

import de.uni_koblenz.jgralab.greql2.exception.Greql2Exception;
import de.uni_koblenz.jgralab.greql2.schema.AggregationPathDescription;
import de.uni_koblenz.jgralab.greql2.schema.AlternativePathDescription;
import de.uni_koblenz.jgralab.greql2.schema.BackwardVertexSet;
import de.uni_koblenz.jgralab.greql2.schema.BagComprehension;
import de.uni_koblenz.jgralab.greql2.schema.BagConstruction;
import de.uni_koblenz.jgralab.greql2.schema.BoolLiteral;
import de.uni_koblenz.jgralab.greql2.schema.Comprehension;
import de.uni_koblenz.jgralab.greql2.schema.ConditionalExpression;
import de.uni_koblenz.jgralab.greql2.schema.Declaration;
import de.uni_koblenz.jgralab.greql2.schema.Definition;
import de.uni_koblenz.jgralab.greql2.schema.DefinitionExpression;
import de.uni_koblenz.jgralab.greql2.schema.Direction;
import de.uni_koblenz.jgralab.greql2.schema.EdgePathDescription;
import de.uni_koblenz.jgralab.greql2.schema.EdgeRestriction;
import de.uni_koblenz.jgralab.greql2.schema.EdgeSetExpression;
import de.uni_koblenz.jgralab.greql2.schema.EdgeSubgraphExpression;
import de.uni_koblenz.jgralab.greql2.schema.EdgeVertexList;
import de.uni_koblenz.jgralab.greql2.schema.ElementSetExpression;
import de.uni_koblenz.jgralab.greql2.schema.ExponentiatedPathDescription;
import de.uni_koblenz.jgralab.greql2.schema.Expression;
import de.uni_koblenz.jgralab.greql2.schema.ForwardVertexSet;
import de.uni_koblenz.jgralab.greql2.schema.FunctionApplication;
import de.uni_koblenz.jgralab.greql2.schema.FunctionId;
import de.uni_koblenz.jgralab.greql2.schema.Greql2;
import de.uni_koblenz.jgralab.greql2.schema.Greql2Expression;
import de.uni_koblenz.jgralab.greql2.schema.Greql2Vertex;
import de.uni_koblenz.jgralab.greql2.schema.Identifier;
import de.uni_koblenz.jgralab.greql2.schema.IntLiteral;
import de.uni_koblenz.jgralab.greql2.schema.IntermediateVertexPathDescription;
import de.uni_koblenz.jgralab.greql2.schema.IteratedPathDescription;
import de.uni_koblenz.jgralab.greql2.schema.LetExpression;
import de.uni_koblenz.jgralab.greql2.schema.ListConstruction;
import de.uni_koblenz.jgralab.greql2.schema.ListRangeConstruction;
import de.uni_koblenz.jgralab.greql2.schema.Literal;
import de.uni_koblenz.jgralab.greql2.schema.MapComprehension;
import de.uni_koblenz.jgralab.greql2.schema.MapConstruction;
import de.uni_koblenz.jgralab.greql2.schema.NullLiteral;
import de.uni_koblenz.jgralab.greql2.schema.OptionalPathDescription;
import de.uni_koblenz.jgralab.greql2.schema.PathConstruction;
import de.uni_koblenz.jgralab.greql2.schema.PathDescription;
import de.uni_koblenz.jgralab.greql2.schema.PathExistence;
import de.uni_koblenz.jgralab.greql2.schema.PathExpression;
import de.uni_koblenz.jgralab.greql2.schema.PathSystemConstruction;
import de.uni_koblenz.jgralab.greql2.schema.PrimaryPathDescription;
import de.uni_koblenz.jgralab.greql2.schema.QuantifiedExpression;
import de.uni_koblenz.jgralab.greql2.schema.Quantifier;
import de.uni_koblenz.jgralab.greql2.schema.RealLiteral;
import de.uni_koblenz.jgralab.greql2.schema.RecordConstruction;
import de.uni_koblenz.jgralab.greql2.schema.RecordElement;
import de.uni_koblenz.jgralab.greql2.schema.RestrictedExpression;
import de.uni_koblenz.jgralab.greql2.schema.SequentialPathDescription;
import de.uni_koblenz.jgralab.greql2.schema.SetComprehension;
import de.uni_koblenz.jgralab.greql2.schema.SetConstruction;
import de.uni_koblenz.jgralab.greql2.schema.SimpleDeclaration;
import de.uni_koblenz.jgralab.greql2.schema.SimplePathDescription;
import de.uni_koblenz.jgralab.greql2.schema.StringLiteral;
import de.uni_koblenz.jgralab.greql2.schema.SubgraphExpression;
import de.uni_koblenz.jgralab.greql2.schema.TableComprehension;
import de.uni_koblenz.jgralab.greql2.schema.ThisEdge;
import de.uni_koblenz.jgralab.greql2.schema.ThisVertex;
import de.uni_koblenz.jgralab.greql2.schema.TransposedPathDescription;
import de.uni_koblenz.jgralab.greql2.schema.TupleConstruction;
import de.uni_koblenz.jgralab.greql2.schema.TypeId;
import de.uni_koblenz.jgralab.greql2.schema.ValueConstruction;
import de.uni_koblenz.jgralab.greql2.schema.Variable;
import de.uni_koblenz.jgralab.greql2.schema.VertexSetExpression;
import de.uni_koblenz.jgralab.greql2.schema.VertexSubgraphExpression;
import de.uni_koblenz.jgralab.greql2.schema.WhereExpression;
import de.uni_koblenz.jgralab.greql2.schema.impl.std.Greql2Impl;

/**
 * @author Tassilo Horn &lt;horn@uni-koblenz.de&gt;
 * 
 */
public class EnhancedGreql2 extends Greql2Impl implements Greql2 {
	public EnhancedGreql2(int vMax, int eMax) {
		this(null, vMax, eMax);
	}

	public EnhancedGreql2(java.lang.String id, int vMax, int eMax) {
		super(id, vMax, eMax);
	}

	private StringBuffer sb = null;

	public String serialize() {
		sb = new StringBuffer();
		serializeGreql2Expression(getFirstGreql2Expression());
		return sb.toString();
	}

	public void serializeGreql2Vertex(Greql2Vertex v) {
		sb = new StringBuffer();
		serializeGreql2Vertex(v, false);
	}

	private void serializeGreql2Vertex(Greql2Vertex v, boolean addSpace) {
		if (v instanceof Declaration) {
			Declaration d = (Declaration) v;
			if (d.getFirstIsQuantifiedDeclOf() != null) {
				serializeDeclaration((Declaration) v, false);
			} else {
				serializeDeclaration((Declaration) v, true);
			}
		} else if (v instanceof Definition) {
			serializeDefinition((Definition) v);
		} else if (v instanceof Direction) {
			serializeDirection((Direction) v);
		} else if (v instanceof EdgeRestriction) {
			serializeEdgeRestriction((EdgeRestriction) v);
		} else if (v instanceof EdgeVertexList) {
			serializeEdgeVertexList((EdgeVertexList) v);
		} else if (v instanceof Greql2Expression) {
			serializeGreql2Expression((Greql2Expression) v);
		} else if (v instanceof Quantifier) {
			serializeQuantifier((Quantifier) v);
		} else if (v instanceof RecordElement) {
			serializeRecordElement((RecordElement) v);
		} else if (v instanceof SimpleDeclaration) {
			serializeSimpleDeclaration((SimpleDeclaration) v);
		} else if (v instanceof Expression) {
			serializeExpression((Expression) v, false);
		} else {
			throw new Greql2Exception("Unknown Greql2Vertex " + v + ".");
		}
		if (addSpace) {
			sb.append(' ');
		}
	}

	private void serializeSimpleDeclaration(SimpleDeclaration v) {
		boolean first = true;
		for (Variable var : v.getDeclaredVarList()) {
			if (first) {
				first = false;
			} else {
				sb.append(", ");
			}
			serializeVariable(var);
		}
		sb.append(": ");
		serializeExpression(v.getTypeExprList().get(0), false);
	}

	private void serializeRecordElement(RecordElement v) {
		serializeIdentifier(v.getRecordIdList().get(0));
		sb.append(" : ");
		serializeExpression(v.getRecordExprList().get(0), false);
	}

	private void serializeQuantifier(Quantifier v) {
		sb.append(v.get_name());
	}

	private void serializeEdgeVertexList(EdgeVertexList v) {
		boolean first = true;
		for (Expression e : v.getEdgeOrVertexExprList()) {
			if (first) {
				first = false;
			} else {
				sb.append(", ");
			}
			serializeExpression(e, false);
		}
	}

	private void serializeEdgeRestriction(EdgeRestriction v) {
		if (!v.getTypeIdList().isEmpty()) {
			serializeIdentifier(v.getTypeIdList().get(0));
		}
		if (!v.getRoleIdList().isEmpty()) {
			sb.append('@');
			serializeIdentifier(v.getRoleIdList().get(0));
		}
	}

	private void serializeDirection(Direction v) {
		sb.append(v.get_dirValue());
	}

	private void serializeDefinition(Definition v) {
		serializeVariable(v.getVarList().get(0));
		sb.append(" := ");
		serializeExpression(v.getExprList().get(0), false);
	}

	private void serializeDeclaration(Declaration v, boolean declOfFWR) {
		boolean first = true;
		for (SimpleDeclaration sd : v.getSimpleDeclList()) {
			if (first) {
				first = false;
			} else {
				sb.append(", ");
			}
			serializeSimpleDeclaration(sd);
		}

		if (!v.getSubgraphList().isEmpty()) {
			sb.append(" in ");
			serializeExpression(v.getSubgraphList().get(0), false);
		}

		first = true;
		for (Expression constraint : v.getConstraintList()) {
			if (declOfFWR) {
				sb.append(" with ");
			} else {
				// in QuantifiedExpressions, the constraints are separated with
				// comma
				sb.append(", ");
			}
			serializeExpression(constraint, false);
		}
	}

	private void serializeGreql2Expression(Greql2Expression greql2Expression) {
		List<? extends Variable> boundVars = greql2Expression.getBoundVarList();
		if (!boundVars.isEmpty()) {
			sb.append("using ");
			for (Variable v : boundVars) {
				serializeVariable(v);
			}
			sb.append(':');
		}

		// TODO: What's the Identifier which may be at the IsIdOf edge???

		serializeExpression(greql2Expression.getQueryExprList().get(0), false);
	}

	private void serializeExpression(Expression exp, boolean addSpace) {
		if (exp instanceof ConditionalExpression) {
			serializeConditionalExpression((ConditionalExpression) exp);
		} else if (exp instanceof FunctionApplication) {
			serializeFunctionApplication((FunctionApplication) exp);
		} else if (exp instanceof Literal) {
			serializeLiteral((Literal) exp);
		} else if (exp instanceof Variable) {
			serializeVariable((Variable) exp);
		} else if (exp instanceof Identifier) {
			serializeIdentifier((Identifier) exp);
		} else if (exp instanceof QuantifiedExpression) {
			serializeQuantifiedExpression((QuantifiedExpression) exp);
		} else if (exp instanceof RestrictedExpression) {
			serializeRestrictedExpression((RestrictedExpression) exp);
		} else if (exp instanceof Comprehension) {
			serializeComprehension((Comprehension) exp);
		} else if (exp instanceof DefinitionExpression) {
			serializeDefinitionExpression((DefinitionExpression) exp);
		} else if (exp instanceof ElementSetExpression) {
			serializeElementSetExpression((ElementSetExpression) exp);
		} else if (exp instanceof PathDescription) {
			serializePathDescription((PathDescription) exp);
		} else if (exp instanceof PathExpression) {
			serializePathExpression((PathExpression) exp);
		} else if (exp instanceof SubgraphExpression) {
			serializeSubgraphExpression((SubgraphExpression) exp);
		} else if (exp instanceof ValueConstruction) {
			serializeValueConstruction((ValueConstruction) exp);
		} else {
			throw new Greql2Exception("Unknown Expression " + exp + ".");
		}
		if (addSpace) {
			sb.append(' ');
		}
	}

	private void serializeValueConstruction(ValueConstruction exp) {
		if (exp instanceof BagConstruction) {
			serializeBagConstruction((BagConstruction) exp);
		} else if (exp instanceof ListConstruction) {
			serializeListConstruction((ListConstruction) exp);
		} else if (exp instanceof MapConstruction) {
			serializeMapConstruction((MapConstruction) exp);
		} else if (exp instanceof PathConstruction) {
			serializePathConstruction((PathConstruction) exp);
		} else if (exp instanceof PathSystemConstruction) {
			serializePathSystemConstruction((PathSystemConstruction) exp);
		} else if (exp instanceof RecordConstruction) {
			serializeRecordConstruction((RecordConstruction) exp);
		} else if (exp instanceof SetConstruction) {
			serializeSetConstruction((SetConstruction) exp);
		} else if (exp instanceof TupleConstruction) {
			serializeTupleConstruction((TupleConstruction) exp, false);
		} else {
			throw new Greql2Exception("Unknown ValueConstruction " + exp + ".");
		}
	}

	private void serializeTupleConstruction(TupleConstruction exp,
			boolean implicit) {
		if (!implicit) {
			sb.append("tup(");
		}
		boolean first = true;
		for (Expression val : exp.getPartList()) {
			if (first) {
				first = false;
			} else {
				sb.append(", ");
			}
			serializeExpression(val, false);
		}
		if (!implicit) {
			sb.append(")");
		}
	}

	private void serializeSetConstruction(SetConstruction exp) {
		sb.append("set(");
		boolean first = true;
		for (Expression val : exp.getPartList()) {
			if (first) {
				first = false;
			} else {
				sb.append(", ");
			}
			serializeExpression(val, false);
		}
		sb.append(")");
	}

	private void serializeRecordConstruction(RecordConstruction exp) {
		sb.append("rec(");
		boolean first = true;
		for (RecordElement re : exp.getRecordElementList()) {
			if (first) {
				first = false;
			} else {
				sb.append(", ");
			}
			serializeRecordElement(re);
		}
		sb.append(')');
	}

	private void serializePathSystemConstruction(PathSystemConstruction exp) {
		sb.append("pathsystem(");
		serializeExpression(exp.getRootList().get(0), true);
		sb.append(", ");
		boolean first = true;
		for (EdgeVertexList evl : exp.getEdgeVertexListList()) {
			if (first) {
				first = false;
			} else {
				sb.append(", ");
			}
			sb.append('(');
			serializeEdgeVertexList(evl);
			sb.append(')');
		}
		sb.append(')');
	}

	private void serializePathConstruction(PathConstruction exp) {
		sb.append("path(");
		boolean first = true;
		for (Expression e : exp.getPartList()) {
			if (first) {
				first = false;
			} else {
				sb.append(", ");
			}
			serializeExpression(e, false);
		}
		sb.append(')');
	}

	private void serializeMapConstruction(MapConstruction exp) {
		sb.append("map(");
		List<? extends Expression> keys = exp.getKeyExprList();
		List<? extends Expression> vals = exp.getValueExprList();

		for (int i = 0; i < keys.size(); i++) {
			if (i > 0) {
				sb.append(", ");
			}
			serializeExpression(keys.get(i), true);
			sb.append("-> ");
			serializeExpression(vals.get(i), false);
		}

		sb.append(")");
	}

	private void serializeListConstruction(ListConstruction exp) {
		sb.append("list(");
		if (exp instanceof ListRangeConstruction) {
			ListRangeConstruction lrc = (ListRangeConstruction) exp;
			serializeExpression(lrc.getFirstValueList().get(0), false);
			sb.append("..");
			serializeExpression(lrc.getLastValueList().get(0), false);
		} else {
			boolean first = true;
			for (Expression val : exp.getPartList()) {
				if (first) {
					first = false;
				} else {
					sb.append(", ");
				}
				serializeExpression(val, false);
			}
		}
		sb.append(")");
	}

	private void serializeBagConstruction(BagConstruction exp) {
		sb.append("bag(");
		boolean first = true;
		for (Expression val : exp.getPartList()) {
			if (first) {
				first = false;
			} else {
				sb.append(", ");
			}
			serializeExpression(val, false);
		}
		sb.append(")");
	}

	private void serializeSubgraphExpression(SubgraphExpression exp) {
		if (exp instanceof EdgeSubgraphExpression) {
			sb.append("e");
		} else if (exp instanceof VertexSubgraphExpression) {
			sb.append("v");
		} else {
			throw new Greql2Exception("Unknown SubgraphExpression " + exp + ".");
		}

		sb.append("Subgraph{");
		boolean first = true;
		for (TypeId t : exp.getTypeRestrList()) {
			if (first) {
				first = false;
			} else {
				sb.append(", ");
			}
			serializeIdentifier(t);
		}
		sb.append('}');
	}

	private void serializePathExpression(PathExpression exp) {
		if (exp instanceof BackwardVertexSet) {
			serializeBackwardVertexSet((BackwardVertexSet) exp);
		} else if (exp instanceof ForwardVertexSet) {
			serializeForwardVertexSet((ForwardVertexSet) exp);
		} else if (exp instanceof PathExistence) {
			serializePathExistence((PathExistence) exp);
		} else {
			throw new Greql2Exception("Unknown PathExpression " + exp + ".");
		}
	}

	private void serializePathExistence(PathExistence exp) {
		serializeExpression(exp.getStartExprList().get(0), true);
		serializeExpression(exp.getPathList().get(0), true);
		serializeExpression(exp.getTargetExprList().get(0), false);
	}

	private void serializeForwardVertexSet(ForwardVertexSet exp) {
		serializeExpression(exp.getStartExprList().get(0), true);
		serializeExpression(exp.getPathList().get(0), false);
	}

	private void serializeBackwardVertexSet(BackwardVertexSet exp) {
		serializeExpression(exp.getPathList().get(0), true);
		serializeExpression(exp.getTargetExprList().get(0), false);
	}

	private void serializePathDescription(PathDescription exp) {
		if (!((exp instanceof PrimaryPathDescription) || (exp instanceof OptionalPathDescription))) {
			sb.append('(');
		}
		if (!exp.getStartRestrList().isEmpty()) {
			sb.append("{");
			serializeExpression(exp.getStartRestrList().get(0), false);
			sb.append("} & ");
		}

		if (exp instanceof AlternativePathDescription) {
			serializeAlternativePathDescription((AlternativePathDescription) exp);
		} else if (exp instanceof ExponentiatedPathDescription) {
			serializeExponentiatedPathDescription((ExponentiatedPathDescription) exp);
		} else if (exp instanceof IntermediateVertexPathDescription) {
			serializeIntermediateVertexPathDescription((IntermediateVertexPathDescription) exp);
		} else if (exp instanceof IteratedPathDescription) {
			serializeIteratedPathDescription((IteratedPathDescription) exp);
		} else if (exp instanceof OptionalPathDescription) {
			serializeOptionalPathDescription((OptionalPathDescription) exp);
		} else if (exp instanceof SequentialPathDescription) {
			serializeSequentialPathDescription((SequentialPathDescription) exp);
		} else if (exp instanceof TransposedPathDescription) {
			serializeTransposedPathDescription((TransposedPathDescription) exp);
		} else if (exp instanceof PrimaryPathDescription) {
			serializePrimaryPathDescription((PrimaryPathDescription) exp);
		} else {
			throw new Greql2Exception("Unknown PathDescription " + exp + ".");
		}

		if (!exp.getGoalRestrList().isEmpty()) {
			sb.append(" & {");
			serializeExpression(exp.getGoalRestrList().get(0), false);
			sb.append("}");
		}
		if (!((exp instanceof PrimaryPathDescription) || (exp instanceof OptionalPathDescription))) {
			sb.append(')');
		}
	}

	private void serializePrimaryPathDescription(PrimaryPathDescription exp) {
		if (exp instanceof EdgePathDescription) {
			serializeEdgePathDescription((EdgePathDescription) exp);
		} else if (exp instanceof SimplePathDescription) {
			serializeSimplePathDescription((SimplePathDescription) exp);
		} else if (exp instanceof AggregationPathDescription) {
			serializeAggregationPathDescription((AggregationPathDescription) exp);
		} else {
			throw new Greql2Exception("Unknown PrimaryPathDescription " + exp
					+ ".");
		}

		if (!exp.getEdgeRestrList().isEmpty()) {
			sb.append("{");
			boolean first = true;
			for (EdgeRestriction er : exp.getEdgeRestrList()) {
				if (first) {
					first = false;
				} else {
					sb.append(", ");
				}
				serializeEdgeRestriction(er);
			}
			sb.append("}");
		}
	}

	private void serializeAggregationPathDescription(
			AggregationPathDescription exp) {
		if (exp.is_outAggregation()) {
			sb.append("<>--");
		} else {
			sb.append("--<>");
		}
	}

	private void serializeSimplePathDescription(SimplePathDescription exp) {
		String dir = exp.getDirectionList().get(0).get_dirValue();
		if (dir.equals("out")) {
			sb.append("-->");
		} else if (dir.equals("in")) {
			sb.append("<--");
		} else {
			sb.append("<->");
		}
	}

	private void serializeEdgePathDescription(EdgePathDescription exp) {
		String dir = exp.getDirectionList().get(0).get_dirValue();
		if (dir.equals("out")) {
			sb.append("--");
			serializeExpression(exp.getEdgeExprList().get(0), false);
			sb.append("->");
		} else if (dir.equals("in")) {
			sb.append("<-");
			serializeExpression(exp.getEdgeExprList().get(0), false);
			sb.append("--");
		} else {
			sb.append("<-");
			serializeExpression(exp.getEdgeExprList().get(0), false);
			sb.append("->");
		}
	}

	private void serializeTransposedPathDescription(
			TransposedPathDescription exp) {
		serializePathDescription(exp.getTransposedPathList().get(0));
		sb.append("^T");
	}

	private void serializeSequentialPathDescription(
			SequentialPathDescription exp) {
		for (PathDescription pd : exp.getSequenceElementList()) {
			serializePathDescription(pd);
		}
	}

	private void serializeOptionalPathDescription(OptionalPathDescription exp) {
		sb.append('[');
		serializePathDescription(exp.getOptionalPathList().get(0));
		sb.append(']');
	}

	private void serializeIteratedPathDescription(IteratedPathDescription exp) {
		serializePathDescription(exp.getIteratedPathList().get(0));
		sb.append(exp.get_times().equals("plus") ? '+' : '*');
	}

	private void serializeIntermediateVertexPathDescription(
			IntermediateVertexPathDescription exp) {
		List<? extends PathDescription> sub = exp.getSubPathList();
		serializePathDescription(sub.get(0));
		serializeExpression(exp.getIntermediateVertexList().get(0), false);
		serializePathDescription(sub.get(1));
	}

	private void serializeExponentiatedPathDescription(
			ExponentiatedPathDescription exp) {
		serializePathDescription(exp.getExponentiatedPathList().get(0));
		sb.append('^');
		serializeLiteral(exp.getExponentList().get(0));
	}

	private void serializeAlternativePathDescription(
			AlternativePathDescription exp) {
		boolean first = true;
		for (PathDescription a : exp.getAlternatePathList()) {
			if (first) {
				first = false;
			} else {
				sb.append(" | ");
			}
			serializePathDescription(a);
		}
	}

	private void serializeElementSetExpression(ElementSetExpression exp) {
		if (exp instanceof VertexSetExpression) {
			sb.append("V");
		} else if (exp instanceof EdgeSetExpression) {
			sb.append("E");
		} else {
			throw new Greql2Exception("Unknown ElementSetExpression " + exp
					+ ".");
		}

		if (exp.getTypeRestrList().isEmpty()) {
			return;
		}

		sb.append("{");
		boolean first = true;
		for (TypeId t : exp.getTypeRestrList()) {
			if (first) {
				first = false;
			} else {
				sb.append(", ");
			}
			serializeIdentifier(t);
		}
		sb.append("}");
	}

	private void serializeDefinitionExpression(DefinitionExpression exp) {
		if (exp instanceof LetExpression) {
			serializeLetExpression((LetExpression) exp);
		} else if (exp instanceof WhereExpression) {
			serializeWhereExpression((WhereExpression) exp);
		} else {
			throw new Greql2Exception("Unknown DefinitionExpression " + exp
					+ ".");
		}
	}

	private void serializeWhereExpression(WhereExpression exp) {
		serializeExpression(exp.getBoundExprOfDefinitionList().get(0), true);
		sb.append("where ");
		boolean first = true;
		for (Definition def : exp.getDefinitionList()) {
			if (first) {
				first = false;
			} else {
				sb.append(", ");
			}
			serializeDefinition(def);
		}
	}

	private void serializeLetExpression(LetExpression exp) {
		sb.append("let ");
		boolean first = true;
		for (Definition def : exp.getDefinitionList()) {
			if (first) {
				first = false;
			} else {
				sb.append(", ");
			}
			serializeDefinition(def);
		}
		sb.append(" in ");
		serializeExpression(exp.getBoundExprOfDefinitionList().get(0), true);
	}

	private void serializeComprehension(Comprehension exp) {
		sb.append("from ");
		serializeDeclaration(exp.getCompDeclList().get(0), true);
		if (exp instanceof SetComprehension) {
			sb.append(" reportSet ");
		} else if (exp instanceof BagComprehension) {
			sb.append(" report ");
		} else if (exp instanceof TableComprehension) {
			sb.append(" reportTable ");
		} else if (exp instanceof MapComprehension) {
			sb.append(" reportMap ");
		} else {
			throw new Greql2Exception("Unknown Comprehension " + exp + ".");
		}

		Expression result = exp.getCompResultDefList().get(0);

		if (result instanceof TupleConstruction) {
			// here the tup() can be omitted
			serializeTupleConstruction((TupleConstruction) result, true);
			sb.append(' ');
		} else {
			serializeExpression(result, true);
		}
		sb.append("end");
	}

	private void serializeRestrictedExpression(RestrictedExpression exp) {
		serializeExpression(exp.getRestrictedExprList().get(0), false);
		sb.append(" & {");
		serializeExpression(exp.getRestrictionList().get(0), false);
		sb.append("}");
	}

	private void serializeQuantifiedExpression(QuantifiedExpression exp) {
		sb.append('(');
		serializeQuantifier(exp.getQuantifierList().get(0));
		sb.append(' ');
		serializeDeclaration(exp.getQuantifiedDeclList().get(0), false);
		sb.append(" @ ");
		serializeExpression(exp.getBoundExprOfQuantifierList().get(0), false);
		sb.append(')');
	}

	private void serializeLiteral(Literal exp) {
		if (exp instanceof BoolLiteral) {
			sb.append(((BoolLiteral) exp).get_boolValue().toString()
					.toLowerCase());
		} else if (exp instanceof IntLiteral) {
			sb.append(((IntLiteral) exp).get_intValue());
		} else if (exp instanceof NullLiteral) {
			sb.append("null");
		} else if (exp instanceof RealLiteral) {
			sb.append(((RealLiteral) exp).get_realValue());
		} else if (exp instanceof StringLiteral) {
			sb.append("\"");
			sb.append(((StringLiteral) exp).get_stringValue());
			sb.append("\"");
		} else if (exp instanceof ThisEdge) {
			sb.append("thisEdge");
		} else if (exp instanceof ThisVertex) {
			sb.append("thisVertex");
		} else {
			throw new Greql2Exception("Unknown Literal " + exp + ".");
		}
	}

	private void serializeIdentifier(Identifier exp) {
		sb.append(exp.get_name());
	}

	private void serializeFunctionApplication(FunctionApplication exp) {
		FunctionId fid = exp.getFunctionIdList().get(0);
		String id = fid.get_name();

		if (id.equals("plus")) {
			serializeFunctionApplicationInfix(exp, "+");
			return;
		} else if (id.equals("minus")) {
			serializeFunctionApplicationInfix(exp, "-");
			return;
		} else if (id.equals("times")) {
			serializeFunctionApplicationInfix(exp, "*");
			return;
		} else if (id.equals("dividedBy")) {
			serializeFunctionApplicationInfix(exp, "/");
			return;
		} else if (id.equals("equals")) {
			serializeFunctionApplicationInfix(exp, "=");
			return;
		} else if (id.equals("nequals")) {
			serializeFunctionApplicationInfix(exp, "<>");
			return;
		} else if (id.equals("grEqual")) {
			serializeFunctionApplicationInfix(exp, ">=");
			return;
		} else if (id.equals("grThan")) {
			serializeFunctionApplicationInfix(exp, ">");
			return;
		} else if (id.equals("leEqual")) {
			serializeFunctionApplicationInfix(exp, "<=");
			return;
		} else if (id.equals("leThan")) {
			serializeFunctionApplicationInfix(exp, "<");
			return;
		} else if (id.equals("reMatch")) {
			serializeFunctionApplicationInfix(exp, "=~");
			return;
		} else if (id.equals("modulo")) {
			serializeFunctionApplicationInfix(exp, "%");
			return;
		} else if (id.equals("and")) {
			serializeFunctionApplicationInfix(exp, "and");
			return;
		} else if (id.equals("or")) {
			serializeFunctionApplicationInfix(exp, "or");
			return;
		} else if (id.equals("xor")) {
			serializeFunctionApplicationInfix(exp, "xor");
			return;
		}

		serializeIdentifier(fid);
		sb.append('(');
		boolean first = true;
		for (Expression arg : exp.getArgumentList()) {
			if (first) {
				first = false;
			} else {
				sb.append(", ");
			}
			serializeExpression(arg, false);
		}
		sb.append(")");
	}

	private void serializeFunctionApplicationInfix(FunctionApplication exp,
			String operator) {
		sb.append("(");
		boolean first = true;
		for (Expression arg : exp.getArgumentList()) {
			if (first) {
				first = false;
			} else {
				sb.append(' ');
				sb.append(operator);
				sb.append(' ');
			}
			serializeExpression(arg, false);
		}
		sb.append(")");
	}

	private void serializeConditionalExpression(ConditionalExpression expression) {
		serializeExpression(expression.getConditionList().get(0), true);
		sb.append("? ");
		serializeExpression(expression.getTrueExprList().get(0), true);
		sb.append(": ");
		serializeExpression(expression.getFalseExprList().get(0), true);
		sb.append(": ");
		serializeExpression(expression.getNullExprList().get(0), false);
	}

	private void serializeVariable(Variable v) {
		sb.append(v.get_name());
	}
}
