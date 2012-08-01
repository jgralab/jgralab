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
import de.uni_koblenz.jgralab.ImplementationType;
import de.uni_koblenz.jgralab.Vertex;
import de.uni_koblenz.jgralab.impl.db.GraphDatabase;
import de.uni_koblenz.jgralab.schema.EdgeClass;
import de.uni_koblenz.jgralab.schema.GraphClass;
import de.uni_koblenz.jgralab.schema.Schema;
import de.uni_koblenz.jgralab.schema.VertexClass;
import de.uni_koblenz.jgralab.schema.exception.SchemaClassAccessException;
import de.uni_koblenz.jgralab.schema.exception.SchemaException;

/**
 * Default implementation for GraphFactory. Per default every create-method
 * creates an instance of exactly the specified class. To change this use
 * <code>setImplementationClass</code>-methods. Class is abstract because only
 * factories which are specific for their schema should be used.
 * 
 * @author ist@uni-koblenz.de
 */
public abstract class GraphFactoryImpl implements GraphFactory {

	// Maps for standard support.
	protected Constructor<? extends Graph> graphConstructor;
	protected HashMap<EdgeClass, Constructor<? extends Edge>> edgeMap;
	protected HashMap<VertexClass, Constructor<? extends Vertex>> vertexMap;

	protected Schema schema;
	protected ImplementationType implementationType;
	protected GraphDatabase graphDatabase;

	protected boolean graphCreated;

	/**
	 * Creates and initializes a new <code>GraphFactoryImpl</code>.
	 */
	protected GraphFactoryImpl(Schema s, ImplementationType i) {
		schema = s;
		implementationType = i;
	}

	public void setGraphDatabase(GraphDatabase graphDatabase) {
		this.graphDatabase = graphDatabase;
	}

	protected void createMaps() {
		edgeMap = new HashMap<EdgeClass, Constructor<? extends Edge>>();
		vertexMap = new HashMap<VertexClass, Constructor<? extends Vertex>>();
	}

	// ---------------------------------------------------
	@Override
	public ImplementationType getImplementationType() {
		return implementationType;
	}

	@Override
	public Schema getSchema() {
		return schema;
	}

	@Override
	public void setGraphImplementationClass(GraphClass gc,
			Class<? extends Graph> implementationClass) {
		if (graphCreated) {
			throw new IllegalStateException(
					"Can't change implementation class after a graph was created.");
		}
		Class<? extends Graph> originalClass = gc.getSchemaClass();
		if (isSuperclassOrEqual(originalClass, implementationClass)) {
			try {
				if (implementationType.equals(ImplementationType.DATABASE)) {
					Class<?>[] params = { String.class, int.class, int.class,
							GraphDatabase.class };
					graphConstructor = implementationClass
							.getConstructor(params);
				} else {
					Class<?>[] params = { String.class, int.class, int.class };
					graphConstructor = implementationClass
							.getConstructor(params);
				}
			} catch (NoSuchMethodException ex) {
				throw new SchemaClassAccessException(
						"Unable to locate constructor for graphclass "
								+ implementationClass.getName(), ex);
			}
		} else {
			throw new SchemaException(implementationClass.getCanonicalName()
					+ " does not implement " + originalClass.getCanonicalName());
		}
	}

