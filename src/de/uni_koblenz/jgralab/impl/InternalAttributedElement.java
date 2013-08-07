package de.uni_koblenz.jgralab.impl;

import java.io.IOException;
import java.util.BitSet;

import de.uni_koblenz.jgralab.GraphIO;
import de.uni_koblenz.jgralab.exception.GraphIOException;
import de.uni_koblenz.jgralab.exception.NoSuchAttributeException;

public interface InternalAttributedElement {
	/**
	 * Marks the given attribute as being set (or unset).
	 * 
	 * @param attr
	 */
	public void internalMarkAttributeAsSet(int attrIdx, boolean set);

	public BitSet internalGetSetAttributesBitSet();

	public void internalInitializeSetAttributesBitSet();

	/**
	 * Initializes all values for all
	 * {@link de.uni_koblenz.jgralab.schema.Attribute}s with their
	 * {@code default} values.
	 */
	void internalInitializeAttributesWithDefaultValues();

	/**
	 * Reads a new value for an {@link de.uni_koblenz.jgralab.schema.Attribute}
	 * from a {@code String} value and sets it
	 * 
	 * @param attributeName
	 *            name of the {@link de.uni_koblenz.jgralab.schema.Attribute} to
	 *            set
	 * @param value
	 *            the {@code String} value to read
	 * 
	 * @throws GraphIOException
	 *             if the {@code value} can not become parsed correctly
	 * @throws NoSuchAttributeException
	 *             if the {@code attributeName} is not the name of a valid
	 *             {@link de.uni_koblenz.jgralab.schema.Attribute}
	 */
	public void readAttributeValueFromString(String attributeName, String value)
			throws GraphIOException, NoSuchAttributeException;

	/**
	 * Reads a new value for an {@link de.uni_koblenz.jgralab.schema.Attribute}
	 * from a {@link GraphIO} value and sets it
	 * 
	 * @param io
	 *            the {@link GraphIO} object to read from
	 * @throws GraphIOException
	 *             if the {@link GraphIO} can not become read correctly
	 */
	public void readAttributeValues(GraphIO io) throws GraphIOException;

	/**
	 * Creates a new {@code String} value from the current value of the given
	 * {@link de.uni_koblenz.jgralab.schema.Attribute} with the given
	 * {@link GraphIO}
	 * 
	 * @param io
	 *            the {@link GraphIO} to create the {@code String} value with
	 * @throws IOException
	 * @throws GraphIOException
	 */
	public void writeAttributeValues(GraphIO io) throws IOException,
			GraphIOException;

	/**
	 * Creates a new {@code String} value from the current value of the given
	 * {@link de.uni_koblenz.jgralab.schema.Attribute}
	 * 
	 * @param attributeName
	 *            name of the {@link de.uni_koblenz.jgralab.schema.Attribute} to
	 *            create a {@link java.lang.String} value of
	 * @return the String representation of the given
	 *         {@link de.uni_koblenz.jgralab.schema.Attribute} value
	 * 
	 * @throws IOException
	 * @throws GraphIOException
	 * @throws NoSuchAttributeException
	 *             if the {@code attributeName} is not the name of a valid
	 *             {@link de.uni_koblenz.jgralab.schema.Attribute}
	 */
	public String writeAttributeValueToString(String attributeName)
			throws IOException, GraphIOException, NoSuchAttributeException;
}
