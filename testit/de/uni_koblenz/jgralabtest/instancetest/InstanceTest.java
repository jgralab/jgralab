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
		for (ImplementationType current : ImplementationType.values()) {
			parameters.add(new Object[] { current });
		}
		// parameters.add(new Object[] { ImplementationType.STANDARD });
		// parameters.add(new Object[] { ImplementationType.TRANSACTION });
		// parameters.add(new Object[] { ImplementationType.SAVEMEM});
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
