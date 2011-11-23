package de.uni_koblenz.jgralab.greql2.funlib;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.net.URL;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.logging.Logger;

import de.uni_koblenz.jgralab.Graph;
import de.uni_koblenz.jgralab.JGraLab;
import de.uni_koblenz.jgralab.greql2.exception.GreqlException;
import de.uni_koblenz.jgralab.greql2.funlib.Function.Category;
import de.uni_koblenz.jgralab.greql2.types.Types;
import de.uni_koblenz.jgralab.greql2.types.Undefined;

public class FunLib {
	private static final String packageDirectory = FunLib.class.getPackage()
			.getName().replace(".", "/");

	static {
		functions = new HashMap<String, FunctionInfo>();
		logger = JGraLab.getLogger(FunLib.class.getPackage().getName());
		registerAllFunctions();
	}

	private FunLib() {
	}

	private static Map<String, FunctionInfo> functions;
	private static Logger logger;

	private static class Signature {
		Class<?>[] parameterTypes;
		Method evaluateMethod;

		final boolean matches(Object[] params) {
			if (params.length != parameterTypes.length) {
				return false;
			}
			for (int i = 0; i < params.length; i++) {
				if (!parameterTypes[i].isInstance(params[i])) {
					return false;
				}
			}
			return true;
		}
	}

	public static class FunctionInfo {
		String name;
		Class<? extends Function> functionClass;
		Function function;
		Signature[] signatures;
		boolean needsGraphArgument;
		boolean acceptsUndefinedValues;

		FunctionInfo(String name, Class<? extends Function> cls) {
			this.name = name;
			functionClass = cls;
			ArrayList<Signature> functionSignatures = new ArrayList<Signature>();
			try {
				function = cls.newInstance();
			} catch (InstantiationException e) {
				throw new GreqlException("Could not instantiate '"
						+ cls.getName() + "'", e);
			} catch (IllegalAccessException e) {
				throw new GreqlException(
						"Could not instantiate '"
								+ cls.getName()
								+ "' (class must be public and needs public default constructor)",
						e);
			}
			needsGraphArgument = cls
					.isAnnotationPresent(NeedsGraphArgument.class);
			acceptsUndefinedValues = cls
					.isAnnotationPresent(AcceptsUndefinedArguments.class);
			registerSignatures(functionSignatures, cls);
			signatures = new Signature[functionSignatures.size()];
			functionSignatures.toArray(signatures);
		}

		void registerSignatures(ArrayList<Signature> signatures,
				Class<? extends Function> cls) {
			for (Method m : cls.getMethods()) {
				if (Modifier.isPublic(m.getModifiers())
						&& !Modifier.isAbstract(m.getModifiers())
						&& m.getName().equals("evaluate")) {
					if (logger != null) {
						logger.finest("\t" + m);
					}
					Signature sig = new Signature();
					sig.evaluateMethod = m;
					sig.parameterTypes = m.getParameterTypes();
					signatures.add(sig);
				}
			}
		}

		public final Function getFunction() {
			return function;
		}

		public final boolean needsGraphArgument() {
			return needsGraphArgument;
		}

		public final boolean acceptsUndefinedValues() {
			return acceptsUndefinedValues;
		}
	}

	public static final boolean contains(String name) {
		return functions.containsKey(name);
	}

	private static final String getFunctionName(String className) {
		return Character.toLowerCase(className.charAt(0))
				+ className.substring(1);
	}

	private static final String getFunctionName(Class<? extends Function> cls) {
		return getFunctionName(cls.getSimpleName());
	}

	public static final String getArgumentAsString(Object arg) {
		if (arg == null) {
			arg = Undefined.UNDEFINED;
		}
		StringBuilder sb = new StringBuilder();
		sb.append(Types.getGreqlTypeName(arg));
		if (arg instanceof String) {
			sb.append(": ").append('"')
					.append(arg.toString().replace("\"", "\\\"")).append('"');
		} else if (!(arg instanceof Graph) && !(arg instanceof Undefined)) {
			sb.append(": ").append(arg);
		}
		return sb.toString();
	}

	public static final Object apply(PrintStream os, String name,
			Object... args) {
		assert (name != null) && (name.length() >= 1);
		assert args != null;
		assert validArgumentTypes(args);
		StringBuilder sb = new StringBuilder();
		sb.append(name);
		if (args.length == 0) {
			sb.append("()");
		} else {
			String delim = "(";
			for (Object arg : args) {
				sb.append(delim).append(getArgumentAsString(arg));
				delim = ", ";
			}
			sb.append(")");
		}
		os.print(sb);
		Object result = apply(name, args);
		os.println(" -> " + getArgumentAsString(result));
		return result;
	}

