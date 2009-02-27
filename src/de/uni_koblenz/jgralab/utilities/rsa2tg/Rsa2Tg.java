package de.uni_koblenz.jgralab.utilities.rsa2tg;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;
import java.util.Stack;
import java.util.TreeSet;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

import de.uni_koblenz.jgralab.AttributedElement;
import de.uni_koblenz.jgralab.Edge;
import de.uni_koblenz.jgralab.GraphIO;
import de.uni_koblenz.jgralab.GraphIOException;
import de.uni_koblenz.jgralab.GraphMarker;
import de.uni_koblenz.jgralab.Vertex;
import de.uni_koblenz.jgralab.grumlschema.AttributedElementClass;
import de.uni_koblenz.jgralab.grumlschema.ContainsGraphElementClass;
import de.uni_koblenz.jgralab.grumlschema.EdgeClass;
import de.uni_koblenz.jgralab.grumlschema.EnumDomain;
import de.uni_koblenz.jgralab.grumlschema.GraphClass;
import de.uni_koblenz.jgralab.grumlschema.GrumlSchema;
import de.uni_koblenz.jgralab.grumlschema.Package;
import de.uni_koblenz.jgralab.grumlschema.Schema;
import de.uni_koblenz.jgralab.grumlschema.VertexClass;
import de.uni_koblenz.jgralab.utilities.tg2dot.Tg2Dot;

public class Rsa2Tg extends org.xml.sax.helpers.DefaultHandler {

	private Stack<String> stack;
	private Stack<StringBuilder> content;
	private de.uni_koblenz.jgralab.grumlschema.SchemaGraph sg;
	private Schema schema;
	private Stack<Package> packageStack;
	private HashMap<String, Vertex> idMap;
	private Set<String> ignoredElements;
	private int ignore;
	private String currentClassId;
	private AttributedElementClass currentClass;
	private GraphMarker<Set<String>> generalizations;
	private GraphMarker<String> attributeType;

	public Rsa2Tg() {
		ignoredElements = new TreeSet<String>();
		ignoredElements.add("profileApplication");
		ignoredElements.add("packageImport");
	}

	public static void main(String[] args) {
		new Rsa2Tg().process("/Users/riediger/Desktop/OsmSchema.xmi");
	}

