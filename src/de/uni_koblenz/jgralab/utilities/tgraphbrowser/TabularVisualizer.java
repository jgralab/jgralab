package de.uni_koblenz.jgralab.utilities.tgraphbrowser;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import de.uni_koblenz.jgralab.AttributedElement;
import de.uni_koblenz.jgralab.Edge;
import de.uni_koblenz.jgralab.GraphElement;
import de.uni_koblenz.jgralab.GraphIOException;
import de.uni_koblenz.jgralab.Vertex;
import de.uni_koblenz.jgralab.greql2.jvalue.JValue;
import de.uni_koblenz.jgralab.greql2.jvalue.JValueList;
import de.uni_koblenz.jgralab.greql2.jvalue.JValueSet;
import de.uni_koblenz.jgralab.schema.Attribute;
import de.uni_koblenz.jgralab.schema.AttributedElementClass;
import de.uni_koblenz.jgralab.schema.EdgeClass;
import de.uni_koblenz.jgralab.schema.VertexClass;
import de.uni_koblenz.jgralab.utilities.tgraphbrowser.StateRepository.State;

public class TabularVisualizer {

	static int NUMBER_OF_INCIDENCES_PER_PAGE = 10;

	/**
	 * Puts all vertices and edges, which are found in <code>elements</code>
	 * into the list of the currently selected elements.
	 * 
	 * @param state
	 * @param elements
	 *            if elements is null, the currently selected elements are
	 *            filtered.
	 */
	public void calculateVertexListAndEdgeList(State state, JValueSet elements) {
		ArrayList<Vertex> vertices = new ArrayList<Vertex>();
		ArrayList<Edge> edges = new ArrayList<Edge>();
		for (JValue jv : elements) {
			if (jv.isVertex()) {
				Vertex v = jv.toVertex();
				if (state.selectedVertexClasses.get(v
						.getAttributedElementClass())) {
					vertices.add(v);
				}
			} else {
				Edge e = jv.toEdge();
				if (state.selectedEdgeClasses
						.get(e.getAttributedElementClass())) {
					edges.add(e);
				}
			}
		}
		state.verticesOfTableView = vertices.isEmpty() ? null : vertices
				.toArray(new Vertex[0]);
		state.edgesOfTableView = edges.isEmpty() ? null : edges
				.toArray(new Edge[0]);
	}

	/**
	 * Calculates the list of vertices and edges whose types are selected to be
	 * shown.
	 * 
	 * @param state
	 *            the state
	 */
	public void calculateVertexListAndEdgeList(State state) {
		StringBuilder query = new StringBuilder();
		// calculate the list of vertices
		query.append("V{");
		boolean first = true;
		for (VertexClass type : state.selectedVertexClasses.keySet()) {
			query.append(!first ? ", " : "").append(
					state.selectedVertexClasses.get(type) ? "" : "^").append(
					type.getQualifiedName()).append("!");
			first = false;
		}
		query.append("} ");
		JValueList temp = StateRepository.evaluateGReQL(query.toString(),
				state.graph, null).toJValueList();
		state.verticesOfTableView = new Vertex[temp.size()];
		int i = 0;
		for (JValue jv : temp) {
			if (jv.isVertex()) {
				state.verticesOfTableView[i++] = jv.toVertex();
			}
		}
		// calculate the list of edges
		query = new StringBuilder("E{");
		first = true;
		for (EdgeClass type : state.selectedEdgeClasses.keySet()) {
			query.append(!first ? ", " : "").append(
					state.selectedEdgeClasses.get(type) ? "" : "^").append(
					type.getQualifiedName()).append("!");
			first = false;
		}
		query.append("}");
		temp = StateRepository.evaluateGReQL(query.toString(), state.graph,
				null).toJValueList();
		state.edgesOfTableView = new Edge[temp.size()];
		i = 0;
		for (JValue jv : temp) {
			if (jv.isEdge()) {
				state.edgesOfTableView[i++] = jv.toEdge();
			}
		}
	}

