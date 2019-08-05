/*
 * JGraLab - The Java Graph Laboratory
 *
 * Copyright (C) 2006-2014 Institute for Software Technology
 *                         University of Koblenz-Landau, Germany
 *                         ist@uni-koblenz.de
 *
 * For bug reports, documentation and further information, visit
 *
 *                         https://github.com/jgralab/jgralab
 *
 * This program is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as published by the
 * Free Software Foundation; either version 3 of the License, or (at your
 * option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General
 * Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, see <http://www.gnu.org/licenses>.
 *
 * Additional permission under GNU GPL version 3 section 7
 *
 * If you modify this Program, or any covered work, by linking or combining
 * it with Eclipse (or a modified version of that program or an Eclipse
 * plugin), containing parts covered by the terms of the Eclipse Public
 * License (EPL), the licensors of this Program grant you additional
 * permission to convey the resulting work.  Corresponding Source for a
 * non-source form of such a combination shall include the source code for
 * the parts of JGraLab used as well as that of the covered work.
 */

package de.uni_koblenz.jgralab.utilities.tgschema2java;

import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.tools.JavaCompiler;
import javax.tools.JavaFileObject;
import javax.tools.StandardJavaFileManager;
import javax.tools.ToolProvider;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;

import de.uni_koblenz.ist.utilities.option_handler.OptionHandler;
import de.uni_koblenz.jgralab.GraphIO;
import de.uni_koblenz.jgralab.JGraLab;
import de.uni_koblenz.jgralab.exception.GraphIOException;
import de.uni_koblenz.jgralab.schema.Domain;
import de.uni_koblenz.jgralab.schema.EdgeClass;
import de.uni_koblenz.jgralab.schema.GraphClass;
import de.uni_koblenz.jgralab.schema.Schema;
import de.uni_koblenz.jgralab.schema.VertexClass;
import de.uni_koblenz.jgralab.schema.codegenerator.CodeGeneratorConfiguration;

public class TgSchema2Java {

    int i = 0;

    /**
     * Stores the name of the .tg-file to be converted.
     */
    private String tgFilename;

    /**
     * Stores the path to where the java-files should be written.
     */
    private String commitPath;

    /**
     * Stores the classpath.
     */
    private String classpath;

    /**
     * Specifies whether the .java-files should be compiled
     */
    private boolean compile;

    /**
     * Specifies whether a .jar-file should be created
     */
    private boolean createJar;

    /**
     * Specifies whether existing files should be overwritten.
     */
    private boolean overwrite;

    /**
     * specifies the name of the jar-file to be created
     */
    private String jarFileName = null;

    /**
     * Holds the schema object after the .tg-file has been read.
     */
    private Schema schema;

    /**
     * Configures which options the generated code should support
     */
    private CodeGeneratorConfiguration config;

    /**
     * @param args
     */
    public static void main(String[] args) {
        TgSchema2Java t = new TgSchema2Java();
        String tgFilename = null;
        try {
            // processing command line arguments
            CommandLine comLine = processCommandLineOptions(args);
            assert comLine != null;
            if (comLine.hasOption("p")) {
                t.setCommitPath(comLine.getOptionValue("p"));
            }
            t.setCompile(comLine.hasOption("c"));
            if (comLine.hasOption("j")) {
                t.setCreateJar(true);
                t.setJarFileName(comLine.getOptionValue("j"));
            }

            if (comLine.hasOption('w')) {
                t.setTypeSpecificMethodSupport(false);
            } else {
                t.setTypeSpecificMethodSupport(true);
            }

            // loading .tg-file and creating schema-object
            tgFilename = comLine.getOptionValue("s");
            t.loadSchema(tgFilename);
        } catch (GraphIOException e) {
            e.printStackTrace();
            System.err.println("Couldn't read schema in file '" + tgFilename + "': " + e.getMessage());
            System.exit(1);
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }
        t.execute();
    }

    public void loadSchema(String tgFilename) throws GraphIOException {
        schema = GraphIO.loadSchemaFromFile(tgFilename);
    }

    public void setTypeSpecificMethodSupport(boolean enabled) {
        config.setTypeSpecificMethodsSupport(enabled);
    }

    /**
     * Constructs an instance of TgSchema2Java with the given command line arguments
     * and creates the schema-object after reading the .tg-file
     */
    public TgSchema2Java() {
        config = new CodeGeneratorConfiguration();
        commitPath = ".";
        compile = false;
        createJar = false;
        overwrite = true;
    }

