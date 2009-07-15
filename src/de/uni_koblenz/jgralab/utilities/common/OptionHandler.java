package de.uni_koblenz.jgralab.utilities.common;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionGroup;
import org.apache.commons.cli.Options;

/**
 * This class is a wrapper for apache commons CLI. It implements a workaround
 * for the missing feature of isolated options for printing the help or the
 * version information. This class is a wrapper for several classes of CLI.
 * 
 * It also provides methods for printing the help and version information.
 * 
 * @author strauss
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
	 * This is the Options structure needed for CLI
	 */
	private Options options;

	/**
	 * This map stores the information whether the option is required or not. It
	 * is necessary to do this because the workaround requires setting all
	 * options to optional.
	 */
	private Map<Option, Boolean> requiredMap;

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
	private HelpFormatter helpForm;

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
		requiredMap = new HashMap<Option, Boolean>();
		helpForm = new HelpFormatter();
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
		requiredMap.put(o, o.isRequired());
		o.setRequired(false);
		optionList.add(o);
		options.addOption(o);
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
	 * Checks if the option -h or -v is set. If one of them is set, the matching
	 * notice will be printed out and the program exits with status 0. If
	 * neither is set, this Method does nothing.
	 * 
	 * @param comLine
	 */
	public void eventuallyPrintHelpOrVersion(CommandLine comLine) {
		// String helpValue = comLine.getOptionValue("h");
		boolean isHelpSet = comLine.hasOption("h");
		if (isHelpSet) {
			printHelpAndExit(0);
		}
		boolean isVersionSet = comLine.hasOption("v");
		if (isVersionSet) {
			printVersionAndExit(0);
		}
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
		helpForm.printHelp(getUsageString(), options);
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

			return out.toString();
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
		Boolean required = requiredMap.get(o);
		return required == null ? false : required.booleanValue();
	}

	/**
	 * Checks if all required options are present in the given CommandLine
	 * according to the internal Map.
	 * 
	 * @param comLine
	 *            the commandLine to check.
	 * @return true if the given CommandLine contains all required options.
	 */
	public boolean containsAllRequired(CommandLine comLine) {
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
		return ok;
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
		if (numberOfArgs == Option.UNLIMITED_VALUES) {
			out.append(" ");
			out.append("{");
			appendArgName(out, current);
			out.append("}");
		}
		if (numberOfArgs >= 1) {
			out.append(" ");
			appendArgName(out, current);
			for (int i = 1; i < numberOfArgs; i++) {
				out.append(current.getValueSeparator());
				appendArgName(out, current);
			}
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
	private static void appendArgName(StringBuilder out, Option current) {
		out.append("<");
		out.append(current.getArgName());
		out.append(">");
	}
}
