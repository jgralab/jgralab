package de.uni_koblenz.jgralab.greql2.evaluator;

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

import de.uni_koblenz.jgralab.Graph;
import de.uni_koblenz.jgralab.GraphIO;
import de.uni_koblenz.jgralab.codegenerator.CodeGeneratorConfiguration;
import de.uni_koblenz.jgralab.greql2.jvalue.JValue;
import de.uni_koblenz.jgralab.greql2.jvalue.JValueCollection;
import de.uni_koblenz.jgralab.greql2.jvalue.JValueMap;
import de.uni_koblenz.jgralab.impl.ProgressFunctionImpl;

public class GreqlServer extends Thread {

	private static Thread clientHandlerLoop;
	private static HashSet<GreqlServer> clients = new HashSet<GreqlServer>();

	private Socket socket;
	private BufferedReader in;
	private PrintWriter out;
	private GreqlEvaluator eval;
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
					String newGraphFile = line.substring(2);
					Graph g = dataGraphs.get(newGraphFile);
					if (g == null) {
						println("Loading " + newGraphFile + ".",
								PrintTarget.BOTH, true);
						g = GraphIO.loadSchemaAndGraphFromFile(newGraphFile,
								CodeGeneratorConfiguration.MINIMAL,
								new ProgressFunctionImpl());
						dataGraphs.put(newGraphFile, g);
					}
					eval.setDatagraph(g);
				} else if (line.startsWith("q:")) {
					String f = line.substring(2);
					evalQuery(f);
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

	private void evalQuery(String queryFile) throws IOException {
		println("Evaling query file " + queryFile + ".", PrintTarget.BOTH, true);
		eval.setQueryFile(new File(queryFile));
		try {
			eval.startEvaluation();
			JValue result = eval.getEvaluationResult();
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
	}

	/**
	 * @param args
	 * @throws IOException
	 */
	public static void main(String[] args) throws IOException {
		// TODO Auto-generated method stub

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
