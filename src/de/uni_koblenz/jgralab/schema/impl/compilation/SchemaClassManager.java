/*
 * JGraLab - The Java Graph Laboratory
 *
 * Copyright (C) 2006-2013 Institute for Software Technology
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

import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.Map;

import de.uni_koblenz.jgralab.schema.Schema;

/**
 * A {@code SchemaClassManager} holds the bytecode of the generated schema
 * classes of one {@link Schema} in a Map {@link #schemaClassFiles}.
 * 
 * As a specialization of {@link ClassLoader}, it overwrites the
 * {@link #findClass(String)} method such that {@link #schemaClassFiles} is
 * first searched for classes' bytecode before invoking {@code findClass} of the
 * original {@code ClassLoader}.
 * 
 * Once the class is loaded, the byte code is discarded.
 * 
 * @author ist@uni-koblenz.de
 */
public class SchemaClassManager extends ClassLoader {
	private static HashMap<String, WeakReference<SchemaClassManager>> instances = new HashMap<String, WeakReference<SchemaClassManager>>();

	private final Map<String, InMemoryClassFile> schemaClassFiles;
	private String schemaQName = null;
	private final ClassLoader parentClassLoader;

	public static SchemaClassManager instance(ClassLoader parent, String qualifiedName) {
		// Singleton implementation using weak references
		WeakReference<SchemaClassManager> ref = instances.get(qualifiedName);
		SchemaClassManager result = null;
		if (ref != null) {
			result = ref.get();
		}
		if (result == null) {
			synchronized (SchemaClassManager.class) {
				ref = instances.get(qualifiedName);
				if (ref != null) {
					result = ref.get();
				}
				if (result == null) {
					result = new SchemaClassManager(parent, qualifiedName);
					instances.put(qualifiedName,
							new WeakReference<SchemaClassManager>(result));
				}
			}
		}

		return result;
	}

    public static SchemaClassManager instance(String qualifiedName) {
        return instance(null, qualifiedName);
    }

	private SchemaClassManager(ClassLoader parent, String schemaQName) {
		parentClassLoader = parent;
		this.schemaQName = schemaQName;
		schemaClassFiles = new HashMap<String, InMemoryClassFile>();
	}

	public void putSchemaClass(String className, InMemoryClassFile cfa) {
		schemaClassFiles.put(className, cfa);
	}

	@Override
	public Class<?> findClass(String name) throws ClassNotFoundException {
		// first, look if the class byte code is in the internal map
		InMemoryClassFile inMemClassFile = schemaClassFiles.get(name);
		if (inMemClassFile != null) {
			// if found, load the byte code
			byte[] bytes = inMemClassFile.getBytecode();
			Class<?> clazz = defineClass(name, bytes, 0, bytes.length);
			// once the class is defined, we can forget its bytecode!
			schemaClassFiles.remove(name);
			return clazz;
		}

		if(parentClassLoader != null) {
			return parentClassLoader.loadClass(name);
		}

		// if not defined internally, use the standard class loader mechanisms
		return Class.forName(name);
	}

	@Override
	public String toString() {
		return "SchemaClassManager for schema '" + schemaQName + "'";
	}
}