	/**
	 * Displays the chosen element in the tableView.
	 * 
	 * @param code
	 *            the StringBuilder which contains the String for the returned
	 *            JavaScript-code. Only append to the end!
	 * @param state
	 *            the id of this session
	 * @param numberPerPage
	 *            how many elements are shown on one page. -1 if all elements
	 *            should be displayed
	 * @param showAttributes
	 *            should the attributes be shown
	 * @param elementId
	 *            the id of the element which should be shown
	 * @param jumpToElement
	 *            if set to true, the browser jumps to the element
	 * @param createLinks
	 *            if set to true, there are links for the current elements
	 *            created.
	 */
	public void visualizeElements(StringBuilder code, State state,
			int numberPerPage, boolean showAttributes, String elementId,
			boolean jumpToElement, boolean createLinks) {
		boolean isVertex = elementId.startsWith("v");
		// set currentVertex or currentEdge to the current element
		code.append("current").append(
				elementId.charAt(0) == 'v' ? "Vertex" : "Edge").append(" = \"")
				.append(elementId).append("\";\n");
		// delete old content
		code.append("var parent = document.getElementById(\"divText").append(
				elementId.charAt(0) == 'v' ? "Vertex" : "Edge").append(
				"\");\nparent.innerHTML = \"\";\n");
		// check if there are elements
		if ((isVertex ? state.graph.getVCount() : state.graph.getECount()) == 0) {
			code.append("var divText = document.getElementById(\"divText")
					.append(isVertex ? "Vertex" : "Edge").append("\");\n");
			code.append("var h1=document.createElement(\"h1\");\n");
			code.append("h1.innerHTML = \"This graph doesn't have any ")
					.append(isVertex ? "vertices" : "edges").append(".\";\n");
			code.append("divText.appendChild(h1);\n");
			return;
		}
		// print number of elements
		code
				.append("document.getElementById(\"h3HowManyElements\").style.display = \"none\";\n");
		if (isVertex) {
			code
					.append("document.getElementById(\"h3HowManyVertices\").style.display = \"block\";\n");
			code
					.append("document.getElementById(\"h3HowManyEdges\").style.display = \"none\";\n");
		}
		code.append("document.getElementById(\"h3").append(
				isVertex ? "HowManyVertices" : "HowManyEdges").append(
				"\").innerHTML = \"").append(
				isVertex ? (state.verticesOfTableView == null ? 0
						: state.verticesOfTableView.length)
						+ " of " + state.graph.getVCount() + " vertices"
						: (state.edgesOfTableView == null ? 0
								: state.edgesOfTableView.length)
								+ " of " + state.graph.getECount() + " edges")
				.append(" visible.\";\n");
		if ((elementId.length() == 0)
				|| (elementId.length() == 1)
				|| (isVertex && ((state.verticesOfTableView == null) || (state.verticesOfTableView.length == 0)))
				|| (!isVertex && ((state.edgesOfTableView == null) || (state.edgesOfTableView.length == 0)))) {
			return;
		}
		// find position of element in the corresponding array TODO
		int idOfElement = Integer.parseInt(elementId.substring(1));
		int positionOfElementInArray = -1;
		if ((isVertex ? state.selectedVertexClasses : state.selectedEdgeClasses)
				.get((isVertex ? state.graph.getVertex(idOfElement)
						: state.graph.getEdge(idOfElement))
						.getAttributedElementClass())) {
			// try to find element if it's type wasn't deselected
			for (int i = 0; i < (isVertex ? state.verticesOfTableView
					: state.edgesOfTableView).length; i++) {
				if ((isVertex ? state.verticesOfTableView
						: state.edgesOfTableView)[i].getId() == idOfElement) {
					positionOfElementInArray = i;
					break;
				}
			}
		}
		boolean elementWasNotFound = positionOfElementInArray < 0;
		// the element wasn't found, the next element is shown
		positionOfElementInArray = positionOfElementInArray < 0 ? (positionOfElementInArray + 1)
				* -1
				: positionOfElementInArray;
		int numberOfPages = 1;
		int numberOfPageWithElementOfId = 1;
		if ((numberPerPage > 0)
				&& ((isVertex ? state.verticesOfTableView
						: state.edgesOfTableView).length > numberPerPage)) {
			// not all vertices could be displayed on one page
			// compute how many pages we have
			int totalNumberOfElements = (isVertex ? state.verticesOfTableView
					: state.edgesOfTableView).length;
			numberOfPages = totalNumberOfElements / numberPerPage;
			if (totalNumberOfElements % numberPerPage != 0) {
				numberOfPages++;
			}
			// determine the page in which the element with the id is found
			numberOfPageWithElementOfId = positionOfElementInArray
					/ numberPerPage + 1;
			// create the navigation bar through the table pages
			createNavigationThroughPages(code, isVertex ? "Vertex" : "Edge",
					numberOfPages, numberOfPageWithElementOfId, true);
		} else {
			code
					.append("parent.appendChild(document.createElement(\"br\"));\n");
			code.append("var divText").append(isVertex ? "Vertex" : "Edge")
					.append(" = parent;\n");
		}
		// create table
		createTableDiv(code, isVertex ? "Vertex" : "Edge");
		createTable(code, numberOfPageWithElementOfId, numberPerPage,
				isVertex ? "Vertex" : "Edge", showAttributes,
				isVertex ? state.verticesOfTableView : state.edgesOfTableView,
				state.selectedVertexClasses, state.selectedEdgeClasses,
				createLinks);
		// color the element
		JValue elem = state.navigationHistory.get(state.insertPosition - 1);
		String coloredElementId = "";
		if (elem.isVertex()) {
			coloredElementId = "v" + elem.toVertex().getId();
		} else if (elem.isEdge()) {
			coloredElementId = "e" + elem.toEdge().getId();
		}
		code.append("changeBackgroundColor(\"").append(coloredElementId)
				.append("\");\n");
		if (jumpToElement) {
			// jump to current element
			code.append("document.location.href = \"#").append(elementId)
					.append("\";\n");
		}
		if (elementWasNotFound) {
			// show message, that the graphelement could not be found.
			code.append("if(").append(isVertex).append(
					" && areVerticesShown()){\n");
			code.append("alert(\"The ").append(isVertex ? "vertex " : "edge ")
					.append(elementId).append(" could not be found");
			if (!(isVertex ? state.selectedVertexClasses
					: state.selectedEdgeClasses).get((isVertex ? state.graph
					.getVertex(idOfElement) : state.graph.getEdge(idOfElement))
					.getAttributedElementClass())) {
				code.append(" because the type ")
						.append(
								(isVertex ? state.graph.getVertex(idOfElement)
										: state.graph.getEdge(idOfElement))
										.getAttributedElementClass()
										.getQualifiedName()).append(
								" is deselected");
			}
			code
					.append(". \\nThat's why the first page of the current table is shown.\");\n");
			code.append("}\n");
		}
	}

