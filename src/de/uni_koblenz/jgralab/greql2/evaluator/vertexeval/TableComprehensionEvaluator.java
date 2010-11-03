/*
 * JGraLab - The Java Graph Laboratory
 * 
 * Copyright (C) 2006-2010 Institute for Software Technology
 *                         University of Koblenz-Landau, Germany
 *                         ist@uni-koblenz.de
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

package de.uni_koblenz.jgralab.greql2.evaluator.vertexeval;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.Map.Entry;

import de.uni_koblenz.jgralab.EdgeDirection;
import de.uni_koblenz.jgralab.greql2.evaluator.GreqlEvaluator;
import de.uni_koblenz.jgralab.greql2.evaluator.VariableDeclarationLayer;
import de.uni_koblenz.jgralab.greql2.evaluator.costmodel.GraphSize;
import de.uni_koblenz.jgralab.greql2.evaluator.costmodel.VertexCosts;
import de.uni_koblenz.jgralab.greql2.exception.EvaluateException;
import de.uni_koblenz.jgralab.greql2.jvalue.JValue;
import de.uni_koblenz.jgralab.greql2.jvalue.JValueImpl;
import de.uni_koblenz.jgralab.greql2.jvalue.JValueTable;
import de.uni_koblenz.jgralab.greql2.jvalue.JValueTuple;
import de.uni_koblenz.jgralab.greql2.schema.Declaration;
import de.uni_koblenz.jgralab.greql2.schema.Expression;
import de.uni_koblenz.jgralab.greql2.schema.Greql2Vertex;
import de.uni_koblenz.jgralab.greql2.schema.IsTableHeaderOf;
import de.uni_koblenz.jgralab.greql2.schema.TableComprehension;

/**
 * Evaluates a TableComprehensionvertex in the GReQL-2 Syntaxgraph. A
 * TableComprehension vertex is constructed using the notation reportTable
 * columHeader, rowHeader, cellContent
 * 
 * @author ist@uni-koblenz.de
 * 
 */
public class TableComprehensionEvaluator extends VertexEvaluator {

	/**
	 * The TableComprehension-Vertex this evaluator evaluates
	 */
	private TableComprehension vertex;

	private VariableDeclarationLayer declarationLayer;

	private VertexEvaluator columnHeaderEval = null;

	private VertexEvaluator rowHeaderEval = null;

	private VertexEvaluator resultDefEval = null;

	private boolean initialized = false;

	private void initialize() {
		Declaration d = (Declaration) vertex.getFirstIsCompDeclOf(
				EdgeDirection.IN).getAlpha();
		DeclarationEvaluator declEval = (DeclarationEvaluator) greqlEvaluator
				.getVertexEvaluatorGraphMarker().getMark(d);
		declarationLayer = (VariableDeclarationLayer) declEval.getResult(
				subgraph).toObject();

		Expression columnHeader = (Expression) vertex
				.getFirstIsColumnHeaderExprOf(EdgeDirection.IN).getAlpha();
		columnHeaderEval = greqlEvaluator.getVertexEvaluatorGraphMarker()
				.getMark(columnHeader);
		Expression rowHeader = (Expression) vertex.getFirstIsRowHeaderExprOf(
				EdgeDirection.IN).getAlpha();
		rowHeaderEval = greqlEvaluator.getVertexEvaluatorGraphMarker().getMark(
				rowHeader);
		Expression resultDef = (Expression) vertex.getFirstIsCompResultDefOf(
				EdgeDirection.IN).getAlpha();
		resultDefEval = greqlEvaluator.getVertexEvaluatorGraphMarker().getMark(
				resultDef);
		initialized = true;
	}

	/**
	 * returns the vertex this VertexEvaluator evaluates
	 */
	@Override
	public Greql2Vertex getVertex() {
		return vertex;
	}

