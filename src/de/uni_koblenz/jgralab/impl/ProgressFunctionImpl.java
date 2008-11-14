/*
 * JGraLab - The Java graph laboratory
 * (c) 2006-2008 Institute for Software Technology
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

/**
 * An implementation of a ProgressFunction which displays an "ascii art"
 * progress bar on a PrintStream, e.g. System.out.
 * 
 * @author riediger
 * 
 */
public class ProgressFunctionImpl implements ProgressFunction {
	private static final int DEFAULTLENGTH = 60;

	private long totalElements;
	private long length;
	private long startTime;
	private int currentChar;
	private PrintStream printStream;

	/**
	 * Creates a ProgressFunction with default length writing to System.out.
	 */
	public ProgressFunctionImpl() {
		this(System.out, DEFAULTLENGTH);
	}

	/**
	 * Creates a ProgressFunction with the specified
	 * <code>length</length> writing to System.out.
	 * 
	 * @param length
	 *            number of characters
	 */
	public ProgressFunctionImpl(int length) {
		this(System.out, length);
	}

	/**
	 * Creates a ProgressFunction with default length writing to the specified
	 * <code>printStreamStream</code>.
	 * 
	 * @param printStream
	 *            a PrintStream where the progress bar is printed
	 */
	public ProgressFunctionImpl(PrintStream printStream) {
		this(printStream, DEFAULTLENGTH);
	}

	/**
	 * Creates a ProgressFunction which displays an "ascii art" progress bar on
	 * the specified PrintStream <code>out</code> with <code>length</code>
	 * steps. After finishing, the time consumed is also printed.
	 * 
	 * For example, a progress bar with length 10 looks like this after 6
	 * updates:
	 * 
	 * [#######
	 * 
	 * and like this after finishing:
	 * 
	 * [##########]
	 * 
	 * @param printStream
	 *            a PrintStream where the progress bar is printed
	 * @param length
	 *            number of characters
	 * 
	 */
	public ProgressFunctionImpl(PrintStream printStream, int length) {
		this.printStream = printStream;
		this.length = length;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.uni_koblenz.jgralab.ProgressFunction#getInterval()
	 */
	@Override
	public long getUpdateInterval() {
		return length > totalElements ? 1 : totalElements / length;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.uni_koblenz.jgralab.ProgressFunction#init(long)
	 */
	@Override
	public void init(long elements) {
		printStream.println("processing " + elements + " elements");
		currentChar = 0;
		this.totalElements = elements;
		printStream.print("[");
		printStream.flush();
		startTime = System.currentTimeMillis();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.uni_koblenz.jgralab.ProgressFunction#progress(long)
	 */
	@Override
	public void progress(long processedElements) {
		if (currentChar < length) {
			printStream.print("#");
			printStream.flush();
			currentChar++;
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.uni_koblenz.jgralab.ProgressFunction#finished()
	 */
	@Override
	public void finished() {
		long stopTime = System.currentTimeMillis();
		for (long i = currentChar; i < length; i++) {
			printStream.print("#");
		}
		printStream.println("]");
		printStream.println("elapsed time: "
				+ ((stopTime - startTime) / 1000.0) + "s");
	}
}