	/**
	 * Creates the table which shows the elements.
	 * 
	 * @param code
	 * 
	 * @param numberOfPageWithElementOfId
	 *            the page which contains the element. The first page has the
	 *            number 1.
	 * @param numberPerPage
	 *            the number of elements which are shown on one page
	 * @param typeInfix
	 *            if you want to display the vertices this is "Vertex".
	 *            Otherwise this is "Edge".
	 * @param showAttributes
	 *            if true the attributes are shown
	 * @param graphElements
	 *            the array of the graph elements
	 * @param selectedVertexClasses
	 *            map of the selected vertextypes
	 * @param selectedEdgeClasses
	 *            map of the selected edgetypes
	 * @param createLinks
	 *            if set to true, the link targets are created
	 */
	private void createTable(StringBuilder code,
			int numberOfPageWithElementOfId, int numberPerPage,
			String typeInfix, boolean showAttributes,
			GraphElement[] graphElements,
			HashMap<VertexClass, Boolean> selectedVertexClasses,
			HashMap<EdgeClass, Boolean> selectedEdgeClasses, boolean createLinks) {
		// create table
		code.append("var mainTable").append(typeInfix).append(
				" = document.createElement(\"table\");\n");
		// create tablehead
		code.append("var table").append(typeInfix).append(
				" = document.createElement(\"thead\");\n");
		code.append("mainTable").append(typeInfix).append(".appendChild(table")
				.append(typeInfix).append(");\n");
		// create head
		code.append("var currentTr = document.createElement(\"tr\");\n");
		code.append("table").append(typeInfix).append(
				".appendChild(currentTr);\n");
		createCell(code, true, typeInfix, false);
		if (showAttributes) {
			createCell(code, true, "Attributes", false);
		}
		createCell(code, true, "Incident "
				+ (typeInfix.startsWith("E") ? "vertices" : "edges"), false);
		// create tablebody
		code.append("var table").append(typeInfix).append(
				" = document.createElement(\"tbody\");\n");
		code.append("mainTable").append(typeInfix).append(".appendChild(table")
				.append(typeInfix).append(");\n");
		// create elements
		for (int currentElementIndex = (numberOfPageWithElementOfId - 1)
				* (numberPerPage < 0 ? 0 : numberPerPage); (currentElementIndex < graphElements.length)
				&& (currentElementIndex < (numberPerPage < 0 ? graphElements.length
						: numberOfPageWithElementOfId * numberPerPage)); currentElementIndex++) {
			GraphElement currentElement = graphElements[currentElementIndex];
			code.append("var currentTr = document.createElement(\"tr\");\n");
			code.append("currentTr.id = \"tr").append(
					currentElement instanceof Vertex ? "v" : "e").append(
					currentElement.getId()).append("\";\n");
			code.append("table").append(typeInfix).append(
					".appendChild(currentTr);\n");
			// create identifier td
			createCell(code, false, createElement(currentElement), false);
			code.append("currentTd.onclick = function(){\nclickOnElement(\"")
					.append(currentElement instanceof Vertex ? "v" : "e")
					.append(currentElement.getId()).append("\");\n}\n");
			// create jumpTo
			if (createLinks) {
				code.append("var anker = document.createElement(\"a\");\n");
				code.append("anker.id = \"").append(
						currentElement instanceof Vertex ? "v" : "e").append(
						currentElement.getId()).append("\";\n");
				code.append("currentTd.appendChild(anker);\n");
			}
			if (!showAttributes) {
				// attributes are shown in tooltip
				createAttributes(code, currentElement, true);
			}
			if (showAttributes) {
				// attributes are shown
				createCell(code, false, "", true);
				createAttributes(code, currentElement, false);
				code.append("currentTd.style.textAlign = \"left\";\n");
			}
			// create incidence td
			createCell(code, false, "", true);
			if (currentElement instanceof Vertex) {
				code.append("currentTd.id = \"td").append("v").append(
						currentElement.getId()).append("\";\n");
				createIncidentEdges(code, (Vertex) currentElement,
						selectedEdgeClasses, selectedVertexClasses, 1, null);
			} else {
				createIncidentVertices(code, (Edge) currentElement,
						selectedVertexClasses);
			}
			code.append("currentTd.style.textAlign = \"left\";\n");
		}
		// append table
		code.append("div").append(typeInfix).append(
				"Table.appendChild(mainTable").append(typeInfix).append(");\n");
	}

