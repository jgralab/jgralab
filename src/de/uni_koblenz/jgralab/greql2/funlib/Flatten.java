/**
 * 
 */
package de.uni_koblenz.jgralab.greql2.funlib;

import java.util.ArrayList;

import de.uni_koblenz.jgralab.Graph;
import de.uni_koblenz.jgralab.graphmarker.AbstractGraphMarker;
import de.uni_koblenz.jgralab.greql2.exception.EvaluateException;
import de.uni_koblenz.jgralab.greql2.exception.WrongFunctionParameterException;
import de.uni_koblenz.jgralab.greql2.jvalue.JValue;
import de.uni_koblenz.jgralab.greql2.jvalue.JValueCollection;
import de.uni_koblenz.jgralab.greql2.jvalue.JValueList;
import de.uni_koblenz.jgralab.greql2.jvalue.JValueSet;
import de.uni_koblenz.jgralab.greql2.jvalue.JValueType;

/**
 * @author Tassilo Horn &lt;horn@uni-koblenz.de&gt;
 * 
 */
public class Flatten extends Greql2Function {
	{
		JValueType[][] x = {
				{ JValueType.COLLECTION, JValueType.COLLECTION },
				{ JValueType.COLLECTION, JValueType.BOOL, JValueType.COLLECTION } };
		signatures = x;
		description = "Returns the list calculated by flattening the given collection of collections.\n"
				+ "The optional second boolean param specifies, if the return value is a set (no duplicates).\n"
				+ "If false or not specified, a list is returned, which may contain duplicates.";

		Category[] c = { Category.COLLECTIONS_AND_MAPS };
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
	public JValue evaluate(Graph graph, AbstractGraphMarker<?> subgraph,
			JValue[] arguments) throws EvaluateException {
		JValueCollection result = null;
		switch (checkArguments(arguments)) {
		case 0:
			result = new JValueList();
			break;
		case 1:
			if (arguments[1].toBoolean().booleanValue()) {
				result = new JValueSet();
			} else {
				result = new JValueList();
			}
			break;
		default:
			throw new WrongFunctionParameterException(this, arguments);
		}
		JValueCollection coll = arguments[0].toCollection();
		flatten(coll, result);
		return result;
	}

	private void flatten(JValueCollection coll, JValueCollection res) {
		for (JValue e : coll) {
			if (e.isCollection()) {
				flatten(e.toCollection(), res);
			} else {
				res.add(e);
			}
		}
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
		return inElements * 5;
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
		long costs = 0;
		for (long l : inElements) {
			costs += l;
		}
		return costs;
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
