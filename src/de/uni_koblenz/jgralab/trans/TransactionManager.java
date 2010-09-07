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
package de.uni_koblenz.jgralab.trans;

import java.util.List;

/**
 * This interface defines the functionality offered by the
 * <code>TransactionManager</code>. Every <code>Graph</code>-instance owns a
 * <code>TransactionManager</code>-instance, while a
 * <code>TransactionManager</code>-instance is always bound to one and the same
 * <code>Graph</code>-instance.
 * 
 * This interface is only relevant for internal use.
 * 
 * @author Jose Monte(monte@uni-koblenz.de)
 */
public interface TransactionManager {
	/**
	 * 
	 * @return a read-write-<code>Transaction</code>
	 */
	public Transaction createTransaction();

	/**
	 * 
	 * @return a read-only-<code>Transaction</code>
	 */
	public Transaction createReadOnlyTransaction();

	/**
	 * 
	 * @param transaction
	 *            the <code>Transaction</code>
	 * @param thread
	 *            the thread in which the <code>transaction</code> should be set
	 *            as active
	 */
	public void setTransactionForThread(Transaction transaction, Thread thread);

	/**
	 * 
	 * @param thread
	 *            the <code>Thread</code>
	 * @return the transaction which is currently active within the given
	 *         <code>thread</code>
	 */
	public Transaction getTransactionForThread(Thread thread);

	/**
	 * Removes the mapping between the thread and the currently assigned active
	 * <code>Transaction</code>.
	 * 
	 * @param thread
	 *            the <code>Thread</code>
	 */
	public void removeTransactionForThread(Thread thread);

	/**
	 * The returned list only contains <code>Transaction<code>s for which
	 * {@link Transaction#getState() getState()} != {@link TransactionState#COMMITTED COMMITTED} &&
	 * {@link Transaction#getState() getState()} != {@link TransactionState#ABORTED ABORTED} is true.
	 * 
	 * @return Returns a list of active <code>Transaction<code>s
	 */
	public List<Transaction> getTransactions();
}
