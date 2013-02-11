/*
 * JGraLab - The Java Graph Laboratory
 *
 * Copyright (C) 2006-2013 Institute for Software Technology
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

import java.util.HashMap;
import java.util.Map;

import de.uni_koblenz.jgralab.Edge;
import de.uni_koblenz.jgralab.Graph;
import de.uni_koblenz.jgralab.GraphIO;
import de.uni_koblenz.jgralab.Vertex;
import de.uni_koblenz.jgralab.exception.GraphIOException;
import de.uni_koblenz.jgralab.impl.ConsoleProgressFunction;
import de.uni_koblenz.jgralab.schema.NamedElement;

public class GraphStatistics {
	static Map<NamedElement, Integer> counters = new HashMap<NamedElement, Integer>();

	public static void main(String[] args) {
		try {
			Graph g = GraphIO.loadGraphFromFile(args[0],
					new ConsoleProgressFunction());

			for (Vertex v : g.vertices()) {
				count(v.getAttributedElementClass());
				count(v.getAttributedElementClass().getPackage());
			}

			for (Edge e : g.edges()) {
				count(e.getAttributedElementClass());
				count(e.getAttributedElementClass().getPackage());
			}
			for (NamedElement e : counters.keySet()) {
				System.out.println(e.getClass().getSimpleName() + ";"
						+ e.getQualifiedName() + ";" + counters.get(e));
			}
		} catch (GraphIOException e) {

			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private static void count(NamedElement e) {
		if (counters.containsKey(e)) {
			counters.put(e, counters.get(e) + 1);
		} else {
			counters.put(e, 1);
		}
	}
}
