package de.uni_koblenz.jgralab.utilities.tgraphbrowser;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Method;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.FutureTask;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import de.uni_koblenz.jgralab.Edge;
import de.uni_koblenz.jgralab.Graph;
import de.uni_koblenz.jgralab.GraphElement;
import de.uni_koblenz.jgralab.GraphIO;
import de.uni_koblenz.jgralab.ProgressFunction;
import de.uni_koblenz.jgralab.Vertex;
import de.uni_koblenz.jgralab.codegenerator.CodeGeneratorConfiguration;
import de.uni_koblenz.jgralab.greql2.evaluator.GreqlEvaluator;
import de.uni_koblenz.jgralab.greql2.exception.EvaluateException;
import de.uni_koblenz.jgralab.greql2.jvalue.JValue;
import de.uni_koblenz.jgralab.greql2.jvalue.JValueImpl;
import de.uni_koblenz.jgralab.greql2.jvalue.JValueSet;
import de.uni_koblenz.jgralab.greql2.jvalue.JValueType;
import de.uni_koblenz.jgralab.schema.EdgeClass;
import de.uni_koblenz.jgralab.schema.VertexClass;

public class StateRepository {

	/**
	 * the maximum of elements which are shown in the breadcrumb bar
	 */
	private static final int NUMBER_OF_ELEMENTS_IN_BREADCRUMBBAR = 10;

	/**
	 * the maximum of elements which are shown in an set in the breadcrumb bar
	 */
	private static final int NUMBER_OF_ELEMENTS_IN_A_SET_IN_BREADCRUMBBAR = 3;

	/**
	 * state of all sessions. The position in this array is the sessionID.
	 * sessions.get(0)==null must always be true.
	 */
	private static ArrayList<State> sessions = new ArrayList<State>();

	/**
	 * The id of the next session
	 */
	private static int nextSessionId = 0;

	/**
	 * The command to call dot.
	 */
	public static String dot;

	/**
	 * maps the name of a method to the corresponding method
	 */
	public static HashMap<String, Method> definedMethods = new HashMap<String, Method>();
	static {
		for (Method method : StateRepository.class.getMethods()) {
			definedMethods.put(method.getName(), method);
		}
	}

	public static ConcurrentHashMap<String, GraphWrapper> usedGraphs = new ConcurrentHashMap<String, GraphWrapper>();

	/**
	 * the workspace
	 */
	private final File workspace;

	/**
	 * Creates a new StateRepository. It initializes sessions and freeSessionId
	 * it it isn't done yet.
	 * 
	 * @param path
	 */
	public StateRepository(File path) {
		workspace = path;
	}

	/**
	 * This method is called, if a GReQL query is typed in.
	 * 
	 * @param sessionId
	 * @param isTableViewShown
	 * @param showAttributes
	 * @param numberPerPage
	 * @param pathLength
	 * @param query
	 * @return
	 */
	public StringBuilder computeGReQLQuery(Integer sessionId,
			Boolean isTableViewShown, Boolean showAttributes,
			Integer numberPerPage, Integer pathLength, String query) {
		State state = getSession(sessionId);
		StringBuilder code = new StringBuilder("function(){\n");
		// evaluate the query
		try {
			JValue result = evaluateGReQL(query, state.getGraph(), null);
			boolean elementsAreDisplayed = false;
			if (result.canConvert(JValueType.COLLECTION)) {
				JValueSet elements = result.toJValueSet();
				boolean containsOnlyVerticesOrEdges = true;
				for (JValue v : elements) {
					if (!v.isVertex() && !v.isEdge()) {
						containsOnlyVerticesOrEdges = false;
					}
				}
				if (containsOnlyVerticesOrEdges) {
					displayJValueSet(sessionId, isTableViewShown,
							showAttributes, numberPerPage, pathLength, state,
							elements, code);
					elementsAreDisplayed = true;
				} else if (elements.isEmpty()) {
					code
							.append("var h3error = document.getElementById(\"h3GReQLError\");\n");
					code
							.append("h3error.innerHTML = \"The query has an empty set as result.\";\n");
				} else {
					code
							.append("var h3error = document.getElementById(\"h3GReQLError\");\n");
					code
							.append("h3error.innerHTML = \"Only VERTEX, EDGE or COLLECTION of vertices or edges is supported.\";\n");
				}
			} else if (result.isVertex()) {
				JValueSet elements = new JValueSet();
				elements.add(result);
				displayJValueSet(sessionId, isTableViewShown, showAttributes,
						numberPerPage, pathLength, state, elements, code);
				elementsAreDisplayed = true;
			} else if (result.isEdge()) {
				JValueSet elements = new JValueSet();
				elements.add(result);
				displayJValueSet(sessionId, isTableViewShown, showAttributes,
						numberPerPage, pathLength, state, elements, code);
				elementsAreDisplayed = true;
			} else {
				code
						.append("var h3error = document.getElementById(\"h3GReQLError\");\n");
				code
						.append("h3error.innerHTML = \"The result is of type ")
						.append(result.getType())
						.append(
								".<br />Only VERTEX, EDGE or a COLLECTION of vertices or edges are supported.\";\n");
			}
			if (elementsAreDisplayed) {
				code.append("cancelGReQL();");
			}
		} catch (EvaluateException e) {
			code
					.append("var h3error = document.getElementById(\"h3GReQLError\");\n");
			String errorMessage = e.getCause() != null ? e.getMessage() + "\n"
					+ e.getCause().getMessage() : e.getMessage();
			errorMessage = errorMessage.replaceAll("\"", "'");
			errorMessage = errorMessage.replaceAll("<", "&lt;");
			errorMessage = errorMessage.replaceAll(">", "&gt;");
			errorMessage = errorMessage.replaceAll("\n", "<br />");
			errorMessage = errorMessage.replaceAll("\r", "");
			code.append("h3error.innerHTML = \"").append("<br />").append(
					errorMessage).append("\";\n");
		}
		state.lastAccess = System.currentTimeMillis();
		code.append("timestamp = ").append(state.lastAccess).append(";\n");
		return code.append("}");
	}

	/**
	 * Evaluates GReQL-queries and returns the result.
	 * 
	 * @param query
	 *            the GReQL-query
	 * @param graph
	 *            the graph on which the query is executed
	 * @return the result of the query
	 */
	synchronized static JValue evaluateGReQL(String query, Graph graph,
			HashMap<String, JValue> boundVars) {
		GreqlEvaluator eval = new GreqlEvaluator(query, graph, boundVars);
		eval.startEvaluation();
		JValue result = eval.getEvaluationResult();
		// GreqlEvaluator.resetOptimizedSyntaxGraphs();
		// GreqlEvaluator.resetGraphIndizes();
		return result;
	}

	/**
	 * This method is called, if the elements to be shown are typed in
	 * explicitly.
	 * 
	 * @param sessionId
	 * @param isTableViewShown
	 * @param showAttributes
	 * @param numberPerPage
	 * @param pathLength
	 * @param content
	 * @return
	 */
	public StringBuilder showTypedElements(Integer sessionId,
			Boolean isTableViewShown, Boolean showAttributes,
			Integer numberPerPage, Integer pathLength, String content) {
		State state = getSession(sessionId);
		// find all elements
		JValueSet elements = new JValueSet();
		StringBuilder notExistingElements = new StringBuilder();
		for (String s : content.split(",")) {
			if (s.startsWith("v")) {
				Vertex element = state.getGraph().getVertex(
						Integer.parseInt(s.substring(1)));
				if (element != null) {
					elements.add(new JValueImpl(element));
				} else {
					notExistingElements
							.append((notExistingElements.length() == 0 ? ""
									: ", ")
									+ s);
				}
			} else {
				Edge element = state.getGraph().getEdge(
						Integer.parseInt(s.substring(1)));
				if (element != null) {
					elements.add(new JValueImpl(element));
				} else {
					notExistingElements
							.append((notExistingElements.length() == 0 ? ""
									: ", ")
									+ s);
				}
			}
		}
		// display the elements
		StringBuilder code = new StringBuilder("function(){\n");
		if (!elements.isEmpty()) {
			if (elements.size() > 1) {
				// there are several elements to show
				displayJValueSet(sessionId, isTableViewShown, showAttributes,
						numberPerPage, pathLength, state, elements, code);
			} else {
				// there is only one entry to show
				JValue element = null;
				for (JValue v : elements) {
					element = v;
				}
				addToBreadcrumbBar(code, state, element, true);
				if (isTableViewShown) {
					GraphElement ge = null;
					if (element.isVertex()) {
						ge = element.toVertex();
					} else {
						ge = element.toEdge();
					}
					code.append("if((areVerticesShown()&&").append(
							element.isEdge()).append(
							")||(!areVerticesShown()&&").append(
							element.isVertex()).append(")){\n");
					code.append("switchTable();\n");
					code.append("}\n");
					new TabularVisualizer().visualizeElements(code, state,
							numberPerPage, showAttributes,
							(element.isVertex() ? "v" : "e") + ge.getId(),
							true, true);
				} else {
					new TwoDVisualizer().visualizeElements(code, state,
							sessionId, workspace.toString(), element,
							showAttributes, pathLength);
				}
			}
		}
		if (notExistingElements.length() > 0) {
			code.append("alert(\"");
			code.append("Following elements do not exist:\\n").append(
					notExistingElements);
			code.append("\");\n");
		}
		state.lastAccess = System.currentTimeMillis();
		code.append("timestamp = ").append(state.lastAccess).append(";\n");
		return code.append("}");
	}

