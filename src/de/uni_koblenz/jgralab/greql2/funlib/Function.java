package de.uni_koblenz.jgralab.greql2.funlib;

import java.util.ArrayList;

public abstract class Function {

	public enum Category {
		ARITHMETICS, COLLECTIONS_AND_MAPS, GRAPH, LOGICS,

		PATHS_AND_PATHSYSTEMS_AND_SLICES, REFLECTION, RELATIONS,

		SCHEMA_ACCESS, STATISTICS, STRINGS, MISCELLANEOUS, UNDEFINED
	}

	private String description;
	private Category[] categories;;
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
