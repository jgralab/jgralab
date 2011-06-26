package de.uni_koblenz.jgralab.eca;


import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import de.uni_koblenz.jgralab.Graph;
import de.uni_koblenz.jgralab.eca.events.Event;
import de.uni_koblenz.jgralab.gretl.Context;
import de.uni_koblenz.jgralab.gretl.Transformation;

public class GretlTransformAction implements Action {

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
	public GretlTransformAction(// ) {
			Class<? extends Transformation<Graph>> transformationClass) {
		this.transformationClass = transformationClass;
	}

	// +++++++++++++++++++++++++++++++++++++++++++++++++

	/**
	 * Executes the action
	 */
	@Override
	public void doAction(Event event) {

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

}
