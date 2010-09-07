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
/**
 * 
 */
package de.uni_koblenz.jgralab.greql2.funlib.pathsearch;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import de.uni_koblenz.jgralab.Vertex;
import de.uni_koblenz.jgralab.greql2.evaluator.fa.State;

public class VertexStateQueue {
	
	private static int initialSize = 100;
	
	public Vertex currentVertex= null;
	
	public State currentState = null;

	int size = initialSize;
	
	Vertex[] vertices = null;
	
	State[] states = null;
	
	int last = 0;
	
	int first = 0;
	
	public VertexStateQueue() {
		vertices = new Vertex[initialSize];
		states = new State[initialSize];
		size = initialSize;
	}
	
	public final void put(Vertex v, State s) { 
		if (last == first + size - 1) {
			resize();
		}	
		vertices[last % size] = v;
		states[last % size] = s;
		last++;
	}

    public final boolean hasNext() {
    	
    	if (first == last) {
//    		if (last < (initialSize / 2)) {
//    			initialSize = last+1;
//    		}
    		return false;
    	}	
	    currentVertex = vertices[first%size];
	    currentState = states[first%size];
	    first++;
	    return true;
    }
    
    
    private final void resize() {
    	Vertex[] newVertices = new Vertex[size * 2];
    	State[] newStates = new State[size * 2];
    	
    	for (int i = 0; i < size; i++) {
    		newVertices[i] = vertices[(first+i)%size];
    		newStates[i] = states[(first+i)%size];
    	}
    	states = newStates;
    	vertices = newVertices;
    	last = size-1;
    	first = 0;
    	size *= 2;
    //	initialSize *= 2;
    }
    
	@Test
	public void test() {
		for (int j = 0; j < 100; j++) {
		VertexStateQueue q = new VertexStateQueue();
		State s = new State();
		s.number = 1;
		q.put(null, s);
		int count = 2;
		int testNumber = 1;
		while (q.hasNext()) {
			State c = q.currentState;
			if (count < 10000) {
				State n = new State();
				n.number = count++;
				q.put(null, n);
				if (count % 3 == 0) {
					n = new State();
					n.number = count++;
					q.put(null, n);
				}
			}
			//System.out.println("Current Number: " + c.number );
			assertEquals(testNumber, c.number);
			testNumber++;
		}
		assertEquals( 10000, testNumber);
		}
	}
    
}
