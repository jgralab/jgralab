package de.uni_koblenz.javax.tools;

import java.io.File;

public interface StandardJavaFileManager extends JavaFileManager {
    Iterable<? extends JavaFileObject> getJavaFileObjects(File... files);

    Iterable<? extends JavaFileObject> getJavaFileObjects(String... names);

    Iterable<? extends JavaFileObject> getJavaFileObjectsFromFiles(
            Iterable<? extends File> files);

    Iterable<? extends JavaFileObject> getJavaFileObjectsFromStrings(
            Iterable<String> names);

    Iterable<? extends File> getLocation(JavaFileManager.Location location);

    void setLocation(JavaFileManager.Location location,
            Iterable<? extends File> path);
}
