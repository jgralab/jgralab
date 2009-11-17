/*
 * JGraLab - The Java graph laboratory
 * (c) 2006-2009 Institute for Software Technology
 *               University of Koblenz-Landau, Germany
 *
 *               ist@uni-koblenz.de
 *
 * Please report bugs to http://serres.uni-koblenz.de/bugzilla
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */

package de.uni_koblenz.jgralab.utilities.tgschema2java;

//import gnu.getopt.Getopt;
//import gnu.getopt.LongOpt;

import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Vector;

import javax.tools.JavaCompiler;
import javax.tools.JavaFileObject;
import javax.tools.StandardJavaFileManager;
import javax.tools.ToolProvider;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionGroup;

import de.uni_koblenz.ist.utilities.option_handler.OptionHandler;
import de.uni_koblenz.jgralab.GraphIO;
import de.uni_koblenz.jgralab.GraphIOException;
import de.uni_koblenz.jgralab.JGraLab;
import de.uni_koblenz.jgralab.codegenerator.CodeGeneratorConfiguration;
import de.uni_koblenz.jgralab.schema.Domain;
import de.uni_koblenz.jgralab.schema.EdgeClass;
import de.uni_koblenz.jgralab.schema.GraphClass;
import de.uni_koblenz.jgralab.schema.Schema;
import de.uni_koblenz.jgralab.schema.VertexClass;

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
	private CodeGeneratorConfiguration config = new CodeGeneratorConfiguration();

	// /**
	// * Holds the long options
	// */
	// private LongOpt[] longOptions;

	/**
	 * Constructs an instance of TgSchema2Java with the given command line
	 * arguments and creates the schema-object after reading the .tg-file
	 * 
	 * @param args
	 *            the command line arguments; only the name of the .tg-file is
	 *            mandatory, for the commit-path and the implementation to be
	 *            used there exist default values
	 */
	public TgSchema2Java(String[] args) {
		commitPath = ".";
		compile = false;
		createJar = false;
		overwrite = true;

		// createLongOptions();

		try {
			// processing command line arguments
			processArguments(args);
			// loading .tg-file and creating schema-object
			schema = GraphIO.loadSchemaFromFile(tgFilename);
		} catch (GraphIOException e) {
			e.printStackTrace();
			System.err.println("Couldn't read schema in file '" + tgFilename
					+ "': " + e.getMessage());
			System.exit(1);
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(1);
		}
	}

	private boolean deleteFolder(String path) {
		File folder = new File(path);

		if (!folder.exists()) {
			return false;
		}
		for (String filename : folder.list()) {
			File file = new File(path + File.separator + filename);
			if (file.isDirectory()) {
				deleteFolder(file.getPath());
			} else {
				file.delete();
			}
		}
		folder.delete();

		return true;
	}

	private Set<String> getGeneratedFilePaths(String path) {
		Set<String> generatedFilePaths = new HashSet<String>();
		JavaFileFilter javaFileFilter = new JavaFileFilter();

		File folder = new File(path);

		if (!folder.exists()) {
			return generatedFilePaths;
		}
		for (File file : folder.listFiles(javaFileFilter)) {
			if (file.isDirectory()) {
				generatedFilePaths.addAll(getGeneratedFilePaths(file
						.getAbsolutePath()));
			} else {
				generatedFilePaths.add(file.getAbsolutePath());
			}
		}

		return generatedFilePaths;
	}

	/**
	 * Checks if all .java-files belonging to the specified Schema already exist
	 * in the commit path. There also must not exist any surplus .java-files.
	 * 
	 * @param schema
	 *            the Schema whose files shall be checked for existence
	 * @return true if all .java-files already exist; false otherwise
	 */
	private boolean isExistingSchema(Schema schema) {
		String pathName;
		String schemaPath = schema.getPathName();
		Set<String> existingFilePaths = getGeneratedFilePaths(commitPath
				+ File.separator + schemaPath);
		Set<String> requiredFilePaths = new HashSet<String>();

		requiredFilePaths.add(commitPath + File.separator
				+ schema.getFileName() + ".java");
		requiredFilePaths.add(commitPath + File.separator
				+ schema.getPathName() + File.separator + "impl"
				+ File.separator + schema.getName() + "Factory.java");
		for (Domain d : schema.getDomains().values()) {
			pathName = d.getPathName();

			if (pathName != "") {
				pathName = pathName.concat(File.separator);
			}
			if (d.toString().startsWith("Enum")
					|| d.toString().startsWith("Record")) {
				requiredFilePaths.add(commitPath + File.separator + schemaPath
						+ File.separator + pathName + d.getSimpleName()
						+ ".java");
			}
		}

		GraphClass gc = schema.getGraphClass();
		requiredFilePaths.add(commitPath + File.separator + schemaPath
				+ File.separator + gc.getFileName() + ".java");
		if (!gc.isAbstract()) {
			pathName = gc.getPathName();

			if (pathName != "") {
				pathName = pathName.concat(File.separator);
			}
			requiredFilePaths.add(commitPath + File.separator + schemaPath
					+ File.separator + "impl" + File.separator + pathName
					+ gc.getSimpleName() + "Impl.java");
		}

		for (VertexClass vc : schema.getVertexClassesInTopologicalOrder()) {
			if (!vc.isInternal()) {
				requiredFilePaths.add(commitPath + File.separator + schemaPath
						+ File.separator + vc.getFileName() + ".java");
				if (!vc.isAbstract()) {
					pathName = vc.getPathName();

					if (pathName != "") {
						pathName = pathName.concat(File.separator);
					}
					requiredFilePaths.add(commitPath + File.separator
							+ schemaPath + File.separator + "impl"
							+ File.separator + pathName + vc.getSimpleName()
							+ "Impl.java");
				}
			}
		}

		for (EdgeClass ec : schema.getEdgeClassesInTopologicalOrder()) {
			if (!ec.isInternal()) {
				requiredFilePaths.add(commitPath + File.separator + schemaPath
						+ File.separator + ec.getFileName() + ".java");
				if (!ec.isAbstract()) {
					pathName = ec.getPathName();

					if (pathName != "") {
						pathName = pathName.concat(File.separator);
					}
					requiredFilePaths.add(commitPath + File.separator
							+ schemaPath + File.separator + "impl"
							+ File.separator + pathName + ec.getSimpleName()
							+ "Impl.java");
					requiredFilePaths.add(commitPath + File.separator
							+ schemaPath + File.separator + "impl"
							+ File.separator + pathName + "Reversed"
							+ ec.getSimpleName() + "Impl.java");
				}
			}
		}

		/*
		 * checks if sets of existing and required .java-files are equal
		 */
		if (!existingFilePaths.containsAll(requiredFilePaths)
				|| !requiredFilePaths.containsAll(existingFilePaths)) {
			return false;
		}
		return true;
	}

	/**
	 * Processes the command line arguments and sets member variables
	 * accordingly.
	 * 
	 * @param args
	 *            the command line arguments
	 * @throws Exception
	 *             Throws Exception if mandatory option "-f" is not specified.
	 */
	private void processArguments(String[] args) throws Exception {
		CommandLine comLine = processCommandLineOptions(args);
		assert comLine != null;
		tgFilename = comLine.getOptionValue("s");
		if (comLine.hasOption("p")) {
			commitPath = comLine.getOptionValue("p");
			commitPath = commitPath.replace("/", File.separator);
			commitPath = commitPath.replace("\\", File.separator);
		}
		compile = comLine.hasOption("c");
		if (comLine.hasOption("j")) {
			createJar = true;
			jarFileName = comLine.getOptionValue("j");
		}
		if (comLine.hasOption("so")) {
			config.wantsToHaveStandardSupport(true);
			config.wantsToHaveTransactionSupport(false);
		} else if (comLine.hasOption("to")) {
			config.wantsToHaveStandardSupport(false);
			config.wantsToHaveTransactionSupport(true);
		}

		if (comLine.hasOption('w')) {
			config.wantsToHaveTypespecificMethodsSupport(false);
		}

	}

	public void compile() throws Exception {
		String packageFolder = schema.getPathName();
		File folder = new File(commitPath + File.separator + packageFolder);
		List<File> files1 = findFilesInDirectory(folder);

		JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
		StandardJavaFileManager fileManager = compiler.getStandardFileManager(
				null, null, null);

		Iterable<? extends JavaFileObject> compilationUnits1 = fileManager
				.getJavaFileObjectsFromFiles(files1);
		Vector<String> options = new Vector<String>();
		if (classpath != null) {
			options.add("-cp");
			options.add(classpath);
		}
		System.out.print("Starting compilation....");
		compiler
				.getTask(null, fileManager, null, null, null, compilationUnits1)
				.call();
		System.out.println("finished");
	}

	private List<File> findFilesInDirectory(File folder) throws Exception {
		List<File> javaSources = new ArrayList<File>();
		for (File file : folder.listFiles()) {
			if ((file != null) && file.isFile()
					&& file.getName().endsWith(".java")) {
				javaSources.add(file);
			} else if (file.isDirectory()) {
				javaSources.addAll(findFilesInDirectory(file));
			}
		}
		return javaSources;
	}

	/**
	 * Writes the .java-files.
	 */
	private void execute() {
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
				System.out.println("Committing schema "
						+ schema.getQualifiedName());
				schema.commit(commitPath, config);
				System.out.println("Schema " + schema.getQualifiedName()
						+ " committed successfully");
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
		SchemaJarGenerator jarGenerator = new SchemaJarGenerator(commitPath,
				schema.getFileName(), jarFileName);
		try {
			jarGenerator.createJar();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		TgSchema2Java tgSchema2Java = new TgSchema2Java(args);

		tgSchema2Java.execute();
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

		Option compile = new Option("c", "compile", false,
				"(optional): if specified, the .java are compiled");
		compile.setRequired(false);
		oh.addOption(compile);

		Option jar = new Option(
				"j",
				"jar",
				true,
				"(optional): specifies the name of the .jar-file; if omitted, no jar will be created");
		jar.setRequired(false);
		jar.setArgName("file");
		oh.addOption(jar);

		OptionGroup group = new OptionGroup();
		group.setRequired(false);
		Option standard = new Option("so", "standard-support-only", false,
				"(optional): Create standard support code only");
		standard.setRequired(false);
		oh.addOption(standard);

		Option transactions = new Option("to", "transaction-support-only",
				false, "(optional): Create transaction support code only");
		transactions.setRequired(false);
		oh.addOption(transactions);

		group.addOption(standard);
		group.addOption(transactions);
		oh.addOptionGroup(group);

		Option without_types = new Option("w", "without-types", false,
				"(optional): Don't create typespecific methods in classes");
		without_types.setRequired(false);
		oh.addOption(without_types);

		Option path = new Option(
				"p",
				"path",
				true,
				"(optional): specifies the path to where the created files are stored; default is current folder (\".\")");
		path.setRequired(true);
		path.setArgName("path");
		oh.addOption(path);

		return oh.parse(args);
	}

	static class JavaFileFilter implements FileFilter {
		public boolean accept(File file) {
			return (file.isDirectory() || file.getName().endsWith(".java"));
		}
	}
}
