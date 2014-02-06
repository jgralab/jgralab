package de.uni_koblenz.javax.lang.model.type;

import java.util.List;

public interface ExecutableType extends TypeMirror {
    List<? extends TypeMirror> getParameterTypes();

    TypeMirror getReturnType();

    List<? extends TypeMirror> getThrownTypes();

    List<? extends TypeVariable> getTypeVariables();
}
