package de.uni_koblenz.jgralab.schema.impl.compilation;

import java.io.File;
import java.net.URI;

import javax.tools.SimpleJavaFileObject;

/**
 * Provides access to class files in an Eclipse (OSGI) bundle. This class is
 * only instantiated when JGraLab is used as Eclipse plugin. It serves here only
 * to prevent compile time dependencies to the Eclipse framework when compiling
 * a stand-alone JGraLab (i.e. NOT compiling the Eclipse plugin).
 * 
 * The real implementation is done in the inner class BundleClassFileObject in
 * de.uni_koblenz.jgralab.plugin.EclipseAdapterImpl.
 * 
 * @author ist@uni-koblenz.de
 */
public abstract class ClassFileObject extends SimpleJavaFileObject {
	protected File file;

	public ClassFileObject(File f, URI uri, Kind kind) {
		super(uri, kind);
		file = f;
	}

	public String getBinaryName() {
		String n = uri.toString();
		n = n.substring(0, n.length() - 6).replace('/', '.');
		return n;
	}
}
