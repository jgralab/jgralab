package de.uni_koblenz.jgralab.codegenerator;

/**
 * This class keeps the configurations of the code generator and is passed to
 * all instances. It keeps and manages the following configuration entries: -
 * <code>transactionSupport</code> toggles, if the classes for transaction
 * support should be created, disables by default -
 * <code>typespecificMethodsSupport</code> toggles, if the typespecific
 * methods such as "getNextXYVertex" should be created, enabled by default -
 * <code>methodsForSubclasseSupport</code> toggles, if the methods with an
 * additional subtype-flag like "getNextXYVertex(boolean withSubclasses)" should
 * be created. Needs typespecifigMethodsSupport to be enabled. Disabled by
 * default.
 * 
 * 
 */

public class CodeGeneratorConfiguration {

	public static final CodeGeneratorConfiguration WITHOUT_TRANSACTIONS = new CodeGeneratorConfiguration(
			false, true, false);

	public static final CodeGeneratorConfiguration FULL = new CodeGeneratorConfiguration(
			true, true, true);

	public static final CodeGeneratorConfiguration FULL_WITHOUT_SUBCLASS_FLAGS = new CodeGeneratorConfiguration(
			true, true, false);

	public static final CodeGeneratorConfiguration WITHOUT_TYPESPECIFIC_METHODS = new CodeGeneratorConfiguration(
			true, false, false);

	public static final CodeGeneratorConfiguration MINIMAL = new CodeGeneratorConfiguration(
			false, false, false);

	/** toggles, if the classes for transaction support should be created */
	private boolean transactionSupport = false;

	/**
	 * toggles, if the typespecific methods such as "getNextXYVertex" should be
	 * created
	 */
	private boolean typespecificMethodSupport = true;

	/**
	 * toggles, if the methods with an additional subtype-flag like
	 * "getNextXYVertex(boolean withSubclasses)" should be created. Needs
	 * typespecifigMethodsSupport to be enabled.
	 */
	private boolean methodsForSubclassesSupport = false;

	public void wantsToHaveTransactionSupport(boolean transactionSupport) {
		this.transactionSupport = transactionSupport;
	}

	public boolean hasTransactionSupport() {
		return transactionSupport;
	}

	public void wantsToHaveTypespecificMethodsSupport(
			boolean typespecificMethodSupport) {
		this.typespecificMethodSupport = typespecificMethodSupport;
	}

	public boolean hasTypespecificMethodsSupport() {
		return typespecificMethodSupport;
	}

	public void wantsToHaveMethodsForSubclassesSupport(
			boolean methodsForSubclassesSupport) {
		this.methodsForSubclassesSupport = methodsForSubclassesSupport;
	}

	public boolean hasMethodsForSubclassesSupport() {
		return methodsForSubclassesSupport;
	}

	/**
	 * copy constructor
	 * 
	 * @param other
	 */
	public CodeGeneratorConfiguration(CodeGeneratorConfiguration other) {
		this.transactionSupport = other.transactionSupport;
		this.typespecificMethodSupport = other.typespecificMethodSupport;
		this.methodsForSubclassesSupport = other.methodsForSubclassesSupport;
	}

	/**
	 * Default constructor, created a default configuration
	 */
	public CodeGeneratorConfiguration() {

	}

	public CodeGeneratorConfiguration(boolean transactionSupport,
			boolean typespecificMethodSupport,
			boolean methodsForSubclassesSupport) {
		this.transactionSupport = transactionSupport;
		this.typespecificMethodSupport = typespecificMethodSupport;
		this.methodsForSubclassesSupport = methodsForSubclassesSupport;
	}

}
