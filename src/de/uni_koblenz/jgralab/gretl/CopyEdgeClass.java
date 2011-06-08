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
