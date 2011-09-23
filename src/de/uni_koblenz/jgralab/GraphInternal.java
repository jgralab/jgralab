package de.uni_koblenz.jgralab;

public interface GraphInternal {

	/**
	 * Changes this graph's version. graphModified() is called whenever the
	 * graph is changed, all changes like adding, creating and reordering of
	 * edges and vertices or changes of attributes of the graph, an edge or a
	 * vertex are treated as a change.
	 */
	public abstract void graphModified();

	/**
	 * Triggers ECA-rules before an Attribute is changed
	 * 
	 * @param name
	 *            of the changing Attribute
	 */
	public abstract void ecaAttributeChanging(String name, Object oldValue,
			Object newValue);

	/**
	 * Triggers ECA-rule after an Attribute is changed
	 * 
	 * @param name
	 *            of the changed Attribute
	 */
	public abstract void ecaAttributeChanged(String name, Object oldValue,
			Object newValue);

	/**
	 * Constructs incidence lists for all vertices after loading this graph.
	 * 
	 * @param firstIncidence
	 *            array of edge ids of the first incidence
	 * @param nextIncidence
	 *            array of edge ids of subsequent edges
	 */
	public abstract void internalLoadingCompleted(int[] firstIncidence,
			int[] nextIncidence);

	/**
	 * Sets the version counter of this graph. Should only be called immediately
	 * after loading.
	 * 
	 * @param graphVersion
	 *            new version value
	 */
	public abstract void setGraphVersion(long graphVersion);

	/**
	 * Sets the loading flag.
	 * 
	 * @param isLoading
	 */
	public abstract void setLoading(boolean isLoading);

}