	/**
	 * Creates a new TableComprehensionEvaluator for the given vertex
	 * 
	 * @param eval
	 *            the GreqlEvaluator instance this VertexEvaluator belong to
	 * @param vertex
	 *            the vertex this VertexEvaluator evaluates
	 */
	public TableComprehensionEvaluator(TableComprehension vertex,
			GreqlEvaluator eval) {
		super(eval);
		this.vertex = vertex;
	}

	@Override
	public JValue evaluate() throws EvaluateException {
		if (!initialized) {
			initialize();
		}
		TreeMap<JValue, HashMap<JValue, JValue>> tableMap = new TreeMap<JValue, HashMap<JValue, JValue>>();
		Set<JValue> completeColumnHeaderTuple = new HashSet<JValue>();
		TreeSet<JValue> rowHeaderSet = new TreeSet<JValue>();

		int noOfVarCombinations = 0;
		declarationLayer.reset();
		while (declarationLayer.iterate(subgraph)) {
			noOfVarCombinations++;
			JValue columnHeaderEntry = columnHeaderEval.getResult(subgraph);
			completeColumnHeaderTuple.add(columnHeaderEntry);
			JValue rowHeaderEntry = rowHeaderEval.getResult(subgraph);
			JValue localResult = resultDefEval.getResult(subgraph);
			HashMap<JValue, JValue> row = tableMap.get(rowHeaderEntry);
			if (row == null) {
				row = new HashMap<JValue, JValue>();
				tableMap.put(rowHeaderEntry, row);
				rowHeaderSet.add(rowHeaderEntry);
				// GreqlEvaluator.println("Adding row");
			}
			row.put(columnHeaderEntry, localResult);
		}

		JValueTable resultTable = new JValueTable();
		JValueTuple headerTuple = resultTable.getHeader();
		TreeSet<JValue> completeColumnHeaderTreeSet = new TreeSet<JValue>();
		for (JValue jValueImpl : completeColumnHeaderTuple) {
			completeColumnHeaderTreeSet.add(jValueImpl);
		}
		Iterator<JValue> colIter = completeColumnHeaderTreeSet.iterator();
		IsTableHeaderOf tHeader = vertex
				.getFirstIsTableHeaderOf(EdgeDirection.IN);
		if (tHeader != null) {
			VertexEvaluator theval = greqlEvaluator
					.getVertexEvaluatorGraphMarker()
					.getMark(tHeader.getAlpha());
			headerTuple.add(theval.getResult(subgraph));
		} else {
			headerTuple.add(new JValueImpl("")); // dummy entry in the upper
			// left
			// corner
		}
		while (colIter.hasNext()) {
			headerTuple.add(colIter.next());
		}
		Iterator<Entry<JValue, HashMap<JValue, JValue>>> rowIter = tableMap
				.entrySet().iterator();
		while (rowIter.hasNext()) {
			Entry<JValue, HashMap<JValue, JValue>> currentEntry = rowIter
					.next();
			JValue currentRowHeader = currentEntry.getKey();
			HashMap<JValue, JValue> currentRow = currentEntry.getValue();
			colIter = completeColumnHeaderTreeSet.iterator();
			JValueTuple rowTuple = new JValueTuple(completeColumnHeaderTuple
					.size());
			rowTuple.add(currentRowHeader);
			while (colIter.hasNext()) {
				JValue cellEntry = currentRow.get(colIter.next());
				if (cellEntry == null) {
					cellEntry = new JValueImpl();
				}
				rowTuple.add(cellEntry);
			}
			resultTable.add(rowTuple);
		}
		return resultTable;
	}

	@Override
	public VertexCosts calculateSubtreeEvaluationCosts(GraphSize graphSize) {
		return this.greqlEvaluator.getCostModel()
				.calculateCostsTableComprehension(this, graphSize);
	}

	@Override
	public long calculateEstimatedCardinality(GraphSize graphSize) {
		return greqlEvaluator.getCostModel()
				.calculateCardinalityTableComprehension(this, graphSize);
	}

}
