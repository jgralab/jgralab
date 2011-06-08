package de.uni_koblenz.jgralabtest.gretl;

import de.uni_koblenz.jgralab.Graph;
import de.uni_koblenz.jgralab.gretl.AddSubClasses;
import de.uni_koblenz.jgralab.gretl.Context;
import de.uni_koblenz.jgralab.gretl.CreateAbstractEdgeClass;
import de.uni_koblenz.jgralab.gretl.CreateAbstractVertexClass;
import de.uni_koblenz.jgralab.gretl.CreateAttribute;
import de.uni_koblenz.jgralab.gretl.CreateAttribute.AttributeSpec;
import de.uni_koblenz.jgralab.gretl.CreateEdgeClass;
import de.uni_koblenz.jgralab.gretl.CreateEdgeClass.IncidenceClassSpec;
import de.uni_koblenz.jgralab.gretl.CreateEnumDomain;
import de.uni_koblenz.jgralab.gretl.CreateRecordDomain;
import de.uni_koblenz.jgralab.gretl.CreateVertexClass;
import de.uni_koblenz.jgralab.gretl.Transformation;
import de.uni_koblenz.jgralab.schema.EdgeClass;
import de.uni_koblenz.jgralab.schema.EnumDomain;
import de.uni_koblenz.jgralab.schema.RecordDomain;
import de.uni_koblenz.jgralab.schema.RecordDomain.RecordComponent;
import de.uni_koblenz.jgralab.schema.VertexClass;

public class FamilyGraph2GenealogyWithHelpers extends Transformation<Graph> {

	public FamilyGraph2GenealogyWithHelpers(Context context) {
		super(context);
	}

	@Override
	protected Graph transform() {
		setGReQLHelper("isMale",
				"using member: degree{HasFather, HasSon}(member) > 0");
		setGReQLHelper("isFemale", "using member: not isMale(member)");
		setGReQLHelper(
				"getMainFamily",
				"using member:                          "
						+ "(degree{HasFather, HasMother}(member) > 0 ?         "
						+ "   theElement(member --<>{HasFather, HasMother}) : "
						+ "   theElement(member --<>))");

		VertexClass person = new CreateAbstractVertexClass(context, "Person")
				.execute();
		VertexClass male = new CreateVertexClass(context, "Male",
				"          from m : V{Member}           "
						+ "with isMale(m)               "
						+ "reportSet m end              ").execute();

		VertexClass female = new CreateVertexClass(context, "Female",
				"          from m : V{Member}           "
						+ "with isFemale(m)             "
						+ "reportSet m end              ").execute();

		EdgeClass hasRelative = new CreateAbstractEdgeClass(context,
				"HasRelative", new IncidenceClassSpec(person),
				new IncidenceClassSpec(person)).execute();
		EdgeClass hasChild = new CreateEdgeClass(
				context,
				"HasChild",
				new IncidenceClassSpec(person, 2, 2, "parents"),
				new IncidenceClassSpec(person, "children"),
				"          from e : E{HasDaughter, HasSon}, "
						+ "     parent : startVertex(e)<>--{HasFather, HasMother} "
						+ "reportSet tup(endVertex(e), parent), parent, endVertex(e) end")
				.execute();

		EdgeClass hasSpouse = new CreateEdgeClass(context, "HasSpouse",
				new IncidenceClassSpec(male, 0, 1, "husband"),
				new IncidenceClassSpec(female, 0, 1, "wife"),
				"          from f : V{Family} "
						+ "reportSet f,                "
						+ "  theElement(f<>--{HasFather}), "
						+ "  theElement(f<>--{HasMother}) end").execute();

		new AddSubClasses(context, person, male, female).execute();
		new AddSubClasses(context, hasRelative, hasChild, hasSpouse).execute();

		new CreateAttribute(context, new AttributeSpec(person, "fullName",
				getStringDomain()), "          from m : keySet(img_Person) "
				+ "reportMap m -> m.firstName ++ ' ' ++  "
				+ "  getMainFamily(m).lastName end").execute();

		EnumDomain ageGroup = new CreateEnumDomain(context, "AgeGroup",
				"CHILD", "ADULT").execute();
		new CreateAttribute(context, new AttributeSpec(person, "ageGroup",
				ageGroup), "from m : keySet(img_Person) "
				+ "reportMap m -> m.age >= 18 ? 'ADULT' : 'CHILD' end").execute();

		RecordDomain addressRecord = new CreateRecordDomain(context, "Address",
				new RecordComponent("street", getStringDomain()),
				new RecordComponent("town", getStringDomain())).execute();
		new CreateAttribute(context, new AttributeSpec(person, "address",
				addressRecord),
				"          from m : keySet(img_Person)          "
						+ "reportMap m ->                 "
						+ "  let f := getMainFamily(m) in "
						+ "    rec(street : f.street,     "
						+ "        town : f.town)         "
						+ "end                            ").execute();

		return context.getTargetGraph();
	}
}
