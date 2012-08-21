/*
 * JGraLab - The Java Graph Laboratory
 *
 * Copyright (C) 2006-2012 Institute for Software Technology
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

package de.uni_koblenz.jgralab;

import java.io.IOException;
import java.net.URL;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.jar.Attributes;
import java.util.jar.Manifest;
import java.util.logging.ConsoleHandler;
import java.util.logging.Formatter;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

import org.pcollections.ArrayPMap;
import org.pcollections.ArrayPSet;
import org.pcollections.ArrayPVector;
import org.pcollections.PMap;
import org.pcollections.PSet;
import org.pcollections.PVector;

/**
 * This class gives information, which software was used creating JGraLab.
 * 
 * @author ist@uni-koblenz.de
 */
public class JGraLab {
	/**
	 * Used by the jgralab4eclipse plugin to manage class loading (GReQL
	 * functions and compiled schema classes) inside eclipse.
	 */
	private static EclipseAdapter eclipseAdapter;

	private static String version;
	private static String codename;

	// read revision and version from the manifest
	private static void readVersionFromManifest() {
		version = codename = "unknown";
		if (eclipseAdapter != null) {
			version = eclipseAdapter.getJGraLabVersion();
			codename = eclipseAdapter.getJGraLabCodename();
		} else {
			// read info from jar manifest
			try {
				Enumeration<URL> resources = JGraLab.class.getClassLoader()
						.getResources("META-INF/MANIFEST.MF");

				extractVersionInfo(resources);

				if (version.equals("unknown") || codename.equals("unknown")) {
					resources = JGraLab.class.getClassLoader().getResources(
							"MANIFEST.MF");
					extractVersionInfo(resources);
					if (version.equals("unknown") || codename.equals("unknown")) {
						getRootLogger().warning("MANIFEST.MF not found.");
					}
				}

			} catch (IOException e) {
			}
		}
	}

	private static void extractVersionInfo(Enumeration<URL> resources)
			throws IOException {
		while (resources.hasMoreElements()) {
			URL url = resources.nextElement();
			Manifest manifest = new Manifest(url.openStream());
			Map<String, Attributes> entries = manifest.getEntries();
			Attributes info = entries.get("de/uni_koblenz/jgralab/");
			if (info == null) {
				continue;
			}
			String implTitle = info.getValue("Implementation-Title");
			if (implTitle.equals("JGraLab")) {
				String[] versionString = info
						.getValue("Implementation-Version").split("@");
				version = versionString[0];
				codename = versionString[1];
			}
		}
	}

	private static String getVersion() {
		if (version == null) {
			readVersionFromManifest();
		}
		return version;
	}

	private static String getCodename() {
		if (codename == null) {
			readVersionFromManifest();
		}
		return codename;
	}

	private static final String[] versionInfo = {
			"JGraLab - The Java graph laboratory", "  Version : $ver",
			"  Codename: $codename" };

	private static final String[] copyrightInfo = {
			"JGraLab - The Java Graph Laboratory",
			"",
			"Copyright (C) 2006-2012 Institute for Software Technology",
			"                        University of Koblenz-Landau, Germany",
			"                        ist@uni-koblenz.de",
			"",
			"For bug reports, documentation and further information, visit",
			"",
			"                        https://github.com/jgralab/jgralab",
			"",
			"This program is free software; you can redistribute it and/or modify it",
			"under the terms of the GNU General Public License as published by the",
			"Free Software Foundation; either version 3 of the License, or (at your",
			"option) any later version.",
			"",
			"This program is distributed in the hope that it will be useful, but",
			"WITHOUT ANY WARRANTY; without even the implied warranty of",
			"MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General",
			"Public License for more details.",
			"",
			"You should have received a copy of the GNU General Public License along",
			"with this program; if not, see <http://www.gnu.org/licenses>.",
			"",
			"Additional permission under GNU GPL version 3 section 7",
			"",
			"If you modify this Program, or any covered work, by linking or combining",
			"it with Eclipse (or a modified version of that program or an Eclipse",
			"plugin), containing parts covered by the terms of the Eclipse Public",
			"License (EPL), the licensors of this Program grant you additional",
			"permission to convey the resulting work.  Corresponding Source for a",
			"non-source form of such a combination shall include the source code for",
			"the parts of JGraLab used as well as that of the covered work.",
			"", "This software uses:", "", "Apache Commons CLI 1.2",
			"Copyright 2001-2009 The Apache Software Foundation" };

	private static HashMap<String, Logger> loggerMap = new HashMap<String, Logger>();
	private static Logger rootLogger = getRootLogger();

