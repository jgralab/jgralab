/*
 * JGraLab - The Java graph laboratory
 * (c) 2006-2009 Institute for Software Technology
 *               University of Koblenz-Landau, Germany
 *
 *               ist@uni-koblenz.de
 *
 * Please report bugs to http://serres.uni-koblenz.de/bugzilla
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */

package de.uni_koblenz.jgralab.impl;

import java.lang.reflect.Constructor;
import java.util.HashMap;

import de.uni_koblenz.jgralab.Edge;
import de.uni_koblenz.jgralab.Graph;
import de.uni_koblenz.jgralab.GraphFactory;
import de.uni_koblenz.jgralab.Vertex;
import de.uni_koblenz.jgralab.schema.exception.M1ClassAccessException;

/**
 * This class provides a default implementation for the GraphFactory. Per
 * default, every create-method creates an instance of exactly the specified
 * class. To change this, the <code>setImplementationClass</code>-methods can be
 * used. The class is abstract because only the factories which are specific for
 * a schema should be used.
 *
 * @author ist@uni-koblenz.de
 */
public abstract class GraphFactoryImpl implements GraphFactory {

	protected HashMap<Class<? extends Graph>, Constructor<? extends Graph>> graphMap;

	protected HashMap<Class<? extends Edge>, Constructor<? extends Edge>> edgeMap;

	protected HashMap<Class<? extends Vertex>, Constructor<? extends Vertex>> vertexMap;

	public GraphFactoryImpl() {
		graphMap = new HashMap<Class<? extends Graph>, Constructor<? extends Graph>>();
		vertexMap = new HashMap<Class<? extends Vertex>, Constructor<? extends Vertex>>();
		edgeMap = new HashMap<Class<? extends Edge>, Constructor<? extends Edge>>();
	}

	public Edge createEdge(Class<? extends Edge> edgeClass, int id, Graph g) {
		try {
			return edgeMap.get(edgeClass).newInstance(id, g);
		} catch (Exception ex) {
			throw new M1ClassAccessException("Cannot create edge of class "
					+ edgeClass.getCanonicalName(), ex);
		}
	}

	public Graph createGraph(Class<? extends Graph> graphClass, String id,
			int vMax, int eMax) {
		try {
			return graphMap.get(graphClass).newInstance(id, vMax, eMax);
		} catch (Exception ex) {
			throw new M1ClassAccessException("Cannot create graph of class "
					+ graphClass.getCanonicalName(), ex);
		}
	}

	public Vertex createVertex(Class<? extends Vertex> vertexClass, int id,
			Graph g) {
		try {
			return vertexMap.get(vertexClass).newInstance(id, g);
		} catch (Exception ex) {
			throw new M1ClassAccessException("Cannot create vertex of class "
					+ vertexClass.getCanonicalName(), ex);
		}
	}

	@SuppressWarnings("unchecked")
	public void setGraphImplementationClass(
			Class<? extends Graph> originalClass,
			Class<? extends Graph> implementationClass) {
		if (isSuperclassOrEqual(originalClass, implementationClass)) {
			try {
				Class[] params = { String.class, int.class, int.class };
				graphMap.put(originalClass, implementationClass
						.getConstructor(params));
			} catch (NoSuchMethodException ex) {
				throw new M1ClassAccessException(
						"Unable to locate default constructor for graphclass "
								+ implementationClass.getName(), ex);
			}
		}
	}

	@SuppressWarnings("unchecked")
	public void setVertexImplementationClass(
			Class<? extends Vertex> originalClass,
			Class<? extends Vertex> implementationClass) {
		if (isSuperclassOrEqual(originalClass, implementationClass)) {
			try {
				Class[] params = { int.class, Graph.class };
				vertexMap.put(originalClass, implementationClass
						.getConstructor(params));
			} catch (NoSuchMethodException ex) {
				throw new M1ClassAccessException(
						"Unable to locate default constructor for vertexclass"
								+ implementationClass, ex);
			}
		}
	}

	@SuppressWarnings("unchecked")
	public void setEdgeImplementationClass(Class<? extends Edge> originalClass,
			Class<? extends Edge> implementationClass) {
		if (isSuperclassOrEqual(originalClass, implementationClass)) {
			try {
				Class[] params = { int.class, Graph.class };
				edgeMap.put(originalClass, implementationClass
						.getConstructor(params));
			} catch (NoSuchMethodException ex) {
				throw new M1ClassAccessException(
						"Unable to locate default constructor for edgeclass"
								+ implementationClass, ex);
			}
		}
	}

	/**
	 * tests if a is a superclass of b or the same class than b
	 *
	 * @param a
	 * @param b
	 * @return
	 */
	@SuppressWarnings("unchecked")
	protected boolean isSuperclassOrEqual(Class a, Class b) {
		if (a == b) {
			return true;
		}
		if (implementsInterface(b, a)) {
			return true;
		}
		while (b.getSuperclass() != null) {
			if (b.getSuperclass() == a) {
				return true;
			}
			if (implementsInterface(b, a)) {
				return true;
			}
			b = b.getSuperclass();
		}
		return false;
	}

	/**
	 * tests if class a implements the interface b
	 *
	 * @param a
	 * @param b
	 * @return
	 */
	@SuppressWarnings("unchecked")
	protected boolean implementsInterface(Class a, Class b) {
		Class[] list = a.getInterfaces();
		for (Class c : list) {
			if (c == b) {
				return true;
			}
		}
		return false;
	}

}
