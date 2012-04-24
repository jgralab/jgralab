/**
 *
 */
package de.uni_koblenz.jgralab.gretl;

import de.uni_koblenz.jgralab.gretl.parser.TokenTypes;

/**
 * @author horn
 *
 */
public class Call extends Transformation<Object> {

	private final Transformation<? extends Object> transformation;

	public Call(Context c, Transformation<? extends Object> transform) {
		super(c);
		transformation = transform;
	}

	public static Call parseAndCreate(ExecuteTransformation et) {
		String name = et.match(TokenTypes.IDENT).value;
		Transformation<?> t = et.getDefinedTransformation(name);
		return new Call(et.context, t);
	}

	@Override
	protected Object transform() {
		return transformation.execute();
	}

}
