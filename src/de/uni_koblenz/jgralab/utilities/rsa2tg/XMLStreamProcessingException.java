package de.uni_koblenz.jgralab.utilities.rsa2tg;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import de.uni_koblenz.jgralab.utilities.common.UtilityMethods;

public class XMLStreamProcessingException extends XMLStreamException {

	private static final long serialVersionUID = 5406370400965287002L;

	public XMLStreamProcessingException(XMLStreamReader parser, String file,
			String message) {
		this(file, parser.getLocation().getLineNumber(), message);
	}

	public XMLStreamProcessingException(String file, String message) {
		super(UtilityMethods.generateUnexpectedErrorMessage(file, -1, message));
	}

	public XMLStreamProcessingException(String file, int lineNumber,
			String message) {
		super(UtilityMethods.generateUnexpectedErrorMessage(file, lineNumber,
				message));
	}
}