    private boolean deleteFolder(String path) {
        File folder = new File(path);

        if (!folder.exists()) {
            return false;
        }
        String[] filenames = folder.list();
        if (filenames != null) {
            for (String filename : filenames) {
                File file = new File(path + File.separator + filename);
                if (file.isDirectory()) {
                    deleteFolder(file.getPath());
                } else {
                    file.delete();
                }
            }
        }
        folder.delete();

        return true;
    }

    private Set<String> getGeneratedFilePaths(String path) {
        Set<String> generatedFilePaths = new HashSet<>();
        JavaFileFilter javaFileFilter = new JavaFileFilter();

        File folder = new File(path);

        if (!folder.exists()) {
            return generatedFilePaths;
        }
        File[] content = folder.listFiles(javaFileFilter);
        if (content != null) {
            for (File file : content) {
                if (file.isDirectory()) {
                    generatedFilePaths.addAll(getGeneratedFilePaths(file.getAbsolutePath()));
                } else {
                    generatedFilePaths.add(file.getAbsolutePath());
                }
            }
        }

        return generatedFilePaths;
    }

    /**
     * Checks if all .java-files belonging to the specified Schema already exist in
     * the commit path. There also must not exist any surplus .java-files.
     *
     * @param schema the Schema whose files shall be checked for existence
     * @return true if all .java-files already exist; false otherwise
     */
    private boolean isExistingSchema(Schema schema) {
        String pathName;
        String schemaPath = schema.getPathName();
        Set<String> existingFilePaths = getGeneratedFilePaths(commitPath + File.separator + schemaPath);
        Set<String> requiredFilePaths = new HashSet<>();

        requiredFilePaths.add(commitPath + File.separator + schema.getFileName() + ".java");
        requiredFilePaths.add(commitPath + File.separator + schema.getPathName() + File.separator + "impl"
                + File.separator + schema.getName() + "Factory.java");
        for (Domain d : schema.getDomains()) {
            pathName = d.getPathName();

            if (!pathName.isEmpty()) {
                pathName = pathName.concat(File.separator);
            }
            if (d.toString()
                    .startsWith("Enum")
                    || d.toString()
                            .startsWith("Record")) {
                requiredFilePaths.add(commitPath + File.separator + schemaPath + File.separator + pathName
                        + d.getSimpleName() + ".java");
            }
        }

        GraphClass gc = schema.getGraphClass();
        requiredFilePaths.add(commitPath + File.separator + schemaPath + File.separator + gc.getFileName() + ".java");

        pathName = gc.getPathName();

        if (!pathName.isEmpty()) {
            pathName = pathName.concat(File.separator);
        }
        requiredFilePaths.add(commitPath + File.separator + schemaPath + File.separator + "impl" + File.separator
                + pathName + gc.getSimpleName() + "Impl.java");

        for (VertexClass vc : schema.getGraphClass()
                .getVertexClasses()) {
            requiredFilePaths
                    .add(commitPath + File.separator + schemaPath + File.separator + vc.getFileName() + ".java");
            if (!vc.isAbstract()) {
                pathName = vc.getPathName();

                if (!pathName.isEmpty()) {
                    pathName = pathName.concat(File.separator);
                }
                requiredFilePaths.add(commitPath + File.separator + schemaPath + File.separator + "impl"
                        + File.separator + pathName + vc.getSimpleName() + "Impl.java");
            }
        }

        for (EdgeClass ec : schema.getGraphClass()
                .getEdgeClasses()) {
            requiredFilePaths
                    .add(commitPath + File.separator + schemaPath + File.separator + ec.getFileName() + ".java");
            if (!ec.isAbstract()) {
                pathName = ec.getPathName();

                if (!pathName.isEmpty()) {
                    pathName = pathName.concat(File.separator);
                }
                requiredFilePaths.add(commitPath + File.separator + schemaPath + File.separator + "impl"
                        + File.separator + pathName + ec.getSimpleName() + "Impl.java");
                requiredFilePaths.add(commitPath + File.separator + schemaPath + File.separator + "impl"
                        + File.separator + pathName + "Reversed" + ec.getSimpleName() + "Impl.java");
            }
        }

        /*
         * checks if sets of existing and required .java-files are equal
         */
        if (!existingFilePaths.containsAll(requiredFilePaths) || !requiredFilePaths.containsAll(existingFilePaths)) {
            return false;
        }
        return true;
    }

