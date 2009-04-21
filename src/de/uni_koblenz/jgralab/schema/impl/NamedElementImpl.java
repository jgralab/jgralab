package de.uni_koblenz.jgralab.schema.impl;

import java.io.File;
import java.util.regex.Pattern;

import de.uni_koblenz.jgralab.schema.BasicDomain;
import de.uni_koblenz.jgralab.schema.CollectionDomain;
import de.uni_koblenz.jgralab.schema.GraphClass;
import de.uni_koblenz.jgralab.schema.MapDomain;
import de.uni_koblenz.jgralab.schema.NamedElement;
import de.uni_koblenz.jgralab.schema.Package;
import de.uni_koblenz.jgralab.schema.Schema;
import de.uni_koblenz.jgralab.schema.exception.InvalidNameException;
import de.uni_koblenz.jgralab.schema.exception.SchemaException;

public abstract class NamedElementImpl implements NamedElement {

	/**
	 * The package containing this named element.
	 */
	protected final Package parentPackage;

	/**
	 * The fully qualified name of an element in a schema. It is composed of the
	 * {@link #packageName name of the package} the element is located in and
	 * the {@link #simpleName simple name} of the element. <br/>
	 * <code>qualifiedName = packageName + "." + simpleName</code>
	 */
	protected final String qualifiedName;

	/**
	 * Unique name of an element in a package without the fully qualified
	 * package name.
	 */
	protected final String simpleName;

	/**
	 * The unique name of an element in a schema. If there is only one class in
	 * the schema with this short name, the unique name is the short name.
	 * Otherwise, the unique name is the same as the qualified name, except that
	 * all <code>'.'</code> are replaced by <code>'$'</code>characters.
	 */
	protected String uniqueName;

	private static final Pattern COLLECTION_OR_MAPDOMAIN_NAME_PATTERN = Pattern
			.compile("[.]?\\p{Upper}\\w*<[<>., _\\w]+>$");

	private static final Pattern PACKAGE_NAME_PATTERN = Pattern
			.compile("\\p{Lower}(\\w*\\p{Alnum})?");

	private static final Pattern ATTRELEM_OR_NOCOLLDOMAIN_PATTERN = Pattern
			.compile("\\p{Upper}(\\w*\\p{Alnum})?");

