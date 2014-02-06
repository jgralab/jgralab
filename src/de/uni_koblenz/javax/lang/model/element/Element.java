package de.uni_koblenz.javax.lang.model.element;

public interface Element {
    <R,P> R accept(ElementVisitor<R,P> v,
            P p);
}
