package de.uni_koblenz.jgralab.utilities.tg2dot.graph_layout;

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
import de.uni_koblenz.jgralab.utilities.tg2dot.graph_layout.definition.DefinitionFactory;
import de.uni_koblenz.jgralab.utilities.tg2dot.graph_layout.definition.ElementDefinition;
import de.uni_koblenz.jgralab.utilities.tg2dot.graph_layout.definition.TemporaryDefinitionStruct;
import de.uni_koblenz.jgralab.utilities.tg2dot.graph_layout.definition.TypeDefinition;
import de.uni_koblenz.jgralab.utilities.tg2dot.greql2.GreqlEvaluatorFacade;
import de.uni_koblenz.jgralab.utilities.tg2dot.json.JsonTemporaryGraphLayoutReader;
import de.uni_koblenz.jgralab.utilities.tg2dot.plist.PListTemporaryGraphLayoutReader;
import static de.uni_koblenz.jgralab.utilities.tg2dot.greql2.GreqlEvaluatorFacade.*;

public class GraphLayoutFactory {

	private Schema schema;

	private GreqlEvaluatorFacade evaluator;

	private DefinitionFactory factory;

	private GraphLayout currentGraphLayout;

	public GraphLayout loadJsonGraphLayout(File graphLayoutFile) {

		System.out.println("Loading JSON-graph layout...");
		TemporaryGraphLayoutReader reader = createAndExecuteJsonGraphLayoutReader(graphLayoutFile);

		return loadGraphLayout(reader);
	}

	private TemporaryGraphLayoutReader createAndExecuteJsonGraphLayoutReader(
			File graphLayoutFile) {
		// GraphLayoutReader processor = new JsonGraphLayoutReader();
		TemporaryGraphLayoutReader reader = new JsonTemporaryGraphLayoutReader();

		try {
			reader.startProcessing(graphLayoutFile);
		} catch (FileNotFoundException e) {
			System.out.println("No JSON graph layout file defined.");
		}
		return reader;
	}

	public GraphLayout loadPListGraphLayout(File layout) {

		System.out.println("Loading PList-graph layout...");
		TemporaryGraphLayoutReader reader = createAndExecutePListGraphLayoutReader(layout);

		return loadGraphLayout(reader);
	}

	private TemporaryGraphLayoutReader createAndExecutePListGraphLayoutReader(
			File graphLayoutFile) {
		TemporaryGraphLayoutReader reader = new PListTemporaryGraphLayoutReader();

		try {
			reader.startProcessing(graphLayoutFile);
		} catch (FileNotFoundException e) {
			System.out.println("No PList graph layout file defined.");
		}
		return reader;
	}

	public GraphLayout loadDefautLayout() {
		return loadGraphLayout(new EmptyGraphLayoutReader());
	}

	public GraphLayout loadGraphLayout(TemporaryGraphLayoutReader reader) {

		validate();

		initializeProcessingStructures();
		startProcessing(reader);

		return currentGraphLayout;
	}

	private void initializeProcessingStructures() {

		factory = new DefinitionFactory(evaluator, schema);

		currentGraphLayout = new GraphLayout();
		currentGraphLayout.setSchema(schema);
	}

	private void validate() {
		if (schema == null || evaluator == null) {
			throw new RuntimeException(
					"The Schema, GreqlEvaluator or both are not set.");
		}
	}

	public void startProcessing(TemporaryGraphLayoutReader reader) {
		currentGraphLayout.initiateAllTypeDefinitions(factory);
		setDefaultLayout();
		currentGraphLayout.setGlobalVariables(reader.getGlobalVariables());
		constructDefinitions(factory, reader.getDefinitionList());
		applyHierarchieToTypeDefinitions();
		evaluateElementDefinitions();
	}

	private void setDefaultLayout() {
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

	private void constructDefinitions(DefinitionFactory factory,
			List<TemporaryDefinitionStruct> specificationList) {

		for (TemporaryDefinitionStruct struct : specificationList) {

			Definition definition = factory.produce(struct);
			currentGraphLayout.add(definition);
		}
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

	public void setGreqlEvaluator(GreqlEvaluatorFacade evaluator) {
		this.evaluator = evaluator;
	}

	public GreqlEvaluatorFacade getGreqlEvaluator() {
		return evaluator;
	}

	public void setSchema(Schema schema) {
		this.schema = schema;
	}

	public Schema getSchema() {
		return schema;
	}
}