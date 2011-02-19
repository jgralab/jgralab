/*
 * JGraLab - The Java Graph Laboratory
 * 
 * Copyright (C) 2006-2011 Institute for Software Technology
 *                         University of Koblenz-Landau, Germany
 *                         ist@uni-koblenz.de
 * 
 * For bug reports, documentation and further information, visit
 * 
 *                         http://jgralab.uni-koblenz.de
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
package de.uni_koblenz.jgralabtest.algolib.nonjunit.kdtree;

import java.util.LinkedList;
import java.util.List;
import java.util.Random;

import de.uni_koblenz.jgralabtest.algolib.nonjunit.Stopwatch;

public class TryKDTree {
	public static void main(String[] args) {
		// common parameters
		int count = 4;
		int segmentSize = 100;
		int length = 500000;
		double min = -100;
		double max = 100;
		Random rng = new Random();

		Stopwatch sw = new Stopwatch();
		System.out.println("Creating random list with " + length + " points.");
		sw.start();
		LinkedList<Point2> list = createRandomList(rng, min, max, length);
		sw.stop();
		System.out.println("List created in " + sw.getDuration() + "ms.");
		Point2 point = createRandomPoint(min, max, rng);

		System.out
				.println("Searching for " + count + " nearest neighbors of "
						+ point + "\nusing KD-tree with segment size of "
						+ segmentSize);

		sw.reset();
		sw.start();
		KDTree<Point2> tree = new KDTree<Point2>(list, segmentSize);
		sw.stop();

		System.out.println("KD-tree built in " + sw.getDuration() + "ms.");

		sw.reset();
		sw.start();
		List<Point2> nearestNeighbors1 = tree.getNearestNeighborsExhaustively(
				point, count);
		sw.stop();
		System.out.println(nearestNeighbors1 + "\n   by exhaustive search in "
				+ sw.getNanoDuration() / 1000.0 + " µsec.");

		for (int i = 0; i < 10; i++) {
			sw.reset();
			sw.start();
			List<Point2> nearestNeighbors2 = tree.getNearestNeighbors(point,
					count);
			sw.stop();
			System.out.println(nearestNeighbors2
					+ "\n   by KD-tree search    in " + sw.getNanoDuration()
					/ 1000.0 + " µsec.");
		}
		System.out.println();
		System.out.println("Fini.");
		// System.out.println(tree);
	}

	private static LinkedList<Point2> createRandomList(Random rng, double min,
			double max, int length) {
		LinkedList<Point2> list = new LinkedList<Point2>();
		for (int i = 0; i < length; i++) {
			Point2 newPoint = createRandomPoint(min, max, rng);
			// System.out.println(newPoint);
			list.add(newPoint);
		}
		return list;
	}

	private static Point2 createRandomPoint(double min, double max, Random rng) {
		double x = rng.nextDouble() * (max - min) + min;
		double y = rng.nextDouble() * (max - min) + min;
		Point2 newPoint = new Point2(x, y);
		return newPoint;
	}

	// private static LinkedList<Point2> createSmallList() {
	// LinkedList<Point2> list = new LinkedList<Point2>();
	// Point2 e = new Point2(1, 1);
	// list.add(e);
	// list.add(new Point2(3, 3.5));
	// list.add(new Point2(4.5, 4));
	// list.add(new Point2(4, 2));
	// list.add(new Point2(2, 1.5));
	// list.add(new Point2(6, 5));
	// list.add(new Point2(1, 3));
	// list.add(new Point2(5, 0.5));
	// return list;
	// }
}
