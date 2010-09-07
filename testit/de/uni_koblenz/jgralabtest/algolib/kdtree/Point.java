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
