/**
 * 
 */
package de.uni_koblenz.jgralab.greql2;

import java.util.List;

import de.uni_koblenz.jgralab.WorkInProgress;
import de.uni_koblenz.jgralab.greql2.exception.Greql2Exception;
import de.uni_koblenz.jgralab.greql2.schema.BoolLiteral;
import de.uni_koblenz.jgralab.greql2.schema.Comprehension;
import de.uni_koblenz.jgralab.greql2.schema.ConditionalExpression;
import de.uni_koblenz.jgralab.greql2.schema.Declaration;
import de.uni_koblenz.jgralab.greql2.schema.Definition;
import de.uni_koblenz.jgralab.greql2.schema.DefinitionExpression;
import de.uni_koblenz.jgralab.greql2.schema.Direction;
import de.uni_koblenz.jgralab.greql2.schema.EdgeRestriction;
import de.uni_koblenz.jgralab.greql2.schema.EdgeVertexList;
import de.uni_koblenz.jgralab.greql2.schema.ElementSetExpression;
import de.uni_koblenz.jgralab.greql2.schema.Expression;
import de.uni_koblenz.jgralab.greql2.schema.FunctionApplication;
import de.uni_koblenz.jgralab.greql2.schema.Greql2;
import de.uni_koblenz.jgralab.greql2.schema.Greql2Expression;
import de.uni_koblenz.jgralab.greql2.schema.Greql2Vertex;
import de.uni_koblenz.jgralab.greql2.schema.Identifier;
import de.uni_koblenz.jgralab.greql2.schema.IntLiteral;
import de.uni_koblenz.jgralab.greql2.schema.Literal;
import de.uni_koblenz.jgralab.greql2.schema.NullLiteral;
import de.uni_koblenz.jgralab.greql2.schema.PathDescription;
import de.uni_koblenz.jgralab.greql2.schema.PathExpression;
import de.uni_koblenz.jgralab.greql2.schema.QuantifiedExpression;
import de.uni_koblenz.jgralab.greql2.schema.Quantifier;
import de.uni_koblenz.jgralab.greql2.schema.RealLiteral;
import de.uni_koblenz.jgralab.greql2.schema.RecordElement;
import de.uni_koblenz.jgralab.greql2.schema.RestrictedExpression;
import de.uni_koblenz.jgralab.greql2.schema.SimpleDeclaration;
import de.uni_koblenz.jgralab.greql2.schema.StringLiteral;
import de.uni_koblenz.jgralab.greql2.schema.SubgraphExpression;
import de.uni_koblenz.jgralab.greql2.schema.ThisEdge;
import de.uni_koblenz.jgralab.greql2.schema.ThisVertex;
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
			serializeExpression((Expression) v, sb);
		} else {
			throw new Greql2Exception("Unknown Greql2Vertex " + v + ".");
		}
	}

	private void serializeSimpleDeclaration(SimpleDeclaration v, StringBuffer sb) {
		// TODO Auto-generated method stub

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
		// TODO Auto-generated method stub

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

		serializeExpression(greql2Expression.getQueryExprList().get(0), sb);
	}

	private void serializeExpression(Expression exp, StringBuffer sb) {
		if (exp instanceof ConditionalExpression) {
			serializeConditionalExpression((ConditionalExpression) exp, sb);
		} else if (exp instanceof FunctionApplication) {
			serializeFunctionApplication((FunctionApplication) exp, sb);
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
	}

	private void serializeValueConstruction(ValueConstruction exp,
			StringBuffer sb) {
		// TODO Auto-generated method stub

	}

	private void serializeSubgraphExpression(SubgraphExpression exp,
			StringBuffer sb) {
		// TODO Auto-generated method stub

	}

	private void serializePathExpression(PathExpression exp, StringBuffer sb) {
		// TODO Auto-generated method stub

	}

	private void serializePathDescription(PathDescription exp, StringBuffer sb) {
		// TODO Auto-generated method stub

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
		// TODO Auto-generated method stub

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
			sb.append(((BoolLiteral) exp).get_boolValue());
		} else if (exp instanceof IntLiteral) {
			sb.append(((IntLiteral) exp).get_intValue());
		} else if (exp instanceof NullLiteral) {
			sb.append("null");
		} else if (exp instanceof RealLiteral) {
			sb.append(((RealLiteral) exp).get_realValue());
		} else if (exp instanceof StringLiteral) {
			sb.append(((StringLiteral) exp).get_stringValue());
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
		// TODO Auto-generated method stub

	}

	private void serializeConditionalExpression(
			ConditionalExpression expression, StringBuffer sb) {
		// TODO Auto-generated method stub

	}

	private void serializeVariable(Variable v, StringBuffer sb) {
		sb.append(v.get_name());
	}
}
