/*
 * JGraLab - The Java graph laboratory
 * (c) 2006-2010 Institute for Software Technology
 *               University of Koblenz-Landau, Germany
 * 
 *               ist@uni-koblenz.de
 * 
 * Please report bugs to http://serres.uni-koblenz.de/bugzilla
 * 
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */

package de.uni_koblenz.jgralab.utilities.rsa;

import javax.xml.stream.XMLStreamReader;

/**
 * 
 * @author ist@uni-koblenz.de
 */
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
