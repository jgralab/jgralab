package de.uni_koblenz.jgralabtest.non_junit_tests;

import java.util.Arrays;

import de.uni_koblenz.jgralab.impl.GraphBase;
import de.uni_koblenz.jgralabtest.schemas.vertextest.DoubleSubNode;
import de.uni_koblenz.jgralabtest.schemas.vertextest.SubLink;
import de.uni_koblenz.jgralabtest.schemas.vertextest.VertexTestGraph;
import de.uni_koblenz.jgralabtest.schemas.vertextest.VertexTestSchema;

public class DebugTrans {

	/**
	 * @param args
	 */
	public static void main(String[] args) throws Exception {
		VertexTestGraph vtg = VertexTestSchema.instance()
				.createVertexTestGraphWithTransactionSupport();
		GraphBase g = (GraphBase) vtg;
		g.newTransaction();
		DoubleSubNode v1 = vtg.createDoubleSubNode();
		DoubleSubNode v2 = vtg.createDoubleSubNode();
		DoubleSubNode v3 = vtg.createDoubleSubNode();
		SubLink e1 = vtg.createSubLink(v1, v2);
		System.out.println(Arrays.toString(g.getEdge()));
		System.out.println(e1.isValid());
		System.out.println();
		SubLink e2 = vtg.createSubLink(v2, v3);
		System.out.println(Arrays.toString(g.getEdge()));
		System.out.println(e2.isValid());
		System.out.println();
		g.commit();

		g.newReadOnlyTransaction();
		System.out.println(Arrays.toString(g.getEdge()));
		System.out.println(e1.isValid());
		System.out.println();
		System.out.println(Arrays.toString(g.getEdge()));
		System.out.println(e2.isValid());
		System.out.println();
		g.commit();
		System.out.println("Fini.");
	}

}
