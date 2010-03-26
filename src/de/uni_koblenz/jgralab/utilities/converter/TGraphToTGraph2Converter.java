package de.uni_koblenz.jgralab.utilities.converter;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;

import de.uni_koblenz.ist.utilities.option_handler.OptionHandler;
import de.uni_koblenz.jgralab.GraphIO;
import de.uni_koblenz.jgralab.JGraLab;

/**
 * This class provides a tool for converting graphs and schemas from the old tg
 * format (before the schema renovation) to the new version 2 of the tg format.
 * 
 * The relevant differences for the conversion are:
 * <ul>
 * <li>The file starts with the String "Version n", where n is the current
 * version of the tg format (currently n=2).</li>
 * <li>Aggregation and composition classes are now declared as edge classes with
 * additional information in the declaration for deciding if the declared class
 * is an edge class, an aggregation class or a composition class.</li>
 * <li>The graph id and the graph version in the element <code>Graph</code> are
 * now separated.</li>
 * </ul>
 * Everything else is copied to the new tg file.
 * 
 * The tool reads the file line by line. It also works, if a statement (ending
 * with ";") is defined using multiple lines.
 * 
 * @author ist@uni-koblenz.de
 * 
 */
public class TGraphToTGraph2Converter {

	private static int VERSION = GraphIO.TGFILE_VERSION;
	private static Pattern AGGREGATION_CLASS_DEFINITION = Pattern
			.compile("(\\s*abstract\\s*)?(AggregationClass)(\\s*.*?)(\\s*:\\s*.*?)?(\\s+from\\s+.*?)(\\s+role\\s+.*?)?(\\s+to\\s+.*?)(\\s+role\\s+.*?)?(\\s+aggregate\\s+)((?:to)|(?:from))(\\s*.*)");
	private static Pattern COMPOSITION_CLASS_DEFINITION = Pattern
			.compile("(\\s*abstract\\s*)?(\\s*CompositionClass)(\\s*.*?)(\\s*:\\s*.*?)?(\\s+from\\s+.*?)(\\s+role\\s+.*?)?(\\s+to\\s+.*?)(\\s+role\\s+.*?)?(\\s+aggregate\\s+)((?:to)|(?:from))(\\s*.*)");
	private static Pattern GRAPH_LINE = Pattern
			.compile("(\\s*Graph\\s*)\"(.*)(?:_)(.*)\"(.*)");
	private static Pattern GRAPH_LINE_NO_VERSION = Pattern
			.compile("(\\s*Graph\\s*)(\".*\")(.*)");
	private static Pattern OLD_ROLE_NAME = Pattern.compile("(\\s*)role '(.*)");

	/**
	 * The method for converting a tg file. The parameters of this methods are
	 * streams for better flexibility.
	 * 
	 * @param out
	 *            the stream to write the content of the new tg file to
	 * @param in
	 *            the stream to read the old tg file from
	 * @throws IOException
	 *             this should not happen
	 */
	public void convertTGStream(OutputStream out, InputStream in)
			throws IOException {
		PrintWriter output = new PrintWriter(new BufferedWriter(
				new OutputStreamWriter(out)), true);
		BufferedReader input = new BufferedReader(new InputStreamReader(in));
		output.print("TGraph ");
		output.print(VERSION);
		output.println(";");
		String currentLine = "";
		boolean graphReached = false;
		while (currentLine != null) {
			currentLine = input.readLine();
			if (currentLine != null) {
				if (graphReached) {
					output.println(currentLine);
					continue;
				}
				if (currentLine.trim().startsWith("//")) {
					output.println(currentLine);
					continue;
				}
				if (!currentLine.trim().endsWith(";")) {
					StringBuilder newCurrentLine = new StringBuilder(
							currentLine);
					currentLine = input.readLine();
					if (currentLine != null) {
						newCurrentLine.append('\n');
						newCurrentLine.append(currentLine);
					}
					currentLine = newCurrentLine.toString();
				}
				output.println(processLine(currentLine));
				if (currentLine.trim().startsWith("Graph ")) {
					graphReached = true;
				}
			}
		}
		out.flush();
		out.close();
	}

