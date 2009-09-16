package de.uni_koblenz.jgralab.utilities.rsa2tg;

import javax.xml.stream.XMLStreamReader;

public class ProcessingException extends RuntimeException {

	private static final long serialVersionUID = 5715378979859807085L;

	public ProcessingException(XMLStreamReader parser, String file,
			String message) {
		this(file, parser.getLocation().getLineNumber(), message);
	}

	public ProcessingException(String file, String message) {
		super("Unexpected error occured in file '" + file + "'.\n" + message);
	}

	public ProcessingException(String file, int lineNumber, String message) {
		super("Unexpected error occured in file '" + file + "' at line "
				+ lineNumber + ".\n" + message);
	}
}
