package de.uni_koblenz.javax.tools;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.Writer;
import java.net.URI;

public interface FileObject {
    boolean delete();
    CharSequence getCharContent(boolean ignoreEncodingErrors);
    long getLastModified();
    String getName();
    InputStream openInputStream() throws IOException;
    OutputStream openOutputStream() throws IOException;
    Reader openReader(boolean ignoreEncodingErrors)
            throws IOException;
    Writer openWriter()
            throws IOException;
    URI toUri();
}
