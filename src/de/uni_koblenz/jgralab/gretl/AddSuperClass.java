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
package de.uni_koblenz.jgralab.gretl;

import de.uni_koblenz.jgralab.gretl.Context.TransformationPhase;
import de.uni_koblenz.jgralab.schema.EdgeClass;
import de.uni_koblenz.jgralab.schema.GraphElementClass;
import de.uni_koblenz.jgralab.schema.VertexClass;

public class AddSuperClass extends Transformation<GraphElementClass<?, ?>> {
	private GraphElementClass<?, ?> subClass;
	private GraphElementClass<?, ?> superClass;

	public AddSuperClass(final Context c, final VertexClass subClass,
			final VertexClass superClass) {
		super(c);
		this.subClass = subClass;
		this.superClass = superClass;
	}

	public AddSuperClass(final Context c, final EdgeClass subClass,
			final EdgeClass superClass) {
		super(c);
		this.subClass = subClass;
		this.superClass = superClass;
	}

	public static AddSuperClass parseAndCreate(ExecuteTransformation et) {
		GraphElementClass<?, ?> subGec = et.matchGraphElementClass();
		if (subGec instanceof VertexClass) {
			VertexClass superVC = et.matchVertexClass();
			return new AddSuperClass(et.context, (VertexClass) subGec, superVC);
		} else {
			EdgeClass superEC = et.matchEdgeClass();
			return new AddSuperClass(et.context, (EdgeClass) subGec, superEC);
		}
	}

	@Override
	protected GraphElementClass<?, ?> transform() {
		if (context.phase != TransformationPhase.SCHEMA) {
			return subClass;
		}

		if (superClass instanceof VertexClass) {
			((VertexClass) subClass).addSuperClass((VertexClass) superClass);
		} else {
			((EdgeClass) subClass).addSuperClass((EdgeClass) superClass);
		}

		return subClass;
	}

}
