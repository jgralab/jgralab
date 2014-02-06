package de.uni_koblenz.javax.lang.model.element;

public interface PackageElement extends Element {
    Name getQualifiedName();

    boolean isUnnamed();
}
