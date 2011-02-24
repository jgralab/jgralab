/*
 * JGraLab - The Java Graph Laboratory
 * 
 * Copyright (C) 2006-2011 Institute for Software Technology
 *                         University of Koblenz-Landau, Germany
 *                         ist@uni-koblenz.de
 * 
 * For bug reports, documentation and further information, visit
 * 
 *                         http://jgralab.uni-koblenz.de
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
package de.uni_koblenz.jgralabtest.greql2.funlib;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.Test;

import de.uni_koblenz.jgralab.Graph;
import de.uni_koblenz.jgralab.greql2.jvalue.JValue;
import de.uni_koblenz.jgralab.greql2.jvalue.JValueBoolean;
import de.uni_koblenz.jgralab.schema.Attribute;
import de.uni_koblenz.jgralab.schema.AttributedElementClass;
import de.uni_koblenz.jgralab.schema.EnumDomain;
import de.uni_koblenz.jgralab.schema.Schema;
import de.uni_koblenz.jgralab.schema.VertexClass;
import de.uni_koblenz.jgralabtest.greql2.GenericTests;

public class SchemaFunctionTest extends GenericTests {

	@Test
	public void testAttributeNames() throws Exception {
		Graph currentGraph = this.getTestGraph(TestVersion.CITY_MAP_GRAPH);
		Schema schema = currentGraph.getSchema();
		testAttributeNames(schema.getVertexClassesInTopologicalOrder());
		testAttributeNames(schema.getEdgeClassesInTopologicalOrder());
	}

	private void testAttributeNames(
			List<? extends AttributedElementClass> classes) throws Exception {
		for (AttributedElementClass clazz : classes) {
			testAttributeNames(clazz);
		}
	}

	private void testAttributeNames(AttributedElementClass clazz)
			throws Exception {
		assertQueryEquals("attributeNames(type('" + clazz.getQualifiedName()
				+ "'))", getAttributeNames(clazz.getAttributeList()));
	}

	private Set<String> getAttributeNames(Set<Attribute> attributes) {
		Set<String> result = new HashSet<String>(attributes.size());
		for (Attribute attribute : attributes) {
			result.add(attribute.getName());
		}
		return result;
	}

	@Test
	public void testAttributes() throws Exception {
		Graph currentGraph = this.getTestGraph(TestVersion.CITY_MAP_GRAPH);
		Schema schema = currentGraph.getSchema();
		testAttributes(schema.getVertexClassesInTopologicalOrder());
		testAttributes(schema.getEdgeClassesInTopologicalOrder());
	}

	private void testAttributes(List<? extends AttributedElementClass> classes)
			throws Exception {
		for (AttributedElementClass clazz : classes) {
			testAttributes(clazz);
		}
	}

	private void testAttributes(AttributedElementClass clazz) throws Exception {
		assertQueryEquals("attributes(type('" + clazz.getQualifiedName()
				+ "'))", getAttributes(clazz.getAttributeList()));
	}

	private Set<List<String>> getAttributes(Set<Attribute> attributes) {
		Set<List<String>> result = new HashSet<List<String>>(attributes.size());
		for (Attribute attribute : attributes) {

			ArrayList<String> tup = new ArrayList<String>();
			tup.add(attribute.getName());
			tup.add(attribute.getDomain().getQualifiedName());
			result.add(tup);
		}
		return result;
	}

	@Test
	public void testEnumConstant() throws Exception {
		Graph currentGraph = this.getTestGraph(TestVersion.CITY_MAP_GRAPH);
		Schema schema = currentGraph.getSchema();

		for (final EnumDomain enumDomain : schema.getEnumDomains()) {
			String enumDomainName = enumDomain.getQualifiedName();
			for (String enumConst : enumDomain.getConsts()) {
				JValue result = evalTestQuery("enumConstant('" + enumDomainName
						+ "', '" + enumConst + "')");
				assertEquals(enumConst, result.toEnum().name());
			}
		}
	}

	@Test
	public void testHasAttribute() throws Exception {
		Graph currentGraph = this.getTestGraph(TestVersion.CITY_MAP_GRAPH);
		Schema schema = currentGraph.getSchema();
		testHasAttribute(schema.getVertexClassesInTopologicalOrder());
		testHasAttribute(schema.getEdgeClassesInTopologicalOrder());
	}

	private void testHasAttribute(List<? extends AttributedElementClass> classes)
			throws Exception {
		for (AttributedElementClass clazz : classes) {
			testHasAttribute(clazz);
		}
	}

	private void testHasAttribute(AttributedElementClass clazz)
			throws Exception {
		for (Attribute attribute : clazz.getAttributeList()) {
			String query = "hasAttribute(type('" + clazz.getQualifiedName()
					+ "'), '" + attribute.getName() + "')";
			assertQueryEquals(query, true);
			System.out.println(query);
		}
	}

	@Test
	public void testHasAttributeFails() throws Exception {
		assertQueryEquals(
				"hasAttribute(type('junctions.Crossroad'), 'jajslkdasd')",
				false);
		assertQueryEquals("hasAttribute(type('junctions.Crossroad'), 'Name')",
				false);
	}

	@Test
	public void testHasType() throws Exception {
		Graph currentGraph = this.getTestGraph(TestVersion.CITY_MAP_GRAPH);
		Schema schema = currentGraph.getSchema();
		testHasType(schema.getVertexClassesInTopologicalOrder());
		testHasType(schema.getEdgeClassesInTopologicalOrder());
	}

	private void testHasType(List<? extends AttributedElementClass> classes)
			throws Exception {
		for (AttributedElementClass clazz : classes) {
			if (clazz.isInternal() || clazz.isAbstract()) {
				continue;
			}
			testHasTypeForSubTypes(clazz.getQualifiedName(), clazz);
			testHasTypeForSuperTypes(clazz.getQualifiedName(), clazz);
		}
	}

	private String queryForTestType = "hasType(%s{%s!}[0], '%s')";

	private void testHasTypeForSubTypes(String qualifiedName,
			AttributedElementClass clazz) throws Exception {

		boolean equal = qualifiedName.equals(clazz.getQualifiedName());
		String formatedString = String.format(queryForTestType,
				dertermineClassTypeChar(clazz), qualifiedName,
				clazz.getQualifiedName());
		System.out.println(formatedString);
		assertQueryEquals(formatedString, equal);

		for (AttributedElementClass elementClass : clazz.getAllSubClasses()) {
			testHasTypeForSubTypes(qualifiedName, elementClass);
		}
	}

	private String dertermineClassTypeChar(AttributedElementClass clazz) {
		String type = clazz instanceof VertexClass ? "V" : "E";
		return type;
	}

	private void testHasTypeForSuperTypes(String qualifiedName,
			AttributedElementClass clazz) throws Exception {

		boolean equal = qualifiedName.equals(clazz.getQualifiedName());
		String formatedString = String.format(queryForTestType,
				dertermineClassTypeChar(clazz), qualifiedName,
				clazz.getQualifiedName());
		System.out.println(formatedString);
		assertQueryEquals(formatedString, true);

		for (AttributedElementClass elementClass : clazz.getAllSuperClasses()) {
			testHasTypeForSuperTypes(qualifiedName, elementClass);
		}

	}

	@Test
	public void testIsA() throws Exception {
		String queryString = "isA(\"Variable\", \"Identifier\")";
		JValue result = evalTestQuery("IsA", queryString);
		assertEquals(JValueBoolean.getTrueValue(), result.toBoolean());
	}

	@Test
	public void testTypes() throws Exception {
		// TODO: Broken, because the GReQL parser removes all WhereExpressions
		// and LetExpressions!
		String queryString = "from x : V{WhereExpression} report types(edgesConnected(x)) end";
		JValue result = evalTestQuery("EdgeTypeSet", queryString);
		assertEquals(3, getNthValue(result.toCollection(), 0).toCollection()
				.size());
	}

}
