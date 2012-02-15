/*
 * JGraLab - The Java Graph Laboratory
 * 
 * Copyright (C) 2006-2012 Institute for Software Technology
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

package de.uni_koblenz.jgralab.impl;

import java.lang.reflect.Constructor;
import java.util.HashMap;

import de.uni_koblenz.jgralab.Edge;
import de.uni_koblenz.jgralab.Graph;
import de.uni_koblenz.jgralab.GraphException;
import de.uni_koblenz.jgralab.GraphFactory;
import de.uni_koblenz.jgralab.Vertex;
import de.uni_koblenz.jgralab.impl.db.GraphDatabase;
import de.uni_koblenz.jgralab.schema.exception.SchemaClassAccessException;

/**
 * Default implementation for GraphFactory. Per default every create-method
 * creates an instance of exactly the specified class. To change this use
 * <code>setImplementationClass</code>-methods. Class is abstract because only
 * factories which are specific for their schema should be used.
 * 
 * @author ist@uni-koblenz.de
 */
public abstract class GraphFactoryImpl implements GraphFactory {

	// FIXME Remove redundancies! Why is this all in one class anyway? Because
	// it seems to be a factory pattern!?!

	// Maps for standard support.
	protected HashMap<Class<? extends Graph>, Constructor<? extends Graph>> graphMap;
	protected HashMap<Class<? extends Edge>, Constructor<? extends Edge>> edgeMap;
	protected HashMap<Class<? extends Vertex>, Constructor<? extends Vertex>> vertexMap;

	// Maps for database support.
	protected HashMap<Class<? extends Graph>, Constructor<? extends Graph>> graphDatabaseMap;
	protected HashMap<Class<? extends Edge>, Constructor<? extends Edge>> edgeDatabaseMap;
	protected HashMap<Class<? extends Vertex>, Constructor<? extends Vertex>> vertexDatabaseMap;

	// Maps for transaction support.
	protected HashMap<Class<? extends Graph>, Constructor<? extends Graph>> graphTransactionMap;
	protected HashMap<Class<? extends Edge>, Constructor<? extends Edge>> edgeTransactionMap;
	protected HashMap<Class<? extends Vertex>, Constructor<? extends Vertex>> vertexTransactionMap;

	/**
	 * Creates and initializes a new <code>GraphFactoryImpl</code>.
	 */
	protected GraphFactoryImpl() {
		createMapsForStandardSupport();
		createMapsForDatabaseSupport();
		createMapsForTransactionSupport();
	}

	private void createMapsForStandardSupport() {
		graphMap = new HashMap<Class<? extends Graph>, Constructor<? extends Graph>>();
		edgeMap = new HashMap<Class<? extends Edge>, Constructor<? extends Edge>>();
		vertexMap = new HashMap<Class<? extends Vertex>, Constructor<? extends Vertex>>();
	}

	private void createMapsForDatabaseSupport() {
		graphDatabaseMap = new HashMap<Class<? extends Graph>, Constructor<? extends Graph>>();
		edgeDatabaseMap = new HashMap<Class<? extends Edge>, Constructor<? extends Edge>>();
		vertexDatabaseMap = new HashMap<Class<? extends Vertex>, Constructor<? extends Vertex>>();
	}

	private void createMapsForTransactionSupport() {
		graphTransactionMap = new HashMap<Class<? extends Graph>, Constructor<? extends Graph>>();
		edgeTransactionMap = new HashMap<Class<? extends Edge>, Constructor<? extends Edge>>();
		vertexTransactionMap = new HashMap<Class<? extends Vertex>, Constructor<? extends Vertex>>();
	}

	// --- Methods for option STDIMPL
	// ---------------------------------------------------

	@Override
	public Edge createEdge(Class<? extends Edge> edgeClass, int id, Graph g,
			Vertex alpha, Vertex omega) {
		try {
			if (!((InternalGraph) g).isLoading()&& g.getECARuleManagerIfThere()!=null) {
				g.getECARuleManagerIfThere().fireBeforeCreateEdgeEvents(edgeClass);
			}
			Edge e = edgeMap.get(edgeClass).newInstance(id, g, alpha, omega);
			return e;
		} catch (Exception ex) {
			if (ex.getCause() instanceof GraphException) {
				throw new GraphException(ex.getCause().getLocalizedMessage(),
						ex);
			}
			throw new SchemaClassAccessException("Cannot create edge of class "
					+ edgeClass.getCanonicalName(), ex);
		}
	}

