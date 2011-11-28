package de.uni_koblenz.jgralab.schema.impl.compilation;

import java.io.IOException;
import java.util.Set;
import java.util.logging.Logger;

import javax.tools.FileObject;
import javax.tools.ForwardingJavaFileManager;
import javax.tools.JavaFileManager;
import javax.tools.JavaFileObject;
import javax.tools.JavaFileObject.Kind;

import de.uni_koblenz.jgralab.EclipseAdapter;
import de.uni_koblenz.jgralab.JGraLab;
import de.uni_koblenz.jgralab.schema.impl.SchemaImpl;

/**
 * File Manager class overwriting the method {@code getJavaFileForOutput} so
 * that bytecode is written to a {@code ClassFileAbstraction}.
 * 
 */
public class ClassFileManager extends
		ForwardingJavaFileManager<JavaFileManager> {

	private Logger logger;

	private final SchemaImpl schemaImpl;

	public ClassFileManager(SchemaImpl schemaImpl, JavaFileManager fm) {
		super(fm);
		// logger = JGraLab.getLogger(getClass().getPackage().getName());
		this.schemaImpl = schemaImpl;
	}

	@Override
	public String inferBinaryName(Location location, JavaFileObject file) {
		if (logger != null && location.getName().equals("CLASS_PATH")) {
			logger.fine("(" + location + ", " + file + ")");
		}
		if (location.getName().equals("CLASS_PATH")
				&& file instanceof ClassFileObject) {
			return ((ClassFileObject) file).getBinaryName();
		}
		return super.inferBinaryName(location, file);
	}

	@Override
	public JavaFileObject getJavaFileForOutput(Location location,
			String className, Kind kind, FileObject sibling) {
		if (logger != null) {
			logger.fine("(" + location + ", " + className + ", " + kind + ", "
					+ sibling + ")");
		}
		InMemoryClassFile cfa = new InMemoryClassFile(className);
		M1ClassManager.instance(schemaImpl.getQualifiedName()).putM1Class(
				className, cfa);
		return cfa;
	}

	@Override
	public Iterable<JavaFileObject> list(Location location, String packageName,
			Set<Kind> kinds, boolean recurse) throws IOException {
		if (logger != null) {
			logger.fine("(" + location + ", " + packageName + ", " + kinds
					+ ", " + recurse + ")");
		}

		EclipseAdapter ea = JGraLab.getEclipseAdapter();
		if (ea == null
				|| !((location.getName().equals("CLASS_PATH") && kinds
						.contains(Kind.CLASS)))) {
			return super.list(location, packageName, kinds, recurse);
		}

		Iterable<JavaFileObject> list = ea.listJavaFileObjects(packageName,
				recurse);
		for (JavaFileObject jfo : list) {
			System.err.println("\t" + jfo.getName());
		}

		return list;
	}
}