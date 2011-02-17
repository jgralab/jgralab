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
package de.uni_koblenz.jgralab.utilities.tg2dot.graph_layout;

import static de.uni_koblenz.jgralab.utilities.tg2dot.greql2.GreqlEvaluatorFacade.ABBREVIATE_EDGE_ATTRIBUTE_NAMES;
import static de.uni_koblenz.jgralab.utilities.tg2dot.greql2.GreqlEvaluatorFacade.COMPOSITE_THAT;
import static de.uni_koblenz.jgralab.utilities.tg2dot.greql2.GreqlEvaluatorFacade.COMPOSITE_THIS;
import static de.uni_koblenz.jgralab.utilities.tg2dot.greql2.GreqlEvaluatorFacade.ELEMENT;
import static de.uni_koblenz.jgralab.utilities.tg2dot.greql2.GreqlEvaluatorFacade.ELEMENT_SEQUENCE_INDEX;
import static de.uni_koblenz.jgralab.utilities.tg2dot.greql2.GreqlEvaluatorFacade.PRINT_DOMAIN_NAMES;
import static de.uni_koblenz.jgralab.utilities.tg2dot.greql2.GreqlEvaluatorFacade.PRINT_EDGE_ATTRIBUTES;
import static de.uni_koblenz.jgralab.utilities.tg2dot.greql2.GreqlEvaluatorFacade.PRINT_ELEMENT_SEQUENCE_INDICES;
import static de.uni_koblenz.jgralab.utilities.tg2dot.greql2.GreqlEvaluatorFacade.PRINT_INCIDENCE_INDICES;
import static de.uni_koblenz.jgralab.utilities.tg2dot.greql2.GreqlEvaluatorFacade.PRINT_ROLENAMES;
import static de.uni_koblenz.jgralab.utilities.tg2dot.greql2.GreqlEvaluatorFacade.SHARED_THAT;
import static de.uni_koblenz.jgralab.utilities.tg2dot.greql2.GreqlEvaluatorFacade.SHARED_THIS;
import static de.uni_koblenz.jgralab.utilities.tg2dot.greql2.GreqlEvaluatorFacade.SHORTEN_STRINGS;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import de.uni_koblenz.jgralab.AttributedElement;
import de.uni_koblenz.jgralab.greql2.jvalue.JValue;
import de.uni_koblenz.jgralab.greql2.jvalue.JValueSet;
import de.uni_koblenz.jgralab.schema.AttributedElementClass;
import de.uni_koblenz.jgralab.schema.Schema;
import de.uni_koblenz.jgralab.utilities.tg2dot.graph_layout.definition.Definition;
import de.uni_koblenz.jgralab.utilities.tg2dot.graph_layout.definition.ElementDefinition;
import de.uni_koblenz.jgralab.utilities.tg2dot.graph_layout.definition.TypeDefinition;
import de.uni_koblenz.jgralab.utilities.tg2dot.graph_layout.reader.GraphLayoutReader;
import de.uni_koblenz.jgralab.utilities.tg2dot.graph_layout.reader.json.JsonGraphLayoutReader;
import de.uni_koblenz.jgralab.utilities.tg2dot.graph_layout.reader.plist.PListGraphLayoutReader;
import de.uni_koblenz.jgralab.utilities.tg2dot.greql2.GreqlEvaluatorFacade;

public class GraphLayoutFactory {

	private Schema schema;

	private GreqlEvaluatorFacade evaluator;

	private GraphLayout currentGraphLayout;

	private GraphLayoutReader reader;

	private File graphLayoutFile;

	public GraphLayoutFactory(GreqlEvaluatorFacade evaluator) {
		this.evaluator = evaluator;
		schema = evaluator.getSchema();
	}

	public void setJsonGraphLayoutFilename(File graphLayoutFile) {
		this.graphLayoutFile = graphLayoutFile;
		reader = new JsonGraphLayoutReader(evaluator);
	}

