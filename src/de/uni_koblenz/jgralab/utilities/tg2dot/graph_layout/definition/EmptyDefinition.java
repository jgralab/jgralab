package de.uni_koblenz.jgralab.utilities.tg2dot.graph_layout.definition;

import java.util.HashSet;
import java.util.Set;

/**
 * An EmptyDefinition provides a description of an {@link Definition} which
 * couldn't be transformed from a {@link TemporaryDefinitionStruct} to a
 * {@link TypeDefinition} or an {@link ElementDefinition}.
 * 
 * @author ist@uni-koblenz.de
 */
public class EmptyDefinition implements Definition {

	/**
	 * An empty set.
	 */
	private static Set<String> emptySet = new HashSet<String>();

	/**
	 * The {@link TemporaryDefinitionStruct} this EmptyDefinition is constructed
	 * from.
	 */
	public TemporaryDefinitionStruct struct;

	/**
	 * Constructs an EmptyDefinition from a {@link TemporaryDefinitionStruct}.
	 * 
	 * @param struct
	 *            A {@link TemporaryDefinitionStruct}.
	 */
	protected EmptyDefinition(TemporaryDefinitionStruct struct) {
		this.struct = struct;
	}

	@Override
	public void addNonExistingAttributes(Definition spec) {
	}

	@Override
	public String getAttributeValue(String name) {
		return null;
	}

	@Override
	public Set<String> getAttributeNames() {
		return emptySet;
	}

	@Override
	public void overwriteAttributes(Definition spec) {
	}

	@Override
	public void setAttribute(String name, String value) {
	}

	@Override
	public Definition clone() {
		return this;
	}
}
