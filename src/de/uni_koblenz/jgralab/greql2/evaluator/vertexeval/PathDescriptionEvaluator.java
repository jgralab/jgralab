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

import de.uni_koblenz.jgralab.BooleanGraphMarker;
import de.uni_koblenz.jgralab.EdgeDirection;
import de.uni_koblenz.jgralab.greql2.evaluator.GreqlEvaluator;
import de.uni_koblenz.jgralab.greql2.evaluator.fa.NFA;
import de.uni_koblenz.jgralab.greql2.exception.EvaluateException;
import de.uni_koblenz.jgralab.greql2.jvalue.JValue;
import de.uni_koblenz.jgralab.greql2.jvalue.JValueInvalidTypeException;
import de.uni_koblenz.jgralab.greql2.jvalue.JValueTypeCollection;
import de.uni_koblenz.jgralab.greql2.schema.IsGoalRestrOf;
import de.uni_koblenz.jgralab.greql2.schema.IsStartRestrOf;
import de.uni_koblenz.jgralab.greql2.schema.PathDescription;

/**
 * This is the base class for all path descriptions. It provides methods to add
 * start- and goalrestrictions to the pathdescription. The subclasses like
 * AlternativePathDescriptionEvaluator etc. don't need to care about this,
 * because the method PathDescriptionEvaluator.getResult(...) automaticly adds
 * start- and goalrestrictions to the pathdescription, if a start or
 * goalrestriction exists.
 * 
 * @author Daniel Bildhauer <dbildh@uni-koblenz.de> Summer 2006, Diploma Thesis
 * 
 */
public abstract class PathDescriptionEvaluator extends VertexEvaluator {

	/**
	 * The NFA which is created out of this PathDescription
	 */
	protected NFA createdNFA;

	/**
	 * Creates a new PathDescriptionEvaluator
	 * @param eval
	 */
	public PathDescriptionEvaluator(GreqlEvaluator eval) {
		super(eval);
	}

	/**
	 * returns the nfa
	 */
	public NFA getNFA() throws EvaluateException {
		if (createdNFA == null)
			getResult(null);
		return createdNFA;
	}

	/**
	 * Returns the created NFA, encapsulated in a JValue The NFA for the path
	 * description doesn't depend on the subgraph, so the getResult-Methode is
	 * overwritten
	 * 
	 * @return the result as jvalue
	 */
	@Override
	public JValue getResult(BooleanGraphMarker subgraph)
			throws EvaluateException {
		if (createdNFA == null) {
		//	long ms = System.currentTimeMillis();
			result = evaluate();
			try {
				createdNFA = result.toNFA();
				addGoalRestrictions();
				addStartRestrictions();
		//		GreqlEvaluator.println("    Time for creating NFA: " + (System.currentTimeMillis() - ms));
			} catch (JValueInvalidTypeException ex) {
				throw new EvaluateException("Error creating a Path NFA", ex);
			}
		}
		return result;
	}

	/**
	 * creates the lists of goal type restrictions from all TypeId-Vertices that
	 * belong to this path descritpion and adds the transitions that accepts
	 * them to the nfa
	 */
	protected void addGoalRestrictions() throws EvaluateException {
		PathDescription pathDesc = (PathDescription) getVertex();
		VertexEvaluator goalRestEval = null;
		IsGoalRestrOf inc = pathDesc.getFirstIsGoalRestrOf(EdgeDirection.IN);
		if (inc == null)
			return;
		JValueTypeCollection typeCollection = new JValueTypeCollection();
		while (inc != null) {
			VertexEvaluator vertexEval = greqlEvaluator.getVertexEvaluatorGraphMarker().getMark(inc.getAlpha());
			if (vertexEval instanceof TypeIdEvaluator) {
				TypeIdEvaluator typeEval = (TypeIdEvaluator) vertexEval;
				try { 
					typeCollection.addTypes(typeEval.getResult(null).toJValueTypeCollection());
				} catch (JValueInvalidTypeException ex) {
					throw new EvaluateException("Result of TypeId is not JValueTypeCollection", ex);
				}
			} else {
				goalRestEval = vertexEval;
			}
			inc = inc.getNextIsGoalRestrOf(EdgeDirection.IN);
		}		
		NFA.addGoalTypeRestriction(getNFA(), typeCollection);
		if (goalRestEval != null)
			NFA.addGoalBooleanRestriction(getNFA(), goalRestEval, greqlEvaluator.getVertexEvaluatorGraphMarker());
	}

	/**
	 * creates the lists of start and goal type restrictions from all
	 * TypeId-Vertices that belong to this path descritpion
	 * 
	 * @return the generated list of types
	 */
	protected void addStartRestrictions() throws EvaluateException {
		PathDescription pathDesc = (PathDescription) getVertex();
		VertexEvaluator startRestEval = null;
		IsStartRestrOf inc = pathDesc.getFirstIsStartRestrOf(EdgeDirection.IN);
		if (inc == null)
			return;
		JValueTypeCollection typeCollection = new JValueTypeCollection();
		while (inc != null) {
			VertexEvaluator vertexEval = greqlEvaluator.getVertexEvaluatorGraphMarker().getMark(inc.getAlpha());
			if (vertexEval instanceof TypeIdEvaluator) {
				TypeIdEvaluator typeEval = (TypeIdEvaluator) vertexEval;
				try { 
					typeCollection.addTypes(typeEval.getResult(null).toJValueTypeCollection());
				} catch (JValueInvalidTypeException ex) {
					throw new EvaluateException("Result of TypeId is not JValueTypeCollection", ex);
				}
			} else {
				startRestEval = vertexEval;
			}
			inc = inc.getNextIsStartRestrOf(EdgeDirection.IN);
		}		
		NFA.addStartTypeRestriction(getNFA(), typeCollection);
		if (startRestEval != null)
			NFA.addStartBooleanRestriction(getNFA(), startRestEval, greqlEvaluator.getVertexEvaluatorGraphMarker());
	}

}
