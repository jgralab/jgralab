package de.uni_koblenz.jgralabtest.algolib.kdtree;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

import de.uni_koblenz.jgralabtest.algolib.Stopwatch;

public class KDTree<P extends Point> {

	private static final int DELTA = 4;

	private abstract class TreeNode {
		private Key parent;
		protected int level;

		public TreeNode(Key parent, int level) {
			super();
			this.parent = parent;
			this.level = level;
		}

		public abstract boolean isLeaf();

	}

	private class Key extends TreeNode {
		private int position;
		private double value;
		private TreeNode left;
		private TreeNode right;

		public Key(Key parent, int level, int position, double value) {
			super(parent, level);
			this.position = position;
			this.value = value;
		}

		public boolean isLeaf() {
			return false;
		}

		public String toString() {
			StringBuilder out = new StringBuilder();
			appendIndent(out, level);
			out.append("position: " + position);
			out.append(" / key: " + value);
			out.append("\n");
			appendIndent(out, level);
			out.append("left:");
			out.append('\n');
			out.append(left.toString());
			out.append('\n');
			appendIndent(out, level);
			out.append("right:");
			out.append('\n');
			out.append(right.toString());
			return out.toString();
		}

	}

	private class Leaf extends TreeNode {
		private LinkedList<P> values;

		public Leaf(Key parent, int level, LinkedList<P> values) {
			super(parent, level);
			this.values = values;
		}

		public boolean isLeaf() {
			return true;
		}

		public String toString() {
			StringBuilder out = new StringBuilder();
			appendIndent(out, level);
			out.append("Points:");
			for (P current : values) {
				out.append('\n');
				appendIndent(out, level);
				out.append(" - ");
				out.append(current.toString());
			}
			return out.toString();
		}

	}

	private int dimension;
	private int maxSegmentSize;
	private TreeNode root;
	private LinkedList<P> rawData;

	public KDTree(LinkedList<P> rawData, int maxSegmentSize) {
		this.maxSegmentSize = maxSegmentSize;
		assert (!rawData.isEmpty());
		dimension = rawData.get(0).getDimension();
		this.rawData = rawData;

		root = createTree(null, 0, rawData, 0);
	}

	public int getDimension() {
		return dimension;
	}

	private TreeNode createTree(Key parent, int level, LinkedList<P> segment,
			int position) {
		int segmentSize = segment.size();
		if (segmentSize <= maxSegmentSize) {
			return new Leaf(parent, level, segment);
		}
		double mean = 0;
		for (P current : segment) {
			mean += current.get(position);
		}
		mean /= segmentSize;
		// segment will become the left list
		LinkedList<P> left = new LinkedList<P>();
		// elements bigger than mean will be filtered out into the right list
		LinkedList<P> right = new LinkedList<P>();
		Iterator<P> iter = segment.iterator();
		while (iter.hasNext()) {
			P current = iter.next();
			if (current.get(position) > mean) {
				right.add(current);
			} else {
				left.add(current);
			}
		}

		assert (segmentSize == left.size() + right.size());

		Key out = new Key(parent, level, position, mean);

		int newPosition = (position + 1) % dimension;
		int newLevel = level + 1;
		TreeNode leftTree = createTree(out, newLevel, left, newPosition);
		TreeNode rightTree = createTree(out, newLevel, right, newPosition);

		out.left = leftTree;
		out.right = rightTree;

		return out;
	}

	public String toString() {
		return root.toString();
	}

	private void appendIndent(StringBuilder out, int level) {
		int indent = level * DELTA;
		for (int i = 0; i < indent; i++) {
			out.append(' ');
		}
	}

	public List<P> getNearestNeighborsExhaustively(P point, int count) {
		List<P> nearestNeighbors = new ArrayList<P>(count);
		double[] minimalDistances = new double[count];
		for (int i = 0; i < count; i++) {
			nearestNeighbors.add(null);
			minimalDistances[i] = Double.POSITIVE_INFINITY;
		}
		findNearestNeighborsInSegment(point, count, nearestNeighbors,
				minimalDistances, rawData);
		return nearestNeighbors;
	}

	public List<P> getNearestNeighbors(P point, int count) {
		// initialize helper structures
		List<P> nearestNeighbors = new ArrayList<P>(count);
		double[] minimalDistances = new double[count];
		for (int i = 0; i < count; i++) {
			nearestNeighbors.add(null);
			minimalDistances[i] = Double.POSITIVE_INFINITY;
		}

		TreeNode startAt = root;

		findNearestNeighborsInSubtree2(point, count, nearestNeighbors,
				minimalDistances, startAt);

		return nearestNeighbors;
	}

