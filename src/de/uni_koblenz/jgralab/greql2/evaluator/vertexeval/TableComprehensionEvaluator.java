/*
 * JGraLab - The Java graph laboratory
 * (c) 2006-2008 Institute for Software Technology
 *               University of Koblenz-Landau, Germany
 *
 *               ist@uni-koblenz.de
 *
 * Please report bugs to http://serres.uni-koblenz.de/bugzilla
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
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
import de.uni_koblenz.jgralab.Vertex;
import de.uni_koblenz.jgralab.greql2.evaluator.GreqlEvaluator;
import de.uni_koblenz.jgralab.greql2.evaluator.VariableDeclarationLayer;
import de.uni_koblenz.jgralab.greql2.evaluator.costmodel.GraphSize;
import de.uni_koblenz.jgralab.greql2.evaluator.costmodel.VertexCosts;
import de.uni_koblenz.jgralab.greql2.exception.EvaluateException;
import de.uni_koblenz.jgralab.greql2.exception.JValueInvalidTypeException;
import de.uni_koblenz.jgralab.greql2.jvalue.JValue;
import de.uni_koblenz.jgralab.greql2.jvalue.JValueTable;
import de.uni_koblenz.jgralab.greql2.jvalue.JValueTuple;
import de.uni_koblenz.jgralab.greql2.schema.Declaration;
import de.uni_koblenz.jgralab.greql2.schema.Expression;
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

	/**
	 * returns the vertex this VertexEvaluator evaluates
	 */
	public Vertex getVertex() {
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

	public JValue evaluate() throws EvaluateException {
		Declaration d = (Declaration) vertex.getFirstIsCompDeclOf(
				EdgeDirection.IN).getAlpha();
		DeclarationEvaluator declEval = (DeclarationEvaluator) greqlEvaluator
				.getVertexEvaluatorGraphMarker().getMark(d);
		VariableDeclarationLayer declLayer = null;
		try {
			declLayer = declEval.getResult(subgraph).toDeclarationLayer();
		} catch (JValueInvalidTypeException exception) {
			throw new EvaluateException("Error evaluating TableComprehension",
					exception);
		}

		Expression columnHeader = (Expression) vertex
				.getFirstIsColumnHeaderExprOf(EdgeDirection.IN).getAlpha();
		VertexEvaluator columnHeaderEval = greqlEvaluator
				.getVertexEvaluatorGraphMarker().getMark(columnHeader);
		Expression rowHeader = (Expression) vertex.getFirstIsRowHeaderExprOf(
				EdgeDirection.IN).getAlpha();
		VertexEvaluator rowHeaderEval = greqlEvaluator
				.getVertexEvaluatorGraphMarker().getMark(rowHeader);
		Expression resultDef = (Expression) vertex.getFirstIsCompResultDefOf(
				EdgeDirection.IN).getAlpha();
		VertexEvaluator resultDefEval = greqlEvaluator
				.getVertexEvaluatorGraphMarker().getMark(resultDef);
		TreeMap<JValue, HashMap<JValue, JValue>> tableMap = new TreeMap<JValue, HashMap<JValue, JValue>>();
		Set<JValue> completeColumnHeaderTuple = new HashSet<JValue>();
		TreeSet<JValue> rowHeaderSet = new TreeSet<JValue>();

		int noOfVarCombinations = 0;
		while (declLayer.iterate(subgraph)) {
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
		for (Iterator<JValue> iter = completeColumnHeaderTuple.iterator(); iter
				.hasNext();)
			completeColumnHeaderTreeSet.add(iter.next());
		Iterator<JValue> colIter = completeColumnHeaderTreeSet.iterator();
		IsTableHeaderOf tHeader = vertex
				.getFirstIsTableHeaderOf(EdgeDirection.IN);
		if (tHeader != null) {
			VertexEvaluator theval = greqlEvaluator
					.getVertexEvaluatorGraphMarker()
					.getMark(tHeader.getAlpha());
			headerTuple.add(theval.getResult(subgraph));
		} else {
			headerTuple.add(new JValue("")); // dummy entry in the upper left
			// corner
		}
		while (colIter.hasNext())
			headerTuple.add(colIter.next());
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
				if (cellEntry == null)
					cellEntry = new JValue();
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
