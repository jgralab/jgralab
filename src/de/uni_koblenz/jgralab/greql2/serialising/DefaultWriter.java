package de.uni_koblenz.jgralab.greql2.serialising;

import java.util.Iterator;
import java.util.Map.Entry;

import org.pcollections.PMap;
import org.pcollections.PSet;
import org.pcollections.PVector;

import de.uni_koblenz.jgralab.Edge;
import de.uni_koblenz.jgralab.Graph;
import de.uni_koblenz.jgralab.Record;
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
import de.uni_koblenz.jgralab.greql2.types.Slice;
import de.uni_koblenz.jgralab.greql2.types.Table;
import de.uni_koblenz.jgralab.greql2.types.Tuple;
import de.uni_koblenz.jgralab.greql2.types.TypeCollection;
import de.uni_koblenz.jgralab.greql2.types.Undefined;
import de.uni_koblenz.jgralab.schema.AttributedElementClass;

public abstract class DefaultWriter {
	/**
	 * The graph all elements in the value to visit belong to
	 */
	private Graph graph;

	protected Object rootValue;

	public DefaultWriter(Graph g) {
		graph = g;
	}

	protected void writeValue(Object value) throws Exception {
		rootValue = value;
		head();
		write(value);
		foot();
	}

	public Graph getGraph() {
		return graph;
	}

	public void setGraph(Graph graph) {
		this.graph = graph;
	}

	/**
	 * Writes a PSet by writing all its elements
	 * 
	 * @param s
	 *            the PSet to write
	 */
	protected void writePSet(PSet<?> s) throws Exception {
		Iterator<?> iter = s.iterator();
		boolean first = true;
		pre();
		while (iter.hasNext()) {
			if (first) {
				first = false;
			} else {
				inter();
			}
			write(iter.next());
		}
		post();
	}

	/**
	 * Writes a PVector by writing all its elements
	 * 
	 * @param b
	 *            the PVector to write
	 */
	protected void writePVector(PVector<?> b) throws Exception {
		Iterator<?> iter = b.iterator();
		boolean first = true;
		pre();
		while (iter.hasNext()) {
			if (first) {
				first = false;
			} else {
				inter();
			}
			write(iter.next());
		}
		post();
	}