	/**
	 * Creates the representation UniqueNameOfType<sub>Id</sub>.
	 * 
	 * @param currentElement
	 * @return
	 */
	private String createElement(GraphElement currentElement) {
		return currentElement.getAttributedElementClass().getUniqueName()
				+ "<sub>" + Math.abs(currentElement.getId()) + "</sub>";
	}

	/**
	 * Creates the representation of the attributes: Domain attributeName =
	 * attributeValue;
	 * 
	 * @param code
	 * 
	 * @param currentElement
	 * @param inToolTip
	 *            if true, it is shown in the tooltip
	 */
	private void createAttributes(StringBuilder code,
			AttributedElement currentElement, boolean inToolTip) {
		createAttributes(code, currentElement, inToolTip, null);
	}

	/**
	 * Creates the representation of the attributes: Domain attributeName =
	 * attributeValue;
	 * 
	 * @param code
	 * @param currentElement
	 * @param inToolTip
	 *            if true, it is shown in the tooltip
	 * @param var
	 *            the name of the JavaScript variable of which the title
	 *            attribute should be set.
	 */
	private void createAttributes(StringBuilder code,
			AttributedElement currentElement, boolean inToolTip, String var) {
		code.append(inToolTip ? (var == null ? "currentTd" : var)
				+ ".title = \"" : "");
		boolean first = true;
		for (Attribute attr : currentElement.getAttributedElementClass()
				.getAttributeList()) {
			if (!first) {
				code
						.append(inToolTip ? "\t"
								: (var == null ? "currentTd" : var)
										+ ".appendChild(document.createElement(\"br\"));\n");
			}
			code.append(inToolTip ? "" : (var == null ? "currentTd" : var)
					+ ".appendChild(document.createTextNode(\"");
			code.append(attr.getName()).append(" = ");
			try {
				String content = currentElement
						.writeAttributeValueToString(attr.getName());
				content = Pattern.compile(Matcher.quoteReplacement("\\"))
						.matcher(content).replaceAll(
								Matcher.quoteReplacement("\\\\"));
				content = Pattern.compile(Matcher.quoteReplacement("\""))
						.matcher(content).replaceAll(
								Matcher.quoteReplacement("\\\""));
				code.append(content.equals("n") ? "null"
						: content.equals("t") ? "true"
								: content.equals("f") ? "false" : content);
			} catch (IOException e) {
				e.printStackTrace();
			} catch (GraphIOException e) {
				e.printStackTrace();
			}
			code.append(inToolTip ? ";" : ";\"));\n");
			first = false;
		}
		code.append(inToolTip ? "\";\n" : "");
	}

