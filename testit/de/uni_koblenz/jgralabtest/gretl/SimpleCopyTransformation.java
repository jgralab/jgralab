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
