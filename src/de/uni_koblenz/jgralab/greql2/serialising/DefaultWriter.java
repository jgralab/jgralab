package de.uni_koblenz.jgralab.greql2.serialising;

import java.util.Iterator;
import java.util.Map.Entry;

import org.pcollections.PMap;
import org.pcollections.PSet;
import org.pcollections.PVector;

import de.uni_koblenz.jgralab.Edge;
import de.uni_koblenz.jgralab.Graph;
import de.uni_koblenz.jgralab.Vertex;
import de.uni_koblenz.jgralab.graphmarker.SubGraphMarker;
import de.uni_koblenz.jgralab.greql2.evaluator.fa.DFA;
import de.uni_koblenz.jgralab.greql2.evaluator.fa.NFA;
import de.uni_koblenz.jgralab.greql2.evaluator.fa.State;
import de.uni_koblenz.jgralab.greql2.evaluator.fa.Transition;
import de.uni_koblenz.jgralab.greql2.exception.SerialisingException;
import de.uni_koblenz.jgralab.greql2.schema.Declaration;
import de.uni_koblenz.jgralab.greql2.types.Path;
import de.uni_koblenz.jgralab.greql2.types.PathSystem;
import de.uni_koblenz.jgralab.greql2.types.Record;
import de.uni_koblenz.jgralab.greql2.types.Slice;
import de.uni_koblenz.jgralab.greql2.types.Table;
import de.uni_koblenz.jgralab.greql2.types.Tuple;
import de.uni_koblenz.jgralab.greql2.types.TypeCollection;
import de.uni_koblenz.jgralab.schema.AttributedElementClass;

public class DefaultWriter {

	public void writePSet(PSet<?> s) throws Exception {
		Iterator<?> iter = s.iterator();
		boolean first = true;
		pre();
		while (iter.hasNext()) {
			if (first) {
				first = false;
			} else {
				inter();
			}
			this.write(iter.next());
		}
		post();
	}

	public void writeTable(Table<?> t) throws Exception {
		this.writePVector(t.getTitles());
		this.writePVector(t.toPVector());
	}

	public void writePVector(PVector<?> b) throws Exception {
		Iterator<?> iter = b.iterator();
		boolean first = true;
		pre();
		while (iter.hasNext()) {
			if (first) {
				first = false;
			} else {
				inter();
			}
			this.write(iter.next());
		}
		post();
	}

	public void writePMap(PMap<?, ?> b) throws Exception {
		boolean first = true;
		pre();
		for (Entry<?, ?> e : b.entrySet()) {
			if (first) {
				first = false;
			} else {
				inter();
			}
			Tuple t = Tuple.empty();
			t = t.plus(e.getKey());
			t = t.plus(e.getValue());
			this.writeTuple(t);
		}
		post();
	}

	public void writeTuple(Tuple t) throws Exception {
		Iterator<?> iter = t.iterator();
		boolean first = true;
		pre();
		while (iter.hasNext()) {
			if (first) {
				first = false;
			} else {
				inter();
			}
			this.write(iter.next());
		}
		post();
	}

	public void writeRecord(Record r) throws Exception {
		boolean first = true;
		pre();
		for (String compName : r.getComponentNames()) {
			if (first) {
				first = false;
			} else {
				inter();
			}
			this.write(compName);
			this.write(r.getComponent(compName));
		}
		post();
	}

	public void writePath(Path p) throws Exception {
		cantWrite(p);
	}

	public void writePathSystem(PathSystem p) throws Exception {
		cantWrite(p);
	}

	public void writeSlice(Slice s) throws Exception {
		cantWrite(s);
	}

	public void writeVertex(Vertex v) throws Exception {
		cantWrite(v);
	}

	public void writeEdge(Edge e) throws Exception {
		cantWrite(e);
	}

	public void writeInteger(Integer n) throws Exception {
		cantWrite(n);
	}

