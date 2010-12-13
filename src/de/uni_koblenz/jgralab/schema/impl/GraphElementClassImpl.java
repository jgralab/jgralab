/*
 * JGraLab - The Java Graph Laboratory
 * 
 * Copyright (C) 2006-2010 Institute for Software Technology
 *                         University of Koblenz-Landau, Germany
 *                         ist@uni-koblenz.de
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

package de.uni_koblenz.jgralab.schema.impl;

import de.uni_koblenz.jgralab.schema.AttributedElementClass;
import de.uni_koblenz.jgralab.schema.GraphClass;
import de.uni_koblenz.jgralab.schema.GraphElementClass;
import de.uni_koblenz.jgralab.schema.Package;
import de.uni_koblenz.jgralab.schema.VertexClass;

public abstract class GraphElementClassImpl extends AttributedElementClassImpl
		implements GraphElementClass {

	protected GraphClass graphClass;

	/**
	 * delegates its constructor to the generalized class
	 * 
	 * @param qn
	 *            the unique identifier of the element in the schema
	 */
	protected GraphElementClassImpl(String simpleName, Package pkg,
			GraphClass graphClass) {
		super(simpleName, pkg, graphClass.getSchema());
		this.graphClass = graphClass;
	}

	@Override
	public GraphClass getGraphClass() {
		return graphClass;
	}

	@Override
	public String toString() {
		StringBuilder output = new StringBuilder(this.getClass()
				.getSimpleName()
				+ " '" + getQualifiedName() + "'");
		if (isAbstract()) {
			output.append(" (abstract)");
		}
		output.append(": \n");

		output.append("subClasses of '" + getQualifiedName() + "': ");

		for (AttributedElementClass aec : getAllSubClasses()) {
			output.append("'" + aec.getQualifiedName() + "' ");
		}
		output.append("\nsuperClasses of '" + getQualifiedName() + "': ");
		for (AttributedElementClass aec : getAllSuperClasses()) {
			output.append("'" + aec.getQualifiedName() + "' ");
		}
		output.append("\ndirectSuperClasses of '" + getQualifiedName() + "': ");
		for (AttributedElementClass aec : getDirectSuperClasses()) {
			output.append("'" + aec.getQualifiedName() + "' ");
		}

		output.append(attributesToString());

		if (this instanceof VertexClass) {
			output.append("outgoing edge classes: ");
			// boolean first = true;
			// for (IncidenceClass ic : ((VertexClass) this)
			// .getOwnOutIncidenceClasses()) {
			// if (first) {
			// first = false;
			// } else {
			// output.append(", ");
			// }
			// output.append(ic.getEdgeClass().getQualifiedName());
			//
			// }
			output.append("\n");
			output.append("incomming edge classes: ");
			// first = true;
			// for (IncidenceClass ic : ((VertexClass) this)
			// .getOwnInIncidenceClasses()) {
			// if (first) {
			// first = false;
			// } else {
			// output.append(", ");
			// }
			// output.append(ic.getEdgeClass().getQualifiedName());
			//
			// }
			output.append("\n");
		}
		output.append("\n");

		return output.toString();
	}
}
