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
package de.uni_koblenz.jgralab.utilities.schemacompare;

import java.util.HashSet;
import java.util.Set;
import java.util.TreeSet;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;

import de.uni_koblenz.ist.utilities.option_handler.OptionHandler;
import de.uni_koblenz.jgralab.GraphIO;
import de.uni_koblenz.jgralab.JGraLab;
import de.uni_koblenz.jgralab.WorkInProgress;
import de.uni_koblenz.jgralab.exception.GraphIOException;
import de.uni_koblenz.jgralab.schema.Attribute;
import de.uni_koblenz.jgralab.schema.Domain;
import de.uni_koblenz.jgralab.schema.EnumDomain;
import de.uni_koblenz.jgralab.schema.GraphClass;
import de.uni_koblenz.jgralab.schema.GraphElementClass;
import de.uni_koblenz.jgralab.schema.NamedElement;
import de.uni_koblenz.jgralab.schema.RecordDomain;
import de.uni_koblenz.jgralab.schema.RecordDomain.RecordComponent;
import de.uni_koblenz.jgralab.schema.Schema;

@WorkInProgress(responsibleDevelopers = "horn")
public class SchemaCompare {
	private Schema s;
	private Schema t;
	private int diffCount = 0;
	private boolean reverseRun = false;

	Set<Object> marked = new HashSet<>();

	public SchemaCompare(Schema s1, Schema s2) {
		s = s1;
		t = s2;
	}

