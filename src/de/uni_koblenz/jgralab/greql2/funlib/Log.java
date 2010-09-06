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
import de.uni_koblenz.jgralab.greql2.jvalue.JValueType;

/**
 * @author Tassilo Horn &lt;horn@uni-koblenz.de&gt;
 * 
 */
public class Log extends Greql2Function {
	{
		JValueType[][] x = {
				{ JValueType.STRING, JValueType.ATTRELEMCLASS,
						JValueType.ATTRELEMCLASS },
				{ JValueType.STRING, JValueType.BOOL, JValueType.BOOL },
				{ JValueType.STRING, JValueType.COLLECTION,
						JValueType.COLLECTION },
				{ JValueType.STRING, JValueType.DOUBLE, JValueType.DOUBLE },
				{ JValueType.STRING, JValueType.EDGE, JValueType.EDGE },
				{ JValueType.STRING, JValueType.ENUMVALUE, JValueType.ENUMVALUE },
				{ JValueType.STRING, JValueType.GRAPH, JValueType.GRAPH },
				{ JValueType.STRING, JValueType.INT, JValueType.INT },
				{ JValueType.STRING, JValueType.LONG, JValueType.LONG },
				{ JValueType.STRING, JValueType.MAP, JValueType.MAP },
				{ JValueType.STRING, JValueType.RECORD, JValueType.RECORD },
				{ JValueType.STRING, JValueType.STRING, JValueType.STRING },
				{ JValueType.STRING, JValueType.VERTEX, JValueType.VERTEX } };
		signatures = x;

		description = "Prints arg1 = arg2.toString() on stdout and returns arg2 unchanged.";

		Category[] c = { Category.DEBUGGING };
		categories = c;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * de.uni_koblenz.jgralab.greql2.funlib.Greql2Function#evaluate(de.uni_koblenz
	 * .jgralab.Graph, de.uni_koblenz.jgralab.graphmarker.BooleanGraphMarker,
	 * de.uni_koblenz.jgralab.greql2.jvalue.JValue[])
	 */
	@Override
	public JValue evaluate(Graph graph, AbstractGraphMarker<AttributedElement> subgraph,
			JValue[] arguments) throws EvaluateException {
		if (checkArguments(arguments) == -1) {
			throw new WrongFunctionParameterException(null, arguments);
		}
		System.out.println(arguments[0].toString() + " = "
				+ arguments[1].toString());
		return arguments[1];
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * de.uni_koblenz.jgralab.greql2.funlib.Greql2Function#getEstimatedCardinality
	 * (int)
	 */
	@Override
	public long getEstimatedCardinality(int inElements) {
		return 1;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * de.uni_koblenz.jgralab.greql2.funlib.Greql2Function#getEstimatedCosts
	 * (java.util.ArrayList)
	 */
	@Override
	public long getEstimatedCosts(ArrayList<Long> inElements) {
		return 1;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.uni_koblenz.jgralab.greql2.funlib.Greql2Function#getSelectivity()
	 */
	@Override
	public double getSelectivity() {
		return 1;
	}

}