	public static final Object apply(FunctionInfo fi, Object... args) {
		assert fi != null;
		if (!fi.acceptsUndefinedValues) {
			for (Object arg : args) {
				if ((arg == null) || (arg == Undefined.UNDEFINED)) {
					return Undefined.UNDEFINED;
				}
			}
		}
		for (Signature sig : fi.signatures) {
			if (sig.matches(args)) {
				try {
					Object result = sig.evaluateMethod
							.invoke(fi.function, args);
					assert Types.isValidGreqlValue(result);
					return result == null ? Undefined.UNDEFINED : result;
				} catch (IllegalArgumentException e) {
					throw new GreqlException(e.getMessage(), e.getCause());
				} catch (IllegalAccessException e) {
					throw new GreqlException(e.getMessage(), e.getCause());
				} catch (InvocationTargetException e) {
					throw new GreqlException(e.getMessage(), e.getCause());
				}
			}
		}
		StringBuilder sb = new StringBuilder();
		sb.append("Function '").append(fi.name)
				.append("' not defined for argument types (");
		String delim = "";
		for (Object arg : args) {
			sb.append(delim).append(Types.getGreqlTypeName(arg));
			delim = ", ";
		}
		sb.append(")");
		throw new GreqlException(sb.toString());
	}

	public static final Object apply(String name, Object... args) {
		assert (name != null) && (name.length() >= 1);
		assert args != null;
		assert validArgumentTypes(args);
		FunctionInfo fi = getFunctionInfo(name);
		if (fi == null) {
			throw new GreqlException("Call to unknown function '" + name + "'");
		}
		return apply(fi, args);
	}

	private static final boolean validArgumentTypes(Object[] args) {
		for (Object arg : args) {
			if (!Types.isValidGreqlValue(arg)) {
				throw new GreqlException("Type unknown to GReQL: "
						+ arg.getClass().getName() + ", value: " + arg);
			}
		}
		return true;
	}

