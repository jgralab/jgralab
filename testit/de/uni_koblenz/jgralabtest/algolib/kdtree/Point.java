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
package de.uni_koblenz.jgralabtest.algolib.kdtree;

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
