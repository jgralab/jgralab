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

package de.uni_koblenz.jgralab.codegenerator;

import de.uni_koblenz.jgralab.schema.EnumDomain;

/**
 * TODO add comment
 * 
 * @author ist@uni-koblenz.de
 * 
 */
public class EnumCodeGenerator extends CodeGenerator {
	private EnumDomain enumDomain;

	/**
	 * Creates a new EnumCodeGenerator which creates code for the given
	 * enumDomain object
	 */
	public EnumCodeGenerator(EnumDomain enumDomain, String schemaPackageName,
			String implementationName) {
		super(schemaPackageName, enumDomain.getPackageName(), new CodeGeneratorConfiguration());
		rootBlock.setVariable("simpleClassName", enumDomain.getSimpleName());
		rootBlock.setVariable("isClassOnly", "true");
		this.enumDomain = enumDomain;
	}

	@Override
	protected CodeBlock createBody(boolean createClass) {
		CodeSnippet constCode = new CodeSnippet(true);
		String delim = "";
		StringBuilder constants = new StringBuilder();
		for (String s : enumDomain.getConsts()) {
			constants.append(delim);
			constants.append(s);
			delim = ", ";
		}
		constants.append(";");
		constCode.add(constants.toString());

		CodeSnippet valueOfCode = new CodeSnippet(true);
		valueOfCode.add("public static #simpleClassName# valueOfPermitNull(String val) {",
						"\tif (val.equals(de.uni_koblenz.jgralab.GraphIO.NULL_LITERAL) || val.equals(de.uni_koblenz.jgralab.GraphIO.OLD_NULL_LITERAL)) {",
						"\t\treturn null;", "\t}", "\treturn valueOf(val);",
						"}");

		CodeList result = new CodeList();
		result.add(constCode);
		result.add(valueOfCode);
		return result;
	}

	@Override
	protected CodeBlock createHeader(boolean createClass) {
		return new CodeSnippet(true, "public enum #simpleClassName# {");
	}
}