	/**
	 * Creates the incidences for the vertices. If there are more then
	 * NUMBER_OF_INCIDENCES_PER_PAGE incident edges, there is a navigation bar
	 * shown.
	 * 
	 * @param code
	 * 
	 * @param currentVertex
	 * @param selectedEdgeClasses
	 * @param selectedVertexClasses
	 * @param displayedPage
	 *            the page to be shown
	 * @param vertexTdId
	 *            the id of the td of the incidence cell of the representation
	 *            of this vertex. If this method is called to create the table
	 *            this parameter must be null.
	 */
	public void createIncidentEdges(StringBuilder code, Vertex currentVertex,
			HashMap<EdgeClass, Boolean> selectedEdgeClasses,
			HashMap<VertexClass, Boolean> selectedVertexClasses,
			int displayedPage, String vertexTdId) {
		if (vertexTdId != null) {
			code.append("var currentTd = document.getElementById(\"").append(
					vertexTdId).append("\");\ncurrentTd.innerHTML = \"\";\n");
		}
		int numberOfEdges = 1;
		boolean hasNoLeadingBr = false;
		for (Edge e : currentVertex.incidences()) {
			if (selectedEdgeClasses.get(e.getAttributedElementClass())) {
				// show all incidences whose type is selected
				if ((numberOfEdges > (displayedPage - 1)
						* NUMBER_OF_INCIDENCES_PER_PAGE)
						&& (numberOfEdges <= displayedPage
								* NUMBER_OF_INCIDENCES_PER_PAGE)) {
					if (numberOfEdges > 1) {
						// create <br />
						code
								.append("currentTd.appendChild(document.createElement(\"br\"));\n");
					} else {
						hasNoLeadingBr = true;
					}
					// create entries for the first
					// NUMBER_OF_INCIDENCES_PER_PAGE incident edges
					code.append("var text = document.createElement(\"b\");\n");
					code
							.append("text.appendChild(document.createTextNode(")
							.append(
									e.getAlpha() == currentVertex ? "String.fromCharCode(8594)"
											: "String.fromCharCode(8592)")
							.append("));\n");
					code.append("text.style.fontSize = \"large\";\n");
					code.append("currentTd.appendChild(text);\n");
					code.append("text = document.createTextNode(\"{\");\n");
					code.append("currentTd.appendChild(text);\n");
					code.append("var aE = document.createElement(\"a\");\n");
					code.append("aE.href = \"javascript:showElement('e")
							.append(Math.abs(e.getId())).append("');\";\n");
					code.append("aE.innerHTML = \"").append(createElement(e))
							.append("\";\n");
					createAttributes(code, e.getNormalEdge(), true, "aE");
					code.append("currentTd.appendChild(aE);\n");
					code
							.append("currentTd.appendChild(document.createTextNode(\"}\"+String.fromCharCode(160)));\n");
					AttributedElementClass qualName = e.getThat()
							.getAttributedElementClass();
					if (selectedVertexClasses.get(qualName)) {
						// if the type of that is selected, show it as link
						code
								.append("var aThat = document.createElement(\"a\");\n");
						code.append("aThat.href = \"javascript:showElement('v")
								.append(e.getThat().getId()).append("');\";\n");
					} else {
						code
								.append("var aThat = document.createElement(\"span\");\n");
					}
					code.append("aThat.innerHTML= \"").append(
							createElement(e.getThat())).append("\";\n");
					code.append("currentTd.appendChild(aThat);\n");
					createAttributes(code, e.getThat(), true, "aThat");
				}
				numberOfEdges++;
			}
		}
		if (numberOfEdges - 1 > NUMBER_OF_INCIDENCES_PER_PAGE) {
			// if there are more than NUMBER_OF_INCIDENCES_PER_PAGE
			// incidences create a header to switch through the pages
			if (hasNoLeadingBr) {
				// crate leading br after the header if it doesn't exist yet
				code
						.append("currentTd.insertBefore(document.createElement(\"br\"),currentTd.firstChild);\n");
			}
			numberOfEdges--;
			int numberOfPages = numberOfEdges
					/ NUMBER_OF_INCIDENCES_PER_PAGE
					+ (numberOfEdges % NUMBER_OF_INCIDENCES_PER_PAGE == 0 ? 0
							: 1);
			createNavigationThroughPages(code, "v" + currentVertex.getId(),
					numberOfPages, displayedPage, false);
		}
	}

