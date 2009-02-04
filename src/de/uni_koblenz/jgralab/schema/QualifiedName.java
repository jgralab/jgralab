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
 * qualified name.</li>
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
	 * Otherwise, the unique name is the same as the qualified name.
	 */
	private String uniqueName;

/**
	 * Creates a new instance of <code>QualifiedName</code>.
	 *
	 * <p>
	 * <b>Pattern:</b> <code>qname = new getQualifiedName(qn);</code>
	 * </p>
	 *
	 * <p>
	 * <b>Preconditions:</b> none //TODO: Potential subject to change
	 * </p>
	 *
	 * <p>
	 * <b>Postconditions:</b>
	 * <ul>
	 * <li><code>qname'.packageName</code> has the following value:
	 * <ul>
	 * <li>empty String if one of the following occurs:
	 * <ul>
	 * <li><code>qn</code> starts with the character sequence "List<", representing a <code>QualifiedName</code> for an element from the <code>ListDomain</code></li>
	 * <li><code>qn</code> starts with the character sequence "Set<", representing a <code>QualifiedName</code> for an element from the <code>SetDomain</code></li>
	 * <li><code>qn.lastIndexOf(".") == 0</code>, representing a <code>QualifiedName</code> for an element not contained in any package</li>
	 * <li><code>qn</code> is an empty String, representing a <code>QualifiedName</code> for an element not contained in any package and without a simple name</li>
	 * </ul>
	 * </li>
	 * <li><code>qn.substring(0, qn.lastIndexOf("."))</code>, the fully qualified name of the package the
	 * element is located in.<br />The last "." character seperates the <code>packageName</code> from the <code>simpleName</code>.</li>
	 * </ul>
	 * </li>
	 * <li><code>qname'.simpleName</code> has the following value:</li>
	 * <ul>
	 * <li>empty String if one of the following occurs:</li>
	 * <ul>
	 * <li><code>qn.substring(qn.lastIndexOf(".") + 1)</code> is an empty String, representing a <code>QualifiedName</code> for an element which has no simple name</li> //TODO: Potential subject to change
	 * <li><code>qn</code> is an empty String, representing a <code>QualifiedName</code> for an element not contained in any package and without a simple name</li>
	 * </ul>
	 * <li><code>(subString =)qn.substring(qn.lastIndexOf(".") + 1) && subString.length > 0</code>, the unique name of an element in a package without
	 * the fully qualified package name</li>
	 * </ul>
	 * <li><code>qname'.qualifiedName</code> has the following value:</li>
	 * <ul>
	 * <li>empty String, if <code>qn</code> is an empty String, representing a <code>QualifiedName</code> for an element not contained in any package and without a simple name</li>
	 * <li>equals <code>simpleName</code>, representing a <code>QualifiedName</code> for an element not contained in any package.<br />Implies that: <code>qname'.packageName.equals("")</code></li>
	 * <li>equals <code>qn</code>, representing a <code>QualifiendName</code> for an element contained in a package and which has a simple name.</li>
	 * </ul>
	 * <li><code>qname'.uniqueName.equals(qname'.simpleName)</code><br />This holds for as long as there is only one class in
	 * the schema with this short name, which is the case upon instantiation. Otherwise, the unique name is the same as the qualified name.</li>
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

	public QualifiedName(String pn, String sn) {
		packageName = pn;
		simpleName = sn;
		qualifiedName = (pn.length() == 0) ? simpleName : packageName + "."
				+ simpleName;
		uniqueName = simpleName;
	}

	@Override
	public String toString() {
		return qualifiedName;
	}

	public void setPackageName(String pn) {
		packageName = pn;
		qualifiedName = (pn.length() == 0) ? simpleName : packageName + "."
				+ simpleName;
	}

	public String getSimpleName() {
		return simpleName;
	}

	public String getPackageName() {
		return packageName;
	}

	/**
	 *
	 * Returns the fully qualified name of an element with this
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
	 * <b>Postconditions:</b>
	 * <ul>
	 * <li><code>qn != null</code></li>
	 * <li><code>qn.length >= 0</li></li>
	 * <li><code>qn</code> is the fully qualified name of an element with this
	 * <code>QualifiedName</code><br />
	 * <code>qn = qname.packageName + "." + qname.simpleName</code></li>
	 * </ul>
	 * </p>
	 *
	 * @return the fully qualified name of an element with this
	 *         <code>QualifiedName</code>
	 */
	public String getQualifiedName() {
		return qualifiedName;
	}

	public String getFileName() {
		return qualifiedName.replace('.', File.separatorChar);
	}

	public String getName() {
		return qualifiedName;
	}

	public String getPathName() {
		return packageName.replace('.', File.separatorChar);
	}

	/**
	 * @return The unique name of this QualifiedName. This name is, as the
	 *         qualifiedName is, unique in the whole schema but much shorter
	 *         than the qualified one. For instance, if there is only one class
	 *         with simple name "X" in the whole schema, the unique name of this
	 *         class can also be "X" without the package prefix
	 */
	public String getUniqueName() {
		return uniqueName;
	}

	/**
	 * Sets the unique name of this QualifiedName. To ensure that the name is
	 * really unique in the whole schema, the element which should have this
	 * unique name needs to be specified. It there is any other element in the
	 * schema which owns the same unique name, a SchemaException is thrown
	 *
	 * @param element
	 *            The element which should be identified with the unique name
	 * @param uniqueName
	 *            The new uniqueName of the element
	 * @throws SchemaException
	 *             if the uniqueName is not unique, i.e. if there is any other
	 *             element in the schema with the same unique name
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

	@Override
	public boolean equals(Object obj) {
		return (this == obj)
				|| ((obj instanceof QualifiedName) && qualifiedName
						.equals(((QualifiedName) obj).qualifiedName));
	}

	@Override
	public int hashCode() {
		return qualifiedName.hashCode();
	}

	@Override
	public int compareTo(QualifiedName o) {
		return qualifiedName.compareTo(o.qualifiedName);
	}

	public boolean isSimple() {
		return packageName.length() == 0;
	}

	public boolean isQualified() {
		return packageName.length() != 0;
	}

	public static String toUniqueName(String qualifiedName) {
		return qualifiedName.replace(".", "_");
	}
}
