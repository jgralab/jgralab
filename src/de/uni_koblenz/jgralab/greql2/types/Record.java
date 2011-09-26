package de.uni_koblenz.jgralab.greql2.types;

import java.io.IOException;
import java.util.List;

import org.pcollections.ArrayPSet;
import org.pcollections.PMap;

import de.uni_koblenz.jgralab.GraphIO;
import de.uni_koblenz.jgralab.GraphIOException;
import de.uni_koblenz.jgralab.JGraLab;

public class Record implements de.uni_koblenz.jgralab.Record {
	private PMap<String, Object> entries;

	private Record() {
		entries = JGraLab.map();
	}

	private Record(PMap<String, Object> m) {
		entries = m;
	}

	private static Record empty = new Record();

	public static Record empty() {
		return empty;
	}

	public Record plus(String name, Object value) {
		return new Record(entries.plus(name, value));
	}

	@Override
	public Object getComponent(String name) {
		return entries.get(name);
	}

	@Override
	public void writeComponentValues(GraphIO io) throws IOException,
			GraphIOException {
		throw new UnsupportedOperationException();
	}

	@Override
	public List<String> getComponentNames() {
		return ((ArrayPSet<String>) entries.keySet()).toPVector();
	}

	@Override
	public PMap<String, Object> toPMap() {
		return entries;
	}
}
