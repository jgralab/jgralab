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
package de.uni_koblenz.jgralab.impl.generic;

import java.io.IOException;

import de.uni_koblenz.jgralab.Edge;
import de.uni_koblenz.jgralab.EdgeDirection;
import de.uni_koblenz.jgralab.Graph;
import de.uni_koblenz.jgralab.GraphIO;
import de.uni_koblenz.jgralab.GraphIOException;
import de.uni_koblenz.jgralab.NoSuchAttributeException;
import de.uni_koblenz.jgralab.impl.std.ReversedEdgeImpl;
import de.uni_koblenz.jgralab.schema.EdgeClass;

public class GenericReversedEdgeImpl extends ReversedEdgeImpl {

	protected GenericReversedEdgeImpl(GenericEdgeImpl e, Graph g) {
		super(e, g);
	}

	@Override
	public EdgeClass getAttributedElementClass() {
		return normalEdge.getAttributedElementClass();
	}

	@Override
	public void readAttributeValueFromString(String attributeName, String value)
			throws GraphIOException, NoSuchAttributeException {
		throw new GraphIOException(
				"Can not call readAttributeValuesFromString for reversed Edges.");
	}

	@Override
	public Edge getNextIncidence(EdgeClass anEdgeClass) {
		return getNextIncidence(anEdgeClass, EdgeDirection.INOUT, false);
	}

	@Override
	public Edge getNextIncidence(EdgeClass anEdgeClass,
			EdgeDirection orientation) {
		return getNextIncidence(anEdgeClass, orientation, false);
	}

	@Override
	public Edge getNextIncidence(EdgeClass anEdgeClass,
			boolean noSubClasses) {
		return getNextIncidence(anEdgeClass, EdgeDirection.INOUT, noSubClasses);
	}

	@Override
	public Edge getNextIncidence(EdgeClass anEdgeClass,
			EdgeDirection orientation, boolean noSubclasses) {
		Edge currentEdge = getNextIncidence(orientation);
		while (currentEdge != null) {
			if (noSubclasses) {
				if (anEdgeClass.equals(currentEdge.getAttributedElementClass())) {
					return currentEdge;
				}
			} else {
				if (anEdgeClass.equals(currentEdge.getAttributedElementClass())
						|| anEdgeClass.getAllSubClasses().contains(
								currentEdge.getAttributedElementClass())) {
					return currentEdge;
				}
			}
			currentEdge = currentEdge.getNextIncidence(orientation);
		}
		return null;
	}

	@Override
	public String writeAttributeValueToString(String attributeName)
			throws IOException, GraphIOException, NoSuchAttributeException {
		throw new GraphIOException(
				"Can not call readAttributeValuesFromString for reversed Edges.");
	}

	@Override
	public void writeAttributeValues(GraphIO io) throws IOException,
			GraphIOException {
		throw new GraphIOException(
				"Can not call readAttributeValuesFromString for reversed Edges.");

	}

	@Override
	public void readAttributeValues(GraphIO io) throws GraphIOException {
		throw new GraphIOException(
				"Can not call readAttributeValuesFromString for reversed Edges.");
	}

	@Override
	public Edge getNextIncidence(Class<? extends Edge> anEdgeClass) {
		throw new UnsupportedOperationException(
				"Can not call readAttributeValuesFromString for reversed Edges.");
	}

	@Override
	public Edge getNextIncidence(Class<? extends Edge> anEdgeClass,
			EdgeDirection orientation) {
		throw new UnsupportedOperationException(
				"Can not call readAttributeValuesFromString for reversed Edges.");
	}

	@Override
	public Edge getNextIncidence(Class<? extends Edge> anEdgeClass,
			EdgeDirection orientation, boolean noSubclasses) {
		throw new UnsupportedOperationException(
				"Can not call readAttributeValuesFromString for reversed Edges.");
	}

	@Override
	public boolean isInstanceOf(EdgeClass cls) {
		// Needs to be overridden from the base variant, because that relies on
		// code generation.
		return getNormalEdge().isInstanceOf(cls);
	}
}