	/**
	 * Displays all elements of the JValueSet <code>elements</code>.
	 * <code>Elements</code> must not be empty.
	 * 
	 * @param sessionId
	 * @param isTableViewShown
	 * @param showAttributes
	 * @param numberPerPage
	 * @param pathLength
	 * @param state
	 * @param elements
	 * @param code
	 */
	private void displayJValueSet(Integer sessionId, Boolean isTableViewShown,
			Boolean showAttributes, Integer numberPerPage, Integer pathLength,
			State state, JValueSet elements, StringBuilder code) {
		state.currentExplicitlyDefinedSet = elements;
		addToBreadcrumbBar(code, state, elements, true);
		if (isTableViewShown) {
			TabularVisualizer tv = new TabularVisualizer();
			tv.calculateVertexListAndEdgeList(state, elements);
			tv
					.visualizeElements(
							code,
							state,
							numberPerPage,
							showAttributes,
							"v"
									+ (state.verticesOfTableView != null ? state.verticesOfTableView[0]
											.getId()
											: ""), false, false);
			tv
					.visualizeElements(
							code,
							state,
							numberPerPage,
							showAttributes,
							"e"
									+ ((state.edgesOfTableView != null)
											&& (state.edgesOfTableView.length > 0) ? state.edgesOfTableView[0]
											.getId()
											: ""), false, false);
			if (state.verticesOfTableView != null) {
				showCorrectTable(code, new JValueImpl(
						state.verticesOfTableView[0]));
			} else if (state.edgesOfTableView != null) {
				showCorrectTable(code,
						new JValueImpl(state.edgesOfTableView[0]));
			}
		} else {
			new TwoDVisualizer().visualizeElements(code, state, sessionId,
					workspace.toString(), elements, showAttributes, pathLength);
		}
	}

	/**
	 * Shows or hides the attributes in the 2D visualization.
	 * 
	 * @param sessionId
	 * @param pathLength
	 * @param showAttributes
	 *            if true, the attributes are shown. Otherwise they are hidden.
	 * @param currentIndex
	 *            the navigationHistory index of the currently shown element
	 * @return
	 */
	public StringBuilder refresh2D(Integer sessionId, Integer pathLength,
			Boolean showAttributes, Integer currentIndex) {
		State state = getSession(sessionId);
		StringBuilder code = new StringBuilder("function(){\n");
		TwoDVisualizer tv = new TwoDVisualizer();
		tv.visualizeElements(code, state, sessionId, workspace.toString(),
				state.navigationHistory.get(currentIndex), showAttributes,
				pathLength);
		state.lastAccess = System.currentTimeMillis();
		code.append("timestamp = ").append(state.lastAccess).append(";\n");
		return code.append("}");
	}

	/**
	 * Displays the chosen element in the 2DView. And shows it in the breadcrumb
	 * bar. This method is called, if the link of an element is clicked.
	 * 
	 * @param sessionId
	 *            the id of this session
	 * @param pathLength
	 *            the length of the path
	 * @param showAttributes
	 *            should the elements be shown
	 * @param elementId
	 *            the id of the element which should be shown
	 * @return
	 */
	public StringBuilder showElementsAs2D(Integer sessionId,
			Integer pathLength, Boolean showAttributes, String elementId) {
		State state = getSession(sessionId);
		int currentElementId = Integer.parseInt(elementId.substring(1));
		JValue currentElement = null;
		if (elementId.charAt(0) == 'v') {
			currentElement = new JValueImpl(state.getGraph().getVertex(
					currentElementId));
		} else {
			currentElement = new JValueImpl(state.getGraph().getEdge(
					currentElementId));
		}
		StringBuilder code = new StringBuilder("function(){\n");
		new TwoDVisualizer().visualizeElements(code, state, sessionId,
				workspace.toString(), currentElement, showAttributes,
				pathLength);
		addToBreadcrumbBar(code, state, currentElement, true);
		state.lastAccess = System.currentTimeMillis();
		code.append("timestamp = ").append(state.lastAccess).append(";\n");
		return code.append("}");
	}

	/**
	 * This method is called, if the view is changed. <br>
	 * The currently selected element of the breadcrumbbar is shown. If the
	 * tableView is shown the hidden table shows the latest element in the
	 * navigationHistory
	 * 
	 * @param sessionId
	 * @param isTableViewShown
	 * @param showAttributes
	 * @param numberPerPage
	 * @param pathLength
	 * @param currentIndex
	 *            the navigationHistory index of the currently shown element
	 * @return
	 */
	public StringBuilder changeView(Integer sessionId,
			Boolean isTableViewShown, Boolean showAttributes,
			Integer numberPerPage, Integer pathLength, Integer currentIndex) {
		State state = getSession(sessionId);
		StringBuilder code = new StringBuilder("function(){\n");
		if (isTableViewShown) {
			code
					.append("document.getElementById(\"h3HowManyElements\").style.display = \"none\";\n");
			TabularVisualizer tv = new TabularVisualizer();
			if (state.navigationHistory.get(currentIndex).isVertex()) {
				tv.calculateVertexListAndEdgeList(state);
				showCorrectTable(code, state.navigationHistory
						.get(currentIndex));
				Vertex current = state.navigationHistory.get(currentIndex)
						.toVertex();
				tv.visualizeElements(code, state, numberPerPage,
						showAttributes, "v" + current.getId(), true, true);
				// find latest edge
				Edge latestEdge = null;
				for (int i = state.navigationHistory.size() - 1; i >= 0; i--) {
					if (state.navigationHistory.get(i).isEdge()) {
						latestEdge = state.navigationHistory.get(i).toEdge();
						break;
					}
				}
				if (latestEdge == null) {
					latestEdge = state.getGraph().getFirstEdgeInGraph();
				}
				tv.visualizeElements(code, state, 20, false, "e"
						+ (latestEdge != null ? latestEdge.getId() : ""),
						false, true);
				code
						.append("document.getElementById(\"h3HowManyVertices\").style.display = \"block\";\n");
				code
						.append("document.getElementById(\"h3HowManyEdges\").style.display = \"none\";\n");
			} else if (state.navigationHistory.get(currentIndex).isEdge()) {
				tv.calculateVertexListAndEdgeList(state);
				showCorrectTable(code, state.navigationHistory
						.get(currentIndex));
				Edge current = state.navigationHistory.get(currentIndex)
						.toEdge();
				tv.visualizeElements(code, state, numberPerPage,
						showAttributes, "e" + current.getId(), true, true);
				// find latest vertex
				Vertex latestVertex = null;
				for (int i = state.navigationHistory.size() - 1; i >= 0; i--) {
					if (state.navigationHistory.get(i).isVertex()) {
						latestVertex = state.navigationHistory.get(i)
								.toVertex();
						break;
					}
				}
				tv.visualizeElements(code, state, 20, false, "v"
						+ (latestVertex != null ? latestVertex.getId() : ""),
						false, true);
				code
						.append("document.getElementById(\"h3HowManyEdges\").style.display = \"block\";\n");
				code
						.append("document.getElementById(\"h3HowManyVertices\").style.display = \"none\";\n");
			} else {
				JValueSet elements = state.navigationHistory.get(currentIndex)
						.toJValueSet();
				state.currentExplicitlyDefinedSet = elements;
				tv.calculateVertexListAndEdgeList(state, elements);
				tv
						.visualizeElements(
								code,
								state,
								numberPerPage,
								showAttributes,
								"v"
										+ (state.verticesOfTableView != null ? state.verticesOfTableView[0]
												.getId()
												: ""), false, false);
				tv
						.visualizeElements(
								code,
								state,
								numberPerPage,
								showAttributes,
								"e"
										+ ((state.edgesOfTableView != null)
												&& (state.edgesOfTableView.length > 0) ? state.edgesOfTableView[0]
												.getId()
												: ""), false, false);
				if (state.verticesOfTableView != null) {
					showCorrectTable(code, new JValueImpl(
							state.verticesOfTableView[0]));
				} else if (state.edgesOfTableView != null) {
					showCorrectTable(code, new JValueImpl(
							state.edgesOfTableView[0]));
				}
			}
		} else {
			code
					.append("document.getElementById(\"h3HowManyElements\").style.display = \"block\";\n");
			code
					.append("document.getElementById(\"h3HowManyVertices\").style.display = \"none\";\n");
			code
					.append("document.getElementById(\"h3HowManyEdges\").style.display = \"none\";\n");
			TwoDVisualizer tv = new TwoDVisualizer();
			tv.visualizeElements(code, state, sessionId, workspace.toString(),
					state.navigationHistory.get(currentIndex), showAttributes,
					pathLength);
		}
		state.lastAccess = System.currentTimeMillis();
		code.append("timestamp = ").append(state.lastAccess).append(";\n");
		return code.append("}");
	}

