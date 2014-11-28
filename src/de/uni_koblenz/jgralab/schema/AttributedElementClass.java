/*
 * JGraLab - The Java Graph Laboratory
 *
 * Copyright (C) 2006-2014 Institute for Software Technology
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

import java.util.List;
import java.util.Set;

import org.pcollections.PVector;

import de.uni_koblenz.jgralab.AttributedElement;
import de.uni_koblenz.jgralab.exception.NoSuchAttributeException;
import de.uni_koblenz.jgralab.schema.exception.SchemaClassAccessException;

/**
 * This is the base class of any <code>GraphClass</code>/
 * <code>VertexClass</code>/<code>EdgeClass</code>/<code>AggregationClass</code>
 * /<code>CompositionClass</code>.
 * 
 * <p>
 * <b>Note:</b> in the following, <code>attrElement</code>, and
 * <code>attrElement'</code> , will represent the states of the given
 * <code>AttributedElementClass</code> before, respectively after, any
 * operation.
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
public interface AttributedElementClass<SC extends AttributedElementClass<SC, IC>, IC extends AttributedElement<SC, IC>>
		extends NamedElement {

	/**
	 * Creates and returns an attribute with the given <code>name</code>,
	 * <code>domain</code>, and default value to this element.
	 * 
	 * <p>
	 * <b>Pattern:</b>
	 * <code>attrElement.createAttribute(name, domain, "7");</code>
	 * </p>
	 * 
	 * <p>
	 * <b>Preconditions:</b>
	 * <ul>
	 * <li>The new attributes <code>name</code> must be distinct from all of
	 * this <code>attrElements</code> direct and inherited attributes names.</li>
	 * </ul>
	 * </p>
	 * 
	 * <p>
	 * <b>Postconditions:</b> In addition to the direct and inherited
	 * attributes(s) of <code>attrElement</code>, <code>attrElement'</code>
	 * holds a new attribute with the specified <code>name</code> and
	 * <code>domain</code>.
	 * </p>
	 * 
	 * @param name
	 *            a unique <code>name</code> in this element´s list of direct
	 *            and inherited attributes
	 * @param domain
	 *            the <code>domain</code> of the new <code>Attribute</code>
	 * 
	 * @param defaultValueAsString
	 *            a String representing the default value of the nerw Attribute
	 *            in TG value syntax, or null if no default value is to be
	 *            specified
	 * @return the new attribute
	 * @throws DuplicateAttributeException
	 *             if this element has a direct or inherited attribute with the
	 *             same <code>name</code>
	 */
	public Attribute createAttribute(String name, Domain domain,
			String defaultValueAsString);

	/**
	 * Creates and returns an attribute with the given <code>name</code> and
	 * <code>domain</code> to this element. The attribute does not have a
	 * default value.
	 * 
	 * <p>
	 * <b>Pattern:</b> <code>attrElement.createAttribute(name, domain);</code>
	 * </p>
	 * 
	 * <p>
	 * <b>Preconditions:</b>
	 * <ul>
	 * <li>The new attributes <code>name</code> must be distinct from all of
	 * this <code>attrElements</code> direct and inherited attributes names.</li>
	 * </ul>
	 * </p>
	 * 
	 * <p>
	 * <b>Postconditions:</b> In addition to the direct and inherited
	 * attributes(s) of <code>attrElement</code>, <code>attrElement'</code>
	 * holds a new attribute with the specified <code>name</code> and
	 * <code>domain</code>.
	 * </p>
	 * 
	 * @param name
	 *            a unique <code>name</code> in this element´s list of direct
	 *            and inherited attributes
	 * @param domain
	 *            the <code>domain</code> of the new <code>Attribute</code>
	 * @return the new attribute
	 * @throws DuplicateAttributeException
	 *             if this element has a direct or inherited attribute with the
	 *             same <code>name</code>
	 */
	public Attribute createAttribute(String name, Domain domain);

	/**
	 * Adds a {@link Constraint} to this attributed element. Constraints are
	 * GReQL predicates, that can be used to validate the graph.
	 * 
	 * <p>
	 * <b>Note:</b> Constraints are not inheritable.
	 * </p>
	 * 
	 * <p>
	 * <b>Pattern:</b> <code>attrElement.addConstraint(constr);</code>
	 * </p>
	 * 
	 * <p>
	 * <b>Preconditions:</b> none
	 * </p>
	 * 
	 * <p>
	 * <b>Postconditions:</b>
	 * <ul>
	 * <li><code>attrElement'.getConstraints().size >= 0</code></li>
	 * <li>
	 * <code>attrElement'.getConstraints().size() == attrElement.getConstraints().size() + 1</code>
	 * , if for each constraint <code>c</code> of <code>attrElement</code> the
	 * following condition holds: <code>!constr.equals(c)</code></li>
	 * <li><code>attrElement'.getConstraints()</code> does not contain any
	 * inherited constraints from possible superclasses of
	 * <code>attrElement</code></li>
	 * </ul>
	 * </p>
	 * </p>
	 * 
	 * @param constraint
	 *            a {@link Constraint} to add to this element
	 */
	public void addConstraint(Constraint constraint);

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
	 * <li><code>attrs</code> contains every of <code>attrElement´s</code>
	 * direct and inherited attributes
	 * <li>the attributes in <code>attrs</code> are sorted lexicographically by
	 * their qualified name</li>
	 * </ul>
	 * </p>
	 * 
	 * @return a {@link PVector} of attributes of this element and all inherited
	 *         attributes
	 */
	public List<Attribute> getAttributeList();

	/**
	 * Returns this element's Set of {@link Constraint}s.
	 * 
	 * <p>
	 * Constraints are GReQL predicates, that can be used to validate the graph.
	 * Constraints are bound to a specific attributed element and are not
	 * inheritable.
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
	 * <li><code>constrs</code> contains all of this element's constraints</li>
	 * <li><code>constrs</code> does not contain any inherited constraint</li>
	 * </ul>
	 * </p>
	 * 
	 * @return a Set of all {@link Constraint}s of this attributed element
	 */
	public Set<Constraint> getConstraints();

	/**
	 * Returns the schema interface for this attributed element.
	 * 
	 * <p>
	 * <b>Pattern:</b> <code>schemaClass = attrElement.getSchemaClass();</code>
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
	 * @return the schema interface class for this element
	 * 
	 * @throws SchemaClassAccessException
	 *             if reflection exceptions occur.
	 */
	public Class<IC> getSchemaClass();

	/**
	 * Returns the schema implementation class for this attributed element.
	 * 
	 * <p>
	 * <b>Pattern:</b>
	 * <code>schemaImplClass = attrElement.getSchemaImplementationClass();</code>
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
	 * @return the schema implementation class for this element
	 * 
	 * @throws SchemaClassAccessException
	 *             if:
	 *             <ul>
	 *             <li>this element is abstract</li>
	 *             <li>there are reflection exceptions</li>
	 *             </ul>
	 */
	public Class<IC> getSchemaImplementationClass();

	/**
	 * Checks if this element has direct or inherited attributes.
	 * 
	 * <p>
	 * <b>Pattern:</b> <code>hasAttributes = attrElement.hasAttributes();</code>
	 * </p>
	 * 
	 * <p>
	 * <b>Preconditions:</b> none
	 * </p>
	 * 
	 * <p>
	 * <b>Postconditions:</b> <code>hasAttributes</code> has one of the
	 * following values:
	 * <ul>
	 * <li><code>true</code> if one of the following or both occur:
	 * <ul>
	 * <li><code>attrElement</code> has direct attributes</li>
	 * <li><code>attrElement</code> has inherited attributes</li>
	 * </ul>
	 * </li>
	 * <li><code>false</code> if the above is not met</li>
	 * </ul>
	 * </p>
	 * 
	 * @return <code>true</code>, if the element has own or inherited
	 *         attributes, <code>false</code> otherwise
	 */
	public boolean hasAttributes();

	/**
	 * Checks if this element has own attributes, that are not inherited.
	 * 
	 * <p>
	 * <b>Pattern:</b>
	 * <code>hasOwnAttributes = attrElement.hasOwnAttributes();</code>
	 * </p>
	 * 
	 * <p>
	 * <b>Preconditions:</b> none
	 * </p>
	 * 
	 * <p>
	 * <b>Postconditions:</b> <code>hasOwnAttributes</code> has one of the
	 * following values:
	 * <ul>
	 * <li><code>true</code> if <code>attrElement</code> has direct attributes</li>
	 * <li><code>false</code> if <coed>attrElement</code> has:</li>
	 * <ul>
	 * <li>only inherited attributes</code>
	 * <li>no attributes at all</code>
	 * </ul>
	 * </ul>
	 * </p>
	 * 
	 * @return <code>true</code>, if the element has own attributes,
	 *         <code>false</code> otherwise
	 */
	public boolean hasOwnAttributes();

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
	 * @return a {@link PVector} of attributes of this element
	 */
	public List<Attribute> getOwnAttributeList();

	/**
	 * States if this attributed element is abstract. Abstract elements can´t
	 * have instances.
	 * 
	 * <p>
	 * <b>Pattern:</b> <code>isAbstract = attrElement.isAbstract();</code>
	 * </p>
	 * 
	 * <p>
	 * <b>Preconditions:</b> none
	 * </p>
	 * 
	 * <p>
	 * <b>Postconditions:</b> <code>isAbstract</code> is:
	 * <ul>
	 * <li><code>true</code> if <code>attrElement</code> is abstract and
	 * therefore may not have any instances</li>
	 * <li>otherwise <code>false</code>
	 * </ul>
	 * 
	 * @return <code>true</code>, if the element is abstract , otherwise
	 *         <code>false</code>
	 */
	public boolean isAbstract();

	/**
	 * Defines if this attributed element is abstract. Abstract elements can´t
	 * have instances.
	 * 
	 * <p>
	 * <b>Pattern:</b> <code>attrElement.setAbstract(value);</code>
	 * </p>
	 * 
	 * <p>
	 * <b>Preconditions:</b> none
	 * </p>
	 * 
	 * <p>
	 * <b>Postconditions:</b> <code>attrElement'</code> is abstract and no new
	 * instances can be created
	 * </p>
	 * 
	 * @param isAbstract
	 *            the new value defining the state of this attributed element
	 */
	public void setAbstract(boolean isAbstract);

	/**
	 * <p>
	 * Returns an index for the attribute with the given name.
	 * </p>
	 * 
	 * <p>
	 * The index can be used to dereference the attribute's value in the generic
	 * implementation. If an attribute by the given name does not exist, a
	 * {@link NoSuchAttributeException} will be thrown.
	 * </p>
	 * 
	 * @param name
	 *            The attributes's name
	 * @return An index for the attribute.
	 * @throws NoSuchAttributeException
	 */
	public int getAttributeIndex(String name) throws NoSuchAttributeException;

}
