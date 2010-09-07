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

/**
 * This interface represents a save-point for a <code>Transaction</code>.
 * 
 * A save-point can be defined by calling the method
 * <code>{@link Transaction#defineSavepoint() defineSavepoint}</code> of a
 * <code>Transaction</code>.
 * 
 * A save-point can be restored within the <code>Transaction</code> ==
 * <code>{@link #getTransaction() getTransaction}</code> by calling the method
 * {@link Transaction#restoreSavepoint(Savepoint savepoint) restoreSavepoint}
 * </code> of the corresponding <code>Transaction</code>.
 * 
 * @author Jose Monte(monte@uni-koblenz.de)
 */
public interface Savepoint {
	/**
	 * 
	 * @return the <code>Transaction</code> the save-point belongs to
	 */
	public Transaction getTransaction();

	/**
	 * 
	 * @return the ID of the save-point
	 */
	public int getID();

	/**
	 * A save-point is true if
	 * <code>{@link Transaction#getSavepoints() Transaction.getSavepoints()}</code>
	 * .contains(this) == true.
	 * 
	 * @return if the save-point is still valid within the corresponding
	 *         <code>Transaction</code>.
	 */
	public boolean isValid();
}
