/*
 * JGraLab - The Java graph laboratory
 * (c) 2006-2008 Institute for Software Technology
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
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLDecoder;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.logging.Logger;

import de.uni_koblenz.jgralab.greql2.exception.DuplicateGreqlFunctionException;

/**
 * This class is the core of the function libary. It's implemented following the
 * singleton-pattern. On load, a instance gets created and all functions in the
 * package are read. One can use the methods
 * <code>boolean isGreqlFunction(String name)</code> to ask the Library if a
 * string is a greql-function
 * <code>JValue evaluateGreqlFunction(String name, JValue[] arguments)</code> to
 * evaluate the greql-function with the given name
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
		packageName = Greql2FunctionLibrary.class.getPackage().getName();
		logger.finer("Packagename : " + packageName);
		nondottedPackageName = packageName.replace(".", "/");
		logger.finer("Nondotted Package name " + nondottedPackageName);
		thisInstance = new Greql2FunctionLibrary();
		logger.finer("FunctionLibrary successfull loaded");
	}

	/**
	 * creates a new GreqlFunctionLibrary
	 */
	public Greql2FunctionLibrary() throws RuntimeException {
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
	 * @param functionClass the class that implements the GReQL function 
	 */
	@SuppressWarnings("unchecked")
	public void registerUserDefinedFunction(Class<? extends Greql2Function> functionClass) throws DuplicateGreqlFunctionException {
			logger.finer("Try to register user defined function: " + functionClass.getName());
			if (isGreqlFunction(toFunctionName(functionClass.getSimpleName())))
				throw new DuplicateGreqlFunctionException("The class " + functionClass.getName() + " can not be registered as GReQL function, there is already a function ");
			Class[] interfaces = functionClass.getInterfaces();
			String funIntName = packageName + ".Greql2Function";
			for (int i = 0; i < interfaces.length; i++) {
				logger.finer("Implementing interface "
						+ interfaces[i].getName());
				if (interfaces[i].getName().equals(funIntName)) {
					try {
						Object o = functionClass.getConstructor().newInstance();
						availableFunctions.put(
							toFunctionName(functionClass.getSimpleName()),
							(Greql2Function) o);
					} catch (Exception ex) {
						throw new RuntimeException("The class " + functionClass.getName() + " has no default constructor and is thus not usable as GReQL function");
					}
				}
			}
	}
	
	/**
	 * registeres the given class as a GReQL-function in this Library
	 * 
	 * @param className
	 *            the class which implements the greqlFunction
	 */
	@SuppressWarnings("unchecked")
	private void registerPredefinedFunction(String className) {
		try {
			logger.finer("Try to register function: " + className);
			logger.finer("Found Class: "
					+ (Class.forName(packageName + "." + className) != null));

			Class[] interfaces = Class.forName(packageName + "." + className)
					.getInterfaces();
			String funIntName = packageName + ".Greql2Function";
			for (int i = 0; i < interfaces.length; i++) {
				logger.finer("Implementing interface "
						+ interfaces[i].getName());
				if (interfaces[i].getName().equals(funIntName)) {
					Object o = Class.forName(packageName + "." + className)
							.getConstructor().newInstance();
					availableFunctions.put(toFunctionName(className),
							(Greql2Function) o);
				}
			}
		} catch (Exception ex) {
			throw new RuntimeException("Error loading GReQL functions, check if folder with function classes is readable");
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
					if (entryName.contains("funlib") && !entryName.contains("funlib/pathsearch"))
						logger.finer("Reading entry " + entryName);
					if (entryName.startsWith(nondottedPackageName)
							&& entryName.endsWith(".class")
							&& Character.isUpperCase(entryName.charAt(nondottedPackageName.length() + 1))) {
						registerPredefinedFunction(entryName.substring(
								nondottedPackageName.length() + 1, entryName
										.length() - 6));
						logger.finer("Registering function: "
								+ entryName.substring(nondottedPackageName
										.length() + 1, entryName.length() - 6));
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
	private boolean registerFunctionsInDirectory(String packagePath) {
		packagePath = packagePath.replaceAll("%20", " ");
		logger.finer("Directory Path : " + packagePath);
		String entries[] = new File(packagePath).list();
		int i = 0;
		for (i = 0; i < entries.length; i++) {
			if (entries[i].endsWith(".class")) {
				String className = entries[i].substring(0,
						entries[i].length() - 6);
				registerPredefinedFunction(className);
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
		logger.finer("Registering all functions");
		availableFunctions = new HashMap<String, Greql2Function>();
		String thisClassName = this.getClass().getCanonicalName();
		logger.finer("Functionlib name: " + thisClassName);
		URL packageUrl = Greql2FunctionLibrary.class.getResource("/"
				+ nondottedPackageName + "/Greql2FunctionLibrary.class");
		if (packageUrl != null) {

			logger.finer("Found Greql2FunctionLibrary");
			logger.finer("URL : " + packageUrl.getPath());

			String packagePath = null;
			
			try {
				packagePath = URLDecoder.decode(packageUrl.getPath(), "UTF-8");
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			}
			
			packagePath = packagePath
					.substring(0, packagePath.lastIndexOf("/"));

			if (!packagePath.startsWith("/")) {
				// stripp leading file://
				packagePath = packagePath.substring(packagePath.indexOf("/"));
			}
			if (registerFunctionsInJar(packagePath))
				return true;
			if (registerFunctionsInDirectory(packagePath))
				return true;
		}
		return false;
	}

}
