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

package de.uni_koblenz.jgralab.greql.optimizer;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Properties;

import de.uni_koblenz.jgralab.Graph;
import de.uni_koblenz.jgralab.greql.OptimizerInfo;
import de.uni_koblenz.jgralab.greql.types.TypeCollection;
import de.uni_koblenz.jgralab.schema.EdgeClass;
import de.uni_koblenz.jgralab.schema.GraphClass;
import de.uni_koblenz.jgralab.schema.GraphElementClass;
import de.uni_koblenz.jgralab.schema.Schema;
import de.uni_koblenz.jgralab.schema.VertexClass;

/**
 * Contains statistical information about the {@link Graph}s of a specific
 * {@link Schema} to support optimizer decisions.
 * 
 * @author ist@uni-koblenz.de
 */
public class DefaultOptimizerInfo implements OptimizerInfo {
	public static final String PROPERTY_FILE_VERSION = "OptimizerInfo-1.0";

	private static final String OPTIMIZER_INFO_VERSION_KEY = "OptimizerInfoVersion";
	private static final String AVERAGE_VERTEX_COUNT_KEY = "AverageVertexCount";
	private static final String AVERAGE_EDGE_COUNT_KEY = "AverageEdgeCount";
	private static final String QUALIFIED_SCHEMA_NAME_KEY = "QualifiedSchemaName";

	private static final double DEFAULT_AVG_EC_SUBCLASSES = 2.0;
	private static final double DEFAULT_AVG_VC_SUBCLASSES = 2.0;
	private static final int DEFAULT_ABSTRACT_EC_COUNT = 10;
	private static final int DEFAULT_EC_COUNT = 50;
	private static final int DEFAULT_ABSTRACT_VC_COUNT = 10;
	private static final int DEFAULT_VC_COUNT = 50;
	private static final int DEFAULT_AVG_EDGE_COUNT = 15000;
	private static final long DEFAULT_AVG_VERTEX_COUNT = 10000;

	private Schema schema;
	private long avgVertexCount;
	private long avgEdgeCount;
	private int abstractVertexClassCount;
	private int abstractEdgeClassCount;
	private int vertexClassCount;
	private int edgeClassCount;
	private double avgEdgeSubclasses;
	private double avgVertexSubclasses;
	private HashMap<GraphElementClass<?, ?>, Double> frequenciesWithoutSubclasses;
	private HashMap<GraphElementClass<?, ?>, Double> frequencies;

	/**
	 * Creates a common OptimizerInfo without schema specific statistics.
	 */
	public DefaultOptimizerInfo() {
		this(null);
	}

	/**
	 * Creates an OptimizerInfo with some schema specific statistics for
	 * {@link Schema} <code>schema</code>.
	 * 
	 * @param schema
	 *            a {@link Schema}
	 */
	public DefaultOptimizerInfo(Schema schema) {
		this(schema, null);
	}

