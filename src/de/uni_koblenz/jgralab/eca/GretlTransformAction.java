package de.uni_koblenz.jgralab.eca;


// import de.uni_koblenz.jgralab.gretl.Context;
// import de.uni_koblenz.jgralab.gretl.Transformation;

public class GretlTransformAction extends Action {

	/**
	 * Class of Transformation
	 */
	//Class<? extends Transformation<Graph>> transformationClass;
	
	/**
	 * Creates a new GretlTransformAction with the given Transformation Class
	 * 
	 * @param t
	 *            the Transformation Class
	 */
	public GretlTransformAction(/* Class<? extends Transformation<Graph>> t */) {
		/* this.transformationClass = t; */
	}


	/**
	 * Executes the action
	 */
	@Override
	public void doAction(){
		/*
		 * try { Graph graph = this.getRule().getECARuleManager().getGraph();
		 * 
		 * Context context = new Context("", ""); context.setSourceGraph(graph);
		 * 
		 * Constructor<? extends Transformation<Graph>> constr =
		 * transformationClass .getConstructor(Context.class);
		 * Transformation<Graph> transform = constr.newInstance(context);
		 * 
		 * transform.execute();
		 * 
		 * } catch (SecurityException e) {
		 * System.err.println("Gretl transformation failed!");
		 * e.printStackTrace(); } catch (NoSuchMethodException e) {
		 * System.err.println("Gretl transformation failed!");
		 * e.printStackTrace(); } catch (IllegalArgumentException e) {
		 * System.err.println("Gretl transformation failed!");
		 * e.printStackTrace(); } catch (InstantiationException e) {
		 * System.err.println("Gretl transformation failed!");
		 * e.printStackTrace(); } catch (IllegalAccessException e) {
		 * System.err.println("Gretl transformation failed!");
		 * e.printStackTrace(); } catch (InvocationTargetException e) {
		 * System.err.println("Gretl transformation failed!");
		 * e.printStackTrace(); }
		 */
	}

}
