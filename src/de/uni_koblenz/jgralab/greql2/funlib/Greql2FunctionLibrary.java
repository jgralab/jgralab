/*
 * JGraLab - The Java Graph Laboratory
 * 
 * Copyright (C) 2006-2010 Institute for Software Technology
 *                         University of Koblenz-Landau, Germany
 *                         ist@uni-koblenz.de
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

package de.uni_koblenz.jgralab.greql2.funlib;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Modifier;
import java.net.URL;
import java.net.URLDecoder;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.Set;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionGroup;

import de.uni_koblenz.ist.utilities.option_handler.OptionHandler;
import de.uni_koblenz.jgralab.JGraLab;
import de.uni_koblenz.jgralab.greql2.exception.DuplicateGreqlFunctionException;
import de.uni_koblenz.jgralab.greql2.funlib.Greql2Function.Category;
import de.uni_koblenz.jgralab.greql2.jvalue.JValueType;

/**
 * This class is the core of the function library. It's implemented following
 * the singleton-pattern. On load, a instance gets created and all functions in
 * the package are read. One can use the methods
 * <code>boolean isGreqlFunction(String name)</code> to ask the Library if a
 * string is a GReQL-function
 * <code>JValue evaluateGreqlFunction(String name, JValue[] arguments)</code> to
 * evaluate the GReQL-function with the given name
 * 
 */
public class Greql2FunctionLibrary {

	private static Logger logger = Logger.getLogger(Greql2FunctionLibrary.class
			.getName());

	/**
	 * this is the package name as greql2.evaluator.funlib
	 */
	private static String packageName;

	/**
	 * this is the package name as greql2/evaluator/funlib
	 */
	private static String nondottedPackageName;

	/**
	 * maps the available functionnames to the objects which implement them
	 */
	private HashMap<String, Greql2Function> availableFunctions = null;

	/**
	 * a reference on the one and only instance of this class
	 */
	private static Greql2FunctionLibrary thisInstance;

	/**
	 * constructs a new instance as soon as the Library gets loaded
	 */
	static {
		logger.setLevel(Level.OFF);
		packageName = Greql2FunctionLibrary.class.getPackage().getName();
		nondottedPackageName = packageName.replace(".", "/");
	}

	/**
	 * creates a new GreqlFunctionLibrary
	 */
	private Greql2FunctionLibrary() {
		availableFunctions = new HashMap<String, Greql2Function>();
	}

	/**
	 * @return The one and only instance of GreqlFunctionLibrary
	 */
	public static Greql2FunctionLibrary instance() {
		if (thisInstance == null) {
			thisInstance = new Greql2FunctionLibrary();
			thisInstance.registerAllFunctions();
		}
		return thisInstance;
	}

	private static CommandLine processCommandLineOptions(String[] args) {
		String toolString = "java "
				+ Greql2FunctionLibrary.class.getSimpleName();
		String versionString = JGraLab.getInfo(false);
		OptionHandler oh = new OptionHandler(toolString, versionString);

		OptionGroup group = new OptionGroup();

		Option listFunctions = new Option("l", "list-functions", false,
				"List all available functions");
		listFunctions.setRequired(true);
		group.addOption(listFunctions);

		Option describeAllFunctions = new Option("a", "describe-all-functions",
				false, "Describe all available functions");
		describeAllFunctions.setRequired(true);
		group.addOption(describeAllFunctions);

		Option greqlRefCard = new Option("g", "greql-ref-card", false,
				"Produce LaTeX longtable output for the GReQL Reference Card");
		greqlRefCard.setRequired(true);
		group.addOption(greqlRefCard);

		Option describeFunction = new Option("d", "describe-function", true,
				"Describe the given function");
		describeFunction.setRequired(true);
		describeFunction.setArgName("functionName");
		group.addOption(describeFunction);

		Option outputOption = new Option("o", "output", true,
				"Specifies the given function.");
		describeFunction.setRequired(false);
		oh.addOption(outputOption);

		Option brieflyDescribeFunction = new Option("b",
				"briefly-describe-function", true,
				"Describe the given function briefly (only one line)");
		brieflyDescribeFunction.setRequired(true);
		brieflyDescribeFunction.setArgName("functionName");
		group.addOption(brieflyDescribeFunction);

		oh.addOptionGroup(group);
		return oh.parse(args);
	}

