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
package de.uni_koblenz.jgralab.gretl;

import de.uni_koblenz.jgralab.Graph;
import de.uni_koblenz.jgralab.GraphIO;
import de.uni_koblenz.jgralab.gretl.parser.TokenTypes;
import de.uni_koblenz.jgralab.impl.ConsoleProgressFunction;

public class AddSourceGraph extends Transformation<Graph> {
	private String alias = null;
	private String graphFile = null;

	public AddSourceGraph(Context c, String alias, String graphFileName) {
		super(c);
		this.alias = alias;
		graphFile = graphFileName;
	}

	public AddSourceGraph(Context c, String graphFileName) {
		this(c, null, graphFileName);
	}

	public static AddSourceGraph parseAndCreate(ExecuteTransformation et) {
		String alias = null;
		if (et.tryMatchGraphAlias()) {
			alias = et.matchGraphAlias();
			// System.out.println("Matched alias " + alias);
		}
		String graphFile = et.match(TokenTypes.STRING).value;
		// System.out.println("Matched file " + graphFile);
		return new AddSourceGraph(et.context, alias, graphFile);
	}

	@Override
	protected Graph transform() {
		if (alias != null) {
			if (context.getSourceGraph(alias) != null) {
				return context.getSourceGraph(alias);
			}
		} else if (context.getSourceGraph() != null) {
			return context.getSourceGraph();
		}

		Graph g;
		try {
			g = GraphIO.loadGraphFromFile(graphFile,
					new ConsoleProgressFunction());
		} catch (Exception e) {
			throw new GReTLException(
					"Something went wrong loading source graph from "
							+ graphFile, e);
		}
		if (alias == null) {
			context.setSourceGraph(g);
		} else {
			context.addSourceGraph(alias, g);
		}
		return g;
	}

}