	public void setPListGraphLayoutFilename(File graphLayoutFile) {
		this.graphLayoutFile = graphLayoutFile;
		reader = new PListGraphLayoutReader(evaluator);
	}

	public GraphLayout createGraphLayout() {

		try {
			createAndLoadGraphLayoutFromFile();
		} catch (FileNotFoundException e) {
			throw new RuntimeException(e);
		}

		return currentGraphLayout;
	}

	public void createAndLoadGraphLayoutFromFile() throws FileNotFoundException {
		validate();

		initializeProcessingStructures();

		loadFromFile();

		applyHierarchieToTypeDefinitions();

		evaluateElementDefinitions();
	}

	private void loadFromFile() throws FileNotFoundException {
		if (reader == null) {
			return;
		}
		reader.startProcessing(graphLayoutFile, currentGraphLayout);
	}

	private void validate() {
		if (schema == null || evaluator == null) {
			throw new RuntimeException(
					"The Schema, GreqlEvaluator or both are not set.");
		}
	}

	private void initializeProcessingStructures() {
		currentGraphLayout = new GraphLayout();
		currentGraphLayout.setSchema(schema);
		currentGraphLayout.initiateAllTypeDefinitions();

		setDefaultVertexLayout();
		setDefaultEdgeLayout();
	}

	private void setDefaultVertexLayout() {
		TypeDefinition definition = currentGraphLayout
				.getTypeDefinition("Vertex");
		definition.setAttribute("label", "'{{v' ++ id(" + ELEMENT + ") ++ ("
				+ PRINT_ELEMENT_SEQUENCE_INDICES + " ? ' (' ++ "
				+ ELEMENT_SEQUENCE_INDEX
				+ " ++ ')'  : '') ++ ' | ' ++ typeName(" + ELEMENT
				+ ") ++ '}|' ++ " + "join(\"\\l\", from attr:attributeNames("
				+ ELEMENT + ") " + "reportSet (attr ++ ' = ' ++ ("
				+ SHORTEN_STRINGS + " ? shortenString(toDotString(getValue("
				+ ELEMENT + ", attr)), 17) : toDotString(getValue(" + ELEMENT
				+ ", attr)) ++ (" + PRINT_DOMAIN_NAMES
				+ " ? ': ' ++ attributeType(" + ELEMENT
				+ ", attr) : ''))) end) ++ '}'");
		definition.setAttribute("shape", "'record'");
		definition.setAttribute("color", "'#999999'");
		definition.setAttribute("fontsize", "14");
		definition.setAttribute("fontname", "'Helvetica'");
	}

