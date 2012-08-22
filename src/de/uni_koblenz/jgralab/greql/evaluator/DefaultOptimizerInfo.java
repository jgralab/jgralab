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

package de.uni_koblenz.jgralab.greql.evaluator;

import java.util.HashMap;

import de.uni_koblenz.jgralab.greql.OptimizerInfo;
import de.uni_koblenz.jgralab.greql.types.TypeCollection;
import de.uni_koblenz.jgralab.schema.EdgeClass;
import de.uni_koblenz.jgralab.schema.GraphClass;
import de.uni_koblenz.jgralab.schema.GraphElementClass;
import de.uni_koblenz.jgralab.schema.Schema;
import de.uni_koblenz.jgralab.schema.VertexClass;

/**
 * This class is needed to propagate the size of the currently used graph along
 * different calculate methods
 * 
 * @author ist@uni-koblenz.de
 */
public class DefaultOptimizerInfo implements OptimizerInfo {
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

	public DefaultOptimizerInfo() {
		this(null);
	}

	public DefaultOptimizerInfo(Schema schema) {
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
	}

	/**
	 * 
	 * @return the number of EdgeTypes this GraphSize object knows
	 */
	@Override
	public int getEdgeClassCount() {
		return edgeClassCount;
	}

	/**
	 * 
	 * @return the number of VertexTypes this GraphSize object knows
	 */
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

	/**
	 * @return the number of vertices in this graphsize object
	 */
	@Override
	public long getAverageVertexCount() {
		return avgVertexCount;
	}

	/**
	 * @return the number of edge in this graphsize object
	 */
	@Override
	public long getAverageEdgeCount() {
		return avgEdgeCount;
	}

	/**
	 * @return the average number of subclasses of a vertex class
	 */
	@Override
	public double getAverageVertexSubclasses() {
		return avgVertexSubclasses;
	}

	/**
	 * @return the average number of subclasses of an edge class
	 */
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
		return 1.5;
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
