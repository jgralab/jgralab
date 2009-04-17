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

package de.uni_koblenz.jgralab.xmlrpc;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Set;

import org.apache.xmlrpc.XmlRpcException;

import de.uni_koblenz.jgralab.Attribute;
import de.uni_koblenz.jgralab.AttributedElement;
import de.uni_koblenz.jgralab.Edge;
import de.uni_koblenz.jgralab.Graph;
import de.uni_koblenz.jgralab.GraphElement;
import de.uni_koblenz.jgralab.GraphIO;
import de.uni_koblenz.jgralab.GraphIOException;
import de.uni_koblenz.jgralab.M1ClassManager;
import de.uni_koblenz.jgralab.Vertex;
import de.uni_koblenz.jgralab.schema.CompositeDomain;
import de.uni_koblenz.jgralab.schema.Domain;
import de.uni_koblenz.jgralab.schema.EdgeClass;
import de.uni_koblenz.jgralab.schema.MapDomain;
import de.uni_koblenz.jgralab.schema.RecordDomain;
import de.uni_koblenz.jgralab.schema.Schema;
import de.uni_koblenz.jgralab.schema.VertexClass;

/**
 * The public methods of this class which have return type (no {@code void}) can
 * be accessed by an XML-RPC client.
 *
 * @author ist@uni-koblenz.de
 */
public class JGraLabFacade {

	/**
	 * the instance of {@code GraphContainer} containing the graphs
	 */
	private GraphContainer graphContainer;

	public JGraLabFacade() {
		graphContainer = GraphContainer.instance();
	}

	/**
	 * Creates a graph of conforming to the given schema with random id. The
	 * maximum number of vertices and edges is initially set to 100 each.
	 * Returns a {@code Map<String, Object>} with four entries:<br>
	 * <br>
	 * "handle" -> {@code Integer} value which has to be used to access the
	 * graph<br>
	 * "id" -> {@code String} value representing id of the graph<br>
	 * "class" -> {@code String} value representing the name of the graph class
	 * the created graph is an instance of<br>
	 * "attributes" -> {@code Map<String, Object>} representing the graph's
	 * attributes (attribute name -> attribute value)
	 *
	 * @param schemaName
	 *            the name of the Java class of the graph schema. This class
	 *            must be on the classpath of the XMLRPC server.
	 * @return a {@code Map<String, Object>} with four entries (see method
	 *         description)
	 *
	 * @throws XmlRpcException
	 */
	public Map<String, Object> createGraph(String schemaName)
			throws XmlRpcException {
		return createGraph(schemaName, null, 100, 100);
	}

	/**
	 * Creates a graph of type {@code graphClassName} with id {@code graphId}.
	 * The maximum number of vertices and edges is initially set to 100 each.
	 * Returns a {@code Map<String, Object>} with four entries:<br>
	 * <br>
	 * "handle" -> {@code Integer} value which has to be used to access the
	 * graph<br>
	 * "id" -> {@code String} value representing id of the graph<br>
	 * "class" -> {@code String} value representing the name of the graph class
	 * the created graph is an instance of<br>
	 * "attributes" -> {@code Map<String, Object>} representing the graph's
	 * attributes (attribute name -> attribute value)
	 *
	 * @param schemaName
	 *            the name of the Java class of the graph schema. This class
	 *            must be on the classpath of the XMLRPC server.
	 * @param graphClassName
	 *            the name of the graph class the created graph shall be an
	 *            instance of
	 * @param graphId
	 *            the id of the created graph
	 * @return a {@code Map<String, Object>} with four entries (see method
	 *         description)
	 *
	 * @throws XmlRpcException
	 */
	public Map<String, Object> createGraph(String schemaName, String graphId)
			throws XmlRpcException {
		return createGraph(schemaName, graphId, 100, 100);
	}

	/**
	 * Creates a graph of the given {@code schemaName} with id {@code graphId},
	 * and maximum numbers of vertices and edges of {@code vMax} and {@code
	 * eMax}, respectively. Returns a {@code Map<String, Object>} with four
	 * entries:<br>
	 * <br>
	 * "handle" -> {@code Integer} value which has to be used to access the
	 * graph<br>
	 * "id" -> {@code String} value representing id of the graph<br>
	 * "class" -> {@code String} value representing the name of the graph class
	 * the created graph is an instance of<br>
	 * "attributes" -> {@code Map<String, Object>} representing the graph's
	 * attributes (attribute name -> attribute value)
	 *
	 * @param schemaName
	 *            the name of the Java class of the graph schema. This class
	 *            must be on the classpath of the XMLRPC server.
	 * @param graphId
	 *            the id of the created graph
	 * @param vMax
	 *            the maximum number of vertices
	 * @param eMax
	 *            the maximum number of edges
	 * @return a {@code Map<String, Object>} with four entries (see method
	 *         description)
	 *
	 * @throws XmlRpcException
	 */
	public Map<String, Object> createGraph(String schemaName, String graphId,
			int vMax, int eMax) throws XmlRpcException {
		try {
			Class<?> schemaClass = Class.forName(schemaName, true,
					M1ClassManager.instance());
			Schema schema = (Schema) (schemaClass.getMethod("instance",
					(Class[]) null).invoke(null));

			Method graphCreateMethod = schema.getGraphCreateMethod();

			Graph graph = (Graph) (graphCreateMethod.invoke(null, new Object[] {
					graphId, vMax, eMax }));

			int graphNo = graphContainer.addGraph(graph);
			return createGraphMap(graphNo);
		} catch (Exception e) {
			e.printStackTrace();
			throw new XmlRpcException(e.toString());
		}
	}

	/**
	 * Creates a graph of type {@code graphClassName} with random id. The
	 * maximum number of vertices and edges is initially set to 100 each.
	 * Returns a {@code Map<String, Object>} with four entries:<br>
	 * <br>
	 * "handle" -> {@code Integer} value which has to be used to access the
	 * graph<br>
	 * "id" -> {@code String} value representing id of the graph<br>
	 * "class" -> {@code String} value representing the name of the graph class
	 * the created graph is an instance of<br>
	 * "attributes" -> {@code Map<String, Object>} representing the graph's
	 * attributes (attribute name -> attribute value)
	 *
	 * If the M1-Classes are not present in the classpath, the "tempSchema"
	 * folder must be on the classpath of the XML-RPC server. The path to the
	 * {@code javac} compiler must be in the {@code PATH} system environment
	 * variable.
	 *
	 * @param schemaUrl
	 *            the URL of the TG-file holding the schema.
	 * @param graphClassName
	 *            the name of the graph class of the new graph
	 * @return a {@code Map<String, Object>} with four entries (see method
	 *         description)
	 *
	 * @throws XmlRpcException
	 */
	public Map<String, Object> createGraphWithRemoteSchema(String schemaUrl,
			String graphClassName) throws XmlRpcException {
		return createGraphWithRemoteSchema(schemaUrl, graphClassName, null,
				100, 100);
	}

	/**
	 * Creates a graph of type {@code graphClassName} with id {@code graphId}.
	 * The maximum number of vertices and edges is initially set to 100 each.
	 * Returns a {@code Map<String, Object>} with four entries:<br>
	 * <br>
	 * "handle" -> {@code Integer} value which has to be used to access the
	 * graph<br>
	 * "id" -> {@code String} value representing id of the graph<br>
	 * "class" -> {@code String} value representing the name of the graph class
	 * the created graph is an instance of<br>
	 * "attributes" -> {@code Map<String, Object>} representing the graph's
	 * attributes (attribute name -> attribute value)
	 *
	 * If the M1-Classes are not present in the classpath, the "tempSchema"
	 * folder must be on the classpath of the XML-RPC server. The path to the
	 * {@code javac} compiler must be in the {@code PATH} system environment
	 * variable.
	 *
	 * @param schemaUrl
	 *            the URL of the TG-file holding the schema.
	 * @param graphClassName
	 *            the name of the graph class of the new graph
	 * @param graphId
	 *            the id of the created graph
	 * @return a {@code Map<String, Object>} with four entries (see method
	 *         description)
	 *
	 * @throws XmlRpcException
	 */
	public Map<String, Object> createGraphWithRemoteSchema(String schemaUrl,
			String graphClassName, String graphId) throws XmlRpcException {
		return createGraphWithRemoteSchema(schemaUrl, graphClassName, graphId,
				100, 100);
	}

	/**
	 * Creates a graph of type {@code graphClassName} with id {@code graphId},
	 * and maximum numbers of vertices and edges of {@code vMax} and {@code
	 * eMax}, respectively. Returns a {@code Map<String, Object>} with four
	 * entries:<br>
	 * <br>
	 * "handle" -> {@code Integer} value which has to be used to access the
	 * graph<br>
	 * "id" -> {@code String} value representing id of the graph<br>
	 * "class" -> {@code String} value representing the name of the graph class
	 * the created graph is an instance of<br>
	 * "attributes" -> {@code Map<String, Object>} representing the graph's
	 * attributes (attribute name -> attribute value)
	 *
	 * If the M1-Classes are not present in the classpath, the "tempSchema"
	 * folder must be on the classpath of the XML-RPC server as well as the path
	 * to the {@code javac} compiler must be in the {@code PATH} system
	 * environment variable.
	 *
	 * @param schemaUrl
	 *            the URL of the TG-file holding the schema.
	 * @param graphClassName
	 *            the name of the graph class of the new graph
	 * @param graphId
	 *            the id of the created graph
	 * @param vMax
	 *            the maximum number of vertices
	 * @param eMax
	 *            the maximum number of edges
	 * @return a {@code Map<String, Object>} with four entries (see method
	 *         description)
	 *
	 * @throws XmlRpcException
	 */
	public Map<String, Object> createGraphWithRemoteSchema(String schemaUrl,
			String graphClassName, String graphId, int vMax, int eMax)
			throws XmlRpcException {
		int graphNo;

		try {
			Method graphCreateMethod;
			Graph graph;
			Schema schema = GraphIO.loadSchemaFromURL(schemaUrl);

			try {
				Class.forName(schema.getQualifiedName(), true, M1ClassManager
						.instance());
			} catch (ClassNotFoundException e) {
				schema.compile();
			}

			graphCreateMethod = schema.getGraphCreateMethod();
			graph = (Graph) graphCreateMethod.invoke(null, new Object[] {
					graphId, vMax, eMax });

			graphNo = graphContainer.addGraph(graph);
		} catch (Exception e) {
			e.printStackTrace();
			throw new XmlRpcException(e.toString());
		}

		return createGraphMap(graphNo);
	}

	/**
	 * Loads the graph located in the TG file at {@code url}. Returns a {@code
	 * Map<String, Object>} with four entries:<br>
	 * <br>
	 * "handle" -> {@code Integer} value which has to be used to access the
	 * graph<br>
	 * "id" -> {@code String} value representing id (name) of the graph<br>
	 * "class" -> {@code String} value representing the name of the graph class
	 * the created graph is an instance of<br>
	 * "attributes" -> {@code Map<String, Object>} representing the graph's
	 * attributes (attribute name -> attribute value)
	 *
	 * @param url
	 *            the URL pointing to the TG-file containing the graph to be
	 *            loaded
	 * @return a {@code Map<String, Object>} with four entries (see method
	 *         description)
	 * @throws XmlRpcException
	 */
	public Map<String, Object> loadGraph(String url) throws XmlRpcException {
		int graphNo;

		try {
			Graph graph;
			Schema schema = GraphIO.loadSchemaFromURL(url);

			try {
				Class.forName(schema.getQualifiedName(), true, M1ClassManager
						.instance());
			} catch (ClassNotFoundException e) {
				schema.compile();
			}

			GraphIO.loadSchemaFromURL(url).compile();

			graph = GraphIO.loadGraphFromURL(url, null);
			graphNo = graphContainer.addGraph(graph);
		} catch (Exception e) {
			e.printStackTrace();
			throw new XmlRpcException(e.toString());
		}

		return createGraphMap(graphNo);
	}

