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
 
package de.uni_koblenz.jgralab.schema;

import java.util.ArrayList;
import java.util.List;

import de.uni_koblenz.jgralab.schema.impl.VertexEdgeEntry;



public class RolenameEntry {


	
	
	private DirectedEdgeClass edgeClassToTraverse;
	
	private VertexClass vertexClassAtFarEnd;
	
	private String roleNameAtFarEnd;
	
	/**
	 * holds the information to which vertex class which edge should be used
	 */
	private List<VertexEdgeEntry> vertexEdgeEntryList;
	
	private boolean redefined;
	
	/* toggles if this entry was inherited from a superclass */
	private boolean inherited;
	
	/* holds the class that defines the rolename. 
	 * Holds the class the rolename belongs to or the
	 * defining superclass if the rolename is an 
	 * inherited one
	 */
	private VertexClass vertexClassDefiningRolename;

	public RolenameEntry(VertexClass definingClass, String roleNameAtFarEnd, DirectedEdgeClass edgeClassToTraverse,
			VertexClass vertexClassAtFarEnd) {
		super();
		vertexClassDefiningRolename = definingClass;
		vertexEdgeEntryList = new ArrayList<VertexEdgeEntry>();
		addVertexWithEdge(vertexClassAtFarEnd, edgeClassToTraverse);
		this.edgeClassToTraverse = edgeClassToTraverse;
		this.vertexClassAtFarEnd = vertexClassAtFarEnd;
		this.roleNameAtFarEnd = roleNameAtFarEnd;
		this.redefined = false;
		this.inherited = false;
	}

	public DirectedEdgeClass getEdgeClassToTraverse() {
		return edgeClassToTraverse;
	}

	public void setEdgeClassToTraverse(DirectedEdgeClass edgeClassToTraverse) {
		this.edgeClassToTraverse = edgeClassToTraverse;
	}

	public VertexClass getVertexClassAtFarEnd() {
		return vertexClassAtFarEnd;
	}

	public void setVertexClassAtFarEnd(VertexClass vertexClassAtFarEnd) {
		this.vertexClassAtFarEnd = vertexClassAtFarEnd;
	}

	public String getRoleNameAtFarEnd() {
		return roleNameAtFarEnd;
	}

	public void setRoleNameAtFarEnd(String roleNameAtFarEnd) {
		this.roleNameAtFarEnd = roleNameAtFarEnd;
	}

	public boolean isRedefined() {
		return redefined;
	}

	public void setRedefined(boolean redefined) {
		for (VertexEdgeEntry entry : vertexEdgeEntryList)
			entry.setRedefined(true);
		this.redefined = redefined;
	}

	public boolean isInherited() {
		return inherited;
	}

	public void setInherited(boolean inherited) {
		this.inherited = inherited;
	}

	public VertexClass getVertexClassDefiningRolename() {
		return vertexClassDefiningRolename;
	}

	public void setVertexClassDefiningRolename(VertexClass vertexClassDefiningRolename) {
		this.vertexClassDefiningRolename = vertexClassDefiningRolename;
	}
	
	public void addVertexWithEdge(VertexClass vertex, DirectedEdgeClass edge) {
		for (VertexEdgeEntry entry : vertexEdgeEntryList) {
			if (!edge.getEdgeClass().isAbstract() && entry.getVertex() == vertex) {
				if (entry.getEdge().isAbstract())
					continue;
				if (edge.getEdgeClass() == entry.getEdge() && edge.getDirection() == entry.getDirection())
					return;
				else
					throw new SchemaException("Rolename '" + roleNameAtFarEnd + "' used to connect to the " +
							"same VertexClass " + vertex.getQualifiedName() +
							" with two different EdgeClasses " + edge.getEdgeClass().getQualifiedName() + 
							" and " + entry.getEdge().getQualifiedName() + 
							" at VertexClass " + vertexClassDefiningRolename.getQualifiedName() + " or a " + 
							" subclass");
			}
		}
		vertexEdgeEntryList.add(new VertexEdgeEntry(vertex, edge, false));
	}
	
	public List<VertexEdgeEntry> getVertexEdgeEntryList() {
		return vertexEdgeEntryList;
	}
	
}
