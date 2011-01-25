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
