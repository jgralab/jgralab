package de.uni_koblenz.jgralab.gretl;

import de.uni_koblenz.jgralab.gretl.Context.TransformationPhase;
import de.uni_koblenz.jgralab.schema.EdgeClass;
import de.uni_koblenz.jgralab.schema.GraphElementClass;
import de.uni_koblenz.jgralab.schema.VertexClass;

public class AddSubClass extends Transformation<GraphElementClass<?, ?>> {

	private GraphElementClass<?, ?> superClass;
	private GraphElementClass<?, ?> subClass;

	public AddSubClass(final Context c, final VertexClass superClass,
			final VertexClass subClass) {
		super(c);
		this.superClass = superClass;
		this.subClass = subClass;
	}

	public AddSubClass(final Context c, final EdgeClass superClass,
			final EdgeClass subClass) {
		super(c);
		this.superClass = superClass;
		this.subClass = subClass;
	}

	public static AddSubClass parseAndCreate(ExecuteTransformation et) {
		GraphElementClass<?, ?> superGec = et.matchGraphElementClass();
		if (superGec instanceof VertexClass) {
			VertexClass subVC = et.matchVertexClass();
			return new AddSubClass(et.context, (VertexClass) superGec, subVC);
		} else {
			EdgeClass subEC = et.matchEdgeClass();
			return new AddSubClass(et.context, (EdgeClass) superGec, subEC);
		}
	}

	@Override
	protected GraphElementClass<?, ?> transform() {
		if (context.phase != TransformationPhase.SCHEMA) {
			return superClass;
		}

		if (superClass instanceof VertexClass) {
			((VertexClass) subClass).addSuperClass((VertexClass) superClass);
		} else {
			((EdgeClass) subClass).addSuperClass((EdgeClass) superClass);
		}

		return superClass;
	}
}