	/**
	 * Writes a PMap by writing all of its Tuples
	 * 
	 * @param b
	 *            the PMap to write
	 */
	protected void writePMap(PMap<?, ?> b) throws Exception {
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
			writeTuple(t);
		}
		post();
	}

	/**
	 * Writes a Table by first write the titles and then write the data
	 * 
	 * @param t
	 *            the Table to write
	 */
	protected void writeTable(Table<?> t) throws Exception {
		writePVector(t.getTitles());
		writePVector(t.toPVector());
	}

	/**
	 * Writes a Tuple by writing all of its components
	 * 
	 * @param t
	 *            the Tuple to write
	 */
	protected void writeTuple(Tuple t) throws Exception {
		Iterator<?> iter = t.iterator();
		boolean first = true;
		pre();
		while (iter.hasNext()) {
			if (first) {
				first = false;
			} else {
				inter();
			}
			write(iter.next());
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
	protected void writeRecord(Record r) throws Exception {
		boolean first = true;
		pre();
		for (String compName : r.getComponentNames()) {
			if (first) {
				first = false;
			} else {
				inter();
			}
			write(compName);
			write(r.getComponent(compName));
		}
		post();
	}

	/**
	 * DefaultWriter has no support for writing a Path
	 * 
	 * @param p
	 *            the Path to write
	 */
	protected void writePath(Path p) throws Exception {
		cantWrite(p);
	}

	/**
	 * DefaultWriter has no support for writing a PathSystem
	 * 
	 * @param p
	 *            the PathSystem to write
	 */
	protected void writePathSystem(PathSystem p) throws Exception {
		cantWrite(p);
	}

	/**
	 * DefaultWriter has no support for writing a Slice
	 * 
	 * @param s
	 *            the Slice to write
	 */
	protected void writeSlice(Slice s) throws Exception {
		cantWrite(s);
	}

	/**
	 * DefaultWriter has no support for writing a Vertex
	 * 
	 * @param v
	 *            the Vertex to write
	 */
	protected void writeVertex(Vertex v) throws Exception {
		cantWrite(v);
	}

	/**
	 * DefaultWriter has no support for writing an undefined value
	 */
	protected void writeUndefined() throws Exception {
		cantWrite(Undefined.UNDEFINED);
	}

	/**
	 * DefaultWriter has no support for writing an Edge
	 * 
	 * @param e
	 *            the Edge to write
	 */
	protected void writeEdge(Edge e) throws Exception {
		cantWrite(e);
	}

	/**
	 * DefaultWriter has no support for writing an Integer
	 * 
	 * @param n
	 *            the Integer to write
	 */
	protected void writeInteger(Integer n) throws Exception {
		cantWrite(n);
	}

	/**
	 * DefaultWriter has no support for writing a Long
	 * 
	 * @param n
	 *            the Long to write
	 */
	protected void writeLong(Long n) throws Exception {
		cantWrite(n);
	}

	/**
	 * DefaultWriter has no support for writing a Double
	 * 
	 * @param n
	 *            the Double to write
	 */
	protected void writeDouble(Double n) throws Exception {
		cantWrite(n);
	}

	/**
	 * DefaultWriter has no support for writing a String
	 * 
	 * @param s
	 *            the String to write
	 */
	protected void writeString(String s) throws Exception {
		cantWrite(s);
	}

	/**
	 * DefaultWriter has no support for writing an Enum
	 * 
	 * @param e
	 *            the Enum to write
	 */
	protected void writeEnum(Enum<?> e) throws Exception {
		cantWrite(e);
	}

	/**
	 * DefaultWriter has no support for writing a Graph
	 * 
	 * @param g
	 *            the Graph to write
	 */
	protected void writeGraph(Graph g) throws Exception {
		cantWrite(g);
	}

	/**
	 * DefaultWriter has no support for writing a SubGraphMarker
	 * 
	 * @param s
	 *            the SubGraphMarker to write
	 */
	protected void writeSubGraphMarker(SubGraphMarker s) throws Exception {
		cantWrite(s);
	}

	/**
	 * DefaultWriter has no support for writing a DFA
	 * 
	 * @param d
	 *            the DFA to write
	 */
	protected void writeDFA(DFA d) throws Exception {
		cantWrite(d);
	}

	/**
	 * DefaultWriter has no support for writing a NFA
	 * 
	 * @param n
	 *            the NFA to write
	 */
	protected void writeNFA(NFA n) throws Exception {
		cantWrite(n);
	}

	/**
	 * DefaultWriter has no support for writing a Boolean
	 * 
	 * @param b
	 *            the Boolean to write
	 */
	protected void writeBoolean(Boolean b) throws Exception {
		cantWrite(b);
	}

	/**
	 * DefaultWriter has no support for writing an AttributedElementClass
	 * 
	 * @param a
	 *            the AttributedElementClass to write
	 */
	protected void writeAttributedElementClass(AttributedElementClass a)
			throws Exception {
		cantWrite(a);
	}

	/**
	 * DefaultWriter has no support for writing a TypeCollection
	 * 
	 * @param a
	 *            the TypeCollection to write
	 */
	protected void writeTypeCollection(TypeCollection a) throws Exception {
		cantWrite(a);
	}

	/**
	 * DefaultWriter has no support for writing a State
	 * 
	 * @param s
	 *            the State to write
	 */
	protected void writeState(State s) throws Exception {
		cantWrite(s);
	}

	/**
	 * DefaultWriter has no support for writing a Transition
	 * 
	 * @param t
	 *            the Transition to write
	 */
	protected void writeTransition(Transition t) throws Exception {
		cantWrite(t);
	}

	/**
	 * DefaultWriter has no support for writing a Declaration
	 * 
	 * @param d
	 *            the Declaration to write
	 */
	protected void writeDeclaration(Declaration d) throws Exception {
		cantWrite(d);
	}

	/**
	 * DefaultWriter has no support for writing an unknown Object
	 * 
	 * @param o
	 *            the Object to write
	 */
	protected void writeDefaultObject(Object o) throws Exception {
		cantWrite(o);
	}

	/**
	 * Decides which of the possible Greql types the given object is and calls
	 * the specific method
	 * 
	 * @param o
	 *            the Object to write
	 */
	protected void write(Object o) throws Exception {
		if (o instanceof PSet) {
			writePSet((PSet<?>) o);
		} else if (o instanceof Table) {
			writeTable((Table<?>) o);
		} else if (o instanceof PVector) {
			writePVector((PVector<?>) o);
		} else if (o instanceof PMap) {
			writePMap((PMap<?, ?>) o);
		} else if (o instanceof Tuple) {
			writeTuple((Tuple) o);
		} else if (o instanceof Record) {
			writeRecord((Record) o);
		} else if (o instanceof Path) {
			writePath((Path) o);
		} else if (o instanceof PathSystem) {
			writePathSystem((PathSystem) o);
		} else if (o instanceof Slice) {
			writeSlice((Slice) o);
		} else if (o instanceof Vertex) {
			writeVertex((Vertex) o);
		} else if (o instanceof Edge) {
			writeEdge((Edge) o);
		} else if (o instanceof Integer) {
			writeInteger((Integer) o);
		} else if (o instanceof Long) {
			writeLong((Long) o);
		} else if (o instanceof Double) {
			writeDouble((Double) o);
		} else if (o instanceof String) {
			writeString((String) o);
		} else if (o instanceof Enum) {
			writeEnum((Enum<?>) o);
		} else if (o instanceof Graph) {
			writeGraph((Graph) o);
		} else if (o instanceof SubGraphMarker) {
			writeSubGraphMarker((SubGraphMarker) o);
		} else if (o instanceof DFA) {
			writeDFA((DFA) o);
		} else if (o instanceof NFA) {
			writeNFA((NFA) o);
		} else if (o instanceof TypeCollection) {
			writeTypeCollection((TypeCollection) o);
		} else if (o instanceof Boolean) {
			writeBoolean((Boolean) o);
		} else if (o instanceof AttributedElementClass) {
			writeAttributedElementClass((AttributedElementClass) o);
		} else if (o instanceof Transition) {
			writeTransition((Transition) o);
		} else if (o instanceof Declaration) {
			writeDeclaration((Declaration) o);
		} else if (o instanceof State) {
			writeState((State) o);
		} else if (o instanceof Undefined) {
			writeUndefined();
		} else {
			writeDefaultObject(o);
		}
	}

	protected void post() throws Exception {
	}

	protected void pre() throws Exception {
	}

	protected void inter() throws Exception {
	}

	protected void head() throws Exception {
	}

	protected void foot() throws Exception {
	}

	protected void cantWrite(Object v) {
		throw new SerialisingException(getClass().getSimpleName()
				+ " can not handle " + v.getClass(), v);

	}

}
