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
package de.uni_koblenz.jgralab.algolib.functions;

import de.uni_koblenz.jgralab.algolib.functions.entries.PermutationEntry;

/**
 * Defines the interface for a permutation. The method names are in analogy to
 * the interface <code>Function</code>. A permutation is a function whose domain
 * is the natural numbers without 0. All implementing classes have to ensure
 * this. A permutation knows about the number of stored elements (length) and
 * provides similar iterators. However, instead of the domain elements it
 * returns an iterator over all range elements.
 * 
 * @author strauss@uni-koblenz.de
 * 
 * @param <RANGE>
 *            the range of the function.
 */
public interface Permutation<RANGE> extends Iterable<PermutationEntry<RANGE>> {

	public RANGE get(int index);

	public void add(RANGE value);

	public boolean isDefined(int index);

	public Iterable<RANGE> getRangeElements();

	public int length();
}
