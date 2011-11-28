/*
 * JGraLab - The Java Graph Laboratory
 * 
 * Copyright (C) 2006-2010 Institute for Software Technology
 *                         University of Koblenz-Landau, Germany
 *                         ist@uni-koblenz.de
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

package de.uni_koblenz.jgralab.plugin;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;

import javax.tools.JavaFileObject;
import javax.tools.JavaFileObject.Kind;

import org.eclipse.core.runtime.FileLocator;

import de.uni_koblenz.jgralab.EclipseAdapter;
import de.uni_koblenz.jgralab.schema.impl.compilation.ClassFileObject;

/**
 * @author Tassilo Horn &lt;horn@uni-koblenz.de&gt;
 * 
 */
public class EclipseAdapterImpl implements EclipseAdapter {
	private static final String RESOURCE_TO_LOCATE = "de/uni_koblenz/jgralab";

	@Override
	public String getJGraLabJarPath() {
		return "";
		// URL jgURL = Activator.getContext().getBundle()
		// .getResource(RESOURCE_TO_LOCATE);
		// if (jgURL != null) {
		// try {
		// URL fileURL = FileLocator.toFileURL(jgURL);
		// URI uri = new URI(fileURL.toString().replace(" ", "%20"));
		// File jgJar = new File(uri);
		// String p = jgJar.getCanonicalPath();
		// if (p.endsWith(RESOURCE_TO_LOCATE)) {
		// p = p.substring(0, p.length() - RESOURCE_TO_LOCATE.length());
		// }
		// System.err.println(">>> URI: " + uri);
		// System.err.println(">>> getJGraLabJarPath: " + p);
		// if (jgJar.isDirectory()) {
		// String[] entries = jgJar.list();
		// for (String s : entries) {
		// System.err.println("\t" + s);
		// }
		// }
		// return p;
		// } catch (IOException e) {
		// e.printStackTrace();
		// } catch (URISyntaxException e) {
		// e.printStackTrace();
		// }
		// }
		// throw new RuntimeException(
		// "Couldn't figure out the path to jgralab.jar.");
	}

	@Override
	public Iterable<JavaFileObject> listJavaFileObjects(String packageName,
			boolean recurse) {
		if (recurse) {
			throw new UnsupportedOperationException("Can not recurse :-(");
		}
		String directoryName = packageName.replace('.', '/');
		URL url = Activator.getContext().getBundle().getResource(directoryName);
		ArrayList<JavaFileObject> list = new ArrayList<JavaFileObject>();
		if (url == null) {
			return list;
		}
		try {
			URL fileURL = FileLocator.toFileURL(url);
			URI fileURI = new URI(fileURL.toString().replace(" ", "%20"));
			File dir = new File(fileURI);
			for (File f : dir.listFiles()) {
				if (f.isFile() && f.getName().endsWith(".class")) {
					URI u = new URI(directoryName + "/" + f.getName());
					JavaFileObject jfo = new BundleJavaFileObject(f, u,
							Kind.CLASS) {
					};
					list.add(jfo);
				}
			}
			return list;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (URISyntaxException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return list;
	}

	private static class BundleJavaFileObject extends ClassFileObject {
		public BundleJavaFileObject(File f, URI uri, Kind kind) {
			super(f, uri, kind);
		}

		@Override
		public InputStream openInputStream() throws IOException {
			return new FileInputStream(file);
		}
	}
}
