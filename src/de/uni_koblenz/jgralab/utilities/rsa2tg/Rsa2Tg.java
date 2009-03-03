package de.uni_koblenz.jgralab.utilities.rsa2tg;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.Stack;
import java.util.TreeSet;
import java.util.Map.Entry;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

import de.uni_koblenz.jgralab.AttributedElement;
import de.uni_koblenz.jgralab.Edge;
import de.uni_koblenz.jgralab.EdgeDirection;
import de.uni_koblenz.jgralab.GraphIO;
import de.uni_koblenz.jgralab.GraphIOException;
import de.uni_koblenz.jgralab.GraphMarker;
import de.uni_koblenz.jgralab.Vertex;
import de.uni_koblenz.jgralab.graphvalidator.GraphValidator;
import de.uni_koblenz.jgralab.grumlschema.AggregationClass;
import de.uni_koblenz.jgralab.grumlschema.Attribute;
import de.uni_koblenz.jgralab.grumlschema.AttributedElementClass;
import de.uni_koblenz.jgralab.grumlschema.CollectionDomain;
import de.uni_koblenz.jgralab.grumlschema.CompositionClass;
import de.uni_koblenz.jgralab.grumlschema.ContainsGraphElementClass;
import de.uni_koblenz.jgralab.grumlschema.Domain;
import de.uni_koblenz.jgralab.grumlschema.EdgeClass;
import de.uni_koblenz.jgralab.grumlschema.EnumDomain;
import de.uni_koblenz.jgralab.grumlschema.From;
import de.uni_koblenz.jgralab.grumlschema.GraphClass;
import de.uni_koblenz.jgralab.grumlschema.GrumlSchema;
import de.uni_koblenz.jgralab.grumlschema.HasAttribute;
import de.uni_koblenz.jgralab.grumlschema.HasDomain;
import de.uni_koblenz.jgralab.grumlschema.HasRecordDomainComponent;
import de.uni_koblenz.jgralab.grumlschema.MapDomain;
import de.uni_koblenz.jgralab.grumlschema.Package;
import de.uni_koblenz.jgralab.grumlschema.RecordDomain;
import de.uni_koblenz.jgralab.grumlschema.Schema;
import de.uni_koblenz.jgralab.grumlschema.StringDomain;
import de.uni_koblenz.jgralab.grumlschema.To;
import de.uni_koblenz.jgralab.grumlschema.VertexClass;
import de.uni_koblenz.jgralab.utilities.tg2dot.Tg2Dot;

public class Rsa2Tg extends org.xml.sax.helpers.DefaultHandler {

	private Stack<String> elementNameStack;
	private Stack<StringBuilder> elementContent;
	private de.uni_koblenz.jgralab.grumlschema.SchemaGraph sg;
	private Schema schema;
	private GraphClass graphClass;
	private Stack<Package> packageStack;
	private Map<String, AttributedElement> idMap;
	private Set<String> ignoredElements;
	private int ignore;
	private String currentClassId;
	private AttributedElementClass currentClass;
	private RecordDomain currentRecordDomain;
	private HasRecordDomainComponent currentRecordDomainComponent;
	private Attribute currentAttribute;
	private GraphMarker<Set<String>> generalizations;
	private GraphMarker<String> attributeType;
	private GraphMarker<String> recordComponentType;
	private Map<String, Domain> domainMap;
	private Set<Vertex> preliminaryVertices;
	private Edge currentAssociationEnd;
	private Set<Edge> aggregateEnds;

	public Rsa2Tg() {
		ignoredElements = new TreeSet<String>();
		ignoredElements.add("profileApplication");
		ignoredElements.add("packageImport");

		ignoredElements.add("ownedRule"); // TODO
	}

	public static void main(String[] args) {
		new Rsa2Tg().process("/Users/riediger/Desktop/OsmSchema.xmi");
		// new Rsa2Tg().process("/Users/riediger/Desktop/test.xmi");
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
		elementContent.peek().append(ch, start, length);
	}

	@Override
	public void endDocument() throws SAXException {
		assert elementNameStack.size() == 0;
		assert ignore == 0;
		linkGeneralizations();
		linkRecordDomainComponents();
		linkAttributeDomains();
		setAggregateFrom();
		createEdgeClassNames();
		removeUnusedDomains();
		removeEmptyPackages();
		if (!preliminaryVertices.isEmpty()) {
			System.err.println("Remaining preliminary vertices ("
					+ preliminaryVertices.size() + "):");
			for (Vertex v : preliminaryVertices) {
				System.err.println(v);
			}
		}
		String schemaName = sg.getFirstSchema().getName();
		// TODO sg.defragment();
		createDotFile(schemaName);
		saveGraph(schemaName);
		validateGraph();
	}

