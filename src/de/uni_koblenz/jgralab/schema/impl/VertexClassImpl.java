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
 
package de.uni_koblenz.jgralab.schema.impl;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import de.uni_koblenz.jgralab.EdgeDirection;
import de.uni_koblenz.jgralab.schema.AttributedElementClass;
import de.uni_koblenz.jgralab.schema.DirectedEdgeClass;
import de.uni_koblenz.jgralab.schema.EdgeClass;
import de.uni_koblenz.jgralab.schema.GraphClass;
import de.uni_koblenz.jgralab.schema.QualifiedName;
import de.uni_koblenz.jgralab.schema.RolenameEntry;
import de.uni_koblenz.jgralab.schema.SchemaException;
import de.uni_koblenz.jgralab.schema.VertexClass;

public class VertexClassImpl extends GraphElementClassImpl implements VertexClass {
	
	private Set<DirectedEdgeClass> associatedEdges;

	/**
	 * builds a new vertex class object
	 * @param name the unique identifier of the vertex class in
	 * the schema
	 */
	public VertexClassImpl(QualifiedName qn, GraphClass aGraphClass) {
		super(qn, aGraphClass);
		associatedEdges = new HashSet<DirectedEdgeClass>();
	}

	@Override
	public String getVariableName() {
		return "vc_" + getQualifiedName().replace('.', '_');
	}
	
	@Override
	public void addSuperClass(VertexClass superClass)  {
		super.addSuperClass(superClass);
	}

	@Override
	public void addEdgeClass(EdgeClass anEdgeClass) {
		if (anEdgeClass.getTo() == this) {
			DirectedEdgeClass dec = anEdgeClass.getInEdgeClass();
			associatedEdges.add(dec);
		}
		if (anEdgeClass.getFrom() == this) {
			DirectedEdgeClass dec = anEdgeClass.getOutEdgeClass();
			associatedEdges.add(dec);
		}
	}

	@Override
	public Set<EdgeClass> getOwnEdgeClasses() {
		Set<EdgeClass> s = new HashSet<EdgeClass>();
		for (DirectedEdgeClass dec : associatedEdges)
			s.add(dec.getEdgeClass());
		return s;
	}

	@Override
	public HashSet<EdgeClass> getEdgeClasses() {
		HashSet<EdgeClass> allEdgeClasses = new HashSet<EdgeClass>();
		allEdgeClasses.addAll(getOwnEdgeClasses());
		for (AttributedElementClass sc :  getAllSuperClasses())
			allEdgeClasses.addAll(((VertexClass) sc).getOwnEdgeClasses());
		HashSet<EdgeClass> edgeClassesWithSuperclasses = new HashSet<EdgeClass>();
		edgeClassesWithSuperclasses.addAll(allEdgeClasses);
		for (EdgeClass ec : allEdgeClasses) 
			for (AttributedElementClass ac : ec.getAllSuperClasses())
				edgeClassesWithSuperclasses.add((EdgeClass) ac);
		return allEdgeClasses;
	}

	@Override
	public Set<DirectedEdgeClass> getOwnDirectedEdgeClasses() {
		return associatedEdges;
	}
	
	@Override
	public Set<DirectedEdgeClass> getDirectedEdgeClasses() {
		Set<DirectedEdgeClass> set = new HashSet<DirectedEdgeClass>();
		for (AttributedElementClass aec : getDirectSuperClasses()) { 
			VertexClass vc = (VertexClass) aec;
			set.addAll(vc.getDirectedEdgeClasses());
		}
		set.addAll(getOwnDirectedEdgeClasses());
		return set;
	}
	
	
	@Override
	public Set<DirectedEdgeClass> getValidDirectedEdgeClasses() {
		Set<DirectedEdgeClass> validClasses = new HashSet<DirectedEdgeClass>();
		Map<String, RolenameEntry> rolenameMap = getRolenameMap();
		for (DirectedEdgeClass ec : getDirectedEdgeClasses()) {
			RolenameEntry entry = rolenameMap.get(ec.getThatRolename());
			if ((entry == null) || (!entry.isRedefined())) {
				validClasses.add(ec);
			} 
		}
		return validClasses;
	}
	

	@Override
	public Set<EdgeClass> getValidFromEdgeClasses() {
		Set<EdgeClass> validFrom = new HashSet<EdgeClass>();
		for (DirectedEdgeClass dec : getValidDirectedEdgeClasses()) {
			if ((!dec.getEdgeClass().isInternal()) && (!dec.getEdgeClass().isAbstract())) {
				if (dec.getDirection() == EdgeDirection.OUT)
					validFrom.add(dec.getEdgeClass());
			}	
		}
		return validFrom;
	}
	