	/**
	 * Saves the graph indicated by {@code graphNo} to the file {@code
	 * tgFilename}. Returns <code>true</code> on success, throws an exception on
	 * failure.
	 *
	 * @param graphNo
	 *            handle of the graph to be saved
	 * @param tgFilename
	 *            the name of the file containing the graph to be loaded
	 * @return true if sucessfully saved (false can not occur because an
	 *         exception is thrown).
	 * @throws XmlRpcException
	 */
	public boolean saveGraph(int graphNo, String tgFilename)
			throws XmlRpcException {
		try {
			GraphIO.saveGraphToFile(tgFilename, graphContainer
					.getGraph(graphNo), null);
			return true;
		} catch (GraphIOException e) {
			e.printStackTrace();
			throw new XmlRpcException(e.toString());
		}
	}

	/**
	 * Removes the graph indicated by {@code graphNo}. Returns {@code true} if
	 * the removal was successful, {@code false} otherwise.
	 *
	 * @param graphNo
	 *            handle of the graph which shall be removed
	 * @return {@code true} if the removal was successful, {@code false}
	 *         otherwise
	 * @throws XmlRpcException
	 */
	public boolean releaseGraph(int graphNo) throws XmlRpcException {
		if (graphContainer.containsGraph(graphNo)) {
			graphContainer.releaseGraph(graphNo);
			return true;
		}
		return false;
	}

	/**
	 * Checks whether a graph with the handle {@code graphNo} exists.
	 *
	 * @param graphNo
	 *            the handle for which the existence of a graph shall be checked
	 * @return {@code true} if a graph with handle {@code graphNo} exists,
	 *         {@code false} otherwise
	 * @throws XmlRpcException
	 */
	public boolean containsGraph(int graphNo) throws XmlRpcException {
		return graphContainer.containsGraph(graphNo);
	}

	/**
	 * Returns the number of vertices in the graph indicated by {@code graphNo}.
	 *
	 * @param graphNo
	 *            the handle of the graph
	 * @return the number of vertices
	 */
	public int getVCount(int graphNo) {
		return graphContainer.getGraph(graphNo).getVCount();
	}

	/**
	 * Returns the number of edges in the graph indicated by {@code graphNo}.
	 *
	 * @param graphNo
	 *            the handle of the graph
	 * @return the number of edges
	 */
	public int getECount(int graphNo) {
		return graphContainer.getGraph(graphNo).getECount();
	}

	/**
	 * Creates a vertex of type {@code vertexClassName} in the graph pointed to
	 * by {@code graphNo}. Returns a {@code Map<String, Object>} with three
	 * entries:<br>
	 * <br>
	 * "id" -> {@code Integer} value representing the id of the vertex<br>
	 * "class" -> {@code String} value representing the name of the vertex class
	 * the created vertex is an instance of<br>
	 * "attributes" -> {@code Map<String, Object>} representing the vertex'
	 * attributes (attribute name -> attribute value)
	 *
	 * @param graphNo
	 *            the handle of the graph for which the vertex shall be created
	 * @param vertexClassName
	 *            the name of the vertex class the created vertex shall be an
	 *            instance of
	 * @return a {@code Map<String, Object>} with three entries (see method
	 *         description)
	 * @throws XmlRpcException
	 */
	public Map<String, Object> createVertex(int graphNo, String vertexClassName)
			throws XmlRpcException {
		Class<? extends Vertex> m1VertexClass;

		Graph graph;

		graph = graphContainer.getGraph(graphNo);
		m1VertexClass = graph.getGraphClass().getVertexClass(vertexClassName)
				.getM1Class();

		return createGraphElementMap(graph.createVertex(m1VertexClass));
	}

	/**
	 * Creates an edge of type {@code edgeClassName} from vertex {@code alpha}
	 * to vertex {@code omega} in the graph pointed to by {@code graphNo}.
	 * Returns a {@code Map<String, Object>} with three entries:<br>
	 * <br>
	 * "id" -> {@code Integer} value representing the id of the edge<br>
	 * "class" -> {@code String} value representing the name of the egde class
	 * the created edge is an instance of<br>
	 * "attributes" -> {@code Map<String, Object>} representing the edge's
	 * attributes (attribute name -> attribute value)
	 *
	 * @param graphNo
	 *            the handle of the graph for which the edge shall be created
	 * @param edgeClassName
	 *            the name of the edge class the created edge shall be an
	 *            instance of
	 * @param alphaId
	 *            the id of the vertex on the "from" side of the created edge
	 * @param omegaId
	 *            the id of the vertex on the "to" side of the created edge
	 * @return a {@code Map<String, Object>} with three entries (see method
	 *         description)
	 * @throws XmlRpcException
	 */
	public Map<String, Object> createEdge(int graphNo, String edgeClassName,
			int alphaId, int omegaId) throws XmlRpcException {
		Class<? extends Edge> m1EdgeClass;

		Graph graph;
		Vertex alpha, omega;

		graph = graphContainer.getGraph(graphNo);
		alpha = graph.getVertex(alphaId);
		omega = graph.getVertex(omegaId);

		m1EdgeClass = graph.getGraphClass().getEdgeClass(edgeClassName)
				.getM1Class();

		return createGraphElementMap(graph
				.createEdge(m1EdgeClass, alpha, omega));
	}

	/**
	 * Deletes the vertex {@code vId} in the graph {@code graphNo}. Returns a
	 * {@code Map<String, Object>} with three entries:<br>
	 * <br>
	 * "id" -> {@code Integer} value representing the id of the vertex<br>
	 * "class" -> {@code String} value representing the name of the vertex class
	 * the deleted vertex is an instance of<br>
	 * "attributes" -> {@code Map<String, Object>} representing the vertex'
	 * attributes (attribute name -> attribute value)
	 *
	 * @param graphNo
	 *            the handle of graph containing the vertex to be deleted
	 * @param vId
	 *            the id of the vertex to be deleted
	 * @return a {@code Map<String, Object>} with three entries (see method
	 *         description)
	 * @throws XmlRpcException
	 */
	public Map<String, Object> deleteVertex(int graphNo, int vId)
			throws XmlRpcException {
		Map<String, Object> vertexMap = createGraphElementMap(graphContainer
				.getGraph(graphNo).getVertex(vId));

		Graph graph = graphContainer.getGraph(graphNo);

		graph.deleteVertex(graph.getVertex(vId));

		return vertexMap;
	}

	/**
	 * Deletes the edge {@code eId} in the graph {@code graphNo}. Returns a
	 * {@code Map<String, Object>} with three entries:<br>
	 * <br>
	 * "id" -> {@code Integer} value representing the id of the edge<br>
	 * "class" -> {@code String} value representing the name of the edge class
	 * the deleted edge is an instance of<br>
	 * "attributes" -> {@code Map<String, Object>} representing the edge's
	 * attributes (attribute name -> attribute value)
	 *
	 * @param graphNo
	 *            the handle of graph containing the edge to be deleted
	 * @param eId
	 *            the id of the edge to be deleted
	 * @return a {@code Map<String, Object>} with three entries (see method
	 *         description)
	 * @throws XmlRpcException
	 */
	public Map<String, Object> deleteEdge(int graphNo, int eId)
			throws XmlRpcException {
		Map<String, Object> edgeMap = createGraphElementMap(graphContainer
				.getGraph(graphNo).getEdge(eId));

		Graph graph = graphContainer.getGraph(graphNo);

		graph.deleteEdge(graph.getEdge(eId));

		return edgeMap;
	}

	/**
	 * Checks whether a vertex {@code vId} exists in the graph {@code graphNo}.
	 *
	 * @param graphNo
	 *            the handle of the graph for which the existence of the vertex
	 *            shall be checked
	 * @param vId
	 *            the id of the vertex whose existence is to be checked
	 * @return {@code true} if a vertex with the id {@code vId} exists in the
	 *         graph with the handle {@code graphNo}, {@code false} otherwise
	 * @throws XmlRpcException
	 */
	public boolean containsVertex(int graphNo, int vId) {
		if (graphContainer.getGraph(graphNo).getVertex(vId) == null) {
			return false;
		} else {
			return true;
		}
	}

	/**
	 * Checks whether an edge {@code eId} exists in the graph {@code graphNo}.
	 *
	 * @param graphNo
	 *            the handle of the graph for which the existence of the edge
	 *            shall be checked
	 * @param eId
	 *            the id of the edge whose existence is to be checked
	 * @return {@code true} if a edge with the id {@code eId} exists in the
	 *         graph with the handle {@code graphNo}, {@code false} otherwise
	 * @throws XmlRpcException
	 */
	public boolean containsEdge(int graphNo, int eId) {
		if (graphContainer.getGraph(graphNo).getEdge(eId) == null) {
			return false;
		} else {
			return true;
		}
	}

	/**
	 * Returns the name of the type of the graph pointed to by {@code graphNo}.
	 *
	 * @param graphNo
	 *            the handle of the graph for which the name of its type shall
	 *            be returned
	 * @return the name of the type of the graph with the handle {@code graphNo}
	 */
	public String getGraphClass(int graphNo) {
		return graphContainer.getGraph(graphNo).getAttributedElementClass()
				.getQualifiedName();
	}

	/**
	 * Returns the name of the type of the vertex {@code vId} contained in the
	 * graph {@code graphNo}.
	 *
	 * @param graphNo
	 *            the handle of the graph which contains the vertex
	 * @param vId
	 *            the id of the vertex for which the name of its type shall be
	 *            returned
	 * @return the name of the type of the vertex with the id {@code vId} in the
	 *         graph pointed to by {@code graphNo}
	 */
	public String getVertexClass(int graphNo, int vId) {
		return graphContainer.getGraph(graphNo).getVertex(vId)
				.getAttributedElementClass().getQualifiedName();
	}

	/**
	 * Returns the name of the type of the edge {@code eId} contained in the
	 * graph {@code graphNo}.
	 *
	 * @param graphNo
	 *            the handle of the graph which contains the edge
	 * @param eId
	 *            the id of the edge for which the name of its type shall be
	 *            returned
	 * @return the name of the type of the edge with the id {@code eId} in the
	 *         graph pointed to by {@code graphNo}
	 */
	public String getEdgeClass(int graphNo, int eId) {
		return graphContainer.getGraph(graphNo).getEdge(eId)
				.getAttributedElementClass().getQualifiedName();
	}

	/**
	 * Retrieves the first vertex in the sequence of vertices <i>Vseq</i> of the
	 * graph indicated by {@code graphNo}. Returns a {@code Map<String, Object>}
	 * with three entries:<br>
	 * <br>
	 * "id" -> {@code Integer} value representing the id of the vertex<br>
	 * "class" -> {@code String} value representing the name of the vertex class
	 * the vertex is an instance of<br>
	 * "attributes" -> {@code Map<String, Object>} representing the vertex'
	 * attributes (attribute name -> attribute value)
	 *
	 * @param graphNo
	 *            the handle of the graph for which the first vertex in
	 *            <i>Vseq</i> shall be returned
	 * @return a {@code Map<String, Object>} with three entries (see method
	 *         description)
	 * @throws XmlRpcException
	 */
	public Map<String, Object> getFirstVertex(int graphNo)
			throws XmlRpcException {
		return createGraphElementMap(graphContainer.getGraph(graphNo)
				.getFirstVertex());
	}

