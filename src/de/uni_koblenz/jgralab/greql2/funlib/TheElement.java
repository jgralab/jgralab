/**
 *
 */
package de.uni_koblenz.jgralab.greql2.funlib;

import java.util.ArrayList;
import java.util.Iterator;

import de.uni_koblenz.jgralab.Graph;
import de.uni_koblenz.jgralab.graphmarker.BooleanGraphMarker;
import de.uni_koblenz.jgralab.greql2.exception.EvaluateException;
import de.uni_koblenz.jgralab.greql2.exception.WrongFunctionParameterException;
import de.uni_koblenz.jgralab.greql2.jvalue.JValue;
import de.uni_koblenz.jgralab.greql2.jvalue.JValueCollection;
import de.uni_koblenz.jgralab.greql2.jvalue.JValueType;

/**
 * Returns the one and only element of the given set, bag or list. If it
 * contains more than one value an {@link WrongFunctionParameterException} is
 * thrown at you.
 * 
 * <dl>
 * <dt><b>GReQL-signature</b></dt>
 * <dd><code>OBJECT theElement(list:LIST&lt;OBJECT&gt;)</code></dd>
 * <dd><code>OBJECT TheElement(set:SET&lt;OBJECT&gt;)</code></dd>
 * <dd><code>OBJECT TheElement(bag:BAG&lt;OBJECT&gt;)</code></dd>
 * <dd>&nbsp;</dd>
 * </dl>
 * <dl>
 * <dt></dt>
 * <dd>
 * <dl>
 * <dt><b>Parameters:</b></dt>
 * <dd><code>list</code> - list to return the element for</dd>
 * <dd><code>set</code> - set to return the element for</dd>
 * <dd><code>bog</code> - bag to return the element for</dd>
 * <dt><b>Returns:</b></dt>
 * <dd>the one and only element of the given list, set or bag</dd>
 * </dl>
 * </dd>
 * </dl>
 * 
 * @author ist@uni-koblenz.de
 */
public class TheElement extends Greql2Function {
	{
		JValueType[][] x = { { JValueType.COLLECTION } };
		signatures = x;
	}

	@Override
	public JValue evaluate(Graph graph, BooleanGraphMarker subgraph,
			JValue[] arguments) throws EvaluateException {
		if (checkArguments(arguments) == -1) {
			throw new WrongFunctionParameterException(this, arguments);
		}

		JValueCollection col = arguments[0].toCollection();
		// theElement is defined only for collections with exactly one
		// element.
		if (col.size() != 1) {
			throw new EvaluateException("The given collection contains "
					+ (col.size() < 1 ? "less" : "more") + " than one element!");
		}
		Iterator<JValue> it = col.iterator();
		return it.next();
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
	 * (int)
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
