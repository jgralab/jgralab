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
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;

import de.uni_koblenz.jgralab.Edge;
import de.uni_koblenz.jgralab.Graph;
import de.uni_koblenz.jgralab.GraphIO;
import de.uni_koblenz.jgralab.Vertex;
import de.uni_koblenz.jgralab.codegenerator.CodeGeneratorConfiguration;
import de.uni_koblenz.jgralab.graphmarker.BooleanGraphMarker;
import de.uni_koblenz.jgralab.greql2.evaluator.GreqlEvaluator;
import de.uni_koblenz.jgralab.greql2.jvalue.JValue;
import de.uni_koblenz.jgralab.greql2.jvalue.JValueCollection;
import de.uni_koblenz.jgralab.greql2.jvalue.JValueMap;
import de.uni_koblenz.jgralab.greql2.jvalue.JValuePath;
import de.uni_koblenz.jgralab.greql2.jvalue.JValuePathSystem;
import de.uni_koblenz.jgralab.greql2.jvalue.JValueSlice;
import de.uni_koblenz.jgralab.impl.ConsoleProgressFunction;
import de.uni_koblenz.jgralab.utilities.tg2dot.Tg2Dot;

public class GreqlServer extends Thread {

	private static Thread clientHandlerLoop;
	private static HashSet<GreqlServer> clients = new HashSet<GreqlServer>();

	private final Socket socket;
	private final BufferedReader in;
	private final PrintWriter out;
	private final GreqlEvaluator eval;
	private String graphFile;
	private static Map<String, Graph> dataGraphs = Collections
			.synchronizedMap(new HashMap<String, Graph>());
	
	static {
		GreqlEvaluator.DEBUG_OPTIMIZATION = true;
	}
	
	public GreqlServer(Socket s) throws IOException {
		socket = s;
		in = new BufferedReader(new InputStreamReader(s.getInputStream()));
		out = new PrintWriter(new OutputStreamWriter(s.getOutputStream()));
		eval = new GreqlEvaluator((String) null, (Graph) null, null);
		println("Hi! I'm your GreqlServer (" + socket.getInetAddress() + ")",
				PrintTarget.BOTH, true);
	}

	private enum PrintTarget {
		CLIENT, SERVER, BOTH
	}

	private void println(String message, PrintTarget target, boolean flush) {
		switch (target) {
		case CLIENT:
			out.println(message);
			break;
		case SERVER:
			System.out.println(message);
			break;
		case BOTH:
			out.println(message);
			System.out.println(message);
			break;
		default:
			break;
		}
		if (flush) {
			out.flush();
		}
	}

	@Override
	public void run() {
		try {
			String line = null;
			while (((line = in.readLine()) != null) && !isInterrupted()) {
				if (line.startsWith("g:")) {
					graphFile = line.substring(2);
					Graph g = dataGraphs.get(graphFile);
					if (g == null) {
						println("Loading " + graphFile + ".", PrintTarget.BOTH,
								true);
						g = GraphIO.loadSchemaAndGraphFromFile(graphFile,
								CodeGeneratorConfiguration.MINIMAL,
								new ConsoleProgressFunction());
						dataGraphs.put(graphFile, g);
					}
					eval.setDatagraph(g);
				} else if (line.startsWith("q:")) {
					evalQuery(line.substring(2));
				} else if (line.startsWith("d:")) {
					String queryFile = line.substring(2);
					saveAsDot(evalQuery(queryFile), queryFile + ".dot");
				} else {
					println("Don't understand line '" + line + "'.",
							PrintTarget.BOTH, true);
				}
				out.println("\u000C");
				out.flush();
			}
			println("GreqlServer says goodbye!", PrintTarget.BOTH, true);
			// FIXME these close statements should also be in the finally block.
			// It must be guaranteed, that they will be called.
			in.close();
			socket.close();
		} catch (Exception e) {
			e.printStackTrace();
			e.printStackTrace(out);
		} finally {
			out.close();
			synchronized (GreqlServer.class) {
				clients.remove(this);
			}
		}
	}

