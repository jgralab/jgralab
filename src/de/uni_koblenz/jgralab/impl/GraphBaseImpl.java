/*
 * JGraLab - The Java graph laboratory
 * (c) 2006-2007 Institute for Software Technology
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

import de.uni_koblenz.jgralab.Aggregation;
import de.uni_koblenz.jgralab.AttributedElement;
import de.uni_koblenz.jgralab.Composition;
import de.uni_koblenz.jgralab.Edge;
import de.uni_koblenz.jgralab.EdgeClass;
import de.uni_koblenz.jgralab.Graph;
import de.uni_koblenz.jgralab.GraphClass;
import de.uni_koblenz.jgralab.GraphException;
import de.uni_koblenz.jgralab.GraphFactory;
import de.uni_koblenz.jgralab.RandomIdGenerator;
import de.uni_koblenz.jgralab.Schema;
import de.uni_koblenz.jgralab.Vertex;
import de.uni_koblenz.jgralab.VertexClass;


/**
 * This class is the abstract base class for the Array and List implementation of the 
 * Graph. It provides functionality that is independent from the list and array implementation,
 * for instance the managment of temporary attributes, the list of edges and vertices in the graph
 * and the graphs id are located here.
 *
 */
public abstract class GraphBaseImpl extends AttributedElementImpl implements Graph {
	
	/**
	 * factor to expand the internal arrays if vertices or edges get too big
	 */
	protected final double EXPANSIONFACTOR = 2.0;
	
	/**
	 * The schema this graph belongs to
	 */
	private Schema schema;
	
	/**
	 * the unique id of the graph in the schema 
	 */
	private String id;
	
	/**
	 * indexed with vertex-id, holds the actual vertex-object itself
	 */
	protected Vertex vertex[];

	/**
	 * indexed with edge-id, holds the actual edge-object itself
	 */
	protected Edge edge[];

	/**
	 * maximum number of edges + 1
	 */
	protected int eSize;

	/**
	 * maximum number of vertices + 1
	 */
	protected int vSize;
	
	/**
	 * holds the graphFactory that was used to create this graph - it wil lbe used
	 * to create vertices and edges in this graph
	 */
	protected GraphFactory graphFactory;
	
	
	/**
	 * holds the version of that graph, for every modification (e.g. adding a vertex or edge or changing the vertex or edge sequence) this version number is increased by one
	 * It is set to 0 when the graph is loaded.
	 */
	protected long graphVersion;
	
	/**
	 * creates a new graph base impl object
	 * @param id The id of the new Graph
	 * @param aGraphClass The Graphclass of the new graph
	 * @param s The schema this Graphclass belongs to
	 * @param vMax initial number of vertices
	 * @param eMax initial number of edges
	 * @ if something went wrong
	 */
	protected GraphBaseImpl(String id, GraphClass aGraphClass, Schema s, int vMax, int eMax)  {
		super(aGraphClass);
		if (vMax < 0)
			throw new GraphException("vMax must not be less than zero", null);
		if (eMax < 0)
			throw new GraphException("eMax must not be less than zero", null);
		if (s == null)
			throw new GraphException("schema is null for graph " + id, null);
		if (aGraphClass == null)
			throw new GraphException("graphclass is null for graph " + id, null);

		this.schema = s;
		if (id == null) {
			this.id = RandomIdGenerator.generateId();
		} else {
			this.id = id;
		}

		expandVertexArray(vMax+1);
		expandEdgeArray(eMax+1);
		graphFactory = schema.getGraphFactory();
	}
	
	/*
	 * remember, cls must be the Impl class!
	 * (non-Javadoc)
	 * @see jgralab.Graph#createVertex(java.lang.Class)
	 */
	@SuppressWarnings("unchecked")
	public <T extends Vertex> T createVertex(Class<T> cls)  {
		try {
			Vertex v = graphFactory.createVertex(cls, 0, this);
//			Constructor<T> constructor = cls.getConstructor( int.class, Graph.class );
//			T v = constructor.newInstance(0, this);
			addVertex(v);
			return (T) v;
		} catch (Exception ex) {
			throw new GraphException("Error creating vertex of class " + cls.getName(), ex);
		} 
	}
	
