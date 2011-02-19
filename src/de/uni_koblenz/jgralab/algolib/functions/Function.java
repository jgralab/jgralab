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
package de.uni_koblenz.jgralab.algolib.functions;

import de.uni_koblenz.jgralab.algolib.functions.entries.FunctionEntry;

/**
 * Interface for creating function objects. This interface should only be used
 * if there are no primitive types involved. For primitive types, there are
 * several other interfaces specified.
 * 
 * @author strauss@uni-koblenz.de
 * 
 * @param <DOMAIN>
 *            the domain of the function
 * @param <RANGE>
 *            the range of the function
 */
// TODO ask Jürgen for parameter names
public interface Function<DOMAIN, RANGE> extends Iterable<FunctionEntry<DOMAIN, RANGE>> {

	/**
	 * Returns the function value for the given <code>parameter</code>. If this
	 * function object cannot obtain a function value for the given
	 * <code>parameter</code>, the result is undefined.
	 * 
	 * @param parameter
	 *            the parameter to get the function value of.
	 * @return the function value of the given <code>parameter</code>.
	 */
	public RANGE get(DOMAIN parameter);

	/**
	 * Sets the function <code>value</code> for a given <code>parameter</code>.
	 * If there is already a function <code>value</code> for the given
	 * <code>parameter</code>, the old value is lost and replaced by the new
	 * <code>value</code>. This operation is optional and should only be
	 * implemented for function objects that are not immutable (e.g. graph
	 * markers).
	 * 
	 * @param parameter
	 *            the parameter to set the function value of.
	 * @param value
	 *            the new function value of the given <code>parameter</code>.
	 * @throws UnsupportedOperationException
	 *             if this function object is immutable
	 */
	public void set(DOMAIN parameter, RANGE value);

	/**
	 * Tells whether this function object has a value defined for the given
	 * <code>parameter</code>.
	 * 
	 * @param parameter
	 *            the parameter to check if this function object has a value
	 *            defined for it.
	 * @return <code>true</code> if there is a function object defined for the
	 *         given <code>parameter</code>.
	 */
	public boolean isDefined(DOMAIN parameter);

	public Iterable<DOMAIN> getDomainElements();
}
