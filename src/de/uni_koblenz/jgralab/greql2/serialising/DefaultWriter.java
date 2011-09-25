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
import de.uni_koblenz.jgralab.greql2.types.Undefined;
import de.uni_koblenz.jgralab.schema.AttributedElementClass;

public abstract class DefaultWriter {

	/**
	 * Writes a PSet by writing all its elements
	 * 
	 * @param s
	 *            the PSet to write
	 */
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

	/**
	 * Writes a PVector by writing all its elements
	 * 
	 * @param b
	 *            the PVector to write
	 */
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

	/**
	 * Writes a PMap by writing all of its Tuples
	 * 
	 * @param b
	 *            the PMap to write
	 */
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

	/**
	 * Writes a Table by first write the titles and then write the data
	 * 
	 * @param t
	 *            the Table to write
	 */
	public void writeTable(Table<?> t) throws Exception {
		this.writePVector(t.getTitles());
		this.writePVector(t.toPVector());
	}

	/**
	 * Writes a Tuple by writing all of its components
	 * 
	 * @param t
	 *            the Tuple to write
	 */
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

	/**
	 * Writes a Record by writing the Components, first the name, second the
	 * value
	 * 
	 * @param r
	 *            the Record to write
	 */
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

	/**
	 * DefaultWriter has no support for writing a Path
	 * 
	 * @param p
	 *            the Path to write
	 */
	public void writePath(Path p) throws Exception {
		cantWrite(p);
	}

	/**
	 * DefaultWriter has no support for writing a PathSystem
	 * 
	 * @param p
	 *            the PathSystem to write
	 */
	public void writePathSystem(PathSystem p) throws Exception {
		cantWrite(p);
	}

	/**
	 * DefaultWriter has no support for writing a Slice
	 * 
	 * @param s
	 *            the Slice to write
	 */
	public void writeSlice(Slice s) throws Exception {
		cantWrite(s);
	}

	/**
	 * DefaultWriter has no support for writing a Vertex
	 * 
	 * @param v
	 *            the Vertex to write
	 */
	public void writeVertex(Vertex v) throws Exception {
		cantWrite(v);
	}

	/**
	 * DefaultWriter has no support for writing an undefined value
	 */
	public void writeUndefined() throws Exception {
		cantWrite(Undefined.UNDEFINED);
	}

	/**
	 * DefaultWriter has no support for writing an Edge
	 * 
	 * @param e
	 *            the Edge to write
	 */
	public void writeEdge(Edge e) throws Exception {
		cantWrite(e);
	}

	/**
	 * DefaultWriter has no support for writing an Integer
	 * 
	 * @param n
	 *            the Integer to write
	 */
	public void writeInteger(Integer n) throws Exception {
		cantWrite(n);
	}

	/**
	 * DefaultWriter has no support for writing a Long
	 * 
	 * @param n
	 *            the Long to write
	 */
	public void writeLong(Long n) throws Exception {
		cantWrite(n);
	}

	/**
	 * DefaultWriter has no support for writing a Double
	 * 
	 * @param n
	 *            the Double to write
	 */
	public void writeDouble(Double n) throws Exception {
		cantWrite(n);
	}

	/**
	 * DefaultWriter has no support for writing a String
	 * 
	 * @param s
	 *            the String to write
	 */
	public void writeString(String s) throws Exception {
		cantWrite(s);
	}

	/**
	 * DefaultWriter has no support for writing an Enum
	 * 
	 * @param e
	 *            the Enum to write
	 */
	public void writeEnum(Enum<?> e) throws Exception {
		cantWrite(e);
	}

	/**
	 * DefaultWriter has no support for writing a Graph
	 * 
	 * @param g
	 *            the Graph to write
	 */
	public void writeGraph(Graph g) throws Exception {
		cantWrite(g);
	}

	/**
	 * DefaultWriter has no support for writing a SubGraphMarker
	 * 
	 * @param s
	 *            the SubGraphMarker to write
	 */
	public void writeSubGraphMarker(SubGraphMarker s) throws Exception {
		cantWrite(s);
	}

	/**
	 * DefaultWriter has no support for writing a DFA
	 * 
	 * @param d
	 *            the DFA to write
	 */
	public void writeDFA(DFA d) throws Exception {
		cantWrite(d);
	}

	/**
	 * DefaultWriter has no support for writing a NFA
	 * 
	 * @param n
	 *            the NFA to write
	 */
	public void writeNFA(NFA n) throws Exception {
		cantWrite(n);
	}

	/**
	 * DefaultWriter has no support for writing a Boolean
	 * 
	 * @param b
	 *            the Boolean to write
	 */
	public void writeBoolean(Boolean b) throws Exception {
		cantWrite(b);
	}

	/**
	 * DefaultWriter has no support for writing an AttributedElementClass
	 * 
	 * @param a
	 *            the AttributedElementClass to write
	 */
	public void writeAttributedElementClass(AttributedElementClass a)
			throws Exception {
		cantWrite(a);
	}

	/**
	 * DefaultWriter has no support for writing a TypeCollection
	 * 
	 * @param a
	 *            the TypeCollection to write
	 */
	public void writeTypeCollection(TypeCollection a) throws Exception {
		cantWrite(a);
	}

	/**
	 * DefaultWriter has no support for writing a State
	 * 
	 * @param s
	 *            the State to write
	 */
	public void writeState(State s) throws Exception {
		cantWrite(s);
	}

	/**
	 * DefaultWriter has no support for writing a Transition
	 * 
	 * @param t
	 *            the Transition to write
	 */
	public void writeTransition(Transition t) throws Exception {
		cantWrite(t);
	}

	/**
	 * DefaultWriter has no support for writing a Declaration
	 * 
	 * @param d
	 *            the Declaration to write
	 */
	public void writeDeclaration(Declaration d) throws Exception {
		cantWrite(d);
	}

	/**
	 * DefaultWriter has no support for writing an unknown Object
	 * 
	 * @param o
	 *            the Object to write
	 */
	public void writeDefaultObject(Object o) throws Exception {
		cantWrite(o);
	}

	/**
	 * Decides which of the possible Greql types the given object is and calls
	 * the specific method
	 * 
	 * @param o
	 *            the Object to write
	 */
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
		} else if (o instanceof Undefined) {
			this.writeUndefined();
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
