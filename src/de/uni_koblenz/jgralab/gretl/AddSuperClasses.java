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

import de.uni_koblenz.jgralab.schema.EdgeClass;
import de.uni_koblenz.jgralab.schema.GraphElementClass;
import de.uni_koblenz.jgralab.schema.VertexClass;

public class AddSuperClasses extends Transformation<GraphElementClass<?, ?>> {
	private GraphElementClass<?, ?> subClass;
	private GraphElementClass<?, ?>[] superClasses;

	public AddSuperClasses(final Context c, final VertexClass subClass,
			final VertexClass... superClasses) {
		super(c);
		this.subClass = subClass;
		this.superClasses = superClasses;
	}

	public AddSuperClasses(final Context c, final EdgeClass subClass,
			final EdgeClass... superClasses) {
		super(c);
		this.subClass = subClass;
		this.superClasses = superClasses;
	}

	public static AddSuperClasses parseAndCreate(ExecuteTransformation et) {
		GraphElementClass<?, ?> subGec = et.matchGraphElementClass();
		if (subGec instanceof VertexClass) {
			VertexClass[] superVCs = et.matchVertexClassArray();
			return new AddSuperClasses(et.context, (VertexClass) subGec,
					superVCs);
		} else {
			EdgeClass[] superECs = et.matchEdgeClassArray();
			return new AddSuperClasses(et.context, (EdgeClass) subGec, superECs);
		}
	}

	@Override
	protected GraphElementClass<?, ?> transform() {
		for (GraphElementClass<?, ?> superCls : superClasses) {
			if (subClass instanceof VertexClass) {
				new AddSuperClass(context, (VertexClass) subClass,
						(VertexClass) superCls).execute();
			} else {
				new AddSuperClass(context, (EdgeClass) subClass,
						(EdgeClass) superCls).execute();
			}
		}

		return subClass;
	}

}