	/**
	 * Retrieves the first vertex in the sequence of vertices <i>Vseq</i> of the
	 * graph indicated by {@code graphNo}. The vertex must be of type {@code
	 * vcName}. Returns a {@code Map<String, Object>} with three entries:<br>
	 * <br>
	 * "id" -> {@code Integer} value representing the id of the vertex<br>
	 * "class" -> {@code String} value representing the name of the vertex class
	 * the vertex is an instance of<br>
	 * "attributes" -> {@code Map<String, Object>} representing the vertex'
	 * attributes (attribute name -> attribute value)
	 *
	 * @param graphNo
	 *            the handle of the graph for which the first vertex of type
	 *            {@code vcName} in <i>Vseq</i> shall be returned
	 * @param vcName
	 *            the name of the vertex class the sought-after vertex shall be
	 *            instance of
	 * @return a {@code Map<String, Object>} with three entries (see method
	 *         description)
	 * @throws XmlRpcException
	 */
	public Map<String, Object> getFirstVertexOfClass(int graphNo, String vcName)
			throws XmlRpcException {
		return createGraphElementMap(graphContainer.getGraph(graphNo)
				.getFirstVertexOfClass(
						(VertexClass) graphContainer.getGraph(graphNo)
								.getSchema().getAttributedElementClass(vcName)));
	}

	/**
	 * Retrieves the vertex after the one with the id {@code vId} in the
	 * sequence of vertices <i>Vseq</i> of the graph indicated by {@code
	 * graphNo}. Returns a {@code Map<String, Object>} with three entries:<br>
	 * <br>
	 * "id" -> {@code Integer} value representing the id of the vertex<br>
	 * "class" -> {@code String} value representing the name of the vertex class
	 * the vertex is an instance of<br>
	 * "attributes" -> {@code Map<String, Object>} representing the vertex'
	 * attributes (attribute name -> attribute value)
	 *
	 * @param graphNo
	 *            the handle of the graph for which the next vertex in
	 *            <i>Vseq</i> after the vertex with {@code vId} shall be
	 *            returned
	 * @param vId
	 *            the id of the vertex whose successor in <i>Vseq</i> shall be
	 *            retrieved
	 * @return a {@code Map<String, Object>} with three entries (see method
	 *         description)
	 * @throws XmlRpcException
	 */
	public Map<String, Object> getNextVertex(int graphNo, int vId)
			throws XmlRpcException {
		Graph graph = graphContainer.getGraph(graphNo);

		return createGraphElementMap(graph.getVertex(vId).getNextVertex());
	}

	/**
	 * Retrieves the vertex after the one with the id {@code vId} in the
	 * sequence of vertices <i>Vseq</i> of the graph indicated by {@code
	 * graphNo}. The vertex must be of type {@code vcName}. Returns a {@code
	 * Map<String, Object>} with three entries:<br>
	 * <br>
	 * "id" -> {@code Integer} value representing the id of the vertex<br>
	 * "class" -> {@code String} value representing the name of the vertex class
	 * the vertex is an instance of<br>
	 * "attributes" -> {@code Map<String, Object>} representing the vertex'
	 * attributes (attribute name -> attribute value)
	 *
	 * @param graphNo
	 *            the handle of the graph for which the next vertex of type
	 *            {@code vcName} in <i>Vseq</i> after the vertex with {@code
	 *            vId} shall be returned
	 * @param vId
	 *            the id of the vertex whose successor of type {@code vcName} in
	 *            <i>Vseq</i> shall be retrieved
	 * @param vcName
	 *            the name of the vertex class the sought-after vertex shall be
	 *            instance of
	 * @return a {@code Map<String, Object>} with three entries (see method
	 *         description)
	 * @throws XmlRpcException
	 */
	public Map<String, Object> getNextVertexOfClass(int graphNo, int vId,
			String vcName) throws XmlRpcException {
		Graph graph = graphContainer.getGraph(graphNo);

		return createGraphElementMap(graph.getVertex(vId).getNextVertexOfClass(
				(VertexClass) graph.getSchema().getAttributedElementClass(
						vcName)));
	}

	/**
	 * Retrieves the first edge in the sequence of incident edges <i>Iseq</i> of
	 * the vertex with {@code vId} in the graph indicated by {@code graphNo}.
	 * Returns a {@code Map<String, Object>} with three entries:<br>
	 * <br>
	 * "id" -> {@code Integer} value representing the id of the edge<br>
	 * "class" -> {@code String} value representing the name of the edge class
	 * the edge is an instance of<br>
	 * "attributes" -> {@code Map<String, Object>} representing the edge's
	 * attributes (attribute name -> attribute value)
	 *
	 * @param graphNo
	 *            the handle of the graph for which the first edge in
	 *            <i>Eseq</i> shall be returned
	 * @param vId
	 *            the id of the vertex whose first edge in <i>Iseq(vId)</i>
	 *            shall be retrieved
	 * @return a {@code Map<String, Object>} with three entries (see method
	 *         description)
	 * @throws XmlRpcException
	 */
	public Map<String, Object> getFirstEdge(int graphNo, int vId)
			throws XmlRpcException {
		Graph graph = graphContainer.getGraph(graphNo);

		return createGraphElementMap(graph.getVertex(vId).getFirstEdge());
	}

	/**
	 * Retrieves the first edge in the sequence of incident edges <i>Iseq</i> of
	 * the vertex with {@code vId} in the graph indicated by {@code graphNo}.
	 * The edge must be of type {@code ecName}. Returns a {@code Map<String,
	 * Object>} with three entries:<br>
	 * <br>
	 * "id" -> {@code Integer} value representing the id of the edge<br>
	 * "class" -> {@code String} value representing the name of the edge class
	 * the edge is an instance of<br>
	 * "attributes" -> {@code Map<String, Object>} representing the edge's
	 * attributes (attribute name -> attribute value)
	 *
	 * @param graphNo
	 *            the handle of the graph for which the first edge of type
	 *            {@code ecName} in <i>Iseq(vId)</i> shall be returned
	 * @param vId
	 *            the id of the vertex whose first edge of class {@code ecName}
	 *            in <i>Iseq(vId)</i> shall be retrieved
	 * @param ecName
	 *            the name of the edge class the sought-after edge shall be
	 *            instance of
	 * @return a {@code Map<String, Object>} with three entries (see method
	 *         description)
	 * @throws XmlRpcException
	 */
	public Map<String, Object> getFirstEdgeOfClass(int graphNo, int vId,
			String ecName) throws XmlRpcException {
		Graph graph = graphContainer.getGraph(graphNo);

		return createGraphElementMap(graph.getVertex(vId)
				.getFirstEdgeOfClass(
						(EdgeClass) graph.getSchema()
								.getAttributedElementClass(ecName)));
	}

	/**
	 * Retrieves the edge after the one with the id {@code eId} in the sequence
	 * of incident edges <i>Iseq</i> of a vertex in the graph indicated by
	 * {@code graphNo}. Returns a {@code Map<String, Object>} with three
	 * entries:<br>
	 * <br>
	 * "id" -> {@code Integer} value representing the id of the edge<br>
	 * "class" -> {@code String} value representing the name of the edge class
	 * the edge is an instance of<br>
	 * "attributes" -> {@code Map<String, Object>} representing the edge's
	 * attributes (attribute name -> attribute value)
	 *
	 * @param graphNo
	 *            the handle of the graph which contains the edge with {@code
	 *            eId}
	 * @param eId
	 *            the id of the edge whose successor in <i>Iseq</i> shall be
	 *            retrieved
	 * @return a {@code Map<String, Object>} with three entries (see method
	 *         description)
	 * @throws XmlRpcException
	 */
	public Map<String, Object> getNextEdge(int graphNo, int eId)
			throws XmlRpcException {
		Graph graph = graphContainer.getGraph(graphNo);

		return createGraphElementMap(graph.getEdge(eId).getNextEdge());
	}

	/**
	 * Retrieves the edge after the one with the id {@code eId} in the sequence
	 * of incident edges <i>Iseq</i> of a vertex in the graph indicated by
	 * {@code graphNo}. The edge must be of type {@code ecName}. Returns a
	 * {@code Map<String, Object>} with three entries:<br>
	 * <br>
	 * "id" -> {@code Integer} value representing the id of the edge<br>
	 * "class" -> {@code String} value representing the name of the edge class
	 * the edge is an instance of<br>
	 * "attributes" -> {@code Map<String, Object>} representing the edge's
	 * attributes (attribute name -> attribute value)
	 *
	 * @param graphNo
	 *            the handle of the graph which contains the edge with {@code
	 *            eId}
	 * @param eId
	 *            the id of the edge whose successor of type {@code ecName} in
	 *            <i>Iseq</i> shall be retrieved
	 * @param ecName
	 *            the name of the edge class the sought-after edge shall be
	 *            instance of
	 * @return a {@code Map<String, Object>} with three entries (see method
	 *         description)
	 * @throws XmlRpcException
	 */
	public Map<String, Object> getNextEdgeOfClass(int graphNo, int eId,
			String ecName) throws XmlRpcException {
		Graph graph = graphContainer.getGraph(graphNo);

		return createGraphElementMap(graph.getEdge(eId)
				.getNextEdgeOfClass(
						(EdgeClass) graph.getSchema()
								.getAttributedElementClass(ecName)));
	}

	/**
	 * Retrieves the first edge in the sequence of edges <i>Eseq</i> of the
	 * graph indicated by {@code graphNo}. Returns a {@code Map<String, Object>}
	 * with three entries:<br>
	 * <br>
	 * "id" -> {@code Integer} value representing the id of the edge<br>
	 * "class" -> {@code String} value representing the name of the edge class
	 * the edge is an instance of<br>
	 * "attributes" -> {@code Map<String, Object>} representing the edge's
	 * attributes (attribute name -> attribute value)
	 *
	 * @param graphNo
	 *            the handle of the graph for which the first edge in
	 *            <i>Eseq</i> shall be returned
	 * @return a {@code Map<String, Object>} with three entries (see method
	 *         description)
	 * @throws XmlRpcException
	 */
	public Map<String, Object> getFirstEdgeInGraph(int graphNo)
			throws XmlRpcException {
		return createGraphElementMap(graphContainer.getGraph(graphNo)
				.getFirstEdgeInGraph());
	}

	/**
	 * Retrieves the first edge in the sequence of edges <i>Eseq</i> of the
	 * graph indicated by {@code graphNo}. The edge must be of type {@code
	 * ecName}. Returns a {@code Map<String, Object>} with three entries:<br>
	 * <br>
	 * "id" -> {@code Integer} value representing the id of the edge<br>
	 * "class" -> {@code String} value representing the name of the edge class
	 * the edge is an instance of<br>
	 * "attributes" -> {@code Map<String, Object>} representing the edge's
	 * attributes (attribute name -> attribute value)
	 *
	 * @param graphNo
	 *            the handle of the graph for which the first edge of type
	 *            {@code ecName} in <i>Eseq</i> shall be returned
	 * @param ecName
	 *            the name of the edge class the sought-after edge shall be
	 *            instance of
	 * @return a {@code Map<String, Object>} with three entries (see method
	 *         description)
	 * @throws XmlRpcException
	 */
	public Map<String, Object> getFirstEdgeOfClassInGraph(int graphNo,
			String ecName) throws XmlRpcException {
		Graph graph = graphContainer.getGraph(graphNo);

		return createGraphElementMap(graph
				.getFirstEdgeOfClassInGraph((EdgeClass) graph.getSchema()
						.getAttributedElementClass(ecName)));
	}

