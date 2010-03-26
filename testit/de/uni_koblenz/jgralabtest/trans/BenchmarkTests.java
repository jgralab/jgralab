package de.uni_koblenz.jgralabtest.trans;

import de.uni_koblenz.jgralab.Edge;
import de.uni_koblenz.jgralab.GraphIO;
import de.uni_koblenz.jgralab.GraphIOException;
import de.uni_koblenz.jgralab.ProgressFunction;
import de.uni_koblenz.jgralab.Vertex;
import de.uni_koblenz.jgralab.impl.ProgressFunctionImpl;
import de.uni_koblenz.jgralab.trans.CommitFailedException;
import de.uni_koblenz.jgralabtest.schemas.motorwaymap.City;
import de.uni_koblenz.jgralabtest.schemas.motorwaymap.Motorway;
import de.uni_koblenz.jgralabtest.schemas.motorwaymap.MotorwayMap;
import de.uni_koblenz.jgralabtest.schemas.motorwaymap.MotorwayMapSchema;

public class BenchmarkTests {
	private MotorwayMap motorwayMap;

	private static final int V = 1;
	private static final int E = 1;

	private static final int N = 1;

	private static final int MULTIPLIER = 5;
	private static final int NROFTHREADS = 20;

	private static final String FILENAME = "motorwaymap_benchmark.tg";
	private ProgressFunction progressFunction;

	/**
	 * 
	 */
	private BenchmarkTests() {
		progressFunction = new ProgressFunctionImpl();
	}

	/**
	 * @param args
	 * @throws CommitFailedException
	 * @throws InterruptedException
	 * @throws GraphIOException
	 */
	/**
	 * @param args
	 * @throws CommitFailedException
	 * @throws InterruptedException
	 * @throws GraphIOException
	 */
	public static void main(String[] args) throws CommitFailedException,
			InterruptedException, GraphIOException {
		BenchmarkTests benchmarkTests = new BenchmarkTests();
		benchmarkTests.iterateVertices();
		benchmarkTests.iterateVerticesWithTransactionSupport();
		benchmarkTests.addGraphElements();
		benchmarkTests.addGraphElementsWithTransactionSupport();
		benchmarkTests.addGraphElementsWithTransactionSupportParallel();
		benchmarkTests.loadGraph(false);
		benchmarkTests.loadGraph(true);
		benchmarkTests.test();
	}

	/**
	 * 
	 * @param testMethodName
	 */
	private void printMemoryUsage(String testMethodName) {
		System.out.println("Speicherverbrauch "
				+ testMethodName
				+ ": "
				+ (Runtime.getRuntime().totalMemory() - Runtime.getRuntime()
						.freeMemory() / 1048576.0) + " MB.");
	}

	private void addGraphElements() throws GraphIOException,
			InterruptedException {
		createGraph(false);
		progressFunction.init(N * MULTIPLIER);
		internaladdGraphElements();
		progressFunction.finished();
		printMemoryUsage("addGraphElements");
		// Thread.sleep(20000);
		saveGraph(false);
	}

	private void addGraphElementsWithTransactionSupport()
			throws CommitFailedException, GraphIOException,
			InterruptedException {
		createGraph(true);
		motorwayMap.newTransaction();
		progressFunction.init(N * MULTIPLIER);
		internaladdGraphElements();
		progressFunction.finished();
		printMemoryUsage("addGraphElementsWithTransactionSupport (vor Commit)");
		progressFunction.init(N * MULTIPLIER);
		motorwayMap.commit();
		progressFunction.finished();
		printMemoryUsage("addGraphElementsWithTransactionSupport (nach Commit)");
		motorwayMap.newReadOnlyTransaction();
		System.out.println(motorwayMap.getVCount() + motorwayMap.getECount());
		saveGraph(true);
	}

