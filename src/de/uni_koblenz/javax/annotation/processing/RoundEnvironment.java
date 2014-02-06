package de.uni_koblenz.javax.annotation.processing;

import java.lang.annotation.Annotation;
import java.util.Set;

import de.uni_koblenz.javax.lang.model.element.Element;
import de.uni_koblenz.javax.lang.model.element.TypeElement;

public interface RoundEnvironment {
    boolean errorRaised();

    Set<? extends Element> getElementsAnnotatedWith(
            Class<? extends Annotation> a);

    Set<? extends Element> getElementsAnnotatedWith(TypeElement a);

    Set<? extends Element> getRootElements();

    boolean processingOver();
}
