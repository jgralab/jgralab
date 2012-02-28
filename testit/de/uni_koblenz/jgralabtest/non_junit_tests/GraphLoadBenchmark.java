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

import de.uni_koblenz.jgralab.GraphIO;
import de.uni_koblenz.jgralab.GraphIOException;
import de.uni_koblenz.jgralab.ImplementationType;

public class GraphLoadBenchmark {
	public static void main(String[] args) {
		final int N = 100;
		// final String FILENAME =
		// "/Users/riediger/Documents/workspace-dev/jgstreetmap/OsmGraph.tg.gz";
		final String FILENAME = "/Users/riediger/Desktop/tmp/anhang/lr5200.tg";
		try {
			long min = Long.MAX_VALUE;
			long max = 0;
			long sum = 0;
			for (int i = 1; i <= N + 2; ++i) {
				System.out.println(i);
				long t0 = System.currentTimeMillis();
				GraphIO.loadGraphFromFile(FILENAME,
						ImplementationType.STANDARD, null);
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
