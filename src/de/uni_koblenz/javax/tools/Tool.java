package de.uni_koblenz.javax.tools;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.Set;

import de.uni_koblenz.javax.lang.model.SourceVersion;

public interface Tool {
    Set<SourceVersion> getSourceVersions();

    int run(InputStream in, OutputStream out, OutputStream err,
            String... arguments);
}
