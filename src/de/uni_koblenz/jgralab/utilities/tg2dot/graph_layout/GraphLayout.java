/*
 * JGraLab - The Java Graph Laboratory
 *
 * Copyright (C) 2006-2014 Institute for Software Technology
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
package de.uni_koblenz.jgralab.utilities.tg2dot.graph_layout;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import de.uni_koblenz.jgralab.AttributedElement;
import de.uni_koblenz.jgralab.Edge;
import de.uni_koblenz.jgralab.Vertex;
import de.uni_koblenz.jgralab.schema.AttributedElementClass;
import de.uni_koblenz.jgralab.schema.EdgeClass;
import de.uni_koblenz.jgralab.schema.Schema;
import de.uni_koblenz.jgralab.schema.VertexClass;
import de.uni_koblenz.jgralab.utilities.tg2dot.graph_layout.definition.Definition;
import de.uni_koblenz.jgralab.utilities.tg2dot.graph_layout.definition.ElementDefinition;
import de.uni_koblenz.jgralab.utilities.tg2dot.graph_layout.definition.TypeDefinition;

public class GraphLayout {

	Map<VertexClass, TypeDefinition> vertexTypeDefinitions;

	Map<EdgeClass, TypeDefinition> edgeTypeDefinitions;

	List<ElementDefinition> elementDefinitions;

	Collection<AttributedElement<?, ?>> attributedElementsDefinedByElementDefinitions;

	Map<String, String> globalVariables;

	private Schema schema;

	public GraphLayout() {
		initiateDataStructures();
	}

	private void initiateDataStructures() {
		vertexTypeDefinitions = new HashMap<>();
		edgeTypeDefinitions = new HashMap<>();
		elementDefinitions = new ArrayList<>();

		attributedElementsDefinedByElementDefinitions = new HashSet<>();

		globalVariables = new HashMap<>();
	}

	void setSchema(Schema schema) {
		this.schema = schema;
	}

	public Schema getSchema() {
		return schema;
	}

	public Map<VertexClass, TypeDefinition> getVertexTypeDefinitions() {
		return vertexTypeDefinitions;
	}

	public Map<EdgeClass, TypeDefinition> getEdgeTypeDefinitions() {
		return edgeTypeDefinitions;
	}

	public List<ElementDefinition> getElementDefinitions() {
		return elementDefinitions;
	}

	public Collection<AttributedElement<?, ?>> getAttributedElementsDefinedByElementDefinitions() {
		return attributedElementsDefinedByElementDefinitions;
	}

	public TypeDefinition getTypeDefinition(AttributedElement<?, ?> element) {
		return getTypeDefinition(element.getAttributedElementClass());
	}

	public TypeDefinition getTypeDefinition(String attributedElementClassName) {

		// 4.12.12, af: Differentiation between default Vertex/Edge class and
		// schema-specific element calsses is necessary as Vertex and Edge no
		// longer are part of the schema and are therefore not found by the
		// getAttributedElement() method!
		if (attributedElementClassName.compareTo("Vertex") == 0) {
			return getTypeDefinition(schema.getGraphClass()
					.getDefaultVertexClass());
		} else if (attributedElementClassName.compareTo("Edge") == 0) {
			return getTypeDefinition(schema.getGraphClass()
					.getDefaultEdgeClass());
		} else {
			return getTypeDefinition(schema
					.getAttributedElementClass(attributedElementClassName));
		}

	}

	public TypeDefinition getTypeDefinition(AttributedElementClass<?, ?> type) {
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

	void initiateAllTypeDefinitions() {
		VertexClass defaultVC = schema.getGraphClass().getDefaultVertexClass();
		vertexTypeDefinitions.put(defaultVC, new TypeDefinition(defaultVC));
		for (VertexClass type : schema.getGraphClass().getVertexClasses()) {
			vertexTypeDefinitions.put(type, new TypeDefinition(type));
		}

		EdgeClass defaultEC = schema.getGraphClass().getDefaultEdgeClass();
		edgeTypeDefinitions.put(defaultEC, new TypeDefinition(defaultEC));
		for (EdgeClass type : schema.getGraphClass().getEdgeClasses()) {
			edgeTypeDefinitions.put(type, new TypeDefinition(type));
		}
	}

	public void add(Definition definition) {
		if (definition instanceof TypeDefinition) {
			add((TypeDefinition) definition);
		} else if (definition instanceof ElementDefinition) {
			add((ElementDefinition) definition);
		} else {
			throw new RuntimeException("This shouldn'd have happend!");
		}
	}

	private void add(TypeDefinition definition) {
		TypeDefinition originalSpec = getTypeDefinition(definition
				.getTypeClass());
		originalSpec.overwriteAttributes(definition);
	}

	private void add(ElementDefinition definition) {
		elementDefinitions.add(definition);
	}

	public boolean isDefinedbyElementDefinitions(AttributedElement<?, ?> v) {
		return attributedElementsDefinedByElementDefinitions.contains(v);
	}

	public void setGlobalVariables(Map<String, String> globalVariables) {
		this.globalVariables = globalVariables;
	}
}
