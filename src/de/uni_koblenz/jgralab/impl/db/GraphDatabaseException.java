package de.uni_koblenz.jgralab.impl.db;

import de.uni_koblenz.jgralab.GraphIOException;

public class GraphDatabaseException extends GraphIOException {
	
	private static final long serialVersionUID = 1L;

	public GraphDatabaseException(String string, Exception exception) {
		super(string, exception);
	}

	public GraphDatabaseException(String string) {
		super(string);
	}

	

}
