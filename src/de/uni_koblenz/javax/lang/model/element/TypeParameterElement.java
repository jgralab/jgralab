package de.uni_koblenz.javax.lang.model.element;

import java.util.List;

import de.uni_koblenz.javax.lang.model.type.TypeMirror;

public interface TypeParameterElement extends Element {
    List<? extends TypeMirror> getBounds();

    Element getGenericElement();
}