	@SuppressWarnings("unused")
	private void addGraphElementsWithTransactionSupportParallel()
			throws GraphIOException {
		createGraph(true);
		progressFunction.init(N * MULTIPLIER * NROFTHREADS);
		ThreadGroup group = new ThreadGroup("BenchmarkTests");
		for (int i = 0; i < NROFTHREADS; i++) {
			Thread thread = new Thread(group, "Thread-" + i) {
				@Override
				public void run() {
					motorwayMap.newTransaction();
					ProgressFunction p = new ProgressFunctionImpl();
					p.init(1234);
					internaladdGraphElements();
					try {
						p.finished();
						motorwayMap.commit();
					} catch (CommitFailedException e) {
						e.printStackTrace();
					}
				}
			};
			thread.start();
		}

		while (group.activeCount() > 0) {

		}
		progressFunction.finished();
		printMemoryUsage("addGraphElementsWithTransactionSupportParallel");
		motorwayMap.newReadOnlyTransaction();
		System.out.println(motorwayMap.getVCount() + motorwayMap.getECount());
		saveGraph(true);
	}

	private void internaladdGraphElements() {
		for (int i = 0; i < N; i++) {
			City city = motorwayMap.createCity();
			Motorway motorway = motorwayMap.createMotorway();
			motorwayMap.createExit(city, motorway);
			motorwayMap.createExit(city, motorway);
			motorwayMap.createExit(city, motorway);
		}
	}

	private void createGraph(boolean transactionSupport) {
		if (transactionSupport) {
			motorwayMap = MotorwayMapSchema.instance()
					.createMotorwayMapWithTransactionSupport(V, E);
		} else {
			motorwayMap = MotorwayMapSchema.instance().createMotorwayMap(V, E);
		}
	}

	@SuppressWarnings("unused")
	private void loadGraph(boolean transactionSupport) throws GraphIOException {
		if (transactionSupport) {
			motorwayMap = MotorwayMapSchema.instance()
					.loadMotorwayMapWithTransactionSupport(FILENAME,
							new ProgressFunctionImpl());
		} else {
			motorwayMap = MotorwayMapSchema.instance().loadMotorwayMap(
					FILENAME, new ProgressFunctionImpl());
		}
		printMemoryUsage("loadGraph");
	}

	private void saveGraph(boolean transactionSupport) throws GraphIOException {
		if (transactionSupport) {
			motorwayMap.newReadOnlyTransaction();
		}
		GraphIO.saveGraphToFile(FILENAME, motorwayMap,
				new ProgressFunctionImpl());
	}

	@SuppressWarnings("unused")
	private void iterateVertices() throws GraphIOException,
			InterruptedException {
		addGraphElements();
		internalIterateVertices();

	}

	@SuppressWarnings("unused")
	private void iterateVerticesWithTransactionSupport()
			throws CommitFailedException, GraphIOException,
			InterruptedException {
		addGraphElementsWithTransactionSupport();
		motorwayMap.newReadOnlyTransaction();
		internalIterateVertices();
	}

	private void internalIterateVertices() {
		progressFunction.init(1000);
		for (Vertex v : motorwayMap.vertices()) {
			v.getId();
			for (Edge edge : v.incidences()) {
				edge.getAlpha();
				edge.getOmega();
			}
		}
		progressFunction.finished();
	}

	public void test() {
		createGraph(true);
		for (int j = 0; j < 1000000; j++) {
			ThreadGroup group = new ThreadGroup("BenchmarkTests");
			for (int i = 0; i < NROFTHREADS; i++) {
				Thread thread = new Thread(group, "Thread-" + i) {
					@Override
					public void run() {
						motorwayMap.newTransaction();
						internaladdGraphElements();
						try {
							motorwayMap.commit();
						} catch (CommitFailedException e) {
							e.printStackTrace();
						}
					}
				};
				thread.start();
			}
			while (group.activeCount() > 0) {

			}
			System.out.println("Durchgang: " + j);
		}
	}
}