	/**
	 * Creates a new named element with the specified name and parent package.
	 *
	 * <p>
	 * <b>Pattern:</b>
	 * <code>namedElement = new NamedElementImpl(sn, pkg);</code>
	 * </p>
	 *
	 * <p>
	 * <b>Preconditions:</b>
	 * <ul>
	 * <li>The simple name is not empty, except if this named element is the
	 * <code>DefaultPackage</code> and the
	 * {@link de.uni_koblenz.jgralab.schema.Package.DEFAULTPACKAGE_NAME
	 * DEFAULTPACKAGE_NAME} is the empty string. In that case, the parent
	 * package must be <code>null</code>.</li>
	 * <li>The simple name starts with a letter.</li>
	 * <li>The following characters in the simple name are either alphanumeric
	 * or the '_' symbol. List-/Map-/Set-Domain may also contain '<>.,'
	 * characters.</li>
	 * <li>The simple name ends with an alphanumeric character, or in the case
	 * of a List-/Map-/Set-Domain with a '>' character.</li>
	 * <li>The qualified name, made of the package name and the simple name,
	 * must differ from any other elementÂ´s name in the schema.</li>
	 * <li>The simple name of Package-instances starts with a small letter.</li>
	 * <li>The simple name of Domain-/AttributedElementClass-instances starts
	 * with a capital letter.</li>
	 * <li>The parent package is not <code>null</code>, except if this named
	 * element is the <code>DefaultPackage</code>.</li>
	 * </ul>
	 * </p>
	 *
	 * <p>
	 * <b>Postconditions:</b>
	 * <ul>
	 * <li><code>namedElement'.package</code> has one the following values:
	 * <ul>
	 * <li><code>null</code> if <code>namedElement</code> is the
	 * <code>DefaultPackage</code></li>
	 * <li>a valid (not <code>null</code>) parent package</li>
	 * <li>
	 * </ul>
	 * </li>
	 * <li><code>namedElement'.qualifiedName</code> has one of the following values:
	 * <ul>
	 * <li>if <code>namedElement</code> is the <code>DefaultPackage</code>, then
	 * it equals the <code>simpleName</code></li>
	 * <li>in any other case, it is a composition of the
	 * <code>packageName</code>, a '.' character and the <code>simpleName</code>
	 * . Formally: <code>qualifiedName = packageName + "." + simpleName</li>
	 * </ul>
	 * </li>
	 * <li><code>namedElement'.simpleName</code> has one of the following values:
	 * <ul>
	 * <li>it equals
	 * {@link de.uni_koblenz.jgralab.schema.Package.DEFAULTPACKAGE_NAME
	 * DEFAULTPACKAGE_NAME} if <code>namedElement'</code> represents the
	 * <code>DefaultPackage</code></li>
	 * <li>any other valid value</li>
	 * </ul>
	 * </li>
	 * <li><code>namedElement'.uniqueName</code> has one of the following values:
	 * <ul>
	 * <li>it equals <code>namedElement'.simpleName</code> if there is no other named element with
	 * this <code>simpleName</code> in the containing schema</li>
	 * <li>it equals the composition of the package name and
	 * <code>simpleName</code>, with all '.' characters replaced by '$'
	 * characters, if there is another named element with the same
	 * <code>simpleName</code> in the containing schema.</li>
	 * </ul>
	 * </ul>
	 * </p>
	 *
	 * @param pkg
	 *            the package containing this named element
	 * @param simpleName
	 *            this named elementÂ´s simple name
	 * @throws InvalidNameException
	 *             if:
	 *             <ul>
	 *             <li>the simple name does not meet the required format (see
	 *             preconditions)</li>
	 *             <li>the simple name is a reserved Java word</li>
	 *             <li>the element is a Package-instance and the parent package
	 *             is <code>null</code>, but the simple name is not the
	 *             {@link de.uni_koblenz.jgralab.schema.Package.DEFAULTPACKAGE_NAME
	 *             DEFAULTPACKAGE_NAME}</li>
	 *             <li>the simple name is empty, for any other element then the
	 *             <code>DefaultPackage</code></li>
	 *             </ul>
	 * @throws SchemaException
	 *             if:
	 *             <ul>
	 *             <li>the element is of any other type then
	 *             <code>Package</code> and the parent package is
	 *             <code>null</code></li>
	 *             <li>the element is a
	 *             {@link de.uni_koblenz.jgralab.schema.BasicDomain BasicDomain}
	 *             , a {@link de.uni_koblenz.jgralab.schema.CollectionDomain
	 *             CollectionDomain} or a
	 *             {@link de.uni_koblenz.jgralab.schema.GraphClass GraphClass}
	 *             and the parent package is not the <code>DefaultPackage</code>
	 *             </li>
	 *             <li>there is already an element in the containing schema,
	 *             that has the exact same qualified name</li>
	 *             </ul>
	 */
	protected NamedElementImpl(String simpleName, Package pkg, Schema schema) {
		/*
		 * An empty (null) parent package is only allowed for the
		 * DefaultPackage.
		 */
		if (pkg == null) {
			/*
			 * The DefaultPackage must have the predefined standart simple name,
			 * and the schema in which it is created must not already contain a
			 * DefaultPackage.
			 */
			if (simpleName.equals(Package.DEFAULTPACKAGE_NAME)
					&& this instanceof PackageImpl
					&& schema.getDefaultPackage() == null) {
				this.qualifiedName = Package.DEFAULTPACKAGE_NAME;
				this.parentPackage = null;
				this.simpleName = Package.DEFAULTPACKAGE_NAME;
				this.uniqueName = Package.DEFAULTPACKAGE_NAME;
				return;
			} else {
				throw new SchemaException("Cannot create the element '"
						+ simpleName + "' cause no parent package was given.");

			}
		}
		this.parentPackage = pkg;

		/*
		 * The simple name must not be empty (except for the DefaultPackage).
		 * The simple name must start with a letter (expect for
		 * Map-/Set-/List-/Collection-Domains which may start with a '.'). Any
		 * following character must be alphanumeric and/or a '_' character
		 * (Composite-/EnumDomain simple names may also have '.<>,' characters).
		 * The simple name must end with an alphanumeric character.
		 *
		 * Simple names of Domains & AttributedElements start with a capital
		 * letter, whereas the simple name for a Package starts with a small
		 * letter.
		 */
		if (this instanceof CollectionDomain || this instanceof MapDomain) {
			if (!COLLECTION_OR_MAPDOMAIN_NAME_PATTERN.matcher(simpleName)
					.matches()) {
				throw new InvalidNameException(
						"Invalid simpleName for Collection- or MapDomain '"
								+ simpleName
								+ "': The simple name must not be empty. "
								+ "The simple name must start with a uppercase letter. "
								+ "Any following character must be alphanumeric or a '_' character (List-/Map-/Set-Domain simple names may also have '.<>,' characters).");
			}
		} else if (this instanceof Package) {
			if (!PACKAGE_NAME_PATTERN.matcher(simpleName).matches()) {
				throw new InvalidNameException(
						"Invalid simpleName for Package '"
								+ simpleName
								+ "': The simple name must start with a small letter. "
								+ "Any following character must be alphanumeric and/or a '_' character. "
								+ "The simple name must end with an alphanumeric character.");
			}
		} else if (!ATTRELEM_OR_NOCOLLDOMAIN_PATTERN.matcher(simpleName)
				.matches()) {
			throw new InvalidNameException(
					"Invalid simpleName for AttributedElementClass or Domain '"
							+ simpleName
							+ "': The simple name must not be empty. "
							+ "The simple name must start with a letter. "
							+ "Any following character must be alphanumeric and/or a '_' character (List-/Map-/Set-Domain simple names may also have '.<>,' characters). "
							+ "The simple name must end with an alphanumeric character.");
		}

		/*
		 * Words that are reserved by Java itself are not allowed as element
		 * names.
		 */
		if (SchemaImpl.RESERVED_JAVA_WORDS.contains(simpleName)) {
			throw new InvalidNameException("Invalid simpleName '" + simpleName
					+ "': The simple name must not be a reserved Java word.");
		}

		this.simpleName = simpleName;

		/*
		 * The qualifiedName is made of: packageName + "." + simpleName In the
		 * event that this element is directly contained in the DefaultPackage,
		 * the qualifiedName equals the simpleName.
		 */
		qualifiedName = ((pkg == null || pkg.getQualifiedName().equals(
				Package.DEFAULTPACKAGE_NAME)) ? "" : pkg.getQualifiedName()
				+ ".")
				+ simpleName;

		/*
		 * The package for Basic-/Map-/Collection-Domains (List, Set) and
		 * GraphClass must be the DefaultPackage.
		 */
		if (this instanceof BasicDomain || this instanceof CollectionDomain
				|| this instanceof MapDomain || this instanceof GraphClass) {
			if (!isInDefaultPackage()) {
				throw new SchemaException(
						"Invalid parent package '"
								+ pkg.getQualifiedName()
								+ "'.\n"
								+ "The parent package for BasicDomains (Boolean, Double, Integer, Long, String),\n"
								+ "CollectionDomains (List, Set) and GraphClasses must be the DefaultPackage.");
			}
		}

		/*
		 * Check if there already is a named element with the same qualified
		 * name in the schema.
		 */
		if (schema.knows(qualifiedName)) {
			throw new SchemaException(
					"The Schema already contains a named element with qualified name '"
							+ qualifiedName + "'.");
		}

		/*
		 * If the unique name is in use, then addToKnownElements() will change
		 * it.
		 */
		uniqueName = simpleName;
		((SchemaImpl) schema).addToKnownElements(this);
	}

