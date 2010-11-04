package de.uni_koblenz.jgralab;

import java.net.URL;

/**
 * Simple interface for loading GReQL functions from Eclipse resource bundles.
 * There's a class in the jgralab4eclipse project that implements that
 * interface. The field eclipseFunctionLoader is then set to an instance of
 * that.
 */
public interface EclipseAdapter {
	public void registerFunctionsInResourceBundle(URL res);

	public String getJGraLabClasspath();
}