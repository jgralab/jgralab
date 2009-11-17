package de.uni_koblenz.jgralab.codegenerator;

/**
 * This class keeps the configurations of the code generator and is passed to
 * all instances. It keeps and manages the following configuration entries:
 * 
 * - <code>standardSupport</code> toggles, if the classes for standard support
 * should be created, enabled by default
 * 
 * - <code>transactionSupport</code> toggles, if the classes for transaction
 * support should be created, enabled by default
 * 
 * - <code>typespecificMethodsSupport</code> toggles, if the typespecific
 * methods such as "getNextXYVertex" should be created, enabled by default
 * 
 * - <code>methodsForSubclasseSupport</code> toggles, if the methods with an
 * additional subtype-flag like "getNextXYVertex(boolean withSubclasses)" should
 * be created. Needs typeSpecifigMethodsSupport to be enabled. Disabled by
 * default.
 */
public class CodeGeneratorConfiguration {

	public static final CodeGeneratorConfiguration WITHOUT_TRANSACTIONS = new CodeGeneratorConfiguration(
			true, false, true, false);

	public static final CodeGeneratorConfiguration FULL = new CodeGeneratorConfiguration(
			true, true, true, true);

	public static final CodeGeneratorConfiguration FULL_WITHOUT_SUBCLASS_FLAGS = new CodeGeneratorConfiguration(
			true, true, true, false);

	public static final CodeGeneratorConfiguration WITHOUT_TYPESPECIFIC_METHODS = new CodeGeneratorConfiguration(
			true, true, false, false);

	public static final CodeGeneratorConfiguration MINIMAL = new CodeGeneratorConfiguration(
			true, false, false, false);

	/** toggles, if the classes for standard support should be created */
	private boolean standardSupport = true;

	/** toggles, if the classes for transaction support should be created */
	private boolean transactionSupport = true;

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

	/**
	 * copy constructor
	 * 
	 * @param other
	 */
	public CodeGeneratorConfiguration(CodeGeneratorConfiguration other) {
		this.standardSupport = other.standardSupport;
		this.transactionSupport = other.transactionSupport;
		this.typespecificMethodSupport = other.typespecificMethodSupport;
		this.methodsForSubclassesSupport = other.methodsForSubclassesSupport;
	}

	/**
	 * Default constructor, created a default configuration
	 */
	public CodeGeneratorConfiguration() {

	}

	public CodeGeneratorConfiguration(boolean standardSupport,
			boolean transactionSupport, boolean typespecificMethodSupport,
			boolean methodsForSubclassesSupport) {
		this.standardSupport = standardSupport;
		this.transactionSupport = transactionSupport;
		this.typespecificMethodSupport = typespecificMethodSupport;
		this.methodsForSubclassesSupport = methodsForSubclassesSupport;
	}

}
