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
package de.uni_koblenz.jgralabtest.instancetest;

import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.Collection;

import de.uni_koblenz.jgralab.Graph;
import de.uni_koblenz.jgralab.ImplementationType;
import de.uni_koblenz.jgralab.trans.CommitFailedException;

public abstract class InstanceTest {

	private static Collection<Object[]> parameters;

	static {
		parameters = new ArrayList<Object[]>();
		// for (ImplementationType current : ImplementationType.values()) {
		// parameters.add(new Object[] { current });
		// }
		parameters.add(new Object[] { ImplementationType.STANDARD });
		parameters.add(new Object[] { ImplementationType.TRANSACTION });
		parameters.add(new Object[] { ImplementationType.SAVEMEM });

		if (System.getProperty("jgralab_dbconnection") != null) {
			System.out.println("Enabling database support testing...");
			parameters.add(new Object[] { ImplementationType.DATABASE });
		} else {
			System.out
					.println("No database access data provided, disabling database support testing...");
		}
	}

	protected GraphDatabaseHandler dbHandler;

	{
		dbHandler = new GraphDatabaseHandler();
	}

	public static Collection<Object[]> getParameters() {
		return parameters;
	}

	/**
	 * Flag for indicating whether transactions are enabled or not.
	 */
	// protected boolean transactionsEnabled;
	protected ImplementationType implementationType;

	protected InstanceTest(ImplementationType implementationType) {
		this.implementationType = implementationType;
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
		if (implementationType == ImplementationType.TRANSACTION) {
			g.commit();
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
