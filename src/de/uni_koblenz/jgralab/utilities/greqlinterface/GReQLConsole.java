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

import de.uni_koblenz.jgralab.Graph;
import de.uni_koblenz.jgralab.GraphIO;
import de.uni_koblenz.jgralab.GraphIOException;
import de.uni_koblenz.jgralab.JGraLab;
import de.uni_koblenz.jgralab.WorkInProgress;
import de.uni_koblenz.jgralab.greql2.evaluator.GreqlEvaluator;
import de.uni_koblenz.jgralab.greql2.exception.EvaluateException;
import de.uni_koblenz.jgralab.greql2.exception.OptimizerException;
import de.uni_koblenz.jgralab.greql2.jvalue.JValue;
import de.uni_koblenz.jgralab.greql2.jvalue.JValueHTMLOutputVisitor;
import de.uni_koblenz.jgralab.impl.ProgressFunctionImpl;
import de.uni_koblenz.jgralab.schema.Schema;
import de.uni_koblenz.jgralab.utilities.common.OptionHandler;

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
	public GReQLConsole(String filename) {
		try {
			Schema schema = GraphIO.loadSchemaFromFile(filename);
			schema.compile();
			graph = GraphIO.loadGraphFromFile(filename,
					new ProgressFunctionImpl());
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
		BufferedReader reader = new BufferedReader(new FileReader(queryFile));
		String line = null;
		StringBuilder builder = new StringBuilder();
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
		if (builder.length() != 0) {
			String query = builder.toString();
			builder = new StringBuilder();
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
						boundVariables, new ProgressFunctionImpl());
				// System.out.println("Query parsing took " +
				// eval.getParseTime() + " Milliseconds");
				// System.out.println("Optimization took " +
				// eval.getOptimizationTime() + " Milliseconds");
				// System.out.println("Whole evaluation took " +
				// eval.getOverallEvaluationTime() + " Milliseconds");
				eval.startEvaluation();
				result = eval.getEvaluationResult();
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

		JGraLab.setLogLevel(Level.SEVERE);
		GReQLConsole example = new GReQLConsole(inputFile);
		JValue result = example.performQuery(new File(queryFile));

		System.out.println("Result: " + result);

		if (comLine.hasOption("o")) {// args.length == 3) {
			example.resultToHTML(result, comLine.getOptionValue("o"));// args[2]);
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

		return oh.parse(args);
	}

}
