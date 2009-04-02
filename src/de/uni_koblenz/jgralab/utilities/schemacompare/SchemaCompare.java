package de.uni_koblenz.jgralab.utilities.schemacompare;

import java.util.HashSet;
import java.util.Set;
import java.util.TreeSet;
import java.util.Map.Entry;

import de.uni_koblenz.jgralab.Attribute;
import de.uni_koblenz.jgralab.GraphIO;
import de.uni_koblenz.jgralab.GraphIOException;
import de.uni_koblenz.jgralab.schema.Domain;
import de.uni_koblenz.jgralab.schema.EnumDomain;
import de.uni_koblenz.jgralab.schema.GraphClass;
import de.uni_koblenz.jgralab.schema.GraphElementClass;
import de.uni_koblenz.jgralab.schema.NamedElement;
import de.uni_koblenz.jgralab.schema.RecordDomain;
import de.uni_koblenz.jgralab.schema.Schema;

public class SchemaCompare {
	private Schema s;
	private Schema t;
	private int diffCount = 0;
	private boolean reverseRun = false;

	Set<Object> marked = new HashSet<Object>();

	public SchemaCompare(Schema s1, Schema s2) {
		s = s1;
		t = s2;
	}

	public void compareSchemas() {
		for (GraphClass gc : s.getGraphClassesInTopologicalOrder()) {
			compareGraphClass(gc, t.getGraphClass(gc.getQName()));
		}

		for (RecordDomain r : s.getRecordDomains()) {
			compareRecordDomain(r, t.getDomain(r.getQName()));
		}

		for (EnumDomain r : s.getEnumDomains()) {
			compareEnumDomain(r, t.getDomain(r.getQName()));
		}

		System.out.println("\nReverse run...\n");
		reverseRun = true;

		for (GraphClass gc : t.getGraphClassesInTopologicalOrder()) {
			compareGraphClass(gc, s.getGraphClass(gc.getQName()));
		}

		for (RecordDomain r : t.getRecordDomains()) {
			compareRecordDomain(r, s.getDomain(r.getQName()));
		}

		for (EnumDomain r : t.getEnumDomains()) {
			compareEnumDomain(r, s.getDomain(r.getQName()));
		}

		if (diffCount > 0) {
			System.out.println("\nFound " + diffCount + " differences!");
		} else {
			System.out.println("Schemas are equivalent.");
		}
	}

	private boolean areMarked(Object d, Object e) {
		return marked.contains(d) && marked.contains(e);
	}

	private void compareRecordDomain(RecordDomain d, Domain e) {
		if (e == null) {
			reportDiff("RecordDomain " + d.getQualifiedName(), "null");
			return;
		}

		if (areMarked(d, e)) {
			return;
		}

		if (!(e instanceof RecordDomain)) {
			reportDiff("RecordDomain " + d.getQualifiedName(),
					"no RecordDomain");
			return;
		}

		RecordDomain f = (RecordDomain) e;

		for (Entry<String, Domain> de : d.getComponents().entrySet()) {
			if (!f.getComponents().containsKey(de.getKey())) {
				reportDiff("RecordDomain " + d.getQualifiedName() + " has "
						+ de.getKey() + " component", "no such component");
			} else if (!f.getComponents().get(de.getKey()).getQualifiedName()
					.equals(de.getValue().getQualifiedName())) {
				reportDiff("RecordDomain component " + de.getKey()
						+ " has Domain " + de.getValue(),
						"component has Domain "
								+ f.getComponents().get(de.getKey())
										.getQualifiedName());
			}
		}

		marked.add(d);
		marked.add(f);
	}

