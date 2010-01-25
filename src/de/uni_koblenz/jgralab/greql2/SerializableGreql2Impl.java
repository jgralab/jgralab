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
