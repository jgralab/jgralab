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
import java.util.Map;

import org.pcollections.POrderedSet;

import de.uni_koblenz.jgralab.AttributedElement;
import de.uni_koblenz.jgralab.Edge;
import de.uni_koblenz.jgralab.Graph;
import de.uni_koblenz.jgralab.GraphException;
import de.uni_koblenz.jgralab.GraphIO;
import de.uni_koblenz.jgralab.GraphIOException;
import de.uni_koblenz.jgralab.NoSuchAttributeException;
import de.uni_koblenz.jgralab.Record;
import de.uni_koblenz.jgralab.Vertex;
import de.uni_koblenz.jgralab.VertexFilter;
import de.uni_koblenz.jgralab.impl.EdgeIterable;
import de.uni_koblenz.jgralab.impl.RecordImpl;
import de.uni_koblenz.jgralab.impl.VertexIterable;
import de.uni_koblenz.jgralab.impl.std.GraphImpl;
import de.uni_koblenz.jgralab.schema.Attribute;
import de.uni_koblenz.jgralab.schema.BasicDomain;
import de.uni_koblenz.jgralab.schema.BooleanDomain;
import de.uni_koblenz.jgralab.schema.Domain;
import de.uni_koblenz.jgralab.schema.DoubleDomain;
import de.uni_koblenz.jgralab.schema.EdgeClass;
import de.uni_koblenz.jgralab.schema.EnumDomain;
import de.uni_koblenz.jgralab.schema.GraphClass;
import de.uni_koblenz.jgralab.schema.IntegerDomain;
import de.uni_koblenz.jgralab.schema.LongDomain;
import de.uni_koblenz.jgralab.schema.RecordDomain;
import de.uni_koblenz.jgralab.schema.RecordDomain.RecordComponent;
import de.uni_koblenz.jgralab.schema.VertexClass;

/**
 * A generic {@link Graph}-Implementation that can represent TGraphs of
 * arbitrary {@link Schema}s.
 */
