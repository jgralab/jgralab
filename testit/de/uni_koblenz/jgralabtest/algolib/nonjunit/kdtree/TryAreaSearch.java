/*
 * JGraLab - The Java Graph Laboratory
 * 
 * Copyright (C) 2006-2010 Institute for Software Technology
 *                         University of Koblenz-Landau, Germany
 *                         ist@uni-koblenz.de
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

public class TryAreaSearch {
	private static LinkedList<Point2> createSmallList() {
		LinkedList<Point2> list = new LinkedList<Point2>();
		Point2 e = new Point2(1, 1);
		list.add(e);
		list.add(new Point2(3, 3.5));
		list.add(new Point2(4.5, 4));
		list.add(new Point2(4, 2));
		list.add(new Point2(2, 1.5));
		list.add(new Point2(6, 5));
		list.add(new Point2(1, 3));
		list.add(new Point2(5, 0.5));
		return list;
	}

	public static void main(String[] args) {
		LinkedList<Point2> list = createSmallList();
		KDTree<Point2> tree = new KDTree<Point2>(list, 1);
		// System.out.println(tree);
		List<Point2> area = tree.getArea(new Point2(3, 3.5), 2);
		System.out.println(area);
	}
}
