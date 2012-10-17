package de.uni_koblenz.jgralab.utilities.argouml2tg;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;

import javax.xml.stream.XMLStreamException;

import org.pcollections.PVector;

import de.uni_koblenz.jgralab.ImplementationType;
import de.uni_koblenz.jgralab.JGraLab;
import de.uni_koblenz.jgralab.Vertex;
import de.uni_koblenz.jgralab.exception.GraphIOException;
import de.uni_koblenz.jgralab.graphvalidator.ConstraintViolation;
import de.uni_koblenz.jgralab.graphvalidator.GraphValidator;
import de.uni_koblenz.jgralab.grumlschema.GrumlSchema;
import de.uni_koblenz.jgralab.grumlschema.SchemaGraph;
import de.uni_koblenz.jgralab.grumlschema.domains.Domain;
import de.uni_koblenz.jgralab.grumlschema.domains.EnumDomain;
import de.uni_koblenz.jgralab.grumlschema.domains.HasRecordDomainComponent;
import de.uni_koblenz.jgralab.grumlschema.domains.ListDomain;
import de.uni_koblenz.jgralab.grumlschema.domains.MapDomain;
import de.uni_koblenz.jgralab.grumlschema.domains.RecordDomain;
import de.uni_koblenz.jgralab.grumlschema.domains.SetDomain;
import de.uni_koblenz.jgralab.grumlschema.structure.AggregationKind;
import de.uni_koblenz.jgralab.grumlschema.structure.Attribute;
import de.uni_koblenz.jgralab.grumlschema.structure.AttributedElementClass;
import de.uni_koblenz.jgralab.grumlschema.structure.EdgeClass;
import de.uni_koblenz.jgralab.grumlschema.structure.GraphClass;
import de.uni_koblenz.jgralab.grumlschema.structure.IncidenceClass;
import de.uni_koblenz.jgralab.grumlschema.structure.Package;
import de.uni_koblenz.jgralab.grumlschema.structure.Schema;
import de.uni_koblenz.jgralab.grumlschema.structure.VertexClass;
import de.uni_koblenz.jgralab.impl.ConsoleProgressFunction;
import de.uni_koblenz.jgralab.utilities.tg2schemagraph.SchemaGraph2Schema;
import de.uni_koblenz.jgralab.utilities.xml2tg.Xml2Tg;
import de.uni_koblenz.jgralab.utilities.xml2tg.XmlGraphUtilities;
import de.uni_koblenz.jgralab.utilities.xml2tg.schema.Element;

public class ArgoUml2Tg extends Xml2Tg {
	private static boolean VALIDATE_XML_GRAPH = false;
	private static boolean USE_UML_NAVIGABILITY_AS_EDGE_DIRECTION = true;
	private static boolean USE_FROM_ROLE = true;

	private static final String ST_GRAPHCLASS = "-64--88-111--125-2048530b:13717182953:-8000:0000000000000D6A";
	private static final String ST_RECORD = "-64--88-111--125-2048530b:13717182953:-8000:0000000000000D6B";
	private static final String ST_ABSTRACT = "-64--88-111--125-2048530b:13717182953:-8000:0000000000000D6C";

	private static final String DT_DOUBLE = "-115-26-95--20--17a78cb8:13718617229:-8000:0000000000000D76";
	private static final String DT_INTEGER = "-115-26-95--20--17a78cb8:13718617229:-8000:00000000000019DA";
	private static final String DT_UML_INTEGER = "-84-17--56-5-43645a83:11466542d86:-8000:000000000000087C";
	private static final String DT_LONG = "-115-26-95--20--17a78cb8:13718617229:-8000:0000000000000D77";
	private static final String DT_BOOLEAN = "-115-26-95--20--17a78cb8:13718617229:-8000:00000000000019DB";
	private static final String DT_STRING = "-115-26-95--20--17a78cb8:13718617229:-8000:00000000000019DC";
	private static final String DT_UML_STRING = "-84-17--56-5-43645a83:11466542d86:-8000:000000000000087E";

	private static final String TV_REDEFINES = "127-0-0-1-219dc0bd:1387ff70ce2:-8000:0000000000000E2D";