	/**
	 * Retrieves the edge after the one with the id {@code eId} in the sequence
	 * of edges <i>Eseq</i> of the graph indicated by {@code graphNo}. Returns a
	 * {@code Map<String, Object>} with three entries:<br>
	 * <br>
	 * "id" -> {@code Integer} value representing the id of the edge<br>
	 * "class" -> {@code String} value representing the name of the edge class
	 * the edge is an instance of<br>
	 * "attributes" -> {@code Map<String, Object>} representing the edge's
	 * attributes (attribute name -> attribute value)
	 *
	 * @param graphNo
	 *            the handle of the graph for which the next edge in <i>Eseq</i>
	 *            after the edge with {@code eId} shall be returned
	 * @param eId
	 *            the id of the edge whose successor in <i>Eseq</i> shall be
	 *            retrieved
	 * @return a {@code Map<String, Object>} with three entries (see method
	 *         description)
	 * @throws XmlRpcException
	 */
	public Map<String, Object> getNextEdgeInGraph(int graphNo, int eId)
			throws XmlRpcException {
		Graph graph = graphContainer.getGraph(graphNo);

		return createGraphElementMap(graph.getEdge(eId).getNextEdgeInGraph());
	}

	/**
	 * Retrieves the edge after the one with the id {@code eId} in the sequence
	 * of edges <i>Eseq</i> of the graph indicated by {@code graphNo}. The edge
	 * must be of type {@code ecName}. Returns a {@code Map<String, Object>}
	 * with three entries:<br>
	 * <br>
	 * "id" -> {@code Integer} value representing the id of the edge<br>
	 * "class" -> {@code String} value representing the name of the edge class
	 * the edge is an instance of<br>
	 * "attributes" -> {@code Map<String, Object>} representing the edge's
	 * attributes (attribute name -> attribute value)
	 *
	 * @param graphNo
	 *            the handle of the graph for which the next edge of type
	 *            {@code ecName} in <i>Eseq</i> after the edge with {@code eId}
	 *            shall be returned
	 * @param eId
	 *            the id of the edge whose successor of type {@code ecName} in
	 *            <i>Eseq</i> shall be retrieved
	 * @param ecName
	 *            the name of the edge class the sought-after edge shall be
	 *            instance of
	 * @return a {@code Map<String, Object>} with three entries (see method
	 *         description)
	 * @throws XmlRpcException
	 */
	public Map<String, Object> getNextEdgeOfClassInGraph(int graphNo, int eId,
			String ecName) throws XmlRpcException {
		Graph graph = graphContainer.getGraph(graphNo);

		return createGraphElementMap(graph.getEdge(eId)
				.getNextEdgeOfClassInGraph(
						(EdgeClass) graph.getSchema()
								.getAttributedElementClass(ecName)));
	}

	/**
	 * Returns the degree, i.e. the number of incident edges of vertex {@code
	 * vId} in graph {@code graphNo}.
	 *
	 * @param graphNo
	 *            the handle of the graph which contains the vertex with the id
	 *            {@code vId}
	 * @param vId
	 *            the id of the vertex whose degree shall be returned
	 * @return the degree of the vertex with id {@code vId}
	 */
	public int getDegree(int graphNo, int vId) {
		Graph graph = graphContainer.getGraph(graphNo);

		return graph.getVertex(vId).getDegree();
	}

	/**
	 * Retrieves the vertex on the "from" side of the edge {@code eId} of the
	 * graph pointed to by {@code graphNo}. Returns a {@code Map<String,
	 * Object>} with three entries:<br>
	 * <br>
	 * "id" -> {@code Integer} value representing the id of the edge<br>
	 * "class" -> {@code String} value representing the name of the edge class
	 * the edge is an instance of<br>
	 * "attributes" -> {@code Map<String, Object>} representing the edge's
	 * attributes (attribute name -> attribute value)
	 *
	 * @param graphNo
	 *            the handle of the graph which contains the edge with the id
	 *            {@code eId}
	 * @param eId
	 *            the id of the edge whose vertex on the "from" side shall be
	 *            retrieved
	 * @return a {@code Map<String, Object>} with three entries (see method
	 *         description)
	 * @throws XmlRpcException
	 */
	public Map<String, Object> getAlpha(int graphNo, int eId)
			throws XmlRpcException {
		Graph graph = graphContainer.getGraph(graphNo);

		return createGraphElementMap(graph.getEdge(eId).getAlpha());
	}

	/**
	 * Retrieves the vertex on the "to" side of the edge {@code eId} of the
	 * graph pointed to by {@code graphNo}. Returns a {@code Map<String,
	 * Object>} with three entries:<br>
	 * <br>
	 * "id" -> {@code Integer} value representing the id of the edge<br>
	 * "class" -> {@code String} value representing the name of the edge class
	 * the edge is an instance of<br>
	 * "attributes" -> {@code Map<String, Object>} representing the edge's
	 * attributes (attribute name -> attribute value)
	 *
	 * @param graphNo
	 *            the handle of the graph which contains the edge with the id
	 *            {@code eId}
	 * @param eId
	 *            the id of the edge whose vertex on the "to" side shall be
	 *            retrieved
	 * @return a {@code Map<String, Object>} with three entries (see method
	 *         description)
	 * @throws XmlRpcException
	 */
	public Map<String, Object> getOmega(int graphNo, int eId)
			throws XmlRpcException {
		Graph graph = graphContainer.getGraph(graphNo);

		return createGraphElementMap(graph.getEdge(eId).getOmega());
	}

	/**
	 * Sets the vertex with the id {@code vId} as the vertex on the "from" side
	 * of the edge {@code eId} of the graph pointed to by {@code graphNo}.
	 * Returns a {@code Map<String, Object>} with three entries:<br>
	 * <br>
	 * "id" -> {@code Integer} value representing the id of the vertex<br>
	 * "class" -> {@code String} value representing the name of the vertex class
	 * the vertex is an instance of<br>
	 * "attributes" -> {@code Map<String, Object>} representing the vertex'
	 * attributes (attribute name -> attribute value)
	 *
	 * @param graphNo
	 *            the handle of the graph which contains the edge with the id
	 *            {@code eId}
	 * @param eId
	 *            the id of the edge whose vertex on the "from" side shall be
	 *            set
	 * @param vId
	 *            the id of the vertex which shall be the vertex on the "from"
	 *            side of the edge with {@code eId}
	 * @return a {@code Map<String, Object>} with three entries (see method
	 *         description)
	 * @throws XmlRpcException
	 */
	public Map<String, Object> setAlpha(int graphNo, int eId, int vId)
			throws XmlRpcException {
		Graph graph = graphContainer.getGraph(graphNo);

		graph.getEdge(eId).setAlpha(graph.getVertex(vId));

		return createGraphElementMap(graph.getVertex(vId));
	}

	/**
	 * Sets the vertex with the id {@code vId} as the vertex on the "to" side of
	 * the edge {@code eId} of the graph pointed to by {@code graphNo}. Returns
	 * a {@code Map<String, Object>} with three entries:<br>
	 * <br>
	 * "id" -> {@code Integer} value representing the id of the vertex<br>
	 * "class" -> {@code String} value representing the name of the vertex class
	 * the vertex is an instance of<br>
	 * "attributes" -> {@code Map<String, Object>} representing the vertex'
	 * attributes (attribute name -> attribute value)
	 *
	 * @param graphNo
	 *            the handle of the graph which contains the edge with the id
	 *            {@code eId}
	 * @param eId
	 *            the id of the edge whose vertex on the "to" side shall be set
	 * @param vId
	 *            the id of the vertex which shall be the vertex on the "to"
	 *            side of the edge with {@code eId}
	 * @return a {@code Map<String, Object>} with three entries (see method
	 *         description)
	 * @throws XmlRpcException
	 */
	public Map<String, Object> setOmega(int graphNo, int eId, int vId)
			throws XmlRpcException {
		Graph graph = graphContainer.getGraph(graphNo);

		graph.getEdge(eId).setOmega(graph.getVertex(vId));

		return createGraphElementMap(graph.getVertex(vId));
	}

	/**
	 * Puts the vertex with the id {@code source} immediately after the vertex
	 * with the id {@code target} in the sequence of vertices <i>Vseq</i> of the
	 * graph pointed to by {@code graphNo}. Returns a {@code Map<String,
	 * Object>} with three entries:<br>
	 * <br>
	 * "id" -> {@code Integer} value representing the id of the vertex {@code
	 * source}<br>
	 * "class" -> {@code String} value representing the name of the vertex class
	 * the vertex {@code source} is an instance of<br>
	 * "attributes" -> {@code Map<String, Object>} representing the attributes
	 * of the vertex {@code source} (attribute name -> attribute value)
	 *
	 * @param graphNo
	 *            the handle of the graph in which the vertex {@code source}
	 *            shall be inserted
	 * @param target
	 *            the id of the vertex after which the vertex {@code source}
	 *            shall be inserted
	 * @param source
	 *            the id of the vertex which shall be inserted after the vertex
	 *            {@code target}
	 * @return a {@code Map<String, Object>} with three entries (see method
	 *         description)
	 * @throws XmlRpcException
	 */
	public Map<String, Object> putAfterVertex(int graphNo, int target,
			int source) throws XmlRpcException {
		Graph graph = graphContainer.getGraph(graphNo);

		graph.getVertex(source).putAfter(graph.getVertex(target));

		return createGraphElementMap(graph.getVertex(source));
	}

	/**
	 * Puts the vertex with the id {@code source} immediately before the vertex
	 * with the id {@code target} in the sequence of vertices <i>Vseq</i> of the
	 * graph pointed to by {@code graphNo}. Returns a {@code Map<String,
	 * Object>} with three entries:<br>
	 * <br>
	 * "id" -> {@code Integer} value representing the id of the vertex {@code
	 * source}<br>
	 * "class" -> {@code String} value representing the name of the vertex class
	 * the vertex {@code source} is an instance of<br>
	 * "attributes" -> {@code Map<String, Object>} representing the attributes
	 * of the vertex {@code source} (attribute name -> attribute value)
	 *
	 * @param graphNo
	 *            the handle of the graph in which the vertex {@code source}
	 *            shall be inserted
	 * @param target
	 *            the id of the vertex before which the vertex {@code source}
	 *            shall be inserted
	 * @param source
	 *            the id of the vertex which shall be inserted before the vertex
	 *            {@code target}
	 * @return a {@code Map<String, Object>} with three entries (see method
	 *         description)
	 * @throws XmlRpcException
	 */
	public Map<String, Object> putBeforeVertex(int graphNo, int target,
			int source) throws XmlRpcException {
		Graph graph = graphContainer.getGraph(graphNo);

		graph.getVertex(source).putBefore(graph.getVertex(target));

		return createGraphElementMap(graph.getVertex(source));
	}

