/**
 *
 */
package de.uni_koblenz.jgralab.gretl;

import de.uni_koblenz.jgralab.gretl.parser.TokenTypes;

/**
 * @author horn
 *
 */
public class Call extends CountingTransformation {

	private final CountingTransformation transformation;

	public Call(Context c, CountingTransformation transform) {
		super(c);
		transformation = transform;
	}

	public static Call parseAndCreate(ExecuteTransformation et) {
		String name = et.match(TokenTypes.IDENT).value;
		CountingTransformation t = (CountingTransformation) et
				.getDefinedTransformation(name);
		return new Call(et.context, t);
	}

	@Override
	protected Integer transform() {
		return transformation.execute();
	}

}
