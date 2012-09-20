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
import java.util.Iterator;

import org.pcollections.PSet;

import de.uni_koblenz.jgralab.JGraLab;
import de.uni_koblenz.jgralab.greql.OptimizerInfo;
import de.uni_koblenz.jgralab.greql.evaluator.InternalGreqlEvaluator;
import de.uni_koblenz.jgralab.greql.exception.UnknownTypeException;
import de.uni_koblenz.jgralab.schema.EdgeClass;
import de.uni_koblenz.jgralab.schema.GraphElementClass;
import de.uni_koblenz.jgralab.schema.Schema;
import de.uni_koblenz.jgralab.schema.VertexClass;

/**
 * Represents a set of allowed and forbidden types
 * 
 * @author ist@uni-koblenz.de
 * 
 */
public final class TypeCollection {

	private PSet<TypeEntry> typeEntries;

	// schema specific values, only valid when bound to a Schema
	private PSet<GraphElementClass<?, ?>> classEntries;
	private BitSet typeIdSet;
	private Schema schema;
	private int schemaVersion;
	private TcType tcType;

	private enum TcType {
		VERTEX, EDGE, UNKNOWN
	}

	private static final class TypeEntry {
		String typeName;
		boolean exactType;
		boolean forbidden;

		public TypeEntry(String typeName, boolean exactType, boolean forbidden) {
			this.typeName = typeName;
			this.exactType = exactType;
			this.forbidden = forbidden;
		}

		@Override
		public boolean equals(Object obj) {
			if (obj == null) {
				return false;
			}
			TypeEntry o = (TypeEntry) obj;
			return (exactType == o.exactType) && (forbidden == o.forbidden)
					&& typeName.equals(o.typeName);
		}

		@Override
		public int hashCode() {
			return typeName.hashCode();
		}

		@Override
		public String toString() {
			return (forbidden ? "^" : "") + typeName + (exactType ? "!" : "");
		}

		// private boolean subsumes(TypeEntry o) {
		// if (equals(o)) {
		// return true;
		// }
		// if (exactType) {
		// return false;
		// }
		// if (forbidden) {
		// return cls.getAllSubClasses().contains(o.cls);
		// }
		// if (forbidden != o.forbidden) {
		// return false;
		// }
		// return cls.getAllSubClasses().contains(o.cls);
		// }
	}