	public void writeLong(Long n) throws Exception {
		cantWrite(n);
	}

	public void writeDouble(Double n) throws Exception {
		cantWrite(n);
	}

	public void writeString(String s) throws Exception {
		cantWrite(s);
	}

	public void writeEnum(Enum<?> e) throws Exception {
		cantWrite(e);
	}

	public void writeGraph(Graph g) throws Exception {
		cantWrite(g);
	}

	public void writeSubGraphMarker(SubGraphMarker s) throws Exception {
		cantWrite(s);
	}

	public void writeDFA(DFA d) throws Exception {
		cantWrite(d);
	}

	public void writeNFA(NFA n) throws Exception {
		cantWrite(n);
	}

	public void writeBoolean(Boolean b) throws Exception {
		cantWrite(b);
	}

	public void writeAttributedElementClass(AttributedElementClass a)
			throws Exception {
		cantWrite(a);
	}

	public void writeTypeCollection(TypeCollection a) throws Exception {
		cantWrite(a);
	}

	public void writeState(State s) throws Exception {
		cantWrite(s);
	}

	public void writeTransition(Transition t) throws Exception {
		cantWrite(t);
	}

	public void writeDeclaration(Declaration d) throws Exception {
		cantWrite(d);
	}

	public void writeDefaultObject(Object o) throws Exception {
		cantWrite(o);
	}

	public void write(Object o) throws Exception {
		if (o instanceof PSet) {
			this.writePSet((PSet<?>) o);
		} else if (o instanceof Table) {
			this.writeTable((Table<?>) o);
		} else if (o instanceof PVector) {
			this.writePVector((PVector<?>) o);
		} else if (o instanceof PMap) {
			this.writePMap((PMap<?, ?>) o);
		} else if (o instanceof Tuple) {
			this.writeTuple((Tuple) o);
		} else if (o instanceof Record) {
			this.writeRecord((Record) o);
		} else if (o instanceof Path) {
			this.writePath((Path) o);
		} else if (o instanceof PathSystem) {
			this.writePathSystem((PathSystem) o);
		} else if (o instanceof Slice) {
			this.writeSlice((Slice) o);
		} else if (o instanceof Vertex) {
			this.writeVertex((Vertex) o);
		} else if (o instanceof Edge) {
			this.writeEdge((Edge) o);
		} else if (o instanceof Integer) {
			this.writeInteger((Integer) o);
		} else if (o instanceof Long) {
			this.writeLong((Long) o);
		} else if (o instanceof Double) {
			this.writeDouble((Double) o);
		} else if (o instanceof String) {
			this.writeString((String) o);
		} else if (o instanceof Enum) {
			this.writeEnum((Enum<?>) o);
		} else if (o instanceof Graph) {
			this.writeGraph((Graph) o);
		} else if (o instanceof SubGraphMarker) {
			this.writeSubGraphMarker((SubGraphMarker) o);
		} else if (o instanceof DFA) {
			this.writeDFA((DFA) o);
		} else if (o instanceof NFA) {
			this.writeNFA((NFA) o);
		} else if (o instanceof TypeCollection) {
			this.writeTypeCollection((TypeCollection) o);
		} else if (o instanceof Boolean) {
			this.writeBoolean((Boolean) o);
		} else if (o instanceof AttributedElementClass) {
			this.writeAttributedElementClass((AttributedElementClass) o);
		} else if (o instanceof Transition) {
			this.writeTransition((Transition) o);
		} else if (o instanceof Declaration) {
			this.writeDeclaration((Declaration) o);
		} else {
			this.writeDefaultObject(o);
		}
	}

	public void post() throws Exception {
	}

	public void pre() throws Exception {
	}

	public void inter() throws Exception {
	}

	public void head() throws Exception {
	}

	public void foot() throws Exception {
	}

	public void cantWrite(Object v) {
		throw new SerialisingException(getClass().getSimpleName()
				+ " can not handle " + v.getClass(), v);

	}

}
