package de.uni_koblenz.jgralab.utilities.rsa2tg;

import javax.xml.stream.XMLStreamReader;

public class ProcessingException extends RuntimeException {

	private static final long serialVersionUID = 5715378979859807085L;

	public ProcessingException(XMLStreamReader parser, String file,
			String message) {
		this(file, parser.getLocation().getLineNumber(), message);
	}

	public ProcessingException(String file, String message) {
		this(file, 0, message);
	}

	public ProcessingException(String file, int lineNumber, String message) {
		super(generateErrorMessage(file, lineNumber, message));
	}

	/**
	 * Generates a UnexpectedError message, which includes a filename, a line
	 * number and a message. Line number and message are optional. If you don't
	 * want to declare a line number use a negative number. For no message use
	 * 'null'.
	 * 
	 * @param file
	 *            Filename of the current processed file. A null reference will
	 *            throw a NullPointerException.
	 * @param lineNumber
	 *            Line number, at which processing stopped. A value less then
	 *            zero results an error message without mentioning the line
	 *            number.
	 * @param message
	 *            Message, which should be added at the end. A null reference
	 *            will be handled like an empty message.
	 * @return UnexpectedError message
	 */
	protected static String generateErrorMessage(String file, int lineNumber,
			String message) {

		StringBuilder sb = new StringBuilder();

		sb.append("Error in file '");
		sb.append(file);
		sb.append("'");

		if (lineNumber > 0) {
			sb.append(" at line ");
			sb.append(lineNumber);
		}
		if (message != null) {
			sb.append(": ");
			sb.append(message);
		} else {
			sb.append(".");
		}
		return sb.toString();
	}
}
