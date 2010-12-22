package de.uni_koblenz.jgralab.utilities.tg2dot.greql2.funlib;

import java.util.ArrayList;

import de.uni_koblenz.jgralab.AttributedElement;
import de.uni_koblenz.jgralab.Graph;
import de.uni_koblenz.jgralab.graphmarker.AbstractGraphMarker;
import de.uni_koblenz.jgralab.greql2.exception.EvaluateException;
import de.uni_koblenz.jgralab.greql2.funlib.Greql2Function;
import de.uni_koblenz.jgralab.greql2.jvalue.JValue;
import de.uni_koblenz.jgralab.greql2.jvalue.JValueImpl;
import de.uni_koblenz.jgralab.greql2.jvalue.JValueType;
import de.uni_koblenz.jgralab.schema.Attribute;
import de.uni_koblenz.jgralab.schema.AttributedElementClass;

/**
 * Retrieves the domain type from a given attribute and associated
 * attributedElement.
 * 
 * <dl>
 * <dt><b>GReQL-signatures</b></dt>
 * <dd><code>STRING attributeType(element:ATTRELEM, attribute:STRING)</code></dd>
 * <dd>
 * <code>STRING attributeType(element:ATTRELEMCLASS, attribute:STRING)</code></dd>
 * <dd>&nbsp;</dd>
 * </dl>
 * <dl>
 * <dt></dt>
 * <dd>
 * <dl>
 * <dt><b>Parameters:</b></dt>
 * <dd><code>element</code> - AttributedElement or AttributedElementClass to
 * which the attribute belongs to.</dd>
 * <dd><code>attribute</code> - Attribute name as String.</dd>
 * <dt><b>Returns:</b></dt>
 * <dd>Domain type of the given Attribute.</dd>
 * </dl>
 * </dd>
 * </dl>
 * 
 * @author ist@uni-koblenz.de
 * 
 */
public class AttributeType extends Greql2Function {

	{
		JValueType[][] x = {
				{ JValueType.ATTRELEM, JValueType.STRING, JValueType.STRING },
				{ JValueType.ATTRELEMCLASS, JValueType.STRING,
						JValueType.STRING } };
		signatures = x;

		description = "Returns the domain type of a given attribute as string.";

		Category[] c = { Category.SCHEMA_ACCESS };
		categories = c;
	}

	@Override
	public JValue evaluate(Graph graph,
			AbstractGraphMarker<AttributedElement> subgraph, JValue[] arguments)
			throws EvaluateException {

		String attributeName;
		AttributedElementClass clazz = null;
		switch (checkArguments(arguments)) {
		case 0:

			AttributedElement element = arguments[0].toAttributedElement();
			clazz = element.getAttributedElementClass();
		case 1:
			if (clazz == null) {
				arguments[0].toAttributedElementClass();
			}
			attributeName = arguments[1].toString();

			Attribute attribute = clazz.getAttribute(attributeName);
			String domainName = attribute.getDomain().getQualifiedName();

			return new JValueImpl(domainName);
		default:
			throw new RuntimeException();
		}
	}

	@Override
	public long getEstimatedCardinality(int inElements) {
		return 1;
	}

	@Override
	public long getEstimatedCosts(ArrayList<Long> inElements) {
		return 1;
	}

	@Override
	public double getSelectivity() {
		return 1;
	}

}