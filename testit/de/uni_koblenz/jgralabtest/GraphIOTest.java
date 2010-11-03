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

package de.uni_koblenz.jgralabtest;

import static junit.framework.Assert.assertEquals;
import junit.framework.Assert;

import org.junit.Test;

import de.uni_koblenz.jgralab.GraphIO;
import de.uni_koblenz.jgralab.GraphIOException;
import de.uni_koblenz.jgralab.grumlschema.GrumlSchema;
import de.uni_koblenz.jgralab.schema.Schema;

public class GraphIOTest {

	@Test
	public void testStringRead() throws Exception {
		GraphIO io = GraphIO.createStringReader(
				"this \"utf string\\nwith newline\" is f n a string",
				GrumlSchema.instance());
		io.match("this");
		String s = io.matchUtfString();
		assertEquals("utf string\nwith newline", s);
		io.match("is");
		Assert.assertFalse(io.matchBoolean());
		s = io.matchEnumConstant();
		assertEquals("n", s);
		io.match("a");
		io.match("string");
	}

	@Test
	public void testStringWrite() throws Exception {
		GraphIO io = GraphIO.createStringWriter(GrumlSchema.instance());

		io.writeUtfString("Umlaute: äöüÄÖÜß");
		assertEquals(
				"\"Umlaute: \\u00e4\\u00f6\\u00fc\\u00c4\\u00d6\\u00dc\\u00df\"",
				io.getStringWriterResult());

	}

	public static void main(String[] args) throws GraphIOException {
		Schema ioTest = GraphIO.loadSchemaFromFile("GraphIOTestInput.tg");

		GraphIO.saveSchemaToFile("GraphIOTestOutput.tg", ioTest);
		System.out.println("Fini.");
	}

}
