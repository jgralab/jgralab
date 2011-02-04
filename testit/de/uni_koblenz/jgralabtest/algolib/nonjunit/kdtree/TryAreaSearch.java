/*
 * JGraLab - The Java graph laboratory
 * (c) 2006-2010 Institute for Software Technology
 *               University of Koblenz-Landau, Germany
 * 
 *               ist@uni-koblenz.de
 * 
 * Please report bugs to http://serres.uni-koblenz.de/bugzilla
 * 
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
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
