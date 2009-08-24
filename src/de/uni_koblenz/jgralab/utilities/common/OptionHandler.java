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

package de.uni_koblenz.jgralab.utilities.common;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionGroup;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

// TODO handle OptionGroups properly
/**
 * This class is a wrapper for apache commons CLI. It implements a workaround
 * for the missing feature of isolated options for printing the help or the
 * version information. This class is a wrapper for several classes of CLI.
 * 
 * It also provides methods for printing the help and version information. <br>
 * Common mean of the parameters:<br>
 * -a --alternative-schema specifies the alternative schema<br>
 * -c --compile if specified, the .java are compiled<br>
 * -d --domains if set, domain names of attributes will be printed<br>
 * -e --edgeattr if set, edge attributes will be printed<br>
 * -e --exclude-pattern regular expression matching elements which should be
 * excluded<br>
 * -g --graph tg-file of a graph as input<br>
 * -h --help print help<br>
 * -j --jar specifies the name of the jar-file<br>
 * -n --namespace-prefix namespace prefix<br>
 * -n --rolenames if set, role names will be printed<br>
 * -o --output output file<br>
 * -p --path output path for the created files<br>
 * -q --queryfile queryfile which should be executed -r --reversed if set, edges
 * will be reversed<br>
 * -s --schema tg-file of a schema as input<br>
 * -s --shorten-strings if set, strings are shortened<br>
 * -s1 -s2 --schema1 --schema2 schemas to be compared<br>
 * -v --version print version information<br>
 * -x --xsd-location the location of the xsd-schema<br>
 * 
 * @author ist@uni-koblenz.de
 * 
 */
public class OptionHandler {
	/**
	 * This List stores all options in addition to the field "options". This
	 * redundancy is neccessary because Options does not implement the Interface
	 * Iterable meaning it is not possible to iterate over all options.
	 */
	private List<Option> optionList;

	/**
	 * Same thing for OptionGroups
	 */
	private List<OptionGroup> optionGroupList;

	/**
	 * This is the Options structure needed for CLI
	 */
	private Options options;

	/**
	 * This set stores the required options. It is necessary to do this because
	 * the workaround requires setting all options to not required.
	 */
	private Set<Option> requiredOptions;

	/**
	 * Same thing for OptionGroups
	 */
	private Set<OptionGroup> requiredOptionGroups;

	/**
	 * The String representing the usage. It is lazily created.
	 */
	private String usageString;

	/**
	 * The String showing the tool name. It is used for the usageString.
	 */
	private String toolString;

	/**
	 * The String showing the version information.
	 */
	private String versionString;

	/**
	 * The Helpformatter needed for printing the help information.
	 */
	private HelpFormatter helpFormatter;

	/**
	 * The only constructor of this class. It sets the toolString and the
	 * versionString. In this constructor the two options -h and -v are created
	 * and added. It also ensures that only -h or -v can be set.
	 * 
	 * @param toolString
	 *            the name of the tool
	 * @param versionString
	 *            the version information of the tool
	 */
	public OptionHandler(String toolString, String versionString) {
		options = new Options();
		optionList = new ArrayList<Option>();
		optionGroupList = new ArrayList<OptionGroup>();
		requiredOptions = new HashSet<Option>();
		requiredOptionGroups = new HashSet<OptionGroup>();
		helpFormatter = new HelpFormatter();
		this.toolString = toolString;
		this.versionString = versionString;

		Option help = new Option("h", "help", false,
				"(optional): print this help message.");
		help.setRequired(false);
		addOption(help);

		Option version = new Option("v", "version", false,
				"(optional): print version information");
		version.setRequired(false);
		addOption(version);

		OptionGroup mainOptions = new OptionGroup();
		mainOptions.setRequired(false);
		mainOptions.addOption(help);
		mainOptions.addOption(version);

		options.addOptionGroup(mainOptions);
	}

	/**
	 * This method is used for adding a new Option. It stores the information if
	 * the Option is required, sets this value to false and adds the Option to
	 * the Options object.
	 * 
	 * @param o
	 */
	public void addOption(Option o) {
		// backup required Status and set it to false
		if (o.isRequired()) {
			requiredOptions.add(o);
			o.setRequired(false);
		}
		optionList.add(o);
		options.addOption(o);
	}

	public void addOptionGroup(OptionGroup og) {
		if (og.isRequired()) {
			requiredOptionGroups.add(og);
			og.setRequired(false);
		}
		optionGroupList.add(og);
		options.addOptionGroup(og);
	}

	/**
	 * Returns the Options structure.
	 * 
	 * @return the Options structure.
	 */
	public Options getOptions() {
		return options;
	}

	/**
	 * Prints the version information and exits with the given exitCode.
	 * 
	 * @param exitCode
	 *            the exitCode
	 */
	public void printVersionAndExit(int exitCode) {
		System.out.println(versionString);
		System.exit(exitCode);
	}

	/**
	 * Prints the help information and exits with the given exitCode.
	 * 
	 * @param exitCode
	 */
	public void printHelpAndExit(int exitCode) {
		helpFormatter.printHelp(getUsageString(), options);
		System.exit(exitCode);
	}

