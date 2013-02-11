/*
 /*
 * JGraLab - The Java Graph Laboratory
 *
 * Copyright (C) 2006-2013 Institute for Software Technology
 *                         University of Koblenz-Landau, Germany
 *                         ist@uni-koblenz.de
 *
 * For bug reports, documentation and further information, visit
 *
 *                         https://github.com/jgralab/jgralab
 *
 * This program is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as published by the
 * Free Software Foundation; either version 3 of the License, or (at your
 * option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General
 * Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, see <http://www.gnu.org/licenses>.
 *
 * Additional permission under GNU GPL version 3 section 7
 *
 * If you modify this Program, or any covered work, by linking or combining
 * it with Eclipse (or a modified version of that program or an Eclipse
 * plugin), containing parts covered by the terms of the Eclipse Public
 * License (EPL), the licensors of this Program grant you additional
 * permission to convey the resulting work.  Corresponding Source for a
 * non-source form of such a combination shall include the source code for
 * the parts of JGraLab used as well as that of the covered work.
 */
package de.uni_koblenz.ist.utilities.csvreader;

// TODO do it without String Tokenizer but with Regex (not String.split, but precompiled Pattern-Object)

import java.io.IOException;
import java.io.LineNumberReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.StringTokenizer;
import java.util.regex.Pattern;

/**
 * CsvReader is used to so called "comma separated values" text files line by
 * line via a LineNumberReader.
 * 
 * Each line is split into fields. Field separators may be specified, or the
 * default ";" can be used. CsvReader supports additional quoting of field
 * separators. By definition, the quote character may not be in the separators,
 * and double a quote character in the input string is regarded as the quote
 * character itself.
 * 
 * Empty fields are represented as empty (not null) Strings.
 * 
 * Since the separator is between fields, even an empty line contains exactly
 * one field. A separator at the end of a line indicates an additional empty
 * field.
 * 
 * The default field separator is a ";", and no quoting is enabled.
 * 
 * TODO possibly add code to check that each record contains exactly as many
 * fields as the number of field names
 * 
 * @author riediger
 */
public class CsvReader {
	/**
	 * Constant for the constructor to indicate that no headline is contained in
	 * the CSV file.
	 */
	public static final int WITHOUT_FIELDNAMES = 0;
	/**
	 * Constant for the constructor to indicate that the first line of the CSV
	 * file contains a headline with field names.
	 */
	public static final int WITH_FIELDNAMES = 1;

	private static final Pattern commentLine = Pattern.compile("^\\s*#.*$");

	/**
	 * input source
	 */
	private LineNumberReader reader;

	/**
	 * Vector of Strings taken from first line of input
	 */
	private ArrayList<String> fieldNames;

	/**
	 * contains data field of most recent readRecord call
	 */
	private ArrayList<String> currentRecord;

	/**
	 * input field separators
	 */
	private String separators;

	/**
	 * field separator quote character
	 */
	private String quote;

	/**
	 * string delimiter character
	 */
	private char stringDelimiter = '"';

	/**
	 * indicates if qouting is wanted
	 */
	private boolean quoting;

	/**
	 * Creates a CsvReader and reads the first line of input as field names.
	 * Field separator is ";", no quote character.
	 * 
	 * @param in
	 *            a Reader used for line-wise reading of input
	 * @param withFieldNames
	 *            either WITH_FIELDNAMES or WITHOUT_FIELDNAMES
	 */
	public CsvReader(Reader in, int withFieldNames) throws IOException {
		this(in, ";", "", withFieldNames);
	}

	/**
	 * Creates a CsvReader and reads the first line of input as field names.
	 * Field separator is ";", no quote character.
	 * 
	 * @param in
	 *            a Reader used for line-wise reading of input
	 */
	public CsvReader(Reader in) throws IOException {
		this(in, ";", "", CsvReader.WITHOUT_FIELDNAMES);
	}

	/**
	 * Creates a CsvReader.
	 * 
	 * @param in
	 *            a Reader used for line-wise reading of input
	 * @param separator
	 *            separators string for StringTokenizer
	 * @param quote
	 *            specifies a quote character to escape separators (must be
	 *            empty or exactly one character)
	 * @param withFieldNames
	 *            either WITH_FIELDNAMES or WITHOUT_FIELDNAMES
	 */
	public CsvReader(Reader in, String separators, String quote)
			throws IOException {
		this(in, separators, quote, CsvReader.WITHOUT_FIELDNAMES);
	}

