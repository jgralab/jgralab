/**
 * 
 */
package de.uni_koblenz.jgralab.greql2;

import de.uni_koblenz.jgralab.greql2.schema.Greql2;
import de.uni_koblenz.jgralab.greql2.schema.Greql2Vertex;

/**
 * @author Tassilo Horn &lt;horn@uni-koblenz.de&gt;
 * 
 */
public interface SerializableGreql2 extends Greql2 {
	public String serialize();

	public String serialize(Greql2Vertex v);
}
