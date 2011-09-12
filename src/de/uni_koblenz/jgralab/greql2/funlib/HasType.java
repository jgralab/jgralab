/*
 * JGraLab - The Java Graph Laboratory
 * 
 * Copyright (C) 2006-2011 Institute for Software Technology
 *                         University of Koblenz-Landau, Germany
 *                         ist@uni-koblenz.de
 * 
 * For bug reports, documentation and further information, visit
 * 
 *                         http://jgralab.uni-koblenz.de
 * 
 * This program is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as published by the
 * Free Software Foundation; either version 3 of the License, or (at your
 * option) any later version.
 * 
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General
 * Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, see <http://www.gnu.org/licenses>.
 * 
 * Additional permission under GNU GPL version 3 section 7
 * 
 * If you modify this Program, or any covered work, by linking or combining
 * it with Eclipse (or a modified version of that program or an Eclipse
 * plugin), containing parts covered by the terms of the Eclipse Public
 * License (EPL), the licensors of this Program grant you additional
 * permission to convey the resulting work.  Corresponding Source for a
 * non-source form of such a combination shall include the source code for
 * the parts of JGraLab used as well as that of the covered work.
 */

package de.uni_koblenz.jgralab.greql2.funlib;

import java.util.ArrayList;

import de.uni_koblenz.jgralab.AttributedElement;
import de.uni_koblenz.jgralab.Graph;
import de.uni_koblenz.jgralab.graphmarker.SubGraphMarker;
import de.uni_koblenz.jgralab.greql2.exception.EvaluateException;
import de.uni_koblenz.jgralab.greql2.exception.WrongFunctionParameterException;
import de.uni_koblenz.jgralab.greql2.jvalue.JValue;
import de.uni_koblenz.jgralab.greql2.jvalue.JValueImpl;
import de.uni_koblenz.jgralab.greql2.jvalue.JValueType;
import de.uni_koblenz.jgralab.greql2.jvalue.JValueTypeCollection;
import de.uni_koblenz.jgralab.schema.AttributedElementClass;

public class HasType extends Greql2Function {
	{
		JValueType[][] x = {
				{ JValueType.ATTRELEM, JValueType.STRING, JValueType.BOOL },
				{ JValueType.ATTRELEM, JValueType.ATTRELEMCLASS,
						JValueType.BOOL },
				{ JValueType.ATTRELEM, JValueType.TYPECOLLECTION,
						JValueType.BOOL } };
		signatures = x;

		description = "Checks if the given AttrElem has the given type.\n"
				+ "The type may be given as qualified name (String), as\n"
				+ "TypeCollection, or as AttributedElementClass.";

		Category[] c = { Category.SCHEMA_ACCESS };
		categories = c;
	}

	@Override
	public JValue evaluate(Graph graph, SubGraphMarker subgraph,
			JValue[] arguments) throws EvaluateException {

		if (!arguments[0].isAttributedElement()) {
			return new JValueImpl();
		}
		AttributedElement elem = arguments[0].toAttributedElement();

		switch (checkArguments(arguments)) {
		case 0:
			String typeName = arguments[1].toString();
			return hasTypeOfQualifiedName(typeName, elem);
		case 1:
			AttributedElementClass aeClass = arguments[1]
					.toAttributedElementClass();
			return hasTypeOfAttributedElementClass(aeClass, elem);
		case 2:
			JValueTypeCollection typeCollection = arguments[1]
					.toJValueTypeCollection();
			return hasTypeFromTypeCollection(typeCollection, elem);
		default:
			throw new WrongFunctionParameterException(this, arguments);
		}
	}

	private JValue hasTypeOfQualifiedName(String typeName,
			AttributedElement elem) {
		AttributedElementClass type = elem.getSchema()
				.getAttributedElementClass(typeName);
		return hasTypeOfAttributedElementClass(type, elem);
	}

	private JValue hasTypeOfAttributedElementClass(
			AttributedElementClass aeClass, AttributedElement elem) {
		return new JValueImpl((elem.getAttributedElementClass() == aeClass)
				|| elem.getAttributedElementClass().isSubClassOf(aeClass), elem);
	}

	private JValue hasTypeFromTypeCollection(
			JValueTypeCollection typeCollection, AttributedElement elem) {
		return new JValueImpl(typeCollection.acceptsType(elem
				.getAttributedElementClass()), elem);
	}

	@Override
	public long getEstimatedCosts(ArrayList<Long> inElements) {
		return 2;
	}

	@Override
	public double getSelectivity() {
		return 0.1;
	}

	@Override
	public long getEstimatedCardinality(int inElements) {
		return 1;
	}

}
