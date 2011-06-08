package de.uni_koblenz.jgralab.gretl.parser;

import de.uni_koblenz.jgralab.gretl.Context;
import de.uni_koblenz.jgralab.gretl.GReTLException;

public class GReTLParsingException extends GReTLException {

	private static final long serialVersionUID = 7020342970993253707L;

	public GReTLParsingException(Context c, String msg) {
		super(c, msg);
	}

	public GReTLParsingException(Context context, String string, Exception cause) {
		super(context, string, cause);
	}

}
