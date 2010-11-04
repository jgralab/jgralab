package de.uni_koblenz.jgralab;

import java.net.URL;

/**
 * Simple interface for loading GReQL functions from Eclipse resource bundles.
 * There's a class in the jgralab4eclipse project that implements that
 * interface. The field eclipseFunctionLoader is then set to an instance of
 * that.
 */
public interface EclipseAdapter {
	/**
	 * Load all GReQL functions in the given resource bundle url.
	 * 
	 * @param res
	 *            the eclipse resoure bundle url
	 */
	public void registerFunctionsInResourceBundle(URL res);

	/**
	 * @return the path to jgralab.jar in the eclipse metadata workspace dir.
	 */
	public String getJGraLabJarPath();
}