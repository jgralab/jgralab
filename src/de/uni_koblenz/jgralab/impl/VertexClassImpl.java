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

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import de.uni_koblenz.jgralab.AttributedElementClass;
import de.uni_koblenz.jgralab.DirectedEdgeClass;
import de.uni_koblenz.jgralab.EdgeClass;
import de.uni_koblenz.jgralab.EdgeDirection;
import de.uni_koblenz.jgralab.GraphClass;
import de.uni_koblenz.jgralab.RolenameEntry;
import de.uni_koblenz.jgralab.Vertex;
import de.uni_koblenz.jgralab.VertexClass;

public class VertexClassImpl extends GraphElementClassImpl implements VertexClass {
	
	/**
	 * If this constant field is set to true, all edge classes may
	 * redefine a rolename, otherwise only edgeclasses that are a
	 * subclass of the original defining class may redefine the rolename
	 */
	private static final boolean ALLOW_ALL_EDGECLASSES_REDEFINE_ROLENAMES = false;
	
	/**
	 * Every VertexClass contains a Map from rolename to a RolenameEntry, consisting of
	 * 	- The least common vertex class for this role
	 *  - The least common edge class for this role
	 *  Out of the <code>rolenameMap</code> the <code>getRolenameList</code>-methods are generated.
	 */
	
	private Map<String, RolenameEntry> rolenameMap;
	
	
	private Set<DirectedEdgeClass> associatedEdges;

	/**
	 * builds a new vertex class object
	 * @param name the unique identifier of the vertex class in
	 * the schema
	 */
	public VertexClassImpl(String name, GraphClass aGraphClass) {
		super(name, aGraphClass);
		rolenameMap = new HashMap<String, RolenameEntry>();
		associatedEdges = new HashSet<DirectedEdgeClass>();
	}
	
	/* (non-Javadoc)
	 * @see jgralab.VertexClass#addSuperClass(jgralab.VertexClass)
	 */
	public void addSuperClass(VertexClass superClass)  {
		// inherit connection constraints
		super.addSuperClass(superClass);
		for (Entry<String, RolenameEntry> entry : rolenameMap.entrySet()) {
			updateRole(entry.getKey(), entry.getValue().getLeastCommonEdgeClass(), entry.getValue().getLeastCommonVertexClass());
		}
	}


	
	/* (non-Javadoc)
	 * @see jgralab.VertexClass#addEdgeClass(jgralab.EdgeClass)
	 */
	public void addEdgeClass(EdgeClass anEdgeClass) {
		/*
		 * The rolenames at the FAR (that) end of the connected EdgeClasses are stored in
		 * the rolename map since out of the rolename map the getRolenameList-methods are created
		 * 
		 */
		if (anEdgeClass.getTo() == this) {
			DirectedEdgeClass dec = anEdgeClass.getInEdgeClass();
			updateRole(anEdgeClass.getFromRolename(), anEdgeClass, anEdgeClass.getFrom());
			associatedEdges.add(dec);
		}
		if (anEdgeClass.getFrom() == this) {
			DirectedEdgeClass dec = anEdgeClass.getOutEdgeClass();
			updateRole(anEdgeClass.getToRolename(), anEdgeClass, anEdgeClass.getTo());
			associatedEdges.add(dec);
		}
	}



	
	/* (non-Javadoc)
	 * @see jgralab.VertexClass#getEdgeClasses()
	 */
	public Set<EdgeClass> getOwnDirectEdgeClasses() {
		Set<EdgeClass> s = new HashSet<EdgeClass>();
		for (DirectedEdgeClass dec : associatedEdges)
			s.add(dec.getEdgeClass());
		return s;
	}

	
	/* (non-Javadoc)
	 * @see jgralab.VertexClass#getAllEdgeClasses()
	 */
	public HashSet<EdgeClass> getEdgeClasses() {
		HashSet<EdgeClass> allEdgeClasses = new HashSet<EdgeClass>();
		allEdgeClasses.addAll(getOwnDirectEdgeClasses());
		for (AttributedElementClass sc :  getAllSuperClasses())
			allEdgeClasses.addAll(((VertexClass) sc).getOwnDirectEdgeClasses());
		HashSet<EdgeClass> edgeClassesWithSuperclasses = new HashSet<EdgeClass>();
		edgeClassesWithSuperclasses.addAll(allEdgeClasses);
		for (EdgeClass ec : allEdgeClasses) 
			for (AttributedElementClass ac : ec.getAllSuperClasses())
				edgeClassesWithSuperclasses.add((EdgeClass) ac);
		return allEdgeClasses;
	}

	
	public Set<DirectedEdgeClass> getOwnDirectedEdgeClasses() {
		return associatedEdges;
	}
	
