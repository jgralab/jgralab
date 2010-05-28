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
import de.uni_koblenz.jgralab.impl.ProgressFunctionImpl;
import de.uni_koblenz.jgralab.utilities.tg2dot.Tg2Dot;

public class GreqlServer extends Thread {

	private static Thread clientHandlerLoop;
	private static HashSet<GreqlServer> clients = new HashSet<GreqlServer>();

	private Socket socket;
	private BufferedReader in;
	private PrintWriter out;
	private GreqlEvaluator eval;
	private String graphFile;
	private static Map<String, Graph> dataGraphs = Collections
			.synchronizedMap(new HashMap<String, Graph>());

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
								new ProgressFunctionImpl());
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
				println("Result contains " + coll.size() + " elements.\n",
						PrintTarget.CLIENT, true);
				for (JValue jv : coll) {
					println(jv.toString(), PrintTarget.CLIENT, false);
				}
			} else if (result.isMap()) {
				JValueMap map = result.toJValueMap();
				println("Result contains " + map.size() + " map entries.\n",
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
