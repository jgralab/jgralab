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

/**
 * NamedElement defines methods to access several parameters evolving around the
 * name and package of a schema element.
 * 
 * <p>
 * <b>Note:</b> in the following, <code>namedElement</code>, and <code>namedElement'</code>
 * , will represent the states of the given <code>NamedElement</code> before,
 * respectively after, any operation.
 * </p>
 * 
 * <p>
 * <b>Note:</b> in the following it is understood that method arguments differ
 * from <code>null</code> (except if stated otherwise). Therefore there will be
 * no preconditions addressing this matter.
 * </p>
 * 
 * @author ist@uni-koblenz.de
 */
public interface NamedElement {

	/**
	 * Returns the full path to this named element. The filename (simpleName) is
	 * included at the end of the directory path.
	 * 
	 * <p>
	 * <b>Pattern:</b> <code>fn = namedElement.getFileName();</code>
	 * </p>
	 * 
	 * <p>
	 * <b>Preconditions:</b> none<br/>
	 * </p>
	 * 
	 * <p>
	 * <b>Postconditions:</b> the file name <code>fn</code> is represented by
	 * the qualified name, where every <code>'.'</code>-character has been
	 * replaced by the default separator character depending on the used
	 * execution environment.
	 * </p>
	 * 
	 * @return the full path to this named element
	 */
	public String getFileName();

	/**
	 * Returns the package holding this named element. For instances of Package
	 * this is the parent package.
	 * 
	 * <p>
	 * <b>Pattern:</b> <code>p = namedElement.getPackage();</code>
	 * </p>
	 * 
	 * <p>
	 * <b>Preconditions:</b> none
	 * </p>
	 * 
	 * <p>
	 * <b>Postconditions:</b> <code>p</code> has one of the following values:
	 * <ul>
	 * <li><code>null</code> if this named element is the
	 * <code>DefaultPackage</code> and therefore has no parent package</li>
	 * <li>the Package holding <code>namedElement</code></li>
	 * </ul>
	 * </p>
	 * 
	 * @return the package holding this named element
	 */
	public Package getPackage();

	/**
	 * Returns the name of the package holding this named element. For instances
	 * of Package this is the name of the parent package.
	 * 
	 * <p>
	 * <b>Pattern:</b> <code>pn = namedElement.getPackageName();</code>
	 * </p>
	 * 
	 * <p>
	 * <b>Preconditions:</b> none
	 * </p>
	 * 
	 * <p>
	 * <b>Postconditions:</b> <code>pn</code> has one of the following values:
	 * <ul>
	 * <li><code>null</code> if this named element is the
	 * <code>DefaultPackage</code>, and therefore has no parent package</li>
	 * <li>the qualified name of the Package holding <code>namedElement</code></li>
	 * </ul>
	 * </p>
	 * 
	 * @return the qualified name of the package holding this named element
	 */
	public String getPackageName();

	/**
	 * Returns the full path name to the directory holding this named element.
	 * For instances of Package this is the name of the parent package.
	 * 
	 * <p>
	 * <b>Pattern:</b> <code>pathName = namedElement.getPathName();</code>
	 * </p>
	 * 
	 * <p>
	 * <b>Preconditions:</b> none
	 * </p>
	 * 
	 * <p>
	 * <b>Postconditions:</b> <code>pathName</code> has one of the following
	 * values:
	 * <ul>
	 * <li><code>null</code> if this named element is the
	 * <code>DefaultPackage</code>, and therefore has no parent package</li>
	 * <li>the package name, where every <code>'.'</code> character has been
	 * replaced by the default separator character depending on the used
	 * execution environment.</li>
	 * </ul>
	 * </p>
	 * 
	 * @return the fully qualified path name to the directory holding this
	 *         element
	 */
	public String getPathName();

	/**
	 * 
	 * Returns the qualified name of this named element.
	 * 
	 * <p>
	 * <b>Pattern:</b> <code>qn = namedElement.getQualifiedName();</code>
	 * </p>
	 * 
	 * <p>
	 * <b>Preconditions:</b> none
	 * </p>
	 * 
	 * <p>
	 * <b>Postconditions:</b> <code>qn</code> takes one of the following values:
	 * <ul>
	 * <li>if this named element is the <code>DefaultPackage</code> (
	 * <code>namedElement.parentPackage</code> is <code>null</code>) and
	 * {@link de.uni_koblenz.jgralab3.schema.Package.DEFAULTPACKAGE_NAME
	 * DEFAULTPACKAGE_NAME} is an empty String, then the qualified name equals
	 * this named element's simple name</li>
	 * <li>if the above does not apply, but this named element's parent package
	 * is the <code>DefaultPackage</code>, then the qualified name equals this
	 * named element's simple name
	 * <li>if none of the above holds, then the qualified name is composed of
	 * the package name and the simple name, separated by a '.' character</li>
	 * </ul>
	 * </p>
	 * 
	 * @return the qualified name of this named element
	 */
	public String getQualifiedName();

