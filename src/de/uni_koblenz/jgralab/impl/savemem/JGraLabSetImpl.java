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
package de.uni_koblenz.jgralab.impl.savemem;

import java.util.Collection;
import java.util.HashSet;

import de.uni_koblenz.jgralab.JGraLabCloneable;
import de.uni_koblenz.jgralab.JGraLabSet;

/**
 * FIXME This is a 1:1 clone of the code found in std.
 * 
 * @author
 * 
 * @param <E>
 */
public class JGraLabSetImpl<E> extends HashSet<E> implements JGraLabSet<E> {

	private static final long serialVersionUID = 5890950480302617008L;

	public JGraLabSetImpl(Collection<? extends E> collection) {
		super(collection);
	}

	public JGraLabSetImpl(int initialCapacity) {
		super(initialCapacity);
	}

	public JGraLabSetImpl() {
		super();
	}

	public JGraLabSetImpl(int initialCapacity, float loadFactor) {
		super(initialCapacity, loadFactor);
	}

	@SuppressWarnings("unchecked")
	@Override
	public JGraLabSetImpl<E> clone() {
		JGraLabSetImpl<E> copy = new JGraLabSetImpl<E>();
		for (E element : this) {
			if (element instanceof JGraLabCloneable) {
				copy.add((E) ((JGraLabCloneable) element).clone());
			} else {
				copy.add(element);
			}
		}
		return copy;
	}
}
