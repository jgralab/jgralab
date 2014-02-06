package de.uni_koblenz.javax.annotation.processing;

import java.util.Set;

import javax.annotation.processing.ProcessingEnvironment;

import de.uni_koblenz.javax.lang.model.SourceVersion;
import de.uni_koblenz.javax.lang.model.element.AnnotationMirror;
import de.uni_koblenz.javax.lang.model.element.Element;
import de.uni_koblenz.javax.lang.model.element.ExecutableElement;
import de.uni_koblenz.javax.lang.model.element.TypeElement;

public interface Processor {
    Iterable<? extends Completion> getCompletions(Element element,
            AnnotationMirror annotation, ExecutableElement member,
            String userText);

    Set<String> getSupportedAnnotationTypes();

    Set<String> getSupportedOptions();

    SourceVersion getSupportedSourceVersion();

    void init(ProcessingEnvironment processingEnv);

    boolean process(Set<? extends TypeElement> annotations,
            RoundEnvironment roundEnv);
}