	public Set<DirectedEdgeClass> getDirectedEdgeClasses() {
		Set<DirectedEdgeClass> set = new HashSet<DirectedEdgeClass>();
		for (AttributedElementClass aec : getDirectSuperClasses()) { 
			VertexClass vc = (VertexClass) aec;
			set.addAll(vc.getDirectedEdgeClasses());
		}
		set.addAll(getOwnDirectedEdgeClasses());
		return set;
	}
	
	public Set<DirectedEdgeClass> getValidDirectedEdgeClasses() {
		Set<DirectedEdgeClass> inheritedClasses = getDirectedEdgeClasses();
		inheritedClasses.removeAll(getOwnDirectedEdgeClasses());
		Set<DirectedEdgeClass> validClasses = new HashSet<DirectedEdgeClass>();
		validClasses.addAll(getOwnDirectedEdgeClasses());
		/* remove all triples from the inherited triples that are redefined
		 * (their rolename is redefined by a subclass of their edgeclass
		 *  that is also part of a triple in the inherited triples)
		 *  in the inherited triples
		 */
		Iterator<DirectedEdgeClass> iter = inheritedClasses.iterator();
		while (iter.hasNext()) {
			DirectedEdgeClass ec = iter.next();
			String iterRolename = ec.getThisRolename();
			if (iterRolename.equals(""))
				continue;
			for (DirectedEdgeClass t : inheritedClasses) {
				if (t.getDirection() != ec.getDirection()) 
					continue;
				if (t.getRedefinedThisRolenames().contains(iterRolename)) {
					if (ALLOW_ALL_EDGECLASSES_REDEFINE_ROLENAMES || t.getEdgeClass().isSubClassOf(ec.getEdgeClass())) {
						iter.remove();
						break;
					}	
				}
			}
		}
		/*
		 * remove all inherited triples that are redefined by a triple
		 * of the ownTriples
		 */
		iter = inheritedClasses.iterator();
		while (iter.hasNext()) {
			DirectedEdgeClass ec = iter.next();
			String iterRolename = ec.getThisRolename();
			if (iterRolename.equals(""))
				continue;
			for (DirectedEdgeClass t : validClasses) {
				if (t.getDirection() != ec.getDirection()) 
					continue;
				if (t.getRedefinedThisRolenames().contains(iterRolename)) {
					if (ALLOW_ALL_EDGECLASSES_REDEFINE_ROLENAMES || t.getEdgeClass().isSubClassOf(ec.getEdgeClass())) {
						iter.remove();
						break;
					}	
				}
			}
		}
		validClasses.addAll(inheritedClasses);
		return validClasses;
	}
	
	
	public Set<DirectedEdgeClass> getInvalidDirectedClasses() {
		Set<DirectedEdgeClass> invalidClasses = getDirectedEdgeClasses();
		Set<DirectedEdgeClass> validClasses = getValidDirectedEdgeClasses();
		invalidClasses.removeAll(validClasses);
		/* remove all elements that have a far rolename that is redefined
		 * in a subclass with the same name 
		 */
		Iterator<DirectedEdgeClass> iter = invalidClasses.iterator();
		while (iter.hasNext()) {
			String iterRole = iter.next().getThisRolename();
			for (DirectedEdgeClass t : validClasses) {
				if (iterRole.equals(t.getThisRolename())) {
					iter.remove();
					break;
				}
			}
		}
		return invalidClasses;
	}

	
	public Set<String> getInvalidRoles() {
		Set<String> invalidRoles = new HashSet<String>();
		for (DirectedEdgeClass ec : getInvalidDirectedClasses()) {
			invalidRoles.add(ec.getThisRolename());
		}
		return invalidRoles;
	}
		
