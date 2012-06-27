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
package de.uni_koblenz.jgralab.eca;

import de.uni_koblenz.jgralab.AttributedElement;
import de.uni_koblenz.jgralab.Graph;
import de.uni_koblenz.jgralab.eca.events.Event;
import de.uni_koblenz.jgralab.greql.GreqlEnvironment;
import de.uni_koblenz.jgralab.greql.Query;
import de.uni_koblenz.jgralab.greql.evaluator.GreqlEnvironmentAdapter;
import de.uni_koblenz.jgralab.greql.evaluator.QueryImpl;
import de.uni_koblenz.jgralab.schema.AttributedElementClass;

public class GreqlCondition<AEC extends AttributedElementClass<AEC, ?>>
		implements Condition<AEC> {
	/**
	 * Condition as GReQuL Query
	 */
	private final String conditionExpression;

	// +++++++++++++++++++++++++++++++++++++++++++++++++

	/**
	 * Creates a Condition with the given GReQuL Query as condition Expression
	 * 
	 * @param conditionExpression
	 *            condition as GReQuL Query
	 */
	public GreqlCondition(String conditionExpression) {
		this.conditionExpression = conditionExpression;
	}

	// +++++++++++++++++++++++++++++++++++++++++++++++++

	/**
	 * Evaluates the condition
	 * 
	 * @param event
	 *            an Event containing the element to check the condition for
	 * @return if the condition is evaluated to true
	 */
	@Override
	public boolean evaluate(Event<AEC> event) {
		AttributedElement<AEC, ?> element = event.getElement();
		Graph datagraph = ((ECARuleManager) event.getGraph()
				.getECARuleManager()).getGraph();
		Query query = null;
		GreqlEnvironment environment = new GreqlEnvironmentAdapter();
		if (conditionExpression.contains("context")) {
			query = new QueryImpl("using context: " + conditionExpression);
			environment.setVariable("context", element);
		} else {
			query = new QueryImpl(conditionExpression);
		}
		return (Boolean) query.evaluate(datagraph, environment);
	}

	// +++++++++++++++++++++++++++++++++++++++++++++++++

	/**
	 * @return the conditionExpression
	 */
	public String getConditionExpression() {
		return conditionExpression;
	}

	@Override
	public String toString() {
		return "Condition: " + conditionExpression;
	}

}
