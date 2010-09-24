/*
 * JGraLab - The Java graph laboratory
 * (c) 2006-2010 Institute for Software Technology
 *               University of Koblenz-Landau, Germany
 * 
 *               ist@uni-koblenz.de
 * 
 * Please report bugs to http://serres.uni-koblenz.de/bugzilla
 * 
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
/**
 * 
 */
package de.uni_koblenz.jgralab.greql2.funlib;

import java.util.ArrayList;

import de.uni_koblenz.jgralab.AttributedElement;
import de.uni_koblenz.jgralab.Graph;
import de.uni_koblenz.jgralab.GraphElement;
import de.uni_koblenz.jgralab.graphmarker.AbstractGraphMarker;
import de.uni_koblenz.jgralab.greql2.exception.EvaluateException;
import de.uni_koblenz.jgralab.greql2.exception.WrongFunctionParameterException;
import de.uni_koblenz.jgralab.greql2.jvalue.JValue;
import de.uni_koblenz.jgralab.greql2.jvalue.JValueImpl;
import de.uni_koblenz.jgralab.greql2.jvalue.JValueRecord;
import de.uni_koblenz.jgralab.greql2.jvalue.JValueTuple;
import de.uni_koblenz.jgralab.greql2.jvalue.JValueType;
import de.uni_koblenz.jgralab.schema.Attribute;
import de.uni_koblenz.jgralab.schema.AttributedElementClass;

/**
 * Describes the given {@link AttributedElement}, that is, it returns a triple.
 * The first tuple component is the qualified name of the elements
 * {@link AttributedElementClass}, the second is its ID (or a record of ID and
 * graph version, if the element is a graph). The third component is a record
 * holding all the elements attributes and their values.
 * 
 * When given no parameter, the current graph is described.
 * 
 * <dl>
 * <dt><b>GReQL-signature</b></dt>
 * <dd><code>TUPLE describe(ae:ATTRELEM)</code></dd>
 * <dd><code>TUPLE describe()</code></dd>
 * <dd>&nbsp;</dd>
 * </dl>
 * <dl>
 * <dt></dt>
 * <dd>
 * <dl>
 * <dt><b>Parameters:</b></dt>
 * <dd><code>ae</code> - the AttributedElement to describe</dd>
 * <dt><b>Returns:</b></dt>
 * <dd>a triple (AttrElemClassQName, ID, AttrRecord)</dd>
 * <dd><code>Null</code> if one of the parameters is <code>Null</code></dd>
 * </dl>
 * </dd>
 * </dl>
 * 
 * 
 * @author Tassilo Horn &lt;horn@uni-koblenz.de&gt;
 * 
 */
public class Describe extends Greql2Function {
	{
		JValueType[][] x = { { JValueType.COLLECTION },
				{ JValueType.ATTRELEM, JValueType.COLLECTION } };
		signatures = x;

		description = "Returns a tuple (qualified name, id, attributes) describing the given attributed element.\n"
				+ "If no element is given, it describes the graph itself, producing: (qualified name, (id, version), attributes).";

		Category[] c = { Category.GRAPH };
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
	public JValue evaluate(Graph graph,
			AbstractGraphMarker<AttributedElement> subgraph, JValue[] arguments)
			throws EvaluateException {
		AttributedElement attrElem = null;
		switch (checkArguments(arguments)) {
		case 0:
			attrElem = graph;
			break;
		case 1:
			attrElem = arguments[0].toAttributedElement();
			break;
		default:
			throw new WrongFunctionParameterException(this, arguments);
		}

		JValueTuple tuple = new JValueTuple();
		tuple.add(new JValueImpl(attrElem.getAttributedElementClass()
				.getQualifiedName()));
		if (attrElem instanceof Graph) {
			Graph g = (Graph) attrElem;
			JValueRecord idRecord = new JValueRecord();
			idRecord.add("id", new JValueImpl(g.getId()));
			idRecord.add("version", new JValueImpl(g.getGraphVersion()));
			tuple.add(idRecord);
		} else {
			GraphElement ge = (GraphElement) attrElem;
			tuple.add(new JValueImpl(ge.getId()));
		}
		JValueRecord attrRecord = new JValueRecord();
		for (Attribute attr : attrElem.getAttributedElementClass()
				.getAttributeList()) {

			attrRecord.add(attr.getName(), JValueImpl.fromObject(attrElem
					.getAttribute(attr.getName())));

		}
		tuple.add(attrRecord);
		return tuple;
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
		return 3;
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