	/**
	 * Creates the entry for the incident vertices of an edge. If the type of
	 * alpha or omega is selected it is shown as link. Otherwise it is text.
	 * 
	 * @param code
	 * 
	 * @param currentEdge
	 * @param selectedVertexClasses
	 * @return
	 */
	private void createIncidentVertices(StringBuilder code, Edge currentEdge,
			HashMap<VertexClass, Boolean> selectedVertexClasses) {
		AttributedElementClass qualName = currentEdge.getAlpha()
				.getAttributedElementClass();
		// create alpha-vertex
		if (selectedVertexClasses.get(qualName)) {
			// if the type of alpha is selected, show it as link
			code.append("var aAlpha = document.createElement(\"a\");\n");
			code.append("aAlpha.href = \"javascript:showElement('v").append(
					currentEdge.getAlpha().getId()).append("');\";\n");
		} else {
			code.append("var aAlpha = document.createElement(\"span\");\n");
		}
		code.append("aAlpha.innerHTML= \"").append(
				createElement(currentEdge.getAlpha())).append("\";\n");
		code.append("currentTd.appendChild(aAlpha);\n");
		createAttributes(code, currentEdge.getAlpha(), true, "aAlpha");
		// create -->
		code
				.append("var textNode = document.createTextNode(String.fromCharCode(160));\n");
		code.append("currentTd.appendChild(textNode);\n");
		code.append("var textNode = document.createElement(\"b\");\n");
		code.append("textNode.appendChild(document.createTextNode("
				+ "String.fromCharCode(8594)));\n");
		code.append("textNode.style.fontSize = \"large\";\n");
		code.append("currentTd.appendChild(textNode);\n");
		code
				.append("var textNode = document.createTextNode(String.fromCharCode(160));\n");
		code.append("currentTd.appendChild(textNode);\n");
		// create omega-vertex
		qualName = currentEdge.getOmega().getAttributedElementClass();
		if (selectedVertexClasses.get(qualName)) {
			// if the type of alpha is selected, show it as link
			code.append("var aOmega = document.createElement(\"a\");\n");
			code.append("aOmega.href = \"javascript:showElement('v").append(
					currentEdge.getOmega().getId()).append("');\";\n");
		} else {
			code.append("var aOmega = document.createElement(\"span\");\n");
		}
		code.append("aOmega.innerHTML= \"").append(
				createElement(currentEdge.getOmega())).append("\";\n");
		code.append("currentTd.appendChild(aOmega);\n");
		createAttributes(code, currentEdge.getOmega(), true, "aOmega");
	}

