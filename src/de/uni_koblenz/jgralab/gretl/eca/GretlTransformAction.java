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
package de.uni_koblenz.jgralab.gretl.eca;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import de.uni_koblenz.jgralab.Graph;
import de.uni_koblenz.jgralab.eca.Action;
import de.uni_koblenz.jgralab.eca.events.Event;
import de.uni_koblenz.jgralab.gretl.Context;
import de.uni_koblenz.jgralab.gretl.Transformation;
import de.uni_koblenz.jgralab.schema.AttributedElementClass;

public class GretlTransformAction<AEC extends AttributedElementClass<AEC, ?>>
		implements Action<AEC> {

	/**
	 * Class of Transformation
	 */
	Class<? extends Transformation<Graph>> transformationClass;

	// +++++++++++++++++++++++++++++++++++++++++++++++++

	/**
	 * Creates a new GretlTransformAction with the given Transformation Class
	 * 
	 * @param transformationClass
	 *            the Transformation Class
	 */
	public GretlTransformAction(
			Class<? extends Transformation<Graph>> transformationClass) {
		this.transformationClass = transformationClass;
	}

	// +++++++++++++++++++++++++++++++++++++++++++++++++

	/**
	 * Executes the action
	 */
	@Override
	public void doAction(Event<AEC> event) {

		try {
			Graph graph = event.getGraph();

			Context context = new Context(graph.getSchema());
			context.setSourceGraph(graph);
			context.setTargetGraph(graph);

			Constructor<? extends Transformation<Graph>> constr = transformationClass
					.getConstructor(Context.class);
			Transformation<Graph> transform = constr.newInstance(context);

			transform.execute();

		} catch (SecurityException e) {
			System.err.println("Gretl transformation "
					+ this.transformationClass.getName() + " failed!");
		} catch (NoSuchMethodException e) {
			System.err.println("Gretl transformation "
					+ this.transformationClass.getName() + " failed!");
		} catch (IllegalArgumentException e) {
			System.err.println("Gretl transformation "
					+ this.transformationClass.getName() + " failed!");
		} catch (InstantiationException e) {
			System.err.println("Gretl transformation "
					+ this.transformationClass.getName() + " failed!");
		} catch (IllegalAccessException e) {
			System.err.println("Gretl transformation "
					+ this.transformationClass.getName() + " failed!");
		} catch (InvocationTargetException e) {
			System.err.println("Gretl transformation "
					+ this.transformationClass.getName() + " failed!");
		}

	}

	/**
	 * @return the Gretl Transformation
	 */
	public Class<? extends Transformation<Graph>> getTransformationClass() {
		return transformationClass;
	}

}
