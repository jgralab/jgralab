/**
 * 
 */
package de.uni_koblenz.jgralab.greql2;

import de.uni_koblenz.jgralab.greql2.schema.Greql2Vertex;
import de.uni_koblenz.jgralab.greql2.schema.impl.trans.Greql2Impl;

/**
 * @author Tassilo Horn &lt;horn@uni-koblenz.de&gt;
 * 
 */
public class SerializableGreql2TransactionsImpl extends Greql2Impl implements
		SerializableGreql2 {
	public SerializableGreql2TransactionsImpl(int vMax, int eMax) {
		super(vMax, eMax);
	}

	public SerializableGreql2TransactionsImpl(java.lang.String id, int vMax,
			int eMax) {
		super(id, vMax, eMax);
	}

	private Greql2Serializer serializer = new Greql2Serializer();

	@Override
	public String serialize() {
		return serialize(getFirstGreql2Expression());
	}

	@Override
	public String serialize(Greql2Vertex v) {
		return serializer.serializeGreql2Vertex(v);
	}
}
