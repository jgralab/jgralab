/*
 * JGraLab - The Java Graph Laboratory
 * 
 * Copyright (C) 2006-2011 Institute for Software Technology
 *                         University of Koblenz-Landau, Germany
 *                         ist@uni-koblenz.de
 * 
 * For bug reports, documentation and further information, visit
 * 
 *                         http://jgralab.uni-koblenz.de
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
package de.uni_koblenz.jgralabtest.instancetest;

import static org.junit.Assert.fail;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;

import de.uni_koblenz.jgralab.Graph;
import de.uni_koblenz.jgralab.ImplementationType;
import de.uni_koblenz.jgralab.impl.db.GraphDatabase;
import de.uni_koblenz.jgralab.impl.db.GraphDatabaseException;
import de.uni_koblenz.jgralab.trans.CommitFailedException;

public abstract class InstanceTest {

	private static Collection<Object[]> parameters;

	static {
		parameters = new ArrayList<Object[]>();
		// for (ImplementationType current : ImplementationType.values()) {
		// parameters.add(new Object[] { current });
		// }

		printIndex();
		parameters.add(new Object[] { ImplementationType.STANDARD, null });
		System.out.println("standard implementation");

		printIndex();
		parameters.add(new Object[] { ImplementationType.TRANSACTION, null });
		System.out.println("transaction implementation");

		String dbURL = System.getProperty("jgralabtest_dbconnection");
		dbURL = dbURL != null && dbURL.startsWith("jdbc") ? dbURL : null;

		String derbyURL = System.getProperty("jgralabtest_derby_dbconnection");
		derbyURL = derbyURL != null && derbyURL.startsWith("jdbc") ? derbyURL
				: null;
		String postgresURL = System
				.getProperty("jgralabtest_postgres_dbconnection");
		postgresURL = postgresURL != null && postgresURL.startsWith("jdbc") ? postgresURL
				: null;

		String mysqlURL = System.getProperty("jgralabtest_mysql_dbconnection");
		mysqlURL = mysqlURL != null && mysqlURL.startsWith("jdbc") ? mysqlURL
				: null;

		boolean dbConnectionEnabled = dbURL != null || derbyURL != null
				|| postgresURL != null || mysqlURL != null;
		if (dbConnectionEnabled) {
			if (dbURL != null) {
				// only one db impl is tested
				addDBTest(dbURL);
			} else {
				addDBTest(derbyURL);
				addDBTest(postgresURL);
				addDBTest(mysqlURL);
			}
		} else {
			System.out
					.println("No database access data provided, disabling database support testing.");
			System.out
					.println("To enable database support test, set the property 'jgralabtest_dbconnection' to a valid JDBC database URL.");
		}
	}

	private static void printIndex() {
		System.out.print("[" + parameters.size() + "] ");
	}

	private static void addDBTest(String url) {
		if (url != null) {
			printIndex();
			parameters.add(new Object[] { ImplementationType.DATABASE, url });
			System.out.println("database implementation using " + url);
			GraphDatabase gdb;
			try {
				// install graph database tables if not already existent
				gdb = GraphDatabase.openGraphDatabase(url);
				gdb.setAutoCommit(false);
				try {
					System.out.println("Clearing graph database at " + url);
					gdb.clearAllTables();
					gdb.commitTransaction();
				} catch (SQLException e) {
					// in this case, the tables did not exist, so install them
					gdb.rollback();
					gdb.applyDbSchema();
				}
			} catch (GraphDatabaseException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		}
	}

	protected GraphDatabaseHandler dbHandler;

	public static Collection<Object[]> getParameters() {
		return parameters;
	}

	/**
	 * Flag for indicating whether transactions are enabled or not.
	 */
	// protected boolean transactionsEnabled;
	protected ImplementationType implementationType;

	protected InstanceTest(ImplementationType implementationType, String dbURL) {
		this.implementationType = implementationType;
		dbHandler = dbURL == null ? null : new GraphDatabaseHandler(dbURL);
		// this.transactionsEnabled = implementationType ==
		// ImplementationType.TRANSACTION;
	}

	/**
	 * Creates a new read only transaction for the given graph iff transactions
	 * are enabled. Otherwise it does nothing.
	 * 
	 * @param g
	 */
	protected void createReadOnlyTransaction(Graph g) {
		if (implementationType == ImplementationType.TRANSACTION) {
			g.newReadOnlyTransaction();
		}
	}

	/**
	 * Creates a new transaction for the given graph iff transactions are
	 * enabled. Otherwise it does nothing.
	 * 
	 * @param g
	 */
	protected void createTransaction(Graph g) {
		if (implementationType == ImplementationType.TRANSACTION) {
			g.newTransaction();
		}
	}

	/**
	 * Commits the last created transaction for the given graph.
	 * 
	 * @param g
	 * @throws CommitFailedException
	 *             if the commit yields an error
	 */
	protected void commit(Graph g) throws CommitFailedException {
		switch (implementationType) {
		case TRANSACTION:
			g.commit();
			break;
		case DATABASE:
			try {
				dbHandler.graphDatabase.commitTransaction();
			} catch (GraphDatabaseException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			break;
		}

	}

	/**
	 * Prints a warning that the method with the given methodName has not been
	 * tested with transaction support. This method is subject to be removed
	 * when all instance tests have been changed to support transactions.
	 * 
	 * @param methodName
	 *            the name of the method that cannot be tested yet.
	 */
	protected void onlyTestWithoutTransactionSupport() {
		if (implementationType == ImplementationType.TRANSACTION) {
			fail("Current test does not support transactions yet");
		}
	}
}
