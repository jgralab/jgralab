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
package de.uni_koblenz.jgralab.gretl;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.logging.Level;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;

import de.uni_koblenz.ist.utilities.option_handler.OptionHandler;
import de.uni_koblenz.jgralab.Graph;
import de.uni_koblenz.jgralab.GraphIO;
import de.uni_koblenz.jgralab.ImplementationType;
import de.uni_koblenz.jgralab.JGraLab;
import de.uni_koblenz.jgralab.exception.GraphIOException;
import de.uni_koblenz.jgralab.greql.GreqlQuery;
import de.uni_koblenz.jgralab.greql.schema.GreqlSchema;
import de.uni_koblenz.jgralab.gretl.template.TemplateSchema;
import de.uni_koblenz.jgralab.impl.ConsoleProgressFunction;
import de.uni_koblenz.jgralab.schema.Schema;
import de.uni_koblenz.jgralab.utilities.tg2dot.Tg2Dot;
import de.uni_koblenz.jgralab.utilities.tg2dot.dot.GraphVizOutputFormat;

public class GReTLRunner {

	static final int MAX_VISUALIZATION_SIZE = Integer.parseInt(System
			.getProperty("GretlMaxVizSize", "400"));

	static {
		JGraLab.setLogLevel(Level.OFF);

		// Load the greql schema so that the class loading time doesn't count
		// for the transformation time.
		GreqlSchema.instance().createGraph(ImplementationType.STANDARD);

		// Also load the greql machinery
		GreqlQuery
				.createQuery("from x: list(1..10) with x = 5 reportSet x end")
				.evaluate();

		// Ditto for the template schema
		TemplateSchema.instance().createGraph(ImplementationType.STANDARD);
	}

	private OptionHandler oh = null;

	public GReTLRunner() {
		String toolString = "java " + GReTLRunner.class.getName();
		String versionString = JGraLab.getInfo(false);

		oh = new OptionHandler(toolString, versionString);
		Option transform = new Option("t", "transformation", true,
				"(required) The GReTL transformation that should be executed.");
		transform.setArgName("gretl-file");
		transform.setRequired(true);
		oh.addOption(transform);

		Option schema = new Option("s", "schema", true,
				"(optional) The name of the target schema. "
						+ "Defaults to foo.bar.BazSchema.");
		schema.setArgName("schema-name");
		schema.setRequired(false);
		oh.addOption(schema);

		Option graphclass = new Option("g", "graphclass", true,
				"(optional) The name of the target graph class. "
						+ "Defaults to BazGraph.");
		graphclass.setArgName("graphclass");
		graphclass.setRequired(false);
		oh.addOption(graphclass);

		Option viz = new Option("z", "visualize", false,
				"(optional) Additionally create a PDF viz of the output graph.");
		viz.setRequired(false);
		oh.addOption(viz);

		Option reverseViz = new Option("r", "reverse-edges", false,
				"(optional) When -z is given, print edges pointing bottom-up.");
		reverseViz.setRequired(false);
		oh.addOption(reverseViz);

		Option debugExecution = new Option("d", "debug", false,
				"(optional) Print the target graph after each transformation op.");
		debugExecution.setRequired(false);
		oh.addOption(debugExecution);

		Option output = new Option("o", "output", true,
				"(optional) The file to store the target graph to.  If many input "
						+ "models are to be transformed, this has no effect.");
		output.setRequired(false);
		debugExecution.setArgName("target-graph-file");
		oh.addOption(output);

		// TODO: Basically, -u should exclude the usage of -s/-g.
		Option useSourceSchema = new Option(
				"u",
				"use-source-schema",
				false,
				"(optional) Use the source schema as target schema. "
						+ "In that case, no schema modifications may be performed by the transformation.");
		useSourceSchema.setRequired(false);
		oh.addOption(useSourceSchema);

		// TODO: Basically, -i should exclude the usage of -s/-g.
		Option inPlace = new Option(
				"i",
				"in-place",
				false,
				"(optional) Use the source graph as target graph. "
						+ "In that case, no schema modifications may be performed by the transformation.");
		inPlace.setRequired(false);
		oh.addOption(inPlace);

		oh.setArgumentCount(Option.UNLIMITED_VALUES);
		oh.setArgumentName("input-graph");
		oh.setOptionalArgument(false);
	}

