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
package de.uni_koblenz.jgralabtest.utilities.common;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionGroup;

import de.uni_koblenz.ist.utilities.option_handler.OptionHandler;

public class TryCLI {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		OptionHandler oh = new OptionHandler("TryCli", "version 0.0");

		// Option multipleValues = new Option("m", "multiple", true,
		// "Can occur multiple times.");
		// multipleValues.setRequired(true);
		// multipleValues.setArgName("arg");
		// multipleValues.setValueSeparator(',');
		// multipleValues.setArgs(Option.UNLIMITED_VALUES);

		// Option multipleValues2 = new Option("M", "Multiple", true,
		// "Can occur multiple times.");
		// multipleValues2.setRequired(false);
		// multipleValues2.setArgName("arg");
		// multipleValues2.setValueSeparator(',');
		// multipleValues2.setOptionalArg(true);
		// multipleValues2.setArgs(Option.UNLIMITED_VALUES);

		Option test = new Option("t", "test", false, "For testing purpose.");
		test.setRequired(false);
		oh.addOption(test);

		Option test2 = new Option("T", "Test", false, "For testing purpose.");
		test2.setRequired(false);
		oh.addOption(test2);

		OptionGroup og = new OptionGroup();
		og.addOption(test);
		og.addOption(test2);
		og.setRequired(true);
		oh.addOptionGroup(og);

		// oh.addOption(multipleValues);
		// oh.addOption(multipleValues2);

		CommandLine cmd = oh.parse(args);

		if (cmd.hasOption('t')) {
			System.out.println("Test1 set");
		}
		if (cmd.hasOption('T')) {
			System.out.println("Test2 set");
		}

	}

}
