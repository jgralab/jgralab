/**
 * 
 */
package de.uni_koblenz.jgralab.greql2.funlib;

import java.util.ArrayList;

import de.uni_koblenz.jgralab.Graph;
import de.uni_koblenz.jgralab.graphmarker.BooleanGraphMarker;
import de.uni_koblenz.jgralab.greql2.exception.EvaluateException;
import de.uni_koblenz.jgralab.greql2.exception.WrongFunctionParameterException;
import de.uni_koblenz.jgralab.greql2.jvalue.JValue;
import de.uni_koblenz.jgralab.greql2.jvalue.JValueCollection;
import de.uni_koblenz.jgralab.greql2.jvalue.JValueList;
import de.uni_koblenz.jgralab.greql2.jvalue.JValueType;

/**
 * @author Tassilo Horn &lt;horn@uni-koblenz.de&gt;
 * 
 */
public class Concat extends Greql2Function {
	{
		JValueType[][] x = {
				{ JValueType.STRING, JValueType.STRING, JValueType.STRING },
				{ JValueType.COLLECTION, JValueType.COLLECTION,
						JValueType.COLLECTION } };
		signatures = x;

		description = "Concatenates the given two strings or collections.\n"
				+ "Alternative usage: str1 ++ str2, lst1 ++ lst2\n"
				+ "With collections, the return value is a list.";

		Category[] c = { Category.STRINGS, Category.COLLECTIONS_AND_MAPS };
		categories = c;
	}

	@Override
	public JValue evaluate(Graph graph, BooleanGraphMarker subgraph,
			JValue[] arguments) throws EvaluateException {
		switch (checkArguments(arguments)) {
		case 0:
			return new JValue(arguments[0].toString() + arguments[1].toString());
		case 1:
			JValueCollection c1 = arguments[0].toCollection();
			JValueCollection c2 = arguments[1].toCollection();
			JValueList l = new JValueList(c1.size() + c2.size());
			l.addAll(c1);
			l.addAll(c2);
			return l;
		default:
			throw new WrongFunctionParameterException(this, arguments);
		}
	}

	@Override
	public long getEstimatedCardinality(int inElements) {
		return 1;
	}

	@Override
	public long getEstimatedCosts(ArrayList<Long> inElements) {
		return 3;
	}

	@Override
	public double getSelectivity() {
		return 1;
	}

}
