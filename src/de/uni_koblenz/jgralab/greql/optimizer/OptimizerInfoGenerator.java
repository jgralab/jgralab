/*
 * JGraLab - The Java Graph Laboratory
 *
 * Copyright (C) 2006-2014 Institute for Software Technology
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
package de.uni_koblenz.jgralab.greql.optimizer;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.PrintStream;
import java.text.NumberFormat;
import java.util.HashMap;

import de.uni_koblenz.jgralab.Edge;
import de.uni_koblenz.jgralab.Graph;
import de.uni_koblenz.jgralab.GraphIO;
import de.uni_koblenz.jgralab.GraphIO.TGFilenameFilter;
import de.uni_koblenz.jgralab.ImplementationType;
import de.uni_koblenz.jgralab.Vertex;
import de.uni_koblenz.jgralab.exception.GraphIOException;
import de.uni_koblenz.jgralab.greql.OptimizerInfo;
import de.uni_koblenz.jgralab.impl.ConsoleProgressFunction;
import de.uni_koblenz.jgralab.schema.GraphElementClass;
import de.uni_koblenz.jgralab.schema.Schema;
import de.uni_koblenz.jgralab.schema.VertexClass;

/**
 * {@link OptimizerInfoGenerator} collects data from graphs and creates a schema
 * specific optimizer info file.
 *
 * @author ist@uni-koblenz.de
 */
public class OptimizerInfoGenerator {
    private Schema schema;
    private HashMap<GraphElementClass<?, ?>, Long> gecCount;
    private HashMap<GraphElementClass<?, ?>, Long> gecWithSubclassCount;
    private long totalVertexCount;
    private long totalEdgeCount;
    private int graphCount;

    /**
     * Creates a new OptimizerInfoGenerator for the <code>schema</code>.
     *
     * @param schema a {@link Schema}, must not be <code>null</code>
     */
    public OptimizerInfoGenerator(Schema schema) {
        if (schema == null) {
            throw new IllegalArgumentException("Schema must not be null");
        }
        this.schema = schema;
        gecCount = new HashMap<>();
        gecWithSubclassCount = new HashMap<>();
        for (GraphElementClass<?, ?> gec : schema.getGraphClass()
                .getGraphElementClasses()) {
            gecCount.put(gec, 0l);
        }
    }

    /**
     * Scans the <code>graph</code> and adds vertex/edge counts to this
     * OptimizerInfoGenerator.
     *
     * @param graph a {@link Graph}, must have the same schema as this
     *        OptimizerInfoGenerator
     */
    public void scanGraph(Graph graph) {
        if (!schema.equals(graph.getSchema())) {
            throw new IllegalArgumentException("Graph has different schema, expected \"" + schema.getQualifiedName()
                    + "\", found \"" + graph.getSchema()
                            .getQualifiedName()
                    + "\"");
        }
        ++graphCount;
        totalVertexCount += graph.getVCount();
        for (Vertex v : graph.vertices()) {
            gecCount.put(v.getAttributedElementClass(), gecCount.get(v.getAttributedElementClass()) + 1l);
        }
        totalEdgeCount += graph.getECount();
        for (Edge e : graph.edges()) {
            gecCount.put(e.getAttributedElementClass(), gecCount.get(e.getAttributedElementClass()) + 1l);
        }
    }

    /**
     * Scans all graphs in directory <code>dirName</code> whose names are accepted
     * by the <code>filter</code>. When <code>recursive</code> is true, all
     * subdirectories are included.
     *
     * @param dirName a directory name
     * @param filter a FilenameFilter for graph file names
     * @param recursive when <code>true</code>, also scan subdirectories
     * @throws GraphIOException when a graph can not be loaded
     */
    public void scanDirectory(String dirName, FilenameFilter filter, boolean recursive) throws GraphIOException {
        File graphDir = new File(dirName);
        System.out.println("Scanning directory \"" + graphDir.getPath() + "\" ...");
        File[] graphs = graphDir.listFiles();
        if (graphs != null) {
            for (File file : graphs) {
                if (file.isFile() && file.canRead() && filter.accept(graphDir, file.getName())) {
                    Graph graph = GraphIO.loadGraphFromFile(file.getPath(), ImplementationType.GENERIC,
                            new ConsoleProgressFunction(file.getName()));
                    scanGraph(graph);
                } else if (recursive && file.isDirectory() && file.canRead()) {
                    scanDirectory(file.getPath(), filter, recursive);
                }
            }
        }
    }

