package de.uni_koblenz.jgralab.impl;

import java.util.BitSet;

public interface InternalAttributedElement {
	/**
	 * Marks the given attribute as being set (or unset).
	 * 
	 * @param attr
	 */
	public void internalMarkAttributeAsSet(int attrIdx, boolean set);

	public BitSet internalGetSetAttributesBitSet();

	public void internalInitializeSetAttributesBitSet();
}
