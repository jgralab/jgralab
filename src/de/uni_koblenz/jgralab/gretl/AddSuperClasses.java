package de.uni_koblenz.jgralab.gretl;

import de.uni_koblenz.jgralab.schema.EdgeClass;
import de.uni_koblenz.jgralab.schema.GraphElementClass;
import de.uni_koblenz.jgralab.schema.VertexClass;

public class AddSuperClasses extends Transformation<GraphElementClass<?, ?>> {
	private GraphElementClass<?, ?> subClass;
	private GraphElementClass<?, ?>[] superClasses;

	public AddSuperClasses(final Context c, final VertexClass subClass,
			final VertexClass... superClasses) {
		super(c);
		this.subClass = subClass;
		this.superClasses = superClasses;
	}

	public AddSuperClasses(final Context c, final EdgeClass subClass,
			final EdgeClass... superClasses) {
		super(c);
		this.subClass = subClass;
		this.superClasses = superClasses;
	}

	public static AddSuperClasses parseAndCreate(ExecuteTransformation et) {
		GraphElementClass<?, ?> subGec = et.matchGraphElementClass();
		if (subGec instanceof VertexClass) {
			VertexClass[] superVCs = et.matchVertexClassArray();
			return new AddSuperClasses(et.context, (VertexClass) subGec,
					superVCs);
		} else {
			EdgeClass[] superECs = et.matchEdgeClassArray();
			return new AddSuperClasses(et.context, (EdgeClass) subGec, superECs);
		}
	}

	@Override
	protected GraphElementClass<?, ?> transform() {
		for (GraphElementClass<?, ?> superCls : superClasses) {
			if (subClass instanceof VertexClass) {
				new AddSuperClass(context, (VertexClass) subClass,
						(VertexClass) superCls).execute();
			} else {
				new AddSuperClass(context, (EdgeClass) subClass,
						(EdgeClass) superCls).execute();
			}
		}

		return subClass;
	}

}