public class GenericGraphImpl extends GraphImpl implements
		InternalAttributesArrayAccess {

	private GraphClass type;
	private Object[] attributes;

	protected GenericGraphImpl(GraphClass type, String id, int vmax, int emax) {
		super(id, type, vmax, emax);
		this.type = type;
		if (type.hasAttributes()) {
			attributes = new Object[type.getAttributeCount()];
			if (!isLoading()) {
				GenericGraphImpl.initializeGenericAttributeValues(this);
			}
		}
	}

	/**
	 * Creates a new {@link GenericVertexImpl} in the graph that conforms to a
	 * given {@Link VertexClass} from the Schema.
	 */
	@Override
	public <T extends Vertex> T createVertex(VertexClass vc) {
		return graphFactory.createVertex(vc, 0, this);
	}

	/**
	 * Creates a new {@Link GenericEdgeImpl} in the Graph that conforms
	 * to a given {@link EdgeClass} from the Schema.
	 */
	@Override
	public <T extends Edge> T createEdge(EdgeClass ec, Vertex alpha,
			Vertex omega) {
		return graphFactory.createEdge(ec, 0, this, alpha, omega);
	}

	@Override
	public GraphClass getAttributedElementClass() {
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

	@SuppressWarnings("unchecked")
	@Override
	public <T> T getAttribute(String name) {
		int i = getAttributedElementClass().getAttributeIndex(name);
		return (T) attributes[i];
	}

	@Override
	public <T> void setAttribute(String name, T data) {
		int i = getAttributedElementClass().getAttributeIndex(name);
		if (type.getAttribute(name).getDomain().isConformValue(data)) {
			if (hasECARuleManager()) {
				T oldValue = getAttribute(name);
				getECARuleManager().fireBeforeChangeAttributeEvents(this, name,
						oldValue, data);
				attributes[i] = data;
				getECARuleManager().fireAfterChangeAttributeEvents(this, name,
						oldValue, data);
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
	public Vertex getFirstVertex(VertexClass vertexClass) {
		Vertex v = getFirstVertex();
		if (v == null) {
			return null;
		}
		if (v.getAttributedElementClass().equals(vertexClass)
				|| v.getAttributedElementClass().getAllSuperClasses()
						.contains(vertexClass)) {
			return v;
		}
		return v.getNextVertex(vertexClass);
	}

	@Override
	public Edge getFirstEdge(EdgeClass edgeClass) {
		Edge e = getFirstEdge();
		if (e == null) {
			return null;
		}
		if (e.getAttributedElementClass().equals(edgeClass)
				|| e.getAttributedElementClass().getAllSuperClasses()
						.contains(edgeClass)) {
			return e;
		}
		return e.getNextEdge(edgeClass);
	}

	@Override
	public Iterable<Vertex> vertices(VertexClass vc, VertexFilter<Vertex> filter) {
		return new VertexIterable<Vertex>(this, vc, filter);
	}

	@Override
	public Iterable<Edge> edges(EdgeClass ec) {
		return new EdgeIterable<Edge>(this, ec);
	}

	@Override
	public void initializeAttributesWithDefaultValues() {
		initializeGenericAttributeValues(this);
	}

	/**
	 * Returns the default value for attributes in the generic implementation if
	 * there is no explicitly defined default value, according to the
	 * attribute's domain.
	 * 
	 * @param domain
	 *            The attribute's domain.
	 * @return The default value for attributes of the domain.
	 */
	public static Object genericAttributeDefaultValue(Domain domain) {
		if (domain instanceof BasicDomain) {
			if (domain instanceof BooleanDomain) {
				return Boolean.valueOf(false);
			} else if (domain instanceof IntegerDomain) {
				return Integer.valueOf(0);
			} else if (domain instanceof LongDomain) {
				return Long.valueOf(0);
			} else if (domain instanceof DoubleDomain) {
				return Double.valueOf(0.0);
			} else { // StringDomain
				return null;
			}
		} else {
			return null;
		}
	}

	/**
	 * Initializes attributes of an (generic) AttributedElement with their
	 * default values.
	 */
	static void initializeGenericAttributeValues(AttributedElement<?, ?> ae) {
		for (Attribute attr : ae.getAttributedElementClass().getAttributeList()) {
			if ((attr.getDefaultValueAsString() != null)
					&& !attr.getDefaultValueAsString().isEmpty()) {
				try {
					attr.setDefaultValue(ae);
				} catch (GraphIOException e) {
					e.printStackTrace();
				}
			} else {
				ae.setAttribute(attr.getName(),
						genericAttributeDefaultValue(attr.getDomain()));
			}
		}
	}

	@Override
	public boolean isInstanceOf(GraphClass cls) {
		// Needs to be overridden from the base variant, because that relies on
		// code generation.
		return type.equals(cls);
	}

	@Override
	public Object getEnumConstant(EnumDomain enumDomain, String constantName) {
		for (String cn : enumDomain.getConsts()) {
			if (cn.equals(constantName)) {
				return cn;
			}
		}
		throw new GraphException("No such enum constant '" + constantName
				+ "' in EnumDomain " + enumDomain);
	}

	@Override
	public Record createRecord(RecordDomain recordDomain,
			Map<String, Object> values) {
		RecordImpl record = RecordImpl.empty();
		for (RecordComponent c : recordDomain.getComponents()) {
			if (!values.containsKey(c.getName())) {
				throw new GraphException("The provided Map misses a "
						+ c.getName() + " key!");
			}
			record = record.plus(c.getName(), values.get(c.getName()));
		}
		return record;
	}

	// ************** unsupported methods ***************/

	/**
	 * This method is not supported by the generic implementation and therefore
	 * throws an {@link UnsupportedOperationException}.
	 */
	@Override
	public Class<? extends Graph> getSchemaClass() {
		throw new UnsupportedOperationException(
				"This method is not supported by the generic implementation");
	}

	/**
	 * This method is not supported by the generic implementation and therefore
	 * throws an {@link UnsupportedOperationException}.
	 */
	@Override
	public Vertex getFirstVertex(Class<? extends Vertex> vertexClass) {
		throw new UnsupportedOperationException(
				"This method is not supported by the generic implementation");
	}

	/**
	 * This method is not supported by the generic implementation and therefore
	 * throws an {@link UnsupportedOperationException}.
	 */
	@Override
	public Edge getFirstEdge(Class<? extends Edge> edgeClass) {
		throw new UnsupportedOperationException(
				"This method is not supported by the generic implementation");
	}

	/**
	 * This method is not supported by the generic implementation and therefore
	 * throws an {@link UnsupportedOperationException}.
	 */
	@Override
	public Iterable<Edge> edges(Class<? extends Edge> edgeClass) {
		throw new UnsupportedOperationException(
				"This method is not supported by the generic implementation");
	}

	/**
	 * This method is not supported by the generic implementation and therefore
	 * throws an {@link UnsupportedOperationException}.
	 */
	@Override
	public <T extends Vertex> POrderedSet<T> reachableVertices(
			Vertex startVertex, String pathDescription, Class<T> vertexType) {
		throw new UnsupportedOperationException(
				"This method is not supported by the generic implementation");
	}

	@Override
	public void invokeOnAttributesArray(OnAttributesFunction fn) {
		attributes = fn.invoke(this, attributes);
	}
}