	/**
	 * @return the number of schema differences
	 */
	public int compareSchemas() {
		System.out.println("Comparing Schemas:\nForward run...\n");

		compareGraphClass(s.getGraphClass(), t.getGraphClass());

		for (RecordDomain r : s.getRecordDomains()) {
			compareRecordDomain(r, t.getDomain(r.getQualifiedName()));
		}

		for (EnumDomain r : s.getEnumDomains()) {
			compareEnumDomain(r, t.getDomain(r.getQualifiedName()));
		}

		System.out.println("\nReverse run...\n");
		reverseRun = true;

		compareGraphClass(t.getGraphClass(), s.getGraphClass());

		for (RecordDomain r : t.getRecordDomains()) {
			compareRecordDomain(r, s.getDomain(r.getQualifiedName()));
		}

		for (EnumDomain r : t.getEnumDomains()) {
			compareEnumDomain(r, s.getDomain(r.getQualifiedName()));
		}

		if (diffCount > 0) {
			System.out.println("\nFound " + diffCount + " differences!");
		} else {
			System.out.println("Schemas are equivalent.");
		}
		return diffCount;
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

		for (RecordComponent dc : d.getComponents()) {
			boolean foundMatch = false;
			for (RecordComponent fc : f.getComponents()) {
				if (dc.getName().equals(fc.getName())
						&& dc.getDomain().getQualifiedName()
								.equals(fc.getDomain().getQualifiedName())) {
					foundMatch = true;
					break;
				}
			}
			if (!foundMatch) {
				reportDiff("RecordDomain " + d.getQualifiedName()
						+ ", Component: " + dc.getName() + " : "
						+ dc.getDomain().getQualifiedName(), "RecordDomain "
						+ f.getQualifiedName() + " has no such component.");
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

		if (!new HashSet<>(d.getConsts()).equals(new HashSet<>(f.getConsts()))) {
			reportDiff(
					"EnumDomain " + d.getQualifiedName() + ": " + d.getConsts(),
					"EnumDomain " + f.getQualifiedName() + ": " + f.getConsts());
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
		if (!g.getQualifiedName().equals(h.getQualifiedName())) {
			reportDiff("GraphClass: " + g.getQualifiedName(), "GraphClass: "
					+ h.getQualifiedName());
			return;
		}

		if (areMarked(g, h)) {
			return;
		}

		for (GraphElementClass<?, ?> gec : g.getGraphElementClasses()) {
			compareGraphElementClass(gec,
					h.getGraphElementClass(gec.getQualifiedName()));
		}

		marked.add(g);
		marked.add(h);
	}

	private void compareGraphElementClass(GraphElementClass<?, ?> g,
			GraphElementClass<?, ?> h) {
		if (h == null) {
			reportDiff("GraphElementClass: " + g.getQualifiedName(), "null");
			return;
		}

		if (areMarked(g, h)) {
			return;
		}

		compareHierarchy(g, h);

		compareAbstractness(g, h);

		for (Attribute a : g.getOwnAttributeList()) {
			compareAttribute(g, a, h, h.getAttribute(a.getName()));
		}

		marked.add(g);
		marked.add(h);
	}

	private void compareAbstractness(GraphElementClass<?, ?> g,
			GraphElementClass<?, ?> h) {
		if (g.isAbstract() ^ h.isAbstract()) {
			reportDiff(g.getQualifiedName()
					+ (g.isAbstract() ? " is" : " is not") + " abstract",
					h.getQualifiedName() + (h.isAbstract() ? " is" : " is not")
							+ " abstract");
		}
	}

	private void compareAttribute(GraphElementClass<?, ?> g, Attribute a,
			GraphElementClass<?, ?> h, Attribute b) {
		if (b == null) {
			reportDiff(g.getQualifiedName() + "." + a.getName(),
					h.getQualifiedName() + " doesn't have such an Atrribute");
			return;
		}

		if (areMarked(g, h)) {
			return;
		}

		if (!a.getDomain().getQualifiedName()
				.equals(b.getDomain().getQualifiedName())) {
			reportDiff(g.getQualifiedName() + "." + a.getName() + " : "
					+ a.getDomain().getQualifiedName(), h.getQualifiedName()
					+ "." + b.getName() + " : "
					+ b.getDomain().getQualifiedName());
		}

		marked.add(g);
		marked.add(h);
	}

	private void compareHierarchy(GraphElementClass<?, ?> g,
			GraphElementClass<?, ?> h) {
		// superclasses
		Set<String> gsup = getQNameSet(g.getDirectSuperClasses());
		Set<String> hsup = getQNameSet(h.getDirectSuperClasses());
		if (!gsup.equals(hsup)) {
			reportDiff(g.getQualifiedName() + " superclasses: " + gsup,
					h.getQualifiedName() + " superclasses: " + hsup);
		}

		// subclassses
		Set<String> gsub = getQNameSet(g.getAllSubClasses());
		Set<String> hsub = getQNameSet(h.getAllSubClasses());
		if (!gsub.equals(hsub)) {
			reportDiff(g.getQualifiedName() + " subclasses: " + gsub,
					h.getQualifiedName() + " subclasses: " + hsub);
		}
	}

	private Set<String> getQNameSet(Set<? extends NamedElement> a) {
		Set<String> q = new TreeSet<>();
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
		CommandLine comLine = processCommandLineOptions(args);
		assert comLine != null;

		// if (args.length != 2) {
		// System.out
		// .println("Usage: java SchemaCompare schema1.tg schema2.tg");
		// return;
		// }
		//
		// SchemaCompare sc = new SchemaCompare(GraphIO
		// .loadSchemaFromFile(args[0]), GraphIO
		// .loadSchemaFromFile(args[1]));

		SchemaCompare sc = new SchemaCompare(GraphIO.loadSchemaFromFile(comLine
				.getOptionValue("s1")), GraphIO.loadSchemaFromFile(comLine
				.getOptionValue("s2")));

		sc.compareSchemas();
	}

	private static CommandLine processCommandLineOptions(String[] args) {
		String toolString = "java " + SchemaCompare.class.getName();
		String versionString = JGraLab.getInfo(false);
		OptionHandler oh = new OptionHandler(toolString, versionString);

		Option schema1 = new Option("s1", "schema1", true,
				"(required): the first schema which is compared with the second");
		schema1.setRequired(true);
		schema1.setArgName("file");
		oh.addOption(schema1);

		Option schema2 = new Option("s2", "schema2", true,
				"(required): the second schema which is compared with the first");
		schema2.setRequired(true);
		schema2.setArgName("file");
		oh.addOption(schema2);

		return oh.parse(args);
	}

}
