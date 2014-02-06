package de.uni_koblenz.javax.tools;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.Writer;
import java.net.URI;

import de.uni_koblenz.javax.lang.model.element.Modifier;
import de.uni_koblenz.javax.lang.model.element.NestingKind;

public class SimpleJavaFileObject implements JavaFileObject {
    final protected URI uri;
    final protected Kind kind;

    public SimpleJavaFileObject(URI uri, Kind kind) {
        this.uri = uri;
        this.kind = kind;
    }

    @Override
    public boolean delete() {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public Modifier getAccessLevel() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public CharSequence getCharContent(boolean ignoreEncodingErrors) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public JavaFileObject.Kind getKind() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public long getLastModified() {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public String getName() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public NestingKind getNestingKind() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public boolean isNameCompatible(String simpleName, Kind kind) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public InputStream openInputStream() throws IOException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public OutputStream openOutputStream() throws IOException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Reader openReader(boolean ignoreEncodingErrors) throws IOException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Writer openWriter() throws IOException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public URI toUri() {
        // TODO Auto-generated method stub
        return null;
    }
}
