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
import de.uni_koblenz.jgralab.gretl.CreateEdgeClass.IncidenceClassSpec;
import de.uni_koblenz.jgralab.schema.Attribute;
import de.uni_koblenz.jgralab.schema.EdgeClass;

/**
 * Copies the given source schema {@link EdgeClass} to an equivalent target
 * schema edge class including all direct (not inherited) attributes.
 * 
 * @author Tassilo Horn &lt;horn@uni-koblenz.de&gt;
 */
public class CopyEdgeClass extends Transformation<EdgeClass> {

	private EdgeClass originalSourceEC;
	private String sourceGraphAlias;
	private String sourceTargetArchExp;

	public CopyEdgeClass(Context c, EdgeClass sourceEC, String alias,
			String semExp) {
		super(c);
		this.originalSourceEC = sourceEC;
		this.sourceGraphAlias = alias;
		this.sourceTargetArchExp = semExp;
	}

	public static CopyEdgeClass parseAndCreate(ExecuteTransformation et) {
		String alias = Context.DEFAULT_SOURCE_GRAPH_ALIAS;
		if (et.tryMatchGraphAlias()) {
			alias = et.matchGraphAlias();
		}
		String qname = et.matchQualifiedName();
		if (et.context.getSourceGraph(alias) == null) {
			throw new GReTLException(et.context,
					"There's no source graph with alias '" + alias + "'.");
		}
		EdgeClass sourceEC = et.context.getSourceGraph(alias).getSchema()
				.getGraphClass().getEdgeClass(qname);
		if (sourceEC == null) {
			throw new GReTLException(et.context, "There's no EdgeClass '"
					+ qname + "' in schema of graph with alias '" + alias
					+ "'.");
		}
		et.matchTransformationArrow();
		String semExp = et.matchSemanticExpression();
		return new CopyEdgeClass(et.context, sourceEC, alias, semExp);
	}

	@Override
	protected EdgeClass transform() {
		String qname = originalSourceEC.getQualifiedName();
		IncidenceClassSpec from = new CreateEdgeClass.IncidenceClassSpec(
				vc(originalSourceEC.getFrom().getVertexClass()
						.getQualifiedName()), originalSourceEC.getFrom());
		IncidenceClassSpec to = new CreateEdgeClass.IncidenceClassSpec(
				vc(originalSourceEC.getTo().getVertexClass().getQualifiedName()),
				originalSourceEC.getTo());
		EdgeClass copiedEC = new CreateEdgeClass(context, qname, from, to, "#"
				+ sourceGraphAlias + "# from e: E{" + qname
				+ "!} reportSet e, " + sourceTargetArchExp + " end").execute();
		for (Attribute oldAttr : originalSourceEC.getOwnAttributeList()) {
			new CreateAttribute(context, new AttributeSpec(copiedEC,
					oldAttr.getName(), new CopyDomain(context,
							oldAttr.getDomain()).execute()),
					"from e: keySet(img_" + copiedEC.getQualifiedName()
							+ ") reportMap e -> e." + oldAttr.getName()
							+ " end").execute();
		}
		return copiedEC;
	}
}
