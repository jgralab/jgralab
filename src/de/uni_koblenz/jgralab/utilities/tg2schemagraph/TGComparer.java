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
package de.uni_koblenz.jgralab.utilities.tg2schemagraph;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

public class TGComparer {

	public static boolean compareTGFiles(String filename1, String filename2) {

		return new TGComparer().compare(filename1, filename2);
	}

	public TGComparer() {

	}

	public boolean compare(String filename1, String filename2) {
		try {
			ArrayList<String> tg1 = readIn(filename1);
			ArrayList<String> tg2 = readIn(filename2);

			System.out.println("Comparing (left) Filename:  " + filename1);
			System.out.println("with (right) Filename:      " + filename2);

			Collections.sort(tg1);
			Collections.sort(tg2);

			dropEmptyLines(tg1);
			dropEmptyLines(tg2);

			lineDiff(tg1, tg2);

			// printLines(tg1);
			// printLines(tg2);

		} catch (IOException ex) {
			System.out.println(ex);
		}

		return false;
	}

	private void dropEmptyLines(ArrayList<String> lines) {

		ArrayList<String> dropList = new ArrayList<>();
		for (String line : lines) {
			if (line.trim().length() == 0) {
				dropList.add(line);
			}
		}

		for (String line : dropList) {
			lines.remove(line);
		}
	}

	private void lineDiff(ArrayList<String> linesLeft,
			ArrayList<String> linesRight) {

		if (linesLeft.size() != linesRight.size()) {
			System.out.println("Number of lines are different.");
		}

		String left, right;

		int i;
		for (i = 0; i < linesLeft.size() && i < linesRight.size(); i++) {
			left = linesLeft.get(i);
			right = linesRight.get(i);

			String[] leftSplit = split(left);
			String[] rightSplit = split(right);

			boolean equal = leftSplit.length == rightSplit.length;

			int min = leftSplit.length < rightSplit.length ? leftSplit.length
					: rightSplit.length;
			for (int j = 0; j < min; j++) {
				String leftTemp = leftSplit[j];
				String rightTemp = rightSplit[j];
				if (leftTemp.contains("{") && rightTemp.contains("{")) {
					if (j % 2 == 0) {
						equal &= compareSubLine(leftTemp, rightTemp);
					} else {
						equal &= compareString(leftTemp, rightTemp);
					}

				} else {
					equal &= !(leftSplit[j].contains("{") || rightSplit[j]
							.contains("{"));
				}

			}

			if (!equal) {
				System.out.println("error");
				System.out.println("left : " + left);
				System.out.println("right: " + right);
				// } else {
				// System.out.println("compare");
				// System.out.println(left);
				// System.out.println(right);
			}

		}
	}

	private boolean compareAttributes(String left, String right) {
		String[] leftSplit = left.split(",");
		String[] rightSplit = right.split(",");

		if (leftSplit.length != rightSplit.length) {
			return false;
		}

		for (int i = 0; i < leftSplit.length; i++) {
			leftSplit[i] = leftSplit[i].trim();
			rightSplit[i] = rightSplit[i].trim();
		}

		Arrays.sort(leftSplit);
		Arrays.sort(rightSplit);

		boolean equal = true;

		for (int i = 0; i < leftSplit.length; i++) {
			equal &= leftSplit[i].equals(rightSplit[i]);
		}
		return equal;
	}

	private boolean compareString(String left, String right) {
		return left.equals(right);
	}

	private boolean compareSubLine(String left, String right) {

		left = left.replace('{', '\"');
		right = right.replace('{', '\"');
		left = left.replace('}', '\"');
		right = right.replace('}', '\"');

		String[] leftSplit = left.split("\"");
		String[] rightSplit = right.split("\"");

		if (leftSplit.length != rightSplit.length) {
			return false;
		}

		boolean equal = leftSplit[0].equals(rightSplit[0]);
		if (leftSplit.length == 2) {
			return false;
		}
		if (leftSplit.length > 2) {
			equal &= compareAttributes(leftSplit[1], rightSplit[1]);
			equal &= rightSplit[2].equals(rightSplit[2]);
		}
		if (leftSplit.length > 3) {
			for (int i = 3; i < leftSplit.length; i++) {
				equal &= leftSplit[i].equals(rightSplit[i]);
			}
		}

		return equal;
	}

	private String[] split(String string) {
		return string.split("\"");
	}

	private ArrayList<String> readIn(String filename) throws IOException {
		BufferedReader br = null;
		ArrayList<String> stringList = null;
		try {
			br = new BufferedReader(new FileReader(filename));

			stringList = new ArrayList<>();

			while (br.ready()) {
				stringList.add(br.readLine());
			}
		} finally {
			if (br != null) {
				br.close();
			}
		}
		return stringList;
	}

	public static void main(String[] args) {
		for (String filename : args) {
			TGComparer comp = new TGComparer();

			comp.compare(filename, filename + ".testSCHEMA");
		}
		System.out.println("Fini.");
	}
}
