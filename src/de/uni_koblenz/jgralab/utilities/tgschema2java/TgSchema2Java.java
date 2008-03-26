/*
 * JGraLab - The Java graph laboratory
 * (c) 2006-2007 Institute for Software Technology
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

import gnu.getopt.Getopt;
import gnu.getopt.LongOpt;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import de.uni_koblenz.jgralab.GraphIO;
import de.uni_koblenz.jgralab.GraphIOException;
import de.uni_koblenz.jgralab.schema.Domain;
import de.uni_koblenz.jgralab.schema.EdgeClass;
import de.uni_koblenz.jgralab.schema.GraphClass;
import de.uni_koblenz.jgralab.schema.Schema;
import de.uni_koblenz.jgralab.schema.VertexClass;
import de.uni_koblenz.jgralab.schema.impl.SchemaImpl;
import de.uni_koblenz.jgralab.utilities.tgschema2java.SchemaJarGenerator;

public class TgSchema2Java {

	/**
	 * Stores the name of the .tg-file to be converted. 
	 */
	private String tgFilename;
	
	/**
	 * Stores the path to where the java-files should be written.
	 */
	private String commitPath;
	
	/**
	 * Stores the path to the javac compiler
	 */
	private String javacPath;
	
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
	 * Holds the long options
	 */
	private LongOpt[] longOptions;
	
	/**
	 * Constructs an instance of TgSchema2Java with the given command line arguments 
	 * and creates the schema-object after reading the .tg-file
	 * 
	 * @param args the command line arguments; only the name of the .tg-file is 
	 * mandatory, for the commit-path and the implementation to be used there exist
	 * default values
	 */
	public TgSchema2Java(String[] args) {
		commitPath = ".";
		compile = false;
		createJar = false;
		overwrite = true;
				
		createLongOptions();
		
		try {
			//processing command line arguments
			processArguments(args);
			//loading .tg-file and creating schema-object
			schema = GraphIO.loadSchemaFromFile(tgFilename);
		}
		catch (GraphIOException e) {
			e.printStackTrace();
			System.err.println("Couldn't read schema in file '" + tgFilename + "': " + e.getMessage());
			System.exit(1);
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(1);
		}
	}
	
	/**
	 * Stores the long option names in array longOptions
	 */
	private void createLongOptions() {
		longOptions = new LongOpt[7];
		
		longOptions[0] = new LongOpt("filename", LongOpt.REQUIRED_ARGUMENT, null, 'f');
		longOptions[1] = new LongOpt("path", LongOpt.REQUIRED_ARGUMENT, null, 'p');
		longOptions[2] = new LongOpt("implementation", LongOpt.REQUIRED_ARGUMENT, null, 'i');
		longOptions[3] = new LongOpt("help", LongOpt.NO_ARGUMENT, null, 'h');
		longOptions[4] = new LongOpt("compile", LongOpt.REQUIRED_ARGUMENT, null, 'c');
		longOptions[5] = new LongOpt("cp", LongOpt.REQUIRED_ARGUMENT, null, 's');
		longOptions[6] = new LongOpt("filename", LongOpt.REQUIRED_ARGUMENT, null, 's');
	}
		
	private boolean deleteFolder(String path) {
		File folder = new File(path);
		File file = new File(path);
		
		if (!folder.exists())
			return false;
		for (String filename : folder.list()) {
			file = new File(path + File.separator + filename);
			if (file.isDirectory())
				deleteFolder(file.getPath());
			else
				file.delete();
		}
		folder.delete();
		
		return true;
	}
	
	/**
	 * Checks if all .java-files belonging to the specified Schema already exist in
	 * the commit path. There also must not exist any surplus .java-files.
	 * 
	 * @param schema the Schema whose files shall be checked for existence
	 * @return true if all .java-files already exist; false otherwise
	 */
	private boolean isExistingSchema(Schema schema) {
		String packageFolder = schema.getDirectoryName();
		File interfaceFolder = new File(commitPath + File.separator + packageFolder);
		File implFolder = new File(commitPath + File.separator + packageFolder + File.separator + SchemaImpl.IMPLPACKAGENAME);
		if (!interfaceFolder.exists() || !implFolder.exists())
			return false;
		
		// retrieve names of existing .java-files
		JavaFileFilter javaFileFilter = new JavaFileFilter();		
		Set<String> interfaceFilenames = new HashSet<String>(
				Arrays.asList(interfaceFolder.list(javaFileFilter)));
		Set<String> implFilenames = new HashSet<String>(
				Arrays.asList(implFolder.list(javaFileFilter)));
				
		/* retrieve names of Schema, Domains and AttributedElementClasses and store them in
		 * requiredInterfaceFilenames; also store names of all non-abstract
		 * AttributedElementClasses in requiredImplFilenames
		 */
		Set<String> requiredInterfaceFilenames = new HashSet<String>();
		Set<String> requiredImplFilenames = new HashSet<String>();
		
		requiredInterfaceFilenames.add(schema.getName() + ".java");
		for (Domain d : schema.getCompositeDomainsInTopologicalOrder())
			if (d.toString().startsWith("Enum") || d.toString().startsWith("Record"))
				requiredInterfaceFilenames.add(d.getName() + ".java");
		for (GraphClass gc : schema.getGraphClassesInTopologicalOrder())
			if (!gc.isInternal()) {
				requiredInterfaceFilenames.add(gc.getName() + ".java");
				if (!gc.isAbstract())
					requiredImplFilenames.add(gc.getName() + "Impl.java");
			}
		for (VertexClass vc : schema.getVertexClassesInTopologicalOrder())
			if (!vc.isInternal()) {
				requiredInterfaceFilenames.add(vc.getName() + ".java");
				if (!vc.isAbstract())
					requiredImplFilenames.add(vc.getName() + "Impl.java");
			}
		for (EdgeClass ec : schema.getEdgeClassesInTopologicalOrder())
			if (!ec.isInternal()) {
				requiredInterfaceFilenames.add(ec.getName() + ".java");
				if (!ec.isAbstract()) {
					requiredImplFilenames.add(ec.getName() + "Impl.java");
					requiredImplFilenames.add("Reversed" + ec.getName() + "Impl.java");
				}
			}
		
		/* checks if sets of existing and required .java-files are equal
		 */
		if (!interfaceFilenames.containsAll(requiredInterfaceFilenames)
				|| !implFilenames.containsAll(requiredImplFilenames)
				|| !requiredInterfaceFilenames.containsAll(interfaceFilenames)
				|| !requiredImplFilenames.containsAll(implFilenames))
			return false;
		
		return true;
	}
	
	/**
	 * Processes the command line arguments and sets member variables accordingly.
	 * 
	 * @param args the command line arguments
	 * @throws Exception Throws Exception if mandatory option "-f" is not specified.
	 */
	private void processArguments(String[] args) throws Exception {
		Getopt getopt = new Getopt("TgSchema2Java", args, "f:p:hc:j:s:", longOptions);
		int option;
		boolean missingFilenameOption = true;
				
		/*if no command line arguments are specified, create an "-h" argument in
		 *order to invoke printHelp()
		 */
		if (args.length == 0)
			getopt.setArgv(new String[] {"-h"});
		//processing of arguments and setting member variables accordingly
		while ((option = getopt.getopt()) != -1) {
			switch(option) {
				case 'f': 
					missingFilenameOption = false;
					tgFilename = getopt.getOptarg();
					break;
				case 'p': 
					commitPath = getopt.getOptarg();
					break;
				case 'c':
					compile = true;
					javacPath = getopt.getOptarg();
					break;
				case 's':
					classpath = getopt.getOptarg();
					break;
				case 'j': //create jar-file
					createJar = true;
					jarFileName = getopt.getOptarg();
					break;
				case 'h':
					printHelp();
					System.exit(0);
			}
		}
		
		if (missingFilenameOption)
			throw new Exception("Missing option \"-f\"");
	}
	
	/**
	 * Prints help.
	 */
	private void printHelp() {
		System.out.println("Usage: java " + TgSchema2Java.class.getSimpleName() +"\n" 
				+ " (-f | --filename) <filename>[.tg] [(-p | --path) <commit-path>]\n" 
				+ " [(-i | --implementation) (array|list)] [(-h | --help)]\n"
				+ " [(-c | --compile) <javac-path>]\n"
				+ " [(-s | --cp | --classpath) <classpath>\n");
		System.out.println("Options:");
		System.out.println("-f | --filename (required): specifies the .tg-file to be converted");
		System.out.println("-p | --path (optional): specifies the path to where the created\n" +
				           "                        files are stored; default is current folder (\".\")");
		System.out.println("-i | --implementation (optional): takes \"array\" or \"list\" as\n" + 
				           "                                  arguments and specifies whether the array-\n" +
				           "                                  or the list-implementation is used;\n" + 
				           "                                  default is \"array\"");
		System.out.println("-c | --compile (optional): specifies the path to the javac-compiler;\n" +
				           "                           if not specified, the .java-files will not be\n" + 
				           "                           compiled");
		System.out.println("-s | --cp | --classpath (optional): specifies the path to jgralab");
		System.out.println("-j | --jar (optional): specifies the name of the .jar-file;\n" +
				           "                       if omitted, no jar will be created");
		System.out.println("-h | --help (optional): prints this help");
	}
	
	/**
	 * Compiles the written .java-files
	 */
	private void compile() throws IOException {
		String packageFolder = schema.getDirectoryName();
		File folder = new File(commitPath + File.separator + packageFolder);
		
		//compiling of interfaces
		for(String filename : folder.list()) {
			if (filename.endsWith(".java"))
				Runtime.getRuntime().exec(new String[] { javacPath + File.separator + "javac", 
						"-classpath", classpath, "-sourcepath", commitPath, 
						"-d", commitPath, packageFolder + File.separator + filename } );
		}
		
		folder = new File(commitPath + File.separator + packageFolder + File.separator + SchemaImpl.IMPLPACKAGENAME);
		
		//compiling of classes (*Impl.java)
		for(String filename : folder.list()) {
			if (filename.endsWith(".java"))
				Runtime.getRuntime().exec(new String[] { javacPath + "/javac", 
						"-classpath", classpath, "-sourcepath", commitPath, 
						"-d", commitPath, packageFolder + "/impl/" + filename } );
		}
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
				switch(input) {
					case 'y':
						break; //overwrite is 'true' by default
					case 'n':
						overwrite = false;
				}		
			}
			if (overwrite) {
				deleteFolder(commitPath + File.separator + schema.getDirectoryName());
				System.out.println("Committing schema " + schema.getQualifiedName());
				schema.commit(commitPath);
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
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * generates the .jar-file
	 */
	private void generateJarFile() {
		SchemaJarGenerator jarGenerator = new SchemaJarGenerator(commitPath, schema.getDirectoryName(), jarFileName);
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
	
	class JavaFileFilter implements FilenameFilter {
		public boolean accept(File dir, String name) {
			return name.endsWith(".java");
		}
	}
}