/**
 * 
 */
package de.uni_koblenz.jgralab.greql2;

import java.util.List;

import de.uni_koblenz.jgralab.WorkInProgress;
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
import de.uni_koblenz.jgralab.greql2.schema.EdgeVertexList;
import de.uni_koblenz.jgralab.greql2.schema.ElementSetExpression;
import de.uni_koblenz.jgralab.greql2.schema.ExponentiatedPathDescription;
import de.uni_koblenz.jgralab.greql2.schema.Expression;
import de.uni_koblenz.jgralab.greql2.schema.ForwardVertexSet;
import de.uni_koblenz.jgralab.greql2.schema.FunctionApplication;
import de.uni_koblenz.jgralab.greql2.schema.Greql2;
import de.uni_koblenz.jgralab.greql2.schema.Greql2Expression;
import de.uni_koblenz.jgralab.greql2.schema.Greql2Vertex;
import de.uni_koblenz.jgralab.greql2.schema.Identifier;
import de.uni_koblenz.jgralab.greql2.schema.IntLiteral;
import de.uni_koblenz.jgralab.greql2.schema.IntermediateVertexPathDescription;
import de.uni_koblenz.jgralab.greql2.schema.IteratedPathDescription;
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
import de.uni_koblenz.jgralab.greql2.schema.ValueConstruction;
import de.uni_koblenz.jgralab.greql2.schema.Variable;
import de.uni_koblenz.jgralab.greql2.schema.impl.std.Greql2Impl;

/**
 * @author Tassilo Horn &lt;horn@uni-koblenz.de&gt;
 * 
 */
@WorkInProgress
public class EnhancedGreql2 extends Greql2Impl implements Greql2 {
	public EnhancedGreql2(int vMax, int eMax) {
		this(null, vMax, eMax);
	}

	public EnhancedGreql2(java.lang.String id, int vMax, int eMax) {
		super(id, vMax, eMax);
	}

	public String serialize() {
		StringBuffer sb = new StringBuffer();

		serializeGreql2Expression(getFirstGreql2Expression(), sb);

		return sb.toString();
	}

	public void serializeGreql2Vertex(Greql2Vertex v, StringBuffer sb) {
		serializeGreql2Vertex(v, sb, false);
	}

	private void serializeGreql2Vertex(Greql2Vertex v, StringBuffer sb,
			boolean addSpace) {
		if (v instanceof Declaration) {
			serializeDeclaration((Declaration) v, sb);
		} else if (v instanceof Definition) {
			serializeDefinition((Definition) v, sb);
		} else if (v instanceof Direction) {
			serializeDirection((Direction) v, sb);
		} else if (v instanceof EdgeRestriction) {
			serializeEdgeRestriction((EdgeRestriction) v, sb);
		} else if (v instanceof EdgeVertexList) {
			serializeEdgeVertexList((EdgeVertexList) v, sb);
		} else if (v instanceof Greql2Expression) {
			serializeGreql2Expression((Greql2Expression) v, sb);
		} else if (v instanceof Quantifier) {
			serializeQuantifier((Quantifier) v, sb);
		} else if (v instanceof RecordElement) {
			serializeRecordElement((RecordElement) v, sb);
		} else if (v instanceof SimpleDeclaration) {
			serializeSimpleDeclaration((SimpleDeclaration) v, sb);
		} else if (v instanceof Expression) {
			serializeExpression((Expression) v, sb, false);
		} else {
			throw new Greql2Exception("Unknown Greql2Vertex " + v + ".");
		}
		if (addSpace) {
			sb.append(' ');
		}
	}

	private void serializeSimpleDeclaration(SimpleDeclaration v, StringBuffer sb) {
		boolean first = true;
		for (Variable var : v.getDeclaredVarList()) {
			if (first) {
				first = false;
			} else {
				sb.append(", ");
			}
			serializeVariable(var, sb);
		}
		sb.append(": ");
		serializeExpression(v.getTypeExprList().get(0), sb, false);
	}

	private void serializeRecordElement(RecordElement v, StringBuffer sb) {
		// TODO Auto-generated method stub

	}