	@Override
	public Graph createGraph(Class<? extends Graph> graphClass, String id,
			int vMax, int eMax) {
		try {
			Graph g = graphMap.get(graphClass).newInstance(id, vMax, eMax);
			return g;
		} catch (Exception ex) {
			throw new SchemaClassAccessException("Cannot create graph of class "
					+ graphClass.getCanonicalName(), ex);
		}
	}

	@Override
	public Graph createGraph(Class<? extends Graph> graphClass, String id) {
		try {
			Graph g = graphMap.get(graphClass).newInstance(id, 1000, 1000);
			return g;
		} catch (Exception ex) {
			throw new SchemaClassAccessException("Cannot create graph of class "
					+ graphClass.getCanonicalName(), ex);
		}
	}

	@Override
	public Vertex createVertex(Class<? extends Vertex> vertexClass, int id,
			Graph g) {
		try {
			if (!((InternalGraph) g).isLoading()&& g.getECARuleManagerIfThere()!=null) {
				g.getECARuleManagerIfThere().fireBeforeCreateVertexEvents(vertexClass);
			}
			Vertex v = vertexMap.get(vertexClass).newInstance(id, g);
			return v;
		} catch (Exception ex) {
			if (ex.getCause() instanceof GraphException) {
				throw new GraphException(ex.getCause().getLocalizedMessage(),
						ex);
			}
			throw new SchemaClassAccessException("Cannot create vertex of class "
					+ vertexClass.getCanonicalName(), ex);
		}
	}

	@Override
	public void setGraphImplementationClass(
			Class<? extends Graph> originalClass,
			Class<? extends Graph> implementationClass) {
		if (isSuperclassOrEqual(originalClass, implementationClass)) {
			try {
				Class<?>[] params = { String.class, int.class, int.class };
				graphMap.put(originalClass, implementationClass
						.getConstructor(params));
			} catch (NoSuchMethodException ex) {
				throw new SchemaClassAccessException(
						"Unable to locate default constructor for graphclass "
								+ implementationClass.getName(), ex);
			}
		}
	}

	@Override
	public void setVertexImplementationClass(
			Class<? extends Vertex> originalClass,
			Class<? extends Vertex> implementationClass) {
		if (isSuperclassOrEqual(originalClass, implementationClass)) {
			try {
				Class<?>[] params = { int.class, Graph.class };
				vertexMap.put(originalClass, implementationClass
						.getConstructor(params));
			} catch (NoSuchMethodException ex) {
				throw new SchemaClassAccessException(
						"Unable to locate default constructor for vertexclass"
								+ implementationClass, ex);
			}
		}
	}

	@Override
	public void setEdgeImplementationClass(Class<? extends Edge> originalClass,
			Class<? extends Edge> implementationClass) {
		if (isSuperclassOrEqual(originalClass, implementationClass)) {
			try {
				Class<?>[] params = { int.class, Graph.class, Vertex.class,
						Vertex.class };
				edgeMap.put(originalClass, implementationClass
						.getConstructor(params));
			} catch (NoSuchMethodException ex) {
				throw new SchemaClassAccessException(
						"Unable to locate default constructor for edgeclass"
								+ implementationClass, ex);
			}
		}
	}

	// -------------------------------------------------------------------------
	// Methods for the TRANSIMPL option.
	// -------------------------------------------------------------------------

	@Override
	public Graph createGraphWithDatabaseSupport(
			Class<? extends Graph> graphClass, GraphDatabase graphDatabase,
			String id) {
		try {
			return graphDatabaseMap.get(graphClass).newInstance(id, 1000, 1000,
					graphDatabase);
		} catch (Exception exception) {
			throw new SchemaClassAccessException("Cannot create graph of class "
					+ graphClass.getCanonicalName(), exception);
		}
	}

