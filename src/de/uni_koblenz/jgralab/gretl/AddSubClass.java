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
package de.uni_koblenz.jgralab.gretl;

import de.uni_koblenz.jgralab.gretl.Context.TransformationPhase;
import de.uni_koblenz.jgralab.schema.EdgeClass;
import de.uni_koblenz.jgralab.schema.GraphElementClass;
import de.uni_koblenz.jgralab.schema.VertexClass;

public class AddSubClass extends Transformation<GraphElementClass<?, ?>> {

	private GraphElementClass<?, ?> superClass;
	private GraphElementClass<?, ?> subClass;

	public AddSubClass(final Context c, final VertexClass superClass,
			final VertexClass subClass) {
		super(c);
		this.superClass = superClass;
		this.subClass = subClass;
	}

	public AddSubClass(final Context c, final EdgeClass superClass,
			final EdgeClass subClass) {
		super(c);
		this.superClass = superClass;
		this.subClass = subClass;
	}

	public static AddSubClass parseAndCreate(ExecuteTransformation et) {
		GraphElementClass<?, ?> superGec = et.matchGraphElementClass();
		if (superGec instanceof VertexClass) {
			VertexClass subVC = et.matchVertexClass();
			return new AddSubClass(et.context, (VertexClass) superGec, subVC);
		} else {
			EdgeClass subEC = et.matchEdgeClass();
			return new AddSubClass(et.context, (EdgeClass) superGec, subEC);
		}
	}

	@Override
	protected GraphElementClass<?, ?> transform() {
		if (context.phase != TransformationPhase.SCHEMA) {
			return superClass;
		}

		if (superClass instanceof VertexClass) {
			((VertexClass) subClass).addSuperClass((VertexClass) superClass);
		} else {
			((EdgeClass) subClass).addSuperClass((EdgeClass) superClass);
		}

		return superClass;
	}
}
