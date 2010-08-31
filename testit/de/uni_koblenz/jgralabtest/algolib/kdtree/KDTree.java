package de.uni_koblenz.jgralabtest.algolib.kdtree;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

public class KDTree<P extends Point> {

	private static final int DELTA = 4;

	private abstract class TreeNode {
		protected int level;

		public TreeNode(int level) {
			super();
			this.level = level;
		}

		public abstract boolean isLeaf();

	}

	private class Key extends TreeNode {
		private int position;
		private double value;
		private TreeNode left;
		private TreeNode right;

		public Key(int level, int position, double value) {
			super(level);
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

		public Leaf(int level, LinkedList<P> values) {
			super(level);
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

		root = createTree(0, rawData, 0);
	}

	public int getDimension() {
		return dimension;
	}

	private TreeNode createTree(int level, LinkedList<P> segment, int position) {
		int segmentSize = segment.size();
		if (segmentSize <= maxSegmentSize) {
			return new Leaf(level, segment);
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

		Key out = new Key(level, position, mean);

		int newPosition = (position + 1) % dimension;
		int newLevel = level + 1;
		TreeNode leftTree = createTree(newLevel, left, newPosition);
		TreeNode rightTree = createTree(newLevel, right, newPosition);

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
}
