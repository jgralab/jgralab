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

package de.uni_koblenz.jgralab;

import java.lang.ref.WeakReference;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.HashMap;
import java.util.Map;

import de.uni_koblenz.jgralab.codegenerator.ClassFileAbstraction;
import de.uni_koblenz.jgralab.schema.Schema;

/**
 * A {@code M1ClassManager} holds the bytecode of the M1 classes of one
 * {@link Schema} in a {@code Map}. As a specialization of {@code ClassLoader},
 * it overwrites the method {@code findClass} so that the {@code Map} is first
 * searched for classes' bytecode before invoking {@code findClass} of the
 * superclass {@code ClassLoader}.
 * 
 * @author ist@uni-koblenz.de
 */
public class M1ClassManager extends ClassLoader {
	private static HashMap<String, WeakReference<M1ClassManager>> instances = new HashMap<String, WeakReference<M1ClassManager>>();

	private Map<String, ClassFileAbstraction> m1Classes;
	private String schemaQName = null;

	public static M1ClassManager instance(final String qualifiedName) {
		WeakReference<M1ClassManager> ref = instances.get(qualifiedName);
		if ((ref != null) && (ref.get() != null)) {
			return ref.get();
		}
		synchronized (M1ClassManager.class) {
			ref = instances.get(qualifiedName);
			if ((ref != null) && (ref.get() != null)) {
				return ref.get();
			}
			ref = AccessController
					.doPrivileged(new PrivilegedAction<WeakReference<M1ClassManager>>() {
						public WeakReference<M1ClassManager> run() {
							return new WeakReference<M1ClassManager>(
									new M1ClassManager(qualifiedName));
						}
					});
			instances.put(qualifiedName, ref);
		}
		return ref.get();
	}

	private M1ClassManager(String schemaQName) {
		this.schemaQName = schemaQName;
		m1Classes = new HashMap<String, ClassFileAbstraction>();
	}

	public void putM1Class(String className, ClassFileAbstraction cfa) {
		m1Classes.put(className, cfa);
	}

	/**
	 * Tries to find a class in the internal {@code Map}
	 * 
	 * @param name
	 *            the name of the class to be found
	 */
	@Override
	protected Class<?> findClass(String name) throws ClassNotFoundException {
		ClassFileAbstraction cfa = m1Classes.get(name);
		if (cfa != null) {
			byte[] bytes = cfa.getBytecode();
			Class<?> clazz = defineClass(name, bytes, 0, bytes.length);
			// once the class is defined, we can forget it!
			m1Classes.remove(name);
			return clazz;
		}
		return Class.forName(name);
	}

	@Override
	public String toString() {
		return "M1ClassManager for schema '" + schemaQName + "'";
	}
}