	private String processLine(String currentLine) {
		Matcher matcher = AGGREGATION_CLASS_DEFINITION.matcher(currentLine);
		if (matcher.matches()) {
			StringBuilder out = new StringBuilder();
			createEdgeClassStatementForAggregationOrCompositionClass(matcher,
					out, "shared");
			return out.toString();
		}
		matcher = COMPOSITION_CLASS_DEFINITION.matcher(currentLine);
		if (matcher.matches()) {
			StringBuilder out = new StringBuilder();
			createEdgeClassStatementForAggregationOrCompositionClass(matcher,
					out, "composite");
			return out.toString();
		}
		matcher = GRAPH_LINE.matcher(currentLine);
		if (matcher.matches()) {
			StringBuilder out = new StringBuilder();
			out.append(matcher.group(1));
			out.append("\"");
			out.append(matcher.group(2));
			out.append("\" ");
			out.append(matcher.group(3));
			out.append(matcher.group(4));
			return out.toString();
		}
		matcher = GRAPH_LINE_NO_VERSION.matcher(currentLine);
		if (matcher.matches()) {
			StringBuilder out = new StringBuilder();
			out.append(matcher.group(1));
			out.append(matcher.group(2));
			out.append(" ");
			out.append(1);
			out.append(" ");
			out.append(matcher.group(3));
			return out.toString();
		}
		return currentLine;
	}

	private void createEdgeClassStatementForAggregationOrCompositionClass(
			Matcher matcher, StringBuilder out, String aggregationType) {
		// determine aggregation side
		String aggregationSide = matcher.group(10);
		assert aggregationSide.equals("to") || aggregationSide.equals("from");

		String currentGroup = matcher.group(1);
		appendOptionalPart(out, currentGroup);
		out.append("EdgeClass");
		currentGroup = matcher.group(3);
		out.append(currentGroup);
		currentGroup = matcher.group(4);
		appendOptionalPart(out, currentGroup);
		currentGroup = matcher.group(5);
		out.append(currentGroup);
		// rolename
		currentGroup = matcher.group(6);
		appendOptionalPart(out, parseRolename(currentGroup));
		if (aggregationSide.equals("to")) {
			out.append(" aggregation ");
			out.append(aggregationType);
		}
		currentGroup = matcher.group(7);
		out.append(currentGroup);
		// rolename
		currentGroup = matcher.group(8);
		appendOptionalPart(out, parseRolename(currentGroup));
		if (aggregationSide.equals("from")) {
			out.append(" aggregation ");
			out.append(aggregationType);
		}
		currentGroup = matcher.group(11);
		out.append(currentGroup);
	}

	private String parseRolename(String in) {
		if (in == null) {
			return null;
		}
		Matcher matcher = OLD_ROLE_NAME.matcher(in);
		if (matcher.matches()) {
			StringBuilder out = new StringBuilder();
			out.append(matcher.group(1));
			out.append("role ");
			out.append(matcher.group(2));
			return out.toString();
		}
		return in;
	}

	private void appendOptionalPart(StringBuilder out, String currentGroup) {
		if (currentGroup != null) {
			out.append(currentGroup);
		}
	}

	private static CommandLine processCommandLineOptions(String[] args) {
		String toolString = "java " + TGraphToTGraph2Converter.class.getName();
		String versionString = JGraLab.getInfo(false);
		OptionHandler oh = new OptionHandler(toolString, versionString);

		Option input = new Option("i", "input", true,
				"(optional): input TG file, if omitted, the tool reads from stdin");
		input.setRequired(false);
		input.setArgName("file");
		oh.addOption(input);

		Option output = new Option("o", "output", true,
				"(optional): output TG file, if omitted, the tool writes to stdout");
		output.setRequired(false);
		output.setArgName("file");
		oh.addOption(output);

		return oh.parse(args);
	}

	/**
	 * Uses the apache cli interface for command line handling.
	 * 
	 * @param args
	 *            the command line parameters
	 */
	public static void main(String[] args) {
		CommandLine cmdl = processCommandLineOptions(args);
		try {
			InputStream in = cmdl.hasOption('i') ? new FileInputStream(cmdl
					.getOptionValue('i')) : System.in;
			OutputStream out = cmdl.hasOption('o') ? new FileOutputStream(cmdl
					.getOptionValue('o')) : System.out;
			TGraphToTGraph2Converter converter = new TGraphToTGraph2Converter();
			converter.convertTGStream(out, in);
			System.out.println("Fini.");
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}
}
