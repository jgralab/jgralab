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

package de.uni_koblenz.jgralab.codegenerator;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Vector;
import java.util.logging.Logger;
import java.util.regex.Matcher;

import de.uni_koblenz.jgralab.GraphIOException;

/**
 * TODO add comment
 * 
 * @author ist@uni-koblenz.de
 * 
 */
public abstract class CodeGenerator {

	private static Logger logger = Logger.getLogger(CodeGenerator.class
			.getName());

	/**
	 * toggles, if additional getNextEdge-Methods with a parameter
	 * "noSubclasses" should be generated.
	 */
	public static final boolean CREATE_METHODS_WITH_TYPEFLAG = false;

	protected CodeList rootBlock;

	private ImportCodeSnippet imports;

	protected String schemaRootPackageName;
	
	/**
	 * Toggles, if the generated code supports transactions or not
	 */
	protected boolean transactionSupport;

	/**
	 * Creates a CodeGenerator for a single class
	 * 
	 * @param schemaRootPackageName
	 *            the name of the root package of the schema, for instance
	 *            de.uni_koblenz.jgralab.greql2
	 * @param packageName
	 *            the name of the package the class is located in, for instance
	 *            comprehensions Out of the three parameters, the CodeGenerator
	 *            calculates the name
	 *            schemaRootPackageName.packageName.implementationName, in the
	 *            example
	 *            "de.uni_koblenz.jgralab.greql2.comprehension.Bagcomprehension"
	 *            for the interface and possibly
	 *            schemaRootPackageName.impl.packageName.implementationName, in
	 *            the example
	 *            "de.uni_koblenz.jgralab.greql2.impl.comprehension.Bagcomprehension"
	 *            for the default implementation class
	 * @param transactionSupport
	 *            toggles, if code with transaction support should be generated           
	 */
	public CodeGenerator(String schemaRootPackageName, String packageName, boolean transactionSupport) {
		this.schemaRootPackageName = schemaRootPackageName;
		this.transactionSupport = transactionSupport;
		rootBlock = new CodeList(null);
		rootBlock.setVariable("jgPackage",           "de.uni_koblenz.jgralab");
		rootBlock.setVariable("jgImplPackage",       "de.uni_koblenz.jgralab.impl");
		rootBlock.setVariable("jgImplStdPackage",    "de.uni_koblenz.jgralab.impl.std");
		rootBlock.setVariable("jgImplTransPackage",	 "de.uni_koblenz.jgralab.impl.trans");
		rootBlock.setVariable("jgSchemaPackage",     "de.uni_koblenz.jgralab.schema");
		rootBlock.setVariable("jgSchemaImplPackage", "de.uni_koblenz.jgralab.schema.impl");
		if (packageName != null && !packageName.equals("")) {
			rootBlock.setVariable("schemaPackage", schemaRootPackageName + "."
					+ packageName);
			// schema implementation packages (standard and for transaction)
			rootBlock.setVariable("schemaImplStdPackage", schemaRootPackageName
					+ ".impl.std." + packageName);
			rootBlock.setVariable("schemaImplTransPackage",
					schemaRootPackageName + ".impl.trans." + packageName);
		} else {
			rootBlock.setVariable("schemaPackage", schemaRootPackageName);
			rootBlock.setVariable("schemaImplStdPackage", schemaRootPackageName
					+ ".impl.std");
			rootBlock.setVariable("schemaImplTransPackage",
					schemaRootPackageName + ".impl.trans");
		}
		rootBlock.setVariable("isClassOnly", "false");
		rootBlock.setVariable("isImplementationClassOnly", "false");
		rootBlock.setVariable("isAbstractClass", "false");
		imports = new ImportCodeSnippet();
	}

	protected abstract CodeBlock createHeader(boolean createClass);

	protected abstract CodeBlock createBody(boolean createClass);

	protected CodeBlock createFooter(boolean createClass) {
		return new CodeSnippet(true, "}");
	}

	public static CodeBlock createDisclaimer() {
		return new CodeSnippet("/*",
				" * This code was generated automatically.",
				" * Do NOT edit this file, changes will be lost.",
				" * Instead, change and commit the underlying schema.", " */");
	}

	/**
	 * writes the source code to location path+fileName,
	 * 
	 * @param pathPrefix
	 *            the path where the java source code is to be
	 * @param fileName
	 *            the filename of the java source code including .java
	 * @throws GraphIOException
	 */
	public void writeCodeToFile(String pathPrefix, String fileName,
			String aPackage) throws GraphIOException {
		aPackage = aPackage.replace(".", File.separator);

		File dir = new File(pathPrefix + aPackage);

		if (dir.exists()) {
			if (!dir.isDirectory()) {
				throw new GraphIOException("'" + dir.getAbsolutePath()
						+ "' exists but is not a directory");
			}
		} else {
			dir.mkdirs();
		}

		File outputFile = null;
		try {
			outputFile = new File(dir.getAbsolutePath() + File.separator
					+ fileName);
			BufferedWriter bw = new BufferedWriter(new FileWriter(outputFile));
			bw.write(rootBlock.getCode());
			bw.close();
		} catch (IOException e) {
			throw new GraphIOException("Unable to create file "
					+ outputFile.getAbsolutePath(), e);
		}
	}

