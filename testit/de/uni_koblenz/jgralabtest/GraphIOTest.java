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

package de.uni_koblenz.jgralabtest;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertTrue;
import static org.junit.Assert.assertNull;

import java.io.File;

import org.junit.Test;

import de.uni_koblenz.jgralab.GraphIO;
import de.uni_koblenz.jgralab.exception.GraphIOException;
import de.uni_koblenz.jgralab.grumlschema.GrumlSchema;
import de.uni_koblenz.jgralab.impl.TgLexer.Token;

public class GraphIOTest {

	@Test
	public void testStringRead() throws Exception {
		GraphIO io;

		// test some tokens
		io = GraphIO
				.createStringReader(
						"Schema 123 -456 this \"utf string\\nwith newline\" is f t n ENUM a string",
						GrumlSchema.instance());
		assertEquals("Schema", io.matchGetText(Token.SCHEMA));
		assertEquals(123, io.matchInteger());
		assertEquals(-456, io.matchInteger());
		assertEquals("this", io.matchGetText(Token.TEXT));
		assertEquals("utf string\nwith newline", io.matchUtfString());
		assertEquals("is", io.matchGetText(Token.TEXT));
		assertFalse(io.matchBoolean());
		assertTrue(io.matchBoolean());
		assertNull(io.matchEnumConstant());
		assertEquals("ENUM", io.matchEnumConstant());
		assertEquals("a", io.matchGetText(Token.TEXT));
		assertEquals("string", io.matchGetText(Token.TEXT));
		io.match(Token.EOF);

		// test some escaped unicodes
		io = GraphIO
				.createStringReader(
						"\"Umlaute: \\u00e4\\u00f6\\u00fc\\u00c4\\u00d6\\u00dc\\u00df\"",
						GrumlSchema.instance());
		assertEquals("Umlaute: äöüÄÖÜß", io.matchUtfString());
		io.match(Token.EOF);

		// create a long string to test TgLexer
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < 512; ++i) {
			sb.append(i);
		}
		String s = sb.toString();
		io = GraphIO
				.createStringReader("\"" + s + "\"", GrumlSchema.instance());
		assertEquals(s, io.matchUtfString());
		io.match(Token.EOF);
	}

	@Test
	public void testStringWrite() throws Exception {
		GraphIO io = GraphIO.createStringWriter(GrumlSchema.instance());
		io.writeUtfString("Umlaute: äöüÄÖÜß");
		assertEquals(
				"\"Umlaute: \\u00e4\\u00f6\\u00fc\\u00c4\\u00d6\\u00dc\\u00df\"",
				io.getStringWriterResult());
	}

	@Test
	public void testLoadCityMap() {
		try {
			File dir = new File(".");
			dir.getAbsolutePath();
			GraphIO.loadGraphFromFile(dir.getAbsolutePath()
					+ "/testit/testgraphs/citymapgraph.tg", null);
		} catch (GraphIOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