	private void serializeQuantifier(Quantifier v, StringBuffer sb) {
		sb.append(v.get_name());
	}

	private void serializeEdgeVertexList(EdgeVertexList v, StringBuffer sb) {
		// TODO Auto-generated method stub

	}

	private void serializeEdgeRestriction(EdgeRestriction v, StringBuffer sb) {
		// TODO Auto-generated method stub

	}

	private void serializeDirection(Direction v, StringBuffer sb) {
		sb.append(v.get_dirValue());
	}

	private void serializeDefinition(Definition v, StringBuffer sb) {
		// TODO Auto-generated method stub

	}

	private void serializeDeclaration(Declaration v, StringBuffer sb) {
		sb.append("from ");
		boolean first = true;
		for (SimpleDeclaration sd : v.getSimpleDeclList()) {
			if (first) {
				first = false;
			} else {
				sb.append(", ");
			}
			serializeSimpleDeclaration(sd, sb);
		}
		sb.append(' ');
		for (Expression constraint : v.getConstraintList()) {
			sb.append("with ");
			serializeExpression(constraint, sb, true);
		}
	}

	private void serializeGreql2Expression(Greql2Expression greql2Expression,
			StringBuffer sb) {
		List<? extends Variable> boundVars = greql2Expression.getBoundVarList();
		if (!boundVars.isEmpty()) {
			sb.append("using ");
			for (Variable v : boundVars) {
				serializeVariable(v, sb);
			}
			sb.append(':');
		}

		// TODO: What's the Identifier which may be at the IsIdOf edge???

		serializeExpression(greql2Expression.getQueryExprList().get(0), sb,
				false);
	}

	private void serializeExpression(Expression exp, StringBuffer sb,
			boolean addSpace) {
		if (exp instanceof ConditionalExpression) {
			serializeConditionalExpression((ConditionalExpression) exp, sb);
		} else if (exp instanceof FunctionApplication) {
			serializeFunctionApplication((FunctionApplication) exp, sb);
		} else if (exp instanceof Variable) {
			serializeVariable((Variable) exp, sb);
		} else if (exp instanceof Identifier) {
			serializeIdentifier((Identifier) exp, sb);
		} else if (exp instanceof Literal) {
			serializeLiteral((Literal) exp, sb);
		} else if (exp instanceof QuantifiedExpression) {
			serializeQuantifiedExpression((QuantifiedExpression) exp, sb);
		} else if (exp instanceof RestrictedExpression) {
			serializeRestrictedExpression((RestrictedExpression) exp, sb);
		} else if (exp instanceof Comprehension) {
			serializeComprehension((Comprehension) exp, sb);
		} else if (exp instanceof DefinitionExpression) {
			serializeDefinitionExpression((DefinitionExpression) exp, sb);
		} else if (exp instanceof ElementSetExpression) {
			serializeElementSetExpression((ElementSetExpression) exp, sb);
		} else if (exp instanceof PathDescription) {
			serializePathDescription((PathDescription) exp, sb);
		} else if (exp instanceof PathExpression) {
			serializePathExpression((PathExpression) exp, sb);
		} else if (exp instanceof SubgraphExpression) {
			serializeSubgraphExpression((SubgraphExpression) exp, sb);
		} else if (exp instanceof ValueConstruction) {
			serializeValueConstruction((ValueConstruction) exp, sb);
		} else {
			throw new Greql2Exception("Unknown Expression " + exp + ".");
		}
		if (addSpace) {
			sb.append(' ');
		}
	}