	private static TypeCollection empty = new TypeCollection(
			JGraLab.<TypeEntry> set());

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
		return typeEntries.isEmpty();
	}

	/**
	 * Creates a TypeCollection that contains <code>cls</code>. When
	 * <code>exactType</code> is true, the resulting TypeCollection only
	 * contains <code>cls</code>, otherwise <code>cls</code> together with all
	 * subclasses. When <code>forbidden</code> is true, the new TypeCollection
	 * will not accept <code>cls</code>.
	 * 
	 * @param typeName
	 *            the name of the type
	 * @param exactType
	 *            when true, only <code>typeName</code> is added, otherwise
	 *            <code>typeName</code> and its subclasses
	 * @param forbidden
	 *            when true, the resulting TypeCollection does not accept
	 *            <code>typeName</code>
	 * @return a new TypeCollection
	 */
	public TypeCollection with(String typeName, boolean exactType,
			boolean forbidden) {
		TypeEntry n = new TypeEntry(typeName, exactType, forbidden);
		if (typeEntries.contains(n)) {
			return this;
		}
		return new TypeCollection(typeEntries.plus(n));
		// PSet<TypeEntry> t;
		// if (typeEntries != null) {
		// t = typeEntries;
		// for (TypeEntry e : typeEntries) {
		// if (e.subsumes(n)) {
		// return this;
		// } else if (n.subsumes(e)) {
		// t = t.minus(e).plus(n);
		// }
		// }
		// if (!t.contains(n)) {
		// t = t.plus(n);
		// }
		// } else {
		// t = JGraLab.<TypeEntry> set().plus(n);
		// }
		// return new TypeCollection(t);
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
		if (other.isEmpty()) {
			return this;
		}
		if (isEmpty()) {
			return other;
		}
		return new TypeCollection(typeEntries.plusAll(other.typeEntries));
	}

	private TypeCollection(PSet<TypeEntry> types) {
		this.typeEntries = types;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null) {
			return false;
		}
		TypeCollection o = (TypeCollection) obj;
		return typeEntries.equals(o.typeEntries);
	}

	@Override
	public int hashCode() {
		return typeEntries.hashCode();
	}

	public boolean acceptsType(GraphElementClass<?, ?> type) {
		assert isBound();
		return isEmpty()
				|| typeIdSet.get(type.getGraphElementClassIdInSchema());
	}

	public boolean isBound() {
		return isEmpty() || schema != null;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		String delim = "{";
		for (TypeEntry e : typeEntries) {
			sb.append(delim).append(e.toString());
			delim = ", ";
		}
		return sb.append("}").toString();
	}

	public double getFrequency(OptimizerInfo info) {
		if (isEmpty()) {
			return 1.0;
		}
		if (schema == null) {
			throw new IllegalStateException(
					"TypeCollection isn't bound to a Schema");
		}
		double f = 0.0;
		double a = 0.0;
		boolean hasAllowedTypes = false;
		Iterator<GraphElementClass<?, ?>> it = classEntries.iterator();
		for (TypeEntry e : typeEntries) {
			GraphElementClass<?, ?> cls = it.next();
			if (e.forbidden) {
				f += e.exactType ? info
						.getFrequencyOfGraphElementClassWithoutSubclasses(cls)
						: info.getFrequencyOfGraphElementClass(cls);
			} else {
				a += e.exactType ? info
						.getFrequencyOfGraphElementClassWithoutSubclasses(cls)
						: info.getFrequencyOfGraphElementClass(cls);
				hasAllowedTypes = true;
			}
		}
		f = Math.min(1.0, f);
		if (hasAllowedTypes) {
			a = Math.min(1.0, a);
			return Math.max(a - f, 0.0);
		} else {
			return 1 - f;
		}
	}

	public long getEstimatedGraphElementCount(OptimizerInfo info) {
		if (schema == null) {
			throw new IllegalStateException(
					"TypeCollection isn't bound to a Schema");
		}
		switch (tcType) {
		case VERTEX:
			return (long) (getFrequency(info) * info.getAverageVertexCount());
		case EDGE:
			return (long) (getFrequency(info) * info.getAverageEdgeCount());
		default:
			return (long) (getFrequency(info) * (info.getAverageVertexCount() + info
					.getAverageEdgeCount()));
		}
	}

	public TypeCollection bindToSchema(InternalGreqlEvaluator evaluator) {
		Schema s = evaluator.getSchema();
		if (s == null) {
			throw new IllegalArgumentException(
					"Evaluator doesn't contain a Schema");
		}
		if (!s.isFinished()) {
			throw new IllegalStateException("Schema is not finished");
		}
		if (isEmpty()) {
			return this;
		}
		if (s == schema && s.getVersion() == schemaVersion) {
			// already bound and schema is unchanged
			return this;
		}
		return new TypeCollection(typeEntries, evaluator, evaluator.getSchema());
	}

	public TypeCollection bindToSchema(Schema s) {
		if (!s.isFinished()) {
			throw new IllegalStateException("Schema is not finished");
		}
		if (isEmpty()) {
			return this;
		}
		if (s == schema && s.getVersion() == schemaVersion) {
			// already bound and schema is unchanged
			return this;
		}
		return new TypeCollection(typeEntries, null, s);
	}

	private TypeCollection(PSet<TypeEntry> types,
			InternalGreqlEvaluator evaluator, Schema s) {
		this(types);
		schema = s;
		schemaVersion = s.getVersion();

		BitSet a = new BitSet(s.getGraphElementClassCount());
		BitSet f = new BitSet(s.getGraphElementClassCount());
		classEntries = JGraLab.set();
		boolean hasAllowedTypes = false;
		for (TypeEntry e : typeEntries) {
			if (!e.forbidden) {
				hasAllowedTypes = true;
			}
			GraphElementClass<?, ?> gec;
			if (evaluator != null) {
				gec = evaluator.getGraphElementClass(e.typeName);
			} else {
				gec = schema.getAttributedElementClass(e.typeName);
				if (gec == null) {
					throw new UnknownTypeException(e.typeName);
				}
			}
			classEntries = classEntries.plus(gec);
			TcType t = (gec instanceof VertexClass) ? TcType.VERTEX
					: TcType.EDGE;
			tcType = tcType == null ? t : t == tcType ? t : TcType.UNKNOWN;
			BitSet b = e.forbidden ? f : a;
			b.set(gec.getGraphElementClassIdInSchema());
			if (!e.exactType) {
				for (GraphElementClass<?, ?> sub : gec.getAllSubClasses()) {
					b.set(sub.getGraphElementClassIdInSchema());
				}
			}
		}
		if (!hasAllowedTypes) {
			typeIdSet = f;
			typeIdSet.flip(0, schema.getGraphElementClassCount());
		} else {
			typeIdSet = a;
			typeIdSet.andNot(f);
		}
		if (tcType == TcType.VERTEX) {
			for (EdgeClass ec : schema.getGraphClass().getEdgeClasses()) {
				typeIdSet.clear(ec.getGraphElementClassIdInSchema());
			}
		} else if (tcType == TcType.EDGE) {
			for (VertexClass vc : schema.getGraphClass().getVertexClasses()) {
				typeIdSet.clear(vc.getGraphElementClassIdInSchema());
			}
		}
		if (tcType == null) {
			tcType = TcType.UNKNOWN;
		}
		// System.out.println(toString() + " " + tcType + " " + typeIdSet);
	}

	public TcType getTcType() {
		if (schema == null) {
			throw new IllegalStateException(
					"TypeCollection isn't bound to a Schema");
		}
		return tcType;
	}

	public BitSet getTypeIdSet() {
		if (schema == null) {
			throw new IllegalStateException(
					"TypeCollection isn't bound to a Schema");
		}
		return typeIdSet;
	}

}
