package de.uni_koblenz.jgralab.greql.optimizer;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.text.NumberFormat;
import java.util.HashMap;

import de.uni_koblenz.jgralab.Edge;
import de.uni_koblenz.jgralab.Graph;
import de.uni_koblenz.jgralab.GraphIO;
import de.uni_koblenz.jgralab.GraphIOException;
import de.uni_koblenz.jgralab.ImplementationType;
import de.uni_koblenz.jgralab.Vertex;
import de.uni_koblenz.jgralab.impl.ConsoleProgressFunction;
import de.uni_koblenz.jgralab.schema.GraphElementClass;
import de.uni_koblenz.jgralab.schema.Schema;
import de.uni_koblenz.jgralab.schema.VertexClass;

public class OptimizerInfoGenerator {
	private Schema schema;
	private HashMap<GraphElementClass<?, ?>, Long> gecCount;
	private HashMap<GraphElementClass<?, ?>, Long> gecWithSubclassCount;
	private long totalVertexCount;
	private long totalEdgeCount;
	private int graphCount;

	public OptimizerInfoGenerator(Schema schema) {
		if (schema == null) {
			throw new IllegalArgumentException("Schema must not be null");
		}
		this.schema = schema;
		gecCount = new HashMap<GraphElementClass<?, ?>, Long>();
		gecWithSubclassCount = new HashMap<GraphElementClass<?, ?>, Long>();
		for (GraphElementClass<?, ?> gec : schema.getGraphClass()
				.getGraphElementClasses()) {
			gecCount.put(gec, 0l);
		}
	}

	public void scanGraph(Graph graph) {
		if (!schema.equals(graph.getSchema())) {
			throw new IllegalArgumentException(
					"Graph has different schema, expected \""
							+ schema.getQualifiedName() + "\", found \""
							+ graph.getSchema().getQualifiedName() + "\"");
		}
		++graphCount;
		totalVertexCount += graph.getVCount();
		for (Vertex v : graph.vertices()) {
			gecCount.put(v.getAttributedElementClass(),
					gecCount.get(v.getAttributedElementClass()) + 1l);
		}
		totalEdgeCount += graph.getECount();
		for (Edge e : graph.edges()) {
			gecCount.put(e.getAttributedElementClass(),
					gecCount.get(e.getAttributedElementClass()) + 1l);
		}
	}

	public void scanDirectory(String dirName, FilenameFilter filter,
			boolean recursive) throws GraphIOException {
		File graphDir = new File(dirName);
		System.out.println("Scanning directory \"" + graphDir.getPath()
				+ "\" ...");
		for (File file : graphDir.listFiles()) {
			if (file.isFile() && file.canRead()
					&& filter.accept(graphDir, file.getName())) {
				Graph graph = GraphIO.loadGraphFromFile(file.getPath(),
						ImplementationType.GENERIC,
						new ConsoleProgressFunction(file.getName()));
				scanGraph(graph);
			} else if (recursive && file.isDirectory() && file.canRead()) {
				scanDirectory(file.getPath(), filter, recursive);
			}
		}
	}

	public void scanDirectory(String dirName) throws GraphIOException {
		scanDirectory(dirName, GraphIO.TGFilenameFilter.instance(), false);
	}

	public void scanDirectory(String dirName, boolean recursive)
			throws GraphIOException {
		scanDirectory(dirName, GraphIO.TGFilenameFilter.instance(), recursive);
	}

	public void storeOptimizerInfo(String propFilename) throws IOException {
		computeSubclassCounts();
		DefaultOptimizerInfo info = new DefaultOptimizerInfo(schema);
		info.setAvgVertexCount(totalVertexCount / graphCount);
		info.setAvgEdgeCount(totalEdgeCount / graphCount);

		for (GraphElementClass<?, ?> gec : schema.getGraphClass()
				.getGraphElementClasses()) {
			info.setFrequencies(gec, getFrequency(gec, false),
					getFrequency(gec, true));
		}
		info.storePropertyFile(propFilename);
	}

	public long getCount(GraphElementClass<?, ?> gec, boolean withSubclasses) {
		assertGraphScanned();
		if (withSubclasses) {
			return gecWithSubclassCount.get(gec);
		} else {
			return gecCount.get(gec);
		}
	}

	public double getFrequency(GraphElementClass<?, ?> gec,
			boolean withSubclasses) {
		assertGraphScanned();
		return (double) getCount(gec, withSubclasses)
				/ (gec instanceof VertexClass ? totalVertexCount
						: totalEdgeCount);
	}

	public void assertGraphScanned() {
		if (graphCount == 0) {
			throw new IllegalStateException("No graph scanned yet");
		}
	}

	public void printStatistics() {
		computeSubclassCounts();
		System.out.println("Schema\t" + schema.getQualifiedName());
		System.out.println("Graphs\t" + graphCount);
		System.out.println("Vertices\t" + totalVertexCount);
		System.out.println("Edges\t" + totalEdgeCount);
		System.out
				.println("V/E\tGraphElementClass\tcount\twith subclasses\tfreq\twith subclasses");
		NumberFormat fmt = NumberFormat.getInstance();
		fmt.setMaximumFractionDigits(16);
		for (GraphElementClass<?, ?> gec : schema.getGraphClass()
				.getGraphElementClasses()) {

			long countClassOnly = gecCount.get(gec);
			double freqClassOnly = (double) countClassOnly
					/ (gec instanceof VertexClass ? totalVertexCount
							: totalEdgeCount);
			long countWithSubclasses = gecWithSubclassCount.get(gec);
			double freqWithSubclasses = (double) countWithSubclasses
					/ (gec instanceof VertexClass ? totalVertexCount
							: totalEdgeCount);
			System.out.println((gec instanceof VertexClass ? "V" : "E") + "\t"
					+ gec.getQualifiedName() + "\t" + gecCount.get(gec) + "\t"
					+ countWithSubclasses + "\t" + fmt.format(freqClassOnly)
					+ "\t" + fmt.format(freqWithSubclasses));
		}
	}

	private void computeSubclassCounts() {
		assertGraphScanned();
		for (GraphElementClass<?, ?> gec : schema.getGraphClass()
				.getGraphElementClasses()) {
			long cnt = gecCount.get(gec);
			for (GraphElementClass<?, ?> sub : gec.getAllSubClasses()) {
				cnt += gecCount.get(sub);
			}
			gecWithSubclassCount.put(gec, cnt);
		}
	}
}