	@Override
	public Graph createGraphWithDatabaseSupport(
			Class<? extends Graph> graphClass, GraphDatabase graphDatabase,
			String id, int vMax, int eMax) {
		try {
			return graphDatabaseMap.get(graphClass).newInstance(id, vMax, eMax,
					graphDatabase);
		} catch (Exception exception) {
			throw new SchemaClassAccessException("Cannot create graph of class "
					+ graphClass.getCanonicalName(), exception);
		}
	}

	@Override
	public Edge createEdgeWithDatabaseSupport(Class<? extends Edge> edgeClass,
			int id, Graph graph, Vertex alpha, Vertex omega) {
		try {
			return edgeDatabaseMap.get(edgeClass).newInstance(id, graph, alpha,
					omega);
		} catch (Exception exception) {
			if (exception.getCause() instanceof GraphException) {
				throw new GraphException(exception.getCause()
						.getLocalizedMessage());
			} else {
				throw new SchemaClassAccessException("Cannot create edge of class "
						+ edgeClass.getCanonicalName(), exception);
			}
		}
	}

	@Override
	public Vertex createVertexWithDatabaseSupport(
			Class<? extends Vertex> vertexClass, int id, Graph graph) {
		try {
			Constructor<? extends Vertex> constructor = vertexDatabaseMap
					.get(vertexClass);
			return constructor.newInstance(id, graph);
		} catch (Exception exception) {
			if (exception.getCause() instanceof GraphException) {
				throw new GraphException(exception.getCause()
						.getLocalizedMessage());
			} else {
				throw new SchemaClassAccessException(
						"Cannot create vertex of class "
								+ vertexClass.getCanonicalName(), exception);
			}
		}
	}

	@Override
	public void setGraphDatabaseImplementationClass(
			Class<? extends Graph> originalClass,
			Class<? extends Graph> implementationClass) {
		if (isSuperclassOrEqual(originalClass, implementationClass)) {
			try {
				Class<?>[] params = { String.class, int.class, int.class,
						GraphDatabase.class };
				graphDatabaseMap.put(originalClass, implementationClass
						.getConstructor(params));
			} catch (NoSuchMethodException exception) {
				throw new SchemaClassAccessException(
						"Unable to locate default constructor for graphclass "
								+ implementationClass.getName(), exception);
			}
		}
	}

	@Override
	public void setVertexDatabaseImplementationClass(
			Class<? extends Vertex> originalClass,
			Class<? extends Vertex> implementationClass) {
		if (isSuperclassOrEqual(originalClass, implementationClass)) {
			try {
				Class<?>[] params = { int.class, Graph.class };
				vertexDatabaseMap.put(originalClass, implementationClass
						.getConstructor(params));
			} catch (NoSuchMethodException exception) {
				throw new SchemaClassAccessException(
						"Unable to locate default constructor for vertex class"
								+ implementationClass, exception);
			}
		}
	}

	@Override
	public void setEdgeDatabaseImplementationClass(
			Class<? extends Edge> originalClass,
			Class<? extends Edge> implementationClass) {
		if (isSuperclassOrEqual(originalClass, implementationClass)) {
			try {
				Class<?>[] params = { int.class, Graph.class, Vertex.class,
						Vertex.class };
				edgeDatabaseMap.put(originalClass, implementationClass
						.getConstructor(params));
			} catch (NoSuchMethodException exception) {
				throw new SchemaClassAccessException(
						"Unable to locate default constructor for edge class"
								+ implementationClass, exception);
			}
		}
	}

	// --- Methods for option TRANSIMPL
	// -------------------------------------------------

	@Override
	public Edge createEdgeWithTransactionSupport(
			Class<? extends Edge> edgeClass, int id, Graph g, Vertex alpha,
			Vertex omega) {
		try {
			Edge e = edgeTransactionMap.get(edgeClass).newInstance(id, g,
					alpha, omega);
			e.initializeAttributesWithDefaultValues();
			return e;
		} catch (Exception ex) {
			if (ex.getCause() instanceof GraphException) {
				throw new GraphException(ex.getCause().getLocalizedMessage(),
						ex);
			}
			throw new SchemaClassAccessException("Cannot create edge of class "
					+ edgeClass.getCanonicalName(), ex);
		}
	}

