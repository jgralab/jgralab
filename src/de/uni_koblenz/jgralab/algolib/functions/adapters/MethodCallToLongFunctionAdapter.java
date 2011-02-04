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
package de.uni_koblenz.jgralab.algolib.functions.adapters;

import java.util.Iterator;

import de.uni_koblenz.jgralab.algolib.functions.LongFunction;
import de.uni_koblenz.jgralab.algolib.functions.entries.LongFunctionEntry;

public abstract class MethodCallToLongFunctionAdapter<DOMAIN> implements
		LongFunction<DOMAIN> {

	
	@Override
	public Iterable<DOMAIN> getDomainElements() {
		throw new UnsupportedOperationException(
				"This function is a method call and cannot iterate over the domain elements.");
	}

	@Override
	public void set(DOMAIN parameter, long value) {
		throw new UnsupportedOperationException("This function is immutable.");
	}

	@Override
	public Iterator<LongFunctionEntry<DOMAIN>> iterator() {
		throw new UnsupportedOperationException(
				"This function is a method call and has no function entries to iterate over.");
	}
}
