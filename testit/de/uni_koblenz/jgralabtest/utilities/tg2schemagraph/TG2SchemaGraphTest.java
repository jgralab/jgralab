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
package de.uni_koblenz.jgralabtest.utilities.tg2schemagraph;

import static org.junit.Assert.fail;

import org.junit.Test;

import de.uni_koblenz.jgralab.GraphIO;
import de.uni_koblenz.jgralab.grumlschema.SchemaGraph;
import de.uni_koblenz.jgralab.schema.Schema;
import de.uni_koblenz.jgralab.utilities.tg2schemagraph.Schema2SchemaGraph;

public class TG2SchemaGraphTest {

	public void testSchema2SchemaGraph(String tgFile) {

		// Loads the Schema
		try {
			System.out.println("Testing with: " + tgFile);
			System.out.print("Loading TG and creating Schema ... ");
			Schema schema = GraphIO.loadSchemaFromFile(tgFile);
			System.out.println("\tdone");

			// Converts the Schema to a SchemaGraph
			System.out.print("Converting Schema to SchemaGraph ...");
			SchemaGraph schemaGraph = new Schema2SchemaGraph()
					.convert2SchemaGraph(schema);
			System.out.println("\tdone");

			// Compares the Schema with the created SchemaGraph
			System.out.print("Testing ...");
			new CompareSchemaWithSchemaGraph().compare(schema, schemaGraph);
			System.out.println("\t\t\t\tdone");
			System.out.println("Succesful!");
		} catch (Exception e) {
			System.out.println("An error occurred.\n");
			e.printStackTrace();
			fail(e.toString());
		} finally {
			System.out.println("\n");
		}

	}

	@Test
	public void testOsmSchema() {
		testSchema2SchemaGraph("testit/de/uni_koblenz/jgralabtest/utilities/tg2schemagraph/OsmSchema.rsa.tg");

	}

	@Test
	public void testGrumlSchema() {
		testSchema2SchemaGraph("testit/de/uni_koblenz/jgralabtest/utilities/tg2schemagraph/GrumlSchema.rsa.tg");
	}

	@Test
	public void testTest() {
		testSchema2SchemaGraph("testit/de/uni_koblenz/jgralabtest/utilities/tg2schemagraph/Test.rsa.tg");
	}

	@Test
	public void testTestSchema0() {
		testSchema2SchemaGraph("testit/de/uni_koblenz/jgralabtest/utilities/tg2schemagraph/testschema0.tg");
	}

	@Test
	public void testTestSchema1() {
		testSchema2SchemaGraph("testit/de/uni_koblenz/jgralabtest/utilities/tg2schemagraph/testschema1.tg");
	}

	@Test
	public void testCityMapSchema() {
		testSchema2SchemaGraph("testit/testschemas/citymapschema.tg");
	}

	@Test
	public void testCommentTestSchema() {
		testSchema2SchemaGraph("testit/testschemas/CommentTestSchema.tg");
	}

	@Test
	public void testConstraintSchema() {
		testSchema2SchemaGraph("testit/testschemas/ConstrainedSchema.tg");
	}

	@Test
	public void testJniTestSchema() {
		testSchema2SchemaGraph("testit/testschemas/jnitestschema.tg");
	}

	@Test
	public void testMinimalSchema() {
		testSchema2SchemaGraph("testit/testschemas/MinimalSchema.tg");
	}

	@Test
	public void testMotorWayMapSchema() {
		testSchema2SchemaGraph("testit/testschemas/motorwaymapschema.tg");
	}

	@Test
	public void testRecordTestSchema() {
		testSchema2SchemaGraph("testit/testschemas/RecordTestSchema.tg");
	}

	@Test
	public void testVertexTestSchema() {
		testSchema2SchemaGraph("testit/testschemas/VertexTestSchema.tg");
	}

}