	private static void registerAllFunctions() {
		try {
			Enumeration<URL> resources = FunLib.class.getClassLoader()
					.getResources(packageDirectory);
			while (resources.hasMoreElements()) {
				URL res = resources.nextElement();
				// unescape URL
				String fileName = URLDecoder.decode(res.getFile(), "UTF-8");
				if (fileName.contains(".jar!/")) {
					String jarName = fileName
							.substring(fileName.indexOf(':') + 1);
					if (logger != null) {
						logger.fine("registerFunctionsInJar(\"" + jarName
								+ "\")");
					}
					registerFunctionsInJar(jarName);
				} else if (res.getProtocol().equals("bundleresource")) {
					if (logger != null) {
						logger.fine("registerFunctionsInResourceBundle(...)");
					}
					registerFunctionsInResourceBundle(res);
				} else {
					if (logger != null) {
						logger.fine("registerFunctionsInDirectory(\""
								+ fileName + "\")");
					}
					registerFunctionsInDirectory(FunLib.class.getPackage()
							.getName(), fileName);
				}
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private static void register(String className)
			throws ClassNotFoundException {
		if (logger != null) {
			logger.finest("Loading class " + className);
		}
		Class<?> cls = Class.forName(className);
		if (Function.class.isAssignableFrom(cls)) {
			@SuppressWarnings("unchecked")
			Class<? extends Function> fun = (Class<? extends Function>) cls;
			register(fun);
		}
	}

	public static final void register(Class<? extends Function> cls) {
		int mods = cls.getModifiers();
		if (Modifier.isAbstract(mods) || Modifier.isInterface(mods)
				|| !Modifier.isPublic(mods)) {
			return;
		}
		String name = getFunctionName(cls);
		FunctionInfo fn = functions.get(name);
		if (fn != null) {
			if (fn.functionClass == cls) {
				// ok, same class is already registered
				return;
			}
			throw new GreqlException("Duplicate function name '" + name + "'");
		}
		if (logger != null) {
			logger.fine("Registering " + cls.getName() + " as '" + name + "'");
		}
		functions.put(name, new FunctionInfo(name, cls));
	}

	private static void registerFunctionsInJar(String packagePath)
			throws IOException, ClassNotFoundException {
		if (packagePath.lastIndexOf(".jar!/") > 0) {
			packagePath = packagePath
					.substring(0, packagePath.lastIndexOf("!"));
			JarFile jar = new JarFile(packagePath);
			for (Enumeration<JarEntry> e = jar.entries(); e.hasMoreElements();) {
				JarEntry je = e.nextElement();
				String entryName = je.getName();
				if (entryName.startsWith(packageDirectory)
						&& entryName.endsWith(".class")) {
					String className = entryName.substring(0,
							entryName.length() - 6).replace("/", ".");
					register(className);
				}
			}
		}
	}

	private static void registerFunctionsInDirectory(String packageName,
			String directoryName) throws ClassNotFoundException {
		File dir = new File(directoryName);
		File[] files = dir.listFiles();
		for (File file : files) {
			if (file.isDirectory()) {
				registerFunctionsInDirectory(
						packageName + "." + file.getName(),
						file.getAbsolutePath());
			}
			if (!file.getName().endsWith(".class")) {
				continue;
			}
			register(packageName + "."
					+ file.getName().substring(0, file.getName().length() - 6));
		}
	}

	private static void registerFunctionsInResourceBundle(URL res) {
		throw new UnsupportedOperationException("Not yet implemented.");
	}

	public static final FunctionInfo getFunctionInfo(String functionName) {
		return functions.get(functionName);
	}

	public static final Logger getLogger() {
		return logger;
	}

	public static void generateLaTeXFunctionDocs(String fileName)
			throws IOException {
		LaTeXFunctionDocsGenerator docGen = new LaTeXFunctionDocsGenerator(
				fileName, functions);
		docGen.generate();
	}

	private static class LaTeXFunctionDocsGenerator {
		private BufferedWriter bw;
		private final Map<Category, SortedMap<String, FunctionInfo>> cat2funs = new HashMap<Function.Category, SortedMap<String, FunctionInfo>>();

		LaTeXFunctionDocsGenerator(String fileName,
				final Map<String, FunctionInfo> funs) throws IOException {
			bw = new BufferedWriter(new FileWriter(fileName));
			fillCat2Funs(funs);
		}

		private void fillCat2Funs(final Map<String, FunctionInfo> funs) {
			for (Entry<String, FunctionInfo> e : funs.entrySet()) {
				for (Category cat : e.getValue().getFunction().getCategories()) {
					SortedMap<String, FunctionInfo> m = cat2funs.get(cat);
					if (m == null) {
						m = new TreeMap<String, FunctionInfo>();
						cat2funs.put(cat, m);
					}
					m.put(e.getKey(), e.getValue());
				}
			}
		}

		/**
		 * Set to true to generate a complete latex doc that can be compiled
		 * standalone. Useful when changing this generator...
		 */
		private boolean STANDALONE = false;

		void generate() throws IOException {
			try {
				if (STANDALONE) {
					write("\\documentclass{article}");
					newLine();
					write("\\begin{document}");
					newLine();
				}
				for (Category cat : Category.values()) {
					if (cat2funs.get(cat) != null) {
						generateCategoryDocs(cat);
					}
				}
				if (STANDALONE) {
					write("\\end{document}");
					newLine();
				}
			} finally {
				bw.close();
			}
		}

		private void write(String... strings) throws IOException {
			for (String s : strings) {
				bw.write(s);
			}
		}

		private void newLine() throws IOException {
			bw.newLine();
		}

		private void generateCategoryDocs(Category cat) throws IOException {
			String heading = cat.toString().toLowerCase().replace('_', ' ');
			heading = heading.substring(0, 1).toUpperCase()
					.concat(heading.substring(1));
			newLine();
			write("\\subsection{" + heading + "}");
			newLine();

			SortedMap<String, FunctionInfo> funs = cat2funs.get(cat);
			for (Entry<String, FunctionInfo> e : funs.entrySet()) {
				generateFunctionDocs(e.getKey(), e.getValue());
			}
		}

		private void generateFunctionDocs(String name, FunctionInfo info)
				throws IOException {
			newLine();
			write("\\paragraph*{" + name + ".}");
			newLine();
			write(info.function.getDescription());
			newLine();

			generateSignatures(name, info.signatures);

			newLine();
		}

		private void generateSignatures(String name, Signature[] signatures)
				throws IOException {
			write("\\begin{itemize}");

			for (Signature sig : signatures) {
				write("\\item $" + name + ": ");
				for (int i = 0; i < sig.parameterTypes.length; i++) {
					if (i != 0) {
						write(" \\times ");
					}
					write(Types.getGreqlTypeName(sig.parameterTypes[i]));
				}
				write(" \\longrightarrow ");
				write(Types
						.getGreqlTypeName(sig.evaluateMethod.getReturnType()));
				write("$");
			}

			write("\\end{itemize}");
		}
	}

	public static void main(String[] args) throws IOException {
		if (args.length != 1) {
			System.out
					.println("Generate a LaTeX documentation for all known GReQL functions.");
			System.out.println("Usage: java FunLib /path/to/fundocs.tex");
		} else {
			generateLaTeXFunctionDocs(args[0]);
		}
	}
}