	private void serializeValueConstruction(ValueConstruction exp,
			StringBuffer sb) {
		if (exp instanceof BagConstruction) {
			serializeBagConstruction((BagConstruction) exp, sb);
		} else if (exp instanceof ListConstruction) {
			serializeListConstruction((ListConstruction) exp, sb);
		} else if (exp instanceof MapConstruction) {
			serializeMapConstruction((MapConstruction) exp, sb);
		} else if (exp instanceof PathConstruction) {
			serializePathConstruction((PathConstruction) exp, sb);
		} else if (exp instanceof PathSystemConstruction) {
			serializePathSystemConstruction((PathSystemConstruction) exp, sb);
		} else if (exp instanceof RecordConstruction) {
			serializeRecordConstruction((RecordConstruction) exp, sb);
		} else if (exp instanceof SetConstruction) {
			serializeSetConstruction((SetConstruction) exp, sb);
		} else if (exp instanceof TupleConstruction) {
			serializeTupleConstruction((TupleConstruction) exp, sb, false);
		} else {
			throw new Greql2Exception("Unknown ValueConstruction " + exp + ".");
		}
	}

	private void serializeTupleConstruction(TupleConstruction exp,
			StringBuffer sb, boolean implicit) {
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
			serializeExpression(val, sb, false);
		}
		if (!implicit) {
			sb.append(")");
		}
	}

	private void serializeSetConstruction(SetConstruction exp, StringBuffer sb) {
		sb.append("set(");
		boolean first = true;
		for (Expression val : exp.getPartList()) {
			if (first) {
				first = false;
			} else {
				sb.append(", ");
			}
			serializeExpression(val, sb, false);
		}
		sb.append(")");
	}

	private void serializeRecordConstruction(RecordConstruction exp,
			StringBuffer sb) {
		// TODO Auto-generated method stub

	}

	private void serializePathSystemConstruction(PathSystemConstruction exp,
			StringBuffer sb) {
		// TODO Auto-generated method stub

	}

	private void serializePathConstruction(PathConstruction exp, StringBuffer sb) {
		// TODO Auto-generated method stub

	}

	private void serializeMapConstruction(MapConstruction exp, StringBuffer sb) {
		sb.append("map(");
		List<? extends Expression> keys = exp.getKeyExprList();
		List<? extends Expression> vals = exp.getValueExprList();

		for (int i = 0; i < keys.size(); i++) {
			if (i > 0) {
				sb.append(", ");
			}
			serializeExpression(keys.get(i), sb, true);
			sb.append("-> ");
			serializeExpression(vals.get(i), sb, false);
		}

		sb.append(")");
	}

	private void serializeListConstruction(ListConstruction exp, StringBuffer sb) {
		sb.append("list(");
		if (exp instanceof ListRangeConstruction) {
			ListRangeConstruction lrc = (ListRangeConstruction) exp;
			serializeExpression(lrc.getFirstValueList().get(0), sb, false);
			sb.append("..");
			serializeExpression(lrc.getLastValueList().get(0), sb, false);
		} else {
			boolean first = true;
			for (Expression val : exp.getPartList()) {
				if (first) {
					first = false;
				} else {
					sb.append(", ");
				}
				serializeExpression(val, sb, false);
			}
		}
		sb.append(")");
	}

	private void serializeBagConstruction(BagConstruction exp, StringBuffer sb) {
		sb.append("bag(");
		boolean first = true;
		for (Expression val : exp.getPartList()) {
			if (first) {
				first = false;
			} else {
				sb.append(", ");
			}
			serializeExpression(val, sb, false);
		}
		sb.append(")");
	}

	private void serializeSubgraphExpression(SubgraphExpression exp,
			StringBuffer sb) {
		// TODO Auto-generated method stub

	}

	private void serializePathExpression(PathExpression exp, StringBuffer sb) {
		if (exp instanceof BackwardVertexSet) {
			serializeBackwardVertexSet((BackwardVertexSet) exp, sb);
		} else if (exp instanceof ForwardVertexSet) {
			serializeForwardVertexSet((ForwardVertexSet) exp, sb);
		} else if (exp instanceof PathExistence) {
			serializePathExistence((PathExistence) exp, sb);
		} else {
			throw new Greql2Exception("Unknown PathExpression " + exp + ".");
		}
	}

	private void serializePathExistence(PathExistence exp, StringBuffer sb) {
		serializeExpression(exp.getStartExprList().get(0), sb, true);
		serializeExpression(exp.getPathList().get(0), sb, true);
		serializeExpression(exp.getTargetExprList().get(0), sb, false);
	}

	private void serializeForwardVertexSet(ForwardVertexSet exp, StringBuffer sb) {
		serializeExpression(exp.getStartExprList().get(0), sb, true);
		serializeExpression(exp.getPathList().get(0), sb, false);
	}

	private void serializeBackwardVertexSet(BackwardVertexSet exp,
			StringBuffer sb) {
		serializeExpression(exp.getPathList().get(0), sb, true);
		serializeExpression(exp.getTargetExprList().get(0), sb, false);
	}

	private void serializePathDescription(PathDescription exp, StringBuffer sb) {
		if (!exp.getStartRestrList().isEmpty()) {
			sb.append("{");
			serializeExpression(exp.getStartRestrList().get(0), sb, false);
			sb.append("} & ");
		}

		if (exp instanceof AlternativePathDescription) {
			serializeAlternativePathDescription(
					(AlternativePathDescription) exp, sb);
		} else if (exp instanceof ExponentiatedPathDescription) {
			serializeExponentiatedPathDescription(
					(ExponentiatedPathDescription) exp, sb);
		} else if (exp instanceof IntermediateVertexPathDescription) {
			serializeIntermediateVertexPathDescription(
					(IntermediateVertexPathDescription) exp, sb);
		} else if (exp instanceof IteratedPathDescription) {
			serializeIteratedPathDescription((IteratedPathDescription) exp, sb);
		} else if (exp instanceof OptionalPathDescription) {
			serializeOptionalPathDescription((OptionalPathDescription) exp, sb);
		} else if (exp instanceof SequentialPathDescription) {
			serializeSequentialPathDescription((SequentialPathDescription) exp,
					sb);
		} else if (exp instanceof TransposedPathDescription) {
			serializeTransposedPathDescription((TransposedPathDescription) exp,
					sb);
		} else if (exp instanceof PrimaryPathDescription) {
			serializePrimaryPathDescription((PrimaryPathDescription) exp, sb);
		} else {
			throw new Greql2Exception("Unknown PathDescription " + exp + ".");
		}

		if (!exp.getGoalRestrList().isEmpty()) {
			sb.append(" & {");
			serializeExpression(exp.getGoalRestrList().get(0), sb, false);
			sb.append("}");
		}
		sb.append(' ');
	}

	private void serializePrimaryPathDescription(PrimaryPathDescription exp,
			StringBuffer sb) {
		if (exp instanceof EdgePathDescription) {
			serializeEdgePathDescription((EdgePathDescription) exp, sb);
		} else if (exp instanceof SimplePathDescription) {
			serializeSimplePathDescription((SimplePathDescription) exp, sb);
		} else if (exp instanceof AggregationPathDescription) {
			serializeAggregationPathDescription(
					(AggregationPathDescription) exp, sb);
		} else {
			throw new Greql2Exception("Unknown PrimaryPathDescription " + exp
					+ ".");
		}

		if (!exp.getEdgeRestrList().isEmpty()) {
			sb.append("{");
			for (EdgeRestriction er : exp.getEdgeRestrList()) {
				serializeEdgeRestriction(er, sb);
			}
			sb.append("}");
		}
	}

	private void serializeAggregationPathDescription(
			AggregationPathDescription exp, StringBuffer sb) {
		String a = "--";
		if (exp.is_outAggregation()) {
			a = "<>" + a;
		} else {
			a = a + "<>";
		}

		String dir = exp.getDirectionList().get(0).get_dirValue();
		if (dir.equals("out")) {
			sb.append(a);
			sb.append(">");
		} else if (dir.equals("in")) {
			sb.append("<");
			sb.append(a);
		} else {
			sb.append("<");
			sb.append(a);
			sb.append(">");
		}
	}

	private void serializeSimplePathDescription(SimplePathDescription exp,
			StringBuffer sb) {
		String dir = exp.getDirectionList().get(0).get_dirValue();
		if (dir.equals("out")) {
			sb.append("-->");
		} else if (dir.equals("in")) {
			sb.append("<--");
		} else {
			sb.append("<->");
		}
	}

	private void serializeEdgePathDescription(EdgePathDescription exp,
			StringBuffer sb) {
		// TODO Auto-generated method stub

	}

	private void serializeTransposedPathDescription(
			TransposedPathDescription exp, StringBuffer sb) {
		// TODO Auto-generated method stub

	}

	private void serializeSequentialPathDescription(
			SequentialPathDescription exp, StringBuffer sb) {
		for (PathDescription pd : exp.getSequenceElementList()) {
			serializePathDescription(pd, sb);
		}
	}

	private void serializeOptionalPathDescription(OptionalPathDescription exp,
			StringBuffer sb) {
		// TODO Auto-generated method stub

	}

	private void serializeIteratedPathDescription(IteratedPathDescription exp,
			StringBuffer sb) {
		// TODO Auto-generated method stub

	}

	private void serializeIntermediateVertexPathDescription(
			IntermediateVertexPathDescription exp, StringBuffer sb) {
		// TODO Auto-generated method stub

	}

	private void serializeExponentiatedPathDescription(
			ExponentiatedPathDescription exp, StringBuffer sb) {
		// TODO Auto-generated method stub

	}

	private void serializeAlternativePathDescription(
			AlternativePathDescription exp, StringBuffer sb) {
		boolean first = true;
		for (PathDescription a : exp.getAlternatePathList()) {
			if (first) {
				first = false;
			} else {
				sb.append(" | ");
			}
			serializePathDescription(a, sb);
		}
	}

	private void serializeElementSetExpression(ElementSetExpression exp,
			StringBuffer sb) {
		// TODO Auto-generated method stub

	}

	private void serializeDefinitionExpression(DefinitionExpression exp,
			StringBuffer sb) {
		// TODO Auto-generated method stub

	}

	private void serializeComprehension(Comprehension exp, StringBuffer sb) {
		serializeDeclaration(exp.getCompDeclList().get(0), sb);
		if (exp instanceof SetComprehension) {
			sb.append("reportSet ");
		} else if (exp instanceof BagComprehension) {
			sb.append("report ");
		} else if (exp instanceof TableComprehension) {
			sb.append("reportTable ");
		} else if (exp instanceof MapComprehension) {
			sb.append("reportMap ");
		} else {
			throw new Greql2Exception("Unknown Comprehension " + exp + ".");
		}

		Expression result = exp.getCompResultDefList().get(0);

		if (result instanceof TupleConstruction) {
			// here the tup() can be omitted
			serializeTupleConstruction((TupleConstruction) result, sb, true);
			sb.append(' ');
		} else {
			serializeExpression(result, sb, true);
		}
		sb.append("end");
	}

	private void serializeRestrictedExpression(RestrictedExpression exp,
			StringBuffer sb) {
		// TODO Auto-generated method stub

	}

	private void serializeQuantifiedExpression(QuantifiedExpression exp,
			StringBuffer sb) {
		// TODO Auto-generated method stub

	}

	private void serializeLiteral(Literal exp, StringBuffer sb) {
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

	private void serializeIdentifier(Identifier exp, StringBuffer sb) {
		sb.append(exp.get_name());
	}

	private void serializeFunctionApplication(FunctionApplication exp,
			StringBuffer sb) {
		// TODO: use operator notation for +-*/
		serializeIdentifier(exp.getFunctionIdList().get(0), sb);
		sb.append('(');
		boolean first = true;
		for (Expression arg : exp.getArgumentList()) {
			if (first) {
				first = false;
			} else {
				sb.append(", ");
			}
			serializeExpression(arg, sb, false);
		}
		sb.append(")");
	}

	private void serializeConditionalExpression(
			ConditionalExpression expression, StringBuffer sb) {
		serializeExpression(expression.getConditionList().get(0), sb, true);
		sb.append("? ");
		serializeExpression(expression.getTrueExprList().get(0), sb, true);
		sb.append(": ");
		serializeExpression(expression.getFalseExprList().get(0), sb, true);
		sb.append(": ");
		serializeExpression(expression.getNullExprList().get(0), sb, false);
	}

	private void serializeVariable(Variable v, StringBuffer sb) {
		sb.append(v.get_name());
	}
}