	/**
	 * This method creates the following code:<br>
	 * If <code>isHead == true && isJavaScript == false</code>:<br>
	 * &lt;th&gt;<code>content</code>&lt;/th&gt;<br>
	 * If <code>isHead == true && isJavaScript == true</code>:<br>
	 * &lt;th&gt;&lt;/th&gt; + content <br>
	 * If <code>isHead == false && isJavaScript == false</code>:<br>
	 * &lt;td&gt;<code>content</code>&lt;/td&gt;<br>
	 * If <code>isHead == false && isJavaScript == true</code>:<br>
	 * &lt;td&gt;&lt;/td&gt; + content <br>
	 * The created th or td is saved in the JavaScript variable currentTh or
	 * currentTd.
	 * 
	 * @param code
	 * 
	 * @param isHead
	 * @param content
	 * @param isJavaScript
	 */
	private void createCell(StringBuilder code, boolean isHead, String content,
			boolean isJavaScript) {
		String infix = isHead ? "h" : "d";
		// create th or td
		code.append("var currentT").append(infix).append(
				" = document.createElement(\"t").append(infix).append("\");\n");
		code.append("currentTr.appendChild(currentT").append(infix).append(
				");\n");
		if (!isJavaScript) {
			code.append("currentT").append(infix).append(".innerHTML=\"")
					.append(content).append("\";\n");
		} else {
			code.append(content);
		}
	}

