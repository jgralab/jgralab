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
package de.uni_koblenz.ist.utilities.license_header;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileFilter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;

import de.uni_koblenz.ist.utilities.option_handler.OptionHandler;

public class LicenseHeader {

	private static final String XML_ENCODING_PREFIX = "<?xml";
	private static final String INDENT = "    ";
	private static final String JAVA_FIRST_LINE = "/*";
	private static final String JAVA_PREFIX = " * ";
	private static final String JAVA_EMPTY_LINE = " *";
	private static final String JAVA_LAST_LINE = " */";

	private static final String XML_START = "<!-- ";
	private static final String XML_END = " -->";

	private static final String TG_PREFIX = "// ";
	private static final String TG_EMPTY_LINE = "//";

	private static enum ParseState {
		/**
		 * Denotes the state when the parser is before a header (or just left a
		 * header)
		 */
		BEFORE_HEADER,

		/**
		 * Denotes the state when the parser is inside an old header (skipping
		 * it)
		 */
		IN_HEADER,

		/**
		 * Denotes the state when the parser passed through all headers (when
		 * the package statement, an import statement or the class statement is
		 * hit)
		 */
		AFTER_HEADERS
	}

	public static void main(String[] args) {
		CommandLine cl = processCommandLineOptions(args);
		assert cl.hasOption('i');
		assert cl.hasOption('l');
		LicenseHeader lh = new LicenseHeader(cl.getOptionValue('i'),
				cl.getOptionValue('l'), cl.hasOption('r'), cl.hasOption('V'));
		try {
			lh.process();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private static CommandLine processCommandLineOptions(String[] args) {
		OptionHandler oh = new OptionHandler("java "
				+ LicenseHeader.class.getName(), "1.0");
		Option input = new Option("i", "input", true,
				"(required): The file or directory to process.");
		// input.setArgs(1);
		input.setRequired(true);
		input.setArgName("fileOrDirectory");
		oh.addOption(input);

		Option licenceHeader = new Option(
				"l",
				"licence",
				true,
				"(required): The file containing the licence header in the correct format. This file should be in plain text without any language specific syntax for comments.");
		// licenceHeader.setArgs(1);
		licenceHeader.setRequired(true);
		licenceHeader.setArgName("licenseFile");
		oh.addOption(licenceHeader);

		// TODO add this feature
		// Option fileType = new Option(
		// "x",
		// "--xml",
		// false,
		// "(optional): This flag tells the program to operate on xml files instead of java files (experimental).");
		// fileType.setRequired(false);
		// oh.addOption(fileType);

		Option recursive = new Option(
				"r",
				"recursive",
				false,
				"(optional): This flag tells the program to process the given directory fully recursively. If only a file is given, this option is ignored. If this flag is not set, only the given directory is processed, without subdirectories.");
		recursive.setRequired(false);
		oh.addOption(recursive);

		Option verbose = new Option(
				"V",
				"verbose",
				false,
				"(optional): This flag tells the program to be more verbose while processing the files. If it is not set, only a summary will be given in the end.");
		verbose.setRequired(false);
		oh.addOption(verbose);

		return oh.parse(args);
	}

	private File input;
	private File licence;
	private boolean fullyRecursive;
	private boolean verbose;
	private int newlyAdded;
	private int replaced;

	private List<String> javaHeaderLines;
	private List<String> xmlHeaderLines;
	private List<String> tgHeaderLines;

	public LicenseHeader(String input, String licence, boolean fullyRecursive,
			boolean verbose) {
		super();
		this.input = new File(input);
		this.licence = new File(licence);
		this.fullyRecursive = fullyRecursive;
		this.verbose = verbose;
		newlyAdded = 0;
		replaced = 0;
	}

	private void printIndent(int level) {
		for (int i = 0; i < level; i++) {
			System.out.print(INDENT);
		}
	}

	public void process() throws IOException {
		if (!input.exists()) {
			throw new FileNotFoundException("The given input file/directory \""
					+ input.getAbsolutePath() + "\" does not exist.");
		}
		if (!licence.exists()) {
			throw new FileNotFoundException("The given licence file \""
					+ licence.getAbsolutePath() + "\" does not exist.");
		}
		if (licence.isDirectory()) {
			throw new IllegalArgumentException("The given licence file \""
					+ licence.getAbsolutePath() + "\" is a directory");
		}
		if (input.isDirectory()) {
			System.out.println("Adding license headers to directory "
					+ input.getAbsolutePath()
					+ (fullyRecursive ? " and all subdirectories."
							: " ignoring subdirectories."));
			processDirectory(input, 0);
		} else {
			System.out.println("Adding license header to file "
					+ input.getAbsolutePath());
			processJavaFile(input, 0, JAVA_FIRST_LINE, JAVA_PREFIX,
					JAVA_LAST_LINE);
		}
		int processed = replaced + newlyAdded;
		System.out.println();
		System.out.println("Summary:");
		System.out.println("Processed " + processed + " files.");
		System.out.println(newlyAdded + "/" + processed
				+ " files didn't have a license header.");
		System.out.println(replaced + "/" + processed
				+ " files' headers were replaced by the new one.");
		System.out.println();
	}

	private void processDirectory(final File toProcess, int level)
			throws IOException {

		File[] directories = toProcess.listFiles(new FileFilter() {

			@Override
			public boolean accept(File pathname) {
				return pathname.isDirectory()
						&& !pathname.getName().equals(".svn");
			}
		});

		File[] javaFilesToProcess = toProcess.listFiles(new FilenameFilter() {

			@Override
			public boolean accept(File dir, String name) {
				return dir.getAbsolutePath()
						.equals(toProcess.getAbsolutePath())
						&& name.toLowerCase().endsWith(".java");
			}
		});

		File[] xmlFilesToProcess = toProcess.listFiles(new FilenameFilter() {
			@Override
			public boolean accept(File dir, String name) {
				String lowerCaseName = name.toLowerCase();
				return dir.getAbsolutePath()
						.equals(toProcess.getAbsolutePath())
						&& (lowerCaseName.endsWith(".xml") || lowerCaseName
								.endsWith(".xmi"));
			}
		});

		File[] tgFilesToProcess = toProcess.listFiles(new FilenameFilter() {
			@Override
			public boolean accept(File dir, String name) {
				return dir.getAbsolutePath()
						.equals(toProcess.getAbsolutePath())
						&& name.toLowerCase().endsWith(".tg");
			}
		});

		if (fullyRecursive) {
			for (File currentSubdirectory : directories) {
				if (verbose) {
					printIndent(level);
					System.out.println("Entering directory "
							+ currentSubdirectory.getName());
				}
				processDirectory(currentSubdirectory, level + 1);
				if (verbose) {
					printIndent(level);
					System.out.println("Leaving directory "
							+ currentSubdirectory.getName());
				}
			}
		}

		for (File currentJavaFile : javaFilesToProcess) {
			processJavaFile(currentJavaFile, level, JAVA_FIRST_LINE,
					JAVA_PREFIX, JAVA_LAST_LINE);
		}

		for (File currentXMLFile : xmlFilesToProcess) {
			processXMLFile(currentXMLFile, level, 72);
		}

		for (File currentTGFile : tgFilesToProcess) {
			processTGFile(currentTGFile, level);
		}
	}

	private void processXMLFile(File toProcess, int level, int length)
			throws FileNotFoundException, IOException {
		if (verbose) {
			printIndent(level);
			System.out.println("Processing file " + toProcess.getName());
		}

		if (xmlHeaderLines == null) {
			cacheXMLHeader(length);
		}

		List<String> outputLines = new LinkedList<String>();

		// now open the current input file
		BufferedReader reader = new BufferedReader(new FileReader(toProcess));
		try {
			ParseState state = ParseState.BEFORE_HEADER;
			int skippedLines = 0;
			String currentLine = null;
			do {
				currentLine = reader.readLine();
				if (currentLine != null) {
					String trimmedLine = currentLine.trim();
					switch (state) {
					case BEFORE_HEADER:
						if (trimmedLine.length() == 0) {
							// ignore empty lines in the beginning
							skippedLines++;
							continue;
						}
						if (trimmedLine.startsWith(XML_START.trim())) {
							// ignore comments in the beginning
							state = trimmedLine.endsWith(XML_END.trim()) ? ParseState.BEFORE_HEADER
									: ParseState.IN_HEADER;
							skippedLines++;
							continue;
						}
						if (trimmedLine.toLowerCase().startsWith(
								XML_ENCODING_PREFIX)) {
							// preserve xml version and encoding settings
							outputLines.add(currentLine);
							continue;
						}

						// now only the root element is possible
						assert (trimmedLine.startsWith("<"));
						state = ParseState.AFTER_HEADERS;
						outputLines.add(currentLine);
						break;
					case IN_HEADER:
						skippedLines++;
						if (trimmedLine.endsWith(XML_END.trim())) {
							state = ParseState.BEFORE_HEADER;
						}
						break;
					case AFTER_HEADERS:
						// normal case, just copy
						outputLines.add(currentLine);
						break;
					}
				}
			} while (currentLine != null);
			if (skippedLines > 0) {
				if (verbose) {
					printIndent(level + 1);
					System.out.println("Skipped " + skippedLines
							+ " lines and replaced "
							+ (skippedLines == 1 ? "it" : "them")
							+ " with new header.");
				}
				replaced++;
			} else {
				if (verbose) {
					printIndent(level + 1);
					System.out.println("Added header.");
				}
				newlyAdded++;
			}
		} finally {
			reader.close();
		}

		PrintWriter writer = new PrintWriter(toProcess);
		try {
			String firstLine = outputLines.get(0);
			if (firstLine.trim().startsWith(XML_ENCODING_PREFIX)) {
				writer.println(firstLine);
				outputLines.remove(0);
			}
			for (String currentOutputLine : xmlHeaderLines) {
				writer.println(currentOutputLine);
			}
			for (String currentOutputLine : outputLines) {
				writer.println(currentOutputLine);
			}
			writer.flush();
		} finally {
			writer.close();
		}
	}

	private void processTGFile(File toProcess, int level)
			throws FileNotFoundException, IOException {
		if (verbose) {
			printIndent(level);
			System.out.println("Processing file " + toProcess.getName());
		}

		if (tgHeaderLines == null) {
			cacheTGHeader();
		}

		List<String> outputLines = new LinkedList<String>();

		// now open the current input file
		BufferedReader reader = new BufferedReader(new FileReader(toProcess));
		try {
			ParseState state = ParseState.IN_HEADER;
			int skippedLines = 0;
			String currentLine = null;

			do {
				currentLine = reader.readLine();
				if (currentLine != null) {
					String trimmedLine = currentLine.trim();
					switch (state) {
					case BEFORE_HEADER:
						// do nothing
						break;
					case IN_HEADER:
						if (trimmedLine.length() == 0) {
							// ignore empty lines in the beginning
							skippedLines++;
							continue;
						}
						if (trimmedLine.startsWith(TG_EMPTY_LINE)) {
							if (currentLine.contains("Version :")) {
								state = ParseState.AFTER_HEADERS;
								outputLines.add(currentLine);
							}
							skippedLines++;
							continue;
						}
						assert (trimmedLine.startsWith("TGraph2"));
						state = ParseState.AFTER_HEADERS;
						outputLines.add(currentLine);
						break;
					case AFTER_HEADERS:
						// normal case, just copy
						outputLines.add(currentLine);
						break;
					}
				}
			} while (currentLine != null);
			if (skippedLines > 0) {
				if (verbose) {
					printIndent(level + 1);
					System.out.println("Skipped " + skippedLines
							+ " lines and replaced "
							+ (skippedLines == 1 ? "it" : "them")
							+ " with new header.");
				}
				replaced++;
			} else {
				if (verbose) {
					printIndent(level + 1);
					System.out.println("Added header.");
				}
				newlyAdded++;
			}
		} finally {
			reader.close();
		}
		PrintWriter writer = new PrintWriter(toProcess);
		try {
			for (String currentOutputLine : tgHeaderLines) {
				writer.println(currentOutputLine);
			}
			for (String currentOutputLine : outputLines) {
				writer.println(currentOutputLine);
			}
			writer.flush();
		} finally {
			writer.close();
		}
	}

	private void processJavaFile(File toProcess, int level, String firstLine,
			String prefix, String lastLine) throws IOException {
		if (verbose) {
			printIndent(level);
			System.out.println("Processing file " + toProcess.getName());
		}

		if (javaHeaderLines == null) {
			cacheJavaHeader(firstLine, lastLine);
		}

		List<String> outputLines = new LinkedList<String>();

		// now open the current input file
		BufferedReader reader = new BufferedReader(new FileReader(toProcess));
		try {
			ParseState state = ParseState.BEFORE_HEADER;
			int skippedHeaders = 0;
			String currentLine = null;
			do {
				currentLine = reader.readLine();
				if (currentLine != null) {
					switch (state) {
					case BEFORE_HEADER:
						if (currentLine.trim().startsWith(firstLine.trim())
								&& !currentLine.trim().startsWith("/**")) {
							state = ParseState.IN_HEADER;
							skippedHeaders++;
							continue;
						}
						if (currentLine.contains("package")
								|| currentLine.contains("import")
								|| currentLine.contains("class")) {
							state = ParseState.AFTER_HEADERS;
						}
						outputLines.add(currentLine);
						break;
					case IN_HEADER:
						if (currentLine.trim().endsWith(lastLine.trim())) {
							state = ParseState.BEFORE_HEADER;
						}
						break;
					case AFTER_HEADERS:
						// normal case, just copy
						outputLines.add(currentLine);
						break;
					}
				}
			} while (currentLine != null);
			if (skippedHeaders > 0) {
				if (verbose) {
					printIndent(level + 1);
					System.out.println("Skipped " + skippedHeaders
							+ " headers and replaced "
							+ (skippedHeaders == 1 ? "it" : "them")
							+ " with new header.");
				}
				replaced++;
			} else {
				if (verbose) {
					printIndent(level + 1);
					System.out.println("Added header.");
				}
				newlyAdded++;
			}
		} finally {
			reader.close();
		}
		PrintWriter writer = new PrintWriter(toProcess);
		try {
			for (String currentOutputLine : javaHeaderLines) {
				writer.println(currentOutputLine);
			}
			for (String currentOutputLine : outputLines) {
				writer.println(currentOutputLine);
			}
			writer.flush();
		} finally {
			writer.close();
		}

	}

	private void cacheJavaHeader(String firstLine, String lastLine)
			throws FileNotFoundException, IOException {
		if (verbose) {
			System.out.println("Caching license header for Java...");
		}
		javaHeaderLines = new LinkedList<String>();

		// add header to outputLines
		BufferedReader reader = new BufferedReader(new FileReader(licence));
		try {
			javaHeaderLines.add(firstLine);
			String currentLine = null;
			do {
				currentLine = reader.readLine();
				if (currentLine != null) {
					javaHeaderLines.add(currentLine.isEmpty() ? JAVA_EMPTY_LINE
							: JAVA_PREFIX + currentLine);
				}
			} while (currentLine != null);
			javaHeaderLines.add(lastLine);
		} finally {
			reader.close();
		}
	}

	private void cacheXMLHeader(int length) throws FileNotFoundException,
			IOException {
		if (verbose) {
			System.out.println("Caching license header for XML...");
		}
		xmlHeaderLines = new LinkedList<String>();
		BufferedReader reader = new BufferedReader(new FileReader(licence));
		try {
			String currentLine = "";

			do {
				currentLine = reader.readLine();
				if (currentLine != null) {
					xmlHeaderLines
							.add(createXMLHeaderLine(length, currentLine));
				}
			} while (currentLine != null);
			xmlHeaderLines.add("");
		} finally {
			reader.close();
		}
	}

	private String createXMLHeaderLine(int length, String content) {
		StringBuilder out = new StringBuilder();
		out.append(XML_START);
		out.append(content);
		int space = length - content.length();
		for (int i = 0; i < space; i++) {
			out.append(" ");
		}
		out.append(XML_END);
		return out.toString();
	}

	private void cacheTGHeader() throws FileNotFoundException, IOException {
		if (verbose) {
			System.out.println("Caching license header for TG...");
		}
		tgHeaderLines = new LinkedList<String>();
		BufferedReader reader = new BufferedReader(new FileReader(licence));
		try {
			String currentLine = "";

			do {
				currentLine = reader.readLine();
				if (currentLine != null) {
					tgHeaderLines.add((TG_PREFIX + currentLine).trim());
				}
			} while (currentLine != null);
			tgHeaderLines.add("");
		} finally {
			reader.close();
		}
	}
}
