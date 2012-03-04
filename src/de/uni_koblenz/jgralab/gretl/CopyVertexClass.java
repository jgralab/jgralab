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
package de.uni_koblenz.jgralab.gretl;

import de.uni_koblenz.jgralab.gretl.CreateAttribute.AttributeSpec;
import de.uni_koblenz.jgralab.schema.Attribute;
import de.uni_koblenz.jgralab.schema.Domain;
import de.uni_koblenz.jgralab.schema.VertexClass;

public class CopyVertexClass extends Transformation<VertexClass> {
	private VertexClass sourceVC;
	private String alias;

	public CopyVertexClass(Context c, VertexClass sourceVC, String alias) {
		super(c);
		this.sourceVC = sourceVC;
		this.alias = alias;
	}

	public static CopyVertexClass parseAndCreate(ExecuteTransformation et) {
		String alias = Context.DEFAULT_SOURCE_GRAPH_ALIAS;
		if (et.tryMatchGraphAlias()) {
			alias = et.matchGraphAlias();
		}
		String qname = et.matchQualifiedName();
		if (et.context.getSourceGraph(alias) == null) {
			throw new GReTLException(et.context,
					"There's no source graph with alias '" + alias + "'.");
		}
		VertexClass sourceVC = et.context.getSourceGraph(alias).getSchema()
				.getGraphClass().getVertexClass(qname);
		if (sourceVC == null) {
			throw new GReTLException(et.context, "There's no VertexClass '"
					+ qname + "' in schema of graph with alias '" + alias
					+ "'.");
		}
		return new CopyVertexClass(et.context, sourceVC, alias);
	}

	@Override
	protected VertexClass transform() {
		String qname = sourceVC.getQualifiedName();
		VertexClass targetVC = new CreateVertexClass(context, qname, "#"
				+ alias + "# V{" + qname + "!}").execute();
		for (Attribute sourceAttr : sourceVC.getOwnAttributeList()) {
			Domain dom = new CopyDomain(context, sourceAttr.getDomain()).execute();
			new CreateAttribute(context, new AttributeSpec(targetVC,
					sourceAttr.getName(), dom), "from v: keySet(img_" + qname
					+ ") reportMap v -> v." + sourceAttr.getName() + " end")
					.execute();
		}
		return targetVC;
	}
}
