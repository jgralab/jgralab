package de.uni_koblenz.jgralabtest.greql.funlib.arithmetics;

public abstract class UnaryFunctionTest {
	int[] intValues = new int[] { 1, -1, 0, 2, -2, 89, 99, -100,
			Integer.MIN_VALUE, Integer.MAX_VALUE };
	long[] longValues = new long[] { 1l, -1l, 0l, 2l, -2l, 89l, 99l, -100l,
			Integer.MIN_VALUE, Integer.MAX_VALUE, Long.MIN_VALUE,
			Long.MAX_VALUE };
	double[] doubleValues = new double[] { 1.0, -1.0, 0.0, 2354.8,
			-100093.90884, Double.MIN_VALUE, Double.MAX_VALUE, Double.NaN,
			Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY };

}
