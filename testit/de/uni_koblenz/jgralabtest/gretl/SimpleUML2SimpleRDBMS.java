package de.uni_koblenz.jgralabtest.gretl;

import de.uni_koblenz.jgralab.Edge;
import de.uni_koblenz.jgralab.EdgeDirection;
import de.uni_koblenz.jgralab.Graph;
import de.uni_koblenz.jgralab.Vertex;
import de.uni_koblenz.jgralab.greql2.jvalue.JValueImpl;
import de.uni_koblenz.jgralab.greql2.jvalue.JValueList;
import de.uni_koblenz.jgralab.greql2.jvalue.JValueMap;
import de.uni_koblenz.jgralab.greql2.jvalue.JValueTuple;
import de.uni_koblenz.jgralab.gretl.Context;
import de.uni_koblenz.jgralab.gretl.CreateEdges;
import de.uni_koblenz.jgralab.gretl.CreateVertices;
import de.uni_koblenz.jgralab.gretl.SetAttributes;
import de.uni_koblenz.jgralab.gretl.Transformation;
import de.uni_koblenz.jgralabtest.gretl.schemas.qvt.simpleuml.Attribute;
import de.uni_koblenz.jgralabtest.gretl.schemas.qvt.simpleuml.Classifier;
import de.uni_koblenz.jgralabtest.gretl.schemas.qvt.simpleuml.Contains;
import de.uni_koblenz.jgralabtest.gretl.schemas.qvt.simpleuml.PackagableElement;
import de.uni_koblenz.jgralabtest.gretl.schemas.qvt.simpleuml.Package;
import de.uni_koblenz.jgralabtest.gretl.schemas.qvt.simpleuml.PrimitiveDataType;
import de.uni_koblenz.jgralabtest.gretl.schemas.qvt.simpleuml.SimpleUMLGraph;

public class SimpleUML2SimpleRDBMS extends Transformation<Graph> {
	SimpleUMLGraph g;
	long startTime;

	public SimpleUML2SimpleRDBMS(Context context) {
		super(context);
		g = (SimpleUMLGraph) context.getSourceGraph();
	}

	@After
	protected void after() {
		System.out.println(SimpleUML2SimpleRDBMS.class.getSimpleName()
				+ " took " + (System.currentTimeMillis() - startTime) + "ms.");
	}

	@Before
	protected void before() {
		startTime = System.currentTimeMillis();
	}

	@Override
	protected Graph transform() {
		// TODO: This should be rewritten from scratch using GReQL helpers...
		new CreateVertices(context, vc("Schema"), "set(\"schema\")").execute();
		new SetAttributes(context, attr("RModelElement.name"),
				"map('schema' -> 'TransformedSchema')").execute();

		// One Table per persistent class and Association
		setGReQLVariable("classTups", calculatePersistentClassTups());
		new CreateVertices(context, vc("Table"),
				"union(from t : classTups reportSet t[0] end, V{Association})")
				.execute();
		new SetAttributes(context, attr("RModelElement.name"),
				"union(from t : classTups reportMap t[0] -> t[1] end, "
						+ "from a : V{Association} reportMap a -> a.name end, "
						+ " true)").execute();
		new CreateEdges(context, ec("HasTable"), "from c : keySet(img_Table) "
				+ "reportSet c, 'schema', c end").execute();

		// create the columns
		setGReQLVariable("class2attrs", calculateClass2AttibutesMap());
		new CreateVertices(context, vc("Column"),
				"from t : flatten(values(class2attrs)) "
						+ "reportSet t['attr'] end").execute();
		new CreateEdges(context, ec("HasColumn"),
				"          from cls : keySet(class2attrs), t : class2attrs[cls] "
						+ "reportSet t['attr'], cls, t['attr'] end").execute();
		new SetAttributes(context, attr("RModelElement.name"),
				"          from t : flatten(values(class2attrs)) "
						+ "reportMap t['attr'] -> t['qname'] end").execute();

		// The primary keys
		new CreateVertices(context, vc("Key"),
				"          from c : V{Class}, a : c <>--{HasAttribute} "
						+ "with c.kind = 'persistent' and a.kind = 'primary' "
						+ "reportSet c, a end").execute();
		new CreateEdges(context, ec("HasPrimaryKey"),
				"from t : keySet(img_Key) " + "reportSet t, t[0], t end")
				.execute();
		new SetAttributes(context, attr("RModelElement.name"),
				"          from t : keySet(img_Key) "
						+ "reportMap t -> \"key_\" ++ t[0].name end").execute();
		new CreateEdges(context, ec("IsIdentifiedBy"),
				"from t : keySet(img_Key), a : t[1] " + "reportSet t, t, a end")
				.execute();

		// Association colums and foreign keys
		new CreateVertices(context, vc("Column"), "from a : V{Association}, "
				+ "  pk : a -->{HasSource, HasTarget} <>--{HasAttribute} "
				+ "with pk.kind = 'primary'          " + "reportSet a, pk end")
				.execute();

		new SetAttributes(
				context,
				attr("Column.type"),
				"          from t : flatten(values(class2attrs)) "
						+ "reportMap t['attr'] -> theElement(t['attr'] -->{HasType}).name end")
				.execute();

		new SetAttributes(
				context,
				attr("RModelElement.name"),
				"from a : V{Association}, "
						+ "  pk : a -->{HasSource, HasTarget} <>--{HasAttribute} "
						+ "with pk.kind = \"primary\"          "
						+ "reportMap tup(a, pk) -> pk.name end").execute();
		new CreateEdges(context, ec("HasColumn"), "from a : V{Association}, "
				+ "  pk : a -->{HasSource, HasTarget} <>--{HasAttribute} "
				+ "with pk.kind = \"primary\"          "
				+ "reportSet tup(a, pk), a, tup(a, pk) end").execute();

		new CreateVertices(context, vc("ForeignKey"),
				"from a : V{Association}, c : a -->{HasSource, HasTarget}, "
						+ "  pk : c <>--{HasAttribute} "
						+ "with pk.kind = \"primary\"          "
						+ "reportSet a, c, pk end").execute();
		new SetAttributes(context, attr("RModelElement.name"),
				"          from t : keySet(img_ForeignKey) "
						+ "reportMap t -> \"fkey_\" ++ t[2].name end").execute();
		new CreateEdges(context, ec("HasForeignKey"),
				"from a : V{Association}, c : a -->{HasSource, HasTarget}, "
						+ "  pk : c <>--{HasAttribute} "
						+ "with pk.kind = \"primary\"          "
						+ "reportSet tup(a, c), a, tup(a, c, pk) end")
				.execute();

		new CreateEdges(
				context,
				ec("RefersTo"),
				"from a : V{Association}, c : a -->{HasSource, HasTarget}, "
						+ "  pk : c <>--{HasAttribute} "
						+ "with pk.kind = \"primary\"          "
						+ "reportSet tup(a, c, pk), tup(a, c, pk), tup(c, pk) end")
				.execute();
		new CreateEdges(context, ec("IsIn"), "from t : keySet(img_ForeignKey) "
				+ "reportSet t, t, tup(t[0], t[2]) end").execute();

		return context.getTargetGraph();
	}