	/**
	 * Switches the table of the browser if it is needed.
	 * 
	 * @param code
	 * @param currentElement
	 */
	private void showCorrectTable(StringBuilder code, JValue currentElement) {
		String infix = "";
		if (currentElement.isVertex()) {
			infix = "Vertices";
		} else if (currentElement.isEdge()) {
			infix = "Edges";
		}
		code.append("var aShow = document.getElementById(\"aShow")
				.append(infix).append("\");\n");
		code.append("if(aShow.hasAttribute){\n");
		code.append("if(aShow.getAttribute(\"class\")!=\"geklickt\"){\n");
		code.append("switchTable();\n}\n");
		code.append("}else{\n");
		code.append("if(aShow.getAttribute(\"className\")!=\"geklickt\"){\n");
		code.append("switchTable();\n}\n");
		code.append("}\n");
	}

	/**
	 * Sets all <code>deselectedTypes</code> to <code>false</code> and all
	 * selected to <code>true</code>. The adjusted view is sent back.
	 * 
	 * @param id
	 *            the id of this session
	 * @param isTableViewShown
	 *            true, iff the tableView is shown
	 * @param deselectedVertexTypes
	 *            the String, which contains the deselected vertex types
	 * @param deselectedEdgeTypes
	 *            the String, which contains the deselected edge types
	 * @param currentVertex
	 *            the id of the current vertex
	 * @param currentEdge
	 *            the id of the current edge
	 * @param currentIndex
	 *            the currently selected element of the navigation history
	 * @return
	 */
	public StringBuilder refreshViewAfterTypeSubmit(Integer id,
			Boolean isTableViewShown, String deselectedVertexTypes,
			String deselectedEdgeTypes, Boolean showAttributes,
			Integer numberPerPage, Integer pathLength, String currentVertex,
			String currentEdge, Integer currentIndex) {
		State state = getSession(id);
		for (VertexClass type : state.selectedVertexClasses.keySet()) {
			state.selectedVertexClasses.put(type, !deselectedVertexTypes
					.contains("#" + type.getQualifiedName() + "#"));
		}
		for (EdgeClass type : state.selectedEdgeClasses.keySet()) {
			state.selectedEdgeClasses.put(type, !deselectedEdgeTypes
					.contains("#" + type.getQualifiedName() + "#"));
		}
		String curVertex = currentVertex;
		String curEdge = currentEdge;
		if (state.currentExplicitlyDefinedSet != null) {
			new TabularVisualizer().calculateVertexListAndEdgeList(state,
					state.currentExplicitlyDefinedSet);
			curVertex = "v"
					+ (state.verticesOfTableView != null ? state.verticesOfTableView[0]
							.getId()
							: "");
			curEdge = "e"
					+ (state.edgesOfTableView != null ? state.edgesOfTableView[0]
							.getId()
							: "");
		} else {
			new TabularVisualizer().calculateVertexListAndEdgeList(state);
		}
		StringBuilder code = new StringBuilder("function(){\n");
		if (!state.navigationHistory.isEmpty()) {
			if (isTableViewShown) {
				new TabularVisualizer().visualizeElements(code, state,
						numberPerPage, showAttributes, curVertex, false,
						state.currentExplicitlyDefinedSet == null);
				// refresh the hidden table
				new TabularVisualizer().visualizeElements(code, state,
						numberPerPage, showAttributes, curEdge, false,
						state.currentExplicitlyDefinedSet == null);
			} else {
				new TwoDVisualizer().visualizeElements(code, state, id,
						workspace.toString(), state.navigationHistory
								.get(currentIndex), showAttributes, pathLength);
			}
		}
		state.lastAccess = System.currentTimeMillis();
		code.append("timestamp = ").append(state.lastAccess).append(";\n");
		return code.append("}");
	}

	/**
	 * Shows an element of the navigation history.
	 * 
	 * @param id
	 *            the sessionId
	 * @param indexOfNavigationHistory
	 *            the index of the element in the navigation history
	 * @param isTableShown
	 *            if true the element is shown in the table view. Otherwise in
	 *            the 2D view.
	 * @param showAttributes
	 *            if true the attributes are shown in the table
	 * @param numberPerPage
	 *            the number of elements which are shown on one page
	 * @param pathLength
	 *            the length of the displayed path
	 * @return
	 */
	public StringBuilder goBackToElement(Integer id,
			Integer indexOfNavigationHistory, Boolean isTableShown,
			Boolean showAttributes, Integer numberPerPage, Integer pathLength) {
		State state = getSession(id);

		// extract the chosen element from the navigationHistory
		JValue currentElement = state.navigationHistory
				.get(indexOfNavigationHistory);
		boolean createVerticesAndEdges = false;
		if (state.currentExplicitlyDefinedSet != null) {
			new TabularVisualizer().calculateVertexListAndEdgeList(state);
			state.currentExplicitlyDefinedSet = null;
			createVerticesAndEdges = true;
		}
		if (currentElement.canConvert(JValueType.COLLECTION)) {
			state.currentExplicitlyDefinedSet = currentElement.toJValueSet();
			new TabularVisualizer().calculateVertexListAndEdgeList(state,
					state.currentExplicitlyDefinedSet);
			if (isTableShown) {
				currentElement = currentElement.toJValueList().get(0);
			}
			createVerticesAndEdges = true;
		}

		state.insertPosition = indexOfNavigationHistory + 1;
		StringBuilder code = new StringBuilder("function(){\n");
		// show selected element
		if (isTableShown) {
			boolean isVertex = currentElement.isVertex();
			code.append("if(").append(isVertex ? "!" : "").append(
					"areVerticesShown()){\n");
			code.append("switchTable();\n");
			code.append("}\n");
			new TabularVisualizer().visualizeElements(code, state,
					numberPerPage, showAttributes, isVertex ? "v"
							+ currentElement.toVertex().getId() : "e"
							+ currentElement.toEdge().getId(), true,
					state.currentExplicitlyDefinedSet == null);
			if (createVerticesAndEdges) {
				new TabularVisualizer()
						.visualizeElements(
								code,
								state,
								numberPerPage,
								showAttributes,
								!isVertex ? "v"
										+ state.getGraph().getFirstVertex()
												.getId()
										: "e"
												+ (state.getGraph()
														.getFirstEdgeInGraph() != null ? state
														.getGraph()
														.getFirstEdgeInGraph()
														.getId()
														: ""), true,
								state.currentExplicitlyDefinedSet == null);
			}
			if (!currentElement.isEdge() && !currentElement.isVertex()) {
				code.append("changeBackgroundColor(\"").append(
						isVertex ? "v" + currentElement.toVertex().getId()
								: "e" + currentElement.toEdge().getId())
						.append("\");");
			}
		} else {
			new TwoDVisualizer().visualizeElements(code, state, id, workspace
					.toString(), currentElement, showAttributes, pathLength);
		}
		addToBreadcrumbBar(code, state, null, false);
		state.lastAccess = System.currentTimeMillis();
		code.append("timestamp = ").append(state.lastAccess).append(";\n");
		return code.append("}");
	}

	/**
	 * This method displays another page of the incidence list.
	 * 
	 * @param id
	 *            the sessionId
	 * @param displayedPage
	 *            the number of the displayed page. The first page has the
	 *            number 1.
	 * @param vertexTdId
	 *            the id of the td, which displayes the incidence list
	 * @return
	 */
	public StringBuilder showIncidencesPage(Integer id, Integer displayedPage,
			String vertexTdId) {
		State state = getSession(id);
		StringBuilder code = new StringBuilder("function (){\n");
		new TabularVisualizer().createIncidentEdges(code, state.getGraph()
				.getVertex(Integer.parseInt(vertexTdId.split("v")[1])),
				state.selectedEdgeClasses, state.selectedVertexClasses,
				displayedPage, vertexTdId);
		state.lastAccess = System.currentTimeMillis();
		code.append("timestamp = ").append(state.lastAccess).append(";\n");
		return code.append("}");
	}

