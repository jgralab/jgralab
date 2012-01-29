/*
 * JGraLab - The Java Graph Laboratory
 * 
 * Copyright (C) 2006-2011 Institute for Software Technology
 *                         University of Koblenz-Landau, Germany
 *                         ist@uni-koblenz.de
 * 
 * For bug reports, documentation and further information, visit
 * 
 *                         http://jgralab.uni-koblenz.de
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
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import de.uni_koblenz.jgralab.AttributedElement;
import de.uni_koblenz.jgralab.schema.Attribute;
import de.uni_koblenz.jgralab.schema.AttributedElementClass;
import de.uni_koblenz.jgralab.schema.Constraint;
import de.uni_koblenz.jgralab.schema.Domain;
import de.uni_koblenz.jgralab.schema.Package;
import de.uni_koblenz.jgralab.schema.Schema;
import de.uni_koblenz.jgralab.schema.exception.DuplicateAttributeException;
import de.uni_koblenz.jgralab.schema.exception.InheritanceException;
import de.uni_koblenz.jgralab.schema.exception.SchemaClassAccessException;
import de.uni_koblenz.jgralab.schema.exception.SchemaException;
import de.uni_koblenz.jgralab.schema.impl.compilation.SchemaClassManager;

public abstract class AttributedElementClassImpl<SC extends AttributedElementClass<SC, IC>, IC extends AttributedElement<SC, IC>>
		extends NamedElementImpl implements AttributedElementClass<SC, IC> {

	/**
	 * the list of attributes. Only the own attributes of this class are stored
	 * here, no inherited attributes
	 */
	private final TreeSet<Attribute> attributeList = new TreeSet<Attribute>();

	/**
	 * the list of all attributes. Own attributes and inherited attributes are
	 * stored here - but only if the schema is finish
	 */
	private SortedSet<Attribute> allAttributeList;

	/**
	 * A set of {@link Constraint}s which can be used to validate the graph.
	 */
	protected HashSet<Constraint> constraints = new HashSet<Constraint>(1);

	/**
	 * the immediate sub classes of this class
	 */
	protected Set<SC> directSubClasses = new HashSet<SC>();

	/**
	 * the sub classes of this class - only set if the schema is finish
	 */
	protected Set<SC> allSubClasses;

	/**
	 * the immediate super classes of this class
	 */
	protected Set<SC> directSuperClasses = new HashSet<SC>();

	/**
	 * the super classes of this class - only set if the schema is finish
	 */
	protected Set<SC> allSuperClasses;

	/**
	 * true if the schema is finish
	 */
	private boolean finish = false;

	/**
	 * true if element class is abstract
	 */
	private boolean isAbstract = false;

	private boolean internal = false;

	/**
	 * The class object representing the generated interface for this
	 * AttributedElementClass
	 */
	private Class<IC> schemaClass;

	/**
	 * The class object representing the implementation class for this
	 * AttributedElementClass. This may be either the generated class or a
	 * subclass of this
	 */
	private Class<IC> schemaImplementationClass;

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

		if (finish) {
			throw new SchemaException("No changes to finished schema!");
		}

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
	public void addAttribute(String name, Domain domain) {
		addAttribute(new AttributeImpl(name, domain, this, null));
	}

	@Override
	public void addConstraint(Constraint constraint) {
		if (finish) {
			throw new SchemaException("No changes to finished schema!");
		}
		constraints.add(constraint);
	}

	/**
	 * adds a superClass to this class
	 * 
	 * @param superClass
	 *            the class to add as superclass
	 */
	@SuppressWarnings("unchecked")
	protected void addSuperClass(SC superClass) {
		if (finish) {
			throw new SchemaException("No changes to finished schema!");
		}

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
		if (superClass.isSubClassOf((SC) this)) {
			throw new InheritanceException(
					"Cycle in class hierarchie for classes: "
							+ getQualifiedName() + " and "
							+ superClass.getQualifiedName());
		}
		directSuperClasses.add(superClass);
		((AttributedElementClassImpl<SC, IC>) superClass).directSubClasses
				.add((SC) this);
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

	@Override
	public boolean containsAttribute(String name) {
		return (getAttribute(name) != null);
	}

	@Override
	public Set<SC> getAllSubClasses() {
		if (finish) {
			return allSubClasses;
		}

		Set<SC> returnSet = new HashSet<SC>();
		for (SC subclass : directSubClasses) {
			returnSet.add(subclass);
			returnSet.addAll(subclass.getAllSubClasses());
		}
		return returnSet;
	}

	@Override
	public Set<SC> getAllSuperClasses() {
		if (finish) {
			return allSuperClasses;
		}

		HashSet<SC> allSuperClasses = new HashSet<SC>();
		allSuperClasses.addAll(directSuperClasses);
		for (SC superClass : directSuperClasses) {
			allSuperClasses.addAll(superClass.getAllSuperClasses());
		}
		return allSuperClasses;
	}

	@Override
	public Attribute getAttribute(String name) {
		// TODO ask if Attributes save as map
		if (finish) {
			Iterator<Attribute> it = allAttributeList.iterator();
			Attribute a;
			while (it.hasNext()) {
				a = it.next();
				if (a.getName().equals(name)) {
					return a;
				}
			}
		}

		Attribute ownAttr = getOwnAttribute(name);
		if (ownAttr != null) {
			return ownAttr;
		}
		for (SC superClass : directSuperClasses) {
			Attribute inheritedAttr = superClass.getAttribute(name);
			if (inheritedAttr != null) {
				return inheritedAttr;
			}
		}
		return null;
	}

	@Override
	public int getAttributeCount() {
		if (finish) {
			return allAttributeList.size();
		}
		int attrCount = getOwnAttributeCount();
		for (SC superClass : directSuperClasses) {
			attrCount += superClass.getAttributeCount();
		}
		return attrCount;
	}

	@Override
	public SortedSet<Attribute> getAttributeList() {
		if (finish) {
			return allAttributeList;
		}

		TreeSet<Attribute> attrList = new TreeSet<Attribute>();
		attrList.addAll(attributeList);
		for (SC superClass : directSuperClasses) {
			attrList.addAll(superClass.getAttributeList());
		}
		return attrList;
	}

	@Override
	public Set<Constraint> getConstraints() {
		return constraints;
	}

	@Override
	public Set<SC> getDirectSubClasses() {
		return directSubClasses;
	}

	@Override
	public Set<SC> getDirectSuperClasses() {
		return directSuperClasses;
	}

	@SuppressWarnings("unchecked")
	@Override
	public Class<IC> getSchemaClass() {
		if (schemaClass == null) {
			String schemaClassName = getSchema().getPackagePrefix() + "."
					+ getQualifiedName();
			try {
				schemaClass = (Class<IC>) Class.forName(schemaClassName, true,
						SchemaClassManager.instance(getSchema()
								.getQualifiedName()));
			} catch (ClassNotFoundException e) {
				throw new SchemaClassAccessException(
						"Can't load (generated) schema class for AttributedElementClass '"
								+ getQualifiedName() + "'", e);
			}
		}
		return schemaClass;
	}

	@SuppressWarnings("unchecked")
	@Override
	public Class<IC> getSchemaImplementationClass() {
		if (isAbstract()) {
			throw new SchemaClassAccessException(
					"Can't get (generated) schema implementation class. AttributedElementClass '"
							+ getQualifiedName() + "' is abstract!");
		}
		if (schemaImplementationClass == null) {
			try {
				Field f = getSchemaClass().getField("IMPLEMENTATION_CLASS");
				schemaImplementationClass = (Class<IC>) f.get(schemaClass);
			} catch (SecurityException e) {
				throw new SchemaClassAccessException(e);
			} catch (NoSuchFieldException e) {
				throw new SchemaClassAccessException(e);
			} catch (IllegalArgumentException e) {
				throw new SchemaClassAccessException(e);
			} catch (IllegalAccessException e) {
				throw new SchemaClassAccessException(e);
			}
		}
		return schemaImplementationClass;
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
	public boolean isDirectSubClassOf(SC anAttributedElementClass) {
		return directSuperClasses.contains(anAttributedElementClass);
	}

	@Override
	public boolean isDirectSuperClassOf(SC anAttributedElementClass) {
		return ((AttributedElementClassImpl<SC, IC>) anAttributedElementClass).directSuperClasses
				.contains(this);
	}

	@Override
	public boolean isInternal() {
		return internal;
	}

	void setInternal(Boolean b) {
		internal = b;
	}

	@Override
	public boolean isSubClassOf(SC anAttributedElementClass) {
		return getAllSuperClasses().contains(anAttributedElementClass);
	}

	@Override
	public boolean isSuperClassOf(SC anAttributedElementClass) {
		return anAttributedElementClass.getAllSuperClasses().contains(this);
	}

	@Override
	public boolean isSuperClassOfOrEquals(SC anAttributedElementClass) {
		return ((this == anAttributedElementClass) || (isSuperClassOf(anAttributedElementClass)));
	}

	@Override
	public void setAbstract(boolean isAbstract) {
		this.isAbstract = isAbstract;
	}

	protected boolean subclassContainsAttribute(String name) {
		for (SC subClass : getAllSubClasses()) {
			Attribute subclassAttr = subClass.getAttribute(name);
			if (subclassAttr != null) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Called if the schema is finished, saves complete subclass, superclass and
	 * attribute list
	 */
	protected void finish() {
		allSuperClasses = new HashSet<SC>();
		allSuperClasses.addAll(directSuperClasses);
		for (SC superClass : directSuperClasses) {
			allSuperClasses.addAll(superClass.getAllSuperClasses());
		}

		allSubClasses = new HashSet<SC>();
		allSubClasses.addAll(directSubClasses);
		for (SC subClass : directSubClasses) {
			allSubClasses.addAll(subClass.getAllSubClasses());
		}

		allAttributeList = new TreeSet<Attribute>();
		allAttributeList.addAll(attributeList);
		for (SC superClass : directSuperClasses) {
			allAttributeList.addAll(superClass.getAttributeList());
		}

		directSubClasses = Collections.unmodifiableSet(directSubClasses);
		directSuperClasses = Collections.unmodifiableSet(directSuperClasses);
		allSuperClasses = Collections.unmodifiableSet(allSuperClasses);
		allSubClasses = Collections.unmodifiableSet(allSubClasses);
		allAttributeList = Collections.unmodifiableSortedSet(allAttributeList);

		finish = true;
	}

	/**
	 * Called if the schema is reopen
	 */
	protected void reopen() {
		directSubClasses = new HashSet<SC>(directSubClasses);
		directSuperClasses = new HashSet<SC>(directSuperClasses);
		allSuperClasses = null;
		allSubClasses = null;
		allAttributeList = null;

		finish = false;
	}

	protected boolean isFinished() {
		return finish;
	}
}
