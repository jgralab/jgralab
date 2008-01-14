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

import java.util.List;
import java.util.Set;

/**
 * aggregates edge/aggregation/composition classes,
 * instances of this class represent an m2 element
 * @author Steffen Kahle
 *
 */
public interface EdgeClass extends GraphElementClass {

	/**
	 * adds a superclass to the list of superclasses,
	 * all attributes get inherited from those classes
	 * @param superClass the edge class to be added to the
	 * list of superclasses if an attribute name exists in superClass and in this class
	 * 
	 */
	public void addSuperClass(EdgeClass superClass) ;

	/**
	 * @return the vertex class where the edge class originates
	 */
	public VertexClass getFrom();
	
	/**
	 * @return the maximum multiplicity at the from-side
	 */
	public int getFromMax();

	/**
	 * @return the minimum multiplicity at the from-side
	 */
	public int getFromMin();

	/**
	 * @return the rolename on the from-side
	 */
	public String getFromRolename();
	
	/**
	 * @return the set of rolenames that are redefined by the rolename on the from-side
	 */
	public Set<String> getRedefinedFromRoles();
	
	/**
	 * Redefines the <code>redefinedRoleName</code> with the rolename
	 * defined while the creation of that edge.
	 * That means on the one hand, that edges of this class have the new role name
	 * as roleName on the from-end and on the other hand that the redefined
	 * edge is not longer allowed at the from-vertex class of this edge 
	 * @param redefinedRoleName the rolename to redefine
	 */
	public void redefineFromRole(String redefinedRoleName);
	
	/**
	 * Redefines all <code>redefinedRoleNames</code> with the rolename
	 * defined while the creation of that edge
	 * That means on the one hand, that edges of this class have the new role name
	 * as roleName on the from-end and on the other hand that the redefined
	 * edges are not longer allowed at the from-vertex class of this edge 
	 * @param redefinedRoleNames the rolenames to redefine
	 */
	public void redefineFromRole(List<String> redefinedRoleNames);

	/**
	 * @return the vertex class where the edge class closes 
	 */
	public VertexClass getTo();

	/**
	 * @return the maximum multiplicity at the to-side
	 */
	public int getToMax();

	/**
	 * @return the minimum mulitplicity at the to-side
	 */
	public int getToMin();

	/**
	 * @return the rolename on the to-side
	 */
	public String getToRolename();
	
	/**
	 * @return the set of rolenames that are redefined by the rolename on the to-side
	 */
	public Set<String> getRedefinedToRoles();
	
	/**
	 * Redefines the <code>redefinedRoleName</code> with the rolename
	 * defined while the creation of that edge
	 * That means on the one hand, that edges of this class have the new role name
	 * as roleName on the to-end and on the other hand that the redefined
	 * edge is not longer allowed at the to-vertex class of this edge 
	 * @param redefinedRoleName the rolename to redefine
	 */
	public void redefineToRole(String redefinedRoleName);
	
	/**
	 * Redefines all <code>redefinedRoleNames</code> with the rolename
	 * defined while the creation of that edge
	 * That means on the one hand, that edges of this class have the new role name
	 * as roleName on the to-end and on the other hand that the redefined
	 * edges are not longer allowed at the to-vertex class of this edge 
	 * @param redefinedRoleNames the rolenames to redefine
	 */
	public void redefineToRole(List<String> redefinedRoleNames);
	
	/**
	 * @return true, if the connectable VertexClasses and cardinalities of this EdgeClass
	 * satisfy the restrictions of its superclasses 
	 */
	public boolean checkConnectionRestrictions();
	
	/**
	 * Tries to merge the cardinalities of the edges endpoints
	 * @return true if a merge was done successfull, false if no merge was needed or if a merge is not possible
	 *
	 */
	public boolean mergeConnectionCardinalities() ;
	
	
	/**
	 * Tries to merge the VertexClasses of the edges endpoints
	 * @return true if a merge was done successfull, false if no merge was needed
	 * or if a merge is not possible
	 */
	public boolean mergeConnectionVertexClasses() ;
	
	
	/**
	 * @return returns the DirectedEdgeClass-Object consisting of this edge class with 
	 * direction EdgeDirection.IN
	 *
	 */
	public DirectedEdgeClass getInEdgeClass();
	
	/**
	 * @return returns the DirectedEdgeClass-Object consisting of this edge class with 
	 * direction EdgeDirection.OUT
	 *
	 */
	public DirectedEdgeClass getOutEdgeClass();
	
}