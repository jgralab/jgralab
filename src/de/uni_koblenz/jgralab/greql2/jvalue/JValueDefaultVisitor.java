/*
 * JGraLab - The Java graph laboratory
 * (c) 2006-2009 Institute for Software Technology
 *               University of Koblenz-Landau, Germany
 *
 *               ist@uni-koblenz.de
 *
 * Please report bugs to http://serres.uni-koblenz.de/bugzilla
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */

package de.uni_koblenz.jgralab.greql2.jvalue;

import java.util.Iterator;

import de.uni_koblenz.jgralab.greql2.exception.JValueVisitorException;

/**
 * This class implements a default visitor. It visits all elements in the
 * structure given recursivly, but it does nothing with them. To implement a own
 * visitor, which for example prints alls vertices to a list, extend this class
 * an overwrite the method visitVertex(Vertex v)
 *
 * @author ist@uni-koblenz.de
 *
 */

public class JValueDefaultVisitor implements JValueVisitor {

	@Override
	public void visitSet(JValueSet s) {
		Iterator<JValue> iter = s.iterator();
		boolean first = true;
		pre();
		while (iter.hasNext()) {
			if (first) {
				first = false;
			} else {
				inter();
			}
			iter.next().accept(this);
		}
		post();
	}

	@Override
	public void visitBag(JValueBag b) {
		Iterator<JValue> iter = b.iterator();
		boolean first = true;
		pre();
		while (iter.hasNext()) {
			if (first) {
				first = false;
			} else {
				inter();
			}
			iter.next().accept(this);
		}
		post();
	}

	@Override
	public void visitTable(JValueTable t) {
		t.getHeader().accept(this);
		t.getData().accept(this);
	}

	@Override
	public void visitList(JValueList b) {
		Iterator<JValue> iter = b.iterator();
		boolean first = true;
		pre();
		while (iter.hasNext()) {
			if (first) {
				first = false;
			} else {
				inter();
			}
			iter.next().accept(this);
		}
		post();
	}

	@Override
	public void visitTuple(JValueTuple t) {
		Iterator<JValue> iter = t.iterator();
		boolean first = true;
		pre();
		while (iter.hasNext()) {
			if (first) {
				first = false;
			} else {
				inter();
			}
			iter.next().accept(this);
		}
		post();
	}

	@Override
	public void visitRecord(JValueRecord r) {
		Iterator<JValue> iter = r.iterator();
		boolean first = true;
		pre();
		while (iter.hasNext()) {
			if (first) {
				first = false;
			} else {
				inter();
			}
			iter.next().accept(this);
		}
		post();
	}

	@Override
	public void visitPath(JValuePath p) {
		Iterator<JValue> eiter = p.traceAsJValue().iterator();
		boolean first = true;
		pre();
		while (eiter.hasNext()) {
			if (first) {
				first = false;
			} else {
				inter();
			}
			eiter.next().accept(this);
		}
		post();
	}

	@Override
	public void visitPathSystem(JValuePathSystem p) {
		cantVisit(p);
	}

	@Override
	public void visitSlice(JValueSlice s) {
		cantVisit(s);
	}

	@Override
	public void visitVertex(JValue v) {
		cantVisit(v);
	}

	@Override
	public void visitEdge(JValue e) {
		cantVisit(e);
	}

	@Override
	public void visitInt(JValue n) {
		cantVisit(n);
	}

	@Override
	public void visitLong(JValue n) {
		cantVisit(n);
	}

	@Override
	public void visitDouble(JValue n) {
		cantVisit(n);
	}

	@Override
	public void visitChar(JValue c) {
		cantVisit(c);
	}

	@Override
	public void visitString(JValue s) {
		cantVisit(s);
	}

	@Override
	public void visitEnumValue(JValue e) {
		cantVisit(e);
	}

	@Override
	public void visitGraph(JValue g) {
		cantVisit(g);
	}

	@Override
	public void visitSubgraph(JValue s) {
		cantVisit(s);
	}

	@Override
	public void visitDFA(JValue d) {
		cantVisit(d);
	}

	@Override
	public void visitNFA(JValue n) {
		cantVisit(n);
	}

	@Override
	public void visitInvalid(JValue i) {
		cantVisit(i);
	}

	@Override
	public void visitBoolean(JValue b) {
		cantVisit(b);
	}

	@Override
	public void visitTrivalentBoolean(JValue b) {
		cantVisit(b);
	}

	@Override
	public void visitObject(JValue o) {
		cantVisit(o);
	}

	@Override
	public void visitAttributedElementClass(JValue a) {
		cantVisit(a);
	}

	@Override
	public void visitTypeCollection(JValue a) {
		cantVisit(a);
	}

	@Override
	public void visitState(JValue s) {
		cantVisit(s);
	}

	@Override
	public void visitTransition(JValue t) {
		cantVisit(t);
	}

	@Override
	public void visitDeclaration(JValue d) {
		cantVisit(d);
	}

	@Override
	public void visitDeclarationLayer(JValue d) {
		cantVisit(d);
	}

	@Override
	public void post() {
	}

	@Override
	public void pre() {
	}

	@Override
	public void inter() {
	}

	@Override
	public void head() {
	}

	@Override
	public void foot() {
	}

	@Override
	public void cantVisit(JValue v) {
		throw new JValueVisitorException(getClass().getSimpleName()
				+ " can not handle " + v.getType(), null);

	}

	@Override
	public void visitMap(JValueMap b) {
		boolean first = true;
		pre();
		for (JValue k : b.keySet()) {
			if (first) {
				first = false;
			} else {
				inter();
			}
			JValueTuple tup = new JValueTuple(b.size());
			tup.add(k);
			tup.add(b.get(k));
			tup.accept(this);
			// k.accept(this);
			// b.get(k).accept(this);
		}
		post();
	}

}
