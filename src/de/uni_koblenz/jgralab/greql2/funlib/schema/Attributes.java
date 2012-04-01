/*
 * JGraLab - The Java Graph Laboratory
 *
 * Copyright (C) 2006-2012 Institute for Software Technology
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
package de.uni_koblenz.jgralab.greql2.funlib.schema;

import org.pcollections.PMap;
import org.pcollections.PVector;

import de.uni_koblenz.jgralab.AttributedElement;
import de.uni_koblenz.jgralab.JGraLab;
import de.uni_koblenz.jgralab.greql2.funlib.Description;
import de.uni_koblenz.jgralab.greql2.funlib.Function;
import de.uni_koblenz.jgralab.schema.Attribute;
import de.uni_koblenz.jgralab.schema.AttributedElementClass;

public class Attributes extends Function {

	public Attributes() {
		super(2, 1, 1.0);
	}

	@Description(params = "cls", description = 
		"Returns the attribute names and domains of the specified schema class "
		+ "in terms of a vector containing one map per attribute with the keys name and domain.",
		categories = Category.SCHEMA_ACCESS)
	public PVector<PMap<String, String>> evaluate(
			AttributedElementClass<?, ?> cls) {
		PVector<PMap<String, String>> result = JGraLab.vector();
		for (Attribute a : cls.getAttributeList()) {
			PMap<String, String> entry = JGraLab.map();
			entry = entry.plus("name", a.getName()).plus("domain",
					a.getDomain().getQualifiedName());
			result = result.plus(entry);
		}
		return result;
	}

	@Description(params = "el", description = 
		"Returns the attribute names and domains of the specified element "
		+ "in terms of a vector containing one map per attribute with the keys name and domain.",
		categories = Category.SCHEMA_ACCESS)
	public PVector<PMap<String, String>> evaluate(AttributedElement<?, ?> el) {
		return evaluate(el.getAttributedElementClass());
	}
}
