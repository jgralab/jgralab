/*
 * JGraLab - The Java graph laboratory
 * (c) 2006-2009 Institute for Software Technology
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

package de.uni_koblenz.jgralab.impl;

import java.io.PrintStream;

/**
 * FreeIndexList manages used and free indices in the vertex and edge arrays of
 * a graph. Valid index values are &gt;= 1 and &lt;= getSize().<br/>
 * <br/>
 * 
 * FreeIndexList resembles "Run Length Encoding" of binary images. It records
 * "runs" of used and free index areas. A negative value u in the runs array
 * means (-u) used entries, a positive value f stands for f free entries.<br/>
 * <br/>
 * 
 * The first free index is either found at the beginning (if
 * <code>runs[0] > 0</code>) or in the second run (<code>runs[1]</code>).
 * FreeIndexList cares for maintaining a compacted run array at all times. This
 * means that allocating and/or freeing an index can result in reorganization
 * and/or expansion of the runs array.<br/>
 * <br/>
 * 
 * The length of the runs array (<code>runCount</code>) is equal to the maximum
 * index value (<code>used+free</code>) in the worst case. This can happen if
 * only every second index is used. In practice, vertex and edge arrays contain
 * large contiguous areas of used and free indexes, such that the size of the
 * runs array is significantly lower than the size of the vertex and edge
 * arrays. In an optimal case, there are only 2 runs.<br/>
 * <br/>
 * 
 * <code>allocateIndex()</code> is O(1) if no reorganisation is required.
 * Otherwise, it is O(runs.length). Reorganisation takes place in two
 * situations:
 * <ol>
 * <li>When the first run is a free run, a new used run has to be inserted.</li>
 * <li>When a free run becomes empty, neighbouring used runs have to be merged.</li>
 * </ol>
 * 
 * <code>freeIndex()</code> and <code>freeRange()</code> are O(runs.length) in
 * the worst case. If no reorganisation takes place, a relatively cheap
 * O(runs.length) operation is required to locate the run where the index is
 * located (summing up the values from beginning of the runs array).
 * Reorganisation takes place in these situations:
 * <ul>
 * <li>a used run becomes empty,</li>
 * <li>a used run has to be split "in the middle",</li>
 * <li>the first run is freed at the beginning.</li>
 * </ul>
 * 
 * FreeIndexList is implemented optimistically, e.g. the user has to take care
 * that no index is freed more than once. However, assumptions, preconditions,
 * and structure of the list can be checked by enabling assertions.<br/>
 * <br/>
 * 
 * Take care: the implementation is somehow "tricky" but well documented :-)<br/>
 * <br/>
 * 
 * @author ist@uni-koblenz.de
 */
public class FreeIndexList {

	// runs array
	private int[] runs;

	// total number of free entries
	private int free;

	// total number of used entries
	private int used;

	// number of runs, e.g. the first unused index in runs (AKA the first index
	// i for which runs[i] == 0 holds)
	private int runCount;

	/**
	 * Creates a new FreeIndexList where <code>initialFreeElements</code>
	 * indexes are free, no index is used.
	 * 
	 * @param initialFreeElements
	 *            count of initial free indexes
	 */
	public FreeIndexList(int initialFreeElements) {
		assert initialFreeElements > 0;
		runs = new int[16];
		free = initialFreeElements;
		used = 0;
		runs[0] = initialFreeElements;
		runCount = 1;
		assert isHealthy();
	}