	/**
	 * Creates an OptimizerInfo for {@link Schema} <code>schema</code> with
	 * schema specific statistics loaded from property file
	 * <code>propFilename</code>. Such property files can be created with a
	 * {@link OptimizerInfoGenerator}.
	 * 
	 * @param schema
	 *            a {@link Schema}
	 * @param propFilename
	 *            the name of a property file containing optimizer info
	 */
	public DefaultOptimizerInfo(Schema schema, String propFilename) {
		this.schema = schema;
		avgVertexCount = DEFAULT_AVG_VERTEX_COUNT;
		avgEdgeCount = DEFAULT_AVG_EDGE_COUNT;
		if (schema == null) {
			vertexClassCount = DEFAULT_VC_COUNT;
			abstractVertexClassCount = DEFAULT_ABSTRACT_VC_COUNT;
			edgeClassCount = DEFAULT_EC_COUNT;
			abstractEdgeClassCount = DEFAULT_ABSTRACT_EC_COUNT;
			avgVertexSubclasses = DEFAULT_AVG_VC_SUBCLASSES;
			avgEdgeSubclasses = DEFAULT_AVG_EC_SUBCLASSES;
			return;
		}

		GraphClass gc = schema.getGraphClass();
		vertexClassCount = gc.getVertexClassCount();
		edgeClassCount = gc.getEdgeClassCount();
		abstractVertexClassCount = 0;
		abstractEdgeClassCount = 0;
		avgVertexSubclasses = 0.0;
		avgEdgeSubclasses = 0.0;
		if (vertexClassCount > 0) {
			int n = 0;
			for (VertexClass vc : gc.getVertexClasses()) {
				n += vc.getAllSubClasses().size();
				if (vc.isAbstract()) {
					abstractVertexClassCount++;
				}
			}
			avgVertexSubclasses = (double) n / vertexClassCount;
			if (abstractVertexClassCount == vertexClassCount) {
				// fallback to prevent div/0
				abstractVertexClassCount = 0;
			}
		}
		if (edgeClassCount > 0) {
			int n = 0;
			for (EdgeClass ec : gc.getEdgeClasses()) {
				n += ec.getAllSubClasses().size();
				if (ec.isAbstract()) {
					abstractEdgeClassCount++;
				}
			}
			avgEdgeSubclasses = (double) n / edgeClassCount;
			if (abstractEdgeClassCount == edgeClassCount) {
				// fallback to prevent div/0
				abstractEdgeClassCount = 0;
			}
		}

		// init frequencies of graph element classes WITHOUT subclasses
		// gec is abstract -> f = 0.0
		// else f = 1 / number of non-abstract classes
		frequenciesWithoutSubclasses = new HashMap<GraphElementClass<?, ?>, Double>(
				vertexClassCount + edgeClassCount);
		for (GraphElementClass<?, ?> gec : gc.getGraphElementClasses()) {
			if (gec instanceof VertexClass) {
				frequenciesWithoutSubclasses.put(gec, gec.isAbstract() ? 0.0
						: 1.0 / (vertexClassCount - abstractVertexClassCount));
			} else {
				frequenciesWithoutSubclasses.put(gec, gec.isAbstract() ? 0.0
						: 1.0 / (edgeClassCount - abstractEdgeClassCount));
			}
		}

		// init frequencies of graph element classes WITH subclasses
		// by traversing the classes in reverse topological order
		frequencies = new HashMap<GraphElementClass<?, ?>, Double>(
				vertexClassCount + edgeClassCount);

		for (GraphElementClass<?, ?> gec : schema.getGraphClass()
				.getGraphElementClasses()) {
			double f = frequenciesWithoutSubclasses.get(gec);
			for (GraphElementClass<?, ?> sub : gec.getAllSubClasses()) {
				f += frequenciesWithoutSubclasses.get(sub);
			}
			frequencies.put(gec, f);
		}

		if (propFilename != null) {
			try {
				loadFromPropertyFile(propFilename);
			} catch (IOException ex) {
				throw new RuntimeException(ex);
			}
		}
	}

	/**
	 * Stores the statistics of this DefaultOptimizerInfo into property file
	 * <code>propFilename</code>.
	 * 
	 * @param propFilename
	 *            the name of the property file containing optimizer info
	 * @throws IOException
	 *             when the file can not be created
	 */
	public void storePropertyFile(String propFilename) throws IOException {
		Properties properties = new Properties();
		properties.put(OPTIMIZER_INFO_VERSION_KEY, PROPERTY_FILE_VERSION);
		properties.put(QUALIFIED_SCHEMA_NAME_KEY, schema.getQualifiedName());
		properties.put(AVERAGE_VERTEX_COUNT_KEY, Long.toString(avgVertexCount));
		properties.put(AVERAGE_EDGE_COUNT_KEY, Long.toString(avgEdgeCount));
		for (GraphElementClass<?, ?> gec : frequencies.keySet()) {
			properties.put(
					(gec instanceof VertexClass ? "VC_" : "EC_")
							+ gec.getQualifiedName(),
					frequenciesWithoutSubclasses.get(gec) + ";"
							+ frequencies.get(gec));
		}
		BufferedOutputStream stream = new BufferedOutputStream(
				new FileOutputStream(propFilename));
		properties.store(stream, null);
		stream.close();
	}

	/**
	 * Loads the statistics from property file <code>propFilename</code> into
	 * this DefaultOptimizerInfo.
	 * 
	 * @param propFilename
	 *            the name of the property file containing optimizer info
	 * @throws IOException
	 *             when the file can not be read
	 */
	private void loadFromPropertyFile(String propFileame) throws IOException {
		if (schema == null) {
			throw new IllegalStateException(
					"schema must not be null when loading a property file");
		}
		BufferedInputStream stream = new BufferedInputStream(
				new FileInputStream(propFileame));
		Properties properties = new Properties();
		properties.load(stream);
		String version = properties.getProperty(OPTIMIZER_INFO_VERSION_KEY);
		if (!PROPERTY_FILE_VERSION.equals(version)) {
			throw new RuntimeException(
					"Wrong property file format, expected: \""
							+ PROPERTY_FILE_VERSION + "\", found: \"" + version
							+ "\"");
		}
		String qn = properties.getProperty(QUALIFIED_SCHEMA_NAME_KEY);
		if (!schema.getQualifiedName().equals(qn)) {
			throw new RuntimeException("Schema name mismatch, expected \""
					+ schema.getQualifiedName() + "\", found \"" + qn + "\"");
		}
		for (String key : properties.stringPropertyNames()) {
			String val = properties.getProperty(key);
			if (key.startsWith("VC_") || key.startsWith("EC_")) {
				String name = key.substring(3);
				GraphElementClass<?, ?> gec = schema.getGraphClass()
						.getGraphElementClass(name);
				if (gec == null) {
					throw new RuntimeException("GraphElementClass \"" + name
							+ "\" does not exist in schema \""
							+ schema.getQualifiedName() + "\"");
				}
				String[] f = val.split(";");
				frequenciesWithoutSubclasses.put(gec, Double.parseDouble(f[0]));
				frequencies.put(gec, Double.parseDouble(f[1]));
			} else if (key.equals(AVERAGE_VERTEX_COUNT_KEY)) {
				setAvgVertexCount(Long.parseLong(val));
			} else if (key.equals(AVERAGE_EDGE_COUNT_KEY)) {
				setAvgEdgeCount(Long.parseLong(val));
			} else {
				if (!key.equals(QUALIFIED_SCHEMA_NAME_KEY)
						&& !key.equals(OPTIMIZER_INFO_VERSION_KEY)) {
					throw new RuntimeException("Unknown property key \"" + key
							+ "\"");
				}
			}
		}
	}

