package de.uni_koblenz.jgralabtest.tg2schemagraphtest;

import de.uni_koblenz.jgralab.utilities.tg2dot.Tg2Dot;
import de.uni_koblenz.jgralab.utilities.tg2schemagraph.Tg2SchemaGraph;

public class BuildTg2Dot {
	final static String[] testgrumlschema = { "testgrumlschema.tg", "testgrumlgraph" };
	final static String[] test0 = { "testschema0.tg", "testgraph0" };
	final static String[] test1 = { "testschema1.tg", "testgraph1" };
	final static String[] test2 = { "testschema2.tg", "testgraph2" };

	public static void main(String[] args) {
		convert(testgrumlschema);
		convert(test0);
		convert(test1);
		convert(test2);

	}

	public static void convert(String[] args) {
		String[] tg2sgargs = new String[4];
		tg2sgargs[0] = "-s";
		tg2sgargs[1] = "de/uni_koblenz/jgralabtest/tg2schemagraphtest/"
				+ args[0];
		tg2sgargs[2] = "-o";
		tg2sgargs[3] = "de/uni_koblenz/jgralabtest/tg2schemagraphtest/"
				+ args[1] + ".tg";
		Tg2SchemaGraph.main(tg2sgargs);
		String[] tg2dargs = new String[4];
		tg2dargs[0] = "-g";
		tg2dargs[1] = "de/uni_koblenz/jgralabtest/tg2schemagraphtest/"
				+ args[1] + ".tg";
		tg2dargs[2] = "-o";
		tg2dargs[3] = "de/uni_koblenz/jgralabtest/tg2schemagraphtest/"
				+ args[1] + ".dot";
		Tg2Dot.main(tg2dargs);
	}

}
