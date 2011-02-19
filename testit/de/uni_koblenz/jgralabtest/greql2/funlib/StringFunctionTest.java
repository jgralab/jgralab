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

import java.util.Arrays;

import org.junit.Test;

import de.uni_koblenz.jgralabtest.greql2.GenericTests;

public class StringFunctionTest extends GenericTests {

	@Test
	public void testConcatInfix() throws Exception {
		assertQueryEquals("\"foo\" ++ \"bar\" ++ \"baz\"", "foobarbaz");
		assertQueryEquals("'foo' ++ 'bar' ++ 'baz'", "foobarbaz");
		assertQueryEquals("'' ++ '' ++ ''", "");
		assertQueryEquals("'g' ++ 'g'", "gg");
	}

	@Test
	public void testConcat() throws Exception {
		assertQueryEquals("concat(concat(\"foo\", \"bar\"), \"baz\")",
				"foobarbaz");
		assertQueryEquals("concat(concat('foo', 'bar'), 'baz')", "foobarbaz");
		assertQueryEquals("concat(concat('', ''), '')", "");
		assertQueryEquals("concat('g', 'g')", "gg");
	}

	@Test
	public void testCapitalizeFirst() throws Exception {
		assertQueryEquals("capitalizeFirst('foobarbaz')", "Foobarbaz");
		assertQueryEquals("capitalizeFirst('foo bar baz')", "Foo bar baz");
		assertQueryEquals("capitalizeFirst(' oobarbaz')", " oobarbaz");
	}

	@Test
	public void testReMatchInfix() throws Exception {
		assertQueryEquals("'aaabbbb' =~ '[a]+[b]+'", true);
		assertQueryEquals("'aaa' =~ '[a]+[b]+'", false);
		assertQueryEquals("'aaabc' =~ '[a]+[b]+'", false);
	}

	@Test
	public void testReMatch() throws Exception {
		assertQueryEquals("reMatch('aaabbbb', '[a]+[b]+')", true);
		assertQueryEquals("reMatch('aaa', '[a]+[b]+')", false);
		assertQueryEquals("reMatch('aaabc', '[a]+[b]+')", false);
	}

	@Test
	public void testSplitt() throws Exception {

		assertQueryEquals("split('aaabc', '[a]+[b]+')", Arrays.asList("", "c"));
		assertQueryEqualsQuery("split('Eckhard-Großmann', '-')",
				"list('Eckhard', 'Großmann')");
		assertQueryEqualsQuery("split('aaa bc', ' ')", "list('aaa', 'bc')");
		assertQueryEqualsQuery("split('Software-Technik', '[-e]')",
				"list('Softwar', '', 'T', 'chnik')");
		assertQueryEqualsQuery("split('JGraLab', '\\p{javaLowerCase}')",
				"list('JG', '', 'L')");
	}
}
