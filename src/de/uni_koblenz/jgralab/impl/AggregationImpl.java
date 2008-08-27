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
 
package de.uni_koblenz.jgralab.impl;

import de.uni_koblenz.jgralab.Aggregation;
import de.uni_koblenz.jgralab.EdgeDirection;
import de.uni_koblenz.jgralab.Graph;
import de.uni_koblenz.jgralab.schema.EdgeClass;

public abstract class AggregationImpl extends EdgeImpl implements Aggregation {

	public AggregationImpl(int anId, Graph aGraph, EdgeClass theClass) {
		super(anId, aGraph, theClass);
	}
	
	public Aggregation getNextAggregationInGraph() {
		return (Aggregation)getNextEdgeOfClassInGraph(Aggregation.class);
	}
	
	public Aggregation getNextAggregationInGraph(boolean noSubClasses) {
		return (Aggregation)getNextEdgeOfClassInGraph(Aggregation.class, noSubClasses);
	}
	
	public Aggregation getNextAggregation() {
		return (Aggregation)getNextEdgeOfClass(Aggregation.class);
	}

	public Aggregation getNextAggregation(EdgeDirection orientation) {
		return (Aggregation)getNextEdgeOfClass(Aggregation.class, orientation);
	}
	
	public Aggregation getNextAggregation(EdgeDirection orientation, boolean noSubClasses) {
		return (Aggregation)getNextEdgeOfClass(Aggregation.class, orientation, noSubClasses);
	}

	public Aggregation getNextAggregation(boolean noSubClasses) {
		return (Aggregation)getNextEdgeOfClass(Aggregation.class, noSubClasses);
	}
	
}
