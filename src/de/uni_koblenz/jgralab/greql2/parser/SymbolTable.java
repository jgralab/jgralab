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

package de.uni_koblenz.jgralab.greql2.parser;

import de.uni_koblenz.jgralab.EdgeDirection;
import de.uni_koblenz.jgralab.Vertex;
import de.uni_koblenz.jgralab.greql2.exception.DuplicateVariableException;
import de.uni_koblenz.jgralab.greql2.schema.Greql2Aggregation;
import de.uni_koblenz.jgralab.greql2.schema.IsBoundVarOf;
import de.uni_koblenz.jgralab.greql2.schema.IsDeclaredVarOf;
import de.uni_koblenz.jgralab.greql2.schema.IsVarOf;
import de.uni_koblenz.jgralab.greql2.schema.Variable;
import de.uni_koblenz.jgralab.greql2.schema.impl.std.SourcePositionImpl;

public class SymbolTable extends EasySymbolTable {

	@Override
	public void insert(String ident, Vertex v)
			throws DuplicateVariableException {

		if (!list.getFirst().containsKey(ident)) {
			list.getFirst().put(ident, v);
		} else {
			Vertex var = list.getFirst().get(ident);
			int offset = -1;
			if (var.getFirstEdge(EdgeDirection.OUT) instanceof IsDeclaredVarOf) {
				offset = ((IsDeclaredVarOf) var.getFirstEdge(EdgeDirection.OUT))
						.get_sourcePositions().get(0).get_offset();
			} else if (var.getFirstEdge(EdgeDirection.OUT) instanceof IsBoundVarOf) {
				offset = ((IsBoundVarOf) var.getFirstEdge(EdgeDirection.OUT))
						.get_sourcePositions().get(0).get_offset();
			} else if (var.getFirstEdge(EdgeDirection.OUT) instanceof IsVarOf) {
				offset = ((IsVarOf) var.getFirstEdge(EdgeDirection.OUT))
						.get_sourcePositions().get(0).get_offset();
			}
			throw new DuplicateVariableException((Variable) var,
					((Greql2Aggregation) v.getFirstEdge(EdgeDirection.IN))
					// .get_sourcePositions(), new SourcePosition(offset,
							// ident.length()));
							// .get_sourcePositions(), new SourcePositionImpl(v
							// .getGraph(), offset, ident.length()));
							.get_sourcePositions(), v.getGraph().createRecord(
							SourcePositionImpl.class, offset, ident.length()));
		}
	}

}