	/**
	 * Puts the edge with the id {@code source} immediately after the edge with
	 * the id {@code target} in the sequence of edges <i>Eseq</i> of the graph
	 * pointed to by {@code graphNo}. Returns a {@code Map<String, Object>} with
	 * three entries:<br>
	 * <br>
	 * "id" -> {@code Integer} value representing the id of the edge {@code
	 * source}<br>
	 * "class" -> {@code String} value representing the name of the edge class
	 * the edge {@code source} is an instance of<br>
	 * "attributes" -> {@code Map<String, Object>} representing the attributes
	 * of the edge {@code source} (attribute name -> attribute value)
	 *
	 * @param graphNo
	 *            the handle of the graph in which the edge {@code source} shall
	 *            be inserted
	 * @param target
	 *            the id of the edge after which the edge {@code source} shall
	 *            be inserted
	 * @param source
	 *            the id of the edge which shall be inserted after the edge
	 *            {@code target}
	 * @return a {@code Map<String, Object>} with three entries (see method
	 *         description)
	 * @throws XmlRpcException
	 */
	public Map<String, Object> putAfterEdgeInGraph(int graphNo, int target,
			int source) throws XmlRpcException {
		Graph graph = graphContainer.getGraph(graphNo);

		graph.getEdge(source).putAfterInGraph(graph.getEdge(target));

		return createGraphElementMap(graph.getEdge(source));
	}

	/**
	 * Puts the edge with the id {@code source} immediately before the edge with
	 * the id {@code target} in the sequence of edges <i>Eseq</i> of the graph
	 * pointed to by {@code graphNo}. Returns a {@code Map<String, Object>} with
	 * three entries:<br>
	 * <br>
	 * "id" -> {@code Integer} value representing the id of the edge {@code
	 * source}<br>
	 * "class" -> {@code String} value representing the name of the edge class
	 * the edge {@code source} is an instance of<br>
	 * "attributes" -> {@code Map<String, Object>} representing the attributes
	 * of the edge {@code source} (attribute name -> attribute value)
	 *
	 * @param graphNo
	 *            the handle of the graph in which the edge {@code source} shall
	 *            be inserted
	 * @param target
	 *            the id of the edge before which the edge {@code source} shall
	 *            be inserted
	 * @param source
	 *            the id of the edge which shall be inserted before the edge
	 *            {@code target}
	 * @return a {@code Map<String, Object>} with three entries (see method
	 *         description)
	 * @throws XmlRpcException
	 */
	public Map<String, Object> putBeforeEdgeInGraph(int graphNo, int target,
			int source) throws XmlRpcException {
		Graph graph = graphContainer.getGraph(graphNo);

		graph.getEdge(source).putBeforeInGraph(graph.getEdge(target));

		return createGraphElementMap(graph.getEdge(source));
	}

	/**
	 * Puts the edge with the id {@code edgeId} immediately after the edge with
	 * the id {@code previousEdgeId} in the sequence of incident edges
	 * <i>Iseq</i> of a vertex in the graph pointed to by {@code graphNo}.
	 * Returns a {@code Map<String, Object>} with three entries:<br>
	 * <br>
	 * "id" -> {@code Integer} value representing the id of the edge {@code
	 * edgeId}<br>
	 * "class" -> {@code String} value representing the name of the edge class
	 * the edge {@code edgeId} is an instance of<br>
	 * "attributes" -> {@code Map<String, Object>} representing the attributes
	 * of the edge {@code edgeId} (attribute name -> attribute value)
	 *
	 * @param graphNo
	 *            the handle of the graph in which the edge {@code edgeId} shall
	 *            be inserted
	 * @param edgeId
	 *            the id of the edge which shall be inserted after the edge
	 *            {@code previousEdgeId}
	 * @param previousEdgeId
	 *            the id of the edge after which the edge {@code edgeId} shall
	 *            be inserted
	 * @return a {@code Map<String, Object>} with three entries (see method
	 *         description)
	 * @throws XmlRpcException
	 */
	public Map<String, Object> putEdgeAfter(int graphNo, int edgeId,
			int previousEdgeId) throws XmlRpcException {
		Graph graph = graphContainer.getGraph(graphNo);

		graph.getEdge(edgeId).putEdgeAfter(graph.getEdge(previousEdgeId));

		return createGraphElementMap(graph.getEdge(edgeId));
	}

	/**
	 * Puts the edge with the id {@code edgeId} immediately before the edge with
	 * the id {@code nextEdgeId} in the sequence of incident edges <i>Iseq</i>
	 * of a vertex in the graph pointed to by {@code graphNo}. Returns a {@code
	 * Map<String, Object>} with three entries:<br>
	 * <br>
	 * "id" -> {@code Integer} value representing the id of the edge {@code
	 * edgeId}<br>
	 * "class" -> {@code String} value representing the name of the edge class
	 * the edge {@code edgeId} is an instance of<br>
	 * "attributes" -> {@code Map<String, Object>} representing the attributes
	 * of the edge {@code edgeId} (attribute name -> attribute value)
	 *
	 * @param graphNo
	 *            the handle of the graph in which the edge {@code edgeId} shall
	 *            be inserted
	 * @param edgeId
	 *            the id of the edge which shall be inserted before the edge
	 *            {@code nextEdgeId}
	 * @param nextEdgeId
	 *            the id of the edge before which the edge {@code edgeId} shall
	 *            be inserted
	 * @return a {@code Map<String, Object>} with three entries (see method
	 *         description)
	 * @throws XmlRpcException
	 */
	public Map<String, Object> putEdgeBefore(int graphNo, int edgeId,
			int nextEdgeId) throws XmlRpcException {
		Graph graph = graphContainer.getGraph(graphNo);

		graph.getEdge(edgeId).putEdgeBefore(graph.getEdge(nextEdgeId));

		return createGraphElementMap(graph.getEdge(edgeId));
	}

	/**
	 * Returns the value of the attribute {@code attrName} of the graph pointed
	 * to by {@code graphNo}.
	 *
	 * @param graphNo
	 *            the handle of the graph whose attribute value shall be
	 *            returned
	 * @param attrName
	 *            the name of the attribute whose value shall be returned
	 * @return the value of the attribute {@code attrName} of graph {@code
	 *         graphNo}
	 * @throws XmlRpcException
	 */
	public Object getGraphAttribute(int graphNo, String attrName)
			throws XmlRpcException {
		Object attrValue = null;

		Graph graph = graphContainer.getGraph(graphNo);
		Domain domain = graph.getAttributedElementClass()
				.getAttribute(attrName).getDomain();

		try {
			attrValue = graph.getAttribute(attrName);
		} catch (NoSuchFieldException e) {
			e.printStackTrace();
			throw new XmlRpcException(e.toString());
		}

		return convertAttribute(domain, attrValue);
	}

	/**
	 * Returns the types of the attributes of the graph pointed to by {@code
	 * graphNo}. The returned attribute types are contained in a {@code
	 * Map<String, String} with mappings (attribute name -> attribute type name)
	 *
	 * @param graphNo
	 *            the handle of the graph whose attribute types shall be
	 *            returned
	 * @return a {@code Map} containing (attribute name -> attribute type name)
	 *         entries
	 * @throws XmlRpcException
	 */
	public Map<String, String> getGraphAttributeTypes(int graphNo) {
		Map<String, String> attributeTypeMap = new HashMap<String, String>(0);
		Set<Attribute> attributeSet;

		attributeSet = graphContainer.getGraph(graphNo)
				.getAttributedElementClass().getAttributeList();

		for (Attribute attribute : attributeSet) {
			attributeTypeMap.put(attribute.getName(), attribute.getDomain()
					.getTGTypeName(null));
		}

		return attributeTypeMap;
	}

	/**
	 * Returns the values of the attributes of the graph pointed to by {@code
	 * graphNo}. The returned attribute values are contained in a {@code
	 * Map<String, Object} with mappings (attribute name -> attribute type
	 * value)
	 *
	 * @param graphNo
	 *            the handle of the graph whose attribute values shall be
	 *            returned
	 * @return a {@code Map} containing (attribute name -> attribute value)
	 *         entries
	 * @throws XmlRpcException
	 */
	public Map<String, Object> getGraphAttributes(int graphNo)
			throws XmlRpcException {
		Map<String, Object> attrMap = new HashMap<String, Object>(0);
		String attrName;

		Graph graph = graphContainer.getGraph(graphNo);

		try {
			for (Attribute attr : graph.getAttributedElementClass()
					.getAttributeList()) {
				attrName = attr.getName();
				attrMap.put(attrName, convertAttribute(attr.getDomain(), graph
						.getAttribute(attrName)));
			}
		} catch (NoSuchFieldException e) {
			e.printStackTrace();
			throw new XmlRpcException(e.toString());
		}

		return attrMap;
	}

	/**
	 * Sets the value of the attribute {@code attrName} of the graph pointed to
	 * by {@code graphNo} to {@code value}.
	 *
	 * @param graphNo
	 *            the handle of the graph whose attribute value shall be set
	 * @param attrName
	 *            the name of the attribute whose value shall be set
	 * @param value
	 *            the value the attribute shall be set to
	 * @return {@code true} if the setting of the attribute was successful,
	 *         {@code false} otherwise
	 * @throws XmlRpcException
	 */
	public boolean setGraphAttribute(int graphNo, String attrName, Object value)
			throws XmlRpcException {
		try {
			graphContainer.getGraph(graphNo).setAttribute(attrName, value);
		} catch (NoSuchFieldException e) {
			e.printStackTrace();
			throw new XmlRpcException(e.toString());
		}

		return true;
	}

	/**
	 * Sets the value of the attribute {@code attrName} of type Integer of the
	 * graph pointed to by {@code graphNo} to {@code value}.
	 *
	 * @param graphNo
	 *            the handle of the graph whose attribute value shall be set
	 * @param attrName
	 *            the name of the attribute whose value shall be set
	 * @param value
	 *            the value the attribute shall be set to
	 * @return {@code true} if the setting of the attribute was successful,
	 *         {@code false} otherwise
	 * @throws XmlRpcException
	 */
	public boolean setGraphAttribute(int graphNo, String attrName, int value)
			throws XmlRpcException {
		return setGraphAttribute(graphNo, attrName, new Integer(value));
	}

	/**
	 * Sets the value of the attribute {@code attrName} of type Double of the
	 * graph pointed to by {@code graphNo} to {@code value}.
	 *
	 * @param graphNo
	 *            the handle of the graph whose attribute value shall be set
	 * @param attrName
	 *            the name of the attribute whose value shall be set
	 * @param value
	 *            the value the attribute shall be set to
	 * @return {@code true} if the setting of the attribute was successful,
	 *         {@code false} otherwise
	 * @throws XmlRpcException
	 */
	public boolean setGraphAttribute(int graphNo, int vId, String attrName,
			double value) throws XmlRpcException {
		return setGraphAttribute(graphNo, attrName, new Double(value));
	}

	/**
	 * Sets the value of the attribute {@code attrName} of type Boolean of the
	 * graph pointed to by {@code graphNo} to {@code value}.
	 *
	 * @param graphNo
	 *            the handle of the graph whose attribute value shall be set
	 * @param attrName
	 *            the name of the attribute whose value shall be set
	 * @param value
	 *            the value the attribute shall be set to
	 * @return {@code true} if the setting of the attribute was successful,
	 *         {@code false} otherwise
	 * @throws XmlRpcException
	 */
	public boolean setGraphAttribute(int graphNo, int vId, String attrName,
			boolean value) throws XmlRpcException {
		return setGraphAttribute(graphNo, attrName, new Boolean(value));
	}

