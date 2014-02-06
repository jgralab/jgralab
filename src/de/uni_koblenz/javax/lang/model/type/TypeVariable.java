package de.uni_koblenz.javax.lang.model.type;

import de.uni_koblenz.javax.lang.model.element.Element;

public interface TypeVariable extends ReferenceType {
    Element asElement();

    TypeMirror getLowerBound();

    TypeMirror getUpperBound();
}