	private JValueList calculatePersistentClassTups() {
		JValueList lst = new JValueList();
		for (Vertex v : g
				.vertices(de.uni_koblenz.jgralabtest.gretl.schemas.qvt.simpleuml.Class.class)) {
			de.uni_koblenz.jgralabtest.gretl.schemas.qvt.simpleuml.Class cls = (de.uni_koblenz.jgralabtest.gretl.schemas.qvt.simpleuml.Class) v;
			if (!"persistent".equals(cls.get_kind())) {
				continue;
			}
			JValueTuple tup = new JValueTuple();
			tup.add(new JValueImpl(cls));
			tup.add(new JValueImpl(calculateQName(cls)));
			lst.add(tup);
		}
		return lst;
	}

	private String calculateQName(PackagableElement e) {
		Edge contains = e.getFirstIncidence(Contains.class, EdgeDirection.IN);
		Package parent = (Package) contains.getAlpha();
		if (parent.get_name().isEmpty()) {
			return e.get_name();
		}
		return calculateQName((PackagableElement) contains.getAlpha()) + "_"
				+ e.get_name();
	}

	/**
	 * 
	 * @return a map from Class to list of attriute tuples, where each tuple is
	 *         (Attribute, Attribute's QName).
	 */
	private JValueMap calculateClass2AttibutesMap() {
		JValueMap cls2attrs = new JValueMap();
		for (Vertex v : g
				.vertices(de.uni_koblenz.jgralabtest.gretl.schemas.qvt.simpleuml.Class.class)) {
			de.uni_koblenz.jgralabtest.gretl.schemas.qvt.simpleuml.Class cls = (de.uni_koblenz.jgralabtest.gretl.schemas.qvt.simpleuml.Class) v;
			if (!"persistent".equals(cls.get_kind())) {
				continue;
			}
			cls2attrs.put(new JValueImpl(cls),
					calculateAttributes(cls, new JValueList(), ""));
		}

		return cls2attrs;
	}

	private JValueList calculateAttributes(Vertex cls, JValueList attrList,
			String prefix) {
		for (Vertex v : cls.adjacences("attribute")) {
			Attribute attr = (Attribute) v;
			Classifier type = (Classifier) attr.adjacences("type").get(0);
			if (type instanceof PrimitiveDataType) {
				JValueMap m = new JValueMap();
				m.put(new JValueImpl("attr"), new JValueImpl(attr));
				m.put(new JValueImpl("qname"),
						new JValueImpl((prefix.isEmpty() ? "" : prefix + "_")
								+ attr.get_name()));
				attrList.add(m);
			} else {
				if (prefix.isEmpty()) {
					calculateAttributes(type, attrList, attr.get_name());
				} else {
					calculateAttributes(type, attrList,
							prefix + "_" + attr.get_name());
				}
			}
		}
		return attrList;
	}
}