	private XmlGraphUtilities xu;
	private HashMap<String, Vertex> qnMap;
	private HashMap<String, Package> packageMap;
	private HashMap<String, Domain> domainMap;
	private HashMap<String, Domain> profileIdMap;
	private HashMap<String, Vertex> xmiIdMap;
	private SchemaGraph sg;
	private Schema schema;
	private Package defaultPackage;
	private GraphClass graphClass;

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		try {
			ArgoUml2Tg a2tg = new ArgoUml2Tg();
			a2tg.process("argo-test/testschema.xmi");
			if (VALIDATE_XML_GRAPH) {
				System.out.println("Validate XML graph...");
				GraphValidator gv = new GraphValidator(a2tg.getXmlGraph());
				gv.validate();
				gv.createValidationReport("xmlgraph.validation.html");
			}
			a2tg.getXmlGraph().save("xmlgraph.tg",
					new ConsoleProgressFunction());
			a2tg.convertToTg("argo-test/testschema.tg");
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (XMLStreamException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (GraphIOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			System.out.println("Fini.");
		}
	}

	public ArgoUml2Tg() {
		setIgnoreCharacters(true);
		addIgnoredElements("XMI.header");
		addIdAttributes("*/xmi.id");
		addIdRefAttributes("*/xmi.idref");
	}

	@Override
	public void process(String fileName) throws FileNotFoundException,
			XMLStreamException {
		System.out.println("Process " + fileName + "...");
		super.process(fileName);
	}

	private void convertToTg(String filename) {
		qnMap = new HashMap<String, Vertex>();
		packageMap = new HashMap<String, Package>();
		domainMap = new HashMap<String, Domain>();
		profileIdMap = new HashMap<String, Domain>();
		xmiIdMap = new HashMap<String, Vertex>();

		xu = new XmlGraphUtilities(getXmlGraph());
		Element model = xu.firstChildWithName(
				xu.firstChildWithName(xu.getRootElement(), "XMI.content"),
				"UML:Model");
		String schemaName = xu.getAttributeValue(model, "name");

		sg = GrumlSchema.instance().createSchemaGraph(
				ImplementationType.STANDARD,
				schemaName + "#" + xu.getAttributeValue(model, "xmi.id"), 100,
				100);

		schema = sg.createSchema();
		int p = schemaName.lastIndexOf('.');
		schema.set_name(schemaName.substring(p + 1));
		schema.set_packagePrefix(schemaName.substring(0, p));

		defaultPackage = sg.createPackage();
		defaultPackage.set_qualifiedName("");
		sg.createContainsDefaultPackage(schema, defaultPackage);
		packageMap.put("", defaultPackage);

		createPrimitiveDomains();
		createEnumDomains();
		createRecordDomains();
		createGraphClass();
		createVertexClasses();
		createEdgeClasses();
		createGeneralizations();

		try {
			sg.save("tgschemagraph.tg", new ConsoleProgressFunction());
		} catch (GraphIOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.out.println("Validate schema graph...");
		GraphValidator gv = new GraphValidator(sg);
		Set<ConstraintViolation> violations = gv.validate();
		if (violations.size() > 0) {
			try {
				System.out
						.println("Schema graph is invalid. Please look at tgschemagraph-validation.html");
				gv.createValidationReport("tgschemagraph-validation.html");
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} else {
			System.out.println("Convert schema graph to schema...");
			try {
				SchemaGraph2Schema s2s = new SchemaGraph2Schema();
				de.uni_koblenz.jgralab.schema.Schema s = s2s.convert(sg);
				s.save(filename);
			} catch (GraphIOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	private void createGeneralizations() {
		for (Element el : xu.elementsWithName("UML:Generalization")) {
			if (!xu.hasAttribute(el, "xmi.id")) {
				continue;
			}
			Element child = xu.firstChildWithName(el,
					"UML:Generalization.child");
			Element e = xu.firstChildWithName(child, "UML:Class");
			if (e == null) {
				e = xu.firstChildWithName(child, "UML:AssociationClass");
			}
			if (e == null) {
				throw new RuntimeException("Unexpected Generalization child "
						+ el);
			}
			child = e;

			Vertex cv = xmiIdMap.get(xu.getAttributeValue(child, "xmi.idref"));
			assert cv != null;

			Element parent = xu.firstChildWithName(el,
					"UML:Generalization.parent");
			e = xu.firstChildWithName(parent, "UML:Class");
			if (e == null) {
				e = xu.firstChildWithName(parent, "UML:AssociationClass");
			}
			if (e == null) {
				throw new RuntimeException("Unexpected Generalization parent "
						+ el);
			}
			parent = e;

			Vertex pv = xmiIdMap.get(xu.getAttributeValue(parent, "xmi.idref"));
			assert pv != null;

			assert cv.getAttributedElementClass() == pv
					.getAttributedElementClass();

			if (pv.isInstanceOf(VertexClass.VC)) {
				sg.createSpecializesVertexClass((VertexClass) cv,
						(VertexClass) pv);
			} else if (pv.isInstanceOf(EdgeClass.VC)) {
				EdgeClass sub = (EdgeClass) cv;
				EdgeClass sup = (EdgeClass) pv;
				sg.createSpecializesEdgeClass(sub, sup);
				IncidenceClass ic = sub.get_from();
				IncidenceClass ip = sup.get_from();
				sg.createSubsets(ic, ip);
				ic = sub.get_to();
				ip = sup.get_to();
				sg.createSubsets(ic, ip);
			} else {
				throw new RuntimeException("Unexpected generalization bewteen "
						+ pv.getSchemaClass().getName() + " vertices.");
			}

		}
	}

	private void createGraphClass() {
		for (Element el : xu.elementsWithName("UML:Class")) {
			if (hasStereotype(el, ST_GRAPHCLASS)) {
				assert !hasStereotype(el, ST_ABSTRACT);
				assert !hasStereotype(el, ST_RECORD);
				if (graphClass != null) {
					throw new RuntimeException(
							"Multiple classes marked with <<graphclass>>, only one is allowed. Offending element: "
									+ el
									+ " (name="
									+ xu.getAttributeValue(el, "name") + ")");
				}
				graphClass = sg.createGraphClass();
				sg.createDefinesGraphClass(schema, graphClass);
				graphClass.set_qualifiedName(xu.getAttributeValue(el, "name",
						true));
				qnMap.put(graphClass.get_qualifiedName(), graphClass);
				xmiIdMap.put(xu.getAttributeValue(el, "xmi.id"), graphClass);

				System.out.println("GraphClass "
						+ graphClass.get_qualifiedName());
				createAttributes(el, graphClass);
			}
		}
	}

	private void createEdgeClasses() {
		for (Element el : xu.elementsWithName("UML:Association")) {
			if (xu.hasAttribute(el, "xmi.id")) {
				createEdgeClass(el);
			}
		}
		for (Element el : xu.elementsWithName("UML:AssociationClass")) {
			if (xu.hasAttribute(el, "xmi.id")) {
				createEdgeClass(el);
			}
		}
	}

	private void createEdgeClass(Element el) {
		Package pkg = getPackage(getPackageName(el));
		String qn = getQualifiedName(el, true);
		String name = xu.getAttributeValue(el, "name", true);
		if (name.length() == 0) {
			name = null;
		}

		boolean isAbstract = hasStereotype(el, ST_ABSTRACT)
				|| (xu.hasAttribute(el, "isAbstract") && xu.getAttributeValue(
						el, "isAbstract").equals("true"));

		System.out.println((isAbstract ? "abstract " : "") + "EdgeClass "
				+ (name != null ? qn : "<<name will be generated>>"));

		EdgeClass ec = sg.createEdgeClass();
		ec.set_abstract(isAbstract);
		sg.createContainsGraphElementClass(pkg, ec);
		createAttributes(el, ec);

		Element conn = xu.firstChildWithName(el, "UML:Association.connection");
		assert conn != null;
		Iterator<Element> it = xu.childrenWithName(conn, "UML:AssociationEnd")
				.iterator();
		assert it.hasNext();
		Element from = it.next();
		IncidenceClass icFrom = createIncidenceClass(from);
		assert it.hasNext();
		Element to = it.next();
		IncidenceClass icTo = createIncidenceClass(to);

		if (USE_UML_NAVIGABILITY_AS_EDGE_DIRECTION) {
			boolean fn = xu.hasAttribute(from, "isNavigable")
					&& xu.getAttributeValue(from, "isNavigable").equals("true");

			boolean tn = xu.hasAttribute(to, "isNavigable")
					&& xu.getAttributeValue(to, "isNavigable").equals("true");

			if (fn && !tn) {
				IncidenceClass tmp = icFrom;
				icFrom = icTo;
				icTo = tmp;
			}
		}

		// flip aggregation types, since ArgoUML seems to annotate the opposite
		// end
		AggregationKind tmp = icFrom.get_aggregation();
		icFrom.set_aggregation(icTo.get_aggregation());
		icTo.set_aggregation(tmp);

		ec.add_from(icFrom);
		ec.add_to(icTo);

		// generate EdgeClass name if none is present
		if (name == null) {
			String toRole = icTo.get_roleName();
			if ((toRole == null) || toRole.equals("")) {
				toRole = ((VertexClass) icTo.getFirstEndsAtIncidence()
						.getThat()).get_qualifiedName();
				int p = toRole.lastIndexOf('.');
				if (p >= 0) {
					toRole = toRole.substring(p + 1);
				}
			} else {
				toRole = Character.toUpperCase(toRole.charAt(0))
						+ toRole.substring(1);
			}

			// There must be a 'to' role name, which is different than null and
			// not empty.
			if ((toRole == null) || (toRole.length() <= 0)) {
				throw new RuntimeException(
						"Undefined 'to' role name for EdgeClass " + ec);
			}

			if ((icFrom.get_aggregation() != AggregationKind.NONE)
					|| (icTo.get_aggregation() != AggregationKind.NONE)) {
				if (icTo.get_aggregation() != AggregationKind.NONE) {
					name = "Contains" + toRole;
				} else {
					name = "IsPartOf" + toRole;
				}
			} else {
				name = "LinksTo" + toRole;
			}

			if (USE_FROM_ROLE) {
				String fromRole = icFrom.get_roleName();
				if ((fromRole == null) || fromRole.equals("")) {
					fromRole = ((VertexClass) icFrom.getFirstEndsAtIncidence()
							.getThat()).get_qualifiedName();
					int p = fromRole.lastIndexOf('.');
					if (p >= 0) {
						fromRole = fromRole.substring(p + 1);
					}
				} else {
					fromRole = Character.toUpperCase(fromRole.charAt(0))
							+ fromRole.substring(1);
				}

				// There must be a 'from' role name, which is different than
				// null and not empty.
				if ((fromRole == null) || (fromRole.length() == 0)) {
					throw new RuntimeException(
							"Undefined 'from' role name for EdgeClass " + ec);
				}
				name = fromRole + name;
			}
			qn = pkg.get_qualifiedName() + "." + name;
		}
		ec.set_qualifiedName(qn);
		assert qnMap.get(qn) == null;
		qnMap.put(qn, ec);
		xmiIdMap.put(xu.getAttributeValue(el, "xmi.id"), ec);
	}

	private IncidenceClass createIncidenceClass(Element associationEnd) {
		IncidenceClass ic = sg.createIncidenceClass();

		// role name
		String role = xu.hasAttribute(associationEnd, "name") ? xu
				.getAttributeValue(associationEnd, "name") : "";
		ic.set_roleName(role.trim());

		// aggregation kind
		String aggregation = xu
				.getAttributeValue(associationEnd, "aggregation");
		if (aggregation.equals("none")) {
			ic.set_aggregation(AggregationKind.NONE);
		} else if (aggregation.equals("aggregate")) {
			ic.set_aggregation(AggregationKind.SHARED);
		} else if (aggregation.equals("composite")) {
			ic.set_aggregation(AggregationKind.COMPOSITE);
		} else {
			throw new RuntimeException("Unexpected aggregation value '"
					+ aggregation + "'");
		}

		// multiplicity
		int min = 0;
		int max = Integer.MAX_VALUE;
		Element mult = xu.firstChildWithName(associationEnd,
				"UML:AssociationEnd.multiplicity");
		if (mult != null) {
			mult = xu.firstChildWithName(mult, "UML:Multiplicity");
		}
		if (mult != null) {
			mult = xu.firstChildWithName(mult, "UML:Multiplicity.range");
		}
		if (mult != null) {
			mult = xu.firstChildWithName(mult, "UML:MultiplicityRange");
		}
		if (mult != null) {
			min = Integer.parseInt(xu.getAttributeValue(mult, "lower"));
			max = Integer.parseInt(xu.getAttributeValue(mult, "upper"));
			if (max < 0) {
				max = Integer.MAX_VALUE;
			}
		}
		ic.set_min(min);
		ic.set_max(max);

		// target vertex class
		Element participant = xu.firstChildWithName(associationEnd,
				"UML:AssociationEnd.participant");
		assert participant != null;
		VertexClass vc = (VertexClass) xmiIdMap.get(xu.getAttributeValue(
				xu.firstChildWithName(participant, "UML:Class"), "xmi.idref"));
		ic.add_targetclass(vc);
		return ic;
	}

	private void createVertexClasses() {
		for (Element el : xu.elementsWithName("UML:Class")) {
			if (xu.hasAttribute(el, "name") && !hasStereotype(el, ST_RECORD)
					&& !hasStereotype(el, ST_GRAPHCLASS)) {

				boolean isAbstract = hasStereotype(el, ST_ABSTRACT)
						|| (xu.hasAttribute(el, "isAbstract") && xu
								.getAttributeValue(el, "isAbstract").equals(
										"true"));

				Package pkg = getPackage(getPackageName(el));
				String qn = getQualifiedName(el, true);

				assert qnMap.get(qn) == null;

				System.out.println((isAbstract ? "abstract " : "")
						+ "VertexClass " + qn);

				VertexClass vc = sg.createVertexClass();
				vc.set_qualifiedName(qn);
				vc.set_abstract(isAbstract);
				sg.createContainsGraphElementClass(pkg, vc);
				qnMap.put(qn, vc);
				xmiIdMap.put(xu.getAttributeValue(el, "xmi.id"), vc);
				createAttributes(el, vc);
			}
		}
	}

	private void createAttributes(Element el, AttributedElementClass aec) {
		Element sf = xu.firstChildWithName(el, "UML:Classifier.feature");
		if (sf != null) {
			for (Element at : xu.childrenWithName(sf, "UML:Attribute")) {
				Domain dom = getAttributeDomain(at);
				assert dom != null;
				System.out.println("\t" + xu.getAttributeValue(at, "name")
						+ ": " + dom.get_qualifiedName());
				Attribute attr = sg.createAttribute();
				attr.set_name(xu.getAttributeValue(at, "name"));
				attr.add_domain(dom);
				aec.add_attribute(attr);
			}
		}
	}

	private void createRecordDomains() {
		for (Element el : xu.elementsWithName("UML:Class")) {
			if (hasStereotype(el, ST_RECORD)) {
				assert !hasStereotype(el, ST_GRAPHCLASS);
				assert !hasStereotype(el, ST_ABSTRACT);

				Package pkg = getPackage(getPackageName(el));
				String qn = getQualifiedName(el, true);

				// check whether a prelminiary vertex for this record domain was
				// already created, create it if not
				RecordDomain rd = (RecordDomain) domainMap.get(qn);
				if (rd == null) {
					assert qnMap.get(qn) == null;
					rd = sg.createRecordDomain();
					rd.set_qualifiedName(qn);
					sg.createContainsDomain(pkg, rd);
					qnMap.put(qn, rd);
					domainMap.put(qn, rd);
					xmiIdMap.put(xu.getAttributeValue(el, "xmi.id"), rd);
				}

				System.out.println("RecordDomain " + qn);

				Element sf = xu
						.firstChildWithName(el, "UML:Classifier.feature");
				if (sf != null) {
					for (Element at : xu.childrenWithName(sf, "UML:Attribute")) {
						System.out.println("\t"
								+ xu.getAttributeValue(at, "name"));
						Domain dom = getAttributeDomain(at);
						assert dom != null;
						HasRecordDomainComponent c = sg
								.createHasRecordDomainComponent(rd, dom);
						c.set_name(xu.getAttributeValue(at, "name"));
					}
				}
			}
		}
	}

	private Domain getAttributeDomain(Element attr) {
		Element type = xu
				.firstChildWithName(attr, "UML:StructuralFeature.type");
		assert type != null;
		Element dt = xu.firstChildWithName(type, "UML:DataType");
		if (dt != null) {
			// UML primitive type
			// - either defined in this xmi (xmi.idref), then it has to be a
			// grUML composite domain
			// - or defined in profile (href)
			if (xu.hasAttribute(dt, "xmi.idref")) {
				Element dom = xu.getReferencedElement(dt, "xmi.idref");
				return createCompositeDomain(xu.getAttributeValue(dom, "name"));
			} else if (xu.hasAttribute(dt, "href")) {
				return getPrimitiveDomainByProfileId(xu.getAttributeValue(dt,
						"href"));
			} else {
				throw new RuntimeException("FIXME: Unhandled UML:DataType");
			}
		}
		dt = xu.firstChildWithName(type, "UML:Enumeration");
		if (dt != null) {
			// domain is an enumeration
			return (Domain) xmiIdMap.get(xu.getAttributeValue(dt, "xmi.idref"));
		}
		dt = xu.firstChildWithName(type, "UML:Class");
		if (dt != null) {
			// domain is a record domain
			Vertex v = xmiIdMap.get(xu.getAttributeValue(dt, "xmi.idref"));
			if (v != null) {
				// record domain already exists
				assert v.isInstanceOf(RecordDomain.VC);
				return (Domain) v;
			}
			// otherwise create a preliminary vertex without the components
			Element el = xu.getReferencedElement(dt, "xmi.idref");
			assert el.get_name().equals("UML:Class")
					&& hasStereotype(el, ST_RECORD)
					&& !hasStereotype(el, ST_ABSTRACT)
					&& !hasStereotype(el, ST_GRAPHCLASS);
			Package pkg = getPackage(getPackageName(el));
			String qn = getQualifiedName(el, true);

			RecordDomain rd = sg.createRecordDomain();
			rd.set_qualifiedName(qn);
			domainMap.put(qn, rd);
			qnMap.put(qn, rd);
			xmiIdMap.put(xu.getAttributeValue(el, "xmi.id"), rd);
			sg.createContainsDomain(pkg, rd);
			return rd;
		}
		throw new RuntimeException("FIXME: Unhandled UML:DataType");
	}

	private Domain createCompositeDomain(String name) {
		name = name.trim().replaceAll("\\s+", "");
		Domain dom = domainMap.get(name);
		if (dom != null) {
			return dom;
		}
		System.out.println("CompositeDomain " + name);
		if (name.startsWith("Map<")) {
			MapDomain md = sg.createMapDomain();
			dom = md;
			int p = 4;
			int b = 0;
			while (p < name.length()) {
				char c = name.charAt(p);
				if (b == 0 && c == ',') {
					break;
				}
				if (c == '<') {
					++b;
				} else if (c == '>') {
					--b;
				}
				++p;
			}
			String keyName = name.substring(4, p);
			String valName = name.substring(p + 1, name.length() - 1);
			md.add_keydomain(createCompositeDomain(keyName));
			md.add_valuedomain(createCompositeDomain(valName));
		} else if (name.startsWith("List<")) {
			ListDomain ld = sg.createListDomain();
			dom = ld;
			String compName = name.substring(5, name.length() - 1);
			ld.add_basedomain(createCompositeDomain(compName));
		} else if (name.startsWith("Set<")) {
			SetDomain sd = sg.createSetDomain();
			dom = sd;
			String compName = name.substring(4, name.length() - 1);
			sd.add_basedomain(createCompositeDomain(compName));
		} else {
			throw new RuntimeException("Unknown domain name '" + name + "'");
		}
		dom.set_qualifiedName(name.replaceAll(",", ", "));
		domainMap.put(name, dom);
		sg.createContainsDomain(defaultPackage, dom);
		return dom;
	}

	private Domain getPrimitiveDomainByProfileId(String href) {
		int p = href.indexOf('#');
		if (p >= 0) {
			href = href.substring(p + 1);
		}
		return profileIdMap.get(href);
	}

	private boolean hasStereotype(Element el, String st_id) {
		Element st = xu.firstChildWithName(el, "UML:ModelElement.stereotype");
		if (st == null) {
			return false;
		}
		st = xu.firstChildWithName(st, "UML:Stereotype");
		return xu.getAttributeValue(st, "href").endsWith("#" + st_id);
	}

	private void createPrimitiveDomains() {
		createPrimitiveDomain(sg.createBooleanDomain(), "Boolean", DT_BOOLEAN);
		createPrimitiveDomain(sg.createIntegerDomain(), "Integer", DT_INTEGER,
				DT_UML_INTEGER);
		createPrimitiveDomain(sg.createLongDomain(), "Long", DT_LONG);
		createPrimitiveDomain(sg.createDoubleDomain(), "Double", DT_DOUBLE);
		createPrimitiveDomain(sg.createStringDomain(), "String", DT_STRING,
				DT_UML_STRING);
	}

	private void createPrimitiveDomain(Domain d, String qn,
			String... profileIds) {
		System.out.println("PrinitiveDomain " + qn);
		d.set_qualifiedName(qn);
		sg.createContainsDomain(packageMap.get(""), d);
		for (String id : profileIds) {
			profileIdMap.put(id, d);
		}
		domainMap.put(qn, d);
		qnMap.put(qn, d);
	}

	private void createEnumDomains() {
		for (Element el : xu.elementsWithName("UML:Enumeration")) {
			if (!xu.hasAttribute(el, "xmi.id")) {
				continue;
			}
			String qn = getQualifiedName(el, true);
			assert qnMap.get(qn) == null;

			System.out.println("EnumDomain " + qn);
			EnumDomain ed = sg.createEnumDomain();
			ed.set_qualifiedName(qn);
			domainMap.put(qn, ed);
			qnMap.put(qn, ed);
			xmiIdMap.put(xu.getAttributeValue(el, "xmi.id"), ed);

			sg.createContainsDomain(getPackage(getPackageName(el)), ed);

			Element literals = xu.firstChildWithName(el,
					"UML:Enumeration.literal");
			PVector<String> constants = JGraLab.vector();
			for (Element enumLiteral : xu.childrenWithName(literals,
					"UML:EnumerationLiteral")) {
				String cn = xu.getAttributeValue(enumLiteral, "name");
				System.out.println("\t" + cn);
				constants = constants.plus(cn);
			}
			ed.set_enumConstants(constants);
		}
	}

	private String getQualifiedName(Element el, boolean upperCaseFirstLetter) {
		String pkgName = getPackageName(el);
		Package pkg = getPackage(pkgName);
		String name = xu.getAttributeValue(el, "name", upperCaseFirstLetter);
		return (pkg == defaultPackage ? name : pkg.get_qualifiedName() + "."
				+ name);
	}

	private Package getPackage(String qn) {
		Package pkg = packageMap.get(qn);
		if (pkg == null) {
			if (qn.length() == 0) {
				pkg = packageMap.get("");
			} else {
				pkg = sg.createPackage();
				pkg.set_qualifiedName(qn);

				int p = qn.lastIndexOf('.');
				Package parentPackage = getPackage(p < 0 ? "" : qn.substring(0,
						p));
				parentPackage.add_subpackage(pkg);
				packageMap.put(qn, pkg);
				qnMap.put(qn, pkg);
			}
		}
		return pkg;
	}

	private String getPackageName(Element el) {
		String result = "";
		el = el.get_parent();
		while (el != null) {
			if (el.get_name().equals("UML:Package")) {
				if (result.length() == 0) {
					result = xu.getAttributeValue(el, "name");
				} else {
					result = xu.getAttributeValue(el, "name") + "." + result;
				}
			}
			el = el.get_parent();
		}
		return result;
	}
}