	/**
	 * Register this named element wherever it has to be known.
	 *
	 * For example, a package has to be added as subpackage of its parent
	 * package and to the schema; the same holds for domains.
	 *
	 * A vertex class has to add itself to the graph class and the package; same
	 * holds for edge classes (+ subclasses).
	 */
	protected abstract void register();

	/**
	 * This method is invoked on one or more element's bearing the same unique
	 * name, when a new element is added to the schema.
	 *
	 * The unique name is changed to match the qualified name, with all '.'
	 * replaced by '$' characters.
	 */
	final void changeUniqueName() {
		uniqueName = toUniqueNameNotation(qualifiedName);
	}

	@Override
	public final String getFileName() {
		return qualifiedName.replace('.', File.separatorChar);
	}

	@Override
	public final Package getPackage() {
		return parentPackage;
	}

	@Override
	public final String getPackageName() {
		return parentPackage != null ? parentPackage.getQualifiedName() : null;
	}

	@Override
	public final String getPathName() {
		return parentPackage != null ? getPackageName().replace('.',
				File.separatorChar) : null;
	}

	@Override
	public final String getQualifiedName() {
		return qualifiedName;
	}

	@Override
	public final String getQualifiedName(Package pkg) {
		if (this.parentPackage == pkg) {
			return simpleName;
		} else if (this.parentPackage.isDefaultPackage()) {
			return Package.DEFAULTPACKAGE_NAME + "." + simpleName;
		} else {
			return qualifiedName;
		}
	}