	public void process(String xmiFileName) {
		SAXParser parser;
		try {
			parser = SAXParserFactory.newInstance().newSAXParser();
			parser.parse(new File(xmiFileName), this);
		} catch (ParserConfigurationException e) {
			e.printStackTrace();
		} catch (SAXException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void characters(char[] ch, int start, int length)
			throws SAXException {
		content.peek().append(ch, start, length);
	}

	@Override
	public void endDocument() throws SAXException {
		assert stack.size() == 0;
		assert ignore == 0;
		linkGeneralizations();
		removeEmptyPackages();
		String schemaName = sg.getFirstSchema().getName() + ".gruml.tg";
		createDotFile(schemaName);
		saveGraph(schemaName);
	}

	private void createDotFile(String schemaName) {
		Tg2Dot tg2Dot = new Tg2Dot();
		tg2Dot.setGraph(sg);
		tg2Dot.setOutputFile(schemaName + ".gruml.dot");
		tg2Dot.printGraph();
	}

	private void saveGraph(String schemaName) throws SAXException {
		try {
			GraphIO.saveGraphToFile(schemaName + ".gruml.tg", sg, null);
		} catch (GraphIOException e) {
			throw new SAXException(e);
		}
	}

	private void linkGeneralizations() {
		for (AttributedElement ae : generalizations.getMarkedElements()) {
			Set<String> superclasses = generalizations.getMark(ae);
			for (String id : superclasses) {
				AttributedElementClass sup = (AttributedElementClass) idMap
						.get(id);
				assert (sup != null);
				if (sup.getM1Class() == VertexClass.class) {
					sg.createSpecializesVertexClass((VertexClass) ae,
							(VertexClass) sup);
				} else {
					sg.createSpecializesEdgeClass((EdgeClass) ae,
							(EdgeClass) sup);
				}
			}
		}
		generalizations.clear();
	}

	private void removeEmptyPackages() {
		// remove all empty packages except the default package
		Package p = sg.getFirstPackage();
		while (p != null) {
			Package n = p.getNextPackage();
			if (p.getDegree() == 1 && p.getQualifiedName().length() > 0) {
				System.out.println("...remove empty package '"
						+ p.getQualifiedName() + "'");
				p.delete();
				// start over to capture empty packages after deletion
				p = sg.getFirstPackage();
			} else {
				p = n;
			}
		}
	}

	@Override
	public void endElement(String uri, String localName, String name)
			throws SAXException {
		assert stack.size() > 0;
		String s = stack.peek();
		int p = s.indexOf('>');
		assert p >= 0;
		String topName = s.substring(0, p);
		String xmiId = s.substring(p + 1);
		assert topName.equals(name);
		s = content.peek().toString().trim();
		if (s.length() > 0) {
			// System.out.println("Content '" + s + "'");
		}
		// System.out.println("End " + name);
		stack.pop();
		content.pop();
		if (ignoredElements.contains(name)) {
			assert ignore > 0;
			--ignore;
		} else if (ignore == 0) {
			Vertex v = idMap.get(xmiId);
			if (v != null) {
				if (v.getM1Class() == Package.class) {
					assert packageStack.size() > 1;
					packageStack.pop();
				}
			}
			if (name.equals("uml:Package")) {
				packageStack.pop();
				assert (packageStack.size() == 0);
			}
		}
	}

	@Override
	public void startDocument() throws SAXException {
		stack = new Stack<String>();
		content = new Stack<StringBuilder>();
		idMap = new HashMap<String, Vertex>();
		packageStack = new Stack<Package>();
		sg = GrumlSchema.instance().createSchemaGraph();
		generalizations = new GraphMarker<Set<String>>(sg);
		attributeType = new GraphMarker<String>(sg);
		ignore = 0;
	}

	@Override
	public void startElement(String uri, String localName, String name,
			Attributes atts) throws SAXException {
		String xmiId = atts.getValue("xmi:id");
		// System.out.println("Start " + name + "(" + xmiId + ")");

		stack.push(name + ">" + (xmiId != null ? xmiId : ""));
		content.push(new StringBuilder());

		if (ignoredElements.contains(name)) {
			++ignore;
		}
		if (ignore > 0) {
			return;
		}
		Vertex idVertex = null;
		if (stack.size() == 1) {
			if (name.equals("uml:Package")) {
				String nm = atts.getValue("name");

				int p = nm.lastIndexOf('.');
				schema = sg.createSchema();
				idVertex = schema;
				schema.setPackagePrefix(nm.substring(0, p));
				schema.setName(nm.substring(p + 1));

				Package defaultPackage = sg.createPackage();
				defaultPackage.setQualifiedName("");
				sg.createContainsDefaultPackage(schema, defaultPackage);
				packageStack.push(defaultPackage);
			} else {
				throw new SAXException("root element must be uml:Package");
			}
		} else {
			// inside toplevel element
			String type = atts.getValue("xmi:type");
			if (name.equals("packagedElement")) {
				if (type.equals("uml:Package")) {
					Package pkg = sg.createPackage();
					idVertex = pkg;
					pkg
							.setQualifiedName(getQualifiedName(atts
									.getValue("name")));
					sg.createContainsSubPackage(packageStack.peek(), pkg);
					packageStack.push(pkg);

				} else if (type.equals("uml:Class")) {
					VertexClass vc = sg.createVertexClass();
					idVertex = vc;
					currentClassId = xmiId;
					currentClass = vc;
					String abs = atts.getValue("isAbstract");
					vc.setIsAbstract(abs != null && abs.equals("true"));
					vc
							.setQualifiedName(getQualifiedName(atts
									.getValue("name")));
					sg.createContainsGraphElementClass(packageStack.peek(), vc);

				} else if (type.equals("uml:Association")) {

				} else if (type.equals("uml:AssociationClass")) {

				} else if (type.equals("uml:Enumeration")) {
					EnumDomain ed = sg.createEnumDomain();
					idVertex = ed;
					Package p = packageStack.peek();
					ed
							.setQualifiedName(getQualifiedName(atts
									.getValue("name")));
					sg.createContainsEnumDomain(p, ed);
					ed.setEnumConstants(new ArrayList<String>());

				} else if (type.equals("uml:PrimitiveType")) {

				} else {
					throw new SAXException("unexpected element " + name
							+ " of type " + type);
				}

			} else if (name.equals("ownedLiteral")) {
				if (type.equals("uml:EnumerationLiteral")) {
					String classifier = atts.getValue("classifier");
					assert classifier != null;
					EnumDomain ed = (EnumDomain) idMap.get(classifier);
					String s = atts.getValue("name");
					assert s != null;
					s = s.trim();
					assert s.length() > 0;
					ed.getEnumConstants().add(s);
				} else {
					throw new SAXException("unexpected element " + name
							+ " of type " + type);
				}

			} else if (name.equals("xmi:Extension")) {
				// ignore
			} else if (name.equals("eAnnotations")) {
				// ignore
			} else if (name.equals("generalization")) {
				String general = atts.getValue("general");
				Set<String> gens = generalizations.getMark(currentClass);
				if (gens == null) {
					gens = new TreeSet<String>();
					generalizations.mark(currentClass, gens);
				}
				gens.add(general);

			} else if (name.equals("details")) {
				String key = atts.getValue("key");
				if (key.equals("graphclass")) {
					// convert currentClass to graphClass
					AttributedElementClass aec = (AttributedElementClass) idMap
							.get(currentClassId);
					GraphClass gc = sg.createGraphClass();
					gc.setQualifiedName(aec.getQualifiedName());
					Edge e = aec.getFirstEdge();
					while (e != null) {
						Edge n = e.getNextEdge();
						if (e.getM1Class() == ContainsGraphElementClass.class) {
							e.delete();
						} else {
							e.setThis(gc);
						}
						e = n;
					}
					sg.createDefinesGraphClass(schema, gc);
					aec.delete();
				} else if (key.equals("record")) {

				} else {
					throw new SAXException("unexpected stereotype " + key);
				}
			} else {
				System.out.println(">>> unexpected element " + name
						+ " of type " + type);
			}
		}
		if (xmiId != null && idVertex != null) {
			idMap.put(xmiId, idVertex);
		}
	}

	private String getQualifiedName(String s) {
		assert s != null;
		s = s.trim();
		assert s.length() > 0;
		Package p = packageStack.peek();
		assert p != null;
		if (p.getQualifiedName().equals("")) {
			return s;
		} else {
			return p.getQualifiedName() + "." + s;
		}
	}
}
