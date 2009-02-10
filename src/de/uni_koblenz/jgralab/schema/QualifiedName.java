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

package de.uni_koblenz.jgralab.schema;

import java.io.File;

import de.uni_koblenz.jgralab.schema.exception.InvalidNameException;
import de.uni_koblenz.jgralab.schema.exception.SchemaException;

/**
 * QualifiedName is used to identify any Schema element (Graph/Vertex/Edge
 * classes, Packages, Domains, etc.).
 * 
 * <p>
 * A <code>QualifiedName</code> stores informations about an element´s name and
 * package:
 * <ul>
 * <li><code>packageName</code> the fully qualified name of the package the
 * element is located in.</li>
 * <li><code>simpleName</code> unique name of the element in the package without
 * the fully qualified package name</li>
 * <li><code>qualifiedName</code> the fully qualified name of an element in the
 * schema<br />
 * <code>qualifiedName = packageName + "." + simpleName</code></li>
 * <li><code>uniqueName</code> the unique name of the element in the schema.<br />
 * If there is only one class in the schema with this short name, the unique
 * name is the short name. Otherwise, the unique name is the same as the
 * qualified name, with the exception that packages are separated with
 * <code>'_'</code> instead of '.' characters.</li>
 * </ul>
 * </p>
 * 
 * <p>
 * <b>Note:</b> in the following, <code>qname</code>, and <code>qname'</code> , will
 * represent the states of the given <code>QualifiedName</code> before,
 * respectively after, any operation.
 * </p>
 * 
 * <p>
 * <b>Note:</b> in the following it is understood that method arguments differ
 * from <code>null</code>. Therefore there will be no preconditions addressing
 * this matter.
 * </p>
 * 
 * @author ist@uni-koblenz.de
 */
public class QualifiedName implements Comparable<QualifiedName> {
	/**
	 * The fully qualified name of the package an element is located in.
	 */
	private String packageName;

	/**
	 * Unique name of an element in a package without the fully qualified
	 * package name.
	 */
	private String simpleName;

	/**
	 * The fully qualified name of an element in a schema. It is composed of the
	 * {@link #packageName name of the package} the element is located in and
	 * the {@link #simpleName simple name} of the element. <br />
	 * <code>qualifiedName = packageName + "." + simpleName</code>
	 */
	private String qualifiedName;

	/**
	 * The unique name of an element in a schema. If there is only one class in
	 * the schema with this short name, the unique name is the short name.
	 * Otherwise, the unique name is the same as the qualified name, except that
	 * all <code>'.'</code> are replaced by <code>'_'</code> characters.
	 */
	private String uniqueName;

