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
package de.uni_koblenz.jgralab.impl.std;

import java.util.ArrayList;
import java.util.Collection;

import de.uni_koblenz.jgralab.JGraLabCloneable;

/**
 * 
 * @author
 * 
 * @param <E>
 */
public class JGraLabListImpl<E> extends ArrayList<E> implements
		de.uni_koblenz.jgralab.JGraLabList<E> {

	public JGraLabListImpl(int initialCapacity) {
		super(initialCapacity);
	}

	public JGraLabListImpl(Collection<? extends E> collection) {
		super(collection);
	}

	public JGraLabListImpl() {
		super();
	}

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	@SuppressWarnings("unchecked")
	@Override
	public JGraLabListImpl<E> clone() {
		JGraLabListImpl<E> copy = new JGraLabListImpl<E>();
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