	/**
	 * Displays the chosen page in the tableView. This method is called, if you
	 * click on the &lt;&lt;, &lt;, &gt; or &gt;&gt; buttons.
	 * 
	 * @param id
	 *            the id of this session
	 * @param numberPerPage
	 *            how many elements are shown on one page. -1 if all elements
	 *            should be displayed
	 * @param showAttributes
	 *            should the elements be shown
	 * @param pageNumber
	 *            the number of the page which should be shown. The first page
	 *            has the number 1
	 * @param showVertices
	 *            if true the vertices are shown
	 * @return
	 */
	public StringBuilder showPageInTable(Integer id, Integer numberPerPage,
			Boolean showAttributes, Integer pageNumber, Boolean showVertices) {
		State state = getSession(id);
		String elementId = (showVertices ? "v" : "e")
				+ (showVertices ? state.verticesOfTableView
						: state.edgesOfTableView)[(numberPerPage == -1 ? 0
						: (pageNumber - 1) * numberPerPage)].getId();
		StringBuilder code = new StringBuilder("function (){\n");
		new TabularVisualizer().visualizeElements(code, state, numberPerPage,
				showAttributes, elementId, false,
				state.currentExplicitlyDefinedSet == null);
		code.append("timestamp = ").append(state.lastAccess).append(";\n");
		return code.append("}");
	}

	/**
	 * Displays the chosen element in the tableView. And shows it in the
	 * breadcrumb bar. This method is called, if the link of an element is
	 * clicked.
	 * 
	 * @param id
	 *            the id of this session
	 * @param numberPerPage
	 *            how many elements are shown on one page. -1 if all elements
	 *            should be displayed
	 * @param showAttributes
	 *            should the elements be shown
	 * @param elementId
	 *            the id of the element which should be shown
	 * @return
	 */
	public StringBuilder showElementsAsTable(Integer id, Integer numberPerPage,
			Boolean showAttributes, String elementId) {
		State state = getSession(id);
		boolean createVerticesAndEdges = false;
		boolean isAJValueSetShown = state.currentExplicitlyDefinedSet == null;
		if (!isAJValueSetShown) {
			new TabularVisualizer().calculateVertexListAndEdgeList(state);
			state.currentExplicitlyDefinedSet = null;
			createVerticesAndEdges = true;
		}
		StringBuilder code = new StringBuilder("function(){\n");
		new TabularVisualizer().visualizeElements(code, state, numberPerPage,
				showAttributes, elementId, true, isAJValueSetShown);
		if (createVerticesAndEdges) {
			new TabularVisualizer()
					.visualizeElements(
							code,
							state,
							numberPerPage,
							showAttributes,
							elementId.charAt(0) == 'e' ? "v"
									+ state.getGraph().getFirstVertex().getId()
									: "e"
											+ (state.getGraph()
													.getFirstEdgeInGraph() != null ? state
													.getGraph()
													.getFirstEdgeInGraph()
													.getId()
													: ""), true,
							isAJValueSetShown);
		}
		if (elementId.startsWith("v")) {
			Vertex current = state.getGraph().getVertex(
					Integer.parseInt(elementId.substring(1)));
			addToBreadcrumbBar(code, state, new JValueImpl(current), true);
		} else {
			Edge current = state.getGraph().getEdge(
					Integer.parseInt(elementId.substring(1)));
			addToBreadcrumbBar(code, state, new JValueImpl(current), true);
		}
		code.append("changeBackgroundColor(\"").append(elementId)
				.append("\");");
		code.append("timestamp = ").append(state.lastAccess).append(";\n");
		return code.append("}");
	}

	/**
	 * This method is called, if the number of elements per page is changed, or
	 * if the attributes should be shown or hidden.
	 * 
	 * @param id
	 *            the id of this session
	 * @param numberPerPage
	 *            how many elements are shown on one page. -1 if all elements
	 *            should be displayed
	 * @param showAttributes
	 *            should the elements be shown
	 * @param currentEdge
	 *            the current edge
	 * @param currentVertex
	 *            the current vertex
	 * @return
	 */
	public StringBuilder refreshTable(Integer id, Integer numberPerPage,
			Boolean showAttributes, String currentEdge, String currentVertex) {
		State state = getSession(id);
		StringBuilder code = new StringBuilder("function(){\n");
		new TabularVisualizer().visualizeElements(code, state, numberPerPage,
				showAttributes, currentEdge, false,
				state.currentExplicitlyDefinedSet == null);
		new TabularVisualizer().visualizeElements(code, state, numberPerPage,
				showAttributes, currentVertex, false,
				state.currentExplicitlyDefinedSet == null);
		return code.append("}");
	}

	/**
	 * Hides the loading bar, fills the filter window, initializes the
	 * breadcrumb bar with the first vertex,visualizes the first 20 vertices.
	 * 
	 * @param id
	 * @return
	 */
	public StringBuilder initializeGraphView(Integer id) {
		State state = getSession(id);
		StringBuilder code = new StringBuilder("function(){\n");
		// ## hide divLoadBar
		code
				.append("document.getElementById(\"divLoadBar\").style.display = \"none\";\n");
		// ## show top
		code
				.append("document.getElementById(\"checkShowAttributes\").style.visibility = \"visible\";\n");
		code
				.append("document.getElementById(\"pAttributes\").style.visibility = \"visible\";\n");
		code
				.append("document.getElementById(\"divTextVis\").style.visibility = \"visible\";\n");
		code
				.append("document.getElementById(\"aChangeView\").style.visibility = \"visible\";\n");
		code
				.append("document.getElementById(\"rightOption\").style.visibility = \"visible\";\n");
		if ((dot == null) || dot.isEmpty()
				|| (state.getGraph().getVCount() == 0)) {
			code
					.append("document.getElementById(\"aChangeView\").style.visibility = \"hidden\";\n");
		}
		// ## hide divLoadBar
		code
				.append("document.getElementById(\"divRight\").style.display = \"block\";\n");
		// ## hide divLoadBar
		code
				.append("document.getElementById(\"divFilterWindow\").style.display = \"block\";\n");
		// ## initialize FilterWindow
		new SchemaVisualizer().createSchemaRepresentation(code, state);
		// ## initialize breadcrumb bar
		Vertex firstVertex = state.getGraph().getFirstVertex();
		if (firstVertex != null) {
			addToBreadcrumbBar(code, state, new JValueImpl(firstVertex), true);
		}
		// ## initialize textual view
		TabularVisualizer tv = new TabularVisualizer();
		tv.calculateVertexListAndEdgeList(state);
		tv.visualizeElements(code, state, 20, false, "v"
				+ (firstVertex != null ? firstVertex.getId() : ""), false,
				state.currentExplicitlyDefinedSet == null);
		code.append("changeBackgroundColor(\"v").append(
				firstVertex != null ? firstVertex.getId() : "")
				.append("\");\n");
		tv.visualizeElements(code, state, 20, false, "e"
				+ (state.getGraph().getFirstEdgeInGraph() != null ? state
						.getGraph().getFirstEdgeInGraph().getId() : ""), false,
				state.currentExplicitlyDefinedSet == null);
		code.append("timestamp = ").append(state.lastAccess).append(";\n");
		code.append("resize();\n");
		code.append("resize();\n");// fixes the correct size in FF
		return code.append("}");
	}

	/**
	 * Adds the element with <code>elementId</code> to the breadcrumb bar. If it
	 * is already in the breadcrumb bar every element behind it gets the color
	 * gray.
	 * 
	 * @param id
	 * @param elementId
	 *            normally it is the elementId. But if you go back to an earlier
	 *            entry in the breadcrumb bar it is the index of the
	 *            navigationHistory.
	 * @param isTableShown
	 * @param isNewElement
	 *            It is false, if you want to show an element of the breadcrumb
	 *            bar.
	 * @return
	 */
	public StringBuilder refreshBreadcrumbBar(Integer id, String elementId,
			Boolean isTableShown, Boolean isNewElement) {
		State state = sessions.get(id);
		JValue element = null;
		StringBuilder code = new StringBuilder("function() {\n");
		if (elementId.startsWith("v")) {
			element = new JValueImpl(state.getGraph().getVertex(
					Integer.parseInt(elementId.substring(1))));
			addToBreadcrumbBar(code, state, element, isNewElement);
		} else if (elementId.startsWith("e")) {
			element = new JValueImpl(state.getGraph().getEdge(
					Integer.parseInt(elementId.substring(1))));
			addToBreadcrumbBar(code, state, element, isNewElement);
		} else {
			// go back to the element which has the index elementId in the
			// navigation history
			int currentIndex = Integer.parseInt(elementId);
			JValue currentElement = state.navigationHistory.get(currentIndex);
			state.insertPosition = currentIndex + 1;
			boolean isVertex = currentElement.isVertex();
			if (isTableShown) {
				code.append("if(").append(isVertex ? "!" : "").append(
						"areVerticesShown()){\n");
				code.append("switchTable();\n");
				code.append("}\n");
			}
			addToBreadcrumbBar(code, state, null, isNewElement);
			code.append("current").append(isVertex ? "Vertex" : "Edge").append(
					" = \"").append(
					isVertex ? "v" + currentElement.toVertex().getId() : "e"
							+ Math.abs(currentElement.toEdge().getId()))
					.append("\";\n");
		}
		code.append("timestamp = ").append(state.lastAccess).append(";\n");
		return code.append("}");
	}

