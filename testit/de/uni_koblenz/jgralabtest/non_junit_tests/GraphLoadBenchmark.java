package de.uni_koblenz.jgralabtest.non_junit_tests;

import de.uni_koblenz.jgralab.GraphIO;
import de.uni_koblenz.jgralab.GraphIOException;
import de.uni_koblenz.jgralab.ImplementationType;

public class GraphLoadBenchmark {
	public static void main(String[] args) {
		final int N = 10;
		final String FILENAME = "/Users/riediger/Desktop/tmp/anhang/lr5200.tg";
		try {
			long min = Long.MAX_VALUE;
			long max = 0;
			long sum = 0;
			for (int i = 1; i <= N + 2; ++i) {
				System.out.println(i);
				long t0 = System.currentTimeMillis();
				GraphIO.loadGraphFromFile(FILENAME, ImplementationType.GENERIC,
						null);
				long t1 = System.currentTimeMillis();
				long t = t1 - t0;
				min = Math.min(min, t);
				max = Math.max(max, t);
				sum += t;
			}
			System.out.println("Min: " + min);
			System.out.println("Max: " + max);
			System.out.println("Total: " + (sum - max - min));
			System.out.println("Average: " + (1.0 * (sum - max - min) / N));
		} catch (GraphIOException e) {

			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
