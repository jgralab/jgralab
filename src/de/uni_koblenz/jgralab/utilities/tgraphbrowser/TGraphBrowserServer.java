/* 
Copyright Paul James Mutton, 2001-2004, http://www.jibble.org/

This file is part of Mini Wegb Server / SimpleWebServer.

This software is dual-licensed, allowing you to choose between the GNU
General Public License (GPL) and the www.jibble.org Commercial License.
Since the GPL may be too restrictive for use in a proprietary application,
a commercial license is also provided. Full license information can be
found at http://www.jibble.org/licenses/

$Author: pjm2 $
$Id: ServerSideScriptEngine.java,v 1.4 2004/02/01 13:37:35 pjm2 Exp $

 */

package de.uni_koblenz.jgralab.utilities.tgraphbrowser;

import java.io.File;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Hashtable;
import java.util.logging.FileHandler;
import java.util.logging.Formatter;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;

import de.uni_koblenz.ist.utilities.option_handler.OptionHandler;
import de.uni_koblenz.jgralab.JGraLab;

/**
 * Copyright Paul Mutton http://www.jibble.org/
 * 
 */
public class TGraphBrowserServer extends Thread {

	private static final String[] versionInfo = { "TGraphBrowser",
			"  Version : 1.0" };

	private static final String[] copyrightInfo = {
			"(c) 2006-2009 Institute for Software Technology",
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
			"",
			"This software uses:",
			"",
			"JGraLab Dimetrodon",
			"(c) 2006-2009 Institute for Software Technology",
			"              University of Koblenz-Landau, Germany",
			"",
			"              ist@uni-koblenz.de",
			"",
			"JDOM 1.0",
			"Copyright (C) 2000-2004 Jason Hunter & Brett McLaughlin.",
			"All rights reserved.",
			"",
			"Apache XML-RPC 3.0",
			"Copyright (C) 2001-2008 The Apache Software Foundation",
			"Please note, you need the Java Enterprise Edition to make full use",
			"of this part of the software.", "", "Apache Commons CLI 1.2",
			"Copyright 2001-2009 The Apache Software Foundation" };

	public static Logger logger;

