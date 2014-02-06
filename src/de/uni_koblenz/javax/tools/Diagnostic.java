package de.uni_koblenz.javax.tools;

import java.util.Locale;

public interface Diagnostic<S> {
    public static enum Kind {
        ERROR, MANDATORY_WARNING, NOTE, OTHER, WARNING
    }

    final static long NOPOS = -1L;

    String getCode();

    long getColumnNumber();

    long getEndPosition();

    Diagnostic.Kind getKind();

    long getLineNumber();

    String getMessage(Locale locale);

    long getPosition();

    S getSource();

    long getStartPosition();
}
