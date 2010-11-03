/*
 * JGraLab - The Java Graph Laboratory
 * 
 * Copyright (C) 2006-2010 Institute for Software Technology
 *                         University of Koblenz-Landau, Germany
 *                         ist@uni-koblenz.de
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
package de.uni_koblenz.jgralab.utilities.greqlinterface;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;

import de.uni_koblenz.ist.utilities.option_handler.OptionHandler;
import de.uni_koblenz.jgralab.Graph;
import de.uni_koblenz.jgralab.GraphIO;
import de.uni_koblenz.jgralab.GraphIOException;
import de.uni_koblenz.jgralab.JGraLab;
import de.uni_koblenz.jgralab.WorkInProgress;
import de.uni_koblenz.jgralab.codegenerator.CodeGeneratorConfiguration;
import de.uni_koblenz.jgralab.greql2.evaluator.GreqlEvaluator;
import de.uni_koblenz.jgralab.greql2.exception.EvaluateException;
import de.uni_koblenz.jgralab.greql2.exception.OptimizerException;
import de.uni_koblenz.jgralab.greql2.jvalue.JValue;
import de.uni_koblenz.jgralab.greql2.jvalue.JValueHTMLOutputVisitor;
import de.uni_koblenz.jgralab.impl.ConsoleProgressFunction;
import de.uni_koblenz.jgralab.schema.Schema;

@WorkInProgress(responsibleDevelopers = "dbildh")
public class GReQLConsole {

	Graph graph = null;

	/**
	 * Creates a new instance of this class, reads the graph and the schema from
	 * the file given as parameter filename
	 * 
	 * @param filename
	 *            the name of the file that contains the schema and the graph
	 */
	public GReQLConsole(String filename, boolean loadSchema) {
		try {
			if (loadSchema) {
				System.out.println("Loading schema from file");
				Schema schema = GraphIO.loadSchemaFromFile(filename);
				schema.compile(CodeGeneratorConfiguration.WITHOUT_TRANSACTIONS);
			}
			graph = GraphIO.loadGraphFromFileWithStandardSupport(filename,
					new ConsoleProgressFunction());
		} catch (GraphIOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Loads all queries stored in a given file and returns a list of query
	 * strings
	 * 
	 * @param queryFile
	 *            the file with queries to load
	 * @return a list containing all queries stored in the queryFile
	 * @throws IOException
	 *             if the file is not found
	 */
	private List<String> loadQueries(File queryFile) throws IOException {
		List<String> queries = new ArrayList<String>();
		StringBuilder builder = new StringBuilder();
		BufferedReader reader = null;
		try {
			reader = new BufferedReader(new FileReader(queryFile));
			String line = null;
			while ((line = reader.readLine()) != null) {
				String trimmedLine = line.trim();
				if (trimmedLine.startsWith("//")) {
					continue;
				}
				if (trimmedLine.length() == 0) {
					// found end of a query
					if (builder.length() != 0) {
						String query = builder.toString();
						builder = new StringBuilder();
						queries.add(query);
					}
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
	private JValue performQuery(File queryFile) {
		JValue result = null;
		try {
			Map<String, JValue> boundVariables = new HashMap<String, JValue>();
			for (String query : loadQueries(queryFile)) {
				System.out.println("Evaluating query: ");
				System.out.println(query);
				GreqlEvaluator eval = new GreqlEvaluator(query, graph,
						boundVariables, new ConsoleProgressFunction());
				// eval.setOptimize(false);
				eval.setEvaluationLogger(null);
				eval.startEvaluation();

				result = eval.getEvaluationResult();
				System.out.println("Query parsing took " + eval.getParseTime()
						+ " Milliseconds");
				System.out.println("Optimization took "
						+ eval.getOptimizationTime() + " Milliseconds");
				System.out.println("Whole evaluation took "
						+ eval.getOverallEvaluationTime() + " Milliseconds");
				if (result.isCollection()) {
					System.out.println("Result size is: "
							+ result.toCollection().size());
				}
			}
		} catch (EvaluateException e) {
			e.printStackTrace();
		} catch (OptimizerException e) {
			e.printStackTrace();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return result;
	}

	/**
	 * Prints the JValue given as first parameter to the file with the name
	 * given as second parameter
	 */
	private void resultToHTML(JValue result, String outputFile) {
		new JValueHTMLOutputVisitor(result, outputFile, graph);
	}

	/**
	 * Performs some queries, extend this method to perform more queries
	 * 
	 * @param args
	 */
	public static void main(String[] args) {
		CommandLine comLine = processCommandLineOptions(args);
		assert comLine != null;

		// String queryFile = null;
		// String inputFile = null;

		// if (args.length < 2) {
		// System.out.println("GReQL2 QUERY_FILE INPUT_FILE [HTML_FILE]");
		// System.exit(-1);
		// }

		String queryFile = comLine.getOptionValue("q");// args[0];
		String inputFile = comLine.getOptionValue("g");// args[1];
		boolean loadSchema = comLine.hasOption("s"); // args[1];

		JGraLab.setLogLevel(Level.SEVERE);
		GReQLConsole example = new GReQLConsole(inputFile, loadSchema);
		JValue result = example.performQuery(new File(queryFile));

		if (comLine.hasOption("o")) {// args.length == 3) {
			example.resultToHTML(result, comLine.getOptionValue("o"));// args[2]);
		} else {
			// System.out.println("Result: " + result);
		}
	}

	private static CommandLine processCommandLineOptions(String[] args) {
		String toolString = "java " + GReQLConsole.class.getName();
		String versionString = JGraLab.getInfo(false);
		OptionHandler oh = new OptionHandler(toolString, versionString);

		Option queryfile = new Option("q", "queryfile", true,
				"(required): queryfile which should be executed");
		queryfile.setRequired(true);
		queryfile.setArgName("file");
		oh.addOption(queryfile);

		Option inputFile = new Option("g", "graph", true,
				"(required): the tg-file of the graph");
		inputFile.setRequired(true);
		inputFile.setArgName("file");
		oh.addOption(inputFile);

		Option output = new Option("o", "output", true,
				"(optional): HTML-file to be generated");
		output.setArgName("file");
		oh.addOption(output);

		Option loadschema = new Option("s", "loadschema", false,
				"(optional): Loads also the schema from the file");
		oh.addOption(loadschema);

		return oh.parse(args);
	}

}
