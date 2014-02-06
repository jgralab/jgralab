package de.uni_koblenz.javax.tools;

import java.io.Closeable;
import java.io.Flushable;
import java.io.IOException;
import java.util.Iterator;
import java.util.Set;

import de.uni_koblenz.javax.tools.JavaFileObject.Kind;

public class ForwardingJavaFileManager<M extends JavaFileManager> implements
        Closeable, Flushable, JavaFileManager, OptionChecker {
    final protected M fileManager;

    public ForwardingJavaFileManager(M fileManager) {
        this.fileManager = fileManager;
    }

    @Override
    public void close() throws IOException {
        fileManager.close();
    }

    @Override
    public int isSupportedOption(String option) {
        return fileManager.isSupportedOption(option);
    }

    @Override
    public ClassLoader getClassLoader(Location location) {
        return fileManager.getClassLoader(location);
    }

    @Override
    public FileObject getFileForInput(Location location, String packageName,
            String relativeName) throws IOException {
        return fileManager.getFileForInput(location, packageName, relativeName);
    }

    @Override
    public FileObject getFileForOutput(Location location, String packageName,
            String relativeName, FileObject sibling) throws IOException {
        return fileManager.getFileForOutput(location, packageName,
                relativeName, sibling);
    }

    @Override
    public JavaFileObject getJavaFileForInput(Location location,
            String className, Kind kind) throws IOException {
        return fileManager.getJavaFileForInput(location, className, kind);
    }

    @Override
    public JavaFileObject getJavaFileForOutput(Location location,
            String className, Kind kind, FileObject sibling) throws IOException {
        return fileManager.getJavaFileForOutput(location, className, kind,
                sibling);
    }

    @Override
    public boolean handleOption(String current, Iterator<String> remaining) {
        return fileManager.handleOption(current, remaining);
    }

    @Override
    public boolean hasLocation(Location location) {
        return fileManager.hasLocation(location);
    }

    @Override
    public String inferBinaryName(Location location, JavaFileObject file) {
        return fileManager.inferBinaryName(location, file);
    }

    @Override
    public boolean isSameFile(FileObject a, FileObject b) {
        return fileManager.isSameFile(a, b);
    }

    @Override
    public Iterable<JavaFileObject> list(Location location, String packageName,
            Set<Kind> kinds, boolean recurse) throws IOException {
        return fileManager.list(location, packageName, kinds, recurse);
    }

    @Override
    public void flush() throws IOException {
        fileManager.flush();
    }
}
