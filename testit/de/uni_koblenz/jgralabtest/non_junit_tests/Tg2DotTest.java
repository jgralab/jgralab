/*
 * JGraLab - The Java Graph Laboratory
 *
 * Copyright (C) 2006-2012 Institute for Software Technology
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
package de.uni_koblenz.jgralabtest.non_junit_tests;

import java.io.IOException;

import org.junit.Test;

import de.uni_koblenz.jgralab.Graph;
import de.uni_koblenz.jgralab.GraphIO;
import de.uni_koblenz.jgralab.GraphIOException;
import de.uni_koblenz.jgralab.schema.Schema;
import de.uni_koblenz.jgralab.utilities.tg2dot.Tg2Dot;
import de.uni_koblenz.jgralab.utilities.tg2dot.dot.GraphVizLayouter;
import de.uni_koblenz.jgralab.utilities.tg2dot.dot.GraphVizOutputFormat;
import de.uni_koblenz.jgralab.utilities.tg2schemagraph.Schema2SchemaGraph;

public class Tg2DotTest {

	public void convertGraph() throws GraphIOException, IOException {
		Graph g = GraphIO.loadGraphFromFile(
				"testit/testgraphs/greqltestgraph.tg", null);
		Tg2Dot.convertGraph(g, "testit/testdata/testoutput.dot");
	}

	public void convertGraph2Svg() throws GraphIOException, IOException {
		Graph g = GraphIO.loadGraphFromFile(
				"testit/testgraphs/greqltestgraph.tg", null);
		Tg2Dot t2d = new Tg2Dot();
		t2d.setGraph(g);
		t2d.setGraphVizOutputFormat(GraphVizOutputFormat.SVG);
		t2d.setOutputFile("testit/testdata/testoutput.svg");
		t2d.convert();
	}

	public void convertGraph2Png() throws GraphIOException, IOException {
		Graph g = GraphIO.loadGraphFromFile(
				"testit/testgraphs/greqltestgraph.tg", null);
		Tg2Dot t2d = new Tg2Dot();
		t2d.setGraph(g);
		t2d.setGraphVizOutputFormat(GraphVizOutputFormat.PNG);
		t2d.setOutputFile("testit/testdata/testoutput.png");
		t2d.convert();
	}

	@Test
	public void convertSchema2png() throws GraphIOException, IOException {
		Schema schema = GraphIO
				.loadSchemaFromFile("src/de/uni_koblenz/jgralab/greql2/greql2Schema.tg");
		Schema2SchemaGraph converter = new Schema2SchemaGraph();
		Graph g = converter.convert2SchemaGraph(schema);
		Tg2Dot t2d = new Tg2Dot();
		t2d.setGraph(g);
		t2d.setGraphVizOutputFormat(GraphVizOutputFormat.PNG);
		t2d.setGraphVizLayouter(GraphVizLayouter.DOT);
		t2d.setOutputFile("testit/testdata/greql2Schema.png");
		t2d.convert();
	}
}
