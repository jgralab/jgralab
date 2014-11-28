/*
 * JGraLab - The Java Graph Laboratory
 *
 * Copyright (C) 2006-2014 Institute for Software Technology
 *                         University of Koblenz-Landau, Germany
 *                         ist@uni-koblenz.de
 *
 * For bug reports, documentation and further information, visit
 *
 *                         https://github.com/jgralab/jgralab
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
