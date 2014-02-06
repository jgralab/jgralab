package de.uni_koblenz.javax.lang.model.type;

public enum TypeKind {
    ARRAY(false), BOOLEAN(true), BYTE(true), CHAR(true), DECLARED(false), DOUBLE(
            true), ERROR(false), EXECUTABLE(false), FLOAT(true), INT(true), LONG(
            true), NONE(false), NULL(false), OTHER(false), PACKAGE(false), SHORT(
            true), TYPEVAR(false), VOID(false), WILDCARD(false);

    final private boolean isPrimitive;

    private TypeKind(boolean isPrimitive) {
        this.isPrimitive = isPrimitive;
    }

    public boolean isPrimitive() {
        return isPrimitive;
    }
}
