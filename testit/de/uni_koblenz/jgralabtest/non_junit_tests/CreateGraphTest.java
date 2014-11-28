/*
 * JGraLab - The Java Graph Laboratory
 *
 * Copyright (C) 2006-2014 Institute for Software Technology
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

import java.util.logging.Level;

import de.uni_koblenz.jgralab.Graph;
import de.uni_koblenz.jgralab.ImplementationType;
import de.uni_koblenz.jgralab.JGraLab;
import de.uni_koblenz.jgralab.exception.GraphIOException;
import de.uni_koblenz.jgralab.schema.GraphClass;
import de.uni_koblenz.jgralab.schema.Schema;
import de.uni_koblenz.jgralab.schema.VertexClass;
import de.uni_koblenz.jgralab.schema.codegenerator.CodeGeneratorConfiguration;
import de.uni_koblenz.jgralab.schema.impl.SchemaImpl;

public class CreateGraphTest {
	public static void main(String[] args) {
		System.out.println("Test for in-memory compilation");

		System.out.println("Create schema...");
		Schema schema = new SchemaImpl("SimpleSchema", "de.uni_koblenz.demo");
		GraphClass gc = schema.createGraphClass("SimpleGraph");
		VertexClass vc = gc.createVertexClass("Node");

		Level l = JGraLab.setLogLevel(Level.FINE);
		System.out.println("Compile schema classes in memory...");
		System.out.flush();
		schema.finish();
		schema.compile(CodeGeneratorConfiguration.MINIMAL);

		System.err.flush();
		System.out.println("Create " + gc.getQualifiedName());
		Graph g = schema.createGraph(ImplementationType.GENERIC);
		g.createVertex(vc);
		g.createVertex(vc);
		JGraLab.setLogLevel(l);
		try {
			System.out.println("Save graph...");
			g.save("testit/testdata/graph.tg");
		} catch (GraphIOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.out.println("Fini.");
	}
}
