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

import java.util.Set;
import java.util.SortedSet;

import de.uni_koblenz.jgralab.Attribute;
import de.uni_koblenz.jgralab.AttributedElement;
import de.uni_koblenz.jgralab.schema.exception.DuplicateAttributeException;
import de.uni_koblenz.jgralab.schema.exception.M1ClassAccessException;
import de.uni_koblenz.jgralab.schema.exception.ReservedWordException;

/**
 * This is the base class of any <code>GraphClass</code>/
 * <code>VertexClass</code>/<code>EdgeClass</code>/<code>AggregationClass</code>
 * /<code>CompositionClass</code>.
 *
 * <p>
 * <b>Note:</b> in the following, <code>attrElement</code>, and <code>attrElement'</code>
 * , will represent the states of the given <code>AttributedElementClass</code>
 * before, respectively after, any operation.
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
public interface AttributedElementClass extends NamedElement,
		Comparable<AttributedElementClass> {
	/**
	 * Sets the package of this Domain to <code>p</code>.
	 *
	 * @param p
	 *            the package of this Domain.
	 */
	public void setPackage(Package p);

	/**
	 * Returns the fully qualified package the element is located in.
	 *
	 * <p>
	 * <b>Pattern:</b> <code>pkg = attrElement.getPackage();</code>
	 * </p>
	 *
	 * <p>
	 * <b>Preconditions:</b> none
	 * </p>
	 *
	 * <p>
	 * <b>Postconditions:</b> <code>pkg</code> can adopt the following content:
	 * <ul>
	 * <li><code>pkg == null</code>, if <code>attrElement</code> is not
	 * contained in any package</li>
	 * <li><code>pkg</code> is the fully qualified package the element is
	 * located in</li>
	 * </ul>
	 * </p>
	 *
	 * @return the fully qualified package the element is located in, or
	 *         <code>null</code> if this element is not contained in any package
	 */
	public Package getPackage();

	/**
	 * @param anAttributedElementClass
	 *            the class to search for
	 * @return true, if anAttributedElementClass is a direct or indirect
	 *         superclass of this class
	 */
	public boolean isSuperClassOf(
			AttributedElementClass anAttributedElementClass);

	/**
	 * @param anAttributedElementClass
	 *            the class to search for
	 * @return true, if anAttributedElementClass is a direct superclass of this
	 *         class
	 */
	public boolean isDirectSuperClassOf(
			AttributedElementClass anAttributedElementClass);

	/**
	 * @param anAttributedElementClass
	 *            the class to search for
	 * @return true, if anAttributedElementClass is a direct subclass of this
	 *         class
	 */
	public boolean isDirectSubClassOf(
			AttributedElementClass anAttributedElementClass);

	/**
	 * @param anAttributedElementClass
	 *            the class to search for
	 * @return true, if anAttributedElementClass is a direct or indirect
	 *         subclass of this class
	 */
	public boolean isSubClassOf(AttributedElementClass anAttributedElementClass);

	/**
	 * Tests if the given Class is a subclass of this class or equal to this
	 * class
	 *
	 * @param anAttributedElementClass
	 *            the class to test
	 * @return true iff the given AttributedElementClass is a subclass of this
	 *         class or is this class itself
	 */
	public boolean isSuperClassOfOrEquals(
			AttributedElementClass anAttributedElementClass);

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
	public Set<AttributedElementClass> getDirectSuperClasses();

	/**
	 * Lists all direct and indirect superclasses of this element.
	 *
	 * <p>
	 * <b>Note:</b> Each instance of a subclass of
	 * <code>AttributedElementClass</code> has one default direct superclass,
	 * and in some cases multiple default indirect superclasses. Please consult
	 * the specifications of the used subclass for details.
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
	 * direct and indirect superclasses (including the default superclasse(s))</li>
	 * </ul>
	 * </p>
	 *
	 * @return a Set of all direct and indirect superclasses of this element
	 */
	public Set<AttributedElementClass> getAllSuperClasses();

	/**
	 * @return the unique name of the element in the package without the fully
	 *         qualified package name
	 */
	public String getSimpleName();

	/**
	 * @return the unique name of the element in the schema, if there is only
	 *         one class in the schema with this short name, the short name is
	 *         returned. Otherwise, the fully qualified package name is returned
	 *         in a camel-cased underscored manner
	 */
	public String getUniqueName();

	/**
	 * @return the fully qualified name of this element
	 */
	public String getQualifiedName();

	/**
	 * Adds an attribute with the given <code>name</code> and
	 * <code>domain</code> to this element.
	 *
	 * <p>
	 * <b>Pattern:</b> <code>attrElement.addAttribute(name, domain);</code>
	 * </p>
	 *
	 * <p>
	 * <b>Preconditions:</b>
	 * <ul>
	 * <li>The <code>name</code> must not contain
	 * {@link de.uni_koblenz.jgralab.schema.Schema#reservedTGWords reserved TG
	 * words} and/or
	 * {@link de.uni_koblenz.jgralab.schema.Schema#reservedJavaWords reserved
	 * Java words}.</li>
	 * <li>The new attribute´s <code>name</code> must be distinct from all of
	 * this <code>attrElement´s</code> direct and inherited attributes´ names.</li>
	 * </ul>
	 * </p>
	 *
	 * <p>
	 * <b>Postconditions:</b> In addition to the direct and inherited
	 * attributes(s) of <code>attrElement</code>, <code>attrElement'</code> holds a new
	 * attribute with the specified <code>name</code> and <code>domain</code>.
	 * </p>
	 *
	 * @param name
	 *            a unique <code>name</code> in this element´s list of direct
	 *            and inherited attributes
	 * @param domain
	 *            the <code>domain</code> of the new <code>Attribute</code>
	 *
	 * @throws DuplicateAttributeException
	 *             if this element has a direct or inherited attribute with the
	 *             same <code>name</code>
	 *
	 * @throws ReservedWordException
	 *             if the <code>name</code> contains reserved
	 *             {@link de.uni_koblenz.jgralab.schema.Schema#reservedTGWords
	 *             TG}/
	 *             {@link de.uni_koblenz.jgralab.schema.Schema#reservedJavaWords
	 *             Java} words
	 */
	public void addAttribute(String name, Domain domain);

	/**
	 * Adds a new attribute <code>anAttribute</code> to this element.
	 *
	 * <p>
	 * <b>Pattern:</b> <code>attrElement.addAttribute(anAttribute);</code>
	 * </p>
	 *
	 * <p>
	 * <b>Preconditions:</b> <code>anAttribute´s</code> name must be distinct
	 * from all of this <code>attrElement´s</code> direct and inherited
	 * attributes´ names.
	 * </p>
	 *
	 * <p>
	 * <b>Postconditions:</b> In addition to the direct and inherited
	 * attributes(s) of <code>attrElement</code>, <code>attrElement'</code> holds a new
	 * attribute with the specified <code>name</code> and <code>domain</code>.
	 * </p>
	 *
	 * @param anAttribute
	 *            the new attribute to be added to this element
	 *
	 * @throws DuplicateAttributeException
	 *             if this element has a direct or inherited attribute with the
	 *             same <code>name</code>
	 */
	public void addAttribute(Attribute anAttribute);

	/**
	 * Fetches the attribute with the specified <code>name</code> from this
	 * element or it´s direct and indirect superclasses.
	 *
	 * <p>
	 * <b>Pattern:</b> <code>attr = attrElement.getAttribute(name);</code>
	 * </p>
	 *
	 * <p>
	 * <b>Preconditions:</b> none
	 * </p>
	 *
	 * <p>
	 * <b>Postconditions:</b>
	 * <ul>
	 * <li><code>attr</code> is a direct or inherited attribute of
	 * <code>attrElement</code> and has the specified <code>name</code></li>
	 * <li><code>attr == null </code>, if <code>attrElement</code> has no direct
	 * or inherited attribute with the given <code>name</code</li>
	 * </ul>
	 * </p>
	 *
	 * @param name
	 *            the <code>name</code> of the attribute
	 * @return the attribute with the specified <code>name</code> or
	 *         <code>null</code> if no such attribute was found in this element
	 *         and it´s superclasses
	 */
	public Attribute getAttribute(String name);

	/**
	 * Fetches the attribute with the specified <code>name</code> from this
	 * element.
	 * <p>
	 * Unlike
	 * {@link de.uni_koblenz.jgralab.schema.AttributedElementClass#getAttribute(String)
	 * getAttribute(String name)}, this method does not consider inherited
	 * attributes.
	 * </p>
	 *
	 * <p>
	 * <b>Pattern:</b> <code>attr = attrElement.getOwnAttribute(name);</code>
	 * </p>
	 *
	 * <p>
	 * <b>Preconditions:</b> none
	 * </p>
	 *
	 * <p>
	 * <b>Postconditions:</b>
	 * <ul>
	 * <li><code>attr</code> is a direct attribute of <code>attrElement</code>
	 * and has the specified <code>name</code></li>
	 * <li><code>attr == null </code>, if <code>attrElement</code> has no direct
	 * attribute with the given <code>name</code</li>
	 * </ul>
	 * </p>
	 *
	 * @param name
	 *            the <code>name</code> of the attribute
	 * @return the attribute with the specified <code>name</code> or
	 *         <code>null</code> if no such attribute was found directly in this
	 *         element
	 */
	public Attribute getOwnAttribute(String name);

	/**
	 * Returns all of this element´s attributes.
	 *
	 * <p>
	 * Unlike
	 * {@link de.uni_koblenz.jgralab.schema.AttributedElementClass#getAttributeList()
	 * getAttributeList()}, this method does not consider inherited attributes.
	 * </p>
	 *
	 * <p>
	 * <b>Pattern:</b> <code>attrs = attrElement.getOwnAttributeList();</code>
	 * </p>
	 *
	 * <p>
	 * <b>Preconditions:</b> none
	 * </p>
	 *
	 * <p>
	 * <b>Postconditions:</b>
	 * <ul>
	 * <li><code>attrs != null</code></li>
	 * <li><code>attrs.size() >= 0</code></li>
	 * <li><code>attrs</code> contains all attributes of
	 * <code>attrElement´s</code> direct attributes</li>
	 * <li><code>attrs</code> does not contain any inherited attributes of
	 * <code>attrElement</code></li>
	 * <li>the attributes in <code>attrs</code> are sorted lexicographically by
	 * their qualified name</li>
	 * </ul>
	 * </p>
	 *
	 * @return a SortedSet of attributes of this element
	 */
	public SortedSet<Attribute> getOwnAttributeList();

	/**
	 * Returns all of this element´s direct and inherited attributes in natural
	 * order.
	 *
	 * <p>
	 * <b>Pattern:</b> <code>attrs = attrElement.getAttributeList();</code>
	 * </p>
	 *
	 * <p>
	 * <b>Preconditions:</b> none
	 * </p>
	 *
	 * <p>
	 * <b>Postconditions:</b>
	 * <ul>
	 * <li><code>attrs != null</code></li>
	 * <li><code>attrs.size() >= 0</code></li>
	 * <li><code>attrs</code> contains every of <code>attrElement´s</code>direct
	 * and inherited attributes
	 * <li>the attributes in <code>attrs</code> are sorted lexicographically by
	 * their qualified name</li>
	 * </ul>
	 * </p>
	 *
	 * @return a SortedSet of attributes of this element and all inherited
	 *         attributes
	 */
	public SortedSet<Attribute> getAttributeList();

	/**
	 * Checks if this element or a superclass has an attribute with the given
	 * <code>name</code>.
	 *
	 * <p>
	 * <b>Pattern:</b>
	 * <code>containsAttr = attrElement.containsAttribute(name);</code>
	 * </p>
	 *
	 * <p>
	 * <b>Preconditions:</b> The <code>name</code> must not be empty.
	 * </p>
	 *
	 * <p>
	 * <b>Postconditions:</b> none
	 * </p>
	 *
	 * @param name
	 *            the <code>name</code> of the attribute to search for
	 *
	 * @return <code>true</code>, if the element or a superclass contains an
	 *         attribute with the specified <code>name</code>
	 *
	 */
	public boolean containsAttribute(String name);

	/**
	 * Gets the direct and inherited attribute count for this element.
	 *
	 * <p>
	 * <b>Pattern:</b> <code>attrCount = attrElement.getAttributeCount();</code>
	 * </p>
	 *
	 * <p>
	 * <b>Preconditions:</b> none
	 * </p>
	 *
	 * <p>
	 * <b>Postconditions:</b>
	 * <ul>
	 * <li><code>attrCount >= 0</code></li>
	 * <li><code>attrCount</code> equals the number of
	 * <code>attrElement´s</code> direct and inherited attributes</li>
	 * </ul>
	 * </p>
	 *
	 * @return the number of this element´s direct and inherited attributes
	 */
	public int getAttributeCount();

	/**
	 * Gets the attribute count for this element.
	 *
	 * <p>
	 * <b>Pattern:</b>
	 * <code>attrCount = attrElement.getOwnAttributeCount();</code>
	 * </p>
	 *
	 * <p>
	 * Unlike
	 * {@link de.uni_koblenz.jgralab.schema.AttributedElementClass#getAttributeCount()
	 * getAttributeCount()}, this method does not count inherited attributes.
	 * </p>
	 *
	 * <p>
	 * <b>Preconditions:</b> none
	 * </p>
	 *
	 * <p>
	 * <b>Postconditions:</b>
	 * <ul>
	 * <li><code>attrCount >= 0</code></li>
	 * <li><code>attrCount</code> equals the number of
	 * <code>attrElement´s</code> direct attributes</li>
	 * <li><code>attrCount</code> does not contain inherited attributes</li>
	 * </ul>
	 * </p>
	 *
	 * @return the number of this element´s direct attributes
	 */
	public int getOwnAttributeCount();

	/**
	 * this method has to be implemented by each specialised class, it has to
	 * return a textual representation of that class
	 */
	public abstract String toString();

	/**
	 * @return true, if the element may not have any instances
	 */
	public boolean isAbstract();

	/**
	 * defines if the element may or may not have any instances
	 *
	 * @param isAbstract
	 *            true or false
	 */
	public void setAbstract(boolean isAbstract);

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
	public Set<AttributedElementClass> getDirectSubClasses();

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
	public Set<AttributedElementClass> getAllSubClasses();

	/**
	 * @return true, if the element has own or inherited attributes, false
	 *         otherwise
	 */
	public boolean hasAttributes();

	/**
	 * Checks if this AttributedElementClass has own attributes
	 *
	 * @return true if this AttributedElementClass contains at least one
	 *         non-inherited attributed, false otherwise
	 */
	public boolean hasOwnAttributes();

	/**
	 * @return true, if this AttributedElementClass is only for internal use
	 */
	public boolean isInternal();

	/**
	 * sets the <code>internal</code>-flag of this AttributedElementClass, if
	 * this is set, the AttributedElementClass is only for internal use (for
	 * instance, the AttributedElementClasses 'Graph', 'Vertex', 'Edge',
	 * 'Composition' and 'Aggregation' are such AttributedElementClasses only
	 * for internal use
	 *
	 * @param internal
	 */
	public void setInternal(boolean internal);

	/**
	 * @return the schema this AttributedElementClass belongs to
	 */
	public Schema getSchema();

	/**
	 * Returns the M1 interface class for this attributed element.
	 *
	 * <p>
	 * <b>Pattern:</b> <code>m1Class = attrElement.getM1Class();</code>
	 * </p>
	 *
	 * <p>
	 * <b>Preconditions:</b> not yet defined
	 * </p>
	 *
	 * <p>
	 * <b>Postconditions:</b> not yet defined
	 * </p>
	 *
	 * @return the M1 interface class for this element
	 *
	 * @throws M1ClassAccessException
	 *             if reflection exceptions occur.
	 */
	public Class<? extends AttributedElement> getM1Class();

	/**
	 * Returns the M1 implementation class for this attributed element.
	 *
	 * <p>
	 * <b>Pattern:</b> <code>m1ImplClass = attrElement.getM1Class();</code>
	 * </p>
	 *
	 * <p>
	 * <b>Preconditions:</b> not yet defined
	 * </p>
	 *
	 * <p>
	 * <b>Postconditions:</b> not yet defined
	 * </p>
	 *
	 * @return the M1 implementation class for this element
	 *
	 * @throws M1ClassAccessException
	 *             if:
	 *             <ul>
	 *             <li>this element is abstract</li>
	 *             <li>there are reflection exceptions</li>
	 *             </ul>
	 */
	public Class<? extends AttributedElement> getM1ImplementationClass();

	/**
	 * Returns the least common superclass of this class and the given class
	 * <code>other</code>
	 *
	 * @return the least common superclass
	 */
	public AttributedElementClass getLeastCommonSuperclass(
			AttributedElementClass other);

	/**
	 * Returns the least common superclass of this class and the classes in the
	 * set <code>other</code>
	 *
	 * @return the least common superclass
	 */
	public AttributedElementClass getLeastCommonSuperclass(
			Set<? extends AttributedElementClass> other);

	/**
	 * Adds a constraint given as GReQL2 expression to this graph element class.
	 *
	 * <p>
	 * <b>Pattern:</b>
	 * <code>attrElement.addConstraint("forall x : V{Foo} @ x.bar < 17");</code>
	 * </p>
	 *
	 * <p>
	 * <b>Preconditions:</b>
	 * <ul>
	 * <li>The expression is a GReQL predicate (e.g. it returns a
	 * <code>JValueBoolean</code>). In that case
	 * <code>GraphValidator.validate()</code> returns a
	 * <code>ConstraintInvalidation</code> if the predicate evaluates to
	 * <code>false</code>.</li>
	 * <li>The expression is a GReQL expression that evaluates to a
	 * <code>JValueCollection</code> (i.e. a FWR clause). In that case
	 * <code>GraphValidator.validate()<ode> returns one
	 * <code>ConstraintInvalidation</code> for each element.</li>
	 * </ul>
	 * </p>
	 *
	 * <p>
	 * <b>Postconditions:</b>
	 * <ul>
	 * <li><code>attrElement'.getConstraints().size >= 0</code></li>
	 * <li><code>attrElement'.getConstraints().size() == attrElement.getConstraints().size() + 1</code>, if for each constraint <code>c2</code> of
	 * <code>attrElement</code> the following condition holds:
	 * <code>!c.equals(c2)</code></li>
	 * </ul>
	 * </p>
	 * </p>
	 *
	 * @param greqlExpression
	 *            a GReQL2 expression as string
	 */
	public void addConstraint(String greqlExpression);

	/**
	 * Returns this element´s Set of constraints.
	 *
	 * <p>
	 * Constraints are greql2 predicates, that can be used to validate the
	 * graph.
	 * </p>
	 *
	 * <p>
	 * <b>Pattern:</b> <code>constrs = attrElement.getConstraints();</code>
	 * </p>
	 *
	 * <p>
	 * <b>Preconditions:</b> none
	 * </p>
	 *
	 * <p>
	 * <b>Postconditions:</b>
	 * <ul>
	 * <li><code>constrs != null</code></li>
	 * <li><code>constrs.size() >= 0</code></li>
	 * <li><code>constrs</code> contains all of this element´s constraints</li>
	 * </ul>
	 * </p>
	 *
	 * @return a Set of all constraints (greql expressions) set for this
	 *         attributed element
	 */
	public Set<String> getConstraints();

	/**
	 * Compares this element to another attributed element.
	 * <p>
	 * This is done by
	 * {@link de.uni_koblenz.jgralab.schema.QualifiedName#compareTo(QualifiedName o)
	 * comparing the elements´ qualified names lexicographically}.
	 * </p>
	 *
	 * <p>
	 * <b>Pattern:</b> <code>comp = attrElement.compareTo(other);</code>
	 * </p>
	 *
	 * <p>
	 * <b>Preconditions:</b> none
	 * </p>
	 *
	 * <p>
	 * <b>Postconditions:</b>
	 * <ul>
	 * <li><code>comp < 0</code>, if this element´s qualified name is
	 * lexicographically less than the <code>other</code> element´s qualified
	 * name</li>
	 * <li><code>comp == 0</code>, if both element´s qualified names are equal</li>
	 * <li><code>comp > 0</code>, if this element´s qualified name is
	 * lexicographically less than the <code>other</code> element´s qualified
	 * name</li>
	 * </ul>
	 * </p>
	 *
	 * @return the result of the lexicographical comparison
	 */
	public int compareTo(AttributedElementClass other);
}