	/**
	 * Creates a instance of the given class and adds this edge to the graph
	 */
	@SuppressWarnings("unchecked")
	public <T extends Edge> T createEdge(Class<T> cls, Vertex alpha, Vertex omega)   {
		try {
			Edge e = graphFactory.createEdge(cls, 0, this);
//			Constructor<T> constructor = cls.getConstructor( int.class, Graph.class );
//			T e = constructor.newInstance(0, this);
			addEdge(e, alpha, omega);
			return (T) e;
		} catch (Exception ex) {
			throw new GraphException("Error creating edge of class " + cls.getName(), ex);
		} 
	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see jgralab.Graph#getMaxVCount()
	 */
	public int getMaxVCount() {
		return vSize - 1;
	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see jgralab.Graph#getMaxECount()
	 */
	public int getMaxECount() {
		return eSize - 1;
	}
	
	public long getGraphVersion() {
		return graphVersion;
	}
	
	public void setGraphVersion(long graphVersion) {
		this.graphVersion = graphVersion;
	}
	
	public final boolean isModified(long aGraphVersion) {
		return (graphVersion != aGraphVersion);
	}
	
	/**
	 * increases the graph version number
	 *
	 */
	public final void modified() {
		graphVersion++;
	}
	
	
	public GraphClass getGraphClass() {
		return (GraphClass)theClass;
	}
	
	public Schema getSchema() {
		return schema;
	}

	/* (non-Javadoc)
	 * @see jgralab.Graph#getId()
	 */
	public String getId() {
		return id;
	}

	/* (non-Javadoc)
	 * @see jgralab.Graph#setId(java.lang.String)
	 */
	public void setId(String id) {
		this.id = id;
	}

	
	/*
	 * (non-Javadoc)
	 * 
	 * @see jgralab.Graph#getFirstVertexOfExplicitClass(jgralab.VertexClass)
	 */
	public Vertex getFirstVertexOfClass(VertexClass aVertexClass,
			boolean explicitType) {
		Vertex firstVertex = getFirstVertex();
		if (firstVertex == null)
			return null;
		if (aVertexClass.equals(firstVertex.getAttributedElementClass()))
				return firstVertex;
		if (!explicitType && aVertexClass.isSuperClassOf(
					firstVertex.getAttributedElementClass())) 
				return getFirstVertex();
		return getNextVertexOfClass(firstVertex, aVertexClass,
					explicitType);
	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see jgralab.Graph#getFirstVertexOfExplicitClass(Class)
	 */
	public Vertex getFirstVertexOfClass(Class<? extends Vertex> aVertexClass,
			boolean explicitType) {
		Vertex firstVertex = getFirstVertex();
		if (firstVertex == null)
			return null;
		if (explicitType) {
			if (aVertexClass == firstVertex.getM1Class())
				return firstVertex;
		} else {
			if (aVertexClass.isInstance(firstVertex))
				return firstVertex;
		}
		return getNextVertexOfClass(firstVertex, aVertexClass,
					explicitType);
	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see jgralab.Graph#getFirstVertexOfClass(jgralab.VertexClass)
	 */
	public Vertex getFirstVertexOfClass(VertexClass aVertexClass) {
		return getFirstVertexOfClass(aVertexClass, false);
	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see jgralab.Graph#getFirstVertexOfClass(Class)
	 */
	public Vertex getFirstVertexOfClass(Class<? extends Vertex> aVertexClass) {
		return getFirstVertexOfClass(aVertexClass, false);
	}
	
	/**
	 * @param no
	 *            edge number (positive or negative)
	 * @return positive id to be used as index in incidence array
	 */
	protected final int edgeOffset(int no) {
		return no + eSize;
	}
	
	protected void expandEdgeArray(int newSize)  {
		if (newSize <= eSize) {
			throw new GraphException("newSize be > eSize: eSize=" + eSize + ", new size=" + newSize);
		}

		Edge[] expandedArray = new Edge[newSize * 2];
		if (edge != null) {
			System.arraycopy(edge, 0, expandedArray, newSize-eSize, edge.length);
		}	
		edge = expandedArray;
		eSize = newSize;
	}
	
	protected void expandVertexArray(int newSize)  {
		if (newSize <= vSize) {
			throw new GraphException("newSize be > vSize: vSize=" + vSize + ", new size=" + newSize);
		}
		Vertex[] expandedArray = new Vertex[newSize];
		if (vertex != null) {
			System.arraycopy(vertex, 0, expandedArray, 0, vertex.length);
		}
		vertex = expandedArray;
		vSize = newSize;
	}
	
	public int compareTo(AttributedElement a) {
		if (a instanceof Graph) {
			Graph g = (Graph) a;
			return this.hashCode() - g.hashCode();
		}
		return -1;
	}
	
	public Iterable<Edge> edges() {
		return new EdgeIterable<Edge>(this);
	}
	
	
	public Iterable<Edge> edges(EdgeClass eclass) {
		return new EdgeIterable<Edge>(this, eclass);
	}
	

	public Iterable<Edge> edges(Class<? extends Edge> eclass) {
		return new EdgeIterable<Edge>(this, eclass);
	}
	
	public Iterable<Edge> edges(EdgeClass eclass, boolean explicitType) {
		return new EdgeIterable<Edge>(this, eclass, explicitType);
	}
	
	public Iterable<Edge> edges(Class<? extends Edge> eclass, boolean explicitType) {
		return new EdgeIterable<Edge>(this, eclass, explicitType);
	}
	
	public Iterable<Aggregation> aggregations() {
		return new EdgeIterable<Aggregation>(this, Aggregation.class);
	}
		
	public Iterable<Composition> compositions() {
		return new EdgeIterable<Composition>(this, Composition.class);
	}
	
	public Iterable<Vertex> vertices() {
		return new VertexIterable<Vertex>(this);
	}
	
	
	public Iterable<Vertex> vertices(VertexClass eclass) {
		return new VertexIterable<Vertex>(this, eclass);
	}
	

	public Iterable<Vertex> vertices(Class<? extends Vertex> vclass) {
		return new VertexIterable<Vertex>(this, vclass);
	}
	
	public Iterable<Vertex> vertices(VertexClass vclass, boolean explicitType) {
		return new VertexIterable<Vertex>(this, vclass, explicitType);
	}
	

	public Iterable<Vertex> vertices(Class<? extends Vertex> vclass, boolean explicitType) {
		return new VertexIterable<Vertex>(this, vclass, explicitType);
	}
	

	
}		