	private void findNearestNeighborsInSubtree2(P point, int count,
			List<P> nearestNeighbors, double[] minimalDistances,
			TreeNode startAt) {
		if (startAt.isLeaf()) {
			findNearestNeighborsInSegment(point, count, nearestNeighbors,
					minimalDistances, ((Leaf) startAt).values);
		} else {
			Key keyNode = (Key) startAt;
			int position = keyNode.position;
			double key = keyNode.value;
			TreeNode nextNode;
			TreeNode alternativeNode;
			if (point.get(position) > key) {
				nextNode = keyNode.right;
				alternativeNode = keyNode.left;
			} else {
				nextNode = keyNode.left;
				alternativeNode = keyNode.right;
			}

			findNearestNeighborsInSubtree2(point, count, nearestNeighbors,
					minimalDistances, nextNode);

			double squaredDistanceFromCurrentHyperplane = ((Point) point)
					.get(keyNode.position)
					- keyNode.value;
			squaredDistanceFromCurrentHyperplane *= squaredDistanceFromCurrentHyperplane;

			if (squaredDistanceFromCurrentHyperplane < minimalDistances[count - 1]) {
				findNearestNeighborsInSubtree2(point, count, nearestNeighbors,
						minimalDistances, alternativeNode);
			}
		}
	}

	private Leaf getMatchingLeaf(TreeNode node, Point point) {
		if (node.isLeaf()) {
			return ((Leaf) node);
		}
		Key keyNode = (Key) node;
		int position = keyNode.position;
		double key = keyNode.value;
		if (point.get(position) > key) {
			return getMatchingLeaf(keyNode.right, point);
		} else {
			return getMatchingLeaf(keyNode.left, point);
		}
	}

	private void findNearestNeighborsInSubtree(P point, int count,
			List<P> nearestNeighbors, double[] minimalDistances,
			TreeNode startAt) {
		if (!startAt.isLeaf()) {
			System.out.println("looking for nearest neighbors at\n"
					+ ((Key) startAt).value
					+ (startAt == root ? " ROOT " : " level: "
							+ ((Key) startAt).level));
		}
		// search segment of given point
		Leaf leaf = getMatchingLeaf(startAt, point);
		LinkedList<P> segment = leaf.values;

		// compute best nearest neighbor candidates from segment
		findNearestNeighborsInSegment(point, count, nearestNeighbors,
				minimalDistances, segment);

		// go back up the tree and probe for better neighbors in other segments
		TreeNode currentTreeNode = leaf;
		while (currentTreeNode != startAt) {
			TreeNode lastTreeNode = currentTreeNode;
			currentTreeNode = currentTreeNode.parent;

			Key currentKey = (Key) currentTreeNode;
			// compute distance from hyperplane that is described by the current
			// tree node
			double squaredDistanceFromCurrentHyperplane = ((Point) point)
					.get(currentKey.position)
					- currentKey.value;
			squaredDistanceFromCurrentHyperplane *= squaredDistanceFromCurrentHyperplane;

			// if this distance is smaller than the worst nearest neighbor found
			// so far, better neighbors can be found.
			if (squaredDistanceFromCurrentHyperplane < minimalDistances[count - 1]) {
				if (lastTreeNode == currentKey.left) {
					findNearestNeighborsInSubtree(point, count,
							nearestNeighbors, minimalDistances,
							currentKey.right);
					// leaf = getMatchingLeaf(currentKey.right, point);
				} else {
					assert (lastTreeNode == currentKey.right);
					findNearestNeighborsInSubtree(point, count,
							nearestNeighbors, minimalDistances, currentKey.left);
					// leaf = getMatchingLeaf(currentKey.left, point);
				}
				segment = leaf.values;
				// try to find better nearest neighbors in new segment
				findNearestNeighborsInSegment(point, count, nearestNeighbors,
						minimalDistances, segment);
			}
		}
	}

	private void findNearestNeighborsInSegment(P point, int count,
			List<P> nearestNeighbors, double[] minimalDistances,
			LinkedList<P> segment) {
		// System.out.println(segment);
		for (P current : segment) {
			Point currentPoint = (Point) current;
			double currentDistance = point.squaredDistance(currentPoint);
			// don't return the same or equal point as nearest neighbor
			if (!currentPoint.equals(point)) {
				for (int i = 0; i < count; i++) {
					if (nearestNeighbors.get(i) == null) {
						nearestNeighbors.set(i, current);
						minimalDistances[i] = currentDistance;
						break;
					}
					if (currentDistance < minimalDistances[i]) {
						// System.out.println("Found new nearest neighbor: "
						// + current + "with distance " + currentDistance
						// + " inserting into " + nearestNeighbors
						// + " with distances "
						// + Arrays.toString(minimalDistances));
						// shift right
						for (int j = count - 1; j > i; j--) {
							minimalDistances[j] = minimalDistances[j - 1];
							nearestNeighbors
									.set(j, nearestNeighbors.get(j - 1));
							// System.out.println("Debug: " + nearestNeighbors);
						}
						// write value
						minimalDistances[i] = currentDistance;
						nearestNeighbors.set(i, current);
						// System.out.println("Result: " + nearestNeighbors);
						// System.out.println();
						break;
					}

				}
			}
		}
	}

	public static void main(String[] args) {
		// common parameters
		int count = 3;
		int segmentSize = 100;
		int length = 110;
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
				+ sw.getDuration() + "ms.");
		sw.reset();
		sw.start();
		List<Point2> nearestNeighbors2 = tree.getNearestNeighbors(point, count);
		sw.stop();
		System.out.println(nearestNeighbors2 + "\n   by KD-tree search    in "
				+ sw.getDuration() + "ms.");
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
}
