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
 
package de.uni_koblenz.jgralab;

import java.util.Collection;
import java.util.Set;
import java.util.SortedSet;

/**
 * aggregates graph/vertex/edge/aggregation/composition classes
 * @author Steffen Kahle
 *
 */
public interface AttributedElementClass extends Comparable<AttributedElementClass> {

	/**
	 * @param anAttributedElementClass the class to search for
	 * @return true, if anAttributedElementClass is a direct or indirect superclass of this class
	 */
	public boolean isSuperClassOf(AttributedElementClass anAttributedElementClass);
	
	/**
	 * @param anAttributedElementClass the class to search for
	 * @return true, if anAttributedElementClass is a direct superclass of this class
	 */
	public boolean isDirectSuperClassOf(AttributedElementClass anAttributedElementClass);

	/**
	 * @param anAttributedElementClass the class to search for
	 * @return true, if anAttributedElementClass is a direct subclass of this class
	 */
	public boolean isDirectSubClassOf(AttributedElementClass anAttributedElementClass);

	/**
	 * @param anAttributedElementClass the class to search for
	 * @return true, if anAttributedElementClass is a direct or indirect subclass of this class
	 */
	public boolean isSubClassOf(AttributedElementClass anAttributedElementClass);
	
	/**
	 * Tests if the given Class is a subclass of this class or equal to this class
	 * @param anAttributedElementClass the class to test
	 * @return true iff the given AttributedElementClass is a subclass of this class or is this class
	 * itself
	 */
	public boolean isSuperClassOfOrEquals(AttributedElementClass anAttributedElementClass);
	
	/**
	 * @return the set of direct superclasses this class is subclass of
	 */
	public Set<AttributedElementClass> getDirectSuperClasses();
	
	/**
	 * @return the set of direct and indirect superclasses
	 */
	public Set<AttributedElementClass> getAllSuperClasses(); 

	/**
	 * @return the unique name of the element in the schema
	 */
	public String getName();

	/**
	 * adds a new attribute to the element
	 * @param name the attribute name
	 * @param domain the attribute domain
	 */
	public void addAttribute(String name, Domain domain) ;

	/**
	 * adds a new attribute to the element
	 * @param anAttribute the attribute to be added
	 */
	public void addAttribute(Attribute anAttribute) ;
	
	/**
	 * adds a whole list of attributes to the element
	 * @param attrs the list of attributes to be appended
	 */
	public void addAttributes(Collection<Attribute> attrs) ;

	/**
	 * @param name the name of the attribute
	 * @return the attribute with the specified name 
	 */
	public Attribute getAttribute(String name);
	
	/**
	 * @param name the name of the attribute
	 * @return the attribute with the specified name 
	 */
	public Attribute getOwnAttribute(String name);

	/**
	 * @return the list of attributes of this element without inherited attributes
	 */
	public SortedSet<Attribute> getOwnAttributeList();
	
	/**
	 * @return the list of attributes of this element and all inherited attributes
	 */
	public SortedSet<Attribute> getAttributeList();

	/**
	 * @param name the name of the attribute to search for
	 * @return true, if the element or its superclasses
	 * contains an attribute with the specified name
	 */
	public boolean containsAttribute(String name);

	/**
	 * @return the number of attributes this element holds including
	 * inherited attributes
	 */
	public int getAttributeCount();
	
	/**
	 * @return the number of attributes this element holds without
	 * inherited attributes
	 */
	public int getOwnAttributeCount();

	/**
	 * this method has to be implemented by each specialised class,
	 * it has to return a textual representation of that class
	 */
	public abstract String toString();

	/**
	 * @return true, if the element may not have any instances
	 */
	public boolean isAbstract();

	/**
	 * defines if the element may or may not have any instances
	 * @param isAbstract true or false
	 */
	public void setAbstract(boolean isAbstract);
	
	/**
	 * @return all direct subclasses of this class
	 */
	public Set<AttributedElementClass> getDirectSubClasses();
	
	/**
	 * @return all direct and indirect subclasses of this class
	 */
	public Set<AttributedElementClass> getAllSubClasses();
	
	/**
	 * @return true, if the element has own or inherited attributes, false otherwise
	 */
	public boolean hasAttributes();
	
	/**
	 * Checks if this AttributedElementClass has own attributes
	 * @return true if this AttributedElementClass contains at least one non-inherited attributed, false otherwise
	 */
	public boolean hasOwnAttributes();
	
	/**
	 * @return true, if this AttributedElementClass is only for internal use
	 */
	public boolean isInternal();

	/**
	 * sets the <code>internal</code>-flag of this AttributedElementClass, if this is set, the AttributedElementClass is
	 * only for internal use (for instance, the AttributedElementClasses 'Graph', 'Vertex', 'Edge', 'Composition' and 'Aggregation'
	 * are such AttributedElementClasses only for internal use
	 * @param internal
	 */
	public void setInternal(boolean internal);
	
	/**
	 * @return the schema this AttributedElementClass belongs to
	 */
	public Schema getSchema();
	
	/**
	 * @return the M1 interface class for this AttributedElementClass if reflection exceptions occures
	 */
	public <T extends AttributedElement> Class<T> getM1Class() ;
	
	/**
	 * @return the M1 implementation class for this AttributedElementClass if this AttributedElementClass is abstract, or upon reflection exceptions
	 */
	public <T extends AttributedElement> Class<T> getM1ImplementationClass() ;
	
	/**
	 * Returns the least common superclass of this class and the given class <code>other</code> 
	 * @return the least common superclass
	 */
	public AttributedElementClass getLeastCommonSuperclass(AttributedElementClass other);
	
	/**
	 * Returns the least common superclass of this class and the classes in the set <code>other</code> 
	 * @return the least common superclass
	 */
	public AttributedElementClass getLeastCommonSuperclass(Set<? extends AttributedElementClass> other);
}
