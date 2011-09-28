package de.uni_koblenz.jgralabtest.non_junit_tests;

import java.io.PrintStream;

import org.pcollections.PSet;
import org.pcollections.PVector;

import de.uni_koblenz.jgralab.JGraLab;
import de.uni_koblenz.jgralab.greql2.funlib.FunLib;

public class FunLibTest {
	enum Color {
		RED, GREEN, BLUE
	};

	public static void main(String[] args) {
		FunLib fl = FunLib.instance();

		// MinimalGraph g = MinimalSchema.instance().createMinimalGraph();
		// Node n1 = g.createNode();
		// n1.set_label("n1");
		// Node n2 = g.createNode();
		// n2.set_label("n2");
		// Node n3 = g.createNode();
		// n3.set_label("n3");
		// Node n4 = g.createNode();
		// n4.set_label("n4");
		// Link l1 = g.createLink(n1, n2);
		// g.createConnection(n1, n3);
		// g.createLink(n2, n3);
		// g.createLink(n3, n1);
		//
		// SubGraphMarker sub = new SubGraphMarker(g);
		// sub.mark(l1);
		//
		// AttributedElementClass[] t = {
		// g.getSchema().getAttributedElementClass(
		// "Connection") };
		// TypeCollection col = new TypeCollection(Arrays.asList(t), false);

		PrintStream os = System.out;
		Object[] a = { 17, 4 };
		fl.apply(os, "add", a);

		long t0 = System.nanoTime();
		for (int i = 0; i < 10000000; ++i) {
			fl.apply("add", a);
		}
		long t1 = System.nanoTime();
		System.out.println("Time for 10M adds: " + (t1 - t0) / 1e6 + " ms");

		fl.apply(os, "div", 17, 4);
		fl.apply(os, "div", 17, 4.0);
		// fl.apply(os, "degree", n1);
		// fl.apply(os, "degree", sub, n1);
		// fl.apply(os, "degree", n1, col);
		// fl.apply(os, "degree", sub, n1, col);
		// fl.apply(os, "isAcyclic", g);
		// fl.apply(os, "isAcyclic", sub);
		fl.apply(os, "concat", "Args: ", fl.apply(os, "concat", "17+", 4));

		PVector<String> v1 = JGraLab.vector();
		PVector<String> v2 = v1.plus("d").plus("e").plus("f");
		v1 = v1.plus("a").plus("b").plus("c");
		fl.apply(os, "concat", "v1: ", v1);
		fl.apply(os, "concat", "v2: ", v2);
		fl.apply(os, "concat", v1, v2);

		fl.apply(os, "max", v2);
		fl.apply(os, "min", fl.apply("concat", v2, v1));

		fl.apply(os, "concat", "Rot: ", Color.RED);
		// fl.apply(os, "topologicalSort", g);
		fl.apply(os, "bitAnd", 5L, -7);
		fl.apply(os, "equals", v1, v1);
		fl.apply(os, "equals", null, null);
		fl.apply(os, "concat", v1, null);
		fl.apply(os, "equals", 5, fl.apply("mul", 10, 0.5));

		PVector<Double> v3 = JGraLab.vector();
		v3 = v3.plus(2.0).plus(10.0).plus(2.0).plus(5.0).plus(-1.0);
		fl.apply(os, "count", v3);
		fl.apply(os, "min", v3);
		fl.apply(os, "max", v3);
		fl.apply(os, "mean", v3);
		fl.apply(os, "sdev", v3);
		fl.apply(os, "variance", v3);
		fl.apply(os, "sort", v3);
		fl.apply(os, "sort", fl.apply("concat", v2, v1));

		PSet<Integer> v4 = JGraLab.set();
		v4 = v4.plus(2).plus(10).plus(2).plus(5).plus(-1);
		fl.apply(os, "count", v4);
		fl.apply(os, "sort", v4);

		fl.apply(os, "round", Math.E);
		fl.apply(os, "floor", Math.E);
		fl.apply(os, "ceil", Math.E);

		fl.apply(os, "round", Math.PI);
		fl.apply(os, "floor", Math.PI);
		fl.apply(os, "ceil", Math.PI);

		// @SuppressWarnings("unchecked")
		// PVector<PMap<String, String>> attrs = (PVector<PMap<String, String>>)
		// fl
		// .apply(os, "attributes", n1);
		//
		// PMap<String, String> m = attrs.get(0);
		// fl.apply(os, "entrySet", m);
		// fl.apply(os, "keySet", m);
		// fl.apply(os, "values", m);
		//
		// fl.apply(os, "describe", g);
		// fl.apply(os, "describe", n1);
		// fl.apply(os, "id", n1);
		//
		// fl.apply(os, "log", "n1.label=", n1.get_label());
		//
		// fl.apply(os, "isDefined", fl.apply("get", m, "name"));
		// fl.apply(os, "isDefined", fl.apply("get", m, "hugo"));
		//
		// fl.apply(os, "isDefined", fl.apply("get", attrs, 0));
		// fl.apply(os, "isDefined", fl.apply("get", attrs, 2));
	}
}
