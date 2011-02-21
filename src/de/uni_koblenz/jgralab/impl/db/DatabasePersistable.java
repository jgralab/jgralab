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
package de.uni_koblenz.jgralab.impl.db;

/**
 * A database persistable Element.
 * 
 * @author ultbreit@uni-koblenz.de
 */
public interface DatabasePersistable {

	/**
	 * Checks if persistable has already been persisted to database.
	 * 
	 * @return true if persistable is persistent in database, false otherwise.
	 */
	boolean isPersistent();

	/**
	 * Sets whether a persistable has been persisted.
	 * 
	 * @param persistent
	 *            Persistance status.
	 */
	void setPersistent(boolean persistent);

	/**
	 * Checks if graph element has been initialized and ready to be used.
	 * 
	 * @return true if graph element is initialized and ready to be used, false
	 *         otherwise.
	 */
	boolean isInitialized();

	/**
	 * Sets whether a graph element has been initialized and ready to be used.
	 * 
	 * @param initialized
	 *            Initialization status.
	 */
	void setInitialized(boolean initialized);

	/**
	 * Notifies persistable that it has been deleted.
	 */
	void deleted();

	/**
	 * Gets primary key of graph in database.
	 * 
	 * @return Primary key of graph in database or -1 if graph has not been
	 *         persisted yet.
	 */
	int getGId();
}
