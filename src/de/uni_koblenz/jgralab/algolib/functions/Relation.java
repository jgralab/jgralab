package de.uni_koblenz.jgralab.algolib.functions;

// TODO maybe add further methods for relations

public interface Relation<DOMAIN1, DOMAIN2> {
	public boolean get(DOMAIN1 parameter1, DOMAIN2 parameter2);

	public void set(DOMAIN1 parameter1, DOMAIN2 parameter2, boolean value);

	public boolean isDefined(DOMAIN1 parameter1, DOMAIN2 parameter2);
}
