package de.uni_koblenz.jgralabtest;

import java.util.ArrayList;
import java.util.Collection;

import de.uni_koblenz.jgralab.Graph;
import de.uni_koblenz.jgralab.trans.CommitFailedException;

public abstract class InstanceTest {

	private static Collection<Object[]> parameters;
	
	static {
		parameters = new ArrayList<Object[]>();
		parameters.add(new Object[] { Boolean.FALSE });
		parameters.add(new Object[] { Boolean.TRUE });
	}

	public static Collection<Object[]> getParameters() {
		return parameters;
	}
	
	/**
	 * Flag for indicating whether transactions are enabled or not.
	 */
	protected boolean transactionsEnabled;

	protected InstanceTest(boolean transactionsEnabled) {
		this.transactionsEnabled = transactionsEnabled;
	}

	/**
	 * Creates a new read only transaction for the given graph iff transactions
	 * are enabled. Otherwise it does nothing.
	 * 
	 * @param g
	 */
	protected void createReadOnlyTransaction(Graph g) {
		if (transactionsEnabled) {
			g.createReadOnlyTransaction();
		}
	}

	/**
	 * Creates a new transaction for the given graph iff transactions are
	 * enabled. Otherwise it does nothing.
	 * 
	 * @param g
	 */
	protected void createTransaction(Graph g) {
		if (transactionsEnabled) {
			g.createTransaction();
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
		if (transactionsEnabled) {
			g.commit();
		}
	}
}