	/**
	 * Adds <code>element</code> to the position State.insertPosition of the
	 * navigationHistory of the state. The last
	 * <code>NUMBER_OF_ELEMENTS_IN_BREADCRUMBBAR</code> of the navigationHistory
	 * are shown.
	 * 
	 * @param code
	 * 
	 * @param state
	 *            the current state
	 * @param element
	 *            the JValue which has to be added. If it is null the element at
	 *            State.insertPosition is shown.
	 * @param isNewElement
	 *            It is false, if you want to show an element of the breadcrumb
	 *            bar.
	 * @return the JavaScript commands to create the current breadcrumb bar
	 */
	private StringBuilder addToBreadcrumbBar(StringBuilder code, State state,
			JValue element, Boolean isNewElement) {
		int currentPage = 0;
		code
				.append("var divBreadcrumbBar = document.getElementById(\"divBreadcrumbBar\");\n");
		code.append("divBreadcrumbBar.innerHTML = \"\";\n");
		// create p
		code.append("var breadcrumbBar = document.createElement(\"p\");\n");
		code.append("breadcrumbBar.id = \"pBreadcrumbContent0\";\n");
		code.append("breadcrumbBar.style.display = \"none\";\n");
		code.append("divBreadcrumbBar.appendChild(breadcrumbBar);\n");
		code.append("var newEntry;\n");
		if (element != null) {
			// add element to the navigationHistory
			state.navigationHistory.add(state.insertPosition++, element);
			if ((state.insertPosition < state.navigationHistory.size())
					&& isNewElement) {
				// the element was added in the middle of the navigationHistory
				// delete the following
				for (int i = state.navigationHistory.size() - 1; i >= state.insertPosition; i--) {
					state.navigationHistory.remove(i);
				}
			}
		}
		if (state.navigationHistory.size() <= NUMBER_OF_ELEMENTS_IN_BREADCRUMBBAR) {
			for (int i = 0; i < state.navigationHistory.size(); i++) {
				if (i > 0) {
					// this is not the first element
					code
							.append("var raquo = document.createTextNode(String.fromCharCode(187));\n");
					code.append("breadcrumbBar.appendChild(raquo);\n");
				}
				createBreadcrumbEntry(code, state, i,
						i < state.insertPosition ? "white" : "gray");
			}
		} else {
			// there are more than NUMBER_OF_ELEMENTS_IN_BREADCRUMBBAR elements
			int modul = state.navigationHistory.size()
					% NUMBER_OF_ELEMENTS_IN_BREADCRUMBBAR;
			int pNumber = 0;
			for (int i = 0; i < state.navigationHistory.size(); i++) {
				if ((i != 0)
						&& (i % NUMBER_OF_ELEMENTS_IN_BREADCRUMBBAR == modul)) {
					// start element of a new breadcrumb bar page but not the
					// first one
					// create next p
					code
							.append("breadcrumbBar = document.createElement(\"p\");\n");
					code.append("breadcrumbBar.id = \"pBreadcrumbContent")
							.append(++pNumber).append("\";\n");
					code
							.append("divBreadcrumbBar.appendChild(breadcrumbBar);\n");
					code.append("breadcrumbBar.style.display = \"none\";\n");
					// create ... >>
					code.append("var aBack = document.createElement(\"a\");\n");
					code.append("aBack.innerHTML = \"...\";\n");
					code
							.append(
									"aBack.href = \"javascript:switchBreadcrumbPage('pBreadcrumbContent")
							.append(pNumber).append("','pBreadcrumbContent")
							.append(pNumber - 1).append("');\";\n");
					code.append("breadcrumbBar.appendChild(aBack);\n");
					code
							.append("var raquo = document.createTextNode(String.fromCharCode(187));\n");
					code.append("breadcrumbBar.appendChild(raquo);\n");
				}
				if (i == state.insertPosition - 1) {
					currentPage = pNumber;
				}
				createBreadcrumbEntry(code, state, i,
						i < state.insertPosition ? "white" : "gray");
				if (i != state.navigationHistory.size() - 1) {
					// create >>
					code
							.append("var raquo = document.createTextNode(String.fromCharCode(187));\n");
					code.append("breadcrumbBar.appendChild(raquo);\n");
					if (i % NUMBER_OF_ELEMENTS_IN_BREADCRUMBBAR == (modul - 1 + NUMBER_OF_ELEMENTS_IN_BREADCRUMBBAR)
							% NUMBER_OF_ELEMENTS_IN_BREADCRUMBBAR) {
						// last element of a new breadcrumb bar page but not the
						// last element in the navigationHistory
						// create ...
						code
								.append("var aBack = document.createElement(\"a\");\n");
						code.append("aBack.innerHTML = \"...\";\n");
						code
								.append(
										"aBack.href = \"javascript:switchBreadcrumbPage('pBreadcrumbContent")
								.append(pNumber)
								.append("','pBreadcrumbContent").append(
										pNumber + 1).append("');\";\n");
						code.append("breadcrumbBar.appendChild(aBack);\n");
					}
				}
			}
		}
		code.append("document.getElementById(\"pBreadcrumbContent").append(
				currentPage).append("\").style.display = \"inline\";\n");
		return code;
	}

	/**
	 * Creates a new entry at the end of the breadcrumb bar.
	 * 
	 * @param code
	 *            the JavaScript code
	 * @param state
	 *            the current state
	 * @param i
	 *            the current position in the navigation history
	 * @param colorOfEntry
	 *            the color of the entry
	 */
	private void createBreadcrumbEntry(StringBuilder code, State state, int i,
			String colorOfEntry) {
		StringBuilder elementId = new StringBuilder();
		JValue elem = state.navigationHistory.get(i);
		if (elem.isVertex()) {
			elementId.append("v").append(elem.toVertex().getId());
		} else if (elem.isEdge()) {
			elementId.append("e").append(elem.toEdge().getId());
		} else {
			elementId.append("{");
			boolean first = true;
			int counter = 0;
			JValueSet elemSet = elem.toJValueSet();
			for (JValue v : elemSet) {
				if (!first) {
					elementId.append(", ");
					if (counter >= NUMBER_OF_ELEMENTS_IN_A_SET_IN_BREADCRUMBBAR) {
						elementId.append("...");
						break;
					}
				}
				if (v.isVertex()) {
					elementId.append("v").append(v.toVertex().getId());
				} else {
					elementId.append("e").append(v.toEdge().getId());
				}
				first = false;
				counter++;
			}
			elementId.append("}");
		}
		code.append("newEntry = document.createElement(\"a\");\n");
		code.append("newEntry.href = \"javascript:goBackToElement(").append(i)
				.append(",'").append(elementId).append("');\";\n");
		code.append("newEntry.innerHTML = \"").append(elementId)
				.append("\";\n");
		code.append("newEntry.style.color = \"").append(colorOfEntry).append(
				"\";\n");
		code.append("breadcrumbBar.appendChild(newEntry);\n");
	}

