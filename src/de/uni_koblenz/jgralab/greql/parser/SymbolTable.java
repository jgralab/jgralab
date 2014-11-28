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

package de.uni_koblenz.jgralab.greql.parser;

import java.util.List;

import de.uni_koblenz.jgralab.EdgeDirection;
import de.uni_koblenz.jgralab.Vertex;
import de.uni_koblenz.jgralab.greql.exception.DuplicateVariableException;
import de.uni_koblenz.jgralab.greql.schema.GreqlAggregation;
import de.uni_koblenz.jgralab.greql.schema.IsBoundVarOf;
import de.uni_koblenz.jgralab.greql.schema.IsDeclaredVarOf;
import de.uni_koblenz.jgralab.greql.schema.IsVarOf;
import de.uni_koblenz.jgralab.greql.schema.SourcePosition;
import de.uni_koblenz.jgralab.greql.schema.Variable;

public class SymbolTable extends SimpleSymbolTable {

	@Override
	public void insert(String ident, Vertex v)
			throws DuplicateVariableException {
		if (!list.getFirst().containsKey(ident)) {
			list.getFirst().put(ident, v);
		} else {
			Vertex var = list.getFirst().get(ident);
			int offset = -1;
			if (var.getFirstIncidence(EdgeDirection.OUT) instanceof IsDeclaredVarOf) {
				offset = ((IsDeclaredVarOf) var
						.getFirstIncidence(EdgeDirection.OUT))
						.get_sourcePositions().get(0).get_offset();
			} else if (var.getFirstIncidence(EdgeDirection.OUT) instanceof IsBoundVarOf) {
				offset = ((IsBoundVarOf) var
						.getFirstIncidence(EdgeDirection.OUT))
						.get_sourcePositions().get(0).get_offset();
			} else if (var.getFirstIncidence(EdgeDirection.OUT) instanceof IsVarOf) {
				offset = ((IsVarOf) var.getFirstIncidence(EdgeDirection.OUT))
						.get_sourcePositions().get(0).get_offset();
			}
			List<SourcePosition> sourcePositions = null;
			if (v.getFirstIncidence(EdgeDirection.IN) != null) {
				sourcePositions = ((GreqlAggregation) v.getFirstIncidence(EdgeDirection.IN))
						.get_sourcePositions();
			}
			
			throw new DuplicateVariableException((Variable) var,
					sourcePositions, new SourcePosition(offset,
							ident.length()));
		}
	}

}
