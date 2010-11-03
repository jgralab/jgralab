/*
 * JGraLab - The Java Graph Laboratory
 * 
 * Copyright (C) 2006-2010 Institute for Software Technology
 *                         University of Koblenz-Landau, Germany
 *                         ist@uni-koblenz.de
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

package de.uni_koblenz.jgralab.schema.impl;

import java.lang.reflect.Field;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import de.uni_koblenz.jgralab.AttributedElement;
import de.uni_koblenz.jgralab.M1ClassManager;
import de.uni_koblenz.jgralab.schema.Attribute;
import de.uni_koblenz.jgralab.schema.AttributedElementClass;
import de.uni_koblenz.jgralab.schema.Constraint;
import de.uni_koblenz.jgralab.schema.Domain;
import de.uni_koblenz.jgralab.schema.Package;
import de.uni_koblenz.jgralab.schema.Schema;
import de.uni_koblenz.jgralab.schema.exception.DuplicateAttributeException;
import de.uni_koblenz.jgralab.schema.exception.InheritanceException;
import de.uni_koblenz.jgralab.schema.exception.M1ClassAccessException;

public abstract class AttributedElementClassImpl extends NamedElementImpl
		implements AttributedElementClass {

	/**
	 * a list of attributes which belongs to the m2 element
	 * (edgeclass/vertexclass/graphclass). Only the own attributes of this class
	 * are stored here, no inherited attributes
	 */
	private final TreeSet<Attribute> attributeList = new TreeSet<Attribute>();

	/**
	 * A set of {@link Constraint}s which can be used to validate the graph.
	 */
	protected HashSet<Constraint> constraints = new HashSet<Constraint>(1);

	/**
	 * the immediate sub classes of this class
	 */
	protected HashSet<AttributedElementClass> directSubClasses = new HashSet<AttributedElementClass>();

	/**
	 * the immediate super classes of this class
	 */
	protected HashSet<AttributedElementClass> directSuperClasses = new HashSet<AttributedElementClass>();

	/**
	 * defines the m2 element as abstract, i.e. that it may not have any
	 * instances
	 */
	private boolean isAbstract = false;

	/**
	 * The class object representing the generated interface for this
	 * AttributedElementClass
	 */
	private Class<? extends AttributedElement> m1Class;

	/**
	 * The class object representing the implementation class for this
	 * AttributedElementClass. This may be either the generated class or a
	 * subclass of this
	 */
	private Class<? extends AttributedElement> m1ImplementationClass;

	/**
	 * builds a new attributed element class
	 * 
	 * @param qn
	 *            the unique identifier of the element in the schema
	 */
	protected AttributedElementClassImpl(String simpleName, Package pkg,
			Schema schema) {
		super(simpleName, pkg, schema);
	}

	@Override
	public void addAttribute(Attribute anAttribute) {
		if (containsAttribute(anAttribute.getName())) {
			throw new DuplicateAttributeException(anAttribute.getName(),
					getQualifiedName());
		}
		// Check if a subclass already contains an attribute with that name. In
		// that case, it may not be added, too.
		if (subclassContainsAttribute(anAttribute.getName())) {
			throw new DuplicateAttributeException(
					"Duplicate Attribute '"
							+ anAttribute.getName()
							+ "' in AttributedElementClass '"
							+ getQualifiedName()
							+ "'. "
							+ "A derived AttributedElementClass already contains this Attribute.");
		}
		attributeList.add(anAttribute);
	}

	@Override
	public void addAttribute(String name, Domain domain,
			String defaultValueAsString) {
		addAttribute(new AttributeImpl(name, domain, this, defaultValueAsString));
	}

	@Override
	public void addConstraint(Constraint constraint) {
		constraints.add(constraint);
	}

	/**
	 * adds a superClass to this class
	 * 
	 * @param superClass
	 *            the class to add as superclass
	 */
	protected void addSuperClass(AttributedElementClass superClass) {
		if ((superClass == this) || (superClass == null)) {
			return;
		}
		directSuperClasses.remove(getSchema().getDefaultGraphClass());
		directSuperClasses.remove(getSchema().getDefaultEdgeClass());
		directSuperClasses.remove(getSchema().getDefaultVertexClass());

		for (Attribute a : superClass.getAttributeList()) {
			if (getOwnAttribute(a.getName()) != null) {
				throw new InheritanceException("Cannot add "
						+ superClass.getQualifiedName() + " as superclass of "
						+ getQualifiedName() + ", cause: Attribute "
						+ a.getName() + " is declared in both classes");
			}
		}
		if (superClass.isSubClassOf(this)) {
			for (AttributedElementClass attr : superClass.getAllSuperClasses()) {
				System.out.println(attr.getQualifiedName());
			}
			System.out.println();
			throw new InheritanceException(
					"Cycle in class hierarchie for classes: "
							+ getQualifiedName() + " and "
							+ superClass.getQualifiedName());
		}
		directSuperClasses.add(superClass);
		((AttributedElementClassImpl) superClass).directSubClasses.add(this);
	}

	/**
	 * @return a textual representation of all attributes the element holds
	 */
	protected String attributesToString() {
		StringBuilder output = new StringBuilder("\nSelf Attributes:\n");
		Iterator<Attribute> it = attributeList.iterator();
		Attribute a;
		while (it.hasNext()) {
			a = it.next();
			output.append(a.toString() + "\n");
		}
		output.append("\nSelf + Inherited Attributes:\n");
		it = getAttributeList().iterator();
		while (it.hasNext()) {
			a = it.next();
			output.append(a.toString() + "\n");
		}
		return output.toString();
	}

	/**
	 * Compares this element to another attributed element.
	 * <p>
	 * This is done by comparing the elements´ qualified names
	 * lexicographically.
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
	 * lexicographically greater than the <code>other</code> element´s qualified
	 * name</li>
	 * </ul>
	 * </p>
	 * 
	 * @return the result of the lexicographical comparison
	 * 
	 */
	@Override
	public int compareTo(AttributedElementClass other) {
		return this.qualifiedName.compareTo(other.getQualifiedName());
	}

	@Override
	public boolean containsAttribute(String name) {
		return (getAttribute(name) != null);
	}

	@Override
	public Set<AttributedElementClass> getAllSubClasses() {
		Set<AttributedElementClass> returnSet = new HashSet<AttributedElementClass>();
		for (AttributedElementClass subclass : directSubClasses) {
			returnSet.add(subclass);
			returnSet.addAll(subclass.getAllSubClasses());
		}
		return returnSet;
	}

	@Override
	public Set<AttributedElementClass> getAllSuperClasses() {
		HashSet<AttributedElementClass> allSuperClasses = new HashSet<AttributedElementClass>();
		allSuperClasses.addAll(directSuperClasses);
		for (AttributedElementClass superClass : directSuperClasses) {
			allSuperClasses.addAll(superClass.getAllSuperClasses());
		}
		return allSuperClasses;
	}

	@Override
	public Attribute getAttribute(String name) {
		Attribute ownAttr = getOwnAttribute(name);
		if (ownAttr != null) {
			return ownAttr;
		}
		for (AttributedElementClass superClass : directSuperClasses) {
			Attribute inheritedAttr = superClass.getAttribute(name);
			if (inheritedAttr != null) {
				return inheritedAttr;
			}
		}
		return null;
	}

	@Override
	public int getAttributeCount() {
		int attrCount = getOwnAttributeCount();
		for (AttributedElementClass superClass : directSuperClasses) {
			attrCount += superClass.getAttributeCount();
		}
		return attrCount;
	}

	@Override
	public SortedSet<Attribute> getAttributeList() {
		TreeSet<Attribute> attrList = new TreeSet<Attribute>();
		attrList.addAll(attributeList);
		for (AttributedElementClass superClass : directSuperClasses) {
			attrList.addAll(superClass.getAttributeList());
		}
		return attrList;
	}

	@Override
	public Set<Constraint> getConstraints() {
		return constraints;
	}

	@Override
	public Set<AttributedElementClass> getDirectSubClasses() {
		return directSubClasses;
	}

	@Override
	public Set<AttributedElementClass> getDirectSuperClasses() {
		return new HashSet<AttributedElementClass>(directSuperClasses);
	}

	@SuppressWarnings("unchecked")
	@Override
	public Class<? extends AttributedElement> getM1Class() {
		if (m1Class == null) {
			String m1ClassName = getSchema().getPackagePrefix() + "."
					+ getQualifiedName();
			try {
				m1Class = (Class<? extends AttributedElement>) Class.forName(
						m1ClassName, true, M1ClassManager.instance(getSchema()
								.getQualifiedName()));
			} catch (ClassNotFoundException e) {
				throw new M1ClassAccessException(
						"Can't load M1 class for AttributedElementClass '"
								+ getQualifiedName() + "'", e);
			}
		}
		return m1Class;
	}

	@SuppressWarnings("unchecked")
	@Override
	public Class<? extends AttributedElement> getM1ImplementationClass() {
		if (isAbstract()) {
			throw new M1ClassAccessException(
					"Can't get M1 implementation class. AttributedElementClass '"
							+ getQualifiedName() + "' is abstract!");
		}
		if (m1ImplementationClass == null) {
			try {
				Field f = getM1Class().getField("IMPLEMENTATION_CLASS");
				m1ImplementationClass = (Class<? extends AttributedElement>) f
						.get(m1Class);
			} catch (SecurityException e) {
				throw new M1ClassAccessException(e);
			} catch (NoSuchFieldException e) {
				throw new M1ClassAccessException(e);
			} catch (IllegalArgumentException e) {
				throw new M1ClassAccessException(e);
			} catch (IllegalAccessException e) {
				throw new M1ClassAccessException(e);
			}
		}
		return m1ImplementationClass;
	}

	@Override
	public Attribute getOwnAttribute(String name) {
		Iterator<Attribute> it = attributeList.iterator();
		Attribute a;
		while (it.hasNext()) {
			a = it.next();
			if (a.getName().equals(name)) {
				return a;
			}
		}
		return null;
	}

	@Override
	public int getOwnAttributeCount() {
		return attributeList.size();
	}

	@Override
	public SortedSet<Attribute> getOwnAttributeList() {
		return attributeList;
	}

	@Override
	public boolean hasAttributes() {
		return !getAttributeList().isEmpty();
	}

	@Override
	public boolean hasOwnAttributes() {
		return !attributeList.isEmpty();
	}

	@Override
	public boolean isAbstract() {
		return isAbstract;
	}

	@Override
	public boolean isDirectSubClassOf(
			AttributedElementClass anAttributedElementClass) {
		return directSuperClasses.contains(anAttributedElementClass);
	}

	@Override
	public boolean isDirectSuperClassOf(
			AttributedElementClass anAttributedElementClass) {
		return (((AttributedElementClassImpl) anAttributedElementClass).directSuperClasses
				.contains(this));
	}

	@Override
	public boolean isInternal() {
		Schema s = getSchema();
		return ((this == s.getDefaultEdgeClass())
				|| (this == s.getDefaultGraphClass()) || (this == s
				.getDefaultVertexClass()));
	}

	@Override
	public boolean isSubClassOf(AttributedElementClass anAttributedElementClass) {
		return getAllSuperClasses().contains(anAttributedElementClass);
	}

	@Override
	public boolean isSuperClassOf(
			AttributedElementClass anAttributedElementClass) {
		return anAttributedElementClass.getAllSuperClasses().contains(this);
	}

	@Override
	public boolean isSuperClassOfOrEquals(
			AttributedElementClass anAttributedElementClass) {
		return ((this == anAttributedElementClass) || (isSuperClassOf(anAttributedElementClass)));
	}

	@Override
	public void setAbstract(boolean isAbstract) {
		this.isAbstract = isAbstract;
	}

	protected boolean subclassContainsAttribute(String name) {
		for (AttributedElementClass subClass : getAllSubClasses()) {
			Attribute subclassAttr = subClass.getAttribute(name);
			if (subclassAttr != null) {
				return true;
			}
		}
		return false;
	}

	@Override
	public boolean equals(Object o) {
		if (o == null) {
			return false;
		}
		if (getClass() == o.getClass()) {
			AttributedElementClass other = (AttributedElementClass) o;
			if (!qualifiedName.equals(other.getQualifiedName())) {
				return false;
			}
			if (!getSchema().getQualifiedName().equals(
					other.getSchema().getQualifiedName())) {
				return false;
			}
			if (getAttributeCount() != other.getAttributeCount()) {
				return false;
			}
			for (Attribute attr : getAttributeList()) {
				if (!attr.equals(other.getAttribute(attr.getName()))) {
					return false;
				}
			}
			return true;
		}
		return false;
	}

}
