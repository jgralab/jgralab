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
package de.uni_koblenz.jgralabtest.algolib.nonjunit;

public class Stopwatch {
	private long starttime;
	private long endtime;
	private int state;

	public Stopwatch() {
		state = 0;
	}

	public void start() {
		if (state != 0) {
			throw new IllegalStateException();
		}
		starttime = System.nanoTime();
		state = 1;
	}

	public void stop() {
		if (state != 1) {
			throw new IllegalStateException();
		}
		endtime = System.nanoTime();
		state = 2;
	}

	public long getDuration() {
		if (state != 2) {
			throw new IllegalStateException();
		}
		return (endtime - starttime) / 1000000;
	}
	
	public long getNanoDuration() {
		if (state != 2) {
			throw new IllegalStateException();
		}
		return endtime - starttime;
	}

	public void reset() {
		starttime = endtime = state = 0;
	}

	public String getDurationString() {
		return "Duration: " + getDuration() / 1000.0 + " sec";
	}
}
