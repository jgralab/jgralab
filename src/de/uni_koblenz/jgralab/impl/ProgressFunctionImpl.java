/*
 * JGraLab - The Java graph laboratory
 * (c) 2006-2007 Institute for Software Technology
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

import de.uni_koblenz.jgralab.ProgressFunction;

public class ProgressFunctionImpl implements ProgressFunction {
	private static final int DEFAULTCHARS = 60;

	private long size;
	private long chars;
	private long currentChar;
	private long time;
	private PrintStream out;

	public ProgressFunctionImpl() {
		this(System.out, DEFAULTCHARS);
	}

	public ProgressFunctionImpl(int chars) {
		this(System.out, chars);
	}

	public ProgressFunctionImpl(PrintStream out) {
		this(out, DEFAULTCHARS);
	}

	public ProgressFunctionImpl(PrintStream out, int chars) {
		this.out = out;
		this.chars = chars;
	}

	public long getInterval() {
		return chars > size ? 1 : size / chars;
	}

	public void init(long size) {
		out.println("processing " + size + " elements");
		currentChar = 0;
		this.size = size;
		out.print("[");
		out.flush();
		time = System.currentTimeMillis();
	}

	public void progress(long progress) {
		if (currentChar < chars) {
			out.print("#");
			out.flush();
			currentChar++;
		}
	}

	public void finished() {
		for (long i = currentChar; i < chars; i++)
			out.print("#");
		out.println("]");
		out.println("elapsed time: "
				+ ((System.currentTimeMillis() - time) / 1000.0) + " seconds");
	}
}
