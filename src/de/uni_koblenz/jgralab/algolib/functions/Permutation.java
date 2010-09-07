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