	public void exec(String[] args) throws GraphIOException, IOException {
		CommandLine cli = oh.parse(args);

		if (cli.hasOption('d')) {
			Transformation.DEBUG_EXECUTION = true;
			Transformation.DEBUG_REVERSE_EDGES = cli.hasOption('r');
		}

		String schema, graphclass;
		if (cli.hasOption('s')) {
			schema = cli.getOptionValue('s');
		} else {
			schema = "foo.bar.BazSchema";
		}
		if (cli.hasOption('g')) {
			graphclass = cli.getOptionValue('g');
		} else {
			graphclass = "BazGraph";
		}

		Schema targetSchema = getExistingSchema(schema);
		Context c;
		if (targetSchema != null) {
			c = new Context(targetSchema);
		} else {
			c = new Context(schema, graphclass, Class.class.getClassLoader());
		}

		if (cli.getArgs().length == 0) {
			if (cli.hasOption('u') || cli.hasOption('i')) {
				System.err
						.println("Options -u and -i cannot be used if no source graph is given.");
				oh.printHelpAndExit(1);
			}

			Graph outGraph = executeTransformation(c,
					new File(cli.getOptionValue('t')));
			String outFileName = null;
			if (cli.hasOption('o')) {
				outFileName = cli.getOptionValue('o');
			} else {
				outFileName = "target_graph.tg";
			}
			saveTargetGraph(outGraph, outFileName, cli);
		} else {
			for (String in : cli.getArgs()) {
				Graph inGraph = GraphIO.loadGraphFromFile(in,
						ImplementationType.GENERIC,
						new ConsoleProgressFunction("Loading"));
				if (cli.hasOption('u')) {
					c = new Context(inGraph.getSchema());
				} else if (cli.hasOption('i')) {
					c = new Context(inGraph.getSchema());
					c.setTargetGraph(inGraph);
				}
				c.setSourceGraph(inGraph);

				Graph outGraph = executeTransformation(c,
						new File(cli.getOptionValue('t')));
				String outFileName = null;
				if (cli.hasOption('o') && (cli.getArgs().length == 1)) {
					outFileName = cli.getOptionValue('o');
				} else {
					File inFile = new File(in);
					outFileName = inFile.getParent() + File.separator
							+ "target_" + inFile.getName();
				}
				saveTargetGraph(outGraph, outFileName, cli);
			}
		}

		System.out.println("Fini.");
	}

	private Graph executeTransformation(Context c, File transFormFile) {
		ExecuteTransformation t = new ExecuteTransformation(c, transFormFile);
		boolean outermost = c.outermost;
		if (outermost) {
			System.out
					.print("Executing transformation " + t.getName() + "... ");
		}
		long startTime = System.currentTimeMillis();
		Graph outGraph = t.execute();
		if (outermost) {
			System.out.println("Finished ("
					+ (System.currentTimeMillis() - startTime) + "ms)");
		}
		return outGraph;
	}

	private Schema getExistingSchema(String qname) {
		try {
			Class<?> schemaClass = Class.forName(qname);
			Method m = schemaClass.getMethod("instance");
			return (Schema) m.invoke(null);
		} catch (Exception e) {
			return null;
		}
	}

	private void saveTargetGraph(Graph outGraph, String outFileName,
			CommandLine cli) throws GraphIOException, IOException {
		GraphIO.saveGraphToFile(outGraph, outFileName,
				new ConsoleProgressFunction("Saving"));
		if (cli.hasOption('z')) {
			if ((outGraph.getVCount() + outGraph.getECount()) > MAX_VISUALIZATION_SIZE) {
				System.err.println("Sorry, graph is too big to be dotted.");
			} else {
				String pdf = outFileName.replaceFirst("\\.tg(\\.gz)?$", ".pdf");
				Tg2Dot.convertGraph(outGraph, pdf, cli.hasOption('r'),
						GraphVizOutputFormat.PDF);
			}
		}
	}

	public static void main(String[] args) throws GraphIOException, IOException {
		GReTLRunner runner = new GReTLRunner();
		runner.exec(args);
	}

}