	/**
	 * Sets the value of the attribute {@code attrName} of types List or Set of
	 * the graph pointed to by {@code graphNo} to {@code value}.
	 *
	 * @param graphNo
	 *            the handle of the graph whose attribute value shall be set
	 * @param attrName
	 *            the name of the attribute whose value shall be set
	 * @param value
	 *            the value the attribute shall be set to
	 * @return {@code true} if the setting of the attribute was successful,
	 *         {@code false} otherwise
	 * @throws XmlRpcException
	 */
	public boolean setGraphAttribute(int graphNo, int vId, String attrName,
			Object[] value) throws XmlRpcException {
		return setGraphAttribute(graphNo, attrName, value);
	}

	/**
	 * Sets the value of the attribute {@code attrName} of type Record of the
	 * graph pointed to by {@code graphNo} to {@code value}.
	 *
	 * @param graphNo
	 *            the handle of the graph whose attribute value shall be set
	 * @param attrName
	 *            the name of the attribute whose value shall be set
	 * @param value
	 *            the value the attribute shall be set to
	 * @return {@code true} if the setting of the attribute was successful,
	 *         {@code false} otherwise
	 * @throws XmlRpcException
	 */
	public boolean setGraphAttribute(int graphNo, int vId, String attrName,
			Map<String, Object> value) throws XmlRpcException {
		return setGraphAttribute(graphNo, attrName, value);
	}

	/**
	 * Sets the value of the attribute {@code attrName} of type Object of the
	 * graph pointed to by {@code graphNo} to {@code value}.
	 *
	 * @param graphNo
	 *            the handle of the graph whose attribute value shall be set
	 * @param attrName
	 *            the name of the attribute whose value shall be set
	 * @param value
	 *            the value the attribute shall be set to
	 * @return {@code true} if the setting of the attribute was successful,
	 *         {@code false} otherwise
	 * @throws XmlRpcException
	 */
	public boolean setGraphAttribute(int graphNo, int vId, String attrName,
			byte[] value) throws XmlRpcException {
		return setGraphAttribute(graphNo, attrName, value);
	}

	/**
	 * Returns the value of the attribute {@code attrName} of the vertex with
	 * the id {@code vId} contained in the graph pointed to by {@code graphNo}.
	 *
	 * @param graphNo
	 *            the handle of the graph whose attribute value shall be
	 *            returned
	 * @param vId
	 *            the id of the vertex whose attribute value shall be returned
	 * @param attrName
	 *            the name of the attribute whose value shall be returned
	 * @return the value of the attribute {@code attrName} of graph {@code
	 *         graphNo}
	 * @throws XmlRpcException
	 */
	public Object getVertexAttribute(int graphNo, int vId, String attrName)
			throws XmlRpcException {
		Object attrValue = null;

		Vertex vertex = graphContainer.getGraph(graphNo).getVertex(vId);
		Domain domain = vertex.getAttributedElementClass().getAttribute(
				attrName).getDomain();

		try {
			attrValue = vertex.getAttribute(attrName);
		} catch (NoSuchFieldException e) {
			e.printStackTrace();
			throw new XmlRpcException(e.toString());
		}

		return convertAttribute(domain, attrValue);
	}

	/**
	 * Returns the types of the attributes of the vertex with the id {@code vId}
	 * contained in the graph pointed to by {@code graphNo}. The returned
	 * attribute types are contained in a {@code Map<String, String} with
	 * mappings (attribute name -> attribute type name)
	 *
	 * @param graphNo
	 *            the handle of the graph containing the vertex {@code vId}
	 * @param vId
	 *            the id of the vertex whose attribute types shall be returned
	 * @return a {@code Map} containing (attribute name -> attribute type name)
	 *         entries
	 * @throws XmlRpcException
	 */
	public Map<String, String> getVertexAttributeTypes(int graphNo, int vId) {
		Map<String, String> attributeTypeMap = new HashMap<String, String>(0);
		Set<Attribute> attributeSet;

		attributeSet = graphContainer.getGraph(graphNo).getVertex(vId)
				.getAttributedElementClass().getAttributeList();

		for (Attribute attribute : attributeSet) {
			attributeTypeMap.put(attribute.getName(), attribute.getDomain()
					.getTGTypeName(null));
		}

		return attributeTypeMap;
	}

	/**
	 * Returns the values of the attributes of the vertex with the id {@code
	 * vId} contained in the graph pointed to by {@code graphNo}. The returned
	 * attribute values are contained in a {@code Map<String, Object} with
	 * mappings (attribute name -> attribute type value)
	 *
	 * @param graphNo
	 *            the handle of the graph containing the vertex with the id
	 *            {@code vId}
	 * @param vId
	 *            the id of the vertex whose attribute values shall be returned
	 * @return a {@code Map} containing (attribute name -> attribute value)
	 *         entries
	 * @throws XmlRpcException
	 */
	public Map<String, Object> getVertexAttributes(int graphNo, int vId)
			throws XmlRpcException {
		Map<String, Object> attrMap = new HashMap<String, Object>(0);
		String attrName;

		Vertex vertex = graphContainer.getGraph(graphNo).getVertex(vId);

		try {
			for (Attribute attr : vertex.getAttributedElementClass()
					.getAttributeList()) {
				attrName = attr.getName();
				attrMap.put(attrName, convertAttribute(attr.getDomain(), vertex
						.getAttribute(attrName)));
			}
		} catch (NoSuchFieldException e) {
			e.printStackTrace();
			throw new XmlRpcException(e.toString());
		}

		return attrMap;
	}

	/**
	 * Sets the value of the attribute {@code attrName} of the vertex with the
	 * id {@code vId} contained in the graph pointed to by {@code graphNo} to
	 * {@code value}.
	 *
	 * @param graphNo
	 *            the handle of the graph containing the vertex {@code vId}
	 * @param vId
	 *            the id of the vertex whose attribute value shall be set
	 * @param attrName
	 *            the name of the attribute whose value shall be returned
	 * @param value
	 *            the value the attribute shall be set to
	 * @return {@code true} if the setting of the attribute was successful,
	 *         {@code false} otherwise
	 * @throws XmlRpcException
	 */
	public boolean setVertexAttribute(int graphNo, int vId, String attrName,
			Object value) throws XmlRpcException {
		value = convertToJGraLabType(value, graphContainer.getGraph(graphNo)
				.getVertex(vId).getAttributedElementClass().getAttribute(
						attrName).getDomain(), graphContainer.getGraph(graphNo));

		try {
			graphContainer.getGraph(graphNo).getVertex(vId).setAttribute(
					attrName, value);
		} catch (NoSuchFieldException e) {
			e.printStackTrace();
			throw new XmlRpcException(e.toString());
		}

		return true;
	}

	/**
	 * Sets the value of the attribute {@code attrName} of type Integer of the
	 * vertex with the id {@code vId} contained in the graph pointed to by
	 * {@code graphNo} to {@code value}.
	 *
	 * @param graphNo
	 *            the handle of the graph containing the vertex {@code vId}
	 * @param vId
	 *            the id of the vertex whose attribute value shall be set
	 * @param attrName
	 *            the name of the attribute whose value shall be returned
	 * @param value
	 *            the value the attribute shall be set to
	 * @return {@code true} if the setting of the attribute was successful,
	 *         {@code false} otherwise
	 * @throws XmlRpcException
	 */
	public boolean setVertexAttribute(int graphNo, int vId, String attrName,
			int value) throws XmlRpcException {
		return setVertexAttribute(graphNo, vId, attrName, new Integer(value));
	}

	/**
	 * Sets the value of the attribute {@code attrName} of type Double of the
	 * vertex with the id {@code vId} contained in the graph pointed to by
	 * {@code graphNo} to {@code value}.
	 *
	 * @param graphNo
	 *            the handle of the graph containing the vertex {@code vId}
	 * @param vId
	 *            the id of the vertex whose attribute value shall be set
	 * @param attrName
	 *            the name of the attribute whose value shall be returned
	 * @param value
	 *            the value the attribute shall be set to
	 * @return {@code true} if the setting of the attribute was successful,
	 *         {@code false} otherwise
	 * @throws XmlRpcException
	 */
	public boolean setVertexAttribute(int graphNo, int vId, String attrName,
			double value) throws XmlRpcException {
		return setVertexAttribute(graphNo, vId, attrName, new Double(value));
	}

	/**
	 * Sets the value of the attribute {@code attrName} of type Boolean of the
	 * vertex with the id {@code vId} contained in the graph pointed to by
	 * {@code graphNo} to {@code value}.
	 *
	 * @param graphNo
	 *            the handle of the graph containing the vertex {@code vId}
	 * @param vId
	 *            the id of the vertex whose attribute value shall be set
	 * @param attrName
	 *            the name of the attribute whose value shall be returned
	 * @param value
	 *            the value the attribute shall be set to
	 * @return {@code true} if the setting of the attribute was successful,
	 *         {@code false} otherwise
	 * @throws XmlRpcException
	 */
	public boolean setVertexAttribute(int graphNo, int vId, String attrName,
			boolean value) throws XmlRpcException {
		return setVertexAttribute(graphNo, vId, attrName, new Boolean(value));
	}

	/**
	 * Sets the value of the attribute {@code attrName} of types List or Set of
	 * the vertex with the id {@code vId} contained in the graph pointed to by
	 * {@code graphNo} to {@code value}.
	 *
	 * @param graphNo
	 *            the handle of the graph containing the vertex {@code vId}
	 * @param vId
	 *            the id of the vertex whose attribute value shall be set
	 * @param attrName
	 *            the name of the attribute whose value shall be returned
	 * @param value
	 *            the value the attribute shall be set to
	 * @return {@code true} if the setting of the attribute was successful,
	 *         {@code false} otherwise
	 * @throws XmlRpcException
	 */
	public boolean setVertexAttribute(int graphNo, int vId, String attrName,
			Object[] value) throws XmlRpcException {
		return setVertexAttribute(graphNo, vId, attrName, (Object) value);
	}

	/**
	 * Sets the value of the attribute {@code attrName} of type Record of the
	 * vertex with the id {@code vId} contained in the graph pointed to by
	 * {@code graphNo} to {@code value}.
	 *
	 * @param graphNo
	 *            the handle of the graph containing the vertex {@code vId}
	 * @param vId
	 *            the id of the vertex whose attribute value shall be set
	 * @param attrName
	 *            the name of the attribute whose value shall be returned
	 * @param value
	 *            the value the attribute shall be set to
	 * @return {@code true} if the setting of the attribute was successful,
	 *         {@code false} otherwise
	 * @throws XmlRpcException
	 */
	public boolean setVertexAttribute(int graphNo, int vId, String attrName,
			Map<String, Object> value) throws XmlRpcException {
		return setVertexAttribute(graphNo, vId, attrName, (Object) value);
	}

	/**
	 * Sets the value of the attribute {@code attrName} of type Object of the
	 * vertex with the id {@code vId} contained in the graph pointed to by
	 * {@code graphNo} to {@code value}.
	 *
	 * @param graphNo
	 *            the handle of the graph containing the vertex {@code vId}
	 * @param vId
	 *            the id of the vertex whose attribute value shall be set
	 * @param attrName
	 *            the name of the attribute whose value shall be returned
	 * @param value
	 *            the value the attribute shall be set to
	 * @return {@code true} if the setting of the attribute was successful,
	 *         {@code false} otherwise
	 * @throws XmlRpcException
	 */
	public boolean setVertexAttribute(int graphNo, int vId, String attrName,
			byte[] value) throws XmlRpcException {
		return setVertexAttribute(graphNo, vId, attrName, (Object) value);
	}

