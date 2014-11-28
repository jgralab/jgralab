/*
 * JGraLab - The Java Graph Laboratory
 *
 * Copyright (C) 2006-2014 Institute for Software Technology
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
package de.uni_koblenz.jgralab.utilities.greqlserver;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.logging.Level;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;

import de.uni_koblenz.ist.utilities.option_handler.OptionHandler;
import de.uni_koblenz.jgralab.Graph;
import de.uni_koblenz.jgralab.GraphIO;
import de.uni_koblenz.jgralab.JGraLab;
import de.uni_koblenz.jgralab.exception.GraphIOException;
import de.uni_koblenz.jgralab.greql.GreqlQuery;
import de.uni_koblenz.jgralab.greql.evaluator.GreqlEnvironmentAdapter;
import de.uni_koblenz.jgralab.greql.exception.GreqlException;
import de.uni_koblenz.jgralab.greql.exception.ParsingException;
import de.uni_koblenz.jgralab.greql.exception.QuerySourceException;
import de.uni_koblenz.jgralab.greql.serialising.DefaultWriter;
import de.uni_koblenz.jgralab.greql.serialising.HTMLOutputWriter;
import de.uni_koblenz.jgralab.greql.serialising.XMLOutputWriter;
import de.uni_koblenz.jgralab.impl.ConsoleProgressFunction;
import de.uni_koblenz.jgralab.schema.Schema;
import de.uni_koblenz.jgralab.schema.codegenerator.CodeGeneratorConfiguration;

public class GReQLConsole {

	private Graph graph = null;
	private boolean verbose = false;

	/**
	 * Creates a new instance of this class, reads the graph and the schema from
	 * the file given as parameter filename
	 * 
	 * @param filename
	 *            the name of the file that contains the schema and the graph
	 * @param verbose
	 *            produce verbose output
	 */
	public GReQLConsole(String filename, boolean loadSchema, boolean verbose) {
		this.verbose = verbose;
		try {
			if (loadSchema) {
				if (verbose) {
					System.out.println("Loading schema from file");
				}
				Schema schema = GraphIO.loadSchemaFromFile(filename);
				schema.compile(CodeGeneratorConfiguration.MINIMAL);
			}
			if (filename != null) {
				graph = GraphIO.loadGraphFromFile(filename,
						(verbose ? new ConsoleProgressFunction("Loading")
								: null));
			}
		} catch (GraphIOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Loads all queries stored in a given file and returns a list of query
	 * strings. Multiple queries have to be separated by lines containing only
	 * "//--".
	 * 
	 * @param queryFile
	 *            the file with queries to load
	 * @return a list containing all queries stored in the queryFile
	 * @throws IOException
	 *             if the file is not found
	 */
	private List<String> loadQueries(File queryFile) throws IOException {
		List<String> queries = new ArrayList<>();
		StringBuilder builder = new StringBuilder();
		BufferedReader reader = null;
		try {
			reader = new BufferedReader(new FileReader(queryFile));
			String line = null;
			while ((line = reader.readLine()) != null) {
				if (line.startsWith("//--")) {
					// End of query, and this line can be ignored.
					queries.add(builder.toString());
					builder = new StringBuilder();
				} else {
					builder.append(line + " \n");
				}
			}
		} finally {
			if (reader != null) {
				reader.close();
			}
		}
		if (builder.length() != 0) {
			String query = builder.toString();
			queries.add(query);
		}
		return queries;
	}

	/**
	 * Performs a query given as a string representation in GReQL on the loaded
	 * graph
	 * 
	 * @param queryString
	 *            the GReQL representation of the query to perform
	 * @return the calculated query result
	 */
	private Object performQuery(File queryFile) throws GreqlException {
		Object result = null;

		try {
			for (String query : loadQueries(queryFile)) {
				if (verbose) {
					System.out.println("Evaluating query: ");
					System.out.println(query);
				}
				result = GreqlQuery.createQuery(query).evaluate(graph,
						new GreqlEnvironmentAdapter(),
						(verbose ? new ConsoleProgressFunction() : null));
				if (verbose && (result instanceof Collection)) {
					System.out.println("Result size is: "
							+ ((Collection<?>) result).size());
				}
			}
		} catch (IOException e) {
			throw new RuntimeException(e);
		}

		return result;
	}

	/**
	 * Prints the JValue given as first parameter to the file with the name
	 * given as second parameter
	 * 
	 * @throws IOException
	 */
	private void saveResultToFile(Object result, String outputFile,
			String outputType) throws Exception {
		if ((outputType == null) || outputType.isEmpty()) {
			outputType = "html";
		}
		if (outputType.equalsIgnoreCase("html")) {
			DefaultWriter w = new HTMLOutputWriter(graph);
			w.writeValue(result, new File(outputFile));
		} else if (outputType.equalsIgnoreCase("xml")) {
			DefaultWriter w = new XMLOutputWriter(graph);
			w.writeValue(result, new File(outputFile));
		} else if (outputType.equalsIgnoreCase("txt")) {
			FileWriter w = new FileWriter(new File(outputFile));
			w.append(result.toString());
			w.flush();
			w.close();
		} else {
			throw new RuntimeException("Unsupported output type " + outputFile
					+ "!");
		}
	}

	/**
	 * Performs some queries, extend this method to perform more queries
	 * 
	 * @param args
	 */
	public static void main(String[] args) {
		try {
			CommandLine comLine = processCommandLineOptions(args);
			assert comLine != null;

			String queryFile = comLine.getOptionValue("q");
			String graphFile = comLine.getOptionValue("g");
			boolean loadSchema = comLine.hasOption("s");

			JGraLab.setLogLevel(Level.SEVERE);
			GReQLConsole console = new GReQLConsole(graphFile, loadSchema,
					comLine.hasOption('v'));
			Object result = console.performQuery(new File(queryFile));

			if (comLine.hasOption("o")) {
				console.saveResultToFile(result, comLine.getOptionValue("o"),
						comLine.getOptionValue('t'));
			} else {
				System.out.println("Result: " + result);
			}
		} catch (Exception e) {
			if (e instanceof ParsingException) {
				ParsingException ex = (ParsingException) e;
				System.err.println("##exception=ParsingException");
				System.err.println("##offset=" + ex.getOffset());
				System.err.println("##length=" + ex.getLength());
			} else if (e instanceof QuerySourceException) {
				QuerySourceException ex = (QuerySourceException) e;
				System.err.println("##exception="
						+ ex.getClass().getSimpleName());
				System.err.println("##offset=" + ex.getOffset());
				System.err.println("##length=" + ex.getLength());
			}
			e.printStackTrace();
			System.exit(1); // exit with non-zero exit code
		}
	}

	private static CommandLine processCommandLineOptions(String[] args) {
		String toolString = "java " + GReQLConsole.class.getName();
		String versionString = JGraLab.getInfo(false);
		OptionHandler oh = new OptionHandler(toolString, versionString);

		Option queryfile = new Option("q", "queryfile", true,
				"(required): queryfile which should be executed. May contain "
						+ "many queries separated by //-- on a line");
		queryfile.setRequired(true);
		queryfile.setArgName("file");
		oh.addOption(queryfile);

		Option inputFile = new Option("g", "graph", true,
				"(optional): the tg-file of the graph");
		inputFile.setRequired(false);
		inputFile.setArgName("file");
		oh.addOption(inputFile);

		Option output = new Option(
				"o",
				"output",
				true,
				"(optional): result file to be generated (see option --output-type, defaults to html)");
		output.setArgName("file");
		oh.addOption(output);

		Option outputType = new Option("t", "output-type", true,
				"(optional): if -o is given, the output type to use (txt, html, or xml)");
		outputType.setArgName("type");
		oh.addOption(outputType);

		Option loadschema = new Option("s", "loadschema", false,
				"(optional): Loads also the schema from the file");
		oh.addOption(loadschema);

		Option verbose = new Option("v", "verbose", false,
				"(optional): Produce verbose output");
		oh.addOption(verbose);

		return oh.parse(args);
	}

}
