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
/**
 * 
 */
package de.uni_koblenz.jgralab.greql2;

import de.uni_koblenz.jgralab.greql2.schema.Greql2;
import de.uni_koblenz.jgralab.greql2.schema.Greql2Schema;
import de.uni_koblenz.jgralab.greql2.schema.Greql2Vertex;
import de.uni_koblenz.jgralab.greql2.schema.impl.std.Greql2Impl;

/**
 * @author Tassilo Horn &lt;horn@uni-koblenz.de&gt;
 * 
 */
public class SerializableGreql2Impl extends Greql2Impl implements
		SerializableGreql2 {
	static {
		Greql2Schema.instance().getGraphFactory().setGraphImplementationClass(
				Greql2.class, SerializableGreql2Impl.class);
	}

	public SerializableGreql2Impl(int vMax, int eMax) {
		super(vMax, eMax);
	}

	public SerializableGreql2Impl(java.lang.String id, int vMax, int eMax) {
		super(id, vMax, eMax);
	}

	private Greql2Serializer serializer = new Greql2Serializer();

	@Override
	public String serialize() {
		return serialize(getFirstGreql2Expression());
	}

	@Override
	public String serialize(Greql2Vertex v) {
		try {
			return serializer.serializeGreql2Vertex(v);
		} catch (Exception e) {
			System.err.println("Couldn't serialize Query.");
			e.printStackTrace();
		}
		return "";
	}
}
