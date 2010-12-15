package de.uni_koblenz.jgralab.utilities.tg2dot.graph_layout;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import de.uni_koblenz.jgralab.AttributedElement;
import de.uni_koblenz.jgralab.Edge;
import de.uni_koblenz.jgralab.Vertex;
import de.uni_koblenz.jgralab.schema.AttributedElementClass;
import de.uni_koblenz.jgralab.schema.EdgeClass;
import de.uni_koblenz.jgralab.schema.Schema;
import de.uni_koblenz.jgralab.schema.VertexClass;
import de.uni_koblenz.jgralab.utilities.tg2dot.graph_layout.definition.Definition;
import de.uni_koblenz.jgralab.utilities.tg2dot.graph_layout.definition.DefinitionFactory;
import de.uni_koblenz.jgralab.utilities.tg2dot.graph_layout.definition.ElementDefinition;
import de.uni_koblenz.jgralab.utilities.tg2dot.graph_layout.definition.EmptyDefinition;
import de.uni_koblenz.jgralab.utilities.tg2dot.graph_layout.definition.TemporaryDefinitionStruct;
import de.uni_koblenz.jgralab.utilities.tg2dot.graph_layout.definition.TypeDefinition;
import de.uni_koblenz.jgralab.utilities.tg2dot.json.JsonTemporaryGraphLayoutReader;

public class GraphLayout {

	Map<AttributedElementClass, TypeDefinition> vertexTypeDefinitions;

	Map<AttributedElementClass, TypeDefinition> edgeTypeDefinitions;

	List<ElementDefinition> elementDefinitions;

	Collection<AttributedElement> attributedElementsDefinedByElementDefinitions;

	Map<String, String> globalVariables;

	private Schema schema;

	private PrintStream stream;

	public GraphLayout() {
		initiateDataStructures();
		setVerbose(false);
	}

	private void initiateDataStructures() {
		vertexTypeDefinitions = new HashMap<AttributedElementClass, TypeDefinition>();
		edgeTypeDefinitions = new HashMap<AttributedElementClass, TypeDefinition>();
		elementDefinitions = new ArrayList<ElementDefinition>();

		attributedElementsDefinedByElementDefinitions = new HashSet<AttributedElement>();

		globalVariables = new HashMap<String, String>();
	}

	void setSchema(Schema schema) {
		this.schema = schema;
	}

	public Schema getSchema() {
		return schema;
	}

	public Map<AttributedElementClass, TypeDefinition> getVertexTypeDefinitions() {
		return vertexTypeDefinitions;
	}

	public Map<AttributedElementClass, TypeDefinition> getEdgeTypeDefinitions() {
		return edgeTypeDefinitions;
	}

	public List<ElementDefinition> getElementDefinitions() {
		return elementDefinitions;
	}

	public Collection<AttributedElement> getAttributedElementsDefinedByElementDefinitions() {
		return attributedElementsDefinedByElementDefinitions;
	}

	public TypeDefinition getTypeDefinition(AttributedElement element) {
		return getTypeDefinition(element.getAttributedElementClass());
	}

	public TypeDefinition getTypeDefinition(String attributedElementClassName) {
		return getTypeDefinition(schema
				.getAttributedElementClass(attributedElementClassName));
	}

	public TypeDefinition getTypeDefinition(AttributedElementClass type) {
		TypeDefinition definition = vertexTypeDefinitions.get(type);
		if (definition == null) {
			definition = edgeTypeDefinitions.get(type);
		}
		return definition;
	}

	public TypeDefinition getTypeDefinition(Vertex vertex) {
		return vertexTypeDefinitions.get(vertex.getAttributedElementClass());
	}

	public TypeDefinition getTypeDefinition(Edge edge) {
		return edgeTypeDefinitions.get(edge.getAttributedElementClass());
	}

	public Map<String, String> getGlobalVariables() {
		return globalVariables;
	}

	void initiateAllTypeDefinitions(DefinitionFactory factory) {
		for (VertexClass type : schema.getVertexClassesInTopologicalOrder()) {
			vertexTypeDefinitions.put(type, factory.produce(type));
		}

		for (EdgeClass type : schema.getEdgeClassesInTopologicalOrder()) {
			edgeTypeDefinitions.put(type, factory.produce(type));
		}
	}

	public void add(Definition definition) {
		if (definition instanceof TypeDefinition) {
			add((TypeDefinition) definition);
		} else if (definition instanceof ElementDefinition) {
			add((ElementDefinition) definition);
		} else if (definition instanceof EmptyDefinition) {
			add((EmptyDefinition) definition);
		} else {
			throw new RuntimeException("This shouldn'd have happend!");
		}
	}

	private void add(EmptyDefinition definition) {
		stream.println("Error: A definition couldn't be resolved to be a type or a element definition."
				+ " Either the qualified name is wrong or it is not a GReQL-query.");
		stream.println("Error: The definitions name is: \""
				+ definition.struct.name + "\"");
		stream.println("Error: This definition will be dropped!");
	}

	private void add(TypeDefinition definition) {
		TypeDefinition originalSpec = getTypeDefinition(definition
				.getTypeClass());
		originalSpec.overwriteAttributes(definition);
	}

	private void add(ElementDefinition definition) {
		elementDefinitions.add(definition);
	}

	public boolean isDefinedbyElementDefinitions(AttributedElement v) {
		return attributedElementsDefinedByElementDefinitions.contains(v);
	}

	public void setVerbose(boolean isVerbose) {
		stream = isVerbose ? System.out : new PrintStream(new OutputStream() {

			@Override
			public void write(int b) throws IOException {
			}
		});
	}

	protected void printDebugInformations(JsonTemporaryGraphLayoutReader my) {

		stream.println("\nRead Specification:");
		printTemporaryDefinitions(my.getDefinitionList());

		stream.println("\nSpecification");
		printDefinitions(vertexTypeDefinitions);
		printDefinitions(edgeTypeDefinitions);
	}

	private static void printTemporaryDefinitions(
			List<TemporaryDefinitionStruct> specificationList) {

		for (TemporaryDefinitionStruct t : specificationList) {
			System.out.print(t.name + ": {");
			String delimiter = "";
			for (Entry<String, String> entry : t.getAttributeList().entrySet()) {
				System.out.print(delimiter);
				delimiter = ", ";
				System.out.print(entry.getKey() + " = " + entry.getValue());
			}
			System.out.println("}");
		}
	}

	private void printDefinitions(
			Map<AttributedElementClass, TypeDefinition> specificationList) {

		for (Entry<AttributedElementClass, TypeDefinition> entry : specificationList
				.entrySet()) {

			TypeDefinition spec = entry.getValue();
			AttributedElementClass type = entry.getKey();

			stream.print(type.getQualifiedName() + " "
					+ spec.getTypeClass().getQualifiedName() + ": {");
			String delimiter = " ";
			for (String attributeName : spec.getAttributeNames()) {
				stream.print(delimiter);
				delimiter = ", ";
				stream.print(attributeName + " = "
						+ spec.getAttributeValue(attributeName));
			}
			stream.println("}");
		}
	}

	public void setGlobalVariables(Map<String, String> globalVariables) {
		this.globalVariables = globalVariables;
	}
}