	/**
	 * Lazily generates and returns the usageString.
	 * 
	 * @return the usageString.
	 */
	private String getUsageString() {
		if (usageString == null) {
			StringBuilder out = new StringBuilder(toolString);

			for (Option current : optionList) {
				out.append(" ");
				if (isOptionRequired(current)) {
					appendOption(out, current);
				} else {
					out.append("[");
					appendOption(out, current);
					out.append("]");
				}
			}
			usageString = out.toString();
		}
		return usageString;
	}

	/**
	 * Checks if a given Option is required according to the information stored
	 * in the Map.
	 * 
	 * @param o
	 *            the Option to check.
	 * @return true if the option is required, false otherwise. If the Option is
	 *         not in the Map, false is returned.
	 */
	public boolean isOptionRequired(Option o) {
		return requiredOptions.contains(o);
	}

	/**
	 * Checks if all required options are present in the given CommandLine
	 * according to the internal Map.
	 * 
	 * @param comLine
	 *            the commandLine to check.
	 * @return true if the given CommandLine contains all required options.
	 */
	public boolean containsAllRequiredOptions(CommandLine comLine) {
		boolean ok = true;
		Option[] setOptions = comLine.getOptions();

		Set<Option> setOptionsSet = new HashSet<Option>();
		for (Option current : setOptions) {
			setOptionsSet.add(current);
		}

		for (Option current : optionList) {
			if (isOptionRequired(current)) {
				ok &= setOptionsSet.contains(current);
				if (!ok) {
					break;
				}
			}
		}
		// if all required options are set and there are required OptionGroups,
		// check them
		return ok && !requiredOptionGroups.isEmpty() ? ok
				&& containsAllRequiredOptionGroups(setOptions) : ok;
	}

	private boolean containsAllRequiredOptionGroups(Option[] setOptions) {
		boolean allContained = true;
		for (OptionGroup currentRequiredOptionGroup : requiredOptionGroups) {
			boolean currentContained = false;
			for (Option currentOption : setOptions) {
				currentContained |= currentRequiredOptionGroup.getOptions()
						.contains(currentOption);
				if (currentContained) {
					break;
				}
			}
			allContained &= currentContained;
			if (!allContained) {
				break;
			}
		}
		return allContained;
	}

	/**
	 * Used by the method getUsageString for adding an Option to the String.
	 * 
	 * @param out
	 *            The StringBuilder to write into.
	 * @param current
	 *            The option to add.
	 */
	private void appendOption(StringBuilder out, Option current) {
		out.append("-");
		out.append(current.getOpt());
		int numberOfArgs = current.getArgs();
		out.append(" ");
		if (current.hasOptionalArg()) {
			out.append("[");
		}
		if (numberOfArgs == Option.UNLIMITED_VALUES) {
			appendArgName(out, current);
			out.append("{");
			out.append(current.getValueSeparator());
			appendArgName(out, current);
			out.append("}");
		}
		if (numberOfArgs >= 1) {
			appendArgName(out, current);
			for (int i = 1; i < numberOfArgs; i++) {
				out.append(current.getValueSeparator());
				appendArgName(out, current);
			}
		}
		if (current.hasOptionalArg()) {
			out.append("]");
		}
	}

	/**
	 * Appends the argument name of the given Option to the given StringBuilder.
	 * 
	 * @param out
	 *            the StringBuilder to write into.
	 * @param current
	 *            the Option of which to write the argument name
	 */
	private void appendArgName(StringBuilder out, Option current) {
		out.append("<").append(current.getArgName()).append(">");
	}

	/**
	 * Parses command line parameters <code>args</code> and checks wheter -h or
	 * -v were specified. In case of any error, prints diagnostic message, usage
	 * information, and exits.
	 * 
	 * @param args
	 *            command line parameters
	 * @return a CommandLine object containing parsed options
	 */
	public CommandLine parse(String[] args) {
		try {
			CommandLineParser parser = new GnuParser();
			CommandLine comLine = parser.parse(getOptions(), args);
			if (comLine.hasOption("h")) {
				printHelpAndExit(0);
			} else if (comLine.hasOption("v")) {
				printVersionAndExit(0);
			} else if (!containsAllRequiredOptions(comLine)) {
				System.err.println("Required options are missing.");
				printHelpAndExit(1);
			}
			return comLine;
		} catch (ParseException e) {
			System.err.println(parseErrorMessage(e.getMessage()));

			printHelpAndExit(1);
		}
		// never reached
		return null;
	}

	private static String parseErrorMessage(String error) {
		StringBuilder sb = new StringBuilder(error.length());

		int begin = 0;
		int end = error.indexOf('[');
		sb.append(error.substring(begin, end));
		sb.append("\n\t");
		begin = end + 1;

		while ((end = error.indexOf(',', begin)) != -1) {
			sb.append(error.substring(begin, end + 1).trim());
			sb.append("\n\t");
			begin = end + 1;
		}

		sb.append(error.substring(begin, error.length() - 1).trim());

		return sb.toString();
	}
}
