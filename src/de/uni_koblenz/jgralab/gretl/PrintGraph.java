/*
 * JGraLab - The Java Graph Laboratory
 *
 * Copyright (C) 2006-2013 Institute for Software Technology
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
package de.uni_koblenz.jgralab.gretl;

import java.io.File;
import java.io.IOException;

import de.uni_koblenz.jgralab.Graph;
import de.uni_koblenz.jgralab.gretl.parser.TokenTypes;
import de.uni_koblenz.jgralab.utilities.tg2dot.Tg2Dot;
import de.uni_koblenz.jgralab.utilities.tg2dot.dot.GraphVizOutputFormat;

public class PrintGraph extends CountingTransformation {

	private File file;
	private Graph graph;

	protected PrintGraph(Context context, Graph g, File f) {
		super(context);
		this.file = f;
		this.graph = g;
	}

	public static PrintGraph parseAndCreate(ExecuteTransformation et) {
		String alias = Context.DEFAULT_SOURCE_GRAPH_ALIAS;
		if (et.tryMatchGraphAlias()) {
			alias = et.matchGraphAlias();
		}
		Graph g = null;
		if (alias.equals(Context.DEFAULT_TARGET_GRAPH_ALIAS)) {
			g = et.context.getTargetGraph();
		} else {
			g = et.context.getSourceGraph(alias);
		}
		File f = new File(et.match(TokenTypes.STRING).value);
		return new PrintGraph(et.context, g, f);
	}

	@Override
	protected Integer transform() {
		try {
			Tg2Dot.convertGraph(graph, file.getAbsolutePath(),
					GraphVizOutputFormat.PDF);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return 0;
	}

}