	/**
	 * Creates a CsvReader.
	 * 
	 * @param in
	 *            a Reader used for line-wise reading of input
	 * @param separator
	 *            separators string for StringTokenizer
	 * @param quote
	 *            specifies a quote character to escape separators (must be
	 *            empty or exactly one character)
	 * @param withFieldNames
	 *            either WITH_FIELDNAMES or WITHOUT_FIELDNAMES
	 */
	public CsvReader(Reader in, String separators, String quote,
			int withFieldNames) throws IOException {
		// logger = new PrintWriter(new FileWriter("logfile.csv"));
		reader = new LineNumberReader(in);
		this.separators = separators;
		this.quote = quote;
		currentRecord = new ArrayList<String>();
		quoting = !quote.equals(""); // switch on quoting if requested
		if (withFieldNames == WITH_FIELDNAMES) {
			readFieldNames();
		}
	}

	/**
	 * @return the line number of the currently read record
	 */
	public int getLineNumber() {
		return reader.getLineNumber();
	}

	/**
	 * Reads one line of input and assigns the resulting fields to the
	 * fieldNames member.
	 * 
	 * @throws IOException
	 *             forwarded from readRecord
	 */
	public void readFieldNames() throws IOException {
		if (readRecord()) {
			fieldNames = new ArrayList<String>(currentRecord);
		}
	}

	/**
	 * Reads and splits one line of input. Input is split into fields by help of
	 * a StringTokenizer. The fields are stored in currentRecord.
	 * 
	 * @return true if a line could be read, false at EOF
	 * @throws IOException
	 *             if the readLine method of the underlying LineNumberReader
	 *             throws one
	 */
	public boolean readRecord() throws IOException {
		while (true) {
			String line = reader.readLine(); // read one line or throw
												// IOException
			// System.out.println(System.currentTimeMillis() + " " + line);
			if (line == null) {
				return false;
			}

			if (commentLine.matcher(line).matches()) {
				// if comment, try again
				continue;
			}
			// not EOF, so create a new result record
			currentRecord.clear();

			// take line apart into tokens
			// separators and quote character are also returned as tokens
			StringTokenizer st = new StringTokenizer(line, separators + quote,
					true);

			StringBuilder field = new StringBuilder(); // temporary for one
														// input
														// field

			while (st.hasMoreTokens()) {
				String s = st.nextToken();

				// each token is one of three kinds:
				// 1) the quote character, then the next token is handled as 3)
				// if
				// one exists
				// 2) a separator, then the field is added to the output record
				// 3) an ordinary token, then it is appended to the current
				// field
				if (quoting && s.equals(quote)) {
					if (st.hasMoreTokens()) {
						s = st.nextToken();
						field.append(s);
					}
				} else if (separators.indexOf(s) >= 0) {
					currentRecord.add(removeStringDelimiters(field));
					field = new StringBuilder();
				} else {
					field.append(s);
				}
			}

			// add last field (could be the only one if the line contains no
			// seperators nor quotes)
			currentRecord.add(removeStringDelimiters(field));
			return true;
		}
	}

	public String removeStringDelimiters(StringBuilder field) {
		String val = field.toString().trim();
		if (val.length() > 0) {
			if (val.charAt(0) == stringDelimiter) {
				val = val.substring(1);
			}
			if (val.length() > 0) {
				if (val.charAt(val.length() - 1) == stringDelimiter) {
					val = val.substring(0, val.length() - 1);
				}
			}
		}
		return val;
	}

	/**
	 * Return the field content in the current record. The field is given by its
	 * name.
	 * 
	 * @param name
	 * @return the content (as a String).
	 * @see #getFieldAt(int)
	 */
	public String getFieldByName(String name) throws CsvReaderException {
		if (fieldNames == null) {
			throw new CsvReaderException(
					"field name lookup disabled because field names were not read");
		}
		// TODO somewhat inefficient because field names are searched each time;
		// better store a map?
		int index = fieldNames.indexOf(name);
		if (index < 0) {
			throw new NoSuchElementException("unknown field name: \"" + name
					+ "\"");
		}
		return currentRecord.get(index);
	}

	/**
	 * Return the field content in the current record. The field is given by its
	 * number. The first field has number zero.
	 * 
	 * @param index
	 * @return the content (as a String)
	 * @see #getFieldByName(String)
	 */
	public String getFieldAt(int index) {
		return currentRecord.get(index);
	}

	/**
	 * Get the number of fields in the current record.
	 * 
	 * @return the number of fields in the current record
	 */
	public int getFieldCount() {
		return currentRecord.size();
	}

	/**
	 * Returns the current record.
	 * 
	 * @return the current record
	 */
	public List<String> getCurrentRecord() {
		return new ArrayList<String>(currentRecord);
	}

	/**
	 * @return a Vector of Strings with field names, possibly null if field
	 *         names were not read
	 */
	public List<String> getFieldNames() {
		return fieldNames;
	}

	/**
	 * Closes the stream of the Reader.
	 * 
	 * @throws java.io.IOException
	 */
	public void close() throws IOException {
		reader.close();
	}

}