	@Override
	public Schema getSchema() {
		return schema;
	}

	@Override
	public int getEdgeClassCount() {
		return edgeClassCount;
	}

	@Override
	public int getVertexClassCount() {
		return vertexClassCount;
	}

	public int getAbstractVertexClassCount() {
		return abstractVertexClassCount;
	}

	public int getAbstractEdgeClassCount() {
		return abstractEdgeClassCount;
	}

	@Override
	public long getAverageVertexCount() {
		return avgVertexCount;
	}

	@Override
	public long getAverageEdgeCount() {
		return avgEdgeCount;
	}

	public void setSchema(Schema schema) {
		if (this.schema != null) {
			throw new IllegalArgumentException("Schema can be set only once");
		}
		this.schema = schema;
	}

	public void setAvgVertexCount(long avgVertexCount) {
		if (avgVertexCount <= 0) {
			throw new IllegalArgumentException("avgVertexCount must be > 0");
		}
		this.avgVertexCount = avgVertexCount;
	}

	public void setAvgEdgeCount(long avgEdgeCount) {
		if (avgEdgeCount <= 0) {
			throw new IllegalArgumentException("avgEdgeCount must be > 0");
		}
		this.avgEdgeCount = avgEdgeCount;
	}

	public void setFrequencies(GraphElementClass<?, ?> gec,
			double freqWithoutSubclasses, double freq) {
		if (schema == null) {
			throw new IllegalStateException(
					"Schema must be set before defining frequencies");
		}
		if (gec.getSchema() != schema) {
			throw new IllegalArgumentException(
					"GraphElementClass does not belong to schema");
		}
		frequenciesWithoutSubclasses.put(gec, freqWithoutSubclasses);
		frequencies.put(gec, freq);
	}

	@Override
	public double getAverageVertexSubclasses() {
		return avgVertexSubclasses;
	}

	@Override
	public double getAverageEdgeSubclasses() {
		return avgEdgeSubclasses;
	}

	@Override
	public double getFrequencyOfGraphElementClass(GraphElementClass<?, ?> gec) {
		if (schema == null) {
			if (gec instanceof VertexClass) {
				return getAverageVertexCount()
						* getAverageVertexSubclasses()
						/ (getVertexClassCount() - getAbstractVertexClassCount());
			} else {
				return getAverageEdgeCount() * getAverageEdgeSubclasses()
						/ (getEdgeClassCount() - getAbstractEdgeClassCount());
			}
		} else {
			return frequencies.get(gec);
		}
	}

	@Override
	public double getFrequencyOfGraphElementClassWithoutSubclasses(
			GraphElementClass<?, ?> gec) {
		if (schema == null) {
			if (gec.isAbstract()) {
				return 0.0;
			}
			if (gec instanceof VertexClass) {
				return (double) getAverageVertexCount()
						/ (getVertexClassCount() - getAbstractVertexClassCount());
			} else {
				return (double) getAverageEdgeCount()
						/ (getEdgeClassCount() - getAbstractEdgeClassCount());
			}
		} else {
			return frequenciesWithoutSubclasses.get(gec);
		}
	}

	@Override
	public double getFrequencyOfTypeCollection(TypeCollection tc) {
		return tc.getFrequency(this);
	}

	@Override
	public double getEdgesPerVertex() {
		return (double) getAverageEdgeCount() / getAverageVertexCount();
	}

	@Override
	public long getEstimatedGraphElementCount(GraphElementClass<?, ?> gec) {
		if (gec instanceof VertexClass) {
			return (long) (getAverageVertexCount() * getFrequencyOfGraphElementClass(gec));
		} else {
			return (long) (getAverageEdgeCount() * getFrequencyOfGraphElementClass(gec));
		}
	}

	@Override
	public long getEstimatedGraphElementCount(TypeCollection tc) {
		return tc.getEstimatedGraphElementCount(this);
	}
}
