package de.uni_koblenz.jgralabtest.utilities.common;

import java.util.Arrays;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionGroup;

import de.uni_koblenz.jgralab.utilities.common.OptionHandler;

public class TryCLI {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		OptionHandler oh = new OptionHandler("TryCli", "version 0.0");

//		Option multipleValues = new Option("m", "multiple", true,
//				"Can occur multiple times.");
//		multipleValues.setRequired(true);
//		multipleValues.setArgName("arg");
//		multipleValues.setValueSeparator(',');
//		multipleValues.setArgs(Option.UNLIMITED_VALUES);

//		Option multipleValues2 = new Option("M", "Multiple", true,
//				"Can occur multiple times.");
//		multipleValues2.setRequired(false);
//		multipleValues2.setArgName("arg");
//		multipleValues2.setValueSeparator(',');
//		multipleValues2.setOptionalArg(true);
//		multipleValues2.setArgs(Option.UNLIMITED_VALUES);

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

//		oh.addOption(multipleValues);
//		oh.addOption(multipleValues2);

		CommandLine cmd = oh.parse(args);

		if(cmd.hasOption('t')){
			System.out.println("Test1 set");
		}
		if(cmd.hasOption('T')){
			System.out.println("Test2 set");
		}
		
		
	}

}