	/**
	 * Returns the value of the attribute {@code attrName} of the edge with the
	 * id {@code eId} contained in the graph pointed to by {@code graphNo}.
	 *
	 * @param graphNo
	 *            the handle of the graph whose attribute value shall be
	 *            returned
	 * @param eId
	 *            the id of the edge whose attribute value shall be returned
	 * @param attrName
	 *            the name of the attribute whose value shall be returned
	 * @return the value of the attribute {@code attrName} of graph {@code
	 *         graphNo}
	 * @throws XmlRpcException
	 */
	public Object getEdgeAttribute(int graphNo, int eId, String attrName)
			throws XmlRpcException {
		Object attrValue = null;

		Edge edge = graphContainer.getGraph(graphNo).getEdge(eId);
		Domain domain = edge.getAttributedElementClass().getAttribute(attrName)
				.getDomain();

		try {
			attrValue = edge.getAttribute(attrName);
		} catch (NoSuchFieldException e) {
			e.printStackTrace();
			throw new XmlRpcException(e.toString());
		}

		return convertAttribute(domain, attrValue);
	}

	/**
	 * Returns the types of the attributes of the edge with the id {@code eId}
	 * contained in the graph pointed to by {@code graphNo}. The returned
	 * attribute types are contained in a {@code Map<String, String} with
	 * mappings (attribute name -> attribute type name)
	 *
	 * @param graphNo
	 *            the handle of the graph containing the edge {@code eId}
	 * @param eId
	 *            the id of the edge whose attribute types shall be returned
	 * @return a {@code Map} containing (attribute name -> attribute type name)
	 *         entries
	 * @throws XmlRpcException
	 */
	public Map<String, String> getEdgeAttributeTypes(int graphNo, int eId) {
		Map<String, String> attributeTypeMap = new HashMap<String, String>(0);
		Set<Attribute> attributeSet;

		attributeSet = graphContainer.getGraph(graphNo).getEdge(eId)
				.getAttributedElementClass().getAttributeList();

		for (Attribute attribute : attributeSet) {
			attributeTypeMap.put(attribute.getName(), attribute.getDomain()
					.getTGTypeName(null));
		}

		return attributeTypeMap;
	}

	/**
	 * Returns the values of the attributes of the edge with the id {@code eId}
	 * contained in the graph pointed to by {@code graphNo}. The returned
	 * attribute values are contained in a {@code Map<String, Object} with
	 * mappings (attribute name -> attribute type value)
	 *
	 * @param graphNo
	 *            the handle of the graph containing the edge with the id
	 *            {@code eId}
	 * @param eId
	 *            the id of the edge whose attribute values shall be returned
	 * @return a {@code Map} containing (attribute name -> attribute value)
	 *         entries
	 * @throws XmlRpcException
	 */
	public Map<String, Object> getEdgeAttributes(int graphNo, int eId)
			throws XmlRpcException {
		Map<String, Object> attrMap = new HashMap<String, Object>(0);
		String attrName;

		Edge edge = graphContainer.getGraph(graphNo).getEdge(eId);

		try {
			for (Attribute attr : edge.getAttributedElementClass()
					.getAttributeList()) {
				attrName = attr.getName();
				attrMap.put(attrName, convertAttribute(attr.getDomain(), edge
						.getAttribute(attrName)));
			}
		} catch (NoSuchFieldException e) {
			e.printStackTrace();
			throw new XmlRpcException(e.toString());
		}

		return attrMap;
	}

	/**
	 * Sets the value of the attribute {@code attrName} of the edge with the id
	 * {@code vId} contained in the graph pointed to by {@code graphNo} to
	 * {@code value}.
	 *
	 * @param graphNo
	 *            the handle of the graph containing the edge {@code eId}
	 * @param eId
	 *            the id of the edge whose attribute value shall be set
	 * @param attrName
	 *            the name of the attribute whose value shall be returned
	 * @param value
	 *            the value the attribute shall be set to
	 * @return {@code true} if the setting of the attribute was successful,
	 *         {@code false} otherwise
	 * @throws XmlRpcException
	 */
	public boolean setEdgeAttribute(int graphNo, int eId, String attrName,
			Object value) throws XmlRpcException {
		try {
			graphContainer.getGraph(graphNo).getEdge(eId).setAttribute(
					attrName, value);
		} catch (NoSuchFieldException e) {
			e.printStackTrace();
			throw new XmlRpcException(e.toString());
		}

		return true;
	}

	/**
	 * Sets the value of the attribute {@code attrName} of type Integer of the
	 * edge with the id {@code vId} contained in the graph pointed to by {@code
	 * graphNo} to {@code value}.
	 *
	 * @param graphNo
	 *            the handle of the graph containing the edge {@code eId}
	 * @param eId
	 *            the id of the edge whose attribute value shall be set
	 * @param attrName
	 *            the name of the attribute whose value shall be returned
	 * @param value
	 *            the value the attribute shall be set to
	 * @return {@code true} if the setting of the attribute was successful,
	 *         {@code false} otherwise
	 * @throws XmlRpcException
	 */
	public boolean setEdgeAttribute(int graphNo, int eId, String attrName,
			int value) throws XmlRpcException {
		return setEdgeAttribute(graphNo, eId, attrName, new Integer(value));
	}

	/**
	 * Sets the value of the attribute {@code attrName} of type Double of the
	 * edge with the id {@code vId} contained in the graph pointed to by {@code
	 * graphNo} to {@code value}.
	 *
	 * @param graphNo
	 *            the handle of the graph containing the edge {@code eId}
	 * @param eId
	 *            the id of the edge whose attribute value shall be set
	 * @param attrName
	 *            the name of the attribute whose value shall be returned
	 * @param value
	 *            the value the attribute shall be set to
	 * @return {@code true} if the setting of the attribute was successful,
	 *         {@code false} otherwise
	 * @throws XmlRpcException
	 */
	public boolean setEdgeAttribute(int graphNo, int eId, String attrName,
			double value) throws XmlRpcException {
		return setEdgeAttribute(graphNo, eId, attrName, new Double(value));
	}

	/**
	 * Sets the value of the attribute {@code attrName} of type Boolean of the
	 * edge with the id {@code vId} contained in the graph pointed to by {@code
	 * graphNo} to {@code value}.
	 *
	 * @param graphNo
	 *            the handle of the graph containing the edge {@code eId}
	 * @param eId
	 *            the id of the edge whose attribute value shall be set
	 * @param attrName
	 *            the name of the attribute whose value shall be returned
	 * @param value
	 *            the value the attribute shall be set to
	 * @return {@code true} if the setting of the attribute was successful,
	 *         {@code false} otherwise
	 * @throws XmlRpcException
	 */
	public boolean setEdgeAttribute(int graphNo, int eId, String attrName,
			boolean value) throws XmlRpcException {
		return setEdgeAttribute(graphNo, eId, attrName, new Boolean(value));
	}

	/**
	 * Sets the value of the attribute {@code attrName} of types List or Set of
	 * the edge with the id {@code vId} contained in the graph pointed to by
	 * {@code graphNo} to {@code value}.
	 *
	 * @param graphNo
	 *            the handle of the graph containing the edge {@code eId}
	 * @param eId
	 *            the id of the edge whose attribute value shall be set
	 * @param attrName
	 *            the name of the attribute whose value shall be returned
	 * @param value
	 *            the value the attribute shall be set to
	 * @return {@code true} if the setting of the attribute was successful,
	 *         {@code false} otherwise
	 * @throws XmlRpcException
	 */
	public boolean setEdgeAttribute(int graphNo, int eId, String attrName,
			Object[] value) throws XmlRpcException {
		return setEdgeAttribute(graphNo, eId, attrName, (Object) value);
	}

	/**
	 * Sets the value of the attribute {@code attrName} of type Record of the
	 * edge with the id {@code vId} contained in the graph pointed to by {@code
	 * graphNo} to {@code value}.
	 *
	 * @param graphNo
	 *            the handle of the graph containing the edge {@code eId}
	 * @param eId
	 *            the id of the edge whose attribute value shall be set
	 * @param attrName
	 *            the name of the attribute whose value shall be returned
	 * @param value
	 *            the value the attribute shall be set to
	 * @return {@code true} if the setting of the attribute was successful,
	 *         {@code false} otherwise
	 * @throws XmlRpcException
	 */
	public boolean setEdgeAttribute(int graphNo, int eId, String attrName,
			Map<String, Object> value) throws XmlRpcException {
		return setEdgeAttribute(graphNo, eId, attrName, (Object) value);
	}

	/**
	 * Sets the value of the attribute {@code attrName} of type Object of the
	 * edge with the id {@code vId} contained in the graph pointed to by {@code
	 * graphNo} to {@code value}.
	 *
	 * @param graphNo
	 *            the handle of the graph containing the edge {@code eId}
	 * @param eId
	 *            the id of the edge whose attribute value shall be set
	 * @param attrName
	 *            the name of the attribute whose value shall be returned
	 * @param value
	 *            the value the attribute shall be set to
	 * @return {@code true} if the setting of the attribute was successful,
	 *         {@code false} otherwise
	 * @throws XmlRpcException
	 */
	public boolean setEdgeAttribute(int graphNo, int eId, String attrName,
			byte[] value) throws XmlRpcException {
		return setEdgeAttribute(graphNo, eId, attrName, (Object) value);
	}

	/**
	 * For the graph pointed to by {@code graphNo}, returns a {@code Map<String,
	 * Object>} with four entries:<br>
	 * <br>
	 * "handle" -> {@code Integer} value which has to be used to access the
	 * graph<br>
	 * "id" -> {@code String} value representing id (name) of the graph<br>
	 * "class" -> {@code String} value representing the name of the graph class
	 * the created graph is an instance of<br>
	 * "attributes" -> {@code Map<String, Object>} representing the graph's
	 * attributes (attribute name -> attribute value)
	 *
	 * @param graphNo
	 *            the handle of the graph
	 * @return a {@code Map<String, Object>} with four entries (see method
	 *         description)
	 * @throws XmlRpcException
	 */
	private Map<String, Object> createGraphMap(int graphNo)
			throws XmlRpcException {
		Map<String, Object> graphMap = new HashMap<String, Object>(6);

		Graph graph = graphContainer.getGraph(graphNo);

		graphMap.put("handle", graphNo);
		graphMap.put("id", graph.getId());
		graphMap.put("class", graph.getGraphClass().getQualifiedName());
		graphMap.put("attributes", getAttributes(graph));

		return graphMap;
	}

	/**
	 * For the given graph element {@code graphElement}, returns a {@code
	 * Map<String, Object>} with three entries:<br>
	 * <br>
	 * "id" -> {@code Integer} value representing the id of the vertex<br>
	 * "class" -> {@code String} value representing the name of the vertex class
	 * the vertex is an instance of<br>
	 * "attributes" -> {@code Map<String, Object>} representing the vertex'
	 * attributes (attribute name -> attribute value)
	 *
	 * @param graphElement
	 *            an instance of {@code GraphElement}
	 * @return a {@code Map<String, Object>} with three entries (see method
	 *         description)
	 * @throws XmlRpcException
	 */
	private Map<String, Object> createGraphElementMap(GraphElement graphElement)
			throws XmlRpcException {
		Map<String, Object> graphMap = new HashMap<String, Object>(5);

		graphMap.put("id", graphElement.getId());
		graphMap.put("class", graphElement.getAttributedElementClass()
				.getQualifiedName());
		graphMap.put("attributes", getAttributes(graphElement));

		return graphMap;
	}

