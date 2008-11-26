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

package de.uni_koblenz.jgralab;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * This class gives information, which software was used creating JGraLab.
 * 
 * @author ist@uni-koblenz.de
 */
public class JGraLab {

	// look but don't touch, both values are updated automatically
	private final String revision = "$Revision$";
	private final String buildID = "62";
	//
	// to use this information inside the text place $rev for the revision
	// information
	// and $bid for the build id
	private final String version = "Carnotaurus";

	private final String[] info = {
			"JGraLab - The Java graph laboratory",
			"  Version : $ver",
			"  $rev",
			"  Build ID: $bid",
			"(c) 2006-2008 Institute for Software Technology",
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
			"Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.",
			"",
			"This software uses:",
			"",
			"ANTLR 2.7.6",
			"Copyright (c) 2003-2008, Terence Parr",
			"All rights reserved.",
			"",
			"JDOM 1.0",
			"Copyright (C) 2000-2004 Jason Hunter & Brett McLaughlin.",
			"All rights reserved.",
			"",
			"Apache XML-RPC 3.0",
			"Copyright (C) 2001-2008 The Apache Software Foundation",
			"Please note, you need the Java Enterprise Edition to make full use",
			"of this part of the software." };

	public JGraLab() {

	}

	/**
	 * Sets the log level for package de.uni_koblenz.jgralab and all its
	 * children to <code>level</code>.
	 * 
	 * @param level
	 *            new log level
	 */
	public static void setLogLevel(Level level) {
		Logger.getLogger(JGraLab.class.getPackage().getName()).setLevel(level);
	}

	/**
	 * Prints version and license info.
	 * 
	 * @param args
	 *            (ignored)
	 */
	public static void main(String[] args) {
		JGraLab jgralab = new JGraLab();
		System.out.println(jgralab.toString());
	}

	private String addInfo(String inputLine) {
		String outputLine = inputLine;
		String revString = revision.replace("$R", "R").replace(" $", "");

		outputLine = outputLine.replace("$ver", version);
		outputLine = outputLine.replace("$rev", revString);
		outputLine = outputLine.replace("$bid", "Build ID: " + buildID);

		return outputLine;
	}

	@Override
	public String toString() {
		StringBuffer output = new StringBuffer(1024);
		output.append('\n');
		for (int i = 0; (i < info.length); i++) {
			output.append(' ');
			output.append(addInfo(info[i]));
			output.append('\n');
		}
		return output.toString();
	}
}