	private void setAggregateFrom() {
		for (Edge e : aggregateEnds) {
			((AggregationClass) e.getAlpha()).setAggregateFrom(e instanceof To);
		}
		aggregateEnds.clear();
	}

	private void createEdgeClassNames() {
		// TODO Auto-generated method stub

	}

	private void removeUnusedDomains() {
		Domain d = sg.getFirstDomain();
		while (d != null) {
			Domain n = d.getNextDomain();
			// unused if degree <=1 (one incoming egde is the ContainsDomain
			// edge from a Package)
			if (d.getDegree(EdgeDirection.IN) <= 1) {
				System.out.println("...remove unused domain '"
						+ d.getQualifiedName() + "'");
				d.delete();
				d = sg.getFirstDomain();
			} else {
				d = n;
			}
		}
	}

	private void linkRecordDomainComponents() {
		for (HasRecordDomainComponent comp : sg
				.getHasRecordDomainComponentEdges()) {
			String domainId = recordComponentType.getMark(comp);
			if (domainId == null) {
				continue;
			}
			Domain dom = (Domain) idMap.get(domainId);
			if (dom != null) {
				Domain d = (Domain) comp.getOmega();
				assert d instanceof StringDomain
						&& d.getQualifiedName().equals(domainId)
						&& preliminaryVertices.contains(d);
				comp.setOmega(dom);
				d.delete();
				preliminaryVertices.remove(d);
				recordComponentType.removeMark(comp);
			} else {
				System.err.println("Undefined Domain " + domainId);
			}
		}
		assert recordComponentType.isEmpty();
	}

