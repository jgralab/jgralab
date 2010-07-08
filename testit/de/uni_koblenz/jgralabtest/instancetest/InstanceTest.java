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
		parameters.add(new Object[] { ImplementationType.STANDARD });
		parameters.add(new Object[] { ImplementationType.TRANSACTION });
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
