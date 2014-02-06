package de.uni_koblenz.javax.lang.model.type;

public interface TypeMirror {
    <R, P> R accept(TypeVisitor<R, P> v, P p);

    @Override
    boolean equals(Object obj);

    TypeKind getKind();

    @Override
    int hashCode();

    @Override
    String toString();
}
