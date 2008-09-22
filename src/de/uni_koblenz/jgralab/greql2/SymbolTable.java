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
 
package de.uni_koblenz.jgralab.greql2;

import java.util.HashMap;
import java.util.LinkedList;

import de.uni_koblenz.jgralab.EdgeDirection;
import de.uni_koblenz.jgralab.Vertex;
import de.uni_koblenz.jgralab.greql2.exception.DuplicateVariableException;
import de.uni_koblenz.jgralab.greql2.schema.Greql2Aggregation;
import de.uni_koblenz.jgralab.greql2.schema.IsBoundVarOf;
import de.uni_koblenz.jgralab.greql2.schema.IsDeclaredVarOf;
import de.uni_koblenz.jgralab.greql2.schema.IsVarOf;
import de.uni_koblenz.jgralab.greql2.schema.SourcePosition;


public class SymbolTable 
{
	
	private LinkedList<HashMap<String, Vertex> > list = null;

	public SymbolTable()
	{
		
		list = new LinkedList<HashMap<String, Vertex> >();
	}
	
	
	public void blockBegin()
	{
		HashMap<String, Vertex> map = new HashMap<String, Vertex>();
		list.addFirst(map);		
	}
	
	public void blockEnd()
	{
		if (! list.isEmpty())
			list.removeFirst();
		
	}
	
	public void insert(String ident, Vertex v) throws DuplicateVariableException
	{
		
		if (! list.getFirst().containsKey(ident))
		{
			list.getFirst().put(ident, v);			
		}
		else
		{	
			Vertex var = list.get(0).get(ident);
			int offset  = -1;
			if (var.getFirstEdge(EdgeDirection.OUT) instanceof IsDeclaredVarOf)
			{
				offset = ((IsDeclaredVarOf) var.getFirstEdge(EdgeDirection.OUT)).getSourcePositions().get(0).offset;
			}
			else if (var.getFirstEdge(EdgeDirection.OUT) instanceof IsBoundVarOf)
			{				
				offset = ((IsBoundVarOf) var.getFirstEdge(EdgeDirection.OUT)).getSourcePositions().get(0).offset;			
			}
			else if (var.getFirstEdge(EdgeDirection.OUT) instanceof IsVarOf)
			{				
				offset = ((IsVarOf) var.getFirstEdge(EdgeDirection.OUT)).getSourcePositions().get(0).offset;			
			}
			throw new DuplicateVariableException(ident, ((Greql2Aggregation) v.getFirstEdge(EdgeDirection.IN)).getSourcePositions(), new SourcePosition(offset, ident.length()));
		}
	}
	
	public Vertex lookup(String ident) 
	{
		for (int i = 0; i< list.size(); i++)
		{
			if (list.get(i).containsKey(ident))
				return list.get(i).get(ident); 
		}
		return null;	   
	}
	
}
