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
import de.uni_koblenz.jgralab.GraphException;
import de.uni_koblenz.jgralab.GraphIO;
import de.uni_koblenz.jgralab.GraphIOException;
import de.uni_koblenz.jgralab.NoSuchAttributeException;
import de.uni_koblenz.jgralab.Vertex;
import de.uni_koblenz.jgralab.impl.InternalGraph;
import de.uni_koblenz.jgralab.impl.RecordImpl;
import de.uni_koblenz.jgralab.impl.ReversedEdgeBaseImpl;
import de.uni_koblenz.jgralab.impl.std.EdgeImpl;
import de.uni_koblenz.jgralab.schema.AggregationKind;
import de.uni_koblenz.jgralab.schema.Attribute;
import de.uni_koblenz.jgralab.schema.Domain;
import de.uni_koblenz.jgralab.schema.EdgeClass;
import de.uni_koblenz.jgralab.schema.RecordDomain;

/**
 * A generic {@link Edge}-Implementation that can represent edges of arbitrary
 * {@link Schema}s.
 */
public class GenericEdgeImpl extends EdgeImpl implements
		InternalAttributesArrayAccess {

	private EdgeClass type;
	private Object[] attributes;

	public GenericEdgeImpl(EdgeClass type, int anId, Graph graph, Vertex alpha,
			Vertex omega) {
		super(anId, graph, alpha, omega);
		if (type.isAbstract()) {
			throw new GraphException(
					"Cannot create instances of abstract type " + type);
		}
		this.type = type;
		if (type.hasAttributes()) {
			attributes = new Object[type.getAttributeCount()];
			if (!((InternalGraph) graph).isLoading()) {
				GenericGraphImpl.initializeGenericAttributeValues(this);
			}
		}
		((GenericGraphImpl) graph).addEdge(this, alpha, omega);
	}

	@Override
	protected ReversedEdgeBaseImpl createReversedEdge() {
		return new GenericReversedEdgeImpl(this, graph);
	}

	@Override
	public EdgeClass getAttributedElementClass() {
		return type;
	}

	@Override
	public void readAttributeValueFromString(String attributeName, String value)
			throws GraphIOException, NoSuchAttributeException {
		int i = type.getAttributeIndex(attributeName);
		attributes[i] = type
				.getAttribute(attributeName)
				.getDomain()
				.parseGenericAttribute(
						GraphIO.createStringReader(value, getSchema()));
	}

	@Override
	public void readAttributeValues(GraphIO io) throws GraphIOException {
		for (Attribute a : type.getAttributeList()) {
			attributes[type.getAttributeIndex(a.getName())] = a.getDomain()
					.parseGenericAttribute(io);
		}
	}

	@Override
	public String writeAttributeValueToString(String attributeName)
			throws IOException, GraphIOException, NoSuchAttributeException {
		GraphIO io = GraphIO.createStringWriter(getSchema());
		type.getAttribute(attributeName).getDomain()
				.serializeGenericAttribute(io, getAttribute(attributeName));
		return io.getStringWriterResult();
	}

	@Override
	public void writeAttributeValues(GraphIO io) throws IOException,
			GraphIOException {
		for (Attribute a : type.getAttributeList()) {
			a.getDomain().serializeGenericAttribute(io,
					getAttribute(a.getName()));
		}
	}

	@Override
	public AggregationKind getAggregationKind() {
		AggregationKind fromAK = (getAttributedElementClass()).getFrom()
				.getAggregationKind();
		AggregationKind toAK = (getAttributedElementClass()).getTo()
				.getAggregationKind();
		return fromAK != AggregationKind.NONE ? fromAK
				: (toAK != AggregationKind.NONE ? toAK : AggregationKind.NONE);
	}

	@Override
	public AggregationKind getAlphaAggregationKind() {
		return getAttributedElementClass().getFrom().getAggregationKind();
	}

	@Override
	public AggregationKind getOmegaAggregationKind() {
		return getAttributedElementClass().getTo().getAggregationKind();
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> T getAttribute(String name) {
		int i = type.getAttributeIndex(name);
		return (T) attributes[i];
	}

	@Override
	public <T> void setAttribute(String name, T data) {
		int i = type.getAttributeIndex(name);
		if (getAttributedElementClass().getAttribute(name).getDomain()
				.isConformValue(data)) {
			if (graph.hasECARuleManager()) {
				T oldValue = this.getAttribute(name);
				graph.getECARuleManager().fireBeforeChangeAttributeEvents(this,
						name, oldValue, data);
				attributes[i] = data;
				graph.getECARuleManager().fireAfterChangeAttributeEvents(this,
						name, oldValue, data);
			} else {
				attributes[i] = data;
			}
		} else {
			Domain d = type.getAttribute(name).getDomain();
			throw new ClassCastException(("Expected "
					+ ((d instanceof RecordDomain) ? RecordImpl.class.getName()
							: d.getJavaAttributeImplementationTypeName(d
									.getPackageName()))
					+ " object, but received " + data) == null ? (data
					.getClass().getName() + " object instead") : data
					+ " instead");
		}
	}

	@Override
	public Edge getNextEdge(EdgeClass anEdgeClass) {
		Edge currentEdge = getNextEdge();
		while (currentEdge != null) {
			if (currentEdge.getAttributedElementClass().equals(anEdgeClass)
					|| currentEdge.getAttributedElementClass()
							.getAllSuperClasses().contains(anEdgeClass)) {
				return currentEdge;
			}
			currentEdge = currentEdge.getNextEdge();
		}
		return currentEdge;
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
	public Edge getNextIncidence(EdgeClass anEdgeClass, boolean noSubClasses) {
		return getNextIncidence(anEdgeClass, EdgeDirection.INOUT, noSubClasses);
	}

	@Override
	public Edge getNextIncidence(EdgeClass anEdgeClass,
			EdgeDirection orientation, boolean noSubclasses) {
		Edge currentEdge = getNextIncidence(orientation);
		while (currentEdge != null) {
			if (noSubclasses) {
				if (currentEdge.getAttributedElementClass().equals(anEdgeClass)) {
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
		return currentEdge;
	}

	@Override
	public void initializeAttributesWithDefaultValues() {
		GenericGraphImpl.initializeGenericAttributeValues(this);
	}

	// ************** unsupported methods ***************/

	/**
	 * This method is not supported by the generic implementation and therefore
	 * throws an {@link UnsupportedOperationException}.
	 */
	@Override
	public Class<? extends Edge> getSchemaClass() {
		throw new UnsupportedOperationException(
				"This method is not supported by the generic implementation");
	}

	/**
	 * This method is not supported by the generic implementation and therefore
	 * throws an {@link UnsupportedOperationException}.
	 */
	@Override
	public boolean isInstanceOf(EdgeClass cls) {
		// Needs to be overridden from the base variant, because that relies on
		// code generation.
		return type.equals(cls) || type.isSubClassOf(cls);
	}

	@Override
	public void invokeOnAttributesArray(OnAttributesFunction fn) {
		attributes = fn.invoke(this, attributes);
	}

}