	static {
		logger = JGraLab.getRootLogger();
		Handler handler = logger.getHandlers()[0];
		logger.removeHandler(handler);
		Formatter formatter = handler.getFormatter();
		// create temp-folder
		String path = System.getProperty("java.io.tmpdir") + File.separator
				+ "tgraphbrowser";
		File tempFolder = new File(path);
		if (!tempFolder.exists()) {
			if (!tempFolder.mkdir()) {
				TGraphBrowserServer.logger.warning(tempFolder
						+ " could not be created.");
			}
		}
		// create log-folder
		path += File.separator + "logs";
		File logs = new File(path);
		if (!logs.exists()) {
			if (!logs.mkdir()) {
				TGraphBrowserServer.logger.warning(logs.toString()
						+ " could not be created");
			}
		}
		try {
			path += File.separator + "Logging.txt";
			handler = new FileHandler(path);
		} catch (SecurityException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		handler.setFormatter(formatter);
		logger.addHandler(handler);
		logger.setLevel(Level.INFO);
	}

	/**
	 * the time the server was started.
	 */
	public static long starttime;

	public static final String VERSION = "SimpleWebServer  http://www.jibble.org/";
	public static final Hashtable<String, String> MIME_TYPES = new Hashtable<String, String>();

	private static final int DEFAULT_PORT = 8080;

	static {
		String image = "image/";
		MIME_TYPES.put(".gif", image + "gif");
		MIME_TYPES.put(".jpg", image + "jpeg");
		MIME_TYPES.put(".jpeg", image + "jpeg");
		MIME_TYPES.put(".png", image + "png");
		String text = "text/";
		MIME_TYPES.put(".html", text + "html");
		MIME_TYPES.put(".htm", text + "html");
		MIME_TYPES.put(".txt", text + "plain");
	}

	private String workspace;
	private final ServerSocket _serverSocket;
	private final boolean _running = true;

	public TGraphBrowserServer(int port, String path, String maximumFileSize,
			String maximumWorkspaceSize) throws IOException {
		workspace = path;
		if (path != null
				&& (workspace.startsWith("\"") || workspace.startsWith("'"))) {
			workspace = workspace.substring(1, workspace.length() - 1);
		}
		RequestThread.MAXIMUM_FILE_SIZE = maximumFileSize == null ? null : Long
				.parseLong(maximumFileSize) * 1024 * 1024;
		File ws = new File(workspace);
		RequestThread.MAXIMUM_WORKSPACE_SIZE = maximumWorkspaceSize == null ? ws
				.getFreeSpace()
				+ ws.getTotalSpace()
				: Long.parseLong(maximumWorkspaceSize) * 1024 * 1024;
		_serverSocket = new ServerSocket(port);
	}

	@Override
	public void run() {
		System.out.println(this.getClass().getSimpleName()
				+ " is running on port " + _serverSocket.getLocalPort());
		System.out.println("Press CTRL + C to quit.");
		while (_running) {
			try {
				Socket socket = _serverSocket.accept();
				RequestThread requestThread = new RequestThread(socket,
						workspace);
				requestThread.start();
			} catch (IOException e) {
				e.printStackTrace();
				this.interrupt();
			}
		}
	}

	// Work out the filename extension. If there isn't one, we keep
	// it as the empty string ("").
	public static String getExtension(java.io.File file) {
		String extension = "";
		String filename = file.getName();
		int dotPos = filename.lastIndexOf(".");
		if (dotPos >= 0) {
			extension = filename.substring(dotPos);
		}
		return extension.toLowerCase();
	}

	/**
	 * Runs the server. Needed args: -w --workspace the workspace
	 * 
	 * @param args
	 */
	public static void main(String[] args) {
		CommandLine comLine = processCommandLineOptions(args);
		assert comLine != null;
		try {
			starttime = System.currentTimeMillis();
			String portnumber = comLine.getOptionValue("p");
			String workspacePath;
			if (comLine.hasOption("w")) {
				workspacePath = comLine.getOptionValue("w");
			} else {
				File workspace = new File(System.getProperty("java.io.tmpdir")
						+ File.separator + "tgraphbrowser");
				if (!workspace.exists()) {
					if (!workspace.mkdir()) {
						logger.info("The temp folder "
								+ workspace.getAbsolutePath()
								+ " could not be created.");
					}
				}
				workspace = new File(workspace.getAbsoluteFile()
						+ File.separator + "workspace");
				if (!workspace.exists()) {
					if (!workspace.mkdir()) {
						logger.info("The default workspace "
								+ workspace.getAbsolutePath()
								+ " could not be created.");
					}
				}
				workspacePath = workspace.getAbsolutePath();
			}
			new TGraphBrowserServer(portnumber == null ? DEFAULT_PORT : Integer
					.parseInt(portnumber), workspacePath, comLine
					.getOptionValue("m"), comLine.getOptionValue("s")).start();
			if (comLine.getOptionValue("ic") != null) {
				TabularVisualizer.NUMBER_OF_INCIDENCES_PER_PAGE = Math.max(
						Integer.parseInt(comLine.getOptionValue("ic")), 1);
			}
			if (comLine.hasOption("d")) {
				StateRepository.dot = comLine.getOptionValue("d");
			} else {
				System.out
						.println("The 2D-Visualization is disabled because parameter -d is not set.");
			}
			String timeout = comLine.getOptionValue("t");
			String checkIntervall = comLine.getOptionValue("i");
			new DeleteUnusedStates(timeout == null ? 600 : Integer
					.parseInt(timeout), checkIntervall == null ? 60 : Integer
					.parseInt(checkIntervall)).start();
		} catch (IOException e) {
			System.out.println(e);
		}
	}

	/**
	 * Processes the command line options.
	 * 
	 * @param args
	 * @return
	 */
	private static CommandLine processCommandLineOptions(String[] args) {
		String toolString = "java " + TGraphBrowserServer.class.getName();
		String versionString = getInfo();
		OptionHandler oh = new OptionHandler(toolString, versionString);

		Option output = new Option("w", "workspace", true,
				"(optional): the workspace. Default is $temp$/tgraphbrowser/workspace");
		output.setRequired(false);
		output.setArgName("path");
		oh.addOption(output);

		Option stateTimeout = new Option("t", "state_timeout", true,
				"(optional): resources are freed, "
						+ "if they were not used in the last <given> seconds."
						+ " Default value is 600 sec.");
		stateTimeout.setRequired(false);
		stateTimeout.setArgName("sec");
		oh.addOption(stateTimeout);

		Option checkInterval = new Option(
				"ci",
				"check_interval",
				true,
				"(optional): the interval in sec after which the states"
						+ " are checked, if thex are to old. Default value is 60 sec.");
		checkInterval.setRequired(false);
		checkInterval.setArgName("sec");
		oh.addOption(checkInterval);

		Option port = new Option("p", "port", true,
				"(optional): the port of the server. Default is port 8080.");
		port.setRequired(false);
		port.setArgName("portnumber");
		oh.addOption(port);

		Option incidences = new Option("i", "incidences", true,
				"(optional): number of incident edges which are"
						+ " shown in the table view. Default is 10.");
		incidences.setRequired(false);
		incidences.setArgName("int");
		oh.addOption(incidences);

		Option dot = new Option("d", "dot", true,
				"(optional): the command to call dot."
						+ " If it is not set, the 2D-view won't be available.");
		dot.setRequired(false);
		dot.setArgName("command");
		oh.addOption(dot);

		Option maxSize = new Option("m", "maximum_filesize", true,
				"(optional): the maximum size of a tg-file which is loaded in MB");
		maxSize.setRequired(false);
		maxSize.setArgName("int");
		oh.addOption(maxSize);

		Option size = new Option(
				"s",
				"size_of_workspace",
				true,
				"(optional): the maximum size of the workspace in MB."
						+ " Newly submitted tg.-files are rejected,"
						+ " if maximumSizeOfWorkspace < currentSizeOfWorkspace + sizeOfSubmittedTgFile ");
		size.setRequired(false);
		size.setArgName("int");
		oh.addOption(size);

		return oh.parse(args);
	}

	public static String getInfo() {
		String[] lines = new String[versionInfo.length + copyrightInfo.length];
		System.arraycopy(versionInfo, 0, lines, 0, versionInfo.length);
		System.arraycopy(copyrightInfo, 0, lines, versionInfo.length,
				copyrightInfo.length);
		return getInfoString(lines);
	}

	private static String getInfoString(String[] lines) {
		StringBuffer output = new StringBuffer(1024);
		for (String line : lines) {
			output.append(' ');
			output.append(line);
			output.append('\n');
		}
		output.append('\n');
		return output.toString();
	}

	/**
	 * This thread is used to delete timeouted states.
	 */
	static class DeleteUnusedStates extends Thread {
		public static long timeout;
		private final long checkInterval;

		/**
		 * Creates a new DeleteUnusedStates object.
		 * 
		 * @param timeoutSec
		 *            if the state wasn't accessed in the last
		 *            <code>timeoutSec</code> seconds it is deleted.
		 * @param checkIntervalSec
		 *            the number of seconds, after that the states are checked
		 *            if they are timeouted
		 */
		public DeleteUnusedStates(long timeoutSec, long checkIntervalSec) {
			super();
			timeout = timeoutSec * 1000;
			checkInterval = checkIntervalSec * 1000;
		}

		@Override
		public void run() {
			while (true) {
				try {
					sleep(checkInterval);
					StateRepository.deleteAllUnusedSessions(timeout);
				} catch (InterruptedException e) {

				}
			}
		}

	}

}