	/**
	 * Creates a new instance of <code>QualifiedName</code> with a given
	 * qualified name.
	 * 
	 * <p>
	 * <b>Pattern:</b> <code>qname = new getQualifiedName(qn);</code>
	 * </p>
	 * 
	 * <p>
	 * <b>Preconditions:</b> none<br />
	 * It is recommended that the part of <code>qn</code> representing the
	 * simple name should not be empty.
	 * </p>
	 * 
	 * <p>
	 * <b>Postconditions:</b>
	 * <ul>
	 * <li><code>qname'.packageName</code> has one the following values:
	 * <ul>
	 * <li>empty String if one of the following occurs:
	 * <ul>
	 * <li><code>qn</code> represents an element of type
	 * List<...>|Map<...>|Set<...>, which does not have a package name, as it is
	 * a mere composition of other elements</li>
	 * <li>the element with this <code>QualifiedName</code> is not contained in
	 * any package</li>
	 * <li><code>qn</code> is an empty String, representing a
	 * <code>QualifiedName</code> for an element not contained in any package
	 * and without a simple name</li>
	 * </ul>
	 * </li>
	 * <li><code>qn.substring(0, qn.lastIndexOf("."))</code>, the fully
	 * qualified name of the package the element is located in.<br />
	 * The last "." character seperates the <code>packageName</code> from the
	 * <code>simpleName</code>.</li>
	 * </ul>
	 * </li>
	 * <li><code>qname'.simpleName</code> has one of the following values:</li>
	 * <ul>
	 * <li>empty String if one of the following occurs:</li>
	 * <ul>
	 * <li>the element with this <code>QualifiedName</code> does not have a
	 * simple name</li>
	 * <li><code>qn</code> is an empty String, representing a
	 * <code>QualifiedName</code> for an element not contained in any package
	 * and without a simple name</li>
	 * </ul>
	 * <li>
	 * <code>(subString =)qn.substring(qn.lastIndexOf(".") + 1) && subString.length > 0</code>
	 * , the unique name of an element in a package without the fully qualified
	 * package name</li>
	 * </ul>
	 * <li><code>qname'.qualifiedName</code> has one of the following values:</li>
	 * <ul>
	 * <li>empty String, if <code>qn</code> is an empty String, representing a
	 * <code>QualifiedName</code> for an element not contained in any package
	 * and without a simple name</li>
	 * <li>equals <code>simpleName</code>, representing a
	 * <code>QualifiedName</code> for an element not contained in any package.<br />
	 * Implies that: <code>qname'.packageName.equals("")</code></li>
	 * <li>equals <code>qn</code>, representing a <code>QualifiendName</code>
	 * for an element contained in a package and which has a simple name.</li>
	 * </ul>
	 * <li><code>qname'.uniqueName.equals(qname'.simpleName)</code><br />
	 * This holds for as long as there is only one class in the schema with this
	 * short name, which is the case upon instantiation. Otherwise, the unique
	 * name is the same as the qualified name with all <code>'.'</code>
	 * characters replaced by <code>'_'</code>.</li>
	 * </ul>
	 * </p>
	 * 
	 * @param qn
	 *            an element´s fully qualified name
	 */
	public QualifiedName(String qn) {
		if (qn.startsWith("List<") || qn.startsWith("Set<")
				|| qn.startsWith("Map<")) {
			setPackageName("");
			simpleName = qn;
			qualifiedName = qn;
		} else {
			int p = qn.lastIndexOf(".");
			if (p >= 0) {
				packageName = qn.substring(0, p);
				simpleName = qn.substring(p + 1);
				qualifiedName = p == 0 ? simpleName : qn;
			} else {
				setPackageName("");
				simpleName = qn;
				qualifiedName = qn;
			}
		}
		uniqueName = simpleName;
	}

	/**
	 * Creates a new instance of <code>QualifiedName</code> with a given package
	 * name and simple name.
	 * 
	 * <p>
	 * <b>Pattern:</b> <code>qname = new getQualifiedName(pn, sn);</code>
	 * </p>
	 * 
	 * <p>
	 * <b>Preconditions:</b> none<br />
	 * It is recommended that the short name <code>sn</code> should not be
	 * empty.
	 * </p>
	 * 
	 * <p>
	 * <b>Postconditions:</b>
	 * <ul>
	 * <li><code>qname'.packageName == pn</code></li>
	 * <li><code>qname'.simpleName == sn</code></li>
	 * <li><code>qname'.qualifiedName</code> has one of the following values:
	 * <ul>
	 * <li>sn</code>, if the package name <code>pn</code> is empty</li>
	 * <li><code>pn + "." + sn</code>, if the package name <code>pn</code> is
	 * not empty</li>
	 * </ul>
	 * </li>
	 * <li><code>qname'.uniqueName == sn</code></li><br />
	 * <br />
	 * For more extensive explanations/postconditions see
	 * {@link #QualifiedName(String) QualifiedName(String)}.
	 * </ul>
	 * </p>
	 * 
	 * @param pn
	 *            an element´s fully qualified package
	 * @param sn
	 *            an element´s simple name
	 * 
	 * @see #QualifiedName(String)
	 */
	public QualifiedName(String pn, String sn) {
		packageName = pn;
		simpleName = sn;
		qualifiedName = (pn.length() == 0) ? simpleName : packageName + "."
				+ simpleName;
		uniqueName = simpleName;
	}

	/**
	 * Textually represents this <code>QualifiedName</code>.
	 * 
	 * <p>
	 * <b>Pattern:</b> <code>str = qname.toString();</code>
	 * </p>
	 * 
	 * <p>
	 * <b>Preconditions:</b> none
	 * </p>
	 * 
	 * <p>
	 * <b>Postconditions:</b> <code>str == qname.qualifiedName</code>
	 * </p>
	 * 
	 * @return a textual representation of this <code>QualifiedName</code>
	 */
	@Override
	public String toString() {
		return qualifiedName;
	}