	private void compareEnumDomain(EnumDomain d, Domain e) {
		if (e == null) {
			reportDiff("EnumDomain " + d.getQualifiedName(), "null");
			return;
		}

		if (areMarked(d, e)) {
			return;
		}

		if (!(e instanceof EnumDomain)) {
			reportDiff("EnumDomain " + d.getQualifiedName(), "no EnumDomain");
			return;
		}

		EnumDomain f = (EnumDomain) e;

		if (!new HashSet<String>(d.getConsts()).equals(new HashSet<String>(f
				.getConsts()))) {
			reportDiff("EnumDomain " + d.getQualifiedName() + ": "
					+ d.getConsts(), "EnumDomain " + f.getQualifiedName()
					+ ": " + f.getConsts());
		}

		marked.add(d);
		marked.add(f);
	}

	private void reportDiff(String s, String t) {
		System.out.println("---");
		if (!reverseRun) {
			System.out.println("Schema 1: " + s);
			System.out.println("Schema 2: " + t);
		} else {
			System.out.println("Schema 2: " + s);
			System.out.println("Schema 1: " + t);
		}
		diffCount++;
	}

	private void compareGraphClass(GraphClass g, GraphClass h) {
		if (h == null) {
			reportDiff("GraphClass: " + g.getQualifiedName(), "null");
			return;
		}

		if (areMarked(g, h)) {
			return;
		}

		for (GraphElementClass gec : g.getGraphElementClasses()) {
			compareGraphElementClass(gec, h
					.getGraphElementClass(gec.getQName()));
		}

		marked.add(g);
		marked.add(h);
	}

	private void compareGraphElementClass(GraphElementClass g,
			GraphElementClass h) {
		if (h == null) {
			reportDiff("GraphElementClass: " + g.getQualifiedName(), "null");
			return;
		}

		if (areMarked(g, h)) {
			return;
		}

		compareHierarchy(g, h);

		for (Attribute a : g.getOwnAttributeList()) {
			compareAttribute(g, a, h, h.getAttribute(a.getName()));
		}

		marked.add(g);
		marked.add(h);
	}

	private void compareAttribute(GraphElementClass g, Attribute a,
			GraphElementClass h, Attribute b) {
		if (b == null) {
			reportDiff(g.getQualifiedName() + "." + a.getName(), h
					.getQualifiedName()
					+ " doesn't have such an Atrribute");
			return;
		}

		if (areMarked(g, h)) {
			return;
		}

		if (!a.getDomain().getQualifiedName().equals(
				b.getDomain().getQualifiedName())) {
			reportDiff(g.getQualifiedName() + "." + a.getName() + " : "
					+ a.getDomain().getQualifiedName(), h.getQualifiedName()
					+ "." + b.getName() + " : "
					+ b.getDomain().getQualifiedName());
		}

		marked.add(g);
		marked.add(h);
	}

	private void compareHierarchy(GraphElementClass g, GraphElementClass h) {
		// superclasses
		Set<String> gsup = getQNameSet(g.getDirectSuperClasses());
		Set<String> hsup = getQNameSet(h.getDirectSuperClasses());
		if (!gsup.equals(hsup)) {
			reportDiff(g.getQualifiedName() + " superclasses: " + gsup, h
					.getQualifiedName()
					+ " superclasses: " + hsup);
		}

		// subclassses
		Set<String> gsub = getQNameSet(g.getAllSubClasses());
		Set<String> hsub = getQNameSet(h.getAllSubClasses());
		if (!gsub.equals(hsub)) {
			reportDiff(g.getQualifiedName() + " subclasses: " + gsub, h
					.getQualifiedName()
					+ " subclasses: " + hsub);
		}
	}

	private Set<String> getQNameSet(Set<? extends NamedElement> a) {
		Set<String> q = new TreeSet<String>();
		for (NamedElement aec : a) {
			q.add(aec.getQualifiedName());
		}
		return q;
	}

	/**
	 * @param args
	 * @throws GraphIOException
	 */
	public static void main(String[] args) throws GraphIOException {
		if (args.length != 2) {
			System.out
					.println("Usage: java SchemaCompare schema1.tg schema2.tg");
			return;
		}

		SchemaCompare sc = new SchemaCompare(GraphIO
				.loadSchemaFromFile(args[0]), GraphIO
				.loadSchemaFromFile(args[1]));

		sc.compareSchemas();
	}

}
