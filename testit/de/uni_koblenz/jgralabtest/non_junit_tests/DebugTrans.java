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
package de.uni_koblenz.jgralabtest.non_junit_tests;

import java.util.Arrays;

import de.uni_koblenz.jgralab.ImplementationType;
import de.uni_koblenz.jgralab.impl.InternalGraph;
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
				.createVertexTestGraph(ImplementationType.TRANSACTION);
		InternalGraph g = (InternalGraph) vtg;
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
