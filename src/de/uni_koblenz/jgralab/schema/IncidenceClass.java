/*
 * JGraLab - The Java graph laboratory
 * (c) 2006-2010 Institute for Software Technology
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

public interface IncidenceClass {
	
	/**
	 * @return the upper multiplicity, i.e. the maximal number of edges connected to the vertex at the opposite end
	 */
	public int getMax();
	
	
	/**
	 * @return the lower multiplicity, i.e. the minimal number of edges connected to the vertex at the opposite end
	 */
	public int getMin();
	
	
	/**
	 * @return the direction of this incidenceclass - either Vertex (from edge to vertex) or edge (from vertex to edge)
	 */
	public IncidenceDirection getDirection();
	
	/**
	 * @return the name of this incidence class, i.e. the rolename of the edge end
	 */
	public String getRolename();
	
	
	/**
	 * @return the type of this IncidenceClass, NONE for a normal edge end, AGGREGATION for an aggregation end and COMPOSITION for a composition end  
	 */
	public AggregationKind getAggregationKind();
	
	/**
	 * sets the type of this IncidenceClass, NONE for a normal edge end, AGGREGATION for an aggregation end and COMPOSITION for a composition end  
	 */
	public void setAggregationKind(AggregationKind kind);
	
	
	/**
	 * @return the set of IncidenceClasses which are subsetted (i.e. specialized) by this IncidenceClass
	 */
	public Set<IncidenceClass> getSubsettedIncidenceClasses();
	
	/**
	 * @return the set of IncidenceClasses which are directly subsetted (i.e. specialized) by this IncidenceClass
	 */
	public Set<IncidenceClass> getOwnSubsettedIncidenceClasses();
	
	
	/**
	 * @return the set of IncidenceClasses which are redefined (i.e. specialized and overwritten) by this IncidenceClass
	 */
	public Set<IncidenceClass> getRedefinedIncidenceClasses();
	
	
	/**
	 * @return the set of IncidenceClasses which are directly redefined (i.e. specialized and overwritten) by this IncidenceClass
	 */
	public Set<IncidenceClass> getOwnRedefinedIncidenceClasses();
	
	
	/**
	 * @return the VertexClass this IncidenceClass is connected to
	 */
	public VertexClass getVertexClass();
	

	/**
	 * @return the EdgeClass this IncidenceClass is connected to
	 */
	public EdgeClass getEdgeClass();
	
	
	/**
	 * @return the set of all role names valid for this IncidenceClass
	 */
	public Set<String> getAllRoles();
	
	/**
	 * @return the set of roles which are redefined by this IncidenceClass
	 */
	public Set<String> getRedefinedRoles();

	
	/** 
	 * Marks a role which is already subsetted by this IncidenceClass
	 * as redefined. 
	 */
	public void addRedefinedRole(String rolename);
	
	/** 
	 * Marks a set of roles which are already subsetted by this IncidenceClass
	 * as redefined. 
	 */
	public void addRedefinedRoles(Set<String> rolenames);


	/**
	 * @return the IncidenceClass at the other end of the EdgeClass this IncidenceClass belongs to
	 */
	public IncidenceClass getOpposite();



	
	
}