	public void createFiles(String pathPrefix) throws GraphIOException {
		// String className = rootBlock.getVariable("className");
		String simpleClassName = rootBlock.getVariable("simpleClassName");
		String schemaPackage = rootBlock.getVariable("schemaPackage");
		String simpleImplClassName = rootBlock
				.getVariable("simpleImplClassName");
		String schemaImplPackage = "";
		if (!transactionSupport)
			schemaImplPackage = rootBlock.getVariable("schemaImplStdPackage");
		else
			schemaImplPackage = rootBlock.getVariable("schemaImplTransPackage");
		logger.finer("createFiles(\"" + pathPrefix + "\")");
		logger.finer(" - simpleClassName=" + simpleClassName);
		logger.finer(" - schemaPackage=" + schemaPackage);
		logger.finer(" - simpleImplClassName=" + simpleImplClassName);
		if (!transactionSupport)
			logger.finer(" - schemaImplStdPackage=" + schemaImplPackage);
		else
			logger.finer(" - schemaImplTransPackage=" + schemaImplPackage);
		
		if (rootBlock.getVariable("isClassOnly").equals("true")) {
			// no separate implementaion
			// create class only
			createCode(true);
			writeCodeToFile(pathPrefix, simpleClassName + ".java",
					schemaPackage);
		} else if (rootBlock.getVariable("isAbstractClass").equals("true")) {
			logger.finer("Creating interface for class: " + simpleClassName);
			logger.finer("Writing file to: " + pathPrefix + "/"
							+ schemaPackage);
			// create interface only
			createCode(false);
			writeCodeToFile(pathPrefix, simpleClassName + ".java",
					schemaPackage);
		} else {
			if (!rootBlock.getVariable("isImplementationClassOnly").equals(
					"true")) {
				// create interface
				createCode(false);
				writeCodeToFile(pathPrefix, simpleClassName + ".java",
						schemaPackage);
			}
			// create implementation
			rootBlock.clear();
			createCode(true);
			writeCodeToFile(pathPrefix, simpleImplClassName + ".java",
					schemaImplPackage);
		}
	}

	/**
	 * creates the generated code string for a class
	 */
	public void createCode(boolean createClass) {
		imports.clear();
		rootBlock.clear();
		rootBlock.addNoIndent(createDisclaimer());
		rootBlock.addNoIndent(createPackageDeclaration(createClass));
		CodeBlock header = createHeader(createClass);
		CodeBlock body = createBody(createClass);
		CodeBlock footer = createFooter(createClass);
		rootBlock.addNoIndent(imports);
		rootBlock.addNoIndent(header);
		rootBlock.addNoIndent(body);
		rootBlock.addNoIndent(footer);
	}

	protected CodeBlock createPackageDeclaration(boolean createClass) {
		CodeSnippet code = new CodeSnippet(true);

		if (rootBlock.getVariable("isClassOnly").equals("true")) {
			code.add("package #schemaPackage#;");
		} else {
			// package declaration standard vs. transaction
			if (!transactionSupport)
				code.add(createClass ? "package #schemaImplStdPackage#;"
						: "package #schemaPackage#;");
			else
				code.add(createClass ? "package #schemaImplTransPackage#;"
						: "package #schemaPackage#;");
		}
		return code;
	}

	protected void addImports(String... importPackages) {
		imports.add(importPackages);
	}

	/**
	 * Transforms the given String into a CamelCase String
	 */
	public static String camelCase(String aString) {
		if (aString.length() < 1) {
			return aString;
		}
		if (aString.length() < 2) {
			return aString.toUpperCase();
		}
		return aString.substring(0, 1).toUpperCase() + aString.substring(1);
	}

	/**
	 * @param aString
	 *            some String
	 * @return the string with " quoted as \"
	 */
	public static String stringQuote(String aString) {
		return aString.replaceAll("\"", Matcher.quoteReplacement("\\\""));
	}

	/**
	 * Returns {@code JavaSourceFromString}s from the generated code.
	 * 
	 * @return a Vector of {@code JavaSourceFromString}s from the generated code
	 */
	public Vector<JavaSourceFromString> createJavaSources() {
		String className = rootBlock.getVariable("simpleClassName");
		String implClassName = rootBlock.getVariable("simpleImplClassName");
		Vector<JavaSourceFromString> javaSources = new Vector<JavaSourceFromString>(
				2);

		if (rootBlock.getVariable("isClassOnly").equals("true")) {
			// no separate implementaion
			// create class only
			createCode(true);
			javaSources.add(new JavaSourceFromString(className, rootBlock
					.getCode()));
		} else if (rootBlock.getVariable("isAbstractClass").equals("true")) {
			// create interface only
			createCode(false);
			javaSources.add(new JavaSourceFromString(className, rootBlock
					.getCode()));
		} else {
			if (!rootBlock.getVariable("isImplementationClassOnly").equals(
					"true")) {
				// create interface
				createCode(false);
				javaSources.add(new JavaSourceFromString(className, rootBlock
						.getCode()));
			}
			// create implementation
			rootBlock.clear();
			createCode(true);
			javaSources.add(new JavaSourceFromString(implClassName, rootBlock
					.getCode()));
		}

		return javaSources;
	}
}