	/**
	 * Returns the values of the attributes of the given attributed element
	 * {@code ae}. The returned attribute values are contained in a {@code
	 * Map<String, Object} with mappings (attribute name -> attribute type
	 * value)
	 *
	 * @param ae
	 *            the graph element whose attribute values shall be returned
	 * @return a {@code Map} containing (attribute name -> attribute value)
	 *         entries
	 * @throws XmlRpcException
	 */
	private Map<String, Object> getAttributes(AttributedElement ae)
			throws XmlRpcException {
		Map<String, Object> attrMap = new HashMap<String, Object>(0);
		String attrName;

		try {
			for (Attribute attr : ae.getAttributedElementClass()
					.getAttributeList()) {
				attrName = attr.getName();
				attrMap.put(attrName, convertAttribute(attr.getDomain(), ae
						.getAttribute(attrName)));
			}
		} catch (NoSuchFieldException e) {
			e.printStackTrace();
			throw new XmlRpcException(e.toString());
		}

		return attrMap;
	}

	/**
	 * Converts the type of an attribute value {@code attrValue} so that it is
	 * useful for the XML-RPC interface. An attribute value of type...<br>
	 * <br>
	 *
	 * - {@code Integer}, {@code Boolean}, {@code Double} is not modified.<br>
	 *
	 * - {@code Long} is set to {@code Integer.MAX_VALUE} if it is greater than
	 * {@code Integer.MAX_VALUE}.<br>
	 *
	 * - {@code String} is set to {@code ""} if it has a {@code null} value.<br>
	 *
	 * - {@code Object} is converted to a {@code byte} array representing a
	 * Base64 representation.<br>
	 *
	 * - {@code Enum} is converted to a {@code String} value representing the
	 * Enum constant. For a {@code null} value, it is converted to the empty
	 * {@code String ""}.<br>
	 *
	 * - {@code List<baseDomain>} is converted to a {@code List<Object>}. The
	 * list elements are converted according to their type.<br>
	 *
	 * - {@code Set<baseDomain>} is converted to a {@code List<Object>}. The set
	 * elements are converted according to their type.<br>
	 *
	 *- {@code Map<keyDomain, valueDomain>} is converted to a {@code
	 * Map<Object, Object>}. The set elements are converted according to their
	 * type.<br>
	 *
	 * - {@code Record} is converted to a {@code Map<String, Object>}, mapping
	 * the names of the record components to their value. The values are
	 * converted according to their type.<br>
	 *
	 * @param domain
	 *            the domain of the attribute value to be converted
	 * @param attrValue
	 *            the attribute value to be converted
	 * @return the attribute value converted to type useful for the XML-RPC
	 *         interface
	 * @throws XmlRpcException
	 */
	@SuppressWarnings("unchecked")
	private Object convertAttribute(Domain domain, Object attrValue)
			throws XmlRpcException {
		if (domain.toString().startsWith("Enum")) {
			if (attrValue == null) {
				attrValue = "";
			} else {
				attrValue = attrValue.toString();
			}
		} else if (domain.toString().startsWith("Record")) {
			if (attrValue == null) {
				attrValue = new HashMap<String, Object>(0);
			} else {
				attrValue = convertRecord((RecordDomain) domain, attrValue);
			}
		} else if (domain.getTGTypeName(null).startsWith("Map<")) {
			if (attrValue == null) {
				attrValue = new HashMap<Object, Object>(0);
			} else {
				attrValue = convertMap((MapDomain) domain,
						(Map<Object, Object>) attrValue);
			}
		} else if (domain.getTGTypeName(null).startsWith("List<")) {
			if (attrValue == null) {
				attrValue = new ArrayList<Object>(0);
			} else {
				attrValue = convertList((CompositeDomain) domain,
						(List<Object>) attrValue);
			}
		} else if (domain.getTGTypeName(null).startsWith("Set<")) {
			if (attrValue == null) {
				attrValue = new ArrayList<Object>(0);
			} else {
				attrValue = convertList((CompositeDomain) domain,
						new ArrayList<Object>((Set<Object>) attrValue));
			}
		} else if (domain.getTGTypeName(null).equals("Object")) {
			try {
				attrValue = toByteArrayRepresentation(attrValue);
			} catch (IOException e) {
				e.printStackTrace();
				throw new XmlRpcException(e.toString());
			}
		} else if (domain.getTGTypeName(null).equals("Long")) {
			if ((Long) attrValue > Integer.MAX_VALUE) {
				attrValue = Integer.MAX_VALUE;
			}
		} else if (domain.getTGTypeName(null).equals("String")) {
			if (attrValue == null) {
				attrValue = "";
			}
		}

		return attrValue;
	}

	/**
	 * Converts an attribute value {@code attrValue} of type {@code MapDomain}
	 * to a {@code Map<Object, Object>}, mapping the keys of the map to their
	 * value. The values are converted according to their type.
	 *
	 * @param domain
	 *            the {@code MapDomain} of the attribute value to be converted
	 * @param attrValue
	 *            the attribute value to be converted
	 * @return a {@code Map<Object, Object>}, mapping the keys of the map to
	 *         their value
	 * @throws XmlRpcException
	 */
	private Map<Object, Object> convertMap(MapDomain domain,
			Map<Object, Object> attrValue) throws XmlRpcException {
		Map<Object, Object> map = new HashMap<Object, Object>();

		for (Object key : attrValue.keySet()) {
			Object val = attrValue.get(key);
			map.put(convertAttribute(domain.getKeyDomain(), key),
					convertAttribute(domain.getValueDomain(), val));
		}
		return map;
	}

	/**
	 * Converts an attribute value {@code attrValue} of type {@code Record} to a
	 * {@code Map<String, Object>}, mapping the names of the record components
	 * to their value. The values are converted according to their type.
	 *
	 * @param domain
	 *            the {@code RecordDomain} of the attribute value to be
	 *            converted
	 * @param attrValue
	 *            the attribute value to be converted
	 * @return a {@code Map<String, Object>}, mapping the names of the record
	 *         components to their value
	 * @throws XmlRpcException
	 */
	private Map<String, Object> convertRecord(RecordDomain domain,
			Object attrValue) throws XmlRpcException {
		Map<String, Object> recordMap = new HashMap<String, Object>(0);

		try {
			for (Map.Entry<String, Domain> comp : domain.getComponents()
					.entrySet()) {
				recordMap.put(comp.getKey(), convertAttribute(comp.getValue(),
						attrValue.getClass().getField(comp.getKey()).get(
								attrValue)));
			}
		} catch (Exception e) {
			e.printStackTrace();
			throw new XmlRpcException(e.toString());
		}

		return recordMap;
	}

	/**
	 * Converts the elements of an attribute value of type {@code List} so that
	 * it is useful for the XML-RPC interface.
	 *
	 * @param domain
	 *            the {@code ListDomain} of the attribute value to be converted
	 * @param attrValue
	 *            the attribute value to be converted
	 * @return a {@code List<Object>} with list elements compatible to XML-RPC
	 * @throws XmlRpcException
	 */
	private List<Object> convertList(CompositeDomain domain,
			List<Object> attrValue) throws XmlRpcException {
		Object listElement;

		for (ListIterator<Object> i = attrValue.listIterator(); i.hasNext();) {
			listElement = i.next();

			i.set(convertAttribute(domain.getAllComponentDomains().iterator()
					.next(), listElement));
		}

		return attrValue;
	}

	/**
	 * Converts an attribute value recieved by the client to the corresponding
	 * type processable by JGraLab. A {@code value} of type...<br>
	 * <br>
	 * - {@code Integer}, {@code Boolean}, or {@code Double} is not modified.<br>
	 * - {@code String} is converted to an Enum, if {@code domain} is String -
	 * {@code Object[]} is converted to a List or Set - {@code Map<String,
	 * Object>} is converted to a Record
	 *
	 * @param value
	 *            the attribute value received by the client
	 * @param domain
	 *            the attribute's domain in the schema
	 * @param graph
	 *            the graph containing the attribute or the graph containing the
	 *            graph element which contains the attribute
	 * @return the value converted to the corresponding type processable by
	 *         JGraLab
	 * @throws XmlRpcException
	 */
	@SuppressWarnings("unchecked")
	private Object convertToJGraLabType(Object value, Domain domain, Graph graph)
			throws XmlRpcException {
		String prefix = graph.getSchema().getPackagePrefix();

		if (domain.toString().startsWith("Enum")) {
			// value if of type String
			// get M1-Class for Enum and invoke fromString() method
			try {
				Class<?> attrType = Class.forName(prefix + "."
						+ domain.getQualifiedName(), true, M1ClassManager
						.instance());
				value = attrType.getMethod("fromString",
						new Class[] { String.class }).invoke(null, value);
			} catch (Exception e) {
				e.printStackTrace();
				throw new XmlRpcException(e.toString());
			}
		} else if (domain.toString().startsWith("List")
				|| domain.toString().startsWith("Set")) {
			// value is of type Object[]
			Object element;

			// convert the Array to a List
			List<Object> list = new ArrayList<Object>(0);
			Collections.addAll(list, (Object[]) value);

			// call convertToJGraLabType for every list element
			for (ListIterator<Object> i = (list).listIterator(); i.hasNext();) {
				element = i.next();

				i.set(convertToJGraLabType(element, ((CompositeDomain) domain)
						.getAllComponentDomains().toArray(new Domain[0])[0],
						graph));
			}

			// if domain is a Set, convert the List to a Set
			if (domain.toString().startsWith("Set")) {
				value = new HashSet<Object>(list);
			} else {
				value = list;
			}
		} else if (domain.toString().startsWith("Record")) {
			// value is of type Map<String, Object>
			// call convertToJGraLabType for every mapping inside the Map
			for (Map.Entry<String, Object> component : ((Map<String, Object>) value)
					.entrySet()) {
				component.setValue(convertToJGraLabType(component.getValue(),
						((RecordDomain) domain).getDomainOfComponent(component
								.getKey()), graph));
			}

			// get M1-Class for Record and invoke the Constructor
			try {
				Class<?> attrType = Class.forName(prefix + "."
						+ domain.getQualifiedName(), true, M1ClassManager
						.instance());
				value = attrType.getConstructor(new Class<?>[] { Map.class })
						.newInstance(value);
			} catch (Exception e) {
				e.printStackTrace();
				throw new XmlRpcException(e.toString());
			}
		} else if (domain.getTGTypeName(null).equals("Object")) {
			// value is of type Object
			try {
				value = fromByteArrayRepresentation((byte[]) value);
			} catch (Exception e) {
				e.printStackTrace();
				throw new XmlRpcException(e.toString());
			}
		}

		return value;
	}

	/**
	 * Creates a byte array representation of the given {@code Object o}.
	 *
	 * @param o
	 *            the {@code Object} whose Base64 representation shall be
	 *            created
	 * @return a {@code byte} array containing the Base64 representation of
	 *         {@code o}
	 * @throws IOException
	 */
	private byte[] toByteArrayRepresentation(Object o) throws IOException {
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		ObjectOutputStream oos = new ObjectOutputStream(bos);
		oos.writeObject(o);
		oos.close();

		return bos.toByteArray();
	}

	/**
	 * Creates an Object from the given byte array {@code ba}.
	 *
	 * @param ba
	 *            a byte array from which to create the returned {@code Object}
	 * @return an {@code Object}
	 * @throws ClassNotFoundException
	 * @throws IOException
	 */
	private Object fromByteArrayRepresentation(byte[] ba)
			throws ClassNotFoundException, IOException {
		ByteArrayInputStream bis = new ByteArrayInputStream(ba);
		ObjectInputStream ois = new ObjectInputStream(bis);
		Object o = ois.readObject();
		ois.close();

		return o;
	}

}