	@Override
	public <G extends Graph> G createGraph(GraphClass gc, String id, int vMax,
			int eMax) {
		try {
			if (implementationType.equals(ImplementationType.DATABASE)) {
				@SuppressWarnings("unchecked")
				G dbGraph = (G) graphConstructor.newInstance(id, vMax, eMax,
						graphDatabase);
				dbGraph.setGraphFactory(this);
				graphCreated = true;
				return dbGraph;
			} else {
				@SuppressWarnings("unchecked")
				G graph = (G) graphConstructor.newInstance(id, vMax, eMax);
				graph.setGraphFactory(this);
				graphCreated = true;
				return graph;
			}
		} catch (Exception ex) {
			throw new SchemaClassAccessException(
					"Cannot create graph of class "
							+ graphConstructor.getDeclaringClass()
									.getCanonicalName(), ex);
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public <E extends Edge> E createEdge(EdgeClass ec, int id, Graph g,
			Vertex alpha, Vertex omega) {
		try {
			if (!((InternalGraph) g).isLoading() && (g.hasECARuleManager())) {
				g.getECARuleManager().fireBeforeCreateEdgeEvents(ec);
			}
			E newInstance;
			if(ec.equals(g.getGraphClass().getTemporaryEdgeClass())){
				newInstance = (E) g.createTemporaryEdge(alpha, omega);
			}
			else if(alpha.isTemporary() || omega.isTemporary()){
				newInstance = (E) g.createTemporaryEdge(ec, alpha, omega);
			}
			else{
				newInstance = (E) edgeMap.get(ec)
					.newInstance(id, g, alpha, omega);
			}
			if (!((InternalGraph) g).isLoading() && (g.hasECARuleManager())) {
				g.getECARuleManager().fireAfterCreateEdgeEvents(newInstance);
			}
			return newInstance;
		} catch (Exception ex) {
			if (ex.getCause() instanceof GraphException) {
				throw new GraphException(ex.getCause().getLocalizedMessage(),
						ex);
			}
			throw new SchemaClassAccessException("Cannot create edge of class "
					+ ec.getQualifiedName(), ex);
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public <V extends Vertex> V createVertex(VertexClass vc, int id, Graph g) {
		try {
			if (!((InternalGraph) g).isLoading() && (g.hasECARuleManager())) {
				g.getECARuleManager().fireBeforeCreateVertexEvents(vc);
			}
			V newInstance;
			if(vc.equals(g.getGraphClass().getTemporaryVertexClass())){
				newInstance = (V) g.createTemporaryVertex();
			}else{
				newInstance = (V) vertexMap.get(vc).newInstance(id, g);
			}
			if (!((InternalGraph) g).isLoading() && (g.hasECARuleManager())) {
				g.getECARuleManager().fireAfterCreateVertexEvents(newInstance);
			}
			return newInstance;
		} catch (Exception ex) {
			if (ex.getCause() instanceof GraphException) {
				throw new GraphException(ex.getCause().getLocalizedMessage(),
						ex);
			}
			throw new SchemaClassAccessException(
					"Cannot create vertex of class " + vc.getQualifiedName(),
					ex);
		}
	}

	@Override
	public void setVertexImplementationClass(VertexClass vc,
			Class<? extends Vertex> implementationClass) {
		if (graphCreated) {
			throw new IllegalStateException(
					"Can't change implementation class after a graph was created.");
		}
		Class<? extends Vertex> originalClass = vc.getSchemaClass();
		if (isSuperclassOrEqual(originalClass, implementationClass)) {
			try {
				Class<?>[] params = { int.class, Graph.class };
				vertexMap.put(vc, implementationClass.getConstructor(params));
			} catch (NoSuchMethodException ex) {
				throw new SchemaClassAccessException(
						"Unable to locate default constructor for vertexclass"
								+ implementationClass, ex);
			}
		} else {
			throw new SchemaException(implementationClass.getCanonicalName()
					+ " does not implement " + originalClass.getCanonicalName());
		}
	}

	@Override
	public void setEdgeImplementationClass(EdgeClass ec,
			Class<? extends Edge> implementationClass) {
		if (graphCreated) {
			throw new IllegalStateException(
					"Can't change implementation class after a graph was created.");
		}
		Class<? extends Edge> originalClass = ec.getSchemaClass();
		if (isSuperclassOrEqual(originalClass, implementationClass)) {
			try {
				Class<?>[] params = { int.class, Graph.class, Vertex.class,
						Vertex.class };
				edgeMap.put(ec, implementationClass.getConstructor(params));
			} catch (NoSuchMethodException ex) {
				throw new SchemaClassAccessException(
						"Unable to locate default constructor for edgeclass"
								+ implementationClass, ex);
			}
		} else {
			throw new SchemaException(implementationClass.getCanonicalName()
					+ " does not implement " + originalClass.getCanonicalName());
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