	/**
	 * Computes the first free index.
	 * 
	 * @return the first free index, or 0 if no more indexes are available.
	 */
	public int allocateIndex() {
		if (free == 0) {
			return 0;
		}
		assert runCount > 0;
		int result = 0;
		if (runs[0] > 0) {
			// first run is a "free" run
			result = 1;
			// decrease free count
			--runs[0];
			if (runs[0] == 0) {
				// free run is empty
				if (runCount > 1) {
					// there are more runs, shift them one position to the left
					System.arraycopy(runs, 1, runs, 0, runCount - 1);
					runs[--runCount] = 0;
					// one more used;
					--runs[0];
				} else {
					// convert free run to used run
					runs[0] = -1;
				}
			} else {
				// insert a "used" entry as first run
				if (runCount >= runs.length) {
					// allocate more space
					int[] newRuns = new int[runs.length * 2];
					System.arraycopy(runs, 0, newRuns, 1, runs.length);
					runs = newRuns;
				} else {
					System.arraycopy(runs, 0, runs, 1, runCount);
				}
				++runCount;
				runs[0] = -1;
			}
		} else {
			// first run is a "used" run
			assert runCount >= 2;
			result = (-runs[0]) + 1;
			// use one
			--runs[0];
			--runs[1];
			if (runs[1] == 0) {
				// a "free" run becomes empty
				if (runCount > 2) {
					// merge used runs
					assert runs[2] < 0;
					runs[0] += runs[2];
					System.arraycopy(runs, 3, runs, 1, runCount - 3);
					// clear last 2 entries
					runs[--runCount] = 0;
					runs[--runCount] = 0;
				} else {
					// only "remove" last entry which is already 0
					assert runs[runCount - 1] == 0;
					--runCount;
				}
			}
		}
		--free;
		++used;
		assert isHealthy();
		return result;
	}

	/**
	 * Frees the specified <code>index</code>.
	 * 
	 * @param index
	 *            a used index
	 */
	public void freeIndex(int index) {
		freeRange(index, 1);
	}

