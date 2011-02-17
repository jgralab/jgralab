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
package de.uni_koblenz.jgralabtest.non_junit_tests;

import java.sql.SQLException;

import de.uni_koblenz.jgralab.impl.db.GraphDatabase;
import de.uni_koblenz.jgralab.impl.db.GraphDatabaseException;
import de.uni_koblenz.jgralabtest.schemas.jniclient.JniTestGraph;
import de.uni_koblenz.jgralabtest.schemas.jniclient.JniTestSchema;
import de.uni_koblenz.jgralabtest.schemas.jniclient.Node;

public class GraphDbTest {

	public static void main(String[] args) throws GraphDatabaseException,
			SQLException {
		GraphDatabase gdb;

		System.out.println("Connecting DB...");
		gdb = GraphDatabase.openGraphDatabase(System
				.getProperty("jgralabtest_dbconnection"));
		gdb.setAutoCommit(false);
		try {
			System.out
					.println("Clearing graph db (hopefully it was only a test DB :-) )...");
			gdb.clearAllTables();
			gdb.commitTransaction();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		try {
			if (!gdb.contains(JniTestSchema.instance())) {
				gdb.insertSchema(JniTestSchema.instance());
			}
		} catch (GraphDatabaseException e) {
			gdb.applyDbSchema();
			// if (!gdb.contains(JniTestSchema.instance())) {
			gdb.insertSchema(JniTestSchema.instance());
			// }
			e.printStackTrace();
		}
		gdb.commitTransaction();

		boolean containsSchema = gdb.contains(JniTestSchema.instance());
		System.out.println(containsSchema);

		try {
			gdb.dropForeignKeyConstraints();
			gdb.commitTransaction();
		} catch (Exception e) {
			gdb.rollback();
		}
		
		containsSchema = gdb.contains(JniTestSchema.instance());
		System.out.println(containsSchema);

		try {
			gdb.dropIndices();
			gdb.commitTransaction();
		} catch (Exception e) {
			gdb.rollback();
		}
		gdb.commitTransaction();
		
		System.out.println("Creating graph...");
		JniTestGraph g = JniTestSchema.instance()
				.createJniTestGraphWithDatabaseSupport("gdbtest", gdb);

		final int NV = 10000;
		final int NE = 10000;

		System.out.println("Creating " + NV + " vertices...");
		long s0 = System.currentTimeMillis();
		for (int i = 1; i <= NV; ++i) {
			g.createNode();
			if (i % 1000 == 0) {
				System.out.print(".");
			}
		}
		System.out.println();
		System.out.println(System.currentTimeMillis() - s0 + " ms");
		gdb.commitTransaction();

		System.out.println("Creating " + NE + " edges...");
		s0 = System.currentTimeMillis();
		for (int i = 1; i <= NE; ++i) {
			Node n1 = (Node) g.getVertex((int) (Math.random() * NV) + 1);
			Node n2 = (Node) g.getVertex((int) (Math.random() * NV) + 1);
			g.createLink(n1, n2);
			if (i % 1000 == 0) {
				System.out.print(".");
			}
		}
		System.out.println();
		System.out.println(System.currentTimeMillis() - s0 + " ms");
		// gdb.optimizeForGraphCreation();
		gdb.commitTransaction();

		// gdb.setAutoCommitMode(true);
		// gdb.optimizeForGraphTraversal();
		System.out.println("Fini.");

	}
}
