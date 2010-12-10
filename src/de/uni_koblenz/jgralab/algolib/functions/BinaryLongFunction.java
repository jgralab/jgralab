package de.uni_koblenz.jgralab.algolib.functions;

public interface BinaryLongFunction<DOMAIN1, DOMAIN2> {
	
	public long get(DOMAIN1 parameter1, DOMAIN2 parameter2);

	public void set(DOMAIN1 parameter1, DOMAIN2 parameter2, long value);

	public boolean isDefined(DOMAIN1 parameter1, DOMAIN2 parameter2);
}
