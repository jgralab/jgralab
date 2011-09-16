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

	
	public void writePSet(PSet<?> s) {
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
	
	public void writeTable(Table<?> t) {
		this.writePVector(t.getTitles());
		this.writePVector(t.toPVector());
	}
	
	public void writePVector(PVector<?> b) {
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
	
	public void writePMap(PMap<?,?> b) {
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
			t= t.plus(e.getValue());
			this.writeTuple(t);
		}
		post();
	}
	
	public void writeTuple(Tuple t) {
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
	
	public void writeRecord(Record r) {
		//TODO - find a way to access the record
		//Iterator<Object> iter = r.iterator();
		boolean first = true;
		pre();
//		while (iter.hasNext()) {
//			if (first) {
//				first = false;
//			} else {
//				inter();
//			}
//			iter.next().accept(this);
//		}
		post();
	}
	
	public void writePath(Path p) {
		//TODO ask if such a method would become added
		//Iterator<Object> eiter = p.traceAsJValue().iterator();
		boolean first = true;
		pre();
//		while (eiter.hasNext()) {
//			if (first) {
//				first = false;
//			} else {
//				inter();
//			}
//			eiter.next().accept(this);
//		}
		post();
	}
	
	public void writePathSystem(PathSystem p) {
		cantWrite(p);
	}
	
	public void writeSlice(Slice s) {
		cantWrite(s);
	}
	
	public void writeVertex(Vertex v) {
		cantWrite(v);
	}
	
	public void writeEdge(Edge e) {
		cantWrite(e);
	}
	
	public void writeInteger(Integer n) {
		cantWrite(n);
	}
	
	public void writeLong(Long n) {
		cantWrite(n);
	}

	public void writeDouble(Double n) {
		cantWrite(n);
	}
	
	public void writeString(String s) {
		cantWrite(s);
	}
	
	public void writeEnum(Enum<?> e) {
		cantWrite(e);
	}
	
	public void writeGraph(Graph g) {
		cantWrite(g);
	}

	public void writeSubGraphMarker(SubGraphMarker s) {
		cantWrite(s);
	}
	
	public void writeDFA(DFA d) {
		cantWrite(d);
	}

	public void writeNFA(NFA n) {
		cantWrite(n);
	}
	
	public void writeBoolean(Boolean b) {
		cantWrite(b);
	}

	public void writeAttributedElementClass(AttributedElementClass a) {
		cantWrite(a);
	}
	
	public void writeTypeCollection(TypeCollection a) {
		cantWrite(a);
	}

	
	public void writeState(State s) {
		cantWrite(s);
	}
	
	public void writeTransition(Transition t) {
		cantWrite(t);
	}
	
	public void writeDeclaration(Declaration d) {
		cantWrite(d);
	}

	public void writeDefaultObject(Object o){
		cantWrite(o);
	}
	
	public void write(Object o) {		
		if(o instanceof PSet){
			this.writePSet((PSet<?>)o);
		}else if(o instanceof Table){
			this.writeTable((Table<?>)o);
		}else if(o instanceof PVector){
			this.writePVector((PVector<?>)o);
		}else if(o instanceof PMap){
			this.writePMap((PMap<?,?>)o);
		}else if(o instanceof Tuple){
			this.writeTuple((Tuple)o);
		}else if(o instanceof Record){
			this.writeRecord((Record)o);
		}else if(o instanceof Path){
			this.writePath((Path)o);
		}else if(o instanceof PathSystem){
			this.writePathSystem((PathSystem)o);
		}else if(o instanceof Slice){
			this.writeSlice((Slice)o);
		}else if(o instanceof Vertex){
			this.writeVertex((Vertex)o);
		}else if(o instanceof Edge){
			this.writeEdge((Edge)o);
		}else if(o instanceof Integer){
			this.writeInteger((Integer)o);
		}else if(o instanceof Long){
			this.writeLong((Long)o);
		}else if(o instanceof Double){
			this.writeDouble((Double)o);
		}else if(o instanceof String){
			this.writeString((String)o);
		}else if(o instanceof Enum){
			this.writeEnum((Enum<?>)o);
		}else if(o instanceof Graph){
			this.writeGraph((Graph)o);
		}else if(o instanceof SubGraphMarker){
			this.writeSubGraphMarker((SubGraphMarker)o);
		}else if(o instanceof DFA){
			this.writeDFA((DFA)o);
		}else if(o instanceof NFA){
			this.writeNFA((NFA)o);
		} else if(o instanceof TypeCollection){
			this.writeTypeCollection((TypeCollection)o);
		}else if(o instanceof Boolean){
			this.writeBoolean((Boolean)o);
		}else if(o instanceof AttributedElementClass){
			this.writeAttributedElementClass((AttributedElementClass)o);
		}else if(o instanceof Transition){
			this.writeTransition((Transition)o);
		}else if(o instanceof Declaration){
			this.writeDeclaration((Declaration)o);
		}else{
			this.writeDefaultObject(o);
		}
	}

	
	public void post() {
	}

	
	public void pre() {
	}

	
	public void inter() {
	}

	
	public void head() {
	}

	
	public void foot() {
	}

	
	public void cantWrite(Object v) {
		throw new SerialisingException(getClass().getSimpleName()
				+ " can not handle " + v.getClass(), v);

	}

}
