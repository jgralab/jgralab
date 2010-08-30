package de.uni_koblenz.jgralabtest.algolib.kdtree;

public class Point2 extends Point {

	private double x;
	private double y;

	public Point2(double x, double y) {
		super(2);
		this.x = x;
		this.y = y;
	}

	@Override
	public double get(int position) {
		if (position == 0) {
			return x;
		}
		if (position == 1) {
			return y;
		}
		throw new IndexOutOfBoundsException("position too high: " + position);
	}

}
