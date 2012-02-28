/*
* JGraLab - The Java Graph Laboratory
*
* Copyright (C) 2006-2012 Institute for Software Technology
*                         University of Koblenz-Landau, Germany
*                         ist@uni-koblenz.de
*
* For bug reports, documentation and further information, visit
*
*                         https://github.com/jgralab/jgralab
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
package de.uni_koblenz.jgralab.greql2.funlib;

import java.util.ArrayList;

public abstract class Function {

	public enum Category {
		ARITHMETICS, COLLECTIONS_AND_MAPS, GRAPH, LOGICS,

		PATHS_AND_PATHSYSTEMS_AND_SLICES, REFLECTION, RELATIONS,

		SCHEMA_ACCESS, STATISTICS, STRINGS, MISCELLANEOUS, UNDEFINED
	}

	private String description;
	private Category[] categories;
	private long costs;
	private long cardinality;
	private double selectivity;

	public Function(String description, Category... categories) {
		this(description, 1, 1, 1.0, categories);

	}

	public Function(String description, long costs, long cardinality,
			double selectivity, Category... categories) {
		this.description = description;
		this.costs = costs;
		this.cardinality = cardinality;
		this.selectivity = selectivity;
		this.categories = categories;
	}

	protected final void printArguments(Object[] args) {
		for (int i = 0; i < args.length; i++) {
			System.out.println("  args[" + i + "] = " + args[i]);
		}
	}

	/**
	 * Calculates the estimated cost for the evaluation of this greql function
	 * 
	 * @param inElements
	 *            the number of input elements
	 * @return The estimated costs in the abstract measure-unit "interpretation
	 *         steps"
	 */
	public long getEstimatedCosts(ArrayList<Long> inElements) {
		return costs;
	}

	/**
	 * Calculates the estimated selectivity of this boolean function. If this
	 * function does not return a boolean value, this method should return 1
	 * 
	 * @return the selectivity of this function, 0 < selectivity <= 1
	 */
	public double getSelectivity() {
		return selectivity;
	}

	/**
	 * Calculates the estimated result size for the given number of input
	 * elements
	 * 
	 * @param inElements
	 *            the number of input elements to calculate the result size for
	 * @return the estimated number of elements in the result
	 */
	public long getEstimatedCardinality(int inElements) {
		return cardinality;
	}

	/**
	 * @return a textual descriptionof this function (can contain LaTeX syntax)
	 */
	public String getDescription() {
		return description;
	}

	public Category[] getCategories() {
		return categories;
	}
}
