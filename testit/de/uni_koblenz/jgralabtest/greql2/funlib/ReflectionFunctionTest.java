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

import org.junit.Test;

import de.uni_koblenz.jgralabtest.greql2.GenericTest;

public class ReflectionFunctionTest extends GenericTest {

	@Test
	public void testHasJValueType() throws Exception {
		// GRAPH
		assertQueryEquals("hasJValueType(getGraph(), 'GRAPH')", true);
		assertQueryEquals("hasJValueType(getGraph(), 'ATTRELEM')", true);
		assertQueryEquals("hasJValueType(getGraph(), 'OBJECT')", true);

		// VERTEX
		assertQueryEquals("hasJValueType(getVertex(1), 'VERTEX')", true);
		assertQueryEquals("hasJValueType(getVertex(1), 'ATTRELEM')", true);
		assertQueryEquals("hasJValueType(getVertex(1), 'OBJECT')", true);

		// EDGE
		assertQueryEquals("hasJValueType(getEdge(1), 'EDGE')", true);
		assertQueryEquals("hasJValueType(getEdge(1), 'ATTRELEM')", true);
		assertQueryEquals("hasJValueType(getEdge(1), 'OBJECT')", true);

		// BOOL
		assertQueryEquals("hasJValueType(true, 'BOOL')", true);
		assertQueryEquals("hasJValueType(false, 'BOOL')", true);
		assertQueryEquals("hasJValueType(false, 'OBJECT')", true);

		// INT
		assertQueryEquals("hasJValueType(17, 'INT')", true);
		assertQueryEquals("hasJValueType(17, 'NUMBER')", true);
		assertQueryEquals("hasJValueType(17, 'LONG')", true);
		assertQueryEquals("hasJValueType(17, 'AUTOMATON')", false);

		// STRING
		assertQueryEquals("hasJValueType('17', 'STRING')", true);
		assertQueryEquals("hasJValueType(\"17\", 'STRING')", true);
		assertQueryEquals("hasJValueType('17', 'OBJECT')", true);
		assertQueryEquals("hasJValueType(\"17\", 'OBJECT')", true);
		assertQueryEquals("hasJValueType('17', 'AUTOMATON')", false);

		// TODO: Check more types...
		
		// assertQueryEquals("hasJValueType(,)", true);
	}
}
