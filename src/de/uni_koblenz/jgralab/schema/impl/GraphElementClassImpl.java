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
//			boolean first = true;
//			for (IncidenceClass ic : ((VertexClass) this)
//					.getOwnOutIncidenceClasses()) {
//				if (first) {
//					first = false;
//				} else {
//					output.append(", ");
//				}
//				output.append(ic.getEdgeClass().getQualifiedName());
//
//			}
			output.append("\n");
			output.append("incomming edge classes: ");
//			first = true;
//			for (IncidenceClass ic : ((VertexClass) this)
//					.getOwnInIncidenceClasses()) {
//				if (first) {
//					first = false;
//				} else {
//					output.append(", ");
//				}
//				output.append(ic.getEdgeClass().getQualifiedName());
//
//			}
			output.append("\n");
		}
		output.append("\n");

		return output.toString();
	}
}
