/*
 * JGraLab - The Java Graph Laboratory
 *
 * Copyright (C) 2006-2013 Institute for Software Technology
 *                         University of Koblenz-Landau, Germany
 *                         ist@uni-koblenz.de
 *
 * For bug reports, documentation and further information, visit
 *
 *                         https://github.com/jgralab/jgralab
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
package de.uni_koblenz.jgralab.greql.funlib.schema;

import de.uni_koblenz.jgralab.GraphElement;
import de.uni_koblenz.jgralab.greql.exception.GreqlException;
import de.uni_koblenz.jgralab.greql.funlib.Description;
import de.uni_koblenz.jgralab.greql.funlib.Function;
import de.uni_koblenz.jgralab.greql.types.TypeCollection;
import de.uni_koblenz.jgralab.schema.GraphElementClass;

public class HasType extends Function {

	public HasType() {
		super();
	}

	@Description(params = {"el","qn"}, description = 
		"Returns true, iff the given attributed element has an attributed element class with the given qualified name.",
				categories = Category.SCHEMA_ACCESS)
	public <SC extends GraphElementClass<SC, IC>, IC extends GraphElement<SC, IC>> Boolean evaluate(
			IC el, String qn) {
		SC aec = el.getSchema().getAttributedElementClass(qn);
		if (aec == null) {
			throw new GreqlException("hasType: Schema doesn't contain a type '");
		}
		return evaluate(el, aec);
	}

	@Description(params = {"el","aec"}, description = 
		"Returns true, iff the given attributed element has the given attributed element class",
				categories = Category.SCHEMA_ACCESS)
	private <SC extends GraphElementClass<SC, IC>, IC extends GraphElement<SC, IC>> Boolean evaluate(
			IC el, SC aec) {
		SC c = el.getAttributedElementClass();
		return c.equals(aec) || c.isSubClassOf(aec);
	}

	@Description(params = {"el", "tc"}, description = 
		"Returns true, iff the given attributed element has an attributed element class accepted by the given type collection.",
				categories = Category.SCHEMA_ACCESS)
	public <SC extends GraphElementClass<SC, IC>, IC extends GraphElement<SC, IC>> Boolean evaluate(
			IC el, TypeCollection tc) {
		SC c = el.getAttributedElementClass();
		return tc.acceptsType(c);
	}

}

/*

*/
