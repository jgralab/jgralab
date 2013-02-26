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
package de.uni_koblenz.jgralab.impl.generic;

import java.io.IOException;

import de.uni_koblenz.jgralab.Graph;
import de.uni_koblenz.jgralab.GraphIO;
import de.uni_koblenz.jgralab.Vertex;
import de.uni_koblenz.jgralab.exception.GraphException;
import de.uni_koblenz.jgralab.exception.GraphIOException;
import de.uni_koblenz.jgralab.exception.NoSuchAttributeException;
import de.uni_koblenz.jgralab.impl.InternalGraph;
import de.uni_koblenz.jgralab.impl.RecordImpl;
import de.uni_koblenz.jgralab.impl.std.VertexImpl;
import de.uni_koblenz.jgralab.schema.Attribute;
import de.uni_koblenz.jgralab.schema.Domain;
import de.uni_koblenz.jgralab.schema.RecordDomain;
import de.uni_koblenz.jgralab.schema.Schema;
import de.uni_koblenz.jgralab.schema.VertexClass;

/**
 * A generic {@link Vertex}-Implementation that can represent vertices of
 * arbitrary {@link Schema}s.
 */
public class GenericVertexImpl extends VertexImpl implements
		InternalAttributesArrayAccess {

	private final VertexClass type;
	private Object[] attributes;

	protected GenericVertexImpl(VertexClass type, int id, Graph graph) {
		super(id, graph);
		if (type.isAbstract()) {
			graph.deleteVertex(this);
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
	}

	@Override
	public VertexClass getAttributedElementClass() {
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
		int i = type.getAttributeIndex(name);
		return (T) attributes[i];
	}

	@Override
	public <T> void setAttribute(String name, T data) {
		int i = type.getAttributeIndex(name);
		if (getAttributedElementClass().getAttribute(name).getDomain()
				.isConformValue(data)) {
			T oldValue = this.<T> getAttribute(name);
			graph.fireBeforeChangeAttribute(this, name, oldValue, data);
			attributes[i] = data;
			graph.fireAfterChangeAttribute(this, name, oldValue, data);
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
	public void initializeAttributesWithDefaultValues() {
		GenericGraphImpl.initializeGenericAttributeValues(this);
	}

	@Override
	public boolean isInstanceOf(VertexClass cls) {
		// Needs to be overridden from the base variant, because that relies on
		// code generation.
		return (type == cls) || type.isSubClassOf(cls);
	}

	@Override
	public void invokeOnAttributesArray(OnAttributesFunction fn) {
		attributes = fn.invoke(this, attributes);
	}

	// ************** unsupported methods ***************/

	/**
	 * This method is not supported by the generic implementation and therefore
	 * throws an {@link UnsupportedOperationException}.
	 */
	@Override
	public Class<? extends Vertex> getSchemaClass() {
		throw new UnsupportedOperationException(
				"This method is not supported by the generic implementation");
	}

}
