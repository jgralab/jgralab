/*
 * JGraLab - The Java graph laboratory
 * (c) 2006-2007 Institute for Software Technology
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
 
package de.uni_koblenz.jgralab.impl;

import java.lang.reflect.Field;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import de.uni_koblenz.jgralab.Attribute;
import de.uni_koblenz.jgralab.AttributedElement;
import de.uni_koblenz.jgralab.AttributedElementClass;
import de.uni_koblenz.jgralab.Domain;
import de.uni_koblenz.jgralab.GraphException;
import de.uni_koblenz.jgralab.M1ClassManager;
import de.uni_koblenz.jgralab.SchemaException;

public abstract class AttributedElementClassImpl implements
		AttributedElementClass {

	private final boolean DEBUG = false;

	/**
	 * toggles wether this class is only for internal use
	 */
	private boolean internal = false;

	/**
	 * the immediate super classes of this class
	 */
	protected HashSet<AttributedElementClass> directSuperClasses;

	/**
	 * a list of subclasses of this class object, all attributes of this class
	 * and all superclasses get inherited from those classes
	 */
	protected HashSet<AttributedElementClass> subClasses;

	/**
	 * a list of attributes which belongs to the m2 element
	 * (edgeclass/vertexclass/graphclass). Only the own attributes of
	 * this class are stored here, no inherited attributes
	 */
	private TreeSet<Attribute> attributeList;

	/**
	 * a unique identifier of the m2 element in the schema
	 * (edgeclass/vertexclass/graphclass)
	 */
	private String name;

	/**
	 * defines the m2 element as abstract, i.e. that it may not have any
	 * instances
	 */
	private boolean isAbstract = false;

	private Class<? extends AttributedElement> m1Class;

	private Class<? extends AttributedElement> m1ImplementationClass;

	/**
	 * builds a new attributed element class
	 * 
	 * @param name
	 *            the unique identifier of the element in the schema
	 */
	public AttributedElementClassImpl(String name) {
		this.name = name;
		m1Class = null;
		m1ImplementationClass = null;
		attributeList = new TreeSet<Attribute>();
		subClasses = new HashSet<AttributedElementClass>();
		directSuperClasses = new HashSet<AttributedElementClass>();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see jgralab.AttributedElementClass#getName()
	 */
	public String getName() {
		return name;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see jgralab.AttributedElementClass#addAttribute(java.lang.String,
	 *      jgralab.Domain)
	 */
	public void addAttribute(String name, Domain domain)  {
		addAttribute(new AttributeImpl(name, domain));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see jgralab.AttributedElementClass#addAttribute(jgralab.Attribute)
	 */
	public void addAttribute(Attribute anAttribute)  {
		if (containsAttribute(anAttribute.getName())) {
			throw new SchemaException("duplicate attribute name '"
					+ anAttribute.getName() + "' in class '" + getName() + "'");
		}
		attributeList.add(anAttribute);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see jgralab.AttributedElementClass#addAttributeList(java.util.List)
	 */
	public void addAttributes(Collection<Attribute> attrs) {
		for (Attribute a : attrs) {
			addAttribute(a);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see jgralab.AttributedElementClass#getAttribute(java.lang.String)
	 */
	public Attribute getOwnAttribute(String name) {
		Iterator<Attribute> it = attributeList.iterator();
		Attribute a;
		while (it.hasNext()) {
			a = (Attribute) it.next();
			if (a.getName().equals(name))
				return a;
		}
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see jgralab.AttributedElementClass#getAttribute(java.lang.String)
	 */
	public Attribute getAttribute(String name) {
		Attribute ownAttr = getOwnAttribute(name);
		if (ownAttr != null)
			return ownAttr;
		for (AttributedElementClass superClass : directSuperClasses) {
			Attribute inheritedAttr = superClass.getAttribute(name);
			if (inheritedAttr != null)
				return inheritedAttr;
		}
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see jgralab.AttributedElementClass#getAttributeList(boolean)
	 */
	public SortedSet<Attribute> getOwnAttributeList() {
		return attributeList;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see jgralab.AttributedElementClass#getAttributeList(boolean)
	 */
	public SortedSet<Attribute> getAttributeList() {
		TreeSet<Attribute> attrList = new TreeSet<Attribute>();
		attrList.addAll(attributeList);
		for (AttributedElementClass superClass : directSuperClasses) {
			attrList.addAll(superClass.getAttributeList());
		}
		return attrList;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see jgralab.AttributedElementClass#containsAttribute(java.lang.String)
	 */
	public boolean containsAttribute(String name) {
		return (getAttribute(name) != null);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see jgralab.AttributedElementClass#getAttributeCount(boolean)
	 */
	public int getOwnAttributeCount() {
		return attributeList.size();
	}

	public int getAttributeCount() {
		int attrCount = getOwnAttributeCount();
		for (AttributedElementClass superClass : directSuperClasses)
			attrCount += superClass.getAttributeCount();
		return attrCount;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	public abstract String toString();

	/**
	 * @return a textual representation of all attributes the element holds
	 */
	protected String attributesToString() {
		String output = "\nSelf Attributes:\n";
		Iterator<Attribute> it = attributeList.iterator();
		Attribute a;
		while (it.hasNext()) {
			a = it.next();
			output += a.toString() + "\n";
		}
		output += "\nSelf + Inherited Attributes:\n";
		it = getAttributeList().iterator();
		while (it.hasNext()) {
			a = it.next();
			output += a.toString() + "\n";
		}
		return output;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see jgralab.AttributedElementClass#isAbstract()
	 */
	public boolean isAbstract() {
		return isAbstract;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see jgralab.AttributedElementClass#setAbstract(boolean)
	 */
	public void setAbstract(boolean isAbstract) {
		this.isAbstract = isAbstract;
	}

	/**
	 * adds a superClass to this class
	 * 
	 * @param superClass the class to add as superclass
	 */
	protected void addSuperClass(AttributedElementClass superClass)  {
		if ((superClass == this) || (superClass == null))
			return;
		if (DEBUG) {
			System.out.println("Adding superclass: " + superClass.getName()
					+ " to class " + this.getName());
		}
		directSuperClasses.remove(getSchema().getDefaultGraphClass());
		directSuperClasses.remove(getSchema().getDefaultEdgeClass());
		directSuperClasses.remove(getSchema().getDefaultVertexClass());
		directSuperClasses.remove(getSchema().getDefaultAggregationClass());
		directSuperClasses.remove(getSchema().getDefaultCompositionClass());
		for (Attribute a: superClass.getAttributeList()) {
			if (getOwnAttribute(a.getName()) != null)
				throw new SchemaException("Cannot add " + superClass.getName() + " as superclass of " + name + ", cause: Attribute " + a.getName() + " is declared in both classes");
		}
		if (superClass.isSubClassOf(this))
			throw new GraphException("Cycle in class hierarchie for classes: " + getName() + " and " + superClass.getName());
		directSuperClasses.add(superClass);
		((AttributedElementClassImpl) superClass).subClasses.add(this);
	}

	/**
	 * adds a subclass to the list of subclasses, all attributes of this class
	 * and all superclasses get inherited to those classes
	 * 
	 * @param subClass
	 *            the edge class to be added to the list of subclasses
	 */
	protected void addSubClass(AttributedElementClass subClass) {
		subClasses.add(subClass);
		Iterator<AttributedElementClass> it = getAllSuperClasses().iterator();
		AttributedElementClass a;
		while (it.hasNext()) {
			a = it.next();
			if (DEBUG)
				System.out.println("Adding subclass " + subClass.getName()
						+ " to superclass " + a.getName());
			((AttributedElementClassImpl) a).addSubClass(subClass);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see jgralab.AttributedElementClass#isSuperClassOf(jgralab.AttributedElementClass)
	 */
	public boolean isSuperClassOf(
			AttributedElementClass anAttributedElementClass) {
	//	System.out.println(this.getName() + " is superclass of " + anAttributedElementClass.getName() + ": " + anAttributedElementClass.getAllSuperClasses().contains(this));
		return anAttributedElementClass.getAllSuperClasses().contains(this);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see jgralab.AttributedElementClass#isSuperClassOf(jgralab.AttributedElementClass)
	 */
	public boolean isDirectSuperClassOf(
			AttributedElementClass anAttributedElementClass) {
		return (((AttributedElementClassImpl) anAttributedElementClass).directSuperClasses
				.contains(this));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see jgralab.AttributedElementClass#isSuperClassOfOrEqual(jgralab.AttributedElementClass)
	 */
	public boolean isSuperClassOfOrEquals(AttributedElementClass anAttributedElementClass) {
		return ((this == anAttributedElementClass) || (isSuperClassOf(anAttributedElementClass)));
	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see jgralab.AttributedElementClass#isSubClassOf(jgralab.AttributedElementClass)
	 */
	public boolean isSubClassOf(AttributedElementClass anAttributedElementClass) {
		return getAllSuperClasses().contains(anAttributedElementClass);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see jgralab.AttributedElementClass#isSubClassOf(jgralab.AttributedElementClass)
	 */
	public boolean isDirectSubClassOf(
			AttributedElementClass anAttributedElementClass) {
		return directSuperClasses.contains(anAttributedElementClass);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see jgralab.AttributedElementClass#getSuperClasses(boolean)
	 */
	public Set<AttributedElementClass> getDirectSuperClasses() {
		return new HashSet<AttributedElementClass>(directSuperClasses);
	}

	public Set<AttributedElementClass> getAllSuperClasses() {
		HashSet<AttributedElementClass> allSuperClasses = new HashSet<AttributedElementClass>();
		allSuperClasses.addAll(directSuperClasses);
		for (AttributedElementClass superClass : directSuperClasses) {
			// System.out.println("Getting superclasses for class: " +
			// superClass.getName());
			allSuperClasses.addAll(superClass.getAllSuperClasses());
		}
		return allSuperClasses;
	}

	public Set<AttributedElementClass> getAllSubClasses() {
		Set<AttributedElementClass> returnSet = new HashSet<AttributedElementClass>();
		for (AttributedElementClass subclass : subClasses) {
			returnSet.add(subclass);
			returnSet.addAll(subclass.getAllSubClasses());
		}
		return returnSet;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see jgralab.AttributedElementClass#getSubClasses()
	 */
	public Set<AttributedElementClass> getDirectSubClasses() {
		return subClasses;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see jgralab.AttributedElementClass#hasAttributes()
	 */
	public boolean hasAttributes() {
		return !getAttributeList().isEmpty();
	}

	public boolean hasOwnAttributes() {
		return !attributeList.isEmpty();
	}

	public boolean isInternal() {
		return internal;
	}

	public void setInternal(boolean internal) {
		this.internal = internal;
	}

	public int compareTo(AttributedElementClass another) {
		return name.compareTo(another.getName());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see jgralab.AttributedElementClass#getM1Class()
	 */
	@SuppressWarnings("unchecked")
	public <T extends AttributedElement> Class<T> getM1Class()
			 {
		if (m1Class == null) {
			String m1ClassName = getSchema().getPrefix() + "." + getName();
			try {
				m1Class = (Class<T>) Class.forName(m1ClassName, true, M1ClassManager.instance());
			} catch (ClassNotFoundException e) {
				throw new SchemaException(
						"Can't load M1 class for AttributedElementClass '"
								+ getName() + "'", e);
			}
		}
		return (Class<T>) m1Class;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see jgralab.AttributedElementClass#getM1ImplementationClass()
	 */
	@SuppressWarnings("unchecked")
	public <T extends AttributedElement> Class<T> getM1ImplementationClass()
			 {
		if (isAbstract()) {
			throw new SchemaException(
					"Can't get M1 implementation class. AttributedElementClass '"
							+ getName() + "' is abstract!");
		}
		if (m1ImplementationClass == null)
			try {
				Field f = getM1Class().getField("IMPLEMENTATION_CLASS");
				m1ImplementationClass = (Class<? extends AttributedElement>) f
						.get(m1Class);
			} catch (SecurityException e) {
				throw new SchemaException(e);
			} catch (NoSuchFieldException e) {
				throw new SchemaException(e);
			} catch (IllegalArgumentException e) {
				throw new SchemaException(e);
			} catch (IllegalAccessException e) {
				throw new SchemaException(e);
			}
		return (Class<T>) m1ImplementationClass;
	}
	
	public AttributedElementClass getLeastCommonSuperclass(AttributedElementClass other) {
		HashSet<AttributedElementClass> classes = new HashSet<AttributedElementClass>();
		classes.add(this);
		classes.add(other);
		return calculateLeastCommonSuperclass(classes);
	}
	
	public AttributedElementClass getLeastCommonSuperclass(Set<? extends AttributedElementClass> other) {
		HashSet<AttributedElementClass> classes = new HashSet<AttributedElementClass>();
		classes.add(this);
		classes.addAll(other);
		return calculateLeastCommonSuperclass(classes);
	}	
	
	public static AttributedElementClass calculateLeastCommonSuperclass(Set<? extends AttributedElementClass> classes) {
		 AttributedElementClass leastCommon = null;
		 for (AttributedElementClass a : classes) {
			 boolean leastCommonCandidate = true;
			 for (AttributedElementClass b : classes) {
				 //if (a == null)
					// System.out.println(" A is null");
				 if (!a.isSuperClassOfOrEquals(b)) { 
					// System.out.println(a.getName() + " is not a superclass of " + b.getName());
					 leastCommonCandidate = false;
					 break;
				 }	 
			 }
			 if (leastCommonCandidate) {
			//	 System.out.println("Found least common candidate: " + leastCommon);
				 if ((leastCommon == null) || (a.isSubClassOf(leastCommon)) )
					 leastCommon = a;
			 }
		 }
		 if (leastCommon == null) {
//			 return null;
			 HashSet<AttributedElementClass> classesWithDirectSuperclasses = new HashSet<AttributedElementClass>();
			 classesWithDirectSuperclasses.addAll(classes);
			 for (AttributedElementClass a : classes) {
				 classesWithDirectSuperclasses.addAll(a.getDirectSuperClasses());
			 }
			 leastCommon = calculateLeastCommonSuperclass(classesWithDirectSuperclasses);
		 }
		 return leastCommon;
	}
	

}
