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
package de.uni_koblenz.jgralab.codegenerator;

/**
 * This class keeps the configurations of the code generator and is passed to
 * all instances. It keeps and manages the following configuration entries:
 * 
 * - <code>standardSupport</code> toggles, if the classes for standard support
 * should be created, enabled by default.<br>
 * <br>
 * 
 * - <code>transactionSupport</code> toggles, if the classes for transaction
 * support should be created, enabled by default.<br>
 * <br>
 * 
 * - <code>typespecificMethodsSupport</code> toggles, if the typespecific
 * methods such as "getNextXYVertex" should be created, enabled by default.<br>
 * <br>
 * 
 * - <code>methodsForSubclasseSupport</code> toggles, if the methods with an
 * additional subtype-flag like "getNextXYVertex(boolean withSubclasses)" should
 * be created. Needs typeSpecifigMethodsSupport to be enabled. Disabled by
 * default.<br>
 * <br>
 * 
 * - <code>saveMemSupport</code> toggles, if the memory saving classes should be
 * created, disabled by default.
 */
public class CodeGeneratorConfiguration {

	public static final CodeGeneratorConfiguration WITH_TRANSACTION_SUPPORT = new CodeGeneratorConfiguration()
			.withTransactionSupport();

	public static final CodeGeneratorConfiguration WITH_DATABASE_SUPPORT = new CodeGeneratorConfiguration()
			.withDatabaseSupport();

	public static final CodeGeneratorConfiguration FULL = new CodeGeneratorConfiguration()
			.withTransactionSupport().withMethodsForSubclassesSupport()
			.withSaveMemSupport().withDatabaseSupport();

	public static final CodeGeneratorConfiguration FULL_WITHOUT_SUBCLASS_FLAGS = new CodeGeneratorConfiguration()
			.withTransactionSupport().withSaveMemSupport()
			.withDatabaseSupport();

	public static final CodeGeneratorConfiguration WITHOUT_TYPESPECIFIC_METHODS = new CodeGeneratorConfiguration()
			.withTransactionSupport().withoutTypeSpecificMethodSupport()
			.withDatabaseSupport();

	public static final CodeGeneratorConfiguration MINIMAL = new CodeGeneratorConfiguration()
			.withoutTypeSpecificMethodSupport();

	/** toggles, if the classes for standard support should be created */
	private boolean standardSupport = true;

	/** toggles, if the classes for transaction support should be created */
	private boolean transactionSupport = false;

	/** toggles, if classes for database support should be created */
	private boolean databaseSupport = false;

	/**
	 * toggles, if the memory saving std classes shall be used or not. If true,
	 * singly linked lists will be used internally, instead of double linked
	 * lists. Runtime will be possibly worse, though.
	 */
	private boolean saveMemSupport = false;

	/**
	 * toggles, if the type-specific methods such as "getNextXYVertex" should be
	 * created
	 */
	private boolean typespecificMethodSupport = true;

	/**
	 * toggles, if the methods with an additional subtype-flag like
	 * "getNextXYVertex(boolean withSubclasses)" should be created. Needs
	 * typespecifigMethodsSupport to be enabled.
	 */
	private boolean methodsForSubclassesSupport = false;

	/**
	 * This constructor creates a default configuration:<br>
	 * <br>
	 * this.standardSupport = true <br>
	 * this.transactionSupport = false <br>
	 * this.typespecificMethodSupport = true <br>
	 * this.methodsForSubclassesSupport = false <br>
	 * this.saveMemSupport = false <br>
	 */
	public CodeGeneratorConfiguration() {
		standardSupport = true;
		transactionSupport = false;
		saveMemSupport = false;
		typespecificMethodSupport = true;
		methodsForSubclassesSupport = false;
		databaseSupport = false;
	}

	public CodeGeneratorConfiguration withoutStandardSupport() {
		standardSupport = false;
		return this;
	}

	public CodeGeneratorConfiguration withTransactionSupport() {
		transactionSupport = true;
		return this;
	}

	public CodeGeneratorConfiguration withDatabaseSupport() {
		databaseSupport = true;
		return this;
	}

	public CodeGeneratorConfiguration withSaveMemSupport() {
		saveMemSupport = true;
		return this;
	}

	public CodeGeneratorConfiguration withoutTypeSpecificMethodSupport() {
		typespecificMethodSupport = false;
		return this;
	}

	public CodeGeneratorConfiguration withMethodsForSubclassesSupport() {
		methodsForSubclassesSupport = true;
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
		this.standardSupport = other.standardSupport;
		this.transactionSupport = other.transactionSupport;
		this.typespecificMethodSupport = other.typespecificMethodSupport;
		this.saveMemSupport = other.saveMemSupport;
		this.databaseSupport = other.databaseSupport;
		this.methodsForSubclassesSupport = other.methodsForSubclassesSupport;
	}

	public void setStandardSupport(boolean standardSupport) {
		this.standardSupport = standardSupport;
	}

	public boolean hasStandardSupport() {
		return standardSupport;
	}

	public void setTransactionSupport(boolean transactionSupport) {
		this.transactionSupport = transactionSupport;
	}

	public boolean hasTransactionSupport() {
		return transactionSupport;
	}

	public void setDatabaseSupport(boolean databaseSupport) {
		this.databaseSupport = databaseSupport;
	}

	public boolean hasDatabaseSupport() {
		return this.databaseSupport;
	}

	public void setTypeSpecificMethodsSupport(boolean typespecificMethodSupport) {
		this.typespecificMethodSupport = typespecificMethodSupport;
	}

	public boolean hasTypeSpecificMethodsSupport() {
		return typespecificMethodSupport;
	}

	public void setMethodsForSubclassesSupport(
			boolean methodsForSubclassesSupport) {
		this.methodsForSubclassesSupport = methodsForSubclassesSupport;
	}

	public boolean hasMethodsForSubclassesSupport() {
		return methodsForSubclassesSupport;
	}

	public void setSaveMemSupport(boolean saveMemSupport) {
		this.saveMemSupport = saveMemSupport;
	}

	public boolean hasSavememSupport() {
		return this.saveMemSupport;
	}
}