	/*
	 * iterates over all superclasses and subclasses and changes the VertexClass
	 * for this role to the least common superclass of all of them
	 */
	private void updateRole(String role, EdgeClass edgeClass, VertexClass vertexClass) {
		if ((role == null) || (role.equals("")))
			return;
		RolenameEntry r = getRolenameEntry(role);
		if (r == null ){
			r = new RolenameEntry(edgeClass, vertexClass);
			rolenameMap.put(role, r);
		}
		HashSet<AttributedElementClass> allClasses = new HashSet<AttributedElementClass>();
		allClasses.addAll(getAllSuperClasses());
		HashSet<AttributedElementClass> superClasses = new HashSet<AttributedElementClass>();
		allClasses.addAll(getAllSuperClasses());
		superClasses.add(this);
		allClasses.addAll(getAllSubClasses());
		allClasses.add(this);
		HashSet<VertexClass> vertexClasses = new HashSet<VertexClass>();
		vertexClasses.add(vertexClass);
		HashSet<EdgeClass> edgeClasses = new HashSet<EdgeClass>();
		edgeClasses.add(edgeClass);
		for (AttributedElementClass c : allClasses) {
			VertexClass vc = (VertexClass) c;
			r = vc.getRolenameEntry(role);
			if (r != null) {
				vertexClasses.add(r.getLeastCommonVertexClass());
				edgeClasses.add(r.getLeastCommonEdgeClass());
			}
		}
		if (!vertexClasses.isEmpty()) {
			VertexClass leastCommonVertexSuperclass = (VertexClass) AttributedElementClassImpl.calculateLeastCommonSuperclass(vertexClasses);
			EdgeClass leastCommonEdgeSuperclass = (EdgeClass) AttributedElementClassImpl.calculateLeastCommonSuperclass(edgeClasses);
			for (AttributedElementClass c : allClasses) {
				VertexClass vc = (VertexClass) c;
				r = vc.getRolenameEntry(role);
				if (r != null) {
					if ((r.getLeastCommonEdgeClass() != leastCommonEdgeSuperclass)
					    || (r.getLeastCommonVertexClass() != leastCommonVertexSuperclass)) {
						r.setLeastCommonEdgeClass(leastCommonEdgeSuperclass);
						r.setLeastCommonVertexClass(leastCommonVertexSuperclass);
						((VertexClassImpl)vc).updateRole(role, leastCommonEdgeSuperclass, leastCommonVertexSuperclass);
					}	
				}	
			}
		}		
	}
	
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

	public RolenameEntry getRolenameEntry(String rolename) {
		return rolenameMap.get(rolename);
	}
	
	public Map<String, RolenameEntry> getOwnRolenameMap() {
		return rolenameMap;
	}
	
	public Map<String, RolenameEntry> getRolenameMap() {
		Map<String, RolenameEntry> allMap = new HashMap<String, RolenameEntry>();
		for (AttributedElementClass aec : getDirectSuperClasses()) {
			VertexClass vc = (VertexClass) aec;
			allMap.putAll(vc.getRolenameMap());
		}
		allMap.putAll(getOwnRolenameMap());
		return allMap;
	}
	
	
	
	public Map<String, RolenameEntry> getValidRolenameMap() {
		Map<String, RolenameEntry> map = getRolenameMap();
		for (String s : getInvalidRoles()) {
			map.remove(s);
		}	
		return map;
	}
	
	public Map<String, RolenameEntry> getInvalidRolenameMap() {
		Map<String, RolenameEntry> map = getRolenameMap();
		Set<String> keySet = new HashSet<String>();
		keySet.addAll(map.keySet());
		Set<String> invalidRolenames = getInvalidRoles();
		for (String s : keySet) {
			if (!invalidRolenames.contains(s))
				map.remove(s);
		}
		return map;
	}
	
	/*
	 * This method is needed to allow the following code
	 * 
	 * 
	 * VertexClass vertexClass = graph.getGraphClass().getVertexClass("V1");
     * Vertex vertex = (Vertex)graph.createVertex(vertexClass.getM1Class());
     *
	 * (non-Javadoc)
	 * @see de.uni_koblenz.jgralab.impl.AttributedElementClassImpl#getM1Class()
	 */
	@SuppressWarnings("unchecked")
	public Class<Vertex> getM1Class() {
		Class<Vertex> v = super.getM1Class();
		return v;
	}
	
	/*
	 * This method is needed to allow the following code
	 * 
	 * 
	 * VertexClass vertexClass = graph.getGraphClass().getVertexClass("V1");
     * Vertex vertex = (Vertex)graph.createVertex(vertexClass.getM1Class());
     *
	 * (non-Javadoc)
	 * @see de.uni_koblenz.jgralab.impl.AttributedElementClassImpl#getM1Class()
	 */
	@SuppressWarnings("unchecked")
	public Class<Vertex> getM1ImplementationClass() {
		Class<Vertex> v = super.getM1ImplementationClass();
		return v;
	}
	
}
