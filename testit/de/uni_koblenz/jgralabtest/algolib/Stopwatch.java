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
package de.uni_koblenz.jgralabtest.algolib;

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
