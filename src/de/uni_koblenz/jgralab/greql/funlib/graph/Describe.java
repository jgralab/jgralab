/*
 * JGraLab - The Java Graph Laboratory
 *
 * Copyright (C) 2006-2014 Institute for Software Technology
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
package de.uni_koblenz.jgralab.greql.funlib.graph;

import java.util.List;

import org.pcollections.PMap;

import de.uni_koblenz.jgralab.AttributedElement;
import de.uni_koblenz.jgralab.Graph;
import de.uni_koblenz.jgralab.GraphElement;
import de.uni_koblenz.jgralab.JGraLab;
import de.uni_koblenz.jgralab.greql.funlib.Description;
import de.uni_koblenz.jgralab.greql.funlib.Function;
import de.uni_koblenz.jgralab.greql.types.Undefined;
import de.uni_koblenz.jgralab.schema.Attribute;

public class Describe extends Function {

	@Description(params = "el", description = "Returns a human-readable description of the given element.",
			categories = Category.GRAPH)
	public Describe() {
		super(10, 3, 1.0);
	}

	public PMap<String, Object> evaluate(AttributedElement<?, ?> el) {
		PMap<String, Object> result = JGraLab.map();
		result = result.plus("type", el.getAttributedElementClass()
				.getQualifiedName());
		if (el instanceof Graph) {
			result = result.plus("id", ((Graph) el).getId());
		} else {
			result = result.plus("id", ((GraphElement<?, ?>) el).getId());
		}
		List<Attribute> al = el.getAttributedElementClass().getAttributeList();
		if (al.size() > 0) {
			PMap<String, Object> attrs = JGraLab.map();
			for (Attribute a : al) {
				Object val = el.getAttribute(a.getName());
				attrs = attrs.plus(a.getName(),
						val == null ? Undefined.UNDEFINED : val);
			}
			result = result.plus("attributes", attrs);
		}
		return result;
	}
}
