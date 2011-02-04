package de.uni_koblenz.jgralab.algolib.functions;

public interface BinaryIntFunction<DOMAIN1, DOMAIN2> {
	
	public int get(DOMAIN1 parameter1, DOMAIN2 parameter2);

	public void set(DOMAIN1 parameter1, DOMAIN2 parameter2, int value);

	public boolean isDefined(DOMAIN1 parameter1, DOMAIN2 parameter2);
}