	/**
	 * Frees the specified range from <code>index</code> to
	 * <code>index + length - 1</code>.
	 * 
	 * @param index
	 *            a used index
	 * @param length
	 *            the length of the range to be freed
	 */
	public void freeRange(int index, int length) {
		assert runCount > 0;
		assert index > 0;
		assert length > 0;
		assert index + length - 1 <= used + free;

		int begin; // first index of the current run
		int end = 0; // last index of the current run
		int runIndex = 0;
		// search for the run containing the index
		do {
			begin = end + 1;
			end += Math.abs(runs[runIndex]);
			if (index <= end) {
				break;
			}
			++runIndex;
		} while (runIndex < runCount);
		assert runIndex < runCount : "freeRange: index " + index
				+ " out of range 1.." + (used + free);
		assert runs[runIndex] < 0;
		if (index == begin) {
			// freeing at the begin of the "used" run
			assert length <= -runs[runIndex];
			if (runIndex == 0) {
				// frist run contains the index
				if (length == -runs[0]) {
					// current run is completely freed
					if (runCount == 1) {
						// this is the only run
						runs[0] = length;
					} else {
						// there are more runs, add length to runs[1], shift
						// left
						System.arraycopy(runs, 1, runs, 0, runCount - 1);
						assert runs[0] > 0;
						runs[--runCount] = 0;
						runs[0] += length;
					}
				} else {
					// paritally freed, insert a new "unused" run at the
					// front
					if (runCount == runs.length) {
						// run array has to be expanded
						int[] newRuns = new int[runs.length * 2];
						System.arraycopy(runs, 0, newRuns, 1, runCount);
						runs = newRuns;
					} else {
						System.arraycopy(runs, 0, runs, 1, runCount);
					}
					++runCount;
					runs[0] = length;
					runs[1] += length;
				}
			} else {
				// other run (not the first) contains the index
				assert runs[runIndex - 1] > 0;
				if (length == -runs[runIndex]) {
					// current run completely freed
					runs[runIndex - 1] += length;
					if (runIndex == runCount - 1) {
						// the last run was cleared
						runs[--runCount] = 0;
					} else {
						runs[runIndex - 1] += runs[runIndex + 1];
						System.arraycopy(runs, runIndex + 2, runs, runIndex,
								runCount - runIndex - 2);
						runs[--runCount] = 0;
						runs[--runCount] = 0;
					}
				} else {
					// partially freed at the begin of the current run
					runs[runIndex - 1] += length;
					runs[runIndex] += length;
				}
			}
		} else if (index == end) {
			// freeing at the end of the "used" run
			// it can not happen that the current run is freed completetly,
			// this is handled by the case "index==begin" above
			assert length == 1;
			if (runIndex == runCount - 1) {
				// free at the last "used" run, add a new "unused" run
				if (runCount == runs.length) {
					// allocate more memory
					int[] newRuns = new int[runs.length * 2];
					System.arraycopy(runs, 0, newRuns, 0, runCount);
					runs = newRuns;
				}
				runs[runIndex] += length;
				runs[runCount++] = length;
			} else {
				assert runs[runIndex + 1] > 0;
				runs[runIndex] += length;
				assert runs[runIndex] < 0;
				runs[runIndex + 1] += length;

			}
		} else {
			// freeing begins somewhere in the middle of the "used" run
			assert index - begin + length <= -runs[runIndex];
			if (index + length == end + 1) {
				/*
				 * TODO DeadCode
				 * Im Graphen kann man nur eine Id freigeben. Dieser
				 * Fall beschäftigt sich mit dem Freigeben mehrer Ids.
				 */
				// complete "tail" of the current run cleared
				if (runIndex == runCount - 1) {
					// tail of last run cleared, add a new "free" run
					if (runCount == runs.length) {
						// allocate more memory
						int[] newRuns = new int[runs.length * 2];
						System.arraycopy(runs, 0, newRuns, 0, runCount);
						runs = newRuns;
					}
					runs[runIndex] += length;
					runs[runCount++] = length;
				} else {
					assert runs[runIndex + 1] > 0;
					runs[runIndex] += length;
					runs[runIndex + 1] += length;
				}
				assert runs[runIndex] < 0;
			} else {
				// current run has to be split, i.e. 2 new runs are created
				int n = runs[runIndex];
				if (runCount + 2 >= runs.length) {
					// allocate more memory
					int[] newRuns = new int[runs.length * 2];
					System.arraycopy(runs, 0, newRuns, 0, runIndex);
					System.arraycopy(runs, runIndex + 1, newRuns, runIndex + 3,
							runCount - runIndex - 1);
					runs = newRuns;
				} else {
					System.arraycopy(runs, runIndex + 1, runs, runIndex + 3,
							runCount - runIndex - 1);
				}
				runCount += 2;
				runs[runIndex] = -(index - begin);
				runs[runIndex + 1] = length;
				runs[runIndex + 2] = n + (index - begin) + length;
			}
		}
		used -= length;

		free += length;
		assert isHealthy();
	}

	/**
	 * Prints the structure of this FreeIndexList to the specified
	 * {@link PrintStream} <code>ps</code>.
	 * 
	 * @param ps
	 *            a PrintStream
	 */
	@SuppressWarnings("unused")
	private void printArray(PrintStream ps) {
		ps.println("---------------------------------------------------");
		ps.println(this);
		ps.println("free=" + free + ", used=" + used + ", length="
				+ runs.length + ", runCount=" + runCount);
		ps.print(" [");
		for (int run : runs) {
			ps.print(" " + run);
		}
		ps.println(" ]");
		ps.println("---------------------------------------------------");
		ps.flush();
	}

