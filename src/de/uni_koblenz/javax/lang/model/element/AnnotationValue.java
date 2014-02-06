package de.uni_koblenz.javax.lang.model.element;

public interface AnnotationValue {
    <R,P> R
    accept(AnnotationValueVisitor<R,P> v, P p);
    Object    getValue();
    @Override
    String    toString();
}