	/**
	 * Sets the package name for the element with this
	 * <code>QualifiedName</code>.<br />
	 * In addition, the qualified name is changed accordingly.
	 * 
	 * <p>
	 * <b>Pattern:</b> <code>qname.setPackageName(pn);</code>
	 * </p>
	 * 
	 * <p>
	 * <b>Preconditions:</b> none
	 * </p>
	 * 
	 * <p>
	 * <b>Postconditions:</b>
	 * <ul>
	 * <li><code>qname'.packageName == pn</code></li>
	 * <li><code>qname'.qualifiedName == pn + "." + qname.simpleName</code></li>
	 * </ul>
	 * </p>
	 * 
	 * @param pn
	 *            the new package name for this <code>QualifiedName</code>
	 */
	public void setPackageName(String pn) {
		packageName = pn;
		qualifiedName = (pn.length() == 0) ? simpleName : packageName + "."
				+ simpleName;
	}

	/**
	 * Returns the simple name of the element with this
	 * <code>QualifiedName</code>.
	 * 
	 * <p>
	 * <b>Pattern:</b> <code>sn = qname.getSimpleName();</code>
	 * </p>
	 * 
	 * <p>
	 * <b>Preconditions:</b> none
	 * </p>
	 * 
	 * <p>
	 * <b>Postconditions:</b> <code>sn == qname.simpleName</code>
	 * </p>
	 * 
	 * @return the simple name of this <code>QualifiedName</code>
	 */
	public String getSimpleName() {
		return simpleName;
	}

	/**
	 * Returns the name of the package holding the element with this
	 * <code>QualifiedName</code>.
	 * 
	 * <p>
	 * <b>Pattern:</b> <code>pn = qname.getPackageName();</code>
	 * </p>
	 * 
	 * <p>
	 * <b>Preconditions:</b> none
	 * </p>
	 * 
	 * <p>
	 * <b>Postconditions:</b> <code>pn == qname.packageName</code>
	 * </p>
	 * 
	 * @return the name of the package holding the element with this
	 *         <code>QualifiedName</code>
	 */
	public String getPackageName() {
		return packageName;
	}

	/**
	 * 
	 * Returns the qualified name of the element with this
	 * <code>QualifiedName</code>.
	 * 
	 * <p>
	 * <b>Pattern:</b> <code>qn = qname.getQualifiedName();</code>
	 * </p>
	 * 
	 * <p>
	 * <b>Preconditions:</b> none
	 * </p>
	 * 
	 * <p>
	 * <b>Postconditions:</b> <code>qn == qname.qualifiedName</code>
	 * </p>
	 * 
	 * @return the fully qualified name of the element with this
	 *         <code>QualifiedName</code>
	 */
	public String getQualifiedName() {
		return qualifiedName;
	}

	/**
	 * Returns the full path to the element with this <code>QualifiedName</code>
	 * . The name of the file is included at the end of the directory path.
	 * 
	 * <p>
	 * <b>Note:</b> Calling this method for element´s with qualified names of
	 * <code>Map<...>|Set<...>|List<...></code> type is not recommended, as
	 * these are no actual files but mere compositions of other files and thus
	 * do not have a file name and path.
	 * </p>
	 * 
	 * <p>
	 * <b>Pattern:</b> <code>fn = qname.getFileName();</code>
	 * </p>
	 * 
	 * <p>
	 * <b>Preconditions:</b> none<br/>
	 * </p>
	 * 
	 * <p>
	 * <b>Postconditions:</b> the file name <code>fn</code> is represented by
	 * the qualified name, where every <code>'.'</code>-character has been
	 * replaced by the default separator character depending on the execution
	 * environment.
	 * </p>
	 * 
	 * @return the full path to the element with this <code>QualifiedName</code>
	 */
	public String getFileName() {
		return packageName.replace('.', File.separatorChar)
				+ File.separatorChar + simpleName;
	}

	/**
	 * 
	 * Returns the qualified name of the element with this
	 * <code>QualifiedName</code>.
	 * 
	 * <p>
	 * <b>Pattern:</b> <code>name = qname.getName();</code>
	 * </p>
	 * 
	 * <p>
	 * <b>Preconditions:</b> none
	 * </p>
	 * 
	 * <p>
	 * <b>Postconditions:</b> <code>name == qname.qualifiedName</code>
	 * </p>
	 * 
	 * @return the fully qualified name of the element with this
	 *         <code>QualifiedName</code>
	 */
	public String getName() {
		return qualifiedName;
	}

