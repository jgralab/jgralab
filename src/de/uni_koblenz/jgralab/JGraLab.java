/*
 * JGraLab - The Java graph laboratory
 * (c) 2006-2010 Institute for Software Technology
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

package de.uni_koblenz.jgralab;

import java.util.HashMap;
import java.util.logging.ConsoleHandler;
import java.util.logging.Formatter;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

/**
 * This class gives information, which software was used creating JGraLab.
 * 
 * @author ist@uni-koblenz.de
 */
public class JGraLab {

	// look but don't touch, both values are updated automatically
	private static final String revision = "$Revision$";

	private static final String buildID = "69";
	//
	// to use this information inside the text place $rev for the revision
	// information and $bid for the build id
	private static final String version = "Dimetrodon";
	private static final String[] versionInfo = {
			"JGraLab - The Java graph laboratory", "  Version : $ver",
			"  $rev", "  Build ID: $bid" };

	private static final String[] copyrightInfo = {
			"(c) 2006-2010 Institute for Software Technology",
			"              University of Koblenz-Landau, Germany",
			"",
			"              ist@uni-koblenz.de",
			"",
			"Please report bugs to http://serres.uni-koblenz.de/bugzilla",
			"",
			"This program is free software; you can redistribute it and/or",
			"modify it under the terms of the GNU General Public License",
			"as published by the Free Software Foundation; either version 2",
			"of the License, or (at your option) any later version.",
			"",
			"This program is distributed in the hope that it will be useful,",
			"but WITHOUT ANY WARRANTY; without even the implied warranty of",
			"MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the",
			"GNU General Public License for more details.",
			"",
			"You should have received a copy of the GNU General Public License",
			"along with this program; if not, write to the Free Software",
			"Foundation Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.",
			"", "This software uses:", "", "JDOM 1.0",
			"Copyright (C) 2000-2004 Jason Hunter & Brett McLaughlin.",
			"All rights reserved.", "", "Apache Commons CLI 1.2",
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
	public static void setLogLevel(Level level) {
		getRootLogger().setLevel(level);
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
						sb.append(record.getLevel()).append(" ").append(
								record.getSourceClassName()).append(".")
								.append(record.getSourceMethodName()).append(
										": ").append(record.getMessage())
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
	 * @param pkgName
	 *            the name of the package.
	 * @return the {@link Logger} for the package <code>pkgName</code>
	 */
	public static Logger getLogger(String pkgName) {
		if (pkgName.equals("")) {
			return getRootLogger();
		}
		Logger l = loggerMap.get(pkgName);
		if (l != null) {
			return l;
		}
		l = Logger.getLogger(pkgName, null);
		l.setParent(getParentLogger(pkgName));
		l.setLevel(null); // inherit level from parent
		l.setUseParentHandlers(true);
		loggerMap.put(pkgName, l);
		return l;
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
		String outputLine = inputLine;
		String revString = revision.replace("$R", "R").replace(" $", "");

		outputLine = outputLine.replace("$ver", version);
		outputLine = outputLine.replace("$rev", revString);
		outputLine = outputLine.replace("$bid", buildID);

		return outputLine;
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
}
