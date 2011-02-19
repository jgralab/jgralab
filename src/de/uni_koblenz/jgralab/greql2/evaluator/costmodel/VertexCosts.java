/*
 * JGraLab - The Java Graph Laboratory
 * 
 * Copyright (C) 2006-2011 Institute for Software Technology
 *                         University of Koblenz-Landau, Germany
 *                         ist@uni-koblenz.de
 * 
 * For bug reports, documentation and further information, visit
 * 
 *                         http://jgralab.uni-koblenz.de
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

package de.uni_koblenz.jgralab.greql2.evaluator.costmodel;

/**
 * Modells the costs a evaluation of a subtree causes.
 * 
 * It's a 3-Tupol containing
 * 
 * <ul>
 * <li>the costs of evaluating this vertex itself once (ownEvaluationCosts),</li>
 * <li>the costs all evaluations of this vertex that are needed when evaluating
 * the current query and</li> *
 * <li>the costs of evaluating the whole subtree below this vertex plus
 * ownEvaluationCosts (subtreeEvaluationCosts).</li>
 * </ul>
 * 
 * @author ist@uni-koblenz.de
 * 
 */
public class VertexCosts {

	/**
	 * The evaluation costs of the whole subtree
	 */
	public long subtreeEvaluationCosts = 0;

	/**
	 * The costs <b>one</b> evaluation of the root-vertex causes
	 */
	public long ownEvaluationCosts = 0;

	/**
	 * The costs all evaluations of the root-vertex
	 */
	public long iteratedEvaluationCosts = 0;

	/**
	 * Creates a new VertexCosts object
	 * 
	 * @param own
	 *            the costs for <b>one</b> evaluation of the vertex this object
	 *            represents
	 * @param iterated
	 *            the costs for <b>all</b> evaluations of the vertex this object
	 *            represents
	 * @param subtree
	 *            the costs for the evaluation of the subtree with the vertex as
	 *            root
	 */
	public VertexCosts(long own, long iterated, long subtree) {
		ownEvaluationCosts = own;
		iteratedEvaluationCosts = iterated;
		subtreeEvaluationCosts = subtree;
	}

}
