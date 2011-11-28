package de.uni_koblenz.jgralab.schema.impl.compilation;

import java.io.File;
import java.net.URI;

import javax.tools.SimpleJavaFileObject;

public class ClassFileObject extends SimpleJavaFileObject {
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
