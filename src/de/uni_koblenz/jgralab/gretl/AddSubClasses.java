package de.uni_koblenz.jgralab.gretl;

import de.uni_koblenz.jgralab.schema.EdgeClass;
import de.uni_koblenz.jgralab.schema.GraphElementClass;
import de.uni_koblenz.jgralab.schema.VertexClass;

public class AddSubClasses extends Transformation<GraphElementClass<?, ?>> {

	private GraphElementClass<?, ?> superClass;
	private GraphElementClass<?, ?>[] subClasses;

	public AddSubClasses(final Context c, final VertexClass superClass,
			final VertexClass... subClasses) {
		super(c);
		this.superClass = superClass;
		this.subClasses = subClasses;
		if (subClasses.length < 1) {
			throw new GReTLException(c, "No subclasses given!");
		}
	}

	public AddSubClasses(final Context c, final EdgeClass superClass,
			final EdgeClass... subClasses) {
		super(c);
		this.superClass = superClass;
		this.subClasses = subClasses;
		if (subClasses.length < 1) {
			throw new GReTLException(c, "No subclasses given!");
		}
	}

	public static AddSubClasses parseAndCreate(ExecuteTransformation et) {
		GraphElementClass<?, ?> superGec = et.matchGraphElementClass();
		if (superGec instanceof VertexClass) {
			VertexClass[] subVCs = et.matchVertexClassArray();
			return new AddSubClasses(et.context, (VertexClass) superGec, subVCs);
		} else {
			EdgeClass[] subECs = et.matchEdgeClassArray();
			return new AddSubClasses(et.context, (EdgeClass) superGec, subECs);
		}
	}

	@Override
	protected GraphElementClass<?, ?> transform() {
		for (GraphElementClass<?, ?> sub : subClasses) {
			if (superClass instanceof VertexClass) {
				new AddSubClass(context, (VertexClass) superClass,
						(VertexClass) sub).execute();
			} else {
				new AddSubClass(context, (EdgeClass) superClass,
						(EdgeClass) sub).execute();
			}
		}

		return superClass;
	}

}