	@Override
	public Set<EdgeClass> getValidToEdgeClasses() {
		Set<EdgeClass> validTo = new HashSet<EdgeClass>();
		for (DirectedEdgeClass dec : getValidDirectedEdgeClasses()) {
			if ((!dec.getEdgeClass().isInternal()) && (!dec.getEdgeClass().isAbstract())) {
				if (dec.getDirection() == EdgeDirection.IN)
					validTo.add(dec.getEdgeClass());
			}	
		}
		return validTo;
	}
	
	
	@Override
	public Map<String, RolenameEntry> getRolenameMap() {
		Map<String, RolenameEntry> allMap = new HashMap<String, RolenameEntry>();
		Map<String, RolenameEntry> ownMap = new HashMap<String, RolenameEntry>();
		Set<String> rolenamesThatMustBeRedefined = new HashSet<String>();
		/* holds the set of all rolenames that are redefined.
		 * every rolename may be redefined only once 
		 */
		Set<String> allRedefinedRolenames = new HashSet<String>();
		/*
		 * add rolenames of all superclasses
		 * - if a rolename occurs twice, test if it originates
		 *   from the same superclass, otherwise throw an exception
		 */
		for (AttributedElementClass aec : getDirectSuperClasses()) {
			VertexClass vc = (VertexClass) aec;
			for (RolenameEntry entry : vc.getRolenameMap().values()) {
				entry.setInherited(true);
				if (allMap.containsKey(entry.getRoleNameAtFarEnd())) {
					RolenameEntry foreign = allMap.get(entry.getRoleNameAtFarEnd());
					//the same class is specialized via two different direct superclasses
					if(foreign.getVertexClassDefiningRolename() == entry.getVertexClassDefiningRolename())
						continue;
					rolenamesThatMustBeRedefined.add(entry.getRoleNameAtFarEnd());
				} else {
					allMap.put(entry.getRoleNameAtFarEnd(), entry);					
				}
			}
		}
		/*
		 * for all connected edge classes
		 *  - add the redefined far roles to the set "allRedefinedRolenames"
		 *    if a role is redefined twice, throw an exception
		 *  - add the new rolename to the list of own rolenames
		 */
		for (DirectedEdgeClass dec : getOwnDirectedEdgeClasses()) {
			String roleName = dec.getThatRolename();
			if (roleName == null || roleName.isEmpty()|| roleName.equals("") )
				continue;
			Set<String> redefinedRolenames = dec.getRedefinedThatRolenames();
			for (String redefinedRole : redefinedRolenames) {
				rolenamesThatMustBeRedefined.remove(redefinedRole);
				if (redefinedRole.equals(roleName))
					continue;
				if (allRedefinedRolenames.contains((redefinedRole))) {
					throw new SchemaException("Rolename " + redefinedRole + " may be redefined only once at vertex " + getSimpleName() + ".");
				}	
				allRedefinedRolenames.add(redefinedRole);
			}
			VertexClass vc = dec.getDirection() == EdgeDirection.IN ? dec.getEdgeClass().getFrom() : dec.getEdgeClass().getTo();
			RolenameEntry entry = new RolenameEntry(this, roleName, dec, vc);
			ownMap.put(roleName, entry);
		}
		
		/*
		 * check for all redefined rolenames if the redefinition is allowed 
		 */
		for (String redefinedRole : allRedefinedRolenames) {
			/* check if redefined name occurs at the same class as rolename*/
			if (ownMap.containsKey(redefinedRole))
				throw new SchemaException("Cannot redefine rolename " + redefinedRole + " for it is used as non-redefined rolename at the same vertex class");
			/* check if rolename is not inherited from a supoerclass */
			RolenameEntry entry = allMap.get(redefinedRole);
			if (entry == null)
				throw new SchemaException("Cannot redefine rolename " + redefinedRole + " that is not inherited from a superclass");
			entry.setRedefined(true);
		}
		
		/*
		 * put all new rolenames to the list of all rolenames
		 */
		for (RolenameEntry entry : ownMap.values()) {
			if (allMap.containsKey(entry.getRoleNameAtFarEnd())) {
				RolenameEntry inheritedEntry = allMap.get(entry.getRoleNameAtFarEnd()); 
				inheritedEntry.addVertexWithEdge(entry.getVertexClassAtFarEnd(), entry.getEdgeClassToTraverse());
				inheritedEntry.setInherited(true);				
			} else {
				allMap.put(entry.getRoleNameAtFarEnd(), entry);
			}	
		}
				
		for (String s : rolenamesThatMustBeRedefined) {
			throw new SchemaException("Multiple inherited rolename '" + s + "' must be redefined at vertexclass " + getQualifiedName());
		}
		return allMap;
	}
}
