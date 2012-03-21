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
import de.uni_koblenz.jgralab.gretl.AddSuperClass;
import de.uni_koblenz.jgralab.gretl.Context;
import de.uni_koblenz.jgralab.gretl.CopyDomain;
import de.uni_koblenz.jgralab.gretl.CreateAbstractEdgeClass;
import de.uni_koblenz.jgralab.gretl.CreateAbstractVertexClass;
import de.uni_koblenz.jgralab.gretl.CreateAttribute;
import de.uni_koblenz.jgralab.gretl.CreateAttribute.AttributeSpec;
import de.uni_koblenz.jgralab.gretl.CreateEdgeClass;
import de.uni_koblenz.jgralab.gretl.CreateEdgeClass.IncidenceClassSpec;
import de.uni_koblenz.jgralab.gretl.CreateVertexClass;
import de.uni_koblenz.jgralab.gretl.Transformation;
import de.uni_koblenz.jgralab.schema.Attribute;
import de.uni_koblenz.jgralab.schema.Domain;
import de.uni_koblenz.jgralab.schema.EdgeClass;
import de.uni_koblenz.jgralab.schema.GraphElementClass;
import de.uni_koblenz.jgralab.schema.Schema;
import de.uni_koblenz.jgralab.schema.VertexClass;

public class SimpleCopyTransformation extends Transformation<Graph> {

	private Schema sourceSchema;

	public SimpleCopyTransformation(Context c) {
		super(c);
		sourceSchema = c.getSourceGraph().getSchema();
	}

	@Override
	protected Graph transform() {
		copyVertexClasses();
		copyEdgeClasses();
		copyAttributes();
		return context.getTargetGraph();
	}

	private void copyAttributes() {
		for (GraphElementClass<?, ?> oldGEC : sourceSchema.getGraphClass()
				.getGraphElementClasses()) {
			for (Attribute oldAttr : oldGEC.getOwnAttributeList()) {
				String qName = oldGEC.getQualifiedName();
				GraphElementClass<?, ?> newGEC = gec(qName);
				Domain domain = new CopyDomain(context, oldAttr.getDomain())
						.execute();
				new CreateAttribute(context, new AttributeSpec(newGEC,
						oldAttr.getName(), domain,
						oldAttr.getDefaultValueAsString()),
						"from ge: keySet(img_" + qName
								+ ") reportMap ge -> ge." + oldAttr.getName()
								+ " end").execute();
			}
		}
	}

	private void copyEdgeClasses() {
		for (EdgeClass oldEC : sourceSchema.getGraphClass().getEdgeClasses()) {
			EdgeClass newEC = null;
			String qName = oldEC.getQualifiedName();
			VertexClass newFrom = vc(oldEC.getFrom().getVertexClass()
					.getQualifiedName());
			VertexClass newTo = vc(oldEC.getTo().getVertexClass()
					.getQualifiedName());
			if (oldEC.isAbstract()) {
				newEC = new CreateAbstractEdgeClass(context, qName,
						new IncidenceClassSpec(newFrom, oldEC.getFrom()),
						new IncidenceClassSpec(newTo, oldEC.getTo())).execute();
			} else {
				newEC = new CreateEdgeClass(
						context,
						qName,
						new IncidenceClassSpec(newFrom, oldEC.getFrom()),
						new IncidenceClassSpec(newTo, oldEC.getTo()),
						"from e: E{"
								+ qName
								+ "!} reportSet e, startVertex(e), endVertex(e) end")
						.execute();
			}

			for (EdgeClass oldSuperEC : oldEC.getDirectSuperClasses()) {
				if (oldSuperEC.isInternal()) {
					continue;
				}
				new AddSuperClass(context, newEC,
						ec(oldSuperEC.getQualifiedName())).execute();
			}
		}
	}

	private void copyVertexClasses() {
		for (VertexClass oldVC : sourceSchema.getGraphClass()
				.getVertexClasses()) {
			VertexClass newVC = null;
			String qName = oldVC.getQualifiedName();
			if (oldVC.isAbstract()) {
				newVC = new CreateAbstractVertexClass(context, qName).execute();
			} else {
				newVC = new CreateVertexClass(context, qName, "V{" + qName
						+ "!}").execute();
			}
			for (VertexClass oldSuperVC : oldVC.getDirectSuperClasses()) {
				if (oldSuperVC.isInternal()) {
					continue;
				}
				new AddSuperClass(context, newVC,
						vc(oldSuperVC.getQualifiedName())).execute();
			}
		}
	}
}
