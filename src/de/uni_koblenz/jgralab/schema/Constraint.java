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
package de.uni_koblenz.jgralab.schema;

/**
 * Describes a constraint which can be added to an
 * {@link AttributedElementClass}. A constraint consists of three parts:
 * 
 * <nl>
 * <li>A message which states what's wrong if the constraint is not satisfied.</li>
 * <li>A GReQL predicate which all elements of the corresponding
 * {@link AttributedElementClass} have to fulfill.</li>
 * <li>A GReQL expressing returning a set of all elements which don't fulfill
 * the predicate. (optional)</li>
 * </nl>
 * 
 * The <code>GraphValidator</code> can be used to check if all constraints are
 * fulfilled.
 * 
 * @author Tassilo Horn <horn@uni-koblenz.de>
 */
public interface Constraint extends Comparable<Constraint> {

	/**
	 * @return the message which states what's wrong if the constraint is not
	 *         satisfied
	 */
	public String getMessage();

	/**
	 * @return the GReQL predicate which all elements of the corresponding
	 *         {@link AttributedElementClass} have to fulfill
	 */
	public String getPredicate();

	/**
	 * @return the GReQL expressing returning a set of all elements which don't
	 *         fulfill the predicate. (optional)
	 */
	public String getOffendingElementsQuery();
}
