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

package de.uni_koblenz.jgralab.greql.types;

import java.util.BitSet;
import java.util.HashSet;
import java.util.TreeSet;

import de.uni_koblenz.jgralab.schema.GraphElementClass;
import de.uni_koblenz.jgralab.schema.Schema;

/**
 * Represents a set of allowed and forbidden types
 * 
 * @author ist@uni-koblenz.de
 * 
 */
public class TypeCollection {
	private HashSet<GraphElementClass<?, ?>> allowedTypes;
	private HashSet<GraphElementClass<?, ?>> forbiddenTypes;
	private BitSet typeIdSet;
	private Schema schema;

	private static TypeCollection empty = new TypeCollection();

	/**
	 * @return an empty TypeCollection that accepts all types
	 */
	public static TypeCollection empty() {
		return empty;
	}

	/**
	 * @return true if this TypeCollection is empty
	 */
	public boolean isEmpty() {
		return typeIdSet == null;
	}

	/**
	 * Creates a TypeCollection that contains <code>cls</code>. When
	 * <code>exactType</code> is true, the resulting TypeCollection only
	 * contains <code>cls</code>, otherwise <code>cls</code> together with all
	 * subclasses. When <code>forbidden</code> is true, the new TypeCollection
	 * will not accept <code>cls</code>.
	 * 
	 * @param cls
	 *            a {@link GraphElementClass}
	 * @param exactType
	 *            when true, only <code>cls</code> is added, otherwise
	 *            <code>cls</code> and its subclasses
	 * @param forbidden
	 *            when true, the resulting TypeCollection does not accept
	 *            <code>cls</code>
	 * @return a new TypeCollection
	 */
	public TypeCollection with(GraphElementClass<?, ?> cls, boolean exactType,
			boolean forbidden) {
		return new TypeCollection(cls, exactType, forbidden);
	}

	/**
	 * Creates a new TypeCollection that combines allowed and forbidden types of
	 * this TypeCollection with the <code>other</code> TypeCollection.
	 * 
	 * @param other
	 *            a TypeCollection
	 * @return a new TypeCollection combining this TypeCollection with
	 *         <code>other</code>
	 */
	public TypeCollection combine(TypeCollection other) {
		if (other == null || other.typeIdSet == null) {
			return this;
		} else if (typeIdSet == null) {
			return other;
		} else {
			// copy this
			TypeCollection r = new TypeCollection();
			r.schema = schema;
			r.allowedTypes.addAll(allowedTypes);
			r.forbiddenTypes.addAll(forbiddenTypes);
			// combine other
			r.allowedTypes.addAll(other.allowedTypes);
			r.forbiddenTypes.addAll(other.forbiddenTypes);
			r.computeTypes();
			return r;
		}
	}

	private TypeCollection() {
		allowedTypes = new HashSet<GraphElementClass<?, ?>>();
		forbiddenTypes = new HashSet<GraphElementClass<?, ?>>();
	}

	private TypeCollection(GraphElementClass<?, ?> cls, boolean exactType,
			boolean forbidden) {
		this();
		schema = cls.getSchema();
		(forbidden ? forbiddenTypes : allowedTypes).add(cls);
		if (!exactType) {
			(forbidden ? forbiddenTypes : allowedTypes).addAll(cls
					.getAllSubClasses());
		}
		computeTypes();
	}

	private void computeTypes() {
		typeIdSet = new BitSet(schema.getGraphElementClassCount());
		if (allowedTypes.isEmpty()) {
			typeIdSet.set(0, typeIdSet.size() - 1);
		} else {
			for (GraphElementClass<?, ?> cls : allowedTypes) {
				typeIdSet.set(cls.getGraphElementClassIdInSchema());
			}
		}
		for (GraphElementClass<?, ?> cls : forbiddenTypes) {
			typeIdSet.clear(cls.getGraphElementClassIdInSchema());
		}
	}

	@Override
	public boolean equals(Object o) {
		if (o == null || !(o instanceof TypeCollection)) {
			return false;
		}
		return allowedTypes.equals(((TypeCollection) o).allowedTypes)
				&& forbiddenTypes.equals(((TypeCollection) o).forbiddenTypes);
	}

	@Override
	public int hashCode() {
		return allowedTypes.hashCode() + forbiddenTypes.hashCode();
	}

	public final boolean acceptsType(GraphElementClass<?, ?> type) {
		return typeIdSet == null
				|| typeIdSet.get(type.getGraphElementClassIdInSchema());
	}

	@Override
	public String toString() {
		if (typeIdSet == null) {
			return "{}";
		} else {
			TreeSet<GraphElementClass<?, ?>> t = new TreeSet<GraphElementClass<?, ?>>(
					allowedTypes);
			t.removeAll(forbiddenTypes);
			StringBuffer sb = new StringBuffer();
			String delim = "{";
			for (GraphElementClass<?, ?> aec : t) {
				sb.append(delim).append(aec.getQualifiedName());
				delim = ", ";
			}
			return sb.append("}").toString();
		}
	}

	public BitSet getTypeIdSet() {
		return typeIdSet;
	}
}
