package de.uni_koblenz.jgralab.greql2.evaluator;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashSet;
import java.util.Map.Entry;

import de.uni_koblenz.jgralab.Graph;
import de.uni_koblenz.jgralab.GraphIO;
import de.uni_koblenz.jgralab.codegenerator.CodeGeneratorConfiguration;
import de.uni_koblenz.jgralab.greql2.jvalue.JValue;
import de.uni_koblenz.jgralab.impl.ProgressFunctionImpl;

public class GreqlEvalServer extends Thread {

	private static Thread clientHandlerLoop;
	private static HashSet<GreqlEvalServer> clients = new HashSet<GreqlEvalServer>();

	private Socket socket;
	private BufferedReader in;
	private BufferedWriter out;
	private GreqlEvaluator eval;

	public GreqlEvalServer(Socket s) throws IOException {
		socket = s;
		in = new BufferedReader(new InputStreamReader(s.getInputStream()));
		out = new BufferedWriter(new OutputStreamWriter(s.getOutputStream()));
		eval = new GreqlEvaluator((String) null, (Graph) null, null);
	}

	@Override
	public void run() {
		try {
			String line = null;
			while (((line = in.readLine()) != null) && !isInterrupted()) {
				if (line.startsWith("g:")) {
					eval.setDatagraph(GraphIO.loadSchemaAndGraphFromFile(line
							.substring(2), CodeGeneratorConfiguration.MINIMAL,
							new ProgressFunctionImpl()));
				} else if (line.startsWith("q:")) {
					eval.setQuery(line.substring(2));
					eval.startEvaluation();
					JValue result = eval.getEvaluationResult();
					out.append("Evaluation Result:\n");
					out.append("==================\n");
					if (result.isCollection()) {
						for (JValue jv : result.toCollection()) {
							out.append(jv.toString());
							out.newLine();
						}
					} else if (result.isMap()) {
						for (Entry<JValue, JValue> e : result.toJValueMap()
								.entrySet()) {
							out.append(e.getKey() + " --> " + e.getValue());
							out.newLine();
						}
					} else {
						out.append(result.toString());
						out.newLine();
					}
					out.append(Character.forDigit(12, 10));
					out.newLine();
					out.flush();
				}
			}
			in.close();
			out.close();
			socket.close();
		} catch (Exception e) {

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

						GreqlEvalServer client = new GreqlEvalServer(s);
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

		for (GreqlEvalServer client : clients) {
			client.interrupt();
		}
	}
}
