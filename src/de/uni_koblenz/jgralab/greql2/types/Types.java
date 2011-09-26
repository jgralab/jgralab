package de.uni_koblenz.jgralab.greql2.types;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;

import org.pcollections.ArrayPSet;
import org.pcollections.ArrayPVector;
import org.pcollections.PMap;
import org.pcollections.POrderedSet;
import org.pcollections.PSet;
import org.pcollections.PVector;

import de.uni_koblenz.jgralab.AttributedElement;
import de.uni_koblenz.jgralab.Edge;
import de.uni_koblenz.jgralab.Graph;
import de.uni_koblenz.jgralab.GraphElement;
import de.uni_koblenz.jgralab.Record;
import de.uni_koblenz.jgralab.Vertex;
import de.uni_koblenz.jgralab.greql2.exception.GreqlException;

public class Types {
	private static final Class<?>[] GREQL_TYPES = { Integer.class, Long.class,
			Boolean.class, Double.class, String.class, Vertex.class,
			Edge.class, Graph.class, AttributedElement.class,
			GraphElement.class, Path.class, PathSystem.class, Slice.class,
			TypeCollection.class, Enum.class, Record.class, Table.class,
			Tuple.class, PVector.class, PSet.class, POrderedSet.class,
			PMap.class, Undefined.class };

	private static final HashMap<Class<?>, String> typeNames;
	private static final HashSet<Class<?>> types;

	static {
		typeNames = new HashMap<Class<?>, String>();
		types = new HashSet<Class<?>>();
		for (Class<?> cls : Types.GREQL_TYPES) {
			typeNames.put(cls, cls.getSimpleName());
			types.add(cls);
		}
		typeNames.put(PVector.class, "List");
		typeNames.put(PSet.class, "Set");
		typeNames.put(POrderedSet.class, "Set");
		typeNames.put(PMap.class, "Map");
	}

	public final boolean isGreqlType(Class<?> cls) {
		return types.contains(cls);
	}

	public static final String getGreqlTypeName(Object arg) {
		if (arg == null) {
			arg = Undefined.UNDEFINED;
		}
		for (Class<?> cls : GREQL_TYPES) {
			if (cls.isInstance(arg)) {
				return typeNames.get(cls);
			}
		}
		return arg.getClass().getSimpleName() + "[unknown to GReQL]";
	}

	public static final PVector<?> toPVector(Object o) {
		if (o instanceof ArrayPVector) {
			return (PVector<?>) o;
		} else if (o instanceof ArrayPSet) {
			return ((ArrayPSet<?>) o).toPVector();
		} else if (o instanceof Tuple) {
			return ((Tuple) o).toPVector();
		} else if (o instanceof Collection) {
			PVector<Object> r = ArrayPVector.empty();
			return r.plusAll((Collection<?>) o);
		}
		throw new GreqlException("Can't convert object of type "
				+ o.getClass().getName() + " to org.pcollections.PVector<?>");
	}
}
