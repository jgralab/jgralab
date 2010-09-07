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
