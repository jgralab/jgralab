package de.uni_koblenz.jgralab.codegenerator;

/**
 * This class keeps the configurations of the code generator and is passed to
 * all instances. It keeps and manages the following configuration entries:
 * 
 * - <code>standardSupport</code> toggles, if the classes for standard support
 * should be created, enabled by default.<br><br>
 * 
 * - <code>transactionSupport</code> toggles, if the classes for transaction
 * support should be created, enabled by default.<br><br>
 * 
 * - <code>typespecificMethodsSupport</code> toggles, if the typespecific
 * methods such as "getNextXYVertex" should be created, enabled by default.<br><br>
 * 
 * - <code>methodsForSubclasseSupport</code> toggles, if the methods with an
 * additional subtype-flag like "getNextXYVertex(boolean withSubclasses)" should
 * be created. Needs typeSpecifigMethodsSupport to be enabled. Disabled by
 * default.<br><br>
 * 
 * - <code>saveMemSupport</code> toggles, if the memory saving classes should be
 * created, disabled by default.
 */
public class CodeGeneratorConfiguration {

	// TODO change constructor to not using this many boolean flags
	//      just use a parameterless constructior and methods
	//      like "withTransactionSupport()"
	//      expample: cgc = new CodeGeneratorConfiguration().withTransactionSupport().withoutStandardSupport();
	//      standard support is enabled by default.
	
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
	 * this.transactionSupport = true <br>
	 * this.typespecificMethodSupport = true <br>
	 * this.methodsForSubclassesSupport = false <br>
	 * this.saveMemSupport = false <br>
	 */
	public CodeGeneratorConfiguration() {

	}

	/**
	 * This constructor creates a new {@link CodeGeneratorConfiguration}
	 * instance. It especially allows to specify nearly all parameters available
	 * and makes use of the default memory setup, using doubly-linked lists.
	 * 
	 * @param standardSupport
	 * @param transactionSupport
	 * @param typespecificMethodSupport
	 * @param methodsForSubclassesSupport
	 */
	public CodeGeneratorConfiguration(boolean standardSupport,
			boolean transactionSupport, boolean typespecificMethodSupport,
			boolean methodsForSubclassesSupport) {
		this(standardSupport, transactionSupport, typespecificMethodSupport,
				methodsForSubclassesSupport, false);
	}

	/**
	 * This constructor creates a new {@link CodeGeneratorConfiguration}
	 * instance. It especially allows to specify all parameters available.
	 * 
	 * @param standardSupport
	 * @param transactionSupport
	 * @param typespecificMethodSupport
	 * @param methodsForSubclassesSupport
	 * @param saveMemSupport
	 *            Hint that the memory saving implementation shall be used.
	 */
	public CodeGeneratorConfiguration(boolean standardSupport,
			boolean transactionSupport, boolean typespecificMethodSupport,
			boolean methodsForSubclassesSupport, boolean saveMemSupport) {
		this.standardSupport = standardSupport;
		this.transactionSupport = transactionSupport;
		this.typespecificMethodSupport = typespecificMethodSupport;
		this.methodsForSubclassesSupport = methodsForSubclassesSupport;
		this.saveMemSupport = saveMemSupport;
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

	public boolean hasSaveMemSupport() {
		return this.saveMemSupport;
	}
}
