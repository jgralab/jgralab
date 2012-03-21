/*
 * JGraLab - The Java Graph Laboratory
 *
 * Copyright (C) 2006-2012 Institute for Software Technology
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

package de.uni_koblenz.jgralab.schema;

import org.pcollections.PSet;

import de.uni_koblenz.jgralab.GraphElement;

/**
 * Base class for VertexClass and EdgeClass.
 *
 * @author ist@uni-koblenz.de
 */
public interface GraphElementClass<SC extends GraphElementClass<SC, IC>, IC extends GraphElement<SC, IC>>
		extends AttributedElementClass<SC, IC> {

	/**
	 * Returns the GraphClass of this AttributedElementClass.
	 *
	 * @return the GraphClass in which this graph element class resides
	 */
	public GraphClass getGraphClass();

	/**
	 * Checks if the current element is a direct or indirect subclass of another
	 * attributed element.
	 *
	 * <p>
	 * <b>Pattern:</b>
	 * <code> isSubClass = attrElement.isSubClassOf(other);</code>
	 * </p>
	 *
	 * <p>
	 * <b>Preconditions:</b> none
	 * </p>
	 *
	 * <p>
	 * <b>Postconditions:</b> <code>isSubClass</code> is:
	 * <ul>
	 * <li><code>true</code> if the <code>other</code> attributed element is a
	 * direct or inherited superclass of this element</li>
	 * <li><code>false</code> if one of the following occurs:
	 * <ul>
	 * <li><code>attrElement</code> and the given <code>other</code> attributed
	 * element are the same</li>
	 * <li>the <code>other</code> attributed element is not a direct or
	 * inherited superclass of <code>attrElement</code></li>
	 * <li>the <code>other</code> attributed element has no relation with
	 * <code>attrElement</code></li>
	 * </ul>
	 * </li>
	 * </ul>
	 * </p>
	 *
	 * @param anAttributedElementClass
	 *            the possible superclass of this attributed element
	 * @return <code>true</code> if <code>anAttributedElementClass</code> is a
	 *         direct or indirect subclass of this element, otherwise
	 *         <code>false</code>
	 */
	public boolean isSubClassOf(SC anAttributedElementClass);

	/**
	 * Checks if the current element is a direct or inherited superclass of
	 * another attributed element.
	 *
	 * <p>
	 * <b>Pattern:</b>
	 * <code> isSuperClass = attrElement.isSuperClass(other);</code>
	 * </p>
	 *
	 * <p>
	 * <b>Preconditions:</b> none
	 * </p>
	 *
	 * <p>
	 * <b>Postconditions:</b> <code>isSuperClass</code> is:
	 * <ul>
	 * <li><code>true</code> if the <code>other</code> attributed element is a
	 * direct or inherited subclass of this element</li>
	 * <li><code>false</code> if one of the following occurs:
	 * <ul>
	 * <li><code>attrElement</code> and the given <code>other</code> attributed
	 * element are the same</li>
	 * <li>the <code>other</code> attributed element is not a direct or indirect
	 * subclass of <code>attrElement</code></li>
	 * <li>the <code>other</code> attributed element has no relation with
	 * <code>attrElement</code></li>
	 * </ul>
	 * </li>
	 * </ul>
	 * </p>
	 *
	 * @param anAttributedElementClass
	 *            the possible subclass of this attributed element
	 * @return <code>true</code> if <code>anAttributedElementClass</code> is a
	 *         direct or indirect subclass of this element, otherwise
	 *         <code>false</code>
	 */
	public boolean isSuperClassOf(SC anAttributedElementClass);

	/**
	 * Lists all direct subclasses of this element.
	 *
	 * <p>
	 * <b>Pattern:</b>
	 * <code>subClasses = attrElement.getDirectSubClasses();</code>
	 * </p>
	 *
	 * <p>
	 * <b>Preconditions:</b> none
	 * </p>
	 *
	 * <p>
	 * <b>Postconditions:</b>
	 * <ul>
	 * <li><code>subClasses != null</code></li>
	 * <li><code>subClasses.size() >= 0</code></li>
	 * <li><code>subClasses</code> holds all of <code>attrElement´s</code>
	 * direct subclasses</li>
	 * <li><code>subClasses</code> does not hold any of
	 * <code>attrElement´s</code> inherited subclasses</li>
	 * </ul>
	 * </p>
	 *
	 * @return a Set of all direct subclasses of this element
	 */
	public PSet<SC> getDirectSubClasses();

	/**
	 * Returns all direct superclasses of this element.
	 *
	 * <p>
	 * <b>Note:</b> Each instance of a subclass of
	 * <code>AttributedElementClass</code> has one default direct superclass.
	 * Please consult the specifications of the used subclass for details.
	 * </p>
	 *
	 * <p>
	 * <b>Pattern:</b>
	 * <code>superClasses = attrElement.getDirectSuperClasses();</code>
	 * </p>
	 *
	 * <p>
	 * <b>Preconditions:</b> none
	 * </p>
	 *
	 * <p>
	 * <b>Postconditions:</b>
	 * <ul>
	 * <li><code>superClasses != null</code></li>
	 * <li><code>superClasses.size() >= 0</code></li>
	 * <li><code>superClasses</code> holds all of <code>attrElement´s</code>
	 * direct superclasses (including the default superclass)</li>
	 * <li><code>superClasses</code> does not hold any of
	 * <code>attrElement´s</code> inherited superclasses
	 * </ul>
	 * </p>
	 *
	 * @return a Set of all direct superclasses of this element
	 */
	public PSet<SC> getDirectSuperClasses();

	/**
	 * Returns all direct and indirect subclasses of this element.
	 *
	 * <p>
	 * <b>Pattern:</b> <code>subClasses = attrElement.getAllSubClasses();</code>
	 * </p>
	 *
	 * <p>
	 * <b>Preconditions:</b> none
	 * </p>
	 *
	 * <p>
	 * <b>Postconditions:</b>
	 * <ul>
	 * <li><code>subClasses != null</code></li>
	 * <li><code>subClasses.size() >= 0</code></li>
	 * <li><code>subClasses</code> holds all of <code>attrElement´s</code>
	 * direct and indirect subclasses</li>
	 * </ul>
	 * </p>
	 *
	 * @return a Set of all direct and indirect subclasses of this element
	 */
	public PSet<SC> getAllSubClasses();

	/**
	 * Lists all direct and indirect superclasses of this element.
	 *
	 * <p>
	 * <b>Note:</b> Each instance of a subclass of
	 * <code>AttributedElementClass</code> has a dedicated default superclass at
	 * the top of its inheritance hierarchy. Please consult the specifications
	 * of the used subclass for details.
	 * </p>
	 *
	 * <p>
	 * <b>Pattern:</b>
	 * <code>superClasses = attrElement.getAllSuperClasses();</code>
	 * </p>
	 *
	 * <p>
	 * <b>Preconditions:</b> none
	 * </p>
	 *
	 * <p>
	 * <b>Postconditions:</b>
	 * <ul>
	 * <li><code>superClasses != null </code></li>
	 * <li><code>superClasses.size() >= 0</code></li>
	 * <li><code>superClasses</code> holds all of <code>attrElement´s</code>
	 * direct and indirect superclasses (including the default superclass)</li>
	 * </ul>
	 * </p>
	 *
	 * @return a Set of all direct and indirect superclasses of this element
	 */
	public PSet<SC> getAllSuperClasses();

	/**
	 * @return the ID of this GraphElementClass in the schema it belongs to
	 */
	public int getGraphElementClassIdInSchema();

	/**
	 * Deletes this graph element class from its graph class (package, and
	 * schema)
	 */
	public void delete();

	/**
	 * @return true, if this GraphElementClass is either the default VertexClass
	 *         or the default EdgeClass
	 */
	public boolean isDefaultGraphElementClass();

}
