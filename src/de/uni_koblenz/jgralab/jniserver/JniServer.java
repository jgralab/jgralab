package de.uni_koblenz.jgralab.jniserver;

import java.lang.reflect.Method;
import java.util.Hashtable;
import java.util.Map;

import de.uni_koblenz.jgralab.Edge;
import de.uni_koblenz.jgralab.Graph;
import de.uni_koblenz.jgralab.GraphException;
import de.uni_koblenz.jgralab.GraphIO;
import de.uni_koblenz.jgralab.Vertex;
import de.uni_koblenz.jgralab.schema.QualifiedName;
import de.uni_koblenz.jgralab.schema.Schema;

public class JniServer {
	private static int keyGenerator;

	/**
	 * the {@code Map} holding the graphs created or loaded via the {@code
	 * JGraLabFacade}
	 */
	private Map<Integer, Graph> graphs;

	public JniServer() {
		keyGenerator = 0;
		graphs = new Hashtable<Integer, Graph>();
	}

	/**
	 * Stores {@code graph} and returns the graphId for accessing the graph.
	 * 
	 * @param graph
	 *            a graph
	 * @return handle for accessing a graph in the Map {@code graphs}
	 */
	private int addGraph(Graph graph) {
		if (graphs.containsValue(graph)) {
			throw new GraphException("Graph was already added before!");
		}
		int key = keyGenerator;
		++keyGenerator;
		graphs.put(key, graph);
		return key;
	}

	/**
	 * Removes the graph with id {@code graphId} from this JniServer.
	 * 
	 * @param graphId
	 *            the id of the graph to be deleted
	 */
	public void deleteGraph(int graphId) {
		graphs.remove(graphId);
	}

	/**
	 * Checks whether a graph with the handle {@code graphId} exists.
	 * 
	 * @param graphId
	 *            the handle for which the existence of a graph shall be checked
	 * @return {@code true} if a graph with handle {@code graphNo} exists,
	 *         {@code false} otherwise
	 */
	public boolean containsGraph(int graphId) {
		return graphs.containsKey(graphId);
	}

	public int createGraph(String schemaName, String graphClassName, int vMax,
			int eMax) {
		Class<?> schemaClass;
		try {
			schemaClass = Class.forName(schemaName);
			Schema schema = (Schema) (schemaClass.getMethod("instance",
					(Class[]) null).invoke(null));

			Method graphCreateMethod = schema
					.getGraphCreateMethod(new QualifiedName(graphClassName));

			Graph g = (Graph) (graphCreateMethod.invoke(null, new Object[] {
					null, vMax, eMax }));
			return addGraph(g);
		} catch (Exception e) {
			throw new GraphException("Exception while creating graph.", e);
		}
	}

	public void saveGraph(int graphId, String fileName) {
		try {
			GraphIO.saveGraphToFile(fileName, graphs.get(graphId), null);
		} catch (Exception e) {
			throw new GraphException("Exception while saving graph.", e);
		}
	}

	public int loadGraph(String fileName) {
		try {
			Graph g = GraphIO.loadGraphFromFile(fileName, null);
			return addGraph(g);
		} catch (Exception e) {
			throw new GraphException("Exception while loading graph.", e);
		}
	}

	public int createVertex(int graphId, String vertexClassName) {
		Graph graph = graphs.get(graphId);
		Class<? extends Vertex> m1Class = graph.getGraphClass().getVertexClass(
				new QualifiedName(vertexClassName)).getM1Class();
		return graph.createVertex(m1Class).getId();
	}

	public void setVertexStringAttribute(int graphId, int vertexId,
			String attributName, String value) {
		try {
			graphs.get(graphId).getVertex(vertexId).setAttribute(attributName,
					value);
		} catch (NoSuchFieldException e) {
			throw new GraphException(e);
		}
	}

	public void setVertexDoubleAttribute(int graphId, int vertexId,
			String attributName, double value) {
		try {
			graphs.get(graphId).getVertex(vertexId).setAttribute(attributName,
					value);
		} catch (NoSuchFieldException e) {
			throw new GraphException(e);
		}
	}

	public void setVertexIntAttribute(int graphId, int vertexId,
			String attributName, int value) {
		try {
			graphs.get(graphId).getVertex(vertexId).setAttribute(attributName,
					value);
		} catch (NoSuchFieldException e) {
			throw new GraphException(e);
		}
	}

	public int createEdge(int graphId, String edgeClassName, int alphaId,
			int omegaId) {
		Graph graph = graphs.get(graphId);
		Class<? extends Edge> m1Class = graph.getGraphClass().getEdgeClass(
				new QualifiedName(edgeClassName)).getM1Class();
		return graph.createEdge(m1Class, graph.getVertex(alphaId),
				graph.getVertex(omegaId)).getId();
	}
}