	/**
	 * Returns the full path name of the directory holding the element with this
	 * <code>QualifiedName</code>.
	 * 
	 * <p>
	 * <b>Note:</b> Calling this method for element´s with qualified names of
	 * <code>Map<...>|Set<...>|List<...></code> type is not recommended, as
	 * these are no actual files but mere compositions of other files and thus
	 * do not have a file name and path.
	 * </p>
	 * 
	 * <p>
	 * <b>Pattern:</b> <code>pathName = qname.getPathName();</code>
	 * </p>
	 * 
	 * <p>
	 * <b>Preconditions:</b> none
	 * </p>
	 * 
	 * <p>
	 * <b>Postconditions:</b> the <code>pathName</code> is represented by the
	 * package name, where every <code>'.'</code>-character has been replaced by
	 * the default separator character depending on the execution environment.
	 * </p>
	 * 
	 * @return the fully qualified path name to the directory holding the
	 *         element with this <code>QualifiedName</code>
	 */
	public String getPathName() {
		return packageName.replace('.', File.separatorChar);
	}

	/**
	 * Returns the unique name of the element with this
	 * <code>QualifiedName</code>.
	 * 
	 * <p>
	 * This name is, as the qualifiedName is, unique in the whole Schema but
	 * much shorter than the qualified one. For instance, if there is only one
	 * class with simple name "X" in the whole schema, the unique name of this
	 * class can also be "X" without the package prefix.
	 * </p>
	 * 
	 * <p>
	 * <b>Note:</b> Keep in mind, that if the simple name is not unqiue in the
	 * Schema, then the unique name is the qualified name with <code>'_'</code>
	 * characters instead of <code>'.'</code> separating the different
	 * subpackages.
	 * </p>
	 * 
	 * <p>
	 * <b>Pattern:</b> <code>un = qname.getUniqueName();</code>
	 * </p>
	 * 
	 * <p>
	 * <b>Preconditions:</b> none
	 * </p>
	 * 
	 * <p>
	 * <b>Postconditions:</b> <code>un == qname.uniqueName</code>, where the
	 * unique name is one of the following:
	 * <ul>
	 * <li>unique name equals simple name, if there is only one class with this
	 * simple name in the Schema</li>
	 * <li>unique name equals qualified name, if there is another class with
	 * this simple name in the Schema</li>
	 * </ul>
	 * </p>
	 * 
	 * @return the unique name of the element with this
	 *         <code>QualifiedName</code>
	 */
	public String getUniqueName() {
		return uniqueName;
	}

