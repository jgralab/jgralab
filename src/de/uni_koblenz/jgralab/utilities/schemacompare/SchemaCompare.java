package de.uni_koblenz.jgralab.utilities.schemacompare;

import java.util.HashSet;
import java.util.Set;
import java.util.TreeSet;
import java.util.Map.Entry;

import org.apache.commons.cli.BasicParser;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

import de.uni_koblenz.jgralab.Attribute;
import de.uni_koblenz.jgralab.GraphIO;
import de.uni_koblenz.jgralab.GraphIOException;
import de.uni_koblenz.jgralab.WorkInProgress;
import de.uni_koblenz.jgralab.schema.Domain;
import de.uni_koblenz.jgralab.schema.EnumDomain;
import de.uni_koblenz.jgralab.schema.GraphClass;
import de.uni_koblenz.jgralab.schema.GraphElementClass;
import de.uni_koblenz.jgralab.schema.NamedElement;
import de.uni_koblenz.jgralab.schema.RecordDomain;
import de.uni_koblenz.jgralab.schema.Schema;

@WorkInProgress(responsibleDevelopers = "horn")
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
		if (!g.getQualifiedName().equals(h.getQualifiedName())) {
			reportDiff("GraphClass: " + g.getQualifiedName(), "GraphClass: "
					+ h.getQualifiedName());
			return;
		}

		if (areMarked(g, h)) {
			return;
		}

		for (GraphElementClass gec : g.getGraphElementClasses()) {
			if (gec.isInternal()) {
				continue;
			}
			compareGraphElementClass(gec, h.getGraphElementClass(gec
					.getQualifiedName()));
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
		// define the options
		Options options = new Options();

		Option oSchema1 = new Option("s1", "schema1", true,
				"(required): the first schema, which is compared with the second schema");
		oSchema1.setRequired(true);
		options.addOption(oSchema1);

		Option oSchema2 = new Option("s2", "schema2", true,
				"(required): the second schema, which is compared with the first schema");
		oSchema2.setRequired(true);
		options.addOption(oSchema2);

		Option oVersion = new Option("v", "version", false,
				"(optional): show version");
		options.addOption(oVersion);

		Option oHelp = new Option("h", "help", false, "(optional): show help");
		options.addOption(oHelp);

		Option oHelp2 = new Option("?", false, "(optional): show help");
		options.addOption(oHelp2);

		// parse arguments
		CommandLine comLine = null;
		HelpFormatter helpForm = new HelpFormatter();
		helpForm
				.setSyntaxPrefix("Usage: java SchemaCompare -s1 schema1.tg -s2 schema2.tg"
						+ "Options are:");
		try {
			comLine = new BasicParser().parse(options, args);
		} catch (ParseException e) {

			/*
			 * If there are required options, apache.cli does not accept a
			 * single -h or -v option. It's a known bug, which will be fixed in
			 * a later version.
			 */
			if (args.length > 0
					&& (args[0].equals("-h") || args[0].equals("--help") || args[0]
							.equals("-?"))) {
				helpForm.printHelp(" ", options);
			} else if (args.length > 0
					&& (args[0].equals("-v") || args[0].equals("--version"))) {
				// TODO check version number
				System.out.println("SchemaCompare version 1.0");
			} else {
				System.err.println(e.getMessage());
				helpForm.printHelp(" ", options);
				System.exit(1);
			}
			System.exit(0);
		}

		// processing of arguments and setting member variables accordingly
			
//		if (args.length != 2) {
//			System.out
//					.println("Usage: java SchemaCompare schema1.tg schema2.tg");
//			return;
//		}
//
//		SchemaCompare sc = new SchemaCompare(GraphIO
//				.loadSchemaFromFile(args[0]), GraphIO
//				.loadSchemaFromFile(args[1]));

		SchemaCompare sc = new SchemaCompare(GraphIO
				.loadSchemaFromFile(comLine.getOptionValue("s1")), GraphIO
				.loadSchemaFromFile(comLine.getOptionValue("s2")));
		
		sc.compareSchemas();
	}

}
