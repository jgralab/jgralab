package de.uni_koblenz.jgralab.gretl;

import de.uni_koblenz.jgralab.gretl.Context.TransformationPhase;
import de.uni_koblenz.jgralab.schema.EdgeClass;
import de.uni_koblenz.jgralab.schema.GraphElementClass;
import de.uni_koblenz.jgralab.schema.VertexClass;

public class AddSuperClass extends Transformation<GraphElementClass> {
	private GraphElementClass subClass;
	private GraphElementClass superClass;

	public AddSuperClass(final Context c, final VertexClass subClass,
			final VertexClass superClass) {
		super(c);
		this.subClass = subClass;
		this.superClass = superClass;
	}

	public AddSuperClass(final Context c, final EdgeClass subClass,
			final EdgeClass superClass) {
		super(c);
		this.subClass = subClass;
		this.superClass = superClass;
	}

	public static AddSuperClass parseAndCreate(ExecuteTransformation et) {
		GraphElementClass subGec = et.matchGraphElementClass();
		if (subGec instanceof VertexClass) {
			VertexClass superVC = et.matchVertexClass();
			return new AddSuperClass(et.context, (VertexClass) subGec, superVC);
		} else {
			EdgeClass superEC = et.matchEdgeClass();
			return new AddSuperClass(et.context, (EdgeClass) subGec, superEC);
		}
	}

	@Override
	protected GraphElementClass transform() {
		if (context.phase != TransformationPhase.SCHEMA) {
			return subClass;
		}

		if (superClass instanceof VertexClass) {
			((VertexClass) subClass).addSuperClass((VertexClass) superClass);
		} else {
			((EdgeClass) subClass).addSuperClass((EdgeClass) superClass);
		}

		return subClass;
	}

}
