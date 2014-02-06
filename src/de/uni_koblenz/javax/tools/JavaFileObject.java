package de.uni_koblenz.javax.tools;

import de.uni_koblenz.javax.lang.model.element.Modifier;
import de.uni_koblenz.javax.lang.model.element.NestingKind;

public interface JavaFileObject extends FileObject {
    /**
     * TODO: Check if extensions are correct. Not sure about OTHER -ti
     */
    public static enum Kind {
        CLASS(".class"), HTML(".html"), OTHER(".*"), SOURCE(".java");

        final public String extension;

        private Kind(String ext) {
            extension = ext;
        }
    }

    Kind getKind();

    boolean isNameCompatible(String simpleName, Kind kind);

    NestingKind getNestingKind();

    Modifier getAccessLevel();
}
