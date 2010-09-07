package de.uni_koblenz.jgralabtest.algolib.kdtree;

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