	public static void main(String[] args) {
		JGraLab.setLogLevel(Level.OFF);
		logger.setLevel(Level.OFF);

		CommandLine cmd = processCommandLineOptions(args);

		String output = null;
		if (cmd.hasOption('l')) {
			output = listFunctions();
		} else if (cmd.hasOption('d')) {
			output = describeFunction(cmd.getOptionValue('d'), false);
		} else if (cmd.hasOption('b')) {
			output = describeFunction(cmd.getOptionValue('b'), true);
		} else if (cmd.hasOption('a')) {
			output = describeAllFunction();
		} else if (cmd.hasOption('g')) {
			output = generateGreqlReferenceCard();
		}

		if (output == null) {
			// FIXME please thrown an exception
			System.out.println("Don't know what to do!");
		}

		if (cmd.hasOption('o')) {
			saveDocumentation(cmd.getOptionValue('o'), output);

		} else {
			System.out.println(output);
		}

	}

	/**
	 * Saves a given string into into a file specified as string.
	 * 
	 * @param filename
	 *            Filename of the output file, specified as string.
	 * @param output
	 *            String to write to a file.
	 */
	private static void saveDocumentation(String filename, String output) {
		FileWriter file = null;
		try {
			file = new FileWriter(new File(filename));
			file.append(output);
			file.flush();
			System.out.println("Fini.");
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				file.close();

			} catch (IOException ex) {
				throw new RuntimeException(
						"An exception occurred while closing the stream.", ex);
			}
		}
	}

	private static String describeAllFunction() {
		StringBuilder sb = new StringBuilder();
		for (String fun : new TreeSet<String>(
				instance().availableFunctions.keySet())) {
			sb.append(describeFunction(fun, false));
			sb.append("\u000C\n");
		}
		return sb.toString();
	}

	private static String listFunctions() {
		StringBuilder sb = new StringBuilder();
		boolean first = true;
		for (String function : new TreeSet<String>(
				Greql2FunctionLibrary.instance().availableFunctions.keySet())) {
			if (!first) {
				sb.append('\n');
			}
			first = false;
			sb.append(describeFunction(function, true));
		}
		return sb.toString();
	}

	private static String describeFunction(String functionName, boolean briefly) {
		Greql2Function fun = instance().availableFunctions.get(functionName);
		if (fun == null) {
			return "`" + functionName + "' is not a known function.";
		}

		Greql2Function afun = fun;

		if (briefly) {
			int end = afun.description.indexOf("\n");
			if (end == -1) {
				return functionName + ": " + afun.description;
			}
			return functionName + ": " + afun.description.substring(0, end);
		}

		StringBuilder sb = new StringBuilder();
		sb.append("Function `");
		String className = afun.getClass().getSimpleName();
		sb.append(className.substring(0, 1).toLowerCase());
		sb.append(className.substring(1));
		sb.append("':\n");
		String cur = sb.toString();
		for (int i = 0; i < cur.length() - 1; i++) {
			sb.append("=");
		}

		sb.append("\n\n");
		sb.append(afun.description);
		sb.append("\n\n");

		sb.append("Signatures:\n");
		sb.append("-----------");
		sb.append("\n\n");
		for (int i = 0; i < afun.signatures.length; i++) {
			boolean first = true;
			sb.append("  ");
			sb.append(i + 1);
			sb.append(". ");
			sb.append(functionName);
			sb.append("(");
			for (int j = 0; j < afun.signatures[i].length - 1; j++) {
				if (!first) {
					sb.append(", ");
				}
				first = false;
				sb.append(afun.signatures[i][j]);
			}
			sb.append(") : ");
			sb.append(afun.signatures[i][afun.signatures[i].length - 1]);
			sb.append('\n');
		}
		sb.append('\n');

		sb.append("Categories: ");
		boolean first = true;
		for (Category category : afun.categories) {
			if (!first) {
				sb.append(", ");
			}
			first = false;
			sb.append(category.toString());
		}
		sb.append("\n-----------\n");
		return sb.toString();
	}

	private static String generateGreqlReferenceCard() {
		SortedMap<Category, SortedSet<Greql2Function>> map = new TreeMap<Category, SortedSet<Greql2Function>>();
		for (Greql2Function fun : instance().availableFunctions.values()) {
			for (Category cat : fun.categories) {
				SortedSet<Greql2Function> set = map.get(cat);
				if (set == null) {
					set = new TreeSet<Greql2Function>(
							new Comparator<Greql2Function>() {
								@Override
								public int compare(Greql2Function o1,
										Greql2Function o2) {
									return o1
											.getClass()
											.getSimpleName()
											.compareTo(
													o2.getClass()
															.getSimpleName());
								}
							});
					map.put(cat, set);
				}
				set.add(fun);
			}
		}

		StringBuilder sb = new StringBuilder();
		// Stands in the Document!
		// sb.append("\\section{Functions}\n\n");
		for (Entry<Category, SortedSet<Greql2Function>> e : map.entrySet()) {
			sb.append("\\subsection{");
			String subSect = e.getKey().toString().toLowerCase()
					.replace("_", " ");
			sb.append(subSect.substring(0, 1).toUpperCase()
					+ subSect.substring(1));
			sb.append("}\n\n");

			sb.append("\\begin{longtable}{|p{0.09\\textwidth}|p{0.56\\textwidth}|p{0.25\\textwidth}|}\n");

			sb.append("\\hline\n");
			sb.append("\\textbf{Name} & \\textbf{Description} & \\textbf{Signatures} \\\\ \n");
			sb.append("\\hline\n");
			sb.append("\\endfirsthead\n");
			sb.append("\\hline\n");
			sb.append("\\textbf{Name} & \\textbf{Description} & \\textbf{Signatures} \\\\ \n");
			sb.append("\\hline\n");
			sb.append("\\endhead\n\n");
			for (Greql2Function fun : e.getValue()) {
				String n = fun.getClass().getSimpleName();
				String funName = n.substring(0, 1).toLowerCase()
						+ n.substring(1);

				sb.append("\\emph{");
				sb.append(funName);
				sb.append("}");
				sb.append(" & ");
				sb.append(fun.description);
				sb.append(" & {\\scriptsize ");

				boolean outerfirst = true;
				for (JValueType[] signature : fun.signatures) {
					if (!outerfirst) {
						sb.append("\\newline ");
					}
					outerfirst = false;
					boolean first = true;
					for (int j = 0; j < signature.length - 1; j++) {
						if (!first) {
							sb.append(" $\\times$ ");
						}
						first = false;
						sb.append(signature[j]);
					}
					sb.append(" $\\rightarrow$ ");
					sb.append(signature[signature.length - 1]);
				}

				sb.append(" } \\\\ \n\\hline\n");
			}
			sb.append("\\end{longtable}\n\n");
		}
		return sb.toString();
	}

	/**
	 * Tests if the given name is a valid GReQL-function
	 * 
	 * @param name
	 *            the name to test
	 * @return true if <code>name</code> is a available GReQL-Function in this
	 *         Library, false otherwise
	 */
	public boolean isGreqlFunction(String name) {
		return availableFunctions.containsKey(name);
	}

	/**
	 * @return a list of all available function names as strings
	 */
	public Set<String> getAvailableGreqlFunctions() {
		return availableFunctions.keySet();
	}

	/**
	 * Returns the Greql2Function with the given name or null if none exists
	 */
	public Greql2Function getGreqlFunction(String name) {
		return availableFunctions.get(name);
	}

	/**
	 * transforms the given className to a valid GReQL Functionname. This means,
	 * the first letter of the className is transformedd to lowercase. So the
	 * className <code>IsSupertype</code> will become the functionName
	 * <code>isSupertype</code>
	 */
	public String toFunctionName(String className) {
		char firstChar = className.charAt(0);
		firstChar = Character.toLowerCase(firstChar);
		className = firstChar + className.substring(1);
		return className;
	}

	/**
	 * registeres the given class as a GReQL-function in this Library
	 * 
	 * @param functionClass
	 *            the class that implements the GReQL function interface
	 */
	public void registerUserDefinedFunction(
			Class<? extends Greql2Function> functionClass)
			throws DuplicateGreqlFunctionException {
		logger.finer("Try to register user defined function: "
				+ functionClass.getName());
		String funName = toFunctionName(functionClass.getSimpleName());
		// The same function may be registered many times, but a function with
		// same name and different class may not. Implementation have to be the
		// same.
		if (isGreqlFunction(funName)
				&& (availableFunctions.get(funName).getClass() != functionClass)) {
			System.out.println(availableFunctions.get(funName) + " != "
					+ functionClass);
			System.exit(1);
			throw new DuplicateGreqlFunctionException(
					"The class "
							+ functionClass.getName()
							+ " can not be registered as GReQL function, there is already a function ");
		}
		try {
			Object o = functionClass.getConstructor().newInstance();
			availableFunctions.put(
					toFunctionName(functionClass.getSimpleName()),
					(Greql2Function) o);
		} catch (Exception ex) {
			throw new RuntimeException(
					"The class "
							+ functionClass.getName()
							+ " has no default constructor and is thus not usable as GReQL function");
		}
	}

	/**
	 * registeres the given class as a GReQL-function in this Library
	 * 
	 * @param className
	 *            the class which implements the greqlFunction
	 */
	private void registerPredefinedFunction(String className) {
		try {
			logger.finer("Try to register function: " + className);
			Class<?> clazz = Class.forName(packageName + "." + className);
			logger.finer("Found Class: " + (clazz != null));
			String funName = toFunctionName(className);
			if (availableFunctions.containsKey(funName)) {
				logger.finer("Skipping doubled registration of " + funName);
				return;
			}
			Class<?> iface = Class
					.forName(packageName + "." + "Greql2Function");
			if (iface.isAssignableFrom(clazz)
					&& !(Modifier.isAbstract(clazz.getModifiers()) || Modifier
							.isInterface(clazz.getModifiers()))) {
				Object o = clazz.getConstructor().newInstance();
				availableFunctions.put(funName, (Greql2Function) o);
			}
		} catch (Exception ex) {
			ex.printStackTrace();
			throw new RuntimeException(
					"Error loading GReQL functions, check if folder with function classes is readable");
		}
	}

	/**
	 * registers all GReQL-functions in the jar-package
	 * 
	 * @return true if a jar-package was found, false otherwise
	 * @param packagePath
	 *            the path to the package this .class-file is located in
	 */
	private boolean registerFunctionsInJar(String packagePath) {
		if (packagePath.lastIndexOf(".jar!/") > 0) {
			// in jar-file
			logger.info("Jar File found");
			packagePath = packagePath
					.substring(0, packagePath.lastIndexOf("!"));
			logger.info("Path of package is: " + packagePath);
			try {
				JarFile jar = new JarFile(packagePath);
				logger.info("Try to read entrys");
				for (Enumeration<JarEntry> e = jar.entries(); e
						.hasMoreElements();) {
					JarEntry je = e.nextElement();
					String entryName = je.getName();
					if (entryName.startsWith(nondottedPackageName)
							&& entryName.endsWith(".class")
							&& Character.isUpperCase(entryName
									.charAt(nondottedPackageName.length() + 1))) {
						registerPredefinedFunction(entryName.substring(
								nondottedPackageName.length() + 1,
								entryName.length() - 6));
						logger.finer("Registering function: "
								+ entryName.substring(
										nondottedPackageName.length() + 1,
										entryName.length() - 6));
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
			return true;
		}
		return false;
	}

	/**
	 * register all GReQL-functions in the directory
	 * 
	 * @return true if the directory contains at least one .class-file, false
	 *         otherwise
	 * @param packagePath
	 *            the path to the package this .class-file is located in
	 */
	public boolean registerFunctionsInDirectory(String fileName) {
		logger.finer("Directory Path : " + fileName);
		boolean foundAClass = false;
		File dir = new File(fileName);
		String[] files = dir.list();
		if (files == null) {
			return foundAClass;
		}
		for (String file : files) {
			if (!file.endsWith(".class")) {
				continue;
			}
			foundAClass = true;
			file = file.substring(0, file.length() - 6);
			registerPredefinedFunction(file);
		}
		return foundAClass;
	}

	/**
	 * registeres all GReQL-Functions in this package. GReQL-Functions are all
	 * classes that implement the Interface GreqlFunction
	 */
	private void registerAllFunctions() {
		logger.finer("Registering all functions");
		availableFunctions = new HashMap<String, Greql2Function>();
		try {
			Enumeration<URL> resources = Greql2FunctionLibrary.class
					.getClassLoader().getResources(nondottedPackageName);
			while (resources.hasMoreElements()) {
				URL res = resources.nextElement();
				// unescape URL
				String fileName = URLDecoder.decode(res.getFile(), "UTF-8");
				if (fileName.contains(".jar!/")) {
					registerFunctionsInJar(fileName.substring(fileName
							.indexOf(':') + 1));
				} else if (res.getProtocol().equals("bundleresource")) {
					registerFunctionsInResourceBundle(res);
				} else {
					registerFunctionsInDirectory(fileName);
				}
			}
		} catch (IOException e1) {
			e1.printStackTrace();
		}
	}

	private void registerFunctionsInResourceBundle(URL res) {
		if (JGraLab.eclipseAdapter != null) {
			JGraLab.eclipseAdapter.registerFunctionsInResourceBundle(res);
		} else {
			throw new RuntimeException(
					"There's no EclipseGreqlFunctionLoader set, so functions "
							+ "cannot be loaded from resource bundles.");
		}
	}
}