	/**
	 * Sets the html-page to the default values. It let's the Browser check, if
	 * the graph is loaded.
	 * 
	 * @param id
	 * @return
	 */
	public StringBuilder initializeBrowser(Integer id) {
		State state = sessions.get(id);
		StringBuilder code = new StringBuilder("function(){\n");
		// ## initialize top bar
		// delete the shown graphs of the server
		code
				.append("var optgroup = document.getElementById(\"OptgroupServersideGraph\");\n");
		code
				.append("var oldOpt = optgroup.getElementsByTagName(\"option\");\n");
		code.append("for(var i=0; i<oldOpt.length; i++ ){\n");
		code.append("optgroup.removeChild(oldOpt[i]);\n");
		code.append("}\n");
		// create new options for the graphs
		code.append("var childOpt;\n");
		code.append("var optValue;\n");
		code.append("var optText;\n");
		createOptionForGraphs(code, workspace);
		code.append("findPositionOf(\"").append(
				state.graphIdentifier.toString().replace("\\", "/")).append(
				"\");\n");
		// set the shown one as selected
		code
				.append("document.getElementById(\"selectGraph\").selectedIndex = selectedGraphIndex;\n");
		// hide rest of title
		code
				.append("document.getElementById(\"checkShowAttributes\").style.visibility = \"hidden\";\n");
		code
				.append("document.getElementById(\"pAttributes\").style.visibility = \"hidden\";\n");
		code
				.append("document.getElementById(\"divTextVis\").style.visibility = \"hidden\";\n");
		code
				.append("document.getElementById(\"aChangeView\").style.visibility = \"hidden\";\n");
		code
				.append("document.getElementById(\"rightOption\").style.visibility = \"hidden\";\n");
		// deselect checkShowAttributes
		code
				.append("document.getElementById(\"checkShowAttributes\").checked = \"\";\n");
		code
				.append("document.getElementById(\"checkShowAttributes\").style.visible = \"hidden\";\n");
		// show table view and table options
		code
				.append("document.getElementById(\"divTextVis\").style.display = \"inline\";\n");
		code
				.append("document.getElementById(\"div2DVis\").style.display = \"none\";\n");
		code
				.append("document.getElementById(\"divTextGraph\").style.display = \"block\";\n");
		code
				.append("document.getElementById(\"div2DGraph\").style.display = \"none\";\n");
		// set 20 as selected
		code
				.append("document.getElementById(\"selectElementsPerPage\").selectedIndex = 1;\n");
		// set textPathLength to 2
		code
				.append("document.getElementById(\"textPathLength\").value = \"2\";\n");
		// ## set FilterWindow
		code
				.append("document.getElementById(\"h3HowManyVertices\").innerHTML = \"\";\n");
		code
				.append("document.getElementById(\"h3HowManyVertices\").style.display = \"block\";\n");
		code
				.append("document.getElementById(\"h3HowManyEdges\").innerHTML = \"\";\n");
		code
				.append("document.getElementById(\"h3HowManyEdges\").style.display = \"none\";\n");
		code
				.append("document.getElementById(\"h3HowManyElements\").innerHTML = \"\";\n");
		code
				.append("document.getElementById(\"h3HowManyElements\").style.display = \"none\";\n");
		// show vertexClasses
		code.append("changeFilterView('divVertexClass','aVertex');\n");
		// delete Content of divVertexClass and divEdgeClass
		code
				.append("document.getElementById(\"divVertexClass\").innerHTML = \"\";\n");
		code
				.append("document.getElementById(\"divEdgeClass\").innerHTML = \"\";\n");
		// select checkSelectAll
		code
				.append("document.getElementById(\"checkSelectAll\").checked = \"checked\";\n");
		// show default regExpr
		code
				.append("document.getElementById(\"inputRegEx\").value = \"<a case insensitive regular expression>\";\n");
		// ## clearDisplay and cancelGReQL
		code.append("cancelGReQL('aGReQL');\n");
		// ## set textElem to ""
		code.append("document.getElementById(\"textElem\").value = \"\";\n");
		// ## clear pBreadcrumbContent
		code
				.append("document.getElementById(\"pBreadcrumbContent0\").innerHTML = \"\";\n");
		// ## clean up table view
		// clear vertexTable and EdgeTable
		code
				.append("document.getElementById(\"divTextVertex\").innerHTML = \"\";\n");
		code
				.append("document.getElementById(\"divTextEdge\").innerHTML = \"\";\n");
		// set to vertexTable
		code
				.append("var divTextVertex = document.getElementById(\"divTextVertex\");\n");
		code
				.append("var divTextEdge = document.getElementById(\"divTextEdge\");\n");
		code
				.append("var aShowVertices = document.getElementById(\"aShowVertices\");\n");
		code
				.append("var aShowEdges = document.getElementById(\"aShowEdges\");\n");
		code.append("if(divTextVertex.hasAttribute){\n");
		code.append("divTextEdge.style.display = \"none\";\n");
		code.append("divTextVertex.style.display = \"block\";\n");
		code.append("aShowVertices.setAttribute(\"class\",\"geklickt\");\n");
		code.append("aShowEdges.setAttribute(\"class\",\"\");\n");
		code.append("}else{\n");
		code.append("divTextEdge.style.display = \"none\";\n");
		code.append("divTextVertex.style.display = \"block\";\n");
		code
				.append("aShowVertices.setAttribute(\"className\",\"geklickt\");\n");
		code.append("aShowEdges.setAttribute(\"className\",\"\");\n");
		code.append("}\n");
		// ## clean up 2D view
		code
				.append("document.getElementById(\"div2DGraph\").innerHTML = \"\";\n");
		// ## show loadBar
		code
				.append("document.getElementById(\"divLoadBar\").style.display = \"block\";\n");
		code
				.append("document.getElementById(\"loadBarForeground\").style.width = \"0px\";\n");
		code
				.append("document.getElementById(\"loadBarNumber\").innerHTML = \"0 %\";\n");
		// ## set timestamp to new time
		state.lastAccess = System.currentTimeMillis();
		code.append("timestamp = ").append(state.lastAccess).append(";\n");
		// ## initialize asking if graph has loaded
		code.append("loadId = window.setTimeout(\"checkLoad()\", 100);\n");
		return code.append("}");
	}

	/**
	 * If the graph wasn't loaded, the browser should ask again in 1 sec. If the
	 * loading was canceled or an exception occurred an error message is sent
	 * back. Otherwise the graph is loaded.
	 * 
	 * @param id
	 * @return
	 */
	public StringBuilder checkLoading(Integer id) {
		State state = sessions.get(id);
		GraphWrapper currentGraphWrapper = state.getGraphWrapper();
		StringBuilder code = new StringBuilder("function(){\n");
		if (currentGraphWrapper.workingCallable == null) {
			// the current graph was already loaded in another session
			return initializeGraphView(id);
		} else {
			try {
				if (currentGraphWrapper.workingCallable.isDone()) {
					currentGraphWrapper.workingCallable.get();
					if (currentGraphWrapper.excOfWorkingCallable != null) {
						// an exception occured while loading the graph
						Exception e = currentGraphWrapper.excOfWorkingCallable;
						code
								.append(
										"document.getElementById(\"loadError\").innerHTML += \"ERROR:<br />")
								.append(e.toString()).append("\";\n");
					} else if (currentGraphWrapper.graph == null) {
						code
								.append("document.getElementById(\"loadError\").innerHTML += \"ERROR:<br />The graph couldn't be loaded!<br />Probably an OutOfMemoryError occured.\";\n");
					} else {
						// the graph was loaded
						currentGraphWrapper.workingCallable = null;
						return initializeGraphView(id);
					}
				} else if (currentGraphWrapper.workingCallable.isCancelled()) {
					code
							.append("document.getElementById(\"loadError\").innerHTML += \"ERROR:<br />The loading of the graph was canceled!\";\n");
				} else {
					// the graph was not loaded completely
					code
							.append(
									"document.getElementById(\"loadBarForeground\").style.width = \"")
							.append(currentGraphWrapper.progress).append(
									"px\";\n");
					code
							.append(
									"document.getElementById(\"loadBarNumber\").innerHTML = \"")
							.append(currentGraphWrapper.progress / 4).append(
									" %\";\n");
					code
							.append("loadId = window.setTimeout(\"checkLoad()\", 1000);\n");
					state.lastAccess = System.currentTimeMillis();
					code.append("timestamp = ").append(state.lastAccess)
							.append(";\n");
				}
			} catch (Exception e) {
				code
						.append(
								"document.getElementById(\"loadError\").innerHTML += \"ERROR:<br />")
						.append(e.toString()).append("\";\n");
			}
		}
		return code.append("}");
	}

	/**
	 * Creates the options for all the graphs on the server. The selected graph
	 * is the first.
	 * 
	 * @param code
	 * 
	 * @param directory
	 *            the directory to look for graphs
	 * @return
	 */
	private void createOptionForGraphs(StringBuilder code, File directory) {
		for (File f : directory.listFiles()) {
			if (f.exists()
					&& f.isFile()
					&& (f.toString().endsWith(".tg") || f.toString().endsWith(
							".gz"))) {
				// f is a graph file
				code.append("childOpt = document.createElement(\"option\");\n");
				code
						.append("optValue = document.createAttribute(\"value\");\n");
				code.append("optValue.nodeValue = \"").append(
						f.toString().replace("\\", "/")).append("\";\n");
				code.append("childOpt.setAttributeNode(optValue);\n");
				code.append("optText = document.createTextNode(\"").append(
						f.toString().replace(workspace.toString(), "").replace(
								"\\", "/").substring(1)).append("\");\n");
				code.append("childOpt.appendChild(optText);\n");
				code
						.append("insertSortedIntoOption(childOpt,1,optgroup.childNodes.length-1);\n");
			} else if (f.exists() && f.isDirectory()) {
				// search the folder
				createOptionForGraphs(code, f);
			}
		}
	}