	@Override
	public Graph createGraphWithTransactionSupport(
			Class<? extends Graph> graphClass, String id, int vMax, int eMax) {
		try {
			Graph g = graphTransactionMap.get(graphClass).newInstance(id, vMax,
					eMax);
			return g;
		} catch (Exception ex) {
			if (ex.getCause() instanceof GraphException) {
				throw new GraphException(ex.getCause().getLocalizedMessage(),
						ex);
			}
			throw new SchemaClassAccessException("Cannot create graph of class "
					+ graphClass.getCanonicalName(), ex);
		}
	}

	@Override
	public Graph createGraphWithTransactionSupport(
			Class<? extends Graph> graphClass, String id) {
		try {
			Graph g = graphTransactionMap.get(graphClass).newInstance(id, 1000,
					1000);
			return g;
		} catch (Exception ex) {
			if (ex.getCause() instanceof GraphException) {
				throw new GraphException(ex.getCause().getLocalizedMessage(),
						ex);
			}
			throw new SchemaClassAccessException("Cannot create graph of class "
					+ graphClass.getCanonicalName(), ex);
		}
	}

	@Override
	public Vertex createVertexWithTransactionSupport(
			Class<? extends Vertex> vertexClass, int id, Graph g) {
		try {
			Vertex v = vertexTransactionMap.get(vertexClass).newInstance(id, g);
			return v;
		} catch (Exception ex) {
			if (ex.getCause() instanceof GraphException) {
				throw new GraphException(ex.getCause().getLocalizedMessage(),
						ex);
			}
			throw new SchemaClassAccessException("Cannot create vertex of class "
					+ vertexClass.getCanonicalName(), ex);
		}
	}

	public void setGraphTransactionImplementationClass(
			Class<? extends Graph> originalClass,
			Class<? extends Graph> implementationClass) {
		if (isSuperclassOrEqual(originalClass, implementationClass)) {
			try {
				Class<?>[] params = { String.class, int.class, int.class };
				graphTransactionMap.put(originalClass, implementationClass
						.getConstructor(params));
			} catch (NoSuchMethodException ex) {
				throw new SchemaClassAccessException(
						"Unable to locate transaction constructor for graphclass "
								+ implementationClass.getName(), ex);
			}
		}
	}

	@Override
	public void setVertexTransactionImplementationClass(
			Class<? extends Vertex> originalClass,
			Class<? extends Vertex> implementationClass) {
		if (isSuperclassOrEqual(originalClass, implementationClass)) {
			try {
				Class<?>[] params = { int.class, Graph.class };
				vertexTransactionMap.put(originalClass, implementationClass
						.getConstructor(params));
			} catch (NoSuchMethodException ex) {
				throw new SchemaClassAccessException(
						"Unable to locate transaction constructor for vertexclass"
								+ implementationClass, ex);
			}
		}
	}

	@Override
	public void setEdgeTransactionImplementationClass(
			Class<? extends Edge> originalClass,
			Class<? extends Edge> implementationClass) {
		if (isSuperclassOrEqual(originalClass, implementationClass)) {
			try {
				Class<?>[] params = { int.class, Graph.class, Vertex.class,
						Vertex.class };
				edgeTransactionMap.put(originalClass, implementationClass
						.getConstructor(params));
			} catch (NoSuchMethodException ex) {
				throw new SchemaClassAccessException(
						"Unable to locate transaction constructor for edgeclass"
								+ implementationClass, ex);
			}
		}
	}

	// -------------------------------------------------------------------------
	// Helper methods.
	// -------------------------------------------------------------------------

	/**
	 * tests if a is a superclass of b or the same class than b
	 * 
	 * @param a
	 * @param b
	 * @return
	 */
	protected boolean isSuperclassOrEqual(Class<?> a, Class<?> b) {
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
	protected boolean implementsInterface(Class<?> a, Class<?> b) {
		Class<?>[] list = a.getInterfaces();
		for (Class<?> c : list) {
			if (c == b) {
				return true;
			}
		}
		return false;
	}

}
