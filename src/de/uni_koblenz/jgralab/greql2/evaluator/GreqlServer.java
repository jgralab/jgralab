package de.uni_koblenz.jgralab.greql2.evaluator;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashSet;
import java.util.Map.Entry;

import de.uni_koblenz.jgralab.Graph;
import de.uni_koblenz.jgralab.GraphIO;
import de.uni_koblenz.jgralab.codegenerator.CodeGeneratorConfiguration;
import de.uni_koblenz.jgralab.greql2.exception.Greql2Exception;
import de.uni_koblenz.jgralab.greql2.jvalue.JValue;
import de.uni_koblenz.jgralab.impl.ProgressFunctionImpl;

public class GreqlServer extends Thread {

	private static Thread clientHandlerLoop;
	private static HashSet<GreqlServer> clients = new HashSet<GreqlServer>();

	private Socket socket;
	private BufferedReader in;
	private PrintWriter out;
	private GreqlEvaluator eval;

	public GreqlServer(Socket s) throws IOException {
		socket = s;
		in = new BufferedReader(new InputStreamReader(s.getInputStream()));
		out = new PrintWriter(new OutputStreamWriter(s.getOutputStream()));
		eval = new GreqlEvaluator((String) null, (Graph) null, null);
		System.out.println("New GreqlServer for " + socket.getInetAddress());
	}

	@Override
	public void run() {
		try {
			String currentGraphFile = null;
			String line = null;
			while (((line = in.readLine()) != null) && !isInterrupted()) {
				if (line.startsWith("g:")) {
					String newGraphFile = line.substring(2);
					if (newGraphFile.equals(currentGraphFile)) {
						System.out.println("This graph is already set.");
						continue;
					}
					currentGraphFile = newGraphFile;
					out.println("Loading " + currentGraphFile + ".");
					System.out.println("Loding " + currentGraphFile + ".");
					eval.setDatagraph(GraphIO.loadSchemaAndGraphFromFile(
							currentGraphFile,
							CodeGeneratorConfiguration.MINIMAL,
							new ProgressFunctionImpl()));
				} else if (line.startsWith("q:")) {
					String f = line.substring(2);
					out.println("Evaling query file " + f + ".");
					System.out.println("Evaling query file " + f + ".");
					eval.setQueryFile(new File(f));
					try {
						eval.startEvaluation();
						JValue result = eval.getEvaluationResult();
						out.println();
						out.println("Evaluation Result:");
						out.println("==================");
						if (result.isCollection()) {
							for (JValue jv : result.toCollection()) {
								out.println(jv.toString());
								System.out.println(jv.toString());
							}
						} else if (result.isMap()) {
							for (Entry<JValue, JValue> e : result.toJValueMap()
									.entrySet()) {
								out
										.println(e.getKey() + " --> "
												+ e.getValue());
								System.out.println(e.getKey() + " --> "
										+ e.getValue());
							}
						} else {
							out.println(result.toString());
							System.out.println(result.toString());
						}
					} catch (Greql2Exception e) {
						e.printStackTrace();
						e.printStackTrace(out);
					}
				} else {
					out.println("Don't understand line '" + line + "'.");
					System.out.println("Don't understand line '" + line + "'.");
				}
				out.println("\u000C");
				out.flush();
			}
			System.out.println("Goodbye!");
			in.close();
			out.close();
			socket.close();
			synchronized (GreqlServer.class) {
				clients.remove(this);
			}
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

		clientHandlerLoop = new Thread() {
			ServerSocket socket = new ServerSocket(10101);

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
	}

	private static void terminateServer() {
		clientHandlerLoop.interrupt();

		for (GreqlServer client : clients) {
			client.interrupt();
		}
	}
}
