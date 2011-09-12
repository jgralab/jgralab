/**
 * 
 */
package de.uni_koblenz.jgralab.greql2.funlib;

import java.util.ArrayList;


import de.uni_koblenz.jgralab.Graph;
import de.uni_koblenz.jgralab.graphmarker.SubGraphMarker;
import de.uni_koblenz.jgralab.greql2.exception.EvaluateException;
import de.uni_koblenz.jgralab.greql2.exception.WrongFunctionParameterException;
import de.uni_koblenz.jgralab.greql2.jvalue.JValue;
import de.uni_koblenz.jgralab.greql2.jvalue.JValueType;

/**
 * @author Tassilo Horn &lt;horn@uni-koblenz.de&gt;
 * 
 */
public class Error extends Greql2Function {
	{
		JValueType[][] x = { { JValueType.OBJECT, JValueType.STRING } };
		signatures = x;
		description = "Throws a GReQL EvaluateException with the given message.";

		Category[] c = { Category.DEBUGGING };
		categories = c;
	}

	@Override
	public JValue evaluate(Graph graph,
			SubGraphMarker subgraph, JValue[] arguments)
			throws EvaluateException {
		if (checkArguments(arguments) < 0) {
			throw new WrongFunctionParameterException(this, arguments);
		}
		throw new EvaluateException(arguments[0].toString());
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
		return 0.5;
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

}
