package de.uni_koblenz.javax.lang.model.element;

import java.util.Map;

import de.uni_koblenz.javax.lang.model.type.DeclaredType;

public interface AnnotationMirror {
    DeclaredType getAnnotationType();

    Map<? extends ExecutableElement, ? extends AnnotationValue> getElementValues();
}
