package de.uni_koblenz.javax.tools;

import java.io.Closeable;
import java.io.Flushable;
import java.io.IOException;
import java.util.Iterator;
import java.util.Set;

public interface JavaFileManager extends Closeable, Flushable, OptionChecker {
    public interface Location {
        String getName();

        boolean isOutputLocation();
    }

    @Override
    void close() throws IOException;

    @Override
    void flush() throws IOException;

    ClassLoader getClassLoader(Location location);

    FileObject getFileForInput(JavaFileManager.Location location,
            String packageName, String relativeName) throws IOException;

    FileObject getFileForOutput(JavaFileManager.Location location,
            String packageName, String relativeName, FileObject sibling)
            throws IOException;

    JavaFileObject getJavaFileForInput(JavaFileManager.Location location,
            String className, JavaFileObject.Kind kind) throws IOException;

    JavaFileObject getJavaFileForOutput(JavaFileManager.Location location,
            String className, JavaFileObject.Kind kind, FileObject sibling)
            throws IOException;

    boolean handleOption(String current, Iterator<String> remaining);

    boolean hasLocation(JavaFileManager.Location location);

    String inferBinaryName(JavaFileManager.Location location,
            JavaFileObject file);

    boolean isSameFile(FileObject a, FileObject b);

    Iterable<JavaFileObject> list(Location location, String packageName,
            Set<JavaFileObject.Kind> kinds, boolean recurse) throws IOException;

}
