/*
* JGraLab - The Java Graph Laboratory
*
* Copyright (C) 2006-2012 Institute for Software Technology
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

public class FamilyGraph2Genealogy extends Transformation<Graph> {

	public FamilyGraph2Genealogy(Context context) {
		super(context);
	}

	@Override
	protected Graph transform() {
		VertexClass person = new CreateAbstractVertexClass(context, "Person")
				.execute();
		VertexClass male = new CreateVertexClass(context, "Male",
				"          from m : V{Member} "
						+ "with degree{HasFather, HasSon}(m) > 0 "
						+ "reportSet m end ").execute();
		VertexClass female = new CreateVertexClass(context, "Female",
				"          from m : V{Member} "
						+ "with not(containsKey(img_Male, m)) "
						+ "reportSet m end ").execute();

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
				+ "reportMap m -> m.firstName ++ \" \" ++                   "
				+ "  (degree{HasFather, HasMother}(m) > 0 ?         "
				+ "    theElement(m--<>{HasFather, HasMother}).lastName : "
				+ "    theElement(m--<>).lastName) end").execute();

		EnumDomain ageGroup = new CreateEnumDomain(context, "AgeGroup",
				"CHILD", "ADULT").execute();
		new CreateAttribute(context, new AttributeSpec(person, "ageGroup",
				ageGroup), "from m : keySet(img_Person) "
				+ "reportMap m -> m.age >= 18 ? 'ADULT' : 'CHILD' end").execute();

		RecordDomain addressRecord = new CreateRecordDomain(context, "Address",
				new RecordComponent("street", getStringDomain()),
				new RecordComponent("town", getStringDomain())).execute();
		new CreateAttribute(
				context,
				new AttributeSpec(person, "address", addressRecord),
				"          from m : keySet(img_Person) "
						+ "reportMap m ->                   "
						+ "  degree{HasFather, HasMother}(m) > 0 ? "
						+ "    rec(street : theElement(m --<>{HasFather, HasMother}).street, "
						+ "        town : theElement(m --<>{HasFather, HasMother}).town) : "
						+ "    rec(street : theElement(m --<>).street, "
						+ "        town : theElement(m --<>).town) "
						+ "end              ").execute();

		return context.getTargetGraph();
	}
}
