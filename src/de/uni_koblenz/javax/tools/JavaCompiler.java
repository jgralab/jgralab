package de.uni_koblenz.javax.tools;

import java.io.Writer;
import java.nio.charset.Charset;
import java.util.Locale;
import java.util.concurrent.Callable;

import de.uni_koblenz.javax.annotation.processing.Processor;

public interface JavaCompiler extends Tool {
    public interface CompilationTask extends Callable<Boolean> {
        void setLocale(Locale locale);

        void setProcessors(Iterable<? extends Processor> processors);
    }

    StandardJavaFileManager getStandardFileManager(
            DiagnosticListener<? super JavaFileObject> diagnosticListener,
            Locale locale, Charset charset);

    CompilationTask getTask(Writer out, JavaFileManager fileManager,
            DiagnosticListener<? super JavaFileObject> diagnosticListener,
            Iterable<String> options, Iterable<String> classes,
            Iterable<? extends JavaFileObject> compilationUnits);
}
