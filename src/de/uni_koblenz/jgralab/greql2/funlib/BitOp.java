/**
 * 
 */
package de.uni_koblenz.jgralab.greql2.funlib;

import java.util.ArrayList;

import de.uni_koblenz.jgralab.AttributedElement;
import de.uni_koblenz.jgralab.Graph;
import de.uni_koblenz.jgralab.graphmarker.AbstractGraphMarker;
import de.uni_koblenz.jgralab.greql2.exception.EvaluateException;
import de.uni_koblenz.jgralab.greql2.exception.WrongFunctionParameterException;
import de.uni_koblenz.jgralab.greql2.jvalue.JValue;
import de.uni_koblenz.jgralab.greql2.jvalue.JValueImpl;
import de.uni_koblenz.jgralab.greql2.jvalue.JValueType;

/**
 * @author Tassilo Horn &lt;horn@uni-koblenz.de&gt;
 * 
 */
public class BitOp extends Greql2Function {
	{
		JValueType[][] x = {
				// Binary ops
				{ JValueType.STRING, JValueType.INT, JValueType.INT,
						JValueType.INT },
				// Unary ops
				{ JValueType.STRING, JValueType.INT, JValueType.INT } };
		signatures = x;
		description = "Performs the bitwise op on args.\n"
				+ "Supported unary ops: " + UnaryBitOps.getVals() + ",\n"
				+ "Supported binary ops: " + BinaryBitOps.getVals();

		Category[] c = { Category.ARITHMETICS };
		categories = c;
	}

	private enum UnaryBitOps {
		NOT;

		static String getVals() {
			StringBuilder sb = new StringBuilder();
			boolean first = true;
			for (UnaryBitOps op : values()) {
				if (first) {
					first = false;
				} else {
					sb.append(", ");
				}
				sb.append(op.toString());
			}
			return sb.toString();
		}
	}

	private enum BinaryBitOps {
		AND, OR, XOR, SHIFT_LEFT, SHIFT_RIGHT, UNSIGNED_SHIFT_RIGHT;

		static String getVals() {
			StringBuilder sb = new StringBuilder();
			boolean first = true;
			for (BinaryBitOps op : values()) {
				if (first) {
					first = false;
				} else {
					sb.append(", ");
				}
				sb.append(op.toString());
			}
			return sb.toString();
		}
	}

	@Override
	public JValue evaluate(Graph graph,
			AbstractGraphMarker<AttributedElement> subgraph, JValue[] arguments)
			throws EvaluateException {
		switch (checkArguments(arguments)) {
		case 0:
			BinaryBitOps binOp = BinaryBitOps.valueOf(arguments[0].toString());
			switch (binOp) {
			case AND:
				return new JValueImpl(arguments[1].toInteger()
						& arguments[2].toInteger());
			case OR:
				return new JValueImpl(arguments[1].toInteger()
						| arguments[2].toInteger());
			case XOR:
				return new JValueImpl(arguments[1].toInteger()
						^ arguments[2].toInteger());
			case SHIFT_LEFT:
				return new JValueImpl(
						arguments[1].toInteger() << arguments[2].toInteger());
			case SHIFT_RIGHT:
				return new JValueImpl(
						arguments[1].toInteger() >> arguments[2].toInteger());
			case UNSIGNED_SHIFT_RIGHT:
				return new JValueImpl(
						arguments[1].toInteger() >>> arguments[2].toInteger());
			default:
				throw new WrongFunctionParameterException(this, arguments);
			}
		case 1:
			UnaryBitOps op = UnaryBitOps.valueOf(arguments[0].toString());
			switch (op) {
			case NOT:
				int x = arguments[1].toInteger();
				return new JValueImpl(~x);
			default:
				throw new WrongFunctionParameterException(this, arguments);
			}
		default:
			throw new WrongFunctionParameterException(this, arguments);
		}
	}

	@Override
	public long getEstimatedCosts(ArrayList<Long> inElements) {
		// TODO Auto-generated method stub
		return 1;
	}

	@Override
	public double getSelectivity() {
		return 1;
	}

	@Override
	public long getEstimatedCardinality(int inElements) {
		return 1;
	}

}
