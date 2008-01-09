/*
 * JGraLab - The Java graph laboratory
 * (c) 2006-2007 Institute for Software Technology
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
 
package de.uni_koblenz.jgralab.impl;

import java.util.Iterator;

import de.uni_koblenz.jgralab.AttributedElementClass;
import de.uni_koblenz.jgralab.EdgeClass;
import de.uni_koblenz.jgralab.GraphClass;
import de.uni_koblenz.jgralab.GraphElementClass;
import de.uni_koblenz.jgralab.Schema;
import de.uni_koblenz.jgralab.VertexClass;

public abstract class GraphElementClassImpl extends AttributedElementClassImpl implements GraphElementClass {

	protected GraphClass graphClass;

	/**
	 * delegates its constructor to the generalized class
	 * @param name the unique identifier of the element in the schema
	 */
	public GraphElementClassImpl(String name, GraphClass aGraphClass) {
		super(name);
		this.graphClass = aGraphClass;
	}
	
	/* (non-Javadoc)
	 * @see jgralab.GraphElementClass#getGraphClass()
	 */
	public GraphClass getGraphClass() {
		return graphClass;
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		String output = this.getClass().getSimpleName() + " '" + getName() + "'";
		if (isAbstract())
			output += " (abstract)";
		output += ": \n";

		output += "subClasses of '" + getName() + "': ";
		Iterator<AttributedElementClass> it = subClasses.iterator();
		while (it.hasNext()) {
			output+= "'"+it.next().getName() + "' ";
		}
		output += "\nsuperClasses of '" + getName() + "': ";
		it = getAllSuperClasses().iterator();
		while (it.hasNext()) {
			output+= "'"+it.next().getName() + "' ";
		}
		output += "\ndirectSuperClasses of '" + getName() + "': ";
		it = directSuperClasses.iterator();
		while (it.hasNext()) {
			output+= "'"+it.next().getName() + "' ";
		}
		
		output += attributesToString();
		
		if (this instanceof VertexClass) {
			output += "may connect to edgeclasses: ";
			Iterator<EdgeClass> it2 = ((VertexClass)this).getEdgeClasses().iterator();
			while (it2.hasNext()) {
				output += it2.next().getName();
				if (it2.hasNext())
					output += ", ";
			}
			output += "\n";
		}
		output += "\n";
		
		return output;
	}
	
	public Schema getSchema() {
		return graphClass.getSchema();
	}

}