	/**
	 * Computes the runs in the specified array <code>a</code> and initializes
	 * the <code>runs</code> array. Free entries are <code>== null</code>, used
	 * entries <code>!= null</code>. The first element <code>a[0]</code> is not
	 * considered, since valid index values start at 1.
	 * 
	 * @param a
	 *            an array of objects
	 */
	public void reinitialize(Object[] a) {
		int i = 1;
		runCount = used = free = 0;
		while (i < a.length) {
			int cnt = 0;
			while (i < a.length && a[i] != null) {
				++i;
				++cnt;
			}
			if (cnt > 0) {
				if (runCount == runs.length) {
					/*
					 * TODO DeadCode: runs.length>=16 und runCount==0 =>
					 * runs.length==runCount immer false
					 */
					int[] newRuns = new int[runs.length * 2];
					System.arraycopy(runs, 0, newRuns, 0, runCount);
					runs = newRuns;
				}
				runs[runCount++] = -cnt;
				used += cnt;
			}

			cnt = 0;
			while (i < a.length && a[i] == null) {
				++i;
				++cnt;
			}
			if (cnt > 0) {
				if (runCount == runs.length) {
					/*
					 * TODO Dead Code: runs.length>=16 und runCount==0 =>
					 * runs.length==runCount immer false
					 */
					int[] newRuns = new int[runs.length * 2];
					System.arraycopy(runs, 0, newRuns, 0, runCount);
					runs = newRuns;
				}
				runs[runCount++] = cnt;
				free += cnt;
			}
		}
		assert used + free + 1 == a.length;
		assert isHealthy();
	}

	/**
	 * Checks if the structure of this FreeIndexList is ok.
	 * 
	 * @return true if the structure is ok. Otherwise, an assertion will fail
	 *         and isHealthy will not return anything.
	 */
	private boolean isHealthy() {
		// printArray();
		assert runCount > 0;
		assert free >= 0;
		assert used >= 0;
		int sum = 0;
		for (int i = 0; i < runCount; ++i) {
			assert runs[i] != 0 : "runs[" + i + "] must be != 0";
			if (i > 0) {
				assert runs[i - 1] > 0 && runs[i] < 0 || runs[i - 1] < 0
						&& runs[i] > 0 : "used and free runs must alternate";
			}
			sum += Math.abs(runs[i]);
		}
		for (int i = runCount; i < runs.length; ++i) {
			assert runs[i] == 0 : "runs[" + i + "] must be == 0";
		}
		assert sum == used + free;
		return true;
	}

	/**
	 * Gets the number of free index values.
	 * 
	 * @return the number of free index values in this FreeIndexList
	 */
	public int getFree() {
		return free;
	}

	/**
	 * Gets the total number of index values.
	 * 
	 * @return the total number of index values in this FreeIndexList
	 */
	public int getSize() {
		return used + free;
	}

	/**
	 * Gets the number of used index values.
	 * 
	 * @return the number of used index values in this FreeIndexList
	 */
	public int getUsed() {
		return used;
	}

	/**
	 * Appends <code>n</code> free index values at the end of this FreeIndexList
	 * 
	 * @param n
	 *            the number of new free index values
	 */
	public void expandBy(int n) {
		assert n > 0;

		// free a range immediately after current maximum index
		if (runs[runCount - 1] > 0) {
			/*
			 * TODO Dead Code:runs[runCount - 1] > 0 ist niemals gegeben, da
			 * dies bedeutet, dass letzter Eintrag free sein muss => free>0 in
			 * diesem Fall wird die Methode nie aufgerufen
			 */
			// last run was a "free" run, simply add
			runs[runCount - 1] += n;
		} else {
			// append a new free run
			if (runCount == runs.length) {
				/*
				 * TODO Dead Code: wird nur aufgerufen wenn free==0. Aber dann
				 * ist runCount=1<runs.length! also nie runCount == runs.length
				 */
				// allocate more memory
				int[] newRuns = new int[runs.length * 2];
				System.arraycopy(runs, 0, newRuns, 0, runCount);
				runs = newRuns;
			}
			runs[runCount++] = n;
		}
		free += n;
		assert isHealthy();
	}

	/**
	 * Checks whether this FreeIndexList is fragmented, i.e. contains "gaps" of
	 * free indices. Unfragmented lists consist of at most 2 runs, the first run
	 * with used indices, the second with free indices.
	 * 
	 * @return true if this FreeIndexList is fragmented
	 */
	public boolean isFragmented() {
		return (runCount > 2 || runCount == 2 && runs[0] > 0);
	}

}
