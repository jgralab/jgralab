/*
 * JGraLab - The Java Graph Laboratory
 * 
 * Copyright (C) 2006-2011 Institute for Software Technology
 *                         University of Koblenz-Landau, Germany
 *                         ist@uni-koblenz.de
 * 
 * For bug reports, documentation and further information, visit
 * 
 *                         http://jgralab.uni-koblenz.de
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
		super(schemaPackageName, enumDomain.getPackageName(),
				new CodeGeneratorConfiguration());
		rootBlock.setVariable("simpleClassName", enumDomain.getSimpleName());
		rootBlock.setVariable("isClassOnly", "true");
		this.enumDomain = enumDomain;
	}

	@Override
	protected CodeBlock createBody() {
		CodeSnippet constCode = new CodeSnippet(true);
		CodeList result = new CodeList();
		if (currentCycle.isClassOnly()) {
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
			valueOfCode
					.add(
							"public static #simpleClassName# valueOfPermitNull(String val) {",
							"\tif (val.equals(de.uni_koblenz.jgralab.GraphIO.NULL_LITERAL)) {",
							"\t\treturn null;", "\t}",
							"\treturn valueOf(val);", "}");
			result.add(constCode);
			result.add(valueOfCode);
		}
		return result;
	}

	@Override
	protected CodeBlock createHeader() {
		if (currentCycle.isClassOnly()) {
			return new CodeSnippet(true, "public enum #simpleClassName# {");
		}
		return new CodeSnippet();
	}
}
