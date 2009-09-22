package de.uni_koblenz.jgralab.utilities.rsa2tg;

import javax.xml.stream.XMLStreamReader;

import de.uni_koblenz.jgralab.utilities.common.UtilityMethods;

public class ProcessingException extends RuntimeException {

	private static final long serialVersionUID = 5715378979859807085L;

	public ProcessingException(XMLStreamReader parser, String file,
			String message) {
		this(file, parser.getLocation().getLineNumber(), message);
	}

	public ProcessingException(String file, String message) {
		super(UtilityMethods.generateUnexpectedErrorMessage(file, -1, message));
	}

	public ProcessingException(String file, int lineNumber, String message) {
		super(UtilityMethods.generateUnexpectedErrorMessage(file, lineNumber,
				message));
	}
}