    public void compile() throws Exception {
        String packageFolder = schema.getPathName();
        File folder = new File(commitPath + File.separator + packageFolder);
        List<File> files1 = findFilesInDirectory(folder);

        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        StandardJavaFileManager fileManager = compiler.getStandardFileManager(null, null, null);

        Iterable<? extends JavaFileObject> compilationUnits1 = fileManager.getJavaFileObjectsFromFiles(files1);
        ArrayList<String> options = new ArrayList<>();
        if (classpath != null) {
            options.add("-cp");
            options.add(classpath);
        }
        System.out.print("Starting compilation....");
        compiler.getTask(null, fileManager, null, null, null, compilationUnits1)
                .call();
        System.out.println("finished");
    }

    private List<File> findFilesInDirectory(File folder) throws Exception {
        List<File> javaSources = new ArrayList<>();
        File[] content = folder.listFiles();
        if (content != null) {
            for (File file : content) {
                if (file.isFile() && file.getName()
                        .endsWith(".java")) {
                    javaSources.add(file);
                } else if (file.isDirectory()) {
                    javaSources.addAll(findFilesInDirectory(file));
                }
            }
        }
        return javaSources;
    }

    /**
     * Writes the .java-files.
     */
    public void execute() {
        int input;

        try {
            if (isExistingSchema(schema)) {
                System.out.println("Schema already exists in " + commitPath);
                System.out.print("Overwrite existing files (y|n)? ");
                input = System.in.read();
                switch (input) {
                case 'y':
                    break; // overwrite is 'true' by default
                case 'n':
                    overwrite = false;
                }
            }
            if (overwrite) {
                deleteFolder(commitPath + File.separator + schema.getPathName());
                System.out.println("Committing schema " + schema.getQualifiedName());
                schema.commit(commitPath, config);
                System.out.println("Schema " + schema.getQualifiedName() + " committed successfully");
            }
            if (compile) {
                System.out.println("Compiling...");
                compile();
                System.out.println("Compiling successful");
            }
            if (createJar) {
                System.out.println("Creating .jar-file");
                generateJarFile();
                System.out.println(".jar-file created successfully");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * generates the .jar-file
     */
    private void generateJarFile() {
        SchemaJarGenerator jarGenerator = new SchemaJarGenerator(commitPath, schema.getFileName(), jarFileName);
        try {
            jarGenerator.createJar();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private static CommandLine processCommandLineOptions(String[] args) {
        String toolString = "java " + TgSchema2Java.class.getName();
        String versionString = JGraLab.getInfo(false);
        OptionHandler oh = new OptionHandler(toolString, versionString);

        Option filename = new Option("s", "schema", true,
                "(required): specifies the .tg-file of the schema to be converted");
        filename.setRequired(true);
        filename.setArgName("file");
        oh.addOption(filename);

        Option compile = new Option("c", "compile", false, "(optional): if specified, the .java are compiled");
        compile.setRequired(false);
        oh.addOption(compile);

        Option jar = new Option("j", "jar", true,
                "(optional): specifies the name of the .jar-file; if omitted, no jar will be created");
        jar.setRequired(false);
        jar.setArgName("file");
        oh.addOption(jar);

        Option without_types = new Option("w", "without-types", false,
                "(optional): Don't create typespecific methods in classes");
        without_types.setRequired(false);
        oh.addOption(without_types);

        Option path = new Option("p", "path", true,
                "specifies the path to where the created files are stored; default is current folder (\".\")");
        path.setRequired(true);
        path.setArgName("path");
        oh.addOption(path);

        return oh.parse(args);
    }

    static class JavaFileFilter implements FileFilter {
        @Override
        public boolean accept(File file) {
            return (file.isDirectory() || file.getName()
                    .endsWith(".java"));
        }
    }

    public String getTgFilename() {
        return tgFilename;
    }

    public void setTgFilename(String tgFilename) {
        this.tgFilename = tgFilename;
    }

    public String getCommitPath() {
        return commitPath;
    }

    public void setCommitPath(String commitPath) {
        commitPath = commitPath.replace("/", File.separator);
        commitPath = commitPath.replace("\\", File.separator);
        this.commitPath = commitPath;
    }

    public boolean isCompile() {
        return compile;
    }

    public void setCompile(boolean compile) {
        this.compile = compile;
    }

    public boolean isCreateJar() {
        return createJar;
    }

    public void setCreateJar(boolean createJar) {
        this.createJar = createJar;
    }

    public boolean isOverwrite() {
        return overwrite;
    }

    public void setOverwrite(boolean overwrite) {
        this.overwrite = overwrite;
    }

    public String getJarFileName() {
        return jarFileName;
    }

    public void setJarFileName(String jarFileName) {
        this.jarFileName = jarFileName;
    }

    public Schema getSchema() {
        return schema;
    }
}
