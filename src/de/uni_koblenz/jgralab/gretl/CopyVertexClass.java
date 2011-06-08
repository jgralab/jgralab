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
