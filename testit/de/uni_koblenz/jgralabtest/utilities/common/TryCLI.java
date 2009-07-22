package de.uni_koblenz.jgralabtest.utilities.common;

import java.util.Arrays;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;

import de.uni_koblenz.jgralab.utilities.common.OptionHandler;

public class TryCLI {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		OptionHandler oh = new OptionHandler("TryCli", "version 0.0");

		Option multipleValues = new Option("m", "multiple", true,
				"Can occur multiple times.");
		multipleValues.setRequired(true);
		multipleValues.setArgName("arg");
		multipleValues.setValueSeparator(',');
		multipleValues.setArgs(Option.UNLIMITED_VALUES);

		Option multipleValues2 = new Option("M", "Multiple", true,
				"Can occur multiple times.");
		multipleValues2.setRequired(true);
		multipleValues2.setArgName("arg");
		multipleValues2.setValueSeparator(',');
		multipleValues2.setOptionalArg(true);
		multipleValues2.setArgs(Option.UNLIMITED_VALUES);

		oh.addOption(multipleValues);
		oh.addOption(multipleValues2);

		CommandLine cmd = oh.parse(args);

		String[] optionValues = cmd.getOptionValues('m');
		String[] optionValues2 = cmd.getOptionValues('M');

		System.out.println(optionValues.length);
		System.out.println(Arrays.toString(optionValues));

		if (optionValues2 != null) {
			System.out.println(optionValues2.length);
			System.out.println(Arrays.toString(optionValues2));
		}
	}

}
