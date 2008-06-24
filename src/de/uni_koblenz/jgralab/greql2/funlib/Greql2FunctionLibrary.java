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

package de.uni_koblenz.jgralab.greql2.funlib;

import java.io.File;
import java.net.URL;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import de.uni_koblenz.jgralab.greql2.evaluator.GreqlEvaluator;

/**
 * This class is the core of the function libary. It's implemented following the
 * singleton-pattern. On load, a instance gets created and all functions in the
 * package are read. One can use the methods
 * <code>boolean isGreqlFunction(String name)</code> to ask the Library if a
 * string is a greql-function
 * <code>JValue evaluateGreqlFunction(String name, JValue[] arguments)</code>
 * to evaluate the greql-function with the given name
 * 
 */
public class Greql2FunctionLibrary {

	/**
	 * toggles debug output on or off
	 */
	private static final boolean DEBUG = false;

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
		packageName = Greql2FunctionLibrary.class.getPackage().getName();
		if (DEBUG)
			GreqlEvaluator.println("Packagename : " + packageName);
		nondottedPackageName = packageName.replace(".", "/");
		if (DEBUG)
			GreqlEvaluator.println("Nondotted Package name "
					+ nondottedPackageName);
		thisInstance = new Greql2FunctionLibrary();
		if (DEBUG)
			GreqlEvaluator.println("FunctionLibrary successfull loaded");
	}

	/**
	 * creates a new GreqlFunctionLibrary
	 */
	public Greql2FunctionLibrary() {
		// GreqlEvaluator.println("Creating a new GreqlFunctionLibrary");
		availableFunctions = new HashMap<String, Greql2Function>();
		registerAllFunctions();
	}

	/**
	 * @return The one and only instance of GreqlFunctionLibrary
	 */
	public static Greql2FunctionLibrary instance() {
		return thisInstance;
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
	 * @param className
	 *            the class which implements the greqlFunction
	 */
	@SuppressWarnings("unchecked")
	private void registerFunction(String className) {
		try {
			if (DEBUG) {
				GreqlEvaluator
						.println("Try to register function: " + className);
				GreqlEvaluator
						.println("Found Class: "
								+ (Class.forName(packageName + "." + className) != null));
			}
			Class[] interfaces = Class.forName(packageName + "." + className)
					.getInterfaces();
			String funIntName = packageName + ".Greql2Function";
			for (int i = 0; i < interfaces.length; i++) {
				if (DEBUG)
					GreqlEvaluator.println("Implementing interface "
							+ interfaces[i].getName());
				if (interfaces[i].getName().equals(funIntName)) {
					Object o = Class.forName(packageName + "." + className)
							.getConstructor().newInstance();
					availableFunctions.put(toFunctionName(className),
							(Greql2Function) o);
				}
			}
		} catch (Exception ce) {
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
			GreqlEvaluator.println("Jar File found");
			packagePath = packagePath
					.substring(0, packagePath.lastIndexOf("!"));
			GreqlEvaluator.println("Path of package is: " + packagePath);
			try {
				JarFile jar = new JarFile(packagePath);
				GreqlEvaluator.println("Try to read entrys");
				for (Enumeration<JarEntry> e = jar.entries(); e
						.hasMoreElements();) {
					JarEntry je = e.nextElement();
					String entryName = je.getName();
					if (DEBUG && entryName.contains("funlib"))
						GreqlEvaluator.println("Reading entry " + entryName);
					if (entryName.startsWith(nondottedPackageName)
							&& entryName.endsWith(".class")) {
						registerFunction(entryName.substring(
								nondottedPackageName.length() + 1, entryName
										.length() - 6));
						if (DEBUG)
							GreqlEvaluator.println("Registering function: "
									+ entryName.substring(nondottedPackageName
											.length() + 1,
											entryName.length() - 6));
					}
				}
			} catch (Exception e) {
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
	private boolean registerFunctionsInDirectory(String packagePath) {
		packagePath = packagePath.replaceAll("%20", " ");
		if (DEBUG)
			GreqlEvaluator.println("Directory Path : " + packagePath);
		String entries[] = new File(packagePath).list();
		int i = 0;
		for (i = 0; i < entries.length; i++) {
			// GreqlEvaluator.println("Entriename " + entries[i]);
			if (entries[i].endsWith(".class")) {
				String className = entries[i].substring(0,
						entries[i].length() - 6);
				// GreqlEvaluator.println("Try to register function : " +
				// className);
				registerFunction(className);
			}
		}
		if (i > 0)
			return true;
		else
			return false;
	}

	/**
	 * registeres all GReQL-Functions in this package. GReQL-Functions are all
	 * classes that implement the Interface GreqlFunction
	 * 
	 * @return true if the package was successfull read
	 */
	private boolean registerAllFunctions() {
		if (DEBUG)
			GreqlEvaluator.println("Registering all functions");
		availableFunctions = new HashMap<String, Greql2Function>();
		String thisClassName = this.getClass().getCanonicalName();
		if (DEBUG)
			GreqlEvaluator.println("Functionlib name: " + thisClassName);
		URL packageUrl = Greql2FunctionLibrary.class.getResource("/"
				+ nondottedPackageName + "/Greql2FunctionLibrary.class");
		if (packageUrl != null) {
			if (DEBUG) {
				GreqlEvaluator.println("Found Greql2FunctionLibrary");
				GreqlEvaluator.println("URL : " + packageUrl.getPath());
			}
			String packagePath = packageUrl.getPath();
			packagePath = packagePath
					.substring(0, packagePath.lastIndexOf("/"));
			// GreqlEvaluator.println("Path : " + packagePath);

			if (!packagePath.startsWith("/")) {
				// stripp leading file://
				packagePath = packagePath.substring(packagePath.indexOf("/"));
				// GreqlEvaluator.println("Stripped path : " + packagePath);
			}
			if (registerFunctionsInJar(packagePath))
				return true;
			if (registerFunctionsInDirectory(packagePath))
				return true;
		}
		return false;
	}

}