    /**
     * Scans all graphs in directory <code>dirName</code> whose names are accepted
     * by {@link TGFilenameFilter}.
     *
     * @param dirName a directory name
     * @throws GraphIOException when a graph can not be loaded
     */
    public void scanDirectory(String dirName) throws GraphIOException {
        scanDirectory(dirName, GraphIO.TGFilenameFilter.instance(), false);
    }

    /**
     * Scans all graphs in directory <code>dirName</code> whose names are accepted
     * by {@link TGFilenameFilter}. When <code>recursive</code> is true, all
     * subdirectories are included.
     *
     * @param dirName a directory name
     * @throws GraphIOException when a graph can not be loaded
     */
    public void scanDirectory(String dirName, boolean recursive) throws GraphIOException {
        scanDirectory(dirName, GraphIO.TGFilenameFilter.instance(), recursive);
    }

    /**
     * Stores the {@link OptimizerInfo} collected by this OptimizerInfoGenerator
     * into the property file <code>propFilename</code>
     *
     * @param propFilename the name of the property file (should end with
     *        ".optimizerinfo.properties")
     * @throws IOException when the file can not be stored
     */
    public void storeOptimizerInfo(String propFilename) throws IOException {
        computeSubclassCounts();
        DefaultOptimizerInfo info = new DefaultOptimizerInfo(schema);
        info.setAvgVertexCount(totalVertexCount / graphCount);
        info.setAvgEdgeCount(totalEdgeCount / graphCount);

        for (GraphElementClass<?, ?> gec : schema.getGraphClass()
                .getGraphElementClasses()) {
            info.setFrequencies(gec, getFrequency(gec, false), getFrequency(gec, true));
        }
        info.storePropertyFile(propFilename);
    }

    /**
     * Computes the count of instances of <code>gec</code> in all scanned graphs.
     *
     * @param gec a {@link GraphElementClass}
     * @param withSubclasses when true, the result includes all subclasses of
     *        <code>gec</code>
     * @return the count of all instances of <code>gec</code>
     */
    public long getCount(GraphElementClass<?, ?> gec, boolean withSubclasses) {
        assertGraphScanned();
        if (withSubclasses) {
            return gecWithSubclassCount.get(gec);
        } else {
            return gecCount.get(gec);
        }
    }

    /**
     * Computes the frequency of <code>gec</code> in all scanned graphs.
     *
     * @param gec a {@link GraphElementClass}
     * @param withSubclasses when true, the result includes all subclasses of
     *        <code>gec</code>
     * @return the frequency <code>gec</code> in all scanned graphs, 0.0 <= result
     *         <= 1.0
     */
    public double getFrequency(GraphElementClass<?, ?> gec, boolean withSubclasses) {
        assertGraphScanned();
        return (double) getCount(gec, withSubclasses)
                / (gec instanceof VertexClass ? totalVertexCount : totalEdgeCount);
    }

    /**
     * Prints statistics suitable for import into spreadsheets to the stream
     * <code>out</code>.
     *
     * @param out a PrintStream
     */
    public void printStatistics(PrintStream out) {
        computeSubclassCounts();
        out.println("Schema\t" + schema.getQualifiedName());
        out.println("Graphs\t" + graphCount);
        out.println("Vertices\t" + totalVertexCount);
        out.println("Edges\t" + totalEdgeCount);
        out.println("V/E\tGraphElementClass\tcount\twith subclasses\tfreq\twith subclasses");
        NumberFormat fmt = NumberFormat.getInstance();
        fmt.setMaximumFractionDigits(16);
        for (GraphElementClass<?, ?> gec : schema.getGraphClass()
                .getGraphElementClasses()) {
            out.println((gec instanceof VertexClass ? "V" : "E") + "\t" + gec.getQualifiedName() + "\t"
                    + getCount(gec, false) + "\t" + getCount(gec, true) + "\t" + fmt.format(getFrequency(gec, false))
                    + "\t" + fmt.format(getFrequency(gec, true)));
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

    private void assertGraphScanned() {
        if (graphCount == 0) {
            throw new IllegalStateException("No graph scanned yet");
        }
    }
}
