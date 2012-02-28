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
import de.uni_koblenz.jgralab.gretl.CreateVertexClass;
import de.uni_koblenz.jgralab.gretl.GReTLException;
import de.uni_koblenz.jgralab.gretl.Transformation;
import de.uni_koblenz.jgralab.schema.AggregationKind;
import de.uni_koblenz.jgralab.schema.EdgeClass;
import de.uni_koblenz.jgralab.schema.VertexClass;

public class ServiceTransformation extends Transformation<Graph> {

	public ServiceTransformation(Context c) {
		super(c);
	}

	@Override
	protected Graph transform() {
		VertexClass namedElement = new CreateAbstractVertexClass(context,
				"NamedElement").execute();
		VertexClass ownedElement = new CreateAbstractVertexClass(context,
				"OwnedElement").execute();

		VertexClass owner = new CreateVertexClass(context, "Owner",
				"from serviceOrDB : V reportSet serviceOrDB.owner end")
				.execute();

		VertexClass database = new CreateVertexClass(context, "Database",
				"V{Database}").execute();

		new CreateAttribute(context, new AttributeSpec(database,
				"lastAccessTime", getLongDomain()),
				"from db : keySet(img_Database) reportMap db -> db.lastAccessTime end")
				.execute();

		VertexClass service = new CreateAbstractVertexClass(context, "Service")
				.execute();

		VertexClass processService = new CreateVertexClass(context,
				"ProcessService", "V{ProcessService}").execute();

		VertexClass middleLevelService = new CreateAbstractVertexClass(context,
				"MiddleLevelService").execute();

		VertexClass composedService = new CreateVertexClass(context,
				"ComposedService", "V{ComposedService}").execute();

		VertexClass basicService = new CreateVertexClass(context,
				"BasicService", "V{BasicService}").execute();

		new CreateEdgeClass(context, "IsOwnedBy", new IncidenceClassSpec(
				ownedElement, AggregationKind.COMPOSITE),
				new IncidenceClassSpec(owner),
				"from v : V reportSet v, v, v.owner end").execute();

		new CreateEdgeClass(context, "Accesses", new IncidenceClassSpec(
				basicService, 1, 1), new IncidenceClassSpec(database, 0, 1),
				"from a : E{Accesses} reportSet a, startVertex(a), endVertex(a) end")
				.execute();

		EdgeClass calls = new CreateAbstractEdgeClass(context, "Calls",
				new IncidenceClassSpec(service),
				new IncidenceClassSpec(service)).execute();
		EdgeClass psCallsMLS = new CreateEdgeClass(context, "PsCallMLS",
				new IncidenceClassSpec(processService), new IncidenceClassSpec(
						middleLevelService, 1, Integer.MAX_VALUE),
				"          from c : E{Calls} with hasType(startVertex(c), 'ProcessService') "
						+ "reportSet c, startVertex(c), endVertex(c) end")
				.execute();

		EdgeClass csCallsMLS = new CreateEdgeClass(
				context,
				"CsCallMLS",
				new IncidenceClassSpec(composedService),
				new IncidenceClassSpec(middleLevelService, 1, Integer.MAX_VALUE),
				"          from c : E{Calls} with hasType(startVertex(c), 'ComposedService') "
						+ "reportSet c, startVertex(c), endVertex(c) end")
				.execute();

		new AddSubClasses(context, namedElement, ownedElement, owner).execute();
		new AddSubClasses(context, ownedElement, database, service).execute();
		new AddSubClasses(context, service, processService, middleLevelService)
				.execute();
		new AddSubClasses(context, middleLevelService, composedService,
				basicService).execute();
		new AddSubClasses(context, calls, psCallsMLS, csCallsMLS).execute();

		// Each element gets the name that its archetype has, and the new Owners
		// get what was the owner attribute was before.
		new CreateAttribute(context, new AttributeSpec(namedElement, "name",
				getStringDomain()),
				"    union(from v : V reportMap v -> v.name end, "
						+ "from o : keySet(img_Owner) reportMap o -> o end)")
				.execute();

		return context.getTargetGraph();
	}

	@After
	protected void checkCallsEdges() {
		boolean result = context
				.<Boolean> evaluateGReQLQuery("forall e : E{Calls} @ contains(keySet(img_Calls), e)");
		if (!result) {
			throw new GReTLException(context, "Error!");
		} else {
			System.out
					.println("All Calls edges in the source graph seem to be valid.");
		}
	}
}
