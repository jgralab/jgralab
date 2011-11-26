package de.uni_koblenz.jgralab.schema.impl.compilation;

import java.io.IOException;

import javax.tools.FileObject;
import javax.tools.ForwardingJavaFileManager;
import javax.tools.JavaFileManager;
import javax.tools.JavaFileObject;
import javax.tools.JavaFileObject.Kind;

import de.uni_koblenz.jgralab.schema.impl.SchemaImpl;

/**
 * File Manager class overwriting the method {@code getJavaFileForOutput} so
 * that bytecode is written to a {@code ClassFileAbstraction}.
 * 
 */
public class ClassFileManager extends
		ForwardingJavaFileManager<JavaFileManager> {

	/**
	 * 
	 */
	private final SchemaImpl schemaImpl;

	public ClassFileManager(SchemaImpl schemaImpl, JavaFileManager fm) {
		super(fm);
		this.schemaImpl = schemaImpl;
	}

	@Override
	public JavaFileObject getJavaFileForInput(Location location,
			String className, Kind kind) throws IOException {
		// TODO Auto-generated method stub
		System.err.println(">>> getJavaFileForInput(" + location + ", "
				+ className + ", " + kind + ")");
		return super.getJavaFileForInput(location, className, kind);
	}

	@Override
	public FileObject getFileForInput(Location location, String packageName,
			String relativeName) throws IOException {
		System.err.println(">>> getFileForInput(" + location + ", "
				+ packageName + ", " + relativeName + ")");
		// TODO Auto-generated method stub
		return super.getFileForInput(location, packageName, relativeName);
	}

	@Override
	public ClassLoader getClassLoader(Location location) {
		System.err.println(">>> getClassLoader(" + location + ")");
		return super.getClassLoader(location);
	}

	@Override
	public JavaFileObject getJavaFileForOutput(Location location,
			String className, Kind kind, FileObject sibling) {
		System.err.println(">>> getJavaFileForOutput(" + location + ", "
				+ className + ", " + kind + ", " + sibling + ")");
		ClassFileAbstraction cfa = new ClassFileAbstraction(className);
		M1ClassManager.instance(schemaImpl.getQualifiedName()).putM1Class(
				className, cfa);
		return cfa;
	}
}