	private void validateGraph() {
		GraphValidator validator = new GraphValidator(sg);
		try {
			validator.createValidationReport("validationreport.html");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private void linkAttributeDomains() {
		for (Attribute att : sg.getAttributeVertices()) {
			String domainId = attributeType.getMark(att);
			if (domainId == null) {
				assert att.getDegree(HasDomain.class, EdgeDirection.OUT) == 1;
				continue;
			}
			Domain dom = (Domain) idMap.get(domainId);
			if (dom != null) {
				sg.createHasDomain(att, dom);
				attributeType.removeMark(att);
			} else {
				System.err.println("Undefined Domain " + domainId);
			}
			assert att.getDegree(HasDomain.class, EdgeDirection.OUT) == 1;
		}
		assert attributeType.isEmpty();
	}

	private void createDotFile(String schemaName) {
		Tg2Dot tg2Dot = new Tg2Dot();
		tg2Dot.setGraph(sg);
		tg2Dot.setPrintEdgeAttributes(true);
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
				if (sup instanceof VertexClass) {
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
				// start over to capture packages that become empty after
				// deletion of p
				p = sg.getFirstPackage();
			} else {
				p = n;
			}
		}
	}

	@Override
	public void endElement(String uri, String localName, String name)
			throws SAXException {
		assert elementNameStack.size() > 0;
		String s = elementNameStack.peek();
		int p = s.indexOf('>');
		assert p >= 0;
		String topName = s.substring(0, p);
		String xmiId = s.substring(p + 1);
		assert topName.equals(name);
		s = elementContent.peek().toString().trim();
		if (s.length() > 0) {
			// System.out.println("Content '" + s + "'");
		}
		// System.out.println("End " + name);
		elementNameStack.pop();
		elementContent.pop();
		if (ignoredElements.contains(name)) {
			assert ignore > 0;
			--ignore;
		} else if (ignore == 0) {
			AttributedElement elem = idMap.get(xmiId);
			if (elem != null) {
				if (elem instanceof Package) {
					assert packageStack.size() > 1;
					packageStack.pop();
				} else if (elem instanceof AttributedElementClass) {
					currentClassId = null;
					currentClass = null;
				} else if (elem instanceof RecordDomain) {
					currentRecordDomain = null;
				} else if (elem instanceof Attribute) {
					currentAttribute = null;
				}
			}
			if (name.equals("uml:Package")) {
				packageStack.pop();
				assert (packageStack.size() == 0);
			} else if (name.equals("ownedAttribute")) {
				currentRecordDomainComponent = null;
				currentAssociationEnd = null;
			} else if (name.equals("ownedEnd")) {
				currentAssociationEnd = null;
			}
		}
	}

	@Override
	public void startDocument() throws SAXException {
		elementNameStack = new Stack<String>();
		elementContent = new Stack<StringBuilder>();
		idMap = new HashMap<String, AttributedElement>();
		packageStack = new Stack<Package>();
		sg = GrumlSchema.instance().createSchemaGraph();
		generalizations = new GraphMarker<Set<String>>(sg);
		attributeType = new GraphMarker<String>(sg);
		recordComponentType = new GraphMarker<String>(sg);
		domainMap = new HashMap<String, Domain>();
		preliminaryVertices = new HashSet<Vertex>();
		aggregateEnds = new HashSet<Edge>();
		ignore = 0;
	}

	@Override
	public void startElement(String uri, String localName, String name,
			Attributes atts) throws SAXException {
		String xmiId = atts.getValue("xmi:id");
		// System.out.println("Start " + name + "(" + xmiId + ")");

		elementNameStack.push(name + ">" + (xmiId != null ? xmiId : ""));
		elementContent.push(new StringBuilder());

		if (ignoredElements.contains(name)) {
			++ignore;
		}
		if (ignore > 0) {
			return;
		}
		Vertex idVertex = null;
		if (elementNameStack.size() == 1) {
			if (name.equals("uml:Package")) {
				String nm = atts.getValue("name");

				int p = nm.lastIndexOf('.');
				schema = sg.createSchema();
				idVertex = schema;
				schema.setPackagePrefix(nm.substring(0, p));
				schema.setName(nm.substring(p + 1));

				graphClass = sg.createGraphClass();
				sg.createDefinesGraphClass(schema, graphClass);

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
					AttributedElement ae = idMap.get(xmiId);
					VertexClass vc = null;
					if (ae != null) {
						assert ae instanceof VertexClass;
						assert preliminaryVertices.contains(ae);
						preliminaryVertices.remove(ae);
						vc = (VertexClass) ae;
					} else {
						vc = sg.createVertexClass();
					}
					idVertex = vc;
					currentClassId = xmiId;
					currentClass = vc;
					String abs = atts.getValue("isAbstract");
					vc.setIsAbstract(abs != null && abs.equals("true"));
					vc
							.setQualifiedName(getQualifiedName(atts
									.getValue("name")));
					sg.createContainsGraphElementClass(packageStack.peek(), vc);

					System.out.println("currentClass = " + currentClass + " "
							+ currentClass.getQualifiedName());

				} else if (type.equals("uml:Association")
						|| type.equals("uml:AssociationClass")) {
					// create an EdgeClass at first, probably, this has to
					// become an Aggregation or Composition later...
					AttributedElement ae = idMap.get(xmiId);
					EdgeClass ec = null;
					if (ae != null) {
						assert ae instanceof EdgeClass;
						assert preliminaryVertices.contains(ae);
						preliminaryVertices.remove(ae);
						ec = (EdgeClass) ae;
					} else {
						ec = sg.createEdgeClass();
					}
					idVertex = ec;
					currentClassId = xmiId;
					currentClass = ec;
					String abs = atts.getValue("isAbstract");
					ec.setIsAbstract(abs != null && abs.equals("true"));
					ec
							.setQualifiedName(getQualifiedName(atts
									.getValue("name")));
					sg.createContainsGraphElementClass(packageStack.peek(), ec);

					String memberEnd = atts.getValue("memberEnd");
					assert memberEnd != null;
					memberEnd = memberEnd.trim().replaceAll("\\s+", " ");
					int p = memberEnd.indexOf(' ');
					String targetEnd = memberEnd.substring(0, p);
					String sourceEnd = memberEnd.substring(p + 1);

					Edge e = (Edge) idMap.get(sourceEnd);
					if (e == null) {
						VertexClass vc = sg.createVertexClass();
						preliminaryVertices.add(vc);
						e = sg.createFrom(ec, vc);
						idMap.put(sourceEnd, e);
					}

					e = (Edge) idMap.get(targetEnd);
					if (e != null) {
						assert e.isValid();
						assert e instanceof From;
						From from = (From) e;
						To to = sg.createTo(ec, (VertexClass) from.getOmega());
						to.setMax(from.getMax());
						to.setMin(from.getMin());
						to.setRoleName(from.getRoleName());
						to.setRedefinedRoles(from.getRedefinedRoles());
						e.delete();
						if (aggregateEnds.contains(from)) {
							aggregateEnds.remove(from);
							aggregateEnds.add(to);
						}
						idMap.put(targetEnd, to);
					} else {
						VertexClass vc = sg.createVertexClass();
						preliminaryVertices.add(vc);
						e = sg.createTo(ec, vc);
						idMap.put(targetEnd, e);
					}

					System.out.println("currentClass = " + currentClass + " "
							+ currentClass.getQualifiedName());
					System.out.println("\tsource " + sourceEnd + " -> "
							+ idMap.get(sourceEnd));
					System.out.println("\ttarget " + targetEnd + " -> "
							+ idMap.get(targetEnd));

					// TODO how are abstract associations represented?

				} else if (type.equals("uml:Enumeration")) {
					EnumDomain ed = sg.createEnumDomain();
					idVertex = ed;
					Package p = packageStack.peek();
					ed
							.setQualifiedName(getQualifiedName(atts
									.getValue("name")));
					sg.createContainsDomain(p, ed);
					ed.setEnumConstants(new ArrayList<String>());
					Domain dom = domainMap.get(ed.getQualifiedName());
					if (dom != null) {
						// there was a preliminary vertex for this domain
						// link the edges to the correct one
						assert preliminaryVertices.contains(dom);
						reconnectEdges(dom, ed);
						// delete preliminary vertex
						dom.delete();
						preliminaryVertices.remove(dom);
					}
					domainMap.put(ed.getQualifiedName(), ed);

				} else if (type.equals("uml:PrimitiveType")) {
					String typeName = atts.getValue("name");
					assert typeName != null;
					typeName = typeName.replaceAll("\\s", "");
					assert typeName.length() > 0;
					Domain dom = createDomain(typeName);
					assert dom != null;
					idVertex = dom;
				} else {
					throw new SAXException("unexpected element " + name
							+ " of type " + type);
				}

			} else if (name.equals("ownedEnd")) {
				if (type.equals("uml:Property")) {
					assert currentClass != null;
					assert currentClass instanceof EdgeClass;
					handleAssociatioEnd(atts, xmiId);
				} else {
					throw new SAXException("unexpected element " + name
							+ " of type " + type);
				}
			} else if (name.equals("ownedAttribute")) {
				if (type.equals("uml:Property")) {
					assert currentClass != null || currentRecordDomain != null;
					String association = atts.getValue("association");
					if (association == null) {
						// this property is not an association end
						String attrName = atts.getValue("name");
						assert attrName != null;
						attrName = attrName.trim();
						assert attrName.length() > 0;

						if (currentClass != null) {
							// property is an "ordinary" attribute
							Attribute att = sg.createAttribute();
							currentAttribute = att;
							att.setName(attrName);
							sg.createHasAttribute(currentClass, att);

							String typeId = atts.getValue("type");
							if (typeId != null) {
								attributeType.mark(att, typeId);
							}
						} else {
							// property is a record component
							assert currentRecordDomain != null;
							currentAttribute = null;
							String typeId = atts.getValue("type");
							currentRecordDomainComponent = null;
							if (typeId != null) {
								Vertex v = (Vertex) idMap.get(typeId);
								if (v != null) {
									assert v instanceof Domain;
									currentRecordDomainComponent = sg
											.createHasRecordDomainComponent(
													currentRecordDomain,
													(Domain) v);
								} else {
									Domain dom = sg.createStringDomain();
									dom.setQualifiedName(typeId);
									preliminaryVertices.add(dom);
									currentRecordDomainComponent = sg
											.createHasRecordDomainComponent(
													currentRecordDomain, dom);
									recordComponentType.mark(
											currentRecordDomainComponent,
											typeId);
								}
							} else {
								Domain dom = sg.createStringDomain();
								preliminaryVertices.add(dom);
								currentRecordDomainComponent = sg
										.createHasRecordDomainComponent(
												currentRecordDomain, dom);
							}
							currentRecordDomainComponent.setName(attrName);
						}
					} else {
						assert currentClass != null
								&& currentRecordDomain == null;
						handleAssociatioEnd(atts, xmiId);
					}
				} else {
					throw new SAXException("unexpected element " + name
							+ " of type " + type);
				}

			} else if (name.equals("type")) {
				if (type.equals("uml:PrimitiveType")) {
					assert currentAttribute != null
							|| currentRecordDomain != null;
					String href = atts.getValue("href");
					assert href != null;
					Domain dom = null;
					if (href.endsWith("#String")) {
						dom = createDomain("String");
					} else if (href.endsWith("#Integer")) {
						dom = createDomain("Integer");
					} else if (href.endsWith("#Boolean")) {
						dom = createDomain("Boolean");
					} else {
						throw new SAXException("unexpected " + type
								+ " with href " + href);
					}
					if (currentRecordDomain != null) {
						assert currentRecordDomainComponent != null;
						if (dom != null) {
							Domain d = (Domain) currentRecordDomainComponent
									.getOmega();
							assert d instanceof StringDomain
									&& d.getQualifiedName() == null
									&& preliminaryVertices.contains(d);
							currentRecordDomainComponent.setOmega(dom);
							d.delete();
							preliminaryVertices.remove(d);
							recordComponentType
									.removeMark(currentRecordDomainComponent);
						}
					} else {
						assert currentAttribute != null;
						if (dom != null) {
							sg.createHasDomain(currentAttribute, dom);
							attributeType.removeMark(currentAttribute);
						}
					}
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
					assert currentClass != null;
					assert currentClass instanceof VertexClass;
					AttributedElementClass aec = (AttributedElementClass) idMap
							.get(currentClassId);
					graphClass.setQualifiedName(aec.getQualifiedName());
					Edge e = aec.getFirstEdge();
					while (e != null) {
						Edge n = e.getNextEdge();
						if (e instanceof ContainsGraphElementClass) {
							e.delete();
						} else {
							e.setThis(graphClass);
						}
						e = n;
					}
					aec.delete();
					currentClass = graphClass;
					System.out.println("currentClass = " + currentClass + " "
							+ currentClass.getQualifiedName());

				} else if (key.equals("record")) {
					// convert current class to RecordDomain
					assert currentClass != null;
					assert currentClass instanceof VertexClass;
					RecordDomain rd = sg.createRecordDomain();
					rd.setQualifiedName(currentClass.getQualifiedName());
					Edge e = currentClass.getFirstEdge();
					while (e != null) {
						Edge n = e.getNextEdge();
						if (e instanceof ContainsGraphElementClass) {
							sg.createContainsDomain((Package) e.getThat(), rd);
							e.delete();
						} else if (e instanceof HasAttribute) {
							Attribute att = (Attribute) e.getThat();
							Edge d = att.getFirstHasDomain();
							if (d != null) {
								Domain dom = (Domain) e.getThat();
								HasRecordDomainComponent comp = sg
										.createHasRecordDomainComponent(rd, dom);
								comp.setName(att.getName());
							} else {
								String typeId = attributeType.getMark(att);
								assert typeId != null;
								Domain dom = sg.createStringDomain();
								dom.setQualifiedName(typeId);
								preliminaryVertices.add(dom);
								HasRecordDomainComponent comp = sg
										.createHasRecordDomainComponent(rd, dom);
								recordComponentType.mark(comp, typeId);
								attributeType.removeMark(att);
							}
							att.delete();
						} else {
							System.err.println("Can't handle " + e);
						}
						e = n;
					}
					assert currentClass.getDegree() == 0;
					domainMap.put(rd.getQualifiedName(), rd);
					idMap.put(currentClassId, rd);
					currentRecordDomain = rd;
					currentClass.delete();
					currentClass = null;
					currentClassId = null;

					System.out
							.println("currentClass = null, currentRecordDomain = "
									+ rd + " " + rd.getQualifiedName());
				} else {
					throw new SAXException("unexpected stereotype " + key);
				}

			} else if (name.equals("lowerValue")) {
				assert currentAssociationEnd != null;
				String val = atts.getValue("value");
				int n = (val == null) ? 0 : val.equals("*") ? Integer.MAX_VALUE
						: Integer.parseInt(val);
				if (currentAssociationEnd instanceof From) {
					((From) currentAssociationEnd).setMin(n);
				} else {
					((To) currentAssociationEnd).setMin(n);
				}

			} else if (name.equals("upperValue")) {
				assert currentAssociationEnd != null;
				String val = atts.getValue("value");
				int n = (val == null) ? 0 : val.equals("*") ? Integer.MAX_VALUE
						: Integer.parseInt(val);
				if (currentAssociationEnd instanceof From) {
					((From) currentAssociationEnd).setMax(n);
				} else {
					((To) currentAssociationEnd).setMax(n);
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

	private void handleAssociatioEnd(Attributes atts, String xmiId) {
		// TODO handle Aggregations/Compositions
		String endName = atts.getValue("name");
		if (endName == null) {
			endName = "";
		}

		String agg = atts.getValue("aggregation");
		boolean aggregation = agg != null && agg.equals("shared");
		boolean composition = agg != null && agg.equals("composite");

		Edge e = (Edge) idMap.get(xmiId);
		if (e == null) {
			// try to find the end's VertexClass
			// if not found, create a preliminary VertexClass
			VertexClass vc = null;
			// we have an "ownedEnd", vertex class id is in "type" attribute
			String typeId = atts.getValue("type");
			assert typeId != null;
			AttributedElement ae = idMap.get(typeId);
			if (ae != null) {
				// VertexClass found
				assert ae instanceof VertexClass;
				vc = (VertexClass) ae;
			} else {
				// create a preliminary vertex class
				vc = sg.createVertexClass();
				preliminaryVertices.add(vc);
				vc.setQualifiedName(typeId);
				idMap.put(typeId, vc);
			}

			// try to find the end's EdgeClass
			EdgeClass ec = null;
			if (currentClass instanceof EdgeClass) {
				// we have an "ownedEnd", so the end's Edge is the
				// currentClass
				ec = correctAggregationAndComposition((EdgeClass) currentClass,
						aggregation, composition);
				currentClass = ec;
				idMap.put(currentClassId, currentClass);
			} else {
				// we have an ownedAttribute
				// edge class id is in "association"
				String association = atts.getValue("association");
				assert association != null;
				ae = idMap.get(association);
				if (ae != null) {
					// EdgeClass found
					assert ae instanceof EdgeClass;
					ec = correctAggregationAndComposition((EdgeClass) ae,
							aggregation, composition);
				} else {
					// create a preliminary edge class
					ec = composition ? sg.createCompositionClass()
							: aggregation ? sg.createAggregationClass() : sg
									.createEdgeClass();
				}
				preliminaryVertices.add(ec);
				idMap.put(association, ec);
			}

			assert vc != null && ec != null;
			e = sg.createFrom(ec, vc);
		} else {
			EdgeClass ec = (EdgeClass) e.getAlpha();
			String id = null;
			for (Entry<String, AttributedElement> idEntry : idMap.entrySet()) {
				if (idEntry.getValue() == ec) {
					id = idEntry.getKey();
					break;
				}
			}
			assert id != null;
			ec = correctAggregationAndComposition(ec, aggregation, composition);
			idMap.put(id, ec);

			if (currentClass instanceof EdgeClass) {
				// an ownedEnd of an association with a possibly preliminary
				// vertex class
				VertexClass vc = (VertexClass) e.getOmega();
				if (preliminaryVertices.contains(vc)) {
					String typeId = atts.getValue("type");
					assert typeId != null;
					AttributedElement ae = idMap.get(typeId);
					if (ae != null && !vc.equals(ae)) {
						assert ae instanceof VertexClass;
						e.setOmega((VertexClass) ae);
						vc.delete();
						preliminaryVertices.remove(vc);
					}
				}
			}
		}

		assert e != null;
		assert e instanceof From || e instanceof To;
		currentAssociationEnd = e;
		if (aggregation || composition) {
			aggregateEnds.add(e);
		}
		idMap.put(xmiId, e);
		if (e instanceof To) {
			To to = (To) e;
			to.setRoleName(endName);
		} else if (e instanceof From) {
			From fr = (From) e;
			fr.setRoleName(endName);
		} else {
			throw new RuntimeException("FIXME! Should never get here.");
		}
		System.out.println("\t"
				+ (currentClass instanceof EdgeClass ? "ownedEnd"
						: "ownedAttribute") + " " + endName + " " + xmiId
				+ " (" + e + " " + e.getOmega() + " "
				+ ((VertexClass) e.getOmega()).getQualifiedName() + ")");

	}

	private EdgeClass correctAggregationAndComposition(EdgeClass ec,
			boolean aggregation, boolean composition) {
		if (composition && ec.getM1Class() != CompositionClass.class) {
			EdgeClass cls = sg.createCompositionClass();
			cls.setQualifiedName(ec.getQualifiedName());
			cls.setIsAbstract(ec.isIsAbstract());
			reconnectEdges(ec, cls);
			ec.delete();
			if (preliminaryVertices.contains(ec)) {
				preliminaryVertices.remove(ec);
				preliminaryVertices.add(cls);
			}
			return cls;
		}
		if (aggregation && ec.getM1Class() != AggregationClass.class) {
			EdgeClass cls = sg.createAggregationClass();
			cls.setQualifiedName(ec.getQualifiedName());
			cls.setIsAbstract(ec.isIsAbstract());
			reconnectEdges(ec, cls);
			ec.delete();
			if (preliminaryVertices.contains(ec)) {
				preliminaryVertices.remove(ec);
				preliminaryVertices.add(cls);
			}
			return cls;
		}
		return ec;
	}

	private void reconnectEdges(Vertex oldVertex, Vertex newVertex) {
		Edge curr = oldVertex.getFirstEdge();
		while (curr != null) {
			Edge next = curr.getNextEdge();
			curr.setThis(newVertex);
			curr = next;
		}
	}

	/**
	 * Creates a Domain vertex corresponding to the specified
	 * <code>typeName</code>.
	 * 
	 * This vertex can also be a preliminary vertex which has to be replaced by
	 * the correct Domain later. In this case, there is no "ContainsDomain"
	 * edge, and the type is "StringDomain".
	 * 
	 * @param typeName
	 * @return
	 */
	private Domain createDomain(String typeName) {
		Domain dom = domainMap.get(typeName);
		if (dom != null) {
			return dom;
		}

		if (typeName.equals("String")) {
			dom = sg.createStringDomain();
		} else if (typeName.equals("Integer")) {
			dom = sg.createIntDomain();
		} else if (typeName.equals("Double")) {
			dom = sg.createDoubleDomain();
		} else if (typeName.equals("Long")) {
			dom = sg.createLongDomain();
		} else if (typeName.equals("Boolean")) {
			dom = sg.createBooleanDomain();
		} else if (typeName.startsWith("Map<") && typeName.endsWith(">")) {
			dom = sg.createMapDomain();
			String keyValueDomains = typeName.substring(4,
					typeName.length() - 1);
			char[] c = keyValueDomains.toCharArray();
			// find the delimiting ',' and take into account nested domains
			int p = 0;
			for (int i = 0; i < c.length; ++i) {
				if (c[i] == ',' && p == 0) {
					p = i;
					break;
				}
				if (c[i] == '<') {
					++p;
				} else if (c[i] == '>') {
					--p;
				}
				assert p >= 0;
			}
			assert p > 0 && p < c.length - 1;
			String keyDomainName = keyValueDomains.substring(0, p);
			Domain keyDomain = createDomain(keyDomainName);
			assert keyDomain != null;
			String valueDomainName = keyValueDomains.substring(p + 1);
			Domain valueDomain = createDomain(valueDomainName);
			assert valueDomain != null;
			sg.createHasKeyDomain((MapDomain) dom, keyDomain);
			sg.createHasValueDomain((MapDomain) dom, valueDomain);
		} else if (typeName.startsWith("List<") && typeName.endsWith(">")) {
			dom = sg.createListDomain();
			String compTypeName = typeName.substring(5, typeName.length() - 1);
			Domain compDomain = createDomain(compTypeName);
			assert compDomain != null;
			sg.createHasBaseDomain((CollectionDomain) dom, compDomain);
		} else if (typeName.startsWith("Set<") && typeName.endsWith(">")) {
			dom = sg.createSetDomain();
			String compTypeName = typeName.substring(4, typeName.length() - 1);
			Domain compDomain = createDomain(compTypeName);
			assert compDomain != null;
			sg.createHasBaseDomain((CollectionDomain) dom, compDomain);
		}
		if (dom != null) {
			sg.createContainsDomain(packageStack.get(0), dom);
		} else {
			// there must exist a named domain (Enum or Record)
			// but this was not yet created in the graph
			// create preliminary domain vertex which will
			// later be re-linked and deleted
			dom = sg.createStringDomain();
			preliminaryVertices.add(dom);
		}

		assert dom != null;
		dom.setQualifiedName(typeName);
		domainMap.put(typeName, dom);
		return dom;
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