	/**
	 * Sets the log level for the whole logging hierachy starting with the
	 * default package to <code>level</code>.
	 * 
	 * @param level
	 *            new log level
	 */
	public static Level setLogLevel(Level level) {
		Level old = getRootLogger().getLevel();
		getRootLogger().setLevel(level);
		return old;
	}

	public static Logger getRootLogger() {
		synchronized (Logger.class) {
			if (rootLogger == null) {
				rootLogger = Logger.getLogger("");
				loggerMap.put("", rootLogger);
				rootLogger.setUseParentHandlers(false);
				ConsoleHandler consoleHandler = new ConsoleHandler();
				// the handler logs everything, but what is sent to the handler
				// is
				// specified by the logger.
				consoleHandler.setLevel(Level.ALL);

				// simple Formatter for single line output (<level> <method>:
				// <message>)
				consoleHandler.setFormatter(new Formatter() {
					@Override
					public String format(LogRecord record) {
						StringBuilder sb = new StringBuilder();
						sb.append(record.getLevel()).append(" ")
								.append(record.getSourceClassName())
								.append(".")
								.append(record.getSourceMethodName())
								.append(": ").append(record.getMessage())
								.append('\n');
						return sb.toString();
					}
				});
				removeHandlers(rootLogger);
				rootLogger.addHandler(consoleHandler);
			}
		}
		return rootLogger;
	}

	private static void removeHandlers(Logger l) {
		for (Handler h : l.getHandlers()) {
			l.removeHandler(h);
		}
	}

	/**
	 * Gets the {@link Logger} for the specified package and create a logger
	 * hierarchy down to the default package.
	 * 
	 * @param packageName
	 *            the name of the package.
	 * @return the {@link Logger} for the package <code>pkgName</code>
	 */
	public static Logger getLogger(String packageName) {
		if (packageName.equals("")) {
			return getRootLogger();
		}
		Logger l = loggerMap.get(packageName);
		if (l != null) {
			return l;
		}
		l = Logger.getLogger(packageName, null);
		l.setParent(getParentLogger(packageName));
		l.setLevel(null); // inherit level from parent
		l.setUseParentHandlers(true);
		loggerMap.put(packageName, l);
		return l;
	}

	/**
	 * Gets the {@link Logger} for the package of the class <code>cls</code> and
	 * create a logger hierarchy down to the default package.
	 * 
	 * @param cls
	 *            a {@link Class}
	 * @return the {@link Logger} for the package of <code>cls</code>
	 */
	public static Logger getLogger(Class<?> cls) {
		return getLogger(cls.getPackage().getName());
	}

	private static Logger getParentLogger(String childPkgName) {
		if (!childPkgName.contains(".")) {
			// the parent is the root logger
			return getRootLogger();
		}
		int lastDot = childPkgName.lastIndexOf('.');
		String parentPkgName = childPkgName.substring(0, lastDot);
		return getLogger(parentPkgName);
	}

	/**
	 * Prints version and license info.
	 * 
	 * @param args
	 *            (ignored)
	 */
	public static void main(String[] args) {
		System.out.println(JGraLab.getInfo(false));
	}

	private static String addInfo(String inputLine) {
		return inputLine.replace("$ver", getVersion()).replace("$codename",
				getCodename());
	}

	public static String getVersionInfo(boolean asTGComment) {
		return getInfoString(versionInfo, asTGComment);
	}

	public static String getInfo(boolean asTGComment) {
		String[] lines = new String[versionInfo.length + copyrightInfo.length];
		System.arraycopy(versionInfo, 0, lines, 0, versionInfo.length);
		System.arraycopy(copyrightInfo, 0, lines, versionInfo.length,
				copyrightInfo.length);
		return getInfoString(lines, asTGComment);
	}

	private static String getInfoString(String[] lines, boolean asTGComment) {
		StringBuffer output = new StringBuffer(1024);
		for (String line : lines) {
			if (asTGComment) {
				output.append("// ");
			} else {
				output.append(' ');
			}
			output.append(addInfo(line));
			output.append('\n');
		}
		output.append('\n');
		return output.toString();
	}

	public static final <T> PVector<T> vector() {
		return ArrayPVector.empty();
	}

	public static final <T> PSet<T> set() {
		return ArrayPSet.empty();
	}

	public static final <K, V> PMap<K, V> map() {
		return ArrayPMap.empty();
	}

	public static void setEclipseAdapter(EclipseAdapter eclipseAdapter) {
		JGraLab.eclipseAdapter = eclipseAdapter;
	}

	public static EclipseAdapter getEclipseAdapter() {
		return eclipseAdapter;
	}
}
