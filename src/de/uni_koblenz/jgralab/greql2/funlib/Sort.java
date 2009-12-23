/**
 * 
 */
package de.uni_koblenz.jgralab.greql2.funlib;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import de.uni_koblenz.jgralab.Graph;
import de.uni_koblenz.jgralab.graphmarker.BooleanGraphMarker;
import de.uni_koblenz.jgralab.greql2.exception.EvaluateException;
import de.uni_koblenz.jgralab.greql2.exception.WrongFunctionParameterException;
import de.uni_koblenz.jgralab.greql2.jvalue.JValue;
import de.uni_koblenz.jgralab.greql2.jvalue.JValueCollection;
import de.uni_koblenz.jgralab.greql2.jvalue.JValueList;
import de.uni_koblenz.jgralab.greql2.jvalue.JValueType;

/**
 * Returns a list sorted by natural ordering of the given collection.
 * <dl>
 * <dt><b>GReQL-signature</b></dt>
 * <dd><code>LIST sort(a: COLLECTION)</code></dd>
 * <dd></dd>
 * </dl>
 * <dl>
 * <dt></dt>
 * <dd>
 * <dl>
 * <dt><b>Parameters:</b></dt>
 * <dd><code>c</code> - a COLLECTION of something implementing Comparable
 * (Strings, Numbers...)</dd>
 * <dt><b>Returns:</b></dt>
 * <dd>a list sorted naturally</dd>
 * </dl>
 * 
 * @author Tassilo Horn &lt;horn@uni-koblenz.de&gt;
 * 
 */
public class Sort extends Greql2Function {
	{
		JValueType[][] x = { { JValueType.COLLECTION } };
		signatures = x;
	}

	@Override
	public JValue evaluate(Graph graph, BooleanGraphMarker subgraph,
			JValue[] arguments) throws EvaluateException {
		switch (checkArguments(arguments)) {
		case 0:
			JValueCollection col = arguments[0].toCollection();
			List<JValue> l = new ArrayList<JValue>(col.size());
			for (JValue jv : col) {
				l.add(jv);
			}

			Collections.sort(l);

			JValueList jl = new JValueList(col.size());
			for (JValue jv : l) {
				jl.add(jv);
			}
			return jl;
		default:
			throw new WrongFunctionParameterException(this, arguments);
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
		return inElements;
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
		return 100;
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
