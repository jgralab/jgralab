package de.uni_koblenz.javax.lang.model.element;

public interface Name extends Element {
    boolean contentEquals(CharSequence cs);

    @Override
    boolean equals(Object obj);

    @Override
    int hashCode();
}
