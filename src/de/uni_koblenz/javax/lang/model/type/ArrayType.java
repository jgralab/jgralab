package de.uni_koblenz.javax.lang.model.type;

public interface ArrayType extends ReferenceType {
    TypeMirror getComponentType();
}
