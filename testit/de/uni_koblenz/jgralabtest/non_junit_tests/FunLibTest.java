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
package de.uni_koblenz.jgralabtest.non_junit_tests;

import java.io.PrintStream;

import org.pcollections.PSet;
import org.pcollections.PVector;

import de.uni_koblenz.jgralab.JGraLab;
import de.uni_koblenz.jgralab.greql2.funlib.FunLib;
import de.uni_koblenz.jgralab.greql2.types.Table;
import de.uni_koblenz.jgralab.greql2.types.Tuple;

public class FunLibTest {
	enum Color {
		RED, GREEN, BLUE
	};

	@SuppressWarnings("unchecked")
	public static void main(String[] args) {
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
		FunLib.apply(os, "add", a);

		long t0 = System.nanoTime();
		for (int i = 0; i < 10000000; ++i) {
			FunLib.apply("add", a);
		}
		long t1 = System.nanoTime();
		System.out.println("Time for 10M adds: " + (t1 - t0) / 1e6 + " ms");

		FunLib.apply(os, "div", 17, 4);
		FunLib.apply(os, "div", 17, 4.0);
		// FunLib.apply(os, "degree", n1);
		// FunLib.apply(os, "degree", sub, n1);
		// FunLib.apply(os, "degree", n1, col);
		// FunLib.apply(os, "degree", sub, n1, col);
		// FunLib.apply(os, "isAcyclic", g);
		// FunLib.apply(os, "isAcyclic", sub);
		FunLib.apply(os, "concat", "Args: ",
				FunLib.apply(os, "concat", "17+", 4));

		PVector<String> v1 = JGraLab.vector();
		PVector<String> v2 = v1.plus("d").plus("e").plus("f");
		v1 = v1.plus("a").plus("b").plus("c");
		FunLib.apply(os, "concat", "v1: ", v1);
		FunLib.apply(os, "concat", "v2: ", v2);
		FunLib.apply(os, "concat", v1, v2);

		FunLib.apply(os, "max", v2);
		FunLib.apply(os, "min", FunLib.apply("concat", v2, v1));

		FunLib.apply(os, "concat", "Rot: ", Color.RED);
		// FunLib.apply(os, "topologicalSort", g);
		FunLib.apply(os, "bitAnd", 5L, -7);
		FunLib.apply(os, "equals", v1, v1);
		FunLib.apply(os, "equals", null, null);
		FunLib.apply(os, "concat", v1, null);
		FunLib.apply(os, "equals", 5, FunLib.apply("mul", 10, 0.5));

		PVector<Double> v3 = JGraLab.vector();
		v3 = v3.plus(2.0).plus(10.0).plus(2.0).plus(5.0).plus(-1.0);
		FunLib.apply(os, "count", v3);
		FunLib.apply(os, "min", v3);
		FunLib.apply(os, "max", v3);
		FunLib.apply(os, "mean", v3);
		FunLib.apply(os, "sdev", v3);
		FunLib.apply(os, "variance", v3);
		FunLib.apply(os, "sort", v3);
		FunLib.apply(os, "sort", FunLib.apply("concat", v2, v1));

		PSet<Integer> v4 = JGraLab.set();
		v4 = v4.plus(2).plus(10).plus(2).plus(5).plus(-1);
		FunLib.apply(os, "count", v4);
		FunLib.apply(os, "sort", v4);
		FunLib.apply(os, "sum", v4);

		FunLib.apply(os, "round", Math.E);
		FunLib.apply(os, "floor", Math.E);
		FunLib.apply(os, "ceil", Math.E);

		FunLib.apply(os, "round", Math.PI);
		FunLib.apply(os, "floor", Math.PI);
		FunLib.apply(os, "ceil", Math.PI);

		// @SuppressWarnings("unchecked")
		// PVector<PMap<String, String>> attrs = (PVector<PMap<String, String>>)
		// FunLib.apply(os, "attributes", n1);
		//
		// PMap<String, String> m = attrs.get(0);
		// FunLib.apply(os, "entrySet", m);
		// FunLib.apply(os, "keySet", m);
		// FunLib.apply(os, "values", m);
		//
		// FunLib.apply(os, "describe", g);
		// FunLib.apply(os, "describe", n1);
		// FunLib.apply(os, "id", n1);
		//
		// FunLib.apply(os, "log", "n1.label=", n1.get_label());
		//
		// FunLib.apply(os, "isDefined", FunLib.apply("get", m, "name"));
		// FunLib.apply(os, "isDefined", FunLib.apply("get", m, "hugo"));
		//
		// FunLib.apply(os, "isDefined", FunLib.apply("get", attrs, 0));
		// FunLib.apply(os, "isDefined", FunLib.apply("get", attrs, 2));

		PVector<Tuple> v = JGraLab.vector();
		v = v.plus(Tuple.empty().plus("c").plus(1))
				.plus(Tuple.empty().plus("a").plus(3))
				.plus(Tuple.empty().plus("d").plus(0))
				.plus(Tuple.empty().plus("a").plus(1))
				.plus(Tuple.empty().plus("b").plus(2));
		FunLib.apply(os, "sortByColumn", 0, v);
		FunLib.apply(os, "sortByColumn", 1, v);

		Table<Tuple> t = Table.empty();
		PVector<String> titles = JGraLab.vector();
		t = t.withTitles(titles.plus("C0").plus("C1")).plusAll(v);
		FunLib.apply(os, "sortByColumn", 0, t);
		FunLib.apply(os, "sortByColumn", 1, t);

		PVector<Integer> cols = JGraLab.vector();
		cols = cols.plus(0).plus(1);
		FunLib.apply(os, "sortByColumn", cols, t);

		cols = JGraLab.vector();
		cols = cols.plus(1).plus(0);
		FunLib.apply(os, "sortByColumn", cols, t);

		PSet<Integer> l = JGraLab.set();
		for (int i = 0; i < 10; ++i) {
			l = l.plus(100 + i);

		}
		l = (PSet<Integer>) FunLib.apply(os, "subCollection", l, 5);
		l = (PSet<Integer>) FunLib.apply(os, "subCollection", l, 2, 4);

		FunLib.apply(os, "equals", Color.RED, "RED");
		FunLib.apply(os, "equals", Color.GREEN, "RED");
		FunLib.apply(os, "equals", "RED", Color.RED);
		FunLib.apply(os, "nequals", Color.RED, "RED");
		FunLib.apply(os, "nequals", Color.GREEN, "RED");
		FunLib.apply(os, "ndequals", "RED", Color.RED);
	}
}