	/**
	 * Creates the navigation headline.
	 * 
	 * @param code
	 * 
	 * @param typeInfix
	 *            if equals to "Vertex" the VertexHeadline is created.If equals
	 *            to "Edge" the EdgeHeadline is created.
	 * @param numberOfPages
	 *            the number of pages
	 * @param displayedPage
	 *            the number of the current page
	 * @param isMainNavigation
	 *            if true the navigation for the table is created. Otherwise the
	 *            navigation for the incident edges of a vertex is created.
	 */
	private void createNavigationThroughPages(StringBuilder code,
			String typeInfix, int numberOfPages, int displayedPage,
			boolean isMainNavigation) {
		if (isMainNavigation) {
			code.append("var divText").append(typeInfix).append(
					" = document.getElementById(\"divText").append(typeInfix)
					.append("\");\n");
		}
		// create div of headline
		code.append("var div").append(typeInfix).append(
				"Headline = document.createElement(\"div\");\n");
		code.append("div").append(typeInfix).append("Headline.id = \"div")
				.append(typeInfix).append("Headline\";\n");
		code.append("if(div").append(typeInfix).append(
				"Headline.hasAttribute){\n");
		code.append("div").append(typeInfix).append(
				"Headline.setAttribute(\"class\",\"divHeadline\");\n");
		code.append("}else{\n");
		code.append("div").append(typeInfix).append(
				"Headline.setAttribute(\"className\",\"divHeadline\");\n");
		code.append("}\n");
		if (isMainNavigation) {
			code.append("divText").append(typeInfix).append(".appendChild(div")
					.append(typeInfix).append("Headline);\n");
		} else {
			code.append("currentTd.insertBefore(div").append(typeInfix).append(
					"Headline,currentTd.firstChild);\n");
		}
		if (isMainNavigation) {
			// create br
			code.append("var brheadline = document.createElement(\"br\");\n");
			code.append("div").append(typeInfix).append(
					"Headline.appendChild(brheadline);\n");
		}
		// create a to beginning
		code.append("var aToBeginning = document.createElement(\"a\");\n");
		code.append("aToBeginning.href = \"javascript:").append(
				isMainNavigation ? "goToPage" : "goToIncidentPage")
				.append("(1").append(
						isMainNavigation ? "" : ",'\"+currentTd.id+\"'")
				.append(");\";\n");
		code.append("aToBeginning.innerHTML = \"&lt;&lt;\";\n");
		code.append("div").append(typeInfix).append(
				"Headline.appendChild(aToBeginning);\n");
		// create a to previous
		code.append("var aToPrevious = document.createElement(\"a\");\n");
		code
				.append("aToPrevious.href = \"javascript:")
				.append(isMainNavigation ? "goToPage" : "goToIncidentPage")
				.append("(document.getElementById('inputPageNumber")
				.append(typeInfix)
				.append(
						"').value==1?1:document.getElementById('inputPageNumber")
				.append(typeInfix).append("').value-1").append(
						isMainNavigation ? "" : ",'\"+currentTd.id+\"'")
				.append(");\";\n");
		code
				.append("aToPrevious.innerHTML = String.fromCharCode(160)+String.fromCharCode(60)+String.fromCharCode(160);\n");
		code.append("div").append(typeInfix).append(
				"Headline.appendChild(aToPrevious);\n");
		// create text " Page "
		code
				.append("var textPage = document.createTextNode(String.fromCharCode(160)+\"Page\"+String.fromCharCode(160));\n");
		code.append("div").append(typeInfix).append(
				"Headline.appendChild(textPage);\n");
		// create input-field
		code.append("var inputPageNumber").append(typeInfix).append(
				" = document.createElement(\"input\");\n");
		code.append("inputPageNumber").append(typeInfix).append(
				".id = \"inputPageNumber").append(typeInfix).append("\";\n");
		code.append("inputPageNumber").append(typeInfix).append(
				".type = \"text\";\n");
		code.append("inputPageNumber").append(typeInfix).append(".size = 6;\n");
		code.append("inputPageNumber").append(typeInfix).append(".value = ")
				.append(displayedPage).append(";\n");
		code.append("inputPageNumber").append(typeInfix).append(
				".style.textAlign = \"right\";\n");
		code
				.append("inputPageNumber")
				.append(typeInfix)
				.append(
						".onchange = function(){\nif(this.value<1){\nthis.value = 1;\n}else if(this.value>")
				.append(numberOfPages).append("){\nthis.value = ").append(
						numberOfPages).append(";\n}\n").append(
						isMainNavigation ? "goToPage" : "goToIncidentPage")
				.append("(this.value*1,").append(
						isMainNavigation ? "true"
								: "this.parentNode.parentNode.id").append(
						");\n};\n");
		code.append("div").append(typeInfix).append(
				"Headline.appendChild(inputPageNumber").append(typeInfix)
				.append(");\n");
		// create text "/xxx "
		code
				.append(
						"var textMax = document.createTextNode(String.fromCharCode(160)+\"/")
				.append(numberOfPages)
				.append("\"+String.fromCharCode(160));\n");
		code.append("div").append(typeInfix).append(
				"Headline.appendChild(textMax);\n");
		// create a to next page
		code.append("var aToNext = document.createElement(\"a\");\n");
		code.append("aToNext.href = \"javascript:").append(
				isMainNavigation ? "goToPage" : "goToIncidentPage").append(
				"(document.getElementById('inputPageNumber").append(typeInfix)
				.append("').value==").append(numberOfPages).append("?").append(
						numberOfPages).append(
						":document.getElementById('inputPageNumber").append(
						typeInfix).append("').value*1+1").append(
						isMainNavigation ? "" : ",'\"+currentTd.id+\"'")
				.append(");\";\n");
		code
				.append("aToNext.innerHTML = String.fromCharCode(160)+String.fromCharCode(62)+String.fromCharCode(160);\n");
		code.append("div").append(typeInfix).append(
				"Headline.appendChild(aToNext);\n");
		// create a to end
		code.append("var aToEnd = document.createElement(\"a\");\n");
		code.append("aToEnd.href = \"javascript:").append(
				isMainNavigation ? "goToPage" : "goToIncidentPage").append("(")
				.append(numberOfPages).append(
						isMainNavigation ? "" : ",'\"+currentTd.id+\"'")
				.append(");\";\n");
		code.append("aToEnd.innerHTML = \"&gt;&gt;\";\n");
		code.append("div").append(typeInfix).append(
				"Headline.appendChild(aToEnd);\n");
		// create br
		if (isMainNavigation) {
			code
					.append("var brafterheadline = document.createElement(\"br\");\n");
			code.append("brafterheadline.id = \"brToDelete").append(typeInfix)
					.append("\";\n");
			code.append("divText").append(typeInfix).append(
					".appendChild(brafterheadline);\n");
		}
	}

	/**
	 * Creates the div for the table of the vertices or edges.
	 * 
	 * @param code
	 * 
	 * @param typeInfix
	 */
	private void createTableDiv(StringBuilder code, String typeInfix) {
		// create div for table
		code.append("var div").append(typeInfix).append(
				"Table = document.createElement(\"div\");\n");
		code.append("div").append(typeInfix).append("Table.id = \"div").append(
				typeInfix).append("Table\";\n");
		code.append("divText").append(typeInfix).append(".appendChild(div")
				.append(typeInfix).append("Table);\n");
	}
}