	/**
	 * Return the qualified name relatively to the given package.
	 * 
	 * <p>
	 * <b>Pattern:</b> <code>relQn = namedElement.getQualifiedName(pkg);</code>
	 * </p>
	 * 
	 * <p>
	 * <b>Preconditions:</b> none
	 * </p>
	 * 
	 * <p>
	 * <b>Postconditions:</b> <code>relQn</code> takes one of the following
	 * values:
	 * <ul>
	 * <li><code>relQn</code> is the simple name, if the given package is this
	 * element's parent package</li>
	 * <li>if the above does not apply and this element's parent package is the
	 * <code>DefaultPackage</code>, then <code>relQn</code> is the composition
	 * of the {@link de.uni_koblenz.jgralab3.schema.Package.DEFAULTPACKAGE_NAME
	 * DEFAULTPACKAGE_NAME} + "." + <code>namedElement.simpleName</code></li>
	 * <li>in any other case, <code>relQn</code> is this named element's
	 * qualified name</li>
	 * </ul>
	 * 
	 * @param pkg
	 * @return
	 */
	public String getQualifiedName(Package pkg);

	/**
	 * Returns the Schema containing this named element.
	 * 
	 * <p>
	 * <b>Pattern:</b> <code>schema = namedElement.getSchema();</code>
	 * </p>
	 * 
	 * <p>
	 * <b>Preconditions:</b> none
	 * </p>
	 * 
	 * <p>
	 * <b>Postconditions:</b> <code>schema</code> is the Schema containing
	 * <code>namedElement</code>
	 * </p>
	 * 
	 * @return the Schema containing this named element
	 * 
	 */
	public Schema getSchema();

	/**
	 * Returns the simple name of this named element.
	 * 
	 * <p>
	 * <b>Pattern:</b> <code>sn = namedElement.getSimpleName();</code>
	 * </p>
	 * 
	 * <p>
	 * <b>Preconditions:</b> none
	 * </p>
	 * 
	 * <p>
	 * <b>Postconditions:</b> <code>sn == namedElement.simpleName</code>
	 * </p>
	 * 
	 * @return the simple name of this named element
	 */
	public String getSimpleName();

	/**
	 * Returns the unique name of this named element.
	 * 
	 * <p>
	 * This name is, as the qualifiedName is, unique in the whole Schema but
	 * much shorter than the qualified one. For instance, if there is only one
	 * class with simple name "X" in the whole schema, the unique name of this
	 * class can also be "X" without the package prefix.
	 * </p>
	 * 
	 * <p>
	 * <b>Pattern:</b> <code>un = namedElement.getUniqueName();</code>
	 * </p>
	 * 
	 * <p>
	 * <b>Preconditions:</b> none
	 * </p>
	 * 
	 * <p>
	 * <b>Postconditions:</b> <code>un == namedElement.uniqueName</code>, where
	 * the unique name is one of the following:
	 * <ul>
	 * <li>unique name equals this named element's simple name, if there is only
	 * one class with this named element's simple name in the Schema</li>
	 * <li>unique name equals this named element's qualified name, if there is
	 * another class with this named element's simple name in the Schema</li>
	 * </ul>
	 * </p>
	 * 
	 * @return the unique name of this named element
	 */
	public String getUniqueName();

	/**
	 * Checks if this named element is in the <code>DefaultPackage</code>.
	 * 
	 * <p>
	 * <b>Pattern:</b>
	 * <code>isInDefaultPackage = namedElement.isInDefaultPackage();</code>
	 * </p>
	 * 
	 * <p>
	 * <b>Preconditions:</b> none
	 * </p>
	 * 
	 * <p>
	 * <b>Postconditions:</b> <code>isInDefaultPackage</code> is:
	 * <ul>
	 * <li><code>true</code> if <code>namedElement</code> lies directly in the
	 * <code>DefaultPackage</code></li>
	 * <li><code>false</code> if <code>namedElement</code> is the
	 * <code>DefaultPackage</code></li>
	 * <li><code>false</code> in any other case
	 * </ul>
	 * </p>
	 * 
	 * @return <code>true</code> if this element lies directly in the
	 *         <code>DefaultPackage</code>, <code>false</code> otherwise
	 */
	public boolean isInDefaultPackage();

	/**
	 * Returns a textual representation of this named element.
	 * 
	 * <p>
	 * This method has to be implemented by each specialising class.
	 * </p>
	 */
	public String toString();
}
