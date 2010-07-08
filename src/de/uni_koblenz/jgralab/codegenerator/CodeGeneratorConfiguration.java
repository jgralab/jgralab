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

	public static final CodeGeneratorConfiguration WITHOUT_TRANSACTIONS = new CodeGeneratorConfiguration();

	public static final CodeGeneratorConfiguration FULL = new CodeGeneratorConfiguration()
			.withTransactionSupport().withMethodsForSubclassesSupport()
			.withSaveMemSupport();

	public static final CodeGeneratorConfiguration FULL_WITHOUT_SUBCLASS_FLAGS = new CodeGeneratorConfiguration()
			.withTransactionSupport().withSaveMemSupport();

	public static final CodeGeneratorConfiguration WITHOUT_TYPESPECIFIC_METHODS = new CodeGeneratorConfiguration()
			.withTransactionSupport().withoutTypeSpecificMethodSupport();

	public static final CodeGeneratorConfiguration MINIMAL = new CodeGeneratorConfiguration()
			.withoutTypeSpecificMethodSupport();

	/** toggles, if the classes for standard support should be created */
	private boolean standardSupport = true;

	/** toggles, if the classes for transaction support should be created */
	private boolean transactionSupport = false;

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
	}

	public CodeGeneratorConfiguration withoutStandardSupport() {
		standardSupport = false;
		return this;
	}

	public CodeGeneratorConfiguration withTransactionSupport() {
		transactionSupport = true;
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
