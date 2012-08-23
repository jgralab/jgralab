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

import org.pcollections.PSet;

import de.uni_koblenz.jgralab.JGraLab;
import de.uni_koblenz.jgralab.greql.OptimizerInfo;
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
	private BitSet typeIdSet;
	private Schema schema;
	private TcType tcType;

	private enum TcType {
		VERTEX, EDGE, UNKNOWN
	}

	private static final class TypeEntry {
		GraphElementClass<?, ?> cls;
		boolean exactType;
		boolean forbidden;

		public TypeEntry(GraphElementClass<?, ?> cls, boolean exactType,
				boolean forbidden) {
			this.cls = cls;
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
					&& cls.equals(o.cls);
		}

		@Override
		public int hashCode() {
			return cls.hashCode();
		}

		@Override
		public String toString() {
			return (forbidden ? "^" : "") + cls.getQualifiedName()
					+ (exactType ? "!" : "");
		}

		private boolean subsumes(TypeEntry o) {
			if (equals(o)) {
				return true;
			}
			if (exactType) {
				return false;
			}
			if (forbidden) {
				return cls.getAllSubClasses().contains(o.cls);
			}
			if (forbidden != o.forbidden) {
				return false;
			}
			return cls.getAllSubClasses().contains(o.cls);
		}
	}

	private static TypeCollection empty = new TypeCollection(null);

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
		return typeEntries == null;
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
		TypeEntry n = new TypeEntry(cls, exactType, forbidden);
		PSet<TypeEntry> t;
		if (typeEntries != null) {
			t = typeEntries;
			for (TypeEntry e : typeEntries) {
				if (e.subsumes(n)) {
					return this;
				} else if (n.subsumes(e)) {
					t = t.minus(e).plus(n);
				}
			}
			if (!t.contains(n)) {
				t = t.plus(n);
			}
		} else {
			t = JGraLab.<TypeEntry> set().plus(n);
		}
		return new TypeCollection(t);
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
		if (other == null || other.typeEntries == null) {
			return this;
		}
		if (typeEntries == null) {
			return other;
		}
		TypeCollection r = this;
		for (TypeEntry e : other.typeEntries) {
			r = r.with(e.cls, e.exactType, e.forbidden);
		}
		return r;
	}

	private TypeCollection(PSet<TypeEntry> types) {
		this.typeEntries = types;
		if (types != null) {
			BitSet a = null;
			BitSet f = null;
			boolean hasAllowedTypes = false;
			for (TypeEntry e : types) {
				if (schema == null) {
					schema = e.cls.getSchema();
					a = new BitSet(schema.getGraphElementClassCount());
					f = new BitSet(schema.getGraphElementClassCount());
				}
				if (!e.forbidden) {
					hasAllowedTypes = true;
				}
				TcType t = (e.cls instanceof VertexClass) ? TcType.VERTEX
						: TcType.EDGE;
				tcType = tcType == null ? t : t == tcType ? t : TcType.UNKNOWN;
				BitSet b = e.forbidden ? f : a;
				b.set(e.cls.getGraphElementClassIdInSchema());
				if (!e.exactType) {
					for (GraphElementClass<?, ?> sub : e.cls.getAllSubClasses()) {
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
		}
		if (tcType == null) {
			tcType = TcType.UNKNOWN;
		}
		// System.out.println(toString() + " " + tcType + " " + typeIdSet);
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null) {
			return false;
		}
		TypeCollection o = (TypeCollection) obj;
		return typeEntries == o.typeEntries
				|| typeEntries.equals(o.typeEntries);
	}

	@Override
	public int hashCode() {
		return typeEntries.hashCode();
	}

	public boolean acceptsType(GraphElementClass<?, ?> type) {
		return typeEntries == null
				|| typeIdSet.get(type.getGraphElementClassIdInSchema());
	}

	@Override
	public String toString() {
		if (typeIdSet == null) {
			return "{}";
		} else {
			StringBuffer sb = new StringBuffer();
			String delim = "{";
			for (TypeEntry e : typeEntries) {
				sb.append(delim).append(e.toString());
				delim = ", ";
			}
			return sb.append("}").toString();
		}
	}

	public double getFrequency(OptimizerInfo info) {
		if (typeEntries == null) {
			return 1.0;
		}
		double f = 0.0;
		double a = 0.0;
		boolean hasAllowedTypes = false;
		for (TypeEntry e : typeEntries) {
			if (e.forbidden) {
				f += e.exactType ? info
						.getFrequencyOfGraphElementClassWithoutSubclasses(e.cls)
						: info.getFrequencyOfGraphElementClass(e.cls);
			} else {
				a += e.exactType ? info
						.getFrequencyOfGraphElementClassWithoutSubclasses(e.cls)
						: info.getFrequencyOfGraphElementClass(e.cls);
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

	public TcType getTcType() {
		return tcType;
	}

	public BitSet getTypeIdSet() {
		return typeIdSet;
	}
}