	private void saveAsDot(JValue val, String dotFileName) {
		Graph g = eval.getDatagraph();
		BooleanGraphMarker marker = new BooleanGraphMarker(g);
		markResultElements(val, marker);
		for (Edge e : g.edges()) {
			if (marker.isMarked(e.getAlpha()) && marker.isMarked(e.getOmega())) {
				marker.mark(e);
			}
		}
		Tg2Dot.printGraphAsDot(marker, false, dotFileName);
	}

	private void markResultElements(JValue val, BooleanGraphMarker marker) {
		if (val.isCollection()) {
			JValueCollection coll = val.toCollection();
			for (JValue v : coll) {
				markResultElements(v, marker);
			}
		} else if (val.isMap()) {
			for (Entry<JValue, JValue> e : val.toJValueMap().entrySet()) {
				markResultElements(e.getKey(), marker);
				markResultElements(e.getValue(), marker);
			}
		} else if (val.isSlice()) {
			JValueSlice slice = val.toSlice();
			for (JValue v : slice.nodes()) {
				marker.mark(v.toVertex());
			}
			for (JValue e : slice.edges()) {
				marker.mark(e.toEdge());
			}
		} else if (val.isPathSystem()) {
			JValuePathSystem pathSystem = val.toPathSystem();
			for (JValue v : pathSystem.nodes()) {
				marker.mark(v.toVertex());
			}
			for (JValue e : pathSystem.edges()) {
				marker.mark(e.toEdge());
			}
		} else if (val.isPath()) {
			JValuePath path = val.toPath();
			for (Vertex v : path.nodeTrace()) {
				marker.mark(v);
			}
			for (Edge e : path.edgeTrace()) {
				marker.mark(e);
			}
		} else if (val.isAttributedElement()) {
			marker.mark(val.toAttributedElement());
		} else {
			println("'" + val + "' is no AttributedElement, "
					+ "so it won't be considered for DOT output.",
					PrintTarget.BOTH, false);
		}
	}

	private JValue evalQuery(String queryFile) throws IOException {
		println("Evaling query file " + queryFile + ".", PrintTarget.BOTH, true);
		eval.setQueryFile(new File(queryFile));
		JValue result = null;
		try {
			eval.startEvaluation();
			result = eval.getEvaluationResult();
			println("<result not printed>", PrintTarget.SERVER, false);
			out.println();
			out.println("Evaluation Result:");
			out.println("==================");
			if (result.isCollection()) {
				JValueCollection coll = result.toCollection();
				println("Result collection (" + coll.getClass().getSimpleName()
						+ ") contains " + coll.size() + " elements.\n",
						PrintTarget.CLIENT, true);
				for (JValue jv : coll) {
					println(jv.toString(), PrintTarget.CLIENT, false);
				}
			} else if (result.isMap()) {
				JValueMap map = result.toJValueMap();
				println("Result map contains " + map.size() + " map entries.\n",
						PrintTarget.CLIENT, true);
				for (Entry<JValue, JValue> e : map.entrySet()) {
					println(e.getKey() + " --> " + e.getValue(),
							PrintTarget.CLIENT, false);
				}
			} else {
				println("Result is a single element.\n", PrintTarget.CLIENT,
						true);
				println(result.toString(), PrintTarget.CLIENT, false);
			}
			out.flush();
		} catch (Exception e) {
			e.printStackTrace();
			e.printStackTrace(out);
		}
		return result;
	}

	/**
	 * @param args
	 * @throws IOException
	 */
	public static void main(String[] args) throws IOException {
		Runtime.getRuntime().addShutdownHook(new Thread() {
			@Override
			public void run() {
				terminateServer();
			}
		});

		final int port = 10101;

		clientHandlerLoop = new Thread() {
			ServerSocket socket = new ServerSocket(port);

			@Override
			public void run() {
				while (!isInterrupted()) {
					Socket s = null;
					try {
						s = socket.accept();
						GreqlServer client = new GreqlServer(s);
						clients.add(client);
						client.start();
					} catch (IOException e) {
						System.err
								.println("Exception while accepting client...");
						e.printStackTrace();
					}
				}
			}
		};
		clientHandlerLoop.start();

		System.out.println("GreqlServer listening on port " + port);
	}

	private static void terminateServer() {
		clientHandlerLoop.interrupt();

		for (GreqlServer client : clients) {
			client.interrupt();
		}
	}
}
