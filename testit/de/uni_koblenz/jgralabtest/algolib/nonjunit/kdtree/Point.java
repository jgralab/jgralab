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
package de.uni_koblenz.jgralabtest.algolib.nonjunit.kdtree;

public abstract class Point {
	private int dimension;

	public Point(int dimension) {
		super();
		this.dimension = dimension;
	}

	public abstract double get(int position);

	public int getDimension() {
		return dimension;
	}

	public double squaredDistance(Point other) {
		assert (dimension == other.getDimension());
		double result = 0;
		for (int i = 0; i < dimension; i++) {
			double currentValue = other.get(i) - get(i);
			result += currentValue * currentValue;
		}
		return result;
	}

	public String toString() {
		StringBuilder out = new StringBuilder();
		out.append('(');
		out.append(get(0));
		for (int i = 1; i < dimension; i++) {
			out.append(',');
			out.append(get(i));
		}
		out.append(')');
		return out.toString();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + dimension;
		long temp;
		for (int i = 0; i < dimension; i++) {
			temp = Double.doubleToLongBits(get(i));
			result = prime * result + (int) (temp ^ (temp >>> 32));
		}
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Point other = (Point) obj;
		if (dimension != other.dimension)
			return false;
		for (int i = 0; i < dimension; i++) {
			if (Double.doubleToLongBits(get(i)) != Double
					.doubleToLongBits(other.get(i)))
				return false;
		}
		return true;
	}

}