	/**
	 * Sets the unique name of this QualifiedName. To ensure that the name is
	 * really unique in the whole schema, the element which should have this
	 * unique name needs to be specified. It there is any other element in the
	 * Schema which owns the same unique name, a SchemaException is thrown.
	 * 
	 * <p>
	 * <b>Pattern:</b> <code>qname.setUniqueName(e, un);</code>
	 * </p>
	 * 
	 * <p>
	 * <b>Preconditions:</b> <code>un</code> contains no <code>'.'</code>
	 * characters and thus has no package information
	 * </p>
	 * 
	 * <p>
	 * <b>Postconditions:</b> <code>qname'.uniqueName == un</code>, if <code>un</code> really is unique
	 * in the Schema
	 * </p>
	 * 
	 * @param element
	 *            the element which should be identified with the unique name
	 * @param uniqueName
	 *            the new uniqueName of the element
	 * @throws SchemaException
	 *             if the uniqueName is not unique, i.e. if there is any other
	 *             element in the Schema with the same unique name
	 */
	public void setUniqueName(NamedElement element, String uniqueName) {
		if (!(uniqueName.indexOf('.') < 0)) {
			throw new InvalidNameException("The unique name " + uniqueName
					+ " must not contain '.'.");
		}
		// Must be 1 because 0 is the default graph-class Graph
		for (GraphClass gc : element.getSchema()
				.getGraphClassesInTopologicalOrder()) {
			if (gc != element) {
				if (gc.getUniqueName().equals(uniqueName)) {
					throw new InvalidNameException("The unique name "
							+ uniqueName + " is already used in this schema");
				}
			}
			for (VertexClass v : gc.getVertexClasses()) {
				if (v != element) {
					if (v.getSimpleName().equals(uniqueName)) {
						throw new InvalidNameException("The unique name "
								+ uniqueName
								+ " is already used in this schema");
					}
				}
			}
			for (EdgeClass e : gc.getEdgeClasses()) {
				if (e != element) {
					if (e.getSimpleName().equals(uniqueName)) {
						throw new InvalidNameException("The unique name "
								+ uniqueName
								+ " is already used in this schema");
					}
				}
			}
			for (AggregationClass e : gc.getAggregationClasses()) {
				if (e != element) {
					if (e.getSimpleName().equals(uniqueName)) {
						throw new InvalidNameException("The unique name "
								+ uniqueName
								+ " is already used in this schema");
					}
				}
			}
			for (CompositionClass e : gc.getCompositionClasses()) {
				if (e != element) {
					if (e.getSimpleName().equals(uniqueName)) {
						throw new InvalidNameException("The unique name "
								+ uniqueName
								+ " is already used in this schema");
					}
				}
			}
		}
		for (Domain e : element.getSchema().getDomains().values()) {
			if (e != element) {
				if (e.getSimpleName().equals(uniqueName)) {
					throw new InvalidNameException("The unique name "
							+ uniqueName + " is already used in this schema");
				}
			}
		}
		this.uniqueName = uniqueName;
	}

	/**
	 * Indicates whether some other object is "equal to" this
	 * <code>QualifiedName</code>. Apart testing if both objects refer to the
	 * same object, it is checked if both have the same qualified name.
	 * 
	 * <p>
	 * <b>Pattern:</b> <code>isEqual = qname.equals(obj);</code>
	 * </p>
	 * 
	 * <p>
	 * <b>Preconditions:</b> none
	 * </p>
	 * 
	 * <p>
	 * <b>Postconditions:</b> <code>isEqual</code> is:
	 * <ul>
	 * <li><code>true</code>, whether if:
	 * <ul>
	 * <li><code>qname</code> and <code>obj</code> represent the same object.<br />
	 * Formally: qname == obj</li>
	 * <li>or if <code>obj</code> is another instance of
	 * <code>QualifiedName</code> but with the exact same qualified name</li>
	 * </ul>
	 * </li>
	 * <li><code>false</code> if none of the above conditions are met</li>
	 * </ul>
	 * </p>
	 * 
	 * @return <code>true</code> if both objects refer to the same object or
	 *         have the same qualified name; else <code>false</code>.
	 * 
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		return (this == obj)
				|| ((obj instanceof QualifiedName) && qualifiedName
						.equals(((QualifiedName) obj).qualifiedName));
	}

	/**
	 * Returns a hash code value for the <code>QualifiedName</code> object,
	 * based upon it´s qualified name.
	 * 
	 * <p>
	 * <b>Pattern:</b> <code>hash = qname.hashCode();</code>
	 * </p>
	 * 
	 * <p>
	 * <b>Preconditions:</b> none
	 * </p>
	 * 
	 * <p>
	 * <b>Postconditions:</b> <code>hash</code> is the hash code of
	 * <code>qname.qualifiedName</code>.<br />
	 * It is computed as described {@link java.lang.String#hashCode() here}, and
	 * underlies the same rules as described {@link java.lang.Object#hashCode()
	 * here}.
	 * </p>
	 * 
	 * @return a hash code value for this <code>QualifiedName</code>.
	 * 
	 * @see java.lang.Object#hashCode()
	 * @see java.lang.String#hashCode()
	 */
	@Override
	public int hashCode() {
		return qualifiedName.hashCode();
	}

