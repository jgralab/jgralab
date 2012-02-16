/*
 * JGraLab - The Java Graph Laboratory
 * 
 * Copyright (C) 2006-2012 Institute for Software Technology
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
package de.uni_koblenz.jgralab.utilities.greqlinterface;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;

import de.uni_koblenz.jgralab.AttributedElement;
import de.uni_koblenz.jgralab.Edge;
import de.uni_koblenz.jgralab.Graph;
import de.uni_koblenz.jgralab.GraphIO;
import de.uni_koblenz.jgralab.Vertex;
import de.uni_koblenz.jgralab.graphmarker.BooleanGraphMarker;
import de.uni_koblenz.jgralab.greql2.evaluator.GreqlEvaluatorImpl;
import de.uni_koblenz.jgralab.greql2.types.Path;
import de.uni_koblenz.jgralab.greql2.types.PathSystem;
import de.uni_koblenz.jgralab.greql2.types.Slice;
import de.uni_koblenz.jgralab.greql2.types.Types;
import de.uni_koblenz.jgralab.impl.ConsoleProgressFunction;
import de.uni_koblenz.jgralab.utilities.tg2dot.Tg2Dot;

public class GreqlServer extends Thread {

	private static Thread clientHandlerLoop;
	private static HashSet<GreqlServer> clients = new HashSet<GreqlServer>();

	private final Socket socket;
	private final BufferedReader in;
	private final PrintWriter out;
	private final GreqlEvaluatorImpl eval;
	private String graphFile;
	private static Map<String, Graph> dataGraphs = Collections
			.synchronizedMap(new HashMap<String, Graph>());

	// static {
	// GreqlEvaluator.DEBUG_OPTIMIZATION = true;
	// }

	public GreqlServer(Socket s) throws IOException {
		socket = s;
		in = new BufferedReader(new InputStreamReader(s.getInputStream()));
		out = new PrintWriter(new OutputStreamWriter(s.getOutputStream()));
		eval = new GreqlEvaluatorImpl((String) null, (Graph) null, null);
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
						g = GraphIO.loadGraphFromFile(graphFile,
								new ConsoleProgressFunction("Loading"));
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

	private void saveAsDot(Object val, String dotFileName) throws IOException {
		Graph g = eval.getDatagraph();
		BooleanGraphMarker marker = new BooleanGraphMarker(g);
		markResultElements(val, marker);
		for (Edge e : g.edges()) {
			if (marker.isMarked(e.getAlpha()) && marker.isMarked(e.getOmega())) {
				marker.mark(e);
			}
		}
		Tg2Dot.convertGraph(marker, dotFileName);
	}

	private void markResultElements(Object val, BooleanGraphMarker marker) {
		if (val instanceof Collection) {
			Collection<?> coll = (Collection<?>) val;
			for (Object v : coll) {
				markResultElements(v, marker);
			}
		} else if (val instanceof Map) {
			for (Entry<? extends Object, ? extends Object> e : ((Map<?, ?>) val)
					.entrySet()) {
				markResultElements(e.getKey(), marker);
				markResultElements(e.getValue(), marker);
			}
		} else if (val instanceof Slice) {
			Slice slice = (Slice) val;
			for (Vertex v : slice.getVertices()) {
				marker.mark(v);
			}
			for (Edge e : slice.getEdges()) {
				marker.mark(e);
			}
		} else if (val instanceof PathSystem) {
			PathSystem pathSystem = (PathSystem) val;
			for (Vertex v : pathSystem.getVertices()) {
				marker.mark(v);
			}
			for (Edge e : pathSystem.getEdges()) {
				marker.mark(e);
			}
		} else if (val instanceof Path) {
			Path path = (Path) val;
			for (Vertex v : path.getVertexTrace()) {
				marker.mark(v);
			}
			for (Edge e : path.getEdgeTrace()) {
				marker.mark(e);
			}
		} else if (val instanceof AttributedElement) {
			marker.mark((AttributedElement<?, ?>) val);
		} else {
			println("'" + val + "' is no AttributedElement, "
					+ "so it won't be considered for DOT output.",
					PrintTarget.BOTH, false);
		}
	}

	private Object evalQuery(String queryFile) throws IOException {
		println("Evaling query file " + queryFile + ".", PrintTarget.BOTH, true);
		eval.setQueryFile(new File(queryFile));
		Object result = null;
		try {
			long startTime = System.currentTimeMillis();
			eval.startEvaluation();
			result = eval.getResult();
			long evalTime = System.currentTimeMillis() - startTime;
			println("<result not printed>", PrintTarget.SERVER, false);
			out.println();
			out.println("Evaluation took " + evalTime + "ms.");
			out.println();
			out.println("Evaluation Result:");
			out.println("==================");
			if (result instanceof Collection) {
				Collection<?> coll = (Collection<?>) result;
				println("Result collection (" + coll.getClass().getSimpleName()
						+ ") contains " + coll.size() + " elements.\n",
						PrintTarget.CLIENT, true);
				for (Object jv : coll) {
					println(jv.toString(), PrintTarget.CLIENT, false);
				}
			} else if (result instanceof Map) {
				Map<?, ?> map = (Map<?, ?>) result;
				println("Result map contains " + map.size() + " map entries.\n",
						PrintTarget.CLIENT, true);
				for (Entry<? extends Object, ? extends Object> e : map
						.entrySet()) {
					println(e.getKey() + " --> " + e.getValue(),
							PrintTarget.CLIENT, false);
				}
			} else {
				println("Result is a single element of type "
						+ Types.getGreqlTypeName(result) + ".\n",
						PrintTarget.CLIENT, true);
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
