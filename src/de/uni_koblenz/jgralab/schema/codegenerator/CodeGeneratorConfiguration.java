/*
 * JGraLab - The Java Graph Laboratory
 *
 * Copyright (C) 2006-2013 Institute for Software Technology
 *                         University of Koblenz-Landau, Germany
 *                         ist@uni-koblenz.de
 *
 * For bug reports, documentation and further information, visit
 *
 *                         https://github.com/jgralab/jgralab
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
package de.uni_koblenz.jgralab.schema.codegenerator;

/**
 * This class keeps the configurations of the code generator and is passed to
 * all instances. It keeps the following configurations:<br>
 * 
 * <br>
 * - <code>MINIMAL</code> creates a new {@link CodeGeneratorConfiguration}
 * object that is marked to be without type specific method support.<br>
 * <br>
 * 
 * - <code>NORMAL</code> creates a new {@link CodeGeneratorConfiguration} object
 * that is marked to be with type specific method support (default).<br>
 * <br>
 * 
 * A <em>type-specific method</em> is a method such as "getNextXYVertex".
 */
public class CodeGeneratorConfiguration {

	public static final CodeGeneratorConfiguration MINIMAL = new CodeGeneratorConfiguration()
			.withoutTypeSpecificMethodSupport();

	public static final CodeGeneratorConfiguration NORMAL = new CodeGeneratorConfiguration();

	/**
	 * toggles, if the type-specific methods such as "getNextXYVertex" should be
	 * created. TODO Explain this better.
	 */
	private boolean typespecificMethodSupport = true;

	/**
	 * This constructor creates a default configuration:<br>
	 * <br>
	 * this.standardSupport = true <br>
	 * this.transactionSupport = false <br>
	 * this.typespecificMethodSupport = true <br>
	 * this.methodsForSubclassesSupport = false <br>
	 */
	public CodeGeneratorConfiguration() {
		typespecificMethodSupport = true;
	}

	public CodeGeneratorConfiguration withTypeSpecificMethodSupport() {
		typespecificMethodSupport = true;
		return this;
	}

	public CodeGeneratorConfiguration withoutTypeSpecificMethodSupport() {
		typespecificMethodSupport = false;
		return this;
	}

	/**
	 * This is a copy constructor.
	 * 
	 * @param other
	 *            A valid instance of {@link CodeGeneratorConfiguration} to copy
	 *            values from.
	 */
	public CodeGeneratorConfiguration(CodeGeneratorConfiguration other) {
		this.typespecificMethodSupport = other.typespecificMethodSupport;
	}

	public void setTypeSpecificMethodsSupport(boolean typespecificMethodSupport) {
		this.typespecificMethodSupport = typespecificMethodSupport;
	}

	public boolean hasTypeSpecificMethodsSupport() {
		return typespecificMethodSupport;
	}

}