	/**
	 * Compares this <code>QualifiedName</code> to another one, by comparing
	 * their qualified names lexicographically.
	 * 
	 * <p>
	 * <b>Pattern:</b> <code>comp = qname.compareTo(other);</code>
	 * </p>
	 * 
	 * <p>
	 * <b>Preconditions:</b> none
	 * </p>
	 * 
	 * <p>
	 * <b>Postconditions:</b>
	 * <ul>
	 * <li><code>comp < 0</code>, if this <code>QualifiedName´s</code> qualified
	 * name is lexicographically less than the <code>other</code>
	 * <code>QualifiedName´s</code> qualified name</li>
	 * <li><code>comp == 0</code>, if both <code>QualifiedNames´</code>
	 * qualified names are lexicographically equal</li>
	 * <li><code>comp > 0</code>, if this <code>QualifiedName´s</code> qualified
	 * name is lexicographically less than the <code>other</code>
	 * <code>QualifiedName´s</code> qualified name</li>
	 * </ul>
	 * </p>
	 * 
	 * @return the result of the lexicographical comparison
	 * 
	 * @see java.lang.Comparable#compareTo(Object)
	 * @see java.lang.String#compareTo(String)
	 */
	@Override
	public int compareTo(QualifiedName o) {
		return qualifiedName.compareTo(o.qualifiedName);
	}

	/**
	 * Indicates if this <code>QualifiedName</code> is simple. <br />
	 * An element bearing this <code>QualifiedName</code> is called simple, if
	 * is not contained in any package. Thus, the package name for this
	 * <code>QualifiedName</code> is empty.
	 * 
	 * <p>
	 * <b>Pattern:</b> <code>isSimple = qname.isSimple();</code>
	 * </p>
	 * 
	 * <p>
	 * <b>Preconditions:</b> none
	 * </p>
	 * 
	 * <p>
	 * <b>Postconditions:</b> <code>isSimple</code> is:
	 * <ul>
	 * <li><code>true</code>, if the element with this
	 * <code>QualifiedName</code> lays in no package</li>
	 * <li><code>false</code> if the above is not the case</li>
	 * </ul>
	 * </p>
	 * 
	 * @return <code>true</code> if this <code>QualifiedName</code> is simple;
	 *         <code>false</code> else
	 * 
	 * @see #isQualified()
	 */
	public boolean isSimple() {
		return packageName.length() == 0;
	}

	/**
	 * Indicates if this <code>QualifiedName</code> is qualified. <br />
	 * An element bearing this <code>QualifiedName</code> is called qualified,
	 * if is contained in a package. Thus, the package name for this
	 * <code>QualifiedName</code> is <u>not</u> empty.
	 * 
	 * <p>
	 * <b>Pattern:</b> <code>isSimple = qname.isSimple();</code>
	 * </p>
	 * 
	 * <p>
	 * <b>Preconditions:</b> none
	 * </p>
	 * 
	 * <p>
	 * <b>Postconditions:</b> <code>isSimple</code> is:
	 * <ul>
	 * <li><code>true</code>, if the element with this
	 * <code>QualifiedName</code> lays in no package</li>
	 * <li><code>false</code> if the above is not the case</li>
	 * </ul>
	 * </p>
	 * 
	 * @return <code>true</code> if this <code>QualifiedName</code> is simple;
	 *         <code>false</code> else
	 * 
	 * @see #isSimple()
	 */
	public boolean isQualified() {
		return packageName.length() != 0;
	}

	/**
	 * Transforms a given qualified name to a unique name. <br />
	 * This is achieved by replacing every occurence of the <code>'.'</code>
	 * character in the given qualified name by <code>'_'</code> characters.
	 * 
	 * <p>
	 * <b>Pattern:</b> <code>un = qname.toUniqueName(qn);</code>
	 * </p>
	 * 
	 * <p>
	 * <b>Preconditions:</b> none
	 * </p>
	 * 
	 * <p>
	 * <b>Postconditions:</b> <code>un</code> has one the following content:
	 * <ul>
	 * <li>it is <code>empty</code>, if <code>qn</code> is empty</li>
	 * <li>it equals <code>qn</code>, if the given qualified name contains no
	 * package information (represented by package delimiting <code>'.'</code>
	 * characters)
	 * <li>it equals <code>qn</code> in so far, that the fully qualified package
	 * name and the simple name are the same stayed the same. The only
	 * difference is that the package deliminiting <code>'.'</code> character
	 * was replaced by <code>'_'</code> characters.
	 * </ul>
	 * <ul>
	 * 
	 * @param qualifiedName
	 *            the qualified name to convert
	 * @return the unique name deducted from the given qualified name
	 */
	public static String toUniqueName(String qualifiedName) {
		return qualifiedName.replace(".", "_");
	}
}
