package de.uni_koblenz.jgralab.algolib.functions;

public interface BinaryDoubleFunction<DOMAIN1, DOMAIN2> {

	public double get(DOMAIN1 parameter1, DOMAIN2 parameter2);

	public void set(DOMAIN1 parameter1, DOMAIN2 parameter2, double value);

	public boolean isDefined(DOMAIN1 parameter1, DOMAIN2 parameter2);

}
