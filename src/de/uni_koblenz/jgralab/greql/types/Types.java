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
package de.uni_koblenz.jgralab.greql.types;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;

import org.pcollections.ArrayPSet;
import org.pcollections.ArrayPVector;
import org.pcollections.PCollection;
import org.pcollections.PMap;
import org.pcollections.POrderedSet;
import org.pcollections.PSet;
import org.pcollections.PVector;

import de.uni_koblenz.jgralab.AttributedElement;
import de.uni_koblenz.jgralab.Edge;
import de.uni_koblenz.jgralab.Graph;
import de.uni_koblenz.jgralab.GraphElement;
import de.uni_koblenz.jgralab.JGraLab;
import de.uni_koblenz.jgralab.Record;
import de.uni_koblenz.jgralab.TraversalContext;
import de.uni_koblenz.jgralab.Vertex;
import de.uni_koblenz.jgralab.greql.evaluator.InternalGreqlEvaluator;
import de.uni_koblenz.jgralab.greql.evaluator.fa.FiniteAutomaton;
import de.uni_koblenz.jgralab.greql.exception.GreqlException;
import de.uni_koblenz.jgralab.schema.AttributedElementClass;

public class Types {
	private static final Class<?>[] GREQL_TYPES = { Integer.class, Long.class,
			Boolean.class, Double.class, Number.class, String.class,
			Vertex.class, Edge.class, Graph.class, AttributedElement.class,
			GraphElement.class, Path.class, PathSystem.class,
			TypeCollection.class, Enum.class, Record.class, Table.class,
			Tuple.class, PVector.class, PSet.class, POrderedSet.class,
			PCollection.class, PMap.class, AttributedElementClass.class,
			FiniteAutomaton.class, TraversalContext.class, Undefined.class,
			InternalGreqlEvaluator.class };

	private static final HashMap<Class<?>, String> typeNames;
	private static final HashSet<Class<?>> types;

	static {
		typeNames = new HashMap<>();
		types = new HashSet<>();
		for (Class<?> cls : Types.GREQL_TYPES) {
			typeNames.put(cls, cls.getSimpleName());
			types.add(cls);
		}
		typeNames.put(PVector.class, "List");
		typeNames.put(PSet.class, "Set");
		typeNames.put(POrderedSet.class, "Set");
		typeNames.put(PMap.class, "Map");
		typeNames.put(PCollection.class, "Collection");
		typeNames.put(FiniteAutomaton.class, "PathDescription");
	}

	private Types() {
		// nobody ever needs an instance...
	}

	public static final boolean isValidGreqlValue(Object value) {
		if (value == null) {
			return true;
		}
		for (Class<?> cls : GREQL_TYPES) {
			if (cls.isInstance(value)) {
				return true;
			}
		}
		return false;
	}

	public static String[] getGreqlTypeNames() {
		String[] names = new String[typeNames.size()];
		int i = 0;
		for (String n : typeNames.values()) {
			names[i] = n;
			i++;
		}
		return names;
	}

	public static final String getGreqlTypeName(Object value) {
		if (value == null) {
			value = Undefined.UNDEFINED;
		}
		for (Class<?> cls : GREQL_TYPES) {
			if (cls.isInstance(value)) {
				return typeNames.get(cls);
			}
		}
		return value.getClass().getSimpleName() + " [unknown to GReQL]";
	}

	public static final String getGreqlTypeName(Class<?> cls) {
		String tn = typeNames.get(cls);
		if (tn != null) {
			return tn;
		}
		return cls.getSimpleName();
	}

	public static final PVector<?> toPVector(Object o) {
		if (o instanceof ArrayPVector) {
			return (PVector<?>) o;
		} else if (o instanceof ArrayPSet) {
			return ((ArrayPSet<?>) o).toPVector();
		} else if (o instanceof Tuple) {
			return ((Tuple) o).toPVector();
		} else if (o instanceof Collection) {
			return JGraLab.vector().plusAll((Collection<?>) o);
		}
		throw new GreqlException("Can't convert object of type "
				+ o.getClass().getName() + " to org.pcollections.PVector<?>");
	}
}
