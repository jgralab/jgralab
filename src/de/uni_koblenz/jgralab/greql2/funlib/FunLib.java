package de.uni_koblenz.jgralab.greql2.funlib;

import java.io.File;
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
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.logging.Level;
import java.util.logging.Logger;

import de.uni_koblenz.jgralab.Graph;
import de.uni_koblenz.jgralab.JGraLab;
import de.uni_koblenz.jgralab.greql2.exception.GreqlException;
import de.uni_koblenz.jgralab.greql2.types.Types;
import de.uni_koblenz.jgralab.greql2.types.Undefined;

public class FunLib {
	private static FunLib instance;
	private Logger logger;

	public Logger getLogger() {
		return logger;
	}

	public static FunLib instance() {
		if (instance == null) {
			instance = new FunLib();
		}
		return instance;
	}

	private Map<String, FunctionInfo> functions;

	private static final String packageDirectory = FunLib.class.getPackage()
			.getName().replace(".", "/");

	private FunLib() {
		functions = new HashMap<String, FunctionInfo>();
		logger = JGraLab.getLogger(FunLib.class.getPackage().getName());
		registerAllFunctions();
	}

	private static class Signature {
		Class<?>[] parameterTypes;
		Method evaluateMethod;

		boolean matches(Object[] params) {
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

	private class FunctionInfo {
		Function function;
		Signature[] signatures;

		FunctionInfo(Class<? extends Function> cls) {
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
			registerSignatures(functionSignatures, cls);
			signatures = new Signature[functionSignatures.size()];
			functionSignatures.toArray(this.signatures);
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
	}

	public boolean contains(String name) {
		return functions.containsKey(name);
	}

	private String getFunctionName(String className) {
		return Character.toLowerCase(className.charAt(0))
				+ className.substring(1);
	}

	private String getFunctionName(Class<? extends Function> cls) {
		return getFunctionName(cls.getSimpleName());
	}

	public String getArgumentAsString(Object arg) {
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

	public Object apply(PrintStream os, String name, Object... args) {
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

	public Object apply(String name, Object... args) {
		assert name != null && name.length() >= 1;
		FunctionInfo fi = functions.get(name);
		if (fi == null) {
			throw new GreqlException("Call to unknown function '" + name + "'");
		}
		assert args != null;
		if (!(fi.function instanceof AcceptsUndefinedArguments)) {
			for (Object arg : args) {
				if (arg == null || arg == Undefined.UNDEFINED) {
					return Undefined.UNDEFINED;
				}
			}
		}
		for (Signature sig : fi.signatures) {
			if (sig.matches(args)) {
				try {
					Object result = sig.evaluateMethod
							.invoke(fi.function, args);
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
		sb.append("Function '").append(name)
				.append("' not defined for argument types");
		String delim = " (";
		for (Object arg : args) {
			sb.append(delim).append(Types.getGreqlTypeName(arg));
			delim = ", ";
		}
		sb.append(")");
		throw new GreqlException(sb.toString());
	}

	private void registerAllFunctions() {
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

	private void register(String className) throws ClassNotFoundException {
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

	public void register(Class<? extends Function> cls) {
		int mods = cls.getModifiers();
		if (Modifier.isAbstract(mods) || Modifier.isInterface(mods)
				|| !Modifier.isPublic(mods)) {
			return;
		}
		String name = getFunctionName(cls);
		if (contains(name)) {
			throw new GreqlException("Duplicate function name '" + name + "'");
		}
		if (logger != null) {
			logger.fine("Registering " + cls.getName() + " as '" + name + "'");
		}
		functions.put(name, new FunctionInfo(cls));
	}

	private void registerFunctionsInJar(String packagePath) throws IOException,
			ClassNotFoundException {
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

	public void registerFunctionsInDirectory(String packageName,
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

	private void registerFunctionsInResourceBundle(URL res) {
		throw new UnsupportedOperationException("Not yet implemented.");
	}

	public static void main(String[] args) {
		JGraLab.setLogLevel(Level.FINEST);
		System.out.println("Hi, this is " + FunLib.class);
		instance();
	}

	public Function getFunction(String functionName) {
		FunctionInfo fi = functions.get(functionName);
		return fi != null ? fi.function : null;
	}
}