	private void setDefaultEdgeLayout() {

		TypeDefinition definition = currentGraphLayout
				.getTypeDefinition("Edge");
		definition.setAttribute("color", "'gray'");
		definition.setAttribute("label", "'e' ++ id(" + ELEMENT
				+ ") ++ ' : ' ++ typeName(" + ELEMENT + ") ++ ("
				+ PRINT_ELEMENT_SEQUENCE_INDICES + " ? ' (' ++ "
				+ ELEMENT_SEQUENCE_INDEX + " ++ ')'  : '') ++ ("
				+ PRINT_EDGE_ATTRIBUTES + " ? '\\n' ++ " + "join('\\l', from "
				+ "attr:attributeNames(" + ELEMENT + ") " + "reportSet (("
				+ ABBREVIATE_EDGE_ATTRIBUTE_NAMES
				+ " ? abbreviateString(attr) : attr) " + "++ ' = ' ++ ("
				+ SHORTEN_STRINGS + " ? shortenString(toDotString(getValue("
				+ ELEMENT + ", attr)), 17) : toDotString(getValue(" + ELEMENT
				+ ", attr)) ++ (" + PRINT_DOMAIN_NAMES
				+ " ? ': ' ++ attributeType(" + ELEMENT
				+ ", attr) : ''))) end) : '')");
		definition.setAttribute("arrowhead", "((" + SHARED_THIS
				+ ")? 'odiamond' :" + "(" + COMPOSITE_THIS
				+ ")? 'diamond': '') ++ 'normal'");
		definition.setAttribute("arrowtail", "(" + SHARED_THAT
				+ ") ? 'odiamond' :" + "(" + COMPOSITE_THAT
				+ ") ? 'diamond' : 'none'");
		definition.setAttribute("taillabel", "((" + PRINT_INCIDENCE_INDICES
				+ ") ? " + "alphaIncidenceNumber(" + ELEMENT + ") : '')"
				+ " ++ ((" + PRINT_INCIDENCE_INDICES + " and "
				+ PRINT_ROLENAMES + " and " + "nequals(alphaRolename("
				+ ELEMENT + "), '')) ? '; ' : '')" + " ++ ((" + PRINT_ROLENAMES
				+ ") ? alphaRolename(" + ELEMENT + ") : '')");
		definition.setAttribute("headlabel", "((" + PRINT_INCIDENCE_INDICES
				+ ") ? " + "omegaIncidenceNumber(" + ELEMENT + ") : '')"
				+ " ++ ((" + PRINT_INCIDENCE_INDICES + " and "
				+ PRINT_ROLENAMES + " and " + "nequals(omegaRolename("
				+ ELEMENT + "), '')) ? '; ' : '')" + " ++ ((" + PRINT_ROLENAMES
				+ ") ? omegaRolename(" + ELEMENT + ") : '')");
		definition.setAttribute("dir", "'both'");
		definition.setAttribute("fontsize", "14");
		definition.setAttribute("fontname", "'Helvetica'");
	}

	private void applyHierarchieToTypeDefinitions() {
		applyHierarchie(currentGraphLayout.vertexTypeDefinitions);
		applyHierarchie(currentGraphLayout.edgeTypeDefinitions);
	}

	private void applyHierarchie(Map<AttributedElementClass, TypeDefinition> map) {

		List<AttributedElementClass> allSchemaTypes = new ArrayList<AttributedElementClass>();
		allSchemaTypes.addAll(schema.getVertexClassesInTopologicalOrder());
		allSchemaTypes.addAll(schema.getEdgeClassesInTopologicalOrder());

		for (Entry<AttributedElementClass, TypeDefinition> entry : map
				.entrySet()) {
			AttributedElementClass type = entry.getKey();

			List<AttributedElementClass> allSuperClasses = new ArrayList<AttributedElementClass>(
					allSchemaTypes);
			allSuperClasses.retainAll(type.getAllSuperClasses());
			Collections.reverse(allSuperClasses);

			for (AttributedElementClass supertype : allSuperClasses) {
				Definition spec = map.get(supertype);
				entry.getValue().addNonExistingAttributes(spec);
			}
		}
	}

	private void evaluateElementDefinitions() {
		for (ElementDefinition definition : currentGraphLayout.elementDefinitions) {
			JValue result = evaluator.evaluate(definition.getGreqlString());
			if (result.isCollection()) {
				evaluateJValueSet(result, definition);
			} else if (result.isVertex() || result.isEdge()) {
				addAttributedElementToElementDefinition(definition, result);
			}
		}
	}

	private void evaluateJValueSet(JValue result, ElementDefinition definition) {
		JValueSet set = result.toJValueSet();

		for (JValue element : set) {
			addAttributedElementToElementDefinition(definition, element);
		}
	}

	private void addAttributedElementToElementDefinition(
			ElementDefinition definition, JValue result) {

		AttributedElement attributedElement = result.isEdge() ? result.toEdge()
				: result.toVertex();

		if (attributedElement != null) {
			definition.add(attributedElement);
			currentGraphLayout.attributedElementsDefinedByElementDefinitions
					.add(attributedElement);
		}
	}
}