	/**
	 * This method is called when the page of the browser is closed or reloaded.
	 * The state of this session is deleted.
	 * 
	 * @param id
	 * @return
	 */
	public StringBuilder closeSession(Integer id) {
		State s = getSession(id);
		if (s != null) {
			s.delete();
		}
		return new StringBuilder("function(){}");
	}

	/**
	 * Deletes the graph <code>path</code>, from the server. The graph must be
	 * in the workspace and end with .tg or .gz.
	 * 
	 * @param graph
	 *            the path to the graph
	 * @return an error message for the browser, if delete fails. Otherwise the
	 *         remaining graphs are shown.
	 */
	public StringBuilder deleteGraph(String path) {
		String graph = "";
		try {
			graph = URLDecoder.decode(path, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
			return returnError(e.toString());
		}
		if (!graph.startsWith(workspace.toString().replace("\\", "/"))) {
			// graph isn't in the workspace
			return returnError(graph + " is not in the workspace!"
					+ workspace.toString().replace("\\", "/"));
		}
		if (!graph.endsWith(".tg") && !graph.endsWith(".gz")) {
			// graph isn't a .tg-file
			return returnError(graph + " is not a graph!");
		}
		File toDelete = new File(graph);
		if (toDelete.delete()) {
			return showGraphsOfServer();
		} else {
			return returnError(graph + " could not be deleted.");
		}
	}

	/**
	 * If there is no workspace, an error message is returned. If there are no
	 * graphs in the workspace it is shown in divserver. Else the graphs are
	 * shown in divserver.
	 * 
	 * @return
	 */
	public StringBuilder showGraphsOfServer() {
		if (workspace != null) {
			StringBuilder code = new StringBuilder("function() {\n");
			code.append("var div=document.getElementById(\"divserver\");\n");
			code.append("div.innerHTML = \"\";\n");
			code.append("div.appendChild(document.createElement(\"br\"));\n");
			StringBuilder list = new StringBuilder();
			if (!createListOfGraphs(list, workspace)) {
				code.append("var h2 = document.createElement(\"h2\");\n");
				code
						.append("h2.innerHTML = \"There are no graphs on the server.\";\n");
				code.append("div.appendChild(h2);\n");
			} else {
				code.append("var h3 = document.createElement(\"h3\");\n");
				code
						.append("h3.innerHTML = \"Choose a graph from the server:\";\n");
				code.append("div.appendChild(h3);\n");
				code
						.append("div.appendChild(document.createElement(\"br\"));\n");
				code.append("var parentUl = document.createElement(\"ul\");\n");
				code.append("div.appendChild(parentUl);\n");
				code.append("parentUl.id = \"parentUl\";");
				code.append(list);
			}
			return code.append("}");
		} else {
			// Server hasn't a workspace.
			return returnError("This server doesn't allow to load graphs of it.");
		}
	}

	/**
	 * Loads the graph <code>graph</code>, from the server.
	 * 
	 * @param graph
	 *            the path to the graph
	 * @return
	 */
	public StringBuilder loadGraphFromServer(String path) {
		return new StringBuilder(Integer.toString(createNewSession(path)));
	}

	/**
	 * Loads the graph from <code>uri</code>. Returns the id of the session or
	 * -1 if the tg.-file is too big.
	 * 
	 * @param uri
	 * @return
	 */
	public StringBuilder loadGraphFromURI(Boolean overwrite, String uri) {
		if (!uri.toLowerCase().endsWith(".tg")
				&& !uri.toLowerCase().endsWith(".gz")) {
			// Checks if uri is a graph.
			return returnError(uri + "isn't a graph.");
		}
		// get the filename of the graph without .tg or .gz
		boolean isCompressed = uri.toLowerCase().endsWith(".gz");
		String[] partsOfURI = uri.split("/");
		String filename = partsOfURI[partsOfURI.length - 1];
		filename = workspace.toString() + "/"
				+ filename.substring(0, filename.length() - 3);
		// find an unused name for the new graph
		File graphFile = new File(filename + (isCompressed ? ".gz" : ".tg"));
		if (!overwrite) {
			int endNumber = 0;
			while (graphFile.exists()) {
				graphFile = new File(filename + (endNumber++)
						+ (isCompressed ? ".gz" : ".tg"));
			}
		}
		boolean isSizeOk = true;
		try {
			// get graph file
			URL url = new URL(uri);
			URLConnection conn = url.openConnection();
			conn.connect();
			long lengthOfFile = conn.getContentLength();
			if (isSizeOk = RequestThread.isSizeOk(lengthOfFile)) {
				Object o = conn.getContent();
				if (!(o instanceof InputStream)) {
					if (conn instanceof HttpURLConnection) {
						((HttpURLConnection) conn).disconnect();
					}
					return returnError("This file isn't plain text.");
				}
				InputStream in = (InputStream) o;
				// receive data and create file
				byte[] readData = new byte[4096];
				int bytesRead;
				if (!graphFile.createNewFile()) {
					TGraphBrowserServer.logger
							.info(graphFile.toString()
									+ " overwrites an existing file or could not be created.");
				}
				FileOutputStream fos = new FileOutputStream(graphFile);
				while ((bytesRead = in.read(readData)) != -1) {
					fos.write(readData, 0, bytesRead);
					fos.flush();
				}
				fos.close();
				in.close();
			}
			// close connection
			if (conn instanceof HttpURLConnection) {
				((HttpURLConnection) conn).disconnect();
			}
		} catch (MalformedURLException e) {
			e.printStackTrace();
			return returnError(e.toString());
		} catch (IOException e) {
			e.printStackTrace();
			return returnError(e.toString());
		}
		return new StringBuilder(!isSizeOk ? "-1" : Integer
				.toString(createNewSession(graphFile.getAbsolutePath())));
	}

	/**
	 * Creates an error message.
	 * 
	 * @param message
	 *            the message of the error.
	 * @return
	 */
	private StringBuilder returnError(String message) {
		StringBuilder code = new StringBuilder("function() {\n");
		code
				.append("document.getElementById('divError').style.display = \"block\";\n");
		code
				.append(
						"document.getElementById('h2ErrorMessage').innerHTML = \"ERROR: ")
				.append(message).append("\";\n");
		code
				.append("document.getElementById('divNonError').style.display = \"none\";\n");
		return code.append("}");
	}

	/**
	 * Creates an html-List of links of all graphs found in
	 * <code>directory</code> and its subdirectories. If a graph is in a
	 * subdirectory the directory is put in front of the graphname.
	 * 
	 * @param code
	 *            the list of links of all graphs
	 * @param directory
	 *            the directory
	 * @return false, iff there are no graphs on the server
	 */
	private boolean createListOfGraphs(StringBuilder code, File directory) {
		assert directory.exists() && directory.isDirectory();
		boolean graphsExist = false;
		for (File f : directory.listFiles()) {
			if (f.exists()) {
				if (f.isDirectory()) {
					graphsExist |= createListOfGraphs(code, f);
				} else if (f.toString().toLowerCase().endsWith(".tg")
						|| f.toString().toLowerCase().endsWith(".gz")) {
					graphsExist = true;
					code.append("var li = document.createElement(\"li\");\n");
					code.append("var a = document.createElement(\"a\");\n");
					code.append("a.innerHTML = \"").append(
							Pattern.compile(Matcher.quoteReplacement("\\"))
									.matcher(
											f.toString().substring(
													workspace.toString()
															.length() + 1))
									.replaceAll("/")).append("\";\n");
					code
							.append(
									"a.href = \"javascript:document.location = 'loadGraphFromServer?path='+'")
							.append(
									Pattern.compile(
											Matcher.quoteReplacement("\\"))
											.matcher(f.toString()).replaceAll(
													"/")).append("';\";\n");
					code.append("li.appendChild(a);\n");
					code
							.append("li.appendChild(document.createTextNode(String.fromCharCode(160)));\n");
					code
							.append("var deleteA = document.createElement(\"a\");\n");
					code.append("deleteA.innerHTML = \"X\";\n");
					code.append("deleteA.href = \"javascript:deleteGraph('")
							.append(
									Pattern.compile(
											Matcher.quoteReplacement("\\"))
											.matcher(f.toString()).replaceAll(
													"/")).append("');\";\n");
					code.append("deleteA.style.textDecoration = \"none\";\n");
					code.append("deleteA.style.color = \"red\";\n");
					code.append("deleteA.style.fontWeight = \"bold\";\n");
					code.append("li.appendChild(deleteA);\n");
					code
							.append("insertSorted(li, parentUl, 0, parentUl.childNodes.length-1);\n");
				}
			}
		}
		return graphsExist;
	}

	/**
	 * Creates and returns a the id of the new State. It gets the first unused
	 * sessionId.
	 * 
	 * @param graph
	 *            the graph
	 * @return the id of the new State
	 */
	public static int createNewSession(String graph) {
		State ret = new State(graph);
		synchronized (sessions) {
			while (sessions.size() < nextSessionId) {
				sessions.add(null);
			}
			sessions.add(ret);
			TGraphBrowserServer.logger.info("Session " + nextSessionId
					+ " created");
			assert sessions.indexOf(ret) == nextSessionId;
		}
		return nextSessionId++;
	}

	/**
	 * Deletes all sessions where the timeout is reached.
	 * 
	 * @param timeoutMilSec
	 *            the timeout in milliseconds
	 */
	public static void deleteAllUnusedSessions(long timeoutMilSec) {
		synchronized (sessions) {
			for (int i = 0; i < sessions.size(); i++) {
				State s = sessions.get(i);
				if ((s != null)
						&& (s.lastAccess + timeoutMilSec < System
								.currentTimeMillis())) {
					// delete all sessions, which are too old
					s.deleteUnsynchronized();
				}
			}
		}
	}

	/**
	 * Returns the state of the session with <code>sessionId</code>. The
	 * lastAccess-time is updated.
	 * 
	 * @param sessionId
	 *            the id of the session
	 * @return the state of the session with <code>sessionId</code>
	 */
	public static State getSession(int sessionId) {
		State s;
		synchronized (sessions) {
			assert sessionId < sessions.size();
			s = sessions.get(sessionId);
			s.lastAccess = System.currentTimeMillis();
		}
		return s;
	}

	/**
	 * This class represents the state of a session.
	 */
	static class State {

		// the last time this session was accessed
		public long lastAccess;

		// if set to false the user is asked if he wants to reload the current
		// graph because its tg-file has changed
		public boolean ignoreNewGraphVersions;

		// the .tg-file of the graph
		public String graphIdentifier;

		// selected or deselected vertexClasses
		public HashMap<VertexClass, Boolean> selectedVertexClasses;

		// selected or deselected edgeClasses
		public HashMap<EdgeClass, Boolean> selectedEdgeClasses;

		// the navigation history
		public ArrayList<JValue> navigationHistory;

		// the position of the navigationHistory where the next element is
		// inserted
		public int insertPosition;

		// the list of vertices
		public Vertex[] verticesOfTableView;

		// the list of edges
		public Edge[] edgesOfTableView;

		// the set of elements explicitly defined and currently shown
		public JValueSet currentExplicitlyDefinedSet;

		/**
		 * Creates a new State instance. All AttributedElementClasses are set to
		 * selected. The current system time is set to lastAccess.
		 * 
		 * @param graphFile
		 *            the graph
		 */
		public State(String graphFile) {
			lastAccess = System.currentTimeMillis();
			graphIdentifier = graphFile + "_"
					+ new File(graphFile).lastModified();
			navigationHistory = new ArrayList<JValue>();
			selectedVertexClasses = new HashMap<VertexClass, Boolean>();
			selectedEdgeClasses = new HashMap<EdgeClass, Boolean>();
			insertPosition = 0;
			setGraph(graphFile);
		}

		/**
		 * @return {@link Graph} the graph used by this state
		 */
		public Graph getGraph() {
			return usedGraphs.get(graphIdentifier).graph;
		}

		/**
		 * @return {@link GraphWrapper} used by this state
		 */
		public GraphWrapper getGraphWrapper() {
			return usedGraphs.get(graphIdentifier);
		}

		/**
		 * If the current graph not already exists it is loaded. Otherwise the
		 * already existing graph is used and its
		 * {@link GraphWrapper#numberOfUsers} is incremented.
		 */
		public void setGraph(String graphFile) {
			if (!usedGraphs.containsKey(graphIdentifier)) {
				usedGraphs.put(graphIdentifier, new GraphWrapper(
						graphIdentifier, graphFile));
			} else {
				usedGraphs.get(graphIdentifier).numberOfUsers++;
			}
		}

		/**
		 * Deletes this state and frees the sessionId. If workingThread is still
		 * alive it is interrupted. At the end the garbage collector is run.
		 * This method accesses <code>sessions</code> without using the monitor.
		 */
		public synchronized void deleteUnsynchronized() {
			int id = sessions.indexOf(this);
			// the state is removed before the id is freed, because this
			// avoids sideeffects
			sessions.set(id, null);
			getGraphWrapper().delete();
			graphIdentifier = null;
			edgesOfTableView = null;
			verticesOfTableView = null;
			navigationHistory = null;
			selectedEdgeClasses = null;
			selectedVertexClasses = null;
			// delete
			TGraphBrowserServer.logger.info("Session " + id + " deleted");
		}

		/**
		 * Deletes this state and frees the sessionId. If workingThread is still
		 * alive it is interrupted. At the end the garbage collector is run.
		 */
		public void delete() {
			synchronized (sessions) {
				deleteUnsynchronized();
			}
		}
	}

	/**
	 * Wraps the graph and all information about the loading of the graph.
	 */
	public static class GraphWrapper {

		// the current graph
		public Graph graph;

		// The tg-file of the current graph
		public String graphPath;

		// the number of states, which uses this graph
		public int numberOfUsers = 1;

		// saves a still working thread, if necessary
		public FutureTask<?> workingCallable;

		// an exception which occurred in the workingThread
		public Exception excOfWorkingCallable;

		// the identifier of the current Graph
		public String graphIdentifier;

		public int progress;

		public GraphWrapper(String graphIdentifier, String graphPath) {
			super();
			this.graphIdentifier = graphIdentifier;
			this.graphPath = graphPath;
			workingCallable = (FutureTask<?>) Executors.newCachedThreadPool()
					.submit(new LoadGraphCallable(this));
		}

		/**
		 * Reduces {@link GraphWrapper#workingCallable} by 1. If it becomes 0 a
		 * possible working {@link Callable} is canceled and all references are
		 * set to null. And the current {@link GraphWrapper} is removed from
		 * {@link StateRepository#usedGraphs}.
		 */
		public synchronized void delete() {
			numberOfUsers--;
			if (numberOfUsers == 0) {
				if ((workingCallable != null) && !workingCallable.isDone()
						&& !workingCallable.isCancelled()) {
					// stop running thread
					workingCallable.cancel(true);
				}
				graph = null;
				workingCallable = null;
				excOfWorkingCallable = null;
				usedGraphs.remove(graphIdentifier);
			}
		}
	}

	/**
	 * This Callable loads the graph. If an exception occurs it is saved in
	 * state.excOfWorkingThread. It saves all vertex- and edgeClasses in
	 * state.selectedClasses and marks them as selected.
	 */
	public static class LoadGraphCallable implements Callable<Void> {

		// the current graph
		private GraphWrapper currentGraph;

		/**
		 * Creates a new Collable which loads the graph.
		 * 
		 * @param graphIdentifier
		 *            the .tg-file of the graph
		 * @param state2
		 *            the corresponsing state
		 */
		public LoadGraphCallable(GraphWrapper graphWrapper) {
			super();
			currentGraph = graphWrapper;
		}

		@Override
		public Void call() throws Exception {
			try {
				synchronized (GraphIO.class) {
					currentGraph.progress = 0;
					currentGraph.graph = GraphIO.loadSchemaAndGraphFromFile(
							currentGraph.graphPath,
							new CodeGeneratorConfiguration()
									.withSaveMemSupport(),
							new MyProgressFunction(currentGraph));
					assert currentGraph.graph != null : "The graph wasn't loaded correctly.";
					currentGraph = null;
				}
			} catch (Exception e) {
				currentGraph.excOfWorkingCallable = e;
				e.printStackTrace();
				currentGraph = null;
			}
			return null;
		}

		private static class MyProgressFunction implements ProgressFunction {

			private long totalElements;
			private static final long length = 400;
			private int currentChar;
			private GraphWrapper currentGraph;

			public MyProgressFunction(GraphWrapper currentGraph) {
				this.currentGraph = currentGraph;
			}

			@Override
			public void finished() {
				try {
					for (long i = currentChar; i < length; i++) {
						currentGraph.progress++;
					}
					currentGraph = null;
				} catch (Exception e) {
					currentGraph.excOfWorkingCallable = e;
					e.printStackTrace();
				}
			}

			@Override
			public long getUpdateInterval() {
				try {
					return length > totalElements ? 1 : totalElements / length;
				} catch (Exception e) {
					currentGraph.excOfWorkingCallable = e;
					e.printStackTrace();
				}
				return 0;
			}

			@Override
			public void init(long elements) {
				currentChar = 0;
				totalElements = elements;
			}

			@Override
			public void progress(long processedElements) {
				try {
					if (currentChar < length) {
						currentGraph.progress++;
						currentChar++;
					}
				} catch (Exception e) {
					currentGraph.excOfWorkingCallable = e;
					e.printStackTrace();
				}
			}

		}

	}
}
