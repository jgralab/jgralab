/*
 * JGraLab - The Java Graph Laboratory
 *
 * Copyright (C) 2006-2014 Institute for Software Technology
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
package de.uni_koblenz.jgralabtest.algolib.nonjunit.kdtree;

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

		@Override
		public boolean isLeaf() {
			return false;
		}

		@Override
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

		@Override
		public boolean isLeaf() {
			return true;
		}

		@Override
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
		LinkedList<P> left = new LinkedList<>();
		// elements bigger than mean will be filtered out into the right list
		LinkedList<P> right = new LinkedList<>();
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

	@Override
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
		List<P> nearestNeighbors = new ArrayList<>(count);
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
		List<P> nearestNeighbors = new ArrayList<>(count);
		double[] minimalDistances = new double[count];
		for (int i = 0; i < count; i++) {
			nearestNeighbors.add(null);
			minimalDistances[i] = Double.POSITIVE_INFINITY;
		}

		TreeNode startAt = root;

		findNearestNeighborsInSubtree(point, count, nearestNeighbors,
				minimalDistances, startAt);

		return nearestNeighbors;
	}

	@SuppressWarnings("unchecked")
	private void findNearestNeighborsInSubtree(P point, int count,
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

			findNearestNeighborsInSubtree(point, count, nearestNeighbors,
					minimalDistances, nextNode);

			double squaredDistanceFromCurrentHyperplane = ((Point) point)
					.get(keyNode.position) - keyNode.value;
			squaredDistanceFromCurrentHyperplane *= squaredDistanceFromCurrentHyperplane;

			if (squaredDistanceFromCurrentHyperplane < minimalDistances[count - 1]) {
				findNearestNeighborsInSubtree(point, count, nearestNeighbors,
						minimalDistances, alternativeNode);
			}
		}
	}

	private void findNearestNeighborsInSegment(P point, int count,
			List<P> nearestNeighbors, double[] minimalDistances,
			LinkedList<P> segment) {

		for (P current : segment) {
			Point currentPoint = current;
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
						for (int j = count - 1; j > i; j--) {
							minimalDistances[j] = minimalDistances[j - 1];
							nearestNeighbors
									.set(j, nearestNeighbors.get(j - 1));
							// System.out.println("Debug: " + nearestNeighbors);
						}
						// write value
						minimalDistances[i] = currentDistance;
						nearestNeighbors.set(i, current);
						break;
					}

				}
			}
		}
	}

	public List<P> getArea(P point, double radius) {
		List<P> area = new LinkedList<>();
		TreeNode startAt = root;
		appendToAreaFromSubtree(point, radius * radius, area, startAt);
		return area;
	}

	@SuppressWarnings("unchecked")
	private void appendToAreaFromSubtree(P point, double squaredRadius,
			List<P> area, TreeNode startAt) {
		if (startAt.isLeaf()) {
			appendToAreaFromSegment(point, squaredRadius, area,
					((Leaf) startAt).values);
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

			appendToAreaFromSubtree(point, squaredRadius, area, nextNode);

			double squaredDistanceFromCurrentHyperplane = ((Point) point)
					.get(keyNode.position) - keyNode.value;
			squaredDistanceFromCurrentHyperplane *= squaredDistanceFromCurrentHyperplane;

			if (squaredDistanceFromCurrentHyperplane <= squaredRadius) {
				appendToAreaFromSubtree(point, squaredRadius, area,
						alternativeNode);
			}
		}
	}

	private void appendToAreaFromSegment(P point, double squaredRadius,
			List<P> area, LinkedList<P> segment) {
		for (P current : segment) {
			Point currentPoint = current;
			double currentDistance = point.squaredDistance(currentPoint);

			if (currentDistance <= squaredRadius) {
				area.add(current);
			}
		}
	}
}
