/*
 * JGraLab - The Java graph laboratory
 * (c) 2006-2008 Institute for Software Technology
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

package de.uni_koblenz.jgralab.schema.impl;

import java.lang.reflect.Field;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import de.uni_koblenz.jgralab.Attribute;
import de.uni_koblenz.jgralab.AttributedElement;
import de.uni_koblenz.jgralab.M1ClassManager;
import de.uni_koblenz.jgralab.schema.AttributedElementClass;
import de.uni_koblenz.jgralab.schema.Domain;
import de.uni_koblenz.jgralab.schema.Package;
import de.uni_koblenz.jgralab.schema.QualifiedName;
import de.uni_koblenz.jgralab.schema.Schema;
import de.uni_koblenz.jgralab.schema.exception.DuplicateAttributeException;
import de.uni_koblenz.jgralab.schema.exception.InheritanceException;
import de.uni_koblenz.jgralab.schema.exception.M1ClassAccessException;
import de.uni_koblenz.jgralab.schema.exception.ReservedWordException;

public abstract class AttributedElementClassImpl implements
		AttributedElementClass {

	/**
	 * toggles if this class is only for internal use
	 */
	private boolean internal = false;

	/**
	 * the package this attributed element class belongs to
	 */
	private Package pkg;

	/**
	 * the immediate super classes of this class
	 */
	protected HashSet<AttributedElementClass> directSuperClasses;

	/**
	 * the immediate sub classes of this class
	 */
	protected HashSet<AttributedElementClass> directSubClasses;

	/**
	 * a list of attributes which belongs to the m2 element
	 * (edgeclass/vertexclass/graphclass). Only the own attributes of this class
	 * are stored here, no inherited attributes
	 */
	private TreeSet<Attribute> attributeList;

	/**
	 * a unique identifier of the m2 element in the schema
	 * (edgeclass/vertexclass/graphclass)
	 */
	private QualifiedName qName;

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

	@Override
	public void setPackage(Package p) {
		pkg = p;
	}

	@Override
	public Package getPackage() {
		return pkg;
	}

	/**
	 * builds a new attributed element class
	 *
	 * @param qn
	 *            the unique identifier of the element in the schema
	 */
	public AttributedElementClassImpl(QualifiedName qn) {
		qName = qn;
		m1Class = null;
		m1ImplementationClass = null;
		attributeList = new TreeSet<Attribute>();
		directSubClasses = new HashSet<AttributedElementClass>();
		directSuperClasses = new HashSet<AttributedElementClass>();
	}

	@Override
	public String getSimpleName() {
		return qName.getSimpleName();
	}

	@Override
	public String getQualifiedName() {
		return qName.getQualifiedName();
	}

	@Override
	public String getQualifiedName(Package pkg) {
		if (this.pkg == pkg) {
			return qName.getSimpleName();
		} else if (this.pkg.isDefaultPackage()) {
			return "." + qName.getSimpleName();
		} else {
			return qName.getQualifiedName();
		}
	}

	@Override
	public String getUniqueName() {
		return qName.getUniqueName();
	}

	@Override
	public void setUniqueName(String uniqueName) {
		qName.setUniqueName(this, uniqueName);
	}

	@Override
	public String getPackageName() {
		return qName.getPackageName();
	}

	@Override
	public String getDirectoryName() {
		return qName.getDirectoryName();
	}

	@Override
	public String getPathName() {
		return qName.getPathName();
	}

	@Override
	public QualifiedName getQName() {
		return qName;
	}

	@Override
	public void addAttribute(String name, Domain domain) {
		addAttribute(new AttributeImpl(name, domain));
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
					"Duplicate Attribute "
							+ anAttribute.getName()
							+ " in AttributedElementClass "
							+ getQualifiedName()
							+ ". "
							+ "A derived AttributedElementClass already contains this Attribute.");
		}
		if (Schema.reservedTGWords.contains(anAttribute.getName())
				|| Schema.reservedJavaWords.contains(anAttribute.getName())) {
			throw new ReservedWordException(anAttribute.getName(),
					"attribute name");
		}
		attributeList.add(anAttribute);
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
	public void addAttributes(Collection<Attribute> attrs) {
		for (Attribute a : attrs) {
			addAttribute(a);
		}
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
	public SortedSet<Attribute> getOwnAttributeList() {
		return attributeList;
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
	public boolean containsAttribute(String name) {
		return (getAttribute(name) != null);
	}

	@Override
	public int getOwnAttributeCount() {
		return attributeList.size();
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

	@Override
	public boolean isAbstract() {
		return isAbstract;
	}

	@Override
	public void setAbstract(boolean isAbstract) {
		this.isAbstract = isAbstract;
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
		for (AttributedElementClass c : directSuperClasses) {
			if ((c != getSchema().getDefaultAggregationClass())
					&& (c
							.isSubClassOf(getSchema()
									.getDefaultAggregationClass()))) {
				directSuperClasses.remove(getSchema()
						.getDefaultAggregationClass());
				break;
			}
		}
		for (AttributedElementClass c : directSuperClasses) {
			if ((c != getSchema().getDefaultCompositionClass())
					&& (c
							.isSubClassOf(getSchema()
									.getDefaultCompositionClass()))) {
				directSuperClasses.remove(getSchema()
						.getDefaultCompositionClass());
				break;
			}
		}
		for (Attribute a : superClass.getAttributeList()) {
			if (getOwnAttribute(a.getName()) != null) {
				throw new InheritanceException("Cannot add "
						+ superClass.getQualifiedName() + " as superclass of "
						+ getQualifiedName() + ", cause: Attribute "
						+ a.getName() + " is declared in both classes");
			}
		}
		if (superClass.isSubClassOf(this)) {
			throw new InheritanceException(
					"Cycle in class hierarchie for classes: "
							+ getQualifiedName() + " and "
							+ superClass.getQualifiedName());
		}
		directSuperClasses.add(superClass);
		((AttributedElementClassImpl) superClass).directSubClasses.add(this);
	}

	@Override
	public boolean isSuperClassOf(
			AttributedElementClass anAttributedElementClass) {
		// System.out.println(this.getName() + " is superclass of " +
		// anAttributedElementClass.getName() + ": " +
		// anAttributedElementClass.getAllSuperClasses().contains(this));
		return anAttributedElementClass.getAllSuperClasses().contains(this);
	}

	@Override
	public boolean isDirectSuperClassOf(
			AttributedElementClass anAttributedElementClass) {
		return (((AttributedElementClassImpl) anAttributedElementClass).directSuperClasses
				.contains(this));
	}

	@Override
	public boolean isSuperClassOfOrEquals(
			AttributedElementClass anAttributedElementClass) {
		return ((this == anAttributedElementClass) || (isSuperClassOf(anAttributedElementClass)));
	}

	@Override
	public boolean isSubClassOf(AttributedElementClass anAttributedElementClass) {
		return getAllSuperClasses().contains(anAttributedElementClass);
	}

	@Override
	public boolean isDirectSubClassOf(
			AttributedElementClass anAttributedElementClass) {
		return directSuperClasses.contains(anAttributedElementClass);
	}

	@Override
	public Set<AttributedElementClass> getDirectSuperClasses() {
		return new HashSet<AttributedElementClass>(directSuperClasses);
	}

	@Override
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
	public Set<AttributedElementClass> getDirectSubClasses() {
		return directSubClasses;
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
	public boolean isInternal() {
		return internal;
	}

	@Override
	public void setInternal(boolean internal) {
		this.internal = internal;
	}

	@Override
	public int compareTo(AttributedElementClass another) {
		return qName.compareTo(another.getQName());
	}

	@SuppressWarnings("unchecked")
	public Class<? extends AttributedElement> getM1Class() {
		if (m1Class == null) {
			String m1ClassName = getSchema().getPackageName() + "."
					+ getQualifiedName();
			try {
				m1Class = (Class<? extends AttributedElement>) Class.forName(
						m1ClassName, true, M1ClassManager.instance());
			} catch (ClassNotFoundException e) {
				throw new M1ClassAccessException(
						"Can't load M1 class for AttributedElementClass '"
								+ getQualifiedName() + "'", e);
			}
		}
		return m1Class;
	}

	@SuppressWarnings("unchecked")
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
	public AttributedElementClass getLeastCommonSuperclass(
			AttributedElementClass other) {
		HashSet<AttributedElementClass> classes = new HashSet<AttributedElementClass>();
		classes.add(this);
		classes.add(other);
		return calculateLeastCommonSuperclass(classes);
	}

	@Override
	public AttributedElementClass getLeastCommonSuperclass(
			Set<? extends AttributedElementClass> other) {
		HashSet<AttributedElementClass> classes = new HashSet<AttributedElementClass>();
		classes.add(this);
		classes.addAll(other);
		return calculateLeastCommonSuperclass(classes);
	}

	public static AttributedElementClass calculateLeastCommonSuperclass(
			Set<? extends AttributedElementClass> classes) {
		AttributedElementClass leastCommon = null;
		for (AttributedElementClass a : classes) {
			boolean leastCommonCandidate = true;
			for (AttributedElementClass b : classes) {
				// if (a == null)
				// System.out.println(" A is null");
				if (!a.isSuperClassOfOrEquals(b)) {
					// System.out.println(a.getName() + " is not a superclass of
					// " + b.getName());
					leastCommonCandidate = false;
					break;
				}
			}
			if (leastCommonCandidate) {
				// System.out.println("Found least common candidate: " +
				// leastCommon);
				if ((leastCommon == null) || (a.isSubClassOf(leastCommon))) {
					leastCommon = a;
				}
			}
		}
		if (leastCommon == null) {
			// return null;
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
