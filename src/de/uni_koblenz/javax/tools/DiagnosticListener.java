package de.uni_koblenz.javax.tools;

public interface DiagnosticListener<S> {
    void report(Diagnostic<? extends S> diagnostic);
}
