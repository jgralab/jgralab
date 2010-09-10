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
package de.uni_koblenz.jgralab.algolib.functions;

/**
 * Interface for creating objects defining binary functions. Unlike unary
 * functions defined by the interface <code>Function</code>, this interface does
 * not provide iterators. They would be too expensive. For boolean ranges, the
 * interface <code>Relation</code> should be used. The methods work in analogy
 * to the methods in the interface <code>Function</code>.
 * 
 * @author strauss@uni-koblenz.de
 * 
 * @param <DOMAIN1>
 *            the first domain of this binary function
 * @param <DOMAIN2>
 *            the second domain of this binary function
 * @param <RANGE>
 *            the range of this binary function
 */
public interface BinaryFunction<DOMAIN1, DOMAIN2, RANGE> {
	public RANGE get(DOMAIN1 parameter1, DOMAIN2 parameter2);

	public void set(DOMAIN1 parameter1, DOMAIN2 parameter2, RANGE value);

	public boolean isDefined(DOMAIN1 parameter1, DOMAIN2 parameter2);

}
