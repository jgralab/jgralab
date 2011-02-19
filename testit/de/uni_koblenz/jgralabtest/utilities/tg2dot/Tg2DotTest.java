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
package de.uni_koblenz.jgralabtest.utilities.tg2dot;

import java.io.IOException;

import org.junit.Test;

import de.uni_koblenz.jgralab.Graph;
import de.uni_koblenz.jgralab.GraphIO;
import de.uni_koblenz.jgralab.GraphIOException;
import de.uni_koblenz.jgralab.ProgressFunction;
import de.uni_koblenz.jgralab.WorkInProgress;
import de.uni_koblenz.jgralab.utilities.common.dot.GraphVizOutputFormat;
import de.uni_koblenz.jgralab.utilities.common.dot.GraphVizProgram;
import de.uni_koblenz.jgralab.utilities.tg2dot.Tg2Dot;
import de.uni_koblenz.jgralab.utilities.tg2image.Tg2Image;

@WorkInProgress(responsibleDevelopers = "mmce@uni-koblenz.de", description = "More test have to be included. Every static method should be tested. Additionally the class itself should be tested.")
public class Tg2DotTest {

	@Test
	public void convertGraph() throws GraphIOException {
		Graph g = GraphIO.loadGraphFromFileWithStandardSupport(
				"testit/testgraphs/greqltestgraph.tg", (ProgressFunction) null);
		Tg2Dot.convertGraph(g, "testit/testoutput.dot", false);
	}

	@Test
	public void convertGraph2Svg() throws GraphIOException,
			InterruptedException, IOException {

		Graph g = GraphIO.loadGraphFromFileWithStandardSupport(

		"testit/testgraphs/greqltestgraph.tg", (ProgressFunction) null);

		GraphVizProgram program = new GraphVizProgram().path("").outputFormat(
				GraphVizOutputFormat.PNG);

		Tg2Image.convertGraph2ImageFile(g, program, "testit/testoutput.png",
				false);
	}
}
