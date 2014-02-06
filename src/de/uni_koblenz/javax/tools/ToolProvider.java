package de.uni_koblenz.javax.tools;

public class ToolProvider {
    public static JavaCompiler getSystemJavaCompiler() {
        return null;
    }

    public static ClassLoader getSystemToolClassLoader() {
        return ClassLoader.getSystemClassLoader();
    }
}