	@Override
	public Schema getSchema() {
		assert parentPackage != null : "There's no parent package!";
		return parentPackage.getSchema();
	}

	@Override
	public final String getSimpleName() {
		return simpleName;
	}

	@Override
	public final String getUniqueName() {
		return uniqueName;
	}

	/**
	 * Returns a hash code value for this named element, based upon itÂ´s
	 * qualified name.
	 *
	 * <p>
	 * <b>Pattern:</b> <code>hash = namedElement.hashCode();</code>
	 * </p>
	 *
	 * <p>
	 * <b>Preconditions:</b> none
	 * </p>
	 *
	 * <p>
	 * <b>Postconditions:</b> <code>hash</code> is the hash code of
	 * <code>namedElement.qualifiedName</code>.<br />
	 * It is computed as described {@link java.lang.String#hashCode() here}, and
	 * underlies the same rules as described {@link java.lang.Object#hashCode()
	 * here}.
	 * </p>
	 *
	 * @return a hash code value for this named element
	 *
	 * @see java.lang.Object#hashCode()
	 * @see java.lang.String#hashCode()
	 */
	@Override
	public final int hashCode() {
		return qualifiedName.hashCode();
	}

	@Override
	public final boolean isInDefaultPackage() {
		return parentPackage != null && parentPackage.isDefaultPackage();
	}

	/**
	 * Transforms a qualified name into unique name notation. This is achieved
	 * by replacing every occurrence of the <code>'.'</code> character in the
	 * given qualified name by a<code>'$'</code> character.
	 *
	 * <p>
	 * <b>Pattern:</b> <code>un = NamedElementImpl.toUniqueName(qn);</code>
	 * </p>
	 *
	 * <p>
	 * <b>Preconditions:</b> none
	 * </p>
	 *
	 * <p>
	 * <b>Postconditions:</b> <code>un</code> equals <code>qn</code>, except
	 * every occurrence of '.' by '$'
	 * </p>
	 *
	 * @param qualifiedName
	 *            the qualified name to convert to unique name notation
	 *
	 * @return the unique name derived from a given qualified name
	 */
	public static String toUniqueNameNotation(String qualifiedName) {
		return qualifiedName.replace('.', '$');
	}
}
